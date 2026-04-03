<template>
  <div class="container">
    <div class="add-menu">
      <el-form :model="mainForm" :rules="mainForm.rules" ref="ruleForm" class="picture-ruleForm" label-width="90px">
        <el-form-item :label="$t('smsList.save.lx')" prop="type">
          <el-select class="select-input" v-model="mainForm.type" :placeholder="$t('smsList.save.qxzlx')" value-key="text">
            <el-option v-for="(item,index) in smsTypeList" :key="index" :label="item.text" :value="item.value">
              <div @click="getCategory(item.text)">{{ item.text }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('smsList.save.lb')" prop="category">
          <el-select class="select-input" v-model="mainForm.category" :placeholder="$t('smsList.save.qxzlb')">
            <el-option v-for="(item,index) in categoryList" :key="index" :label="item.text" :value="item.value">
              <div @click="getSmsTemplateList(item.value)">{{ item.text }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('smsList.save.dxmb')" prop="smsTemplateId">
          <el-select class="select-input" v-model="smsTemplateType" :placeholder="$t('smsList.save.qxzdxmb')" @change="change_id">
            <el-option v-for="(item,index) in smsTemplateList" :key="index" :label="item.name" :value="item.id">
              <div @click="getTemplate(item.id)">{{ item.name }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <!-- <el-form-item :label="内容" prop="content">
          <div>{{mainForm.content}}</div>
        </el-form-item> -->

        <div class="form-footer">
          <el-form-item class="form-footers">
            <el-button class="bgColor" type="primary" @click="Save('ruleForm')">{{ $t('DemoPage.tableFromPage.save')}}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
        <!-- <div class="footer-button">
          <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">取消</el-button>
          <el-button type="primary" class="footer-save bgColor mgleft" @click="Save('ruleForm')">保存</el-button>
        </div> -->
      </el-form>
    </div>
  </div>
</template>

<script>
import main from "@/webManage/js/mall/sms/smsSave";

export default main
</script>

<style scoped lang="less">
  @import "../../../webManage/css/mall/sms/smsSave";
</style>

