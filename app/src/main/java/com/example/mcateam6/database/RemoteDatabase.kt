package com.example.mcateam6.database

import android.media.Image
import android.util.Log
import com.example.mcateam6.datatypes.Attribute
import com.example.mcateam6.datatypes.Product
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.HashMap

class RemoteDatabase {

    data class FirebaseProduct(
        var id: String = "",
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
        fun toProduct(): Product {
            val aNew = convertAttributes()
            val pNew = convertProducts()
            return Product(id, name_english, name_korean, barcode, description, pNew, aNew, null)
        }

        /**
         * convert the var attributes to a Map with keys of Enum Attribute
         * @see Attribute
         * @return map<Attribute, Boolean>
         */
        private fun convertAttributes(): Map<Attribute, Boolean> {
            val aNew: EnumMap<Attribute, Boolean> = EnumMap(Attribute::class.java)
            attributes.onEach { entry ->
                aNew[Attribute.valueOf(entry.key)] = entry.value.toBoolean()
            }
            return aNew
        }

        /**
         * convert list of Strings referencing other documents to a list of products
         * @see Product
         * @return list of products (list<Product>)
         */
        private fun convertProducts(): List<Product> {
            // TODO implement
            return emptyList()
        }
    }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private var user: FirebaseUser?

    private var SMALL: Int = 1000

    val prodColl: CollectionReference
        get() = db.collection("products")

    constructor(
        user: FirebaseUser?
    ) {
        this.user = user
    }

    constructor() {
        this.user = auth.currentUser
    }


    fun signIn(): Task<AuthResult> {
        return auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("RemoteDB", "signInAnonymously:success")
                    val user = auth.currentUser
                    this.user = user
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("RemoteDB", "signInAnonymously:failure", task.exception)
                    this.user = null
                }
            }
    }

    private val LOG_TAG = "FirebaseDatabase"

    fun upload(p: Product): Task<DocumentReference> {
        val productMap: HashMap<String, Any> = HashMap()
        productMap["name_english"] = p.englishName
        productMap["name_korean"] = p.koreanName
        productMap["barcode"] = p.barcode.orEmpty()
        productMap["description"] = p.description
        productMap["ingredients"] = p.ingredients
        productMap["attributes"] = p.attributes

        return prodColl
            .add(productMap)
            .addOnSuccessListener { documentReference ->
                Log.d(LOG_TAG, "Upload successful")
            }
            .addOnFailureListener { e ->
                Log.e(LOG_TAG, "Error uploading document", e)
            }
    }

    /**
     * Upload multiple products. Make sure that all ingredients are uploaded
     * before uploading the product
     * @param products one or more products
     */
    fun upload(vararg products: Product): Task<Void> {
        val batch = db.batch()

        for (p in products) {

            if (p.id == "" || p.document == null) {
                p.createDocument(this)
            }

            val docRef = p.document


            // Map attribute map to a <String, String> map for compatibility with the firestore datatypes
            val attributes: HashMap<String, String> = HashMap()
            p.attributes.onEach { entry ->
                attributes[entry.key.toString()] = entry.value.toString()
            }

            val productMap: HashMap<String, Any> = HashMap()

            productMap["name_english"] = p.englishName
            productMap["name_korean"] = p.koreanName
            productMap["barcode"] = p.barcode.orEmpty()
            productMap["description"] = p.description
            productMap["ingredients"] = p.ingredients.map { product -> db.document("/products/${product.id}") }
            productMap["attributes"] = attributes

            batch.set(docRef!!, productMap)
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

        return prodColl
            .add(productMap)
            .addOnSuccessListener { documentReference ->
                Log.d(LOG_TAG, "Upload successful")
            }
            .addOnFailureListener { e ->
                Log.e(LOG_TAG, "Error uploading document", e)
            }
    }

    /**
     * Returns an object of the class DocumentSnapshot? with the id from the database
     */
    fun getByID_Raw(id: String): Task<DocumentSnapshot> {
        val docRef = prodColl.document(id)
        return docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(LOG_TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(LOG_TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(LOG_TAG, "get failed with ", exception)
            }
    }

    fun getProductById(id: String): Task<FirebaseProduct> {
        val docRef = prodColl.document(id)
        return docRef.get()
            .continueWith { task: Task<DocumentSnapshot> -> task.result!!.toObject(FirebaseProduct::class.java) }
    }

    fun getProductByEnglishName(name: String): Task<FirebaseProduct> {
        val query = prodColl.whereEqualTo("name_english", name).limit(1)
        return query.get()
            .continueWith { task: Task<QuerySnapshot> -> task.result!!.toObjects(FirebaseProduct::class.java)[0] }
    }

    fun getProductByKoreanName(name: String): Task<FirebaseProduct> {
        val query = prodColl.whereEqualTo("name_korean", name).limit(1)
        return query.get()
            .continueWith { task: Task<QuerySnapshot> -> task.result!!.toObjects(FirebaseProduct::class.java)[0] }
    }

    fun getProductByBarcode(barcode: String): Task<FirebaseProduct> {
        val query = prodColl.whereEqualTo("barcode", barcode).limit(1)
        return query.get()
            .continueWith { task: Task<QuerySnapshot> -> task.result!!.toObjects(FirebaseProduct::class.java)[0] }
    }

    fun uploadImage(p: Product, image: File): UploadTask {
        assert(image.extension == "png")
        assert(p.id != "")
        val storageRef = storage.reference
        val pRef = storageRef.child("${p.id}.png")
        val pIRef = storageRef.child("images/" + pRef.path)

        val stream = FileInputStream(image)

        val uploadTask = pIRef.putStream(stream)
        return uploadTask
    }

    fun uploadImage(p: Product, imageStream: InputStream): UploadTask {
        assert(p.id != "")
        val storageRef = storage.reference
        val pRef = storageRef.child("${p.id}.png")
        val pIRef = storageRef.child("images/" + pRef.path)

        val uploadTask = pIRef.putStream(imageStream)
        return uploadTask
    }

    fun downloadImage(p: Product): Task<ByteArray> {
        val storageRef = storage.reference
        val pathRef = storageRef.child("images/${p.id}.png")

        return pathRef.getBytes(1024*1024*10)
    }

    fun downloadImage_Small(p: Product): Task<ByteArray> {
        val storageRef = storage.reference
        val pathRef = storageRef.child("images/small/${p.id}_${SMALL}x${SMALL}.png")

        return pathRef.getBytes(1024*1024*3)
    }
}