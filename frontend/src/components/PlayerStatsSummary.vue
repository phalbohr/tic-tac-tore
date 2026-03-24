<script setup lang="ts">
import type { PlayerStats } from '@/services/statisticsService'

defineProps<{
  stats: PlayerStats
}>()
</script>

<template>
  <div class="space-y-6">
    <!-- Overall Stats Card -->
    <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-lg font-black text-gray-900 uppercase tracking-tighter">Your Performance</h2>
        <span class="px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full text-xs font-black">
          Overall Win Rate: {{ stats.overall.winRate.toFixed(1) }}%
        </span>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="bg-gray-50 p-4 rounded-xl">
          <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Total Matches</p>
          <p class="text-2xl font-black text-gray-900">{{ stats.overall.matches }}</p>
        </div>
        <div class="bg-green-50 p-4 rounded-xl">
          <p class="text-[10px] font-black text-green-400 uppercase tracking-widest mb-1">Wins</p>
          <p class="text-2xl font-black text-green-700">{{ stats.overall.wins }}</p>
        </div>
        <div class="bg-red-50 p-4 rounded-xl">
          <p class="text-[10px] font-black text-red-400 uppercase tracking-widest mb-1">Losses</p>
          <p class="text-2xl font-black text-red-700">{{ stats.overall.losses }}</p>
        </div>
        <div class="bg-indigo-50 p-4 rounded-xl">
          <p class="text-[10px] font-black text-indigo-400 uppercase tracking-widest mb-1">Win Rate</p>
          <p class="text-2xl font-black text-indigo-700">{{ stats.overall.winRate.toFixed(1) }}%</p>
        </div>
      </div>
    </div>

    <!-- Position Breakdown -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Attacker Stats -->
      <div data-test="attacker-stats" class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 bg-orange-100 rounded-xl flex justify-center items-center text-orange-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <div>
            <h3 class="font-black text-gray-900 uppercase tracking-tight text-sm">Attacker</h3>
            <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest">Offensive Efficiency</p>
          </div>
        </div>

        <div class="flex items-end justify-between">
          <div>
            <span class="text-3xl font-black text-gray-900">{{ stats.attacker.winRate.toFixed(1) }}%</span>
            <span class="ml-2 text-xs font-bold text-gray-400">Win Rate</span>
          </div>
          <div class="text-right">
            <p class="text-sm font-bold text-gray-600">{{ stats.attacker.wins }} wins</p>
            <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">{{ stats.attacker.matches }} matches total</p>
          </div>
        </div>

        <div class="mt-4 h-2 bg-gray-100 rounded-full overflow-hidden">
          <div 
            class="h-full bg-orange-500 rounded-full" 
            :style="{ width: `${Math.min(Math.max(stats.attacker.winRate, 0), 100)}%` }"
          ></div>
        </div>
      </div>

      <!-- Defender Stats -->
      <div data-test="defender-stats" class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 bg-blue-100 rounded-xl flex justify-center items-center text-blue-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.040L3 14.535a9.235 9.235 0 00.178 1.903 9.198 9.198 0 001.69 3.016 11.032 11.032 0 004.832 3.419 11.037 11.037 0 006.293 0 11.034 11.034 0 004.832-3.419 9.197 9.197 0 001.69-3.016 9.236 9.236 0 00.178-1.903l-0.382-8.551z" />
            </svg>
          </div>
          <div>
            <h3 class="font-black text-gray-900 uppercase tracking-tight text-sm">Defender</h3>
            <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest">Protective Mastery</p>
          </div>
        </div>

        <div class="flex items-end justify-between">
          <div>
            <span class="text-3xl font-black text-gray-900">{{ stats.defender.winRate.toFixed(1) }}%</span>
            <span class="ml-2 text-xs font-bold text-gray-400">Win Rate</span>
          </div>
          <div class="text-right">
            <p class="text-sm font-bold text-gray-600">{{ stats.defender.wins }} wins</p>
            <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">{{ stats.defender.matches }} matches total</p>
          </div>
        </div>

        <div class="mt-4 h-2 bg-gray-100 rounded-full overflow-hidden">
          <div 
            class="h-full bg-blue-500 rounded-full" 
            :style="{ width: `${Math.min(Math.max(stats.defender.winRate, 0), 100)}%` }"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>
