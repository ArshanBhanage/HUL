package com.hul.web_form

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.ui.AppBarConfiguration
import com.hul.R
import com.hul.camera.CameraActivity
import com.hul.data.FormElement
import com.hul.databinding.ActivityMainBinding
import com.hul.databinding.ActivityWebFormBinding
import com.hul.databinding.LayoutFileUploadBinding

class WebForm : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityWebFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebFormBinding.inflate(layoutInflater)
        var formElements = ArrayList<FormElement>()

        formElements.add(
            FormElement(
                type = "edittext",
                label = "Name",
                placeholder = "Enter your name"
            )
        )

        formElements.add(
            FormElement(
                type = "edittext",
                label = "Name",
                placeholder = "Enter your name"
            )
        )

        formElements.add(
            FormElement(
                type = "edittext",
                label = "Name",
                placeholder = "Enter your name"
            )
        )

        formElements.add(
            FormElement(
                type = "edittext",
                label = "Name",
                placeholder = "Enter your name"
            )
        )

        formElements.add(
            FormElement(
                type = "radiobutton",
                label = "Gender",
                options = arrayListOf("Male", "Female")
            )
        )

        formElements.add(
            FormElement(
                type = "dropdown",
                label = "Country",
                options = arrayListOf("USA", "Canada", "UK")
            )
        )
        createForm(formElements)

        setContentView(binding.root)
    }

    private fun createForm(formElements: List<FormElement>) {
        for (element in formElements) {
            when (element.type) {
                "edittext" -> inflateEditTextLayout(element)
                "radiobutton" -> inflateRadioButtonLayout(element)
                "dropdown" -> inflateSpinnerLayout(element)
            }
        }
    }

    private fun inflateEditTextLayout(element: FormElement) {
        val view = layoutInflater.inflate(R.layout.layout_edittext, binding.formContainer, false)
        view.findViewById<TextView>(R.id.label).text = element.label
        view.findViewById<EditText>(R.id.editText).hint = element.placeholder
        binding.formContainer.addView(view)
    }

    private fun inflateRadioButtonLayout(element: FormElement) {
        val view = layoutInflater.inflate(R.layout.layout_radiobutton, binding.formContainer, false)
        view.findViewById<TextView>(R.id.label).text = element.label
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        element.options?.forEach { option ->
            val radioButton = RadioButton(this).apply {
                text = option
            }
            radioGroup.addView(radioButton)
        }
        binding.formContainer.addView(view)
    }

    private fun inflateSpinnerLayout(element: FormElement) {
        val view = LayoutFileUploadBinding.inflate(layoutInflater)
        view.label.text = element.label
        view.capture.setOnClickListener {
            redirectToCamera(0, "Back", getString(R.string.school_pic1))
        }
//        val spinner = view.findViewById<Spinner>(R.id.spinner)
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, element.options ?: listOf())
//        spinner.adapter = adapter
        binding.formContainer.addView(view.root)
    }

    private fun redirectToCamera(position: Int, imageType: String, heading: String) {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("imageType", imageType)
        intent.putExtra("heading", heading)
        startImageCapture.launch(intent)
    }

    val startImageCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val position = data!!.getIntExtra("position", 0)
                val imageUrl = result.data!!.getStringExtra("imageUrl")

//                startTimer()
//
//                // Update the view model's imageUrl at the corresponding position
//                when (position) {
//                    0 -> form1FillViewModel.imageUrl1.value = imageUrl
//                    1 -> form1FillViewModel.imageUrl2.value = imageUrl
//                    2 -> form1FillViewModel.imageUrl3.value = imageUrl
//                    3 -> form1FillViewModel.imageUrl4.value = imageUrl
//                }
            }
        }

}