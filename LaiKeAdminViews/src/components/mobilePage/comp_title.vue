<template>
  <div class="mobile-page comp-title" :style="{marginTop : mbConfig + 'px',background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}">
    <div class="left"> 
      <span class="title" :style="{fontSize: titleSize + 'px',color:textColor[0].item}">{{title}}</span>
    </div>
    <div class="right" v-if="linkStyle">
      更多
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'comp_title',
  cname: '标题组件',
  configName: 'c_comp_title',
  icon: 'iconfenleidaohang',
  type: 2, // 0 基础组件 1 营销组件 2工具组件
  defaultName: 'c_comp_title', // 外面匹配名称
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
          this.$set(data, 'pointName', 'comp_title')
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
          configName: 'c_comp_title',
          pointName:'comp_title',
          name: 'comp_title',
          cname: '标题组件', 
          icon: 'iconfenleidaohang',  
          timestamp: this.num, 

          title: '链接',
          mbConfig: {
            title: '页面间距',
            val: 0,
            min: 0
          },
          titleConfig: {
            title: '标题链接',
            value: '本月推荐',
            max: 25
          },
          titleSize: {
            title: '字体大小',
            val: 18,
            min: 12,
            max: 32
          },
          shuHeight: {
            title: '线条高度',
            val: 17,
            min: 12
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
          textColor: {
            title: '文字顏色', 
            default: [
              {
                item: '#000'
              } 
            ],
            color: [
              {
                item: '#000'
              } 
            ]
          },
          linkStyle: {
            title: '是否显示更多',
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
          linkConfig: {
            title: '链接地址',
            val: '',
            maxList: 10,
            list: [
              {
                img: '',
                info: [
                  {
                    title: '标题',
                    value: '今日推荐',
                    tips: '选填，不超过4个字',
                    max: 4
                  },
                  {
                    title: '链接',
                    value: '',
                    max: 100,
                    link_type: 'product'
                  }
                ]
              }
            ]
          },
        },
        title: '本月推荐',
        titleSize: 19,
        shuHeight: 17,
        mbConfig: 0,
        linkStyle: false,
        bgColor: [
          {
            item: '#FFFFFF'
          },
          {
            item: '#FFFFFF'
          }
        ],
        textColor: [
          {
            item: '#FFFFFF'
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
        if (!data.mbConfig) {
          data = data[this.num]
        }
        this.mbConfig = data.mbConfig.val
        this.title = data.titleConfig.value
        this.shuHeight = data.shuHeight.val
        this.titleSize = data.titleSize.val
        this.linkStyle = data.linkStyle.val
        this.bgColor = data.bgColor.color;
         this.textColor = data.textColor.color;
      }
    }
}
</script>


<style scoped lang="less">
.mobile-page {
  padding-left: 15px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .left {
    display: flex;
    align-items: center;
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
    padding-right: 9px;
    font-size: 12px;
    line-height: 12px;
    display: flex;
    align-items: center;
  }
}
</style>
