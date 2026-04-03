<template>
  <div class="search-box" :style="{background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`,marginTop:`${slider}px`}" v-if="bgColor.length>0">
    <img crossOrigin='anonymous' style="margin-right: 5px;width: 86px;height: 34px;" :src="logoUrl" alt="" v-if="tabVal == 1">
    <div class="address" v-if="tabVal == 2">
      <span>市北区</span>
      <Icon type="ios-arrow-down" />
    </div>
    <div class="box" :class="{on:rollStyle,center:txtPosition}" :style="{color:colorValue}"><img class="search" :src="searchh" alt=""> 我是输入的内容</div>
    <div class="msg" v-if="msgStyle">
      <i class="iconfont icon-xinxi"></i>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import logoUrl from '@/assets/images/logo.jpg'
import searchh from '@/assets/images/searchh.png'

export default {
  name: 'search_box',
  cname: '搜索框',
  icon: 'iconsousukuang',
  configName: 'c_search_box',
  type: 0, // 0 基础组件 1 营销组件 2工具组件
  defaultName: 'headerSerch', // 外面匹配名称
  props: {
    index: {
      type: null
    },
    num: {
      type: null
    }
  },
  computed: {
    ...mapState('admin/mobildConfig', ['defaultArray'])
  },
  watch: {
    pageData: {
      handler(nVal, oVal) {
        this.setConfig(nVal)
      },
      deep: true
    },
    num: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[nVal]
        this.setConfig(data)
      },
      deep: true
    },
    defaultArray: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[this.num]
        console.log("search_box watch:::", data, this.num)
        if (data && !data.pointName) {
          this.$set(data, 'pointName', 'search_box')
        }
        this.setConfig(data);
      },
      deep: true
    }
  },
  data() {
    return {
      // 默认初始化数据禁止修改
      defaultConfig: {
        pointName: 'search_box',
        configName: 'c_search_box',
        name: 'headerSerch',
        cname: '搜索框',
        icon: 'iconsousukuang',
        timestamp: this.num,
        colorValue: "#333",
        tabConfig: {
          tabVal: 0,
          type: 1,
          tabList: [
            {
              name: '样式1',
              icon: 'iconsearch_1'
            },
            {
              name: '样式2',
              icon: 'iconsearch_2'
            }
          ]
        },
        bgColor: {
          title: '背景颜色',
          name: 'bgColor',
          default: [
            {
              item: '#FFFFFF'
            },
            {
              item: '#FFFFFF'
            }
          ],
          color: [
            {
              item: '#FFFFFF'
            },
            {
              item: '#FFFFFF'
            }
          ]
        },
        boxStyle: {
          title: '框体样式',
          name: 'boxStyle',
          type: 0,
          list: [
            {
              val: '圆角',
              icon: 'iconPic_fillet'
            },
            {
              val: '直角',
              icon: 'iconPic_square'
            }
          ]
        },
        txtStyle: {
          title: '文本位置',
          name: 'txtStyle',
          type: 0,
          list: [
            {
              val: '居左',
              icon: 'icondoc_left'
            },
            {
              val: '居中',
              icon: 'icondoc_center'
            }
          ]
        },
        msgStyle: {
          title: '是否显示消息',
          name: 'msgStyle',
          val: true,
          list: [
            {
              val: false
            },
            {
              val: true
            }
          ]
        },
        // 页面间距
        mbConfig: {
          title: '页面间距',
          val: 0,
          min: 0
        }, 
        logoConfig: {
          title: '最多可添加1张图片，建议宽度86 * 40px',
          url: logoUrl
        }
      },
      tabVal: '',
      bgColor: [],
      rollStyle: '',
      txtPosition: '',
      slider: '',
      pageData: {},
      logoUrl: logoUrl,
      msgStyle: true,
      searchh: searchh,
      isTop: true, // 在 App.vue 里面添加的
      colorValue: '#333'
    }
  },
  mounted() {
    this.$nextTick(() => {
      console.log("search_box mounted:::", this.$store.state.admin.mobildConfig.defaultArray, this.num)
      this.pageData = this.$store.state.admin.mobildConfig.defaultArray[this.num]
      this.setConfig(this.pageData)
    })
  },
  methods: {
    setConfig(data) {
      if (data) {
        console.log("search_box methods:::", data)
        if (!data.tabConfig) {
          data = data[this.num]
        }
        this.tabVal = data.tabConfig.tabVal;
        this.bgColor = data.bgColor.color;
        this.msgStyle = data.msgStyle.val;
        this.rollStyle = data.boxStyle.type;
        this.txtPosition = data.txtStyle.type;
        this.slider = data.mbConfig.val;
        this.logoUrl = data.logoConfig.url
        this.colorValue = data.colorValue
      }
    }
  }
}
</script>

<style scoped lang="stylus">
.search-box {
  display: flex;
  align-items: center;
  width: 100%;
  height: 48px;
  padding: 10px;

  .address {
    padding-right: 5px;
    color: white;
  }

  .msg {
    width: 32px;
    height: 32px;
    background: #fff;
    border-radius: 50%;
    color: #014343;
    font-size: 50px;
    text-align: center;
    line-height: 17px;
    margin-left: 10px;

    i {
      font-size: 25px;
    }
  }
}

.box {
  flex: 1;
  height: 36px;
  line-height: 36px;
  color: grey;
  font-size: 12px;
  padding-left: 10px;
  background: #f4f4f4;
  border-radius: 18px;

  .search {
    width: 13px;
    height: 13px;
    margin-right: 4px;
    vertical-align: middle;
  }

  &.on {
    border-radius: 0;
  }

  &.center {
    text-align: center;
    padding-left: 0;
  }
}
</style>
