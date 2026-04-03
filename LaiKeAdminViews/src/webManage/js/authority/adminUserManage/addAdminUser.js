import { save, index, del } from "@/api/authority/authorityManage";
import { isEmpty } from "element-ui/src/utils/util";
export default {
  name: 'addAdminUser',
  //初始化数据
  data() {
    return {
      showIcon: require('../../../../assets/imgs/psd_show.png'),
      hideIcon: require('../../../../assets/imgs/psd_hide.png'),
      pwdObj: { pwdType: 'password' },
      pwdObj2: { pwdType: 'password' },
      pwdObj3: { pwdType: 'password' },
      dataMain: { passwordOld: null, roleId: null, passwordNew: null },
      roleList: {},
      customer_number: null,
      id: null,
      rules: {
        name: [{ required: true, message: this.$t('addAdminUser.qsrglyzh'), trigger: 'blur' },
        { validator: this.checkData, trigger: 'blur' }],
        password: [{ required: true, message: this.$t('addAdminUser.qsrglymm'), trigger: 'blur' }],
        passwordNew: [{ required: true, message: this.$t('addAdminUser.qsrglymm'), trigger: 'blur' }],
        passwordOld: [{ required: true, message: this.$t('addAdminUser.qzcsrmm'), trigger: 'blur' }],
        roleId: [{ required: true, message: this.$t('addAdminUser.qxzjs'), trigger: 'change' }],
      },
      fangdou: true,
    }
  },
  //组装模板
  created() {
    this.loadData();
  },
  beforeRouteLeave(to, from, next) {
    if (to.name == 'adminUserList' && this.$route.name == 'editAdminUser') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }
    next();
  },
  mounted() {
  },
  methods: {
    changeye(typeName, refName) {
      this.$set(
        this.pwdObj,
        `${typeName}`,
        this.pwdObj[`${typeName}`] === 'password' ? 'text' : 'password'
      )
      this.$refs[`${refName}`].focus()
    },
    changeye2(typeName, refName) {
      this.$set(
        this.pwdObj2,
        `${typeName}`,
        this.pwdObj2[`${typeName}`] === 'password' ? 'text' : 'password'
      )
      this.$refs[`${refName}`].focus()
    },
    changeye3(typeName, refName) {
      this.$set(
        this.pwdObj3,
        `${typeName}`,
        this.pwdObj3[`${typeName}`] === 'password' ? 'text' : 'password'
      )
      this.$refs[`${refName}`].focus()
    },
    checkData(rule, value, callback) {
      if (value) {
        if (/[\u4E00-\u9FA5]/g.test(value)) {
          callback(new Error('不能包含中文!'))
        } else {
          callback()
        }
      }
      callback()
    },
    async loadData() {
      this.id = this.$route.params.id;
      this.customer_number = this.$route.params.customer_number;
      if (!isEmpty(this.id)) {
        //加载管理员信息
        await index({
          api: 'admin.role.getAdminInfo',
          id: this.id,
        }).then(data => {
          if (!isEmpty(data)) {
            data = data.data.data;
            this.customer_number = data.customer_number;
            let main = data.list[0];
            main.password = null
            main.roleId = Number(main.role);
            this.dataMain = data.list[0];
          }
        });
      }
      //加载角色下拉
      await index({
        api: 'admin.role.getUserRoles',
      }).then(data => {
        if (!isEmpty(data)) {
          data = data.data.data.roleList;
          this.roleList = data;
        }
      });

    },
    async Save(formName) {

      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            if (isEmpty(this.id)) {
              if (this.dataMain.password !== this.dataMain.passwordOld) {
                this.$message({
                  message: this.$t('addAdminUser.lcmmby'),
                  type: 'error',
                  offset: 100
                })
                return
              }
            } else {
              // if (this.dataMain.passwordNew !== this.dataMain.passwordOld) {
              //   this.$message({
              //     message: this.$t('addAdminUser.lcmmby'),
              //     type: 'error',
              //     offset: 100
              //   })
              //   return
              // }
            }
            if (isEmpty(this.dataMain.roleId)) {
              this.$message({
                message: this.$t('addAdminUser.qxzjs'),
                type: 'error',
                offset: 100
              })
              return
            }
            let text = this.$t('zdata.tjcg');
            if (!isEmpty(this.id)) {
              text = this.$t('zdata.bjcg');
            }
            if (!this.fangdou) {
              return
            }
            this.fangdou = false
            await save({
              api: 'admin.role.addAdminInfo',
              id: this.id ? this.id : "",
              adminName: this.dataMain.name,
              roleId: this.dataMain.roleId,
              adminPWD: this.dataMain.password ? this.dataMain.password : this.dataMain.passwordNew,
            }).then(data => {
              if (data.data.code == '200' && !isEmpty(data)) {
                this.$message({
                  message: text,
                  type: 'success',
                  offset: 100
                })
                this.$router.go(-1);
              }
            }).finally(() => {
              setTimeout(() => {
                this.fangdou = true
              }, 1500)
            })

          } catch (error) {
            this.fangdou = true
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true
            })
          }
        } else {
          this.fangdou = true
          console.log('error submit!!');
          return false;
        }
      });

    },

    oninput2(num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '');
      str = str.replace('.', '');

      return str
    },


  }

}
