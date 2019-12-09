package com.example.mcateam6.datatypes

import com.yalantis.filter.model.FilterModel

class Tag constructor(private val _text: String, private val _color: Int): FilterModel {
    override fun getText(): String {
        return _text
    }
    fun getColor(): Int {
        return _color
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tag) return false

        val tag: Tag = other
        if (getColor() != tag.getColor()) return false
        return getText()==tag.getText()
    }
}