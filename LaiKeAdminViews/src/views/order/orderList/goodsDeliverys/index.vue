<template>
  <div class="container">
    <div class="goods-info">
      <div class="header">
        <span>{{ $t("goodsDelivery.xzsp") }}</span>
      </div>
      <div class="goods-block">
        <div class="dictionary-list">
          <el-table
            :data="goodsTables"
            ref="table"
            class="el-table"
            @selection-change="handleSelectionChange"
            style="width: 100%"
          >
            <el-table-column type="selection" width="55"> </el-table-column>
            <el-table-column prop="p_name" :label="$t('goodsDelivery.spmc')">
            </el-table-column>
            <el-table-column prop="" :label="$t('goodsDelivery.sptp')">
              <template slot-scope="scope">
                <img :src="scope.row.imgurl" @error="handleErrorImg" alt="" />
              </template>
            </el-table-column>
            <el-table-column prop="deliverNum" :label="$t('goodsDelivery.sl')">
            </el-table-column>
            <el-table-column prop="p_price" :label="$t('goodsDelivery.spjg')">
              <template slot-scope="scope">
                <span>{{scope.row.currency_symbol}}{{ LaiKeCommon.formatPrice(scope.row.p_price,scope.row.exchange_rate) }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="size"
              :label="$t('goodsDelivery.gg')"
              width="200"
            >
            </el-table-column>
            <el-table-column prop="brand_name" :label="$t('goodsDelivery.pp')">
            </el-table-column>
            <el-table-column
              prop="mynum"
              :label="$t('goodsDelivery.fhsl')"
              key="mynum"
              width="200"
            >
              <template slot-scope="scope">
                <el-input-number
                  v-model="mynum[scope.$index]"
                  @change="handleChange(scope.$index)"
                  :min="1"
                  :max="scope.row.deliverNum"
                ></el-input-number>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
    <div class="footer-button">
      <div class="info">
        <!-- goodsNum : js赋值错误 -->
        {{ $t("goodsDelivery.gong") }}<span>{{ goodsTables.length }}</span
        >{{ $t("goodsDelivery.jsp") }}
      </div>
      <el-button
        plain
        class="footer-cancel fontColor"
        @click="$router.go(-1)"
        >{{ $t("goodsDelivery.ccel") }}</el-button
      >
      <el-button
        type="primary"
        class="footer-save bgColor mgleft"
        @click="dialogShow"
        >{{ $t("goodsDelivery.fh") }}</el-button
      >
    </div>

    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('goodsDelivery.fh')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <el-form
          :model="ruleForm2"
          :rules="rules2"
          ref="ruleForm2"
          label-width="auto"
          class="demo-ruleForm"
        >
          <el-form-item :label="$t('goodsDelivery.kdgs')" prop="kuaidi_name">
            <el-select
              class="select-input"
              v-model="ruleForm2.kuaidi_name"
              :placeholder="$t('goodsDelivery.qxzkdgs')"
              @change="handleSelecChange"
            >
              <el-option
                v-for="item in courierList"
                :key="item.id"
                :label="item.kuaidi_name"
                :value="item.id"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('goodsDelivery.kddh')" prop="kuaidi_no" v-if="!logistics_type">
            <el-input
              v-model="ruleForm2.kuaidi_no"
              @keyup.native="
                ruleForm2.kuaidi_no = oninput3(ruleForm2.kuaidi_no)
              "
            ></el-input>
          </el-form-item>
          <!-- 提示 -->
          <div class="tishi">
            <p class="title">
              <img src="@/assets/imgs/czts.png" alt="" />
              <span class="text"> 温馨提示</span>
            </p>
            <div class="tishi_content">
              <div>（1）发货时，选择的物流公司如暂未开通电子面单功能，只能人工填写;</div>
              <div>（2）已开通电子面单的物流公司，支持线上打印面单功能;</div>
            </div>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button @click="handleClose">{{
                $t("goodsDelivery.ccel")
              }}</el-button>
              <el-button type="primary" @click="determine('ruleForm2')">{{
                $t("goodsDelivery.okk")
              }}</el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import goodsDelivery from "@/webManage/js/order/orderList/goodsDelivery";
export default goodsDelivery;
</script>

<style scoped lang="less">
@import "../../../../webManage/css/order/orderList/goodsDelivery.less";
</style>
