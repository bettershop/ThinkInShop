<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title"></heads>
        <toload v-if="!load" :load="load"></toload>
        <view v-else class="content">
            <view class="title">
                {{ pro.name }}
                <text class="icon1" v-if="pro.status == 1">{{language.activities_details.notBegun}}</text>
                <text class="icon2" v-if="pro.status == 2">{{language.activities_details.ongoing}}</text>
                <text class="icon3" v-if="pro.status == 3">{{language.activities_details.finished}}</text>
            </view>
        
            <image class="image" :src="pro.image" mode="aspectFit"></image>
        
            <view class="list">
                <view class="list_left" :style="{width: languageType=='en_US'?'190rpx':''}">{{language.activities_details.activityType}}</view>
                <view class="list_right">{{ pro.type == 'pt' ? language.activities_details.proPt : language.activities_details.proMs }}</view>
            </view>
        
            <view class="list">
                <view class="list_left" :style="{width: languageType=='en_US'?'190rpx':''}">{{language.activities_details.activityTime}}</view>
                <view class="list_right">
                    {{ pro.starttime }}
                    <br />
                    <text class="text">{{language.activities_details.to}}</text>
                    {{ pro.endtime }}
                </view>
            </view>
        
            <view class="list">
                <view class="list_left" :style="{width: languageType=='en_US'?'190rpx':''}">{{language.activities_details.activityRules}}</view>
                <view class="list_right" v-if="pro.type == 'pt'">
                    {{ pro.group_man }} {{language.activities_details.ptRules[0]}} {{ pro.group_time }} {{language.activities_details.ptRules[1]}} {{ pro.group_kai_num }} {{language.activities_details.ptRules[2]}} {{ pro.group_can_num }} {{language.activities_details.ptRules[3]}} {{
                        pro.group_can_again == 1 ? language.activities_details.ptRules[4] : language.activities_details.ptRules[5]
                    }}
                </view>
                <view class="list_right wrap" v-else-if="pro.type == 'ms'">
                   {{ pro.free_freight == 1 ? language.activities_details.msRules[0] : language.activities_details.msRules[1] }} {{language.activities_details.msRules[2]}} {{ pro.buy_time1 }} {{language.activities_details.msRules[3]}} {{ pro.default_num }} {{language.activities_details.msRules[4]}}
                </view>
            </view>
        
            <view class="list">
                <view class="list_left" :style="{width: languageType=='en_US'?'190rpx':''}">{{language.activities_details.registrationTime}}</view>
                <view class="list_right">
                    {{ pro.join_starttime }}
                    <br />
                    <text class="text">{{language.activities_details.to}}</text>
                    {{ pro.join_endtime }}
                </view>
            </view>
        </view>
        
        <view style="height: 120rpx;"></view>
        
        <view class="btnBox" v-if="pro.status == 1">
            <view class="btn" @tap="setting">{{language.activities_details.setGoods}}</view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: '活动详情',
            
            pro: {},
            id: '',
            type: '',
            load: false
        };
    },
    onLoad(option) {
        this.id = option.id;
        this.type = option.type;
    },
    onShow() {
		if (this.type == 'pt') {
		    this.title = this.language.activities_details.title_pt;
		} else if (this.type == 'ms') {
		    this.title = this.language.activities_details.title_ms;
		}else{
            this.title = this.language.activities_details.title
        }
		
        this.isLogin(()=>{
			this.axios();
		})
    },
    methods: {
        setting(){
            //获取现在时间的毫秒数
            let now = new Date().getTime()
            let start = new Date(this.pro.join_starttime).getTime()
            if (now < start) {
                uni.showToast({
                    title: this.language.activities_details.timeNoStart,
                    icon: 'none'
                })
                return
            }
            
            //获取报名截止毫秒数
            let end = new Date(this.pro.join_endtime).getTime()
            
            if(now >= end){
                uni.showToast({
                    title: this.language.activities_details.timeClose,
                    icon: 'none'
                })
                return
            }
            this.navito('/pagesA/myStore/activities_add?id=' + this.pro.id + '&type=' + this.pro.type + '&platform_activities_id=' + this.id)
            
        },
        changeLoginStatus(){
			this.axios();
		},
        navito(url) {
            uni.navigateTo({
                url
            });
        },
        axios() {
            let data = {
                api: 'app.platform_activities.activity_details',
                
                id: this.id
            };
            this.$req.post({ data }).then(res => {
                let { code, data, message } = res;
                this.load = true;
                if (code == 200) {
                    this.pro = data[0];
                } else {
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    });
                }
            });
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
.container {
    background-color: #ffffff;
    min-height: 100vh;

    .content {
        padding: 40rpx 30rpx;
        margin-bottom: 118rpx;

        .title {
            color: #020202;
            font-size: 36rpx;
            line-height: 41rpx;
            margin-bottom: 30rpx;

            .icon1,
            .icon2,
            .icon3 {
                display: inline-flex;
                justify-content: center;
                align-items: center;
                min-width: 82rpx;
                height: 36rpx;
                border-radius: 6rpx;
                font-size: 22rpx;
                margin-left: 14rpx;
                padding: 0 10rpx;
                box-sizing: border-box;
            }

            .icon1 {
                color: #ff6600;
                border: 1px solid #ff6600;
            }

            .icon2 {
                color: #ff0000;
                border: 1px solid rgba(255, 0, 0, 1);
            }

            .icon3 {
                color: #999999;
                border: 1px solid #999999;
            }
        }

        .image {
            width: 100%;
        }

        .list {
            display: flex;
            margin-top: 30rpx;

            &_left {
                font-size: 28rpx;
                color: #666666;
            }

            &_right {
                flex: 1;
                font-size: 28rpx;
                color: #020202;

                .text {
                    color: #999999;
                    margin-right: 10rpx;
                }
            }
        }
    }

    .btnBox {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        height: 118rpx;
        padding: 14rpx 30rpx;
        box-sizing: border-box;
        box-shadow: 0px -2rpx 24rpx 0px rgba(2, 2, 2, 0.24);

        .btn {
			.solidBtn();
            display: flex;
            justify-content: center;
            align-items: center;
            border-radius: 8rpx;
            font-size: 30rpx;
            width: 100%;
            height: 100%;
        }
    }
}
    .wrap{
        word-wrap:break-word;
        word-break:break-all;
    }
</style>
