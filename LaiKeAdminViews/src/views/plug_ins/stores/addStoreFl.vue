<template>
  <div class="container">
    <!-- <div class="header">
      <span>添加分类</span>
    </div> -->

    <el-form label-position="right" ref="ruleForm" label-width="100px" class="form-search">
      <div class="notice">
        <el-form-item class="input-interval" :label="$t('stores.addStoreFl.flmc')" prop="" required>
          <el-input style="width:580px" v-model="ruleForm.name" :placeholder="$t('stores.addStoreFl.qsrfl')"></el-input>
        </el-form-item>

        <el-form-item :label="$t('stores.addStoreFl.fltb')">
          <l-upload
            :limit="1"
            v-model="ruleForm.img"
            :text="$t('stores.addStoreFl.zdscq')"
          >
          </l-upload>
        </el-form-item>

        <el-form-item class="input-interval" :label="$t('stores.addStoreFl.pxh')" prop="" required>
          <el-input style="width:580px" @keyup.native="ruleForm.px = oninput2(ruleForm.px)" v-model="ruleForm.px" :placeholder="max + $t('stores.addStoreFl.mrpxh')"></el-input>
        </el-form-item>

       <el-form-item :label="$t('stores.addStoreFl.sfxs')" prop="" required>
            <el-radio-group v-model="ruleForm.ishow">
              <el-radio v-for="item in belongList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
            </el-radio-group>
        </el-form-item>

        <el-form-item class="footer-button">
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm">{{$t('DemoPage.tableFromPage.save')}}</el-button>
          <el-button plain class="footer-cancel fontColor shaco_left" @click="$router.go(-1)">{{$t('DemoPage.tableFromPage.cancel')}}</el-button>
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>
<script>
import addStoreFl from '@/webManage/js/plug_ins/stores/addStoreFl'
export default addStoreFl
</script>
<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/stores/addStoreFl.less';
</style>
