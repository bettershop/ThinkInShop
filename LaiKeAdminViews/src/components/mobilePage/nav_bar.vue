<template>
  <div class="mobile-page" :style="{background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`,marginTop:`${cSlider}px`}">
    <div class="menus" v-if="bgColor.length>0">
      <div class="item" v-for="(item,index) in list" :key="index" :class="{on:curIndex == index}" :style="{color:txtColor, borderRightColor: rightBorderColor}">
        <div class="tab-but " :style="{color: index === 0 ? txtSelectColor : txtColor}" style="margin-bottom: 2px;">
          {{ item.name }}
          <div class="subtitle" :style="{color:index== 0?selectBorderColor:''}">{{item.es}}</div>
        </div>
        <span :style="{background:selectBorderColor}" style="width: 48px;"></span>
      </div>

    </div>
    <div class="product-grid">
      <div v-for="(product, index) in products" :key="index" class="product-card" :style="{ 'margin-top': index === 2 ? '-22px' : '', 'height': index === 0 ? '235px' : 'auto'}">
        <img src='../../assets/images/diy/Default_picture.png' alt="商品图片" class="product-image" style="width: 100%;" />
        <div class="product-details" style=" padding: 8px">
          <div class="store-name">{{ product.storeName }}</div>
          <div class="product-title">{{ product.title }}</div>
          <div class="tag-box" v-if="index ==1">
            <div class="jx">精选</div>
            <div class="rm">品牌</div>
          </div>
          <div class="price">
            <span>
              <span class="current-price diy-newPrice">¥{{ product.price }}</span>
              <span class="original-price">{{ product.originalPrice }}</span>
            </span>
            <img class="product-icon" src='../../assets/images/diy/redgwc.png' alt="">
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'nav_bar',
  configName: 'c_nav_bar',
  cname: '商品列表',
  icon: 'iconfenleidaohang',
  type: 0, // 0 基础组件 1 营销组件 2工具组件
  defaultName: 'tabNav', // 外面匹配名称
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
        this.setConfig(nVal)
      },
      deep: true
    },
    num: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[nVal]
        this.setConfig(data)
      },
      deep: true
    },
    defaultArray: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[this.num]
        if (data && !data.pointName) {
          this.$set(data, 'pointName', 'nav_bar')
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
        configName: 'c_nav_bar',
        pointName: "nav_bar",
        name: 'tabNav',
        cname: '商品列表',
        icon: 'iconfenleidaohang',
        timestamp: this.num,
        txtColor: {
          title: '文字颜色',
          name: 'txtColor',
          default: [{
            item: '#020202'
          }],
          color: [
            {
              item: '#020202'
            }

          ]
        },
        txtSelectColor: {
          title: '文字选中颜色',
          name: 'txtColor',
          default: [{
            item: '#014343'
          }],
          color: [
            {
              item: '#014343'
            }

          ]
        },
        rightBorderColor: {
          title: '分割线颜色',
          name: 'txtColor',
          default: [{
            item: 'rgb(161, 206, 206)'
          }],
          color: [
            {
              item: 'rgb(161, 206, 206)'
            }
          ]
        },
        selectBorderColor: {
          title: '选中下标线颜色',
          name: 'txtColor',
          default: [{
            item: '#76e6b6'
          }],
          color: [
            {
              item: '#76e6b6'
            }

          ]
        },
        bgColor: {
          title: '背景颜色',
          name: 'bgColor',
          default: [
            {
              item: '#FFFFFF'
            },
            {
              item: '#FFFFFF'
            }
          ],
          color: [
            {
              item: '#FFFFFF'
            },
            {
              item: '#FFFFFF'
            }
          ]
        },
        // 页面间距
        mbConfig: {
          title: '页面间距',
          val: 0,
          min: 0
        }
      },
      products: [
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 经典T恤休闲短袖T恤 经典T恤',
          price: 219,
          originalPrice: '8人付款'
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 经典T恤休闲短袖T恤 经典T恤',
          price: 219,
          originalPrice: '8人付款'
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 经典T恤休闲短袖T恤 经典T恤',
          price: 219,
          originalPrice: '1人付款'
        },


      ],
      list: [
        {
          name: '推荐',
          es: '精选商品'
        },
        {
          name: '服装',
          es: '热门穿搭'
        },
        {
          name: '箱包',
          es: '潮流好货'
        }
      ],
      curIndex: 0,
      bgColor: [{
        item: 'rgba(207,230,230,1)'
      },
      {
        item: 'rgba(207,230,230,1)'
      }],
      cSlider: 0,
      txtColor: '',
      txtSelectColor: '',
      rightBorderColor: '',
      selectBorderColor: '',
      pageData: {}
    };
  },
  mounted() {
    this.$nextTick(() => {

      this.pageData = this.$store.state.admin.mobildConfig.defaultArray[this.num]
      console.log('nav_bar mounted::', this.pageData)
      this.setConfig(this.pageData)
    })
  },
  methods: {
    setConfig(data) {
      if (data && !data.bgColor) {
        data = data[this.num]
      }
      console.log('nav_bar setConfig::', data)
      if (data && Object.keys(data).length) {

        this.bgColor = data.bgColor.color;
        this.cSlider = data.mbConfig.val;
        this.txtColor = data.txtColor.color[0].item;
        this.txtSelectColor = data.txtSelectColor.color[0].item;
        this.rightBorderColor = data.rightBorderColor.color[0].item;
        this.selectBorderColor = data.selectBorderColor.color[0].item;
      }
    }
  }
};
</script>

