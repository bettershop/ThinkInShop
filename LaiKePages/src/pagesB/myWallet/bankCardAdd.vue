<template>
    <view class="bankCardAdd" style="position: relative">
        <heads :bgColor='bgColor' :ishead_w="ishead_w" titleColor="#333333" :title="title"></heads>
        <ul class="message">
            
            <li>
                <span><span class="require">*</span>{{language.putForward.cardholder}}&nbsp;&nbsp;</span>
                <view style="width: 520rpx;">
                    <input class="input_box" placeholder-style="color: #b8b8b8;font-size:32rpx;"
                        style="height: 76rpx;border-radius: 16rpx;" type="text"
                        :placeholder="language.putForward.cardholder_placeholder" v-model="user_name"
                        @blur="_usname()" />
                </view>
            </li>
            <li>
                <span><span class="require">*</span>{{language.putForward.number}}</span>
                <view style="width: 520rpx;">
                    <input placeholder-style="color: #b8b8b8;" style="height: 76rpx;border-radius: 16rpx;" type="number"
                        :placeholder="language.putForward.number_placeholder" v-model="bank_number" @input="_bank()"
                        @blur="_bank_p" />
                </view>
            </li>
            <li>
                <span><span class="require">*</span>{{language.putForward.bank}}</span>
                <view style="width: 520rpx;">
                    <input placeholder-style="color: #b8b8b8;" style="height: 76rpx;border-radius: 16rpx;" type="text"
                        :placeholder="language.putForward.bank_placeholder" v-model="bank_name" @input="_onBankNameInput"
                        @blur="_bank_p" />
                </view>
            </li>
            <li>
                <span><span class="require">*</span>{{language.putForward.branch}}</span>
                <view style="width: 520rpx;">
                    <input placeholder-style="color: #b8b8b8;" style="height: 76rpx;border-radius: 16rpx;" type="text"
                        :placeholder="language.putForward.branch_placeholder" v-model="branch" />
                </view>
            </li>

            

        </ul>

        <view class="default_btn">
            <view class="default_btn_txt">{{language.bankCardAdd.swmr}}</view>
            <view class="default_btn_switch" @click='_is_default'>
                <switch checked color="#fa5151" style="transform:scale(0.7)" :checked='modify_default_flag' />
            </view>
        </view>
        <div :class="user_name.length > 0 && bank_number.length>0 && bank_name.length>0 && branch.length>0 ? 'btn':'btn'"
            @tap="submit" v-if="type!='store'">
            <view v-if="!id"
                :class="user_name.length > 0 && bank_number.length>0 && bank_name.length>0 && branch.length>0 ? 'btn_titlel':'btn_title'">
                {{ language.bankCardAdd.qrtj }}</view>
                <view v-else
                    :class="user_name.length > 0 && bank_number.length>0 && bank_name.length>0 && branch.length>0 ? 'btn_titlel':'btn_title'" >
                    {{ language.bankCardAdd.qrbj }}</view>
        </div>
        <div :class="user_name.length > 0 && bank_number.length>0 && bank_name.length>0 && branch.length>0 ? 'btn':'btn'"
            @tap="edit" v-if="type=='store'">
            <view v-if="id"
                :class="user_name.length > 0 && bank_number.length>0 && bank_name.length>0 && branch.length>0 ? 'btn_titlel':'btn_title'">
                {{ language.bankCardAdd.qrbj }}</view>
            <view v-if="!id" @click.stop="submit"
                :class="user_name.length > 0 && bank_number.length>0 && bank_name.length>0 && branch.length>0 ? 'btn_titlel':'btn_title'">
                {{ language.bankCardAdd.qrtj }}</view>
        </div>
        <!-- 提示 -->
        <showToast
            :is_showToast="is_showToast" 
            :is_showToast_obj="is_showToast_obj">
        </showToast>
    </view>
    
</template>

