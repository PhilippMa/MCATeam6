package com.example.mcateam6.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import com.example.mcateam6.R
import com.example.mcateam6.activities.AddProductFormPage
import com.example.mcateam6.database.RemoteDatabase
import com.example.mcateam6.datatypes.Attribute
import com.example.mcateam6.datatypes.Product
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*

class ProductIngredientsFragment : AddProductFormPageFragment() {

    override val formPage = AddProductFormPage.INGREDIENTS

    private val notVeganIngr = ArrayList<String>()
    private val notVegetarianIngr = ArrayList<String>()

    private lateinit var addButton: Button
    private lateinit var ingredientsEdit: EditText
    private lateinit var ingredientsChips: ChipGroup
    private lateinit var vegChips: ChipGroup
    private lateinit var noneChip: Chip
    private lateinit var vegetarianChip: Chip
    private lateinit var veganChip: Chip
    private lateinit var vegetarianDisabledText: TextView
    private lateinit var veganDisabledText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pagedFormModel.setValid(formPage)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_product_ingredients, container, false)

        findViews(v)

        setAddButtonClickListener()

        setVegChipsCheckedChangeListener()

        loadIngredientsFromModel()

        updateDietaryConstraints()

        return v
    }

    private fun loadIngredientsFromModel() {
        productModel.ingredients.forEach { product ->
            createChip(product, ingredientsChips)
            if (product.attributes[Attribute.VEGAN] != true)
                notVeganIngr.add(product.englishName)
            if (product.attributes[Attribute.VEGETARIAN] != true)
                notVegetarianIngr.add(product.englishName)
        }

        when {
            productModel.attributes[Attribute.VEGAN] != false -> veganChip.isChecked = true
            productModel.attributes[Attribute.VEGETARIAN] != false -> vegetarianChip.isChecked = true
            else -> noneChip.isChecked = true
        }
    }

    private fun setVegChipsCheckedChangeListener() {
        vegChips.setOnCheckedChangeListener { group, checkedId ->
            onCheckedChange(group, checkedId)
        }
    }

    private fun setAddButtonClickListener() {
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
                    if (product.attributes[Attribute.VEGAN] != true)
                        notVeganIngr.add(product.englishName)
                    if (product.attributes[Attribute.VEGETARIAN] != true)
                        notVegetarianIngr.add(product.englishName)

                    createChip(product, ingredientsChips)

                    updateDietaryConstraints()
                }.addOnFailureListener {
                    Toast.makeText(activity, "Unknown ingredient", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun findViews(v: View) {
        addButton = v.findViewById(R.id.add_button)
        ingredientsEdit = v.findViewById(R.id.ingredients_edit)
        ingredientsChips = v.findViewById(R.id.ingredients_chips)
        vegChips = v.findViewById(R.id.veg_chips)
        noneChip = v.findViewById(R.id.none_chip)
        vegetarianChip = v.findViewById(R.id.vegetarian_chip)
        veganChip = v.findViewById(R.id.vegan_chip)
        vegetarianDisabledText = v.findViewById(R.id.vegetarian_disabled_text)
        veganDisabledText = v.findViewById(R.id.vegan_disabled_text)
    }

    private fun updateDietaryConstraints() {

        veganChip.isEnabled = true
        vegetarianChip.isEnabled = true

        if (notVegetarianIngr.isNotEmpty()) {
            veganChip.isEnabled = false
            vegetarianChip.isEnabled = false
            noneChip.isChecked = true

            vegetarianDisabledText.text = resources.getQuantityString(
                R.plurals.info_attribute_disabled,
                notVegetarianIngr.size,
                getString(R.string.vegetarian),
                getString(R.string.vegetarian).toLowerCase(Locale.getDefault()),
                notVegetarianIngr.first(),
                notVegetarianIngr.size - 1
            )
            vegetarianDisabledText.visibility = View.VISIBLE
        } else {
            vegetarianDisabledText.visibility = View.GONE
        }

        if (notVeganIngr.isNotEmpty()) {
            veganChip.isEnabled = false
            if (veganChip.isChecked) vegetarianChip.isChecked = true

            veganDisabledText.text = resources.getQuantityString(
                R.plurals.info_attribute_disabled,
                notVeganIngr.size,
                getString(R.string.vegan),
                getString(R.string.vegan).toLowerCase(Locale.getDefault()),
                notVeganIngr.first(),
                notVeganIngr.size - 1
            )
            veganDisabledText.visibility = View.VISIBLE
        } else {
            veganDisabledText.visibility = View.GONE
        }
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
                notVeganIngr.remove(product.englishName)
                notVegetarianIngr.remove(product.englishName)
                updateDietaryConstraints()
            }
        }

        chipGroup.addView(chip as View)
    }
}
