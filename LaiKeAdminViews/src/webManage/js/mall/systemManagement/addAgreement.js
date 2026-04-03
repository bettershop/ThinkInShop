import { addAgreement } from '@/api/mall/systemManagement'
import { VueEditor } from 'vue2-editor'
import Config from "@/packages/apis/Config"
import axios from 'axios'
import { getStorage, setStorage } from '@/utils/storage'
import $ from 'jquery'

export default {
    name: 'addAgreement',

    components: {
        VueEditor
    },

    data() {
        return {
            radio1: '关于我们',
            actionUrl: Config.baseUrl,
            ruleForm: {
                agreement_title: '',
                protocol_type: 0,
                user_agreement: ''
            },

            protocolTypeList: [
                {
                    value: 0,
                    name: this.$t('systemManagement.protocolConfiguration.zcxy')
                },
                {
                    value: 2,
                    name: this.$t('systemManagement.protocolConfiguration.ysxy')
                },
                {
                    value: 1,
                    name: this.$t('systemManagement.protocolConfiguration.dpxy')
                },
                {
                    value: 3,
                    name: this.$t('systemManagement.protocolConfiguration.hyxy')
                }
            ],
            goodsEditorBase: '',
        }
    },

    watch: {
        'ruleForm.protocol_type': {
            handler() {
                this.ruleForm.user_agreement = ''
            }
        }
    },

    created() {
        this.getBase()
    },

    methods: {
        getBase() {
            this.goodsEditorBase = Config.baseUrl
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
            //   Editor.setSelection(length + 1)
              resetUploader();
          })
          .catch(err => {
              console.log(err);
          });
        },

        submitForm() {
            addAgreement({
                api: 'admin.system.addAgreement',
                title: this.ruleForm.agreement_title,
                type: this.ruleForm.protocol_type,
                content: this.ruleForm.user_agreement
            }).then(res => {
                if(res.data.code == '200') {
                    this.$message({
                      message: this.$t('zdata.tjcg'),
                      type: 'success',
                      offset: 102
                    })
                    // 返回上一级,携带参数
                    this.$router.push({
                      path: '/mall/basicConfiguration',
                      query: {
                        tabPosition: 7,
                      }
                    })
                  }
            })
        }
    }
}
