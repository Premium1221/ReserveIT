plugins {
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("java")
    // Keep recent SonarQube Gradle plugin; use the 'sonar' task (not deprecated 'sonarqube')
    id("org.sonarqube") version "5.1.0.4882"
    id("jacoco")
}

group = "com.reserveit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Lombok
    implementation("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // MySQL Driver
    implementation("com.mysql:mysql-connector-j:8.0.33")

    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Validation
    implementation("jakarta.validation:jakarta.validation-api:3.0.0")
    implementation("org.hibernate.validator:hibernate-validator:7.0.0.Final")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")


    // JWT Dependencies
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Spring Boot Mail Starter
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // WebSocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.webjars:webjars-locator-core")
    implementation("org.webjars:sockjs-client:1.0.2")
    implementation("org.webjars:stomp-websocket:2.3.3")

    // H2 (for local dev profile)
    runtimeOnly("com.h2database:h2")


}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    mainClass.set("com.reserveit.MainApplication")
    archiveBaseName.set("app")
    archiveVersion.set("")
}

tasks.named<Jar>("jar") {
    enabled = false
}

jacoco {
    toolVersion = "0.8.10"
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true) // Ensure XML is generated
        html.required.set(true)
        csv.required.set(false) // Disable CSV to reduce clutte
    }
    dependsOn(tasks.test)
}

// Ensure coverage is generated before analysis (both task names)
tasks.named("sonar") {
    dependsOn(tasks.jacocoTestReport)
}
// Re-add legacy 'sonarqube' task wiring for local workflows using the old task name
tasks.named("sonarqube") {
    dependsOn(tasks.jacocoTestReport)
}

sonarqube {
    properties {
        // Project identity (key + display name)
        property("sonar.projectKey", "reserveIT2.0")
        property("sonar.projectName", "ReserveIT")
        // Hardcoded local SonarQube server URL and token, per your request
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.token", "sqp_835bf2823b0f645cfdc5f2c0e8f9929b590ef714")

        property("sonar.java.binaries", "build/classes/java/main")
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.qualitygate.wait", true)
        property("sonar.exclusions", listOf(
            "**/MainApplication.java",
            "**/model/**",
            "**/config/**",
            "**/dto/**"
        ).joinToString(","))
    }
}
