<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.condition"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('ledgerList.qsrddbh')"
          ></el-input>
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
            $t('DemoPage.tableExamplePage.reset')
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{
            $t('DemoPage.tableExamplePage.demand')
          }}</el-button>
          <el-button
            class="bgColor export"
            type="primary"
            @click="dialogShow"
            >{{ $t('DemoPage.tableExamplePage.export') }}</el-button
          >
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
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column prop="order_no" :label="$t('ledgerList.ddbh')" width="200">
        </el-table-column>
        <el-table-column prop="z_price" :label="$t('ledgerList.ddje')">
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}} {{ scope.row.z_price.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="refund_price" :label="$t('ledgerList.tdje')">
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}} {{ scope.row.refund_price.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="account" :label="$t('ledgerList.fzdx')">
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}} {{ scope.row.account.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" :label="$t('ledgerList.fzje')">
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}} {{ scope.row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="z_freight" :label="$t('ledgerList.yf')">
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}} {{ scope.row.yf.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('ledgerList.dpmc')"> </el-table-column>
        <el-table-column prop="add_date" :label="$t('ledgerList.clsj')" width="180">
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('ledgerList.cz')"
          width="200"
          class="hhh"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-bottom">
                <div class="item view">
                  <el-button icon="el-icon-view" @click="View(scope.row)"
                    >{{ $t('ledgerList.ckdd') }}</el-button
                  >
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="showPagebox">
        <div class="pageLeftText">
          {{ $t('DemoPage.tableExamplePage.show') }}
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
            {{ $t('DemoPage.tableExamplePage.on_show') }}{{ currpage }}-{{
              current_num
            }}{{ $t('DemoPage.tableExamplePage.twig') }}{{ total
            }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
          </div>
        </el-pagination>
      </div>
    </div>
    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('DemoPage.tableExamplePage.export_data')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <div class="item" @click="exportPage">
          <i class="el-icon-document"></i>
          <span>{{ $t('DemoPage.tableExamplePage.export_page') }}</span>
        </div>
        <div class="item item-center" @click="exportAll">
          <i class="el-icon-document-copy"></i>
          <span>{{ $t('DemoPage.tableExamplePage.export_all') }}</span>
        </div>
        <div class="item" @click="exportQuery">
          <i class="el-icon-document"></i>
          <span>{{ $t('DemoPage.tableExamplePage.export_query') }}</span>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { management } from '@/api/finance/withdrawalManage'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'

export default {
  name: 'ledgerRecords',
  mixins: [mixinstest],
  data () {
    return {
      inputInfo: {
        condition: null,
        date:null,
      },
      laikeCurrencySymbol:'￥',
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      // 导出弹框数据
      dialogVisible: false
    }
  },

  created () {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    this.axios()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    //初始化分账记录列表数据
    async axios () {
      const res = await management({
        api: 'admin.divideAccount.divideRecord',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        condition: this.inputInfo.condition,
        startDate: this.inputInfo.date?.[0] ?? '',
        endDate: this.inputInfo.date?.[1] ?? ''
      })
      console.log('res:', res)

      if (res.data.code == 200) {
        this.tableData = res.data.data.list
        this.total = res.data.data.total
        this.loading = false
      }
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
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight,'clientHeight')
    },
    // 重置
    reset () {
      this.inputInfo.condition = null
      this.inputInfo.date = null
    },

    // 查询
    demand () {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.axios().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    View (row) {
      this.$router.push({
        path: '/order/orderList/orderLists',
        query: {
          orderNo: row.order_no
        }
      })
    },

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      // this.current_num = e
      this.pageSize = e
      this.axios().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange (e) {
      this.loading = true
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.axios().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },

    // 导出弹框方法
    dialogShow () {
      this.dialogVisible = true
    },

    handleClose (done) {
      this.dialogVisible = false
    },

    async exportPage () {
      exports(
        {
          api: 'admin.divideAccount.divideRecord',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          condition: this.inputInfo.condition,
          startDate: this.inputInfo.date?.[0] ?? '',
          endDate: this.inputInfo.date?.[1] ?? ''
        },
        '分账记录_导出本页'
      )
    },

    async exportAll () {
      exports(
        {
          api: 'admin.divideAccount.divideRecord',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1
        },
        '分账记录_导出全部'
      )
    },

    async exportQuery () {
      exports(
        {
          api: 'admin.divideAccount.divideRecord',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          condition: this.inputInfo.condition,
          startDate: this.inputInfo.date?.[0] ?? '',
          endDate: this.inputInfo.date?.[1] ?? ''
        },
        '分账记录_导出查询'
      )
    }
  }
}

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
          margin-right: 8px;
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
        .status_name {
          width: 58px;
          height: 20px;
          line-height: 20px;
          text-align: center;
          display: block;
          border-radius: 10px;
          font-size: 14px;
          color: #fff;
          &.active {
            background-color: #18c364;
          }
          &.actives {
            background-color: #97a0b4;
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
      .OP-button {
        button {
          margin: 0 4px;
        }
        .OP-button-top {
          margin-bottom: 8px;
          button {
            margin-right: 0px !important;
          }
        }
        .OP-button-bottom {
          display: flex;
          justify-content: center;
          .item2 {
            display: flex;
            justify-content: space-between;
          }
        }
      }
    }
  }

  .dialog-block {
    // 弹框样式
    /deep/.el-dialog {
      width: 460px;
      height: 17.5rem;
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
        border-bottom: 2px solid #e9ecef;
        box-sizing: border-box;
        margin: 0;
        padding: 0 0 0 19px;
        .el-dialog__headerbtn {
          font-size: 18px;
          top: 0 !important;
        }
        .el-dialog__title {
          font-weight: normal;
        }
      }

      .el-dialog__body {
        padding: 0 !important;
        .el-form {
          width: 100%;
          .task-container {
            padding: 41px 0px 0px 0px !important;
            width: 100%;
            .through {
              width: 100%;
              display: flex;
              flex-direction: column;
              justify-content: center;
              align-items: center;
              h3 {
                margin-top: -20px;
              }
              .select-input {
                .el-form-item__label {
                  width: 100px !important;
                }
                .el-form-item__content {
                  margin-left: 100px !important;
                }
              }
              .refund {
                display: flex;
                justify-content: center;
                align-items: center;
                margin-top: 20px;
                .y-refund {
                  margin-right: 20px;
                }
                .s-refund {
                  .el-input {
                    width: 100px;
                    input {
                      padding-right: 0px !important;
                    }
                  }
                }
              }
            }

            .balance,
            .integral,
            .level {
              display: flex;
              justify-content: center;
              .el-form-item__content {
                margin-left: 0px !important;
                .el-input {
                  width: 200px;
                }
              }
            }
          }
        }
        .el-form-item__label {
          font-weight: normal;
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
          .bgColor:hover {
            background-color: #fff;
            color: #2890ff;
            border: 1px solid #2890ff;
          }
        }
      }
    }
  }
}
</style>
