<template>
  <div class="container">
    <!-- <div class="header">
      <span>店铺详情</span>
    </div> -->
    <el-form label-position="right" ref="ruleForm" label-width="auto" class="form-search" v-if="storeInfo">
      <div class="notice">
        <el-form-item class="members-head" :label="$t('stores.viewStore.dplg')" required>
          <img style="width: 100px;height: 100px;" :src="storeInfo.logo" alt="" @error="handleErrorImg">
        </el-form-item>
        <el-form-item class="members-head" :label="$t('stores.viewStore.dptx')" required>
          <img style="width: 100px;height: 100px;" :src="storeInfo.headimgurl" alt="" @error="handleErrorImg">
        </el-form-item>
        <el-form-item class="members-head" :label="$t('stores.viewStore.xct')">
          <img style="width: 100px;height: 100px;" :src="storeInfo.poster_img" alt="" @error="handleErrorImg">
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.dpmc')" required>
          <span>{{ storeInfo.name }}</span>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.zfjmh')" prop="" v-if="storeInfo.review_status == 1">
          <span>{{ storeInfo.roomid }}</span>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.dpxx')" prop="">
          <span>{{ storeInfo.shop_information }}</span>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.dpfl')" required>
          <span>{{ storeInfo.className }}</span>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.jyfw')" required>
          <span>{{ storeInfo.shop_range }}</span>
        </el-form-item>
        <!-- <el-form-item :label="$t('stores.viewStore.yhmc')" prop="">
          <span>：{{ storeInfo.user_name }}</span>
        </el-form-item> -->
        <el-form-item :label="$t('stores.viewStore.yyzt')" required>
          <span>{{ storeInfo.is_open == 0 ? $t('stores.viewStore.wyy') : storeInfo.is_open == 1 ? $t('stores.viewStore.yyz') : $t('stores.viewStore.ydy') }}</span>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.zsxm')" required>
          <span>{{ storeInfo.realname }}</span>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.sfzhm')" required>
          <span>{{ storeInfo.ID_number }}</span>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.lxdh')" required>
          <span>{{ storeInfo.tel }}</span>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.lxdz')" required>
          <span>{{ `${storeInfo.sheng}${storeInfo.shi}${storeInfo.xian}${storeInfo.address}` }}</span>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.ssxz')" prop="">
          <span>{{ storeInfo.shop_nature == 0 ? $t('stores.viewStore.gr') : $t('stores.viewStore.qy') }}</span>
        </el-form-item>
        <el-form-item class="business-license" :label="storeInfo.shop_nature == 0 ?$t('stores.viewStore.sfzj'):$t('stores.viewStore.yyzz')" required>
          <div v-for="(item,index) in storeInfo.business_license" :key="index" style="margin-right: 10px;">
            <el-image :src="item" alt="" style="width: 100px;height: 100px;" :preview-src-list="storeInfo.business_license" @error="handleErrorImg"></el-image>
          </div>
          <!-- <div class="comment-img" v-if="srcList && srcList.length">
            <viewer :images="storeInfo.business_license">
              <img v-for="src in srcList" :src="src" :key="src">
            </viewer>
          </div> -->
          <!-- <img :src="storeInfo.business_license"  style="width: 100px;height: 100px;"/> -->
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.shzt')" prop="">
          <span>{{ storeInfo.review_status == 0 ? $t('stores.viewStore.dsh') : storeInfo.review_status == 1 ? $t('stores.viewStore.shtg') : $t('stores.viewStore.shbtg') }}</span>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.jjly')" prop="" v-if="$route.query.status && $route.query.status == $t('stores.viewStore.shbth')">
          <span>{{ storeInfo.review_result }}</span>
        </el-form-item>
        <!-- <el-form-item class="footer-button">
          <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">返回</el-button>
        </el-form-item> -->
        <div style="min-height: 30px;width: 100%;"></div>
      </div>
      <!-- <div class="footer-button">
        <el-form-item>
          <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">返回</el-button>
        </el-form-item>
      </div> -->
	  </el-form>
  </div>
</template>

<script>
import viewStore from '@/webManage/js/plug_ins/stores/viewStore'
export default viewStore
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/stores/viewStore.less';
</style>