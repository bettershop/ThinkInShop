<template>
    <div class="container">
      <el-form
        ref="ruleForm"
        class="form-search"
        :rules="rules"
        :model="ruleForm"
        label-width="120px"
      >
        <div class="order-info">
          <div class="header">
            <span>{{ $t("plugInsSet.groupSetInfo.jcgn") }}</span>
          </div>
          <div class="order-block">
            <el-form-item :label="$t('plugInsSet.groupSetInfo.yhdrksz')">
              <el-switch
                v-model="ruleForm.isOpen"
                :active-value="1"
                :inactive-value="0"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              >
              </el-switch>
              <div class="tip">
                <p>{{ $t("preSale.preSaleSet.preSaleInfo.one") }}</p>
                <p>{{ $t("preSale.preSaleSet.preSaleInfo.two") }}</p>
                <p>{{ $t("preSale.preSaleSet.preSaleInfo.three") }}</p>
                <p>{{ $t("preSale.preSaleSet.preSaleInfo.four") }}</p>
              </div>
            </el-form-item>
          </div>
        </div>
        <div class="order-info">
          <div class="header">
            <span>{{ $t("preSale.preSaleSet.ddsz") }}</span>
          </div>
          <div class="order-block">
            <el-form-item
              class="package-mail"
              :label="$t('preSale.preSaleSet.djbysz')"
            >
              <el-switch
                v-model="ruleForm.package_mail"
                :active-value="1"
                :inactive-value="0"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              >
              </el-switch>
              <div class="package_set" v-show="ruleForm.package_mail == 1">
                <span class="red">*</span>
                <span>{{ $t("preSale.preSaleSet.xtsps") }}</span>
                <el-input
                  v-model="ruleForm.package_num"
                  @keyup.native="
                    ruleForm.package_num = oninput3(ruleForm.package_num)
                  "
                >
                </el-input>
                <span>{{ $t("preSale.preSaleSet.jby") }}</span>
              </div>
            </el-form-item>
            <div class="order-time">
              <el-form-item
                :label="$t('preSale.preSaleSet.zdshs')"
                prop="automatic_time"
              >
                <el-input
                  :placeholder="$t('preSale.preSaleSet.qsrzd')"
                  v-model="ruleForm.automatic_time"
                  @keyup.native="
                    ruleForm.automatic_time = oninput3(ruleForm.automatic_time)
                  "
                >
                  <template slot="append">{{
                    $t("preSale.preSaleSet.tian")
                  }}</template>
                </el-input>
                <span class="gray" style="color: #97a0b4; margin-left: 14px">{{
                  $t("preSale.preSaleSet.yhwsd")
                }}</span>
              </el-form-item>
              <el-form-item
                :label="$t('preSale.preSaleSet.ddsxs')"
                prop="failure_time"
              >
                <el-input
                  :placeholder="$t('preSale.preSaleSet.qsrdds')"
                  v-model="ruleForm.failure_time"
                  @keyup.native="
                    ruleForm.failure_time = oninput3(ruleForm.failure_time)
                  "
                >
                  <template slot="append">{{
                    $t("preSale.preSaleSet.xs")
                  }}</template>
                </el-input>
                <span class="gray" style="color: #97a0b4; margin-left: 14px">{{
                  $t("preSale.preSaleSet.yhxdw")
                }}</span>
              </el-form-item>
              <el-form-item
                :label="$t('preSale.preSaleSet.ddsgs')"
                prop="afterSales_time"
              >
                <el-input
                  :placeholder="$t('preSale.preSaleSet.qsrdd')"
                  v-model="ruleForm.afterSales_time"
                  @keyup.native="
                    ruleForm.afterSales_time = oninput2(ruleForm.afterSales_time)
                  "
                >
                  <template slot="append">{{
                    $t("preSale.preSaleSet.tian")
                  }}</template>
                </el-input>
                <span class="gray" style="color: #97a0b4; margin-left: 14px">{{
                  $t("preSale.preSaleSet.qrshs")
                }}</span>
              </el-form-item>
            </div>
            <div class="order-time">
              <el-form-item
                :label="$t('preSale.preSaleSet.xtxz')"
                prop="remind_hours"
              >
                <el-input
                  :placeholder="$t('preSale.preSaleSet.qsrdds')"
                  v-model="ruleForm.remind_hours"
                  @keyup.native="
                    ruleForm.remind_hours = oninput2(ruleForm.remind_hours)
                  "
                >
                  <template slot="append">{{
                    $t("preSale.preSaleSet.xs")
                  }}</template>
                </el-input>
                <!-- <el-input v-model="ruleForm.remind_day" @keyup.native="ruleForm.remind_day = oninput2(ruleForm.remind_day)">
                </el-input>
                <span>天</span>
                <el-input v-model="ruleForm.remind_hours" @keyup.native="ruleForm.remind_hours = oninput2(ruleForm.remind_hours)">
                </el-input>
                <span>小时</span> -->
                <span class="gray" style="color: #97a0b4; margin-left: 14px">{{
                  $t("preSale.preSaleSet.dzckf")
                }}</span>
              </el-form-item>
            </div>

            <!-- <el-form-item class="remind-limit" :label="自动评价设置" prop="auto_remind_day">
              <el-input v-model="ruleForm.auto_remind_day" @keyup.native="ruleForm.auto_remind_day = oninput2(ruleForm.auto_remind_day)">
              </el-input>
              <span>天自动好评</span>
            </el-form-item> -->
            <el-form-item :label="$t('preSale.preSaleSet.zdpjsz')">
              <el-switch @change="mychange($event)" v-model="isop" />
              <div class="djby" v-if="isop">
                <div class="row myrow">
                  {{ $t("preSale.preSaleSet.ddjyw") }}
                  <el-input
                    :placeholder="$t('preSale.preSaleSet.zdshs')"
                    v-model="ruleForm.auto_remind_day"
                    @keyup.native="
                      ruleForm.auto_remind_day = oninput2(
                        ruleForm.auto_remind_day
                      )
                    "
                    class="width"
                  >
                    <template slot="append">{{
                      $t("preSale.preSaleSet.tian")
                    }}</template> </el-input
                  ><span>{{ $t("preSale.preSaleSet.nyhwj") }}</span>
                  <!-- <div class="el-input"><input type="number" autocomplete="off" class="el-input__inner" v-model="mainData.remind_hour"/></div>小时 -->
                </div>
                <div class="row">
                  <span
                    style="display: inline-block; width: 70px; line-height: 32px"
                    >{{ $t("orderSet.hpnr") }}</span
                  >
                  <el-input
                    v-model="ruleForm.autoCommentContent"
                    :placeholder="$t('preSale.preSaleSet.hpnr')"
                    type="textarea"
                    class="width"
                  >
                  </el-input>
                </div>
              </div>
            </el-form-item>
          </div>
        </div>
        <div class="basic-info">
          <div class="header">
            <span>{{ $t("preSale.preSaleSet.gzsz") }}</span>
          </div>
          <div class="basic-block rules-blocks">
            <!-- <el-form-item :label="$t('preSale.preSaleSet.kqcj')">
              <el-switch
                v-model="ruleForm.isOpen"
                :active-value="1"
                :inactive-value="0"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              >
              </el-switch>
            </el-form-item> -->
            <el-form-item
              :label="$t('preSale.preSaleSet.ddyss')"
              prop="depositDesc"
            >
              <div class="rules-block">
                <vue-editor
                  v-model="ruleForm.depositDesc"
                  useCustomImageHandler
                  @image-added="handleImageAdded"
                ></vue-editor>
              </div>
            </el-form-item>
            <el-form-item
              :label="$t('preSale.preSaleSet.dhyss')"
              prop="balanceDesc"
            >
              <div class="rules-block">
                <vue-editor
                  v-model="ruleForm.balanceDesc"
                  useCustomImageHandler
                  @image-added="handleImageAdded"
                ></vue-editor>
              </div>
            </el-form-item>
          </div>
        </div>
        <div class="footer-button">
          <el-button plain class="footer-cancel fontColor" @click="back">{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          <el-button
            type="primary"
            class="footer-save bgColor mgleft"
            @click="submitForm('ruleForm')"
            >{{ $t("DemoPage.tableFromPage.save") }}</el-button
          >
        </div>
      </el-form>
    </div>
  </template>

  <script>
  import preSaleSetInfo from '@/webManage/js/plug_ins/plugInsSet/preSaleSetInfo'
  export default preSaleSetInfo
  </script>

  <style scoped lang="less">
  @import '../../../webManage/css/plug_ins/plugInsSet/preSaleSetInfo.less';
  /deep/.el-input-group__append{
    padding: 0 !important;
    width: 45px !important;
    text-align: center;
  }
  .container {
    width: 100%;
    height: 737px;
    .djby {
      width: 36.25rem;
      background: rgba(244, 247, 249, 1);
      border-radius: 4px;
      padding: 20px 20px 6px 20px;
      box-sizing: border-box;
      div {
        width: 100%;
      }
      .myrow {
        div {
          width: 7rem !important;
        }
      }
      .row {
        display: flex;
        // align-items: center;
        margin-bottom: 14px;
        .el-input {
          width: 150px;
          margin: 0 5px;
          .el-input__inner {
            border-color: #d5dbe8;
            color: #414658;
            font-size: 14px;
            padding: 0 10px;
          }
        }
      }
    }
    .tip{
      width: 580px;
      // height: 83px;
      background-color: #F4F7F9;
      font-size: 14px;
      color: #97A0B4;
      padding: 8px;
      p{
          display: flex;
          align-items: center;
          height: 22px;
          line-height: 1;
      }
    }
    .width {
      width: 570px;
    }
  }
  </style>
