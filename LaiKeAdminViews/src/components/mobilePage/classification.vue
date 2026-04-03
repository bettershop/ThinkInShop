<template>
  <div class="classification-box" :style="style" :class="tabberinfo.backType == 1 ? 'backgImg':'backgColor'">
    <!-- 搜索栏 -->
    <div class="search-bar" v-if="isShow">
      <div class="search-input">
        <input class="input" type="text" :placeholder="placeholder" />
        <div class="search-button">搜索</div>
      </div>
      <img class="menu-icon" src='../../assets/images/diy/searchResult_horizontal.png' alt="" v-if="isStyleShow && styleType == 1">
      <img class="menu-icon" src='../../assets/images/diy/searchResult_vertical.png' alt="" v-if="isStyleShow && styleType == 2">
    </div>
    <div class="classification" v-if="styleType == 2">
      <!-- 左侧分类导航 -->
      <div class="left-nav">
        <div v-for="(category,index) in leftCategories" :key="category.id" :class="{ 'category-active': index == 2 }" class="category-name" :style="{
                background: index == 2 ? bgColor : '',
                color: index == 2 ? color : ''
              }">
          {{ category.name }}
        </div>
      </div>

      <!-- 右侧商品展示 -->
      <div class="right-content">
        <!-- 表头 -->
        <div class="category-header">
          <div class="title-cell" style="color:red;">综合</div>
          <div class="title-cell">销量</div>
          <div class="title-cell">价格</div>
          <div class="title-cell">筛选</div>
        </div>
        <!-- 商品列表 -->
        <div class="product-list">
          <div class="product-item" v-for="(product, index) in products" :key="index">
            <img class="product-img" src='../../assets/images/diy/Default_picture.png' alt="商品图片">
            <div class="product-info">
              <div class="product-title" style="">{{ product.title }}</div>
              <div class="price">
                <span>¥{{ product.price }}</span>
                <del>¥{{ product.originalPrice }}</del>
              </div>
              <div class="product-price">
                <div class="store-name" style="font-size: 10px;color: #999;margin-bottom: 4px;">{{ product.storeName }}</div>
                <img class="product-icon" src='../../assets/images/diy/redgwc.png' alt="">
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else>
      <!-- 第二种静态页面样式 -->
      <div class="static-layout">
        <!-- 顶部标签栏 -->
        <div class="top-tabs">
          <div v-for="(tab, index) in topCategories" :key="tab.name" class="tab-item" @click="handleTabClick(tab)">
            <img src='../../assets/images/diy/Default_picture.png' alt="图标" class="tab-icon" />
            <span class="tab-text">{{ tab.title }}</span>
          </div>
        </div>

        <!-- 商品网格布局 -->
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
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";
import draggable from 'vuedraggable';

export default {
  cname: "全部分类",
  icon: "iconfenleidaohang",
  name: "classification",
  configName: "c_classification_center",
  defaultName: "c_classification_center", // 外面匹配名称

  type: 2,
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

      },
      deep: true
    },
    num: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[nVal];
      },
      deep: true
    },
    defaultArray: {
      handler(nVal, oVal) {
        const data = this.$store.state.admin.mobildConfig.defaultArray[
          this.num
        ];
      },
      deep: true
    }
  },
  components: {
    draggable,
  },
  data() {
    return {
      activeTab: '',
      topCategories: [
        { title: '标题1' },
        { title: '标题2' },
        { title: '标题2' },
        { title: '标题1' },
        { title: '标题2' },
      ],
      leftCategories: [
        { id: 0, name: '全部分类' },
        { id: 1, name: '家用电器' },
        { id: 2, name: '美妆护肤' },
        { id: 3, name: '日用百货' },
        { id: 4, name: '精致鞋靴' },
        { id: 5, name: '家居布艺' },
        { id: 6, name: '手机数码' },
        { id: 7, name: '时尚潮牌' },
        { id: 8, name: '电脑配件' },
        { id: 9, name: '国际大牌' },
      ],
      products: [
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 ',
          price: 219,
          originalPrice: 300.99
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 ',
          price: 219,
          originalPrice: 300.99
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 ',
          price: 219,
          originalPrice: 300.99
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 ',
          price: 219,
          originalPrice: 300.99
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 ',
          price: 219,
          originalPrice: 300.99
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 ',
          price: 219,
          originalPrice: 300.99
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 ',
          price: 219,
          originalPrice: 300.99
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 ',
          price: 219,
          originalPrice: 300.99
        },
        {
          storeName: '平台自营店',
          title: '休闲短袖T恤 ',
          price: 219,
          originalPrice: 300.99
        },

      ],
      defaultConfig: {
        configName: "c_classification_center",
        defaultName: "c_classification_center", // 外面匹配名称
        name: "classification",
        pointName: 'classification',
        cname: "全部分类", // 模板名称"
        icon: "icontupianguanggao",
        timestamp: this.num,
        // 模板选择
        c_classification_info: {
          type: 'img', // 纯色 渐变 图片
          styleType: 2, // 样式布局
          placeholder: '请输入标题',
          isShow: true,
          isStyleShow: true,
          bgColor: [
            {
              item: '#ffffff'
            }
          ],
          color: [
            {
              item: '#FA5151'
            }
          ],
          pureColor: [
            {
              item: '#ffffff'
            }
          ],
          graduatedColor: [
            {
              item: '#ffffff'
            },
            {
              item: '#ffffff'
            }
          ],
          bgImg: '',
          bgNum: 0
        },
      },
      styleType: 2,
      type: 'pureColor',
      isShow: true,
      isStyleShow: true,
      placeholder: '请输入标题',
      bgColor: '',
      color: '',
      pureColor: [
        {
          item: '#ffffff'
        }
      ],
      graduatedColor: [
        {
          item: '#ffffff'
        },
        {
          item: '#ffffff'
        }
      ],
      bgImg: '',
      bgNum: 0,
      style: ""
    };
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
        console.log(222222222, this.$store.state.admin.mobildConfig.defaultArray, this.num)
        this.setConfig(data);
      },
      deep: true,
      immediate: true
    }
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
    handleTabClick(tab) {
      console.log('Selected tab:', tab.name);
    },
    setConfig(data) {
      console.log('datadatadatadatadata', data)
      if (data) {
        let info = data.c_classification_info
        this.styleType = info.styleType;
        this.type = info.type;
        this.isShow = info.isShow;
        this.isStyleShow = info.isStyleShow;
        this.placeholder = info.placeholder;
        this.bgColor = info.bgColor[0].item;
        this.color = info.color[0].item;
        this.pureColor = info.pureColor;
        this.graduatedColor = info.graduatedColor;
        this.bgNum = info.bgNum;
        this.bgImg = info.bgImg || '/static/img/grbj.791807ed.png';
        // if (this.type == "graduatedColor") {
        //   this.style = `background: linear-gradient(${this.bgNum}deg,${this.graduatedColor[0].item},${this.graduatedColor[1].item});`;
        // }
        // if (this.type == "pureColor") {
        //   this.style = `background-color:${this.pureColor[0].item};`;
        // }
        // if (this.type == "img") {
        //   this.style = `background-image:url(${this.bgImg}); background-size: cover;`;
        // }
      }
    }
  }
};
</script>

