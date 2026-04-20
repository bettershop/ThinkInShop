import Vue from "vue";
import store from "./store";

import App from "./App";
App.mpType = 'app'

import laiketuiComm from "./components/laiketuiCommon.js";
Vue.prototype.LaiKeTuiCommon = laiketuiComm;

// 统一导入语言配置
import {
	normalizeLangCode,
	deviceLangToCode,
	externalLangMap,
	getLangModule
} from "./common/lang/config.js";

// #ifdef H5
window.__LKT_TRANSLATED__ = false;
window.__LKT_TRANSLATE_IN_PROGRESS__ = false;
window.__LKT_TRANSLATE_NEED_RERUN__ = false;
window.__LKT_TRANSLATE_TIMER__ = null;
Vue.prototype.$isTranslated = false;
import translate from '@my-miniprogram/src/lib/translate.js'

//使用 normalizeLangCode 解析 URL 语言
const getUrlLang = (() => {
	const params = new URLSearchParams(window.location.search || '');
	const raw = params.get('language') || params.get('lang');
	if (!raw) return null;
	// 直接标准化
	return normalizeLangCode(raw);
})();

// 本地源语言应固定为中文，目标语言由 changeLanguage 控制
translate.language.setLocal('chinese_simplified')
translate.visual.webPageLoadTranslateBeforeHiddenText()
translate.listener.start()

const markTranslationDone = () => {
	window.__LKT_TRANSLATED__ = true;
	Vue.prototype.$isTranslated = true;
	setPageTitle();
	window.dispatchEvent(new CustomEvent('translationDone'));
};

window.__LKT_MARK_TRANSLATION_DONE__ = markTranslationDone;

const nextFrame = (callback) => {
	if (typeof window.requestAnimationFrame === 'function') {
		window.requestAnimationFrame(callback);
		return;
	}
	setTimeout(callback, 16);
};

window.__LKT_REQUEST_TRANSLATION__ = function(reason = 'manual') {
	if (window.__LKT_TRANSLATE_IN_PROGRESS__) {
		window.__LKT_TRANSLATE_NEED_RERUN__ = true;
		return;
	}
	if (window.__LKT_TRANSLATE_TIMER__) {
		clearTimeout(window.__LKT_TRANSLATE_TIMER__);
	}
	window.__LKT_TRANSLATE_TIMER__ = setTimeout(() => {
		window.__LKT_TRANSLATE_IN_PROGRESS__ = true;
		window.__LKT_TRANSLATE_TIMER__ = null;

		const lang = normalizeLangCode(uni.getStorageSync('language') || getUrlLang || 'zh_CN') || 'zh_CN';
		const targetLang = externalLangMap[lang] || 'chinese_simplified';
		const shouldSwitchLanguage = lang !== 'zh_CN' || uni.getStorageSync('setLangFlag');

		try {
			if (shouldSwitchLanguage) {
				translate.changeLanguage(targetLang);
			}
		} catch (e) {
			console.error(`[translate][${reason}] changeLanguage failed:`, e);
		}

		const executeOnce = () => {
			try {
				translate.execute();
			} catch (e) {
				console.error(`[translate][${reason}] execute failed:`, e);
			}
		};

		const finalize = () => {
			uni.removeStorageSync('setLangFlag');
			window.__LKT_TRANSLATE_IN_PROGRESS__ = false;
			markTranslationDone();
			if (window.__LKT_TRANSLATE_NEED_RERUN__) {
				window.__LKT_TRANSLATE_NEED_RERUN__ = false;
				window.__LKT_REQUEST_TRANSLATION__('rerun');
			}
		};

		nextFrame(() => {
			executeOnce();
			setTimeout(executeOnce, 220);
			setTimeout(() => {
				executeOnce();
				finalize();
			}, 760);
		});
	}, 40);
};

// 翻译完成事件
window.addEventListener('translationDone', () => {
	window.__LKT_TRANSLATED__ = true
	Vue.prototype.$isTranslated = true
	// 翻译完成后重新设置页面标题，确保导航栏标题能够被翻译
	setPageTitle()
})

// H5 初始化触发
setTimeout(() => {
	const shouldTranslate = uni.getStorageSync('setLangFlag')
	const lang = normalizeLangCode(uni.getStorageSync('language') || getUrlLang || 'zh_CN') || 'zh_CN'
	if (shouldTranslate || lang !== 'zh_CN') {
		window.__LKT_REQUEST_TRANSLATION__('bootstrap')
	} else if (!window.__LKT_TRANSLATED__) {
		markTranslationDone()
	}
}, 80)
// #endif


import lktauthorize from "./components/lktauthorize.vue";
import load from "./components/toload.vue";
import heads from "./components/header.vue";
import uniLoadMore from "./components/uni-load-more.vue";

import lktauthorizejs from "@/mixins/lktauthorize.js"

import req from "./common/req/main.js";
import {
	getSystemConfig,
	setPageTitle
} from "./common/systemConfig.js";
import {
	navigator
} from "./common/navigate";
import {
	getStatusBarHeight,
	guid,
	pxToRpxRetNumber,
} from "./common/util";

