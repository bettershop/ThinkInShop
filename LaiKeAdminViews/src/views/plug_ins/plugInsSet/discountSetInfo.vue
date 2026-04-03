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
          <span>{{ $t("distribution.distributionSet.jcsz") }}</span>
        </div>
        <div class="basic-block">
          <el-form-item :label="$t('plugInsSet.groupSetInfo.yhdrksz')">
            <el-switch
              v-model="ruleForm.is_open"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
            <div class="tip">
              <p>{{ $t('plugInsSet.discountInfo.one') }}</p>
              <p>{{ $t('plugInsSet.discountInfo.two') }}</p>
              <p>{{ $t('plugInsSet.discountInfo.three') }}</p>
            </div>
          </el-form-item>
        </div>
      </div>
      <div class="basic-info">
        <div class="header">
          <span>{{ $t('plugInsSet.groupSetInfo.ddsz') }}</span>
        </div>
        <div class="basic-block">
          <el-form-item :label="$t('plugInsSet.groupSetInfo.zdshsj')" prop="auto_the_goods" >
            <span style="margin-right: 10px">{{  $t('plugInsSet.groupSetInfo.text1')}}</span>
            <el-input
              v-on:input="
                ruleForm.auto_the_goods = ruleForm.auto_the_goods.replace(
                  /^(0+)|[^\d]+/g,
                  ''
                )
              "
              style="width: 318px"
              :placeholder="$t('plugInsSet.groupSetInfo.qsrzdshsj')"
              v-model="ruleForm.auto_the_goods"
            >
              <template slot="append">{{
                $t("plugInsSet.groupSetInfo.tian")
              }}</template>
            </el-input>
            <span>{{ $t('plugInsSet.groupSetInfo.xtzdwcshcz') }}</span>
          </el-form-item>
          <el-form-item label="售后时间设置" prop="order_after" >
            <span style="margin-right: 10px">{{ $t('plugInsSet.groupSetInfo.ddwch') }}</span>
            <el-input
              v-on:input="
                ruleForm.order_after = ruleForm.order_after.replace(
                  /[^\d]+/g,
                  ''
                )
              "
              style="width: 318px"
              :placeholder="$t('plugInsSet.groupSetInfo.qsrzdshouhshijian')"
              v-model="ruleForm.order_after"
            >
              <template slot="append">{{
                $t("plugInsSet.groupSetInfo.tian")
              }}</template>
            </el-input>
            <span>{{ $t('plugInsSet.groupSetInfo.wshsj') }}</span>
          </el-form-item>
          <el-form-item :label="$t('plugInsSet.groupSetInfo.zdpjsz')" required>
            <el-switch
              v-model="ruleForm.goodSwitch"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
              @change="goodChange($event)"
            >
            </el-switch>
            <div class="shaco_box_three" v-if="ruleForm.goodSwitch == 1">
              <div >
                <span class="font_wone">{{
                  $t("plugInsSet.groupSetInfo.text7")
                }}</span>
                <el-input
                  style="width: 175px"
                  placeholder=""
                  v-model="ruleForm.auto_good_comment_day"
                  v-on:input="
                    ruleForm.auto_good_comment_day =
                      ruleForm.auto_good_comment_day.replace(
                        /^(0+)|[^\d]+/g,
                        ''
                      )
                  "
                  clearable
                >
                  <template slot="append">{{
                    $t("plugInsSet.groupSetInfo.tian")
                  }}</template>
                </el-input>
                <span class="font_wtwo">{{
                  $t("plugInsSet.groupSetInfo.text8")
                }}</span>
              </div>
              <div style="display: flex;align-items: center;">
                <span
                  class="font_wone"
                  style="line-height: 80px; margin-right: 10px"
                  >{{ $t("plugInsSet.groupSetInfo.hpnr") }}</span
                >
                <el-input
                  style="width: 314px"
                  type="textarea"
                  placeholder=""
                  resize="none"
                  v-model="ruleForm.auto_good_comment_content"
                  rows="2"
                >
                </el-input>
              </div>
            </div>
          </el-form-item>
        </div>
      </div>
      <div style="min-height: 80px"></div>
      <div class="footer-button">
        <el-button
          plain
          class="footer-cancel fontColor"
          @click="back"
          >{{ $t("DemoPage.tableFromPage.cancel") }}</el-button
        >
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
import { getDiscount, setDiscount } from "@/api/plug_ins/coupons";
export default {
  name: "",
  props: {},
  components: {},
  data() {
    return {
      ruleForm: {
        is_open: 1,
        auto_the_goods: "", //收货
        order_after: "", //售后
        goodSwitch: 1,
        auto_good_comment_day: "", //自动评价时间
        auto_good_comment_content: "", //内容
      },
      rules: {
        auto_the_goods: [
          {
            required: true,
            message: this.$t('plugInsSet.groupSetInfo.qsrzdshsj'),
            trigger: "blur",
          },
        ],
        order_after: [
          {
            required: true,
            message: this.$t('plugInsSet.groupSetInfo.qsrzdshouhshijian'),
            trigger: "blur",
          },
        ],
      },
      flag: true
    };
  },
  computed: {},
  watch: {},
  created() {
    this.handleGetDiscount();
  },
  mounted() {},
  beforeRouteLeave (to, from, next) {
    if (JSON.stringify(this.ruleForm) == sessionStorage.getItem('ruleForm_discount')) {
      next()
    } else {
      console.log('表单变化，询问是否保存')
      next(false)
      this.$confirm(
        this.$t('coupons.couponsSet.sjygx'),
        this.$t('coupons.ts'),
        {
          distinguishCancelAndClose: true,
          confirmButtonText: this.$t('coupons.okk'),
          cancelButtonText: this.$t('coupons.ccel'),
          type: 'warning'
        }
      )
        .then(() => {
          // this.submitForm()
          next()
        })
        .catch(() => {
          // next()
          // next('/plug_ins/plugInsSet/plugInsList')
        })
    }
  },
  methods: {
    back () {
      this.$router.push({
        path: '/plug_ins/plugInsSet/plugInsList'
      })
    },
    async handleGetDiscount() {
      const res = await getDiscount({
        api: "plugin.flashsale.AdminFlashSale.getFlashSaleConfig",
      });
      console.log("1721721721472172", res);
      if (res.data.code == 200) {
        const {
          is_open,
          auto_the_goods,
          order_after,
          auto_good_comment_day,
          auto_good_comment_content,
        } = res.data.data.config;
        console.log(
          is_open,
          auto_the_goods,
          order_after,
          auto_good_comment_day,
          auto_good_comment_content
        );
        this.ruleForm.is_open = is_open;
        this.ruleForm.auto_the_goods = auto_the_goods / 86400;
        this.ruleForm.order_after = order_after / 86400;
        this.ruleForm.auto_good_comment_day = auto_good_comment_day / 86400;
        this.ruleForm.auto_good_comment_content = auto_good_comment_content;
        // 关闭打开
        this.ruleForm.goodSwitch = res.data.data.config.good_switch
        // if (this.ruleForm.auto_good_comment_day == 0) {
        //   this.ruleForm.goodSwitch = 0;
        // } else {
        //   this.ruleForm.goodSwitch = 1;
        // }
      sessionStorage.setItem('ruleForm_discount', JSON.stringify(this.ruleForm))

      }
    },
    async submitForm() {
      this.$refs.ruleForm.validate(async (valid) => {
        if (valid) {
          if (this.ruleForm.goodSwitch == 1) {
            if (
              this.ruleForm.auto_good_comment_day == "" ||
              this.ruleForm.auto_good_comment_content == ""
            ) {
              this.$message({
              type: "warning",
              message:this.$t("plugInsSet.groupSetInfo.kqhpszbnwk"),
              offset: 102,
            });
              return;
            }
          }
          if (!this.flag) {
            return
          }
          this.flag = false
          const res = await setDiscount({
            api: "plugin.flashsale.AdminFlashSale.addFlashSaleConfig",
            ...this.ruleForm,
          }).finally(()=>{
            setTimeout(()=>{
              this.flag = true 
            },1500)
          })
          if (res.data.code == 200) {
            this.$message({
              type: "success",
              message: this.$t("zdata.baccg"),
              offset: 102,
            });
            sessionStorage.setItem('ruleForm_discount', JSON.stringify(this.ruleForm))
            setTimeout(() => {
              this.$router.push({
                    path: '/plug_ins/plugInsSet/plugInsList'
                  })
            }, 1000);
          }
        }
      });
    },
  },
};
</script>
<style scoped lang="less">
@import "../../../webManage/css/plug_ins/plugInsSet/discount.less";
</style>
