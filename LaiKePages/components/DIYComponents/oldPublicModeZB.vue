<template>
    <view class="isPublicModeZB"> 
        <template v-if="list && list.length>0">
            <view class="content_newTwo" :style="'background-image: url('+ new_home_bgm2 +')'">
                <view>
                    <span class='mytop'>推荐主播</span>
                    <view class="content_newFive_tilte2" @tap="_seeMore()">
                        {{language.home.more}}
                        <image :src="jiantou" lazy-load="true" style="width: 32rpx;height: 44rpx;"></image>
                    </view>
                </view>
                <view>
                    <view 
                        v-for="(item,index) in list" :key="index" 
                        v-if="index < 4"  
                        class="content_newTwo_content" 
                        @tap="_seeLive(item)">
                        
                            <view>
                                <image :src="LaiKeTuiCommon.getFastImg(item.headimgurl, 124, 124)" mode="scaleToFill"
                                    lazy-load="true" @error="setDefaultImage(index)"/>
                            </view>
                            <view class="content_newTwo_content_name">{{item.user_name}}</view> 
                            <view class="mydian">
                                <div class="live_icon">
                                    <!-- <img :src="live" alt=""> -->
                                    <i class="one"></i>
                                    <i class="two"></i>
                                    <i class="three"></i>
                                </div>
                            </view>
                    </view>
                </view>
            </view>
        </template>
    </view>
</template>

<script>
    export default{
        
        data(){
            return{
                new_home_huiyuan1:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_huiyuan1.png',
                new_home_huiyuan2:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_huiyuan2.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                zaibo:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zaibo.png',
                new_home_bgm2: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_bgm2.png',
                list:[],
            }
        },
        mounted() {
            this.getLive()
        },
        methods:{
            setDefaultImage(index){ 
                this.list[index].headimgurl= this.ErrorImg
            },
            //获取直播
            getLive() {
                let data = { 
                     api:"plugin.living.App.queryLiving",
                };
                this.$req
                    .post({
                        data,
                    })
                    .then((res) => {
                        if (res.code == 200) {
                            this.list = res.data.list;
                        }
                    });
            },
            //进入推荐直播
            _seeMore(){
                console.log(11111111111)
                uni.navigateTo({
                    url: "/pagesD/liveStreaming/liveRecommended",
                });
            },
            //查看直播
            _seeLive(item){
               let data = {
                   api: 'plugin.living.App.addLivingBrowse',
                   roomId: item.roomId,
               }
                this.$req.post({
                   data
                }).then(res => {
                	if (res.code == 200) {
                		uni.reLaunch({
                		    url: '/pagesD/liveStreaming/liveDetail?id=' + item.roomId +'&aid='+res.data.id
                            
                		});
                	}else {
                		uni.showToast({
                			title: res.message,
                			duration: 1500,
                			icon: "none",
                		});
                		
                	}
                })
                
            },
            
        }
    }
</script>

<style lang="less" scoped> 
@import url("@/laike.less");
    .isPublicModeZB{
        .content_newTwo{
            width: 686rpx;
            height: auto;
            background-color: #ffffff;
            border-radius: 26rpx;
            margin: 32rpx auto;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 24rpx;
            box-sizing: border-box;
            background-size: cover;
            
            >view{}
            >view:first-child{
                width: 636rpx;height:42rpx;display:flex;justify-content: space-between;
                .mytop{
                    font-size: 32rpx;
                    color: #333333;
                    font-weight: bold;
                }
                .content_newFive_tilte2{
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    
                    font-size: 24rpx;
                    color: #999999;
                    line-height: 32rpx;
                }
            }
            >view:last-child{
                width: 100%;
                display: flex;
                flex-direction: row;
                // >view:nth-child(2){margin-left:20rpx;margin-right:20rpx;}
                .content_newTwo_content{
                    position: relative;
                    display: flex;
                    flex-direction: column;
                    margin-top: 24rpx;
                    margin-right: 40rpx;
                    
                    >view:nth-child(1){
                        width: 124rpx;
                        height: 124rpx;
                        border-radius: 50%;
                        overflow: hidden;
                        border: 1px solid rgba(250, 81, 81, 0.6);
                        >img{width: 112rpx ; height: 112rpx;border-radius: 50%;margin: 6rpx;}
                    }
                    >view:nth-child(2){
                        font-size: 24rpx;
                        font-weight: 500;
                        color: rgba(0,0,0,0.85);
                        line-height: 34rpx;
                        margin: 8rpx 0 0 0;
                        text-align: center;
                        // 超出隐藏
                        width: 124rpx;
                        height: auto;
                        display:-webkit-box;
                        overflow: hidden; /*超出隐藏*/
                        text-overflow: ellipsis;/*隐藏后添加省略号*/
                        -webkit-box-orient:vertical; 
                        -webkit-line-clamp:1; //想显示多少行
                    }
                    .mydian{
                        width: 28rpx;
                        height: 28rpx;
                        border-radius: 50%;
                        position: absolute;
                        // background-color: red;
                        right: 0;
                        top: 96rpx;
                        img{
                            width: 28rpx;
                            height: 28rpx;
                            border-radius: 50%;
                        }
                    }
                }
            }
        }
        .live_icon {
            flex: none;
            position: absolute;
            width: 32rpx;
            height: 32rpx;
            bottom: 0 !important;
            right: 0 !important;
            border-radius: 50%;
            background-color: #fa5151;
            box-sizing: border-box;
            overflow: hidden;
            i {
                width: 4rpx;
                height: 12rpx;
                background-color: #ffffff;
                position: absolute;
                bottom: 7rpx;
                border-radius: 1.5rpx;
                left: 0;
            }
            .one {
                left: 6rpx;
                width: 4rpx; /* 初始宽度 */
                height: 12rpx; /* 高度 */
                background-color: #ffffff; /* 背景颜色 */
                animation: stretch 0.8s infinite alternate; /* 应用动画 */
        
                @keyframes stretch {
                    0% {
                        height: 12rpx; /* 动画开始时的宽度 */
                    }
                    100% {
                        height: 18rpx; /* 动画结束时的宽度 */
                    }
                }
            }
            .two {
                left: 14rpx;
                width: 4rpx; /* 初始宽度 */
                height: 12rpx; /* 高度 */
                background-color: #ffffff; /* 背景颜色 */
                animation: stretch 0.2s infinite alternate; /* 应用动画 */
        
                @keyframes stretch {
                    0% {
                        height: 12rpx; /* 动画开始时的宽度 */
                    }
                    100% {
                        height: 18rpx; /* 动画结束时的宽度 */
                    }
                }
            }
            .three {
                left: 22rpx;
                width: 4rpx; /* 初始宽度 */
                height: 12rpx; /* 高度 */
                background-color: #ffffff; /* 背景颜色 */
                animation: stretch 0.6s infinite alternate; /* 应用动画 */
        
                @keyframes stretch {
                    0% {
                        height: 12rpx; /* 动画开始时的宽度 */
                    }
                    100% {
                        height: 18rpx; /* 动画结束时的宽度 */
                    }
                }
            }
        
            // img {
            //     width: 28rpx;
            //     height: 28rpx;
            //     vertical-align: middle;
            // }
        }
    }
</style>
