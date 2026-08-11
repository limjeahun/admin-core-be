# Admin Core API 정의서

## 1. 접속 정보

### 로컬 실행 주소

| 구분 | URL |
|---|---|
| API Base URL | `http://localhost:10086` |
| Swagger UI | `http://localhost:10086/api/docu` |
| OpenAPI JSON | `http://localhost:10086/api/v3/api-docs` |
| Actuator Health | `http://localhost:11086/actuator/health` |

모든 업무 API의 기본 경로는 `/api/v1`이다.

```text
http://localhost:10086/api/v1
```

### Content-Type

| 요청·응답 종류 | Content-Type |
|---|---|
| JSON 요청 | `application/json` |
| JSON 응답 | `application/json` |
| Excel 응답 | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |

### 날짜와 시간 형식

| 타입 | 형식 | 예시 |
|---|---|---|
| 검색 날짜 | ISO-8601 `yyyy-MM-dd` | `2026-08-10` |
| 응답 일시 | ISO-8601 Local Date-Time | `2026-08-10T14:30:25` |

검색 조건의 `fromDate`, `toDate`는 모두 해당 날짜를 포함한다. `toDate=2026-08-10`이면 서버는 2026년 8월 10일 23시 59분 59초 이후까지 포함하도록 다음 날 0시 미만 조건으로 조회한다.

---

## 2. 공통 요청 규칙

### Access Token

인증이 필요한 API는 다음 Header를 전달한다.

```http
Authorization: Bearer {accessToken}
```

- Access Token은 OTP 인증 또는 토큰 갱신 응답의 `data.accessToken`으로 받는다.
- Token Type은 `Bearer`다.
- 기본 Access Token 유효시간은 30분이다.
- Token 서명뿐 아니라 현재 DB의 사용자와 역할 활성 상태도 다시 확인한다.
- 사용자 또는 역할이 비활성 상태라면 유효한 JWT라도 인증되지 않는다.

### User-Agent와 클라이언트 IP

로그인, OTP와 감사 이력 기록에 사용하는 값이다.

| 값 | 전달 방식 | 설명 |
|---|---|---|
| `User-Agent` | HTTP Header | 생략하면 서버에서 `Unknown` 사용 |
| Client IP | 서버에서 추출 | Request Body로 받지 않음 |

신뢰 프록시 설정이 비활성화된 기본 환경에서는 원격 접속 주소를 사용한다. 프록시 신뢰 설정을 활성화한 환경에서만 전달 Header를 기준으로 IP를 해석한다.

### 페이지 요청

목록 API는 `page`, `size`를 사용한다.

| 파라미터 | 기본값 | 서버 보정 범위 | 설명 |
|---|---:|---:|---|
| `page` | `0` | 최소 `0` | 0부터 시작하는 페이지 번호 |
| `size` | `10` | `1`~`100` | 한 페이지의 항목 수 |

- 음수 `page`는 `0`으로 보정한다.
- `size`가 1보다 작으면 `1`, 100보다 크면 `100`으로 보정한다.
- 목록은 기본적으로 최신 생성 순으로 반환한다.

---

## 3. 공통 응답 규격

### 성공 응답

Excel 다운로드를 제외한 모든 API는 `ApiResponse<T>` 형식을 사용한다.

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {},
  "error": null
}
```

| 필드 | 타입 | Nullable | 설명 |
|---|---|---|---|
| `code` | number | N | 실제 HTTP Status와 동일한 숫자 코드 |
| `message` | string | N | 처리 결과 메시지 |
| `data` | object | Y | API별 응답 데이터 |
| `error` | object | Y | 성공 시 `null` |

데이터가 없는 성공 응답은 `data`가 `null`이다.

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": null,
  "error": null
}
```

리소스 생성 성공은 HTTP 201을 사용한다.

```json
{
  "code": 201,
  "message": "요청한 정보가 생성되었습니다.",
  "data": {},
  "error": null
}
```

### 페이지 응답

사용자, 역할, 로그인 이력, 파일 이력 목록은 다음 페이지 구조를 사용한다.

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "items": [],
    "totalCount": 0,
    "page": 0,
    "size": 10
  },
  "error": null
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `items` | array | 현재 페이지의 항목 |
| `totalCount` | number | 검색 조건에 맞는 전체 항목 수 |
| `page` | number | 서버에서 보정된 현재 페이지 번호 |
| `size` | number | 서버에서 보정된 페이지 크기 |

메뉴 목록은 페이지를 사용하지 않으므로 `data.items`만 반환한다.

### 실패 응답

