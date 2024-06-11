package com.hul.camera.imagePreview

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.hul.HULApplication
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.api.controller.UploadFileController
import com.hul.camera.CameraComponent
import com.hul.data.RequestModel
import com.hul.databinding.FragmentImagePreviewBinding
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import com.hul.utils.setProgressDialog
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import javax.inject.Inject


class ImagePreviewFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentImagePreviewBinding? = null

    private lateinit var cameraComponent: CameraComponent

    @Inject
    lateinit var imagePreviewViewModel: ImagePreviewViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    @Inject
    lateinit var uploadFileController: UploadFileController

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        cameraComponent =
            (activity?.application as HULApplication).appComponent.cameraComponent()
                .create()
        cameraComponent.inject(this)
        binding.viewModel = imagePreviewViewModel
        binding.heading.text = requireArguments().getString("heading")
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.save.setOnClickListener {
            val returnIntent = Intent()

            returnIntent.putExtra(
                "position",
                requireArguments().getInt("position", 0)
            )
            returnIntent.putExtra(
                "imageUrl",
                imagePreviewViewModel.uri.value.toString()
            )

            requireActivity().setResult(Activity.RESULT_OK, returnIntent)
            requireActivity().finish()
        }
        imagePreviewViewModel.lattitude.value = requireArguments().getString("lattitude")
        imagePreviewViewModel.longitude.value = requireArguments().getString("longitude")

        imagePreviewViewModel.uri.value = addTimestampToImage(
            requireContext(),
            requireArguments().getString("imageUri")!!.toUri()
        )

        val imageUri = imagePreviewViewModel.uri.value
        binding.ivCapture.setImageURI(imageUri)


        binding.retake.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.date.text = SimpleDateFormat("dd MMM yyyy").format(Date())
        binding.time.text = SimpleDateFormat("hh:mm:ss").format(Date())


    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Video.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor?.moveToFirst()
            column_index?.let { cursor!!.getString(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            cursor?.close()
        }
    }

    private fun copyFontToInternalStorage(context: Context, fontFileName: String): String {
        val outputFile = File(context.filesDir, fontFileName)
        if (!outputFile.exists()) {
            try {
                val inputStream: InputStream = context.assets.open(fontFileName)
                val outputStream = FileOutputStream(outputFile)
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        Log.d("File Path", outputFile.absolutePath)

        return outputFile.absolutePath
    }

    fun addTimestampToImage(context: Context, imageUri: Uri): Uri? {
        val originalExif = ExifInterface(imageUri.path!!)
        // Load the original image
        val originalBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)

        // Correct orientation if needed
        val correctedBitmap = correctOrientation(originalBitmap, context, imageUri)

        // Create a mutable bitmap to draw onto
        val mutableBitmap = correctedBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Get the dimensions of the bitmap
        val bitmapWidth = mutableBitmap.width
        val bitmapHeight = mutableBitmap.height

        // Calculate the text size and padding based on the image dimensions
        val textSizeInPixels = 0.03f * bitmapHeight
        val padding = 16f * bitmapHeight

        // Draw the timestamp onto the bitmap
        val canvas = Canvas(mutableBitmap)
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        var newString =
            "Lon : ${imagePreviewViewModel.longitude.value} Lat : ${imagePreviewViewModel.lattitude.value}"
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = textSizeInPixels
        }

        // Calculate the width and height of the timestamp text
        val textWidth = paint.measureText(timeStamp)
        val textHeight = paint.descent() - paint.ascent()


        val x = mutableBitmap.width - textWidth - textSizeInPixels // 50 is for some padding
        val y = mutableBitmap.height - textHeight - textSizeInPixels // 50 is for some padding

        canvas.drawText(timeStamp, x, y, paint)

        // Calculate the width and height of the timestamp text
        val textWidthLocation = paint.measureText(newString)
        val textHeightLocation = paint.descent() - paint.ascent()


        val xLocation =
            mutableBitmap.width - textWidthLocation - textSizeInPixels // 50 is for some padding
        val yLocation =
            mutableBitmap.height - textHeightLocation - (textSizeInPixels * 2) // 50 is for some padding

        val paintLocation = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = textSizeInPixels
        }

        canvas.drawText(newString, xLocation, yLocation, paintLocation)


        // Compress the modified bitmap to achieve a target file size of approximately 150 KB
        var quality = 100 // Start with maximum quality
        var outputStream: ByteArrayOutputStream
        do {
            outputStream = ByteArrayOutputStream()
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            quality -= 5 // Reduce quality in steps until desired file size is achieved
        } while (outputStream.toByteArray().size > 700 * 1024 && quality > 0)


        // Insert the new image into the MediaStore
        val resolver = context.contentResolver
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, timeStamp + ".jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val uri = resolver.insert(contentUri, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { out: OutputStream ->
                outputStream.writeTo(out)
                out.flush()
            }
        }
        copyDateExifData(requireContext(), imageUri, uri!!)

        return uri
    }


    fun copyDateExifData(context: Context, sourceUri: Uri, destinationUri: Uri) {
        try {
            // Get the source file path from the source URI
            val sourceFilePath = getPathFromUri(context, sourceUri)
            if (sourceFilePath == null) {
                // Handle error: Unable to get source file path
                return
            }

            // Get the destination file path from the destination URI
            val destinationFilePath = getPathFromUri(context, destinationUri)
            if (destinationFilePath == null) {
                // Handle error: Unable to get destination file path
                return
            }

            // Create ExifInterface instances for the source and destination files
            val sourceExif = ExifInterface(sourceFilePath)
            val destinationExif = ExifInterface(destinationFilePath)

            // Get the date-related attributes from the source ExifInterface
            val dateAttributes = listOf(
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_DATETIME_DIGITIZED,
                ExifInterface.TAG_DATETIME_ORIGINAL
            )

            // Copy date-related attributes from the source to the destination ExifInterface
            for (attribute in dateAttributes) {
                val value = sourceExif.getAttribute(attribute)
                if (value != null) {
                    destinationExif.setAttribute(attribute, value)
                }
            }

            // Save the changes to the destination file
            destinationExif.saveAttributes()
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle error: IOException occurred
        }
    }

    fun getPathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        when {
            uri.scheme == "content" -> {
                val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        filePath = it.getString(columnIndex)
                    }
                }
            }

            uri.scheme == "file" -> {
                filePath = uri.path
            }

            else -> {
                // Handle other URI schemes if needed
            }
        }
        return filePath
    }

    private fun correctOrientation(bitmap: Bitmap, context: Context, uri: Uri): Bitmap {
        val exif = ExifInterface(context.contentResolver.openInputStream(uri)!!)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun uploadImage() {
        val imageUri = requireArguments().getString("imageUri")!!.toUri()
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Uploading")
            uploadFileController.getApiResponse(
                this,
                imagePreviewViewModel.uri.value!!,
                uploadImageModel(),
                ApiExtentions.ApiDef.UPLOAD_IMAGE.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.UPLOAD_IMAGE.ordinal, this)
        }

    }

    private fun uploadImageModel(): RequestModel {
        return RequestModel(
            leadId = requireArguments().getString("leadId")!!.toDouble().toInt().toString(),
            regNo = requireArguments().getString("regNo")!!,
            docType = requireArguments().getString("imageType")!!
        )
    }

    override fun onApiSuccess(o: String?, objectType: Int) {

        cancelProgressDialog()
        when (ApiExtentions.ApiDef.values()[objectType]) {

            ApiExtentions.ApiDef.UPLOAD_IMAGE -> {
                val model = JSONObject(o.toString())


                requireActivity().finish()

            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {
        redirectionAlertDialogue(requireContext(), message!!)
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.values()[type]) {
            ApiExtentions.ApiDef.UPLOAD_IMAGE -> uploadImage()
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }


}