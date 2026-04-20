<template>
    <view class="bankCardDetail" :style="bankName==language.bankCarDetail.yh[0]?headBg:(bankName==language.bankCarDetail.yh[1]?headBg1:(bankName==language.bankCarDetail.yh[2]?headBg2:(bankName==language.bankCarDetail.yh[3]?headBg3:headBg4)))">
        <heads :bgColor="bgColor" :ishead_w="3" :title="language.bankCarDetail.title"></heads>
        <div class="container">
            <div class="bankInfo">
                <view class="bankInfo_img">
                    <image
                        :src="bankName==language.bankCarDetail.yh[0]?zhaoshang:(bankName==language.bankCarDetail.yh[1]?pingan:(bankName==language.bankCarDetail.yh[2]?nongye:(bankName==language.bankCarDetail.yh[3]?jianshe:moren_yinghan)))"
                        style="width: 134rpx;height: 134rpx;border-radius: 50%;"></image>
                </view>
                <div class="bankName">
                    {{bankName}}
                </div>
                <view class="branch">
                    {{branchName}}
                </view>
                <div class="cardNum">
                    {{'****'}} **** **** {{ cardNum }}
                </div>
            </div>

            <div class="btn edit" v-if="storeType=='store'"
                @click="navTo('/pagesB/myWallet/bankCardAdd?type=store&is_edit=1&id='+ id)" :style="{color: color}">
                {{ language.bankCardDetail.bj }}
            </div>
            
            <div class="btn edit" v-if="storeType!='store'"
                @click="navTo('/pagesB/myWallet/bankCardAdd?&is_edit=1&id='+ id)" :style="{color: color}">
                {{ language.bankCardDetail.bj }}
            </div>
            
            <div class="btn" @click="jiebang">
                {{ language.bankCardDetail.jcbd }}
            </div>


        </div>
        <maskM :content="language.bankCardDetail.qrjb" :display="display" @mask_value="onHande"></maskM>
        <show-toast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" @richText="_hide1(0)" @confirm="_confirm"></show-toast>

    </view>
</template>

<script>
    import maskM from "@/components/maskM";
    import showToast from '@/components/aComponents/showToast.vue'

    /**
     * 太忙了不做国际化了
     */
    export default {
        name: "bankCardDetail",
        data() {
            return {
                bankName: '',
                is_showToast:0,
                is_showToast_obj:{},
                cardNum: '',
                headBg: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/bankCardDetail.bjred.png);background-size: 100% 100%;background-repeat: no-repeat;',
                headBg1: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/pingan_bj.png);background-size: 100% 100%;background-repeat: no-repeat;',
                headBg2: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/nongye_bj.png);background-size: 100% 100%;background-repeat: no-repeat;',
                headBg3: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/jianshe_bj.png);background-size: 100% 100%;background-repeat: no-repeat;',
                headBg4: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/qita_bj.png);background-size: 100% 100%;background-repeat: no-repeat;',
                bankCardDetail_chinaicon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/bankCardDetail_chinaicon.png',
                id: '',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                branchName: '',
                color: '',
                display: false,
                storeType: '',
                zhaoshang: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zhangshang.png',
                nongye: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/nongye.png',
                pingan: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pingan.png',
                jianshe: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jianshe.png',
                moren_yinghan: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/moren_yinghan.png',
                type: 1, // 1绿色，2红色
            }
        },
        components: {
            maskM,
            showToast
        },
        computed: {
            bgColor() {
                if (this.type === 1) {
                    this.color = '#fa5151'
                    return [{
                            item: 'transparent'
                        },
                        {
                            item: 'transparent'
                        }
                    ]
                }
                this.color = '#AD4E4C'
                return [{
                        item: 'transparent'
                    },
                    {
                        item: 'transparent'
                    }
                ]
            }
        },
        onLoad(option) {
            this.bankName = option.bankName
            this.cardNum = option.cardNum
            this.id = option.id
            this.branchName = option.branchName
            this.type = Number.parseInt(option.type) || 1
            this.storeType = option.storeType
        },
        methods: {
            //确认
            _confirm(e){
                this.onHande()
            },
            //取消
            _hide1(e){
                this.is_showToast = e
            },
            jiebang() {
                this.is_showToast = 4
                this.is_showToast_obj.title = this.language.putForward.jbts
                this.is_showToast_obj.content = this.language.putForward.qrjcb
                this.is_showToast_obj.button = this.language.putForward.qx
                this.is_showToast_obj.endButton = this.language.putForward.qr
            },
            onHande(value) {
                


                    let data = {}
                    if (this.storeType === 'store') {
                        data = {
                            api:"mch.App.MchBank.DelBank",
                            id: this.id
                        }
                    } else {
                        data = {
                            api: 'app.user.del_bank',
                            id: this.id
                        }
                    }

                    this.$req.post({
                        data
                    }).then(res => {
                        if (res.code == 200) {
                            this.is_showToast = 1
                            this.is_showToast_obj.imgUrl = this.sus
                            this.is_showToast_obj.title = this.language.putForward.jbcg
                            setTimeout(() => {
                                this.navBack()
                            }, 1500)
                        } else {
                            uni.showToast({
                                title: res.message,
                                icon: 'none'
                            })
                        }
                    })
            }
        }
    }
</script>

<style lang="less" scoped> 
    @import url("@/laike.less");
    .branch {
        font-size: 28rpx;
        color: #999999;
        
    }

    .bankCardDetail {
        height: 100vh;

        .bankInfo {
            text-align: center;
            /* #ifdef H5 */
            padding-top:110rpx;
            /* #endif */
            /* #ifdef MP-WEIXIN */
            padding-top: 65rpx;
            /* #endif */
            /* #ifdef APP-PLUS */
            padding-top: 90rpx;
            /* #endif */
            position: relative;

            .bankName {
                font-size: 36rpx;
                
                font-weight: 600;
                color: #333333;
                height: 36rpx;
                margin-top: 48rpx;
                display: flex;
                justify-content: center;
                margin-bottom: 29rpx;
            }

            .cardNum {
                font-size: 48rpx;
                
                font-weight: 600;
                color: #333333;
                margin-top: 48rpx;
            }

        }

        .bankInfo_img {
            width: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .btn {
            width: 590rpx !important;
            height: 90rpx;
            background: #FFFFFF;
            border-radius: 50rpx;
            position: absolute;
            bottom: 80rpx;
            left: 80rpx;
            border: 2rpx solid #fa5151;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fa5151;
            font-size: 32rpx;

            &.edit {
                bottom: 200rpx;
                background-color: #fa5151;
                color: #ffffff !important;
            }

            &:nth-child(3) {
                color: #fa5151;
                border: 2rpx solid #fa5151;
            }
        }
    }
</style>
