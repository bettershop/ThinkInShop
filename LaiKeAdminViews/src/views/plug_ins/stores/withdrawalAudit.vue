<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('stores.dp')" @click.native.prevent="$router.push('/plug_ins/stores/store')" v-if="handleTabLimits(routerList,'/plug_ins/stores/store')"></el-radio-button>
        <el-radio-button :label="$t('stores.dpfl')" @click.native.prevent="$router.push('/plug_ins/stores/storeFl')" v-if="handleTabLimits(routerList,'/plug_ins/stores/storeFl')"></el-radio-button>
        <el-radio-button :label="$t('stores.dpsh')" @click.native.prevent="$router.push('/plug_ins/stores/auditList')" v-if="handleTabLimits(routerList,'/plug_ins/stores/auditList')"></el-radio-button>
        <el-radio-button :label="$t('stores.bzjjl')" @click.native.prevent="$router.push('/plug_ins/stores/bondMoney')" v-if="handleTabLimits(routerList,'/plug_ins/stores/bondMoney.vue')"></el-radio-button>
        <el-radio-button :label="$t('stores.bzjsh')" @click.native.prevent="$router.push('/plug_ins/stores/bondExamine')" v-if="handleTabLimits(routerList,'/plug_ins/stores/bondExamine.vue')"></el-radio-button>
        <el-radio-button :label="$t('stores.spsh')" @click.native.prevent="$router.push('/plug_ins/stores/goodsAudit')" v-if="handleTabLimits(routerList,'/plug_ins/stores/goodsAudit.vue')"></el-radio-button>
        <el-radio-button :label="$t('stores.txsh')" @click.native.prevent="$router.push('/plug_ins/stores/withdrawalAudit')" v-if="handleTabLimits(routerList,'/plug_ins/stores/withdrawalAudit.vue')"></el-radio-button>
        <el-radio-button :label="$t('stores.txjl')" @click.native.prevent="$router.push('/plug_ins/stores/withdrawalRecord')" v-if="handleTabLimits(routerList,'/plug_ins/stores/withdrawalRecord.vue')"></el-radio-button>
        <!-- <el-radio-button :label="$t('stores.dpsz')" @click.native.prevent="toset()"></el-radio-button> -->
      </el-radio-group>
    </div>

    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.storeName"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('stores.withdrawalAudit.qsrdp')"
          ></el-input>
          <el-select
            class="select-input"
            v-model="inputInfo.txType"
            :placeholder="$t('请选择提现方式')"
          >
            <el-option
              v-for="(item, index) in sourceList"
              :key="index"
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
            <p style="color: #414658">{{$t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column
          fixed="left"
          prop="mch_id"
          :label="$t('stores.auditList.dpid')"
        >
        </el-table-column>
        <el-table-column
          prop="store_info"
          :label="$t('stores.auditList.dpxx')"
          width="410"
        >
          <template slot-scope="scope">
            <div class="store-info">
              <div class="head-img">
                <img :src="scope.row.headimgurl" alt="" @error="handleErrorImg"/>
              </div>
              <div class="store-info">
                <div class="item name">
                  {{ $t('stores.auditList.dpmc') }}：{{ scope.row.mch_name }}
                </div>
                <div class="item grey center">
                  {{ $t('stores.auditList.yhid') }}：{{ scope.row.user_id }}
                </div>
                <div class="item grey">
                  {{ $t('stores.auditList.ssyh') }}：{{ scope.row.userName }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="mobile"
          :label="$t('stores.auditList.lxdh')"
          width="120"
        >
        </el-table-column>
        <el-table-column
          prop="add_date"
          :label="$t('stores.withdrawalAudit.sqsj')"
          width="200"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="money"
          :label="$t('stores.withdrawalAudit.txje')"
        >
          <template slot-scope="scope" v-if="scope.row.money">
            <span
              >{{ scope.row.money.toFixed(2)
              }}{{ $t('stores.withdrawalAudit.yu') }}</span
            >
          </template>
        </el-table-column>
        <el-table-column
          prop="s_charge"
          :label="$t('stores.withdrawalAudit.txsxf')"
          width="120"
        >
          <template slot-scope="scope" v-if="scope.row.s_charge">
            <span
              >{{ scope.row.s_charge.toFixed(2)
              }}{{ $t('stores.withdrawalAudit.yu') }}</span
            >
          </template>
        </el-table-column>
        <el-table-column prop="withdraw_status" :label="$t('stores.withdrawalAudit.txfs')" min-width="170">
          <template slot-scope="scope"  >
            <span>{{ scope.row.withdraw_status==1?$t('stores.withdrawalAudit.yhk'):scope.row.withdraw_status==2?$t('stores.withdrawalAudit.wxlq'):$t('stores.withdrawalAudit.bbye')}}</span>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="status" :label="$t('stores.withdrawalAudit.shzt')">
            <template slot-scope="scope">
              <span class="ststus" :class="[[ scope.row.status == 0 ? 'active1' : scope.row.status == 1 ? 'active2' : 'active3' ]]">{{ scope.row.status == 0 ? $t('stores.withdrawalAudit.dsh') : scope.row.status == 1 ? $t('stores.withdrawalAudit.shtg') : $t('stores.withdrawalAudit.yjj')}}</span>
            </template>
        </el-table-column> -->
        <el-table-column
          prop="Cardholder"
          :label="$t('stores.withdrawalAudit.ckrxm')"
          width="130"
        >
        </el-table-column>
        <el-table-column
          prop="Bank_name"
          :label="$t('stores.withdrawalAudit.yhmc')"
          width="100"
        >
        </el-table-column>
        <el-table-column
          prop="branch"
          :label="$t('stores.withdrawalAudit.zhmc')"
          width="100"
        >
        </el-table-column>
        <el-table-column
          prop="card"
          :label="$t('withdrawalExamineList.kh')"
          width="200"
        >

        </el-table-column>
        <el-table-column fixed="right" :label="$t('stores.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  icon="el-icon-circle-check"
                  @click="Through(scope.row)"
                  >{{ $t('stores.withdrawalAudit.tg') }}</el-button
                >
                <el-button
                  icon="el-icon-circle-close"
                  @click="dialogShow2(scope.row)"
                  >{{ $t('stores.withdrawalAudit.jj') }}</el-button
                >
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox">
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

    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('stores.withdrawalAudit.txly')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <el-form
          :model="ruleForm"
          :rules="rules"
          ref="ruleForm"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="task-container">
            <el-form-item
              class="integral"
              :label="$t('stores.withdrawalAudit.jjly')"
              prop="reason"
            >
              <el-input
                type="textarea"
                v-model="ruleForm.reason"
                :placeholder="$t('stores.withdrawalAudit.qsrjj')"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button class="ekko_bt" @click="handleClose2()">{{
                $t('stores.ccel')
              }}</el-button>
              <el-button
                class="bdColor"
                type="primary"
                @click="submitForm2('ruleForm')"
                >{{ $t('stores.okk') }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
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
import withdrawalAudit from '@/webManage/js/plug_ins/stores/withdrawalAudit'
export default withdrawalAudit
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/stores/withdrawalAudit.less';
</style>