// #ifdef H5
var jweixin = require("jweixin-module");
Vue.prototype.$jweixin = jweixin;
import {
	jssdk_share
} from "@/common/util.js";
import VueClipboard from "vue-clipboard2";

Vue.use(VueClipboard);
import vconsole from "vconsole";
if (laiketuiComm.IS_DEBUG) {
	Vue.prototype.$vconsole = new vconsole(); // 使用vconsole
}
// #endif


Vue.config.productionTip = false;
Vue.prototype.$store = store;

Vue.prototype.pxToRpxNum = pxToRpxRetNumber;

Vue.component("lktauthorize", lktauthorize);
Vue.component("toload", load);
Vue.component("heads", heads);
Vue.component("uniLoadMore", uniLoadMore);
Vue.prototype.$req = req;

// i18n: simple key path lookup to match existing $t usage
Vue.prototype.$t = function(key, fallback = "") {
	if (!key) return "";
	const langSource = (this && this.language && Object.keys(this.language).length)
		? this.language
		: getLangModule(uni.getStorageSync("language"));
	const value = String(key).split(".").reduce((obj, k) => {
		if (!obj || typeof obj !== "object") return undefined;
		return obj[k];
	}, langSource);
	if (value === undefined || value === null) return fallback || key;
	return value;
};


// 敏感词接口 先暂时 不封装成一个函数
// saas.PublicTools.getDictionaryCatalogInfo

let statusBarHeight = getStatusBarHeight();

const padZero = n => n < 10 ? `0${n}` : n;

Vue.filter("dateFormat", function(val) {
	if (!val) return "暂无时间";

	const d = new Date(val);
	// 校验日期合法性（避免Invalid Date）
	if (isNaN(d.getTime())) return "暂无时间";

	// 复用补零函数，精简代码
	return `${d.getFullYear()}-${padZero(d.getMonth() + 1)}-${padZero(d.getDate())}
                ${padZero(d.getHours())}:${padZero(d.getMinutes())}:${padZero(d.getSeconds())}`;
});

