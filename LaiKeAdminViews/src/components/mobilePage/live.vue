<template>
  <div class="mobile-page  ">
    <div class="isPublicModeZB">
      <div class="content_newTwo" :style="{'margin-top':mbConfig.val+'px',background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}">
        <div class="title diy-titleName">
          <!-- <img crossOrigin='anonymous' class="icon" :src="imgUrl" alt=""> -->
          <span class="content_newThree_tilte1" :style="{'color':titleTxtColor.color[0].item}">{{ titleConfig.value }}</span>
          <span class="content_newThree_tilte2">
            <span class="content_newFive_tilte2">更多</span>
            <Icon type="ios-arrow-forward" />
          </span>
        </div>
        <div class="bodye-box">
          <div class="content_newTwo_content" v-for="(item,index) in list" :key="index" :style="{ marginRight: listRight + 'px'}">
            <div style="width: 62px;height: 62px;border-radius: 50%;overflow: hidden;border: 1px solid rgba(250, 81, 81, .6)">
              <img src='../../assets/images/diy/Default_picture.png' alt="" style="width: 56px;height:56px;border-radius: 50%;margin: 3px;">
            </div>
            <div class="content_newTwo_content_name">百货自营</div>
            <div class="mydian">
              <div class="live_icon">
                <!-- <img :src="live" alt=""> -->
                <i class="one"></i>
                <i class="two"></i>
                <i class="three"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";
