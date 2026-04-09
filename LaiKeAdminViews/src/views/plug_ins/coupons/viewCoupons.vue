<template>
<div class="container">
  <el-form :model="ruleForm" label-position="right" ref="ruleForm" label-width="110px" class="form-search">
    <div class="basic-info">
      <div class="notice">
      <el-form-item label="发行单位" required>
        <el-radio-group v-model="ruleForm.issuer" disabled>
          <el-radio :label="0">商城</el-radio>
          <el-radio :label="1">自营店</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="优惠券类型" required>
        <!-- <span>{{ ruleForm.coupons_type }}</span> -->
        <el-select disabled class="select-input" v-model="ruleForm.coupons_type" placeholder="请选择优惠券类型">
          <el-option v-for="(item,index) in couponsTypeList" :key="index" :label="item.value" :value="item.key">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="优惠券名称" required>
        <!-- <span>{{ ruleForm.coupons_name }}</span> -->
        <el-input disabled class="coupons-name" v-model="ruleForm.coupons_name" placeholder="请输入优惠券名称"></el-input>
      </el-form-item>
      <!-- <el-form-item label="剩余数量：" required>
        <span>{{ ruleForm.num }}</span>
        <span class="num">张</span>
      </el-form-item> -->  
      <el-form-item label="面值" required v-if="ruleForm.coupons_type == 2" >
        <!-- <span>{{ ruleForm.face_value }}</span>
        <span class="num">元</span> -->
        <el-input disabled class="issue-number" v-model="ruleForm.face_value" placeholder="">
          <template slot="append">元</template>
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('coupons.addCoupons.zkz')"  v-if="ruleForm.coupons_type == 3 || ruleForm.type == 0" prop="issue_discount">
        <el-input disabled class="issue-number" v-model="ruleForm.discount" placeholder=""  >
          <template slot="append">{{$t('coupons.addCoupons.ze')}}</template>
        </el-input>
      </el-form-item>
      
      <el-form-item label="消费门槛" required>
        <!-- <span>消费</span>
        <span>{{ ruleForm.consumption_threshold }}</span>
        <span>元可使用</span>
        <span class="grey">（为0，则无限制）</span> -->
        <el-radio-group v-model="ruleForm.consumption_threshold_type" disabled>
          <el-radio :label="1">无门槛</el-radio>
          <el-radio :label="2">设置金额</el-radio>
        </el-radio-group>
        <div v-if="ruleForm.consumption_threshold_type == 2" class="shaco_box_one">
        <!-- 每人限领 -->
        <el-form-item label="消费" label-width="45px">
          <el-input disabled class="issue-number_three" v-model="ruleForm.consumption_threshold" placeholder="">
            <template slot="append">元</template>
          </el-input>
            <span style="margin-left: 10px;">可使用</span>
        </el-form-item>
        </div>
      </el-form-item>
      <el-form-item label="可用范围">
        <el-radio-group v-model="ruleForm.available_range" disabled>
          <el-radio v-for="item in availableRangeList" disabled :label="item.value" :key="item.value">{{item.name}}</el-radio>
        </el-radio-group>
        <div class="select-goods" v-if="ruleForm.available_range === 2">
          <!-- <el-select disabled v-model="ruleForm.select_goods" multiple placeholder="请选择商品">
            <el-option
              v-for="item in goodsList"
              :key="item.id"
              :label="item.product_title"
              :value="item.id">
            </el-option>
          </el-select> -->
          <div class="flex">
            <div class="changeUser" style="">
                <!-- <el-button class="add_bt" plain @click="AddPro">添加商品</el-button>
                <el-button plain @click="delPro" class="cancel">全部清空</el-button> -->
                <el-table :data="prochangedata" v-if="prochangedata.length>0" style="overflow: auto;max-height: 300px;width: 580px;border: 1px solid #E9ECEF;">
                    <!-- <el-table-column prop="goodsId" align="center" label="商品编号">
                    </el-table-column> -->
                    <el-table-column prop="proName" align="center" label="商品信息" width="200">
                        <template slot-scope="scope">
                            <div class="Info">
                                <img :src="scope.row.imgurl" width="40" height="40" alt="">
                                <span>{{ scope.row.product_title  }}</span>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="num" align="center" label="库存">
                    </el-table-column>
                    <el-table-column prop="price" align="center" label="价格">
                      <template slot-scope="scope">
                        {{ scope.row.price.toFixed(2) }}
                      </template>
                    </el-table-column>
                    <!-- <el-table-column prop="nums" align="center" label="购买数量">
                    </el-table-column>
                    <el-table-column prop="name" align="center" label="所属店铺">
                    </el-table-column> -->
                    <el-table-column prop="action" align="center" label="操作">
                        <template slot-scope="scope">
                          <div style="display: flex;justify-content: center;">
                            <el-button disabled class="delete" @click="ChangeProdel(scope.$index)" plain icon="el-icon-delete">删除</el-button>
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
          <div class="class-block">
            <span>已选：</span>
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
      <el-form-item label="领取方式" required>
        <el-radio-group v-model="ruleForm.pickup_type" disabled>
          <el-radio :label="0">手动领取</el-radio>
          <el-radio :label="1" :disabled="ruleForm.issue_number_type == 2">系统赠送</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="发行数量" required>
        <!-- <span>{{ ruleForm.issue_number }}</span>
        <span class="num">张</span> -->
        <el-radio-group disabled v-model="ruleForm.issue_number_type" @change="issueChange()">
          <el-radio :label="1">不限制</el-radio>
          <el-radio :label="2">设置数量</el-radio>
        </el-radio-group>
        <div v-if="ruleForm.issue_number_type == 2" class="shaco_box_one">
        <!-- 每人限领 -->
        <el-form-item>
          <el-input disabled class="issue-number_two" v-model="ruleForm.issue_number" @keyup.native="ruleForm.issue_number = oninput2(ruleForm.issue_number)" placeholder="">
            <template slot="append">张</template>
          </el-input>
        </el-form-item>
        </div>
      </el-form-item>
      <el-form-item label="过期时间" required>
        <!-- <span>{{ ruleForm.date[0] }}</span>
        <span class="zhi">至</span>
        <span>{{ ruleForm.date[1] }}</span> -->
        <el-radio-group v-model="ruleForm.date_type" disabled>
          <el-radio :label="1">不限制</el-radio>
          <el-radio :label="2">设置指定过期时间</el-radio>
          <el-radio :label="3">设置领取后多久失效</el-radio>
        </el-radio-group>
        <div class="shaco_box_one" v-if="ruleForm.date_type == 2">
          <el-date-picker
          disabled
          class="issue-number_four"
            v-model="ruleForm.date_one"
            type="date"
            placeholder="选择日期">
          </el-date-picker>
        </div>
        <div class="shaco_box_one" v-if="ruleForm.date_type == 3">
          <el-input disabled class="issue-number_four" v-model="ruleForm.date_num" placeholder="">
              <template slot="append">天</template>
          </el-input>
        </div>
      </el-form-item>
      <el-form-item label="使用限制" required v-if="ruleForm.pickup_type !=1">
        <!-- <span>每人限领1张</span> -->
        <div class="shaco_box_one">
          <el-form-item label="每人限领" label-width="78px">
            <el-input disabled class="issue-number_five" v-model="ruleForm.limit_num" placeholder="">
              <template slot="append">张</template>
            </el-input>
          </el-form-item>
        </div>
      </el-form-item>
      <el-form-item label="使用说明">
        <!-- <span>{{ ruleForm.instructions }}</span> -->
        <el-input disabled class="instructions" v-model="ruleForm.instructions" type="textarea" :rows="3" placeholder="请输入优惠券使用说明"></el-input>
      </el-form-item>
      </div>
    </div>
  </el-form>
  <div style="min-height: 91px;width: 100%;background: #edf1f5;border-radius: 4px 4px 0 0;"></div>
  <div class="footer-button">
      <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">返回</el-button>
      <!-- <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">保存</el-button> -->
  </div>
</div>
</template>

<script>
import viewCoupons from '@/webManage/js/plug_ins/coupons/viewCoupons'
export default viewCoupons
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/coupons/viewCoupons.less';
</style>