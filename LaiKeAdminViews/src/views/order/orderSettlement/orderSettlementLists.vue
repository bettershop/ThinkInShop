<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            size="medium"
            v-model="search.orderNo"
            class="Search-input"
            :placeholder="$t('orderSettlementList.qsrjs')"
          ></el-input>
        </div>
        <div class="query-input">
          <el-input
            size="medium"
            v-model="search.mchName"
            class="Search-input"
            :placeholder="$t('orderSettlementList.qsrdp')"
          ></el-input>
        </div>
        <div class="query-input">
          <el-select
            class="select-input"
            v-model="search.type"
            :placeholder="$t('orderSettlementList.qxzzt')"
          >
            <el-option :label="$t('orderSettlementList.yjs')" value="1">
              <div>{{ $t("orderSettlementList.yjs") }}</div>
            </el-option>
            <el-option :label="$t('orderSettlementList.djs')" value="0">
              <div>{{ $t("orderSettlementList.djs") }}</div>
            </el-option>
          </el-select>
        </div>

        <div class="query-input">
          <el-date-picker
            v-model="search.time"
            type="datetimerange"
            :range-separator="$t('reportManagement.businessReport.zhi')"
            :start-placeholder="$t('reportManagement.businessReport.ksrq')"
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
          <el-button
            v-has-permi="'export'"
            class="bgColor export"
            @click="isExportBox = !isExportBox"
          >
            {{ $t("DemoPage.tableExamplePage.export") }}</el-button
          >
        </div>
      </div>
    </div>
    <div v-if="summary" class="summary-info">
        <span>{{$t("DemoPage.tableExamplePage.ddsl")}} : {{ Number(summary.total || 0 ) }}</span>
        <span>{{$t("DemoPage.tableExamplePage.jsje")}} : {{laikeCurrencySymbol}}{{ Number(summary.allSettlementPrice || 0).toFixed(2) }}</span>
        <span>{{$t("DemoPage.tableExamplePage.yj")}} : {{laikeCurrencySymbol}}{{ Number(summary.commission || 0).toFixed(2) }}</span>
        <span>{{$t("DemoPage.tableExamplePage.tdje")}} : {{laikeCurrencySymbol}}{{ Number(summary.returnMoney || 0).toFixed(2) }}</span>
        <span>{{$t("DemoPage.tableExamplePage.tkje")}} : {{laikeCurrencySymbol}}{{ Number(summary.returnCommission || 0).toFixed(2) }}</span>
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
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
          </div>
        </template>
        <el-table-column
          prop="id"
          :label="$t('orderSettlementList.sddh')"
          width="150"
        >
        </el-table-column>
        <el-table-column
          prop="settlementPrice"
          :label="$t('orderSettlementList.sjje')"
          width="150"
        >
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}}{{ Number(scope.row.settlementPrice|| 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="commission"
          :label="$t('orderSettlementList.yj')"
          width="120"
        >
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}}{{ Number(scope.row.commission|| 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="r_commission"
          :label="$t('orderSettlementList.thyj')"
          width="200"
        >
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}}{{ Number(scope.row.r_commission|| 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="sNo"
          :label="$t('orderSettlementList.ddbh')"
          width="200"
        >

        </el-table-column>
        <el-table-column
          prop="z_price"
          :label="$t('orderSettlementList.ddje')"
          width="130"
        >
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}}{{ Number(scope.row.z_price|| 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="shopName"
          :label="$t('orderSettlementList.dpmc')"
          width="150"
        >
        </el-table-column>
        <el-table-column
          prop="return_money"
          :label="$t('orderSettlementList.tdje')"
          width="130"
        >
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}}{{ Number(scope.row.return_money|| 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="status_name"
          :label="$t('orderSettlementList.jszt')"
          width="150"
        >
          <template slot-scope="scope">
            <span v-if="scope.row.status_name == '已结算'">{{
              $t("orderSettlementList.yjs")
            }}</span>
            <span v-if="scope.row.status_name == '待结算'">{{
              $t("orderSettlementList.djs")
            }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('orderSettlementList.jssj')" width="160">
          <template slot-scope="scope" v-if="scope.row.arrive_time != null">
            {{ scope.row.arrive_time | dateFormat }}
          </template>
        </el-table-column>
        <el-table-column
          prop="z_freight"
          :label="$t('orderSettlementList.yf')"
          width="100"
        >
          <template slot-scope="{ row }">
            {{laikeCurrencySymbol}}{{ Number(row.freight || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="mch_discount"
          :label="$t('orderSettlementList.spyh')"
          width="100"
        >
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}}{{ Number(scope.row.mch_discount|| 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="preferential_amount"
          :label="$t('orderSettlementList.ptyh')"
          width="100"
        >
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}}{{ Number(scope.row.preferential_amount|| 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('orderSettlementList.ddsc')" width="160">
          <template slot-scope="scope">
            {{ scope.row.add_time | dateFormat }}
          </template>
        </el-table-column>

        <el-table-column
          fixed="right"
          :label="$t('orderSettlementList.cz')"
          width="120"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  v-has-permi="'orderSettlementDetails'"
                  icon="el-icon-view"
                  @click="See(scope.row)"
                  >{{ $t("orderSettlementList.ck") }}</el-button
                >
                <el-button
                  v-has-permi="'delOrder'"
                  icon="el-icon-delete"
                  @click="Del(scope.row.id)"
                  >{{ $t("orderSettlementList.shanchu") }}</el-button
                >
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
          :current-page="dictionaryNum"
          @current-change="handleCurrentChange"
          :total="total"
        >
          <div class="pageRightText">
            {{ $t("DemoPage.tableExamplePage.on_show") }}{{ currpage }}-{{
              current_num
            }}{{ $t("DemoPage.tableExamplePage.twig") }}{{ total
            }}{{ $t("DemoPage.tableExamplePage.twig_notes") }}
          </div>
        </el-pagination>
      </div>
    </div>

    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('DemoPage.tableExamplePage.export_data')"
        :visible.sync="isExportBox"
        :before-close="isExportBoxClose"
      >
        <div class="item" @click="exportPage">
          <i class="el-icon-document"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_page") }}</span>
        </div>
        <div class="item item-center" @click="exportAll">
          <i class="el-icon-document-copy"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_all") }}</span>
        </div>
        <div class="item" @click="exportQuery">
          <i class="el-icon-document"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_query") }}</span>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import main from "@/webManage/js/order/orderSettlement/orderSettlementList";
export default main;
</script>

<style scoped lang="less">
@import "../../../common/commonStyle/form";
@import "../../../webManage/css/order/orderSettlement/orderSettlementList.less";
</style>
