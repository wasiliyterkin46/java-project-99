import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    checkstyle
    jacoco
    id("java")
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.14.2"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("org.sonarqube") version "6.3.1.5724"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

application {
    mainClass.set("hexlet.code.spring.AppApplication")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    //Библиотеки для авторизации
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    testImplementation("org.springframework.security:spring-security-test")

    // Для удобной работы с экземплярами моделей при тестировании
    implementation("org.instancio:instancio-junit:5.5.0")
    implementation("net.datafaker:datafaker:2.5.0")

    // Бибилиотеки баз данных
    // h2
    runtimeOnly("com.h2database:h2:2.3.232")
    //postgresql
    runtimeOnly("org.postgresql:postgresql:42.7.7")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")

    // Библиотека для автоматической конвертации между DTO и сущностями
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    /*Библиотека для реализации возможности частичного обновления (разрешение конфликта,когда переданы
    * значения не всех ожидаемых полей и при этом поле может быть явно передано,
    * но иметь значение = null)
    * */
    implementation("org.openapitools:jackson-databind-nullable:0.2.7")

    compileOnly("org.projectlombok:lombok:1.18.40")
    annotationProcessor("org.projectlombok:lombok:1.18.40")

    // TEST
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Для работы с аутентификацией в тестах
    testImplementation("org.springframework.security:spring-security-test")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Библиотека для проверки содержания ответа в тестах
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.1.0")
}

tasks.jacocoTestReport { reports { xml.required.set(true) } }

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        showStandardStreams = true
    }
}

sonar {
    properties {
        property("sonar.projectKey", "wasiliyterkin46_java-project-99")
        property("sonar.organization", "wasiliyterkin46")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
