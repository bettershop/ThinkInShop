<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <!-- <div class="query-input">
            <el-input size="medium" v-model="search.mchName" class="Search-input" :placeholder="$t('orderSettlementList.qsrdp')"></el-input>
          </div> -->
        <div class="query-input">
          <el-input
            size="medium"
            v-model="search.fileName"
            class="Search-input"
            :placeholder="$t('bulkList.qsrbh')"
          ></el-input>

          <el-select
            class="select-input"
            v-model="search.status"
            :placeholder="$t('bulkList.qxzzt')"
          >
            <el-option :label="$t('bulkList.fhcg')" value="1">
              <div>{{ $t("bulkList.fhcg") }}</div>
            </el-option>
            <el-option :label="$t('bulkList.fhsb')" value="0">
              <div>{{ $t("bulkList.fhsb") }}</div>
            </el-option>
          </el-select>
        </div>

        <div class="query-inputs">
          <el-date-picker
            v-model="search.time"
            type="datetimerange"
            :range-separator="$t('reportManagement.businessReport.zhi')"
            :start-placeholder="$t('reportManagement.businessReport.ksrq')"
            value-format="yyyy-MM-dd HH:mm:ss"
            :end-placeholder="$t('reportManagement.businessReport.jsrq')"
          >
          </el-date-picker>
        </div>

        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button
            class="bgColor"
            type="primary"
            @click="demand"
            v-enter="demand"
            >{{ $t("DemoPage.tableExamplePage.demand") }}</el-button
          >
          <!-- <el-button class="bgColor export" @click="isExportBox=!isExportBox">{{$t('DemoPage.tableExamplePage.export')}}</el-button> -->
        </div>
      </div>
    </div>

    <div class="jump-list">
      <el-button
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="printAdd()"
        >{{ $t("bulkList.plfh") }}</el-button
      >
    </div>

    <div class="merchants-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="page.loading"
        :data="page.tableData"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
      >
        <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
          </div>
        </template>
        <el-table-column prop="id" :label="$t('bulkList.bh')" width="210">
        </el-table-column>
        <el-table-column prop="name" :label="$t('bulkList.wjmc')" width="210">
        </el-table-column>
        <el-table-column
          prop="order_num"
          :label="$t('bulkList.fhds')"
          width="210"
        >
        </el-table-column>
        <el-table-column
          prop=" "
          :label="$t('bulkList.zt')"
          width="210"
        >
          <template slot-scope="scope"  >
              {{   getOrderStatusText(scope.row.status)}}
            </template>
        </el-table-column>
        <el-table-column prop="text" :label="$t('bulkList.bz')" width="210">
        </el-table-column>

        <el-table-column :label="$t('bulkList.zxsj')" width="210">
          <template slot-scope="scope">
            {{ scope.row.add_date | dateFormat }}
          </template>
        </el-table-column>

        <el-table-column
          fixed="right"
          :label="$t('orderSettlementList.cz')"
          width="200"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button @click="Del(scope.row.id)">{{
                  $t("orderSettlementList.shanchu")
                }}</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pageBox" ref="pageBox" v-if="page.showPagebox">
        <div class="pageLeftText">
          {{ $t("DemoPage.tableExamplePage.show") }}
        </div>
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
          <div class="pageRightText">
            {{ $t("DemoPage.tableExamplePage.on_show") }}{{ currpage }}-{{
              current_num
            }}{{ $t("DemoPage.tableExamplePage.twig") }} {{ total }}
            {{ $t("DemoPage.tableExamplePage.twig_notes") }}
          </div>
        </el-pagination>
      </div>
    </div>

    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('bulkList.plfh')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <div>
          <div>
            <div>
              <div class="operation-prompt">
                <div class="logo">
                  <img src="@/assets/imgs/czts.png" alt="" />
                  <a href="javascript:;" @click="mychange"
                    >{{ $t("bulkList.czsm") }}
                    <span
                      style="display: inline-block"
                      :style="{ transform: showmore ? '' : '' }"
                      >></span
                    ></a
                  >
                </div>
                <div class="prompt-content" v-show="showmore">
                  <p>{{ $t("bulkList.plfhm") }}</p>
                  <p>
                    ① {{ $t("bulkList.yx")
                    }}<a href="./batchShipment.xlsx">{{
                      $t("bulkList.xzmb")
                    }}</a
                    >{{ $t("bulkList.ambyq") }}
                  </p>
                  <p>② {{ $t("bulkList.zxcks") }}</p>
                </div>
              </div>
            </div>
            <div class="updiv">
              <div class="updiv_r">
                <span style="color: red">*</span>{{ $t("bulkList.scmb") }}
              </div>
              <div class="uploads_up">
                <el-upload
                  class="upload-demo"
                  :action="actionUrl"
                  :on-success="handleUploadSuccess"
                  :before-upload="handleBeforeUpload"
                  accept=".xlsx,.xls,.csv"
                  multiple
                  :limit="1"
                  :on-exceed="handleExceed"
                  :file-list="fileList"
                >
                  <el-button size="small" type="primary">{{
                    $t("bulkList.scwj")
                  }}</el-button>
                </el-upload>
              </div>
              <!-- JAVA用user_Import.xlsx  PHP用user_Import.csv -->
              <span class="uploads"
                ><a href="./batchShipment.xlsx">{{
                  $t("bulkList.xzplf")
                }}</a></span
              >
              <!-- <span class="uploads"><a href="./batchShipment.csv">{{$t('bulkList.xzplf')}}</a></span> -->
            </div>
          </div>
          <div class="form-footer">
            <div class="el-form-item">
              <div class="el-form-item__content">
                <el-button class="bgColor" @click="handleClose()">{{
                  $t("zdata.off")
                }}</el-button>
                <el-button
                  class="bdColor"
                  type="primary"
                  @click="submitForm()"
                  >{{ $t("zdata.ok") }}</el-button
                >
              </div>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

  <script>
