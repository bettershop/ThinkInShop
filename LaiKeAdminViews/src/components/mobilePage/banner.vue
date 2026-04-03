<template>
  <div class="mobile-page" :style="{
      marginTop: `${mTOP}px`,
      background: bgColor2[0].item,
      height: `${height}px`
    }">
    <div class="bg" :style="{
        background: `linear-gradient(${deg}deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`
      }" v-if="bgColor.length > 1 && swiperStyle == 1"></div>

    <div class="bg" :style="{
        background: bgColor[0].item
      }" v-if="bgColor.length == 1  && swiperStyle == 1"></div>

    <!-- 样式一  样式二 -->
    <div class="banner" :style="{
        height: `${height}px`,
        padding: `${direction}`
      }" v-if="swiperStyle == 0 || swiperStyle == 1">
      <img crossOrigin="anonymous" :class="{ doc: imgStyle }" :src="imgSrc" alt="" v-if="imgSrc" />
      <div class="empty-box" :class="{ on: imgStyle }" v-else>
        <img src='../../assets/images/diy/Default_picture.png' alt="">
      </div>
    </div>

    <!-- 样式三 卡片化 -->

    <div class="banner" :style="{
        height: `${height}px`,
        padding: `${direction}`
      }" v-if="swiperStyle == 2">
      <div class="banner-box">
        <div class="banner-left">
          <img crossOrigin="anonymous" :class="{ doc: imgStyle }" :src="imgSrcList[1].img " alt="" v-if="imgSrcList&&imgSrcList.length>1" />
          <div class="empty-box" :class="{ on: imgStyle }" v-else>
            <img src='../../assets/images/diy/Default_picture.png' alt="">
          </div>
        </div>
        <div class="banner-center">
          <img crossOrigin="anonymous" :class="{ doc: imgStyle }" :src="imgSrc" alt="" v-if="imgSrc" />
          <div class="empty-box" :class="{ on: imgStyle }" v-else>
            <img src='../../assets/images/diy/Default_picture.png' alt="">
          </div>
        </div>
        <div class="banner-right">
          <img crossOrigin="anonymous" :class="{ doc: imgStyle }" :src="imgSrcList&&imgSrcList[2].img" alt="" v-if="imgSrcList.length>2" />
          <div class="empty-box" :class="{ on: imgStyle }" v-else>
            <img src='../../assets/images/diy/Default_picture.png' alt="">
          </div>
        </div>
      </div>
    </div>

    <div>
      <div class="dot" v-if="docStyle == 'circle'">
        <div style="display:flex;padding: 1px 3px;border-radius: 2px;" :style="{ background: docBackgroundColor[0].item }">
          <div class="dot-item active" :style="{ background: docSelectColor[0].item }"></div>
          <div class="dot-item" :style="{ background: docColor[0].item }"></div>
          <div class="dot-item" :style="{ background: docColor[0].item }"></div>
        </div>
      </div>

      <div class="dot line-dot" v-if="docStyle == 'line'">
        <div style="display:flex;padding: 1px 3px;border-radius: 2px;" :style="{ background: docBackgroundColor[0].item }">
          <div class="line_dot-item active" :style="{ background: docSelectColor[0].item }"></div>
          <div class="line_dot-item" :style="{ background: docColor[0].item }"></div>
          <div class="line_dot-item" :style="{ background: docColor[0].item }"></div>
        </div>
      </div>

      <div class="dot line-dot" v-if="docStyle == 'number'">
        <div :style="{ background: docBackgroundColor[0].item }" class="number-dot">
          <div class="number-dot-item number-dot-active" :style="{ background: docSelectColor[0].item }">0</div>
          <div class="number-dot-item number-dot-active" :style="{ background: docColor[0].item }" style="margin:0 8px;">1</div>
          <div class="number-dot-item number-dot-active" :style="{ background: docColor[0].item }">2</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";
