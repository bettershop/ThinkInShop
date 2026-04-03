<template>
    <div style='display: flex;flex-direction: column;min-height: 100vh;background-color: #F4F5F6;'  @touchmove.prevent="handleTouchMove">
        <div class="content">
            <!--登录页面-->
            <div :style="{position:'relative',top:baiduHeadTop + 'px'}">
                <!-- 头部 -->
                <heads :title="language.login.page2.topBtn" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
                <!-- 导航 -->
                <view class="tabbar">
                    <view @click="_phone" :class="tabbar_isShow==1?'tabbar_chos':''">
                        {{language.login.yzmdl}}
                        <view class="tabbar_xian" v-if="tabbar_isShow==1"></view>
                    </view>
                    <view @click="_landing_passw" :class="tabbar_isShow==2?'tabbar_chos':''">
                        {{language.login.mmdl}}
                        <view class="tabbar_xian" v-if="tabbar_isShow==2"></view>
                    </view>
                </view>
                <switchNavTwo ref="switchNavTwo" :is_switchLable="header_label" @choose="_header_label_index"></switchNavTwo>
                <!--密码登录--> 
                <template v-if="landing"> 
                    <div class='login'>
                        <div class='login_inpu qu-box' style="margin-top: 48rpx;" v-if="isSwitchLable==0">
                            <view class="qu" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
                                <view>{{diqu.code}} +{{diqu.code2}} </view>
                                <view class="san-jiao"></view>
                            </view>
                            <input 
                                type="number"
                                :placeholder="language.login.page2.tel_placeHolder" 
                                placeholder-style="color:#999999;"
                                v-model="phone"
                                @focus="_pone_f"
                                @blur="_telephone(phone,2)"
                                @input='_codeChangePhone'/>
                            <img v-if="phone.length" :src="del" @tap="_empty(3)"/>
                        </div>
                        <div class='login_inpu' style="margin-top: 48rpx;" v-else>
                            <img :src="account.length?del1x:del1" class="login_inpu_img"/>
                            <input 
                                type="text"
                                style="width: 71%;" 
                                :placeholder="language.login.page1.zhanghao_placeHolder" 
                                placeholder-style="color:#999999;"
                                v-model='account'
                                @blur="_noNull(1)"/>
                            <img :src="del" v-if="account.length" @tap="_empty(1)"/>
                        </div>
                        
                        <div class='login_inpu'>
                            <img :src="password.length?del2x:del2" class="login_inpu_img"/>
                            <input 
                                class='PWS-input'
                                type="text" 
                                :password="LoginPWStatus" 
                                :placeholder="language.login.page1.mima_placeHolder" 
                                placeholder-style="color:#999999;"
                                v-model='password'
                                @blur='_noNull(1)'/>
                            <img style='height: 40rpx;width: 40rpx;' class='PWS-img' :src="LoginPWStatus?pwHide:pwShow" @tap="pwStatus(1)"/>
                        </div>
                        <template v-if="landing">
                            <p class='login_pass'>
                                <span @tap='_navigateTo("retrievepassword")'>
                                    {{language.login.page1.forgot}}？
                                </span>
                            </p>
                        </template>
                        <!-- v-if='pwLoginBtnStatus' -->
                        <template >
                            <div class='button1' style='margin-top: 70rpx;' @tap="_landing">
                                {{language.login.page1.topBtn}}
                            </div>
                        </template>
                      <!--  <template v-else>
                            <div class='button1' style='opacity: 0.4;margin-top: 70rpx;'>
                                {{language.login.page1.topBtn}}
                            </div>
                        </template> -->
                        <div class='button2' @tap="_register_q()">
                            {{language.login.zc}}
                        </div>
                    </div>
                </template> 
                <!--验证码登录-->
                <template v-if="!landing">
                    <div class='login'>
                            <!-- 手机号验证 -->
                            <template v-if="isSwitchLable==0">
                                <div class='login_inpu qu-box' style="margin-top: 48rpx;">
                                    <view class="qu" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
                                        <view>{{diqu.code}} +{{diqu.code2}} </view>
                                        <view class="san-jiao"></view>
                                    </view>
                                    <input 
                                        type="number"
                                        :placeholder="language.login.page2.tel_placeHolder" 
                                        placeholder-style="color:#999999;"
                                        v-model="phone"
                                        @focus="_pone_f"
                                        @blur="_telephone(phone,2)"
                                        @input='_codeChangePhone'/>
                                    <img v-if="phone.length" :src="del" @tap="_empty(3)"/>
                                </div>
                                <div class='login_inpu' style='margin-bottom: 100rpx;'>
                                    <div class="codeBox">
                                        <input 
                                            type="number"
                                            maxlength="6"
                                            :placeholder="language.login.page2.code_placeHolder"
                                            placeholder-style="color:#999999;" 
                                            style="margin-bottom: 0;border: 0;flex: 1;color: #333333;"
                                            v-model="phone_code"
                                            @input='_codeChangePhone'
                                            @focus='_codeF()'
                                            @blur='_codeB()'/>
                                        <p class='login_p' :style="{color:time_code!='获取验证码'?'#666666':''}" @tap="_phone_code(1)">
                                            {{time_code}}
                                        </p>
                                    </div>
                                </div>
                            </template>
                            <!-- 邮箱验证 -->
                            <template v-else>
                                <div class='login_inpu qu-box' style="margin-top: 48rpx;">
                                    <input 
                                        type="text"
                                        :placeholder="language.login.page2.tel_EmailHolder" 
                                        placeholder-style="color:#999999;"
                                        v-model="emailText"
                                        @focus="_pone_f" 
                                        @input='_codeChangeEmail' />
                                    <img v-if="emailText.length" :src="del" @tap="_empty(6)"/>
                                </div>
                                <div class='login_inpu' style='margin-bottom: 100rpx;'>
                                    <div class="codeBox">
                                        <input 
                                            type="number"
                                            maxlength="6"
                                            :placeholder="language.login.page2.code_placeHolder"
                                            placeholder-style="color:#999999;" 
                                            style="margin-bottom: 0;border: 0;flex: 1;color: #333333;"
                                            v-model="phone_code"
                                            @input='_codeChangeCode'
                                            @focus='_codeF()'
                                            @blur='_codeB()'/>
                                        <p class='login_p' :style="{color:time_code!='获取验证码'?'#666666':''}" @tap="getAutByEmail()">
                                            {{time_code}}
                                        </p>
                                    </div>
                                </div>
                            </template>
                            <!-- v-if='codeLoginBtnStatus' -->
                        <template >
                            <div class='button1' style='margin-top: 70rpx;' @tap="_landing">
                                {{language.login.page2.topBtn}}
                            </div>
                        </template>
                       <!-- <template v-else>
                            <div class='button1' style='opacity: 0.4;margin-top: 70rpx;'>
                                {{language.login.page2.topBtn}}
                            </div>
                        </template> -->
                        <div class='button2' @tap="_register_q()">
                            {{language.login.zc}}
                        </div>
                    </div>
                </template>
            </div>
        </div>
        
        <!-- 其他登录方式 隐藏 快捷登录方式被隐藏了 要先使用 快捷登录 使用 login_cope 文件 -->
   
        <!-- 协议弹窗 -->
        <view class="xieyi" v-if="is_tc">
            <view>
                <view class="xieyi_title">{{is_tc_tit}}</view>
                <view class="xieyi_text">
                    <toload :load="load">
                        <rich-text class="richtext" :nodes="contentNodes" style="font-size: 14px;"></rich-text>
                    </toload>
                </view>
                <view class="xieyi_btm" @click="_xieyiShow('tc')">{{language.login.wzdl}}</view>
            </view>
        </view>
        
        <!-- 提示弹窗 -->
        <view class="xieyi" v-if="is_ts">
            <view>
                <view class="xieyi_title" style="margin-bottom: 64rpx;">{{is_ts_tit}}</view>
                <view class="xieyi_btm" @click="_xieyiShow('ts')">{{language.login.wzdl}}</view>
            </view>
        </view>
        
        <!-- 提示 -->
        <showToast
            :is_showToast="is_showToast" 
            :is_showToast_obj="is_showToast_obj">
        </showToast>
        <chooseS
            ref="isLanguage" 
            :is_choose_obj='is_choose_obj' 
            :is_type="chooseType"   
             @_choose="bindCouponChange" 
        	 :is_choose="couponList"
             @_isHide="_isHide">
        </chooseS>
    </div>
