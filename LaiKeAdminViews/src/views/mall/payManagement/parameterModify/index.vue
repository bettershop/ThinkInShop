<template>
 <div class="container">
    <div class="parameters-change">
      <el-form v-if="this.$route.query.id == 4 || this.$route.query.id == 5 || this.$route.query.id == 6 || this.$route.query.id == 10 || this.$route.query.id == 12" :model="ruleForm1" :rules="rules1" ref="ruleForm1"  class="wx-form" label-width="210px">
        <!-- <el-form-item required :label="$t('payManagement.sfqy')" prop="status">
          <el-switch v-model="ruleForm1.status" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
          </el-switch>
        </el-form-item> -->
        <el-form-item label="AppId" prop="appid">
          <el-input v-model="ruleForm1.appid" :placeholder="$t('payManagement.qsrapp')"></el-input>
        </el-form-item>
        <el-form-item label="AppSecret" prop="appsecret" class="prompt">
          <el-input v-model="ruleForm1.appsecret" :placeholder="$t('payManagement.qsrapps')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.appbz')}}</span>
          </div>
        </el-form-item>
        <el-form-item :label="$t('payManagement.hdlj')" prop="notify_url" class="prompt">
          <el-input v-model="ruleForm1.notify_url" :placeholder="$t('payManagement.qsthdlj')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.hdljbz')}}</span>
          </div>
        </el-form-item>

        <el-form-item :label="$t('payManagement.wxzfptzsxlh')" >
          <el-input v-model="ruleForm1.serial_no" :placeholder="$t('payManagement.qsrwxzfptzsxlh')"></el-input>
        </el-form-item>

        <el-form-item label="apiV3Key" >
          <el-input v-model="ruleForm1.APIv3_key" :placeholder="$t('payManagement.qsrvApi3Key')"></el-input>
        </el-form-item>
 
      
        <el-form-item :label="$t('payManagement.wxapip1id')" >
          <el-input v-model="ruleForm1.pub_key_id" :placeholder="$t('payManagement.qsrwxapip1id')"></el-input>
        </el-form-item>


        <el-form-item :label="$t('payManagement.wxshh')" prop="mch_id">
          <el-input v-model="ruleForm1.mch_id" :placeholder="$t('payManagement.qsrwxshh')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.wxmy')" prop="mch_key">
          <el-input v-model="ruleForm1.mch_key" :placeholder="$t('payManagement.qsrwxmy')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.wxapi')" prop="cert_pem" class="prompt ">
          <el-input type="textarea" v-model="ruleForm1.cert_pem" :placeholder="$t('payManagement.qsrwxapi')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.wxzfbz')}}</span>
          </div>
        </el-form-item>
        <el-form-item :label="$t('payManagement.wxapik')" prop="key_pem" class="prompt ">
          <el-input type="textarea" v-model="ruleForm1.key_pem" :placeholder="$t('payManagement.qsrwxapik')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.wxapikbz')}}</span>
          </div>
        </el-form-item>

        <el-form-item :label="$t('payManagement.wxapip1')" prop="key_pem" class="prompt ">
          <el-input type="textarea" v-model="ruleForm1.pub_pem" :placeholder="$t('payManagement.qsrwxapik')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.qsrqsrwxapip1')}}</span>
          </div>
        </el-form-item>

        <el-form-item :label="$t('payManagement.wxapip')" prop="cert_p12" class="prompt " v-if="!isShow">
          <el-upload
            :action="actionUrl"
            :data="uploadData"
            :show-file-list="true"
            :on-success="handleAvatarSuccess"
            :multiple="false"
            :file-list="cert_p12_list"
            accept=".p12"
            :limit=1 >
            <el-button size="small" type="primary" >{{$t('payManagement.scwj')}}</el-button>
          </el-upload>
          <div class="black-prompt">
            <span>{{$t('payManagement.scwjbz')}}</span>
          </div>
        </el-form-item>
 

        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm1')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>

      <el-form v-if="this.$route.query.id == 1 || this.$route.query.id == 7 || this.$route.query.id == 11 || this.$route.query.id == 13" :model="ruleForm2" :rules="rules2" ref="ruleForm2"  class="zfb-form" label-width="210px">
        <!-- <el-form-item required :label="$t('payManagement.sfqy')" prop="status">
          <el-switch v-model="ruleForm2.status" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
          </el-switch>
        </el-form-item> -->
        <el-form-item :label="$t('payManagement.zfbid')" prop="appid">
          <el-input v-model="ruleForm2.appid" :placeholder="$t('payManagement.qsrzfbid')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.zfbhdlj')" prop="notify_url" class="prompt">
          <el-input v-model="ruleForm2.notify_url" :placeholder="$t('payManagement.qsrzfbhdlj')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.zfbhdbz')}}</span>
          </div>
        </el-form-item>
        <el-form-item :label="$t('payManagement.qmlx')" prop="signType">
          <el-radio-group v-model="ruleForm2.signType">
            <el-radio :label="'RSA'">RSA</el-radio>
            <el-radio :label="'RSA2'">RSA2</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="this.$route.query.id !== 7" :label="$t('payManagement.jmmy')" prop="encryptKey">
          <el-input v-model="ruleForm2.encryptKey" :placeholder="$t('payManagement.qsrjmmy')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.kfzsy')" prop="rsaPrivateKey" class="prompt ">
          <el-input type="textarea" v-model="ruleForm2.rsaPrivateKey" :placeholder="$t('payManagement.qsrkfzsy')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.mybz')}}</span>
          </div>
        </el-form-item>
        <el-form-item :label="$t('payManagement.zfbgy')" prop="alipayrsaPublicKey" class="prompt ">
          <el-input type="textarea" v-model="ruleForm2.alipayrsaPublicKey" :placeholder="$t('payManagement.qsrzfbgy')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.gybz')}}</span>
          </div>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm2')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>

      <el-form v-if="this.$route.query.id == 8" :model="ruleForm3" :rules="rules3" ref="ruleForm3"  class="tt-form" label-width="210px">
        <!-- <el-form-item required :label="$t('payManagement.sfqy')" prop="status">
          <el-switch v-model="ruleForm3.status" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
          </el-switch>
        </el-form-item> -->
        <el-form-item :label="$t('payManagement.ttid')" prop="ttAppid">
          <el-input v-model="ruleForm3.ttAppid" :placeholder="$t('payManagement.qsrttid')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.ttmy')" prop="ttAppSecret">
          <el-input v-model="ruleForm3.ttAppSecret" :placeholder="$t('payManagement.qsrttmy')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.zfbhdlj')" prop="notify_url" class="prompt">
          <el-input v-model="ruleForm3.notify_url" :placeholder="$t('payManagement.qsrzfbhdlj')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.zfbhdbz')}}</span>
          </div>
        </el-form-item>
        <el-form-item :label="$t('payManagement.ttzfid')" prop="ttshid">
          <el-input v-model="ruleForm3.ttshid" :placeholder="$t('payManagement.qsrttzfid')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.ttapi')" prop="ttpayappid" class="prompt ">
          <el-input v-model="ruleForm3.ttpayappid" :placeholder="$t('payManagement.qsrttapi')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.ttapibz')}}</span>
          </div>
        </el-form-item>
        <el-form-item :label="$t('payManagement.ttmy')" prop="ttpaysecret" class="prompt ">
          <el-input v-model="ruleForm3.ttpaysecret" :placeholder="$t('payManagement.qsrttmy')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.ttmybz')}}</span>
          </div>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm3')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>

      <el-form v-if="this.$route.query.id == 9" :model="ruleForm4" :rules="rules4" ref="ruleForm4" class="bd-form" label-width="210px">
        <!-- <el-form-item required :label="$t('payManagement.sfqy')" prop="status">
          <el-switch v-model="ruleForm4.status" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
          </el-switch>
        </el-form-item> -->
        <el-form-item :label="$t('payManagement.bdk')" prop="bdmpappid">
          <el-input v-model="ruleForm4.bdmpappid" :placeholder="$t('payManagement.qsrbdk')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.bdas')" prop="bdmpappsk">
          <el-input v-model="ruleForm4.bdmpappsk" :placeholder="$t('payManagement.qsrbdas')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.zfid')" prop="appid">
          <el-input v-model="ruleForm4.appid" :placeholder="$t('payManagement.qsrzfid')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.zfk')" prop="appkey">
          <el-input v-model="ruleForm4.appkey" :placeholder="$t('payManagement.qsrzfk')"></el-input>
        </el-form-item>
        <el-form-item label="dealld" prop="dealId">
          <el-input v-model="ruleForm4.dealId" :placeholder="$t('payManagement.qsrd')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.kfzsy')" prop="rsaPrivateKey" class="prompt ">
          <el-input type="textarea" v-model="ruleForm4.rsaPrivateKey" :placeholder="$t('payManagement.qsrkfzsy')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.bdbz')}}</span>
          </div>
        </el-form-item>
        <el-form-item :label="$t('payManagement.ptgy')" prop="rsaPublicKey" class="prompt ">
          <el-input type="textarea" v-model="ruleForm4.rsaPublicKey" :placeholder="$t('payManagement.qsrptgy')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.pygrbz')}}</span>
          </div>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm4')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>

      <el-form v-if="this.$route.query.id == 3" :model="ruleForm5" :rules="rules4" ref="ruleForm5" class="bd-form" label-width="210px">
        <!-- <el-form-item required :label="$t('payManagement.sfqy')" prop="status">
          <el-switch v-model="ruleForm5.status" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
          </el-switch>
        </el-form-item> -->
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm5')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>

      <el-form v-if="this.$route.query.id == 14" :model="ruleForm6" :rules="rules6" ref="ruleForm6"  class="wx-form" label-width="210px">
        <el-form-item label="AppId" prop="appid">
          <el-input v-model="ruleForm6.appid" :placeholder="$t('payManagement.qsrapp')"></el-input>
        </el-form-item>
        <el-form-item label="AppSecret" prop="appsecret" class="prompt">
          <el-input v-model="ruleForm6.appsecret" :placeholder="$t('payManagement.qsrapps')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.appbz')}}</span>
          </div>
        </el-form-item>
        <el-form-item :label="$t('payManagement.wxshh')" prop="mch_id">
          <el-input v-model="ruleForm6.mch_id" :placeholder="$t('payManagement.qsrwxshh')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.wxmy')" prop="mch_key">
          <el-input v-model="ruleForm6.mch_key" :placeholder="$t('payManagement.qsrwxmy')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('微信支付平台证书序列号')" prop="serial_no">
          <el-input v-model="ruleForm6.serial_no" :placeholder="$t('请输入微信支付平台证书序列号')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.wxapik')" prop="key_pem" class="prompt ">
          <el-input type="textarea" v-model="ruleForm6.key_pem" :placeholder="$t('payManagement.qsrwxapik')"></el-input>
          <div class="red-prompt">
            <span>{{$t('payManagement.wxapikbz')}}</span>
          </div>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm6')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>



      <!-- 贝宝支付 -->
      <el-form v-if="this.$route.query.id == 15":model="payPalForm" :rules="payPalRules" ref="payPalForm" class="paypal-form" label-width="210px">
        <el-form-item :label="$t('payManagement.paypalhdlj')" prop="pc_mall_domain">
          <el-input v-model="payPalForm.pc_mall_domain" :placeholder="$t('请输入回调路径')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.paypalclientid')" prop="client_id">
          <el-input v-model="payPalForm.client_id" :placeholder="$t('请输入贝宝客户端ID')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.paypalsecret')" prop="client_secret">
          <el-input v-model="payPalForm.client_secret" :placeholder="$t('请输入贝宝密钥')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.brandname')" prop="brandName">
          <el-input v-model="payPalForm.brand_name" :placeholder="$t('请输入品牌名称')"></el-input>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <!-- <el-button class="bgColor" type="primary" @click="submitPayPalForm()">{{ $t('DemoPage.tableFromPage.save') }}</el-button> -->
            <el-button class="bgColor" type="primary" @click="submitForm('payPalForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>
      <!-- stripe -->
      <el-form v-if="this.$route.query.id == 16" :model="stripeForm" :rules="payPalRules" ref="payPalForm" class="paypal-form" label-width="210px">
        <el-form-item :label="$t('payManagement.paypalhdlj')" prop="pc_mall_domain">
          <el-input v-model="stripeForm.pc_mall_domain" :placeholder="$t('请输入回调路径')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.stripeKey')" prop="publishable_key">
          <el-input v-model="stripeForm.publishable_key" :placeholder="$t('payManagement.qsrstripeKey')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.stripePrivateKey')" prop="secret_key">
          <el-input v-model="stripeForm.secret_key" :placeholder="$t('payManagement.qsrstripePrivateKey')"></el-input>
        </el-form-item> 
        <div class="form-footer">
          <el-form-item>
            <!-- <el-button class="bgColor" type="primary" @click="submitPayPalForm()">{{ $t('DemoPage.tableFromPage.save') }}</el-button> -->
            <el-button class="bgColor" type="primary" @click="submitForm('payPalForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>
      <div style="width:121px"></div>
    </div>
  </div>
</template>

<script>
import parameterModify from '@/webManage/js/mall/payManagement/parameterModify'
export default parameterModify
</script>

<style scoped lang="less">
@import '../../../../webManage/css/mall/payManagement/parameterModify.less';
</style>
