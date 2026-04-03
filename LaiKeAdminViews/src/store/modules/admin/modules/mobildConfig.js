
/**
 * diy自定义组件
 * */
import { setCSSVariables } from '@/utils/utils'

import LaiKeCommon from '@/api/common.js'

export default {
    namespaced: true,
    state: {
        configName: '',
        // 已知组件列表默认数据 数组
        defaultArray: {},
        // 全部的链接
        product_links: [],
        pageList: [], // 页面列表(整个页面切换交互的盒子对象)
        feilei_links: [],
        dianpu_links: [],
        tabBarList: [],// tabber 数组 （文字 图片大小）
        tabberinfo: {  // tabber 信息设置 （文字大小 字体  间距）
            fontType: '',//字体
            fontSize: '',//字号
            color: '#1F2937',//字色
            marginBottom: '',
            marginTop: '',
            lingHehig: '',
            iconIsShow: true,
            fontIsShow: true,
        },
        labelList: [], // 主题列别集合 （数据字典信息）
        pagesId: '', // 当前 自定义diy页面 id
        pagesKey: '', // 当前 使用的页面 key (不区分 页面类型)
        pagesListFalg: true, // 通知 页面列表更新 状态，与页面关联动作联动
        pageItem: {}, // 用于获取外部自定义页面
        newActive: [], // 当前选中页面渲染的组件
        mConfig: {}, // 当前选中页面的中间手机渲染的对象
        correlationList: [], //自定义页面点击关联时 生成的数据
        delLinkList: [] //存放 被删除的组件 link
    },
    mutations: {
        SET_DELLINKLIST(state, data) {
            state.delLinkList = data || []
        },
        SET_MCONFIG(state, data) {
            state.mConfig = data
            state.pageList.forEach((item) => {
                if (item.key == state.pageskey) {
                    item.mConfig = data
                }
            })
            console.log(state.pageList, state.pageskey, '当前页面列表')
        },
        SET_PAGESKEY(state, data) {
            state.pageskey = data;
            if (state.pageList.length > 0) {
                console.log('第一次加载页面', state.pageList)

                console.log('key', state.pageskey)
                const pageItem = state.pageList.find(v => {
                    return state.pageskey == v.key
                });
                console.log('当前选中页面的是谁', pageItem)
                if (pageItem.defaultArray) {
                    state.defaultArray = pageItem.defaultArray || {}
                } else {
                    try {
                        const { defaultArray } = JSON.parse(pageItem.page_context)
                        state.defaultArray = defaultArray || {}
                    } catch (e) {
                        state.defaultArray = {}
                        console.error('出问题了：', e)
                    }
                }

                // 针对tabber页面切换做的处理 
                // const tabObj = state.tabBarList.find(v => v.key == pageItem.key)
                // if (!pageItem.mConfig && tabObj) {
                //     pageItem.mConfig = tabObj.mConfig
                // }
                const mConfig = pageItem.mConfig || []

                if (pageItem.mConfig) {
                    state.newActive = mConfig || []
                } else {
                    try {
                        const { mConfig } = JSON.parse(pageItem.page_context)
                        state.newActive = mConfig || []
                    } catch (e) {
                        state.newActive = []
                        console.error('出问题了：', e)
                    }
                }


            } else {
                console.log('列表为空');
            }

        },
        /**
         *  用于显示自定义页面关联列表
         */
        SET_CORRELATIONLIST(state, data) {
            state.correlationList = data
        },

        SET_PAGESLIST(state, data) {
            state.pageList = JSON.parse(JSON.stringify(data)) || [];
            console.log('页面列表更新了', data)

        },
        /**
         * 获取当前使用的 自定义页面id
         * @param {*} state 
         * @param {*} data 
         */
        SET_PAGESID(state, data) {
            state.pagesId = data
        },
        /**
         * 通知 页面列表更新数据
         * @param {*} state 
         * @param {*} data 
         */
        SET_PAGESLISTFALG(state, data) {
            console.log('页面列表更新状态', state.pagesListFalg)
            state.pagesListFalg = !state.pagesListFalg
        },
        /**
         * 获取从外部复制过来的页面 
         */
        ADD_PAGEITEM(state, data) {
            state.pageItem = data
        },
        SET_TABBERINFO(state, data) {
            state.tabberinfo = data;
            console.log('vuex tabberinfo', state.tabberinfo)
            setCSSVariables({
                '--fontColor': data.color,
                '--optColor': data.optColor,
                '--typeFace': data.fontType,
                '--fontSzie': data.fontSize,

                '--lingHehig': `${data.lingHehig}px`,
                '--marginTop': `${data.marginTop}px`,
                '--marginBottom': `${data.marginBottom}px`,

                '--marginLeft': `${data.marginLeft}px`,
                '--marginRight': `${data.marginRight}px`,

                '--colorTwo': `${data.colorTwo}`,
                '--imgurl': `url(${data.imgurl})`
            })
        },
        SET_TABBARLIST(state, data) {
            state.tabBarList = data;
        },
        /**
          * @description 获取DIY类型 数据字典信息
          * @param {Object} state
          * @param {Array} data
          */
        SELECT_LABEL_LIST(state, data) {
            console.log('vuex labelLists', data)
            state.labelList = data || [];
        },
        /**
         * @description 设置当前商品的跳转链接
         * @param {Object} state
         * @param {Array} data
         */
        SET_PRODUCT_LINKS(state, data) {
            state.product_links = data;
        },
        /**
         * @description 设置当前分类的跳转链接
         * @param {Object} state
         * @param {Array} data
         */
        SET_FENLEI_LINKS(state, data) {
            state.feilei_links = data;
        },
        /**
         * @description 设置当前分类的跳转链接
         * @param {Object} state
         * @param {Array} data
         */
        SET_DIANPU_LINKS(state, data) {
            state.dianpu_links = data;
        },
        /**
         * @description 默认配置push到数组里面
         * @param {Object} state vuex state
         * @param {Object} data
         * 把默认数据添加到默认数组里面，解耦重复组件公用一条配置的问题
         */
        ADDARRAY(state, data) {  

            state.defaultArray[data.num] = data.val

            let titleKey = '';
            let titleVal = null;

            //如果有 页面标题 放在第一个位置
            if (data.val.name == 'page_title') {
                titleKey = data.num;
                titleVal = data.val;
            }  
            if (titleKey && titleVal) {
                const newVal = {};
                // 第一步：强制把 page_title 放到新对象的第一个位置
                newVal[titleKey] = titleVal;
                
                // 第二步：遍历原对象，追加其他非空、非 titleKey 的键值对
                let keys = Object.keys(state.defaultArray);
                keys.forEach(key => {
                    // 跳过 page_title 本身 + 空值项
                    if (key !== titleKey && state.defaultArray[key] !== undefined) { 
                        newVal[key] = state.defaultArray[key];
                    }
                }); 
                // 核心修复：把排序后的新对象赋值回 state.defaultArray！
                state.defaultArray = newVal;
                console.log('排序后的对象', newVal);
            }
        },
        /**
         * @description 删除列表第几个默认数据
         * @param {Object} state vuex state
         * @param {Object} data 数据
         */
        DELETEARRAY(state, data) {
            console.log('DELETEARRAY', state.defaultArray, data.num)

            delete state.defaultArray[data.num]
            console.log('删除列表第几个默认数据', state.defaultArray)
            // state.defaultArray.splice(index, 1)
        },
        /**
         * @description 数组排序
         * @param {Object} state vuex state
         * @param {Object} data 位置index记录
         */
        defaultArraySort(state, data) {
            let defaultArray = JSON.parse(JSON.stringify(state.defaultArray))
            console.log(defaultArray, 'defaultArray')
            //  对象转成数组
            function objectToArray(obj) {
                // 检查是否为有效对象
                if (!obj || typeof obj !== 'object' || Array.isArray(obj)) {
                    return [];
                }
                // 转换为包含键值对的数组
                return Object.keys(obj).map(key => ({
                    key: key,
                    value: obj[key]
                }));
            }
            let obj = {}
            let newObj1 = []
            if (Object.keys(defaultArray).length > 0) {
                newObj1 = JSON.parse(JSON.stringify(objectToArray(defaultArray)))
                obj = JSON.parse(JSON.stringify(objectToArray(defaultArray)))[data.oldIndex]

                newObj1.splice(data.oldIndex, 1)
                newObj1.splice(data.newIndex, 0, obj)

                const mergedObj = Object.assign({}, ...newObj1.map(v => ({ [v.key]: v.value })));
                state.defaultArray = JSON.parse(JSON.stringify(mergedObj))
            }



        },
        /**
         * @description 更新数组某一组数据
         * @param {Object} state vuex state
         * @param {Object} data
         */
        UPDATEARR(state, data) {
            console.log(22222, data.val, data.num, state.defaultArray)

            state.defaultArray[data.num] = data.val
            let value = Object.assign({}, state.defaultArray);
            state.defaultArray = value;
            console.log('更新后的数组', state.defaultArray)
        },
        /**
         * @description 保存组件名称
         * @param {Object} state vuex state
         * @param {string} data
         */
        SETCONFIGNAME(state, name) {
            state.configName = name;
        },
        /**
         * @description 默认组件清空
         * @param {Object} state vuex state
         * @param {string} data
         */
        SETEMPTY(state, name) {
            state.defaultArray = {};
        },
        /**
         * 页面离开时 清空数据
         */
        DELDATA(state) {
            state.defaultArray = {};
            state.pageList = [];
            state.pagesKey = '';
            state.tabBarList = []
            state.newActive = [];
            state.mConfig = {};
            state.correlationList = []
            state.delLinkList = []
        }
    },
    actions: {
        getData({ commit }, data) {
            console.log(data, 'actions')
        },
        // 用于 获取外部自定义页面
        async bindPages({ state, commit }, data) {
            // 
            /**
             * unit   组件单位标识
             * lodValue 原始数值
             *  linkKey  页面key
             * state.pagesId, //页面id
             */
            const { diyId, link, linkKey, unit, typeIndex, lodValue } = data
            console.log(data)

            let data1 = {
                api: 'plugin.template.AdminDiy.getPageJson',
                diyId: diyId, //主题id
                link: link, //链接 
            }

            const res = await LaiKeCommon.select(data1)

            if (res.data.code == 200) {
                try {
                    if (res.data.data) {

                        let item = res.data.data
                        item.type = false
                        item.key = item.page_key
                        item.pagesId = item.id
                        const page_context = JSON.parse(item.page_context)
                        if (item.page_context) {
                            const page_context = JSON.parse(item.page_context)
                            item.defaultArray = page_context.defaultArray
                            item.mConfig = page_context.mConfig
                        } else {
                            item.defaultArray = {}
                            item.mConfig = {}
                        }


                        delete item.page_context // 删除原有的page_context 
                        console.log('state.pageList', state.pageList)
                        console.log('lodValue', item)
                        const index = state.pageList.findIndex(v => (v.id || v.pagesId) == item.id)
                        console.log('lodValue', index)

                        if (index < 0) {
                            commit('ADD_PAGEITEM', item)
                            commit('SET_PAGESLISTFALG', null)
                        }
                    }
                } catch (e) {

                }
            }
        },
    },
}
