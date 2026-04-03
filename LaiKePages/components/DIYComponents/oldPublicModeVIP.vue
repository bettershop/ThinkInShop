<template>
    <view class="isPublicModeVIP">
        <template v-if="isData && isData.length">
            <view class="content_newTwo">
                <view @tap="_seeVIP">
                    <image v-if='language.maskM.qr=="confirm"' :src="new_home_huiyuan2" lazy-load="true"></image>
                    <image v-else :src="new_home_huiyuan1" lazy-load="true"></image>
                </view>
                <view>
                    <view 
                        v-for="(item,index) in isData" :key="index" 
                        v-if="index < 3"  
                        class="content_newTwo_content" 
                        @tap="_seeGoods(item)">
                            <view>
                                <image :src="LaiKeTuiCommon.getFastImg(item.imgurl, 200, 200)" mode="scaleToFill"
                                    lazy-load="true" style="width: 109px;height:109px;" @error="setDefaultImage(index)"/>
                            </view>
                            <view>{{item.product_title}}</view>
                            <view class="content_newTwo_price"> 
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
           
        },
        data(){
            return{
                new_home_huiyuan1:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_huiyuan1.png',
                new_home_huiyuan2:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_huiyuan2.png',
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                isData:[],
                is_vip: 0, //VIP商品：跳转会员中心传入参数
                memberEquity: "",
            }
        },
        created() {
            console.log('我是vip')
            this.getHasGrades()
        } ,
        methods:{
            setDefaultImage(index){
                // this.isData[index].imgurl= "../../static/img/Default_picture.png"
                this.isData[index].imgurl= this.ErrorImg
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
            				this.isData = res.data.memberPro;
            				this.is_vip = res.data.userInfo.grade;
            				this.memberEquity = res.data.memberEquity;
            			}
            		});
            },
            //进入会员中心
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
    .isPublicModeVIP{
        .content_newTwo{
            width: 686rpx;
            height: auto;
            background-color: #ffffff;
            border-radius: 24rpx;
            margin: 32rpx auto;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 24rpx;
            box-sizing: border-box;
            >view{}
            >view:first-child{width: 636rpx;height:96rpx;>image{width: 100%;height: 100%;}}
            >view:last-child{
                width: 100%;
                display: flex;
                flex-direction: row;
                >view:nth-child(2){margin-left:20rpx;margin-right:20rpx;}
                .content_newTwo_content{
                    display: flex;
                    flex-direction: column;
                    margin-top: 24rpx;
                    >view:nth-child(1){
                        width: 200rpx;
                        height: 200rpx;
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
                                font-size: 34rpx;
                                font-family: DIN-Bold, DIN;
                                font-weight: bold;
                                line-height: 42rpx;
                                margin-left: 4rpx;
                            }
                        }
                        >view:nth-child(2){
                            font-size: 20rpx;
                            font-weight: 400;
                            color: #999999;
                            // margin-left: 10rpx;
                            text-decoration: line-through;
                        }
                    }
                }
            }
        }
    }
</style>
