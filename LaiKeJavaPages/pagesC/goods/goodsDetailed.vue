<template>
	<div class="order_ii goodsDetail">
		<view class="onReachBottomBg"></view>
		<lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus" v-on:cancle="cancle">
		</lktauthorize>
		<div ref="box" id="boxs" class="relative" @touchmove="setHead">
			<template v-if="!bargain">
				<heads 
					:title="title" 
					:titleColor="titleColor" 
					:bgImg="headBg" 
					:ishead_w="ishead_w"
					:tabs="headerTabs"
					:activeTabIndex="activeHeaderTab"
					:centerTitle="true"
					:centerTabs="shouldCenterHeaderTabs"
					@tab-click="onHeaderTabClick"
				></heads>
			</template>
			<template v-else>
				<image :src="goodsDetailed_fh" class="fh_btn" @click="fh_click"></image>
				<div class="gd_header" :style="{ top: halfWidth }">
				</div>
			</template>

			<div class="skeleton">
				<!-- 有数据 -->
				<div v-if="loadFlag">
					<!--  轮播图   -->
					<div class="relative">
						<div class="kanjiaNull" v-if="(bargain && leftTime <= 0) || brStatus == -1"><img :src="list_end"
								mode="" /></div>
						<swiper class="swiper" @change="swiperChange">
							<swiper-item v-for="(img,index) in pro.img_arr" :key="img.id">
								<div style="width: 100%;height: 100%;position: relative;"
									v-if="video != '' && index == 0">
									<!-- #ifdef H5 -->
									<video 
                                        id="myVideo" 
										class="swipe"
                                        :poster="video+'?x-oss-process=video/snapshot,t_0,f_jpg'"
                                        :src="img" 
                                        @click="diyVideo()"
                                        @ended="diyVideo()"
                                        controls
                                    >
									</video>   
									<img class="bofang_bt" @click="diyVideo()" v-show="!videoIsPlay"
										:src="bofang" />
									<!-- #endif -->
									<!-- #ifdef MP -->
									<video :controls="false" :show-play-btn="false" :show-fullscreen-btn="false"
										:show-center-play-btn="false" class="swipe" :src="video">
										<div>
											<img class="bofang_bt" @click="changeVideoStatus()" v-show="!videoIsPlay"
												:src="bofang" />
										</div>
									</video>
									<!-- #endif -->
									<!-- #ifdef APP-PLUS -->
									<view class="swipe" style="background: black;" v-html="type1html"></view>
									<img class="bofang_bt" @click="changeVideoStatus()" v-show="!videoIsPlay"
										:src="bofang" />
									<!-- #endif -->
								</div>
								<img v-else class="swipe" :src="img" @error="handleErrorImg(index)" />
							</swiper-item>
						</swiper>
						<view class="swiper_dots_new">
							<view>{{swiperIndex}}</view>
							<view>{{ pro.img_arr && pro.img_arr.length }}</view>
						</view>
					</div>
					<template v-if="pro.status == 3">
						<view class="new_pre_title">
							{{language.goods.goodsDet.shelf}}~
						</view>
					</template>
					<template v-else-if="pro.num == 0">
						<view class="new_pre_title">
							{{language.goods.goodsDet.soldOut}}~
						</view>
					</template>
					<template v-else-if="shop_list.is_open != 1">
						<view class="new_pre_title">
							{{language.goods.goodsDet.close}}~
						</view>
					</template>
					<template v-else-if="pro.recycle == 1">
						<view class="new_pre_title">
							{{language.goods.goodsDet.close}}~
						</view>
					</template>
					<template v-else-if="pro.status == 3">
						<view class="new_pre_title">
							{{language.goods.goodsDet.close}}~
						</view>
					</template>

					<!-- 非砍价商品价格和名称 -->
					<div class="goods_name" v-if="!bargain">
						<template v-if="active == 1 && !isDistribution">
							<p class="goods_price" v-if="isVipUser">
								<span class="span">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
								{{ LaiKeTuiCommon.formatPrice(Number(displayPrice))}}
								<span v-if="showVipYprice"
									style="margin-left: 20rpx;color: #9A9A9A; text-decoration: line-through;font-size: 24rpx;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(Number(displayYprice))}}</span>
							</p>
							<p class="goods_price"
								style="display: flex;justify-content: space-between;align-items: center;" v-else>
								<view> <span class="spanspan">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
									{{ LaiKeTuiCommon.formatPrice(Number(displayPrice)) }}
								</view>
								<view class="fx_btn" v-if='!isLive'>
									<button @tap="showShareMask(0)" class="fx_btn_box"
										v-if="!isDistribution || ( isDistribution &&  pro.canbuy)" id="copyy copyy1"
										:data-clipboard-text="share.shareHref">
										<img :src="weixin_share" style="width: 32rpx;height: 32rpx;" />
<!--										<view>{{language.goods.goodsDet.fx}}</view>-->
									</button>
								</view>
							</p>
						</template>
						<template v-else>
							<view style="display: flex;justify-content: space-between;">
								<p class="goods_price">
									<span class="span"
										style="font-size: 28rpx;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
									{{LaiKeTuiCommon.formatPrice(Number(displayPrice)) }}
									<span v-if="showVipYprice"
										style="margin-left: 20rpx;color: #9A9A9A; text-decoration: line-through;font-size: 24rpx;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(Number(displayYprice))}}</span>
								</p>
								<!-- 分享 -->
								<!-- <p class="goods_price"
                                    style="display: flex;justify-content: space-between;align-items: center;">
                                    <view class="fx_btn">
                                        <button @tap="showShareMask(0)" class="fx_btn_box"
                                            v-if="!isDistribution || ( isDistribution &&  pro.canbuy)" id="copyy copyy1"
                                            :data-clipboard-text="share.shareHref">
                                            <img :src="weixin_share" style="width: 32rpx;height: 32rpx;" />
                                            <view>{{language.goods.goodsDet.fx}}</view>
                                        </button>
                                    </view>
                                </p> -->
							</view>
						</template>
						<p class="goods_proName">{{ pro.name }}</p>  
						<div class="volume">
							<span>{{language.goods.goodsDet.sales}}：{{ pro.volume }}</span>
							<span
								v-if="commodity_type == 0">{{language.goods.goodsDet.freight}}：{{LaiketuiCommon.DEFAULT_STORE_SYMBOL}}
								{{ LaiketuiCommon.formatPrice(pro.yunfei) }}</span>
						</div>
					</div>
					<!-- 砍价商品价格和名称 -->
					<div class="goods_name goods_bargain_name" v-else>
						<p class="goods_price">
							{{ cs_num }}{{language.goods.goodsDet.Bargaining_success}}
							<span class="cs_yprice">{{ cs_yprice }}</span>
							<span class="cs_price">{{ cs_price }}</span>
						</p>
						<p>{{ pro.name }}</p>
					</div>
					<div class="hr"
						v-if="isDistribution == false && !bargain && active != 6 && coupon_status == true && coupon_str.length>0">
					</div>
					<div class="coupon" @tap="getCoupon()"
						v-if="isDistribution == false && !bargain && active != 6 && coupon_status == true && coupon_str.length>0">
						<span>{{language.goods.goodsDet.Collect_coupons}}</span>
						<div>
							<div v-for="(item, index) in coupon_str" :key="index" class="coupon_data"
								:style="'background-image:url(' + coupon_img + ')'">{{ item }}</div>
						</div>
						<img class="arrow" :src="you_img" />
					</div>
					<div class="hr" v-if="write_off_settings == 1"></div>
					<!-- 核销门店 -->
					<template v-if="write_off_settings == 1">
						<div class="user hxShop" @tap="_getShopHX">
							<p>适用核销门店</p>
							<view>
								<span>{{mchStoreListNum}}家</span>
								<image :src="baga"></image>
							</view>
						</div>
					</template>
					<div v-if="!bargain" class="hr"></div>
					<div class="user gd_user" style="border-radius: none;" v-if="commentsTotal && !bargain"
						@tap="_evaluate(pro_id)"
						:style='!commentsTotal && !bargain?"border-radius: 24rpx;":"border-top-left-radius: 24rpx;border-top-right-radius: 24rpx;"'>
						<p>{{language.goods.goodsDet.User_evaluation}}（{{ commentsTotal }}）</p>
						<img class="arrow" :src="baga" />
					</div>
					<div class="comments" v-if="commentsTotal && !bargain">
						<div class="allCenter">
							<img class="img"
								:src="comments[comments.length-1].user_name !== '匿名' ? comments[comments.length-1].headimgurl || niming : niming" />
							<span class="span">{{ comments[comments.length-1].user_name }}</span>
						</div>
						<div class="time">{{ comments[comments.length-1].add_time }}</div>
						<div class="disc">{{ comments[comments.length-1].content }}</div>
					</div>
					<div class="user" v-if="!commentsTotal && !bargain"
						:style='!commentsTotal && !bargain?"border-radius: 24rpx;":"border-top-left-radius: 24rpx;border-top-right-radius: 24rpx;"'>
						<p>{{language.goods.goodsDet.No_evaluation}}</p>
					</div>
					<!-- 砍价页面显示 -->
					<div v-if="bargain">
						<div v-if="comments.length > 0" class="user gd_user" @tap="_evaluate(pro_id)">
							<p>{{language.goods.goodsDet.sheets}}（{{ comments.length }}）</p>
							<img class="arrow" :src="you_img" />
						</div>
						<div v-else class="user gd_user">
							<p>{{language.goods.goodsDet.sheets}}（0）</p>
							<img class="arrow" :src="you_img" />
						</div>
						<template v-if="comments.length">
							<div class="comments" v-for="(item, index) in comments" :key="index">
								<div class="allCenter">
									<img class="img" :src="item.headimgurl ? item.headimgurl   : niming" />
									<span class="span">{{ item.user_name }}</span>
								</div>
								<div class="time">{{ item.add_time }}</div>
								<div class="disc">{{ item.content }}</div>
							</div>
						</template>
						<div class="user" v-if="comments.length <= 0">
							<p class="font_30">{{language.goods.goodsDet.No_evaluation}}</p>
						</div>
					</div>
					<div class="hr"></div>
					<div class="video_box" v-if="proVideo != ''" style="position: relative;">
						<!-- #ifdef H5 -->
						<video id="myVideo2" :poster="proVideo+'?x-oss-process=video/snapshot,t_0,f_jpg'"
							class="xq_video" :src="proVideo" controls>
						</video>
						<img class="bofang_bt" @click="changeVideoStatus2()" v-show="!videoIsPlay2" :src="bofang" />
						<!-- #endif -->

						<!-- #ifdef MP -->
						<video :controls="false" :show-play-btn="false" :show-fullscreen-btn="false"
							:show-center-play-btn="false" class="xq_video" :src="proVideo">
							<div>
								<img class="bofang_bt" @click="changeVideoStatus2()" v-show="!videoIsPlay2"
									:src="bofang" />
							</div>
						</video>
						<!-- #endif -->
						<!-- #ifdef APP-PLUS -->
						<view class="xq_video" style="background: black;" v-html="type1html2"></view>
						<img class="bofang_bt" @click="changeVideoStatus2()" v-show="!videoIsPlay2" :src="bofang" />
						<!-- #endif -->
					</div>
					<div class="hr" v-if="proVideo != ''"></div>
					<!-- 商品详情切换栏目 -->
					<div class="seeTitle1">
						<scroll-view-nav :is_ScrollView="IntroList" @_changeNav="_changeNav"></scroll-view-nav>
					</div>
					<div class="hrs">
						<view class="hrs_box"></view>
					</div>
					<!--  商品详情    -->
					<div class="content_d safe-area-inset-bottom" id="content_d" v-if="goods_x">
						<view class="margin_boxs">
							<rich-text class="richtext" :nodes="IntroList[topTabBar].content"></rich-text>
						</view>
					</div>
					<view class="bozztm">~{{language.goods.goodsDet.wsydxd}}~</view>
					<!--  规格参数   -->
					<ul class="goods_spec" v-if="goods_g">
						<li>
							<div class="g_div">
								<div>{{language.goods.goodsDet.Trade_name}}：</div>
							</div>
							<p>{{ pro.name }}</p>
						</li>
						<li>
							<div class="g_div">
								<div>{{language.goods.goodsDet.brand}}：</div>
							</div>
							<p>{{ pro.brand_name }} * {{ pro.cat_name }}</p>
						</li>
						<li>
							<div class="g_div">
								<div>{{language.goods.goodsDet.Commodity_number}}：</div>
							</div>
							<p>{{ pro_id }}</p>
						</li>
						<li>
							<div class="g_div">
								<div>{{language.goods.goodsDet.classification}}：</div>
							</div>
							<p>{{ pro.cat_name }}</p>
						</li>
						<li>
							<div class="g_div">
								<div>{{language.goods.goodsDet.After_sale}}：</div>
							</div>
							<p>{{language.goods.goodsDet.qianshou}}</p>
						</li>
					</ul>

					<template v-if="write_off_settings != 1">
						<div class="goods_foot" v-if="!bargain">
							<div class="goods_bottom safe-area-inset-bottom"
								:class="{ goods_bottoms: pro.status == '3' }">
								<!-- 店铺 -->
								<div class="goods_one center" @tap="_goStore(shop_list.shop_id)">
									<img :src="goodsDetailed_dp" />
									<p>{{language.integral.integral_detail.dp}}</p>
								</div>
								<!-- 客服 -->
								<div class="goods_one center" @tap="kf(shop_list.shop_id)">
									<img :src="goodsDetailed_kf" />
									<p>{{language.goods.goodsDet.customer_service}}</p>
								</div>

								<!-- 分享(直播调整) -->
								<div class="goods_one center" @tap="showShareMask(0)" v-if='isLive'>
									<img :src="weixin_share" />
