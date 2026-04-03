<template>
    <view style="background-color: #f4f5f6;">
        <!-- 头部 -->
        <heads :title="language.myWallet.title" :ishead_w="3"></heads>
        <view style="height: 618rpx;">
            <view style="width: 100%;position: fixed;background-color: #f4f5f6;">
                <!-- 提现/充值 -->
                <view class="position" :style="bg_vip">
                    <view class="wallet_account">
                        <p class="wallet_ct">{{ LaiKeTuiCommon.getPriceWithExchangeRate(user_money|| 0, exchange_rate )}}</p>
                        <p class="wallet_xt">{{language.myWallet.money}}({{currency_symbol}})</p>
                        <view class="btn">
                            <view class="btn_left"  @tap="_tixian()" >{{language.myWallet.withdrawal}}</view>
                            <view class="btn_rigth" @tap="navTo('recharge?cide=1')">{{language.myWallet.recharge}}</view>
                        </view>
                    </view>
                </view>
                <!-- 明细导航 -->
                <view class="overflow-btn">
                    <view class="overflow-btn-box" @click="typeclick(0)">
                        <view :class="typetop==0?'overflow-btn-jymx':'overflow-btn-jymxl'">{{language.myWallet.type[0]}}</view>
                        <view v-if="typetop==0" class="overflow-btn-hx"></view>
                    </view>
                    <view class="overflow-btn-box" @click="typeclick(1)">
                        <view :class="typetop==1?'overflow-btn-jymx':'overflow-btn-jymxl'">{{language.myWallet.type[1]}}</view>
                        <view v-if="typetop==1" class="overflow-btn-hx"></view>
                    </view>
                    <view class="overflow-btn-box" @click="typeclick(2)">
                        <view :class="typetop==2?'overflow-btn-jymx':'overflow-btn-jymxl'">{{language.myWallet.type[2]}}</view>
                        <view v-if="typetop==2" class="overflow-btn-hx"></view>
                    </view>
                </view>
            </view>
        </view>
        <!-- 明细内容 -->
        <view style="background-color: #fff;border-radius: 0 0 24rpx 24rpx;">
            <ul class="overflow" ref="wallet">
                <li class="wallet_mx" v-for="(item, index) in list" :key="index" v-if="list.length>0" @click="godetail(item)">
                    <view>
                        <p class="wallet_value">{{ getType(item.type,item) }}</p>
                        <p class="wallet_size">{{ item.add_date }}</p>
                    </view>
                    <p :class="item.moneyType== 1  ? 'wallet_zhi':'hei'">
                        
                        <span
                            v-text="item.moneyType == 1  ? '+' : '-'"
                        ></span>
                        {{item.currency_symbol}}{{LaiKeTuiCommon.getPriceWithExchangeRate(item.money,item.exchange_rate)}}
                    </p>
                </li>
                <view class="nodata" v-else>
                    
                   <img class="wsj_img" :src="wushuju"/>
                    <p>{{ language.myFinance.noList }}</p>
                
                </view>
            </ul>

        </view>
        <view class="null"  v-if="list.length==0&&!loading">
                            <image class="null_img" :src="noShop"></image>
                            <view class="tips">
                                {{language.myWallet.tips}}
                            </view>
                        </view>
        <view class="load" style="height: 360rpx;" v-if="loading">
           
            <div>
                <img :src="loadGif" />
                <p>{{ language.toload.load }}</p>
            </div>
        </view>
        
        <uni-load-more v-if="list.length > 10" :loadingType="loadingType"></uni-load-more>
        <maskM :content="language.myWallet.qxtj" :display="display" @mask_value="onHandle"></maskM>
    </view>
</template>

