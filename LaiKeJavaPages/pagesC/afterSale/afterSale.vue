<template>
    <div ref="box">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>

        <heads :title="language.afterSale.title" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>

        <div class="allgoods_s home_navigation yh-home_navigation">
            <div class="home_input yh-home_input"  @click.stop>
                <img class="searchImg" :src="serchimg" alt="" />
                <input type="text" v-model="searchtxt" id="input" :placeholder="language.afterSale.search_placeholder" placeholder-style="color:#999999;font-size: 26upx" name="sourch" />
                <image v-show="searchtxt.length > 0" @click="cleardata" class="cancel" :src="sc_icon" mode=""></image>
            </div>
            <div class="yh_seart" @tap="_seart">{{language.afterSale.search_btn}}</div>
        </div>

        <!--   订单列表     -->
        <template v-if="display">
            <ul class="order_goods" v-for="(item, index) in list" :key="index">
                <li class="order_one">
                    <p @tap="_goStore(item.shop_id)" class="shopName">
                        <!-- 禅道50610 -->
                        <img :src="item.headImg?item.headImg:ErrorImg" @error="handleError(index)"/>
                        {{ item.ismch ? '' : item.shop_name }}
                        <img :src="jiantou" class="yh-jiantou" />
                    </p>

                    <p class="yh-prompt">{{ item.prompt }}</p>
                </li>

                <li class="order_two" @click="_uni_navigateTo( '../../pagesC/afterSale/afterDetail?order_id=' + item.detailId + '&status=' + item.r_status + '&id=' + item.id + '&pid=' + item.pid)">
                    <img :src="item.imgurl?item.imgurl:ErrorImg"  class="storeImg" @error="handleError2(index)"/>
                    <div class="yh-order_two goodsTitle">
                        <p class="order_p_name" style="height: auto;">{{ item.p_name }}</p>
                        <p class="color_one ellipsis-text">{{ item.size }}</p>
                        <div class="commodity-price">
                            <p><span class="priceImg">{{ item.currency_symbol }}</span>{{ LaiKeTuiCommon.getPriceWithExchangeRate(item.p_price,item.exchange_rate)  }} <text v-if="type =='IN'">积分</text></p>
                            <!-- <p class="color_two">x{{ item.num }}</p> -->
                            
                            <p class="commodity-price-integral">共 {{ item.num }} {{ item.unit }}</p>
                        </div>
                    </div>
                    
                </li>
                <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
                    <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                        <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                            <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                        </view>
                        <view class="xieyi_title"
                            style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                            撤销成功
                        </view>
                    </view>
                </view>
                <view class="xieyi mask" style="background-color: initial;" v-if="is_susa">
                    <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                        <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                            <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                        </view>
                        <view class="xieyi_title"
                            style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                            收货成功
                        </view>
                    </view>
                </view>
                <li class="order_last yh-order_last">
                    <div style="display: flex;align-items: center;">
                        <img v-if="item.re_type == 2 || item.r_type == 4 || item.r_type == 9|| item.r_type == 15" :src="after_tk" class="after_icon">
                        <img v-if="item.re_type == 3 || item.r_type == 12  && item.r_type != 4" :src="after_hh" class="after_icon">
                        <img v-if="item.re_type == 1 && item.r_type != 4 "  :src="after_th" class="after_icon">

                        {{ item.re_type == 1 ? language.afterSale.return_types[0] : item.re_type == 2 ? language.afterSale.return_types[1] : item.re_type == 3 && item.r_type == 12 ? language.afterSale.return_types[2] : language.afterSale.return_types[3] }}
                        {{ item.r_type == 4 || item.r_type == 9|| item.r_type == 15 ? language.afterSale.return_end : '' }}
                    </div>
                    <div class="last_right">
                        <div
                        class="afterDetail"
                            @click="_uni_navigateTo( '../../pagesC/afterSale/afterDetail?order_id=' + item.detailId + '&status=' + item.r_status + '&id=' + item.id + '&pid=' + item.pid)">
                            {{language.afterSale.details}}
                        </div>
                        <div v-if="item.r_type == 0"
                            @click="
                                _Cancellation_of_application(item.id)
                            "
                        >
                            {{language.afterSale.cancelAudit}}
                        </div>
                        <div v-if="item.r_type == 11" @click="ok_order(item)" style="margin-left:22rpx">{{language.afterSale.receipt}}</div>
                        <div v-if="(item.re_type == '3' || item.re_type == '1')  && ( item.r_type == 1 ? true : item.r_type == 3 || item.r_type == 11 ? language.afterSale.return_text[2] : false ) " ref="bottom" @tap="_logistics(item.r_type, item.id, item.sNo, item.pid)">

                            {{ item.r_type == 1 ? language.afterSale.return_text[1] : item.r_type == 3 || item.r_type == 11 ? language.afterSale.return_text[2] : '' }}
                        </div>
                    </div>
                </li>

                <!-- <li class="yh-line"></li> -->
            </ul>

            <uni-load-more v-if="list.length > 8" :loadingType="loadingType"></uni-load-more>
        </template>

        <div v-if="!display">
            <div class="noFindDiv yh-noFindDiv">
                <div><img class="noFindImg" :src="noOrder" /></div>
                <span class="noFindText">{{language.order.myorder.no_order}}</span>
                <div @tap="navToHome()" class="yh-toHome"><span class="goHome">{{language.order.myorder.go_shopping}}</span></div>
            </div>
        </div>

        <!--   订单列表     -->
        <div class="load" v-if="load">
            <div>
                <img :src="loadImg" />
                <p>{{language.afterSale.loding}}…</p>
            </div>
        </div>

        <!--  物流信息  -->
        <div class="mask" v-if="logistics_display">
            <div class="logistics" v-if="logistics_f">
                <img @tap="_close" class="logistics_img" :src="guanbi_img" />
                <div class="logistics_top">
                    <p class="logistics_head">{{language.afterSale.information}}</p>
                    <p>{{language.afterSale.return_address}}：{{ address.address }}</p>
                    <p>{{language.afterSale.return_contact}}：{{ address.name }}</p>
                    <p>{{language.afterSale.return_phone}}：{{ address.phone }}</p>
                </div>
                <div class="yh-lines"></div>
                <ul class="message">
                    <li class="flex" @tap="showSinglePicker">
                        <span>{{language.afterSale.courierName}}</span>
                        <div>
                            <input type="text" disabled="true" :placeholder="language.afterSale.select_express" v-model="express_name" />
                        </div>
                        <img class="arrow" :src="jiantou_img"/>
                    </li>
                    <li>
                        <span>{{language.afterSale.courierNumner}}</span>
                        <input type="text" class="input" :placeholder="language.afterSale.courierNumner_placeholder" v-model="express_number" maxlength="30"/>
                    </li>
                    <li>
                        <span>{{language.afterSale.contact}}&nbsp;&nbsp;&nbsp;</span>
                        <input type="text" class="input" :placeholder="language.afterSale.contact_placeholder" v-model="contacts" />
                    </li>
                    <li>
                        <span>{{language.afterSale.phone}}</span>
                        <input type="number" :placeholder="language.afterSale.phone_placeholder" v-model="contacts_phone" />
                    </li>
                </ul>
                <div class="logistics_but" @tap="_but">{{language.afterSale.submit}}</div>
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
            </div>

            <div class="logistics" v-if="logistics_name">
                <div class="express_head">
                    <div class="yh-express_btn" @tap="_cancel">{{language.afterSale.cancel}}</div>
                    <div class="yh-express_btn" @tap="_confirm">{{language.afterSale.confirm}}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import Heads from '../../components/header.vue';
    import mpvuePicker from '../../components/mpvuePicker.vue';
    import {telephone} from '../../common/landing.js';

    export default {
        data() {
            return {
                searchtxt: '',
                title: '退款/售后',
                themeColor: '#D73B48',
                mode: '',
                deepLength: 1,
                pickerValueDefault: [0],
                pickerValueArray: [],
                is_sus: false, //提示
                is_susa: false, //提示
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/noOrder.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + '/images/icon/jiantou2x.png',
                storeImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_store.png',
                back_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + '/images/icon1/back2x.png',
                loadImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + '/images/icon1/loading.gif',
                bback_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + '/images/icon1/bback.png',
                search_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + '/images/icon1/search2x.png',
                guanbi_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + '/images/icon1/guanbi2x.png',
                empyimg_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + '/images/icon1/empyimg92x.png',
                jiantou_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + '/images/icon1/jiantou2x.png',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                // after_tk: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + '/images/icon1/after_tk.png',
                // after_th: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + '/images/icon1/after_th.png',
                after_tk: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/tuikuan2x.png',
                after_th: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/tuihuo2x.png',
                after_hh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/huanhuo.png',
                
                sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/delete2x.png',
                head: true, //头部切换
                header: ['全部', '待付款', '待发货', '待收货', '待评价'],
                sreach_value: '',
                express_name: '',
                express_number: '',
                contacts: '',
                contacts_phone: '',
                slots: [
                    {
                        values: [],
                        textAlign: 'center'
                    }
                ],

                list: [], //订单
                display: true,
                logistics_m: '', //是否填写物流信息
                logistics_display: false,
                logistics_name: false,
                logistics_f: true,
                logistics_re: '',
                address: '',
                phone_next: '', //电话号码验证，格式正确为1
                oid: '', //订单详情id
                flag: true, //头部颜色
                show: false,
                text: ['退换中', '寄回商品', '查看物流'],
                fastTap: true,
                page: 1,
                load: true,
                loadingType: 0,
                type: '',
                bgColor: [
                    {
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
            };
        },
        onLoad(option) {
            if (option.searchtxt) {
                this.searchtxt = option.searchtxt;
            }
            if (option.type) {
                this.type = option.type;
            }
        },
        watch: {
            'express_number': {
                handler() {
                    
                    this.$nextTick(function() {
                       let str = this.express_number
                       this.express_number = str.replace(/[^\w\.\/]/ig,'')
                    })
                   
                    
                },
                
            },
            'contacts': {
                handler() {
                    let str = this.contacts
                    this.contacts = str.replace(/\s+/g,'')
                },
                deep: true,
                
            }
        },
        onShow() {
            this.page = 1;
            this.list = [];
            this.loadingType = 0;
            this.fastTap = true;
            this._axios('onShow');
        },
        methods: {
            handleError(index){
                this.list[index].headImg = this.ErrorImg
            },
            handleError2(index){
                this.list[index].imgurl = this.ErrorImg
            },
            cleardata(){
                this.searchtxt = ''
            },
            /* 
                    跳转客服
                */
            kf(pro_id) {
                this.isLogin(()=>{
                    uni.navigateTo({
                        url: '/pagesB/message/service?pid=' + pro_id
                    });
                })
            },
            /**
             *
             * */
            changeLoginStatus() {
                this._axios();
            },
            /**
             *
             * */
            _seart() {
                
                this.page = 1;
                this.list =[];
                this.loadingType = 0;
                this._axios()

            },
            /**
             *
             * */
            ok_order(item) {
                let data = {
                    
                    api: 'app.UserReturn.confirm_receipt',
                    id: Number.parseInt(item.id),
                    r_type: 11
                };
                this.$req.post({data}).then(res => {
                    let { code } = res;
                    if (code == 200) {
                        
                        this.is_susa = true
                        setTimeout(()=>{
                            this.is_susa = false
                        },1000)
                        setTimeout(() => {
                            this.page = 1;
                            this.list = [];
                            this.loadingType = 0;
                            this._axios();
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
            /**
             *
             * */
            showSinglePicker() {
                this.show = true;
                this.mode = 'selector';
                this.deepLength = 1;
                this.pickerValueDefault = [0];
                
                setTimeout(()=>{
                    this.$refs.mpvuePicker.show()
                },10)
            
            },
            /**
             *
             * */
            onConfirm(e) {
                this.express_name = e.label;
                this.show = false;
            },
            /**
             *
             * */
            _uni_navigateTo(url) {
                uni.navigateTo({
                    url: url
                });
            },
            _Cancellation_of_application(id) {
                // return
                var data = {
                    api: 'app.order.Cancellation_of_application',
                    id: id
                };
                
                this.$req.post({ data }).then(res => {
                   
                    this.page = 1;
                    this.list = [];
                    this.loadingType = 0;
                   if(res.code == 200){
                       this.is_sus = true
                       setTimeout(()=>{
                           this.is_sus = false
                       },1000)
                       this._axios()
                   }
                });
            },
            /**
             *
             * */
            _homes() {
                uni.redirectTo({
                    url: '/pages/shell/shell?pageType=home'
                });
            },
            /**
             *
             * */
            _back() {
                this.flag = false;
                uni.navigateBack({
                    delta: 1
                });
            },
            /**
             * 头部切换
             * */
            _head() {
                this.head = !this.head;
            },
            /**
             * 物流公司选择
             * */
            onValuesChange(picker, values) {
                this.logistics_re = values[0];
            },
            /**
             *
             * */
            _goStore(shop_id) {
                uni.navigateTo({
                    url: '/pagesB/store/store?shop_id=' + shop_id
                });
            },
            /**
             * 寄回商品或查看物流
             * */
            _logistics(index, id, r_sNo, pid) {
                this.oid = id;
                if (index == 1) {
                    //同意寄回商品
                    if (!this.fastTap) {
                        return;
                    }
                    this.fastTap = false;
                    
                    uni.navigateTo({
                        url: '../../pagesC/afterSale/writeLogistics?pid='+pid+'&oid='+this.oid 
                    });
                    
                } else if (index == 3 || index == 11) {
                    //用户已发货
                    //跳转物流页面
                    this.fastTap = true;
                    let data = {
                        api: 'app.order.logistics',
                        // id: r_sNo,
                        id: id,
                        o_source: 2,
                        type: ''
                    };

                    if (this.source == 1) {
                        data.type = 'pond';
                    }

                    this.$req.post({ data }).then(res => {
                        uni.hideLoading();
                        if (res.code == 200) {
                            let data = res.data;
                            if (data.list.length > 1 || data.isPackage) {
                                uni.navigateTo({
                                    url: '../../pagesB/expressage/expressage?sNo=' + r_sNo + '&id=' + id + '&o_source=2'
                                });
                            } else {
                                uni.navigateTo({
                                    url: '../../pagesC/expressage/expressage?list=' + JSON.stringify(data.list[0]) + '&sNo=' + r_sNo
                                });
                            }
                        } else {
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            });
                        }
                    });
                }
            },
            /**
             * 确认
             * */
            _confirm() {
                this.logistics_f = true;
                this.logistics_name = false;
                this.express_name = this.logistics_re;
            },
            /**
             * 取消
             * */
            _cancel() {
                this.logistics_f = true;
                this.logistics_name = false;
                this.express_name = this.express_name;
            },
            /**
             *
             * */
            _close() {
                this.logistics_display = false;
            },
            /**
             * 提交
             * */
            _but() {
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;
                if(this.express_name == ''){
                    this.fastTap = true;
                    uni.showToast({
                        title: this.language.afterSale.select_express,
                        duration: 1000,
                        icon: 'none'
                    });
                } else if(this.express_number == ''){
                    this.fastTap = true;
                    uni.showToast({
                        title: this.language.afterSale.courierNumner_placeholder,
                        duration: 1000,
                        icon: 'none'
                    });
                } else if(this.contacts == ''){
                    this.fastTap = true;
                    uni.showToast({
                        title: this.language.afterSale.contact_placeholder,
                        duration: 1000,
                        icon: 'none'
                    });
                } else if(this.contacts_phone.length != 11){
                    this.fastTap = true;
                    uni.showToast({
                        title: this.language.afterSale.phone_allright,
                        duration: 1000,
                        icon: 'none'
                    });
                }
                if (this.express_name && this.express_number && this.contacts && this.contacts_phone.length == 11) {
                    //发送请求
                    let data = {
                        api: 'app.order.back_send',
                        kdcode: this.express_number,
                        kdname: this.express_name,
                        lxdh: this.contacts_phone,
                        lxr: this.contacts,
                        id: this.oid,

                    };

                    this.$req.post({ data }).then(res => {
                        this.fastTap = true;
                        let { code, message } = res;
                        if (code == 200) {
                            this.logistics_display = false;
                            uni.showToast({
                                title: this.language.afterSale.submitOk,
                                duration: 1000,
                                icon: 'none'
                            });
                            setTimeout(() => {
                                this.express_number = '';
                                this.express_name = '';
                                this.contacts_phone = '';
                                this.contacts = '';
                                this.page = 1
                                this.list = [];
                                this.loadingType = 0;

                                this._axios();
                            }, 1000);
                        } else {
                            uni.showToast({
                                title: message,
                                duration: 1000,
                                icon: 'none'
                            });
                        }
                    });
                }
            },

            /**
             * 手机号码验证
             * */
            _telephone() {
                this.phone_next = telephone(this.contacts_phone);
                if (!this.phone_next) {
                    this.contacts_phone = '';
                }
            },
            /**
             *
             * */
            _axios(type = '') {
       
                var limit = 0;
                if (type == 'onShow' && this.page != 1) {
                    limit = this.page * 10;
                }
                var data = {
                    api: 'app.order.ReturnDataList',
                    page: this.page,
                    keyword: this.searchtxt,
                    limit: limit
                };
                if(this.type == 'FX'){
                    data.order_type = this.type
                }
                if(this.type == 'MS'){
                    data.order_type = this.type
                }
                if(this.type == 'yushou'){
                    data.type = 'PS'
                }
                if(this.type == 'FS'){
                    data.type = 'FS'
                }
                if(this.type == 'ZB'){
                    data.type = 'ZB'
                }
                if(this.type == 'IN'){
                    data.type = 'IN'
                }
                this.$req.post({ data }).then(res => {
                    if (res.code == 200) {
                        let list = res.data.list
                        if(this.page == 1){
                            this.list = list
                        } else {
                            this.list.push(...list)
                        }
                        if (list.length > 0) {
                            this.display = true;
                            this.load = false;
                        } else {
                            this.display = false;
                            this.load = false;
                        }
                        if (list.length < 10) {
                            this.loadingType = 2
                        } else {
                            this.loadingType = 0
                        }
                        // this.searchtxt = ''
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
            }
        },
        onReachBottom() {
            if (this.loadingType != 0) {
                return;
            }
            this.loadingType = 1;
            this.page ++;
            this._axios()
        },
        components: {
            mpvuePicker,
            Heads
        }
    };
</script>

<style>
    page{
        background: #F4F5F6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url('../../static/css/afterSale/afterSale.less');
</style>
