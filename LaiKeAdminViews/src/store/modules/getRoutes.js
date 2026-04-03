import {asyncRoutes, constantRoutes} from '@/router'
import {getStorage,setStorage} from '@/utils/storage'
import request from "@/api/https";
import { getUserRoleInfo } from '@/api/layout/information'
import Layout from "@/layout/index";
import {isEmpty} from "element-ui/src/utils/util";

/**
 * 使用 meta.role 确定当前用户是否有权限
 * @param roles
 * @param route
 */
function hasPermission(roles, route) {
  if (route.meta && route.meta.roles) {
    return roles.includes(route.meta.roles)
  } else {
    return true
  }
}

/**
 * 通过递归过滤异步路由表
 * @param routes asyncRoutes
 * @param roles
 */
export function filterAsyncRoutes(routes, roles) {
  const res = []

  routes.forEach(route => {
    const tmp = {...route}
    if (hasPermission(roles, tmp)) {
      if (tmp.children) {
        tmp.children = tmp.children.filter(route => {
          if (hasPermission(roles, route)) {
            return route
          }
        })
      }
      res.push(tmp)
    }
  })

  return res
}

const state = {
  routes: [],
  addRoutes: []
}

const mutations = {
  SET_ROUTES: (state, routes) => {
    state.addRoutes = routes
    state.routes = constantRoutes.concat(routes)
  },

  SET_FIRSTROUTES:(state, routes) => {
    state.addRoutes = []
    state.addRoutes = routes
    state.routes = state.routes.slice(0,4)
    state.routes.push(...routes)
  },

  DEL_FIRSTROUTES:(state, routes) => {
    state.addRoutes = []
  }
}

const actions = {

  //菜单递归
  getMenus(menuList) {
    if (isEmpty(menuList)) {
      return [];
    }
    menuList.forEach((currentMenu, index) => {
      let childrenMenu = {
        children: currentMenu.children,
        path: currentMenu.module,
        name: currentMenu.module,
        meta: {title: currentMenu.title, is_core: currentMenu.is_core},
        id:currentMenu.id,
      }
      if (childrenMenu.children.length == 0) {
        childrenMenu.component = resolve => require([`@/views${currentMenu.url}`], resolve)
      } else {
        childrenMenu.redirect = currentMenu.url
        childrenMenu.component = {
          render(c) {;
            return c('router-view')
          }
        }
        //继续递归
        childrenMenu.children = this.getMenus(currentMenu.children)
      }
      menuList[index] = childrenMenu
    })
    return menuList;
  }

}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
