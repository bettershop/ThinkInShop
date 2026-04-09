<template>
	<view class="skeleton containerBg aa" :style="grbj">
		<!-- DIY首页 -->  
		<template v-if="is_diy && load">
			<view class="diy" :style="{
                    background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`,
                }">
				<lktauthorize ref="lktAuthorizeComp"></lktauthorize> 
                <blankPage :diyData='styleConfig'></blankPage>
			</view>
		</template>
 
		<!-- 普通首页区 -->
		<template v-if="!is_diy && load">
			<view class="container">
				<!-- 授权登录弹窗 -->
				<lktauthorize ref="lktAuthorizeComp"></lktauthorize>

				<!-- 头部 -->
				<view class="container_top">
					<!-- #ifdef MP -->
					<!-- 小程序占位 -->
					<!-- :title="language.home.title" -->
					<heads v-if="!scrollTopNum_show" :title="appTitle" :titleColor="titleColor" :border="true"
						:returnFlag="true" :bgImg="headBg"></heads>
					<view v-else class="data_h" style="height: 44px;background-color: pink">
						<view class="data_h_h" style="height: 44px; background: #fff"></view>
					</view>
					<!-- #endif -->

					<!-- #ifndef MP -->
					<!-- 普通占位 -->
					<view class="data_h" :style="{ height: halfWidth }">
						<view class="data_h_h" :style="{ height: halfWidth }"></view>
					</view>
					<!-- #endif -->

					<!-- 头部内容 -->
					<view style="height: 88rpx">
						<view class="container_top_item" :class="{ isBg: isBg }">

							<!-- 搜索框 -->
							<view class="container_top_item_center backgroundColor_FFF" :class="{
                                    isBg: isBg,
                                    new_top: scrollTopNum_show,
                                }" :style="{ flex: 1, marginLeft: 0 }" @tap="toUrl('/pagesA/search/search')">
								<image :src="syss" lazy-load="true"></image>
								{{ language.home.search_placeholder }}
							</view>
							<!-- 系统消息 -->
							<view v-if="!scrollTopNum_show" class="container_top_item_right" @tap="
                                    _navigateTo('/pagesB/message/systemMesage')
                                ">
								<image :src="grxx" lazy-load="true"></image>
								<view :style="{
                                        padding: xxnum > 9 ? '0 6rpx' : '',
                                    }" v-if="xxnum">{{ xxnum }}</view>
							</view>
						</view>
					</view>
				</view>

				<!-- banner -->
				<view class="swiperBox">
					<swiper class="swiper" autoplay="true" interval="3000" circular="true" @change="changeBanner">
						<swiper-item v-for="(item, index) in banner" :key="index">
							<image class="swiper_image_banner" mode="aspectFill" lazy-load="true"
								:src="getFastImg(item.image, 900, 330)" @tap="toUrl(item.url)"
								@error="setDefaultImage(index)" />
						</swiper-item>
					</swiper>
					<view class="swiper_dots_new">
						<view>{{
                            banner.length != 0 ? dotIndex + 1 : dotIndex
                        }}</view>
						<view>{{ banner.length }}</view>
					</view>
				</view>

				<!-- 金刚区 -->
				<view class="nav">
					<template>
						<view v-for="(item, index) of nav_list" class="nav_item" :key="index" @tap="navClick(item)"
							v-if="item.isshow ==1">
							<image :src="getFastImg(item.appimg == ''?default_img:item.appimg, 144, 144)"
								mode="aspectFit" lazy-load="true" @error="handleNavLisImg(index)"></image>
							<p>{{ item.name }}</p>
						</view>
					</template>
				</view>
				<!-- 插件/活动/优惠 内容区 -->
				<view class="content">
                    
                    <!-- #ifdef H5 --> 
                        <!-- 直播 -->
                    <!-- #endif -->
                    
					<!-- 会员VIP商品 -->

					<!-- 自定义活动 -->
					<template v-if="Marketing_list_zdy && Marketing_list_zdy.length">
						<template v-for="(item, index) in Marketing_list_zdy">
							<publicMode v-if="item.is_display" :isData="item" @_seeGoods="_seeGoods"></publicMode>
						</template>
					</template>

					<!-- 限时秒杀 -->
                    <!-- 积分商城 新品 好物 -->
                     
                    <template
                        v-if="
                            Marketing_list_jf || newImgList || recommendImgList
                        "
                    >
                       <!-- <publicModeJF
                            :isData="Marketing_list_jf"
                            :isDataXP="newImgList"
                            :isDataHW="recommendImgList"
                            :isJF="shaco_plugin.integral"
                            @_seeMore="navClick"
                            @_seeMoreJF="_navgTo"
                        >
                        </publicModeJF> -->
                    </template>
 
					<!-- 分销商品 -->
					</template> 
                    
					<!-- 推荐商家 -->
					</template>
                    
					<!-- 各分类推荐商品 -->
					<view class="goodsBox">
						<!-- 商品分类切换 -->
						<scroll-view-nav :is_ScrollView="bottom_list" @_changeNav="_changeNav"></scroll-view-nav>
						<!-- 商品瀑布流 -->
						<waterfall ref="waterfall" :goodsLike="false" :addShopCar="true" :mchLogo="false"
							:goodsPriceText="false" :isDataKO="isDataKO"
							:isHeight="class_pro.length > 0 ? minHeight : '0'" @_seeGoods="_seeGoods"
							@_addShopCar="_addShopCar" @_goodsNumber="_goodsNumber" :goodList="class_pro">
						</waterfall>
					</view>

					<!-- 0下拉加载更多/1加载中/2没有更多了 -->
					<uni-load-more :loadingType="loadingType"></uni-load-more>
				</view>
				<!-- 插件/活动/优惠 内容区结束 -->
			</view>
		</template>
  
		<!-- 弹窗提示区 -->
		<template>
			<!-- 提示：APP更新 -->
			<div class="mask" v-if="widgetinfoo">
				<div class="mask_all">
					<image :src="upbg" class="mask_bg" mode="widthFix" lazy-load="true" />
					<image :src="close" class="mask_close" lazy-load="true" @tap="close_mask()" />
					<b class="mask_titel">{{ newapp.version }}{{ language.home.Tips[2] }}</b>
					<div class="mask_content">
						<wxParse :content="newapp.note"></wxParse> 
					</div>
					<div class="mask_text">
						<div class="mask_btn" @tap="update()">
							{{ language.home.Tips[1] }}
						</div>
					</div>
				</div>
			</div>
            
            

			<!-- 取消成功 -->
			<view class="xieyi mask" style="background-color: initial;" v-if="is_susa">
				<view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
					<view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
						<image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
					</view>
					<view class="xieyi_title"
						style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
						{{ xieyiTitle }}
					</view>
				</view>
			</view>
  
			<!-- 提示：vip续费  -->
			</view>

			<!-- 提示：待付款订单弹窗 -->
			<template v-if="isShow_dialog_Pre && load">
				<showToast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" :load="true"
					@richText="is_showToast = 0" @confirm="_order">
				</showToast>
			</template>
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
    
	import publicModeVIP from "@/components/DIYComponents/oldPublicModeVIP.vue"; 
	import publicMode from "@/components/DIYComponents/publicMode.vue";
    
	import publicModeJF from "@/components/DIYComponents/publicModeJF.vue";
	import publicModeMCH from "@/components/DIYComponents/publicModeMCH.vue";
    
    import publicModeZB from "@/components/DIYComponents/oldPublicModeZB.vue";
    import publicModeMS from "@/components/DIYComponents/oldPublicModeMS.vue";
    import publicModeFX from "@/components/DIYComponents/oldPublicModeFX.vue"; 
	
    import blankPage from "@/components/DIYComponents/blankPage"; 
	// #ifdef APP-PLUS
    import wxParse from '@my-miniprogram/src/components/mpvue-wxparse/src/wxParse.vue'; 
	// import wxParse from "@/components/mpvue-wxparse/src/wxParse.vue";
	// #endif

	import skus from "@/components/skus.vue";
      import skeleton from "@/components/skeleton";
	import {
		getTimeDiff, 
	} from "@/common/util.js";
	import { buildUrlFromBackend } from "@/common/pluginsutil.js";
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
                grbj:'',
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
				//APP更新弹窗
				widgetinfoo: false, //APP更新弹窗：是否显示
				newapp: {}, //APP更新弹窗：标题内容
				upbg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon1/upbg1.png", //APP更新弹窗：背景图
			   
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
                    
				isBg: false, //定位：是否设置白色背景   
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
				bottom_list: [], //商品分类：分类列表
				class_pro: [], //分类商品：商品列表
				handleNum: 0, //分类商品：当前分类商品数量
				class_cid: "", //分类商品：当前分类id用于下拉加载更多
				opensetting: false, //推荐店铺：是否显示
				memberPlugin: false, //VIP商品：是否显示
				 
				distribution_list: [], //分销商品：商品列表 
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
				Intervalid: "", //用于清除定时器
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
                shaco_plugin:{},//存取插件配置参数
                newImgList: [], //新品上市图片列表
                recommendImgList: [], //好物优选图片列表
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
				default_img: "../../static/img/default-img.png",
				tabbarShow: true, // 判断自定tabbar组件是否显示
				list: []
			};
		},
		components: {
			// #ifdef APP-PLUS
			wxParse,
			// #endif
            notice,
			blankPage,  
			waterfall,
			scrollViewNav,
            publicModeVIP , 
            publicMode ,
            publicModeJF ,
            publicModeMCH , 
             
            publicModeZB ,
            publicModeMS , 
            publicModeFX , 
             skeleton,
            
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
			uni.removeStorageSync("signFlag");

			//移除移动店铺端拼团缓存
			uni.removeStorageSync("shacoList");
			uni.removeStorageSync("proIds");

			//清除倒计时
			clearInterval(this.Intervalid);

			//调试diy
			this.closeDiy = option.closeDiy;

			//当前屏幕高度
			// #ifdef H5s
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
			//清除定时器
			clearInterval(this.Intervalid);
		},
		mounted() {  
           
            /** 防止 从别的页面跳转过来 因为 页面栈缓存不触发  mounted 钩子 而封装的普通函数
            * 改为通过 shell 手动触发 
            */
            // this.triggerMounted() 
            //新品上市：商品列表
            this.getNewImg();
            //好物优选：商品列表
            this.getRecommendImg();
            //获取插件配置参数
            this.getPlus();
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
				sort: this.sort,
				sort_criteria: this.sort_criteria
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
						// item.volumes = item.volume;
						// item.volume = item.payPeople;
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
				requestH5Translation(reason = 'home') {
					// #ifdef H5
					if (typeof window.__LKT_REQUEST_TRANSLATION__ === 'function') {
						window.__LKT_REQUEST_TRANSLATION__(reason);
					}
					// #endif
				},
				getFastImg(url, width, height) {
				if (!url || typeof url !== "string") return url;
				if (url.indexOf("x-oss-process=") !== -1) return url;
				if (!/aliyuncs\.com/i.test(url)) return url;
				if (url.indexOf("Signature=") !== -1 || url.indexOf("OSSAccessKeyId=") !== -1) return url;
				const sep = url.indexOf("?") !== -1 ? "&" : "?";
				let process = `x-oss-process=image/resize,m_fill,w_${width || 750}`;
				if (height) process += `,h_${height}`;
				return `${url}${sep}${process}`;
			},
        _navgTo(url) {
                uni.navigateTo({
                    url: url,
                });
        },
        getPlus(){
                let data = {
                    api:"app.index.pluginStatus",
                };
                this.$req.post({data})
                    .then((res) =>{
                        if(res.code == 200){
                            this.shaco_plugin = res.data.plugin
                        }
                    })
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
                    if(res.code == 200){                        
                        if (res.data.list) {
                            this.newImgList = res.data.list.map((item) => {
                                return item.imgurl;
                            });
                        }
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
                    if(res.code == 200){  
                        if (res.data.list) {
                            this.recommendImgList = res.data.list.map((item) => {
                                return item.imgurl;
                            });
                        }
                    }
                });
            },
           triggerMounted(){
               this.$nextTick(()=>{
                   if(uni.getStorageSync('tabbar')){ 
                       const index = JSON.parse(uni.getStorageSync('tabbar')).findIndex(v=>(v.url || v.pagePath) == 'pages/tabBar/home')
                       uni.setStorageSync('tabbarIndex',index) 
                   }
               })
               
               uni.removeStorageSync('payTarget')
               this.loadingType = 0 
               this._axios();
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
                
               //防止重复点击跳转链接
               this.isClick = true;
               
               //好像没有用处
               if (uni.getStorageSync("isHomeShow")) {
               	uni.pageScrollTo({
               		scrollTop: 0,
               		duration: 10,
               	});
               	uni.removeStorageSync("isHomeShow");
               }
               
               /*
                ** 获取页面数据
                */
               this.getShow();  
               //分享进入
               if (this.homeOption.scene || this.homeOption.share == 'true') {
               	//小程序二维码分享 重定向
               	if (this.homeOption.scene) {
               		this._wxEwmShare(this.homeOption.scene)
               	} else {
               		this._share(this.homeOption)
               	}
               }
               
               // 调用弹窗
               setTimeout(() => {
               	this.$refs.mynotice.getData()
               }, 0)
           },
				handleTabChange({
					index,
					item
				}) {
                
				const tabbar = this.$store.state.tabBarList
				const data = this.$store.state.diyPagesInfo || uni.getStorageSync('DIYPAGEINFO')
					if (tabbar && tabbar.length > 0 && this.tabbarShow) { 
						const oneTablePagsItem = data.find(v => {
							return v.key == tabbar[index].key
						}) 
						this.styleConfig = this.objToArr(oneTablePagsItem.defaultArray || {});
						this.$nextTick(() => {
							this.requestH5Translation('diy-tab-change');
						});
					}
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
					let urls = "/pages/pay/PayResults?i_state=" + option.payResult + '&type=GM'
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
		 
			 
			// nav图标默认图片更改
			handleNavLisImg(index) {
				this.nav_list[index].appimg =require("@/static/img/default-img.png");
			},
			// 更改默认图片
			setDefaultImage(index) {
				setTimeout(() => {
					this.banner[index].image = require("@/static/img/default-img.png");
				})
			},
			
			 
			//轮播图切换
			changeBanner(e) {
				this.dotIndex = e.detail.current;
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
                        if(res.code != 200){  
                            return
                        }
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
			//营销商品跳转解析
			_resolveMarketingUrl(marketingParams) {
				if (!marketingParams) return "";
				let params = marketingParams;
				if (typeof params === "string") {
					const trimmed = params.trim();
					if (!trimmed || trimmed === "null" || trimmed === "undefined") return "";
					if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
						try {
							params = JSON.parse(trimmed);
						} catch (e) {
							params = trimmed;
						}
					} else {
						params = trimmed;
					}
				}
				if (Array.isArray(params)) {
					for (let i = 0; i < params.length; i += 1) {
						const url = this._resolveMarketingUrl(params[i]);
						if (url) return url;
					}
					return "";
				}
				if (params && typeof params === "object") {
					const plugin = params.plugin || params.pluginCode || params.plugin_key || params.code || params.type;
					const paramStr = params.paramStr || params.params || params.param || params.query || params.urlparam || params.paramStr;
					if (plugin) {
						const url = buildUrlFromBackend(plugin, paramStr);
						if (url) return url;
					}
					const keys = Object.keys(params);
					for (let i = 0; i < keys.length; i += 1) {
						const key = keys[i];
						const value = params[key];
						if (!value) continue;
						if (typeof value === "string") {
							const url = buildUrlFromBackend(key, value);
							if (url) return url;
						} else if (typeof value === "object") {
							const innerParam = value.paramStr || value.params || value.param || value.query || value.urlparam;
							const url = buildUrlFromBackend(key, innerParam);
							if (url) return url;
						}
					}
					return "";
				}
				if (typeof params === "string") {
					const trimmed = params.trim();
					if (!trimmed) return "";
					const parts = trimmed.split("?");
					if (parts.length > 1 && parts[0]) {
						const url = buildUrlFromBackend(parts[0], parts.slice(1).join("?"));
						if (url) return url;
					}
				}
				return "";
			},
			//查看商品详情
			_seeGoods(payload) {
				let target = null;
				let id = payload;
				if (payload && typeof payload === "object") {
					target = payload;
					id = payload.id;
				} else {
					target = this.class_pro.find((item) => String(item.id) === String(payload));
				}
				const url = this._resolveMarketingUrl(target && target.marketingParams);
				if (url) {
					if (id) {
						this.$store.state.pro_id = id;
					}
					uni.navigateTo({
						url,
					});
					return;
				}
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
						// item.volumes = item.volume;
						// item.volume = item.payPeople;
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
			toUrl(url, is_login = false) {
				//如果是diy 
				if (this.is_diy) {
					if (is_login) {
						this.isLogin(() => {
							 
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
				if (url.includes("blankpage")) {
					uni.navigateTo({
						url
					});
                    return
				}
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
			 ** DIY开始
			 */

			//diy：
				async _diy_axios() {
					//获取diy首页数据
					await this.getDiy();
					//骨架屏
					this.load = true;
					this.$nextTick(() => {
						this.requestH5Translation('diy-loaded');
					});
				},

			//diy：获取diy首页数据
			async getDiy() {
			  
				//diy首页请求
				let data = {
					api: "plugin.diy.index",
				};
				let {
					data: res
				} = await this.$req.post({
					data
				});

				uni.stopPullDownRefresh();

				// 渲染 tabbar信息
				if (res.tab_bar) {
					const {
						tab_bar,
						tabber_info,
					} = res
					const tabbar = JSON.parse(tab_bar || '[]')
					this.$store.commit('SET_TABBER', tabbar)
					uni.setStorageSync('tabbar', JSON.stringify(tabbar) )

					uni.setStorageSync('tabbar_info', tabber_info)
					this.$store.commit('SET_TABBARINFO', JSON.parse(tabber_info))
					const tabberInfo = JSON.parse(tabber_info)  
                    
                    this.grbj = ''
                    if(tabberInfo.backType == 0){
                        // 颜色设置
                        this.grbj = { backgroundColor: tabberInfo.colorTwo }
                    }else {
                        // 图片设置
                         this.grbj =  { backgroundImage: `url(${tabberInfo.imgurl});
                                           background-repeat: no-repeat;
                                           background-size: 100% 100%;` 
                                       }
                    }
                    
					if (tabbar && tabbar.length > 0) {
						const data = JSON.parse(res.data)
						this.$store.commit('SET_DIYPAGEINFO', data)
						uni.setStorageSync('DIYPAGEINFO', data)

						const oneTablePagsItem = data.find(v => {
							return v.key == tabbar[0].key
						})
                        
						this.styleConfig = oneTablePagsItem.defaultArray
					} 

				} else {
					this.styleConfig = this.objToArr(JSON.parse(res.data));
				}
 
				this.objToArr(res.data).forEach((item) => {
					if (item.name === "homeBg") {
						this.bgColor = item.bgColor.color;
					}
					if (item.name === "headerSerch") {
						this.onReachBottomBg = item.bgColor.color;
					}
				});
					if (res.grade && res.grade != 0) {
						this.is_grade = true;
					} 
					this.$nextTick(() => {
						this.requestH5Translation('diy-data-ready');
					});
				},

			//diy：对象转数组
			objToArr(data) {
				let obj = Object.getOwnPropertyNames(data || {});
				let m = obj.map((key) => data[key]);
				return m;
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
			 
					//下拉加载更多
					this.loadingType = 0;
					 
                    this._isDiy();
				});
			},

			//获取数据判断：判断是否是diy首页，并初始化显示对应首页内容
			_isDiy() { 
				//判断首页类型 ：diy首页 / 普通首页  
				if (this.is_diy) {
					this.tabbarShow = true
					//diy首页
					this._diy_axios();
				} else {
					this.tabbarShow = false   
                    
                    this.grbj =`background-image: url("${this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL}images/icon1/auction/gr                       bj.png");background-size: 100vw 100vh;background-repeat: no-repeat; `
                    
					//普通首页
					this._axios();
				}
				uni.setStorageSync('tabbarShow', this.tabbarShow) 
			},

			//初始化数据
			async _axios() {
			  
				// 首页数据
				var data = {
					api: "app.index.index",
					longitude: "112.951227", //定位：经度 
					latitude:'28.227965', //28.227965 定位：纬度
				};
				this.$req
					.post({
						data,
						xhrFields: {
							withCredentials: true,
						},
					})
					.then((res) => {
						uni.stopPullDownRefresh();
						let {
							data: {
								banner,
								Marketing_list,
								list2,
								xxnum, 
								sign_status,
								go_group_list,
								grade, 
								login_status,
								nav_list,
							},
						} = res;
                        if(res.code != 200){
                            return
                        }
						this.vippupo = res.data.grade_remind; 
						this.memberPlugin = res.data.memberPlugin;
						this.livingStatus = res.data.livingStatus
						if (grade && grade != 0) {
							this.is_grade = true;
						} else {
							this.is_grade = false;
						} 
						this.Marketing_list = Marketing_list;
						this.Marketing_list.forEach((item, index) => {
							if (item.activity_type == 0) {
								//去重
								let is_true = this.Marketing_list_zdy.every(
									(it, idx) => {
										return it.id != item.id;
									}
								);
								if (is_true) {
									// 自定义活动
									this.Marketing_list_zdy.push(item);
								}
							} else if (
								item.activity_type == 1 &&
								item.plug_type == 7
							) {
								//积分商品活动
								this.Marketing_list_jf = item;
							}
						});
					 
					 
						this.nav_list = nav_list;

						// 小程序端需要隐藏直播功能入口，但h5要保留
						// #ifdef MP-WEIXIN
						this.nav_list = nav_list.filter(v => v.name != '直播')
						// #endif 

						this.banner = banner;
						list2.filter((item) => {
							if (item.list) {
								item.list.filter((it) => {
									it.vip_price = Number(it.vip_price).toFixed(2);
									it.vip_yprice = Number(it.vip_yprice).toFixed(
										2
									);
								});
							}
						});
						this.bottom_list = list2;
				 
						//限制消息数超过99时显示99+
						this.xxnum = xxnum > 99 ? "99+" : Number(xxnum);
					 
						//
						if (Marketing_list) {
							for (let item of Marketing_list.values()) {
								if (
									item.activity_type == 1 &&
									item.plug_type == 8
								) {
									if (
										item.list.current_time &&
										item.list.current_time[0]
									) {
										this.setCountDown(
											item.list.current_time[0]
										);
									}
									break;
								}
							}
						}
						//加载完成 隐藏骨
						this.load = true;

							// #ifdef H5
							// 数据加载并渲染后，走统一调度器触发翻译，避免与 DOM 更新抢时机
							if (typeof window.__LKT_REQUEST_TRANSLATION__ === 'function') {
								this.$nextTick(() => {
									window.__LKT_REQUEST_TRANSLATION__('home-data-loaded');
								});
							}
							// #endif
					})
					.catch((error) => {
						uni.stopPullDownRefresh();
					});
			},
			/*
			 ** 渲染数据结束
			 */

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
		 
			 
			/*
			 ** 插件结束
			 */
 
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
								// uni.hideTabBar();
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

			/*
			 ** 倒计时开始
			 */

			//倒计时：年-月-人 时：分：秒
			setCountDown(item) {
				clearInterval(this.Intervalid);
				let year = new Date().getFullYear();
				let month = new Date().getMonth() + 1;
				let dates = new Date().getDate();
				let endtime = "";
				if (item.type == 2) {
					endtime = `${year}-${month}-${dates} ${item.starttime}`;
				} else {
					endtime = `${year}-${month}-${dates} ${item.endtime}`;
				}
				this.timeFun(endtime, item);
				this.Intervalid = setInterval(() => {
					this.timeFun(endtime, item);
				}, 1000);
			},
			//倒计时：定时事件
			timeFun(endtime, item) {
				let result = getTimeDiff(endtime);
				this.time.hours = result.hours;
				this.time.minutes = result.minutes;
				if (result.seconds < 0 || result.hours < 0 || result.minutes < 0) {
					clearInterval(this.Intervalid);
					this._axios();
					return;
				}
				this.time.seconds = result.seconds;
			},
			/*
			 ** 倒计时结束
			 */

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

	.diy *:last-child {
		margin-bottom: 20rpx !important; 
	}
</style>
