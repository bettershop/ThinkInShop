<template>
  <div class="container">
    <div class="merchants-list">
      <el-form
        :model="mainData"
        element-loading-text="拼命加载中..."
        ref="ruleForm"
        class="demo-ruleForm"
      >
        <div class="card">
          <div class="title">{{ $t('orderDetails.jcxx') }}</div>
          <el-row :gutter="4">
            <el-col :lg="6">
              <el-form-item :label="$t('orderDetails.ddbh') + ':'">
                {{ mainData.sNo }}
              </el-form-item>
            </el-col>
            <el-col :lg="6">
              <el-form-item :label="$t('orderDetails.ddzt') + ':'">
                {{ mainData.status }}
              </el-form-item>
            </el-col>
            <el-col :lg="6">
              <el-form-item :label="$t('orderDetails.ddlx') + ':'">
                {{ mainData.orderTypeName }}订单
              </el-form-item>
            </el-col>
            <!-- 普通订单展示 -->
            <el-col :lg="6" v-if="otype == 'GM'">
              <el-form-item :label="$t('orderDetails.psfy') + ':'">
                {{laikeCurrencySymbol}}{{ orderInfo.z_freight }}
              </el-form-item>
            </el-col>
            <!-- 虚拟订单展示 -->
            <el-col :lg="6" v-if="otype == 'VI'">
              <el-form-item :label="$t('orderDetails.ddly') + ':'">
                <template>
                  {{ $myGetSource(mainData.source) }}
                </template>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="4">
            <el-col :lg="6" v-if="otype == 'GM'">
              <el-form-item :label="$t('orderDetails.ddly') + ':'">
                <template>
                  {{ $myGetSource(mainData.source) }}
                </template>
              </el-form-item>
            </el-col>
            <el-col :lg="6">
              <el-form-item :label="$t('orderDetails.zffs') + ':'">
                {{ mainData.paytype }}
              </el-form-item>
            </el-col>
            <el-col :lg="6">
              <el-form-item :label="$t('orderDetails.xdsj') + ':'">
                {{ mainData.add_time | dateFormat }}
              </el-form-item>
            </el-col>
            <el-col :lg="6">
              <el-form-item :label="$t('orderDetails.fksj') + ':'">
                {{ mainData.pay_time | dateFormat }}
              </el-form-item>
            </el-col>
            <el-col :lg="6" v-if="otype == 'VI'">
              <el-form-item :label="$t('orderDetails.wcsj') + ':'">
                <template>
                  {{ mainData.arrive_time | dateFormat }}
                </template>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="4">
            <el-col :lg="6" v-if="otype == 'GM'">
              <el-form-item
                :label="$t('orderDetails.fhsj') + ':'"
                v-if="mainData.arrive_time != null"
              >
                {{ mainData.deliver_time | dateFormat }}
              </el-form-item>
            </el-col>
            <el-col :lg="6" v-if="otype == 'GM'">
              <el-form-item :label="$t('orderDetails.shsj') + ':'">
                {{ mainData.arrive_time | dateFormat }}
              </el-form-item>
            </el-col>
            <el-col :lg="6" v-if="otype == 'GM'">
              <el-form-item :label="$t('orderDetails.kddh') + ':'">
                {{ orderInfo.expressStr }}
              </el-form-item>
            </el-col>
            <el-col :lg="6" v-if="otype == 'VI'">
              <el-form-item :label="$t('orderDetails.yhid') + ':'">
                {{ mainData.user_id }}
              </el-form-item>
            </el-col>
            <el-col :lg="6" v-if="otype == 'VI'">
              <el-form-item :label="$t('orderDetails.yhmc') + ':'">
                {{ mainData.user_name }}
              </el-form-item>
            </el-col>
            <el-col :lg="6">
              <el-form-item :label="$t('orderDetails.ddbz') + ':'">
                <el-tooltip
                  v-if="mainData.remark"
                  :content="mainData.remark"
                  placement="bottom"
                  effect="light"
                >
                  <div class="span_kip_div">
                    {{ mainData.remark }}
                  </div>
                </el-tooltip>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
    </div>

    <div class="merchants-list" v-if="otype == 'GM'">
      <el-form
        :model="mainData"
        element-loading-text="拼命加载中..."
        ref="ruleForm"
        class="demo-ruleForm"
      >
        <div class="card">
          <div class="title">{{ $t('orderDetails.shrxx') }}</div>
          <el-row :gutter="1">
            <el-col :lg="12">
              <el-form-item :label="$t('orderDetails.shr') + ':'">
                {{ mainData.user_name }}
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="1">
            <el-col :lg="12">
              <el-form-item :label="$t('orderDetails.xldh') + ':'">
                {{ mainData.mobile }}
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="1">
            <el-col :lg="12">
              <el-form-item :label="$t('orderDetails.shdz') + ':'">
                {{ mainData.address }}
              </el-form-item>
            </el-col>
          </el-row>
          <!-- <el-row :gutter="1" style="padding-bottom:8px">
            <el-col :lg="12">
              <el-form-item :label="$t('orderDetails.ddbz')+':'">
                {{mainData.remark}}
              </el-form-item>
            </el-col>
          </el-row> -->
        </div>
      </el-form>
    </div>
    <div class="merchants-list" v-if="otype == 'VI' && write_off_settings == 1 && mainData.show_write_time ==1">
      <el-form :model="mainData" element-loading-text="拼命加载中..." ref="ruleForm"
              class="demo-ruleForm">
        <div class="card">
          <div class="title">{{$t('orderDetails.yyxxa')}}</div>
          <el-row :gutter="1">
            <el-col :lg="12">
              <el-form-item :label="$t('orderDetails.yysj')+':'">
                {{mainData.write_time_info.time}}
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="1">
            <el-col :lg="12">
              <el-form-item :label="$t('orderLists.hxyymd')+':'">
                {{mainData.write_time_info.mch_store}}
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="1">
            <el-col :lg="12">
              <el-form-item :label="$t('orderDetails.mddza')+':'">
                {{mainData.write_time_info.address}}
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
    </div>
    <div class="merchants-list">
      <div class="card">
        <div class="title">{{ $t('orderDetails.spxx') }}</div>
        <div class="dictionary-list">
          <el-table
            :header-cell-style="{ background: '#f4f7f9', height: '50px' }"
            :data="goodsList"
            ref="table"
            class="el-table"
            style="width: 100%"
          >
            <el-table-column
              prop="p_id"
              align="center"
              :label="$t('orderDetails.spbh')"
            >
            </el-table-column>
            <el-table-column
              prop="p_name"
              align="center"
              :label="$t('orderDetails.spmc')"
              width="400"
            >
              <template slot-scope="scope">
                <div class="name-info">
                  <img :src="scope.row.pic" alt="" />
                  <span>{{ scope.row.p_name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              prop="size"
              align="center"
              :label="$t('orderDetails.spgg')"
            >

            </el-table-column>
            <el-table-column
              prop="p_price"
              align="center"
              :label="$t('orderDetails.dj')"
            >
              <template slot-scope="scope">
                <span>{{laikeCurrencySymbol}}{{ Number(scope.row.p_price|| 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="num"
              align="center"
              :label="$t('orderDetails.sl')"
            >
            </el-table-column>
            <!-- 已核销次数 只有虚拟订单且是线下核销才需要展示 -->
            <el-table-column
              v-if="
                otype == 'VI' &&
                (mainData.status == '订单完成' ||
                mainData.status == '待核销') &&
                write_off_settings == 1
              "
              prop="after_write_off_num"
              align="center"
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
              v-if="otype == 'VI' && write_off_settings == 1"
              prop="write_off_num"
              align="center"
              :label="$t('orderDetails.dhxcs')"
              width="152"
            >
            </el-table-column>
            <!-- 退款核销次数 只有虚拟订单且是线下核销才需要展示 -->
            <el-table-column
              v-if="
                otype == 'VI' &&
                mainData.status != '待付款' &&
                orderInfo.returnStatus &&
                orderInfo.returnStatus != '' &&
                write_off_settings == 1
              "
              align="center"
              prop="r_write_off_num"
              :label="$t('orderDetails.tkhxcs')"
              width="152"
            >
            </el-table-column>
             <!-- 虚拟订单展示这个 -->
             <el-table-column
              v-if="
                orderInfo.returnStatus &&
                orderInfo.returnStatus != '' &&
                otype == 'VI'
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
              align="center"
              :label="$t('orderDetails.xj')"
            >
              <template slot-scope="scope">
                {{laikeCurrencySymbol}} {{ Number(scope.row.p_price * scope.row.num ||0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column
            v-if="
                orderInfo.returnStatus &&
                orderInfo.returnStatus != '' &&
                otype == 'GM'
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
              align="center"
              v-if="otype != 'VI' "
              :label="$t('orderDetails.kc')"
            >
              <template slot-scope="scope">
                <el-link
                  @click="natoa(scope.row)"
                  type="primary"
                  :underline="false"
                  >{{ scope.row.stockNum }}</el-link
                >
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('orderDetails.cz')"
              align="center"
              width="140"
              v-if="
                otype == 'VI'  && write_off_settings == 1 && mainData.show_write_time == 0
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
          </el-table>
        </div>
      </div>
    </div>
    <div class="statistical" style="margin-bottom: 0">
      <div class="goods-totalprice">
        <span class="title name">{{ $t('orderDetails.spzj') }}：</span>
        <span class="price">{{laikeCurrencySymbol}}{{ orderInfo.spz_price.toFixed(2) }} </span>
      </div>
      <div class="discount-stores" v-if="orderInfo.z_freight">
        <span class="title name">{{ $t('orderDetails.yf') }}：</span>
        <span class="price">{{laikeCurrencySymbol}}{{ orderInfo.z_freight.toFixed(2) }}</span>
      </div>
      <div class="discount-stores">
        <span class="title name">{{ $t('orderDetails.dpyh') }}：</span>
        <span class="price">{{laikeCurrencySymbol}}{{ orderInfo.reduce_price.toFixed(2) }}</span>
      </div>
      <div class="discount-platform">
        <span class="title name">{{ $t('orderDetails.ptyh') }}：</span>
        <span class="price"
          >{{laikeCurrencySymbol}}{{ orderInfo.preferential_amount.toFixed(2) }}</span
        >
      </div>
      <div class="totle-pay">
        <span class="title-totle name">{{ $t('orderDetails.hjzf') }}：</span>
        <span class="red price">{{laikeCurrencySymbol}}{{ mainData.z_price.toFixed(2) }}</span>
      </div>
    </div>
    <div class="statistical" style="margin-bottom: 0">
      <div class="hr"></div>
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
import main from '@/webManage/js/order/orderSettlement/orderSettlementDetail'

export default main
</script>

<style scoped lang="less">
@import '../../../webManage/css/order/orderSettlement/orderSettlementDetail';
.span_kip_div {
  width: 160px;
  height: 40px;
  -webkit-box-orient: vertical;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
