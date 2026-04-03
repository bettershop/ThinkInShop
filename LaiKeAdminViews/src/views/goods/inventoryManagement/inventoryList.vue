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
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
          <el-button v-has-permi="'export'" class="bgColor export" type="primary" @click="dialogShow">{{$t('DemoPage.tableExamplePage.export')}}</el-button>
        </div>
      </div>
	  </div>
    <div class="jump-list">
        <el-button
          class="bgColor laiketui laiketui-add"
          type="primary"
          @click="addInventory"
          >{{ $t("inventoryManagement.pltjkc") }}</el-button
        >
      </div>
    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
      @selection-change="handleSelectionChange"
		  :height="tableHeight">

      <el-table-column
        type="selection"
        width="55">
      </el-table-column>

      <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column fixed="left" prop="id" :label="$t('inventoryManagement.xh')" width="115">
        </el-table-column>
        <el-table-column prop="goods_img" :label="$t('inventoryManagement.spmc')" width="300">
          <template slot-scope="scope">
            <div class="goods-info">
              <img :src="scope.row.imgurl" alt="" @error="handleErrorImg">
              <span style="text-align: left;">{{ scope.row.product_title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('inventoryManagement.zt')">
            <template slot-scope="scope">
              <span class="ststus">{{ scope.row.status == 2 ? $t('inventoryManagement.sjia') : scope.row.status == 3 ? $t('inventoryManagement.xj') : $t('inventoryManagement.dsj')}}</span>
            </template>
        </el-table-column>
        <el-table-column prop="price" :label="$t('inventoryManagement.sj')">
          <template slot-scope="scope">
            <span>{{ laikeCurrencySymbol }}{{ scope.row.price.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="specifications" :label="$t('inventoryManagement.gg')" width="200">
        </el-table-column>
        <el-table-column prop="total_num" :label="$t('inventoryManagement.zkc')">
        </el-table-column>
        <el-table-column prop="num" :label="$t('inventoryManagement.sykc')">
        </el-table-column>
        <el-table-column prop="shop_name" show-overflow-tooltip :label="$t('inventoryManagement.ssdp')">
        </el-table-column>
        <el-table-column prop="upper_shelf_time" :label="$t('inventoryManagement.sjsj')"  width="200">
          <template slot-scope="scope">
            <span v-if="!scope.row.upper_shelf_time"></span>
            <span v-else>{{ scope.row.upper_shelf_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('inventoryManagement.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button v-has-permi="'InventoryDetails'" icon="el-icon-view" @click="inventorydetails(scope.row)">{{$t('inventoryManagement.kcxq')}}</el-button>
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
          <!-- @keyup.native="ruleForm2.addNum = oninput2(ruleForm2.addNum)" -->
          <el-form :model="ruleForm2" :rules="rules2" ref="ruleForm2" label-width="auto" class="demo-ruleForm">
            <el-form-item :label="$t('inventoryManagement.tjkc')" prop="addNum">
              <el-input  v-model="ruleForm2.addNum" @keyup.enter.native="enters" :placeholder="$t('inventoryManagement.qsrzjckl')" onkeyup="value=value.match(/^-?[1-9]\d*$/)||value.match(/-?/)"></el-input>
            </el-form-item>
            <el-form-item :label="$t('inventoryManagement.sykc')">
              <!-- <span>{{ ruleForm2.num }}</span> -->
              <el-input v-model="ruleForm2.num" disabled></el-input>
            </el-form-item>
            <el-form-item :label="$t('inventoryManagement.zkc')" style="margin-bottom: 0;">
              <!-- <span>{{ ruleForm2.total_num }}</span> -->
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
    <!-- 批量添加库存 -->
    <el-dialog
        :title="$t('inventoryManagement.pltjkc')"
        :visible.sync="dialogVisible1"
        :before-close="resetForm"

        width="700px"
        class="stock-box"
      >
        <div class="item stock-form">
          <el-form :model="ruleForm" :rules="rules" ref="ruleForm" inline  label-width="150px">
            <el-form-item :label="$t('inventoryManagement.tjkc')" prop="addStockVal"  >
              <el-input
                v-model="ruleForm.addStockVal"
                :placeholder="$t('inventoryManagement.qsrspkcsl')"
                >
              </el-input>
              <div class="explain">
                <img src="../../../assets/imgs/ts3.png" alt="" />
                <span class="red">{{ $t("membersLists.kcqtfh") }}</span>
              </div>
            </el-form-item>
          </el-form>
        </div>
        <div class="floot-box">
          <el-button @click="resetForm('ruleForm')">{{$t('inventoryManagement.ccel')}}</el-button>
          <el-button type="primary" @click="submitForm('ruleForm')">{{$t('addIntegralGoods.save')}}</el-button>
        </div>
      </el-dialog>

  </div>
</template>

<script>
import inventoryList from '@/webManage/js/goods/inventoryManagement/inventoryList'
export default inventoryList
</script>

<style scoped lang="less">
@import '../../../webManage/css/goods/inventoryManagement/inventoryList.less';
</style>
