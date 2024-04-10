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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}
