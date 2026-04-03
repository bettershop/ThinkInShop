<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('systemManagement.jcpz')" @click.native.prevent="$router.push('/mall/basicConfiguration')"></el-radio-button>
        <el-radio-button :label="$t('systemManagement.xypz')" @click.native.prevent="tabJump()" ></el-radio-button>
        <el-radio-button :label="$t('systemManagement.cjwt')" @click.native.prevent="$router.push('/mall/commonProblem')"></el-radio-button>
        <el-radio-button :label="$t('systemManagement.xszn')" @click.native.prevent="$router.push('/mall/newbieGuide')"></el-radio-button>
        <el-radio-button :label="$t('systemManagement.shfw')" @click.native.prevent="$router.push('/mall/afterSales')"></el-radio-button>
        <el-radio-button :label="$t('systemManagement.gywm')" @click.native.prevent="$router.push('/mall/aboutUs')"></el-radio-button>
      </el-radio-group>
    </div>

    <div class="basic-configuration">
      <div class="header">
        <span>{{$t('systemManagement.xszn')}}</span>
      </div>

      <el-form :model="ruleForm" label-position="right" ref="ruleForm" label-width="92px" class="form-search" :rules="rules">
        <div class="notice">
          <el-form-item :label="$t('systemManagement.gwlc')" prop="shopping_process" style="margin-bottom:8px">
            <div class="richText-info">
              <vue-editor
                v-model="ruleForm.shopping_process"
                useCustomImageHandler
                @image-added="handleImageAdded"
              ></vue-editor>
            </div>
          </el-form-item>
          <el-form-item :label="$t('systemManagement.zffs')" prop="payment_method" style="margin-bottom:28px">
            <div class="richText-info">
              <vue-editor 
                v-model="ruleForm.payment_method"
                useCustomImageHandler
                @image-added="handleImageAdded"
              ></vue-editor>
            </div>
          </el-form-item>
        </div>
        <div class="footer-button">
          <!-- <el-button plain class="footer-cancel fontColor" @click="$router.push('/mall/basicConfiguration')">取消</el-button> -->
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import newbieGuide from '@/webManage/js/mall/systemManagement/newbieGuide'
export default newbieGuide
</script>

<style scoped lang="less">
@import '../../../webManage/css/mall/systemManagement/newbieGuide.less';
</style>