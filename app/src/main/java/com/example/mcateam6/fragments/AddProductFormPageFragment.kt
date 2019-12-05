package com.example.mcateam6.fragments

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.mcateam6.activities.AddProductFormPage
import com.example.mcateam6.viewmodels.PagedFormModel
import com.example.mcateam6.viewmodels.ProductListViewModel
import com.example.mcateam6.viewmodels.ProductViewModel

abstract class AddProductFormPageFragment : Fragment() {
    val pagedFormModel: PagedFormModel by lazy { ViewModelProviders.of(activity!!)[PagedFormModel::class.java] }
    val productModel: ProductViewModel by lazy { ViewModelProviders.of(activity!!)[ProductViewModel::class.java] }
    val productListModel: ProductListViewModel by lazy { ViewModelProviders.of(activity!!)[ProductListViewModel::class.java] }

    abstract val formPage: AddProductFormPage

    override fun onStart() {
        super.onStart()

        pagedFormModel.setCurrentPage(formPage)
        pagedFormModel.currentFragment = this
    }
}

abstract class AddProductFormPageAsyncFragment : AddProductFormPageFragment() {
    abstract fun asyncValidation(cont: (Boolean) -> Unit)
}