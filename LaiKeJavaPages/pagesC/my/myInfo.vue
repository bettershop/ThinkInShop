<template>
    <div>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>

        <heads :title="language.myinfo.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        
        <ul class="yh-ul" style="border-radius: 0 0 24rpx 24rpx;">
        
            <li class='myMessage_title'>
                <p>{{language.myinfo.Avatar_settings}}</p>
                <div class='myMessage_right'>
                    <!-- image与img的区别，当img的src为空时会显示裂图，image则不会显示 -->
                    <image v-if="img" @tap="_seeImg" class='myMessage_img' :src="img"></image>
                    <image v-else @tap="_seeImg" class='myMessage_img' :src="unLogin"></image>
                    <image @tap="choiceImg" class='arrow' :src="jiantou"></image>
                </div>
            </li>
        
        </ul>
        <ul class="yh-ul" style="margin-top: 24rpx;">
            <li class='myMessage_name'>
                <p>{{language.myinfo.zh}}</p>
                <div>
                    <span @click="copy">{{zhanghao}}</span>
                </div>
            </li>
            
            <li class='myMessage_name' style="border-top: 0.5px solid rgba(0, 0, 0, .1);">
                <p>{{language.myinfo.sjhm}}</p>
                <div>
                    <template v-if="mobile">
                        <span >+{{cpc}}</span>  <span>{{mobile == null ? '' : mobile}}</span>
                    </template>
                   <view v-else @tap="navTo('/pagesB/setUp/changePhone')"><span style="color: #999999;">去设置</span><img class='arrow' :src="jiantou"/></view>
                </div>
            </li>
            <!-- 邮箱 -->
            <li class='myMessage_name' style="border-top: 0.5px solid rgba(0, 0, 0, .1);">
                <p>{{language.myinfo.email}}</p>
                <div  @tap='bindEmail'>
                    <span v-if="email == '立即绑定'" style="color: #999999;">{{email == null ? '' : email}}</span>
                    <span v-else>{{email == null ? '' : email}}</span>
                    <img class='arrow' :src="jiantou"/>
                </div>
            </li>
        </ul>
        <ul class="yh-ul" style="margin-top: 16rpx;">

            <li class='myMessage_name' @tap="_myname" style="border-bottom: 0.5px solid rgba(0, 0, 0, .1);">
                <p>{{language.myinfo.My_nickname}}</p>
                <div>
                    <span>{{name}}</span>
                    <img class='arrow' :src="jiantou"/>
                </div>
            </li>
            
            <!-- 绑定微信（提现至零钱） -->
            <li class='myMessage_name' @tap="_setWx(wx_withdraw?2:1)" style="border-bottom: 0.5px solid rgba(0, 0, 0, .1);">
                <div>
                    <image :src="wx" style="width: 40rpx;height: 40rpx;border-radius: 50%;margin-right: 10rpx;"></image>
                    <p>微信</p>
                </div>
                <div>
                    <!-- 未绑定 -->
                    <span v-if="!wx_withdraw" style="color: #999999;">立即绑定</span>
                    <!-- 已绑定 -->
                    <span v-else style="color: #999999;">已绑定</span>
                    <img class='arrow' :src="jiantou"/>
                </div>
            </li>

            <!-- #ifndef MP-ALIPAY -->
            <li class='myMessage_name' style="border-bottom: 0.5px solid rgba(0, 0, 0, .1);">
                <p>{{language.myinfo.data}}</p>
                <div style="font-size: 32rpx;">
                    <picker 
                        class='picker_ yh-picker'
                        style="margin-right: 52rpx; position: relative;"
                        mode="date"
                        :disabled="is_birth"
                        start="1900-01-01"
                        :end="currentdate" 
                        @change="changeData">
                            <view>{{user_birth}}</view>
                            <img class='arrow' style="position: absolute; top: 0; right: -52rpx;" :src="jiantou"/>
                    </picker>
                </div>
            </li>
            <!-- #endif -->
            
            <li class='myMessage_name' @tap="_setSex" style="border-bottom: 0.5px solid rgba(0, 0, 0, .1);">
                <p>{{language.myinfo.sex}}</p>
                <div>
                    <span style="color: #999999;">{{sex}}</span>
                    <img class='arrow' :src="jiantou"/>
                </div>
            </li>
            
            <li class='myMessage_name' @tap="_myaddress" >
                <p>{{language.myinfo.dzgl}}</p>
                <div>
                    <img class='arrow' :src="jiantou"/>
                </div>
            </li>

        </ul>

        <div class='mask' v-if="mask_display">
            <div class='mask_cont'>
                <p>{{language.myinfo.modify_nickname}}</p>
                <input type="text" placeholder="请输入昵称，最多16个字段" placeholder-style="color: #999999;" v-model="new_name"/>
                <div class='mask_button'>
                    <div class='mask_button_left' @tap="_cancel">{{language.myinfo.cancel}}</div>
                    <div style="color: #D73B48;" @tap="_confirm('')">{{language.myinfo.determine}}</div>
                </div>
            </div>
        </div>

        <!-- 生日只可设置一次，是否确认修改？ -->
        <div class="bid_pup" v-if="showEndFlag">
            <div class="bid_pup_flex">
                <div class="bid_pup_auto">
                    <div class="end_content">
                        <div class="yh-sr">{{language.myinfo.Tips_birthday}}</div>
                    </div>
                    <div @tap="closeEndFlag('cancel')" class="end_btn yh-end_btns">{{language.myinfo.cancel1}}</div>
                    <div @tap="closeEndFlag('go')" class="end_btn yh-end_btnb">{{language.myinfo.determine}}</div>
                </div>
            </div>
        </div>
        
        <choose-s ref="refChooseS" :is_type="chooseType" :is_choose="sexArr" :is_choose_obj="is_choose_obj" @_leftClick="_quxiao" @_rightClick="_queren"></choose-s>
        <!-- 弹窗组件 -->
        <show-toast :is_showToast="is_showToast1" :is_showToast_obj="is_showToast_obj1" @richText="richText" @confirm="confirm"></show-toast>
    </div>
