<template>
  <div>
    <div class="tabber-box">
      <div class="tabber-item" v-for="(item,index) in tabBar" :key="index">
        <div @click="queryItem(index)">
          <img v-if="tabberinfo.iconIsShow  " :src="selectIndex === index ? item.selectedIconPath ||defImg  : item.iconPath ||defImg" :style="(item.size||'small') | imgWidthAndHegin" />
          <span v-if="tabberinfo.fontIsShow" class='tabber-text' :class="selectIndex == index ?'optColor':''">{{ item.page_name }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { mapState } from 'vuex';
export default {
  name: 'tabber',
  data() {
    return {
      selectIndex: '',
      tabBar: [],
      defImg: require('@/assets/images/diy/Default_picture.png')
    }
  },
  computed: {
    ...mapState("admin/mobildConfig", ["tabBarList", 'tabberinfo'])
  },
  watch: {
    tabberinfo: {
      handler(newVal) {
        console.log(newVal)
      },
      deep: true
    },
    tabBarList: {
      handler(newVal) {
        console.table('page_name', newVal)
        this.tabBar = newVal
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    queryItem(index) {
      this.selectIndex = index
    },
    customVars() {
      return {

      }
    }
  },
  filters: {
    // 官方图片默认大小 是24 px
    imgWidthAndHegin(value) {
      const WANDH = {
        small: '24px',
        medium: '32px',
        big: '40px',
      }
      console.log('WANDH[value]')

      return {
        'width': WANDH[value],
        'height': WANDH[value]
      }
    }
  }
}
</script>

<style lang="less" scoped>
.tabber-box {
  width: 100%;
  min-height: 50px;
  max-height: 50px;
  position: absolute;
  bottom: 0px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  justify-content: space-around;
  align-items: center;
  user-select: none;
  background-color: white;
  border-bottom-left-radius: 20px;
  border-bottom-right-radius: 15px;
  z-index: 40;

  margin-top: var(--marginTop);
  margin-bottom: var(--marginBottom);
  margin-left: var(--marginLeft);
  padding-right: var(--marginRight);
  .tabber-item {
    cursor: pointer;
    div {
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
    }
  }
  .tabber-text {
    color: var(--fontColor);
    font-family: var(--typeFace);
    font-size: var(--fontSzie);
    line-height: var(--lingHehig);
  }
  .optColor {
    color: var(--optColor) !important;
  }
}
</style>
