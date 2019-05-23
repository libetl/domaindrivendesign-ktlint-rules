import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

group = "org.toilelibre.libe"
version = "1.0.0"

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "8.0.0"
    id("org.jetbrains.kotlin.jvm") version "1.3.31"
    java
    jacoco
    application
    idea
    `maven-publish`
    signing
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin"))
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4+")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:8.0.0")
    }
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("jacoco")
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    kotlin {
        sourceSets {
            val main by getting {
                kotlin.srcDir("src/main/kotlin")
                resources.srcDir("src/main/resources")
            }

            val test by getting {
                kotlin.srcDir("src/test/kotlin")
                resources.srcDir("src/test/resources")
                dependsOn(main)
            }
        }
    }

    dependencies {
        compileOnly("org.jetbrains.intellij.deps:trove4j:1.0.20181211")
        compileOnly("com.pinterest:ktlint:0.32.0")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        compileOnly("org.jetbrains.kotlin:kotlin-reflect")
        testImplementation("junit:junit:4.12")
        testImplementation("org.assertj:assertj-core:3.10.0")
        testImplementation("com.pinterest.ktlint:ktlint-test:0.32.0")
        testImplementation("com.pinterest.ktlint:ktlint-core:0.32.0")
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        testImplementation("org.jetbrains.kotlin:kotlin-reflect")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
        kotlinOptions.jvmTarget = "1.8"
    }

    dependencies { // you can create a ktlint run config with that
        compileOnly("com.pinterest:ktlint:0.32.0")
        compileOnly("org.jetbrains.intellij.deps:trove4j:1.0.20181211")
    }
}

configure<IdeaModel> {
    project {
        languageLevel = IdeaLanguageLevel(JavaVersion.VERSION_1_8)
    }
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

ktlint {
    version.set("0.32.0")
    debug.set(false)
    verbose.set(false)
    android.set(false)
    outputToConsole.set(true)
    reporters.set(setOf(ReporterType.PLAIN, ReporterType.CHECKSTYLE))
    ignoreFailures.set(true)
    enableExperimentalRules.set(true)
    filter {
        include("**/kotlin/**")
        exclude("**/*.json")
        exclude("**/*.properties")
        exclude("**/*.xml")
        exclude("**/*.yml")
    }
}

jacoco {
    toolVersion = "0.8.3"
    reportsDir = file("$buildDir/customJacocoReportDir")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
        create<MavenPublication>("binaryAndSources") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
        }
    }

    repositories {
        maven {
            name = "oss-sonatype"
            url = uri("https://oss.sonatype.org/")
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useGpgCmd()
    sign(configurations.archives.get())
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(tasks["jar"])
    sign(publishing.publications["maven"])
}

gradle.taskGraph.whenReady {
    if (allTasks.any { it is Sign }) {
        // Use Java's console to read from the console (no good for
        // a CI environment)
        val console = System.console()
        console.printf("\n\nWe have to sign some things in this build." +
                "\n\nPlease enter your signing details.\n\n")

        val id = console.readLine("PGP Key Id: ")
        val file = console.readLine("PGP Secret Key Ring File (absolute path): ")
        val password = console.readPassword("PGP Private Key Password: ")

        allprojects {
            extra["signing.keyId"] = id
            extra["signing.secretKeyRingFile"] = file
            extra["signing.password"] = password
        }

        console.printf("\nThanks.\n\n")
    }
}

