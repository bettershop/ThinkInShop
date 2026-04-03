import { deliveryView, deliverySave, searchExpress } from '@/api/order/orderList'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
    name: 'goodsDelivery',

    data() {
        return {
            laikeCurrencySymbol: '￥',
            goodsTables: [],
            goodsNum: null,
            flag: false,
            courierList: [],
            mydata: [],
            // 弹框数据
            dialogVisible: false,
            ruleForm2: {
                kuaidi_name: null,
                kuaidi_no: null,
            },
            rules2: {
                kuaidi_name: [
                    { required: true, message: this.$t('goodsDelivery.qtxkdmc'), trigger: 'change' }
                ],
                kuaidi_no: [
                    { required: true, message: this.$t('goodsDelivery.qtxkddh'), trigger: 'blur' }
                ],
            },
            mynum: [],
            logistics_type: false
        }
    },

    created() {
        this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
        this.deliveryViews()
    },

    beforeRouteLeave(to, from, next) {
        if (to.name == 'orderLists') {
            to.params.dictionaryNum = this.$route.query.dictionaryNum
            to.params.pageSize = this.$route.query.pageSize
            to.params.radio1 = this.$route.query.radio1
        }
        next();
    },

    methods: {
        handleErrorImg(e) {
            console.log('图片报错了', e.target.src);
            e.target.src = ErrorImg
        },
        handleSelecChange(item) {
            console.log('快递公司zzzz', item, this.courierList.find(item2 => item2.id == item))
            let obj = this.courierList.find(item2 => item2.id == item)
            // if(obj.logistics_type){
            this.logistics_type = obj.logistics_type
            // }
        },
        async searchExpress() {
            const res = await searchExpress({
                api: 'admin.order.GetLogistics',
                sNo: this.$route.query.no
            })
            console.log(res)
            this.courierList = res.data.data.list
        },
        async deliveryViews() {
            const res = await deliveryView({
                api: 'admin.order.deliveryView',
                sNo: this.$route.query.no
            })
            this.goodsTables = res.data.data.goods
            this.goodsTables.forEach(e => {
                e.open = false
                this.mynum.push(e.deliverNum)
            })
            if (this.goodsTables && this.goodsTables.length == 1) {
                this.$refs.table.toggleAllSelection()
            }
            this.goodsNum = res.data.data.goods.num
            this.courierList = res.data.data.express.list
            // this.logistics_type = res.data.data.logistics_type
            if (res.data.data.logistics_type) {
                this.searchExpress()
            }
            // this.id = res.data.data.goods[0].id
            // this.id = res.data.data.goods.map(item => {
            //     return item.id
            // })
        },

        handleSelectionChange(val) {
            this.goodsTables.forEach(e => {
                e.open = false
            })
            console.log(val);
            if (val.length !== 0) {
                this.flag = true
                // this.id = val.map(item => {
                //     return item.id
                // })
                val.forEach(e => {
                    e.open = true
                })
            } else {
                this.flag = false
            }
            console.log(this.mydata);
        },
        handleChange(e) {
            console.log(e)
        },
        // 弹框方法
        dialogShow() {
            if (this.flag) {
                let list = []
                let isblock = false
                this.goodsTables.forEach((item, index) => {
                    if (item.open) {
                        if (!this.mynum[index]) {
                            isblock = true
                        }
                        list.push({

                            num: this.mynum[index],
                            detailId: item.id
                        })
                    }
                });
                console.log(isblock)
                if (isblock) {
                    this.$message({
                        message: '请输入发货数量',
                        type: 'error',
                        offset: 100
                    })
                    return
                }
                this.mydata = list
                this.dialogVisible = true
            } else {
                this.$message({
                    message: this.$t('goodsDelivery.qxzzs'),
                    type: 'error',
                    offset: 100
                })
            }
        },

        handleClose(done) {
            this.dialogVisible = false
            this.$refs.ruleForm2.clearValidate()
        },

        // 发货
        determine(formName2) {
            this.$refs[formName2].validate(async (valid) => {
                console.log(this.ruleForm2);
                if (valid) {
                    try {
                        if (!this.logistics_type && (this.ruleForm2.kuaidi_no.length < 10 || this.ruleForm2.kuaidi_no.length > 30)) {
                            this.$message({
                                message: '快递单号输入格式错误，必须大于等于10位哦',
                                type: 'error',
                                offset: 100
                            })
                            return
                        } else {
                            let api = 'admin.order.UnifiedShipment'
                            let expressid = this.ruleForm2.kuaidi_name
                            let courierNum = !this.logistics_type ? this.ruleForm2.kuaidi_no : ''
                            let type = this.logistics_type ? '2' : '1'
                            let psyInfo = ''
                            let orderList = this.mydata
                            let list = {
                                expressid, //物流公司id
                                courierNum, //物流号
                                type,	//发货类型 1普通发货 2电子面单 3商家配送
                                psyInfo, //配送员信息
                                orderList, //订单信息 detailId订单详情id num订单商品数量
                            }
                            list = JSON.stringify(list)
                            if (!this.flag) {
                                return
                            }
                            this.flag = false
                            deliverySave({ api, list }).then(res => {
                                if (res.data.code == '200') {
                                    this.$message({
                                        message: this.$t('zdata.fhcg'),
                                        type: 'success',
                                        offset: 100
                                    })
                                    this.$router.go(-1)
                                }
                                console.log(res);
                            }).finally(() => {
                                setTimeout(() => {
                                    this.flag = true
                                }, 1500)
                            })
                        }
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
        oninput3(str) {
            // 允许输入的字符：数字（0-9）和英文字符（a-zA-Z）
            const allowedChars = /^[0-9a-zA-Z]*$/;

            // 如果输入的字符串不符合允许的字符规则，则进行过滤
            if (!allowedChars.test(str)) {
                // 过滤掉不符合要求的字符
                return str.replace(/[^0-9a-zA-Z]/g, '');
            }

            return str;
        }
    }
}
