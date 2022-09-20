import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21" apply false
}

group = "com.sokolov"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}