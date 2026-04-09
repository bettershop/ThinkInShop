 
import laiketuiComm from '@/components/laiketuiCommon'

import {langModules} from '@/common/lang/config.js'

let toast;
// 根据页面key 获取页面内容
function getDIYPageInfoById(val) {
	const getType = (value) => Object.prototype.toString.call(value).slice(8, -1)
	if (getType(val) == 'Object') { 
		let styleConfig = '' 
        
		const data = uni.getStorageSync('DIYPAGEINFO')
		if (data) {
			const pageItem = data.find(v => v.key == val.key)
            console.log('pageItem',data)
			if (pageItem && pageItem.page_context) {
				styleConfig = JSON.parse(pageItem.page_context).defaultArray;
			} else {
				styleConfig = pageItem.defaultArray;
			}
		}
 		return styleConfig
	} else {
		uni.showToast({
			title: 'TypeError:val not is Object !!!',
			icon: 'none'
		})
	}
	return ''
} 

// 缓存系统信息
let  systemInfoCache = null

// px转 rpx计算 返回 计算好的rpx 数值不带rpx单位 
function pxToRpxRetNumber(px,_this) {
  const systemInfo = initSystemInfo();
  if (!systemInfo) return px * 1; 
    
    // // 手机端特征关键词（覆盖主流移动设备）
    const mobileKeywords = [
      'Mobile',    // 通用移动设备标识
      'Android',   // Android 设备
      'iPhone',    // iPhone 设备
      'iPad',      // iPad 设备（平板归为移动设备）
      'iPod',      // iPod 设备
      'Windows Phone', // 微软手机
      'Symbian',   // 塞班系统
      'BlackBerry',// 黑莓手机
      'Opera Mini' // 移动 Opera 浏览器
    ]; 
    // // 判断 UA 中是否包含任意移动关键词（忽略大小写）
    let isMobile =''
    try{   
        isMobile =  mobileKeywords.some(keyword => { 
          return systemInfo.ua.toLowerCase().includes(keyword.toLowerCase())
        });
    }catch(e){
        console.error(e)
    }
  let rpxValue  = ''
  if(isMobile){ 
    rpxValue = px * (750 / systemInfo.windowWidth); 
  }else{
     rpxValue = px *(750 / 375) ;
  }
  return Math.round(rpxValue).toString();
}

// 初始化获取系统信息 
function initSystemInfo() { 
    if (systemInfoCache) { 
      return systemInfoCache;
    }
    try {
      //  获取系统信息 
      const res = uni.getSystemInfoSync();
      systemInfoCache = res;  
    } catch (err) { 
      console.error('获取系统信息失败:', err);
      systemInfoCache = null;
    }
      return systemInfoCache; 
}

function formatTime(time) {
	if (typeof time !== 'number' || time < 0) {
		return time
	}

	var hour = parseInt(time / 3600)
	time = time % 3600
	var minute = parseInt(time / 60)
	time = time % 60
	var second = time

	return ([hour, minute, second]).map(function(n) {
		n = n.toString()
		return n[1] ? n : '0' + n
	}).join(':')
}

function formatLocation(longitude, latitude) {
	if (typeof longitude === 'string' && typeof latitude === 'string') {
		longitude = parseFloat(longitude)
		latitude = parseFloat(latitude)
	}

	longitude = longitude.toFixed(2)
	latitude = latitude.toFixed(2)

	return {
		longitude: longitude.toString().split('.'),
		latitude: latitude.toString().split('.')
	}
}

