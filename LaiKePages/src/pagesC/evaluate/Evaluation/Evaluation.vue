<template>
	<view>
		<heads :title="language.evaluate.evaluate.pjgl" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
        <view class="pages">
            <div v-if="isData.length == 0"  style="height: 100vh;width: 100%;display: flex;align-items: center;">
                <div class="noFindDiv" style="width: 100%;padding-top: 178rpx;height: 100%;">
                    <div><img class="noFindImg" :src="noOrder" /></div>
                    <span class="noFindText">{{language.zdata.nhmyplo}}</span>
                </div>
            </div>
            <view v-else class="pages_box" v-for="(item,index) in isData" :key="index">
                <view class="pages_list" @click="nato(item)">
                    <view class="pages_list_left">
                        <image :src="item.headimgurl" style="width: 64rpx;height: 64rpx;border-radius: 52rpx;"></image>
                    </view>
                    <view class="pages_list_cent">
                        <view class="pages_list_cent_name">{{item.user_name}}
                            <view class="dian" v-if="item.is_look ==0"></view>
                         </view>
                        <view class="pages_list_cent_txt" style="margin: 16rpx 0 8rpx 0;">{{item.content}}</view>
                        <view class="pages_list_cent_time">{{item.add_time}}</view>
                        
                    </view>
                    <view class="pages_list_right" style="display: flex;">
                        <image :src="item.commentImgList[0]" style="width: 128rpx;height: 128rpx;border-radius: 16rpx;"></image>
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
				bgColor: [{
				        item: '#ffffff'
				    },
				    {
				        item: '#ffffff'
				    }
				],
                pageNo: 1,
                isData: [],
                loadingType: 0,
                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/noOrder.png',
			}
		},
        onLoad(option) {
            this._axios()
        },
        onShow() {
            this.pageNo = 1
            this._axios()
        },
        onReachBottom() {
            if (this.loadingType == 1 || this.loadingType == 2) {
                return false
            }
            this.loadingType = 1;
            this.pageNo++;
            this._axios()
        },
		methods: {
			nato(item){
                uni.navigateTo({
                    url:'/pagesC/evaluate/Evaluation/Evaluation_detail?id='+( item.commentsId || item.id) + '&pid=' + item.pid
                })
            },
            _axios(){
                let data = {
                    api: 'plugin.integral.AppIntegral.getCommentsInfo',
                    page: this.pageNo,
                    pageSize: 10,
                }
                this.$req.post({data}).then(async res => {
                    if(res.code ==200){
                        if (this.pageNo == 1) {
                            this.isData = res.data.list
                        } else {
                            this.isData.push(...res.data.list)
                        }
                        if (res.data.list.length > 0) {
                            this.loadingType = 0
                        } else {
                            this.loadingType = 2
                        }
                    } else {
                    }
                })
            },
		}
	}
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    .pages_list_right{
        margin-left: 66rpx;
    }
    .pages_list_cent_time{
        margin-top: 8rpx;
        font-size: 24rpx;
         
        font-weight: normal;
        color: #666666;
    }
    .pages_list_cent_txt{
        width: 414rpx;
        font-size: 24rpx;
         
        font-weight: normal;
        color: #333333;
    }
    .pages_list_cent_name{
        font-size: 28rpx;
        display: flex;
        align-items: center;
        font-weight: 500;
        color: #333333;
        .dian{ 
           margin-left: 10rpx; 
           border: 8rpx solid red;
           border-radius: 50%;
        }
    }
    .pages_list_cent{
        display: flex;
        flex-direction: column;
        margin-left: 16rpx;
    }
    .pages_list_left{
        width: 64rpx;
        height: 64rpx;
    }
    .pages_list{
        display: flex;
        width: 100%;
        border-radius: 24rpx;
        background-color: #fff;
        padding: 32rpx;
        margin-top: 24rpx;
    }
.pages{
    width: 100%;
}
.pages_box{
    width: 100%;
    display: flex;
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
</style>
