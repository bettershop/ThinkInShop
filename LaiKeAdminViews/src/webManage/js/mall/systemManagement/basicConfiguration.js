import {
  getSystemIndex,
  addSystemConfig,
  saveAgreement
} from '@/api/mall/systemManagement'
import cascade from '@/api/publics/cascade'
import { getStorage, setStorage } from '@/utils/storage'
import Config from '@/packages/apis/Config'

import underlyingConfiguration from '@/views/mall/systemManagement/basicConfigurationCommon/underlyingConfiguration.vue'
import sensitiveWords from '@/views/mall/systemManagement/basicConfigurationCommon/sensitiveWords.vue'
import logisticsAndPrinting from '@/views/mall/systemManagement/basicConfigurationCommon/logisticsAndPrinting.vue'
import pcsetter from '@/views/mall/systemManagement/basicConfigurationCommon/PCSetter.vue'
import appsetter from '@/views/mall/systemManagement/basicConfigurationCommon/appSetter.vue'
import wechatminiprogram from '@/views/mall/systemManagement/basicConfigurationCommon/wechatMiniProgram.vue'
import protocolconfiguration from '@/views/mall/systemManagement/basicConfigurationCommon/protocolconfiguration.vue'
import systemsSetter from '@/views/mall/systemManagement/basicConfigurationCommon/systemsSetter.vue'
import iconSetter from '@/views/mall/systemManagement/basicConfigurationCommon/iconSetter.vue'
import wartermarkSetter from '@/views/mall/systemManagement/basicConfigurationCommon/wartermarkSetter.vue'
export default {
  name: 'basicConfiguration',
  components: {
    underlyingConfiguration,
    sensitiveWords,
    logisticsAndPrinting,
    pcsetter,
    appsetter,
    wechatminiprogram,
    protocolconfiguration,
    systemsSetter,
    iconSetter,
    wartermarkSetter
  },
  data() {
    return {
      tabPosition: '1',
      language: '',
      radio1: this.$t('systemManagement.jcpz'),
      ruleForm: {
        is_Registered: 1,
        companyLogo: '',
        headImg: '',
        h_Address: '',
        front_message: '',
        login_validity: '',
        service: '',
        tx_key: '',
        is_unipush: 1, // unipush推送
        Appkey: '',
        Appid: '',
        MasterECRET: '',
        is_courier: 1, // 是否开启快递100
        api_address: '',
        customer: '',
        express_secret: "",// 电子面单
        express_tempId: "",

        authorization: '',
        same_account: 1,
        watermarkName: '',
        watermarkUrl: '',

        pcMchPath: '', // pc店铺地址
        // 搜索配置
        isOpen: 1,
        limitNum: '',
        keyword: '',
        sheng: '',
        shi: '',
        xian: '',
        address: '',
        printName: '',
        printUrl: '',
        phone: '',

        //分账设置
        isAccounts: 1,
        accountsSet: '',
      },
      //省市级联集
      shengList: [],
      shiList: [],
      xianList: [],
      rules: {
        printName: [
          {
            required: true,
            message: this.$t('systemManagement.dymcts'),
            trigger: 'blur'
          }
        ],
        printUrl: [
          {
            required: true,
            message: this.$t('systemManagement.dywzts'),
            trigger: 'blur'
          }
        ],
        sheng: [
          {
            required: true,
            message: this.$t('aftersaleAddress.add.qxzs'),
            trigger: 'change'
          }
        ],
        shi: [
          {
            required: true,
            message: this.$t('aftersaleAddress.add.qxzss'),
            trigger: 'change'
          }
        ],
        xian: [
          {
            required: true,
            message: this.$t('aftersaleAddress.add.qxzx'),
            trigger: 'change'
          }
        ],
        express_secret: [
          {
            required: true,
            message: this.$t('systemManagement.qsrsecret'),
            trigger: 'blur'
          }
        ],
        address: [
          {
            required: true,
            message: this.$t('systemManagement.dydzts'),
            trigger: 'blur'
          }
        ],
        phone: [
          {
            required: true,
            message: this.$t('systemManagement.dydhts'),
            trigger: 'blur'
          }
        ],
        companyLogo: [
          {
            required: true,
            message: this.$t('systemManagement.qscgslo'),
            trigger: 'change'
          }
        ],
        headImg: [
          {
            required: true,
            message: this.$t('systemManagement.qscwxtx'),
            trigger: 'change'
          }
        ],
        h_Address: [
          {
            required: true,
            message: this.$t('systemManagement.qtxh5'),
            trigger: 'blur'
          }
        ],
        tx_key: [
          {
            required: true,
            message: this.$t('systemManagement.qtxkfz'),
            trigger: 'blur'
          }
        ],
        watermarkName: [
          {
            required: true,
            message: this.$t('systemManagement.qtxmc'),
            trigger: 'blur'
          }
        ],
        watermarkUrl: [
          {
            required: true,
            message: this.$t('systemManagement.qtxwz'),
            trigger: 'blur'
          }
        ],
        limitNum: [
          {
            required: true,
            message: this.$t('searchConfig.qsrgjcsx'),
            trigger: 'blur'
          }
        ],
        keyword: [
          {
            required: true,
            message: this.$t('searchConfig.qsrgjc'),
            trigger: 'blur'
          }
        ],
        accountsSet: [
          {
            required: true,
            message: this.$t('systemManagement.qsrdycfzzh'),
            trigger: 'blur'
          }
        ],
      },

      isRegisteredList: [
        {
          value: 2,
          name: this.$t('systemManagement.zc')
        },
        {
          value: 0,
          name: this.$t('systemManagement.mzc')
        }
      ],
      goodsEditorBase: ''
    }
  },

  created() {
    this.language = this.getCookit()

    this.getBase()
    this.getSystemIndexs()
    this.getSheng()

    // 判断是否携带参数
    if (this.$route.query.tabPosition) {
      this.tabPosition = this.$route.query.tabPosition
    } else {
      this.tabPosition = '10'
    }
  },

  methods: {
    accChange() {
      this.ruleForm.accountsSet = ''
    },
    // 获取cookiet
    getCookit() {
      let myCookie = document.cookie.split(';').map(item => {
        let arr = item.split('=')
        return { name: arr[0], value: arr[1] }
      })
      let strCookit = ''
      myCookie.forEach(item => {
        if (item.name.indexOf('language') !== -1) {
          strCookit = item.value
        }
      })
      return strCookit
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
        this.ruleForm.shi = ''
        this.ruleForm.xian = ''
      }
    },
    // 获取县级
    async getXian(sid, flag) {
      const res = await cascade.getXian(sid)
      this.xianList = res.data.data
      if (!flag) {
        this.ruleForm.xian = ''
      }
    },
    //省市级联回显
    async cascadeAddress() {
      //省市级联
      for (const sheng of this.shengList) {
        if (sheng.districtName === this.ruleForm.sheng) {
          await this.getShi(sheng.id, true)
          for (const shi of this.shiList) {
            if (shi.districtName === this.ruleForm.shi) {
              await this.getXian(shi.id, true)
              break
            }
          }
          break
        }
      }
    },
    tabJump() {
      this.$router.push('/mall/agreement/protocolConfiguration')
      setStorage('menuId', 600)
    },
    getBase() {
      this.goodsEditorBase = Config.baseUrl
    },
    async getSystemIndexs() {
      let { entries } = Object
      let data = {
        api: 'admin.system.getSystemIndex'
      }
      let formData = new FormData()
      for (let [key, value] of entries(data)) {
        formData.append(key, value)
      }
      const res = await getSystemIndex(formData)
      let systemInfo = res.data.data.data
      let printSetupConfig = res.data.data.printSetupConfig
        ; (this.ruleForm.is_Registered = parseInt(systemInfo.is_register)),
          (this.ruleForm.companyLogo = systemInfo.logo),
          (this.ruleForm.headImg = systemInfo.wx_headimgurl),
          (this.ruleForm.h_Address = systemInfo.h5_domain),
          (this.ruleForm.front_message = systemInfo.message_day),
          (this.ruleForm.login_validity = systemInfo.exp_time),
          (this.ruleForm.service = systemInfo.customer_service),
          // (this.ruleForm.tx_key = systemInfo.tencent_key), // 腾讯位置配置
          // this.ruleForm.is_unipush = systemInfo.is_push, // unipush推送
          (this.ruleForm.Appkey = systemInfo.push_Appkey),
          (this.ruleForm.Appid = systemInfo.push_Appid),
          (this.ruleForm.MasterECRET = systemInfo.push_MasterECRET),
          // this.ruleForm.is_courier = systemInfo.is_express, // 是否开启快递100
          (this.ruleForm.api_address = systemInfo.express_address),
          (this.ruleForm.customer = systemInfo.express_number),
          (this.ruleForm.authorization = systemInfo.express_key),
          (this.ruleForm.same_account = systemInfo.is_Kicking)
      this.ruleForm.express_secret = systemInfo.express_secret ? systemInfo.express_secret : ""
      this.ruleForm.express_tempId = systemInfo.express_tempId ? systemInfo.express_tempId : ""
      // 打印设置
      this.ruleForm.printName = printSetupConfig.printName
      this.ruleForm.printUrl = printSetupConfig.printUrl
      this.ruleForm.phone = printSetupConfig.phone
      this.ruleForm.address = printSetupConfig.address
      this.ruleForm.sheng = printSetupConfig.sheng ? printSetupConfig.sheng : ''
      this.ruleForm.shi = printSetupConfig.shi ? printSetupConfig.shi : ''
      this.ruleForm.xian = printSetupConfig.xian ? printSetupConfig.xian : ''

      // change事件回显数据的时候，为null和undefind会触发一次校验
      //   for (let [key, value] of entries(this.ruleForm)) {
      //     if (value == null  || value == undefined) {
      //       this.ruleForm[key] = ""
      //     } else {
      //       this.ruleForm[key] = value
      //     }
      // }
      await this.cascadeAddress()
      this.ruleForm.isOpen = res.data.data.hotKeywordsConfig.is_open
      this.ruleForm.watermarkName = systemInfo.watermark_name
      this.ruleForm.watermarkUrl = systemInfo.watermark_url
      this.ruleForm.pcMchPath = systemInfo.pc_mch_path

      //分账设置
      this.ruleForm.isAccounts = systemInfo.isAccounts
      this.ruleForm.accountsSet = systemInfo.accountsSet

        ; (this.ruleForm.limitNum = res.data.data.hotKeywordsConfig.num),
          (this.ruleForm.keyword = res.data.data.hotKeywordsConfig.keyword),
          console.log(res)
    },

    submitForm(ruleForm) {
      // addSystemConfig({
      //     api: 'admin.system.addSystemConfig',
      //     isRegister: this.ruleForm.is_Registered,
      //     logoUrl: this.ruleForm.companyLogo,
      //     wxHeader: this.ruleForm.headImg,
      //     pageDomain: this.ruleForm.h_Address,
      //     messageSaveDay: this.ruleForm.front_message,
      //     appLoginValid: this.ruleForm.login_validity,
      //     serverClient: this.ruleForm.service,
      //     tencentKey: this.ruleForm.tx_key,
      //     unipush: this.ruleForm.is_unipush,
      //     pushAppkey: this.ruleForm.Appkey,
      //     pushAppid: this.ruleForm.Appid,
      //     pushMasterEcret: this.ruleForm.MasterECRET,
      //     isExpress: this.ruleForm.is_courier,
      //     expressAddress: this.ruleForm.api_address,
      //     expressNumber: this.ruleForm.customer,
      //     expressKey: this.ruleForm.authorization,
      //     isKicking: this.ruleForm.same_account
      // }).then(res => {
      //     if(res.data.code == '200') {
      //       console.log(res);
      //       this.$message({
      //         type: 'success',
      //         message: '成功!',
      //         offset: 102
      //       });
      //     }
      // })

      this.$refs[ruleForm].validate(async valid => {
        if (valid) {
          try {
            saveAgreement({
              api: 'admin.system.addSystemConfig',
              isRegister: this.ruleForm.is_Registered,
              logoUrl: this.ruleForm.companyLogo,
              wxHeader: this.ruleForm.headImg,
              pageDomain: this.ruleForm.h_Address,
              messageSaveDay: this.ruleForm.front_message,
              appLoginValid: this.ruleForm.login_validity,
              serverClient: this.ruleForm.service,
              // tencentKey: this.ruleForm.tx_key,// 腾讯位置配置
              // unipush: this.ruleForm.is_unipush,
              pushAppkey: this.ruleForm.Appkey,
              pushAppid: this.ruleForm.Appid,
              pushMasterEcret: this.ruleForm.MasterECRET,
              isExpress: 1,
              expressAddress: this.ruleForm.api_address,
              expressNumber: this.ruleForm.customer,
              expressKey: this.ruleForm.authorization,
              isKicking: this.ruleForm.same_account,
              watermarkName: this.ruleForm.watermarkName,
              watermarkUrl: this.ruleForm.watermarkUrl,
              pcMchPath: this.ruleForm.pcMchPath,
              isOpen: this.ruleForm.isOpen,
              limitNum: this.ruleForm.limitNum,
              keyword: this.ruleForm.keyword,
              sheng: this.ruleForm.sheng,
              shi: this.ruleForm.shi,
              xian: this.ruleForm.xian,
              address: this.ruleForm.address,
              phone: this.ruleForm.phone,
              printName: this.ruleForm.printName,
              printUrl: this.ruleForm.printUrl,
              express_secret: this.ruleForm.express_secret,
              express_tempId: this.ruleForm.express_tempId,
              isAccounts: this.ruleForm.isAccounts,
              accountsSet: this.ruleForm.accountsSet,

            }).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('zdata.baccg'),
                  type: 'success',
                  offset: 102
                })
                this.getSystemIndexs()
              }
            })
          } catch (err) {
            this.$message({
              message: err,
              type: 'error',
              offset: 102
            })
          }
        }
      })
    },
    oninput2(num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '')
      str = str.replace('.', '')
      if (Number(str) == 0) {
        return 1
      }
      return str
    },
    oninput3(num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '')
      str = str.replace('.', '')
      // if (Number(str) == 0) {
      //   return 1
      // }
      return str
    }
  }
}
