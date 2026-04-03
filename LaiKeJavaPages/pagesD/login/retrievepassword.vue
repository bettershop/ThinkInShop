<template>
    <div style="background-color: #F4F5F6;height: 100ch;">
        <heads 
            :title="title" 
            :ishead_w="2" 
            :bgColor="bgColor" 
            :titleColor="'#333333'">
        </heads>
        <!-- 验证方式 -->
        <template v-if="type === ''">
            <div class='login' v-if='!code_dis'>
                <div class='login_inpu loginsss' style="margin-top: 48rpx;">
            		<view class="qu" @tap="selectionMode(0)">
            			 <text>手机号验证</text>
            			 <img :src="jiantou" class='arrow qu_img' style='margin-left: 8rpx; top: 0rpx;'  />
            		</view> 
                </div>
                <div class='login_inpu loginsss' style="margin-top: 48rpx;">
                	<view class="qu" @tap="selectionMode(1)">
                		 <text>邮箱验证</text>
                		 <img :src="jiantou" class='arrow qu_img' style='margin-left: 8rpx; top: 0rpx;'  />
                	</view> 
                </div>
            </div>  
        </template>
        <!-- 手机号验证 -->
        <template v-if="type === 0 && !verificationCode">
            <div class='login'>
                <div class='login_inpu loginsss' style="margin-top: 48rpx;display: flex;align-items: center;">
                    <view class="qu" @tap.shop="selectQuHao">
                    	<view>{{diqu.code}}+{{diqu.code2}} </view>
                    	<view class="san-jiao"></view>
                    </view>
                    <input 
                        type="text"
                        v-model="zhanghao"
                        placeholder-style="color:#999999; " 
                        :placeholder="language.retrievepassword.zhanghao_placeHolder" 
                        :disabled="phoneS"
                        onkeypress="javascript:if(event.keyCode == 32)event.returnValue = false;"
                        @focus="_phone_t(1)" 
                        @blur='_telephone(phone,1)' 
                        @input="_changeStep"/>
                    <img :src="del" v-if="zhanghao.length&&phone_t" @tap="_empty(1)"/>
                </div>
                <div class='button a1' :class="{active:changeStep}" @tap="_next_one" v-if='next'>{{language.retrievepassword.nextBtn}}</div>
                <div class='button a2' :class="{active:changeStep}" @tap="_next_two" v-if='!next'>{{language.retrievepassword.nextBtn}}</div>
            </div>
        </template>
        <!-- 邮箱验证 -->
        <template v-if="type === 1 && !verificationCode">
            <div class='login'>
                <div class='login_inpu loginsss' style="margin-top: 48rpx;display: flex;align-items: center;">
              
                    <input 
                        type="text"
                        v-model="mailbox"
                        placeholder-style="color:#999999; " 
                        placeholder="请输入邮箱地址" 
                        :disabled="phoneS"
                        onkeypress="javascript:if(event.keyCode == 32)event.returnValue = false;"
                        @focus="_phone_t(1)" 
                        @blur='_telephone(phone,1)' 
                        @input="_changeStep"/>
                    <img :src="del" v-if="mailbox.length&&phone_t" @tap="_empty(2)"/>
                </div>
                <div class='button a1' :class="{active:changeStep}" @tap="_next_one" v-if='next'>{{language.retrievepassword.nextBtn}}</div>
                <div class='button a2' :class="{active:changeStep}" @tap="_next_two" v-if='!next'>{{language.retrievepassword.nextBtn}}</div>
            </div>
        </template>
        <!-- 验证码区域 --> 
        <template  v-if="verificationCode">
            <div class='login'>
                <view v-if="type === 0" style="margin:20rpx;margin-top: 40rpx;">将发送短信验证码至 {{language.retrievepassword.jsjhmfs}} +{{diqu.code2}} {{pohecode}}</view>
                <view v-else style="margin:20rpx;margin-top: 40rpx;">将发送邮箱验证码至 {{mailbox}}</view>
                <div class='login_inpu loginsss'  style="display: flex; align-items: center;">
                    <input 
                        type="number" 
                        v-model="phone_code"
                        style="flex: 1;"
                        placeholder-style="color:#999999; " 
                        :placeholder="language.retrievepassword.code_placeHolder" 
                        @input="_changeStep1"/>
                    <div class='login_p' :style="{color:time_code!='获取验证码'?'#666666':''}" v-text="time_code" @tap="_code()"></div>
                </div>
                
                <div class='' style='position: relative;margin-top: 48rpx;'>
                    <div class='login_inpu loginsss'>
                        <input v-if='!pwFlag' 
                            type="password" 
                            v-model="new_password"
                            :placeholder="language.retrievepassword.mima1_placeHolder"
                            placeholder-style="color:#999999; " 
                            @focus="_phone_t(2)"
                            @blur="_w_password(1)" 
                            @input="_changeStep2"/>
                        <input v-else 
                            type="text"
                            v-model="new_password"
                            :placeholder="language.retrievepassword.mima1_placeHolder"
                            placeholder-style="color:#999999; "
                            @focus="_phone_t(2)"
                            @blur="_w_password(1)"
                            @input="_changeStep2"/>
                        <img v-if='pwFlag' :src="pwShow" @tap="_seepw(1)"/>
                        <img v-else :src="pwHide" @tap="_seepw(1)"/>
                    </div>
                    <div class='login_inpu loginsss'>
                        <input v-if='!pwFlag1' 
                            type="password"
                            v-model="old_password"
                            :placeholder="language.retrievepassword.mima2_placeHolder"
                            placeholder-style="color:#999999; "
                            @focus="_phone_t(3)"
                            @blur="_w_password(2)"
                            @input="_changeStep2"/>
                        <input v-else 
                            type="text"
                            v-model="old_password"
                            :placeholder="language.retrievepassword.mima2_placeHolder"
                            placeholder-style="color:#999999; "
                            @focus="_phone_t(3)"
                            @blur="_w_password(2)"
                            @input="_changeStep2"/>
                        <img v-if='pwFlag1' :src="pwShow" @tap="_seepw(2)"/>
                        <img v-else :src="pwHide" @tap="_seepw(2)"/>
                    </div>
                    <template v-if='!pass'>
                        <template v-if="old_password.length > 5 && new_password.length > 5">
                            <div class='button buttons active' @tap="_next_three">
                                {{language.retrievepassword.saveBtn}}
                            </div>
                        </template>
                        <template v-else>
                            <div class='button buttons'>
                                {{language.retrievepassword.saveBtn}}
                            </div>
                        </template>
                    </template>
                </div> 
                
                
                <div class='button buttons active' @tap="changePassword"> 确认</div>
            </div>
        </template>
        
        <!--  请输入手机号    -->
        <!-- <div v-if='pass' style='position: relative;'>
            <div class='login'>
                <div class='login_inpu loginsss' style="margin-top: 48rpx;">
                    <input 
                        type="text"
                        v-model="zhanghao"
                        placeholder-style="color:#999999; " 
                        :placeholder="language.retrievepassword.zhanghao_placeHolder" 
                        :disabled="phoneS"
                        onkeypress="javascript:if(event.keyCode == 32)event.returnValue = false;"
                        @focus="_phone_t(1)" 
                        @blur='_telephone(phone,1)' 
                        @input="_changeStep"/>
                    <img :src="del" v-if="zhanghao.length&&phone_t" @tap="_empty(1)"/>
                </div>
                <div class='login_inpu loginsss' v-if='code_dis' style="display: flex; align-items: center;">
                    <input 
                        type="number" 
                        v-model="phone_code"
                        style="flex: 1;"
                        placeholder-style="color:#999999; " 
                        :placeholder="language.retrievepassword.code_placeHolder" 
                        @input="_changeStep1"/>
                    <div class='login_p' :style="{color:time_code!='获取验证码'?'#666666':''}" v-text="time_code" @tap="_code()"></div>
                </div>
            </div>
            
            <div class='button a1' :class="{active:changeStep}" @tap="_next_one" v-if='next'>{{language.retrievepassword.nextBtn}}</div>
            <div class='button a2' :class="{active:changeStep}" @tap="_next_two" v-if='!next'>{{language.retrievepassword.nextBtn}}</div>
        </div> -->
        <!-- 重置密码/设置新密码 -->
        <!-- <div v-if='!pass'>
            <div class='login' style='position: relative;margin-top: 48rpx;'>
                <div class='login_inpu loginsss'>
                    <input v-if='!pwFlag' 
                        type="password" 
                        v-model="new_password"
                        :placeholder="language.retrievepassword.mima1_placeHolder"
                        placeholder-style="color:#999999; " 
                        @focus="_phone_t(2)"
                        @blur="_w_password(1)" 
                        @input="_changeStep2"/>
                    <input v-else 
                        type="text"
                        v-model="new_password"
                        :placeholder="language.retrievepassword.mima1_placeHolder"
                        placeholder-style="color:#999999; "
                        @focus="_phone_t(2)"
                        @blur="_w_password(1)"
                        @input="_changeStep2"/>
                    <img v-if='pwFlag' :src="pwShow" @tap="_seepw(1)"/>
                    <img v-else :src="pwHide" @tap="_seepw(1)"/>
                </div>
                <div class='login_inpu loginsss'>
                    <input v-if='!pwFlag1' 
                        type="password"
                        v-model="old_password"
                        :placeholder="language.retrievepassword.mima2_placeHolder"
                        placeholder-style="color:#999999; "
                        @focus="_phone_t(3)"
                        @blur="_w_password(2)"
                        @input="_changeStep2"/>
                    <input v-else 
                        type="text"
                        v-model="old_password"
                        :placeholder="language.retrievepassword.mima2_placeHolder"
                        placeholder-style="color:#999999; "
                        @focus="_phone_t(3)"
                        @blur="_w_password(2)"
                        @input="_changeStep2"/>
                    <img v-if='pwFlag1' :src="pwShow" @tap="_seepw(2)"/>
                    <img v-else :src="pwHide" @tap="_seepw(2)"/>
                </div>
                <template v-if='!pass'>
                    <template v-if="old_password.length > 5 && new_password.length > 5">
                        <div class='button buttons active' @tap="_next_three">
                            {{language.retrievepassword.saveBtn}}
                        </div>
                    </template>
                    <template v-else>
                        <div class='button buttons'>
                            {{language.retrievepassword.saveBtn}}
                        </div>
                    </template>
                </template>
            </div>

        </div> -->
        <!-- 提示弹窗 -->
        <showToast
            :is_showToast="is_showToast" 
            :is_showToast_obj="is_showToast_obj">
        </showToast>
    </div>
