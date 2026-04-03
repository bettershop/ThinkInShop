import { groupSetNow, getSetInfo } from '@/api/plug_ins/group'
import Config from '@/packages/apis/Config'
import { VueEditor } from 'vue2-editor'
import { getStorage } from '@/utils/storage'
import axios from 'axios'
export default {
  name: 'groupSet',
  components: {
    VueEditor
  },
  data() {
    return {
      radio1: '拼团设置',
      ruleForm: {
        issuer: 0,
        endTime: '',
        openLimit: 1,
        joinLimit: 1,
        autoTheGoods: '',
        afterSwitch: 0,
        orderAfter: '',
        goodSwitch: 0,
        autoGoodCommentDay: '',
        autoCommentContent: '',
        content: ''
        // rule: '' //规则
      },
      rules: {
        autoTheGoods: [
          { required: true, message: '请输入自动收货时间', trigger: 'blur' }
        ],
        openLimit: [
          { required: true, message: '请输入开团限制', trigger: 'change' }
        ],
        joinLimit: [
          { required: true, message: '请输入参团限制', trigger: 'blur' }
        ]
      },
      actionUrl: Config.baseUrl
    }
  },

  created() {
    this.getInfo()
  },

  methods: {
    getInfo() {
      let data = {
        api: 'plugin.group.admin.getStoreMchConfig'
      }
      getSetInfo(data).then(res => {
        console.log('data', res.data.data)

        let arr = res.data.data.config
        let list = res.data.data
        console.log('arr', arr)

        if (arr.endTime == 0) {
          this.ruleForm.issuer = 0
          this.ruleForm.endTime = ''
        } else {
          this.ruleForm.issuer = 1
          this.ruleForm.endTime = arr.endTime / 60 / 60
        }
        this.ruleForm.openLimit = arr.openLimit ?? 1
        this.ruleForm.joinLimit = arr.joinLimit ?? 1
        this.ruleForm.autoTheGoods = list.autoTeGood
        console.log('autoTheGoods', this.ruleForm.autoTheGoods)

        this.ruleForm.orderAfter = list.orderAfter
        if (this.ruleForm.orderAfter == 15) {
          this.ruleForm.afterSwitch = 0
        } else {
          this.ruleForm.afterSwitch = 1
        }
        this.ruleForm.autoGoodCommentDay = list.commentDay
        if (this.ruleForm.autoGoodCommentDay == 0) {
          this.ruleForm.goodSwitch = 0
        } else {
          this.ruleForm.goodSwitch = 1
        }
        this.ruleForm.autoCommentContent = list.autoCommentContent
        this.ruleForm.content = list.ruleContent
      })
    },
    afterChange(row) {
      if (row == 0) {
        this.ruleForm.orderAfter = 15
      }
    },
    goodChange(row) {
      if (row == 0) {
        this.ruleForm.autoGoodCommentDay = 0
        this.ruleForm.autoCommentContent = ''
      }
    },
    handleImageAdded(file, Editor, cursorLocation, resetUploader) {
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
          console.log(result)
          let url = result.data.data.imgUrls[0] // 返回给你的图片路径
          Editor.insertEmbed(cursorLocation, 'image', url)
          // Editor.setSelection(length)
          resetUploader()
        })
        .catch(err => {
          console.log(err)
        })
    },
    submitForm(formName) {
      if (this.ruleForm.issuer == 0) {
        this.ruleForm.endTime = 0
        var endTime = 0
      } else {
        var endTime = this.ruleForm.endTime * 60 * 60
      }
      this.$refs[formName].validate(async valid => {
        if (valid) {
          try {
            if (this.ruleForm.afterSwitch == 1) {
              if (this.ruleForm.orderAfter == '') {
                this.warnMsg('订单售后时间不能为空')
                return
              }
            }
            if (this.ruleForm.goodSwitch == 1) {
              if (
                this.ruleForm.autoGoodCommentDay == '' ||
                this.ruleForm.autoCommentContent == ''
              ) {
                this.warnMsg('开启好评设置不能为空')
                return
              }
            }
            let data = {
              api: 'plugin.group.admin.setStoreMchConfig',
              endTime: endTime,
              openLimit: this.ruleForm.openLimit,
              joinLimit: this.ruleForm.joinLimit,
              autoTheGoods: this.ruleForm.autoTheGoods,
              orderAfter: this.ruleForm.orderAfter,
              autoGoodCommentDay: this.ruleForm.autoGoodCommentDay,
              autoCommentContent: this.ruleForm.autoCommentContent,
              content: this.ruleForm.content
            }
            let { entries } = Object
            let formData = new FormData()
            for (let [key, value] of entries(data)) {
              formData.append(key, value)
            }
            groupSetNow(formData).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  type: 'success',
                  message: '设置成功!',
                  offset: 100
                })
              }
              this.getInfo()
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              offset: 100
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
