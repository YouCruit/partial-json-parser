plugins {
    kotlin("jvm") version "2.0.20"
    antlr
}

group = "com.lanefinder"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.10")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    outputDirectory = file("${project.buildDir}/generated/sources/main/kotlin/antlr")
    arguments = listOf("-package", "com.lanefinder.parser")
}

tasks.compileTestKotlin {
    dependsOn(tasks.generateTestGrammarSource)
}

sourceSets {
    main {
        java {
            srcDir(tasks.generateGrammarSource)
        }
    }
}
