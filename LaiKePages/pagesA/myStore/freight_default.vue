<template>
    <view class="container">
        <view class="position_head">
            <heads :title="title" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        </view>
        <view class="content">
            <view class="header_info">
                <ul class="enterprise">
                    <li>
                        <p class="header_info_left">
                            {{ typeIndex == 1 ? language.freight_default.spzl : language.freight_default.spsl }}
                        </p>
                        <view class="header_info_right">
                            <input @input="input1" placeholder-style="font-weight: 400;" type="text" v-if="type != 'see'" :placeholder="typeIndex == 1 ? language.freight_default.qsrspzl : language.freight_default.qsrspsl" v-model="inputInfo.num1" />
                            <input placeholder-style="font-weight: 400;" type="text" v-else :placeholder="language.freight_default.qsrspsl " v-model="inputInfo.num1" disabled/>
                            <p>{{ typeIndex == 1 ? language.freight_default.qk : language.freight_default.jian }}</p>
                        </view>
                    </li>
                    <li>
                        <p class="header_info_left">
                            {{language.freight_default.sfje}}
                        </p>
                        <view class="header_info_right priceInput">
                            <input 
                                 @keyup.native="inputInfo.num2 = input3(inputInfo.num2,2)"
                                placeholder-style="font-weight: 400;" type="text" v-if="type != 'see'" :placeholder="language.freight_default.qsrsfje" v-model="inputInfo.num2">
                            <input placeholder-style="font-weight: 400;" type="text" v-else :placeholder="language.freight_default.qsrsfje " v-model="inputInfo.num2" disabled/>
                            <p>{{language.freight_default.yuan}}</p>
                        </view>
                    </li>
                    <li>
                        <p class="header_info_left">
                            {{language.freight_default.mcxz}}
                        </p>
                        <view class="header_info_right">
                            <input @input="input2" placeholder-style="font-weight: 400;" type="text" v-if="type != 'see'" :placeholder="typeIndex == 1 ? language.freight_default.qsrmcqk : language.freight_default.qsrmcjs" v-model="inputInfo.num3" />
                            <input placeholder-style="font-weight: 400;" type="text" v-else :placeholder="typeIndex == 1 ? language.freight_default.qsrmcqk  : language.freight_default.qsrmcjs " v-model="inputInfo.num3" disabled/>
                            <p>{{ typeIndex == 1 ? language.freight_default.qk  : language.freight_default.jian  }}</p>
                        </view>
                    </li>
                    <li>
                        <p class="header_info_left">
                            {{language.freight_default.ewsf}}
                        </p>
                        <view class="header_info_right priceInput">
                            <input @keyup.native="inputInfo.num4 = input3(inputInfo.num4,2)" placeholder-style="font-weight: 400;" type="number" v-if="type != 'see'" :placeholder="language.freight_default.qsrewsf" v-model="inputInfo.num4" />
                            <input placeholder-style="font-weight: 400;" type="number" v-else :placeholder="language.freight_default.qsrewsf " v-model="inputInfo.num4" disabled/>
                            <p>{{language.freight_default.yuan }}</p>
                        </view>
                    </li>
                    <li class="promt">
                        <p>{{language.freight_default.zygmd}}{{  typeIndex == 1 ? language.freight_default.zl : language.freight_default.sl }}<={{language.freight_default.szzst}}</p>
                    </li>
                </ul>
            </view>
            
        </view>
        <view class="btn" v-if="type != 'see'">
            <p @tap="submit">{{language.freight_sheng.save}}</p>
        </view>
    </view>
</template>

