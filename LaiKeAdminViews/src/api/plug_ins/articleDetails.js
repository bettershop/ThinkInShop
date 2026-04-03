import request from '../https'
let { entries } = Object;

// 种草文章 详情
export const getInfo = params => {
    return request({
        method: 'post',
        params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
