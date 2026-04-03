import { classIndex, classMove, classSwitch, classTop } from '@/api/plug_ins/template'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
export default {
  name: 'classification',
  mixins: [mixinstest],
  data() {
    return {
      radio1:this.$t('template.flgl'),
      button_list:[],
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      menuId:'',
    }
  },

  created() {
    this.classIndexs()
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
        route.forEach(item => {
            if(item.redirect=='/plug_ins/template/divTemplate'){
                item.children.forEach(e => {
                    if(e.path=='classification'){
                    return this.menuId=e.id
                    }
                })
            }
        });
        let buttonList = await getButton ({
        api:'saas.role.getButton',
        menuId:this.menuId,
        })
        this.button_list=buttonList.data.data
        console.log(this.button_list,"获取按纽权限")
    },
    async classIndexs() {
      const res = await classIndex({
        // api: 'admin.diy.classIndex',
        api: 'plugin.template.AdminDiy.ClassIndex',
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
      console.log(res);
    },

    // 是否显示
    switchs(value) {
      classSwitch({
        // api: 'admin.diy.classSwitch',
        api: 'plugin.template.AdminDiy.ClassSwitch',
        id: value.cid
      }).then(res => {
        if(res.data.code == '200') {
          this.classIndexs()
          console.log(res);
          this.$message({
            type: 'success',
            message: this.$t('template.cg'),
            offset: 100
          });
        }else{
          value.is_display = !value.is_display
        }
      })
    },

    // 移动
    moveUp(value) {
      if(value !== 0) {
        classMove({
          // api: 'admin.diy.classMove',
          api: 'plugin.template.AdminDiy.ClassMove',
          id: this.tableData[value - 1].cid,
          id1: this.tableData[value].cid
        }).then(res => {
          if(res.data.code == '200') {
            this.classIndexs()
            console.log(res);
            this.$message({
              type: 'success',
              message: this.$t('template.cg'),
              offset: 100
            });
          }
        })
      } else {
        classMove({
          // api: 'admin.diy.classMove',
          api: 'plugin.template.AdminDiy.ClassMove',
          id: this.tableData[value + 1].cid,
          id1: this.tableData[value].cid
        }).then(res => {
          if(res.data.code == '200') {
            this.classIndexs()
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
      classTop({
        // api: 'admin.diy.classTop',
        api: 'plugin.template.AdminDiy.ClassTop',
        id: value.cid
      }).then(res => {
          console.log(res);
          if(res.data.code == '200') {
              this.classIndexs()
              this.$message({
                  type: 'success',
                  message: this.$t('template.cg'),
                  offset: 100
              })
          }
      })
    },

    //选择一页多少条
    handleSizeChange(e){
      this.loading = true
      console.log(e);
      // this.current_num = e
      this.pageSize = e
      this.classIndexs().then(() => {
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
      this.classIndexs().then(() => {
        this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.loading = false
      })
    },
  }
}
