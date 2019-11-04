package com.example.mcateam6.datatypes

/**
 * This enum class contains all possible attribute keys, like vegan or vegetarian. It is used by
 * the Product class to map the Attributes to boolean values. If a product covers more than one
 * attribute indirectly (like vegan --> vegetarian), both attributes should be true.
 * @see Product
 * @author Alexander Kranzer
 */
enum class Attribute {
    /**
     * Attribute id for a vegan product
     */
    VEGAN,
    /**
     * Attribute id for a vegetarian product
     */
    VEGETARIAN
}