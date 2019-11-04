package com.example.mcateam6.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.mcateam6.activities.AddProductFormPage
import com.example.mcateam6.viewmodels.PagedFormModel
import com.example.mcateam6.viewmodels.ProductViewModel

abstract class AddProductFormPageFragment : Fragment() {
    val pagedFormModel: PagedFormModel by lazy { ViewModelProviders.of(activity!!)[PagedFormModel::class.java] }
    val productModel: ProductViewModel by lazy { ViewModelProviders.of(activity!!)[ProductViewModel::class.java] }

    abstract val formPage: AddProductFormPage

    override fun onAttach(context: Context) {
        super.onAttach(context)

        pagedFormModel.setCurrentPage(formPage)
        Log.i("FormPage", "attached: $formPage")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("FormPage", "created: $formPage")
    }
}