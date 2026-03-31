<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>

<template>
  <div class="relative overflow-hidden bg-white">
    <!-- Background Decor -->
    <div class="absolute top-0 left-1/2 -translate-x-1/2 w-full h-full pointer-events-none overflow-hidden opacity-10">
      <div class="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-indigo-600 rounded-full blur-3xl"></div>
      <div class="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-emerald-500 rounded-full blur-3xl"></div>
    </div>

    <div class="relative max-w-6xl mx-auto px-4 py-24 sm:py-32">
      <div class="text-center">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-50 border border-indigo-100 mb-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
          <span class="w-2 h-2 bg-indigo-600 rounded-full animate-pulse"></span>
          <span class="text-[10px] font-black text-indigo-600 uppercase tracking-widest">Office League Live</span>
        </div>
        
        <h1 class="text-5xl sm:text-7xl font-black text-gray-900 uppercase tracking-tighter mb-6 leading-[0.9] animate-in fade-in slide-in-from-bottom-6 duration-1000">
          Foosball <br/>
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-emerald-500">Statistics</span> <br/>
          Platform
        </h1>
        
        <p class="max-w-2xl mx-auto text-lg text-gray-500 font-medium mb-12 animate-in fade-in slide-in-from-bottom-8 duration-1000 delay-200">
          Transform your casual office matches into a professional league. Track every goal, analyze positional efficiency, and dominate the leaderboard.
        </p>

        <div class="flex flex-col sm:flex-row items-center justify-center gap-4 animate-in fade-in slide-in-from-bottom-10 duration-1000 delay-300">
          <template v-if="authStore.isAuthenticated">
            <router-link to="/dev-recording" class="w-full sm:w-auto px-8 py-4 bg-gray-900 text-white font-black rounded-2xl shadow-xl shadow-gray-200 hover:bg-black transition-all active:scale-95 uppercase tracking-tight">
              Record Match
            </router-link>
            <router-link to="/leaderboards" class="w-full sm:w-auto px-8 py-4 bg-white text-gray-900 font-black rounded-2xl border-2 border-gray-100 hover:border-gray-200 transition-all active:scale-95 uppercase tracking-tight">
              View Rankings
            </router-link>
          </template>
          <template v-else>
            <p class="text-sm font-bold text-gray-400 uppercase tracking-widest">
              Please use the navigation bar to login and start tracking
            </p>
          </template>
        </div>
      </div>

      <!-- Feature Grid -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mt-32 border-t border-gray-100 pt-24">
        <div class="p-8 bg-gray-50/50 rounded-3xl border border-gray-100/50 hover:bg-white hover:shadow-xl hover:shadow-indigo-100/50 transition-all group duration-500">
          <div class="w-12 h-12 bg-indigo-100 text-indigo-600 rounded-2xl flex items-center justify-center text-xl mb-6 group-hover:scale-110 transition-transform">🏆</div>
          <h3 class="text-sm font-black text-gray-900 uppercase tracking-tight mb-2">Live Leaderboards</h3>
          <p class="text-xs text-gray-500 font-bold leading-relaxed">Dynamic rankings for Overall, Attacker, and Defender roles based on verified match performance.</p>
        </div>
        <div class="p-8 bg-gray-50/50 rounded-3xl border border-gray-100/50 hover:bg-white hover:shadow-xl hover:shadow-emerald-100/50 transition-all group duration-500">
          <div class="w-12 h-12 bg-emerald-100 text-emerald-600 rounded-2xl flex items-center justify-center text-xl mb-6 group-hover:scale-110 transition-transform">📊</div>
          <h3 class="text-sm font-black text-gray-900 uppercase tracking-tight mb-2">Advanced Analytics</h3>
          <p class="text-xs text-gray-500 font-bold leading-relaxed">Detailed Head-to-Head breakdowns and positional win rates to help you find your perfect partner.</p>
        </div>
        <div class="p-8 bg-gray-50/50 rounded-3xl border border-gray-100/50 hover:bg-white hover:shadow-xl hover:shadow-amber-100/50 transition-all group duration-500">
          <div class="w-12 h-12 bg-amber-100 text-amber-600 rounded-2xl flex items-center justify-center text-xl mb-6 group-hover:scale-110 transition-transform">🤝</div>
          <h3 class="text-sm font-black text-gray-900 uppercase tracking-tight mb-2">Social Proof</h3>
          <p class="text-xs text-gray-500 font-bold leading-relaxed">Mandatory multi-player approval system ensuring data integrity and fair competition across the office.</p>
        </div>
      </div>
    </div>
  </div>
</template>
