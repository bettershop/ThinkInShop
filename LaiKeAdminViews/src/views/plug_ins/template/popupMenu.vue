<template>
    <div class="popup-menu"> 
      <img :src="shuaix" class="icon" @click="toggleMenu">  
      <transition name="fade">
        <div class="menu-content" v-show="isOpen" @click="queryType">
          <!-- 三角形指示器 -->
          <div class="menu-arrow"></div>
          <div class="menu-item" data-type="all" :class="{ 'avitem': type1 === 'all' }">全部</div>
          <div class="menu-item" data-type="sys" :class="{ 'avitem': type1 === 'sys' }">系统页面</div>
          <div class="menu-item" data-type="cust" :class="{ 'avitem': type1 === 'cust' }">自定义页面</div>
        </div>
      </transition>
    </div>
  </template>
  
  <script> 
  export default { 
    data() {
      return {
        isOpen:false,
        shuaix: require('../../../assets/images/diy/shuaix.png'),
        type1:'all'
      };
    },
    methods: {
      toggleMenu() {
        this.isOpen = !this.isOpen;
      },
      queryType({target}){
        const type = target.getAttribute('data-type');
        if (type) {
          this.type1 = type
          this.$emit('queryType', type);
        }
        this.isOpen = false; // 点击后关闭菜单
      }
    }
  };
  </script>
  
  <style scoped>
    .avitem{
      background-color: #409EFF !important;
      color: #fff;
    }
  .icon{
    cursor: pointer;
    width: 16px;
    height: 16px;
    margin-left: 10px;
  }
  .popup-menu {
    position: relative;
    display: inline-block;
    height: 16px;
  }
  
  .menu-content {
    position: absolute;
    top: 38px; /* 调整位置以容纳三角形 */
    left: -80px;
    width: 120px;
    background-color: #fff;
    border: 1px solid #dcdcdc;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 10px 0;
    text-align: center;
    border-radius: 4px;
    z-index: 10;
  }
  
  /* 三角形指示器 */
  .menu-arrow {
    position: absolute;
    top: -10px; /* 位于菜单顶部外 */
    right: 10px; /* 水平位置 */
    width: 0;
    height: 0;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
    border-bottom: 10px solid #fff; /* 与菜单背景色一致 */
    z-index: 20;
  }
  
  /* 为三角形添加边框 */
  .menu-arrow::after {
    /* content: ''; */
    position: absolute;
    top: -11px; /* 微调以显示边框 */
    left: 20px;
    width: 0;
    height: 0;
    border-left: 11px solid transparent;
    border-right: 11px solid transparent;
    border-bottom: 11px solid #dcdcdc; /* 与菜单边框色一致 */
    z-index: 10;
  }
  
  .menu-item {
    padding: 6px 0;
    cursor: pointer;
  }
  
  .menu-item:hover {
    background-color: #f5f7fa;
  }
  
  /* 优化的渐入渐出动画 */
  .fade-enter-active,
  .fade-leave-active {
    transition: all 0.3s ease;
    opacity: 1;
  }
  
  .fade-enter,
  .fade-leave-to {
    opacity: 0;
    transform: translateY(-10px); /* 添加垂直位移增强动画效果 */
  }
  </style>