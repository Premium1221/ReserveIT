plugins {
    id("org.springframework.boot") version "3.1.0" // Spring Boot plugin
    id("io.spring.dependency-management") version "1.1.0" // Manages dependencies versions for Spring Boot
    id("java")
}

group = "com.reserveit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral() // Ensures Gradle pulls dependencies from Maven Central
}

dependencies {
    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-web") // For creating REST APIs
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // For JPA support
    implementation("mysql:mysql-connector-java:8.0.34") // MySQL connector

    // Jackson for JSON conversion
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JUnit for testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Spring Boot testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
