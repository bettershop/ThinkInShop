<template>
    <view class="container">
        <heads :title="title"></heads>
		<template v-if="time">
			<view class="timeBox" >
			    {{language.substration.activity_time}}：{{time}}
			</view>
			<view style="height: 80rpx;"></view>
		</template>
        
        <view class="content">
            <view class="list" v-for="item,index of list" :key="index">
                <image :src="item.imgurl" mode=""></image>
                
                <view class="list_right">
                    <view class="list_right_title">
                        {{item.product_title}}
                    </view>
                    
                    <view class="icon">{{title}}</view>
                    
                    <view class="list_right_bottom">
                        <view class="price">
                            <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
                            {{LaiKeTuiCommon.formatPrice(item.price)}}
                        </view>
                        <view class="btn" @tap="toUrl('/pagesC/goods/goodsDetailed?toback=true&pro_id='+item.id)">{{language.substration.snap_up}}</view>
                    </view>
                </view>
            </view>
        </view>
        
        <uni-load-more v-if="list.length>8" :loadingType="loadingType"></uni-load-more>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: '',
            id: '',
            list: [],
            page: 1,
            loadingType: 0,
            
            time: ''
        };
    },
    onLoad(option) {
        this.title = option.title
        this.id = option.id
    },
    onShow() {
        uni.pageScrollTo({
            scrollTop: 0,
            duration: 10
        })
        this.page = 1
        uni.showLoading({
            title: this.language.showLoading.loading,
            mask: true
        })
        this.axios()
    },
    // 下拉加载
    onReachBottom: function() {
        if(this.loadingType != 0){
            return
        }
        this.loadingType = 1
        
        this.page++
        this.axios()
    },
    methods: {
        toUrl(url){
            uni.navigateTo({
                url
            })
        },
        axios(){
            let data = {
                api: 'app.subtraction.index',
                id:this.id, // 满减ID
                page:this.page, // 加载次数
                pagesize:10 // 每次多少条
            }
            
            if(this.page == 1){
                this.list = []
            }
            
            this.$req.post({data}).then(res=>{
                uni.hideLoading()
                if(res.code == 200){
                    res.data.starttime = res.data.starttime.replace(/-/g,'.')
                    res.data.endtime = res.data.endtime.replace(/-/g,'.')
                    this.time = res.data.starttime.substr(0,10) + ' - ' + res.data.endtime.substr(0,10)
                    this.list.push(...res.data.list)
                    
                    if(res.data.list.length>0){
                        this.loadingType = 0
                    }else{
                        this.loadingType = 2
                    }
                }else{
                    uni.showToast({
                        title: res.message,
                        icon: 'none'
                    })
                }
            }).catch(error=>{
                uni.hideLoading()
            })
        }
    },
};
</script>

<style lang="less">
@import url("@/laike.less");
@import url('../../static/css/home/substration.less');
</style>
