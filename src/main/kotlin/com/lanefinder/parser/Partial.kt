package com.lanefinder.parser

fun ParseResult.isPartial(path: String): Boolean {
    return root?.isPartial(path) ?: true
}

class FastIllegalArgumentException(message: String) : IllegalArgumentException(message) {
    // Fill in stack trace takes time
    // This might be thrown in a tight loop where we dont need the penalty
    override fun fillInStackTrace(): Throwable {
        return this
    }
}

fun JsonObject.isPartial(path: String): Boolean {
    return if (path == "" || path == ".") {
        partial
    } else {
        val (firstPart, remainingPart) = extractNextKey(path)
        return pairs.firstOrNull { it.key == firstPart }?.isPartial(remainingPart)
            ?: throw FastIllegalArgumentException("Invalid path $path for $this")
    }
}

fun JsonPair.isPartial(path: String): Boolean {
    return partial || (value?.isPartial(path) ?: false)
}

fun JsonArray.isPartial(path: String): Boolean {
    return if (path == "") {
        partial
    } else {
        val firstPart = path.substringBefore(".")
        val remainingPart = path.substringAfter(firstPart)
        val number = """\[(\d+)]""".toRegex().find(firstPart)?.groupValues?.get(1)?.toInt()!!
        this.values[number]?.isPartial(remainingPart) ?: throw IllegalArgumentException("TODO")
    }
}

fun JsonLiteral.isPartial(path: String): Boolean {
    if (path != "" && path != ".") {
        throw IllegalArgumentException("Invalid path $path for $this")
    }
    return partial
}

private fun JsonValue.isPartial(path: String): Boolean {
    return when (this) {
        is JsonObject -> isPartial(path)
        is JsonArray -> isPartial(path)
        is JsonLiteral -> isPartial(path)
        is JsonPair -> isPartial(path)
    }
}

