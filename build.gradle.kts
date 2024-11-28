plugins {
    `maven-publish`
    signing
    kotlin("jvm") version "2.0.20"
    antlr
}

group = "com.lanefinder"
version = "1.0.1"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "com.lanefinder"
            artifactId = "partial-json-parser"
            version = project.version.toString()

            pom {
                name.set("Partial JSON Parser")
                description.set("A library to parse partial JSON data.")
                url.set("https://github.com/YouCruit/partial-json-parser")

                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("pgilmore")
                        name.set("Patrick Gilmore")
                        email.set("patrick.gilmore@lanefinder.com")
                        organization.set("Lanefinder")
                        organizationUrl.set("https://www.lanefinder.com/")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/YouCruit/partial-json-parser.git")
                    developerConnection.set("scm:git:ssh://git@github.com:YouCruit/partial-json-parser.git")
                    url.set("https://github.com/YouCruit/partial-json-parser")
                }
            }
        }
    }
    repositories {
        maven {
            name = "ossrh"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.providers.environmentVariable("OSSRH_USERNAME").orNull
                password = project.providers.environmentVariable("OSSRH_PASSWORD").orNull
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        project.providers.environmentVariable("GPG_KEY_ID").orNull,
        project.providers.environmentVariable("GPG_KEY").orNull,
        project.providers.environmentVariable("GPG_PASSPHRASE").orNull
    )
    sign(publishing.publications["mavenJava"])
}

// Add checks during task execution
tasks.withType<PublishToMavenRepository>().configureEach {
    doFirst {
        // Fail the build if the version contains "SNAPSHOT"
        if (project.version.toString().contains("SNAPSHOT", ignoreCase = true)) {
            throw GradleException("Publishing snapshots is not allowed. Current version: ${project.version}")
        }

        // Check for required environment variables
        val ossrhUsername = System.getenv("OSSRH_USERNAME")
        val ossrhPassword = System.getenv("OSSRH_PASSWORD")
        if (ossrhUsername.isNullOrEmpty() || ossrhPassword.isNullOrEmpty()) {
            throw GradleException("OSSRH_USERNAME and OSSRH_PASSWORD must be set to publish to OSSRH")
        }

        // Ensure GPG keys are set for signing
        val signingKeyId = System.getenv("GPG_KEY_ID")
        val signingPassword = System.getenv("GPG_PASSPHRASE")
        val signingKey = System.getenv("GPG_KEY")
        if (signingKeyId.isNullOrEmpty() || signingPassword.isNullOrEmpty() || signingKey.isNullOrEmpty()) {
            throw GradleException("GPG_KEY_ID, GPG_KEY, and GPG_PASSPHRASE must be set for signing")
        }
    }
}

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
    outputDirectory = File(buildDir, "generated/sources/main/kotlin/antlr")
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
