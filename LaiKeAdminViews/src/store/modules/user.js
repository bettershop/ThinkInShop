import { login } from '@/api/login/index'
import Cookies from 'js-cookie';
import { setUserAdmin } from '@/api/Platform/merchants'
import { getShopInfo } from '@/api/Platform/merchants'
import {
  getSystemIndex
} from '@/api/mall/systemManagement'
import { getRoleMenu } from '@/api/Platform/permissions'
import router, {resetRouter, sysRouters} from '@/router'
import { getStorage, getCookie, setStorage, removeStorage } from '@/utils/storage'

import { updateLanguage } from '@/lang/index'

const getDefaultState = () => {
  return {
    token: getStorage('laike_admin_userInfo') ? getStorage('laike_admin_userInfo').token : '',
    name: '', // 用户名
    rolesInfo: getStorage('rolesInfo'),
    // authorizationList: getStorage('menuList'),
    merchants_Logo: getStorage('laike_head_img') ? getStorage('laike_head_img') : '',
    head_img: getStorage('laike_admin_userInfo') ? getStorage('laike_admin_userInfo').portrait : ''
  }
}

const state = getDefaultState()

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  // SET_AUTHORIZATIONLIST: (state, authorizationList) => {
  //   state.authorizationList = authorizationList
  // },
  SET_MERCHANTSLOGO: (state, merchants_Logo) => {
    state.merchants_Logo = merchants_Logo
  },
  SET_HEADIMG: (state, head_img) => {
    state.head_img = head_img
  }
}

