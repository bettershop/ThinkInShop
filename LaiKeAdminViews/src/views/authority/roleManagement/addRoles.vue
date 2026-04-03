<template>
  <div class="container">
    <div class="add-role">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="90px">
        <el-form-item class="role-name" :label="$t('addRoles.jsmc')" prop="rolename">
            <el-input v-model="ruleForm.rolename" :placeholder="$t('addRoles.qsrjsmc')"></el-input>
        </el-form-item>
        <el-form-item class="permissions" :label="$t('addRoles.bdqx')" prop="" required>
          <el-tree
            :data="treeData"
            show-checkbox
            node-key="id"
            ref="tree"
            v-model="ruleForm.rolelist"
            @check-change="handleCheckChange"
            :props="defaultProps">
          </el-tree>
        </el-form-item>
        <el-form-item class="role-describe" :label="$t('addRoles.jsms')" >
            <el-input v-model="ruleForm.roledescribe" :placeholder="$t('addRoles.qsrjsms')" type="textarea"></el-input>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor kid_left" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import addRoles from '@/webManage/js/authority/roleManagement/addRoles'
export default addRoles
</script>

<style scoped lang="less">
@import '../../../webManage/css/authority/roleManagement/addRoles.less';
</style>
