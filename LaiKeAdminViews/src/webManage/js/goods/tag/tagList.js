import { del, save, index } from "@/api/goods/tag/tagList";
import { isEmpty } from "element-ui/src/utils/util";
import { mixinstest } from "@/mixins/index";
import { valid } from "mockjs";
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
export default {
  name: 'tagList',
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      dialogVisible: false,
      title: '',
      dataForm: {
        id: null,
        name: '',
        lang_code: null,
        country_num: null,
        color:'#0099FF'
      },
      rules: {
        name: [
          { required: true, message: this.$t('tagList.bqmcwk'), trigger: 'blur' }
        ]
      },
      loading:true,
      tableData:[],
      colorlist:[
        {
          value:'1',
          label:'#FA5151'
        },
        {
          value:'2',
          label:'#FA9D3B'
        },
        {
          value:'3',
          label:'#FFC300'
        },
        {
          value:'4',
          label:'#07C160'
        },
        {
          value:'5',
          label:'#1485EE'
        },
      ],
      inputInfo:{
        name:'',
      },
      button_list: [],
      countryList: [],
      languages: [],
      lang_code: '',
      country_num: '',
      // table高度
      tableHeight: null,
      lang_disabled:false,
    }
  },
  //组装模板
  created() {
    this.lang_disabled = false
    this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal()
    this.loadData()
    this.getButtons()
    this.getCountrys();
    this.getLanguage();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {

    getIds(value) {
      this.country_num = value
    },

    getlangCode(value) {
      this.lang_code = value
    },

    async getCountrys() {
      try {
        const result = await this.LaiKeCommon.getCountries();
        this.countryList =  result.data.data;
      } catch (error) {
        console.error('获取国家列表失败:', error);
      }
    },

    async getLanguage() {
      try {
        const result = await this.LaiKeCommon.getLanguages();
        this.languages = result.data.data;
      } catch (error) {
        console.error('获取语种列表失败:', error);
      }
    },
    // 获取table高度
    getHeight() {
      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight);
    },

    //获取按纽权限
    async getButtons() {
      let route = getStorage('route')
      route.forEach(item => {
        if (item.path == 'tag') {
          return this.menuId = item.id
        }
      });
      let buttonList = await getButton({
        api: 'saas.role.getButton',
        menuId: this.menuId,
      })
      this.button_list = buttonList.data.data
      this.button_list = buttonList.data.data.map(item => {
        return item.title
      })
    },

    async loadData() {
      await this.getList().then()
    },
    // 获取列表
    async getList() {
      const res = await index({
        api: 'admin.label.index',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        name: this.inputInfo.name,
        lang_code: this.inputInfo.lang_code,
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
      this.dataForm.name = ''
      this.dataForm.lang_code = this.LaiKeCommon.getUserLangVal()
      this.dataForm.country_num = ''
      this.dataForm.color='#0099FF'
      this.title = this.$t('tagList.xzbq');
    },

    // 重置
    reset() {
      this.inputInfo.name = ''
      this.inputInfo.lang_code = ''
    },
    handleClose() {
      this.dataForm.id=''
      this.dataForm.name = ''
      this.dataForm.lang_code = ''
      this.dataForm.country_num = ''
      this.dataForm.color='#0099FF'
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
        this.title = this.$t('tagList.bjbq');
      }
      this.dialogVisible = true;
      const res = await index({
        api: 'admin.label.index',
        id: id
      });
      let data = res.data.data.list[0];
      if (!isEmpty(data)) {
        this.lang_disabled = true;
        this.dataForm.color = data.color;
        this.dataForm.id = data.id;
        this.dataForm.name = data.name;
        this.dataForm.lang_code = data.lang_code;
        this.dataForm.country_num = data.country_num;
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
          if (this.dataForm.name.length > 20) {
            this.$message({
              type: 'error',
              message: this.$t('tagList.bqm20'),
              offset: 100
            })
            return
          }
          if (this.dataForm.color == '' || !this.dataForm.color) {
            this.$message({
              type: 'error',
              message: this.$t('tagList.qxzbjs'),
              offset: 100
            })
            return
          }
          const res = await save({
            api: 'admin.label.addGoodsLabel',
            name: this.dataForm.name,
            lang_code: this.dataForm.lang_code,
            country_num: this.dataForm.country_num,
            id: this.dataForm.id,
            color:this.dataForm.color
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
      this.$confirm(this.$t('tagList.scqr'), this.$t('tagList.ts'), {
        confirmButtonText: this.$t('tagList.okk'),
        cancelButtonText: this.$t('tagList.ccel'),
        type: 'warning'
      }).then(() => {
        del({
          api: 'admin.label.delGoodsLabel',
          type: 2,
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
