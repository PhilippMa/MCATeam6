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
import kotlinx.android.synthetic.main.fragment_product_general_information.*


class ProductGeneralInformationFragment : AddProductFormPageFragment() {

    override val formPage = AddProductFormPage.GENERAL_INFORMATION

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pagedFormModel.setInvalid(formPage)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_product_general_information, container, false)

        val enNameEdit: EditText = v.findViewById(R.id.en_name_edit)
        val krNameEdit: EditText = v.findViewById(R.id.kr_name_edit)
        val barcodeEdit: EditText = v.findViewById(R.id.barcode_edit)

        enNameEdit.setText(productModel.englishName)
        krNameEdit.setText(productModel.koreanName)
        barcodeEdit.setText(productModel.barcode)
        updateModels(enNameEdit, krNameEdit, barcodeEdit)

        addFormListener(enNameEdit)
        addFormListener(krNameEdit)
        addFormListener(barcodeEdit)

        return v
    }

    private fun updateModels(
        enNameEditText: EditText = en_name_edit,
        krNameEditText: EditText = kr_name_edit,
        barcodeEditText: EditText = barcode_edit
    ) {
        pagedFormModel.setIsValid(
            formPage,
            enNameEditText.text.toString().isNotBlank() && krNameEditText.text.toString().isNotBlank()
        )
        productModel.englishName = enNameEditText.text.toString()
        productModel.koreanName = krNameEditText.text.toString()
        productModel.barcode = barcodeEditText.text.toString()
    }

    private fun addFormListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateModels()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}
