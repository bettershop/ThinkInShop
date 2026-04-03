<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.condition"
            size="medium"
            class="Search-input"
            :placeholder="$t('goodsclassifyExamine.qsrfl')"
          ></el-input>
          <el-select
            class="select-input"
            v-model="inputInfo.status"
            :placeholder="$t('goodsclassifyExamine.qxzsp')"
          >
            <el-option :label="$t('goodsclassifyExamine.dsh')" value="0"></el-option>
            <el-option :label="$t('goodsclassifyExamine.shtg')" value="1"></el-option>
            <el-option :label="$t('goodsclassifyExamine.btg')" value="2"></el-option>
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
          <div class="select-date">
            <el-date-picker
              v-model="inputInfo.date"
              type="datetimerange"
              :range-separator="$t('reportManagement.businessReport.zhi')"
              :start-placeholder="$t('reportManagement.businessReport.ksrq')"
              :end-placeholder="$t('reportManagement.businessReport.jsrq')"
              value-format="yyyy-MM-dd HH:mm:ss"
              :editable="false"
            >
            </el-date-picker>
          </div>
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
        <el-table-column prop="cid" :label="$t('goodsclassifyExamine.bh')" width="88"> </el-table-column>

        <el-table-column prop="img" :label="$t('goodsclassifyExamine.fltp')">
          <template slot-scope="scope">
            <div class="goods-img">
              <img :src="scope.row.img" alt="" @error="handleErrorImg"/>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="pname" :label="$t('goodsclassifyExamine.flmc')"> </el-table-column>
        <el-table-column prop="lang_name" :label="$t('goodsclassifyExamine.flyz')"> </el-table-column>
        <el-table-column prop="level" :label="$t('goodsclassifyExamine.fljb')">
          <template slot-scope="scope">
            <span>{{
              scope.row.level == 0
                ? $t('goodsclassifyExamine.yij')
                : scope.row.level == 1
                ? $t('goodsclassifyExamine.erj')
                : scope.row.level == 2
                ? $t('goodsclassifyExamine.sanj')
                : scope.row.level == 3
                ? $t('goodsclassifyExamine.sij')
                : $t('goodsclassifyExamine.wuj')
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="spname" :label="$t('goodsclassifyExamine.sjfl')"> </el-table-column>
        <el-table-column prop="add_date" :label="$t('goodsclassifyExamine.tjjs')" min-width="129">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="supplier_name" :label="$t('goodsclassifyExamine.flss')">
        </el-table-column>
        <el-table-column prop="examineDesc" :label="$t('goodsclassifyExamine.sqzt')">
          <template slot-scope="scope">
            <span
              class="examine-status"
              :class="{ active: scope.row.examineDesc == '审核通过' }"
              >{{ scope.row.examineDesc }}</span
            >
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('goodsclassifyExamine.cz')" width="150">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button @click="viewlower(scope.row)">{{$t('goodsclassifyExamine.ck')}}</el-button>
                <el-button
                  v-if="scope.row.examine == 0"
                  @click="pass(scope.row)"
                  >{{$t('goodsclassifyExamine.tg')}}</el-button
                >
                <el-button
                  v-if="scope.row.examine == 0"
                  @click="refuse(scope.row)"
                  >{{$t('goodsclassifyExamine.jj')}}</el-button
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
        :title="$t('goodsclassifyExamine.jj')"
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
            <el-form-item :label="$t('goodsclassifyExamine.jjly')" prop="reason">
              <el-input
                autosize="5"
                type="textarea"
                v-model="ruleForm3.reason"
                :placeholder="$t('goodsclassifyExamine.qsrjjly')"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button @click="handleClose3" class="qxcolor">{{$t('goodsclassifyExamine.ccel')}}</el-button>
              <el-button
                type="primary"
                @click="determine2('ruleForm3')"
                class="qdcolor"
                >{{$t('goodsclassifyExamine.okk')}}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

  <script>
import { auditList, audit } from "@/api/goods/goodsClass";
import { mixinstest } from "@/mixins/index";
import request from "@/api/https";
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: "classExamineList",
  mixins: [mixinstest],
  data() {
    return {
      mytype: "",
      inputInfo: {
        condition: "",
        status: "",
        date: "",
        lang_code:'',
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
          { required: true, message: this.$t('goodsclassifyExamine.qsrjjly'), trigger: "blur" },
        ],
      },

      // table高度
      tableHeight: null,
      languages:[],
      ids: null,
    };
  },

  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
    }
    this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal()
    this.auditLists();
    this.$store.commit("EMPTY_SUPERIOR");
    this.getLanguage();
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
        api: "admin.supplierClass.auditList",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        condition: this.inputInfo.condition,
        status: this.inputInfo.status,
        lang_code: this.inputInfo.lang_code,
        startTime: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endTime: this.inputInfo.date ? this.inputInfo.date[1] : null,
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
      this.inputInfo.date = "";
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
      // this.$router.push({
      //   name: "brandDetail",
      //   params: value,
      //   query: {
      //     dictionaryNum: this.dictionaryNum,
      //     pageSize: this.pageSize,
      //   },
      // });
      this.$router.push({
        name: "classDetail",
        params: value,
        query: {
          id: value.cid,
          classlevel: Number(value.level) + 1,
        },
      });
    },
    pass(e) {
      request({
        method: "post",
        params: {
          api: "admin.supplierClass.examine",
          id: e.cid,
          status: "1",
        },
      }).then((res) => {
        if (res.data.code == 200) {
          this.$message({
            type: "success",
            message: this.$t("zdata.tgcga"),
            offset: 120,
          });
          this.auditLists();
        } else {
        }
      });
    },
    Edit(e) {
      this.$prompt(this.$t('goodsclassifyExamine.jjly'), this.$t('goodsclassifyExamine.jj'), {
        confirmButtonText: this.$t('goodsclassifyExamine.okk'),
        cancelButtonText: this.$t('goodsclassifyExamine.ccel'),
      })
        .then(({ value }) => {
          request({
            method: "post",
            params: {
              api: "admin.supplierClass.examine",
              id: e.cid,
              status: "2",
              remark: value,
            },
          }).then((res) => {
            if (res.data.code == 200) {
              this.$message({
                type: "success",
                message: this.$t('goodsclassifyExamine.cg'),
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
      this.id = value.cid;
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
            audit({
              api: "admin.supplierClass.examine",
              id: this.id,
              status: "2",
              remark: this.ruleForm3.reason,
            }).then((res) => {
              console.log(res);
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("zdata.jjcga"),
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
        display: flex;
        margin-right: 10px;
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
            width: 88px;
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
