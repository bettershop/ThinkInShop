<template>
	<view class="pagesx" style="background-color: initial;">
		<view class="input">
			<!-- 消息 -->
			<view class="input_box">
				<!-- 输入框 -->
				<view class="input_box_input" @tap="_fatherClick">
					<input v-if="!isDisabled" type="text" class="input_box_input_input" adjust-position="false"
						v-model="input_content" :disabled="isDisabled" :focus="isshow" @tap.stop @focus="_focus"
						@blur="_blur" @confirm="handleInput">
					<template v-else>
						<view class="inputCP">{{input_content}} </view>
					</template>
				</view>
				<template v-if="SystemType == 'PC-H5'">
					<!-- 笑脸 -->
					<image :src="biaoqing" class="input_box_img" @mousedown="expression(iconFalg)"></image>
					<!-- 加号 -->
					<image :src="jiahao" class="input_box_img" @mousedown="photo(configFalg)"></image>
				</template>
				<template v-else>
					<!-- 笑脸 -->
					<image :src="biaoqing" class="input_box_img" @touchstart="expression(iconFalg)"></image>
					<!-- 加号 -->
					<image :src="jiahao" class="input_box_img" @touchstart="photo(configFalg)"></image>
				</template>
			</view>
			<!-- 拍照/相册 -->
           
			<view class="photo_box" :style="{height:JSON.parse(otherHeight)>200?otherHeight+'px':'350px'}"
				v-if="is_photobox||is_expressionbox">
				<template v-if="is_photobox">
					<!-- 拍照 -->
					<view class="photo_box_one" @click="photograph">
						<image :src="xiangji" class="photo_box_one_img"></image>
						<view>{{language.InputWidgets.pz}}</view>
					</view>
					<!-- 相册 -->
					<view class="photo_box_one" @click="Photoclick">
						<image :src="xiangce" class="photo_box_one_img"></image>
						<view>{{language.InputWidgets.xc}}</view>
					</view>
					<!-- 订单 -->
					<view class="photo_box_one" @click="selecOrderListBymchId" v-if="is_store ==0">
						<image :src="order" class="photo_box_one_img"></image>
						<view>{{language.InputWidgets.dd}}</view>
					</view>
				</template>
				<template v-if="is_expressionbox">
					<!-- 表情 -->
					<swiper class="sliders" :current="0">
						<swiper-item class="slider-emoji" v-for="(item, key) in emojiData" :key="key">
							<view class="slider-emoji-icon" v-for="(emoji, index) in item" :key="index"
								@click="selemoji(emoji)">
								{{emoji}}
							</view>
						</swiper-item>
					</swiper>
				</template>
			</view>
		</view>
	</view>
</template>

