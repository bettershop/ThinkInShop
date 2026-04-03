// 作用于 '我的页面' 的一些共同的枚举文件  应用场景 ：diy
const functionalCenter = {
    'living': '/pagesD/liveStreaming/anchorCenter' ,//主播
    'livingOrder': '/pagesD/liveStreaming/liveStreamingOrder', //直播订单
    'goodLiving': '/pagesD/liveStreaming/likeTheLiveBroadcast', //赞过的直播
    'flashsale': '/pagesC/discount/discount_my' ,// 限时折扣
    'presell': '/pagesC/preSale/order/myOrder' ,//我的预售
    'distribution': '/pagesA/distribution/distribution_center' ,// 分销中心 
    'distributionOrder': '/pagesB/order/myOrder?status=0&type=FX' ,// 分销订单
    'invoiceManagement': '/pagesB/invoice/invoiceManagement',// 发票管理
    'integral': '/pagesB/integral/exchange' ,// 我的兑换
    'auction': '/pagesA/myStore/MyBidding/MyBidding' ,//我的竞拍
    'seconds': '/pagesB/seckill/seckill_my' ,//我的秒杀 
    'go_group': '/pagesA/group/groupOrder' ,//拼团订单
    'mch': '/pagesA/myStore/myStore' ,//我的店铺 
    'myService': '/pagesB/message/buyers_service/Regular_customer' ,// 我的客服
    'member': '/pagesB/userVip/memberCenter?id=1', //会员
    'bankCard': '/pagesB/myWallet/bankCard' //银行卡
}
/**
 * 用于 组件模块 跳转文件路径
 * parameter: type (转入的文件类型)
 * return filerURL (文件路径-字符串)
 */
function funCenterByType(type){
    const filtUlr = functionalCenter[type]
    return filtUlr 
}

//      0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭 8:待核销
const orderTyle = {
    0:'orderTyle.dfk',//待付款
    1:'orderTyle.wfh',//未发货
    2:'orderTyle.dsh',//待收货
    5:'orderTyle.ywc',//已完成
    7:'orderTyle.ddgb',//订单关闭
    8:'orderTyle.dhx',//待核销 
}
// '' 全部 ||payment(待付款) send(待发货) receipt(待收货) evaluete(待评价) return(售后) write(待核销)"
// 用于订单查询
function queryOrder(orderStatus){
    console.log('orderStatus',orderStatus)
    let  queryOrderType = ''
    if (orderStatus === 0) {
        queryOrderType = "payment";
    } else if (orderStatus === 1) {
        queryOrderType = "send";
    } else if (orderStatus === 2) { 
        queryOrderType = "receipt"; 
    } else if (orderStatus === 5) { 
        queryOrderType = "evaluete"; 
    } 
    return queryOrderType
};
// 返回 订单状态
function getOrderTypeLabel(orderStatus){
    const otderText = orderTyle[orderStatus]
    return otderText || '无效类型'
};



const languageCountryCodeMap = {
  // --- 东亚 ---
  'zh-CN': '+86',      // 中文 (中国大陆)
  'zh-TW': '+886',     // 中文 (中国台湾)
  'zh-HK': '+852',     // 中文 (中国香港)
  'zh-MO': '+853',     // 中文 (中国澳门)
  'ja-JP': '+81',      // 日语 (日本)
  'ko-KR': '+82',      // 韩语 (韩国)
  'vi-VN': '+84',      // 越南语 (越南)
  'th-TH': '+66',      // 泰语 (泰国)
  'id-ID': '+62',      // 印尼语 (印度尼西亚)
  'ms-MY': '+60',      // 马来语 (马来西亚)

  // --- 欧洲 ---
  'en-GB': '+44',      // 英语 (英国)
  'de-DE': '+49',      // 德语 (德国)
  'fr-FR': '+33',      // 法语 (法国)
  'it-IT': '+39',      // 意大利语 (意大利)
  'es-ES': '+34',      // 西班牙语 (西班牙)
  'pt-PT': '+351',     // 葡萄牙语 (葡萄牙)
  'ru-RU': '+7',       // 俄语 (俄罗斯)
  'nl-NL': '+31',      // 荷兰语 (荷兰)
  'sv-SE': '+46',      // 瑞典语 (瑞典)
  'pl-PL': '+48',      // 波兰语 (波兰)
  'tr-TR': '+90',      // 土耳其语 (土耳其)
  'uk-UA': '+380',     // 乌克兰语 (乌克兰)

  // --- 美洲 ---
  'en-US': '+1',       // 英语 (美国)
  'en-CA': '+1',       // 英语 (加拿大)
  'es-MX': '+52',      // 西班牙语 (墨西哥)
  'pt-BR': '+55',      // 葡萄牙语 (巴西)
  'fr-CA': '+1',       // 法语 (加拿大)

  // --- 大洋洲 ---
  'en-AU': '+61',      // 英语 (澳大利亚)
  'en-NZ': '+64',      // 英语 (新西兰)

  // --- 中东与非洲 ---
  'ar-SA': '+966',     // 阿拉伯语 (沙特阿拉伯)
  'ar-EG': '+20',      // 阿拉伯语 (埃及)
  'hi-IN': '+91',      // 印地语 (印度)
  'fr-CI': '+225',     // 法语 (科特迪瓦)
  'af-ZA': '+27',      // 南非荷兰语 (南非)

  // --- 备用/通用语言代码 (用于提高匹配成功率) ---
  // 当找不到 "zh-CN" 时，可以尝试匹配 "zh"
  'zh': '+86',         // 中文 (默认中国大陆)
  'en': '+1',          // 英语 (默认美国)
  'fr': '+33',         // 法语 (默认法国)
  'de': '+49',         // 德语 (默认德国)
  'es': '+34',         // 西班牙语 (默认西班牙)
  'pt': '+351',        // 葡萄牙语 (默认葡萄牙)
  'ru': '+7',          // 俄语 (默认俄罗斯)
  'ar': '+966',        // 阿拉伯语 (默认沙特阿拉伯)
};
module.exports = {
    funCenterByType,
    getOrderTypeLabel,
    queryOrder,
    languageCountryCodeMap
}

