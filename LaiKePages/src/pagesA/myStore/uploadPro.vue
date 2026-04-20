<template>
	<div class="container uploadPro " :class="{'page-loading':isLoading }">
		<lktauthorize ref="lktAuthorizeComp"></lktauthorize>
		<template v-if="title == '发布预售商品'">
			<heads class="zindex99" :title="language.uploadPro.newSet[3]" :ishead_w="2" :bgColor="bgColor"
				:titleColor="'#333333'"></heads>
		</template>
		<template v-else-if="title == '编辑预售商品'">
			<heads class="zindex99" :title="language.uploadPro.newSet[2]" :ishead_w="2" :bgColor="bgColor"
				:titleColor="'#333333'"></heads>
		</template>

		<template v-else>
			<heads class="zindex99" :title="title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
		</template>
		<div class="relative">
			<!-- 商品类型 -->
			<div class="pl_30" :class="[pageStatus == 2 ? 'pl_30_see' : '']" v-if="!inputDisabled">
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span>商品类型</span>
						</div>
						<div v-if="pageStatus == 2" class="rightInput2">
							<div class="proName">{{ goodsType == 0?'实物商品':'虚拟商品' }}</div>
						</div>
						<view v-else class="goodsType">
							<view :class="{active: goodsType == 0}" @tap="_chooseGooodsType(0)">
								<image :src="goodsType==0?shiwu01:shiwu02"></image>
								<span>{{language.goodsType.swsp}}</span>
							</view>
							<view v-if="title != '发布预售商品'&& title != '编辑预售商品'" :class="{active: goodsType == 1}"
								@tap="_chooseGooodsType(1)">
								<image :src="goodsType==1?xuniu01:xuniu02"></image>
								<span>{{language.goodsType.xnsp}}</span>
							</view>
						</view>
					</div>
				</div>
			</div>
			<div v-if="pageStatus == 2" class="seeTitle">{{language.uploadPro.info}}</div>
			<!-- 标题-》图片 -->
			<div class="pl_30" :class="[pageStatus == 2 ? 'pl_30_see' : '']">

				<!-- 国家  pageStatus:'',[0-上传，1-编辑，2-查看] -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">

						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span class="must" v-if="pageStatus != 2">*</span>
							<span>{{language.lang.ssgj}}</span>
						</div>
						<div :class="[pageStatus == 2 ? 'rightInput2' : 'rightInput1',{'input-color':inputDisabled}]"
							@tap="getCountryList()">
							<div type="text" disabled="true" v-if="pageStatus == 2">{{ countrySet.name }}</div>
							<input type="text" disabled="true" v-if="pageStatus == 0 && countryList.length > 0 "
								v-model="countrySet.name" :placeholder="language.lang.qxzgj"
								:placeholder-style="placeStyle" />
							<input type="text" disabled="true" v-if="pageStatus == 1" v-model="countrySet.name"
								:placeholder="language.lang.qxzgj" :placeholder-style="placeStyle" />
							<input type="text" disabled="true" v-if="pageStatus == 0 && proCountryPickerArray.length==0"
								v-model="countrySet.name" :placeholder="language.lang.qxzgj"
								:placeholder-style="placeStyle" style="padding-right: 70rpx;" />

							<div v-if="pageStatus != 2" class="jiantouDiv"><img :src="jiantou" alt="" /></div>
						</div>
					</div>
				</div>

				<!-- 语种  pageStatus:'',[0-上传，1-编辑，2-查看] -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span class="must" v-if="pageStatus != 2">*</span>
							<span>{{language.lang.yz}}</span>
						</div>
						<div :class="[pageStatus == 2 ? 'rightInput2' : 'rightInput1',{'input-color':inputDisabled}]"
							@tap="getLanguages()">
							<input type="text" disabled="true" v-if="pageStatus == 2" v-model="languageSet.lang_name"
								:placeholder="language.lang.qxzyz" :placeholder-style="placeStyle" />
							<input type="text" disabled="true" v-if="pageStatus == 0 && languages.length > 0 "
								v-model="languageSet.lang_name" :placeholder="language.lang.qxzyz"
								:placeholder-style="placeStyle" />
							<input type="text" disabled="true" v-if="pageStatus == 1" v-model="languageSet.lang_name"
								:placeholder="language.lang.qxzyz" :placeholder-style="placeStyle" />
							<input type="text" disabled="true"
								v-if="pageStatus == 0 && proLanguagePickerArray.length==0"
								:placeholder="language.lang.qxzyz" :placeholder-style="placeStyle"
								style="padding-right: 70rpx;" />

							<div v-if="pageStatus != 2" class="jiantouDiv"><img :src="jiantou" alt="" /></div>
						</div>
					</div>
				</div>

				<!-- 标题 -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span class="must" v-if="pageStatus != 2">*</span>
							<span>{{language.uploadPro.Product}}</span>
						</div>
						<div v-if="pageStatus == 2" class="rightInput2">
							<div class="proName">{{ proName }}</div>
						</div>
						<div v-else class="rightInput1" :class=" {'input-color':inputDisabled}">
							<input v-if="pageStatus == 0" type="text" v-model="proName" :placeholder-style="placeStyle"
								maxlength="100" :placeholder="language.uploadPro.Product_placeholder[0]" />
							<input v-if="pageStatus == 1" type="text" v-model="proName" :placeholder-style="placeStyle"
								:disabled="inputDisabled" maxlength="100"
								:placeholder="language.uploadPro.Product_placeholder[0]" />
						</div>
					</div>
				</div>
				<!-- 副标题 -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']">
					<div class="formList" :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span>{{language.uploadPro.Product_Vice}}</span>
						</div>
						<div :class="[pageStatus == 2 ? 'rightInput2' : 'rightInput1',{'input-color':inputDisabled}]">
							<input v-if="pageStatus == 0" type="text" v-model="vstName" :placeholder-style="placeStyle"
								maxlength="14" :placeholder="language.uploadPro.Product_placeholder[1]" />
							<input v-else-if="pageStatus == 1" type="text" v-model="vstName" :disabled="inputDisabled"
								:placeholder-style="placeStyle" maxlength="14"
								:placeholder="language.uploadPro.Product_placeholder[1]" />
							<div v-if="pageStatus == 2" class="rightInput2">
								<div class="proName">{{ vstName }}</div>
							</div>
						</div>
					</div>
				</div>
				<!-- 关键字 -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']" v-if="type!='yushou'">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span class="must" v-if="pageStatus != 2">*</span>
							<span>{{language.uploadPro.keyword}}</span>
						</div>
						<div :class="[pageStatus == 2 ? 'rightInput2' : 'rightInput1',{'input-color':inputDisabled}]">
							<input type="text" v-if="pageStatus == 0" v-model="keyWord" :placeholder-style="placeStyle"
								maxlength="14" :placeholder="language.uploadPro.keywordPH" />
							<input type="text" v-if="pageStatus == 1" v-model="keyWord" :placeholder-style="placeStyle"
								:disabled="inputDisabled" maxlength="14" :placeholder="language.uploadPro.keywordPH" />
							<div v-if="pageStatus == 2" class="rightInput2">
								<div class="proName">{{ keyWord }}</div>
							</div>
						</div>
					</div>
				</div>
				<!-- 实物商品才有重量 -->
				<template v-if="goodsType == 0">
					<!-- 重量 -->
					<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.weight}}</span>
							</div>
							<div
								:class="[pageStatus == 2 ? 'rightInput2' : 'rightInput1',{'input-color':inputDisabled}]">
								<input type="digit" v-if="pageStatus == 0" v-model="proWegiht"
									:placeholder-style="placeStyle" maxlength="14"
									:placeholder="language.uploadPro.Product_placeholder[2]" />
								<input type="digit" v-if="pageStatus == 1" v-model="proWegiht" :disabled="inputDisabled"
									:placeholder-style="placeStyle" maxlength="14"
									:placeholder="language.uploadPro.Product_placeholder[2]" />
								<div v-if="pageStatus == 2" class="rightInput2">
									<div class="proName">{{ proWegiht }}</div>
								</div>kg
							</div>
						</div>
					</div>
				</template>
				<!-- 类名  pageStatus:'',[0-上传，1-编辑，2-查看] -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span class="must" v-if="pageStatus != 2">*</span>
							<span>{{language.uploadPro.category}}</span>
						</div>
						<div :class="[pageStatus == 2 ? 'rightInput2' : 'rightInput1',{'input-color':inputDisabled}]"
							@tap="showProClass()">
							<input v-if="chooseClass.length == 0" disabled="true" type="text"
								:placeholder-style="placeStyle"
								:placeholder="language.uploadPro.Product_placeholder[4]" />
							<input v-else-if="chooseClass.length != 0&&pageStatus != 2" type="text" disabled="true"
								:placeholder-style="placeStyle" v-model="chooseClass[chooseClass.length - 1].pname"
								:placeholder="language.uploadPro.Product_placeholder[4]" />
							<div v-if="chooseClass.length> 0 && pageStatus == 2" class="rightInput2">
								<div class="proName">{{ chooseClass[chooseClass.length - 1].pname }}</div>
							</div>

							<div v-if="pageStatus != 2" class="jiantouDiv"><img :src="jiantou" alt="" /></div>
						</div>
					</div>
				</div>
				<!-- 品牌 -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']" @tap="showProBrand()">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span class="must" v-if="pageStatus != 2">*</span>
							<span>{{language.uploadPro.brand}}</span>
						</div>
						<div :class="[pageStatus == 2 ? 'rightInput2' : 'rightInput1',{'input-color':inputDisabled}]">
							<input type="text" v-if="pageStatus == 0" disabled="true" :placeholder-style="placeStyle"
								v-model="proBrand" :placeholder="language.uploadPro.Product_placeholder[5]" />
							<input type="text" v-if="pageStatus == 1" disabled="true" :placeholder-style="placeStyle"
								:disabled="inputDisabled" v-model="proBrand.brand_name"
								:placeholder="language.uploadPro.Product_placeholder[5]" />
							<div v-if="pageStatus == 2" class="rightInput2">
								<div class="proName">{{ proBrand.brand_name }}</div>
							</div>
							<div v-if="pageStatus != 2" class="jiantouDiv"><img :src="jiantou" alt="" /></div>
						</div>
					</div>
				</div>
				<!-- 主图 -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']"
					:style="pageStatus == 2 ? 'margin-top: 0rpx;' : 'margin-bottom: 32rpx;'">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList1']" style="align-items: initial;">

						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText']"
							:style="pageStatus == 2 ? 'height: 30rpx;' : 'padding-top:16rpx;'">
							<span class="must" v-if="pageStatus != 2">*</span>
							<span>{{language.uploadPro.cover}}</span>
						</div>

						<div class="rightInput noFlex">
							<div class="upImg1" v-if="cover_map">
								<img class="wh_100" :src="cover_map" alt="" />
								<img :src="delImg" @tap="_delImg2()" class="delImg"
									v-if="pageStatus != 2 && !inputDisabled" />
							</div>
							<div class="upImg" @tap="_chooseImg2()" v-else>
								<img :src="textIcon" style="width: 48rpx;height: 48rpx;" alt="" />
							</div>
							<div class="jianyi" v-if="pageStatus != 2">{{language.uploadPro.Tips[4]}}</div>
						</div>
					</div>
				</div>
				<!-- 图片 -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']"
					:style="pageStatus == 2 ? 'margin-top: 0rpx;' : 'margin-bottom: 14rpx;'">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList1']" style="align-items: initial;">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText']"
							:style="pageStatus == 2 ? 'height: 30rpx;' : 'padding-top:16rpx;'">
							<span class="must" v-if="pageStatus != 2">*</span>
							<span>{{language.uploadPro.Exhibition}}</span>
						</div>
						<div class="rightInput" v-if="pageStatus != 2">
							<div class="upImg1" v-for="(item, index) in showImg" :key="index">
								<img class="wh_100" :src="item" alt="" />
								<img :src="delImg" @tap="_delImg(index)" class="delImg" v-if='!inputDisabled' />
								<div v-if="index != 0" class="setMainImg" @tap="_setMain(index)">
									{{language.uploadPro.set_graph}}
								</div>
								<div v-else class="mainImg">{{language.uploadPro.graph}}</div>
							</div>
							<div v-if="showImg.length != 5 && !inputDisabled" class="upImg"
								@tap="_chooseImg(showImg.length)">
								<img :src="textIcon" style="width: 48rpx;height: 48rpx;" alt="" />
								<div class="max_5">{{language.uploadPro.Tips[0]}}</div>
							</div>
							<div class="jianyi">{{language.uploadPro.Tips[1]}}</div>
						</div>
						<div class="rightInput" v-else>
							<div class="upImg1" v-for="(item, index) in showImg" :key="index">
								<img class="wh_100" :src="item" alt="" />
								<div v-if="index == 0" class="mainImg">{{language.uploadPro.graph}}</div>
							</div>
						</div>
					</div>
				</div>
				<!-- 展示视频 -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']"
					:style="pageStatus == 2 ? 'margin-top: 0rpx;' : 'margin-bottom: 32rpx;'">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList1']" style="align-items: initial;">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText']"
							:style="pageStatus == 2 ? 'height: 40rpx;' : 'padding-top:16rpx;'">
							<span>{{language.uploadPro.zssp}}</span>
						</div>
						<div class="rightInput noFlex">
							<div class="upImg1" v-if="video">
								<!-- #ifdef H5  -->
								<video :controls="false" :show-play-btn="false" :show-fullscreen-btn="false"
									:show-center-play-btn="false" :show-loading="false" :enable-progress-gesture="false"
									class="wh_100" :src="video"
									:poster="video+'?x-oss-process=video/snapshot,t_0,f_jpg'">
									<cover-image class="bofang_bt" @click="seeVideo()" v-show="!video_dio"
										:src="bofang">
									</cover-image>
									<cover-image :src="delImg" @tap="_delVideo()" class="delImg"
										v-if="pageStatus != 2 && !inputDisabled"></cover-image>
								</video>
								<!-- #endif -->
								<!-- #ifdef MP -->
								<video :controls="false" :show-play-btn="false" :show-fullscreen-btn="false"
									:show-center-play-btn="false" class="wh_100" :src="video">
									<div>
										<img class="bofang_bt" @click="seeVideo()" v-show="!video_dio" :src="bofang" />
									</div>
								</video>
								<!-- #endif -->
								<!-- #ifdef APP-PLUS -->
								<view class="wh_100" v-html="type1html"></view>
								<image class="bofang_bt" @tap="seeVideo()" v-show="!video_dio" :src="bofang" />
								<image :src="delImg" @tap="_delVideo()" class="delImg" v-if="pageStatus != 2" />
								<!-- #endif -->
							</div>
							<div class="upImg" @tap="_chooseVideo()" v-else>
								<img :src="textIcon" style="width: 48rpx;height: 48rpx;" alt="" />
							</div>
							<div class="jianyi" v-if="pageStatus != 2">{{language.uploadPro.sptext1}}</div>
						</div>
					</div>
				</div>
			</div>

			<!-- 核销设置title -->
			<div v-if="goodsType == 1 && pageStatus == 2" class="seeTitle">核销设置</div>
			<!-- 虚拟商品 核销设置 -->
			<div v-if="goodsType == 1" class="pl_30" :class="[pageStatus == 2 ? 'pl_30_see' : '']">
				<!-- 核销设置 -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span>核销设置</span>
						</div>
						<template v-if="pageStatus == 2">
							<div class="newClass" style="font-size: 32rpx;">
								<span v-if="xnHxType == 1">线下核销</span>
								<span v-else>无需核销</span>
							</div>
						</template>
						<template v-else>
							<div class="btn_danXuan">

								<!-- 没有核门店不能选择线下核销 -->
								<div @tap="_xnDanXuan(1)">
									<span
										:style="{opacity: !haveStore?'0.5':'', backgroundColor: !haveStore?'#EEEEEE':''}"
										:class="{btn_danXuan_xuanzhong_active:xnHxType == 1 && haveStore}">
										<span :class="{btn_danXuan_xuanzhong:xnHxType == 1}"></span>
									</span>
									<span>线下核销</span>
								</div>
								<div @tap="_xnDanXuan(2)">
									<span
										:style="{opacity: pageStatus==1?'0.5':'', backgroundColor: pageStatus==1?'#EEEEEE':''}"
										:class="{btn_danXuan_xuanzhong_active:xnHxType == 2}">
										<span :class="{btn_danXuan_xuanzhong:xnHxType == 2}"></span>
									</span>
									<span>无需核销</span>
								</div>
							</div>
						</template>
					</div>
					<view v-if="pageStatus != 2" class="xnts-btn_danXuan">
						<view></view>
						<view>(发布线下核销商品，请先创建门店，设置好核销时间！)</view>
					</view>
				</div>
				<!-- xnHxType==1线下核销时间  -->
				<div v-if="xnHxType==1" :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span>核销时间</span>
						</div>
						<template v-if="pageStatus == 2">
							<div class="btn_danXuan" style="opacity: .5;">
								<switch disabled="true" style="transform:scale(0.75);margin-right: -16rpx;"
									:checked="xnHxTime" color="#FA5151" @change="_xnHxTime" />
							</div>
						</template>
						<template v-else>
							<div class="btn_danXuan">
								<switch style="transform:scale(0.75);margin-right: -16rpx;" :checked="xnHxTime"
									color="#FA5151" @change="_xnHxTime" />
							</div>
						</template>
					</div>
					<view v-if="pageStatus != 2" class="xnts-btn_danXuan">
						<view></view>
						<view>(默认为关闭状态，开启后用户购买商品需选择 预约时间)</view>
					</view>
				</div>
				<!-- xnHxType==1线下核销门店 -->
				<div v-if="xnHxType==1" :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span>核销门店</span>
						</div>
						<div v-if="pageStatus == 2" class="rightInput2">
							<div class="proName">{{xnHxShopName}}</div>
						</div>
						<div v-else class="rightInput1" @tap="_navTo('/pagesB/store/storeChoose?shop_id=' + shop_id)">
							<div>{{xnHxShopName}}</div>
							<div v-if="pageStatus != 2" class="jiantouDiv"><img :src="jiantou" alt="" /></div>
						</div>
					</div>
				</div>
			</div>

			<!-- 商品属性title -->
			<div v-if="pageStatus == 2" class="seeTitle">{{language.uploadPro.attributes}}</div>

			<div class="pl_30" :class="[pageStatus == 2 ? 'pl_30_see' : '']">
				<!-- 成本价 / 建议零售价-->
				<div class="formDiv" v-if="pageStatus != 2">
					<div class="formList">
						<div class="leftText1">
							<span class="must">*</span>
							<span>{{language.uploadPro.price[type=='gys'?10:0]}}</span>
						</div>
						<div class="rightInput1  priceColor" :class="{'input-color':inputDisabled}"><span
								class="symbol">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span><input
								v-if="!chooseClassFlag" type="digit" v-model="costM"
								:placeholder="language.uploadPro.price_placeholder[0]" :disabled="inputDisabled"
								@input="_cbj" placeholder-style="color:#FA5151;font-weight: 400;" /></div>
					</div>
				</div>
				<!-- 原价 -->
				<div class="formDiv" v-if="pageStatus != 2">
					<div class="formList">
						<div class="leftText1">
							<span class="must">*</span>
							<span>{{language.uploadPro.price[type=='gys'?8:1]}}</span>
						</div>
						<div class="rightInput1 priceColor" :class="{'input-color':inputDisabled}"><span
								class="symbol">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span><input
								v-if="!chooseClassFlag" type="digit" v-model="oldM"
								:placeholder="language.uploadPro.price_placeholder[1]" :disabled="inputDisabled"
								@input="_yj" placeholder-style="color:#FA5151;font-weight: 400;" /></div>
					</div>
				</div>
				<!-- 售价 -->
				<div class="formDiv" v-if="pageStatus != 2">
					<div class="formList">
						<div class="leftText1">
							<span class="must">*</span>
							<span>{{language.uploadPro.price[2]}}</span>
						</div>
						<div class="rightInput1 priceColor"><span
								class="symbol">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span><input
								v-if="!chooseClassFlag" type="digit" v-model="sellM"
								:placeholder="language.uploadPro.price_placeholder[2]" @input="_sj"
								placeholder-style="color:#FA5151;font-weight: 400;" /></div>
					</div>
				</div>
				<!-- 虚拟商品没有库存 设置默认库存99999 预警1 -->
				<template v-if="goodsType == 0">
					<!-- 库存 -->
					<div class="formDiv" v-if="pageStatus != 2 && type != 'yushou'">
						<div class="formList">
							<div class="leftText1">
								<span class="must">*</span>
								<span>{{language.uploadPro.inventory}}</span>
							</div>
							<div class="rightInput1" :class="{'input-color':inputDisabled}"><input
									v-if="!chooseClassFlag" type="number" v-model="stock"
									:placeholder="language.uploadPro.price_placeholder[3]" @input="_kc"
									:disabled="inputDisabled" :placeholder-style="placeStyle" /></div>
						</div>
					</div>
					<!-- 库存预警 -->
					<div class="formDiv" v-if="pageStatus != 2 && type != 'yushou'">
						<div class="formList">
							<div class="leftText1">
								<span class="must">*</span>
								<span>{{language.uploadPro.price[4]}}</span>
							</div>
							<div class="rightInput1" :class="{'input-color':inputDisabled}"><input
									v-if="!chooseClassFlag" type="number" v-model="stockWarn"
									:placeholder="language.uploadPro.price_placeholder[4]" @input="_kcyj"
									:disabled="inputDisabled" :placeholder-style="placeStyle" /></div>
						</div>
					</div>
				</template>
				<!-- 单位 -->
				<div class="formDiv" v-if="pageStatus != 2">
					<div class="formList">
						<div class="leftText1">
							<span class="must">*</span>
							<span>{{language.uploadPro.price[5]}}</span>
						</div>
						<div class="rightInput1" :class="{'input-color':inputDisabled}" @tap="showProUnit()">
							<input disabled="true" v-if="pageStatus == 0 && !chooseClassFlag" v-model="unit"
								:placeholder="language.uploadPro.price_placeholder[5]"
								:placeholder-style="placeStyle" />
							<input disabled="true" v-if="pageStatus == 1 && !chooseClassFlag" v-model="unit"
								:placeholder-style="placeStyle" />
							<input disabled="true" v-if="pageStatus == 2 && !chooseClassFlag" v-model="unit" />
							<div v-if="pageStatus != 2" class="jiantouDiv"><img :src="jiantou" alt="" /></div>
						</div>
					</div>
				</div>
				<!-- 商品属性图 -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']"
					:style="pageStatus == 2 ? 'margin-top: 0rpx;' : 'margin-bottom: 32rpx;'">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList1']" style="align-items: initial;">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText']"
							:style="pageStatus == 2 ? 'height: 30rpx;' : 'padding-top:16rpx;'">
							<span><span class="must" v-if="pageStatus != 2">*</span>{{language.uploadPro.spsxt}}</span>
						</div>
						<div class="rightInput noFlex">
							<div class="upImg1" v-if="arrimg">
								<img class="wh_100" :src="arrimg" alt="" />
								<img :src="delImg" @tap="_delImg3()" class="delImg"
									v-if="pageStatus != 2 && !inputDisabled" />
							</div>
							<div class="upImg" @tap="_chooseImg3()" v-else>
								<img :src="textIcon" style="width: 48rpx;height: 48rpx;" alt="" />
							</div>
							<div class="jianyi" v-if="pageStatus != 2">{{language.uploadPro.jy164}}</div>
						</div>
					</div>
				</div>
				<!-- 设置属性 -->
				<div class="formDiv" v-if="pageStatus != 2">
					<div class="formList">
						<div class="leftText1"><span>{{language.uploadPro.price[6]}}</span></div>
						<div v-if="pageStatus != 2" class="rightInput1" @tap="_setAttr()" style="border: 0;">
							<!-- 优化样式 -->
							<input v-if="!chooseClassFlag" disabled="true" v-model="setProt" />
							<div class="jiantouDiv"><img :src="jiantou" alt="" /></div>
						</div>
					</div>
				</div>

				<!-- 所设置的属性值start -->
				<template v-if="pageStatus != 2">
					<template v-for="(item, index) in attr_group">
						<div class="formDiv" :key="index" v-if="item.attr_list.length > 0">
							<div class="formList">
								<div class="leftText1">
									<span>{{ item.attr_group_name }}</span>
								</div>
								<div class="rightInput1 attrInput">
									<p class="attrSpan" v-for="(items, indx) in item.attr_list" :key="indx">
										<span v-if="indx > 0">|</span>
										<text>{{ items.attr_name }}</text>
									</p>
								</div>
							</div>
						</div>
					</template>
				</template>
				<div class="attrList table" :style="{ width: tableList }"
					v-if="attr_group.length > 0 && pageStatus == 2">
					<scroll-view class="scroll-view_H" scroll-x="true">
						<div class="attrListHead tr">
							<div class="list2 th" v-for="(item, index) in attr_group" :key="index">
								{{ item.attr_group_name }}
							</div>
							<div class="list1 th">{{language.uploadPro.price[type=='gys'?8:0]}}</div>
							<div class="list1 th">{{language.uploadPro.price[type=='gys'?10:1]}}</div>
							<div class="list1 th">{{language.uploadPro.price[2]}}</div>
							<div class="list1 th" v-if="type != 'yushou' && xnHxType != 2">
								<!-- 核销次数 -->
								<template v-if="goodsType == 1 &&  xnHxType == 1">
									核销次数
								</template>
								<!-- 库存 -->
								<template v-else>
									{{language.setAttr.price[3]}}
								</template>
							</div>
							<div class="list1 th">{{language.setAttr.price[4]}}</div>
						</div>
						<div class="attrListBody">
							<div class="tr" v-for="(items, index) in attr_arr" :key="index">
								<div class="list2  aa td" v-for="(item, index1) in items.attr_list" :key="index1">
									<span class="a1">{{ item.attr_name }}</span>
								</div>
								<div class="list1 td">{{ items.cbj }}</div>
								<div class="list1 td">{{ items.yj }}</div>
								<div class="list1 td">{{ items.sj }}</div>
								<div class="list1 td" v-if="type != 'yushou' && xnHxType != 2">{{ items.kucun }}</div>
								<div class="list1 td">
									<img :src="items.img" style="width: 60rpx;height: 60rpx;">
								</div>
							</div>
						</div>
					</scroll-view>
				</div>
				<!-- 所设置的属性值end -->

			</div>

			<div v-if="pageStatus == 2 " class="seeTitle">{{language.uploadPro.set_up[0]}}</div>

			<div class="pl_30" :class="[pageStatus == 2 ? 'pl_30_see' : '']">
				<!-- 运费模板 -->
				<div v-if="tabKey == 0 && goodsType == 0" :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']"
					@tap="showProFreight()">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<span class="must" v-if="pageStatus != 2">*</span>
							<span v-if="type == 'gys' && pageStatus==2 ">{{language.uploadPro.freight1}}</span>
							<span v-else>{{language.uploadPro.freight}}</span>
						</div>
						<div class="rightInput1" :class="{'input-color':inputDisabled}" v-if="pageStatus != 2">
							<input type="text" disabled="true" v-if="pageStatus == 0 && proFreightPickerArray.length>0"
								v-model="freightSet.name" :placeholder="language.uploadPro.set_placeholder[0]"
								:placeholder-style="placeStyle" />
							<input type="text" disabled="true" v-if="pageStatus == 1" v-model="freightSet.name"
								:placeholder="language.uploadPro.set_placeholder[0]" :placeholder-style="placeStyle" />
							<input type="text" disabled="true" v-if="pageStatus == 0 && proFreightPickerArray.length==0"
								:value="language.uploadPro.addFreight"
								:placeholder="language.uploadPro.set_placeholder[0]" :placeholder-style="placeStyle"
								style="padding-right: 70rpx;" />
							<div v-if="pageStatus != 2" class="jiantouDiv"><img :src="jiantou" alt="" /></div>
						</div>

						<div v-else class="rightInput2">
							<div class="proName">{{ freightSet.name }}</div>
						</div>
					</div>
				</div>

				<!-- 显示位置 -->
				<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']"
					v-if="show_adrStatus && tabKey == 0 && !['yushou','gys'].includes(type)">
					<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
						<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
							<!-- 注释原因 38868-->
							<!-- <span class="must" v-if="pageStatus != 2">*</span> -->
							<span>{{language.uploadPro.show_adr}}</span>
						</div>
						<div v-if="pageStatus != 2" class="rightInput1 boxChoose">
							<div class="boxChooseDiv" v-for="(item, index) in show_adr"
								:class="{ actives: item.status }" :key="index" @tap="_chooseShowAdr(item,index)">
								{{ item.name }}
							</div>
						</div>
						<div v-else class="rightInput2">
							<div class="proName">{{ show_adrShow }}</div>
						</div>
					</div>
					<div style="display: flex;">
						<div class="leftText1"></div>
						<view style="font-size: 12px;color: #999999;">{{language.uploadPro.text1}}</view>
					</div>
				</div>
				<!-- 预售类型 -->
				<template v-if="pageStatus == 2">
					<div class="formDiv1" v-if="type == 'yushou'">
						<div class="formList">
							<div class="leftText1">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.yslx}}</span>
							</div>
							<div class="newClass">
								<span v-if="sell_type == 1">{{language.uploadPro.modes[0]}}</span>
								<span v-else>{{language.uploadPro.modes[1]}}</span>
							</div>
						</div>
					</div>
				</template>
				<template v-else>
					<div class="formDiv" v-if="type == 'yushou'">
						<div class="formList">
							<div class="leftText1">
								<span class="must" v-if="pageStatus != 2 && pageStatus != 1">*</span>
								<span>{{language.uploadPro.yslx}}</span>
							</div>
							<template v-if="pageStatus == 0">
								<div class="btn_danXuan">
									<div @tap="_danXuan('leix', 1)">
										<span :class="{btn_danXuan_xuanzhong_active:yushou_type == 1}"><span
												:class="{btn_danXuan_xuanzhong:yushou_type == 1}"></span></span>
										<span>{{language.uploadPro.djms}}</span>
									</div>
									<div @tap="_danXuan('leix', 2)">
										<span :class="{btn_danXuan_xuanzhong_active:yushou_type == 2}"><span
												:class="{btn_danXuan_xuanzhong:yushou_type == 2}"></span></span>
										<span>{{language.uploadPro.dhms}}</span>
									</div>
								</div>
							</template>
							<template v-else>
								<div class="btn_danXuan" style="opacity: .5;">
									<div>
										<span :class="{btn_danXuan_xuanzhong_active:yushou_type == 1}"><span
												:class="{btn_danXuan_xuanzhong:yushou_type == 1}"></span></span>
										<span>{{language.uploadPro.djms}}</span>
									</div>
									<div>
										<span :class="{btn_danXuan_xuanzhong_active:yushou_type == 2}"><span
												:class="{btn_danXuan_xuanzhong:yushou_type == 2}"></span></span>
										<span>{{language.uploadPro.dhms}}</span>
									</div>
								</div>
							</template>
						</div>
					</div>
				</template>
				<!-- 定金模式 -->
				<template v-if="yushou_type == 1 && type == 'yushou'">
					<!-- 定金 -->
					<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']" v-if="type == 'yushou'">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.dj}}</span>
							</div>
							<template v-if="pageStatus == 2">
								<div class="newClass">
									<span>{{sellDj}}{{language.uploadPro.modes[6]}}</span>
								</div>
							</template>
							<template v-else>
								<div class="rightInput1 priceColor"><span
										class="symbol">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span><input
										v-if="!chooseClassFlag" type="digit" v-model="sellDj"
										:placeholder="language.uploadPro.price_placeholder[2]" @input="_sj"
										placeholder-style="color:#FA5151;font-weight: 400;" /></div>
							</template>
						</div>
					</div>
					<!-- 付定金时间类型 -->
					<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']" v-if="type == 'yushou' && pageStatus != 2">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.fdjsj}}</span>
							</div>
							<div class="btn_danXuan">
								<div @tap="_danXuan('time', 1)">
									<span :class="{btn_danXuan_xuanzhong_active:yushou_time == 1}"><span
											:class="{btn_danXuan_xuanzhong:yushou_time == 1}"></span></span>
									<span>{{language.uploadPro.mr}}</span>
								</div>
								<div @tap="_danXuan('time', 2)">
									<span :class="{btn_danXuan_xuanzhong_active:yushou_time == 2}"><span
											:class="{btn_danXuan_xuanzhong:yushou_time == 2}"></span></span>
									<span>{{language.uploadPro.zdy}}</span>
								</div>
							</div>
						</div>
						<div v-if="yushou_time == 1" class="tishi" style="margin-top: 0;">
							({{language.uploadPro.others}})</div>
					</div>
					<!-- 付定金时间 -->
					<template v-if="pageStatus == 2">
						<div class="formDiv1">
							<div class="formList2">
								<div class="leftText2">
									<span>{{language.uploadPro.time}}</span>
								</div>
								<div class="newClass">
									<template v-if="startTime">
										<span>{{startTime}} {{language.uploadPro.modes[2]}} {{endTime}}</span>
									</template>
									<template v-else>
										<span>{{language.uploadPro.modes[3]}} {{language.uploadPro.modes[2]}}
											{{language.uploadPro.modes[4]}}</span>
									</template>
								</div>
							</div>
						</div>
					</template>
					<!-- 开始时间 -->
					<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']"
						v-if="type == 'yushou' && yushou_time == 2">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.kssj}}</span>
							</div>
							<div class="rightInput1">
								<picker style="flex: 1;z-index: 90;" class='picker_ yh-picker' mode="date"
									:start="currentdate" end="2100-01-01" @change="_changeDataS">
									<view v-if="!startTime" style="color: #999999;">{{language.uploadPro.qxzrq}}</view>
									<view v-if="startTime">{{startTime}}</view>
								</picker>
								<div v-if="pageStatus != 2" class="jiantouDiv"><img :src="jiantou" alt="" /></div>
							</div>
						</div>
					</div>
					<!-- 结束时间 -->
					<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']"
						v-if="type == 'yushou'  && yushou_time == 2">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.jssj}}</span>
							</div>
							<div class="rightInput1">
								<picker style="flex: 1;z-index: 90;" class='picker_ yh-picker' mode="date"
									:start="currentdate" end="2100-01-01" @change="_changeDataE">
									<view v-if="!endTime" style="color: #999999;">{{language.uploadPro.qxzrq}}</view>
									<view v-if="endTime">{{endTime}}</view>
								</picker>
								<div v-if="pageStatus != 2" class="jiantouDiv"><img :src="jiantou" alt="" /></div>
							</div>
						</div>
					</div>
					<!-- 付尾款时间 -->
					<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']" v-if="type == 'yushou'">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.fwksj}}</span>
							</div>
							<template v-if="pageStatus == 2">
								<div class="newClass">
									<span>{{balancePayTime}}</span>
								</div>
							</template>
							<template v-else>
								<div class="rightInput1">
									<picker style="flex: 1;z-index: 90;" class='picker_ yh-picker' mode="date"
										:start="currentdate" end="2100-01-01" @change="_changeData">
										<view v-if="!balancePayTime" style="color: #999999;">
											{{language.uploadPro.qxzrq}}
										</view>
										<view v-if="balancePayTime">{{balancePayTime}}</view>
									</picker>
									<div v-if="pageStatus != 2" class="jiantouDiv"><img :src="jiantou" alt="" /></div>
								</div>
							</template>
						</div>
						<div class="tishi" v-if="pageStatus != 2">{{language.uploadPro.dtdwk }}</div>
					</div>
					<!-- 发货时间 -->
					<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']" v-if="type == 'yushou'">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.fhsj}}</span>
							</div>
							<template v-if="pageStatus == 2">
								<div class="newClass">
									<span>{{sellData}}{{language.uploadPro.modes[5]}}</span>
								</div>
							</template>
							<template v-else>
								<div class="rightInput1"><input type="digit" v-model="sellData"
										placeholder-style="color:#333333;" />{{language.uploadPro.modes[5]}}</div>
							</template>
						</div>
						<div class="tishi" v-if="pageStatus != 2">
							({{language.uploadPro.modes[7]}}{{sellData?sellData:'7'}}{{language.uploadPro.modes[8]}})
						</div>
					</div>
				</template>
				<!-- 订货模式 -->
				<template v-if="yushou_type == 2 && type == 'yushou'">
					<!-- 预售数量 -->
					<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']" v-if="type == 'yushou'">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.yssl}}</span>
							</div>
							<template v-if="pageStatus == 2">
								<div class="newClass">
									<span>{{sellNum}}</span>
								</div>
							</template>
							<template v-else>
								<div class="rightInput1"><input type="digit" v-model="sellNum"
										:placeholder="language.uploadPro.qsrys" @input="_sj"
										placeholder-style="color:#999999;" /></div>
							</template>
						</div>
					</div>
					<!-- 截止时间 -->
					<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']" v-if="type == 'yushou'">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.jzsj}}</span>
							</div>
							<template v-if="pageStatus == 2">
								<div class="newClass">
									<span>{{endDay}}{{language.uploadPro.modes[5]}}</span>
								</div>
							</template>
							<template v-else>
								<div class="rightInput1"><input type="digit" v-model="endDay" :placeholder="'7'"
										@input="_sj"
										placeholder-style="color:#333333;" />{{language.uploadPro.modes[5]}}</div>
							</template>
						</div>
						<div class="tishi" v-if="pageStatus != 2">
							({{language.uploadPro.newSet[0]}}{{endDay?endDay:'7'}}{{language.uploadPro.newSet[1]}})
						</div>
					</div>
					<!-- 发货时间 -->
					<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']" v-if="type == 'yushou'">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span class="must" v-if="pageStatus != 2">*</span>
								<span>{{language.uploadPro.fhsj}}</span>
							</div>
							<template v-if="pageStatus == 2">
								<div class="newClass">
									<span>{{sellData}}{{language.uploadPro.modes[5]}}</span>
								</div>
							</template>
							<template v-else>
								<div class="rightInput1"><input type="digit" v-model="sellData" :placeholder="'7'"
										@input="_sj"
										placeholder-style="color:#333333;" />{{language.uploadPro.modes[5]}}</div>
							</template>
						</div>
						<div class="tishi" v-if="pageStatus != 2">
							({{sellData?sellData:'7'}}{{language.uploadPro.modes[10]}})</div>
					</div>
				</template>
			</div>

			<!-- 商品视频 -->
			<div :class="[pageStatus == 2 ? 'formDiv1' : 'formDiv']"
				:style="pageStatus == 2 ? 'border-radius:24rpx;padding-bottom:32rpx' : 'border-radius:0;border-top-right-radius: 24rpx;border-top-left-radius: 20rpx;'"
				style="background-color: #fff;padding: 32rpx 32rpx 0 32rpx;margin:0">
				<div :class="[pageStatus == 2 ? 'formList2' : 'formList1']" style="align-items: initial;">
					<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText']"
						:style="pageStatus == 2 ? 'height: 40rpx;' : 'padding-top:16rpx;'">
						<span>{{language.uploadPro.spsp}}</span>
					</div>
					<div class="rightInput noFlex">
						<div class="upImg1" v-if="proVideo">
							<!-- #ifdef H5  -->
							<video :controls="false" :show-play-btn="false" :show-fullscreen-btn="false"
								:show-center-play-btn="false" :show-loading="false" :enable-progress-gesture="false"
								class="wh_100" :src="proVideo"
								:poster="proVideo+'?x-oss-process=video/snapshot,t_0,f_jpg'">
								<cover-image class="bofang_bt" @click="seeVideo2()" v-show="!video_dio" :src="bofang">
								</cover-image>
								<cover-image :src="delImg" @tap="_delVideo2()" class="delImg"
									v-if="pageStatus != 2"></cover-image>
							</video>
							<!-- #endif -->
							<!-- #ifdef MP -->
							<video :controls="false" :show-play-btn="false" :show-fullscreen-btn="false"
								:show-center-play-btn="false" class="wh_100" :src="proVideo">
								<div>
									<img class="bofang_bt" @click="seeVideo2()" v-show="!video_dio" :src="bofang" />
								</div>
							</video>
							<!-- #endif -->
							<!-- #ifdef APP-PLUS  -->
							<view class="wh_100" v-html="type1html2"></view>
							<img class="bofang_bt" @click="seeVideo2()" v-show="!video_dio" :src="bofang" />
							<img :src="delImg" @tap="_delVideo2()" class="delImg" v-if="pageStatus != 2" />
							<!-- #endif -->
						</div>
						<div class="upImg" @tap="_chooseVideo2()" v-else>
							<img :src="textIcon" style="width: 48rpx;height: 48rpx;" alt="" />
						</div>
						<div class="jianyi" v-if="pageStatus != 2">{{language.uploadPro.sptext1}}</div>
					</div>
				</div>
			</div>

			<div v-if="pageStatus == 2" class="hr"></div>

			<!-- 添加介绍START -->
			<div v-if="pageStatus == 2" class="seeTitle">
				<switchNavThree :is_ScrollView="isGoodsAdd" @_changeNav="_changeNav"></switchNavThree>
			</div>

			<div v-if="pageStatus == 2" class="noContent">
				<wxParse v-if="showContent" :content="seeGoods.content"></wxParse>
			</div>

			<!-- 商品介绍模块 增删改 -->
			<div
				style="width: 100%;background-color: #ffffff;border-radius: 0 0 24rpx 24rpx ;margin-bottom: 40rpx;overflow: hidden;">
				<div class="pl_30"
					style="margin-bottom: 0;border-top-right-radius: 0;border-top-left-radius: 0;padding-top:24rpx;padding-bottom: 32rpx;"
					:class="[pageStatus == 2 ? 'pl_30_see' : '']">
					<div v-if="pageStatus != 2" class="formDiv border_bottom_0">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<!-- 商品介绍 -->
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span>{{language.uploadPro.spjs}}</span>
							</div>
							<!-- 添加介绍 -->
							<div class="addGoods" @tap="addProductIntroduction">
								<span style="font-size: 56rpx;display: contents;">+</span>
								<span style="margin-left: 10rpx;">{{language.uploadPro.tjjs}}</span>
							</div>
						</div>
					</div>
				</div>
				<!-- 商品详情 -->
				<div v-for="(item,index) in isGoodsAdd" class="pl_30"
					style="margin-bottom: 0;border-radius: 24rpx 24rpx 0 0;padding-top: 0;padding-bottom: 32rpx;"
					:class="[pageStatus == 2 ? 'pl_30_see' : '']">
					<div v-if="pageStatus != 2" class="formDiv border_bottom_0">
						<div :class="[pageStatus == 2 ? 'formList2' : 'formList']">
							<!-- 商品介绍标题 -->
							<div :class="[pageStatus == 2 ? 'leftText2' : 'leftText1']">
								<span>{{item.name}}</span>
							</div>
							<div class="rightInput1" @tap="_toGoodDetail(item,index)">
								<!-- 去设置 -->
								<input v-if="item.content == ''" type="text" disabled="true"
									:placeholder="language.uploadPro.qsz" :placeholder-style="placeStyle" />
								<!-- 已设置 -->
								<input v-else type="text" disabled="true" :placeholder="language.uploadPro.ysz"
									:placeholder-style="placeStyle" />
								<!-- 箭头图标 -->
								<div class="jiantouDiv0" @tap.stop="_toGoodDetail(item,index)"><img :src="jiantou"
										alt="" /></div>
								<!-- 删除图标 -->
								<div class="jiantouDiv1" @tap.stop="_delGoods(index)"><img :src="readimg" alt="" />
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<!-- 添加介绍END -->

			<!-- 底部按钮start -->
			<div v-if="pageStatus == 0 && type != 'yushou' && type !='gys'" class="submitDiv">
				<div class="submitBtn div1" @tap="_Check('no')">{{language.uploadPro.Details[4]}}</div>
				<div class="submitBtn div2" @tap="_Check('yes')">{{language.uploadPro.Details[3]}}</div>
			</div>
			<div v-else-if="pageStatus == 1 && type != 'yushou' && type !='gys'" class="submitDiv">
				<div class="submitBtn div1" @tap="_Check('no')">{{language.uploadPro.Details[4]}}</div>
				<div class="submitBtn div2" @tap="_Check('yes')">{{language.uploadPro.tjfbsq}}</div>
			</div>
			<div v-else-if="type == 'yushou' && pageStatus != 2" class="submitDiv">
				<div class="submitBtn div3" @tap="_goodsYs('add')">{{language.uploadPro.modes[11]}}</div>
			</div>
			<div v-else-if="type == 'gys' && pageStatus != 2 " class="submitDiv">
				<div class="submitBtn div3" @tap="_Check('yes')">{{language.uploadPro.modes[11]}}</div>
			</div>
			<!-- 底部按钮end -->

			<!-- 选择商品所属国家弹窗 -->
			<!-- <mpvue-picker v-if="proCountryShow" :themeColor="themeColor" ref="proCountryShowPicker" :mode="mode"
                          :deepLength="deepLength" :pickerValueDefault="proCountryPickerDefault" @onConfirm="onConfirmProCountry"
                          :pickerValueArray="proCountryPickerArray"></mpvue-picker> -->
			<homeCountry :showCountryPicker="showCountryPicker" :countryList="countryList"
				@confirm="handleScreenConfirm" @cancel="showCountryPicker = false"></homeCountry>

			<!-- 选择商品语种弹窗 -->
			<mpvue-picker v-if="proLanguageShow" :themeColor="themeColor" ref="proLanguageShowPicker" :mode="mode"
				:deepLength="deepLength" :pickerValueDefault="proLanguagePickerDefault"
				@onConfirm="onConfirmProLanguage" :pickerValueArray="proLanguagePickerArray"></mpvue-picker>

			<!-- 选择商品类别弹窗 -->
			<div v-if="chooseClassFlag" ref="proClassPicker" class="mask" style="z-index: 999;" @touchmove.stop.prevent>
				<div class="mask_box">
					<p class="mask_title">
						<span></span>
						<span>{{language.uploadPro.Tips[2]}}</span>
						<img class="closeImg" :src="closeImg" @tap="_closeClass" />
					</p>
					<scroll-view scroll-x class="chooseValueBox">
						<div class="flex">
							<p class="chooseValue" v-for="(item, index) in chooseClass" :key="index">
								<image v-if="index != 0" class="fenge" :src="jiantou" mode=""></image>
								<span class="span" @tap="del_Class(item, index)">{{ item.pname }}</span>
							</p>
						</div>
					</scroll-view>
					<scroll-view scroll-y class="chooseBox" @tap="_chooseBtn">
						<ul>
							<li :class="{ active1: item.status }" v-for="(item, index) in arrClass" :key="index"
								:value="item.cid" @tap="_chooseClass(item, index)" style="display: flex;">
								{{ item.pname }}
							</li>
						</ul>
					</scroll-view>
				</div>
			</div>
			<!-- 商品品牌弹窗 -->
			<mpvue-picker v-if="proBrandShow" :themeColor="themeColor" ref="proBrandPicker" :mode="mode"
				:deepLength="deepLength" :pickerValueDefault="proBrandPickerDefault" @onConfirm="onConfirmProBrand"
				:pickerValueArray="proBrandPickerArray"></mpvue-picker>

			<!-- 运费模板弹窗 -->
			<mpvue-picker v-if="proFreightShow" :themeColor="themeColor" ref="proFreightPicker" :mode="mode"
				:deepLength="deepLength" :pickerValueDefault="proFreightPickerDefault" @onConfirm="onConfirmProFreight"
				:pickerValueArray="proFreightPickerArray"></mpvue-picker>

			<mpvue-picker v-if="proDistributorsShow" :themeColor="themeColor" ref="proDistributorsPicker" :mode="mode"
				:deepLength="deepLength" :pickerValueDefault="proDistributorsPickerDefault"
				@onConfirm="onConfirmProDistributors" :pickerValueArray="proDistributorsPickerArray"></mpvue-picker>
			<mpvue-picker v-if="proUnitShow" :themeColor="themeColor" ref="proUnitPicker" :mode="mode"
				:deepLength="deepLength" :pickerValueDefault="proUnitPickerDefault" @onConfirm="onConfirmProUnit"
				:pickerValueArray="proUnitPickerArray"></mpvue-picker>
			<showToast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj">
			</showToast>
			<view class="mask" v-show="video_dio == true" @click.stop="videoChange()" style="z-index: 999;">
				<video class="wh_video" :src="showVideo" autoplay="true" @click.stop="stopChange()"
					show-fullscreen-btn="false">
				</video>
			</view>
		</div>
		<!-- 添加商品介绍弹窗 -->
		<div class='mask1' v-if="add_mask_display">
			<div class='mask_cont'>
				<p>{{language.uploadPro.tjjs}}</p>
				<input maxlength="10" type="text" :placeholder="language.uploadPro.qsrbtmc"
					placeholder-style="color: #999999;" v-model="introduceName" />
				<div class='mask_button'>
					<div class='mask_button_left' @tap="add_mask_display = !add_mask_display">
						{{language.storeSetup.cancel}}
					</div>
					<div style="color: #D73B48;" @tap="_addConfirm2(4)">{{language.uploadPro.tj}}</div>
				</div>
			</div>
		</div>
		<view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
			<view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
				<view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
					<image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
				</view>
				<view class="xieyi_title"
					style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
					{{type =='gys' ?language.zdata.baccg : language.uploadPro.fbcg}}
				</view>
			</view>
		</view>
	</div>
