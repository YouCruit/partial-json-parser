package com.lanefinder.parser

fun JsonValue.valueAt(path: String): Any? {
    return when(this) {
        is JsonObject -> valueAt(path)
        is JsonArray -> valueAt(path)
        is JsonLiteral -> valueAt(path)
        is JsonPair -> valueAt(path)
    }
}

private fun JsonObject.valueAt(path: String): Any? {
    return if (path == "") {
        this.toString()
    } else {
        val (firstPart, remainingPart) = extractNextKey(path)
        return pairs.firstOrNull { it.key == firstPart }?.valueAt(remainingPart)
    }
}

private fun JsonPair.valueAt(path: String): Any? {
    return value?.valueAt(path)
}

private fun JsonArray.valueAt(path: String): Any? {
    return if (path == "") {
        this.toString()
    } else {
        val firstPart = path.substringBefore(".")
        val remainingPart = path.substringAfter(firstPart)
        val number = """\[(\d+)]""".toRegex().find(firstPart)?.groupValues?.get(1)?.toInt()!!
        this.values[number]?.valueAt(remainingPart)
    }
}

private fun JsonLiteral.valueAt(path: String): Any? {
    if (path != "" && path != ".") {
        throw IllegalArgumentException("Invalid path $path for $this")
    }
    return when (type) {
        JsonLiteralType.STRING -> {
            var value = value as String
            if (partial) {
                value = value.substring(1).removeSuffix("\\")
            } else {
                value = value.removeSurrounding("\"")
            }
            return value
                .replace("\\\"", "\"") // Replace \" with "
                .replace("\\t", "\t") // Replace \\ with \
                .replace("\\r", "\r")
                .replace("\\n", "\n") // Replace \\ with \
                .replace("\\\\", "\\") // Replace \\ with \
                .replace("\\/", "/") // Replace \\ with \
                .replace(Regex("\\\\u([0-9A-Fa-f]{4})")) { matchResult ->
                    val unicodeValue = matchResult.groupValues[1]
                    val charCode = unicodeValue.toInt(16) // Parse hex value
                    charCode.toChar().toString()         // Convert to character
                }
        }
        else -> value
    }
}
