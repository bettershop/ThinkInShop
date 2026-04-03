<template>
  <div class="mobile-page  ">
    <div class="isPublicModeVIP" :style="{'margin-top':mbConfig.val+'px',background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}">
      <div class="content_newTwo">
        <div style=" width: 100%;">
          <div class="title1 diy-titleName">
            <!-- <img crossOrigin='anonymous' class="icon" :src="imgUrl" alt=""> -->
            <span class="content_newThree_tilte1" :style="{'color':titleTxtColor.color[0].item}">{{ titleConfig.value }}</span>
            <span class="content_newThree_tilte2">
              <span class="content_newFive_tilte2">更多</span>
              <Icon type="ios-arrow-forward" />
            </span>
          </div>
          <!-- <img src="../../assets/images/diy/new_home_huiyuan1.png" alt="" style="width: 100%;height: 100%;"> -->
        </div>
        <div class="goods-box">
          <div class="content_newTwo_content  list-item" v-for="(item,index) in list" :key="index" :style="{ marginRight: listRight + 'px'}">
            <div class="img-box">
              <img crossOrigin='anonymous' :src="item.img" alt="" v-if="item.img" />
              <div class="empty-box"> <img src='../../assets/images/diy/Default_picture.png' alt=""></div>
            </div>
            <div class="googs-name title line1 ">{{ item.name }}</div>
            <div class="price">
              <div class="left">
                <div class="x-price">
                  <span class="num-label">￥</span>
                  <span class="num diy-newPrice">
                    {{item.price}}
                  </span>
                </div>
                <div class="y-price">
                  <span class="num-label">￥</span>
                  <span class="  diy-lodPrice">
                    {{item.price}}
                  </span>
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
import Cookies from 'js-cookie'
import homeTitleBg from '@/assets/images/home_title_bg.png'

let goodsItem = {
  img: '',
  name: '小米家用电饭煲小米家用电饭煲',
  price: '1.25',
  discount: '1.2'
};
export default {
  name: "VIP", // 组件名称
  cname: "VIP", // 标题名称
  icon: "icontupianguanggao",
  defaultName: "defaul_VIP", // 外面匹配名称
  configName: "c_publicModeVIP", // 右侧配置名称
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
          this.$set(data, 'pointName', 'VIP')
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
        name: "VIP",
        pointName: 'VIP',
        cname: "VIP", // 标题名称
        icon: "icontupianguanggao",
        configName: "c_publicModeVIP",
        timestamp: this.num,

        numberConfig: {
          val: 3
        },
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
          value: '会员专区',
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
      goodsNum: 3,
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

          this.titleConfig = titleConfig
          this.titleTxtColor = titleTxtColor
          this.mbConfig = mbConfig
          this.imgUrl = data.imgConfig.url
          this.bgColor = data.bgColor.color;
          this.listRight = lrConfig.val;
          this.goodsNum = data.numberConfig.val
          let list = [];
          for (let i = 0; i < this.goodsNum; i++) {
            list.push(goodsItem)
          }
          this.list = list;
        }
      } catch (e) {
        console.error(e, 'e')
      }
    }

  }
};
</script>
 
<style scoped lang="less">
.empty-box {
  border: 1px solid #c6ab6b;
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
.googs-name {
  font-family: Source Han Sans, Source Han Sans;
  font-weight: 500;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.851);
  line-height: 17px;
  text-align: left;
  font-style: normal;
  text-transform: none;
}
.goods-box {
  width: 100%;
  display: flex;
  flex-direction: row;
  background-color: #ffffff;
  padding: 10px;
  border-radius: 12px 12px 12px 12px;
  overflow: hidden;
}
.mobile-page {
  position: relative;
  width: auto;
  .content_newTwo {
    height: auto;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12px;
    padding-top: 0;
    padding-bottom: 5px;
    box-sizing: border-box;

    > div:last-child {
      width: 100%;
      display: flex;
      flex-direction: row;
    }

    .content_newTwo_price {
      display: flex;
      align-items: flex-end;
      width: 100px;
      display: -webkit-inline-box;
      overflow: hidden;
      text-overflow: ellipsis;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      > div:nth-child(1) {
        font-size: 12px;
        font-weight: 600;
        color: #fa5151;
        line-height: 16px;
      }
      > div:nth-child(2) {
        font-size: 10px;
        font-weight: 400;
        color: #999;
        text-decoration: line-through;
      }
    }
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
}
.list-item {
  flex-shrink: 0;
  width: 100px;

  .img-box {
    position: relative;
    width: 100%;
    height: 100px;

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
    // font-size: 13px;
    color: #282828;
  }

  .price {
    display: flex;
    // align-items: center;
    justify-content: space-between;
    line-height: 1;
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

      .y-price {
        margin-left: 5px;
        font-size: 12px;
        text-decoration: line-through;
        margin-top: auto;
        .num-label {
          color: #9a9a9a;
        }
      }
    }
  }
}
</style>
 