</template>

<script>
	import switchNavThree from "@/components/aComponents/switchNavThree.vue"
	import htmlParser from '@/common/html-parser.js';
	import showToast from "@/components/aComponents/showToast.vue"
	import mpvuePicker from '../../components/mpvuePicker.vue';
	import wxParse from '@my-miniprogram/src/components/mpvue-wxparse/src/wxParse.vue';
	import homeCountry from './components/home_country.vue';
	import {
		getNowFormatDate
	} from "@/common/util";
	import {
		LaiKeTui_axios,
		LaiKeTui_cbj,
		LaiKeTui_Check,
		LaiKeTui_chooseImg,
		LaiKeTui_chooseVideo,
		LaiKeTui_chooseVideo2,
		LaiKeTui_chooseImg2,
		LaiKeTui_chooseImg4,
		LaiKeTui_chooseShowAdr,
		LaiKeTui_chooseSType,
		LaiKeTui_delImg,
		LaiKeTui_delImg2,
		LaiKeTui_delImg3,
		LaiKeTui_delVideo,
		LaiKeTui_delVideo2,
		LaiKeTui_editor,
		LaiKeTui_kc,
		LaiKeTui_kcyj,
		LaiKeTui_onConfirmProBrand,
		LaiKeTui_onConfirmProDistributors,
		LaiKeTui_onConfirmProFreight,
		LaiKeTui_request,
		LaiKeTui_see,
		LaiKeTui_setAttr,
		LaiKeTui_setMain,
		LaiKeTui_showProBrand,
		LaiKeTui_showProClass,
		LaiKeTui_showProDistributors,
		LaiKeTui_showProFreight,
		LaiKeTui_showProUnit,
		LaiKeTui_sj,
		LaiKeTui_yj,
		LaiKeTui_volume,
		LaiKeTui_showProCountry,
		LaiKeTui_showProLanguage,
		LaiKeTui_onConfirmProCountry,
		LaiKeTui_onConfirmProLanguage

	} from '@/pagesA/myStore/myStore/uploadPro.js';

	const UPLOAD_PRO_DRAFT_KEY = 'uploadProDraft';
	const UPLOAD_PRO_DRAFT_FROM_DETAIL_KEY = 'uploadProDraftFromDetail';
	const UPLOAD_PRO_DRAFT_KEYS = [
		'goodsType',
		'xnHxType',
		'xnHxTime',
		'xnHxShopId',
		'xnHxShopName',
		'proName',
		'vstName',
		'keyWord',
		'proWegiht',
		'proCode',
		'proClass',
		'proClassId',
		'proClassSmall',
		'proClassSmallId',
		'chooseClass',
		'arrClass',
		'secondClass',
		'haveChildrenClass',
		'proBrand',
		'proBrandId',
		'showImg',
		'showImgOld',
		'showImgNewAdd',
		'cover_map',
		'arrimg',
		'initial',
		'costM',
		'oldM',
		'sellM',
		'stock',
		'stockWarn',
		'unit',
		'vshop',
		'volume',
		'countrySet',
		'country_num',
		'languageSet',
		'lang_code',
		'freightSet',
		'freightSetId',
		's_type',
		's_typeShow',
		's_typeStr',
		'active',
		'show_adr',
		'display_position',
		'distributorsSet',
		'distributorsSetId',
		'downLineStatus',
		'downLineAdd',
		'frames_status',
		'fxStatus',
		'attr_group',
		'attr_arr',
		'richList',
		'content',
		'isGoodsAdd',
		'isGoodsAdd_index',
		'seeGoods',
		'video',
		'proVideo',
		'yushou_type',
		'yushou_time',
		'sellDj',
		'startTime',
		'endTime',
		'balancePayTime',
		'sellData',
		'sellNum',
		'endDay',
		'goodsId'
	];

	export default {
		data() {
			return {
				countryList: [],
				languages: [],
				isLoading: false,
				goodsType: 0, //0实物商品 1虚拟商品
				xnHxType: 1, //虚拟商品 1线下核销 2无需核销
				xnHxTime: false, //虚拟商品-线下核销-是否需要预约时间 false不需要预约 true需要预约
				xnHxShopId: '', //虚拟商品-线下核销-选择门店 全部门店：0 其他：'id1, id2, id3, id4,'
				xnHxShopName: '', //虚拟商品-线下核销-选择门店 name
				haveStore: '', //当前店铺是否有核销门店 0无门店，1有
				// 添加介绍
				add_mask_display: false,
				IntroList: [],
				drawingList: [],
				introduceName: "",
				topTabBar: 0,
				tabKey: '0',
				title: '上传商品',
				access_id: '',
				is_sus: false,
				shiwu01: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/shiwu01.png',
				shiwu02: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/shiwu02.png',
				xuniu01: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuniu01.png',
				xuniu02: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuniu02.png',
				sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
				textIcon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xiangji2x.png',
				delImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
				jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
				closeImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/close2x.png',
				chooseImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/chooseMeh.png',
				readimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/search/search_del.png',
				shop_id: '',
				status: '',
				p_id: '',
				pageStatus: '', //[0-上传，1-编辑，2-查看]
				widthArr: [],
				tableWidth: 0,
				upImgNum: [5, 4, 3, 2, 1],
				themeColor: '#FA5151',
				mode: '',
				deepLength: 1,
				res: [],
				proName: '',
				vstName: '',
				keyWord: '',
				proWegiht: '',
				proCode: '',
				proClass: '',
				proClassId: '',
				proClassShow: false,
				proClassPickerArray: [],
				proClassPickerDefault: [],
				proClassSmall: '',
				proClassSmallId: '',
				proClassSmallShow: false,
				proClassSmallPickerArray: [],
				proClassSmallPickerDefault: [],
				proBrand: '',
				proBrandId: '0',
				proBrandShow: false,
				proBrandPickerArray: [],
				proBrandPickerDefault: [],
				showImg: [],
				showImgOld: '', //原图 --用于对照
				showImgNewAdd: [],
				initial: '',
				costM: '',
				oldM: '',
				vshop: '',
				volume: '',
				sellM: '',
				stock: '',
				stockWarn: '',
				unit: '',
				setProt: '',

				countrySet: '',
				country_num: '',
				proCountryShow: false,
				proCountryPickerArray: [],
				proCountryPickerDefault: [],

				languageSet: '',
				lang_code: '',
				proLanguageShow: false,
				proLanguagePickerArray: [],
				proLanguagePickerDefault: [],

				proUnitShow: false,
				proUnitPickerArray: [],
				proUnitPickerDefault: [],

				freightSet: {},
				freightSetId: '',
				proFreightShow: false,
				proFreightPickerArray: [],
				proFreightPickerDefault: [],
				s_type: '',
				s_typeShow: '',
				plugin_list: [],
				active: [],
				show_adrStatus: true,
				show_adr: '',
				show_adrShow: '',
				distributorsStatus: false,
				distributorsSet: '',
				distributorsSetId: '',
				proDistributorsShow: false,
				proDistributorsPickerArray: [],
				proDistributorsPickerDefault: [],
				downLineStatus: false,
				downLineAdd: '',
				showContent: false, //是否显示商品详情
				content: '', ///商品详情
				frames_status: '',
				display_position: '',
				//以下为不必要数据
				fxStatus: '',
				s_typeStr: [],
				fastTap: true,
				secondClass: [], //二级类名数组
				haveChildrenClass: 1, //是否有副类[1-有，-1-没有]
				type_status: '', // 商品类型状态
				check_Flag: true, //限制提交按钮只能点一次
				placeStyle: 'color:#999999;font-size:28upx',
				chooseClassFlag: false,
				chooseSsgjFlag: false, //所属国家弹窗标记
				chooseLanguageFlag: false, //所属国家弹窗标记
				arrClass: [], //现在分类的数据 {id:'',name:'美妆护肤'}
				chooseClass: [], //选中的分类  {id:'',name:'美妆护肤'}
				// receivingForm:['1'],//配送方式1.邮寄 2.自提
				attr_group: [], //设置属性的表格标题
				attr_arr: [], //设置属性的表格数据
				richList: '', //商品详情
				platform1: 'android', //平台
				loadFlag: false, //已经加载完，用来规避监听事件改编属性表格
				cover_map: '', // 封面图
				arrimg: '', //商品属性图
				video: '', //展示视频
				proVideo: '', //商品详情视频
				showVideo: '',
				video_dio: false, //展示视频弹窗
				isGoodsAdd_index: 0, //回显的时候 判断是插入商品介绍模块的那个数据中
				isGoodsAdd: [], //商品介绍模块
				seeGoods: {}, //查看详情切换标签显示对应内容
				bgColor: [{
						item: '#ffffff'
					},
					{
						item: '#ffffff'
					}
				],
				//预售开始
				is_showToast: 0,
				is_showToast_obj: {},
				type: '', //进入类型 yushou-预售
				yushou_type: '', //1-定金模式  2-订货模式
				yushou_time: 1, //1-默认  2-自定义
				sellDj: '', //定金
				startTime: '', //定金支付开始时间
				endTime: '', //定金支付结束时间
				balancePayTime: '', //定金支付尾款时间
				currentdate: '', //当前时间
				sellData: '7', //发货时间
				sellNum: '', //预售数量
				endDay: '7', //截止天数
				goodsId: '', //商品id 查看详情时使用
				//预售结束
				inputDisabled: false, //输入框禁用
				type1html: '',
				type1html2: '',
				bofang: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/bofang.png',
				xqList: [],
				lang_text: '',
				showCountryPicker: false
			};
		},
		watch: {
			freightSet: {
				handler(val) {
				},
				deep: true,
				immediate: true
			},
			isGoodsAdd: {
				//监听事件
				handler(val) {
					//监听商品介绍的变化，并赋值给content 用于传给后台
					this.content = JSON.stringify(val)
				},
				deep: true, // 开启深度监听  专门对付复杂数据类型
				immediate: true, // 首次监听  一打开页面就会监听
			},
			costM(val) {
				if (this.attr_arr && this.attr_arr.length > 0 && this.loadFlag) {
					this.attr_arr.filter(item => {
						item.cbj = val;
					});

					uni.setStorageSync('upload_attr_arr', this.attr_arr);
				}
			},
			oldM(val) {
				if (this.attr_arr && this.attr_arr.length > 0 && this.loadFlag) {
					this.attr_arr.filter(item => {
						item.yj = val;
					});

					uni.setStorageSync('upload_attr_arr', this.attr_arr);
				}
			},
			sellM(val) {
				if (this.attr_arr && this.attr_arr.length > 0 && this.loadFlag) {
					this.attr_arr.filter(item => {
						item.sj = val;
					});

					uni.setStorageSync('upload_attr_arr', this.attr_arr);
				}
			},
			vshop(val) {

				this.attr_arr.filter(item => {
					item.volume = val;
				})

				uni.setStorageSync('upload_attr_arr', this.attr_arr);
				// }
			},

			lang_code(val, oldVal) {

				//语种联动数据
				LaiKeTui_axios(this);
				//1：分类联动
				//2：品牌联动
				//3：运费联动
				//4：显示位置联动
				//5：商品标签联动

			},
		},
		onLoad(option) {
			uni.showLoading({
				title: this.language.toload.loading
			});
			this.isLoading = true
			// 初始化数据
			uni.removeStorageSync('uploadProXnShopListId')
			uni.removeStorageSync('uploadProXnShopListName')
			/**
			 * 预售插件 数据处理开始
			 */
			if (option.type) {
				this.type = option.type
				if (this.type == 'yushou') {
					if (option.pageStatus == 'see') {
						this.title = this.language.ProductDetails.spxq
					} else if (option.pageStatus == 'editor') {
						this.title = '编辑预售商品'
					} else {
						this.title = '发布预售商品'
					}
					this.goodsId = option.id
				}
				if (this.type == 'gys') {
					this.title = '编辑供应商商品'
					// 如果是店铺供应商商品编辑 只能修改售价 其余信息不可以被修改
					this.inputDisabled = true
					this.goodsId = option.id
				}
			}
			//获取当前时间
			this.currentdate = getNowFormatDate()
			/**
			 * 预售插件 数据处理结束
			 */
			if (option.commodity_type == 0 || option.commodity_type == 1) {
				this.goodsType = option.commodity_type
			}
			this.platform1 = uni.getSystemInfoSync().platform;
			this.status = option.pageStatus;
			if (option.p_id) {
				this.p_id = option.p_id;
			}

			this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
			this.access_id = uni.getStorageSync('access_id') ? uni.getStorageSync('access_id') : this.$store.state
				.access_id;

			this.isLogin(async () => {
				let defaultLang = uni.getStorageSync("language");
				this.showContent = false;
				if (!this.status) {
					uni.hideLoading();
					this.isLoading = false
					this.pageStatus = 0;
					//登录后必有语种信息
					this.lang_code = defaultLang;
					LaiKeTui_axios(this);
				} else if (this.status == 'editor') {
					this.pageStatus = 1;
					if (this.type != 'yushou') {
						await LaiKeTui_editor(this)
					} else {
						this._setGoods()
					}
				} else if (this.status == 'see' || this.status == 'seePro') {
					this.pageStatus = 2;
					if (this.type != 'yushou') {
						LaiKeTui_see(this);
					} else {
						this._seeGoods()
					}
				}
			})

			if (option.commodity_type == 1) {
				this.getFreight()
			}
		},
		async onShow() {

			const result = await this.LaiKeTuiCommon.getCountryList();
			this.countryList = result.data || result.arg.data;
			this.proCountryPickerArray = [];
			for (let i = 0; i < this.countryList.length; i++) {
				this.proCountryPickerArray.push(this.countryList[i].name)

			}

			const result1 = await this.LaiKeTuiCommon.getLanguages();
			this.languages = result1.data || result1.arg.data;
			this.proLanguagePickerArray = [];
			let defaultLang = uni.getStorageSync("language");

			//设置默认语种
			this.lang_code = defaultLang + ""
			for (let i = 0; i < this.languages.length; i++) {
				this.proLanguagePickerArray.push(this.languages[i].lang_name)
				if (this.languages[i].lang_code == defaultLang) {
					this.languageSet = {
						lang_name: this.languages[i].lang_name,
						lang_code: this.languages[i].lang_code
					}
				}
			}

			//虚拟商品-线下核销-选择门店
			if (uni.getStorageSync('uploadProXnShopListName') && uni.getStorageSync('uploadProXnShopListId')) {
				this.xnHxShopId = uni.getStorageSync('uploadProXnShopListId')
				this.xnHxShopName = uni.getStorageSync('uploadProXnShopListName')
			} else {
				this.xnHxShopId = 0
				this.xnHxShopName = '全部'
			}
			// 得到数据
			if (uni.getStorageSync("richList")) {
				try {
					let list = JSON.parse(uni.getStorageSync("richList"))
					let index = JSON.parse(uni.getStorageSync("richIndex"))
					this.IntroList[index].content = list
					uni.removeStorageSync("richList");
					uni.removeStorageSync("richIndex");
				} catch (error) {
				}
			}
			if (!this.status) {
				if (this.type == 'yushou') {
					this.title = '发布预售商品'
					uni.setNavigationBarTitle({
						title: '发布预售商品'
					})
				} else {
					this.title = this.language.uploadPro.title
					uni.setNavigationBarTitle({
						title: this.language.uploadPro.title
					})
				}
			} else if (this.status == 'editor') {
				this.title = this.language.uploadPro.title1;
				uni.setNavigationBarTitle({
					title: this.language.uploadPro.title1
				})
				if (this.type == 'yushou') {
					if (this.status == 'see') {
						this.title = this.language.ProductDetails.spxq
					} else if (this.status == 'editor') {
						this.title = '编辑预售商品'
					} else {
						this.title = '发布预售商品'
					}
				}
				if (this.type == 'gys') {
					this.title = '编辑供应商商品'
					// 如果是店铺供应商商品编辑 只能修改售价 其余信息不可以被修改
					this.inputDisabled = true
				}
			} else if (this.status == 'see' || this.status == 'seePro') {
				this.title = this.language.uploadPro.title2;
				uni.setNavigationBarTitle({
					title: this.language.uploadPro.title2
				})
			}

			this.isLogin(() => {})

			if (this.pageStatus == 0 && this.proFreightPickerArray.length == 0 && this.loadFlag && this.type !=
				'gys') {
				this.getFreight()
			}

			this.platform1 = uni.getSystemInfoSync().platform;
			if (this.pageStatus != 2) {
				if (uni.getStorageSync('upload_attr_group')) {
					this.attr_group = uni.getStorageSync('upload_attr_group');
					this.attr_arr = uni.getStorageSync('upload_attr_arr');
				}
			}
			this.tableWidth = this.$store.state.tableWidth;
			if (this.pageStatus != 2) {
				this._restoreUploadProDraftFromDetail();
			}
			if (this.$store.state.detaiFlag) {
				this.richList = this.$store.state.goodsDetail;
				this.$store.state.detaiFlag = undefined;
				if (this.isGoodsAdd && this.isGoodsAdd[this.isGoodsAdd_index]) {
					this.isGoodsAdd[this.isGoodsAdd_index].content = this.getGoodsDetailsContext()
				}
			}
			this.lang_text = uni.getStorageSync('language')
		},
		onUnload() {
			uni.removeStorageSync('upload_attr_group');
			uni.removeStorageSync('upload_attr_arr');
			uni.removeStorageSync('upload_chooseAttr');

			uni.removeStorageSync('upload_attrDataList');
			uni.removeStorageSync('upload_addAttrStr');
			uni.removeStorageSync('upload_addAttrObj');
		},
		beforeDestroy() {
			this.$store.state.goodsDetail = [];
			this.fastTap = true;

			this.$req
				.post({
					data: {

						api: "mch.App.Mch.Del",

						shop_id: this.shop_id
					}
				})
				.then(res => {});
		},

		methods: {
			_cloneUploadProDraftValue(value) {
				if (value === undefined) {
					return value;
				}
				try {
					return JSON.parse(JSON.stringify(value));
				} catch (e) {
					return value;
				}
			},
			_buildUploadProDraft() {
				const draft = {};
				for (const key of UPLOAD_PRO_DRAFT_KEYS) {
					if (Object.prototype.hasOwnProperty.call(this.$data, key)) {
						const value = this[key];
						if (value !== undefined) {
							draft[key] = this._cloneUploadProDraftValue(value);
						}
					}
				}
				return draft;
			},
			_applyUploadProDraft(draft) {
				if (!draft || typeof draft !== 'object') {
					return;
				}
				for (const key of UPLOAD_PRO_DRAFT_KEYS) {
					if (Object.prototype.hasOwnProperty.call(draft, key) &&
						Object.prototype.hasOwnProperty.call(this.$data, key)) {
						this[key] = this._cloneUploadProDraftValue(draft[key]);
					}
				}
			},
			_saveUploadProDraft() {
				try {
					const draft = this._buildUploadProDraft();
					uni.setStorageSync(UPLOAD_PRO_DRAFT_KEY, JSON.stringify(draft));
				} catch (e) {}
				uni.setStorageSync(UPLOAD_PRO_DRAFT_FROM_DETAIL_KEY, 1);
			},
			_restoreUploadProDraftFromDetail() {
				if (!uni.getStorageSync(UPLOAD_PRO_DRAFT_FROM_DETAIL_KEY)) {
					return;
				}
				uni.removeStorageSync(UPLOAD_PRO_DRAFT_FROM_DETAIL_KEY);
				const draftStr = uni.getStorageSync(UPLOAD_PRO_DRAFT_KEY);
				if (!draftStr) {
					return;
				}
				try {
					const draft = JSON.parse(draftStr);
					this._applyUploadProDraft(draft);
				} catch (e) {}
			},
			// 点击国家
			handleScreenConfirm(e) {
				this.showCountryPicker = false
				if (this.pageStatus == 1) {
					this.countrySet.name = e.name
				} else {
					this.countrySet = {
						name: e.name
					}
				}
				this.country_num = e.num3
			},

			// 点击国家
			// onConfirmProCountry(e) {
			//     LaiKeTui_onConfirmProCountry(this, e);
			// },

			// 展示国家信息：国家信息没有做控制
			async getCountryList() {
				const result = await this.LaiKeTuiCommon.getCountryList();
				this.countryList = result.data;
				this.showCountryPicker = true
				// LaiKeTui_showProCountry(this);
			},

			// 点击语种
			onConfirmProLanguage(e) {
				LaiKeTui_onConfirmProLanguage(this, e);
			},

			// 展示商城语种：商城没有绑定的语种不展示
			async getLanguages() {
				const result = await this.LaiKeTuiCommon.getLanguages();
				this.languages = result.data;
				LaiKeTui_showProLanguage(this);
			},



			//选择商品类型
			_chooseGooodsType(index) {

				if (this.pageStatus == 1) {
				}

				if (this.type == 'yushou') {
				}
				this.goodsType = index
				//实物商品 库存 预警初始化为空
				if (index == 0) {
					this.stock = ''
					this.stockWarn = ''
					this.freightSet = {
						name: ''
					}
					this.freightSetId = ''
				} else {
					//运费字段手动修改不为空
					this.freightSet = {
						name: '虚拟商品无运费'
					}
					this.freightSetId = ''
					if (this.xnHxType == 1) {
						//虚拟商品-需要核销 (库存改为默认1核销次数 预警为1)
						this.stock = 1
					} else if (this.xnHxType == 2) {
						//虚拟商品-无需核销 (库存为9999999 预警为1)
						this.stock = 9999999
					}
					this.stockWarn = 1
					//虚拟商品 - 店铺存在核销门店时默认选择 需核销 否则 无需核销
					if (this.haveStore) {
						this.xnHxType = 1
					} else {
						this.xnHxType = 2
					}
				}
			},
			//跳转
			_navTo(urls) {
				uni.navigateTo({
					url: urls
				})
			},
			// 商品详情
			changeTabBar(val) {
				this.topTabBar = val
			},
			// 添加介绍
			_addConfirm() {
				this.add_mask_display = false
				let data = {
					name: this.introduceName,
					content: []
				}
				this.IntroList.push(data)
			},
			//商品详情 切换
			_changeNav(item, index) {
				this.seeGoods = item
			},
			// 添加介绍
			_addConfirm2() {
				if (this.introduceName == '' || !this.introduceName) {
					uni.showToast({
						title: this.language.uploadPro.qsrbtmc,
						duration: 1500,
						icon: 'none'
					})
					return
				}
				let new_obj = {}
				new_obj.name = this.introduceName
				new_obj.content = ''
				this.isGoodsAdd.push(new_obj)
				this.add_mask_display = false
				this.introduceName = ''
			},
			// 添加介绍
			addProductIntroduction() {
				// 供应商商品编辑时 不可以 添加介绍
				if (this.inputDisabled) {
					return
				}
				this.add_mask_display = true
			},
			//删除商品介绍
			_delGoods(index) {
				// 供应商商品编辑时 不可以 删除商品介绍
				if (this.inputDisabled) {
					return
				}
				this.isGoodsAdd.splice(index, 1)
			},
			_addCancel() {
				this.add_mask_display = false
			},
			handleAddIntroduce() {
				this.add_mask_display = true
				this.introduceName = ""
			},
			_handleToGoodDetail(val, index) {
				uni.navigateTo({
					url: `../myStore/newGoodsDetail?itemIntroList=${JSON.stringify(val)}&index=${index}`
				});
				this.$store.state.goodsDetail = JSON.parse(JSON.stringify(val.content));
			},
			_handleDelDetatil(i) {
				this.IntroList.splice(i, 1)
			},

			getFreight() {
				let data = {

					api: "mch.App.Mch.Upload_merchandise_page",
					shop_id: this.shop_id,
				}
				this.$req.post({
					data
				}).then(res => {
					let {
						code,
						data,
						message
					} = res

					if (code == 200) {
						this.haveStore = data.haveStore || 0
						this.xnHxType = this.haveStore ? 1 : 2
						this.proFreightPickerArray = []
						if (typeof data.freight_list == 'object') {
							for (let i in data.freight_list) {
								this.proFreightPickerArray.push(data.freight_list[i].name)

								if (data.freight_list[i].is_default == 1) {

									this.freightSet = {
										name: data.freight_list[i].name,
									}
									this.freightSetId = data.freight_list[i].id
								}
							}
							return
						}
						for (var i = 0; i < data.freight_list.length; i++) {

							this.proFreightPickerArray.push(data.freight_list[i].name)

							if (data.freight_list[i].is_default == 1) {

								this.freightSet = {
									name: data.freight_list[i].name,
								}
								this.freightSetId = data.freight_list[i].id
							}

						}
					} else {
						uni.showToast({
							title: message,
							duration: 1500,
							icon: 'none'
						})
					}
				})
			},
			// 返回
			back() {
				uni.navigateBack({
					delta: 1
				});
			},
			// 获取分类
			showProClass() {
				// 供应商商品编辑时 不可以修改 分类
				if (this.inputDisabled) {
					return
				}
				if (this.pageStatus != 2) {
					LaiKeTui_showProClass(this);
				}
			},
			//选中类名
			_chooseClass(item, index) {

				for (let i = 0; i < this.arrClass.length; i++) {
					if (index != i) {
						this.arrClass[i].status = false
					}
				}

				item.status = !item.status

			},
			_chooseBtn() {
				let arrClass = this.arrClass.filter(item => {
					return item.status
				})

				if (arrClass.length == 0) {
					uni.showToast({
						title: this.language.uploadPro.classToast,
						icon: 'none'
					})
					return
				}

				let item = arrClass[0]

				if (typeof this.proBrand == 'string') {
					this.proBrand = ''
				} else {
					this.proBrand.brand_name = ''
				}
				this.proBrandId = ''

				let data = {

					api: "mch.App.Mch.Choice_class",
					shop_id: this.shop_id,
					cid: item.cid,
					brand_str: this.proBrandId,
				}

				this.$req.post({
					data
				}).then(res => {
					let {
						code,
						data,
						message
					} = res

					if (code == 200) {
						var chooseClass_num = this.chooseClass.length - 1
						this.proClassId = item.cid
						this.proClass = item.pname

						this.chooseClass[chooseClass_num] = item
						if (data.list.class_list.length == 0) {
							this.chooseClassFlag = false
						} else {
							this.arrClass = data.list.class_list[0]
							this.chooseClass.push({
								pname: this.language.uploadPro.choice
							})
						}

						this.proBrandPickerArray = []
						this.res.brand_class_list = data.list.brand_list
						var proBrandId = this.proBrandId
						this.proBrand = ''
						this.proBrandId = '0'

						for (var i = 0; i < data.list.brand_list.length; i++) {
							if (proBrandId == data.list.brand_list[i].brand_id) {
								this.proBrandId = data.list.brand_list[i].brand_id
								this.proBrand = data.list.brand_list[i].brand_name
							}

							if (data.list.brand_list[i].notset == 0) {
								this.proBrandPickerArray.push(data.list.brand_list[i].brand_name)
							}
						}

					} else {
						uni.showToast({
							title: message,
							duration: 1500,
							icon: 'none'
						})
					}
				})
			},
			// 删除类名
			del_Class(item, index) {
				if (item.pname == "请选择" && !item.cid) {
					return
				}

				this.chooseClass = this.chooseClass.splice(0, index);

				if (this.chooseClass.length == 0) {
					this.proClassId = 0;
				} else {
					this.proClassId = this.chooseClass[this.chooseClass.length - 1].cid;
				}

				if (typeof this.proBrand == 'string') {
					this.proBrand = ''
				} else {
					this.proBrand.brand_name = ''
				}
				this.proBrandId = ''

				this.showProClass();
			},
			// 选择品牌
			showProBrand() {
				// 供应商商品编辑时 不可以修改 选择品牌
				if (this.inputDisabled) {
					return
				}
				if (this.pageStatus == 2) {
					return
				}
				if (!this.proClassId) {
					return uni.showToast({
						icon: 'none',
						title: this.language.uploadPro.Tips[3]
					});
				}
				LaiKeTui_showProBrand(this);
			},
			// 点击品牌
			onConfirmProBrand(e) {
				LaiKeTui_onConfirmProBrand(this, e);
			},
			// 删除图片
			_delImg(index) {
				LaiKeTui_delImg(this, index);
			},
			// 删除图片
			_delImg2() {
				LaiKeTui_delImg2(this);
			},
			// 删除图片
			_delImg3() {
				LaiKeTui_delImg3(this);
			},
			// 删除展示视频
			_delVideo() {
				LaiKeTui_delVideo(this);
			},
			// 删除商品视频
			_delVideo2() {
				LaiKeTui_delVideo2(this);
			},
			// 设为主图
			_setMain(index) {
				LaiKeTui_setMain(this, index);
			},
			// 选择封面图片
			_chooseImg2() {
				LaiKeTui_chooseImg2(this);
			},
			// 选择封面图片
			_chooseImg3() {
				LaiKeTui_chooseImg4(this);
			},
			// 选择图片
			_chooseImg(num) {
				LaiKeTui_chooseImg(this, num);
			},
			// 上传展示视频
			_chooseVideo() {
				// 供应商商品编辑时 不可以修改 上传展示视频
				if (this.inputDisabled) {
					return
				}
				LaiKeTui_chooseVideo(this);
			},
			// 上传展示视频
			_chooseVideo2() {
				// 供应商商品编辑时 不可以修改 上传展示视频
				if (this.inputDisabled) {
					return
				}
				LaiKeTui_chooseVideo2(this);
			},
			// 成本价
			_cbj(e) {
				LaiKeTui_cbj(this, e);
			},
			// 原价
			_yj(e) {
				LaiKeTui_yj(this, e);
			},
			// 售价
			_sj(e) {
				LaiKeTui_sj(this, e);
			},
			// 虚拟销量
			_volume(e) {
				// 正则只允许输入正整数
				e.target.value = e.target.value.replace(/^0|[^\d]|[.]/g, '')
				this.$nextTick(() => {
					this.vshop = e.target.value
				})
				LaiKeTui_volume(this, e);
			},
			// 库存
			_kc(e) {
				// 正则只允许输入正整数
				e.target.value = e.target.value.replace(/^0|[^\d]|[.]/g, '')
				this.$nextTick(() => {
					this.stock = e.target.value
				})
				LaiKeTui_kc(this, e);
			},
			// 库存预警
			_kcyj(e) {
				// 正则只允许输入正整数
				e.target.value = e.target.value.replace(/^0|[^\d]|[.]/g, '')
				this.$nextTick(() => {
					this.stockWarn = e.target.value
				})
				LaiKeTui_kcyj(this, e);
			},
			// 选择单位
			onConfirmProUnit(e) {
				this.unit = e.label;
				this.show = false;
			},
			// 显示单位
			showProUnit() {
				// 供应商商品编辑时 不可以修改 显示单位
				if (this.inputDisabled) {
					return
				}
				LaiKeTui_showProUnit(this);
			},
			// 设置属性
			_setAttr() {
				// 虚拟商品 编辑时不允许修
				LaiKeTui_setAttr(this);
			},
			// 运费
			showProFreight() {
				// 供应商商品编辑时 不可以修改 运费
				if (this.inputDisabled) {
					return
				}
				if (this.pageStatus != 2) {
					if (this.proFreightPickerArray.length == 0) {
						uni.navigateTo({
							url: '/pagesA/myStore/freight'
						})
						return
					}
					LaiKeTui_showProFreight(this);
				}
			},



			// 点击运费
			onConfirmProFreight(e) {
				LaiKeTui_onConfirmProFreight(this, e);
			},

			// 显示标签
			_chooseSType(index) {
				// 供应商商品编辑时 不可以修改 显示标签
				if (this.inputDisabled) {
					return
				}
				LaiKeTui_chooseSType(this, index);
			},
			// 支持活动
			_changeActive(num) {},
			// 显示位置
			_chooseShowAdr(item, index) {
				// 供应商商品编辑时 不可以修改 显示位置
				if (this.inputDisabled) {
					return
				}
				LaiKeTui_chooseShowAdr(this, item, index);
			},
			// 等级绑定
			showProDistributors() {
				LaiKeTui_showProDistributors(this);
			},
			// 点击分销等级
			onConfirmProDistributors(e) {
				LaiKeTui_onConfirmProDistributors(this, e);
			},
			_downLineStatus: function(e) {
				this.downLineStatus = e.target.value;
			},
			// 商品详情
			_toGoodDetail(row, index) {
				this.isGoodsAdd_index = index
				let richList = htmlParser(row.content)
				var arrList = richList.map((item, index) => {
					if (item.name == 'p') {
						if (item.children[0].name == 'img') {
							return {
								style: item.attrs?.style,
								value: item.children[0].attrs.src,
								tagType: "image",
							}
						} else {
							return {
								style: item.attrs?.style,
								value: item.children[0].text,
								tagType: item.name,
							};
						}
					} else if (item.name == 'img') {
						return {
							style: item.attrs.style,
							value: item.attrs.src,
							tagType: 'image',
						};
					} else if (item.name == 'video') {
						return {
							style: item.attrs.style,
							value: item.attrs.src,
							tagType: item.name,
						};
					}
				});
				this.$store.state.goodsDetail = arrList
				if (this.pageStatus != 2) {
					this._saveUploadProDraft();
				}
				//跳转
				uni.navigateTo({
					url: '/pagesA/myStore/goodsDetail?title=' + this.isGoodsAdd[index].name
				});
			},
			// 提交
			_Check(type) {
				// 样式优化后改为返回（之前是保存到本地）
				if (type == 'no') {
					uni.navigateBack()
					return
				}
				// 样式优化后改为返回end
				if (this.check_Flag) {
					this.check_Flag = false;
					LaiKeTui_Check(this, type);
				}
			},
			async _request(type) {
				LaiKeTui_request(this, type);
			},
			/**
			 * 获取
			 */
			getGoodsDetailsContext() {
				let myGoodsInfo = this.$store.state.goodsDetail;
				let htmlStr = '';
				for (var x in myGoodsInfo) {
					let elementHtmlInfo = myGoodsInfo[x];
					let tagType = elementHtmlInfo.tagType;
					let style = elementHtmlInfo.style;
					let htmlText = elementHtmlInfo.value;
					switch (tagType) {
						case 'p':
						case 'P':
							htmlStr += '<' + tagType + ' style="' + style + '">' + htmlText + '</' + tagType + '>';
							break;
						case 'image':
							htmlStr += '<img style="' + style + '" src="' + htmlText + '" />';
							break;
						case 'video':
							htmlStr += '<video style="' + style + '" src="' + htmlText + '" />';
							break;
					}
				}

				return htmlStr;
			},

			_fxStatus: function(e) {
				this.fxStatus = e.target.value;
			},
			seeVideo() {
				this.showVideo = this.video
				this.video_dio = true
			},
			seeVideo2() {
				this.showVideo = this.proVideo
				this.video_dio = true
			},
			// 关闭类名选择
			_closeClass() {

				this.chooseClassFlag = false;

				this.chooseClass = this.chooseClass.filter(item => {
					return item.pname != "请选择"
				})

			},

			/**
			 * 预售插件 数据处理开始
			 */

			/**
			 * 预售商品 选择模式
			 * @param 
			 */
			_danXuan(type, value) {
				if (type == 'leix') {
					this.yushou_type = value
				} else if (type == 'time') {
					this.yushou_time = value
				}
			},
			/**
			 * 虚拟商品 核销设置
			 * @param 
			 */
			_xnDanXuan(value) {
				//虚拟商品
				if (value == 1) {
					//没有门店不能选择线下核销
					if (!this.haveStore) {
						return
					}
					//虚拟商品-需要核销 (库存改为默认1核销次数 预警为1)
					this.stock = 1
				} else if (value == 2) {
					//编辑进入不能切换核销设置
					if (this.pageStatus == 1) {
						return
					}
					//虚拟商品-无需核销 (库存为9999999 预警为1)
					this.stock = 9999999
				}
				this.stockWarn = 1
				this.xnHxType = value
			},
			/**
			 * 虚拟商品 核销时间
			 * @param 
			 */
			_xnHxTime(event) {
				this.xnHxTime = event.detail.value
			},
			videoChange() {
				this.video_dio = false
			},
			stopChange() {
			},
			/**
			 * 预售商品 发布预售商品
			 * @param type类型add发布新商品xxx保存
			 */
			_goodsYs(type) {
				if (type == "add") {
					//指针
					let me = this
					// 成本价/原价/售价/单位 初始值 与设置属性值不一样
					me.initial = 'cbj=' + me.costM + ',yj=' + me.oldM + ',sj=' + me.sellM + ',unit=' + me.unit +
						',kucun=9999999' + ',attrImg=' + me.arrimg
					//为空提示
					if (!me.shop_id) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[0],
							icon: 'none'
						})
						return
					}
					if (!me.proName) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[1],
							icon: 'none'
						})
						return
					}
					if (me.proName && me.proName.length > 20) {
						uni.showToast({
							title: me.language.toasts.uploadPro.spmcmax20,
							icon: 'none'
						})
						return
					}
					if (!me.proWegiht) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[2],
							icon: 'none'
						})
						return
					}
					if (!me.proClassId) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[3],
							icon: 'none'
						})
						return
					}
					if (me.proBrandId == 0) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[4],
							icon: 'none'
						})
						return
					}
					if (me.showImg.length == 0) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[5],
							icon: 'none'
						})
						return
					}
					if (!me.costM || !me.oldM || !me.sellM) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[6],
							icon: 'none'
						})
						return
					}
					if (me.attr_group.length == 0) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[7],
							icon: 'none'
						})
						return
					}
					if (me.attr_arr.length == 0) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[8],
							icon: 'none'
						})
						return
					}
					if (!me.freightSetId || me.freightSetId == 0) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[9],
							icon: 'none'
						})
						return
					}

					if (!me.unit) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[11],
							icon: 'none'
						})
						return
					}
					if (!me.cover_map) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[12],
							icon: 'none'
						})
						return
					}
					if (!me.sellDj && me.yushou_type != 2) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[13],
							icon: 'none'
						})
						return
					}
					if (!me.sellData) {
						uni.showToast({
							title: me.language.toasts.uploadPro.fbYs[14],
							icon: 'none'
						})
						return
					}
					if (me.yushou_type == 1) {
						//定金模式
						if (!me.balancePayTime) {
							uni.showToast({
								title: me.language.toasts.uploadPro.fbYs[15],
								icon: 'none'
							})
							return
						}
						//付定金时间是否自定义 
						if (me.yushou_time == 2) {
							if (me.startTime == '') {
								uni.showToast({
									title: me.language.toasts.uploadPro.fbYs[16],
									icon: 'none'
								})
								return
							}
							if (me.endTime == '') {
								uni.showToast({
									title: me.language.toasts.uploadPro.fbYs[17],
									icon: 'none'
								})
								return
							}
						}
					}
					// 发布预售商品 针对 预售模式 做的处理
					if (this.type == 'yushou') {
						// 订货模式 库存 跟着 填写的预售数量走
						if (this.yushou_type == 2) {
							me.attr_arr.forEach(v => {
								v.kucun = me.sellNum
							})
							me.initial = 'cbj=' + me.costM + ',yj=' + me.oldM + ',sj=' + me.sellM +
								',unit=' + me.unit + ',kucun=' + me.sellNum + ',attrImg=' + me.arrimg

						} else if (this.yushou_type == 1) {
							// 定金模式 库存 直接 9999
							me.attr_arr.forEach(v => {
								v.kucun = 9999999
							})
							me.initial = 'cbj=' + me.costM + ',yj=' + me.oldM + ',sj=' + me.sellM +
								',unit=' + me.unit + ',kucun=9999999' + ',attrImg=' + me.arrimg
						}
					}
					//添加的商品数据
					let data = {
						//api: 'app.mchPreSell.addOrUpdateGoods',
						api: 'plugin.presell.AppPreSell.addOrUpdateGoods',
						shopId: me.shop_id, //店铺id
						productTitle: me.proName, //商品标题
						subtitle: me.vstName, //商品副标题
						//scan: me.proCode?me.proCode,//条形码
						productClassId: me.proClassId, //商品类名
						brandId: me.proBrandId, //商品品牌
						keyword: (me.keyWord || '').toString(), //关键字
						weight: me.proWegiht, //重量
						showImg: me.showImg, //商品图片
						initial: me.initial, //初始值 成本价/原价/售价
						attrGroup: JSON.stringify(me.attr_group), //属性/规格
						attrArr: JSON.stringify(me.attr_arr), //属性详细信息
						freightId: me.freightSetId, //运费模版
						//displayPosition: me.displayPosition?me.displayPosition,//显示位置
						active: 1, //支持活动
						//distributorId: me.distributorId?me.distributorId,//关联的分销层级id
						content: this.content, //产品内容
						//volume: me.LaiKeTuiCommon.isempty(me.vshop) ? 0 : me.vshop,//虚拟销量
						richList: JSON.stringify(me.richList), //产品数组内容 (设置前端店铺商品详情插件)
						//mchStatus: me.mchStatus?me.mchStatus,//商品审核状态 1.待审核，2.审核通过，3.审核不通过，4.暂不审核
						unit: me.unit, //单位
						stockWarn: 10, //库存预警
						coverMap: me.cover_map, //商品主图
						//sort: me.sort?me.sort,//序号
						video: me.video,
						proVideo: me.proVideo,
						//预售类型
						sellType: me.yushou_type,
						//定金
						deposit: me.sellDj ? me.sellDj : '', //定金
						payType: me.yushou_time ? me.yushou_time : '', //支付定金时间类型
						deliveryTime: me.sellData ? me.sellData : '', //发货时间
						//订货
					}
					if (me.s_typeStr) {
						data.sType = me.s_typeStr.join() //显示标签
					}
					if (me.goodsId) {
						data.pId = me.goodsId //商品id(修改时传)
					}
					if (me.startTime) {
						data.startTime = me.startTime + ' ' + '00:00:00' //定金支付开始时间
					}
					if (me.endTime) {
						data.endTime = me.endTime + ' ' + '00:00:00' //定金支付结束时间
					}
					if (me.balancePayTime) {
						data.balancePayTime = me.balancePayTime + ' ' + '00:00:00' //付尾款时间
					}
					if (me.sellNum) {
						data.sellNum = me.sellNum //预售数量
					}
					if (me.yushou_type == 2) {
						data.endDay = me.endDay //截止天数
					}
					//请求
					this.$req.post({
						data
					}).then(res => {
						if (res.code == 200) {
							this.is_showToast = 1
							this.is_showToast_obj.imgUrl = this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
								'images/icon1/sus.png',
								this.is_showToast_obj.title = '保存成功'
							setTimeout(() => {
								this.is_showToast = 0
								uni.navigateBack({
									delta: 1
								})
							}, 2000)
						} else {
							uni.showToast({
								title: res.message,
								icon: 'none'
							})
						}
					})
				}
			},

			/**
			 * 预售商品 选择支付尾款时间选择
			 * @param null
			 */
			_changeData(e) {
				this.balancePayTime = e.detail.value
			},

			/**
			 * 预售商品 选择支付开始时间选择
			 * @param null
			 */
			_changeDataS(e) {
				this.startTime = e.detail.value
			},

			/**
			 * 预售商品 选择支付结束时间选择
			 * @param null
			 */
			_changeDataE(e) {
				this.endTime = e.detail.value
			},

			/**
			 * 预售商品 查看详情
			 * @param null
			 */
				_seeGoods() {
				let data = {
					api: 'plugin.presell.AppPreSell.getGoodsInfoById',
					goodsId: this.goodsId, //商品id
				}
				this.$req.post({
					data
					}).then(res => {
						setTimeout(() => {
							uni.hideLoading();
							this.isLoading = false
						}, 1000)
						if (res && res.code == 200 && res.data) {
							const sellGoodInfo = res.data.sellGoodInfo || {}
							const listInfo = res.data.list || {}
							// 国家回显
							this.country_num = res.data.country_num
						this.countrySet = {
							name: res.data.country_name,
							num3: res.data.country_num
						}

						// 语种回显
						this.lang_code = res.data.lang_code
						this.languageSet = {
							lang_name: res.data.lang_name,
							lang_code: res.data.lang_code
						}
						this.video = res.data.video
						this.proVideo = res.data.proVideo
						this.proName = res.data.product_title
						this.vstName = res.data.subtitle
						this.keyWord = (res.data.keyword || '').toString()
						this.chooseClass = res.data.ctypes
						this.proBrand = res.data.brand_class_list1
						this.cover_map = res.data.cover_map
						this.showImg = res.data.imgurls
						this.attr_group = res.data.attr_group_list
						this.attr_arr = res.data.checked_attr_list
						this.freightSet = res.data.freight_list1
						this.freightSetId = res.data.freight_list1.id
						for (var i in res.data.sp_type) {
							if (res.data.sp_type[i].status) {
								this.s_typeShow += ',' + res.data.sp_type[i].name
							}
						}
						this.s_typeShow = this.s_typeShow.replace(',', '')
							this.yushou_type = sellGoodInfo.sell_type
							this.sell_type = sellGoodInfo.sell_type
							this.sellDj = sellGoodInfo.deposit
							if (sellGoodInfo.depositStartTime) {
								this.startTime = sellGoodInfo.depositStartTime.substring(0, 10)
							}
							if (sellGoodInfo.depositEndTime) {
								this.endTime = sellGoodInfo.depositEndTime.substring(0, 10)
							}
							this.proWegiht = res.data.weight
							this.balancePayTime = sellGoodInfo.balancePayTime
							this.sellNum = sellGoodInfo.sellNum
							this.endDay = sellGoodInfo.endDay
							this.showContent = true
							try {
								let IntroList2 = JSON.parse(listInfo.content || '[]')
							IntroList2 = IntroList2.map((item) => {
								return {
									name: item.name,
									pname: item.name,
									content: item.content.replace(/src/g,
											'style=\'width:100%!important;height: auto!important;\' src'
										)
										.replace(/<table/g, `<table style="width:100%!important;"`)
										.replace(/<img/g, `<img style="width:100%!important;"`)
										.replace(/<div/g, `<p style="width:100%!important;"`)
								};
							});
							this.isGoodsAdd = IntroList2
							if (this.isGoodsAdd.length <= 0) {
								this.isGoodsAdd = [{
									content: '暂无详情',
									pname: "详情"
								}]
							}
							} catch (error) {
								this.isGoodsAdd = [{
									content: (res.data.list && res.data.list.content) || '',
									pname: "详情"
								}]
							}
						this.arrimg = res.data.initial.attrImg
						} else {
							uni.showToast({
								title: (res && res.message) || '加载失败，请稍后重试',
								icon: 'none'
							})
						}
					}).catch(() => {
						uni.hideLoading()
						this.isLoading = false
						uni.showToast({
							title: '网络异常，请稍后重试',
							icon: 'none'
						})
					})
				},

			/**
			 * 预售商品 编辑商品
			 * @param null
			 */
			_setGoods() {
				let data = {
					api: 'plugin.presell.AppPreSell.getGoodsInfoById',
					goodsId: this.goodsId, //商品id
				}
				this.$req.post({
					data
					}).then(res => {
						if (res && res.code == 200 && res.data) {
							const sellGoodInfo = res.data.sellGoodInfo || {}
							const listInfo = res.data.list || {}
							// 国家回显
							this.country_num = res.data.country_num
						this.countrySet = {
							name: res.data.country_name,
							num3: res.data.country_num
						}

						// 语种回显
						this.lang_code = res.data.lang_code
						this.languageSet = {
							lang_name: res.data.lang_name,
							lang_code: res.data.lang_code
						}
						uni.hideLoading();
						this.isLoading = false
						this.video = res.data.video
						this.proVideo = res.data.proVideo
						this.proName = res.data.product_title
						this.vstName = res.data.subtitle
						this.keyWord = (res.data.keyword || '').toString()
						this.chooseClass = res.data.ctypes
						this.proClassId = res.data.class_id //类名id
						this.proBrandId = res.data.brand_id //品牌id
							this.freightSetId = (res.data.freight_list1 && res.data.freight_list1.id) || '' //运费id
							if (res.data.richList) {
								try {
									this.richList = JSON.parse(res.data.richList) //商品详情
								} catch (error) {
									this.richList = []
								}
							}
						this.proBrand = res.data.brand_class_list1
						this.cover_map = res.data.cover_map
						this.showImg = res.data.imgurls
						this.attr_group = res.data.attr_group_list
						this.attr_arr = res.data.checked_attr_list
						this.freightSet = res.data.freight_list1
							this.freightSetId = (res.data.freight_list1 && res.data.freight_list1.id) || ''
						for (var i in res.data.sp_type) {
							if (res.data.sp_type[i].status) {
								this.s_typeShow += ',' + res.data.sp_type[i].name
							}
						}
						this.s_typeShow = this.s_typeShow.replace(',', '')
							this.yushou_type = sellGoodInfo.sell_type
							this.sell_type = sellGoodInfo.sell_type
							this.sellDj = sellGoodInfo.deposit
							if (sellGoodInfo.depositStartTime) {
								this.startTime = sellGoodInfo.depositStartTime
							}
							if (sellGoodInfo.depositEndTime) {
								this.endTime = sellGoodInfo.depositEndTime
							}
							this.proWegiht = res.data.weight || 0
							this.balancePayTime = sellGoodInfo.balancePayTime
							this.sellNum = sellGoodInfo.sellNum
							this.endDay = sellGoodInfo.endDay
							this.showContent = true
							try {
								let IntroList2 = JSON.parse(listInfo.content || '[]')
							IntroList2 = IntroList2.map((item) => {
								return {
									name: item.name,
									pname: item.name,
									content: item.content.replace(/src/g,
											'style=\'width:100%!important;height: auto!important;\' src'
										)
										.replace(/<table/g, `<table style="width:100%!important;"`)
										.replace(/<img/g, `<img style="width:100%!important;"`)
										.replace(/<div/g, `<p style="width:100%!important;"`)
								};
							});
							this.isGoodsAdd = IntroList2
							if (this.isGoodsAdd.length <= 0) {
								this.isGoodsAdd = [{
									content: '暂无详情',
									pname: "详情"
								}]
							}
							} catch (error) {
								this.isGoodsAdd = [{
									content: (res.data.list && res.data.list.content) || '',
									pname: "详情"
								}]
							}
						this.arrimg = res.data.initial.attrImg
						//与查看详情不同的地方
						this.costM = res.data.initial.cbj
						this.oldM = res.data.initial.yj
						this.sellM = res.data.initial.sj
						this.proUnitPickerArray = res.data.unit
						this.unit = res.data.initial.unit
						this.s_type = res.data.sp_type
						this.proFreightPickerArray = res.data.freight_list
						this.loadFlag = true

						//用于 设置属性 回显
						let chooseAttr = ''
						for (let i = 0; i < res.data.attr_group_list.length; i++) {
							chooseAttr += ',' + res.data.attr_group_list[i].attr_group_name
						}
						chooseAttr = chooseAttr.replace(',', '')
						uni.setStorageSync('upload_attr_group', res.data.attr_group_list)
						uni.setStorageSync('upload_attr_arr', res.data.checked_attr_list)
						uni.setStorageSync('upload_chooseAttr', chooseAttr)
						} else {
							uni.showToast({
								title: (res && res.message) || '加载失败，请稍后重试',
								icon: 'none'
							})
						}
					}).catch(() => {
						uni.hideLoading()
						this.isLoading = false
						uni.showToast({
							title: '网络异常，请稍后重试',
							icon: 'none'
						})
					})
				},

			/**
			 * 预售插件 数据处理结束
			 */
		},
		computed: {
			widthArr1: function() {
				return this.widthArr;
			},
			tableList: function() {
				if (this.attr_group.length == 0) {
					var width = 750;
					return uni.upx2px(width) + 'px';
				} else {
					var width = this.tableWidth * 2 + 130 * 5;
					return uni.upx2px(width) + 'px';
				}
			}
		},
		components: {
			mpvuePicker,
			wxParse,
			showToast,
			switchNavThree,
			homeCountry
		}
	};
