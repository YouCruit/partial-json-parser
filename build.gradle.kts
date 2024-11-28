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

tasks.register("prepareRelease") {
    doLast {
        val currentVersion = project.version.toString()
        val newVersion = currentVersion.replace(Regex("(\\d+\\.\\d+\\.)(\\d+)")) { matchResult ->
            val majorMinor = matchResult.groupValues[1]
            val patch = matchResult.groupValues[2].toInt() + 1
            "$majorMinor$patch"
        }
        project.version = newVersion

        val filesToCommit = listOf("build.gradle.kts")
        exec {
            commandLine("git", "add", *filesToCommit.toTypedArray())
        }
        exec {
            commandLine("git", "commit", "-m", "Release version $newVersion")
        }
        exec {
            commandLine("git", "tag", "v$newVersion")
        }
        exec {
            commandLine("git", "push")
        }
        exec {
            commandLine("git", "push", "--tags")
        }
    }
}
kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    val buildDir = layout.buildDirectory.get().asFile
    outputDirectory = File(buildDir,"generated/sources/main/kotlin/antlr")
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
