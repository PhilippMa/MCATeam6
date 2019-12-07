package com.example.mcateam6.fragments


import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.core.view.children
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mcateam6.R
import com.example.mcateam6.activities.AddProductFormPage
import com.example.mcateam6.adapters.FilteredProductAdapter
import com.example.mcateam6.adapters.FilteredProductAdapterListener
import com.example.mcateam6.datatypes.Attribute
import com.example.mcateam6.datatypes.Product
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*


class ProductIngredientsFragment : AddProductFormPageFragment(), FilteredProductAdapterListener {

    override val formPage = AddProductFormPage.INGREDIENTS

    private val notVeganIngr = ArrayList<String>()
    private val notVegetarianIngr = ArrayList<String>()

    private lateinit var ingredientsChips: ChipGroup
    private lateinit var vegChips: ChipGroup
    private lateinit var noneChip: Chip
    private lateinit var vegetarianChip: Chip
    private lateinit var veganChip: Chip
    private lateinit var vegetarianDisabledText: TextView
    private lateinit var veganDisabledText: TextView

    private lateinit var collapsedCard: CardView
    private lateinit var expandedCard: CardView
    private lateinit var expandCardText: TextView
    private lateinit var collapseCardImage: ImageView

    private var collapsedHeight = 0
    private var collapsedWidth = 0

    private lateinit var productAdapter: FilteredProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pagedFormModel.setValid(formPage)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_product_ingredients, container, false)

        findViews(v)

        setVegChipsCheckedChangeListener()

        loadIngredientsFromModel()

        updateDietaryConstraints()

        initSearch(v)

        initCardAnimation()

        return v
    }

    private fun initCardAnimation() {
        collapseCardImage.setOnClickListener {
            collapsedCard.visibility = View.VISIBLE
            expandedCard.visibility = View.INVISIBLE
            slideView(
                collapsedCard,
                collapsedHeight,
                collapsedWidth
            ) {
                expandCardText.visibility = View.VISIBLE
                collapsedCard.isEnabled = true
            }
        }

        collapsedCard.setOnClickListener {
            collapsedWidth = collapsedCard.width
            collapsedHeight = collapsedCard.height

            collapsedCard.isEnabled = false
            expandCardText.visibility = View.INVISIBLE
            slideView(
                collapsedCard,
                expandedCard.height,
                expandedCard.width
            ) {
                expandedCard.visibility = View.VISIBLE
                collapsedCard.visibility = View.INVISIBLE
            }
        }
    }

    private fun initSearch(v: View) {
        productAdapter = FilteredProductAdapter(activity!!, productListModel.productList, this)

        val recyclerView: RecyclerView = v.findViewById(R.id.product_recycler)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity!!.applicationContext)
            itemAnimator = DefaultItemAnimator()
            adapter = productAdapter
        }

        val searchManager = activity!!.getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView: SearchView = v.findViewById(R.id.ingredient_search)
        searchView.setSearchableInfo(
            searchManager.getSearchableInfo(activity!!.componentName)
        )
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.isSubmitButtonEnabled = false

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // filter recycler view when query submitted
                productAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                // filter recycler view when text is changed
                productAdapter.filter.filter(query)
                return false
            }
        })
    }

    private fun slideView(
        view: View,
        heightEnd: Int,
        widthEnd: Int,
        doOnEnd: () -> Unit = {}
    ) {
        val slideAnimator = ValueAnimator
            .ofFloat(0f, 1f)
            .setDuration(500)

        val heightStart = view.height
        val widthStart = view.width
        val heightDiff = heightEnd - heightStart
        val widthDiff = widthEnd - widthStart

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */

        slideAnimator.addUpdateListener { anim ->
            val ratio = anim.animatedValue as Float
            view.layoutParams.height = heightStart + (heightDiff * ratio).toInt()
            view.layoutParams.width = widthStart + (widthDiff * ratio).toInt()
            view.requestLayout()
        }

        /*  We use an animationSet to play the animation  */

        AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            play(slideAnimator)
            start()
        }.doOnEnd {
            doOnEnd()
        }
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
            productModel.attributes[Attribute.VEGETARIAN] != false -> vegetarianChip.isChecked =
                true
            else -> noneChip.isChecked = true
        }
    }

    private fun setVegChipsCheckedChangeListener() {
        vegChips.setOnCheckedChangeListener { group, checkedId ->
            onCheckedChange(group, checkedId)
        }
    }

    private fun findViews(v: View) {
        ingredientsChips = v.findViewById(R.id.ingredients_chips)
        vegChips = v.findViewById(R.id.veg_chips)
        noneChip = v.findViewById(R.id.none_chip)
        vegetarianChip = v.findViewById(R.id.vegetarian_chip)
        veganChip = v.findViewById(R.id.vegan_chip)
        vegetarianDisabledText = v.findViewById(R.id.vegetarian_disabled_text)
        veganDisabledText = v.findViewById(R.id.vegan_disabled_text)

        collapsedCard = v.findViewById(R.id.collapsed_search_card)
        expandedCard = v.findViewById(R.id.expanded_search_card)
        expandCardText = v.findViewById(R.id.expand_card_text)
        collapseCardImage = v.findViewById(R.id.collapse_card_image)
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

    override fun onProductSelected(product: Product) {
        if (productModel.ingredients.any {
                it.id == product.id
            }) return

        productModel.ingredients.add(product)
        if (product.attributes[Attribute.VEGAN] != true)
            notVeganIngr.add(product.englishName)
        if (product.attributes[Attribute.VEGETARIAN] != true)
            notVegetarianIngr.add(product.englishName)

        createChip(product, ingredientsChips)

        updateDietaryConstraints()
    }
}
