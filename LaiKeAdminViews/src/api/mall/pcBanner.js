import request from '../https'

// 获取轮播图列表
export const bannerIndexs = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 删除轮播图
export const delBanner = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 轮播图置顶
export const topBannerById = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 添加轮播图
export const addBannerInfo = params => {
    return request({
        method: 'post',
        data:params,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}