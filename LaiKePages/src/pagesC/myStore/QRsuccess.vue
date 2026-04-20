<template>
    <div>
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        <heads :title="language.myStore.QRsuccess.title" :bgColor="bgColor" titleColor="#333333"  ishead_w="2"></heads>
        <div class="QR-text">
            <view class="orderInfo">
                <view>
                    <view>
                        <span>{{language.myStore.QRsuccess.ddbh}}：</span>
                        <span>{{orderInfo.sNo}}</span>
                    </view>
                    <view>
                        <span>{{language.myStore.QRsuccess.dhx}}</span>
                    </view>
                </view>
                <view>
                    <view>
                        <span>{{language.myStore.QRsuccess.yhmc}}：</span>
                        <span>{{orderInfo.name}}</span>
                    </view>
                    <view>
                        <span>{{language.myStore.QRsuccess.yhdh}}：</span>
                        <span>{{orderInfo.mobile}}</span>
                    </view>
                </view>
                <view>
                    <view>
                        <span>{{language.myStore.QRsuccess.spsl}}：</span>
                        <span>{{orderInfo.num}}</span>
                    </view>
                    <view>
                        <span>{{language.myStore.QRsuccess.ddje}}：</span>
                        <span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(orderInfo.z_price)}}</span>
                    </view>
                </view>
            </view>
            <!-- 虚拟商品已预约核销门店信息 -->
            <view class="xn-hxMch" v-if="orderInfo.otype == 'VI' && orderInfo.show_write_time == 1">
            	<view>
            		<view>预约门店</view>
            		<view>{{orderInfo.write_time_info.mch_store}}</view>
            	</view>
            	<view>
            		<view>预约时间</view>
            		<view>{{orderInfo.write_time_info.time}}</view>
            	</view>
            </view>
            <!-- 核销门店 -->
            <template  v-if="hxShopList.length">
                <view class="hxShop" @tap="chooseType = 2">
                    <p>适用核销门店</p>
                    <view>
                        <span>{{hxShopList[chooseIndex].name}}</span>
                        <image :src="baga"></image>
                    </view>
                </view>
            </template>
            <view class="por-list" v-for="(item , index) in por_list">
                <view class="orders">
                    <!-- <view v-if="orderInfo.otype == 'VI'" class="xn-choose">
                        <image :src="item.choose ? quanHei : quanHui" @tap="_choose(index)"></image>
                    </view> -->
                    <view class="pro-img">
                        <image :src="item.img"></image>
                    </view>
                    <view class="pro-content">
                        <view>{{item.product_title}}</view>
                        <view>
                            <span>{{item.size}}</span>
                            <span>x{{item.num}}</span>
                        </view>
                    </view>
                </view>
                <view v-if="orderInfo.otype == 'VI'" class="XN-hxnum">
                    <view>待核销次数</view>
                    <view>
                        <span>{{item.write_off_num}}</span>
                    </view>
                </view>
            </view>
        </div>
        <view style="height: 124rpx;"></view>
        <view class="btn">
            <view class="enter" @tap="sureBtn">
                {{language.myStore.QRsuccess.qr}}
            </view>
        </view>
        <!-- 切换核销门店 -->
        <chooseS 
            ref="isLanguage" 
            :is_choose_obj='is_choose_obj' 
            :is_type="chooseType" 
            :is_choose="isLangth"
             @_choose="_chooseS" 
             @_isHide="_isHide">
        </chooseS>
        <!-- 提示弹窗 -->
        <showToast
            :is_showToast="is_showToast" 
            :is_showToast_obj="is_showToast_obj">
        </showToast>
    </div>
</template>

<script>
import showToast from "@/components/aComponents/showToast.vue"
import chooseS from "@/components/aComponents/choose.vue"
export default {
    data() {
        return {
            orderInfo:{},
            por_list: [],
            bgColor: [{
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            is_showToast: 0,
            is_showToast_obj: {},
            is_choose_obj:{
                title: '选择门店',
                colorLeft: '#999999',
                colorRight: '#FA5151',
                background: '#F4F5F6',//显示图标
                borderRadius: '24rpx 24rpx 0 0',//提示文字
            },
            isLangth: [],//核销门店列表name
            chooseType: 0,//选择核销门店弹窗
            chooseIndex: 0,//选择核销门店下标
            hxShopList: [],//核销门店列表
            quanHui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
            quanHei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
            baga: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/baga.png',
        };
    },
    onLoad(option) {
        if(option&&option.data){
            this.orderInfo = JSON.parse(decodeURIComponent(option.data)).orderInfo;
            this.por_list = JSON.parse(decodeURIComponent(option.data)).por_list;
            //虚拟商品 核销门店
            let datas = JSON.parse(decodeURIComponent(option.data))
            if(datas.hxShop){
                this.hxShopList = datas.hxShop;
                this.isLangth = this.hxShopList.map((item, index)=>{
                    return item.name
                });
            }
            //虚拟商品核销
            if(this.orderInfo.otype == 'VI'){
                this.por_list.forEach((item, index)=>{
                    this.$set(item, 'choose', true)
                })
            }
        }
    },
    components: {
        chooseS,
        showToast,
    },
    methods: {
        //选择的语言
        _chooseS(index){
            this.chooseType = 0
            this.chooseIndex = index
        },
        //隐藏
        _isHide(){
            this.chooseType = 0
        },
        //选择需要核销的商品
        _choose(index){
            this.por_list.forEach((itm, inx)=>{
                if(inx == index){
                    itm.choose = true
                } else {
                    itm.choose = false
                }
            })
        },
        // 跳转
        _navTo(url) {
            uni.redirectTo({
                url: url,
            });
        },
        // 确认核销
        sureBtn() {
            let that=this
            var data = {
                api: 'app.mch.verification_extraction_code',
                shop_id:this.orderInfo.shop_id,
                order_id:this.orderInfo.id,
                extraction_code: this.orderInfo.extraction_code.split(',')[0]//取订单信息中的第一个code值
            };
            //虚拟商品核销
            if(this.orderInfo.otype == 'VI'){
                let str = ''
                let str1 = ''
                this.por_list.forEach((item, index)=>{
                    if(item.choose){
                        if(index == 0){
                            str = item.p_id
                            str1 = item.c_id
                        } else {
                            str = str + ',' + item.p_id
                            str1 = str1 + ',' + item.c_id
                        }
                    }
                })
                if(str == ''){
                    uni.showToast({
                        title: '请选择需要核销的商品',
                        icon: 'none'
                    })
                    return
                }
                data.p_id = str
                data.c_id = str1
                //线下核销 无需预约 选择核销门店 传入选择的核销门店id
                if(this.orderInfo.write_shop_id){
                    //订单管理点击核销进入（上一级页面选择核销门店id，传入当前页面）
                    data.write_shop_id = this.orderInfo.write_shop_id
                } else if(this.hxShopList.length){
                    //我的店铺直接点击进入（当前页面选择核销门店id）
                    data.write_shop_id = this.hxShopList[this.chooseIndex].id
                }
            }
            this.$req.post({data}).then(res => {
                if (res.code == 200) {
                    this.is_showToast = 1
                    this.is_showToast_obj.imgUrl = this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png'
                    this.is_showToast_obj.title = res.message
                    setTimeout(()=>{
                        this.is_showToast = 0
                        that._navTo('/pagesA/myStore/myOrder')
                    },2000) 
                }else{
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'error'
                    });
                }
                
            })
        },
        // 返回按钮
        Return() {
            uni.navigateBack({
                delta: 1
            });
        }
    }
};
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/QRsuccess.less');
</style>
