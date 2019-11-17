package com.example.mcateam6.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.example.mcateam6.R
import com.example.mcateam6.database.RemoteDatabase
import com.example.mcateam6.datatypes.Product
import com.example.mcateam6.fragments.AddProductFormPageAsyncFragment
import com.example.mcateam6.fragments.ProductDescriptionFragmentDirections
import com.example.mcateam6.fragments.ProductGeneralInformationFragmentDirections
import com.example.mcateam6.fragments.ProductIngredientsFragmentDirections
import com.example.mcateam6.viewmodels.PagedFormModel
import com.example.mcateam6.viewmodels.ProductViewModel
import com.github.razir.progressbutton.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_add_product.*

enum class AddProductFormPage {
    GENERAL_INFORMATION {
        override val hasNext: Boolean = true

        override fun nextNavAction(): NavDirections? {
            return ProductGeneralInformationFragmentDirections.actionProductGeneralInformationFragmentToProductDescriptionFragment()
        }
    },
    DESCRIPTION {
        override val hasNext: Boolean = true
        override val hasPrevious: Boolean = true

        override fun nextNavAction(): NavDirections? {
            return ProductDescriptionFragmentDirections.actionProductDescriptionFragmentToProductIngredientsFragment()
        }

        override fun previousNavAction(): NavDirections? {
            return ProductDescriptionFragmentDirections.actionProductDescriptionFragmentToProductGeneralInformationFragment()
        }
    },
    INGREDIENTS {
        override val hasPrevious: Boolean = true

        override fun previousNavAction(): NavDirections? {
            return ProductIngredientsFragmentDirections.actionProductIngredientsFragmentToProductDescriptionFragment()
        }
    };

    open val hasNext: Boolean = false
    open val hasPrevious: Boolean = false
    open fun nextNavAction(): NavDirections? {
        return null
    }

    open fun previousNavAction(): NavDirections? {
        return null
    }
}

class AddProductActivity : AppCompatActivity() {

    lateinit var productModel: ProductViewModel
    lateinit var pagedFormModel: PagedFormModel

    private var nextButtonEnabled = true

    private val navController: NavController by lazy { findNavController(R.id.add_product_nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        initViewModels()

        previous_button.setOnClickListener {
            navigatePrevious()
        }

        next_button.setOnClickListener {
            if (!nextButtonEnabled) return@setOnClickListener

            val currentFragment = pagedFormModel.currentFragment!!

            if (currentFragment is AddProductFormPageAsyncFragment) {
                next_button.showProgress {
                    progressColor = Color.WHITE
                    buttonText = ""
                    gravity = DrawableButton.GRAVITY_CENTER
                }

                nextButtonEnabled = false

                currentFragment.asyncValidation { valid ->
                    nextButtonEnabled = true
                    next_button.hideProgress(R.string.next)

                    if (valid) navigateNext()
                }
            } else {
                navigateNext()
            }
        }

        bindProgressButton(next_button)
        next_button.attachTextChangeAnimator()

        create_button.setOnClickListener {
            val db = RemoteDatabase()

            db.signIn().addOnSuccessListener {
                uploadProduct(db).addOnSuccessListener {
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Unable to upload product to database.\n Please try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        close_button.setOnClickListener { finish() }

        setFullScreen()
    }

    private fun navigateNext() {
        navController.navigate(pagedFormModel.getCurrentPage().nextNavAction()!!)
    }

    private fun navigatePrevious() {
        navController.navigate(pagedFormModel.getCurrentPage().previousNavAction()!!)
    }

    private fun uploadProduct(db: RemoteDatabase): Task<List<String>> {
        return db.upload(
            Product(
                productModel.brand,
                productModel.englishName,
                productModel.koreanName,
                if (!productModel.barcode.isNullOrBlank()) productModel.barcode else null,
                productModel.description,
                productModel.ingredients,
                productModel.attributes
            )
        )
    }

    private fun initViewModels() {
        productModel = ViewModelProviders.of(this)[ProductViewModel::class.java]
        pagedFormModel = ViewModelProviders.of(this)[PagedFormModel::class.java]

        pagedFormModel.currentPage.observe(this, Observer { page ->
            previous_button.visibility = if (page.hasPrevious) View.VISIBLE else View.INVISIBLE
            next_button.visibility = if (page.hasNext) View.VISIBLE else View.INVISIBLE
            next_button.isEnabled = pagedFormModel.isValid(page)
            create_button.visibility =
                if (page == AddProductFormPage.values().last()) View.VISIBLE else View.INVISIBLE
        })

        pagedFormModel.valid.observe(this, Observer { map ->
            create_button.isEnabled = map.values.all { it }
            next_button.isEnabled = map[pagedFormModel.getCurrentPage()] == true
        })
    }

    private fun setFullScreen() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.constraintLayout)
        ) { _, insets ->
            val container = findViewById<ConstraintLayout>(R.id.constraintLayout)
            val containerLayoutParams = container.layoutParams as ViewGroup.MarginLayoutParams
            containerLayoutParams.setMargins(0, insets.systemWindowInsetTop, 0, 0)
            container.layoutParams = containerLayoutParams
            insets.consumeSystemWindowInsets()
        }
    }
}
