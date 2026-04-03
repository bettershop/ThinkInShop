<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.condition"
            size="medium"
            class="Search-input"
            :placeholder="$t('brandExamine.qsrpp')"
          ></el-input>
          <el-select
            class="select-input"
            v-model="inputInfo.status"
            :placeholder="$t('brandExamine.qxzsp')"
          >
            <el-option :label="$t('brandExamine.dsh')" value="0"></el-option>
            <el-option :label="$t('brandExamine.shtg')" value="1"></el-option>
            <el-option :label="$t('brandExamine.btg')" value="2"></el-option>
          </el-select>

          <el-select
            class="select-input"
            v-model="inputInfo.lang_code"
            :placeholder="$t('qxzyz')"
          >
            <el-option
              v-for="item in languages"
              :key="item.lang_code"
              :label="item.lang_name"
              :value="item.lang_code"
            >
            </el-option>
          </el-select>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand">{{
            $t("DemoPage.tableExamplePage.demand")
          }}</el-button>
        </div>
      </div>
    </div>
    <div class="menu-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="loading"
        :data="tableData"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
      >
      <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column prop="brand_id" :label="$t('brandExamine.bh')" width="88">
        </el-table-column>

        <el-table-column prop="brand_pic" :label="$t('brandExamine.pptp')">
          <template slot-scope="scope">
            <div class="goods-img">
              <img :src="scope.row.brand_pic" alt="" @error="handleErrorImg"/>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="brand_name" :label="$t('brandExamine.ppmc')"> </el-table-column>
        <el-table-column prop="lang_name" :label="$t('yz')"> </el-table-column>
        <el-table-column prop="categoriesName" :label="$t('brandExamine.ssfl')">
        </el-table-column>

        <el-table-column
          prop="upper_shelf_time"
          :label="$t('brandExamine.sqsj')"
          min-width="129"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.brand_time }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="supplier_name" :label="$t('brandExamine.ppss')">
        </el-table-column>
        <el-table-column prop="examineDesc" :label="$t('brandExamine.sqzt')">
          <template slot-scope="scope">
            <span
              class="examine-status"
              :class="{ active: scope.row.examineDesc == '审核通过' }"
              >{{ scope.row.examineDesc }}</span
            >
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('brandExamine.cz')" width="150">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button @click="viewlower(scope.row)">{{$t('brandExamine.ck')}}</el-button>
                <el-button
                  v-if="scope.row.examine == 0"
                  @click="pass(scope.row)"
                  >{{$t('brandExamine.tg')}}</el-button
                >
                <el-button
                  v-if="scope.row.examine == 0"
                  @click="refuse(scope.row)"
                  >{{$t('brandExamine.jj')}}</el-button
                >
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox">
        <div class="pageLeftText">{{$t('DemoPage.tableExamplePage.show')}}</div>
        <el-pagination
          layout="sizes, slot, prev, pager, next"
          :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
          :next-text="$t('DemoPage.tableExamplePage.next_text')"
          @size-change="handleSizeChange"
          :page-sizes="pagesizes"
          :current-page="pagination.page"
          @current-change="handleCurrentChange"
          :total="total"
        >
          <div class="pageRightText">{{$t('DemoPage.tableExamplePage.on_show')}}{{currpage}}-{{current_num}}{{$t('DemoPage.tableExamplePage.twig')}}{{total}}{{ $t('DemoPage.tableExamplePage.twig_notes') }}</div>
        </el-pagination>
      </div>
    </div>

    <div class="dialog-refuse">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('brandExamine.jj')"
        :visible.sync="dialogVisible3"
        :before-close="handleClose3"
      >
        <el-form
          :model="ruleForm3"
          :rules="rules3"
          ref="ruleForm3"
          label-width="80px"
          class="demo-ruleForm"
        >
          <div class="pass-input">
            <el-form-item :label="$t('brandExamine.jjly')" prop="reason">
              <el-input
                autosize="5"
                type="textarea"
                v-model="ruleForm3.reason"
                :placeholder="$t('brandExamine.qsrjjly')"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button @click="handleClose3" class="qxcolor">{{$t('brandExamine.ccel')}}</el-button>
              <el-button
                type="primary"
                @click="determine2('ruleForm3')"
                class="qdcolor"
                >{{$t('brandExamine.okk')}}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

  <script>
