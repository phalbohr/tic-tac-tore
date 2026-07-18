import re
import os

# 1. ScoreStepper.vue
stepper_file = "frontend/src/features/match/components/ScoreStepper.vue"
with open(stepper_file, "r") as f:
    stepper = f.read()

stepper = stepper.replace("class=\"bg-surface-container-highest", "aria-label=\"Add 5\"\n            class=\"disabled:opacity-50 disabled:cursor-not-allowed bg-surface-container-highest", 1)
stepper = stepper.replace("class=\"bg-surface-container-highest", "aria-label=\"Add 1\"\n            class=\"disabled:opacity-50 disabled:cursor-not-allowed bg-surface-container-highest", 1)
stepper = stepper.replace("@click=\"emit('decrement')\"", "@click=\"emit('decrement')\"\n            :disabled=\"score === 0\"\n            aria-label=\"Subtract 1\"")
stepper = stepper.replace("class=\"bg-surface-container-highest", "class=\"disabled:opacity-50 disabled:cursor-not-allowed bg-surface-container-highest", 1)

with open(stepper_file, "w") as f:
    f.write(stepper)

# 2. matchDraftStore.ts
store_file = "frontend/src/features/match/stores/matchDraftStore.ts"
with open(store_file, "r") as f:
    store = f.read()

store = store.replace("export const useMatchDraftStore", """export const useMatchDraftStore""")

# Add fetchedPlayers
store = store.replace("const frequentOpponents = ref<PlayerDto[]>([])", """const frequentOpponents = ref<PlayerDto[]>([])
  const fetchedPlayers = ref<Record<string, PlayerDto>>({})""")

# loadRuleConfig
old_loadRuleConfig = """  async function loadRuleConfig() {
    try {
      const res = await fetch(`/api/rules/${ruleSystem.value}`)
      if (res.ok) {
        ruleConfig.value = await res.json()
      } else {
        throw new Error('API failed')
      }
    } catch (e) {
      console.warn('Failed to load rule config, falling back to Standard rules', e)
      ruleConfig.value = { scoreLimit: 10, gameLimit: 1, winsNeeded: 1 }
    }
  }"""

new_loadRuleConfig = """  async function loadRuleConfig(signal?: AbortSignal) {
    try {
      const res = await fetch(`/api/rules/${encodeURIComponent(ruleSystem.value)}`, { signal })
      if (res.ok) {
        const data = await res.json()
        if (typeof data.scoreLimit !== 'number' && typeof data.scoreLimit !== 'string') {
           throw new Error('Invalid numeric fields in rule config')
        }
        ruleConfig.value = { 
          scoreLimit: Number(data.scoreLimit), 
          gameLimit: Number(data.gameLimit), 
          winsNeeded: Number(data.winsNeeded) 
        }
      } else {
        throw new Error('API failed')
      }
    } catch (e: any) {
      if (e.name === 'AbortError') throw e;
      console.error('Failed to load rule config', e)
      throw e;
    }
  }"""
store = store.replace(old_loadRuleConfig, new_loadRuleConfig)

# incrementScore & decrementScore & AutoCompletion
old_score_logic = """  function incrementScore(team: 1 | 2, amount: number) {
    if (matchState.value === 'ready_for_submission') return
    const limit = ruleConfig.value?.scoreLimit ?? 10
    if (team === 1) {
      currentGame.value.team1Score = Math.min(currentGame.value.team1Score + amount, limit)
    } else {
      currentGame.value.team2Score = Math.min(currentGame.value.team2Score + amount, limit)
    }
    checkAutoCompletion()
  }

  function decrementScore(team: 1 | 2) {
    if (team === 1) {
      currentGame.value.team1Score = Math.max(0, currentGame.value.team1Score - 1)
    } else {
      currentGame.value.team2Score = Math.max(0, currentGame.value.team2Score - 1)
    }
  }

  function checkAutoCompletion() {
    const limit = ruleConfig.value?.scoreLimit ?? 10
    if (currentGame.value.team1Score >= limit || currentGame.value.team2Score >= limit) {
      games.value.push({ ...currentGame.value })
      currentGame.value = { team1Score: 0, team2Score: 0 }
      
      const winsNeeded = ruleConfig.value?.winsNeeded ?? 1
      const gameLimit = ruleConfig.value?.gameLimit ?? 1
      
      const team1Wins = games.value.filter(g => g.team1Score > g.team2Score).length
      const team2Wins = games.value.filter(g => g.team2Score > g.team1Score).length
      
      if (team1Wins >= winsNeeded || team2Wins >= winsNeeded || games.value.length >= gameLimit) {
        matchState.value = 'ready_for_submission'
      }
    }
  }"""

