package com.example.mcateam6.datatypes

/**
 * This class represents an object from the database with its attributes. This constructor can be used to set all available attributes of a product.
 * @param englishName English name of the product
 * @param koreanName Korean name of the product
 * @param description: String = ""
 * @param barcode Barcode of the product as a String to including leading zeros. Null, if no barcode is existing (eg. raw ingredients)
 * @param ingredients List of Product Objects representing the ingredients of the current Product. Empty, if the product doesn't have any ingredients besides itself
 * @param attributes Map of attributes (vegetarian, vegan, ...) of the product. Should at least contain every attributes of the ingredients
 * @see Attribute
 * @author Alexander Kranzer
 */
data class Product(
    var englishName: String = "",
    var koreanName: String = "",
    var barcode: String? = null,
    var description: String = "",
    var ingredients: List<Product> = emptyList(),
    var attributes: Map<Attribute, Boolean> = emptyMap()
) {

}