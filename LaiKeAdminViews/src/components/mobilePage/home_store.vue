<template>
  <div class="mobile-page  " :style="{background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`,marginTop: mTOP + 'px' }">
    <div class="seckill-box">
      <div class="title1 diy-titleName">
        <!-- <img crossOrigin='anonymous' class="icon" :src="imgUrl" alt=""> -->
        <span class="content_newThree_tilte1" :style="{'color':titleTxtColor.color[0].item}">{{ titleConfig.value }}</span>
        <span class="content_newThree_tilte2">
          <span class="content_newFive_tilte2">更多</span>
          <Icon type="ios-arrow-forward" />
        </span>
      </div>
      <div class="list-wrapper">
        <div class="content_newFour_content" v-for="(item, index) of list" :key="index" :style="{ marginRight: listRight + 'px' }">
          <div>
            <img src='../../assets/images/diy/Default_picture.png' />
          </div>
          <div>
            <div>
              <img src='../../assets/images/diy/Default_picture.png' />
            </div>
          </div>
          <div class="shop-name">自营店</div>
          <div class="content_newFour_btn">
            <div class="gz" v-if="index % 2 == 0">关注</div>
            <div v-else class="gz yiguanzhu newBgcolor">已关注</div>
          </div>
          <!-- 定位图标 -->
          <!-- <div class="live_icon" v-if="index % 3 ==0"> 
            <i class="one"></i>
            <i class="two"></i>
            <i class="three"></i>
          </div> -->
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import homeTitleBg from '@/assets/images/home_title_bg.png'

let storeItem = {
  img: '',
  name: '辉煌家居官方旗舰店',
  price: '234',
  discount: '1.2',
  distance: '500m'
}

export default {
  name: 'home_store',
  cname: '店铺',
  configName: 'c_home_store',
  icon: 'icon-dianpu',
  type: 1, // 0 基础组件 1 营销组件 2工具组件
  defaultName: 'homeStore', // 外面匹配名称
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
        const data = this.$store.state.admin.mobildConfig.defaultArray[this.num];
        if (!data.pointName) {
          this.$set(data, 'pointName', 'home_store')
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
        pointName: 'home_store',
        name: 'homeStore',
        configName: 'c_home_store',
        cname: '店铺',
        icon: 'icon-dianpu',
        timestamp: this.num,
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
          title: '店铺标题',
          value: '优选店铺',
          max: 10
        },
        numberConfig: {
          val: 3
        },
        lrConfig: {
          title: '右边距',
          val: 10,
          min: 10
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
      list: [],
      mTOP: 0,
      listRight: 0,
      bgColor: [
        {
          item: '#cfe6e6'
        },
        {
          item: '#cfe6e6'
        }
      ],
      pageData: {},
      imgUrl: '',
      titleConfig: '',
      storeNum: 0,
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
    };
  },
  mounted() {
    this.$nextTick(() => {
      this.pageData = this.$store.state.admin.mobildConfig.defaultArray[this.num]
      this.setConfig(this.pageData)
    })
  },
  methods: {
    setConfig(data) {
      if (!data) { return }
      if (!data.mbConfig) {
        data = data[this.num]
      }
      this.mTOP = data.mbConfig.val;
      this.listRight = data.lrConfig.val;
      this.bgColor = data.bgColor.color;
      this.storeNum = data.numberConfig.val;

      this.imgUrl = data.imgConfig.url
      this.titleConfig = data.titleConfig
      this.titleTxtColor = data.titleTxtColor

      let list = [];
      for (let i = 0; i < this.storeNum; i++) {
        list.push(storeItem)
      }
      this.list = list
    }
  }
};
</script>

<style scoped lang="less">
.mobile-page {
  padding: 6px 12px 5px;
}
.diy-titleName {
  margin-top: 0px;
}
.gz {
  font-family: Source Han Sans, Source Han Sans;
  font-weight: 500;
  font-size: 12px !important;
  color: #fa5151;
  line-height: 1;
  text-align: center;
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
.shop-name {
  font-family: Source Han Sans, Source Han Sans;
  font-weight: 500;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.851);
  line-height: 1;
  text-align: center;
}
.content_newThree_tilte2 {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
  line-height: 16px;
}
.content_newFour_content {
  // width: 100px;
  height: auto;
  background-color: #ffffff;
  border-radius: 8px;
  padding-bottom: 8px;
  box-sizing: border-box;
  margin-right: 8px;
  //滚动
  display: inline-block;
  position: relative;

  > div:nth-child(1) {
    height: 100px;
    border-radius: 6px;
    overflow: hidden;

    > img {
      width: 100%;
      height: 100%;
    }
  }

  > div:nth-child(2) {
    display: flex;
    align-content: center;
    justify-content: center;

    > div {
      width: 32px;
      height: 32px;
      border-radius: 32px;
      overflow: hidden;
      border: 1px solid rgba(0, 0, 0, 0.1);
      margin-top: -16px;
      position: relative;
      z-index: 1;

      > img {
        width: 100%;
        height: 100%;
      }
    }
  }

  > div:nth-child(3) {
    font-size: 12px;
    font-weight: 500;
    color: rgba(0, 0, 0, 0.85);
    line-height: 17px;
    text-align: center;
    margin: 4px auto;
    // 超出隐藏
    width: 100px;
    height: auto;
    display: block;
    overflow: hidden;
    /*超出隐藏*/
    text-overflow: ellipsis;
    /*隐藏后添加省略号*/
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1; //想显示多少行
  }

  .live_icon {
    flex: none;
    position: absolute;
    width: 14px;
    height: 14px;
    top: 4px !important;
    left: 4px !important;
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

  .content_newFour_btn {
    display: flex;
    justify-content: center;
    align-items: center;

    > div {
      padding: 7px 12px 5px;
      // width: 48px;
      // height: 24px;
      background: rgba(250, 81, 81, 0.1);
      border-radius: 20px;
      font-size: 12px;
      font-weight: 500;
      color: #fa5151;
      text-align: center;
    }

    .yiguanzhu {
      font-weight: 400 !important;
      color: #999999 !important;
    }
  }

  .newBgcolor {
    background: rgba(244, 245, 246, 1) !important;
  }
}

.seckill-box {
  // padding: 12px 0px;

  .list-wrapper {
    display: flex;
    margin-top: 8px;
    overflow: hidden;

    .list-item {
      flex-shrink: 0;
      width: 108px;

      .img-box {
        position: relative;
        width: 100%;
        height: 108px;

        img,
        .box {
          width: 100%;
          height: 100%;
          border-radius: 8px;
        }

        .box {
          background: #d8d8d8;
        }

        .discount {
          position: absolute;
          left: 8px;
          bottom: 8px;
          height: 18px;
          padding: 0 3px;
          line-height: 18px;
          background: rgba(255, 255, 255, 1);
          border-radius: 2px;
          border: 1px solid transparent;
          font-size: 12px;
        }
      }

      .title {
        margin-top: 5px;
        font-size: 13px;
        color: #282828;
        font-weight: 500;
      }

      .price {
        display: flex;
        align-items: center;

        .label {
          font-size: 9px;
          width: 16px;
          height: 16px;
          color: #fff;
          text-align: center;
          line-height: 16px;
        }

        .num-label {
          color: #999999;
          font-size: 12px;
          font-weight: 600;
          margin: 1px 2px 0;
        }

        .num {
          color: #999999;
          font-size: 16px;
          font-weight: 600;
        }
      }
    }
  }
}

.home_title {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.home_title > span {
  font-weight: bold;
}

.title1 {
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