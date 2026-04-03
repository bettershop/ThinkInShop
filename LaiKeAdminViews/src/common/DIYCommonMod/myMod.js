// 我的页面 数据内容填充
const goodsEditorBase = process.env.VUE_APP_VERSION_URL
 
const myCondifg = {
    "key": "homeItem17569763719113",
    "page_name": "我的",
    "iconPath": "https://laikeds.oss-cn-shenzhen.aliyuncs.com/1/8/20250808/1953642482147926017.png",
    "selectedIconPath": "https://laikeds.oss-cn-shenzhen.aliyuncs.com/1/8/20250808/1953642482206646272.png",
    "size": "",
    "url": "/pages/tabBar/my",
    "isDel": false,
    "is_img": true,
    "defaultArray": {
        "L_175697_6543870_g": {
            "configName": "c_personal_center",
            "defaultName": "c_personal_center",
            "name": "personal_center",
            "pointName": "personal_center",
            "icon": "icon-gonggao",
            "cname": "个人中心",
            "timestamp": "L_175697_6543870_g",
            "num": "L_175697_6543870_g",
            "pageConfig": {
            },
            "iconConfig": {
                "kjrk": true,
                "iconURl1": "",
                "iconURl2": "",
                "iconShow1": true,
                "iconShow2": true,
                "iconlocation1": "left",
                "iconlocation2": "left",
                "boxType": 0,
                "boxRoundedVal": 0,
                "backgType": 1,
                "backgColor1": "#FFFFFF",
                "backgColor2": "#FFFFFF",
                "checkList": [
                    "kq",
                    "sc",
                    "jf"
                ],
                "checkListInfo": [
                    {
                        "type": "kq",
                        "name": "卡券"
                    },
                    {
                        "type": "ye",
                        "name": "余额"
                    },
                    {
                        "type": "jf",
                        "name": "积分"
                    },
                    {
                        "type": "sc",
                        "name": "收藏"
                    }
                ]
            },
            "advConfig": {
                "vipShow": true,
                "shopShow": true,
                "boxType": 0,
                "backgType": 0,
                "boxRoundedVal": 15,
                "backgColor1": "#FFFFFF",
                "backgColor2": "#FFFFFF"
            },
            "orderCondfig": {
                "isShow": true,
                "orderTitle": "我的订单",
                "orderList": [
                    {
                        "imgUrl": `${goodsEditorBase}images/DIYICON/grdsh.png`,
                        "type": 0,
                        "name": "待付款"
                    },
                    {
                        "imgUrl": `${goodsEditorBase}images/DIYICON/grdfh.png`,
                        "type": 1,
                        "name": "待发货"
                    },
                    {
                        "imgUrl": `${goodsEditorBase}images/DIYICON/grdsh.png`,
                        "type": 2,
                        "name": "待收货"
                    },
                    {
                        "imgUrl": `${goodsEditorBase}images/DIYICON/my_dpj.png`,
                        "type": 4,
                        "name": "待评价"
                    },
                    {
                        "imgUrl": `${goodsEditorBase}images/DIYICON/tksh.png`,
                        "type": 'del',
                        "name": "售后"
                    }
                ],
                "boxType": 0,
                "backgType": 0,
                "boxRoundedVal": 0,
                "backgColor1": "#FFFFFF",
                "backgColor2": "#FFFFFF"
            },
            "functionConfig": {
                "isShow": true,
                "titleName": "功能中心",
                "list": [
                    {
                        "type": "go_group",
                        "imgUrl": `${goodsEditorBase}images/DIYICON/grwdpt.png`,
                        "name": "我的拼团"
                    },
                    {
                        "type": "seconds",
                        "imgUrl": `${goodsEditorBase}images/DIYICON/grwdms.png`,
                        "name": "我的秒杀"
                    },
                    {
                        "type": "auction",
                        "imgUrl": `${goodsEditorBase}images/DIYICON/grwdjp.png`,
                        "name": "我的竞拍"
                    },
                    {
                        "type": "integral",
                        "imgUrl": `${goodsEditorBase}images/DIYICON/grwddh.png`,
                        "name": "我的兑换"
                    },
                    {
                        "type": "invoiceManagement",
                        "imgUrl": `${goodsEditorBase}images/DIYICON/grfpgl.png`,
                        "name": "发票管理"
                    },
                    {
                        "type": "distribution",
                        "imgUrl": `${goodsEditorBase}images/DIYICON/fenxiao.png`,
                        "name": "分销中心"
                    },
                    {
                        "type": "presell",
                        "imgUrl": `${goodsEditorBase}images/DIYICON/yusho_new.png`,
                        "name": "我的预售"
                    },
                    {
                        "type": "living",
                        "imgUrl": `${goodsEditorBase}images/DIYICON/zbzx_icon.png`,
                        "name": "主播中心"
                    },
                    {
                        "type": "bankCard",
                        "imgUrl": `${goodsEditorBase}images/DIYICON/yhkgl.png`,
                        "name": "银行卡"
                    },
                    {
                        "type": "mch",
                        "imgUrl": `${goodsEditorBase}images/DIYICON/grwddp.png`,
                        "name": "我的店铺"
                    }
                ],
                "boxType": 0,
                "backgType": 0,
                "boxRoundedVal": 15,
                "backgColor1": "#FFFFFF",
                "backgColor2": "#FFFFFF",
                "option":1
            },
            "goodsConfig": {
                "goodsShow": true,
                "layoutType": 1
            },
            "link_key": "L_175697_6543870_g",

        }
    },
    "mConfig": [
        {
            "name": "personal_center",
            "cname": "个人中心",
            "icon": "icon-gonggao",
            "configName": "c_personal_center",
            "defaultName": "c_personal_center",
            "type": 2,
            "props": {
                "index": {
                    "type": null
                },
                "num": {
                    "type": null
                }
            },
            "computed": {
            },
            "watch": {
                "pageData": {
                    "deep": true,
                    "user": true
                },
                "num": {
                    "deep": true,
                    "user": true
                },
                "defaultArray": {
                    "deep": true,
                    "user": true
                }
            },
            "methods": {
            },
            "staticRenderFns": [
                null,
                null,
                null
            ],
            "_compiled": true,
            "_scopeId": "data-v-50ab0e86",
            "beforeCreate": [
                null
            ],
            "beforeDestroy": [
                null
            ],
            "__file": "src/components/mobilePage/personal_center.vue",
            "num": "L_175697_6543870_g",
            "_Ctor": {
            }
        }
    ],
    "type": true
}

export default myCondifg;