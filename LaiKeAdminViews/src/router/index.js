import Vue from 'vue'
import Router from 'vue-router'
import store from '../store'
Vue.use(Router)

/* Layout */
import Layout from '@/layout'
import {render} from 'less'

// 公共路由
export const constantRoutes = [
  // 登录
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  // 商城登录
  {
    path: '/mallLogin',
    component: () => import('@/views/mallLogin/index'),
    hidden: true
  },

  // 打印
  {
    path: '/print',
    component: () => import('@/views/print/index'),
    hidden: true
  },
  // 电子面单打印
  {
    path: '/printSheet',
    component: () => import('@/views/printSheet/index'),
    hidden: true
  },

  // 404
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/404'),
    hidden: true
  },

  // 系统维护
  {
    path: '/systemMaintenance',
    component: () => import('@/views/systemMaintenance'),
    hidden: true
  },

  // 楼层列表
  {
    path: '/goods',
    component: Layout,
    hidden: false,
    children: [
      {
        path: 'floor/floorList',
        name: 'FloorList',
        component: () => import('@/views/goods/floor/floorList/index'),
        meta: { title: '楼层管理', icon: 'list' }
      }
    ]
  },

  // 首页
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index'),
      meta: {title: '首页', icon: 'dashboard'}
    }]
  },

]

// 动态路由
export const asyncRoutes = [

]


const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({y: 0}),
  routes: constantRoutes
})


const router = createRouter()

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

router.$addRoutes = (params) => {
  router.matcher = new Router({mode: 'hash'}).matcher;
  router.addRoutes(params)
}

export default router
