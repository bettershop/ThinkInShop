<template>
  <div class="container">
    <div class="header">
      <span>{{ $t("bulletin.announcementrelease.fbgg") }}</span>
    </div>
    <el-form
      :model="ruleForm"
      :rules="rule"
      ref="ruleForm"
      class="form-search"
      :disabled="true"
    >
      <div class="notice">
        <el-form-item
          class="title"
          :label="$t('bulletin.announcementrelease.ggbt')"
          prop="goodsTitle"
        >
          <el-input
            v-model="ruleForm.goodsTitle"
            :placeholder="$t('bulletin.announcementrelease.qsrggbt')"
          ></el-input>
        </el-form-item>
        <el-form-item
          class="Select"
          :label="$t('bulletin.announcementrelease.gglx')"
          prop="region"
        >
          <el-select
            class="select-input"
            v-model="ruleForm.region"
            :placeholder="$t('bulletin.announcementrelease.qxzgglx')"
          >
            <el-option
              v-for="(item, index) in option"
              :key="index"
              :label="item.text"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-tooltip placement="bottom" effect="light">
            <div slot="content">
              <p style="margin-bottom: 5px; color: #414658; font-size: 12px">
                {{ $t("bulletin.announcementrelease.ptgg") }}
              </p>
              <p style="color: #97a0b4">
                {{ $t("bulletin.announcementrelease.ptggtx") }}
              </p>
              <p style="margin-bottom: 5px; margin-top: 5px; color: #414658">
                {{ $t("bulletin.announcementrelease.bbsjgg") }}
              </p>
              <p style="color: #97a0b4">
                {{ $t("bulletin.announcementrelease.bbsjggtx") }}
              </p>
              <p style="margin-bottom: 5px; margin-top: 5px; color: #414658">
                {{ $t("bulletin.announcementrelease.xtwhgg") }}
              </p>
              <p style="color: #97a0b4">
                {{ $t("bulletin.announcementrelease.xtwhggtx") }}
              </p>
            </div>
            <img
              style="
                width: 17px;
                height: 17rpx;
                object-fit: contain;
                margin-left: 10px;
              "
              src="../../../../assets//imgs//wenhao.png"
              alt=""
            />
          </el-tooltip>
        </el-form-item>
      </div>

      <div class="Commencement-date">
        <el-form-item
          :label="$t('bulletin.announcementrelease.yxsj')"
          prop="range"
        >
          <el-date-picker
            v-model="ruleForm.range"
            type="datetimerange"
            :range-separator="$t('bulletin.announcementrelease.zhi')"
            :start-placeholder="$t('bulletin.announcementrelease.kssj')"
            :end-placeholder="$t('bulletin.announcementrelease.jsrq')"
            value-format="yyyy-MM-dd HH:mm:ss"
            :editable="false"
          >
          </el-date-picker>
          <div class="introduce">
            <!-- <span>&nbsp;{{ introduce }}</span> -->
          </div>
        </el-form-item>
      </div>

      <div class="Commencement-date">
        <el-form-item
          :label="$t('bulletin.announcementrelease.jsfxz')"
          prop="storeTypes"
        >
          <el-checkbox-group v-model="ruleForm.storeTypes">
            <el-checkbox v-for="(item, index) in selectOption" :key="index" >{{
              item
            }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </div>

      <div class="rich-text">
        <el-form-item required :label="$t('bulletin.announcementrelease.ggnr')">
        </el-form-item>

        <div class="richText-info">
          <vue-editor
            v-model="content"
            useCustomImageHandler
            :disabled="true"
            @image-added="handleImageAdded"
            @focus="onEditorFocus"
          ></vue-editor>
        </div>

        <!-- <div class="footer-button">
            <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">{{$t('bulletin.announcementrelease.ccel')}}</el-button>
            <el-button style="margin-left: 14px !important" type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{$t('bulletin.announcementrelease.save')}}</el-button>
          </div> -->
      </div>
    </el-form>
  </div>
</template>

