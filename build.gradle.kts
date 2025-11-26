plugins {
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
}

group = "org.example"
version = "0.0.1-SNAPSHOT"
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")

    // MySQL connector
    runtimeOnly("com.mysql:mysql-connector-j:8.4.0")

    // Testes
    testImplementation("com.h2database:h2") // simula um banco de dados na memória
    testImplementation("org.springframework.boot:spring-boot-starter-test") // inclui JUnit e Hamcrest
    // ter que usar spring-boot-starter-test por causa do Spring Boot q tem problemas de compatibilidade
}

tasks.test {
    useJUnitPlatform()
}