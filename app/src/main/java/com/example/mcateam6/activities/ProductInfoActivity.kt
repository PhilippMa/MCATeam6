package com.example.mcateam6.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mcateam6.R
import com.example.mcateam6.adapters.SearchItemAdapter
import com.example.mcateam6.database.RemoteDatabase
import kotlinx.android.synthetic.main.activity_product_info.*

class ProductInfoActivity : AppCompatActivity() {
    val db = RemoteDatabase()

    var itemList: MutableList<RemoteDatabase.FirebaseProduct>? = mutableListOf()
    lateinit var itemAdapter: SearchItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_info)

        itemAdapter = SearchItemAdapter(this, itemList)
        recycler_ingredients.adapter = itemAdapter
        recycler_ingredients.layoutManager = GridLayoutManager(this, 1)

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
                if (attr?.get("VEGAN") == "true") {
                    image_vegan.setImageDrawable(getDrawable(android.R.drawable.checkbox_on_background))
                } else {
                    image_vegan.setImageDrawable(getDrawable(android.R.drawable.checkbox_off_background))
                }
                if (attr?.get("VEGETARIAN") == "true") {
                    image_vegetarian.setImageDrawable(getDrawable(android.R.drawable.checkbox_on_background))
                } else {
                    image_vegetarian.setImageDrawable(getDrawable(android.R.drawable.checkbox_off_background))
                }

                val imageTask = db.downloadImageSmall(id)
                imageTask.addOnCompleteListener{ it2 ->
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

                val ingredientTask = product?.getIngredientProducts()
                ingredientTask?.addOnCompleteListener { it3 ->
                    if (it3.isSuccessful) {
                        itemAdapter.updateWholeData(it3.result)
                    } else {
                        val toast = Toast.makeText(this, "Failed to load ingredients", Toast.LENGTH_SHORT)
                        toast.show()
                    }
                }
            } else {
                val toast = Toast.makeText(this, "Failed to load product", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }
}