</template>

<script>
    import chooseS from "@/components/aComponents/choose.vue"
    import showToast from '@/components/aComponents/showToast.vue'
    import {getNowFormatDate ,copyText} from "@/common/util";
    export default {
        data () {
            return {
                cpc:'',
                sexArr: ['男','女'],//
                chooseType: '',//选择性别
                is_choose_obj: {},//
                is_showToast1: 0,//是否显示
                is_showToast_obj1: {},//弹窗显示文字
                is_seeImg: false,
                mobile:'',
                email:'',
                wx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wx.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                unLogin: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                del: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
                title: '我的信息',
                wx_withdraw: false,//是否绑定微信 false/true
                wx_name: '',//绑定的微信名称
                name: '',
                new_name: '',
                mask_display: false,
                img: '', //头像
                zhanghao: '',//账号
                sex: '',//性别
                imgsrc: '',
                show: '',
                src_img: '',
                fastTap: true,
                user_birth: '0001-11-30',
                currentdate:'',//当前日期
                is_birth: false, //日期是否为空 fasle 空
                showEndFlag: false, //弹出框  false-不弹出  true-弹出
                birth_flag: true,
                birthday: '',
                haveAllUserInfo: false,// true 则表示，my页面通过点击完善信息进入
                userSetBr: 0,// 0用户设置过生日 1用户没有设置过生日    1可以设置生日
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ],
            }
        },
        onLoad (option) {
            if(option.haveAllUserInfo){
                this.haveAllUserInfo = option.haveAllUserInfo
            }
            let language = uni.getStorageSync('language')
            if(language == 'en_US'){
                this.sexArr = ['male','female' ]
                this.is_choose_obj= {
                    left: 'Cancel',
                    colorLeft: '#999999',
                    right: 'verify',
                    colorRight: '#FA5151',
                    background: '#F4F5F6',//显示图标
                    borderRadius: '24rpx 24rpx 0 0',//提示文字
                }
            } else{
                this.sexArr = ['男','女' ],
                this.is_choose_obj= {
                    left: '取消',
                    colorLeft: '#999999',
                    right: '确认',
                    colorRight: '#FA5151',
                    background: '#F4F5F6',//显示图标
                    borderRadius: '24rpx 24rpx 0 0',//提示文字
                }
            }
        },
        onShow() {
            this._axios()
            // 获取当前时间
            this.currentdate = getNowFormatDate()
        },
        mounted () {
            var me = this
            this.isLogin(()=>{
            	me._axios()
            })
        },
        components:{
            showToast,chooseS
        },
        methods: {
            
            copy(){
                uni.setClipboardData({
                    data: this.zhanghao,
                })
            },

            /**
             * 绑定微信、解绑微信
             * @param {Object} type 1未绑定微信 2已绑定微信
             */
            _setWx(type){
                switch (type){
                    case 1:
                        this._bdWx()
                        break
                    case 2:
                        this._jbWx()
                        break
                    default:
                }
            },
            /**
             * 绑定邮箱 / 换绑邮箱
             */
            bindEmail(){
                if(this.email !='立即绑定'){
                    this.navTo(`/pagesC/my/changeMailbox?email=${this.email}`)
                }else{
                    this.navTo(`/pagesC/my/changeMailbox`)
                }
            },
            //去绑定微信
            _bdWx(){
                // #ifdef MP
                uni.navigateTo({
                    url: '/pagesB/setWx/wxMp?type=myInfo'
                })
                // #endif
                // #ifdef H5
                this.h5_bangWX()
                // #endif
                // #ifdef APP-PLUS
                this.app_bangWX()
                // #endif
            },
            //解绑微信
            _jbWx(){
                this.is_showToast1 = 4
                this.is_showToast_obj1.content = '确认要解除与微信账号的绑定么?'
                this.is_showToast_obj1.button = '再想想'
                this.is_showToast_obj1.endButton = '确认' 
            },
            //解绑微信-再想想
            richText(){
                this.is_showToast1 = 0
            },
            //解绑微信-确认
            confirm(){
                this.is_showToast1 = 0
                let data = {
                    api: 'app.user.wxUnbind',
                }
                this.$req.post({ data }).then(res => {
                    if (res.code == 200) {
                        this.is_showToast1 = 1
                        this.is_showToast_obj1.imgUrl = this.sus
                        this.is_showToast_obj1.title = '解绑成功'
                        setTimeout(()=>{
                            this.is_showToast1 = 0
                        }, 1500)
                        //重新加载数据
                        this._axios()
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }
                })
            },
            /**
             * H5绑定微信开始
             */
            h5_bangWX(){
                //toLowerCase()将字符串中的所有大写字母转换为对应的小写字母
                let ua = navigator.userAgent.toLowerCase()
                if (ua.match(/MicroMessenger/i) == "micromessenger"){
                    const BrowserType = '微信浏览器'
                    this.h5_bangWX_wxllq()
                } else {
                    const BrowserType = '其他浏览器'
                    this.h5_bangWX_qtllq()
                }
            },
            //（H5）去绑定微信-微信浏览器中进行
            h5_bangWX_wxllq(){
                let data = {
                    api: 'app.login.getWxAppId'
                }
                this.$req.post({data}).then(res => {
                    let appid = res.data.appId//项目appid
                    let urls = this.LaiKeTuiCommon.LKT_H5_DEFURL + 'pages/shell/shell?pageType=my'
                    let url = encodeURIComponent(urls)//这里的是回调地址要与申请的地址填写一致
                    let scopes = "snsapi_userinfo"//表示授权的作用域，多个可以用逗号隔开，snsapi_base表示静默授权，snsapi_userinfo表示非静默授权
                    let mainstate = true//state：用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止csrf攻击（跨站请求伪造攻击）可设置为简单的随机数加session进行校验
                    window.location.href =`https://open.weixin.qq.com/connect/oauth2/authorize?appid=${appid}&redirect_uri=${url}&response_type=code&scope=${scopes}&state=${mainstate}#wechat_redirect`
                })
            },
            //（H5）去绑定微信-其他浏览器中进行
            h5_bangWX_qtllq(){
                let data = {
                    api: 'app.login.getWxAppId'
                }
                this.$req.post({data}).then(res => {
                    let appid = res.data.appId//项目appid
                    let urls = this.LaiKeTuiCommon.LKT_H5_DEFURL + 'pages/shell/shell?pageType=my'
                    let url = encodeURIComponent(urls)//这里的是回调地址要与申请的地址填写一致
                    let scopes = "snsapi_login"//应用授权作用域，拥有多个作用域用逗号（,）分隔，网页应用目前仅填写snsapi_login即可
                    let mainstate = true//state：用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止csrf攻击（跨站请求伪造攻击）可设置为简单的随机数加session进行校验
                    window.location.href =`https://open.weixin.qq.com/connect/qrconnect?appid=${appid}&redirect_uri=${url}&response_type=code&scope=${scopes}&state=${mainstate}#wechat_redirect`
                })
            },
            /**
             * H5绑定微信结束
             */
            //app绑定微信
            app_bangWX(){
                let _this = this;
                uni.getProvider({
                    service: 'oauth',
                    success: function(res) {
                        if (~res.provider.indexOf('weixin')) {
                            uni.login({
                                provider: 'weixin',
                                success: function(loginRes) {
                                    let unionids = loginRes.authResult.unionid
                                    let access_tokens = loginRes.authResult.access_token
                                    let data = {
                                        api: 'app.user.bindWechat',
                                        userApiToken: access_tokens,
                                        unionId: unionids
                                    }
                                    _this.$req.post({ data }).then(res => {
                                        uni.showToast({
                                            title: res,
                                            icon: 'none'
                                        })
                                    })
                                },
                                fail: function(err) {
                                    uni.showToast({
                                        title: err,
                                        icon: 'none'
                                    })
                                }
                            });
                        }
                    },
                    fail: function(err) {
                        uni.showToast({
                            title: err,
                            icon: 'none'
                        })
                    }
                });
            },
            _quxiao(){
                this.chooseType = 0
            },
            _queren(index){
                this.chooseType = 0
                // if(index == 3){
                //     //保密传0
                //     this._confirm(0)
                // }
                this._confirm(index + 1)
            },
            _setSex(){
                this.chooseType = 1
            },
            /**
             * 确认设置出生日期 弹窗
             * */
            changeData (e) {
                //PC端H5（未开启F12）disabled未起效果 只能用如此方式
                if(this.is_birth){
                    uni.showToast({
                        title: '出生日期只可设置一次',
                        icon: 'none'
                    })
                    return
                }
                this.birthday = e.detail.value
                this.showEndFlag = true
            },
            /**
             * 设置出生日期弹窗 按钮
             * */
            closeEndFlag (flag) {
                this.showEndFlag = false
                if (flag == 'cancel') {
                    //取消设置
                    return false
                } else {
                    //确认设置
                    this.submitBirthday()
                }
            },
            /**
             *
             * */
            changeLoginStatus () {
                this._axios()
            },
            submitBirthday() {
                let data = {
                    api: 'app.user.set_user',
                    birthday: this.birthday
                }

                this.$req.post({data}).then(res => {
                     this.fastTap = true
                    if (res && res.code == 200) {
                        this.mask_display = false
                        this.user_birth =  this.birthday
                        this.is_birth = true
                        
                        this.is_showToast1 = 1
                        this.is_showToast_obj1.imgUrl = this.sus
                        this.is_showToast_obj1.title = this.language.retrievepassword.setSus
                        setTimeout(()=>{
                            this.is_showToast1 = 0
                        },1000)
                    } else {
                        uni.showToast({
                            title: (res && res.message) || '操作失败，请稍后重试',
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                }).catch(() => {
                    this.fastTap = true
                    uni.showToast({
                        title: '网络异常，请稍后重试',
                        duration: 1500,
                        icon: 'none'
                    })
                })
            },
            /**
             *
             * */
            _axios () {
                var me = this
                var data = {
                    api: 'app.user.index',}
                this.$req.post({data}).then(res => {
                    if (res && res.code == 200 && res.data && res.data.data && res.data.data.user) {
                        const user = res.data.data.user
                        me.wx_withdraw = user.wx_withdraw
                        me.wx_name = user.wx_name
                        me.name = user.user_name
                        me.img = user.headimgurl
                        me.user_birth = user.birthday || '去设置'
                        if(user.mobile == 'null' || user.mobile == ''){
                            let language = uni.getStorageSync('language')
                            if(language == 'en_US'){
                                me.mobile = 'Not yet bound'
                            } else{
                                me.mobile = '未绑定'
                            }
                        } else {
                            me.cpc = user.cpc
                            me.mobile = user.mobile
                        }  
                        if(!user.e_mail){
                            let language = uni.getStorageSync('language')
                            if(language == 'en'){
                                me.email = 'Not yet bound'
                            } else{
                                me.email = '立即绑定'
                            }
                        } else {
                            me.email = user.e_mail
                        }
                      
                        me.zhanghao = user.zhanghao
                        me.userSetBr = user.is_default_birthday == 1?1:0
                        let language = uni.getStorageSync('language')
                        if(language == 'en_US'){
                            me.sex = user.sex == 1?'male':user.sex == 2?'female':'Go to Settings'
                        } else{
                            me.sex = user.sex == 1?'男':user.sex == 2?'女':'去设置'
                        }
                        me.$refs.refChooseS.is_choose_index = user.sex == 1?0:user.sex == 2?1:0
                        if (me.userSetBr) {
                            me.is_birth = false
                        } else {
                            me.is_birth = true
                        }

                    } else {
                        uni.showToast({
                            title: (res && res.message) || '加载失败，请稍后重试',
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                }).catch(() => {
                    uni.showToast({
                        title: '网络异常，请稍后重试',
                        duration: 1500,
                        icon: 'none'
                    })
                })
            },
            /**
             *
             * */
            _myname () {
                this.mask_display = true
                this.new_name = this.name
            },
            
            _myaddress() {
                uni.navigateTo({
                    url: '/pagesB/address/receivingAddress?state_manage=2&form=myInfo'
                });
            },
            /**
             *
             * */
            _confirm (sex) {
                if (this.new_name.length > 16) {
                    uni.showToast({
                        title: this.language.myinfo.nickname[0],
                        icon: 'none',
                        duration: 1000
                    })
                    return
                }
                if (!this.fastTap) {
                    return
                }
                this.fastTap = false
                let data = {
                    api: 'app.user.set_user',
                }
                if(sex){
                    data.sex = sex
                } else {
                    if(this.new_name.length == 0){
                        this.fastTap = true
                        uni.showToast({
                            title: '请输入需要修改的昵称',
                            icon: 'none',
                            duration: 1000
                        })
                        return
                    }
                    data.Nickname = this.new_name?this.new_name:this.name
                }
                this.$req.post({data}).then(res => {
                    this.fastTap = true
                    let {
                        code,
                        message
                    } = res
                    if (code == 200) {
                        if(!sex){
                            this.name =  this.new_name
                        }
                         this.mask_display = false
                         this._axios()
                         
                         this.is_showToast1 = 1
                         this.is_showToast_obj1.imgUrl = this.sus
                         this.is_showToast_obj1.title = this.language.zdata.xgcg
                         setTimeout(()=>{
                             this.is_showToast1 = 0
                         },2000) 
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
            },
            /**
             *
             * */
            _cancel () {
                this.mask_display = false
            },
            _seeImg(){
                //this.is_seeImg = !this.is_seeImg
                let imgsArray = [];
                imgsArray[0] = this.img? this.img : this.unLogin;
                uni.previewImage({
                    current: 0,
                    urls: imgsArray
                });
            },
            /**
             *
             * */
            choiceImg () {
                // #ifdef MP-WEIXIN
                uni.chooseMedia({
                    count: 1,
                    mediaType: ['image'],
                    success: res => {
                        this.uploadPictures(res.tempFiles[0].tempFilePath)
                    },
                    fail: err => {
                    }
                })
                // #endif
                
                // #ifndef MP-WEIXIN
                uni.chooseImage({
                    count: 1,
                    success: res => {
                        this.uploadPictures(res.tempFilePaths[0]) 
                    },
                    fail: err => {
                    }
                })
                // #endif
            },
            uploadPictures(res){
                let data = {
                    src_img: res,
                    api: 'app.user.set_user',
                }
                        
                this.$req.upLoad(res, data).then(res => {
                    let {
                        code,
                        message
                    } = res
                    
                    if (code == 200) {
                        uni.showToast({
                            title:  this.language.myinfo.upload_seccess,
                            duration: 1500,
                            icon: 'none'
                        })
                         this._axios()
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                }) 
            }
            
        }

    }
</script>
<style>
    page{
        height: 100vh;
        background-color: #F4F5F6;
    }
</style>

<style scoped lang="less">
    @import url("../../static/css/my/myInfo.less");
    p{
        font-size: 32rpx;
        color: #333333;
    }
    span{
        font-size: 32rpx;
        color: #333333;
    }
</style>
