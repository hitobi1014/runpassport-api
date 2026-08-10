아래 지침에 따라 코드 리뷰를 수행해줘.
- 코드리뷰는 ai/code-review.md에 작성

## 리뷰 제외대상 
- .md 파일 (문서, 지침, 결과 등)

## 리뷰 대상 결정
- ARGUMENTS `$ARGUMENTS` 가 제공된 경우: 해당 파일(들)을 Read 도구로 읽어서 리뷰
- ARGUMENTS가 없는 경우: `git diff --staged` 로 현재 staged 변경사항을 확인하여 리뷰
  - staged 변경사항이 없으면 `git diff HEAD` 로 미커밋 변경사항 확인

## 리뷰 기준 (프로젝트 컨벤션 기반)
1. **타입 안전성**: TypeScript 타입이 올바르게 사용되었는지, any/unknown 남용 없는지
2. **Zustand 스토어 패턴**: isLoading 관리, 파생 데이터 처리, set/get 사용이 올바른지
3. **API 호출**: fetchApi 래퍼 사용 여부, 에러 처리 (toast.error), 성공 처리 (toast.success)
4. **컴포넌트 구조**: Props 인터페이스 정의, export 방식, 역할 분리
5. **경로 alias**: `@/` 사용 여부 (상대경로 지양)
6. **ESLint 규칙 준수**: unused vars, react-hooks 규칙
7. **불필요한 코드**: 주석 처리된 코드, 사용하지 않는 import
8. **가독성/유지보수성**: 함수/변수명 명확성, 단일 책임 원칙

## 리뷰 출력 형식
각 항목을 아래 형식으로 맨 아랫줄 코드리뷰에 출력:

### [파일명 또는 변경 범위]
- **[심각도: 🔴 오류 / 🟡 경고 / 🔵 제안]** 내용
  - 문제점: ...
  - 개선 방향: ...

마지막에 전체 요약 (잘된 점 / 개선 필요 점) 작성