</template>

<script>
    import showToast from "@/components/aComponents/showToast.vue"
    export default {
        data () {
            return {
                diqu:{
                	code:'CN',
                	code2:'86',
                    num3:156,
                },
                mailbox:'',
                pohecode:'',
                type:'',
                verificationCode:false,
                is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
                is_showToast_obj: {}, //imgUrl提示图标  title提示文字
                phoneS: false,
                fastTap: true,
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                del: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete1x.png',
                pwShow: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwHide.png',
                pwHide: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwShow.png',
                bg_white: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/bg_white.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                title: '',
                phone: '',
                phone_next: '',
                code_dis: false,
                phone_code: '',
                old_phone: '',
                count: '',
                timer: null,
                time_code: '获取验证码',
                next: true,
                pass: true,
                pwFlag: false,
                pwFlag1: false,
                old_password: '',
                new_password: '',
                phone_t: '',
                new_password_f: '',
                old_password_f: '',
                changeStep: false,
                zhanghao: '',
                codeBtnAllowClick: false,//获取验证码按钮是否允许点击
                isInput: false,
                back1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x_w.png',
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
            showToast,
        },
        onShow () {
            this.codeBtnAllowClick = true
            this.title = this.language.retrievepassword.title
            this.time_code = this.language.retrievepassword.getCode
            if(uni.getStorageSync('diqu')){
            	this.diqu = JSON.parse(uni.getStorageSync('diqu'))
            } 
        },
        onUnload(){
        	uni.removeStorageSync('diqu')
        },
        watch:{
            'zhanghao':{
                handler(){
                    if(this.zhanghao != ''){
                        this.changeStep = true
                    }else{
                        this.changeStep = false
                    }
                }
            }
        },
        methods: {
            // 返回
            _back() {
                
                if(this.pass) {
                    if(this.next) {
                        uni.redirectTo({
                            url: '/pagesD/login/newLogin'
                        })
                    }else {
                        this.next = true
                        this.code_dis = false
                        this.phoneS = false
                        this.changeStep = true
                    }
                } else {
                    this.pass = !this.pass
                    this.title = this.language.retrievepassword.title
                }
            },
            
            _seepw (index) {
                if (index == 1) {
                    this.pwFlag = !this.pwFlag
                    return
                }
                this.pwFlag1 = !this.pwFlag1
            },
            _changeStep (e) {
                if (e.detail.value !== '') {
                    this.isInput = true
                    this.changeStep = true

                } else {
                    this.changeStep = false
                }
                //正则验证 不能输入特殊字符
                if(this.type === 0){                    
                    if(/[^u4e00-u9fa5w]/g.test(e.detail.value)){
                        uni.showToast({
                            title: '不能输入特殊字符',
                            icon: 'none'
                        })
                    }
                }
                this.zhanghao = e.detail.value.replace(/[^u4e00-u9fa5w]/g,'')
            },
            _changeStep1 (e) {
                if (e.detail.value.length === 6) {
                    this.isInput = false
                    this.changeStep = true
                } else {
                    this.changeStep = false
                }
            },
            _changeStep2 (e) {
                if (e.detail.value == this.new_password || e.detail.value == this.old_password) {
                    this.isInput = true
                    this.changeStep = true
                } else {
                    this.changeStep = false
                }
            },
            _phone_t (type) {
                if (type == 1) {
                    this.phone_t = true
                } else if (type == 2) {
                    this.new_password_f = true
                } else if (type == 3) {
                    this.old_password_f = true
                }

            },
            _w_password (type) {
                if (type == 1) {
                    this.new_password_f = true
                } else if (type == 2) {
                    this.old_password_f = true
                }
            },
            _telephone (value, type) {
                if (type == 1) {
                    this.phone_t = true
                }
            },
            _next_one () { 
                if(!this.zhanghao) {
                    return
                }
                var me = this

                let data = {
                   api:'app.Login.forget_zhanghao',
                    type: this.type, //类型 0.手机号 1.邮箱
                    cpc: this.type === 1 ? '': this.diqu.code2 , // 国家/地区
                    country_num: this.type === 1 ? '' : this.diqu.num3 , // 国家代码
                    zhanghao: this.type === 1?this.mailbox : this.zhanghao// 手机号 或是邮箱
                }
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                        if(this.type === 0 ){
                            this.pohecode = this.zhanghao.substring(0,3) +'****'+this.zhanghao.substring(7)
                        }
                        this.verificationCode = true
                        
                    }  else {
                        uni.showToast({
                            title: res.message,
                            duration: 1000,
                            icon: 'none'
                        })
                    }  
                })
            },
            _next_two () {
                // if (this.isInput) { return }
                var me = this
                if (!this.fastTap) {
                    return
                }
                this.fastTap = false
                if (!this.phone_code && this.code_dis) {
                    uni.showToast({
                        title: this.language.retrievepassword.captchaEmpty,
                        duration: 1500,
                        icon: 'none'
                    })
                    setTimeout(function () {
                        me.fastTap = true
                    }, 1500)
                } else if (this.phone_code.length != 6) {
                    uni.showToast({
                        title: this.language.retrievepassword.captchaFail,
                        duration: 1500,
                        icon: 'none'
                    })
                    setTimeout(function () {
                        me.fastTap = true
                    }, 1500)
                } else if (this.phone_code.length == 6 && this.phoneS) {
                    
                    let data = {
                        api: 'app.login.forget_code',
                        phone: me.zhanghao,
                        keyCode: me.phone_code
                    }
                    this.$req.post({data}).then(res => {
                        let { code, message } = res

                        if (code == 200) {
                            me.title = me.language.retrievepassword.title1
                            me.changeStep = false
                            me.pass = false
                        } else {
                            uni.showToast({
                                title: message,
                                duration: 1500,
                                icon: 'none'
                            })
                        }
                        setTimeout(function () {
                            me.fastTap = true
                        }, 1500)  
                    })
                }
            },
            //叉，清空内容
            _empty (val) {
                if (val == 1) {
                    this.zhanghao = ''
                } else if (val == 2) {
                    this.mailbox = ''
                }  
            },
            _newpassword () {
                let re = /^[a-z0-9]{6,16}$/i
                if (this.new_password != '') {
                    this.rez = re.test(this.new_password)
                    if (this.rez == true) {
                        return 1
                    } else {
                        uni.showToast({
                            title: this.language.retrievepassword.zhanghaoTips,
                            duration: 2000,
                            icon: 'none'
                        })
                    }
                }
            },
            _next_three () {
                if (!this.isInput) { return }
                let type = this._newpassword()
                if (type == 1) {
                    if (this.old_password == this.new_password && this.new_password != '' && this.phone_code.length == 6) {
                        let data = {
                            api: 'app.login.forgotpassword',
                            phone: this.zhanghao,
                            password: this.new_password,
                            keyCode: this.phone_code
                        }
                        if(!this.phone_code || this.phone_code.length ==0 ){
                            uni.showToast({
                                title: this.language.register.captchaEmpty,
                                duration: 1000,
                                icon: 'none'
                            })
                            return
                        }
                        if (this.new_password != this.old_password) {
                            uni.showToast({
                                title: this.language.register.mimaTips2,
                                duration: 1000,
                                icon: 'none'
                            })
                            return
                        }
                        this.$req.post({data}).then(res => {
                            let { message, code } = res
                            if (code == 200) {
                                //提示设置成功
                                this.is_showToast = 1
                                this.is_showToast_obj.imgUrl = this.sus
                                this.is_showToast_obj.title = this.language.retrievepassword.setSus,
                                setTimeout(()=>{
                                    this.is_showToast = 0
                                },1000)
                                setTimeout(function () {
                                    uni.redirectTo({
                                        url: 'login?toHome=true'
                                    })
                                }, 1000)
                            } else {
                                uni.showToast({
                                    title: message,
                                    icon: 'none',
                                    duration: 1000
                                })
                            }
                        })
                        
                    } else {
                        uni.showToast({
                            title: this.language.retrievepassword.mimaTips1,
                            duration: 1000,
                            icon: 'none'
                        })
                    }
                } else {
                    uni.showToast({
                        title: this.language.retrievepassword.mimaTips2,
                        duration: 1000,
                        icon: 'none'
                    })
                }
            },
            _code () {
                var me = this
                if (!me.codeBtnAllowClick) {
                    return
                }
                me.codeBtnAllowClick = false
                
                let data = {}
                // 手机号获取验证码
                if(this.type === 0){                    
                    data = {
                        api: 'app.user.secret_key',
                        phone: this.zhanghao,
                        cpc:this.diqu.code2,
                    }
                }
                // 邮箱获取 验证码
                if(this.type === 1){
                     data = {
                        api:'app.User.send_email_verification_code',
                        email:this.mailbox
                    } 
                }
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
                                    if(uni.getStorageSync('language') == 'en_US'){
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
            selectionMode(numType){
                this.type = numType
            },
            // 跳转选择国家/地区
            selectQuHao(){ 
            	this.navTo('/pagesD/login/countryAndRegion')
            },
         
            // 忘记密码 修改密码 保存接口
            changePassword(){
                if(!this.phone_code || this.phone_code.length ==0 ){
                    uni.showToast({
                        title: this.language.retrievepassword.captchaEmpty,
                        duration: 1000,
                        icon: 'none'
                    })
                    return
                }
                if(!this.new_password){
                    uni.showToast({
                        title: this.language.register.mima1_placeHolder,
                        duration: 1000,
                        icon: 'none'
                    })
                    return
                }
                if (this.new_password != this.old_password) {
                    uni.showToast({
                        title: this.language.register.mimaTips2,
                        duration: 1000,
                        icon: 'none'
                    })
                    return
                }
                let re = /^[a-z0-9]{6,16}$/i
            
                    let rez = re.test(this.new_password)
                    if (rez == true) {
                
                    } else {
                       
                        uni.showToast({
                            title: this.language.register.zhanghaoTips,
                            duration: 2000,
                            icon: 'none'
                        }) 
                        return
                    } 
                const data ={
                    api:'app.Login.forget_code', 
                    type: this.type, //类型 0.手机号 1.邮箱
                    cpc: this.type === 1 ? '': this.diqu.code2 , // 国家/地区
                    country_num: this.type === 1 ? '' : this.diqu.num3 , // 国家代码
                    tel: this.type === 1 ? '' : this.zhanghao , // 手机号 或是邮箱
                    e_mail:this.mailbox , // 邮箱
                    password: this.new_password, // 密码
                    keyCode: this.phone_code //验证码
                }
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                       uni.showToast({
                           title: '操作成功',
                           duration: 1000,
                           icon: 'none'
                       })
                       setTimeout(()=>{
                           uni.reLaunch({
                               url: '/pages/shell/shell?pageType=my',
                               success: function () {}
                           })
                       },1500)
                        
                    }  else {
                        uni.showToast({
                            title: res.message,
                            duration: 1000,
                            icon: 'none'
                        })
                    }  
                })
            }
        }
    }
</script>

<style scoped lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/login/retrievepassword.less");
    .qu {
        height:90rpx;
        .qu_img{
            margin: 0rpx;
        }
    }
</style>
