import request from '../https'

// diy后台首页
export const index = params => {
    return request({
        method: 'post',
        params
    })
}

// 获取轮播图列表
export const bannerIndex = params => {
    return request({
        method: 'post',
        params
    })
}

// 删除轮播图
export const bannerDel = request.throttle(params => {
    return request({
        method: 'post',
        params
    })
})

// 轮播图置顶
export const bannerMoveTop = params => {
    return request({
        method: 'post',
        params
    })
}

// 轮播图上移下移
export const bannerRemove = request.throttle(params => {
    return request({
        method: 'post',
        params
    })
})

// 获取轮播图分类路径
export const bannerPathList = params => {
    return request({
        method: 'post',
        params
    })
}

// 添加轮播图
export const bannerSave = request.throttle(params => {
    return request({
        method: 'post',
        params
    })
})

// 获取ui导航栏列表
export const uiIndex = params => {
    return request({
        method: 'post',
        params
    })
}

// 删除ui导航栏
export const uiDel = request.throttle(params => {
    return request({
        method: 'post',
        params
    })
})

// ui导航栏是否显示开关
export const uiIsShowSwitch = params => {
    return request({
        method: 'post',
        params
    })
}

// ui导航栏上下移动
export const uiMove = params => {
    return request({
        method: 'post',
        params
    })
}

// ui导航栏置顶
export const uiTop = params => {
    return request({
        method: 'post',
        params
    })
}

// 添加/编辑ui导航栏
export const uiSave = params => {
    return request({
        method: 'post',
        params
    })
}

// 获取分类管理列表
export const classIndex = params => {
    return request({
        method: 'post',
        params
    })
}

// 类别上下移动
export const classMove = request.throttle(params => {
    return request({
        method: 'post',
        params
    })
})

// 类别是否显示开关
export const classSwitch = params => {
    return request({
        method: 'post',
        params
    })
}

// 类别置顶
export const classTop = request.throttle(params => {
    return request({
        method: 'post',
        params
    })
})

// 活动管理列表
export const activityList = params => {
    return request({
        method: 'post',
        params
    })
}

// 活动管理上下移动
export const activityMove = request.throttle(params => {
    return request({
        method: 'post',
        params
    })
})

// 删除活动
export const activityDel = request.throttle(params => {
    return request({
        method: 'post',
        params
    })
})

// 活动显示开关
export const activitySwitch = params => {
    return request({
        method: 'post',
        params
    })
}

// 获取商品列表
export const getGoodsList = params => {
    return request({
        method: 'post',
        params
    })
}

// 获取满减活动
export const fullReduction = params => {
    return request({
        method: 'post',
        params
    })
}

// 保存、编辑营销活动
export const activitySave = request.throttle(params => {
    return request({
        method: 'post',
        params
    })
})

// 获取活动商品列表
export const getActGoodsList = params => {
    return request({
        method: 'post',
        params
    })
}

// 获取插件类型
export const getPluginTypeList = params => {
    return request({
        method: 'post',
        params
    })
}

// 活动商品上下移动
export const actGoodsMove = params => {
    return request({
        method: 'post',
        params
    })
}

// 活动商品置顶
export const actGoodsTop = params => {
    return request({
        method: 'post',
        params
    })
}

// 活动商品是否显示
export const actGoodsSwitch = params => {
    return request({
        method: 'post',
        params
    })
}

// 创建DIY
export const diyList = params => {
    return request({
        method: 'post',
        params
    })
}

// 删除DIY
export const delDiy = params => {
    return request({
        method: 'post',
        params
    })
}

// 应用DIY
export const applicationDiy = params => {
    return request({
        method: 'post',
        params
    })
}