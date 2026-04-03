import Vue from 'vue'
import VueI18n from 'vue-i18n'
import Cookies from 'js-cookie'
import elementZhLocale from 'element-ui/lib/locale/lang/zh-CN'// element-ui lang
import elementEsLocale from 'element-ui/lib/locale/lang/en'// element-ui lang
import elementMsLocale from 'element-ui/lib/locale/lang/ms'// element-ui lang
import elementIdLocale from 'element-ui/lib/locale/lang/id'// element-ui lang
import elementJaLocale from 'element-ui/lib/locale/lang/ja'// element-ui lang
import elementRULocale from 'element-ui/lib/locale/lang/ru-RU'// element-ui lang
import elementfilLocale from './el-fil'// element-ui lang

import enLocale from './en'
import zhLocale from './zh'
import twLocale from './tw'
import msLocale from './ms'
import filLocale from './fil_PH'
import jaLocale from './ja_JP'
import ruLocale from './ru_RU'
import idLocale from './id'
import { getStorage } from '@/utils/storage'

Vue.use(VueI18n)

const messages = {
  en_US: {
    ...enLocale,
    ...elementEsLocale
  },
  zh_CN: {
    ...zhLocale,
    ...elementZhLocale
  },
  zh_TW: {
    ...twLocale,
    ...elementZhLocale
  },
  ms_MY: {
    ...msLocale,
    ...elementMsLocale
  },
  id_ID: {
    ...idLocale,
    ...elementIdLocale
  },
  ru_RU: {
    ...ruLocale,
    ...elementRULocale
  },
  ja_JP: {
    ...jaLocale,
    ...elementJaLocale
  },
  fil_PH: {
    ...filLocale,
    ...elementfilLocale
  }
}
export function getLanguage() {
  const userInfo = getStorage('laike_admin_userInfo');
  if(userInfo)
  {
    let storeId = userInfo.storeId;
    console.log(storeId);
    const chooseLanguage = Cookies.get('language'+storeId);
    if(!chooseLanguage){
       let lang = userInfo.lang;
       let storeId = userInfo.storeId;
       updateLanguage(lang,storeId)
       console.log("lang=",lang)
       return lang;
    }
    return chooseLanguage
  }

  const language = (navigator.language || navigator.browserLanguage).toLowerCase()
  const locales = Object.keys(messages)

  for (const locale of locales) {
    if (language.indexOf(locale) > -1) {
      return locale
    }
  }
  return 'zh_CN'
}
const i18n = new VueI18n({
  // set locale
  // options: en | zh | es
  locale: getLanguage(),
  // set locale messages
  messages
})

// 新增函数，用于更新语言
export function updateLanguage(lang, storeId) {
  if(storeId && lang && i18n && i18n.locale)
  {
    // 更新 Cookies 中的语言信息
    Cookies.set('language'+storeId, lang);
    // 更新 VueI18n 的 locale 属性
    i18n.locale = lang;
    // 右上角语言
    sessionStorage.setItem('trcLang'+storeId, lang);
  }
  else
  {
    console.log("lang<",lang,">storeid<"+storeId+">");
  }
}

export default i18n
