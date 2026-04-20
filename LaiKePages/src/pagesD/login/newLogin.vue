<template>
	<div class="login-page" :style="{ backgroundImage: 'url(' + bg + ')' }">
		<div class="login-container">
			<!-- 头部 -->
			<div class="back" @click="goBack">
				<image :src="x" mode="aspectFill"></image>
			</div>
			<div class="login-header">{{ language.newlogin.hydl }}</div>

			<!-- 登录方式切换 -->
			<LoginTab :activeTab="activeTab" @switchTab="switchTab" />

			<!-- 登录表单 -->
			<LoginPhoneForm v-if="activeTab === 'phone'" :phone="phone" :password="password" :code2="code2" :num3="num3"
				@inputPhone="inputPhone" @inputPassword="inputPassword" @clearFrom="clearFrom" />
			<LoginEmailForm v-if="activeTab === 'email'" :email="email" :password="password" @inputEmail="inputEmail"
				@inputPassword="inputPassword" @clearFrom="clearFrom" />
			<LoginAccountForm v-if="activeTab === 'account'" :account="account" :password="password"
				@inputAccount="inputAccount" @inputPassword="inputPassword" @clearFrom="clearFrom" />

			<!-- 登录按钮 -->
			<LoginButton :isReading="isReading" @setReading="setReading" @handleLogin="handleLogin" />

		</div>
		<!-- 社交登录 -->
		<SocialLogin />
		<!-- 提示弹窗 -->
		<!-- <showToast
      :is_showToast="is_showToast" 
      :is_showToast_obj="is_showToast_obj">
    </showToast> -->
	</div>
</template>

