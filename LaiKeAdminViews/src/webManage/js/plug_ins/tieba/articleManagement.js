import {
  getList,
  operation,
} from '@/api/plug_ins/tieba'
import { mixinstest } from '@/mixins/index'
export default {
  name: 'groupGoods',
  mixins: [mixinstest],
  data() {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('tieaba.wzgl'),
      inputInfo: {
        status: null,
        name: null,
        title: '',
        type: '',
        user_id: '',
        label_id:''
      },
      tableData: [],
      loading: true,
      tableHeight: null,
      changeCouList: [],
      idList: [],
      is_tuijian: 0,
      dialogVisible3: false,
      ruleForm3: {
        reason: "",
      },
    }
  },

  created() {
    if (this.$route.query.label_id) {
      this.inputInfo.label_id = this.$route.query.label_id
    }
    this.getList()
  },
  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    typeText(type) {
      console.log( type,'this')
      const obj = {
        0: this.$t('classification.text'),
        1: this.$t('classification.sp'),
        2: this.$t('classification.londText')
      }
      return obj[type] || ''
    },
     handleClose3(done) {
      this.dialogVisible3 = false;
    },

    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    // 选框改变
    handleSelectionChange(val) {
      if (val.length == 0) {
        this.is_disabled = true;
      } else {
        this.is_disabled = false;
      }
      console.log(val);
      this.idList = val.map((item) => {
        return item.id;
      });
      this.idList = this.idList.join(",");
    },
    async getList() {
      const res = await getList({
        api: 'plugin.bbs.Adminbbs.postList',
        page: this.dictionaryNum,
        pagesize: this.pageSize,
        name: this.inputInfo.name,
        user_id: this.inputInfo.user_id,
        title: this.inputInfo.title,
        status: this.inputInfo.status,
        type: this.inputInfo.type,
        label_id: this.inputInfo.label_id
      })
      this.total = res.data.data.total
      this.is_tuijian = res.data.data.is_tuijian
      this.tableData = res.data.data.list
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },

    // 重置
    reset() {
      this.inputInfo.status = null
      this.inputInfo.name = null
      this.inputInfo.user_id = ''
      this.inputInfo.type = ''
      this.inputInfo.title = ''
      this.inputInfo.label_id = ''
    },

    // 查询
    demand() {
      this.inputInfo.label_id = ''
      this.currpage = 1
      this.current_num = 10
      this.loading = true
      this.dictionaryNum = 1
      this.getList().then(() => {
        this.loading = false
      })
    },

    // 查看
    viewDetails(row) {
      this.$router.push({
        path: '/plug_ins/tieba/articleDetails',
        query: {
          id: row.id
        }
      })
    },

    viewComment(row) {
      this.$router.push({
        path: '/plug_ins/tieba/comment',
        query: {
          id: row.id
        }
      })
    },
    delAll() {
      this.$confirm(
        this.$t('确认要删除吗?'),
        this.$t("integralGoodsList.ts"),
        {
          confirmButtonText: this.$t("integralGoodsList.okk"),
          cancelButtonText: this.$t("integralGoodsList.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          operation({
            api: "plugin.bbs.Adminbbs.delPost",
            id: this.idList,
          }).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              this.getList()
              this.$message({
                type: 'success',
                message: this.$t('tieaba.article.czcg'),
                offset: 100
              })
            }
          });
        })
    },
    Delete(row) {
      this.$confirm('确认要删除吗?', this.$t('zdata.ts'), {
        confirmButtonText: this.$t('zdata.ok'),
        cancelButtonText: this.$t('zdata.off'),
        type: 'warning'
      })
        .then(() => {
          operation({
            api: 'plugin.bbs.Adminbbs.delPost',
            id: row.id
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.getList()
              this.$message({
                type: 'success',
                message: this.$t('tieaba.article.czcg'),
                offset: 100
              })
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('group.yqxsc'),
            offset: 100
          })
        })
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true
      console.log('e', e)
      // this.current_num = e
      this.pageSize = e
      this.getList().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.loading = true
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.getList().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },
    pass(row) {
      operation({
        api: 'plugin.bbs.Adminbbs.reviewPost',
        id: row.id,
        status: 2
      }).then(res => {
        console.log(res)
        if (res.data.code == '200') {
          this.getList()
          this.$message({
            type: 'success',
            message: this.$t('tieaba.article.czcg'),
            offset: 100
          })
        }
      })
    },
    // 拒绝
    reject(row) {
      this.dialogVisible3 = true
      this.id = row.id
    },
    determine2() {
      operation({
        api: 'plugin.bbs.Adminbbs.reviewPost',
        id: this.id,
        refuse_text: this.ruleForm3.reason,
        status: 3
      }).then(res => {
        console.log(res)
        if (res.data.code == '200') {
          this.dialogVisible3 = false
          this.getList()
          this.$message({
            type: 'success',
            message: this.$t('tieaba.article.czcg'),
            offset: 100
          })
        }
      })
    },
    cancelRecommendation(row) {
      operation({
        api: 'plugin.bbs.Adminbbs.is_home_recommend',
        id: row.id
      }).then(res => {
        console.log(res)
        if (res.data.code == '200') {
          this.getList()
          this.$message({
            type: 'success',
            message: this.$t('tieaba.article.czcg'),
            offset: 100
          })
        }
      })
    },
    recommend(row) {
      operation({
        api: 'plugin.bbs.Adminbbs.is_home_recommend',
        id: row.id
      }).then(res => {
        if (res.data.code == '200') {
          this.getList()
          this.$message({
            type: 'success',
            message: this.$t('tieaba.article.czcg'),
            offset: 100
          })
        }
      })
    },
  }
}
