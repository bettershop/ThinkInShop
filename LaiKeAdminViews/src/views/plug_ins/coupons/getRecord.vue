<template>
  <div class="container">
    <div class="person">
      <span>{{$t('coupons.getRecord.zj') }}{{person}} {{$t('coupons.getRecord.rlqz')}} {{total}} {{$t('coupons.getRecord.zhang')}}</span>
    </div>
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input v-model="inputInfo.keyWord" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('coupons.getRecord.qsryhmc')"></el-input>
          <!-- <el-input v-model="inputInfo.membersName" size="medium" @keyup.enter.native="demand" class="Search-input" placeholder="请输入用户名称"></el-input> -->
          <el-select class="select-input" v-model="inputInfo.type" :placeholder="$t('coupons.getRecord.qxzyhql')">
            <el-option v-for="item in couponsTypeList" :key="item.key" :label="item.value" :value="item.key">
            </el-option>
          </el-select>
          <el-select class="select-input" v-model="inputInfo.state" :placeholder="$t('coupons.getRecord.qxzyhqz')">
            <el-option v-for="item in stateList" :key="item.brand_id" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
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
                <p style="color: #414658 ;">{{$t('zdata.zwsj') }}</p>
            </div>
        </template>
        <el-table-column prop="id" :label="$t('优惠券ID')" width="150">
        </el-table-column>
        <el-table-column prop="user_id" :label="$t('coupons.getRecord.yhid')" width="150">
        </el-table-column>
        <el-table-column prop="user_name" :label="$t('coupons.getRecord.yhmc')" width="200">
        </el-table-column>
        <el-table-column prop="typeName" :label="$t('coupons.getRecord.yhqlx')">
        </el-table-column>
        <el-table-column prop="discount" :label="$t('coupons.getRecord.mzzk')" width="200">
          <template slot-scope="scope">
            <div class="item" v-if="scope.row.money && scope.row.activity_type == 2">
              <span>{{ scope.row.money.toFixed(2) }}</span>
            </div>
            <div class="item" v-else-if="scope.row.activity_type == 3">
              <span>{{ scope.row.discount }}</span>{{$t('coupons.getRecord.zhe')}}
            </div>
            <div class="item" v-else-if="scope.row.activity_type == '会员赠券'">
              <span v-show="scope.row.money !== 0">
                {{ scope.row.money }}
              </span>
            </div>
            <div class="item" v-else>
              <span v-if="scope.row.discount">{{ scope.row.discount }}{{$t('coupons.getRecord.zhe')}}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('coupons.getRecord.zt')" width="130">
          <template slot-scope="scope">
            <span v-if="scope.row.type == 0" style="color:#30c02d;">{{$t('coupons.getRecord.wsy')}}</span>
            <span v-if="scope.row.type == 1">{{$t('coupons.getRecord.syz')}}</span>
            <span v-if="scope.row.type == 2" style="color:#ff2a1f">{{$t('coupons.getRecord.ysy')}}</span>
            <span v-if="scope.row.type == 3" style="color: #7A7A7A;">{{$t('coupons.getRecord.ygq')}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderList" :label="$t('coupons.getRecord.glddh')" width="240">
          <template slot-scope="scope">
            <ul v-if="scope.row.orderList.length != 0">
              <li>
                <span></span>
              </li>
            </ul>
            <ul v-if="scope.row.orderList.length == 1" class="order-no">
              <li>
                <span @click="goOrderList(scope.row.orderList[0])">{{ scope.row.orderList[0] }}</span>
              </li>
            </ul>
            <ul v-if="scope.row.orderList.length > 1" class="order-no">
              <li v-for="(item,index) in scope.row.orderList" :key='index'>
                <span @click="goOrderList(scope.row.orderList[index])">{{ item }}</span>
              </li>
            </ul>
          </template>
        </el-table-column>
        <el-table-column prop="add_time" :label="$t('coupons.getRecord.lqsj')" width="280">
           <template slot-scope="scope">
            <span>{{ scope.row.add_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expiry_time" fixed="right" :label="$t('coupons.getRecord.gqsj')" width="280">
          <template slot-scope="scope">
            <span>{{ scope.row.expiry_time }}</span>
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
    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog :title="$t('DemoPage.tableExamplePage.export_data')" :visible.sync="dialogVisible" :before-close="handleClose">
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
import getRecord from '@/webManage/js/plug_ins/coupons/getRecord'
export default getRecord
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/coupons/getRecord.less';
</style>
