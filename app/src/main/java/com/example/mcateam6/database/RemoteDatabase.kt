package com.example.mcateam6.database

import android.util.Log
import com.example.mcateam6.datatypes.Product
import com.google.firebase.firestore.FirebaseFirestore

class RemoteDatabase {

    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance();

    fun initialize(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun upload(db: FirebaseFirestore, p: Product): Boolean {
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


}