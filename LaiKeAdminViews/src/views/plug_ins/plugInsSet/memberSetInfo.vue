<template>
  <div class="container">
    <el-form ref="ruleForm" class="form-search" :rules="rules" :model="ruleForm" label-width="130px">
      <div class="basic-info">
        <div class="header">
          <span>{{$t('member.memberSet.jbsz')}}</span>
        </div>
        <div class="basic-block">
          <!-- <el-form-item :label="$t('member.memberSet.hycjkg')">
            <el-switch v-model="ruleForm.isOpen" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
					</el-form-item> -->
          <el-form-item :label="$t('plugInsSet.groupSetInfo.yhdrksz')">
            <el-switch
              v-model="ruleForm.isOpen"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
            <div class="tip">
              <p>{{ $t('member.memberSet.memberSetInfo.one') }}</p>
              <p>{{ $t('member.memberSet.memberSetInfo.two') }}</p>
              <p>{{ $t('member.memberSet.memberSetInfo.three') }}</p>
              <p>{{ $t('member.memberSet.memberSetInfo.four') }}</p>
              <p>{{ $t('member.memberSet.memberSetInfo.five') }}</p>
              <p>{{ $t('member.memberSet.memberSetInfo.six') }}</p>
              <p>{{ $t('member.memberSet.memberSetInfo.seven') }}</p>
              <p>{{ $t('member.memberSet.memberSetInfo.eight') }}</p>
              <p>{{ $t('member.memberSet.memberSetInfo.nine') }}</p>
            </div>
          </el-form-item>
          <el-form-item class="trailen" :label="$t('member.memberSet.ktsz')" required>
            <div class="trailen-time" v-for="(item,index) in ruleForm.open_config" :key="index">
              <span style="margin-right: 60px;">{{ item.openMethod }}</span>
              <el-input style="width: 180px;" :placeholder="$t('member.memberSet.qsrktfsmc')" v-model="item.openMethodName">
              </el-input>
              <!-- <span>|</span>
              <el-select class="select-input sequence-input" v-model="item.openMethod" :placeholder="$t('member.memberSet.qxzktfs')">
                <el-option v-for="item in sequenceList" :key="item.value" :label="item.label" :value="item.label">
                </el-option>
              </el-select> -->
              <span class="line"></span>
              <!-- <view class="line"></view> -->
              <el-input style="width: 180px;" :placeholder="$t('member.memberSet.qsrjg')" @blur="item.price = handleBlue(item.price)" v-model="item.price" @keyup.native="item.price = oninput4(item.price,2)">
                <template slot="append">元</template>
              </el-input>
              <span class="line"></span>
              <el-input style="width: 180px;" :placeholder="$t('member.memberSet.qsrktzsjf')" v-model="item.points" @blur="item.points = oninput3(item.points)">
              </el-input>
            </div>
					</el-form-item>
          <el-form-item class="trailen" :label="$t('member.memberSet.hysr')">
            <div class="isOpen-trailen">
              <el-switch v-model="ruleForm.birthdayOpen" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
              </el-switch>
            </div>
            <div class="trailen-time">
              <span>{{$t('member.memberSet.hysrdt')}}</span>
              <el-input placeholder="" v-model="ruleForm.pointsMultiple" @keyup.native="ruleForm.pointsMultiple = oninput(ruleForm.pointsMultiple,2)">
              </el-input>
              <span style="margin: 10px;">{{$t('member.memberSet.bjf')}}</span>
              <span  class=" my_gray">{{$t('member.memberSet.ctqb')}}</span>
            </div>
					</el-form-item>
          <!-- <el-form-item class="trailen" :label="$t('member.memberSet.hyzs')">
            <div class="isOpen-trailen">
              <el-switch v-model="ruleForm.bonusPointsOpen" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
              </el-switch>
            </div>
            <div class="trailen-time" v-for="(item,index) in ruleForm.bonusPointsConfig" :key="index">
              <el-select class="select-input sequence-input" v-model="item.openMethod" :placeholder="$t('member.memberSet.qxzkt')">
                <el-option v-for="item in sequenceList" :key="item.value" :label="item.label" :value="item.label">
                </el-option>
              </el-select>
              <span>|</span>
              <el-input :placeholder="$t('member.memberSet.qsrjg')" v-model="item.points" @keyup.native="item.points = oninput2(item.points)">
              </el-input>
            </div>
					</el-form-item> -->
          <el-form-item class="activity-push" :label="$t('member.memberSet.hyzk')" prop="memberDiscount">
            <el-input v-model="ruleForm.memberDiscount" @keyup.native="ruleForm.memberDiscount = oninput(ruleForm.memberDiscount,2)">
              <template slot="append">{{$t('member.memberSet.zhe')}}</template>
            </el-input>
            <span style="margin-left: 0;" class="my_gray">{{$t('member.memberSet.qsr1')}}</span>
          </el-form-item>
          <el-form-item class="trailen" :label="$t('member.memberSet.xftxs')">
            <div class="isOpen-trailen">
              <el-switch v-model="ruleForm.renewOpen" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
              </el-switch>
            </div>
            <div class="trailen-time">
              <span>{{$t('member.memberSet.dqq')}}</span>
              <el-input placeholder="" v-model="ruleForm.renewDay" @keyup.native="ruleForm.renewDay = oninput3(ruleForm.renewDay)">
              </el-input>
              <span style="margin: 10px;">{{$t('member.memberSet.tmcd')}}</span>
            </div>
					</el-form-item>
        </div>
      </div>
      <div class="order-info">
        <div class="header">
          <span>{{$t('member.memberSet.hyqy')}}</span>
        </div>
        <div class="order-block">
          <div class="equity-content">
            <div class="equity-item" v-for="(item,index) in ruleForm.memberEquity" :key="index">
              <i class="el-icon-close" @click="delEquity(index)"></i>
              <div class="equity-item-left">
                <p>{{ item.equityName }}</p>
                <p>{{ item.englishName }}</p>
              </div>
              <div class="equity-item-right">
                <img :src="item.icon" alt="">
              </div>
            </div>
          </div>
          <div class="add-equity">
            <el-form-item class="activity-push" :label="$t('member.memberSet.hyqymc')">
              <el-input v-model="equity.equityName" :placeholder="$t('member.memberSet.qsrqymc')"></el-input>
            </el-form-item>
            <el-form-item class="activity-push" :label="$t('member.memberSet.hyqyyw')">
              <el-input v-model="equity.englishName" :placeholder="$t('member.memberSet.qsrqyyw')"></el-input>
            </el-form-item>
            <el-form-item class="activity-push" :label="$t('member.memberSet.qytb')">
              <l-upload
                :limit="1"
                v-model="equity.icon"
                :text="$t('member.memberSet.qsc48')"
                ref="upload"
              >
            </l-upload>
            </el-form-item>
            <el-form-item>
              <el-button class="bgColor" type="primary" @click="add">{{$t('member.memberSet.tj')}}</el-button>
              <el-button class="bdColor" @click="reset" plain>{{$t('member.memberSet.cz')}}</el-button>
            </el-form-item>
          </div>
        </div>
      </div>
      <div style="min-height: 75px"></div>
      <div class="footer-button">
        <el-button plain class="footer-cancel fontColor" @click="back">{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
        <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import memberSetInfo from '@/webManage/js/plug_ins/plugInsSet/memberSetInfo'
export default memberSetInfo
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/plugInsSet/memberSetInfo.less';
</style>