[create-pr.md](../../../../ticket-system-fe/.claude/commands/create-pr.md)현재 git staged 변경사항(`git diff --staged`)을 분석 후 아래 지침 적용
커밋 메시지 생성 후 `pbcopy` 명령으로 클립보드에 복사해줘.

형식:
type: 제목 (50자 이내)

- 변경 내용 요약 1
- 변경 내용 요약 2

type 규칙:
- feat: 새 기능
- fix: 버그 수정
- refactor: 리팩토링
- chore: 설정, 패키지 등 기타
- docs: 문서

규칙:
- 제목은 한글로 작성
- 변경 내용 요약이 더 있으면 해당 내용만큼 추가
- staged 변경사항이 없으면 `git diff HEAD`로 대신 분석
- 커밋 메시지만 출력 (설명 없이)
