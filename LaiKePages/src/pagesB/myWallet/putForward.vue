<template>
    <div class="box">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :bgColor='bgColor' :ishead_w="ishead_w" titleColor="#333333" :title="language.putForward.title"></heads>
        <ul class="message">
            <li
                style="border: none;align-items: baseline;border-bottom-right-radius:24rpx;border-bottom-left-radius:24rpx;">
                <view style="margin-top: 49rpx;width: 100%;">
                    <text style="color: #020202;font-size: 28rpx;margin-left: 5%;">{{language.putForward.withdrawal}}</text>

                    <view class="" style="margin-left: 5%;display: flex;margin-top: 49rpx;align-items: center;">
                        <text style="font-size: 28rpx;color: #333333;font-weight: 500;">{{currency_symbol}}</text>
                        <input style="color: #333333;font-size: 48rpx;font-family: peiyii-Bold, peiyii;" class="inputaa" placeholder-style="color: #b8b8b8;font-size:28rpx;" type="digit"
                            :placeholder="pshd" v-model="put_mon" @blur="_money" id="put_mon"
                            placeholder-class="put_mons" />
                        <text @tap="_put()" style="text-align: end; margin-right: 15px;font-size: 28rpx;color: #666666;">{{language.putForward.allBtn}}</text>
                    </view>

                    <view
                        style="margin-left: 5%;padding-bottom:0;width:92%;margin: 26rpx;border-top: 2rpx solid #E6E6E6;border-top: 1px solid rgb(230, 230, 230);display: flex;color: #999999;font-size: 22rpx;padding-top: 24rpx;">
                        <image :src="warnIng" style="width: 54rpx;height: 29rpx;margin-right: 10rpx;"></image>
                        <p>
                            {{language.putForward.minWithdrawal}}{{currency_symbol}}{{ min_amount }}，{{language.putForward.maxWithdrawal}}{{currency_symbol}}{{ max_amount }}，{{language.putForward.poundage}}{{ service_charge }}
                            ；{{language.walletDetail.txzwx}}
                            <!-- 最小提现金额￥100，最大提现金额￥500，提现手续费3%；提现至微信零钱如果提现金额大于￥2000需进行身份校验！-->
                        </p>
                    </view>
                </view>
            </li>
            
            <!-- 提现/手续费/到账 -->
            <view class="withdrawal-box">
                <view>
                    <text class="withdrawal-box-title">{{language.putForward.withdrawal}}</text>
                    <text class="withdrawal-box-num" style="margin-left:auto;">{{currency_symbol}}{{put_mon==''?'0.00':put_mon}}</text>
                </view>
                
                <view>
                    <text class="withdrawal-box-title">{{language.putForward.sxf}}</text>
                    <text class="withdrawal-box-num" style="margin-left:auto;">{{currency_symbol}}{{poundages.toFixed(2)}}</text>
                </view>
                
                <view>
                    <text class="withdrawal-box-title">{{language.putForward.dzje}}</text>
                    <text class="withdrawal-box-num" style="margin-left:auto;">{{currency_symbol}}{{put_mon==''?'0.00':receipt_mon.toFixed(2)}}</text>
                </view>
            </view>
            
            <!-- 提现方式：微信零钱/银行卡 -->
            <view class="txType-box">
                <view class="txType-box-title">{{language.walletDetail.txfs}}</view>
                <view>
                    <view :class="{active:chooseTxType==1}" v-if="currency_code == 'CNY'" @tap="_chooseTxType(1)">
                        <image :src="wx"></image><span>{{language.walletDetail.wxlq}}</span>
                    </view>
                    <view :class="{active:chooseTxType==2}" v-if="currency_code == 'CNY'" @tap="_chooseTxType(2)">
                        <image :src="yhk"></image><span>{{language.walletDetail.yhk}}</span>
                    </view>
                  <view :class="{active:chooseTxType==3}" @tap="_chooseTxType(3)" v-if="is_show && currency_code != 'CNY'">
                      <image :src="paypallogo"></image><span>PayPal</span>
                  </view>
                  <view :class="{active:chooseTxType==4}" @tap="_chooseTxType(4)" v-if="currency_code != 'CNY'">
                      <image :src="stripe_logo"></image><span>stripe</span>
                  </view>
                </view>
            </view>
            <!-- 提现至零钱 -->
            <template v-if="chooseTxType == 1 && currency_code == 'CNY'">
                <view class='add-wx-li' @tap="_setWx(wx_withdraw?2:1)">
                    <view>
                        <image :src="wx" style="width: 40rpx;height: 40rpx;border-radius: 50%;margin-right: 10rpx;"></image>
                        <p>{{language.walletDetail.wx}}</p>
                    </view>
                    <view>
                        <!-- 未绑定 -->
                        <span v-if="!wx_withdraw" style="color: #999999;">{{language.walletDetail.ljbd}}</span>
                        <!-- 已绑定 -->
                        <span v-else  style="color: #999999;">已绑定</span>
                        <img v-if="!wx_withdraw" class='arrow' :src="jiantou"/>
                    </view>
                </view>
                <li  style="border-bottom-left-radius: 24rpx;border-bottom-right-radius: 24rpx;height: 32rpx;" ></li>
            </template>
            <!-- 提现至银行卡 -->
            <template v-if="chooseTxType == 2 && currency_code == 'CNY'">
                <li class="add-bank-li" @click="navTo('/pagesB/myWallet/bankCardAdd?type=' + type)"
                    v-if="!bank_information.length" style="height: 50rpx;">
                    <view class="left_text" style="margin-left: 5%;">{{ language.distribution_form.bankcard }}</view>
                    <span class="placeholder">
                        {{ language.distribution_form.qcybr }}
                    </span>
                    <img :src="jiantou" alt="" />
                </li>
                <li class="add-bank-li"   @click="chooseClassFlagChange" v-if="bank_information.length">
                    <view class="left_text" style="margin-left: 5%;">{{ language.distribution_form.bankcard }}</view>
                    <view class="yuankuan">
                        <span class="placeholder" style="color: #333333;margin-left: 16rpx;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;width: 80%;">
                            {{ bankText}}
                        </span>
                        <img :src="jiantou" alt="" style="width: 32rpx;height: 44rpx;margin-right: 16rpx;"/>
                    </view>
                </li>
                <view v-if="!bank_information.length" style="width: 100%;background-color: #fff;"></view>
                <li v-if="bank_information.length" style="height: 100rpx">
                    <view class="left_text" style="margin-left: 5%;">{{language.distribution_form.tel}}&nbsp;&nbsp;</view>
                    <input type="number" :placeholder="language.distribution_form.telPlaceholder" v-model="phone"
                        disabled="true" readonly="readonly"
                        style="color: #999;width:232rpx;flex:0.94;background-color: #f4f5f6;border-radius: 12rpx;font-size: 32rpx;height: 74rpx;" />
                </li>
                <view style="width: 100%;background-color: #fff;"></view>
                <li v-if="bank_information.length" class="yz_li" style='height: 90rpx;'>
                    <span class="left_text" style="margin-left: 5%;">{{language.putForward.verification}}&nbsp;&nbsp;</span>
                    <view class="yuankuan">
                        <input placeholder-style="color: #b8b8b8;" class="input" :class="{ marginleft: display }" type="number"
                            :placeholder="language.putForward.verification_placeholder" v-model="input_code" style="flex: 1;color: #333333;font-size: 32rpx;" />
                
                        <!-- 如果没有符合规则的提现则不可点击 -->
                        <span class="color yz_span" v-if="display" :style="put_mon==''||put_mon==0?'pointer-events: none;cursor: not-allowed;color:#999':''" @tap="_verif" >{{language.putForward.getCode}}</span>

                        <span class="active" v-if="!display">
                            <template v-if="is_en == 'en'">
                                {{language.putForward.countdown}} {{ count }}s
                            </template>
                            <template v-else>
                                {{ count }}s{{language.putForward.countdown}}
                            </template>
                
                        </span>
                    </view>
                </li>
                <li  style="border-bottom-left-radius: 24rpx;border-bottom-right-radius: 24rpx;height: 32rpx;" ></li>
            </template>
			
			<!-- 提现至贝宝 -->
			<template v-if="chooseTxType == 3 && currency_code != 'CNY' ">
				<view class='add-wx-li' >
					<view>
						<image :src="paypallogo" style="width: 40rpx;height: 40rpx;border-radius: 50%;margin-right: 10rpx;"></image>
						<view>
							<input v-model="email" placeholder="请输入PayPall邮箱地址"  style="width: 600rpx;"/>
						</view>
					</view> 
				</view>
				<li  style="border-bottom-left-radius: 24rpx;border-bottom-right-radius: 24rpx;height: 32rpx;" ></li>
			</template>

			<template v-if="chooseTxType == 4 && currency_code != 'CNY' ">
				<view class='add-wx-li' >
					<view>
						<image :src="stripe_logo" style="width: 40rpx;height: 40rpx;border-radius: 50%;margin-right: 10rpx;"></image>
						<view>
							<input v-model="account_id" placeholder="请输入stripe account_id"  style="width: 600rpx;"/>
						</view>
					</view> 
				</view>
				<li  style="border-bottom-left-radius: 24rpx;border-bottom-right-radius: 24rpx;height: 32rpx;" ></li>
			</template>
		</ul>
        <div v-if="chooseClassFlag" ref="proClassPicker" class="mask" style="z-index: 999;" @tap="_closeClass" @touchmove.stop.prevent>
            <div style="border-top-left-radius: 24rpx;border-top-right-radius: 24rpx;" @tap.stop>
                <p class="mask_title"
                    style="background-color: #f4f5f6;border-top-left-radius: 24rpx;border-top-right-radius: 24rpx;">
                    <view class="mask_title_title">{{language.distribution_form.xzdz}}</view>
                </p>
                <scroll-view scroll-y class="chooseBox">
                    <ul>
                        <li :class="{ active1: active1 == index }" v-for="(item, index) in arrClass" :key="index"
                            :value="item.id" @tap="_chooseClass(item, index)" style="display: flex;">
                            {{ item.Bank_card_number }}
                            <img class="img" v-if="active1 == index" :src="chooseImg" />
                            <img class="img" v-if="active1 != index" :src="wxzaa" />
                        </li>
                    </ul>
                </scroll-view>
                <view class="chooseBtn">
                    <view class="chooseBtn_left" @click="navTo('/pagesB/myWallet/bankCardAdd?type=' + type)">{{language.delModel.tjxk}}</view>
                    <view class="chooseBtn_rigth" @tap="_chooseBtn">{{language.delModel.confirm}}</view>
                </view>
            </div>
        </div>
        <div v-if='txTrue' class="setup-buttom" @tap="_typeSumber">
            {{language.putForward.bottomBtn}}
        </div>
        <div v-else class="setup-buttom1">
            {{language.putForward.bottomBtn}}
        </div>
        
        <!-- 提示弹窗 身份校验-->
        <div class='mask-tx' v-if="mask_display">
            <div class='mask_cont-tx'>
                <p>{{language.walletDetail.sfjy}}</p>
                <input type="text" :placeholder="language.walletDetail.qsrzs" placeholder-style="color: #999999;" v-model="zsUserName"/>
                <div class='mask_button-tx'>
                    <div class='mask_button_left' @tap="mask_display=false">{{language.myinfo.cancel}}</div>
                    <div style="color: #D73B48;" @tap="_sumber('WX')">{{language.myinfo.determine}}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import {
        telephone
    } from '../../common/landing.js';
    import {
        mapMutations
    } from 'vuex';

    export default {
        data() {
            return {
                //注：当前页面按照商城默认币种充值和后台保持一致：前端用户充值时候显示的充值方式，跟随后台商城设置的默认币种来改变 
                currency_code: this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE,
                currency_symbol: this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL,
                exchange_rate: this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE,
                showPaypal:false,//注：由于贝宝不支持使用人民币收款，所以在商城使用人民币收款的时候不展示贝宝支付方式
                pshd: '',
                type: '',
                title: '提现申请',
                put_mon: '', //提现金额
                bank_number: '',
                bank_name: '',
                user_name: '',
                phone: '',
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                ishead_w: '2',
                chooseClass: [],
                input_code: '',
                arrClass: [],
                display: true,
                count: '',
                timer: '',
                code: '',
                bankText: '',
                one_code: '', //手机号码格式正确返回值
                max_amount: '', //提现的最大金额
                min_amount: '', //提现的最小金额
                bank_information: [], //用户银行卡历史信息
                mobile: '',
                oldPhone: '',
                chooseClassFlag: false,
                allComplete: false,
                fastTap: true,
                shop_id: '',
                money: '',
                chooseTxType: 1,//提现方式：1微信零钱 2银行卡
                wx_withdraw: false,//是否绑定微信 false/true
                wx_name: '',//绑定的微信名称
                zsUserName: '',//真实姓名
                mask_display: false,//提现金额大于等于2000时显示 校验真实姓名弹窗
                wx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wx.png',
                yhk: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yhk.png',
                chooseImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/chooseMeh.png',
                wxzaa: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wxzaa.png',
                warnIng: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/warnIng.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                closeImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/close2x.png',
				paypallogo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/beibao.png',
                stripe_logo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/stripe_logo.png',
                service_charge: '', //提现手续费
                id: '',
                text: '',
                is_en: '',
                back_type: '', // 1 从提现详情重新申请过来的
                branch: '',
                active1: 0,
                poundages: 0.00,//手续费
                receipt_mon:0.00,//到账金额
                serviceCharge: '',
				email:'',
                is_show:true,
                account_id:''
            };
        },
        computed: {
            txTrue(){ 
                if(this.chooseTxType == 1  ){
                    //提现金额>0,已经绑定了微信
                    if(this.put_mon > 0 && this.wx_withdraw){
                        return true
                    }
                } else if(this.chooseTxType == 2){
                    //提现金额>0,已经绑定了银行卡
                    if(this.put_mon > 0 && this.input_code.length == 6){
                        return true
                    }
                } else if(this.chooseTxType == 3){ 
                    // payPal支付
                    if(this.put_mon > 0 && this.email.length >= 6){
                        return true
                    }
                } else if(this.chooseTxType == 4){ 
                    if(this.put_mon > 0 && this.account_id.length >= 6){
                        return true
                    }
                } else {
                    return false
                }
            }
        },
        watch: {
            put_mon() {
                this.poundages = Number(this.put_mon) * Number(this.serviceCharge)
                this.receipt_mon=Number(this.put_mon)-this.poundages
            }
        },
        onLoad(option) {
            this.getUserCurrencyInfo();
            this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
            this.type = option.type;
            if (option.id) {
                this.id = option.id;
            }
            this.back_type = option.back_type
            this.is_wx()
        },
        onShow() {
            this.getUserCurrencyInfo();
            if (uni.getStorageSync('language')) {
                this.is_en = uni.getStorageSync('language')
            }
            this.isLogin(() => {
                this._axios();
            })
        },
        methods: {
            getUserCurrencyInfo(){
                let currency = uni.getStorageSync('currency');
                //用户自己选择的货币符号
                this.currency_symbol = currency.currency_symbol
                //汇率
                this.exchange_rate = currency.exchange_rate
                //货币符号
                this.currency_code = currency.currency_code;

            },
            is_wx() {
                let en = window.navigator.userAgent.toLowerCase()
                // 匹配en中是否含有MicroMessenger字符串
                this.is_show = en.match(/MicroMessenger/i) !== 'micromessenger'
            },
            /**
             * 提现方式
             * @param {Object} type 1微信零钱 2银行卡
             */
            _chooseTxType(type){
                switch (type){
                    case 1:
                        this.chooseTxType = 1 
                        break
                    case 2:
                        this.chooseTxType = 2
                        break
					case 3:
						this.chooseTxType = 3
						break
                    case 4:
						this.chooseTxType = 4
						break    
                    default:
                }
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
                        break
                    default:
                }
            },
            //去绑定微信
            _bdWx(){
                // #ifdef MP
                uni.navigateTo({
                    url: '/pagesB/setWx/wxMp?type=putForward'
                })
                // #endif
                // #ifdef H5
                this.h5_bangWX()
                // #endif
                // #ifdef APP-PLUS
                this.app_bangWX()
                // #endif
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
                    fail: function(er) {
                        uni.showToast({
                            title: err,
                            icon: 'none'
                        })
                    }
                });
            },
            getTimeNow(){
                const date = new Date();
                const year = date.getFullYear();
                const month = date.getMonth() + 1;
                const day = date.getDate();
                const hours = date.getHours();
                const minutes = date.getMinutes();
                const seconds = date.getSeconds();
                var time = year+'-'+month+'-'+day+' '+this.compareNumber(hours)+':'+this.compareNumber(minutes)+':'+this.compareNumber(seconds)
                    
                return time
            },
            compareNumber (num) {
                  return num < 10 ? '0' + num : num
            },
            _chooseBtn() {
                this.chooseClassFlag = false
                this.bankText = this.arrClass[this.active1].Bank_card_number
            },
            // 关闭类名选择
            _closeClass() {
                this.chooseClassFlag = false;

                this.chooseClass = this.chooseClass.filter(item => {
                    return item.pname != "请选择"
                })
            },
            chooseClassFlagChange() {
                this.chooseClassFlag = true
            },
            //选中类名
            _chooseClass(item, index) {
                this.active1 = index


            },
            changeLoginStatus() {
                this._axios();
            },
            _axios() {
                var data = {
                    api: 'app.user.into_withdrawals',
                    id: this.id
                };
                if (this.type == 'store') {
                    data.shop_id = this.shop_id;
                }

                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        let {
                            data: {
                                bank_information,
                                pshd,
                                max_amount,
                                min_amount,
                                money,
                                mobile,
                                service_charge,
                                total,
                                serviceCharge,
                                wx_withdraw,
                                wx_name
                            }
                        } = res;
                        this.wx_withdraw = wx_withdraw
                        this.wx_name = wx_name
                        this.max_amount = max_amount;
                        this.min_amount = min_amount;
                        this.bank_information = bank_information;
                        this.mobile = mobile;
                        this.bank_number = this.bank_information.Bank_card_number ? this.bank_information
                            .Bank_card_number : '';
                        this.phone = this.mobile ? this.mobile : uni.getStorageSync('user_phone');
                        this.serviceCharge = serviceCharge
                        this.arrClass = bank_information;
                        if (this.arrClass.length) {
                            // this.bankText = this.arrClass[0].Bank_card_number
                            this.arrClass.forEach((item,index)=>{
                                if(item.is_default==1){
                                    this.bankText = item.Bank_card_number
                                    this.active1=index
                                }
                            })
                        }


                        if (this.type == 'store') {
                            this.pshd = this.language.putForward.money_placeholder + (total ? total : 0)+this.language.open_renew_membership.yuan;
                        } else {
                            this.pshd = this.language.putForward.money_placeholder + (money ? money : 0)+this.language.open_renew_membership.yuan;
                        }

                        this.money = total || money;

                        this.service_charge = service_charge;
                    } else if (res.code == 115) {
                        this.isLogin(() => {})
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
            },
            back() {
                this.myforward('');
                this.mybacks('');
                uni.navigateBack({
                    delta: 1
                });
            },
            //全部提现
            _put() {
                var mon = Number(this.max_amount);
                var monr = Number(this.put_mon);


                if (this.money == 0) {
                    uni.showToast({
                        title: this.language.putForward.noWithdraw,
                        duration: 1000,
                        icon: 'none'
                    });
                } else if (this.money > this.max_amount) {
                    this.put_mon = this.max_amount;
                } else {
                    this.put_mon = this.money;
                }
            },

            //金额格式判断
            _money() {
                var mon = Number(this.put_mon);//输入的金额
                var monrys = Number(this.min_amount);//最小提现金额                
                if(mon==''){
                    return
                }else if (mon < monrys) {
                    mon = 0;
                    this.put_mon = mon.toFixed(2);
                    uni.showToast({
                        title: `${this.language.putForward.moneyTips[0]} ${monrys} ${this.language.putForward.moneyTips[1]}`,
                        duration: 1000,
                        icon: 'none'
                    });
                } else if (mon > this.max_amount) {
                    mon = 0;
                    this.put_mon = mon.toFixed(2);
                    uni.showToast({
                        title: this.language.putForward.moneyTips[3],
                        duration: 1000,
                        icon: 'none'
                    });
                } else if (mon > this.money) {
                    mon = 0;
                    this.put_mon = mon.toFixed(2);
                    uni.showToast({
                        title: `${this.language.putForward.moneyTips[2]} ${this.max_amount}`,
                        duration: 1000,
                        icon: 'none'
                    });
                }
                this.put_mon = mon.toFixed(2);
            },
            //银行卡匹配
            _bank() {
                var pattern = /^([1-9]{1})(\d{5})$/;
                if (pattern.test(this.bank_number)) {
                    var data = {
                        api: 'app.user.Verification',
                        Bank_name: typeof(this.bank_name) == "undefined" ? "" : this.bank_name,
                        Bank_card_number: typeof(this.bank_number) == "undefined" ? "" : this.bank_number
                    };

                    this.$req.post({
                        data
                    }).then(res => {
                        let {
                            code,
                            message
                        } = res;

                        if (code != 200) {
                            this.bank_number = '';
                            uni.showToast({
                                title: this.language.putForward.bankTip,
                                duration: 1000,
                                icon: 'none'
                            });
                        } else {
                            this.bank_name = res.data.Bank_name;
                        }
                    });
                }
                if (this.bank_number == '') {
                    this.bank_name = '';
                }
            },
            // 银行卡号输入完毕
            _bank_p() {
                var patt = /^[1-9]{1}\d{15}|\d{18}$/;

                if (this.bank_number.indexOf('*') > -1) {
                    return false;
                }

                if (!patt.test(Number.parseInt(this.bank_number))) {
                    uni.showToast({
                        title: this.language.putForward.bankpTips[0],
                        duration: 1000,
                        icon: 'none'
                    });
                } else {
                    var data = {
                        api: 'app.user.Verification',
                        Bank_name: this.bank_name,
                        Bank_card_number: this.bank_number
                    };

                    this.$req.post({
                        data
                    }).then(res => {
                        let {
                            code,
                            message
                        } = res;

                        if (code != 200) {
                            this.bank_number = '';
                            uni.showToast({
                                title: this.language.putForward.bankpTips[1],
                                duration: 1000,
                                icon: 'none'
                            });
                        } else {
                            this.bank_name = res.data.Bank_name;
                        }
                    });
                }
            },
            //持卡人验证
            _usname() {
                var name = /^[\u4e00-\u9fa5]{2,8}$/;
                if (!name.test(this.user_name)) {
                    this.user_name = '';
                    uni.showToast({
                        title: this.language.putForward.nameTip,
                        duration: 1000,
                        icon: 'none'
                    });
                }
            },
            /*   验证码发送1分钟倒计时     */
            _verif() {
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;
                this._telephone(this.phone);
                var me = this;
                var data = {
                    api: 'app.user.secret_key',
                    phone: this.phone,
                    message_type: 0, // 短信类型 0.验证码 1.短信通知
                    message_type1: 6 // 短信类别 6.提现
                };
                if (this.phone) {
                    this.$req
                        .post({
                            data
                        })
                        .then(res => {
                            uni.showToast({
                                title: '发送成功',
                                duration: 1000,
                                icon: 'none'
                            })
                            this.fastTap = true;
                            let {
                                code,
                                message
                            } = res;
                            if (code != 200) {
                                uni.showToast({
                                    title: message,
                                    duration: 1000,
                                    icon: 'none'
                                });
                            } else {
                                this._time();
                                this.one_code == 1;
                                this.oldPhone = this.phone;
                            }
                        })
                        .catch(error => {
                            this.fastTap = true;
                        });
                } else {
                    this.fastTap = true;
                    uni.showToast({
                        title: this.language.putForward.telTip,
                        duration: 1000,
                        icon: 'none'
                    });
                }
            },
            //1分钟倒计时
            _time() {
                const TIME_COUNT = 60;
                if (!this.timer) {
                    this.display = false;
                    this.count = TIME_COUNT;
                    this.timer = setInterval(() => {
                        if (this.count > 0 && this.count <= TIME_COUNT) {
                            this.count--;
                        } else {
                            this.display = true;
                            clearInterval(this.timer);
                            this.timer = null;
                        }
                    }, 1000);
                }
            },
            //手机号码正则验证
            _telephone(value) {
                this.one_code = telephone(value);
            },
            //校验提现方式
            _typeSumber(){
                //chooseTxType 1 提现微信零钱
                if(this.chooseTxType == 1){
                    //提现金额大于等于2000 需要输入真实姓名
                    if(Number(this.put_mon)>=2000){
                        this.mask_display = true
                    } else {
                        this._sumber('WX')
                    }
                }
                //chooseTxType 2 提现银行卡
                if(this.chooseTxType == 2){
                    if (!this.bank_information.length) {
                        uni.showToast({
                            title: this.language.toasts.distribution_center.lkt_submit[4],
                            duration: 1000,
                            icon: 'none'
                        })
                        this.fastTap = true
                        return false;
                    }
                    this._sumber()
                }
				// 贝宝支付
				if(this.chooseTxType == 3){
				    if (!this.email.length) {
				        uni.showToast({
				            title: '请输入充值账号',
				            duration: 1000,
				            icon: 'none'
				        })
				        this.fastTap = true
				        return false;
				    }
				    this._sumber()
				}
				// stripe支付
				if(this.chooseTxType == 4){
				    if (!this.account_id.length) {
				        uni.showToast({
				            title: '请输入stripe account_id',
				            duration: 1000,
				            icon: 'none'
				        })
				        this.fastTap = true
				        return false;
				    }
				    this._sumber()
				}                
            },
            //提交
            _sumber(tjType) {
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;
                let patt = /^([1-9]{1})(\d{15}|\d{18})$/
                let bank_id = this.arrClass[this.active1]?this.arrClass[this.active1].id:''
				
                if (this.put_mon) {
                    var data = {
                        api: 'app.user.withdrawals',
                        bank_id: bank_id,
                        amoney: this.put_mon,
                        Bank_name: this.bank_name,
                        Bank_card_number: this.bank_number,
                        Cardholder: this.user_name,
                        mobile: this.phone,
                        keyCode: this.input_code,
                        id: this.id,
                        branch: this.branch,
                        withdrawStatus: this.chooseTxType==1?2:1 ,//提现类型 1银行卡 2微信余额 3 贝宝提现 4 stripe提现
                        
                        userName: this.zsUserName,
                    };
					if(this.chooseTxType == 3){
						data.withdrawStatus = 3
						data.email = this.email
					}
                    if(this.chooseTxType == 4){
						data.withdrawStatus = 4
						data.account_id = this.account_id
					}
                    
                    if (this.type == 'store') {
                        data.shop_id = this.shop_id;
                    }

                    this.$req
                        .post({
                            data
                        })
                        .then(res => {
                            if (res.code == 200) {
                                uni.showToast({
                                    title: this.language.putForward.tixianTip,
                                    duration: 1000,
                                    icon: 'none'
                                });
                                setTimeout(() => {
                                    this.fastTap = true;
                                    let id_catd = this.arrClass[this.active1]?this.arrClass[this.active1].Bank_card_number:''
                                    let url = 'rechargeSuccess?id_name=' + this.bank_name + '&id_catd=' +
                                        id_catd + '&id_monsy=' + this.put_mon + '&store=true' + '&poundages=' + 
                                        this.poundages+'&time=' + this.getTimeNow()
                                    if (this.back_type) {
                                        url += '&back_type=' + this.back_type
                                    }
                                    //微信零钱提现
                                    if(tjType == 'WX'){
                                        url += '&tjType=WX'
                                    }else if(this.chooseTxType ==3){
                                        url += '&tjType=paypal'
                                    }else if(this.chooseTxType ==4){
                                        url += '&tjType=stripe'
                                    }
                                    uni.redirectTo({
                                        url: url
                                    });
                                }, 1000);
                            } else {
                                this.fastTap = true;
                                uni.showToast({
                                    title: res.message,
                                    duration: 1000,
                                    icon: 'none'
                                });
                            }
                        })
                        .catch(error => {
                            this.fastTap = true;
                        });
                } else if (this.one_code != 1) {
                    uni.showToast({
                        title: this.language.putForward.yzmTip,
                        duration: 1000,
                        icon: 'none'
                    });
                    this.fastTap = true;
                } else if (this.oldPhone != this.phone) {
                    uni.showToast({
                        title: this.language.putForward.telTip1,
                        duration: 1000,
                        icon: 'none'
                    });
                    this.fastTap = true;
                } else {
                    setTimeout(() => {
                        uni.showToast({
                            title: this.language.putForward.wzTip,
                            duration: 1000,
                            icon: 'none'
                        });
                        this.fastTap = true;
                    }, 1500);
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
<style scoped lang="less">
    @import url("@/laike.less");
    @import url('../../static/css/myWallet/putForward.less');
    .mask-tx {
        background-color: rgba(000, 000, 000, 0.5);
        position: fixed;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        z-index: 999;
        display: flex;
        justify-content: center;
        align-items: center;
        .mask_cont-tx {
            width: 640rpx;
            text-align: center;
            font-size: 32rpx;
            color: #020202;
            background-color: #fff;
            border-radius: 22rpx;
        }
        .mask_cont-tx > p {
            margin: 64rpx 0 32rpx 0;
            color: #333333;
        }
        .mask_cont-tx > input {
            width: 544rpx;
            height: 66rpx;
            margin: 0 auto;
            margin-bottom: 62rpx;
            border-bottom: 1px solid rgba(0, 0, 0, 0.1);
            text-align: left;
        }
        .mask_button-tx {
            border-top: 1px solid rgba(0, 0, 0, 0.1);
            font-size: 32rpx;
        }
        .mask_button-tx > div {
            float: left;
            width: 50%;
            padding: 32rpx 0;
            color: #020202;
        }
        .mask_button-tx:after {
            display: block;
            content: "";
            clear: both;
        }
        .mask_button_left {
            border-right: 1px solid #eee;
            color: #333333 !important;
        }
    }
    .add-wx-li{
        width: 100%;
        padding: 0 32rpx;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 32rpx;
        background-color: #fff;
        >view{
            display: flex;
        }
    }
    .txType-box{
        background: #fff;
        width:100%;
        padding: 24rpx 32rpx 40rpx;
        box-sizing: border-box;
        border-radius: 24rpx 24rpx 0 0;
        overflow: hidden;
        // display: flex;
        // align-items: center;
        // justify-content: space-between;
		
        .txType-box-title{
			margin-bottom: 20rpx;
            font-size: 32rpx;
            color: #333333;
        }
        >view:last-child{
            display: flex;
            align-items: center;
            // justify-content: space-between;
            >view{
                width: 236rpx;
                height: 76rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                border: 2rpx solid #E5E5E5;
                margin-left: 26rpx;
                border-radius: 16rpx;
                >image{
                    width: 44rpx;
                    height: 44rpx;
                    border-radius: 50%;
                    margin-right: 8rpx;
                }
                >span{
                    font-size: 32rpx;
                    color: #666666;
                }
            }
            >view:first-child{
                margin-left: 0;
            }
            .active{
                border: 2rpx solid #FA5151;
                >span{
                    color: #333333;
                }
            }
        }
    }
</style>
