import { paymentParmaInfo, setPaymentParma } from '@/api/mall/payManagement'
import Config from "@/packages/apis/Config";
import { getStorage } from '@/utils/storage'
export default {
  name: 'parameterModify',

  data() {
    return {
      isShow: false,
      //贝宝支付
      payPalForm: {
        status: '',
        pc_mall_domain: '',
        client_id: '',
        client_secret: '',
        brand_name: ''
      },
      stripeForm:{
        status: '',
        publishable_key:'',
        secret_key:'',
        pc_mall_domain: ''
      },
      payPalRules: {
        pc_mall_domain: [
          { required: true, message: this.$t('payManagement.qsrbbhdlj'), trigger: 'blur' },
          // 可以添加更多验证规则，例如 URL 格式验证
          { pattern: /^(http|https):\/\/[^ "]+$/, message: this.$t('payManagement.notifyUrlInvalid'), trigger: 'blur' }
        ],
        client_id: [
          { required: true, message: this.$t('payManagement.qsrbbkhdid'), trigger: 'blur' }
        ],
        client_secret: [
          { required: true, message: this.$t('payManagement.qsrbbmy'), trigger: 'blur' }
        ],
        brand_name: [
          { required: true, message: this.$t('payManagement.qsrppmc'), trigger: 'blur' }
        ],
        publishable_key: [
          { required: true, message: this.$t('payManagement.qsrstripeKey'), trigger: 'blur' }
        ],
        secret_key: [
          { required: true, message: this.$t('payManagement.qsrstripePrivateKey'), trigger: 'blur' }
        ],
      },
        //贝宝结束

      // 微信支付
      ruleForm1: {
        status: '',
        appid: '',
        appsecret: '',
        notify_url: '',
        mch_id: '',
        mch_key: '',
        cert_pem: '',
        key_pem: '',
        cert_p12: '',
        serial_no: '',
        APIv3_key:'',
        pub_pem: '',
        pub_key_id: ''
      },
      cert_p12_list: [],
      cert_p12_list1: [],
      actionUrl: Config.baseUrl,
      rules1: {
        appid: [
          { required: true, message: this.$t('payManagement.qsrapp'), trigger: 'blur' }
        ],
        appsecret: [
          { required: true, message: this.$t('payManagement.qsrapps'), trigger: 'blur' }
        ],
        notify_url: [
          { required: true, message: this.$t('payManagement.qsthdlj'), trigger: 'blur' }
        ],
        mch_id: [
          { required: true, message: this.$t('payManagement.qsrwxshh'), trigger: 'blur' }
        ],
        mch_key: [
          { required: true, message: this.$t('payManagement.qsrwxmy'), trigger: 'blur' }
        ],
        cert_pem: [
          { required: true, message: this.$t('payManagement.qsrwxapi'), trigger: 'blur' }
        ],
        key_pem: [
          { required: true, message: this.$t('payManagement.qsrcdmc'), trigger: 'blur' }
        ],
      },

      // 支付宝支付
      ruleForm2: {
        status: '',
        appid: '',
        notify_url: '',
        signType: 'RSA',
        encryptKey: '',
        rsaPrivateKey: '',
        alipayrsaPublicKey: '',
      },
      rules2: {
        appid: [
          { required: true, message: this.$t('payManagement.qsrzfbid'), trigger: 'blur' }
        ],
        notify_url: [
          { required: true, message: this.$t('payManagement.qsrzfbhdlj'), trigger: 'blur' }
        ],
        signType: [
          { required: true, message: this.$t('payManagement.qxzqmlx'), trigger: 'blur' }
        ],
        encryptKey: [
          { required: true, message: this.$t('payManagement.qsrjmmy'), trigger: 'blur' }
        ],
        rsaPrivateKey: [
          { required: true, message: this.$t('payManagement.qsrkfzsy'), trigger: 'blur' }
        ],
        alipayrsaPublicKey: [
          { required: true, message: this.$t('payManagement.zfbgy'), trigger: 'blur' }
        ],
      },

      // 头条支付
      ruleForm3: {
        status: '',
        ttAppid: '',
        ttAppSecret: '',
        notify_url: '',
        ttshid: '',
        ttpayappid: '',
        ttpaysecret: '',
      },
      rules3: {
        ttAppid: [
          { required: true, message: this.$t('payManagement.qsrttid'), trigger: 'blur' }
        ],
        ttAppSecret: [
          { required: true, message: this.$t('payManagement.qsrttmy'), trigger: 'blur' }
        ],
        notify_url: [
          { required: true, message: this.$t('payManagement.qsthdlj'), trigger: 'blur' }
        ],
        ttshid: [
          { required: true, message: this.$t('payManagement.qsrttzfid'), trigger: 'blur' }
        ],
        ttpayappid: [
          { required: true, message: this.$t('payManagement.qsrttapi'), trigger: 'blur' }
        ],
        ttpaysecret: [
          { required: true, message: this.$t('payManagement.qsrttmy'), trigger: 'blur' }
        ],
      },

      // 百度支付
      ruleForm4: {
        status: '',
        bdmpappid: '',
        bdmpappsk: '',
        appid: '',
        appkey: '',
        dealId: 'RSA',
        rsaPrivateKey: '',
        rsaPublicKey: '',
      },
      rules4: {
        bdmpappid: [
          { required: true, message: this.$t('payManagement.qsrbdk'), trigger: 'blur' }
        ],
        bdmpappsk: [
          { required: true, message: this.$t('payManagement.qsrbdas'), trigger: 'blur' }
        ],
        appid: [
          { required: true, message: this.$t('payManagement.qsrzfid'), trigger: 'blur' }
        ],
        appkey: [
          { required: true, message: this.$t('payManagement.qsrzfk'), trigger: 'blur' }
        ],
        dealId: [
          { required: true, message: this.$t('payManagement.qsrd'), trigger: 'blur' }
        ],
        rsaPrivateKey: [
          { required: true, message: this.$t('payManagement.bdbz'), trigger: 'blur' }
        ],
        rsaPublicKey: [
          { required: true, message: this.$t('payManagement.qsrptgy'), trigger: 'blur' }
        ],
      },

      // 钱包
      ruleForm5: {
        status: '',
      },

      // 微信提现零钱
      ruleForm6: {
        status: '',
        appid: '',
        appsecret: '',
        mch_id: '',
        mch_key: '',
        serial_no: '',
        key_pem: '',
      },
      rules6: {
        appid: [
          { required: true, message: this.$t('payManagement.qsrapp'), trigger: 'blur' }
        ],
        appsecret: [
          { required: true, message: this.$t('payManagement.qsrapps'), trigger: 'blur' }
        ],
        mch_id: [
          { required: true, message: this.$t('payManagement.qsrwxzfptzsxlh'), trigger: 'blur' }
        ],
        serial_no: [
          { required: true, message: this.$t('payManagement.qsrwxshh'), trigger: 'blur' }
        ],
        mch_key: [
          { required: true, message: this.$t('payManagement.qsrwxmy'), trigger: 'blur' }
        ],
        APIv3_key: [
          { required: true, message: this.$t('payManagement.qsrvApi3Key'), trigger: 'blur' }
        ],
        key_pem: [
          { required: true, message: this.$t('payManagement.qsrcdmc'), trigger: 'blur' }
        ],
      },
    }
  },

  computed: {
    uploadData() {
      {
        return {
          api: 'admin.payment.uploadCertP12',
          storeId: getStorage('laike_admin_userInfo').storeId,
          accessId: this.$store.getters.token
        }
      }
    }
  },

  beforeRouteLeave(to, from, next) {
    if (to.name == 'payList') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }
    next();
  },

  created() {
    this.paymentParmaInfos()
    // php 环境 隐藏  微信apiclient_cert.p12 文件上传
    const environment = process.env.VUE_APP_BASE_API.toString()
    this.isShow = environment.split('http')[1].includes('tp')
  },

  methods: {
    handleAvatarSuccess(res, file) {
      console.log(res);
      this.ruleForm1.cert_p12 = res.data.savePath
    },
    handleAvatarSuccess1(res, file) {
      console.log(res);
      this.ruleForm1.pub_pem = res.data.savePath
    },
    async paymentParmaInfos() {
      let { entries } = Object
      let data = {
        api: 'admin.payment.paymentParmaInfo',
        id: this.$route.query.id
      }
      let formData = new FormData()
      for (let [key, value] of entries(data)) {
        formData.append(key, value)
      }
      const res = await paymentParmaInfo(formData)
      console.log(res);
      if (this.$route.query.id == 4 || this.$route.query.id == 5 || this.$route.query.id == 6 || this.$route.query.id == 10 || this.$route.query.id == 12) {
        this.ruleForm1.status = res.data.data.config.status
        this.ruleForm1.appid = res.data.data.config.appid ? res.data.data.config.appid : ''
        this.ruleForm1.appsecret = res.data.data.config.appsecret ? res.data.data.config.appsecret : ''
        this.ruleForm1.notify_url = res.data.data.config.notify_url ? res.data.data.config.notify_url : ''
        this.ruleForm1.mch_id = res.data.data.config.mch_id ? res.data.data.config.mch_id : ''
        this.ruleForm1.mch_key = res.data.data.config.mch_key ? decodeURIComponent(res.data.data.config.mch_key) : ''
        this.ruleForm1.cert_pem = res.data.data.config.cert_pem ? decodeURIComponent(res.data.data.config.cert_pem) : ''
        this.ruleForm1.key_pem = res.data.data.config.key_pem ? decodeURIComponent(res.data.data.config.key_pem) : ''
        this.ruleForm1.serial_no = res.data.data.config.serial_no ? decodeURIComponent(res.data.data.config.serial_no) : ''
        this.ruleForm1.APIv3_key = res.data.data.config.APIv3_key ? decodeURIComponent(res.data.data.config.APIv3_key) : ''

        const {data} = res.data

        this.ruleForm1.pub_key_id = data.config.pub_key_id ? data.config.pub_key_id : ''


        this.ruleForm1.pub_pem = data.config.pub_pem ? decodeURIComponent(data.config.pub_pem) : ''

        if (res.data.data.config.cert_p12) {
          this.ruleForm1.cert_p12 = res.data.data.config.cert_p12
          this.cert_p12_list = [{ name: 'apiclient_cert.p12', url: res.data.data.config.cert_p12 }]
        }
      } else if (this.$route.query.id == 1 || this.$route.query.id == 7 || this.$route.query.id == 11 || this.$route.query.id == 13) {
        if (this.$route.query.id !== 7) {
          this.ruleForm2.status = res.data.data.config.status ? res.data.data.config.status : ''
          this.ruleForm2.appid = res.data.data.config.appid ? res.data.data.config.appid : ''
          this.ruleForm2.notify_url = res.data.data.config.notify_url ? res.data.data.config.notify_url : ''
          this.ruleForm2.signType =  res.data.data.config && res.data.data.config.signType ? decodeURIComponent(res.data.data.config.signType) : ''
          this.ruleForm2.encryptKey =  res.data.data.config && res.data.data.config.encryptKey ? decodeURIComponent(res.data.data.config.encryptKey) : ''
          this.ruleForm2.rsaPrivateKey =  res.data.data.config && res.data.data.config.rsaPrivateKey ? decodeURIComponent(res.data.data.config.rsaPrivateKey) : ''
          this.ruleForm2.alipayrsaPublicKey = res.data.data.config && res.data.data.config.alipayrsaPublicKey ? decodeURIComponent(res.data.data.config.alipayrsaPublicKey) : ''
        } else {
          this.ruleForm2.status = res.data.data.config.status
          this.ruleForm2.appid = res.data.data.config.appid ? res.data.data.config.appid : ''
          this.ruleForm2.notify_url = res.data.data.config.notify_url ? res.data.data.config.notify_url : ''
          this.ruleForm2.signType = res.data.data.config && res.data.data.config.signType ? decodeURIComponent(res.data.data.config.signType) : ''
          this.ruleForm2.rsaPrivateKey =  res.data.data.config && res.data.data.config.rsaPrivateKey ? decodeURIComponent(res.data.data.config.rsaPrivateKey) : ''
          this.ruleForm2.alipayrsaPublicKey =  res.data.data.config && res.data.data.config.alipayrsaPublicKey ? decodeURIComponent(res.data.data.config.alipayrsaPublicKey) : ''
        }
      } else if (this.$route.query.id == 8) {
        this.ruleForm3.status = res.data.data.config.status
        this.ruleForm3.ttAppid = res.data.data.config.ttAppid ? res.data.data.config.ttAppid : ''
        this.ruleForm3.ttAppSecret = res.data.data.config.ttAppSecret ? res.data.data.config.ttAppSecret : ''
        this.ruleForm3.notify_url = res.data.data.config.notify_url ? res.data.data.config.notify_url : ''
        this.ruleForm3.ttshid = res.data.data.config.ttshid ? res.data.data.config.ttshid : ''
        this.ruleForm3.ttpayappid = res.data.data.config.ttpayappid ? res.data.data.config.ttpayappid : ''
        this.ruleForm3.ttpaysecret = res.data.data.config.ttpaysecret ? res.data.data.config.ttpaysecret : ''
      } else if (this.$route.query.id == 9) {
        this.ruleForm4.status = res.data.data.config.status
        this.ruleForm4.bdmpappid = res.data.data.config.bdmpappid ? res.data.data.config.bdmpappid : ''
        this.ruleForm4.bdmpappsk = res.data.data.config.bdmpappsk ? res.data.data.config.bdmpappsk : ''
        this.ruleForm4.appid = res.data.data.config.appid ? res.data.data.config.appid : ''
        this.ruleForm4.appkey = res.data.data.config.appkey ? res.data.data.config.appkey : ''
        this.ruleForm4.dealId = res.data.data.config.dealId ? res.data.data.config.dealId : ''
        this.ruleForm4.rsaPrivateKey = res.data.data.config.rsaPrivateKey ? res.data.data.config.rsaPrivateKey : ''
        this.ruleForm4.rsaPublicKey = res.data.data.config.rsaPublicKey ? res.data.data.config.rsaPublicKey : ''
      } else if (this.$route.query.id == 14) {
        this.ruleForm6.status = res.data.data.config.status
        this.ruleForm6.appid = res.data.data.config.appid ? res.data.data.config.appid : ''
        this.ruleForm6.appsecret = res.data.data.config.appsecret ? res.data.data.config.appsecret : ''
        this.ruleForm6.mch_id = res.data.data.config.mch_id ? res.data.data.config.mch_id : ''
        this.ruleForm6.mch_key = res.data.data.config.mch_key ? decodeURIComponent(res.data.data.config.mch_key) : ''
        this.ruleForm6.serial_no = res.data.data.config.serial_no ? decodeURIComponent(res.data.data.config.serial_no) : ''
        this.ruleForm6.APIv3_key = res.data.data.config.APIv3_key ? decodeURIComponent(res.data.data.config.APIv3_key) : ''
        this.ruleForm6.key_pem = res.data.data.config.key_pem ? decodeURIComponent(res.data.data.config.key_pem) : ''
      } else if (this.$route.query.id == 15) {  //贝宝支付
        this.status = res.data.data.config.status
          this.payPalForm.brand_name = res.data.data.config.brand_name ? res.data.data.config.brand_name : ''
          this.payPalForm.pc_mall_domain = res.data.data.config.pc_mall_domain ? res.data.data.config.pc_mall_domain : ''
          this.payPalForm.client_id = res.data.data.config.client_id ? encodeURIComponent(res.data.data.config.client_id) : ''
          this.payPalForm.client_secret = res.data.data.config.client_secret ? encodeURIComponent(res.data.data.config.client_secret) : ''
      }else if (this.$route.query.id == 16) {  //stripe支付
        this.stripeForm.status = res.data.data.config.status
          this.stripeForm.pc_mall_domain = res.data.data.config.pc_mall_domain ? res.data.data.config.pc_mall_domain : ''
          this.stripeForm.secret_key = res.data.data.config.secret_key || ''
          this.stripeForm.publishable_key = res.data.data.config.publishable_key || ''
      }else {
        this.ruleForm5.status = res.data.data.config.status
      }
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          try {
            let json = {}
            if (this.$route.query.id == 4 || this.$route.query.id == 5 || this.$route.query.id == 6 || this.$route.query.id == 10 || this.$route.query.id == 12) {

              json.status = this.ruleForm1.status
                json.appid = this.ruleForm1.appid.trim()
                json.appsecret = this.ruleForm1.appsecret.trim()
                json.notify_url = this.ruleForm1.notify_url.trim()
                json.mch_id = this.ruleForm1.mch_id.trim()
                json.serial_no = encodeURIComponent(this.ruleForm1.serial_no.trim())
                json.APIv3_key = encodeURIComponent(this.ruleForm1.APIv3_key.trim())
                json.mch_key = encodeURIComponent(this.ruleForm1.mch_key.trim())
                json.cert_pem = encodeURIComponent(this.ruleForm1.cert_pem)
                json.key_pem = encodeURIComponent(this.ruleForm1.key_pem)
                json.pub_pem =  encodeURIComponent(this.ruleForm1.pub_pem)
                json.pub_key_id =  encodeURIComponent(this.ruleForm1.pub_key_id)
                json.cert_p12 = this.ruleForm1.cert_p12
            } else if (this.$route.query.id == 1 || this.$route.query.id == 7 || this.$route.query.id == 11 || this.$route.query.id == 13) {

              if (this.$route.query.id !== 7) {
                json.status = this.ruleForm2.status
                  json.appid = this.ruleForm2.appid.trim()
                  json.notify_url = this.ruleForm2.notify_url.trim()
                  json.signType = encodeURIComponent(this.ruleForm2.signType)
                  json.encryptKey = encodeURIComponent(this.ruleForm2.encryptKey.trim())
                  json.rsaPrivateKey = encodeURIComponent(this.ruleForm2.rsaPrivateKey)
                  json.alipayrsaPublicKey = encodeURIComponent(this.ruleForm2.alipayrsaPublicKey)
              } else {
                json.status = this.ruleForm2.status,
                  json.appid = this.ruleForm2.appid.trim()
                  json.notify_url = this.ruleForm2.notify_url.trim()
                  json.signType = encodeURIComponent(this.ruleForm2.signType)
                  json.rsaPrivateKey = encodeURIComponent(this.ruleForm2.rsaPrivateKey)
                  json.alipayrsaPublicKey = encodeURIComponent(this.ruleForm2.alipayrsaPublicKey)
              }
            } else if (this.$route.query.id == 8) {
              json.status = this.ruleForm4.status
                json.ttAppid = this.ruleForm4.ttAppid
                json.ttAppSecret = this.ruleForm4.ttAppSecret
                json.notify_url = this.ruleForm4.notify_url.trim()
                json.ttshid = this.ruleForm4.ttshid
                json.ttpayappid = this.ruleForm4.ttpayappid
                json.ttpaysecret = encodeURIComponent(this.ruleForm4.ttpaysecret)
            } else if (this.$route.query.id == 9) {
              json.status = this.ruleForm4.status,
                json.bdmpappid = this.ruleForm4.bdmpappid
                json.bdmpappsk = this.ruleForm4.bdmpappsk
                json.appid = this.ruleForm4.appid.trim()
                json.appkey = encodeURIComponent(this.ruleForm4.appkey)
                json.dealId = this.ruleForm4.dealId
                json.rsaPrivateKey = encodeURIComponent(this.ruleForm4.rsaPrivateKey)
                json.rsaPublicKey = encodeURIComponent(this.ruleForm4.rsaPublicKey)
            } else if (this.$route.query.id == 14) {
              json.status = this.ruleForm6.status,
                json.appid = this.ruleForm6.appid.trim()
                json.appsecret = this.ruleForm6.appsecret.trim()
                json.mch_id = this.ruleForm6.mch_id.trim()
                json.mch_key = encodeURIComponent(this.ruleForm6.mch_key.trim())
                json.serial_no = encodeURIComponent(this.ruleForm6.serial_no)
                json.key_pem = encodeURIComponent(this.ruleForm6.key_pem)
            } else if (this.$route.query.id == 15) {  //贝宝支付
              json.status = this.payPalForm.status,
                json.brand_name = this.payPalForm.brand_name.trim()
                json.pc_mall_domain = this.payPalForm.pc_mall_domain.trim()
                json.client_id = encodeURIComponent(this.payPalForm.client_id.trim())
                json.client_secret = encodeURIComponent(this.payPalForm.client_secret.trim())
            }  else if (this.$route.query.id == 16) {  //strpie支付
                json.status = this.stripeForm.status
                json.pc_mall_domain = this.stripeForm.pc_mall_domain.trim()
                json.publishable_key =  encodeURIComponent(this.stripeForm.publishable_key.trim())
                json.secret_key =  encodeURIComponent(this.stripeForm.secret_key.trim())
            } else {
              json.status = this.ruleForm5.status
            }
            console.log(json);
            let { entries } = Object
            let mydata = {
              api: 'admin.payment.setPaymentParma',
              id: this.$route.query.id,
              json: json,
              status: json.status
            }
            let myformData = new FormData()
            for (let [key, value] of entries(mydata)) {
              if (key == 'json') {
                myformData.append(key, JSON.stringify(value))
              } else {
                myformData.append(key, value)
              }
            }
            console.log('XXXXXXXXXXXXXXX', myformData)
            setPaymentParma(myformData).then(res => {
              console.log(res);
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('zdata.xgcg'),
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
