<template>
    <view class="isPublicModeVIP" :style="{'margin-top':pxToRpxNum(mbConfig)+'rpx',background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}"> 
        <template v-if="goodsArr && goodsArr.length"> 
        <view class="home_title">
            <view class='content_newThree_tilte1 title-view' :style="{color: txtColor}">{{title}}</view>
            <image class="title-image" :src="LaiKeTuiCommon.getFastImg(titleBg, 240, 40)" lazy-load="true"></image>
            <view class="more" @tap="_seeVIP">
            	{{language.home.more}}
            	<image :src="jiantou" lazy-load="true"></image>
            </view> 
        </view>
            <view class="content_newTwo">  
                <view>
                    <view 
                        v-for="(item,index) in goodsArr" :key="index"  
                         :style="{ marginRight: pxToRpxNum(listRight) + 'rpx'}"
                        class="content_newTwo_content" 
                        @tap="_seeGoods(item)">
                            <view class="img-box">
                                <image :src="LaiKeTuiCommon.getFastImg(item.imgurl, 200, 200)" mode="scaleToFill"
                                    lazy-load="true" style="width: 200rpx;height:200rpx;" @error="setDefaultImage(index)"/>
                            </view>
                            <view>{{item.product_title}}</view>
                            <view class="content_newTwo_price" style="display: flex;">
                                <view>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}<span>{{LaiKeTuiCommon.formatPrice(item.vipPrice)}}</span></view>
                                <view>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.price)}}</view>
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
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                title: 'VIP',
                txtColor: '',
                titleBg: '',
                lrConfig: '',
                mbConfig: '',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou2x.png',
                default_img: "../../static/img/default-img.png",
                goodsArr: [], //VIP商品：商品列表
                is_vip: 0, //VIP商品：跳转会员中心传入参数
                memberEquity: "",
                listRight:0,
                bgColor: [{
                		item: '#FFFFFF'
                	},
                	{
                		item: '#FFFFFF'
                	}
                ],
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
                this.listRight =this.dataConfig.lrConfig.val;
                 this.bgColor = this.dataConfig.bgColor.color
                num = this.dataConfig.numberConfig.val
            }
            this.getHasGrades(num)
        },
        watch: {
            dataConfig:{
                handler(newVal,lodVla){
                    console.log(newVal)
                    if(newVal && Object.keys(newVal).length>0){ 
                           this.bgColor = this.dataConfig.bgColor.color
                        this.txtColor = newVal.titleTxtColor.color[0].item
                        this.title = newVal.titleConfig.value 
                        this.mbConfig = newVal.mbConfig.val
                        this.titleBg = newVal.imgConfig.url 
                        this.listRight =newVal.lrConfig.val;
                    }
                },
                deep:true, 
                 
            }
            
        },
        methods:{
            setDefaultImage(index){ 
                this.goodsArr[index].imgurl= this.ErrorImg
            },
            //会员中心：会员商品
            getHasGrades(limitNum = 3) {
            	let data = { 
            		api: "plugin.member.AppMember.MemberCenter",
                     limit_num:limitNum
            	};
            	this.$req
            		.post({
            			data,
            		})
            		.then((res) => {
            			if (res.code == 200) {
            				this.goodsArr = res.data.memberPro;
            				this.is_vip = res.data.userInfo.grade;
            				this.memberEquity = res.data.memberEquity;
            			}
            		});
            },
            //进入会员中心
            _seeVIP(){
                // this.isLogin(() => {
                	uni.navigateTo({
                		url: "/pagesB/userVip/memberCenter",
                	});
                // });
            },
            //查看会员商品
            _seeGoods(e){
               if (this.is_vip == 1) {
               	uni.navigateTo({
               		url: "/pagesC/goods/goodsDetailed?pro_id=" +
               			e.id +
               			"&vipprice=" +
               			e.vipPrice +
               			"&price=" +
               			e.price +
               			"&is_hy=" +
               			this.is_vip,
               	});
               } else {
               	uni.showToast({
               		icon: "none",
               		title: "请先开通会员",
               	});
               }
            },
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");

.home_title {
		position: relative;
		z-index: 10;
		margin: 0rpx 32rpx 22rpx;
		display: flex;
		justify-content: space-between;
        
	}

	.home_title>.title-view { 
		line-height: 34rpx;
		color: #014343; 
        font-family: Source Han Sans, Source Han Sans;
        font-weight: 500;
        font-size: 32rpx;
        color: rgba(0,0,0,0.851);
	}
    .more {
		display: flex;
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


    .isPublicModeVIP{
         padding: 12rpx 0rpx 10rpx;
        .content_newTwo{
            width: 686rpx;
            height: auto;
            background-color: #ffffff;
            border-radius: 24rpx;
            margin: 0rpx 32rpx; 
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            box-sizing: border-box; 
            padding:20rpx;
            >view:first-child{width: 636rpx; }
            >view:last-child{ 
                display: flex;
                flex-direction: row;
                overflow-y: overlay;
                >view:nth-child(2){margin-left:20rpx;margin-right:20rpx;}
                 .content_newTwo_content:first-child{
                    // margin-left:20rpx;
                 } 
                .content_newTwo_content{
                    display: flex;
                    flex-direction: column; 
                    .img-box{
                        border-radius: 16rpx;
                        border: 2rpx solid #C6AB6B;
                    }
                    >view:nth-child(1){
                        width: 196rpx;
                        height: 196rpx;
                        border-radius: 16rpx;
                        overflow: hidden;
                        >image{width: 100%;height: 100%;}
                    }
                    >view:nth-child(2){
                        font-size: 24rpx;
                        font-weight: 500;
                        color: rgba(0,0,0,0.85);
                        line-height: 34rpx;
                        margin: 8rpx 0;
                        // 超出隐藏
                        width: 200rpx;
                        height: auto;
                        display:-webkit-box;
                        overflow: hidden; /*超出隐藏*/
                        text-overflow: ellipsis;/*隐藏后添加省略号*/
                        -webkit-box-orient:vertical; 
                        -webkit-line-clamp:1; //想显示多少行
                    }
                    .content_newTwo_price{
                        display: flex;
                        align-items: flex-end;
                        width: 200rpx;
                        display:-webkit-inline-box;
                        overflow: hidden; /*超出隐藏*/
                        text-overflow: ellipsis;/*隐藏后添加省略号*/
                        -webkit-box-orient:vertical; 
                        -webkit-line-clamp:2; //想显示多少行
                         
                        >view:nth-child(1){
                            font-size: 24rpx;
                            font-weight: 600;
                            color: #FA5151;
                            line-height: 32rpx;
                            >span{
                                // font-size: 34rpx; 
                                margin-left: 4rpx; 
                               font-family: Source Han Sans, Source Han Sans;
                               font-weight: 700;
                               font-size: 34rpx;
                               color: #FA5151;
                               line-height: 34rpx;
                            }
                        }
                        >view:nth-child(2){
                            font-family: Source Han Sans, Source Han Sans;
                            font-weight: 500; 
                            color: #999999;
                             
                            margin-left: 8rpx;
                            line-height: 42rpx;
                            font-size: 20rpx; 
                            // margin-left: 10rpx;
                            text-decoration: line-through;
                        }
                    }
                }
            }
        }
    }
</style>
