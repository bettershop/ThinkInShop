<template>
  <div
    class="mobile-page"
    :style="{ background: bgColor2[0].item, marginTop: `${mTOP}px` }"
  >
    <div
      class="adv-wrap"
      :class="{
        onTopLeft: bgTopLeftStyle,
        onTopRight: bgTopRightStyle,
        onBottomLeft: bgBottomLeftStyle,
        onBottomRight: bgBottomRightStyle
      }"
      :style="{
        background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`
      }"
    >
      <div class="adv">
        <img
          crossOrigin="anonymous"
          :class="{ on: imgStyle }"
          :src="imgSrc"
          alt=""
          v-if="imgSrc"
        />
        <div class="empty-box" :class="{ on: imgStyle }" v-else>
          <span class="iconfont-diy icontupian"></span>
        </div>

        <img
          crossOrigin="anonymous"
          :class="{ on: imgStyle }"
          :src="imgSrc1"
          alt=""
          v-if="imgSrc1"
        />
        <div class="empty-box" :class="{ on: imgStyle }" v-else>
          <span class="iconfont-diy icontupian"></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  name: "home_adv",
  cname: "广告",
  icon: "icon-gonggao",
  configName: "c_home_adv",
  defaultName: "homeAdv", // 外面匹配名称
  type: 2, // 0 基础组件 1 营销组件 2工具组件
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
          this.$set(data,'pointName','home_adv')
        }
        this.setConfig(data);
      },
      deep: true
    }
  },
  data() {
    return {
      defaultConfig: {
        name: "homeAdv",
        pointName:'home_adv',
        icon: "icon-gonggao",
        cname: "广告",
        timestamp: this.num,
        advConfig: {
          title:
            "最多可添加2张图片，建议尺寸 333px * 160px；鼠标拖拽左侧圆点可调整图片顺序",
          maxList: 2,
          list: [
            {
              img: "",
              info: [
                {
                  title: "标题",
                  value: "",
                  // value: '新品上市',
                  tips: "选填，不超过4个字",
                  max: 4
                },
                {
                  title: "链接",
                  value: "",
                  // value: '/pagesB/home/proList?title=新品上市',
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
                  value: "",
                  // value: '好物优选',
                  tips: "选填，不超过4个字",
                  max: 4
                },
                {
                  title: "链接",
                  value: "",
                  // value: '/pagesB/home/proList?title=好物优选',
                  max: 100,
                  link_type: "product"
                }
              ]
            }
          ]
        },
        // 背景颜色
        bgColor: {
          title: "前景颜色",
          default: [
            {
              item: "#cfe6e6"
            },
            {
              item: "#cfe6e6"
            }
          ],
          color: [
            {
              item: "#cfe6e6"
            },
            {
              item: "#cfe6e6"
            }
          ]
        },
        // 背景颜色
        bgColor2: {
          title: "背景颜色",
          default: [
            {
              item: "#FFFFFF"
            }
          ],
          color: [
            {
              item: "#FFFFFF"
            }
          ]
        },
        // 图片样式
        imgConfig: {
          cname: "docStyle",
          title: "图片倒角",
          type: 0,
          list: [
            {
              val: "圆角",
              icon: "iconPic_fillet"
            },
            {
              val: "直角",
              icon: "iconPic_square"
            }
          ]
        },
        bgTopLeftConfig: {
          title: "背景左上倒角",
          cname: "docStyle",
          type: 1,
          list: [
            {
              val: "圆角",
              icon: "iconPic_fillet"
            },
            {
              val: "直角",
              icon: "iconPic_square"
            }
          ]
        },
        bgTopRightConfig: {
          title: "背景右上倒角",
          cname: "docStyle",
          type: 1,
          list: [
            {
              val: "圆角",
              icon: "iconPic_fillet"
            },
            {
              val: "直角",
              icon: "iconPic_square"
            }
          ]
        },

        bgBottomLeftConfig: {
          title: "背景左下倒角",
          cname: "docStyle",
          type: 1,
          list: [
            {
              val: "圆角",
              icon: "iconPic_fillet"
            },
            {
              val: "直角",
              icon: "iconPic_square"
            }
          ]
        },
        bgBottomRightConfig: {
          title: "背景右下倒角",
          cname: "docStyle",
          type: 1,
          list: [
            {
              val: "圆角",
              icon: "iconPic_fillet"
            },
            {
              val: "直角",
              icon: "iconPic_square"
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
      mTOP: 0,
      imgStyle: 0,
      bgTopLeftStyle: 1,
      bgTopRightStyle: 1,
      bgBottomLeftStyle: 1,
      bgBottomRightStyle: 1,
      imgSrc: "",
      imgSrc1: "",
      bgColor: [
        {
          item: "#cfe6e6"
        },
        {
          item: "#cfe6e6"
        }
      ],
      bgColor2: [
        {
          item: "#FFFFFF"
        }
      ]
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
       if(!data.mbConfig){
            data = data[this.num] 
        }
      // this.tabVal = data.tabConfig.tabVal;
      this.mTOP = data.mbConfig.val;
      this.bgColor = data.bgColor.color;
      this.bgColor2 = data.bgColor2.color;
      // this.rollStyle = data.boxStyle.type;
      this.imgStyle = data.imgConfig.type;
      this.bgTopLeftStyle = data.bgTopLeftConfig.type;
      this.bgTopRightStyle = data.bgTopRightConfig.type;
      this.bgBottomLeftStyle = data.bgBottomLeftConfig.type;
      this.bgBottomRightStyle = data.bgBottomRightConfig.type;
      this.imgSrc = data.advConfig.list.length
        ? data.advConfig.list[0].img
        : "";
      this.imgSrc1 = data.advConfig.list.length
        ? data.advConfig.list[1].img
        : "";
    }
  }
};
</script>

<style scoped lang="stylus">
.mobile-page
  position relative
  width: auto

  .adv-wrap {
    border-radius 15px
    &.onTopLeft {
      border-top-left-radius 0
    }

    &.onTopRight {
      border-top-right-radius 0
    }

    &.onBottomLeft {
      border-bottom-left-radius 0
    }

    &.onBottomRight {
      border-bottom-right-radius 0
    }
  }

  .adv
    padding: 10px 15px
    display: flex;
    justify-content: space-between;
    img, .empty-box
      width: 160px
      height 80px
      border-radius 10px;

      &.doc, &.on
        border-radius: 0;
</style>
