<template>
    <div class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.shop.coupon.title" :bgColor='bgColor' :ishead_w="ishead_w" titleColor="#333333" ></heads>
        <div class="tabBox">
            <div class="tabBox_box">
                <div :class="{active: type==1}" @tap="changeTab(1)">{{language.shop.coupon.coupon[0]}}</div>
                <div :class="{active: type==2}" @tap="changeTab(2)">{{language.shop.coupon.coupon[1]}}</div>
            </div>
        </div>
        <!-- 搜索 -->
        <search
            :is_search_prompt="'请输入店铺名称'"
            :is_search_type="1"
            @search="_seart"
            v-if="type==2"
        >
        </search>
        <div v-if="loopStatu" class="mycoupon_bottom" @tap="_mycoupon()">
            <image :src="coupon_icon"style="width: 40rpx;height: 36rpx;margin-right: 12rpx;"></image>
            <span style="color: #333333;font-size: 32rpx;">{{language.shop.coupon.coupon[2]}}</span>
            </div>
        <div class="skeleton" v-if="!load">
            <ul class="coupon_ul">
                <li class="coupon_li" v-for="item of 3" :key="item">
                    <img src="" alt="" class="bg_img skeleton-rect">
                </li>
                
            </ul>
        </div>
        <view class="jianju"></view>
        <view style="width: 100%;height: 32rpx;border-radius:24rpx 24rpx 0 0 ;background-color: #fff;"></view>
        <div v-if="list" class="list_box">
            <ul class="coupon_ul">
                <li class="coupon_li" v-for="(item, index) in list" :class="{ active_background: isNone[index] }" :key="index">
                    <img class="bg_img" :src="isNone[index] ? coupon_bg1 : (item.point_type==4||item.point_type==2?coupon_bg2:coupon_bg)" />
                    <!-- <img class="coupon_status" :src="isNone[index] ? coupon_no : coupon_on" v-if="item.point == '' || item.point == '去使用' || isNone[index]" /> -->
                    <div class="coupon_li_data">
                        <div class="coupon_left" :class="{ active_border: isNone[index] }">
                            <p class="coupon_p" :style="isNone[index]?'color:#333333':'color:#FA5151'">{{ item.name }}</p>
                            <div class="coupon_price" :class="{ active_color: isNone[index] }">
                                <div class="coupon_price_div" v-if="item.activity_type == 2">
                                    <span class="coupon_price_span" :class="{ active_color: isNone[index] }">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
                                    <span class="coupon_price_money" :class="{ active_color: isNone[index] }">{{ LaiKeTuiCommon.formatPrice(item.money) }}</span>
                                    <span class="coupon_t" :class="{ active_color: isNone[index] }">{{ item.limit }}</span>
                                </div>
                                <div class="coupon_price_div color_ff3" v-else-if="item.activity_type == 3">
                                    <span class="coupon_price_money" :class="{ active_color: isNone[index] }">
                                        {{ item.discount }}
                                        <span class="font_28" :class="{ active_color: isNone[index] }">{{language.shop.coupon.fold}}</span>
                                    </span>
                                    <span class="coupon_t" :class="{ active_color: isNone[index] }">{{ item.limit }}</span>
                                </div>
                                <div class="color_ff3" v-else-if="item.activity_type == 1">
                                    <span class="coupon_t ml_0" :class="{ active_color: isNone[index] }">{{ item.limit }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="coupon_right" v-if="item.point!= language.shop.coupon.points[2]">
                            <button v-if="item.point_type==1"
                                type="button"
                                class="coupon_but"
                                @tap="_login(index, item.id, item.point, item.url)"
                                :class="{ coupon_no: isNone[index], coupon_red: item.point == language.shop.coupon.points[3], coupon_red: item.point == language.shop.coupon.points[0] }"
                            >
                                <span ref="point">{{ item.point == language.shop.coupon.points[0]?language.shop.coupon.points[0]:item.point == ''?language.shop.coupon.points[1]:item.point == ''?language.shop.coupon.points[2]:language.shop.coupon.points[3] }}</span>
                            </button>
                        </div>
                        
                    </div>
                </li>
                
                <uni-load-more v-if="list&&list.length>8" :loadingType="loadingType"></uni-load-more>
            </ul>
            
            
            <div v-if="list && list.length == 0" class="relative">
                <div class="noFindDiv">
                    <div><img :src="noCoupon" style="width: 750rpx;" class="noFindImg" alt="" /></div>
                    <span class="noFindText">{{language.shop.coupon.coupon[3]}}~</span>
                </div>
            </div>
        </div>
        <!-- 提示 -->
        <view class="tishi" style="z-index: 999; position: fixed;top: 50%;margin-top: -46rpx;left: 50%;margin-left: -128rpx;" v-if="is_tishi">
            <view style="min-width: 256rpx;height: 92rpx;background-color: rgba(0, 0, 0, .8);border-radius: 48rpx;color: #ffffff;display: flex;align-items: center;justify-content: center;">
                <span style="font-size: 16px;font-weight: 400;">{{is_tishi_content}}</span>
            </view>
        </view>

        <skeleton  :animation="true" :loading="!load"  bgColor="#FFF"></skeleton>
    </div>
</template>

<script>
import skeleton from "@/components/skeleton";
import search from "@/components/aComponents/search.vue";
export default {
    data() {
        return {
            returnR: '',
            back: '',
            title: '领券中心',
            //提示
            is_tishi:false,
            is_tishi_content:'领取成功！',
            load: false,
            list: false,
            receive_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/lingqu2x.png',
            noreceive_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/qiangguang2x.png',
            coupon_bg1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coupon_js.png',
            back_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x.png',
            huiquan_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/huiquan2x.png',
            coupon_on: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coupon/coupon_on.png',
            coupon_no: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/coupon_no.png',
            coupon_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/coupon_icon.png',
            coupon_bg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/coupon_hybj.png',
            coupon_bg2: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/coupon_bg2.png',
            fastTap: true,
            frompage: '',
            noCoupon:  this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_k.png',
            loopStatu: true,
            isNone: [] ,//是否是已抢光状态，true已抢光
            ishead_w:'2',
            bgColor: [{
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            type: 1,
            page: 1,
            loadingType: 0
        };
    },
    components: {
        search,
        skeleton
    },
    onReachBottom() {
        if(this.loadingType!=0){
            return
        }
        this.loadingType = 1
        this.page++
        this._axios()
    },
    onLoad(option) {
        if (option.loop) {
            this.loopStatu = false;
        }
    },
    onShow() {
        this.$nextTick(() => {
            if (!this.list) {
                this.load = false
            }
            this.page = 1
            this._axios();
        });
    },
    methods: {
        _seart(val){
            this._axios(val)
        },
        changeTab(type){
            this.type = type
            this.page = 1
            this._axios()
        },
        
        // 去我的优惠券
        _mycoupon() {
            this.isLogin(()=>{
				uni.navigateTo({
				    url: '/pagesB/coupon/mycoupon?loop=false'
				});
			})
        },
        _axios(mchName) {
            let data = {
                api:"plugin.coupon.Appcoupon.Index",
                page: this.page,
                pageSize:'99999',
                type: this.type
            };
            if(mchName){
                data.mchName = mchName
            }
            this.$req.post({ data }).then(res => {
                if(res.code == 203){
                   
                    return;
                }
                
                
                if((this.page == 1 && this.type == 1) || (this.page == 1 && this.type == 2)){
                    this.list = []
                    this.isNone = []
                }
                let { list } = res.data;
                list.filter(item=>{
                    if(item.limit){
                        item.limit = item.limit.replace('满','满'+this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL)
                    }
                })
                var newlist = []
                for(var i = 0;i<list.length;i++){
                    if(list[i].point_type==1){
                        newlist.push(list[i])
                    }
                }
				//立即了领取
                for(var i = 0;i<list.length;i++){
                    if(list[i].point_type==2||list[i].point_type==4){
                        newlist.push(list[i])
                    }
                }
				//已领取
				for(var i = 0;i<list.length;i++){
					if(list[i].point_type==3){
						newlist.push(list[i])
					}
				}
				//已抢光
                
                
                list = newlist
                this.list.push(...list);
                this.fastTap = true;
                if (list && list.length) {
                    let arr = [];
                    for (let i = 0; i < list.length; i++) {
                        arr.push(false);
                        if (list[i].point === '已抢光') {
                            arr[i] = true;
                        }
                    }
                    this.isNone.push(...arr);
                }
                this.load = true
                
                if(list && list.length>0){
                    this.loadingType = 0
                }else{
                    this.loadingType = 2
                }
            });
        },
        _login(index, id, point, url){
            this.isLogin(() =>{
                this._receive(index, id, point, url)
            })
        },
        // 点击优惠券右边的按钮
        _receive(index, id, point, url) {
            if (point === '立即领取') {
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;
                
                
                let data = {
                    
                    api:"plugin.coupon.Appcoupon.Receive",
                    id: id
                };

                this.$req.post({ data }).then(res => {
                    let { code } = res;
                    if (code == 200) {
                        this.is_tishi_content = this.language.shop.coupon.Tips[0]
                        this.is_tishi = true
                        setTimeout(()=>{
                            this.is_tishi = false
                        },1000)
                        
                        
                        setTimeout(() => {
                            this.page = 1
                            this._axios();
                        }, 1500);
                    } else if (code === 203) {
                        uni.showToast({
                            title: '请登录',
                            duration: 1500,
                            icon: 'none'
                        });
                        
                        setTimeout(function() {
                            uni.navigateTo({
                                url: '/pagesD/login/newLogin?landing_code=coupon'
                            });
                        }, 1500);
                    } else {
                        setTimeout(() => {
                            this._axios()
                        }, 1500);
                    }
                });
            } else if (point === '去使用') {
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;
                if (!url) {
                    url = '/pages/shell/shell?pageType=home'
                }
				if(this.type == 2){
					uni.navigateTo({
					    url: '/pagesB/store/store?shop_id='+this.list[index].mch_id
					});
				}else if (url.indexOf('tabBar') > 0) {
                    uni.redirectTo({
                        url: url
                    });
                } else {
                    uni.navigateTo({
                        url: url
                    });
                }
            }
        }
    }
};
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped> 
@import url("@/laike.less");
@import url('../../static/css/shop/coupon.less');
.noFindDiv{
    padding: 0;
}
/deep/.is_search{
    padding-top: 12rpx !important;
}
/deep/.search_btn{
    border: initial !important;
    background-color: rgba(250, 81, 81, .1) !important;
    color: #FA5151 !important;
}
</style>
