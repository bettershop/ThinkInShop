<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.userName"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('pointManagement.qsryh')"
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
              v-has-permi="'export'"
              class="bgColor export"
              type="primary"
              @click="dialogShow"
              >{{$t('DemoPage.tableExamplePage.export')}}</el-button
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
                    <img src="../../../assets/imgs/empty.png" alt="">
                    <p style="color: #414658 ;">{{$t('zdata.zwsj')}}</p>
                </div>
            </template>
        <el-table-column prop="user_id" :label="$t('pointManagement.yhid')"> </el-table-column>
        <el-table-column prop="user_name" :label="$t('pointManagement.yhmz')"> </el-table-column>
        <el-table-column prop="mobile" :label="$t('pointManagement.sjhm')"> </el-table-column>
        <el-table-column prop="source" :label="$t('pointManagement.ly')">
          <template slot-scope="scope">
            {{ scope.row.sourceName }}
          </template>
        </el-table-column>
        <el-table-column prop="score" :label="$t('pointManagement.syjf')"> </el-table-column>

        <el-table-column prop="Register_data" :label="$t('pointManagement.zcsj')">
        </el-table-column>
        <el-table-column fixed="right" :label="$t('fundsManagement.cz')" width="200" class="hhh">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-bottom">
                <div class="item view">
                  <el-button icon="el-icon-view" @click="View(scope.row)"
                    >{{$t('pointManagement.ckxq')}}</el-button
                  >
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-show="showPagebox">
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
import pointManagement from '@/webManage/js/finance/withdrawalManage/pointManagement'
export default pointManagement
</script>

<style scoped lang="less">
@import '../../../webManage/css/finance/withdrawalManage/pointManagement.less';
</style>
