import { uiIndex, uiSave, uiDel, uiIsShowSwitch, uiMove, uiTop, bannerPathList } from '@/api/plug_ins/template'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { setTimeout } from 'core-js'
export default {
  name: 'navigationBar',
  mixins: [mixinstest],
  data() {
    return {
      radio1:this.$t('template.uidhl'),
      button_list:[],
      tableData: [],
      loading: true,
      menuId:'',
      // 弹框数据
      dialogVisible: false,
      ruleForm: {
        name: '',
        img: '',
        class1: 0,
        url: '',
        is_display: 1,
        is_login:0
      },

      title: '',

      navUrl: '',

      classList1: [
        {
          value: 2,
          label: this.$t('template.navigationBar.sp')
        },
        {
          value: 1,
          label: this.$t('template.navigationBar.fl')
        },
        {
          value: 3,
          label: this.$t('template.navigationBar.dp')
        },
        {
          value: 0,
          label: this.$t('template.navigationBar.zdy')
        }
      ],

      classList2: [],
      baseUrl: '',

      rules: {
        reason: [
          { required: true, message: this.$t('template.navigationBar.qsrjj'), trigger: 'blur' }
        ],
      },
      id: null,
      // table高度
      tableHeight: null,
      tag: true
    }
  },

  created() {
    this.uiIndexs()
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
      const route=getStorage('route')
      route.forEach(item => {
          if(item.redirect=='/plug_ins/template/divTemplate'){
            item.children.forEach(e => {
              if(e.path=='navigationBar'){
                  return this.menuId=e.id
              }
            })
          }
      });
      let buttonList = await getButton ({
      api:'saas.role.getButton',
      menuId: this.menuId,
      })
      this.button_list=buttonList.data.data
      console.log(this.menuId,4444);
      console.log(this.button_list,"获取按纽权限")
    },

    async uiIndexs() {
      const res = await uiIndex({
        // api: 'admin.diy.uiIndex',
        api: 'plugin.template.AdminDiy.UiIndex',
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

    change(item) {
      if(item.value && item.value !== 0) {
        console.log('当前一级选择～', item);
        this.ruleForm.url = ''
        if(item.value){
          this.bannerPathLists(item.value)
        }
        if(item.value == 2) {
          this.navUrl = '/pagesC/goods/goodsDetailed'
        } else if(item.value == 1) {
          this.navUrl = '/pagesC/goods/goods'
        } else {
          this.navUrl = '/pagesB/store/store'
        }
      } else {
        console.log('当前二级选择～', item);
        this.ruleForm.url = item.parameter
      }
      console.log(item);
    },

    // 是否显示
    switchs(value) {
      uiIsShowSwitch({
        // api: 'admin.diy.uiIsShowSwitch',
        api: 'plugin.template.AdminDiy.UiIsShowSwitch',
        id: value.id
      }).then(res => {
        if(res.data.code == '200') {
          this.uiIndexs()
          console.log(res);
          this.$message({
            type: 'success',
            message: this.$t('template.cg'),
            offset: 100
          });
        }
      })
    },
    switchs1(value) {
      uiIsShowSwitch({
        // api: 'admin.diy.uiIsLoginSwitch',
        api: 'plugin.template.AdminDiy.UiIsLoginSwitch',
        id: value.id
      }).then(res => {
        if(res.data.code == '200') {
          this.uiIndexs()
          console.log(res);
          this.$message({
            type: 'success',
            message: this.$t('template.cg'),
            offset: 100
          });
        }
      })
    },

    // 移动
    moveUp(value) {
      if(value !== 0) {
        uiMove({
          // api: 'admin.diy.uiMove',
          api: 'plugin.template.AdminDiy.UiMove',
          id: this.tableData[value - 1].id,
          id1: this.tableData[value].id
        }).then(res => {
          if(res.data.code == '200') {
            this.uiIndexs()
            console.log(res);
            this.$message({
              type: 'success',
              message: this.$t('template.cg'),
              offset: 100
            });
          }
        })
      } else {
        uiMove({
          // api: 'admin.diy.uiMove',
          api: 'plugin.template.AdminDiy.UiMove',
          id: this.tableData[value + 1].id,
          id1: this.tableData[value].id
        }).then(res => {
          if(res.data.code == '200') {
            this.uiIndexs()
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
      uiTop({
        // api: 'admin.diy.uiTop',
        api: 'plugin.template.AdminDiy.UiTop',
        id: value.id
      }).then(res => {
          console.log(res);
          if(res.data.code == '200') {
              this.uiIndexs()
              this.$message({
                  type: 'success',
                  message: this.$t('template.cg'),
                  offset: 100
              })
          }
      })
    },

    // 删除
    Delete(value) {
      this.$confirm(this.$t('template.navigationBar.qrsc'), this.$t('template.ts'), {
        confirmButtonText: this.$t('template.okk'),
        cancelButtonText: this.$t('template.ccel'),
        type: 'warning'
      }).then(() => {
        uiDel({
            // api: 'admin.diy.uiDel',
            api: 'plugin.template.AdminDiy.UiDel',
            id: value.id
        }).then(res => {
          console.log(res);
          if(res.data.code == '200') {
            this.uiIndexs()
            this.$message({
                type: 'success',
                message: this.$t('template.cg'),
                offset: 100
            })
          }
        })
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除',
        //   offset: 100
        // });
      });
    },

    //选择一页多少条
    handleSizeChange(e){
      this.loading = true
      console.log(e);
      // this.current_num = e
      this.pageSize = e
      this.uiIndexs().then(() => {
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
      this.uiIndexs().then(() => {
        this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.loading = false
      })
    },


    // 弹框方法
    dialogShow(value, flag) {
      this.title = ''
      if(flag) {
        console.log('添加UI导航栏')
        this.title = '添加UI导航栏'
        this.ruleForm.name = '',
        this.ruleForm.img = '',
        this.ruleForm.class1 = 0,
        this.ruleForm.url = '',
        this.ruleForm.is_display = 1
        this.ruleForm.is_login=0
        this.$nextTick(()=>{
          this.$refs.upload.removeImg(0)
        })
      } else {
        console.log('编辑UI导航栏')
        this.title = '编辑UI导航栏'
        if(value.type !== 0) {
          this.bannerPathLists(value.type)
        }
        this.ruleForm.name = value.name
        this.ruleForm.img = value.image + '?' + new Date().getTime()
        this.ruleForm.class1 = value.type
        if(value.type == 0) {
          this.ruleForm.url = value.url
        } else {
          this.ruleForm.url = value.url.split('?')[1]
        }
        this.ruleForm.is_display = value.isshow
        this.ruleForm.is_login = value.is_login
      }
      this.dialogVisible = true
      if(value) {
        this.id = value.id
      }
    },

    handleAvatarSuccess(res, file) {
      console.log(res);
      this.ruleForm.img = res.data.imgUrls[0]
    },

    removeImgs() {
      this.ruleForm.img = ''
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

    handleClose(done) {
      this.dialogVisible = false
      if(!done) {
        this.$refs.upload.fileList = []
      }
      this.id = null
      this.$refs['ruleForm'].clearValidate()

    },

    submitForm2() {
      if(this.ruleForm.class1 == 0 && this.ruleForm.url.length > 100) {
        this.$message({
          message: this.$t('template.navigationBar.zddurl'),
          type: 'error',
          offset:100
        })
        return
      }
      if(this.title === '添加UI导航栏') {
        console.log('添加UI导航栏');
        let that = this
        if(!this.tag) {
          return
        }
        this.tag = false
        setTimeout(function() {
          that.tag = true
        },3000)

        uiSave({
          api: 'plugin.template.AdminDiy.UiSave',
          name: this.ruleForm.name,
          picUrl: this.ruleForm.img,
          type0: this.ruleForm.class1,
          url: this.navUrl? this.navUrl+ '?' + this.ruleForm.url:this.ruleForm.url,
          isShow: this.ruleForm.is_display,
          isLogin:this.ruleForm.is_login
        }).then(res => {
          if(res.data.code == '200') {
            this.uiIndexs()
            console.log(res);
            this.$message({
              type: 'success',
              message: this.$t('template.cg'),
              offset: 100
            });

            this.dialogVisible = false
            this.$refs.upload.fileList = []
          }
        })
      } else {
        console.log('编辑UI导航栏');
		let index = this.ruleForm.img.indexOf('?')
		if(index == -1){} else {
			this.ruleForm.img = this.ruleForm.img.substring(0, index)
		}
        uiSave({
          api: 'plugin.template.AdminDiy.UiSave',
          name: this.ruleForm.name,
          picUrl: this.ruleForm.img,
          type0: this.ruleForm.class1,
          url: this.ruleForm.class1 !== 0 ? this.navUrl + '?' + this.ruleForm.url : this.ruleForm.url,
          isShow: this.ruleForm.is_display,
          id: this.id,
          isLogin:this.ruleForm.is_login
        }).then(res => {
          if(res.data.code == '200') {
            this.uiIndexs()
            console.log(res);
            this.$message({
              type: 'success',
              message: this.$t('template.cg'),
              offset: 100
            });
            // this.$refs.upload.fileList = []
            this.dialogVisible = false
          }
        })
      }
    },

    getUrl() {

    }
  }
}
