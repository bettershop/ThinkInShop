<template>
	<view class="container" :style="grbj"> 
		<view class="isHeader">
			<!-- 如果是小程序 设置顶部距离 -->
			<!-- #ifdef MP -->
			<heads :title="language.zdata.grzx" :bgColor="bgColor" :titleColor="titleColor" :border="true"
				:returnFlag="true" :bgImg="headBg"></heads>
			<!-- #endif -->
			<!-- 如果不是小程序 且是如果H5，则不需要设置顶部距离； 如果是APP，则需要设置顶部距离 -->
			<!-- #ifndef MP -->
			<view class="isHeader_top" :style="{paddingTop: isPaddingTop}">
				<image class="isHeader_setUp" lazy-load="true" :src="grsz" @tap="_navigateTo1('/pagesB/setUp/setUp')">
				</image>
				<view class="isHeader_message">
					<image class="isHeader_message_img" lazy-load="true" :src="grxx"
						@tap="_navigateTo1('/pagesB/message/systemMesage')">
					</image>
					<view class="isHeader_message_num" :style="{padding:user.xxnum > 9 ? '0 6rpx':''}"
						v-if=" access_id1 && user.xxnum > 0 ">
						{{user.xxnum > 99 ? '99+' : user.xxnum}}
					</view>
				</view>
			</view>
			<!-- #endif -->
		</view>

		<view class="card">
			<!-- 未登录状态 -->
			<template v-if="!access_id1">
				<view class="card_bj">
					<!-- 有未登录默认头像 -->
					<image v-if="defaultHeaderImgUrl" :src="defaultHeaderImgUrl "
						@tap="_navigateTo1('/pagesC/my/myInfo')" mode=""></image>
					<!-- 无未登录默认头像 -->
					<image v-if="!defaultHeaderImgUrl" :src="unLogin" @tap="_navigateTo1('/pagesC/my/myInfo')" mode="">
					</image>
				</view>
				<view class="card_center">
					<!-- 有未登录默认名称显示【默认名称】 没有默认名称显示【去登录】 -->
					<view class="card_username">
						{{ defaultUserName ? defaultUserName : language.my.loginPrompt }}
					</view>
					<!-- H5/APP (未登录状态按钮) -->
					<template>
						<!-- #ifndef MP --> 
						<view class="card_btn card_btn1" @tap="_navigateto('/pagesD/login/newLogin?landing_code=1')">
							{{language.my.toLogin}}
						</view>
						<!-- #endif -->
					</template>
					<!-- 小程序（未登录状态按钮） -->
					<template>
						<!-- #ifdef MP-ALIPAY -->
						<button class="card_btn card_btn1" open-type="getAuthorize" size="mini" type="primary"
							scope='userInfo' @tap="onGetAuthorize">
							{{language.my.authorization}}
						</button>
						<!-- #endif -->
						<!-- #ifdef MP-BAIDU -->
						<button class="card_btn card_btn1" open-type="getUserInfo" size="mini" type="primary"
							@tap="bdAuth">
							{{language.my.authorization}}
						</button>
						<!-- #endif -->
						<!-- #ifdef MP-TOUTIAO -->
						<button class="card_btn card_btn1" open-type="getUserInfo" size="mini" type="primary"
							@tap="ttAuth">
							{{language.my.authorization}}
						</button>
						<!-- #endif -->
						<!-- #ifdef MP-WEIXIN -->
						<button class="card_btn card_btn1" open-type="getUserInfo" type="primary" size="mini"
							@tap="bindGetUserInfo">
							{{language.my.djdl}}
						</button>
						<!-- #endif -->
					</template>
				</view>
			</template>
			<!-- 登录状态 -->
			<template v-if="access_id1">
				<view class="card_bj">
					<!-- 登录状态头像 -->
					<image @tap="_navigateTo1('/pagesC/my/myInfo')" :src="user.headimgurl" mode=""
						@error="handleErrorImg"></image>
				</view>
				<view class="card_center">
					<!-- 已登录 用户名称/会员小钻石图标 -->
					<view class="card_username">
						{{user.user_name}}
						<img v-if="grade&&plugin.member==1" style="width: 32rpx;height: 32rpx;margin-left: 20rpx;"
							:src="grade ? viplogo : ''" alt="" />
					</view>
					<!-- 已登录 会员信息显示 -->
					<template>
						<!-- 未开通会员 显示 立即解锁会员图片 -->
						<view v-if="grade == 0 &&plugin.member==1" style="display: flex;">
							<image @click="vipClick" :src="lang_text=='en'?wktvip_en:wktvip"
								style="width: 242rpx;height: 64rpx;margin-top: 10rpx;"></image>
						</view>
						<!-- 已开通会员 显示 会员到期时间 -->
						<view v-else-if="grade == 1&&plugin.member==1">
							<view class="viptitle">{{gradeEndTime && gradeEndTime.substring(0,10)}}{{language.my.dq}}</view>
						</view>
					</template>
				</view>
			</template>
			<!-- 小程序端，我的消息 -->
			<!-- #ifdef MP -->
			<view class="mp-top-message" @tap="_navigateTo1('/pagesB/message/systemMesage')">
				<view class="mp-message">
					<image :src="grxx" class="mp-message-img" lazy-load="true"></image>
					<view class="mp-xxnum" v-if="access_id1&&user.xxnum>0">{{ user.xxnum > 99 ? '99+' : user.xxnum }}
					</view>
				</view>
				<view class="mp-message-title">
					<view>{{language.my.myMessage}}</view>
					<image :src='jiantou2x'></image>
				</view>
			</view> 
			<!-- #endif -->
		</view> 
		<view class="card_bottom">
            <template v-if="plugin.coupon"> 
                <view @tap="_navigateTo1('/pagesB/coupon/mycoupon')"
                    v-if="!pageConfig || iconConfig.checkList.includes('kq')">
                    <view>{{access_id1?user.coupon_num:'—'}}</view>
                    <text>{{language.my.couponNum}}</text>
                </view>
            </template>
            </template>
			
            <template v-if="plugin.wallet == 1">
                <view @tap="_navigateTo1('/pagesB/myWallet/myWallet')"
                    v-if="!pageConfig || iconConfig.checkList.includes('ye')">
                    <view>
                        {{access_id1?currency_symbol:''}}{{ access_id1 ? LaiKeTuiCommon.getPriceWithExchangeRate(user.money,this.exchange_rate):'—'}}
                    </view>
                    <text>{{language.my.remainingNum}}</text>
                </view>
            </template>
              
            <template>
                <view @tap="_navigateTo1('/pagesC/collection/collection')"
                	v-if="!pageConfig || iconConfig.checkList.includes('sc')">
                	<view class="relative">
                		{{access_id1?user.collection_num:'—'}}
                	</view>
                	<text>{{language.my.my_collection}}</text>
                </view>
            </template>
			
		</view>
 
		<view class="relativell" v-if="!Object.keys(diyObj || {}).length">
			<image class="relative-imgll" @click="vipClick()" v-if="plugin.member==1"
				:src="lang_text=='en'?gerenzhongx_en:gerenzhongx"></image>
			<view class="relative-box">
				<view class="relative-box-box" v-if="plugin.mch == 1 " @tap="_navigateTo1('/pagesA/myStore/myStore')">
					<view class="relative-box-title">
						<view class="relative-box-title-txtl">{{language.my.wddp}}</view>
						<view class="relative-box-title-txt">{{language.my.gldpc}}</view>
					</view>
					<view class="relative-box-img">
						<image class="relative-box-img-img" :src="grwddp"></image>
					</view>
				</view>
				<view class="relative-box-box" v-if="plugin.mch == 1" @tap="_navigateTo1('/pagesB/myWallet/bankCard')">
					<view class="relative-box-title">
						<view class="relative-box-title-txtl">{{language.my.yhkgl}}</view>
						<view class="relative-box-title-txt">{{language.my.glyhk}}</view>
					</view>
					<view class="relative-box-img">
						<image class="relative-box-img-img" :src="yhkgl"></image>
					</view>
				</view>
				<!-- 当我的店铺不显示时，银行卡管理用这种方式显示 -->
				<view class="relative-box-box" v-if="plugin.mch != 1" @tap="_navigateTo1('/pagesB/myWallet/bankCard')"
					style="height: 96rpx;padding: 0 24rpx;flex: 1;">
					<view class="relative-box-img">
						<image class="relative-box-img-img" :src="yhkgl"></image>
						<view class="relative-box-title" style="flex-direction: row;align-items: center;">
							<view class="relative-box-title-txtl">{{language.my.yhkgl }}</view>
							<view class="relative-box-title-txt" style="margin-top: initial;">/{{language.my.glyhk }}
							</view>
						</view>
					</view>
					<view class="relative-box-img" style="margin-right: 0;">
						<image class="relative-box-img-img" :src="jiantou" style="width: 32rpx;height: 44rpx;"></image>
					</view>
				</view>
			</view>
		</view>

                            
                            <!-- 发票管理 -->
                            <view class="Functioncenter-box" @tap="_navigateTo('/pagesB/invoice/invoiceManagement')">
                                <view class="Functioncenter-img">
                                    <image :src="grfpgl" class="Functioncenter-img-img"></image>
                                </view>
                                <view class="Functioncenter-title">{{language.my.fpgl}}</view>
                            </view> 
                            
                            <!-- 分销中心 -->
                            <template v-if="user.isDistribution">
                                <!-- <template v-if="true"> -->
                                <view class="Functioncenter-box" v-if="plugin.distribution  == 1"
                                    @tap="_navigateTo('/pagesA/distribution/distribution_center')">
                                    <view class="Functioncenter-img">
                                        <image :src="fenxiao" class="Functioncenter-img-img"></image>
                                    </view>
                                    <view class="Functioncenter-title">{{language.my.my_distribution}}</view>
                                </view>
                            </template>
                            </template> 
                            
                            <!-- 我的预售 -->
                                @tap="_navigateTo('/pagesC/preSale/order/myOrder')">
                                <view class="Functioncenter-img">
                                    <image :src="yusho" class="Functioncenter-img-img"></image>
                                </view>
                                <view class="Functioncenter-title">{{language.my.wdys}}</view>
                            </view> 
                            <!-- 直播Start -->
                            <!-- #ifdef H5 -->
                                    <view class="Functioncenter-title">{{language.my.my_zbzx}}</view>
                                </view>
                                    <view class="Functioncenter-title">{{language.my.my_zbdd}}</view>
                                </view>
                                    <view class="Functioncenter-title">{{language.my.my_zzdzb}}</view>
                                </view> 
                            <!-- #endif -->
                            <!-- 直播END -->
                            
                                <view class="Functioncenter-title">{{language.my.zc}}</view>
                            </view>
                            <!-- 限时折扣 flashsale-->
                                <view class="Functioncenter-title">{{ language.discount.discount_list.xszk }}</view>
                            </view>
                            <!-- 我的客服 -->
                            <view class="Functioncenter-box" style="position: relative;"
                                @tap="_navigateTo1('/pagesB/message/buyers_service/Regular_customer?mch_id=' + mch_id)">
                                <view class="Functioncenter-img">
                                    <image :src="my_kefu" class="Functioncenter-img-img"></image>
                                </view>
                                <view class="Functioncenter-title">{{language.myStore.wdkf}}</view>
                                <span v-if="userOnlineMessageNotRead" class="my-kefu-red"></span>
                            </view>
                             <template v-if="Object.keys(functionConfig || {}).length">
                                 
                                <!-- 银行卡 -->
                                <view class="Functioncenter-box" style="position: relative;"
                                    @tap="_navigateTo1('/pagesB/myWallet/bankCard')">
                                    <view class="Functioncenter-img">
                                        <image :src="yhkgl" class="Functioncenter-img-img"></image> 
                                    </view>
                                    <view class="Functioncenter-title">{{language.my.yhkgl }}</view> 
                                </view>
                                <!-- 我的店铺 -->
                               <view class="Functioncenter-box" style="position: relative;"
                                    @tap="_navigateTo1('/pagesA/myStore/myStore') ">
                                    <view class="Functioncenter-img">
                                        <image :src="grwddp" class="Functioncenter-img-img"></image>
                                    </view>
                                    <view class="Functioncenter-title">{{language.my.wddp}}</view>
                                    <span v-if="userOnlineMessageNotRead" class="my-kefu-red"></span>
                                </view>
                            </template>
                            
                            <!-- 小程序端，设置按钮 -->
                            <!-- #ifdef MP -->
                            <view class="Functioncenter-box" @tap="_navigateTo1('/pagesB/setUp/setUp')">
                                <view class="Functioncenter-img">
                                    <image :src="mySet" class="Functioncenter-img-img"></image>
                                </view>
                                <view class="Functioncenter-title">{{language.my.wdsz}}</view>
                            </view>
                            <!-- #endif -->
                        </view> 
                    </view>
                </template>
               
                        <!-- 竞拍 -->
                            @tap="_navigateTo('/pagesA/myStore/MyBidding/MyBidding')">
                            <view>
                                <image :src="grwdjp"></image>
                                <span>{{language.my.my_bidding}}</span>
                            </view>
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- 兑换 -->
                            @tap="_navigateTo('/pagesB/integral/exchange')">
                            <view>
                                <image :src="grwddh"></image>
                                <span>{{language.my.my_exchange}}</span>
                            </view>
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- 发票管理 -->
                        <view class="new_function_box" @tap="_navigateTo('/pagesB/invoice/invoiceManagement')">
                            <view>
                                <image :src="grfpgl"></image>
                                <span>{{language.my.fpgl}}</span>
                            </view>
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- 分销中心 -->
                            <template v-else>
                                <view class="new_function_box" style="position: relative"
                                    @tap="_navigateTo('/pagesB/order/myOrder?status=0&type=FX')">
                                    <view>
                                        <image :src="fenxiao"></image>
                                        <span>{{language.my.my_distribution}}</span>
                                        <view class="order_bottom_icon_s" style="right: 80rpx;top: auto;">
                                            <view v-if="fxNum && fxNum>0" class="order_bottom_icon">{{fxNum}}</view>
                                        </view>
                                    </view>
                                    <view>
                                        <image :src="jiantou"></image>
                                    </view>
                                </view>
                            </template>
                        </view>
                        <!-- 我的预售 -->
                            @tap="_navigateTo('/pagesC/preSale/order/myOrder')">
                            <view>
                                <image :src="yusho"></image>
                                <span>{{language.my.wdys}}</span>
                            </view>
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- #ifdef H5 -->
                            @tap="_navigateTo('/pagesD/liveStreaming/anchorCenter')">
                            <view>
                                <image :src="zbzx_icon"></image>
                                <span>{{language.my.my_zbzx}}</span>
                            </view>
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- 直播订单 -->
                            @tap="_navigateTo('/pagesD/liveStreaming/liveStreamingOrder')">
                            <view>
                                <image :src="zbdd_icon"></image>
                                <span>{{language.my.my_zbdd}}</span>
                            </view>
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- 赞过的直播 -->
                            @tap="_navigateTo('/pagesD/liveStreaming/likeTheLiveBroadcast')">
                            <view>
                                <image :src="zgdzb_icon"></image>
                                <span>{{language.my.my_zzdzb}}</span>
                            </view>
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- #endif -->
                        <!-- 限时折扣 -->
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        
                        <template v-if="Object.keys(functionConfig || {}).length">
                            <!-- 银行卡 -->
                             <view class="new_function_box" @tap="_navigateTo1('/pagesB/myWallet/bankCard')" >
                                 <view>
                                     <image :src="yhkgl"></image>
                                     <span>{{language.my.yhkgl}}</span>
                                 </view>
                                 <view>
                                     <image :src="jiantou"></image>
                                 </view>
                             </view>
                             <!-- 我的店铺 -->
                             <view class="new_function_box" @tap="_navigateTo1('/pagesA/myStore/myStore')" >
                                 <view>
                                     <image :src="grwddp"></image>
                                     <span>{{language.my.wddp}}</span>
                                 </view>
                                 <view>
                                     <image :src="jiantou"></image>
                                 </view>
                             </view>
                         </template>
                      
                        <!-- 我的客服 -->
                        <view class="new_function_box"
                            @tap="_navigateTo1('/pagesB/message/buyers_service/Regular_customer?mch_id=' + mch_id)">
                            <view>
                                <image :src="my_kefu"></image>
                                <span>{{language.myStore.wdkf}}</span>
                            </view>
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- 种草 -->
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- #ifdef MP -->
                        <!-- 我的设置 -->
                        <view class="new_function_box" @tap="_navigateTo1('/pagesB/setUp/setUp')">
                            <view>
                                <image :src="mySet"></image>
                                <span>{{language.my.wdsz}}</span>
                            </view>
                            <view>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- #endif -->
                    </view>
                    <view v-if="class_pro.length == 0" style="height: 96rpx;"></view>
                </template>
            </template> 
                    
			<div v-if="class_pro.length > 0" class="skeleton-rect"> 
                <h3 style="color:#333;margin: 20rpx 0px;font-weight: bold;">{{ language.my.wntj }}</h3>
			</div>
			<view class="goodsBox" v-if="class_pro.length">
				<waterfall ref="waterfall" :goodsLike="false" :addShopCar="true" :mchLogo="false"
					:goodsPriceText="false" :isDataKO="isDataKO" :isHeight="class_pro.length > 0 ? minHeight : '0'"
					@_seeGoods="_goods" @_addShopCar="_addShopCar" :goodList="class_pro">
				</waterfall>
				<!-- 0下拉加载更多/1加载中/2没有更多了 -->
				<uni-load-more v-if="class_pro.length > 0" :loadingType="loadingType"></uni-load-more>
			</view>

		</view>


		<!-- 设置密码弹窗 -->
		<showToast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" :isOneBtn="false"
			@richText="closePsw" @confirm="toSetPsw">
		</showToast>
		<maskM :content="maskContent" :display="display" @mask_value="onHandle"></maskM>
		<!--  遮罩：点击购物小图标弹出  -->
		<skus ref="attrModal" @confirm="_confirm"></skus>
		<!-- 公告提示弹窗 -->
		<notice ref="mynotice" ></notice>

	</view>
