<template>
    <view class="screen-picker-modal" v-if="showCountryPicker">
        <view class="mask" @tap="cancel"></view>
        <view class="country-selector">
          <view class="head">
             <view class="cancel" @tap="cancel">取消</view>
             <view class="confirm" @tap="confirm">确认</view>
          </view> 
          <!-- 搜索框 -->
          <view class="search-box">
            <img class="input-icon" :src="icon_ss" alt="Record" />
            <input v-model="searchKeyword" placeholder="搜索国家" />
            <img class="input-icon" v-if="searchKeyword.length>0" :src="del" alt="Record" @tap="clearSearch" />
          </view>
      
          <!-- 国家列表 -->
          <view class="country-list">
            <view
              v-for="(country, index) in filteredCountries"
              :key="index"
              class="country-item"
              :class="{ selected: selectedId === country.id }"
              @click="selectCountry(country.id)"
            >
              {{ country.zh_name }}
              <text v-if="selectedId === country.id" class="check-icon">✓</text>
            </view>
          </view>
        </view>
    </view>
  </template>
  
  <script>
  export default {
    props: {
      showCountryPicker: {
        type: Boolean,
        default: false
      },
      countryList: {
        type: [String, Array],
        default: []
      },
    },
    data() {
      return {
        searchKeyword: "",
        selectedId: -1,
        icon_ss: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/whxt/icon_ss.png',
        del: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete1x.png',
      };
    },
    computed: {
      filteredCountries() {
        if (!this.searchKeyword) {
          return this.countryList;
        }

        return this.countryList.filter(country =>
          country.zh_name.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
          country.name.toLowerCase().includes(this.searchKeyword.toLowerCase())
        );
      }
    },
    methods: {
      selectCountry(id) {
        this.selectedId = id;
      },
      clearSearch() {
        this.searchKeyword = "";
      },
      cancel() {
        this.$emit("cancel");
      },
      confirm() {
        if (this.selectedId !== -1) {
          //遍历this.countryList里面this.selectedId的一项item
          var item = this.countryList.find(country => country.id === this.selectedId);
          this.$emit("confirm",item);
        }
      }
    }
  };
  </script>
  
  <style lang="less">
.screen-picker-modal {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 999;
  
    .mask {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
    }
  
  }
  .country-selector {
    position: relative;
    background: #fff;
    z-index: 99999;
    border-radius: 20rpx 20rpx 0 0;
    padding-bottom: 56rpx;
    .head{
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 32rpx;
        background: #F4F5F6;
        border-radius: 20rpx 20rpx 0 0;
        font-size: 36rpx;
        .cancel{
           color: #999999;
        }
        .confirm{
            color: #FA5151;
        }
    }
    .search-box {
      display: flex;
      align-items: center;
      margin:32rpx;
      padding: 16rpx 16rpx 16rpx 16rpx;
      background: #F4F5F6;
      border-radius: 54rpx;
      .input-icon{
        width: 32rpx;
        height: 32rpx;
        margin:0 16rpx;
      }
      input {
        flex: 1;
        border: none;
      }
    }
    .country-list {
        padding:0 32rpx;
        height: 500rpx;
        overflow: auto;
    
      .country-item {
        padding: 32rpx 8rpx;
        border-bottom: 1rpx solid #eee;
        &.selected {
          color: red;
        }
        .check-icon {
          float: right;
          color: red;
        }
      }
    }
    .button-group {
      display: flex;
      justify-content: space-between;
      margin-top: 20rpx;
      button {
        flex: 1;
      }
    }
  }
  </style>
