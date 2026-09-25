plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	id("org.sonarqube") version "7.5.0.8588"
	kotlin("kapt") version "2.4.10" // для генерации
}

group = "io.hexlet"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")

	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.springframework.boot:spring-boot-h2console")

	// для работы с JPA (Hibernate) и репозиториями
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("net.datafaker:datafaker:2.7.0")

	// MapStruct
	implementation("org.mapstruct:mapstruct:1.6.3")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

	//jackson-databind-nullable
	implementation("org.openapitools:jackson-databind-nullable:0.2.11")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	// Дает MockMvc и аннотацию @AutoConfigureMockMvc
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	// Для работы с аутентификацией
	testImplementation("org.springframework.security:spring-security-test")
	// Для проверки тела ответа
	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:6.2.0")
	// Instancio
	testImplementation("org.instancio:instancio-junit:5.6.0")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

tasks.check {
	dependsOn(tasks.jacocoTestReport)
}

sonar {
	properties {
		property("sonar.projectKey", "AMOrlovSev_hexlet-spring-boot")
		property("sonar.organization", "amorlovsev")
	}
}