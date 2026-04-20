<template>
    <view class="container"
        :style="{backgroundColor: coupon_list.length == 0 && hasLoad?'':tabIndex == 1?'#F5F5F5':''}">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.storeCoupon.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>

        <view class="tabBox">
            <view class="tab">
                <view class="tab_item" :class="{active: tabIndex == 0}"
                    :style="tabIndex == 0?'color: #333333;font-size: 32rpx;':'color: #999999;font-size: 28rpx;'"
                    @tap="changeTab(0)">
                    {{language.storeCoupon.yhqlb}}
                </view>
                <view class="tab_item" :class="{active: tabIndex == 1}"
                    :style="tabIndex == 1?'color: #333333;font-size: 32rpx;':'color: #999999;font-size: 28rpx;'"
                    @tap="changeTab(1)">
                    {{pagetitle}}
                </view>
            </view>
        </view>

        <view class="addCoupon" v-if="tabIndex == 1">
            <view class="addCoupon_box">
                <view class="addCoupon_list">
                    <view class="addCoupon_list_left">
                        <text>*</text>
                        {{language.storeCoupon.type}}
                    </view>
                    <view :class="!editsFlag?'input_box':'input_box1'">
                        <picker :disabled="editsFlag" class="addCoupon_list_ipt" style="height: 22.36px;"
                            @change="bindPickerChange" :value="activity_type<0?0:activity_type" :range="type_list">
                            <view class="uni-input" v-if="activity_type>=0">{{type_list[activity_type]}}</view>
                            <view class="uni-input" v-else style="color: #b8b8b8;">
                                {{language.storeCoupon.typePlaceholder}}
                            </view>
                        </picker>
                        <image class="jiantou" :src="jiantou"></image>
                    </view>
                </view>
                <view class="addCoupon_list">
                    <view class="addCoupon_list_left">
                        <text>*</text>
                        {{language.storeCoupon.name}}
                    </view>
                    <view :class="!ck?'input_box':'input_box1'">
                        <input :disabled="ck" class="addCoupon_list_ipt" v-model="name" type="text"
                            :placeholder="language.storeCoupon.namePlaceholder" placeholder-style="color:#b8b8b8">
                    </view>
                </view>
                <view class="addCoupon_list" v-if="type_list[activity_type]=='折扣券'">
                    <view class="addCoupon_list_left">
                        <text>*</text>
                        {{language.storeCoupon.zkz}}
                    </view>
                    <view :class="!editsFlag?'input_box':'input_box1'">
                        <input :disabled="editsFlag" class="addCoupon_list_ipt" v-model="discount" type="digit"
                            :placeholder="language.storeCoupon.qsrzkq"
                            placeholder-style="color:#b8b8b8">{{language.storeCoupon.discount}}
                    </view>
                </view>

                <view class="addCoupon_list" v-if="type_list[activity_type]!='折扣券'">
                    <view class="addCoupon_list_left">
                        <text>*</text>
                        {{language.storeCoupon.faceValue}}
                    </view>
                    <view :class="!editsFlag?'input_box':'input_box1'">
                        <input :disabled="editsFlag" class="addCoupon_list_ipt" v-model="money" type="digit"
                            :placeholder="language.storeCoupon.faceValuePlaceholder" placeholder-style="color:#b8b8b8">
                        {{language.storeCoupon.yuan}}
                    </view>
                </view>

                <!-- 消费门槛 -->
                <view class="addCoupon_list">
                    <view class="addCoupon_list_left">
                        <text>*</text>
                        {{language.storeCoupon.threshold}}
                    </view>
                    <block v-if="!editsFlag">
                        <view :class="menkan==1?'numbox_box_xz':'numbox_box_wxz'" @click="menkanclick(1)">
                            {{language.storeCoupon.wmk}}
                        </view>
                        <view :class="menkan==2?'numbox_box_xz':'numbox_box_wxz'" @click="menkanclick(2)">
                            {{language.storeCoupon.szje}}
                        </view>
                    </block>
                    <block v-else>
                        <view v-if="menkan==1" :class="menkan==1?'numbox_box_xz':'numbox_box_wxz'">
                            {{language.storeCoupon.wmk}}
                        </view>
                        <view v-if="menkan==2" :class="menkan==2?'numbox_box_xz':'numbox_box_wxz'">
                            {{language.storeCoupon.szje}}
                        </view>
                    </block>

                </view>
                
                <!-- 消费金额 -->
                <view class="addCoupon_list" v-if="menkan==2">
                    <view class="addCoupon_list_left"></view>
                    <view :class="!editsFlag?'input_box':'input_box1'">
                        <input :disabled="editsFlag" class="addCoupon_list_ipt" v-model="z_money" type="number"
                            :placeholder="language.storeCoupon.qsrxfje" placeholder-style="color:#b8b8b8">
                        {{language.storeCoupon.yuan}}
                    </view>
                </view>

                <!-- 优惠券图片 -->
                <!-- <view class="addCoupon_list" style="height: 192rpx;">
                    <view class="addCoupon_list_left">
                        <text>*</text>{{language.storeCoupon.tp}}
                    </view>
                    <div class="rightInput">
                        <div class="upImg1" v-if="cover_map">
                            <img class="wh_100" :src="cover_map" alt="" />
                            <img :src="delImg" @tap="_delImg2()" class="delImg"/>
                        </div>
                        <div class="upImg" @tap="_chooseImg2()" v-else>
                            <img :src="textIcon" class="wh_48" alt="" />
                        </div>
                        <div class="jianyi">(建议上传500px图片，只在封面展示)</div>
                    </div>
                
                </view> -->

            </view>
            <view class="addCoupon_box" style="margin-top: 16rpx;">
                <view class="addCoupon_list">
                    <view class="addCoupon_list_left">
                        {{language.storeCoupon.range}}
                    </view>
                    <view class="addCoupon_list_ipt" v-if="!editsFlag">
                        <radio-group class="radioBox">
                            <view :class="type==1?'kyfw':'kyfw1'" @click="radioChange(1)">
                                {{language.storeCoupon.all}}
                            </view>
                            <view :class="type==2?'kyfw':'kyfw1'" @click="radioChange(2)">
                                {{language.storeCoupon.specifyPro}}
                            </view>
                            <view :class="type==3?'kyfw':'kyfw1'" @click="radioChange(3)">
                                {{language.storeCoupon.specifyClass}}
                            </view>
                        </radio-group>
                    </view>
                    <view class="addCoupon_list_ipt" v-else>
                        <radio-group class="radioBox">
                            <view v-if="type==1" :class="type==1?'kyfw':'kyfw1'">
                                {{language.storeCoupon.all}}
                            </view>
                            <view v-if="type==2" :class="type==2?'kyfw':'kyfw1'">
                                {{language.storeCoupon.specifyPro}}
                            </view>
                            <view v-if="type==3" :class="type==3?'kyfw':'kyfw1'">
                                {{language.storeCoupon.specifyClass}}
                            </view>
                        </radio-group>
                    </view>
                </view>

                <view class="addCoupon_list" v-if="type==2">
                    <view class="addCoupon_list_left">
                    </view>
                    <view :class="!editsFlag?'input_box':'input_box1'" @click="toUrl('/pagesA/myStore/choosePro')">
                        <input disabled :value="pro_listText" class="addCoupon_list_ipt" type="text"
                            :placeholder="language.storeCoupon.chooseGoodsPlaceholder"
                            placeholder-style="color:#b8b8b8">
                        <image class="jiantou" :src="jiantou"></image>
                    </view>
                </view>
                <view class="addCoupon_list" v-if="type==3">
                    <view class="addCoupon_list_left">
                    </view>
                    <view :class="!editsFlag?'input_box':'input_box1'" @click="toUrl('/pagesA/myStore/chooseClass')">
                        <input disabled :value="munu_listText" class="addCoupon_list_ipt" type="text"
                            :placeholder="language.storeCoupon.chooseClassPlaceholder" style="margin-right: 20rpx;"
                            placeholder-style="color:#b8b8b8" />
                        <image class="jiantou" :src="jiantou"></image>
                    </view>
                </view>
                <view class="addCoupon_list" v-if="!editsFlag">
                    <view class="addCoupon_list_left">
                        {{language.storeCoupon.lqfs}}
                    </view>
                    <view :class="is_receive==0?'numbox_box_xz':'numbox_box_wxz'" @click="receiveClick(0)">
                        {{language.storeCoupon.sdlq}}
                    </view>
                    <view :class="is_receive==1?'numbox_box_xz':'numbox_box_wxz'" @click="receiveClick(1)">
                        {{language.storeCoupon.zdff}}
                    </view>

                </view>
                <view class="addCoupon_list" v-else>
                    <view class="addCoupon_list_left">
                        {{language.storeCoupon.lqfs}}
                    </view>
                    <view v-if="is_receive==0" :class="is_receive==0?'numbox_box_xz':'numbox_box_wxz'"
                        @click="receiveClick(0)">{{language.storeCoupon.sdlq}}</view>
                    <view v-if="is_receive==1" :class="is_receive==1?'numbox_box_xz':'numbox_box_wxz'"
                        @click="receiveClick(1)">{{language.storeCoupon.zdff}}</view>

                </view>
                <!-- 发行数量 -->
                <view class="addCoupon_list">
                    <view class="addCoupon_list_left">
                        <text>*</text>
                        {{language.storeCoupon.issueNumber}}
                    </view>
                    <block v-if="!editsFlag">
                        <view :class="MentalRay==1?'numbox_box_xz':'numbox_box_wxz'" @click="MentalRayClick(1)">
                            {{language.storeCoupon.bxz }}
                        </view>
                        <view :class="MentalRay==2?'numbox_box_xz':'numbox_box_wxz'" @click="MentalRayClick(2)">
                            {{language.storeCoupon.szsl}}
                        </view>
                    </block>
                    <block v-else>
                        <view v-if="MentalRay==1" :class="MentalRay==1?'numbox_box_xz':'numbox_box_wxz'">
                            {{language.storeCoupon.bxz}}
                        </view>
                        <view v-if="MentalRay==2" :class="MentalRay==2?'numbox_box_xz':'numbox_box_wxz'">
                            {{language.storeCoupon.szsl}}
                        </view>
                    </block>

                </view>
                <view class="addCoupon_list" style="justify-content: flex-end;
                margin-right: 20rpx;" v-if="MentalRay==2">
                    <view class="addCoupon_list_left">

                    </view>
                    <view class="numbox" style="width: 486rpx;">
                        <view :class="!editsFlag?'input_box':'input_box1'" style="width: 100%;">
                            <input :disabled="editsFlag" class="addCoupon_list_ipt" v-model="circulation" type="number"
                                @input="limitCode" :placeholder="language.storeCoupon.issueNumberPlaceholder"
                                placeholder-style="color:#b8b8b8">
                            {{language.storeCoupon.zhang}}
                        </view>
                    </view>


                </view>
                <!-- 领取限制 -->
                <view class="addCoupon_list" v-if="is_receive!=1">
                    <view class="addCoupon_list_left">
                        <text>*</text>
                        {{language.storeCoupon.limit}}
                    </view>
                    <view :class="!editsFlag?'input_box':'input_box1'">
                        <input v-if="limit_type == 1&&!editsFlag" class="addCoupon_list_ipt" v-model="receive"
                            type="number" @input="limitCode2" :placeholder="language.storeCoupon.limitPlaceholder"
                            placeholder-style="color:#b8b8b8">
                        <input v-else :disabled="editsFlag" class="addCoupon_list_ipt" v-model="receive" type="digit"
                            :placeholder="language.storeCoupon.limitPlaceholder" placeholder-style="color:#b8b8b8">
                        {{language.storeCoupon.zhang}}
                    </view>

                </view>
                <!-- 开始时间 -->
                <view class="addCoupon_list" style="height: 148rpx;align-items: baseline;">
                    <view class="addCoupon_list_left">
                        {{language.storeCoupon.endTime}} 
                    </view>
                    <view class="slectBox" v-if="!editsFlag">
                        <view :class="is_expiration==0?'numbox_box_xz':'numbox_box_wxz'" @click="expiration(0)">
                            {{language.storeCoupon.wxz}}
                        </view>
                        <view :class="is_expiration==1?'numbox_box_xz':'numbox_box_wxz'" @click="expiration(1)">
                            {{language.storeCoupon.zdgqsj}}
                        </view>
                        <view :class="is_expiration==2?'numbox_box_xz':'numbox_box_wxz'" @click="expiration(2)">
                            {{language.storeCoupon.lqhdj}}
                        </view>
                    </view>
                    <view class="slectBox" v-else> 
                        <view v-if="!end_time&&!day" :class="!end_time&&!day?'numbox_box_xz':'numbox_box_wxz'"
                            @click="expiration(0)">{{language.storeCoupon.wxz }}</view>
                        <view v-if="end_time&&!day" :class="end_time&&!day?'numbox_box_xz':'numbox_box_wxz'"
                            > {{language.storeCoupon.zdgqsj}}</view>
                        <view v-if="!end_time&&day" :class="!end_time&&day?'numbox_box_xz':'numbox_box_wxz'"
                            @click="expiration(2)">{{language.storeCoupon.lqhdj }}</view>
                    </view>
                </view>
                <view class="addCoupon_list" v-if=" end_time || (!day&&is_expiration==1)">
                    <view class="addCoupon_list_left">
                    </view>
                    <view @click="clickEndtime" :class="!editsFlag?'input_box':'input_box1'">
                        <input :disabled="editsFlag" v-model="end_time.substring(0,10)" disabled
                            class="addCoupon_list_ipt" type="text" :placeholder="language.storeCoupon.xzsj"
                            placeholder-style="color:#b8b8b8">
                        <image class="jiantou" :src="jiantou"></image>
                    </view>
                </view>
                <view class="addCoupon_list" v-if="is_expiration==2 || day">
                    <view class="addCoupon_list_left">
                    </view>
                    <view :class="!editsFlag?'input_box':'input_box1'">
                        <!-- style="pointer-events: none;" -->
                        <input type="number" @input="checkPositiveInteger" v-model="day" :disabled="editsFlag"
                            class="addCoupon_list_ipt" :placeholder="language.storeCoupon.qsrlq"
                            placeholder-style="color:#b8b8b8">{{language.storeCoupon.tian}}
                    </view>
                </view>

                <view class="addCoupon_lista" style="border-bottom: 0;">
                    <view class="addCoupon_list_left" style="margin-top: 16rpx;">
                        {{language.storeCoupon.instructions}}
                    </view>
                    <view :class="!ck?'input_boxa':'input_boxal'">
                        <textarea :disabled="ck" class="addCoupon_list_iptaa" type="text" v-model="Instructions"
                            maxlength="-1" :placeholder="language.storeCoupon.instructionsPlaceholder"
                            placeholder-style="color:#b8b8b8"></textarea>
                    </view>
                </view>
            </view>
            <view style="height: 200rpx;"></view>
             
            <view v-if="pagetitle != '查看优惠券'" @click="saveAdd" class="addCoupon_btn">
                <view>{{language.storeCoupon.submit}}</view>
            </view>
     
        </view>

        <view class="seeCoupon" v-else>
            <view class="seeCoupon_item" v-for="item,index of coupon_list" :key="index">
                <view class="seeCoupon_item_top"
                    :style="{backgroundImage: 'url('+(item.status==3?mycoupon_ygq:mycoupon_redbj)+    ')'}">
                    <view class="seeCoupon_item_top_left" :style="{color:item.status==3?'#666666':'#FA5151'}">
                        <view v-if="item.activity_type == '满减券'">
                            <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>{{LaiKeTuiCommon.formatPrice(item.money)}}
                        </view>
                        <view v-if="item.activity_type == '折扣券'">
                            {{item.discount}}<text>{{language.storeCoupon.discount}}</text>
                        </view>
                        <text :style="{color:item.status==3?'#666666':'#FA5151'}"
                            v-if="item.z_money>0">{{language.storeCoupon.full}}{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.z_money)}}{{language.storeCoupon.use}}</text>
                        <text :style="{color:item.status==3?'#666666':'#FA5151'}"
                            v-else>{{language.storeCoupon.noThreshold}}</text>
                    </view>
                    <view class="seeCoupon_item_top_right" :style="{color:item.status==3?'#666666':'#FA5151'}">
                        <view :style="{color:item.status==3?'#666666':'#FA5151'}">{{item.name}}</view>
                        <text v-if="item.day==0&&!item.end_time"
                            :style="{color:item.status==3?'#666666':'#FA5151'}">{{language.storeCoupon.gqyj}}</text>
                        <text v-if="item.day==0&&item.end_time"
                            :style="{color:item.status==3?'#666666':'#FA5151'}">{{language.storeCoupon.gqjz}}{{item.expirationDate}}{{language.storeCoupon.dq}}</text>
                        <text v-if="item.day!=0"
                            :style="{color:item.status==3?'#666666':'#FA5151'}">{{language.storeCoupon.lqh}}{{item.day}}{{language.storeCoupon.tsx}}</text>
                    </view>

                </view>

                <view class="seeCoupon_item_center">
                    <view v-if="item.issue_number_type!='1'">
                        {{language.storeCoupon.circulation}}
                        <text>{{item.circulation}}</text>
                    </view>
                    <view v-else>
                        {{language.storeCoupon.circulation}}
                        <text>{{language.storeCoupon.bxz }}</text>
                    </view>
                    <view>
                        {{language.storeCoupon.remaining}}
                        <text v-if="item.issue_number_type==1">{{language.storeCoupon.bxz }}</text>
                        <text v-else>{{item.num}}</text>
                    </view>
                </view>
                <view class="seeCoupon_item_bottom">
                    <view @tap.stop="delCoupon(item.id)">{{language.storeCoupon.del}}</view>
                    <view @tap.stop="edits(item.id)" v-if="item.status!=3">{{language.storeCoupon.editor}}</view>
                    <view @tap.stop="edits(item.id,1)" v-if="item.status==3">{{language.storeCoupon.ck}}</view>
                    <view @tap.stop="navto(item.id)">{{language.storeCoupon.recorda}}</view>
                    <view @tap.stop="toUrl('/pagesA/myStore/seeCoupon?id='+item.id)">{{language.storeCoupon.record}}
                    </view>
                </view>
            </view>

            <view class="no-bargain" v-if="coupon_list.length == 0 && hasLoad">
                <img :src="noCoupon">
                <p>{{language.storeCoupon.noFx}}</p>
            </view>

            <uni-load-more v-if="coupon_list.length>8" :loadingType="loadingType"></uni-load-more>
        </view>


        <date ref="starttimePicker" :themeColor="themeColor" :is_min="is_min" :urseTime="start_time"
            @onConfirm="onConfirm1"></date>
        <date ref="endtimePicker" :themeColor="themeColor" :is_min="is_min" :urseTime="end_time"
            @onConfirm="onConfirm2"></date>

        <view class="mask" v-if="delFlag">
            <view class="delMask">
                <text>{{language.storeCoupon.confirmDel}}</text>
                <view>
                    <view @tap="delFlag = false">{{language.storeCoupon.cancel}}</view>
                    <view style="color: #D73B48;" @tap="delOk">{{language.storeCoupon.confirm}}</view>
                </view>
            </view>
        </view>
        <view class="xieyi" style="background-color: initial;" v-if="is_tishi">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    {{is_tishi_content}}
                </view>
            </view>
        </view>
        <!-- 注册成功 -->
        <show-toast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj"></show-toast>
    </view>
