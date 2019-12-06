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

    /**
     * Use for download products from Firestore and convert them directly to this class. Contains
     * Firestore compatible datatypes for all attributes. Use methods to compare to datatypes used
     * in Product class.
     */
    data class FirebaseProduct(
        /**
         * Remote id of the product
         */
        var id: String = "",
        /**
         * Brand name of the product (empty if product is a raw ingredients (e.g. water, salt, ...)
         */
        var brand: String = "",
        /**
         * English name of the product
         */
        var name_english: String = "",
        /**
         * Korean name of the product
         */
        var name_korean: String = "",
        /**
         * Optional description of the product
         */
        var description: String = "",
        /**
         * Barcode of the product
         */
        var barcode: String? = null,
        /**
         * List of references to the ingredients. Use getIngredientProducts() to get the ingredients
         * as products
         * @see DocumentReference
         */
        var ingredients: List<DocumentReference?> = emptyList(),
        /**
         * String, String map of the attributes (vegan, vegetarian). Use convertAttributes() to get
         * a map of type Attribute, Boolean.
         */
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
                id, brand, name_english, name_korean, barcode, description, aNew
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
         * Returns all ingredients of the current product. List of products will be returned as task
         * as ingredients have to be downloaded first.
         * @return A Task of a list of FirebaseProduct objects.
         */
        fun getIngredientProducts(): Task<List<FirebaseProduct>> {
            return Tasks.whenAllSuccess(ingredients.map { documentReference ->
                documentReference!!.get().continueWith { task: Task<DocumentSnapshot> ->
                    val doc = task.result!!
                    convertToFirebaseProduct(doc)
                }
            })
        }
    }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    /**
     * Anonymous user identity needed for Firestore.
     */
    private var user: FirebaseUser?

    /**
     * Resolution of the small images, which are created by the Firestore Extension in the
     * Firebase console. Resolution is needed for downloading images, as the smaller resolution
     * is written to filename. Change this variable, if image compression resolution is changed
     * in the extension settings.
     */
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

    /**
     * Sign in anonymously to FirebaseAuthentication. Sign in is required to access and write
     * products to the database. No user input or details are needed for the sign-in as an
     * anonymous id is created by the authenticator.
     */
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
            productMap["brand"] = p.brand
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
        brand: String,
        englishName: String,
        koreanName: String,
        barcode: String?,
        description: String,
        ingredients: List<Product>,
        attributes: Map<Attribute, Boolean>
    ): Task<DocumentReference> {

        // Map attribute map to a <String, String> map for compatibility with the firestore datatypes
        val stringAttributes: HashMap<String, String> = HashMap()
        attributes.onEach { entry ->
            stringAttributes[entry.key.toString()] = entry.value.toString()
        }

        val productMap: HashMap<String, Any> = HashMap()
        productMap["brand"] = brand
        productMap["name_english"] = englishName
        productMap["name_korean"] = koreanName
        productMap["barcode"] = barcode.orEmpty()
        productMap["description"] = description
        productMap["ingredients"] =
            ingredients.map { product -> db.document("/products/${product.id}") }
        productMap["attributes"] = stringAttributes

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
            .continueWith { task: Task<QuerySnapshot> ->
                val doc = task.result!!.documents.getOrNull(0)
                if (doc != null) {
                    convertToFirebaseProduct(doc)
                } else {
                    null
                }
            }
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
            .continueWith { task: Task<QuerySnapshot> ->
                val doc = task.result!!.documents.getOrNull(0)
                if (doc != null) {
                    convertToFirebaseProduct(doc)
                } else {
                    null
                }
            }
    }

    companion object {
        fun convertToFirebaseProduct(doc: DocumentSnapshot): FirebaseProduct? {
            val obj = doc.toObject(FirebaseProduct::class.java)
            obj!!.id = doc.id
            return obj
        }
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
            .continueWith { task: Task<QuerySnapshot> ->
                val doc = task.result!!.documents.getOrNull(0)
                if (doc != null) {
                    convertToFirebaseProduct(doc)
                } else {
                    null
                }
            }
    }

    /**
     * Upload an image for a specific product. Only one image per product can be uploaded,
     * if an image already exists for a product, it will be replaced by this method.
     * @param p product which the image refers to
     * @param image file object of the image
     * @return upload task
     */
    fun uploadImage(p: Product, image: File): UploadTask {
        return uploadImage(p.id, image)
    }

    /**
     * Upload an image for a specific id. Only one image per product can be uploaded,
     * if an image already exists for an id, it will be replaced by this method.
     * @param id id of the product which the image refers to
     * @param image file object of the image
     * @return upload task
     */
    fun uploadImage(id: String, image: File): UploadTask {
        assert(image.extension == "png")
        assert(id != "")
        val storageRef = storage.reference
        val pRef = storageRef.child("${id}.png")
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
        return uploadImage(p.id, imageStream)
    }

    /**
     * Upload a image for a specific id. Only one image per product can be uploaded, if
     * an image already exists for an id, it will be replaced by this method.
     * @param id id of the product which the image refers to
     * @param imageStream stream of the image
     * @return upload task
     */
    fun uploadImage(id: String, imageStream: InputStream): UploadTask {
        assert(id != "")
        val storageRef = storage.reference
        val pRef = storageRef.child("${id}.png")
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
        return downloadImage(p.id)
    }

    /**
     * Download the original, uncompressed image for the id of a specific product. Only one image
     * per product is available. Please use downloadImageSmall for a smaller version of the
     * images, as uncompressed images might take long to download.
     * @param id if of the product, which image will be downloaded
     * @return A task of type ByteArray. Task might take some time until completion.
     */
    fun downloadImage(id: String): Task<ByteArray> {
        val storageRef = storage.reference
        val pathRef = storageRef.child("images/${id}.png")

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
        return downloadImageSmall(p.id)
    }

    /**
     * Download a compressed image in the size 1024*1024 (or smaller, according to ratio)
     * for an id of a specific product. Only one image per product is available. Small images are available
     * about 1 min after upload as the compression extension has to run first.
     * @param id id of the product, which image will be downloaded
     * @return A Task of type ByteArray. Task might take some time until completion.
     * @see Task
     */
    fun downloadImageSmall(id: String): Task<ByteArray> {
        val storageRef = storage.reference
        val pathRef = storageRef.child("images/small/${id}_${small}x${small}.png")

        return pathRef.getBytes(1024 * 1024 * 3)
    }

    /**
     * Search the entire Product collection for a product which contains the given string
     * in the english name.
     * @param s search string
     * @param ignoreCase if set to true, case is ignore while searching (default value is true)
     * @return a task of a list of FirebaseProducts which contain the search string in their english
     * name
     * @see Task
     * @see FirebaseProduct
     */
    fun searchEnglish(s: String, ignoreCase: Boolean = true): Task<List<FirebaseProduct>> {
        val list = prodColl.get()
            .continueWith { task: Task<QuerySnapshot> ->
                task.result!!.documents.mapNotNull { doc ->
                    convertToFirebaseProduct(doc)
                }
            }

        return list.continueWith { task: Task<List<FirebaseProduct>> ->
            task.result!!.filter { firebaseProduct ->
                firebaseProduct.name_english.contains(
                    s,
                    ignoreCase
                )
            }
        }
    }

    /**
     * Search the entire Product collection for a product which contains the given string
     * in the korean name.
     * @param s search string
     * @param ignoreCase if set to true, case is ignore while searching (default value is true)
     * @return a task of a list of FirebaseProducts which contain the search string in their korean
     * name
     * @see Task
     * @see FirebaseProduct
     */
    fun searchKorean(s: String, ignoreCase: Boolean = true): Task<List<FirebaseProduct>> {
        val list = prodColl.get()
            .continueWith { task: Task<QuerySnapshot> ->
                task.result!!.documents.mapNotNull { doc ->
                    convertToFirebaseProduct(doc)
                }
            }

        return list.continueWith { task: Task<List<FirebaseProduct>> ->
            task.result!!.filter { firebaseProduct ->
                firebaseProduct.name_korean.contains(
                    s,
                    ignoreCase
                )
            }
        }
    }

    /**
     * Search the entire Product collection for a product which contains the given string
     * in either the korean name or the english name.
     * @param s search string
     * @param ignoreCase if set to true, case is ignore while searching (default value is true)
     * @return a task of a list of FirebaseProducts which contain the search string in their korean
     * or english name
     * @see Task
     * @see FirebaseProduct
     */
    fun searchAll(s: String, ignoreCase: Boolean = true): Task<List<FirebaseProduct>> {
        val list = prodColl.get()
            .continueWith { task: Task<QuerySnapshot> ->
                task.result!!.documents.mapNotNull { doc ->
                    convertToFirebaseProduct(doc)
                }
            }

        return list.continueWith { task: Task<List<FirebaseProduct>> ->
            task.result!!.filter { firebaseProduct ->
                firebaseProduct.name_korean.contains(s, ignoreCase)
                        || firebaseProduct.name_english.contains(s, ignoreCase)
            }
        }
    }

    /**
     * Checks remote database for a similar product (== identical english, korean and brand names)
     * @param p product which should be checked for duplicate
     * @return a task of boolean which returns True, if a product with the identical values exists in the remote database
     * @see Task
     */
    fun exists(p: Product): Task<Boolean> {
        return exists(p.brand, p.englishName)
    }

    /**
     * Checks remote database for a product with similar values (== identical english and brand names)
     * @param brand brand name of the product
     * @param englishName english name of the product
     * @return a task of boolean which returns True, if a product with the identical values exists in the remote database
     * @see Task
     */
    fun exists(brand: String, englishName: String): Task<Boolean> {
        val query = prodColl.whereEqualTo("name_english", englishName).whereEqualTo("brand", brand)
        return query.get().continueWith { task: Task<QuerySnapshot> -> !task.result!!.isEmpty }
    }

    /**
     * Checks remote database for a product with
     */
    fun barcodeExists(barcode: String): Task<Boolean> {
        val query = prodColl.whereEqualTo("barcode", barcode)
        return query.get().continueWith { task: Task<QuerySnapshot> -> !task.result!!.isEmpty }
    }
}