<script>
    import showToast from "@/components/aComponents/showToast.vue"
    export default {
        name: "bankCardAdd",
        data() {
            return {
                user_name: '',
                bank_name: '',
                branch: '',
                bank_number: '',
                type: '',
                circle_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png', //图片地址
                circle_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehei2x.png', //图片地址
                modify_default: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/modify_default.png', //图片地址
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                fastTap: false,
                modify_default_flag: false,
                is_default: 0,
                id: '',
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                ishead_w: '2',
                title: '',
                is_showToast: 0,//
                is_showToast_obj: {},//
                bankNameByApi: false,
            }
        },
        onLoad(option) {
            this.type = option.type;
            if (option.id) {
                this.id = option.id;
                this._axios();
            }
        },
        onShow(){
            this.title = this.language.bankCardAdd.tj
            if (this.id) {
                this.title = this.language.bankCardAdd.bj
            }
        },
        components:{
            showToast
        },
        methods: {
            async _axios() {
               if(this.type=='store'){
                   let data = {
                    
                    api:"mch.App.MchBank.BankPage",
                    id: this.id
                   }
                   let {
                       data: {
                           bank: [{
                               cardholder,
                               bank_name,
                               bank_card_number,
                               is_default,
                               branch
                           }]
                       }
                   } = await this.$req.post({
                       data
                   })
                   this.user_name = cardholder
                   this.bank_name = bank_name
                   this.bank_number = bank_card_number
                   this.is_default = is_default
                   this.branch = branch
                   
                   if (this.is_default == 1) {
                       this.modify_default_flag = true
                   } else {
                       this.modify_default_flag = false
                   }
               }else{
                  var data = {
                       api:'app.user.getBankDetail',
                       bankId:this.id
                   }
                    this.$req.post({
                        data
                    }).then(res => {
                        this.user_name = res.data.cardholder
                        this.bank_name = res.data.bankName
                        this.bank_number = res.data.cardNo
                        this.branch = res.data.branchName
                        this.is_default = res.data.isDefault
                        
                        if (this.is_default == 1) {
                            this.modify_default_flag = true
                        } else {
                            this.modify_default_flag = false
                        }
                    })
                    }
            },
            // 默认卡
            _is_default() {
                if (this.is_default === 1) {
                    this.is_default = 0
                } else {
                    this.is_default = 1
                }
            },
            //持卡人验证
            _usname() {
                
            },
            // 银行名称手动输入后，不再覆盖为自动识别结果
            _onBankNameInput() {
                this.bankNameByApi = false;
            },
            handleBankNameResult(res) {
                if (res.code == 200 && res.data && res.data.Bank_name) {
                    if (!this.bank_name || this.bankNameByApi) {
                        this.bank_name = res.data.Bank_name;
                        this.bankNameByApi = true;
                    }
                    return;
                }

                if (this.bankNameByApi) {
                    this.bank_name = '';
                    this.bankNameByApi = false;
                }
            },
            // 银行卡号输入完毕
            _bank_p() {
                if (!this.bank_number) {
                    return;
                }
                var data = {
                    api: 'app.user.Verification',
                    Bank_name: this.bank_name,
                    Bank_card_number: this.bank_number
                };

                this.$req.post({
                    data
                }).then(res => {
                    this.handleBankNameResult(res);
                });
                // }
            },
            //银行卡匹配
            _bank() {
                if (this.bank_number == '') {
                    this.bank_name = '';
                    this.bankNameByApi = false;
                    return;
                }
                var data = {
                    api: 'app.user.Verification',
                    Bank_name: this.bank_name,
                    Bank_card_number: this.bank_number
                };

                this.$req.post({
                    data
                }).then(res => {
                    this.handleBankNameResult(res);
                });
            },
            edit() {
                var name = /^[\u4e00-\u9fa5]{2,8}$/;
                if (!name.test(this.user_name)) {
                    this.user_name = '';
                    uni.showToast({
                        title: this.language.putForward.nameTip,
                        duration: 1000,
                        icon: 'none'
                    });
                    return false;
                }
                
                
                
                var patt = /^[1-9]{1}\d{15}|\d{18}$/;
                
                if (this.bank_number.indexOf('*') > -1) {
                    uni.showToast({
                        title: this.language.putForward.bankTip,
                        duration: 1000,
                        icon: 'none'
                    });
                    return false;
                }
                var pattern = /^([1-9]{1})(\d{5})$/;
                if (pattern.test(this.bank_number)) {
                    this.bank_number = '';
                    uni.showToast({
                        title: this.language.putForward.bankTip,
                        duration: 1000,
                        icon: 'none'
                    });
                }

                if (this.fastTap) return false;
                
                this.fastTap = true;
                let data = {
                    Cardholder: this.user_name,
                    Bank_name: this.bank_name,
                    branch: this.branch,
                    Bank_card_number: this.bank_number,
                    
                    api:"mch.App.MchBank.EditBank",
                    is_default: this.is_default,
                    id: this.id
                }
                this.$req.post({
                    data: data
                }).then(res => {
                    if (res.code == 200) {
                        
                        this.is_showToast = 1
                        this.is_showToast_obj.imgUrl = this.sus
                        this.is_showToast_obj.title = this.id?this.language.putForward.bjcg:this.language.putForward.tjcg
                        setTimeout(()=>{
                            this.is_showToast = 0
                        },1500) 
                        setTimeout(() => {
                            uni.navigateBack({
                                delta: 2
                            });
                        }, 1500)
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }

                    this.fastTap = false;
                })
            },
            submit() {
                // 开户名校验
                // var name = /^[\u4e00-\u9fa5]{2,8}$/;
                // if (!name.test(this.user_name)) {
                //     this.user_name = '';
                //     uni.showToast({
                //         title: this.language.putForward.nameTip,
                //         duration: 1000,
                //         icon: 'none'
                //     });
                //     return
                // }
                
                
                
                // if (this.bank_number.length!=16&&this.bank_number.length!=17&&this.bank_number.length!=18&&this.bank_number.length!=19) {
                //     uni.showToast({
                //         title: '请填写完整银行卡号',
                //         duration: 1000,
                //         icon: 'none'
                //     });
                //     return false;
                // }
                // if(!this.bank_name){
                //     uni.showToast({
                //         title: '请输入正确的银行卡号',
                //         duration: 1000,
                //         icon: 'none'
                //     });
                //     return false;
                // }
                if(!this.branch){
                    uni.showToast({
                        title: '请输入开户支行',
                        duration: 1000,
                        icon: 'none'
                    });
                    return false;
                }
                // var pattern = /^([1-9]{1})(\d{5})$/;
                // if (pattern.test(this.bank_number)) {
                //     this.bank_number = '';
                //     uni.showToast({
                //         title: this.language.putForward.bankTip,
                //         duration: 1000,
                //         icon: 'none'
                //     });
                //     return
                // }
                if (this.fastTap) return false;

              

                this.fastTap = true;
                let data = {};

                if (this.type === 'store') {
                    data = {
                        Cardholder: this.user_name,
                        Bank_name: this.bank_name,
                        branch: this.branch,
                        Bank_card_number: this.bank_number,
                        
                        api:"mch.App.MchBank.AddBank",
                        is_default: this.is_default
                    }
                } else {
                    data = {
                        Cardholder: this.user_name,
                        Bank_name: this.bank_name,
                        branch: this.branch,
                        Bank_card_number: this.bank_number,
                        is_default: this.is_default,
                        api: 'app.user.add_bank',
                        id:this.id
                    }
                }

                this.$req.post({
                    data: data
                }).then(res => {
                    if (res.code == 200) {
                        this.is_showToast = 1
                        this.is_showToast_obj.imgUrl = this.sus
                        this.is_showToast_obj.title = this.id?this.language.putForward.bjcg:this.language.putForward.tjcg
                        setTimeout(()=>{
                            this.is_showToast = 0
                        },1500) 
                        
                        setTimeout(() => {
                            if(this.type != 'store'){
                                
                                uni.navigateTo({
                                    url: '/pagesB/myWallet/bankCard'
                                })
                            }else{
                                this.navBack();
                            }
                        }, 1500)
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }

                    this.fastTap = false;
                })
            }
        }
    }
