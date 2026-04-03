<template>
  <div class="container">
    <el-form
      :model="ruleForm"
      label-position="right"
      ref="ruleForm"
      label-width="auto"
      class="form-search"
    >
      <div class="box">
        <div class="header">
          <span>{{ $t('stores.storeSet.ggsz') }}</span>
        </div>
        <div class="box_one">
          <el-form-item :label="$t('stores.storeSet.mrtx')" required>
            <l-upload
              :limit="1"
              v-model="ruleForm.headImg"
              :text="$t('stores.storeSet.jy120')"
            >
            </l-upload>
          </el-form-item>
          <el-form-item :label="$t('stores.storeSet.dpmr')" required>
            <l-upload
              :limit="1"
              v-model="ruleForm.defaultLogo"
              :text="$t('stores.storeSet.jy64')"
            >
            </l-upload>
          </el-form-item>
          <el-form-item :label="$t('stores.storeSet.dpmrxc')" required>
            <l-upload
              :limit="1"
              v-model="ruleForm.posterImg"
              :text="$t('stores.storeSet.jy200')"
            >
            </l-upload>
          </el-form-item>
        </div>
      </div>
      <div class="box">
        <div class="header">
          <span>{{ $t('stores.storeSet.qtsz') }}</span>
        </div>
        <div class="box_one">
          <el-form-item
            class="del-set"
            :label="$t('stores.storeSet.zxsz')"
            required
          >
            <div class="set">
              <span>{{ $t('stores.storeSet.cg') }}</span>
              <el-input
                class="box_input1"
                v-model="ruleForm.autoLogOff"
                @keyup.native="
                  ruleForm.autoLogOff = oninput3(ruleForm.autoLogOff)
                "
              ></el-input>
              <span>{{ $t('stores.storeSet.xtzdzx') }}</span>
            </div>
          </el-form-item>
          <el-form-item class="del-set" :label="$t('stores.storeSet.rzsh')" required>
            <div class="set">
              <span>{{ $t('stores.storeSet.cg') }}</span>
              <el-input
                class="box_input1"
                v-model="ruleForm.autoExamine"
                @keyup.native="
                  ruleForm.autoExamine = oninput2(ruleForm.autoExamine)
                "
              ></el-input>
              <span>{{ $t('stores.storeSet.ggznw') }}</span>
              <span class="ke_font">{{ $t('stores.storeSet.szwls') }}</span>

            </div>
          </el-form-item>
          <el-form-item :label="$t('stores.storeSet.spsc')" required>
            <el-checkbox-group v-model="ruleForm.goodsUpload">
              <el-checkbox
                v-for="label in goodsUploadList"
                :label="label.id"
                :key="label.id"
                >{{ label.name }}</el-checkbox
              >
            </el-checkbox-group>
          </el-form-item>
          <el-form-item :label="$t('stores.storeSet.bzjkg')">
            <template>
              <el-switch
                v-model="ruleForm.isPromiseSwitch"
                :active-value="1"
                :inactive-value="0"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              >
              </el-switch>
              <span class="ke_font"
                >{{ $t('stores.storeSet.jybyf') }}</span
              >
              <div class="box_security" v-show="ruleForm.isPromiseSwitch == 1">
                <el-form-item :label="$t('stores.storeSet.bzj')" required>
                  <el-input
                    class="box_input2"
                    v-model="ruleForm.bondmoney"
                    :placeholder="$t('stores.storeSet.qsrbzj')"
                    @keyup.native="
                      ruleForm.bondmoney = oninput2(ruleForm.bondmoney, 2)
                    "
                  >
                    <span slot="append" icon="el-icon-search">
                      <span>
                        {{ $t('stores.storeSet.yu') }}
                      </span>
                    </span>
                  </el-input>
                </el-form-item>
                <el-form-item :label="$t('stores.storeSet.bzjs')">
                  <vue-editor
                    v-model="remark"
                    useCustomImageHandler
                    @image-added="handleImageAdded"
                  ></vue-editor>
                </el-form-item>
              </div>
            </template>
          </el-form-item>
        </div>
      </div>
      <div class="box" style="margin-bottom: 71px;">
        <div class="header">
          <span>{{ $t('stores.storeSet.txsz') }}</span>
        </div>
        <div class="box_one">
          <el-form-item :label="$t('stores.storeSet.zxtx')" required>
            <el-input
              class="box_input3"
              v-model="ruleForm.min_pric"
              :placeholder="$t('stores.storeSet.qsrzx')"
            >
              <span slot="append" icon="el-icon-search">
                <span>
                  {{ $t('stores.storeSet.yu') }}
                </span>
              </span>
            </el-input>
            <span style="color: #97a0b4;margin-left: 10px;">({{
              $t('stores.storeSet.zdxzwsm')
            }})</span>
          </el-form-item>
          <el-form-item :label="$t('stores.storeSet.zdtx')" required>
            <el-input
              class="box_input3"
              v-model="ruleForm.max_price"
              :placeholder="$t('stores.storeSet.qsrzd')"
            >
              <span slot="append" icon="el-icon-search">
                <span>
                  {{ $t('stores.storeSet.yu') }}
                </span>
              </span>
            </el-input>
          </el-form-item>
          <el-form-item
            class="poundages"
            :label="$t('stores.storeSet.sxf')"
            required
          >
            <el-input
              class="box_input3"
              type="number"
              min="0"
              v-model="ruleForm.poundage"
              @keyup.native="ruleForm.poundage = oninput(ruleForm.poundage,1)"
              placeholder=""
            ><el-button slot="append">%</el-button></el-input>
            <span class="ke_font">{{
              $t('stores.storeSet.sxfwd')
            }}</span>
          </el-form-item>
          <el-form-item :label="$t('stores.storeSet.txsjsz')">
            <el-radio-group v-model="ruleForm.withdrawalTimeOpen" @change="timeChange()">
              <el-radio :label="0">{{ $t('stores.storeSet.bxz') }}</el-radio>
              <el-radio :label="1">{{ $t('stores.storeSet.zdrq') }}</el-radio>
              <el-radio :label="2">{{ $t('stores.storeSet.zdsjd') }}</el-radio>
            </el-radio-group>
            <div class="box_three" v-if="ruleForm.withdrawalTimeOpen == 1">
              <span class="red">*</span><span>{{ $t('stores.storeSet.my') }}</span>
              <el-input
              class="box_input1"
              @keyup.native="
                      ruleForm.withdrawalTime1 = oninput4(ruleForm.withdrawalTime1)
                    "
              v-model="ruleForm.withdrawalTime1"
              placeholder=""
            ></el-input>
            <span>{{ $t('stores.storeSet.rktx') }}</span>
            </div>
            <div class="box_four" v-if="ruleForm.withdrawalTimeOpen == 2">
              <span class="red">*</span><span>{{ $t('stores.storeSet.my') }}</span>
              <el-input
              class="box_input1"
              @keyup.native="
                      ruleForm.withdrawalTime1 = oninput4(ruleForm.withdrawalTime1)
                    "
              v-model="ruleForm.withdrawalTime1"
              placeholder=""
            ></el-input>
            <span>{{ $t('stores.storeSet.rz') }}</span>
            <el-input
              class="box_input1"
              @keyup.native="
                      ruleForm.withdrawalTime2 = oninput4(ruleForm.withdrawalTime2)
                    "
              v-model="ruleForm.withdrawalTime2"
              placeholder=""
            ></el-input>
            <span>{{ $t('stores.storeSet.rktx') }}</span>
            </div>
          </el-form-item>
          <el-form-item
            class="instructions"
            :label="$t('stores.storeSet.txsm')"
          >
            <div class="richText-info">
              <vue-editor
                v-model="content"
                useCustomImageHandler
                @image-added="handleImageAdded" 
              ></vue-editor>
            </div>
          </el-form-item>
        </div>
      </div>
       
      <div class="footer-button">
        <el-button
          plain
          class="footer-cancel fontColor"
          @click="back"
          >{{ $t('DemoPage.tableFromPage.cancel') }}</el-button
        >
        <el-button
          type="primary"
          class="footer-save bgColor mgleft"
          @click="submitForm('ruleForm')"
          >{{ $t('DemoPage.tableFromPage.save') }}</el-button
        >
      </div>
    </el-form>
  </div>
</template>

<script>
import storeSetInfo from '@/webManage/js/plug_ins/plugInsSet/storeSetInfo'
export default storeSetInfo
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/plugInsSet/storeSetInfo.less';
.money {
  margin-top: 20px;
  font-size: 14px;
  font-family: MicrosoftYaHei;
  color: #414658;
  opacity: 1;
  span {
    margin: 0 5px;
  }
  .el-input {
    .el-input__inner {
      width: 320px !important;
      height: 36px;
    }
  }
}
</style>
