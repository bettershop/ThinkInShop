<template v-cloak>
  <div class="iframe-content">
    <div class="shaco_box">
      <div class="chart">
        <div>
          <span class="shaco_font">{{$t('ordereport.dfk')}}</span>
        </div>
        <div class="chart_bottom">
          <span class="font_two">{{ obligationList.num }}</span>
        </div>
        <div class="shaco_flex">
          <div>
            <div class="font_one">
              <span v-if="time_one == true">{{$t('ordereport.rtb')}}</span>
              <span v-if="time_one == false">{{$t('ordereport.ztb')}}</span>
              {{ obligationList.tbrate }}
                <i v-if="obligationList.tbflag == 'up'" class="el-icon-caret-top"></i>
                <i v-if="obligationList.tbflag == 'down'" class="el-icon-caret-bottom"></i>
            </div>
          </div>
          <div class="chart_left">
            <span class="font_one">
              <span v-if="time_one == true">{{$t('ordereport.rhb')}}</span>
              <span v-if="time_one == false">{{$t('ordereport.zhb')}}</span>
              {{ obligationList.hbrate }}
              <i v-if="obligationList.hbflag == 'up'" class="el-icon-caret-top"></i>
              <i v-if="obligationList.hbflag == 'down'" class="el-icon-caret-bottom"></i>
            </span>
          </div>
          <div class="time_box">
            <div
              :class="time_one == true ? 'time_change' : 'time_left'"
              @click="changeTime_one(1)"
            >
              <span>{{$t('ordereport.ri')}}</span>
            </div>
            <div
              :class="time_one == false ? 'time_change' : 'time_right'"
              @click="changeTime_one(2)"
            >
              <span>{{$t('ordereport.zhou')}}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart">
        <div>
          <span class="shaco_font">{{$t('ordereport.dfh')}}</span>
        </div>
        <div class="chart_bottom">
          <span class="font_two">{{ waitList.num }}</span>
        </div>
        <div class="shaco_flex">
          <div>
            <span class="font_one">
              <span v-if="time_two == true">{{$t('ordereport.rtb')}}</span>
              <span v-if="time_two == false">{{$t('ordereport.ztb')}}</span>
              {{ waitList.tbrate }}
              <i v-if="waitList.tbflag == 'up'" class="el-icon-caret-top"></i>
              <i v-if="waitList.tbflag == 'down'" class="el-icon-caret-bottom"></i>
            </span>
          </div>
          <div class="chart_left">
            <span class="font_one">
              <span v-if="time_two == true">{{$t('ordereport.rhb')}}</span>
              <span v-if="time_two == false">{{$t('ordereport.zhb')}}</span>
              {{ waitList.hbrate }}
              <i v-if="waitList.hbflag == 'up'" class="el-icon-caret-top"></i>
              <i v-if="waitList.hbflag == 'down'" class="el-icon-caret-bottom"></i>
            </span>
          </div>
          <div class="time_box">
            <div
              :class="time_two == true ? 'time_change' : 'time_left'"
              @click="changeTime_two(1)"
            >
              <span>{{$t('ordereport.ri')}}</span>
            </div>
            <div
              :class="time_two == false ? 'time_change' : 'time_right'"
              @click="changeTime_two(2)"
            >
              <span>{{$t('ordereport.zhou')}}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart">
        <div>
          <span class="shaco_font">{{$t('ordereport.dsh')}}</span>
        </div>
        <div class="chart_bottom">
          <span class="font_two">{{ receList.num }}</span>
        </div>
        <div class="shaco_flex">
          <div>
            <span class="font_one">
              <span v-if="time_three == true">{{$t('ordereport.rtb')}}</span>
              <span v-if="time_three == false">{{$t('ordereport.ztb')}}</span>
              {{ receList.tbrate }}
              <i v-if="receList.tbflag == 'up'" class="el-icon-caret-top"></i>
              <i v-if="receList.tbflag == 'down'" class="el-icon-caret-bottom"></i>
            </span>
          </div>
          <div class="chart_left">
            <span class="font_one">
              <span v-if="time_three == true">{{$t('ordereport.rhb')}}</span>
              <span v-if="time_three == false">{{$t('ordereport.zhb')}}</span>
              {{ receList.hbrate }}
              <i v-if="receList.hbflag == 'up'" class="el-icon-caret-top"></i>
              <i v-if="receList.hbflag == 'down'" class="el-icon-caret-bottom"></i>
            </span>
          </div>
          <div class="time_box">
            <div
              :class="time_three == true ? 'time_change' : 'time_left'"
              @click="changeTime_three(1)"
            >
              <span>{{$t('ordereport.ri')}}</span>
            </div>
            <div
              :class="time_three == false ? 'time_change' : 'time_right'"
              @click="changeTime_three(2)"
            >
              <span>{{$t('ordereport.zhou')}}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart">
        <div>
          <span class="shaco_font">{{$t('ordereport.dpj')}}</span>
        </div>
        <div class="chart_bottom">
          <span class="font_two">{{ appraiseList.num }}</span>
        </div>
        <div class="shaco_flex">
          <div>
            <span class="font_one">
              <span v-if="time_four == true">{{$t('ordereport.rtb')}}</span>
              <span v-if="time_four == false">{{$t('ordereport.ztb')}}</span>
              {{ appraiseList.tbrate }}
              <i v-if="appraiseList.tbflag == 'up'" class="el-icon-caret-top"></i>
              <i v-if="appraiseList.tbflag == 'down'" class="el-icon-caret-bottom"></i>
            </span>
          </div>
          <div class="chart_left">
            <span class="font_one">
              <span v-if="time_four == true">{{$t('ordereport.rhb')}}</span>
              <span v-if="time_four == false">{{$t('ordereport.zhb')}}</span>
              {{ appraiseList.hbrate }}
              <i v-if="appraiseList.hbflag == 'up'" class="el-icon-caret-top"></i>
              <i v-if="appraiseList.hbflag == 'down'" class="el-icon-caret-bottom"></i>
            </span>
          </div>
          <div class="time_box">
            <div
              :class="time_four == true ? 'time_change' : 'time_left'"
              @click="changeTime_four(1)"
            >
              <span>{{$t('ordereport.ri')}}</span>
            </div>
            <div
              :class="time_four == false ? 'time_change' : 'time_right'"
              @click="changeTime_four(2)"
            >
              <span>{{$t('ordereport.zhou')}}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart">
        <div>
          <span class="shaco_font">{{$t('ordereport.clz')}}</span>
        </div>
        <div class="chart_bottom">
          <span class="font_two">{{ processedList.num }}</span>
        </div>
        <div class="shaco_flex">
          <div>
            <span class="font_one">
              <span v-if="time_five == true">{{$t('ordereport.rtb')}}</span>
              <span v-if="time_five == false">{{$t('ordereport.ztb')}}</span>
              {{ processedList.tbrate }}
              <i v-if="processedList.tbflag == 'up'" class="el-icon-caret-top"></i>
              <i v-if="processedList.tbflag == 'down'" class="el-icon-caret-bottom"></i>
            </span>
          </div>
          <div class="chart_left">
            <span class="font_one">
              <span v-if="time_five == true">{{$t('ordereport.rhb')}}</span>
              <span v-if="time_five == false">{{$t('ordereport.zhb')}}</span>
              {{ processedList.hbrate }}
              <i v-if="processedList.hbflag == 'up'" class="el-icon-caret-top"></i>
              <i v-if="processedList.hbflag == 'down'" class="el-icon-caret-bottom"></i>
            </span>
          </div>
          <div class="time_box">
            <div
              :class="time_five == true ? 'time_change' : 'time_left'"
              @click="changeTime_five(1)"
            >
              <span>{{$t('ordereport.ri')}}</span>
            </div>
            <div
              :class="time_five == false ? 'time_change' : 'time_right'"
              @click="changeTime_five(2)"
            >
              <span>{{$t('ordereport.zhou')}}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="shaco_box">
      <div class="amount">
        <div class="amount_head">
          <div
            class="shaco_flex"
            style="justify-content: center; align-items: center"
          >
            <span class="font_one">{{$t('ordereport.ssdds')}}</span>
            <el-tooltip
              style="margin-left: 16px; padding-top: 2px"
              effect="dark"
              :content="$t('ordereport.jrld')"
              placement="right"
            >
              <img src="../../../assets/imgs/popover.png" />
            </el-tooltip>
          </div>
        </div>
        <div class="amount_body_box">
          <div class="amount_bottom">
            <div>
              <span class="font_two">
                {{ orderNumberList.today_num }}
              </span>
              <span class="amount_left">
                <i
                  v-if="orderNumberList.flag == 'up'"
                  class="el-icon-caret-top"
                ></i>
                <i
                  v-if="orderNumberList.flag == 'down'"
                  class="el-icon-caret-bottom"
                ></i>
                {{ orderNumberList.add_rate }}
              </span>
            </div>
            <div class="amount_div">
              <img style="width: 100%;" src="../../../assets/imgs/order_one.png" />
            </div>
          </div>
          <div class="amount_mleft">
            <div class="amount_body">
              <span class="font_three">{{$t('ordereport.zrdd')}}</span
              ><span class="font_four">{{ orderNumberList.yesterday_num }}</span>
            </div>
            <div class="amount_body">
              <span class="font_three">{{$t('ordereport.bydd')}}</span
              ><span class="font_four">{{ orderNumberList.month_num }}</span>
            </div>
            <div class="amount_body">
              <span class="font_three">{{$t('ordereport.ljdd')}}</span
              ><span class="font_four">{{ orderNumberList.all_num }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="amount">
        <div class="amount_head">
          <div>
            <span class="font_one">{{$t('ordereport.ddje')}}</span>
          </div>
        </div>
        <div class="amount_body_box">
          <div class="amount_body_box1">
            <div class="amount_bottom_two">
              <span class="font_two">{{laikeCurrencySymbol}}{{ orderMoneyList.today_money }}</span
              ><span class="amount_left">
                <i
                  v-if="orderMoneyList.flag == 'up'"
                  class="el-icon-caret-top"
                ></i>
                <i
                  v-if="orderMoneyList.flag == 'down'"
                  class="el-icon-caret-bottom"
                ></i>
                {{ orderMoneyList.add_money_rate }}
              </span>
            </div>
            <div class="amount_div_two">
              <img
                src="../../../assets/imgs/order_two.png"
                style="width: 100%"
              />
            </div>
          </div>
          <div class="amount_mleft">
            <div class="amount_body">
              <span class="font_three">{{$t('ordereport.zrddje')}}</span
              ><span class="font_four">{{
                orderMoneyList.yesterday_money
              }}</span>
            </div>
            <div class="amount_body">
              <span class="font_three">{{$t('ordereport.byddje')}}</span
              ><span class="font_four">{{ orderMoneyList.month_money }}</span>
            </div>
            <div class="amount_body">
              <span class="font_three">{{$t('ordereport.ljddje')}}</span
              ><span class="font_four">{{ orderMoneyList.all_money }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="amount">
        <div class="amount_head">
          <div>
            <span class="font_one">{{$t('ordereport.tkje')}}</span>
          </div>
        </div>
        <div class="amount_body_box">
          <div class="amount_body_box1">
            <div class="amount_bottom_three">
              <span class="font_two">{{ laikeCurrencySymbol }}{{ returnList.today_return }}</span
              ><span class="amount_left"
                ><i
                  v-if="returnList.flag == 'up'"
                  class="el-icon-caret-top"
                ></i>
                <i
                  v-if="returnList.flag == 'down'"
                  class="el-icon-caret-bottom"
                ></i>
                {{ returnList.all_return_rate }}</span
              >
            </div>
            <div class="amount_div_three">
              <img src="../../../assets/imgs/order_three.png" style="width: 100%;" />
            </div>
          </div>
          <div class="amount_mleft">
            <div class="amount_body">
              <span class="font_three">{{$t('ordereport.zrddje')}}</span
              ><span class="font_four">{{ returnList.yesterday_return }}</span>
            </div>
            <div class="amount_body">
              <span class="font_three">{{$t('ordereport.byddje')}}</span
              ><span class="font_four">{{ returnList.month_return }}</span>
            </div>
            <div class="amount_body">
              <span class="font_three">{{$t('ordereport.ljddje')}}</span
              ><span class="font_four">{{ returnList.all_return }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="shaco_box">
      <div class="stat">
        <div class="stat_head">
          <div>
            <span class="font_one">{{$t('ordereport.fktk')}}</span>
          </div>
          <div class="shaco_flex">
            <el-button
              :class="dep == 1 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData(1)"
              >{{$t('ordereport.az')}}</el-button
            >
            <el-button
              :class="dep == 2 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData(2)"
              >{{$t('ordereport.ay')}}</el-button
            >
            <el-button
              :class="dep == 3 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData(3)"
              >{{$t('ordereport.an')}}</el-button
            >
          </div>
        </div>
        <div
          style="width: 100%; height: 87.5%; padding: 24px 24px 24px 24px"
          id="refundOrder"
        ></div>
      </div>
      <div class="stat">
        <div class="stat_head">
          <div>
            <span class="font_one">{{$t('ordereport.ddzstj')}}</span>
          </div>
          <div class="shaco_flex">
            <el-button
              :class="kep == 1 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_two(1)"
              >{{$t('ordereport.az')}}</el-button
            >
            <el-button
              :class="kep == 2 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_two(2)"
              >{{$t('ordereport.ay')}}</el-button
            >
            <el-button
              :class="kep == 3 ? 'shaco_bt_dep' : 'shaco_bt_kep'"
              @click="changeData_two(3)"
              >{{$t('ordereport.an')}}</el-button
            >
          </div>
        </div>
        <div
          style="width: 100%; height: 87.5%; padding: 24px 24px 24px 24px"
          id="totalStatistics"
        ></div>
      </div>
    </div>
    <!-- <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <div class="select-date">
            <el-date-picker v-model="page.inputInfo.date"
              type="daterange" range-separator="至" start-placeholder="开始日期"
              end-placeholder="结束日期" value-format="yyyy-MM-dd"
              :editable="false">
            </el-date-picker>
          </div>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button class="bgColor" type="primary" @click="demand">{{$t('DemoPage.tableExamplePage.demand')}}
          </el-button>
        </div>
      </div>
    </div>

    <div class="box">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="grid-content bg-purple">订单总数:<span class="left">{{orderCount.num}}</span></div>
        </el-col>
        <el-col :span="6">
          <div class="grid-content bg-purple">订单总额:<span class="left">{{orderCount.z_price.toFixed(2)}}</span></div>
        </el-col>
        <el-col :span="6">
          <div class="grid-content bg-purple">有效订单数:<span class="left">{{orderCount.yx_num}}</span></div>
        </el-col>
        <el-col :span="6">
          <div class="grid-content bg-purple">有效总额:<span class="left">{{orderCount.yx_amt.toFixed(2)}}</span></div>
        </el-col>
      </el-row>
    </div>

    <div id="orderMap" class="chart" :style="{width: '100%', height: '500px'}"></div>
    <div id="priceMap" class="chart" :style="{width: '100%', height: '500px'}"></div> -->
  </div>
</template>

<script>
import main from '@/webManage/js/data/orde-report/index'

export default main
</script>

<style scoped lang="less">
@import '../../../webManage/css/data/orde-report/index.less';
</style>
