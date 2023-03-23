import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "com.sokolov"
version = "1.0-SNAPSHOT"

val ksmtVersion: String by extra
val coroutinesVersion: String by extra
val slf4jVersion: String by extra
val logbackVersion: String by extra
val jupiterParamsVersion: String by extra

dependencies {
    implementation(project(":CovBoy-core"))

    implementation("com.github.UnitTestBot.ksmt:ksmt-core:$ksmtVersion")
    implementation("com.github.UnitTestBot.ksmt:ksmt-runner:$ksmtVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // logger
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("ch.qos.logback:logback-core:$logbackVersion")

    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterParamsVersion")
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
