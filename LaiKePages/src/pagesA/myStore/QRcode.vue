<template>
    <div class="QrCodeBox" :style="writeOffType==1?headBg1:headBg0">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>        
         <heads :bgColor="bgColor" :ishead_w="3" title=" "></heads>        
        <!-- #ifdef MP-WEIXIN -->
        <template v-if="writeOffType==0">
            <camera class="camera" mode="scanCode" @scancode="QRcode" ></camera>
        </template>
        <!-- #endif -->
        <view class="inputQRcodBox" v-if="writeOffType==1">
            <view class="inputTitle">{{language.QRcode.sdsryzm}}</view>
            <!-- 虚拟商品 预约 选择门店 -->
            <view class="inputBox XN-xzMch" @tap="_setMch" v-if="xnGoodsHx == 2">
                <span>{{xnMchName}}</span>
                <image :src="baga"></image>
            </view>
            <view class="inputBox">
                <input v-model="shop_code" type="text" :placeholder="language.QRcode.qsryzm" placeholder-style="color: #999999;justify-content: center;">
            </view>
            <view class="inputBtn" :style="{marginTop: xnGoodsHx == 2?'76rpx':'174rpx'}" @tap="QRsuccess">{{language.QRcode.qr}}</view>
        </view>
        <!-- #ifndef H5 -->
        <view class="modeBtn">
            <view class="QRcode-btn" :class="{QRcodeType:writeOffType==0}" @tap="changeWriteOff(0)">
                <image :src="writeOffType==0?QRcode_b:QRcode_w" mode="widthFix"></image>
                <view class="QRcode_title">{{language.QRcode.sm}}</view>
            </view>
           
            <view class="QRcode-btn" :class="{QRcodeType:writeOffType==1}" @tap="changeWriteOff(1)">
                <image :src="writeOffType==1?QRcode_input_b:QRcode_input_w" mode="widthFix"></image>
                <view class="QRcode_title">{{language.QRcode.sd}}</view>
            </view>
        </view>
        <!-- #endif -->
        <view class="scanErrorDialog" v-if="errorDialog">{{errorMsg}}</view>
         <!-- 弹窗组件 -->
        <choose-s 
            ref="refChooseS"
            :is_type="chooseType"
            :is_choose="mchArr" 
            :is_choose_obj="is_choose_obj"
            @_choose="_choose" 
            @_isHide="_isHide">
        </choose-s>
    </div>
</template>

