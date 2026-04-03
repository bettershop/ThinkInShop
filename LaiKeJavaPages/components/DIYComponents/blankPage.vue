<template>
	<view class="skeleton  ">
		<!-- DIY首页 -->
		<template v-if="styleConfig && Object.keys(styleConfig).length>0">
			<view class="diy" :style="  bgColor ">
				<block v-for="(item, index) in styleConfig" :key="index">
					<headerSerch v-if="item.name == 'headerSerch'" :title="appTitle" :dataConfig="item" @toUrl="toUrl"
						:region="region"> </headerSerch>
					<headerMod v-if="item.name == 'page_title'" :dataConfig="item"></headerMod>
					<homeTitle v-if="item.name == 'comp_title'" :dataConfig="item" @toUrl="toUrl"></homeTitle>
					<combination v-if="item.name == 'combination'" :dataConfig="item" @toUrl="toUrl"></combination>
					<guide v-if="item.name == 'guide'" :dataConfig="item"></guide>
					<homeAdv v-if="item.name == 'homeAdv'" :dataConfig="item" @toUrl="toUrl"></homeAdv>
					<homeStore v-if="item.name == 'homeStore'" :dataConfig="item" @toUrl="toUrl"></homeStore>
					<homeVideo v-if="item.name == 'homeVideo'" :dataConfig="item"></homeVideo>
					<menus v-if="item.name == 'menus'" :dataConfig="item" @toUrl="toUrl"></menus>
					<richText v-if="item.name == 'z_ueditor'" :dataConfig="item" @toUrl="toUrl"></richText>
					<reduction v-if="item.name == 'reduction'" :dataConfig="item" @toUrl="toUrl"></reduction>
					<swiperBg v-if="item.name == 'swiperBg'" :dataConfig="item" @toUrl="toUrl"></swiperBg>
					<swiperPicture v-if="item.name == 'swiperPicture'" :dataConfig="item" @toUrl="toUrl">
					</swiperPicture>
					<tabNav ref="diyFl" v-if="item.name == 'tabNav'" :dataConfig="item" :page="page"
						:is_grade="is_grade" @loadingType="onHandleLoadingType" @changePage="changePage" @toUrl="toUrl">
					</tabNav>
					<!-- 直播 -->
					<!-- #ifdef H5 -->
					<publicModeZB v-if="item.name == 'liveModel'" :dataConfig="item"> </publicModeZB>
					<!-- #endif -->
					<publicModeMS v-if="shaco_plugin.seconds && item.name == 'seckill'" :dataConfig="item">
					</publicModeMS>
					<publicModeVIP v-if="shaco_plugin.member && item.name == 'VIP'" :dataConfig="item"> </publicModeVIP>
					<publicModeFX title="Go! 分享赚" v-if="item.name == 'PublicModeFX'" :dataConfig="item"></publicModeFX>
				</block>
			</view>
		</template>

		<skus ref="attrModal" @confirm="_confirm"></skus>
		<!-- 回到顶部 -->
		<image class="zhiding" v-if="scrollTopNum > 1000" :src="zhiding" @tap="_zhiding"></image>

		<!-- 0下拉加载更多/1加载中/2没有更多了 -->
		<uni-load-more :loadingType="loadingType" style="padding-bottom: 30rpx;"></uni-load-more>
	</view>
</template>

