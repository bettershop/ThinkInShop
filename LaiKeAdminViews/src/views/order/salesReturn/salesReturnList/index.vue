<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.orderNo"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('salesReturnList.qsrddh')"
          ></el-input>
          <el-input
            v-model="inputInfo.mchName"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('salesReturnList.qsrdpmc')"
          ></el-input>
          <el-select
            class="select-input"
            v-model="inputInfo.retype"
            :placeholder="$t('salesReturnList.qxzlx')"
          >
            <el-option :label="$t('salesReturnList.inputInfoList1')" value="2">
            </el-option>
            <el-option :label="$t('salesReturnList.inputInfoList2')" value="3">
            </el-option>
            <el-option :label="$t('salesReturnList.inputInfoList3')" value="1">
            </el-option>
          </el-select>
          <el-select
            class="select-input"
            v-model="inputInfo.state"
            :placeholder="$t('salesReturnList.qxzzt')"
          >
            <el-option
              v-for="item in stateList"
              :key="item.brand_id"
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
            $t('DemoPage.tableExamplePage.reset')
          }}</el-button>
          <el-button
            class="bgColor"
            type="primary"
            @click="demand"
            v-enter="demand"
            >{{ $t('DemoPage.tableExamplePage.demand') }}</el-button
          >
          <span v-for="(item, index) in button_list" :key="index">
            <el-button
              v-if="item.title == '导出'"
              class="bgColor export"
              type="primary"
              @click="dialogShow"
              >{{ $t('DemoPage.tableExamplePage.export') }}</el-button
            >
          </span>
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
        <el-table-column prop="" :label="$t('salesReturnList.xh')" width="120">
          <template slot-scope="scope">
            <span>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="user_id" :label="$t('salesReturnList.yhid')">
        </el-table-column>
        <el-table-column
          prop="p_name"
          :label="$t('salesReturnList.spmc')"
          width="200"
        >
        </el-table-column>
        <el-table-column
          prop="mchName"
          :label="$t('salesReturnList.dpmc')"
          width="200"
        >
        </el-table-column>
        <el-table-column
          prop="p_price"
          :label="$t('salesReturnList.spjg')"
          width="180"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.currency_symbol }}{{ LaiKeCommon.formatPrice( scope.row.p_price,scope.row.exchange_rate)}} </span>
          </template>
        </el-table-column>
        <el-table-column prop="num" :label="$t('salesReturnList.sl')">
        </el-table-column>
        <el-table-column
          prop="sNo"
          :label="$t('salesReturnList.ddh')"
          width="200"
        >
        </el-table-column>
        <el-table-column
          prop=""
          :label="$t('salesReturnList.stje')"
          width="180"
        >
          <template slot-scope="scope">
            <span v-if="scope.row.real_money > 0">
              {{ scope.row.currency_symbol }}{{ LaiKeCommon.formatPrice( scope.row.real_money,scope.row.exchange_rate)}}
            </span>
            <span v-else>{{ $t('salesReturnList.wtk') }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="re_time"
          :label="$t('salesReturnList.sqsj')"
          width="200"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.re_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="mobile"
          :label="$t('salesReturnList.lx')"
          width="80"
        >
          <template slot-scope="scope">
            <span v-if="scope.row.re_type == 2">{{
              $t('salesReturnList.jtk')
            }}</span>
            <span v-else-if="scope.row.re_type == 1">{{
              $t('salesReturnList.thtk')
            }}</span>
            <span v-else>{{ $t('salesReturnList.hh') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="prompt" :label="$t('salesReturnList.zt')">
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('salesReturnList.cz')"
          width="140"
          class="hhh"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <!-- <template v-if="scope.row.r_type == 3">
                  <el-button v-if="scope.row.re_type<3 && detectionBtn(button_list, '通过')" icon="el-icon-circle-check" @click="dialogShow2(scope.row,1)">通过</el-button>
                    <el-button v-if="scope.row.re_type==3 && detectionBtn(button_list, '回寄')" icon="el-icon-document-checked" @click="View(scope.row,3)">回寄</el-button>
                </template> -->
                <!-- <template v-if="scope.row.r_type == 3">
                  <el-button icon="el-icon-document-checked" @click="View(scope.row,3)">回寄</el-button>
                </template> -->
                <template v-if="!scope.row.isExamine">
                  <el-button
                    v-has-permi="'salesReturnDetails'"
                    v-if="!scope.row.isManExamine"
                    icon="el-icon-view"
                    @click="View(scope.row)"
                    >{{ $t('salesReturnList.ck') }}</el-button
                  >
                </template>

                <template v-if="scope.row.isExamine">
                  <el-button
                    v-has-permi="'refuse'"
                    icon="el-icon-document-checked"
                    @click="View(scope.row, 2)"
                    >{{ $t('salesReturnList.sh') }}</el-button
                  >
                </template>

                <el-button
                  v-has-permi="'manExamine'"
                  v-if="scope.row.isManExamine"
                  icon="el-icon-user"
                  @click="View(scope.row, 4)"
                  >{{ $t('salesReturnList.rgsl') }}</el-button
                >
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
import salesReturnList from '@/webManage/js/order/salesReturn/salesReturnList'
export default salesReturnList
</script>

<style scoped lang="less">
@import '../../../../webManage/css/order/salesReturn/salesReturnList.less';
</style>
