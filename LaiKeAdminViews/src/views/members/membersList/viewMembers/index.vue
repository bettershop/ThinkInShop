<template>
  <div class="container">
    <!-- <div class="header">
      <div>
        <span>用户详情</span>
      </div>
      <div>
        <el-button type="primary" @click="$router.go(-1)">返回</el-button>
      </div>
    </div> -->

    <div class="info-block" v-if="userInfo">
      <el-form label-position="right" label-width="100px" class="form-search">
        <el-form-item class="members-head" :label="$t('editorMembers.yhtx')+':'">
          <img :src="userInfo.headimgurl" alt="">
        </el-form-item>
        <el-form-item :label="$t('editorMembers.yhid')+':'">
          <span>{{ userInfo.user_id }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.yhmc')+':'">
          <span>{{ userInfo.user_name }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.yhzh')+':'">
          <span>{{ userInfo.zhanghao }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.sjhm')+':'">
          <span><span v-if='userInfo.mobile' >+{{ userInfo.cpc }}</span> {{ userInfo.mobile }}</span>
        </el-form-item>
         <el-form-item :label="$t('merchants.addmerchants.yx')+':'">
          <span > {{ userInfo.e_mail }}</span>
        </el-form-item>
        <!-- <el-form-item :label="$t('editorMembers.dlmm')+':'">
          <span>{{ '******' }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.zfmm')+':'">
          <span>{{ '******' }}</span>
        </el-form-item> -->
        <el-form-item :label="$t('editorMembers.zhye')+':'">
          <span>{{laikeCurrencySymbol}} {{ userInfo.money.toFixed(2) }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.jfye')+':'">
          <span>{{ userInfo.score }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.yhsr')+':'">
          <span>{{ userInfo.birthday | dateFormat1 }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.xb')+':'">
          <span v-if="userInfo.sex == 1">男</span>
          <span v-if="userInfo.sex == 2">女</span>
          <span v-if="userInfo.sex == 0">未设置</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.zhly')+':'">
          <span>{{ $myGetSource(userInfo.source) }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.yxdds')+':'">
          <span>{{ userInfo.z_num }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.jyje')+':'">
          <span>{{laikeCurrencySymbol}} {{ userInfo.z_price.toFixed(2) }}</span>
        </el-form-item>
        <!-- <el-form-item label="访问次数：">
          <span>{{  }}</span>
        </el-form-item> -->
        <el-form-item :label="$t('editorMembers.zhdl')+':'">
          <span>{{ userInfo.last_time | dateFormat }}</span>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.zcsj')+':'">
          <span>{{ userInfo.Register_data }}</span>
        </el-form-item>
        <!-- <el-form-item label="推送CID：">
          <span>{{ userInfo.clientId }}</span>
        </el-form-item> -->
        <!-- <div class="footer-button">
          <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">返回</el-button>
        </div> -->
		  </el-form>
    </div>
  </div>
</template>

<script>
import viewMembers from '@/webManage/js/members/membersList/viewMembers'
export default viewMembers
</script>

<style scoped lang="less">
@import '../../../../webManage/css/members/membersList/viewMembers.less';
/deep/.el-form-item {
    //padding-bottom: 1.375rem !important;
}
</style>