<script> 
	import LoginTab from './components/LoginTab.vue';
	import LoginPhoneForm from './components/LoginPhoneForm.vue';
	import LoginEmailForm from './components/LoginEmailForm.vue';
	import LoginAccountForm from './components/LoginAccountForm.vue';
	import LoginButton from './components/LoginButton.vue';
	import SocialLogin from './components/SocialLogin.vue';
	import showToast from "@/components/aComponents/showToast.vue"
	import {
		img
	} from "@/static/js/login/imgList.js";
	import {
		submitLogin
	} from '@/static/js/login/login.js'
	import {
		mapMutations
	} from 'vuex'

	export default {
		components: {
			LoginTab,
			LoginButton,
			LoginPhoneForm,
			LoginEmailForm,
			LoginAccountForm,
			SocialLogin,
			showToast,
		},
		data() {
			return {
				phone: '',
				password: '',
				email: '',
				account: '',
				activeTab: 'phone',
				showAgreement: false,
				isReading: false,
				code2: '',
				num3: '156',
				fatherId: '',
				toHome: false,
				showWxLogin: true,
				showAliLogin: true,
				provider: [],
				baiduHeadTop: 0,
				time_code: '',
				SystemType: '',
				getSystemHeight: 0,
				keyboardHeight: 0,
				x: img(this).x,
				bg: img(this).bg,
			};
		},
		async mounted() {
			await this.getCountey()
		},
		async onLoad(option) {
			// #ifdef H5
			let ua = navigator.userAgent.toLowerCase();
			if (ua.match(/MicroMessenger/i) != "micromessenger") {
				this.showWxLogin = false
			} else {
				this.showAliLogin = false
			}

			// #endif
			uni.removeStorageSync('signFlag');
			// #ifdef MP-BAIDU
			// 百度小程序头部兼容
			uni.getSystemInfo({
				success: (res) => {
					this.baiduHeadTop = res.navigationBarHeight
				}
			})
			// #endif

			// 获取服务供应商
			uni.getProvider({
				service: 'oauth',
				success: res => {
					this.provider = res.provider
				}
			})
			//判断landing_code是否存在，存在则是登录验证未通过进入，登录成功后需要返回上一页
			if (option.landing_code) {
				this.togoodsDetail = option.landing_code
			}
			//获取、绑定上级id
			if (option.fatherId) {
				this.fatherId = option.fatherId
			}
			this.toHome = option.toHome;
			if (uni.getStorageSync('fatherId')) {
				this.fatherId = uni.getStorageSync('fatherId')
			}
			if (!uni.getStorageSync('diqu')) {
				try {
					const defaultData = await this.findCountryByLang();
					if (defaultData) {
						this.code2 = defaultData.code2;
						this.num3 = defaultData.num3;
					} else {
						// 兜底：如果没匹配到，用中国
						this.code2 = '86';
						this.num3 = '156';
					}
				} catch (err) {
					console.warn('自动设置默认国家失败:', err);
					this.code2 = '86';
					this.num3 = '156';
				}
			}
		},
		created() {
			//获取 终端初始可用屏幕高度
			this.getSystemHeight = uni.getStorageSync('getSystemHeight').value
			//获取 操作系统类型
			this.SystemType = uni.getStorageSync('getSystemType').value
			//获取 键盘高度
			this.keyboardHeight = uni.getStorageSync('keyboardHeight') ? uni.getStorageSync('keyboardHeight').value : 0
			// #ifdef H5
			if (this.SystemType == 'Android-H5') {
				//Android键盘弹起会触发页面的resize 事件（ios不会）
				window.addEventListener('resize', () => {
					uni.getSystemInfo({
						success: (res) => {
							if (res.windowHeight < this.getSystemHeight) {
								this.keyboardHeight = this.getSystemHeight - res.windowHeight
								uni.setStorageSync('keyboardHeight', {
									name: '手机键盘高度',
									value: this.keyboardHeight
								})
							}
						}
					})
				})
			}
			// #endif
		},
		onUnload() {
			//很多地方都要用到用户手机号，登陆不一定会跳转个人中心页面。分享进入登陆后会返回分享页面，就没办法获取手机号
			//登陆接口为什么不传手机号？得重新调个人中心的app.user.index接口？这样好多地方都要调这样的接口。
			//只能在登陆页面关闭前调用这个接口，并存缓存
			let data = {
				api: 'app.user.index',};
				this.$req.post({
					data
				}).then(res => {
					if (res && res.code == 200) {
						if (res.data && res.data.data && res.data.data.user) {
							this.$store.state.user_phone = res.data.data.user.mobile;
							uni.setStorage({
								key: 'user_phone',
								data: res.data.data.user.mobile
							});
						}

					}
				}).catch(() => {})
				uni.removeStorageSync('diqu')
			},
		onShow() {
			this.time_code = this.language.login.page2.getCode
			if (uni.getStorageSync('diqu')) {
				let diqu = JSON.parse(uni.getStorageSync('diqu'))
				this.code2 = diqu?.code2
				this.num3 = diqu?.num3
			}

		},
		methods: {
			...mapMutations({
				set_access_id: 'SET_ACCESS_ID',
				user_phone: 'SET_USER_PHONE'
			}),
			// 根据语言从 regionAndCountry 中查找默认国家数据
			async findCountryByLang() {
				// 先尝试从缓存读取 regionAndCountry
				let regionAndCountry = uni.getStorageSync('regionAndCountry');
				if (!regionAndCountry) {
					await this.getCountey();
					regionAndCountry = uni.getStorageSync('regionAndCountry');
				}
				if (!regionAndCountry) return null;

				const countryList = JSON.parse(regionAndCountry);

				// >>> 【关键修改】优先使用缓存中的 language（来自 URL 或用户选择）<<<
				let langKey = uni.getStorageSync("language") || 'zh_CN'; // 如 ja_JP, en_US

				// 将 langKey 转为类似 'ja-jp' 或 'en-us' 的格式，便于提取国家
				let langForCountry = langKey.toLowerCase().replace('_', '-');

				// 提取国家代码（如 ja_JP → JP）
				let countryCode = null;
				const parts = langForCountry.split('-');
				if (parts.length > 1) {
					const region = parts[1].toUpperCase();
					if (region.length === 2 && isNaN(region)) {
						countryCode = region;
					}
				}

				// 如果没从 langKey 提取到（比如 zh_CN 可能无明确国家），再用语言前缀映射
				if (!countryCode) {
					const langPrefix = parts[0]; // 如 'zh', 'en', 'ja'

					// 映射逻辑（可复用）
					const prefixToCountry = {
						// 东南亚
						'th': 'TH',
						'vi': 'VN',
						'id': 'ID',
						'km': 'KH',
						'lo': 'LA',
						'my': 'MM',
						'fil': 'PH',
						'tl': 'PH',
						'ms': 'MY',
						// 东亚
						'ja': 'JP',
						'ko': 'KR',
						'zh': 'CN', // 注意：zh 默认 CN，若需区分 TW/HK 需额外处理
						// 南亚
						'hi': 'IN',
						'bn': 'IN',
						'ta': 'IN',
						'te': 'IN',
						'mr': 'IN',
						'gu': 'IN',
						// 中东/中亚
						'ar': 'SA',
						'fa': 'IR',
						'he': 'IL',
						'iw': 'IL',
						'tr': 'TR',
						'ru': 'RU',
						// 欧美
						'pt': 'BR', // 葡语优先巴西（更常见）
						'es': 'ES',
						'fr': 'FR',
						'de': 'DE',
						'it': 'IT',
						'nl': 'NL',
						'pl': 'PL',
						'sv': 'SE',
						'da': 'DK',
						'fi': 'FI',
						'no': 'NO',
						'en': 'US'
					};

					countryCode = prefixToCountry[langPrefix] || 'US';
				}

				// 查找匹配的国家
				for (const group in countryList) {
					const list = countryList[group];
					const match = list.find(item => item.code === countryCode);
					if (match) {
						return {
							code2: match.code2,
							num3: match.num3
						};
					}
				}

				// 兜底：中国
				return {
					code2: '86',
					num3: '156'
				};
			},
			async getCountey() {
				if (uni.getStorageSync('regionAndCountry')) {
					return
				}
				const data = {
					api: 'app.user.getItuList',
					keyword: ''
				}
				await this.$req.post({
					data
				}).then(res => {
					if (res.code == 200) {
						const list = res.data
						// 排序并分类
						const classifiedWords = list.sort().reduce((acc, word) => {
							// 获取单词的首字母（大写）
							let firstLetter = word.name.charAt(0).toUpperCase();
							// 如果 acc 中还没有这个首字母的数组，则创建一个新的空数组
							if (!acc[firstLetter]) {
								this.$set(acc, firstLetter, [])
							}
							// 将当前单词添加到对应首字母的数组中
							acc[firstLetter].push(word);
							return acc;
						}, {});
						if (!this.selectValue) {
							uni.setStorageSync('regionAndCountry', JSON.stringify(classifiedWords))
						}
					} else {
						uni.showToast({
							title: res.message,
							duration: 1000,
							icon: 'none'
						})
					}
				})
			},
			// 触发登录逻辑
			async handleLogin() {
				submitLogin(this)
			},
			// 选中协议
			setReading(e) {
				this.isReading = e;
			},
			// 跳转
			goBack() {
				const pages = getCurrentPages();
				try {
					// 未登录 预览直播中心时
					if (pages[pages.length - 2].route === 'pagesD/liveStreaming/liveRecommended') {
						uni.navigateBack({
							delta: 2
						});
						return
					}
					// 判断页面栈深度
					if (pages.length >= 1) {
						// 正常返回上一页
						uni.navigateBack({
							delta: 1
						});
					} else {
						uni.reLaunch({
							url: '/pages/shell/shell?pageType=my',
						});
					}
				} catch (e) {
					uni.reLaunch({
						url: '/pages/shell/shell?pageType=my',
					});
				}
			},
			// 切换登录方式
			switchTab(tab) {
				this.activeTab = tab;
				this.password = ''
				this.clearFrom()
			},
			// 输入手机号
			inputPhone(e) {
				this.phone = e
			},
			//输入邮箱
			inputEmail(e) {
				this.email = e
			},
			//输入账号
			inputAccount(e) {
				this.account = e
			},
			// 输入密码
			inputPassword(e) {
				this.password = e
			},
			// 清空输入框
			clearFrom() {
				this.phone = ''
				this.email = ''
				this.account = ''
			},
		}
	};
</script>

<style scoped lang="less">
	.login-page {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		background-size: cover;
		background-repeat: no-repeat;
		background-position: center;
		min-height: 100vh;
		width: 100%;
	}

	.login-container {
		// display: flex;
		// flex-direction: column;
		padding: 20px;
		background-color: #FFFFFF;

		.login-header {
			margin: 72rpx 0;
			font-weight: 500;
			font-size: 48rpx;
			color: #333333;

		}

		.back {
			/* #ifdef MP-WEIXIN */
			margin: 60rpx 0 20rpx 0;
			/* #endif */
			font-weight: 500;
			font-size: 48rpx;
			color: #333333;

			image {
				width: 24rpx;
				height: 24rpx;
			}
		}
	}
</style>