<script>
	import headerSerch from "@/components/DIYComponents/headerSerch.vue";
	import combination from "@/components/DIYComponents/combination.vue";
	import guide from "@/components/DIYComponents/guide.vue";
	import homeAdv from "@/components/DIYComponents/homeAdv.vue";
	import homeStore from "@/components/DIYComponents/homeStore.vue";
	import homeTitle from "@/components/DIYComponents/homeTitle.vue";
	import headerMod from "@/components/DIYComponents/header.vue";
	import homeVideo from "@/components/DIYComponents/homeVideo.vue";
	import menus from "@/components/DIYComponents/menus.vue";
	import richText from "@/components/DIYComponents/richText.vue";
	import reduction from "@/components/DIYComponents/reduction.vue";
	import swiperBg from "@/components/DIYComponents/swiperBg.vue";

	import swiperPicture from "@/components/DIYComponents/swiperPicture.vue";
	import tabNav from "@/components/DIYComponents/tabNav.vue";


	import publicModeZB from "@/components/DIYComponents/publicModeZB.vue";
	import publicModeMS from "@/components/DIYComponents/publicModeMS.vue";
	import publicModeFX from "@/components/DIYComponents/publicModeFX.vue";
	import publicModeVIP from "@/components/DIYComponents/publicModeVIP.vue";


	import skus from "../../components/skus.vue";
	import {
		getTimeDiff,
	} from "@/common/util.js";
	import {
		mapMutations,
		mapState
	} from "vuex";

	export default {
		data() {
			return {
				appTitle: '',
				//首页整体背景图
				shaco_plugin: [], //组件权限列表
				//DIY参数
				is_diy: false, //diy：是否显示 
				styleConfig: [], //diy：显示数据列表
				is_grade: false, //diy：tabNav组件控制是否显示商品价格
				//待支付弹窗
				isShow_dialog_Pre: false, //待支付弹窗：是否显示
				myPopup: 0, //待支付弹窗：显示待支付数量
				is_showToast: 0, //待支付弹窗: 是否显示
				is_showToast_obj: {}, //待支付弹窗: 弹窗内容 
				//APP更新弹窗
				widgetinfoo: false, //APP更新弹窗：是否显示
				newapp: {}, //APP更新弹窗：标题内容 
				//顶部区  
				region: {
					district: "岳麓区"
				}, //定位：默认地区   
				bgColor: [{
					tem: "#FFFFFF"
				}, {
					item: "#FFFFFF"
				}], //header组件：背景色   
				//骨架屏
				load: false, //骨架屏：是否显示
				//回到顶部
				zhiding: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon/zhiding.png", //回到顶部图标
				scrollTopNum: 0, //距离顶部距离
				//其他逻辑使用参数
				time: {
					hours: "00",
					minutes: "00",
					seconds: "00"
				}, //时：分：秒
				isClick: true, //用于防止重复点击 
				page: 1, //请求第几页数据
				loadingType: 0, //下拉加载状态 0下拉加载更多/1加载中/2没有更多了
				clientHeight: 0, //当前屏幕高度
				coverBg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/product_share_img/cover.png", //小程序转发默认图--》不传则会默认选中当前页面顶部一部分截图

				// 加入购物车弹窗所需参数
				fastTap: true,
				proid: "",
				haveSkuBean: "",
				num: "",
				numb: "",

				homeOption: '', //onLoad中的option 
			};
		},
		props: ['diyData'],
		components: {
			headerSerch,
			combination,
			guide,
			homeAdv,
			homeStore,
			homeTitle,
			headerMod,
			homeVideo,
			menus,
			richText,
			reduction,
			swiperBg,
			swiperPicture,
			tabNav,


			publicModeVIP,
			publicModeZB,
			publicModeMS,
			publicModeFX,

			skus,
		},
		computed: {
			...mapState(['tabBarList']),

			...mapState({
				_cart_num: "cart_num",
			}),
		},

		//小程序右上角转发事件
		onShareAppMessage: function(res) {
			var shareimg = this.coverBg;
			return {
				title: this.appTitle,
				path: "/pages/shell/shell?pageType=home",
				success: function(res) {},
				fail: function(res) {},
			};
		},
		onLoad(option) {
			//获取连接中参数,用于部分项目对接 实现静默登录
			if (option.user_from_id && option.user_name) {
				this.otherLogin(option)
			}

			//存一下option，在onshow用
			this.homeOption = option

			//店铺id
			// #ifdef H5
			let storeID = option.store_id;
			if (!storeID) {
				storeID = uni.getStorageSync("store_id");
			}
			this.LaiKeTuiCommon.initStoreID(storeID);
			// #endif

			//移除位置缓存
			uni.removeStorageSync("region");
			uni.removeStorageSync("signFlag");

			//移除移动店铺端拼团缓存
			uni.removeStorageSync("shacoList");
			uni.removeStorageSync("proIds");

			this.syncMallTitle();

		},
		onShow() {
			this.syncMallTitle();
		},
		mounted() {
			uni.removeStorageSync('payTarget')
			this.loadingType = 0
			uni.$emit("update");
			this.getPlus()
			// 待付款订单数量
			this.myPopup = uni.getStorageSync("myPopup");
			if (this.myPopup > 0 && uni.getStorageSync("login_key") == 0) {
				this.isShow_dialog_Pre = true;
				uni.setStorageSync("login_key", 1);
			}
			//预售订单待支付弹窗
			if (this.myPopup > 0) {
				this.is_showToast = 6;
				this.is_showToast_obj.title = "提示";
				this.is_showToast_obj.contentNodes =
					'<p>您有 <span style="color:#D73B48">' +
					this.myPopup +
					"</span> 笔待付款的预售订单，<br />请尽快处理！</p>";
				this.is_showToast_obj.button = "取消";
				this.is_showToast_obj.endButton = "去处理";
			}

			//防止重复点击跳转链接
			this.isClick = true;

			//分享进入
			if (this.homeOption.scene || this.homeOption.share == 'true') {
				console.log('分享进入~option', this.homeOption)
				//小程序二维码分享 重定向
				if (this.homeOption.scene) {
					this._wxEwmShare(this.homeOption.scene)
				} else {
					this._share(this.homeOption)
				}
			}

			//当前屏幕高度
			// #ifdef H5
			this.clientHeight = document.body.clientHeight;
			// #endif
			// #ifndef H5
			this.clientHeight = uni.getSystemInfoSync().windowHeight;
			// #endif

			// #ifdef APP-PLUS
			this.widgetinfo();
			// #endif

		},
		watch: {
			diyData: {
				handler(newVal) {
					if (newVal && Object.keys(newVal).length > 0) {
						this.getDiy()
					}
				},
				deep: true,
				immediate: true
			}
		},
		//事件
			methods: {
				requestH5Translation(reason = 'blank-page') {
					// #ifdef H5
					if (typeof window.__LKT_REQUEST_TRANSLATION__ === 'function') {
						window.__LKT_REQUEST_TRANSLATION__(reason);
					}
					// #endif
				},
				getPlus() {
				let data = {
					api: "app.index.pluginStatus",
				};
				this.$req.post({
						data
					})
					.then((res) => {
						this.shaco_plugin = res.data.plugin
						console.log('shaco_plugin', this.shaco_plugin);
					})
			},
			/**
			 * 获取连接中参数,用于部分项目对接 实现静默登录
			 * 
			 * 内嵌进入H5首页传参：/pages/shell/shell?pageType=home?
			 * user_from_id=xxx&   静默登录 用户id
			 * user_name=&         静默登录 用户名称
			 * source=&            静默登录 用户来源
			 * phone=&             静默登录 用户手机号
			 * mobile=&            静默登录 用户电话
			 * openid=&            获取小程序支付信息 openid
			 * isType=&            支付类型  通用：gm 充值：cz
			 * total=&             支付结果  金额
			 * sNo=&               支付结果  订单号
			 * orderTime=&         支付结果  下单时间
			 * payResult=&         支付结果  支付成功/失败
			 * total_score=        插件积分  支付的积分
			 * @param {Object} option
			 */
			otherLogin(option) {
				uni.setStorageSync('openid', option.openid)
				if (option.phone) {
					uni.setStorageSync('user_phone', option.phone)
				} else if (option.mobile) {
					uni.setStorageSync('user_phone', option.mobile)
				}
				let data = {
					api: "app.user.synuser",
					user_from_id: option.user_from_id, //第三方用户ID
					user_name: option.user_name, //第三方用户名称
					source: option.source, //来源
					phone: option.phone, //手机号1
					mobile: option.mobile, //手机号2
				};

				this.$req.post({
					data
				}).then(res => {
					uni.setStorageSync('access_id', res.data.access_id)
					uni.setStorageSync('store_id', res.data.store_id)
					uni.setStorageSync('user_id', res.data.user_id)
					//登录后获取用户信息
					this.getUserInfor()
				})

				if (option.isType == 'gm') {
					let obj = {}
					if (option.total_score) {
						obj.total_score = option.total_score
					}
					obj.total = option.total
					obj.sNo = option.sNo
					obj.orderTime = decodeURI(option.orderTime)
					uni.setStorageSync('payRes', JSON.stringify(obj))
					let urls = "/pagesE/pay/PayResults?i_state=" + option.payResult + '&type=GM'

					uni.navigateTo({
						url: urls
					});
				} else if (option.isType == 'cz') {
					if (option.payResult == 'false') {
						console.log('小程序内嵌H5-》充值失败', option.payResult);
						uni.showToast({
							title: '充值失败，请稍后再试！',
							icon: 'none'
						})
						return
					}
					let mylei = 1
					let money = option.total
					let time = option.orderTime
					time = decodeURI(time)
					let urls = "/pagesB/myWallet/rechargeSuccess?mylei=" + mylei + '&money=' + money + '&time=' + time +
						'&_type=wx'
					uni.navigateTo({
						url: urls
					});
				} else {
					console.log('isType-》是否支付完成进入', option.isType);
				}
			},
			//获取用户信息
			getUserInfor() {
				let data = {
					api: "app.user.index",
				};
				this.$req.post({
					data
				}).then(res => {
					uni.setStorageSync('userinfo', res.data.data.user)
				})
			},

			// suk组件返回数据
			_confirm(sku) {
				Object.assign(this.$data, sku);

				if (!this.fastTap) {
					return;
				}
				this.fastTap = false;
				if (Boolean(this.haveSkuBean)) {
					if (this.num == 0) {
						uni.showToast({
							title: this.language.proList.Tips[0],
							duration: 1000,
							icon: "none",
						});
						this.fastTap = true;
					} else if (this.num != 0) {
						this._shopping();
					}
				} else {
					if (this.num == 0) {
						uni.showToast({
							title: this.language.proList.Tips[0],
							duration: 1000,
							icon: "none",
						});
						this.fastTap = true;
					} else {
						uni.showToast({
							title: this.language.proList.Tips[1],
							duration: 1000,
							icon: "none",
						});
						this.fastTap = true;
					}
				}
			},
			// 点击确定购买之后，如果库存不为零。则运行
			_shopping(id) {
				if (this.haveSkuBean) {
					var data = {
						api: "app.product.add_cart",
						pro_id: this.proid,
						attribute_id: this.haveSkuBean.cid,
						num: this.numb,
						type: "addcart",
					};

					this.$req
						.post({
							data,
						})
						.then((res) => {
							let {
								code,
								data,
								message
							} = res;
							if (code == 200) {
								uni.showToast({
									title: this.language.proList.Tips[2],
									icon: "none",
								});

								this.$store.state.access_id = data.access_id;

								this.haveSkuBean = "";
								this.$refs.attrModal._mask_f();
								this.fastTap = true;
							} else {
								uni.showToast({
									title: message,
									icon: "none",
								});
								this.fastTap = true;
							}
						})
						.catch((error) => {
							this.fastTap = true;
						});
				} else {
					this.fastTap = true;
				}
			},
			//加入购物车end


			//回到顶部
			_zhiding() {
				uni.pageScrollTo({
					scrollTop: 0,
					duration: 300,
				});
			},

			// - - - 分割线 - - -

			//VUEX 缓存库 @/store/index.js
			...mapMutations({
				cart_num: "SET_CART_NUM",
			}),

			// - - - 分割线 - - -  
			//跳转：多功能验证
			toUrl(url, is_login = false, is_sign) {
				console.log(url, this.is_diy)
				//如果是diy
				if (this.is_diy) {

					if (url.includes("tabBar")) {
						const queryString = url.includes('?') ?
							url.split('?')[1] // 若有 ?，取其后面的部分
							:
							url; // 若没有 ?，默认传入的就是查询字符串
						const params = {};
						const paramPairs = queryString.split('&');

						paramPairs.forEach(pair => {
							// 处理没有 = 的情况（如 ?a&b=1，此时 a 的值为 ''）
							const [key, value = ''] = pair.split('=');
							// 解码特殊字符（如 %20 解码为空格）
							params[decodeURIComponent(key)] = decodeURIComponent(value);
						});
						const data = this.$store.state.diyPagesInfo || uni.getStorageSync('DIYPAGEINFO')
						const index = data.findIndex(v => v.key == params.page_key)
						console.log(index)
						if (index >= 0) {
							uni.navigateTo({
								url,
							});
						}

					} else {
						if (url.includes("http")) {
							let query = {
								url
							};
							uni.navigateTo({
								url: "/pagesC/webView/webView?query=" +
									encodeURIComponent(JSON.stringify(query)),
							});
						} else {
							uni.navigateTo({
								url,
							});
						}
					}
					return false;
				} else {
					//如果是tabBar
					if (url.includes("tabBar")) {
						uni.redirectTo({
							url,
						});
					} else {
						uni.navigateTo({
							url,
						});
					}
				}

			},


			//diy：获取diy首页数据
				async getDiy() {
				await this.syncMallTitle();
				const isArrayWithContent = (arr) => Array.isArray(arr) && arr.length > 0;

				const data = isArrayWithContent(this.$store.state.diyPagesInfo) ?
					this.$store.state.diyPagesInfo :
					isArrayWithContent(uni.getStorageSync('DIYPAGEINFO')) ?
					uni.getStorageSync('DIYPAGEINFO') : [];

				let pageItem = null
				if (this.diyData && Object.keys(this.diyData).length > 0) {
					this.styleConfig = this.diyData
				} else {
					pageItem = data.find(v => v.key == this.homeOption.page_key)
					if (pageItem.page_context) {
						this.styleConfig = JSON.parse(pageItem.page_context).defaultArray;
					} else {
						this.styleConfig = pageItem.defaultArray;
					}
				}
				console.log(this.styleConfig, 'styleConfigstyleConfig')

				this.objToArr(this.styleConfig).forEach((item) => {
					if (item.name === "homeBg") {
						if (item.bgColor.type == 1) {
							const bgColor = item.bgColor.color
							this.bgColor =
								`background: linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`
						} else {
							const bgImgUrl = item.bgColor.imgUrl
							if (bgImgUrl) {
								this.bgColor = {
									backgroundImage: `url(${bgImgUrl})`, // 改用 bgImgUrl，而非 bgColor
									backgroundSize: '100% 100%',
									backgroundRepeat: 'no-repeat',
									backgroundPosition: 'center'
								};
							} else {
								this.bgColor = ''
							}
						}
					}
					// if (item.name === "headerSerch") {
					// 	this.onReachBottomBg = item.bgColor.color;
					// }
				});
					console.log('bgColorbgColor', this.bgColor)
					this.load = false;
					this.$nextTick(() => {
						this.requestH5Translation('blank-page-diy-ready');
					});
				},

			//diy：对象转数组
			objToArr(data) {
				let obj = Object.keys(data);
				let m = obj.map((key) => data[key]);
				return m;
			},

			//diy组件：导航切换
			changePage(page) {
				this.page = page;
			},

			//diy组件：
			onHandleLoadingType(loadingType) {
				console.log("loadingType", loadingType);
				this.loadingType = loadingType;
			},
			/*
			 ** DIY结束
			 */

			// - - - 分割线 - - -


			/*
			 ** APP开始
			 */

			//APP：
			widgetinfo() {
				var me = this;
				plus.runtime.getProperty(plus.runtime.appid, function(widgetInfo) {
					uni.request({
						url: uni.getStorageSync("endurl") +
							"widgetinfo/update.php?store_id=" +
							me.LaiKeTuiCommon.LKT_STORE_ID,
						data: {
							version: widgetInfo.version,
							name: widgetInfo.name,
						},
						success: (result) => {
							me.newapp = result.data;
							if (uni.getSystemInfoSync().platform === "android") {
								me.newapp.url = me.newapp.android_url;
							} else {
								me.newapp.url = me.newapp.ios_url;
							}
							if (me.newapp.status == 1) {
								me.widgetinfoo = true;
								uni.hideTabBar();
							}
						},
					});
				});
			},

			//APP：更新
			update() {
				this.widgetinfoo = false;
				uni.showTabBar();
				plus.runtime.openURL(this.newapp.url, function(res) {
					uni.showToast({
						title: this.language.home.updateFail,
						duration: 1000,
						icon: "none",
					});
				});
			},
			/*
			 ** APP结束
			 */

			// - - - 分割线 - - -


			//分享重定向 - 小程序二维码分享
			_wxEwmShare(scene) {
				uni.showLoading({
					title: '加载中...'
				})
				let data = {
					m: 'getCodeParameter',
					api: 'app.getcode.index',
					key: scene
				};
				this.$req.post({
					data
				}).then(res => {
					console.log('小程序二维码分享参数～', res)
					uni.hideLoading()
					if (res.code == 200 && res.data.parameter) {
						let parameter = res.data.parameter.split("&")
						let share = parameter[0].split("=")[1]
						//重定向页面 并处理pages中的 .- 以 / 替代，在分享时替换了的，现在切换回去.
						let pages = parameter[1].split("=")[1].replace(/\.\-/g, '/')
						//重定向页面携带的参数
						let states = '?'
						parameter.forEach((item, index) => {
							if (index > 1) {
								states = states + item + '&'
							}
						})
						states = states.slice(0, -1)
						//分享重定向url
						let sharUrl = '/' + pages + states
						console.log('分享重定向url~', sharUrl)
						uni.redirectTo({
							url: sharUrl
						});
					} else {
						uni.showToast({
							title: res.message,
							icon: 'none'
						});
					}
				});
			},

			//分享重定向
			_share(homeOption) {
				let share = homeOption.share
				//重定向页面 并处理pages中的 .- 以 / 替代，在分享时替换了的，现在切换回去.
				let pages = homeOption.pages.replace(/\.\-/g, '/')
				//重定向页面携带的参数
				let states = '?'
				for (let i in homeOption) {
					if (i != 'share' && i != 'pages') {
						states = states + i + "=" + homeOption[i] + "&";
					}
				}
				states = states.slice(0, -1)
				//分享重定向url
				let sharUrl = '/' + pages + states
				console.log('分享重定向url~', sharUrl)
				uni.redirectTo({
					url: sharUrl
				});
			}

		},
	};
</script>

<style scoped lang="less">
	@import url("@/laike.less");
	@import url("../../static/css/tabBar/home.less");

	@media screen and (min-width: 600px) {
		.zhiding {
			left: calc(50% + 270rpx);
		}
	}
</style>
