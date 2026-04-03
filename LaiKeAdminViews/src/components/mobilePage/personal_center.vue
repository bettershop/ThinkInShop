<template>
  <div class="mobile-page" ref="mobilePage" :class="tabberinfo.backType == 1 ? 'backgImg':'backgColor'">
    <div class="adv-wrap">
      <div class="isHeader">
        <div class="isHeader_top" v-if="defaultConfig.iconConfig.kjrk">
          <div> <img src="@/assets/imgs/grsz.png" alt="" width="28px" height="28px"></div>
          <div> <img src="@/assets/imgs/grxx.png" alt="" width="28px" height="28px"></div>

        </div>
      </div>
      <div class="card">
        <div class="card_bj">
          <img src="@/assets/images/diy/Default_picture.png" alt="" width="100%" height="100%">
        </div>
        <div class="card_center">
          <div class="card_username">Fans</div>
          <div class="card_btn card_btn1">登录/注册</div>
        </div>
      </div>
      <div class="card_bottom">
        <template v-for="(item, index) in checkListInfo">
          <div :key="index" v-if="iconConfig.checkList.includes(item.type)" :style="{'border-radius':iconConfig.boxRoundedVal+'px', 
            }">
            <div>—</div>
            <span>{{ item.name }}</span>
          </div>
        </template>
      </div>
      <!-- vpi -->
      <!-- <div style="margin: 10px auto 0;" v-if="advConfig.vipShow">
        <img src="@/assets/imgs/gerenzhongx.png" alt="" width="100%">
      </div> -->
      <!-- 我的店铺 -->
      <!-- <div class="relative-box" style="margin: 10px">
        <template> 
          <div class="relative-box-box" style="width: 140px;" :style="{'border-radius':advConfig.boxRoundedVal+'px', 
              'background': advConfig.backgType == 0 ?advConfig.backgColor2 : `linear-gradient(to right,${advConfig.backgColor1},${advConfig.backgColor2})`
            }">
            <div class="relative-box-title">
              <div class="relative-box-title-txtl">我的店铺</div>
              <div class="relative-box-title-txt">管理店铺</div>
            </div>
            <div class="relative-box-img">
              <img class="relative-box-img-img" src="@/assets/imgs/gerenzhongx.png" alt="" width="24px" height="24px">
            </div>
          </div>

          <div class="relative-box-box" style="width: 140px;" :style="{'border-radius':advConfig.boxRoundedVal+'px', 
             'background': advConfig.backgType == 0 ?advConfig.backgColor2 : `linear-gradient(to right,${advConfig.backgColor1},${advConfig.backgColor2})`
          }">
            <div class="relative-box-title">
              <div class="relative-box-title-txtl">银行卡管理</div>
              <div class="relative-box-title-txt">管理银行卡</div>
            </div>
            <div class="relative-box-img">
              <img class="relative-box-img-img" src="@/assets/imgs/gerenzhongx.png" alt="" width="24px" height="24px">
            </div>
          </div>
        </template>
      </div> -->

    </div>

    <!-- 订单模块 -->
    <div class="order" style="margin: 10px" v-if="orderCondfig.isShow">
      <div class="order_top">
        {{orderCondfig.orderTitle}}
        <div>
          <span>查看全部</span>
          <img src="../../assets/images/diy/jiantou.png" alt="" style=" width: 15px;margin-left: 6px;" />
        </div>
      </div>
      <div class="order_bottom">
        <div :style="{'border-radius':orderCondfig.boxRoundedVal+'px', 
                'background': orderCondfig.backgType == 0 ?orderCondfig.backgColor2 : `linear-gradient(to right,${orderCondfig.backgColor1},${orderCondfig.backgColor2})`
              }" v-for="(item,index) in orderCondfig.orderList" :key="index">

          <div>
            <div>
              <img v-if="item.imgUrl" :src="item.imgUrl" alt="" style="width: 28px;height: 28px;" />
              <img v-else src='@/assets/images/diy/Default_picture.png' alt="" style="width: 28px;height: 28px;" />
            </div>
            <span>{{item.name}}</span>
          </div>
        </div> 
      </div> 
    </div>
    <!-- 功能模块 -->
    <template v-if="functionConfig.option == 1"> 
        <div class="listBox" v-if="functionConfig.isShow" style="margin: 10px;min-height:200px;padding-bottom: 20px; ">
            <div class="listBox-title" style="color: #333;">{{functionConfig.titleName}}</div>
            <div class="Functioncenter">
              
              <div class="Functioncenter-box" :style="{'border-radius':functionConfig.boxRoundedVal+'px', 
                    'background': functionConfig.backgType == 0 ?functionConfig.backgColor2 : `linear-gradient(to right,${functionConfig.backgColor1},${functionConfig.backgColor2})`
                  }" v-for="(value,index) in functionConfig.list" :key="index">
              <div class="Functioncenter-img">
                <img v-if="value.imgUrl" :src="value.imgUrl" alt="" style="width: 28px;height: 28px;">
                <img v-else src='@/assets/images/diy/Default_picture.png' alt="" style="width: 28px;height: 28px;">
              </div>
              <div class="Functioncenter-title">{{ value.name }}</div>
            </div>
          </div>
        </div>
    </template>
    <template v-if="functionConfig.option == 2"> 
        <div  v-if="functionConfig.isShow" style="margin: 10px;min-height:200px;padding-bottom: 20px; ">
            <div class="listBox-title" style="color: #333;"> </div>
            <div class="Functioncenter">
              
              <div class="option-two-item" :style="{'border-radius':functionConfig.boxRoundedVal+'px', 
                    'background': functionConfig.backgType == 0 ?functionConfig.backgColor2 : `linear-gradient(to right,${functionConfig.backgColor1},${functionConfig.backgColor2})`
                  }" v-for="(value,index) in functionConfig.list" :key="index">
                  <div style="display: flex;align-items: center;"> 
                     <img v-if="value.imgUrl" :src="value.imgUrl" alt="" style="width: 28px;height: 28px;"></img>
                    <img v-else src='@/assets/images/diy/Default_picture.png' alt="" style="width: 28px;height: 28px;margin-right: 8px;"> 
                    <span class="option-two-title">{{ value.name }}</span>
                  </div>
                  <div style="height: 22px;">
                    <img  src="../../assets/imgs/jiantou.png" alt="" style="width: 16px;height: 22px;">
                  </div> 
                 
              </div>
          </div>
        </div>
    </template>
    <footer>
      <!-- <div style="margin: 10px;">
        <div style="text-align: center;"> 
          <h3 style="color:#333;margin: 10px 0px;">{{ $t("diy.wntj") }}</h3>
        </div>
        <div class="goods-list  " style="grid-template-columns: 1fr;">
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
      </div>  -->
    </footer>
  </div>

