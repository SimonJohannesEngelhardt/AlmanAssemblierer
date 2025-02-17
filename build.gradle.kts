plugins {
    java
    application
}

application {
    mainClass.set("com.engelhardt.simon.Main")
}

group = "com.engelhardt.simon"
version = "v0.1"


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.antlr:antlr4:4.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:3.+")
}

val ENABLE_PREVIEW = "--enable-preview"

tasks {
    val generateGrammarSource by registering(JavaExec::class) {
        group = "build"
        description = "Generate ANTLR source files from grammar"
        mainClass.set("org.antlr.v4.Tool")
        classpath = configurations["runtimeClasspath"]
        args = listOf(
            "-visitor",
            "-o",
            "src/main/java/com/engelhardt/simon/antlr",
            "src/main/resources/alman.g4",
            "-Xexact-output-dir"
        )
    }

    compileJava {
        options.compilerArgs.add(ENABLE_PREVIEW)

        options.compilerArgs.add("-Xlint:none")
        options.compilerArgs.add("-nowarn")

        options.release.set(22)
    }

    test {
        useJUnitPlatform()
        jvmArgs(ENABLE_PREVIEW)
    }
    javadoc {
        exclude("com/engelhardt/simon/antlr/**")
    }

}
tasks.withType<JavaCompile>().all {
    options.compilerArgs.add(ENABLE_PREVIEW)
}
tasks.named<JavaExec>("run") {
    jvmArgs(ENABLE_PREVIEW)
}
tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addBooleanOption("-enable-preview", true)
        source = "22"
    }
}