</script>

<style>
	page {
		background-color: #f4f5f6;
	}
</style>
<style lang="less">
	@import url("@/laike.less");
	@import url('../../static/css/myStore/uploadPro.less');

	@media screen and (min-width: 600px) {
		/deep/ .screen-picker-modal {
			left: 50%;
			height: 100vh;
			transform: translateX(-50%);

			.head {
				width: auto !important;
			}

			.country-selector {
				position: absolute;
				left: 0;
				bottom: 0;
				width: 100vw;
			}
		}
	}

	.attrSpan {
		white-space: nowrap
	}

	.page-loading {
		position: relative;
		/* 其他样式 */
	}

	.page-loading::after {
		content: '';
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: #f6f6f6; // 灰色遮罩层的颜色和透明度
		z-index: 20;
	}

	.submitDiv,
	.jiantouDiv {
		z-index: 99;
	}

	.input-color {
		background-color: #f3f5f7;
	}

	.xnts-btn_danXuan {
		display: flex;
		align-items: center;
		justify-content: flex-end;

		>view:first-child {
			width: 200rpx;
		}

		>view:last-child {
			flex: 1;
			font-size: 24rpx;
			color: #999999;
		}
	}

	.goodsType {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: space-between;

		>view {
			padding: 16rpx 32rpx;
			box-sizing: border-box;
			border-radius: 16rpx;
			border: 2rpx solid #E5E5E5;

			display: flex;
			align-items: center;
			justify-content: space-between;

			>image {
				width: 44rpx;
				height: 44rpx;
				margin-right: 8rpx;
			}

			>span {
				font-size: 32rpx;
				color: #666666;
			}
		}

		.active {
			border: 2rpx solid #FA5151;

			>span {
				color: #333333;
			}
		}
	}

	@media screen and (min-width: 600px) {
		.goodsType {
			>view {
				>span {
					font-size: 28rpx;
				}
			}
		}
	}

	.newClass {
		>span {
			color: #333333;
			font-weight: 500;
		}
	}

	.enents {
		pointer-events: none;
	}

	.ys_goods {
		background-color: #ffffff;
		padding: 32rpx;
		box-sizing: border-box;
		margin-bottom: 16rpx;
		border-radius: 0 0 24rpx 24rpx;
		overflow-y: auto;
	}

	.addGoods {
		width: 178rpx;
		height: 76rpx;
		background: rgba(250, 81, 81, 0.1);
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		color: #D73B48;
		line-height: 36rpx;
		margin-left: auto;
	}

	.tishi {
		font-size: 24rpx;
		color: #999999;
		line-height: 32rpx;
		margin-left: 200rpx;
		margin-top: 16rpx;
	}

	.upImg1 {
		/deep/.uni-video-cover {
			display: none !important;
		}
	}

	.video_dialog {
		position: fixed;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, 0.4);
		z-index: 99;
		display: flex;
		justify-content: center;
		align-items: center;

		/deep/.uni-video-container {
			background-color: rgba(0, 0, 0, 0) !important;
			// top: 440rpx !important;
			height: auto !important;
			z-index: 1000;
		}
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
		width: 70rpx;
		height: 70rpx;
		z-index: 100 !important;
	}

	/deep/uni-video {
		width: 100% !important;
		border-radius: 24rpx;
	}

	.btn_danXuan {
		display: flex;
		align-items: center;
		min-height: 76rpx;

		>div {
			display: flex;
			align-items: center;
			margin-right: 32rpx;

			>span:first-child {
				width: 32rpx;
				height: 32rpx;
				border: 1.5px solid #CCCCCC;
				border-radius: 50%;
				margin-right: 16rpx;
				display: flex;
				align-items: center;
				justify-content: center;

				.btn_danXuan_xuanzhong {
					display: inline-block;
					width: 16rpx;
					height: 16rpx;
					background-color: #CCCCCC;
					border-radius: 50%;
				}
			}

			>span:last-child {
				font-size: 32rpx;
				color: #333333;
			}

			.btn_danXuan_xuanzhong_active {
				border-color: #FA5151 !important;

				>span {
					background-color: #FA5151 !important;
				}
			}
		}
	}
</style>
