import pageData from '@/api/constant/page'
import {isEmpty} from "element-ui/src/utils/util";
import { mixinstest } from '@/mixins/index'

export default {
  name: 'countryList',
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      language:"",
      page: pageData.data(),
      sortType: 1,
      switchFlag: false,
      // table高度
      tableHeight: null,

      is_disabled: true
    }
  },
  //组装模板
  created() {
    this.language = this.getCookit()

    if(this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.loadData()
  },

  mounted() {
    this.$nextTick(function() {
      this.getHeight()
    })
    window.addEventListener('resize',this.getHeight(),false)

  },

  methods: {
    // 获取cookiet
    getCookit(){
      let myCookie = document.cookie.split(';').map(item=>{
        let arr = item.split('=')
        return {name:arr[0],value:arr[1]}
      })
      let strCookit = ''
      myCookie.forEach(item=>{
        if(item.name.indexOf('language')!==-1){
          strCookit = item.value
        }
      })
      return strCookit
    },
    getHeight(){
      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },

    async loadData() {
      const res = await this.LaiKeCommon.select({
        api: 'admin.district.districtList',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        district_name: this.page.inputInfo.district_name
      });

      this.total = res.data.data.total
      this.page.tableData = res.data.data.list
      this.page.loading = false
      if (res.data.data.total < 10) {
        this.current_num = this.total
      }
      if(res.data.data.total < 5) {
        this.page.showPagebox = false
      }
      if (this.total < this.current_num) {
        this.current_num = this.total
      }

      // this.total = res.data.data.total
      // this.tableData = res.data.data.list
      // this.loading = false
      // if (res.data.data.total < 10) {
      //   this.current_num = this.total
      // }
      // if (this.total < this.current_num) {
      //   this.current_num = this.total
      // }

    },

    //排序
    sortChange(obj) {
      console.log('sort',obj);
      // this.sortType = 0;
      if (obj.order === 'descending') {
        this.sortType = 1;
      }
      if (obj.order === 'ascending') {
        this.sortType = 0;
      }
      this.demand()
    },
    // 重置
    reset() {
      this.page.inputInfo.district_name = ''
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
        if (this.page.tableData.length > 5) {
          this.page.showPagebox = true
        }
      })
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.page.loading = true
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
    handleSelectionChange(val) {
      if(val.length==0){
        this.is_disabled = true
      }else{
        this.is_disabled = false
      }
      this.idList = val.map(item => {
        return item.id
      })
    },
    //添加/编辑
    Save(id) {
      this.$router.push({
        name: "addOrEidtDistrict",
        params: {id: id},
        query: {
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize
        }
      })
    },

    // 删除
    Delete(id) {
      let num = 1;
      let paramIds = id;
      let datas = {
        api: 'admin.district.deleteDistrict'
      };
      if (isEmpty(id)) {
        paramIds  = this.idList.toString();
        datas.ids =  paramIds;
      }
      else
      {
        datas.id = id;
      }

      this.$confirm(this.$t('district.qrsc'), this.$t('district.ts'), {
        confirmButtonText: this.$t('district.okk'),
        cancelButtonText: this.$t('district.ccel'),
        type: 'warning'
      }).then(() => {
        this.LaiKeCommon.del(datas).then(res => {
          if(res.data.code == '200') {
            this.loadData();
            this.$message({
              type: 'success',
              message: this.$t('zdata.sccg'),
              offset:102,
            })
          }
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
