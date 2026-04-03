<template>
    <view class="page">
        <!-- 头部 -->
        <heads
            :title="language.MBPayResults.zfjg"
            :border="true"
            :bgColor="'i_state' ? bgColor : bgColor1"
            :titleColor="titleColor"
            :ishead_w="i_state ? '3' : '2'"
            :returnR="6"
        >
        </heads>
        <!-- 内容 -->
        <view class="content" :class="i_state ? 'successful' : 'failure'">
            <!-- 占位 -->
            <view class="zhanwei"></view>
            <!-- 内容一 支付状态-->
            <view class="content_one">
                <view :style="i_state ? 'padding-top:44rpx' : ''">
                    <view>
                        <image :src="i_state ? cg : sb"></image>
                    </view>
                    <text :class="i_state ? 'successful_b' : 'failure_b'">{{
                        i_content
                    }}</text>
                </view>
            </view>
            <!-- 内容二 背景线-->
            <view class="content_two" v-if="i_state">
                <view></view>
            </view>
            <!-- 内容三 失败-支付金额/订单编号-->
            <view class="content_three_f" v-if="!i_state">
                <view>
                    <!-- 订单编号 -->
                    <view v-if="shacoType != 'PT'">
                        <text>{{ language.MBPayResults.ddbh }}</text>
                        <text class="order-id" @tap='onCopy( payReslist.sNo)'>{{ payReslist.sNo }}</text>
                    </view>
                    <!-- 支付金额 -->
                    <view>
                        <text>{{ language.MBPayResults.zfje }}</text>
                        <view>
                            <text v-if="payReslist.total_score">{{
                                language.MBPayResults.jf
                            }}</text>

                            <text v-if="payReslist.total_score">{{
                                payReslist.total_score
                            }}</text>
                            <text
                                v-if="
                                    payReslist.total && payReslist.total_score
                                "
                                >+</text
                            >
                            <text v-if="payReslist && payReslist.total >= 0"
                                >{{currency_symbol}}</text
                            >
                            <text v-if="payReslist && payReslist.total >= 0">{{
                                    LaiKeTuiCommon.formatPrice(Number(payReslist.total))
                            }}</text>
                        </view>
                    </view>
                    <!-- 下单时间 -->
                    <view v-if="shacoType != 'PT'">
                        <text>{{ language.MBPayResults.xdsj }}</text>
                        <view>
                            <text>{{ payReslist.orderTime }}</text>
                        </view>
                    </view>
                
                </view>
            </view>
            <!-- 内容三 成功-支付金额/订单编号-->
            <view class="content_three_s" v-if="i_state">
                <view>
                    <!-- 背景色 -->
                    <view></view>
                    <!-- 金额 -->
                    <view>
                        <text v-if="payReslist.total_score">{{
                            language.MBPayResults.jf
                        }}</text>
                        <text v-if="payReslist.total_score" class="jf_class">{{
                            payReslist.total_score
                        }}<text class="jf_add">+</text></text>
                        
                        <text v-if="payReslist.total_score"></text>
                        <text v-if="payReslist && payReslist.total >= 0"
                            >{{currency_symbol}}</text
                        >
                        <text v-if="payReslist ">
                            {{LaiKeTuiCommon.formatPrice(Number(payReslist.total || 0))
                        }}</text>
                    </view>
                    <!-- 订单编号 -->
                    <view v-if="shacoType != 'PT'">
                        <text>{{ language.MBPayResults.ddbh }}</text>
                        <text  class="order-id" @tap='onCopy( payReslist.sNo)'>{{ payReslist.sNo }}</text>
                    </view>
                    <!-- 支付金额 -->
                    <view>
                        <text>{{ language.MBPayResults.zfje }}</text>
                        <view>
                            <text v-if="payReslist.total_score">{{
                                language.MBPayResults.jf
                            }}</text>
                            <text v-if="payReslist.total_score">{{
                                payReslist.total_score
                            }}</text>
                            <text
                                v-if="
                                    payReslist.total && payReslist.total_score
                                "
                                >+</text
                            >
                            <text v-if="payReslist && payReslist.total >= 0"
                                >{{currency_symbol}}</text
                            >
                            <text v-if="payReslist && payReslist.total >= 0">{{
                                    LaiKeTuiCommon.formatPrice(Number(payReslist.total || 0))
                            }}</text>
                        </view>
                    </view>
                    <!-- 下单时间 -->
                    <view v-if="shacoType != 'PT'">
                        <text>{{ language.MBPayResults.xdsj }}</text>
                        <view>
                            <text v-if="payReslist.endetime != ''">{{
                                payReslist.orderTime
                            }}</text>
                            <text v-else>{{
                                payReslist.orderTime | dateFormat
                            }}</text>
                        </view>
                    </view> 
                    <!-- 备注 -->
                    <!-- <view  >
                        <text>{{language.pay.orderDetailsr.Order_notes }}</text>
                        <view>
                            <text >{{
                                payReslist.remarks
                            }}</text> 
                        </view>
                    </view>-->
                    <!-- 占位 -->
                    <view></view>
                </view>
            </view>
            <!-- 内容四 按钮 查看订单/返回-->
            <view class="content_four" v-if="shacoType != 'PT'">
                <view v-if="!is_queren">
                    <!-- 支付成功 再来一单 -->
                    <template
                        v-if="
                            i_state &&
                            type != 'IN' &&
                            type != 'FS' &&
                            type != 'DJ' &&
                            type != 'ZB'
                        "
                    >
                        <view @tap="_navigateto('/pagesE/pay/orderDetailsr', 1)">
                            {{ language.MBPayResults.zlyb }}
                        </view>
                    </template>
                    <view v-if="type == 'ZB'" v-show="false"></view>
                    <!-- 支付失败 重新支付 -->
                    <template v-if="!i_state">
                        <view
                            @tap="_navigateto('/pagesB/order/order_payment', 2)"
                        >
                            {{ language.MBPayResults.cxzf }}
                        </view>
                    </template>

                    <!-- 支付成功 查看订单 bugid 55486显示全部 -->
                    <template v-if="i_state && type != 'FS'">
                        <view @tap="_navigateto('/pagesB/order/myOrder', 3)">
                            {{ language.MBPayResults.ckdd }}
                        </view>
                    </template>

                    <!-- 支付失败 查看订单 -->
                    <template v-if="!i_state && type != 'IN' && type != 'FS'">
                        <view
                            @tap="
                                _navigateto('/pagesB/order/myOrder?orderType_id=0&status=1', 4)
                            "
                        >
                            {{ language.MBPayResults.ckdd }}
                        </view>
                    </template>

                    <!-- 支付成功 查看订单 -->
                    <template v-if="i_state && type == 'FS'">
                        <view
                            @tap="
                                _navigateto('/pagesC/discount/discount_my', 1)
                            "
                        >
                            {{ language.MBPayResults.ckdd }}
                        </view>
                    </template>

                    <!-- 返回 -->
                    <template v-if="type == 'IN'">
                        <view @tap="_navigateto('', 6)">
                            {{ language.MBPayResults.fh }}
                        </view>
                    </template>
                    <!-- 去除限时折扣返回按钮的占位 -->
                    <view
                        v-if="type == 'FS'"
                        :style="type == 'FS' ? 'opacity:0' : ''"
                    ></view>
                </view>
                <view v-else>
                    <!-- 确认 -->
                    <view
                        style="color: #ffffff"
                        @tap="_navigateto('/pages/shell/shell?pageType=my', 5)"
                    >
                        {{ language.MBPayResults.qr }}
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
	
