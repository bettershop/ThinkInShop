import request from '../https'
let { entries } = Object;
// 获取系统公告信息
export const getSysNoticeInfo = params => {
    return request({
        method: 'post',
        params
    })
}

// 添加/编辑系统公告
export const addSysNoticeInfo = params => {
    return request({
        method: 'post',
        data: params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 删除系统公告
export const delSysNoticeInfo = params => {
    return request({
        method: 'post',
        params
    })
}

// 获取公告类型
export const getDictionaryInfo = params => {
    return request({
        method: 'post',
        params
    })
}