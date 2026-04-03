<template>
    <view class="container">
        <view class="position_head">
           <view class="overlay">
               <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
               <heads :title="language.myStore.ledgerRecord.fzjl"  :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
                   <div class='home_navigation' :class="is_mask?'is_mask':''">
                       <div class='home_input'>
                           <img class='searchImg' :src="serchimg" alt="">
                           <input type="text" v-model.trim="searchtxt" :placeholder="placeholder1" placeholder-style="color:#999999;" @confirm="_placeholder()" id='input' name="sourch"/>
                       </div>
                       <div class='search_btn' @tap='_timeMask'>{{search_time==''?language.myFinance.xzrq:search_time}}</div>
                   </div>
               </view>
        </view>
        
        
        <view class="LKT-list" v-if="listdata&&listdata.length>0">
            <view class="LKT-Item" v-for="(item,index) in listdata" @tap="ToUrl(item)">
                <view class="item-content">
                    <view class="Item-top">
                        <label for="">{{item.account}}</label>
                        <span class="Unsettled">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.amount)}}</span>
                    </view>
                    <view class="Item-bottom">
                        <span>{{language.myStore.OrderSet.dd}}{{item.order_no}}</span>
                        <font>{{item.add_date}}</font>
                    </view>
                </view>
                <view class="item-icon">
                    <image :src="jiantou" class="right-icon"></image>
                </view>
            </view>
        </view>
        
        <view class="proList proList_no" v-if="listdata&&listdata.length == 0">
            <image :src="OrderSet_noOrder" alt="" class="noFindImg">
            <view>{{language.myStore.ledgerRecord.tips}}</view>
        </view>
        <view class="mask"  v-if="is_mask" @click.stop="_timeMask()">
            <view class="time">
                <view class="time_txt">{{language.myFinance.xzrq}}</view>
                <view class="time_time">
                    <view class="time_time_ks" :style="{'color':startDatetxt==language.myFinance.kssj?'':' #333333'}" @click.stop="open_time(1)">{{startDatetxt}}</view>
                    <view class="time_time_z">{{language.myFinance.zhi}}</view>
                    <view class="time_time_ks" :style="{'color':endDatetxt==language.myFinance.jssj?'':' #333333'}" @click.stop="open_time(2)">{{endDatetxt}}</view>
                </view>
                <view class="time_btn">
                    <view class="time_btn_cz" @click.stop="reset">{{language.myFinance.chongz}}</view>
                    <view class="time_btn_ss" @click.stop="_axiosGoods">{{language.myFinance.shous}}</view>
                </view>
            </view>
            <date ref="starttimePicker" :themeColor="themeColor" :is_min="is_min" :urseTime="start_time" @onConfirm="onConfirm1"></date>
        </view>
    </view>
</template>

