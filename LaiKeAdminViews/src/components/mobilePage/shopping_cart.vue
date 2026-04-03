<template>
  <div class="mobile-page" :class="tabberinfo.backType == 1 ? 'backgImg':'backgColor'">
    <div class="adv-wrap">
      <div class="adv" v-if="!defaultConfig.pageConfig.shppingType" ref="mobilePage">
        <header>
          <div class="page-title">
            <!-- 页面名称 -->
            <h5 v-if="defaultConfig.pageConfig.pageNameShow" style="font-size: 18px; color: #333;">{{ defaultConfig.pageConfig.pageName }}</h5>
          </div>
        </header>

        <body style=" background: transparent;">
          <!-- 商品标签 -->
          <div>
            <ul class="is_switchNav" v-if="defaultConfig.pageConfig.tabIsShow">
              <li class="is_switchNav_li" v-for="(item,index) in  defaultConfig.pageConfig.pageTypeList" :key="index">{{ item.name }}</li>
              <li class="is_switchNav_line"> </li>
            </ul>
          </div>

          <!-- 购物车主体 -->
          <div>
            <div class="empty" :style="{'background-color':defaultConfig.pageConfig.boxBackColor,'border-radius':defaultConfig.pageConfig.boxRoundedVal+'px'}">

              <img src="@/assets/images/lllaaa.png" alt="购物车空" v-if="!defaultConfig.pageConfig.imgConfig.imgVal">
              <img :src="defaultConfig.pageConfig.imgConfig.imgVal" alt="购物车空" v-else>
              <p>{{ defaultConfig.pageConfig.imgConfig.description }}</p>
              <!-- 逛逛按钮 -->
              <div class="toHomeBtn" v-if="defaultConfig.pageConfig.butConfig.isShow" :style="{'border-radius': defaultConfig.pageConfig.butConfig.roundedVal+'px',
                      'background': defaultConfig.pageConfig.butConfig.backgType == 0 ? defaultConfig.pageConfig.butConfig.backgColor2 : `linear-gradient(to right,${defaultConfig.pageConfig.butConfig.backgColor1},${defaultConfig.pageConfig.butConfig.backgColor2})`
              }">{{ defaultConfig.pageConfig.butConfig.title }}</div>
            </div>
          </div>
        </body>
        <footer>
          <div v-if="defaultConfig.pageConfig.goodsConfig.goodsShow">

            <div style="text-align: center;">
              <!-- <img src="@/assets/images/fgt.png" alt="" width="100px"> -->
              <h3 style="color:#333;margin: 10px 0px;">{{ $t("diy.wntj") }}</h3>
            </div>
            <div class="goods-list" :style="{ 
                gridTemplateColumns: defaultConfig.pageConfig.goodsConfig.layoutType != 1 ? '1fr' : '1fr',
              }" :class="{'goods-list3':defaultConfig.pageConfig.goodsConfig.layoutType == 3,'goods-list2':defaultConfig.pageConfig.goodsConfig.layoutType == 2}">
              <template>

                <div class="product-grid">
                  <div v-for="(product, index) in products" :key="index" class="product-card">
                    <img src='../../assets/images/diy/Default_picture.png' alt="商品图片" class="product-image" />
                    <div class="product-details">
                      <div class="store-name">{{ product.storeName }}</div>
                      <div class="product-title">{{ product.title }}</div>
                      <div class="price">
                        <span>
                          <span class="current-price">¥{{ product.price }}</span>
                          <span class="original-price">¥{{ product.originalPrice }}</span>
                        </span>
                        <img class="product-icon" src='../../assets/images/diy/redgwc.png' alt="">
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>

        </footer>
      </div>
      <div v-else>
        <!-- 悬浮球 -->
        <div class="cartImg">
          <div class="img-box" :style="{
                'background': defaultConfig.pageConfig.supendedConfig.backgType == 0 ? defaultConfig.pageConfig.supendedConfig.backgColor2 : `linear-gradient(to right,${defaultConfig.pageConfig.supendedConfig.backgColor1},${defaultConfig.pageConfig.supendedConfig.backgColor2})`,
                'border-radius': defaultConfig.pageConfig.supendedConfig.roundedVal+'px', 
              }">

            <img :src="defaultConfig.pageConfig.supendedConfig.imgVal" v-if="defaultConfig.pageConfig.supendedConfig.imgVal" :style="{width:  defaultConfig.pageConfig.supendedConfig.imgsiez+'px'}" alt="购物车图标" />

            <img src="@/assets/imgs/home_one.png" v-else :style="{width:  defaultConfig.pageConfig.supendedConfig.imgsiez+'px'}" alt="购物车图标" />
            <span class="title" v-if="defaultConfig.pageConfig.supendedConfig.sunShow">8</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script> 
import { mapState } from "vuex";

