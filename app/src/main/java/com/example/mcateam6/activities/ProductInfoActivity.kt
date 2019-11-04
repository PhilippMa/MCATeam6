package com.example.mcateam6.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mcateam6.R
import kotlinx.android.synthetic.main.activity_product_info.*

class ProductInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_info)

        val intent = this.intent
        val englishName = intent.extras?.get("englishName")
        val koreanName = intent.extras?.get("koreanName")
        val description = intent.extras?.get("description")
//        Leaving out for now
//        val attributes = intent.extras?.get("attributes")

        text_english_name.text = englishName.toString()
        text_korean_name.text = koreanName.toString()
        text_description.text = description.toString()
    }
}
