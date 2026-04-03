import { bannerPathList, bannerSave } from '@/api/plug_ins/template'
import request from '@/api/https'
export default {
    name: 'addSlideShow',
    data() {
        return {
            ruleForm: {
                img: '',
                class1: 2,
                url: ''
            },
            search:'',
            prochangedata: '',
            prodata: [],
            currpage: 1,
            current_num: '',
            total: 0,
            dialogVisible2:false,
            pagination: {
                page: 1,
                pagesize: 10,
            },
            pagesizes: [10, 25, 50, 100],
            rules: {
                img: [
                  {required: true, message: '请上传轮播图', trigger: 'blur'}
                ],
            },

            classList1: [
                {
                    value: 2,
                    label: this.$t('template.addSlideShow.sp')
                },
                {
                    value: 1,
                    label: this.$t('template.addSlideShow.fl')
                },
                {
                    value: 3,
                    label: this.$t('template.addSlideShow.dp')
                },
                {
                    value: 0,
                    label: this.$t('template.addSlideShow.zdy')
                }
            ],

            classList2: [],
            baseUrl: '/pagesC/goods/goodsDetailed?productId=',

        }
    },

    watch: {
        'ruleForm.class1'() {
            if(this.ruleForm.class1 == 1) {
                this.baseUrl = '/pagesC/goods/goods?cid='
                this.ruleForm.url = ''
                this.bannerPathLists(this.ruleForm.class1)
            } else if(this.ruleForm.class1 == 3) {
                this.baseUrl = '/pagesB/store/store?shop_id='
                this.ruleForm.url = ''
                this.bannerPathLists(this.ruleForm.class1)
            } else if(this.ruleForm.class1 == 0) {
                this.baseUrl = ''
                this.ruleForm.url = ''
                this.classList2 = []
            } else {
                this.baseUrl = '/pagesC/goods/goodsDetailed?productId='
                this.ruleForm.url = ''
                this.bannerPathLists(this.ruleForm.class1)
            }
        },
        'ruleForm.img'() {
            if(this.ruleForm.img) {
              this.$refs.ruleForm.clearValidate('img')
            }
        },
    },

    created() {
        this.ruleForm.class1 = this.$route.params.open_type ? parseInt(this.$route.params.open_type) : 0
        if(this.ruleForm.class1 !== 0) {
            if(this.ruleForm.class1 == 2){
                request({
                    method: 'post',
                    params:{
                      api: 'plugin.coupon.Admincoupon.GetSpecifiedGoodsInfo',
                      pageNo:this.currpage ,
                      pageSize:this.pagination.pagesize,
                      id:this.$route.params.url.split('?')[1].split('=')[1]+','
                    }

                  }).then(res=>{
                    console.log(res)
                    this.prochangedata=res.data.data.list[0]
                  })
            }
            this.bannerPathLists(this.ruleForm.class1).then(() => {
                this.ruleForm.url = this.$route.params.url.split('?')[1]
            })
        } else {
            this.ruleForm.url = this.$route.params.url
        }

        this.ruleForm.img = this.$route.params.image
        // this.bannerPathLists(2)
        this.getprolist()
    },

    methods: {
        AddPro(){
            this.dialogVisible2 = true
            this.current_num = this.prodata.length
            this.currpage = 1
            this.pagination.pageSize = 10
            // this.$refs.multipleTable.clearSelection();
            // this.$refs.multipleTable.toggleRowSelection(this.prochangedata);
          },
          delpro(){
            this.prochangedata=''
            this.$refs.multipleTable.clearSelection();
          },
          handleSelectionChange(e) {
            console.log(e)
            this.prochangedata = e
          },
          Reset() {
            this.search=''
          },
          query(){
            this.getprolist()
          },
          confirm(){
            if(this.prochangedata.length == 0) {
              this.$message({
                  message: this.$t('template.addSlideShow.qxzsp'),
                  type: 'error',
                  offset:100
              })
              return
            }
            this.dialogVisible2 = false
            // this.$refs.multipleTable.clearSelection();
            // this.getprolist()
          },
          //选择一页多少条
          handleSizeChange(e) {
            this.pagination.pagesize = e
            // if (this.dialogVisible3) {
                this.getprolist()
            // } else {
            //     this.getUserList()
            // }
          },
          //点击上一页，下一页
          handleCurrentChange(e) {
              this.currpage = e
              // if (this.dialogVisible3) {
                  this.getprolist()
              // } else {
              //     this.getUserList()
              // }
          },
          getprolist(){
            request({
              method: 'post',
              params:{
                api: 'plugin.coupon.Admincoupon.GetSpecifiedGoodsInfo',
                pageNo:this.currpage ,
                pageSize:this.pagination.pagesize,
                productTitle:this.search
              }

            }).then(res=>{
              console.log(res)
              this.total = Number(res.data.data.total)
              this.prodata = res.data.data.list
              this.current_num = this.prodata.length
              if(this.total < this.current_num) {
                  this.current_num = this.total
              }
            })
          },
        async bannerPathLists(value) {
            const res = await bannerPathList({
                // api: 'admin.diy.bannerPathList',
                api: 'plugin.template.AdminDiy.BannerPathList',
                type: value
            })
            console.log(res);
            this.classList2 = res.data.data.list
        },

        submitForm(formName) {
            this.$refs[formName].validate(async (valid) => {
                console.log(this.ruleForm,this.getUrl());
                if (valid) {
                  try {
                    if(this.ruleForm.class1 == 0 && this.ruleForm.url.length > 100) {
                        this.$message({
                            message: this.$t('template.addSlideShow.zdyurl'),
                            type: 'error',
                            offset:100
                        })
                        return
                    }
                    bannerSave({
                        // api: 'admin.diy.bannerSave',
                        api: 'plugin.template.AdminDiy.BannerSave',
                        type0: this.ruleForm.class1,
                        url: this.getUrl(),
                        picUrl: this.ruleForm.img
                    }).then(res => {
                        if(res.data.code == '200') {
                            this.$message({
                                message: this.$t('template.cg'),
                                type: 'success',
                                offset:100
                            })
                            this.$router.go(-1)
                        }
                        console.log(res);
                    })
                  } catch (error) {
                    this.$message({
                        message: error.message,
                        type: 'error',
                        showClose: true
                    })
                  }
                } else {
                    console.log('error submit!!');
                    return false;
                }
            })
        },

        getUrl() {
            if(this.ruleForm.class1 == 0) {
                return this.ruleForm.url
            }else if(this.ruleForm.class1 == 2){
                return this.baseUrl + this.prochangedata.id
            } else {
                if(this.ruleForm.url) {
                    return this.baseUrl + this.ruleForm.url.split('=')[1]
                } else {
                    return ''
                }
            }
        }
    }

}
