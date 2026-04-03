<template>
  <div class="container">
    <div class="basic-info" v-if="dataInfo">
      <div class="header">
        <span>{{ $t('orderDetails.jcxx') }}</span>
      </div>
      <div class="basic-block">
        <ul class="items">
          <li>
            <span>{{ $t('orderDetails.ddbh') }}：</span
            ><span class="span_kip">{{ dataInfo.sNo }}</span>
          </li>
          <li>
            <span>{{ $t('orderDetails.ddzt') }}：</span
            ><span class="span_kip">{{ dataInfo.status }}</span>
          </li>
          <li>
            <span>{{ $t('orderDetails.ddlx') }}：</span
            ><span class="span_kip"
              >{{ dataInfo.orderTypeName }}{{ $t('orderDetails.dd') }}</span
            >
          </li>
          <li>
            <span>{{ $t('orderDetails.ddly') }}：</span
            ><span class="span_kip">{{ $myGetSource(dataInfo.source) }}</span>
          </li>
        </ul>
        <ul class="items">
          <li>
            <span>{{ $t('orderDetails.zffs') }}：</span
            ><span class="span_kip">{{ dataInfo.paytype }}</span>
          </li>
          <li>
            <span>{{ $t('orderDetails.xdsj') }}：</span
            ><span class="span_kip">{{ dataInfo.add_time | dateFormat }}</span>
          </li>
          <li>
            <span>{{ $t('orderDetails.fksj') }}：</span
            ><span class="span_kip">{{ dataInfo.pay_time }}</span>
          </li>
          <!-- 虚拟订单不需要展示发货时间 -->
          <li v-if="type != 3">
            <span>{{ $t('orderDetails.fhsj') }}：</span
            ><span class="span_kip">{{ dataInfo.deliver_time }}</span>
          </li>
          <!-- 虚拟订单需要展示完成时间 -->
          <li v-if="type == 3">
            <span>{{ $t('orderDetails.wcsj') }}：</span
            ><span class="span_kip">{{
              dataInfo.arrive_time | dateFormat
            }}</span>
          </li>
        </ul>
        <ul class="items">
          <!-- 虚拟订单不需要展示收货时间 -->
          <li v-if="type != 3">
            <span>{{ $t('orderDetails.shsj') }}：</span
            ><span class="span_kip">{{ dataInfo.arrive_time }}</span>
          </li>
          <li>
            <span>{{ $t('orderDetails.yhid') }}：</span
            ><span class="span_kip">{{ dataInfo.user_id }}</span>
          </li>
          <li>
            <span>{{ $t('orderDetails.yhmc') }}：</span
            ><span class="span_kip">{{ dataInfo.user_name }}</span>
          </li>
          <li class="notes">
            <!-- <span>订单备注：</span> -->
            <!-- <ul class="remaks" v-if="noteList">
                        <li v-for="(item,index) in noteList" :key="index">
                            {{ item }}
                        </li>
                    </ul> -->
            <span class="notes_head">{{ $t('orderDetails.ddbz') }}：</span>
            <span class="span_kip" v-if="dataInfo.remarks">
              <el-tooltip
                :content="dataInfo.remarks"
                placement="bottom"
                effect="light"
              >
                <div class="span_kip_div">
                  {{ dataInfo.remarks ? dataInfo.remarks : '' }}
                </div>
              </el-tooltip>
            </span>
          </li>
          <li v-if="type == 3"></li>
        </ul>
        <ul class="items" v-if="type != 3">
          <li>
            <span>{{ $t('orderDetails.kddh') }}：</span
            ><span class="span_kip">{{ totleInfo.expressStr }}</span>
          </li>
        </ul>
      </div>
    </div>
    <!-- 虚拟订单不需要展示 -->
    <div class="consignee-info" v-if="dataInfo && type != 3">
      <div class="header">
        <template v-if="totleInfo.selfLifting == 2">
          <!-- 商家配送 配送信息 -->
          <span>{{ $t("配送信息") }}</span>
        </template>
        <template v-else>
          <!-- 收货人信息 -->
          <span>{{ $t("orderDetails.shrxx") }}</span>
        </template>
      </div>
      <div class="consignee-block">
        <ul class="items">
          <li>
            <span>{{ $t('orderDetails.shr') }}：</span
            ><span class="span_kip">{{ dataInfo.name }}</span>
          </li>
          <li>
            <span>{{ $t('orderDetails.xldh') }}：</span
            ><span class="span_kip">{{ dataInfo.mobile }}</span>
          </li>
          <li v-if="totleInfo.selfLifting != 1">
            <span>
              <template v-if="totleInfo.selfLifting == 2">
                <!-- 配送地址 -->
                {{ $t("配送地址") }}：
              </template>
              <template v-else>
                <!-- 收货地址 -->
                {{ $t("orderDetails.shdz") }}：
              </template>
            </span
            ><span class="span_kip">{{ dataInfo.address }}</span>
          </li>
          <!-- <li>
              <span>订单备注：</span>{{ Object.keys(dataInfo.remarks).length === 0 ? '' : '' }}
          </li> -->
          <li v-if="totleInfo.storeSelfInfo && totleInfo.storeSelfInfo.delivery_time">
            <!-- 配送时间 -->
            <span>{{ $t("配送时间") }}：</span><span class="span_kip">{{ totleInfo.storeSelfInfo.delivery_time + ' ' + (totleInfo.storeSelfInfo.delivery_period == 1 ? '上午':'下午') }}</span>
          </li>
          <li v-if="totleInfo.storeSelfInfo && totleInfo.storeSelfInfo.courier_name">
            <!-- 配送员姓名 -->
            <span>{{ $t("配送员姓名") }}：</span><span class="span_kip">{{ totleInfo.storeSelfInfo.courier_name }}</span>
          </li>
          <li v-if="totleInfo.storeSelfInfo && totleInfo.storeSelfInfo.phone">
            <!-- 配送员电话 -->
            <span>{{ $t("配送员电话") }}：</span><span class="span_kip">{{ totleInfo.storeSelfInfo.phone }}</span>
          </li>
        </ul>
      </div>
    </div>
    <!-- write_off_settings为1是线下核销 -->
    <!-- show_write_time为1是有预约信息 -->
    <!-- 有预约的不展示 无需核销不展示 -->
    <!-- <div
      class="hx-info"
      v-if="
        type == 3 && write_off_settings == 1 && dataInfo.show_write_time == 0
      "
    >
      <div class="header">
        <span>{{ $t('orderDetails.syhxmd') }}</span>
        <div class="box" @click="openStore">
          <span class="font1"
            >{{ storeList.length }} {{ $t('orderDetails.jia') }}</span
          >
          <img src="../../../../assets/imgs/jty.png" width="8" height="16" />
        </div>
      </div>
    </div> -->
    <!-- 有预约的展示 无需核销不展示-->
    <div
      class="consignee-info"
      v-if="
        dataInfo &&
        type == 3 &&
        write_off_settings == 1 &&
        dataInfo.show_write_time == 1
      "
    >
      <div class="header">
        <span>{{ $t('orderDetails.yyxxa') }}</span>
      </div>
      <div class="consignee-block">
        <ul class="items">
          <li>
            <span>{{ $t('orderDetails.yysj') }}：</span
            ><span class="span_kip">{{ dataInfo.write_time_info.time }}</span>
          </li>
          <li>
            <span>{{ $t('orderLists.hxyymd') }}：</span
            ><span class="span_kip">{{ dataInfo.write_time_info.mch_store }}</span>
          </li>
          <li>
            <span>{{ $t('orderDetails.mddza') }}：</span
            ><span class="span_kip">{{ dataInfo.write_time_info.address }}</span>
          </li>
          <!-- <li>
                    <span>订单备注：</span>{{ Object.keys(dataInfo.remarks).length === 0 ? '' : '' }}
                </li> -->
        </ul>
      </div>
    </div>
    <div class="goods-info" v-if="dataInfo">
      <div class="header">
        <span>{{ $t('orderDetails.spxx') }}</span>
      </div>
      <div class="goods-block">
        <div class="dictionary-list">
          <el-table
            :data="goodsTables"
            ref="table"
            class="el-table"
            style="width: 100%"
          >
            <el-table-column
              prop="p_id"
              :label="$t('orderDetails.spbh')"
              width="152"
            >
            </el-table-column>
            <el-table-column
              prop="p_name"
              :label="$t('orderDetails.spmc')"
              width="350"
            >
              <template slot-scope="scope">
                <div class="name-info">
                  <img :src="scope.row.pic" alt="" @error="handleErrorImg" />
                  <span>{{ scope.row.p_name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              prop="size"
              :label="$t('orderDetails.spgg')"
              width="158"
            >
            </el-table-column>
            <el-table-column
              prop="p_price"
              :label="$t('orderDetails.dj')"
              width="152"
            >
              <template slot-scope="scope">
                <span v-if="scope.row.p_price">{{ totleInfo.currency_symbol  }}{{
                    LaiKeCommon.formatPrice(scope.row.p_price,totleInfo.exchange_rate)
                  }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="num"
              :label="`${$t('valetOrder.gmsl')}(${conut})`"
              width="152"
            >
            </el-table-column>
            <!-- 已核销次数 只有虚拟订单且是线下核销才需要展示 -->
            <el-table-column
              v-if="
                type == '3' &&
                (dataInfo.status == '订单完成' ||
                  dataInfo.status == '待核销') &&
                write_off_settings == 1
              "
              prop="after_write_off_num"
              :label="$t('orderDetails.yhxcs')"
              width="152"
            >
              <template slot-scope="scope">
                <el-link
                  @click="openRecord(scope.row)"
                  type="primary"
                  :underline="false"
                  >{{ scope.row.after_write_off_num }}</el-link
                >
              </template>
            </el-table-column>
            <!-- 待核销次数 只有虚拟订单且是线下核销才需要展示 -->
            <el-table-column
              v-if="type == '3' && write_off_settings == 1"
              prop="write_off_num"
              :label="$t('orderDetails.dhxcs')"
              width="152"
            >
            </el-table-column>
            <!-- 退款核销次数 只有虚拟订单且是线下核销才需要展示 -->
            <el-table-column
              v-if="
                type == '3' &&
                dataInfo.status != '待付款' &&
                totleInfo.returnStatus &&
                totleInfo.returnStatus != '' &&
                write_off_settings == 1
              "
              prop="r_write_off_num"
              :label="$t('orderDetails.tkhxcs')"
              width="152"
            >
            </el-table-column>
            <!-- 虚拟订单展示这个 -->
            <el-table-column
              v-if="
                totleInfo.returnStatus &&
                totleInfo.returnStatus != '' &&
                type == '3'
              "
              align="center"
              prop="returnStatus"
              :label="$t('orderDetails.tkzt')"
              width="152"
            >
              <template slot-scope="scope">
                <el-link
                  @click="nato(scope.row)"
                  type="primary"
                  :underline="false"
                  >{{ scope.row.returnInfo.statusName }}</el-link
                >
              </template>
            </el-table-column>
            <el-table-column
              prop="p_price"
              :label="$t('orderDetails.xj')"
              width="152"
            >
              <template slot-scope="scope">
                <span v-if="scope.row.p_price">{{ totleInfo.currency_symbol  }}{{
                    LaiKeCommon.formatPrice((Number(scope.row.p_price) * Number(scope.row.num)),totleInfo.exchange_rate)
                }}</span>
              </template>
            </el-table-column>
            <!-- 非虚拟订单展示这个 -->
            <el-table-column
              v-if="
                totleInfo.returnStatus &&
                totleInfo.returnStatus != '' &&
                type != '3'
              "
              align="center"
              prop="returnStatus"
              :label="$t('orderDetails.tkzt')"
            >
              <template slot-scope="scope">
                <el-link
                  @click="nato(scope.row)"
                  type="primary"
                  :underline="false"
                  >{{ scope.row.returnInfo.statusName }}</el-link
                >
              </template>
            </el-table-column>
            <el-table-column
              prop="stockNum"
              :label="$t('orderDetails.kc')"
              width="152"
              v-if="type != 3"
            >
              <template slot-scope="scope">
                <el-link
                  type="primary"
                  :underline="false"
                  @click="
                    $router.push(
                      '/goods/inventoryManagement/inventoryList?productTitle=' +
                        scope.row.p_name
                    )
                  "
                  >{{ scope.row.stockNum }}</el-link
                >
              </template>
            </el-table-column>
            <!-- write_off_settings为1是线下核销 -->
            <!-- show_write_time为1是有预约信息 -->
            <!-- 有预约的不展示 无需核销不展示 -->
            <el-table-column
              :label="$t('orderDetails.cz')"
              align="center"
              width="140"
              v-if="
                type == 3 && write_off_settings == 1 && dataInfo.show_write_time == 0
              "
            >
              <template slot-scope="scope">
                <div class="OP-button">
                  <div class="OP-button-top">
                    <el-button
                     class="store"
                      icon="el-icon-store"
                      @click="openStore(scope.row)"
                      >{{ $t('orderLists.jxmd') }}</el-button
                    >
                  </div>
                </div>
              </template>
            </el-table-column>
            <!-- <el-table-column :label="$t('orderDetails.cz')" align="center" width="140">
                        <template>
                            <div class="OP-button">
                                <div class="OP-button-top">
                                    <el-button icon="el-icon-view" @click="$router.push('/goods/inventoryManagement/inventoryList')">{{$t('orderDetails.ck')}}</el-button>
                                </div>
                            </div>
                        </template>
                    </el-table-column> -->
          </el-table>
        </div>

        <div class="statistical">
          <div v-if="goodsTables && goodsTables.length>0" class="list-totle">
            {{ $t('home.homeReport.spzs') }}:{{ goodsTables.length }}
          </div>
          <div class="goods-totalprice">
            <span class="title name">{{ $t('orderDetails.spzj') }}：</span>
            <span class="price">{{ totleInfo.currency_symbol }}{{ LaiKeCommon.formatPrice(totleInfo.spz_price,totleInfo.exchange_rate) }}</span>
          </div>
          <div class="goods-totalprice" v-if="totleInfo.z_freight">
            <span class="title name">{{ $t('orderDetails.yf') }}：</span>
            <span class="price">{{ totleInfo.currency_symbol  }}{{  LaiKeCommon.formatPrice(totleInfo.z_freight,totleInfo.exchange_rate)  }}</span>
          </div>
          <div class="discount-stores">
            <span class="title name">{{ $t('orderDetails.dpyh') }}：</span>
            <span class="price">{{ totleInfo.currency_symbol  }}{{ LaiKeCommon.formatPrice(totleInfo.coupon_price,totleInfo.exchange_rate) }}</span>
          </div>
          <div class="discount-platform">
            <span class="title name">{{ $t('orderDetails.ptyh') }}：</span>
            <span class="price"
              >{{ totleInfo.currency_symbol  }}{{ LaiKeCommon.formatPrice(totleInfo.preferential_amount,totleInfo.exchange_rate)  }}</span
            >
          </div>
          <div class="discount-platform" v-if="totleInfo.grade_rate_amount">
            <span class="title name">{{ $t('orderDetails.hyyh') }}：</span>
            <span class="price"
              >{{ totleInfo.currency_symbol  }}{{ LaiKeCommon.formatPrice(totleInfo.grade_rate_amount,totleInfo.exchange_rate)   }}</span
            >
          </div>
          <div class="totle-pay">
            <span
              v-if="
                dataInfo.status == '待付款' ||
                (dataInfo.status == '订单关闭' && !dataInfo.pay_time)
              "
              class="title-totle name"
              >应支付：</span
            >
            <span v-else class="title-totle name"
              >{{ $t('orderDetails.hjzf') }}：</span
            >
            <!-- <span class="red price">￥{{ totleInfo.pay_price.toFixed(2) }}</span> -->
            <span class="red price">{{ totleInfo.currency_symbol  }}{{ LaiKeCommon.formatPrice(totleInfo.old_total,totleInfo.exchange_rate) }}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="dialog-orderInfo">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('orderDetails.symd')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <div class="orderBox">
          <el-table
            class="el-table"
            :data="storeList"
            style="width: 100%"
            height="412"
          >
            <el-table-column
              align="center"
              prop="name"
              :label="$t('orderDetails.hxmd')"
            >
            </el-table-column>
            <el-table-column
              align="center"
              prop="address"
              :label="$t('orderDetails.mddz')"
            >
              <template slot-scope="scope">
                <div class="div_center">
                  <span class="span_center">{{ scope.row.address }}</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-dialog>
    </div>
    <div class="dialog-orderInfo">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('orderDetails.hxjl')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <div class="orderBox">
          <el-table
            class="el-table"
            :data="recordList"
            style="width: 100%"
            height="412"
          >
            <el-table-column
              align="center"
              prop="name"
              :label="$t('orderDetails.hxmd')"
            >
            </el-table-column>
            <el-table-column
              align="center"
              prop="code"
              :label="$t('orderDetails.hxm')"
            >
            </el-table-column>
            <el-table-column
              align="center"
              prop="time"
              :label="$t('orderDetails.hxsja')"
            >
            </el-table-column>
          </el-table>
        </div>
      </el-dialog>
    </div>

  </div>
</template>

<script>
import orderDetails from '@/webManage/js/order/orderList/orderDetails'
export default orderDetails
</script>

<style scoped lang="less">
@import '../../../../webManage/css/order/orderList/orderDetails.less';
.list-totle{
  margin: 10px 0px;
    position: absolute;
    left: 0px;
}
.span_kip_div {
  width: 160px;
  max-height: 40px;
  -webkit-box-orient: vertical;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}
.notes_head {
  min-width: 76px;
}
</style>
