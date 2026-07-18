import re

# 1. ScoreEntry.vue
score_entry = "frontend/src/features/match/components/ScoreEntry.vue"
with open(score_entry, "r") as f:
    se = f.read()

se = se.replace("v-if=\"store.isGameComplete\"", ":disabled=\"!store.isGameComplete\"")

with open(score_entry, "w") as f:
    f.write(se)

# 2. ScoreStepper.vue
stepper_file = "frontend/src/features/match/components/ScoreStepper.vue"
with open(stepper_file, "r") as f:
    stepper = f.read()

# Original patch: v-if="scoreLimit >= 5 && (scoreLimit - score) >= 5"
# I need to change it back to v-if="scoreLimit >= 5" and add the class binding
stepper = stepper.replace("v-if=\"scoreLimit >= 5 && (scoreLimit - score) >= 5\"", "v-if=\"scoreLimit >= 5\"\n            :class=\"{ 'invisible': (scoreLimit - score) < 5 }\"")

with open(stepper_file, "w") as f:
    f.write(stepper)

# 3. matchDraftStore.ts
store_file = "frontend/src/features/match/stores/matchDraftStore.ts"
with open(store_file, "r") as f:
    store = f.read()

store = store.replace("if (isGameComplete.value) return // Block increments if game complete\n    ", "")

with open(store_file, "w") as f:
    f.write(store)

