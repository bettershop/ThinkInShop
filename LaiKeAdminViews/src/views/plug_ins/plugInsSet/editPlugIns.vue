<template>
  <div class="container">
    <div class="add-menu">
      <el-form
        :model="ruleForm"
        :rules="rules"
        ref="ruleForm"
        class="picture-ruleForm"
        label-width="110px"
      >
        <el-form-item :label="$t('plugInsSet.editPlugIns.cjmc')" required>
          <el-input
            disabled
            v-model="ruleForm.plugin_name"
            :placeholder="$t('plugInsSet.editPlugIns.qsrcjmc')"
          ></el-input>
        </el-form-item>
        <el-form-item :label="$t('plugInsSet.editPlugIns.cjlx')" required>
          <el-input
            disabled
            v-model="plugType"
          ></el-input>
        </el-form-item>
        <el-form-item :label="$t('plugInsSet.editPlugIns.cjjs')" prop="content">
          <el-input
            type="textarea"
            rows="6"
            maxlength="200"
            resize="none"
            v-model="ruleForm.content"
            :placeholder="$t('plugInsSet.editPlugIns.qsrcjjsjtnr')"
          ></el-input>
        </el-form-item>
        <el-form-item :label="$t('plugInsSet.editPlugIns.sfsqdp')" required v-if="isMchPlugin=='1'">
          <el-switch
            v-model="ruleForm.status"
            :active-value="1"
            :inactive-value="0"
            active-color="#00ce6d"
            inactive-color="#d4dbe8"
          >
          </el-switch>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button
              class="bgColor"
              type="primary"
              @click="submitForm('ruleForm')"
              >{{ $t('DemoPage.tableFromPage.save') }}</el-button
            >
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{
              $t('DemoPage.tableFromPage.cancel')
            }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import editPlugIns from '@/webManage/js/plug_ins/plugInsSet/editPlugIns'
export default editPlugIns
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/plugInsSet/editPlugIns.less';
</style>
