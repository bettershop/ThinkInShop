<template>
    <view class="page flex-col">
        <heads :title="language.afterSale.writeLogistics.title" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <view class="block_1 flex-col">
            
            
            
        </view>
        <view class="block_2 flex-col">
            <view class="text-wrapper_5 flex-row">
                <text class="text_3">{{language.afterSale.writeLogistics.thdz}}</text>
            </view>
            <view class="text-wrapper text-wrapper_6 flex-row justify-start">
                <text class="text_4">{{language.afterSale.writeLogistics.lxdz}}：</text>
                <text class="text_5">{{ address.address}}</text>
            </view>
            <view class="text-wrapper text-wrapper_7 flex-row justify-start">
                <text class="text_6">{{language.afterSale.writeLogistics.lxr}}：</text>
                <text class="text_7">{{ address.name }}</text>
            </view>
            <view class="text-wrapper text-wrapper_8 flex-row justify-start">
                <text class="text_8">{{language.afterSale.writeLogistics.lxdh}}：</text>
                <text class="text_9">{{ address.phone }}</text>
            </view>
        </view>
        <view class="block_3 flex-col">
            <view class="box_2 flex-row justify-between">
                <text class="text_12">{{language.afterSale.writeLogistics.kdgs}}</text>
                <view class="dropdown_1 flex-row justify-between"  >
                    <cover-view @tap="showSinglePicker" class="input-cover"></cover-view>
                    <input  class="text_11" type="text" disabled="true" :placeholder="language.afterSale.select_express" v-model="express_name" />
                    <image class="image_4" referrerpolicy="no-referrer"
                        src="https://lanhu-dds-backend.oss-cn-beijing.aliyuncs.com/merge_image/imgs/972691f878c34a59b0ee7f12674fadbc_mergeImage.png" />
                </view>
            </view>
            <view class="box_3 flex-row justify-between">
                <text class="text_12">{{language.afterSale.writeLogistics.kddh}}</text>
                <view class="input_1 flex-col">
                    <input class="text_11" type="text"  :placeholder="language.afterSale.courierNumner_placeholder" v-model="express_number"   @input="numberFixedDigit" maxlength="30"/>
                </view>
            </view>
            <view class="box_3 flex-row justify-between">
                <text class="text_12">{{language.afterSale.writeLogistics.lxr}}</text>
                <view class="input_1 flex-col">
                    
                    <input class="text_11" type="text"  :placeholder="language.afterSale.contact_placeholder" v-model="contacts" />
                </view>
            </view>
            <view class="box_3 flex-row justify-between">
                <text class="text_12">{{language.afterSale.writeLogistics.lxdh}}</text>
                <view class="input_1 flex-col">
                    
                    <input class="text_11" type="number" :placeholder="language.afterSale.phone_placeholder" v-model="contacts_phone" />
                </view>
            </view>
        </view>
        <view class="box_6 flex-col">
            <button class="button_2 flex-col" @click="_but">
                <text class="text_21">{{language.afterSale.writeLogistics.bc}}</text>
            </button>
        </view>
        <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    提交成功
                </view>
            </view>
        </view>
        <mpvue-picker
            v-if="show"
            :themeColor="themeColor"
            ref="mpvuePicker"
            :mode="mode"
            :deepLength="deepLength"
            :pickerValueDefault="pickerValueDefault"
            @onConfirm="onConfirm"
            :pickerValueArray="pickerValueArray"
        ></mpvue-picker>
    </view>
