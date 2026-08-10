현재 브랜치와 main 브랜치의 차이를 분석해서 GitHub PR 내용을 작성해줘.

아래 순서로 정보 수집:
1. `git branch --show-current` — 현재 브랜치명 확인
2. `git log main..HEAD --oneline` — 커밋 목록 확인
3. `git diff main..HEAD` — 전체 변경사항 확인

PR 형식:

개요
(변경사항 한 줄 요약)
변경 내용

변경 내용 1
변경 내용 2

작업 배경
(왜 이 작업이 필요했는지)
테스트
[ ] 테스트 항목 1
[ ] 테스트 항목 2

규칙:
- 제목은 브랜치명 기반으로 한글 작성
- main 브랜치와 비교 불가 시 `git diff HEAD~1..HEAD` 로 대체
- 작성 완료 후 전체 내용을 `pbcopy` 명령으로 클립보드에 복사해줘 만약 복사에 실패한다면 `ai/git-pr.md`에 작성해줘
