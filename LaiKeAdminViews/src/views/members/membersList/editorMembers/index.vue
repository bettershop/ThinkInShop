<template>
  <div class="container">
    <!-- <div class="header">
      <span>编辑用户</span>
    </div> -->

    <div class="info-block" v-if="userInfo">
      <el-form :model="ruleForm" :rules="rule" ref="ruleForm" label-position="right" label-width="110px" class="form-search">
        <!-- <el-form-item class="members-head" :label="$t('editorMembers.yhtx')" style="margin-bottom:7px">
          <img :src="ruleForm.headimgurl" alt="">
        </el-form-item> -->
        <el-form-item :label="$t('addMembers.yhtx')" id="upload" ref="membersHead">
						<l-upload
              :limit="1"
              v-model="ruleForm.headimgurl"
              text=" "
            >
            </l-upload>
				</el-form-item>
        <el-form-item :label="$t('editorMembers.yhid')">
          <span>{{ userInfo.user_id }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.yhmc')" prop="user_name">
          <el-input v-model="ruleForm.user_name" :placeholder="$t('editorMembers.qsryhmc')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.yhzh')">
          <span>{{ userInfo.zhanghao }}</span>
        </el-form-item>
        <el-form-item :label="$t('addMembers.sjhm')" prop="mobile">
          <div style='display:flex;width:100%' class='phone'>
             <el-autocomplete
             class='auto'
              v-model="state"
              :fetch-suggestions="querySearchAsync"
              :placeholder="$t('addMembers.qsrqh')"
              @select="handleSelect" 
            >
             <template slot-scope="{ item }">
              <div>
                <span>{{ item.name }}</span>
                <span style="color: #999; margin-left: 10px;">+({{ item.code2 }})</span>
              </div>
            </template>
            </el-autocomplete>

            <el-input   
            class='phone-numer'
            v-on:input="ruleForm.mobile=ruleForm.mobile.replace(/^(-1+)|[^\d]+/g,'')"
            v-model="ruleForm.mobile" 
            :placeholder="$t('addMembers.qsrsjhm')"></el-input>
          </div>
        </el-form-item>
        <!-- 邮箱 -->
        <el-form-item :label="$t('merchants.addmerchants.yx')" prop="mailbox">
          <el-input
          v-model="ruleForm.mailbox" :placeholder="$t('merchants.addmerchants.qsryx')" auto-complete="new-password"></el-input>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.dlmm')" prop="loginPwd">
          <el-input
          v-on:input="ruleForm.loginPwd=ruleForm.loginPwd.replace(/[^\w]/g,'')"
          v-model="ruleForm.loginPwd"
          :placeholder="$t('editorMembers.qsrdlmm')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.zfmm')">
          <el-input v-model="ruleForm.paypwd" maxlength="6" @keyup.native="ruleForm.paypwd = oninput2(ruleForm.paypwd)" :placeholder="$t('editorMembers.qsrzfmm')"></el-input>
        </el-form-item>
        <!-- 账号余额 -->
        <el-form-item :label="$t('editorMembers.zhye')">
          <el-input disabled v-model="ruleForm.money" :placeholder="$t('editorMembers.asrzhye')" @keyup.native="ruleForm.money = oninput(ruleForm.money,2)"></el-input>
        </el-form-item>
        <!-- 积分余额 -->
        <el-form-item :label="$t('editorMembers.jfye')">
          <el-input
           disabled
           v-on:input="ruleForm.score=ruleForm.score.replace(/^(-1+)|[^\d]+/g,'')"
           v-model="ruleForm.score"
           :placeholder="$t('editorMembers.qsrjfye')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.yhsr')">
          <!-- <el-input v-model="ruleForm.birthday" placeholder="请输入会员生日"></el-input> -->
          <el-date-picker
            v-model="ruleForm.birthday"
            :picker-options="pickerOptionsStart"
            type="date"
            :placeholder="$t('editorMembers.xzrqsj')">
          </el-date-picker>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.xb')">
          <el-select
            class="select-input"
            v-model="ruleForm.sex"
          >
            <el-option
              v-for="(item, index) in sexList"
              :key="index"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.zhly')">
          <span>{{ $myGetSource(userInfo.source) }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.yxdds')">
          <span>{{ userInfo.z_num }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.jyje')">
          <span>{{laikeCurrencySymbol}} {{ userInfo.z_price.toFixed(2) }}</span>
        </el-form-item>
        <!-- <el-form-item :label="访问次数：">
          <span>{{  }}</span>
        </el-form-item> -->
        <el-form-item :label="$t('editorMembers.zhdl')">
          <span>{{ userInfo.last_time | dateFormat }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.zcsj')">
          <span>{{ userInfo.Register_data }}</span>
        </el-form-item>

        <!-- <div class="footer-button"> -->
        <el-form-item class="footer-button">
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
          <el-button plain class="footer-cancel fontColor kid_left" @click="$router.go(-1)">{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
        </el-form-item>
		  </el-form>
    </div>
  </div>
</template>

<script>
import editorMembers from '@/webManage/js/members/membersList/editorMembers'
export default editorMembers
</script>

<style scoped lang="less">
@import '../../../../webManage/css/members/membersList/editorMembers.less';
.phone{
    /deep/ .el-autocomplete{
          width: 90px !important;
            margin-right: 20px !important;
          /deep/  el-input{
            width: 90px !important;
            margin-right: 20px !important;
          } 
    }
    /deep/ .el-input {
          width: 256px !important;
    }
   /deep/ .phone-numer{
     flex:1 ;
      .el-input__inner {
          width: 100% !important;
    }
   }
 }
</style>
