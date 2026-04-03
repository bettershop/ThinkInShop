import { getSecConfig, setSecConfig } from '@/api/plug_ins/preSale'
import { VueEditor } from 'vue2-editor'
import OSS from 'ali-oss'
import { getStorage } from '@/utils/storage'
import axios from 'axios'
import Config from "@/packages/apis/Config"

export default {
  name: 'preSaleSet',
  components: {
    VueEditor
  },
  data() {
    return {
      isop: false,
      inOrderNum: '',
      radio1: '5',
      ruleForm: {
        // isOpen: 1,
        package_mail: 0,
        package_num: '1',
        automatic_time: '',
        failure_time: '',
        afterSales_time: '',
        remind_day: '',
        remind_hours: '',
        auto_remind_day: '',
        balanceDesc: null,
        depositDesc: null,
        autoCommentContent: ''
      },

      rules: {
        depositDesc: [
          { required: true, message: this.$t('preSale.preSaleSet.qtxdj'), trigger: 'blur' }
        ],
        balanceDesc: [
          { required: true, message: this.$t('preSale.preSaleSet.qtxdh'), trigger: 'blur' }
        ],
        limit_num: [
          { required: true, message: this.$t('preSale.preSaleSet.qtxmr'), trigger: 'blur' }
        ],
        activity_push: [
          { required: true, message: this.$t('preSale.preSaleSet.qtxhd'), trigger: 'blur' }
        ],
        automatic_time: [
          { required: true, message: this.$t('preSale.preSaleSet.qsrzd'), trigger: 'blur' }
        ],
        failure_time: [
          { required: true, message: this.$t('preSale.preSaleSet.qsrdds'), trigger: 'blur' }
        ],
        afterSales_time: [
          { required: true, message: this.$t('preSale.preSaleSet.qsrdd'), trigger: 'blur' }
        ],
        remind_hours: [
          { required: true, message: this.$t('preSale.preSaleSet.qtxtx'), trigger: 'blur' }
        ],
        auto_remind_day: [
          { required: true, message: this.$t('preSale.preSaleSet.qtxzdh'), trigger: 'blur' }
        ],
      },

      actionUrl: Config.baseUrl,
      flag: true
    }
  },

  created() {
    this.getBase()
    this.getSecConfigs()
    this.inOrderNum = getStorage('inOrderNum')
  },

  //保存对比是否有改动数据，有则需要提示弹窗
  beforeRouteLeave(to, from, next) {
    if (JSON.stringify(this.ruleForm) == sessionStorage.getItem('ruleForm_preSaleSetInfo')) {
      next()
    } else {
      console.log('表单变化，询问是否保存')
      next(false)
      this.$confirm(
        this.$t('coupons.couponsSet.sjygx'),
        this.$t('coupons.ts'),
        {
          distinguishCancelAndClose: true,
          confirmButtonText: this.$t('coupons.okk'),
          cancelButtonText: this.$t('coupons.ccel'),
          type: 'warning'
        }
      )
        .then(() => {
          //this.submitForm('ruleForm')
          next()
        })
        .catch(() => {
          //next()
        })
    }
  },

  methods: {
    back() {
      this.$router.push({
        path: "/plug_ins/plugInsSet/plugInsList",
      });
    },
    getBase() {
      this.goodsEditorBase = Config.baseUrl
    },
    async getSecConfigs() {
      const res = await getSecConfig({
        api: 'plugin.presell.AdminPreSell.getPreSellConfig'
      })
      console.log(res.data.data, "赋值");
      const Config = res.data.data.config
      if (!Config) {
        sessionStorage.setItem('ruleForm_preSaleSetInfo', JSON.stringify(this.ruleForm))
        return
      }
      this.ruleForm.isOpen = Config.is_open
      this.ruleForm.package_mail = Config.package_settings
      if (Config.same_piece) { this.ruleForm.package_num = Config.same_piece }
      this.ruleForm.automatic_time = Config.auto_the_goods / 86400
      this.ruleForm.failure_time = Config.order_failure / 3600
      this.ruleForm.afterSales_time = Config.order_after / 86400
      this.ruleForm.remind_day = parseInt(Math.floor(Config.deliver_remind / 86400))
      this.ruleForm.remind_hours = Math.floor(((Config.deliver_remind - this.ruleForm.remind_day * 3600 * 24) % 86400) / 3600)
      this.ruleForm.auto_remind_day = Config.auto_good_comment_day / 86400
      this.ruleForm.balanceDesc = Config.balance_desc
      this.ruleForm.depositDesc = Config.deposit_desc
      this.ruleForm.autoCommentContent = Config.auto_good_comment_content
      this.isop = this.ruleForm.auto_remind_day > 0 ? true : false
      //保存对比是否有改动数据，有则需要提示弹窗
      sessionStorage.setItem('ruleForm_preSaleSetInfo', JSON.stringify(this.ruleForm))
    },
    handleImageAdded(file, Editor, cursorLocation, resetUploader) {
      var formData = new FormData();
      formData.append("file", file) //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl,//上传路径
        method: "POST",
        params: {
          api: 'resources.file.uploadFiles',
          storeId: getStorage('laike_admin_userInfo').storeId,
          groupId: -1,
          uploadType: 2,
          coverage:1,
          accessId: this.$store.getters.token
        },
        data: formData
      }).then(result => {
        let url = result.data.data.imgUrls[0]; // 返回给你的图片路径
        Editor.insertEmbed(cursorLocation, "image", url); 
      })
        .catch(err => {
          console.log(err);
        })
        .finally(() => {
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
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm, "保存");
        if (valid) {
          try {
            if (this.isop && this.ruleForm.autoCommentContent == '') {
              this.ruleForm.autoCommentContent = '评价方未及时评价，系统自动默认好评'
            }
            let { entries } = Object
            let data = {
              api: 'plugin.presell.AdminPreSell.addPreSellConfig',
              // isOpen: this.ruleForm.isOpen,
              isFreeShipping: this.ruleForm.package_mail,
              goodsNum: Math.round(this.ruleForm.package_num),
              autoReceivingGoodsDay: Math.round(this.ruleForm.automatic_time),
              orderInvalidTime: Math.round(this.ruleForm.failure_time),
              returnDay: Math.round(this.ruleForm.afterSales_time),
              deliverRemind: parseInt(this.ruleForm.remind_day) == 0 ? (parseInt(this.ruleForm.remind_hours) ? parseInt(this.ruleForm.remind_hours) * 3600 : 0) : (parseInt(this.ruleForm.remind_day) ? parseInt(this.ruleForm.remind_day) * 86400 : 0) + (parseInt(this.ruleForm.remind_hours) ? parseInt(this.ruleForm.remind_hours) * 3600 : 0),
              autoCommentDay: Math.round(this.ruleForm.auto_remind_day),
              balanceDesc: this.ruleForm.balanceDesc,//订货
              depositDesc: this.ruleForm.depositDesc,
              autoCommentContent: this.ruleForm.autoCommentContent,
              isOpen: this.ruleForm.isOpen,
            }

            let formData = new FormData()
            for (let [key, value] of entries(data)) {
              formData.append(key, value)
            }
            if (!this.flag) {
              return
            }
            this.flag = false
            setSecConfig(formData).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('plugInsSet.czcg'),
                  type: 'success',
                  offset: 102
                })
                this.getSecConfigs()
                setTimeout(() => {
                  //this.$router.go(-1)
                  this.$router.push({
                    path: '/plug_ins/plugInsSet/plugInsList'
                  })
                }, 1000)
              }
            }).finally(() => {
              setTimeout(() => {
                this.flag = true
              }, 1500)
            })
          } catch (error) {
            setTimeout(() => {
              this.flag = true
            }, 1500)
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true,
              offset: 102
            })
          }
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },
    mychange(e) {
      if (e) {
        this.ruleForm.auto_remind_day = 1
      } else {
        this.ruleForm.auto_remind_day = 0
      }
    },
    oninput3(num) {
      if (num == 0) { return }
      var str = num;
      str = str.replace(/[^\.\d]/g, "");
      str = str.replace(".", "");
      return str;
    },
  }
}
