<template>
  <div class="container">
    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
		  :height="tableHeight">
        <el-table-column fixed="left" prop="" :label="$t('inventoryManagement.xh')">
          <template slot-scope="scope">
            <span>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="goods_img" :label="$t('inventoryManagement.spmc')" width="230">
          <template slot-scope="scope">
            <div class="goods-info">
              <img :src="scope.row.imgurl" alt="">
              <span style="text-align: left;">{{ scope.row.product_title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" :label="$t('inventoryManagement.sj')" width="230">
          <template slot-scope="scope">
            <span>{{laikeCurrencySymbol}}{{ scope.row.price.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="specifications" :label="$t('inventoryManagement.gg')" width="230">
        </el-table-column>
        <el-table-column prop="status" :label="$t('inventoryManagement.zt')" width="120">
          <template slot-scope="scope">
            <span class="ststus" :class="[ scope.row.status == 2 ? 'active1' : scope.row.status == 3 ? 'active2' : 'active3' ]">{{ scope.row.status == 2 ? $t('inventoryManagement.shangj') : scope.row.status == 3 ? $t('inventoryManagement.xj') : $t('inventoryManagement.dsj')}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="flowing_num" :label="$t('inventoryManagement.rkck')" width="200">
          <template slot-scope="scope">
            <div class="num-Detailds" :class="[ scope.row.type === 0 ? 'active' : 'actives' ]">
              <span v-if="scope.row.type == 0">+</span>
              <span v-if="scope.row.type == 1">-</span>
              <span>{{ scope.row.flowing_num }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('inventoryManagement.crk')" width="200">
          <template slot-scope="scope">
            <span v-if="!scope.row.add_date"></span>
            <span v-else>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" show-overflow-tooltip :label="$t('inventoryManagement.bz')" width="300">
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
  </div>
</template>

<script>
import InventoryDetails from '@/webManage/js/goods/inventoryManagement/InventoryDetails'
export default InventoryDetails
</script>

<style scoped lang="less">
@import '../../../webManage/css/goods/inventoryManagement/InventoryDetails.less';
</style>
