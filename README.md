# Battlegrounds

Paper 26.2용 맵 독립형 PUBG 스타일 아이템·전투 시스템 프로토타입이다.

## 현재 구현

- 설정 기반 총기: AKM, M416, Kar98k, UMP45
- 우클릭 사격, 좌클릭 재장전
- 총기별 피해·사거리·거리 감쇠·탄창·발사 간격
- 머리/몸통/사지 근사 판정
- 총기별 호환 탄약
- 1~3레벨 헬멧·조끼, 피해 감소와 내구도
- 붕대·구급상자·의료용 키트
- 순수 Java 피해·탄창·DBNO/소생 도메인
- `/bg give <id>`, `/bg reload`, `/bg status`

## 요구사항

- Paper `26.2.build.56-alpha`
- Java 25

## 빌드

```bash
mvn clean package
```

생성물: `target/battlegrounds-0.1.0-SNAPSHOT.jar`

## 테스트 방법

1. JAR을 서버 `plugins/`에 넣고 실행한다.
2. `/bg give akm`으로 총기를 받는다.
3. `FLINT`를 탄약으로 준비한다.
4. 우클릭으로 발사하고 좌클릭으로 재장전한다.
5. `/bg give helmet-2`, `/bg give first-aid` 등으로 장비와 회복품을 시험한다.

## 아이템 ID

- 무기: `akm`, `m416`, `kar98k`, `ump45`
- 방어구: `helmet-1..3`, `vest-1..3`
- 회복: `bandage`, `first-aid`, `med-kit`

모든 수치는 `config.yml`에서 변경할 수 있다. 현재는 맵, 매치 상태 머신, 블루존, 차량을 의도적으로 포함하지 않았다.

## 현재 한계

- 총기는 리소스팩 없이 바닐라 아이템으로 표시된다.
- 히트박스는 Paper ray trace 높이 비율로 근사한다.
- DBNO 도메인은 검증되어 있지만 팀/매치 시스템이 없어서 런타임 이벤트에는 아직 연결하지 않았다.
- 회복은 현재 즉시 사용이다. 사용 시간·취소 조건은 매치 코어와 함께 추가할 예정이다.