</template>

<script> 
import { mapState } from "vuex";

export default {
  name: "personal_center",
  cname: "个人中心",
  icon: "icon-gonggao",
  configName: "c_personal_center",
  defaultName: "c_personal_center", // 外面匹配名称
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

        this.setConfig(data);
      },
      deep: true
    }
  },
  data() {
    return {
      defaultConfig: {
        configName: "c_personal_center",
        defaultName: "c_personal_center",
        name: "personal_center",
        pointName: 'personal_center',
        icon: "icon-gonggao",
        cname: "个人中心", 
        timestamp: this.num,
        pageConfig: {},
        iconConfig: {
          kjrk: true,
          iconURl1: '',//图标一
          iconURl2: '',
          iconShow1: true,//图标一是否显示
          iconShow2: true,
          iconlocation1: 'left',//图标一位置 left right
          iconlocation2: 'left',

          boxType: 0,
          boxRoundedVal: 0,//边框圆角

          backgType: 0,//背景类型 0纯色 1渐变

          backgColor1: '#FFFFFF',//背景颜色1
          backgColor2: '#FFFFFF',//背景颜色2
          checkList: ['kq', 'ye', 'jf', 'sc'],

        },
        advConfig: {
          vipShow: true,
          shopShow: true,
          boxType: 0,
          backgType: 0,
          boxRoundedVal: 0,//边框圆角
          backgColor1: '#FFFFFF',//背景颜色1
          backgColor2: '#FFFFFF',//背景颜色2
        },
        orderCondfig: {
          isShow: true,
          orderTitle: '我的订单',
          orderList: [
            {
              imgUrl: '',//图标
              type: 0,
              name: '待付款',
            },
            {
              imgUrl: '',//图标
              type: 1,
              name: '待发货',
            },
            {
              imgUrl: '',//图标
              type: 2,
              name: '待收货',
            },
            {
              imgUrl: '',//图标
              type: 7,
              name: '订单关闭',
            },
            {
              imgUrl: '',//图标
              type: 5,
              name: '已完成',
            },
          ],

          boxType: 0,
          backgType: 0,
          boxRoundedVal: 0,//边框圆角
          backgColor1: '#FFFFFF',//背景颜色1
          backgColor2: '#FFFFFF',//背景颜色2
        },
        functionConfig: {
          isShow: true,
          titleName: '功能中心',
          list: [],
          boxType: 0,
          backgType: 0,
          boxRoundedVal: 0,//边框圆角
          backgColor1: '#FFFFFF',//背景颜色1
          backgColor2: '#FFFFFF',//背景颜色2
          option:1,
        },
        goodsConfig: {
          goodsShow: true, // 是否显示推荐商品
          layoutType: 1, // 1 一排两个; 2 一排一个; 3 纵向显示  
        },
      },

      checkListInfo: [{
        type: 'kq',
        name: '卡券'
      }, {
        type: 'ye',
        name: '余额'
      }, {
        type: 'jf',
        name: '积分'
      }, {
        type: 'sc',
        name: '收藏'
      }],
      iconConfig: {
        checkList: ['kq', 'ye', 'jf', 'sc'],
      },
      advConfig: {},
      orderCondfig: {},
      functionConfig: {},
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
      if (data && Object.keys(data).length) {
        this.defaultConfig.pageConfig = data.pageConfig
        this.iconConfig = data.iconConfig
        this.advConfig = data.advConfig
        this.orderCondfig = data.orderCondfig
        this.functionConfig = data.functionConfig
      }
    }
  }
};
</script>

