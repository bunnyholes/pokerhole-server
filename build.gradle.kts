plugins {
    java
    id("org.springframework.boot") version "4.0.0-M3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "bunnyholes"
version = "1.0.0"
description = "포커 게임 구현 프로젝트"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot 4.x Core
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Virtual Threads (Java 21)
    // 별도 설정 불필요, Spring Boot 4.x에서 자동 지원
    
    // Database
    runtimeOnly("org.postgresql:postgresql:42.7.4")
    runtimeOnly("com.h2database:h2:2.3.232")

    // Docker Compose Support (Spring Boot 3.1+)
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // Flyway (Database Migration)
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    
    // Redis (캐싱 + 세션) - Optional, can be enabled when Redis is available
    // implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // implementation("org.springframework.session:spring-session-data-redis")
    
    // Lombok (보일러플레이트 제거)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    
    // MapStruct (매핑 자동화)
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    
    // Validation
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    
    // Terminal UI (JLine) - removed as client now handles all UI
    // SSH Server - removed, using WebSocket instead
    
    // Scheduling (AI 투입, 자동 충전)
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    
    // Problem Details (RFC 7807 에러 응답)
    implementation("org.zalando:problem-spring-web:0.29.1")
    
    // OpenAPI 3.1 (문서화)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    
    // Micrometer (메트릭)
    implementation("io.micrometer:micrometer-registry-prometheus")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.rest-assured:rest-assured:5.5.0")
    testImplementation("org.testcontainers:testcontainers:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.awaitility:awaitility:4.2.2")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
    
    // Mockito + AssertJ (최신 테스트 도구)
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("org.assertj:assertj-core:3.27.0")
    
    // Mutation Testing (테스트 품질 검증)
    testImplementation("org.pitest:pitest-junit5-plugin:1.2.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
