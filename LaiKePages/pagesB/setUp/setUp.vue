<template>
    <div class="box">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.setUp.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <div class="load" v-if="load">
            <div>
                <img :src="loadImg" />
                <p>{{language.addStock.load}}……</p>
            </div>
        </div>
        <div v-else>
            <ul class="setup-ul">
                <li @tap="_uni_navigateTo('changePhone')" v-if="1">
                    <!-- 设置手机号 -->
                    <text v-if="!isphone">{{language.setUp.changePhone}}</text>
                    <!-- 修改手机号 -->
                    <text v-else>{{language.setUp.editPhone}}</text>
                    <image class="arrow" :src="jiantou" />
                </li>
                <li @tap="_uni_navigateTo('loginPassword')" v-if="1">
                    <!-- 设置登录密码 -->
                    <text v-if="havePass1">{{language.setUp.changePswd}}</text>
                    <!-- 修改登录密码 -->
                    <text v-else>{{language.setUp.bindPswd}}</text>
                    <image class="arrow" :src="jiantou" />
                </li>
                <!-- 设置支付密码 -->
                <li v-if="!havePass" @tap="laikeNavigateTo('setPaypswd', 'payment')">
                    <text>{{language.setUp.bindPaypswd}}</text>
                    <image class="arrow" :src="jiantou" />
                </li>
                <!-- 修改支付密码 -->
                <li v-else @tap="laikeNavigateTo('changePaypswd', 'paymentPassword')">
                    <text>{{language.setUp.changePaypswd}}</text>
                    <image class="arrow" :src="jiantou" />
                </li>
            </ul>
            <ul class="setup-ul" style="margin-top: 22rpx;border-radius: 24rpx;">
                <!-- 关于我们 -->
                <li @tap="_navigateTo('/pagesC/my/aboutMe')">
                    <text>{{language.my.aboutUs}}</text>
                    <image class="jiantou" :src="jiantou"></image>
                </li>
                <!-- 切换语言 -->
                <li @tap="_chooseLangth">
                    <text>{{language.my.languages}}</text>
                    <image class="jiantou" :src="jiantou"></image>
                </li>
                <!-- 切换语言弹窗 -->
                <chooseS 
                    ref="isLanguage" 
                    :is_choose_obj='is_choose_obj' 
                    :is_type="chooseType" 
                    :is_choose="languages"
                    :datas="currencys"
                     @_choose="_choose" 
                     @_isHide="_isHide">
                </chooseS>
                
                <!-- 切换货币 -->
                <li @tap="openCurrency">
                    <text>{{language.my.currency}}</text>
                    <image class="jiantou" :src="jiantou"></image>
                </li>

                <!-- 切换语言弹窗 -->
                <chooseS
                    ref="chooseCurrencys"
                    :is_choose_obj='is_choose_obj'
                    :is_type="chooseType_currency"
                    :is_choose="languages"
                    :datas="currencys"
                    @_choose="_choose" 
                    @_isHide="_isHide">
                </chooseS>
                
            </ul>
            <!-- 啥需求！微信小程序也要退出功能 -->
            <div class="anniu" v-if="1">
                <div class="button margin" @tap="quit">{{language.setUp.bottomBtn}}</div>
                <div class="safe-area-inset-bottom"></div>
            </div>
        </div>
    </div>
</template>

<script>
import chooseS from "@/components/aComponents/choose.vue" 
import { mapMutations } from 'vuex';