<script>
    import loadmore from '@/components/uni-load-more.vue';
    import date from '../../components/date-time-picker.vue'
    export default {
        data() {
            return {
                listdata: [],
                searchtxt:'',
                placeholder1: '请输入订单号/分账账号',
                search_time:'',
                is_mask:false,
                start_time: '',
                startDatetxt:'',
                startDate:'',
                endDate:'',
                endDatetxt:'',         
                search_time:'',
                is_ks:false,
                themeColor: '#FA5151',
                is_min:false,
                isactive: 0,
                Down_arrow: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/Down_arrow.png',
                noPro: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noPro.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
                OrderSet_noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/OrderSet_noOrder.png',
                serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
                page: 1,
                loadingType: 0,
                z_total: '',
                mchid:'',
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ],
                
            }
        },
        onLoad() {
            this.isLogin()
            // this._axios()
        },
        onShow () {
            uni.showLoading({
                title: this.language.toload.loading
            });
            this.startDatetxt = this.language.myFinance.kssj
            this.endDatetxt = this.language.myFinance.jssj
            this.mchid = uni.getStorageSync('mch_id') 
            this.placeholder1 = this.language.myStore.ledgerRecord.qsrddhfzzh//选择日期文字提示
            this.listdata = [];
            this.page = 1;
            this._axios();
        },
        onReachBottom() {
            this.loadingType = 1;
            this.page++;
            this._axios();
        },
        components: {
            date
        },
        methods: {
            ToUrl(item) {
                uni.navigateTo({
                    url : '/pagesB/myStore/ledgerReDetail?arr=' + JSON.stringify(item)
                })
            },
            changeLoginStatus() {
                this._axios();
            },
            _axios() {
                let data = {
                    api:'mch.App.LedgerRecords.queryLedgerRecord',
                    // 导出
                    mchId:this.mchid,
                    pageSize: 10,
                    // 每页显示多少条数据
                    pageNo: this.page
                }
                data.condition=this.searchtxt,
                data.startDate=this.startDatetxt==this.language.myFinance.kssj?'':this.startDatetxt,
                data.endDate=this.endDatetxt==this.language.myFinance.jssj?'':this.endDatetxt
                this.$req.post({data}).then(res => {
                     uni.hideLoading();
                    if (res.code == 200) {
                        this.z_total = res.data.settlementPrice
                        if (res.data.list.length>0) {
                            this.loadingType = 2;
                            this.listdata.push(...res.data.list);
                        }
                        
                        if (res.data.list.length > 10) {
                            this.loadingType = 0;
                        }
                        
                    }else{
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }
                })
            },
            _placeholder(){
                this.listdata = [];
                this.page = 1;
                this._axios()
            },
            _timeMask(){
                this.is_mask = !this.is_mask
            },
            open_time(e){
                if(e==1){
                    this.is_ks = true
                }
                if(e==2){
                    this.is_js = true
                }
                    this.$refs.starttimePicker.show(this.start_time);
            },
            reset(){
                this.startDatetxt=this.language.myFinance.kssj
                this.endDatetxt=this.language.myFinance.jssj
                this.searchtxt=''//输入内容
                this.placeholder1 = this.language.myStore.ledgerRecord.qsrddhfzzh//选择日期文字提示
                this.startDate=''
                this.endDate=''
                this.search_time=''
                this.listdata = [];
                this.is_mask=false
                this._placeholder()
            },
            _axiosGoods() {
                let m = this
                m.is_mask=false//关闭弹窗
                if(m.startDatetxt==this.language.myFinance.kssj&&m.endDatetxt==this.language.myFinance.jssj){
                    return
                }
                m.search_time=m.startDatetxt+'-'+m.endDatetxt
                m._placeholder()
            },
            // 营业时间-确定
            onConfirm1(e) {
                var start = e[0].replace(/undefined/g, '00');
                var end = e[1].replace(/undefined/g, '00');
                this.start_time = start + ' ' + end;
                if(this.is_ks==true){
                    this.startDate = start
                    this.startDatetxt = start
                    this.is_ks = false
                }
                if(this.is_js == true){
                    this.endDate = start
                    this.endDatetxt = start
                    this.is_js = false
                }
            },
        }
    }
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped> 
    @import url("@/laike.less");
    .container{
        background-color: #f4f5f6;
        min-height: 100vh;
        .position_head{
            position: sticky;
            top: 0;
            width: 100%;
            z-index: 990;//sticky对于z-index无效
            .overlay{
                // overflow-y: overlay;
            }
        }
    }
    .lkt-tab{
        display: flex;
        justify-content: space-around;
        padding-bottom: 44rpx;
        background: #FFFFFF;
        padding-top: 32rpx;
        border-radius: 0px 0px 24rpx 24rpx;
        .tab-item{
            position: relative;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 32rpx;
            
            font-weight: 500;
            color: #999999;
        }
        .flex{
            flex: 1;
        }
        .tab_active{
           color: #333333;
        }
        .tab_active:after{
            content: '';
            position: absolute;
            left: 50%;
            transform: translateX(-50%);
            bottom: -16rpx;
            width: 96rpx;
            height: 4rpx;
            background: #FA5151;
            border-radius: 4rpx;
        }
    }
    .dateChange {
        background: #FFFFFF;
        padding:0 32rpx 32rpx 32rpx;
        border-radius: 0 0 24rpx 24rpx;
        .price {
            display: flex;
            align-items: center;
            background: rgba(7,193,96,0.1);
            border-radius: 20rpx;
            padding: 56rpx 48rpx;
            font-size: 32rpx;
            
            font-weight: 500;
            color: #07C160;
        }
       .Unsettled{
            background: rgba(250, 81, 81, .1);
       }
    }
    .Unsettled{
        color: #FA5151 !important;
    }
    .home_navigation {
        border-bottom: none;
        background-color: #FFFFFF;
        align-items: flex-end;
        box-sizing: border-box;
        border-radius: 0 0 24rpx 24rpx;
        padding: 0 30rpx 0 32rpx;
        padding-bottom: 12rpx;
        position: fixed;
        z-index:99;
        .search_btn {
            width: 176rpx;
            height: 64rpx;
            padding: 0 12rpx;
            margin-left: 16rpx;
            box-sizing: border-box;
            background-color: #F4F5F6;
            border-radius: 32rpx;
            border: 2rpx solid #F4F5F6;
            
            font-size: 28rpx;
             
            font-weight: 400;
            color: #999999;
            text-align: center;
            line-height: 60rpx;
            
            word-wrap: break-word;        text-overflow: -o-ellipsis-lastline;        overflow: hidden; //溢出内容隐藏        text-overflow: ellipsis; //文本溢出部分用省略号表示        display: -webkit-box; //特别显示模式        -webkit-line-clamp: 1; //行数        line-clamp: 1;         -webkit-box-orient: vertical; //盒子中内容竖直排列
        }
        .home_input{
            max-width: 496rpx;
        }
        .home_input input{
            font-size: 24rpx;
             
            font-weight: normal;
            color: #333333;
            height: 64rpx;
            border-radius: 32rpx;
            padding-left: 70rpx;
            padding-right: 54rpx;
            background-color: #F4F5F6;
        }
        
        .searchImg{
            width: 34rpx;
            height: 34rpx;
            left: 20rpx;
            top: 50%;
            transform: translateY(-50%);
        }
    
    }
    .is_mask{
        z-index: 999;
        border-radius: 0;
        border-bottom: 1rpx solid rgba(0,0,0,.1);
    }
    .mask{
        z-index:98;
        background-color: rgba(0,0,0,.5) !important;
    }
    /deep/.pickerMask{
        background: rgba(0, 0, 0, 0) !important;
    }
    /* #ifdef H5 || APP-PLUS */
    .time{
        width: 750rpx;
        height: 344rpx;
        background:#FFFFFF;
        position: fixed;
        top: 176rpx;
        left: 0;
        border-radius: 0rpx 0rpx 16rpx 16rpx;
        display: flex;
        flex-direction: column;
        z-index: 98;
    }
    /* #endif */
    /* #ifdef MP-WEIXIN */
    .time{
        width: 750rpx;
        height: 344rpx;
        background:#FFFFFF;
        position: fixed;
        top: 264rpx;
        left: 0;
        border-radius: 0rpx 0rpx 16rpx 16rpx;
        display: flex;
        flex-direction: column;
        z-index: 98;
    }
    /* #endif */
    /* #ifdef APP-PLUS */
    .time{
        width: 750rpx;
        height: 344rpx;
        background:#FFFFFF;
        position: fixed;
        top: 230rpx;
        left: 0;
        border-radius: 0rpx 0rpx 16rpx 16rpx;
        display: flex;
        flex-direction: column;
        z-index: 98;
    }
    /* #endif */
    .time_time{
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .time_time_ks{
        width: 304rpx;
        height: 92rpx;
        text-align: center;
        line-height: 92rpx;
        background: rgba(255,255,255,0.2);
        border-radius: 16rpx;
        font-size: 32rpx;
        
        font-weight: normal;
        color: #999999;
        border: 2rpx solid rgba(0,0,0,0.1);
    }
    .time_time_z{
        font-size: 32rpx;
        
        font-weight: normal;
        color: #333333;
        margin-left: 24rpx;
        margin-right: 22rpx;
    }
    .time_txt{
        margin-top: 32rpx;
        margin-bottom: 24rpx;
        margin-left: 32rpx;
        font-size: 32rpx;
         
        font-weight: normal;
        color: #333333;
    }
    .time_btn{
        margin-top: 40rpx;
        margin-bottom: 40rpx;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .time_btn_cz{
        width: 340rpx;
        height: 72rpx;
        border-radius: 36rpx;
        background: #CCCCCC;
        font-size: 28rpx;
         
        font-weight: normal;
        color: #FFFFFF;
        text-align: center;
        line-height: 72rpx;
        margin-right: 22rpx;
    }
    .time_btn_ss{
        text-align: center;
        line-height: 72rpx;
        width: 340rpx;
        height: 72rpx;
        border-radius: 36rpx;
        background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
        font-size: 28rpx;
        
        font-weight: normal;
        color: #FFFFFF;
    }
    .choice{
        display: flex;
        justify-content: space-between;
        align-items: center;
        text-align: center;
        margin-bottom: 32rpx;
        .choice-center{
            margin:  0 16rpx;
            width: 42rpx;
            height: 44rpx;
            color: #999999;
        }
        .choice-left,.choice-right{
            display: flex;
            align-items: center;
            justify-content: center;
            width: 306rpx;
            border-radius: 16rpx;
            border: 1rpx solid rgba(0,0,0,0.1);
            padding: 16rpx;
            .uni-input{
                font-size: 32rpx;
                
                font-weight: 400;
                color: #999999;
            }
        }
        image{
            width: 28rpx;
            height: 16rpx;
            padding-left: 22rpx;
        }
    }
    .LKT-list{
        margin-top:114rpx ;
        background-color: #f4f5f6;
        overflow-y: scroll;
        view{
            overflow: hidden;
        }
    }
    .LKT-Item{     
        background-color: #FFFFFF;
        padding: 32rpx;
        margin-bottom:16rpx ;
        border-radius: 24rpx;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .item-content{
            flex: 1;
        }
        .item-icon{
            margin-left: 8rpx;
            width: 32rpx;
             height: 44rpx;
           .right-icon{
               width: 32rpx;
                height: 44rpx;
           }
        }
        .Item-top{
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 36rpx;
            label{
                font-weight: 400;
                color: #333333;
                
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
            span{
                font-weight: 500;
                color: #07C160;
                width: 220rpx;
                text-align: right;
            }
            em{
                margin-left: 20rpx;
                font-style: normal;
                padding: 0 10rpx;
                height: 28rpx;
                border: 1px solid #B8B8B8;
                border-radius: 2px;
                font-size: 22rpx;
                font-weight: 500;
                color: #888888;
            }
        }
        .Item-bottom{
            margin-top: 16rpx;
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 24rpx;
            
            font-weight: 400;
            color: #999999;
        }
    }

.proList_no {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0;
    margin: 0;
    padding-top: 300rpx;
    height: auto;
    background-color: #f4f5f6;
    .noFindImg{
        width: 750rpx;
        height: 460rpx;
    }
    view{
        color: #333333;
        font-size: 28rpx;
        line-height: 28rpx;
        margin-top:-40rpx ;
    }
}


</style>
