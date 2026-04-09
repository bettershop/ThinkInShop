<template>
<div class="container">
  <!-- <div class="header">
    <span>基本信息</span>
  </div> -->

  <el-form :model="ruleForm" label-position="right" :rules="rules" ref="ruleForm" label-width="auto" class="form-search">
    <div class="notice">
      <el-form-item :label="$t('coupons.addCoupons.fxdw')" required>
        <el-radio-group v-model="ruleForm.issuer">
          <el-radio :label="0">{{$t('coupons.addCoupons.sc')}}</el-radio>
          <el-radio :disabled="zy_coupon == 0" :label="1">{{$t('coupons.addCoupons.zyd')}}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.yhqlx')" prop="coupons_type">
        <el-select class="select-input" v-model="ruleForm.coupons_type" :placeholder="$t('coupons.addCoupons.qxzyhq')">
          <el-option v-for="(item,index) in couponsTypeList" :key="index" :label="item.label" :value="item.value">
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
        <el-radio-group v-model="ruleForm.type">
          <el-radio v-for="item in typeList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
        </el-radio-group>
      </el-form-item>
      <!-- <el-form-item :label="发行数量" v-if="ruleForm.coupons_type !== 4" prop="issue_number">
        <el-input class="issue-number" v-model="ruleForm.issue_number" @keyup.native="ruleForm.issue_number = oninput2(ruleForm.issue_number)" placeholder="">
          <template slot="append">张</template>
        </el-input>
      </el-form-item> -->
      <el-form-item v-if="ruleForm.coupons_type == 2 " :label="$t('coupons.addCoupons.mz')" prop="face_value">
        <el-input class="issue-number" v-model="ruleForm.face_value" placeholder="">
          <template slot="append">{{$t('coupons.addCoupons.yu')}}</template>
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.zkz')"  v-if="ruleForm.coupons_type == 3 || ruleForm.type == 0" prop="issue_discount">
        <el-input class="issue-number" v-model="ruleForm.issue_discount" placeholder="" @keyup.native="ruleForm.issue_discount = oninput(ruleForm.issue_discount)">
          <template slot="append">{{$t('coupons.addCoupons.ze')}}</template>
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.xfmk')" required>
        <el-radio-group v-model="ruleForm.consumption_threshold_type" @change="thresholdChange()">
          <el-radio :label="1">{{$t('coupons.addCoupons.wmk')}}</el-radio>
          <el-radio :label="2">{{$t('coupons.addCoupons.szje')}}</el-radio>
        </el-radio-group>
        <div v-if="ruleForm.consumption_threshold_type == 2" class="shaco_box_one">
        <el-form-item :label="$t('coupons.addCoupons.xf')" label-width="45px">
          <el-input class="issue-number_three" v-model="ruleForm.consumption_threshold" placeholder="">
            <template slot="append">{{$t('coupons.addCoupons.yu')}}</template>
          </el-input>
            <span style="margin-left: 10px;">{{$t('coupons.addCoupons.ksy')}}</span>
        </el-form-item>
        </div>
      </el-form-item>
      <!-- <el-form-item :label="消费门槛" prop="consumption_threshold">
        <span>消费</span>
        <el-input style="width:125px" class="consumption-threshold" v-model="ruleForm.consumption_threshold" placeholder=""></el-input>
        <span>元可使用</span>
        <span class="grey" style="color:#97a0b4"> 为0，则无限制 </span>
      </el-form-item> -->
      <el-form-item :label="$t('coupons.addCoupons.kyfw')" required>
        <el-radio-group v-model="ruleForm.available_range">
          <!-- <el-radio v-for="item in availableRangeList" :label="item.value" :key="item.value">{{item.name}}</el-radio> -->
          <el-radio :label="1" :key="1">{{$t('coupons.addCoupons.qbsp')}}</el-radio>
          <el-radio :label="2" :key="2">{{$t('coupons.addCoupons.zdsp')}}</el-radio>
          <el-radio :label="3" :key="3">{{$t('coupons.addCoupons.zdfl')}}</el-radio>
          <el-radio v-if="ruleForm.issuer != 1" :label="4" :key="4">{{$t('coupons.addCoupons.czhy')}}</el-radio>
        </el-radio-group>
        <div class="select-goods" v-show="ruleForm.available_range === 2">
          <!-- <el-select v-model="ruleForm.select_goods" multiple filterable placeholder="请选择商品">
            <el-option
              v-for="item in goodsList"
              :key="item.id"
              :label="item.product_title"
              :value="item.id">
            </el-option>
          </el-select> -->
          <div class="flex">
            <div class="changeUser" style="">
                <el-button class="add_bt" plain @click="AddPro">{{$t('coupons.addCoupons.tjsp')}}</el-button>
                <el-button plain @click="delPro" class="cancel">{{$t('coupons.addCoupons.qbqk')}}</el-button>
                <el-table :data="prochangedata" v-if="prochangedata.length>0" style="overflow: auto;width: 580px;max-height: 300px;border: 1px solid #E9ECEF;">
                    <!-- <el-table-column prop="goodsId" align="center" :label="商品编号">
                    </el-table-column> -->
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
                    <!-- <el-table-column prop="nums" align="center" :label="购买数量">
                    </el-table-column>
                    <el-table-column prop="name" align="center" :label="所属店铺">
                    </el-table-column> -->
                    <el-table-column prop="action" align="center" :label="$t('coupons.cz')">
                        <template slot-scope="scope">
                          <div style="display: flex;justify-content: center;">
                            <el-button class="delete" @click="ChangeProdel(scope.$index)" plain icon="el-icon-delete">{{$t('coupons.addCoupons.yc')}}</el-button>
                          </div>
                          </template>
                    </el-table-column>
                </el-table>
                <!-- <el-pagination  v-if="prochangedata.length>0" style="margin: 5px;float: right;"
                  prev-text="上一页" next-text="下一页"
                  @current-change="handleCurrentChange2"
                  background
                  layout="prev, pager, next"
                  :total="prochangedata.length">
                </el-pagination> -->
            </div>
          </div>
        </div>
        <div class="select-class" v-if="ruleForm.available_range === 3">
          <div class="addBtn">
            <el-button type="primary" @click="dialogVisible=true">{{$t('coupons.addCoupons.tjfl')}}</el-button>
          </div>
          <div class="class-block">
            <span>{{$t('coupons.addCoupons.yx')}}</span>
            <div class="class-item">
              <el-tree
              :data="toData"
              node-key="cid"
              default-expand-all
              :props="defaultProps"
              :expand-on-click-node="false">
                <span class="custom-tree-node" slot-scope="{ node, data }">
                  <span>{{ node.label }}</span>
                  <span class="remove">
                    <el-button
                      type="text"
                      size="mini"
                      icon="el-icon-close"
                      @click="() => remove2(node, data)">
                    </el-button>
                  </span>
                </span>
              </el-tree>
            </div>
          </div>
        </div>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.lqfs')" required>
        <el-radio-group v-model="ruleForm.pickup_type">
          <el-radio :label="0">{{$t('coupons.addCoupons.sdlq')}}</el-radio>
          <el-radio :label="1" :disabled="ruleForm.issue_number_type == 2">{{$t('coupons.addCoupons.zdff')}}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.fxsl')" required>
        <el-radio-group v-model="ruleForm.issue_number_type" @change="issueChange()" :disabled="ruleForm.pickup_type == 1">
          <el-radio :label="1">{{$t('coupons.addCoupons.bxz')}}</el-radio>
          <el-radio :label="2">{{$t('coupons.addCoupons.szsl')}}</el-radio>
        </el-radio-group>
        <div v-if="ruleForm.issue_number_type == 2" class="shaco_box_one">
        <!-- 每人限领 -->
        <el-form-item>
          <el-input class="issue-number_two" v-model="ruleForm.issue_number" @keyup.native="ruleForm.issue_number = oninput2(ruleForm.issue_number)" placeholder="">
            <template slot="append">{{$t('coupons.addCoupons.zhang')}}</template>
          </el-input>
        </el-form-item>
        </div>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.gqsj')" required>
        <el-radio-group v-model="ruleForm.date_type" @change="dateChange()">
          <el-radio :label="1">{{$t('coupons.addCoupons.bxz')}}</el-radio>
          <el-radio :label="2">{{$t('coupons.addCoupons.szzdgqsj')}}</el-radio>
          <el-radio :label="3">{{$t('coupons.addCoupons.szlqhdjsx')}}</el-radio>
        </el-radio-group>
        <div class="shaco_box_one" v-if="ruleForm.date_type == 2">
          <el-date-picker
          class="issue-number_four"
            v-model="ruleForm.date_one"
            type="date"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd HH:mm:ss"
            :placeholder="$t('coupons.addCoupons.xzrq')">
          </el-date-picker>
        </div>
        <div class="shaco_box_one" v-if="ruleForm.date_type == 3">
          <el-input class="issue-number_four" v-model="ruleForm.date_day" @keyup.native="ruleForm.date_day = oninput2(ruleForm.date_day)" placeholder="">
              <template slot="append">{{$t('coupons.addCoupons.tian')}}</template>
          </el-input>
        </div>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.syxz')" required v-if="ruleForm.pickup_type !=1">
        <div class="shaco_box_one">
          <el-form-item :label="$t('coupons.addCoupons.mrlq')" label-width="78px">
            <el-input class="issue-number_five" v-model="ruleForm.limit_count" placeholder=""
            v-on:input="ruleForm.limit_count=ruleForm.limit_count.replace(/^(0+)|[^\d]+/g,'')">
              <template slot="append">{{$t('coupons.addCoupons.zhang')}}</template>
            </el-input>
          </el-form-item>
        </div>
      </el-form-item>
      <!-- <el-form-item :label="有效时间" v-if="ruleForm.coupons_type !== 4" prop="date">
        <el-date-picker
          style="width:580px"
          v-model="ruleForm.date"
          :picker-options="forbiddenTime"
          type="datetimerange" range-separator="至" start-placeholder="开始日期"
          end-placeholder="结束日期" value-format="yyyy-MM-dd HH:mm:ss"
          :editable="false">
        </el-date-picker>
      </el-form-item>
      <el-form-item :label="有效时间" v-if="ruleForm.coupons_type == 4" prop="coupons_time">
        <span>开通会员当天后的</span>
        <el-input class="consumption-threshold" v-model="ruleForm.coupons_time" placeholder=""></el-input>
        <span>天可使用</span>
      </el-form-item>
      <el-form-item :label="领取限制" v-if="ruleForm.coupons_type !== 4 && limit_type == 0" required>
        <span>每人限领1张</span>
      </el-form-item>
      <el-form-item :label="领取限制" v-if="ruleForm.coupons_type !== 4  && limit_type == 1" required>
        <span>每人限领 </span>
        <el-input style="width:125px" class="issue-number" type="number" @blur="exgNumber()" v-model="ruleForm.limit_count" placeholder=""></el-input>
        <span>张</span>
      </el-form-item> -->
      <el-form-item :label="$t('coupons.addCoupons.sysm')">
        <el-input class="instructions" v-model="ruleForm.instructions" type="textarea" :rows="3" :placeholder="$t('coupons.addCoupons.qsryhqsy')"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{$t('DemoPage.tableFromPage.save')}}</el-button>
        <el-button plain class="footer-cancel fontColor kid_left" @click="$router.go(-1)">{{$t('DemoPage.tableFromPage.cancel')}}</el-button>
      </el-form-item>
      <!-- <div class="footer-button"> -->
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
        <tree-transfer :title="title" :from_data='fromData' :to_data='toData' :defaultProps="{label: 'cname',children: 'child'}" pid="sid" node_key="cid" @add-btn='add' @remove-btn='remove' :mode='mode' height='540px' filter>
        </tree-transfer>
        <!-- <tree-transfer :title="title" :from_data='fromData' :to_data='toData' :defaultProps="{label:'label'}" @add-btn='add' @remove-btn='remove' :mode='mode' height='540px' filter openAll>
      </tree-transfer> -->
      </div>
      <div slot="footer" class="form-footer">
        <el-button class="qx_bt" @click="dialogVisible = false">{{$t('coupons.ccel')}}</el-button>
        <el-button class="bdColor" type="primary" @click="handleClose">{{$t('coupons.okk')}}</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="$t('coupons.addCoupons.tjsp')" :visible.sync="dialogVisible2" width="920px">
        <div class="">
            <div class="Search" style="padding: 0;">
                <el-input class="Search-input" v-model="search" :placeholder="$t('coupons.addCoupons.qsrspmc')"></el-input>
                <!-- <el-select class="select-input" style="margin-right: 5px;" v-model="inputInfo.cid" placeholder="请选择分类">
                  <el-option v-for="item in cidList" :key="item.cid" :label="item.cname" :value="item.cid">
                  </el-option>
                </el-select> -->
                <el-cascader
                  v-model="inputInfo.cid"
                  class="Search-select"
                  ref="myCascader"
                  :placeholder="$t('coupons.addCoupons.qxzspfl')"
                  :options="classList"
                  :props="{ checkStrictly: true }"
                  @change="changeProvinceCity"
                  clearable>
                </el-cascader>
                <el-select class="select-input" v-model="inputInfo.brandId" :placeholder="$t('coupons.addCoupons.qxzsppp')">
                  <el-option v-for="item in brandList" :key="item.brand_id" :label="item.brand_name" :value="item.brand_id">
                  </el-option>
                </el-select>
                <el-button style="margin-left: 10px;" @click="Reset" plain>{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
                <el-button @click="query" type="primary" v-enter="query">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
            </div>
            <div class="Table">
                <el-table
                :header-cell-style="{
                background:'#F4F7F9',height:'50px'}"
                height="350" :row-key="getRowKey" :data="prodata" style="width: 100%" @selection-change="handleSelectionChange" ref="multipleTable">
                    <!-- <el-table-column :label="选择" align="center" width="55">
                        <template slot-scope="scope">
                            <el-radio :label="scope.row" v-model="tableRadio" ><i></i></el-radio>
                        </template>
                    </el-table-column> -->
                    <el-table-column :reserve-selection="true" align="center" type="selection" width="55"></el-table-column>
                    <el-table-column prop="ProName" align="center" :label="$t('coupons.addCoupons.qpxx')" width="280">
                        <template slot-scope="scope">
                            <div class="Info" style="display:flex">
                                <div> <img :src="scope.row.imgurl" width="40" height="40" alt=""/></div>
                                <div style="margin-left: 16px;text-align: left;display: flex;flex-direction: column;justify-content: center;"> <span>{{scope.row.product_title}}</span></div>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="p_name" align="center" :label="$t('coupons.addCoupons.fl')">
                    </el-table-column>
                    <el-table-column prop="brand_name" align="center" :label="$t('coupons.addCoupons.pp')">
                    </el-table-column>
                    <el-table-column prop="num" align="center" :label="$t('coupons.addCoupons.kc')">
                    </el-table-column>
                    <el-table-column prop="price" align="center" :label="$t('coupons.addCoupons.jg')">
                    </el-table-column>
                </el-table>
                <div class="pageBox">
                  <el-pagination
                    layout="sizes, slot, prev, pager, next"
                    :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
                    :next-text="$t('DemoPage.tableExamplePage.next_text')"
                    @size-change="handleSizeChange"
                    :page-sizes="pagesizes"
                    :current-page="pagination.page"
                    @current-change="handleCurrentChange"
                    :total="total"
                  >
                  <!-- {{currpage}}-{{current_num}} -->
                    <div class="pageRightText">{{$t('DemoPage.tableExamplePage.on_show')}} {{showItem}} {{$t('DemoPage.tableExamplePage.twig')}}{{total}}{{ $t('DemoPage.tableExamplePage.twig_notes') }}</div>
                  </el-pagination>
                </div>
            </div>

        </div>
        <span slot="footer" class="dialog-footer">
          <div style="display: flex;justify-content: space-between;align-items: center;">
            <div>
              <span style="color:#414658;font-size:16px">{{$t('coupons.addCoupons.yx')}}<span style="color:#2890FF">{{ store_num }}</span> {{$t('coupons.addCoupons.jian')}}</span>
            </div>
            <div>
              <el-button class="qx_bt" @click="dialogVisible2 = false;">{{$t('coupons.ccel')}}</el-button>
              <el-button type="primary" @click="confirm">{{$t('coupons.okk')}}</el-button>
            </div>
        </div>
        </span>
	  </el-dialog>
  </div>
  <div style="min-height: 20px;width: 100%;background: #edf1f5;border-radius: 4px 4px 0 0;"></div>
  <!-- <div class='hr'></div> -->
</div>
</template>

<script>
import addCoupons from '@/webManage/js/plug_ins/coupons/addCoupons'
export default addCoupons
</script>

<style scoped lang="less">
/deep/.el-dialog__body{
  padding: 40px 20px;
}
/deep/.wl-transfer .transfer-title{
  font-size: 14px
}
@import  '../../../webManage/css/plug_ins/coupons/addCoupons.less';
</style>
