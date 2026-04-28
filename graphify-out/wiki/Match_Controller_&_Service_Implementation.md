# Match Controller & Service Implementation

> 19 nodes · cohesion 0.16

## Key Concepts

- **MatchService** (9 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`
- **MatchController** (7 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/MatchController.java`
- **.getCurrentUser()** (6 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`
- **.validateUserIsOpponent()** (5 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`
- **.getPendingMatchesForCurrentUser()** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`
- **MatchApi** (3 connections)
- **.approveMatch()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`
- **.createMatch()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`
- **.rejectMatch()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`
- **.getPendingMatches()** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/MatchController.java`
- **.findPendingApprovalsForUser()** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/repository/MatchRepository.java`
- **.validateCreatorIsParticipant()** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`
- **MatchController.java** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/MatchController.java`
- **MatchController.java** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/MatchController.java`
- **.approveMatch()** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/MatchController.java`
- **.createMatch()** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/MatchController.java`
- **.rejectMatch()** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/MatchController.java`
- **MatchService.java** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`
- **MatchService.java** (1 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`

## Relationships

- [[Match Model & Domain Logic]] (2 shared connections)
- [[Statistics Repository & Projections]] (1 shared connections)
- [[Match Operations & Services]] (1 shared connections)

## Source Files

- `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/MatchController.java`
- `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/repository/MatchRepository.java`
- `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/MatchService.java`

## Audit Trail

- EXTRACTED: 51 (88%)
- INFERRED: 7 (12%)
- AMBIGUOUS: 0 (0%)

---

*Part of the graphify knowledge wiki. See [[index]] to navigate.*