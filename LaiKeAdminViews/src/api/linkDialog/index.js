 
import request from '../https'
let { entries } = Object;

// 链接弹窗 获取 平台商品分类
export const getAdminLinkClassAll = (params) => {
    return request({ 
        method: "post",
        params:{
            api: 'admin.goods.getClassInfo',
            level:0,
            pageNo:1,
            pageSize:100,
            ...params
        },
    });
};
// 链接弹窗 获取 平台商品分类
export const getAdminLinkGoodAll = (params) => {
    return request({
        method: "post",
        params: {
            api: 'admin.goods.index', 
            pageSize: 10,
            ...params
        },
    });
};

// 链接弹窗 获取 积分商品分类
export const getAdminLinkPointsGoodAll = (params) => {
    return request({
        method: "post",
        params: {
            api: 'plugin.integral.AdminIntegral.index',
            pageSize: 10,
            ...params
        },
    });
};

