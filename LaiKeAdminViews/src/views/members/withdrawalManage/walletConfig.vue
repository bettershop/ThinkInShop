<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('withdrawalExamineList.txsh')" @click.native.prevent="$router.push('/members/withdrawalManage/withdrawalExamineList')" v-if="handleTabLimits(routerList,'/members/withdrawalManage/withdrawalExamineList')"></el-radio-button>
        <el-radio-button :label="$t('withdrawalExamineList.txjl')" @click.native.prevent="$router.push('/members/withdrawalManage/withdrawalRecordList')" v-if="handleTabLimits(routerList,'/members/withdrawalManage/withdrawalRecordList')"></el-radio-button>
        <el-radio-button :label="$t('withdrawalExamineList.qbcs')" @click.native.prevent="$router.push('/members/withdrawalManage/walletConfig')" v-if="handleTabLimits(routerList,'/members/withdrawalManage/walletConfig')"></el-radio-button>
      </el-radio-group>
    </div>

    <div class="add-menu">
      <el-form :model="mainForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="auto">
        <el-form-item :label="$t('walletConfig.zxczje',{currencySymbol:laikeCurrencySymbol})" prop="min_cz">
          <el-input v-model="mainForm.min_cz" :placeholder="$t('walletConfig.qsrzxczje',{currencySymbol:laikeCurrencySymbol})"></el-input>
        </el-form-item>
        <el-form-item :label="$t('walletConfig.zxtx',{currencySymbol:laikeCurrencySymbol})" prop="min_amount">
          <el-input v-model="mainForm.min_amount" :placeholder="$t('walletConfig.qsrzxtx',{currencySymbol:laikeCurrencySymbol})"></el-input>
        </el-form-item>

        <el-form-item :label="$t('walletConfig.zdtx',{currencySymbol:laikeCurrencySymbol})" prop="max_amount">
          <el-input v-model="mainForm.max_amount" :placeholder="$t('walletConfig.qsrzdtx',{currencySymbol:laikeCurrencySymbol})"></el-input>
        </el-form-item>
        <el-form-item class="poundage" :label="$t('walletConfig.sxf',{currencySymbol:laikeCurrencySymbol})" prop="service_charge">
          <div class="poun_flex">
            <div>
              <el-input v-model="mainForm.service_charge" :placeholder="$t('walletConfig.qsrsxf',{currencySymbol:laikeCurrencySymbol})">
                <el-button slot="append">%</el-button>
              </el-input>
            </div>
            <div class="prompt">{{$t('walletConfig.sxfbz')}}</div>
          </div>
        </el-form-item>
        <!-- <el-form-item :label="$t('walletConfig.qbdw')" prop="unit">
          <el-input v-model="mainForm.unit" :placeholder="$t('walletConfig.qsrqbdw')"></el-input>
        </el-form-item> -->

        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <!-- <el-button class="bdColor kid_left" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button> -->
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import main from "@/webManage/js/finance/withdrawalManage/walletConfig";
export default main
</script>

<style scoped lang="less">
// ../../../webManage/css/finance/withdrawalManage/walletConfig
  @import "../../../webManage/css/finance/withdrawalManage/walletConfig.less";
</style>
