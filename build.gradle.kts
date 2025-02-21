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


    test {
        useJUnitPlatform()
    }
}
