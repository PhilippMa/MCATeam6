package com.example.mcateam6.fragments


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.mcateam6.R
import kotlinx.android.synthetic.main.fragment_product_description.*


class ProductDescriptionFragment : Fragment() {

    private var lastDescriptionCursorPosition: Int = 0
    private var description: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        description_edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                lastDescriptionCursorPosition = description_edit.selectionStart;
                description = description_edit.text.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                description_edit.removeTextChangedListener(this)

                if (description_edit.lineCount > 5) {
                    description_edit.setText(description)
                    description_edit.setSelection(lastDescriptionCursorPosition)
                }

                description_edit.addTextChangedListener(this)
            }
        })

        return inflater.inflate(R.layout.fragment_product_description, container, false)
    }


}
