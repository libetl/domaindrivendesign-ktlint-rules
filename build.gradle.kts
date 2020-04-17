import groovy.lang.Closure
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.net.URI

group = "org.toile-libre.libe"
version = "1.0.0"

rootProject.extra["junit-jupiter.version"] = "5.6.2"
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "8.0.0"
    id("org.jetbrains.kotlin.jvm") version "1.3.40"
    id("com.bmuschko.nexus") version "2.3.1"
    id("io.codearte.nexus-staging") version "0.21.0"
    `java-library`
    jacoco
    application
    idea
    maven
    `maven-publish`
    signing
}

application {
    mainClassName = "org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider"
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin"))
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4+")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:8.0.0")
        classpath("com.bmuschko:gradle-nexus-plugin:2.3.1")
    }
}

allprojects {
    apply {
        plugin("java-library")
        plugin("kotlin")
        plugin("jacoco")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("com.bmuschko.nexus")
        plugin("io.codearte.nexus-staging")
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
        testRuntimeOnly("org.junit.platform:junit-platform-commons:1.6.2")
        testRuntimeOnly("org.junit.platform:junit-platform-engine:1.6.2")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
        testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
        testImplementation("com.winterbe:expekt:0.5.0")
        testImplementation("com.pinterest.ktlint:ktlint-test:0.35.0")
        testImplementation("com.pinterest.ktlint:ktlint-core:0.35.0")
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        testImplementation("org.jetbrains.kotlin:kotlin-reflect")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
        kotlinOptions.jvmTarget = "1.8"
    }

    dependencies {
        // you can create a ktlint run config with that
        compileOnly("com.pinterest:ktlint:0.35.0")
        compileOnly("org.jetbrains.intellij.deps:trove4j:1.0.20181211")
    }
    
    nexusStaging {
        packageGroup = "org.toile-libre.libe"
        stagingProfileId = "3ebf87dd30af1"
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
    maven {
        url = URI("https://oss.sonatype.org/content/repositories/snapshots")
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

nexus {
    sign = true
}

val sources by tasks.registering(Jar::class) {
    baseName = project.name
    classifier = "sources"
    version = null
    from(sourceSets.main.get().allSource)
}

val modifyPom : Closure<MavenPom> by ext

modifyPom(closureOf<MavenPom> {
    project {
        withGroovyBuilder {
            "name"("domaindrivendesign-ktlint-rules")
            "description"("Domain Driven Design ktlint rules")
            "url"("https://github.com/libetl/domaindrivendesign-ktlint-rules")
            "inceptionYear"("2019")

            "scm" {
                "connection"("scm:git@github.com:libetl/domaindrivendesign-ktlint-rules.git")
                "developerConnection"("scm:git@github.com:libetl/domaindrivendesign-ktlint-rules.git")
                "url"("scm:git@github.com:libetl/domaindrivendesign-ktlint-rules.git")
            }

            "licenses" {
                "license" {
                    "name"("The Apache Software License, Version 2.0")
                    "url"("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            "developers" {
                "developer" {
                    "id"("libetl")
                    "name"("LiBe")
                    "url"("https://github.com/libetl")
                }
            }

        }
    }
})