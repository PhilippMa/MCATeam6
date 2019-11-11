package com.example.mcateam6.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.mcateam6.R
import com.example.mcateam6.activities.AddProductFormPage


class ProductGeneralInformationFragment : AddProductFormPageFragment() {

    override val formPage = AddProductFormPage.GENERAL_INFORMATION

    lateinit var enNameEdit: EditText
    lateinit var krNameEdit: EditText
    lateinit var barcodeEdit: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pagedFormModel.setInvalid(formPage)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_product_general_information, container, false)

        enNameEdit = v.findViewById(R.id.en_name_edit)
        krNameEdit = v.findViewById(R.id.kr_name_edit)
        barcodeEdit = v.findViewById(R.id.barcode_edit)

        enNameEdit.setText(productModel.englishName)
        krNameEdit.setText(productModel.koreanName)
        barcodeEdit.setText(productModel.barcode)
        updateValidation()
        updateProductModel()

        addFormListener(enNameEdit)
        addFormListener(krNameEdit)
        addFormListener(barcodeEdit)

        return v
    }

    private fun updateProductModel() {
        productModel.englishName = enNameEdit.text.toString()
        productModel.koreanName = krNameEdit.text.toString()
        productModel.barcode = barcodeEdit.text.toString()
    }

    private fun addFormListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateValidation()
                updateProductModel()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updateValidation() {
        val enNameValid = enNameEdit.text.toString().isNotBlank()
        val krNameValid = krNameEdit.text.toString().isNotBlank()

        enNameEdit.error = if (enNameValid) null else getString(R.string.error_english_name_blank)
        krNameEdit.error = if (krNameValid) null else getString(R.string.error_korean_name_blank)

        val pageValid = enNameValid && krNameValid

        pagedFormModel.setIsValid(
            formPage,
            pageValid
        )
    }
}
