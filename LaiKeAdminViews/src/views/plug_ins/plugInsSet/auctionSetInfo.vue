<template>
  <div class="container">
    <div class="box_two">
      <div class="header">
        <span>{{ $t("plugInsSet.auctionSetInfo.ggsz") }}</span>
      </div>
      <el-form
        :model="ruleForm"
        label-position="right"
        :rules="rules"
        ref="ruleForm"
        label-width="137px"
        class="form-search"
        style="padding-bottom: 18px" 
      >
        <el-form-item :label="$t('plugInsSet.auctionSetInfo.sfkq')">
          <el-switch
            v-model="ruleForm.isOpen"
            :active-value="1"
            :inactive-value="0"
            active-color="#00ce6d"
            inactive-color="#d4dbe8"
          >
          </el-switch>
          <div class="tip">
            <p>{{ $t("plugInsSet.auctionSetInfo.one") }}</p>
            <p>{{ $t("plugInsSet.auctionSetInfo.two") }}</p>
            <p>{{ $t("plugInsSet.auctionSetInfo.three") }}</p>
            <p>{{ $t("plugInsSet.auctionSetInfo.four") }}</p>
          </div>
        </el-form-item>
        <el-form-item :label="$t('plugInsSet.auctionSetInfo.zdkprs')">
          <el-input
            v-on:input="
              ruleForm.lowPepole = ruleForm.lowPepole.replace(
                /^(0+)|[^\d]+/g,
                ''
              )
            "
            style="width: 305px"
            :placeholder="$t('plugInsSet.auctionSetInfo.qsrzdkprs')"
            v-model="ruleForm.lowPepole"
          >
            <template slot="append">{{
              $t("plugInsSet.auctionSetInfo.ren")
            }}</template>
          </el-input>
          <!-- <span class="notice_font3">{{  }}</span> -->
          <span class="notice_font">{{
            $t("plugInsSet.auctionSetInfo.text1")
          }}</span>
        </el-form-item>
        <el-form-item :label="$t('plugInsSet.auctionSetInfo.cjddsj')">
          <el-input
            v-on:input="
              ruleForm.waitTime = ruleForm.waitTime.replace(/^(0+)|[^\d]+/g, '')
            "
            style="width: 305px"
            :placeholder="$t('plugInsSet.auctionSetInfo.qsrcjddsj')"
            v-model="ruleForm.waitTime"
          >
            <template slot="append">{{
              $t("plugInsSet.auctionSetInfo.miao")
            }}</template>
          </el-input>
          <!-- <span class="notice_font3">{{  }}</span> -->
          <span class="notice_font">{{
            $t("plugInsSet.auctionSetInfo.text12")
          }}</span>
        </el-form-item>
        <el-form-item :label="$t('plugInsSet.auctionSetInfo.blts')">
          <el-input
            v-on:input="
              ruleForm.days = ruleForm.days.replace(/^(0+)|[^\d]+/g, '')
            "
            style="width: 305px"
            :placeholder="$t('plugInsSet.auctionSetInfo.qsrblts')"
            v-model="ruleForm.days"
          >
            <template slot="append">{{
              $t("plugInsSet.auctionSetInfo.tian")
            }}</template>
          </el-input>
          <!-- <span class="notice_font3">{{  }}</span> -->
          <span class="notice_font">{{
            $t("plugInsSet.auctionSetInfo.text13")
          }}</span>
        </el-form-item>
        <el-form-item :label="$t('plugInsSet.auctionSetInfo.ddfkdjs')">
          <el-input
            v-on:input="
              ruleForm.orderFailure = ruleForm.orderFailure.replace(
                /^(0+)|[^\d]+/g,
                ''
              )
            "
            style="width: 305px"
            :placeholder="$t('plugInsSet.auctionSetInfo.qsrddfkdjs')"
            v-model="ruleForm.orderFailure"
          >
            <template slot="append">{{
              $t("plugInsSet.auctionSetInfo.xiaoshi")
            }}</template>
          </el-input>
          <!-- <span class="notice_font3">{{  }}</span> -->
          <span class="notice_font">{{
            $t("plugInsSet.auctionSetInfo.text14")
          }}</span>
        </el-form-item>
      </el-form>
    </div>
    <div class="box_two">
      <div class="header">
        <span>{{ $t("plugInsSet.auctionSetInfo.xysz") }}</span>
      </div>
      <el-form
        :model="ruleForm"
        label-position="right"
        :rules="rules"
        ref="ruleForm"
        label-width="137px"
        class="form-search"
      >
        <el-form-item :label="$t('plugInsSet.auctionSetInfo.xybt')" required>
          <el-input
            style="width: 551px"
            :placeholder="$t('plugInsSet.auctionSetInfo.qsrbtmc')"
            v-model="ruleForm.agreeTitle"
          >
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('plugInsSet.auctionSetInfo.xynr')" required>
          <vue-editor
            style="width: 100%; height: 365px"
            v-model="ruleForm.agreeContent"
            useCustomImageHandler
            @image-added="handleImageAdded"
          ></vue-editor>
        </el-form-item>
      </el-form>
    </div>
    <div class="box_three">
      <div class="header">
        <span>{{ $t("plugInsSet.auctionSetInfo.gzsz") }}</span>
      </div>
      <el-form
        :model="ruleForm"
        label-position="right"
        ref="ruleForm"
        label-width="137px"
      >
        
        <el-form-item :label="$t('distribution.distributionSet.gznr')">
          <vue-editor
          style="width: 100%; height: 365px"
          v-model="ruleForm.content"
          useCustomImageHandler
          @image-added="handleImageAdded"
        ></vue-editor>
        </el-form-item>
      </el-form>
      
    </div>
    <div style="min-height: 88px"></div>
    <div class="footer-button">
      <el-button
        plain
        class="footer-cancel fontColor"
        @click="back"
        >{{ $t("DemoPage.tableFromPage.cancel") }}</el-button
      >
      <el-button
        type="primary"
        class="footer-save bgColor mgleft"
        @click="submitForm('ruleForm')"
        >{{ $t("DemoPage.tableFromPage.save") }}</el-button
      >
    </div>
  </div>
</template>

<script>
import auctionSetInfo from "@/webManage/js/plug_ins/plugInsSet/auctionSetInfo";
export default auctionSetInfo;
</script>

<style scoped lang="less">
@import "../../../webManage/css/plug_ins/plugInsSet/auctionSetInfo.less";
.tip {
  width: 690px;
  // height: 83px;
  background-color: #f4f7f9;
  font-size: 14px;
  color: #97a0b4;
  padding: 8px;
  p {
    display: flex;
    align-items: center;
    height: 22px;
    line-height: 1;
  }
}
/deep/.el-input-group__append{
  min-width:4.375rem;
  text-align: center;
}
</style>
