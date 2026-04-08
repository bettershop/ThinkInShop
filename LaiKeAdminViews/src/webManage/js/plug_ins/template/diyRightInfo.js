
import { mixinstest } from '@/mixins/index'
import { mapMutations, mapState } from 'vuex'
import vuedraggable from 'vuedraggable'
import popupMenu from '@/views/plug_ins/template/popupMenu.vue'
import LaiKeCommon from '@/api/common.js'
import diyModel from '@/common/DIYCommonMod'
import { addDiyInfo } from '@/api/diy/index'
export default {
    name: 'diyRightInfo',
    mixins: [mixinstest],
    props: {
        tabbar: {
            type: Array,
            default: () => []
        }
    },
    data() {
        return {
            showComponentIcons: true,
            pagesLink: '',
            relevanceIndex: '',
            relevanceHover: '',
            statusType: '',
            bindDiyList: [],
            bindDiyListTwo: [],
            activeName: 'generalSett',
            querpageItem: 0, // 当前选中页面的索引
            preSelection: '',
            noeFalg: true,
            queryFalg: true,
            tabbarModFalg: false,
            dialogFormVisible: false,
            systemSettingsFalg: false,
            dialogPages: false,
            loading: false,
            formLabelWidth: '120px',
            color: '',
            queryPageName: '',
            createdFrom: {},
            createdFromTwo: {},
            tabberInfo: {
                fontType: 'Microsoft YaHei',
                fontSize: '10px',
                backType: 0,  //1 颜色 2   图片 
                imgurl: '',
                color: 'rgba(135,131,131,1)',
                optColor: 'rgba(11,11,11,1)',
                ficonSize: 'small',
                marginTop: 0,
                marginRight: 0,
                marginBottom: 0,
                marginLeft: 0,
                lingHehig: 15,

                colorTwo: '#f4f5f6',
                textColor: '#000',

                tabbarShow: false, // 是否显示 tabber 导航栏
                iconIsShow: true,// 图标是否显示
                fontIsShow: true,

            },
            pointTo: '',
            tabBarList: [],
            pageList: [],
            tableData: [{}],
            fontTypeList: [
                { name: '苹方', value: 'PingFang SC' },
                { name: '中易宋体', value: 'SimSun' },
                { name: '微软雅黑', value: 'Microsoft YaHei' },
                { name: '思源黑体', value: 'Source Han Sans' },
            ],
            fontSizeList: [
                { value: '10px', disabled: false },
                { value: '12px', disabled: false },
                { value: '14px', disabled: false },
                { value: '16px', disabled: false },  // 禁用16px
                { value: '18px', disabled: false },
                { value: '24px', disabled: false }],
            input1: '',
            disable: true,

            themeType: "",
            newPage: {
                pageName: '',
            },
            dialogVisible: false,
            rules: {
                pageName: [
                    { required: true, message: this.$t('template.pageList.qsrymcm'), trigger: 'blur' },
                ],
                name: [
                    { required: true, message: this.$t('template.divTemplate.qsr'), trigger: 'blur' }
                ],
                theme_type_code: [
                    { required: true, message: this.$t('template.divTemplate.qxz'), trigger: 'change' }
                ]
            },
            pageList1: [], // 页面列表查询结果
            selectType: 'all',
            typeIndex: 2, // 1 系统 、2 自定义
            isLkt: false
        }
    },
    components: {
        draggable: vuedraggable,
        popupMenu,
    },
    computed: {
        // labelList 主题列表集合
        ...mapState("admin/mobildConfig", ['labelList', 'correlationList', 'pagesListFalg', 'pageItem']),

        // 渲染间距 方位 间距值
        marginPointTo: {
            get() {
                return this.tabberInfo[`margin${this.pointTo}`]
            },
            set(val) {
                this.tabberInfo[`margin${this.pointTo}`] = val
            }
        },

    },
    watch: {
        '$store.state.admin.mobildConfig.pagesListFalg'(newVal) {
            console.log('pagesListFalg', this.pagesListFalg)
            this.pageList.push(this.pageItem)
            this.pageList1.push(this.pageItem)
        },
        bindDiyList: {
            handler(newVal) {
                console.log('newValnewValnewVal', newVal)
                this.bindDiyListTwo = JSON.parse(JSON.stringify(newVal))
            },
            deep: true,
            immediate: true
        },
        'tabberInfo.ficonSize'(newVal) {
            if (newVal == 'big') {
                this.tabberInfo.fontSize = '10px'
                this.fontSizeList.forEach((item, index) => {
                    if (index <= 1) {
                        item.disabled = false
                    } else {
                        item.disabled = true
                    }
                })
            }
            if (newVal == 'small') {
                this.fontSizeList.forEach((item, index) => {
                    item.disabled = false
                })
            }
            if (newVal == 'medium') {
                this.fontSizeList.forEach((item, index) => {
                    if (index <= 3) {
                        item.disabled = false
                    } else {
                        item.disabled = true
                    }
                })
            }
        },
        tabBarList: {
            handler(newVal) {
                console.log('tabBarList', JSON.parse(JSON.stringify(newVal)))
                if (newVal && newVal.length == 5) {
                    this.disable = true
                }
                // 存储 tabber 信息用于最接口终提交
                this.SET_TABBARLIST(newVal)

                // status  代表着 是 tabber 页面
                if (newVal.length > 0) {
                    const pageList = this.$store.state.admin.mobildConfig.pageList
                    const arr = pageList.filter(item => !item.type)
                    // 将 tabber 信息 同步到页面列表中
                    this.pageList = [...newVal.map(item => ({ ...item, type: true })), ...arr]
                    let arr1 = this.pageList.filter(item => {
                        if (typeof item.isShow != 'boolean') {
                            return item
                        }
                    })
                    console.log('arr1', JSON.parse(JSON.stringify(arr1)))
                    this.pageList1 = JSON.parse(JSON.stringify(arr1))
                }
            },
            deep: true
        },
        pageList: {
            handler(newVal) {
                // 更新页面列表 用于后续编辑交互
                console.log('newValnewVal', JSON.parse(JSON.stringify(newVal)))
                this.SET_PAGESLIST(newVal.length > 0 ? newVal : [])
            },
            deep: true,
        },
        tabberInfo: {
            handler(newVal) {
                this.SET_TABBERINFO(newVal)
            },
            deep: true,
            immediate: true
        },
        tabbar: {
            handler(newVal) {
                if (newVal.length > 0) {
                    this.tabBarList = newVal

                } else {
                    const { homeMod, goodClassMod, myMod, cartMod } = diyModel
                    const data = [
                        homeMod,
                        goodClassMod,
                        cartMod,
                        myMod
                    ]

                    this.tabBarList.push(...data)
                }

            },
            deep: true,
        }

    },
    async created() {
        const { createdFrom, themeType, typeIndex, status } = this.$route.query;
        this.statusType = status
        if (createdFrom) {

            this.createdFrom = JSON.parse(createdFrom) // 主题描述信息
            const obj = this.labelList.find(v => v.id == this.createdFrom.theme_type_code)
            this.createdFrom.theme_type_name = obj.name
        }

        themeType && (this.themeType = themeType) // 主题类别（字典id）
        typeIndex && (this.typeIndex = parseInt(typeIndex)) // 1 系统 、2 自定义

        this.isLkt = process.env.VUE_APP_IS_LKT || true
        if (this.$route.query.pageKeyId) {
            this.activeName = 'pageList'
        }
    },
    mounted() {

        console.log('createdFromcreatedFromcreatedFrom=>', this.createdFrom)
    },

    methods: {
        ...mapMutations("admin/mobildConfig", ["SET_TABBARLIST", 'SET_TABBERINFO', 'SET_PAGESID', 'SET_PAGESKEY', 'SET_PAGESLIST', 'SET_CORRELATIONLIST']),
        UpBackType(event) {
            const type = event.target.dataset.type
            if ([0, 1].includes(Number(type))) {
                this.$set(this.tabberInfo, 'backType', Number(type))
            }
        },
        odalPicTap(type, index) {
            console.log('index', index)
            this.modalPic = true;
            this.orderListIndex = index
            this.functionIndex = ''
        },
        modalPicTap1(type, index) {
            console.log('index11', index)

            this.modalPic = true;
            this.orderListIndex = ''
            this.functionIndex = index
        },
        selectedIconImg(value, data) {
            const index = this.tabBarList.findIndex(v => v.selectedIconPath == value)
            this.$set(this.tabBarList[index], 'relative_selectedIconPath', data.image_name)
        },

        selectIconPathImg(value, data) {
            console.log(data)
            const index = this.tabBarList.findIndex(v => v.iconPath == value)
            this.$set(this.tabBarList[index], 'relative_iconPath', data.image_name)

        },
        quyerDiyItembyName() {
            const arr = this.bindDiyList.filter(v => v.name.includes(this.diyName))
            this.bindDiyListTwo = JSON.parse(JSON.stringify(arr))
        },
        // 提供公共方法获取   用于其他组件访问 实现最终提交 滚动到未设置好的tabbat-item 元素上
        getTabbarElement(pageIndex, refMod) {
            this.$nextTick(() => {
                // 如果没有打开导航区域 则自动打开 在金星滚动定位
                if (!this.tabbarModFalg) {
                    this.queryTabbarModInfoFalg()
                }
                const tabbarBox = this.$refs[refMod];
                // 通过 $el 获取组件的根 DOM 元素
                const scrollableEl = tabbarBox.$el || tabbarBox;
                console.log('scrollableEl', scrollableEl)

                // 获取第一个子元素的完整高度
                const firstChild = scrollableEl.querySelector('.item');
                if (firstChild) {
                    const rect = firstChild.getBoundingClientRect();
                    const style = window.getComputedStyle(firstChild);
                    // 获取元素高度 + 上下外边距
                    const fullHeight = rect.height +
                        parseFloat(style.marginTop) +
                        parseFloat(style.marginBottom);

                    // 滚动到这个高度
                    this.$ScrollTop(scrollableEl, {
                        to: pageIndex * fullHeight,
                        time: 2500
                    })
                }
            })
        },
        //
        upTBarIcon() {
            console.log(this.tabberInfo.ficonSize)
            this.tabBarList.forEach(item => {
                item.size = this.tabberInfo.ficonSize
            })
        },
        toUp() {
            this.$store.commit('admin/mobildConfig/DELDATA')
            this.$router.push('/plug_ins/template/divTemplate')
        },
        SubmitEvent() {
            if (this.isLkt) {
                this.ok();
            } else {
                // 如果是客户身份 操作系统主题时
                if (this.typeIndex == 1) {
                    this.editDiyinfo()
                } else {
                    this.ok()
                }
            }
        },
        // 页面保存接口
        async PageSubmitEvent() {
            const { query } = this.$route
            let resut = await new Promise(resolve => {
                this.$emit("getPageImg", resolve)
            })
            const defaultArray = this.$store.state.admin.mobildConfig.defaultArray
            const mConfig = this.$store.state.admin.mobildConfig.mConfig
            const res = await this.LaiKeCommon.edit({
                api: 'plugin.template.AdminDiy.addOrUpdateDiyPage',
                id: query.pageId,
                link: query.link, // 页面key
                url: resut,// 图片地址
                page_context: JSON.stringify({ defaultArray, mConfig }), //保存内容
            }, true)
            console.log('res', res)
            if (res.data.code == 200) {
                this.succesMsg(res.data.message)
                setTimeout(() => {
                    this.$router.go(-1)
                }, 1500)
            } else {
                this.errorMsg(res.data.message)
            }

        },
        ok() {
            this.$emit('upAddLoin')

            // 保存当前页面信息
            if (this.pageList && this.pageList.length > 0) {
                const pageIndex = this.pageList.findIndex(v => {
                    return this.$store.state.admin.mobildConfig.pageskey == v.key
                });

                this.$set(this.pageList[pageIndex], 'defaultArray', this.$store.state.admin.mobildConfig.defaultArray)
                this.$set(this.pageList[pageIndex], 'mConfig', this.$store.state.admin.mobildConfig.mConfig)
            }
            console.log('最终保存的结果', this.pageList)

            this.$emit("ok")
        },
        updataColor() {
            console.log(this.color)

            this.tabberInfo.color = `#${this.color}`
        },
        // 间距 方位渲染
        orientation(text) {
            this.pointTo = text
            const fx = {
                Top: '上',
                Right: '右',
                Bottom: '下',
                Left: '左',
            }
            // 设置 CSS 变量 通过伪类元素 配合 改变组件 方向文案显示
            this.$el.style.setProperty('--prefix-text', `'${fx[text]}'`);
        },
        queryType(val) {
            this.selectType = val
            this.quyerPageItembyName()
        },
        // 页面列表查询
        quyerPageItembyName() {

            console.log(this.pageList)
            let type = ''
            if (this.selectType == 'all') {
                // 全部页面类型查询
                type = ''
            } else if (this.selectType == 'sys') {
                // 系统页面 查询
                type = true
            } else if (this.selectType == 'cust') {
                // 自定义页面查询
                type = false
            }
            if (type === '') {

                this.pageList1 = this.pageList.filter(item => item.page_name.includes(this.input1) && typeof item.isShow != 'boolean')
            } else {

                this.pageList1 = this.pageList.filter(item => item.page_name.includes(this.input1) && (item.type || false) === type && typeof item.isShow != 'boolean')
            }

        },

        handleDragEnd(evt) {
            console.log(evt)
            const indexA = evt.oldIndex
            const indexB = evt.newIndex
            let arr = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.tabBarList));

            const items = this.$refs.tabbarBox.$el.querySelectorAll('.item');
            console.log('items ', items)
            // 遍历所有元素，获取它们的位置信息
            const keyPages = Array.from(items).map((item, index) => {
                const pageKey = item.dataset
                return pageKey.pagekey
            })
            console.log('ketPages', keyPages)
            const arr1 = keyPages.map((key, index) => {
                return arr.find(item => item.key === key)
            })
            console.log('排序后的数组:', arr1);
            // 将交换后的数组赋值给目标数组
            this.SET_TABBARLIST(arr1)
            this.tabbarList = this.$store.state.admin.mobildConfig.tabBarList;
        },
        textAndIconIsShow({ target }) {
            const dom = target.closest('[data-type]');
            if (!dom) return;

            const type = dom.getAttribute('data-type');

            // 重置所有状态
            this.tabberInfo.fontIsShow = false;
            this.tabberInfo.iconIsShow = false;
            console.log(type)
            // 根据点击类型设置对应状态
            switch (type) {
                case 'font':
                    this.tabberInfo.fontIsShow = true;
                    this.tabberInfo.tabbarShow = false;
                    break;
                case 'icon':
                    this.tabberInfo.iconIsShow = true;
                    this.tabberInfo.tabbarShow = false;
                    break;
                case 'fontAndIcon':
                    this.tabberInfo.fontIsShow = true;
                    this.tabberInfo.iconIsShow = true;
                    this.tabberInfo.tabbarShow = false;
                    break;
                case 'none':
                    this.tabberInfo.tabbarShow = true;
            }
        },
        // 主题信息的 显示与隐藏
        queryInfoFalg(number = 0) {
            this.queryFalg = !this.queryFalg
            this.tabbarModFalg = false
            this.systemSettingsFalg = false

            if (number != 1 && !this.systemSettingsFalg && !this.tabbarModFalg) {
                this.queryFalg = true
            }
        },
        // 系统设置的 显示与隐藏
        querySystemSettingsFalg() {
            this.systemSettingsFalg = !this.systemSettingsFalg
            this.tabbarModFalg = false
            this.queryFalg = false
            if (!this.systemSettingsFalg && !this.tabbarModFalg) {
                this.queryFalg = true
            }
        },
        // 导航模块的 显示与隐藏
        queryTabbarModInfoFalg() {
            this.tabbarModFalg = !this.tabbarModFalg
            this.queryFalg = false
            this.systemSettingsFalg = false
            if (!this.systemSettingsFalg && !this.tabbarModFalg) {
                this.queryFalg = true
            }
        },
        maxValuelengt(index) {
            // 一般建议每个 tab 的文字不超过 5 个汉字或 10 个英文字符
            const value = this.tabBarList[index].page_name
            let byteLength = this.getByteLength(value);

            if (byteLength > 10) {
                // 截断到最大字节长度
                this.tabBarList[index].page_name = this.truncateToMaxBytes(value);
            }
        },
        // 限制可输入直接程度
        truncateToMaxBytes(str) {
            let byteLength = 0;
            let truncated = '';

            for (let char of str) {
                const charByteLength = char.codePointAt(0) > 127 ? 2 : 1;

                if (byteLength + charByteLength <= 10) {
                    truncated += char;
                    byteLength += charByteLength;
                } else {
                    break;
                }
            }

            return truncated;
        },
        cancelPage() {
            this.$refs.ruleForm.clearValidate();
            this.dialogVisible = false
        },
        // 计算字节长度（中文2字节，英文1字节）
        getByteLength(str) {
            if (!str) return 0;
            // 匹配所有非 ASCII 字符（中文、符号等）
            const nonAsciiChars = str.match(/[^\x00-\x7F]/g);
            const asciiLength = str.length;
            const nonAsciiLength = nonAsciiChars ? nonAsciiChars.length : 0;
            return asciiLength + nonAsciiLength; // 总字节数 = 字符数 + 非 ASCII 字符数
        },
        editDiyinfo() {
            this.createdFromTwo = JSON.parse(JSON.stringify(this.createdFrom))
            console.log(this.createdFromTwo)
            this.dialogFormVisible = true

        },

        resetForm() {
            this.$refs.createdFromRef.clearValidate();
            this.dialogFormVisible = false
        },
        editCoupons(formName) {

            // 修改主题备注信息
            this.$refs[formName].validate(async (valid) => {
                if (valid) {
                    await addDiyInfo({
                        api: 'plugin.template.AdminDiy.AddOrUpdateDiy',
                        name: this.createdFromTwo.name,
                        remark: this.createdFromTwo.remark,//备注
                        theme_type: this.themeType, //主题类型  1 系统 、2 自定义
                        id: this.$route.query.id, // 主题id
                        theme_type_code: this.createdFromTwo.theme_type_code // 主题类别（字典id）
                    }).then(res => {
                        if (res.data.code == '200') {
                            this.dialogFormVisible = false
                            this.createdFrom = JSON.parse(JSON.stringify(this.createdFromTwo))
                            const obj = this.labelList.find(v => v.id == this.createdFromTwo.theme_type_code)

                            this.createdFrom.theme_type_name = obj.name
                            this.succesMsg(this.$t('distribution.xgcg'))

                            /** 有点忘记需求是什么来，不知道这里为什么还要调用 ok接口
                             * 系统主题 且不是来客推身份
                            */
                            if (this.typeIndex == 1 && !this.isLkt) {
                                this.ok()
                            }
                        } else {
                            // this.warnMsg(res.data.message)
                        }
                    })
                }
            })
        },
        // 添加tabBer
        addTabBerItem() {
            const tabBarList = this.$store.state.admin.mobildConfig.tabBarList
            if (tabBarList.length == 5) {
                this.warnMsg('最多只能配置五条')
                return
            }
            console.log(tabBarList)
            this.tabBarList = tabBarList
            const time = new Date().getTime()

            this.tabBarList.push({
                id: this.tabBarList.length,
                page_name: '',
                iconPath: '', //未选中图标
                selectedIconPath: '', //选中的图标
                size: '', // 图标尺寸
                isDel: true,
                is_img: true
            })
            for (let i = 0; i < this.tabBarList.length; i++) {
                const item = this.tabBarList[i]
                if (!item.key) {
                    this.$set(item, 'key', `homeItem${time}0`)
                }
            }

        },

        /** 自定义页面 关联, 查找页面之间的引用
         * @pageItem 包含页面相关信息
         */
        relevance(pageItem, index) {
            // this.dialogPages = true
            console.log(this.correlationList, 'correlationList')
            const pagesLink = pageItem.link
            if (this.correlationList && this.correlationList.length > 0 && this.pagesLink == pagesLink) {
                this.SET_CORRELATIONLIST([])
                return
            }
            this.pagesLink = pagesLink
            const pageList = this.$store.state.admin.mobildConfig.pageList
            console.log('pagesKey', pagesLink)
            console.log('pagesKey', pageList)
            let arr = []
            console.log(this.pageList)
            pageList.forEach(({ defaultArray, page_name, key }) => {
                for (let comkey in defaultArray) {
                    const pageComponent = JSON.stringify(defaultArray[comkey])
                    if (pageComponent.includes(pagesLink)) {
                        arr.push({
                            pageName: page_name,// 页面名称
                            key: key, // 页面key
                            pageComKey: comkey, // 页面其中的组件key
                            pageComName: defaultArray[comkey].name, //页面其中的组件名称
                            icon: defaultArray[comkey].icon || '', // 页面其中的组件图标
                            pointName: defaultArray[comkey].pointName || '', // 页面其中的组件名称
                        })
                    }
                }
            })
            console.log('包含的页面', arr)
            if (arr.length > 0) {
                this.relevanceIndex = index

            }
            this.SET_CORRELATIONLIST(arr || [])
        },
        // 自定义页面 获取列表接口
        async getBindPageList() {
            const res = await LaiKeCommon.select({
                api: 'plugin.template.AdminDiy.getBindPageList',
                id: this.$route.query.id,
            })
            console.log('页面列表 ==>', res)
            if (res.data.code == 200) {
                if (res.data.data.list && res.data.data.list.length > 0) {
                    res.data.data.list.forEach(item => {
                        // 只定义页面类型
                        this.$set(item, 'type', false)
                        this.$set(item, 'key', item.page_key)
                        this.$set(item, 'pagesId', item.id)
                        if (item.page_context) {
                            const page_context = JSON.parse(item.page_context)
                            this.$set(item, 'defaultArray', page_context.defaultArray)
                            this.$set(item, 'mConfig', page_context.mConfig)
                        } else {
                            this.$set(item, 'defaultArray', {})
                            this.$set(item, 'mConfig', {})
                        }
                        delete item.page_context // 删除原有的page_context
                    })
                }
            }
            // 就是应该放在外面  如果接口出错了 会使用 try捕获抛出 继续执行流程
            this.pageList = []
            const tabPage = this.tabBarList.map(item => ({ ...item, type: true }))
            try {
                this.pageList.push(...tabPage, ...(res.data.data.list || []))
            } catch (e) {
                console.error('获取之定义页面出问题了:', e)
                this.pageList.push(...tabPage)
            }
            console.log('过滤后的页面1111', this.pageList)


            let arr = this.pageList.filter(item => {
                if (typeof item.isShow != 'boolean') {
                    return item
                }
            })
            this.pageList1 = JSON.parse(JSON.stringify(arr))
            setTimeout(() => {
                if (this.$route.query.pageKeyId) {
                    this.querpageItem = this.$route.query.pageKeyId
                    this.SET_PAGESKEY(`${this.querpageItem}`)
                } else {
                    this.querpageItem = this.pageList1[0].key
                    this.SET_PAGESKEY(`${this.pageList1[0].key}`) // 设置当前使用第一页的页面key
                }
                this.relevance({ link: this.querpageItem })


            }, 1500)


        },
        // 自定义页面 删除方法
        delPageItem(item) {
            console.log('item', item)
            this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.LaiKeCommon.del({
                    api: 'plugin.template.AdminDiy.delDiyPage',
                    id: item.pagesId
                }).then(res => {
                    if (res.data.code == 200) {
                        this.$message({
                            type: 'success',
                            message: this.$t('template.pageManafenebt.delSuccess'),
                            offset: 100
                        });
                        const arr = this.pageList.filter(v => v.pagesId != item.pagesId)
                        this.pageList = JSON.parse(JSON.stringify(arr))
                        const list = this.pageList1.filter(v => v.pagesId != item.pagesId)
                        this.pageList1 = list
                    }
                })

            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },
        //选择一页多少条
        handleSizeChange(e) {
            this.loading = true
        },

        //点击上一页，下一页
        handleCurrentChange(e) {
            this.loading = true

        },
        // 添加自定义页面
        addPagesItem() {
            this.newPage.pageName = ''
            this.dialogVisible = true
        },
        // 创建自定义页面
        addTabber(formName) {
            this.$refs[formName].validate(async (valid) => {
                if (valid) {
                    const time = new Date().getTime()

                    const res = await this.LaiKeCommon.select({
                        api: 'plugin.template.AdminDiy.addOrUpdateDiyPage',
                        page_name: this.newPage.pageName,
                        link: `/pages/tabBar/blankpage?page_key=pageItem${time}0`,
                        page_key: `pageItem${time}0`,
                        page_type: this.$route.query.typeIndex
                    })
                    if (res.data.code == 200) {
                        try {
                            this.dialogVisible = false
                            this.pageList.push({
                                pagesId: res.data.data.id,
                                page_name: this.newPage.pageName,
                                key: `pageItem${time}0`,
                                url: '/pages/tabBar/blankpage',
                                link: `/pages/tabBar/blankpage?page_key=pageItem${time}0`,
                                defaultArray: {}
                            })
                            let arr = this.pageList.filter(item => {
                                if (typeof item.isShow != 'boolean') {
                                    return item
                                }
                            })
                            this.pageList1 = JSON.parse(JSON.stringify(arr))
                            // 系统页面 除了 首页 和 第五个页面 其他的都不能被修改

                        } catch (e) {
                            console.error(e)
                        }
                    }
                }
            });
        },
        // 切换页面时
        queryPage(item, falg = true) {
            // 保存当前页面信息 随后在进行切换
            const pageIndex = this.pageList.findIndex(v => {
                return (this.$store.state.admin.mobildConfig.pageskey || item.key) == v.key
            });
            console.log('pageIndex', pageIndex)
            if (pageIndex >= 0) {
                const defaultArray = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray))
                const mConfig = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.mConfig))
                this.$set(this.pageList[pageIndex], 'defaultArray', defaultArray || {})
                this.$set(this.pageList[pageIndex], 'mConfig', mConfig)
            }
            // 保存当前页面信息
            this.upPageInfo(item)
            if (falg) {
                this.querpageItem = item.key
                console.log(this.querpageItem, 'querpageItem')
                this.SET_CORRELATIONLIST([])
            }
            // 设置当前使用的页面key
            this.SET_PAGESID(item.pagesId || '')
            this.SET_PAGESKEY(`${item.key || item.id}`)

            this.$emit('page-selected', item);
        },
        // 系统设置改变背景色
        changeColor(e, color) {
            this.tabberInfo.colorTwo = e

            this.$emit('changeColor', { color: e })
        },

        // 系统设置改变文字颜色
        changeTextColor(e) {
            let pageList = this.$store.state.admin.mobildConfig.pageList
            this.tabberInfo.textColor = e
            pageList.forEach(v => {
                if (v.defaultArray && Object.keys(v.defaultArray).length > 0) {
                    for (const key in v.defaultArray) {
                        if (v.defaultArray[key].titleColor && v.defaultArray[key].titleColor.title == '标题颜色') {
                            const colorObj = v.defaultArray[key].titleColor
                            colorObj.color.forEach((v, i) => {
                                if (v.item == colorObj.default[i].item) {
                                    v.item = e
                                    colorObj.default[i].item = e
                                }
                            })
                        }
                    }
                }
            })

        },
        resetBgA() {
            this.tabberInfo.color = `rgba(135,131,131,1)`
        },
        resetBgB() {
            this.changeTextColor('#000')
        },
        resetBgC() {
            this.changeColor(`#fff`)
        },
        // 删除 tabBar 元素
        delTabber(index) {
            // 最少需要有一个 导航
            if (this.tabBarList && this.tabBarList.length == 1) {
                this.$message({
                    message: this.$t('template.pageManafenebt.tabBarIndex'),
                    type: 'warning',
                    offset: 100
                })
                return
            }
            this.$confirm(this.$t('template.pageManafenebt.delTabText'), this.$t('template.pageManafenebt.ts'), {
                confirmButtonText: this.$t('template.pageManafenebt.qr'),
                cancelButtonText: this.$t('template.pageManafenebt.qx'),
                type: 'warning'
            }).then(() => {
                this.tabBarList.splice(index, 1)
                this.$message({
                    type: 'success',
                    message: '删除成功!',
                    offset: 100
                });
            }).catch(() => {

            });
        },
        // 修改页面组件内容
        async upPageInfo(item) {
            let resut = ''
            let pagesId = this.$store.state.admin.mobildConfig.pagesId
            if (pagesId) {
                resut = await new Promise(resolve => {
                    this.$emit("getPageImg", resolve)
                })
                this.pageList.forEach(page => {
                    if (page.key == pagesId) {
                        this.$set(page, 'pagesImg', resut)
                    }
                })
                this.pageList1.forEach(page => {
                    if (page.key == pagesId) {
                        this.$set(page, 'pagesImg', resut)
                    }
                })
                const pageItem = this.pageList.find(page => {
                    return page.pagesId == pagesId
                })
                console.log(pageItem, 'pageItem')

                const data = {
                    defaultArray: pageItem.defaultArray,
                    mConfig: pageItem.mConfig
                }
                this.LaiKeCommon.edit({
                    api: 'plugin.template.AdminDiy.addOrUpdateDiyPage',
                    id: pageItem.pagesId,
                    link: pageItem.link, // 页面key
                    url: resut,// 图片地址
                    page_context: JSON.stringify(data), //保存内容
                }, true)

            }

        },
        // 表格 呈现方式
        handleHeaderCellStyle({ column }) {
            if (['name', 'bdsj'].includes(column.property)) {
                return { 'text-align': 'left' };
            } else {
                return { 'text-align': 'center' };
            }
        },
    }
}
