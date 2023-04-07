import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
//    kotlin("kapt")
    id("me.champeau.jmh")
}

group = "com.sokolov"
version = "1.0-SNAPSHOT"

val ksmtVersion: String by extra
val coroutinesVersion: String by extra
val serializationVersion: String by extra
val slf4jVersion: String by extra
val logbackVersion: String by extra
val jupiterParamsVersion: String by extra
val jmhVersion: String by extra


dependencies {

    implementation(projects.covBoyCore)

    // ksmt core
    implementation("com.github.UnitTestBot.ksmt:ksmt-core:$ksmtVersion")
    implementation("com.github.UnitTestBot.ksmt:ksmt-runner:$ksmtVersion")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    jmhImplementation("org.openjdk.jmh:jmh-core:$jmhVersion")
//    kaptJmh("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")

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

/*java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}*/