export default {
  name: "shopping_cart",
  cname: "购物车",
  icon: "icon-gonggao",
  configName: "shoppingCart",
  defaultName: "shoppingCart", // 外面匹配名称
  type: 2, // 0 基础组件 1 营销组件 2工具组件
  props: {
    index: {
      type: null
    },
    num: {
      type: [String, Number]
    }
  },
  computed: {
    ...mapState("admin/mobildConfig", ["defaultArray", "tabberinfo"])
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
        console.log('numnumnum nVal', nVal)
        if (nVal) {

          const data = this.$store.state.admin.mobildConfig.defaultArray[nVal];
          this.setConfig(data);
        }
      },
      deep: true

    },
    // tabberinfo: {
    //   handler(nVal, oVal) {
    //     console.log(nVal.backType, 'oValtabberinfo')
    //     const element = this.$refs.mobilePage
    //     if (nVal.backType == 0) {
    //       element.style.backgroundColor = nVal.colorTwo;
    //     } else {
    //       element.style.backgroundImage = `url(${nVal.Imgurl})`;
    //       element.style.backgroundSize = '100% 100%';
    //       element.style.backgroundRepeat = 'no-repeat'; // 不重复
    //       element.style.backgroundPosition = 'center'; // 居中显示
    //       element.style.backgroundColor = 'transparent';
    //     }
    //   },
    //   deep: true,
    //   immediate: true
    // },
    defaultArray: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[
          this.num
        ];

        this.setConfig(data);
      },
      deep: true
    }
  },
  data() {
    return {
      defaultConfig: {
        configName: "shoppingCart",
        defaultName: "shoppingCart",
        name: "shopping_cart",
        pointName: 'shopping_cart',
        icon: "icon-gonggao",
        cname: "购物车",
        timestamp: this.num,
        pageConfig: {
          shppingType: false,
          // 页面名称
          pageName: '购物车',
          // 页面标题显示
          pageNameShow: true,
          // 是否显示tab
          tabIsShow: true,
          // 购物车查询类型
          pageTypeList: [
            {
              name: "实物商品",
            },
            {
              name: "虚拟商品",
            }
          ],
          // 页面背景颜色
          boxBackColor: '#fff',
          boxType: 0,

          boxRoundedVal: '15', // 背景圆角弧度
          imgConfig: {
            description: '购物车内暂无商品',
            imgVal: ''
          },
          // 按钮按钮显示
          butConfig: {
            title: '去逛逛',
            toUrl: '2',
            isShow: true, // 是否显示按钮
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
            ],
            roundedVal: '18',//圆角幅度 
            backgType: '1',// 1 纯色 ;2 渐变
            backgColor1: '', // 按钮颜色1
            backgColor2: '',// 按钮颜色2
          },
          goodsConfig: {
            goodsShow: true, // 是否显示推荐商品
            layoutType: 1, // 1 一排两个; 2 一排一个; 3 纵向显示  
          },
          // 悬浮球配置
          supendedConfig: {
            imgVal: '', // 悬浮球图片
            imgsiez: '40', // 悬浮球图片大小
            type: 1, // 1 直角 0 圆角
            roundedVal: '0', // 圆角幅度
            sunShow: true, // 是否显示商品计数
            backgType: 0,
            backgColor1: '#fff',
            backgColor2: '#fff',
            list: [
              {
                val: "圆角",
                icon: "iconPic_fillet"
              },
              {
                val: "直角",
                icon: "iconPic_square"
              }
            ],
          }
        },

      },
      products: [
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 经典T恤休闲短袖T恤 经典T恤',
          price: 219,
          originalPrice: 300.99
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 经典T恤休闲短袖T恤 经典T恤',
          price: 219,
          originalPrice: 300.99
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 经典T恤休闲短袖T恤 经典T恤',
          price: 219,
          originalPrice: 300.99
        },]
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
      console.log(data, 'datadatadatadata')
      this.defaultConfig.pageConfig = data.pageConfig
    }
  }
};
</script>

