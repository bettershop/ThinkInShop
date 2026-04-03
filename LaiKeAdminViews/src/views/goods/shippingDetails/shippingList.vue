<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input v-model="inputInfo.storeName" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('inventoryManagement.qsrdpmc')"></el-input>
          <el-input v-model="inputInfo.goodsName" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('inventoryManagement.qsrspmc')"></el-input>
          <el-select
            class="select-input"
            :placeholder="$t('qxzyz')"
            v-model="inputInfo.lang_code"
          >
            <el-option
              v-for="item in languages"
              :key="item.lang_code"
              :label="item.lang_name"
              :value="item.lang_code"
            >
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
          <el-button v-has-permi="'export'" class="bgColor export" type="primary" @click="dialogShow">{{$t('DemoPage.tableExamplePage.export')}}</el-button>
        </div>
      </div>
	  </div>

    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
		  :height="tableHeight">
      <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column fixed="left" prop="attrId" :label="$t('inventoryManagement.xh')" width="115">
        </el-table-column>
        <el-table-column prop="goods_img" :label="$t('inventoryManagement.spmc')" width="300">
          <template slot-scope="scope">
            <div class="goods-info">
              <img :src="scope.row.imgurl" alt="" @error="handleErrorImg">
              <span style="text-align: left;">{{ scope.row.product_title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" :label="$t('inventoryManagement.sj')">
          <template slot-scope="scope">
            <span>{{ laikeCurrencySymbol }}{{ scope.row.price.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="specifications" :label="$t('inventoryManagement.gg')">
        </el-table-column>
        <el-table-column prop="status" :label="$t('inventoryManagement.zt')">
            <template slot-scope="scope">
              <span class="ststus" :class="[ scope.row.status == 2 ? 'active1' : scope.row.status == 3 ? 'active2' : 'active3' ]">{{ scope.row.status == 2 ? $t('inventoryManagement.shangj') : scope.row.status == 3 ? $t('inventoryManagement.xj') : $t('inventoryManagement.dsj')}}</span>
            </template>
        </el-table-column>
        <el-table-column prop="num" :label="$t('inventoryManagement.sykc')">
        </el-table-column>
        <el-table-column prop="flowing_num" :label="$t('inventoryManagement.ckl')">
        </el-table-column>
        <el-table-column prop="name" show-overflow-tooltip :label="$t('inventoryManagement.dpmc')">
        </el-table-column>
        <el-table-column fixed="right" prop="add_date" :label="$t('inventoryManagement.cksj')" width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
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
import shippingDetails from '@/webManage/js/goods/inventoryManagement/shippingDetails'
export default shippingDetails
</script>

<style scoped lang="less">
@import '../../../webManage/css/goods/inventoryManagement/shippingDetails.less';
</style>
