<template>
	<view class="mch" v-if="r_mch.length"
		:style="{background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`, marginTop: pxToRpxNum(mbConfig) + 'rpx',}">
		<view class="home_title">
			<view :style="{color: txtColor}">{{title}}</view>
			<image :src="LaiKeTuiCommon.getFastImg(titleBg, 222, 38)" lazy-load="true"></image>
			<view class="more" @tap="toUrl('/pagesB/home/mchList')">
                 {{language.home.more}}  
				<image :src="jiantou" lazy-load="true"></image>
			</view> 
		</view>

		<scroll-view scroll-x="true" class="mch_content" >
			<view class="content_newFour_content" v-for="(item, index) of r_mch" :key="index" 
                    @tap=" _seeMore(
			            '/pagesB/store/store?shop_id=' +
			                item.shop_id
			        ) "
                     :style="{ marginRight: pxToRpxNum(listRight) + 'rpx'}">
				<view>
					<image :src="LaiKeTuiCommon.getFastImg(!item.backImgUrl ? default_img : item.backImgUrl, 200, 200)"
                        lazy-load="true" mode="aspectFill" @error="imgError(index)">
					</image>
				</view>
				<view>
					<view>
						<image :src="LaiKeTuiCommon.getFastImg(!item.headimgurl ? default_img : item.headimgurl, 64, 64)"
                            lazy-load="true" mode="aspectFill" @error="imgHeadError(index)">
                         </image>
					</view>
				</view>
				<view>{{ item.name }}</view>
				<view class="content_newFour_btn" @tap.stop="
			            _collectMch(
			                item.shop_id,
			                item.collection_status
			            )
			        ">
					<view v-if="!item.collection_status">{{
			            language.home.gz
			        }}</view>
					<view v-else class="yiguanzhu newBgcolor">{{
			            language.home.ygz
			        }}</view>
				</view>
				<!-- 定位图标 -->
				<div class="live_icon" v-if="item.livingStatus&&item.mch_is_open==1">
					<!-- <img :src="live" alt=""> -->
					<i class="one"></i>
					<i class="two"></i>
					<i class="three"></i>
				</div>
			</view> 
		</scroll-view> 
	</view>
</template>

<script>
	export default {
		name: "homeStore",
		props: {
			dataConfig: {
				type: Object,
				default: () => {}
			}, 
		},
		watch: {
            
			dataConfig(newVal) {
                if(newVal && Object.keys(newVal).length>0){
                    
                    this.bgColor = newVal.bgColor.color
                    this.numberConfig = newVal.numberConfig.val
                    this.title = newVal.titleConfig.value
                    this.txtColor = newVal.titleTxtColor.color[0].item
                    this.titleBg = newVal.imgConfig.url
                    this.lrConfig = newVal.lrConfig.val
                    this.mbConfig = newVal.mbConfig.val
                    this.listRight =newVal.lrConfig.val;
                }
			}
		},
		data() {
			return {
				home_title_bg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_title_bg.png',
				r_mch: [],
				numberConfig: this.dataConfig.numberConfig.val,
				bgColor: [{
						item: '#FFFFFF'
					},
					{
						item: '#FFFFFF'
					}
				],
				title: this.dataConfig.titleConfig.value,
				txtColor: this.dataConfig.titleTxtColor.color[0].item,
				titleBg: this.dataConfig.imgConfig.url,
				lrConfig: this.dataConfig.lrConfig.val,
				mbConfig: this.dataConfig.mbConfig.val,
				jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou2x.png',
				default_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                listRight:""
			}
		},
		created() {
			this.setLang();
		},
		mounted() {
              let num = ''
            if( this.dataConfig && Object.keys( this.dataConfig).length>0)
            { 
                this.listRight =this.dataConfig.lrConfig.val;
                this.bgColor = this.dataConfig.bgColor.color
                 num = this.dataConfig.numberConfig.val
            }
			this.axios(num)
		},

		methods: {
            // 图片失效时
            imgError(index){  
                this.r_mch[index].backImgUrl =this.default_img  // 替换为默认图片  
            }, 
            imgHeadError(index){
                this.r_mch[index].headimgurl =this.default_img  // 替换为默认图片  
            },
			axios(limitNum = 3) { 
                let data ={
                    api: 'app.index.getMchList',
                    limit_num:limitNum 
                }
				this.$req.post({
					data
				}).then(res => {
					if (res.code == 200) {
						this.r_mch = res.data.r_mch
					} else {
						uni.showToast({
							title: res.message,
							icon: 'none'
						})
					}
				})
			},
			//查看更多
			_seeMore(url) {
				uni.navigateTo({
					url: url,
				});
			},
		 
			////店铺关注/取关
			_collectMch(id, state) { 
                this.isLogin(() => { 
                    let data ={
                			api: "mch.App.Mch.Collection_shop",
                			shop_id: id
                		}
                	this.$req.post({data}).then(res => {
                			const text = state ? this.language.order.myorder.cancel_success : this
                				.language.goods.goodsDet.gzcg
                            uni.showToast({
                                title:text,
                                icon:'none'
                            })
                            console.log(res)
                			if (res.code == 200) {
                				this.axios()
                			}
                		})
                		.catch(error => {
                			console.log(error)
                		});
                }) 
			},
			toUrl(url) { 
                uni.navigateTo({
                    url
                }); 
			}
		}
	}
