const state = {
  formData: false,
  vi_formData: false,
}

const mutations = {
  SET_ORDER_STATUS: (state,status) => {
    state.formData = status
  },

  SET_VI_ORDER_STATUS: (state,status) => {
    state.vi_formData = status
  }
}

export default {
  namespaced: true,
  state,
  mutations,
}
