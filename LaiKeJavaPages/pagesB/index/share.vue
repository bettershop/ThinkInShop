<template>
    <div>
        <toload v-if="!guiderImg"></toload>
        <div v-else class="relative">
            <div class="guiderBtn" @tap="countDown(true)">
                <span class="time">{{ time }}</span>
                {{language.index.In}}
            </div>
            <swiper class="swiper" circular="false" :autoplay="autoplay" indicator-dots="true" interval="1000" @change="changeAutoplay">
                <swiper-item v-for="(item, index) in guiderImg" :key="index"><image class="image" :src="item.image"></image></swiper-item>
            </swiper>
        </div>
    </div>
</template>

<script>
import { getLaiketuiNoRegisterLoginInfo } from '../../common/laiketuiNoRegisterLogin.js'; 
export default {
    data() {
        return {
            title: 'Hello',
            guiderImg: [],
            time: 3,
            timer: '',
            timeStatu: false,
            autoplay: true,
            num: 1,
            clear: '',
            option: '',
            a_type:''
        };
    },
    onLoad(option,query) {  
        let stid = option.store_id;
        // #ifdef H5 
         this.LaiKeTuiCommon.initStoreID(stid);
        // #endif 
        // #ifdef MP-WEIXIN
        if(option.scene) {
            let data = {
                m: 'getCodeParameter',
                api: 'app.getcode.index',
                key: option.scene
            };
            
            this.$req.post({ data }).then(res => {
                if(!res.data.parameter) {
                    uni.showToast({
                        title: '分享链接已失效',
                        icon: 'none'
                    });
                    uni.redirectTo({
                        url: '/pages/shell/shell?pageType=home'
                    });
                } else {
                    uni.redirectTo({
                        url: `/pages/index/share?${res.data.parameter}`
                    });
                }
            });
            return
        }
        // #endif
        this.option = option;
        if(option.a_type){
            this.a_type = option.a_type
        }
        this._geturl().then(() => {
            this.getImg();
            uni.getSystemInfo({
                success: (res) => {
                    this.$store.state.data_height = res.statusBarHeight;
                }
            });
        });
        this.clear = setInterval(this.countDown, 1000);
    },
    onShow() {
        this.timeStatu = false;
        this.autoplay = true;
        this.time = 3;
    },
    methods: {
        changeAutoplay(e) {
            if (e.detail.current <= this.num) {
                this.autoplay = false;
            } else {
                this.num = e.detail.current;
                this.autoplay = true;
            }
        },
        getImg: function() {
            let data = {
                api: 'app.index.guided_graph',
                // #ifdef MP-WEIXIN || MP-ALIPAY
                guideType: 1,
                // #endif
                // #ifndef MP-WEIXIN
                guideType: 2
                // #endif
            };
            
            
            this.$req.post({data}).then(res=>{
                 this.guiderImg = res.list;
            })
        },
        // 倒计时
        countDown() {
            if (this.time-- == 1) {
                let param = ''
                if(this.a_type == 1){
                    param = 'a_type=1'
                }else{
                    param = ''
                }
                
                this.timeStatu = true;
                clearInterval(this.clear);
                uni.setStorageSync('isfx', true);
                var isfx = uni.getStorageSync('isfx');
                
                if (this.option.pages == 'goodsDetailed') {
                    if (this.option.fatherId != '' || this.option.fatherId) { 
                        if (this.option.isDistribution) {
                            uni.redirectTo({
                                url: '/pagesC/goods/goodsDetailed?productId=' + this.option.productId + '&isDistribution=true&isfx=true&fatherId=' + this.option.fatherId + '&fx_id=' + this.option.fx_id+ "&store_id=" + this.option.store_id
                            });
                        } else {
                            uni.redirectTo({
                                url: '/pagesC/goods/goodsDetailed?productId=' + this.option.productId + '&isfx=true&fatherId=' + this.option.fatherId + "&store_id=" + this.option.store_id
                            });    
                        }  
                        return false;
                    } 
                    uni.redirectTo({
                        url: '/pagesC/goods/goodsDetailed?productId=' + this.option.productId + '&isfx=true'+ "&store_id=" + this.option.store_id
                    });
                    
                    return false;
                } else if (this.option.pages == 'groupDetailed') {
                    uni.setStorageSync('pages', 'groupDetailed');
                    uni.setStorageSync('activity_no', this.option.activity_no);
                    uni.setStorageSync('pro_id', this.option.pro_id);

                    uni.redirectTo({
                        url: '/pages/shell/shell?pageType=home?pages=groupDetailed&pro_id=' + this.option.pro_id + '&activity_no=' + this.option.activity_no + '&isfx=true&' + param+ "&store_id=" + this.option.store_id
                    });
                    
                    return false;
                } else if (this.option.pages == 'group_end') {
                    uni.setStorageSync('pages', 'group_end');
                    uni.setStorageSync('sNo', this.option.sNo);
                    uni.setStorageSync('friend', true);

                    uni.redirectTo({
                        url: '/pages/shell/shell?pageType=home?pages=group_end&sNo=' + this.option.sNo + '&friend=true&isfx=true&' + param + "&store_id=" + this.option.store_id
                    });
                    
                    return false;
                } else if (this.option.pages == 'preGoodsDetailed') {
                    uni.redirectTo({
                        url: '/pagesC/preSale/goods/goodsDetailed?toback=true&pro_id=' + this.option.productId + "&store_id=" + this.option.store_id
                    });  
                    return false;
                }
                
                
            }
        },
        _geturl: function() {
            return new Promise((resolve, reject) => {
                var data = {
                    api: 'app.url.geturl',
                    get: 'mini_url,H5,endurl'
                };
                
                this.$req.post({data}).then(res=>{
					
					let store_type;
					// #ifdef APP-PLUS
					store_type = 11
					// #endif
					// #ifdef MP-ALIPAY
					store_type = 3
					// #endif
					// #ifdef MP-BAIDU
					store_type = 5
					// #endif
					// #ifdef MP-TOUTIAO
					store_type = 4
					// #endif
					// #ifdef MP-WEIXIN
					store_type = 1
					// #endif
					// #ifdef H5
					store_type = 2
					// #endif
					
					this.$store.state.h5_url = this.LaiKeTuiCommon.LKT_H5_DEFURL;
					this.$store.state.endurl = this.LaiKeTuiCommon.LKT_ENDURL;
					
					let api_wx_exturl = ""; 
                    //#ifdef MP-WEIXIN
                    let extConfig = uni.getExtConfigSync? uni.getExtConfigSync(): {};
					
                    if (extConfig.url) {
                        api_wx_exturl = extConfig.url;
                    }
                    api_wx_exturl = api_wx_exturl + '&store_type='+store_type;
                    //获取是否免密码登录开关
                    if (!uni.getStorageSync('needRegister')) {
                        getLaiketuiNoRegisterLoginInfo(1);
                    }
                    //#endif 
                    api_wx_exturl = api_wx_exturl + '&store_type='+store_type;
                    
                    uni.setStorageSync('url', api_wx_exturl);
                    uni.setStorageSync('h5_url', this.$store.state.h5_url);
                    uni.setStorageSync('endurl', this.$store.state.endurl);
                    resolve(this);
                })
            });
        }
    }
};
</script>

<style lang="less">
@import url('../../static/css/index/index.less');
</style>
