import { getDistributionGradeList ,addGradeInfo} from '@/api/plug_ins/distribution'
import { getStorage } from '@/utils/storage'
import Config from "@/packages/apis/Config";
import $ from 'jquery'
export default {
    name: 'addLevel',
    data() {
        return {
            ruleForm: {
                dengjiname: '',
                onebuy: '',
                recomm: [],
                manybuy: '',
                manyyeji: [],
                manypeople: [],
                zhekouIsOpen: 1,
                shopping_discounts: '100',
                siblingM: '',
                differentM: '',
                directM: '',
                indirectM: ''
            },

            siblingType: '1',
            differentType: '1',
            directType: '1',
            indirectType: '1',
            rules: {
                dengjiname: [
                    { required: true, message: this.$t('distribution.addLevel.qsrfxdj'), trigger: 'blur' }
                ],
                siblingM: [
                    { required: true, message: this.$t('distribution.addLevel.qsrtjfy'), trigger: 'blur' }
                ],
                differentM: [
                    { required: true, message: this.$t('distribution.addLevel.qsrjc'), trigger: 'blur' }
                ],
                directM: [
                    { required: true, message: this.$t('distribution.addLevel.qsrzt'), trigger: 'blur' }
                ],
                indirectM: [
                    { required: true, message: this.$t('distribution.addLevel.qsrjt'), trigger: 'blur' }
                ],
            },

            gradeList: [
                {
                    value: 0,
                    name: this.$t('distribution.addLevel.sheng')
                },
                {
                    value: 1,
                    name: this.$t('distribution.addLevel.shi')
                },
                {
                    value: 2,
                    name: this.$t('distribution.addLevel.xian')
                }
            ],

            checked1: false,
            checked2: false,
            checked3: false,
            checked4: false,
            checked5: false,

            goodsEditorBase: '',

            id: null
        }
    },

    created() {
        this.getDistributionGradeLists()
        this.getBase()
    },

    watch: {
        // 'ruleForm.shopping_discounts': {
        //     handler() {
        //         if(Number(this.ruleForm.shopping_discounts) > 1) {
        //             this.ruleForm.shopping_discounts = 1
        //         }
        //     }
        // }
    },

    methods: {
        async getDistributionGradeLists() {
            const res = await getDistributionGradeList({
                api: 'plugin.distribution.AdminDistribution.getDistributionGradeList'
            })
            console.log(res);
            this.gradeList = res.data.data.gradeInfoList
        },

        getBase() {
            this.goodsEditorBase = Config.baseUrl
        },

        submitForm(formName) {
            let obj = {}
            if(this.checked1) {
              if(!this.ruleForm.onebuy) {
                this.$message({
                  message: this.$t('distribution.addLevel.qwsjstj'),
                  type: 'error',
                  offset: 100
                })
                return
              }
              obj.onebuy = this.ruleForm.onebuy
            } 
            if(this.checked2) {
              if(this.ruleForm.recomm.length != 2) {
                this.$message({
                  message: this.$t('distribution.addLevel.qwsjstj'),
                  type: 'error',
                  offset: 100
                })
                return
              }
              if(this.ruleForm.recomm[0]<= 0){
                this.$message({
                  message: this.$t('distribution.addLevel.bnwl'),
                  type: 'error',
                  offset: 100
                })
                return
              }
              obj.recomm = this.ruleForm.recomm.toString()
            }
            if(this.checked3) {
              if(!this.ruleForm.manybuy) {
                this.$message({
                  message: this.$t('distribution.addLevel.qwsjstj'),
                  type: 'error',
                  offset: 100
                })
                return
              }
              obj.manybuy = this.ruleForm.manybuy
            } 
            if(this.checked4) {
              if(!this.ruleForm.manyyeji.length) {
                this.$message({
                  message: this.$t('distribution.addLevel.qwsjstj'),
                  type: 'error',
                  offset: 100
                })
                return
              }
              obj.manyyeji = this.ruleForm.manyyeji.toString()
            } 
            if(this.checked5) {
              if(this.ruleForm.manypeople.length != 2) {
                this.$message({
                  message: this.$t('distribution.addLevel.qwsjstj'),
                  type: 'error',
                  offset: 100
                })
                return
              }
              obj.manypeople = this.ruleForm.manypeople.toString()
            }
            console.log(JSON.stringify(obj));
            this.$refs[formName].validate(async (valid) => {
              console.log(this.ruleForm);
              if (valid) {
                try {
                  addGradeInfo({
                    api: 'plugin.distribution.AdminDistribution.addGradeInfo',
                    dengjiname: this.ruleForm.dengjiname,
                    levelobj: JSON.stringify(obj),
                    zhekouIsOpen: this.ruleForm.zhekouIsOpen,
                    discount: this.ruleForm.shopping_discounts,
                    siblingType: this.siblingType == '1' ? 0 : 1,
                    siblingM: this.ruleForm.siblingM,
                    differentType: this.differentType == '1' ? 0 : 1,
                    differentM: this.ruleForm.differentM,
                    directType: this.directType == '1' ? 0 : 1,
                    directM: this.ruleForm.directM,
                    indirectType: this.indirectType == '1' ? 0 : 1,
                    indirectM: this.ruleForm.indirectM,
                }).then(res => {
                    if(res.data.code == '200') {
                      this.$message({
                        message: this.$t('distribution.cg'),
                        type: 'success',
                        offset: 100
                      })
                      this.$router.go(-1)
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

        oninput(num, limit) {
            var str = num
            var len1 = str.substr(0, 1)
            var len2 = str.substr(1, 1)
            //如果第一位是0，第二位不是点，就用数字把点替换掉
            if (str.length > 1 && len1 == 0 && len2 != ".") {
              str = str.substr(1, 1)
            }
            //第一位不能是.
            if (len1 == ".") {
              str = ""
            }
            //限制只能输入一个小数点
            if (str.indexOf(".") != -1) {
              var str_ = str.substr(str.indexOf(".") + 1)
              if (str_.indexOf(".") != -1) {
                str = str.substr(0, str.indexOf(".") + str_.indexOf(".") + 1)
              }
            }
            //正则替换
            str = str.replace(/[^\d^\.]+/g, '') // 保留数字和小数点
            if (limit / 1 === 1) {
              str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/,'$1') // 小数点后只能输 1 位
            } else {
              str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/,'$1') // 小数点后只能输 2 位
            }

            // 检查是否为1-100之间的正整数
            if (limit === "int") {
              if (parseInt(str) > 100) {
                return 100
              }
              return !/^\+?[1-9]\d*$/.test(str) ? "" : str
            }

            if(this.ruleForm.price_type == 1 && this.totlePrice !== 0 && parseInt(this.ruleForm.price) > this.totlePrice) {
                str = this.totlePrice
            }
            return str
        },

    }
}