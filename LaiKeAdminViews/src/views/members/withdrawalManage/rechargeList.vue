<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.userName"
            size="medium"
            class="Search-input"
            :placeholder="$t('rechargeList.qsryhmc')"
          ></el-input>
          <el-select
            class="select-input"
            v-model="inputInfo.oType"
            :placeholder="$t('rechargeList.qxzlx')"
          >
            <el-option
              v-for="item in TypeList"
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
            $t('DemoPage.tableExamplePage.reset')
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{
            $t('DemoPage.tableExamplePage.demand')
          }}</el-button>
          <span v-for="(item, index) in button_list" :key="index">
            <el-button
              v-if="item.title == '导出'"
              class="bgColor export"
              type="primary"
              @click="dialogShow"
              >{{
                  $t('DemoPage.tableExamplePage.export')
                }}</el-button
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
              <img src="../../../assets/imgs/empty.png" alt="">
              <p style="color: #414658 ;">{{$t('zdata.zwsj') }}</p>
          </div>
      </template>
        <el-table-column prop="user_id" :label="$t('rechargeList.yhid')"> </el-table-column>
        <el-table-column prop="user_name" :label="$t('rechargeList.yhmc')"> </el-table-column>
        <el-table-column prop="money" :label="$t('rechargeList.czzje')">
          <template slot-scope="scope">
            <span v-if="scope.row.money"
              >￥{{ scope.row.money.toFixed(2) }}</span
            >
          </template>
        </el-table-column>
        <el-table-column prop="source" :label="$t('rechargeList.ly')">
          <template slot-scope="scope">
            <span v-if="scope.row.source == 1">小程序</span>
            <span v-if="scope.row.source == 2">app</span>
            <span v-if="scope.row.source == 3">支付宝小程序</span>
            <span v-if="scope.row.source == 4">头条小程序</span>
            <span v-if="scope.row.source == 5">百度小程序</span>
            <span v-if="scope.row.source == 6">PC端</span>
            <span v-if="scope.row.source == 7">H5</span>
          </template>
        </el-table-column>
        <el-table-column prop="mobile" :label="$t('rechargeList.sjhm')"> </el-table-column>
        <el-table-column prop="type" :label="$t('rechargeList.lx')">
          <template slot-scope="scope">
            <span v-if="scope.row.type == 1">{{$t('rechargeList.yhcz')}}</span>
            <span v-if="scope.row.type == 11">{{$t('rechargeList.xtkk')}}</span>
            <span v-if="scope.row.type == 14">{{$t('rechargeList.qxcz')}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('rechargeList.czsj')"> </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="showPagebox">
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
    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('DemoPage.tableExamplePage.export_data')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <div class="item" @click="exportPage">
          <i class="el-icon-document"></i>
          <span>{{$t('DemoPage.tableExamplePage.export_page')}}</span>
        </div>
        <div class="item item-center" @click="exportAll">
          <i class="el-icon-document-copy"></i>
          <span>{{$t('DemoPage.tableExamplePage.export_all')}}</span>
        </div>
        <div class="item" @click="exportQuery">
          <i class="el-icon-document"></i>
          <span>{{$t('DemoPage.tableExamplePage.export_query')}}</span>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import rechargeList from '@/webManage/js/finance/withdrawalManage/rechargeList'
export default rechargeList
</script>

<style scoped lang="less">
@import '../../../webManage/css/finance/withdrawalManage/rechargeList.less';
</style>
