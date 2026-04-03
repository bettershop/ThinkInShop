<template>
  <div class="container">
    <el-form
      ref="ruleForm"
      class="form-search"
      :rules="rules"
      :model="ruleForm"
      label-width="120px"
    >
      <div class="basic-info">
        <div class="header">
          <span>{{ $t("seckill.seckillSet.jscs") }}</span>
        </div>
        <div class="basic-block">
          <!-- <el-form-item :label="$t('seckill.seckillSet.kqcj')">
            <el-switch
              v-model="ruleForm.isOpen"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
              @change="turnOnOff()"
            >
            </el-switch>
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
              <p>{{ $t("seckill.seckillSet.seckillInfo.one") }}</p>
              <p>{{ $t("seckill.seckillSet.seckillInfo.two") }}</p>
              <p>{{ $t("seckill.seckillSet.seckillInfo.three") }}</p>
              <p>{{ $t("seckill.seckillSet.seckillInfo.four") }}</p>
              <p>{{ $t("seckill.seckillSet.seckillInfo.five") }}</p>
            </div>
          </el-form-item>
          <el-form-item
            class="limit-input"
            :label="$t('seckill.seckillSet.mrxgsl')"
            prop="limit_num"
          >
            <el-input
              :placeholder="$t('seckill.seckillSet.qsrmrxg')"
              v-model="ruleForm.limit_num"
              @keyup.native="ruleForm.limit_num = oninput2(ruleForm.limit_num)"
            >
              <el-button slot="append">{{
                $t("seckill.seckillSet.ge")
              }}</el-button>
            </el-input>
          </el-form-item>
          <el-form-item class="trailen" :label="$t('seckill.seckillSet.ygsj')">
            <div class="isOpen-trailen">
              <el-switch
                v-model="ruleForm.set_trailer"
                :active-value="1"
                :inactive-value="0"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              >
              </el-switch>
              <span class="gray">{{ $t("seckill.seckillSet.msspz") }}</span>
            </div>
            <div class="trailen-time" v-show="ruleForm.set_trailer == 1">
              <span class="red">*</span>
              <span>{{ $t("seckill.seckillSet.ygsj") }}</span>
              <el-input
                :placeholder="$t('seckill.seckillSet.qsryg')"
                v-model="ruleForm.trailen_time"
                @keyup.native="
                  ruleForm.trailen_time = oninput2(ruleForm.trailen_time)
                "
              >
                <template slot="append">{{
                  $t("seckill.seckillSet.hour")
                }}</template>
              </el-input>
            </div>
          </el-form-item>

          <!-- <el-form-item class="activity-push" :label="活动消息推送" prop="activity_push">
              <span>秒杀活动开始前</span>
              <el-input v-model="ruleForm.activity_push" @keyup.native="ruleForm.activity_push = oninput2(ruleForm.activity_push)"></el-input>
              <span>分钟提醒</span>
            </el-form-item> -->
          <el-form-item
            class="trailen"
            :label="$t('seckill.seckillSet.xxtssz')"
          >
            <div class="isOpen-trailen" style="height: 40px">
              <el-switch
                v-model="ruleForm.set_trailer2"
                :active-value="1"
                :inactive-value="0"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              >
              </el-switch>
            </div>
            <div
              class="trailen-time trailen-time-info"
              v-show="ruleForm.set_trailer2 == 1"
            >
              <div
                class="add-info"
                v-for="(item, index) in attributeList"
                :key="index"
              >
                <span>{{ $t("seckill.seckillSet.mshdk") }}</span>
                <el-input
                  placeholder=""
                  v-model="attributeList[index].value"
                  @keyup.native="
                    attributeList[index].value = oninput2(attributeList[index].value)
                  "
                >
                </el-input>
                <span style="margin-left: 10px">{{
                  $t("seckill.seckillSet.fzts")
                }}</span>
                <div class="add-reduction">
                  <i
                    class="el-icon-remove-outline"
                    @click="minus(index)"
                    v-if="attributeList.length !== 1"
                  ></i>
                  <i
                    class="el-icon-circle-plus-outline"
                    @click="addOne"
                    v-show="index === attributeList.length - 1"
                  ></i>
                </div>
              </div>
            </div>
          </el-form-item>
        </div>
      </div>
      <div class="order-info">
        <div class="header">
          <span>{{ $t("seckill.seckillSet.ddsz") }}</span>
        </div>
        <div class="order-block">
          <el-form-item
            class="package-mail"
            :label="$t('seckill.seckillSet.djbysz')"
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
              <span>{{ $t("seckill.seckillSet.xtsp") }}</span>
              <el-input
                v-model="ruleForm.package_num"
                @keyup.native="
                  ruleForm.package_num = oninput2(ruleForm.package_num)
                "
              >
              </el-input>
              <span>{{ $t("seckill.seckillSet.jby") }}</span>
            </div>
          </el-form-item>
          <div class="order-time">
            <!-- <el-form-item
              :label="$t('seckill.seckillSet.zdsh')"
              prop="automatic_time"
            >
              <el-input
                :placeholder="$t('seckill.seckillSet.qsrzd')"
                v-model="ruleForm.automatic_time"
                @keyup.native="
                  ruleForm.automatic_time = oninput2(ruleForm.automatic_time)
                "
              >
                <template slot="append">{{
                  $t("seckill.seckillSet.day")
                }}</template>
              </el-input>
              
            </el-form-item> -->
            <!-- <el-form-item
              :label="$t('seckill.seckillSet.ddsx')"
              prop="failure_time"
            >
              <el-input
                :placeholder="$t('seckill.seckillSet.qsrddsx')"
                v-model="ruleForm.failure_time"
                @keyup.native="
                  ruleForm.failure_time = oninput2(ruleForm.failure_time)
                "
              >
                <template slot="append">{{
                  $t('seckill.seckillSet.hour')
                }}</template>
              </el-input>
            </el-form-item>
            <el-form-item
              :label="$t('seckill.seckillSet.ddsh')"
              prop="afterSales_time"
            >
              <el-input
                :placeholder="$t('seckill.seckillSet.qsrddsh')"
                v-model="ruleForm.afterSales_time"
                @keyup.native="
                  ruleForm.afterSales_time = oninput2(ruleForm.afterSales_time)
                "
              >
                <template slot="append">{{
                  $t('seckill.seckillSet.day')
                }}</template>
              </el-input>
            </el-form-item> -->
          </div>
          <!-- <el-form-item class="remind-limit" :label="提醒限制" prop="remind_day">
              <el-input v-model="ruleForm.remind_day" @keyup.native="ruleForm.remind_day = oninput2(ruleForm.remind_day)">
              </el-input>
              <span>天</span>
              <el-input v-model="ruleForm.remind_hours" @keyup.native="ruleForm.remind_hours = oninput2(ruleForm.remind_hours)">
              </el-input>
              <span>小时</span>
              <span class="gray">店主查看发货提醒后，买家多久后能再次提醒。0表示只能提醒一次</span>
            </el-form-item> -->
          <el-form-item
            :label="$t('seckill.seckillSet.zdsh')"
            prop="automatic_time"
            class="remind-limit"
          >
            <el-input
              :placeholder="$t('seckill.seckillSet.qsrzd')"
              v-model="ruleForm.automatic_time"
              style="width: 400px"
              @keyup.native="
                ruleForm.automatic_time = oninput2(ruleForm.automatic_time)
              "
            >
              <template slot="append">{{
                $t("seckill.seckillSet.day")
              }}</template>
            </el-input>
            <span class="gray">{{ $t("seckill.seckillSet.zdshts") }}</span>
          </el-form-item>
          <el-form-item
            :label="$t('seckill.seckillSet.ddsx')"
            prop="failure_time"
            class="remind-limit"
          >
            <el-input
              :placeholder="$t('seckill.seckillSet.qsrddsx')"
              v-model="ruleForm.failure_time"
              style="width: 400px"
              @keyup.native="
                ruleForm.failure_time = oninput2(ruleForm.failure_time)
              "
            >
              <template slot="append">{{
                $t("seckill.seckillSet.hour")
              }}</template>
            </el-input>
            <span class="gray">{{ $t("seckill.seckillSet.ddsxsjts") }}</span>
          </el-form-item>
          <el-form-item
            class="remind-limit"
            :label="$t('seckill.seckillSet.ddsh')"
            prop="afterSales_time"
          >
            <el-input
              :placeholder="$t('seckill.seckillSet.qsrddsh')"
              v-model="ruleForm.afterSales_time"
              style="width: 400px"
              @keyup.native="
                ruleForm.afterSales_time = oninput2(ruleForm.afterSales_time)
              "
            >
              <template slot="append">{{
                $t("seckill.seckillSet.day")
              }}</template>
            </el-input>
            <span class="gray">{{ $t("seckill.seckillSet.ddshsjts") }}</span>
          </el-form-item>
          <el-form-item
            class="remind-limit"
            :label="$t('seckill.seckillSet.txxz')"
            prop="remind_hours"
          >
            <el-input
              v-model="ruleForm.remind_hours"
              style="width: 400px"
              @keyup.native="
                ruleForm.remind_hours = oninput2(ruleForm.remind_hours)
              "
            >
              <template slot="append">{{
                $t("seckill.seckillSet.hour")
              }}</template>
            </el-input>
            <!-- <span>{{$t('seckill.seckillSet.hour')}}</span> -->
            <span class="gray">{{ $t("seckill.seckillSet.dzck") }}</span>
          </el-form-item>
          <!-- <el-form-item class="remind-limit" :label="评价设置" prop="auto_remind_day">
              <el-input v-model="ruleForm.auto_remind_day" @keyup.native="ruleForm.auto_remind_day = oninput2(ruleForm.auto_remind_day)">
              </el-input>
              <span>天自动好评</span>
            </el-form-item> -->
          <el-form-item
            class="trailen"
            :label="$t('seckill.seckillSet.zdpjsz')"
          >
            <div class="isOpen-trailen">
              <el-switch
                v-model="ruleForm.set_trailer3"
                :active-value="1"
                :inactive-value="0"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              >
              </el-switch>
            </div>
            <div class="trailen-time" v-show="ruleForm.set_trailer3 == 1">
              <span>{{ $t("seckill.seckillSet.ddjywc") }}</span>
              <el-input
                placeholder=""
                v-model="ruleForm.auto_remind_day"
                @keyup.native="
                  ruleForm.auto_remind_day = oninput2(ruleForm.auto_remind_day)
                "
              >
                <template slot="append">{{
                  $t("seckill.seckillSet.day")
                }}</template>
              </el-input>
              <span style="margin-left: 10px">{{
                $t("seckill.seckillSet.nyhw")
              }}</span>
            </div>
            <div class="trailen-time" v-show="ruleForm.set_trailer3 == 1">
              <span>{{ $t("seckill.seckillSet.hpnr") }}</span>
              <el-input
                type="textarea"
                class="content-goods"
                :placeholder="$t('seckill.seckillSet.yhwjs')"
                v-model="ruleForm.autoCommentContent"
              >
              </el-input>
            </div>
          </el-form-item>
        </div>
      </div>
      <div class="rules-set">
        <div class="header">
          <span>{{ $t("seckill.seckillSet.gzsz") }}</span>
        </div>
          <el-form
            :model="ruleForm"
            label-position="right"
            :rules="rules"
            ref="ruleForm"
            label-width="137px"
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
      <div style="height: 78px"></div>
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
import seckillSetInfo from "@/webManage/js/plug_ins/plugInsSet/seckillSetInfo";
export default seckillSetInfo;
</script>

<style scoped lang="less">
@import "../../../webManage/css/plug_ins/plugInsSet/seckillSetInfo.less";
</style>