<!--									<p>分享</p>-->
								</div>
								<!-- 收藏 -->
								<div class="goods_one center" @tap="_collection" v-else>
									<img :src="collection ? goodsDetailed_ysc : goodsDetailed_sc" />
									<p v-if="collection">{{language.goods.goodsDet.Collected}}</p>
									<p v-else>{{language.goods.goodsDet.Collection}}</p>
								</div>
								<!-- 购物车 -->
								<template v-if='!isLive'>
									<!-- #ifdef MP-WEIXIN -->
									<div class="goods_one center" @tap="Back_Tabbar">
										<img :src="goodsDetailed_gwc" />
										<p>{{language.goods.goodsDet.shopping_cart}}</p>
										<view class="order_bottom_icon_s">
											<view v-if="cart_nums>0" class="order_bottom_icon">{{cart_nums}}</view>
										</view>
									</div>
									<!-- #endif -->
									<!-- #ifndef MP-WEIXIN || MP-TOUTIAO || MP-BAIDU -->
									<div class="goods_one center" @tap="Back_Tabbar">
										<img :src="goodsDetailed_gwc" />
										<p>{{language.goods.goodsDet.shopping_cart}}</p>
										<view class="order_bottom_icon_s">
											<view v-if="cart_nums>0" class="order_bottom_icon">{{cart_nums}}</view>
										</view>
									</div>
									<!-- #endif -->
								</template>
								<!-- 加入购物车/立即购买 -->
								<template
									v-if="pro.status == '3' || pro.recycle == '1' || shop_list.is_open == 2 || pro.num == 0">
									<!-- 直播商品isLive -->
									<div v-if="isLive" class="noAddCar">
										<div class="goods_two _buy1">
											<view class="goods_two_btnl" style="background: #cccccc;">
												{{language.goods.goodsDet.Buy_now}}
											</view>
										</div>
									</div>
									<div v-else-if="(pro.is_distribution == '1' || active == '6') && pro.num != 0">
										<div class="goods_two" style="width: 210rpx;">
											<view class="goods_two_btn" style="background: #cccccc;">
												{{language.goods.goodsDet.share_make}}
											</view>
										</div>
										<div class="goods_two _buy1" style="width: 210rpx;">
											<view class="goods_two_btnl" style="background: #cccccc;">
												{{language.goods.goodsDet.Buy_now}}
											</view>
										</div>
									</div>

									<div v-else-if="(pro.is_distribution == '1' || active == '6') && pro.num == 0">
										<div class="goods_two" style="width: 210rpx;">
											<view class="goods_two_btn" style="background: #cccccc;">
												{{language.goods.goodsDet.share_make}}
											</view>
										</div>
										<div class="goods_two _buy1" style="width: 210rpx;">
											<view class="goods_two_btnl" style="background: #cccccc;">
												{{language.goods.goodsDet.Buy_now}}
											</view>
										</div>
									</div>
									<div v-else-if="pro.is_distribution == '0' && active != '6'">
										<div class="goods_two" style="width: 210rpx;">
											<view class="goods_two_btn" style="background: #cccccc;">
												{{language.goods.goodsDet.add_cart}}
											</view>
										</div>
										<div class="goods_two _buy1" style="width: 210rpx;">
											<view class="goods_two_btnl" style="background: #cccccc;">
												{{language.goods.goodsDet.Buy_now}}
											</view>
										</div>
									</div>
								</template>
								<template v-else>
									<!-- 直播商品isLive -->
									<div v-if="isLive" class="noAddCar">
										<div class="goods_two _buy1" @tap="_buy">
											<view class="goods_two_btnl">{{language.goods.goodsDet.Buy_now}}</view>
										</div>
									</div>
									<!--  -->
									<div v-else-if="pro.is_distribution == '0' && active == '6'">
										<div class="goods_two _buy" @tap="_buy">{{language.goods.goodsDet.Buy_now}}
										</div>
									</div>
									<div v-else-if="pro.is_distribution == '1'">
										<div class="goods_two" @tap="showShareMask(0)"
											style="width: 192rpx;margin-right: 10rpx;">
											<view class="goods_two_btn">{{language.goods.goodsDet.share_make}}</view>
										</div>
										<div class="goods_two _buy1" @tap="_buy" style="width: 210rpx;">
											<view class="goods_two_btnl">{{language.goods.goodsDet.Buy_now}}</view>
										</div>
									</div>

									<div v-else-if="pro.is_distribution == '0' && active != '6' &&!vipprice">
										<div class="goods_two" @tap="_shopping" style="width: 210rpx;">
											<view class="goods_two_btn">{{language.goods.goodsDet.add_cart}}</view>
										</div>
										<div class="goods_two _buy1" @tap="_buy" style="width: 210rpx;">
											<view class="goods_two_btnl">{{language.goods.goodsDet.Buy_now}}</view>
										</div>
									</div>
									<div v-else-if="pro.is_distribution == '0' && active != '6' &&vipprice">
										<div class="goods_two _buy1" @tap="_buy"
											style="width: 450rpx;justify-content: flex-end;padding-right: 32rpx;">
											<view style="width: 450rpx;" class="goods_two_btnl">
												{{language.goods.goodsDet.Buy_now}}
											</view>
										</div>
									</div>
								</template>
							</div>
						</div>
						<div class="goods_foot" :style="{ height: height }" v-else>
							<div class="goods_bottom bt_0">
								<div class="goods_two goods_two_gd"
									@tap="navTo('/pagesC/bargain/bargain?topTabBar=true')">
									{{language.goods.goodsDet.More_bargains}}
								</div>

								<div v-if="leftTime <= 0 || brStatus == -1" class="goods_two btn-gray">
									{{language.goods.goodsDet.Bargain_now}}
								</div>
								<div v-else-if="brStatus == 4 && isbegin == 1" class="goods_two btn-black"
									@tap="toBr()">
									{{language.goods.goodsDet.Bargain_now}}
								</div>
								<div v-else-if="brStatus == 0 && leftTime > 0 && isbegin == 1"
									class="goods_two btn-black" @tap="toBr()">
									{{language.goods.goodsDet.Continue_bargain}}
								</div>
								<!--@tap="toBr()" 继续砍价 -->
								<div v-else-if="leftTime < 0 && isbegin == 1" class="goods_two btn-black">
									{{language.goods.goodsDet.Finished}}
								</div>
								<div v-else-if="leftTime > 0 && isbegin != 1" class="goods_two btn-black">
									{{language.goods.goodsDet.Not_started}}
								</div>
								<div v-else-if="brStatus == 1 && hasorder == 0 && isbegin == 1"
									:class="{gray:brStatus == 1 && hasorder == 0 && isbegin == 1}"
									class="goods_two btn-black">{{language.goods.goodsDet.Bargain_now}}</div>
								<!-- 去付款 -->

								<div v-else-if="brStatus == 1 && hasorder == 1 && isbegin == 1 || brStatus == 1 && isbegin == 1"
									class="goods_two btn-black"
									:class="{gray:brStatus == 1 && hasorder == 1 && isbegin == 1 || brStatus == 1 && isbegin == 1}">
									{{language.goods.goodsDet.Bargain_now}}<!-- 待付款 -->
								</div>
								<div v-else-if="brStatus == 2 && isbegin == 1"
									:class="{gray:brStatus == 2 && isbegin == 1}" class="goods_two btn-black">
									{{language.goods.goodsDet.Bargain_now}}
								</div><!-- 已付款 -->
								<div v-else-if="brStatus == 3 || leftTime < 0"
									:class="{gray:brStatus == 3 || leftTime < 0}" class="goods_two btn-black">
									{{language.goods.goodsDet.Bargain_now}}
								</div><!-- 砍价失败 -->
							</div>
						</div>
					</template>
					<!-- 
                        虚拟商品 加入购物车 立即购买 按钮逻辑
                    -->
					<template v-else>
						<div class="goods_foot" v-if="!bargain">
							<div class="goods_bottom safe-area-inset-bottom"
								:class="{ goods_bottoms: pro.status == '3' }">
								<!-- 店铺 -->
								<div class="goods_one center" @tap="_goStore(shop_list.shop_id)">
									<img :src="goodsDetailed_dp" />
									<p>{{language.integral.integral_detail.dp}}</p>
								</div>
								<!-- 客服 -->
								<div class="goods_one center" @tap="kf(shop_list.shop_id)">
									<img :src="goodsDetailed_kf" />
									<p>{{language.goods.goodsDet.customer_service}}</p>
								</div>
								<!-- 分享(直播调整) -->
								<div class="goods_one center" @tap="showShareMask(0)" v-if='isLive'>
									<img :src="weixin_share" />
									<p>分享</p>
								</div>
								<!-- 收藏 -->
								<div class="goods_one center" @tap="_collection" v-else>
									<img :src="collection ? goodsDetailed_ysc : goodsDetailed_sc" />
									<p v-if="collection">{{language.goods.goodsDet.Collected}}</p>
									<p v-else>{{language.goods.goodsDet.Collection}}</p>
								</div>
								<!-- 购物车 -->
								<template v-if='!isLive'>
									<!-- #ifdef MP-WEIXIN -->
									<div class="goods_one center" @tap="Back_Tabbar">
										<img :src="goodsDetailed_gwc" />
										<p>{{language.goods.goodsDet.shopping_cart}}</p>
										<view class="order_bottom_icon_s">
											<view v-if="cart_nums>0" class="order_bottom_icon">{{cart_nums}}</view>
										</view>
									</div>
									<!-- #endif -->
									<!-- #ifndef MP-WEIXIN || MP-TOUTIAO || MP-BAIDU -->
									<div class="goods_one center" @tap="Back_Tabbar">
										<img :src="goodsDetailed_gwc" />
										<p>{{language.goods.goodsDet.shopping_cart}}</p>
										<view class="order_bottom_icon_s">
											<view v-if="cart_nums>0" class="order_bottom_icon">{{cart_nums}}</view>
										</view>
									</div>
									<!-- #endif -->
								</template>
								<!-- 加入购物车/立即购买 -->
								<div :class="isAddCar!=1?'noAddCar':''">
									<!-- 商品上架 没打烊 显示-->
									<template v-if="status == 2 && shop_list.is_open == 1">
										<!-- 无需核销+无需预约才显示 加入购物车 -->
										<div class="goods_two" v-if="isAddCar == 1">
											<view class="goods_two_btn" @tap="_shopping">
												{{language.goods.goodsDet.add_cart}}
											</view>
										</div>
										<!-- 商品数量不为0显示 立即购买-->
										<template v-if="pro.num != 0">
											<div class="goods_two _buy1">
												<view class="goods_two_btnl" @tap="_buy">
													{{language.goods.goodsDet.Buy_now}}
												</view>
											</div>
										</template>
										<!-- 商品数量为0显示 立即购买-->
										<template v-else>
											<div class="goods_two _buy1">
												<view class="goods_two_btnl" style="background: #cccccc;">
													{{language.goods.goodsDet.Buy_now}}
												</view>
											</div>
										</template>
									</template>
									<!-- 商品已下架 显示 -->
									<template v-else>
										<div class="goods_two _buy1" v-if="isAddCar == 1">
											<view class="goods_two_btnl" style="background: #cccccc;">
												{{language.goods.goodsDet.add_cart}}
											</view>
										</div>
										<div class="goods_two _buy1">
											<view class="goods_two_btnl" style="background: #cccccc;">
												{{language.goods.goodsDet.Buy_now}}
											</view>
										</div>
									</template>
								</div>
							</div>
						</div>
					</template>

					<div class="share" v-if="g_show">
						<div><img :src="top_img" /></div>
					</div>
				</div>

				<!-- 没数据 -->
				<div v-else>
					<!--  轮播图   -->
					<div class="relative">
						<swiper class="swiper">
							<swiper-item><img class="swipe skeleton-rect" src="" /></swiper-item>
						</swiper>
					</div>
					<!-- 非砍价商品价格和名称 -->
					<div class="goods_name" v-if="!bargain">
						<p style="width: 80px;" class="goods_price skeleton-rect">
							<span class="span">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
							{{ LaiKeTuiCommon.formatPrice(mockPro.price) }}
						</p>
						<p style="padding-bottom: 0;margin-bottom: 9px;height: 21px;"
							class="goods_proName skeleton-rect">{{ mockPro.name }}</p>
						<div class="volume">
							<span
								class="skeleton-rect">{{language.goods.goodsDet.Freight_rules}}：{{ mockPro.yunfei }}</span>
							<span
								class="skeleton-rect">{{language.goods.goodsDet.sales_volume}}{{ mockPro.volume }}</span>
						</div>
					</div>
					<!-- 砍价商品价格和名称 -->
					<div class="goods_name goods_bargain_name skeleton-rect" v-else>
						<p class="goods_price skeleton-rect">
							xxxx{{language.goods.goodsDet.completed}}
							<span class="cs_yprice skeleton-rect">xxxxx</span>
							<span class="cs_price skeleton-rect">xxxx</span>
						</p>
						<p class="">{{ mockPro.name }}</p>
					</div>
					<div class="hr" v-if="isDistribution == false"></div>
					<div class="coupon skeleton-rect" @tap="getCoupon()"
						v-if="isDistribution == false && !bargain && active != 6 && coupon_status == true">
						<span class="">{{language.goods.goodsDet.Collect_coupons}}</span>
					</div>
					<div class="hr"></div>
					<div v-if="!bargain" class="hr"></div>
					<div class="user gd_user skeleton-rect" v-if="comments.length && !bargain" @tap="_evaluate(pro_id)">
						<p>{{language.goods.goodsDet.User_evaluation}}（{{ comments.length }}）</p>
						<img style="width: 16rpx;height: 22rpx;" class="arrow" :src="you_img" />
					</div>
					<div class="comments skeleton-rect" v-if="comments.length && !bargain">
						<div class="allCenter">
							<img class="img skeleton-circle" />
							<span class="span skeleton-rect">xxxx</span>
						</div>
						<div class="time skeleton-rect">xxxx-xx-xx</div>
						<div class="disc skeleton-rect">xxxxxxxxxxxxxxxxxxxxx</div>
					</div>
					<div class="user skeleton-rect">
						<p style="font-size: 30rpx;">{{language.goods.goodsDet.No_evaluation}}</p>
					</div>
					<!-- 砍价页面显示 -->
					<div>
						<div class="user gd_user" @tap="_evaluate(pro_id)">
							<p class="skeleton-rect">{{language.goods.goodsDet.sheets}}（{{ comments.length }}）</p>
							<img class="arrow" :src="you_img" />
						</div>
						<div class="user gd_user skeleton-rect">
							<p>{{language.goods.goodsDet.sheets}}（0）</p>
							<img class="arrow" :src="you_img" />
						</div>
						<template>
							<div class="comments">
								<div class="allCenter">
									<img class="img skeleton-circle" />
									<span class="span skeleton-rect">xxxxx</span>
								</div>
								<div class="time skeleton-rect">xxxx-xx-xx</div>
								<div class="disc skeleton-rect">xxxxxxxxxxxxxx</div>
							</div>
						</template>
						<div class="user skeleton-rect" v-if="comments.length <= 0">
							<p class="font_30">{{language.goods.goodsDet.No_evaluation}}</p>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!--        苹果底部黑条适配-->
		<div class="goods-detailed-safe-area-inset-bottom">

		</div>
		<skeleton :animation="true" :loading="true" v-if="!loadFlag" bgColor="#FFF"></skeleton>


		<img class="zhiding" :src="zhiding" @tap="_zhiding" v-if="scrollTopNum>500" />
		<!-- 播放视频 -->
		<view class="mask" v-show="video_dio == true" @click.stop="videoChange()" style="z-index: 999;">
			<!-- controls="controls" -->
			<video class="wh_video" :src="showVideo" autoplay="true" @click.stop="stopChange()"
				show-fullscreen-btn="false">
			</video>
		</view>
		<!-- 领取优惠券 -->
		<div class="mask" v-if="couponMask">
			<div class="couponMask">
				<div class="couponWapper">
					<div class="title">
						<div style="font-weight: bold;">{{language.goods.goodsDet.Collect_coupons}}</div>
						<img style="-webkit-touch-callout: default;" class="cha" :src="guan_img"
							@tap="closeCouponMask" />
					</div>
					<div class="couponList" v-if="coupon_list && coupon_list.length > 0">
						<div class="couponItem" v-for="(item, index) in coupon_list" :key="index">
							<div class="rightPart">
								<div class="limit">{{ item.name }}</div>
								<span class="money"
									v-if="item.activity_type == 1">{{language.goods.goodsDet.free_shipping}}</span>
								<span class="money" v-if="item.activity_type != 1">
									<span class="font_32"
										v-if="item.money > 0">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
									{{ LaiKeTuiCommon.formatPrice((item.money > 0 ? item.money : item.discount ))}}
									<span class="font_32"
										v-if="item.money <= 0">{{language.goods.goodsDet.fracture}}</span>
								</span>
								<span class="payLine">{{ item.limit }}</span>
							</div>

							<div class="splitLine"></div>

							<div class="leftPart">
								<button class="toReceive" v-if="item.point == '立即领取'"
									@tap="_receive(index, item.id)">{{ language.goods.goodsDet.lingqu }}</button>
								<button class="toReceive clicks"
									v-else-if="item.point == '可用商品'">{{language.goods.goodsDet.Received}}</button>
								<img style="-webkit-touch-callout: default;" class="img" alt=""
									v-if="item.point == '可用商品'" :src="coupon_on" />
							</div>
						</div>
					</div>

					<div v-else class="no_coupon">
						<img :src="no_coupon" alt="" />
						<p>{{language.goods.goodsDet.coupons}}~</p>
					</div>
				</div>
			</div>
		</div>
		<!-- 收藏成功 -->
		<view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
			<view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
				<view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
					<image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
				</view>
				<view class="xieyi_title"
					style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
					{{language.goods.goodsDet.sccg}}
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
					{{language.goods.goodsDet.qxcg}}
				</view>
			</view>
		</view>
		<!-- 购物车添加成功 -->
		<view class="xieyi mask" style="background-color: initial;" v-if="is_gwc">
			<view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
				<view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
					<image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
				</view>
				<view class="xieyi_title"
					style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
					{{language.goods.goodsDet.cgtjgwc }}
				</view>
			</view>
		</view>
		<ruleModal v-model="isShow" @click="_toGradeUse" :title="language.goods.goodsDet.Usage_rules"
			:details="bargain_content" />
		<skus ref="attrModal" :stock="language.goods.goodsDet.stock" :nums="language.goods.goodsDet.number"
			:confirm="language.goods.goodsDet.confirm" @confirm="_confirm"></skus>

		<share ref="share" :share="share" :pro='pro'></share>

		<!-- 弹窗组件 -->
		<show-toast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" :isOneBtn="isOneBtn"
			@confirm="confirm"></show-toast>

		<notice ref="mynotice"></notice>
	</div>