var dateUtils = {
	UNITS: {
		'年': 31557600000,
		'月': 2629800000,
		'天': 86400000,
		'小时': 3600000,
		'分钟': 60000,
		'秒': 1000
	},
	humanize: function(milliseconds) {
		var humanize = ''
		for (var key in this.UNITS) {
			if (milliseconds >= this.UNITS[key]) {
				humanize = Math.floor(milliseconds / this.UNITS[key]) + key + '前'
				break
			}
		}
		return humanize || '刚刚'
	},
	format: function(dateStr) {
		var date = this.parse(dateStr)
		var diff = Date.now() - date.getTime()
		if (diff < this.UNITS['天']) {
			return this.humanize(diff)
		}
		var _format = function(number) {
			return (number < 10 ? ('0' + number) : number)
		}
		return date.getFullYear() + '/' + _format(date.getMonth() + 1) + '/' + _format(date.getDay()) + '-' +
			_format(date.getHours()) + ':' + _format(date.getMinutes())
	},
	//将"yyyy-mm-dd HH:MM:ss"格式的字符串，转化为一个Date对象
	parse: function(str) {
		var a = str.split(/[^0-9]/)
		return new Date(a[0], a[1] - 1, a[2], a[3], a[4], a[5])
	}
}

// 获取当前日期（时分秒）
function getNowFormatDate() {
	var date = new Date();
	var seperator1 = "-";
	var year = date.getFullYear();
	var month = date.getMonth() + 1;
	var day = date.getDate();
	if (month >= 1 && month <= 9) {
		month = "0" + month;
	}
	if (day >= 0 && day <= 9) {
		day = "0" + day;
	}
	var currentdate = year + seperator1 + month + seperator1 + day;
	return currentdate;
}
// 获取当前时间 (年月日 时分秒)
function getDateAll() {
	var date = new Date();
	var _format = function(number) {
		return (number < 10 ? ('0' + number) : number)
	}
	return date.getFullYear() + '-' + _format(date.getMonth() + 1) + '-' + _format(date.getDay()) + ' ' +
		_format(date.getHours()) + ':' + _format(date.getMinutes() + ':' + _format(date.getSeconds()))
}


/**
 * H5 复制
 * */
/**
 * H5 复制
 */
const copyText = (domname, text) => {
  // 获取当前语言
  const currentLang = uni.getStorageSync('language') || 'zh_CN';
  console.log("currentLang",currentLang)
  console.log("langModules[currentLang]",langModules)
  // 从公共语言模块中获取对应 toast
 const toastList = langModules[currentLang]?.toasts?.copyText ||
                    langModules['zh_CN'].toasts.copyText; // 默认回退到中文

  // 数字没有 .length 不能执行selectText 需要转化成字符串
  const textString = text.toString();
  let input = '';

  if (domname) {
    input = document.querySelector(domname);
  }

  if (!input) {
    input = document.createElement('input');
    input.id = 'copy-input';
    input.readOnly = 'readOnly';
    input.style.position = 'fixed';
    input.style.left = '-1000px';
    input.style.top = '-1000px';
    input.style.zIndex = '-1000';
    input.style.border = '1px #fff solid';
    input.style.opacity = '0';
    document.body.appendChild(input);
  } else {
    input.readOnly = 'readOnly';
  }

  input.value = textString;
  selectText(input, 0, textString.length);

  if (document.execCommand('copy')) {
    document.execCommand('copy');
    // ✅ 可在此处加成功提示（如果需要）
    // uni.showToast({ title: toastList[0] || '复制成功', icon: 'none' });
  }

  input.blur();

  function selectText(textbox, startIndex, stopIndex) {
    if (textbox.createTextRange) {
      const range = textbox.createTextRange();
      range.collapse(true);
      range.moveStart('character', startIndex);
      range.moveEnd('character', stopIndex - startIndex);
      range.select();
    } else {
      textbox.setSelectionRange(startIndex, stopIndex);
      textbox.focus();
    }
  }
};

function share(me) {
	if (document.addEventListener) {
		document.addEventListener('WeixinJSBridgeReady', onBridgeReady(me), false)
	}
}

