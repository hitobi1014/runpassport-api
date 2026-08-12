# [BE] API 공통 응답 래퍼 구성

> 상태: IN_PROGRESS
> 시작일: 2026-08-13

---

## 목표

성공/실패 응답을 일관된 구조로 감싸는 `CommonResponse<T>`를 정의하여, 클라이언트가 응답 형식을 예측 가능하게 한다.

---

## 설계 고려사항

### 응답 구조 선택지

**Option A: 성공/실패 통합 구조**
```json
{
  "success": true,
  "data": { ... },
  "error": null
}
```

**Option B: 성공/실패 분리 구조**
- 성공: `{ "data": { ... } }`
- 실패: `{ "code": "...", "message": "..." }` (기존 ErrorResponse)

**권장: Option B**
- 이미 `ErrorResponse`가 있으므로, 성공 응답만 래핑
- 불필요한 null 필드 제거
- Spring의 `ResponseEntity` 상태코드로 성공/실패 구분 가능

---

## 패키지 구조

```
com.runpassport.runpassportapi/
└── common/
    └── response/
        ├── ErrorResponse.kt      # (기존) 에러 응답
        └── CommonResponse.kt     # (신규) 성공 응답 래퍼
```

---

## 작업 순서

### 1단계: CommonResponse 설계 결정

성공 응답만 래핑할지, 성공/실패 통합할지 결정.

**권장 구조 (성공 응답 전용)**:
```kotlin
data class CommonResponse<T>(
    val data: T,
    val message: String? = null  // 선택적 성공 메시지
) {
    companion object {
        fun <T> success(data: T, message: String? = null): CommonResponse<T> {
            return CommonResponse(data, message)
        }
    }
}
```

**포인트**:
- 제네릭 `<T>`로 어떤 데이터든 감쌀 수 있음
- `companion object`의 팩토리 메서드로 가독성 향상
- 에러는 기존 `ErrorResponse` + `GlobalExceptionHandler` 사용

---

### 2단계: CommonResponse 작성

**파일**: `common/response/CommonResponse.kt`

```kotlin
data class CommonResponse<T>(
    val data: T,
    val message: String? = null,
) {
    companion object {
        fun <T> success(data: T, message: String? = null): CommonResponse<T> {
            return CommonResponse(data, message)
        }
    }
}
```

---

### 3단계: 동작 확인 (TestController 확장)

기존 TestController에 성공 응답 테스트 엔드포인트 추가.

```kotlin
@GetMapping("/success")
fun testSuccess(): CommonResponse<Map<String, String>> {
    val data = mapOf("greeting" to "Hello, RunPassport!")
    return CommonResponse.success(data, "조회 성공")
}
```

**검증 방법**:
```bash
curl http://localhost:8080/test/success
```

**기대 응답**:
```json
{
  "data": {
    "greeting": "Hello, RunPassport!"
  },
  "message": "조회 성공"
}
```

---

## 완료 조건 체크리스트

- [ ] `CommonResponse<T>` data class 작성
- [ ] 성공 응답 팩토리 메서드 (`CommonResponse.success(data)`)
- [ ] 테스트 컨트롤러에서 래퍼 적용 후 JSON 응답 확인

---

## 참고 사항

- `message` 필드는 선택적(nullable)으로, 필요할 때만 사용
- 컨트롤러에서 `ResponseEntity<CommonResponse<T>>`로 반환해도 되고, 직접 `CommonResponse<T>` 반환해도 됨 (Spring이 자동으로 200 OK)
- 나중에 페이징이 필요하면 `PageResponse<T>` 등 별도 래퍼 추가 가능
