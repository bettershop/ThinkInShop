<template>
  <div class="container">
    <!-- <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('coupons.yhqlb')"
          @click.native.prevent="$router.push('/plug_ins/coupons/couponsList')"></el-radio-button>
        <el-radio-button :label="$t('coupons.yhqsz')"
          @click.native.prevent="$router.push('/plug_ins/coupons/couponsSet')"></el-radio-button>
      </el-radio-group>
    </div> -->

    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.couponsName"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('coupons.couponsList.qsryhq')"
          ></el-input>
          <el-select
            class="select-input"
            v-model="inputInfo.issueUnit"
            :placeholder="$t('coupons.couponsList.qxzfx')"
          >
            <el-option
              v-for="(item, index) in unitList"
              :key="index"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-select
            class="select-input"
            v-model="inputInfo.couponsType"
            :placeholder="$t('coupons.couponsList.qxzyhl')"
          >
            <el-option
              v-for="(item, index) in couponsTypeList"
              :key="index"
              :label="item.value"
              :value="item.key"
            >
            </el-option>
          </el-select>
          <el-select
            class="select-input"
            v-model="inputInfo.receiveType"
            :placeholder="$t('coupons.couponsList.qxzlqf')"
          >
            <el-option
              v-for="(item, index) in typeList"
              :key="index"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-select
            class="select-input"
            v-model="inputInfo.is_overdue"
            :placeholder="$t('coupons.couponsList.sfgq')"
          >
            <el-option
              v-for="(item, index) in overdueList"
              :key="index"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand">{{
            $t("DemoPage.tableExamplePage.demand")
          }}</el-button>
          <el-button
            class="bgColor export"
            type="primary"
            @click="dialogShow"
            >{{ $t("DemoPage.tableExamplePage.export") }}</el-button
          >
        </div>
      </div>
    </div>

    <div class="jump-list">
      <el-button
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="addCoupons"
        >{{ $t("coupons.couponsList.tjyhq") }}</el-button
      >
      <el-button
        style="background: #ffffff"
        icon="el-icon-delete"
        @click="bulkDel()"
        >{{ $t("coupons.couponsList.plsc") }}</el-button
      >
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
        @selection-change="handleSelectionChange"
      >
        <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{$t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column type="selection" width="55"> </el-table-column>
        <el-table-column fixed="left" prop="id" label="ID" width="70">
        </el-table-column>
        <el-table-column
          prop="name"
          :label="$t('coupons.couponsList.yhqmc')"
          width="100"
        >
        </el-table-column>
        <el-table-column
          prop="issueUnit"
          :label="$t('coupons.couponsList.fxdw')"
        >
        </el-table-column>
        <el-table-column
          prop="activity_type"
          :label="$t('coupons.couponsList.yhqlx')"
          width="100"
        >
        </el-table-column>
        <el-table-column prop="type" :label="$t('coupons.couponsList.kyfw')">
        </el-table-column>
        <el-table-column
          prop="circulation"
          :label="$t('coupons.couponsList.fxsl')"
        >
          <template slot-scope="scope">
            <span v-if="scope.row.circulation > 900000">{{
              $t("coupons.couponsList.bxz")
            }}</span>
            <span v-else>{{ scope.row.circulation }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="discount"
          :label="$t('coupons.couponsList.mzzk')"
        >
          <template slot-scope="scope">
            <div
              class="item"
              v-if="scope.row.money && scope.row.activity_type == '满减券'"
            >
              <span>{{ scope.row.money.toFixed(2) }}</span>
            </div>
            <div class="item" v-else-if="scope.row.activity_type == '折扣券'">
              <span>{{ scope.row.discount }}</span
              >折
            </div>
            <div class="item" v-else>
              <span></span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="z_money" :label="$t('coupons.couponsList.xfmk')">
          <template
            slot-scope="scope"
            v-if="scope.row.z_money || scope.row.z_money == 0"
          >
            <span>{{
              scope.row.z_money
                ? scope.row.z_money.toFixed(2)
                : $t("coupons.couponsList.wmk")
            }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="circulation"
          :label="$t('coupons.couponsList.lqfs')"
          width="100"
        >
          <template slot-scope="scope">
            <span v-if="scope.row.pickup_type == 0">{{
              $t("coupons.couponsList.sdlq")
            }}</span>
            <span v-if="scope.row.pickup_type == 1">{{
              $t("coupons.couponsList.zdff")
            }}</span>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="num" :label="剩余数量">
        </el-table-column> -->
        <el-table-column
          prop="expirationDate"
          :label="$t('coupons.couponsList.gqsj')"
          width="200"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.expirationDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="isEnd" :label="$t('coupons.couponsList.sfgq')">
          <template slot-scope="scope">
            <span>{{
              scope.row.isEnd == true
                ? $t("coupons.couponsList.shi")
                : $t("coupons.couponsList.fou")
            }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="is_display"
          :label="$t('coupons.couponsList.sfxs')"
        >
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.is_display"
              @change="switchs(scope.row)"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column
          prop="z_price"
          :label="$t('coupons.couponsList.fxsj')"
          width="200"
        >
          <template slot-scope="scope">
            <div class="valid-time">
              <span>{{ scope.row.add_time }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('coupons.cz')" width="300">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  icon="el-icon-view"
                  @click="View(scope.row)"
                  v-if="
                    scope.row.status == 3
                  "
                >
                  {{ $t("coupons.couponsList.ck") }}</el-button
                >
                <el-button
                  icon="el-icon-edit-outline"
                  @click="Edit(scope.row)"
                  v-if="
                    scope.row.status != 3
                  "
                >
                  {{ $t("coupons.bianji") }}</el-button
                >
                <el-button
                  v-if="
                    scope.row.pickup_type != 1
                  "
                  icon="el-icon-tickets"
                  @click="givingCoupons(scope.row)"
                  >{{ $t("coupons.couponsList.zq") }}</el-button
                >
                <el-button
                  icon="el-icon-delete"
                  @click="Delete(scope.row)"
                  >{{ $t("coupons.shanchu") }}</el-button
                >
              </div>
              <div class="OP-button-bottom">
                <el-button
                  v-if="
                    scope.row.pickup_type != 1
                  "
                  icon="el-icon-document-copy"
                  @click="Receive(scope.row)"
                  >{{ $t("coupons.couponsList.lqjl") }}</el-button
                >
                <el-button
                  icon="el-icon-document"
                  @click="Giving(scope.row)"
                  >{{ $t("coupons.couponsList.zsjl") }}</el-button
                >
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox">
        <div class="pageLeftText">
          {{ $t("DemoPage.tableExamplePage.show") }}
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
            {{ $t("DemoPage.tableExamplePage.on_show") }}{{ currpage }}-{{
              current_num
            }}{{ $t("DemoPage.tableExamplePage.twig") }}{{ total
            }}{{ $t("DemoPage.tableExamplePage.twig_notes") }}
          </div>
        </el-pagination>
      </div>
    </div>

    <el-dialog
      :title="$t('coupons.couponsList.zq')"
      class="addUser"
      :visible.sync="dialogVisible3"
      :before-close="handleClose2"
      width="920px"
      top="100px"
    >
      <div class="">
        <el-radio-group class="tabKey" v-model="tabKey">
          <el-radio-button :class="{ active: tabKey == '0' }" :label="0">{{
            $t("coupons.couponsList.xzyh")
          }}</el-radio-button>
          <el-radio-button :class="{ active: tabKey == '1' }" :label="1">{{
            $t("coupons.couponsList.yxyh")
          }}</el-radio-button>
        </el-radio-group>
        <div class="dialog-search" v-show="tabKey == '0'">
          <!-- <el-select class="Search-select" v-model="grade" placeholder="请选择用户级别">
            <el-option v-for="(item,index) in gradeList" :key="index" :label="item.label" :value="item.value">
            </el-option>
          </el-select> -->
          <el-input
            class="Search-input"
            :placeholder="$t('coupons.couponsList.qsryh')"
            v-model="userName"
          ></el-input>
          <el-button @click="Reset" plain>{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button @click="query" type="primary">{{
            $t("DemoPage.tableExamplePage.demand")
          }}</el-button>
        </div>
        <div class="Table" v-show="tabKey == '0'">
          <el-table
            ref="multipleTable"
            :data="ProList"
            :row-key="rowKeys"
            style="width: 100%"
            v-loading="is_show"
            reserve-selection
            @selection-change="handleSelectionChange2"
            height="280"
            :header-cell-style="{
              background: '#F4F7F9',
              height: '50px',
            }"
          >
            <el-table-column
              :reserve-selection="true"
              align="center"
              type="selection"
              width="55"
            ></el-table-column>
            <el-table-column
              prop="user_id"
              align="center"
              :label="$t('coupons.couponsList.yhid')"
            >
            </el-table-column>
            <el-table-column
              prop="user_name"
              align="center"
              :label="$t('coupons.couponsList.yhmc')"
            >
            </el-table-column>
            <el-table-column
              prop="mobile"
              align="center"
              :label="$t('coupons.couponsList.sjh')"
            >
            </el-table-column>

            <!-- <p slot="append" v-if="is_show" style="text-align: center; line-height: 50px; cursor: pointer"
              v-loading="loadings" @click="addUser">
              {{$t('coupons.couponsList.djjzgd')}}
            </p> -->
          </el-table>
          <!-- <div class="showTotal">
            <div class="select-user">
              {{$t('coupons.couponsList.yx')}}<span>{{ selectUser || 0 }}</span>{{$t('coupons.couponsList.wyh')}}
            </div>
            <div class="total-user">
              {{$t('coupons.couponsList.gon')}}<span>{{ userTotal || 0 }}</span>{{$t('coupons.couponsList.wyh')}}
            </div>
          </div> -->
          <div class="pageBox" ref="pageBox">
            <div class="pageLeftText">
              {{ $t("DemoPage.tableExamplePage.show") }}
            </div>
            <el-pagination
              layout="slot, prev, pager, next"
              :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
              :next-text="$t('DemoPage.tableExamplePage.next_text')"
              @size-change="handleSizeChange2"
              :page-sizes="pagesizes"
              :current-page.sync="page2"
              @current-change="handleCurrentChange2"
              :total="userTotal"
            >
              <div class="pageRightText">
                {{ $t("DemoPage.tableExamplePage.on_show") }}{{ currpage2 }}-{{
                  current_num2
                }}{{ $t("DemoPage.tableExamplePage.twig") }}{{ userTotal
                }}{{ $t("DemoPage.tableExamplePage.twig_notes") }}
              </div>
            </el-pagination>
          </div>
        </div>
        <div
          class="Table selected"
          v-show="tabKey == '1'"
          style="margin-top: 14px"
        >
          <el-table
            class="selected"
            height="350"
            :data="ChangeProList"
            :header-cell-style="{
              background: '#F4F7F9',
              height: '50px',
            }"
          >
            <el-table-column
              prop="user_id"
              align="center"
              :label="$t('coupons.couponsList.yhid')"
            >
            </el-table-column>
            <el-table-column
              prop="user_name"
              align="center"
              :label="$t('coupons.couponsList.yhmc')"
            >
            </el-table-column>
            <el-table-column
              prop="mobile"
              align="center"
              :label="$t('coupons.couponsList.sjh')"
            >
            </el-table-column>
            <el-table-column
              fixed="right"
              class="delet"
              align="center"
              :label="$t('coupons.cz')"
              width="200"
            >
              <template slot-scope="scope">
                <div class="OP-button">
                  <div class="OP-button-top" style="justify-content: center">
                    <el-button
                      class="el-icon-delete"
                      @click="DeleteP(scope.$index, scope.row)"
                      >{{ $t("coupons.shanchu") }}</el-button
                    >
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <!-- <div class="showTotal">
            <div class="select-user">
              {{$t('coupons.couponsList.yx')}}<span>{{ selectUser || 0 }}</span>{{$t('coupons.couponsList.wyh')}}
            </div>
            <div class="total-user">
              {{$t('coupons.couponsList.gon')}}<span>{{ userTotal || 0 }}</span>{{$t('coupons.couponsList.wyh')}}
            </div>
          </div> -->
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <div class="showTotal">
          <div class="select-user">
            {{ $t("coupons.couponsList.yx") }}<span>{{ selectUser || 0 }}</span
            >{{ $t("coupons.couponsList.wyh") }}
          </div>
          <div>
            <el-button class="clo_bt" @click="handleClose2()">{{
              $t("coupons.ccel")
            }}</el-button>
            <el-button type="primary" @click="AddProconfirm">{{
              $t("coupons.okk")
            }}</el-button>
          </div>
        </div>
      </span>
    </el-dialog>
    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('DemoPage.tableExamplePage.export_data')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <div class="item" @click="exportPage">
          <i class="el-icon-document"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_page") }}</span>
        </div>
        <div class="item item-center" @click="exportAll">
          <i class="el-icon-document-copy"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_all") }}</span>
        </div>
        <div class="item" @click="exportQuery">
          <i class="el-icon-document"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_query") }}</span>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import couponsList from "@/webManage/js/plug_ins/coupons/couponsList";
export default couponsList;
</script>

<style scoped lang="less">
@import "../../../webManage/css/plug_ins/coupons/couponsList.less";
</style>
