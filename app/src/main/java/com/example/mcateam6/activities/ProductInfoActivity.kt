package com.example.mcateam6.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mcateam6.R
import com.example.mcateam6.database.RemoteDatabase
import kotlinx.android.synthetic.main.activity_product_info.*

class ProductInfoActivity : AppCompatActivity() {
    val db = RemoteDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_info)

        val intent = this.intent
        val id = intent.extras?.get("id").toString()

        val task = db.getProductById(id)
        task.addOnCompleteListener{
            if (it.isSuccessful) {
                val product = it.result
                text_english_name.text = product?.name_english
                text_korean_name.text = product?.name_korean
                text_description.text = product?.description

                val attr = product?.attributes
                text_vegan.text = "Vegan: ${attr?.get("VEGAN")}"
                text_vegetarian.text = "Vegetarian: ${attr?.get("VEGETARIAN")}"

                val imageTask = db.downloadImageSmall(id)
                imageTask.addOnCompleteListener{it2 ->
                    if (it2.isSuccessful) {
                        val byteArray = it2.result
                        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
                        view_image.setImageBitmap(Bitmap.createScaledBitmap(
                            bmp, view_image.width, view_image.height, false))
                    } else {
                        val toast = Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }

//                recycler_ingredients.
            } else {
                val toast = Toast.makeText(this, "Failed to load product", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }
}
