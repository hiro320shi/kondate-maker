<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api/index.js'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const pageTitle = computed(() => isEdit.value ? 'レシピを編集' : 'レシピを登録')

const categories = ref([])
const loading = ref(false)
const submitting = ref(false)
const error = ref('')

const form = ref({
  name: '',
  categoryId: '',
  point: '',
  memo: '',
})

const POINT_OPTIONS = ['0.1', '0.2', '0.3', '0.4', '0.5', '0.6', '0.7', '0.8', '0.9', '1.0']

onMounted(async () => {
  loading.value = true
  try {
    categories.value = await api.categories.getAll()

    if (isEdit.value) {
      const id = route.params.id
      const recipes = await api.recipes.getAll()
      const recipe = recipes.find((r) => String(r.id) === String(id))
      if (!recipe) {
        error.value = 'レシピが見つかりません'
        return
      }
      form.value = {
        name: recipe.name,
        categoryId: String(recipe.categoryId),
        point: String(recipe.point),
        memo: recipe.memo || '',
      }
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})

async function submit() {
  error.value = ''
  submitting.value = true
  try {
    const payload = {
      name: form.value.name,
      categoryId: Number(form.value.categoryId),
      point: Number(form.value.point),
      memo: form.value.memo || null,
    }

    if (isEdit.value) {
      await api.recipes.update(route.params.id, payload)
    } else {
      await api.recipes.create(payload)
    }

    router.push('/recipes')
  } catch (e) {
    error.value = e.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-lg mx-auto px-4 py-6">
    <div class="flex items-center gap-3 mb-6">
      <RouterLink to="/recipes" class="text-gray-400 hover:text-gray-600 transition-colors">
        ← 戻る
      </RouterLink>
      <h1 class="text-xl font-bold text-gray-800">{{ pageTitle }}</h1>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">読み込み中...</div>

    <form v-else @submit.prevent="submit" class="bg-white border border-gray-200 rounded-xl p-6 space-y-5">
      <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">
        {{ error }}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          料理名 <span class="text-red-500">*</span>
        </label>
        <input
          v-model="form.name"
          type="text"
          required
          maxlength="50"
          class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
          placeholder="例: 鶏の唐揚げ"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          カテゴリ <span class="text-red-500">*</span>
        </label>
        <select
          v-model="form.categoryId"
          required
          class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
        >
          <option value="">選択してください</option>
          <option v-for="cat in categories" :key="cat.id" :value="String(cat.id)">
            {{ cat.name }}
          </option>
        </select>
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          ポイント <span class="text-red-500">*</span>
        </label>
        <select
          v-model="form.point"
          required
          class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
        >
          <option value="">選択してください</option>
          <option v-for="p in POINT_OPTIONS" :key="p" :value="p">{{ p }}</option>
        </select>
        <p class="text-xs text-gray-400 mt-1">1食の構成比（0.1〜1.0）</p>
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">メモ</label>
        <textarea
          v-model="form.memo"
          rows="3"
          maxlength="500"
          class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
          placeholder="レシピのポイントや参考URLなど（任意）"
        />
      </div>

      <div class="flex gap-3 pt-2">
        <RouterLink
          to="/recipes"
          class="flex-1 text-center border border-gray-300 text-gray-600 hover:bg-gray-50 font-medium rounded-lg py-2 text-sm transition-colors"
        >
          キャンセル
        </RouterLink>
        <button
          type="submit"
          :disabled="submitting"
          class="flex-1 bg-green-600 hover:bg-green-700 disabled:opacity-50 text-white font-medium rounded-lg py-2 text-sm transition-colors"
        >
          {{ submitting ? '保存中...' : (isEdit ? '更新する' : '登録する') }}
        </button>
      </div>
    </form>
  </div>
</template>
