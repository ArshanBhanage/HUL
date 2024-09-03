package com.hul.camera.cameraPreview

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.telecom.TelecomManager.EXTRA_LOCATION
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.hul.HULApplication
import com.hul.R
import com.hul.api.controller.APIController
import com.hul.camera.CameraComponent
import com.hul.databinding.FragmentCameraPreviewBinding
import com.hul.user.UserInfo
import com.hul.utils.cancelProgressDialog
import com.hul.utils.setProgressDialog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class CameraPreviewFragment : Fragment() {

    private var _binding: FragmentCameraPreviewBinding? = null

    private lateinit var cameraComponent: CameraComponent

    @Inject
    lateinit var cameraPreviewViewModel: CameraPreviewViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    var rotaion = -90f;

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService


    private var startTime: Long = 0
    private var isRunning = false
    private var elapsedTime: Long = 0

    private lateinit var orientationEventListener: OrientationEventListener
    var imageType = ""

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // TODO: Step 1.1, Review variables (no changes).
// FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you
// should receive updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Used only for local storage of the last known location. Usually, this would be saved to your
// database, but because this is a simplified sample without a full database, we only need the
// last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null


    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                checkLocationSettings()
            } else {
                showInformationMessage()
            }
        }

    private fun showInformationMessage() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Permissions Needed")
            .setMessage("You have denied the permissions. Please go to settings and allow the permissions manually.")
            .setPositiveButton("Settings") { dialog, _ ->
                requestPermissionSettings()
                dialog.dismiss() // This dismisses the dialog
            }
            .setCancelable(false)
            .show()
    }

    private fun requestPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        requestPermissionSetting.launch(intent)
    }


    private val requestPermissionSetting =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { permissions ->
            if(!allPermissionsGranted()) {
                showInformationMessage()
            }
        }


    private val requestLocation =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { permissions ->
            checkLocationSettings()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCameraPreviewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        cameraComponent =
            (activity?.application as HULApplication).appComponent.cameraComponent()
                .create()
        cameraComponent.inject(this)
        binding.viewModel = cameraPreviewViewModel

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireArguments() != null) {
            imageType = requireArguments().getString("imageType")!!
        }
        if (allPermissionsGranted()) {
            checkCameratype()
        } else {
            requestPermission()
        }

        // set on click listener for the button of capture photo
        // it calls a method which is implemented below
        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }
        outputDirectory = getOutputDirectory()

        orientationEventListener =
            object : OrientationEventListener(requireContext(), SensorManager.SENSOR_DELAY_NORMAL) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation == ORIENTATION_UNKNOWN) return

                    // Determine the current orientation
                    rotaion = when {
                        orientation in 45..134 -> 180f
                        orientation in 135..224 -> if (imageType.contains("Front")) 90f else -90f
                        orientation in 225..314 -> 0f
                        else -> if (imageType.contains("Front")) -90f else 90f
                    }

                    when {
                        orientation in 45..134 -> {
                            binding.cameraHeader.visibility = View.VISIBLE
                            binding.info.visibility = View.GONE
                        }

                        orientation in 135..224 -> {
                            binding.cameraHeader.visibility = View.GONE
                            binding.info.visibility = View.VISIBLE
                        }

                        orientation in 225..314 -> {
                            binding.cameraHeader.visibility = View.VISIBLE
                            binding.info.visibility = View.GONE
                        }

                        else -> {
                            binding.cameraHeader.visibility = View.GONE
                            binding.info.visibility = View.VISIBLE
                        }
                    }

                    // Handle orientation change
                    //handleOrientationChange(displayRotation)
                }
            }

        // Enable the listener
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }


    }

//    private fun handleOrientationChange(displayRotation: String) {
//        println("Orientation changed to: $displayRotation")
//    }

    fun checkCameratype() {
        try {
            checkLocationSettings()
            startCamera()

        } catch (e: Exception) {
            requireActivity().finish()
        }

    }

    private val REQUEST_CHECK_SETTINGS = 1002

    private fun checkLocationSettings() {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGpsEnabled) {
            //Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show()
            requestLocation()
        } else {
            requestLocationUpdates()
            // Location services are enabled
            //Toast.makeText(this, "Location services are enabled", Toast.LENGTH_SHORT).show()
            // Proceed with location-related operations
        }
    }


    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())


        locationRequest = LocationRequest.create().apply {
            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = 60

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            fastestInterval = 30

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            maxWaitTime = 10

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Normally, you want to save a new location to a database. We are simplifying
                // things a bit and just saving it as a local variable, as we only need it again
                // if a Notification is created (when the user navigates away from app).
                currentLocation = locationResult.lastLocation

                cameraPreviewViewModel.longitude.value = currentLocation!!.longitude.toString()
                cameraPreviewViewModel.lattitude.value = currentLocation!!.latitude.toString()
                if (cameraPreviewViewModel.uri.value != null) {
                    cancelProgressDialog()
                    redurectToImagePreview(cameraPreviewViewModel.uri.value!!)
                }
            }
        }


        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        fetchLastKnownLocation()
