import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login',    name: 'Login',    component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/dashboard',name: 'Dashboard',component: () => import('../views/Dashboard.vue'), meta: { auth: true } },
  { path: '/vocabulary',name: 'Vocabulary',component: () => import('../views/Vocabulary.vue'), meta: { auth: true } },
  { path: '/writing',  name: 'Writing',  component: () => import('../views/Writing.vue'), meta: { auth: true } },
  { path: '/reading',  name: 'Reading',  component: () => import('../views/Reading.vue'), meta: { auth: true } },
  { path: '/wordbank', name: 'WordBank', component: () => import('../views/WordBank.vue'), meta: { auth: true } },
  { path: '/papers',   name: 'Papers',   component: () => import('../views/Papers.vue'), meta: { auth: true } },
]

const router = createRouter({ history: createWebHashHistory(), routes })

// 路由守卫：未登录跳登录页
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.auth && !token) next('/login')
  else if ((to.path === '/login' || to.path === '/register') && token) next('/dashboard')
  else next()
})

export default router
