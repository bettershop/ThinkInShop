<template>
  <div class="mobile-page" :style="{'margin-top':mbConfig.val+'px',background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}">
    <div class="isPublicModeFX">
      <div class="content_newThree">
        <div class="title diy-titleName">
          <!-- <img crossOrigin='anonymous' class="icon" :src="imgUrl" alt=""> -->
          <span class="content_newThree_tilte1" :style="{'color':titleTxtColor.color[0].item}">{{ titleConfig.value }}</span>
          <span class="content_newThree_tilte2">
            <span class="content_newFive_tilte2">更多</span>
            <Icon type="ios-arrow-forward" />
          </span>
        </div>
        <div class="Fx-body">
          <div class="content_newThree_content" v-for="(item,index) in list" :key="index">
            <div class="img">
              <img src='../../assets/images/diy/Default_picture.png' alt="">
            </div>
            <div class="content_newThree_name">
              <div class="overflowHide goods-name">商品名称</div>
              <div class="zgkz">最高可赚 10.78</div>
              <div class="content_newThree_price">
                <div>
                  <span class="new-pric diy-newPrice">¥49.00</span>
                  <span class="lod-pric diy-lodPrice">¥49.00</span>
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
import { mapState } from "vuex";
import homeTitleBg from '@/assets/images/home_title_bg.png'
let goodsItem = {
  img: '',
  name: '商品名称',
  price: '234',
  discount: '1.2'
};
export default {
  name: "PublicModeFX", // 组件名称
  cname: "分销", // 标题名称
  icon: "icontupianguanggao",
  defaultName: "defaul_PublicModeFX", // 外面匹配名称
  configName: "c_publicModeFX", // 右侧配置名称
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
          this.$set(data, 'pointName', 'PublicModeFX')
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
        configName: "c_publicModeFX",
        name: "PublicModeFX",
        pointName: 'PublicModeFX',
        cname: "PublicModeFX", // 标题名称
        icon: "icontupianguanggao",
        timestamp: this.num,

        numberConfig: {
          val: 3
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
          value: 'GO！分享赚',
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
      titleConfig: {  //标题
        title: '标题名称',
        value: 'GO！分享赚',
        max: 10
      },
      titleTxtColor: { //标题颜色
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
      goodsNum: 3,
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
          const { titleConfig, titleTxtColor, mbConfig } = data
          this.titleConfig = titleConfig
          this.titleTxtColor = titleTxtColor
          this.mbConfig = mbConfig
          this.imgUrl = data.imgConfig.url
          this.bgColor = data.bgColor.color;

          this.goodsNum = data.numberConfig.val
          let list = [];
          for (let i = 0; i < this.goodsNum; i++) {
            list.push(goodsItem)
          }
          this.list = list;
        }
      } catch (error) {
        console.error('error', error)
      }

    }
  }
};
</script>
 
<style scoped lang="less">
* {
  user-select: none;
  border: 0;
  font-size: 100%;
  font-weight: 400;
  vertical-align: initial;
  background: transparent;
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
.goods-name {
  font-family: Source Han Sans, Source Han Sans;
  font-weight: 500;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.851);
  text-align: left;
  font-style: normal;
  text-transform: none;
}
.mobile-page {
  position: relative;
  width: auto;
  padding: 12px;
  padding-top: 0px;
  padding-bottom: 5px;
  .content_newThree {
    height: auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
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
    .Fx-body {
      width: 100%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
    }

    .content_newThree_content {
      width: 100%;
      display: flex;
      flex-direction: row;
      align-items: center;
      background-color: #fff;
      padding: 8px;
      box-sizing: border-box;
      border-radius: 8px;
    }
    .content_newThree_content:nth-child(n + 2) {
      margin-top: 8px;
    }
    .img {
      width: 88px;
      height: 88px;
      border-radius: 8px;
      overflow: hidden;
      img {
        width: 96px;
        height: 96px;
        object-fit: fill;
      }
    }
    .content_newThree_name {
      flex: 1;
      flex-direction: column;
      justify-content: space-between;
      margin-left: 12px;
      .overflowHide {
        font-size: 14px;
        font-weight: 500;
        color: rgba(0, 0, 0, 0.85);
        line-height: 20px;
        margin: 4px 0;
      }
      .zgkz {
        font-family: Source Han Sans, Source Han Sans;
        display: inline-block !important;
        padding-right: 6px;
        background: rgba(250, 81, 81, 0.1);
        border-radius: 2px 10px 10px 2px;
        border-left: 2px solid #fa5151;
        font-size: 12px;
        font-weight: 500;
        color: #fa5151;
        line-height: 20px;
        padding-left: 2px;
        box-sizing: border-box;
      }
      .content_newThree_price {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        margin-top: 8px;
        > div:first-child {
          font-size: 12px;
          font-weight: 600;
          color: #fa5151;
          line-height: 16px;
        }
        .new-pric {
          font-size: 17px;
          font-weight: 700;
          font-family: DIN-Bold, DIN;
          line-height: 1;
          margin: 0 5px 0 2px;
        }
        .lod-pric {
          color: #999;
          font-weight: 400;
          text-decoration: line-through;
        }
        .but {
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
}
</style>
