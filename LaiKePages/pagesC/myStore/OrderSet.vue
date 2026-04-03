<template>
    <view class="container">
        <view class="position_head">
           <view class="overlay">
               <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
               <heads :title="language.myStore.OrderSet.title"  :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
               <view class="lkt-tab":style="{borderRadius:listdata&&listdata.length>0?'0px':''}">
                   <view class="tab-item " :class="{tab_active:index==isactive}" v-for="(item,index) in language.myStore.OrderSet.list" @tap="tabswitch(index)">
                       {{item}}
                   </view>
               </view>
               <view class="dateChange" >
                   <view class="choice">
                       <view class="choice-left">
                           <picker mode="date" :value="date" :start="startDate" :end="endDate" @change="bindDateChange">
                               <view class="uni-input" :style="date?'color:#333333;':''">{{date || language.myStore.OrderSet.kssj}}</view>
                           </picker>
                       </view>
                       <view class="choice-center">
                           /
                       </view>
                       <view class="choice-right">
                           <picker mode="date" :value="Enddate" :start="startDate" :end="endDate" @change="bindDateChange2">
                               <view class="uni-input" :style="Enddate?'color:#333333;':''">{{Enddate || language.myStore.OrderSet.jssj}}</view>
                           </picker>
                       </view>
                   </view>
                   <view class="price" :class="{Unsettled:isactive!=0}">
                       <span>{{isactive == 0 ? language.myStore.OrderSet.y : language.myStore.OrderSet.d}}{{language.myStore.OrderSet.jsje}}：</span>
                       <view>{{store_default_currency_symbol}}{{ z_total || 0 }}</view>
                   </view>
               </view>
           </view>
        </view>
        
        
        <view class="LKT-list" v-if="listdata&&listdata.length>0">
            <view class="LKT-Item" v-for="(item,index) in listdata" @tap="ToUrl(item)">
                <view class="item-content">
                    <view class="Item-top">
                        <label for="">{{language.myStore.OrderSet.jsdh}}{{item.id ? item.id : ''}}</label>
                        <span :class="{Unsettled:isactive!=0}">{{store_default_currency_symbol}}{{item.settlementPrice}}</span>
                    </view>
                    <view class="Item-bottom">
                        <span>{{language.myStore.OrderSet.dd}}{{item.sNo}}</span>
                        <font>{{item.add_time}}</font>
                    </view>
                </view>
                <view class="item-icon">
                    <image :src="jiantou" class="right-icon"></image>
                </view>
            </view>
        </view>
        
        <view class="proList proList_no" v-if="listdata&&listdata.length == 0">
            <image :src="OrderSet_noOrder" alt="" class="noFindImg"></image>
            <view>{{language.myStore.OrderSet.tips}}～</view>
        </view>
        
        <loadmore v-if="list.length > 9" :loadingType="loadingType"></loadmore>
    </view>
</template>

<script>
    import loadmore from '@/components/uni-load-more.vue';
    export default {
        data() {
            const currentDate = this.getDate({
                format: true
            })
            return {
                date: '',
                Enddate: '',
                list: [
                    '已结算',
                    '待结算'
                ],
                listdata: [],
                store_default_currency_code:'CNY',
                store_default_currency_symbol:'￥',
                store_default_exchange_rate:1,
                isactive: 0,
                Down_arrow: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/Down_arrow.png',
                noPro: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noPro.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
                OrderSet_noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/OrderSet_noOrder.png',
                page: 1,
                loadingType: 0,
                z_total: '',
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
            this._axios()
            let storeCurrency = uni.getStorageSync('storeCurrency');  
            this.store_default_exchange_rate = storeCurrency.exchange_rate;
            this.store_default_currency_symbol = storeCurrency.currency_symbol;
            this.store_default_currency_code = storeCurrency.currency_code;
        },
        onReachBottom() {
            this.loadingType = 1;
            this.page++;
            this._axios();
        },
        computed: {
            startDate() {
                return this.getDate('start');
            },
            endDate() {
                return this.getDate('end');
            }
        },
        
        methods: {
            ToUrl(item) {
                uni.navigateTo({
                    url : '/pagesC/myStore/Setdetails?arr=' + JSON.stringify(item)
                })
            },
            changeLoginStatus() {
                this._axios();
            },
            _axios() {
                let data = {
                    api:'mch.App.Mch.GetSettlementOrderList',
                    status: this.isactive == 0 ? 1 : 0,//计算状态，1已结算0待结算
                    startDate: this.date,
                    endDate: this.Enddate,
                    pageto: '',
                    // 导出
                    pagesize: 10,
                    // 每页显示多少条数据
                    page: this.page
                }
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                        this.z_total = res.data.settlementPrice
                        if (res.data.list.length>0) {
                            this.loadingType = 2;
                            this.listdata.push(...res.data.list);
                        }
                        
                        if (res.data.list.length > 10) {
                            this.loadingType = 0;
                        }
                        
                       
                    }
                })
            },
            bindDateChange(e) {
                this.page = 1
                this.date  = e.detail.value
                this.listdata=[]
                this._axios()
            },
            bindDateChange2(e) {
                this.page = 1
                this.Enddate = e.detail.value
                if(this.date > this.Enddate){
                    uni.showToast({
                        title:'时间范围选择有误',
                        icon:'none'
                    })
                    this.Enddate=''
                    return
                }
                this.listdata=[]
                this._axios()
            },
            tabswitch(index) {
                this.isactive = index
                this.listdata = []
                this.page = 1
                this.date=''
                this.Enddate=''
                this._axios()
            },
            getDate(type) {
                const date = new Date();
                let year = date.getFullYear();
                let month = date.getMonth() + 1;
                let day = date.getDate();
                
                if (type === 'start') {
                    year = year - 60;
                } else if (type === 'end') {
                    year = year + 2;
                }
                month = month > 9 ? month : '0' + month;;
                day = day > 9 ? day : '0' + day;
                return `${year}-${month}-${day}`;
            }
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
        margin-top:24rpx ;
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
    padding-top: 100rpx;
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
