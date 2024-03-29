import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.net.URI

group = "org.toile-libre.libe"
version = "3.0.3"

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-1"
    id("io.codearte.nexus-staging") version "0.30.0"
    id("org.jetbrains.dokka") version "1.8.20"
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
        classpath("org.jlleitschuh.gradle:ktlint-gradle:11.5.1")
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
    compileOnly("com.pinterest.ktlint:ktlint-cli:1.0.0")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
    api("com.pinterest.ktlint:ktlint-cli-ruleset-core:1.0.0")
    api("com.pinterest.ktlint:ktlint-rule-engine-core:1.0.0")
    testRuntimeOnly("org.slf4j:slf4j-api:2.0.9")
    testImplementation("org.junit.platform:junit-platform-commons:1.10.0")
    testImplementation("org.junit.platform:junit-platform-engine:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("com.pinterest.ktlint:ktlint-test:1.0.0")
    testImplementation("com.pinterest.ktlint:ktlint-rule-engine:1.0.0")
    testImplementation("com.winterbe:expekt:0.5.0")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    kotlinOptions.jvmTarget = "17"
}

dependencies {
    // you can create a ktlint run config with that
    compileOnly("com.pinterest.ktlint:ktlint-cli:1.0.0")
    compileOnly("org.jetbrains.intellij.deps:trove4j:1.0.20200330")
}

nexusStaging {
    packageGroup = "org.toile-libre.libe"
    stagingProfileId = "3ebf87dd30af1"
}

configure<IdeaModel> {
    project {
        languageLevel = IdeaLanguageLevel(JavaVersion.VERSION_17)
    }
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

ktlint {
    version.set("1.0.0")
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
    toolVersion = "0.8.10"
    reportsDirectory.dir("${project.getLayout().getBuildDirectory()}/customJacocoReportDir")
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
    jvmTarget = "17"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
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
                password = project.properties["signing.password"] as? String
            }
        }
    }
}

signing {
    sign(publishing.publications["library"])
}
