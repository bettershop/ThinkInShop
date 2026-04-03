<template>
  <div
    class="mobile-page"
    :style="{
      marginTop: slider + 'px',
      background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`
    }"
  >  
    <div class="home_menu" :class="{ on: !boxStyle }">
      <div class="menu-item" v-for="(item, index) in vuexMenu" :key="index">
        <div class="img-box">
          <img crossOrigin="anonymous" :src="item.img" alt="" v-if="item.img" />
          <div class="empty-box on" v-else>
            <img src='../../assets/images/diy/Default_picture.png' alt="">
          </div>
        </div>

        <p :style="{ color: txtColor }" class="tab-tit">{{ item.info[0].value }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  name: "home_menu",
  cname: "导航组",
  icon: "icondaohangzu",
  configName: "c_home_menu",
  type: 0, // 0 基础组件 1 营销组件 2工具组件
  defaultName: "menus", // 外面匹配名称
  props: {
    index: {
      type: null
    },
    num: {
      type: null
    }
  },
  computed: {
    ...mapState("admin/mobildConfig", ["defaultArray"])
  },
  watch: {
    pageData: {
      handler(nVal, oVal) {
        this.setConfig(nVal);
      },
      deep: true
    },
    num: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[nVal];
        this.setConfig(data);
      },
      deep: true
    },
    defaultArray: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[
          this.num
        ];
        if(!data.pointName){
            this.$set(data,'pointName','home_menu')
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
        configName: "c_home_menu",
        pointName:'home_menu',
        name: "menus",
        cname: "导航组",
        icon: "icondaohangzu",
        timestamp: this.num,
        menuConfig: {
          title: "最多可添加10张图片，建议宽度90 * 90px",
          maxList: 10,
          list: [
            {
              img: "",
              info: [
                {
                  title: "标题",
                  value: "今日推荐",
                  tips: "选填，不超过4个字",
                  max: 4
                },
                {
                  title: "链接",
                  value: "",
                  max: 100,
                  link_type: "product"
                }
              ]
            },
            {
              img: "",
              info: [
                {
                  title: "标题",
                  value: "热门榜单",
                  tips: "选填，不超过4个字",
                  max: 4
                },
                {
                  title: "链接",
                  value: "",
                  max: 100,
                  link_type: "product"
                }
              ]
            },
            {
              img: "",
              info: [
                {
                  title: "标题",
                  value: "首发新品",
                  tips: "选填，不超过4个字",
                  max: 4
                },
                {
                  title: "链接",
                  value: "",
                  max: 100,
                  link_type: "product"
                }
              ]
            },
            {
              img: "",
              info: [
                {
                  title: "标题",
                  value: "促销单品",
                  tips: "选填，不超过4个字",
                  max: 4
                },
                {
                  title: "链接",
                  value: "",
                  max: 100,
                  link_type: "product"
                }
              ]
            }
          ]
        },
        rowStyle: {
          title: "每行数量",
          name: "rowStyle",
          type: 0,
          list: [
            {
              val: "4个",
              icon: "iconFour"
            },
            {
              val: "5个",
              icon: "iconfive"
            }
          ]
        },
        titleColor: {
          title: "标题颜色",
          name: "themeColor",
          default: [
            {
              item: "#333333"
            }
          ],
          color: [
            {
              item: "#333333"
            }
          ]
        },
        bgColor: {
          title: "背景颜色",
          name: "themeColor",
          default: [
            {
              item: "#FFFFFF"
            },
            {
              item: "#FFFFFF"
            }
          ],
          color: [
            {
              item: "#FFFFFF"
            },
            {
              item: "#FFFFFF"
            }
          ]
        },
        // 页面间距
        mbConfig: {
          title: "页面间距",
          val: 0,
          min: 0
        }
      },
      vuexMenu: "",
      txtColor: "",
      bgColor: [
        {
          item: "#FFFFFF"
        },
        {
          item: "#FFFFFF"
        }
      ],
      boxStyle: "",
      slider: "",
      pageData: {}
    };
  },
  mounted() {
    this.$nextTick(() => {
      this.pageData = this.$store.state.admin.mobildConfig.defaultArray[
        this.num
      ];
     
      this.setConfig(this.pageData);
    });
  },
  methods: {
    setConfig(data) {
      if (data) { 
        if(!data.menuConfig){
          data = data[this.num] 
        } 
          this.vuexMenu = data.menuConfig.list;
          this.txtColor = data.titleColor.color[0].item;
          this.bgColor = data.bgColor.color;
          this.boxStyle = data.rowStyle.type;
          this.slider = data.mbConfig.val;
      }
    }
  }
};
</script>

<style scoped lang="stylus">
.tab-tit 
  font-family Source Han Sans, Source Han Sans
  font-weight 500
  font-size 12px
  color #333333

.home_menu
  padding 0 12px 12px;
  display flex;
  flex-wrap wrap;
  .menu-item
    margin-top 12px
    font-size 11px
    color #282828
    text-align center
    width 20%
    display: flex
    flex-direction: column
    justify-content: center
    align-items: center
    .img-box
      width 48px
      height 48px

    .box, img
      width 100%
      height 100%

    .box
      background #D8D8D8

    p
      margin-top 5px

    &:nth-child(5n)
      margin-right 0

  &.on
    .menu-item
      width 25%
      display: flex
      flex-direction: column
      justify-content: center
      align-items: center

  .icontupian
    font-size 16px
</style>