</template>

<script>
	import {
		mapMutations,
		mapState
	} from 'vuex';
     import notice from '@/components/notice.vue'
	import maskM from "@/components/maskM";
	import skus from '@/components/skus.vue';
	import waterfall from "@/components/aComponents/waterfall.vue";
	import showToast from "@/components/aComponents/showToast.vue"; 
	import {
		getDIYPageInfoById
	} from "@/common/util.js";
    import {
    	funCenterByType
    } from "@/common/enus.js";
    
    
    // #ifdef MP-WEIXIN
        import LaiketuiWeixinAuth from "@/components/laiketuiauth/mpweixin/mpweixin.js";
    // #endif
    
	export default {
		data() {
			return {
                diyObj:{},
				currency_symbol: this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL,
				exchange_rate: this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE,
				fsNum: '',
				fxNum: '',
				ptNum: '',
              
				title: '个人中心', //标题
				titleColor: '#333333', //标题字体颜色
				bgColor: [{
						item: "#FFFFFF",
					},
					{
						item: "#FFFFFF",
					},
				],
				isBg: false, //下滑后状态栏样式
				access_id: '',
				access_id1: false,
				curent_user_currency_exchange_rate: 1, //用户使用货币基于商城基础货币的汇率  
				bg_vip: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/member/vippupu.png)',
				rate: '',
				vip_check: false,
                zc: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zc.png', //直播icon
                zbzx_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zbzx_icon.png', //直播icon
                zgdzb_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zgdzb_icon.png', //直播icon
                zbdd_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zbdd_icon.png', //直播icon
				grdfk: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grdfk.png',
				grwdpt: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grwdpt.png',
				grwdms: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grwdms.png', 
				grwdjp: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grwdjp.png',
				grwddh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grwddh.png',
				grfpgl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grfpgl.png',
				grdfh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grdfh.png',
				grdsh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grdsh.png',
				tksh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/tksh.png',
				grxx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grxx.png',
				grsz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grsz.png',
				grwddp: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grwddp.png',
				yhkgl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/yhkgl.png',  
				grbj: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/auction/grbj.png);background-size:750rpx 100%;',
				headBg: 'url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/auction/grbj.png) 750rpx 100%;',
    
				//vip会员中心
				gerenzhongx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/gerenzhongx.png',
				gerenzhongx_en: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/gerenzhongx_en.png',
				xz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/xz.png',
				yuanquan: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/yuanquan.png', 
				guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/guanbi.png', 
				fenxiao: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/fenxiao.png', 
				//箭头-更多图标
				jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
				jiantou2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou2x.png',
				// 授权手机号的红色右箭头 
				my_dpj: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/my_dpj.png', 
				wktvip: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/wktvip.png',
				wktvip_en: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/wktvip_en.png',
				my_kefu: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/my_kefu.png', 
				mySet: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mySet.png',
				xszk_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xszk_icon.png',
				//默认头像
				unLogin: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/unLogin.png', 
				display: false,
				viplogo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/viplogo.png',
				notviplogo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/noviplogo.png',
				defaultPicture: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png', 
				user: {},
				sign: 0,
				plugin: {},
				orderNum: {},
				synchronize_type: 0, //是否是合并账号信息 
				mobile: '',
				grade: '', 
				gradeEndTime: '', 
				vippupo: false,
				xzbtn: false, 
				memberEquity: [],
				yusho: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/yusho_new.png',
				haveAllUserInfo: false, // 是否未完善个人信息，true则需要跳转去完善
				//功能中心 个数
				gn_num: 0,  
				is_showToast: 0, //是否显示设置密码弹窗
				 
				is_showToast_obj: {
					title: '提示', //提示文字
					button: '取消', //按钮
					content: '暂未设置密码，前往设置', //文本内容
					endButton: '确认', //确认按钮
				}, //设置密码弹窗内容
				showToastType: '', //1完善资料 2设置手机号 ‘’设置密码
				defaultHeaderImgUrl: '',
				defaultUserName: '',
				lang_text: '', //判断是属于中文还是英文
				shaco_plugin: {}, //存取插件配置参数 
				isDataKO: false, //商品瀑布：数据是否初始化（切换导航时清空左右div数据）
				minHeight: "", //瀑布流最小高度，用来固定切换导航时显示位置
				class_pro: [], //分类商品：商品列表
				// 加入购物车弹窗所需参数
				fastTap: true,
				proid: "",
				haveSkuBean: "",
				mch_id: 0, // 默认自营店id
				num: "",
				numb: "",
                
				page: 1, 
				loadingType: 0,
				userOnlineMessageNotRead: 0, //用户是否有客服未读消息 -》 0没有  >0有
				gesTime: '', //定时器
				onHide: false, //页面是否已隐藏  用于 页面划入后台时 不调用 客服信息接口
				userMessage: true, //是否发送监听请求 
                
				pageConfig: null, //DIY配置

				orderCondfig: {}, //订单配置
				background: '',

				functionConfig: {}, //功能中心
				background1: '',

				iconConfig: {}, // 资产信息
			};
		},
		watch: {
			haveAllUserInfo(e) {
				if (e) {
					this.showToastType = 1
					this.is_showToast = 4
					this.is_showToast_obj.title = '温馨提示'
					this.is_showToast_obj.button = '暂不完善'
					this.is_showToast_obj.endButton = '去完善'
					this.is_showToast_obj.content = '您的资料未完善，请先完善资料！'
					//调接口标记已经提示过，不管去不去完善下次都不会显示了；周家辉要求。
					let data = {
						api: 'app.User.isDefaultValue',
					}
					this.$req.post({
						data
					}).then(res => {
						if (res.code == 200) {
						}
					})
				}
			}
		}, 
		computed: {
			...mapState({
				tabBarList: 'tabBarList'
			}),
			maskContent() {
				return `是否同步${this.user.mobile}的 账号信息`;
			},
			isPaddingTop() {
				let top = 0
				//如果是H5   paddingTop = null
				// #ifdef H5
				top = ''
				// #endif
				//如果是APP  paddingTop = 104rpx
				// #ifdef APP
				top = 16 + 88 + 'rpx'
				// #endif
				return top
			},
		},
		components: {
            notice,
			maskM,
			showToast,
			waterfall,
			skus
		},

		mounted() { 
           
             
            // this.triggerMounted()
		},  
		beforeDestroy() { 
			//标记为页面已隐藏
			this.onHide = true
			//清除定时器
			clearInterval(this.gesTime)
			this.gesTime = ''
			this.haveAllUserInfo = ''
			//偶尔还是会内存泄漏 3秒后再清理一次
			setTimeout(() => {
				clearInterval(this.gesTime)
				this.gesTime = ''
			}, 3000)
            uni.removeStorageSync('isfx');

		}, 
		onPageScroll(e) { 
			//是否隐藏 header组件中的标题模块
			if (e.scrollTop > 0) {
				this.headBg = ''
				this.isAnd = false
			} else {
				this.headBg = 'url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/auction/grbj.png) 750rpx 100%;'
				// #ifdef H5
				this.headBg = '#ffffff'
				// #endif
				this.isAnd = true
			}
		},
		//上拉加载
		onReachBottom() {
			if (this.loadingType != 0) {
				return
			}
			//显示加载中
			this.loadingType = 1;
			//页面++
			this.page++;

			this.getList()
		},
		methods: {
            orderListByType(item){
                this.isLogin(() => {
                    if(item.type =='del'){ 
                        // 售后列表
                        uni.navigateTo({
                            url: '/pagesC/afterSale/afterSale'
                        });
                    }else{
                        this.status(item.type);
                        uni.navigateTo({
                            url: '/pagesB/order/myOrder?status=' + item.type
                        });
                    } 
                })
            },
            
            triggerMounted(){ 
              try{ 
                    this.syncMallTitle()
                    clearInterval(this.gesTime)
                    
                    const pageInfo = getDIYPageInfoById({
                        key: "homeItem17569763719113"
                    }) 
                    if (pageInfo) {
                        this.getDiyInfo(pageInfo)
                    } else {
                        this.getPlus()
                        
                          this.getList()
                    }
                    this.gesTime = '' 
                    this.page = 1 
                               
                    this.aliUserLoginApp();
                    this.getHasGrades(); 
                    this.getCart();
                    this.$store.state.frompage = 'my';
                    
                    this.$nextTick(()=>{
                         const index = JSON.parse(uni.getStorageSync('tabbar')).findIndex(v=>(v.url || v.pagePath) == 'pages/tabBar/my')
                         uni.setStorageSync('tabbarIndex',index) 
                    })
                    this.lang_text = uni.getStorageSync('language')
                    const tabber_info = uni.getStorageSync('tabbar_info') 
                    if(tabber_info){
                        
                        let obj  = {}
                        if(typeof tabber_info == 'string'){
                            obj = JSON.parse(tabber_info)
                        }else{
                            obj =  tabber_info
                        }
                        if(obj.backType == 0){
                            // 颜色设置
                            this.grbj = { backgroundColor: obj.colorTwo }
                        }else {
                            // 图片设置
                              this.grbj =  {
                                                backgroundImage: `url(${obj.imgurl})`,   
                                                backgroundRepeat: 'no-repeat',  
                                                backgroundSize: '100% 100%'  
                                            } 
                        } 
                    }
                    let userCurrency = uni.getStorageSync("currency");
                    
                    if (userCurrency) {
                        this.currency_symbol = userCurrency.currency_symbol;
                        this.exchange_rate = userCurrency.exchange_rate;
                    }
                    
                    // 用于 订单是否走缓存 （JAVA后端使用）
                    if (uni.getStorageSync('skipType')) {
                        uni.removeStorageSync('skipType')
                    }
                     
                    uni.hideTabBar()
                     
                    uni.removeStorageSync('payTarget')
                    //标记为页面已显示
                    this.onHide = false
                    //开启轮循
                    const access_id = this.$store.state.access_id
                    
                   
                        if (this.gesTime == '' && (access_id && access_id.length > 0)) {
                            this.gesTime = setInterval(() => {
                                this.getuserOnlineMessageNotRead()
                            }, 3000)
                        }          
                    this.lang_text = uni.getStorageSync('language')
                    this.loadingType = 0
                              
                    // uni.showToast({
                    //     title:'开始执行',
                    //     icon:'none'
                    // })
                   
                    this._axios();
                    // #ifndef MP
                    this.LaiKeTuiCommon.getUrlFirst(this._axios);
                    // #endif 
                    // #ifdef MP-WEIXIN
                    // LaiketuiWeixinAuth.laiketui_mp_weixin_checkauth(this, this._axios);
                    // #endif
                    // #ifdef MP-ALIPAY
                    this.LaiketuiAliAuth.laiketui_mp_alipay_check(null, this, this._axios);
                    // #endif
                    // #ifdef MP-TOUTIAO
                    this.LaiketuiTTAuth.laiketui_mp_tt_check(null, this, this._axios);
                    // #endif
                    // #ifdef MP-BAIDU
                    this.LaiketuiBDAuth.laiketui_mp_baidu_check(null, this, this._axios);
                    // #endif
                    setTimeout(() => {
                        this.$refs.mynotice.getData()
                    }, 0)
                    
                }catch(e){
                   uni.showToast({
                       title: e,
                       icon: 'none'
                   })
                }
            },
            aliUserLoginApp(){
                // #ifdef H5 
                let ua = navigator.userAgent.toLowerCase();
                if (ua.match(/MicroMessenger/i) == "micromessenger") {
                    try{ 
                        let info = window.location.href
                        let infos = info.split('code=')[1]
                        if(infos){ 
                            let code = infos.split('&')[0]
                            let state = infos.split('state=')[1]
                            state = state.split('#')[0]
                            if (uni.getStorageSync('pay_code') == code) {
                                code = ""
                            }
                            if (code) {
                                if (state === 'true') {
                                    //提现零钱绑定微信
                                    this.wxTixian(code)
                                } else {
                                    //微信快捷登录
                                    this.wxAuth(code)
                                }
                            }
                        }
                    }catch(e){
                        console.error(e)
                    }
                } else {
                    let info = window.location.href.split('auth_code=')[1]
                    if (info) {
                        let auth_code = info.split('&')[0]
                        if (auth_code) {
                            var data = {
                                api: 'app.login.aliUserLoginApp',
                                store_type: 7,
                                authCode: auth_code,
                            };
                            this.$req.post({
                                data
                            }).then(res => {
                                uni.showToast({
                                    title: res.message,
                                    icon: 'none'
                                })
                                uni.setStorageSync("login_key", 0)
                                this.$store.state.access_id = []
                                uni.setStorageSync('isHomeShow', 1)
                                this.set_access_id(res.data.userInfo.access_id)
                                uni.setStorage({
                                    key: 'access_id',
                                    data: res.data.userInfo.access_id,
                                    success: function() {
      
                                    }
                                })
                                uni.setStorage({
                                    key: 'user_id',
                                    data: res.data.userInfo.user_id
                                })
                                this._axios()
                                uni.redirectTo({
                                    url: '/pages/shell/shell?pageType=my'
                                })
                            })
                        }
                    }
      
                }
                // #endif  
            },
            // 获取diy 配置 
            getDiyInfo(pageInfo){
                const key = Object.keys(pageInfo)[0] 
                const {
                	pageConfig,
                	orderCondfig,
                	functionConfig,
                	iconConfig
                } = pageInfo[key]
                this.diyObj = pageInfo[key]
                this.pageConfig = pageInfo[key].pageConfig
                
                // 订单配置
                this.orderCondfig = orderCondfig
                
                const {backgColor1,backgColor2,backgType,boxRoundedVal} = orderCondfig
                
                this.background = `
                    border-radius: ${boxRoundedVal}px;
                    background: ${backgType === 0 
                        ? backgColor2 
                        : `linear-gradient(to right, ${backgColor1}, ${backgColor2})`
                    };
                `;
                
                // 功能中心
                this.functionConfig = functionConfig
                // diy 金刚区配置
                if(this.functionConfig && Object.keys(this.functionConfig).length){ 
                    
                    const { boxRoundedVal,backgType,backgColor2,backgColor1,list } = functionConfig
                    
                    this.background1 = `
                         border-radius: ${boxRoundedVal}px;
                         background: ${backgType === 0 
                             ? backgColor2 
                             : `linear-gradient(to right, ${backgColor1}, ${backgColor2})`
                         };
                     `;
                     
                    const typeList = list.map(v => v.type)
                    
                    this.plugin = {}
                    
                    typeList.forEach(v => {
                        this.$set(this.plugin, v, 1)
                    })
                    
                    this.gn_num = typeList.length 
                }
                // 资产信息
                this.iconConfig = iconConfig 
            },
            _navigateTo3(type){ 
              const filtUlr = funCenterByType(type)
              if(filtUlr){
                  // 客服入口处理
                  if(type === 'myService'){
                      filtUlr+='?mch_id=' + this.mch_id
                  }
                  
                  uni.navigateTo({
                  	url: filtUlr
                  });
              }
            }, 
			//客服是否存在未读消息
			getuserOnlineMessageNotRead() {
                // tsog 测试场景使用 为了好找到接口 没有实际业务场景
                if(uni.getStorageInfoSync('tsog')){
                    return
                }
				if (!this.userMessage) {
				}
                
                if(uni.getStorageSync('stop')){
                     clearInterval(this.gesTime)
                     this.gesTime = ''
                     return
                }
                
				let data = {
					api: "app.message.messageNotReade",
				};
				this.userMessage = false
				this.$req.post({
					data
				}).then((res) => {
					this.userMessage = true
					if (res.code == 200 && !this.onHide) {
						this.userOnlineMessageNotRead = res.data.userOnlineMessageNotRead
					} else {
						clearInterval(this.gesTime)
						this.gesTime = ''
					}
				})
			},
			//跳转：商品详情
			_goods(id) {
				uni.navigateTo({
					url: "/pagesC/goods/goodsDetailed?toback=true&pro_id=" + id,
				});
			},
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
							},
						} = res;

						this.$refs.attrModal.imgurl = pro.img_arr[0];
						this.$refs.attrModal.num = pro.num;
						this.$refs.attrModal.price = qj_price;
						this.$refs.attrModal.skuBeanList = attribute_list;
						this.$refs.attrModal.initData();
						this.$refs.attrModal._mask_display();
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

			handleErrorImg() {
				this.user.headimgurl = this.defaultPicture
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
						//计算plugin中符合条件的有几个显示
						let gn_nums = 0
						// #ifdef MP
						gn_nums++
						// #endif
						for (let x in this.shaco_plugin) {
							if (x == 'go_group' || x == 'seconds' || x == 'auction' || x == 'integral' || x ==
								'distribution' || x == 'presell' || x == 'coupon' || x == 'diy' || x == 'flashsale' ||
								x == 'living' || x == 'member' || x == 'seconds' || x == 'sigin' || x == 'wallet') {
								if (this.shaco_plugin[x] == 1) {
									gn_nums++
								}
							}
						}
						this.gn_num = gn_nums //如果小于5  功能中心排版要用另外一个显示
					})
			},
			async wxAuth(code) {
				let me = this
				let data = {
					api: 'app.login.appletsWx',
					code: code,
					store_type: 2
				}
				this.$req.post({
					data
				}).then(res => {
					uni.showToast({
						title: res.message,
						icon: 'none'
					})
					uni.setStorageSync("login_key", 0)
					me.$store.state.access_id = []
					uni.setStorageSync('isHomeShow', 1)
					me.set_access_id(res.data.access_id)
					uni.setStorage({
						key: 'access_id',
						data: res.data.access_id,
						success: function() {

						}
					})
					uni.setStorage({
						key: 'user_id',
						data: res.data.user.user_id
					})
					me._axios()
					me.navTo('/pages/shell/shell?pageType=my')
				})
			},
			//提现微信零钱绑定微信
			wxTixian(code) {
				let data = {
					api: 'app.user.bindWechat',
					code: code
				}
				this.$req.post({
					data
				}).then(res => {
					uni.showToast({
						title: res.message,
						icon: 'none'
					})
				})
			},
			// 关闭设置密码弹窗
			closePsw() { 
				this.is_showToast = 0
			},
			// 去设置登录密码页面
			toSetPsw() {
				this.is_showToast = 0
				if (this.showToastType == 1) {
					uni.navigateTo({
						url: '/pagesC/my/myInfo?haveAllUserInfo=true'
					})
				} else if (this.showToastType == 2) {
					uni.navigateTo({
						url: '/pagesB/setUp/loginPassword?havePass=false'
					})
				} else {
					uni.navigateTo({
						url: '/pagesB/setUp/changePhone?havePass=true&isNoPhone=true'
					})
				}
			},
			onHandle(value) {
				if (value === '取消') {
					this.display = false;
				} else {
					this.display = false;
					this.navTo('/pagesB/setUp/changePhone?withdrawalNoPhone=1')
				}
			},
			 
			vipClick() {
				this.isLogin(() => {
					uni.navigateTo({
						url: '/pagesB/userVip/memberCenter?id=1'
					})
				});
			},
			getHasGrades() {
				let data = { 
					api: "plugin.member.AppMember.MemberCenter"
				};
				this.$req.post({
					data
				}).then(res => {
					if (res.code == 200) {
						this.memberEquity = res.data.memberEquity
					}
				});
			},
			shutClick() {
				this.vippupo = false
				if (!this.xzbtn) {
					return
				} else {
					var data = {
						api: "plugin.member.AppMember.CloseFrame"
					}
					this.$req.post({
						data
					}).then(res => {
					})
				}
			},
			xzbtnClick() {
				this.xzbtn = !this.xzbtn
			},
            //为您推荐商品列表
			getList() {
				var data = {
					api: 'app.user.myRecommendation',
					page: this.page
				}

				this.$req.post({
					data
				}).then(res => {
                    try{                        
                        if(res.code == 200){                        
                            res.data.list.forEach((item, index) => {
                                //可能有相同路径的图片，会无法加载@load事件；拼接随机字符串，可以解决这个问题
                                item.imgurl = item.imgurl + '?a=' + Math.floor(Math.random() * 1000000);
                                item.volume = item.payPeople;
                            });
                            if(this.page == 1){
                                this.class_pro = res.data.list
                            }else{ 
                                this.class_pro.push(...res.data.list)
                            }
                            //>=10条可以继续上拉
                            if (res.data.list.length < 10) {
                                this.loadingType = 2;
                            } else {
                                this.loadingType = 0;
                            }
                        }
                    }catch(e){
                        console.error(e)
                    }
				})
			},
			getCart() {
				var data = {
					api: 'app.cart.index',
					cart_id: '',
					page: 1
				}

				this.$req.post({
					data
				}).then(res => {
					let {
						code,
						data: {
							data,
							list,
							mch_list,
							login_status,
							grade
						},
						message
					} = res

					if (code == 200) {
                        // 缓存 tabbar 购物车数量
						this.setCartNum(data.length)
					}
				})
			},
			//微信授权
			bindGetUserInfo(res) {
				 LaiketuiWeixinAuth.laiketui_mp_weixin_auth(res, this)
			},

			//头条授权
			ttAuth(res) {
				this.LaiketuiTTAuth.laiketui_mp_tt_userInfo(this)
			},

			//百度授权
			bdAuth(res) {
				this.LaiketuiBDAuth.laiketui_mp_baidu_userInfo(this)
			},

			//支付宝授权
			onGetAuthorize(res) {
				this.LaiketuiAliAuth.laiketui_mp_alipay_userInfo(res, this)
			},
			toKeFu() {
				// #ifndef MP-WEIXIN ||  MP-TOUTIAO || MP-BAIDU
				this._navigateTo1('/pagesB/message/service')
				// #endif
			},
			_order(status) {
                // status ： '' 全部 ;  1 待付款 ; 2 待发货; 3 待收货  ; 4  待评价
				this.isLogin(() => {
					this.status(status);
					uni.navigateTo({
						url: '/pagesB/order/myOrder?status=' + status
					});
				})
			},
 
			_navigateTo(url) {
				this.isLogin(() => {
					uni.navigateTo({
						url: url
					});
				})
			}, 
            _navigateto(url) {
                uni.navigateTo({
                    url: url
                });
            },
			_navigateTo1(url) { 
                this.isLogin(() => {
                    uni.navigateTo({
                        url: url
                    });
                })
			},
           
			_axios() {
               
				//每次点击 都得初始化上一次商品瀑布流的数据
				this.isDataKO = true;
				//500毫秒，取消初始化
				setTimeout(() => {
					this.isDataKO = false;
				}, 100);
				var data = {
					api: 'app.user.index',
                    // api:'app.user.index',
					mobile: uni.getStorageSync('phoneNumbers') ? uni.getStorageSync('phoneNumbers') : ''
				};
				this.$req.post({
					data
				}).then(res => {
                   
                    try{ 
                        if (res.data && res.data.data && res.data.data.merchant_id) {
                            uni.setStorageSync('merchant_id', {
                                name: '微信商户号',
                                value: res.data.data.merchant_id
                            })
                        }
                        if (res.data && res.data.data && res.data.data.storeMchId) {
                            this.mch_id = res.data.data.storeMchId
                        }
                        if (res.data && res.data.plugin) {
                            this.plugin = res.data.plugin
                        }
                        if (res.data && res.data.defaultHeaderImgUrl) {
                            this.defaultHeaderImgUrl = res.data.defaultHeaderImgUrl
                        }
                        if (res.data && res.data.defaultUserName) {
                            this.defaultUserName = res.data.defaultUserName
                        }
                    }catch(e){
                        uni.showToast({
                            title:`${e}`,
                            icon:'none'
                        })
                    }
                    
					// 缓存账号地区 用于 修改支付密码 操作

					try { 
						if (res.data.data.user.cpc) {
							uni.setStorageSync('diqu', JSON.stringify({
								code: res.data.data.user.code,
								code2: res.data.data.user.cpc,
								num3: res.data.data.user.country_num,
							}))
						}
						// 判断是否可以 修改支付密码
						uni.setStorageSync('havaMobile', JSON.stringify(res.data.data.user.havaMobile))
						//是否是初始状态 fasle:初始状态  true:非初始状态
						uni.setStorageSync('havaPayPwd', res.data.data.user.havaPayPwd)
					} catch (e) {
						uni.removeStorageSync('diqu')
						uni.setStorageSync('havaMobile', JSON.stringify(false))
						uni.removeStorageSync('havaPayPwd')
                        // uni.showToast({
                        //     title:`地区异常${e}`,
                        //     icon:'none'
                        // })
					}
                    try{ 
                        if (res.data && res.data.data == undefined) {
                            this.access_id1 = false
                           
                            return
                        } else {
                            if (res.code == 200) {
                                let {
                                    data: {
                                        data: { 
                                            grade,
                                            user,
                                            grade_end,
                                            xxnum,

                                            dfh_num,
                                            dfk_num,
                                            dpj_num,
                                            dsh_num,
                                            th,
                                            synchronize_type,
                                            remind,
                                            rate,
                                            collection_num
                                        },
                                        plugin
                                    },

                                } = res;
                                this.vippupo = res.data.data.memberInfo.remind
                                this.fsNum = res.data.data.fs_num
                                this.fxNum = res.data.data.fx_num
                                this.ptNum = res.data.data.pt_num
                                this.grade = res.data.data.memberInfo.grade
                                this.gradeEndTime = res.data.data.memberInfo.gradeEndTime 
                                user.grade = grade
                                user.grade_end = grade_end
                                user.xxnum = xxnum
                                user.collection_num = collection_num
                                this.user = user; 
                                this.user.headimgurl = this.user.headimgurl + '?a=' + Math.floor(Math.random() *
                                    1000000);
                                  
                                this.sign = plugin.sign;
                                if (plugin.sign == undefined) {
                                    this.sign = 0
                                }
                                this.mch = plugin.mch;
                                this.synchronize_type = synchronize_type

                                this.orderNum = {
                                    th_num: th,
                                    dfk_num: dfk_num,
                                    dfh_num: dfh_num,
                                    dsh_num: dsh_num,
                                    dpj_num: dpj_num
                                }

                                this.$store.state.user_phone = user.mobile;
                                uni.setStorage({
                                    key: 'user_phone',
                                    data: user.mobile
                                }); 
                                this.haveAllUserInfo = !user.checkInfo
                                this.access_id1 = true;
                                this.rate = rate;

                                 
                                uni.showTabBar();

                                this.plugin = plugin
                     
                                this.load = true
                                let that = this
                                var pphone = uni.getStorageSync('phoneNumbers')
                                if (synchronize_type == 1) {
                                    var data = {
                                        api: 'app.user.synchronizeAccount',
                                        mobile: pphone,
                                    };
                                    // #ifdef MP-WEIXIN
                                    data.source = 1
                                    // #endif

                                    // #ifdef MP-ALIPAY
                                    data.source = 2
                                    // #endif
                                    this.$req.post({
                                        data
                                    }).then(res => {
                                        if (res.code == 200) {
                                            uni.showModal({
                                                content: '已同步' + pphone + '的 \n 账号信息',
                                                showCancel: false,
                                                confirmText: this.language.order.myorder.confirm,
                                                cancelText: this.language.order.myorder.cancel,
                                                success(res) {
                                                    if (res.confirm) {
                                                        that._axios()
                                                    }
                                                }
                                            })
                                        }
                                    })

                                }
                                if (pphone) {
                                    setTimeout(() => {
                                        uni.removeStorage({
                                            key: 'phoneNumbers'
                                        })
                                    }, 10000)
                                }
 
                            } else {
                                this.access_id1 = false;
                                this.load = true
                                if (res.data && res.data.plugin) {
                                    this.plugin = res.data.plugin
                                }
                                
                            }
                        }
                    }catch(e){
                        uni.showToast({
                            title:`登录:${e}`,
                            icon:'none'
                        })
                    }
				})
			}, 
			...mapMutations({
				set_access_id: 'SET_ACCESS_ID',
				status: 'SET_STATUS',
				setCartNum: 'SET_CART_NUM'
			})
		}
	};
