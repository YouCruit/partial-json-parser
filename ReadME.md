# Partial JSON Parser

## Overview

Partial JSON Parser is a Kotlin-based library designed for parsing and handling incomplete or partially received JSON data. Built with ANTLR, this parser suite enables you to work with JSON documents that may not be fully available or properly terminated. The parser provides robust handling and analysis of partial JSON data, making it suitable for streaming applications or situations where data is received in chunks.

## Features

- **Parse Incomplete JSON**: Handle JSON strings that are partially received or incomplete.
- **Detect Partial Elements**: Identify which elements or values within the JSON structure are incomplete.
- **Robust and Customizable**: Use powerful ANTLR grammar to define custom parsing logic.
- **Kotlin-based Implementation**: Seamlessly integrates with Kotlin projects and supports JVM-based applications.

## Getting Started

To start using Partial JSON Parser, you can add it as a dependency in your Gradle project:

```kotlin
dependencies {
    implementation("com.lanefinder:partial-json-parser:1.0.0")
}
```

Ensure that you have the ANTLR plugin configured as follows:

```kotlin
plugins {
    kotlin("jvm") version "2.0.20"
    antlr
}
```

### Prerequisites

- Kotlin JVM 17
- Gradle 8.6 or newer
- ANTLR 4

## Usage

Here's a simple example to demonstrate how to use the Partial JSON Parser:

```kotlin
import com.lanefinder.parser.api.PartialJsonParser

fun main() {
    val json = """{"key": "value that is not complete..."""
    val parser = PartialJsonParser()
    val parseResult = parser.parse(json)

    println("Partial: ${parseResult.isPartial(".key")}")
    println("Value: ${parseResult.valueAt(".key")}")
}
```

## Project Structure

- **`com.lanefinder.parser`**: Contains core logic for JSON parsing, construction of JSON elements, and handling partial data scenarios.
- **`com.lanefinder.parser.api`**: Provides interfaces and classes for interfacing with the parsing functionality.
- **ANTLR Grammar Files**: Define the rules and structures for parsing JSON and partial JSON data.

## Tests

This repository includes comprehensive tests to ensure the parser handles all defined scenarios, including:

- Partial JSON Objects and Arrays
- Handling Escaped Characters
- Complex Nested Structures
- Incomplete JSON Tokens

Run tests using the `./gradlew test` command.

## Contributing

We welcome contributions to the project. Please feel free to submit a pull request or open an issue for any bugs or feature requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.