<script>
import maskM from "@/components/maskM";
export default {
    data() {
        return {
            loadGif:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/5-121204193R7.gif",
            qbkp: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/qbkp.jpg',
            qbcz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/qianbaochongzhi.png',
            qbtx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/qianbaotixian.png',
            wushuju: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/wuzhangdan.png',
            noShop: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/noShop.png',
            //注：当前页面按照商城默认币种充值和后台保持一致：前端用户充值时候显示的充值方式，跟随后台商城设置的默认币种来改变 
            currency_code: this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE,
            currency_symbol: this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL,
            exchange_rate: this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE,
            
            title: '我的钱包',
            bg_vip: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                'images/icon/myyxbj.png)',
            list: '', //全部记录
            user_money: '',
            page: 2, //加载页面
            allLoaded: false,
            autoFill: false, //若为真，loadmore 会自动检测并撑满其容器
            bottomStatus: '',
            bottomPullText: '上拉加载更多...',
            bottomDropText: '释放更新...',
            loading: false,
            starts: '',
            unit: this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL,
            loadingType: 0,
            display: false,
            typetop:0,
        };
    },
    onShow () {
        this.loadingType = 0;
        this.list = [];
        this.page = 1;
        this.typetop = 0
        this.getUserCurrencyInfo();
        this._axios();
    },
    components: {
        maskM
    },
    onReachBottom() {
        if (this.loadingType != 0) {
            return;
        }
        this.loadingType = 1;
        var data = {
            api: 'app.user.wallet_detailed',
            page: this.page,
            type:this.typetop,
            pagesize:10
        };
        this.$req.post({ data }).then(res => {
            let { code, data: {list} } = res;
            this.page += 1
            if (list && list.length > 0) {
                this.list = this.list.concat(list);
                this.loadingType = 0;
            } else {
                this.loadingType = 2;
            }
        });
    },
    methods: {
        
        getUserCurrencyInfo(){
            let userCurrency = uni.getStorageSync('currency');
            //用户自己选择的货币符号
            this.currency_symbol = userCurrency.currency_symbol
            //汇率
            this.exchange_rate = userCurrency.exchange_rate
            //货币符号
            this.currency_code = userCurrency.currency_code;
        },
        
        godetail(e){
            this.navTo('/pagesB/myWallet/walletDetail?id='+e.details_id)
        },
        getType(e,item){//新改的type判断
            let name=''
            const {myWallet } =this.language
           if(e==1||e==2){
               name=myWallet.paymentType[0]
           }else if(e==3){
               name=myWallet.paymentType[1]
           }else if(e==4){
               name=myWallet.paymentType[2]
           }else if(e==5){
               name=myWallet.paymentType[3]
           }else if(e==6){
               name=myWallet.paymentType[4]
           }else if(e==7){
               name=myWallet.paymentType[5]
           }else if(e==8){
               name=myWallet.paymentType[6]
           }else if(e==9){
               name=myWallet.paymentType[7]
           }else if(e==10){
               name=myWallet.paymentType[8]
           }else if(e==11){
               name=myWallet.paymentType[9]
           }else if(e==12){
               name=myWallet.paymentType[10]
           }else if(e==42){
               name=myWallet.paymentType[11]
           }else if(e==13){
               name=myWallet.paymentType[12]
           }
            return name
        },
        typeclick(e){
            //初始化

            this.typetop = e
            this.page = 1
            this.loadingtype = 0
            this.list = []
            this.loading=true
            var data = {
                api: 'app.user.wallet_detailed',
                page: this.page,
                type:this.typetop,
                pagesize:10
            };
            console.dir(this.page)
            this.$req.post({ data }).then(res => {
                try{
                    let { code,message, data: {list} } = res;
                    this.loading=false
                    if(code != 200){
                        uni.showToast({
                            title:message,
                            icon:'none'
                        }) 
                        return
                    }
                    this.page += 1
                    this.list = this.list.concat(list);
                    console.dir(this.list)
                }catch(e){
                    this.loading=false
                     this.list= []
                    uni.showToast({
                        title:e.toString(),
                        icon:'none'
                    }) 
                    console.error(e)
                }
            });
        },
        onHandle (value) {
            if (value === '取消') {
                this.display = false;
            } else {
                this.display = false;
                this.navTo('/pagesB/setUp/changePhone?withdrawalNoPhone=1')
            }
        },
        handleBottomChange(status) {
            this.bottomStatus = status;
        },
        _axios() {
            let data = {
                api: 'app.user.details',};
            this.$req.post({ data }).then(res => {
                if (res.code == 200) {
                    let { data: {unit, user_money, list,status} } = res;
                    this.user_money = user_money;
                    this.starts = status;
                    this.unit = unit;
                    this.typeclick(0)
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            });
        },
        _tixian() {
            if (this.starts == 0) {
                let user_phone = uni.getStorageSync('user_phone')
                if (!user_phone) {
                    this.display = true;
                    return false;
                }
                
                uni.navigateTo({ url: 'putForward' });
            } else if (this.starts == 1) {
                uni.showToast({
                    title: this.language.myWallet.haswithdrawal,
                    duration: 1000,
                    icon: 'none'
                });
            } else if (this.starts == 2) {
                uni.showToast({
                    title: this.language.myWallet.qbpzbcz,
                    duration: 1000,
                    icon: 'none'
                });
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
@import url('../../static/css/myWallet/myWallet.less');
</style>
