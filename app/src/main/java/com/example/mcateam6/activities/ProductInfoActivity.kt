package com.example.mcateam6.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
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

        back_button.setOnClickListener { finish() }

        db.signIn()

        itemAdapter = SearchItemAdapter(this, itemList)
        recycler_ingredients.adapter = itemAdapter
        recycler_ingredients.layoutManager = GridLayoutManager(this, 1)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val intent = this.intent
        val id = intent.extras?.get("id").toString()

        val task = db.getProductById(id)
        task.addOnCompleteListener{
            if (it.isSuccessful) {
                val product = it.result
                text_english_name.text = product?.name_english
                text_korean_name.text = product?.name_korean
                text_brand.text = product?.brand
                text_description.text = product?.description

                if (product?.brand != "") text_brand.visibility = View.VISIBLE
                if (product?.description != "") {
                    headline_description.visibility = View.VISIBLE
                    text_description.visibility = View.VISIBLE
                }

                val attr = product?.attributes
                val vegan = attr?.get("VEGAN")
                val vegetarian = attr?.get("VEGETARIAN")
                val checkboxOn = getDrawable(android.R.drawable.checkbox_on_background)
                val checkboxOff = getDrawable(android.R.drawable.checkbox_off_background)

                if (vegan == "true") {
                    text_vegan.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, checkboxOn, null)
                } else {
                    text_vegan.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, checkboxOff, null)
                }
                if (vegetarian == "true") {
                    text_vegetarian.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, checkboxOn, null)
                } else {
                    text_vegetarian.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, checkboxOff, null)
                }

                val dietPref = sharedPreferences.getString("diet_pref", "")

                if ((dietPref == "Vegan" && vegan != "true") || (dietPref == "Vegetarian" && vegetarian != "true")) {
                    text_suitable.text = getString(R.string.unsuitable_product)
                    text_suitable.setTextColor(Color.parseColor("#cc0000"))
                } else {
                    text_suitable.text = getString(R.string.suitable_product)
                    text_suitable.setTextColor(Color.parseColor("#669900"))
                }

                val imageTask = db.downloadImage(id)
                imageTask.addOnCompleteListener{ it2 ->
                    if (it2.isSuccessful) {
                        val byteArray = it2.result
                        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
                        view_image.setImageBitmap(Bitmap.createBitmap(bmp))
                        image_card.visibility = View.VISIBLE
                    }
                }

                val ingredientTask = product?.getIngredientProducts()
                ingredientTask?.addOnCompleteListener { it3 ->
                    if (it3.isSuccessful) {
                        itemAdapter.updateWholeData(it3.result)
                        if (!it3.result.isNullOrEmpty()) {
                            headline_ingredients.visibility = View.VISIBLE
                            recycler_ingredients.visibility = View.VISIBLE
                        }
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
