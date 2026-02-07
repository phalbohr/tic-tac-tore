<script setup lang="ts">
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-50">
    <div class="max-w-md w-full space-y-8 p-10 bg-white rounded-xl shadow-lg border border-gray-100">
      <div class="text-center">
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900">
          Tic-Tac-Tore
        </h2>
        <p class="mt-2 text-sm text-gray-600">
          Sign in to track your matches
        </p>
      </div>
      
      <div class="mt-8">
        <a 
          href="http://localhost:8080/oauth2/authorization/google"
          class="group relative w-full flex justify-center py-3 px-4 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors duration-200"
        >
          Login with Google
        </a>
      </div>
    </div>
  </div>
</template>
