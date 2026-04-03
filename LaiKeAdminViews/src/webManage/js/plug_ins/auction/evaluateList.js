import pageData from "@/api/constant/page";
import {del, save, index} from "@/api/order/comment";
import {isEmpty} from "element-ui/src/utils/util";
import { mixinstest } from '@/mixins/index'
import { exports } from '@/api/export/index'

export default {
  name: 'evaluateList',
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      page: pageData.data(),
      choseDate: [],
      orderNo: null,
      commentTypeList: [{ val: 'GOOD', name: this.$t('commentList.hp') }, { val: 'NOTBAD', name: this.$t('commentList.zp') }, { val: 'BAD', name: this.$t('commentList.cp') }],
      dataForm: {
        id: null,
        CommentType: null,
        orderNo: null,
        mchName:null,
      },
      rules: {
        name: [
          { required: true, message: this.$t('commentList.bqmcwk'), trigger: 'blur' }
        ]
      },
      sales_imgs:null,
      dialogVisible: false,
      dialogVisible1:false,
      id: null,
      ruleForm: {
        comment_content: "",
      },
      rules: {
        comment_content: [
          { required: true, message: this.$t('commentList.qsrhf'), trigger: 'blur' }
        ]
      },
      // table高度
      tableHeight: null,
      showPagebox:true
    }
  },
  //组装模板
  created() {
    this.loadData()
  },

  mounted() {
    this.$nextTick(function() {
        this.getHeight()
    })
    window.addEventListener('resize',this.getHeight(),false)
  },

  methods: {
    godetail(id){
      this.$router.push({
        path: '/plug_ins/auction/evaluateDetail',
        query: {
          id: id
        }
      })
    },
    // 获取table高度
    getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
		},
    async loadData() {
      await this.getList().then()
    },
    // 获取列表
    async getList() {
      const res = await index({
        api: 'plugin.auction.AdminAuction.getCommentsInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        orderType: 'JP',
        orderno: this.dataForm.orderNo,
        mchName:this.dataForm.mchName,
        type: this.dataForm.CommentType,
        startDate: isEmpty(this.dataForm.choseDate) ? null : this.dataForm.choseDate[0],
        endDate: isEmpty(this.dataForm.choseDate) ? null : this.dataForm.choseDate[1],
      });
      this.total = res.data.data.total
      this.page.tableData = res.data.data.list
      this.page.loading = false
      if (res.data.data.total < 10) {
        this.current_num = this.total
      }
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },

    // 重置
    reset() {
      this.dataForm.CommentType = null
      this.dataForm.orderNo = null
      this.dataForm.mchName=null
      this.dataForm.choseDate = []
    },
    

    // 查询
    demand() {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.page.loading = true
      this.dictionaryNum = 1
      this.loadData().then(() => {
        this.page.loading = false
        if (this.page.tableData.length > 0) {
          this.showPagebox = true
        }
      })
    },

    // 弹框方法
    dialogShow(value) {
      this.ruleForm.comment_content = ''
      this.id = value.id
      this.dialogVisible = true
    },

    handleClose1(done) {
      this.dialogVisible = false
      this.$refs['ruleForm'].clearValidate()
    },

    // 回复
    determine(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          try {
            if(this.ruleForm.comment_content.length > 50) {
              this.$message({
                message: "回复内容长度不能大于50个字符",
                type: "error",
                offset: 100,
              });
              return
            }
            save({
              api: "admin.order.replyComments",
              commentId: this.id,
              commentText: this.ruleForm.comment_content,
            }).then((res) => {
              console.log(res);
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t('commentList.cg'),
                  type: "success",
                  offset: 100,
                });
                this.getList()
                this.dialogVisible = false;
              }
            });
          } catch (error) {

          }
        } else {

        }
      });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.page.loading = true
      // this.page.current_num = e
      this.pageSize = e
      this.loadData().then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.page.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
        this.page.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.page.loading = true
      this.dictionaryNum = e
      this.currpage = ((e - 1) * this.pageSize) + 1
      this.loadData().then(() => {
        this.current_num = this.page.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.page.loading = false
      })

    },
    Reply(id) {
      this.$router.push({
        name: 'commentReply',
        params: {id: id},
      })
    },
    //修改
    async Edit(id,type) {
      this.$router.push({
        path: '/plug_ins/auction/evaluateEdit',
        query: {
          id: id,
          type: type
        }
      })
    },

    // 删除
    Delete(id) {
      this.$confirm(this.$t('commentList.scts'), this.$t('commentList.ts'), {
        confirmButtonText: this.$t('commentList.okk'),
        cancelButtonText: this.$t('commentList.ccel'),
        type: 'warning'
      }).then(() => {
        del({
          api: 'plugin.auction.AdminAuction.delComments',
          type: 1,
          id: id
        }).then(res => {
          this.demand();
          this.$message({
            type: 'success',
            message: this.$t('commentList.cg')
          })
        })
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // })
      })
    },
  // 弹框方法
  dialogShow1() {
    this.dialogVisible1 = true
  },

  handleClose(done) {
    this.dialogVisible1 = false
  },

  async exportPage() {
    await exports({
      api: 'plugin.auction.AdminAuction.getCommentsInfo',
      pageNo: this.dictionaryNum,
      pageSize: this.pageSize,
      exportType: 1,
      orderType: 'JP',
      mchName:this.dataForm.mchName,
      orderno: this.dataForm.orderNo,
      type: this.dataForm.CommentType,
      startDate: isEmpty(this.dataForm.choseDate) ? null : this.dataForm.choseDate[0],
      endDate: isEmpty(this.dataForm.choseDate) ? null : this.dataForm.choseDate[1],
    },'pagegoods')
  },

  async exportAll() {
    console.log(this.total);
    await exports({
      api: 'plugin.auction.AdminAuction.getCommentsInfo',
      pageNo: 1,
      pageSize: this.total,
      exportType: 1,
      orderType: 'JP',
      mchName:this.dataForm.mchName,
      orderno: this.dataForm.orderNo,
      type: this.dataForm.CommentType,
      startDate: isEmpty(this.dataForm.choseDate) ? null : this.dataForm.choseDate[0],
      endDate: isEmpty(this.dataForm.choseDate) ? null : this.dataForm.choseDate[1],
    },'allgoods')
  },

  async exportQuery() {
    await exports({
      api: 'plugin.auction.AdminAuction.getCommentsInfo',
      pageNo:1,
      pageSize: this.total,
      exportType: 1,
      orderType: 'JP',
      mchName:this.dataForm.mchName,
      orderno: this.dataForm.orderNo,
      type: this.dataForm.CommentType,
      startDate: isEmpty(this.dataForm.choseDate) ? null : this.dataForm.choseDate[0],
      endDate: isEmpty(this.dataForm.choseDate) ? null : this.dataForm.choseDate[1],
    },'querygoods')
  }

  }

}