<script>
    export default {
        data() {
            return {
                title: '',
                select_header: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/select_header.png',
                no_select_header: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/no_select_header.png',
                selectType: 1,
                type:'',//是否可以设置
                inputInfo: {
                    num1: '',
                    num2: '',
                    num3: '',
                    num4: '',
                },
                
                bgColor: [
                    {
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                
                typeIndex: 0
            }
        },
        
        onLoad(option) {
            if(option.defaultFreight) {
                this.inputInfo = JSON.parse(option.defaultFreight)
            }
            if(option&&option.type){
                this.type=option.type
            }
            if(option.id) {
                this.id = option.id
            }
            if(option.typeIndex) {
                this.typeIndex = option.typeIndex
            }
        },
        
        onShow() {
            this.title=this.language.freight_default.mryfsz
        },
        
        methods: {
            input1(e) {
                let value = e.detail.value
                const inputRule = /[^\d]/g
                this.$nextTick(() => {
                    this.inputInfo.num1 = value.replace(inputRule, '');
                    if (this.inputInfo.num1 == '') {
                        return
                    } else {
                        //执行对应代码
                    }
                })
            },
            input2(e) {
                let value = e.detail.value
                const inputRule = /[^\d]/g
                this.$nextTick(() => {
                    this.inputInfo.num3 = value.replace(inputRule, '');
                    if (this.inputInfo.num3 == '') {
                        return
                    } else {
                        //执行对应代码
                    }
                })
            },
            input3(num, limit) {
                  var str = num
                  var len1 = str.substr(0, 1)
                  var len2 = str.substr(1, 1)
                  //如果第一位是0，第二位不是点，就用数字把点替换掉
                  if (str.length > 1 && len1 == 0 && len2 != ".") {
                    str = str.substr(1, 1)
                  }
                  //第一位不能是.
                  if (len1 == ".") {
                    str = ""
                  }
                  //限制只能输入一个小数点
                  if (str.indexOf(".") != -1) {
                    var str_ = str.substr(str.indexOf(".") + 1)
                    if (str_.indexOf(".") != -1) {
                      str = str.substr(0, str.indexOf(".") + str_.indexOf(".") + 1)
                    }
                  }
                  //正则替换
                  str = str.replace(/[^\d^\.]+/g, '') // 保留数字和小数点
                  if (limit / 1 === 1) {
                    str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/,'$1') // 小数点后只能输 1 位
                  } else {
                    str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/,'$1') // 小数点后只能输 2 位
                  }
                    return str
            },
            numberFixedDigit (e) { // 只能输入整数
              e.target.value = e.target.value.replace(/^0|[^\d]|[.]/g, '')
              this.$nextTick(() => {
                this.bankNumber= e.target.value
              })
            },
            numberFixedDigit2 (e) { // 只能输入整数
              e.target.value = e.target.value.replace(/^0|[^\d]|[.]/g, '')
              this.$nextTick(() => {
                this.registerPhone= e.target.value
              })
            },
            numberFixedDigit3 (e) {
              e.target.value = e.target.value.replace(/[^\w]/g, '')
              this.$nextTick(() => {
                this.companyTaxNumber = e.target.value
              })
            },
            
            changeType(value) {
                this.selectType = value
            },
            submit() {
                if(!this.inputInfo.num1) {
                    uni.showToast({
                        title: this.typeIndex == 1 ? this.language.freight_default.qsrspzl : this.language.freight_default.qsrspsl,
                        duration: 1500,
                        icon: 'none'
                    });
                    return
                }
                if(!this.inputInfo.num2) {
                    uni.showToast({
                        title: this.language.freight_default.qsrsfje,
                        duration: 1500,
                        icon: 'none'
                    });
                    return
                }
                if(!this.inputInfo.num3) {
                    uni.showToast({
                        title: this.typeIndex == 1 ? this.language.freight_default.qsrmcqk : this.language.freight_default.qsrmcjs,
                        duration: 1500,
                        icon: 'none'
                    });
                    return
                }
                if(!this.inputInfo.num4) {
                    uni.showToast({
                        title: this.language.freight_default.qsrewsf,
                        duration: 1500,
                        icon: 'none'
                    });
                    return
                }
                uni.setStorageSync('defaultFreight',this.inputInfo)
                uni.navigateBack({
                    delta: 1
                })
            }
        },
    }
</script>
<style>
   page {
       background-color: #f4f5f6;
   }
</style>
<style lang="less" scoped> 
    @import url("@/laike.less");
    .container {
        min-height: 100vh;
        background-color: #F4F5F6;
        .content {
            width: 100%;
            padding: 32rpx;
            border-bottom-left-radius: 24rpx;
            border-bottom-right-radius:24rpx;
            box-sizing: border-box;
            background-color: #ffffff;
            .header_type {
                width: 100%;
                display: flex;
                align-items: center;
                padding: 32rpx 0 8rpx 0;
                .header_type_left {
                    width: 160rpx;
                    font-size: 32rpx;
                    color: #333333;
                    text-align: right;
                }
                .header_type_right {
                    flex: 1;
                    margin-left: 32rpx;
                    display: flex;
                    align-items: center;
                    p {
                        display: flex;
                        align-items: center;
                        &:first-child {
                            margin-right: 64rpx;
                        }
                        span {
                            font-size: 32rpx;
                            color: #333333;
                            margin-right: 16rpx;
                        }
                        image {
                            width: 32rpx;
                            height: 32rpx;
                        }
                    }
                }
            }
            
            .header_info {
                width: 100%;
                background-color: #ffffff;
                .enterprise,.personal {
                    display: flex;
                    flex-direction: column;
                    li {
                        display: flex;
                        align-items: center;
                        &:not(:first-child) {
                            margin-top: 24rpx;
                        }
                       .header_info_left {
                           width: 160rpx;
                           font-size: 28rpx;
                           color: #333333;
                           display: flex;
                           align-items: center;
                           justify-content: flex-end;
                           place-content: start;
                           font-weight: 400 !important;
                           span {
                               color: #FA5151;
                               margin-right: 8rpx;
                           }
                       }
                       .priceInput{
                           font-weight: 500;
                           color: #FA5151;
                       }
                       .header_info_right {
                           flex: 1;
                           display: flex;
                           align-items: center;
                           justify-content: flex-end;
                           position: relative;
                           input {
                               width: 100%;
                               height: 76rpx;
                               border-radius: 16rpx;
                               border: 1rpx solid rgba(0,0,0,0.1);
                               padding-left: 20rpx;
                               box-sizing: border-box;
                           }
                           p {
                               position: absolute;
                               font-size: 14px;
                               color: #333333;
                               right: 8px;
                               top: 50%;
                               font-weight: 400 !important;
                               transform: translateY(-50%);
                           }
                           
                           .uni-input-placeholder {
                               font-size: 28rpx;
                           }
                       }
                    }
                    .promt {
                        padding: 16rpx;
                        background: #F4F5F6;
                        border-radius: 16rpx;
                        font-size: 28rpx;
                        
                        font-weight: 400;
                        color: #666666;
                    }
                    
                }
            }
        }
        .btn {
            width: 100%;
            padding: 16rpx 32rpx;
            
            transform: translate(0) !important;//h5适配web页面设置单独的样式
            margin-top: 104rpx;
            box-sizing: border-box;
            border-bottom-left-radius: 24rpx;
            border-bottom-right-radius: 24rpx;
            padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
            padding-bottom: env(safe-area-inset-bottom);
            p {
                width: 100%;
                height: 92rpx;
                background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
                border-radius: 52rpx;
                display: flex;
                justify-content: center;
                align-items: center;
                color: #ffffff;
            }
        }
    }
    
</style>
