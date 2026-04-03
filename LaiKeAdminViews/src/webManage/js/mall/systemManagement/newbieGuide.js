import {
  getSystemIndex,
  updateBeginnerGuide,
  saveAgreement
} from '@/api/mall/systemManagement'
import { VueEditor } from 'vue2-editor'
import Config from '@/packages/apis/Config'
import axios from 'axios'
import { getStorage, setStorage } from '@/utils/storage'
import $ from 'jquery'

export default {
  name: 'newbieGuide',

  components: {
    VueEditor
  },
  data () {
    return {
      radio1: this.$t('systemManagement.xszn'),
      actionUrl: Config.baseUrl,
      ruleForm: {
        shopping_process: '',
        payment_method: ''
      },
      rules: {
        shopping_process: [
          {
            required: true,
            message: this.$t('systemManagement.gwlcwk'),
            trigger: 'blur'
          }
        ],
        payment_method: [
          {
            required: true,
            message: this.$t('systemManagement.zffswk'),
            trigger: 'blur'
          }
        ]
      }
    }
  },

  created () {
    this.getBase()
    this.getSystemIndexs()
  },

  methods: {
    getBase () {
      this.goodsEditorBase = Config.baseUrl
    },
    tabJump () {
      this.$router.push('/mall/agreement/protocolConfiguration')
      setStorage('menuId', 600)
    },
    async getSystemIndexs () {
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
      this.ruleForm.shopping_process = systemInfo.shopping_process
      this.ruleForm.payment_method = systemInfo.payment_method
      console.log(res)
    },

    handleImageAdded (file, Editor, cursorLocation, resetUploader) {
      var formData = new FormData()
      formData.append('file', file) //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl, //上传路径
        method: 'POST',
        params: {
          api: 'resources.file.uploadFiles',
          storeId: getStorage('laike_admin_userInfo').storeId,
          groupId: -1,
          uploadType: 2,
          accessId: this.$store.getters.token
        },
        data: formData
      })
        .then(result => {
          let url = result.data.data.imgUrls[0] // 返回给你的图片路径
          Editor.insertEmbed(cursorLocation, 'image', url)
          // Editor.setSelection(length + 1);
          resetUploader()
        })
        .catch(err => {
          console.log(err)
        })
    },

    submitForm () {
      saveAgreement({
        api: 'admin.system.updateBeginnerGuide',
        shoppingProcess: this.ruleForm.shopping_process,
        payType: this.ruleForm.payment_method
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
    }
  }
}