</template>

<script>
    import { LaiKeTui_chooseImg2, LaiKeTui_delImg2 } from '@/pagesA/myStore/myStore/uploadPro.js';
    import date from '../../components/date-time-picker.vue'
    import showToast from '@/components/aComponents/showToast.vue'
    export default {
        data() {
            return {
                //弹窗
                is_tishi: false,
                is_tishi_content: '添加成功',
                title: '优惠券',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                couponBg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/couponBg.png',
                noCoupon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coupon_qsy.png',
                mycoupon_redbj: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_redbj.png',
                mycoupon_ygq: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_ygq.png',
                type_list: ['满减券', '折扣券', '免邮券', '会员赠送'],
                type_index: [],
                activity_type: -1,
                name: '',
                circulation: '',
                money: '',
                discount: '',
                z_money: '',
                is_min: false,
                pro_list: '',
                pro_listText: '',
                munu_list: '', //选中分类的ID集合
                munu_listText: '', //选中分类展示在前端的名字集合
                start_time: '',
                end_time: '',
                ck: false,
                day: '', //多少天后过期
                receive: '',
                menkan: 1,
                pagetitle: '发行优惠券',
                Instructions: '',
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                type: 1,
                is_expiration: 0, //过期时间选项
                themeColor: '#FA5151',
                tabIndex: 0,
                limit_type: 0,
                page: 1,
                coupon_list: [],
                loadingType: 0,
                MentalRay: 1, //发行数量
                is_receive: 0, //领取方式
                editsFlag: false,
                editsId: '',
                delFlag: false,
                delId: '',
                hasLoad: false,
                //组件化弹窗
                is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
                is_showToast_obj: {}, //imgUrl提示图标  title提示文字
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                lastValidValue: null ,// 用于存储上一个有效的值
                textIcon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xiangji2x.png',
                delImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
                cover_map: '', // 封面图
            };
        },

        onUnload() {
            uni.removeStorageSync('chooseClass')
            uni.removeStorageSync('chooseClassText')
            uni.removeStorageSync('checkProList')
            uni.removeStorageSync('checkProListId')
        },
        onLoad() {
            uni.removeStorageSync('chooseClass')
            uni.removeStorageSync('chooseClassText')
            uni.removeStorageSync('checkProList')
            uni.removeStorageSync('checkProListId')
        },
        onReachBottom() {
            if (this.loadingType != 0) {
                return
            }
			if (this.tabIndex == 1) {
				return
			}
            this.loadingType = 1
            this.page++
            this.getList()
        },
        watch:{
            '$store.state.editCoupon':{
                handler(newVal,lodVal){ 
                    if(!newVal){
                        this.changeTab(0)
                    }
                }
            }
        },
        onShow() {
            this.pagetitle = this.language.storeCoupon.issuedCoupon
            this.ck = false
            if (uni.getStorageSync('chooseClass')) {
                this.munu_list = uni.getStorageSync('chooseClass')
                this.munu_listText = uni.getStorageSync('chooseClassText')
            }

            if (uni.getStorageSync('checkProList')) {
                let pro_list = uni.getStorageSync('checkProList')
                let pro_listText = []

                this.pro_list = []

                pro_list.filter(item => {
                    pro_listText.push(item.product_title)
                    this.pro_list.push(item.id)
                })
                this.pro_listText = pro_listText.join()
                this.pro_list = this.pro_list.join()
            }

            this.isLogin(() => {})

            if (this.tabIndex == 0) {
                this.page = 1
                this.getList()
            } else {
                this.axios()
            }
        },

        methods: {
            // 选择封面图片
            _chooseImg2() {
                LaiKeTui_chooseImg2(this);
            },
            // 删除图片
            _delImg2() {
                LaiKeTui_delImg2(this);
            },
            checkPositiveInteger(e) {
              e.target.value = e.target.value.replace(/^0|[^\d]|[.]/g, '')
               this.$nextTick(() => {
                 this.day= e.target.value
               })
            },
            //过期时间
            expiration(e) {
                this.is_expiration = e
                this.end_time = ''
                this.day = ''
            },
            //领取方式
            receiveClick(e) {
                this.is_receive = e
                if (e == 1) {
                    this.MentalRay = 1
                }
            },
            //发行数量
            MentalRayClick(e) {
                if (this.is_receive == 1) {

                } else {
                    this.MentalRay = e
                }
            },
            //消费门槛
            menkanclick(e) {
                this.menkan = e
                this.z_money = ''
                if (e == 2) {
                    this.is_receive = 0
                }
            },
            navto(e) {
                uni.navigateTo({
                    url: '/pagesA/myStore/couponrecord?id=' + e,
                    fail(err) {
                    }
                })
            },
            limitCode(e) {
                e.target.value = e.target.value.replace(/^0|[^\d]|[.]/g, '')
                this.$nextTick(() => {
                    this.circulation = e.target.value
                })

            },
            limitCode2(e) {
                e.target.value = e.target.value.replace(/^0|[^\d]|[.]/g, '')
                this.$nextTick(() => {
                    this.receive = e.target.value
                })

            },
            limitCode3(e) {
                e.target.value = e.target.value.replace(/[^\.\d]/g, '')
                e.target.value = e.target.value.replace('.', '')
                this.$nextTick(() => {
                    this.discount = e.target.value
                })

            },
            async edits(id, is_ck) {
                this.tabIndex = 1
                this.editsFlag = true
                this.editsId = id
                if (is_ck == 1) {
                    this.ck = true
                }else{
                    this.ck = false
					this.$store.commit('SET_EDITCOUPON',true)
				}
                
                let data = {
                    api: "plugin.coupon.AppMchcoupon.ModifyPage",
                    mch_id: uni.getStorageSync('shop_id'), // 店铺id
                    id
                }

                await this.$req.post({
                    data
                }).then(res => {
                    let {
                        code,
                        message,
                        data
                    } = res
                    if (code == 200) {

                        this.name = data.name;
                        this.circulation = data.circulation;
                        this.money = data.money;
                        this.discount = data.discount;
                        this.z_money = data.z_money;
                        if (this.z_money) {
                            this.menkan = 2
                        }
                        this.start_time = data.start_time;
                        this.end_time = data.end_time;
                        this.receive = data.limitCount;
                        this.Instructions = data.instructions;
                        this.type = data.type;
                        this.limit_type = data.limit_type;
                        this.day = data.day;
                        this.pro_list = data.product_id;
                        this.pro_listText = data.product_name;
                        this.munu_list = data.product_class_name;
                        this.munu_listText = data.product_class_name1;
                        this.cover_map = data.cover_map
                        this.type_list = []
                        this.type_index = []

                        res.data.coupon_type.filter((item, index) => {
                            this.type_list.push(item.name)
                            this.type_index.push(item.value)
                            if (data.activity_type == item.value) {
                                this.activity_type = index;
                            }
                        })
                        if (this.circulation == 9999999) {
                            this.MentalRay = 1
                        }else{
							this.MentalRay = 2
						}
                        if (this.ck == true) {
                            this.pagetitle = '查看优惠券'
                        }else {
                            this.pagetitle = '编辑优惠券'
                        }
                        
                        uni.setStorageSync('chooseClass', data.product_class_name)
                        uni.setStorageSync('chooseClassText', data.product_class_name1)
                        uni.setStorageSync('checkProListId', data.product_id)
                    } else {
                        uni.showToast({
                            title: message,
                            icon: 'none'
                        })
                    }
                })
            },
            delOk() {
                let data = {
                    api: "plugin.coupon.AppMchcoupon.Del",
                    mch_id: uni.getStorageSync('shop_id'), // 店铺id
                    id: this.delId
                }

                this.$req.post({
                    data
                }).then(res => {
                    let {
                        code,
                        message
                    } = res
                    if (code == 200) {
                        this.delFlag = false

                        this.page = 1
                        this.getList()
                        this.is_showToast = 1
                        this.is_showToast_obj.imgUrl = this.sus
                        this.is_showToast_obj.title = '删除成功'
                        setTimeout(() => {
                            this.is_showToast = 0
                        }, 1000)
                    }


                })
            },
            delCoupon(id) {
                this.delId = id
                this.delFlag = true
            },
          
            changeLoginStatus() {
                if (this.tabIndex == 1) {
                    this.page = 1
                    this.getList()
                } else {
                    this.axios()
                }
            },
            async saveAdd() { 
                if (this.activity_type < 0) {
                    uni.showToast({
                        title: this.language.storeCoupon.couponTips[0],
                        icon: 'none'
                    })
                    return
                }

                if (!this.name) {
                    uni.showToast({
                        title: this.language.storeCoupon.couponTips[1],
                        icon: 'none'
                    })
                    return
                }



                if (this.type_index[this.activity_type] == 2 && !this.money) {
                    uni.showToast({
                        title: this.language.storeCoupon.couponTips[3],
                        icon: 'none'
                    })
                    return
                }

                if (this.type_index[this.activity_type] == 3 && !this.discount) {
                    uni.showToast({
                        title: this.language.storeCoupon.couponTips[4],
                        icon: 'none'
                    })
                    return
                }

                if (this.type_index[this.activity_type] == 3 && Number(this.discount) > 10) {
                    uni.showToast({
                        title: '折扣值不能大于10',
                        icon: 'none'
                    })
                    return
                }



                if (this.type == 2 && !this.pro_listText) {
                    uni.showToast({
                        title: this.language.storeCoupon.couponTips[6],
                        icon: 'none'
                    })
                    return
                }

                if (this.type == 3 && !this.munu_listText) {
                    uni.showToast({
                        title: this.language.storeCoupon.couponTips[7],
                        icon: 'none'
                    })
                    return
                } 

                let activity_type = this.type_index[this.activity_type] 


                if (this.editsFlag == true) {
                    // data.app = 'modify'
                    var data = {
                        api: 'plugin.coupon.AppMchcoupon.Modify',
                        mch_id: uni.getStorageSync('shop_id'), // 店铺id
                        activityType: activity_type, // 活动类型
                        name: this.name, // 活动名称
                        circulation: this.circulation, // 发行数量
                        money: this.money, // 优惠券面值
                        discount: this.discount, // 折扣值
                        zmoney: this.z_money, // 满多少
                        type: this.type, // 优惠券使用范围
                        start_time: this.start_time, // 活动开始时间
                        end_time: this.end_time, // 活动结束时间
                        limitCount: this.receive, // 领取限制
                        menuList: this.pro_list, // 已选项
                        classList: this.munu_list, // 已选项
                        issueUnit: '2', //发行单位 0=商城 1=自营店 2店铺
                        day: this.day, //有效时间(设置多久(天)后失效)
                        receiveType: this.is_receive, //领取方式 0=手动领取 1=系统赠送
                        instructions: this.LaiKeTuiCommon.isempty(this.Instructions) ? "" : this.Instructions, // 使用说明
                        cover_map: this.cover_map||'',//
                    }
                    data.id = this.editsId
                } else {
                    var data = { 
                        api: "plugin.coupon.AppMchcoupon.Add",
                        mch_id: uni.getStorageSync('shop_id'), // 店铺id
                        activityType: activity_type, // 活动类型
                        name: this.name, // 活动名称
                        circulation: this.circulation, // 发行数量
                        money: this.money, // 优惠券面值
                        discount: this.discount, // 折扣值
                        zmoney: this.z_money, // 满多少
                        type: this.type, // 优惠券使用范围
                        start_time: this.start_time, // 活动开始时间
                        end_time: this.end_time, // 活动结束时间
                        limitCount: this.receive, // 领取限制
                        menuList: this.pro_list, // 已选项
                        classList: this.munu_list, // 已选项
                        issueUnit: '2', //发行单位 0=商城 1=自营店 2店铺
                        day: this.day, //有效时间(设置多久(天)后失效)
                        receiveType: this.is_receive, //领取方式 0=手动领取 1=系统赠送
                        instructions: this.LaiKeTuiCommon.isempty(this.Instructions) ? "" : this.Instructions, // 使用说明
                        cover_map: this.cover_map||'',//
                    }
                }
                if(!this.$store.state.falg){
                    return
                }
                this.$store.commit('upfalg',false)
                await this.$req.post({
                    data
                }).then(res => {
                    uni.hideLoading()
                    if (res.code == 200) {
                        this.changeTab(0)
                        this.ck = false
                        if (this.editsId != '') {
                            this.is_tishi_content = this.language.zdata.bjcg
                        } else {
                            this.is_tishi_content = this.language.zdata.tjcg
                        }
                        setTimeout(() => {
                            this.is_tishi = true
                        }, 500)
                        setTimeout(() => {
                            this.is_tishi = false
                        }, 1500)
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    } 
                }).finally(()=>{
                    this.$store.commit('upfalg',true)
                })
            },
            reset() {
                this.activity_type = -1;
                this.name = '';
                this.circulation = '';
                this.money = '';
                this.discount = '';
                this.z_money = '';
                this.pro_list = '';
                this.pro_listText = '';
                this.munu_list = '';
                this.munu_listText = '';
                this.start_time = '';
                this.end_time = '';
                this.receive = '';
                this.Instructions = '';
                this.type = 1;

                uni.removeStorageSync('chooseClass')
                uni.removeStorageSync('chooseClassText')
                uni.removeStorageSync('checkProList')
            },
            axios() {
                let data = {

                    api: "plugin.coupon.AppMchcoupon.AddPage",
                    mch_id: uni.getStorageSync('shop_id'), // 店铺id
                }

                this.$req.post({
                    data
                }).then(res => {
                    this.limit_type = res.data.limit_type

                    this.type_list = []
                    this.type_index = []

                    res.data.coupon_type_list.filter(item => {
                        this.type_list.push(item.name)
                        this.type_index.push(item.value)

                    })

                    if (this.limit_type == 0) {
                        this.receive = 1
                    }
                })

            },
            changeTab(index) {
                if (this.tabIndex == index) {
                    return
                }
                this.tabIndex = index
                this.editsFlag = false
                this.pagetitle = this.language.storeCoupon.issuedCoupon
                this.munu_list = ''
                this.munu_listText = ''
                this.day = ''
                this.pro_listText = ''
                this.pro_list = ''

                uni.removeStorageSync('chooseClass')
                uni.removeStorageSync('chooseClassText')
                uni.removeStorageSync('checkProList')
                uni.removeStorageSync('checkProListId')

                if (this.tabIndex == 0) {
                    this.page = 1
                    this.getList()
                } else {
                    this.reset()
                    this.axios()
                }
            },
            getList() {
                let data = {

                    api: 'plugin.coupon.AppMchcoupon.MchIndex',
                    mch_id: uni.getStorageSync('shop_id'), // 店铺id
                    page: this.page
                }

                this.$req.post({
                    data
                }).then(res => {
                    this.hasLoad = true

                    let {
                        code,
                        data,
                        message
                    } = res
                    if (code == 200) {
                        data.list.filter(item => {
                            item.money = Number(item.money)
                            item.z_money = Number(item.z_money)
                            item.start_time = item.start_time.substr(0, 10).replace(/-/g, '.')
                            item.end_time = item.end_time.substr(0, 10).replace(/-/g, '.')
                        })

                        if (this.page == 1) {
                            this.coupon_list = []
                        }

                        this.coupon_list.push(...data.list)
                        this.cover_map = data.cover_map
                        if (data.list.length > 0) {
                            this.loadingType = 0
                        } else {
                            this.loadingType = 2
                        }
                    } else {
                        uni.showToast({
                            title: message,
                            icon: 'none'
                        })
                    }
                })
            },
            toUrl(url) {
                if (this.editsFlag == false) {
                    uni.navigateTo({
                        url
                    })
                }

            },
            radioChange(e) {
                this.type = e

                this.munu_list = ''
                this.munu_listText = ''

                this.pro_listText = ''
                this.pro_list = ''

                uni.removeStorageSync('chooseClass')
                uni.removeStorageSync('chooseClassText')
                uni.removeStorageSync('checkProList')
                uni.removeStorageSync('checkProListId')
            },
            bindPickerChange(e) {
                this.activity_type = Number(e.detail.value)

            },
            // 营业时间-确定
            onConfirm1(e) {
                var start = e[0].replace(/undefined/g, '00');
                var end = e[1].replace(/undefined/g, '00');
                this.start_time = start + ' ' + end;
            },
            clickStarttime() {
                if (!this.editsFlag) {
                    this.$refs.starttimePicker.show(this.start_time);
                }
            },
            onConfirm2(e) {
                var start = e[0].replace(/undefined/g, '00');
                var end = e[1].replace(/undefined/g, '00');
                this.end_time = start + ' ' + end;
                this.end_time.substring(0, 10)
            },
            clickEndtime() {
                if (!this.editsFlag) {
                    this.$refs.endtimePicker.show(this.end_time);
                }
            }
        },
        components: {
            showToast,
            date
        }
    };
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped>
    @import url("@/laike.less");
    @import url('../../static/css/myStore/coupon.less');
    .rightInput{
        display: block;
        flex: 1;
        position: relative;
        .upImg {
            width: 144rpx;
            height: 144rpx;
            border: 1rpx dashed #CCCCCC;
            font-size: 24rpx;
            text-align: center;
            padding: 8rpx;
            border-radius: 16rpx;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            .wh_48{
                width: 48rpx;
                height: 48rpx;
            }
        }
        .upImg1 {
            width: 144rpx;
            height: 144rpx;
            position: relative;
            margin-right: 10rpx;
            margin-bottom: 16rpx;
            border-radius: 16rpx;
            .wh_100 {
                width: 100% !important;
                height: 100% !important;
                border-radius: 16rpx !important;
                z-index: 99 !important;
            }
            .delImg {
                width: 32rpx;
                height: 32rpx;
                position: absolute;
                top: 8rpx;
                right: 8rpx;
                z-index: 2;
            }
        }
        .jianyi {
            margin-top: 16rpx;
            font-size: 24rpx;
            color: #999999;
        }
    }
</style>
