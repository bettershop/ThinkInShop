<template>
    <div class="container">
      <el-form ref="ruleForm" class="form-search" :rules="rules" :model="ruleForm" label-width="150px">
        <div class="basic-info">
          <div class="header">
            <span>{{$t('distribution.distributionSet.jcsz')}}</span>
          </div>
          <div class="basic-block">
            <el-form-item :label="$t('distribution.distributionSet.sfkq')">
              <el-switch v-model="ruleForm.status" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
              </el-switch>
              <div class="tip">
                <p>{{ $t("distribution.distributionSet.one") }}</p>
                <p>{{ $t("distribution.distributionSet.two") }}</p>
                <p>{{ $t("distribution.distributionSet.three") }}</p>
              </div>
            </el-form-item>
            <el-form-item :label="$t('distribution.distributionSet.fxng')">
              <el-switch v-model="ruleForm.neigou" :active-value="2" :inactive-value="1" active-color="#00ce6d" inactive-color="#d4dbe8">
              </el-switch>
              <span class="gray">{{$t('distribution.distributionSet.kqfx')}}</span>
                </el-form-item>
            <el-form-item :label="$t('distribution.distributionSet.jmfx')">
              <el-switch v-model="ruleForm.advertising" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
              </el-switch>
              <span class="gray">{{$t('distribution.distributionSet.kqh')}}</span>
                </el-form-item>
            <el-form-item label="" v-if="ruleForm.advertising == 1">
              <l-upload
                :limit="1"
                v-model="ruleForm.adimage"
                :text="$t('distribution.distributionSet.zstz')"
              >
              </l-upload>
            </el-form-item>
            <el-form-item :label="$t('distribution.distributionSet.djjs')" required>
              <el-radio-group v-model="ruleForm.uplevel">
                <el-radio v-for="item in uplevelList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('distribution.distributionSet.yjjs')" required>
              <el-radio-group v-model="ruleForm.pay">
                <el-radio v-for="item in payList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item class="integral-proportion" :label="$t('distribution.distributionSet.fxcj')" prop="cengji">
              <el-select class="select-input" v-model="ruleForm.cengji" :placeholder="$t('distribution.distributionSet.qsrfx')">
                <el-option label="1层" value="1">
                </el-option>
                <el-option label="2层" value="2">
                </el-option>
              </el-select>
              <!-- <el-input :placeholder="$t('distribution.distributionSet.qsrfx')" v-model="ruleForm.cengji" @keyup.native="ruleForm.cengji = oninput2(ruleForm.cengji)">
              </el-input> -->
              <!-- <span>{{$t('distribution.distributionSet.ceng')}}</span> -->
              <span style="color: #97A0B4;">{{$t('distribution.distributionSet.fxcjmr')}}</span>
            </el-form-item>
            <el-form-item class="integral-proportion" :label="$t('distribution.distributionSet.yjjs')" prop="yjjisuan">
              <el-select class="select-input" v-model="ruleForm.yjjisuan" :placeholder="$t('distribution.distributionSet.qxzyjjs')">
                <el-option v-for="item in yjjisuanList" :key="item.value" :label="item.name" :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('distribution.distributionSet.fxgxbd')" required>
              <el-radio-group v-model="ruleForm.relationship">
                <el-radio v-for="item in relationshipList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </div>
        <div class="rules-set">
          <div class="header">
            <span>{{$t('distribution.distributionSet.gzsz')}}</span>
          </div>
          <el-form
            :model="ruleForm"
            label-position="right"
            ref="ruleForm"
            label-width="150px"
            class="form-set"
          >
            <el-form-item :label="$t('distribution.distributionSet.gznr')">
              <vue-editor
                v-model="ruleForm.content"
                useCustomImageHandler
                @image-added="handleImageAdded"
              ></vue-editor>
            </el-form-item>
            
          </el-form>
          
        </div>
        <div class="footer-button">
          <el-button plain class="footer-cancel fontColor" @click="back">{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
        </div>
      </el-form>
    </div>
  </template>
  
  <script>
  import distributionSetInfo from '@/webManage/js/plug_ins/plugInsSet/distributionSetInfo'
  export default distributionSetInfo
  </script>
  
  <style scoped lang="less">
  @import '../../../webManage/css/plug_ins/plugInsSet/distributionSetInfo.less';
  .tip {
    width: 750px;
    // height: 83px;
    background-color: #f4f7f9;
    font-size: 14px;
    color: #97a0b4;
    padding: 8px;
    p {
      display: flex;
      align-items: center;
      height: 22px;
      line-height: 1;
    }
  }
  .form-set{
    margin: 57px 80px 57px 0;
  }
  /deep/.el-radio{
    margin-right: 18px;
  }
  </style>