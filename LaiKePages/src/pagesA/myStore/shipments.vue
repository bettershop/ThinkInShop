<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        <heads  :title="title" :bgColor="bgColor" titleColor="#000" :ishead_w="ishead_w"></heads>
        <template v-if="type == 1">
            <view class="list list1">
                <view class="list_li" v-for="(item, index) in list.list" :key="index" v-if="item.r_status == 1" @tap="_choose(item)">
                    <image v-if="item.chooseMe" class="xuanze" :src="xuanze"></image>
                    <view class="xuanze_i" v-else></view>
                    <image class="image" :src="item.imgurl"></image>
                    <view class="right">
                        <view class="view">{{ item.p_name }}</view>
                        <view class="text">{{ item.size }}</view>
                    </view>
                </view>
            </view>
           
            
            <view class="btn">
				<!-- 下一步 （选择商品后点击）-->
                <div class='bottomBtn' @tap="_shipments">{{language.shipments.xyb}}</div>
            </view>
        </template>
        <template v-else>
            <view class="_ul">
                <view class="_li">
                    <text>{{ language.shipments.express[0] }}</text>
                    <view class="_input" >
                        <view class="" @tap="showSinglePicker"></view>
                        <input  @tap="showSinglePicker" type="text" disabled  :placeholder="language.shipments.express[1]" v-model="company" />
                        <image  @tap="showSinglePicker" class="jiantou" :src="jiantou"></image>
                    </view>
                </view>
                <view class="_li" v-if="!logistics_type">
                    <text>{{ language.shipments.express[2] }}</text>
                    <view class="_input"><input type="text" :placeholder="language.shipments.express[3]" v-model="courier_num" placeholder-style="color: #999999;"/></view>
                </view>
                <view class="_li2">
                    <p>温馨提示</p>
                    <p>（1）发货时，选择的物流公司如暂未开通电子面单功能，只<text style="margin-left: 60rpx;">能人工填写物流单号；</text></p>
                    <p>（2）已开通电子面单的物流公司，支持线上打印面单功能；</p>
                </view>
            </view>
            <view class="list">
                <view class="list_li" v-for="(item, index) in list.list" :key="index" v-show="item.r_status == 1&&item.chooseMe">
                    <view class="list_li_l">
                        <image class="image" :src="item.imgurl"></image>
                        <view class="right">
                            <view class="view">{{ item.p_name }}</view>
                            <view class="text">数量：{{item.deliverNum}} 品牌：{{item.brand_name}}</view>
                        </view>
                    </view>
                    
                    <view class="list_text">
                        <span style="color: #999999;">发货数量 </span>
                        <view class="list_num">
                            <uni-number-box v-model="item.mynum" :min="1" :max="item.deliverNum" background="none"  />
                        </view>
                    </view>
                    
                </view>
            </view>
          
               <!-- 普通发货 -->
               <view class="bottom_ship" @tap="_shipments1" v-if="pageType != 1">{{ language.shipments.determine }}</view>
               <!-- 回寄物流 发货 -->
               <view class="bottom_ship" @tap="_shipments2" v-else>{{ language.shipments.determine }}</view>
 
            
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
        </template>
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
            title: '选择商品',
            is_sus:false,
            sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
            list: '',
            hwxz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/hwxz.png',
            rxz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/rxz.png',
            xuanze: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            quanxuan: false,
            bgColor: [{
                    item: '#fff'
                },
                {
                    item: '#fff'
                }
            ],
            ishead_w:2,
            courier_num: '', //快递单号
            company: '', //快递公司
            orderList_id: [],
            sNo: '',
            shop_id: '',
            kuaidiList: [],
            express_id: '', //快递公司ID

            show: false,
            themeColor: '#D73B48',
            mode: '',
            deepLength: 1,
            pickerValueDefault: [0],
            pickerValueArray: [],
            id: '',

            type: 1,
            pageType: '',
            
            order_details_id: '',
            group:'',
            
            logistics_type:false,//面单
        };
    },
    watch:{
        courier_num(){
            this.$nextTick(function() {
                this.courier_num = this.courier_num.replace(/[\W]/g, "")//只允许输入数字和字母（如需限制其它类型则换个正则即可）
            })
        }
    },
    computed: {},
    onLoad(option) {
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
        this.id = option.id;
        if(option.group){
            this.group = option.group
        }
        if (option.type == 1) {
            this.type = 2;
            this.pageType = 1;
            this.sNo = option.id;
            this.order_details_id = option.order_details_id
        } else {
            this._axios();
        }
        this.title = this.language.shipments.title;
    },
    onShow() {

        if (this.pageType == 1) {
            this.title = this.language.shipments.title2;
            this.getKuaidi()
        }
    },
    methods: {
        // 售后回寄商品
        async _shipments2(){
            if(!this.express_id){
                uni.showToast({
                    title: this.language.shipments.qxzkdgs,
                    icon: 'none'
                })
                return
            }
            if(!this.courier_num){
                uni.showToast({
                    title: this.language.shipments.qsrkddh,
                    icon: 'none'
                })
                return
            }
           var data = {
               shop_id: this.shop_id,
            //    module: 'app',
            //    action: 'mch',
            //    m: 'examine',
            api:'mch.App.Mch.Examine',
               sNo: this.sNo,
               id: this.order_details_id,
               express_id: this.express_id, // 快递ID
               courier_num: this.courier_num, // 快递单号
               r_type: 11
           };
           if(this.group == 'pintuan'){
               data.action = 'groupMch'
               data.m = 'examine'
           }
           uni.showLoading({
               title: this.language.shipments.qsh,
               mask: true
           })
           await this.$req.post({data}).then(res=>{
               uni.hideLoading()
               uni.showToast({
                   title: res.message,
                   icon: 'none'
               })
               if(res.code == 200){
                   setTimeout(()=>{
                       uni.navigateBack({
                           delta: 1
                       })
                   },1000)
               }
           })
           
        },
        getKuaidi(){
           
            let data={
                        sNo: this.sNo,
                        
                        api:'mch.App.Mch.GetLogistics',
            
                    }
            
            this.$req.post({data}).then(res => {
                uni.hideLoading();
                if (res.code == 200) {
                    var list = [];
                    this.kuaidiList = res.data.list;
                    for (var key in res.data.list) {
                        list.push(res.data.list[key].kuaidi_name);
                    }
                    this.pickerValueArray = list;
                } else if (res.code == 404) {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                    setTimeout(function() {
                        uni.navigateTo({
                            url: '/pagesD/login/newLogin?landing_code=1'
                        });
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
        async _axios() {
            let data = {
               
                api:"mch.App.Mch.Deliver_show",
                id: this.id,
                shop_id: this.shop_id
            };
            let res = await this.$req.post({ data });
            if (!res.data.goods.length) {
                uni.showToast({
                    title: this.language.shipments.Tips[0],
                    icon: 'none'
                });
                setTimeout(() => {
                    uni.navigateBack();
                }, 1500);
                return false;
            }
            if(res.data.goods && res.data.goods.length == 1){
               
                this.$set(res.data.goods[0],'chooseMe',true)
            }
            this.list = {
                list: res.data.goods,
                sNo: this.id
            };
            this.list.list.forEach(e=>{
                e.mynum=e.deliverNum
            })
        },
        _choose(item) {
            this.$set(item, 'chooseMe', !item.chooseMe);
            let list = this.list.list;
            let flag = true;
            for (let i = 0; i < list.length; i++) {
                if (!list[i].chooseMe) {
                    flag = false;
                    break;
                }
            }
            this.quanxuan = flag;
        },
        _chooseQ() {
            this.quanxuan = !this.quanxuan;
            let list = this.list.list;
            for (let i = 0; i < list.length; i++) {
                this.$set(list[i], 'chooseMe', this.quanxuan);
            }
        },
        _shipments() {
            this.orderList_id = [];
            var dd = this.list.list;
            for (let i = 0; i < dd.length; i++) {
                if (dd[i].chooseMe && dd[i].r_status == 1) {
                    this.orderList_id.push({'detailId':dd[i].id});
                }
            }
            this.sNo = this.list.sNo;
            if (this.orderList_id.length == 0) {
                uni.showToast({
                    title: this.language.shipments.Tips[1],
                    duration: 1500,
                    icon: 'none'
                });
                return;
            }
            uni.showLoading({
                title: this.language.shipments.Tips[2]
            });
            
            let data={
                        sNo: this.sNo,
                       
                        api:'mch.App.Mch.GetLogistics',
            
                    }
            this.$req
                .post({data})
                .then(res => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        var list = [];
                        this.kuaidiList = res.data.list;
                        for (var key in res.data.list) {
                            list.push(res.data.list[key].kuaidi_name);
                        }
                        this.pickerValueArray = list;
                        this.type = 2;
                        this.title = this.language.shipments.title2;
                    } else if (res.code == 404) {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                        setTimeout(function() {
                            uni.navigateTo({
                                url: '/pagesD/login/newLogin?landing_code=1'
                            });
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
        async _shipments1() {
            this.orderList_id = [];
            var dd = this.list.list;
            for (let i = 0; i < dd.length; i++) {
                if (dd[i].chooseMe && dd[i].r_status == 1) {
                    this.orderList_id.push({'detailId':dd[i].id,'num':dd[i].mynum});
                }
            }
            if(!this.express_id){
                uni.showToast({
                    title: this.language.shipments.express[1],
                    duration: 1500,
                    icon: 'none'
                });
                return;
            }
			
			let expressid = this.express_id
			let courierNum = !this.logistics_type ? this.courier_num : ''
			let type = this.logistics_type ? '2':'1'
			let psyInfo = ''
			let orderList = this.orderList_id
			let list = {
				expressid, //物流公司id
				courierNum, //物流号
				type,	//发货类型 1普通发货 2电子面单 3商家配送
				psyInfo, //配送员信息
				orderList, //订单信息 detailId订单详情id num订单商品数量
			}
			//订单发货
			await this.LaiKeTuiCommon.orderSend(this, list).then((res)=>{
				if (res.code == 200) {
                    uni.hideLoading()
                    this.is_sus = true
                    setTimeout(()=> {
						uni.navigateBack({
							delta: 1
						});
					},1500)
				} else if (res.code == 404) {
					uni.showToast({
						title: res.message,
						duration: 1500,
						icon: 'none'
					});
					setTimeout(function() {
						uni.navigateTo({
							url: '/pagesD/login/newLogin?landing_code=1'
						});
					}, 1500);
				} else {
					uni.showToast({
						title: res.message,
						duration: 1500,
						icon: 'none'
					});
				}
			})
        },
        // 选择快递公司
        showSinglePicker() {
            this.show = true;
            this.mode = 'selector';
            this.deepLength = 1;
            this.pickerValueDefault = [0];
            this.$nextTick(()=>{
                this.$refs.mpvuePicker.show();
            })
        },
        onConfirm(e) {
            this.company = e.label;
            this.show = false;
            this.express_id = this.kuaidiList[e.index[0]].id;
            this.logistics_type=this.kuaidiList[e.index[0]].logistics_type
        },  
    },
    components: {
        mpvuePicker
    }
};
</script>
<style>
	    page{
	        background-color: #f4f5f6;
	    }
</style>
<style scoped lang="less">
@import url("@/laike.less");
.container {
    min-height: 100vh;
    background-color: #f4f4f4;
    padding-bottom: 98rpx;
    box-sizing: border-box;
}

.list {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    flex-direction: column;
}

.list_li {
    width: 100%;
    align-items: center;
    background-color: #ffffff;
    
    padding: 32rpx;
    box-sizing: border-box;
    border-radius: 16rpx;
    margin-bottom: 16rpx;
    
}

.list_li .list_li_l{
    display: flex;
}
.list1 .list_li{
    &:nth-child(1){
        border-radius: 0 0 16rpx 16rpx;
    }
}
.list1 .list_li{
    display: flex;
}
.list .list_li:not(:last-child) {
}

.list_li .image {
    width: 152rpx;
    height: 152rpx;
    border-radius: 16rpx;
    margin-right: 24rpx;
}

.xuanze,
.xuanze_i {
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    margin-right: 32rpx;
}

.xuanze_i {
    box-sizing: border-box;
    border: 1px solid #b8b8b8;
}

.list_li .right {
    flex: 1;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-self: flex-start;
}

.list_li .right .view {
    color: #333333;
    font-size: 32rpx;
    margin-bottom: 24rpx;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
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
.list_li .right .text {
    display: inline-block;
    color: #999999;
    font-size: 22rpx;
    line-height: 22rpx;
    font-weight: 400;
}

.bottom {
    display: flex;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 98rpx;
}

.bottom .xuanze,
.bottom .xuanze_i {
    margin-right: 12rpx;
}

.bottom > view:first-child {
    flex: 1;
    display: flex;
    align-items: center;
    padding: 0 30rpx;
    background: #ffffff;
}

.bottom > view:last-child {
    .solidBtn();
    width: 240rpx;
    height: 100%;
    font-size: 30rpx;
    line-height: 98rpx;
    text-align: center;
}

.jiantou {
    width: 32rpx;
    height: 44rpx;
}

._ul {
    background-color: #ffffff;
    padding: 32rpx;
    border-radius:0 0 24rpx 24rpx;
    margin-bottom: 24rpx;
}

._li2{
    height: 184rpx;
    background: #F4F5F6;
    border-radius: 16rpx;
    padding: 16rpx;
    font-size: 24rpx;
    color:#666666;
    line-height: 42rpx;
}
._li {
    display: flex;
    align-items: center;
    height: 90rpx;
    
    box-sizing: border-box;
}

._ul > ._li:not(:last-child) {
    margin-bottom: 24rpx;
}

._li text {
   font-size: 32rpx;
   
   font-weight: 400;
   color: #333333;
}

._li ._input {
    margin-left: 60rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex: 1;
    padding: 16rpx;
    font-size: 32rpx;
    
    font-weight: 400;
    color: #333333;
    border-radius: 16rpx;
    border: 1rpx solid rgba(0,0,0,0.1);
}
._ul > ._li:first-of-type {
    ._input{
        position: relative;
        view{
            position: absolute;
            width: 100%;
            height: 60rpx;
            z-index: 999;
        }
    } 
}

.bottom_ship {
    .solidBtn();
   
    margin: 0 32rpx;
    margin-top: 104rpx;
    border-radius: 52rpx;
    height: 92rpx;
    line-height: 92rpx;
    text-align: center;
    font-size: 32rpx;
    font-weight: 500;
}
.btn{
        position: fixed;
        bottom: 0;
        width: 100%;
        height: 124rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffffff;
        box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
    }
.bottomBtn {
    width: 686rpx;
    height: 92rpx;
    font-size: 32rpx;
    color: #fff;
    text-align: center;
    line-height: 92rpx;
    border-radius: 52rpx;
    margin: 0 auto;
    padding:0;
    .solidBtn()
}
.list_text{
    display: flex;
    justify-content: space-between;
    line-height: 56rpx;
    .list_num{
        width: 152rpx;
        border-radius: 26rpx;
        border: 1px solid rgba(0,0,0,0.1);
    }
}
</style>