export default {
    data() {
        return {
            languages:[],
            currencys:[],
            my_abouts: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/my_abouts.png',
            my_kefu: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/my_kefu.png',
            languageImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/language.png',
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            isLangth: ['中文-简体','English'],//
            chooseType: 0,//选择语言
            chooseType_currency: 0,//选择货币
            name: '',
            id: '',
            user_id: '', // 授权id
            size: '',
            number_s: '',
            lktauthflag: false, // 是否为授权登录
            bindPhone: '修改手机号',
            showSetPayPswdFlag: true, // 显示修改支付密码和设置密码
            load: true,
            loadImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/loading.gif',
            bgColor:[{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
            is_choose_obj:{
                selected_lang_value:'zh_CN',
                selected_currency_value:'￥',
                title: '切换语言',
                title_currency: '切换货币',
                colorLeft: '#999999',
                colorRight: '#FA5151',
                background: '#F4F5F6',//显示图标
                borderRadius: '24rpx 24rpx 0 0',//提示文字
            },
            isphone: false,//用户是否有 绑定手机号
            havePass1: false,//户是否有 登录密码
            havePass: false,//户是否有设置 支付密码
            havaMobile:false
        };
    },
    components:{
        chooseS,
    },
    async onLoad() {
        
        //获取用户手机号
        if(uni.getStorageSync('user_phone') && uni.getStorageSync('user_phone') != ''){
            this.isphone = true
        }
        
        //语言
        let result = await this.LaiKeTuiCommon.getLanguages(); 
        this.languages = (result.data || []).map(item => ({
            ...item,
            lang_name: this.formatLangName(item)
        }));
        
        
        //货币
        let result1 = await this.LaiKeTuiCommon.getCurrencys();  
        this.currencys = result1.data;
        
    },
    onShow() {
        if(uni.getStorageSync('diqu')){
        	this.diqu = JSON.parse(uni.getStorageSync('diqu'))
        } 
        if(uni.getStorageSync('havaMobile')){
            this.havaMobile = uni.getStorageSync('havaMobile')
        }
        
        this.load = true;
        this.bindPhone = this.language.setUp.changePhone
        this.is_choose_obj.title=this.language.my.languages 
        this.is_choose_obj.title_currency=this.language.my.currency 
        
        this.isLogin(()=>{  
            this._axios();
			let needRegister = uni.getStorageSync('needRegister');
			let LoingByHand = uni.getStorageSync('LoingByHand');
			
			this.lktauthflag = !LoingByHand && needRegister == this.LaiKeTuiCommon.LKT_NRL_TYPE.NRL;
			if (this.lktauthflag) {
			    this.bindPhone = this.language.setUp.bindPhone;
			}
		})
        
        let currency = uni.getStorageSync("currency");
        if(currency){
            this.is_choose_obj.selected_currency_value = currency.currency_symbol;
        } else {
            this.is_choose_obj.selected_currency_value = '￥';
        }
        
        this.is_choose_obj.selected_lang_value = uni.getStorageSync("language");
        
    },
    methods: {
        //选择语言
        _chooseLangth(){
            this.chooseType = 4
        },
        openCurrency(){
            this.chooseType = 5
        },
        //选择的语言
        _choose(item,type){ 
            if(type == "currency")
            {
                this.changeCurrency(item)
            }
            else
            {
                this.changeLang(item)
            }
        },
        
        //隐藏
        _isHide(){
            this.chooseType = 0
        },
        // 客服
        toKeFu() {
            // #ifndef MP-WEIXIN ||  MP-TOUTIAO || MP-BAIDU
            this._navigateTo('/pagesB/message/service')
            // #endif
        },
        //关于我们
        _navigateTo(url) {
            uni.navigateTo({
                url: url
            });
        },
        //选择货币
        changeCurrency(item) {
            this.chooseType = 0;
            let currency_symbol = item.currency_symbol; 
            uni.setStorageSync('currency', item)
            this.is_choose_obj.title_currency= this.language.my.currency
            
            this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = currency_symbol;
            //汇率
            this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE = item.exchange_rate
            
            //修改用户个人选择的货币登录时设置本地缓存币种符号 app.index.changeCurrency -> app.common.userChangeCurrency
            let data = {api:'app.common.userChangeCurrency',currency_id:item.currency_id}
            this.$req.post({
                data
            }).then(res => {
                let {
                    code,
                    message
                } = res
                // uni.setStorageSync('storeCurrency', item)
                uni.reLaunch({
                    url:'/pages/shell/shell?pageType=home'
                })
            })
        },
        //选择语言
        changeLang(item) {
            this.chooseType = 0;
            let oldlang = uni.getStorageSync('language')
            let langcode = item.lang_code;
            uni.setStorageSync('language', langcode)
            uni.setStorageSync('lang_code', langcode)

            this.is_choose_obj.title=this.language.my.languages
            let data = {
                api: 'app.index.select_language',
            }
            this.$req.post({
                data
            }).then(res => {
                let {
                    code,
                    message
                } = res 
                if (code != 200) {
                    if (oldlang == 'en_US') {
                        uni.setStorageSync('language', 'en_US')
                        uni.setStorageSync('lang_code', 'en_US')
                    } else {
                        uni.removeStorageSync('language');
                        uni.removeStorageSync('lang_code');
                    }
                    this.setLang();
                    const toastMessage = this.$t ? this.$t(message, message) : message;
                    uni.showToast({
                        title: toastMessage,
                        icon:'none'
                    })
                    return
                }

                this.updateUrlLanguage(langcode);
                    
                uni.setStorageSync('setLangFlag', true)
            
                uni.reLaunch({
                    url:'/pages/shell/shell?pageType=home'
                })
                this.is_choose_obj.title=this.language.my.languages
                this.is_choose_obj.title_currency=this.language.my.currency 
                
            })
        },
        formatLangName(item) {
            if (!item) {
                return '';
            }
            const rawName = (item.lang_name || '').trim();
            if (rawName) {
                const translated = this.$t ? this.$t(rawName, '') : '';
                if (translated && translated !== rawName) {
                    return translated;
                }
                if (!this.isI18nKey(rawName)) {
                    return rawName;
                }
            }
            const code = (item.lang_code || '').trim();
            return code || rawName || '';
        },
        isI18nKey(value) {
            return typeof value === 'string' && value.indexOf('.') > -1;
        },
        ...mapMutations({ 
            user_phone: 'SET_USER_PHONE'
        }),
        /**
         *
         * */
        changeLoginStatus() {
            let needRegister = uni.getStorageSync('needRegister');
            let LoingByHand = uni.getStorageSync('LoingByHand');
            
            this.lktauthflag = !LoingByHand && needRegister == this.LaiKeTuiCommon.LKT_NRL_TYPE.NRL;
            if (this.lktauthflag) {
                this.bindPhone = '绑定手机号';
            }
            this._axios();
        },
        /**
         * @param {Object} type 类型  设置支付密码setPaypswd   修改支付密码changePaypswd
         * @param {Object} url  跳转页面
         */
        laikeNavigateTo(type, url) {
            let havaPayPwd  = uni.getStorageSync('havaPayPwd')
            if(havaPayPwd && this.havaMobile.toString() != 'true'){
               uni.showToast({
                   title: this.language.setUp.noBind,
                   duration: 1000,
                   icon: 'none'
               });
               return; 
            }
            let needRegister = uni.getStorageSync('needRegister');
            let LoingByHand = uni.getStorageSync('LoingByHand');
            this.lktauthflag = !LoingByHand && needRegister == this.LaiKeTuiCommon.LKT_NRL_TYPE.NRL;
            let phone = uni.getStorageSync('user_phone') ? uni.getStorageSync('user_phone') : this.$store.state.user_phone || '';
            // if (this.lktauthflag && !phone) {
            //     uni.showToast({
            //         title: this.language.setUp.noBind,
            //         duration: 1000,
            //         icon: 'none'
            //     });
            //     return;
            // }
            uni.navigateTo({
                url: url + '?havePass=' + this.havePass1+'&type=true'
            });
        },
        /**
         *
         * */
        _axios() {
            let data = {
                api: 'app.user.set',};
            this.$req.post({data}).then(res => {
                let needRegister = uni.getStorageSync('needRegister');
                let LoingByHand = uni.getStorageSync('LoingByHand');
                let {
                    data: {
                        password_status,
                        mima_status
                    }
                } = res
                if (res.code == 200) {
                    if (password_status == 1) {
                        this.havePass = true;
                    } else if (password_status == 0) {
                        this.havePass = false;
                    }
                    if (mima_status == 1) {
                        this.havePass1 = true;
                    } else if (mima_status == 0) {
                        this.havePass1 = false;
                    }

                    this.lktauthflag = !LoingByHand && needRegister == this.LaiKeTuiCommon.LKT_NRL_TYPE.NRL;
                    this.load = false;
                }
            });
        },
        /**
         *
         * */
        _uni_navigateTo(url) {
            uni.navigateTo({
                url: url + '?havePass=' + this.havePass1
            });
        },
        /**
         *
         * */
        quit() {
            uni.removeStorage({
                key: 'history'
            });

            uni.removeStorage({
                key: 'user_phone'
            });

            uni.removeStorage({
                key: 'hotStatu'
            });
            uni.removeStorageSync('user_phone')
            this.$store.state.cart = [];
            this.$store.state.cart_id = [];
            this.$store.state.nCart = [];
            this.$store.state.shouquan = false;

            uni.removeStorageSync('userinfo');
            uni.removeStorageSync('laiketuiAccessId');
            this.user_phone('')
            uni.removeStorageSync('online');
            uni.removeStorageSync('LoingByHand');
            
            uni.removeStorageSync('lang_code');
            // uni.removeStorageSync('language');
            
            
            uni.removeStorageSync('currency');
            
            uni.removeStorageSync('setLangFlag')
            let data = {
                api: 'app.login.quit',
                
            };

            this.$req
                .post({
                    data
                })
                .then(res => {
                    this.$store.state.access_id = '';
                    uni.setStorageSync('isHomeShow',1)

                    uni.removeStorage({
                        key: 'access_id'
                    });
 
                    uni.navigateTo({
                        url:'/pages/shell/shell?pageType=home'
                    })
                     
                    let storeCurrency = uni.getStorageSync('storeCurrency');
                    if(storeCurrency)
                    {
                        //商城默认货币
                        this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = storeCurrency.currency_symbol
                        this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE = storeCurrency.exchange_rate;
                    }
                    else
                    {
                        //默认货币符号
                        this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = '￥'
                        this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE = 1;
                    }
                    
                });
        }
    }
};
</script>
<style>
    page{
        height: 100vh;
        background-color: #F4F5F6;
    }
</style>

<style lang="less" scoped> 
@import url("@/laike.less");
@import url('../../static/css/setUp/setUp.less');
</style>
