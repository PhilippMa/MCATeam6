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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.mcateam6.R
import com.example.mcateam6.activities.AddProductFormPage
import com.example.mcateam6.activities.BARCODE_EXTRA
import com.example.mcateam6.activities.LiveBarcodeScanningActivity
import com.example.mcateam6.database.RemoteDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.fragment_product_description.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


const val REQUEST_CAMERA = 0
const val REQUEST_GALLERY = 1
const val REQUEST_BARCODE = 2

class ProductDescriptionFragment : AddProductFormPageAsyncFragment() {

    override val formPage = AddProductFormPage.DESCRIPTION

    private var lastDescriptionCursorPosition: Int = 0
    private var description: String = ""

    private val addPhotoBottomDialog = AddPhotoBottomDialogFragment(
        this::onSelectCamera,
        this::onSelectGallery,
        this::onSelectRemove
    )

    private lateinit var barcodeEdit: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pagedFormModel.setValid(formPage)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_product_description, container, false)

        initDescriptionEdit(v)

        initBarcodeEdit(v)

        initImageChooser(v)

        initScan(v)

        return v
    }

    private fun initScan(v: View) {
        v.findViewById<Button>(R.id.scan_button).setOnClickListener {
            val intent = Intent().setClass(context!!, LiveBarcodeScanningActivity::class.java)
            startActivityForResult(intent, REQUEST_BARCODE)
        }
    }

    private fun initBarcodeEdit(v: View) {
        barcodeEdit = v.findViewById(R.id.barcode_edit)

        barcodeEdit.setText(productModel.barcode)

        barcodeEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                productModel.barcode = barcodeEdit.text.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun initImageChooser(v: View) {
        v.findViewById<FloatingActionButton>(R.id.edit_image_fab).setOnClickListener {
            addPhotoBottomDialog.show(
                activity!!.supportFragmentManager,
                "add_photo_dialog_fragment"
            )
        }

        productModel.imageUri.also { uri ->
            if (uri != Uri.EMPTY) {
                v.findViewById<ImageView>(R.id.product_image).setImageURI(uri)
            } else {
                v.findViewById<ImageView>(R.id.product_image).setImageResource(R.drawable.no_image)
            }
        }
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

                REQUEST_BARCODE -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val barcode = data.getStringExtra(BARCODE_EXTRA)
                    barcode_edit.setText(barcode)
                    productModel.barcode = barcode
                }
            }
        }
    }

    private fun resolveColor(id: Int): Int {
        return ContextCompat.getColor(activity!!, id)
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

            val options = UCrop.Options().apply {
                setStatusBarColor(resolveColor(R.color.colorPrimaryDark))
                setToolbarColor(resolveColor(R.color.colorPrimary))
                setActiveWidgetColor(resolveColor(R.color.colorPrimary))
                setActiveControlsWidgetColor(resolveColor(R.color.colorPrimary))
                setToolbarWidgetColor(resolveColor(R.color.white))
                setToolbarTitle("Adjust Image")
            }

            UCrop.of(uncroppedUri, croppedUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(400, 400)
                .withOptions(options)
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

    override fun asyncValidation(cont: (Boolean) -> Unit) {
        if (productModel.barcode.isNullOrBlank()) {
            cont(true)
            return
        }

        val db = RemoteDatabase()

        db.signIn().addOnSuccessListener {
            db.barcodeExists(productModel.barcode!!).addOnSuccessListener { exists ->
                if (exists) {
                    Toast.makeText(activity, "A product with this barcode already exists!", Toast.LENGTH_SHORT).show()
                }

                // The product is valid if it does not yet exist
                cont(!exists)
            }
        }
    }
}
