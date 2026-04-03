<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group
        fill="#2890ff"
        text-color="#fff"
        v-model="radio1"
        ref="tab_bun"
      >
        <!-- <el-radio-button label="" v-if="handleTabLimits(routerList,'orderLists',1)">
          {{$t('orderLists.ddlb')}}<span class="order_dfhNum" :class="[ radio1 == '' ? 'active' : '']" v-if='this.$store.getters.orderListNum >0'>({{ this.$store.getters.orderListNum }})</span>
        </el-radio-button> -->
        <el-radio-button
          label="1"
          v-if="handleTabLimits(routerList, 'tabPhysicalOrder', 1)"
        >
          {{ $t('orderLists.swdd')
          }}<span v-if='this.$store.getters.physicalOrderNum' class="order_dfhNum" :class="[radio1 == '1' ? 'active' : '']"
            >({{ this.$store.getters.physicalOrderNum}})</span
          >
        </el-radio-button>
        <el-radio-button
          label="3"
          v-if="handleTabLimits(routerList, 'tabPhysicalOrder', 1)"
        >
          {{ $t('orderLists.xndd')
          }}<span v-if='this.$store.getters.virtualNum' class="order_dfhNum" :class="[radio1 == '3' ? 'active' : '']"
            >({{ this.$store.getters.virtualNum }})</span
          >
        </el-radio-button>
        <el-radio-button
          label="2"
          v-if="handleTabLimits(routerList, 'tabPickUpOrder', 1)"
        >
          {{ $t('orderLists.ztdd') }}
        </el-radio-button>
        <el-radio-button
          label="5"
          v-if="handleTabLimits(routerList, 'tabPhysicalOrder', 1)"
        >
          {{ $t('orderLists.psdd')
          }}<span class="order_dfhNum"  v-if='this.$store.getters.ziPeiNum' :class="[radio1 == '5' ? 'active' : '']"
            >({{ this.$store.getters.ziPeiNum }})</span
          >
        </el-radio-button>
        <!-- <el-radio-button :label="3">
          虚拟订单
        </el-radio-button>
        <el-radio-button :label="4">
          活动订单<span class="order_dfhNum" :class="[ radio1 == '4' ? 'active' : '']">({{ this.$store.getters.activityOrderNum }})</span>
        </el-radio-button> -->
      </el-radio-group>
    </div>

    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.orderInfo"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input orderinfo-input"
            :placeholder="$t('orderLists.qsrddbh')"
          ></el-input>
          <el-input
            v-model="inputInfo.store"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('orderLists.qsrdpmc')"
          ></el-input>
          <el-select
            v-if="radio1 != '3'"
            class="select-input zt-select-input"
            v-model="inputInfo.state"
            :placeholder="$t('orderLists.qxzddzt')"

          >
            <el-option
              v-for="item in stateList"
              :key="item.brand_id"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-select
            v-if="radio1 == '3'"
            class="select-input"
            v-model="inputInfo.state"
            :placeholder="$t('orderLists.qxzddzt')"
          >
            <el-option
              v-for="item in stateHxList"
              :key="item.brand_id"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-select
            class="select-input"
            v-model="inputInfo.type"
            :placeholder="$t('orderLists.qxzxdzt')"
          >
            <el-option
              v-for="item in typeList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <div class="select-date">
            <el-date-picker
              v-model="inputInfo.date"
              type="datetimerange"
              :range-separator="$t('reportManagement.businessReport.zhi')"
              :start-placeholder="$t('reportManagement.businessReport.ksrq')"
              :end-placeholder="$t('reportManagement.businessReport.jsrq')"
              value-format="yyyy-MM-dd HH:mm:ss"
              :editable="false"
            >
            </el-date-picker>
          </div>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t('DemoPage.tableExamplePage.reset')
          }}</el-button>
          <el-button
            class="bgColor"
            type="primary"
            @click="demand"
            v-enter="demand"
            >{{ $t('DemoPage.tableExamplePage.demand') }}</el-button
          >
            <el-button
              v-has-permi="'export'"
              class="bgColor export"
              type="primary"
              @click="dialogShow"
              >{{ $t('DemoPage.tableExamplePage.export') }}</el-button
            >
        </div>
      </div>
    </div>

    <div class="jump-list">
        <el-button
          v-has-permi="'valetOrder'"
          v-if="radio1 != '3'"
          class="bgColor laiketui laiketui-add"
          type="primary"
          @click="placeOrder"
          >{{ $t('orderLists.dkxd') }}</el-button
        >
        <el-button
          v-has-permi="'printOrder'"
          class="bgColor el-icon-printer"
          type="primary"
          @click="print"
          >{{ $t('orderLists.dddy') }}</el-button
        >
        <el-button
          v-has-permi="'/order/bulkDelivery/bulkList'"
          v-if="radio1 != '3'"
          class="bgColor laiketui laiketui-add"
          type="primary"
          @click="goBulk"
          >{{ $t('bulkList.plfh') }}</el-button
        >
        <el-button
          v-has-permi="'batchDelete'"
          class="fontColor"
          @click="delAll"
          :disabled="is_disabled"
          icon="el-icon-delete"
          >{{ $t('orderLists.plsc') }}</el-button
        >
    </div>

    <div class="menu-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="'85%'"
      >
        <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column fixed="left" type="selection" width="55">
        </el-table-column>
        <el-table-column
          prop="orderInfo"
          :label="$t('orderLists.ddxx')"
          width="400"
          align="left"
        >
          <template slot-scope="scope">
            <div class="head-info">
              <span class="red-dot" v-if="scope.row.status == '待发货'"></span>
              <span style="margin-right: 1.875rem"
                >{{ $t('orderLists.ddbh') }}：{{ scope.row.orderno }}</span
              >
              <span
                >{{ $t('orderLists.shdd') }}：{{ scope.row.mchOrderNo }}</span
              >
              <span>{{ $t('orderLists.ddmxid') }}：{{ scope.row.detailId }}</span>
              <span
                >{{ $t('orderLists.cjsj') }}：{{ scope.row.createDate }}</span
              >
              <span>{{ $t('orderLists.dp') }}：{{ scope.row.mchName }}</span>

            </div>
            <div class="content-info">
              <div class="img-item">
                <img
                  :src="scope.row.goodsImgUrl"
                  alt=""
                  @error="handleErrorImg"
                />
              </div>
              <div class="goods-item">
                <span class="name">{{ scope.row.goodsName }}</span>
                <span
                  ><span class="kip_name">{{ $t('orderLists.gg') }}：</span
                  >{{ scope.row.attrStr }}</span
                >
                <span
                  ><span class="kip_name">{{ $t('orderLists.js') }}：</span
                  >{{ scope.row.needNum }}</span
                >
                <span
                  ><span class="kip_name">{{ $t('orderLists.jg') }}：</span
                  >{{ scope.row.currency_symbol}}{{ LaiKeCommon.formatPrice(scope.row.goodsPrice,scope.row.exchange_rate) }}</span
                >
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="orderPrice"
          :label="$t('orderLists.ddzj')"
          width="150"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.currency_symbol}}{{ LaiKeCommon.formatPrice(scope.row.old_total,scope.row.exchange_rate)  }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="goodsNum"
          :label="$t('orderLists.sl')"
          width="150"
        >
        </el-table-column>
        <el-table-column
          prop="price"
          :label="$t('orderLists.xdlx')"
          width="150"
        >
          <template slot-scope="scope">
            <span>{{
              scope.row.operation_type == '1'
                ? $t('orderLists.yhxd')
                : scope.row.operation_type == '2'
                ? $t('orderLists.dpxd')
                : $t('orderLists.ptxd')
            }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          :label="$t('orderLists.ddzt')"
          width="150"
        >
          <template slot-scope="scope">
            <span> {{ getOrderStatusText(scope.row.orderStatus) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('orderLists.ddlx')" width="150">
          <template slot-scope="scope">
            <span>{{ scope.row.otype }}{{ $t('orderLists.dd') }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('orderLists.mjxx')"
          width="200"
          show-overflow-tooltip
        >
          <template slot-scope="scope">
            <div class="align">
              <div class="id">
                <span class="id-item">{{ $t('orderLists.yhid') }}：</span>
                <span>{{ scope.row.userId }}</span>
              </div>
              <div class="name">
                <span class="name-item"
                  >{{
                    radio1 != '3'
                      ? $t('orderLists.shr')
                      : $t('orderLists.yhmc')
                  }}：</span
                >
                <span>{{ scope.row.userName }}</span>
              </div>
              <div class="name" v-if="radio1 != '3'">
                <span class="name-item">{{ $t('orderLists.lxdh') }}：</span>
                <span>{{ scope.row.mobile }}</span>
              </div>
              <div class="name" v-if="radio1 != '3'">
                <span class="name-item">{{ $t('orderLists.shdz') }}：</span>
                <span>{{ scope.row.addressInfo }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="payName"
          :label="$t('orderLists.zffs')"
          width="150"
        >
        <template slot-scope="scope">
          {{scope.row.payName}}
          </template>
        </el-table-column>
        <el-table-column
          v-if="['', '1', '4', '5'].includes(radio1)"
          prop="expressStr"
          :label="$t('orderLists.wlxx')"
          width="300"
        >
          <template slot-scope="scope">
            <div class="expressStrs">
              <div class="item" v-if="scope.row.self_lifting != 2">
                <div class="item-title">
                  <span>{{ $t('orderLists.kddh') }}：</span>
                </div>
                <ul v-if="scope.row.expressList">
                  <li
                    v-for="(item, index) in scope.row.expressList"
                    :key="index"
                  >
                    <span>{{ item }}</span>
                  </li>
                </ul>
              </div>
              <div class="item" v-if="scope.row.self_lifting == 2">
                <span class="kuaidi-info">{{ $t('配送时间') }}：</span
                >{{scope.row.delivery_time + ' ' + (scope.row.delivery_period == 1 ? '上午': scope.row.delivery_period == 2 ? '下午':'')}}
              </div>
              <div class="item">
                <span class="kuaidi-info">{{ $t('orderLists.yf') }}：</span
                >{{scope.row.currency_symbol}}{{ LaiKeCommon.formatPrice(scope.row.freight,scope.row.exchange_rate)  }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('orderLists.cz')" width="120">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">

                  <el-button
                    icon="el-icon-view"
                    @click="Details(scope.row)"
                    v-has-permi="'orderDetails'"
                    >{{ $t('orderLists.ddxq') }}</el-button
                  >
                  <el-button
                    icon="el-icon-edit-outline"
                    @click="Edit(scope.row)"
                    v-has-permi="'editorOrder'"
                    v-if="['待付款', '待发货'].includes(scope.row.status) && radio1 != '3'"
                  >
                    {{ $t('orderLists.bjdd') }}</el-button
                  >
                  <el-button
                    icon="el-icon-box"
                    @click="Delivery(scope.row)"
                    v-has-permi="'goodsDeliverys'"
                    v-if="
                      ['', '1', '4', '5'].includes(radio1) &&
                      radio1 != '3'
                    "
                    v-show="['待发货'].includes(scope.row.status)"
                  >
                    {{ $t('orderLists.spfh') }}</el-button
                  >
                  <el-button
                    icon="el-icon-truck"
                    @click="dialogShow2(scope.row)"
                    v-if="
                      scope.row.courier_num &&
                      scope.row.courier_num.length > 0 &&
                      radio1 != '3'
                    "
                    class="logistics"
                  >
                    {{ $t('orderLists.ckwl') }}</el-button
                  >
                  <el-button
                    icon="el-icon-tickets"
                    v-has-permi="'E-shipping'"
                    v-if="scope.row.logistics_type"
                    @click="goPage(scope.row)"
                    class="logistics"
                    >{{ $t('orderLists.dzmd') }}</el-button
                  >
                  <el-button
                    icon="el-icon-document-checked"
                    v-has-permi="'RedeemCancel'"
                    v-if=" scope.row.showHX == 1 && radio1 == '3' && scope.row.write_off_settings == 1 && ['待核销'].includes(scope.row.status)"
                    @click="dialogShow3(scope.row)"
                    class="logistics"
                    >{{ $t('orderLists.hx') }}</el-button
                  >
                 <!-- 取消用户订单 -->
                 <el-button
                    v-has-permi="'OrderCancel'"
                    icon="el-icon-edit-outline"
                    @click="CancellationOfOrder(scope.row)"
                    v-if="
                      ['待发货'].includes(scope.row.status) &&
                      (radio1 == '1' || radio1 == '5')
                    "
                  >
                    {{ $t('systemManagement.qxdd') }} </el-button
                  >
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <!-- v-if="showPagebox" -->
      <div class="pageBox" ref="pageBox" v-if="showPagebox">
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

    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('orderLists.wlxx')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <el-form
          :model="ruleForm"
          ref="ruleForm"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div
            class="task-container"
            v-for="(item, index) in logisticsList"
            :key="index"
          >
            <div class="courier-company">
              {{ $t('orderLists.kdgs') }}：<span class="span_two">{{
                item.kuaidi_name
              }}</span>
            </div>
            <div class="courier-no">
              {{ $t('orderLists.kddh') }}：<span class="span_two">{{
                item.courier_num
              }}</span>
            </div>
            <div class="logistics" v-if="item.list.length == 0">
              {{ $t('orderLists.wlgz') }}：<span class="span_two">{{
                $t('orderLists.zwwl')
              }}</span>
            </div>
            <div class="logistics" v-else>
              <span class="logistics-tracking"
                >{{ $t('orderLists.wlgz') }}：</span
              >
              <el-timeline>
                <template  >
                    <el-timeline-item
                    v-for="(item, index) in item.list"
                    :key="index"
                    :timestamp="item.time"
                    >
                    {{ item.context||item.description }}
                  </el-timeline-item>
                </template>
              </el-timeline>
            </div>
          </div>
        </el-form>
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
          <span>{{ $t('DemoPage.tableExamplePage.export_page') }}</span>
        </div>
        <div class="item item-center" @click="exportAll">
          <i class="el-icon-document-copy"></i>
          <span>{{ $t('DemoPage.tableExamplePage.export_all') }}</span>
        </div>
        <div class="item" @click="exportQuery">
          <i class="el-icon-document"></i>
          <span>{{ $t('DemoPage.tableExamplePage.export_query') }}</span>
        </div>
      </el-dialog>
    </div>

    <div :class="is_appointment == 1?'dialog-verification':'dialog-verification2'">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('orderLists.hx')"
        :visible.sync="dialogVisible3"
        :before-close="handleClose3"
      >
        <el-form
          :model="ruleForm3"
          :rules="rules3"
          ref="ruleForm3"
          label-width="auto"
          class="demo-ruleForm"
        >
          <div class="task-container">
          <!-- 以下只有不需要预约的才需要 -->
            <el-form-item
              class="integral"
              :label="$t('orderLists.jxmd')"
              prop="store"
              v-if="is_appointment == 1"
            >
              <el-select
                v-model="ruleForm3.store"
                :placeholder="$t('orderLists.qxzhxmd')"
              >
                <el-option
                  v-for="item in storeList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
              class="integral"
              :label="$t('orderLists.yzma')"
              prop="verification"
            >
              <el-input
                v-model="ruleForm3.verification"
                :placeholder="$t('orderLists.qsrhxyzm')"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button class="bgColor1" @click="handleClose3()">{{
                $t('zdata.off')
              }}</el-button>
              <el-button
                class="bdColor"
                type="primary"
                @click="submitForm3('ruleForm3')"
                >{{ $t('zdata.ok') }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
    <div class="dialog-verification sjps">
      <!-- 商家配送 填写配送员信息 弹框组件 -->
      <el-dialog
        :title="$t('orderLists.fh')"
        :visible.sync="dialogVisible5"
        :before-close="handleClose5"
      >
        <el-form
          :model="ruleForm5"
          :rules="rules5"
          ref="ruleForm5"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="task-container">
            <el-form-item
              class="integral"
              :label="$t('orderLists.psddryxm')"
              prop="courier_name"
            >
              <el-input
                v-model="ruleForm5.courier_name"
                :placeholder="$t('orderLists.qsrpsddryxm')"
              ></el-input>
            </el-form-item>
            <el-form-item
              class="integral"
              :label="$t('orderLists.psddrydh')"
              prop="phone"
            >
              <el-input
                v-model="ruleForm5.phone"
                :placeholder="$t('orderLists.qsrpsddrydh')"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button @click="handleClose5()">{{
                $t('zdata.off')
              }}</el-button>
              <el-button
                :disabled="ruleForm5.courier_name == '' || ruleForm5.phone ==''"
                class="bdColor"
                type="primary"
                @click="submitForm5('ruleForm5')"
                >{{ $t('zdata.ok') }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
    <div class="dialog-orderInfo">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('orderLists.hxddxx')"
        :visible.sync="dialogVisible4"
        :before-close="handleClose4"
      >
        <div class="orderBox">
          <div class="one">{{ $t('orderLists.hxddbh') }}：{{ ruleForm4.sNo }}</div>
          <div class="two">
            <div class="widOne">{{ $t('orderLists.hxyhmc') }}：{{ ruleForm4.name }}</div>
            <div>{{ $t('orderLists.hxyhsj') }}：{{ ruleForm4.mobile }}</div>
          </div>
          <div class="two">
            <div class="widOne">{{ $t('orderLists.hxspsl') }}：{{ ruleForm4.num }}</div>
            <div>{{ $t('orderLists.hxddje') }}：￥ {{ ruleForm4.p_price }}</div>
          </div>
          <!-- 以下只有有预约的才需要 -->
          <div class="three" v-if="ruleForm4.store_name">
            <div class="widOne">{{ $t('orderLists.hxyymd') }}：{{ ruleForm4.store_name }}</div>
            <div>{{ $t('orderLists.hxyysj') }}：{{ ruleForm4.time }}</div>
          </div>
          <el-table
            class="el-table"
            :data="orInfoList"
            style="width: 100%;"
            height="300"
            @current-change="handleSelectionChange2"
          >
          <el-table-column
                :label="$t('orderLists.hxxz')"
                align="center"
                width="55"
              >
                <template slot-scope="scope">
                  <el-radio :label="scope.row" v-model="tableRadio"
                    ><i></i
                  ></el-radio>
                </template>
              </el-table-column>
            <el-table-column
              align="center"
              prop="name"
              :label="$t('orderLists.hxspmc')"
              width="280"
            >
              <template slot-scope="scope">
                <div class="div_center">
                  <img :src="scope.row.img" width="40" height="40" />
                  <span class="span_center">{{ scope.row.product_title }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column align="center" prop="size" :label="$t('orderLists.hxgg')">
            </el-table-column>
            <el-table-column align="center" prop="num" :label="$t('orderLists.hxsl')">
            </el-table-column>
            <el-table-column align="center" prop="write_off_num" :label="$t('orderLists.hxdhxcs')">
            </el-table-column>
          </el-table>
        </div>
        <div slot="footer" class="form-footer">
          <el-button class="bgColor1" @click="handleClose4"
            >{{
                $t('zdata.off')
              }}</el-button
          >
          <el-button class="submit" type="primary" @click="submit"
            >{{ $t('orderLists.hxqrhx') }}</el-button
          >
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import orderLists from '@/webManage/js/order/orderList/orderLists'
export default orderLists
</script>

<style scoped lang="less">
@import '../../../../webManage/css/order/orderList/orderLists.less';
</style>
