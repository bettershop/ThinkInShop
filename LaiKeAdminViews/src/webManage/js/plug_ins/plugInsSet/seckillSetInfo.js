import { getSecConfig, setSecConfig } from '@/api/plug_ins/seckill'
import { VueEditor } from 'vue2-editor'
import OSS from 'ali-oss'
import Config from "@/packages/apis/Config"
import axios from 'axios'
import { getStorage, setStorage } from '@/utils/storage'

export default {
  name: 'seckillSet',
  components: {
    VueEditor
  },
  data() {
    var validatePass = (rule, value, callback) => {
      if (value.length > 4) {
        callback(new Error(this.$t('seckill.seckillSet.cd4w')));
      } else {
        callback();
      }
    };
    return {
      radio1: '4',
      actionUrl: Config.baseUrl,
      ruleForm: {
        isOpen: 1,
        limit_num: 0,
        set_trailer: 0,
        trailen_time: 1,
        activity_push: '',
        package_mail: 0,
        package_num: '',
        automatic_time: '',
        failure_time: '',
        afterSales_time: '',
        remind_day: '',
        remind_hours: '',
        auto_remind_day: '',
        content: '',

        set_trailer2: 1,
        set_trailer3: 1,
        trailen_time3: '',

        autoCommentContent: '', //好评内容
      },
      isClose: false,
      rules: {
        limit_num: [
          { required: true, message: this.$t('seckill.seckillSet.qsrmrxg'), trigger: 'blur' },
          { validator: validatePass, trigger: "blur" },
        ],
        activity_push: [
          { required: true, message: this.$t('seckill.seckillSet.qsrhdts'), trigger: 'blur' },
          { validator: validatePass, trigger: "blur" },
        ],
        automatic_time: [
          { required: true, message: this.$t('seckill.seckillSet.qsrzd'), trigger: 'blur' },
          { validator: validatePass, trigger: "blur" },
        ],
        failure_time: [
          { required: true, message: this.$t('seckill.seckillSet.qsrddsx'), trigger: 'blur' },
          { validator: validatePass, trigger: "blur" },
        ],
        afterSales_time: [
          { required: true, message: this.$t('seckill.seckillSet.qsrddsh'), trigger: 'blur' },
          { validator: validatePass, trigger: "blur" },
        ],
        remind_day: [
          { required: true, message: this.$t('seckill.seckillSet.qtxtxxz'), trigger: 'blur' },
          { validator: validatePass, trigger: "blur" },
        ],
        remind_hours: [
          { required: true, message: this.$t('seckill.seckillSet.qtxtxxz'), trigger: 'blur' },
          { validator: validatePass, trigger: "blur" },
        ],
        auto_remind_day: [
          { required: true, message: this.$t('seckill.seckillSet.qtxzdhp'), trigger: 'blur' },
          { validator: validatePass, trigger: "blur" },
        ],
      },
      attributeList: [],
      flag: true
    }
  },

  created() {
    this.getSecConfigs()
    if (this.attributeList.length == 0) {
      this.attributeList.push({
        value: ''
      })
    }
  },
  beforeRouteLeave(to, from, next) {
    if (JSON.stringify(this.ruleForm) == sessionStorage.getItem('ruleForm_seckil')) {
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
          // this.submitForm();
          next()
        })
        .catch(() => {
          // next();
          // next('/plug_ins/plugInsSet/plugInsList')
        });
    }
  },

  methods: {
    back() {
      this.$router.push({
        path: '/plug_ins/plugInsSet/plugInsList'
      })
    },
    addOne() {
      this.attributeList.push(
        {
          value: ''
        }
      )
    },

    minus(index) {
      if (this.attributeList.length !== 0) {
        this.attributeList.splice(index, 1)
      }
    },
    turnOnOff() {
      if (this.isClose && this.ruleForm.isOpen == 0) {
        this.$confirm(this.$t('seckill.seckillSet.scqr'), this.$t('seckill.ts'), {
          confirmButtonText: this.$t('seckill.okk'),
          cancelButtonText: this.$t('seckill.ccel'),
          type: 'warning'
        }).then(() => {
          this.ruleForm.isOpen = 0
          this.submitForm('ruleForm')
        }).catch(() => {
          this.ruleForm.isOpen = 1
        });
      }

    },
    async getSecConfigs() {
      const res = await getSecConfig({
        api: 'plugin.sec.AdminSec.getSecConfig'
      })
      console.log(res);
      const Config = res.data.data.res
      if (!Config) {
        sessionStorage.setItem('ruleForm_seckil', JSON.stringify(this.ruleForm))
        return
      }

      this.ruleForm.isOpen = Config.is_open
      this.ruleForm.limit_num = Config.buy_num
      this.ruleForm.set_trailer = Config.is_herald
      this.ruleForm.trailen_time = Config.heraldTime / 3600
      this.ruleForm.activity_push = Config.remind / 60
      this.ruleForm.package_mail = Config.package_settings
      this.ruleForm.package_num = Config.same_piece ? Config.same_piece : ''
      this.ruleForm.automatic_time = Config.auto_the_goods / 86400
      this.ruleForm.failure_time = Config.order_failure / 3600
      this.ruleForm.afterSales_time = Config.order_after / 86400
      // this.ruleForm.remind_day = parseInt(Math.floor(Config.deliver_remind / 86400))
      this.ruleForm.autoCommentContent = Config.auto_good_comment_content
      this.ruleForm.remind_hours = parseInt(Math.floor(Config.deliver_remind / 3600))
      this.ruleForm.auto_remind_day = Config.auto_good_comment_day / 86400
      this.ruleForm.content = Config.rule
      this.isClose = res.data.data.isClose
      if (!Config.auto_good_comment_day) {
        this.ruleForm.set_trailer3 = 0
      }
      if (Config.remind == '0') {
        this.ruleForm.set_trailer2 = 0
      }
      this.attributeList = Config.remind.split(',').map(item => {
        return {
          value: item
        }
      })
      sessionStorage.setItem('ruleForm_seckil', JSON.stringify(this.ruleForm))
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
        // Editor.setSelection(length + 1)
        resetUploader();
      })
        .catch(err => {
          console.log(err);
        });
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            if (this.ruleForm.package_num === 0 || this.ruleForm.package_num == '0') {
              this.$message({
                message: this.$t('seckill.seckillSet.byspw'),
                type: 'error',
                offset: 100,
                showClose: true
              })
              return
            }
            if (this.ruleForm.package_mail == 1 && this.ruleForm.package_num.length > 4) {
              this.$message({
                message: this.$t('seckill.seckillSet.byspd4'),
                type: 'error',
                offset: 100,
                showClose: true
              })
              return
            }
            if ((this.ruleForm.trailen_time === 0 || this.ruleForm.trailen_time == '0') && this.ruleForm.set_trailer == 1) {
              this.$message({
                message: this.$t('seckill.seckillSet.ygsjw'),
                type: 'error',
                offset: 100,
                showClose: true
              })
              return
            }
            if (this.ruleForm.set_trailer == 1 && this.ruleForm.trailen_time.length > 4) {
              this.$message({
                message: this.$t('seckill.seckillSet.ygsjc4'),
                type: 'error',
                offset: 100,
                showClose: true
              })
              return
            }
            let tag = false
            if (this.ruleForm.set_trailer2) {
              this.attributeList.map(item => {
                if (item.value == '') {
                  tag = true
                }
              })
            }
            if (this.ruleForm.set_trailer2 && tag) {
              this.$message({
                message: this.$t('seckill.seckillSet.qwsxxts'),
                type: 'error',
                offset: 100,
                showClose: true
              })
              return
            }
            let messages = []
            if (this.ruleForm.set_trailer2 && !tag) {
              this.attributeList.map(item => {
                messages.push(item.value)
              })
            }
            let { entries } = Object
            let params = {
              api: 'plugin.sec.AdminSec.setSecConfig',
              isOpen: this.ruleForm.isOpen,
              buyNum: Math.round(this.ruleForm.limit_num),
              isHerald: this.ruleForm.set_trailer,
              heraldTime: Math.round(this.ruleForm.trailen_time),
              remind: this.ruleForm.set_trailer2 ? messages.join(',') : 0,
              isFreeShipping: this.ruleForm.package_mail,
              goodsNum: Math.round(this.ruleForm.package_num),
              autoReceivingGoodsDay: Math.round(this.ruleForm.automatic_time),
              orderInvalidTime: Math.round(this.ruleForm.failure_time),
              returnDay: Math.round(this.ruleForm.afterSales_time),
              deliverRemind: parseInt(this.ruleForm.remind_hours) * 3600,
              autoCommentDay: this.ruleForm.set_trailer3 ? Math.round(this.ruleForm.auto_remind_day) : 0,
              autoCommentContent: this.ruleForm.autoCommentContent,
              rule: this.ruleForm.content
            }
            let formData = new FormData()
            for (let [key, value] of entries(params)) {
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
                  offset: 100
                })
                this.getSecConfigs()
                setTimeout(() => {
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
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true
            })
          }
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },

    oninput2(num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '');
      str = str.replace('.', '');

      return str
    },
  }
}
