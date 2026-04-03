import request from '../https'

// 获取支付类型列表
export const index = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 获取支付配置参数k
export const paymentParmaInfo = params => {
    return request({
        method: 'post',
        data: params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 设置支付配置参数
export const setPaymentParma = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 设置开启状态
export const setPaymentSwitch = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 编辑支付Icon图标
export const editPayIcon = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

export const uploadIcon = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}