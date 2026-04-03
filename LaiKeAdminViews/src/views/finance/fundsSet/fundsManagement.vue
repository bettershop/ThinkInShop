
<template>
    <div class="container">
      <div class="Search">
        <div class="Search-condition">
          <div class="query-input">
            <el-input v-model="inputInfo.userName" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('fundsManagement.qsryhmc')"></el-input>
            <el-select class="select-input" v-model="inputInfo.source" :placeholder="$t('fundsManagement.qxzly')">
            <el-option
              v-for="item in sourceList"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
            <div class="select-date">
              <el-date-picker v-model="inputInfo.date"
                type="datetimerange" :range-separator="$t('reportManagement.businessReport.zhi')"
              :start-placeholder="$t('reportManagement.businessReport.ksrq')"
              :end-placeholder="$t('reportManagement.businessReport.jsrq')"
              value-format="yyyy-MM-dd HH:mm:ss"
                :editable="false">
              </el-date-picker>
            </div>
          </div>
          <div class="btn-list">
            <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
            <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
            <el-button class="bgColor export" type="primary" @click="dialogShow">{{$t('DemoPage.tableExamplePage.export')}}</el-button>
          </div>
        </div>
        </div>

      <div class="menu-list" ref="tableFather">
        <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
            :height="tableHeight" >
            <template slot="empty">
                <div class="empty">
                    <img src="../../../assets/imgs/empty.png" alt="">
                    <p style="color: #414658 ;">{{$t('zdata.zwsj')}}</p>
                </div>
            </template>
          <el-table-column prop="user_id" :label="$t('fundsManagement.yhid')">
          </el-table-column>
          <el-table-column prop="user_name" :label="$t('fundsManagement.yhmc')">
          </el-table-column>
          <el-table-column prop="source" :label="$t('fundsManagement.ly')">
            <template slot-scope="scope">
              {{ scope.row.sourceName }}
              <!-- <span v-if="scope.row.source == 1">{{ $t('fundsManagement.lbList1') }}</span>
              <span v-if="scope.row.source == 7">{{ $t('fundsManagement.lbList2') }}</span>
              <span v-if="scope.row.source == 3">{{ $t('fundsManagement.lbList3') }}</span>
              <span v-if="scope.row.source == 4">{{ $t('fundsManagement.lbList4') }}</span>
              <span v-if="scope.row.source == 5">{{ $t('fundsManagement.lbList5') }}</span>
              <span v-if="scope.row.source == 6||scope.row.source == 8">{{ $t('fundsManagement.lbList6') }}</span>
              <span v-if="scope.row.source == 2">{{ $t('fundsManagement.lbList7') }}</span> -->
            </template>
          </el-table-column>
          <el-table-column prop="money" :label="$t('fundsManagement.ye')">
            <template slot-scope="scope">
              <span>{{laikeCurrencySymbol}} {{ scope.row.money.toFixed(2) }}</span>
            </template>
        </el-table-column>
          <el-table-column prop="Register_data" :label="$t('fundsManagement.zcsj')">
          </el-table-column>
          <el-table-column fixed="right" :label="$t('fundsManagement.cz')" width="200" class="hhh">
            <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-bottom">
                <div class="item view">
                    <el-button icon="el-icon-view" @click="View(scope.row)">{{$t('fundsManagement.ckxq')}}</el-button>
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
  import fundsManagement from '@/webManage/js/finance/withdrawalManage/fundsManagement'
  export default fundsManagement
  </script>

  <style scoped lang="less">
     @import "../../../webManage/css/finance/withdrawalManage/fundsManagement.less";
  </style>
