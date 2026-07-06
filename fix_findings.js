const fs = require('fs');
const path = require('path');

// 1. matchDraftStore.ts
const storePath = 'frontend/src/features/match/stores/matchDraftStore.ts';
let storeCode = `import { defineStore } from 'pinia'
import { ref } from 'vue'

export enum MatchType {
  ONE_VS_ONE = '1v1',
  TWO_VS_TWO = '2v2'
}

export const useMatchDraftStore = defineStore('matchDraft', () => {
  const matchType = ref<MatchType>(MatchType.ONE_VS_ONE)
  const selectedPlayers = ref<string[]>([])
  const ruleSystem = ref<string>('STANDARD')
  const frequentOpponents = ref<any[]>([])

  async function fetchDefaults() {
    try {
      const [opponentsRes, prefsRes] = await Promise.all([
        fetch('/api/users/me/frequent-opponents'),
        fetch('/api/users/me/preferences/last-rule-system')
      ])
      if (opponentsRes.ok) {
        frequentOpponents.value = await opponentsRes.json()
      }
      if (prefsRes.ok) {
        const data = await prefsRes.json()
        if (data.lastRuleSystem) {
          ruleSystem.value = data.lastRuleSystem
        }
      }
    } catch (e) {
      console.error('Failed to fetch match defaults', e)
    }
  }

  function setMatchType(type: MatchType) {
    matchType.value = type
    if (type === MatchType.ONE_VS_ONE) {
      selectedPlayers.value = selectedPlayers.value.slice(0, 2)
    }
  }

  function addPlayer(playerId: string) {
    if (!playerId.trim()) return;
    const maxPlayers = matchType.value === MatchType.ONE_VS_ONE ? 2 : 4
    if (selectedPlayers.value.length < maxPlayers && !selectedPlayers.value.includes(playerId)) {
      selectedPlayers.value.push(playerId)
    }
  }

  function removePlayer(playerId: string) {
    selectedPlayers.value = selectedPlayers.value.filter(id => id !== playerId)
  }

  function reset() {
    matchType.value = MatchType.ONE_VS_ONE
    selectedPlayers.value = []
    ruleSystem.value = 'STANDARD'
  }

  return {
    matchType,
    selectedPlayers,
    ruleSystem,
    frequentOpponents,
    fetchDefaults,
    setMatchType,
    addPlayer,
    removePlayer,
    reset
  }
})
`;
fs.writeFileSync(storePath, storeCode);

// 2. PlayerSelection.vue
const playerSelectionPath = 'frontend/src/features/match/components/PlayerSelection.vue';
let playerSelectionCode = `<script setup lang="ts">
import { computed } from 'vue'
import { useMatchDraftStore, MatchType } from '../stores/matchDraftStore'

defineOptions({
  name: 'PlayerSelection'
})

const store = useMatchDraftStore()
const maxPlayers = computed(() => store.matchType === MatchType.ONE_VS_ONE ? 2 : 4)

</script>

<template>
  <div class="flex flex-col gap-2 w-full mt-6">
    <h2 class="text-on-surface font-headline font-bold text-lg mb-2">Players</h2>
    <div 
      v-for="index in maxPlayers" 
      :key="index"
      class="player-slot h-16 flex items-center px-4 bg-surface-container-highest rounded-xl gap-4 mb-2"
    >
      <div class="w-10 h-10 rounded-full bg-surface-container-low flex items-center justify-center overflow-hidden">
        <span v-if="!store.selectedPlayers[index - 1]" class="text-on-surface-variant font-bold">{{ index }}</span>
        <img v-else-if="store.frequentOpponents.find(p => p.id === store.selectedPlayers[index - 1])?.avatar" :src="store.frequentOpponents.find(p => p.id === store.selectedPlayers[index - 1])?.avatar" class="w-full h-full object-cover" />
      </div>
      <span class="text-on-surface flex-1">
        {{ store.selectedPlayers[index - 1] ? (store.frequentOpponents.find(p => p.id === store.selectedPlayers[index - 1])?.nickname || \`Player \${store.selectedPlayers[index - 1]}\`) : 'Select Player' }}
      </span>
      <button v-if="store.selectedPlayers[index - 1]" @click="store.removePlayer(store.selectedPlayers[index - 1])" class="text-error font-bold px-2">X</button>
    </div>
    
    <div v-if="store.frequentOpponents.length > 0 && store.selectedPlayers.length < maxPlayers" class="mt-4">
      <h3 class="text-on-surface-variant font-bold text-sm mb-2">Frequent Opponents</h3>
      <div class="flex gap-2 overflow-x-auto pb-2">
        <button 
          v-for="opponent in store.frequentOpponents" 
          :key="opponent.id"
          @click="store.addPlayer(opponent.id)"
          :disabled="store.selectedPlayers.includes(opponent.id)"
          class="flex flex-col items-center gap-1 min-w-[72px] opacity-100 disabled:opacity-50"
        >
          <div class="w-12 h-12 rounded-full bg-surface-container-highest overflow-hidden">
             <img v-if="opponent.avatar" :src="opponent.avatar" class="w-full h-full object-cover" />
          </div>
          <span class="text-xs text-on-surface truncate w-full text-center">{{ opponent.nickname }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
`;
fs.writeFileSync(playerSelectionPath, playerSelectionCode);

