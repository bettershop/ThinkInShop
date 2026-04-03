import Cookies from 'js-cookie'
import { getLanguage } from '@/lang/index'
const state = {
  language: getLanguage()
}

const mutations = {
  SET_LANGUAGE: (state, playload) => {
    if(playload && playload.storeid){
      state.language = playload.language
      Cookies.set('language'+playload.storeid, playload.language)
    }
  }
}
const actions = {
  setLanguage({ commit }, {language,storeid}) {
    commit('SET_LANGUAGE', {language,storeid})
  }
}
export default {
  namespaced: true,
  state,
  mutations,
  actions
}
