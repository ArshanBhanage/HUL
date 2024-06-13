package com.hul.screens.field_auditor_dashboard.ui.image_preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.hul.R

class ImagePreviewDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_IMAGE_URI = "image_uri"

        fun newInstance(imageUri: Uri): ImagePreviewDialogFragment {
            val fragment = ImagePreviewDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_IMAGE_URI, imageUri)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_preview_dialog, container, false)
        val imageView: ImageView = view.findViewById(R.id.imageView)

        arguments?.getParcelable<Uri>(ARG_IMAGE_URI)?.let { imageUri ->
            val bitmap = getBitmapFromUri(imageUri)
            imageView.setImageBitmap(bitmap)
        }

        return view
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        activity?.contentResolver?.openInputStream(uri)?.use { inputStream ->
            return BitmapFactory.decodeStream(inputStream)
        }
        return null
    }
}
