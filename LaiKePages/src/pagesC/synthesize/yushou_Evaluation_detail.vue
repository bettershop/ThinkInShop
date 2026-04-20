<template>
    <div class='evaluate'>
        <div ref='evaluate_all'>
            <!-- 头部 -->
            <heads :title="language.evaluate.evaluate_detail.title" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
            <!-- 商品信息 -->
            <view class="goods">
                <view class="goods_box">
                    <image :src="isDataPid.cover_map" style="height: 152rpx;width: 152rpx;border-radius: 16rpx;" mode=""></image>
                    <view class="goods_title">{{isDataPid.product_title}}</view>
                </view>
            </view>
            <!-- 分类切换 -->
            <div>   
                <ul class='evaluate-classify'>
                    <li class='evaluate-classify_li' @tap="_comment(0)" :class='allevaluat5==0?"active":""'>
                        <div>
                            <span>{{language.evaluate.evaluate.whole}} </span>
                        </div>
                    </li>
                    <li class='evaluate-classify_li' @tap="_comment(1)" :class='allevaluat5==1?"active":""'>
                        <div>
                            <span>{{language.evaluate.evaluate.Favorable_comments}} </span>
                        </div>
                    </li>
                    <li class='evaluate-classify_li' @tap="_comment(2)" :class='allevaluat5==2?"active":""'>
                        <div>
                            <span>{{language.evaluate.evaluate.Medium_evaluation}} </span>
                        </div>
                    </li>
                    <li class='evaluate-classify_li' @tap="_comment(3)" :class='allevaluat5==3?"active":""'>
                        <div>
                            <span>{{language.evaluate.evaluate.negative_comment}} </span>
                        </div>
                    </li>
                    <li class='evaluate-classify_li' @tap="_comment(4)" :class='allevaluat5==4?"active":""'>
                        <div>
                            <span>{{language.evaluate.evaluate.yes_img}} </span>
                        </div>
                    </li>
                </ul>
            </div>
            <!-- 内容 -->
            <ul class='evaluate-content' ref='evaluate_content'>
                <li v-for='(item,index) in isData' :key='index' @click="navto">
                    <!-- 用户头像 评价时间 -->
                    <div class='content-user'>
                        <div class='content_n'>
                            <img v-if="item.user_name==null" :src="logo" class="ev_img">
                            <img v-else :src="item.headimgurl" class="ev_img">
                            <span class='user-name'>{{item.user_name==''|| item.user_name== null ? language.evaluate.evaluate.anonymous : item.user_name}}</span>
                            <div style="font-size: 24rpx;color: #666666;">
                                {{item.add_time}}
                            </div>
                        </div>
                    </div>
                    <!-- 评价星级 订单号 -->
                    <view class="starall_box">
                        <div class='starall'>
                            <img v-for='(it,inx) in 5' :src="it <= item.CommentType ? starOnImg: starOffImg"/>
                        </div>
                        <view class="starall_title">
                            <span style="margin-left: 16rpx;">{{item.r_sNo?item.r_sNo:'没有返回sNo'}}</span>
                        </view>
                    </view>
                    <!-- 评价内容 商家回复 -->
                    <div class="center">
                        <p style="margin-left: 0;">{{item.content}}</p>
                        <div class='content_img'>
                            <image class='img' v-for='(url,imgindex) in item.commentImgList' :key='imgindex' :src='url' mode="aspectFit"/>
                        </div>
                    </div>
                    <!-- 追评 -->
                    <div v-if='item.review && item.review!== "undefined"'>
                        <div class='review_title'>
                            <span style="position: relative;color: #d8424e;z-index: 555;font-weight: 500;font-size: 28rpx;display:inline-block;">
                                <span class="huangxian"></span>
                                {{language.evaluate.evaluate.add_comments}}:
                            </span>
                            {{item.review == 'undefined' ? '':item.review}}
                        </div>
                        <p></p>
                        <div class='content_img'>
                            <image  class='img' v-for='(re_img,img_index) in item.review_images'
                                 :key='imgindex' :src='re_img.url'
                                 @click="_bigimg(re_img.url,index,img_index,'next')" mode="aspectFit"/>
                        </div>
                    </div>
                    <!-- 商家回复 -->
                    <div v-if='item.replyText' class="huifu">
                        <p>{{language.evaluate.evaluate.business_huifu}}: {{item.replyText}}</p>
                       
                    </div>
                    <!-- 明细 回复 -->
                    <view class="amount">
                        <view class="amount_num" @click.stop="nato(item)">{{language.open_renew_membership.mingxi}}</view>
                        <!-- 已经回复了不需要再显示 -->
                        <template v-if="!item.replyText">
                            <view class="amount_num" @click.stop="maskclick(item.commentsId)">{{language.evaluate.evaluate.hf}}</view>
                        </template>
                    </view>
                </li>
            </ul>
            <view class="buttom_box">{{language.loadMore.contentText.contentnomore}}</view>
            <!-- 回复弹窗 -->
            <view @touchmove.stop.prevent class="bounced" v-if="mask">
                <view class="bounced-box">
                    <view class="bounced-box-box">
                        <view class="bounced-box-box-title">{{language.evaluate.evaluate.hf}}</view>
                        <image :src="guanbi" class="bounced-box-box-img" @click="maskclick"></image>
                    </view>
                    <view class="bounced-box-list">
                           <view class="bounced-box-list_title">
                               <textarea class="bounced-box-list_title_inptut" adjust-position="true" @input="inechange" maxlength="200">
                               </textarea>
                               <view class="bounced-box-list_title_num">
                                   <span style="color: #000000;">{{inechangenum}}</span>
                                   /200</view>
                           </view>
                    </view>
                    <view class="bounced-box-list_btn">
                        <view class="bounced-box-list_btn_box" @click="_huifu">{{language.discover.confirm}}</view>
                    </view>
                </view>
            </view>
           <uni-load-more v-if="allevaluat.length > 10" :loadingType="loadingType"></uni-load-more>
        </div>
    </div>
