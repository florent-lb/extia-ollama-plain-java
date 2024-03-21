plugins {
    java
    id("io.freefair.lombok") version "8.6"
}

group = "com.extia.flb.lc4j"
version = "1.0-SNAPSHOT"

val langChainVersion = "0.28.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {

    implementation("dev.langchain4j:langchain4j-ollama:${langChainVersion}")
    implementation("dev.langchain4j:langchain4j-open-ai:${langChainVersion}")
    implementation("dev.langchain4j:langchain4j:${langChainVersion}")
    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.15.1")

    //Used by langchain4j
    implementation("org.tinylog:tinylog-impl:2.6.2")
    implementation("org.tinylog:slf4j-tinylog:2.6.2")


    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
