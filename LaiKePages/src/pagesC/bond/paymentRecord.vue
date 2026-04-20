<template>
    <view class='container'>
        <lktauthorize ref="lktAuthorizeComp" ></lktauthorize>
        <heads :title='language.bond.payText.jnjl' :bgColor="bgColor" titleColor="#333333" ishead_w="2"></heads>
        <view class="listContent">
            <view class="listBox" v-if="recordList&&recordList.length>0">
                <view class="boxItem" v-for="item in recordList">
                    <view class="itemTop">
                        <view class="name">{{item.event}}</view>
                        <view class="price">{{item.type==40?'+':'-'}}{{item.money}}</view>
                    </view>
                    <view class="itemBottom">{{item.add_date}}</view>
                </view>
            </view>
           <div class="noFindDiv" v-else>
               <div>
                   <img :src="noRecord" class="noFindImg">
               </div>
               <span class="noFindText">{{language.bond.payText.nhzwjl}}</span>
           </div>
        </view>
    </view>
</template>

<script>
    export default{
        data(){
            return{
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                recordList:[],
                noRecord: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noPro.png',
            }
        },
        onShow () {
            this.isLogin(()=>{
                this._axios()
            })
        },
        methods:{
            _axios() {
                var me = this
                var data = {
                    api:"mch.App.Promise.PromiseList",
                }
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                        this.recordList=res.data.list
                    }else{
                        uni.showToast({
                            title:res.message,
                            duration: 1000,
                            icon: 'none'
                        })
                    }
                })
            }
        }
    }
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style  lang="less" scoped>
    @import url("@/laike.less");
    .boxItem{
        background: #FFFFFF;
        border-radius:24rpx;
        padding: 32rpx;
        margin-bottom: 16rpx;
        &:nth-child(1){
            border-radius: 0 0 24rpx 24rpx;
        }
        .itemTop{
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 32rpx;
            
            font-weight: 500;
            margin-bottom: 16rpx;
            .name{ 
                color: #333333;
            }
            .price{
                color: #FA5151;
                font-weight: 500;
            }
        }
        .itemBottom{
            font-size: 28rpx;
            
            font-weight: 400;
            color: #999999;
        }
    }
    .noFindDiv {
        width: 100% !important;
        padding-top: 278rpx !important;
        height: 100% !important;
        .noFindImg{
            width: 750rpx;
            /* #ifdef MP */
                height: 404rpx;
            /* #endif */
        }
    }
</style>
