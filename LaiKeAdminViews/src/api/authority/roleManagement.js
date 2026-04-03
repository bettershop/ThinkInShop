import request from '../https'
let { entries } = Object;

// 获取管理员角色信息
export const getRoleListInfo = params => {
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

// 删除角色
export const delUserRoleMenu = request.throttle(params => {
    return request({
        method: 'post',
        params
    })
})

// 添加/修改角色
export const addUserRoleMenu = request.throttle(params => {
    return request({
        method: 'post',
        data: params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
})