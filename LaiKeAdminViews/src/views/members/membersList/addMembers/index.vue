<template>
  <div class="container">
    <!-- <div class="header">
      <span>添加用户</span>
    </div> -->

    <el-form :model="ruleForm" label-position="right" :rules="rule" ref="ruleForm" label-width="120px" class="form-search">
      <div class="notice">
        <el-form-item :label="$t('addMembers.yhtx')" id="upload" prop="membersHead" ref="membersHead">
						<l-upload
              :limit="1"
              v-model="ruleForm.membersHead"
              text=" "
            >
            </l-upload>
				</el-form-item>
        <el-form-item :label="$t('addMembers.yhmc')" prop="userName">
          <el-input v-model="ruleForm.userName" :placeholder="$t('addMembers.qsryhmc')"></el-input>
          <!-- <span class="gray">(用户名称默认显示用户设置的昵称)</span> -->
        </el-form-item>
        <!-- <el-form-item :label="用户级别" prop="grade">
          <el-select class="select-input" v-model="ruleForm.grade" placeholder="请选择用户等级">
            <el-option v-for="(item,index) in membersGrade" :key="index" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
          <span class="gray">(用户级别默认显示普通用户)</span>
        </el-form-item> -->
        <el-form-item :label="$t('addMembers.yhzh')" prop="zhanghao">
         <!-- <el-input
          maxlength="16"
          v-on:input="ruleForm.zhanghao=ruleForm.zhanghao.replace(/[^\w]/g,'')"
          v-model="ruleForm.zhanghao" :placeholder="$t('addMembers.qsryhzh')"></el-input> -->
          <span>{{ruleForm.zhanghao}}</span>
        </el-form-item>
        <el-form-item :label="$t('addMembers.yhmm')" prop="mima">
          <el-input
          v-on:input="ruleForm.mima=ruleForm.mima.replace(/[^\w]/g,'')"
          v-model="ruleForm.mima" :placeholder="$t('addMembers.qsryhmm')" show-password auto-complete="new-password"></el-input>
        </el-form-item>
        <el-form-item :label="$t('addMembers.qrmm')" prop="confirmMima">
          <el-input
          v-on:input="ruleForm.confirmMima=ruleForm.confirmMima.replace(/[^\w]/g,'')"
          v-model="ruleForm.confirmMima" :placeholder="$t('addMembers.qqrmm')" show-password auto-complete="new-password"></el-input>
        </el-form-item>
        <el-form-item :label="$t('addMembers.sjhm')" prop="phone">
         <div  style='display:flex;width:100%' class='phone'>
             <el-autocomplete
             class='auto custom-autocomplete inline-input'
              v-model="state"
              :fetch-suggestions="querySearchAsync"
              :placeholder="$t('addMembers.qsrqh')"
              @select="handleSelect"
              :popper-append-to-body="false"
            >
             <template slot-scope="{ item }">
              <div>
                <span>{{ item.name }}</span>
                <span style="color: #999; margin-left: 10px;">+({{ item.code2 }})</span>
              </div>
            </template>
            </el-autocomplete>

            <el-input
            class='phone-numer'
            v-on:input="ruleForm.phone=ruleForm.phone.replace(/^(-1+)|[^\d]+/g,'')"
            v-model="ruleForm.phone"
            :placeholder="$t('addMembers.qsrsjhm')"></el-input>
          </div>
        </el-form-item>
        <!-- 邮箱 -->
        <el-form-item :label="$t('merchants.addmerchants.yx')" prop="mailbox">
          <el-input
          v-model="ruleForm.mailbox" :placeholder="$t('merchants.addmerchants.qsryx')" auto-complete="new-password"></el-input>
        </el-form-item>

        <el-form-item :label="$t('addMembers.zhly')" prop="source">
          <el-select class="select-input" v-model="ruleForm.source" :placeholder="$t('addMembers.qxzzhly')">
            <el-option v-for="(item,index) in sourceList" :key="index" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.yhsr')">
          <!-- <el-input v-model="ruleForm.birthday" placeholder="请输入会员生日"></el-input> -->
          <el-date-picker
            v-model="ruleForm.birthday"
            :picker-options="pickerOptionsStart"
            type="date"
            :placeholder="$t('editorMembers.xzrqsj')">
          </el-date-picker>
        </el-form-item>
        <el-form-item :label="$t('editorMembers.xb')">
          <el-select
            class="select-input"
            v-model="ruleForm.sex"
          >
            <el-option
              v-for="(item, index) in sexList"
              :key="index"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item class="footer-button">
        <!-- <div class="footer-button"> -->
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
          <el-button plain class="footer-cancel fontColor kid_left" @click="$router.go(-1)">{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
        <!-- </div> -->
        </el-form-item>
      </div>
		</el-form>
  </div>
</template>

<script>
import addMembers from '@/webManage/js/members/membersList/addMembers'
export default addMembers
</script>

<style scoped lang="less">
@import '../../../../webManage/css/members/membersList/addMembers.less';

 .phone{
   /deep/ .phone-numer{
       width: 433px !important;
      .el-input__inner {
          width: 100% !important;
    }
   }
 }
</style>