</template>

<script>
import skeleton from "@/components/skeleton";
	import notice from '@/components/notice.vue'
	import showToast from '@/components/aComponents/showToast.vue'
	import heads from '../../components/header.vue';
	import skus from '../../components/skus.vue';
	import {
		jssdk_share, 
	} from "../../common/util.js";
	import {
		UniAppShareHelper
	} from "../../common/share.js";
	import {
		mapMutations,
		mapState
	} from 'vuex';
	import {
		mockPro
	} from '@/mock/goodsDetailed/goodsDetailed.js';
	import {
		LaiKeTui_axios,
		LaiKeTui_buy_handle,
		LaiKeTui_collection,
		LaiKeTui_confirm,

		LaiKeTui_shopping,
		LaiKeTui_spec,
		LaiKeTuiSetTimeData,
		LaiKeTuiShowState,
		LaiKeTuiToBr
	} from '../../static/js/goods/goodsDetailed.js';
	import {
		LaiKeTuiGetCoupon,
		LaiKeTui_receive,
	} from "@/static/js/couponRequest/index.js"
	import ruleModal from '@/components/ruleModal.vue';
	import toload from '../../components/toload.vue';
	import htmlParser from '@/common/html-parser.js';
	import share from "@/components/share.vue"; 
	import scrollViewNav from '@/components/aComponents/switchNavThree.vue';
    import LaiketuiCommon from "../../components/laiketuiCommon.js";
	import { buildUrlFromBackend, getPluginName, resolvePluginKey } from "@/common/pluginsutil.js";
	export default {
		data() {
			return {
				IntroList: [],
				topTabBar: 0,
				mockPro,
               
				back1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x_w.png',
				niming: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/landing_logo2x.png', //图片
				zhiding: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/zhiding.png', //置顶
				coupon_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/coupon_detail.png',
				guan_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
				sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
				bofang: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/bofang.png',
				close: '',
				is_susa: false,
				saves: '',
				cs_yprice: '',
				cs_price: '',
				is_gwc: false, //购物车添加成功提示
				cs_num: '',
				hour: 0,
				title: '',
				mniuate: 0,
				second: 0,
				day: 0,
				leftTime: '',
				attr_id: '',
				shareType: '',
				bargain: false,
				bargain_rule: '', //砍价规则
				bargain_content: '', //砍价规则
				isShow: '',
				shop_list: [],
				option: '',
				overflow: 'scroll',
				bgColor: [{
						item: 'transparent'
					},
					{
						item: 'transparent'
					}
				],
				fastTap: true,
				axios_complete: false, //是否已加载完初始数据
				is_goods: true,
				is_hy: 0,
				is_grade: 0,
				shop_id: '',
				goodsDetailed_ffxx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/lafengxiang.png',
				weixin_share: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/fenxiang_wq.png',
				bback_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/bback.png',
				fx_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/fx.png',
				gw_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/gw.png',
				goodsDetailed_sc: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/goodsDetailed_sc.png',
				goodsDetailed_fh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/goodsDetailed_fh.png',
				goodsDetailed_ysc: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/goodsDetailed_ysc.png',
				goodsDetailed_gwc: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/goodsDetailed_gwc.png',
				goodsDetailed_kf: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/goodsDetailed_kf.png',
				goodsDetailed_dp: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/goodsDetailed_dp.png',
				you_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantouhei2x.png',
				baga: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/baga.png',
				kf_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/kefu2x.png',
				top_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/kaobei2x.png',

				wx_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wechat.png',

				xing_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xinghei2x.png?v=' + Math.random(),
				xing_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xinghui2x.png',
				erm_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/ewmShare.png',
				erm_pyq_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wepyq.png',
				coupon_on: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coupon_on.png',
				no_coupon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noFind.png',
				share_make: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/share_make.png',
				attribute_id: '',
				attrList: '',
				imgurl: '',
				ys_price: '', //规格初始价格
				img: '',
				collection_id: null, //收藏id
				pro: {}, //商品信息
				comments: '', //评价信息
				commentsTotal: 0, //总评论数
				commodity_type: '', // 1是虚拟商品
				Back_home: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/Back_home.png',
				collection: false, //收藏状态
				type: '', //判断进入规格选择是从立即购买1、加入购物车2、规格选择进入3
				goods_x: true,
				goods_g: false,
				headBg: 'transparent',
				g_show: false, //回顶部显示
				imgurls: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/landing_logo.png', //图片
				load: true,
				shareMask: false,
				saveEWM: false,
				shares: {},
				pricel: '',
				ishead_w: 99,
				is_sus: false, //提示
				shareContent: '一起来用来客推吧！', //分享的内容
				shareHref: '', //分享的链接
				sharehrefTitle: '一起来用来客推吧!', //分享的链接的标题
				sharehrefDes: '一起来用来客推吧!', //分享的链接的描述
				shareDiv: false,
				logo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/lktlogo.png',
				list_end: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jp_end.png',
				//商品默认图片
				defaultPicture: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
				pro_id: '',
				order_no: '',
				brStatus: '',
				bargain_id: '',
				vipprice: '',
				isDistribution: false, //是否是分销商品？true是，false不是
				isLive: false, //是否是直播商品
				roomId: '', //直播间id
				ewmImg: '', //二维码图片
				login_status: 0, //0:未登录；1已登录
				active: '', //活动类型
				hasorder: 0, //是否是待付款 0|1(待付款)
				sNo_id: 0, //订单id
				isbegin: 1, //是否是已开始的砍价商品 0（否）|1（是）
				shareMask_H5: false,
				h5_url: '',
				shareshop: 0,
				couponMask: false,
				coupon_list: [],
				top_index: 99,
				clicktimes: [], //记录点击buy按钮时的时间
				allCartNum: 0, //购物车的总商品数
				status: '2',
				is_shop: false,
				headerplus: false,
				coupon_status: false,
				coupon_str: [], //显示的优惠券图标
				swiperIndex: 1, //轮播图下标
				isAnd: true,
				loadFlag: false,
				fx_id: '', // 分销id
				num: '', //规格数量
				numb: '',
				haveSkuBean: {
					name: ''
				}, //选择规则属性
				is_kj: false,
				titleColor: '#ffffff',
				vipSource: 0,
				scrollTopNum: 0,
				video: '',
				proVideo: '',
				cart_nums: 0, // 统计购物车数量
				videoIsPlay: false, //控制视频按钮
				videoIsPlay2: false, //控制视频按钮
				showVideo: '',
				video_dio: false,
				oneCover: '',
				twoCover: '',
				type1html: '',
				type1html2: '',
				share: {
					mch: {
						type: false, //是否店铺分享
						mchId: '', //店铺id
					},
					goods: {
						title: '', //分享-商品标题
						imgUrl: '', //分享-商品图片
						price: '', //分享-商品金额
					},
					shareHref: '', //分享-链接
					shareType: '', //分享-类型：普通商品（shareType: 'gm'） 分销商品（shareType: 'fx'） 竞拍商品（shareType: 'jp'） 
				},
				is_showToast: 0, //是否显示
				is_showToast_obj: {}, //弹窗显示文字
				isOneBtn: true, //弹窗只显示一个按钮
				//虚拟商品新增
				write_off_settings: '', //‘’空值实物商品 1虚拟商品核销 2虚拟商品无需核销
				mchStoreListNum: [], //虚拟商品-需要核销-核销门店信息
				isAddCar: '', //虚拟商品-需要核销 1可加入购物车 2不可加入购物车
				xnHxTime: '', //虚拟商品-需要核销 false:没有可预约的时间
				//虚拟商品新增结束
                 
                videoContext: null ,// 用于存储视频上下文对象
                isPlaying:true,
				activityEntries: [],
				activeHeaderTab: 0,
                activity_type_codes:null,//此参加的活动
				activityEntryPos: {
					top: null,
					left: null
				},
				activityEntryDrag: {
					dragging: false,
					startX: 0,
					startY: 0,
					startTop: 0,
					startLeft: 0,
					lock: ''
				},
				activityEntryBounds: null,
				activityEntrySystemInfo: null,
				activityEntryMeasuredWidth: null,
				activityEntryContentWidth: 0,
				activityEntryViewportWidth: 0,
				activityEntryMarqueeDuration: 8,
				activityEntryMarqueePlay: true
			};
		},
		computed: {
            //   Vue.prototype 上的纯对象虽然能被序列化，但模板里访问的是当前组件的 $data，而不是 Vue.prototype
             LaiketuiCommon() {
                // 所以使用整个 方法 
                return LaiketuiCommon
            },
			halfWidth: function() {
				var gru = uni.getStorageSync('data_height') ? uni.getStorageSync('data_height') : this.$store.state
					.data_height;
				var heigh = (parseInt(gru) + 44) * 2;
				heigh = uni.upx2px(heigh)
				return heigh + 'px';
			},
			activityEntryDefaultTop() {
				if (this.bargain) {
					return uni.upx2px(100);
				}
				var top = parseInt(this.halfWidth, 10);
				return isNaN(top) ? 0 : top;
			},
			activityEntryDefaultLeft() {
				return uni.upx2px(176);
			},
			activityEntryMaxWidth() {
				const info = this.activityEntrySystemInfo || uni.getSystemInfoSync();
				const width = info.windowWidth - this.activityEntryDefaultLeft - uni.upx2px(24);
				return width > 0 ? width : info.windowWidth;
			},
			activityEntryStyle() {
				const top = this.activityEntryPos.top != null ? this.activityEntryPos.top : this.activityEntryDefaultTop;
				const left = this.activityEntryPos.left != null ? this.activityEntryPos.left : this.activityEntryDefaultLeft;
				return {
					top: top + 'px',
					left: left + 'px',
					maxWidth: this.activityEntryMaxWidth + 'px'
				};
			},
			activityEntryViewportStyle() {
				if (!this.activityEntryViewportWidth) {
					return {};
				}
				return {
					width: this.activityEntryViewportWidth + 'px'
				};
			},
			activityEntryTrackStyle() {
				const duration = this.activityEntryMarqueeDuration || 8;
				return {
					animationDuration: duration + 's'
				};
			},
			localizedDetailTitle() {
				return (this.language && this.language.ProductDetails && this.language.ProductDetails.spxq) || '商品详情';
			},
			headerTabs() {
				const tabs = [{ key: 'detail', name: this.localizedDetailTitle, _type: 'detail' }];
				if (this.isDistribution === true || this.isDistribution === 'true') {
					return tabs;
				}
				if (Array.isArray(this.activityEntries) && this.activityEntries.length) {
					this.activityEntries.forEach((it) => {
						tabs.push({
							key: it.key,
							name: it.name,
							_type: 'activity',
							_payload: it
						});
					});
				}
				return tabs;
			},
			shouldCenterHeaderTabs() {
				return !this.headerTabs || this.headerTabs.length <= 3;
			},
			isVipUser() {
				return this.is_hy == 1 || this.is_hy === '1' || this.is_hy === true || this.is_grade == 1 || this.is_grade === '1';
			},
			displayPrice() {
				const vipPrice = this.pro && this.pro.vip_price !== undefined && this.pro.vip_price !== null && this.pro.vip_price !== '' ? this.pro.vip_price : this.vipprice;
				const normalPrice = this.pro && this.pro.price !== undefined && this.pro.price !== null && this.pro.price !== '' ? this.pro.price : '';
				const price = this.isVipUser ? vipPrice : normalPrice;
				return price !== undefined && price !== null && price !== '' ? price : 0;
			},
			displayYprice() {
				const vipYprice = this.pro && this.pro.vip_yprice !== undefined && this.pro.vip_yprice !== null && this.pro.vip_yprice !== '' ? this.pro.vip_yprice : this.pricel;
				return vipYprice !== undefined && vipYprice !== null && vipYprice !== '' ? vipYprice : 0;
			},
			showVipYprice() {
				return this.isVipUser && this.displayYprice !== 0 && this.displayYprice !== '0' && this.displayYprice !== '0.00';
			},
			height: function() {
				if (this.leftTime > 0) {
					return uni.upx2px((750 * 79) / 375) + 'px';
				}
			},
			...mapState({
				_cart_num: 'cart_num'
			})
		},
		watch: {
			activityEntries: {
				handler() {
					this.$nextTick(() => {
						this.updateActivityEntryMarquee();
					});
				},
				deep: true
			}
		},
		beforeCreate() {
			// #ifdef H5
			window.scrollTo(0, 0);
			window.onbeforeunload = function() {
				window.scrollTo(0, 0);
			}
			// #endif
		},
		onPageScroll(e) {
			//是否显示--回到顶部
			this.scrollTopNum = e.scrollTop

			//是否隐藏 header组件中的标题模块
			if (e.scrollTop > 0) {
				this.headBg = '#ffffff'
				this.titleColor = '#333333'
				this.ishead_w = '2'
				this.fanhui = 2
				this.title = this.language.ProductDetails.spxq,
					this.isAnd = false
				this.top_index = 98
			} else {
				this.headBg = 'transparent'
				this.titleColor = '#FFFFFF'
				this.ishead_w = '99'
				this.fanhui = 3
				this.title = '',
					this.isAnd = true
				this.top_index = 99
			}
		},
		onLoad(option) {
			this.getCart()
            
            if (option.productId) {
            	this.pro_id = option.productId;
            } else if (option.pro_id) {
            	this.pro_id = option.pro_id;
            }
            
			// #ifdef H5 
			if (option.store_id) {
				let storeID = option.store_id;
				 this.LaiKeTuiCommon.initStoreID(storeID);
			}
			// #endif 
			const vipFlag = option.is_hy == 1 || option.is_hy === '1' || option.is_hy === true || option.is_hy === 'true' || option.vipSource == 1 || option.vipSource === '1'
			this.is_hy = vipFlag ? 1 : 0
			this.vipSource = vipFlag ? 1 : 0
			this.setLang();
			this.shareContent = this.language.showModal.shareContent
			this.sharehrefTitle = this.language.showModal.shareContent
			this.sharehrefDes = this.language.showModal.shareContent
			uni.setStorageSync('vipSource', vipFlag ? '1' : '0')
			this.option = option;
			this.fastTap = true;

			this.leftTime = option.leftTime;
              
			
			this.bargain_id = option.bargain_id;

			if (option.fx_id) {
				this.fx_id = option.fx_id;
			}

			if (option.mch_id) {
				this.mch_id = option.mch_id;
			}

			if (option.bargain) {
				this.bargain = option.bargain;
			}
			this.brStatus = option.brStatus;
			this.attr_id = option.attr_id;
			this.order_no = option.order_no;
			this.sNo_id = option.sNo_id;
			this.isbegin = option.isbegin;
			if (this.bargain) {
				this.is_kj = true
				this.LaiKeTuiSetBargainRuleData();
			}

			if (this.pro_id == '' || this.pro_id == undefined) {
				this.pro_id = this.$store.state.pro_id;
			}

			if (option.isDistribution) {
				this.isDistribution = option.isDistribution;
				this.shareType = 'FX'
			}
			if (option.isLive) {
				this.isLive = option.isLive;
				this.roomId = option.roomId
				this.shareType = 'ZB'
			}
			this.hasorder = option.hasorder;
			if (option.fatherId) {
				uni.setStorageSync('fatherId', option.fatherId);
			}

		},
		onReady() {
			this.getElementHeight('.swiper-item2')
			this.activityEntrySystemInfo = uni.getSystemInfoSync();
			this.$nextTick(() => {
				this.updateActivityEntryMarquee();
			});
            
		},
		onHide() {
			this.activityEntryMarqueePlay = false;
		},
		onUnload() {
			this.activityEntryMarqueePlay = false;
		},
		onShow(option) {
			this.activityEntryMarqueePlay = true;
			this.activeHeaderTab = 0;
			/**
			 * 解决vip商品下单成功后 返回上一个 再次购买变成了 普通商品 问题
			 * 根据页面栈获取 页面地址参数
			 */
			const pages = getCurrentPages();
			const {
				options
			} = pages[pages.length - 1];
			const vipFlag = options.is_hy == 1 || options.is_hy === '1' || options.is_hy === true || options.is_hy === 'true' || options.vipSource == 1 || options.vipSource === '1'
			this.is_hy = vipFlag ? 1 : 0
			this.vipSource = vipFlag ? 1 : 0
			// 是vip商品
			uni.setStorageSync('vipSource', vipFlag ? '1' : '0')


			this.getCart()
			this.allCartNum = this.$store.state.cart_num;
			this.close = this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/close_bb.png';
			this.saves = this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/save.png';
			this.fastTap = true;

			this.haveSkuBean = ''

			let p = new Promise((resolve, reject) => {
				this.LaiKeTuiCommon.getLKTApiUrl().then(function() {
					resolve(1231);
				});
			});

			p.then(() => {
				this._axios();

				this.$nextTick(function() {
					uni.getProvider({
						service: 'share',
						success: function(res) {
						}
					});
				});
			});

			this.couponMask = false;
			this.$nextTick(() => {
				this.updateActivityEntryMarquee();
			});
			// 调用弹窗
			setTimeout(() => {
				this.$refs.mynotice.getData()
			}, 0)
		},
		//微信小程序好友分享
		onShareAppMessage: function(res) {
			//隐藏分享弹窗
			this.$refs.share._closeAllMask()
			let shareTitle = '' //分享标题
			let shareImageUrl = '' //分享图片
			if (this.share.mch.type) {
				//店铺分享
				shareTitle = this.shop_list.shop_name
				shareImageUrl = this.shop_list.posterImg
			} else {
				//商品分享
				shareTitle = this.share.goods.title
				shareImageUrl = this.share.goods.imgUrl
			}
			//分享路径
			let sharePath = this.$refs.share.share_href

			return {
				title: shareTitle,
				path: sharePath,
				imageUrl: shareImageUrl
			};
		},
        mounted() {
            if(!this.videoContext){
                this.videoContext = wx.createVideoContext('myVideo');
            } 
        },
		methods: {
			updateActivityEntryMarquee() {
				const query = uni.createSelectorQuery().in(this);
				query.select('.activity-entry-content--main').boundingClientRect((rect) => {
					if (!rect || !rect.width) {
						return;
					}
					this.activityEntryContentWidth = rect.width;
					const padding = uni.upx2px(32);
					const maxContentWidth = Math.max(0, this.activityEntryMaxWidth - padding);
					this.activityEntryViewportWidth = Math.min(this.activityEntryContentWidth, maxContentWidth || this.activityEntryContentWidth);
					const speed = 40;
					const duration = this.activityEntryContentWidth / speed;
					this.activityEntryMarqueeDuration = Math.max(6, duration);
					this.$nextTick(() => {
						this.measureActivityEntryWidth();
					});
				}).exec();
			},
			measureActivityEntryWidth() {
				const query = uni.createSelectorQuery().in(this);
				query.select('.activity-entry-bar').boundingClientRect((rect) => {
					if (rect && rect.width) {
						this.activityEntryMeasuredWidth = rect.width;
						if (this.activityEntryDrag.dragging) {
							this.initActivityEntryBounds();
						}
					}
				}).exec();
			},
			initActivityEntryBounds() {
				const info = this.activityEntrySystemInfo || uni.getSystemInfoSync();
				this.activityEntrySystemInfo = info;
				const width = this.activityEntryMeasuredWidth || this.activityEntryMaxWidth;
				const height = uni.upx2px(56);
				this.activityEntryBounds = {
					minLeft: 0,
					maxLeft: Math.max(0, info.windowWidth - width),
					minTop: this.activityEntryDefaultTop,
					maxTop: Math.max(this.activityEntryDefaultTop, info.windowHeight - height)
				};
			},
			onActivityTouchStart(e) {
				if (!e.touches || e.touches.length !== 1) {
					return;
				}
				this.measureActivityEntryWidth();
				this.initActivityEntryBounds();
				const touch = e.touches[0];
				const startTop = this.activityEntryPos.top != null ? this.activityEntryPos.top : this.activityEntryDefaultTop;
				const startLeft = this.activityEntryPos.left != null ? this.activityEntryPos.left : this.activityEntryDefaultLeft;
				this.activityEntryDrag.dragging = true;
				this.activityEntryDrag.startX = touch.pageX;
				this.activityEntryDrag.startY = touch.pageY;
				this.activityEntryDrag.startTop = startTop;
				this.activityEntryDrag.startLeft = startLeft;
				this.activityEntryDrag.lock = '';
			},
			onActivityTouchMove(e) {
				if (!this.activityEntryDrag.dragging) {
					return;
				}
				if (!e.touches || e.touches.length !== 1) {
					return;
				}
				const touch = e.touches[0];
				const dx = touch.pageX - this.activityEntryDrag.startX;
				const dy = touch.pageY - this.activityEntryDrag.startY;
				if (!this.activityEntryDrag.lock) {
					if (Math.abs(dx) < 6 && Math.abs(dy) < 6) {
						return;
					}
					this.activityEntryDrag.lock = 'drag';
				}
				if (this.activityEntryDrag.lock !== 'drag') {
					return;
				}
				let nextLeft = this.activityEntryDrag.startLeft + dx;
				let nextTop = this.activityEntryDrag.startTop + dy;
				if (this.activityEntryBounds) {
					nextLeft = Math.min(Math.max(nextLeft, this.activityEntryBounds.minLeft), this.activityEntryBounds.maxLeft);
					nextTop = Math.min(Math.max(nextTop, this.activityEntryBounds.minTop), this.activityEntryBounds.maxTop);
				}
				this.activityEntryPos = {
					left: nextLeft,
					top: nextTop
				};
				if (e.preventDefault) {
					e.preventDefault();
				}
				if (e.stopPropagation) {
					e.stopPropagation();
				}
			},
			onActivityTouchEnd() {
				this.activityEntryDrag.dragging = false;
				this.activityEntryDrag.lock = '';
			},
			syncActivityEntries(activityTypeCodes) {
				const langCode = uni.getStorageSync('lang_code') || uni.getStorageSync('language') || 'zh_CN';
				const list = [];
				const rawList = Array.isArray(activityTypeCodes)
					? activityTypeCodes
					: (activityTypeCodes && typeof activityTypeCodes === 'object' ? [activityTypeCodes] : []);
				let index = 0;
				rawList.forEach((group) => {
					if (!group || typeof group !== 'object') return;
					Object.keys(group).forEach((code) => {
						const paramStr = typeof group[code] === 'string' ? group[code] : '';
						if (!paramStr) return;
						const url = buildUrlFromBackend(code, paramStr);
						if (!url) return;
						const name = getPluginName(code, langCode);
						const baseKey = resolvePluginKey(code) || code;
						list.push({
							key: `${baseKey}-${index}`,
							code,
							name,
							url,
							paramStr
						});
						index += 1;
					});
				});
				this.activityEntries = list;
				this.activity_type_codes = activityTypeCodes || null;
			},
			onActivityTap(item) {
				if (!item || !item.url) {
					uni.showToast({
						title: this.language?.showModal?.hint || '提示',
						icon: 'none'
					});
					return;
				}
				this._navTo(item.url);
			},
	        diyVideo(){
                    // 获取视频对象 
                    if (this.isPlaying) { 
                        this.isPlaying = false
                        // 如果是暂停状态，则调用 play() 方法播放
                          this.videoContext.play();
                          this.videoIsPlay = true
                    } else {
                     // 如果正在播放，则调用 pause() 方法暂停
                       this.isPlaying = true
                       this.videoIsPlay = false
                       this.videoContext.pause();
                    }
            },
			shareData(data) {
				let shareData = {
					title: data.name,
					desc: '',
					link: window.location.href,
					imgUrl: data.photo_x,
				};
				UniAppShareHelper.onPageShow(shareData);
				UniAppShareHelper.checkOGTags();
				UniAppShareHelper.updatePageMeta(shareData);

				jssdk_share(shareData);
			},
			//弹窗点击事件
			confirm() {
				this.is_showToast = 0
			},
			//进入虚拟商品核销门店列表
			_getShopHX() {
				this._navTo('/pagesB/store/storeHx?shop_id=' + this.shop_list.shop_id + '&pro_id=' + this.pro.pro_id)
			},
			//路由跳转
			_navTo(url) {
				uni.navigateTo({
					url: url
				});
			},
			// header tabs 点击
			onHeaderTabClick(tab, index) {
				this.activeHeaderTab = index;
				if (index === 0) {
					this.scrollToGoodsDetail();
					return;
				}
				// 活动类型：仍然走原来的数据/跳转逻辑
				const payload = tab && tab._payload ? tab._payload : null;
				if (payload) {
					this.onActivityTap(payload);
				}
			},
			scrollToGoodsDetail() {
				// 定位到商品详情内容区域
				const query = uni.createSelectorQuery().in(this);
				query.select('#content_d').boundingClientRect((rect) => {
					if (!rect) {
						return;
					}
					// rect.top 是相对于视口的距离
					uni.pageScrollTo({
						scrollTop: (this.scrollTopNum || 0) + rect.top,
						duration: 300
					});
				}).exec();
			},

			// 商品详情
			_changeNav(item, index) {
				this.topTabBar = index
			},

			// 图片报错处理
			handleErrorImg(index) {
				setTimeout(() => {
					this.$set(this.pro.img_arr, index, this.defaultPicture)
				}, 0)
			},
			handleShopImg() {
				this.shop_list.shop_head = this.defaultPicture
			},
			changeVideoStatus() {
				this.video_dio = true
				this.videoIsPlay = true
				this.showVideo = this.video
			},
			changeVideoStatus2() {
				this.video_dio = true
				this.videoIsPlay2 = true
				this.showVideo = this.proVideo
			},
			stopChange() {
			},
			videoChange() {
				this.videoIsPlay = false
				this.videoIsPlay2 = false
				this.video_dio = false
				this.showVideo = ''
			},
			// 统计购物车
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
						},
						message
					} = res

					if (code == 200) {
						this.cart_nums = data.length
					}
				})
			},
			getElementHeight(element) {
				setTimeout(() => {
					let query = uni.createSelectorQuery().in(this);
					query.select(element).boundingClientRect();
					query.exec((res) => {
						if (!res) {
							//如果没获取到，再调一次
							this.getElementHeight();
						} else {
							if (res[0]) {
								this.swiperHeight = res[0].height;
							}
						}
					})
				}, 20)
			},
			SetTimeData() {
				LaiKeTuiSetTimeData(this);
			},
			swiperChange(e) {
				this.swiperIndex = e.detail.current + 1;
			},
			_zhiding() {
				uni.pageScrollTo({
					scrollTop: 0,
					duration: 300
				});
			},
			setHead() {
				const query = uni.createSelectorQuery().in(this);
				query.select('#boxs').boundingClientRect(data => {
						if (data.top > -20) {
							this.headerplus = false;
						} else if (data.top < -20) {
							this.headerplus = true;
						}
					})
					.exec();
			},
			//授权取消按钮
			cancle() {
				this.fastTap = true;
			},
			changeLoginStatus() {
				this.login_status = 1;
				this.fastTap = true;
				this._axios();
			},
			copy_url() {
				this.shareMask_H5 = true;
				this.h5_url = this.shareHref;
			},
			fh_click() { //返回按钮
				uni.navigateBack({
					fail(err) {
					}
				})
			},
			goPay() {
				if (!this.bargain_id) {
					let product = [];
					product.push({
						pid: this.pro_id
					});
					product.push({
						cid: this.attr_id
					});
					product.push({
						num: 1
					});
					product.push({
						vipprice: this.vipprice
					});
					//直播间id
					product.push({
						roomId: this.roomId
					});
					product = JSON.stringify(product);
					uni.navigateTo({
						url: '/pagesE/pay/orderDetailsr?product=' + product + '&bargain=true&bargain_id=' + this
							.bargain_id + '&order_no=' + this.order_no + '&returnR=2' + '&vipprice=' + this
							.vipprice + '&is_hy=' + this.is_hy + '&room_id=' + this.roomId
					});
				} else {
					let product = []
					product.push({
						pid: this.pro_id
					})
					product.push({
						cid: this.attr_id
					})
					product.push({
						num: 1
					})
					product.push({
						bargain: true
					})
					product.push({
						bargain_id: this.bargain_id
					})
					product.push({
						order_no: this.order_no
					})

					product.push({
						vipprice: this.vipprice
					});
					product.push({
						roomId: this.roomId
					});
					product = JSON.stringify(product)

					//#ifdef H5
					var storage = window.localStorage
					storage.product = product
					storage.bargain = true
					storage.bargain_id = this.bargain_id
					storage.order_no = this.order_no
					uni.setStorageSync('bargain', true)
					// #endif
					uni.navigateTo({
						url: '/pagesC/bargain/bargainDetailsr?product=' + product +
							'&bargain=true&bargain_id=' + this.bargain_id + '&order_no=' + this.order_no +
							'&returnR=2' + '&vipprice=' + this.vipprice
					});
				}

			},
			toBr() {
				if (this.pro.num < 1) {
					uni.showToast({
						title: this.language.goods.goodsDet.stock2,
						icon: 'none',
						duration: 1500
					});
					return;
				}
				LaiKeTuiToBr(this);
			},
			_goStore(shop_id) {
				uni.navigateTo({
					url: '/pagesB/store/store?shop_id=' + shop_id
				});
			},
			_goRule() {
				this._toGradeUse(true);
			},
			_goback() {
				if (this.option.toback == 'true') {
					uni.navigateBack({
						delta: 1
					});
					return;
				}
				// #ifdef H5
				uni.reLaunch({
					url: '/pages/shell/shell?pageType=home'
				});
				// #endif
				// #ifndef H5
				if (this.isDistribution == 'true') {
					uni.reLaunch({
						url: '/pages/shell/shell?pageType=home'
					});
					return false;
				} else {
					uni.navigateBack({
						delta: 1
					});
				}
				// #endif
			},
			_downEWM() {
				let me = this;
				uni.downloadFile({
					url: this.ewmImg,
					success(res) {
						let filePath = res.tempFilePath;
						uni.saveImageToPhotosAlbum({
							filePath: filePath,
							success() {
								uni.showToast({
									title: me.language.goods.goodsDet.Save_success,
									duration: 1500,
									icon: 'none'
								});
								me.shareMask = false;
								me.saveEWM = false;
							},
							fail: function() {
								uni.showToast({
									title: me.language.goods.goodsDet.fail_success,
									duration: 1500,
									icon: 'none'
								});
							}
						});
					},
					fail: function() {
						uni.showToast({
							title: me.language.goods.goodsDet.fail_success,
							duration: 1500,
							icon: 'none'
						});
					}
				});
			},
			/**
			 * 分享功能
			 * @param {Object} shop 店铺id：0商品分享 !=0店铺分享
			 */
			showShareMask(shop) {
				//验证登录
				this.isLogin(() => {
					//获取分享信息 这里会存在生命周期问题 显示弹窗用$nextTick延迟执行
					this.getGoodsDetailParams(shop);
					this.$nextTick(() => {
						this.$refs.share.showShareMask()
					})
				})
			},
			/**
			 * 重定向分享页面 + 携带参数
			 * @param {Object} shop 店铺id：0商品分享 !=0店铺分享
			 */
			getGoodsDetailParams(shop) {
				//参数说明：
				//shop      (店铺id)          店铺分享shop>0，否则为商品分享
				//goodsInfor(分享商品信息)     title商品名称、imgUrl商品图片、price商品金额（如果不是商品详情页面的分享可以不传）
				//plugIn    (分享插件类型)     gm普通商品、fx分销商品、jp竞拍商品...
				//pages     (重定向页面)       分享页面的路径
				//states    (重定向页面的参数)  分享的页面需要携带的参数
				let plugIn = 'gm'
				let pages = ''
				let states = ''
				let goodsInfor = {
					title: this.pro.name,
					imgUrl: this.pro.coverImage,
					price: this.pro.price
				}
				if (shop && shop > 0) {
					//店铺分享
					pages = '/pagesB/store/store'
					states = '?shop_id=' + shop + '&fatherId=' + this.pro.user_id
				} else {
					//商品分享
					pages = '/pagesC/goods/goodsDetailed'
					states = '?productId=' + this.pro_id + '&fatherId=' + this.pro.user_id + '&store_id=' + uni
						.getStorageSync('store_id')
					//如果是分销商品 没有分享商品-删除此判断
					if (this.isDistribution) {
						plugIn = 'fx'
						states = '?isDistribution=true' + '&productId=' + this.pro_id + '&fatherId=' + this.pro.user_id +
							'&fx_id=' + this.fx_id
					}
				}
				//获取分享参数
				this.LaiKeTuiCommon.shareModel(this, shop, pages, states, plugIn, goodsInfor)
			},
			kf(pro_id) {
				this.isLogin(() => {
					const temporarily = {
						imgUrl: this.pro.coverImage,
						goodsNmae: this.pro.name || this.mockPro.name,
						price: this.pro.price || this.mockPro.price,
						volume: this.pro.volume || this.mockPro.volume || 0
					}
					uni.setStorageSync('temporarily', JSON.stringify(temporarily))
					uni.setStorageSync('pId', this.pro_id)
					uni.navigateTo({
						url: '/pagesB/message/buyers_service/Regular_customer?mch_id=' + pro_id
					});

				});
			},
			_load_img() {
				this.load = false;
			},
			_goods_x() {
				this.goods_x = true;
				this.goods_g = false;
			},
			_goods_g() {
				this.goods_x = false;
				this.goods_g = true;
			},
			...mapMutations({
				cart_id: 'SET_CART_ID',
				order_id: 'SET_ORDER_ID',
				address_id: 'SET_ADDRESS_ID',
				pay_lx: 'SET_PAY_LX',
				cart_num: 'SET_CART_NUM'
			}),
			_evaluate(pro_id) {
				if (this.bargain) {
					uni.navigateTo({
						url: '/pagesC/evaluate/evaluate?bargain=true&pid=' + pro_id
					});
				} else {
					uni.navigateTo({
						url: '/pagesC/evaluate/evaluate?pid=' + pro_id
					});
				}
			},

			//收藏
			_collection() {
				LaiKeTui_collection(this);
			},
			//规格选择
			_selectG() {
				this.type = 1;
				this._mask_display();
			},
			//加入购物车
			_shopping() {
				if (this.status == 2) {
					LaiKeTui_shopping(this);
				} else {
					uni.showToast({
						title: this.language.goods.goodsDet.shelf2,
						icon: 'none'
					});
				}
			},
			_shopping1() {
				this.type = 1;
				this.fastTap = true;
				this._mask_display();
			},
			getCoupon() {
				LaiKeTuiGetCoupon(this);
			},
			_receive(index, id) {
				this.isLogin(() => {
					LaiKeTui_receive(this, id);
				});
			},
			closeCouponMask() {
				this.couponMask = false;
			},
			_buy() {
				if (!this.axios_complete) {
					return false;
				}

				let canbuy = false;

				if (this.active == 5) {
					canbuy = true;
				}


				if (this.status == 2) {
					if (this.pro.canbuy == 1 || canbuy) {
						this.LaiKeTuiCommon.laiketuiNoDoublePress(this, this._buy_handle);
					} else {
						if (this.active == 5) {

						} else {
							uni.showToast({
								title: this.language.goods.goodsDet.distribution,
								icon: 'none'
							});
						}
					}
				} else {
					uni.showToast({
						title: this.language.goods.goodsDet.shelf2,
						icon: 'none'
					});
				}
			},
			//立即购买
			_buy_handle() {
				LaiKeTui_buy_handle(this);
			},
			//打开遮罩层
			_mask_display() {
				this.$refs.attrModal._mask_display()
			},

			//确认
			_confirm(sku) {
				Object.assign(this.$data, sku)
				LaiKeTui_confirm(this);
			},
			_spec() {
				LaiKeTui_spec(this);
			},
			//选择属性
			showState(index, indx) {
				LaiKeTuiShowState(this, index, indx);
			},
			_axios() {
				LaiKeTui_axios(this);
				this.axios_complete = true;
			},
			_toGradeUse(is) {
				if (is) {
					if (!this.bargain_rule) {
						this.bargain_content = ''
					} else {
						this.bargain_content = htmlParser(this.bargain_rule);
					}

					this.isShow = !this.isShow;
				} else {
					this.isShow = !this.isShow;
				}
			},
			/**
			 * 设置砍价规则
			 * @param me
			 * @constructor
			 */
			LaiKeTuiSetBargainRuleData() {
				let data = {
					api: 'app.bargain.getRule',
				};

				this.$req.post({
					data
				}).then(res => {
					this.bargain_rule = res.data.rule;
				});
			},
			Back_Tabbar() {
				uni.redirectTo({
					url: '/pages/shell/shell?pageType=shoppingCart'
				});
			}
		},
		components: {
            skeleton,
			notice,
			heads,
			toload,
			ruleModal,
			skus,
			share,
			scrollViewNav,
			showToast
		}
	};
