package com.lanefinder.parser

fun extractNextKey(path: String): Pair<String, String> {
    val firstPart = path.substringAfter(".").substringBefore(".").substringBefore("[")
    val remainingPart = path.substringAfter(".$firstPart")
    return firstPart to remainingPart
}
