<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.fileName"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('bulkDeliveryRecord.qsrwjmc')"
          ></el-input>
          <el-select
            class="select-input"
            v-model="inputInfo.fileStatus"
            :placeholder="$t('bulkDeliveryRecord.qxzwjzt')"
          >
            <el-option
              v-for="item in fileStatus"
              :key="item.value"
              :label="item.label"
              :value="item.value"
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
        <el-table-column prop="id" :label="$t('bulkDeliveryRecord.wjid')" width="200">
        </el-table-column>
        <el-table-column prop="name" :label="$t('bulkDeliveryRecord.wjmc')"> </el-table-column>
        <el-table-column prop="status" :label="$t('bulkDeliveryRecord.wjzt')">
          <template slot-scope="scope">
            <span
              class="status"
              :class="[scope.row.status == 0 ? 'actives' : '']"
              >{{ scope.row.status == 0 ? $t('bulkDeliveryRecord.scsb'): $t('bulkDeliveryRecord.sccg') }}</span
            >
          </template>
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('bulkDeliveryRecord.wcsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="text" :label="$t('bulkDeliveryRecord.sbyy')">
          <template slot-scope="scope">
            <span v-if="scope.row.text">{{ scope.row.text }}</span>
            <span v-else>一</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('bulkDeliveryRecord.cz')" width="150" class="hhh">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-delete" @click="Delet(scope.row)">{{$t('bulkDeliveryRecord.shanchu')}}</el-button>
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
  </div>
</template>
  
  <script>
import { deliverList, delDelivery } from "@/api/goods/goodsList";
import { mixinstest } from "@/mixins/index";
export default {
  name: "bulkDeliveryRecord",
  mixins: [mixinstest],
  data() {
    return {
      fileStatus: [
        {
          value: "1",
          label: this.$t('bulkDeliveryRecord.sccg'),
        },
        {
          value: "0",
          label: this.$t('bulkDeliveryRecord.scsb'),
        },
      ], // 订单状态
      inputInfo: {
        fileName: null,
        fileStatus: null,
        date: null,
      },

      tableData: [],
      loading: false,

      // table高度
      tableHeight: null,

    };
  },

  created() {
    this.deliverList();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    async deliverList() {
      const res = await deliverList({
        api: "admin.goods.uploadRecordList",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        key: this.inputInfo.fileName,
        status: this.inputInfo.fileStatus,
        startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
      });
      console.log(res);
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.loading = false;
      // if (this.total < this.current_num) {
      //   this.current_num = this.total;
      // }
      this.sizeMeth()
    },
    sizeMeth(){
      this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
      this.current_num =
        this.tableData.length === this.pageSize
          ? this.dictionaryNum * this.pageSize
          : this.total
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      console.log(this.$refs.tableFather.clientHeight);
    },

    // 重置
    reset() {
        this.inputInfo.fileName = null
        this.inputInfo.fileStatus = null
        this.inputInfo.date = null
    },

    // 查询
    demand() {
      // this.currpage = 1;
      // this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.deliverList().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },
    isFillList () {
      let totalPage = Math.ceil((this.total - 1) / this.pageSize)
      let dictionaryNum =this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum
      this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum
      this.deliverList() //数据初始化方法
    },
    Delet(value) {
      this.$confirm(this.$t('bulkDeliveryRecord.scts'), this.$t('bulkDeliveryRecord.ts'), {
        confirmButtonText: this.$t('bulkDeliveryRecord.okk'),
        cancelButtonText: this.$t('bulkDeliveryRecord.ccel'),
        type: "warning",
      })
        .then(() => {
          delDelivery({
            api: "admin.goods.delUploadRecord",
            id: value.id,
          }).then((res) => {
            if (res.data.code == "200") {
              this.$message({
                type: "success",
                message: this.$t('zdata.sccg'),
                offset: 102,
              });
              this.isFillList();
            }
          });
        })
        .catch(() => {
          // this.$message({
          //   type: "info",
          //   message: "已取消删除",
          //   offset: 102,
          // });
        });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      // this.current_num = e
      this.pageSize = e;
      this.deliverList().then(() => {
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
      this.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.deliverList().then(() => {
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
    .Search-condition {
      .query-input {
        display: flex;
        margin-right: 10px;
        .Search-input {
          margin-right: 10px;
        }
        .select-input {
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
        .status {
          color: #18C364;
          &.actives {
            color: #ff453d;
          }
        }
      }
      .cell {
        img {
          width: 60px;
          height: 60px;
        }
      }
    }

    /deep/.el-table {
      .add {
        .cell {
          overflow: visible !important;
        }
      }
      .cell {
        .text-renson {
          text-align: left;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2;
        }
      }
      .OP-button {
        .OP-button-top {
          justify-content: center;
        }
      }
    }
  }
}
</style>