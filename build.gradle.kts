plugins {
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.3"
    id("java")
}

group = "org.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")

    // MySQL connector J
    implementation("com.mysql:mysql-connector-j:9.5.0")

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")

    // Hamcrest
    testImplementation("org.hamcrest:hamcrest:2.2")
}

tasks.test {
    useJUnitPlatform()
}
