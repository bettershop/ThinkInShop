import { index, diyList, delDiy, applicationDiy } from '@/api/plug_ins/template'
import pageManagement from '@/views/plug_ins/template/pageManagement'
import guideMap from '@/views/plug_ins/template/guideMap'
import { addDiyInfo } from '@/api/diy/index'
import diyModel from '@/common/DIYCommonMod'
import { mapMutations } from 'vuex';
import linkDialog from "@/components/link-dialog/linkDialog.vue";
export default {
    name: 'divTemplate',
    data() {
        return {
            imgList: [],
            linkFalg:false,
            loading: false,
            typeIndex: 1, // 标签 1 系统 、2 自定义 、3 页面管理
            H5_domain: '',
            is_default: true,
            iframeShow: true,
            id: null,
            iconType: 'AndImg',
            selection: null, //选中时
            preSelection: null, // 鼠标移入移出时
            template: this.$t('template.divTemplate.mrmb'),
            yingyong: 'images/diy/icon_yingyong.png',
            gouxuan: 'images/diy/gouxuan.png',
            butMask: 'images/diy/butMask.png',
            textMask: 'images/diy/textMask.png',
            noeFalg: true,
            Bgimg: 'images/diy/Android.png',
            IOSImg: 'images/diy/ios.png',
            AndImg: 'images/diy/Android.png',
            uniAppImg: 'images/diy/myUni.png',
            azw: 'images/diy/az-w.png',
            iosw: 'images/diy/ios-w.png',
            xcxw: 'images/diy/xcx-w.png',
            azcur: 'images/diy/az-cur.png',
            ioscur: 'images/diy/ios-cur.png',
            xcxcur: 'images/diy/xcx-cur.png',
            dt: 'images/diy/dt.png',
            pl: 'images/diy/pl.png',
            dialogFormVisible: false,
            formLabelWidth: '120px',
            switchIndex: 0,
            createdFrom: {
                name: '',
                theme_type_code: '',
                remark: ''
            },
            tableHeight: null, //列表高度
            themeType: 1,//主题类型  1 系统 、2 自定义
            themeTypeCode: '',
            isDragging: false,
            startX: 0,
            startScrollLeft: 0,
            //导航类型 1不需要权限 2需要权限认证
            nav_label_type: 1,
            //导航数据 name:导航文字 label:绑定值 value:数量 disabled:是否禁用 routerTypes是否启用权限判断
            nav_label_list: [],
            nav_label_list1: [],
            rules: {
                name: [
                    { required: true, message: this.$t('template.divTemplate.qsr'), trigger: 'blur' }
                ],
                theme_type_code: [
                    { required: true, message: this.$t('template.divTemplate.qxz'), trigger: 'change' }
                ]
            },
            iframeHeight: '',
            isLkt: false
        }
    },
    components: {
        pageManagement,
        guideMap,
        linkDialog,
    },
    created() {

        this.index()
        // this.getDiyList()
        diyList({
            api: 'plugin.template.AdminDiy.GetDiyList',
            theme_type: this.themeType,
            theme_type_code: this.themeTypeCode,
        }).then(res => {
            try {
                if (res.data.data.list.every(item => item.status != 1)) {
                    this.typeIndex = 2
                    this.themeType = 2
                    this.resetNavChoose()
                    this.themeTypeCode = ''
                }
                this.getDiyList()
                this.index()
            } catch (e) {
                console.log(e, '错误')
                this.$message({
                    type: 'error',
                    message: this.$t('errorMsg'),
                    offset: 100
                })
            }
        })
    },
    watch: {
    },
    mounted() {
        this.isLkt = process.env.VUE_APP_IS_LKT || true
        console.log(this.isLkt, 'this.isLkt')
        this.$nextTick(() => {
            this.getHeight()
            this.adjustIframeHeight()
        })

        window.addEventListener('resize', this.getHeight(), false)
        window.addEventListener('resize', this.calculateDimensions);

    },
    beforeDestroy() {

        window.removeEventListener('resize', this.getHeight);
        window.removeEventListener('resize', this.calculateDimensions);
    },
    methods: {
        ...mapMutations({
            setLabelList: 'admin/mobildConfig/SELECT_LABEL_LIST'
        }),
        openLinkDialog(){
            this.$nextTick(()=>{
                this.$refs.linkDialog.dialogVisible = true
            })
        },
        calculateDimensions() {
            this.$nextTick(() => {
                let iframeWidth = this.$refs.bgImg?.offsetWidth || 0
                let iframeHeight = this.$refs.bgImg?.offsetHeight || 0
                // 更新 iframe 样式
                if (this.$refs.mobsf) {
                    this.$refs.mobsf.style.width = `${iframeWidth - 10}px`;
                    this.$refs.mobsf.style.height = `${iframeHeight - 80}px`;
                    console.log('走了吗？', iframeWidth - 10, iframeHeight - 86, '计算的宽高');
                }
            })
        },
        // 获取列表高度
        getHeight() {
            this.tableHeight = this.$refs.templateContent.clientHeight
            console.log(this.tableHeight, 'clientHeight');
        },
        iconSwitch(e) {
            const target = e.target
            // 查找最接近的包含 data-type 属性的父元素（因为点击的可能是 img 标签）
            const dom = target.closest('[data-type]');
            if (dom) {
                const type = dom.getAttribute('data-type');
                this.iconType = type
                this.Bgimg = this.$data[type]
            }
        },
        getImageUrl(path) {
            try {
                return require(`@/assets/${path}`) // 根据你的项目结构调整前缀
            } catch (e) {
                console.error('图片加载失败:', path, e)
                // 可增加默认的图片
                return ''
            }
        },
        resetNavChoose() {
            this.$nextTick(() => {
                if (this.$refs.navLabel) {
                    this.$refs.navLabel.nav_choose = ''
                }
            })
        },
        chooseType(event) {
            const target = event.target;
            const index = target.dataset.type;
            if (index) {
                this.typeIndex = index;
                this.themeType = index
                if (index == 2) {
                    this.resetNavChoose()
                }
                if (index != 3 && index != 4) {
                    this.themeTypeCode = ''
                    this.getDiyList()
                }
            }
        },
        // 自定义主题 主题类型切换
        async nav_choose(id) {
            this.themeTypeCode = id
            this.themeType = 2
            this.loading = true
            const res = await diyList({
                api: 'plugin.template.AdminDiy.GetDiyList',
                theme_type: this.themeType,
                theme_type_code: this.themeTypeCode,

            })

            this.loading = false
            if (res.data.code != 200) {
                this.imgList = []
                return
            }
            this.imgList = res.data.data.list

            this.imgList.sort((a, b) => {
                if (a.status === 1) return -1; // a 排前面
                if (b.status === 1) return 1;  // b 排前面
                return 0;                      // 保持原有顺序
            });
            this.imgList.map(item => {
                if (item.status == 1) {
                    this.is_default = false
                    this.id = item.id
                    this.template = item.name
                }
            })
        },
        queryItem(index) {
            this.selection = index
            // this.index()
        },
        selecyItemByIndex(index) {
            this.selection = index
        },

        onMouseDown(event) {
            this.isDragging = true;
            this.startX = event.pageX;
            this.startScrollLeft = this.$refs.scrollContainer.scrollLeft;
        },
        onMouseMove(event) {
            console.log(this.isDragging)
            if (this.isDragging) {
                const deltaX = event.pageX - this.startX;
                this.$refs.scrollContainer.scrollLeft = this.startScrollLeft - deltaX;
            }
        },
        onMouseUp() {
            this.isDragging = false;
        },
        switchShowType(event) {
            const target = event.target;
            if (target.classList.contains('item')) {
                const index = parseInt(target.dataset.index);
                console.log(target.dataset.index)
                this.switchIndex = index;
            }
        },
        async getDiyList() {
            this.loading = true

            const res = await diyList({
                api: 'plugin.template.AdminDiy.GetDiyList',
                theme_type: this.themeType,
                theme_type_code: this.themeTypeCode,

            })

            this.loading = false
            if (res.data.code == 200) {

                this.imgList = res.data.data.list
                this.imgList.sort((a, b) => {
                    if (a.status === 1) return -1; // a 排前面
                    if (b.status === 1) return 1;  // b 排前面
                    return 0;                      // 保持原有顺序
                });
                this.imgList.map(item => {
                    if (item.status == 1) {
                        this.is_default = false
                        this.id = item.id
                        this.template = item.name
                    }
                })

                if (res.data.data.details && res.data.data.details.length > 0) {
                    this.nav_label_list = res.data.data.details.map(item => ({
                        name: item.text,
                        id: item.code,
                        label: item.value,
                        ischoose: false,
                        value: item.count,
                        kuohao: true
                    }))
                    this.nav_label_list1 = JSON.parse(JSON.stringify(this.nav_label_list))

                    this.nav_label_list.unshift({
                        id: '',
                        name: this.$t('template.divTemplate.qbzt'),
                        ischoose: false,
                        value: res.data.data.total,
                        kuohao: true
                    })
                    if (!this.noeFalg) { return }
                    this.setLabelList(this.nav_label_list1)
                    this.noeFalg = false


                } else {
                    this.imgList = []
                    // this.setLabelList(this.nav_label_list)
                }

            } else {
                // this.warnMsg(res.data.message)
            }
        },
        async index() {
            this.iframeShow = false
            const res = await index({
                api: 'plugin.template.AdminDiy.Index',
            })
            console.log(res, 'res-res');
            this.iframeShow = true
            this.H5_domain = res.data.data.H5_domain
            this.$nextTick(() => {
                let iframeWidth = this.$refs.bgImg?.offsetWidth
                let iframeHeight = this.$refs.bgImg?.offsetHeight
                console.log('走了吗？', iframeWidth, iframeHeight, '计算的宽高');
                // 更新 iframe 样式
                if (this.$refs.mobsf) {
                    console.log('我进来了')
                    this.$refs.mobsf.style.width = `${iframeWidth - 10}px`;
                    this.$refs.mobsf.style.height = `${iframeHeight - 80}px`;
                }
            })
        },

        // 新建模板
        addCoupons(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    const { homeMod, goodClassMod, myMod, cartMod, tabbarMod } = diyModel
                    addDiyInfo({
                        api: 'plugin.template.AdminDiy.AddOrUpdateDiy',
                        name: this.createdFrom.name,
                        remark: this.createdFrom.remark,//备注
                        theme_type: this.themeType, //主题类型
                        theme_type_code: this.createdFrom.theme_type_code, // 主题类别（字典id）
                        value: JSON.stringify([{ ...homeMod }, { ...goodClassMod }, { ...myMod }, { ...cartMod }]),
                        tabberinfo: JSON.stringify(tabbarMod.tabb_info),
                        tabBar: JSON.stringify(tabbarMod.tabbar),

                    }).then(res => {
                        if (res.data.code == '200') {
                            this.$router.push({
                                path: '/plug_ins/template/addTemplate',
                                query: {
                                    createdFrom: JSON.stringify(this.createdFrom),
                                    themeType: this.themeType,
                                    id: res.data.data.id, //主题id
                                    typeIndex: this.typeIndex
                                }
                            })
                        } else {
                            this.$message({
                                type: 'success',
                                message: res.data.message,
                                offset: 100
                            })
                        }
                    })
                }
            })
        },
        resetForm(formName) {
            this.$nextTick(() => {
                this.$refs[formName].resetFields();
                this.createdFrom.remark = ''
                this.dialogFormVisible = false
            })
        },
        // 编辑
        editor(value) {
            this.$router.push({
                path: '/plug_ins/template/addTemplate',
                query: {
                    id: value.id,
                    status: 'editor',
                    typeIndex: this.typeIndex
                }
            })
        },

        editors() {
            if (this.is_default) {
                this.$router.push({
                    path: '/plug_ins/template/playlist',
                })
            } else {
                this.$router.push({
                    path: '/plug_ins/template/addTemplate',
                    query: {
                        id: this.id,
                        typeIndex: this.typeIndex,
                        status: 'editor',
                    }
                })
            }
        },

        // 应用
        async Application(value) {
            if (value == '默认') {
                const res = await applicationDiy({
                    // api: 'plugin.adminDiy.diyStatus',
                    api: 'plugin.template.AdminDiy.DiyStatus',
                })
                if (res.data.code == '200') {
                    this.$message({
                        type: 'success',
                        message: this.$t('template.cg'),
                        offset: 100
                    })
                    this.template = this.$t('template.divTemplate.mrmb')

                    this.getDiyList()
                    this.is_default = true

                    // window.open(document.getElementById('mobsf').src,'refresh_name','')

                    this.index()
                } else {
                    this.$message({
                        type: 'success',
                        message: res.data.message,
                        offset: 100
                    })
                }
            } else {
                const res = await applicationDiy({
                    // api: 'plugin.adminDiy.diyStatus',
                    api: 'plugin.template.AdminDiy.DiyStatus',
                    id: value?.id || ''
                })
                this.selection = 0
                console.log(res)
                if (res.data.code == '200') {
                    this.$message({
                        type: 'success',
                        message: this.$t('template.cg'),
                        offset: 100
                    })
                    this.getDiyList()
                    // window.open(document.getElementById('mobsf').src,'refresh_name','')
                    this.index()
                } else {
                    this.$message({
                        type: 'success',
                        message: res.data.message,
                        offset: 100
                    })
                }
            }
        },

        // 删除
        Delete(value) {
            this.$confirm(this.$t('template.divTemplate.qrsc'), this.$t('template.ts'), {
                confirmButtonText: this.$t('template.okk'),
                cancelButtonText: this.$t('template.ccel'),
                type: 'warning'
            }).then(() => {
                delDiy({
                    // api: 'plugin.adminDiy.delDiy',
                    api: 'plugin.template.AdminDiy.DelDiy',
                    id: value.id
                }).then(res => {
                    console.log(res);
                    if (res.data.code == '200') {
                        this.getDiyList()
                        this.$message({
                            type: 'success',
                            message: this.$t('template.cg'),
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
                // this.$message({
                //     type: 'info',
                //     message: '已取消删除',
                //     offset: 100
                // })
            })
        },
        getBackgroundStyle(index) {
            const fileName = this.preSelection === index ? 'textMask.png' : 'butMask.png';
            return {
                'background-image': `url('../../../../assets/images/diy/${fileName}')`
            }
        },
        adjustIframeHeight() {
            try {
                const iframe = this.$refs.mobsf
                if (iframe && iframe.contentWindow) {
                    // 检测iOS设备
                    const isiOS = /iPad|iPhone|iPod/.test(navigator.userAgent)

                    if (isiOS) {
                        // iOS特殊处理：使用contentDocument.body.scrollHeight
                        const height = iframe.contentDocument.body.scrollHeight + 'px'
                        this.iframeHeight = height
                    } else {
                        // 其他设备使用contentWindow.document.documentElement.scrollHeight
                        const height = iframe.contentWindow.document.documentElement.scrollHeight + 'px'
                        this.iframeHeight = height
                    }
                }
                console.log('iframeHeight', this.iframeHeight)
            } catch (error) {
                console.error('调整iframe高度失败:', error)
            }
        }
    }
}