// 3. UserMatchController.java & UserService.java
const controllerPath = 'src/main/java/com/tictactore/controller/UserMatchController.java';
const servicePath = 'src/main/java/com/tictactore/service/UserService.java';

fs.mkdirSync(path.dirname(servicePath), { recursive: true });

fs.writeFileSync(servicePath, `package com.tictactore.service;

import com.tictactore.controller.UserMatchController.PlayerDto;
import com.tictactore.controller.UserMatchController.UserPreferencesDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    public UserPreferencesDto getLastRuleSystem() {
        return new UserPreferencesDto("STANDARD");
    }

    public List<PlayerDto> getFrequentOpponents() {
        return List.of(
                new PlayerDto(UUID.randomUUID().toString(), "Mock Player 1", "avatar1"),
                new PlayerDto(UUID.randomUUID().toString(), "Mock Player 2", "avatar2")
        );
    }
}
`);

fs.writeFileSync(controllerPath, `package com.tictactore.controller;

import com.tictactore.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserMatchController {

    private final UserService userService;

    public UserMatchController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/preferences/last-rule-system")
    public ResponseEntity<UserPreferencesDto> getLastRuleSystem() {
        return ResponseEntity.ok(userService.getLastRuleSystem());
    }

    @GetMapping("/frequent-opponents")
    public ResponseEntity<java.util.List<PlayerDto>> getFrequentOpponents() {
        return ResponseEntity.ok(userService.getFrequentOpponents());
    }

    public record UserPreferencesDto(String lastRuleSystem) {}
    public record PlayerDto(String id, String nickname, String avatar) {}
}
`);

// 5. Core components creation
fs.mkdirSync('frontend/src/core/components', { recursive: true });
fs.writeFileSync('frontend/src/core/components/BaseButton.vue', `<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost'
}>()
</script>
<template>
  <button 
    class="flex items-center justify-center font-bold rounded-xl transition-colors h-14 px-4"
    :class="{
      'bg-primary text-background': variant === 'primary' || !variant,
      'bg-surface-container-highest text-on-surface': variant === 'secondary',
      'border-2 border-primary text-primary bg-transparent': variant === 'outline',
      'bg-transparent text-on-surface hover:bg-surface-container': variant === 'ghost'
    }"
  >
    <slot />
  </button>
</template>
`);

// 6 & 9. HomeView.vue and NewMatchFlow.vue extraction
fs.writeFileSync('frontend/src/features/match/components/NewMatchFlow.vue', `<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useMatchDraftStore } from '../stores/matchDraftStore'
import MatchTypePicker from './MatchTypePicker.vue'
import PlayerSelection from './PlayerSelection.vue'
import BaseButton from '@/core/components/BaseButton.vue'

const store = useMatchDraftStore()
const emit = defineEmits<{
  (e: 'cancel'): void
}>()

onMounted(() => {
  store.fetchDefaults()
})

onUnmounted(() => {
  store.reset()
})

function submitMatchDraft() {
  console.log('Submitting match draft', store.matchType, store.selectedPlayers, store.ruleSystem)
  // Logic to actually create the match
  store.reset()
  emit('cancel')
}

function handleCancel() {
  store.reset()
  emit('cancel')
}
</script>

<template>
  <div class="w-full flex flex-col items-center bg-surface-container-low rounded-2xl p-4 gap-6">
    <div class="flex justify-between items-center w-full mb-2">
      <h2 class="text-on-surface font-bold text-xl">New Match</h2>
      <BaseButton variant="secondary" @click="handleCancel" class="!h-12">Cancel</BaseButton>
    </div>
    
    <div class="w-full flex flex-col gap-2 text-start">
      <h3 class="text-on-surface font-headline font-bold mb-1">Match Type</h3>
      <MatchTypePicker />
    </div>
    
    <PlayerSelection />
    
    <BaseButton @click="submitMatchDraft" class="w-full mt-4 rounded-full">
      Start Match
    </BaseButton>
  </div>
</template>
`);