<script>
	import emoji from "@/js_sdk/m-emoji/m-emoji/emoji.js"
	import {
		LaiKeTui_chooseImg3,
		getImageList
	} from "@/pagesA/myStore/myStore/uploadPro.js";
	export default {
		data() {
			return {
				xiangce: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xiangce.png',
				order: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/3515.png',
				xiangji: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xiangji.png',
				biaoqing: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/biaoqing.png',
				jiahao: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiahao.png',
				input_content: '',
				emojiData: [],
				cover_mapa: '',
				text_num: 0,
				isshow: false,
				keyboardShow: false, //键盘是否开启
				SystemType: '', //获取操作系统类型（PC-H5/iOS-H5/Android-H5/iOS-MP/Android-MP/iOS-APP/Android-APP）
				isDisabled: true, //是否禁用输入框
				iconFalg: false,
				configFalg: false
			}
		},
		props: {
			//输入内容
			"content": {
				type: String,
				require: true
			},
			//相机开关
			"is_photobox": {
				type: Boolean,
				require: true
			},
			//表情开关
			"is_expressionbox": {
				type: Boolean,
				require: true
			},
			//计算高度
			"otherHeight": {
				type: Number,
				require: 0
			},
			//是否店铺
			"is_store": {
				type: Number,
				require: true
			},
		},
		watch: {
			isshow(e) {
				console.log('isshow是否聚焦～', e);
			},
			otherHeight(e) {
				console.log('otherHeight键盘高度～', e);
			},
			//解决选择标签后input没获取聚焦问题
			is_expressionbox(e) {
				console.log('是否显示表情～', e);
				// this.isshow = !this.isshow
				if (e) {
					this.isshow = false
				}
			},
			is_photobox(e) {
				console.log('是否显示相册～', e);
				if (e) {
					this.isshow = false
				}
			},
		},
		created() {
			console.log('子组件~created')
			//获取 操作系统类型
			this.SystemType = uni.getStorageSync('getSystemType').value
		},
		mounted() {
			console.log('子组件mounted事件～');
			//iOS-H5另外处理
			if (this.SystemType == 'iOS-H5' || this.SystemType == 'iOS-APP') {
				this.isDisabled = false
			}
			//表情 分页处理
			var page = Math.ceil(emoji.length / 45);
			for (let i = 0; i < page; i++) {
				this.emojiData[i] = [];
				for (let k = 0; k < 45; k++) {
					this.emojiData[i].push(emoji[i * 45 + k])
				}
			}
		},
		methods: {
			/**
			 * 逻辑梳理
			 * 1.input禁用不显示，让上级标签可以触发点击事件;
			 * 2.上级标签触发点击事件时，显示占位弹窗，并启用input.
			 * 3.通知父组件，计算聊天内容高度，需要减去占位弹窗高度（键盘高度）;
			 * 4.父组件通过refs动态让input聚焦.
			 * 5.input失去焦点时，input禁用不显示。
			 */
			_fatherClick() {
				console.log('触发input上级标签点击事件～通知父组件，计算聊天内容高度')
				if (this.SystemType == 'PC-H5') {
					this.keyboardShow = false
				} else {
					//显示占位弹窗
					this.keyboardShow = true
				}
				//启用input
				this.isDisabled = false
				//通知父组件，计算聊天内容高度
				this.$emit('_fatherClick')
			},
			//获取焦点
			_focus(event) {
				console.log('focus事件～通知父组件，计算聊天内容高度', event);
				console.log('我是谁', this.SystemType);
				this.isshow = true
				//iOS-H5另外处理
				if (this.SystemType == 'iOS-H5' || this.SystemType == 'iOS-APP') {
					return
				}
				//计算键盘高度
				let keyboardHeight = event.detail.height ? event.detail.height : this.otherHeight
				//把键盘高度存缓存
				if (event.detail.height) {
					uni.setStorageSync('keyboardHeight', {
						name: '手机键盘高度',
						value: this.keyboardHeight
					})
				}
				//通知父组件，计算聊天内容高度
				this.$emit('_focus', keyboardHeight)
			},
			//失去焦点
			_blur() {
				console.log('blur事件～通知父组件，计算聊天内容高度');
				this.isshow = false
				//iOS-H5另外处理
				if (this.SystemType == 'iOS-H5' || this.SystemType == 'iOS-APP') {
					return
				}
				//禁用input
				this.isDisabled = true
                
                if(this.iconFalg || this.configFalg){
                    return
                }
				//隐藏占位弹窗
				this.keyboardShow = false
				//通知父组件，计算聊天内容高度
				this.$emit('_blur')
			},
			//确认发送
			handleInput(e) {
				this.$emit('change', this.input_content)
				this.input_content = ''
			},
			//选择表情
			selemoji(m) {
				this.input_content = this.input_content.concat(m)
				//this.isshow = true
			},
			//相册
			Photoclick() {
				console.log('点击了相册');
				// 选择图片
				let me = this
				uni.chooseImage({
					count: 9,
					success: async (res) => {
						let list = await getImageList(res.tempFilePaths, me)
						me.cover_mapa = list
						let lista = me.cover_mapa
						this.$emit('imgs', lista)
					}
				})
			},
			// 查询当前 店铺的订单
			selecOrderListBymchId() {
				this.$emit('queryOrderList')
			},
			//拍照
			photograph() {
				console.log('点击了拍照');
				// 选择图片
				let me = this
				uni.chooseImage({
					count: 1,
                     sizeType: ['original', 'compressed'], // 可选择原图或压缩图
                    sourceType: ['camera'] ,
					success: async (res) => {
						let list = await getImageList(res.tempFilePaths, me)
						console.log(list);
						me.cover_mapa = list
						let lista = me.cover_mapa
						this.$emit('imgs', lista)
					}
				})
				// this.$emit('img',this.cover_mapa)
			},
			//上传图片
			getImageList(tempFilePaths, me) {
				let list = tempFilePaths
				uni.showLoading({
					title: me.language.showLoading.upLoading,
					mask: true
				})
				for (let key of list.keys()) {
					let res = me.$req.upLoad(list[key])
					console.log(res);
					list[key] = res.data
				}
				uni.hideLoading()
				return Promise.resolve(list);
			},
			//点击笑脸
			expression(e) {
				this.configFalg = false
				this.iconFalg = !this.iconFalg
				console.log('点击了笑脸1', this.iconFalg);
				this.isshow = false
				if (this.iconFalg) { 
					this.$emit('is_photobox', JSON.parse(this.otherHeight) > 200 ? this.otherHeight : 350)
                    
				} else { 
					this.$emit('is_photobox', 0)
				}
			},
			//点击加号
			photo(e) {
				this.configFalg = !this.configFalg
				this.iconFalg = false
				console.log('点击了加号');
				this.isshow = false
				if (this.configFalg) {
					this.$emit('is_expressionbox', JSON.parse(this.otherHeight) > 200 ? this.otherHeight : 350)
				} else {
					this.$emit('is_expressionbox', 0)
				}
			},
		}
	}
