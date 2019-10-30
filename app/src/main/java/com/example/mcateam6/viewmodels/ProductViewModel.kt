package com.example.mcateam6.viewmodels

import androidx.lifecycle.ViewModel

class ProductViewModel : ViewModel() {
    var id: String = ""
    var englishName: String = ""
    var koreanName: String = ""
    var barcode: String? = null
    var description: String = ""
}