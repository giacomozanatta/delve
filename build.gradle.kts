import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Paths

data class Dependency(
    val name: String,
    val repository: String,
    val type: String,
    val branch: String,
    val antlrGeneratedSrcBuildRoot: String,
    val srcRoot: String,
    val targetLanguages: List<String>
)

data class ProjectConfig(
    val lisaRootPkg: String,
    val projectPath: String,
    val dependencies: List<Dependency>
)


plugins {
    id("java")
    id("antlr")
    id("application")
    id("eclipse")
    id("com.diffplug.spotless") version "6.18.0"
    id("maven-publish")
    id("signing")
}

group = "it.unive"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.8-1")

    // utils
    api("commons-io:commons-io:2.14.0")
    api("org.apache.commons:commons-lang3:3.9")
    api("org.apache.commons:commons-text:1.10.0")
    api("org.apache.commons:commons-collections4:4.4")

    // logging
    api("org.apache.logging.log4j:log4j-api:2.17.1")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.17.1")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")

    // lombok
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    testCompileOnly("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    implementation("guru.nidi:graphviz-java:0.18.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.springframework:spring-web:6.1.12")
    implementation("org.jdom:jdom2:2.0.5")
    implementation("com.sun.xml.bind:jaxb-impl:3.0.2")
    implementation("org.thymeleaf:thymeleaf:3.0.15.RELEASE")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("io.github.classgraph:classgraph:4.8.175")

    implementation("commons-cli:commons-cli:1.5.0")

    api("org.graphstream:gs-core:2.0")

    // time handling
    api("joda-time:joda-time:2.10.14")

    // reflective lookup
    api("org.reflections:reflections:0.9.12")

    testImplementation("junit:junit:4.13.1")
    testImplementation("org.thymeleaf:thymeleaf:3.0.15.RELEASE")

    testImplementation("nl.jqno.equalsverifier:equalsverifier:3.6")

}

tasks.generateGrammarSource {
    dependsOn("sourcesJar")
    maxHeapSize = "64m"
    arguments.addAll(listOf("-visitor", "-no-listener"))
    doLast {

        val jsonText = Files.readString(Paths.get("$projectDir/lisa-dependencies.json"))
        val jsonObj = JSONObject(jsonText)

        // Get dependencies array
        val dependencies = jsonObj.getJSONArray("dependencies")

        for (i in 0 until dependencies.length()) {
            val dependency = dependencies.getJSONObject(i)
            val srcRoot = dependency.getString("srcRoot")
            val targetLanguages = dependency.getJSONArray("targetLanguages")

            for (j in 0 until targetLanguages.length()) {
                val targetLanguage = targetLanguages.getString(j)
                val srcDir = file("build/generated-src/antlr/main/")
                val destDir = file("build/generated-src/antlr/main/it/unive/$srcRoot/antlr")

                copy {
                    from(srcDir)
                    include("${targetLanguage}*.java") // Only Java files starting with targetLanguage
                    into(destDir)
                }
            }
        }
        //(project.plugins.getPlugin(GenerateDependenciesGrammarSource::class.java)).exec()
        copy {
            from("build/generated-src/antlr/main/")
            include("Maru*.java")
            into("build/generated-src/antlr/main/it/unive/delve/antlr")
        }
        project.delete(fileTree("build/generated-src/antlr/main") {
            include("*.*")
        })
    }
}

java {
    //withJavadocJar()
    withSourcesJar()
}

tasks.compileJava {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

/*tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}*/

tasks.test {
    useJUnitPlatform()
}