new_score_logic = """  import { computed } from 'vue'

  const isGameComplete = computed(() => {
    const limit = ruleConfig.value?.scoreLimit ?? 10
    return currentGame.value.team1Score >= limit || currentGame.value.team2Score >= limit
  })

  const isMatchComplete = computed(() => {
    const winsNeeded = ruleConfig.value?.winsNeeded ?? 1
    const gameLimit = ruleConfig.value?.gameLimit ?? 1
    
    let t1w = games.value.filter(g => g.team1Score > g.team2Score).length
    let t2w = games.value.filter(g => g.team2Score > g.team1Score).length
    
    if (isGameComplete.value) {
       if (currentGame.value.team1Score > currentGame.value.team2Score) t1w++;
       else if (currentGame.value.team2Score > currentGame.value.team1Score) t2w++;
    }
    
    return t1w >= winsNeeded || t2w >= winsNeeded || (games.value.length + 1) >= gameLimit
  })

  function incrementScore(team: 1 | 2, amount: number) {
    if (matchState.value === 'ready_for_submission') return
    if (isGameComplete.value) return // Block increments if game complete
    
    const limit = ruleConfig.value?.scoreLimit ?? 10
    if (team === 1) {
      currentGame.value.team1Score = Math.min(currentGame.value.team1Score + amount, limit)
    } else {
      currentGame.value.team2Score = Math.min(currentGame.value.team2Score + amount, limit)
    }
  }

  function decrementScore(team: 1 | 2) {
    if (team === 1) {
      currentGame.value.team1Score = Math.max(0, currentGame.value.team1Score - 1)
    } else {
      currentGame.value.team2Score = Math.max(0, currentGame.value.team2Score - 1)
    }
  }

  function completeCurrentGame() {
    if (!isGameComplete.value) return;
    
    if (isMatchComplete.value) {
      matchState.value = 'ready_for_submission'
    } else {
      games.value.push({ ...currentGame.value })
      currentGame.value = { team1Score: 0, team2Score: 0 }
    }
  }"""
store = store.replace(old_score_logic, new_score_logic)

# fetchPlayer profile
store = store.replace("function addPlayer(playerId: string) {", """  async function fetchPlayer(id: string) {
    if (frequentOpponents.value.find(o => o.id === id)) return;
    if (fetchedPlayers.value[id]) return;
    try {
      const res = await fetch(`/api/v1/players/${id}`);
      if (res.ok) {
        fetchedPlayers.value[id] = await res.json();
      }
    } catch (e) {
      console.warn('Failed to fetch player profile', e);
    }
  }

  function addPlayer(playerId: string) {""")

store = store.replace("selectedPlayers.value.push(playerId)", "selectedPlayers.value.push(playerId)\n      fetchPlayer(playerId)")

# return block
store = store.replace("frequentOpponents,", "frequentOpponents,\n    fetchedPlayers,\n    isGameComplete,\n    isMatchComplete,\n    completeCurrentGame,")

with open(store_file, "w") as f:
    f.write(store)


# 3. ScoreEntry.vue
score_entry_file = "frontend/src/features/match/components/ScoreEntry.vue"
with open(score_entry_file, "r") as f:
    score_entry = f.read()

score_entry = score_entry.replace("const opp = store.frequentOpponents.find(o => o.id === id)\n  return opp ? opp.nickname : `Player ${id.substring(0, 4)}`", """const opp = store.frequentOpponents.find(o => o.id === id)
  if (opp) return opp.nickname
  const fetched = store.fetchedPlayers[id]
  if (fetched) return fetched.nickname
  return `Player ${id.substring(0, 4)}`""")

