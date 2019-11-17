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

    private var enNameShowError = false
    private var krNameShowError = false

    private var enNameValid = false
    private var krNameValid = false

    private lateinit var enNameEdit: EditText
    private lateinit var krNameEdit: EditText
    private lateinit var barcodeEdit: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_product_general_information, container, false)

        findViews(v)

        loadEditTextContents()

        initShowError()

        initValidation()

        updateValidationModel()

        addTextChangedListeners()

        return v
    }

    private fun initShowError() {
        enNameShowError = productModel.englishName.isNotBlank()
        krNameShowError = productModel.koreanName.isNotBlank()
    }

    private fun initValidation() {
        enNameValid = productModel.englishName.isNotBlank()
        krNameValid = productModel.koreanName.isNotBlank()
    }

    private fun loadEditTextContents() {
        enNameEdit.setText(productModel.englishName)
        krNameEdit.setText(productModel.koreanName)
        barcodeEdit.setText(productModel.barcode)
    }

    private fun findViews(v: View) {
        enNameEdit = v.findViewById(R.id.en_name_edit)
        krNameEdit = v.findViewById(R.id.kr_name_edit)
        barcodeEdit = v.findViewById(R.id.barcode_edit)
    }

    private fun addTextChangedListeners() {
        enNameEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                enNameShowError = true

                enNameValid = enNameEdit.text.toString().isNotBlank()
                enNameEdit.error = if (enNameValid) null else getString(R.string.error_english_name_blank)
                updateValidationModel()

                productModel.englishName = enNameEdit.text.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        krNameEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                krNameShowError = true

                krNameValid = krNameEdit.text.toString().isNotBlank()
                krNameEdit.error = if (krNameValid) null else getString(R.string.error_korean_name_blank)
                updateValidationModel()

                productModel.koreanName = krNameEdit.text.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        barcodeEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                productModel.barcode = barcodeEdit.text.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updateValidationModel() {
        pagedFormModel.setIsValid(
            formPage,
            enNameValid && krNameValid
        )
    }
}