</template>

<script>
    import chooseS from "@/components/aComponents/choose.vue"
    import htmlParser from '@/common/html-parser.js'
    import showToast from "@/components/aComponents/showToast.vue"
    import switchNavTwo from "@/components/aComponents/switchNavTwo.vue";
    import {
        telephone
    } from '../../common/landing.js'
    import {
        mapMutations
    } from 'vuex'
    import {
        lkt_pwStatus,
        lkt_telephone,
        lkt_phone_code,
    } from '../../static/js/login/login.js'

    export default {
        data () {
            return {
                diqu:{
                	code:'CN',
                	code2:'86',
                    num3: 156
                },
                wayText:'手机号验证',
                chooseType:0,
                is_choose_obj:{
                    title: '',
                    colorLeft: '#999999',
                    colorRight: '#FA5151',
                    background: '#F4F5F6',//显示图标
                    borderRadius: '24rpx 24rpx 0 0',//提示文字
                },
                couponList:[
                	'手机号验证',
                	'邮箱验证',
                ],
                header_label:[],
                emailText:'',
                //弹窗
                is_tc: false,
                is_tc_tit:'',
                //提示
                is_ts: false,
                is_ts_tit:'',
                //
                content: '',
                contentNodes: [],
                load: false,
                loadImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/loading.gif',
                //弹窗
                tabbar_isShow: 2,
                toHome: false,
                togoodsDetail: false,
                phone_codeStatus1: false,
                LoginPWStatus: true,
                pwLoginBtnStatus: false,
                codeLoginBtnStatus: false,
                fastTap: true,
                del: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete1x.png',
                del1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/del1.png',
                del1x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/del1x.png',
                del2: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/del2.png',
                del2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/del2x.png',
                wx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wx.png',
                zfb: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zfb.png',
                guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
                pwShow: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwHide.png',
                pwHide: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwShow.png',
                bg_white: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/bg_white.png',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                passLoginCodePH: '请输入账号/手机号',
                passLoginCodePH1: '请输入账号/手机号',
                passLoginPWPH: '请输入密码',
                passLoginPWPH1: '请输入密码',
                codeLoginCodePH: '请输入手机号',
                codeLoginCodePH1: '请输入手机号',
                codeLoginPWPH: '请输入验证码',
                codeLoginPWPH1: '请输入验证码',
                account: '', //登录账号
                password: '', //登录密码
                landing: true, //切换密码登录和手机号码登录
                phone: '', //验证码登录手机号
                phone_code: '', //验证码
                one_code: '', //手机号码格式正确返回值
                time_code: '获取验证码',
                timer: null,
                count: '', //倒计时时间
                old_phone: '', //存储获取验证码时的手机号码
                landing_code: '',
                passone: '',
                passtwo: '',
                account_f: '',
                pone_f: '',
                provider: '',
                agreement1: '',
                agreement2:'',
                company: '',
                logo: '',
                src: false,
                fatherId: '',//父级id(分销商分享使用)
                baiduHeadTop: 0,// 百度小程序头部兼容
                bgColor:[{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                showWxLogin: true,
                showAliLogin: true,
                is_showToast: 0,//
                is_showToast_obj: {},//
                getSystemHeight:'',//终端初始可用屏幕高度
                keyboardHeight: 0, //默认键盘高度
                SystemType: '',//获取操作系统类型（PC-H5/iOS-H5/Android-H5/iOS-MP/Android-MP/iOS-APP/Android-APP）
                isSwitchLable:0
            }
        },
        components:{
            showToast,
            chooseS,
            switchNavTwo
        },
        created() {
            //获取 终端初始可用屏幕高度
            this.getSystemHeight = uni.getStorageSync('getSystemHeight').value
            //获取 操作系统类型
            this.SystemType = uni.getStorageSync('getSystemType').value
            //获取 键盘高度
            this.keyboardHeight = uni.getStorageSync('keyboardHeight')?uni.getStorageSync('keyboardHeight').value:0
            // #ifdef H5
                if(this.SystemType == 'Android-H5'){
                    //Android键盘弹起会触发页面的resize 事件（ios不会）
                    window.addEventListener('resize', () => {
                        uni.getSystemInfo({
                            success: (res) => {
                                if(res.windowHeight<this.getSystemHeight){
                                    this.keyboardHeight = this.getSystemHeight - res.windowHeight
                                    uni.setStorageSync('keyboardHeight', {name:'手机键盘高度',value:this.keyboardHeight})
                                }
                            }
                        })
                    })
                }
            // #endif
           this.header_label=[
                this.language.putForward.phone,
                this.language.putForward.emailOraccount
            ]
        },
        
        onShow(){
            this.time_code = this.language.login.page2.getCode
            // #ifdef APP
            let args = plus.runtime.arguments;
            let me = this;
            if (args) { 
                plus.runtime.arguments=null;
                this.zfb_authCode = args.split("=")[1];  
                let auth_code = this.zfb_authCode;
                if(auth_code) {
                    var data = {
                        api: 'app.login.aliUserLoginApp',
                        store_type: 2,
                        authCode: auth_code,
                    };
                    this.$req.post({
                        data
                    }).then(res => {
                        if (!(res && res.code == 200 && res.data && res.data.userInfo)) {
                            return uni.showToast({
                                title: (res && res.message) || '登录失败，请稍后重试',
                                duration: 1500,
                                icon: 'none'
                            })
                        }
                        uni.setStorageSync("login_key",0)
                        me.$store.state.access_id = []
                        uni.setStorageSync('isHomeShow',1)
                        me.set_access_id(res.data.userInfo.access_id)
                        uni.setStorage({
                            key: 'access_id',
                            data: res.data.userInfo.access_id,
                            success: function () {
                        
                            }
                        })
                        
                        uni.setStorage({
                            key: 'user_id',
                            data: res.data.userInfo.user_id
                        })
                        uni.redirectTo({
                            url:'/pages/shell/shell?pageType=my&auth_code='+auth_code
                        });
                    }).catch(() => {
                        uni.showToast({
                            title: '网络异常，请稍后重试',
                            duration: 1500,
                            icon: 'none'
                        })
                    })
                        
                }
            } 
            // #endif
            if(uni.getStorageSync('diqu')){
            	this.diqu = JSON.parse(uni.getStorageSync('diqu'))
            } 
        },
        watch:{
            phone(val){
                if(val.length >0 && this.phone_code.length == 6){
                    this.codeLoginBtnStatus = true
                }
            },
            phone_code(val){
                if(val.length == 6 && this.phone.length >0){
                    this.codeLoginBtnStatus = true
                }
            },
            password() {
                if(this.account !== '' && this.password !== '') {
                    this.pwLoginBtnStatus = true
                }
            }
        },
        onUnload(){
            //很多地方都要用到用户手机号，登陆不一定会跳转个人中心页面。分享进入登陆后会返回分享页面，就没办法获取手机号
            //登陆接口为什么不传手机号？得重新调个人中心的app.user.index接口？这样好多地方都要调这样的接口。
            //只能在登陆页面关闭前调用这个接口，并存缓存
            let data = {
                api: 'app.user.index',};
            this.$req.post({data}).then(res => {
                if (res && res.code == 200 && res.data && res.data.data && res.data.data.user) {
                    this.$store.state.user_phone = res.data.data.user.mobile;
                    uni.setStorage({
                        key: 'user_phone',
                        data: res.data.data.user.mobile
                    });
                }
            }).catch(() => {})
        	uni.removeStorageSync('diqu')
        },
        onLoad (option) {
            // #ifdef H5
            let ua = navigator.userAgent.toLowerCase();
            if (ua.match(/MicroMessenger/i) != "micromessenger"){
                this.showWxLogin = false
            } else {
                this.showAliLogin = false
            }
            // #endif
            uni.removeStorageSync('signFlag');
            // #ifdef MP-BAIDU
            // 百度小程序头部兼容
            uni.getSystemInfo({
                success: (res) => {
                    this.baiduHeadTop = res.navigationBarHeight
                }
            })
            // #endif
            let data = {
                api: 'app.login.index',}
            this.$req.post({data}).then(res => {
                if (res && res.code == 200 && res.data) {
                    this.agreement1 = res.data.Agreement
                    this.agreement2 = res.data.Agreement_1
                    this.logo = res.data.logo
                    this.company = res.data.company
                }
            }).catch(() => {})

            // 获取服务供应商
            uni.getProvider({
                service: 'oauth',
                success: res => {
                    this.provider = res.provider
                }
            })
            
            //判断landing_code是否存在，存在则是登录验证未通过进入，登录成功后需要返回上一页
            if (option.landing_code) {
                this.togoodsDetail = option.landing_code
            }
            
            //获取、绑定上级id
            if (option.fatherId) {
                this.fatherId = option.fatherId
            }

            this.toHome = option.toHome;

            if (uni.getStorageSync('fatherId')) {
                this.fatherId = uni.getStorageSync('fatherId')
            }
        },
        methods: {
             handleTouchMove(e) {
                  // 阻止默认的触摸移动行为
                  e.preventDefault();
            },
            /**
             * 导航栏切换
             */
            _header_label_index(index) {
                
                 this.isSwitchLable = index
                 // 清空数据
                 this.password = ''
                 this.account = ''
                 this.phone = ''
                 this.phone_code = ''
                 this.emailText = ''
                
            },
            // 切换验证方式
            selectionMode(){
            	this.is_choose_obj.title = '请选择验证方式'
            	this.chooseType = 2
            },
            //隐藏
            _isHide(){
                this.chooseType = 0
            },
            bindCouponChange(item){
            	this.wayText= item ? '邮箱验证':'手机号验证' 
            	this._isHide()
            },
            aliLogin() {               
                let data = {
                    api: 'app.login.aliUserLoginByWeb',
                    // #ifdef APP-PLUS
                    store_type: 11,
                    // #endif
                    // #ifdef H5
                    store_type: 2
                    // #endif
                }
                this.$req.post({data}).then(res => {
                    let urls = res.data.url
                    //前端写死url   （H5测试使用。APP不能用，APP跳转的是中间页）
                    //JAVA  app_id=2021001171664275     redirect_uri=https://java.houjiemeishi.com/#/pages/shell/shell?pageType=my
                    //let url = 'https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2021001171664275&scope=auth_user&redirect_uri=https://java.houjiemeishi.com/#/pages/shell/shell?pageType=my'
                    //PHP   app_id=2019030763497116     redirect_uri=https://seo.houjiemeishi.com/H5/#/pages/shell/shell?pageType=my
                    //let url = 'https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2019030763497116&scope=auth_user&redirect_uri=https://seo.houjiemeishi.com/H5/#/pages/shell/shell?pageType=my'
                    //url = encodeURIComponent(url)  //这里需要转码
                    //let urls = 'alipays://platformapi/startapp?appId=20000067&url=' + url
                    // #ifdef APP-PLUS
                    //要配置za.html 中间页面
                    plus.runtime.openURL(urls, err => {
                        uni.showToast({
                            title: '打开支付宝失败！请检查是否已安装？',
                            icon: 'none' 
                        });
                    },'com.eg.android.AlipayGphone');
                    // #endif
                    // #ifdef H5
                    window.location.href = urls
                    // #endif                   
                })
            },
            
            //关闭协议弹窗
            _xieyiShow(e){
                if(e == 'tc'){
                    this.is_tc = !this.is_tc
                } else {
                    this.is_ts = !this.is_ts
                }
            },
            //用户协议
            _yhxy(e){
                this.is_tc = !this.is_tc
                this.is_tc_tit = e
                
                this.setLang();
                var me = this
                let data = {
                    api: 'app.login.register_agreement',
                }
                this.$req.post({data}).then(res => {
                    if (res.data.content) {
                        //吧view标签替换为html的P标签
                        me.content = '<div style=\'padding-left:8px;padding-right:8px;color: #999999;font-size: 16px;\'>' + res.data.content.replace(new RegExp('<view', 'gm'), '<p').replace(new RegExp('</view>', 'gm'), '</p>') + '</div>'
                        var htmlString = me.content.replace(/\\/g, '').replace(/<img/g, '<img style="display:none;"')
                        htmlString = htmlString.replace('<div style=\'padding-left:8px;padding-right:8px;\'>', '<div style="padding: 25px 15px;">')
                        me.contentNodes = htmlParser(htmlString)
                        // #ifndef MP-ALIPAY
                        setTimeout(function () {
                            me.load = true
                        }, 50)
                        // #endif
                        // #ifdef MP-ALIPAY
                        me.load = true
                        // #endif
                    }  
                })
            },
            //隐私协议
            _ysxy(e){
                this.is_tc = !this.is_tc
                this.is_tc_tit = e
                
                var me = this
                let data = {
                    api: 'app.login.privacy_agreement',
                }
                this.$req.post({data}).then(res => {
                    if (res.data.content) {
                        //吧view标签替换为html的P标签
                        me.content = '<div style=\'padding-left:8px;padding-right:8px;color: #999999;font-size: 16px;\'>' + res.data.content.replace(new RegExp('<view', 'gm'), '<p').replace(new RegExp('</view>', 'gm'), '</p>') + '</div>'
                        var htmlString = me.content.replace(/\\/g, '').replace(/<img/g, '<img style="display:none;"')
                        htmlString = htmlString.replace('<div style=\'padding-left:8px;padding-right:8px;\'>', '<div style="padding: 25px 15px;">')
                        me.contentNodes = htmlParser(htmlString)
                        // #ifndef MP-ALIPAY
                        setTimeout(function () {
                            me.load = true
                        }, 50)
                        // #endif
                        // #ifdef MP-ALIPAY
                        me.load = true
                        // #endif
                    }else{
                        me.load = true
                    }
                })
            },
            ...mapMutations({
                set_access_id: 'SET_ACCESS_ID',
                user_phone: 'SET_USER_PHONE'
            }),
            close(){
                const pages = getCurrentPages()
                if(pages[pages.length-2].route == 'pagesB/setUp/setUp'){
                    uni.redirectTo({
                        url: "/pages/shell/shell?pageType=my"
                    })
                    return;
                }
                this.navBack()
            },
            //to注册页面
            _register_q () {
                this.account = ''
                this.password = ''
                this.phone = ''
                this.phone_code = ''
                this.pwLoginBtnStatus = false
                this.codeLoginBtnStatus = false
                this.phone_codeStatus1 = false
                clearInterval(this.timer)
                this.time_code = this.language.login.page2.getCode
                this.timer = null
                this.count = ''

                if (this.fatherId) {
                    uni.navigateTo({
                        url: '/pagesD/login/register?fatherId=' + this.fatherId + '&landing_code=' + this.togoodsDetail
                    })
                } else {
                    uni.navigateTo({
                        url: '/pagesD/login/register?landing_code=' + this.togoodsDetail
                    })
                }

            },
            //账号、密码输入框失焦 判断账号不为空
            _noNull (type) {
                var me = this

                function in_noNull () {
                    if (type == 1 && me.account && me.password) {
                        me.passLoginCodePH = me.passLoginCodePH1
                        me.one_code = 1
                        me.account_f = true
                        me.pwLoginBtnStatus = true
                    } else {
                        me.pwLoginBtnStatus = false
                    }
                }

                me.$nextTick(() => {
                    in_noNull()
                })
            },
            //叉，清空内容 1登录账号 3验证码登录手机号 245没用到 6 邮箱
            _empty (val) {
                
                if (val == 1) {
                    this.account = ''
                    this.pwLoginBtnStatus = false
                } else if (val == 2) {
                    this.password = ''
                } else if (val == 3) {
                    this.phone = ''
                } else if (val == 4) {
                    this.passone = ''
                } else if (val == 5) {
                    this.passtwo = ''
                } else if(val == 6){
                    this.emailText = ''
                }
            },
            // 密码是否可见 1登录密码 2注册密码 3再次输入注册密码
            pwStatus (type) {
                lkt_pwStatus(type, this)
            },
            //登录
            async _landing (e) {
                var me = this 
                // 验证码登录
                if(!this.landing){
                    this.loginByCode()
                    return
                } 
                
                if ((this.isSwitchLable == 0 ? !this.phone : !this.account) || !this.password) {
                    const text ={
                        0:'nopassword', //手机号判断
                        1:'emailPassword' //邮箱判断
                    }
                    // 非空判断
                    uni.showToast({
                        title: this.language.login[text[this.isSwitchLable]],
                        duration: 1000,
                        icon: 'none'
                    })
                } else if ((this.phone || this.account) && this.password) {
                    let data = {
                        api: 'app.login.login',
                        phone: this.account,
                        password: this.password,
                        access_id: this.$store.state.access_id,
                        clientid: uni.getStorageSync('cid'),
                        type:this.isSwitchLable+1 //1：手机号 2：账号/邮箱
                    }
                    if(this.isSwitchLable == 0){
                        data.cpc= this.diqu.code2 // 国家/地区
                        data.country_num= this.diqu.num3  // 国家代码
                        data.phone= this.phone // 手机号码
                    }
                    
                    if (this.fatherId != '') {
                        data.pid = this.fatherId//分销推荐人id
                    }
                    
                    await this.$req.post({data}).then(res => {
                        
                        if (res.code == 200) {
                            let {
                                access_id,
                                wx_status,
                                user_id,
                                sellOrderNum
                            } = res.data                         
                            // 首页弹窗数据
                            uni.setStorageSync("isxufei",true)
                            uni.setStorageSync("myPopup",sellOrderNum) 
                            uni.setStorageSync("login_key",0)
                            
                            me.$store.state.access_id = []
                            // 缓存公告
                            uni.setStorageSync("laike_move_uaerInfo", res.data)
                            // 是否提示公告弹窗
                            if((uni.getStorageSync('laike_move_uaerInfo').systemMsgType == 2||uni.getStorageSync('laike_move_uaerInfo').systemMsgType == 3) && uni.getStorageSync('laike_move_uaerInfo').type != 0) {
                                uni.setStorageSync('versionUpdate3',true)
                            }
                            //缓存userinfo，小程序也可以密码登录，分享有问题.
                            let userinfo = uni.getStorageSync('userinfo') || '{}'
                            if(typeof userinfo == 'string'){
                                userinfo = JSON.parse(userinfo)
                            }
                            userinfo.user = res.data
                            uni.setStorageSync('userinfo', userinfo)
                            
                            //商城默认币种不可能为空
                            let storeCurrency = userinfo.user.storeCurrency;
                            uni.setStorageSync('storeCurrency',storeCurrency);
                            uni.setStorageSync('language',userinfo.user.lang);
                            uni.setStorageSync('lang_code',userinfo.user.lang);
                            
                            //个人选择的默认币种 可能为空
                            let preferred_currency = userinfo.user.preferred_currency
                            
                            this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE = storeCurrency.exchange_rate;
                            
                            if(!preferred_currency)
                            {
                                uni.setStorageSync('preferred_currency',storeCurrency.id);
                                uni.setStorageSync('currency',storeCurrency);
                                this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = storeCurrency.currency_symbol
                            }
                            else
                            {
                                uni.setStorageSync('preferred_currency',userinfo.user.preferred_currency);
                                uni.setStorageSync('currency',userinfo.user.userCurrency);
                                this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = userinfo.user.userCurrency.currency_symbol
                            }


                            me.is_showToast = 1
                            me.is_showToast_obj.imgUrl = me.sus
                            me.is_showToast_obj.title = me.language.login.loginSuc
                            setTimeout(()=>{
                                me.is_showToast = 0
                            },2000) 
                            
                            uni.setStorageSync('isHomeShow',1)

                            me.set_access_id(access_id)
                            uni.setStorage({
                                key: 'access_id',
                                data: access_id,
                                success: function () {

                                }
                            })

                            uni.setStorage({
                                key: 'user_id',
                                data: user_id
                            })

                            //免注册登录下用账户或手机号登录
                            uni.setStorageSync('LoingByHand', true)
                            if (me.togoodsDetail) {

                                if (getCurrentPages().length > 1) {
                                    setTimeout(function () {
                                        uni.navigateBack({
                                            delta: 1
                                        })
                                    }, 1000)
                                } else {
                                    uni.redirectTo({
                                        url: '/pages/shell/shell?pageType=my',
                                        success: function () {}
                                    })
                                }

                            } else {

                                setTimeout(function () {


                                    uni.redirectTo({
                                        url: '/pages/shell/shell?pageType=my',
                                        success: function () {
                                            if (wx_status != 1) {
                                                me.$store.state.shouquan = true
                                            }
                                        }
                                    })
                                }, 1000)
                            }
                        } else {
                            uni.showToast({
                                title: res.message,
                                duration: 1000,
                                icon: 'none'
                            })
                        }    
                        return
                    })

                }  
            },
            // 验证码登录
            async loginByCode(){
                var me =this 
                if(this.isSwitchLable == 0){                 
                    // 校验手机号
                    if(!this.phone || this.phone.length == 0){
                        uni.showToast({
                            title: this.language.register.tel_placeHolder,
                            icon:'none'
                        })
                        return
                    }
                }else{ 
                    // 邮箱非空校验
                    if(!this.emailText || this.emailText.length == 0){
                        uni.showToast({
                            title: this.language.login.page2.tel_EmailHolder,
                            icon:'none'
                        })
                        return
                    }
                }
                // 校验验证码
                if(!this.phone_code || this.phone_code.length == 0){
                    uni.showToast({
                        title: this.language.login.yzmEmpty,
                        icon:'none'
                    })
                    return
                } 
                
                const data ={ 
                    api: 'app.register.login',
                    // type: 0, // 类型 0.手机号 1.邮箱
                    type:this.isSwitchLable+1, //1：手机号 2：账号/邮箱 
                    keyCode:this.phone_code, // 验证码
                    isLogin:1
                }
                
                if(this.isSwitchLable==0){
                    data.cpc= this.diqu.code2 // 国家/地区
                    data.country_num=this.diqu.num3  // 国家代码
                    data.phone=this.phone  // 手机号码
                }else{
                    data.phone= this.emailText // 邮箱
                }
                if (this.fatherId != '') {
                    data.pid = this.fatherId
                }
                
                await this.$req.post({data}).then(res => {
                    if (res.code == 200 && res.data && res.data.access_id) {
                        let {
                            access_id,
                            y_password,
                            user_id,
                            wx_status
                        } = res.data
                      
                        me.$store.state.access_id = []
                        uni.showToast({
                            title: this.language.login.loginSuc,
                            duration: 1000,
                            icon: 'none'
                        })
                        uni.setStorageSync("isxufei",true)
                        uni.setStorageSync('isHomeShow',1)
                        
                        me.set_access_id(access_id)
                        uni.setStorageSync('access_id', access_id)
                        uni.setStorageSync('user_phone', me.phone)
                        uni.setStorageSync('user_id', user_id)
                        // 缓存公告
                        uni.setStorageSync("laike_move_uaerInfo", res.data)
                        // 是否提示公告弹窗
                        if((uni.getStorageSync('laike_move_uaerInfo').systemMsgType == 2||uni.getStorageSync('laike_move_uaerInfo').systemMsgType == 3) && uni.getStorageSync('laike_move_uaerInfo').type != 0) {
                            uni.setStorageSync('versionUpdate3',true)
                        }

                        //商城默认币种不可能为空
                        let storeCurrency = userinfo.user.storeCurrency;
                        uni.setStorageSync('storeCurrency',storeCurrency);
                        uni.setStorageSync('language',userinfo.user.lang);
                        uni.setStorageSync('lang_code',userinfo.user.lang);
                        //个人选择的默认币种 可能为空
                        let preferred_currency = userinfo.user.preferred_currency
                        
                        this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE = storeCurrency.exchange_rate;
                        
                        if(!preferred_currency)
                        {
                            uni.setStorageSync('preferred_currency',storeCurrency.id);
                            uni.setStorageSync('currency',storeCurrency);
                            this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = storeCurrency.currency_symbol
                        }
                        else
                        {
                            uni.setStorageSync('preferred_currency',userinfo.user.preferred_currency);
                            uni.setStorageSync('currency',userinfo.user.userCurrency);
                            this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = userinfo.user.userCurrency.currency_symbol
                        }
                        
                        //免注册登录下用账户或手机号登录
                        uni.setStorageSync('LoingByHand', true)
                        // 是否设置了密码
                        if (y_password == 0) {
                            setTimeout(function () {
                                uni.reLaunch({
                                    url: '/pages/shell/shell?pageType=my',
                                    success: function () {
                                        if (wx_status != 1) {
                                            me.$store.state.shouquan = true
                                        }
                                    }
                                })
                            }, 1000) 
                        } else {
                            setTimeout(function () {
                                uni.reLaunch({
                                    url: '/pages/shell/shell?pageType=my',
                                    success: function () {
                                        if (wx_status != 1) {
                                            me.$store.state.shouquan = true
                                        }
                                    }
                                })
                            }, 1000)
                        }
                    } else {
                        this.is_ts = true
                        if(res.message == '验证码不正确'){
                            this.is_ts_tit = '验证码输入错误，请重新输入。'
                        } else {
                            this.is_ts_tit = res.message
                        }
                       
                    }  
                })  
            },
            // 验证码登录 手机号聚焦
            _pone_f () {
                this.codeLoginCodePH = ''
                this.pone_f = true
            },
            async getPhoneNumber(e) {
            
                if (!e.detail.encryptedData) {
                    return false;
                }
            
                let openid = uni.getStorageSync('userinfo').openid
            
                let {
                    data: phoneNumber
                } = await this.$req.post({
                    data: {
                        openid: openid,
                        encryptedData: e.detail.encryptedData,
                        iv: e.detail.iv,
                        api: 'app.login.getWxInfo',
                    }
                });
                let mobile_bind = phoneNumber.phoneNumber;
            },
            // 验证码登录 手机号输入
            _codeChangePhone: function (e) {
                if (e.target.value.length > 0 ) {
                    this.codeLoginBtnStatus = true
                } else {
                    this.codeLoginBtnStatus = false
                }
            },
            // 邮箱方式验证码
            getAutByEmail(){
                if(this.emailText && this.emailText.length == 0){
                    uni.showToast({
                        title:this.language.login.page2.tel_EmailHolder,
                        icon:'none'
                    })
                    return
                }
                const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g;
                 if(!regex.test(this.emailText)){
                     uni.showToast({
                         title:this.language.openInvoice.qsrzqddzyx,
                         icon:'none'
                     }) 
                     return
                 }
                
                const data ={
                    api:'app.User.send_email_verification_code',
                    email:this.emailText
                }
                this.$req.post({data}).then(res => {
                    if(res.code == 200){
                        uni.showToast({
                           title:this.language.login.page2.sendCode,
                           icon:'none'
                        })
                        //验证码倒计时
                        const TIME_COUNT = 60
                        if(uni.getStorageSync('language') == 'en'){
                            this.time_code = this.language.login.page2.countdown + ' ' + TIME_COUNT + `s`
                        }else{
                            this.time_code = TIME_COUNT + `s` + this.language.login.page2.countdown
                        }
                        if (!this.timer) {
                            this.count = TIME_COUNT
                            this.timer = setInterval(() => {
                                if (this.count > 0 && this.count <= TIME_COUNT) {
                                    this.count--
                                    if(uni.getStorageSync('language') == 'en'){
                                        this.time_code = this.language.login.page2.countdown + ' ' + this.count + `s`
                                    }else{
                                        this.time_code = this.count + `s` + this.language.login.page2.countdown
                                    }
                                } else {
                                    clearInterval(this.timer)
                                    this.time_code = this.language.login.page2.getCode
                                    this.timer = null
                                    this.count = ''
                                }
                            }, 1000)
                        }
                    }else{
                        uni.showToast({
                           title:res.message,
                           icon:'none'
                        })
                    }
                })
            },
            _codeChangeEmail(e){
                const regularText = /^\w+@\w+.(com|cn|net|de|lk|nz|tw)/g
                const falg = regularText.test(this.emailText)
                if(falg){
                    this.codeLoginBtnStatus = true
                } else {
                    this.codeLoginBtnStatus = false
                }
            },
            //手机号码正则验证 type2验证码登录输入手机号，3注册输入手机号
            _telephone (value, type) {
                // this.one_code = telephone(value)
                this.one_code =true
                // lkt_telephone(type, this)
                
                if(this.phone_code){
                    this.codeLoginBtnStatus = true
                }
            },
            // 验证码登录 验证码聚焦
            _codeF () {
                this.codeLoginPWPH = ''
            },
            // 验证码登录 验证码输入
            _codeChangeCode: function (e) {
                if (e.target.value.length == 6 && this.phone.length >0) {
                    this.codeLoginBtnStatus = true
                } else {
                    this.codeLoginBtnStatus = false
                }
            },
            // 验证码登录 验证码失焦
            _codeB () {
                this.codeLoginPWPH = this.codeLoginPWPH1
            },
            //获取验证码 type1验证码登录 2注册
            _phone_code (type) {
                lkt_phone_code(type, this)
            },
            //密码登录to验证码登录
            _phone () {
                this.landing = false 
                this.codeLoginBtnStatus = false
                this.phone_codeStatus1 = false
                this.$refs.switchNavTwo.isSwitchLable = 0
                this.isSwitchLable= 0 
                this.phone = ''
                this.account = ''
                this.password = ''
                this.tabbar_isShow = 1
                this.header_label=[
                     this.language.putForward.phone,
                     this.language.myinfo.email
                 ]
            },
            //验证码登录to密码登录
            _landing_passw () {
                this.pwLoginBtnStatus = false
                this.landing = true
                this.$refs.switchNavTwo.isSwitchLable = 0
                this.isSwitchLable= 0
                this.phone = ''
                this.phone_code = ''
                this.tabbar_isShow = 2
                
                this.header_label=[
                     this.language.putForward.phone,
                     this.language.putForward.emailOraccount
                 ] 
            },
            _navigateTo (url) {
                uni.navigateTo({
                    url
                })
            }
        },
        computed: {
            halfLoginIosWidth () {
                var gru = uni.getStorageSync('data_height') ? uni.getStorageSync('data_height') : this.$store.state.data_height
                var heigh = parseInt(gru)
                var he = heigh * 2
                // #ifdef MP-TOUTIAO
                const info = uni.getSystemInfoSync()
                if (info.platform != 'ios') {
                    he = 0
                }
                // #endif
                return uni.upx2px(he) + 'px'
            },
        },
    }
</script>

<style scoped lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/login/login.less");
    .login{
        padding: 0 32rpx;
    }
    .uni-modal{
        width: 640rpx;
    }
    .PWS-input{
        width: 580rpx;
        margin-left: 100rpx; 
        padding-right:44rpx;
        // text-indent: 20rpx;
    }
    .PWS-img{ 
           z-index: 99;
           background-color: #fff;
    }
    .qu{
    	width: 260rpx;
        margin-left: 32rpx;
    	display: flex;
        align-items: center;
    	height: 100rpx;
    	img{
    		padding :0px
    	}
    }
</style>