export default {
    data() {
        return {
            bgColor: [
                {
                    item: "rgba(255,255,255,0)",
                },
                {
                    item: "rgba(255,255,255,0)",
                },
            ],
            bgColor1: [
                {
                    item: "rgba(255,255,255)",
                },
                {
                    item: "rgba(255,255,255)",
                },
            ],
            payReslist: {}, //订单信息
            titleColor: "", //标题颜色成功为white，失败则为black。
            i_state: true, //判断支付成功或者失败true false
            dateFormat: "", //如果支付成功，显示支付成功，失败则显示支付失败。
            i_content: "", //支付状态
            is_queren: false, //是否显示确认按钮
            type: "", //订单类型
			paypal_token: "", //贝宝订单id
            cg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/pay/pay_chenggong.png",
            sb:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/pay/pay_shibai.png",
            shacoType: "",
        };
    },
    onLoad(option) {
       this.getUserCurrencyInfo();
        if(option.type){
            this.shacoType = option.type
            // 待付款支付（跳转增加一个type参数） 只需要显示确认按钮
            if(option.type == 'IN' || option.type == 'MS' || option.type == 'FS' || option.type == 'GM'){
                this.is_queren = true
            }
        }
        //支付成功/失败判断
        if (option.i_state) {
            this.i_state = JSON.parse(option.i_state);
        }
        //如果支付成功，显示成功，失败则显示失败。标题颜色成功为white，失败则为black。
        if (this.i_state) {
            this.titleColor = "white";
            this.$nextTick(() => {
                this.i_content = this.language.payresults.title[0];
            });
        } else {
            this.titleColor = "black";
            this.$nextTick(() => {
                this.i_content = this.language.payresults.title[1];
            });
        }
        //获取订单信息
        if(uni.getStorageSync("payRes")){
            this.payReslist = JSON.parse(uni.getStorageSync("payRes"));
        }else{
            // 充值
            var url = window.location.href;
            var preUrl = url.split('?')[1];
            const params = new URLSearchParams(preUrl);
            const result = {};
            
            for (const [key, value] of params.entries()) {
                result[key] = decodeURIComponent(value);
            }
            this.payReslist.sNo = result.out_trade_no
            this.payReslist.total = result.total_amount 
            this.payReslist.orderTime = result.timestamp  
            this.payReslist.remarks = result.remarks
        }
        if (!this.payReslist.total) {
            this.payReslist.total = this.payReslist.amount;
        } 
		//贝宝
		if( option.token){
			this.paypal_token = option.token
			this.capture()
		}
        //订单类型
        let type = this.payReslist.sNo.substring(0, 2);
        this.type = type;
        //购物车下单&&支付成功 只有确认按钮
        if (this.payReslist.cart_id && this.i_state) {
            this.is_queren = true;
        }
        //保证金 竞拍 只显示确认按钮
        if (type == "JB" || type == "JP" || type == "DJ" || type == "ZB") {
            this.is_queren = true;
        } else if (type == "PS" && !this.i_state) {
            //预售支付失败 只有确认按钮
            this.is_queren = true;
        }
    },
    methods: {
        getUserCurrencyInfo(){
                let currency = uni.getStorageSync('currency');
                //用户自己选择的货币符号
                this.currency_symbol = currency.currency_symbol

            },
         onCopy(orderId) { 
            var me = this;
            uni.setClipboardData({
                data: orderId,
                success: function(res) {
                    uni.showToast({
                        title: me.language.order.order.copy_success,
                        duration: 1500,
                        icon: "none",
                    });
                },
            });
        },
        //跳转其他子页面
        _navigateto(dftUrl, index) {
            //跳转链接
            let type = ''
            try{
             type = this.payReslist.sNo.substring(0, 2);
            }catch(e){
                console.error(e)
                type = ''
            }
            let url = "";
            if (index == 1) {
                //再来一笔
                switch (type) {
                    case "PS":
                        url = "/pagesC/preSale/order/orderDetailsr";
                        break;
                    case "MS":
                        url = "/pagesB/seckill/seckillDetailsr";
                        break;
                    case "IN":
                        url = "/pagesB/integral/integral_order";
                        break;
                    case "ZB":
                        url = "/pagesD/liveStreaming/liveStreamingOrder";
                        break;
                    default:
                        url = dftUrl;
                        break;
                }
            } else if (index == 2) {
                //重新支付
                switch (type) {
                    case "PS":
                        url = dftUrl;
                        break;
                    case "MS":
                        // url = '/pagesB/seckill/seckill_my'
                        url = dftUrl;
                        break;
                    default:
                        url = dftUrl;
                        break;
                }
            } else if (index == 3) {
                //成功的查看订单
                switch (type) {
                    case "PS":
                        //定金模式跳转待付款
                        if (this.payReslist && this.payReslist.typePs == "dj") {
                            url = "/pagesC/preSale/order/myOrder?status=1";
                        } else {
                            url = "/pagesC/preSale/order/myOrder?status=2";
                        }
                        break;
                    case "MS":
                        url = "/pagesB/seckill/seckill_my";
                        break;
                    case "IN":
                        url = "/pagesB/integral/exchange";
                        break;
                    case "FX":
                        url = "/pagesB/order/myOrder?orderType_id=0&status=0&type=FX";
                        break;
                    case "ZB":
                        url = "/pagesD/liveStreaming/liveStreamingOrder";
                        break;
                    case "VI":
                        url = "/pagesB/order/myOrder?orderType_id=1&status=0";
                        break;
                    default:
                        url = dftUrl;
                        //如果是自提，跳转待收货
                        if (
                            this.payReslist.wuliu &&
                            this.payReslist.wuliu == "ziti"
                        ) {
                            url = "/pagesB/order/myOrder?orderType_id=0&status=3";
                        }
                        break;
                }
            } else if (index == 4) {
                //失败的查看订单
                switch (type) {
                    case "PS":
                        url = "/pagesC/preSale/order/myOrder?status=1";
                        break;
                    case "MS":
                        url = "/pagesB/seckill/seckill_my";
                        break;
                    case "IN":
                        url = "/pagesB/integral/exchange";
                        break;
                    case "FX":
                        url = "/pagesB/order/myOrder?orderType_id=0&status=0&type=FX";
                        break;
                    case "ZB":
                        url = "/pagesD/liveStreaming/liveStreamingOrder";
                        break;
                    case "VI":
                        url = "/pagesB/order/myOrder?orderType_id=1&status=0";
                        break;
                    default:
                        url = dftUrl;
                        break;
                }
            } else if (index == 5) {
                //确认按钮
                switch (type) {
                    case "JB":                        
                    // 如果页面栈为空 跳回个人中心
                        if(getCurrentPages() && getCurrentPages().length >1){
                            uni.navigateBack({ delta: 1 });
                        }else{
                            uni.redirectTo({ url: dftUrl });
                        }
                        return;
                    case "PS":
                        url = "/pagesC/preSale/order/myOrder";
                        break;
                    case "GM":
                        if(getCurrentPages() && getCurrentPages().length >1){
                            uni.navigateBack({ delta: 1 });
                        }else{
                            uni.redirectTo({ url: dftUrl });
                        }  
                        break;
                    case "VI":
                        uni.navigateBack({ delta: 1 });
                        break;
                    case "ZB":
                        url = "/pagesD/liveStreaming/liveStreamingOrder";
                        break;
                    default:
                        uni.redirectTo({ url: dftUrl });
                        return;
                }
            } else if (index == 6) {
                //返回
                switch (type) {
                    case "IN":
                        uni.navigateBack();
                        return;
                    case "FS":
                        uni.navigateBack({ delta: 1 });
                        return;
                    default:
                        url = dftUrl;
                        break;
                }
            }
            //再来一单/重新支付需要参数拼接
            if (index == 1) {
                let options = uni.getStorageSync("options");
                url = url + "?";
                for (let i in options) {
                    url = url + i + "=" + options[i] + "&";
                }
            } else if (index == 2) {
                let order_id = uni.getStorageSync("order_id");
                url = url + "?order_id=" + order_id;
            }
            //swith逻辑后跳转
            uni.redirectTo({
                url: url,
            });
        },
		//贝宝执行扣款
async capture() {
 //    let paypal_token = this.paypal_token;
	// let sNo = this.payReslist.sNo;
	const  data = {
		api:'app.pay.capture',
		orderId : this.paypal_token,
		sNo : this.payReslist.sNo
	}
	this.$req.post({data}).then(res => {
	    uni.hideLoading()
	    if(res.code == 200 ){
	       // 成功
	    } else {
	        uni.showToast({
	            title: res.message,
	            icon: 'none'
	        })
	    }
	}).catch(e => {
	})
}
    }

	
};
</script>

<style>
page {
    background-color: #f4f5f6;
}
</style>

<style lang="less" scoped>
@import url("@/laike.less");
@import url("@/static/css/pay/PayResults.less");

.successful {
    background: #fa5151 !important;
}
.failure {
    background: #ffffff !important;
}
.successful_a {
    background-color: #ffffff !important;
}
.failure_a {
    background-color: #333333 !important;
}
.successful_b {
    color: #ffffff !important;
}
.failure_b {
    color: #000000 !important;
}
.jf_class {
    font-size: 24px;
    font-family: DIN;
}
.jf_add{
    font-size: 48rpx;
}
</style>
