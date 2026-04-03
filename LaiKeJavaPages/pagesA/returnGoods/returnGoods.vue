<template>
    <div>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.returnGoods.title" :bgColor="bgColor" titleColor="#333333" ishead_w="2" ></heads>
        
        <ul class="order_goods">
            <li class="order_two" v-for="(item, index) in order" :key="index"
                @tap="_goods(item.p_id,item.recycle,item.pluginId)"
            >
                <img :src="item.image?item.image:ErrorImg" @error="handleErrorImg(index)" class="shopImg"/>
                
                <div class="order_two_a" >
                    <p class="order_p_name">
                        {{ item.p_name }}
                    </p>
                    <view class="price_num">
                        <p class="price">
                            <span v-if="orderType == 'IN'">积分{{ order[0].scoreDeduction }} + </span>
                            <span class="price_symbol">{{currency_symbol}}</span>
                            {{LaiKeTuiCommon.getPriceWithExchangeRate(Number(item.p_price),exchange_rate)}}
                        </p>                      
                        <p class="color_two">x{{ item.num }}</p>
                    </view>
                    <p class="color_one">{{ item.size }}</p>
                </div>
               
            </li>
        </ul>
 
        <ul>
            <!-- 仅退款 -->
            <li
                class="return_pay" v-if="refundAmt && orderType!='MS' && orderType!='FX'"
                @tap="_uni_navigateTo('refund?refund_type=2&order_details_id=' + order_details_id + '&order_anking=' + order_anking + '&rType=' + rType+'&isbatch='+isbatch+'&otype='+otype)"
            >
                <div class="return_right">
                    <div>
                        <img class="return_right_img" :src="tuikuan_img" />
                        <span>{{language.returnGoods.only}}</span>
                    </div>
                    <p>{{language.returnGoods.Tips[0]}}</p>
                </div>
                <img class="arrow" :src="jiantouhei_img" />
            </li>
            <!-- 退货退款 -->
            <li class="return_pay" v-if="refundGoodsAmt  && orderType!='MS' && orderType!='PM' && orderType!='FX'" @tap="_uni_navigateTo('refund?refund_type=1&order_details_id=' + order_details_id + '&rType=' + rType+'&isbatch='+isbatch)">
                <div class="return_right">
                    <div>
                        <img class="return_right_img" :src="tuihuo_img" />
                        <span>{{language.returnGoods.Return_refund}}</span>
                    </div>
                    <p>{{language.returnGoods.Tips[1]}}</p>
                </div>
                <img class="arrow" :src="jiantouhei_img" />
            </li>
            <!-- 换货 -->
            <li
                class="return_pay"
                v-if="refundGoods"
                @tap="_uni_navigateTo('refund?refund_type=3&order_details_id=' + order_details_id + '&order_anking=' + order_anking + '&r_status=' + r_status + '&rType=' + rType+'&isbatch='+isbatch)"
            >
                <div class="return_right">
                    <div>
                        <img class="return_right_img" :src="huanhuo_img" />
                        <span>{{language.returnGoods.exchange}}</span>
                    </div>
                    <p>{{language.returnGoods.Tips[2]}}</p>
                </div>
                <img class="arrow" :src="jiantouhei_img" />
            </li>
        </ul>
    </div>
</template>

<script>
export default {
    data() {
        return {
            title: '请选择售后类型',
            order_details_id: '',
            ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',

            tuihuo_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/tuihuo2x.png',
            jiantouhei_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            tuikuan_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/tuikuan2x.png',
            huanhuo_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/huanhuo.png',
            
            order: '',
            order_anking: '', // 批量申请还是单个申请
            rType: false,
            status: '',
            self_lifting: '',
            r_status: '',
            otype: '',
            refundAmt:false,
            refundGoodsAmt:false,
            refundGoods:false,
            currency_symbol: '￥',//数据
            exchange_rate: 1,//数据
            isbatch: false,
            orderType:'',
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
        this.isLogin(()=>{})
        this.order_details_id = option.order_details_id;
        this.order_anking = option.order_anking;
        this.r_status = option.r_status;
        if(option.isbatch == 'true'){
            this.isbatch = true
        }
        if (option.otype) {
            this.otype = option.otype
        }
        this._axios();
        this.rType = option.rType;
        if (this.r_status == '3') {
            this.rType = 3;
        }
    },
    methods: {
        handleErrorImg(index){
            this.order[index].image = this.ErrorImg
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
        _uni_navigateTo(url) {
            uni.redirectTo({
                url
            });
        },
        /**
         * @description 加载数据
         * */
        _axios() {
            var data = {
                api: 'app.order.return_method',
                order_details_id: this.order_details_id
            };
            //虚拟商品
            if(this.otype == 'VI'){
                data.order_type = 'VI'
            }
            this.$req.post({ data }).then(res => {
                if (res.code == 200) {
                    let { data } = res;
                    this.order = data.list;
                    this.self_lifting = data.self_lifting;
                    this.status = data.status;
                    this.refundAmt = data.refundAmt;
                    this.refundGoodsAmt = data.refundGoodsAmt;
                    this.currency_symbol = data.currency_symbol;
                    this.exchange_rate = data.exchange_rate;
                    this.refundGoods = data.refundGoods;
                    if(data.orderType == 'FX'){this.refundGoods = true}
                    this.orderType = data.orderType
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            });
        }
    }
};
</script>

<style>
    page{
        background-color: #F4F5F6;
    }
</style>
<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/returnGoods/returnGoods.less');
</style>
