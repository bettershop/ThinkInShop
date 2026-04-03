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
      radio1: this.$t('tieaba.plgl'),
      inputInfo: {
        post_key: '',
        user_key: '',
        content: '',
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
      viewRow: {},
    }
  },

  created() {

    if (this.$route.query.id) {
      this.inputInfo.post_key = this.$route.query.id
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
        api: 'plugin.bbs.Adminbbs.getComment',
        page: this.dictionaryNum,
        pagesize: this.pageSize,
        post_key: this.inputInfo.post_key,
        user_key: this.inputInfo.user_key,
        content: this.inputInfo.content,
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
      this.inputInfo.post_key = ''
      this.inputInfo.user_key = ''
      this.inputInfo.content = ''
    },

    // 查询
    demand() {
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
      this.viewRow = row
      this.dialogVisible3 = true
    },
    delAll() {
      this.$confirm(
        this.$t("classification.qrwz1"),
        this.$t("integralGoodsList.ts"),
        {
          confirmButtonText: this.$t("integralGoodsList.okk"),
          cancelButtonText: this.$t("integralGoodsList.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          operation({
            api: "plugin.bbs.Adminbbs.delComment",
            ids: this.idList,
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
            api: 'plugin.bbs.Adminbbs.delComment',
            ids: row.id
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
