package com.lanefinder.parser.api

import com.lanefinder.parser.PartialJsonGrammarLexer
import com.lanefinder.parser.PartialJsonGrammarParser
import com.lanefinder.parser.buildJson
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class PartialJsonParser {
    fun parse(textToParse: String): ParseResult {
        if (textToParse == "") {
            return ParseResult(null)
        }
        val charStream = CharStreams.fromString(textToParse)
        val lexer = PartialJsonGrammarLexer(charStream)
        val tokens = CommonTokenStream(lexer)
        val parser = PartialJsonGrammarParser(tokens)
        return buildParseResult(parser)
    }

    private fun buildParseResult(parser: PartialJsonGrammarParser): ParseResult {
        val json = parser.json()
        val jsonValue = buildJson(json)
        return ParseResult(jsonValue)
    }
}

