<template>
  <div class="mobile-page" :style="{
      marginTop: `${mTOP}px`,
      background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`,
      height:(height) +'px'
    }">
    <div class="banner" :style="{
        paddingLeft: edge + 'px',
        paddingRight: edge + 'px',
        height:(height) +'px'
      }">
      <img crossOrigin="anonymous" :class="{ doc: imgStyle }" :src="imgSrc" alt="" v-if="imgSrc" />
      <div class="empty-box" :class="{ on: imgStyle }" v-else>
        <img src='../../assets/images/diy/Default_picture.png' alt="">
      </div>
    </div>
    <div v-if="swiperStyle != 0">
      <div class="dot" v-if="docStyle == 0">
        <div class="dot-item active"></div>
        <div class="dot-item"></div>
        <div class="dot-item"></div>
      </div>
      <div class="dot line-dot" v-if="docStyle == 1">
        <div class="line_dot-item active"></div>
        <div class="line_dot-item"></div>
        <div class="line_dot-item"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  name: "picture1", // 组件名称
  cname: "图片", // 标题名称
  icon: "icontupianguanggao",
  defaultName: "swiperPicture", // 外面匹配名称
  configName: "c_picture", // 右侧配置名称
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
        this.setConfig(data);
        // this.$store.state.admin.mobildConfig.defaultArray.forEach((el, i) => {
        //     if (el.timestamp == nVal) {
        //         this.setConfig(el)
        //         console.log(i, 'index监听')
        //     }
        // })
      },
      deep: true
    },
    defaultArray: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[
          this.num
        ];
        if (Object.keys(data).length && !data.pointName) {
          this.$set(data, 'pointName', 'picture1')
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
        configName: "c_picture",
        pointName: "picture1", // 组件标识
        name: "swiperPicture",
        cname: "图片", // 标题名称
        icon: "icontupianguanggao",
        timestamp: this.num,
        // 模板选择

        // 图片列表
        swiperConfig: {
          title: "最多可添加1张图片，建议宽度750px",
          maxList: 1,
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
        // 背景颜色
        bgColor: {
          title: "背景颜色",
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
        // 左右间距
        lrConfig: {
          title: "左右边距",
          val: 10,
          min: 0
        },
        // 页面间距
        mbConfig: {
          title: "页面间距",
          val: 0,
          min: 0
        },
        // 页面间距
        htConfig: {
          title: "图片高度",
          val: 120,
          min: 0
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
        }
      },
      pageData: {},
      bgColor: [
        {
          item: "rgb(161, 206, 206)"
        },
        { item: "rgb(161, 206, 206)" }
      ],
      mTOP: 0,
      edge: 0,
      height: 0,
      imgStyle: 0,
      imgSrc: "",
      docStyle: 0,
      swiperStyle: 0
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
      if (data && !data.bgColor) {
        data = data[this.num]
      }

      if (data && Object.keys(data).length) {
        this.bgColor = data.bgColor.color;
        this.mTOP = data.mbConfig.val;
        this.edge = data.lrConfig.val;
        this.height = data.htConfig.val;
        this.imgStyle = data.imgConfig.type;
        this.imgSrc = data.swiperConfig.list.length
          ? data.swiperConfig.list[0].img
          : "";
      }
      // this.docStyle = data.docConfig.type
    }
  }
};
</script>

<style scoped lang="stylus">
.mobile-page {
  position: relative;
  width: auto;
  height: 140px;

  .banner {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 140px;

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
    height: 50px;
    background: linear-gradient(90deg, #F62C2C 0%, #F96E29 100%);
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
      background: #fff;
      margin: 0 3px;

      &.active {
        background: #AAAAAA;
      }
    }
  }
}
</style>