<style scoped lang="less">
.mobile-page {
  padding: 0px 0 1px;
}

.menus {
  display: flex;
  align-items: center;
  width: 100%;
  height: 46px;

  .item {
    position: relative;
    text-align: center;
    color: #fff;
    // width: 67px;
    // border-right: 1px solid rgb(161, 206, 206);
    padding: 0px 10px;

    .tab-but {
      font-family: Source Han Sans, Source Han Sans;
      font-weight: 350;
      font-size: 14px;
      color: #999999;
      line-height: 14px;
    }

    &:last-child {
      // border-right: 0px solid rgb(161, 206, 206);
    }

    &:first-child {
      div {
        &:first-child {
          font-weight: bold;
          font-size: 16px;
          color: #333333;
          line-height: 16px;
          text-align: center;
          font-style: normal;
          text-transform: none;
        }
        .subtitle:first-child {
          font-size: 12px;
        }
      }
    }

    &.on {
      span {
        &:last-child {
          display: block;
          position: absolute;
          left: 50%;
          bottom: -9px;
          width: 16px;
          height: 3px;
          transform: translateX(-50%);
          background: #fff;
          border-radius: 1px;
        }
      }
    }
  }
}
.tag-box {
  display: flex;

  div {
    height: 20px;
    border-radius: 4px 4px 4px 4px;
    color: #fff;
    padding: 4px 4px 3px;
    font-family: Source Han Sans, Source Han Sans;
    font-weight: 500;
    font-size: 12px;
    line-height: 12px;
    text-align: left;
  }
  .jx {
    background: #fa9d3b;
  }
  .rm {
    background: #1485ee;
    margin-left: 4px;
  }
}
.subtitle {
  height: 14px;
  font-family: Source Han Sans, Source Han Sans;
  font-weight: 350;
  font-size: 12px;
  color: #999999;
  line-height: 20px;
  margin-top: 3px;
}
.product-grid {
  padding: 10px;
  margin-top: 5px;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  // display: flex;
  // flex-wrap: wrap;

  .product-card {
    border: 1px solid #eaeaea;
    border-radius: 8px;
    overflow: hidden;
    background-color: white;
    flex: 0 0 calc(50% - 5px);

    .product-image {
      width: 100%;

      border-radius: 8px 8px 8px 8px;
      object-fit: cover;
      height: auto;
    }

    .product-details {
      padding: 8px;
    }

    .store-name {
      font-family: Source Han Sans, Source Han Sans;
      font-weight: 500;
      font-size: 12px;
      color: #999999;
      margin-bottom: 4px;
    }

    .product-title {
      font-family: Source Han Sans, Source Han Sans;
      font-weight: 500;
      font-size: 14px;
      color: #333333;
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
        color: #fa5151;
      }

      .original-price {
        font-family: Source Han Sans, Source Han Sans;
        font-weight: 500;
        font-size: 12px;
        color: #666666;
        margin-left: 4px;
      }
    }
  }
}

.product-icon {
  width: 24px;
  height: 24px;
}
</style>