import pageData from "@/api/constant/page";
import { del, save, index, batchDelivery } from "@/api/order/comment";
import { mixinstest } from "@/mixins/index";
import Config from "@/packages/apis/Config";
import { isEmpty } from "element-ui/src/utils/util";
// let actionUrl = Config.baseUrl
export default {
  name: "bulkList",
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      // actionUrl:'',
      actionUrl: Config.baseUrl,
      page: pageData.data(),
      search: {},
      mainData: {},
      //导出弹窗
      isExportBox: false,
      // table高度
      tableHeight: null,

      dialogVisible: false,

      fileList: [],
      myfile: "",
      showmore: false,
    };
  },
  //组装模板
  created() {
    this.loadData();
  },
  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },
  methods: {
    getOrderStatusText(val){
      if(val == 0 ){
        return this.$t('bulkList.fhsb')
      }else{ 
        return this.$t('bulkList.fhcg')
      }
    },
    printAdd() {
      this.dialogVisible = true;
    },
    handleClose(done) {
      this.dialogVisible = false;
    },
    mychange() {
      this.showmore = !this.showmore;
    },

    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
    },
    async loadData() {
      await index({
        api: "admin.order.deliveryList",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        fileName: this.search.fileName,
        status: this.search.status,
        startDate: isEmpty(this.search.time) ? "" : this.search.time[0],
        endDate: isEmpty(this.search.time) ? "" : this.search.time[1],
      }).then((res) => {
        if (!isEmpty(res)) {
          let total = res.data.data.total;
          let data = res.data.data.list;

          this.page.tableData = data;
          this.total = total;
          if (this.total.total < 10) {
            this.current_num = this.total;
          }
          if (this.total < this.current_num) {
            this.current_num = this.total;
          }
          this.page.loading = false;
        }
      });
    },
    submitForm() {
      console.log(this.myfile);
      if (!this.myfile) {
        this.$message({
          type: "error",
          message: this.$t("bulkList.qscwj"),
          offset: 100,
        });
      } else {
        this.$store.commit("loading/SET_LOADING");
        const formData = new FormData();
        formData.append("image", this.myfile);
        batchDelivery(formData).then((res) => {
          if (res.data.code == 200) {
            if (res.data.data == false) {
              this.$message({
                type: "error",
                message: this.$t("bulkList.fhsb"),
                offset: 102,
              });
            } else {
              this.$message({
                type: "success",
                message: this.$t("bulkList.fhcg"),
                offset: 102,
              });
            }
            this.demand();
            this.dialogVisible = false;
            this.$store.commit("loading/SET_LOADING");
          } else {
            this.demand();
            this.dialogVisible = false;
            this.$store.commit("loading/SET_LOADING");
          }
        });
      }
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.page.loading = true;
      this.pageSize = e;
      this.loadData().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.page.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.page.loading = false;
      });
    },
    // 查询
    demand() {
      this.page.showPagebox = false;
      this.page.loading = true;
      this.dictionaryNum = 1;
      this.loadData().then(() => {
        this.page.loading = false;
        // if (this.page.tableData.length > 5) {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.page.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.page.showPagebox = true;
        // }
      });
    },
    //点击上一页，下一页
    handleCurrentChange(e) {
      this.page.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.loadData().then(() => {
        this.current_num =
          this.page.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.page.loading = false;
      });
    },
    // 重置
    reset() {
      this.search = { orderNo: null, mchName: null, type: null, time: null };
    },
    // See(value) {

    // this.$router.push({
    //     path: '/order/orderSettlement/orderSettlementDetail',
    //     query: {
    //     orderNo: value.sNo
    //     },
    // })
    // },
    async Del(id) {
      this.$confirm(
        this.$t("bulkList.sfsc"),
        this.$t("orderSettlementList.ts"),
        {
          confirmButtonText: this.$t("orderSettlementList.okk"),
          cancelButtonText: this.$t("orderSettlementList.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          del({
            api: "admin.order.delDelivery",
            id: id,
          }).then((res) => {
            if (res.data.code == "200") {
              // this.demand();
              this.isFillList();
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
                offset: 102,
              });
            }
          });
        })
        .catch(() => {});
    },
    isFillList() {
      let totalPage = Math.ceil(
        (this.total - this.page.tableData.length) / this.pageSize
      );
      // let totalPage = this.orderList.length
      let dictionaryNum =
        this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum;
      this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum;
      console.log("this.dictionaryNum", this.dictionaryNum, this.pageSize);
      this.loadData(); //数据初始化方法
    },

    handleExceed(files, fileList) {
      this.warnMsg(this.$t("bulkList.dqxzx")// 消息内容
     );
      // this.warnMsg(`当前限制选择 1 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`);
    },
    // 上传成功
    handleUploadSuccess(res) {
      const {code ,message} = res
   
    },
    // 上传之前的处理
    handleBeforeUpload(file) {
      console.log(file);
      this.myfile = file;
    },
  },
};
</script>

  <style scoped lang="less">
