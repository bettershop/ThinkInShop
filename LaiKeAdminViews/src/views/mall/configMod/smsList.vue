<template>
  <div class="container">
    <!-- <el-main> -->
      <!-- tbl页 -->

      <div class="hr"></div>
      <!-- 短信列表 end -->
      <div class="core-set"  >
        <el-form :model="mainForm" :rules="rules" ref="ruleForm" class="picture-ruleForm" label-width="150px">
          <el-form-item :label="$t('configMod.yjfwqdz')" prop="host">
            <el-input v-model="mainForm.host" :placeholder="$t('fastboot.SMTP')"></el-input>
          </el-form-item>
          <el-form-item :label="$t('configMod.fjryxzh')" prop="username">
            <el-input v-model="mainForm.username" :placeholder="$t('fastboot.yxzh')"></el-input>
          </el-form-item>

          <el-form-item :label="$t('configMod.yxsqm')" prop="password">
            <el-input v-model="mainForm.password" :placeholder="$t('fastboot.yxsqm')" show-password></el-input>
          </el-form-item>
          <el-form-item :label="$t('configMod.sfqySSL')" >
            <el-switch v-model="mainForm.sslEnable"></el-switch>
          </el-form-item>

          <div class="form-footer">
            <el-form-item>
              <el-button class="bgColor" type="primary" @click="saveConfig('ruleForm')">{{ $t('DemoPage.tableFromPage.save')
                }}
              </el-button>
              <!-- <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}
              </el-button> -->
            </el-form-item>
          </div>
          <!-- <div class="footer-button">
            <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">取消</el-button>
            <el-button type="primary" class="footer-save bgColor mgleft" @click="saveConfig('ruleForm')">保存</el-button>
          </div> -->
        </el-form>
      </div>
    <!-- </el-main> -->
  </div>
</template>

<script>
import LaiKeCommon from '@/api/common.js'
export default {
  data() {
    return {
      id:'',
      mainForm: {
        host: '',
        username: '',
        password: '',
        sslEnable: true
      },
      rules:{
        host: [
          {
            required: true,
            message: this.$t('configMod.qsryjfwqdz'),
            trigger: 'blur'
          }
        ],
        username: [
          {
            required: true,
            message: this.$t('configMod.qsrfjryxzh'),
            trigger: 'blur'
          }
        ],
        password: [
          {
            required: true,
            message: this.$t('configMod.qsryxsqm'),
            trigger: 'blur'
          }
        ],

      }
    };
  },

    //组装模板
  created() {
    this.loadData();
  },
  mounted() {

  },
  methods:{
    async loadData() {
      const {data:res} = await LaiKeCommon.select({
          api: 'admin.system.getEmailConfig',
          id: this.$route.query.id,
      })
      if(res && res.code == '200'){
        const {data} =res
        this.id = data.id
         if(data.mail_config){
          this.mainForm = JSON.parse(data.mail_config)
         }
      }
    },
    saveConfig(formName){
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          LaiKeCommon.edit({
            api: 'admin.system.addOrUpdateEmailConfig',
            id: this.id,
            mail_config: JSON.stringify(this.mainForm),
          }).then(({data:res}) =>{
          if(res.code == '200'){
            this.succesMsg(this.$t('zdata.xgcg'))
          }else{
            this.errorMsg(res.message)
          }
        })
        }
      })
    }
  }
}
</script>

<style scoped lang="less">
@import "../../../webManage/css/mall/sms/smsList.less";
</style>