```json
{
  "code": 401,
  "message": "인증이 필요합니다.",
  "data": null,
  "error": {
    "code": "UNAUTHORIZED",
    "detail": "인증이 필요합니다.",
    "fields": {}
  }
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `error.code` | string | 클라이언트가 분기 처리할 오류 코드 |
| `error.detail` | string | 오류 상세 또는 사용자 안내 메시지 |
| `error.fields` | object | Bean Validation 실패 시 필드별 오류 메시지 |

Validation 실패 예시는 다음과 같다.

```json
{
  "code": 400,
  "message": "입력값을 확인해 주세요.",
  "data": null,
  "error": {
    "code": "INVALID_INPUT",
    "detail": "입력값을 확인해 주세요.",
    "fields": {
      "loginId": "공백일 수 없습니다",
      "password": "공백일 수 없습니다"
    }
  }
}
```

`fields`의 메시지는 Jakarta Validation 기본 메시지 또는 Request DTO에 선언한 메시지다.

### 공통 오류 코드

| HTTP Status | 오류 코드 | 발생 조건 |
|---:|---|---|
| 400 | `INVALID_INPUT` | Validation 실패, 허용하지 않는 값, 메뉴 권한 조합 오류 |
| 401 | `INVALID_CREDENTIALS` | 로그인 ID 또는 비밀번호 불일치 |
| 401 | `UNAUTHORIZED` | Access Token 없음, 사전 인증 또는 OTP 검증 실패 |
| 401 | `TOKEN_INVALID` | JWT 서명·클레임 오류, Refresh Token 저장값 불일치 |
| 401 | `TOKEN_EXPIRED` | 인증 Token 만료 |
| 403 | `ACCOUNT_LOCKED` | 사용자 또는 역할이 비활성 상태 |
| 403 | `ACCESS_DENIED` | 인증됐지만 필요한 메뉴 권한 없음, CSRF 검증 실패 |
| 404 | `USER_NOT_FOUND` | 사용자 ID에 해당하는 사용자 없음 |
| 404 | `ROLE_NOT_FOUND` | 역할 ID에 해당하는 역할 없음 |
| 409 | `USER_DUPLICATE` | 로그인 ID 또는 이메일 중복 |
| 409 | `ROLE_DUPLICATE` | 역할명 중복 |
| 500 | `INTERNAL_ERROR` | 처리되지 않은 서버 내부 오류 |

Spring Security 인증 실패의 고정 응답은 다음과 같다.

```json
{
  "code": 401,
  "message": "인증이 필요합니다.",
  "data": null,
  "error": {
    "code": "UNAUTHORIZED",
    "detail": "인증이 필요합니다.",
    "fields": {}
  }
}
```

인가 실패의 고정 응답은 다음과 같다.

```json
{
  "code": 403,
  "message": "접근 권한이 없습니다.",
  "data": null,
  "error": {
    "code": "ACCESS_DENIED",
    "detail": "접근 권한이 없습니다.",
    "fields": {}
  }
}
```

---

## 4. 인증과 인가 규칙

### 공개 경로와 보호 방식

| 경로 | Access Token | 실제 보호 수단 |
|---|---|---|
| `/api/v1/auth/login` | 불필요 | 로그인 ID·비밀번호 검증 |
| `/api/v1/auth/otp/barcode` | 불필요 | Request Body의 Pre-Auth Token과 Redis 상태 검증 |
| `/api/v1/auth/otp/verify` | 불필요 | Pre-Auth Token, Redis 상태와 OTP 검증 |
| `/api/v1/auth/csrf` | 불필요 | CSRF Token 발급 API |
| `/api/v1/auth/refresh` | 불필요 | Refresh Token HttpOnly Cookie와 CSRF Header |
| `/api/v1/auth/logout` | 불필요 | Refresh Token HttpOnly Cookie와 CSRF Header |
| `/api/v1/menus/me` | 필요 | 인증된 사용자 |
| 그 밖의 업무 API | 필요 | Access Token과 메뉴별 조회·편집 권한 |

`permitAll`은 Spring Security의 Access Token 인증을 요구하지 않는다는 뜻이다. OTP, Refresh API가 아무 검증 없이 실행된다는 뜻이 아니다.

### 메뉴 권한

| 메뉴 코드 | 메뉴명 | 프론트 경로 |
|---|---|---|
| `OPERATIONS` | 운영 관리 | `null` |
| `USERS` | 사용자 관리 | `/users` |
| `ROLES` | 권한 관리 | `/roles` |
| `LOGIN_HISTORY` | 로그인 이력 조회 | `/history/login` |
| `FILE_HISTORY` | 파일 이력 조회 | `/history/file` |

| API 범위 | 필요한 권한 |
|---|---|
| `GET /api/v1/users/**` | `USERS:VIEW` |
| 사용자 생성·수정·비밀번호 초기화 | `USERS:EDIT` |
| `GET /api/v1/roles` | `ROLES:VIEW` 또는 `USERS:VIEW` |
| 그 밖의 `GET /api/v1/roles/**` | `ROLES:VIEW` |
| 역할과 역할 메뉴 변경 | `ROLES:EDIT` |
| `GET /api/v1/menus/me` | 로그인된 사용자 |
| `GET /api/v1/menus/active` | `ROLES:VIEW` |
| `GET /api/v1/history/login/**` | `LOGIN_HISTORY:VIEW` |
| `GET /api/v1/history/file/**` | `FILE_HISTORY:VIEW` |

`GET /api/v1/roles`를 `USERS:VIEW`로도 허용하는 이유는 사용자 등록·수정 화면에서 부여할 역할 목록이 필요하기 때문이다.

---

## 5. Refresh Token Cookie와 CSRF

### Refresh Token Cookie

OTP 인증 성공과 Token 갱신 성공 시 다음 Cookie가 `Set-Cookie` Header로 발급된다.

| 속성 | 기본값 | 설명 |
|---|---|---|
| Name | `admin_refresh_token` | Refresh Token Cookie 이름 |
| HttpOnly | `true` | JavaScript에서 읽을 수 없음 |
| Secure | `true` | HTTPS에서만 전송 |
| SameSite | `Strict` | 다른 Site의 요청에는 전송하지 않음 |
| Path | `/api/v1/auth` | 인증 API에만 전송 |
| Max-Age | Refresh Token 만료시간 | 기본 7일 |

`local` Profile에서는 HTTP 로컬 개발을 위해 `Secure=false`로 재정의한다.

Refresh Token은 JSON 응답에 포함하지 않는다. 브라우저는 다음 API를 호출할 때 Cookie를 자동 전송해야 한다.

```javascript
fetch("http://localhost:10086/api/v1/auth/refresh", {
  method: "POST",
  credentials: "include",
  headers: {
    "X-XSRF-TOKEN": csrfToken
  }
});
```

CORS 환경에서 Cookie를 주고받으려면 요청에 `credentials: "include"`가 필요하다. 기본 허용 Origin은 `http://localhost:13010`이다.

### CSRF Token

CSRF 검증은 다음 두 API에만 적용한다.

```text
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

호출 순서는 다음과 같다.

1. `GET /api/v1/auth/csrf`를 호출한다.
2. 응답의 `data.token`과 `data.headerName`을 확인한다.
3. Refresh 또는 Logout 요청에서 해당 Header에 Token을 전달한다.
4. Refresh Token Cookie가 자동 전송되도록 Credentials를 포함한다.

기본 Header 이름은 `X-XSRF-TOKEN`이다.

---

## 6. API 전체 목록

### 인증 API

| Method | URI | 성공 | 설명 |
|---|---|---:|---|
| POST | `/api/v1/auth/login` | 200 | 아이디·비밀번호 인증과 Pre-Auth Token 발급 |
| PUT | `/api/v1/auth/otp/barcode` | 200 | OTP 등록 QR 생성과 메일 발송 |
| POST | `/api/v1/auth/otp/verify` | 200 | OTP 검증과 Access/Refresh Token 발급 |
| GET | `/api/v1/auth/csrf` | 200 | CSRF Token 발급 |
| POST | `/api/v1/auth/refresh` | 200 | Access Token 재발급과 Refresh Token 회전 |
| POST | `/api/v1/auth/logout` | 200 | Refresh 상태 삭제와 Cookie 만료 |

### 사용자 API

| Method | URI | 성공 | 권한 | 설명 |
|---|---|---:|---|---|
| GET | `/api/v1/users` | 200 | `USERS:VIEW` | 사용자 목록 조회 |
| GET | `/api/v1/users/{userId}` | 200 | `USERS:VIEW` | 사용자 상세 조회 |
| POST | `/api/v1/users` | 201 | `USERS:EDIT` | 사용자 등록 |
| PUT | `/api/v1/users/{userId}` | 200 | `USERS:EDIT` | 사용자 수정 |
| POST | `/api/v1/users/{userId}/password-reset` | 200 | `USERS:EDIT` | 비밀번호 초기화 |
| GET | `/api/v1/users/excel` | 200 | `USERS:VIEW` | 사용자 Excel 다운로드 |

### 역할·메뉴 API

| Method | URI | 성공 | 권한 | 설명 |
|---|---|---:|---|---|
| GET | `/api/v1/roles` | 200 | `ROLES:VIEW` 또는 `USERS:VIEW` | 역할 목록 조회 |
| GET | `/api/v1/roles/{roleId}` | 200 | `ROLES:VIEW` | 역할 상세 조회 |
| POST | `/api/v1/roles` | 201 | `ROLES:EDIT` | 역할 등록 |
| PUT | `/api/v1/roles/{roleId}` | 200 | `ROLES:EDIT` | 역할 수정 |
| GET | `/api/v1/roles/{roleId}/menus` | 200 | `ROLES:VIEW` | 역할 메뉴 권한 조회 |
| PUT | `/api/v1/roles/{roleId}/menus` | 200 | `ROLES:EDIT` | 역할 메뉴 권한 전체 교체 |
| GET | `/api/v1/roles/{roleId}/users` | 200 | `ROLES:VIEW` | 역할 사용자 조회 |
| GET | `/api/v1/menus/me` | 200 | 인증 사용자 | 내 메뉴 조회 |
| GET | `/api/v1/menus/active` | 200 | `ROLES:VIEW` | 전체 메뉴 카탈로그 조회 |

### 이력 API

| Method | URI | 성공 | 권한 | 설명 |
|---|---|---:|---|---|
| GET | `/api/v1/history/login` | 200 | `LOGIN_HISTORY:VIEW` | 로그인·OTP 이력 조회 |
| GET | `/api/v1/history/login/excel` | 200 | `LOGIN_HISTORY:VIEW` | 로그인·OTP 이력 Excel 다운로드 |
| GET | `/api/v1/history/file` | 200 | `FILE_HISTORY:VIEW` | 파일 이력 조회 |
| GET | `/api/v1/history/file/excel` | 200 | `FILE_HISTORY:VIEW` | 파일 이력 Excel 다운로드 |

---

## 7. 인증 API 상세

### 7.1 아이디·비밀번호 로그인

```http
POST /api/v1/auth/login
Content-Type: application/json
```

사용자와 역할의 활성 상태, 로그인 ID와 비밀번호를 확인하고 OTP 단계에서 사용할 Pre-Auth Token을 발급한다.

#### Request Body

| 필드 | 타입 | 필수 | 제약 조건 | 설명 |
|---|---|---|---|---|
| `loginId` | string | Y | 4~100자, 공백 불가 | 관리자 로그인 ID |
| `password` | string | Y | 공백 불가 | 원문 비밀번호 |
| `loginReason` | string | Y | 최대 100자, 공백 불가 | 관리자 시스템 접속 사유 |

```json
{
  "loginId": "master",
  "password": "master",
  "loginReason": "정산 내역 확인"
}
```

#### Response `data`

| 필드 | 타입 | 설명 |
|---|---|---|
| `preAuthToken` | string | OTP 등록·검증에 사용할 단기 JWT |
| `expiresInSeconds` | number | Pre-Auth Token 유효시간(초), 기본 300초 |
| `loginId` | string | 인증된 로그인 ID |
| `name` | string | 인증된 사용자명 |
| `otpRegistered` | boolean | OTP Secret 등록 여부 |

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "preAuthToken": "eyJ...",
    "expiresInSeconds": 300,
    "loginId": "master",
    "name": "관리자",
    "otpRegistered": true
  },
  "error": null
}
```

#### 처리 흐름

- `otpRegistered=true`: OTP 앱에 표시된 번호로 `/otp/verify`를 호출한다.
- `otpRegistered=false`: 같은 `preAuthToken`으로 `/otp/barcode`를 먼저 호출한 뒤 `/otp/verify`를 호출한다.
- 로그인 ID 존재 여부와 비밀번호 중 어느 것이 틀렸는지는 구분해서 노출하지 않는다.

#### 주요 오류

| HTTP | 오류 코드 | 조건 |
|---:|---|---|
| 400 | `INVALID_INPUT` | 필수값 또는 길이 검증 실패 |
| 401 | `INVALID_CREDENTIALS` | 로그인 ID 또는 비밀번호 불일치 |
| 403 | `ACCOUNT_LOCKED` | 사용자 또는 역할 비활성 |

### 7.2 OTP 바코드 발급

```http
PUT /api/v1/auth/otp/barcode
Content-Type: application/json
```

Pre-Auth Token과 Redis의 사전 인증 상태를 확인하고 새로운 OTP Secret과 QR Code를 생성한다. QR Code 메일 발송이 성공한 경우에만 사용자에게 OTP Secret을 저장한다.

#### Request Body

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `preAuthToken` | string | Y | 로그인 응답에서 받은 Pre-Auth JWT |

```json
{
  "preAuthToken": "eyJ..."
}
```

#### Response

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": null,
  "error": null
}
```

QR 이미지나 OTP Secret은 응답 JSON으로 반환하지 않는다. 등록 QR은 사용자의 등록 이메일로 전달한다.

#### 주요 오류

| HTTP | 오류 코드 | 조건 |
|---:|---|---|
| 400 | `INVALID_INPUT` | Pre-Auth Token 누락 |
| 401 | `UNAUTHORIZED` | Token 만료·변조 또는 Redis 상태 불일치 |
| 403 | `ACCOUNT_LOCKED` | 사용자 또는 역할 비활성 |
| 500 | `INTERNAL_ERROR` | QR 생성 또는 메일 발송 실패 |

### 7.3 OTP 인증

```http
POST /api/v1/auth/otp/verify
Content-Type: application/json
```

검증된 Pre-Auth Token의 사용자 ID를 기준으로 OTP를 확인한다. Request Body에서 사용자 ID를 받지 않으므로 다른 사용자 ID로 바꾸어 요청할 수 없다.

#### Request Body

| 필드 | 타입 | 필수 | 제약 조건 | 설명 |
|---|---|---|---|---|
| `preAuthToken` | string | Y | 공백 불가 | 로그인 응답의 Pre-Auth JWT |
| `otpNumber` | string | Y | 숫자 6자리 | 인증 앱에 표시된 TOTP |

```json
{
  "preAuthToken": "eyJ...",
  "otpNumber": "314976"
}
```

#### Response `data`

| 필드 | 타입 | 설명 |
|---|---|---|
| `accessToken` | string | 보호 API 호출에 사용할 Access JWT |
| `tokenType` | string | `Bearer` |
| `expiresInSeconds` | number | Access Token 유효시간(초), 기본 1800초 |
| `userId` | string | 인증된 사용자 ID |
| `userName` | string | 사용자명 |
| `roleId` | string | 현재 역할 ID |
| `roleName` | string | 현재 역할명 |

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "accessToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresInSeconds": 1800,
    "userId": "7",
    "userName": "관리자",
    "roleId": "1",
    "roleName": "마스터"
  },
  "error": null
}
```

Response Header에는 Refresh Token Cookie가 포함된다.

```http
Set-Cookie: admin_refresh_token={refreshToken}; Path=/api/v1/auth; HttpOnly; SameSite=Strict; ...
```

#### OTP 실패 처리

- 실패 횟수는 Pre-Auth Redis 상태에 기록한다.
- 기본 최대 실패 횟수는 5회다.
- 최대 실패 횟수에 도달하면 Pre-Auth Redis 상태를 삭제한다.
- 성공하면 Pre-Auth 상태를 원자적으로 소비하므로 같은 Token을 다시 사용할 수 없다.

#### 주요 오류

| HTTP | 오류 코드 | 조건 |
|---:|---|---|
| 400 | `INVALID_INPUT` | OTP가 숫자 6자리 형식이 아님 |
| 401 | `UNAUTHORIZED` | Pre-Auth 검증 실패, OTP 불일치 또는 Token 재사용 |
| 403 | `ACCOUNT_LOCKED` | 사용자 또는 역할 비활성 |

### 7.4 CSRF Token 발급

```http
GET /api/v1/auth/csrf
```

#### Response `data`

| 필드 | 타입 | 설명 |
|---|---|---|
| `token` | string | Refresh·Logout 요청에 전달할 CSRF Token |
| `headerName` | string | Token을 전달할 Header 이름 |

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "token": "c0d4...",
    "headerName": "X-XSRF-TOKEN"
  },
  "error": null
}
```

브라우저 요청에서는 CSRF Cookie 수신을 위해 Credentials를 포함한다.

### 7.5 Token 갱신

```http
POST /api/v1/auth/refresh
Cookie: admin_refresh_token={refreshToken}
X-XSRF-TOKEN: {csrfToken}
```

Request Body는 없다. 기존 Refresh Token을 검증한 뒤 Access Token과 Refresh Token을 모두 새로 발급한다. Redis에 저장된 현재 Refresh Token 해시와 일치할 때만 원자적으로 회전한다.

#### Response

응답 `data` 구조는 [OTP 인증 응답](#73-otp-인증)의 `LoginResponse`와 같다. 새 Refresh Token은 JSON이 아니라 `Set-Cookie` Header로 다시 발급한다.

#### 주요 오류

| HTTP | 오류 코드 | 조건 |
|---:|---|---|
| 401 | `TOKEN_INVALID` | Cookie 누락, Token 변조 또는 Redis 저장값 불일치 |
| 401 | `TOKEN_EXPIRED` | Refresh Token 만료 |
| 403 | `ACCOUNT_LOCKED` | 사용자 또는 역할 비활성 |
| 403 | `ACCESS_DENIED` | CSRF Token 누락 또는 불일치 |
| 404 | `USER_NOT_FOUND` | Token 사용자가 현재 존재하지 않음 |

### 7.6 로그아웃

```http
POST /api/v1/auth/logout
Cookie: admin_refresh_token={refreshToken}
X-XSRF-TOKEN: {csrfToken}
```

Request Body는 없다.

- 유효한 Refresh Token이 서버 저장값과 일치하면 Redis 상태를 삭제한다.
- Cookie가 없거나 이미 무효인 Token이어도 CSRF 검증을 통과하면 로그아웃 응답은 성공한다.
- 응답에서 같은 이름과 경로의 Cookie를 `Max-Age=0`으로 만료한다.

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": null,
  "error": null
}
```

```http
Set-Cookie: admin_refresh_token=; Path=/api/v1/auth; Max-Age=0; HttpOnly; SameSite=Strict; ...
```

---

## 8. 사용자 API 상세

모든 사용자 API는 Access Token이 필요하다.

### 사용자 응답 모델

| 필드 | 타입 | Nullable | 설명 |
|---|---|---|---|
| `userId` | string | N | 사용자 ID |
| `loginId` | string | N | 로그인 ID |
| `name` | string | N | 사용자명 |
| `email` | string | N | 이메일 |
| `phoneNo` | string | Y | 연락처 |
| `deptName` | string | Y | 소속 또는 부서명 |
| `roleId` | string | N | 역할 ID |
| `roleName` | string | Y | 역할명 |
| `status` | string | N | `ACTIVE` 또는 `INACTIVE` |
| `otpRegistered` | boolean | N | OTP Secret 등록 여부 |
| `lastLoginAt` | string | Y | 마지막 최종 로그인 일시 |
| `createdAt` | string | N | 생성 일시 |
| `updatedAt` | string | N | 수정 일시 |

```json
{
  "userId": "7",
  "loginId": "admin01",
  "name": "관리자",
  "email": "admin@example.com",
  "phoneNo": "010-1234-5678",
  "deptName": "운영팀",
  "roleId": "1",
  "roleName": "마스터",
  "status": "ACTIVE",
  "otpRegistered": true,
  "lastLoginAt": "2026-08-10T14:30:25",
  "createdAt": "2026-08-01T09:00:00",
  "updatedAt": "2026-08-10T14:30:25"
}
```

### 8.1 사용자 목록 조회

```http
GET /api/v1/users
Authorization: Bearer {accessToken}
```

필요 권한은 `USERS:VIEW`다.

#### Query Parameters

| 이름 | 타입 | 필수 | 기본값 | 허용값·설명 |
|---|---|---|---|---|
| `roleId` | string | N | - | 역할 ID로 필터 |
| `status` | string | N | - | `ACTIVE`, `Y`, `INACTIVE`, `N` |
| `conditionType` | string | N | `ALL` | `LOGIN_ID`, `ID`, `NAME`, `USER_NAME`, `EMAIL`, `ALL` |
| `keyword` | string | N | - | 선택 필드의 부분 일치 검색어 |
| `page` | integer | N | `0` | 0부터 시작 |
| `size` | integer | N | `10` | 1~100으로 보정 |

`conditionType`을 생략하거나 알 수 없는 값을 전달하면 로그인 ID, 사용자명, 이메일 전체에서 검색한다.

```http
GET /api/v1/users?roleId=1&status=ACTIVE&conditionType=NAME&keyword=관리&page=0&size=10
```

응답의 `data.items`는 [사용자 응답 모델](#사용자-응답-모델) 배열이다.

### 8.2 사용자 상세 조회

```http
GET /api/v1/users/{userId}
Authorization: Bearer {accessToken}
```

| Path Variable | 타입 | 필수 | 설명 |
|---|---|---|---|
| `userId` | string | Y | 조회할 사용자 ID |

필요 권한은 `USERS:VIEW`다. 응답 `data`는 [사용자 응답 모델](#사용자-응답-모델)이다. 사용자가 없으면 `404 USER_NOT_FOUND`를 반환한다.

### 8.3 사용자 등록

```http
POST /api/v1/users
Authorization: Bearer {accessToken}
Content-Type: application/json
```

필요 권한은 `USERS:EDIT`다.

#### Request Body

| 필드 | 타입 | 필수 | 제약 조건 | 설명 |
|---|---|---|---|---|
| `loginId` | string | Y | 4~100자 | 중복 불가 로그인 ID |
| `name` | string | Y | 최대 100자 | 사용자명 |
| `email` | string | Y | 이메일 형식 | 중복 불가 이메일 |
| `phoneNo` | string | N | - | 연락처 |
| `deptName` | string | N | - | 소속 또는 부서명 |
| `roleId` | string | Y | 공백 불가 | 부여할 활성 역할 ID |
| `password` | string | Y | 8~100자, 영문·숫자·특수문자 포함 | 초기 비밀번호 |
| `status` | string | N | `ACTIVE`/`Y`, `INACTIVE`/`N` | 생략 시 `ACTIVE` |

```json
{
  "loginId": "operator01",
  "name": "운영 담당자",
  "email": "operator01@example.com",
  "phoneNo": "010-1111-2222",
  "deptName": "운영팀",
  "roleId": "2",
  "password": "Operator!234",
  "status": "ACTIVE"
}
```

성공 시 HTTP 201과 [사용자 응답 모델](#사용자-응답-모델)을 반환한다.

#### 주요 오류

| HTTP | 오류 코드 | 조건 |
|---:|---|---|
| 400 | `INVALID_INPUT` | Request Validation 또는 지원하지 않는 상태값 |
| 404 | `ROLE_NOT_FOUND` | 역할이 존재하지 않음 |
| 403 | `ACCOUNT_LOCKED` | 부여할 역할이 비활성 |
| 409 | `USER_DUPLICATE` | 로그인 ID 또는 이메일 중복 |

### 8.4 사용자 수정

```http
PUT /api/v1/users/{userId}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

필요 권한은 `USERS:EDIT`다.

#### Request Body

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `name` | string | N | 변경할 사용자명. `null` 또는 공백이면 기존 값 유지 |
| `email` | string | N | 변경할 이메일. 이메일 형식 검증, 공백이면 기존 값 유지 |
| `phoneNo` | string | N | `null`이면 기존 값 유지 |
| `deptName` | string | N | `null`이면 기존 값 유지 |
| `roleId` | string | N | 공백이면 기존 역할 유지 |
| `status` | string | N | `ACTIVE`/`Y`, `INACTIVE`/`N`; 공백이면 기존 값 유지 |

```json
{
  "name": "운영 담당자2",
  "email": "operator02@example.com",
  "phoneNo": "010-3333-4444",
  "deptName": "정산운영팀",
  "roleId": "3",
  "status": "ACTIVE"
}
```

`loginId`, 비밀번호, OTP Secret은 이 API로 변경하지 않는다. 성공 시 변경된 [사용자 응답 모델](#사용자-응답-모델)을 반환한다.

### 8.5 사용자 비밀번호 초기화

```http
POST /api/v1/users/{userId}/password-reset
Authorization: Bearer {accessToken}
Content-Type: application/json
```

필요 권한은 `USERS:EDIT`다.

#### Request Body

| 필드 | 타입 | 필수 | 제약 조건 |
|---|---|---|---|
| `newPassword` | string | Y | 8~100자, 영문·숫자·특수문자 포함 |
| `confirmPassword` | string | Y | `newPassword`와 일치해야 함 |

```json
{
  "newPassword": "Changed!234",
  "confirmPassword": "Changed!234"
}
```

- 새 비밀번호와 확인값이 다르면 `400 INVALID_INPUT`이다.
- 기존 비밀번호와 같은 비밀번호로 변경할 수 없다.
- 비밀번호 원문은 저장하지 않고 BCrypt 해시만 저장한다.
- 성공 시 [사용자 응답 모델](#사용자-응답-모델)을 반환한다.

### 8.6 사용자 Excel 다운로드

```http
GET /api/v1/users/excel
Authorization: Bearer {accessToken}
```

필요 권한은 `USERS:VIEW`다. Query Parameters는 [사용자 목록 조회](#81-사용자-목록-조회)와 같다.

#### Response

```http
HTTP/1.1 200 OK
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="users.xlsx"
```

- 공통 JSON 응답이 아니라 XLSX Binary를 반환한다.
- 다운로드 성공 또는 실패를 파일 이력에 기록한다.
- 파일 이력에는 인증 사용자 ID와 클라이언트 IP를 서버에서 기록한다.

---

## 9. 역할 API 상세

### 역할 응답 모델

| 필드 | 타입 | Nullable | 설명 |
|---|---|---|---|
| `roleId` | string | N | 역할 ID |
| `name` | string | N | 역할명 |
| `description` | string | Y | 역할 설명 |
| `useYn` | string | N | 사용 여부 `Y` 또는 `N` |
| `userCount` | number | N | 역할이 부여된 사용자 수 |
| `createdAt` | string | N | 생성 일시 |
| `updatedAt` | string | N | 수정 일시 |

```json
{
  "roleId": "1",
  "name": "마스터",
  "description": "전체 관리자 기능",
  "useYn": "Y",
  "userCount": 3,
  "createdAt": "2026-08-01T09:00:00",
  "updatedAt": "2026-08-01T09:00:00"
}
```

### 9.1 역할 목록 조회

```http
GET /api/v1/roles?keyword=운영&page=0&size=10
Authorization: Bearer {accessToken}
```

필요 권한은 `ROLES:VIEW` 또는 `USERS:VIEW` 중 하나다.

| Query Parameter | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `keyword` | string | N | - | 역할명 또는 설명 부분 일치 검색 |
| `page` | integer | N | `0` | 0부터 시작 |
| `size` | integer | N | `10` | 1~100으로 보정 |

응답은 [역할 응답 모델](#역할-응답-모델)의 페이지 구조다.

### 9.2 역할 상세 조회

```http
GET /api/v1/roles/{roleId}
Authorization: Bearer {accessToken}
```

필요 권한은 `ROLES:VIEW`다. 응답 `data`는 [역할 응답 모델](#역할-응답-모델)이다. 역할이 없으면 `404 ROLE_NOT_FOUND`를 반환한다.

### 9.3 역할 등록

```http
POST /api/v1/roles
Authorization: Bearer {accessToken}
Content-Type: application/json
```

필요 권한은 `ROLES:EDIT`다.

#### Request Body

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `name` | string | Y | 중복 불가 역할명 |
| `description` | string | N | 역할의 업무 범위 설명 |
| `useYn` | string | N | `Y`/`ACTIVE` 또는 `N`/`INACTIVE`; 생략 시 활성 |
| `permissions` | array | N | 최초 메뉴 권한 목록, 생략 시 빈 목록 |

`permissions` 항목은 다음 구조다.

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `menuCode` | string | Y | 지원하는 메뉴 코드 |
| `canView` | boolean | Y | 조회 허용 여부 |
| `canEdit` | boolean | Y | 편집 허용 여부 |

```json
{
  "name": "운영 관리자",
  "description": "사용자와 이력 조회 담당",
  "useYn": "Y",
  "permissions": [
    {
      "menuCode": "USERS",
      "canView": true,
      "canEdit": true
    },
    {
      "menuCode": "LOGIN_HISTORY",
      "canView": true,
      "canEdit": false
    }
  ]
}
```

성공 시 HTTP 201과 [역할 응답 모델](#역할-응답-모델)을 반환한다.

#### 메뉴 권한 검증

- 지원 메뉴 코드는 `USERS`, `ROLES`, `LOGIN_HISTORY`, `FILE_HISTORY`다.
- 상위 그룹 `OPERATIONS`는 저장하지 않고 사용자 메뉴 조회 시 자동 포함한다.
- 같은 `menuCode`를 중복해서 전달할 수 없다.
- 선택한 메뉴는 최소 조회 또는 편집 권한 하나가 있어야 한다.
- `canEdit=true`인데 `canView=false`인 조합은 허용하지 않는다.

### 9.4 역할 수정

```http
PUT /api/v1/roles/{roleId}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

필요 권한은 `ROLES:EDIT`다.

#### Request Body

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `name` | string | N | `null` 또는 공백이면 기존 역할명 유지 |
| `description` | string | N | `null`이면 기존 설명 유지, 빈 문자열이면 설명 제거 |
| `useYn` | string | N | `Y`/`ACTIVE`, `N`/`INACTIVE`; 공백이면 기존 값 유지 |

```json
{
  "name": "운영 관리자",
  "description": "사용자 및 이력 관리",
  "useYn": "Y"
}
```

역할 메뉴 권한은 이 API로 변경하지 않는다. `/roles/{roleId}/menus`를 사용한다.

### 9.5 역할 메뉴 권한 조회

```http
GET /api/v1/roles/{roleId}/menus
Authorization: Bearer {accessToken}
```

필요 권한은 `ROLES:VIEW`다. 전체 메뉴 카탈로그를 반환하며 각 메뉴에 현재 역할의 연결 상태가 표시된다.

응답 모델은 [메뉴 응답 모델](#메뉴-응답-모델)을 사용한다.

### 9.6 역할 메뉴 권한 전체 교체

```http
PUT /api/v1/roles/{roleId}/menus
Authorization: Bearer {accessToken}
Content-Type: application/json
```

필요 권한은 `ROLES:EDIT`다.

```json
{
  "permissions": [
    {
      "menuCode": "USERS",
      "canView": true,
      "canEdit": true
    },
    {
      "menuCode": "FILE_HISTORY",
      "canView": true,
      "canEdit": false
    }
  ]
}
```

이 API는 부분 추가가 아니라 **전체 교체**다.

- 요청에 포함되지 않은 기존 메뉴 권한은 삭제한다.
- `permissions`가 `null`이거나 빈 배열이면 역할의 메뉴 권한을 모두 제거한다.
- 검증 규칙은 역할 등록의 메뉴 권한 검증과 같다.

성공 응답의 `data`는 `null`이다.

### 9.7 역할 사용자 조회

```http
GET /api/v1/roles/{roleId}/users?page=0&size=10
Authorization: Bearer {accessToken}
```

필요 권한은 `ROLES:VIEW`다.

| 파라미터 | 위치 | 타입 | 기본값 | 설명 |
|---|---|---|---|---|
| `roleId` | Path | string | - | 조회할 역할 ID |
| `page` | Query | integer | `0` | 0부터 시작 |
| `size` | Query | integer | `10` | 1~100으로 보정 |

역할 존재 여부를 먼저 검증한 뒤 해당 역할이 부여된 사용자 페이지를 반환한다.

---

## 10. 메뉴 API 상세

### 메뉴 응답 모델

| 필드 | 타입 | 설명 |
|---|---|---|
| `menuCode` | string | 메뉴 코드 |
| `name` | string | 화면 표시명 |
| `path` | string/null | 프론트엔드 Route, 그룹 메뉴는 `null` |
| `parentMenuCode` | string/null | 상위 메뉴 코드 |
| `sortOrder` | number | 같은 계층의 표시 순서 |
| `assigned` | boolean | 현재 역할에 연결 또는 노출된 메뉴인지 여부 |
| `canView` | boolean | 조회 권한 여부 |
| `canEdit` | boolean | 편집 권한 여부 |

```json
{
  "menuCode": "USERS",
  "name": "사용자 관리",
  "path": "/users",
  "parentMenuCode": "OPERATIONS",
  "sortOrder": 10,
  "assigned": true,
  "canView": true,
  "canEdit": true
}
```

### 10.1 내 메뉴 조회

```http
GET /api/v1/menus/me
Authorization: Bearer {accessToken}
```

별도의 메뉴 코드를 요구하지 않고 인증만 필요하다.

- 현재 사용자의 역할에 `canView=true`로 연결된 메뉴를 반환한다.
- 화면 계층을 유지하기 위해 선택된 하위 메뉴의 상위 메뉴도 자동 포함한다.
- 상위 그룹은 직접 저장된 권한이 없어도 `canView=true`, `canEdit=false`로 표시될 수 있다.
- 로그인 응답에는 메뉴를 포함하지 않으므로 로그인 완료 후 이 API를 별도로 호출한다.

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "items": [
      {
        "menuCode": "OPERATIONS",
        "name": "운영 관리",
        "path": null,
        "parentMenuCode": null,
        "sortOrder": 10,
        "assigned": true,
        "canView": true,
        "canEdit": false
      },
      {
        "menuCode": "USERS",
        "name": "사용자 관리",
        "path": "/users",
        "parentMenuCode": "OPERATIONS",
        "sortOrder": 10,
        "assigned": true,
        "canView": true,
        "canEdit": true
      }
    ]
  },
  "error": null
}
```

### 10.2 전체 메뉴 카탈로그 조회

```http
GET /api/v1/menus/active
Authorization: Bearer {accessToken}
```

필요 권한은 `ROLES:VIEW`다.

- 코드에 정의된 전체 메뉴를 선언 순서대로 반환한다.
- 특정 역할의 연결 정보를 조회하는 API가 아니므로 `assigned`, `canView`, `canEdit`는 모두 `false`다.
- 역할별 현재 연결 상태가 필요하면 `GET /api/v1/roles/{roleId}/menus`를 사용한다.

---

## 11. 로그인·OTP 이력 API 상세

### 로그인 이력 응답 모델

| 필드 | 타입 | Nullable | 설명 |
|---|---|---|---|
| `historyId` | string | N | 이력 ID |
| `userId` | string | Y | 식별된 사용자 ID. 로그인 ID 식별 전 실패하면 `null` |
| `userName` | string | Y | 사용자명 |
| `loginId` | string | Y | 식별된 사용자의 로그인 ID |
| `authStep` | string | N | `LOGIN` 또는 `OTP` |
| `success` | boolean | N | 인증 성공 여부 |
| `loginReason` | string | Y | 사용자가 입력한 접속 사유 |
| `failReason` | string | Y | 실패 사유 코드, 성공 시 `null` |
| `inputId` | string | Y | 로그인 단계에서 실제 입력한 로그인 ID |
| `clientIp` | string | Y | 요청 클라이언트 IP |
| `userAgent` | string | Y | 요청 User-Agent |
| `createdAt` | string | N | 인증 시도 일시 |

### 11.1 로그인·OTP 이력 목록 조회

```http
GET /api/v1/history/login
Authorization: Bearer {accessToken}
```

필요 권한은 `LOGIN_HISTORY:VIEW`다.

#### Query Parameters

| 이름 | 타입 | 필수 | 기본값 | 허용값·설명 |
|---|---|---|---|---|
| `fromDate` | date | N | - | 시작일 포함, `yyyy-MM-dd` |
| `toDate` | date | N | - | 종료일 포함, `yyyy-MM-dd` |
| `authStep` | string | N | - | `LOGIN` 또는 `OTP` |
| `success` | boolean | N | - | `true` 성공, `false` 실패 |
| `conditionType` | string | N | `ALL` | `NAME`, `USER_NAME`, `LOGIN_ID`, `ID`, `IP`, `CLIENT_IP`, `ALL` |
| `keyword` | string | N | - | 선택 필드 부분 일치 검색 |
| `page` | integer | N | `0` | 0부터 시작 |
| `size` | integer | N | `10` | 1~100으로 보정 |

```http
GET /api/v1/history/login?fromDate=2026-08-01&toDate=2026-08-10&authStep=OTP&success=false&conditionType=LOGIN_ID&keyword=admin&page=0&size=20
```

`conditionType=LOGIN_ID`는 식별된 사용자의 로그인 ID와 실제 입력 ID를 함께 검색한다. `conditionType`을 생략하거나 알 수 없는 값을 전달하면 사용자명, 로그인 ID, 입력 ID, 클라이언트 IP 전체를 검색한다.

### 11.2 로그인·OTP 이력 Excel 다운로드

```http
GET /api/v1/history/login/excel
Authorization: Bearer {accessToken}
```

필요 권한과 Query Parameters는 목록 조회와 같다.

```http
HTTP/1.1 200 OK
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="login-history.xlsx"
```

다운로드 결과는 파일 이력에 메뉴 코드 `LOGIN_HISTORY`와 함께 기록한다.

---

## 12. 파일 이력 API 상세

파일 이력은 사용자·이력 Excel 내보내기 같은 파일 처리 유스케이스에서 서버가 기록한다. 외부에서 임의로 파일 이력을 생성하는 API는 제공하지 않는다.

### 파일 이력 응답 모델

| 필드 | 타입 | Nullable | 설명 |
|---|---|---|---|
| `historyId` | string | N | 이력 ID |
| `userId` | string | N | 작업 사용자 ID |
| `userName` | string | Y | 사용자명 |
| `loginId` | string | Y | 사용자 로그인 ID |
| `ioType` | string | N | 업로드 `U` 또는 다운로드 `D` |
| `menuCode` | string | N | 작업이 발생한 메뉴 코드 |
| `menuName` | string | N | 메뉴 표시명 |
| `fileName` | string | N | 파일명 |
| `fileSize` | number | Y | 파일 크기(KB), 기록할 내용이 없으면 `null` |
| `success` | boolean | N | 처리 성공 여부 |
| `failReason` | string | Y | 실패 사유, 성공 시 `null` |
| `clientIp` | string | Y | 요청 클라이언트 IP |
| `createdAt` | string | N | 파일 처리 일시 |

### 12.1 파일 이력 목록 조회

```http
GET /api/v1/history/file
Authorization: Bearer {accessToken}
```

필요 권한은 `FILE_HISTORY:VIEW`다.

#### Query Parameters

| 이름 | 타입 | 필수 | 기본값 | 허용값·설명 |
|---|---|---|---|---|
| `fromDate` | date | N | - | 시작일 포함, `yyyy-MM-dd` |
| `toDate` | date | N | - | 종료일 포함, `yyyy-MM-dd` |
| `ioType` | string | N | - | `U`/`UPLOAD` 또는 `D`/`DOWNLOAD` |
| `success` | boolean | N | - | `true` 성공, `false` 실패 |
| `conditionType` | string | N | `ALL` | `NAME`, `USER_NAME`, `LOGIN_ID`, `ID`, `IP`, `CLIENT_IP`, `FILE_NAME`, `ALL` |
| `keyword` | string | N | - | 선택 필드 부분 일치 검색 |
| `page` | integer | N | `0` | 0부터 시작 |
| `size` | integer | N | `10` | 1~100으로 보정 |

```http
GET /api/v1/history/file?fromDate=2026-08-01&toDate=2026-08-10&ioType=DOWNLOAD&success=true&conditionType=FILE_NAME&keyword=users&page=0&size=20
```

`conditionType`을 생략하거나 알 수 없는 값을 전달하면 사용자명, 로그인 ID, 클라이언트 IP, 파일명 전체를 검색한다.

### 12.2 파일 이력 Excel 다운로드

```http
GET /api/v1/history/file/excel
Authorization: Bearer {accessToken}
```

필요 권한과 Query Parameters는 목록 조회와 같다.

```http
HTTP/1.1 200 OK
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="file-history.xlsx"
```

다운로드 결과는 파일 이력에 메뉴 코드 `FILE_HISTORY`와 함께 다시 기록된다.

---

## 13. 호출 예시

### 로그인부터 내 메뉴 조회까지

```text
1. POST /api/v1/auth/login
   └─ preAuthToken 수신

2. otpRegistered가 false인 경우
   PUT /api/v1/auth/otp/barcode
   └─ OTP 등록 QR 메일 수신 및 인증 앱 등록

3. POST /api/v1/auth/otp/verify
   ├─ accessToken 수신
   └─ admin_refresh_token HttpOnly Cookie 수신

4. GET /api/v1/menus/me
   └─ Authorization: Bearer {accessToken}
```

### Access Token 갱신

```text
1. GET /api/v1/auth/csrf
   └─ token, headerName 수신

2. POST /api/v1/auth/refresh
   ├─ admin_refresh_token Cookie 자동 전송
   ├─ X-XSRF-TOKEN Header 전송
   ├─ 새 accessToken 수신
   └─ 회전된 admin_refresh_token Cookie 수신
```

### Curl 로그인 예시

```bash
curl -X POST "http://localhost:10086/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -H "User-Agent: admin-api-client" \
  -d '{
    "loginId": "master",
    "password": "master",
    "loginReason": "운영 업무"
  }'
```

### Curl 보호 API 예시

```bash
curl "http://localhost:10086/api/v1/users?page=0&size=10" \
  -H "Authorization: Bearer {accessToken}"
```

### 브라우저 Refresh 예시

```javascript
const csrfResponse = await fetch(
  "http://localhost:10086/api/v1/auth/csrf",
  { credentials: "include" }
);
const csrf = (await csrfResponse.json()).data;

const refreshResponse = await fetch(
  "http://localhost:10086/api/v1/auth/refresh",
  {
    method: "POST",
    credentials: "include",
    headers: {
      [csrf.headerName]: csrf.token
    }
  }
);

const login = await refreshResponse.json();
const accessToken = login.data.accessToken;
```
