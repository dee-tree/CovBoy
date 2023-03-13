import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.sokolov"
version = "1.0-SNAPSHOT"


val ksmtVersion: String by extra
val kotlinSerializationVersion: String by extra

dependencies {
    // ksmt core
    implementation("com.github.UnitTestBot.ksmt:ksmt-core:$ksmtVersion")
    implementation("com.github.UnitTestBot.ksmt:ksmt-runner:$ksmtVersion")
    // ksmt - z3 solver
    implementation("com.github.UnitTestBot.ksmt:ksmt-z3:$ksmtVersion")
    // ksmt - bitwuzla solver
    implementation("com.github.UnitTestBot.ksmt:ksmt-bitwuzla:$ksmtVersion")
    // ksmt - cvc5 solver
    implementation("com.github.UnitTestBot.ksmt:ksmt-cvc5:$ksmtVersion")
    // ksmt - yices solver
    implementation("com.github.UnitTestBot.ksmt:ksmt-yices:$ksmtVersion")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // serializatio
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$kotlinSerializationVersion")

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
