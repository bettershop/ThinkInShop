import request from '../https'

// 配置列表获取
export const getBottomInfo = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 编辑/添加 配置
export const addBottomInfo = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 删除配置 
export const delBottomById = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}