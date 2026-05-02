import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const routes = [
  { path: '/', redirect: '/recipes' },
  {
    path: '/login',
    component: () => import('../pages/LoginPage.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    component: () => import('../pages/RegisterPage.vue'),
    meta: { guest: true },
  },
  {
    path: '/recipes',
    component: () => import('../pages/RecipeListPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/recipes/new',
    component: () => import('../pages/RecipeFormPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/recipes/:id/edit',
    component: () => import('../pages/RecipeFormPage.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return '/login'
  }
  if (to.meta.guest && auth.isAuthenticated) {
    return '/recipes'
  }
})

export default router
