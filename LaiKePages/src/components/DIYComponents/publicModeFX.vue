<template>
    <view class="isPublicModeFX" :style="{'margin-top':pxToRpxNum(mbConfig)+'rpx',background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}"> 
        <template v-if="isData && isData.length">
            <view  class="content_newThree"> 
                <view class="home_title">
                    <view class='content_newThree_tilte1 title-view' :style="{color: txtColor}">{{title}}</view>
                    <image class="title-image" :src="LaiKeTuiCommon.getFastImg(titleBg, 240, 40)" lazy-load="true"></image>
                    <view class="more" @tap="_seeMore('/pagesA/distribution/fxProduct')">
                    	{{language.home.more}}
                    	<image :src="jiantou" lazy-load="true"></image>
                    </view> 
                </view>
                <view>
                    <view 
                        class="content_newThree_content"
                        :style="index>=1?'margin-top: 16rpx;':''"
                        v-for="(item, index) of isData" :key="index"  
                        @tap="_seeGoods(item.id, item.p_id)">
                            <view>
                                <image :src="LaiKeTuiCommon.getFastImg(item.imgurl, 176, 176)" mode="scaleToFill"
                                    lazy-load="true" @error="setDefaultImage(index)" style="width: 176rpx;height:176rpx;"/>
                            </view>
                            <view class="content_newThree_name">
                                <view class="overflowHide">{{item.product_title}}</view>
                                <view>{{language.home.share_make}} {{item.fx_price?item.fx_price.toFixed(2):'0.00'}}</view>
                                <view class="content_newThree_price">
                                    <view>
                                        {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}<span>{{LaiKeTuiCommon.formatPrice(item.price)}}</span><span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.price)}}</span>
                                    </view>
                                    <view>
                                        {{language.seckill.seckill.Panic[3]}}
                                    </view>
                                </view>
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
            //总数据
             
            dataConfig:{
                type:Object,
                default: () => { }
            }
            // --- 分割线 ---
        },
        data(){
            return{
                new_home_chajian1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_chajian1.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
                home_title_bg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_title_bg.png',
                isData: [], 
                title: 'GO! 分享赚',
                bgColor: [{
                		item: '#FFFFFF'
                	},
                	{
                		item: '#FFFFFF'
                	}
                ],
                txtColor: '',
                titleBg: '', 
                mbConfig: '',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou2x.png',
                default_img: "../../static/img/default-img.png",
            }
        },
        created() {
            let  num = ''
            if( this.dataConfig && Object.keys( this.dataConfig).length>0)
            { 
                this.txtColor = this.dataConfig.titleTxtColor.color[0].item
                this.title = this.dataConfig.titleConfig.value 
                this.mbConfig = this.dataConfig.mbConfig.val
                this.titleBg = this.dataConfig.imgConfig.url 
                this.bgColor = this.dataConfig.bgColor.color
                num = this.dataConfig.numberConfig.val
            }
            console.log('dataConfig',this.dataConfig.numberConfig.val)
            this.queryList(num)
             
        },
        watch: {
            dataConfig:{
                handler(newVal,lodVla){
                    console.log(newVal)
                    if(newVal && Object.keys(newVal).length>0){ 
                        this.bgColor = newVal.bgColor.color
                        this.txtColor = newVal.titleTxtColor.color[0].item
                        this.title = newVal.titleConfig.value 
                        this.mbConfig = newVal.mbConfig.val
                        this.titleBg = newVal.imgConfig.url 
                    }
                },
                deep:true, 
                 
            }
            
        },
        methods:{
            setDefaultImage(index){
                this.isData[index].imgurl= "../../static/img/Default_picture.png"
            },
            //查看更多
           _seeMore(url) {
                uni.navigateTo({
                    url: url,
                });
           },
            //查看商品详情
            _seeGoods(id, p_id){ 
                this.$store.state.pro_id = id;
                uni.navigateTo({
                	url: "/pagesC/goods/goodsDetailed?isDistribution=true&toback=true&pro_id=" +
                		p_id +
                		"&fx_id=" +
                		id,
                });
            },
            queryList(limitNum = 3 ){
                let data = {
                	api: "app.index.distribution_list",
                    limit_num:limitNum
                };
                this.$req
                	.post({
                		data,
                	})
                	.then((res) => { 
                        try{ 
                            if (res.code == 200) {
                                this.isData = res.data.distribution_list.list;  
                            }else{
                                uni.showToast({
                                    icon:'none',
                                    title:res.message
                                })
                            }
                        }catch(err){
                            this.isData = []
                            console.error(err)
                        }
                	});
            }
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
.home_title {
		position: relative;
		z-index: 10; 
		display: flex;
		justify-content: space-between;
      
        margin: 12rpx 32rpx 22rpx;
	}

	.home_title>.title-view {
		font-size: 34rpx;
		line-height: 34rpx;
		color: #014343;
		font-weight: bold;
	}
.more {
        display: flex ;
        align-items: center;
        color: #999999 !important;
        font-size: 26rpx !important;
        margin-left: auto;
        height: 36rpx;
        font-family: Source Han Sans, Source Han Sans;
        font-weight: 500;
        line-height: 34rpx;
	}

	.more image {
		width: 12rpx;
		height: 22rpx;
		margin-left: 12rpx;
	}

	.home_title>.title-image {
		position: absolute;
		width: 111rpx;
		height: 19rpx;
		top: 20rpx;
		left: 24rpx;
		z-index: -1;
	}

    .isPublicModeFX{
        .content_newThree{
            width: 686rpx;
            height: auto;
            margin: 0 32rpx;  
            padding-top: 0rpx;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            box-sizing: border-box;
            padding-bottom: 10rpx;
            >view{width: 100%;}
            >view:first-child{
                display: flex;
                flex-direction: row;
                align-items: center;
                justify-content: space-between;
                .content_newThree_tilte1{
                    min-width: 168rpx; 
                    font-family: Source Han Sans, Source Han Sans;
                    font-weight: 500;
                    font-size: 32rpx;
                    color: #333333;
                    line-height: 32rpx;
                    
                    >image{width: 100%;height: 100%;}
                }
                
            }
            >view:last-child{
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                
                .content_newThree_content{
                    width: 100%;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    background-color: #ffffff;
                    padding: 16rpx;
                    box-sizing: border-box;
                    border-radius: 16rpx;
                    >view:first-child{
                        width: 176rpx;
                        height: 176rpx;
                        border-radius: 16rpx;
                        overflow: hidden;
                        >image{width: 100%;height: 100%;}
                    }
                    .content_newThree_name{
                        flex: 1;
                        flex-direction: column;
                        justify-content: space-between;
                        margin-left: 24rpx;
                        >view:nth-child(1){
                            font-size: 28rpx;
                            font-weight: 500;
                            color: rgba(0,0,0,0.85);
                            line-height: 40rpx;
                            margin: 8rpx 0;
                        }
                        >view:nth-child(2){
                            //width: 174rpx;
                            //height: 40rpx;
                            display: inline-block !important;
                            padding-right: 12rpx;
                            background: rgba(250,81,81,0.1);
                            border-radius: 4rpx 20rpx 20rpx 4rpx;
                            border-left: 4rpx solid #FA5151;
                            // 字体
                            font-size: 24rpx;
                            font-weight: 500;
                            color: #FA5151;
                            line-height: 40rpx;
                            padding-left: 4rpx;
                            box-sizing: border-box;
                        }
                        .content_newThree_price{
                            display: flex;
                            flex-direction: row;
                            align-items: center;
                            justify-content: space-between;
                            margin-top: 16rpx;
                            >view:first-child{
                                font-size: 24rpx;
                                font-weight: 600;
                                color: #FA5151;
                                line-height: 32rpx;
                                >span:nth-child(1){
                                    font-size: 34rpx;
                                    font-weight: bold;
                                    font-family: DIN-Bold, DIN;
                                    line-height: 42rpx;
                                    margin: 0 10rpx 0 4rpx;
                                }
                                >span:nth-child(2){color: #999999;font-weight: 400;text-decoration: line-through;}
                            }
                            >view:last-child{
                                width: 148rpx;
                                height: 56rpx;
                                background-color: rgba(250, 81, 81, .1);
                                border-radius: 28rpx;
                                font-size: 24rpx;
                                font-weight: 500;
                                color: #FA5151;
                                line-height: 56rpx;
                                text-align: center;
                            }
                        }
                    }
                }
            }
        }
    }
</style>