function onBridgeReady(me) {
	console.log('aaaaa')
	var shareHref = ''
	if (me.shareHref == '') {
		shareHref = window.location.href
	} else {
		shareHref = me.shareHref
	}
	var wdesc = '我分享了一个店铺，快来看一看吧！'
	var wtit = '来客电商'
	var wappid = ''
	var wimg = me.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/kfef2x.png'
	// 发送给好友
	WeixinJSBridge.on('menu:share:appmessage', function(argv) {
		WeixinJSBridge.invoke('sendAppMessage', {
			'appid': wappid,
			'img_url': wimg,
			'img_width': '200',
			'img_height': '200',
			'link': shareHref,
			'desc': wdesc,
			'title': wtit,
		})
	})
	// 分享到朋友圈
	WeixinJSBridge.on('menu:share:timeline', function(argv) {
		WeixinJSBridge.invoke('shareTimeline', {
			'img_url': wimg,
			'img_width': '200',
			'img_height': '200',
			'link': shareHref,
			'desc': wdesc,
			'title': wtit
		})
	})
	// 分享到微博
	WeixinJSBridge.on('menu:share:weibo', function(argv) {
		WeixinJSBridge.invoke('shareWeibo', {
			'content': '',
			'url': shareHref,
		})
	})
}

const jssdk_share = function(option) {
    var jweixin = require('jweixin-module')
  
	var now_url = window.location.href
	var can_share = false

	var data = {
		api: 'app.jssdk.index',
		m: 'getData',
		url: now_url
	}

	uni.request({
		data,
		url: uni.getStorageSync('url'),
		header: {
			'content-type': 'application/x-www-form-urlencoded'
		},
		method: 'POST',
		success: (res) => {
			var jsApiList = ['updateAppMessageShareData', 'updateTimelineShareData']

			jweixin.config({
				debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
				appId: res.data.appId, // 必填，公众号的唯一标识
				timestamp: res.data.timestamp, // 必填，生成签名的时间戳
				nonceStr: res.data.nonceStr, // 必填，生成签名的随机串
				signature: res.data.signature, // 必填，签名
				jsApiList: ['updateAppMessageShareData',
					'updateTimelineShareData'
				] // 必填，需要使用的JS接口列表
			})
		},
	})

	jweixin.ready(() => {
		// config信息验证后会执行ready方法
		// 所有接口调用都必须在config接口获得结果之后
		// config是一个客户端的异步操作，所以如果需要在页面加载时就调用相关接口，则须把相关接口放在ready函数中调用来确保正确执行。
		// 对于用户触发时才调用的接口，则可以直接调用，不需要放在ready函数中。

		jweixin.updateAppMessageShareData({
			title: '来客推', // 分享标题
			desc: '来客推，你的电商之选！', // 分享描述
			link: now_url, // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
			imgUrl: laiketuiComm.LKT_ROOT_VERSION_URL + 'images/share_img.png', // 分享图标
			success: function() {}
		})
	})

}
 
/**
 * 分享绑定上级
 * @param isfx
 * @param fatherId
 * @returns {Promise<void>}
 */
const bindPID = (isfx, fatherId) => {
    if (isfx && fatherId) {
        let data = {
            api: 'app.login.chang_pid',
            pid: fatherId
        }
        let res = this.$req.post({data})
        console.log(res);
    }
    
}


/**
 * @description 时差
 * @param { time }
 * */
const getTimeDiff = (time) => {

	// 兼容app
	time = new Date(time.replace(/-/g, '/'))

	// 当前时间戳
	let currentTime = new Date()

	let reslut = time.getTime() - currentTime.getTime()

	//计算出相差天数
	let days = Math.floor(reslut / (24 * 3600 * 1000))

	//计算出小时数
	let leave1 = reslut % (24 * 3600 * 1000) //计算天数后剩余的毫秒数
	let hours = Math.floor(leave1 / (3600 * 1000))

	//计算相差分钟数
	let leave2 = leave1 % (3600 * 1000) //计算小时数后剩余的毫秒数
	let minutes = Math.floor(leave2 / (60 * 1000))

	//计算相差秒数
	let leave3 = leave2 % (60 * 1000) //计算分钟数后剩余的毫秒数
	let seconds = Math.floor(leave3 / 1000)

	if (hours.toString().length == 1) {
		hours = '0' + hours
	}

	if (minutes.toString().length == 1) {
		minutes = '0' + minutes
	}

	if (seconds.toString().length == 1) {
		seconds = '0' + seconds
	}

	return {
		days,
		hours,
		minutes,
		seconds
	}
}

/**
 * 状态栏高度
 * @returns {number}
 */
