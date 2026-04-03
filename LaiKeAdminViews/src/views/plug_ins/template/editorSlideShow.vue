<template>
  <div class="container">
      <!-- <div class="header">
        <span>编辑轮播图</span>
    </div> -->
    <el-form :model="ruleForm" label-position="right" ref="ruleForm" label-width="135px" class="form-search">
        <div class="notice">
          <el-form-item :label="$t('template.addSlideShow.tp')" required>
            <l-upload
              :limit="1"
              v-model="ruleForm.img"
              :text="$t('template.addSlideShow.jy750')"
            >
            </l-upload>
          </el-form-item>
          <el-form-item :label="$t('template.addSlideShow.lj')">
            <el-select class="select-input type-stlect" v-model="ruleForm.class1" placeholder="">
                <el-option v-for="(item,index) in classList1" :key="index" :label="item.label" :value="item.value">
                </el-option>
            </el-select>
            <el-select class="select-input selects" filterable v-if="ruleForm.class1 !== 0" v-model="ruleForm.url" :placeholder="$t('template.addSlideShow.qsrgj')">
                <el-option v-for="(item,index) in classList2" :key="index" :label="item.name" :value="item.parameter">
                </el-option>
            </el-select>
            <el-input v-else v-model="ruleForm.url"></el-input>
          </el-form-item>

          <div class="footer-button">
            <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{$t('template.addSlideShow.save')}}</el-button>
            <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">{{$t('template.ccel')}}</el-button>
          </div>
        </div>
      </el-form>
  </div>
</template>

<script>
import editorSlideShow from '@/webManage/js/plug_ins/template/editorSlideShow'
export default editorSlideShow
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/template/editorSlideShow.less';
</style>