import { isEmpty } from "element-ui/src/utils/util";
import { mixinstest } from "@/mixins/index";
export default {
  name: 'langmanager',
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      dialogVisible: false,
      title: '',
      dataForm: {
        id: null,
        lang_name: '',
        lang_code: ''
      },
      rules: {
        lang_name: [
          { required: true, message: this.$t('langmanager.qsrsyzmc'), trigger: 'blur' }
        ],
        lang_code: [
          { required: true, message: this.$t('langmanager.qsrsyzbm'), trigger: 'blur' }
        ]
      },
      loading:true,
      tableData:[],
      inputInfo:{
        lang_name:'',
      },
      button_list: [],
      lang_code: '',
      // table高度
      tableHeight: null,
      lang_disabled:false,
    }
  },
  //组装模板
  created() {
    this.lang_disabled = false
    // this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal()
    this.loadData()
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {


    // 获取table高度
    getHeight() {
      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight);
    },


    async loadData() {
      await this.getList()
    },
    // 获取列表
    async getList() {
      const res = await this.LaiKeCommon.select({
        api: 'admin.lang.index',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        lang_name: this.inputInfo.lang_name,
        type: 2
      });
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (res.data.data.total < 10) {
        this.current_num = this.total
      }
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },

    dialogShow() {
      this.dialogVisible = true
      this.lang_disabled = false
      this.dataForm.id=''
      this.dataForm.lang_name = ''
      this.dataForm.lang_code = ""
      this.title = this.$t('tagList.xzbq');
    },

    // 重置
    reset() {
      this.inputInfo.lang_name = ''
      this.inputInfo.lang_code = ''
    },
    handleClose() {
      this.dataForm.id=''
      this.dataForm.lang_name = ''
      this.dataForm.lang_code = ''
      this.dialogVisible = false;
    },

    // 查询
    demand() {
      this.loading = true
      this.dictionaryNum = 1
      this.loadData().then(() => {
        this.loading = false
        if (this.tableData.length > 0) {
          this.showPagebox = true
        }
      })
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true
      this.pageSize = e
      this.loadData().then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.loading = true
      this.dictionaryNum = e
      this.pcurrpage = ((e - 1) * this.pageSize) + 1
      this.loadData().then(() => {
        this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.loading = false
      })

    },
    // 下载文件
    async Show(id) {
      if (!isEmpty(id)) {
        this.title = this.$t('langmanager.bjyz');
      }
      this.dialogVisible = true;
      const res = await this.LaiKeCommon.select({
        api: 'admin.lang.index',
        id: id
      });
      let data = res.data.data.list[0];

      if (!isEmpty(data)) {
        this.lang_disabled = true;
        this.dataForm.id = data.id;
        this.dataForm.lang_name = data.lang_name;
        this.dataForm.lang_code = data.lang_code;
      }
    },
    //添加/编辑
    async Save(dataForm) {
      this.$refs[dataForm].validate(async (valid) => {
        if (!valid) {
          // this.$message({
          //   type: 'error',
          //   message: this.$t('tagList.bqmcwk')
          // })
        } else {
          const res = await this.LaiKeCommon.add({
            api: 'admin.lang.addOrEditLang',
            lang_name: this.dataForm.lang_name,
            lang_code: this.dataForm.lang_code,
            id: this.dataForm.id
          });

          if (res.data.code == '200') {

            if (!isEmpty(res)) {
              if(!this.dataForm.id) {
                this.$message({
                  type: 'success',
                  offset: 100,
                  message: this.$t('zdata.tjcg')
                })
              } else {
                this.$message({
                  type: 'success',
                  offset: 100,
                  message: this.$t('zdata.bjcg')
                })
              }

            } else {
              // this.$message({
              //   type: 'error',
              //   message: this.title + '失败!'
              // })
            }
            this.handleClose()
            this.demand();
          }


        }
      })
    },

    // 删除
    Delete(id) {
      this.$confirm(this.$t('langmanager.scqr'), this.$t('langmanager.ts'), {
        confirmButtonText: this.$t('langmanager.okk'),
        cancelButtonText: this.$t('langmanager.ccel'),
        type: 'warning'
      }).then(() => {
         this.LaiKeCommon.del({
          api: 'admin.lang.delLang',
          id: id
        }).then(res => {
          // this.demand();
          if (this.dictionaryNum != 1 && this.tableData.length <= 1) {
            this.dictionaryNum--;
          }
          this.getList()
          this.$message({
            type: 'success',
            offset: 100,
            message: this.$t('zdata.sccg')
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
