<template>
    <div style='display: flex;flex-direction: column;min-height: 100vh;background-color: #F4F5F6;'>
        <div class="content">
            <!--注册页面-->
            <div :style="{position:'relative',top:baiduHeadTop + 'px'}">
                <heads :title="language.register.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
                <view class="tabbar">
                    <view @click="_phone" :class="tabbar_isShow==1?'tabbar_chos':''">
                        手机号注册
                        <view class="tabbar_xian" v-if="tabbar_isShow==1"></view>
                    </view>
                    <view @click="_landing_passw" :class="tabbar_isShow==2?'tabbar_chos':''">
                        邮箱注册
                        <view class="tabbar_xian" v-if="tabbar_isShow==2"></view>
                    </view>
                </view>
                
                <!-- 手机号注册 -->
                <div class='login' v-if='tabbar_isShow == 1'>
                    <!-- 账号 -->
                  <!--  <div class='login_inpu' style="margin-top: 48rpx;">
                        <input placeholder-style="color:#999999; " type="text" :placeholder="language.register.zhanghao_placeHolder"
                               onkeypress="javascript:if(event.keyCode == 32)event.returnValue = false;"
                               @input="_regIdIpt" v-model="regId" @focus="_close_empty(1)" @blur="blurReg"/>
                        <img :src="del" v-if="regId.length" @tap="_empty(1)"/>
                    </div> --> 

                    <!-- 手机号 -->
                    <div class='login_inpu qu-box' style="margin-top: 48rpx;">
                        <view class="qu" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
                            <view>{{diqu.code}} +{{diqu.code2}} </view>
                            <view class="san-jiao"></view>
                        </view>
                        <input placeholder-style="color:#999999; " type="number" :placeholder="language.register.tel_placeHolder"
                               @input="_regPhoneIpt" v-model="phone" @focus="_close_empty(1)"
                               @blur="_telephone(phone,3)" />
                    </div>

                    <!-- 验证码 -->
                    <div class='login_inpu regCode'>
                        <input class='flex1' placeholder-style="color:#999999; " type="number" :placeholder="language.register.code_placeHolder"
                               @input="_regCodeIpt" v-model="phone_code" maxlength="6"/>
                        <p class='login_p1 login-inpu-p' :style="{color:time_code!='获取验证码'?'#666666':''}" @tap="_phone_code(2)">{{time_code}}</p>
                    </div>
                 
                    <!-- 密码 -->
                    <div class='login_inpu' >
                        <input placeholder-style="color:#999999; " type="text" :password="regPWStatus1"
                               @input="_regPW1Ipt" :placeholder="language.register.mima1_placeHolder" v-model="passone"
                               @focus="_close_empty(2)" @blur="_passone"/>
                        <img :src="regPWStatus1?pwHide:pwShow" class="login-inpu-img" @tap="pwStatus(2)"/>
                    </div>
                    
                    <!-- 再次输入密码 -->
                    <div class='login_inpu'>
                        <input placeholder-style="color:#999999; " type="text" :password="regPWStatus2"
                               @input='_regPW2Ipt' :placeholder="language.register.mima2_placeHolder" v-model="passtwo" @focus="_close_empty(3)"
                               @blur="_passtwo_t"/>
                        <img :src="regPWStatus2?pwHide:pwShow" class="login-inpu-img" @tap="pwStatus(3)"/>
                    </div>
                    
                </div>
                <!-- 邮箱注册 -->
                <div class='login' v-else>
                    <!-- 账号 -->
                    <div class='login_inpu' style="margin-top: 48rpx;">
                        <input placeholder-style="color:#999999; " type="text" placeholder="请输入邮箱地址"
                               onkeypress="javascript:if(event.keyCode == 32)event.returnValue = false;"
                               @input="ema_regPWIpt"
                            v-model="email" @focus="_close_empty(1)" />
                        <img :src="del" v-if="email.length" @tap="_empty(1)"/>
                    </div>
                    <!-- 验证码 -->
                        <div class='login_inpu regCode'>
                            <div style="display: flex;">                        
                                <input   placeholder-style="color:#999999; " type="number" placeholder="请输入邮箱验证码"
                                   @input="ema_regPWIpt" v-model="emailCode" maxlength="6" style="border: none;"/>
                                <p class='login_p1 login-inpu-p' :style="{color:time_code!='获取验证码'?'#666666':''}" @tap="getEmailCode">{{time_code}}</p>
                            </div>
                        </div>
                        
                    <!-- 密码 -->
                    <div class='login_inpu'>
                        <input placeholder-style="color:#999999; " type="text" :password="regPWStatus1"
                               @input="ema_regPWIpt" :placeholder="language.register.mima1_placeHolder" v-model="passone"
                               @focus="_close_empty(2)" @blur="_passone"/>
                        <img :src="regPWStatus1?pwHide:pwShow" class="login-inpu-img" @tap="pwStatus(2)"/>
                    </div>
                
                    <!-- 再次输入密码 -->
                    <div class='login_inpu'>
                        <input placeholder-style="color:#999999; " type="text" :password="regPWStatus2"
                               @input='ema_regPWIpt' :placeholder="language.register.mima2_placeHolder" v-model="passtwo" @focus="_close_empty(3)"
                               @blur="_passtwo_t"/>
                        <img :src="regPWStatus2?pwHide:pwShow" class="login-inpu-img" @tap="pwStatus(3)"/>
                    </div> 
                </div>

                <div style='padding: 12rpx 32rpx;'>
                    <div class='button1 button-top' v-if='regBtnStatus' @tap='_register'>{{language.register.btn}}</div>
                    <div class='button1 button-top button-opacity' v-else>{{language.register.btn}}</div>
                </div>

            </div>
        </div>
        <!-- 协议 -->
        <div class='agreement' v-if="Agreement">{{language.register.bottomIcon}} 
            <span style="text-decoration:underline;color: #333;" @tap="_yhxy('注册协议')">《{{Agreement}}》</span>
            <template v-if="Agreement2">
                <span style="text-decoration:none">和</span> 
                <span style="text-decoration:underline;color: #333;" @tap="_ysxy('隐私协议')">《{{Agreement2}}》</span>
            </template>
        </div>
        <view class="safe-area-inset-bottom"></view>
        <!-- 协议弹窗 -->
        <show-toast :is_showToast="is_xieyi" :is_showToast_obj="is_showToast_obj" :load="load" @richText="_xieyiShow1"></show-toast>
        <!-- 注册成功 -->
        <show-toast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj"></show-toast>
        <!-- 错误提示弹窗 -->
        <show-toast :is_showToast="is_error" :is_showToast_obj="is_showToast_obj" @richText="_xieyiShow2"></show-toast>
    </div>