<style scoped lang="less">
.img-box {
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 40px;
  min-height: 40px;
  max-width: 55px;
  max-height: 55px;
  overflow: hidden;
}
.backgImg {
  background-image: var(--imgurl);
  background-size: 100% 100%;
  background-repeat: 'no-repeat'; // 不重复
  background-position: 'center'; // 居中显示
  background-color: 'transparent'; // 背景色设为透明
}
.backgColor {
  background-color: var(--colorTwo);
}
.mobile-page {
  background-size: cover;
  position: relative;
  width: auto;

  .adv-wrap {
    // border-radius: 15px ;
  }

  .adv {
    padding: 10px 15px;

    .page-title {
      text-align: center;
    }
  }
  .is_switchNav {
    width: 100%;
    height: auto;
    display: flex;
    align-items: center;
    justify-content: space-around;

    .is_switchNav_li {
      position: relative;
      z-index: 2;
      flex: 1;
      height: 35px;
      line-height: 35px;
      font-size: 14px;
      text-align: center;
      user-select: none;
      cursor: pointer;
      color: rgb(153, 153, 153);
    }
    .is_switchNav_li:nth-child(1) {
      color: rgb(51, 51, 51) !important;
      font-weight: 700 !important;
      font-size: 16px !important;
    }
    .is_switchNav_line {
      left: 60px;
      border-bottom: 2px solid rgb(250, 81, 81);
      position: absolute;

      z-index: 1;
      width: 50px;
      height: 35px;
    }
  }
  .empty {
    text-align: center;
    font-size: 15px;
    color: #9d9d9d;
    background-color: #fff;
    padding: 20px 0;
    border-radius: 12px;
    margin: 0 16px;
    img {
      width: 100%;
      height: 120px;
      margin: 0;
    }
    p {
      margin-bottom: 24px;
    }
    .toHomeBtn {
      border-radius: 27px;
      background-color: #fa5151;
      color: #fff;
      width: 74px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto;
      cursor: pointer;
    }
  }
  .cartImg {
    display: flex;
    align-items: center;
    justify-content: end;
    padding: 10px 10px 10px;
    img {
      position: relative;
    }
    .title {
      margin-right: 10px;
      position: absolute;
      z-index: 999;
      top: -2%;
      right: -4%;
      width: 25px;
      height: 25px;
      background: #fa5151;
      color: #fff;
      text-align: center;
      line-height: 25px;
      border-radius: 50%;
    }
  }
  .goods-list {
    display: grid;
    gap: '16px';

    .good-img {
      width: 100%;
      border-radius: 8px;
      display: inline-block;
      overflow: hidden;
      position: relative;
    }
    .isWaterfall_other {
      display: flex;
      flex-direction: column;
    }
    .shop_name {
      display: flex;
      align-items: center;
      margin: 8px;
      margin-top: 5px;
      margin-bottom: 4px;
    }
    .goods_name {
      font-size: 14px;
      color: #333;
      line-height: 20px;
      margin: 8px;
      margin-top: 2px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 1;
    }
    .goods_price {
      font-size: 12px;
      padding: 0 8px 8px 8px;
      box-sizing: border-box;
      display: flex;
      align-items: flex-end;
      div:first-child {
        color: #fa5151;
        font-weight: 600;
        line-height: 16px;
        display: flex;
        align-items: flex-end;
      }
      div:last-child {
        color: #666;
        line-height: 16px;
        text-decoration: line-through;
        margin-left: 5px;
      }
    }
  }
  .goods-list2 {
    .product-grid {
      display: flex;
      flex-direction: column;
    }
    .good-img {
      width: 120px;
      height: 120px;
      border-radius: 8px;
      display: inline-block;
      overflow: hidden;
      position: relative;
      margin-right: 12px;
    }

    .product-card {
      display: flex;
      padding: 10px;
    }
    .product-title {
      width: 141px;
      overflow: hidden;
    }
    > div {
      display: flex;
      align-items: center;
      box-shadow: 0px 1px 1px 0px rgba(6, 0, 1, 0.1);
      border-radius: 3px;
      box-sizing: border-box;
      margin-bottom: 12px;
      padding-bottom: 10px;

      .goods_price {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        div:first-child {
          font-size: 20px !important;
          font-family: Source Han Sans CN-Heavy, Source Han Sans CN !important;
          font-weight: 800 !important;
          color: #9a2619 !important;
          margin-bottom: 5px;
        }
        div:last-child {
          font-size: 12px;
          font-family: Source Han Sans CN-Normal, Source Han Sans CN;
          font-weight: 400;
          color: #666;
          margin-left: 0px !important;
        }
      }
    }
  }
  .goods-list3 {
    .good-img {
      width: 100%;
      border-radius: 8px;
      display: inline-block;
      overflow: hidden;
      position: relative;
    }
    .goods_name {
      font-size: 16px !important;
      line-height: 16px !important;
    }
    .goods_price {
      div:first-child {
        font-size: 20px !important;
        font-family: Source Han Sans CN-Heavy, Source Han Sans CN !important;
        font-weight: 800 !important;
        color: #9a2619 !important;
        margin-bottom: 1px;
      }
      div:last-child {
        font-size: 12px;
        font-family: Source Han Sans CN-Normal, Source Han Sans CN;
        font-weight: 400;
        color: #666;
        margin-left: 8px !important;
      }
    }
  }
  .product-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
    .product-card {
      border: 1px solid #eaeaea;
      border-radius: 8px;
      overflow: hidden;
      background-color: white;
      .product-image {
        width: 100%;
        height: 100px;
        object-fit: cover;
      }
      .product-details {
        padding: 8px;
        .store-name {
          font-size: 10px;
          color: #666;
          margin-bottom: 4px;
        }
        .product-title {
          font-size: 12px;
          color: #333;
          margin-bottom: 4px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        .price {
          font-size: 14px;
          color: #ff6700;
          margin-top: 4px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          .current-price {
            font-weight: bold;
          }
          .original-price {
            font-size: 10px;
            color: #999;
            margin-left: 4px;
            text-decoration: line-through;
          }
          .product-icon {
            width: 18px;
            height: 18px;
          }
        }
      }
    }
  }
}
</style>
