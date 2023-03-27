plugins {
    kotlin("jvm") version "1.6.21" apply false
    kotlin("plugin.serialization") version "1.6.21" apply false
    kotlin("kapt") version "1.6.21" apply false
    id("me.champeau.jmh") version "0.7.0" apply false
}

group = "com.sokolov"
version = "1.0-SNAPSHOT"

val ksmtVersion: String by project
val coroutinesVersion: String by project
val logbackVersion: String by project
val slf4jVersion: String by project
val jupiterParamsVersion: String by project
val jmhVersion: String by project

extra { ksmtVersion }
extra { coroutinesVersion }
extra { logbackVersion }
extra { slf4jVersion }
extra { jupiterParamsVersion }
extra { jmhVersion }

allprojects {
    repositories {
        mavenCentral()

        // ksmt
        maven { url = uri("https://jitpack.io") }
    }
}