# Match Model & Domain Logic

> 22 nodes · cohesion 0.12

## Key Concepts

- **Match** (12 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- **.calculateStatsFromMatches()** (6 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/StatisticsService.java`
- **StatisticsServiceTest** (6 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/StatisticsServiceTest.java`
- **.calculateWinner()** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- **.isWinner()** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- **.calculateStatsFromMatches_MultipleMatchesAndPositions()** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/StatisticsServiceTest.java`
- **DevDataController** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/DevDataController.java`
- **.approve()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- **.isUserInTeamA()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- **.isUserInTeamB()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- **.seedMatches()** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/DevDataController.java`
- **.isAttacker()** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- **.isDefender()** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- **.assertStats()** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/StatisticsServiceTest.java`
- **.createMatch()** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/StatisticsServiceTest.java`
- **DevDataController.java** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/DevDataController.java`
- **Match.java** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- **StatisticsServiceTest.java** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/StatisticsServiceTest.java`
- **.setUp()** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/StatisticsServiceTest.java`
- **DevDataController.java** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/DevDataController.java`
- **Match.java** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- **StatisticsServiceTest.java** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/StatisticsServiceTest.java`

## Relationships

- [[Match Operations & Services]] (3 shared connections)
- [[Statistics Repository & Projections]] (3 shared connections)
- [[Match Controller & Service Implementation]] (2 shared connections)
- [[Coverage Report Formatting]] (1 shared connections)

## Source Files

- `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/DevDataController.java`
- `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/model/Match.java`
- `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/StatisticsService.java`
- `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/StatisticsServiceTest.java`

## Audit Trail

- EXTRACTED: 50 (77%)
- INFERRED: 15 (23%)
- AMBIGUOUS: 0 (0%)

---

*Part of the graphify knowledge wiki. See [[index]] to navigate.*