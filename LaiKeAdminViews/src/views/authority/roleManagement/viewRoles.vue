<template>
  <div class="container">
    <div class="container">
      <div class="add-role">
        <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="90px">
          <el-form-item class="role-name" :label="$t('addRoles.jsmc')" prop="rolename">
            <span>{{ ruleForm.rolename }}</span>
          </el-form-item>
          <el-form-item class="permissions" :label="$t('addRoles.bdqx')" prop="" required>
            <el-tree
              :data="treeData"
              show-checkbox
              node-key="id"
              ref="tree"
              v-model="ruleForm.rolelist"
              :check-strictly="systemNodeFlag"
              :props="defaultProps">
            </el-tree>
          </el-form-item>
          <el-form-item class="role-describe" :label="$t('addRoles.jsms')" >
            <span>{{ ruleForm.roledescribe }}</span>
          </el-form-item>
          <div class="form-footer">
            <el-form-item>
              <!-- {{ $t('DemoPage.tableFromPage.cancel') }} -->
              <el-button class="bdColor" @click="$router.go(-1)" plain>{{$t('addRoles.fh')}}</el-button>
              <!-- <el-button class="bgColor" type="primary" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button> -->
            </el-form-item>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import viewRoles from '@/webManage/js/authority/roleManagement/viewRoles'
export default viewRoles
</script>

<style scoped lang="less">
@import '../../../webManage/css/authority/roleManagement/viewRoles.less';
</style>