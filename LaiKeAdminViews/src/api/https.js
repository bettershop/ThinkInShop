import axios from 'axios'
import ElementUI from 'element-ui';
import { removeStorage, getStorage } from '@/utils/storage'
import router from '@/router'
import Config from "@/packages/apis/Config";
const request = axios.create({
  // timeout: 10000
})


let flag = true
// 节流   接口 在得出结果前不允许再次请求，得出结果之后 2秒内不能请求
request.throttle = function (func) {
  return function (...args) {
    if (!flag) {
      return new Promise((resolve, reject) => {
        resolve({
          data: {
            code: 300,
            message: '请勿重复点击'
          }
        }); // 这里返回错误信息
      });
    }
    flag = false
    const res = func.apply(this, args);
    setTimeout(() => {
      flag = true
    }, 2000)
    return res
  };
}

// //loading对象
// let loading;
// //当前正在请求的数量
// let needLoadingRequestCount = 0;
// //显示loading
// function showLoading(target) {
//   // 后面这个判断很重要，因为关闭时加了抖动，此时loading对象可能还存在，
//   // 但needLoadingRequestCount已经变成0.避免这种情况下会重新创建个loading
//   if (needLoadingRequestCount === 0 && !loading) {
//     loading = ElementUI.Loading.service({
//       lock: true,
//       text: "Loading...",
//       background: 'rgba(255, 255, 255, 0.5)',
//       target: target || "body"
//     });
//   }
//   needLoadingRequestCount++;
// }

// //隐藏loading
// function hideLoading() {
//   needLoadingRequestCount--;
//   needLoadingRequestCount = Math.max(needLoadingRequestCount, 0); //做个保护
//   if (needLoadingRequestCount === 0) {
//     //关闭loading
//     toHideLoading();
//   }
// }
// //防抖：将 300ms 间隔内的关闭 loading 便合并为一次。防止连续请求时， loading闪烁的问题。
// var toHideLoading = _.debounce(()=>{
//   loading.close();
//   loading = null;
// }, 300);


function getCookie(name) {
  var cookies = document.cookie;
  var list = cookies.split("; ");     // 解析出名/值对列表

  for (var i = 0; i < list.length; i++) {
    var arr = list[i].split("=");   // 解析出名和值
    if (arr[0] == name)
      return decodeURIComponent(arr[1]);   // 对cookie值解码
  }

  //登录后默认返回用户所选语种；如用户未选择语种；则默认返回系统默认语种
  //浏览器用户未操作右上角语种选择 默认返回zh_CN
  return "zh_CN";
}

request.defaults.baseURL = Config.baseUrl

// 配置请求拦截器
request.interceptors.request.use(config => {
  // if(config.headers.showLoading !== false){
  //   showLoading(config.headers.loadingTarget);
  // }
  console.log('config', config)
  if (config.method === 'post') {

    try{ 
      const userInfo = getStorage('laike_admin_userInfo') || {} 
      config.params = {
        //商城id
        storeId: userInfo.storeId || 1,
        //来源
        storeType: 8,
        ...config.params,
        //token
        accessId: userInfo.token,
        language: getCookie('language' + userInfo.storeId) || 'zh_CN'
      }
    }catch(e){
      console.log('请求拦截～post请求参数异常->', e)
    }
    //遍历formData的值如果存在[中括号，则需要encodeURI编码（bugID 56675）
    // for (let key in config.params){
    //   if(typeof config.params[key] == 'string' && config.params[key].indexOf('[')!=-1){
    //     console.log('请求拦截～中括号需encodeURI编码->', 'key键：' +key+ ';', '键值类型：' +typeof config.params[key]+ ';', '键值：' +config.params[key]+ ';', '键值编码后：' +encodeURI(config.params[key])+ ';');
    //     config.params[key] = encodeURI(config.params[key])
    //   }
    // }
    if (config.params.exportType && config.params.exportType == 1) {
      config.headers = {
        'Content-Type': 'application/octet-stream;charset=utf-8'
      }
      config.responseType = "blob";
    }
  }
  return config
}, err => {
  // if(config.headers.showLoading !== false){
  //   hideLoading();
  // }
  return Promise.reject(err)
})

// 配置响应拦截器
request.interceptors.response.use((res) => {
  // if(res.config.headers.showLoading !== false){
  //   hideLoading();
  // }
  if (res.data.code == '203') {
    let type = getStorage('rolesInfo').type
    // if(type == 0) {
    //   window.sessionStorage.clear()
    //   window.location.href = '/login'
    //   location.reload();
    // } else {
    //   window.sessionStorage.clear()
    //   window.location.href = '/mallLogin'
    //   location.reload();
    // }
    if (type == 0) {
      window.sessionStorage.clear()
      router.push({
        path: '/login'
      })
    } else {
      window.sessionStorage.clear()
      router.push({
        path: '/mallLogin'
      })
    }
  }
  if (res.data instanceof Blob) {
    return res
  }
  if (res.status && res.data.code == '200') {
    return res;
  } else {
    ElementUI.Message.closeAll()
    ElementUI.Message({
      message: res.data.message,
      type: 'error',
      offset: 100
    })
    //如果提示 请添加自营店 则跳转到 添加自营店页面
    if (res.data.code == 51008) {
      router.push("/mall/fastBoot/index");
    }
    return res
  }
 

}, (error) => {
  // if(error.config.headers.showLoading !== false){
  //   hideLoading();
  // }

});

export default request;
