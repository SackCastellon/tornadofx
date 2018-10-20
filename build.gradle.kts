import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val json_version: String by project
val httpclient_version: String by project
val osgi_version: String by project
val junit_version: String by project
val testfx_version: String by project
val fontawesomefx_version: String by project

plugins {
    java
    osgi
    `maven-publish`
    signing
    kotlin("jvm") version "1.3.0-rc-190"
    id("org.jetbrains.dokka") version "0.9.17"
}

group = "no.tornado"
version = "1.7.18-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))

    compile("org.glassfish:javax.json:$json_version")
    compileOnly("org.apache.httpcomponents:httpclient:$httpclient_version")
    compileOnly("org.apache.felix:org.apache.felix.framework:$osgi_version")
    testCompile("junit:junit:$junit_version")
    testCompile(kotlin("test-junit"))
    testCompile("org.testfx:testfx-junit:$testfx_version")
    testCompile("de.jensd:fontawesomefx:$fontawesomefx_version")
}

tasks.withType<Jar> {
    manifest.attributes(
        mapOf(
            "Bundle-Activator" to "tornadofx.osgi.impl.Activator",
            "Bundle-SymbolicName" to "no.tornado.tornadofx"
        )
    )
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("tornadofx") {
            pom {
                packaging = "jar"
                name.set("TornadoFX")
                description.set("Lightweight JavaFX Framework for Kotlin")
                url.set("https://github.com/edvin/tornadofx")

                organization {
                    name.set("SYSE")
                    url.set("https://www.syse.no/")
                }

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        name.set("Edvin Syse")
                        email.set("es@syse.no")
                        organization.set("SYSE AS")
                        organizationUrl.set("https://www.syse.no")
                    }
                    developer {
                        name.set("Thomas Nield")
                        email.set("thomasnield@ live.com")
                        organization.set("Southwest Airlines")
                        organizationUrl.set("https://www.southwest.com/")
                    }
                    developer {
                        name.set("Matthew Turnblom")
                        email.set("uberawesomeemailaddressofdoom@gmail.com")
                        organization.set("Xactware")
                        organizationUrl.set("https://www.xactware.com/")
                    }
                }

                scm {
                    connection.set("scm:git:git@github.com:edvin/tornadofx.git")
                    developerConnection.set("scm:git:git@github.com:edvin/tornadofx.git")
                    url.set("git@ github.com:edvin/tornadofx.git")
                }
            }
        }
    }
}
