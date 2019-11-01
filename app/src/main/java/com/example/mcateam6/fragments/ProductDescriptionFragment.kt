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
import kotlinx.android.synthetic.main.fragment_product_description.*


class ProductDescriptionFragment : AddProductFormPageFragment() {

    override val formPage = AddProductFormPage.DESCRIPTION

    private var lastDescriptionCursorPosition: Int = 0
    private var description: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pagedFormModel.setValid(formPage)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_product_description, container, false)

        val descriptionEdit = v.findViewById<EditText>(R.id.description_edit)

        descriptionEdit.setText(productModel.description)

        addFormListener(descriptionEdit)

        return v
    }

    private fun addFormListener(editText: EditText): Unit =
        editText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    lastDescriptionCursorPosition = editText.selectionStart;
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
