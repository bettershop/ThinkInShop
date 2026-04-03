<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input v-model="inputInfo.storeName" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('inventoryManagement.qsrdpmc1')"></el-input>
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
        <el-table-column  prop="goodsId" :label="$t('member.memberGoods.spid')" width="115">
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
        <el-table-column prop="specifications" :label="$t('inventoryManagement.gg')" width="200">
        </el-table-column>
        <el-table-column prop="status" :label="$t('inventoryManagement.zt')">
            <template slot-scope="scope">
              <span class="ststus">{{ scope.row.status == 2 ? $t('inventoryManagement.sjia') : scope.row.status == 3 ? $t('inventoryManagement.xj') : $t('inventoryManagement.dsj')}}</span>
            </template>
        </el-table-column>
        <el-table-column prop="total_num" :label="$t('inventoryManagement.zkc')">
        </el-table-column>
        <el-table-column prop="num" :label="$t('inventoryManagement.sykc')">
          <template #header>
              <p @click="clickSort('num')" class="sales-volume">
                  {{$t('inventoryManagement.sykc')}}
                  <img src="@/assets/imgs/none.png" alt="" v-if="!IsItDescendingOrder" />
                  <img src="@/assets/imgs/asc.png" alt=""  v-else-if="IsItDescendingOrder === 'asc'" />
                  <img src="@/assets/imgs/desc.png" alt="" v-else />
              </p>
            </template>
        </el-table-column>
        <el-table-column prop="name" show-overflow-tooltip :label="$t('inventoryManagement.ssdp')">
        </el-table-column>
        <el-table-column prop="upper_shelf_time" :label="$t('inventoryManagement.sjsj')" width="200">
          <template #header>
              <p @click="clickSort1('upper_shelf_time')" class="sales-volume">
                  {{$t('inventoryManagement.sjsj')}}
                  <img src="@/assets/imgs/none.png" alt="" v-if="!IsItDescendingOrder1" />
                  <img src="@/assets/imgs/asc.png" alt=""  v-else-if="IsItDescendingOrder1 === 'asc'" />
                  <img src="@/assets/imgs/desc.png" alt="" v-else />
              </p>
            </template>
          <template slot-scope="scope">
            <span v-if="!scope.row.upper_shelf_time"></span>
            <span v-else>{{ scope.row.upper_shelf_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('inventoryManagement.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top" :key="index">
                <el-button v-has-permi="'warningRecord'" icon="el-icon-view" @click="warningRecord(scope.row)">{{$t('inventoryManagement.yjjl')}}</el-button>
                <el-button v-has-permi="'addStock'" icon="el-icon-circle-plus-outline" @click="dialogShow2(scope.row)">{{$t('inventoryManagement.tjkc')}}</el-button>
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
        :title="$t('inventoryManagement.tjkc')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <div class="dialog-container">
          <el-form :model="ruleForm2" :rules="rules2" ref="ruleForm2" label-width="auto" class="demo-ruleForm">
            <el-form-item :label="$t('inventoryManagement.tjkc')" prop="addNum">
              <el-input v-model="ruleForm2.addNum" @keyup.native="ruleForm2.addNum = oninput2(ruleForm2.addNum)" :placeholder="$t('inventoryManagement.qsrzjckl')"></el-input>
            </el-form-item>
            <el-form-item :label="$t('inventoryManagement.sykc')">
              <el-input v-model="ruleForm2.num" disabled></el-input>
            </el-form-item>
            <el-form-item :label="$t('inventoryManagement.zkc')">
              <el-input v-model="ruleForm2.total_num" disabled></el-input>
            </el-form-item>
        </el-form>
        </div>
        <div slot="footer" class="form-footer">
           <el-button class="kipColor" @click="handleClose2">{{$t('inventoryManagement.ccel')}}</el-button>
                <el-button class="bdColor" type="primary" @click="determine('ruleForm2')">{{$t('inventoryManagement.okk')}}</el-button>
        </div>
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
import InventoryWarning from '@/webManage/js/goods/inventoryManagement/InventoryWarning'
export default InventoryWarning
</script>

<style scoped lang="less">
@import '../../../webManage/css/goods/inventoryManagement/InventoryWarning.less';
</style>
