package com.example.mcateam6.datatypes

class Product {
    var englishName: String = ""
    var koreanName: String = ""
    var barcode: String? = null
    var ingredients: List<Product> = emptyList()
    var attributes: Map<String, Boolean> = emptyMap()

    /**
     * @param englishName
     * @param koreanName
     * @param barcode
     * @param ingredients
     * @param attributes
     */
    constructor(englishName: String, koreanName: String, barcode: String, ingredients: List<Product>, attributes: Map<String, Boolean>) {
        this.englishName = englishName
        this.koreanName = koreanName
        this.barcode = barcode
        this.ingredients = ingredients
        this.attributes = attributes
    }

    constructor() {

    }
}