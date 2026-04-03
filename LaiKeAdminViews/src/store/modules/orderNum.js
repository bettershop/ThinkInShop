import { orderCount } from '@/api/order/orderList'
const state = {
  orderListNum: '', // 订单列表数
  physicalOrderNum: '', // 实物订单列表数
  activityOrderNum: '', // 活动订单列表数
  refundListNum: '', // 退货列表数
  virtualNum: '',//虚拟订单列表数
  ziPeiNum: '',//配送订单数
  secOrderNum: '', // 秒杀订单待发货数
  inOrderNum: '', // 积分订单待发货数

}


const mutations = {
  SET_ORDERLISTNUM: (state, orderListNum) => {
    state.orderListNum = orderListNum
  },
  SET_PHYSICALORDERNUM: (state, physicalOrderNum) => {
    state.physicalOrderNum = physicalOrderNum
  },
  SET_VIRTUALNUM: (state, virtualNum) => {
    state.virtualNum = virtualNum
  },
  SET_ZIPEINUM: (state, ziPeiNum) => {
    state.ziPeiNum = ziPeiNum
  },
  SET_ACTIVITYORDERNUM: (state, activityOrderNum) => {
    state.activityOrderNum = activityOrderNum
  },
  SET_REFUNDLISTNUM: (state, refundListNum) => {
    state.refundListNum = refundListNum
  },

  SET_SECORDERNUM: (state, secOrderNum) => {
    state.secOrderNum = secOrderNum
  },
  SET_INORDERNUM: (state, inOrderNum) => {
    state.inOrderNum = inOrderNum
  },

}

const actions = {
  // 获取订单统计
  async getOrderCount({ commit }) {
    const res = await orderCount({
      api: 'admin.order.orderCount'
    })
    commit('SET_ORDERLISTNUM', res.data.data.orderNum)
    commit('SET_PHYSICALORDERNUM', res.data.data.shiWuNum)
    commit('SET_VIRTUALNUM', res.data.data.VINum)
    commit('SET_ZIPEINUM', res.data.data.ziPeiNum)
    commit('SET_ACTIVITYORDERNUM', res.data.data.activityNum)
    commit('SET_REFUNDLISTNUM', res.data.data.returnNum)
  },

  async getOrderSecCount({ commit }) {
    const res = await orderCount({
      api: 'plugin.sec.order.orderCount'
    })
    commit('SET_SECORDERNUM', res.data.data.secOrderNum)
  },

  async getOrderInCount({ commit }, total) {
    try {
      let res = null
      if (total == null) {
        res = await orderCount({
          // 原来来的
          // api: 'plugin.integral.order.orderCount'
          api: 'plugin.integral.order.index',
        })
      }
      commit('SET_INORDERNUM', total != null ? total : res.data.data.total)
    } catch (error) {
      console.error(error)
      commit('SET_INORDERNUM', 0)
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

