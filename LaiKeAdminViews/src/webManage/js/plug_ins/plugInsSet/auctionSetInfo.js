import { PlugSet, getPlugSet } from '@/api/mall/plugInsSet'
import Config from '@/packages/apis/Config'
import { VueEditor } from 'vue2-editor'
import { getStorage } from '@/utils/storage'
import axios from 'axios'
export default {
  name: 'groupSetInfo',
  components: {
    VueEditor
  },
  data() {
    return {
      ruleForm: {
        isOpen: 0, //是否开启插件 0-不开启 1-开启
        lowPepole: '', //最低开拍人数
        waitTime: '', //出价等待时间(秒)
        days: 7, //保留天数
        orderFailure: '', //订单付款倒计时
        content: '', //竞拍规则
        agreeTitle: '', //协议标题
        agreeContent: '' //竞拍协议
      },

      rules: {
        // agreeTitle: [
        //   { required: true, message: '请输入标题名称', trigger: 'blur' }
        // ],
        // agreeContent: [
        //   { required: true, message: '请输入协议内容', trigger: 'change' }
        // ]
      },

      actionUrl: Config.baseUrl,
      flag: true

    }
  },
  watch: {
    // 'ruleForm.days'() {
    //   if(this.ruleForm.days == '') {
    //     this.ruleForm.days = 7
    //   }
    // },
  },
  created() {
    this.getPlugSetInfo()
  },
  beforeRouteLeave(to, from, next) {
    if (JSON.stringify(this.ruleForm) == sessionStorage.getItem('ruleForm_auction')) {
      next()
    } else {
      console.log('表单变化，询问是否保存')
      next(false)
      this.$confirm(
        this.$t('plugInsSet.auctionSetInfo.sjygx'),
        this.$t('coupons.ts'),
        {
          distinguishCancelAndClose: true,
          confirmButtonText: this.$t('coupons.okk'),
          cancelButtonText: this.$t('coupons.ccel'),
          type: 'warning'
        }
      )
        .then(() => {
          // this.submitForm('ruleForm')
          next()
        })
        .catch(() => {
          // next()
          // next('/plug_ins/plugInsSet/plugInsList')
        })
    }
  },
  methods: {
    back() {
      this.$router.push({
        path: '/plug_ins/plugInsSet/plugInsList'
      })
    },
    async getPlugSetInfo() {
      let { entries } = Object
      let data = {
        api: 'plugin.auction.AdminAuction.getConfig',
      }
      let formData = new FormData()
      for (let [key, value] of entries(data)) {
        formData.append(key, value)
      }
      const res = await getPlugSet(formData)
      if (res.data.data.low_pepole) {
        let arr = res.data.data
        this.ruleForm.isOpen = arr.is_open
        this.ruleForm.lowPepole = arr.low_pepole
        this.ruleForm.waitTime = arr.wait_time
        this.ruleForm.days = arr.days
        this.ruleForm.orderFailure = arr.orderFailure
        this.ruleForm.content = arr.content
        this.ruleForm.agreeTitle = arr.agreeTitle
        this.ruleForm.agreeContent = arr.agreeContent
      }
      sessionStorage.setItem('ruleForm_auction', JSON.stringify(this.ruleForm))

    },
    handleImageAdded(file, Editor, cursorLocation, resetUploader) {
      var formData = new FormData()
      formData.append('file', file) //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl, //上传路径
        method: 'POST',
        params: {
          api: 'resources.file.uploadFiles',
          storeId: getStorage('laike_admin_userInfo').storeId,
          groupId: -1,
          uploadType: 2,
          coverage:1,
          accessId: this.$store.getters.token
        },
        data: formData
      })
        .then(result => {
          console.log(result)
          let url = result.data.data.imgUrls[0] // 返回给你的图片路径
          Editor.insertEmbed(cursorLocation, 'image', url)
          
        })
        .catch(err => {
          console.log(err)
        }).finally(() => {
          // 【关键】重置 input，让同一张图可以再次触发
          resetUploader()
          this.clearImageUploadCache()

        })
    },
    /**
   * 清空图片上传缓存，允许重复选择同一张图片
   */
    clearImageUploadCache() {
      const fileInput = document.querySelector('.ql-image + input[type="file"]');
      if (fileInput) {
        // 清空缓存（关键）
        fileInput.value = "";
        // 触发 change 事件，同步编辑器状态（2.10.2 版本需要）
        fileInput.dispatchEvent(new Event("change", { bubbles: true }));
      } else {
        // 兜底：遍历查找编辑器内的图片上传 input（防止选择器偏差）
        const allFileInputs = document.querySelectorAll('input[type="file"][accept="image/*"]');
        Array.from(allFileInputs).forEach((input, index) => {
          input.value = "";
        });
      }
    },

    submitForm(formName) {
      this.$refs[formName].validate(async valid => {
        if (valid) {
          try {
            if (this.ruleForm.lowPepole < 2) {
              this.$message({
                message: this.$t('plugInsSet.auctionSetInfo.text2'),
                type: 'warning',
                offset: 100
              })
              return
            }
            if (this.ruleForm.waitTime < 5) {
              this.$message({
                message: this.$t('plugInsSet.auctionSetInfo.text3'),
                type: 'warning',
                offset: 100
              })
              return
            }
            if (this.ruleForm.waitTime > 30) {
              this.$message({
                message: this.$t('plugInsSet.auctionSetInfo.text4'),
                type: 'warning',
                offset: 100
              })
              return
            }
            if (this.ruleForm.agreeTitle == '') {
              this.$message({
                message: this.$t('plugInsSet.auctionSetInfo.text5'),
                type: 'warning',
                offset: 100
              })
              return
            }
            if (this.ruleForm.agreeContent == '') {
              this.$message({
                message: this.$t('plugInsSet.auctionSetInfo.text6'),
                type: 'warning',
                offset: 100
              })
              return
            }
            if (this.ruleForm.days == '') {
              this.ruleForm.days = 7
            }
            let data = {
              api: 'plugin.auction.AdminAuction.addConfig',
              ...this.ruleForm
            }
            let { entries } = Object
            let formData = new FormData()
            for (let [key, value] of entries(data)) {
              formData.append(key, value)
            }
            if (!this.flag) {
              return
            }
            this.flag = false
            PlugSet(formData).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('plugInsSet.czcg'),
                  type: 'success',
                  offset: 100
                })
                this.getPlugSetInfo()
                setTimeout(() => {
                  this.$router.push({
                    path: '/plug_ins/plugInsSet/plugInsList'
                  })
                }, 2000)
              }
            }).finally(() => {
              setTimeout(() => {
                this.flag = true
              }, 1500)
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              offset: 100
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
