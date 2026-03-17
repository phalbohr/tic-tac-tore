<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const showJwt = () => {
  if (authStore.token) {
    alert(`Current JWT Token:\n\n${authStore.token}`)
    console.log('JWT Token:', authStore.token)
  } else {
    alert('No token found. Please login first.')
  }
}
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-50 p-4">
    <div class="max-w-md w-full space-y-8 p-10 bg-white rounded-xl shadow-lg border border-gray-100">
      <div class="text-center">
        <h1 class="text-4xl font-black text-indigo-600 tracking-tight">
          Tic-Tac-Tore
        </h1>
        <p class="mt-2 text-sm text-gray-500 font-medium">
          Sign in to track your matches and climb the leaderboard
        </p>
      </div>
      
      <div class="mt-8 space-y-4">
        <!-- Standard Google Login (OAuth2) -->
        <a 
          href="http://localhost:8080/oauth2/authorization/google"
          class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-bold rounded-lg text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-all duration-200 shadow-md hover:shadow-lg active:scale-[0.98]"
        >
          Login with Google
        </a>

        <!-- Debugging Utilities -->
        <div class="pt-6 border-t border-gray-100 space-y-3">
          <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider text-center">
            Development Tools
          </p>
          <div class="grid grid-cols-2 gap-3">
            <button
              @click="showJwt"
              class="flex justify-center py-2 px-4 border border-gray-300 text-xs font-bold rounded-md text-gray-600 bg-white hover:bg-gray-50 transition-colors"
            >
              Show JWT
            </button>
            <a 
              href="http://localhost:8080/oauth2/authorization/google"
              class="flex justify-center py-2 px-4 border border-gray-300 text-xs font-bold rounded-md text-gray-600 bg-white hover:bg-gray-50 transition-colors"
            >
              Direct Login
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer for context -->
    <div class="mt-8 text-center text-xs text-gray-400">
      <p>Running in development mode</p>
    </div>
  </div>
</template>