</template>

<script>
    import htmlParser from '@/common/html-parser.js'
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
    import showToast from '@/components/aComponents/showToast.vue'

    export default {
        data () {
            return {
                diqu:{
                	code:'CN',
                	code2:'86',
                    num3:156
                },
                email:'',
                emailCode:'',//邮箱验证码
                //组件化弹窗
                is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
                is_xieyi: 0, //0隐藏弹窗  2显示协议弹窗
                is_error: 0, //0隐藏弹窗  3显示错误提示弹窗
                is_showToast_obj: {}, //imgUrl提示图标  title提示文字
                //弹窗
                is_tc: false,
                is_tc_tit:'',
                is_sus: false,
                //提示
                is_ts: false,
                is_ts_tit:'',
                //
                content: '',
                contentNodes: [],
                load: false,
                loadImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/loading.gif',
                //弹窗
                del: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete1x.png',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
                bg_white: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/bg_white.png',
                regId: '',
                tabbar_isShow: 2,
                toHome: false,
                regPWStatus1: true,
                regPWStatus2: true,
                regBtnStatus: false,
                phone_codeStatus2: false,
                fastTap: true,
                passone: '',
                passtwo: '',
                phone_y: '',
                passone_y: '',
                passtwo_y: '',
                pwShow: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwHide.png',
                pwHide: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwShow.png',
                phone: '', //验证码登录手机号
                phone_code: '', //验证码
                one_code: '', //手机号码格式正确返回值
                time_code: '获取验证码',
                timer: null,
                count: '', //倒计时时间
                old_phone: '', //存储获取验证码时的手机号码
                flag: true, //返回
                fatherId: '',//父级id(分销商分享使用)
                baiduHeadTop: 0,// 百度小程序头部高度兼容
                togoodsDetail: false,//登录校验未通过进入
                Agreement: '',
                Agreement2:'',
                bgColor:[{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ]
            }
        },
        components:{
            showToast
        },
        onShow() {
            this.time_code = this.language.login.page2.getCode
            if(uni.getStorageSync('diqu')){
            	this.diqu = JSON.parse(uni.getStorageSync('diqu'))
            }
        },
        onUnload(){
        	uni.removeStorageSync('diqu')
        },
        onLoad (option) {
            
            const pages = getCurrentPages()
            //判断landing_code是否存在，存在则是登录验证未通过进入，登录成功后需要返回上一页
            if (option.landing_code) {
                this.togoodsDetail = option.landing_code
            }

            // #ifdef MP-BAIDU
            // 百度小程序头部兼容
            uni.getSystemInfo({
                success: (res) => {
                    this.baiduHeadTop = res.navigationBarHeight
                }
            })
            // #endif

            if (option.fatherId) {
                this.fatherId = option.fatherId
            }
            if (uni.getStorageSync('fatherId')) {
                this.fatherId = uni.getStorageSync('fatherId')
            }
            let data = {
                api: 'app.login.index',
            }
            this.$req.post({ data }).then(res => {
                if (res.code == 200) {
                    this.Agreement = res.data.Agreement
                    this.Agreement2 = res.data.Agreement_1
                }
            })
        },
        methods: {
            // 手机号、邮箱注册
            userRegister(){
                const data ={
                    api:'app.Login.user_register',
                    type: this.tabbar_isShow - 1, // 类型 0.手机号 1.邮箱
                    cpc: this.diqu.code2, // 国家/地区
                    phone: this.phone, // 手机号码
                    e_mail: this.email,   // 邮箱
                    password: this.passone,//密码
                    keyCode: this.phone_code,// 验证码
                    // userId: this.regId// 账号
                }
            },
            _xieyiShow1(){
                this.is_xieyi = 0
            },
            _xieyiShow2(){
                this.is_error = 0
            },
            //关闭协议弹窗
            _xieyiShow(e){
                if(e == 'tc'){
                    this.is_tc = !this.is_tc
                } else {
                    this.is_ts = !this.is_ts
                }
            },
            //密码登录to验证码登录
            _phone () {   
                this.tabbar_isShow = 1
                this.passone = ''
                this.passtwo = ''
                this.regBtnStatus = false
            },
            //验证码登录to密码登录
            _landing_passw () {  
                this.tabbar_isShow = 2
            },
            getEmailCode(){ 
                if(!this.email || this.email.length == 0){
                    uni.showToast({
                        title:this.language.login.page2.tel_EmailHolder,
                        icon:'none'
                    })
                    return
                }
                const regex = /^[a-zA-Z0-9]+@[a-zA-Z0-9]+\.[a-zA-Z]{2,}$/;
                 if(!regex.test(this.email)){
                     uni.showToast({
                         title:this.language.openInvoice.qsrzqddzyx,
                         icon:'none'
                     })
                     return
                 }
                const data = {
                    api:'app.User.send_email_verification_code',
                    email:this.email
                } 
                const me = this
                this.$req.post({data}).then(res => {
                    let { code, message } = res
                    if (code == 220) {//短信发送频率超限
                        uni.showToast({
                            title: message,
                            duration: 1000,
                            icon: 'none'
                        })
                
                    } else if (code == 200) {
                        uni.showToast({
                            title: '发送成功',
                            duration: 1000,
                            icon: 'none'
                        })
                        me.phoneS = true
                        const TIME_COUNT = 60
                        if (!me.timer) {
                            me.count = TIME_COUNT
                            me.timer = setInterval(() => {
                                if (me.count > 0 && me.count <= TIME_COUNT) {
                                    me.count--
                                    if(uni.getStorageSync('language') == 'en'){
                                        me.time_code = me.language.login.page2.countdown + ' ' + me.count + `s`
                                    }else{
                                        me.time_code = me.count + `s` + me.language.login.page2.countdown
                                    }
                                } else {
                                    clearInterval(me.timer)
                                    me.time_code = me.language.retrievepassword.getCode
                                    me.timer = null
                                    me.count = ''
                                    me.codeBtnAllowClick = true
                                }
                            }, 1000)
                        }
                    } else {
                        uni.showToast({
                            title: this.language.retrievepassword.noGetCaptcha,
                            duration: 1000,
                            icon: 'none'
                        })
                    }  
                })
            },
            //用户协议
            _yhxy(e){
                let data = {
                    api: 'app.login.register_agreement',
                }
                this.$req.post({data}).then(res => {
                    if(res.code == 200){
                        //跳转公共规则页面
                        let title = res.data.name
                        let content = encodeURIComponent(JSON.stringify(res.data.content))
                        let url = '/pagesA/distribution/distribution_rule?title='+ title + '&content=' + content
                        uni.navigateTo({url: url})
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }
                })
            },
            //隐私协议
            _ysxy(e){
                let data = {
                    api: 'app.login.privacy_agreement',
                }
                this.$req.post({data}).then(res => {
                    if(res.code == 200){
                        //跳转公共规则页面
                        let title = res.data.name
                        let content = encodeURIComponent(JSON.stringify(res.data.content))
                        let url = '/pagesA/distribution/distribution_rule?title='+ title + '&content=' + content
                        uni.navigateTo({url: url})
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }
                })
            },
            // 返回
            _banck () {
                if (this.toHome) {
                    uni.redirectTo({
                        url: "/pages/shell/shell?pageType=home"
                    })
                } else {
                    uni.navigateBack({
                        delta: 2
                    })
                }
            },
            // 到登录页面
            _landing_q () {
                this.regId = ''
                this.flag = false
                this.regBtnStatus = false
                this.phone_codeStatus2 = false
                this.phone = ''
                this.phone_code = ''
                this.passone = ''
                this.passtwo = ''
                clearInterval(this.timer)
                this.time_code = this.language.login.page2.getCode
                this.timer = null
                this.count = ''
                this.flag = true
                uni.navigateBack({
                    delta: 1
                })
            },
            // input聚焦
            _close_empty (type) {
                if (type == 1) {
                    this.phone_y = true
                } else if (type == 2) {
                    this.passone_y = true
                } else if (type == 3) {
                    this.passtwo_y = true
                }
            },
            // 叉，清空内容 1登录账号 3验证码登录手机号 245没用到
            _empty (val) {
                if (val == 1) {
                    this.regId = ''
                } else if (val == 2) {
                    this.password = ''
                } else if (val == 3) {
                    this.phone = ''
                } else if (val == 4) {
                    this.passone = ''
                } else if (val == 5) {
                    this.passtwo = ''
                }
            },
            // 账号输入
            _regIdIpt: function (e) {
                if (this.tabbar_isShow == 1 && e.target.value.length > 5 && this.passone.length > 5 &&
                    this.passtwo.length > 5 && this.phone_code.length == 6 && this.passone.length == this.passtwo.length) {
                    this.regBtnStatus = true
                } else {
                    this.regBtnStatus = false
                }
            },
            // 账号失焦  账号正则，限定输入数字与字母组合
            blurReg: function (e) {
                let re = /^[a-z0-9]{6,16}$/i
                if (e.target.value != '') {
                    let rez = re.test(e.target.value)
                    if (rez == true) {

                    } else {
                        e.target.value = ''
                        uni.showToast({
                            title: this.language.register.zhanghaoTips,
                            duration: 2000,
                            icon: 'none'
                        })

                    }
                }
                this.regId = e.target.value
            },
            // 首次密码输入
            _regPW1Ipt: function (e) {
                if (this.tabbar_isShow == 1 &&   this.phone.length >0 && e.target.value.length > 5 &&
                    this.passtwo.length > 5 && this.phone_code.length == 6) {
                    this.regBtnStatus = true
                } else {
                    this.regBtnStatus = false
                }
            },
            // 再次密码输入
            _regPW2Ipt: function (e) {
                if (this.tabbar_isShow == 1 &&   this.phone.length >0 && this.passone.length > 5 && this.passtwo.length > 5 &&
                    e.target.value.length > 5 && this.phone_code.length == 6) {
                    this.regBtnStatus = true
                } else {
                    this.regBtnStatus = false
                }
            },
            // 邮箱 按钮 校验
            ema_regPWIpt(e){
            
                if (  this.passtwo.length > 5 &&   this.passone.length > 5 && this.emailCode.length == 6
                    && this.email.length >0) {
                    this.regBtnStatus = true 
                } else {
                    this.regBtnStatus = false
                }
            },
         
            
            // 密码失焦
            _passone () {
                this.passone_y = false
                var re = /^[a-z0-9]{6,16}$/i
                if (this.passone != '') {
                    var rez = re.test(this.passone)
                    if (rez == true) {

                    } else {
                        this.passone = ''
                        uni.showToast({
                            title: this.language.register.mimaTips1,
                            duration: 3000,
                            icon: 'none'
                        })
                    }
                }
            },
            // 两次密码输入是否一致
            _passtwo_t () {
                this.passtwo_y = false
                if (this.passone != this.passtwo) {
                    uni.showToast({
                        title: this.language.register.mimaTips2,
                        duration: 1000,
                        icon: 'none'
                    })
                }
            },
            // 密码是否可见 1登录密码 2注册密码 3再次输入注册密码
            pwStatus (type) {
                lkt_pwStatus(type, this)
            },
            // 手机号输入
            _regPhoneIpt: function (e) {
                if (e.target.value.length >0 && this.passone.length > 5 &&
                    this.passtwo.length > 5 && this.phone_code.length == 6 && this.passone.length == this.passtwo.length) {
                    this.regBtnStatus = true
                } else {
                    this.regBtnStatus = false
                }
            },
            //手机号码正则验证 type2验证码登录输入手机号，3注册输入手机号
            _telephone (value, type) {
                // this.one_code = telephone(value)
                this.one_code = true
                lkt_telephone(type, this)
            },
            //获取验证码 type1验证码登录 2注册
            _phone_code (type) {
                lkt_phone_code(type, this)
            },
            // 验证码输入
            _regCodeIpt: function (e) {
                if (  this.phone.length >0 && this.passone.length > 5 &&
                    this.passtwo.length > 5 && e.target.value.length == 6 && this.passone.length == this.passtwo.length) {
                    this.regBtnStatus = true
                } else {
                    this.regBtnStatus = false
                }
            },
            // 邮箱验证码输入
            emailCodeIpt: function (e) {
                if (  this.phone.length >0 && this.passone.length > 5 &&
                    this.passtwo.length > 5 && e.target.value.length == 6 && this.passone.length == this.passtwo.length) {
                    this.regBtnStatus = true
                } else {
                    this.regBtnStatus = false
                }
            },
            // 注册
            _register () {
                var me = this
                // if (this.tabbar_isShow == 1 && this.phone && this.one_code == 1 && this.passone == this.passtwo && this.passone) {
                    if (!this.fastTap) {
                        return
                    }
                    if (this.passone != this.passtwo) {
                        uni.showToast({
                            title: this.language.register.mimaTips2,
                            duration: 1000,
                            icon: 'none'
                        })
                        me.fastTap = true
                        return
                    }
                    this.fastTap = false
                    let data = {}
                    // 手机号注册
                    if(this.tabbar_isShow == 1){
                         
                         data ={
                            api:'app.Login.user_register',                        
                            type: 0, // 类型 0.手机号 1.邮箱
                            cpc: this.diqu.code2, // 国家/地区
                            phone: this.phone, // 手机号码 
                            password: this.passone,//密码
                            keyCode: this.phone_code,// 验证码
                            // userId: this.regId,// 账号
                            cpc: this.diqu.code2, // 国家/地区
                            country_num:this.diqu.num3,  // 国家代码
                        }
                    }else{  
                         const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g;
                          if(!regex.test(this.email)){
                              uni.showToast({
                                  title:this.language.openInvoice.qsrzqddzyx,
                                  icon:'none'
                              })
                              me.fastTap = true
                              return
                          }
                        // 邮箱注册
                          data ={
                            api:'app.Login.user_register',
                            type: 1, // 类型 0.手机号 1.邮箱 
                            e_mail:this.email,  // 邮箱
                            password:this.passone, //密码
                            keyCode:this.emailCode // 验证码
                        }
                    }
                    // 分享時 綁定人手機號
                    if (this.fatherId != '') {
                        data.pid = this.fatherId
                    }
                    // 补充变量url，解决uni.request中url为undefined的问题
                    this.$req.post({data}).then(res => {
                        me.fastTap = true
                        
                        if (res.code == 200 && res.data && res.data.access_id) {
                            let {
                                access_id,
                                y_password,
                                wx_status
                            } = res.data
                            
                            //提示注册成功
                            this.is_showToast = 1
                            this.is_showToast_obj.imgUrl = this.sus
                            this.is_showToast_obj.title = '注册成功'
                            setTimeout(()=>{
                                this.is_showToast = 0
                            },2000)
                            
                            uni.setStorageSync('LoingByHand', true)
                            me.set_access_id(access_id)
                            uni.setStorageSync('access_id', access_id) 
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
                            me.fastTap = true
                            this.is_error = 3
                            this.is_showToast_obj.button = '我知道了'
                            if(res.message == '验证码不正确'){
                                this.is_showToast_obj.title = '验证码输入错误，请重新输入。'
                            } else {
                                this.is_showToast_obj.title = res.message
                            }
                        }
                    }).catch(e => { me.fastTap = true })
                    
                // } else if (this.one_code != 1) {
                //     uni.showToast({
                //         title: this.language.register.telTips,
                //         duration: 1000,
                //         icon: 'none'
                //     })
                // } else if (this.phone_code.length != 6) {
                //     uni.showToast({
                //         title: this.language.register.yzmTips,
                //         duration: 1000,
                //         icon: 'none'
                //     })
                // } else if (this.passone != this.passtwo && this.passone) {
                //     uni.showToast({
                //         title: this.language.register.mimaTips3,
                //         duration: 1000,
                //         icon: 'none'
                //     })
                // } else {
                //     uni.showToast({
                //         title: this.language.register.wanzhengTips,
                //         duration: 1000,
                //         icon: 'none'
                //     })
                // }
            },
            _navigateTo (url) {
                uni.navigateTo({
                    url
                })
            },
            ...mapMutations({
                set_access_id: 'SET_ACCESS_ID',
                user_phone: 'SET_USER_PHONE'
            })
        },
    }
</script>

<style scoped lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/login/register.less");
    .login{
        padding: 0 32rpx;
    }
    
</style>
