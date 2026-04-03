<template>
  <div class="container">
    <div class="add-menu">
      <el-form :model="formMain" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="120px">
        <el-form-item :label="$t('logisticsCompanyManage.logisticsCompanyList.wlgsmc')" prop="kuaidi_name">
          <el-input v-model="formMain.kuaidi_name" :placeholder="$t('logisticsCompanyManage.logisticsCompanySave.qsrwlgs')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('logisticsCompanyManage.logisticsCompanyList.bm')" prop="type">
          <el-input v-model="formMain.type" :placeholder="$t('logisticsCompanyManage.logisticsCompanySave.qsrbm')"></el-input>
        </el-form-item>
        <el-form-item  :label="$t('logisticsCompanyManage.logisticsCompanySave.xh')" prop="sort">
          <el-input v-model="formMain.sort" :placeholder="$t('logisticsCompanyManage.logisticsCompanySave.qsrxh')"></el-input>
        </el-form-item>
        <el-form-item  :label="$t('logisticsCompanyManage.logisticsCompanyList.kg')">
          <el-switch v-model="formMain.is_open"/>
        </el-form-item>

        <div class="form-footer">
          <el-form-item>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
            <el-button class="bgColor" type="primary" @click="Save('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import main from "@/webManage/js/platform/logisticsCompanyManage/logisticsCompanySave";

export default main
</script>


<style scoped lang="less">
  @import "../../../webManage/css/platform/logisticsCompanyManage/logisticsCompanySave";
</style>
