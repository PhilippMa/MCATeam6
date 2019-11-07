package com.example.mcateam6.fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.children
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController

import com.example.mcateam6.R
import com.example.mcateam6.activities.AddProductFormPage
import com.example.mcateam6.database.RemoteDatabase
import com.example.mcateam6.datatypes.Attribute
import com.example.mcateam6.datatypes.Product
import com.example.mcateam6.viewmodels.ProductViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_product_ingredients.*
import java.util.ArrayList

class ProductIngredientsFragment : AddProductFormPageFragment() {

    override val formPage = AddProductFormPage.INGREDIENTS

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pagedFormModel.setValid(formPage)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_product_ingredients, container, false)

        val addButton: Button = v.findViewById(R.id.add_button)
        val ingredientsEdit: EditText = v.findViewById(R.id.ingredients_edit)
        val ingredientsChips: ChipGroup = v.findViewById(R.id.ingredients_chips)
        val vegChips: ChipGroup = v.findViewById(R.id.veg_chips)
        val noneChip: Chip = v.findViewById(R.id.none_chip)
        val vegetarianChip: Chip = v.findViewById(R.id.vegetarian_chip)
        val veganChip: Chip = v.findViewById(R.id.vegan_chip)

        addButton.setOnClickListener {
            val ingredientName = ingredientsEdit.text.toString().trim()

            if (ingredientName.isBlank()) return@setOnClickListener

            if (productModel.ingredients.any { it.englishName == ingredientName }) return@setOnClickListener

            RemoteDatabase().apply {
                signIn()
                getProductByEnglishName(ingredientName).addOnSuccessListener { fbProduct ->
                    ingredientsEdit.setText("".toCharArray(), 0, 0)
                    ingredientsEdit.setSelection(0)

                    val product = fbProduct.toProduct()

                    productModel.ingredients.add(product)

                    createChip(product, ingredientsChips)
                }.addOnFailureListener {
                    Toast.makeText(activity, "Unknown ingredient", Toast.LENGTH_SHORT).show()
                }
            }
        }

        vegChips.setOnCheckedChangeListener { group, checkedId ->
            onCheckedChange(group, checkedId)
        }

        productModel.ingredients.forEach { createChip(it, ingredientsChips) }

        when {
            productModel.attributes[Attribute.VEGAN] == true -> veganChip.isChecked = true
            productModel.attributes[Attribute.VEGETARIAN] == true -> vegetarianChip.isChecked = true
            productModel.attributes[Attribute.VEGETARIAN] == false -> noneChip.isChecked = true
            else -> veganChip.isChecked = true
        }

        return v
    }

    private fun onCheckedChange(
        group: ChipGroup,
        checkedId: Int
    ) {
        group.children.forEach { chip ->
            chip.isClickable = checkedId != chip.id
        }

        when (checkedId) {
            R.id.none_chip ->
                productModel.attributes.apply {
                    set(Attribute.VEGAN, false)
                    set(Attribute.VEGETARIAN, false)
                }
            R.id.vegetarian_chip ->
                productModel.attributes.apply {
                    set(Attribute.VEGAN, false)
                    set(Attribute.VEGETARIAN, true)
                }
            R.id.vegan_chip ->
                productModel.attributes.apply {
                    set(Attribute.VEGAN, true)
                    set(Attribute.VEGETARIAN, true)
                }
        }
    }

    private fun createChip(
        product: Product,
        chipGroup: ChipGroup
    ) {
        val chip = Chip(activity).apply {
            text = product.englishName
            isCloseIconVisible = true
            isClickable = true
            isCheckable = false
            animation = AnimationUtils.loadAnimation(activity, R.anim.fade_in)
            setOnCloseIconClickListener {
                chipGroup.removeView(this as View)
                productModel.ingredients.remove(product)
            }
        }

        chipGroup.addView(chip as View)
    }
}
