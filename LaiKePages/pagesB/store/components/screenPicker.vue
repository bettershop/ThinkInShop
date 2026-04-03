<template>
    <view v-if="showScreenPicker">
      <view class="screen-picker-modal">
        <view class="mask" @tap="cancel"></view>
        <view class="content">
          <view class="picker-header">
            <image class="close"  :src="close2x " @tap="cancel"></image>
            筛选
          </view>
          <view class="screen-text">评论</view>
          <view class="type-options">
            <view class="option" :class="{ active: sort == 'asc' }" @tap="setSort('asc')">升序</view>
            <view class="option" :class="{ active: sort == 'desc' }" @tap="setSort('desc')">降序</view>
          </view>
          <view class="screen-text">价格区间(元)</view>
          <view class="pricerBox">
		   	<input v-model="min_price" type="number" :placeholder="language.search2.searchRes.min" placeholder-style="display: flex; justify-content: center;color: #B8B8B8;">
		   	<span>—</span>
		   	<input v-model="max_price" type="number" :placeholder="language.search2.searchRes.max" placeholder-style="display: flex; justify-content: center;color: #B8B8B8;"/>
		  </view>
          <view class="screen-text">品牌</view>
          <view class="type-options">
            <view 
              class="option" 
              :class="{ active: brand_id === option.brand_id }" 
              v-for="option in brand_list" 
              :key="option.value" 
              @tap="selectType(option)"
            >
              {{ option.brand_name }}
            </view>
          </view>
          <view class="buttons">
            <button @tap="reset">重置</button>
            <button @tap="confirm">确定</button>
          </view>
        </view>
      </view>
    </view>
  </template>
  
  <script>
  export default {
    props: {
      showScreenPicker: {
        type: Boolean,
        default: false
      },
      brand_list: {
        type: [String, Array],
        default: []
      },
    },
    data() {
      return {
        brand_id: '',
        sort: '',
        max_price: '',
        min_price: '',
        close2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/close2x.png',
      };
    },

    methods: {
      selectType(item) {
        this.brand_id = item?.brand_id
      },
      reset() {
        this.brand_id = '';
        this.sort = '';
        this.min_price = '';
        this.max_price = '';
        this.$emit('reset');
      },
      confirm() {
        this.$emit('confirm', {
            brand_id: this.brand_id,
            sort: this.sort,
            min_price: this.min_price,
            max_price: this.max_price
        });
      },
      cancel() {
        this.$emit('cancel');
      },
      setSort(type) {
        this.sort = type;
      }
    }
  };
  </script>
  
  <style lang="less" scoped>
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
  
    .content {
      position: relative;
      background: #fff;
      border-radius: 20rpx 20rpx 0 0;
      padding:0 32rpx 50rpx 32rpx;
      z-index: 99999;
      .picker-header{
        padding: 52rpx 0;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 36rpx;
        position: relative;
      }
      .close {
        position: absolute;
        right:0rpx;
        top: 50%;
        transform: translateY(-50%);
        cursor: pointer;
        width: 40rpx;
        height: 40rpx;
      }
      .screen-text {
        font-size: 30rpx;
        color: #333333;
        margin-bottom: 28rpx;
      }
      .pricerBox{
	    display: flex;
	    align-items: center;
        margin-bottom: 34rpx;
	    span{
	        margin: 0 16rpx;
	        font-size: 32rpx;
	        color: #333333;
	    }
	    input{
	        text-align: center;
	        font-size: 28rpx;
	        width: 312rpx;
	        height: 72rpx;
	        // background: #F4F4F4;
	        border-radius: 38rpx;
	        color: #020202;
	        border: 2rpx solid rgba(0,0,0,0.1);
	    }
	  }
      .type-options {
        display: flex;
        flex-wrap: wrap;
        margin-bottom: 34rpx;
        max-height: 260rpx;
        overflow-y: auto;
        .option {
          width: 214rpx;
          height: 60rpx;
          border-radius: 30rpx;
          background: #F6F5F8;
          color: #666666;
          font-size: 24rpx;
          display: flex;
          cursor: pointer;
          justify-content: center;
          align-items: center;
          box-sizing: border-box;
          margin-right: 22rpx;
          margin-bottom: 24rpx;
  
          &.active {
            background: rgba(250,81,81,0.1);
            color: #FA5151;
            border-color: #ff6b6b;
          }
        }
        .option:nth-child(3n) {
            margin-right:0rpx;
        }

      }
  
      .buttons {
        display: flex;
        justify-content: flex-end;
        padding-top: 14rpx;
        border-top: 1rpx solid #eee;
        button {
          padding: 10px 20px;
          border: none;
          border-radius: 5px;
          cursor: pointer;
  
          &:first-child {
            width: 220rpx;
            height: 92rpx;
            border-radius: 52rpx;
            background: #fff;
            border: 1px solid #ddd;
            color: #333;
            margin-right: 10px;
            display: flex;
            justify-content: center;
            align-items: center;
          }
  
          &:last-child {
            color: #fff;
            width: 440rpx;
            height: 92rpx;
            background: linear-gradient( 270deg, #FF6F6F 0%, #FA5151 100%);
            border-radius: 52rpx;
            display: flex;
            justify-content: center;
            align-items: center;
          }
        }
      }
    }
  }
  </style>
