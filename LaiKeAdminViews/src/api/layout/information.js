import request from '../https'
// 公告不弹出
export const notGG = params => {
    return request({
        method: 'post',
        params
    })
}

// 修改用户信息
export const updateAdminInfo = params => {
    return request({
        method: 'post',
        params
    })
}

// 订单通知
export const noticeList = params => {
    return request({
        method: 'post',
        params
    })
}

// 一键已读
export const noticeRead = params => {
    return request({
        method: 'post',
        params
    })
}
// 子菜单切换
export const getButton = params => {
    return request({
        method: 'post',
        params
    })
}

// 获取权限列表
export const getUserRoleInfo = params => {
    return request({
        method: 'post',
        params
    })
}

// 退出登录
export const loginOut = params => {
    return request({
        method: 'post',
        params
    })
}

// 消息弹框已读
export const noticePopup = params => {
    return request({
        method: 'post',
        params
    })
}