<template>
  <div class="container">
    <div class="parameters-change">
      <el-form
        class="wx-form"
        label-width="110px"
        :rules="rules"
        ref="rulesForm"
      >
        <el-form-item :label="$t('payManagement.mc')">
          <el-input :disabled="true" v-model="formData.name"></el-input>
        </el-form-item>
        <el-form-item :label="$t('payManagement.tb')" class="prompt upload-headimg" prop="Logo">
          <div class="upload-box">
            <div class="avatar-uploader-box">
              <div class="video-preview"  @mouseover.stop="isShowPopup = true" :style="formData.Logo.length > 0?'border: 1px solid #D5DBE8;border-radius: 6px;':''">
                <img
                  :src="`${formData.Logo}`"
                  class="avatar"
                  v-show="formData.Logo.length > 0"
                />
                <div
                @mouseleave="isShowPopup = false"
                  class="avatar-uploader-popup"
                  v-show="formData.Logo.length > 0&&isShowPopup"
                >
                  <i @click="handleDelIcon" class="el-icon-delete"></i>
                </div>
              </div>
              <el-upload
                class="avatar-uploader"
                action="image/*"
                list-type="picture-card"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :on-change="uploadFile"
                :limit="1"
                v-show="formData.Logo.length <= 0"
              >
                <span style="display: block">
                  <i class="el-icon-plus avatar-uploader-icon" v-loading="videoLoading2"></i>
                </span>
              </el-upload>
              <div class="upload-tip">
                <slot>（{{$t('payManagement.editIcon.jycu')}}）</slot>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item :label="$t('payManagement.editIcon.bz')">
          <el-input
            type="textarea"
            :placeholder="$t('payManagement.editIcon.qsrbzsm')"
            v-model="formData.remark"
          ></el-input>
          <!-- <textarea name="1212" id=""  placeholder="请输入备注说明"></textarea> -->
        </el-form-item>

        <div class="form-footer">
          <el-form-item>
            <!-- @click="submitForm('ruleForm1')" -->
            <el-button class="bgColor" type="primary" @click="handleOK">{{
              $t("DemoPage.tableFromPage.save")
            }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{
              $t("DemoPage.tableFromPage.cancel")
            }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import editIcon from "@/webManage/js/mall/payManagement/editIcon";
export default editIcon;
</script>

<style scoped lang="less">
@import "../../../../webManage/css/mall/payManagement/editIcon.less";
</style>
