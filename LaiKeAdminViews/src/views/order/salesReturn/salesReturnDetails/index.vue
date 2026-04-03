<template>
  <div class="container">
    <el-form ref="ruleForm" class="form-search" label-width="100px">
      <div class="goods-info">
        <div class="header">
          <span>{{$t('salesReturnDetails.spxx')}} </span>
        </div>
        <div class="goods-block">
            <el-table :data="goodsDate" style="max-height: 350px;" :header-cell-style="header">
              <el-table-column prop="pid" align="center" :label="$t('salesReturnDetails.spbh')">
              </el-table-column>
              <el-table-column prop="" align="left" :label="$t('salesReturnDetails.spmc')">
                <template slot-scope="scope">
                  <img :src="scope.row.imgurl" alt="">
                  <span>{{ scope.row.p_name }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="size" align="center" :label="$t('salesReturnDetails.spgg')">
              </el-table-column>
              <el-table-column prop="p_price" align="center" :label="$t('salesReturnDetails.dj')">
                <template slot-scope="scope">
                  <span>{{order_currency_symbol}} {{ LaiKeCommon.formatPrice(scope.row.p_price,order_exchange_rate) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="num" align="center" :label="$t('salesReturnDetails.sl')">
              </el-table-column>
              <!-- 核销的不需要展示运费 要加判断值 -->
              <el-table-column v-if="orderType == 0" prop="freight" align="center" :label="$t('salesReturnDetails.yf')">
                <template slot-scope="scope">
                  <span>{{order_currency_symbol}}{{ LaiKeCommon.formatPrice(scope.row.freight,order_exchange_rate) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="discountAmount" align="center" :label="$t('salesReturnDetails.yhje')">
                <template slot-scope="scope">
                  <span>{{order_currency_symbol}}{{ LaiKeCommon.formatPrice(scope.row.discountAmount,order_exchange_rate) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="z_price" align="center" :label="$t('salesReturnDetails.xj')">
                <template slot-scope="scope">
                  <span>{{order_currency_symbol}}{{ LaiKeCommon.formatPrice(scope.row.z_price,order_exchange_rate) }}</span>
                </template>
              </el-table-column>
            </el-table>
            <div class="totle-price">
              <div class="totle-price-item">
                <span>{{$t('salesReturnDetails.hj')}}：</span>
                <span style="color: #ff453d;">{{order_currency_symbol}}{{ LaiKeCommon.formatPrice(totle_price,order_exchange_rate) }}</span>
              </div>
            </div>
        </div>
      </div> 

      <div class="apply-info" v-if="applyInfo">
        <div class="header">
          <span>{{$t('salesReturnDetails.sqxx')}}</span>
        </div>
        <div class="apply-block">
          <el-form ref="ruleForm" class="form-search" label-width="120px">
            <el-form-item :label="$t('salesReturnDetails.shlx')">
              {{ applyInfo.re_type == 1 ? $t('salesReturnDetails.thtk') : applyInfo.re_type == 2 ? $t('salesReturnDetails.jtk') : $t('salesReturnDetails.hh') }}
            </el-form-item>
            <el-form-item :label="$t('salesReturnDetails.sqyy')">
              <span v-if='applyInfo.r_type==15'>极速退款</span>
              <span v-else>{{ applyInfo.content }}</span>

            </el-form-item>
            <el-form-item class="right" :label="$t('salesReturnDetails.ddbh')">
              {{ applyInfo.sNo }}
            </el-form-item>
            <el-form-item :label="$t('salesReturnDetails.sqje')">
              <span v-if="applyInfo.re_apply_money">{{order_currency_symbol}}{{ LaiKeCommon.formatPrice(applyInfo.re_apply_money,order_exchange_rate) }}</span>
            </el-form-item>
            <!-- 只有线下核销的才有 -->
            <el-form-item v-if="orderType == 3" :label="$t('salesReturnDetails.tkhxcs')">
              {{ applyInfo.r_write_off_num }}
            </el-form-item>
            <el-form-item class="right" :label="$t('salesReturnDetails.sqsj')">
              {{ applyInfo.re_time | dateFormat }}
            </el-form-item>
            <el-form-item :label="$t('salesReturnDetails.shpz')">
              <ul class="sales-img" v-if="sales_imgs">
                <!-- <li v-for="(item,index) in sales_imgs" :key="index">
                  <img :src="item" alt="" >
                </li> -->
                <viewer :images="sales_imgs">
                  <img v-for="src in sales_imgs" :src="src" :key="src">
                </viewer>
              </ul>
              <img v-else-if="applyInfo.imgurl" :src="applyInfo.imgurl[0]" alt="">
            </el-form-item>

            <el-form-item class="right" :label="$t('orderLists.xdrlxdh')">
              {{ applyInfo.user_mobile }}
            </el-form-item>
 
          </el-form>
        </div>
      </div>
 
      <div class="audit-info" style="margin-bottom: 10px;" >
        <div class="header">
          <span  >{{$t('orderDetails.shrxx')}}</span> 
        </div>
        <div class="audit-block" style="padding-bottom: 10px;"> 
          <div >
            <div class="item" >
              <span class="gray">{{$t('orderLists.shr')}}：</span>
              <span class=""> {{ applyInfo.name}}</span>
            </div>
            <div class="item" >
              <span class="gray">{{$t('orderLists.lxdh')}}：</span>
              <span class="">{{ applyInfo.mobile}} </span>
            </div>
            <div class="item">
              <span class="gray">{{$t('orderLists.shdz')}}：</span>
              <span class="">{{ applyInfo.address}} </span>
            </div>
           
          </div>
           
        </div>
      </div>
 
      <div class="audit-info" v-if="applyInfo">
        <div class="header">
          <span v-if="applyInfo.r_type==15">处理记录</span>
          <span v-else>{{$t('salesReturnDetails.shjl')}}</span>
        </div>
        <div class="audit-block"> 
          <div v-if="applyInfo.r_type==15">
            <div class="item" >
              <span class="gray">{{$t('salesReturnDetails.sqjg')}}</span>
              <span class="">{{ examineInfo[0].examineResult }}</span>
            </div>
            <div class="item" >
              <span class="gray">{{$t('salesReturnDetails.tkje')}}</span>
              <span class="">{{order_currency_symbol}}{{ LaiKeCommon.formatPrice(examineInfo[0].returnMoney,order_exchange_rate) }}</span>
            </div>
            <div class="item">
              <span class="gray">{{$t('salesReturnDetails.sqsj')}}</span>
              <span class="">{{ examineInfo[0].examineResultTime }}</span>
            </div>
          </div>
          <template v-else>
            <div v-for="(item,index) of examineInfo" :key="index">
              <div class="item" v-if="item.examineResult">
                <span class="gray">{{$t('salesReturnDetails.sqjg')}}</span>
                <span class="">{{ item.examineResult }}</span>
              </div>
              <div class="item" v-if="item.applyTime">
                <span class="gray">{{$t('salesReturnDetails.sqsj')}}</span>
                <span class="">{{ item.applyTime }}</span>
              </div> 
              <div class="item" v-if="item.examineResultTime">
                <span class="gray">{{$t('salesReturnDetails.shsj')}}</span>
                <span class="">{{ item.examineResultTime }}</span>
              </div>
              <div class="item" v-if="item.reBackNo">
                <span class="gray">{{$t('salesReturnDetails.hjwl')}}</span>
                <span class="">{{ item.reBackNo}}</span>
              </div>
              <div class="item" v-if="item.reBackTime">
                <span class="gray">{{$t('salesReturnDetails.hjsj')}}</span>
                <span class="">{{ item.reBackTime}}</span>
              </div> 
              <div class="item" v-if="item.returnBackNo">
                <span class="gray">{{$t('salesReturnDetails.thwl')}}</span>
                <span class="">{{ item.returnBackNo }}</span>
              </div>
              <div class="item" v-if="item.refuseText">
                <span class="gray">{{$t('salesReturnDetails.jjly')}}</span>
                <span class="">{{ item.refuseText }}</span>
              </div> 
              <div class="item" v-if="item.returnMoney">
                <span class="gray">{{$t('salesReturnDetails.tkje')}}</span>
                <span class="">{{ item.returnMoney }}</span>
              </div> 
            </div>
          </template> 
        </div>
      </div>


      <div class="hr" ></div>

    </el-form>
    
    <div class="footer-button" v-if="mydata.type">
      <el-button v-if="mydata.type!=4" plain class="footer-cancel fontColor" @click="dialogShow2(mydata.value,2)">{{$t('salesReturnDetails.jj')}}</el-button>

      <div v-if="mydata.value.r_type==0">
        <!-- <el-button v-if="mydata.type!=4" plain class="footer-cancel fontColor" @click="dialogShow2(mydata.value,2)">拒绝</el-button> -->
        <el-button v-if="mydata.value.re_type==1||mydata.value.re_type==3" type="primary" class="footer-save bgColor mgleft" @click="submitForm1(mydata.value,1)">{{$t('salesReturnDetails.tg')}}</el-button>
        <el-button v-if="mydata.value.re_type==2"  type="primary" class="footer-save bgColor mgleft" @click="dialogShow2(mydata.value,1)">{{$t('salesReturnDetails.tk') }} </el-button>
      </div>
      <div v-if="mydata.value.r_type==3">
        <el-button v-if="mydata.value.re_type==1"  type="primary" class="footer-save bgColor mgleft" @click="dialogShow2(mydata.value,1)">{{$t('salesReturnDetails.tk') }} </el-button>
        <el-button v-else type="primary" class="footer-save bgColor mgleft" @click="dialogShow2(mydata.value,3)">{{$t('salesReturnDetails.tg')}}</el-button>
      </div>
      <div v-if="mydata.value.r_type==5">
        <!-- <el-button plain class="footer-cancel fontColor" @click="dialogShow2(mydata.value,2)">拒绝</el-button> -->
        <el-button type="primary" class="footer-save bgColor mgleft" @click="dialogShow2(mydata.value,1)">{{$t('salesReturnDetails.tk')}} </el-button>
      </div>


    </div>
    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="toggle == 3 ? toggleTitle : toggle == 1 ? $t('salesReturnDetails.qtxje') : $t('salesReturnDetails.txly')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <el-form :model="ruleForm" ref="ruleForm" label-width="80px" class="demo-ruleForm">
          <div class="task-container">
            <div class="through" v-if="toggle === 1 || toggle === 3">
              <!-- <h3 v-if="this.type !== 11&&content"><i style="color:#E6A23C;" class="el-icon-warning"></i>{{ content }}</h3> -->
              <div v-if="toggle === 3">
                <el-form-item class="select-input" :label="$t('salesReturnDetails.kdgs')" prop="kuaidi_name">
                  <el-select class="select-input" v-model="ruleForm.kuaidi_name" :placeholder="$t('salesReturnDetails.qxzkdgs')">
                    <el-option v-for="item in courierList" :key="item.id" :label="item.kuaidi_name" :value="item.id">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item class="select-input" :label="$t('salesReturnDetails.kddh')" prop="kuaidi_no">
                  <el-input v-model="ruleForm.kuaidi_no" @keyup.native="ruleForm.kuaidi_no = oninput3(ruleForm.kuaidi_no)"></el-input>
                </el-form-item>
              </div>
              <div v-if="toggle === 1">
                <el-form-item class="select-input" :label="$t('salesReturnDetails.stje')" required style="margin-bottom: 20px;">
                  <el-input-number min="0" :controls="false" @keyup.native="ruleForm.s_refund = oninput(ruleForm.s_refund,2)" v-model="ruleForm.s_refund"></el-input-number>
                </el-form-item>
                <el-form-item class="select-input" :label="$t('salesReturnDetails.ytje')" prop="y_refund">
                  <el-input type="number" disabled v-model="ruleForm.y_refund"></el-input>
                </el-form-item>
                <p style="margin:6px 0 22px 100px"><i style="color:#FF453D;margin-right: 5px;" class="el-icon-warning-outline"></i>{{$t('salesReturnDetails.wxts')}}</p>
                <!-- <div class="s-refund">
                  <span>实退金额：</span>
                  <el-input type="number" min="0" @keyup.native="ruleForm.s_refund = oninput(ruleForm.s_refund,2)" v-model="ruleForm.s_refund"></el-input>
                </div> -->
              </div>


            </div>
            <el-form-item v-if="toggle === 2" class="integral" :label="$t('salesReturnDetails.txly')" required>
              <el-input type="textarea" v-model="ruleForm.reason" :placeholder="$t('salesReturnDetails.qsrjj')"></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button class="footer-cancel fontColor" @click="handleClose2()">{{$t('salesReturnDetails.ccel')}}</el-button>
              <el-button class="bdColor" type="primary" @click="submitForm2('ruleForm')">{{$t('salesReturnDetails.okk')}}</el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import salesReturnDetails from '@/webManage/js/order/salesReturn/salesReturnDetails'
export default salesReturnDetails
</script>

<style scoped lang="less">
@import '../../../../webManage/css/order/salesReturn/salesReturnDetails.less';
</style>
