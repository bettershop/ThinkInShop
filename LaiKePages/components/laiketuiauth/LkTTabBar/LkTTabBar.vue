<!-- components/CustomTabBar.vue -->
<template>
	<view class="custom-tab-bar" :style="tabBarStyle">
		<!-- 自定义 tabbar 内容 -->    
		<view v-for="(items, index) in tabList" :key="index" class="tab-item"
			:class="{ 'active': Number(currentIndex || 0) === index }" @click="switchTab1(items,index)"> 
			<view class="tab-icon" v-if="tabInfo.iconIsShow" :style="{'width':inconSiz,'height':inconSiz}">
				<image :src="Number(currentIndex || 0) !== index ? items.iconPath : items.selectedIconPath" mode="aspectFit"></image>
				<text class="iconfont" :class="Number(currentIndex || 0) === index ? items.activeIconClass : items.iconClass"></text>
			</view>
			<text v-if="tabInfo.fontIsShow" class="tab-text" :style="{ color: Number(currentIndex || 0) === index ? tabInfo.optColor : tabInfo.color,
            'fontSize':tabInfo.fontSize
        }">
				{{ items.page_name }}  
			</text>
		</view>
	</view>
</template>

<script>
	import {
		mapState
	} from 'vuex'
	export default {
		name: 'LKTTabBar',
		data() {
			return {
				// 当前选中的 tab 索引
				currentIndex: 0,
				// 底部安全区域高度（适配全面屏）
				safeAreaBottom: 0,
				tabList: [],
                tabInfo:{},
				inconSiz: '48rpx'
			};
		},
		props: {
			// 选中项文字颜色
			activeColor: {
				type: String,
				default: '#007AFF'
			},
			// 未选中项文字颜色
			textColor: {
				type: String,
				default: '#999999'
			},
			// 背景颜色
			backgroundColor: {
				type: String,
				default: '#FFFFFF'
			}
		},
		computed: {
			...mapState(['tabBarList', 'tabbarInfo']),
			// 动态样式
			tabBarStyle() {
				return `background-color: ${this.backgroundColor}; padding-bottom: ${this.safeAreaBottom}rpx;`;
			},
		},
		watch: {
			tabbarInfo: {
				handler(newVal) {
					if (Object.keys(newVal).length > 0) {
						this.tabInfo = newVal
					} else {
                        if(uni.getStorageSync('tabbar_info')){
                            this.tabInfo =uni.getStorageSync('tabbar_info')
                        } 
                        console.log(this.tabInfo,'tabbarinfo')
					}
					const iconSiz = {
						'small': '48rpx',
						'medium': '64rpx',
						'big': '80rpx',
					}
					if (this.tabInfo.ficonSize) {
						this.inconSiz = iconSiz[this.tabInfo.ficonSize]
					}
				},
				deep: true,
				
			},
            tabList:{
                handler(newval){
                    console.log('newvalnewvalnewval',newval)
                    console.log('newvalnewvalnewval', this.tabInfo)
                }
            },
			tabBarList: {
				handler(newVal) {    
					 this.tabList =newVal || []  
                     if(uni.getStorageSync('tabbar_info')){
                         this.tabInfo =JSON.parse(uni.getStorageSync('tabbar_info')) 
                     }
                     this.$nextTick(() => {
                     	this.requestH5Translation('tabbar-list-update')
                     })
				},
				deep: true,  
			},
           

		},
		created() { 
            const iconSiz = {
            	'small': '48rpx',
            	'medium': '64rpx',
            	'big': '80rpx',
            } 
			// 获取当前页面路径，设置初始选中项
			this.setCurrentTab(); 
           
            if(uni.getStorageSync('tabbar_info')){
                try{
                    this.tabInfo = JSON.parse(uni.getStorageSync('tabbar_info'))
                }catch(e){
                    this.tabInfo =uni.getStorageSync('tabbar_info') 
                }
            }
            if (this.tabInfo.ficonSize) {
            	this.inconSiz = iconSiz[this.tabInfo.ficonSize]
            } 
			// 获取底部安全区域高度
			this.getSafeArea();
		},  
		mounted() {
           this.setPageIndex();

           const tabArr = JSON.parse(uni.getStorageSync('tabbar')|| '[]')
           this.tabList = tabArr

           // #ifdef H5
           // 组件挂载完成后，走统一翻译调度，确保底部导航文本被翻译
           if (typeof window.__LKT_REQUEST_TRANSLATION__ === 'function') {
               this.$nextTick(() => {
                   window.__LKT_REQUEST_TRANSLATION__('tabbar-mounted');
               });
           }
           // #endif
		},
 
		methods: {
			requestH5Translation(reason = 'tabbar') {
				// #ifdef H5
				if (typeof window.__LKT_REQUEST_TRANSLATION__ === 'function') {
					window.__LKT_REQUEST_TRANSLATION__(reason)
				}
				// #endif
			},
            setPageIndex(){
                const tabberIndex = uni.getStorageSync('tabbarIndex')
                this.currentIndex = Number(tabberIndex || 0) 
            },
			// 设置当前选中的 tab
			setCurrentTab(pagePath) {
				const path = pagePath || this.getCurrentPagePath();
                if(this.tabList && this.tabList.length>0){                    
                    const index = this.tabList.findIndex(item => path.includes(item.pagePath)); 
                    this.currentIndex = index !== -1 ? index : 0;
                }
			},

			// 获取当前页面路径
			getCurrentPagePath() {
				const pages = getCurrentPages();
				const currentPage = pages[pages.length - 1];
				return currentPage.route;
			},

			// 获取底部安全区域高度
			getSafeArea() {
				// #ifdef APP-PLUS
				const safeArea = uni.getSystemInfoSync().safeAreaInsets;
				this.safeAreaBottom = safeArea.bottom * 2; // 转换为 rpx
				// #endif

				// #ifdef H5
				// H5 端适配底部安全区域（可根据实际情况调整）
				const isIPhoneX = /iphone/gi.test(navigator.userAgent) && (screen.height === 812 || screen.height === 896);
				this.safeAreaBottom = isIPhoneX ? 34 : 0;
				// #endif
			},

			// 切换 tab
			switchTab1(item,index) { 
				if (Number(this.currentIndex || 0) == index) {return}; 
                
                uni.setStorageSync('tabbarIndex', index) 
                 
                this.currentIndex = index
				// 触发 tab 切换事件
				this.$emit('tabChange', {
					index,
					item
				});
			 
			}
		}
	};
</script>

<style lang="scss" scoped>
	.custom-tab-bar {
		position: fixed;
		bottom: 0;


		width: 100%;
		height: 120rpx !important;
		box-sizing: border-box;
		/* 确保 padding 和 border 不增加整体高度 */
		padding-bottom: constant(safe-area-inset-bottom);
		/* iOS 11.0-11.2 */
		padding-bottom: env(safe-area-inset-bottom);
		/* iOS 11.2+ */
		display: flex;
		justify-content: space-around;
		align-items: center;
		box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
		z-index: 998;

		.tab-item {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			height: 100%;
			flex: 1;
			cursor: pointer;

			.tab-icon {
				// width: 40rpx;    
				margin-bottom: 4rpx;

				image {
					width: 100%;
					height: 100%;
				}

				text {
					ling-hength: 10rpx
				}
			}

			.tab-text {
				font-size: 22rpx;
				line-height: 13px;
			}
		}
	}
</style>
