<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const seedData = ref<any>(null)

const handleSeed = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/v1/dev/seed', { method: 'POST' })
    const data = await response.json()
    seedData.value = data
  } catch (err) {
    console.error('Navigation Seed failed:', err)
  }
}

const loginAs = (key: 'me' | 'opp1') => {
  if (!seedData.value) return
  const u = seedData.value[key]
  authStore.login(u.token, {
    id: u.id,
    name: u.name,
    email: u.email
  })
  router.push('/dashboard')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/')
}

onMounted(handleSeed)
</script>

<template>
  <nav class="bg-white border-b border-gray-100 sticky top-0 z-50 shadow-sm font-sans">
    <div class="max-w-6xl mx-auto px-4">
      <div class="flex justify-between h-16">
        <div class="flex items-center gap-1 sm:gap-2">
          <!-- Main Links -->
          <div class="flex items-center gap-1 overflow-x-auto no-scrollbar py-2">
            <router-link to="/" class="px-3 py-2 rounded-lg text-xs font-black uppercase tracking-wider transition-colors hover:bg-gray-50 whitespace-nowrap shrink-0" :class="[route.path === '/' ? 'text-indigo-600 bg-indigo-50' : 'text-gray-500']">
              Home
            </router-link>
            <router-link to="/dev-recording" data-testid="nav-record-match" class="px-3 py-2 rounded-lg text-xs font-black uppercase tracking-wider transition-colors hover:bg-gray-50 whitespace-nowrap shrink-0" :class="[route.path === '/dev-recording' ? 'text-indigo-600 bg-indigo-50' : 'text-gray-500']">
              New Match
            </router-link>
            <router-link to="/dashboard" class="px-3 py-2 rounded-lg text-xs font-black uppercase tracking-wider transition-colors hover:bg-gray-50 whitespace-nowrap shrink-0" :class="[route.path === '/dashboard' ? 'text-indigo-600 bg-indigo-50' : 'text-gray-500']">
              Dashboard
            </router-link>
            <router-link to="/leaderboards" class="px-3 py-2 rounded-lg text-xs font-black uppercase tracking-wider transition-colors hover:bg-gray-50 whitespace-nowrap shrink-0" :class="[route.path === '/leaderboards' ? 'text-indigo-600 bg-indigo-50' : 'text-gray-500']">
              Leaderboard
            </router-link>
            <router-link to="/token" class="px-3 py-2 rounded-lg text-xs font-black uppercase tracking-wider transition-colors hover:bg-gray-50 whitespace-nowrap shrink-0" :class="[route.path === '/token' ? 'text-indigo-600 bg-indigo-50' : 'text-gray-500']">
              Token
            </router-link>
          </div>
        </div>

        <div class="flex items-center gap-2 shrink-0 ml-4 border-l border-gray-50 pl-4">
          <template v-if="authStore.isAuthenticated">
            <div class="hidden lg:flex flex-col items-end mr-2">
              <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest leading-none mb-0.5">Player</span>
              <span class="text-xs font-black text-gray-900 uppercase tracking-tight">{{ authStore.user?.name }}</span>
            </div>
            <button
              @click="handleLogout"
              class="px-4 py-2 bg-red-50 hover:bg-red-100 text-red-600 font-black rounded-lg border border-red-100 transition-all active:scale-95 text-xs uppercase tracking-wider"
            >
              Logout
            </button>
          </template>
          <template v-else>
            <div class="flex items-center gap-2">
              <button
                @click="loginAs('me')"
                class="px-3 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-black rounded-lg transition-all active:scale-95 text-xs uppercase tracking-wider whitespace-nowrap"
              >
                Pavel
              </button>
              <button
                @click="loginAs('opp1')"
                class="px-3 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-black rounded-lg transition-all active:scale-95 text-xs uppercase tracking-wider whitespace-nowrap"
              >
                Acc B
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