<style scoped lang="less">
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
.option-two-item{
  display: flex;
      height: 48px;
  align-items: center;
  justify-content: space-between;  
  margin-bottom: 8px; 
  box-sizing: border-box;
  padding: 0 12px 0 12px;
  color: #333;
  font-size: 16px;
}
.option-two-title{
  height: 22px;
    color: #333;
}
.mobile-page {
  // background-image: url('../../assets/images/diy/grbj.png');
  background-size: cover;
  background-color: #f4f5f6;

  position: relative;
  width: auto;
  padding-bottom: 1px;
  .isHeader {
    width: 100%;
    height: auto;
    display: flex;
    .isHeader_top {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 16px;
      box-sizing: border-box;
    }
  }
  .card {
    display: flex;
    margin: 8px 15px 4px 15px;
    position: relative;
  }
  .card_bj {
    width: 54px;
    height: 54px;
    border-radius: 54px;
    padding: 1px;
    background-color: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-right: 12px;
    img {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      margin-right: 12px;
      background-color: #ddd;
      margin: 1px;
    }
  }
  .card_username {
    font-size: 15px;
    line-height: 15px;
    color: #333;
    margin-top: 6px;
    font-weight: 700;
    display: flex;
    align-items: center;
  }
  .card_btn {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 10px;
  }
  .card_center {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    position: relative;
    justify-content: center;
  }
  .card_btn1 {
    margin-left: 0;
    margin-top: 8px;
    background-color: initial;
    color: #fa5151;
    border: 1px solid #fa5151;
    box-sizing: border-box;
    font-size: 12px;
    height: 28px;
    border-radius: 24px;
  }

  .card_bottom {
    display: flex;
    padding: 16px 15px;
    > div {
      flex: 1;
      height: 35px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      > div {
        font-size: 16px;
        font-weight: bold;
        line-height: 13px;
        color: #333;
        font-family: DIN;
      }
      span {
        font-size: 12px;
        line-height: 12px;
        color: #888;
        margin-top: auto;
      }
    }
  }

  .relative-box {
    display: flex;
    justify-content: space-between;
    border-bottom-right-radius: 12px;
    border-bottom-left-radius: 12px;
    .relative-box-box {
      // width: 167px;
      height: 64px;
      display: flex;
      justify-content: space-between;
      background-color: #fff;
      // border-radius: 8px;
    }
    .relative-box-title-txtl {
      font-size: 14px;
      font-weight: 500;
      color: #333;
      line-height: 20px;
    }
    .relative-box-title-txt {
      font-size: 12px;
      color: #666;
      margin-top: 4px;
    }
    .relative-box-title {
      font-size: 8px;
      margin-left: 12px;
      color: #333;
      display: flex;
      flex-direction: column;
      font-weight: 500;
      justify-content: center;
    }
    .relative-box-img {
      display: flex;
      align-items: center;
      margin-right: 12px;
    }
  }
  .order {
    height: 114px;
    background: #fff;
    border-radius: 12px;
    margin: auto;
    .order_top {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 14px 12px 0 14px;
      font-size: 16px;
      line-height: 15px;
      color: #333;
      div {
        display: flex;
        align-items: end;

        font-size: 12px;
        color: #999;
      }
    }
    .order_bottom {
      display: flex;
      align-items: center;
      padding: 12px 10px 18px 10px;
      position: relative;
      > div {
        display: flex;
        flex-direction: column;
        align-items: center;
        flex: 1;
        div {
          display: flex;
          position: relative;
          flex-direction: column;
          align-items: center;
        }
        span {
          font-size: 12px;
          line-height: 12px;
          height: 16px;
          color: #333;
          margin-top: 8px;
        }
      }
    }
  }
  .listBox {
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 12px;
    margin: 12px auto 0;
    // justify-content: center;
    > div {
      position: relative;
      display: flex;
      align-items: center;
    }
    .listBox-title {
      font-size: 16px;
      margin-left: 12px;
      margin-top: 12px;
    }
    .Functioncenter {
      flex-wrap: wrap;
      padding: 12px 10px 8px 10px;
      display: grid;
      grid-template-columns: repeat(5, 1fr);
    }
    .Functioncenter-box {
      height: 64px;
      flex-direction: column;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .Functioncenter-img {
      position: relative;
      img {
        width: 28px;
        height: 28px;
        display: inline-block;
        overflow: hidden;
        position: relative;
      }
    }
    .Functioncenter-title {
      height: 16px;
      font-size: 12px;
      color: #333;
    }
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
</style>
