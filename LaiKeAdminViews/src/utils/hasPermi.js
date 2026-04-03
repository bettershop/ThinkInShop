import Vue from 'vue'
import router from '@/router'

/**
 * xuxiong
 * 全局指令v-hasPermi来控制按钮权限传入按钮对应的id即可控制
 * 示例 v-hasPermi="xxx" xxx对应的是发布商品按钮的label,可以打印userPermissions来查看
 **/
const hasPermi = Vue.directive('hasPermi', {
  inserted (el, binding) {
    // 获取权限值
    const permissionValue = binding.value
    // 获取当前用户的权限列表
    const userPermissions = router.currentRoute.meta.permission

    //todo 如果按钮不没有显示请打开这里 看userPermissions 和 管理后台 - 平台 - 权限管理下的菜单地址是否一致
    // console.log("========>")
    // console.log('permissionValue', permissionValue)
    // console.log('userPermissions', userPermissions)
    // console.log("========>")

    // 判断用户权限是否包含按钮所需权限
    if (permissionValue && userPermissions) {
      if (!userPermissions.includes(permissionValue)) {
        // 移除元素
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
})

export default {
  hasPermi
}
