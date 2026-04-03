<template>
  <div class="container">
    <el-form
      ref="ruleForm"
      class="form-search"
      :rules="rules"
      :model="ruleForm"
      label-width="150px"
    >
      <div class="basic-info">
        <div class="header">
          <span>{{ $t("integralMall.mallSet.jcsz") }}</span>
        </div>
        <div class="basic-block">
          <!-- <el-form-item :label="$t('integralMall.mallSet.kcqc')">
              <el-switch v-model="ruleForm.isOpen" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
              </el-switch>
                </el-form-item> -->
          <!-- <el-form-item :label="轮播图">
              <l-upload
                :limit="1"
                v-model="ruleForm.slideshow"
                text="（建议上传750*300px尺寸的图片）"
              >
              </l-upload>
            </el-form-item> -->
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
              <p>{{ $t("integralMall.mallSet.mallSetInfo.one") }}</p>
              <p>{{ $t("integralMall.mallSet.mallSetInfo.two") }}</p>
              <p>{{ $t("integralMall.mallSet.mallSetInfo.three") }}</p>
              <p>{{ $t("integralMall.mallSet.mallSetInfo.four") }}</p>
              <p>{{ $t("integralMall.mallSet.mallSetInfo.seven") }}</p>
              <p>{{ $t("integralMall.mallSet.mallSetInfo.five") }}</p>
              <p>{{ $t("integralMall.mallSet.mallSetInfo.six") }}</p>
            </div>
          </el-form-item>
          <el-form-item
            class="integral-proportion"
            :label="$t('integralMall.mallSet.gezs')"
            prop="integral_proportion"
          >
            <el-input
              :placeholder="$t('integralMall.mallSet.qsrgwzs')"
              v-model="ruleForm.integral_proportion"
              @keyup.native="
                ruleForm.integral_proportion = oninput2(
                  ruleForm.integral_proportion
                )
              "
            >
              <el-button slot="append">%</el-button>
            </el-input>
          </el-form-item>

         <el-form-item
           class="package-mail jf"
           :label="$t('integralMall.mallSet.jfbl')"
           prop = 'integralNumber'
         >
           <div class="package_set" >
             <el-input
               :placeholder="$t('integralMall.mallSet.jsrjfbl')"
               v-model="ruleForm.integralNumber"
               @keyup.native="
                 ruleForm.integralNumber = oninput2(
                   ruleForm.integralNumber
                 )
               "
             >
               <el-button slot="append">{{$t('integralMall.mallSet.jf')}}</el-button>
             </el-input>
             ：
            <el-input
              :placeholder="$t('integralMall.mallSet.jsrjebl')"
              v-model="ruleForm.moneyNumber"
              @keyup.native="
                ruleForm.moneyNumber = oninput2(
                  ruleForm.moneyNumber
                )
              "
            >
             <el-select v-model="moneyUnit" slot="append" placeholder="请选择" >
                  <el-option v-for="item in sortList" :key="item.value" :label="item.label" :value="item.value">
                  </el-option>
                </el-select>
            </el-input>
           </div>
         </el-form-item>

          <el-form-item :label="$t('integralMall.mallSet.jfff')">
            <el-radio-group
              v-model="ruleForm.issue_time"
              style="float: left"
              @change="agreeChange"
            >
              <el-radio
                class="myradio"
                style=""
                v-for="item in issueTimeList"
                :label="item.value"
                :key="item.value"
                >{{ item.name }}</el-radio
              >
            </el-radio-group>
            <div style="float: left">
              <div style="display: flex">
                <el-form-item
                  class="integral-proportion"
                  prop="amsTime"
                  v-if="ruleForm.issue_time == 0"
                >
                  <el-input
                    style="width: 300px"
                    placeholder=""
                    v-model="ruleForm.amsTime"
                    @keyup.native="
                      ruleForm.amsTime = oninput3(ruleForm.amsTime)
                    "
                  >
                    <el-button slot="append">{{
                      $t("integralMall.mallSet.day")
                    }}</el-button>
                  </el-input>
                </el-form-item>
                <span style="margin: 0 8px" v-if="ruleForm.issue_time == 0">{{
                  $t("integralMall.mallSet.hfjf")
                }}</span
                ><span class="gray">{{ $t("integralMall.mallSet.sqsf") }}</span>
              </div>

              <div class="gray">
                {{ $t("integralMall.mallSet.fkhf") }}
              </div>
            </div>
          </el-form-item>
          <el-form-item
            class="overdue-set"
            :label="$t('integralMall.mallSet.jfgq')"
            prop="overdue_set"
          >
            <!-- <span class="gray">{{ $t('integralMall.mallSet.jfyxsj') }}</span> -->
            <el-input
              class="inputs"
              :placeholder="$t('integralMall.mallSet.qsrjf')"
              v-model="ruleForm.overdue_set"
              @keyup.native="
                ruleForm.overdue_set = oninput3(ruleForm.overdue_set)
              "
            >
              <el-button slot="append">{{
                $t("integralMall.mallSet.day")
              }}</el-button>
            </el-input>
            <span class="gray">{{ $t("integralMall.mallSet.hsx") }}</span>
            <span class="gray2">
              {{ $t("integralMall.mallSet.jfgqszts") }}
            </span>
          </el-form-item>
        </div>
      </div>
      <div class="order-info">
        <div class="header">
          <span>{{ $t("integralMall.mallSet.ddsz") }}</span>
        </div>
        <div class="order-block">
          <el-form-item
            class="package-mail"
            :label="$t('integralMall.mallSet.djby')"
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
              <span>{{ $t("integralMall.mallSet.xtsp") }}</span>
              <el-input
                v-model="ruleForm.package_num"
                @keyup.native="
                  ruleForm.package_num = oninput2(ruleForm.package_num)
                "
              >
              </el-input>
              <span>{{ $t("integralMall.mallSet.jby") }}</span>
            </div>
          </el-form-item>
          <div class="order-time" style="display: block">
            <el-form-item
              :label="$t('integralMall.mallSet.zdsh')"
              prop="automatic_time"
              class="remind-limit"
            >
              <el-input
                :placeholder="$t('integralMall.mallSet.qsrzdsh')"
                v-model="ruleForm.automatic_time"
                @keyup.native="
                  ruleForm.automatic_time = oninput2(ruleForm.automatic_time)
                "
              >
                <template slot="append">{{
                  $t("integralMall.mallSet.day")
                }}</template>
              </el-input>
              <span class="gray">{{
                $t("integralMall.mallSet.zdshsjts")
              }}</span>
            </el-form-item>
            <el-form-item
              :label="$t('integralMall.mallSet.ddsx')"
              prop="failure_time"
              class="remind-limit"
            >
              <el-input
                :placeholder="$t('integralMall.mallSet.qsrddsx')"
                v-model="ruleForm.failure_time"
                @keyup.native="
                  ruleForm.failure_time = oninput2(ruleForm.failure_time)
                "
              >
                <template slot="append">{{
                  $t("integralMall.mallSet.hour")
                }}</template>
              </el-input>
              <span class="gray">{{
                $t("integralMall.mallSet.ddsxsjts")
              }}</span>
            </el-form-item>
            <el-form-item
              :label="$t('integralMall.mallSet.ddsh')"
              prop="afterSales_time"
              class="remind-limit"
            >
              <el-input
                :placeholder="$t('integralMall.mallSet.qsrddsh')"
                v-model="ruleForm.afterSales_time"
                @keyup.native="
                  ruleForm.afterSales_time = oninput2(ruleForm.afterSales_time)
                "
              >
                <template slot="append">{{
                  $t("integralMall.mallSet.day")
                }}</template>
              </el-input>
              <span class="gray"
                >{{ $t("integralMall.mallSet.ddshsjts") }}
                {{ ruleForm.afterSales_time }}
                {{ $t("integralMall.mallSet.ddshsjts1") }}
              </span>
            </el-form-item>
          </div>
          <el-form-item
            class="remind-limit"
            :label="$t('integralMall.mallSet.txxz')"
            prop="remind_hours"
          >
            <!-- <el-input v-model="ruleForm.remind_day" @keyup.native="ruleForm.remind_day = oninput2(ruleForm.remind_day)">
              </el-input>
              <span>天</span>
              <el-input v-model="ruleForm.remind_hours" @keyup.native="ruleForm.remind_hours = oninput2(ruleForm.remind_hours)">
              </el-input>
              <span>小时</span> -->
            <el-input
              :placeholder="$t('integralMall.mallSet.qsrtx')"
              v-model="ruleForm.remind_hours"
              @keyup.native="
                ruleForm.remind_hours = oninput2(ruleForm.remind_hours)
              "
            >
              <template slot="append">{{
                $t("integralMall.mallSet.hour")
              }}</template>
            </el-input>
            <span class="gray">{{ $t("integralMall.mallSet.dzck") }}</span>
          </el-form-item>
          <el-form-item :label="$t('integralMall.mallSet.zdpj')">
            <el-switch @change="mychange($event)" v-model="isop" />
            <div class="djby" v-if="isop">
              <div class="row myrow">
                {{ $t("integralMall.mallSet.ddjy") }}
                <el-input
                  :placeholder="$t('integralMall.mallSet.zdsh')"
                  v-model="ruleForm.auto_remind_day"
                  @keyup.native="
                    ruleForm.auto_remind_day = oninput2(
                      ruleForm.auto_remind_day
                    )
                  "
                  class="width"
                >
                  <template slot="append">{{
                    $t("integralMall.mallSet.day")
                  }}</template> </el-input
                ><span>{{ $t("integralMall.mallSet.nyhw") }}</span>
                <!-- <div class="el-input"><input type="number" autocomplete="off" class="el-input__inner" v-model="mainData.remind_hour"/></div>小时 -->
              </div>
              <div class="row">
                <span
                  style="display: inline-block; width: 70px; line-height: 32px"
                  >{{ $t("orderSet.hpnr") }}</span
                >
                <el-input
                  v-model="ruleForm.autoCommentContent"
                  :placeholder="$t('integralMall.mallSet.hpnr')"
                  type="textarea"
                  class="width"
                >
                </el-input>
              </div>
            </div>
          </el-form-item>
          <!-- <el-form-item class="remind-limit" :label="评价设置" prop="auto_remind_day">

              <el-input placeholder="请输入订单售后时间" v-model="ruleForm.auto_remind_day" @keyup.native="ruleForm.auto_remind_day = oninput2(ruleForm.auto_remind_day)">
                  <template slot="append">天</template>
                </el-input>
              <span>自动好评</span>
            </el-form-item>
            <el-form-item class="remind-textarea" :label="好评内容设置" prop="autoCommentContent">
                <el-input type="textarea" placeholder="请输入好评内容" v-model="ruleForm.autoCommentContent" >
                </el-input>
              </el-form-item> -->
        </div>
      </div>
      <div class="rules-set">
        <div class="header">
          <span>{{ $t("integralMall.mallSet.gzsz") }}</span>
        </div>

        <el-form
          :model="ruleForm"
          label-position="right"
          :rules="rules"
          ref="ruleForm"
          label-width="164px"
          class="shaco_form"
        >
          <el-form-item :label="$t('plugInsSet.groupSetInfo.gznr')">
            <vue-editor
              v-model="ruleForm.content"
              useCustomImageHandler
              @image-added="handleImageAdded"
            ></vue-editor>
          </el-form-item>
        </el-form>
      </div>
      <div style="height: 75px"></div>
      <div class="footer-button">
        <el-button plain class="footer-cancel fontColor" @click="back">{{
          $t("DemoPage.tableFromPage.cancel")
        }}</el-button>
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
import integralSetInfo from "@/webManage/js/plug_ins/plugInsSet/integralSetInfo";
export default integralSetInfo;
</script>

<style scoped lang="less">
@import "../../../webManage/css/plug_ins/plugInsSet/integralSetInfo.less";

.container {
  width: 100%;
  height: 737px;
  .jf{
    .el-input{
      width: 300px;
    }
      /deep/ .el-select .el-input {
        width: 100px;
      }

  }
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

  .width {
    width: 570px;
  }
}
</style>
