---
allowed-tools: Read, Bash(git log:*), mcp__notion__notion-query-data-sources, mcp__notion__notion-fetch, mcp__notion__notion-update-page
argument-hint: (선택 없음, 또는 특정 Notion 티켓 ID)
description: Notion "Run패스포트 Develop" BE+INFRA 티켓과 로컬 develop-ticket.md를 대조해 불일치를 리포트
---

# 목적

Notion "Run패스포트 Develop" DB의 BE+INFRA 뷰와 로컬 `ai/develop-ticket.md`를 대조한다.
**로컬 파일을 자동으로 덮어쓰지 않는다** — 불일치 리포트만 만들고, 실제 반영(로컬 파일 수정 /
Notion 상태 갱신)은 매번 사용자 확인을 받은 뒤 1건씩 처리한다. 자세한 배경은 `ai/response.md`
("요청 응답: Notion 티켓 동기화 관리 방식") 참고.

## 1단계: Notion 조회

- 데이터 소스: `collection://2c59c9e2-d478-828c-978b-07827b6aa116` ("Run패스포트 Develop")
- SQL 조건: `영역 IN ('백엔드', '인프라')`
- 가져올 컬럼: `url`, `userDefined:ID`, `이름`, `상태`, `우선순위`, `마일스톤`, `date:기간:start`, `date:기간:end`
- 마일스톤 이름이 필요하면 `collection://5f49c9e2-d478-827d-8046-87cff91fa269` ("Run패스포트 마일스톤")도 함께 조회해서 relation URL을 이름으로 매핑

## 2단계: 로컬 조회

- `ai/develop-ticket.md`의 "## 티켓 목록" 섹션을 파싱한다.
- 각 티켓 제목에서 Notion ID 패턴을 추출한다:
  - `[BE] (#12) 제목` → Notion 티켓 #12에 직접 대응
  - `[BE] (#10 하위) 제목` → Notion 티켓 #10의 하위 작업 (직접 매칭 대상 아님, 참고용)
  - ID 표기가 없는 티켓은 "노션과 연결 안 됨"으로 분류
- 각 티켓의 로컬 상태(TODO/IN_PROGRESS/BLOCKED/DONE)도 함께 수집

## 3단계: 대조

상태 매핑 규칙 (`ai/response.md` 3-3절과 동일):

| Notion 상태 | 로컬 상태 |
|---|---|
| Backlog, To Do | TODO |
| In Progress | IN_PROGRESS |
| Review/Test | IN_PROGRESS |
| Blocked | BLOCKED |
| Done | DONE (로컬 규칙상 삭제됨) |

다음 세 그룹으로 나눠서 정리한다:

1. **Notion에 있는데 로컬에 없음** — 특히 현재 진행 중인 마일스톤 주간(오늘 날짜 기준 `기간`이 걸치는 마일스톤) 것을 우선 표시. Notion 상태가 Done이면 굳이 로컬에 없어도 정상(완료 티켓은 로컬에서 지우는 규칙)이므로 제외.
2. **로컬에 있는데 Notion ID가 없음** — 노션 대비 세분화된 하위 작업으로 추정. 상위 티켓을 사용자에게 물어봐야 함.
3. **같은 ID인데 상태가 다름** — 매핑 규칙 기준으로 불일치인 것만.

## 4단계: 출력

- 채팅에 표 형태로 요약만 출력한다 (파일로 저장하지 않는다 — 이건 read-only 진단이라 매번 최신 상태를 다시 봐야 의미가 있음).
- 각 불일치 항목에 대해 "로컬에 반영할지 / Notion에 반영할지" 사용자에게 묻는다.
- 사용자가 승인한 것만:
  - 로컬 반영 → `ai/develop-ticket.md`를 Edit으로 수정 (새 티켓 추가 또는 제목의 `(#ID)` 표기 수정)
  - Notion 반영 → 티켓 1건씩 `notion-update-page`로 상태 변경. 여러 건을 한 번에 일괄 변경하지 않는다.
