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
          <el-input v-model="inputInfo.goodsName" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('stores.goodsAudit.qsrsp')"></el-input>
          <el-input v-model="inputInfo.storeName" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('stores.goodsAudit.qsrdp')"></el-input>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
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
        <el-table-column fixed="left" prop="id" :label="$t('stores.goodsAudit.spid')" width="70">
        </el-table-column>
        <el-table-column prop="imgurl" :label="$t('stores.goodsAudit.sptp')">
          <template slot-scope="scope">
            <img :src="scope.row.imgurl" alt="" @error="handleErrorImg">
          </template>
        </el-table-column>
        <el-table-column prop="goods_name" :label="$t('stores.goodsAudit.spmc')">
          <template slot-scope="scope">
            <div class="goods-info">
              <div class="item name">
                <span>{{ scope.row.product_title }}</span>
              </div>
              <!-- <div class="item label" v-if="scope.row.s_type">
                <span v-show="scope.row.s_type.includes('3')" class="recommend">{{$t('stores.goodsAudit.tj')}}</span>
                <span v-show="scope.row.s_type.includes('2')" class="sell">{{$t('stores.goodsAudit.rx')}}</span>
                <span v-show="scope.row.s_type.includes('1')" class="new">{{$t('stores.goodsAudit.xp')}}</span>
              </div> -->
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="mch_id" :label="$t('stores.goodsAudit.dpid')">
        </el-table-column>
        <el-table-column prop="name" :label="$t('stores.goodsAudit.dpmc')">
        </el-table-column>
        <el-table-column prop="pname" :label="$t('stores.goodsAudit.flmc')">
        </el-table-column>
        <el-table-column prop="num" :label="$t('stores.goodsAudit.kc')">
        </el-table-column>
        <el-table-column prop="status_name" :label="$t('stores.goodsAudit.spzt')">
            <!-- <template slot-scope="scope">
              <span class="ststus active">{{ scope.row.mch_status == 1 ? '待审核' : '' }}</span>
            </template> -->
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('stores.goodsAudit.sbsj')" width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
       <el-table-column prop="brand_name" :label="$t('stores.goodsAudit.sppp')">
        </el-table-column>
        <el-table-column prop="price" class="price" :label="$t('stores.goodsAudit.jg')">
          <template slot-scope="scope">
              <span>{{ scope.row.price.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="showName" :label="$t('stores.goodsAudit.xswz')">
          <!-- <template slot-scope="scope">
            <span>{{ scope.row.showAdrNameList[0] == '0' ? $t('stores.goodsAudit.qbsp') : $t('stores.goodsAudit.gwc') }}</span>
          </template> -->
        </el-table-column>
        <el-table-column fixed="right" :label="$t('stores.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-view" @click="View(scope.row)">{{$t('stores.goodsAudit.ck')}}</el-button>
                <el-button icon="el-icon-circle-check" @click="Through(scope.row)">{{$t('stores.goodsAudit.tg')}}</el-button>
              </div>
              <div class="OP-button-bottom">
                <el-button icon="el-icon-circle-close" @click="dialogShow2(scope.row)">{{$t('stores.goodsAudit.jj')}}</el-button>
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
        :title="$t('stores.goodsAudit.txly')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <el-form :model="ruleForm" ref="ruleForm" :rules="rules" label-width="100px" class="demo-ruleForm">
          <div class="task-container">
            <el-form-item class="integral" :label="$t('stores.goodsAudit.jjly')" prop="reason">
              <el-input type="textarea" v-model="ruleForm.reason" :placeholder="$t('stores.goodsAudit.qsrjj')"></el-input>
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
  </div>
</template>

<script>
import goodsAudit from '@/webManage/js/plug_ins/stores/goodsAudit'
export default goodsAudit
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/stores/goodsAudit.less';
</style>