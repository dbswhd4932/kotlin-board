import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
    kotlin("kapt") version "2.0.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

// QueryDSL generated sources를 sourceSets에 추가
sourceSets {
    main {
        kotlin {
            srcDir("src/main/kotlin")
            srcDir("build/generated/source/kapt/main")  // kapt로 생성된 Q 클래스
        }
    }
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlin 관련 의존성
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")  // JSON 직렬화/역직렬화를 위한 Kotlin 모듈
    implementation("org.jetbrains.kotlin:kotlin-reflect")  // Kotlin 리플렉션 (Spring에서 필요)
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")  // Kotlin 표준 라이브러리

    // Kotlin Coroutines (비동기 프로그래밍)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")  // Coroutines 핵심 라이브러리
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")  // Spring WebFlux와 통합
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")  // CompletableFuture 지원

    // QueryDSL (타입 안전한 동적 쿼리 빌더)
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")

    // Swagger (SpringDoc OpenAPI) - API 문서화
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Database Drivers
    runtimeOnly("com.h2database:h2")  // H2 Database (개발용)
    runtimeOnly("com.mysql:mysql-connector-j")  // MySQL Driver (프로덕션용)

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.8")  // Kotlin 전용 Mocking 라이브러리
    testImplementation("com.ninja-squad:springmockk:4.0.2")  // Spring + MockK 통합

    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

// kapt에 Java 컴파일러 모듈 접근 권한 부여 (Java 9+ 모듈 시스템 대응)
tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xjvm-default=all",
            "-Xjsr305=strict"
        )
    }
}

kapt {
    arguments {
        arg("--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
        arg("--add-exports", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
        arg("--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"  // Java의 @Nullable/@Nonnull 어노테이션 지원
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
