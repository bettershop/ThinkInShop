<template>
  <div class="container">
    <div class="add-menu">
      <el-form
        :model="mainForm"
        :rules="rules"
        ref="ruleForm"
        class="picture-ruleForm"
        label-width="150px"
      >
        <el-form-item :label="$t('sheetOfNoodles.expressSheet.wlgs')" prop="express_id">
          <el-select
            class="select-input"
            v-model="mainForm.express_id"
            :placeholder="$t('sheetOfNoodles.expressSheet.qxlwgs')"
            @change="change_type"
          >
            <el-option
              v-for="(item, index) in categoryList"
              :key="index"
              :label="item.kuaidi_name"
              :value="item.id"
            >
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="partnerId" prop="partnerId">
          <el-input
            v-model="mainForm.partnerId"
            :placeholder="$t('sheetOfNoodles.expressSheet.qrdzmdzhhxx')"
          ></el-input>
          <div class="form-tip">{{$t('sheetOfNoodles.expressSheet.dzmdzhsm')}}</div>
        </el-form-item>

        <el-form-item label="tempId" prop="temp_id">
          <el-input
            v-model="mainForm.temp_id"
            :placeholder="$t('sheetOfNoodles.expressSheet.qsrmbid')"
          ></el-input>
          <div class="form-tip">{{$t('sheetOfNoodles.expressSheet.mbidsm')}}</div>
        </el-form-item>

        <el-form-item label="partnerKey" prop="partnerKey">
          <el-input
            v-model="mainForm.partnerKey"
            :placeholder="$t('sheetOfNoodles.expressSheet.qrdzmdzhmm')"
          ></el-input>
          <div class="form-tip">
            {{$t('sheetOfNoodles.expressSheet.dzmdzhmmsm')}}
            <a href="javascript:;" class="link-text" @click="openGuideLink"> {{$t('sheetOfNoodles.expressSheet.zy')}}</a>
          </div>
        </el-form-item>

        <el-form-item label="partnerSecret" prop="partnerSecret">
          <el-input
            v-model="mainForm.partnerSecret"
            :placeholder="$t('sheetOfNoodles.expressSheet.qrdzmdmy')"
          ></el-input>
          <div class="form-tip">
            {{$t('sheetOfNoodles.expressSheet.dzmdmysm')}}
            <a href="javascript:;" class="link-text" @click="openGuideLink">{{$t('sheetOfNoodles.expressSheet.zy')}}</a>
          </div>
        </el-form-item>

        <el-form-item label="partnerName" prop="partnerName">
          <el-input
            v-model="mainForm.partnerName"
            :placeholder="$t('sheetOfNoodles.expressSheet.qrdzmdkhmc')"
          ></el-input>
          <div class="form-tip">{{$t('sheetOfNoodles.expressSheet.dzmdkhmcsm')}}</div>
        </el-form-item>

        <el-form-item label="net" prop="net">
          <el-input
            v-model="mainForm.net"
            :placeholder="$t('sheetOfNoodles.expressSheet.qrwdm')"
          ></el-input>
          <div class="form-tip">{{$t('sheetOfNoodles.expressSheet.wdmcsm')}}</div>
        </el-form-item>

        <el-form-item label="code" prop="code">
          <el-input
            v-model="mainForm.code"
            :placeholder="$t('sheetOfNoodles.expressSheet.qrdzmdzcbb')"
          ></el-input>
          <div class="form-tip">{{$t('sheetOfNoodles.expressSheet.czbhsm')}}</div>
        </el-form-item>

        <el-form-item label="checkMan" prop="checkMan">
          <el-input
            v-model="mainForm.checkMan"
            :placeholder="$t('sheetOfNoodles.expressSheet.dzmdzckdy')"
          ></el-input>
          <div class="form-tip">{{$t('sheetOfNoodles.expressSheet.kdysm')}}</div>
        </el-form-item>

        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="Save('ruleForm')"
            >{{ $t("DemoPage.tableFromPage.save") }}
            </el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain
            >{{ $t("DemoPage.tableFromPage.cancel") }}
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>
<script>
import { getLogisticsList, saveLogistics } from "@/api/order/setExpressSheet";
import log from "@/libs/util.log";

export default {
  name: "",
  props: {},
  components: {},
  data() {
    return {
      mainForm: {
        express_id: "",
        partnerId: "",
        temp_id: "",
        partnerKey: "",
        partnerSecret: "",
        partnerName: "",
        net: "",
        code: "",
        checkMan: "",
      
      },
      rules: {
        express_id: [
          { required: true, message: this.$t('sheetOfNoodles.expressSheet.qxlwgs'), trigger: "change" },
        ],
        temp_id: [
          { required: true, message: this.$t('sheetOfNoodles.expressSheet.qsrmbid'), trigger: "change" },
        ],
        partnerId: [
          {
            required: true,
            message: this.$t('sheetOfNoodles.expressSheet.qrdzmdzhhxx'),
            trigger: "blur",
          },
        ],
      },
      //   物流公司数据
      categoryList: [],
      cate_type: "",
      // 指引链接地址
      guideUrl: "https://api.kuaidi100.com/document/5f0ff6e82977d50a94e10237"
    };
  },
  computed: {},
  watch: {},
  created() {
    this.getLogistics();
  },
  mounted() {},
  methods: {
    change_type(){},
    // 打开指引链接
    openGuideLink() {
      // 在新标签页中打开链接
      window.open(this.guideUrl, '_blank');
    },
    // 获取物流数据
    async getLogistics() {
      const res = await getLogisticsList({
        api: "admin.express.get_logistics",
      }); 
      if (res.data.code == 200) {
        this.categoryList = res.data.data.list;
      }
    },
    // 保存
    Save(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          let data = {
            api: "admin.express.add_logistics",
            ...this.mainForm,
          };
          saveLogistics(data).then((res) => {
            console.log("保存数据xxxxxx", res);
            if (res.data.code == 200) {
              this.$message({
                type: "success",
                message: this.$t("zdata.tjcg"),
                offset: 102,
              });
              this.$router.go(-1);
            }
          });
        }
      });
    },
  },
};
</script>
<style scoped lang="less">
.add-menu {
  margin: 0 auto;
}
.container {
  padding-top: 40px;
  align-items: normal;
  height: auto !important;
}
.container {
  width: 100%; 
  background-color: #fff;
  padding: 40px 0 0 0;
  color: #414658;
  display: flex;
  align-items: center;
  .el-input {
    width: 580px;
    height: 40px;
    input {
      width: 580px;
      height: 40px;
    }
  }
  .el-select {
    width: 580px;
    height: 40px;
    .el-input {
      width: 580px;
      height: 40px;
      input {
        width: 580px;
        height: 40px;
      }
    }
  }

  .right {
    margin-left: 10px;
    color: #97a0b4;
  }
}

// 新增的提示文本样式
.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  padding-left: 2px;

  // 链接文字样式
  .link-text {
    color: #409eff;
    text-decoration: none;
    cursor: pointer;
    &:hover {
      text-decoration: underline;
    }
  }
}

// 调整表单项的间距
/deep/ .el-form-item {
  margin-bottom: 20px;
}
</style>
