plugins {
    kotlin("jvm") version "1.6.21" apply false
    kotlin("plugin.serialization") version "1.6.21" apply false
}

group = "com.sokolov"
version = "1.0-SNAPSHOT"

val ksmtVersion: String by project
val kotlinSerializationVersion: String by project

extra { ksmtVersion }
extra { kotlinSerializationVersion }

allprojects {
    repositories {
        mavenCentral()

        // ksmt
        maven { url = uri("https://jitpack.io") }
    }
}