import { getStoreConfigInfo, setStoreConfigInfo } from '@/api/plug_ins/stores'
import { VueEditor } from 'vue2-editor'
import OSS from 'ali-oss'
import Config from "@/packages/apis/Config"
import axios from 'axios'
import { getStorage, setStorage } from '@/utils/storage'

export default {
  name: 'storeSet',

  components: {
    VueEditor
  },

  data() {
    return {
      radio1:this.$t('stores.dpsz'),

      actionUrl: Config.baseUrl,

      ruleForm: {
        switchs: '',
        defaultLogo: '',
        headImg:'',
        posterImg:'',
        delete_day: '',
        goodsUpload: [],
        min_pric: '',
        max_price: '',
        poundage: '',
        isPromiseSwitch:'',
        bondmoney:'',
        autoExamine:'',
      },

      goodsUploadList: [
        {
          id: 1,
          name: this.$t('stores.storeSet.scsp')
        },
        {
          id: 2,
          name: this.$t('stores.storeSet.zxsp')
        },
      ],

      setInfo: {},

      // 富文本编辑器数据
      content: '' ,
      remark:'',
    }
  },

  created() {
    this.getBase()
    this.getStoreConfigInfos()
  },

  methods: {
    toset(){
      if(getStorage('laike_admin_userInfo').mchId !== 0) {
        this.$router.push('/plug_ins/stores/storeSet')
      } else {
        this.$message({
          type: 'error',
          message: '请添加店铺!',
          offset: 102
        })
        this.$router.push('/mall/fastBoot/index')
      }
    },
    getBase() {
      this.goodsEditorBase = Config.baseUrl
    },
    async getStoreConfigInfos() {
      const res = await getStoreConfigInfo({
        api: 'mch.Admin.Mch.GetStoreConfigInfo'
      })
      // if(res.data.code!=200){
      //   this.$message({
      //     type: 'error',
      //     message: "请添加店铺!",
      //     offset: 102
      //   })
      //   this.$router.push('/mall/fastBoot/index')
      // }
      let setInfo = res.data.data.data
      this.ruleForm.switchs = setInfo.is_display
      this.ruleForm.isPromiseSwitch = setInfo.promise_switch
      this.ruleForm.bondmoney = setInfo.promise_amt.toFixed(2)
      this.remark = setInfo.promise_text
      this.ruleForm.defaultLogo = setInfo.logo
      this.ruleForm.headImg = setInfo.head_img
      this.ruleForm.posterImg = setInfo.poster_img
      this.ruleForm.autoExamine = setInfo.auto_examine
      this.ruleForm.delete_day = setInfo.delete_settings
      if(setInfo.commodity_setup && setInfo.commodity_setup != 'NaN') {
        this.ruleForm.goodsUpload = setInfo.commodity_setup.split(',').map(item => {
          return parseInt(item)
        })
      }
      this.ruleForm.min_pric = setInfo.min_charge,
      this.ruleForm.max_price = setInfo.max_charge,
      this.ruleForm.poundage = setInfo.service_charge
      this.content = setInfo.illustrate
    },

    handleImageAdded(file, Editor, cursorLocation, resetUploader) {
      var formData = new FormData();
      formData.append("file", file) //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl,//上传路径
        method: "POST",
        params: {
          api: 'resources.file.uploadFiles',
          storeId: getStorage('laike_admin_userInfo').storeId,
          groupId: -1,
          uploadType: 2,
          accessId: this.$store.getters.token
        },
        data: formData
      }).then(result => {
        let url = result.data.data.imgUrls[0]; // 返回给你的图片路径
        Editor.insertEmbed(cursorLocation, "image", url);
        // Editor.setSelection(length + 1)
        resetUploader();
      })
      .catch(err => {
          console.log(err);
      });
    },

    submitForm() {
      console.log(this.ruleForm.bondmoney)
      if(this.ruleForm.poundage == 0) {
        this.$message({
          type: 'error',
          message: this.$t('stores.storeSet.ssfbx'),
          offset: 102
        })
        return
      }
      // if(this.ruleForm.autoExamine == '') {
      //   this.$message({
      //     type: 'error',
      //     message: '入驻审核时间不能为空',
      //     offset: 102
      //   })
      //   return
      // }
      let { entries } = Object
      let data = {
        api: 'mch.Admin.Mch.SetStoreConfigInfo',
        isOpenPlugin: this.ruleForm.switchs,
        logiUrl: this.ruleForm.defaultLogo,
        headImg: this.ruleForm.headImg,
        posterImg: this.ruleForm.posterImg,
        outDayDel: this.ruleForm.delete_day,
        uploadType: this.ruleForm.goodsUpload.join(','),
        minWithdrawalMoney: this.ruleForm.min_pric,
        maxWithdrawalMoney: this.ruleForm.max_price,
        serviceCharge: this.ruleForm.poundage,
        illustrate: this.content,
        promiseSwitch:this.ruleForm.isPromiseSwitch,
        promiseAmt:this.ruleForm.bondmoney,
        promiseText:this.remark,
        autoExamine:this.ruleForm.autoExamine,
      }

      let formData = new FormData()
      for (let [key, value] of entries(data)) {
        formData.append(key, value)
      }

      setStoreConfigInfo(formData).then(res => {
        if(res.data.code == '200') {
          this.getStoreConfigInfos()
          this.$message({
            type: 'success',
            message: this.$t('stores.cg'),
            offset: 102
          })
        }
      })

      // setStoreConfigInfo({
      //   api: 'mch.Admin.Mch.SetStoreConfigInfo',
      //   isOpenPlugin: this.ruleForm.switchs,
      //   logiUrl: this.ruleForm.defaultLogo,
      //   outDayDel: this.ruleForm.delete_day,
      //   uploadType: this.ruleForm.goodsUpload.join(','),
      //   minWithdrawalMoney: this.ruleForm.min_pric,
      //   maxWithdrawalMoney: this.ruleForm.max_price,
      //   serviceCharge: this.ruleForm.poundage,
      //   illustrate: this.content,
      //   promiseSwitch:this.ruleForm.isPromiseSwitch,
      //   promiseAmt:this.ruleForm.bondmoney,
      //   promiseText:this.remark,
      // }).then(res => {
      //   if(res.data.code == '200') {
      //     this.getStoreConfigInfos()
      //     this.$message({
      //       type: 'success',
      //       message: '成功!',
      //       offset: 102
      //     })
      //   }
      //   console.log(res);
      // })
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

      if(this.ruleForm.price_type == 1 && this.totlePrice !== 0 && parseInt(this.ruleForm.price) > this.totlePrice) {
          str = this.totlePrice
      }
      return str
    }
  }
}
