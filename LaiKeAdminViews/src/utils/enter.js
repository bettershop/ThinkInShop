import Vue from 'vue'

//绑定事件
// v-enter="demand"


const enter = Vue.directive('enter', {
  // 当绑定元素插入到DOM中
  inserted: function (el, binding, vnode) {
    // 为元素添加键盘事件监听
    el.addEventListener('keyup', function (event) {
      // 检查是否是回车键
      if (event.keyCode === 13) {
        // 调用绑定的方法
        binding.value();
      }
    });
  }
})

export default {
    enter
  }