</template>

<script>
    export default {
        data () {
            return {
                //
                cid: '',
                pid: '',
                isData: [],
                isDataPid: [],
                huifu: '',
                commentId: '',
                //
                bargain: false,
                fff: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/ffff3x.png',
                evaluate_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/evaluate_icon.png',
                title: '评价',
                pid: '',
                inechangenum:0,
                mask:false,
                guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
                loadingType: 0,
                evaluat: '',//存储全部评价数据
                praise: [],//存储好评数据
                review: [],//存储中评数据
                negativeComment: [],//存储差评数据
                reply: '',//商家回复
                img: [],//存储有图片的数据
                allevaluat: [],//存储全部评价数据
                allevaluat1: true,
                allevaluat2: false,
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                allevaluat3: false,
                allevaluat4: false,
                allevaluat5: false,
                isBigimg: false,//放大图片状态
                src: '',//放大图片路径
                comments_cha: 1,
                comments_hao: 0,
                comments_image: 1,
                comments_total: 1,
                comments_zhong: 0,
                type: '', //GOOD=好评,NOTBAD=中评,BAD=差评,HAVEIMG=有图
                starOffImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xing12x.png',
                starOnImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xing22x.png',
                stars: [],
                page: 1, //加载页面
                is_fj:false,//是不是从积分商品详情进来的 
                allLoaded: false,
                autoFill: false, //若为真，loadmore 会自动检测并撑满其容器
                bottomStatus: '',
                bottomPullText: '上拉加载更多...',
                bottomDropText: '释放更新...',
                loading: false,
                all_img: [],
                logo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/lktlogo.png',
                defaultIndex: 0,
                imgurl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/landing_logo2x.png',  //图片
                spanIndex:[]
            }
        },
        onReachBottom() {},
        onLoad(option) {
            if(option.id){
                this.cid = option.id
            }
            if(option.pid){
                this.pid = option.pid
            }
            this._axios()
            this._axiosPid()
        },
        methods: {
            //明细
            nato(item){
                uni.navigateTo({
                    url:'/pagesC/synthesize/yushou_evaluation_subsidiary?id=' + item.id + '&item=' + encodeURIComponent(JSON.stringify(item))
                })
            },
            //评价数据初始化
            _axios(){
                let data = {
                    //api: 'app.mchPreSell.getCommentsInfo',
                    api: 'plugin.presell.AppPreSell.getCommentsInfo',
                    orderType: 'PS',
                    type: this.type,
                    productId: this.pid,
                    page: this.page,
                    pageSize: 10,
                }
                this.$req.post({data}).then(async res => {
                    if(res.code ==200){
                        this.isData = res.data.list
                    } else {
                    }
                })
            },
            //商品数据
            _axiosPid(){
                let data = {
                    //api: 'app.mchPreSell.getGoodsInfoById',
                    api: 'plugin.presell.AppPreSell.getGoodsInfoById',
                    goodsId: this.pid,
                    page: this.page,
                    pageSize: 10,
                }
                this.$req.post({data}).then(async res => {
                    if(res.code ==200){
                        this.isDataPid = res.data
                    } else {
                    }
                })
            },
            // 选择分类
            _comment(index){
                this.allevaluat5 = index
                if(index == 0){
                    this.type = ''
                } else if(index == 1){
                    this.type = 'GOOD'
                } else if(index == 2){
                    this.type = 'NOTBAD'
                } else if(index == 3){
                    this.type = 'BAD'
                } else if(index == 4){
                    this.type = 'HAVEIMG'
                }
                this._axios()
            },
            //回复弹窗
            maskclick(commentId) {
                this.huifu = ''
                this.inechangenum = 0
                this.mask = !this.mask
                this.commentId = commentId
            },
            //回复输入
            inechange(e){
                this.huifu = e.detail.value
                this.inechangenum = e.detail.cursor
            },
            //确认回复
            _huifu(){
                this.mask = !this.mask
                let data = {
                    //api: 'app.mchPreSell.replyComments',
                    api: 'plugin.presell.AppPreSell.replyComments',
                    commentId: this.commentId,
                    commentText: this.huifu,
                    page: this.page,
                    pageSize: 10,
                }
                this.$req.post({data}).then(async res => {
                    if(res.code ==200){
                        uni.showToast({
                            title: '回复成功',
                            icon: 'none'
                        })
                        this._axios()
                    } else {
                    }
                })
            },
        },
       
    }
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
    @import url('@/laike.less');
    .evaluate {
        width: 100%;
        background-color: #f4f5f6;
    }
    
    .evaluate-header {
        height: 150rpx;
        padding-top: 40rpx;
        border-bottom: 1rpx solid #eee;
    }
    
    .evaluate-classify_li {
        //height: 48rpx;
        width: 118rpx;
        text-align: center;
        border-radius: 30rpx;
        line-height: 48rpx;
        margin-right: 24rpx;
        font-size: 22rpx;
        border: 2rpx solid rgba(0,0,0,.1);
    }
    
    .evaluate-header > ul {
        font-size: 26rpx;
        margin-left: 25%;
    }
    
    .evaluate-header > ul:after {
        content: "";
        display: block;
        clear: both;
    }
    
    .evaluate-header > ul li {
        width: 180rpx;
        height: 66rpx;
        text-align: center;
        line-height: 66rpx;
        float: left;
    }
    
    .evaluate-header > ul li:first-child {
        border-top-left-radius: 10rpx;
        border-bottom-left-radius: 10rpx;
        border: 1px solid #E83737;
    }
    
    .evaluate-header > ul li:last-child {
        border-top-right-radius: 10rpx;
        border-bottom-right-radius: 10rpx;
        border: 1px solid #E83737;
    }
    
    .evaluate-header > ul li span {
        margin-left: 10rpx;
    }
    
    .evaluate-classify {
        padding: 20rpx 30rpx  30rpx;
        border-top-left-radius: 24rpx;
        border-top-right-radius: 24rpx;
        height: 120rpx;
        width: 100%;
        display: flex;
        align-items: center;
        background-color: #fff;
    
    }
    
    .evaluate-classify div {
        margin-left: 20rpx;
        margin-right: 20rpx;
        text-align: center;
        //line-height: 48rpx;
    }
    
    .evaluate-content li {
        background-color: #fff;
        padding: 32rpx 32rpx 32rpx 32rpx;
        margin-bottom: 16rpx;
        border-radius: 24rpx;
        &:first-child{
            border-radius: 0 0 24rpx 24rpx !important;
        }
    }
    
    .content-user {
        margin-bottom: 12rpx;
    }
    
    .content-user img {
        width: 46rpx;
        height: 46rpx;
        border-radius: 50%;
        vertical-align: bottom;
    }
    
    .evaluate-content span, .evaluate-content .color {
        font-size: 24rpx;
        color: #9D9D9D;
    }
    
    .evaluate-content p, .evaluate-content .user-name {
        font-size: 32rpx;
        color: #333333;
        font-weight: 500;
        
        flex: 1;
        // margin-left: 80rpx;
    }
    
    .evaluate-content .img {
        width: 180rpx;
        height: 188rpx;
        margin-right: 20rpx;
        border-radius: 16rpx;
        margin-bottom: 12rpx;
    }
    
    .active {
        background: rgba(250,81,81,0.1);
        border-radius: 30rpx;
        color: #FA5151;
        height: 50rpx;
        border: initial;
        width: 122rpx;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .activea{
        
        background: #FFF7CC;
        border-radius: 30rpx;
        color: #FFC300;
        height: 48rpx;
        border: 1rpx solid #FFC300;
        width: 120rpx;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .bigimg {
        height: 100vh;
        width: 100%;
        position: fixed;
        top: 0;
        left: 0;
        z-index: 45;
        background-color: #242424;
    }
    
    .bigimg_img {
        width: 100%;
        height: 100vh;
        background-color: #000000;
        position: fixed;
        top: 0;
        left: 0;
        text-align: center;
    }
    
    .bigimg img {
        width: 100%;
    }
    
    .position_flex {
        position: fixed;
        top: 88rpx;
        left: 0;
        background-color: #FFFFFF;
        z-index: 29;
        width: 100%;
        height: auto;
    }
    
    .starall {
        // margin-left: 80rpx;
    }
    .starall_title{
        display: flex;
        justify-content: center;
        text-align: center;
        height: 42rpx;
        align-items: flex-start;
    }
    .starall_box{
        margin-left: 80rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16rpx;
    }
    .starall img {
        width: 32rpx;
        height: 32rpx;
        margin-right: 12rpx;
    }
    .starall_box span{
        color: #666666;
        
        font-size: 24rpx;
    }
    .content-user {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .center{
        margin-left: 80rpx;
    }
    .center p {
        font-size: 28rpx;
        
        color: #333333;
    }
    .content_n {
        display: flex;
        justify-content: flex-start;
        align-items: center;
        width: 100%;
    }
    
    .content_img {
        margin-top: 32rpx;
        display: flex;
        flex-wrap: wrap;
        margin-bottom: 12rpx;
    }
    
    .bigimg_i {
        height: 88rpx;
        width: 100%;
    }
    
    .bigimg_i img {
        width: 32rpx;
        height: 32rpx;
        margin: 30rpx 0 0 30rpx;
    }
    
    .bigimg_w {
        height: 100%;
        width: 100%;
        margin-top: 1rpx;
    }
    
    .bigimg_w img {
        width: 100%;
    }
    .huangxian{
        position: absolute;
        bottom: 4rpx;
        left: 0rpx;
        width: 60rpx;
        height: 12rpx;
        background-color: #ffe17f;
        z-index: 1;
        opacity: 0.5;
    }
    .review_title {
        // display: flex;
        // align-items: center;
        margin-left: 80rpx;
        
        font-size: 28rpx;
    }
    
    .review_title p {
        font-size: 24rpx;
        color: #999;
        margin-left: 80rpx;
    }
    
    .review_title p:first-child {
        font-size: 26rpx;
        color: red;
        margin-left: 80rpx;
    }
    
    .ev_img {
        width: 64rpx !important;
        height: 64rpx !important;
        border-radius: 50%;
        margin-right: 16rpx;
    }
    .amount{
        margin-top: 16rpx;
        display: flex;
        justify-content: flex-end;
        align-items: center;
    }
    .buttom_box{
        margin-top: 48rpx;
        padding-bottom: 48rpx;
        font-size: 24rpx;
        
        display: flex;
        justify-content: center;
        align-items: center;
        color: #999999;
    }
    .amount_icon{
        width: 40rpx;
        height: 40rpx;
        margin-right: 12rpx;
    }
    .amount_num{
        width: 96rpx;
        height: 56rpx;
        text-align: center;
        border-radius: 28rpx;
        line-height: 56rpx;
        background-color: #F4F5F6;
        font-size: 24rpx;
        
        font-weight: normal;
        color: #333333;
        margin-left: 16rpx;
    }
    .huifu{
            position: relative;
            background: #F8F8F8;
            padding: 24rpx;
            
            color: #333333;
            font-size: 28rpx;
            background: rgba(250,81,81,0.1);
            border-radius: 16rpx;
            margin-left: 80rpx;
            p{
                overflow: hidden;
                text-overflow:ellipsis;
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 2;
                width: 90%;
                font-weight: 400;
                
                color: #333333;
                font-size: 28rpx;
            }
            .Open_p{
                width: 100%;
                display:inherit;
            }
            .Open{
                position: absolute;
                right: 20rpx;
                bottom: 20upx;
                color: #020202;
                font-weight: 500;
            }
            .disnone{
                display: none;
            }
        }
    .bounced {
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            position: fixed;
            left: 0;
            top: 0;
            z-index: 1000;
        }
    .goods{
        background-color: #fff;
        border-radius: 0 0 24rpx 24rpx;
        margin-bottom: 16rpx;
    }
    .goods_box{
        padding: 32rpx;
        display: flex;
    }
    .goods_title{
        width: 510rpx;
        margin-left: 24rpx; 
    }
    .bounced-box {
         bottom: 0;
         width: 100%;
         background: #fff;
         position: fixed;
         z-index: 999;
         border-radius: 24rpx 24rpx 0px 0px;
    
     }
     .bounced-box-list_title{
         width: 654rpx;
         height: 494rpx;
         border-radius: 20rpx;
         border: 1rpx solid rgba(0,0,0,0.1);
         display: flex;
         flex-direction: column;
     }
     .bounced-box-list_title_num{
         padding-left: 32rpx;
         text-align: right;
         padding-right: 32rpx;
     }
     .bounced-box-list_title_inptut{
         padding:  32rpx 32rpx 0 32rpx;
         width: 590rpx;
         height: 400rpx;
     }
     .bounced-box-box {
         width: 100%;
         display: flex;
         height: 136rpx;
         display: flex;
         background-color: #f4f5f6;
         border-radius: 24rpx 24rpx 0px 0px;
         align-items: center;
         justify-content: center
         }
         .bounced-box-list{
             display: flex;
             align-items: center;
             justify-content: center;
             margin-top: 48rpx;
         }
     .bounced-box-box-title {
         margin-left: 270rpx;
         font-size: 40rpx;
         
         font-weight: 500;
         color: #333333;
     }
     .bounced-box-list_btn{
         margin-top: 64rpx;
         display: flex;
         justify-content: center;
         width: 100%;
         align-items: center;
         height: 124rpx;
         border-top: 1rpx solid #eee;
     }
     .bounced-box-list_btn_box{
         width: 686rpx;
         height: 92rpx;
         background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
         border-radius: 52rpx;
         line-height: 92rpx;
         text-align: center;
         color: #fff;
         
         font-weight: 500;
         font-size: 32rpx;
         
     }
     .bounced-box-box-img {
         width: 32rpx;
         height: 32rpx;
         margin-left: 258rpx;
     }
</style>