import { auditList, examine } from "@/api/goods/brandManagement";
import { mixinstest } from "@/mixins/index";
import request from "@/api/https";
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: "examineList",
  mixins: [mixinstest],
  data() {
    return {
      mytype: "",
      inputInfo: {
        condition: "",
        status: "",
        lang_code: "",
      },
      classList: [],
      brandList: [],

      tableData: [],
      loading: true,

      // 拒绝弹框数据
      dialogVisible3: false,
      id: "",
      ruleForm3: {
        reason: "",
      },
      rules3: {
        reason: [
          { required: true, message: this.$t('brandExamine.qsrjjly'), trigger: "blur" },
        ],
      },

      // table高度
      tableHeight: null,
      ids: null,
      // 语种
      languages: [],
    };
  },

  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
    }

    this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal();

    this.auditLists();
    this.getLanguage();
    this.$store.commit("EMPTY_SUPERIOR");
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    async getLanguage() {
      try {
        const result = await this.LaiKeCommon.getLanguages();
        this.languages = result.data.data;
      } catch (error) {
        console.error('获取语种列表失败:', error);
      }
    },
    // 图片错误处理
    handleErrorImg(e){
      console.log('图片报错了',e.target.src);
      e.target.src=ErrorImg
    },
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
    },
    async auditLists() {
      const res = await auditList({
        api: "admin.supplierBrand.auditList",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        condition: this.inputInfo.condition,
        status: this.inputInfo.status,
        lang_code: this.inputInfo.lang_code,
      });
      console.log(res.data);
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.loading = false;
      if (res.data.data.total < 10) {
        this.current_num = this.total;
      }
    },

    reset() {
      this.inputInfo.condition = "";
      this.inputInfo.status = "";
      this.inputInfo.supplierName = "";
      this.inputInfo.lang_code = "";
    },

    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.auditLists().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    viewlower(value) {
      this.$router.push({
        name: "brandDetail",
        params: value,
        query: {
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
        },
      });
    },
    pass(e) {
      request({
        method: "post",
        params: {
          api: "admin.supplierBrand.examine",
          id: e.brand_id,
          status: "1",
        },
      }).then((res) => {
        if (res.data.code == 200) {
          this.$message({
            type: "success",
            message: this.$t('zdata.tgcga'),
            offset:120,
          });
          this.auditLists();
        } else {
        }
      });
    },
    Edit(e) {
      this.$prompt(this.$t('brandExamine.jjly'), this.$t('brandExamine.jj'), {
        confirmButtonText: this.$t('brandExamine.okk'),
        cancelButtonText: this.$t('brandExamine.ccel'),
      })
        .then(({ value }) => {
          request({
            method: "post",
            params: {
              api: "admin.supplierBrand.examine",
              id: e.brand_id,
              status: "2",
              remark: value,
            },
          }).then((res) => {
            if (res.data.code == 200) {
              this.$message({
                type: "success",
                message: this.$t('brandExamine.cg'),
                offset: 100,
              });
              this.auditLists();
            } else {
            }
          });
        })
        .catch(() => {});
    },

    // 拒绝弹框方法
    refuse(value) {
      this.id = value.brand_id;
      this.ruleForm3.reason = "";
      this.dialogVisible3 = true;
    },

    handleClose3(done) {
      this.dialogVisible3 = false;
      this.$refs.ruleForm3.resetFields();
    },

    // 拒绝
    determine2(formName3) {
      this.$refs[formName3].validate(async (valid) => {
        if (valid) {
          try {
            if (this.ruleForm3.reason.length > 200) {
              this.$message({
                message: "拒绝理由长度不能大于200个字符",
                type: "error",
                offset: 100,
              });
              return;
            }
            examine({
              api: "admin.supplierBrand.examine",
              id: this.id,
              status: "2",
              remark: this.ruleForm3.reason,
            }).then((res) => {
              console.log(res);
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t('zdata.jjcga'),
                  type: "success",
                  offset: 120,
                });
                this.auditLists();
                this.dialogVisible3 = false;
              }
            });
          } catch (error) {}
        } else {
        }
      });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      this.pageSize = e;
      this.auditLists().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.loading = false;
      });
    },
    //点击上一页，下一页
    handleCurrentChange(e) {
      if (e > 1) {
        this.ids = this.tableData[this.tableData.length - 1].id;
      }
      this.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.auditLists().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },
  },
};
</script>

  <style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
  /deep/.Search {
    .select-input {
      margin-right: 10px;
    }
    .Search-condition {
      .query-input {
        .Search-input {
          margin-right: 10px;
        }
      }
    }
  }

  .menu-list {
    flex: 1;
    background: #ffffff;
    border-radius: 4px;
    /deep/.el-table__header {
      thead {
        tr {
          th {
            height: 61px;
            text-align: center;
            font-size: 14px;
            font-weight: bold;
            color: #414658;
          }
        }
      }
    }
    /deep/.el-table__body {
      tbody {
        tr {
          td {
            height: 92px;
            text-align: center;
            font-size: 14px;
            color: #414658;
            font-weight: 400;
          }
        }
      }
      .cell {
        .examine-status {
          &.active {
            color: #00be5f !important;
          }
        }
        .goods-img {
          display: flex;
          align-items: center;
          justify-content: center;
          img {
            width: 80px;
            height: 80px;
          }
        }
      }
    }

    /deep/.el-table {
      .OP-button {
        .OP-button-top {
          margin-bottom: 8px;
          display: flex;
          flex-direction: column;
          align-items: center;
          button {
            margin-left: 0;
            &:not(:first-child) {
              margin-top: 8px;
            }
          }
        }
      }
    }
  }

  .dialog-refuse {
    /* 弹框样式 */
    /deep/.el-dialog {
      width: 580px;
      height: 400px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      .el-dialog__header {
        width: 100%;
        height: 58px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 1px solid #e9ecef;
        box-sizing: border-box;
        margin: 0;
        padding: 0 0 0 19px;
        .el-dialog__headerbtn {
          font-size: 18px;
          top: 0 !important;
        }
        .el-dialog__title {
          font-weight: normal;
          font-size: 16px;
          color: #414658;
        }
      }

      .el-dialog__body {
        padding: 41px 60px 16px 60px !important;
        .pass-input {
          /deep/.demo-ruleForm {
            width: 340px;
          }
          .el-textarea {
            width: 340px;
            height: 193px;
            border-radius: 4px;
            textarea {
              width: 340px;
              height: 193px !important;
              border-radius: 4px;
              padding-top: 9px;
            }
          }
        }
        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          display: flex;
          justify-content: flex-end;
          padding-right: 20px;
          border-top: 1px solid #e9ecef;
          .el-form-item {
            padding: 0 !important;
            height: 100%;
            .el-form-item__content {
              height: 100%;
              line-height: 72px;
              margin: 0 !important;
            }
          }

          .qxcolor {
            color: #6a7076;
            border: 1px solid #d5dbc6;
          }
          .qdcolor {
            background-color: #2890ff;
          }
          .qdcolor {
            background-color: #2890ff;
          }
          .qdcolor:hover {
            opacity: 0.8;
          }
          .qxcolor {
            color: #6a7076;
            border: 1px solid #d5dbe8;
            // margin-left: 14px;
          }
          .qxcolor:hover {
            color: #2890ff;
            border: 1px solid #2890ff;
            background-color: #fff;
          }
        }
      }
    }
    /deep/.el-form-item__label {
      font-weight: normal;
      color: #414658;
    }
  }
}
</style>
