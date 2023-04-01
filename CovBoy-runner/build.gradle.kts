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
val argParserVersion: String by extra

dependencies {
    implementation(projects.covBoyCore)

    implementation("com.github.UnitTestBot.ksmt:ksmt-core:$ksmtVersion")
    implementation("com.github.UnitTestBot.ksmt:ksmt-runner:$ksmtVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    implementation("com.xenomachina:kotlin-argparser:$argParserVersion")

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

fun Project.stringProperty(name: String): String? =
    if (project.hasProperty(name))
        project.property(name).toString()
    else null

fun Project.booleanProperty(name: String): Boolean? =
    if (project.hasProperty(name))
        project.property(name)?.toString()?.toBoolean()
    else null

fun Project.longProperty(name: String): Long? =
    if (project.hasProperty(name))
        project.property(name)?.toString()?.toLong()
    else null

// :benchmarks-sampler
val benchmarksDir = project.stringProperty("benchmarksDir")
    ?: (project.projectDir.absolutePath + "/data/benchmarks/formulas")
// ---

// :coverage-compare
val primarySolver: String = project.stringProperty("primarySolver") ?: "Z3"
// ---

// :coverage-sampler args
val benchmarkFile = project.stringProperty("benchmarkFile") ?: ""
// ---

// common properties:
val coverageDir = project.stringProperty("coverageDir")
    ?: (project.projectDir.absolutePath + "/data/benchmarks/coverage")
val coverageFile = project.stringProperty("coverageFile") ?: ""
val coverageSamplerType: String = project.stringProperty("samplerType") ?: "baseline"
val samplerParams: String = project.stringProperty("samplerParams") ?: ""
// ---

tasks.register<JavaExec>("benchmarks-sampler") {
    group = "run"
    description = "Run the CoverageSampler on *.smt2 SMT formulas on different processes"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.sokolov.covboy.sampler.benchmarks.BenchmarksSamplerRunner")

    args(
        listOf(
            "--benchmarks=$benchmarksDir",
            "--out=$coverageDir",
            "--$coverageSamplerType",
        ) + samplerParams.split(",")
    )
}

tasks.register<JavaExec>("coverage-sampler") {
    group = "run"
    description = "Run the CoverageSampler on specified file and solver"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.sokolov.covboy.sampler.main.SamplerMain")

    args(
        listOf(
            "--in=$benchmarkFile",
            "--out=${
                coverageFile.ifBlank {
                    File(benchmarkFile).also { benchFile ->
                        File(benchFile.parentFile, benchFile.nameWithoutExtension + ".cov")
                    }
                }
            }",
            "--$coverageSamplerType",
        ) + samplerParams.split(",")
    )
}

tasks.register<JavaExec>("coverage-info") {
    group = "run"
    description = "Get info of serialized formula coverage"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.sokolov.covboy.coverage.CoverageInfoPrinter")

    args(coverageFile)
}



tasks.register<JavaExec>("coverage-compare") {
    group = "run"
    description = "Run the CoverageComparator on each coverage result in group of inputs"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.sokolov.covboy.coverage.PredicatesCoverageComparatorRunner")
    val arguments = listOf(coverageDir, primarySolver)

    args(arguments)
}
