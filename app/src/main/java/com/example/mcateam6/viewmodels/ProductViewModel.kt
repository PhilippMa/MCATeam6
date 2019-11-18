package com.example.mcateam6.viewmodels

import androidx.lifecycle.ViewModel
import com.example.mcateam6.datatypes.Attribute
import com.example.mcateam6.datatypes.Product
import java.util.*
import kotlin.collections.ArrayList

class ProductViewModel : ViewModel() {
    var id: String = ""
    var englishName: String = ""
    var koreanName: String = ""
    var brand: String = ""
    var barcode: String? = null
    var description: String = ""
    var ingredients: ArrayList<Product> = ArrayList()
    var attributes: EnumMap<Attribute, Boolean> = EnumMap(Attribute::class.java)
}