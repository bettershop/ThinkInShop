import { getDistributionInfo, editeDistribution } from '@/api/plug_ins/distribution'
export default {
    name: 'editorDistributors',

    data() {
        return {
            distributorsIndo: null,
        }
    },

    created() {
        this.getDistributionInfos()
    },

    beforeRouteLeave (to, from, next) {        
        if (to.name == 'auditList' || to.name == 'store') {
          to.params.dictionaryNum = this.$route.query.dictionaryNum
          to.params.pageSize = this.$route.query.pageSize
        }   
        next();
    },

    methods: {
        async getDistributionInfos() {
            const res = await getDistributionInfo({
                api: 'plugin.distribution.AdminDistribution.getDistributionInfo',
                id: this.$route.query.id
            })
            console.log(res)
            this.distributorsIndo = res.data.data.list[0]
            this.distributorsIndo.accumulative = this.distributorsIndo.accumulative.toFixed(2)
            this.distributorsIndo.tx_commission = this.distributorsIndo.tx_commission.toFixed(2)
            this.distributorsIndo.allamount = this.distributorsIndo.allamount.toFixed(2)
        },

        submitForm() {
            editeDistribution({
                api: 'plugin.distribution.AdminDistribution.editDistribution',
                id: this.$route.query.id,
                consumptionAmt: this.distributorsIndo.accumulative != '' ? this.distributorsIndo.accumulative : 0,
                withdrawalAmt: this.distributorsIndo.tx_commission != '' ? this.distributorsIndo.tx_commission : 0,
                achievementAmt: this.distributorsIndo.allamount != '' ? this.distributorsIndo.allamount : 0
            }).then(res => {
                if(res.data.code == '200') {
                    this.$message({
                        message:this.$t('distribution.cg'),
                        type: 'success',
                        offset:100
                    })
                    this.$router.go(-1)
                }
                console.log(res);
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
              str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/,'$1') // 小数点后只能输 1 位
            } else {
              str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/,'$1') // 小数点后只能输 2 位
            }
            return str
        },

        oninput2(num) {
            var str = num
            str = str.replace(/[^\.\d]/g,'');
            str = str.replace('.','');

            return str
        },
    }
}