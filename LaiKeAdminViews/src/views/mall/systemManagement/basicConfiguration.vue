<template>
  <div class="container">
    <div class="btn-nav" >
      <div class="btn-nav">
      <el-radio-group
        v-model="tabPosition"
        aria-checked="true"
        ref="tab_bun"
      >
      <el-radio-button
          label="10"
          >{{ $t("systemManagement.xtxx") }}</el-radio-button
        >
        <el-radio-button
          label="8"
          >{{ $t("systemManagement.tbsz") }}</el-radio-button
        >
        <el-radio-button
          label="9"
          >{{ $t("systemManagement.sysz") }}</el-radio-button
        >
        <el-radio-button
          label="2"
          >{{ $t("systemManagement.ssjmgc") }}</el-radio-button
        >
        <el-radio-button
          label="3"
          >{{ $t("systemManagement.wljdy") }}</el-radio-button
        >

        <el-radio-button
          label="4"
          >{{ $t("terminalConfig.wxxcx") }}</el-radio-button
        >
        <el-radio-button
          label="7"
          >{{ $t(`systemManagement.xypz`) }}</el-radio-button
        >
      </el-radio-group>
    </div>
    </div>


    <div>
      <!-- 基础配置 -->
      <systemsSetter v-if="tabPosition == 10" />
      <!-- 图标配置 -->
      <iconSetter v-if="tabPosition == 8" />
      <!-- 水印配置 -->
      <wartermarkSetter v-if="tabPosition == 9" />
      <!-- 基础配置 -->
      <underlyingConfiguration v-if="tabPosition == 1" />
      <!-- 收索及敏感词 -->
      <sensitiveWords :language='language' v-if="tabPosition == 2"/>
      <!-- 物流及打印 -->
      <logisticsAndPrinting :language='language' v-if="tabPosition == 3" />
      <!-- 微信小程序 -->
      <wechatminiprogram v-if="tabPosition == 4" :tabPosition="'1'"/>

      <!-- 协议配置 -->
      <protocolconfiguration v-if="tabPosition == 7" />

    </div>

    <div style="min-height: 1.9vw"></div>
  </div>
</template>

<script>
import basicConfiguration from "@/webManage/js/mall/systemManagement/basicConfiguration";
export default basicConfiguration;

</script>

<style scoped lang="less">
@import "../../../webManage/css/mall/systemManagement/basicConfiguration.less";
</style>
