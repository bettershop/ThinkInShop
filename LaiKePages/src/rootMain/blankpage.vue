<template>
	<view class="skeleton containerBg" :style="grbj">
		<!-- DIY首页 -->
		<template v-if="is_diy && load">
			<view class="diy" :style="{
                    background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`,
                }"> 
				  <blankPage></blankPage>
			</view>
		</template>

		<!-- 骨架屏组件 -->
		<template>
			<view class="container skeleton">
				<view v-if="!load">
					<view class="container_top">
						<!-- #ifndef MP -->
						<view class="data_h skeleton-rect">
							<view class="data_h_h"></view>
						</view>
						<!-- #endif -->
						<!-- #ifdef MP -->
						<!-- :title="language.home.title" -->
						<heads class="skeleton-rect" :title="`${appTitle}`" :border="true" :returnFlag="true"
							:bgColor="bgColor" ishead_w="1"></heads>
						<!-- #endif -->
						<view style="height: 88rpx">
							<view class="container_top_item">
								<view class="container_top_item_left skeleton-rect" style="
                                        white-space: nowrap;
                                        width: 120rpx;
                                        height: 68rpx;
                                    "></view>
								<view class="container_top_item_center skeleton-fillet"
									style="background-color: initial"></view>
								<view class="container_top_item_right skeleton-circle">

								</view>
							</view>
						</view>
					</view>
					<view class="swiperBox skeleton-fillet" style="height: 320rpx; width: 710rpx; margin: 0 auto">
					</view>
					<view class="nav">
						<template v-for="item in 10">
							<view class="nav_item">
								<span class="skeleton-rect">{{
                                    language.home.gjp
                                }}</span>
							</view>
						</template>
					</view>
					<view class="content_top">
					</view>
					<div class="seckill skeleton-rect"></div>
				</view>
				<skeleton :loading="!load" :animation="true" bgColor="#FFF"></skeleton>
			</view>
		</template>
 

		<skus ref="attrModal" @confirm="_confirm"></skus>
		<!-- 回到顶部 -->
		<image class="zhiding" v-if="scrollTopNum > 1000" :src="zhiding" @tap="_zhiding"></image>
		<!-- 公告提示弹窗 -->
		<notice ref="mynotice" ></notice>
	</view>
</template>

<script>
     import notice from '@/components/notice.vue'
	import showToast from "@/components/aComponents/showToast.vue";
	import scrollViewNav from "@/components/aComponents/switchNavThree.vue";
	import waterfall from "@/components/aComponents/waterfall.vue";
     
    import blankPage from "@/components/DIYComponents/blankPage";    
  import skeleton from "@/components/skeleton";
	import skus from "@/components/skus.vue";
	import {
		getTimeDiff,  
	} from "@/common/util.js";
	import {
		mapMutations,
		mapState
	} from "vuex";
	import path from "path";

	export default {
		data() {
			return {

				// 取消关注
				is_susa: false,
				xieyiTitle: '',
				sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
				//首页整体背景图
				grbj: "background-image: url(" +
					this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/auction/grbj.png);background-size: 100vw 100vh;background-repeat: no-repeat; ",
				//DIY参数
				is_diy: false, //diy：是否显示
				closeDiy: false, //diy：通过页面参数关闭diy来方便调试
				styleConfig: [], //diy：显示数据列表
				is_grade: false, //diy：tabNav组件控制是否显示商品价格
				//待支付弹窗
				isShow_dialog_Pre: false, //待支付弹窗：是否显示
				myPopup: 0, //待支付弹窗：显示待支付数量
				is_showToast: 0, //待支付弹窗: 是否显示
				is_showToast_obj: {}, //待支付弹窗: 弹窗内容
				grounds: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/home_remind.png", //待支付弹窗：背景图
				diongClose: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/home_remind3.png", //待支付弹窗图：删除按钮图
				//APP更新弹窗
				widgetinfoo: false, //APP更新弹窗：是否显示
				newapp: {}, //APP更新弹窗：标题内容
				upbg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/upbg1.png", //APP更新弹窗：背景图
				//提醒签到弹窗
				is_sign: false, //提醒签到弹窗：是否显示
				home_sign: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"/images/icon1/home_sign.png", //提醒签到弹窗：背景图
				cha: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"/images/icon1/close_y.png", //提醒签到弹窗：删除按钮图
				//提醒开启定位弹窗
				opensetting1: 0, //提醒开启授权：绝授权之后再次提醒
				opensetting2: 0, //提醒开启定位：是否显示
				//VIP续费弹窗
				vippupo: false, //VIP续费弹窗：是否显示
				isxufei: false, //VIP续费弹窗：是否显示
				xzbtn: false, //VIP续费弹窗：用于判断是否勾选
				xz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/member/xz.png", //VIP续费弹窗：勾选图标
				yuanquan: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/member/yuanquan.png", //VIP续费弹窗：未勾选图标
				guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/member/guanbi.png", //VIP续费弹窗：关闭弹窗
				bg_vip: "background-image: url(" +
					this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/member/vippupu.png)", //VIP续费弹窗：背景图
				//顶部区
				xxnum: "", //系统消息：未读数量
				grxx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/auction/grxx.png", //系统消息：图标
				region: {
					district: "岳麓区"
				}, //定位：默认地区
				isBg: false, //定位：是否设置白色背景
				longitude: "112.951227", //定位：精度
				latitude: "28.227965", //定位：纬度
				jianX_h: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/icon_xiahua.png", //定位： 向下尖括号图标
				syss: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/home/syss.png", //搜索：放大镜
				scrollTopNum_show: false, //header组件：是否显示
				titleColor: "#333333", //header组件：标题字体颜色
				bgColor: [{
					tem: "#FFFFFF"
				}, {
					item: "#FFFFFF"
				}], //header组件：背景色
				headBg: "url(" +
					this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/auction/grbj.png) 750rpx 100%;", //header组件：背景图
				//banner区
				banner: [], //banner：轮播列表图
				dotIndex: 0, //banner：轮播列表图下标
				//金刚区
				nav_list: [], //金刚区：插件列表
				appTitle: "", //首页标题
				//内容区
				livingStatus: false, //直播：是否显示
				Marketing_list: [], //内容区：显示插件
				Marketing_list_zdy: [], //自定义活动：活动列表
				Marketing_list_jf: {}, //积分商城：活动列表
				newImgList: [], //新品上市：商品列表
				recommendImgList: [], //好物优选：商品列表
				bottom_list: [], //商品分类：分类列表
				class_pro: [], //分类商品：商品列表
				handleNum: 0, //分类商品：当前分类商品数量
				class_cid: "", //分类商品：当前分类id用于下拉加载更多
				opensetting: false, //推荐店铺：是否显示
				r_mch: [], //推荐店铺：店铺列表
				memberPlugin: false, //VIP商品：是否显示
				goodsArr: [], //VIP商品：商品列表
				is_vip: 0, //VIP商品：跳转会员中心传入参数
				fxBg: "", //分销商品：广告图
				distribution_list: [], //分销商品：商品列表
				subtraction_list: [], //满减券： 列表
				secList: [], //限时秒杀：商品列表
				isDataKO: false, //商品瀑布：数据是否初始化（切换导航时清空左右div数据）
				minHeight: "", //瀑布流最小高度，用来固定切换导航时显示位置
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
				onReachBottomBg: [{
					item: "#014343"
				}, {
					item: "#014343"
				}], //背景色
				//其他新增参数
				memberEquity: "",
				// 加入购物车弹窗所需参数
				fastTap: true,
				proid: "",
				haveSkuBean: "",
				num: "",
				numb: "",
				secTitle: "",
				disTitle: "",
				isJF: false, //是否显示积分入口 
				homeOption: '', //onLoad中的option 
			};
		},
		components: { 
            skeleton,
			blankPage,  
            notice,
			waterfall,
			scrollViewNav,
               
			showToast,
			skus,
		},
		computed: {
			...mapState(['tabBarList']),
			//Mp时设置高度
			halfWidth: function() {
				var gru = uni.getStorageSync("data_height") ?
					uni.getStorageSync("data_height") :
					this.$store.state.data_height;
				var heigh = parseInt(gru);
				// #ifdef MP
				heigh = 0;
				// #endif
				var he = heigh * 2;
				return uni.upx2px(he) + "px";
			},
			...mapState({
				_cart_num: "cart_num",
			}),
		},
        
		 
		//点击 tab 时触发
		onTabItemTap(e) {
			//关闭小程序授权弹出窗
			this.LaiKeTuiCommon.closeMPAuthWin(this);
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
 

			//调试diy
			this.closeDiy = option.closeDiy;

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

			//好像没有用处
			uni.removeStorageSync("isHomeShow");
			this.syncMallTitle();
		},
		onShow() {
			this.syncMallTitle();
		},
		//页面卸载
		beforeDestroy() { 
		},
		onHide() {},
		//监听页面初次渲染完成
		onReady() {
			this.syncMallTitle();
		},
		mounted() {
			uni.removeStorageSync('payTarget')
			this.loadingType = 0 
			this.isxufei = false;
			uni.$emit("update");
			if (uni.getStorageSync("isxufei")) {
				this.isxufei = true;
			}
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
			//购物车 右上角数量
			this.getCart();
			//新品上市：商品列表
			this.getNewImg();
			//好物优选：商品列表
			this.getRecommendImg();
			//限时秒杀：秒杀商品
			this.getSeckill();
			//签到弹窗
			this.is_sign = false;
			//会员商品
			this.getHasGrades();
			//防止重复点击跳转链接
			this.isClick = true;
		  
			/*
			 ** 获取页面数据
			 */
			this.getShow(); //----------》数据渲染 开始 入口
			/*
			 ** 获取页面数据
			 */

			//分享进入
			if (this.homeOption.scene || this.homeOption.share == 'true') {
				//小程序二维码分享 重定向
				if (this.homeOption.scene) {
					this._wxEwmShare(this.homeOption.scene)
				} else {
					this._share(this.homeOption)
				}
			}

		},
		//上拉加载
		onReachBottom: function() {
			//diy上拉
			if (this.is_diy) {
				if (this.loadingType != 0) {
					return;
				}
				//diy上拉 页面++
				this.page++;
				//直接return 在diy组件中通过watch监听page变化 实现加载更多
				return;
			}
			//是否还存在下一页商品
			if (this.loadingType != 0) {
				return;
			}
			//判断是否还在上拉加载中，等待上一次加载完成后再允许上拉。
			if (this.handleNum < this.class_pro.length) {
				return;
			}
			//显示加载中
			this.loadingType = 1;
			//页面++
			this.page++;
			//请求
			var data = {
				api: "app.index.get_more",
				page: this.page,
				cid: this.class_cid,
			};
			this.$req.post({
				data
			}).then((res) => {
				let {
					data
				} = res;
				if (data && data.length > 0) {
					data.filter((item) => {
						item.vip_price = Number(item.vip_price).toFixed(2);
						item.vip_yprice = Number(item.vip_yprice).toFixed(2);
					});
					let new_data = data;
					this.class_pro = this.class_pro.concat(new_data);
					//商品图片参数修改
					this.class_pro.forEach((item, index) => {
						item.imgurl = item.cover_map;
						item.volumes = item.volume;
						item.volume = item.payPeople;
					});
					//>=10条可以继续上拉
					if (data.length < 10) {
						this.loadingType = 2;
					} else {
						this.loadingType = 0;
					}
				} else {
					//没有返回数据，停止上拉
					this.loadingType = 2;
				}
			});
		},
		//下拉刷新
		onPullDownRefresh() {
			uni.removeStorageSync("region");
			/*
			 ** 获取页面数据
			 */
			this.getShow(); //----------》数据渲染 开始 入口
			/*
			 ** 获取页面数据
			 */
		},
		//滚动条事件
		onPageScroll(e) {
			//节点离窗口顶部的距离
			let top1 = 0;
			const query = uni.createSelectorQuery().in(this);
			query
				.select(".goodsBox")
				.boundingClientRect((data) => {
					if (data) {
						top1 = data.top;
						//如果 节点离窗口顶部的距离 < 当前屏幕高度clientHeight > 0
						if (top1 < this.clientHeight && top1 > 0) {
							//并且 瀑布流数据大于0的时候 设置瀑布流最小高度
							if (this.class_pro.length) {
								//设置瀑布流最小高度 -- 用于切换导航时 固定导航在窗口的位置
								this.minHeight =
									(this.clientHeight - top1) * 2 + "rpx";
							}
						}
					}
				})
				.exec();
			//是否显示--回到顶部
			this.scrollTopNum = e.scrollTop;
			//是否隐藏 header组件中的标题模块
			if (e.scrollTop > 0) {
				this.headBg = "#ffffff";
				if (!this.isBg) {
					this.isBg = true;
				}
				// #ifdef MP-WEIXIN
				if (!this.scrollTopNum_show) {
					this.scrollTopNum_show = true;
				}
				// #endif
			} else {
				this.headBg =
					"url(" +
					this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/auction/grbj.png) 750rpx 100%;";
				this.isBg = false;
				// #ifdef MP-WEIXIN
				this.scrollTopNum_show = false;
				// #endif
			}
		},
		//事件
		methods: {
			handleTabChange({
				index,
				item
			}) {
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
				let _this = this
				_this.$req.post({
					data
				}).then(res => {
					uni.setStorageSync('access_id', res.data.access_id)
					uni.setStorageSync('store_id', res.data.store_id)
					uni.setStorageSync('user_id', res.data.user_id)
					//登录后获取用户信息
					_this.getUserInfor()
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
				}
			},
			_axios() {

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
			 
			handleErrorImgMCH(index) {
				setTimeout(() => {
					this.r_mch[index].headimgurl = require("@/static/img/default-img.png")
				})
			},
			// nav图标默认图片更改
			handleNavLisImg(index) {
				this.nav_list[index].appimg = require("@/static/img/default-img.png")
			},
			// 更改默认图片
			setDefaultImage(index) {
				setTimeout(() => {
					this.banner[index].image = require("@/static/img/default-img.png");
				})
			},
			//店铺关注/取关
			collectMch(mac_id, state) {
				this.isLogin(() => {
					let data = {
						api: "mch.App.Mch.Collection_shop",
						shop_id: mac_id
					};
					this.$req.post({
							data
						}).then(res => {
							this.xieyiTitle = state ? this.language.order.myorder.cancel_success : this
								.language.goods.goodsDet.gzcg
							this.is_susa = true
							setTimeout(() => {
								this.is_susa = false
							}, 1500)
							if (res.code == 200) {
								// this._axios()
							}
						})
						.catch(error => {
						});
				})
			},
			//轮播图切换
			changeBanner(e) {
				this.dotIndex = e.detail.current;
			},
			//提醒签到：关闭弹窗
			closeSign() {
				this.is_sign = false;
			},
			//加入购物车start
			//加入购物车
			_addShopCar(item, id) {
				if (item && item.status == 3) {
					uni.showToast({
						title: "商品已下架",
						icon: "none",
					});
					return;
				}
				if (item && item.num == 0) {
					uni.showToast({
						title: "商品已售罄",
						icon: "none",
					});
					return;
				}
				this.shopping_j(id);
			},
			// 为你推荐商品右下角的小购物车图标
			shopping_j(id) {
				if (!this.fastTap) {
					return;
				}
				this.fastTap = false;
				this.proid = id;
				var data = {
					api: "app.product.index",
					pro_id: id,
				};

				this.$req
					.post({
						data,
					})
					.then((res) => {
						this.fastTap = true;
						let {
							data: {
								collection_id,
								pro,
								comments,
								attrList,
								skuBeanList,
								attribute_list,
								qj_price,
								type,
								commodity_type,
								write_off_settings
							},
						} = res;

						this.$refs.attrModal.imgurl = pro.img_arr[0];
						this.$refs.attrModal.num = pro.num;
						this.$refs.attrModal.price = qj_price;
						this.$refs.attrModal.skuBeanList = attribute_list;
						this.$refs.attrModal.initData();
						this.$refs.attrModal._mask_display();
						pro.commodity_type = commodity_type
						pro.write_off_settings = write_off_settings
						this.$refs.attrModal.pro2 = pro
					})
					.catch((error) => {
						this.fastTap = true;
					});
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
								this.getCart()
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
			//查看商品详情
			_seeGoods(id) {
				this._goods(id);
			},
			//当前分类商品数量
			_goodsNumber(num) {
				this.handleNum = num;
			},
			//商品分类
			_changeNav(item, index) {
				//记录分类id 用于下拉加载更多
				try {
					if (!item) return
					this.class_cid = item.cid;
					//初始化 开启下拉加载，如果请求数据小于10则>0，停止禁用下拉并提示～我是有底线的～
					this.page = 1;
					this.loadingType = 0;
					//每次点击 都得初始化上一次商品瀑布流的数据
					this.isDataKO = true;
					//显示当前选中的下标数据
					this.class_pro = this.bottom_list[index].list.concat();
					//商品图片参数修改
					this.class_pro.forEach((item, index) => {
						//可能有相同路径的图片，会无法加载@load事件；拼接随机字符串，可以解决这个问题
						item.imgurl = item.cover_map + '?a=' + Math.floor(Math.random() * 1000000);
						item.volumes = item.volume;
						item.volume = item.payPeople;
					});
					//500毫秒，取消初始化
					setTimeout(() => {
						this.isDataKO = false;
					}, 100);
				} catch (e) {
					//TODO handle the exception
					this.class_pro = []
					this.class_cid = ''
					console.error('home.vue:', e)
				}
			},
			//回到顶部
			_zhiding() {
				uni.pageScrollTo({
					scrollTop: 0,
					duration: 300,
				});
			},
			//购物车 右上角数量
			getCart() {
				var data = {
					api: "app.cart.index",
					cart_id: "",
					page: 1,
				};
				this.$req.post({
					data
				}).then((res) => {
					let {
						code,
						data: {
							data,
							list,
							mch_list,
							login_status,
							grade
						},
						message,
					} = res;
					if (code == 200) {
						this.cart_num(data.length);
						//添加，移除tabbar购物车小点提醒
						if (this._cart_num) {
							var cart_num_str = String(this._cart_num);
							// #ifdef MP-WEIXIN
							uni.setTabBarBadge({
								index: 2,
								text: cart_num_str,
							});
							// #endif
							// #ifndef MP-WEIXIN
							uni.setTabBarBadge({
								index: 2,
								text: cart_num_str,
							});
							// #endif
						} else {
							// #ifdef MP-WEIXIN
							uni.removeTabBarBadge({
								index: 2,
							});
							// #endif
							// #ifndef MP-WEIXIN
							uni.removeTabBarBadge({
								index: 2,
							});
							// #endif
						}
					} else {
						// #ifdef MP-WEIXIN
						uni.removeTabBarBadge({
							index: 2,
						});
						// #endif

						// #ifndef MP-WEIXIN
						uni.removeTabBarBadge({
							index: 2,
						});
						// #endif
					}
				});
			},

			// - - - 分割线 - - -

			//VUEX 缓存库 @/store/index.js
			...mapMutations({
				cart_num: "SET_CART_NUM",
			}),

			// - - - 分割线 - - -

			/*
			 ** 跳转开始
			 */

			//跳转：先验证登录
			_navgTo(url) {
				// this.isLogin(() => {
				uni.navigateTo({
					url: url,
				});
				// });
			},
			//跳转：防止重复点击
			_navigateTo(url) {
				if (this.isClick) {
					this.isClick = false;
					this.isLogin(() => {
						uni.navigateTo({
							url: url,
						});
					});
					setTimeout(function() {
						this.isClick = true;
					}, 800);
				}
			},
			//跳转：多功能验证
			toUrl(url, is_login = false, is_sign) {
				//如果是diy
				if (this.is_diy) {
					if (is_login) {
						this.isLogin(() => {
							if (is_sign) {
								this.$req
									.post({
										data: {
											api: "app.sign.sign",
										},
									})
									.then((res) => {
										if (res.code == 200) {
											this.toUrl(url, false);
										} else {
											uni.showToast({
												title: res.message,
												icon: "none",
											});
										}
									});
								return;
							}
							this.toUrl(url, false);
						});
						return false;
					}
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
				}
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
			},
			//跳转：金刚区跳转 根据参数判断是否开启登录验证跳转
			navClick(item) {
				// #ifndef MP-WEIXIN
				if (item.name == "来客电商直播" || item.name == "live") {
					uni.showToast({
						title: this.language.home.openWeChat,
						icon: "none",
					});
					return;
				}
				// #endif
				if (item.is_login) {
					this.isLogin(() => {
						if (item.url.includes("tabBar")) {
							uni.redirectTo({
								url: item.url,
							});
						} else {
							uni.navigateTo({
								url: item.url,
							});
						}
					});
				} else {
					if (item.url.includes("tabBar")) {
						uni.redirectTo({
							url: item.url,
						});
					} else {
					 
						uni.navigateTo({
							url: item.url,
						}); 
					}
				}
			},
			//跳转：APP
			goApp() {
				location.href = "ZhongBingGouMang://pages/index/home/index";
			},
			//跳转：会员中心
			vipclick() {
				this.isLogin(() => {
					uni.navigateTo({
						url: "/pagesB/userVip/memberCenter",
					});
				});
			},
			//跳转：续费VIP会员
			vipClick() {
				uni.navigateTo({
					url: "/pagesB/userVip/memberCenter?id=1",
				});
			},
			//跳转：会员VIP商品-查看详情
			navtovip(e) {
				if (this.is_vip == 1) {
					uni.navigateTo({
						url: "/pagesC/goods/goodsDetailed?pro_id=" +
							e.id +
							"&vipprice=" +
							e.vipPrice +
							"&price=" +
							e.price +
							"&is_hy=" +
							this.is_vip,
					});
				} else {
					uni.showToast({
						icon: "none",
						title: "请先开通会员",
					});
				}
			},
			//跳转：限时秒杀-查看详情
			toSeckill(item) {
				let types = null;
				if (item.secStatus == 2) {
					types = 1;
				} else if (item.secStatus == 4) {
					types = 2;
				} else if (item.secStatus == 5) {
					types = 3;
				} else {
					types = 0;
				}
				uni.navigateTo({
					url: "/pagesB/seckill/seckill_detail?pro_id=" +
						item.goodsId +
						"&navType=" +
						types +
						"&id=" +
						item.secGoodsId +
						"&type=MS",
				});
			},
			//跳转：待付款订单-去支付
			_order() {
				this.isLogin(() => {
					this.isShow_dialog_Pre = false;
					uni.navigateTo({
						url: "/pagesC/preSale/order/myOrder?status=0",
					});
				});
			},
			//跳转：商品详情
			_goods(id) {
				this.$store.state.pro_id = id;
				uni.navigateTo({
					url: "/pagesC/goods/goodsDetailed?toback=true&pro_id=" + id,
				});
			},
			//跳转：分销商品详情
			_fx_goods(id, p_id) {
				this.$store.state.pro_id = id;
				uni.navigateTo({
					url: "/pagesC/goods/goodsDetailed?isDistribution=true&toback=true&pro_id=" +
						p_id +
						"&fx_id=" +
						id,
				});
			},
			//跳转：积分商品详情页
			_integralGoods(id, goods_id, integral, num) {
				this.isLogin(() => {
					uni.navigateTo({
						url: "/pagesB/integral/integral_detail?pro_id=" +
							id +
							"&goodsId=" +
							goods_id +
							"&integral=" +
							integral +
							"&num=" +
							num,
					});
				});
			},
			/*
			 ** 跳转结束
			 */

			// - - - 分割线 - - -

			/*
			 ** 定位开始
			 */

			//定位：去选择地区
			toArea() {
				uni.setStorageSync("region", this.region);
				this.toUrl("/pagesB/home/chooseArea");
			},
			//定位：未开启定位时，点击去开启。
			_openSetting() {
				this.opensetting1 = 0;
				//调起客户端小程序设置界面，返回用户设置的操作结果。
				uni.openSetting({
					success(res) {
					},
				});
				//重新获取定位
				//this.getLocationAuthorize();
			},
			//定位：是否在微信浏览器中
			_isWx() {
				let en = window.navigator.userAgent.toLowerCase();
				return en.match(/MicroMessenger/i) == "micromessenger";
			},
			//定位：获取当前地址
			getLocationAuthorize() {
				// 小程序 获取定位
				// #ifdef MP
				uni.authorize({
					scope: "scope.userLocation",
					success: (res) => {
						//已授权定位后 获取定位信息
						this.getLocation();
					},
					fail: () => {
						//未授权定位
						//提示 请授权位置获取更好的体验
						this.opensetting1 = 7;
						this.is_showToast_obj.title = "授权提示";
						this.is_showToast_obj.content =
							"请授权位置信息，获取更好的体验";
						this.is_showToast_obj.button = "取消";
						this.is_showToast_obj.endButton = "确认";
						this.is_showToast_obj.type = "primary";
						this.is_showToast_obj.openType = "openSetting";
						//请求页面数据 使用默认定位-岳麓区
						this._isDiy();
					},
				});
				// #endif

				// APP 获取定位
				// #ifdef APP
				// 与余庆和文哥通过后决定将APP环境的首页授权定位功能隐藏
				// this.getLocation();
				// #endif

				// H5 获取定位 （只有微信浏览器中可以获取定位，普通浏览器没有定位）
				// #ifdef H5
				if (this._isWx()) {
					//如果是微信浏览器
					//获取定位信息 暂时关闭微信浏览器中获取订单信息
					//this.getLocation();
					//请求页面数据 -- 这里和上面同步执行 就算点击拒绝也能使用默认定位请求
					this._isDiy();
				} else {
					//如果是普通浏览器
					//请求页面数据 使用默认定位-岳麓区
					this._isDiy();
				}
				// #endif
			},
			//定位：APP获取定位 微信浏览器获取定位
			getLocation() {
				let me = this;
				uni.getLocation({
					geocode: true,
					success: (res) => {
						//定位成功 获取经纬度
						this.longitude = res.longitude;
						this.latitude = res.latitude;
						//经纬度 存入缓存 （获取优选店铺接口需要用到）
						uni.setStorageSync("longitude", this.longitude);
						uni.setStorageSync("latitude", this.latitude);
						//请求页面数据
						this._isDiy();
					},
					fail: (e) => {
						//未开启定位，获取定位失败
						//提示 请在系统设置中打开定位服务
						this.opensetting2 = 3;
						this.is_showToast_obj.button = "我知道了";
						this.is_showToast_obj.title =
							"授权位置失败，请打开定位服务!";
						//请求页面数据 使用默认定位-岳麓区
						this._isDiy();
					},
					complete: () => {
					},
				});
			},
			/*
			 ** 定位结束
			 */

			// - - - 分割线 - - -

			/*
			 ** DIY开始
			 */

			//diy：
			async _diy_axios() {
				//获取diy首页数据
				await this.getDiy();
				//骨架屏
				this.load = true;
			},

			//diy：获取diy首页数据
			async getDiy() {
				const isArrayWithContent = (arr) => Array.isArray(arr) && arr.length > 0;
 
				const data = isArrayWithContent(this.$store.state.diyPagesInfo) ?
					this.$store.state.diyPagesInfo :
					isArrayWithContent(uni.getStorageSync('DIYPAGEINFO')) ?
					uni.getStorageSync('DIYPAGEINFO') : []; 
				const pageItem = data.find(v => v.key == this.homeOption.page_key)


				if (pageItem.page_context) {
					this.styleConfig = JSON.parse(pageItem.page_context).defaultArray;
				} else {
					this.styleConfig = pageItem.defaultArray;
				}
				this.load = false;
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
				this.loadingType = loadingType;
			},
			/*
			 ** DIY结束
			 */

			// - - - 分割线 - - -

			/*
			 ** 渲染数据开始
			 */

			//获取页面数据
			getShow() {
				//先请求diy，获取是否使用diy首页
				let data = {
					api: "app.index.hasDiy",
				};
				this.$req.post({
					data
				}).then((res) => {
					//调试diy的时候用的参数
					if (this.closeDiy) {
						this.is_diy = false;
					} else {
						//如果未开启diy 后台返回false 否则反之
						this.is_diy = res.data;
					}
					//修改：bug56676 定制项目反馈DIY问题 主线也存在
					//第一页数据
					//this.page = 1;
					//下拉加载更多
					this.loadingType = 0;
					//获取定位定位 -》 普通浏览器使用默认定位  -》 判断是否是diy首页，并初始化显示首页内容
					this.LaiKeTuiCommon.getUrlFirst(this.getLocationAuthorize);
				});
			},

			//获取数据判断：判断是否是diy首页，并初始化显示对应首页内容
			_isDiy() {
				//用户是否是会员
				// this.getHasGrade();
				//判断首页类型 ：diy首页 / 普通首页 
				this._diy_axios();

			},


			// - - - 分割线 - - -

			/*
			 ** 插件开始
			 */

			//VIP：续费
			xzbtnClick() {
				this.xzbtn = !this.xzbtn;
			},
			//VIP：续费
			shutClick() {
				this.vippupo = false;
				if (!this.xzbtn) {
					uni.removeStorageSync("isxufei");
					return;
				} else {
					var data = {

						api: "plugin.member.AppMember.CloseFrame"
					};
					this.$req
						.post({
							data,
						})
						.then((res) => {
						});
				}
			},
			//VIP：用户是否是会员
			async getHasGrade() {
				let data = {
					api: "app.index.get_membership_status",
				};
				let res = await this.$req.post({
					data
				});
				this.getGrade = true;
				if (res.code == 200) {
					this.hasGrade = res.data.membership_status;
					uni.setStorageSync("hasGrade", this.hasGrade);
				}
			},
			//限时秒杀：秒杀商品
			getSeckill() {
				let data = {
					api: 'plugin.sec.AppSec.goodsList',
					pageNo: 1,
					pageSize: 3,
				};
				this.$req.post({
					data
				}).then((res) => {
					let {
						data
					} = res;
					if (res.code == 200) {
						this.secList = res.data.list;
						this.secTitle = res.data.title;
					}
				});
			},
			//会员中心：会员商品
			getHasGrades() {
				let data = {

					api: "plugin.member.AppMember.MemberCenter"
				};
				this.$req
					.post({
						data,
					})
					.then((res) => {
						if (res.code == 200) {
							this.goodsArr = res.data.memberPro;
							this.is_vip = res.data.userInfo.grade;
							this.memberEquity = res.data.memberEquity;
						}
					});
			},
			//新品上市：商品列表
			getNewImg() {
				let data = {
					api: "app.index.new_arrival",
					page: 1,
					pageSize: 2,
				};
				this.$req
					.post({
						data,
					})
					.then((res) => {
						if (res.data.list) {
							this.newImgList = res.data.list.map((item) => {
								return item.imgurl;
							});
						}
					});
			},
			//好物优选：商品列表
			getRecommendImg() {
				let data = {
					api: "app.index.recommend",
					page: 1,
					pageSize: 2,
				};
				this.$req
					.post({
						data,
					})
					.then((res) => {
						if (res.data.list) {
							this.recommendImgList = res.data.list.map((item) => {
								return item.imgurl;
							});
						}
					});
			},
			/*
			 ** 插件结束
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
			close_mask() {
				uni.showTabBar();
				this.widgetinfoo = false;
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
					api: 'app.getcode.getCodeParameter',
					key: scene
				};
				this.$req.post({
					data
				}).then(res => {
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
				uni.redirectTo({
					url: sharUrl
				});
			}

		},
	};
</script>

<style scoped lang="less">
	@import url("@/laike.less");
	@import url("@/static/css/tabBar/home.less");

	@media screen and (min-width: 600px) {
		.zhiding {
			left: calc(50% + 270rpx);
		}
	}
</style>
