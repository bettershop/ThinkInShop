import Vue from 'vue' 

import {
    getExternalLangCode,
    langModules
} from "@/common/lang/config.js";

 // #ifdef H5
import translate from '@my-miniprogram/src/lib/translate.js'
// #endif
let toast;


function log(msg, data, type = 'log') {
    
  // ✅ 获取当前语言
  const currentLang = uni.getStorageSync('language') || 'zh_CN';
  
  // ✅ 从 langModules 安全获取 request toast 配置
  const requestToasts = langModules[currentLang]?.toasts?.request ||
                        langModules['zh_CN'].toasts.request;

  // #ifdef MP-WEIXIN
  console[type](msg, data)
  // #endif

  // #ifndef MP-WEIXIN
  if (!Vue.prototype.LaiKeTuiCommon.IS_DEBUG) return false
  console[type](msg, data)
  // #endif

  // 可选：错误时弹出提示（取消注释即可）
  // if (type === 'error' && requestToasts?.error) {
  //   uni.showToast({
  //     title: requestToasts.error,
  //     icon: 'none'
  //   })
  // }
}

function getQueryParams(){
    let url = uni.getStorageSync('url');
    let pos = url.indexOf("?");
    let queryParams = uni.getStorageSync('url').substring(pos,url.length);
    return queryParams;
}
let pending = 0 // 当前页面请求接口的总数量 （用于触发国家化 翻译 变量）
class Request {
    
    constructor() {
        this.URI = uni.getStorageSync('url') || ''
    }

    request(Parma) {
        log('当前请求参数', Parma) 
        pending ++ 
        return new Promise((resolve, reject) => {
            // 请求拦截
            if (typeof this.request.use === 'function') {
                this.request.use(Parma)
            }
            let storeType = Vue.prototype.LaiKeTuiCommon.getStoreType()
			let TMPUrl = uni.getStorageSync('url') || Vue.prototype.LaiKeTuiCommon.LKT_API_URL;
 
            if(TMPUrl.indexOf("&store_type") == -1){
                TMPUrl = TMPUrl + `&store_type=${storeType}`
            } 
            
            if(Parma.data && !Parma.data.hasOwnProperty("country_num")){
                Parma.data.country_num = uni.getStorageSync('country_num') ? uni.getStorageSync('country_num'):156;
            }
             
            uni.request({
                url: TMPUrl ,
                method: Parma.method || 'GET',
                data: {
                    access_id: Vue.prototype.$store.state.access_id || uni.getStorageSync('access_id') || uni.getStorageSync('laiketuiAccessId'),
                    language: uni.getStorageSync('language') || 'zh_CN',
                    lang_code: uni.getStorageSync('lang_code') || uni.getStorageSync('language') || 'zh_CN',
                    ...Parma.data
                } || {},
                header: Parma.header || {
                    'content-type': 'application/x-www-form-urlencoded'
                },
                success: (res) => {  
                    // 重定向 - 到首页
                    if(res?.data?.code == 301){
                      uni.redirectTo({
                          url:'/pages/shell/shell'
                      })
                      return
                    }
                    if (this.response(res)) {
                        log('请求成功', res.data)
                        resolve(res.data) 
                    } else {
                        log('接口报错', res.data, 'error')    
                    }
                },
                fail: (err) => {
                    // TODO:错误处理
                    log('接口报错', err, 'error')
                    reject(err)
                }
            })
        }).finally(() => {
            pending--;
            // #ifdef H5
            const shouldTranslate = uni.getStorageSync('setLangFlag');
            if (shouldTranslate && pending <= 0) {
                const requestTranslation = window.__LKT_REQUEST_TRANSLATION__;
                if (typeof requestTranslation === 'function') {
                    requestTranslation('request-idle');
                } else {
                    // 兜底：当全局调度器未就绪时，延后执行，确保不抢在页面 DOM 更新前触发
                    setTimeout(() => {
                        const lang = uni.getStorageSync('language') || 'zh_CN';
                        const targetLang = getExternalLangCode(lang);
                        try {
                            translate.changeLanguage(targetLang);
                            translate.execute();
                        } catch (e) {
                            console.error('Translation execute failed:', e);
                        } finally {
                            uni.removeStorageSync('setLangFlag');
                            window.dispatchEvent(new CustomEvent('translationDone'));
                        }
                    }, 120);
                }
            }
            // #endif
        });
    }
    