<script>
import chooseS from "@/components/aComponents/choose.vue"
export default {
    data() {
        return {
            shop_id: '',
            order_id: '',
            p_price: '',
            sNo: '',
            QR_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/store_input.png',
            QR_flag: true,
            headBg1: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon/QRcode_bg.png);background-size: 100vw 100vh;background-repeat: no-repeat;',
            headBg0:"background:'transparent'",
            QRcode_b: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/QRcode_b.png',
            QRcode_w:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/QRcode_w.png',
            QRcode_input_w: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/QRcode_input_w.png',
            QRcode_input_b:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/QRcode_input_b.png',
            baga: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/baga.png',
            bgColor:[
                    {
                        item: 'transparent'
                    },
                    {
                        item: 'transparent'
                    }
                ],
            writeOffType:1,
            shop_code: '',
            errorDialog:false,//手动核销错误弹窗
            errorMsg:'',//核销失败的弹窗信息
            mchArr: [],//
            chooseType: 0,//选择性别
            is_choose_obj: {
                title: '选择门店',
                background: '#F4F5F6',//显示图标
                borderRadius: '24rpx 24rpx 0 0',//提示文字
            },
            // 虚拟商品
            xnGoodsId: '',//虚拟商品id
            xnGoodsHx: '',//虚拟商品 是否已预约门店 1已预约 2未预约（未预约的 需要在此时选择门店）
            xnMchList: '',//虚拟商品核销门店list
            xnMchID: '',//选择的-虚拟商品核销门店id
            xnMchName: '',//选择的-虚拟商品核销门店name
            // 虚拟商品结束
        };
    },
    components:{
        chooseS
    },
    onLoad(option) {
        this.order_id = option.order_id
        // this.writeOffType=1
        //虚拟商品
        if(option.xnGoodsId && option.xnGoodsHx){
            this.xnGoodsId = option.xnGoodsId
            this.xnGoodsHx = option.xnGoodsHx
            if(this.xnGoodsHx == 2){ 
                let data = {
                    api: 'mch.App.Mch.get_write_shop',
                    s_no: this.xnGoodsId
                } 
                this.$req.post({data}).then(res=>{ 
                    if(res.code == 200){
                        //默认选择第一个核销门店
                        this.xnMchName = res.data[0]?.name  ||''
                        this.xnMchID = res.data[0].id
                        //虚拟商品核销门店list
                        this.xnMchList = res.data
                        let xnMchList = []
                        this.xnMchList.forEach((item, index)=>{
                            xnMchList.push(item.name)
                        })
                        this.mchArr = xnMchList
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }
                })
            }
        }
        if (option.shop_id) {
            this.shop_id = option.shop_id;
            this.shop_id = this.shop_id.replace(/,/g, '');
        } else {
            this.shop_id = this.$store.state.shop_id;
        }
    },
    onShow() {
        // this.writeOffType=1
    },
    methods: {
        _setMch(){
            if(this.mchArr.length == 0){
                uni.showToast({
                    title:'请先添加门店',
                    icon:'none'
                })
                return
            }
            this.chooseType = 2
        },
        _choose(index){
            this.chooseType = 0
            this.xnMchName = this.xnMchList[index].name
            this.xnMchID = this.xnMchList[index].id
        },
        _isHide(){
            this.chooseType = 0
        },
        changeWriteOff(type){
            this.writeOffType = type    
            if(type==0){
                this.QRs()
            }
        },
        // 点击确认按钮提交验证码
        QRsuccess(e) {
            // 成功后跳转 QRsuccess页面
            uni.showLoading({
                title: this.language.QRdraw.Tips[0]
            });
            let me = this;
            if (me.shop_code == '') {
                uni.showToast({
                    title: this.language.QRdraw.Tips[1],
                    duration: 1500,
                    icon: 'none'
                });
                return;
            }
            let data = {
                api:'mch.App.Mch.OrderInfoForCode',
                shop_id: me.shop_id,//店铺id
                extraction_code: me.shop_code
            };
            //虚拟商品
            if(this.xnMchID){
                data.write_shop_id = this.xnMchID
            }
            if(this.order_id){
                data.order_id = this.order_id
            }
            this.$req.post({data}).then(res => {
                uni.hideLoading();
                if (res.code == 200) {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                    res.data.orderInfo.shop_id=me.shop_id//赋值shop_id到订单信息中
                    uni.redirectTo({
                        url: '/pagesC/myStore/QRsuccess?data=' + encodeURIComponent(JSON.stringify(res.data))
                    });
                } else {
                    if(this.writeOffType==0){
                        me._navigateTo('/pagesC/myStore/QRfailed')
                    }else{
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                        // ui 要求不实用此提示 bugID 56323
                        // this.errorMsg=res.message
                        // this.errorDialog=true
                        // setTimeout(()=>{
                        //     this.errorDialog=false
                        // },1500)
                    }
                }
            })
            
        },
        _navigateTo(url) {
            uni.redirectTo({
                url
            });
        },
        QRcode(res) {
            if (!this.QR_flag) {
                return;
            }
            this.QR_flag = false;
            const me = this;
            // 小程序扫码
            // 成功后跳转 QRsuccess页面
            let data = {
                api:'mch.App.Mch.OrderInfoForCode',
                shop_id: me.shop_id,
                extraction_code: res.detail.result
            };
            //虚拟商品
            if(this.xnMchID){
                data.write_shop_id = this.xnMchID
            }
            if(me.order_id){
                data.order_id = me.order_id
            }
            this.$req.post({data}).then(res => {
                uni.hideLoading();
                
                if (res.code == 200) {
                    uni.createCameraContext().stop()
                    setTimeout(() => {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }, 100);
                    // 成功后跳转 QRsuccess页面
                    res.data.orderInfo.shop_id = me.shop_id//赋值shop_id到订单信息中
                    
                    setTimeout(() => {
                        uni.navigateTo({
                            url: '/pagesC/myStore/QRsuccess?data=' + encodeURIComponent(JSON.stringify(res.data))
                        });
                    }, 1000);
                } else {
                    this.QR_flag = true;
                    me._navigateTo('/pagesC/myStore/QRfailed')
                }  
            })
        },
        // 小程序以外扫码
        QRs() {
            let me = this;
            // #ifndef H5
            uni.scanCode({
                success: function(rew) {
                    let data = {
                        api:'mch.App.Mch.OrderInfoForCode',
                        shop_id: me.shop_id,
                        extraction_code: rew.result
                    };
                    //虚拟商品
                    if(this.xnMchID){
                        data.write_shop_id = this.xnMchID
                    }
                    if(me.order_id){
                        data.order_id = me.order_id
                    }
                    me.$req.post({data}).then(res => {
                        uni.hideLoading();
                        if (res.code == 200) {
                            setTimeout(() => {
                                uni.showToast({
                                    title: res.message,
                                    duration: 1500,
                                    icon: 'none'
                                });
                            }, 100);
                            // 成功后跳转 QRsuccess页面
                            res.data.orderInfo.shop_id = me.shop_id//赋值shop_id到订单信息中
                            setTimeout(() => {
                                uni.navigateTo({
                                    url: '/pagesC/myStore/QRsuccess?data=' + encodeURIComponent(JSON.stringify(res.data))
                                });
                            }, 1000);
                        } else {
                            // 失败的页面
                            me._navigateTo('/pagesC/myStore/QRfailed')
                        }
                    })
                },
                fail:function(err){
                    me.writeOffType=1
                }
            });
            // #endif

            // #ifdef H5
            uni.showToast({
                icon: 'none',
                title: this.language.QRcode.Tips[1]
            });
            // #endif
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/QRcode.less');
/* #ifdef MP */
/deep/.headerBox{
    padding-top: 0 !important;
    height: auto !important;
}
/* #endif */
</style>
