<template>
    <div>
        <heads :title="language.walletDetail.title" :returnR="12" :bgColor="bgColor" titleColor="#333333" ishead_w="2" ></heads>
        <div class='mydiv'>
            
            
            <div class="mytop">
                <p class="mytop_p1"><span v-if='mydata.moneyType==1'>+</span><span v-if='mydata.moneyType==2'>-</span>
                    {{mydata.currency_symbol}}{{LaiKeTuiCommon.getPriceWithExchangeRate(mydata.money,mydata.exchange_rate)}}</p>
                <p class="mytop_p2">{{getType(mydata.type)}}</p>
                <p class="mytop_p3" v-if='mydata.status==2'>{{language.walletDetail.txsb}}</p>
            </div>
            
                <div class="mybody">
                    <div class="mybody_div"  v-if='mydata.type!=11'>
                        <p class="mybody_div_l" v-if='mydata.moneyType==1'>{{language.walletDetail.srlx}}</p>
                        <p class="mybody_div_l" v-if='mydata.moneyType==2'>{{language.walletDetail.zclx}}</p>
                        <p class="mybody_div_r">{{getTypeName(mydata.moneyTypeName)}}</p>
                    </div>
                    <!-- 充值 -->
                    <template v-if='mydata.type==1||mydata.type==2'> 
                        <div class="mybody_div" v-if='mydata.type==1'>
                            <p class="mybody_div_l">{{language.walletDetail.czfs}}</p>
                            <p class="mybody_div_r">{{mydata.typeName}}</p>
                        </div>
                        <div class="mybody_div" v-if='mydata.moneyTypeName!=1'>
                            <p class="mybody_div_l">{{language.walletDetail.bz}}</p>
                            <p class="mybody_div_r">{{mydata.recordNotes?mydata.recordNotes:''}}</p>
                        </div>
                        
                    </template>
                    <!-- 提现 -->
                    <template v-if='mydata.type==11'> 
                        
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.dzje}}</p>
                                <p class="mybody_div_r">{{mydata.account}}</p>
                            </div>
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.sxf}}</p>
                                <p class="mybody_div_r">{{mydata.withdrawalFees}}</p>
                            </div>
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.txfs}}</p>
                                <p class="mybody_div_r" v-if="mydata.withdrawalMethod == '微信零钱'" style="display: flex;align-items: center;">
                                   <image class="mybody_div_r-image" :src="wx"></image>{{mydata.withdrawalMethod}}
                                </p>
                                <p class="mybody_div_r" v-else>{{mydata.withdrawalMethod}}</p>
                            </div>
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.txdh}}</p>
                                <p class="mybody_div_r order-id" style=" "><span class="mybody_div_copy " style="" @click="mycopy(mydata.sNo)">{{language.walletDetail.fz}}</span>{{mydata.sNo}}</p>
                            </div>
                            
                    </template>
                    <!-- 活动佣金 -->
                    <template v-if='mydata.type==3'> 
                        
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.ddbh}}</p>
                                <p class="mybody_div_r order-id"><span class="mybody_div_copy" style="" @click="mycopy(mydata.sNo)">{{language.walletDetail.fz}}</span>{{mydata.sNo}}</p>
                            </div>
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.hdmc}}</p>
                                <p class="mybody_div_r">{{mydata.titleName}}</p>
                            </div>
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.hdbh}}</p>
                                <p class="mybody_div_r">{{mydata.activityCode}}</p>
                            </div>
                          
                    </template>
                    <!-- 订单退款 -->
                    <template v-if='mydata.type==4||mydata.type==7'> 
                        
                            <div class="mybody_div" v-if='mydata.type==4'>
                                <p class="mybody_div_l">{{language.walletDetail.spmc}}</p>
                                <p class="mybody_div_r">{{mydata.titleName}}</p>
                            </div>
                            <div class="mybody_div" v-if='mydata.type==4'>
                                <p class="mybody_div_l">{{language.walletDetail.dpmc}}</p>
                                <p class="mybody_div_r">{{mydata.mchName}}</p>
                            </div>
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.ddbh}}</p>
                                <p class="mybody_div_r order-id"><span class="mybody_div_copy" style="" @click="mycopy(mydata.sNo)">{{language.walletDetail.fz}}</span>{{mydata.sNo}}</p>
                            </div>
                            
                    </template>
                    <!-- 活动退款 -->
                    <template v-if='mydata.type==12'> 
                        
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.tklx}}</p>
                                <p class="mybody_div_r">{{mydata.typeName}}</p>
                            </div>
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.hdbh}}</p>
                                <p class="mybody_div_r">{{mydata.activityCode}}</p>
                            </div>
                            
                    </template>
                    <!-- 店铺保证金退款 -->
                    <template v-if='mydata.type==5||mydata.type==10'> 

                        
                    </template>
                    <!-- 平台扣款 -->
                    <template v-if='mydata.type==6'> 
                       
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.bz}}</p>
                                <p class="mybody_div_r">{{mydata.recordNotes?mydata.recordNotes:''}}</p>
                            </div>
                            
                    </template>
                    <!-- 开通会员 -->
                    <template v-if='mydata.type==9'> 
                        
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.ddbh}}</p>
                                <p class="mybody_div_r"><span class="mybody_div_copy" style="" @click="mycopy(mydata.sNo)">{{language.walletDetail.fz}}</span>{{mydata.sNo}}</p>
                            </div>
                    
                    </template>
                    <!-- 参与活动 -->
                    <template v-if='mydata.type==8'> 
                        
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.hdlx}}</p>
                                <p class="mybody_div_r">{{mydata.typeName}}</p>
                            </div>
                            <div class="mybody_div">
                                <p class="mybody_div_l">{{language.walletDetail.ddbh}}</p>
                                <p class="mybody_div_r"><span class="mybody_div_copy" style="" @click="mycopy(mydata.sNo)">{{language.walletDetail.fz}}</span>{{mydata.sNo}}</p>
                            </div>
                    </template>
                    
                    <div class="mybody_div">
                        <p class="mybody_div_l" v-if="mydata.type!=11&&mydata.moneyType==2">{{language.walletDetail.zcsj}}</p>
                        <p class="mybody_div_l" v-if="mydata.type!=11&&mydata.moneyType==1">{{language.walletDetail.dzsj}}</p>
                        <p class="mybody_div_l" v-if="mydata.type==11">{{language.walletDetail.txsj}}</p>
                        <p class="mybody_div_r">{{mydata.recordTime}}</p>
                    </div>
                    <div class="mybody_div">