<style scoped lang="less">
.classification-box { 
  background-color:var(--colorTwo)
  // background-image:url('../../assets/images/diy/grbj.png');
  //  background-size: cover;
}
.classification {
  display: flex;
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
.category-header {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  padding-right: 12px;
  font-size: 10px;
  box-sizing: border-box;
  .title-cell {
    text-align: center;
    font-size: 14px;
    color: #999999;
  }
}

.search-bar {
  display: flex;
  padding: 10px;
  border-bottom: 1px solid #eaeaea;
  .search-input {
    width: 100%;
    background-color: #ffffff;
    border-radius: 16px;
    padding: 2px;
    padding-left: 12px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: space-between;
    .input {
      border: none;
    }
  }
  .search-button {
    align-items: center;
    justify-content: center;
    padding: 4px 12px;
    box-sizing: border-box;
    background-color: #fa5151;
    font-weight: 700;
    font-size: 14px;
    color: #ffffff;
    border-radius: 14px;
  }
  .menu-icon {
    width: 36px;
    height: 36px;
  }
}

.left-nav {
  width: 90px;
  background-color: transparent;
  .category-name {
    padding: 8px 10px; 
    color: #020202;
    font-weight: 400;
    font-size: 14px; 
  }
  .category-active {
    width: 80px;
    background-color: #fff;
    font-weight: bold;
    position: relative;
    border-bottom-right-radius: 6px;
    border-top-right-radius: 6px;

    :after {
      content: '';
      position: absolute;
      left: 0;
      width: 4px;
      height: 22px;
      background: #fa5151;
      border-radius: 0 12px 12px 0;
    }
  }
}

.right-content {
  flex: 1;
  padding: 10px;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));

  background-color: #fff;
  gap: 10px;
  border-radius: 12px 12px 0 0;
  padding: 12px;

  .product-item {
    display: flex;
    align-items: start;
    .product-img {
      width: 60px;
      height: 60px;
      border-radius: 8px;
    }
    .product-info {
      margin-left: 10px;
      .store-name {
        font-size: 12px;
        color: #666;
      }
      .product-title {
        font-size: 10px;
        margin: 5px 0;
        width: 100px;
      }
      .price {
          span {
            font-family: DIN;
            font-size: 14px;
            color: #fa5151;
            margin-left: 1px;
          }
          del {
            font-size: 10px;
            color: #999;
            line-height: 16px;
            -webkit-text-decoration-line: line-through;
            text-decoration-line: line-through;
            margin-left: 4px;
          }
        }
      .product-price {
        display: flex;
        align-items: center;
        justify-content: space-between;
       
      }
      .product-icon {
        width: 18px;
        height: 18px;
      }
      .add-cart {
        margin-top: 5px;
        width: 80px;
        height: 30px;
        background-color: #ff6700;
        color: white;
        border: none;
        border-radius: 4px;
      }
    }
  }
}

// 新增第二种静态页面样式
.static-layout {
  width: 100%;
  padding: 10px;
  box-sizing: border-box;
  .top-tabs {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    padding: 8px 0;
    border-radius: 8px;
    .tab-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 4px 8px;
      cursor: pointer;
      transition: background-color 0.3s ease;
      &:hover {
        background-color: #eaeaea;
      }
      .tab-icon {
        width: 30px;
        height: 30px;
        margin-bottom: 4px;
      }
      .tab-text {
        font-size: 12px;
        color: #666;
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
