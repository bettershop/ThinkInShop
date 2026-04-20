<template>
    <view>
        <heads :title="language.evaluation_subsidiary.title" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
        <view class="pages">
           
            <view class="pages_box" style="margin-top: 16rpx;">
                
            </view>
            <view class="pages_box">
                
                <view class="pages_list" v-for="(item,index) in isData" :key="item.id">
                    <view class="pages_list_left">
                        <image :src="item.headimgurl" style="width: 64rpx;height: 64rpx;border-radius: 52rpx;"></image>
                    </view>
                    <view class="pages_list_cent">
                        <view class="pages_list_cent_a">
                            <view class="pages_list_cent_name">{{item.replyName}}</view>
                            <view class="pages_list_cent_time">{{item.add_time}}</view>
                        </view>
                        <view class="pages_list_cent_txt">{{item.content}}</view>
                    </view>
                </view>
                
            </view>
        </view>
    </view>
</template>

<script>
    export default {
        data() {
            return {
                starOffImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xing12x.png',
                starOnImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xing22x.png',
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                page: 1,
                cid: '',
                item: {},
                isData: [],
            }
        },
        onLoad(option) {
            if(option.id){
                this.cid = option.id
            }
            let str = decodeURIComponent(option.item)
            if(str != "undefined"){
                this.item = JSON.parse(str);
            }
            this._axios()
        },
        methods: {
            _axios(){
                let data = {
                    api: 'plugin.presell.AppPreSell.getCommentsDetailInfoById',
                    cid: this.cid,
                    page: this.page,
                    pageSize: 10,
                }
                this.$req.post({data}).then(async res => {
                    if(res.code ==200){
                        this.isData = res.data.list
                    } else {
                    }
                })
            },
        }
    }
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
    @import url('@/laike.less');
    .list_top{
        border-radius:  0 0 24rpx 24rpx!important;
    }
    .pages_box_head{
        border-radius: 24rpx 24rpx 0 0;
        margin-bottom: -24rpx;
        padding: 24rpx;
        margin-top: 36rpx;
        background: #fff;

    }
    .pages {
        width: 100%;
        display: flex;
        flex-direction: column;
        // margin-top: 24rpx;
    }

    .pages_list_right {
        margin-left: 66rpx;
    }
    .pages_list_cent_a{
        display: flex;
        width: 100%;
        height: 64rpx;
        justify-content: space-between;
        align-items: center;
    }
    .pages_list_cent_time {
        margin-top: 8rpx;
        font-size: 24rpx;
         
        font-weight: normal;
        color: #666666;
    }

    .pages_list_cent_txt {
        width: 606rpx;
        margin-top: 26rpx;
        font-size: 24rpx;
         
        font-weight: normal;
        color: #333333;
    }

    .pages_list_cent_name {
        font-size: 28rpx;
         
        font-weight: 500;
        color: #333333;
    }

    .pages_list_cent {
        display: flex;
        flex-direction: column;
        justify-content: center;
        width: 100%;
        // justify-content: space-between;
        // align-items: center;
        margin-left: 16rpx;
    }

    .pages_list_left {
        width: 64rpx;
        height: 64rpx;
    }

    .pages_list {
        display: flex;
        // width: 100%;
        border-radius: 24rpx;
        background-color: #fff;
        padding: 32rpx;
        margin-bottom: 8rpx;
    }

    .pages {
        width: 100%;
    }

    .pages_box {
        width: 100%;
        // display: flex;
    }

    .noFindDiv {
        width: 100%;
        padding-top: 430rpx;
        height: 100%;
        background-color: #F4F5F6;

        .noFindText {
            color: #888888;
        }

        .noFindImg {
            width: 750rpx;
            height: 394rpx;
        }
    }
    .starall_title{
        display: flex;
        justify-content: center;
        text-align: center;
        height: 42rpx;
        align-items: flex-start;
    }
    .starall_box{
        // margin-left: 80rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        // margin-bottom: 16rpx;
    }
    .starall img {
        width: 32rpx;
        height: 32rpx;
        margin-right: 12rpx;
    }
    .starall_box span{
        color: #666666;
        
        font-size: 24rpx;
    }
</style>
