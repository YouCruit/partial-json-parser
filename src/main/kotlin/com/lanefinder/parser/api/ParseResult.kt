package com.lanefinder.parser.api

import com.lanefinder.parser.JsonValue
import com.lanefinder.parser.isPartial
import com.lanefinder.parser.valueAt

class ParseResult(private val root: JsonValue?) {
    fun isPartial(path: String): Boolean {
        return root?.isPartial(path) ?: true
    }

    fun valueAt(path: String): Any? {
        return root?.valueAt(path)
    }
}
