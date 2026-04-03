import request from './https.js';
import Cookies from 'js-cookie';
import { getStorage } from '@/utils/storage'
let { entries } = Object;

/**
 * @description 表格--删除
 * @param {Number} param id {Number} 配置id
 */
export function tableDelApi(data) {
  return request({
    url: data.url,
    method: data.method,
    data: data.ids
  });
}

/**
 * 获取消息提醒
 */
export function jnoticeRequest() {
  return request({
    url: 'jnotice',
    method: 'GET'
  });
}

// 查询
export const select = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 新增
export const add = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 删除
export const del = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 编辑
export const edit = (params, formFalg = false) => {
  if (formFalg) {
    let formData = new FormData()
    for (let [key, value] of entries(params)) {
      formData.append(key, value)
    }
    return request({
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
  return request({
    method: "post",
    params,
  });
};

// 获取国家列表
export const getCountries = () => {
  return select({ api: "admin.goods.getCountry" });
};

// 获取语种列表
export const getLanguages = (params) => {
  const mergedParams = { ...{ api: "admin.goods.getLangs" }, ...params };
  return select(mergedParams);
};

//用户所选语种
export const getUserLangVal = () => {
  const userInfo = getStorage('laike_admin_userInfo')
  if (!userInfo || !userInfo.storeId) {
    return ''
  }
  const storeId = userInfo.storeId
  const cookieLang = Cookies.get('language' + storeId)
  const sessionLang = sessionStorage.getItem('trcLang' + storeId)
  const lang = cookieLang || sessionLang || userInfo.lang || ''
  console.log("lang" + lang)
  return lang
}

export const getDefaultCurrencySymbol = () => {
  const userInfo = getStorage('laike_admin_userInfo')
  let currencySymbol = sessionStorage.getItem('currency_symbol_' + userInfo.storeId);
  console.log("currencySymbol" + currencySymbol)
  return currencySymbol;
}


export const formatPrice = (basePrice, exchageRate) => {
  return Number(basePrice * exchageRate).toFixed(2);
}

/**
 *
 * @param basePrice
 * @param exchageRate
 * @returns {string}
 */
export const toStoreDefaultCurrencyPrice = (basePrice, exchageRate) => {
  return Number(basePrice / exchageRate).toFixed(2);
}

export default {
  tableDelApi,
  jnoticeRequest,
  getUserLangVal,
  select,
  add,
  del,
  edit,
  getLanguages,
  getDefaultCurrencySymbol,
  formatPrice,
  toStoreDefaultCurrencyPrice,
  getCountries
};
