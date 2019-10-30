package com.example.mcateam6.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils

import com.example.mcateam6.R
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_product_ingredients.*
import java.util.ArrayList

class ProductIngredientsFragment : Fragment() {

    private val ingredientsList : ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        add_button.setOnClickListener {
            if (ingredients_edit.text.isNullOrBlank()) return@setOnClickListener

            ingredientsList.add(ingredients_edit.text.toString())
            val chip = Chip(activity)
            chip.text = ingredients_edit.text.toString()
            chip.isCloseIconVisible = true

            chip.isClickable = true
            chip.isCheckable = false

            val fadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_in)
            chip.animation = fadeInAnimation

            ingredients_chips.addView(chip as View)
            ingredients_edit.setText("".toCharArray(), 0, 0)
            ingredients_edit.setSelection(0)
            chip.setOnCloseIconClickListener {
                ingredients_chips.removeView(chip as View)
                ingredientsList.remove(chip.text.toString())
            }
        }

        return inflater.inflate(R.layout.fragment_product_ingredients, container, false)
    }


}