<script>
import { VueEditor } from "vue2-editor";
import OSS from "ali-oss";
import {
  addSysNoticeInfo,
  getDictionaryInfo,
} from "@/api/Platform/bulletin.js";
import Config from "@/packages/apis/Config";
import { getStorage } from "@/utils/storage";
import axios from "axios";
export default {
  name: "vieworannouncement",

  components: {
    VueEditor,
  },

  data() {
    return {
      selectOption:this.$t('bulletin.announcementrelease.SelectOptionList'),
      ruleForm: {
        goodsTitle: "",
        region: this.$t("bulletin.announcementrelease.xtwh"),
        range: "",
        storeTypes: [],
      },
      type: 1,
      option: [
        {
          data: this.$t("bulletin.announcementrelease.xtwh"),
        },
        {
          data: this.$t("bulletin.announcementrelease.bbgx"),
        },
      ],

      rule: {
        goodsTitle: [
          {
            required: true,
            message: this.$t("bulletin.announcementrelease.qsrggbt"),
            trigger: "blur",
          },
        ],
        region: [
          {
            required: true,
            message: this.$t("bulletin.announcementrelease.qxzgglx"),
            trigger: "change",
          },
        ],
        range: [
          {
            required: true,
            message: this.$t("bulletin.announcementrelease.qxzyxsj"),
            trigger: "change",
          },
        ],
        storeTypes: [
          {
            required: true,
            message: this.$t("bulletin.announcementrelease.qxzjsf"),
            trigger: "change",
          },
        ],
      },

      value1: [new Date(2000, 10, 10, 10, 10), new Date(2000, 10, 11, 10, 10)],
      introduce: this.$t("bulletin.announcementrelease.yxsjn"),
      actionUrl: Config.baseUrl,

      // 富文本编辑器数据
      content: "",
    };
  },

  created() {
    this.getDictionaryInfo();
    if (this.$route.params.title) {
      this.ruleForm.goodsTitle = this.$route.params.title;
      this.ruleForm.region = this.$route.params.type.toString();
      this.ruleForm.range = [];
      this.ruleForm.range[0] = this.$route.params.startdate;
      this.ruleForm.range[1] = this.$route.params.enddate;
      this.content = this.$route.params.content;
      this.ruleForm.storeTypes = this.$route.params.storeTypes;
    } else {
      this.ruleForm.goodsTitle = "";
      this.ruleForm.region = this.$t("bulletin.announcementrelease.xtwh");
      this.ruleForm.range = "";
      this.msg = "";
    }
  },

  watch: {
    "ruleForm.region": {
      handler: function () {
        if (
          this.ruleForm.region === this.$t("bulletin.announcementrelease.xtwh")
        ) {
          this.introduce = this.$t("bulletin.announcementrelease.yxsjn1");
          this.type = 1;
        } else {
          this.introduce = this.$t("bulletin.announcementrelease.yxsjn2");
          this.type = 2;
        }
      },
    },
  },

  beforeRouteLeave(to, from, next) {
    if (to.name == "announcementlist") {
      to.params.dictionaryNum = this.$route.query.dictionaryNum;
      to.params.pageSize = this.$route.query.pageSize;
    }
    next();
  },

  methods: {
    onEditorFocus(event) {
      event.enable(false);
    },
    async getDictionaryInfo() {
      const res = await getDictionaryInfo({
        api: "saas.dic.getDictionaryInfo",
        key: "公告类型",
        status: 1,
      });
      console.log(res);
      this.option = res.data.data.list;
    },
    getverson(val) {
      if (val === 1) {
        return this.$t("bulletin.announcementrelease.xtwh");
      } else {
        return this.$t("bulletin.announcementrelease.bbgx");
      }
    },

    handleImageAdded(file, Editor, cursorLocation, resetUploader) {
      var formData = new FormData();
      formData.append("file", file); //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl, //上传路径
        method: "POST",
        params: {
          api: "resources.file.uploadFiles",
          storeId: getStorage("laike_admin_userInfo").storeId,
          groupId: -1,
          uploadType: 2,
          accessId: this.$store.getters.token,
        },
        data: formData,
      })
        .then((result) => {
          console.log(result);
          let url = result.data.data.imgUrls[0]; // 返回给你的图片路径
          Editor.insertEmbed(cursorLocation, "image", url);
          // Editor.setSelection(length)
          resetUploader();
        })
        .catch((err) => {
          console.log(err);
        });
    },

    // 发布/修改公告
    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            if (this.ruleForm.goodsTitle.length > 100) {
              this.$message({
                message: this.$t("bulletin.announcementrelease.ggcd"),
                type: "error",
                offset: 102,
              });
              return;
            }
            let { entries } = Object;
            let data = {
              api: "saas.sysNotice.addSysNoticeInfo",
              storeType: 8,
              id: this.$route.params.id,
              title: this.ruleForm.goodsTitle,
              type: this.ruleForm.region,
              startDate: this.ruleForm.range[0],
              endDate: this.ruleForm.range[1],
              content: this.content,
              storeTypes: this.ruleForm.storeTypes.toString(),
            };

            let formData = new FormData();
            for (let [key, value] of entries(data)) {
              formData.append(key, value);
            }
            addSysNoticeInfo(formData).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("zdata.bjcg"),
                  type: "success",
                  offset: 102,
                });
                console.log(res);
                this.$router.go(-1);
              }
            });
          } catch (error) {
            this.$message({
              message: error.message,
              type: "error",
              showClose: true,
            });
          }
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
  },
};
</script>

<style scoped lang="less">
.container {
  width: 100%;
  height: 737px;
  background-color: #fff;
  border-radius: 4px;
  .header {
    width: 100%;
    height: 60px;
    line-height: 60px;
    padding-left: 20px;
    border-bottom: 1px solid #e9ecef;
    span {
      font-size: 16px;
      font-weight: 400;
      color: #414658;
    }
  }

  /deep/.el-form {
    width: 100%;
    .notice {
      padding: 40px 0 0 60px;
      display: flex;
      .Select {
        margin-left: 40px;
      }
      .el-form-item__label {
        color: #414658;
      }
      .el-form-item__content {
        display: flex;
        input {
          width: 420px;
          height: 40px;
        }
      }
    }
    .ql-toolbar {
            pointer-events: none;
          }

    .Commencement-date {
      padding-left: 60px;
      .el-form-item__label {
        // padding-right: 22px;
      }
      .el-form-item__content {
        display: flex;
        .el-date-editor {
          width: 420px;
        }
      }
      .introduce {
        color: #97a0b4;
        margin-left: 5px;
      }
    }

    .rich-text {
      padding-left: 60px;
      display: flex;
      width: 90%;
      .text-title {
        margin-right: 20px;
        span {
          font-size: 14px;
          font-weight: 400;
          color: #414658;
        }
      }
    }

    .richText-info {
      flex: 1;
      height: 341px;
      .quillWrapper {
        .ql-container {
          height: 300px;
        }
      }
    }

    .footer-button {
      position: fixed;
      right: 0;
      bottom: 40px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding: 15px 20px;
      border-top: 1px solid #e9ecef;
      background: #ffffff;
      width: 300%;
      z-index: 10;
      button {
        width: 70px;
        height: 40px;
      }
    }
  }
}
</style>