import Cookies from 'js-cookie'
import homeTitleBg from '@/assets/images/home_title_bg.png'
let goodsItem = {
  img: '',
  name: '小米家用电饭煲小米家用电饭煲',
  price: '234',
  discount: '1.2'
};
export default {
  name: "live", // 组件名称
  cname: "直播", // 标题名称
  icon: "icontupianguanggao",
  defaultName: "defaul_live", // 外面匹配名称
  configName: "c_publicModeLive", // 右侧配置名称
  type: 1, // 0 基础组件 1 营销组件 2工具组件
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
          this.$set(data, 'pointName', 'live')
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
        name: "liveModel",
        pointName: 'live',
        cname: "直播", // 标题名称
        icon: "icontupianguanggao",
        timestamp: this.num,
        configName: "c_publicModeLive",

        // 左右边距
        lrConfig: {
          title: '右边距',
          val: 10,
          min: 10
        },
        // 背景颜色
        bgColor: {
          title: '背景颜色',
          default: [
            {
              item: '#FFF'
            },
            {
              item: '#FFF'
            }
          ],
          color: [
            {
              item: '#FFF'
            },
            {
              item: '#FFF'
            }
          ]
        },
        titleTxtColor: {
          title: '标题颜色',
          name: 'themeColor',
          default: [{
            item: '#014343'
          }],
          color: [
            {
              item: '#014343'
            }

          ]
        },
        titleConfig: {
          title: '标题名称',
          value: '推荐主播',
          max: 10
        },
        numberConfig: {
          val: 3
        },
        // 页面间距
        mbConfig: {
          title: '页面间距',
          val: 0,
          min: 0
        },
        imgConfig: {
          title: '最多可添加1张图片，建议宽度111 * 19px（标题背景色）',
          url: homeTitleBg
        }
      },
      imgUrl: '',
      titleConfig: {},//标题
      titleTxtColor: {
        title: '标题颜色',
        name: 'themeColor',
        default: [{
          item: '#014343'
        }],
        color: [
          {
            item: '#014343'
          }

        ]
      },
      list: [],
      listRight: 0,
      mbConfig: {},
      bgColor: [
        {
          item: '#FFF'
        },
        {
          item: '#FFF'
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
      try {
        if (data) {
          const { titleConfig, titleTxtColor, mbConfig, lrConfig } = data
          console.log(data.bgColor)
          this.titleConfig = titleConfig
          this.titleTxtColor = titleTxtColor
          this.mbConfig = mbConfig
          this.bgColor = data.bgColor.color;
          this.imgUrl = data.imgConfig.url

          this.listRight = lrConfig.val;
          this.goodsNum = data.numberConfig.val
          let list = [];
          for (let i = 0; i < this.goodsNum; i++) {
            list.push(goodsItem)
          }
          this.list = list;
        }
      } catch (e) {
        console.error(e, 'setConfig error')
      }
    }
  }
};
</script> 
<style scoped lang="less">
* {
  border: 0;
  font-size: 100%;
  font-weight: 400;
  vertical-align: initial;
  background: transparent;
}
.bodye-box {
  background-color: #fff;
  border-radius: 12px;
  height: 113px;
  align-items: center;
  .content_newTwo_content:nth-child(1) {
    margin-left: 12px;
  }
}
.mobile-page {
  position: relative;
  width: auto;

  .content_newTwo {
    height: auto;
    background-color: #fff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12px;
    padding-top: 0;
    padding-bottom: 5px;
    box-sizing: border-box;
    background-size: cover;
    > div:first-child {
      width: 100%;
      height: 21px;
      display: flex;
      justify-content: space-between;
    }
    > div:last-child {
      width: 100%;
      display: flex;
      overflow: hidden;
      // display: grid;
      // grid-template-columns: repeat(4, 1fr);
    }
    .content_newTwo_content {
      position: relative;
      display: flex;
      flex-direction: column;
      .content_newTwo_content_name {
        font-family: Source Han Sans, Source Han Sans;
        font-size: 12px;
        font-weight: 500;
        color: #333333;
        line-height: 1;
        margin: 11px 0 2px 0;
        text-align: center;
        height: auto;
        display: -webkit-box;
        overflow: hidden;
        text-overflow: ellipsis;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 1;
      }
    }
  }
  .mytop {
    font-size: 16px;
    color: #333;
    font-weight: 700;
  }
  .content_newFive_tilte2 {
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: Source Han Sans, Source Han Sans;
    font-weight: 500;
    font-size: 12px;
    color: #999999;
    line-height: 16px;
  }
}
.mydian {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  position: absolute;
  right: 0;
  top: 48px;
}
.title {
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  .content_newThree_tilte1 {
    min-width: 84px;
    font-size: 16px;
    color: #333;
    font-weight: 500;
    z-index: 9;
  }
  .content_newThree_tilte2 {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: #999;
    line-height: 16px;
  }
}
.live_icon {
  position: absolute;
  width: 16px;
  height: 16px;
  bottom: 0px !important;
  right: 0px !important;
  border-radius: 50%;
  background-color: #fa5151;
  box-sizing: border-box;
  overflow: hidden;

  i {
    width: 2px;
    height: 6px;
    background-color: #ffffff;
    position: absolute;
    bottom: 3px;
    border-radius: 1px;
    left: 0;
  }

  .one {
    left: 2px;
    width: 2px;
    /* 初始宽度 */
    height: 6px;
    /* 高度 */
    background-color: #ffffff;
    /* 背景颜色 */
    animation: stretch 0.8s infinite alternate;
    /* 应用动画 */

    @keyframes stretch {
      0% {
        height: 6px;
        /* 动画开始时的宽度 */
      }

      100% {
        height: 9px;
        /* 动画结束时的宽度 */
      }
    }
  }

  .two {
    left: 6px;
    width: 2px;
    /* 初始宽度 */
    height: 6px;
    /* 高度 */
    background-color: #ffffff;
    /* 背景颜色 */
    animation: stretch 0.2s infinite alternate;
    /* 应用动画 */

    @keyframes stretch {
      0% {
        height: 6px;
        /* 动画开始时的宽度 */
      }

      100% {
        height: 9px;
        /* 动画结束时的宽度 */
      }
    }
  }

  .three {
    left: 10px;
    width: 2px;
    /* 初始宽度 */
    height: 6px;
    /* 高度 */
    background-color: #ffffff;
    /* 背景颜色 */
    animation: stretch 0.6s infinite alternate;
    /* 应用动画 */

    @keyframes stretch {
      0% {
        height: 6px;
        /* 动画开始时的宽度 */
      }

      100% {
        height: 9px;
        /* 动画结束时的宽度 */
      }
    }
  }
}
.icon {
  width: 57px;
  height: 13px;
  position: absolute;
  top: 12px;
  left: 16px;
  z-index: 0;

  img {
    width: 18px;
    height: 18px;
    margin-right: 5px;
    border-radius: 50%;
  }

  p {
    font-size: 16px;
    color: #282828;
    font-weight: 600;
  }

  .time {
    display: flex;
    align-items: center;
    margin-left: 5px;
    color: #ff4444;

    span {
      width: 20px;
      height: 16px;
      font-size: 12px;
      text-align: center;
      line-height: 16px;
    }

    em {
      font-size: 12px;
      margin: 0 3px;
      font-style: initial;
      font-weight: bold;
    }
  }
}
</style>
