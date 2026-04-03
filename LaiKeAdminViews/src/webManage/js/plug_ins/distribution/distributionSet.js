import { VueEditor } from 'vue2-editor'
import { getDistributionConfigInfo, addDistributionConfigInfo } from '@/api/plug_ins/distribution'
import Config from "@/packages/apis/Config"
import axios from 'axios'
import { getStorage, setStorage } from '@/utils/storage'
export default {
    name: 'distributionSet',
    components: {
        VueEditor
    },
    data() {
        return {
            radio1:'8',
            ruleForm: {
                status: 1,
                neigou: 2,
                advertising: 1,
                adimage: '',
                uplevel: 2,
                pay: 1,
                cengji: 2,
                yjjisuan: 0,
                relationship: '',
                content: ''
            },

            rules: {
                cengji: [
                    { required: true, message: this.$t('distribution.distributionSet.qsrfxc'), trigger: 'blur' }
                ],
                yjjisuan: [
                    { required: true, message: this.$t('distribution.distributionSet.qxzyjj'), trigger: 'change' }
                ],
            },

            uplevelList: [
                {
                    value: 2,
                    name: this.$t('distribution.distributionSet.mzsy')
                },
                {
                    value: 1,
                    name: this.$t('distribution.distributionSet.mzyx')
                },
            ],

            payList: [
                {
                    value: 1,
                    name: this.$t('distribution.distributionSet.fkh')
                },
                {
                    value: 2,
                    name: this.$t('distribution.distributionSet.shh')
                },
            ],
            yjjisuanList: [
                {
                    value: 0,
                    name: this.$t('distribution.distributionSet.ddcjj')
                },
                {
                    value: 1,
                    name: this.$t('distribution.distributionSet.lr')
                },
                {
                    value: 2,
                    name: this.$t('distribution.distributionSet.ggsj')
                },
                {
                    value: 3,
                    name: this.$t('distribution.distributionSet.pvz')
                },
            ],
            relationshipList: [
                {
                    value: 0,
                    name: this.$t('distribution.distributionSet.zcbdyj')
                },
                {
                    value: 1,
                    name: this.$t('distribution.distributionSet.zcbdls')
                },
            ],

            id: null,
            tag: false,
            actionUrl: Config.baseUrl,
        }
    },

    created() {
        this.getDistributionConfigInfo()
    },

    watch: {
        'ruleForm.pay': {
            handler(newVal,oldVal) {
                if(this.tag) {
                    this.$alert(this.$t('distribution.distributionSet.qhjsfs'), {
                        confirmButtonText: this.$t('distribution.okk'),
                    })
                }

            }
        }
    },

    methods: {
        async getDistributionConfigInfo() {
            const res = await getDistributionConfigInfo({
                api: 'plugin.distribution.AdminDistribution.getDistributionConfigInfo'
            })
            console.log(res);
            let info = res.data.data
            this.id = info.id
            this.ruleForm.status = info.status
            this.ruleForm.neigou = info.parmamSetsMap.c_neigou
            this.ruleForm.advertising = info.advertising
            this.ruleForm.adimage = info.ad_image
            this.ruleForm.uplevel = info.parmamSetsMap.c_uplevel ? Number(info.parmamSetsMap.c_uplevel) : 2
            this.ruleForm.pay = info.parmamSetsMap.c_pay ? Number(info.parmamSetsMap.c_pay) : 1
            this.ruleForm.cengji = info.parmamSetsMap.c_cengji ? Number(info.parmamSetsMap.c_cengji) : 2
            this.ruleForm.yjjisuan = info.parmamSetsMap.c_yjjisuan ? Number(info.parmamSetsMap.c_yjjisuan) : 0
            this.ruleForm.relationship = info.relationship
            this.ruleForm.content = info.parmamSetsMap.content
            let me = this
            setTimeout(function() {
                me.tag = true
            },300)
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

        submitForm(formName) {
            this.$refs[formName].validate(async (valid) => {
              console.log(this.ruleForm);
              if (valid) {
                try {
                    if(Number(this.ruleForm.cengji) > 2) {
                        this.$message({
                            message: this.$t('distribution.distributionSet.fxcjbn'),
                            type: 'error',
                            offset: 100
                        })
                        return
                    }
                    if(this.ruleForm.content.length > 5000) {
                        this.$message({
                            message: this.$t('distribution.distributionSet.gzcdbn'),
                            type: 'error',
                            offset: 100
                        })
                        return
                    }
                    let { entries } = Object
                    let data = {
                        api: 'plugin.distribution.AdminDistribution.addDistributionConfigInfo',
                        id: this.id,
                        // status: this.ruleForm.status,
                        status: 1,
                        cengji: this.ruleForm.cengji,
                        uplevel: this.ruleForm.uplevel,
                        neigou: this.ruleForm.neigou,
                        pay: this.ruleForm.pay,
                        yjjisuan: this.ruleForm.yjjisuan,
                        advertising: this.ruleForm.advertising,
                        adimage: this.ruleForm.adimage,
                        relationship: this.ruleForm.relationship,
                        content: this.ruleForm.content
                    }

                    let formData = new FormData()
                    for (let [key, value] of entries(data)) {
                        formData.append(key, value)
                    }
                    addDistributionConfigInfo(formData).then(res => {
                        console.log(res)
                        if(res.data.code == '200') {
                        this.$message({
                            message: this.$t('distribution.cg'),
                            type: 'success',
                            offset: 100
                        })
                        this.getDistributionConfigInfo()
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

        oninput2(num) {
            var str = num
            str = str.replace(/[^\.\d]/g,'');
            str = str.replace('.','');

            return str
        },
    }
}
