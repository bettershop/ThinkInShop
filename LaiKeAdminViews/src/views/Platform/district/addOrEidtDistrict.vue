<template>
  <div class="container">
    <div class="add-menu">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="120px">
        <el-form-item :label="$t('district.district_pid')" prop="district_pid">
          <el-select class="select-input" filterable v-model="ruleForm.district_pid" :placeholder="$t('qxzsjdq')">
            <el-option v-for="(item,index) in districts" :key="index" :label="item.district_name" :value="item.id">
              <div @click="getDistrictid(item)">{{ item.district_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item  :label="$t('district.district_country_num')" prop="district_country_num">
          <el-select class="select-input" filterable v-model="ruleForm.district_country_num" :placeholder="$t('qxzssgj')">
            <el-option v-for="(item,index) in countriesList" :key="index" :label="item.zh_name" :value="item.num3">
              <div @click="getIds(item.num3)">{{ item.zh_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('district.district_name')" prop="district_name">
          <el-input v-model="ruleForm.district_name" :placeholder="$t('district.qsrdistrict_name')"></el-input>
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
import main from "@/webManage/js/platform/district/districtSaveOrEdit";
export default main
</script>


<style scoped lang="less">
@import "../../../webManage/css/platform/district/districtSaveOrEdit";
</style>