</script>
<style>
	page {
		background-color: #fff;
	}
</style>
<style lang="less" scoped>
	@import url("@/laike.less");
	@import url('../../static/css/goods/goodsDetailed.less');

	.noAddCar {
		margin: 0 32rpx;

		._buy1 {
			width: 388rpx;

			.goods_two_btnl {
				flex: 1;
			}
		}
	}

	.topTabBar div {
		.center();
		font-size: 28rpx !important;
		height: 100%;
		padding: 0 10rpx;
		font-weight: 500;

	}

	.activity-entry-bar {
		position: fixed;
		z-index: 1000;
		height: 56rpx;
		display: inline-flex;
		align-items: center;
		justify-content: center;
		background: rgba(255, 255, 255, 0.92);
		border-radius: 28rpx;
		padding: 0 16rpx;
		box-shadow: 0 12rpx 18rpx rgb(85, 77, 80);
		box-sizing: border-box;
	}

	.activity-entry-scroll {
		width: auto;
		max-width: 100%;
		overflow: hidden;
		flex: 0 1 auto;
	}

	.activity-entry-track {
		display: inline-flex;
		align-items: center;
		white-space: nowrap;
		animation-name: activity-entry-marquee;
		animation-timing-function: linear;
		animation-iteration-count: infinite;
		will-change: transform;
	}

	.activity-entry-track.is-paused {
		animation-play-state: paused;
	}

	.activity-entry-content {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		white-space: nowrap;
	}

	.activity-entry-item {
		display: inline-flex;
		align-items: center;
		font-size: 26rpx;
		font-weight: 600;
		color: #222222;
		line-height: 56rpx;
	}

	.activity-entry-sep {
		color: #999999;
		margin: 0 12rpx;
	}

	@keyframes activity-entry-marquee {
		0% {
			transform: translateX(0);
		}
		100% {
			transform: translateX(-50%);
		}
	}

	.headInfo {
		height: 50rpx;
	}

	.topTabBar {
		width: 100%;
		display: flex;
		align-items: center;
		font-size: 28rpx;
		background-color: #fff;
		color: #999999;
		z-index: 99;

		.itemTabBar {
			background-color: #fff;
		}

		.active1 {
			position: relative;
			color: #333333;
			;
			font-size: 32rpx !important;

			&:after {
				content: '';
				position: absolute;
				bottom: -8rpx;
				left: 50%;
				transform: translateX(-50%);
				width: 64rpx;
				height: 4rpx;
				background: #Fa5151;
				border-radius: 4rpx;
				z-index: 99;
			}
		}

	}

	.seeTitle {
		background-color: #ffffff;
		border-bottom: 1rpx solid #eee;
		width: 100%;
		font-size: 32rpx;
		font-weight: 500;
		padding: 32rpx;
		color: #333333;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		border-radius: 24rpx 24rpx 0px 0px;

		&:nth-child(1) {
			border-radius: 0;
		}
	}

	.seeTitle1 {
        box-sizing: border-box;
		background-color: #ffffff;
		width: 100%;
		font-size: 28rpx 32rpx;
		font-weight: 500;
		padding: 20rpx 32rpx 20rpx 32rpx;
		padding-left: 10rpx;
		color: #333333;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		border-radius: 24rpx 24rpx 0px 0px;

		&:nth-child(1) {
			border-radius: 0;
		}
	}

	.richtext p {
		color: #999;
		font-size: 24rpx;
	}

	.richtext {
		// line-height: 0 !important;

		img {
			display: block;
			/* 将图片转为块级元素，消除行内间距 */
			margin: 0;
			/* 清除默认外边距 */
			padding: 0;
			/* 清除默认内边距 */
			vertical-align: middle;
			/* 额外保险，避免基线对齐导致的偏移 */
		}
	}

	.new_pre_title {
		position: absolute;
		width: 376rpx;
		height: 100rpx;
		background: linear-gradient(135deg, rgba(0, 0, 0, 0.2) 0%, rgba(0, 0, 0, 0.5) 100%);
		border-radius: 16rpx 16rpx 0 0;
		font-size: 28rpx;
		color: #FFFFFF;
		text-align: center;
		line-height: 100rpx;
		margin-top: -100rpx;
		margin-left: 30rpx;
	}

	.gray {
		background: #9A9A9A;
		color: white;
	}

	.bofang_bt {
		cursor: pointer;
		border-radius: 50%;
		display: block;
		position: absolute;
		margin: auto;
		top: 0;
		bottom: 0;
		left: 0;
		right: 0;
		width: 112rpx;
		height: 112rpx;
	}

	/deep/.video_box uni-video {
		width: 100% !important;
		border-radius: 24rpx;
	}

	.disc {}
</style>
