<template>
<div class="container">
  <!-- <div class="header">
    <span>基本信息</span>
  </div> -->

  <el-form :model="ruleForm" label-position="right" ref="ruleForm" :rules="rules" label-width="auto" class="form-search">
    <div class="notice">
      <el-form-item :label="$t('coupons.addCoupons.fxdw')" required>
        <el-radio-group v-model="ruleForm.issuer" disabled>
          <el-radio :label="0">{{$t('coupons.addCoupons.sc')}}</el-radio>
          <el-radio :label="1">{{$t('coupons.addCoupons.zyd')}}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.yhqlx')" prop="coupons_type">
        <el-select disabled class="select-input" v-model="ruleForm.coupons_type" :placeholder="$t('coupons.addCoupons.qxzyhq')">
          <el-option v-for="(item,index) in couponsTypeList" :key="index" :label="item.value" :value="item.key">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.yhqmc')" prop="coupons_name">
        <el-input class="coupons-name" v-model="ruleForm.coupons_name" :placeholder="$t('coupons.addCoupons.qsryhq')"></el-input>
      </el-form-item>

      <el-form-item :label="$t('coupons.addCoupons.hydj')" v-if="ruleForm.coupons_type == 4" prop="grade">
        <el-select class="select-input" v-model="ruleForm.grade" :placeholder="$t('coupons.addCoupons.qxzhyd')">
          <el-option v-for="(item,index) in membersGrade" :key="index" :label="item.label" :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.lx')" v-if="ruleForm.coupons_type == 4">
        <el-radio-group v-model="ruleForm.type" disabled>
          <el-radio v-for="item in typeList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="ruleForm.coupons_type == 2" :label="$t('coupons.addCoupons.mz')" prop="face_value">
        <el-input disabled class="issue-number" v-model="ruleForm.face_value" placeholder="">
          <template slot="append">{{$t('coupons.addCoupons.yu')}}</template>
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.zkz')"  v-if="ruleForm.coupons_type == 3 || ruleForm.type == 0" prop="issue_discount">
        <el-input disabled class="issue-number" v-model="ruleForm.issue_discount" placeholder="" @keyup.native="ruleForm.issue_discount = oninput(ruleForm.issue_discount)">
          <template slot="append">{{$t('coupons.addCoupons.ze')}}</template>
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.xfmk')" required>
        <el-radio-group disabled v-model="ruleForm.consumption_threshold_type" @change="thresholdChange()">
          <el-radio :label="1">{{$t('coupons.addCoupons.wmk')}}</el-radio>
          <el-radio :label="2">{{$t('coupons.addCoupons.szje')}}</el-radio>
        </el-radio-group>
        <div v-if="ruleForm.consumption_threshold_type == 2" class="shaco_box_one">
        <el-form-item :label="$t('coupons.addCoupons.xf')" label-width="45px">
          <el-input disabled class="issue-number_three" v-model="ruleForm.consumption_threshold" placeholder="">
            <template slot="append">{{$t('coupons.addCoupons.yu')}}</template>
          </el-input>
            <span style="margin-left: 10px;">{{$t('coupons.addCoupons.ksy')}}</span>
        </el-form-item>
        </div>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.kyfw')">
        <el-radio-group v-model="ruleForm.available_range" disabled>
          <el-radio v-for="item in availableRangeList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
        </el-radio-group>
        <div class="select-goods" v-if="ruleForm.available_range === 2">
          <div class="flex">
            <div class="changeUser" style="">
                <el-table :data="prochangedata" v-if="prochangedata.length>0" style="overflow: auto;max-height: 300px;width: 580px;border: 1px solid #E9ECEF;">
                    <el-table-column prop="proName" align="center" :label="$t('coupons.addCoupons.spxx')" width="280">
                        <template slot-scope="scope">
                            <div class="Info">
                                <img :src="scope.row.imgurl" width="40" height="40" alt="">
                                <span>{{ scope.row.product_title  }}</span>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="num" align="center" :label="$t('coupons.addCoupons.kc')">
                    </el-table-column>
                    <el-table-column prop="price" align="center" :label="$t('coupons.addCoupons.jg')">
                    </el-table-column>
                    <el-table-column prop="action" align="center" :label="$t('coupons.cz')">
                        <template slot-scope="scope">
                          <div style="display: flex;justify-content: center;">
                            <el-button disabled class="delete" @click="ChangeProdel(scope.$index)" plain icon="el-icon-delete">删除</el-button>
                          </div>
                          </template>
                    </el-table-column>
                </el-table>
            </div>
          </div>
        </div>
        <div class="select-class" v-if="ruleForm.available_range === 3">
          <div class="class-block">
            <span>{{$t('coupons.addCoupons.yx')}}</span>
            <div class="class-item">
              <el-tree
              :data="toData"
              node-key="cid"
              default-expand-all
              :props="defaultProps"
              :expand-on-click-node="false">
              </el-tree>
            </div>
          </div>
        </div>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.lqfs')" required>
        <el-radio-group disabled v-model="ruleForm.pickup_type">
          <el-radio :label="0">{{$t('coupons.addCoupons.sdlq')}}</el-radio>
          <el-radio :label="1" :disabled="ruleForm.issue_number_type == 2">{{$t('coupons.addCoupons.zdff')}}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.fxsl')" required>
        <!-- :disabled="ruleForm.pickup_type == 1 || ruleForm.issue_number_type == 1" -->
        <el-radio-group v-model="ruleForm.issue_number_type" @change="issueChange()" disabled>
          <el-radio :label="1">{{$t('coupons.addCoupons.bxz')}}</el-radio>
          <el-radio :label="2" >{{$t('coupons.addCoupons.szsl')}}</el-radio>
        </el-radio-group>
        <div v-if="ruleForm.issue_number_type == 2" class="shaco_box_one">
        <!-- 每人限领 -->
        <el-form-item>
          <el-input disabled class="issue-number_two" v-model="ruleForm.issue_number" @keyup.native="ruleForm.issue_number = oninput2(ruleForm.issue_number)" placeholder="">
            <template slot="append">{{$t('coupons.addCoupons.zhang')}}</template>
          </el-input>
        </el-form-item>
        </div>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.gqsj')" required>
        <el-radio-group disabled v-model="ruleForm.date_type" @change="dateChange()">
          <el-radio :label="1">{{$t('coupons.addCoupons.bxz')}}</el-radio>
          <el-radio :label="2">{{$t('coupons.addCoupons.szzdgqsj')}}</el-radio>
          <el-radio :label="3">{{$t('coupons.addCoupons.szlqhdjsx')}}</el-radio>
        </el-radio-group>
        <div class="shaco_box_one" v-if="ruleForm.date_type == 2">
          <el-date-picker
            disabled
            class="issue-number_four"
            v-model="ruleForm.date_one"
            type="date"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd HH:mm:ss"
            :placeholder="$t('coupons.addCoupons.xzrq')">
          </el-date-picker>
        </div>
        <div class="shaco_box_one" v-if="ruleForm.date_type == 3">
          <el-input disabled class="issue-number_four" v-model="ruleForm.date_day" placeholder="">
              <template slot="append">{{$t('coupons.addCoupons.tian')}}</template>
          </el-input>
        </div>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.syxz')" required v-if="ruleForm.pickup_type !=1">
        <div class="shaco_box_one">
          <el-form-item :label="$t('coupons.addCoupons.mrlq')" label-width="78px">
            <el-input disabled class="issue-number_five" v-model="ruleForm.limit_count" placeholder=""
            v-on:input="ruleForm.limit_count=ruleForm.limit_count.replace(/^(0+)|[^\d]+/g,'')">
              <template slot="append">{{$t('coupons.addCoupons.zhang')}}</template>
            </el-input>
          </el-form-item>
        </div>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.sysm')">
        <el-input class="instructions" v-model="ruleForm.instructions" type="textarea" :rows="3" :placeholder="$t('coupons.addCoupons.qsryhqsy')"></el-input>
      </el-form-item>
      <!-- <div class="footer-button"> -->
        <el-form-item>
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{$t('DemoPage.tableFromPage.save')}}</el-button>
          <el-button plain class="footer-cancel fontColor kid_left" @click="$router.go(-1)">{{$t('DemoPage.tableFromPage.cancel')}}</el-button>
        </el-form-item>
      <!-- </div> -->
    </div>
  </el-form>

  <div class="dialog-block">
    <!-- 弹框组件 -->
    <el-dialog
    :title="$t('coupons.addCoupons.tjfl')"
      :visible.sync="dialogVisible"
      :before-close="handleClose"
    >
      <div class="dialog-container">
         <!-- arrayToTree -->
        <tree-transfer ref="tree" :title="title" :from_data='fromData' :to_data='toData' :defaultProps="{label: 'cname',children: 'child'}" pid="sid" node_key="cid" @add-btn='add' @remove-btn='remove' :mode='mode' height='540px' filter>
        </tree-transfer>
      </div>
      <div slot="footer" class="form-footer">
        <el-button class="qx_bt" @click="dialogVisible = false">{{$t('coupons.ccel')}}</el-button>
        <el-button class="bdColor" type="primary" @click="handleClose">{{$t('coupons.okk')}}</el-button>
      </div>
    </el-dialog>
  </div>
  <div style="min-height: 20px;width: 100%;background: #edf1f5;border-radius: 4px 4px 0 0;"></div>
</div>
</template>

<script>
import editorCoupons from '@/webManage/js/plug_ins/coupons/editorCoupons'
export default editorCoupons
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/coupons/editorCoupons.less';
</style>
