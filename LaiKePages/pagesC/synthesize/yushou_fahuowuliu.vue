<template>
    <view>
    <view class="page flex-col">
        <heads :title="language.afterSale.writeLogistics.title" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <view class="block_3 flex-col">
            <view class="box_2 flex-row justify-between">
                <text class="text_12">{{language.afterSale.writeLogistics.kdgs}}</text>
                <view class="dropdown_1 flex-row justify-between"  @tap="showSinglePicker">
                    <input  class="text_11" type="text" disabled="true" :placeholder="language.afterSale.select_express" v-model="express_name" />
                    <image class="image_4" referrerpolicy="no-referrer"
                        src="https://lanhu-dds-backend.oss-cn-beijing.aliyuncs.com/merge_image/imgs/972691f878c34a59b0ee7f12674fadbc_mergeImage.png" />
                </view>
            </view>
            <view class="box_3 flex-row justify-between" v-if="!logistics_type">
                <text class="text_12">{{language.afterSale.writeLogistics.kddh}}</text>
                <view class="input_1 flex-col">
                    <input class="text_11" type="text"  :placeholder="language.afterSale.courierNumner_placeholder" v-model="express_number"  @input="numberFixedDigit" maxlength="30"/>
                </view>
            </view>
        </view>
        <view class="box_6 flex-col">
            <button class="button_2 flex-col" @click="_but">
                <text class="text_21">{{language.afterSale.writeLogistics.bc}}</text>
            </button>
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
    <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
        <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
            <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
            </view>
            <view class="xieyi_title"
                style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                发货成功
            </view>
        </view>
    </view>
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
                express_name: '',
                express_number: '',
                show: false,
                themeColor: '#D73B48',
                mode: '',
                deepLength: 1,
                pickerValueDefault: [0],
                is_sus:false,
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                pickerValueArray: [],
                wuliulist: [],
                orderDetailIds: '',
                sum:0,
                exId: '',
                is_integral:false,
                logistics_type: false, //电子面单
                sNo: '',//订单号
            };
        },
        components: {
            mpvuePicker
        },
        onLoad(option) {
            if(option.orderDetailIds){
                this.orderDetailIds = option.orderDetailIds
            }
            if(option.sum){
                this.sum = option.sum
            }
            if(option.is_integral){
                this.is_integral = option.is_integral
            }
            if(option.sNo){
                this.sNo = option.sNo
            }
            this._axios()
        },
        methods: {
            /**
             * 物流信息
             * 
             */
            _axios(){
                let data = {
                    //api: 'plugin.integral.AppIntegral.searchExpress',
                    api:'mch.App.Mch.GetLogistics',
                    sNo: this.sNo,
                }
                this.$req.post({data}).then(res=>{
                    if(res.code == 200){
                        let wuliu = res.data.list
                        this.wuliulist = res.data.list
                        wuliu.forEach((item,index)=>{
                            let nowuliu = this.pickerValueArray.every((it,inx)=>{
                                return it != item.kuaidi_name
                            })
                            if(nowuliu){
                                this.pickerValueArray.push(item.kuaidi_name)
                            }
                        })
                    } else {
                    }
                })
            },
            /**
             *物流选择弹窗
             * 
             */
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
             * 确认选择物流
             * 
             */
            onConfirm(e) {
                this.express_name = e.label;
                this.show = false;
                this.wuliulist.forEach((item,index)=>{
                    if(item.kuaidi_name == this.express_name){
                        this.exId = item.id
                    }
                })
                this.logistics_type = this.wuliulist[e.index[0]].logistics_type
            },
            /**
             * 提交
             * 
             */
            _but() {
				let expressid = this.exId
				let courierNum = !this.logistics_type ? this.express_number : ''
				let type = this.logistics_type ? '2':'1'
				let psyInfo = ''
				let orderList = [
					{
						detailId: this.orderDetailIds,
						num: this.sum,
					},
				]
				let list = {
					expressid, //物流公司id
					courierNum, //物流号
					type,	//发货类型 1普通发货 2电子面单 3商家配送
					psyInfo, //配送员信息
					orderList, //订单信息 detailId订单详情id num订单商品数量
				}
				//订单发货
				this.LaiKeTuiCommon.orderSend(this, list).then((res)=>{
					if(res.code == 200){
						this.is_sus = true
						setTimeout(function() {
							this.is_sus = false
						}, 1500)
						setTimeout(()=>{
							if(this.is_integral){
								uni.redirectTo({
									url:'/pagesC/redeemOrder/Redeem_order'
								})
							}else{
								uni.navigateBack({delta:1})
							}
						},1000)
					} else {
						uni.showToast({
							title: res.message,
							icon: 'none'
						})
					}
				})
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
      border: 1px solid transparent;
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


    .page {
        position: relative;
        width: 750rpx;
        height: 100%;
        // height: 1624rpx;
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
            margin-bottom: 24rpx;
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
                    // line-height: 44rpx;
                    // margin-top: 16rpx;
                }

                .dropdown_1 {
                    background-color: rgba(255, 255, 255, 1);
                    border-radius: 8px;
                    width: 498rpx;
                    border: 0.5px solid rgba(0, 0, 0, 0.1);
                    padding: 16rpx;

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
</style>
