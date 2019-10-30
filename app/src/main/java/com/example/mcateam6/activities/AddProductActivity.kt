package com.example.mcateam6.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.example.mcateam6.R
import com.example.mcateam6.viewmodels.ProductViewModel

class AddProductActivity : AppCompatActivity() {

    lateinit var productModel: ProductViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        productModel = ViewModelProviders.of(this)[ProductViewModel::class.java]
    }
}
