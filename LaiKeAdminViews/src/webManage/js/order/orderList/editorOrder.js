import { editOrderView, saveEditOrder } from '@/api/order/orderList'
import cascade from '@/api/publics/cascade'
import { getItuList } from '@/api/members/membersSet'
export default {
    name: 'editorOrder',
    data() {
        return {
            laikeCurrencySymbol:'￥',
            dataInfo: null,
            goodsTables: [],
            totleInfo: null,
            code2: '', //国家区号
            countriesList:[], //国家列表
            restaurants: [], //异步查询建议列表

            sjpsDaList:[],//商家配送日期列表
            sjpsRqList:[],//商家配送时间列表
            ruleForm: {
                status: null,
                name: '',
                mobile: '',
                sheng: '',
                shi: '',
                xian: '',
                r_address: '',
                remarks: '',
                pay_price: '',
                sjpsData: '',
                sjpsTime: '',
                sjpsTimeId: 1,
            },

            rules:{
                name:[{required: true, message: '请填写收货人', trigger: 'blur'}],
                mobile:[{required: true, validator: (rule, value, callback) => {
                    if (!value) {
                      callback(new Error('请输入联系方式'))
                    } else {
                      const reg = /^1[3|4|5|6|7|8|9][0-9]\d{8}$/
                      if (reg.test(value)) {
                        callback()
                      } else {
                        return callback(new Error('请输入正确的电话'))
                      }
                    }
                  }, trigger: 'blur'}],
                xian:[{required: true, message: '请选择联系地址', trigger: 'change'}],
                r_address:[{required: true, message: '请填写详细地址', trigger: 'blur'}]
            },

            stateList: [
                {
                    value: '0',
                    label: '待付款'
                },
                {
                    value: '1',
                    label: '待发货'
                }
            ],// 订单状态

            //省市级联集
            shengList: {},
            shiList: {},
            xianList: {},

            isTag: true,
            orderStatus:null,
            // 订单类型
            goodsListType:this.$route.query.goodsListType
        }
    },

    created() {
        this.getSheng()
        this.editOrderViews().then(() => {
            this.cascadeAddress();
        })
        //商家配送
        this.sjpsRqList = [{name: '上午', id: 1},{name: '下午', id: 2}]
        this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
        this.queryAdd()
        this.getSelectCountrys()
    },

    beforeRouteLeave (to, from, next) {
        if (to.name == 'orderLists') {
          to.params.dictionaryNum = this.$route.query.dictionaryNum
          to.params.pageSize = this.$route.query.pageSize
          to.params.radio1 = this.$route.query.radio1
        }
        next();
    },

    methods: {
        queryAdd() {
            const data = {
                api: 'admin.user.getItuList',
                keyword: this.keyword
            }
            getItuList(data).then(res => {
                if (res.data.code == 200) {
                    this.restaurants = res.data.data
                    console.log('this.restaurants', this.restaurants)
                    sessionStorage.setItem('restaurants', JSON.stringify(this.restaurants))
                }
            })
        },
        // 获取国家列表
        async getSelectCountrys() {
            const res = await this.LaiKeCommon.getCountries()
            this.countriesList = res.data.data
        },
        // 异步查询建议列表的方法
        querySearchAsync(queryString, cb) {
            // 模拟异步请求
            setTimeout(() => {
                const results = queryString
                    ? this.restaurants.filter(this.createFilter(queryString))
                    : this.restaurants;
                // 调用回调函数，将查询结果传递给组件
                cb(results);
            }, 300);
        },
        createFilter(queryString) {
            return (country) => {
                const lowerCaseQuery = queryString.toLowerCase();
                return (
                    country.name.toLowerCase().indexOf(lowerCaseQuery) > -1 ||
                    country.code2.toLowerCase().indexOf(lowerCaseQuery) > -1 ||
                    country.zh_name.toLowerCase().indexOf(lowerCaseQuery) > -1
                );
            };
        },
        handleSelect(item) {
            console.log('选中的项:', item);
            this.state = item.code2; // 可以根据需求更新输入框显示的值
            this.code2 = item.code2;

        },
      //商家配送时间（当前至往后7天时间列表）
      getTime(index){
          var nowDate = new Date();
          nowDate.setDate(nowDate.getDate() + index);
          var yy = nowDate.getFullYear();
          var mm = nowDate.getMonth() + 1;
          var dd = nowDate.getDate();
          var hh = nowDate.getHours();
          var mmi = nowDate.getMinutes();
          var ss = nowDate.getSeconds();
          var endD = yy + "-" + mm + "-" + dd
          this.sjpsDaList.push(endD)
          return endD
      },
        async editOrderViews() {
            const res = await editOrderView({
                api: 'admin.order.editOrderView',
                sNo: this.$route.query.no
            })
            this.dataInfo = res.data.data.data
            this.goodsTables = res.data.data.detail
            this.totleInfo = res.data.data
            if(this.totleInfo.storeSelfInfo && this.totleInfo.storeSelfInfo.delivery_time){
              this.ruleForm.sjpsData = this.totleInfo.storeSelfInfo.delivery_time
              this.ruleForm.sjpsTimeId = this.totleInfo.storeSelfInfo.delivery_period
              this.ruleForm.sjpsTime = this.totleInfo.storeSelfInfo.delivery_period == 1 ? '上午': this.totleInfo.storeSelfInfo.delivery_period == 2 ? '下午':''
              for(var a = 1; a<8; a++){
                this.getTime(a)
              }
              console.log(this.sjpsDaList)
            }
            this.ruleForm.name = res.data.data.data.name
            if (res.data.data.cpc){
                this.code2 = res.data.data.cpc
            }
            this.ruleForm.mobile = res.data.data.data.mobile
            this.ruleForm.sheng = res.data.data.data.sheng
            this.ruleForm.shi = res.data.data.data.shi
            this.ruleForm.xian = res.data.data.data.xian
            this.ruleForm.r_address = res.data.data.data.r_address
            this.ruleForm.remarks = Object.keys(res.data.data.data.remarks).length === 0 ? '' : ''
            this.ruleForm.pay_price = this.totleInfo.pay_price
            if(this.dataInfo.status == '待付款') {
                // this.ruleForm.pay_price = this.totleInfo.pay_price
                this.ruleForm.status = '0'
            }
        },



        // 获取省级
        async getSheng() {
            const res = await cascade.getSheng()
            this.shengList = res.data.data
        },

        // 获取市级
        async getShi(sid, flag) {
            const res = await cascade.getShi(sid)
            this.shiList = res.data.data
            if (!flag) {
                this.ruleForm.shi = "";
                this.ruleForm.xian = "";
            }
        },

        // 获取县级
        async getXian(sid, flag) {
            const res = await cascade.getXian(sid)
            this.xianList = res.data.data
            if (!flag) {
                this.ruleForm.xian = "";
            }
        },

        //省市级联回显
        async cascadeAddress() {
            //省市级联
            for (const sheng of this.shengList) {
            if (sheng.districtName === this.ruleForm.sheng) {
                await this.getShi(sheng.id, true);
                for (const shi of this.shiList) {
                if (shi.districtName === this.ruleForm.shi) {
                    await this.getXian(shi.id, true);
                    break;
                }
                }
                break;
            }
            }
        },

        submitForm(formName) {
            let me = this
            this.$refs[formName].validate(async (valid) => {
              console.log(this.ruleForm);
              if (valid) {
                try {
                    if(!this.isTag) {
                        return
                    }
                    if(this.dataInfo.status == '待付款'){
                        if(this.ruleForm.status == 1){
                            this.orderStatus = 1
                        }else{
                            this.orderStatus = 0
                        }
                    }else{
                      this.orderStatus = 1
                    }
                    console.log('orderStatus',this.orderStatus);

                    saveEditOrder({
                        api: 'admin.order.saveEditOrder',
                        orderNo: this.$route.query.no,
                        userName: this.ruleForm.name,
                        tel: this.ruleForm.mobile,
                        shen: this.ruleForm.sheng,
                        shi: this.ruleForm.shi,
                        xian: this.ruleForm.xian,
                        address: this.ruleForm.r_address,
                        remarks: this.ruleForm.remarks,
                        order_status:  this.orderStatus,
                        orderAmt: this.dataInfo.status == '待付款' ? this.ruleForm.pay_price :null,
                        deliveryTime: this.ruleForm.sjpsData,
                        deliveryPeriod: this.ruleForm.sjpsTimeId,
                        cpc:this.code2
                    }).then(res => {
                        console.log(res);
                        if(res.data.code == '200') {
                            this.isTag = false
                            setTimeout(function() {
                                me.isTag = true
                            },500)
                            this.$message({
                                message: this.$t('zdata.bjcg'),
                                type: 'success',
                                offset:100
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
    }
}
