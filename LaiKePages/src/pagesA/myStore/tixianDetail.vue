<template>
    <div class="container">
        <heads :title="language.tixian.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        
        <toload :load="load">
        </toload>
        <template v-if="load">
            <view class="zprice">-{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(list.money?list.money:list.price)}}</view>
            <view class="status" :class="{red: status == 0}">{{status==0?language.tixian.Tips[0]:status==1?language.tixian.Tips[1]:language.tixian.Tips[2]}}</view>

            <view class="list_box">
                <view class="list">
                    <view>{{language.tixian.money}}</view>
                    <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(list.money?list.money:list.price)}}</text>
                </view>
                <view class="list">
                    <view>{{language.tixian.s_charge}}</view>
                    <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(list.s_charge)}}</text>
                </view>
                <view class="list">
                    <view>{{language.walletDetail.txfs}}</view>
                    <!-- <text>{{list.Bank_name}} ({{list.Bank_card_number}})</text> -->
                    <text>
                        <p class="mybody_div_r" v-if="list.withdrawalMethod == '微信零钱'" style="display: flex;align-items: center;">
                           <image class="mybody_div_r-image" :src="wx"></image>{{list.withdrawalMethod}}
                        </p>
                        <p class="mybody_div_r" v-else>{{list.withdrawalMethod}}</p>
                    </text>
                </view>
                <view class="list">
                    <view>{{language.tixian.add_date}}</view>
                    <text>{{list.add_date}}</text>
                </view>
                <view class="list" v-if="status == 2">
                    <view>{{language.tixian.refuse}}</view>
                    <text>{{list.refuse}}</text>
                </view>
                <view class="list" v-if="status != 0">
                    <view>{{language.tixian.examine_date}}</view>
                    <text>{{list.examine_date}}</text>
                </view>
            </view>

            <view class="btnBox">
                <template >
                    <!-- 删除申请 -->
                    <view class="border" v-if="status == 2" @tap="_del()">{{language.tixian.deletes}}</view>
                </template>
            </view>
            
            <!-- 注册成功 -->
            <showToast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" @richText="richText" @confirm="confirm"></showToast>
        </template>
    </div>
</template>

<script>
import showToast from '@/components/aComponents/showToast.vue'
export default {
    data() {
        return {
            title: '提现详情',
            wx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wx.png',
            status: 0, // 0是审核中 1是已完成  2是审核失败
            shop_id: '',
            id: '',
            load: false,
            list: [],
            //组件化弹窗
            is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
            is_showToast_obj: {}, //imgUrl提示图标  title提示文字
            bgColor: [{
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
        };
    },
    components:{
        showToast
    },
    onLoad(option) {
        this.id = option.id
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
        this._axios();
        
        this.is_showToast_obj.title = '撤销提示'
        this.is_showToast_obj.content = '确定要撤销审核嘛？'
        this.is_showToast_obj.button = '取消'
        this.is_showToast_obj.endButton = '确认' 
    },
    methods: {
		changeLoginStatus(){
			this._axios();
		},
        // 取消弹窗
        richText(){
            this.is_showToast=0
        },
        // 确认弹窗
        confirm(){
            this.is_showToast=0
        },
        _axios() {
            this.$req.post({
                data: {
                    shop_id: this.shop_id,
                    // module: 'app',
                    // action: 'mch',
                    // m: 'Withdrawal_details',
                    api:"mch.App.Mch.Withdrawal_details",
                    id: this.id
                }
            })
            .then(res => {
                if (res.code == 200) {
                    this.list = res.data.list;
                    this.status = res.data.list.status
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
                this.load = true
            });
        },
        _del() {
            uni.showLoading({
                title: this.language.tixian.Tips[3],
                mask: true
            });
            this.$req.post({
                data: {
                    shop_id: this.shop_id,
                    // module: 'app',
                    // action: 'mch',
                    // m: 'del_Withdrawal_details',
                    api:"mch.App.Mch.del_Withdrawal_details",
                    id: this.id
                }
            })
            .then(res => {
                uni.hideLoading();
                if (res.code == 200) {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                    setTimeout(() => {
                        uni.navigateBack()
                    }, 1500);
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            });
        },
        _cancel(){
            this.is_showToast=4
        },
        _reapply(){
            uni.navigateTo({
                url: '/pagesB/myWallet/putForward?type=store&id='+this.id + '&back_type=1'
            });
        }
    },
};
</script>

<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/tixianDetail.less');
.mybody_div_r-image{
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        margin-right: 12rpx;
    }
</style>
