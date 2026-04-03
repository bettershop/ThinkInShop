import { bannerIndex, bannerDel, bannerMoveTop, bannerRemove } from '@/api/plug_ins/template'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
export default {
    name: 'playlist',
    mixins: [mixinstest],
    data() {
        return {
            radio1:this.$t('template.lbtlb'),
            button_list:[],
            tableData: [],
            loading: true,
            // table高度
            tableHeight: null,
            menuId:'',
        }
    },

    created() {
        if(this.$route.params.pageSize) {
            this.pagination.page = this.$route.params.dictionaryNum
            this.dictionaryNum = this.$route.params.dictionaryNum
            this.pageSize = this.$route.params.pageSize
        }
        this.bannerIndexs()
        this.getButtons()
    },


    mounted() {
        this.$nextTick(function() {
          this.getHeight()
        })
        window.addEventListener('resize',this.getHeight(),false)
    },

    methods: {
        // 获取table高度
        getHeight(){
            this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
        },
        //获取按纽权限
        async getButtons() {
            let route=getStorage('route')
            console.log(route,"1111111`1");
            route.forEach(item => {
                if( item.redirect=='/plug_ins/template/divTemplate'){
                    item.children.forEach(i => {
                        if(i.path=='playlist'){
                          return  this.menuId=i.id
                        }
                    })
                }
            });

            let buttonList = await getButton ({
            api:'saas.role.getButton',
            token:getStorage('laike_admin_userInfo').token,
            storeId:getStorage('rolesInfo').storeId,
            menuId:this.menuId,
            })
            this.button_list=buttonList.data.data
            console.log(this.button_list,"获取按纽权限")
        },
        async bannerIndexs() {
            const res = await bannerIndex({
                // api: 'admin.diy.bannerIndex',
                api: 'plugin.template.AdminDiy.BannerIndex',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
            })
            this.current_num = 10
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.loading = false
            if(this.total < this.current_num) {
                this.current_num = this.total
            }
            if(this.total == 0) {
                this.showPagebox = false
            }
            console.log(res);
        },

        // 移动
        moveUp(value) {
            if(value !== 0) {
                bannerRemove({
                  // api: 'admin.diy.bannerRemove',
                  api: 'plugin.template.AdminDiy.BannerRemove',
                  id: this.tableData[value - 1].id,
                  id1: this.tableData[value].id
                }).then(res => {
                    if(res.data.code == '200') {
                        this.bannerIndexs()
                        console.log(res);
                        this.$message({
                            type: 'success',
                            message: this.$t('template.cg'),
                            offset: 100
                        });
                    }
                })
            } else {
                bannerRemove({
                  // api: 'admin.diy.bannerRemove',
                  api: 'plugin.template.AdminDiy.BannerRemove',
                  id: this.tableData[value + 1].id,
                  id1: this.tableData[value].id
                }).then(res => {
                    if(res.data.code == '200') {
                        this.bannerIndexs()
                        console.log(res);
                        this.$message({
                            type: 'success',
                            message: this.$t('template.cg'),
                            offset: 100
                        });
                    }
                })
            }
        },

        // 置顶
        placedTop(value) {
            bannerMoveTop({
                // api: 'admin.diy.bannerMoveTop',
                api: 'plugin.template.AdminDiy.BannerMoveTop',
                id: value.id
            }).then(res => {
                console.log(res);
                if(res.data.code == '200') {
                    this.bannerIndexs()
                    this.$message({
                        type: 'success',
                        message: this.$t('template.cg'),
                        offset: 100
                    })
                }
            })
        },

        // 编辑
        Edit(value) {
            this.$router.push({
                name: 'editorSlideShow',
                params: value,
                query: {
                    dictionaryNum: this.dictionaryNum,
                    pageSize: this.pageSize
                }
            })
        },

        // 删除
        Delete(value) {
            this.$confirm(this.$t('template.playlist.qdsc'), this.$t('template.ts'), {
              confirmButtonText: this.$t('template.okk'),
              cancelButtonText: this.$t('template.ccel'),
              type: 'warning'
            }).then(() => {
                bannerDel({
                    // api: 'admin.diy.bannerDel',
                    api: 'plugin.template.AdminDiy.BannerDel',
                    id: value.id
                }).then(res => {
                    console.log(res);
                    if(res.data.code == '200') {
                        this.bannerIndexs()
                        this.$message({
                            type: 'success',
                            message: this.$t('template.cg'),
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
            //   this.$message({
            //     type: 'info',
            //     message: '已取消删除',
            //     offset: 100
            //   });
            });
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log(e);
            // this.current_num = e
            this.pageSize = e
            this.bannerIndexs().then(() => {
                this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
                this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
                this.loading = false
            })
        },

        //点击上一页，下一页
        handleCurrentChange(e){
            this.loading = true
            this.dictionaryNum = e
            this.currpage = ((e - 1) * this.pageSize) + 1
            this.bannerIndexs().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },
    }
}
