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
            isop:false,
            inOrderNum:'',
            radio1:'5',
            ruleForm: {
                isOpen: 1,
                package_mail: 0,
                package_num: '',
                automatic_time: '',
                failure_time: '',
                afterSales_time: '',
                remind_day: '',
                remind_hours: '',
                auto_remind_day: '',
                balanceDesc:null,
                depositDesc:null,
                autoCommentContent:''
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
        }
    },

    created() {
      this.getBase()
      this.getSecConfigs()
      this.inOrderNum=getStorage('inOrderNum')
    },

    methods: {
      getBase() {
        this.goodsEditorBase = Config.baseUrl
      },
      async getSecConfigs() {
          const res = await getSecConfig({
              api: 'plugin.presell.AdminPreSell.getPreSellConfig'
          })
          console.log(res.data.data,"赋值");
          const Config = res.data.data.config
          if(!Config) {
            return
          }
          this.ruleForm.isOpen = Config.is_open
          this.ruleForm.package_mail = Config.package_settings
          this.ruleForm.package_num = Config.same_piece
          this.ruleForm.automatic_time = Config.auto_the_goods / 86400
          this.ruleForm.failure_time = Config.order_failure / 3600
          this.ruleForm.afterSales_time = Config.order_after / 86400
          this.ruleForm.remind_day = parseInt(Math.floor(Config.deliver_remind / 86400))
          this.ruleForm.remind_hours = Math.floor(((Config.deliver_remind - this.ruleForm.remind_day*3600*24) % 86400) / 3600)
          this.ruleForm.auto_remind_day = Config.auto_good_comment_day / 86400
          this.ruleForm.balanceDesc=Config.balance_desc
          this.ruleForm.depositDesc=Config.deposit_desc
          this.ruleForm.autoCommentContent=Config.auto_good_comment_content
          this.isop=this.ruleForm.auto_remind_day>0?true:false
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
              console.log(this.ruleForm,"保存");
              if (valid) {
                try {
                  if(this.isop && this.ruleForm.autoCommentContent == '') {
                    this.ruleForm.autoCommentContent = '评价方未及时评价，系统自动默认好评'
                  }
                  let { entries } = Object
                  let data = {
                    api: 'plugin.presell.AdminPreSell.addPreSellConfig',
                    isOpen: this.ruleForm.isOpen,
                    isFreeShipping: this.ruleForm.package_mail,
                    goodsNum: Math.round(this.ruleForm.package_num),
                    autoReceivingGoodsDay: Math.round(this.ruleForm.automatic_time),
                    orderInvalidTime: Math.round(this.ruleForm.failure_time),
                    returnDay: Math.round(this.ruleForm.afterSales_time),
                    deliverRemind: parseInt(this.ruleForm.remind_day) == 0 ? (parseInt(this.ruleForm.remind_hours) ? parseInt(this.ruleForm.remind_hours) * 3600 : 0) : (parseInt(this.ruleForm.remind_day) ? parseInt(this.ruleForm.remind_day) * 86400 : 0) + (parseInt(this.ruleForm.remind_hours) ? parseInt(this.ruleForm.remind_hours) * 3600 : 0),
                    autoCommentDay: Math.round(this.ruleForm.auto_remind_day),
                    balanceDesc: this.ruleForm.balanceDesc,//订货
                    depositDesc: this.ruleForm.depositDesc,
                    autoCommentContent:this.ruleForm.autoCommentContent
                  }

                  let formData = new FormData()
                  for (let [key, value] of entries(data)) {
                    formData.append(key, value)
                  }
                  setSecConfig(formData).then(res => {
                    if(res.data.code == '200') {
                      this.$message({
                        message: this.$t('preSale.cg'),
                        type: 'success',
                        offset: 100
                      })
                      this.getSecConfigs()
                    }
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
        mychange(e){
          if(e){
            this.ruleForm.auto_remind_day=1
          }else{
            this.ruleForm.auto_remind_day=0
          }
        },


    }
}