//        locationCallback = object : LocationCallback() {
//            //This callback is where we get "streaming" location updates. We can check things like accuracy to determine whether
//            //this latest update should replace our previous estimate.
//            override fun onLocationResult(locationResult: LocationResult) {
//
//                if (locationResult == null) {
//                    Log.d(TAG, "locationResult null")
//                    return
//                }
//                Log.d(TAG, "received " + locationResult.locations.size + " locations")
//                for (loc in locationResult.locations) {
//                    cameraPreviewViewModel.longitude.value = loc.longitude.toString()
//                    cameraPreviewViewModel.lattitude.value = loc.latitude.toString()
//                    if (cameraPreviewViewModel.uri.value != null) {
//                        cancelProgressDialog()
//                        redurectToImagePreview(cameraPreviewViewModel.uri.value!!)
//                    }
//                }
//            }
//
//            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
//                Log.d(TAG, "locationAvailability is " + locationAvailability.isLocationAvailable)
//                super.onLocationAvailability(locationAvailability)
//            }
//        }

//        val locationRequest = LocationRequest.create().apply {
//            interval = 100 // Update interval in milliseconds
//            fastestInterval = 500 // Fastest update interval in milliseconds
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//
//        fusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            null /* Looper */
//        )
    }

    private fun fetchLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val location: Location? = task.result
                    currentLocation = location
                    cameraPreviewViewModel.longitude.value = location!!.longitude.toString()
                    cameraPreviewViewModel.lattitude.value = location!!.latitude.toString()
                    if (cameraPreviewViewModel.uri.value != null) {
                        cancelProgressDialog()
                        redurectToImagePreview(cameraPreviewViewModel.uri.value!!)
                    }
                } else {
                    // Handle the case where no location is available
                }
            }
    }


    fun requestPermission() {
        requestPermission.launch(REQUIRED_PERMISSIONS)
    }

    fun requestLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        requestLocation.launch(intent)
    }


    private fun takePhoto() {
        // Get a stable reference of the
        // modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener,
        // which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    cameraPreviewViewModel.uri.value = Uri.fromFile(photoFile)
                    orientationEventListener.disable()
                    //startFetchingLastLocation()
                    ensureLandscapeOrientation(photoFile)

                    if (currentLocation != null) {
                        redurectToImagePreview(cameraPreviewViewModel.uri.value!!)

                    } else {
                        setProgressDialog(requireContext(), "Processing Image")
                    }

                    // set the saved uri to the image view
//                    binding.ivCapture.visibility = View.VISIBLE
//                    binding.ivCapture.setImageURI(savedUri)


                    //val msg = "Photo capture succeeded: $savedUri"
                    //Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    //Log.d(TAG, msg)
                }
            })
    }

    private fun ensureLandscapeOrientation(photoFile: File) {
        val bitmap = BitmapFactory.decodeFile(photoFile.path)

        // Rotate the image to landscape if it is in portrait
        val matrix = Matrix()
        matrix.postRotate(rotaion)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)


        // Save the rotated bitmap back to the file
        FileOutputStream(photoFile).use { out ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

    }


    fun redurectToImagePreview(uri: Uri) {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        val bundle = Bundle()
        bundle.putString("lattitude", cameraPreviewViewModel.lattitude.value)
        bundle.putString("longitude", cameraPreviewViewModel.longitude.value)
        bundle.putString("imageUri", uri.toString())
        bundle.putInt("position", requireArguments().getInt("position", 0))
        bundle.putString("heading", requireArguments().getString("heading"))
        bundle.putString("tag", requireArguments().getString("tag"))
        if (bundle.getString("visitData") != null) {
            bundle.putString("visitData", bundle.getString("visitData"))
        }
        findNavController().navigate(
            R.id.action_cameraPreviewFragment_to_imagePreviewFragment,
            bundle
        )
    }

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null


    private fun startCamera() {
        binding.cameraHeader.visibility = View.VISIBLE
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        val cameraSelector: CameraSelector
        // Select back camera as a default
        if (requireArguments().getString("imageType")!!.contains("Front")) {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
        cameraProviderFuture.addListener(Runnable {

            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()



            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    // creates a folder inside internal storage
    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }


    companion object {
        private const val TAG = "CameraXGFG"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R){
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                )
            }
            else{
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )

            }
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener.disable()
        try {
            if (!requireArguments().getString("imageType").equals("Video")) {
                val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                removeTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Location Callback removed.")
                    } else {
                        Log.d(TAG, "Failed to remove Location Callback.")
                    }
                }
            }
            cameraExecutor.shutdown()
        } catch (e: Exception) {

        }
    }

    override fun onResume() {
        super.onResume()
        super.onResume()
        orientationEventListener.enable()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}