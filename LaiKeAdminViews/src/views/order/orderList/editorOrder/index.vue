<template>
  <div class="container1">
    <div class="basic-info" v-if="dataInfo">
        <div class="header">
            <span>{{$t('orderDetails.jcxx')}}</span>
        </div>
        <div class="basic-block">
            <ul class="items">
                <li>
                    <span>{{$t('orderDetails.ddbh')}}：</span><span class="span_kip">{{ dataInfo.sNo }}</span>
                </li>
                <li class="order-status">
                    <div v-if="goodsListType==2">
                        <span v-show="dataInfo.status !== '待付款' && dataInfo.status !== '待发货' &&!totleInfo.isManyMch">{{$t('orderDetails.ddzt')}}：</span>
                    <el-select class="select-input" v-if="dataInfo.status !== '待付款' && dataInfo.status !== '待发货' &&!totleInfo.isManyMch" v-model="ruleForm.status" placeholder="请选择订单状态">
                        <el-option v-for="item in stateList" :key="item.brand_id" :label="item.label" :value="item.value">
                        </el-option>
                    </el-select>
                    <span v-else>{{$t('orderDetails.ddzt')}}：<span class="span_kip">{{ dataInfo.status }}</span></span>
                    </div>
                    <div v-else>
                        <span v-show="dataInfo.status == '待付款' && !totleInfo.isManyMch">{{$t('orderDetails.ddzt')}}：</span>
                    <el-select class="select-input" v-if="dataInfo.status == '待付款' && !totleInfo.isManyMch" v-model="ruleForm.status" placeholder="请选择订单状态">
                        <el-option v-for="item in stateList" :key="item.brand_id" :label="item.label" :value="item.value">
                        </el-option>
                    </el-select>
                    <span v-else>{{$t('orderDetails.ddzt')}}：<span class="span_kip">{{ dataInfo.status }}</span></span>
                    </div>

                </li>
                <li>
                    <span>{{$t('orderDetails.ddlx')}}：</span><span class="span_kip">{{ dataInfo.orderTypeName }}订单</span>
                </li>
                <li>
                    <span>{{$t('orderDetails.ddly')}}：</span><span class="span_kip">{{ $myGetSource(dataInfo.source) }}</span>
                </li>
            </ul>
            <ul class="items">
                <li>
                    <span>{{$t('orderDetails.zffs')}}：</span><span class="span_kip">{{ dataInfo.paytype }}</span>
                </li>
                <li>
                    <span>{{$t('orderDetails.xdsj')}}：</span><span class="span_kip">{{ dataInfo.add_time | dateFormat}}</span>
                </li>
                <li>
                    <span>{{$t('orderDetails.fksj')}}：</span><span class="span_kip">{{ dataInfo.pay_time}}</span>
                </li>
                <li>
                    <span>{{$t('orderDetails.fhsj')}}：</span><span class="span_kip">{{ dataInfo.deliver_time?dataInfo.deliver_time:$t('zdata.zwtm')}}</span>
                </li>
            </ul>
            <ul class="items">
                <li>
                    <span>{{$t('orderDetails.shsj')}}：</span><span class="span_kip">{{ dataInfo.arrive_time?dataInfo.arrive_time:$t('zdata.zwtm')}}</span>
                </li>
                <li>
                    <span>{{$t('orderDetails.yhid')}}：</span><span class="span_kip">{{ dataInfo.user_id }}</span>
                </li>
                <li>
                    <span>{{$t('orderDetails.yhmc')}}：</span><span class="span_kip">{{ dataInfo.user_name }}</span>
                </li>
                <li class="notes">
                    <!-- <span>订单备注：</span> -->
                    <!-- <ul class="remaks" v-if="noteList">
                        <li v-for="(item,index) in noteList" :key="index">
                            {{ item }}
                        </li>
                    </ul> -->
                    <span>{{$t('orderDetails.ddbz')}}：</span><span class="span_kip">{{ dataInfo.remarks ? dataInfo.remarks : '' }}</span>
                </li>
            </ul>
        </div>
    </div>
    <div class="consignee-info" v-if="dataInfo">
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
            <el-form ref="ruleForm" :rules="rules" class="form-search" :model="ruleForm" label-width="auto">
              <el-form-item :label="$t('orderDetails.shr')" prop="name" >
                <el-input v-model="ruleForm.name" :placeholder="$t('orderDetails.qsrshr')"></el-input>
              </el-form-item>
              <el-form-item :label="$t('orderDetails.xldh')" prop="mobile" >
                 
                <el-autocomplete
                  class='custom-autocomplete'
                    v-model="code2"
                    :fetch-suggestions="querySearchAsync"
                    :placeholder="$t('membersLists.qh')"
                    @select="handleSelect"
                  >
                  <template slot-scope="{ item }">
                    <div>
                      <span>{{ item.name }}</span>
                      <span style="color: #999; margin-left: 10px;">+({{ item.code2 }})</span>
                    </div>
                  </template>
                  </el-autocomplete>
                <el-input v-model="ruleForm.mobile" :placeholder="$t('orderDetails.qsrlxdh')" style="width: 250px;" class="inputs"></el-input>
              </el-form-item>
              <!-- self_lifting 1 自提订单  0 配送订单 -->
              <el-form-item class="cascadeAddress" :label="$t(totleInfo.selfLifting == 2?'配送地址':'orderDetails.shdz')" prop="xian" 
                v-if="dataInfo.self_lifting != 1 && ['86','852','853'].includes(code2) "
              >
                <div class="cascadeAddress-block">
                  <el-select class="select-input" v-model="ruleForm.sheng" :placeholder="$t('orderDetails.sheng')">
                    <el-option v-for="(item,index) in shengList" :key="index" :label="item.districtName" :value="item.districtName">
                      <div @click="getShi(item.id)">{{ item.districtName }}</div>
                    </el-option>
                  </el-select>
                  <el-select class="select-input" v-model="ruleForm.shi" :placeholder="$t('orderDetails.shi')">
                    <el-option v-for="(item,index) in shiList" :key="index" :label="item.districtName" :value="item.districtName">
                      <div @click="getXian(item.id)">{{ item.districtName }}</div>
                    </el-option>
                  </el-select>
                  <el-select class="select-input" v-model="ruleForm.xian" :placeholder="$t('orderDetails.xian')">
                    <el-option v-for="(item,index) in xianList" :key="index" :label="item.districtName" :value="item.districtName">
                      <div>{{ item.districtName }}</div>
                    </el-option>
                  </el-select>
                </div>
              </el-form-item>
              <el-form-item :label="$t('increaseStore.xxdz')" prop="r_address" v-if="dataInfo.self_lifting != 1 ">
                <el-input v-model="ruleForm.r_address" placeholder=""></el-input>
              </el-form-item>
              <!-- <el-form-item :label="订单备注" prop="remarks">
                <el-input v-model="ruleForm.remarks" placeholder="请输入订单备注"></el-input>
              </el-form-item> -->
              <!-- 商家配送 配送时间 -->
              <el-form-item
                v-if="totleInfo.selfLifting == 2 && ruleForm.sjpsData !=''"
                class="cascadeAddress"
                :label="$t('配送时间')"
              >
                <div class="cascadeAddress-block">
                  <!-- 商家配送 选择日期 -->
                  <el-select
                    class="select-input"
                    v-model="ruleForm.sjpsData"
                    :placeholder="$t('选择日期')"
                  >
                    <el-option
                      v-for="(item, index) in sjpsDaList"
                      :key="index"
                      :label="item"
                      :value="item"
                    >
                      <div @click="ruleForm.sjpsData = item">{{ item }}</div>
                    </el-option>
                  </el-select>
                  <!-- 商家配送 选择时间 -->
                  <el-select
                    class="select-input"
                    v-model="ruleForm.sjpsTime"
                    :placeholder="$t('选择时间')"
                  >
                    <el-option
                      v-for="(item, index) in sjpsRqList"
                      :key="index"
                      :label="item.name"
                      :value="item.name"
                    >
                      <div @click="ruleForm.sjpsTimeId = item.id">{{ item.name }}</div>
                    </el-option>
                  </el-select>
                </div>
              </el-form-item>
            </el-form>
        </div>
    </div>
    <div class="goods-info" v-if="dataInfo">
        <div class="header">
            <span>{{$t('orderDetails.spxx')}}</span>
        </div>
        <div class="goods-block">
            <div class="dictionary-list">
                <el-table :data="goodsTables" ref="table" class="el-table" style="width: 100%">
                    <el-table-column prop="p_name" :label="$t('orderDetails.spmc')" width="400">
                        <template slot-scope="scope">
                            <div class="name-info">
                                <img :src="scope.row.pic" alt="">
                                <span>{{ scope.row.p_name }}</span>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="size" :label="$t('orderDetails.spgg')" >
                    </el-table-column>
                    <el-table-column prop="p_id" :label="$t('orderDetails.spbh')" width="152">
                    </el-table-column>
                    <el-table-column prop="p_price" :label="$t('orderDetails.dj')" width="152">
                        <template slot-scope="scope">
                            <span v-if="scope.row.p_price">{{totleInfo.currency_symbol }}{{ scope.row.p_price.toFixed(2) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="num" :label="$t('orderDetails.sl')" width="152">
                    </el-table-column>
                    <el-table-column prop="stockNum" :label="$t('orderDetails.kc')" width="152">
                    </el-table-column>
                    <el-table-column prop="p_price" :label="$t('orderDetails.xj')" width="152">
                        <template slot-scope="scope">
                            <span v-if="scope.row.p_price">{{totleInfo.currency_symbol }}{{ (Number(scope.row.p_price) *  Number(scope.row.num)).toFixed(2) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('orderDetails.cz')" align="center" width="140">
                        <template>
                            <div class="OP-button">
                                <div class="OP-button-top">
                                    <el-button icon="el-icon-view" @click="$router.push('/goods/inventoryManagement/inventoryList')">{{$t('orderDetails.ck')}}</el-button>
                                </div>
                            </div>
                        </template>
                    </el-table-column>
                </el-table>
            </div>

            <div class="statistical">
                <div class="goods-totalprice">
                    <span class="title name">{{$t('orderDetails.spzj')}}：</span>
                    <span class="price">{{totleInfo.currency_symbol }}{{ totleInfo.spz_price.toFixed(2) }}</span>
                </div>
                <div class="goods-totalprice" v-if="totleInfo.z_freight">
                    <span class="title name">{{$t('orderDetails.yf')}}：</span>
                    <span class="price">{{totleInfo.currency_symbol }}{{ totleInfo.z_freight.toFixed(2) }}</span>
                </div>
                <div class="discount-stores">
                    <span class="title name">{{$t('orderDetails.dpyh')}}：</span>
                    <span class="price">{{totleInfo.currency_symbol }}{{ totleInfo.coupon_price.toFixed(2) }}</span>
                </div>
                <div class="discount-platform">
                    <span class="title name">{{$t('orderDetails.ptyh')}}：</span>
                    <span class="price">{{totleInfo.currency_symbol }}{{ totleInfo.preferential_amount.toFixed(2) }}</span>
                </div>
                <div class="discount-platform" v-if="totleInfo.grade_rate_amount">
                    <span class="title name">{{$t('orderDetails.hyyh')}}：</span>
                    <span class="price">{{totleInfo.currency_symbol }}{{ totleInfo.grade_rate_amount.toFixed(2) }}</span>
                </div>
                <div class="totle-pay">
                    <span v-if="dataInfo.status=='待付款'||(dataInfo.status=='已关闭'&&!dataInfo.pay_time)" class="title-totle  name">应支付：</span>
                    <span v-else class="title-totle  name">{{$t('orderDetails.hjzf')}}：</span>
                    <!-- <span class="red price">{{totleInfo.currency_symbol }}{{ totleInfo.pay_price.toFixed(2) }}</span> -->
                    <span class="red price">{{totleInfo.currency_symbol }}{{ totleInfo.old_total }}</span>
                </div>
            </div>
        </div>
        <div class="hr myHr" style=""></div>
    </div>
    <!-- <div class="hr" style="border-radius:0;heigth:400px;margin-bottom:100px;"></div> -->
    <div class="footer-button">
      <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
      <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
    </div>
  </div>
</template>

<script>
import orderDetails from '@/webManage/js/order/orderList/editorOrder'
export default orderDetails
</script>

<style scoped lang="less">
@import '../../../../webManage/css/order/orderList/editorOrder.less';
.myHr{
    background: #fff;margin-bottom:50px;
}
/deep/.inputs{
  width: 250px !important;
  .el-input__inner{
    width: 250px !important;
  }
}
/deep/.custom-autocomplete{
  margin-right: 12px !important;
  .el-input{
      width: 135px !important;
    .el-input__inner{
      width: 135px !important;
    }
  }
}
</style>
