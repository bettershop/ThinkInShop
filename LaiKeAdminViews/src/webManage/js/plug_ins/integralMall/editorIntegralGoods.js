import { getProList, addStock, addIntegral } from '@/api/plug_ins/integralMall'
import { setTimeout } from 'core-js'

export default {
    name: 'editorIntegralGoods',

    data() {
        return {
            ruleForm: {
                integral: '',
                shelves_num: '',
                yuan: '',
                residue: ''
            },

            rules: {
                integral: [
                    { required: true, message: this.$t('addIntegralGoods.qsrdhjf'), trigger: 'blur' }
                ],
                shelves_num: [
                    { required: true, message: this.$t('addIntegralGoods.qsrsjkc'), trigger: 'blur' }
                ],
                residue: [
                    { required: true, message: this.$t('addIntegralGoods.qsrsykc'), trigger: 'blur' }
                ],
            },

            tableData: [],

            // 弹框数据
            dialogVisible: false,
            ruleForm1: {
                num: "",
            },
            rules1: {
                num: [
                  { required: true, message: this.$t('addIntegralGoods.qsrzfkc'), trigger: "blur" },
                ]
            },
            throttle: true,
            attrId:'',
        }
    },

    created() {
        this.getProLists()
    },

    watch: {
        'ruleForm.yuan':{
            handler:function() {
                if(parseInt(this.ruleForm.yuan) > this.tableData[0].price) {
                  this.ruleForm.yuan = this.tableData[0].price
                }
            }
        },
    },

    methods: {
        async getProLists() {
            const res = await getProList({
                api: 'plugin.integral.AdminIntegral.index',
                id: this.$route.query.id
            })
            if(res.data.code==50786){
              this.$router.push('/mall/fastBoot/index')
          }
            console.log(res);
            this.tableData = res.data.data.list
            this.ruleForm.integral = res.data.data.list[0].integral
            this.ruleForm.shelves_num = res.data.data.list[0].max_num
            this.ruleForm.yuan = res.data.data.list[0].money
            this.ruleForm.residue = res.data.data.list[0].num
            this.attrId=res.data.data.list[0].attrId
        },

        tableHeaderColor({ row, column, rowIndex, columnIndex }) {
            if (rowIndex === 0) {
              return 'background-color: #f4f7f9;'
            }
        },

        // 弹框方法
        dialogShow(value) {
            this.dialogVisible = true
        },

        handleClose(done) {
            this.dialogVisible = false
            this.$refs['ruleForm1'].clearValidate()
        },

        // 增发库存
        determine(formName) {
            this.$refs[formName].validate(async (valid) => {
                if (valid) {
                    try {
                      if(!this.throttle) {
                        return
                      }
                      addStock({
                          api: "plugin.integral.AdminIntegral.addStock",
                          proId: this.$route.query.id,
                          num: this.ruleForm1.num
                      }).then((res) => {
                        this.throttle = false
                        let that = this
                        setTimeout(function() {
                          that.throttle = true
                        },500)
                        if (res.data.code == "200") {
                            this.$message({
                                message: this.$t('addIntegralGoods.cg'),
                                type: "success",
                                offset: 100,
                            });
                            this.dialogVisible = false;
                            this.getProLists()
                        }
                      });
                    } catch (error) {

                    }
                } else {

                }
            });
        },

        submitForm(formName) {
            this.$refs[formName].validate(async (valid) => {
              console.log(this.ruleForm);
              if (valid) {
                try {
                  addIntegral({
                    api: 'plugin.integral.AdminIntegral.addIntegral',
                    goodsid: this.tableData[0].goods_id,
                    id: this.$route.query.id,
                    stockNum: this.ruleForm.shelves_num,
                    integral: this.ruleForm.integral,
                    money: this.ruleForm.yuan,
                    attrId:this.attrId,
                  }).then(res => {
                    if(res.data.code == '200') {
                      this.$message({
                        message: this.$t('zdata.bjcg'),
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

            if(this.totlePrice && parseInt(this.ruleForm.yuan) > this.totlePrice) {
                str = this.totlePrice
            }
            return str
        },

        oninput2(num) {
            var str = num
            str = str.replace(/[^\.\d]/g,'');
            str = str.replace('.','');
            if(num==0){
              str = ''
          }
            return str
        },
    }
}
