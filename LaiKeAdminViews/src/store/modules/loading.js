const state = {
    loadings: false,
  }
  
  const mutations = {
    SET_LOADING: (state) => {
      state.loadings = !state.loadings
    }
  }
  
  export default {
    namespaced: true,
    state,
    mutations,
  }