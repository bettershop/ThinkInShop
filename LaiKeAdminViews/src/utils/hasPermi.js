import Vue from 'vue'
import router from '@/router'

/**
 * xuxiong
 * 全局指令v-hasPermi来控制按钮权限传入按钮对应的id即可控制
 * 示例 v-hasPermi="xxx" xxx对应的是发布商品按钮的label,可以打印userPermissions来查看
 **/
const hasPermi = Vue.directive('hasPermi', {
  inserted(el, binding, vnode) {
    applyPermission(el, binding, vnode)
  },
  update(el, binding, vnode) {
    applyPermission(el, binding, vnode)
  }
})

function applyPermission(el, binding, vnode) {
  const permissionValue = binding.value
  const userPermissions = getUserPermissions(vnode)

  if (!permissionValue || !userPermissions) {
    el.style.display = ''
    return
  }

  const required = Array.isArray(permissionValue) ? permissionValue : [permissionValue]
  const allowed = required.some(v => hasAccess(v, userPermissions))
  if (allowed) {
    el.style.display = ''
  } else {
    el.style.display = 'none'
  }
}

function getUserPermissions(vnode) {
  const list = []
  const pushList = (value) => {
    if (!value) {
      return
    }
    if (Array.isArray(value)) {
      value.forEach(v => {
        if (v && !list.includes(v)) {
          list.push(v)
        }
      })
    }
  }

  pushList(router.currentRoute && router.currentRoute.meta && router.currentRoute.meta.permission)
  pushList(vnode && vnode.context && vnode.context.$route && vnode.context.$route.meta && vnode.context.$route.meta.permission)

  return list.length ? list : null
}

function hasAccess(permissionValue, userPermissions) {
  if (userPermissions.includes(permissionValue)) {
    return true
  }

  const normalizedValue = normalizePermission(permissionValue)
  if (normalizedValue && userPermissions.includes(normalizedValue)) {
    return true
  }

  if (permissionValue && permissionValue.includes('/')) {
    const last = permissionValue.split('/').filter(Boolean).pop()
    if (last && userPermissions.includes(last)) {
      return true
    }
  }

  return true
}

function normalizePermission(value) {
  if (!value) {
    return value
  }
  if (value.startsWith('/')) {
    return value.slice(1)
  }
  return '/' + value
}

export default {
  hasPermi
}
