package com.example.mcateam6.database

import android.util.Log
import com.example.mcateam6.datatypes.Attribute
import com.example.mcateam6.datatypes.Product
import com.google.firebase.firestore.FirebaseFirestore

class RemoteDatabase {


    fun upload(p: Product): Boolean {
        val db = FirebaseFirestore.getInstance();
        val productMap: HashMap<String, Any> = HashMap()
        productMap["name_english"] = p.englishName
        productMap["name_korean"] = p.koreanName
        productMap["barcode"] = p.barcode.orEmpty()
        productMap["description"] = p.description
        productMap["ingredients"] = p.ingredients
        productMap["attributes"] = p.attributes

        return db.collection("products")
            .add(productMap)
            .addOnSuccessListener { documentReference ->
                Log.d(documentReference.toString(), "Successful")
            }
            .addOnFailureListener { e ->
                Log.w("Error", "Error adding document", e)
            }
            .isSuccessful
    }

    fun upload(englishName: String,  koreanName: String, barcode: String?, description: String, ingredients: List<Product>, attributes: Map<Attribute, Boolean>): Boolean {
        val db = FirebaseFirestore.getInstance();
        val productMap: HashMap<String, Any> = HashMap()
        productMap["name_english"] = englishName
        productMap["name_korean"] = koreanName
        productMap["barcode"] = barcode.orEmpty()
        productMap["description"] = description
        productMap["ingredients"] = ingredients
        productMap["attributes"] = attributes

        return db.collection("products")
            .add(productMap)
            .addOnSuccessListener { documentReference ->
                Log.d(documentReference.toString(), "Successful")
            }
            .addOnFailureListener { e ->
                Log.w("Error", "Error adding document", e)
            }
            .isSuccessful
    }


}