Vue.mixin({
	data() {
		return {
			access_id: Vue.prototype.$store.state.access_id ||
				uni.getStorageSync("access_id"),
			language: {},
			languageType: "",
			hasGrade: "",
		};
	},
	created() {
		// 非 H5 平台（如小程序、App）无法在 created 获取 URL，
		// 所以只做基础语言初始化（无 URL 参与）
		// 但为保证 this.language 有值，仍调用 setLang（此时 URL 为空，走缓存或设备语言）
		this.setLang();

		// #ifdef H5
		window.onbeforeunload = function() {
			window.scrollTo(0, 0);
		};
		// #endif 
		this.hasGrade = uni.getStorageSync("hasGrade");
	},
	onLoad: function(option) {
		if (option.fatherId) {
			uni.setStorageSync("fatherId", option.fatherId);
			if (option.isfx) {
				laiketuiComm.bindPID(option.isfx, option.fatherId);
			}
		}
		// #ifdef H5
		uni.removeStorageSync("url");
		let storeID = option.store_id;
		if (!storeID) {
			storeID = uni.getStorageSync("store_id");
		}
		laiketuiComm.initStoreID(storeID);

		// 【关键】H5 在 onLoad 时调用一次 setLang，让它读取 URL 并初始化
		this.setLang();
		// #endif
	},
	onShow() {
		let data2 = {
			api: "app.index.getUserTell",
		};
		this.$req.post({
			data: data2
		}).then((res2) => {
			if (res2.code == 200) {
				if (res2.data.systemMsgType == 1) {
					uni.navigateTo({
						url: "/pages/index/index"
					});
				}
			}
		});

		// ✅【关键修复】删除了原来 onShow 中全部的语言重置逻辑！
		// 语言只由 onLoad 或用户手动切换决定，onShow 不再干预
		this.setLang();

		// 翻译完成后重新同步页面标题，确保导航栏标题能够被翻译
		if (window.__LKT_TRANSLATED__) {
			this.syncMallTitle();
		}
	},
	mounted() {

	},
	computed: {
		halfWidth() {
			return `${statusBarHeight}px`;
		},
	},
	methods: {
		syncMallTitle(retry = true) {
			setPageTitle();
			return getSystemConfig().then((systemInfo) => {
				const name = (systemInfo && systemInfo.name) || uni.getStorageSync("mall_name") || "";
				if (name) {
					uni.setStorageSync("appTitle", name);
					// 页面如果有 appTitle 字段，同步一份，避免头部展示旧值
					if (this && this.$data && Object.prototype.hasOwnProperty.call(this.$data, "appTitle")) {
						this.appTitle = name;
					}
				}
				setPageTitle();
				return name;
			}).catch(() => {
				if (retry) {
					return new Promise((resolve) => {
						setTimeout(() => resolve(this.syncMallTitle(false)), 800);
					});
				}
				return "";
			});
		},

		// 更新 URL 中的 language 参数（仅 H5）
		updateUrlLanguage(langCode) {
			// #ifdef H5
			if (typeof langCode !== 'string' || !langCode.trim()) {
				console.warn('updateUrlLanguage: Invalid langCode provided:', langCode);
				return;
			}

			const hash = window.location.hash;
			if (!hash) {
				console.warn('updateUrlLanguage: No hash found in URL.');
				return;
			}

			// 检查当前 hash 是否已包含 language 或 lang 参数
			const queryIndex = hash.indexOf('?');
			if (queryIndex === -1) {
				// 没有查询参数，直接添加
				this._updateHashWithLanguage(hash, langCode);
				return;
			}

			const search = hash.substring(queryIndex + 1); // 获取查询字符串部分
			const params = new URLSearchParams(search);
			const existingLang = params.get('language') || params.get('lang');

			if (existingLang) {
				// 如果 URL 中已有 language 或 lang 参数，则不执行任何操作
				// 这样可以避免干扰 uni.navigateTo 本身可能设置的 URL
				// console.debug('updateUrlLanguage: Language parameter already exists in URL, skipping update.');
				return;
			}

			// 如果 URL 中没有 language 或 lang 参数，则添加它
			this._updateHashWithLanguage(hash, langCode);

			// #endif
		},

		// 提取公共逻辑，用于更新 hash
		_updateHashWithLanguage(hash, langCode) {
			// #ifdef H5
			let [path, search] = hash.substring(1).split('?'); // 去掉开头的 #
			const params = new URLSearchParams(search || '');
			params.set('language', langCode); // 设置或更新 language 参数
			const newSearch = params.toString();
			const newHash = newSearch ? `#${path}?${newSearch}` : `#${path}`;
			// 使用 replaceState 更新当前 URL 状态，但不影响历史记录栈
			// 注意：这仍然会替换当前历史记录项，但仅在当前页面缺少 language 时才执行
			window.history.replaceState(null, '', newHash);
			// #endif
		},
		// 方法：从 hash 中获取参数
		getHashQuery() {
			// #ifdef H5
			const hash = window.location.hash;
			const queryIndex = hash.indexOf('?');
			if (queryIndex === -1) return {};
			const search = hash.slice(queryIndex + 1);
			const params = new URLSearchParams(search);
			const obj = {};
			for (let [key, value] of params.entries()) {
				obj[key] = value;
			}
			return obj;
			// #endif
			return {};
		},

		// ✅【替换】getDeviceLanguage 使用统一映射
		getDeviceLanguage() {
			let lang = 'zh-CN';
			try {
				const sysInfo = uni.getSystemInfoSync();
				lang = sysInfo.language || sysInfo.appLanguage || navigator?.language || 'zh-CN';
			} catch (e) {
				lang = navigator?.language || 'zh-CN';
			}

			lang = lang.split(/[;,]/)[0].toLowerCase();

			// ✅ 直接使用统一映射表
			for (const [key, value] of Object.entries(deviceLangToCode)) {
				if (key === lang || key.startsWith(lang + '-') || key.endsWith('-' + lang)) {
					return value;
				}
			}

			// 尝试标准化后匹配
			const normalized = normalizeLangCode(lang);
			if (normalized && deviceLangToCode[normalized]) {
				return deviceLangToCode[normalized];
			}

			return 'zh_CN';
		},
		// 保留名称 setLang，但增强其智能初始化能力
		setLang() {
			let urlLang = null;
			// #ifdef H5
			const hashParams = this.getHashQuery();
			const raw = hashParams.lang || hashParams.language;
			if (raw) {
				urlLang = normalizeLangCode(raw);
			}
			// #endif

			let finalLang;
			if (urlLang) {
				finalLang = urlLang;
				uni.setStorageSync("language", finalLang);
				uni.setStorageSync("lang_code", finalLang);
			} else {
				finalLang = uni.getStorageSync("language") || this.getDeviceLanguage();
				if (!uni.getStorageSync("language")) {
					uni.setStorageSync("language", finalLang);
				}
				if (!uni.getStorageSync("lang_code")) {
					uni.setStorageSync("lang_code", finalLang);
				}
			}

			// 【关键】使用统一 getLangModule（支持别名如 zh_HK → zh_TW）
			this.language = getLangModule(finalLang);

			// #ifdef H5
			this.updateUrlLanguage(finalLang);
			uni.setStorageSync('setLangFlag', true);
			// #endif
		},
		// 检测登录
		isLogin(callback) {
			this.$nextTick(() => {
				lktauthorizejs.methods.handleAfterAuth(
					this,
					"/pagesD/login/newLogin?landing_code=1",
					function() {
						callback && callback();
					}
				);
			});
		},
		guid() {
			return guid();
		},
		navTo(url, checkLogin = false, me = null) {
			return navigator.to(url, checkLogin, me);
		},
		navToHome() {
			return navigator.toHome();
		},
		navBack() {
			return navigator.back();
		},
		navSwitchTab(url) {
			return navigator.switchTab(url);
		},
	},
});

const app = new Vue({
	...App,
	store,
});
app.$mount();
