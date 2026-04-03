<template>
  <div class="container">
    <div class="add-menu" >
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="120px">
        <el-form-item :label="$t('currency.currency_name')" prop="currency_name">
          <el-input v-model="ruleForm.currency_name" :placeholder="$t('currency.qsrcurrencyname')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('currency.currency_code')" prop="currency_code">
          <el-input v-model="ruleForm.currency_code" :placeholder="$t('currency.qsrzhcurrencycode')"></el-input><span style="margin-left: 10px !important">e.g.:USD</span>
        </el-form-item>
        <el-form-item  :label="$t('currency.currency_symbol')" prop="currency_symbol">
          <el-input v-model="ruleForm.currency_symbol" :placeholder="$t('currency.qsrcurrencysymbol')"></el-input>
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
import main from "@/webManage/js/platform/currency/currencySaveOrEdit";
export default main
</script>


<style scoped lang="less">
@import "../../../webManage/css/platform/currency/currencySaveOrEdit";
</style>