<!--                        <p class="mybody_div_l">{{language.walletDetail.zhye}}</p>-->
                    </div>
                </div>
                <!-- 提现审核 -->
                <div class="mybody mybody_line" v-if='mydata.type==11'>
                    <div class="mybody_line_l">
                        <div class="round_r"></div>
                        <div class="line_r"></div>
                        <div :class="mydata.status==0?'round_g':'round_r'"></div>
                    </div>
                    <div style="width: 100%;">
                        <div class="mybody_div mybody_div_line">
                            <p class="mybody_div_l">{{language.walletDetail.fqsq}}</p>
                            <p class="mybody_div_r">{{mydata.startTime}}</p>
                        </div>
                        <div class="mybody_div mybody_div_line">
                            <p class="mybody_div_l" v-if='mydata.status==0'>{{language.walletDetail.ddpts}}</p>
                            <p class="mybody_div_l" v-if='mydata.status==2'>{{language.walletDetail.shyjj}}</p>
                            <p class="mybody_div_l" v-if='mydata.status==1'>{{language.walletDetail.shytg}}</p>
                            <p class="mybody_div_r" v-if='mydata.status==1||mydata.status==2'>{{mydata.examineTime}}</p>
                        </div>
                        <div class="mybody_div mybody_div_line" v-if='mydata.refuse'>
                            <p class="mybody_div_l"></p>
                            <p class="mybody_div_r">{{mydata.refuse}}</p>
                        </div>
                    </div>
                    
                </div>
            

            
        </div>
        


        
    </div>
</template>

