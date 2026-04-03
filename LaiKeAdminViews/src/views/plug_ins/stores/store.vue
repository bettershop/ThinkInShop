<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button
          :label="$t('stores.dp')"
          @click.native.prevent="$router.push('/plug_ins/stores/store')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/store')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.dpfl')"
          @click.native.prevent="$router.push('/plug_ins/stores/storeFl')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/storeFl')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.dpsh')"
          @click.native.prevent="$router.push('/plug_ins/stores/auditList')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/auditList')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.bzjjl')"
          @click.native.prevent="$router.push('/plug_ins/stores/bondMoney')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/bondMoney.vue')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.bzjsh')"
          @click.native.prevent="$router.push('/plug_ins/stores/bondExamine')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/bondExamine.vue')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.spsh')"
          @click.native.prevent="$router.push('/plug_ins/stores/goodsAudit')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/goodsAudit.vue')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.txsh')"
          @click.native.prevent="
            $router.push('/plug_ins/stores/withdrawalAudit')
          "
          v-if="
            handleTabLimits(routerList, '/plug_ins/stores/withdrawalAudit.vue')
          "
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.txjl')"
          @click.native.prevent="
            $router.push('/plug_ins/stores/withdrawalRecord')
          "
          v-if="
            handleTabLimits(routerList, '/plug_ins/stores/withdrawalRecord.vue')
          "
        ></el-radio-button>
        <!-- <el-radio-button :label="$t('stores.dpsz')" @click.native.prevent="toset()"></el-radio-button> -->
        <!-- <el-radio-button :label="$t('stores.dpsz')" @click.native.prevent="toset()"></el-radio-button> -->
      </el-radio-group>
    </div>

    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-select
            class="select-input"
            v-model="inputInfo.status"
            :placeholder="$t('stores.store.qxzyy')"
          >
            <el-option
              v-for="(item, index) in statusList"
              :key="index"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-select
            class="shaco_select"
            v-model="inputInfo.promiseStatus"
            :placeholder="$t('stores.store.qxzjn')"
          >
            <el-option :label="$t('stores.store.wjn')" value="0"></el-option>
            <el-option :label="$t('stores.store.yjn')" value="1"></el-option>
          </el-select>
          <el-input
            v-model="inputInfo.storeName"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('stores.store.qsrdp')"
          ></el-input>
          <!-- 选择店铺分类 -->
          <el-select
            class="select-input"
            v-model="inputInfo.mac_choose_fl"
            :placeholder="$t('stores.store.xzdp')"
          >
            <el-option
              v-for="(item, index) in choose_fl"
              :key="index"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button
            class="bgColor"
            type="primary"
            @click="demand"
            v-enter="demand"
            >{{ $t("DemoPage.tableExamplePage.demand") }}</el-button
          >
          <!-- <el-button class="bgColor export" type="primary" @click="dialogShow">导出</el-button> -->
        </div>
      </div>
    </div>

    <div class="jump-list">
      <el-button
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="$router.push('/plug_ins/stores/increaseStore')"
        >{{ $t("stores.store.tjdp") }}</el-button
      >
      <el-button
        class="bgColor laiketui laiketui-download"
        icon="el-icon-download"
        type="primary"
        @click="dialogShow2"
        >{{ $t("stores.store.fzzdxz") }}</el-button
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
      >
        <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
          </div>
        </template>
        <el-table-column prop="id" :label="$t('stores.store.dpid')" width="100">
        </el-table-column>
        <el-table-column
          prop="store_info"
          :label="$t('stores.store.dpxx')"
          width="350"
        >
          <template slot-scope="scope">
            <div class="stores-info">
              <div class="head-img">
                <img
                  :src="scope.row.headimgurl"
                  alt=""
                  @error="handleErrorImg"
                />
              </div>
              <div class="store-info">
                <el-tooltip popper-class="tool-tip2" placement="top">
                  <div slot="content">{{ scope.row.name }}</div>
                  <div class="item name">
                    {{ $t("stores.store.dpmc") }}：{{ scope.row.name }}
                  </div>
                </el-tooltip>
                <!-- <div class="item name">{{$t('stores.store.dpmc')}}：{{ scope.row.name }}</div> -->
                <div class="item grey center">
                  {{ $t("stores.store.yhid") }}：{{ scope.row.user_id }}
                </div>
                <div class="item grey">
                  {{ $t("stores.store.ssyh") }}：<span class="font_one">{{
                    scope.row.user_name
                  }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <!-- 店铺分类 -->
        <el-table-column
          prop="className"
          :label="$t('stores.store.dpfl')"
        ></el-table-column>
        <!-- 联系人 -->
        <el-table-column prop="realname" :label="$t('stores.store.lxr')">
        </el-table-column>
        <el-table-column
          prop="tel"
          :label="$t('stores.store.lxdh')"
          width="150"
        >
        <template slot-scope="{row}">
          <span>{{`+${row.cpc || '86'} ${row.tel}`}}</span>
        </template>
        </el-table-column>
        <el-table-column prop="cashable_money" :label="$t('stores.store.dpye')">
          <template slot-scope="{row}">
            <span v-if="row.cashable_money">{{
              row.cashable_money.toFixed(2)
            }}</span>
            <span v-else>0.00</span>
          </template>
        </el-table-column>
        <el-table-column prop="tel" :label="$t('stores.store.sps')">
          <template slot-scope="scope">
            <span>{{ scope.row.goodsNum }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tel" :label="$t('stores.store.dds')">
          <template slot-scope="scope">
            <span>{{ scope.row.ordersNum }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="promiseStatus"
          :label="$t('stores.store.jnbzj')"
          width="130"
        >
          <template slot-scope="scope">
            <span
              v-if="
                scope.row.promiseStatus == '已缴' ||
                scope.row.promiseStatus == '已交纳'
              "
              >{{ $t("stores.store.yjn") }}</span
            >
            <span
              v-if="
                scope.row.promiseStatus == '未缴' ||
                scope.row.promiseStatus == '未缴纳'
              "
              >{{ $t("stores.store.wjn") }}</span
            >
          </template>
        </el-table-column>
        <el-table-column
          prop="add_time"
          :label="$t('stores.store.rzsj')"
          width="180"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.add_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="is_open" :label="$t('stores.store.dpzt')">
          <template slot-scope="scope">
            <span
              class="ststus"
              :class="[
                [
                  scope.row.is_open == 0
                    ? 'active2'
                    : scope.row.is_open == 1
                    ? 'active1'
                    : 'active3',
                ],
              ]"
              >{{
                scope.row.is_open == 0
                  ? $t("stores.store.wyy")
                  : scope.row.is_open == 1
                  ? $t("stores.store.yyz")
                  : $t("stores.store.ydy")
              }}</span
            >
          </template>
        </el-table-column>
        <el-table-column
          prop="tel"
          :label="$t('stores.store.zjdlsj')"
          width="180"
        >
          <template slot-scope="scope">
            <span v-if="scope.row.last_login_time">{{
              scope.row.last_login_time
            }}</span>
          </template>
        </el-table-column>
        <!-- width="280" -->
        <el-table-column
          fixed="right"
          :label="$t('stores.cz')"
          width="200"
          class="hhh"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  icon="el-icon-view"
                  @click="View(scope.row)"
                  >{{ $t("stores.store.ck") }}</el-button
                >
                <el-button
                  icon="el-icon-edit-outline"
                  @click="Modify(scope.row)"
                  >{{ $t("stores.bianji") }}</el-button
                >
                <!-- <el-button icon="el-icon-qxpz" class="qxpz_bt">{{
                  $t('stores.qxpz')
                }}</el-button> -->
              </div>
              <div class="OP-button-bottom">
                <el-button
                  v-if="
                    laike_admin_userInfo != scope.row.id
                  "
                  icon="el-icon-delete"
                  @click="Delete(scope.row)"
                  >{{ $t("stores.shanchu") }}</el-button
                >
                <div
                  @click="tags(scope.row)"
                  :class="
                    laike_admin_userInfo == scope.row.id ? 'more2' : 'more'
                  "
                  v-if="scope.row.isAccounts == 1"
                >
                  {{ $t("membersLists.gd")
                  }}<i class="el-icon-caret-bottom"></i>
                  <ul

                    class="more-block"
                    :class="[tag == scope.row.id ? 'active' : '']"
                  >
                    <!-- <li class="more-box">
                      <div @click="Viewdata(scope.row)">
                        {{ $t("stores.jysj") }}
                      </div>
                    </li> -->
                    <li class="more-box" v-if="scope.row.isAccounts == 1">
                      <div @click="seeLedger(scope.row)">
                        {{ $t("stores.store.fzsz") }}
                      </div>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <!-- v-if="showPagebox" -->
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
          :current-page="dictionaryNum"
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
    <div class="dialog-block2">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('stores.store.xz')"
        :visible.sync="dialogVisible1"
        :before-close="handleClose2"
      >
        <!-- <div class="sm_box">
          <span class="sm_font">{{ $t('stores.store.text1') }}</span>
        </div> -->
        <el-form
          :model="ruleForm2"
          :rules="rules2"
          ref="ruleForm2"
          label-width="auto"
          class="demo-ruleForm"
        >
          <el-form-item :label="$t('stores.store.zdrq')" prop="time">
            <el-date-picker
              style="width: 328px"
              v-model="ruleForm2.time"
              type="date"
              value-format="yyyy-MM-dd"
              :placeholder="$t('stores.store.qxzxzdzdrq')"
            >
            </el-date-picker>
            <div class="explain">
              <img src="../../../assets/imgs/ts3.png" alt="" />
              <span class="red">{{ $t("stores.store.text1") }}</span>
            </div>
          </el-form-item>
          <div class="form-footer">
            <el-form-item>
              <el-button class="qx_bt" @click="handleClose2()">{{
                $t("membersLists.ccel")
              }}</el-button>
              <el-button
                class="bdColor"
                type="primary"
                @click="submitForm2('ruleForm2')"
                >{{ $t("membersLists.okk") }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import store from "@/webManage/js/plug_ins/stores/store";
export default store;
</script>

<style scoped lang="less">
@import "../../../webManage/css/plug_ins/stores/store.less";
</style>

<style>
.tool-tip2 {
  max-width: 194px;
  background: #ffffff !important;
  color: #6a7076 !important;
  box-shadow: 0px 0px 12px 1px rgba(0, 0, 0, 0.12);
}
.tool-tip2 .popper__arrow {
  border-top-color: #ffffff !important;
}
.tool-tip2 .popper__arrow::after {
  border-top-color: #ffffff !important;
}
</style>
