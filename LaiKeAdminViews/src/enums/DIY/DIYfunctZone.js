
/**
 * 用于 个人中心 功能模块
 */
const functionalCenter = {
    'living': 'DIYfunctZone.living' ,//主播
    'livingOrder': 'DIYfunctZone.livingOrder', //直播订单
    'goodLiving': 'DIYfunctZone.goodLiving', //赞过的直播
    'flashsale': 'DIYfunctZone.flashsale' ,// 限时折扣
    'presell': 'DIYfunctZone.presell' ,//我的预售
    'distribution': 'DIYfunctZone.distribution' ,// 分销中心 
    'distributionOrder': 'DIYfunctZone.distributionOrder' ,// 分销订单
    'invoiceManagement': 'DIYfunctZone.invoiceManagement',// 发票管理
    'integral': 'DIYfunctZone.integral' ,// 我的兑换
    'auction': 'DIYfunctZone.auction' ,//我的竞拍
    'seconds': 'DIYfunctZone.seconds' ,//秒杀 
    'go_group': 'DIYfunctZone.go_group' ,//拼团订单
    'mch': 'DIYfunctZone.mch' ,//我的店铺 
    'myService': 'DIYfunctZone.myService' ,// 我的客服
    'member': 'DIYfunctZone.member' //会员
}
/**
 * 用于 组件模块 跳转文件路径 
 * DIYfunctZone
 */ 
export  const funCenterByType = (_this) =>{
    const list = []
    for(let key in functionalCenter){
        list.push({
            type: key,
            value:_this.$t(functionalCenter[key]) 
        })
    }
    
    return list 
}
