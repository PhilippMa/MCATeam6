package com.example.mcateam6.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mcateam6.activities.AddProductFormPage
import java.util.*

class PagedFormModel : ViewModel() {
    val valid = MutableLiveData<EnumMap<AddProductFormPage, Boolean>>(EnumMap(AddProductFormPage::class.java))

    fun setValid(page: AddProductFormPage) {
        valid.value?.set(page, true)
        valid.value = valid.value
    }

    fun setInvalid(page: AddProductFormPage) {
        valid.value?.set(page, false)
        valid.value = valid.value
    }

    fun setIsValid(page: AddProductFormPage, v: Boolean) {
        valid.value?.set(page, v)
        valid.value = valid.value
    }

    val currentPage = MutableLiveData<AddProductFormPage>(AddProductFormPage.values().first())

    fun setCurrentPage(page: AddProductFormPage) {
        currentPage.value = page
    }
}