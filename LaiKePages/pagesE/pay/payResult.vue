<template>
	<view class="page">
		<heads :title="language.MBPayResults.zfjg" :border="true" :bgColor="bgColor" :titleColor="titleColor"
			:ishead_w="'3'" :returnR="6">
		</heads>
		<view class="content" :class="i_state ? 'successful' : 'failure'">
			<view class="zhanwei"></view>
			<!-- 内容一 支付状态-->
			<view class="content_one">
				<view :style="i_state ? 'padding-top:44rpx' : ''">
					<view>
						<image :src="i_state ? cg : sb"></image>
					</view>
					<text :class="i_state ? 'successful_b' : 'failure_b'">{{title}}</text>
				</view>
			</view>
			<!-- 内容二 背景线-->
			<view class="content_two" v-if="i_state">
				<view></view>
			</view>
			<view class="content_three_s" v-if="i_state">
				<view>
					<!-- 背景色 -->
					<view></view>
					<!-- 金额 -->
					<view>
						<text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}
							<template v-if="data1.total">
								{{LaiKeTuiCommon.formatPrice(data1.total)}}
							</template>
							<template v-if="total">
								{{LaiKeTuiCommon.formatPrice(total)}}
							</template>
							<template v-else>
								{{LaiKeTuiCommon.formatPrice((payment_money || 0))}}
							</template>
						</text>
					</view>
					<view class="item" v-if="data1.memberTypeDesc">
						<span class="label">{{language.pay.payResult.ktfs}}：</span>
						<span>{{data1.memberTypeDesc}}</span>
					</view>
					<view class="item">
						<span class="label">{{language.pay.payResult.ddbh}}：</span>
						<span>{{data1.sNo||sNo}}</span>
					</view>
					<view class="item" v-if="is_jifen">
						<span class="label">{{language.pay.payResult.je}}：</span>
						<span v-if="is_jifen" class="yh-is_jifen">
							<text v-if="data1.total > 0"
								style="color: #fa5151;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(data1.total)}}+</text>
							<img :src="integral_img" class="yh-integral_img">{{data1.total_score}}
						</span>
					</view>
					<view class="item" v-else>
						<span class="label">{{language.pay.payResult.je }}：</span>
						<span style="color:  #fa5151;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}
							<template v-if="data1.total">
								{{LaiKeTuiCommon.formatPrice(data1.total)}}
							</template>
							<template v-else-if="total">
								{{LaiKeTuiCommon.formatPrice(total)}}
							</template>
							<template v-else>
								{{LaiKeTuiCommon.formatPrice((payment_money||0))}}
							</template>
						</span>
					</view>
					<view class="item">
						<span class="label">{{language.pay.orderDetailsr.Order_notes }}：</span>
						<span style="color:  #fa5151;">{{data1.remark}}</span>
					</view>
					<view class="item" v-if="data1.endetime">
						<span class="label">{{language.pay.payResult.dqsj}}：</span>
						<span>{{data1.endetime.substring(0,16)}}</span>
					</view>

					<!-- 占位 -->
					<div style="height: 46rpx;"></div>
				</view>
			</view>
			<view class="content_four">
				<view>
					<!-- 确认 -->
					<view style="color: #fff;" :class="payresults ? 'successbut' : 'failurebut'" @click="toother">
						{{ payresults ? language.pay.payResult.qd : language.pay.payResult.fh }}
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import heads from "../../components/header.vue";
	import {
		mapMutations
	} from "vuex";

	export default {
		data() {
			return {
				payment_money: "",
				title: "支付结果",
				bgColor: [{
						item: "rgba(255,255,255,0)",
					},
					{
						item: "rgba(255,255,255,0)",
					},
				],
				bgColor1: [{
						item: "rgba(255,255,255)",
					},
					{
						item: "rgba(255,255,255)",
					},
				],
				sNo: "",
				order_id: "",
				data1: "",
				returnR: "6",
				showTitle: true,
				flag: true,
				payresults: "",
				zfcg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/member/zfcg.png",
				zfsb: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/member/zfsb.png",
				is_jifen: false,
				is_ms: false,
				xuanze: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon/xuanzehei2x.png",
				bback: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon/bback.png",
				back: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon/back2x.png",
				integral_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon/integral_hei.png",
				is_preSale: "",
				endetiem: "",
				memberTypeDesc: "",
				total: "",
				combalck_url: "",
				i_state: true,
				titleColor: "white",
				cg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/pay/pay_chenggong.png",
				sb: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/pay/pay_shibai.png",
			};
		},
		onLoad(option) {
			if (option.payresults) {
				this.payresults = false;
			} else {
				this.payresults = true;
			}
			if (option.is_preSale) {
				this.is_preSale = option.is_preSale;
			}
			// #ifdef H5
			window.addEventListener(
				"popstate",
				(e) => {
					//回调函数中实现需要的功能
					//alert("我监听到了浏览器的返回按钮事件啦");
					this._home();
				},
				false
			);
			// #endif
			this.is_ms = option.is_ms;
			if (option.payment_money) {
				this.payment_money = option.payment_money;
				this.sNo = option.sNo;
				let me = this;
				if (option.isH5) {
					this.showTitle = false;
                    uni.showActionSheet({
                        itemList: ["已完成支付,查看订单", "支付遇到问题"],
                        success: function (res) {
                            if(res == 0 ){
                                uni.redirectTo({
                                    url: '/pagesB/order/myOrder?status=1&mweb=1'
                                })
                            }else{
                                uni.redirectTo({
                                    url: '/pagesB/order/myOrder?status=0&mweb=1'
                                })
                            }
                        },
                        fail: function (res) {
                            uni.redirectTo({
                                url: '/pagesB/order/myOrder?status=0&mweb=1'
                            })
                        }
                    });
				}
			} else if (option.is_jifen) {
				this.is_jifen = option.is_jifen;
				if (uni.getStorageSync("payRes")) {
					this.data1 = JSON.parse(uni.getStorageSync("payRes"));
				}
				this.order_id = this.data1.order_id;
			} else {
				if (uni.getStorageSync("payRes")) {
					this.data1 = JSON.parse(uni.getStorageSync("payRes"));
				}
				if (this.data1 && this.data1.amount) {
					this.data1.total = this.data1.amount.toFixed(2);
				}
				this.order_id = this.data1.order_id;
			}

			this.combalck_url = uni.getStorageSync("tiaozhuan_url");
		},
		components: {
			heads,
		},
		methods: {
			...mapMutations({
				status: "SET_STATUS",
			}),
			/**
			 *
			 * */
			toother() {
				if (this.combalck_url) {
					uni.navigateBack({
						delta: 2,
					});
				} else if (this.payresults) {
					uni.redirectTo({
						url: "/pages/shell/shell?pageType=my",
					});
				} else {
					// uni.navigateBack();
					uni.redirectTo({
						url: "/pages/shell/shell?pageType=my",
					});
				}
			},
			_home() {
				if (this.is_ms) {
					uni.navigateTo({
						url: "/pagesB/seckill/seckill",
					});
					location.reload();
				} else if (this.is_jifen) {
					uni.navigateTo({
						url: "/pagesB/integral/integral",
					});
					location.reload();
				} else {
					uni.redirectTo({
						url: "/pages/shell/shell?pageType=home",
					});
					location.reload();
				}
			},
			/**
			 *
			 * */
			_back() {
				uni.redirectTo({
					url: "/pages/shell/shell?pageType=home",
				});
				location.reload();
			},
			/**
			 * @description 查看订单
			 * */
			_order() {
				this.status(2);
				if (this.is_ms || this.data1.sNo.substring(0, 2) == "MS") {
					uni.navigateTo({
						url: "/pagesB/seckill/seckill_my",
					});
					location.reload();
				} else if (
					this.is_jifen ||
					this.data1.sNo.substring(0, 2) == "IN"
				) {
					uni.navigateTo({
						url: "/pagesB/integral/exchange",
					});
					location.reload();
				} else if (this.is_preSale) {
					uni.navigateTo({
						url: "/pagesC/preSale/order/myOrder",
					});
				} else {
					uni.navigateTo({
						url: "../order/myOrder",
					});
					location.reload();
				}
			},
		},
	};
</script>
<style>
	page {
		height: 100%;
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
</style>
