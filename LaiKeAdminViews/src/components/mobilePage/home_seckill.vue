<template>
  <div class="mobile-page paddingBox" :style="{background: `linear-gradient(90deg,${bgColor2[0].item} 0%,${bgColor2[1].item} 100%)`,marginTop:`${mTOP}px`}">
    <div class="seckill-box">
      <!-- <div class="hd diy-titleName">
        <div class="left">
          <img crossOrigin='anonymous' :src="imgUrl" alt="" />
        </div>
        <div class="right">
          <span class="content_newThree_tilte2">
            <span class="content_newFive_tilte2">更多</span>
            <Icon type="ios-arrow-forward" />
          </span>
        </div>
      </div> -->

      <div class="title diy-titleName">
        <!-- <img crossOrigin='anonymous' class="icon" :src="imgUrl" alt=""> -->
        <span class="content_newThree_tilte1" :style="{'color':titleTxtColor.color[0].item}">{{ titleConfig.value }}</span>
        <span class="content_newThree_tilte2">
          <span class="content_newFive_tilte2">更多</span>
          <Icon type="ios-arrow-forward" />
        </span>
      </div>
      <div class="list-wrapper">
        <div class="list-item" v-for="(item, index) in list" :index="index" :style="{ marginTop: (index>0?listRight:0) + 'px'}" :key="index">
          <div class="img-box">
            <img crossOrigin='anonymous' :src="item.img" alt="" v-if="item.img" />
            <div class="empty-box"> <img src='../../assets/images/diy/Default_picture.png' alt=""></div>
          </div>
          <div class="rigth-box">
            <div class="title line1">{{ item.name }}</div>
            <div class="rendering">

              <div class="progress"> 已抢86% </div>
            </div>
            <div class="price">
              <div class="left">
                <div class="x-price">
                  <span class="num-label" :style="{ color: themeColor }">￥</span>
                  <span class="  diy-newPrice" :style="{ color: themeColor }">
                    {{item.price}}
                  </span>
                </div>
                <div class="but">
                  立即抢购
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import homeSeckillTitle from '@/assets/images/home_seckillTitle.png'
import seckillBtn from '@/assets/images/seckill_btn.png'

let goodsItem = {
  img: '',
  name: '小米家用电饭煲小米家用电饭煲',
  price: '234',
  discount: '1.2'
};

export default {
  name: 'home_seckill',
  cname: '秒杀',
  configName: 'c_home_seckill',
  icon: 'iconmiaosha',
  type: 1, // 0 基础组件 1 营销组件 2工具组件
  defaultName: 'seckill', // 外面匹配名称
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
          this.$set(data, 'pointName', 'home_seckill')
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
        pointName: 'home_seckill',
        name: 'seckill',
        cname: '秒杀',
        configName: 'c_home_seckill',
        timestamp: this.num,
        countDownColor: {
          title: '倒计时背景色',
          name: 'countDownColor',
          default: [
            {
              item: '#f88e28'
            }
          ],
          color: [
            {
              item: '#f88e28'
            }
          ]
        },
        themeColor: {
          title: '主题风格',
          name: 'themeColor',
          default: [
            {
              item: '#f88e28'
            }
          ],
          color: [
            {
              item: '#f88e28'
            }
          ]
        },
        titleConfig: {
          title: '标题名称',
          value: '限时秒杀',
          max: 10
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
          title: '最多可添加1张图片，建议宽度206 * 49px',
          url: homeSeckillTitle
        },
        seckillBtnImgConfig: {
          title: '最多可添加1张图片，建议宽度25 * 19px',
          url: seckillBtn
        }
      },
      list: [],
      mTOP: 0,
      listRight: 0,
      bgColor: '', // 倒计时背景色
      bgColor2: [{
        item: '#cfe6e6'
      },
      {
        item: '#cfe6e6'
      }], // 组件背景色
      titleConfig: {
        title: '标题名称',
        value: '限时秒杀',
        max: 10
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
      themeColor: '',
      pageData: {},
      imgUrl: '',
      seckillBtnImg: '',
      goodsNum: 3
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
      if (data) {
        if (!data.mbConfig) {
          data = data[this.num]
        }
        this.titleConfig = data.titleConfig
        this.mTOP = data.mbConfig.val;
        this.listRight = data.lrConfig.val;
        this.bgColor = data.countDownColor.color[0].item;
        this.bgColor2 = data.bgColor.color;
        this.themeColor = data.themeColor.color[0].item;
        this.imgUrl = data.imgConfig.url
        this.seckillBtnImg = data.seckillBtnImgConfig.url
        this.goodsNum = data.numberConfig.val
        console.log(this.goodsNum);

        let list = [];
        for (let i = 0; i < this.goodsNum; i++) {
          list.push(goodsItem)
        }
        this.list = list;
      }
    }
  }
};
</script>

<style scoped lang="less">
.rendering {
  width: 106px;
  height: 20px;
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #fa5151;
  position: relative;
  margin: 4px 0px 8px 0px;
  .progress {
    position: absolute;
    top: 0px;
    left: 0px;

    width: 95px;
    height: 20px;
    background: rgba(250, 81, 81, 0.1);
    border-radius: 4px;
    color: #fa5151;

    text-align: center;

    font-family: Source Han Sans, Source Han Sans;
    font-weight: 500;
    font-size: 12px;
  }
}
.diy-titleName {
  margin-top: 0px;
  padding-top: 6px;
}
.content_newThree_tilte2 {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
  line-height: 16px;
}

.paddingBox {
  padding: 0px 12px 5px;
}
.rigth-box {
  margin-left: 12px;
}
.but {
  margin-left: auto;
  font-family: Source Han Sans, Source Han Sans;
  width: 74px;
  height: 28px;
  background-color: rgba(250, 81, 81, 0.1);
  border-radius: 14px;
  font-size: 12px;
  font-weight: 500;
  color: #fa5151;
  line-height: 28px;
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
.seckill-box {
  // background: #fff;

  .hd {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;

      img {
        width: 103px;
        height: 25px;
        /* margin-right 5px */
        /* border-radius 50% */
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
        color: #fff;

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
  }

  .list-wrapper {
    // display: flex;
    // margin-top: 8px;
    overflow: hidden;
    .list-item:nth-child(n + 2) {
      margin-top: 8px;
    }
    .list-item {
      padding: 8px;
      flex-shrink: 0;
      background: #ffffff;
      border-radius: 8px 8px 8px 8px;
      display: flex;
      // width: 130px;

      .img-box {
        position: relative;
        width: 88px;
        height: 88px;

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
        color: #282828;
        font-family: Source Han Sans, Source Han Sans;
        font-weight: 500;
        font-size: 14px;
        color: rgba(0, 0, 0, 0.851);
        line-height: 20px;
        text-align: left;
        font-style: normal;
        text-transform: none;
        width: 176px;
      }

      .price {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .label {
          font-size: 9px;
          width: 25px;
          height: 19px;
          color: #fff;
          text-align: center;
          line-height: 16px;

          img {
            width: 100%;
            height: 100%;
          }
        }

        .num-label {
          color: #ff4444;
          font-size: 12px;
          margin-right: -3px;
        }

        /* font-weight: 600; */
        /* margin 1px 2px 0 */
        .num {
          color: #ff4444;
          font-size: 15px;
          font-weight: 600;
        }

        .left {
          display: flex;
          align-items: center;
          width: 100%;
          .y-price {
            margin-left: 5px;
            font-size: 12px;
            text-decoration: line-through;

            .num-label {
              color: #9a9a9a;
            }

            .num {
              color: #9a9a9a;
            }
          }
        }
      }
    }
  }
}
</style>
