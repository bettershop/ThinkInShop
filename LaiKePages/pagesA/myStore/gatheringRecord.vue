<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        <heads :title="title" :bgColor="bgColor" titleColor="#333333" ishead_w="2"></heads>
        <view class="ul" v-for="(item, index) in list" :key="index">
            <view class="ul_top" @tap="open_time()">
                <span>{{item.date}}</span>
                <image :src="bottom_icon"></image>
            </view>
            <view class="li" v-for="(item1, index1) in item.num" :key="index1">
                <view class="li_left">
                    <view class="text">{{item1.sourceNickname}}</view>
                    <view class="descTime">2023 {{item1.time}}</view>
                </view>
                <view class="li_right">
                    <view class="text">{{item1.money}}</view>
                   
                </view>
            </view>
        </view>
        <date ref="starttimePicker" :themeColor="themeColor" :urseTime="start_time" :is_days="true" @onConfirm="onConfirm1"></date>
        <view class="noFindDiv" v-if="list&&list.length==0">
            <div><img class="noFindImg" :src="noOrder" /></div>
            <span class="noFindText">{{language.gatheringRecord.zwsj}}～</span>
        </view>
        <uni-load-more v-if="list.length>9" :loadingType="loadingType"></uni-load-more>
    </view>
</template>

<script>
    import date from '@/components/date-time-picker.vue'
    export default {
        data() {
            return {
                title: '',
                loadingType: 0,
                list: [{
                        "date": "2023-08",
                        "num": [{
                            "sourceNickname": "o-OWK5UenRdfOyrrXjXJO2qb7h5Q",
                            "money": "+0.01",
                            "time": "18:20:48",
                            "status": "1",
                            "text": "已到账"
                        }, {
                            "sourceNickname": "o-OWK5UenRdfOyrrXjXJO2qb7h5Q",
                            "money": "+0.01",
                            "time": "18:20:48",
                            "status": "1",
                            "text": "已到账"
                        }]
                    },
                    {
                        "date": "2023-09",
                        "num": [{
                            "sourceNickname": "o-OWK5UenRdfOyrrXjXJO2qb7h5Q",
                            "money": "+0.01",
                            "time": "18:20:48",
                            "status": "1",
                            "text": "已到账"
                        }]
                    }
                ],
                page: 1,
                pagesize: 10,
                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/noOrder.png',
                bottom_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/bottom_icon.png',
                themeColor: '#FA5151',
                start_time: '',//收款日期筛选
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
            };
        },
        onLoad(option) {
            this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
            // this.axios()
        },
        onShow() {
            this.title=this.language.gatheringRecord.title
            this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
        },
        components: {
            date
        },
        onReachBottom() {
            if (this.loadingType != 0) {
                return
            }
            this.loadingType = 1;
            this.page++;
            this.axios();
        },
        methods: {
            open_time(){
                this.$refs.starttimePicker.show(this.start_time);
            },
            // 时间-确定
            onConfirm1(e) {
                // 年月
                var start = e[0].replace(/undefined/g, '00').substring(0, 7);                
                this.start_time = start 
            },
            axios() {
                this.$req
                    .post({
                        data: {
                            api: 'app.mch.collection_record', 
                            shop_id: this.shop_id,
                            page: this.page,
                            pagesize: this.pagesize
                        }
                    })
                    .then(res => {
                        let {
                            code,
                            data,
                            message
                        } = res
                        this.loadFlag = true;
                        uni.hideLoading();
                        if (code == 200) {
                            if (this.page == 1) {
                                this.list = []
                            }
                            if (data.list.length > 0) {
                                this.list.push(...data.list);
                                this.loadingType = 0
                            } else {
                                this.loadingType = 2
                            }
                        } else {
                            uni.showToast({
                                title: message,
                                duration: 1500,
                                icon: 'none'
                            });
                        }
                    });
            }
        }
    };
</script>

<style>
    page {
        background: #F4F5F6
    }
</style>
<style scoped lang="less">
@import url("@/laike.less");
    @import url('../../static/css/myStore/gatheringRecord.less');
</style>