</script>

<style scoped >
    .photo_box {
        display: flex;
        border-top: 1rpx solid #eee;
        align-items: baseline;
    	width: 100%; 
    	height: 250px; /* 此处必须用px，用于固定高度,不然聊天内容高度会乱 */
    }
    
	@media screen and (min-width: 600px) {
		.pagesx {
			width: 375px !important
		}
	}

	@media screen and (min-width: 600px) {
		.pagesx {
			transform: translateX(-50%);
			margin-left: 50% !important;
			max-width: 375px;
		}
	}

	.sliders {
		width: 750rpx;
		height: 500rpx !important;
		display: flex;
	}

	.slider-emoji {
		width: 100%;
		display: flex;
		flex-direction: row;
		flex-wrap: wrap;
		/* justify-content: center; */
	}

	.slider-emoji-icon {
		/* 此处必须使用固定宽高。不然不同浏览器之间不同尺寸都会有问题。 */
		width: 42rpx;
		height: 52rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 20rpx;
	}

	/* 设置最后一列左靠齐 */
	.lastbox {
		justify-content: flex-start;
	}

	.pagesx {
		width: 100%;
		background-color: #F4F5F6;
		overflow: hidden;
		border-radius: 24rpx 24rpx 0 0;
		/* 兼容 iOS < 11.2 */
		padding-bottom: constant(safe-area-inset-bottom);
		padding-bottom: env(safe-area-inset-bottom);
	}

	.photo_box_one_img {
		width: 72rpx;
		height: 72rpx;
	}

	.photo_box_one {
		width: 72rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-top: 36rpx;
		margin-left: 42rpx; 
	}  
    .photo_box_one view {
        font-size: 24rpx;

        font-weight: 400;
        color: #999999;
        margin-top: 12rpx;
    }
	.input_box_input {
		width: 550rpx;
		height: 64rpx;
		background: #FFFFFF;
		border-radius: 32rpx 32rpx 32rpx 32rpx;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.input_box_input_input {
		width: 510rpx;
		height: 64rpx;
		padding-left: 20rpx;
		padding-right: 20rpx;
	}

	.input_box {
		width: 100%;
		display: flex;
		height: 52px; /* 此处必须用px，用于固定高度，不然聊天内容高度会乱 */
		justify-content: center;
		align-items: center;
	}

	.input {
		width: 100%;
		display: flex;
		flex-direction: column;
		background: #F4F5F6;
	}

	.input_box_img {
		width: 48rpx;
		height: 48rpx;
		margin-left: 24rpx;
	}

	.inputCP {
		flex: 1;
		padding: 0 20rpx;
		box-sizing: border-box;
		white-space: nowrap;
		overflow: hidden;
	}
</style>
