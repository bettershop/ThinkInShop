<template>
  <div class="container">
    <!-- 引导图模块 -->
    <div
      v-if="tabPosition != '3'"
      :class="tabPosition === '2' ? `` : 'intro'"
      :style="tabPosition === '2' ? `flex:1` : ''"
    >
      <div
        class="card"
        :style="tabPosition === '2' ? `height:100%;margin-bottom:0;` : ''"
      >
        <div class="title">
          <div>
            {{ $t("terminalConfig.ydt") }}
            <span style="color: rgb(151, 160, 180); font-size: 14px"
              >（ {{ $t("terminalConfig.zdktjsx") }}）</span
            >
          </div>
          <el-button
            class="shaco_btn"
            @click="showisAddGuidBox()"
            v-if="page.tableData.length < 3"
            >{{ $t("DemoPage.tableExamplePage.add_Guid") }}</el-button
          >
        </div>
        <div
          :style="
            page.tableData.length <= 0
              ? `padding: 0 20px 40px 20px`
              : 'padding: 0 20px 0px 20px'
          "
        >
          <el-table
            :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
            v-loading="page.loading"
            :data="page.tableData"
            ref="table"
            class="el-table"
         
            :height="tabPosition === '2' ? `520` : `520`"
            :style="
              page.tableData.length <= 0
                ? `border-bottom: 1px solid #E9ECEF;`
                : ''
            "
            :header-cell-style="{
              background: '#F4F7F9',
              height: '50px',
            }"
          >
            <template slot="empty">
              <div class="empty">
                <img src="../../../assets/imgs/empty.png" alt="" />
                <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
              </div>
            </template>
            <el-table-column :label="$t('terminalConfig.xh')" align="center">
              <template slot-scope="scope">
                {{ scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('terminalConfig.tp')" align="center">
              <template slot-scope="scope">
                <img :src="scope.row.image" alt="" @error="handleErrorImg" style="width: 80px; height: 80px; object-fit: cover; border-radius: 4px;" />
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('terminalConfig.pxh')"
              prop="sort"
              align="center"
            ></el-table-column>
            <el-table-column
              :label="$t('terminalConfig.tjsj')" 
              align="center"
            >
              <template slot-scope="scope">
                {{ scope.row.add_date | dateFormat }}
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('terminalConfig.cz')" 
              align="center"
            >
              <template slot-scope="scope">
                <div class="OP-button">
                  <div class="OP-button-top">
                    <el-button
                      icon="el-icon-edit-outline"
                      @click="loadGuid(scope.row.id)"
                      >{{ $t("DemoPage.tableExamplePage.edit") }}
                    </el-button>
                    <el-button
                      icon="el-icon-delete"
                      @click="Delete(scope.row.id)"
                      >{{ $t("DemoPage.tableExamplePage.delete") }}
                    </el-button>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 编辑引导图弹框 -->
    <div class="dialog-block">
      <el-dialog
        :title="$t('terminalConfig.bjydt')"
        :visible.sync="dialogVisible"
        :before-close="close"
        width="500px"
      >
        <el-form
          :model="guidForm"
          :rules="rules"
          ref="ruleForm1"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="pass-input">
            <el-form-item :label="$t('terminalConfig.ydt')" prop="image">
              <l-upload
                :limit="1"
                v-model="guidForm.image"
                ref="upload1"
                :text="$t('terminalConfig.jy750')"
                :mask_layer="false"
              ></l-upload>
            </el-form-item>
            <el-form-item :label="$t('terminalConfig.xh')">
              <el-input
                v-model="guidForm.sort"
                type="number"
                @input="exgNumber(1)"
                :placeholder="$t('terminalConfig.qsrxh')"
                style="width: 360px"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footers">
            <el-form-item>
              <el-button @click="cancel('ruleForm1', 1)" class="shaco_color">{{
                $t("terminalConfig.ccel")
              }}</el-button>
              <el-button
                type="primary"
                @click="saveGuid(guidForm.id, 'ruleForm1', 1)"
                class="qdcolor"
                >{{ $t("terminalConfig.okk") }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>

    <!-- 添加引导图弹框 -->
    <div class="dialog-block">
      <el-dialog
        :title="$t('terminalConfig.tjydt')"
        :visible.sync="isAddGuidBox"
        :before-close="close2"
        width="500px"
      >
        <el-form
          :model="guidFormAdd"
          :rules="rules"
          ref="ruleForm2"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="pass-input">
            <el-form-item :label="$t('terminalConfig.ydt')" prop="image">
              <l-upload
                :limit="1"
                v-model="guidFormAdd.image"
                ref="upload2"
                :text="$t('terminalConfig.jy750')"
                :mask_layer="false"
              ></l-upload>
            </el-form-item>
            <el-form-item :label="$t('terminalConfig.xh')">
              <el-input
                v-model="guidFormAdd.sort"
                type="number"
                @input="exgNumber(2)"
                :placeholder="$t('terminalConfig.qsrxh')"
                style="width: 360px"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footers">
            <el-form-item>
              <el-button @click="cancel('ruleForm2', 2)" class="shaco_color">{{
                $t("terminalConfig.ccel")
              }}</el-button>
              <el-button
                type="primary"
                @click="saveGuid(null, 'ruleForm2', 2)"
                class="qdcolor"
                >{{ $t("terminalConfig.okk") }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import pageData from "@/api/constant/page";
import { index, save } from "@/api/mall/terminalConfig";
import { isEmpty } from "element-ui/src/utils/util";
import { del } from "@/api/mall/aftersaleAddress/aftersaleAddress";
import ErrorImg from "@/assets/images/default_picture.png";

export default {
  name: "wechatMiniProgram",
  props: {
    tabPosition: {
      type: String,
      default: "1",
    },
  },
  data() {
    return {
      language: "",
      page: pageData.data(), // 分页相关数据
      dialogVisible: false, // 编辑弹框显示状态
      isAddGuidBox: false, // 添加弹框显示状态
      // 编辑引导图表单
      guidForm: {},
      // 添加引导图表单
      guidFormAdd: { type: "1", image: null, sort: null },
      // 表单校验规则
      rules: {
        image: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrydt"),
            trigger: "change",
          },
        ],
      },
      ErrorImg, // 图片错误占位图
    };
  },
  watch: {
    // 监听添加引导图的图片变化，清除校验
    "guidFormAdd.image": {
      handler: function () {
        if (this.guidFormAdd.image !== null) {
          this.$refs["ruleForm2"]?.clearValidate();
        }
      },
    },
    // 监听编辑引导图的图片变化，清除校验
    "guidForm.image": {
      handler: function () {
        if (this.guidForm.image !== null) {
          this.$refs["ruleForm1"]?.clearValidate();
        }
      },
    },
  },
  created() {
    this.language = this.getCookit(); // 获取语言配置
    this.loadData(); // 加载引导图列表
  },
  methods: {
    // 安全清空 l-upload 组件文件列表（l-upload 的 fileList 是数组）
    clearUploadRef(refName) {
      const uploadRef = this.$refs[refName];
      if (!uploadRef) return;
      if (Array.isArray(uploadRef.fileList)) {
        uploadRef.fileList = [];
      }
    },

    // 获取cookie中的语言配置
    getCookit() {
      let myCookie = document.cookie.split(";").map((item) => {
        let arr = item.split("=");
        return { name: arr[0].trim(), value: arr[1] };
      });
      let strCookit = "";
      myCookie.forEach((item) => {
        if (item.name.indexOf("language") !== -1) {
          strCookit = item.value;
        }
      });
      return strCookit;
    },

    // 图片加载错误处理
    handleErrorImg(e) {
      e.target.src = this.ErrorImg;
    },

    // 加载引导图列表数据
    async loadData() {
      this.page.loading = true;
      try {
        const res = await index({
          api: "admin.terminal.index",
          type: this.tabPosition,
          storeType: this.tabPosition,
        });
        if (!isEmpty(res) && Number(res.data.code) === 200) {
          const data = res.data.data;
          this.page.tableData = data.guide_list || [];
          this.page.total = data.guide_total || 0;
        }
      } catch (err) {
        console.error("加载引导图列表失败：", err);
        this.$message.error(this.$t("zdata.jzsb"));
      } finally {
        this.page.loading = false;
        // 分页数据处理
        if (this.page.tableData.length < 10) {
          this.page.current_num = this.page.total;
        }
        if (this.page.total < this.page.current_num) {
          this.page.current_num = this.page.total;
        }
        this.page.showPagebox = true;
      }
    },

    // 显示添加引导图弹框
    showisAddGuidBox() {
      this.isAddGuidBox = true;
      // 重置表单
      this.guidFormAdd = { type: "1", image: null, sort: null };
      this.$nextTick(() => {
        this.clearUploadRef("upload2");
        this.$refs["ruleForm2"]?.clearValidate();
      });
    },

    // 处理排序号（只能是整数）
    exgNumber(type) {
      const form = type === 1 ? this.guidForm : this.guidFormAdd;
      if (form.sort < 0) {
        form.sort = 0;
      }
      form.sort = Number(form.sort);
      // 验证是否为整数
      if (form.sort !== Math.floor(form.sort)) {
        this.$message.warning(this.$t("terminalConfig.pxhbnwxs"));
        form.sort = Math.floor(form.sort);
      }
    },

    // 删除引导图
    Delete(id) {
      this.$confirm(
        this.$t("terminalConfig.scts"),
        this.$t("terminalConfig.ts"),
        {
          confirmButtonText: this.$t("terminalConfig.okk"),
          cancelButtonText: this.$t("terminalConfig.ccel"),
          type: "warning",
        }
      )
        .then(async () => {
          try {
            const formData = new FormData();
            formData.append("api", "admin.weixinApp.delWeiXinGuideImage");
            formData.append("type", this.tabPosition);
            formData.append("id", id);
            const res = await del(formData);
            if (Number(res.data.code) === 200) {
              this.$message.success(this.$t("terminalConfig.shanchuchengg"));
              this.demand(); // 刷新列表
            }
          } catch (err) {
            console.error("删除引导图失败：", err);
            this.$message.error(this.$t("zdata.scsb"));
          }
        })
        .catch(() => {});
    },

    // 加载编辑引导图数据
    async loadGuid(id) {
      this.dialogVisible = true;
      try {
        const res = await index({
          api: "admin.weixinApp.getWeiXinGuideImageInfo",
          id,
          storeType: this.tabPosition,
        });
        if (!isEmpty(res) && Number(res.data.code) === 200) {
          const data = res.data.data;
          this.guidForm = { ...data.list[0], type: data.list[0]?.type || "1" };
        }
      } catch (err) {
        console.error("加载编辑引导图数据失败：", err);
        this.$message.error(this.$t("zdata.jzsb"));
      }
    },

    // 保存引导图（添加/编辑）
    async saveGuid(id, ruleForm, uploadType) {
      // 验证排序号
      const form = id ? this.guidForm : this.guidFormAdd;
      form.sort = Number(form.sort) || 0;
      if (form.sort !== Math.floor(form.sort)) {
        this.$message.warning(this.$t("zdata.pxbnwxs"));
        form.sort = Math.floor(form.sort);
        return;
      }

      // 表单校验
      this.$refs[ruleForm].validate(async (valid) => {
        if (!valid) return;

        try {
          const formData = new FormData();
          formData.append("api", "admin.weixinApp.addWeiXinGuideImage");
          formData.append("storeType", this.tabPosition);

          if (id) {
            // 编辑模式
            formData.append("id", id);
            formData.append("type", parseInt(form.type));
            formData.append("sort", form.sort);
            formData.append("imgUrl", form.image);
          } else {
            // 添加模式
            formData.append("type", parseInt(form.type));
            formData.append("sort", form.sort);
            formData.append("imgUrl", form.image);
          }

          const res = await save(formData);
          if (Number(res.data.code) === 200) {
            this.$message.success(
              id ? this.$t("terminalConfig.bjcg") : this.$t("terminalConfig.tianjiachengg")
            );
            this.demand(); // 刷新列表
            // 关闭弹框并重置
            id ? (this.dialogVisible = false) : (this.isAddGuidBox = false);
            this.guidForm = {};
            this.guidFormAdd = { type: "1", image: null, sort: null };
            this.clearUploadRef(`upload${uploadType}`);
          }
        } catch (err) {
          console.error("保存引导图失败：", err);
          this.$message.error(this.$t("zdata.bcsb"));
        }
      });
    },

    // 关闭添加弹框
    close2() {
      this.isAddGuidBox = false;
      this.guidFormAdd = { type: "1", image: null, sort: null };
      this.clearUploadRef("upload2");
      this.$refs["ruleForm2"]?.clearValidate();
    },

    // 关闭编辑弹框
    close() {
      this.dialogVisible = false;
      this.guidForm = {};
      this.clearUploadRef("upload1");
      this.$refs["ruleForm1"]?.clearValidate();
    },

    // 取消弹框
    cancel(val, uploadType) {
      this.$refs[val]?.clearValidate();
      this.isAddGuidBox = false;
      this.dialogVisible = false;
      this.clearUploadRef(`upload${uploadType}`);
      if (uploadType === 1) {
        this.guidForm = {};
      } else {
        this.guidFormAdd = { type: "1", image: null, sort: null };
      }
    },

    // 刷新列表
    demand() {
      this.page.loading = true;
      this.loadData().then(() => {
        this.page.loading = false;
      });
    },
  },
};
</script>

<style scoped lang="less">

.dialog-block {
  /deep/ .el-dialog__body {
    padding: 0 !important;
  }

  .pass-input {
    padding: 20px !important;
  }

  .form-footers {
    width: 100% !important;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    border-top: 0.0625rem solid #e9ecef;
    padding: 20px;
    .el-form-item {
      padding-right: 20px;
    }
  }
}

// 操作按钮样式
.OP-button {
  display: flex;
  justify-content: center;
  .OP-button-top {
    display: flex;
    gap: 10px;
    el-button {
      padding: 6px 12px;
      font-size: 14px;
    }
  }
}

// 空状态样式
.empty {
  text-align: center;
  padding: 40px 0;
  img {
    width: 80px;
    height: 80px;
    margin-bottom: 16px;
  }
}

// 适配上传组件样式
/deep/ .l-upload {
  width: 100%;
}

// 标题栏样式
.title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e9ecef;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
}
</style>