@import "../../../../common/commonStyle/form";
.container {
  display: flex;
  flex-direction: column;
  .merchants-list {
    flex: 1;
    .OP-button-top {
      justify-content: center;
    }
  }
  .OP-button-top {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    .el-button {
      width: 88px;
      margin-left: 0 !important;
      &:not(:last-child) {
        margin-bottom: 8px;
      }
    }
  }
  tr td:last-child {
    z-index: -999;
  }
  .jump-list button {
    padding: 0 10px;
  }
  .query-inputs {
    margin-right: 10px;
  }
  .Search-input {
    width: 250px;
  }
  .form-footer {
    width: 100%;
    height: 72px;
    position: absolute;
    bottom: 0;
    right: 0;
    border-top: 1px solid #e9ecef;
    .el-form-item {
      padding: 0 !important;
      height: 100%;
      display: flex;
      justify-content: flex-end;
      margin-right: 17px;
      .el-form-item__content {
        height: 100%;
        line-height: 72px;
        margin: 0 !important;
      }
    }
    .bgColor {
      background-color: #fff;
    }
    .bgColor:hover {
      background-color: #fff;
      color: #2890ff;
      border: 1px solid #2890ff;
    }
  }
  .operation-prompt {
    .logo {
      color: #3e9fff;
      display: flex;
      line-height: 20px;
      margin-bottom: 8px;
      img {
        margin-right: 8px;
      }
      a {
        line-height: 20px;
      }
    }
    .prompt-content {
      background: rgba(233, 244, 255, 0.39);
      border-radius: 2px;
      border-left: 4px solid #50bfff;
      padding: 10px;
      color: #999999;
    }
  }
  /deep/.el-dialog {
    width: 720px;
    height: auto;
    .el-dialog__body {
      height: auto;
      display: block;
      padding: 30px 20px 90px 20px;
    }
  }
  .updiv {
    display: flex;
    line-height: 36px;
    margin-top: 20px;
    .el-upload-list__item-name {
    }
    .uploads_up {
      flex: 1;
    }
    .updiv_r {
      min-width: 80px;
      margin-right: 10px;
    }
    .uploads {
      margin-left: 30px;
    }
  }
}
</style>
