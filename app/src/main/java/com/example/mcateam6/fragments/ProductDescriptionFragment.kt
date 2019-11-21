package com.example.mcateam6.fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.mcateam6.R
import com.example.mcateam6.activities.AddProductFormPage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.fragment_product_description.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


const val REQUEST_CAMERA = 0
const val REQUEST_GALLERY = 1

class ProductDescriptionFragment : AddProductFormPageFragment() {

    override val formPage = AddProductFormPage.DESCRIPTION

    private var lastDescriptionCursorPosition: Int = 0
    private var description: String = ""

    private val addPhotoBottomDialog = AddPhotoBottomDialogFragment(
        this::onSelectCamera,
        this::onSelectGallery,
        this::onSelectRemove
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pagedFormModel.setValid(formPage)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_product_description, container, false)

        initDescriptionEdit(v)

        v.findViewById<FloatingActionButton>(R.id.edit_image_fab).setOnClickListener {
            addPhotoBottomDialog.show(
                activity!!.supportFragmentManager,
                "add_photo_dialog_fragment"
            )
        }

        productModel.imageUri.also { uri ->
            if (uri != Uri.EMPTY) {
                v.findViewById<ImageView>(R.id.product_image).setImageURI(uri)
            }
        }

        return v
    }

    private fun initDescriptionEdit(v: View) {
        val descriptionEdit = v.findViewById<EditText>(R.id.description_edit)

        descriptionEdit.setText(productModel.description)

        addFormListener(descriptionEdit)
    }

    private fun addFormListener(editText: EditText) {
        editText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    lastDescriptionCursorPosition = editText.selectionStart
                    description = editText.text.toString()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    editText.removeTextChangedListener(this)

                    if (editText.lineCount > 5) {
                        editText.setText(description)
                        editText.setSelection(lastDescriptionCursorPosition)
                    }

                    editText.addTextChangedListener(this)

                    productModel.description = description_edit.text.toString()
                }
            }
        )
    }

    private fun onSelectCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File
            null
        }

        photoFile?.also {
            productModel.imageUri = FileProvider.getUriForFile(
                activity!!,
                "com.example.mcateam6.fileprovider",
                it
            )

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, productModel.imageUri)
            startActivityForResult(takePictureIntent, REQUEST_CAMERA)
        }
    }

    private fun onSelectGallery() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(pickPhoto, REQUEST_GALLERY)
    }

    private fun onSelectRemove() {
        productModel.imageUri = Uri.EMPTY
        product_image.setImageResource(R.drawable.no_image)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                REQUEST_CAMERA -> if (resultCode == Activity.RESULT_OK) {
                    cropImage()
                }

                REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.also { uri ->
                        productModel.imageUri = uri
                        cropImage()
                    }
                }

                UCrop.REQUEST_CROP -> product_image.setImageURI(productModel.imageUri)
            }
        }
    }

    private fun cropImage() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File
            null
        }

        photoFile?.also { file ->
            val uncroppedUri = productModel.imageUri

            val croppedUri = Uri.fromFile(file)

            productModel.imageUri = FileProvider.getUriForFile(
                activity!!,
                "com.example.mcateam6.fileprovider",
                file
            )

            UCrop.of(uncroppedUri, croppedUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(400, 400)
                .start(activity!!, this)
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }
}