</script>

<style scoped lang="less">
	.mch {
		padding-top: 10rpx;
		padding-bottom: 10rpx;
		width: 100% !important;
		padding: 12rpx  0rpx 10rpx !important;
		border-radius: 0 !important; 
	}

	.home_title {
		position: relative;
		z-index: 10;
		margin: 0rpx 30rpx 0;
		display: flex;
		justify-content: space-between;
        margin-bottom: 22rpx; 
	}

	.home_title>view {
        font-family: Source Han Sans, Source Han Sans;
        
		font-size: 32rpx;
		line-height: 32rpx; 
		// font-weight: bold; 
        font-weight: 500; 
        color: #333333; 
	}

	.home_title>image {
		position: absolute;
		width: 111rpx;
		height: 19rpx;
		top: 20rpx;
		left: 24rpx;
		z-index: -1;
	}

	.more {
		display: flex;
		align-items: center;
		color: #999999 !important;
		font-size: 26rpx !important;
		margin-left: auto;
		height: 36rpx;
        font-family: Source Han Sans, Source Han Sans;
        font-weight: 500 !important;
        line-height: 34rpx;
	}

	.more image {
		width: 12rpx;
		height: 22rpx;
		margin-left: 12rpx;
	}

	.mch_content {
		/*display: flex;*/ 
		margin: 32rpx;
		width: 688rpx;
        margin-bottom: 0px;
        margin-top: 0px;
        /deep/ .uni-scroll-view-content {
            overflow-y: overlay;
            white-space: nowrap;
        }
	}

	.mch_content_item {
		display: flex;
		flex-direction: column;
		width: 216rpx;
		height: 316rpx;
		background: rgba(255, 255, 255, 1);
		border-radius: 10rpx;
		margin-right: 20rpx;
	}

	.mch_content_item>image {
		width: 216rpx;
		height: 216rpx;
		border-radius: 10rpx 10rpx 0rpx 0rpx;
	}

	.mch_content_item>view {
		font-size: 24rpx;
		line-height: 24rpx;
		color: #000000;
		margin: 20rpx 14rpx 18rpx;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.mch_content_item>text {
		color: #999999;
		font-size: 24rpx;
		line-height: 20rpx;
		margin: 0 14rpx;
	}

	.content_newFour_content {
		width: 200rpx;
		height: auto;
		background-color: #ffffff;
		border-radius: 16rpx;
		overflow: hidden;
		padding-bottom: 16rpx;
		box-sizing: border-box;
		margin-right: 16rpx;
		//滚动
		display: inline-block;
		position: relative;

		>view:nth-child(1) {
			width: 200rpx;
			height: 200rpx;
			border-radius: 16rpx;
			overflow: hidden;

			>image {
				width: 100%;
				height: 100%;
			}
		}

		>view:nth-child(2) {
			display: flex;
			align-content: center;
			justify-content: center;

			>view {
				width: 64rpx;
				height: 64rpx;
				border-radius: 64rpx;
				overflow: hidden;
				border: 1px solid rgba(0, 0, 0, 0.1);
				margin-top: -32rpx;
				position: relative;
				z-index: 1;

				>image {
					width: 100%;
					height: 100%;
				}
			}
		}

		>view:nth-child(3) {
			font-size: 24rpx;
			font-weight: 500;
			color: rgba(0, 0, 0, 0.85);
			line-height: 34rpx;
			text-align: center;
			margin: 8rpx auto;
			// 超出隐藏
			width: 180rpx;
			height: auto;
			display: block;
			overflow: hidden;
			/*超出隐藏*/
			text-overflow: ellipsis;
			/*隐藏后添加省略号*/
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 1; //想显示多少行
		}

		.live_icon {
			flex: none;
			position: absolute;
			width: 28rpx;
			height: 28rpx;
			top: 8rpx !important;
			left: 8rpx !important;
			border-radius: 50%;
			background-color: #fa5151;
			box-sizing: border-box;
			overflow: hidden;

			i {
				width: 4rpx;
				height: 12rpx;
				background-color: #ffffff;
				position: absolute;
				bottom: 5rpx;
				border-radius: 1.5rpx;
				left: 0;
			}

			.one {
				left: 4rpx;
				width: 4rpx;
				/* 初始宽度 */
				height: 12rpx;
				/* 高度 */
				background-color: #ffffff;
				/* 背景颜色 */
				animation: stretch 0.8s infinite alternate;
				/* 应用动画 */

				@keyframes stretch {
					0% {
						height: 12rpx;
						/* 动画开始时的宽度 */
					}

					100% {
						height: 18rpx;
						/* 动画结束时的宽度 */
					}
				}
			}

			.two {
				left: 12rpx;
				width: 4rpx;
				/* 初始宽度 */
				height: 12rpx;
				/* 高度 */
				background-color: #ffffff;
				/* 背景颜色 */
				animation: stretch 0.2s infinite alternate;
				/* 应用动画 */

				@keyframes stretch {
					0% {
						height: 12rpx;
						/* 动画开始时的宽度 */
					}

					100% {
						height: 18rpx;
						/* 动画结束时的宽度 */
					}
				}
			}

			.three {
				left: 20rpx;
				width: 4rpx;
				/* 初始宽度 */
				height: 12rpx;
				/* 高度 */
				background-color: #ffffff;
				/* 背景颜色 */
				animation: stretch 0.6s infinite alternate;
				/* 应用动画 */

				@keyframes stretch {
					0% {
						height: 12rpx;
						/* 动画开始时的宽度 */
					}

					100% {
						height: 18rpx;
						/* 动画结束时的宽度 */
					}
				}
			}

			// img {
			//     width: 28rpx;
			//     height: 28rpx;
			//     vertical-align: middle;
			// }
		}

		.content_newFour_btn {
			display: flex;
			justify-content: center;
			align-items: center;

			>view {
				width: 96rpx;
				height: 48rpx;
				background: rgba(250, 81, 81, 0.1);
				border-radius: 24rpx;
				font-size: 24rpx;
				font-weight: 500;
				color: #fa5151;
				line-height: 48rpx;
				text-align: center;
			}

			.yiguanzhu {
				font-weight: 400 !important;
				color: #999999 !important;
			}
		}

		.newBgcolor {
			background: rgba(244, 245, 246, 1) !important;
		}
	}
</style>