<script>
export default {
    data() {
        return {
            title: '账单详情',
            id:'',
            tuihuo_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/tuihuo2x.png',
            jiantouhei_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            tuikuan_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/tuikuan2x.png',
            huanhuo_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/huanhuo.png',
            wx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wx.png',
            bgColor: [
                {
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            mydata:'',
        };
    },
    onLoad(option) {
        this.isLogin(()=>{})
        this.id=option.id
        this._axios();

    },
    methods: {

        /**
         *
         * */
        _uni_navigateTo(url) {
            uni.redirectTo({
                url
            });
        },
        getType(e){
            let name=''
            if(e==1||e==2){
                name='余额充值'
            }else if(e==3){
                name='活动佣金'
            }else if(e==4){
                name='订单退款'
            }else if(e==5){
                name='店铺保证金提取'
            }else if(e==6){
                name='平台扣款'
            }else if(e==7){
                name='订单支付'
            }else if(e==8){
                name='参与活动'
            }else if(e==9 || e==13){
                name='充值会员'
            }else if(e==10){
                name='店铺保证金支付'
            }else if(e==11){
                name='余额提现'
            }else if(e==12){
                name='活动退款'
            }
            return name
        },
        getTypeName(e){
            let name=''
            if(e==1){
                name='用户充值'
            }else if(e==2){
                name='平台充值'
            }else if(e==3){
                name='拼团活动开团佣金'
            }else if(e==4){
                name='普通订单'
            }else if(e==5){
                name='竞拍活动'
            }else if(e==6){
                name='店铺保证金'
            }else if(e==7){
                name='余额扣款'
            }else if(e==8){
                name='预售定金缴纳'
            }else if(e==9){
                name='会员开通'
            }else if(e==10){
                name='普通订单(代客下单)'
            }else if(e==11){
                name='拼团订单'
            }else if(e==12){
                name='竟拍订单'
            }else if(e==13){
                name='预售订单'
            }else if(e==14){
                name='分销订单'
            }else if(e==15){
                name='秒杀订单'
            }else if(e==16){
                name='积分订单'
            }else if(e==17){
                name='限时折扣订单'
            }else if(e==18){
                name='竞拍保证金缴纳'
            }else if(e==19){
                name='虚拟订单'
            }else if(e==20){
                name='直播订单'
            }else if(e==21){
                name='会员续费'
            }else if(e==42){
                name='直播佣金转入'
            }else if(e==23){
                name='预售尾款缴纳'
            }
            return name
        },
        mycopy(e){
            // #ifndef MP-WEIXIN
            uni.setClipboardData({
                data: e,
                success: function (res) {
                    
                }
            })
            // #endif
 
            
            // #ifdef MP-WEIXIN
            copyText('', e)
            // #endif
            
                           
            uni.showToast({
                title: this.language.expressage.copy_success,
                icon: 'none',
                duration: 1500
            })
        },
        /**
         * @description 加载数据
         * */
        _axios() {
            var data = {
                api:'app.user.getRecordDetails',
                id: this.id
            };

            this.$req.post({ data }).then(res => {
                if (res.code == 200) {
                    let { data } = res;
                    this.mydata=res.data
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            });
        }
    }
};
</script>
<style scoped lang="less">
    .order-id{
        white-space: nowrap !important;
        overflow: hidden;
        text-overflow: ellipsis;
        width: 500rpx !important;
    }
    .mybody_div_r-image{
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        margin-right: 12rpx;
    }
   
    .mydiv{
        background-color: #F4F5F6;
        min-height: 100vh;
    }
    .mytop{
        width: 100%;
        background: #FFFFFF;
        border-radius: 0px 0px 24rpx 24rpx;
        text-align: center;
        margin-bottom: 16rpx;
        padding-bottom: 64rpx;
        .mytop_p1{
            font-size: 56rpx;
            font-weight: bold;
            color: #333333;
            margin-top: 64rpx;
        }
        .mytop_p2{
            font-size: 32rpx;
            font-weight: normal;
            color: #666666;
            margin-top: 32rpx;
        }
        .mytop_p3{
            font-size: 24rpx;
            color: #FA5151;
            margin-top: 16rpx;
        }
    }
    
    .mybody{
        background: #FFFFFF;
        border-radius: 24rpx;
        margin-bottom: 16rpx;
        .mybody_div{
            // height: 108rpx;
            line-height: 108rpx;
            border-bottom: solid 1rpx #e6e6e6;
            margin: 0 32rpx;
            display: flex;
            justify-content: space-between;
            white-space: nowrap;
            .mybody_div_r{
                max-width: 600rpx;
                white-space: break-spaces;
            }
            .mybody_div_copy{
                color: #D73B48;
                margin-right: 16rpx;
            }
        }
        .mybody_div:nth-last-child(1){
            border-bottom: none
        }
        
        /deep/.uni-steps__column-text{
            
        }
    }
    .mybody_line{
        display: flex;
        .mybody_div_line{
            border-bottom: none;        }
        .mybody_line_l{
            margin: 44rpx 0 0 32rpx;
            .round_r{
                width: 24rpx;
                height: 24rpx;
                background: #FA5151;
                border-radius: 50%;
            }
            .round_g{
                width: 24rpx;
                height: 24rpx;
                background: #CCCCCC;
                border-radius: 50%;
            }
            .line_r{
                height: 86rpx;
                width: 1rpx;
                border: 1px solid #CCCCCC;
                margin-left: 11rpx;
            }
        }
        
    }
</style>

