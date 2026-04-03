<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
          <el-radio-button :label="$t('stores.dp')" @click.native.prevent="$router.push('/plug_ins/stores/store')"></el-radio-button>
          <el-radio-button :label="$t('stores.dpfl')" @click.native.prevent="$router.push('/plug_ins/stores/storeFl')"></el-radio-button>
          <el-radio-button :label="$t('stores.dpsh')" @click.native.prevent="$router.push('/plug_ins/stores/auditList')"></el-radio-button>
          <el-radio-button :label="$t('stores.bzjjl')" @click.native.prevent="$router.push('/plug_ins/stores/bondMoney')"></el-radio-button>
          <el-radio-button :label="$t('stores.bzjsh')" @click.native.prevent="$router.push('/plug_ins/stores/bondExamine')"></el-radio-button>
          <el-radio-button :label="$t('stores.spsh')" @click.native.prevent="$router.push('/plug_ins/stores/goodsAudit')"></el-radio-button>
          <el-radio-button :label="$t('stores.txsh')" @click.native.prevent="$router.push('/plug_ins/stores/withdrawalAudit')"></el-radio-button>
          <el-radio-button :label="$t('stores.txjl')" @click.native.prevent="$router.push('/plug_ins/stores/withdrawalRecord')"></el-radio-button>
          <el-radio-button :label="$t('stores.dpsz')" @click.native.prevent="toset()"></el-radio-button>
        </el-radio-group>
    </div>

    <div class="store-set">
      <el-form :model="ruleForm" label-position="right" ref="ruleForm" label-width="135px" class="form-search">
        <div class="notice">
          <el-form-item :label="$t('stores.storeSet.sfkw')">
            <el-switch v-model="ruleForm.switchs" @change="switchs(scope.row)" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
          </el-form-item>
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
          <!-- <el-form-item :label="游客默认头像：" required>
            <l-upload
              :limit="1"
              v-model="ruleForm.defaultLogo"
              text="（建议上传690*180px尺寸的图片）"
            >
            </l-upload>
          </el-form-item> -->
          <el-form-item class="del-set" :label="$t('stores.storeSet.scsz')" required>
            <div class="set">
              <span>{{$t('stores.storeSet.cg')}}</span>
              <el-input v-model="ruleForm.delete_day" @keyup.native="ruleForm.delete_day = oninput2(ruleForm.delete_day)"></el-input>
              <span>{{$t('stores.storeSet.gywdl')}}</span>
            </div>
          </el-form-item>
          <el-form-item class="del-set" :label="$t('stores.storeSet.rzsh')" required>
            <div class="set">
              <span>{{$t('stores.storeSet.mr')}}</span>
              <el-input v-model="ruleForm.autoExamine" @keyup.native="ruleForm.autoExamine = oninput2(ruleForm.autoExamine)"></el-input>
              <span>{{$t('stores.storeSet.ggznw')}}</span>
            </div>
          </el-form-item>
          <el-form-item :label="$t('stores.storeSet.spsc')" required>
            <el-checkbox-group v-model="ruleForm.goodsUpload">
              <el-checkbox v-for="label in goodsUploadList" :label="label.id" :key="label.id">{{label.name}}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item :label="$t('stores.storeSet.zxtx')" required>
            <el-input v-model="ruleForm.min_pric" :placeholder="$t('stores.storeSet.qsrzx')"></el-input>
          </el-form-item>
          <el-form-item :label="$t('stores.storeSet.zdtx')" required>
            <el-input v-model="ruleForm.max_price" :placeholder="$t('stores.storeSet.qsrzd')"></el-input>
          </el-form-item>
          <el-form-item class="poundages" :label="$t('stores.storeSet.sxf')" required>
            <el-input type="number" min="0" v-model="ruleForm.poundage" placeholder=""></el-input>
            <span style="color:#97A0B4">{{$t('stores.storeSet.sxfwd')}}</span>
          </el-form-item>
          <el-form-item :label="$t('stores.storeSet.bzjkg')" required>
            <template>
              <el-switch v-model="ruleForm.isPromiseSwitch" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
              </el-switch>
              <span style="color: #97A0B4;margin-left: 0.625rem;">{{$t('stores.storeSet.jybyf')}}</span>
              <div class="security-deposit" v-show="ruleForm.isPromiseSwitch == 1">
                <div required>
                  <label style="color:red;margin-top:2px">*</label>
                  <span style="padding-right: 14px;">{{$t('stores.storeSet.bzj')}}</span>
                  <el-input v-model="ruleForm.bondmoney" :placeholder="$t('stores.storeSet.qsrbzj')" @keyup.native="ruleForm.bondmoney = oninput(ruleForm.bondmoney,2)"></el-input><span>{{$t('stores.storeSet.yu')}}</span>
                </div>
                <div class="richText-info money">
                  <span>{{$t('stores.storeSet.bzjs')}}</span>
                  <vue-editor 
                    v-model="remark"
                    useCustomImageHandler
                    @image-added="handleImageAdded"
                  ></vue-editor>
                </div>
              </div>
            </template>
          </el-form-item>
          <el-form-item class="instructions" :label="$t('stores.storeSet.txsm')">
            <div class="richText-info">
              <vue-editor 
                v-model="content"
                useCustomImageHandler
                @image-added="handleImageAdded"
              ></vue-editor>
            </div>
          </el-form-item>
        </div>
        
        <div class="footer-button">
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import storeSet from '@/webManage/js/plug_ins/stores/storeSet'
export default storeSet
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/stores/storeSet.less';
.money{
  margin-top:20px;
  font-size: 14px;
  font-family: MicrosoftYaHei;
  color: #414658;
  opacity: 1;
    span{
      margin:0 5px;
    }
    .el-input{
      .el-input__inner{
        width:320px !important;
        height:36px;
      }
    }
}
</style>