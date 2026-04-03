<template>
  <div class="container">
    <div class="add-menu">
      <el-form :model="mainForm" :rules="mainForm.rules" ref="ruleForm" class="picture-ruleForm" label-width="150px">
<!--        <el-form-item :label="$t('addtemplate.ssgj')" class="title calculation" prop="country_num">
          <el-select  :disabled="disabled" filterable v-model="mainForm.country_num" :placeholder="$t('addtemplate.qxzssgj')">
            <el-option v-for="(item,index) in countriesList" :key="index" :label="item.zh_name" :value="item.num3">
              <div @click="getIds(item.num3)">{{ item.zh_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>-->
        <el-form-item :label="$t('smsList.save.dxqm')" prop="SignName">
          <span slot="label" v-if="shaco == 1">
            <span style="color:red">*</span>&nbsp; {{$t('smsList.save.dxqm')}}
          </span>
          <el-input v-model="mainForm.SignName" :placeholder="$t('smsList.save.qsrdxqm')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('smsList.save.dxmb')" prop="name">
          <span slot="label" v-if="shaco == 1">
            <span style="color:red">*</span>&nbsp; {{$t('smsList.save.dxmb')}}
          </span>
          <el-input v-model="mainForm.name" :placeholder="$t('smsList.save.qsrdxmb')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('smsList.save.lx')" prop="type">
          <span slot="label" v-if="shaco == 1">
            <span style="color:red">*</span>&nbsp; {{$t('smsList.save.lx')}}
          </span>
          <el-select class="select-input" v-model="mainForm.type" :placeholder="$t('smsList.save.qxzlx')" value-key="text">
            <el-option v-for="(item,index) in smsTypeList" :key="index" :label="item.text" :value="item.value">
              <div @click="getCategory(item.text)">{{ item.text }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('smsList.save.lb')" prop="category">
          <span slot="label" v-if="shaco == 1">
            <span style="color:red">*</span>&nbsp; {{$t('smsList.save.lb')}}
          </span>
          <el-select class="select-input" v-model="cate_type" :placeholder="$t('smsList.save.qxzlb')" @change="change_type">
            <el-option v-for="(item,index) in categoryList" :key="index" :label="item.text" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('smsList.save.dxco')" prop="TemplateCode">
          <span slot="label" v-if="shaco == 1">
            <span style="color:red">*</span>&nbsp; {{$t('smsList.save.dxco')}}
          </span>
          <el-input v-model="mainForm.TemplateCode" :placeholder="$t('smsList.save.qsrdxco')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('smsList.save.dxcs')">
    <span slot="label" v-if="shaco == 1">
      {{$t('smsList.save.dxcs')}}
    </span>
          <!-- 新增区号输入框：使用class替代内联样式 -->
          <el-input
            v-model="mainForm.cpc"
            :placeholder="$t('smsList.save.qh')"
            class="area-code-input"
          ></el-input>
          <!-- 原有手机号输入框：使用class替代内联样式 -->
          <el-input
            v-model="mainForm.phone"
            :placeholder="$t('smsList.save.qsrdxcs')"
            class="phone-input"
          >
          </el-input>
          <span style="color:#97a0b4;margin-left: 10px;">{{$t('smsList.save.yzdx')}}</span>
        </el-form-item>
        <el-form-item :label="$t('smsList.save.fsnr')" prop="content">
          <span slot="label" v-if="shaco == 1">
            <span style="color:red">*</span>&nbsp; {{$t('smsList.save.fsnr')}}
          </span>
          <el-input style="width:36.25rem" v-model="mainForm.content" type="textarea" :rows="2" :placeholder="$t('smsList.save.qsrr')"></el-input>
        </el-form-item>

        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="Save('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import main from "@/webManage/js/mall/sms/smsTemplateSave";

export default main
</script>

<style scoped lang="less">
@import "../../../webManage/css/mall/sms/smsTemplateSave";
.add-menu{
  margin: 0 auto;
}
.container{
  padding-top: 40px;
  align-items:normal
}
</style>
