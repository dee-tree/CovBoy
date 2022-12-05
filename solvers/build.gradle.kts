plugins {
    kotlin("jvm")
}

group = "com.sokolov"
version = "1.0-SNAPSHOT"

repositories {
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
val javasmtYices2Version = "3.12.0"
val z3Version = "4.10.1"
val smtInterpolVersion = "2.5-916-ga5843d8b"
val boolectorVersion = "3.2.2-g1a89c229"
val cvc4Version = "1.8-prerelease-2020-06-24-g7825d8f28"
val mathsat5Version = "5.6.6-sosy1" //"5.6.8"
val optiMathsat5Version = "1.7.1-sosy0"
val yices2Version = "2.6.2-396-g194350c1"
val princessVersion = "2021-11-15"

dependencies {
    // java SMT
    implementation("org.sosy-lab:java-smt:$javasmtVersion")

    // Z3
    runtimeOnly("org.sosy-lab:javasmt-solver-z3:$z3Version:com.microsoft.z3@jar")
    runtimeOnly("org.sosy-lab:javasmt-solver-z3:$z3Version:libz3@so")
    runtimeOnly("org.sosy-lab:javasmt-solver-z3:$z3Version:libz3java@so")

    // Retrieve CVC4 via Maven
    runtimeOnly("org.sosy-lab:javasmt-solver-cvc4:$cvc4Version:CVC4@jar")
    runtimeOnly("org.sosy-lab:javasmt-solver-cvc4:$cvc4Version:libcvc4@so")
    runtimeOnly("org.sosy-lab:javasmt-solver-cvc4:$cvc4Version:libcvc4jni@so")
    runtimeOnly("org.sosy-lab:javasmt-solver-cvc4:$cvc4Version:libcvc4parser@so")

    // Retrieve Boolector via Maven
    runtimeOnly("org.sosy-lab:javasmt-solver-boolector:$boolectorVersion:libboolector@so")
    runtimeOnly("org.sosy-lab:javasmt-solver-boolector:$boolectorVersion:libminisat@so")
    runtimeOnly("org.sosy-lab:javasmt-solver-boolector:$boolectorVersion:libpicosat@so")

    // mathsat
    runtimeOnly("org.sosy-lab:javasmt-solver-mathsat5:$mathsat5Version:libmathsat5j@so")
    runtimeOnly("org.sosy_lab:javasmt-solver-optimathsat:$optiMathsat5Version:liboptimathsat5j@so")
    // Retrieve Princess
    runtimeOnly("io.github.uuverifiers:princess_2.13:$princessVersion@jar")

    // Retrieve SMTInterpol
    runtimeOnly("de.uni-freiburg.informatik.ultimate:smtinterpol:$smtInterpolVersion@jar")

    // Example as to how to use Yices2
    // First get JavaSMT for Yices2 from Maven (if you want to use only Yices2 use this dependency in the "implementation" part above instead of regual JavaSMT)
    runtimeOnly("org.sosy-lab:javasmt-yices2:$javasmtYices2Version@jar")
    // And the Yices2 solver from Maven
    runtimeOnly("org.sosy-lab:javasmt-solver-yices2:$yices2Version:libyices2j@so")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // logger
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("ch.qos.logback:logback-core:1.2.11")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation(fileTree("dir" to "build/dependencies", "include" to "*.jar"))

    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
}


// Use a config to identify JavaSMT components
configurations {
    register("javaSMTConfig").configure {
        dependencies.addAll(runtimeOnly.get().dependencies.filter { it.group == "org.sosy-lab" })
        dependencies.addAll(implementation.get().dependencies.filter { it.group == "org.sosy-lab" })
    }
}

// JavaSMT needs the solver dependencies in a particular folder structure; in the same folder as JavaSMT is the easiest.
// Also we need to rename some solver dependencies.
// Clean, then copy all JavaSMT components into the build/dependencies folder, rename and use it from there
tasks.register<Copy>("copyDependencies") {
    dependsOn("cleanDownloadedDependencies")
    from(configurations["javaSMTConfig"])
    into("build/dependencies")
    rename(".*(lib[^-]*)-?.*.so", "\$1.so")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    dependsOn("copyDependencies")
}

// Cleanup task for the JavaSMT components/dependencies
tasks.register<Delete>("cleanDownloadedDependencies") {
    delete(file("build/dependencies"))
}

// Use the cleanup task for JavaSMT declared above when cleaning
tasks.clean {
    dependsOn("cleanDownloadedDependencies")

    delete(file("out"))
}