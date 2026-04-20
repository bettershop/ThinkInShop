<!-- components/DatePicker.vue -->
<template>
    <view v-if="showDatePicker">
      <!-- 底部日期选择器 -->
      <view class="date-picker-modal">
        <view class="mask" @click="cancel"></view>
        <view class="picker-container">
          <view class="picker-header">
            <text class="btn cancel" @click="cancel">取消</text>
            <text class="btn confirm" @click="handleConfirm">确认</text>
          </view>
          
          <picker-view 
            class="picker-view" 
            :value="pickerValue" 
            @change="handlePickerChange"
          >
            <picker-view-column>
              <view 
                v-for="(year, index) in yearRange" 
                :key="index" 
                class="picker-item"
              >{{ year }}年</view>
            </picker-view-column>
            
            <picker-view-column>
              <view 
                v-for="month in 12" 
                :key="month" 
                class="picker-item"
              >{{ month }}月</view>
            </picker-view-column>
          </picker-view>
        </view>
      </view>
    </view>
  </template>
  
  <script>
  export default {
    props: {
      showDatePicker: {
        type: Boolean,
        default: false
      },
    },
    mounted() {
      // 生成1998到当前年份的数组
      const startYear = 1998;
      this.yearRange = Array.from(
        { length: this.selectedYear - startYear + 1 },
        (_, i) => startYear + i
      );
      // 自动滚动到当前年位置
      const currentYearIndex = this.yearRange.indexOf(this.selectedYear);
      this.pickerValue = [currentYearIndex, this.selectedMonth];
    },
    data() {
      return {
        selectedYear: new Date().getFullYear(),
        selectedMonth: new Date().getMonth(),
        yearRange:[],
        pickerValue:[]
      };
    },
    methods: {
      handlePickerChange(e) {
        this.pickerValue = e.detail.value;
      },
      handleConfirm() {
        this.selectedYear = this.yearRange[this.pickerValue[0]];
        this.selectedMonth = this.pickerValue[1];
        this.$emit('confirm', {
          year: this.selectedYear,
          month: this.selectedMonth + 1
        });
      },
      cancel() {
        this.$emit('cancel');
      }
    }
  };
  </script>
  
  <style lang="less" scoped>
  .date-picker-modal {
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
  
    .picker-container {
      position: relative;
      background: #fff;
      border-radius: 20rpx 20rpx 0 0;
      padding-top: 20rpx;
      z-index: 99999;
      .picker-header {
        display: flex;
        justify-content: space-between;
        padding: 20rpx 30rpx;
        border-bottom: 1rpx solid #eee;
  
        .btn {
          font-size: 32rpx;
          padding: 10rpx 20rpx;
        }
  
        .cancel {
          color: #999;
        }
  
        .confirm {
          color: #007AFF;
        }
      }
  
      .picker-view {
        height: 400rpx;
        width: 100%;
  
        .picker-item {
          line-height: 62rpx;
          text-align: center;
          font-size: 32rpx;
        }
      }
    }
  }
  </style>
