import axios from 'axios'
import ElementUI from 'element-ui';
import { removeUserInfo } from '@/utils/storage'
import { getStorage, setStorage } from '@/utils/storage'
import Config from "@/packages/apis/Config";

const http = axios.create({
  timeout: 5000
})

http.defaults.baseURL = Config.baseUrl
// 配置请求拦截器
http.interceptors.request.use(config => {
  return config
}, err => {
  return Promise.reject(err)
})

// 配置响应拦截器
http.interceptors.response.use((res) => {
  if (res.data.code == '200') {
    if (res.data.data.info) {
      if (res.data.data.info !== '系统维护中') {
        let message = ''
        if (res.data.data.lang == 'en') {
          message = 'login success'
        } else {
          message = res.data.data.info
        }
        ElementUI.Message({
          message: message,
          type: 'success',
          offset: 100
        })
      }
    } else {
      ElementUI.Message({
        message: '登录成功',
        type: 'success',
        offset: 100
      })
    }

  } else {
    ElementUI.Message({
      message: res.data.message,
      type: 'error',
      offset: 100
    })
  }
  return res;
}, (error) => {

});

export default http;