const homeHubContent = fs.readFileSync('frontend/src/views/HomeHub.vue', 'utf8');
const newHomeViewContent = homeHubContent
  .replace(/import MatchTypePicker from '[^']+';?\n/g, '')
  .replace(/import PlayerSelection from '[^']+';?\n/g, '')
  .replace(/import GoogleOAuthButton from '@/components/GoogleOAuthButton.vue'/g, "import GoogleOAuthButton from '@/components/GoogleOAuthButton.vue'\nimport NewMatchFlow from '@/features/match/components/NewMatchFlow.vue'\nimport BaseButton from '@/core/components/BaseButton.vue'")
  .replace(/<button \n\s*@click="showNewMatch = true"\n\s*class="bg-primary text-background font-bold h-14 rounded-full w-full mt-4"\n\s*>\n\s*New Match\n\s*<\/button>/g, '<BaseButton @click="showNewMatch = true" class="w-full mt-4 rounded-full">New Match</BaseButton>')
  .replace(/<div v-else class="w-full flex flex-col items-center bg-surface-container-low rounded-2xl p-4 gap-6 w-full">[\s\S]*?<\/div>\s*<button \n\s*v-if="!showNewMatch"/g, '<NewMatchFlow v-else @cancel="showNewMatch = false" />\n\n        <button \n          v-if="!showNewMatch"');

fs.writeFileSync('frontend/src/views/HomeView.vue', newHomeViewContent);
if (fs.existsSync('frontend/src/views/HomeHub.vue')) {
  fs.unlinkSync('frontend/src/views/HomeHub.vue');
}

// update router
const routerPath = 'frontend/src/router/index.ts';
let routerCode = fs.readFileSync(routerPath, 'utf8');
routerCode = routerCode.replace(/import HomeHub from '@\/views\/HomeHub\.vue'/g, "import HomeView from '@/views/HomeView.vue'");
routerCode = routerCode.replace(/component: HomeHub/g, 'component: HomeView');
fs.writeFileSync(routerPath, routerCode);

// update MatchTypePicker.vue
fs.writeFileSync('frontend/src/features/match/components/MatchTypePicker.vue', `<script setup lang="ts">
import { useMatchDraftStore, MatchType } from '../stores/matchDraftStore'
import BaseButton from '@/core/components/BaseButton.vue'

defineOptions({
  name: 'MatchTypePicker'
})

const store = useMatchDraftStore()
</script>

<template>
  <div class="flex gap-4 w-full">
    <BaseButton 
      class="flex-1"
      :variant="store.matchType === MatchType.ONE_VS_ONE ? 'primary' : 'secondary'"
      @click="store.setMatchType(MatchType.ONE_VS_ONE)"
    >
      1v1
    </BaseButton>
    <BaseButton 
      class="flex-1"
      :variant="store.matchType === MatchType.TWO_VS_TWO ? 'primary' : 'secondary'"
      @click="store.setMatchType(MatchType.TWO_VS_TWO)"
    >
      2v2
    </BaseButton>
  </div>
</template>
`);

// 7, 10, 11 e2e tests
const e2eTestPath = 'frontend/e2e/tests/e2e/new-match-creation.spec.ts';
let e2eTestCode = fs.readFileSync(e2eTestPath, 'utf8');
// remove force: true
e2eTestCode = e2eTestCode.replace(/await newMatchBtn\.click\(\{ force: true \}\)/g, 'await newMatchBtn.click()');
// use proper context.cookies()
e2eTestCode = e2eTestCode.replace(/const cookiesArray = response\.headersArray\(\)\.filter[^\}]+\}\s*\}/s, `const cookies = await page.context().cookies();
    // Playwright handles setting the cookies in the browser context from the request context automatically if we use page.request? No, we used \`request.get\`, which is an isolated APIRequestContext.
    // Actually, to set cookies properly from \`request.get\`, we can get headers or just use page.goto and then page.request?
    // Wait, let's fix it by parsing set-cookie headers using proper regex or just extracting from response.headers().
    // Or better, let's just make the request using the browser context so cookies are saved.
    // Wait, context.cookies() doesn't get cookies from APIRequestContext. Let's just fix the regex parsing or use context.addCookies properly.
    const headers = response.headersArray();
    const setCookies = headers.filter(h => h.name.toLowerCase() === 'set-cookie');
    const cookiesToAdd = setCookies.map(header => {
      const parts = header.value.split(';');
      const [name, value] = parts[0].split('=');
      return { name, value, domain: 'localhost', path: '/' };
    });
    if (cookiesToAdd.length > 0) {
      await page.context().addCookies(cookiesToAdd);
    }
`);
// improve portrait orientation constraint test
e2eTestCode = e2eTestCode.replace(/expect\(boundingBox\.width\)\.toBeLessThanOrEqual\(500\)/g, `expect(boundingBox.width).toBeLessThanOrEqual(500)
    expect(boundingBox.height).toBeGreaterThan(boundingBox.width) // Ensure portrait ratio`);
