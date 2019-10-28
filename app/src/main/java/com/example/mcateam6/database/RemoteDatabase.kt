package com.example.mcateam6.database

import android.util.Log
import com.example.mcateam6.datatypes.Attribute
import com.example.mcateam6.datatypes.Product
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
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
        var ingredients: List<DocumentReference> = emptyList(),
        var attributes: Map<String, String> = emptyMap()
    ) {

        /**
         * convert to object of Product class without ingredients. To get
         * @see Product
         * @return object of class Product
         */
        fun toProduct(): Product {
            val aNew = convertAttributes()
            /*return convertIngredients().continueWith { task: Task<List<Product>> ->
                Product(
                    id, name_english, name_korean, barcode, description,
                    task.result!!, aNew, null
                )
            }*/
            return Product(
                id, name_english, name_korean, barcode, description, aNew
            )
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
        /*private fun convertIngredients(): Task<List<Product>> {
            val db = RemoteDatabase()
            val list = Tasks.whenAllSuccess<Product>(ingredients.map { s ->
                db.getProductById(s)
                    .continueWith { task: Task<FirebaseProduct> -> task.result!!.toProduct() }
            })
            return list
        }*/

        fun getIngredientProducts(): Task<List<FirebaseProduct>> {
            return Tasks.whenAllSuccess(ingredients.map { documentReference ->
                documentReference.get().continueWith { task: Task<DocumentSnapshot> ->
                    task.result!!.toObject(FirebaseProduct::class.java)
                }
            })
        }
    }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private var user: FirebaseUser?

    private val small: Int = 1000

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

    private val logTag = "FirebaseDatabase"

    /*fun upload(p: Product): Task<DocumentReference> {
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
                Log.d(logTag, "Upload successful")
            }
            .addOnFailureListener { e ->
                Log.e(logTag, "Error uploading document", e)
            }
    }*/

    /**
     * Upload multiple products. Make sure that all ingredients are uploaded
     * before uploading the product
     * @param products one or more products
     * @return list of ids
     */
    fun upload(vararg products: Product): Task<List<String>> {
        val batch = db.batch()
        val id: MutableList<String> = emptyList<String>().toMutableList()

        for (p in products) {

            if (p.id == "" || p.document == null) {
                p.createDocument(this)
            }

            val docRef = p.document
            id.add(p.id)

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
            productMap["ingredients"] =
                p.ingredients.map { product -> db.document("/products/${product.id}") }
            productMap["attributes"] = attributes

            batch.set(docRef!!, productMap)
        }

        return batch.commit().continueWith { id.toList() }
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
                Log.d(logTag, "Upload successful")
            }
            .addOnFailureListener { e ->
                Log.e(logTag, "Error uploading document", e)
            }
    }


    /**
     * Returns an object of the class DocumentSnapshot with the id from the database.
     * @param id id for the product as a string (id contains letters)
     * @return task of the type DocumentSnapshot
     * @see DocumentSnapshot
     * @see Task
     */
    @Deprecated(
        "returns raw DocumentSnapshot. getProductById should be used to work with" +
                " correct objects"
    )
    fun getByIdRaw(id: String): Task<DocumentSnapshot> {
        val docRef = prodColl.document(id)
        return docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(logTag, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(logTag, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(logTag, "get failed with ", exception)
            }
    }

    /**
     * Returns a product for a specific id.
     * @param id id of the product as a string (id contains letters)
     * @return task of the type FirebaseProduct
     * @see FirebaseProduct
     * @see Task
     */
    fun getProductById(id: String): Task<FirebaseProduct> {
        val docRef = prodColl.document(id)
        return docRef.get()
            .continueWith { task: Task<DocumentSnapshot> -> task.result!!.toObject(FirebaseProduct::class.java) }
    }

    /**
     * Returns a product for a specific english name. The name has to match the name of the product
     * in the database exactly, otherwise no product will be returned. If more than one product
     * exists with the same name (should be avoided), only the first product will be returned.
     * @param name english name of the product as a string
     * @return task of the type FirebaseProduct
     * @see FirebaseProduct
     * @see Task
     */
    fun getProductByEnglishName(name: String): Task<FirebaseProduct> {
        val query = prodColl.whereEqualTo("name_english", name).limit(1)
        return query.get()
            .continueWith { task: Task<QuerySnapshot> -> task.result!!.toObjects(FirebaseProduct::class.java)[0] }
    }

    /**
     * Returns a product for a specific korean name. The name has to match the name of the product
     * in the database exactly, otherwise no product will be returned. If more than one product
     * exists with the same name (should be avoided), only the first product will be returned.
     * @param name korean name of the product as a string
     * @return task of the type FirebaseProduct
     * @see FirebaseProduct
     * @see Task
     */
    fun getProductByKoreanName(name: String): Task<FirebaseProduct> {
        val query = prodColl.whereEqualTo("name_korean", name).limit(1)
        return query.get()
            .continueWith { task: Task<QuerySnapshot> -> task.result!!.toObjects(FirebaseProduct::class.java)[0] }
    }

    /**
     * Returns a product for a specific barcode. If more than one product exists with the same
     * barcode (should be avoided), only the first product will be returned.
     * @param barcode barcode of the product as a string
     * @return task of the type FirebaseProduct
     * @see FirebaseProduct
     * @see Task
     */
    fun getProductByBarcode(barcode: String): Task<FirebaseProduct> {
        val query = prodColl.whereEqualTo("barcode", barcode).limit(1)
        return query.get()
            .continueWith { task: Task<QuerySnapshot> -> task.result!!.toObjects(FirebaseProduct::class.java)[0] }
    }

    /**
     * Upload an image for a specific product. Only one image per product can be uploaded,
     * if an image already exists for a product, it will be replaced by this method.
     * @param p product which the image refers to
     * @param image file object of the image
     * @return upload task
     */
    fun uploadImage(p: Product, image: File): UploadTask {
        assert(image.extension == "png")
        assert(p.id != "")
        val storageRef = storage.reference
        val pRef = storageRef.child("${p.id}.png")
        val pIRef = storageRef.child("images/" + pRef.path)

        val stream = FileInputStream(image)

        return pIRef.putStream(stream)
    }

    /**
     * Upload a image for a specific product. Only one image per product can be uploaded, if
     * an image already exists for a product, it will be replaced by this method.
     * @param p product which the image refers to
     * @param imageStream stream of the image
     * @return upload task
     */
    fun uploadImage(p: Product, imageStream: InputStream): UploadTask {
        assert(p.id != "")
        val storageRef = storage.reference
        val pRef = storageRef.child("${p.id}.png")
        val pIRef = storageRef.child("images/" + pRef.path)

        return pIRef.putStream(imageStream)
    }

    /**
     * Download the original, uncompressed image for a specific product. Only one image
     * per product is available. Please use downloadImageSmall for a smaller version of the
     * images, as uncompressed images might take long to download.
     * @param p the product, which image will be downloaded
     * @return A task of type ByteArray. Task might take some time until completion.
     */
    fun downloadImage(p: Product): Task<ByteArray> {
        val storageRef = storage.reference
        val pathRef = storageRef.child("images/${p.id}.png")

        return pathRef.getBytes(1024 * 1024 * 10)
    }

    /**
     * Download a compressed image in the size 1024*1024 (or smaller, according to ratio)
     * for a specific product. Only one image per product is available. Small images are available
     * about 1 min after upload as the compression extension has to run first.
     * @param p the product, which image will be downloaded
     * @return A Task of type ByteArray. Task might take some time until completion.
     * @see Task
     */
    fun downloadImageSmall(p: Product): Task<ByteArray> {
        val storageRef = storage.reference
        val pathRef = storageRef.child("images/small/${p.id}_${small}x${small}.png")

        return pathRef.getBytes(1024 * 1024 * 3)
    }

    fun getProductsFromReferences(reference: List<DocumentReference>): Task<List<Product>> {
        val task = reference.map { documentReference ->
            documentReference.get().continueWith { task: Task<DocumentSnapshot> ->
                task.result!!.toObject(FirebaseProduct::class.java)!!.toProduct()
            }
        }
        return Tasks.whenAllSuccess<Product>(task)
    }

    //TODO search
}