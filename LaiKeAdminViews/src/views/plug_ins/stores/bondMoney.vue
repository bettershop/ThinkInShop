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
          <el-input
            v-model="inputInfo.keyName"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('stores.bondMoney.qsrdp')"
          ></el-input>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t('DemoPage.tableExamplePage.reset')
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{
            $t('DemoPage.tableExamplePage.demand')
          }}</el-button>
        </div>
      </div>
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
            <p style="color: #414658">{{$t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column
          fixed="left"
          prop="mch_id"
          :label="$t('stores.bondMoney.dpid')"
        >
        </el-table-column>
        <el-table-column
          prop="store_info"
          :label="$t('stores.bondMoney.dpxx')"
          fixed="left"
          width="300"
        >
          <template slot-scope="scope">
            <div class="store-info">
              <div class="head-img">
                <img :src="scope.row.headimgurl" alt="" @error="handleErrorImg"/>
              </div>
              <div class="store_info">
                <div class="item name">
                  {{ $t('stores.bondMoney.dpmc') }}：{{ scope.row.mchName }}
                </div>
                <div class="item grey center">
                  {{ $t('stores.bondMoney.yhid') }}：{{ scope.row.user_id }}
                </div>
                <div class="item grey">
                  {{ $t('stores.bondMoney.ssyh') }}：{{ scope.row.userName }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="realname" :label="$t('stores.bondMoney.lxr')">
        </el-table-column>
        <el-table-column prop="tel" :label="$t('stores.bondMoney.lxdh')">
        </el-table-column>
        <el-table-column prop="status" :label="$t('stores.bondMoney.jllx')">
          <template slot-scope="scope">
            <div class="store-info" style="justify-content: center">
              {{
                scope.row.status == 1
                  ? $t('stores.bondMoney.jn')
                  : $t('stores.bondMoney.th')
              }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="mobile" :label="$t('stores.bondMoney.sfytg')">
          <template slot-scope="scope">
            <div class="store-info" style="justify-content: center">
              {{
                scope.row.is_return_pay == 1
                  ? $t('stores.bondMoney.shi')
                  : $t('stores.bondMoney.fou')
              }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('stores.bondMoney.jnsqth')">
          <template slot-scope="scope">
            <span v-if="scope.row.is_return_pay != 1">{{
              scope.row.add_date | dateFormat
            }}</span>
            <span v-else>{{ scope.row.update_date | dateFormat }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox">
        <div class="pageLeftText">
          {{ $t('DemoPage.tableExamplePage.show') }}
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
            {{ $t('DemoPage.tableExamplePage.on_show') }}{{ currpage }}-{{
              current_num
            }}{{ $t('DemoPage.tableExamplePage.twig') }}{{ total
            }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
          </div>
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import bondMoney from '@/webManage/js/plug_ins/stores/bondMoney'
export default bondMoney
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/stores/bondMoney.less';
</style>
