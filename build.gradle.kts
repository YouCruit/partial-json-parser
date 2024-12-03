import net.researchgate.release.GitAdapter

plugins {
    signing
    kotlin("jvm") version "2.0.20"
    id("net.researchgate.release") version "3.0.2"
    id("eu.kakde.gradle.sonatype-maven-central-publisher") version "1.0.6"
    antlr
}

group = "com.lanefinder"

sonatypeCentralPublishExtension {
    val group = "com.lanefinder"
    val artifactName = "partial-json-parser"
    val publishingType = "AUTOMATIC"
    val desc = "A library to parse partial JSON data."
    val license = "The MIT License"
    val licenseUrl = "https://opensource.org/licenses/MIT"
    val githubRepo = "YouCruit/partial-json-parser"
    val developerId = "pgilmore"
    val developerName = "Patrick Gilmore"
    val developerOrganization = "lanefinder.com"
    val developerOrganizationUrl = "https://www.lanefinder.com"

    groupId.set(group)
    artifactId.set(artifactName)
    version.set(project.version.toString())
    componentType.set("java") // "java" or "versionCatalog"
    this.publishingType.set(publishingType) // USER_MANAGED or AUTOMATIC

    // Set username and password for Sonatype repository
    username.set(project.providers.environmentVariable("OSSRH_USERNAME"))
    password.set(project.providers.environmentVariable("OSSRH_PASSWORD"))

    // Configure POM metadata
    pom {
        name.set(artifactName)
        description.set(desc)
        url.set("https://github.com/${githubRepo}")
        licenses {
            license {
                name.set(license)
                url.set(licenseUrl)
            }
        }
        developers {
            developer {
                id.set(developerId)
                name.set(developerName)
                organization.set(developerOrganization)
                organizationUrl.set(developerOrganizationUrl)
            }
        }
        scm {
            url.set("https://github.com/${githubRepo}")
            connection.set("scm:git:https://github.com/${githubRepo}")
            developerConnection.set("scm:git:https://github.com/${githubRepo}")
        }
        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/${githubRepo}/issues")
        }
    }
}

signing {
    useInMemoryPgpKeys(
        project.providers.environmentVariable("GPG_KEY_ID").orNull,
        project.providers.environmentVariable("GPG_KEY").orNull,
        project.providers.environmentVariable("GPG_PASSPHRASE").orNull,
    )
    sign(tasks["jar"])
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    antlr("org.antlr:antlr4:4.9.2")
    testImplementation(kotlin("test"))
}

release {
    failOnCommitNeeded = true
    failOnPublishNeeded = true
    failOnSnapshotDependencies = true
    failOnUnversionedFiles = true
    failOnUpdateNeeded = true
    revertOnFail = true
    preTagCommitMessage = "Gradle Release Plugin] - pre tag commit: "
    tagCommitMessage = "[Gradle Release Plugin] - creating tag: "
    newVersionCommitMessage = "[Gradle Release Plugin] - new version commit: "
    tagTemplate = "v${'$'}{version}"
    versionPropertyFile = "gradle.properties"
    snapshotSuffix = "-SNAPSHOT"

    scmAdapters = listOf(GitAdapter::class.java)

    git {
        requireBranch = "master"
        pushToRemote = "origin"
        commitVersionFileOnly = true
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.javadoc {
    enabled = false
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
