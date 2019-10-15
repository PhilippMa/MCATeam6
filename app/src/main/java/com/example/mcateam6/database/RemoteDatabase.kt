package com.example.mcateam6.database

import android.util.Log
import com.example.mcateam6.datatypes.Attribute
import com.example.mcateam6.datatypes.Product
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class RemoteDatabase {

    data class FirebaseProduct(
        var name_english: String = "",
        var name_korean: String = "",
        var description: String = "",
        var barcode: String? = null,
        var ingredients: List<Product> = emptyList(),
        var attributes: Map<String, Boolean> = emptyMap()
    )

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun upload(p: Product): Task<DocumentReference> {
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
                Log.d("FirebaseDatabase", "Upload successful")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseDatabase", "Error uploading document", e)
            }
    }

    /**
     * Upload multiple products
     * @param products one or more products
     */
    fun upload(vararg products : Product): Task<Void> {
        val col = db.collection("products")
        val batch = db.batch()

        for (p in products) {

            // Map attribute map to a <String, String> map for compatibility with the firestore datatypes
            val attributes : HashMap<String, String> = HashMap()
            p.attributes.onEach { entry -> attributes[entry.key.toString()] = entry.value.toString() }

            val productMap: HashMap<String, Any> = HashMap()
            productMap["name_english"] = p.englishName
            productMap["name_korean"] = p.koreanName
            productMap["barcode"] = p.barcode.orEmpty()
            productMap["description"] = p.description
            //TODO add ingredients
            productMap["ingredients"] = listOf({})
            productMap["attributes"] = attributes

            val docRef = col.document()
            batch.set(docRef, productMap)
        }

        return batch.commit()
    }

    fun upload(
        englishName: String,
        koreanName: String,
        barcode: String?,
        description: String,
        ingredients: List<Product>,
        attributes: Map<Attribute, Boolean>
    ): Task<DocumentReference> {
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
                Log.d("FirebaseDatabase", "Upload successful")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseDatabase", "Error uploading document", e)
            }
    }

    /**
     * Returns an object of the class DocumentSnapshot? with the id from the database
     */
    fun getByID_Raw(id: String): Task<DocumentSnapshot> {
        val docRef = db.collection("products").document(id)
        return docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("FirebaseDatabase", "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d("FirebaseDatabase", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FirebaseDatabase", "get failed with ", exception)
            }
    }

    fun getProductById(id: String): Task<FirebaseProduct> {
        val docRef = db.collection("products").document(id)
        return docRef.get()
            .continueWith { task: Task<DocumentSnapshot> -> task.result!!.toObject(FirebaseProduct::class.java) }
    }


}