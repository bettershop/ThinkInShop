<template>
    <div class="container">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        <heads :title="language.expressageb.title" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <template  v-if="orderType != 'supplier' ||  list.list && list.list.length >1" >
            <div class="wl_content" :class="index==0?'wl_content_one':''" v-for="(item, index) in list.list" :key="index" @tap="_goEx(index)">
                <div class="wx_title">
                    <div  class="wx_title_d">
                        <img class="wl_img" :src="wl_img" />
                        <text v-if='item.shop_type == 1'>{{language.expressageb.logistics}}</text>
                        <text v-if='item.shop_type == 2'>{{language.expressageb.logistics1}}</text>
                        <text v-if='item.shop_type == 3'>{{language.expressageb.logistics2}}</text>
                        ：{{ item.courier_num }}
                    </div>
                    <img :src="jiantou" class='arrow' />
                </div>
                <scroll-view class="scroll_view" scroll-x>
                    <div class="scroll_view_data">
                        <div v-for="(items, indx) in item.pro_list" :key="indx">
                            <span class="scroll_view_num" v-if="items.num > 1">{{ items.num }}</span>
                            <img class="img" :src="items.img" />
                        </div>
                    </div>
                </scroll-view>
            </div>
        </template>
       
        <template v-if=' orderType == "supplier" && list.list && list.list.length == 1'>  
            <!-- 供应商订单 查看物流 --> 
           <view class="wl_content1"  v-for="(item, index) in list.list" :key="index"> 
               <view class="item_box wlinfo">
                   <view class="wx_title expressDelivery">
                       <view  class="wx_title_d">  
                            快递公司
                       </view> 
                       <view  class="wx_title_d">
                           {{ item.kuaidi_name}}
                       </view>
                   </view>
                   <view class="wx_title expressDelivery">
                       <view  class="wx_title_d">  
                           快递单号
                       </view> 
                       <view class="wx_title_d">
                          <text class="copy_text" @tap="textCopy(item.courier_num)">复制</text>
                          {{ item.courier_num }}
                       </view>
                   </view>
               </view>
               <scroll-view class="scroll_view" scroll-x v-if="item.list && item.list.length>0">
                   <view class="item_box context_box">
                        <lktTimeline :events='item.list'></lktTimeline> 
                   </view>
               </scroll-view>
               <div class='zwwl' v-else>
                   <img :src="zwwl" style='width: 750rpx;height: 460rpx;'>
                   {{language.expressage.logistics_tips}}~
               </div>
           </view>
        </template>
    </div>
</template>
<!-- liveStreaming/liveOrderExpressage -->
<script>
    import lktTimeline from "@/components/laiKetuiTimeline.vue";
export default {
    data() {
        return {
            title: '查看物流',
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            wl_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/wuliu2x.png',
             zwwl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zwwl.png',
            list: [],
            sNo: '',
            orderType:'',
            bgColor: [
                {
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
        };
    },
    components:{
        lktTimeline
    },
    onLoad(option) {
        let myid=''
        let data = null
        if (option.sNo) {
            this.sNo = option.sNo
            myid = option.sNo;
            
        }
        if(option.id){
            myid = option.id;
        }
        this.orderType = option.type || ''
        // 供应商查看物流信息
        if(option.type == 'supplier'){
            data= {
                api : 'supplier.AppMch.Orders.kuaidishow',
                orderno: myid
            } 
        }else{
            data = {
                api: 'app.order.logistics',
                id: myid,
                // sNo:this.sNo,
                o_source: option.o_source || 1,
                type: ''
            };
            
            if (this.source == 1) {
                data.type = 'pond';
            }
        }
           
        
        this.$req
            .post({ data })
            .then(res => {
                let { code, message, data } = res;
                uni.hideLoading();
                if (code == 200) {
                    this.list = data;
                } else {
                    uni.showToast({
                        title: message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            })
            .catch(error => {
                uni.showToast({
                    title: this.language.expressageb.Loading,
                    duration: 2000,
                    icon: 'none'
                });
            });
    },
    methods: {
        _goEx(index) {
            let data = this.list.list[index];
            uni.navigateTo({
                url: '/pagesC/expressage/expressage?list=' + JSON.stringify(data) + '&sNo=' +  this.sNo
            });
        },
        textCopy(text){
            uni.setClipboardData({
            	data: text,
            	success: function () {
            		uni.showToast({
            		    icon:'success',
                        title:this.language.toasts.order.copyOk
            		})
            	}
            });
        }
    }
};
</script>

<style scoped lang="less">

@import url("@/laike.less");
.zwwl {
    text-align: center;
    display: flex;
    flex-direction: column;
    padding-top: 224rpx;
    font-size: 28rpx;
    color: #333333;
    justify-content: center;
    align-items: center;
}

page{
     background: #f4f5f6;
}
.container {
    min-height: 100vh;
    background: #f4f5f6;
}

.wl_content {
    display: flex;
    flex-direction: column;
    padding: 30rpx;
    background: #ffffff;
    margin-bottom: 20rpx;
    border-radius:24rpx;    
}
.wl_content_one{
    border-radius:0 0 24rpx 24rpx;
}
.wl_img {
    width: 31rpx;
    height: 31rpx;
    margin-right: 14rpx;
}

.wx_title {
    display: flex;
    align-items: center;
    font-size: 28rpx;
    line-height: 28rpx;
    color: #020202;
    justify-content: space-between;
}
.wx_title_d{
    display: flex;
    .copy_text {
        color: #f55451;
        margin-right: 40rpx;
    }
}

.scroll_view {
    flex: 1;
}

.scroll_view_data {
    display: flex;
    padding-top: 32rpx;
}

.scroll_view_data > div {
    position: relative;
    width: 120rpx;
    height: 120rpx;
    margin-right: 30rpx;
    border: 1px solid #e6e6e6;
}

.scroll_view_data .img {
    width: 100%;
    height: 100%;
}

.scroll_view_num {
    position: absolute;
    border-radius: 50%;
    border: 1px solid #ff3333;
    font-size: 22rpx;
    color: #ff3333;
    width: 30rpx;
    height: 30rpx;
    line-height: 30rpx;
    text-align: center;
    right: -13rpx;
    top: -13rpx;
    background: #ffffff;
}
.expressDelivery{
    line-height: 60rpx;
    font-size: 30rpx;
}
// .wlinfo .expressDelivery:not(.expressDelivery:last-child){
//     border-bottom: 2rpx solid #eee;
// }
.wlinfo .expressDelivery.data-v-e9c4f968 {
  border-bottom: 2rpx solid #eee; /* 默认样式 */
}
.wlinfo .expressDelivery.data-v-e9c4f968:last-child {
  border-bottom: none; /* 最后一个子元素不应用底边框 */
}
.wl_content1 {
    display: flex;
    flex-direction: column; 
    margin-bottom: 20rpx; 
}
.item_box{
    background-color: #ffffff;
        padding: 30rpx 30rpx 10rpx 30rpx;
     border-radius:24rpx;   
} 
.context_box{
    margin-top: 20rpx;
}
</style>
