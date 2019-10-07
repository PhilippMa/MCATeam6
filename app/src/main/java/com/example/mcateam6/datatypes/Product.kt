package com.example.mcateam6.datatypes

class Product {
    var englishName: String = ""
    var koreanName: String = ""
    var barcode: String? = null
    var ingredients: List<Product> = emptyList()
    var attributes: Map<Attribute, Boolean> = emptyMap()
    var description: String = ""

    /**
     * This class represents an object from the database with its attributes. This constructor can be used to set all available attributes of a product.
     * @param englishName English name of the product
     * @param koreanName Korean name of the product
     * @param barcode Barcode of the product as a String to including leading zeros. Null, if no barcode is existing (eg. raw ingredients)
     * @param ingredients List of Product Objects representing the ingredients of the current Product. Empty, if the product doesn't have any ingredients besides itself
     * @param attributes Map of attributes (vegetarian, vegan, ...) of the product. Should at least contain every attributes of the ingredients
     * @param description (Optional) Description of the product
     * @see Attribute
     * @author Alexander Kranzer
     */
    constructor(englishName: String, koreanName: String, barcode: String, ingredients: List<Product>, attributes: Map<Attribute, Boolean>, description: String) {
        this.englishName = englishName
        this.koreanName = koreanName
        this.barcode = barcode
        this.ingredients = ingredients
        this.attributes = attributes
        this.description = description
    }

    /**
     * This class represents an object from the database with its attributes. This constructor can be for raw ingredients, as they don't have barcodes or other ingredients.
     * @param englishName English name of the product
     * @param koreanName Korean name of the product
     * @param attributes Map of attributes (vegetarian, vegan, ...) of the product. Should at least contain every attributes of the ingredients.
     * @param description (Optional) Description of the product
     * @see Attribute
     * @author Alexander Kranzer
     */
    constructor(englishName: String, koreanName: String, attributes: Map<Attribute, Boolean>, description: String) {
        this.englishName = englishName
        this.koreanName = koreanName
        this.attributes = attributes
        this.description = description
    }

    constructor() {

    }
}