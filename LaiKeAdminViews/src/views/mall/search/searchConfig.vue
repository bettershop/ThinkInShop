<template>
  <div class="container">
    <div class="formCenter">
      <el-form :model="dataForm"  :rules="rules" ref="dataForm"  class="picture-ruleForm" label-width="100px">
        <el-form-item :label="$t('searchConfig.sfkq')">
          <el-switch active-color="#13ce66" inactive-color="#d5dbe8" :disabled="dataForm.is_open === 1" v-model="dataForm.is_open"></el-switch>
        </el-form-item>
        <el-form-item v-if="dataForm.is_open" :label="$t('searchConfig.gjcsx')" prop="num">
          <el-input v-model="dataForm.num" @keyup.native="dataForm.num = oninput2(dataForm.num)" :placeholder="$t('searchConfig.qsrgjcsx')"></el-input>
        </el-form-item>
        <el-form-item v-if="dataForm.is_open" :label="$t('searchConfig.gjc')" prop="keyword">
          <el-input v-model="dataForm.keyword" type="textarea" :placeholder="$t('searchConfig.qsrgjc')"></el-input>
          <span>{{$t('searchConfig.gjcbz')}}</span>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('dataForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <!-- <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button> -->
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>


<script>
import addSearchConfig from "@/webManage/js/mall/search/addSearchConfig";

export default addSearchConfig
</script>

<style scoped lang="less">
  @import "../../../common/commonStyle/form";
  @import "../../../webManage/css/mall/search/searchConfig";
</style>
