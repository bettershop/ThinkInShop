<template>
	<view class="pages" @click.stop="closeclick">
		<scroll-view scroll-y="true" class="pages_scroll" :style="{height: height_max}">
            <view class="pages_scroll_box" v-for="(item,index) in itemuser" :key="index" :style="typeindex==index?background:background1" @click="typeclick(item,index)">
                <view class="page_scroll_box_tx" :style="item.is_online==0?border:border1">
                    <image :src="item.headimgurl" class="pages_scroll_box_img"></image>
                    <view class="redpoint" v-if="item.no_read_num>0"></view>
                </view>
                <view class="page_scroll_box_name">{{item.user_name}}</view>
            </view>
        </scroll-view>
	</view>
</template>

<script>
	export default {
        props:{
            "height_max":{
                type:String,
                require:true
            },
            //用户列表
            "userlist": {
                type: Array,
                default: [],
            },
        },
        watch:{
            userlist(){
                this.itemuser = this.userlist
            }
        },
		data() {
			return {
                border: 'border: 3rpx solid #D8D8D8',
                border1:'border: 3rpx solid #FA5151;',
                background:'background-color: #f4f5f6;',
                background1:'background-color: #FFFFFF;',
                typeindex:0,
                itemuser:[],
			}
		},
		methods: {
			typeclick(l,e){
                this.typeindex = e
                this.$emit('change', l,e)
            },
            closeclick() {
                this.$emit('is_expressionboxa', false)
                this.$emit('is_photoboxa', false)
            },
		}
	}
</script>

<style scoped>
    .pages{
        width: 236rpx;

    }
    .redpoint{
        width: 24rpx;
        height: 24rpx;
        background: #FA5151;
        border-radius: 52rpx;
        border: 3rpx solid #FFFFFF;
        position: absolute;
        bottom: 4rpx;
        right: 0rpx;
    }
    .pages_scroll{
        background-color: #ffffff;
        width: 236rpx;
        display: flex;
        flex-direction: column;
    }
    .pages_scroll_box{
        width: 236rpx;
        height: 104rpx;
        display: flex;
        align-items: center;
        background-color: #f4f5f6;
    }
    .pages_scroll_box_img{
        width: 76rpx;
        height: 76rpx;
        border-radius: 52rpx;
    }
    .page_scroll_box_tx{
        width: 80rpx;
        height: 80rpx;
        border-radius: 52rpx;
        border: 3rpx solid #D8D8D8;
        display: flex;
        justify-content: center;
        align-items: center;
        // margin-left: 22rpx;
        position: relative;
        margin: 12rpx 22rpx;
    }
    .page_scroll_box_name{
        margin-left: 12rpx;
        font-size: 28rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
         
        font-weight: 400;
        color: #000000;
    }
</style>