    uploadimg(filePath, extData) {
        return new Promise((resolve, reject) => {
            let data = {
                // module: 'app',
                // action: 'mch',
                // m: 'uploadImgs',
                api:"mch.App.Mch.UploadImgs",
                shop_id: uni.getStorageSync('shop_id') || '',
                storeType:Vue.prototype.LaiKeTuiCommon.getStoreType()
            }

            data = {...data, ...extData }

            let access_id = Vue.prototype.$store.state.access_id || uni.getStorageSync('access_id')
            if (access_id) {
                data.access_id = access_id;
            }
            let endUrl = uni.getStorageSync('url') 
            this.decideURl()
            //跨域处理
            if(uni.getStorageSync('isCrossDomain')){
                //连接本地请注释这里
                // endUrl = '/api' + getQueryParams()
            }
            uni.uploadFile({
                url: endUrl,
                filePath: filePath,
                timeout: 100000,
                name: 'image',
                // #ifdef MP-ALIPAY
                fileType: 'image',
                // #endif
                formData: data,
                success: (res) => {
                    let Data = JSON.parse(res.data)
                    if (Data.code == 200) {
                        resolve(Data)
                    } else {
                        reject(Data)
                        uni.showToast({
                            title: Data.message,
                            icon: 'none'
                        })
                    }
                    uni.hideLoading()
                }
            })

        })
    }
    
     // 判断 本地地址 和 配置地址 是否一致, 并缓存 结果
            decideURl(){
                // #ifdef H5
                    
                    if(uni.getStorageSync('isCrossDomain')){
                        return
                    }
                
                    //跨域处理20240603
                    let isCrossDomain = false
                    
                    //获取接口请求地址 及 H5服务器部署地址，如果不一样则要进行跨域处理
                    let JKurl =  Vue.prototype.LaiKeTuiCommon.LKT_ROOT_URL
                    let H5url = 'https://' + window.location.host 
                    if(JKurl != H5url){
                        isCrossDomain = true
                    } else if(H5url.indexOf('localhost') != -1){
                        isCrossDomain = true
                    }
                    if(isCrossDomain){
                        uni.setStorageSync('isCrossDomain', {name:'接口请求地址与H5服务器部署地址是否不一致',value:isCrossDomain})
                    }
                // #endif
            }
    
    uploadimgl(filePath, extData) {
        return new Promise((resolve, reject) => {
            let data = {
                api:'resources.file.uploadFiles',
                shop_id: uni.getStorageSync('shop_id') || '',
                uploadType:2,
                coverage :1,
                img_type:0
            }
    
            data = {...data, ...extData }
    
            let access_id = Vue.prototype.$store.state.access_id || uni.getStorageSync('access_id')
            if (access_id) {
                data.access_id = access_id;
            }

            let endUrl = uni.getStorageSync('url')
            this.decideURl()
            //跨域处理
            if(uni.getStorageSync('isCrossDomain')){
                //连接本地请注释这里
                endUrl = '/api' + getQueryParams()
            }
    
            uni.uploadFile({
                url: endUrl,
                filePath: filePath,
                timeout: 100000,
                name: 'file',
                // #ifdef MP-ALIPAY
                fileType: 'file',
                // #endif
                formData: data,
                success: (res) => {
                    let Data = JSON.parse(res.data)
                    if (Data.code == 200) {
                        resolve(Data)
                    } else {
                        reject(Data)
                    }
                },
                fail: (err) => {
                    console.log(err,'err')
                }
            })
    
        })
    }

    response(res) {
        return true;
        let {
            code,
            statusCode 
        } = res
        // 重定向 到首页
        
		// console.log(res.data)
        
        if (typeof res.data !== 'object') {
            
            console.log('如果不是支付接口，可能请求出错了')
            console.log(res)
            // return false
        }
        
        // 响应拦截
        if (typeof this.response.use === 'function') {
            this.response.use(res)
        }


        
        if (statusCode === 200) {
            return true
        }else if(statusCode === 203){
             console.log(statusCode)
        }
        
        return false
    }

}

export default Request
