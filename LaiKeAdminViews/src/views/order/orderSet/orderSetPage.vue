<template>
  <div class="container">
    <div class="content">
    <div class="formCenter">
      <el-form :model="mainData" class="picture-ruleForm" label-width="auto">
        <el-form-item :label="$t('orderSet.djbysz')">
          <el-switch v-model="mainData.isMany" />
          <div class="djby width" v-if="mainData.isMany">
            <div class="row">
              {{ $t("orderSet.tjsp") }}
              <div class="el-input">
                <input
                  type="number"
                  @blur="blurPiece"
                  autocomplete="off"
                  @keyup.native="
                    mainData.same_piece = oninput2(mainData.same_piece)
                  "
                  class="el-input__inner"
                  v-model="mainData.same_piece"
                />
              </div>
              {{ $t("orderSet.jby") }}
            </div>
            <div class="row">
              {{ $t("orderSet.tydd") }}
              <div class="el-input">
                <input
                  type="number"
                  @blur="blurOrder"
                  autocomplete="off"
                  class="el-input__inner"
                  @keyup.native="
                    mainData.same_order = oninput2(mainData.same_order)
                  "
                  v-model="mainData.same_order"
                />
              </div>
              {{ $t("orderSet.jby") }}
            </div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('orderSet.qrshsz')">
          <div class="djby width">
            <div class="row myrow">
              {{ $t("orderSet.ddfh") }}
              <el-input
                :placeholder="$t('orderSet.zdsh')"
                v-model="mainData.auto_the_goods"
                @keyup.native="
                  mainData.auto_the_goods = oninput2(mainData.auto_the_goods)
                "
                class="width"
              >
                <template slot="append">{{
                  $t("orderSet.day")
                }}</template> </el-input
              ><span>{{ $t("orderSet.nzdsh") }}</span>
              <!-- <div class="el-input"><input type="number" autocomplete="off" class="el-input__inner" v-model="mainData.remind_hour"/></div>小时 -->
            </div>
          </div>
        </el-form-item>
        <!-- <el-form-item :label="自动收货时间">
          <el-input placeholder="自动收货时间" v-model="mainData.auto_the_goods" @keyup.native="mainData.auto_the_goods = oninput2(mainData.auto_the_goods)" class="width">
            <template slot="append">天</template>
          </el-input>
        </el-form-item> -->
        <el-form-item :label="$t('orderSet.ddsx')">
          <el-input
            :placeholder="$t('orderSet.ddss')"
            v-model="mainData.order_failure"
            @keyup.native="
              mainData.order_failure = oninput2(mainData.order_failure)
            "
            class="width"
          >
            <template slot="append">{{ $t("orderSet.hour") }}</template>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('orderSet.ddsh')">
          <el-input
            :placeholder="$t('orderSet.ddsh')"
            @keyup.native="
              mainData.order_after = oninput2(mainData.order_after)
            "
            v-model="mainData.order_after"
            class="width"
          >
            <template slot="append">{{ $t("orderSet.day") }}</template>
          </el-input>
        </el-form-item>

        <!-- <el-form-item label="购物赠积分比例">
          <el-switch @change="myProportion($event)" v-model="isProportion" />
          <div v-if="isProportion">
            <div class="row myrow">
              <el-input
                :placeholder="$t('orderSet.zdsh')"
                v-model="mainData.proportion"
                @keyup.native="
                  mainData.proportion = oninput2(mainData.proportion)
                "
                class="width2"
              >
                <template slot="append">%</template>
              </el-input
              ><span style="margin-left: 14px;">购物赠送积分=交易金额*赠送比例</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item class="select-time" label="积分发放时间">
          <el-radio-group v-model="mainData.giveStatus">
            <el-radio
              v-for="item in issueTimeList"
              :label="item.value"
              :key="item.value"
              >{{ item.name }}</el-radio
            >
          </el-radio-group>
          <div class="tips">
            <div class="items1" style="display: flex">
              <el-form-item
                class="integral-proportion"
                prop="amsTime"
                v-if="mainData.giveStatus == 0"
              >
                <el-input
                  style="width: 400px"
                  placeholder=""
                  v-model="mainData.amsTime"
                  @keyup.native="mainData.amsTime = oninput4(mainData.amsTime)"
                >
                  <el-button slot="append">天</el-button>
                </el-input>
              </el-form-item>
              <span style="margin-left: 10px" v-if="mainData.giveStatus == 0"
                >后返积分</span
              ><span class="gray grays">(申请售后成功后，积分会回滚)</span>
            </div>

            <div class="gray items1">
              (付款后返回积分，申请售后成功后是不会退还积分的)
            </div>
          </div>
        </el-form-item> -->

        <el-form-item :label="$t('orderSet.txxz')">
          <div class="djby width">
            <el-input
              :placeholder="$t('orderSet.ddss')"
              v-model="mainData.remind_hour"
              @keyup.native="
                mainData.remind_hour = oninput2(mainData.remind_hour)
              "
              class="width"
            >
              <template slot="append">{{ $t("orderSet.hour") }}</template>
            </el-input>
            <span style="color: #97a0b4">{{ $t("orderSet.dzckts") }}</span>
          </div>
        </el-form-item>
        <el-form-item :label="$t('orderSet.zdpj')">
          <el-switch @change="mychange($event)" v-model="isop" />
          <div class="djby width" v-if="isop">
            <div class="row myrow">
              {{ $t("orderSet.ddjy") }}
              <el-input
                :placeholder="$t('orderSet.zdsh')"
                v-model="mainData.auto_good_comment_day"
                @keyup.native="
                  mainData.auto_good_comment_day = oninput2(
                    mainData.auto_good_comment_day
                  )
                "
                class="width"
              >
                <template slot="append">{{
                  $t("orderSet.day")
                }}</template> </el-input
              ><span>{{ $t("orderSet.nzdhp") }}</span>
              <!-- <div class="el-input"><input type="number" autocomplete="off" class="el-input__inner" v-model="mainData.remind_hour"/></div>小时 -->
            </div>
            <div class="row">
              <span
                style="display: inline-block; width: 70px; line-height: 32px"
                >{{ $t("orderSet.hpnr") }}</span
              >
              <el-input
                v-model="mainData.autoCommentContent"
                :placeholder="$t('orderSet.hpnr')"
                type="textarea"
                class="width"
              >
              </el-input>
            </div>
          </div>
        </el-form-item>
        <!-- <el-form-item :label="">
          <el-input placeholder="自动评价设置" @keyup.native="mainData.auto_good_comment_day = oninput2(mainData.auto_good_comment_day)" v-model="mainData.auto_good_comment_day" class="width">
            <template slot="append">天</template>
          </el-input>
        </el-form-item> -->

        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="Save()"
              >{{ $t("DemoPage.tableFromPage.save") }}
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <div class="hr" v-if="mainData.isMany&&isop"></div>
  </div>
  </div>
