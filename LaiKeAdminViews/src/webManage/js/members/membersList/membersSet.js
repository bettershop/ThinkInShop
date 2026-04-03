import { getUserConfigInfo, addUserRule } from '@/api/members/membersSet'
import { VueEditor } from 'vue2-editor'
import OSS from 'ali-oss'
export default {
    name: 'membersSet',
    components: {
        VueEditor
    },
    data() {
        return {
            radio1:this.$t('membersLists.yhsz'),
            rule: {
                wx_headimgurl:[
                    { required: true, message: this.$t('默认头像不能为空'), trigger: 'change' },
                ],
                wx_name: [
                    { required: true, message: this.$t('默认昵称不能为空'), trigger: 'blur' },
                ]
            },
            ruleForm: {
                // 基础信息
                wx_headimgurl: '',// 默认头像设置
                wx_name: '',// 默认昵称设置
                is_auto: '',// 自动续费提醒
                auto_time: '',
                str_method: [],// 开通方式
                preferentialGoods: [],// 享受优惠商品
                is_wallet: '',// 开通余额支付
                upgrade: '',// 等级晋升设置
                is_birthday: '',// 会员生日特权
                bir_multiple: '',
                is_jifen: '',// VIP等比例积分
                pointsOut: '',
                is_product: '',// 会员赠送商品
                valid: '',
                poster: '',// 分享海报设置
            },
            flag: false,

            pointsOutList1: [
                {
                    value: 1,
                    name: this.$t('membersLists.shh')
                },
                {
                    value: 2,
                    name: this.$t('membersLists.fkh')
                },
            ],

            openModeList: [
                {
                    value: '1',
                    name: this.$t('membersLists.by')
                },
                {
                    value: '2',
                    name: this.$t('membersLists.bj')
                },
                {
                    value: '3',
                    name: this.$t('membersLists.bn')
                },
            ],

            checkAll: false,
            isIndeterminate: true,
            preferentialGoodsList: [
                {
                    id: '1',
                    name: this.$t('membersLists.zjsp')
                },
                {
                    id: '2',
                    name: this.$t('membersLists.ptsp')
                },
                {
                    id: '3',
                    name: this.$t('membersLists.kjsp')
                },
                {
                    id: '4',
                    name: this.$t('membersLists.jpsp')
                },
            ],

            levelSetList: [
                {
                    id: '1',
                    name: this.$t('membersLists.gmhyfw')
                },
                {
                    id: '2',
                    name: this.$t('membersLists.bcesj')
                },
            ],

            pointsOutList2: [
                {
                    value: 1,
                    name: this.$t('membersLists.shh')
                },
                {
                    value: 0,
                    name: this.$t('membersLists.fkh')
                },
            ],

            open: false,
            open2: false,

            // 富文本编辑器数据
            content: ''
        }
    },

    created() {
        this.getUserConfigInfos()
    },

    watch: {

    },

    methods: {
        async getUserConfigInfos() {
            const res = await getUserConfigInfo({
                api: 'admin.user.getUserConfigInfo'
            })
            console.log(res);
            let userInfo = res.data.data
            let setInfo = res.data.data.gradeRule[0]
            this.ruleForm.wx_headimgurl = userInfo.wx_headimgurl,
            this.ruleForm.wx_name = userInfo.wx_name,
            this.ruleForm.is_auto = setInfo.is_auto,
            this.ruleForm.auto_time = setInfo.auto_time,
            this.ruleForm.is_wallet = setInfo.is_wallet,
            this.ruleForm.is_birthday = setInfo.is_birthday,
            this.ruleForm.bir_multiple = setInfo.bir_multiple,
            this.ruleForm.is_jifen = setInfo.is_jifen,
            this.ruleForm.pointsOut = setInfo.jifen_m,
            this.ruleForm.is_product = setInfo.is_product,
            this.ruleForm.valid = setInfo.valid,
            this.ruleForm.poster = setInfo.poster

            this.ruleForm.upgrade = setInfo.upgrade === "1,2" ? true : false,
            this.ruleForm.str_method = setInfo.method.split(','),
            this.ruleForm.preferentialGoods = setInfo.active.split(',')
            this.content = setInfo.rule
        },

        handleImageAdded(file, Editor, cursorLocation, resetUploader) {
            let random_name = new Date().getTime() + '.' + file.name.split('.').pop()
            const client = new OSS({
              region: "oss-cn-shenzhen.aliyuncs.com",
              secure: true,
              endpoint: 'oss-cn-shenzhen.aliyuncs.com',
              accessKeyId: "LTAI4Fm8MFnadgaCdi6GGmkN",
              accessKeySecret: "NhBAJuGtx218pvTE4IBTZcvRzrFrH4",
              bucket: 'laikeds'
            });
            client.multipartUpload(random_name, file)
            .then(res => {
              console.log(res);
              Editor.insertEmbed(cursorLocation, 'image', res.res.requestUrls[0])
              resetUploader()
            })
            .catch(err => {
              console.log(err)
            })
        },

        handleCheckAllChange(val) {
            console.log(this.checkAll);
            this.ruleForm.preferentialGoods = val ? this.preferentialGoodsList.map(item => { return item.id }) : [];
            this.isIndeterminate = false;
        },
        handleCheckedCitiesChange(value) {
            let checkedCount = value.length;
            this.checkAll = checkedCount === this.preferentialGoodsList.length;
            this.isIndeterminate = checkedCount > 0 && checkedCount < this.preferentialGoodsList.length;
        },

        switchs1(value) {
            console.log(value);
            if(value == 1) {
                this.open = true
            } else {
                this.open = false
            }
        },

        switchs2(value) {
            console.log(value);
            if(value == 1) {
                this.open2 = true
            } else {
                this.open2 = false
            }
        },

        submitForm(ruleForm) {
            this.$refs[ruleForm].validate((valid) => {
              if (valid) {
                this.flag = true
                if(this.ruleForm.wx_name.length > 150) {
                    this.$message({
                        message: '默认昵称过长，请重新输入',
                        type: 'error',
                        offset:100
                    })
                    this.ruleForm.wx_name = ''
                    return
                }
                addUserRule({
                    api: 'admin.user.addUserRule',
                    wxImgUrl: this.ruleForm.wx_headimgurl,
                    wxName: this.ruleForm.wx_name,
                    isAutoSwitch: this.ruleForm.is_auto,
                    autoDayExpire: this.ruleForm.auto_time,
                    method: this.ruleForm.str_method.join(','),
                    active: this.ruleForm.preferentialGoods.join(','),
                    isWallet: this.ruleForm.is_wallet,
                    upgrade: this.ruleForm.upgrade == true ? '2' : '',
                    isBirthday: this.ruleForm.is_birthday,
                    birMultiple: this.ruleForm.bir_multiple,
                    isProduct: this.ruleForm.is_product,
                    valid: this.ruleForm.valid,
                    isJifen: this.ruleForm.is_jifen,
                    jifenM: this.ruleForm.pointsOut,
                    poster: this.ruleForm.poster,
                    rule: this.content
                }).then(res => {
                    if(res.data.code == '200') {
                        this.$message({
                            message: this.$t("zdata.baccg"),
                            type: "success",
                            offset: 100,
                          });
                        setTimeout(() => {
                            this.flag = false
                        }, 1000)
                        this.getUserConfigInfos()
                    }
                    console.log(res);
                })
              } else {
                console.log('error submit!!');
                return
              }
            })
            
        },

        oninput2(num) {
            var str = num
            str = str.replace(/[^\.\d]/g,'');
            str = str.replace('.','');

            return str
        },

    }
}
