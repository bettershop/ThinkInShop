<template>
    <view v-show="isTranslated" >
        <home v-if="activeIndex === 0" ref='tabBarPaes' />
        <allClass v-if="activeIndex === 1" ref='tabBarPaes' />
        <cart v-if="activeIndex === 2" ref='tabBarPaes' :cartHeads="cartHeads" />
        <profile v-if="activeIndex === 3" ref='tabBarPaes'/>

        <!-- 自定义TabBar -->
        <LkT-tab-bar :activeColor="'#007AFF'" ref="lktTabbar" :backgroundColor="'#FFFFFF'"
                     @tabChange="handleTabChange"></LkT-tab-bar>
    </view>
</template>

<script>
import translateSafe from '@/mixins/translateSafe.js'
import home from '@/rootMain/home.vue'
import allClass from '@/rootMain/allClass.vue'
import cart from '@/rootMain/shoppingCart.vue'
import profile from '@/rootMain/my.vue'
import LkTTabBar from '@/components/laiketuiauth/LkTTabBar/LkTTabBar.vue'

export default {
    mixins: [translateSafe],
    data() {
        return {
            activeIndex: 0,
            cartHeads: {
                headBg: 'transparent',
                backgroundColor: 'transparent'
            }
        }
    },
    onShow(){ 
        // 触发当前 子组件 Mounted 钩子逻辑
        this.syncMallTitle()
        this.triggerPage()
    },
    onLoad(option) {
        if (option.pageType) {
            this.getPgaeInfo(option.pageType)
        } else {
            this.activeIndex = 0
        }
        this.syncMallTitle()
        this.shareEvent(option)
        this.getShow()
    },
    methods: {
        handleTabChange(e) {
            this.activeIndex = e.index
            this.triggerPage()
           this.upMainUrl( this.activeIndex )
        },
        upMainUrl(index){
            const list = ['home','allClass','shoppingCart','my']
            uni.redirectTo({
                url: `/pages/shell/shell?pageType=${list[index]}`
            })
        },
        getShow() {
            //先请求diy，获取是否使用diy首页
            let data = {
                api: "app.index.hasDiy",
            };
            this.$req.post({
                data
            }).then((res) => {
                if(!res.data){
                    this._isDiy();
                }
            });
        },
        // 没有配置diy时
        _isDiy() {
            const tabbarList = [{
                "pagePath": "pages/tabBar/home",
                "iconPath": require('@/static/img/homeWx.png'),
                "selectedIconPath": require('@/static/img/homeWxh.png'),
                "page_name": "首页"
            },{
                "pagePath": "pages/tabBar/allClass",
                "iconPath": require("@/static/img/goShoppingWx.png"),
                "selectedIconPath": require("@/static/img/goShoppingWxh.png"),
                "page_name": "全部分类"
            },{
                "pagePath": "pages/tabBar/shoppingCart",
                "iconPath": require("@/static/img/shoppingWx.png"),
                "selectedIconPath": require("@/static/img/shoppingWxh.png"),
                "page_name": "购物车"
            },{
                "pagePath": "pages/tabBar/my",
                "iconPath": require("@/static/img/myWx.png"),
                "selectedIconPath": require("@/static/img/myWxh.png"),
                "page_name": "个人中心"
            }
            ]
            uni.setStorageSync('tabbar_info',JSON.stringify( {
                fontIsShow:true,
                fontSize:'24rpx',
                optColor:'#000',
                color:'#333',
                iconIsShow :true,
                inconSiz:'48rpx',
            }))
            this.$store.commit('SET_TABBER', tabbarList)
            uni.setStorageSync('tabbar', JSON.stringify(tabbarList))
            uni.removeStorageSync('DIYPAGEINFO')
        },
        getPgaeInfo(pageType){
            try{
                const rootPageList =JSON.parse(uni.getStorageSync('tabbar') || '[]')
                // 页面
                let activeIndex = rootPageList.findIndex(v=>
                    (v.url || v.pagePath).includes(pageType)
                ) || 0
                if(activeIndex<0){
                    this.activeIndex = 0
                }else{
                    this.activeIndex = activeIndex
                }
                uni.setStorageSync('tabbarIndex',this.activeIndex )

            }catch(e){
                this. activeIndex = 0
                console.error(e)
            }

        },
        // 分享
        shareEvent(option){ 
            // #ifdef APP-PLUS
            let args = plus.runtime.arguments;
            if (args) {
                plus.runtime.arguments = null;
                this.zfb_authCode = args.split("=")[1];
                let auth_code = this.zfb_authCode;
                if (auth_code) {
                    var data = {
                        api: 'app.login.aliUserLoginApp',
                        store_type: 2,
                        authCode: auth_code
                    };
                    this.$req.post({
                        data
                    }).then(res => {
                        uni.setStorageSync("login_key", 0)
                        this.$store.state.access_id = []
                        uni.setStorageSync('isHomeShow', 1)
                        this.set_access_id(res.data.userInfo.access_id)
                        uni.setStorage({
                            key: 'access_id',
                            data: res.data.userInfo.access_id,
                            success: function() {

                            }
                        })
                        
                        uni.setStorage({
                            key: 'user_id',
                            data: res.data.userInfo.user_id
                        })
                        uni.redirectTo({
                            url: '/pages/shell/shell?pageType=my'
                        })
                    })
                }
            }
            // #endif
             
            if (option.scene) {
                let data = {
                    api: 'app.getcode.getCodeParameter',
                    key: option.scene
                };

                this.$req.post({
                    data
                }).then(res => {
                    let str = res.data.parameter
                    let fatherId = str.split('=')[1]
                    uni.setStorageSync('fatherId', fatherId)
                    if (!res.data.parameter) {
                        uni.showToast({
                            title: '分享链接已失效',
                            icon: 'none'
                        });
                        uni.redirectTo({
                            url: '/pages/shell/shell?pageType=home'
                        });
                    } else {
                        let parameter = res.data.parameter.split("&")
                        //重定向页面 并处理pages中的 .- 以 / 替代，在分享时替换了的，现在切换回去.
                        let pages = parameter[1].split("=")[1].replace(/\.\-/g,'/')
                        //重定向页面携带的参数
                        let states = '?'
                        parameter.forEach((item, index)=>{
                            if(index>1){
                                states = states + item + '&'
                            }
                        })
                        states = states.slice(0, -1)
                        //分享重定向url
                        let sharUrl = '/' + pages + states
                        uni.redirectTo({ url: sharUrl });
                    }
                });
                return
            }else{
                if (option&&option.pages) {
                    uni.setStorageSync('store_id',this.LaiKeTuiCommon.LKT_STORE_ID)
                 
                    //重定向页面 并处理pages中的 .- 以 / 替代，在分享时替换了的，现在切换回去.
                    let pages = option.pages.replace(/\.\-/g,'/')
                    //重定向页面携带的参数
                    let states = '?'
                    for (let i in option) {
                        if(i != 'share' && i != 'pages'){
                            states = states + i + "=" + option[i] + "&";
                        }
                    }
                    states = states.slice(0, -1)
                   
                    //分享重定向url
                    let sharUrl = '/' + pages + states
                    uni.redirectTo({ url: sharUrl });
                }
            }
        },
        triggerPage() {
            try {
                this.$nextTick(() => {
                    //用于触发 子组件 自定义 Mounted 逻辑
                    const ref = this.$refs.tabBarPaes
                    const target = Array.isArray(ref) ? ref[0] : ref
                    if (target && typeof target.triggerMounted === 'function') {
                        target.triggerMounted()
                    }
                })
            } catch (e) {
                console.error(e)
            }
        },
        // 购物车 滚动事件
        cartEvent(){
            try{
                let me = this
                let query = uni.createSelectorQuery().in(this);
                query.select('.order_ii').boundingClientRect(data => {
                    let cartHeadsObj ={}
                    if (data?.top < -5) {
                        cartHeadsObj.headBg = '#ffffff'
                        cartHeadsObj.backgroundColor = '#ffffff'
                    } else {
                        cartHeadsObj.headBg = 'transparent'
                        cartHeadsObj.backgroundColor = 'transparent'
                    }
                    this.cartHeads = cartHeadsObj
                }).exec();
            }catch(e){
                console.log(e)
            }

        },
        getSystemIconAndName() {
            let data = {
                api:"app.user.getSystemIconAndName",
            };
            this.$req.post({ data}).then((res) => {
                uni.setStorageSync('systemIcon', res.data);
                const link = document.createElement('link')
                link.rel = 'icon'
                link.href = res.data.html_icon
                document.head.appendChild(link)
                this.syncMallTitle()
            });
        },
        
    },
    components: { home, allClass, cart, profile, LkTTabBar }
}
</script>

<style>
</style>