</template>

<script>
import main from "@/webManage/js/order/orderSet/orderSetPage";

export default main;
</script>

<style scoped lang="less">
@import "../../../common/commonStyle/form";
.picture-ruleForm{
  margin-top:30px;
}
.container {
  width: 100%;
  height: 737px;
  background-color: #fff;
  .djby {
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

  /deep/.select-time {
    .el-form-item__content {
      display: flex;
      .integral-proportion {
        .el-input {
          width: 400px;
        }
      }
      .tips {
        height: 88px;
        .items1 {
          height: 50%;
          display: flex;
          align-items: center;
        }
      }
      .el-radio-group {
        height: 88px;
        display: flex;
        flex-direction: column;
        justify-content: space-evenly;
        .el-radio {
          height: 50%;
          display: flex;
          align-items: center;
        }
      }
    }

    .gray {
      color: #97A0B4;
    }

    .grays {
      margin-left: 10px;
    }
  }

  .width {
    width: 39.625rem ;
  }

  .width2 {
    width: 400px;
  }

  .formCenter {
    justify-content: center;
    flex-direction: column;
    padding-bottom: 0px !important;

  }

  .hr {
    width: 100%;
    height: 3px;
    background-color: #edf1f5;
  }

  .content {
    width: 100%;
  }
}
</style>
