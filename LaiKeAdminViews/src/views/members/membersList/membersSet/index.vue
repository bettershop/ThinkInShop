<template>
  <div class="container">
    <!-- <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button  :label="$t('membersLists.yhlb')" @click.native.prevent="$router.push('/members/membersList')"></el-radio-button>
        <el-radio-button :label="会员等级" @click.native.prevent="$router.push('/members/membersLevel')"></el-radio-button>
        <el-radio-button   :label="$t('membersLists.yhsz')" @click.native.prevent="$router.push('/members/membersSet')"></el-radio-button>
      </el-radio-group>
    </div> -->
    <el-form ref="ruleForm" class="form-search" :rules="rule" :model="ruleForm" label-width="130px">
        <div class="basic-info">
          <!-- <div class="header">
            <span>用户设置</span>
          </div> -->
          <div class="formBox">
              <div class="basic-block">
                <!-- 默认头像设置 -->
                <el-form-item :label="$t('membersSet.mrtx')" prop="wx_headimgurl">
                  <l-upload
                    :limit="1"
                    v-model="ruleForm.wx_headimgurl"
                    :text="$t('membersSet.txbz')"
                  >
                  </l-upload>
                </el-form-item>
                <!-- 默认昵称设置 -->
                <el-form-item :label="$t('membersSet.mrnc')" prop="wx_name">
                  <el-input maxlength="16" v-model="ruleForm.wx_name" @keyup.native="ruleForm.wx_name = stripscript(ruleForm.wx_name)" :placeholder="$t('membersSet.qsrmrnc')"></el-input>
                </el-form-item>
                <!-- 保存 -->
                <el-form-item label="">
                  <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')" :disabled="flag">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
                </el-form-item>
              </div>
        </div>
      </div>

    </el-form>
  </div>
</template>

<script>
import membersSet from '@/webManage/js/members/membersList/membersSet'
export default membersSet
</script>

<style scoped lang="less">
@import '../../../../webManage/css/members/membersList/membersSet.less';
</style>
