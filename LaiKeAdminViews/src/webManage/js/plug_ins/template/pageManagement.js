import { mixinstest } from '@/mixins/index'

export default {
    name: 'pageManagement',
    mixins: [mixinstest],

    data() {
        return {
            inputInfo:{
                name:'',
                status:''
            },
            labelList:[
                {id:0,name:'待使用'},
                {id:1,name:'已使用'},
            ],
            tableData: [],
            gridData: [],
            detectionBtn: [],
            loading: false,
            loading1: false,
            dialogFormVisible: false,
            formLabelWidth: '120px',
            switchIndex: 0,
            tableHeight: null,
            createdFrom: {
                ztName: '',
                ztType: '',
                ztInfo: ''
            },
            id: '',//主题id
            page_key:'',//页面id
            rules: {
                ztName: [
                    { required: true, message: this.$t('template.divTemplate.qsr'), trigger: 'blur' }
                ],
                ztType: [
                    { required: true, message: this.$t('template.divTemplate.qxz'), trigger: 'change' }
                ]
            },
            current_num1: 10,
            currpage1: 1,
            total1: 0,


        }
    },

    created() {

    },
    watch: {

    },

    mounted() {
        console.log('我是页面')


        this.getList()
    },
    methods: {
        toThemeById(row){
            console.log
            this.$router.push({
                path: '/plug_ins/template/addTemplate',
                query: {
                    id: row.diy_id,
                    status: 'editor',
                    typeIndex:2,
                    pageKeyId:this.page_key // 定位页面id
                }
            })
        },
        reset(){
            this.inputInfo = {
                name:'',
                status:''
            }
        },
        demand(){
            this.currpage = 1
            this.getList()
        },
        // 获取 页面列表信息
        async getList() {
            const res = await this.LaiKeCommon.select({
                api: 'plugin.template.AdminDiy.getDiyPageList',
                pageSize: 10, // 每页条数
                pageNo: this.currpage, // 当前页码
                ...this.inputInfo

            })
            if (res.data.code == 200) {
                const { data: { list, total } } = res.data
                if (list && list.length > 0) {
                    this.tableData = list;
                    this.total = total
                       this.current_num  =
                        this.tableData.length === 10
                        ? this.currpage * this.current_num
                        : this.total
                } else {
                    this.tableData = []
                    this.total = 0
                }
                this.$nextTick(() => {
                    this.getHeight()
                })
            }

        },
        quyerListByText() {
            this.currpage = 1
            this.lockInfo()
        },
        upPageItem(row){
            this.$router.push({
                path: '/plug_ins/template/addTemplate',
                query: {
                    pageId: row.id,
                    status: 'pageDetail',
                    link:row.link,
                    typeIndex:2
                }
            })
        },
        // 获取table高度
        getHeight() {
            this.$nextTick(() => {

                try {
                    this.tableHeight =
                        this.$refs.tableFather.clientHeight - this.$refs.pageBox1.clientHeight - 80
                } catch (e) {
                    this.tableHeight = 630
                }

                console.log(this.tableHeight, 'clientHeight', this.$refs.tableFather.clientHeight, this.$refs.pageBox1.clientHeight);
            })
        },
        // 解绑
        unbindById(row) {
            this.$confirm(this.$t('template.pageManafenebt.tsinfo'), this.$t('template.pageManafenebt.ts'), {
                distinguishCancelAndClose: true,
                confirmButtonText: this.$t('template.pageManafenebt.qr'),
                cancelButtonText: this.$t('template.pageManafenebt.qx')
            }).then((res) => {
                this.LaiKeCommon.del({
                    api: 'plugin.template.AdminDiy.delBindDiyPage',
                    diy_id:row.diy_id,
                    diy_page_id:row.diy_page_id
                }).then(res => {
                    if (res.data.code == 200) {
                        this.$message({
                            type: 'success',
                            message: this.$t('template.pageManafenebt.jscg'),
                            offset: 100
                        });

                        this.lockInfo(this.id)
                    }
                })
            })
        },
        handleClose(done) {
            this.createdFrom.ztName = ''
            done();
            this.getList()
        },
        delPageById(row) {
            let  text = ''
            if(row.status == 0){
                text = this.$t('template.pageManafenebt.dletsInfo')
            }else{
                text = this.$t('template.pageManafenebt.dletsInfo1')
            }
            this.$confirm(text, this.$t('template.pageManafenebt.ts'), {
                distinguishCancelAndClose: true,
                confirmButtonText: this.$t('template.pageManafenebt.qr'),
                cancelButtonText: this.$t('template.pageManafenebt.qx'),
                 type: 'warning'
            }).then(() => {
                this.loading=true
                this.LaiKeCommon.del({
                    api: 'plugin.template.AdminDiy.delDiyPage',
                    id: row.id
                }).then(res => {
                    this.loading=false
                    if (res.data.code == 200) {
                        this.$message({
                            type: 'success',
                            message: this.$t('template.pageManafenebt.delSuccess'),
                            offset: 100
                        });

                        this.getList()
                    }
                })

            })
        },
        // 查看关联主题
        async lockInfo(row) {
            console.log(row)
            if (row && row.id) {
                this.id = row.id
            }
            if(row && row.page_key){
                this.page_key =row.page_key
            }

            this.loading1 = true

            const res = await this.LaiKeCommon.select({
                api: 'plugin.template.AdminDiy.getDiyPageBindList',
                pageSize: 10, // 每页条数
                pageNo: this.currpage1, // 当前页码
                id: row?.id || this.id,//页面id
                name: this.createdFrom.ztName
            })
            this.loading1 = false
            if (res.data.code == 200) {
                this.dialogFormVisible = true
                const { data: { list, total } } = res.data
                if (list && list.length > 0) {
                    this.gridData = list
                    this.total1 = total

                    this.current_num1  =
                        this.gridData.length === 10
                        ? this.currpage1 * this.pageSize
                        : this.total1

                } else {
                    this.gridData = []
                    this.total1 = 0
                    this.current_num1 = 0
                }

            }
        },
        //选择一页多少条
        handleSizeChange(e) {
            this.loading = true
            console.log(e)
            this.pageSize = e
            this.getList().then(() => {
                this.loading = false
            })
        },

        //点击上一页，下一页
        handleCurrentChange(e) {
            this.loading = true
            this.currpage = e
            this.getList().then(() => {
                this.loading = false
            })
        },

        //点击上一页，下一页
        handleCurrentChange1(e) {
            this.currpage1 = e
            this.lockInfo()
        },

        //选择一页多少条
        handleSizeChange1(e) {
            this.pageSize1 = e
            this.lockInfo()
        },

        // 链接复制
        copyThelink(row) {
            if (row.link) {
                const tempTextarea = document.createElement('textarea');
                try {
                    tempTextarea.value = row.link;
                    document.body.appendChild(tempTextarea);
                    tempTextarea.select()
                    // 执行复制命令
                    const successful = document.execCommand('copy');
                    if (successful) {
                        this.$message({
                            type: 'success',
                            message: this.$t('template.pageManafenebt.fzcg'),
                            offset: 100
                        })
                    } else {
                        this.$message({
                            type: 'error',
                            message: this.$t('template.pageManafenebt.fzsb'),
                            offset: 100
                        })
                    }
                } catch (err) {
                    this.$message({
                        type: 'error',
                        message: err,
                        offset: 100
                    })
                }
                document.body.removeChild(tempTextarea);
            }
        },
        // 表格 呈现方式
        handleHeaderCellStyle({ column }) {
            // 除了地址的显示是靠左，其他的都居中
            if (column.property === 'link') {
                return { 'text-align': 'left' };
            } else {
                return { 'text-align': 'center' };
            }
        },


    }
}
