import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.net.URI

group = "org.toile-libre.libe"
version = "2.0.7"

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("io.codearte.nexus-staging") version "0.30.0"
    id("org.jetbrains.dokka") version "1.7.10"
    jacoco
    application
    idea
    `java-library`
    `maven-publish`
    signing
}

application {
    mainClass.set("org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider")
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin"))
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4+")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:11.0.0")
    }
}

apply {
    plugin("java-library")
    plugin("maven-publish")
    plugin("kotlin")
    plugin("jacoco")
    plugin("org.jlleitschuh.gradle.ktlint")
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
    compileOnly("org.jetbrains.intellij.deps:trove4j:1.0.20200330")
    compileOnly("com.pinterest:ktlint:0.43.2")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect")
    api("com.pinterest.ktlint:ktlint-core:0.43.2")
    testImplementation("org.junit.platform:junit-platform-commons:1.9.0")
    testImplementation("org.junit.platform:junit-platform-engine:1.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("com.pinterest.ktlint:ktlint-test:0.43.2")
    testImplementation("com.winterbe:expekt:0.5.0")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    // you can create a ktlint run config with that
    compileOnly("com.pinterest:ktlint:0.43.2")
    compileOnly("org.jetbrains.intellij.deps:trove4j:1.0.20200330")
}

nexusStaging {
    packageGroup = "org.toile-libre.libe"
    stagingProfileId = "3ebf87dd30af1"
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
    version.set("0.43.2")
    debug.set(false)
    verbose.set(false)
    android.set(false)
    outputToConsole.set(true)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
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
    reportsDirectory.dir("$buildDir/customJacocoReportDir")
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
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
val sources by tasks.registering(Jar::class) {
    archiveBaseName.set(project.name)
    archiveClassifier.set("sources")
    archiveVersion.set(null as String?)
    from(sourceSets.main.get().allSource)
}
val javadocSources by tasks.registering(Jar::class) {
    archiveBaseName.set(project.name)
    archiveClassifier.set("javadoc")
    archiveVersion.set(null as String?)
    from(tasks["dokkaJavadoc"])
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            artifact(sources)
            artifact(javadocSources)
            pom.name.set("domaindrivendesign-ktlint-rules")
            pom.description.set("Domain Driven Design ktlint rules")
            pom.url.set("https://github.com/libetl/domaindrivendesign-ktlint-rules")
            pom.inceptionYear.set("2019")

            pom.scm {
                connection.set("scm:git@github.com:libetl/domaindrivendesign-ktlint-rules.git")
                developerConnection.set("scm:git@github.com:libetl/domaindrivendesign-ktlint-rules.git")
                url.set("scm:git@github.com:libetl/domaindrivendesign-ktlint-rules.git")
            }

            pom.licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            pom.developers {
                developer {
                    id.set("libetl")
                    name.set("LiBe")
                    url.set("https://github.com/libetl")
                }
            }
        }
    }
    repositories {
        maven {
            url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = "libetl"
                password = project.properties["signing.password"] as String
            }
        }
    }
}

signing {
    sign(publishing.publications["library"])
}
