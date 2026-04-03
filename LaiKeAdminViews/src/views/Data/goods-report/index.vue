<template>
  <div class="iframe-content">
    <div class="shaco_box">
      <div class="chart">
        <div class="chart_box">
          <span class="font_one">{{$t('goodsreport.djsp')}}</span>
          <span class="font_two">{{ countList.userAmount }}</span>
        </div>
      </div>
      <div class="chart">
        <div class="chart_box">
          <span class="font_one">{{$t('goodsreport.djdp')}}</span>
          <span class="font_two">{{ countList.mchAmount }}</span>
        </div>
      </div>
      <div class="chart">
        <div class="chart_box">
          <span class="font_one">{{$t('goodsreport.spfl')}}</span>
          <span class="font_two">{{ countList.productClassAmount }}</span>
        </div>
      </div>
      <div class="chart">
        <div class="chart_box">
          <span class="font_one">{{$t('goodsreport.sppp')}}</span>
          <span class="font_two">{{ countList.brandAmount }}</span>
        </div>
      </div>
    </div>
    <div class="shaco_box">
      <div class="commodity_num">
        <div class="shaco_head">
          <div>
            <span class="font_head">{{$t('goodsreport.spsl')}}</span>
            <span>
              <el-select
                v-model="oneday"
                clearable
                placeholder="请选择"
                class="shaco_select"
                @change="changeShop($event)"
              >
                <el-option
                  v-for="item in commodityNum_List"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-select>
            </span>
          </div>
          <div class="shaco_flex">
            <el-button
              :class="kep == 1 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData(1)"
              >{{$t('goodsreport.az')}}</el-button
            >
            <el-button
              :class="kep == 2 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData(2)"
              >{{$t('goodsreport.ay')}}</el-button
            >
            <el-button
              :class="kep == 3 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData(3)"
              >{{$t('goodsreport.an')}}</el-button
            >
          </div>
        </div>
        <div
          style="width: 100%; height: 87.5%; padding: 24px 24px 24px 24px"
          id="commodity_num"
        ></div>
      </div>
      <div class="commodity_type">
        <div class="shaco_head">
          <div>
            <span class="font_head">{{$t('goodsreport.spzt')}}</span>
            <span>
              <el-select
                v-model="twoday"
                placeholder="请选择"
                class="shaco_select"
                @change="changeStatus($event)"
              >
                <el-option
                  v-for="item in commodityType_List"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-select>
            </span>
          </div>
        </div>
        <div
          style="width: 100%; height: 87.5%; padding: 24px 24px 24px 24px"
          id="commodity_type"
        ></div>
      </div>
    </div>
    <div class="shaco_box">
      <div class="sku_box">
        <div class="shaco_head">
          <div>
            <span class="font_head">{{$t('goodsreport.spgg')}}</span>
          </div>
          <div class="shaco_flex">
            <el-button
              :class="dep == 1 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_two(1)"
              >{{$t('goodsreport.sc')}}</el-button
            >
            <el-button
              :class="dep == 2 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_two(2)"
              >{{$t('goodsreport.dp')}}</el-button
            >
            <el-button
              style="width: 52px"
              :class="dep == 3 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_two(3)"
              >{{$t('goodsreport.gys')}}</el-button
            >
          </div>
        </div>
        <div style="width: 100%; height: 84%; padding: 24px" id="SKU"></div>
      </div>
      <div class="sku_box">
        <div class="shaco_head">
          <div>
            <span class="font_head">{{$t('goodsreport.spxs')}}</span>
          </div>
          <div class="shaco_flex">
            <el-button
              :class="yep == 1 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_three(1)"
              >{{$t('goodsreport.az')}}</el-button
            >
            <el-button
              :class="yep == 2 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_three(2)"
              >{{$t('goodsreport.ay')}}</el-button
            >
            <el-button
              :class="yep == 3 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_three(3)"
              >{{$t('goodsreport.an')}}</el-button
            >
          </div>
        </div>
        <div
          style="width: 100%; height: 84%; padding: 24px"
          id="commodity_all"
        ></div>
      </div>
    </div>
    <div class="shaco_box">
      <div class="inventory_box">
        <div class="shaco_head">
          <div>
            <span class="font_head">{{$t('goodsreport.spkc')}}</span>
          </div>
          <div class="shaco_flex">
            <el-button
              :class="rep == 1 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_four(1)"
              >{{$t('goodsreport.az')}}</el-button
            >
            <el-button
              :class="rep == 2 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_four(2)"
              >{{$t('goodsreport.ay')}}</el-button
            >
            <el-button
              :class="rep == 3 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_four(3)"
              >{{$t('goodsreport.an')}}</el-button
            >
          </div>
        </div>
        <div class="inventory_box_body">
          <div style="width: 40%; height: 100%" id="inventory"></div>
          <div class="listBox" ref="tableFather">
            <el-table
              :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
              v-loading="loading"
              :data="tableData"
              ref="table"
              class="el-table"
              style="width: 100%"
              height="500"
              :height="tableHeight"
            >
            <template slot="empty">
              <div class="empty">
                <img src="@/assets/imgs/empty.png" alt="" />
                <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
              </div>
            </template>
              <el-table-column prop="sort" :label="$t('goodsreport.xh')" width="80">
                <!-- <template slot-scope="scope">
                  <span>{{ scope.$index + 1 }}</span>
                </template> -->
              </el-table-column>
              <el-table-column prop="product_number" :label="$t('goodsreport.bh')" width="80">
              </el-table-column>
              <el-table-column :label="$t('goodsreport.spxx')" width="230">
                <template slot-scope="scope">
                  <div class="img_box">
                    <div style="height: 50px">
                      <img
                        :src="scope.row.img"
                        style="width: 50px; height: 50px"
                      />
                    </div>
                    <div>
                      {{ scope.row.product_title }}
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="config" :label="$t('goodsreport.spgg')" width="220">
                <template slot-scope="scope">
                  <div style="text-align: left;">{{ scope.row.config }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="total_num" :label="$t('goodsreport.zkc')">
              </el-table-column>
              <el-table-column prop="num" :label="$t('goodsreport.sykc')"> </el-table-column>
              <el-table-column prop="warnDate" :label="$t('goodsreport.yjsj')" width="180">
              </el-table-column>
              <!-- <el-table-column prop="instock" :label="入库数量">
              </el-table-column>
              <el-table-column prop="outstock" :label="出库数量">
              </el-table-column> -->
            </el-table>
            <div class="pageBox" ref="pageBox" v-if="showPagebox">
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
      </div>
    </div>
  </div>
</template>

<script>
import main from '@/webManage/js/data/goods-report/index'

export default main
</script>

<style scoped lang="less">
@import '../../../webManage/css/data/goods-report/index.less';
</style>
