import { saveUser } from '@/api/members/membersList'
import { getTime } from '@/utils/utils'
import { getUserConfigInfo,getItuList ,generateAccount} from '@/api/members/membersSet'
export default {
    name: 'addMembers',

    data() {
        var validatePass = (rule, value, callback) => {
            if (value === '') {
              callback(new Error(this.$t('addMembers.qsryhmm')));
            } else {
              if (this.ruleForm.confirmMima !== '') {
                this.$refs.ruleForm.validateField('confirmMima');
              }
              callback();
            }
        };
        var validatePass2 = (rule, value, callback) => {
            if (value === '') {
                callback(new Error(this.$t('addMembers.qzcsrmm')));
            } else if (value !== this.ruleForm.mima) {
                callback(new Error(this.$t('addMembers.lcmmbyy')));
            } else {
                callback();
            }
        };

        return {
            pickerOptionsStart: {
                // 时间不能大于当前时间
                disabledDate: time => {
                  return time.getTime() > Date.now()
                }
              },
            laikeCurrencySymbol:'￥',
            countryNum:'',
            ruleForm: {
                membersHead: '',
                userName: '',
                grade: this.$t('addMembers.pthy'),
                zhanghao: '',
                mima: '',
                confirmMima: '',
                phone: '',
                source: '',
                birthday:  Date.now(),
                sex: 1,
                mailbox:''
            },
            sexList: [
                {
                  value: 1,
                  label: '男'
                },
                {
                  value: 2,
                  label: '女'
                },
                {
                  value: 0,
                  label: '未设置'
                },
              ],
            rule: {
                membersHead:[
                    { required: true, message: this.$t('addMembers.qscyh'), trigger: 'change' },
                ],
                userName: [
                    { required: true, message: this.$t('addMembers.qsryhmc'), trigger: 'blur' },
                ],
                zhanghao: [
                    { required: true, message: this.$t('addMembers.qsryhzh'), trigger: 'blur' },
                ],
                mima: [
                    { required: true, message: this.$t('addMembers.qsryhmm'), trigger: 'blur' },
                    { min: 6, max: 16, message: this.$t('addMembers.cdz6'), trigger: 'blur' },
                    { validator: validatePass, trigger: 'blur' }
                ],
                confirmMima: [
                    { required: true, message: this.$t('addMembers.qqrmm'), trigger: 'blur' },
                    { min: 6, max: 16, message: this.$t('addMembers.cdz6'), trigger: 'blur' },
                    { validator: validatePass2, trigger: 'blur', required: true }
                ],
                phone: [
                    { required: true, message: this.$t('addMembers.qsrsjhm'), trigger: 'blur' },
                ],
                mailbox: [
                    { required: true, message: this.$t('merchants.addmerchants.qsryx'), trigger: 'blur' },
                    {
                      validator: (rule, value, callback) => {
                        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
                        if (value && !emailRegex.test(value)) {
                          callback(new Error(this.$t('merchants.addmerchants.yxgscw')));
                        } else {
                          callback();
                        }
                      },
                      trigger: 'blur'
                    }
                  ],
                source: [
                    { required: true, message: this.$t('addMembers.qtxhzly'), trigger: 'blur' },
                ],
            },

            membersGrade: [
                {
                    value: '0',
                    label: this.$t('addMembers.pthy')
                },
                {
                    value: '30',
                    label: this.$t('addMembers.zshy')
                },
                {
                    value: '25',
                    label: this.$t('addMembers.hjhy')
                },
                {
                    value: '24',
                    label: this.$t('addMembers.bjhy')
                },
                {
                    value: '34',
                    label: this.$t('addMembers.csdj')
                }
            ],// 会员等级

            sourceList: [
                {
                    value: '6',
                    label: 'PC端'
                },
                {
                    value: '11',
                    label: 'APP端'
                },
                {
                    value: '2',
                    label: 'H5移动端'
                },
                {
                    value: '1',
                    label: '小程序'
                },
            ],// 账号来源
            keyword:'',
            state:'',
            restaurants:[]
        }
    },

    created() {
        this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
        this.getUserConfigInfos()
    },
    mounted(){
        if(sessionStorage.getItem('restaurants')){
            this.restaurants =JSON.parse( sessionStorage.getItem('restaurants'))

        }else{
            this.queryAdd()
        }
        // 自动生成账号
        generateAccount({
            api: 'admin.user.generateAccount'
        }).then(res=>{
            if(res.data.code == 200){
                this.ruleForm.zhanghao = res.data.data
            }
        })
    },
    watch: {
        'ruleForm.membersHead'() {
            if(this.ruleForm.membersHead) {
                this.$refs['membersHead'].clearValidate()
            }
        }
    },

    methods: {
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
          // 选择建议项时触发的方法
          handleSelect(item) {
            console.log('选中的项:', item);
            this.state = item.code2; // 可以根据需求更新输入框显示的值
            this.countryNum = item.num3;
          }
        ,
        queryAdd(){
          const data ={
              api:'admin.user.getItuList',
              keyword:this.keyword
          }
          getItuList(data).then(res=>{
            if(res.data.code == 200){
                this.restaurants = res.data.data
                sessionStorage.setItem('restaurants',JSON.stringify(this.restaurants))
            }
          })

        },
        async getUserConfigInfos() {
            const res = await getUserConfigInfo({
                api: 'admin.user.getUserConfigInfo'
            })
            console.log(res);
            this.ruleForm.membersHead = res.data.data.wx_headimgurl
            this.ruleForm.userName = res.data.data.wx_name
        },

        // 添加会员
        submitForm(formName) {
            this.$refs[formName].validate(async (valid) => {
            console.log(this.ruleForm);
            if (valid) {
                try {
                    if(this.ruleForm.userName.length > 16) {
                        this.$message({
                            message: '用户昵称不能大于16个字符',
                            type: 'error',
                            offset: 100
                        })
                        return
                    }
                     if(this.ruleForm.zhanghao.length < 6 || this.ruleForm.zhanghao.length > 16) {
                        this.$message({
                            message: '账号长度为6到16',
                            type: 'error',
                            offset: 100
                        })
                        return
                    }
                    if(!this.state || this.state.length==0){
                        this.$message({
                            message: '请选择区号',
                            type: 'error',
                            offset: 100
                        })
                        return
                    }
                    let isCpc = this.restaurants.some((item) => item.num3 == this.countryNum) 
                    if (!isCpc) {
                      this.$message({
                        message: '区号不正确！',
                        type: 'error',
                        offset: 102
                      })
                      return
                    }
                    saveUser({
                        api: 'admin.user.saveUser',
                        headerUrl: this.ruleForm.membersHead,
                        userName: this.ruleForm.userName,
                        grade: this.ruleForm.grade == this.$t('addMembers.pthy') ? 0 : this.ruleForm.grade,
                        zhanghao: this.ruleForm.zhanghao,
                        mima: this.ruleForm.mima,
                        phone: this.ruleForm.phone,
                        source: this.ruleForm.source == '小程序' ? 1 : this.ruleForm.source,
                        sex: this.ruleForm.sex,
                        cpc:this.state, // 国家区号
                        country_num : this.countryNum, // 国家代码
                        e_mail:this.ruleForm.mailbox,
                        birthday: this.ruleForm.birthday
                          ? getTime(this.ruleForm.birthday)
                          : null
                    }).then(res => {
                        if(res.data.code == '200') {
                            this.$message({
                                message: this.$t('commonLanguage.tjcg'),
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
    }
}
