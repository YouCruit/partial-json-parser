package com.lanefinder.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class JsonOutputParserTest {
    private val parser = PartialJsonParser()

    @Test
    fun `partial content test`() {
        val incompleteJson = """{"aField": "partial text that is not complete yet..."""
        val expectedValue = "partial text that is not complete yet..."
        val actualValue = parser.parse(incompleteJson).valueAt(".aField")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `partial with ending quote`() {
        val incompleteJson = """{"aField": "partial text that is not complete yet...""""
        val expectedValue = """partial text that is not complete yet..."""
        val actualValue = parser.parse(incompleteJson).valueAt(".aField")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `complete simple object`() {
        val incompleteJson = """{"aField": "partial text that is not complete yet..."}"""
        val expectedValue = """partial text that is not complete yet..."""
        val actualValue = parser.parse(incompleteJson).valueAt(".aField")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `complete object with different types`() {
        val json = """{"aField": "text", "number": 5.5, "int_value": 1, "string_with_phone": "+1555555", "string_with_number": "666", "boolean_value": true, "boolean_false":false, "null_value": null}"""
        val result = parser.parse(json)
        assertEquals("text", result.valueAt(".aField"))
        assertEquals(5.5, result.valueAt(".number"))
        assertEquals(1, result.valueAt(".int_value"))
        assertEquals("+1555555", result.valueAt(".string_with_phone"))
        assertEquals("666", result.valueAt(".string_with_number"))
    }

    @Test
    fun `partial simple object with partial field name`() {
        val incompleteJson = """{"aFie"""
        val actualValue = parser.parse(incompleteJson).valueAt(".aField")
        assertNull(actualValue)
    }

    @Test
    fun `complete numbers`() {
        val completeNumbers = listOf("-1", "2.2", "-0.3", "2.2e4", "-3e+10", "4e-2")
        for (completeNumber in completeNumbers) {
            val res = parser.parse(completeNumber)
            assertNotNull(res.valueAt("."))
            assertFalse(res.isPartial("."))
        }
    }

    @Test
    fun `partial numbers`() {
        val incompleteNumbers = listOf("-", "2.", "-0.", "2.2e", "-3e+", "4e-")
        for (incompleteNumber in incompleteNumbers) {
            val res = parser.parse(incompleteNumber)
            assertNull(res.valueAt("."))
            assertTrue(res.isPartial("."))
        }
    }

    @Test
    fun `partial simple object with complete field name no colon`() {
        val incompleteJson = """{"aField""""
        val actualValue = parser.parse(incompleteJson).valueAt(".aField")
        assertNull(actualValue)
    }

    @Test
    fun `partial simple object with complete field name and colon`() {
        val incompleteJson = """{"aField":"""
        val expectedValue = null
        val actualValue = parser.parse(incompleteJson).valueAt(".aField")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `complete simple object with simple escaped characters`() {
        val jsonString = """{"aField": "This is a test with escape sequences: \" \\ \n"}"""
        val expectedValue = """This is a test with escape sequences: " \ 

        """.trimIndent()

        val actualValue = parser.parse(jsonString).valueAt(".aField")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `complete simple object with  b control character`() {
        val jsonString = """{"aField": "This is a test with escape sequences: \b"}"""
        val expectedValue = """This is a test with escape sequences: \b"""
        val actualValue = parser.parse(jsonString).valueAt(".aField")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `complete simple object with  r control character`() {
        val jsonString = """{"aField": "This is a test with escape sequences: \r"}"""
        val expectedValue = """
            This is a test with escape sequences: 
            
        """.trimIndent()
        val actualValue = parser.parse(jsonString).valueAt(".aField") as String
        assertEquals(expectedValue, actualValue.replace("\r", "\n"))
    }

    @Test
    fun `complete simple object with  t control character`() {
        val jsonString = """{"aField": "This is a test with escape sequences: \t"}"""
        val expectedValue = """This is a test with escape sequences: 	"""
        val actualValue = parser.parse(jsonString).valueAt(".aField") as String
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `complete simple object with  u0031 control character`() {
        val jsonString = """{"aField": "This is a test with escape sequences: \u0031"}"""
        val expectedValue = """This is a test with escape sequences: 1"""
        val actualValue = parser.parse(jsonString).valueAt(".aField")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `complete simple object with  slash character`() {
        val jsonString = """{"aField": "This is a test with escape sequences: \/"}"""
        val expectedValue = """This is a test with escape sequences: /"""
        val actualValue = parser.parse(jsonString).valueAt(".aField")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `two escaped linebreaks at end of line`() {
        val jsonString = """{"type":"CONVERSATION", "text": "Great! I'll help you get in touch to find out more. Here's the contact information for each one:\n\n"""
        val expectedValue = """
            Great! I'll help you get in touch to find out more. Here's the contact information for each one:
            
            
        """.trimIndent()
        val actualValue = parser.parse(jsonString).valueAt(".text")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `ignore lone unescaped backslash in partial object`() {
        val jsonString = """{"asdf":"text\"""
        val expectedValue = "text"
        val actualValue = parser.parse(jsonString).valueAt(".asdf")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `include spaces at end of partial object`() {
        val jsonString = """{"asdf":"text """
        val expectedValue = "text "
        val actualValue = parser.parse(jsonString).valueAt(".asdf")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `include spaces at start of partial object`() {
        val jsonString = """{"asdf":" text"""
        val expectedValue = " text"
        val actualValue = parser.parse(jsonString).valueAt(".asdf")
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `incomplete true is true`() {
        val jsonString = """{"asdf":t"""
        val res = parser.parse(jsonString)
        val actualValue = res.valueAt(".asdf")
        assertEquals(true, actualValue)
        assertEquals(true, res.isPartial(".asdf"))
    }

    @Test
    fun `incomplete true is true - space`() {
        val jsonString = """{"asdf":t """
        val res = parser.parse(jsonString)
        val actualValue = res.valueAt(".asdf")
        assertEquals(true, actualValue)
        assertEquals(true, res.isPartial(".asdf"))
    }

    @Test
    fun `incomplete null is null`() {
        val jsonString = """{"asdf":n"""
        val res = parser.parse(jsonString)
        val actualValue = res.valueAt(".asdf")
        assertNull(actualValue)
        assertTrue(res.isPartial(".asdf"))
    }

    @Test
    fun `incomplete false is false`() {
        val jsonString = """{"asdf": fa"""
        val res = parser.parse(jsonString)
        val actualValue = res.valueAt(".asdf")
        assertEquals(false, actualValue)
        assertEquals(true, res.isPartial(".asdf"))
    }

    @Test
    fun `boolean with space is complete`() {
        val jsonString = """{"asdf":true """
        val resultValue = parser.parse(jsonString)
        val actualValue = resultValue.valueAt(".asdf")
        assertEquals(true, actualValue)
        assertEquals(true, resultValue.isPartial("."))
        assertEquals(false, resultValue.isPartial(".asdf"))
    }

    @Test
    fun `null with space is complete`() {
        val jsonString = """{"asdf":null """
        val resultValue = parser.parse(jsonString)
        assertNotNull(resultValue)

        val actualValue = resultValue.valueAt(".asdf")
        assertNull(actualValue)
        assertEquals(true, resultValue.isPartial("."))
        assertEquals(false, resultValue.isPartial(".asdf"))
    }

    @Test
    fun `nested object`() {
        val jsonString = """{"foo": { "bar": 1 } }"""
        val resultValue = parser.parse(jsonString)
        assertNotNull(resultValue)

        assertEquals("""{"bar":1}""", resultValue.valueAt(".foo"))
    }

    @Test
    fun `incomplete nested object`() {
        val jsonString = """{"foo": { "bar": 1 """
        val resultValue = parser.parse(jsonString)
        assertNotNull(resultValue)

        assertEquals("""{"bar":1}""", resultValue.valueAt(".foo"))
        assertEquals(1, resultValue.valueAt(".foo.bar"))
    }

    @Test
    fun `array value`() {
        val jsonString = """{"foo": [1]}"""
        val resultValue = parser.parse(jsonString)
        assertNotNull(resultValue)

        assertEquals("[1]", resultValue.valueAt(".foo"))
    }

    @Test
    fun `nested array`() {
        val jsonString = """{"foo": [[1]]}"""
        val resultValue = parser.parse(jsonString)
        assertNotNull(resultValue)

        assertEquals("[[1]]", resultValue.valueAt(".foo"))
    }

    @Test
    fun `partial array`() {
        val jsonString = """{"foo": [1"""
        val resultValue = parser.parse(jsonString)
        assertNotNull(resultValue)

        assertEquals("[1", resultValue.valueAt(".foo"))
    }

    @Test
    fun `array indices`() {
        val jsonString = """{"foo": ["bar", "baz"]"""
        val resultValue = parser.parse(jsonString)
        assertNotNull(resultValue)

        assertEquals("bar", resultValue.valueAt(".foo[0]"))
        assertEquals("baz", resultValue.valueAt(".foo[1]"))
    }

    @Test
    fun `deep array path`() {
        val jsonString = """
          {
            "foo": [
              {
                "bar": [
                  2,
                  {
                    "baz": 3
                  }
                ]
              },
              1
            ]
          }
        """.trimIndent()
        val resultValue = parser.parse(jsonString)
        assertNotNull(resultValue)

        assertEquals(3, resultValue.valueAt(".foo[0].bar[1].baz"))
    }

    @Test
    fun `Incomplete pair`() {
        val jsonString = """{"foo": """
        val resultValue = parser.parse(jsonString)
        assertNotNull(resultValue)
        assertNull(resultValue.valueAt(".foo"))
        assertTrue(resultValue.isPartial("."))
        assertTrue(resultValue.isPartial(".foo"))
    }

    @Test
    fun `Incomplete pair 2`() {
        val jsonString = """{"foo" """
        val resultValue = parser.parse(jsonString)
        assertNotNull(resultValue)
        assertNull(resultValue.valueAt(".foo"))
        assertTrue(resultValue.isPartial("."))
        assertTrue(resultValue.isPartial(".foo"))
    }

    @Test
    fun `Partial boolean`() {
        val jsonString = """{"foo": tr"""
        val resultValue = parser.parse(jsonString)
        assertEquals(true, resultValue.valueAt(".foo"))
        assertTrue(resultValue.isPartial("."))
        assertTrue(resultValue.isPartial(".foo"))
    }

    @Test
    fun `Complex object with varying cut-offs`() {
        val jsonInput = """
            {
              "string": "hello world",
              "integer": 123,
              "negative_integer": -456,
              "float": 789.1011,
              "negative_float": -12.34,
              "boolean_true": true,
              "boolean_false": false,
              "null_value": null,
              "empty_object": {},
              "nested_object": {
                "nested_key1": "nested_value1",
                "nested_key2": {
                  "deep_nested_key": 9999
                }
              },
              "empty_array": [],
              "array_of_numbers": [1, 2, 3, 4.56, -7],
              "array_of_strings": ["foo", "bar", "baz"],
              "array_of_mixed_types": [true, null, "text", 42, {"inner_obj_key": "value"}, [1, 2, 3]],
              "unicode_string": "こんにちは",
              "escaped_string": "This is a \"quoted\" string with a backslash: \\",
              "complex_object": {
                "key_with_space": "value with space",
                "key-with-hyphen": "value-with-hyphen",
                "key_with_underscore": "value_with_underscore",
                "key.with.dot": "value.with.dot"
              },
              "deeply_nested": {
                "level1": {
                  "level2": {
                    "level3": {
                      "level4": {
                        "level5": "deep_value"
                      }
                    }
                  }
                }
              },
              "long_array": [
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
              ],
              "special_characters": "!@#${'$'}%^&*()_+{}[]:;<>,.?/~`|\\"
            }
        """.trimIndent()
        for (cutOff in 0..jsonInput.length) {
            val subJson = jsonInput.substring(0, cutOff)
            parser.parse(subJson)
        }
    }

    @Test
    fun `single bracket`() {
        val resultValue = parser.parse("{")
        assertNull(resultValue.valueAt("."))
        assertTrue(resultValue.isPartial("."))
    }

    @Test
    fun `partial key`() {
        val resultValue = parser.parse("{\"")
        assertNull(resultValue.valueAt("."))
        assertTrue(resultValue.isPartial("."))
    }

    @Test
    fun `escaped quote does not make value full`() {
        val jsonString = """{"asdf":"abc\""""
        val resultValue = parser.parse(jsonString)
        assertTrue(resultValue.isPartial(".asdf"))
        assertTrue(resultValue.isPartial("."))
        assertEquals("abc\"", resultValue.valueAt(".asdf"))
    }

    @Test
    fun `complete simple object with all escaped characters1`() {
        val jsonString = """
        {
            "testField": "This string contains all escape characters: \"quotes\", \\backslash, \/forward slash,\n newline,\r carriage return, \t tab, \u00A9 copyright symbol"
        }
        """

        val expectedValue = """
            This string contains all escape characters: "quotes", \backslash, /forward slash,
             newline,
             carriage return, 	 tab, © copyright symbol
        """.trimIndent()

        val resultValue = parser.parse(jsonString)
        val actual = resultValue.valueAt(".testField")
        require(actual is String)
        assertEquals(expectedValue.length, actual.length)
        assertEquals(expectedValue, actual.replace("\r", "\n")) // interpret \r and \n the same for testing purposes
    }
}
