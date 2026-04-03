import request from '../https'
let { entries } = Object;

//拼团活动
export const getGroupList = params => {
    return request({
        method: 'post',
        params,
    })
}
// 发货界面信息
export const deliveryView = params => {
    return request({
        method: 'post',
        params
    })
}
export const getSwitch = params => {
    return request({
        method: 'post',
        params,
    })
}
export const startGroup = params => {
    return request({
        method: 'post',
        params,
    })
}
export const deltGroup = params => {
    return request({
        method: 'post',
        params,
    })
}
export const getStore = params => {
    return request({
        method: 'post',
        params,
    })
}
export const getStoreInfo = params => {
    return request({
        method: 'post',
        params,
    })
}

export const addGroup = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
          }
    })
}
 
//开团记录
export const getGroupRecord = params => {
    return request({
        method: 'post',
        params,
    })
}
export const RecordDetails = params => {
    return request({
        method: 'post',
        params,
    })
}
export const delRecord = params => {
    return request({
        method: 'post',
        params,
    })
}
//参团记录
export const getPartRecord = params => {
    return request({
        method: 'post',
        params,
    })
}

export const delPartRecord = params => {
    return request({
        method: 'post',
        params,
    })
}
// 拼团订单
export const orderList = params => {
    return request({
        method: 'post',
        params
    })
}

// 删除订单
export const delOrder = params => {
    return request({
        method: 'post',
        params
    })
}
// 关闭订单
export const closeOrder = params => {
    return request({
        method: 'post',
        params
    })
}

// 获取售后列表
export const getRefundList = params => {
    return request({
        method: 'post',
        params
    })
}
// 拼团设置
export const getSetInfo = params => {
    return request({
        method: 'post',
        params,
    })
}

export const groupSetNow = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
          }
    })
}