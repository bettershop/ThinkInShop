import { get_class } from '@/api/plug_ins/integralMall'
import { getGoodsNotDistributionInfo, getDistributionGradeList, addDistributionGoods } from '@/api/plug_ins/distribution'
import { mixinstest } from '@/mixins/index'

export default {
    name: 'addGoods',
    mixins: [mixinstest],

    data() {
        return {
            ruleForm: {
                distributionSet: 2,
                level: '',
                direct_drive: '',
                between_push: '',
                pv: ''
            },

            rules: {
                distributionSet: [
                    { required: true, message: this.$t('distribution.addGoods.qxzfxsz'), trigger: 'change' }
                ],
                pv: [
                    { required: true, message: this.$t('distribution.addGoods.qsrpvz'), trigger: 'blur' }
                ]
            },


            customerDistributteSet: [],

            distributionSetList: [
                {
                    value: 2,
                    name: this.$t('distribution.addGoods.zdysz')
                },
                {
                    value: 1,
                    name: this.$t('distribution.addGoods.syfxdj')
                },
            ],

            levelList: [],

            select: '%',

            classList: [],
            brandList: [],
            inputInfo: {
                goodsClass: '',
                brand: '',
                name: '',
                mchName: ""
            },

            id: null,
            sid: null,

            tableData: [],

            tableRadio: '',

        }
    },

    created() {
        this.getGoodsNotDistributionInfos()
        this.getDistributionGradeLists()
        this.get_classs()
    },

    methods: {
        async getGoodsNotDistributionInfos() {
            const res = await getGoodsNotDistributionInfo({
                api: 'plugin.distribution.AdminDistribution.getGoodsNotDistributionInfo',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                classId: this.inputInfo.goodsClass[this.inputInfo.goodsClass.length - 1],
                myBrand: this.inputInfo.brand,
                goodsName: this.inputInfo.name,
                mchName: this.inputInfo.mchName
            })
            console.log(res);
            this.current_num = 10
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            if (this.total < this.current_num) {
                this.current_num = this.total
            }
            if (this.total == 0) {
                this.showPagebox = false
            } else {
                this.showPagebox = true
            }
        },

        async getDistributionGradeLists() {
            const res = await getDistributionGradeList({
                api: 'plugin.distribution.AdminDistribution.getDistributionGradeList'
            })
            this.levelList = res.data.data.gradeInfoList

            for (let i = 0; i < this.levelList.length; i++) {
                let gradeInfo = {};
                gradeInfo['id'] = this.levelList[i]['value']
                gradeInfo['direct_m'] = 0
                gradeInfo['indirect_m'] = 0
                //(0-100] 后台计算的时候 * 0.01 了
                gradeInfo['diy_discount'] = 100
                gradeInfo['direct_mode_type'] = '%'
                gradeInfo['indirect_mode_type'] = '%'
                gradeInfo['gradeName'] = this.levelList[i]['label']
                this.customerDistributteSet.push(gradeInfo)
            }

            this.levelList.unshift({
                label: this.$t('distribution.addGoods.qxzkjs'),
                value: ""
            })
        },

        reset() {
            this.inputInfo.goodsClass = ''
            this.inputInfo.brand = ''
            this.inputInfo.name = ''
            this.inputInfo.mchName = ''
            this.brandList = []
        },

        demand() {
            this.currpage = 1
            this.current_num = 10
            this.dictionaryNum = 1
            this.sid = null
            this.tableRadio = ''
            this.getGoodsNotDistributionInfos()
        },

        tableHeaderColor({ row, column, rowIndex, columnIndex }) {
            if (rowIndex === 0) {
                return 'background-color: #f4f7f9;'
            }
        },

        // 获取商品类别
        async get_classs() {
            const res = await get_class({
                api: 'admin.goods.choiceClass',
            })
            res.data.data.list.class_list[0].forEach((item, index) => {
                let obj = item
                this.classList.push({
                    value: obj.cid,
                    label: obj.pname,
                    index: index,
                    children: []
                })
            })
        },

        // 根据商品类别id获取商品品牌
        changeProvinceCity(value) {
            get_class({
                api: 'admin.goods.choiceClass',
                classId: value.length > 1 ? value[value.length - 1] : value[0]
            }).then(res => {
                let num = this.$refs.myCascader.getCheckedNodes()[0].data.index
                // this.brandList = res.data.data.list.brand_list.splice(-1,1) 不知道之前为什么要.splice(-1,1) 禅道50639
                this.brandList = res.data.data.list.brand_list

                if (res.data.data.list.class_list && res.data.data.list.class_list[0].length !== 0) {
                    this.classList[num].children = []
                    res.data.data.list.class_list[0].forEach((item, index) => {
                        let obj = item
                        this.classList[num].children.push({
                            value: obj.cid,
                            label: obj.pname,
                            index: index,
                            children: []
                        })
                    })
                }
            })
        },

        handleSelectionChange(e) {
            console.log(e);
            this.sid = e.id
        },

        submitForm(formName) {
            if (!this.sid) {
                this.$message({
                    message: this.$t('distribution.addGoods.qxzsp'),
                    type: 'error',
                    offset: 100
                })
                return
            }
            // if (this.ruleForm.distributionSet == 2) {
            //     console.log("======");
            //     console.log(JSON.stringify(this.customerDistributteSet));
            //     console.log("======");
            //     if (!this.ruleForm.direct_drive) {
            //         this.$message({
            //             message: this.$t('distribution.addGoods.qsrztfy'),
            //             type: 'error',
            //             offset: 100
            //         })
            //         return
            //     }
            //     if (!this.ruleForm.between_push) {
            //         this.$message({
            //             message: this.$t('distribution.addGoods.qsrjtfy'),
            //             type: 'error',
            //             offset: 100
            //         })
            //         return
            //     }
            // }
            this.$validateAndScroll(formName).then((res) => {
                if(res){
                    if (!this.ruleForm.pv || Number(this.ruleForm.pv) === 0) {
                        this.$message({
                            message: this.$t('distribution.addGoods.pvzmin'),
                            type: 'error',
                            offset: 100
                        }) 
                        return
                    }
                    try { 
                        let customerRuleSet = [];
                        if (this.ruleForm.distributionSet == 2) {
                            for (let i = 0; i < this.customerDistributteSet.length; i++) {
                                customerRuleSet.push(this.customerDistributteSet[i]);
                                customerRuleSet[i].direct_mode_type = (customerRuleSet[i].direct_mode_type == '%' ? 0 : 1);
                                customerRuleSet[i].indirect_mode_type = (customerRuleSet[i].indirect_mode_type == '%' ? 0 : 1);
                                let diy_discount = customerRuleSet[i].diy_discount;
                                // debugger
                                if (diy_discount < 0 || diy_discount > 100) {
                                    this.$message({
                                        message: this.$t('distribution.addGoods.diy_discount'),
                                        type: 'error',
                                        offset: 100
                                    })
                                    return
                                }
                            }
                        } else {
                            customerRuleSet = null
                        }

                        let data = {
                            api: 'plugin.distribution.AdminDistribution.addDistributionGoods',
                            sid: this.sid,
                            uplevel: this.ruleForm.level,
                            distributionRule: this.ruleForm.distributionSet,
                            directType: this.select == '%' ? 0 : 1,
                            directM: this.ruleForm.direct_drive,
                            indirectType: this.select == '%' ? 0 : 1,
                            indirectM: this.ruleForm.between_push,
                            pv: this.ruleForm.pv
                        };

                        if (customerRuleSet != null) {
                            data.customerDistributteSet = JSON.stringify(customerRuleSet);
                        }
                        addDistributionGoods(data).then(res => {
                            if (res.data.code == '200') {
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
                }
            }) 
        },

        //选择一页多少条
        handleSizeChange(e) {
            this.pageSize = e
            this.getGoodsNotDistributionInfos().then(() => {
                this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
                this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
            })
        },

        //点击上一页，下一页
        handleCurrentChange(e) {
            this.dictionaryNum = e
            this.currpage = ((e - 1) * this.pageSize) + 1
            this.getGoodsNotDistributionInfos().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
            })
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
                str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/, '$1') // 小数点后只能输 1 位
            } else {
                str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/, '$1') // 小数点后只能输 2 位
            }
            return str
        },

        oninput2(num) {

            var str = num
            str = str.replace(/[^\.\d]/g, '');
            str = str.replace('.', '');

            return str
        },
    }
}