# Remove flex from h3 line-clamp
score_entry = score_entry.replace("class=\"text-on-surface font-bold text-center mb-4 line-clamp-2 h-12 w-full flex items-center justify-center break-words\"", "class=\"text-on-surface font-bold text-center mb-4 h-12 w-full block overflow-hidden text-ellipsis line-clamp-2 break-words\"")

# Add past games
old_header = """<div class="text-on-surface-variant text-base font-bold flex flex-col items-center">
        <span>Match Score</span>
        <span class="text-xl text-on-surface">{{ team1Wins }} - {{ team2Wins }}</span>
      </div>
    </div>"""

new_header = """<div class="text-on-surface-variant text-base font-bold flex flex-col items-center">
        <span>Match Score</span>
        <span class="text-xl text-on-surface">{{ team1Wins }} - {{ team2Wins }}</span>
      </div>
    </div>
    
    <div v-if="store.games.length > 0" class="flex flex-col items-center mb-4 gap-1">
      <div v-for="(g, idx) in store.games" :key="idx" class="text-sm text-on-surface-variant">
        Game {{ idx + 1 }}: {{ g.team1Score }} - {{ g.team2Score }}
      </div>
    </div>"""
score_entry = score_entry.replace(old_header, new_header)

# Add complete button
old_template_end = "    </div>\n  </div>\n</template>"
new_template_end = """    </div>
    
    <BaseButton 
      v-if="store.isGameComplete"
      @click="store.completeCurrentGame()"
      class="w-full mt-6"
    >
      {{ store.isMatchComplete ? 'Complete Match' : 'Next Game' }}
    </BaseButton>
  </div>
</template>"""
score_entry = score_entry.replace(old_template_end, new_template_end)

with open(score_entry_file, "w") as f:
    f.write(score_entry)


# 4. NewMatchFlow.vue
flow_file = "frontend/src/features/match/components/NewMatchFlow.vue"
with open(flow_file, "r") as f:
    flow = f.read()

# Add abortController logic
old_submit = """const isSubmitting = ref(false)
async function submitMatchDraft() {
  if (isSubmitting.value) return
  isSubmitting.value = true
  try {
    await store.loadRuleConfig()
    store.beginScoreEntry()
  } finally {
    isSubmitting.value = false
  }
}

function handleCancel() {
  store.reset()
  emit('cancel')
}"""

new_submit = """const isSubmitting = ref(false)
const errorMsg = ref('')
let abortController: AbortController | null = null

async function submitMatchDraft() {
  if (isSubmitting.value) return
  if (store.selectedPlayers.length !== (store.matchType === '1v1' ? 2 : 4)) return
  isSubmitting.value = true
  errorMsg.value = ''
  abortController = new AbortController()
  try {
    await store.loadRuleConfig(abortController.signal)
    if (store.matchState === 'draft') store.beginScoreEntry()
  } catch (e: any) {
    if (e.name !== 'AbortError') {
      errorMsg.value = 'Failed to start match. Check rules config.'
    }
  } finally {
    isSubmitting.value = false
    abortController = null
  }
}

function handleCancel() {
  if (abortController) abortController.abort()
  store.reset()
  emit('cancel')
}"""
flow = flow.replace(old_submit, new_submit)

# Add error rendering and blank state rendering
flow = flow.replace("<PlayerSelection />", "<PlayerSelection />\n    <div v-if=\"errorMsg\" class=\"text-red-500 text-sm mt-2\">{{ errorMsg }}</div>")

flow = flow.replace("v-else-if=\"store.matchState === 'score_entry' || store.matchState === 'ready_for_submission'\"", "v-else-if=\"store.matchState === 'score_entry' || store.matchState === 'ready_for_submission'\"")

flow = flow.replace("</template>", """  
  <div v-else class="w-full flex flex-col items-center bg-surface-container-low rounded-2xl p-4 gap-6">
    <p>Invalid match state. Please try again.</p>
    <BaseButton @click="handleCancel">Go Back</BaseButton>
  </div>
</template>""")

with open(flow_file, "w") as f:
    f.write(flow)

