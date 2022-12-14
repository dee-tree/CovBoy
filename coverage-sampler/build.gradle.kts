import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.21"
}

group = "com.sokolov"
version = "1.0-SNAPSHOT"

//repositories {
//    mavenCentral()
//}

repositories {
    mavenLocal()
    // Use MavenCentral as a source, but try using POMs first and if that fails just use the artifact
    mavenCentral {
        metadataSources {
            mavenPom()
        }
    }
    mavenCentral {
        metadataSources {
            artifact()
        }
    }

    // Ivy can be used as an alternative to MavenCentral
    ivy {
        url = uri("https://www.sosy-lab.org/ivy")
        patternLayout {
            artifact("/[organisation]/[module]/[classifier]-[revision].[ext]")
        }
        metadataSources {
            artifact()
        }
    }
}


val javasmtVersion = "3.14.0"
val junit4Version = "4.13"

dependencies {
    implementation(project(":solvers"))

//    implementation(fileTree("dir" to "../solvers/build/dependencies", "include" to "*.jar"))
    // java SMT
//    implementation("org.sosy-lab:java-smt:$javasmtVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    // logger
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("ch.qos.logback:logback-core:1.2.11")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
}

val outputsRootDir = File(projectDir, "out/coverage_result")

tasks.register<JavaExec>("compareCoverage") {
    mainClass.set("com.sokolov.covboy.run.CoverageComparator")

    val outRootDir = outputsRootDir
    val baseSolver = "Z3"

    classpath(sourceSets["main"].runtimeClasspath)

    args(outRootDir.absolutePath, baseSolver)
}

tasks.withType<Test> {
    setForkEvery(1)
    maxParallelForks = 5
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