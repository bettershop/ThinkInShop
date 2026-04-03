<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('withdrawalExamineList.txsh')" @click.native.prevent="$router.push('/members/withdrawalManage/withdrawalExamineList')" v-if="handleTabLimits(routerList,'/members/withdrawalManage/withdrawalExamineList')"></el-radio-button>
        <el-radio-button :label="$t('withdrawalExamineList.txjl')" @click.native.prevent="$router.push('/members/withdrawalManage/withdrawalRecordList')" v-if="handleTabLimits(routerList,'/members/withdrawalManage/withdrawalRecordList')"></el-radio-button>
        <el-radio-button :label="$t('withdrawalExamineList.qbcs')" @click.native.prevent="$router.push('/members/withdrawalManage/walletConfig')" v-if="handleTabLimits(routerList,'/members/withdrawalManage/walletConfig')"></el-radio-button>
      </el-radio-group>
    </div>

    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input v-model="page.inputInfo.name" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('withdrawalExamineList.qsryhmc')"></el-input>
          <el-select class="select-input" v-model="page.inputInfo.txType" :placeholder="$t('withdrawalExamineList.qxztxfs')">
            <el-option v-for="(item, index) in sourceList" :key="index" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
          <el-select
            class="select-input"
            v-model="page.inputInfo.status"
            :placeholder="$t('stores.withdrawalRecord.qxzshzt')"
          >
            <el-option
              v-for="(item, index) in statusList"
              :key="index"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-select
            class="select-input"
            v-model="page.inputInfo.dkstatus"
            :placeholder="$t('withdrawalExamineList.qxzdkzt')"
          >
            <el-option
              v-for="(item, index) in dkstatusList"
              :key="index"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <div class="select-date">
            <el-date-picker v-model="page.inputInfo.date"
              type="datetimerange" :range-separator="$t('reportManagement.businessReport.zhi')"
              :start-placeholder="$t('reportManagement.businessReport.ksrq')"
              :end-placeholder="$t('reportManagement.businessReport.jsrq')" value-format="yyyy-MM-dd HH:mm:ss"
            :editable="false">
            </el-date-picker>
          </div>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
          <span v-for="(item,index) in button_list" :key="index">
            <el-button v-if="item.title == '导出'" class="bgColor export" type="primary" @click="dialogShow">{{$t('DemoPage.tableExamplePage.export')}}</el-button>
          </span>
        </div>
      </div>
    </div>

    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="page.loading" :data="page.tableData" ref="table" class="el-table" style="width: 100%"
      :height="tableHeight">
      <template slot="empty">
          <div class="empty">
              <img src="../../../assets/imgs/empty.png" alt="">
              <p style="color: #414658 ;">{{$t('zdata.zwsj') }}</p>
          </div>
      </template>
        <!-- <el-table-column fixed="left" :label="$t('withdrawalExamineList.xh')" min-width="120">
          <template slot-scope="scope">
            <span>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column> -->
        <el-table-column prop="sNo" :label="$t('withdrawalExamineList.txdh')" min-width="130"></el-table-column>
        <el-table-column prop="store_info" :label="$t('withdrawalExamineList.yhmc')" min-width="120">
          <template slot-scope="scope">
            <div class="store-info">
              <div class="store-info">
                {{ scope.row.userName }}
              </div>
            </div>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="sourceName" :label="$t('withdrawalExamineList.ly')">
        </el-table-column> -->
        <el-table-column prop="add_date" :label="$t('withdrawalExamineList.sqsj')" min-width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="money" :label="$t('withdrawalExamineList.txje')" min-width="150">
          <template slot-scope="scope">
            <span>{{ laikeCurrencySymbol }}{{ scope.row.money?(scope.row.money.toFixed(2)):0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="s_charge" :label="$t('withdrawalExamineList.txsxf')" min-width="160">
          <template slot-scope="scope">
            <span>{{ laikeCurrencySymbol }}{{ scope.row.s_charge?(scope.row.s_charge.toFixed(2)):0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="withdraw_status" :label="$t('withdrawalExamineList.txfs')" min-width="170">
          <template slot-scope="scope"  >
            <span v-if="scope.row.withdraw_status==1" @click="openChild(scope.row)">{{$t('withdrawalExamineList.yhk')}}</span>
            <span v-if="scope.row.withdraw_status==2" @click="openChild(scope.row)">{{$t('withdrawalExamineList.wxlq')}}</span>
            <span v-if="scope.row.withdraw_status==3" @click="openChild(scope.row)">{{$t('withdrawalExamineList.bbye')}}</span>
            <span v-if="scope.row.withdraw_status==4" @click="openChild(scope.row)">{{$t('withdrawalExamineList.stripe')}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="Bank_name" :label="$t('withdrawalExamineList.yinhmc')" min-width="100">
        </el-table-column>
        <el-table-column prop="branch" :label="$t('withdrawalExamineList.zhmc')" min-width="120">
        </el-table-column>
        <el-table-column prop="Cardholder" :label="$t('withdrawalExamineList.ckrxm')" min-width="130">
        </el-table-column>
        <el-table-column prop="Bank_card_number" :label="$t('withdrawalExamineList.kh')"  min-width="120">
         <template slot-scope="{row}">
            <span>{{row.Bank_card_number || row.email || row.stripe_account_id}} </span>
          </template>
        </el-table-column>
        <el-table-column prop="mobile" :label="$t('withdrawalExamineList.lxdh')"  min-width="130">
        </el-table-column>
        <el-table-column prop="status" :label="$t('withdrawalExamineList.zt')">
          <template slot-scope="scope">
            <span class="ststus" :class="[[ scope.row.status == 0 ? 'active1' : scope.row.status == 1 ? 'active2' : 'active3' ]]">{{ scope.row.status == 0 ? $t('withdrawalExamineList.dsh') : scope.row.status == 1 ? $t('withdrawalExamineList.shtg') : $t('withdrawalExamineList.yjj')}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="wx_status" :label="$t('打款状态')" min-width="170">
          <template slot-scope="scope"  >
            <span>{{ scope.row.wx_status==1?'进行中':scope.row.wx_status==2?'已完成':scope.row.wx_status==3?'提现失败':''}}</span>
          </template>
        </el-table-column>
        <el-table-column show-overflow-tooltip prop="refuse" :label="$t('withdrawalExamineList.bz')">
        </el-table-column>

      </el-table>
      <div class="pageBox" ref="pageBox" >
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
import main from '@/webManage/js/finance/withdrawalManage/withdrawalRecordList'
export default main
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/stores/withdrawalAudit.less';
@import  '../../../webManage/css/finance/withdrawalManage/withdrawalRecordList.less';
</style>
