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
          <el-select class="select-input" v-model="inputInfo.status" :placeholder="$t('stores.auditList.qxzsg')">
            <el-option v-for="(item,index) in statusList" :key="index" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
          <el-input v-model="inputInfo.storeName" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('stores.auditList.qsrdp')"></el-input>
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
		  :height="tableHeight">
      <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{$t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column fixed="left" prop="id" :label="$t('stores.auditList.dpid')">
        </el-table-column>
        <el-table-column prop="store_info" :label="$t('stores.auditList.dpxx')" width="410">
          <template slot-scope="scope">
            <div class="stores-info">
              <div class="head-img">
                <img :src="scope.row.headimgurl" alt="" @error="handleErrorImg">
              </div>
              <div class="store-info">
                <div class="item name">
                  {{$t('stores.auditList.dpmc')}}：{{ scope.row.name }}
                </div>
                <div class="item grey center">
                  {{$t('stores.auditList.yhid')}}：{{ scope.row.user_id }}
                </div>
                <div class="item grey">
                  {{$t('stores.auditList.ssyh')}}：{{ scope.row.user_name }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="realname" :label="$t('stores.auditList.lxr')">
        </el-table-column>
        <el-table-column prop="tel" :label="$t('stores.auditList.lxdh')">
        </el-table-column>
        <el-table-column prop="add_time" :label="$t('stores.auditList.sqsh')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="review_status" :label="$t('stores.auditList.shzt')">
            <template slot-scope="scope">
              <span class="ststus" :class="[[ scope.row.review_status == 0 ? 'active' : 'actives' ]]">{{ scope.row.review_status == 0 ? $t('stores.auditList.dsh') : $t('stores.auditList.shbtg') }}</span>
            </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('stores.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-view" @click="View(scope.row)">{{$t('stores.auditList.ck')}}</el-button>
                <el-button icon="el-icon-circle-check" v-if="scope.row.review_status == 0 " @click="Through(scope.row)">
                  {{$t('stores.auditList.tg')}}</el-button>

              </div>
              <div class="OP-button-bottom">
                <el-button icon="el-icon-circle-close" v-if="scope.row.review_status == 0" @click="dialogShow2(scope.row)">
                  {{$t('stores.auditList.jj')}}</el-button>
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

    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('stores.auditList.txly')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
          <div class="task-container">
            <el-form-item class="integral" :label="$t('stores.auditList.jjly')" prop="reason">
              <el-input type="textarea" v-model="ruleForm.reason" :placeholder="$t('stores.auditList.qsrjj')"></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button class="ekko_bt" @click="handleClose2()">{{$t('stores.ccel')}}</el-button>
              <el-button class="bdColor" type="primary" @click="submitForm2('ruleForm')">{{$t('stores.okk')}}</el-button>
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
import auditList from '@/webManage/js/plug_ins/stores/auditList'
export default auditList
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/stores/auditList.less';
</style>
