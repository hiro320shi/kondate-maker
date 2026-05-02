<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth.js'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <nav v-if="auth.isAuthenticated" class="bg-white border-b border-gray-200">
      <div class="max-w-4xl mx-auto px-4 h-14 flex items-center justify-between">
        <RouterLink to="/recipes" class="text-lg font-bold text-green-600">
          献立メーカー
        </RouterLink>
        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-500">{{ auth.email }}</span>
          <button
            @click="logout"
            class="text-sm text-gray-600 hover:text-gray-900 transition-colors"
          >
            ログアウト
          </button>
        </div>
      </div>
    </nav>

    <RouterView />
  </div>
</template>
