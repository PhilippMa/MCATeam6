package com.example.mcateam6.viewmodels

import androidx.lifecycle.ViewModel
import com.example.mcateam6.database.RemoteDatabase

class ProductListViewModel : ViewModel() {
    var productList: ArrayList<RemoteDatabase.FirebaseProduct> = ArrayList()
}