import re
import os

# 1. matchDraftStore.ts (import, completeCurrentGame, decrementScore)
store_file = "frontend/src/features/match/stores/matchDraftStore.ts"
with open(store_file, "r") as f:
    store = f.read()

# fix import
store = store.replace("import { computed } from 'vue'", "")
store = "import { computed } from 'vue'\n" + store

# fix decrementScore
old_dec = """  function decrementScore(team: 1 | 2) {"""
new_dec = """  function decrementScore(team: 1 | 2) {
    if (matchState.value === 'ready_for_submission') return"""
store = store.replace(old_dec, new_dec)

# fix completeCurrentGame
old_comp = """  function completeCurrentGame() {
    if (!isGameComplete.value) return;
    
    if (isMatchComplete.value) {
      matchState.value = 'ready_for_submission'
    } else {
      games.value.push({ ...currentGame.value })
      currentGame.value = { team1Score: 0, team2Score: 0 }
    }
  }"""
new_comp = """  function completeCurrentGame() {
    if (!isGameComplete.value) return;
    
    const wasMatchComplete = isMatchComplete.value;
    games.value.push({ ...currentGame.value });
    
    if (wasMatchComplete) {
      matchState.value = 'ready_for_submission';
    } else {
      currentGame.value = { team1Score: 0, team2Score: 0 };
    }
  }"""
store = store.replace(old_comp, new_comp)

with open(store_file, "w") as f:
    f.write(store)

# 2. ScoreStepper.vue (+5 button logic)
stepper_file = "frontend/src/features/match/components/ScoreStepper.vue"
with open(stepper_file, "r") as f:
    stepper = f.read()

old_vif = "v-if=\"scoreLimit >= 5\""
new_vif = "v-if=\"scoreLimit >= 5 && (scoreLimit - score) >= 5\""
stepper = stepper.replace(old_vif, new_vif)

with open(stepper_file, "w") as f:
    f.write(stepper)

# 3. ScoreEntry.vue (Game 1 of 3)
score_entry = "frontend/src/features/match/components/ScoreEntry.vue"
with open(score_entry, "r") as f:
    se = f.read()

old_h2 = "<h2 class=\"text-on-surface font-bold text-xl\">Score Entry</h2>"
new_h2 = "<h2 class=\"text-on-surface font-bold text-xl\">Game {{ store.games.length + 1 }} of {{ store.ruleConfig?.gameLimit || '?' }}</h2>"
se = se.replace(old_h2, new_h2)

with open(score_entry, "w") as f:
    f.write(se)


# 4. NewMatchFlow.vue (Cancellation race condition)
flow_file = "frontend/src/features/match/components/NewMatchFlow.vue"
with open(flow_file, "r") as f:
    flow = f.read()

flow = flow.replace("if (store.matchState === 'draft') store.beginScoreEntry()", "if (store.matchState === 'draft' && (!abortController || !abortController.signal.aborted)) store.beginScoreEntry()")

with open(flow_file, "w") as f:
    f.write(flow)


# 5. spec-2-3-score-entry-and-automatic-completion.md (Outdated Spec ACs)
spec_file = "_bmad-output/implementation-artifacts/spec-2-3-score-entry-and-automatic-completion.md"
with open(spec_file, "r") as f:
    spec = f.read()

spec = spec.replace("- **And** game automatically completes", "- **And** game requires manual completion")
spec = spec.replace("- **And** match automatically completes", "- **And** match requires manual completion")
spec = spec.replace("Require manual 'End Game' or 'End Match' clicks if limits are reached (must auto-advance)", "Game and match completion requires explicit user confirmation via 'Complete Game' or 'Complete Match' button to prevent misclicks.")
spec = spec.replace("System automatically completes a game/match when win condition is met", "System requires manual confirmation to complete a game/match when win condition is met to prevent misclicks.")

with open(spec_file, "w") as f:
    f.write(spec)