const actions = {
  // 用户登录
  login({ commit }, userInfo) {

    const { customerNumber, userName, pwd ,imgCodeToken,imgCode} = userInfo
    //用户登录无法知道是哪个商城只能不传语种 获取商城管理后台的language值 无法知道商城id
    // let language = getCookie('language'+storeId)?getCookie('language'+storeId):'zh_CN'
    let language = 'zh_CN'
    return new Promise((resolve, reject) => {
      login({ customerNumber: customerNumber ? customerNumber : '' , userName: userName.trim(), pwd: pwd ,imgCodeToken:imgCodeToken,imgCode:imgCode,language:language})
      .then(response => {
        const { data } = response
        commit('SET_TOKEN', data.data.token)
        setStorage('laike_admin_userInfo',response.data.data)
        
        const  haveStoreMchId = response.data?.data?.haveStoreMchId
        sessionStorage.setItem('haveStoreMchId', haveStoreMchId);
        //默认币种
        let currency_symbol = response.data?.data?.storeDefaultCurrencyInfo?.currency_symbol
        if(!currency_symbol || 'undefined' ==currency_symbol )
        {
          console.error(">>>没有设置默认币种，默认使用RMB")
          currency_symbol = '￥';
        }
        let default_storeid = data.data.storeId
        sessionStorage.setItem('currency_symbol_'+default_storeid, currency_symbol);
        console.log("currency_symbol"+currency_symbol);

        commit('SET_HEADIMG',data.data.portrait)
        setStorage('rolesInfo',{
          type: data.data.type,
          role: parseInt(data.data.role),
          storeId: data.data.storeId
        })

        let sid = data.data.storeId;
        console.log("data.data.storeId:"+sid)
        // debugger

        let lang = response.data.data.lang;

        //这个trcLang 是给右上角的语种回显用的 选中样式
        sessionStorage.setItem('trcLang'+default_storeid, lang);

        //更新i18n的语言和cookie中的语言
        updateLanguage(lang,data.data.storeId)

        console.log('61616161',getStorage('rolesInfo'));
        resolve(response)
      })
      .catch(error => {
        reject(error)
      })
    })


  },

  async getSystemIcon() {
    let { entries } = Object
    let data = {
      api: 'admin.system.GetBasicConfiguration'
    }
    let formData = new FormData()
    for (let [key, value] of entries(data)) {
      formData.append(key, value)
    }
    const res = await getSystemIndex(formData)
    let systemInfo = res.data.data.list 
  
    if (systemInfo.html_icon){ 
        console.log('res', systemInfo)
        const { html_icon } = systemInfo 

        const oldLink = document.querySelector('link[rel="icon"]');
        if (oldLink) {
          document.head.removeChild(oldLink);
        }

        // 创建新icon标签（对应你提供的原生代码）
        const link = document.createElement('link');
        link.rel = 'icon';
        // 自动识别icon格式，兼容ico/png
        link.type = html_icon.includes('png') ? 'image/png' : 'image/x-icon';
        link.href = html_icon;

        // 插入到head
        document.head.appendChild(link);
    }
  },

  // 获取系统管理员角色权限列表
  getAuthorizationList({ commit, state }) {
    return new Promise((resolve, reject) => {
      if(getStorage('rolesInfo').type === 0) {
        getShopInfo({
          api: 'saas.shop.getShopInfo',
          pageNo: 1,
          pageSize: 1,
          storeId: null
        }).then(res => {
          if(res.data.code == '200') {
            const laike_admin_userInfo = getStorage('laike_admin_userInfo')
            const rolesInfo = getStorage('rolesInfo')
            console.log('获取系统管理员角色权限列表',res.data.data);
            setStorage('laike_admin_userInfo',laike_admin_userInfo)
            if(res.data.data.dataList && res.data.data.dataList.length>0){

              if( !getStorage('website_information')) {
                let website_information = {
                  contact_address: res.data.data.dataList[0].contact_address,
                  contact_number: res.data.data.dataList[0].contact_number,
                  copyright_information: res.data.data.dataList[0].copyright_information,
                  record_information: res.data.data.dataList[0].record_information
                }
                setStorage('website_information',website_information)
              }

              let storeName = laike_admin_userInfo.store_name;
              if(getStorage('rolesInfo').storeId == 1 && !storeName) {
                rolesInfo.storeId = res.data.data.dataList[0].id
                laike_admin_userInfo.storeId = res.data.data.dataList[0].id
                setStorage('laike_head_img',res.data.data.dataList[0].merchant_logo)
              }

            }
            setStorage('laike_admin_userInfo',laike_admin_userInfo)
            setStorage('rolesInfo',rolesInfo)

            setUserAdmin({
              api: 'admin.saas.user.setUserAdmin',
            })
            .then(response => {
              if(response.data.code == '200') {
                laike_admin_userInfo.mchId = response.data.data.mchId
                setStorage('laike_admin_userInfo',laike_admin_userInfo)
                resolve(response)
              }
            })
            .catch(error => {
              reject(error)
            })


            resolve()
          }
        }).catch(error => {
          window.sessionStorage.clear()
          reject(error)
        })
      } else if(getStorage('rolesInfo').type === 2 || getStorage('rolesInfo').type === 3) {
        getShopInfo({
          api: 'saas.shop.getShopInfo',
          storeId: getStorage('rolesInfo').storeId
        }).then(res => {
          if(res.data.code == '200') {
            const laike_admin_userInfo = getStorage('laike_admin_userInfo')
              setStorage('laike_admin_userInfo',laike_admin_userInfo)
            if(res.data.data.dataList && res.data.data.dataList.length>0){
              setStorage('laike_head_img',res.data.data.dataList[0].merchant_logo)
              if(!getStorage('website_information')) {
                let website_information = {
                  contact_address: res.data.data.dataList[0].contact_address,
                  contact_number: res.data.data.dataList[0].contact_number,
                  copyright_information: res.data.data.dataList[0].copyright_information,
                  record_information: res.data.data.dataList[0].record_information
                }
                setStorage('website_information',website_information)
              }
            }
            resolve(res)
          }
        })
      } else if(getStorage('rolesInfo').type === 1) {
        getShopInfo({
          api: 'saas.shop.getShopInfo',
          storeId: getStorage('rolesInfo').storeId
        }).then(res => {
          if(res.data.code == '200') {
            const laike_admin_userInfo = getStorage('laike_admin_userInfo')
            setStorage('laike_admin_userInfo',laike_admin_userInfo)
            if(res.data.data.dataList && res.data.data.dataList.length>0 ){

              setStorage('laike_head_img',res.data.data.dataList[0].merchant_logo)

              if(!getStorage('website_information')) {
                let website_information = {
                  contact_address: res.data.data.dataList[0].contact_address,
                  contact_number: res.data.data.dataList[0].contact_number,
                  copyright_information: res.data.data.dataList[0].copyright_information,
                  record_information: res.data.data.dataList[0].record_information
                }
                setStorage('website_information',website_information)
              }
            }

            resolve(res)
          }
        })
      }
    })

  },

  // 赋予系统管理员商城
  setUserAdmin() {
    return new Promise((resolve, reject) => {
      setUserAdmin({
        api: 'admin.saas.user.setUserAdmin',
        storeId: 0
      })
      .then(response => {
        if(response.data.code == '200') {
          resolve(response)
        }
      })
      .catch(error => {
        reject(error)
      })
    })
  },

  // 退出登录
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      logout(state.token).then(() => {
        removeStorage('laike_admin_userInfo') // must remove  token  first
        removeStorage('auctionTabel')
        resetRouter()
        commit('RESET_STATE')
        //把角色信息设置为空列表
        commit('SET_ROLES', [])
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },

  // 移除token
  resetToken({ commit }) {
    return new Promise(resolve => {
      // removeStorage('laike_admin_userInfo') // must remove  token  first
      commit('RESET_STATE')
      //把角色信息设置为空列表
      commit('SET_ROLES', [])
      resolve()
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

