<template>
    <view class="isPublicModeZB" >
        <template v-if="list && list.length>0">  
            <view class="content_newTwo" :style="styleText" >
                <view class="home_title"> 
                   <view class='mytop title-view' :style="{color: txtColor}">{{title}}</view>
                    <image class="title-image" :src="LaiKeTuiCommon.getFastImg(titleBg, 240, 40)" lazy-load="true"></image> 
                    
                    <view class="content_newFive_tilte2 more" @tap="_seeMore()">
                        {{language.home.more}} 
                        <image :src="jiantou" lazy-load="true"></image>
                    </view>
                </view>
                <view>
                    <view 
                        v-for="(item,index) in list" :key="index"  
                         :style="{ marginRight: pxToRpxNum(listRight) + 'rpx'}"
                        class="content_newTwo_content" 
                        @tap="_seeLive(item)"> 
                            <view>
                                <image :src="LaiKeTuiCommon.getFastImg(item.headimgurl, 124, 124)" mode="scaleToFill"
                                    lazy-load="true" @error="setDefaultImage(index)"/>
                            </view>
                            <view class="content_newTwo_content_name">{{item.user_name}}</view>
                            <view class="mydian">
                                <div class="live_icon"> 
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
        props:{
          
            dataConfig:{
                type:Object,
                default: () => { }
            }
            // --- 分割线 ---
        },
        data(){
            return{
                new_home_huiyuan1:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_huiyuan1.png',
                new_home_huiyuan2:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_huiyuan2.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                zaibo:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zaibo.png',
                new_home_bgm2: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_bgm2.png',
                list:[],
                styleText:'',
                title: '推荐主播',
                txtColor: '',
                titleBg: '',
                lrConfig: '',
                mbConfig: '',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou2x.png',
                default_img: "../../static/img/default-img.png",
                bgColor: [{
                		item: 'transparent'
                	},
                	{
                		item: 'transparent'
                	}
                ],
                listRight:0,
            }
        },
        
        created() {
            let num = ''
            if( this.dataConfig && Object.keys( this.dataConfig).length>0)
            { 
                this.txtColor = this.dataConfig.titleTxtColor.color[0].item
                this.title = this.dataConfig.titleConfig.value 
                this.mbConfig = this.dataConfig.mbConfig.val
                this.titleBg = this.dataConfig.imgConfig.url 
                this.bgColor = this.dataConfig.bgColor.color
                this.styleText = `margin-top: ${this.mbConfig}px;background: linear-gradient(90deg,${this.bgColor[0].item} 0%,${this.bgColor[1].item} 100%)`
                num = this.dataConfig.numberConfig.val
                 this.listRight =this.dataConfig.lrConfig.val;
            }else{
                this.styleText=`background-image: url(${this.new_home_bgm2})`
            }
              this.getLive(num)
             
        },
        watch: {
            dataConfig:{
                handler(newVal,lodVla){
                    console.log(newVal)
                    if(newVal && Object.keys(newVal).length>0){ 
                        console.log(this.dataConfig)
                           this.bgColor = this.dataConfig.bgColor.color
                        this.txtColor = newVal.titleTxtColor.color[0].item
                        this.title = newVal.titleConfig.value 
                        this.mbConfig = newVal.mbConfig.val
                        this.titleBg = newVal.imgConfig.url 
                         this.listRight =newVal.lrConfig.val;
                        this.styleText = `margin-top: ${this.mbConfig}px;background: linear-gradient(90deg,${this.bgColor[0].item} 0%,${this.bgColor[1].item} 100%)` 
                    }else{
                        this.styleText=`background-image: url(${this.new_home_bgm2})`
                    }
                },
                deep:true, 
                 
            }
            
        },
        mounted() {
          
        },
        methods:{
            setDefaultImage(index){ 
                this.list[index].headimgurl= this.ErrorImg
            },
            //获取直播
            getLive(limitNum = 4) { 
                let data = {
                    api:"plugin.living.App.queryLiving",
                     limit_num:limitNum,
                }
                this.$req
                    .post({
                        data
                    })
                    .then((res) => {
                        if (res.code == 200) {
                            this.list = res.data.list;
                        }
                    });
            },
            //进入推荐直播
            _seeMore(){ 
                uni.navigateTo({
                    url: "/pagesD/liveStreaming/liveRecommended",
                });
            },
            //查看直播
            _seeLive(item){
                // 查看直播
                let data = { 
                	api: 'plugin.living.App.addLivingBrowse',
                	roomId: item.roomId,
                };
                
                this.$req.post(
                	{data}
                ).then(res => {
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
                        // 未登录状态
                		if(res.code == 203){
                            setTimeout(()=>{
                                uni.reLaunch({
                                    url: '/pagesD/login/newLogin'
                                });
                            },1500)
                        }
                	}
                })
                
            },
            
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
.home_title {
        width: 100% !important;
        position: relative;
        z-index: 10;
        margin: 0rpx 30rpx 22rpx;
        display: flex;
        justify-content: space-between;
      
    }
  
	.home_title>.title-view {
		font-size: 34rpx;
		line-height: 34rpx;
		color: #014343;
		font-weight: bold;
	}
    
    .home_title>.title-image {
    	position: absolute;
    	width: 111rpx;
    	height: 19rpx;
    	top: 20rpx;
    	left: 24rpx;
    	z-index: -1;
    }
    .more {
    		display: flex;
    		align-items: center; 
    		font-size: 26rpx !important;
    		margin-left: auto;
    		height: 36rpx;
    	}
    
    	.more image {
    		width: 12rpx;
    		height: 22rpx;
    		margin-left: 12rpx;
    	}
    
    
    .isPublicModeZB{
        .content_newTwo{
            // width: 686rpx;
            height: auto;
            background-color: #ffffff;
            // border-radius: 26rpx;
            // margin: 32rpx auto;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 32rpx;
            box-sizing: border-box;
            background-size: cover;
            padding-top: 12rpx;
            padding-bottom: 10rpx;
            >view{}
            >view:first-child{
                width: 636rpx;height:42rpx;display:flex;justify-content: space-between;
                .mytop{ 
                    font-size: 32rpx;
                    font-family: Source Han Sans, Source Han Sans;
                    font-weight: 500;
                    color: rgba(0,0,0,0.851);
                    line-height: 16px;
                    text-align: left;
                    font-style: normal;
                    text-transform: none;
                }
                .content_newFive_tilte2{
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    
                    font-size: 24rpx;
                    color: #999999;
                    // line-height: 32rpx;
                    
                    font-family: Source Han Sans, Source Han Sans;
                    font-weight: 500; 
                    line-height: 34rpx;
                }
            }
            >view:last-child{
                width: 100%;
                display: flex;
                flex-direction: row;
                overflow-y: overlay;
                background: #FFFFFF;
                border-radius: 24rpx;
                padding: 28rpx 0rpx 24rpx 0rpx; 
                .content_newTwo_content:first-child{
                     margin-left: 24rpx !important;
                }
                .content_newTwo_content{
                    position: relative;
                    display: flex;
                    flex-direction: column; 
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
                        font-family: Source Han Sans, Source Han Sans;
                        font-size: 24rpx;
                        font-weight: 500;
                        color: #333333;
                        line-height: 1;
                        margin-top: 22rpx;  
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
