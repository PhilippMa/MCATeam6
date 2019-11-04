package com.example.mcateam6.datatypes

import com.example.mcateam6.database.RemoteDatabase
import com.google.firebase.firestore.DocumentReference

/**
 * This class represents an object from the database with its attributes. This constructor can be used to set all available attributes of a product.
 * @param englishName English name of the product
 * @param koreanName Korean name of the product
 * @param description: String = ""
 * @param barcode Barcode of the product as a String to including leading zeros. Null, if no barcode is existing (eg. raw ingredients)
 * @param ingredients List of Product Objects representing the ingredients of the current Product. Empty, if the product doesn't have any ingredients besides itself
 * @param attributes Map of attributes (vegetarian, vegan, ...) of the product. Should at least contain every attributes of the ingredients
 * @see Attribute
 */
open class Product(
    var id: String = "",
    var englishName: String = "",
    var koreanName: String = "",
    var barcode: String? = null,
    var description: String = "",
    var ingredients: List<Product> = emptyList(),
    var attributes: Map<Attribute, Boolean> = emptyMap(),
    var document: DocumentReference?
) {
    /*constructor(
        englishName: String,
        koreanName: String,
        barcode: String?,
        description: String,
        ingredients: List<Product>,
        attributes: Map<Attribute, Boolean>
    ) : this("", englishName, koreanName, barcode, description, ingredients, attributes)
*/
    constructor(
        id: String,
        englishName: String,
        koreanName: String,
        barcode: String?,
        description: String,
        attributes: Map<Attribute, Boolean>
    ) : this(id, englishName, koreanName, barcode, description, emptyList(), attributes, null)


    constructor(englishName: String, koreanName: String, barcode: String?, description: String, ingredients: List<Product>, attributes: Map<Attribute, Boolean>) : this("", englishName, koreanName, barcode, description, ingredients, attributes, null)

    companion object {
        fun equals(p: Product, fP: RemoteDatabase.FirebaseProduct): Boolean {
            return p.id == fP.id
        }
    }

    /**
     * Creates a new document for the product in the remote database and
     * sets the id accordingly
     * @param db RemoteDatabase object
     * @return id of the created document
     */
    fun createDocument(db: RemoteDatabase): String {
        val doc = db.prodColl.document()
        document = doc
        id = doc.id
        return doc.id
    }
}