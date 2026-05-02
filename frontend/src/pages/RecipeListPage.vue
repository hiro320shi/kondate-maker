<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/index.js'

const router = useRouter()

const recipes = ref([])
const categories = ref([])
const loading = ref(false)
const error = ref('')

const searchName = ref('')
const selectedCategory = ref('')

onMounted(async () => {
  await Promise.all([loadCategories(), loadRecipes()])
})

async function loadCategories() {
  try {
    categories.value = await api.categories.getAll()
  } catch (e) {
    // カテゴリ取得失敗は致命的でないので無視
  }
}

async function loadRecipes() {
  loading.value = true
  error.value = ''
  try {
    recipes.value = await api.recipes.getAll({
      name: searchName.value || undefined,
      categoryId: selectedCategory.value || undefined,
    })
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function deleteRecipe(id) {
  if (!confirm('このレシピを削除しますか？')) return
  try {
    await api.recipes.delete(id)
    recipes.value = recipes.value.filter((r) => r.id !== id)
  } catch (e) {
    alert(e.message)
  }
}

function editRecipe(id) {
  router.push(`/recipes/${id}/edit`)
}

function resetSearch() {
  searchName.value = ''
  selectedCategory.value = ''
  loadRecipes()
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-6">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-bold text-gray-800">レシピ一覧</h1>
      <RouterLink
        to="/recipes/new"
        class="bg-green-600 hover:bg-green-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
      >
        + 新規登録
      </RouterLink>
    </div>

    <!-- 検索・絞り込み -->
    <div class="bg-white border border-gray-200 rounded-xl p-4 mb-4 flex flex-col sm:flex-row gap-3">
      <input
        v-model="searchName"
        type="text"
        placeholder="料理名で検索"
        class="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
        @keydown.enter="loadRecipes"
      />
      <select
        v-model="selectedCategory"
        class="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
      >
        <option value="">すべてのカテゴリ</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.id">
          {{ cat.name }}
        </option>
      </select>
      <div class="flex gap-2">
        <button
          @click="loadRecipes"
          class="bg-green-600 hover:bg-green-700 text-white text-sm px-4 py-2 rounded-lg transition-colors"
        >
          検索
        </button>
        <button
          @click="resetSearch"
          class="border border-gray-300 text-gray-600 hover:bg-gray-50 text-sm px-3 py-2 rounded-lg transition-colors"
        >
          リセット
        </button>
      </div>
    </div>

    <!-- エラー -->
    <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-4">
      {{ error }}
    </div>

    <!-- ローディング -->
    <div v-if="loading" class="text-center py-12 text-gray-400">読み込み中...</div>

    <!-- 空状態 -->
    <div v-else-if="recipes.length === 0" class="text-center py-12 text-gray-400">
      <p class="text-lg mb-2">レシピがありません</p>
      <p class="text-sm">「新規登録」からレシピを追加してみましょう</p>
    </div>

    <!-- レシピ一覧 -->
    <div v-else class="space-y-3">
      <div
        v-for="recipe in recipes"
        :key="recipe.id"
        class="bg-white border border-gray-200 rounded-xl px-4 py-4 flex items-start justify-between gap-4"
      >
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-1">
            <span class="bg-green-100 text-green-700 text-xs font-medium px-2 py-0.5 rounded-full">
              {{ recipe.categoryName }}
            </span>
            <span class="text-xs text-gray-400">{{ recipe.point }} pt</span>
          </div>
          <p class="font-medium text-gray-800 truncate">{{ recipe.name }}</p>
          <p v-if="recipe.memo" class="text-sm text-gray-400 mt-1 line-clamp-2">{{ recipe.memo }}</p>
        </div>
        <div class="flex gap-2 shrink-0">
          <button
            @click="editRecipe(recipe.id)"
            class="text-sm text-gray-600 hover:text-green-600 border border-gray-200 hover:border-green-300 px-3 py-1.5 rounded-lg transition-colors"
          >
            編集
          </button>
          <button
            @click="deleteRecipe(recipe.id)"
            class="text-sm text-gray-600 hover:text-red-600 border border-gray-200 hover:border-red-300 px-3 py-1.5 rounded-lg transition-colors"
          >
            削除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
