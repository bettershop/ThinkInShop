// diy 链接
export const catalogue = {
    "data": [
        {   
            "id":0, // 菜单id 用于 按钮选中高亮的 
            "pid": 0, // 等级
            "type": "link", // 类型
            "name": "商城页面", //标题
            "title": "商城页面", //  对应的国际化
            "expand": true,
            "children": [
                {
                    'pid':1,
                    "id": '0-0',
                    "name": "商城链接",
                    "title": "MallLink",
                    "expand": true,
                    "sonChildren": 
                    [
                        { 
                            "name": "基础链接",
                            "title": "BasicLink",
                            "children": [
                                {
                                    "name": "商城首页",
                                    "title": "MallHome",
                                    "url": '/pages/shell/shell?pageType=home'  //  链接地址
                                },
                                {
                                    "name": "商城分类",
                                    "title": "MallCategory",
                                    "url": '/pages/shell/shell?pageType=allClass'  //  链接地址
                                },
                                { "name": "购物车", "title": "ShoppingCart", "url": "/pages/shell/shell?pageType=shoppingCart" }, 
                                { "name": "退款列表", "title": "RefundList", "url": "/pagesC/afterSale/afterSale" },
                                { "name": "我的订单", "title": "MyOrder", "url": "/pagesB/order/myOrder" },  
                            ]
                        },
                        {
                            "name": "个人中心",
                            "title": "UserCenter",
                            "children": [
                                { "name": "会员中心", "title": "PaidMember", "url": "/pagesB/userVip/memberCenter" }, 
                                { "name": "充值页面", "title": "RechargePage", "url": "/pagesB/myWallet/recharge" },  
                                { "name": "联系客服", "title": "ContactCS", "url": "/pagesB/message/buyers_service/Regular_customer" },  
                                { "name": "提现页面", "title": "WithdrawPage", "url": "/pagesB/myWallet/putForward" }, 
                                { "name": "个人资料", "title": "UserProfile", "url": "/pagesC/my/myInfo" }, 
                                { "name": "地址列表", "title": "AddressList", "url": "/pagesB/address/receivingAddress" },
                                { "name": "收藏页面", "title": "FavoritePage", "url": "/pagesC/collection/collection" }, 
                                { "name": "个人中心", "title": "UserCenter", "url": "/pages/shell/shell?pageType=my" }, 
                                { "name": "会员权益", "title": "MemberBenefits", "url": "/pagesB/userVip/memberCenter" },  
                            ]
                        },
                        // {
                        //     "name": "分销",
                        //     "title": "Distribution",
                        //     "children": [
                        //         { "name": "推广人列表", "title": "PromoterList", "url": "/" },
                        //         { "name": "分销海报", "title": "DistributionPoster", "url": "/" },
                        //         { "name": "我的推广", "title": "MyPromotion", "url": "/" },
                        //     ]
                        // },
                    ]
                },
                {
                    "id": '0-1',
                    "pid": 1,
                    "type": "marketing_link",
                    "name": "营销链接",
                    "title": "营销链接",
                    "expand": true,
                    "sonChildren": [
                        {
                            "name": "优惠活动",
                            "title": "Promotion",
                            "expand": true,
                            "children": [
                                { "name": "我的优惠券", "title": "MyCoupon", "url": "/pagesB/coupon/mycoupon" },
                                { "name": "领取优惠券", "title": "GetCoupon", "url": "/pagesA/shop/coupon" }, 
                            ]
                        },
                        {
                            "name": "秒杀",
                            "title": "Seckill",
                            "children": [
                                { "name": "秒杀列表", "title": "SeckillList", "url": "/pagesB/seckill/seckill?needLogin=1" }
                            ]
                        },
                     
                        {
                            "name": "拼团",
                            "title": "GroupBuy",
                            "children": [
                                { "name": "拼团列表", "title": "GroupBuyList", "url": "/" }
                            ]
                        },
                        {
                            "name": "预售",
                            "title": "PS",
                            "children": [
                                { "name": "预售列表", "title": "PSL", "url": "/pagesC/preSale/goods/goods" }
                            ]
                        },
                        {
                            "name": "积分",
                            "title": "PT",
                            "children": [ 
                                { "name": "积分兑换记录", "title": "PER", "url": "/pagesC/my/myScore?signPlugin=0" },
                                { "name": "积分商城", "title": "PM", "url": "/pagesB/integral/integral?toBack=true&needLogin=1" },
                                { "name": "积分详情", "title": "PD", "url": "/pagesB/regulation/regulation" }
                            ]
                        },
                        
                    ]
                },
                // {
                //     "id": '0-2', 
                //     "pid": 1,
                //     "type": "special",
                //     "name": "专题页",
                //     "title": "专题页",
                //     "expand": true,
                //     "sonChildren": []
                // }
            ]
        },
        {
            "id": 2,
            "pid": 0,
            "type": "product",
            "name": "商品页面",
            "title": "商品页面",
            "expand": true,
            "children": [
                {
                    "id": 8,
                    "pid": 2,
                    "type": "product_category",
                    "name": "商品分类",
                    "title": "商品分类",
                    "expand": true,
                    "children": []
                },
                {
                    "id": 9,
                    "pid": 2,
                    "type": "product",
                    "name": "商品",
                    "title": "商品",
                    "expand": true,
                    "children": []
                },
                {
                    "id": 10,
                    "pid": 2,
                    "type": "seckill",
                    "name": "秒杀商品",
                    "title": "秒杀商品",
                    "expand": true,
                    "children": []
                },
               
                {
                    "id": 12,
                    "pid": 2,
                    "type": "combination",
                    "name": "拼团商品",
                    "title": "拼团商品",
                    "expand": true,
                    "children": []
                },
                {
                    "id": 13,
                    "pid": 2,
                    "type": "integral",
                    "name": "积分商品",
                    "title": "积分商品",
                    "expand": true,
                    "children": []
                }
            ]
        },
        {
            "id": 3,
            "pid": 0,
            "type": "article",
            "name": "文章页面",
            "title": "文章页面",
            "expand": true,
            "children": [
                {
                    "id": 14,
                    "pid": 3,
                    "type": "news",
                    "name": "文章",
                    "title": "文章",
                    "expand": true,
                    "children": []
                }
            ]
        },
        {
            "id": 4,
            "pid": 0,
            "type": "custom",
            "name": "自定义",
            "title": "自定义",
            "expand": true,
            "children": [
                {
                    "id": 15,
                    "pid": 4,
                    "type": "custom",
                    "name": "自定义链接",
                    "title": "自定义链接",
                    "expand": true,
                    "children": []
                }
            ]
        }
    ]
}