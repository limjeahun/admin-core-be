# admin-core-be

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.7-6DB33F?logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.5-02303A?logo=gradle&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-11.4-003545?logo=mariadb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.4-DC382D?logo=redis&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-DDD_+_Hexagonal-6E40C9)

관리자 로그인부터 사용자·역할·메뉴 권한과 감사 이력까지 관리하는 독립형 백엔드 API다.
ID·비밀번호와 OTP 2단계 인증, JWT·Redis 기반 인증 상태를 제공하며 DDD·헥사고날 아키텍처로 업무 규칙과 외부
기술을 분리한다.

## 주요 기능

- ID·비밀번호 인증과 TOTP 기반 OTP 등록·검증
- JWT Access Token과 HttpOnly Refresh Token Cookie
- Redis 기반 Pre-Auth 상태, OTP 실패 횟수와 Refresh Token Hash 관리
- 관리자 사용자 등록·조회·수정과 비밀번호 초기화
- 역할별 메뉴 조회·편집 권한 관리
- 현재 사용자가 접근할 수 있는 메뉴 조회
- 로그인·OTP 성공/실패 감사 이력
- 사용자·로그인 이력·파일 이력 Excel 다운로드
- 파일 다운로드 성공/실패 감사 이력

이 프로젝트는 관리자 Core 기능만 담당한다. 가맹점·결제 서비스 연동, Kafka, 민감정보 암호화 키 관리와 운영 환경용
Docker Compose는 포함하지 않는다.

## 프로젝트 상세 스펙

| 구분 | 기술과 버전 | 적용 내용 |
|---|---|---|
| Language | Java 21 | Gradle Java Toolchain 21 |
| Framework | Spring Boot 3.3.7 | Spring MVC 기반 REST API |
| Build | Gradle 8.5, Kotlin DSL | 프로젝트에 포함된 Gradle Wrapper 사용 |
| Security | Spring Security | Stateless Filter Chain, URL·메뉴 권한 인가 |
| Token | JJWT 0.12.6 | Access, Refresh, Pre-Auth JWT |
| Password | BCrypt | 비밀번호 원문을 저장하지 않고 Hash 검증 |
| OTP | TOTP, ZXing 3.5.3 | 인증 앱 등록용 QR 생성과 6자리 OTP 검증 |
| Persistence | Spring Data JPA, QueryDSL 5.1.0 | MariaDB Aggregate 영속화와 조회 |
| Database | MariaDB 11.4 | 사용자, 역할, 메뉴 권한과 감사 이력 |
| Redis | Redis 7.4, Spring Data Redis | Repository CRUD와 원자 연산용 Repository Fragment |
| Excel | Apache POI 5.4.0 | 사용자·이력 Excel 다운로드 |
| API 문서 | Springdoc OpenAPI 2.3.0 | Swagger UI와 OpenAPI JSON |
| Monitoring | Spring Boot Actuator | Health, Info, Metrics, Prometheus |
| Test | JUnit 5, Spring Boot Test | 단위 테스트와 아키텍처 규칙 검사 |
| Architecture | DDD, Hexagonal Architecture | Domain 중심 의존성과 Port/Adapter 분리 |
| Local Infra | Docker Compose | MariaDB와 Redis만 컨테이너 실행 |

## 문서

처음 프로젝트를 실행할 때는 로컬 실행 가이드를 먼저 보고, 기능을 개발하기 전에는 아키텍처와 인증·인가 가이드를
확인한다.

| 순서 | 문서 | 내용 | 읽는 시점 |
|---:|---|---|---|
| 1 | [로컬 실행 가이드](docs/guide/로컬_실행_가이드.md) | Windows·macOS 필수 프로그램 설치, Docker MariaDB·Redis, `bootRun`, 문제 해결 | 프로젝트를 처음 실행할 때 |
| 2 | [아키텍처 가이드](docs/architecture/아키텍처_가이드.md) | DDD·헥사고날 계층, DTO 변환, Aggregate, Port, Service, Transaction과 개발 규칙 | 코드를 작성하거나 리뷰하기 전 |
| 3 | [API 정의서](docs/api/API_정의서.md) | 전체 Endpoint, Request/Response, 오류, Cookie·CSRF와 호출 예시 | API를 구현하거나 연동할 때 |
| 4 | [인증·인가 가이드](docs/security/인증_인가_가이드.md) | 로그인·OTP·JWT·Redis·CORS 흐름과 신규 API의 `SecurityConfig` 등록 규칙 | 인증 또는 보호 API를 개발할 때 |
| 5 | [DB 정의서](docs/db/DB_정의서.md) | 로컬 접속 정보, ERD, 테이블 정의, DDL과 초기 로그인 데이터 | DB 구조와 초기 계정이 필요할 때 |

## 빠른 시작

### 1. 필수 프로그램

- JDK 21
- Docker Desktop
- Git은 저장소를 새로 내려받을 때만 필요

Gradle, MariaDB와 Redis는 호스트에 별도로 설치하지 않는다. Gradle은 Wrapper를 사용하고 MariaDB와 Redis는 Docker
Compose로 실행한다. 운영체제별 설치 방법은 [로컬 실행 가이드](docs/guide/로컬_실행_가이드.md)를 따른다.

