plugins {
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("java")
}

group = "com.reserveit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Jackson for JSON conversion
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JUnit for testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Spring Boot testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }
}

tasks.test {
    useJUnitPlatform()
}