</script>
<style>
	page {
		background-color: #f4f5f6;
	}
</style>
<style scoped lang="less">
	@import url("@/laike.less");
	@import url('@/static/css/tabBar/my.less');

	.my-kefu-red {
		width: 16rpx;
		height: 16rpx;
		background-color: red;
		border-radius: 50%;
		position: absolute;
		top: 16rpx;
		margin-left: 40rpx;
	}

	.relative-box-title-txtl {
		font-size: 28rpx;

		font-weight: 500;
		color: #333333;
		line-height: 40rpx;
	}

	.relative-box-box {
		width: 334rpx;
		height: 128rpx;
		display: flex;
		justify-content: space-between;
		background-color: #ffffff;
		border-radius: 16rpx;
	}

	.relative-box {
		width: 686rpx;
		//height: 128rpx;
		display: flex;
		//padding-top: 8rpx;
		justify-content: space-between;
		//background-color: #fff;
		border-bottom-right-radius: 25rpx;
		border-bottom-left-radius: 25rpx;
	}

	.relativell {
		width: 100%;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		margin-bottom: 24rpx;
		// background-color: #fff;
	}

	.relative-imgll {
		width: 690rpx;
		height: 96rpx;
		margin-bottom: 16rpx;
	}

	.viptitle {
		color: #7B6A41;
		margin-top: 30rpx;
		font-size: 28rpx;
	}

	.relative-box-title {
		font-size: 28rpx;
		margin-left: 24rpx;
		color: #333333;
		// line-height: 128rpx;
		display: flex;
		flex-direction: column;
		font-weight: 500;
		justify-content: center;
	}

	.relative-box-title-txt {
		font-size: 24rpx;
		color: #666666;
		margin-top: 8rpx;
	}

	.relative-box-img {
		display: flex;
		align-items: center;
		margin-right: 24rpx;
	}

	.relative-box-img-img {
		width: 56rpx;
		height: 56rpx;
	}
    .Functioncenter-button{
        margin-bottom: 120rpx;
    }
	.Functioncenter {
		display: flex;
		flex-wrap: wrap;

		align-items: center;
		padding: 24rpx 20rpx 16rpx 20rpx;
	}

	.Functioncenter-img-img {
		width: 56rpx;
		height: 56rpx;
	}

	.Functioncenter-title {
		height: 32rpx;
		font-size: 24rpx;
		color: #333333;
	}

	.Functioncenter-box {
		width: 130rpx;
		height: 128rpx;
		flex-direction: column;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.rights {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 306rpx;
		display: flex;
		width: 85%;
		margin-left: 7%;

		.scrollx {
			white-space: nowrap
		}

		.item {
			display: inline-block;
			width: 140rpx;
			padding: 12rpx 8rpx;
			text-align: center;
			flex-grow: 1;
			color: #E4C270;

			.itemimg {
				height: 48rpx;
				margin-bottom: 8rpx;

				img {
					width: 48rpx;
					height: 48rpx;
				}
			}

			.title {
				font-size: 24rpx;
				margin-bottom: 8rpx;
			}

			.en {
				font-size: 20rpx;
				opacity: .6;
			}
		}
	}

	.Functioncenter-img {
		position: relative;
	}
   

	@media screen and (min-width: 600px) {
		.content {
			width: auto !important;
		}
	}
    .option-two{
        .new_function_box:last-child{
            margin-bottom: 80rpx;
        }
    }
</style>
