import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.21"
}

group = "com.sokolov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    // ksmt
    maven { url = uri("https://jitpack.io") }
}

val junit4Version = "4.13"
val ksmtVersion = "0.3.0"

dependencies {
    // ksmt core
    implementation("com.github.UnitTestBot.ksmt:ksmt-core:$ksmtVersion")
    // ksmt - z3 solver
    implementation("com.github.UnitTestBot.ksmt:ksmt-z3:$ksmtVersion")
    // ksmt - bitwuzla solver
    implementation("com.github.UnitTestBot.ksmt:ksmt-bitwuzla:$ksmtVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    // logger
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("ch.qos.logback:logback-core:1.2.11")

    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

// Use the cleanup task for JavaSMT declared above when cleaning
tasks.clean {
    delete(file("out"))
}