import Cookies from 'js-cookie'
let Language = Cookies.get('language') != 'en' ? '轮播图' : 'banner'
export default {
  name: "banner", // 组件名称
  cname: "轮播图", // 标题名称
  icon: "icontupianguanggao",
  defaultName: "swiperBg", // 外面匹配名称
  configName: "c_banner", // 右侧配置名称
  type: 0, // 0 基础组件 1 营销组件 2工具组件
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
        console.log(data, 'num')
        this.setConfig(data);
      },
      deep: true
    },
    defaultArray: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[
          this.num
        ];
        console.log(data, 'defaultArrayhandler')

        if (data && !data.pointName) {
          this.$set(data, 'pointName', 'banner')
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
        configName: "c_banner",
        name: "swiperBg",
        pointName: 'banner',
        cname: "轮播图", // 标题名称
        icon: "icontupianguanggao",
        timestamp: this.num,
        // 模板选择
        tabConfig: {
          tabVal: 0,
          type: 1,
          tabList: [
            {
              name: "样式一",
              icon: "iconbanner_1"
            },
            {
              name: "样式二",
              icon: "iconbanner_2"
            },
            {
              name: "样式三",
              icon: "iconbanner_3"
            }
          ]
        },
        // 图片列表
        swiperConfig: {
          title:
            "建议尺寸750px*200px，拖拽可调整图片顺序",
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
            }
          ]
        },
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
        tabColor: {
          title: "前置颜色",
          type: "tabColor",
          tabType: 1,
          val: 0,
          default: [
            {
              item: "#FFFFFF"
            }
          ],
          color: [
            {
              item: "#FFFFFF",

            }
          ]
        },
        c_title: {
          title: "设置",
        },
        c_indication: {
          type: "circle",
        },
        heightConfig: {
          val: 140
        },
        docColorConfig: {
          title: "默认颜色",
          name: "themeColor",
          default: [
            {
              item: "#FFFFFF99"
            }
          ],
          color: [
            {
              item: "#FFFFFF99"
            }
          ]
        },
        docSelectColorConfig: {
          title: "选中颜色",
          name: "themeColor",
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
        docBackgroundColorConfig: {
          title: "背景颜色",
          name: "themeColor",
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
        // 左右间距
        // lrConfig: {
        //   title: "左右边距",
        //   val: 10,
        //   min: 0
        // },
        // 页面间距
        mbConfig: {
          title: "组件间距",
          val: 0,
          min: 0
        },
        itemEdge: {
          title: "图片边距",
          val: 0,
          min: 0
        },
        paddingConfig: {
          title: "组件间距",
          type: 'padding',
          val: 10,
          min: 0,
          padding: {
            top: 10,
            right: 10,
            bottom: 10,
            left: 10
          }
        },
        // 轮播图点样式
        docConfig: {
          cname: "swiper",
          title: "轮播切换",
          type: 0,
          list: [
            {
              val: "圆点",
              icon: "iconDot"
            },
            {
              val: "直线",
              icon: "iconSquarepoint"
            }
          ]
        },
        // 图片样式
        imgConfig: {
          cname: "docStyle",
          title: "直角/圆角",
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
        }
      },
      pageData: {},
      bgColor: [
        {
          item: "#FFFFFF"
        }
      ],
      bgColor2: [
        {
          item: "#FFFFFF"
        }
      ],
      docColor: [
        {
          item: "rgba(255, 255, 255, 0.6)"
        }
      ],
      docSelectColor: [
        {
          item: "rgba(255, 255, 255, 1)"
        }
      ],
      docBackgroundColor: [
        {
          item: "rgba(255, 255, 255, 0)"
        }
      ],
      mTOP: 0,
      Padding: '',
      imgStyle: 0,
      imgSrc: "",
      docStyle: 0,
      swiperStyle: 0,
      height: 140,
      deg: 0,
      imgSrcList: [],
      direction: '',
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
        if (!data.tabConfig) {
          data = data[this.num]
        }

        this.swiperStyle = data.tabConfig.tabVal;
        this.bgColor2 = data.bgColor2.color;
        this.deg = data.tabColor.val;
        this.bgColor = data.tabColor.color;
        this.docColor = data.docColorConfig.color;
        this.docSelectColor = data.docSelectColorConfig.color;
        this.docBackgroundColor = data.docBackgroundColorConfig.color;
        this.mTOP = data.mbConfig.val;
        this.Padding = data.paddingConfig.padding;
        //拼接
        this.direction = `${this.Padding.top}px ${this.Padding.right}px ${this.Padding.bottom}px ${this.Padding.left}px`

        this.imgStyle = data.imgConfig.type;
        this.imgSrc = data.swiperConfig.list.length
          ? data.swiperConfig.list[0].img
          : "";
        this.imgSrcList = data.swiperConfig.list
        this.docStyle = data.c_indication.type;
        this.height = data.heightConfig.val;
        console.log(data.tabConfig.tabVal, 'docStyle')
      }
    }
  }
};
</script>
<style scoped>
.number-dot {
  display: flex;
  color: #ffffff;
  border-radius: 12px;
  font-size: 9px;
  padding: 2px 6px;
}
.number-dot-active {
  background: #409eff;
  color: gray;
  border-radius: 50%;
  padding: 0 4px;
}
</style>
<style scoped lang="stylus">
.mobile-page {
  position: relative;
  width: auto;

  .banner {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    display: flex;

    .banner-box {
      display: flex;
      width: 300px; // 控制最大宽度
      overflow: hidden;
      position: relative;

      .banner-left {
        width: 200px;
        height: 100%;
        position: absolute;
        left: -170px;
      }

      .banner-center {
        width: 200px;
        height: 100%;
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
      }

      .banner-right {
        width: 200px;
        height: 100%;
        position: absolute;
        right: -170px;
      }
    }

    img {
      width: 100%;
      height: 100%;
      border-radius: 6px;

      &.doc {
        border-radius: 0;
      }
    }
  }

  .bg {
    width: 100%;
    height: 100px;
    background: linear-gradient(90deg, #F62C2C 0%, #F96E29 100%);
    border-bottom-left-radius: 20px;
    border-bottom-right-radius: 20px;
  }
}

.dot {
  position: absolute;
  left: 0;
  bottom: 20px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;

  .dot-item {
    width: 5px;
    height: 5px;
    background: #fff;
    border-radius: 50%;
    margin: 0 3px;

    &.active {
      background: #AAAAAA;
    }
  }

  &.line-dot {
    bottom: 20px;

    .line_dot-item {
      width: 8px;
      height: 2px;
      /* background #fff */
      margin: 0 3px;

      &.active {
        /* background #AAAAAA */
      }
    }
  }
}
</style>
