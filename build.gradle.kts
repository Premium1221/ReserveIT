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

springBoot {
    mainClass.set("com.reserveit.MainApplication")
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // HSQLDB In-Memory Database
    implementation("org.hsqldb:hsqldb:2.6.1")
    implementation("com.h2database:h2")

    // AWS S3 SDK
    implementation("software.amazon.awssdk:s3:2.20.0")

    // Add Jakarta Validation API and Hibernate Validator
    implementation("jakarta.validation:jakarta.validation-api:3.0.0")
    implementation("org.hibernate.validator:hibernate-validator:7.0.0.Final")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.test {
    useJUnitPlatform()
}
