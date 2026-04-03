import {asyncRoutes, constantRoutes} from '@/router'
import {getStorage,removeStorage,setStorage} from '@/utils/storage'
import request from "@/api/https";
import Layout from "@/layout/index";
import {isEmpty} from "element-ui/src/utils/util";
import ElementUI from 'element-ui';
import { cloneDeep,flatMapDeep } from 'lodash';
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
  }
}

const actions = {
  getAsyncRoutes({commit}) {
    const res = []
    return request({
      method: 'post',
      params: {
        api: 'saas.role.getAsyncRoutesByRoutes',
      },
    }).then(routes => {
      if(routes.data.code == '200' && routes.data.data.menu.length) {
        let route = routes.data.data;
        // tab切换的配置（过滤掉一二三级菜单列表）
        let routerList = cloneDeep(routes.data.data)
        const fn = (source)=>{
          let res = []
          source.forEach(el=>{
              res.push(el)
              el.children && res.push(...fn(el.children))
          })
          // 过滤掉相关数据（新增加的tab配置）
          return res.filter(i=>i.is_tab!==0)
      }
        console.log(route,"菜单权限707070707070",routerList,fn(routerList.menu))
        setStorage("tabRouter",fn(routerList.menu))
        if (route.menu.length !== 0) {
          const list=[]
          route.menu.forEach((menu, index) => {
            let icons = [];
            if (!isEmpty(menu.image)) {
              icons.push(menu.image)
            }
            if (!isEmpty(menu.image1)) {
              icons.push(menu.image1)
            }
            let modules = menu.children.length != 0 ? menu.children[0].module : ''
            //一级菜单
            let topMenu = {
              path: "/" + menu.module,
              component: Layout,
              redirect: "/" + menu.module + "/" + modules,
              name: menu.module,
              meta: {title: menu.title, icon: icons},
              taburl:"/" + menu.module + "/" + modules,
              taburl101:"/" + menu.module + "/" + modules
            }
            //递归子菜单
            topMenu.children = actions.getMenus(menu.children)

            res.push(topMenu)
            menu.children.forEach((item, i)=>{
              list.push(item)
            })

          })
          removeStorage('route')
          setStorage("route",list)

        }
        return actions.generateRoutes(commit, res);
      } else {
        window.sessionStorage.clear()
        window.location.href = '/login'
        location.reload();
      }
    })
  },
  //菜单递归
  getMenus(menuList) {
    if (menuList.length == 0) {
      return [];
    }
     console.log('getMenus',menuList)

    menuList.forEach((currentMenu, index) => {
      let childrenMenu = {
        path: currentMenu.module,
        name: currentMenu.module,
        meta: {
          title: currentMenu.title,
          is_core: currentMenu.is_core,
          permission: [],       // 存储权限URL列表
          permissionList: []    // 存储权限按钮信息
        },
        id:currentMenu.id,
        taburl:currentMenu.url,
        taburl142:currentMenu.url
      }

      //增加按钮权限
      menuList.forEach(item =>{
        let arr =  {
          label: item?.meta?.title || item?.title,
          value: item?.id
        }
        childrenMenu.meta.permissionList.push(arr)
        //弃用，本来是要用id判断按钮权限，但是php和Javaid可能不同意，改用label
        // childrenMenu.meta.permission.push(item?.id)//此判断是因为在递归过程中可能有的数据结构已经变更了
        if(item){
          if( item.url )
          {
            childrenMenu.meta.permission.push(item.url)
          }else if(item.action ){
            childrenMenu.meta.permission.push(item.action)
          }else if(item.path ){
            childrenMenu.meta.permission.push(item.path)
          }
        }

      })
      //是否有子菜单
      console.log('currentMenu.isChildren',currentMenu.isChildren)
      if (!currentMenu.isChildren) {
        childrenMenu.component = resolve => require([`@/views${currentMenu.url}`], resolve)
      } else {
        childrenMenu.redirect = currentMenu.url
        childrenMenu.component = {
          render(c) {
            return c('router-view')
          }
        }
        //继续递归
        childrenMenu.children = actions.getMenus(currentMenu.children);
      }
      menuList[index] = childrenMenu
    })
    return menuList;
  },
  generateRoutes(commit, authorizationList) {
    return new Promise(resolve => {
      let authorizationLists = authorizationList
      if(getStorage('laike_admin_userInfo') && getStorage('laike_admin_userInfo').type != 0) {
        authorizationLists = authorizationLists.filter(item => {
          if(item.meta.title !== '平台') {
            return item
          }
        })
      }
      commit('SET_ROUTES', authorizationLists)
      resolve(authorizationList)
    })
  },

}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
