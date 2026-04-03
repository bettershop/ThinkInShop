<template>
  <div class="mobile-page page-title" :style="{ background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}">
    <div class="right" v-if="linkStyle">
     <img  class="jian-tou" src='@/assets/images/diy/new_back.png' alt=""> 
    </div>
    <div class="left"> 
      <span class="title" :style="{fontSize: titleSize + 'px', lineHeight: titleSize + 'px',color:colorValue[0].item}">{{title}}</span>
    </div>
  
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'page_title',
  cname: '页面标题',
  configName: 'c_page_title',
  icon: 'iconfenleidaohang',
  type: 0, // 0 基础组件 1 营销组件 2工具组件
  defaultName: 'c_page_title', // 外面匹配名称
  props: {
    index: {
      type: null,
      default: -1
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
        if (!data.pointName) {
          this.$set(data, 'pointName', 'page_title')
        }
        this.setConfig(data);
      },
      deep: true
    }
  },
  data () {
      return {
       
        // 默认初始化数据禁止修改
        defaultConfig: { 
          configName: 'c_page_title',
          pointName:'page_title',
          name: 'page_title', 
          cname: '页面标题', 
          icon: 'iconfenleidaohang',
          timestamp: this.num,

          title: '链接', 
          titleConfig: {
            title: '页面标题',
            value: '页面标题',
            max: 25
          },
          titleSize: {
            title: '字体大小',
            val: 18,
            min: 12,
            max: 25
          }, 
          bgColor: {
            title: '背景顏色',
            name: 'themeColor', 
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
          linkStyle: {
            title: '是否显示返回箭',
            name: 'linkStyle',
            val: false,
            list: [
              {
                val: false
              },
              {
                val: true
              }
            ]
          },
          color:{
            title: '字体颜色',
            name: 'color', 
            value: '#000000',
            default: [
              {
                item: '#000000'
              }
            ],
            color: [
              {
                item: '#000000'
              }
            ]
          }
        },
        title: '页面标题',
        titleSize: 19, 
        linkStyle: false,
        bgColor: [
          {
            item: '#FFFFFF'
          },
          {
            item: '#FFFFFF'
          }
        ],
        colorValue:  [
          {
            item: '#000'
          }
        ],
      };
    },
  mounted() {
    this.$nextTick(() => {
      this.pageData = this.$store.state.admin.mobildConfig.defaultArray[this.num]
      this.setConfig(this.pageData)
    })
  }, 
   methods: {
      setConfig (data) {
        console.log(data, 'page-title')
        if (!data.titleConfig) {
          data = data[this.num]
        } 
        this.title = data.titleConfig.value 
        this.titleSize = data.titleSize.val
        this.linkStyle = data.linkStyle.val
        this.bgColor = data.bgColor.color;
        this.colorValue = data.color.color;
      }
    }
}
</script>


<style scoped lang="less">
  .page-title{
    min-height: 44px;
  }
  .jian-tou{
    width: 12px;
    height: 24px;
    margin-right: 8px;
    margin-left: 27px;
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
  }
.mobile-page { 
    display: flex;
    align-items: center;
   
  .left {
     flex: 1;
    text-align: center;
    .shu {
      width: 2px;
      background: #000000;
      margin-right: 10px
    }

    .title {
      font-family: Source Han Sans CN;
      font-weight: 500;
      color: #000000;
    }
  }
  .right {
    width: 0px;
    font-size: 12px;
    line-height: 12px;
    display: flex;
    align-items: center;
  }
}
</style>