const getStatusBarHeight = () => {
	let statusBarHeight = 0
	uni.getSystemInfo({
		success: async res => {
			statusBarHeight = res.statusBarHeight
		}
	})
	return statusBarHeight
}

/**
 * 延迟执行
 * @param delay
 * @returns {Promise<unknown>}
 */
async function later(delay) {
	return new Promise(function(resolve) {
		setTimeout(resolve, delay);
	});
}

/**
 * 本算法来源于简书开源代码，详见：https://www.jianshu.com/p/fdbf293d0a85
 * 全局唯一标识符（uuid，Globally Unique Identifier）,也称作 uuid(Universally Unique IDentifier)
 * 一般用于多个组件之间,给它一个唯一的标识符,或者v-for循环的时候,如果使用数组的index可能会导致更新列表出现问题
 * 最可能的情况是左滑删除item或者对某条信息流"不喜欢"并去掉它的时候,会导致组件内的数据可能出现错乱
 * v-for的时候,推荐使用后端返回的id而不是循环的index
 * @param {Number} len uuid的长度
 * @param {Boolean} firstU 将返回的首字母置为"u"
 * @param {Nubmer} radix 生成uuid的基数(意味着返回的字符串都是这个基数),2-二进制,8-八进制,10-十进制,16-十六进制
 */
function guid(len = 32, firstU = true, radix = null) {
	let chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
	let uuid = [];
	radix = radix || chars.length;

	if (len) {
		// 如果指定uuid长度,只是取随机的字符,0|x为位运算,能去掉x的小数位,返回整数位
		for (let i = 0; i < len; i++) uuid[i] = chars[0 | Math.random() * radix];
	} else {
		let r;
		// rfc4122标准要求返回的uuid中,某些位为固定的字符
		uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
		uuid[14] = '4';

		for (let i = 0; i < 36; i++) {
			if (!uuid[i]) {
				r = 0 | Math.random() * 16;
				uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
			}
		}
	}
	// 移除第一个字符,并用u替代,因为第一个字符为数值时,该guuid不能用作id或者class
	if (firstU) {
		uuid.shift();
		return 'u' + uuid.join('');
	} else {
		return uuid.join('');
	}
}

function is_wx() {
	let en = window.navigator.userAgent.toLowerCase()
	// 匹配en中是否含有MicroMessenger字符串
	return en.match(/MicroMessenger/i) == 'micromessenger'
}
// 只允许输入整数
function oninput2(num) {
	var str = num
	str = str.replace(/[^\.\d]/g, '')
	// 防止多次点击小数点
	str = str.replace(/\.+/, '')
	return Number(str).toString()
}
// 设置只能输入整数和小数
function oninput(num, limit) {
	var str = num
	var len1 = str.substr(0, 1)
	var len2 = str.substr(1, 1)
	//如果第一位是0，第二位不是点，就用数字把点替换掉
	if (str.length > 1 && len1 == 0 && len2 != '.') {
		str = str.substr(1, 1)
	}
	//第一位不能是.
	if (len1 == '.') {
		str = ''
	}
	//限制只能输入一个小数点
	if (str.indexOf('.') != -1) {
		var str_ = str.substr(str.indexOf('.') + 1)
		if (str_.indexOf('.') != -1) {
			str = str.substr(0, str.indexOf('.') + str_.indexOf('.') + 1)
		}
	}
	//正则替换
	str = str.replace(/[^\d^\.]+/g, '') // 保留数字和小数点
	if (limit / 1 === 1) {
		str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/, '$1') // 小数点后只能输 1 位
	} else {
		str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/, '$1') // 小数点后只能输 2 位
	}

	return str
}

// 设置不能输入特殊字符
function oninput3(num) {
	var str = num
	str = str.replace(/[^\w]/g, '')

	return str
}
module.exports = { 
	getDIYPageInfoById,
	formatTime,
	formatLocation,
	getNowFormatDate,
	dateUtils,
	copyText,
	share,
	jssdk_share,
	getTimeDiff,
	getStatusBarHeight,
	later,
	guid,
	is_wx,
	oninput,
	oninput2,
	oninput3,
	getDateAll,
    pxToRpxRetNumber
}
