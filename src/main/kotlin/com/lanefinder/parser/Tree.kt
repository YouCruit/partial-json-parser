package com.lanefinder.parser

import com.lanefinder.parser.PartialJsonGrammarParser.ArrayContext
import com.lanefinder.parser.PartialJsonGrammarParser.FalseContext
import com.lanefinder.parser.PartialJsonGrammarParser.PartialPairContext
import com.lanefinder.parser.PartialJsonGrammarParser.PartialStringContext
import com.lanefinder.parser.PartialJsonGrammarParser.JsonContext
import com.lanefinder.parser.PartialJsonGrammarParser.ObjectContext
import com.lanefinder.parser.PartialJsonGrammarParser.NullContext
import com.lanefinder.parser.PartialJsonGrammarParser.NumberContext
import com.lanefinder.parser.PartialJsonGrammarParser.StringContext
import com.lanefinder.parser.PartialJsonGrammarParser.TrueContext
import com.lanefinder.parser.PartialJsonGrammarParser.ValueContext
import com.lanefinder.parser.PartialJsonGrammarParser.KeyValuePairContext
import com.lanefinder.parser.PartialJsonGrammarParser.PairContext
import com.lanefinder.parser.PartialJsonGrammarParser.PartialTrueContext
import com.lanefinder.parser.PartialJsonGrammarParser.PartialFalseContext
import com.lanefinder.parser.PartialJsonGrammarParser.PartialNullContext
import com.lanefinder.parser.PartialJsonGrammarParser.PartialNumberContext
import com.lanefinder.parser.PartialJsonGrammarParser.PartialObjectContext
import com.lanefinder.parser.PartialJsonGrammarParser.PartialArrayContext

fun buildJson(context: JsonContext): JsonValue {
    val value = context.value()
    return value.buildTree()
}

fun KeyValuePairContext.buildTree(): JsonPair {
    return when (this) {
        is PartialPairContext -> buildTree()
        is PairContext -> buildTree()
        else -> throw RuntimeException("Unknown pair context $this")
    }
}

fun PartialPairContext.buildTree(): JsonPair {
    val key = (STRING() ?: PARTIAL_STRING()).text.removeSurrounding("\"")
    return JsonPair(key = key, value = null, partial = true)
}

fun PairContext.buildTree(): JsonPair {
    val key = STRING().text.removeSurrounding("\"")
    val valueTree = value().buildTree()
    return JsonPair(key = key, value = valueTree, partial = false)
}

fun ObjectContext.buildTree(): JsonObject {
    val pairs = jsonObject().keyValuePair().map { it.buildTree() }
    return JsonObject(pairs = pairs, false)
}

fun PartialObjectContext.buildTree(): JsonObject {
    val pairs = partialJsonObject().keyValuePair().map { it.buildTree() }
    return JsonObject(pairs = pairs, partial = true)
}

fun ArrayContext.buildTree(): JsonArray {
    return JsonArray(values = jsonArray().value().map { it.buildTree() })
}

fun PartialArrayContext.buildTree(): JsonArray {
    return JsonArray(values = partialJsonArray().value().map { it.buildTree() }, partial = true)
}

fun PartialStringContext.buildTree(): JsonLiteral {
    return JsonLiteral(value = PARTIAL_STRING().text, type = JsonLiteralType.STRING, partial = true)
}

fun StringContext.buildTree(): JsonLiteral {
    return JsonLiteral(value = STRING().text, type = JsonLiteralType.STRING)
}

fun NumberContext.buildTree(): JsonLiteral {
    return JsonLiteral(value = NUMBER().text.toIntOrNull() ?: NUMBER().text.toDouble(), type = JsonLiteralType.NUMBER)
}

fun PartialNumberContext.buildTree(): JsonLiteral {
    return JsonLiteral(value = null, type = JsonLiteralType.NUMBER, partial = true)
}

fun TrueContext.buildTree(): JsonLiteral {
    return JsonLiteral(value = true, type = JsonLiteralType.BOOLEAN)
}

fun PartialTrueContext.buildTree(): JsonLiteral {
    return JsonLiteral(value = true, type = JsonLiteralType.BOOLEAN, partial = true)
}

fun FalseContext.buildTree(): JsonLiteral {
    return JsonLiteral(value = false, type = JsonLiteralType.BOOLEAN)
}

fun PartialFalseContext.buildTree(): JsonLiteral {
    return JsonLiteral(value = false, type = JsonLiteralType.BOOLEAN, partial = true)
}

fun NullContext.buildTree(): JsonLiteral {
    return JsonLiteral(value = null, type = JsonLiteralType.NULL)
}

fun PartialNullContext.buildTree(): JsonLiteral {
    return JsonLiteral(value = null, type = JsonLiteralType.NULL, partial = true)
}

fun ValueContext.buildTree(): JsonValue {
    return when (this) {
        is StringContext -> buildTree()
        is PartialStringContext -> buildTree()
        is NumberContext -> buildTree()
        is TrueContext -> buildTree()
        is PartialNumberContext -> buildTree()
        is PartialTrueContext -> buildTree()
        is FalseContext -> buildTree()
        is PartialFalseContext -> buildTree()
        is NullContext -> buildTree()
        is PartialNullContext -> buildTree()
        is ObjectContext -> buildTree()
        is PartialObjectContext -> buildTree()
        is ArrayContext -> buildTree()
        is PartialArrayContext -> buildTree()
        else -> throw RuntimeException("Unknown context $this")
    }
}

sealed interface JsonValue

class JsonObject(val pairs: List<JsonPair>, val partial: Boolean = false) : JsonValue {
    override fun toString(): String {
        val pairsString = pairs.joinToString(",") { "$it" }
        return "{$pairsString}"
    }
}

class JsonPair(val key: String, val value: JsonValue?, val partial: Boolean = false) : JsonValue {
    override fun toString(): String {
        return "\"$key\":${value}"
    }
}

class JsonArray(val values: List<JsonValue?>, val partial: Boolean = false) : JsonValue {
    override fun toString(): String {
        val valuesString = values.joinToString(",") { "$it" }
        return "[$valuesString${if (partial) "" else "]"}"
    }
}

class JsonLiteral(val value: Any?, val type: JsonLiteralType, val partial: Boolean = false) : JsonValue {
    override fun toString(): String {
        return value.toString()
    }
}

enum class JsonLiteralType {
    STRING, NUMBER, BOOLEAN, NULL
}
