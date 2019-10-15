package com.example.mcateam6.database

import android.util.Log
import com.example.mcateam6.datatypes.Attribute
import com.example.mcateam6.datatypes.Product
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap

class RemoteDatabase {

    data class FirebaseProduct(
        var name_english: String = "",
        var name_korean: String = "",
        var description: String = "",
        var barcode: String? = null,
        // TODO change datatype
        var ingredients: List<String> = emptyList(),
        var attributes: Map<String, String> = emptyMap()
    ) {

        /**
         * convert to object of Product class
         * @see Product
         * @return object of class Product
         */
        fun toProduct() : Product {
            val aNew = convertAttributes()
            val pNew = convertProducts()
            return Product(name_english, name_korean, barcode, description, pNew, aNew)
        }

        /**
         * convert the var attributes to a Map with keys of Enum Attribute
         * @see Attribute
         * @return map<Attribute, Boolean>
         */
        private fun convertAttributes() : Map<Attribute, Boolean> {
            val aNew : EnumMap<Attribute, Boolean> = EnumMap(Attribute::class.java)
            attributes.onEach { entry -> aNew[Attribute.valueOf(entry.key)] = entry.value.toBoolean() }
            return aNew
        }

        /**
         * convert list of Strings referencing other documents to a list of products
         * @see Product
         * @return list of products (list<Product>)
         */
        private fun convertProducts() : List<Product> {
            // TODO implement
            return emptyList()
        }
    }

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

            val ingredients: List<Product> = emptyList()

            val productMap: HashMap<String, Any> = HashMap()
            productMap["name_english"] = p.englishName
            productMap["name_korean"] = p.koreanName
            productMap["barcode"] = p.barcode.orEmpty()
            productMap["description"] = p.description
            //TODO add ingredients
            productMap["ingredients"] = ingredients
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