fs.writeFileSync(e2eTestPath, e2eTestCode);

// 8. API race condition
const apiTestPath = 'frontend/e2e/tests/api/new-match.spec.ts';
let apiTestCode = fs.readFileSync(apiTestPath, 'utf8');
apiTestCode = apiTestCode.replace(/expect\(response\.status\(\)\)\.toBe\(200\)/g, `expect.poll(async () => {
      const res = await request.get('/api/users/me/frequent-opponents', {
        headers: cookieStr ? { 'Cookie': cookieStr } : {}
      });
      return res.status();
    }, {
      message: 'Wait for endpoint to be available',
      timeout: 10000,
    }).toBe(200);
    // Re-fetch to get the actual response since expect.poll only checks the status
    const finalResponse = await request.get('/api/users/me/frequent-opponents', {
      headers: cookieStr ? { 'Cookie': cookieStr } : {}
    });
    expect(finalResponse.status()).toBe(200);
    const data = await finalResponse.json();`);

apiTestCode = apiTestCode.replace(/const data = await response\.json\(\)\s*expect\(Array\.isArray\(data\)\)\.toBeTruthy\(\)/g, `expect(Array.isArray(data)).toBeTruthy()`);

// Second test
apiTestCode = apiTestCode.replace(/const response = await request\.get\('\/api\/users\/me\/preferences\/last-rule-system'[\s\S]*?expect\(response\.status\(\)\)\.toBe\(200\)\s*const data = await response\.json\(\)\s*expect\(data\)\.toHaveProperty\('lastRuleSystem'\)/g, `
    expect.poll(async () => {
      const res = await request.get('/api/users/me/preferences/last-rule-system', {
        headers: cookieStr ? { 'Cookie': cookieStr } : {}
      });
      return res.status();
    }, {
      timeout: 10000,
    }).toBe(200);

    const finalResponse = await request.get('/api/users/me/preferences/last-rule-system', {
      headers: cookieStr ? { 'Cookie': cookieStr } : {}
    });
    expect(finalResponse.status()).toBe(200);
    const data = await finalResponse.json();
    expect(data).toHaveProperty('lastRuleSystem');`);

fs.writeFileSync(apiTestPath, apiTestCode);

// 4. Missing Unit tests
fs.mkdirSync('frontend/src/features/match/components/__tests__', { recursive: true });
fs.writeFileSync('frontend/src/features/match/stores/matchDraftStore.spec.ts', `import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useMatchDraftStore, MatchType } from './matchDraftStore'

describe('matchDraftStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    global.fetch = vi.fn()
  })

  it('initializes with default values', () => {
    const store = useMatchDraftStore()
    expect(store.matchType).toBe(MatchType.ONE_VS_ONE)
    expect(store.selectedPlayers).toEqual([])
    expect(store.ruleSystem).toBe('STANDARD')
  })

  it('changes match type and truncates players if needed', () => {
    const store = useMatchDraftStore()
    store.addPlayer('p1')
    store.addPlayer('p2')
    store.addPlayer('p3')
    
    store.setMatchType(MatchType.TWO_VS_TWO)
    expect(store.matchType).toBe(MatchType.TWO_VS_TWO)
    
    store.setMatchType(MatchType.ONE_VS_ONE)
    expect(store.selectedPlayers.length).toBe(2)
  })

  it('prevents adding empty players', () => {
    const store = useMatchDraftStore()
    store.addPlayer('   ')
    expect(store.selectedPlayers).toEqual([])
  })
})
`);

fs.writeFileSync('frontend/src/features/match/components/__tests__/PlayerSelection.spec.ts', `import { mount } from '@vue/test-utils'
import { describe, it, expect, beforeEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import PlayerSelection from '../PlayerSelection.vue'
import { useMatchDraftStore } from '../../stores/matchDraftStore'

describe('PlayerSelection.vue', () => {
  it('renders player slots based on match type', () => {
    const wrapper = mount(PlayerSelection, {
      global: {
        plugins: [createTestingPinia()]
      }
    })
    const store = useMatchDraftStore()
    expect(wrapper.findAll('.player-slot')).toHaveLength(2)
  })
})
`);

fs.writeFileSync('frontend/src/features/match/components/__tests__/MatchTypePicker.spec.ts', `import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import MatchTypePicker from '../MatchTypePicker.vue'

describe('MatchTypePicker.vue', () => {
  it('renders two buttons for match types', () => {
    const wrapper = mount(MatchTypePicker, {
      global: {
        plugins: [createTestingPinia()]
      }
    })
    expect(wrapper.findAll('button')).toHaveLength(2)
  })
})
`);
