<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('coupons.yhqlb')"
          @click.native.prevent="$router.push('/plug_ins/coupons/couponsList')"></el-radio-button>
        <el-radio-button :label="$t('coupons.yhqsz')"
          @click.native.prevent="$router.push('/plug_ins/coupons/couponsSet')"></el-radio-button>
      </el-radio-group>
    </div>

    <div class="coupons-set">
      <el-form :model="ruleForm" label-position="right" ref="ruleForm" label-width="185px" class="form-search">
        <div class="notice">
          <el-form-item :label="$t('coupons.couponsSet.sfkqlq')">
            <el-switch v-model="ruleForm.isShow" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
            <span class="set_font">{{ $t('coupons.couponsSet.lqzxyy') }}</span>
          </el-form-item>
          <el-form-item :label="$t('coupons.couponsSet.dpyhqsz')">
            <el-switch v-model="ruleForm.switchs" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
            <span class="set_font">{{ $t('coupons.couponsSet.dpyhyy') }}</span>
          </el-form-item>
          <!-- <el-form-item :label="是否自动清除过期优惠券：">
            <el-radio-group v-model="ruleForm.clear_coupon">
              <el-radio v-for="item in clearCouponList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
            </el-radio-group>
            <span class="prompt">这个是指自动清除前端用户已失效的优惠券记录</span>
            <div class="clear_coupon_day" v-if="ruleForm.clear_coupon == '1'">
              <span>优惠券过期后：</span>
              <el-input type="number" min="0" v-model="ruleForm.clear_coupon_day"></el-input>
              <span>天，系统自动清除。</span>
            </div>
          </el-form-item>
          <el-form-item :label="是否自动清除过期活动：">
            <el-radio-group v-model="ruleForm.clear_activity">
              <el-radio v-for="item in clearActivityList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
            </el-radio-group>
            <span class="prompt">设置后，将自动清除优惠券列表中已过期的记录</span>
            <div class="clear_activity_day" v-if="ruleForm.clear_activity == '1'">
              <span>活动过期后：</span>
              <el-input type="number" min="0" v-model="ruleForm.clear_activity_day"></el-input>
              <span>天，系统自动清除。</span>
            </div>
          </el-form-item>
          <el-form-item :label="限领设置：">
            <el-radio-group v-model="ruleForm.limit_get">
              <el-radio v-for="item in limitGetList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
            </el-radio-group>
          </el-form-item> -->
          <el-form-item :label="$t('coupons.couponsSet.yhqlx')" class="coupons-type" required>
            <el-checkbox class="allGoods" :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange">{{$t('coupons.couponsSet.qx')}}</el-checkbox>
						<el-checkbox-group v-model="ruleForm.coupons_type">
              <el-checkbox v-for="item in couponsTypeList" :label="item.key" :key="item.key" @change="handleCheckedCitiesChange">{{item.value}}</el-checkbox>
            </el-checkbox-group>
					</el-form-item>
        
        <div class="footer-button">
          <!-- <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">返回</el-button> -->
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
        </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import couponsSet from '@/webManage/js/plug_ins/coupons/couponsSet'
export default couponsSet
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/coupons/couponsSet.less';
</style>