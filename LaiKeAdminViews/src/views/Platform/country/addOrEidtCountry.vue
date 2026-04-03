<template>
  <div class="container">
    <div class="add-menu">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="120px">
        <el-form-item label="国家图标" prop="national_flag" required>
            <l-upload
              :limit="1"
              :size="86"
              :heightSize="86"
              v-model="ruleForm.national_flag"
              :text="$t('systemConfig.llqiconjycc')"
            >
            </l-upload>
        </el-form-item>
        <el-form-item :label="$t('countryManage.name')" prop="name">
          <el-input v-model="ruleForm.name" :placeholder="$t('countryManage.qsrname')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('countryManage.zh_name')" prop="zh_name">
          <el-input v-model="ruleForm.zh_name" :placeholder="$t('countryManage.qsrzh_name')"></el-input>
        </el-form-item>
        <el-form-item  :label="$t('countryManage.code2')" prop="code2">
          <el-input v-model="ruleForm.code2" :placeholder="$t('countryManage.qsrcode2')"></el-input>
        </el-form-item>
        <el-form-item  :label="$t('countryManage.code')" prop="code">
          <el-input v-model="ruleForm.code" :placeholder="$t('countryManage.qsrcode')"></el-input>
        </el-form-item>
        <el-form-item  :label="$t('countryManage.num3')" prop="num3">
          <el-input v-model="ruleForm.num3" :placeholder="$t('countryManage.qsrnum3')"></el-input>
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
import main from "@/webManage/js/platform/country/countrySaveOrEdit";
export default main
</script>


<style scoped lang="less">
@import "../../../webManage/css/platform/country/countrySaveOrEdit";
</style>
