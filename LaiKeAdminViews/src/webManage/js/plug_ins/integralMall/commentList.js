import pageData from "@/api/constant/page";
import {del, save, index} from "@/api/order/comment";
import {isEmpty} from "element-ui/src/utils/util";
import { mixinstest } from '@/mixins/index'

export default {
  name: 'list',
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      showPagebox:true,
      page: pageData.data(),
      choseDate: [],
      orderNo: null,
      commentTypeList: [{val: 'GOOD', name: this.$t('integralMall.commentList.hp')}, {val: 'NOTBAD', name: this.$t('integralMall.commentList.zp')}, {val: 'BAD', name: this.$t('integralMall.commentList.cp')}],
      dataForm: {
        id: '',
        CommentType: '',
        orderNo: '',
        choseDate: []
      },
      rules: {
        name: [
          {required: true, message: this.$t('integralMall.commentList.bqmwk'), trigger: 'blur'}
        ]
      },

      dialogVisible: false,
      id: null,
      ruleForm: {
        comment_content: "",
      },
      rules: {
        comment_content: [
          { required: true, message: this.$t('integralMall.commentList.qsrhfnr'), trigger: "blur" },
        ]
      },
      // table高度
      tableHeight: null
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
    see(index) {
      this.$router.push({
        name: 'evaluationDetail',
        params: { id: index.id },
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
        api: 'plugin.integral.order.getCommentsInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        orderno: this.dataForm.orderNo,
        type: this.dataForm.CommentType,
        orderType: 'IN',
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
      setTimeout(() => {
        if (this.page.tableData.length > 0) {
          this.showPagebox = true
        }else{
          this.showPagebox = false
        }
      }, 1000);
    },

    // 重置
    reset() {
      this.dataForm.CommentType = ''
      this.dataForm.orderNo = ''
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

    handleClose(done) {
      this.dialogVisible = false
      this.$refs['ruleForm'].clearValidate()
    },

    // 回复
    determine(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          try {
            save({
              api: "plugin.integral.order.replyComments",
              commentId: this.id,
              commentText: this.ruleForm.comment_content,
            }).then((res) => {
              console.log(res);
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t('integralMall.commentList.cg'),
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
    async Edit(id) {
      this.$router.push({
        path: '/plug_ins/integralMall/commentEdit',
        query: {
          id: id
        }
      })
    },

    // 删除
    Delete(id) {
      this.$confirm(this.$t('integralMall.commentList.scqr'), this.$t('integralMall.commentList.ts'), {
        confirmButtonText: this.$t('integralMall.commentList.okk'),
        cancelButtonText: this.$t('integralMall.commentList.ccel'),
        type: 'warning'
      }).then(() => {
        del({
          api: 'plugin.integral.order.delComments',
          commentId: id
        }).then(res => {
          this.demand();
          this.$message({
            type: 'success',
            message: this.$t('zdata.sccg'),
            offset:100
          })
        })
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // })
      })
    }

  }

}
