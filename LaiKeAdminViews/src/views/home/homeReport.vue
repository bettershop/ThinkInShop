<template>
  <div class="main">
    <!-- MAIN CONTENT -->
    <div class="main-content">
      <div class="container-fluid">
        <!-- OVERVIEW -->
        <div class="panel panel-headline">
            <div class="panel-body">
            <div class="order-nav">
              <div class="shaco grid-content">
                <div class="shaco_box">
                  <div class="shacoe_box_one">
                    <span class="font_one">{{$t('home.homeReport.spzs')}}</span>
                    <span class="font_two">{{ goodsData.allGoodsAmount }}</span>
                  </div>

                  <div class="shacoe_box_two">
                    <div class="box_img_one">
                      <img
                        src="../../assets/imgs/home_one.png"
                        class="box_img_one"
                      />
                    </div>
                    <div class="shaco_left" style="padding-left: 24px;">
                      <div class="shacoe_box_three">
                        <div class="shaco_bottom">
                          <span class="font_three">{{$t('home.homeReport.cssp')}}</span>
                          <span class="font_four">{{ goodsData.onSaledAmount }}</span>
                        </div>
                      </div>
                      <div class="shacoe_box_three">
                        <div class="shaco_bottom">
                          <span class="font_three">{{$t('home.homeReport.xjsp')}}</span>
                          <span class="font_four">{{ goodsData.underSaledAmount }}</span>
                        </div>
                      </div>
                      <div class="shacoe_box_four">
                        <div class="shaco_bottom">
                          <span class="font_three">{{$t('home.homeReport.yjsp')}}</span>
                          <span class="font_four">{{ goodsData.warnenAnmun }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="shaco grid-content">
                <div class="shaco_box">
                  <div class="shacoe_box_five">
                    <div>
                      <div>
                        <span class="font_one">{{$t('home.homeReport.ssyye')}}</span>
                      </div>
                      <div class="center_box">
                        <div>
                          <span class="font_two">{{ volumeData.currentVolume }}</span>
                        </div>
                        <div>
                          <span>
                            <!-- <i class="el-icon-caret-bottom"></i> -->
                            <i v-if="volumeData.flag == 'up'" class="el-icon-caret-top"></i>
                            <i v-if="volumeData.flag == 'down'" class="el-icon-caret-bottom"></i>
                            {{ volumeData.rate }}
                          </span>
                        </div>
                      </div>
                    </div>
                    <div class="box_img_two">
                      <img
                        src="../../assets/imgs/home_two.png"
                        class="box_img_two"
                      />
                    </div>
                  </div>
                  <div class="shacoe_box_two" style="width: 47%;">
                    <div class="shaco_left">
                      <div class="shacoe_box_three">
                        <div class="shaco_bottom">
                          <span class="font_three">{{$t('home.homeReport.zryye')}}</span>
                          <span class="font_four">{{ volumeData.yesterdayVolume }}</span>
                        </div>
                      </div>
                      <div class="shacoe_box_three">
                        <div class="shaco_bottom">
                          <span class="font_three">{{$t('home.homeReport.byyye')}}</span>
                          <span class="font_four">{{ volumeData.monthtVolume }}</span>
                        </div>
                      </div>
                      <div class="shacoe_box_four">
                        <div class="shaco_bottom">
                          <span class="font_three">{{$t('home.homeReport.ljyye')}}</span>
                          <span class="font_four">{{ volumeData.allVolume }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="shaco grid-content">
                <div style="padding: 24px; height: 100%;display: flex;flex-direction: column;justify-content: space-between;">
                  <div class="shacoe_box_one">
                    <div>
                    <span class="font_one">{{$t('home.homeReport.ktdpzh')}}</span>
                  </div>
                  <div style="height:40px;">
                    <span class="font_two">{{ mchData.total }}</span>
                  </div>
                  </div>
                  <div>
                    <div style="display:flex;margin-bottom: 10px;">
                    <div>
                      <span style="padding-right: 8px;">{{$t('home.homeReport.ztb')}}</span>
                      <span style="padding-right: 16px;">
                        {{ mchData.tbrate }}
                        <i v-if="mchData.tbflag == 'up'" class="el-icon-caret-top"></i>
                        <i v-if="mchData.tbflag == 'down'" class="el-icon-caret-bottom"></i>
                      </span>
                    </div>
                    <div>
                      <span style="padding-right: 8px;">{{$t('home.homeReport.zhb')}}</span>
                      <span>
                        {{ mchData.hbrate }}
                        <i v-if="mchData.hbflag == 'up'" class="el-icon-caret-top"></i>
                        <i v-if="mchData.hbflag == 'down'" class="el-icon-caret-bottom"></i>
                      </span>
                    </div>
                    </div>
                    <div>
                        <img src="../../assets/imgs/home_three.png" style="width: 100%;"/>
                    </div>
                  </div>
                </div>
              </div>
              <div class="shaco grid-content">
                <div style="padding: 24px; height: 100%;display: flex;flex-direction: column;justify-content: space-between;">
                  <div class="shacoe_box_one">
                    <div>
                      <span class="font_one">{{$t('home.homeReport.rzgyszh')}}</span>
                    </div>
                    <div style="height:40px;">
                      <span class="font_two">{{ supplierCountData.total }}</span>
                    </div>
                  </div>
                  <div>
                    <div style="display:flex;margin-bottom:10px">
                      <div>
                        <span style="padding-right: 8px;">{{$t('home.homeReport.ztb')}}</span>
                        <span style="padding-right: 16px;">
                          {{ supplierCountData.tbrate }}
                          <i v-if="supplierCountData.tbflag == 'up'" class="el-icon-caret-top"></i>
                          <i v-if="supplierCountData.tbflag == 'down'" class="el-icon-caret-bottom"></i>
                        </span>
                      </div>
                      <div>
                        <span style="padding-right: 8px;">{{$t('home.homeReport.zhb')}}</span>
                        <span>
                          {{ supplierCountData.hbrate }}
                        <i v-if="supplierCountData.hbflag == 'up'" class="el-icon-caret-top"></i>
                        <i v-if="supplierCountData.hbflag == 'down'" class="el-icon-caret-bottom"></i>
                        </span>
                      </div>
                    </div>
                    <div>
                        <img src="../../assets/imgs/home_four.png" style="width: 100%;"/>
                    </div>
                  </div>
                </div>
              </div>
              </div>
            </div>
            <div class="shacoe_box_eleven">
              <div class="shacoe_box_six">
                  <div class="shacoe_box_seven">
                    <div>
                      <span class="font_five">{{$t('home.homeReport.syzh')}}</span>
                    </div>
                    <div class="shaco_flex">
                      <el-button :class="kep == 1?'shaco_bt_dep':'shaco_bt_kep'" @click="changeData(1)">{{$t('home.homeReport.az')}}</el-button>
                      <el-button :class="kep == 2?'shaco_bt_dep':'shaco_bt_kep'" @click="changeData(2)">{{$t('home.homeReport.ay')}}</el-button>
                      <el-button :class="kep == 3?'shaco_bt_dep':'shaco_bt_kep'" @click="changeData(3)">{{$t('home.homeReport.an')}}</el-button>
                    </div>
                  </div>
                  <div style="width: 100%;height:80%;" id="incomeSummary"></div>
              </div>
              <div class="shacoe_box_eight">
                  <div class="shacoe_box_seven">
                    <div>
                      <span class="font_five">{{$t('home.homeReport.spxsq')}}</span>
                    </div>
                </div>
                <div style="width: 100%;height:80%;" id="Market"></div>
              </div>
            </div>
            <div class="shacoe_box_twelve">
              <div class="shacoe_box_nine">
                  <div class="shacoe_box_seven">
                    <div>
                      <span class="font_five">{{$t('home.homeReport.dpcjje')}}</span>
                    </div>
                    <div>
                      <el-select v-model="twoday" placeholder="请选择" class="shaco_select" @change="timeChange_one($event)">
                        <el-option
                          v-for="item in dayList_two"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value">
                        </el-option>
                      </el-select>
                    </div>
                  </div>
                  <div class="shaco_box_fifteen">
                      <div class="shaco_flex">
                        <div class="shacoe_box_fourteen">
                          <div v-for="(item,index) in chargeDataList.slice(0,5)" :key="index">
                              <div :class="index == 4?'shacoe_box_thirteen_two':'shacoe_box_thirteen'">
                                <div>
                                  <div class="shaco_flex">
                                    <div>
                                      <img src="../../assets/imgs/grade_one.png" class="grade_box_img" v-if="index == 0"/>
                                      <img src="../../assets/imgs/grade_two.png" class="grade_box_img" v-if="index == 1"/>
                                      <img src="../../assets/imgs/grade_three.png" class="grade_box_img" v-if="index == 2"/>
                                      <img src="../../assets/imgs/grade_four.png" class="grade_box_img" v-if="index == 3"/>
                                      <img src="../../assets/imgs/grade_five.png" class="grade_box_img" v-if="index == 4"/>
                                    </div>
                                    <div>
                                      <span>{{ item.name }}</span>
                                    </div>
                                  </div>
                                </div>
                                <div style="margin-right: 51px;">
                                  <span>{{ item.amount.toLocaleString() }}</span>
                                </div>
                              </div>
                          </div>
                        </div>
                        <div style="width:347px">
                          <div v-for="(item,index) in chargeDataList.slice(-5)" :key="index">
                            <div :class="index == 4?'shacoe_box_thirteen_two':'shacoe_box_thirteen'">
                                <div>
                                  <div style="display: flex;">
                                    <div>
                                      <img src="../../assets/imgs/grade_six.png" class="grade_box_img" v-if="index == 0"/>
                                      <img src="../../assets/imgs/grade_seven.png" class="grade_box_img" v-if="index == 1"/>
                                      <img src="../../assets/imgs/grade_eight.png" class="grade_box_img" v-if="index == 2"/>
                                      <img src="../../assets/imgs/grade_nine.png" class="grade_box_img" v-if="index == 3"/>
                                      <img src="../../assets/imgs/grade_ten.png" class="grade_box_img" v-if="index == 4"/>
                                    </div>
                                    <div>
                                      <span>{{ item.name }}</span>
                                    </div>
                                  </div>
                                </div>
                                <div>
                                  <span>{{ item.amount.toLocaleString() }}</span>
                                </div>
                              </div>
                          </div>
                        </div>
                      </div>
                      <div style="width: 100%;height:59%;margin-top: 24px;" id="Store">
                      </div>
                  </div>
              </div>
              <div class="shacoe_box_ten">
                <div class="shacoe_box_seven">
                    <div>
                      <span class="font_five">{{$t('home.homeReport.gysgh')}}</span>
                    </div>
                    <div>
                      <el-select v-model="oneday" placeholder="请选择" class="shaco_select" @change="timeChange_two($event)">
                        <el-option
                          v-for="item in dayList_one"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value">
                        </el-option>
                      </el-select>
                    </div>
                  </div>
                  <div class="shaco_box_fifteen">
                      <div class="shaco_flex">
                        <div class="shacoe_box_fourteen">
                          <div v-for="(item,index) in supplierArr.slice(0,5)" :key="index">
                              <div :class="index == 4?'shacoe_box_thirteen_two':'shacoe_box_thirteen'">
                                <div>
                                  <div class="shaco_flex">
                                    <div>
                                      <img src="../../assets/imgs/grade_one.png" class="grade_box_img" v-if="index == 0"/>
                                      <img src="../../assets/imgs/grade_two.png" class="grade_box_img" v-if="index == 1"/>
                                      <img src="../../assets/imgs/grade_three.png" class="grade_box_img" v-if="index == 2"/>
                                      <img src="../../assets/imgs/grade_four.png" class="grade_box_img" v-if="index == 3"/>
                                      <img src="../../assets/imgs/grade_five.png" class="grade_box_img" v-if="index == 4"/>
                                    </div>
                                    <div>
                                      <span>{{ item.supplier_name }}</span>
                                    </div>
                                  </div>
                                </div>
                                <div style="margin-right: 51px;">
                                  <span>{{ item.num.toLocaleString() }}</span>
                                </div>
                              </div>
                          </div>
                        </div>
                        <div style="width:347px">
                          <div v-for="(item,index) in supplierArr.slice(-5)" :key="index">
                            <div :class="index == 4?'shacoe_box_thirteen_two':'shacoe_box_thirteen'">
                                <div>
                                  <div style="display: flex;">
                                    <div>
                                      <img src="../../assets/imgs/grade_six.png" class="grade_box_img" v-if="index == 0"/>
                                      <img src="../../assets/imgs/grade_seven.png" class="grade_box_img" v-if="index == 1"/>
                                      <img src="../../assets/imgs/grade_eight.png" class="grade_box_img" v-if="index == 2"/>
                                      <img src="../../assets/imgs/grade_nine.png" class="grade_box_img" v-if="index == 3"/>
                                      <img src="../../assets/imgs/grade_ten.png" class="grade_box_img" v-if="index == 4"/>
                                    </div>
                                    <div>
                                      <span>{{ item.supplier_name }}</span>
                                    </div>
                                  </div>
                                </div>
                                <div>
                                  <span>{{ item.num.toLocaleString() }}</span>
                                </div>
                              </div>
                          </div>
                        </div>
                      </div>
                      <div style="width: 100%;height:59%;margin-top: 24px;" id="Supplier">
                      </div>
                  </div>
              </div>
            </div>
            <!-- <div class="order-nav">
              <div class="grid-content bg-purple" @click="toOrderDetail('0')">
                <div class="metric">
                  <div class="leftIcon" style="background: #68c8c7;"><img
                    src="../../assets/imgs/fukuan.png"/>
                  </div>
                  <div class="rightNum">
                    <div>
                      <span class="title">待付款</span>
                      <span class="number" style="color:#68c8c7 ;">{{dataMain.notPay}}</span>
                    </div>
                  </div>
                  <div class="clearfix"></div>
                </div>
              </div>
              <div class="grid-content bg-purple" @click="toOrderDetail('1')">
                <div class="metric">
                  <div class="leftIcon" style="background: #ff6c60;"><img
                    src="../../assets/imgs/fahuo.png"/>
                  </div>
                  <div class="rightNum">
                    <div>
                      <span class="title">待发货</span>
                      <span class="number" style="color:#ff6c60 ;">{{dataMain.noSendOrderNum}}</span>
                    </div>
                  </div>
                  <div class="clearfix"></div>
                </div>
              </div>
              <div class="grid-content bg-purple" @click="toOrderDetail('2')">
                <div class="metric">
                  <div class="leftIcon" style="background: #feb04c;"><img
                    src="../../assets/imgs/shouhuo.png"/>
                  </div>
                  <div class="rightNum">
                    <div>
                      <span class="title">待收货</span>
                      <span class="number" style="color:#feb04c ;">{{dataMain.notDeliverNum}}</span>
                    </div>
                  </div>
                  <div class="clearfix"></div>
                </div>
              </div>
              <div class="grid-content bg-purple" @click="toOrderDetail('5')">
                <div class="metric">
                  <div class="leftIcon" style="background: #a6d867;"><img
                    src="../../assets/imgs/pingjia.png"/>
                  </div>
                  <div class="rightNum">
                    <div>
                      <span class="title">待评价</span>
                      <span class="number" style="color:#a6d867 ;">{{dataMain.orderNotCommentsNum}}</span>
                    </div>
                  </div>
                  <div class="clearfix"></div>
                </div>
              </div>
              <div class="metric grid-content" @click="toOrderDetail('退货')">
                <div class="leftIcon" style="background: #57c8f2;"><img
                  src="../../assets/imgs/tuihuo.png"/>
                </div>
                <div class="rightNum">
                  <div>
                    <span class="title">退货</span>
                    <span class="number" style="color:#57c8f2 ;">{{dataMain.orderReturnWaitNum}}</span>
                  </div>
                </div>
                <div class="clearfix"></div>
              </div>
            </div> -->

            <!-- <el-row :gutter="5">
              <el-col :span="9">
                <div class="mainLeft">
                  <div class="realDiv">
                    <div class="realDivTitle">实时营业额</div>
                    <div class="realDivMainNum">
                      <span class="number">{{dataMain.toDaySaleTotal }}</span>
                      <span class="percentage">
                      <i v-if="isUp1" class="el-icon-caret-bottom"></i>
                      <i v-else class="el-icon-caret-top"></i>
                      <span class="downText">
                          {{dataMain.dayAmtProportion}}%
                      </span>
                    </span>
                    </div>
                    <div class="realDiv3Num">
                      <div class="realNum3">
                        <div class="numText_copy" style="position: relative;">
                          <p class="numText">昨日营业额
                          <el-tooltip class="item" effect="light" content="昨日的有效订单金额" placement="right-start">
                            <span class="my_mark" id="mark_img_1"></span>
                          </el-tooltip>
                          </p>
                          <div class="mark_div" id="mark_img_11"><span></span>昨日的有效订单金额</div>
                          <p class="num">{{dataMain.yesterdaySale}}</p>
                        </div>
                      </div>
                      <div class="realNum3">
                        <div class="numText_copy" style="position: relative;">
                          <p class="numText">本月营业额
                            <el-tooltip class="item" effect="light" content="本月的有效订单总金额" placement="right-start">
                              <span class="my_mark" id="mark_img_2"></span>
                            </el-tooltip>
                          </p>
                          <div class="mark_div" id="mark_img_22"><span></span>本月的有效订单总金额</div>
                          <p class="num">{{dataMain.monthAmtTotal}}</p>
                        </div>
                      </div>
                      <div class="realNum3">
                        <div class="numText_copy" style="position: relative;">
                          <p class="numText">累计营业额
                          <el-tooltip class="item" effect="light" content="自开店以来的累积营业额" placement="right-start">
                            <span class="my_mark" id="mark_img_3"></span>
                          </el-tooltip>
                          </p>
                          <div class="mark_div" id="mark_img_33"><span></span>自开店以来的累积营业额</div>
                          <p class="num">{{z_total}}</p>
                        </div>
                      </div>
                      <div class="clearfix"></div>
                    </div>
                  </div>
                  <div class="realDiv">
                    <div class="realDivTitle">实时订单数</div>
                    <div class="realDivMainNum">
                      <span class="number">{{dataMain.today_order_num}}</span>
                      <span class="percentage">
                      <i v-if="isUp2" class="el-icon-caret-bottom"></i>
                      <i v-else class="el-icon-caret-top"></i>
                      <span class="downText">
                          {{dataMain.yoy_order}}%
                      </span>
                    </span>
                    </div>
                    <div class="realDiv3Num">
                      <div class="realNum3">
                        <div>
                          <p class="numText">昨日订单</p>
                          <p class="num">{{dataMain.yesterdayOrderNum}}</p>
                        </div>
                      </div>
                      <div class="realNum3">
                        <div>
                          <p class="numText">本月订单</p>
                          <p class="num">{{dataMain.monthOrderNum}}</p>
                        </div>
                      </div>

                      <div class="realNum3" style="border:none">
                        <div>
                          <p class="numText">累计订单</p>
                          <p class="num">{{z_num}}</p>
                        </div>
                      </div>
                      <div class="clearfix"></div>
                    </div>
                  </div>
                  <div class="realDiv">
                    <div class="realDivTitle">实时访客数</div>
                    <div class="realDivMainNum">
                      <span class="number">{{dataMain.todayBrowseTotal}}</span>
                      <span class="percentage">
                      <i v-if="isUp3" class="el-icon-caret-bottom"></i>
                      <i v-else class="el-icon-caret-top"></i>
                      <span class="downText">
                          {{dataMain.daySale}}%
                      </span>
                    </span>
                    </div>
                    <div class="realDiv3Num">
                      <div class="realNum3">
                        <div>
                          <p class="numText">昨日访客</p>
                          <p class="num">{{dataMain.yesterdayBrowseTotal}}</p>
                        </div>
                      </div>
                      <div class="realNum3">
                        <div>
                          <p class="numText">本月访客</p>
                          <p class="num">{{dataMain.monthBrowseTotal}}</p>
                        </div>
                      </div>
                      <div class="realNum3" style="border:none">
                        <div>
                          <p class="numText">累计访客</p>
                          <p class="num">{{dataMain.coustomer_num_z}}</p>
                        </div>
                      </div>
                      <div class="clearfix"></div>
                    </div>
                  </div>
                </div>
              </el-col>
              <el-col :span="15">
                <div class="mainRight">
                  <div class="tjtDiv" style="height: 315px;background: transparent;">
                    <div class="member">
                      <div class="realDivTitle">用户统计</div>
                      <div class="title1" style="float: right; margin-right: 20px">累计用户: {{dataMain.userTotal}}人
                      </div>
                      <div class="tjtDivIn" id="memberTJT" style="height:240px;margin: 0 auto;">

                      </div>
                    </div>
                    <div class="news">
                      <div class="realDivTitle" style="border-bottom: 2px solid #eee;">公告</div>
                      <ul style="height: 240px;">
                        <li v-for="(item,index) in dataMain.sysNotice" :key="index" @click="toNoticeDetail(item.id)">
                          <div>
                            <span>{{item.title}}</span>
                            <div class="newsDate">{{item.add_time | dateFormat}}</div>
                          </div>
                        </li>
                      </ul>
                    </div>
                  </div>
                  <div class="tjtDiv" style="height: 415px;">
                    <div class="realDivTitle">订单统计</div>
                    <div class="title1" style="float: right; margin-right: 20px">
                      <a href="javascript:;" @click="loadOrderReport(2)" :class='["type1",this.orderType===2 ? " activeTJTBtn" : ""]'>最近七天</a>
                      <a href="javascript:;" @click="loadOrderReport(3)" :class='["type2",this.orderType===3 ? " activeTJTBtn" : ""]'>本月</a>
                      <a href="javascript:;" @click="loadOrderReport(4)" :class='["type3",this.orderType===4 ? " activeTJTBtn" : ""]'>本年</a>
                    </div>
                    <div class="tjtDivIn" id="ddTJT" style="height:345px;"></div>
                  </div>
                </div>
              </el-col>
            </el-row> -->
          </div>
        </div>
      </div>
    </div>


</template>

<script>
import main from '@/webManage/js/home/homeReport.js'

export default main
</script>

<style scoped lang="less">
@import '../../webManage/css/home/homeReport.less';
</style>