</script>
<style>
    page {
        background: #f6f6f6;
    }

   
</style>
<style lang="less" scoped> 
    @import url("@/laike.less");
    /deep/uni-input-input {
        font-size: 32rpx !important;
    }
    .message {
        font-size: 28rpx;
        color: #020202;
        background-color: #fff;
        position: relative;
        padding-bottom: 20rpx;
        padding-top: 20rpx;
        border-radius: 0 0 24rpx 24rpx;
    }

    
    .input_box {
        font-size: 32rpx !important;
        font-size: #333333;
    }

    .message li {
        padding: 0 32rpx;
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12rpx;

        &.default-li {
            display: flex;
            justify-content: space-between;
        }
    }

    .bank_card_info {
        margin: 0 !important;
    }

    li span {
        width: 160rpx;
        display: flex;
        height: 90rpx;
        align-items: center;
        justify-content: flex-start;
        font-size: 32rpx;
        color: #333333;
        white-space: nowrap;

        &.require {
            color: red;
            width: auto;
            margin-right: 2rpx;
        }
    }

    .message uni-input {
        border: 1rpx solid rgba(0, 0, 0, 0.1);
        height: 76rpx;
        border-radius: 16rpx;
    }

    .message input {
        padding: 0 20rpx;
        border: 2rpx solid rgba(0, 0, 0, 0.1);
        flex: 1;
        font-size: 32rpx;
        color: #333333;
        margin-left: 4rpx;
    }
    .default_btn_switch{
        margin-right: 10rpx;
    }
    .bankCardAdd {
        height: 100vh;
    }

    .btn {
        width: 100%;
        /* #ifdef APP-PLUS || H5 */
        min-height: 124rpx;
        /* #endif */
        /* #ifdef MP-WEIXIN */
        padding-top: 15rpx;
        padding-bottom: 15rpx;
        /* #endif */
        
        position: fixed;
        bottom: 0rpx;
        background-color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        padding-bottom: constant(safe-area-inset-bottom);
        /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
        // left: calc((100% - 690rpx) / 2);

    }

    .btn_titlel {
        background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
        border-radius: 52px;
        font-size: 32rpx;
        height: 92rpx;
        color: #fff;
        text-align: center;
        width: 686rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50rpx;
        margin:  16rpx 0;
    }

    .btn1 {
        width: 100%;
        height: 124rpx;
        position: absolute;
        bottom: 0rpx;
        background-color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        padding-bottom: constant(safe-area-inset-bottom);
        /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
        // left: calc((100% - 690rpx) / 2);

    }

    .default_btn {
        width: 100%;
        height: 108rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        background-color: #fff;
        border-radius: 24rpx;
        margin-top: 24rpx;
    }

    .default_btn_txt {
        font-size: 32rpx;
        
        font-weight: 400;
        color: #333333;
        margin-left: 32rpx
    }

    .btn_title {
        .solidBtn();
        background-color: #cccccc;
        font-size: 32rpx;
        height: 92rpx;
        text-align: center;
        width: 686rpx;
        display: flex;
        align-items: center;

        justify-content: center;
        border-radius: 50rpx;
    }
</style>
