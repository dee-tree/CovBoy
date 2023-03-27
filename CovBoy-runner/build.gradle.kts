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
    implementation(projects.covBoyCore)

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

val benchmarksDir = project.stringProperty("benchmarksDir")
    ?: (project.projectDir.absolutePath + "/data/benchmarks/formulas")

val coverageDir = project.stringProperty("coverageDir")
    ?: (project.projectDir.absolutePath + "/data/benchmarks/coverage")

val rewriteResults = project.booleanProperty("rewriteResults") ?: false

val solverTimeoutMillis = project.longProperty("solverTimeoutMillis") ?: 1000L

val samplerTimeoutMillis = project.longProperty("samplerTimeoutMillis") ?: 60_000L

val solvers: Array<String> = project.stringProperty("solvers")?.split(',')?.toTypedArray()
    ?: emptyArray()

val primarySolver: String = project.stringProperty("primarySolver") ?: "Z3"

tasks.register<JavaExec>("benchmarks-sampler") {
    group = "run"
    description = "Run the CoverageSampler on *.smt2 SMT formulas on different processes"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.sokolov.covboy.sampler.BenchmarksSamplerRunner")
    val arguments = listOf(
        benchmarksDir,
        coverageDir,
        solverTimeoutMillis.toString(),
        samplerTimeoutMillis.toString(),
        rewriteResults.toString()
    ) + solvers.toList()

    args(arguments)
}

tasks.register<JavaExec>("coverage-compare") {
    group = "run"
    description = "Run the CoverageComparator on each coverage result in group of inputs"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.sokolov.covboy.coverage.PredicatesCoverageComparatorRunner")
    val arguments = listOf(coverageDir, primarySolver)

    args(arguments)
}
