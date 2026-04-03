<template>
  <div class="container">
    <div class="box_two">
      <div class="header">
        <span>{{ $t('plugInsSet.groupSetInfo.jcsz') }}</span>
      </div>
      <el-form
        :model="ruleForm"
        label-position="right"
        :rules="rules"
        ref="ruleForm"
        label-width="137px"
        class="form-search"
      >
        <el-form-item :label="$t('plugInsSet.groupSetInfo.yhdrksz')" class="append_first">
          <el-switch
            v-model="ruleForm.isOpen"
            :active-value="1"
            :inactive-value="0"
            active-color="#00ce6d"
            inactive-color="#d4dbe8"
          >
          </el-switch>
          <!-- v-if="ruleForm.isOpen == 1" -->
          <el-form-item>
                <div class="sb_box">
                  <span class="sb_font">{{ $t('plugInsSet.groupSetInfo.text11') }}</span>
                  <span class="sb_font">{{ $t('plugInsSet.groupSetInfo.text22') }}</span>
                  <span class="sb_font">{{ $t('plugInsSet.groupSetInfo.text33') }}</span>
                  <span class="sb_font">{{ $t('plugInsSet.groupSetInfo.text44') }}</span>
                </div>
              </el-form-item>
        </el-form-item>
      </el-form>
    </div>
    <div class="box_two">
      <div class="header">
        <span>{{ $t('plugInsSet.groupSetInfo.ddsz') }}</span>
      </div>
      <el-form
        :model="ruleForm"
        label-position="right"
        :rules="rules"
        ref="ruleForm"
        label-width="137px"
        class="form-search"
      >
        <el-form-item
          :label="$t('plugInsSet.groupSetInfo.zdshsj')"
          prop="autoTheGoods"
          class="append_first"
        >
          <span class="sbfont1">{{ $t('plugInsSet.groupSetInfo.text1') }}</span>
          <el-input
            v-on:input="
              ruleForm.autoTheGoods = ruleForm.autoTheGoods.replace(
                /^(0+)|[^\d]+/g,
                ''
              )
            "
            style="width: 318px"
            :placeholder="$t('plugInsSet.groupSetInfo.qsrzdshsj')"
            v-model="ruleForm.autoTheGoods"
          >
            <template slot="append">{{ $t('plugInsSet.groupSetInfo.tian') }}</template>
          </el-input>
          <span class="sbfont2">{{ $t('plugInsSet.groupSetInfo.text66') }}</span>
        </el-form-item>
        <el-form-item :label="$t('plugInsSet.groupSetInfo.text77')" required class="append_first">
          <span class="sbfont1">{{ $t('plugInsSet.groupSetInfo.ddwch') }}</span>
          <el-input
            v-on:input="
              ruleForm.orderAfter = ruleForm.orderAfter.replace(
                /^(-1+)|[^\d]+/g,
                ''
              )
            "
            style="width: 318px"
            :placeholder="$t('plugInsSet.groupSetInfo.qsrzdshsj')"
            v-model="ruleForm.orderAfter"
          >
            <template slot="append">{{ $t('plugInsSet.groupSetInfo.tian') }}</template>
          </el-input>
          <span class="sbfont2">{{ $t('plugInsSet.groupSetInfo.text1010') }}</span>
          <!-- <el-switch
            v-model="ruleForm.afterSwitch"
            :active-value="1"
            :inactive-value="0"
            active-color="#00ce6d"
            inactive-color="#d4dbe8"
            @change="afterChange($event)"
          >
          </el-switch>
          <span class="notice_font" v-if="ruleForm.afterSwitch == 0"
            >{{ $t('plugInsSet.groupSetInfo.text3') }}</span
          >
          <span class="notice_font" v-if="ruleForm.afterSwitch == 1"
            >{{ $t('plugInsSet.groupSetInfo.text4') }}</span
          >
          <div class="shaco_box_two" v-if="ruleForm.afterSwitch == 1">
            <span class="font_wone">{{ $t('plugInsSet.groupSetInfo.text5') }}</span>
            <el-input
              style="width: 111px"
              placeholder=""
              v-model="ruleForm.orderAfter"
              v-on:input="
                ruleForm.orderAfter = ruleForm.orderAfter.replace(
                  /^(0+)|[^\d]+/g,
                  ''
                )
              "
            >
              <template slot="append">{{ $t('plugInsSet.groupSetInfo.tian') }}</template>
            </el-input>
            <span class="font_wtwo"
              >{{ $t('plugInsSet.groupSetInfo.text6') }}</span
            >
          </div> -->
        </el-form-item>
        <el-form-item :label="$t('plugInsSet.groupSetInfo.text1111')" class="append_first">
          <el-switch
            v-model="ruleForm.goodSwitch"
            :active-value="1"
            :inactive-value="0"
            active-color="#00ce6d"
            inactive-color="#d4dbe8"
            @change="goodChange($event)"
          >
          </el-switch>
          <div class="shaco_box_three" v-if="ruleForm.goodSwitch == 1">
            <div>
              <span class="sbfont3">{{ $t('plugInsSet.groupSetInfo.text1212') }}</span>
              <el-input
                style="width: 175px"
                placeholder=""
                v-model="ruleForm.autoGoodCommentDay"
                v-on:input="
                  ruleForm.autoGoodCommentDay =
                    ruleForm.autoGoodCommentDay.replace(/^(0+)|[^\d]+/g, '')
                "
                clearable
              >
                <template slot="append">{{ $t('plugInsSet.groupSetInfo.tian') }}</template>
              </el-input>
              <span class="sbfont4"
                >{{ $t('plugInsSet.groupSetInfo.text1313') }}</span
              >
            </div>
            <div>
              <span class="font_wone" style="line-height: 80px">{{ $t('plugInsSet.groupSetInfo.hpnr') }}</span>
              <el-input
                style="width: 484px"
                type="textarea"
                :placeholder="$t('integralMall.mallSet.hpnr')"
                resize="none"
                v-model="ruleForm.autoCommentContent"
                rows="2"
              >
              </el-input>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div class="box_three">
      <div class="header">
        <span>{{ $t('plugInsSet.groupSetInfo.gzsz') }}</span>
      </div>
      <el-form
        :model="ruleForm"
        label-position="right"
        :rules="rules"
        ref="ruleForm"
        label-width="137px"
        class="shaco_form"
      >
      <el-form-item :label="$t('plugInsSet.groupSetInfo.gznr')">
        <vue-editor
          v-model="ruleForm.content"
          useCustomImageHandler
          @image-added="handleImageAdded"
        ></vue-editor>
        </el-form-item>
      </el-form>
      <!-- <div style="padding: 36px 117px 85px 117px">
        <vue-editor
          style="width: 100%; height: 365px"
          v-model="ruleForm.content"
          useCustomImageHandler
          @image-added="handleImageAdded"
        ></vue-editor>
      </div> -->
    </div>
    <div style="min-height: 71px"></div>
    <div class="footer-button">
      <el-button plain class="footer-cancel fontColor" @click="back"
        >{{ $t('DemoPage.tableFromPage.cancel') }}</el-button
      >
      <el-button
        type="primary"
        class="footer-save bgColor mgleft"
        @click="submitForm('ruleForm')"
        >{{ $t('DemoPage.tableFromPage.save') }}</el-button
      >
    </div>
  </div>
</template>

<script>
import groupSetInfo from '@/webManage/js/plug_ins/plugInsSet/groupSetInfo'
export default groupSetInfo
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/plugInsSet/groupSetInfo.less';
</style>