</template>
<script>
    import mpvuePicker from '../../components/mpvuePicker.vue';
    export default {
        data() {
            return {
                bgColor: [
                    {
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                constants: {},
                fastTap: true,
                pid:0,
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                oid: '', //订单详情id
                is_sus: false, //提示
                address: '',
                express_name: '',
                express_number: '',
                contacts: '',
                contacts_phone: '',
                
                show: false,
                themeColor: '#D73B48',
                 mode: '',
                  deepLength: 1,
                  pickerValueDefault: [0],
                  pickerValueArray: [],
            };
        },
        components: {
            mpvuePicker
        },
        onLoad(option) {
            if(option.pid&&option.oid){
                 this.pid=option.pid
                 this.oid=option.oid
                 this._axios()
            }
         
        },
        methods: {
            _axios(){
              var data = {
                  api: 'app.order.see_send',
                  pid:this.pid
              };
              
              this.$req
                  .post({ data })
                  .then(res => {
                      this.fastTap = true;
              
                      let { code, message, data: {address, express, name, phone} } = res;
              
                      if (code == 200) {
                          this.address = {
                              address,
                              name,
                              phone
                          };
              
                          for (var key in express) {
                              this.pickerValueArray.push(express[key].kuaidi_name);
                          }
                      } else {
                          uni.showToast({
                              title: message,
                              duration: 1500,
                              icon: 'none'
                          });
                      }
                  })
                  .catch(error => {
                      this.fastTap = true;
                  });  
            },
            /**
             *
             * */
            showSinglePicker() {
                this.show = true;
                this.mode = 'selector';
                this.deepLength = 1;
                this.pickerValueDefault = [0];
                var timing
                setTimeout(()=>{
                    timing = this.$refs.mpvuePicker
                },10)
                setTimeout(()=>{
                    timing.show()
                },100)
            },
            /**
             *
             * */
            onConfirm(e) {
                this.express_name = e.label;
                this.show = false;
            },
            /**
             * 确认
             * */
            _confirm() {
                this.logistics_f = true;
                this.logistics_name = false;
                this.express_name = this.logistics_re;
            },
            /**
             * 取消
             * */
            _cancel() {
                this.logistics_f = true;
                this.logistics_name = false;
                this.express_name = this.express_name;
            },
            /**
             * 提交
             * */
            _but() {
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;
                if(this.express_name == ''){
                    this.fastTap = true;
                    uni.showToast({
                        title: this.language.afterSale.select_express,
                        duration: 1000,
                        icon: 'none'
                    });
                } else if(this.express_number == ''){
                    this.fastTap = true;
                    uni.showToast({
                        title: this.language.afterSale.courierNumner_placeholder,
                        duration: 1000,
                        icon: 'none'
                    });
                } else if(this.contacts == ''){
                    this.fastTap = true;
                    uni.showToast({
                        title: this.language.afterSale.contact_placeholder,
                        duration: 1000,
                        icon: 'none'
                    });
                }/* else if(this.contacts_phone.length != 11){
                    this.fastTap = true;
                    uni.showToast({
                        title: this.language.afterSale.phone_allright,
                        duration: 1000,
                        icon: 'none'
                    });
                } */
                if (this.express_name && this.express_number && this.contacts && this.contacts_phone.length == 11) {
                    //发送请求
                    let data = {
                        api: 'app.order.back_send',
                        kdcode: this.express_number,
                        kdname: this.express_name,
                        lxdh: this.contacts_phone,
                        lxr: this.contacts,
                        id: this.oid,
            
                    };
            
                    this.$req.post({ data }).then(res => {
                        this.fastTap = true;
                        let { code, message } = res;
                        if (code == 200) {
                            this.is_sus = true
                            setTimeout(()=>{
                                this.is_sus = false
                            },1000)
                            setTimeout(() => {
                                this.express_number = '';
                                this.express_name = '';
                                this.contacts_phone = '';
                                this.contacts = '';
                                this.navBack()
                                // this._axios();
                            }, 1000);
                        } else {
                            uni.showToast({
                                title: message,
                                duration: 1000,
                                icon: 'none'
                            });
                        }
                    });
                }
            },
            /* 
            *只能输入整数 和 大小写字母
            */
            numberFixedDigit (e) { //
              e.target.value = e.target.value.replace(/[^\w]/g, '')
              this.$nextTick(() => {
                this.express_number= e.target.value
              })
            }
            
        },
    };
</script>
<style>
    page{
        background-color: rgba(244, 245, 246, 1);
    }
</style>
<style lang="less" scoped> 
    @import url("@/laike.less");
    body *,
    page view {
      box-sizing: border-box;
      flex-shrink: 0;
    }
    body {
      margin: 0;
    }
    button {
      margin: 0;
      padding: 0;
      outline: none;
      background-color: transparent;
    }
    
    button:active {
      opacity: 0.6;
    }
    .flex-col {
      display: flex;
      flex-direction: column;
    }
    .flex-row {
      display: flex;
      flex-direction: row;
    }
    .justify-start {
      display: flex;
      justify-content: flex-start;
    }
    .justify-center {
      display: flex;
      justify-content: center;
    }
    
    .justify-end {
      display: flex;
      justify-content: flex-end;
    }
    .justify-evenly {
      display: flex;
      justify-content: space-evenly;
    }
    .justify-around {
      display: flex;
      justify-content: space-around;
    }
    .justify-between {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .align-start {
      display: flex;
      align-items: flex-start;
    }
    .align-center {
      display: flex;
      align-items: center;
    }
    .align-end {
      display: flex;
      align-items: flex-end;
    }

    .xieyi{
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, .5);
        z-index: 99;
        display: flex;
        justify-content: center;
        align-items: center;
        >view{
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #FFFFFF;
            border-radius: 24rpx;
            .xieyi_btm{
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0,0,0,.1);
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_title{
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 32rpx;
                
                font-weight: 500;
                color: #333333;
                line-height: 44rpx;
                margin-top: 64rpx;
                margin-bottom: 32rpx;
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_text{
                width: 100%;
                max-height: 236rpx;
                overflow-y: scroll;
                padding: 0 32rpx;
                box-sizing: border-box;
            }
        }
    }
    .page {
        position: relative;
        width: 750rpx;
        overflow: hidden;

        .block_1 {
            box-shadow: 0px 0px 0px 0px rgba(0, 0, 0, 0.2);
            background-color: rgba(255, 255, 255, 1);
            padding-top: 32rpx;

            .group_5 {
                width: 642rpx;
                align-self: center;

                .text_19 {
                    overflow-wrap: break-word;
                    color: rgba(0, 0, 0, 1);
                    font-size: 34rpx;
                    font-weight: 600;
                    text-align: center;
                    white-space: nowrap;
                    line-height: 44rpx;
                }

                .thumbnail_4 {
                    width: 38rpx;
                    height: 24rpx;
                    margin: 10rpx 0 10rpx 378rpx;
                }

                .thumbnail_5 {
                    width: 34rpx;
                    height: 24rpx;
                    margin: 10rpx 0 10rpx 16rpx;
                }

                .image_3 {
                    width: 54rpx;
                    height: 26rpx;
                    margin: 8rpx 0 10rpx 14rpx;
                }
            }

            .nav-bar_2 {
                background-color: rgba(255, 255, 255, 1);
                margin-top: 14rpx;
                padding: 12rpx 18rpx 12rpx 36rpx;

                .thumbnail_6 {
                    width: 12rpx;
                    height: 28rpx;
                    margin: 18rpx 0 18rpx 0;
                }

                .text_20 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 40rpx;
                     
                    font-weight: 500;
                    text-align: center;
                    white-space: nowrap;
                    line-height: 56rpx;
                    margin: 4rpx 0 0 246rpx;
                }

                .applet-top-bar_2 {
                    width: 174rpx;
                    height: 64rpx;
                    margin-left: 104rpx;
                }
            }
        }

        .block_2 {
            background-color: rgba(255, 255, 255, 1);
            border-radius: 0px 0px 12px 12px;

            .text-wrapper_5 {
                // margin-right: 558rpx;
                padding:0 32rpx 32rpx;
                border-bottom:1rpx solid rgba(0,0,0,.1);

                .text_3 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                    font-weight: NaN;
                    text-align: left;
                    white-space: nowrap;
                    line-height: 44rpx;
                }
            }
            .text-wrapper{
                border-bottom:1rpx solid rgba(0,0,0,0.1);
                padding:32rpx 0; 
                margin:0 32rpx;
                &:nth-last-child(1){
                    border:0;
                }
            }
            .text-wrapper_6 {
                width: 686rpx;

                .text_4 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                    font-weight: NaN;
                    text-align: left;
                    white-space: nowrap;
                    line-height: 44rpx;
                    margin-right:38rpx;
                    width:160rpx;
                }

                .text_5 {
                    width: 494rpx;
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                     
                    font-weight: 500;
                    text-align: left;
                    line-height: 44rpx;
                }
            }

            .text-wrapper_7 {
                width: 686rpx;

                .text_6 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                    font-weight: NaN;
                    text-align: left;
                    white-space: nowrap;
                    line-height: 44rpx;
                    margin-right:38rpx;
                    width:160rpx;
                }

                .text_7 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                     
                    font-weight: 500;
                    text-align: left;
                    white-space: nowrap;
                    line-height: 44rpx;
                    width:160rpx;
                }
            }

            .text-wrapper_8 {
                width: 686rpx;

                .text_8 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                    font-weight: NaN;
                    text-align: left;
                    white-space: nowrap;
                    line-height: 44rpx;
                    margin-right:38rpx;
                }

                .text_9 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                     
                    font-weight: 500;
                    text-align: left;
                    white-space: nowrap;
                    line-height: 44rpx;
                }
            }
        }

        .block_3 {
            background-color: rgba(255, 255, 255, 1);
            border-radius: 12px;
            margin-top: 24rpx;
            padding: 32rpx 32rpx 32rpx 32rpx;

            .box_2 {
                width: 686rpx;

                .text_10 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                    font-weight: NaN;
                    text-align: left;
                    white-space: nowrap;
                }

                .dropdown_1 {
                    background-color: rgba(255, 255, 255, 1);
                    border-radius: 8px;
                    width: 498rpx;
                    border: 0.5px solid rgba(0, 0, 0, 0.1);
                    padding: 16rpx;
                    position: relative;
                    z-index: 0; /* 确保文字在覆盖层下方可见 */
                    .text_11 {
                        overflow-wrap: break-word;
                        color: rgba(51, 51, 51, 1);
                        font-size: 32rpx;
                        font-weight: NaN;
                        text-align: left;
                        white-space: nowrap;
                        line-height: 44rpx;
                       
                    }

                    .image_4 {
                        width: 32rpx;
                        height: 44rpx;
                    }
                }
            }

            .box_3 {
                width: 686rpx;
                margin-top: 24rpx;

                .text_12 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                    font-weight: NaN;
                    text-align: left;
                    white-space: nowrap;
                    line-height: 44rpx;
                    margin-top: 16rpx;
                }

                .input_1 {
                    border-radius: 8px;
                    border: 0.5px solid rgba(0, 0, 0, 0.1);
                    width: 498rpx;
                    padding:16rpx;
                    .text_13 {
                        overflow-wrap: break-word;
                        color: rgba(153, 153, 153, 1);
                        font-size: 32rpx;
                        font-weight: NaN;
                        text-align: left;
                        white-space: nowrap;
                        line-height: 44rpx;
                    }
                }
            }

            .box_4 {
                width: 686rpx;
                margin-top: 24rpx;

                .text_14 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                    font-weight: NaN;
                    text-align: left;
                    white-space: nowrap;
                    line-height: 44rpx;
                    margin-top: 16rpx;
                }

                .input_2 {
                    border-radius: 8px;
                    border: 0.5px solid rgba(0, 0, 0, 0.1);
                    padding: 7.1rpx 112.1rpx 7.1rpx 7.1rpx;

                    .text_15 {
                        overflow-wrap: break-word;
                        color: rgba(153, 153, 153, 1);
                        font-size: 32rpx;
                        font-weight: NaN;
                        text-align: left;
                        white-space: nowrap;
                        line-height: 44rpx;
                    }
                }
            }

            .box_5 {
                width: 686rpx;
                margin-top: 24rpx;

                .text_16 {
                    overflow-wrap: break-word;
                    color: rgba(51, 51, 51, 1);
                    font-size: 32rpx;
                    font-weight: NaN;
                    text-align: left;
                    white-space: nowrap;
                    line-height: 44rpx;
                    margin-top: 16rpx;
                }

                .input_3 {
                    border-radius: 8px;
                    border: 0.5px solid rgba(0, 0, 0, 0.1);
                    padding: 7.1rpx 112.1rpx 7.1rpx 7.1rpx;

                    .text_17 {
                        overflow-wrap: break-word;
                        color: rgba(153, 153, 153, 1);
                        font-size: 32rpx;
                        font-weight: NaN;
                        text-align: left;
                        white-space: nowrap;
                        line-height: 44rpx;
                    }
                }
            }
        }

        .box_6 {
            padding: 104rpx 32rpx 18rpx 32rpx;

            .button_2 {
                background-image: linear-gradient(270deg,
                        rgba(255, 111, 111, 1) 0,
                        rgba(250, 81, 81, 1) 100%);
                border-radius: 26px;
                padding: 22rpx 180rpx 22rpx 180rpx;

                .text_21 {
                    overflow-wrap: break-word;
                    color: rgba(255, 255, 255, 1);
                    font-size: 32rpx;
                     
                    font-weight: 500;
                    text-align: center;
                    white-space: nowrap;
                    line-height: 44rpx;
                }
            }

            .home-indicator_2 {
                width: 268rpx;
                height: 10rpx;
                align-self: center;
                margin-top: 286rpx;
            }
        }
    }
    .input-cover {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 1; /* 覆盖 input，但不遮挡内容 */
    } 
</style>
