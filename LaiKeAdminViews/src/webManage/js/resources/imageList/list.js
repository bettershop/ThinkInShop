import pageData from "@/api/constant/page";
import { del, save, index, download } from "@/api/resources/imageManage";
import { exportss } from "@/api/export/index";
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import ErrorImg from '@/assets/images/default_picture.png'
export default {
  name: 'list',
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      page: pageData.data(),
      dialogVisible: false,
      inputInfo:{
        imageName: "",
        range: "",
      },
      imageName: null,
      data:null,
      choseList: [],
      dataForm: {},
      button_list: [],
      //需要下载的图片
      needImg: null,
      is_disabled: true,
      // table高度
      tableHeight: null
    }
  },
  //组装模板
  created() {
    this.loadData()
    this.getButtons()
  },
  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },
  methods: {
    // 图片错误处理
    handleErrorImg(e){
      console.log('图片报错了',e.target.src);
      e.target.src=ErrorImg
    },
    // 获取table高度
    getHeight() {
      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage('route')
      route.forEach(item => {
        if (item.path == 'imageList') {
          return this.menuId = item.id
        }
      });
      let buttonList = await getButton({
        api: 'saas.role.getButton',
        menuId: this.menuId,
      })
      this.button_list = buttonList.data.data
    },
    async loadData() {
      await this.getList().then()
    },
    // 获取列表
    async getList() {
      const res = await index({
        api: 'admin.resources.index',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        imageName: this.inputInfo.imageName,
        startTime: this.inputInfo.range?.[0]??'',
        endTime: this.inputInfo.range?.[1]??'',
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
      this.inputInfo.imageName = null
      this.inputInfo.range = null
    },
    handleClose() {
      this.dialogVisible = false;
    },

    // 查询
    demand() {
      this.showPagebox = false
      this.page.loading = true
      this.dictionaryNum = 1
      this.loadData().then(() => {
        this.page.loading = false
        if (this.page.tableData.length > 5) {
          this.showPagebox = true
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
    //选中项
    Chose(obj) {
      this.choseList = []
      obj.forEach((item) => {
        this.choseList.push(item.id);
      })
      if (!this.choseList.length) {
        this.is_disabled = true
      } else {
        this.is_disabled = false
      }
    },
    //批量下载
    async Download() {
      await exportss({
        api: 'admin.resources.downForZip',
        imgIds: this.choseList.toString(),
        exportType: 1
      }, 'pagegoods')
    },
    isFillList () {
      const totalPage = Math.ceil((this.total - this.page.tableData.length) / this.pageSize) // 总页数
      this.dictionaryNum = this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum
      this.dictionaryNum = this.dictionaryNum < 1 ? 1 : this.dictionaryNum
      this.getList()
    },
    //批量删除
    Del() {
      this.$confirm( this.$t('zdata.qdysctpm'), this.$t('zdata.ts'), {
        confirmButtonText: this.$t('zdata.ok'),
        cancelButtonText: this.$t('zdata.off'),
        type: 'warning'
      }).then(() => {
        del({
          api: 'admin.resources.del',
          imgIds: this.choseList.toString()
        }).then(res => {
          if (res.data.code == '200') {
            this.$message({
              type: 'success',
              message: this.$t('zdata.sccg')
            })
            this.isFillList()
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$t('zdata.yqxsc')
        })
      })
    }

  }

}