### 2. 프로젝트 디렉터리 이동

Windows PowerShell:

```powershell
cd D:/workspace/admin-core-be
```

macOS:

```bash
cd ~/workspace/admin-core-be
```

### 3. MariaDB와 Redis 실행

Docker Desktop을 실행한 후 프로젝트 루트에서 다음 명령을 사용한다.

```bash
docker compose -f infra/local/docker-compose.yml up -d
docker compose -f infra/local/docker-compose.yml ps
```

`admin-core-mariadb`와 `admin-core-redis`가 모두 `healthy`인지 확인한다. 이 Compose 파일은 DB와 Redis만 실행하며
Spring Boot 애플리케이션은 실행하지 않는다.

| 서비스 | 로컬 접속값 |
|---|---|
| MariaDB | `localhost:13308/admin_core` |
| MariaDB 계정 | `admin_core / admin_core` |
| Redis | `localhost:16380` |

Docker 명령별 의미와 Health 확인 방법은
[로컬 실행 가이드의 컨테이너 시작](docs/guide/로컬_실행_가이드.md#62-컨테이너-시작)을 참고한다.

### 4. 테스트

Windows:

```powershell
.\gradlew.bat clean test
```

macOS:

```bash
./gradlew clean test
```

테스트 보고서는 `build/reports/tests/test/index.html`에 생성된다.

### 5. Spring Boot 실행

애플리케이션은 Docker가 아니라 호스트의 JDK 21에서 `bootRun`한다.

Windows:

```powershell
$env:SPRING_PROFILES_ACTIVE='local'
$env:JWT_SECRET='replace-with-local-secret-at-least-32-characters'
$env:ADMIN_FRONTEND_ORIGIN='http://localhost:13010'
$env:SPRING_MAIL_HOST='your-smtp-host'
$env:SPRING_MAIL_PORT='your-smtp-port'
$env:SPRING_MAIL_USERNAME='your-smtp-account'
$env:SPRING_MAIL_PASSWORD='your-smtp-password'
.\gradlew.bat bootRun
```

macOS:

```bash
export SPRING_PROFILES_ACTIVE=local
export JWT_SECRET='replace-with-local-secret-at-least-32-characters'
export ADMIN_FRONTEND_ORIGIN='http://localhost:13010'
export SPRING_MAIL_HOST='your-smtp-host'
export SPRING_MAIL_PORT='your-smtp-port'
export SPRING_MAIL_USERNAME='your-smtp-account'
export SPRING_MAIL_PASSWORD='your-smtp-password'
./gradlew bootRun
```

`application.yml`의 `spring.mail`이 `SPRING_MAIL_*` 환경 변수를 SMTP 설정값으로 바인딩한다. `MailConfig`는 이 YAML
설정을 읽어 STARTTLS와 TLS 1.2를 적용한 `JavaMailSenderImpl`을 생성한다. 프로젝트를 전달받은 팀은 자신의 메일
서버에 맞는 Host, Port, 계정과 비밀번호를 모두 설정해야 하며 SMTP 계정은 발신 주소로도 사용한다.

### 6. 실행 확인

| 대상 | URL |
|---|---|
| Swagger UI | [http://localhost:10086/api/docu](http://localhost:10086/api/docu) |
| OpenAPI JSON | [http://localhost:10086/api/v3/api-docs](http://localhost:10086/api/v3/api-docs) |
| Actuator Health | [http://localhost:11086/actuator/health](http://localhost:11086/actuator/health) |

Health 응답이 다음과 같으면 애플리케이션과 로컬 인프라 연결이 정상이다.

```json
{"status":"UP"}
```

## 초기 데이터와 로그인

로컬 애플리케이션 시작 시 `admin_core_schema.sql`이 테이블을 자동 생성한다. 역할, 사용자와 메뉴 권한 데이터는
자동으로 INSERT하지 않으므로 로그인 전에 [DB 정의서의 초기 데이터 INSERT 문](docs/db/DB_정의서.md#32-초기-데이터-insert-문)을
MariaDB에서 한 번 실행한다.

| 항목 | 로컬 초기값 |
|---|---|
| 로그인 ID | `master` |
| 비밀번호 | `master` |
| OTP Secret | 첫 로그인 이후 새로 발급 |

초기 계정은 로컬 개발 전용이다. 로컬 애플리케이션 실행과 OTP QR 메일 발급에는 SMTP 환경 변수가 필요하며 자세한 값은
[로컬 실행 가이드의 OTP 메일 설정](docs/guide/로컬_실행_가이드.md#11-otp-메일을-포함한-로그인-테스트)을 확인한다.

## 로컬 실행 구성

```text
호스트
├─ admin-core-be
│  ├─ API                   localhost:10086
│  └─ Actuator              localhost:11086
│
└─ Docker Desktop
   ├─ MariaDB 11.4          localhost:13308 → 3306
   └─ Redis 7.4             localhost:16380 → 6379
```

| 설정 파일 | 역할 |
|---|---|
| `src/main/resources/application.yml` | 공통 설정, 기본 `local` Profile과 SMTP 환경 변수 바인딩 |
| `src/main/resources/application-local.yml` | 로컬 DB·Redis, DDL 실행과 HTTP Cookie 설정 |
| `src/main/java/com/espay/admincore/config/MailConfig.java` | SMTP 연결과 `JavaMailSenderImpl` 생성 |
| `infra/local/docker-compose.yml` | 로컬 MariaDB와 Redis 컨테이너 |
| `src/main/resources/db/schema/admin_core_schema.sql` | MariaDB 테이블 DDL |

기본 로컬 CORS Origin은 `http://localhost:13010`이고 Refresh Token Cookie는 로컬 HTTP 테스트를 위해
`Secure=false`가 적용된다.

## 아키텍처

```text
HTTP Request
    ↓
Input Adapter
Controller / Security Filter / Request·Response
    ↓
Input Port
    ↓
Application Service
UseCase 순서와 Transaction 조정
    ↓
Domain
Aggregate / Value / 업무 규칙
    ↓
Output Port
    ↓
Output Adapter
JPA / Redis / JWT / BCrypt / OTP / Mail / Excel
```

의존성은 외부 Adapter에서 Domain 방향으로만 향한다.

```text
adapter.in ──→ application ──→ domain
adapter.out ─→ application ──→ domain
config ──────→ adapter / application
```

주요 패키지:

```text
com.espay.admincore
├─ domain
│  └─ model                 Aggregate와 업무 규칙
├─ application
│  ├─ dto                   Command, Query, Result
│  ├─ port.in               Input UseCase
│  ├─ port.out              Output Port
│  └─ service               UseCase 구현
├─ adapter
│  ├─ in.web                Controller, Request, Response
│  ├─ in.security           인증·인가 입력 Adapter
│  └─ out                   JPA, Redis, JWT, Mail, Excel Adapter
├─ config                   Spring Boot 구성
└─ common                   공통 API 응답과 예외 처리
```

세부 규칙과 실제 코드 예시는 [아키텍처 가이드](docs/architecture/아키텍처_가이드.md)를 기준으로 한다.

## 신규 기능 개발 순서

1. [아키텍처 가이드](docs/architecture/아키텍처_가이드.md)에서 해당 계층의 규칙을 확인한다.
2. Aggregate 자신의 상태로 판단하는 업무 규칙은 `domain.model.{업무}`에 구현한다.
3. 유스케이스 입력과 출력은 `application.dto`의 Command, Query와 Result로 정의한다.
4. 호출 경계는 `port.in`, 외부 시스템이 필요한 경계는 `port.out`으로 정의한다.
5. Application Service는 여러 Aggregate와 Port의 처리 순서만 조정하고 다른 Service를 호출하지 않는다.
6. HTTP Request·Response와 Controller는 `adapter.in.web.{업무}`에 구현한다.
7. JPA·Redis·JWT 같은 기술 구현은 `adapter.out`에 구현한다.
8. 변경 유스케이스의 `@Transactional`은 Application Service 메서드에 선언한다.
9. 신규 API를 [인증·인가 가이드](docs/security/인증_인가_가이드.md)에 따라 `SecurityConfig`에 등록한다.
10. 테스트를 추가하고 [API 정의서](docs/api/API_정의서.md)와 필요한 DB 문서를 갱신한다.

### 신규 API Security 규칙

| API 성격 | 적용 규칙 |
|---|---|
| Access Token 없이 시작해야 하는 인증 API | `permitAll()`, Application에서 별도 Token 검증 |
| 로그인 사용자 공통 API | `authenticated()` |
| 업무 조회 API | `permissions.canView("MENU_CODE")` |
| 업무 등록·수정·삭제 API | `permissions.canEdit("MENU_CODE")` |

GET 조회 규칙은 같은 URL의 변경 규칙보다 먼저 선언한다. 정확한 예시는
[인증·인가 가이드의 SecurityConfig 등록 규칙](docs/security/인증_인가_가이드.md#신규-api-개발-시-securityconfig-등록-규칙)을
따른다.

## 자주 사용하는 명령

| 작업 | Windows | macOS |
|---|---|---|
| 테스트 | `.\gradlew.bat clean test` | `./gradlew clean test` |
| 애플리케이션 실행 | `.\gradlew.bat bootRun` | `./gradlew bootRun` |
| Gradle 정보 | `.\gradlew.bat --version` | `./gradlew --version` |
| Gradle Daemon 종료 | `.\gradlew.bat --stop` | `./gradlew --stop` |

Docker 명령:

```bash
# MariaDB와 Redis 시작
docker compose -f infra/local/docker-compose.yml up -d

# 상태 확인
docker compose -f infra/local/docker-compose.yml ps

# 로그 확인
docker compose -f infra/local/docker-compose.yml logs --tail=100 mariadb redis

# 컨테이너와 네트워크 종료, 데이터 Volume 보존
docker compose -f infra/local/docker-compose.yml down
```
