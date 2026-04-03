<template>
    <view>
        <heads :title='language.evaluate.evaluate_detail.title' :bgColor='bgColor' titleColor="#000" ishead_w="2">
        </heads>
        <view class="page">
            <view class="page_top">
                <view class="page_top_img">
                    <image :src="detailInfo.imgurl" class="page_top_img_img"></image>
                </view>
                <view class="page_top_txt">
                    <view class="page_top_txt_box">{{detailInfo.product_title}}</view>
                </view>
            </view>
            <view class="page_body">
                <!-- UI向伟云说不需要这个点击事件 -->
                <!-- @click="comments('','')" -->
                <ul class='evaluate-content' ref='evaluate_content'>
                    <li>
                        <!-- 头部用户信息 -->
                        <div class='content-user'>
                            <div class='content_n'>
                                <img v-if="detailInfo.user_name==null" :src="logo" class="ev_img">
                                <img v-else :src="detailInfo.anonymous==0?(detailInfo.headimgurl==''?imgurl:detailInfo.headimgurl):imgurl"
                                     class="ev_img">
                                <span class='user-name'>{{detailInfo.anonymous==1 || detailInfo.user_name=='' || detailInfo.user_name== 'null' || detailInfo.user_name== null ?language.evaluate.evaluate.anonymous:detailInfo.user_name}}</span>
                                <div style="font-size: 24rpx;color: #666666;">
                                    {{detailInfo.addTime}}
                                </div>
                            </div>
                        </div>
                        <!-- 星星评价 -->
                        <view class="starall_box">
                            <div class='starall'>
                                <img v-for='(item,index) in stars' :key="index" :src="item?starOnImg:starOffImg" />
                            </div>
                            <view class="starall_box_gz">
                                <view style="text-align: end;color: #666;">{{attribute_str}}</view>
                            </view>
                        </view>

                        <div class="center">
                            <p>{{detailInfo.content}}</p>
                            <div class='content_img'>
                                <image class='img' v-for='(img,imgindex) in commentImgList' :key='imgindex' :src='img'
                                    @click="_bigimg(img.url,index,imgindex,'')" mode="aspectFit" />
                            </div>
                            <div v-if='detailInfo.replyAdmin' class="huifu">
                                <p :class="{'Open_p':spanIndex>-1}">
                                    {{language.evaluate.evaluate.business_huifu}}: {{detailInfo.replyAdmin}}
                                </p>
                            </div>
                        </div>
                    </li>
                </ul>
                <view class="reply">
                    <view class="reply_box">
                        <view class="reply_box_top" style="border-bottom: 1rpx solid #eee;">
                            {{replylistNum}}{{language.evaluate.evaluate.thf}}
                        </view>
                        <block v-for="(item,index) in replylist" >
                            <view class="reply_box_conten" v-if="replylist">
                                <view class="reply_box_conten_left">
                                    <image :src="item.headimgurl"
                                        style="width: 64rpx;height: 64rpx;border-radius: 50rpx;">
                                    </image>
                                    <view class="reply_box_conten_left_name">{{item.replyName}}</view>
                                </view>
                                <view class="reply_box_conten_right">{{item.addTime}}</view>
                            </view>
                            <view class="reply_box_box">
                                <view class="reply_box_box_top" @click="comments(item.replyName,item.id)">
                                    {{item.content}}</view>
                                <view class="reply_box_box_butom" v-for="(ITEM,INDEX) in item.children" :key="INDEX">
                                    <view class="reply_box_box_butom_box">
                                        <view class="reply_box_box_butom_left">
                                            <image :src="ITEM.replyHeadimgurl"
                                                style="width: 64rpx;height: 64rpx;border-radius: 50rpx;"></image>
                                            <view class="reply_box_box_butom_left_name">{{ITEM.replyName}}</view>
                                        </view>
                                        <view class="reply_box_box_butom_right">{{ITEM.dateTime}}</view>
                                    </view>
                                    <view class="reply_box_box_butom_body" >
                                        {{language.evaluate.evaluate.hf}}<span>@{{ITEM.byName}}：</span>{{ITEM.content}}
                                    </view>
                                </view>
                            </view>
                        </block>
                        <block v-if="replylist.length==0">
                            <view class="empty" >
                                <image :src="No_reply" class="empty_img"></image>
                                <view class="empty_txt">暂无回复~</view>
                            </view>
                        </block>
                    </view>
                </view>
            </view>
            <view :class="classtype==0?'btn':'btn'" :style="{ bottom: bottomVal }">
                <view class="btn_input">
                    <image :src="evaluate_detail_icon"
                        style="width: 40rpx;height: 40rpx;margin-left: 32rpx;margin-right: 8rpx;"></image>
                    <input class="input_die" :adjust-position="false" @blur="inputBindBlur" @focus="inputBindFocus"
                        :focus="firstFocus" :placeholder="barcontent" v-model="barcontentinput"
                        placeholder-class="input_box" />
                </view>
                <view :class="!is_jf?'btn_fx':'btn_fxa'" @click="send">{{language.evaluate.evaluate.fs}}</view>
            </view>
            <view class="buttom_box">{{language.loadMore.contentText.contentnomore}}</view>
        </view>
    </view>
</template>

<script>
    export default {
        data() {
            return {
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                stars: [],
                logo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/lktlogo.png',
                No_reply: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/No_reply.png',
                barcontentinput: '', //输入框内容
                firstFocus: false, //input获取焦点
                logo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/lktlogo.png',
                starOffImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xing12x.png',
                starOnImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xing22x.png',
                evaluate_detail_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/evaluate_detail_icon.png',
                replylist: [],
                spanIndex: [],
                is_jf: false,
                commentImgList: [],
                detailInfo: '',
                loadingType: 0,
                replylistNum: '',
                commentId: '',
                imgurl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/landing_logo2x.png',  //图片
                pageSize: 10,
                pageNo: 1,
                toUserId: '',
                classtype:0,
                barcontent: '说点好听的咯',
                bottomVal: '',
                attribute_str: '',
            }
        },
        onLoad(e) {
            this.is_jf = e.is_jf
            if(e.is_jf=='undefined'){
                this.is_jf = false
            }
            this.commentId = e.id
            this.attribute_str = e.attribute_str
            this.stars = JSON.parse(decodeURIComponent(e.stars));
            this._axios()
            this._axios1()
        },
        onReachBottom() {
            if (this.loadingType == 1 || this.loadingType == 2) {
                return false
            }
            this.loadingType = 1;
            this.pageNo++;
            this._axios()
        },
        methods: {
            changeBottomVal(val) {
            		this.bottomVal = val
            },
            inputBindFocus(e) {
                // 获取手机键盘的高度，赋值给input 所在盒子的 bottom 值
                // 注意!!! 这里的 px 至关重要!!! 我搜到的很多解决方案都没有说这里要添加 px
                // this.$emit('changeBottomVal', e.detail.height + 'px')
                this.bottomVal = e.detail.height + 'px'
                this.classtype = 1
            },

            inputBindBlur() {
                // input 失去焦点，键盘隐藏，设置 input 所在盒子的 bottom 值为0
                // this.$emit('changeBottomVal', 0)
                this.bottomVal =0
                this.classtype = 0
            },
            send() {
                var data = {
                    api: 'app.product.comment.sendComment',
                    text: this.barcontentinput,
                    sid: this.toUserId,
                    commentId: this.commentId
                };
                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        uni.showToast({
                            icon: 'none',
                            title: '发送成功'
                        })
                        this.pageNo=1
                        this._axios()
                        this._axios1()
                        this.barcontentinput = ''
                        this.barcontent = ''
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
            },
            _axios1() {
                var data = {
                    api: 'app.product.comment.getCommentDetail',
                    commentId: this.commentId,
                };
                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        this.commentImgList = res.data.commentImgList
                        this.detailInfo = res.data.detailInfo
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
            },
            _axios() {
                var data = {
                    api: 'app.product.comment.getCommentReplyList',
                    commentId: this.commentId,
                    pageSize: this.pageSize,
                    pageNo: this.pageNo
                };
                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        this.replylistNum = res.data.total
                        if (this.pageNo == 1) {
                            this.replylist = res.data.list
                        } else {
                            this.replylist.push(...res.data.list)
                        }
                        if (res.data.list.length > 0) {
                            this.loadingType = 0
                        } else {
                            this.loadingType = 2
                        }

                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
            },
            comments(e, id) {
                if (id) {
                    this.toUserId = id
                }
                this.initialization(e);
                setTimeout(() => {
                    this.firstFocus = true;
                    // this.barcontent = ''
                }, 0)
                this._axios()
                this._axios1()
            },
            // 初始化所有焦点变量
            initialization(e) {
                this.firstFocus = false;
                if (e) {
                    this.barcontent = '@' + e + '：'
                } else {
                    this.barcontent = ''
                }
            },
            Open(index) {
                let arrIndex = this.spanIndex.indexOf(index);

                if (arrIndex > -1) {
                    this.spanIndex.splice(arrIndex, 1);
                } else {
                    this.spanIndex.push(index);
                }
            },
            Close(index) {
                let arrIndex = this.spanIndex.indexOf(index);

                if (arrIndex > -1) {
                    this.spanIndex.splice(arrIndex, 1);
                } else {
                    this.spanIndex.push(index);
                }
            }
        },
    }
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    .message-input {
    	flex-shrink: 0;
    	width: 100%;
    	position: absolute; // input 所在盒子设置绝对定位
    	left: 0;
    	bottom: 0; // 默认 0
    	z-index: 199;
    }
    .btn_fx {
        width: 120rpx;
        height: 72rpx;
        line-height: 72rpx;
        margin-left: 24rpx;
        text-align: center;
        background: #FA5151;
        font-size: 28rpx;
        
        font-weight: 500;
        color: #FFFFFF;
        border-radius: 36px;
    }

    .btn_fxa {
        width: 120rpx;
        height: 72rpx;
        line-height: 72rpx;
        margin-left: 24rpx;
        text-align: center;
        background: #FFC300;
        font-size: 28rpx;
        
        font-weight: 500;
        color: #333333;
        border-radius: 36px;
    }

    .input_die {
        width: 420rpx;
    }

    .input_box {
        font-size: 28rpx;
        
        font-weight: 400;
        color: #d5d5d6;
    }

    .btn_input {
        width: 542rpx;
        height: 72rpx;
        margin-left: 32rpx;
        background: #F4F5F6;
        border-radius: 52rpx;
        display: flex;
        align-items: center;
    }

    .btn {
        width: 100%;
        height: 128rpx;
        position: fixed;
        border-top: 1rpx solid #eee;
        bottom: 0;
        background-color: #fff;
        display: flex;
        align-items: center;
        left: 0;
        padding-bottom: constant(safe-area-inset-bottom);
        /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
    }
    .btna {
        width: 100%;
        height: 128rpx;
        position: fixed;
        border-top: 1rpx solid #eee;
        bottom: 0;
        background-color: #fff;
        display: flex;
        align-items: center;
        left: 0;
    }

    .reply_box_box_butom_right {
        margin-right: 24rpx;
    }

    .reply_box_box_butom_body {
        margin-top: 16rpx;
        margin-left: 24rpx;
        font-size: 28rpx;
        padding-bottom: 24rpx;
        
        font-weight: 400;
        color: #333333;

        span {
            color: #666666;
        }
    }

    .reply_box_box_butom_right {
        font-size: 24rpx;
        
        font-weight: 400;
        color: #666666;
    }

    .reply_box_box_butom_left_name {
        margin-left: 16rpx;
        font-size: 32rpx;
        
        font-weight: 500;
        color: #333333;
    }

    .starall_box_gz {
        width: 50%;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;

        view {
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
            width: 100%;
            font-size: 14px;
            
            color: #333333;
        }
    }

    .reply_box_box_butom_left {
        margin-left: 24rpx;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .reply_box_box_butom_box {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 24rpx;
    }

    .reply_box_box_butom {
        background-color: #f4f5f6;
        border-radius: 16rpx;
        margin-right: 32rpx;
        // :first-child{
        //     border-radius:  16rpx 16rpx 0 0;
        // }
        // :last-child{
        //     border-radius:  0 0 16rpx 16rpx;
        // }
    }

    .reply_box_box_top {
        font-size: 28rpx;
        
        color: #333333;
        margin-bottom: 24rpx;
    }

    .reply_box_box {
        margin-top: 14rpx;
        margin-left: 112rpx;
        border-radius: 16rpx;
    }

    .reply_box_conten_right {
        font-size: 24rpx;
        
        font-weight: 400;
        color: #666666;
    }

    .reply_box_conten_left_name {
        font-size: 32rpx;
        
        font-weight: 500;
        color: #333333;
        margin-left: 16rpx;
    }

    .reply_box_conten_left {
        margin-top: 32rpx;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .reply_box_conten {
        background-color: #fff;
        margin-left: 32rpx;
        margin-right: 32rpx;

        display: flex;
        justify-content: space-between;
        align-items: center;
    }



    .reply_box_top {
        height: 108rpx;
        padding-left: 32rpx;
        padding-right: 32rpx;
        line-height: 108rpx;
        border-top-left-radius: 24rpx;
        border-top-right-radius: 24rpx;
        font-size: 32rpx;
        
        color: #333333;
        background-color: #fff;

    }

    .reply {
        width: 100%;
        display: flex;
    }

    .reply_box {
        width: 100%;
        border-radius: 24rpx;
        background-color: #fff;
        padding-bottom: 32rpx;
    }

    .page {
        width: 100%;
    }

    .page_top {
        width: 100%;
        height: 216rpx;
        display: flex;
        background-color: #fff;
        border-radius: 0 0 24rpx 24rpx;
        margin-bottom: 24rpx;
    }

    .page_top_img {
        width: 152rpx;
        height: 152rpx;
        margin: 32rpx 24rpx 32rpx 32rpx;
    }

    .page_top_img_img {
        width: 152rpx;
        height: 152rpx;
        border-radius: 16rpx;
    }

    .page_top_txt {
        margin-right: 32rpx;
        padding-top: 32rpx;

    }

    .page_top_txt_box {
        overflow: hidden; //多出的隐藏
        text-overflow: ellipsis; //多出部分用...代替
        display: -webkit-box; //定义为盒子模型显示
        -webkit-line-clamp: 2; //用来限制在一个块元素显示的文本的行数
        -webkit-box-orient: vertical; //从上到下垂直排列子元素（设置伸缩盒子的子元素排列方式）
        
        font-size: 32rpx;
        font-weight: 400;
        color: #333333;
    }

    .page_body {
        width: 100%;
        border-radius: 24rpx;
    }

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
        height: 48rpx;
        width: 118rpx;
        text-align: center;
        border-radius: 24rpx;
        line-height: 54rpx;
        margin-right: 24rpx;
        font-size: 22rpx;
        border: 2rpx solid rgba(0, 0, 0, 0.1);
    }

    .evaluate-header>ul {
        font-size: 26rpx;
        margin-left: 25%;
    }

    .evaluate-header>ul:after {
        content: "";
        display: block;
        clear: both;
    }

    .evaluate-header>ul li {
        width: 180rpx;
        height: 66rpx;
        text-align: center;
        line-height: 66rpx;
        float: left;
    }

    .evaluate-header>ul li:first-child {
        border-top-left-radius: 10rpx;
        border-bottom-left-radius: 10rpx;
        border: 1px solid #E83737;
    }

    .evaluate-header>ul li:last-child {
        border-top-right-radius: 10rpx;
        border-bottom-right-radius: 10rpx;
        border: 1px solid #E83737;
    }

    .evaluate-header>ul li span {
        margin-left: 10rpx;
    }

    .evaluate-classify {
        padding: 20rpx 30rpx 30rpx;
        margin-bottom: 24rpx;
        border-bottom-left-radius: 24rpx;
        border-bottom-right-radius: 24rpx;
        height: 112rpx;
        width: 100%;
        display: flex;
        align-items: center;
        background-color: #fff;

    }

    .evaluate-classify div {
        margin-left: 20rpx;
        margin-right: 20rpx;
        text-align: center;
        line-height: 48rpx;
    }

    .evaluate-content {
        margin-bottom: 24rpx;
    }

    .evaluate-content li {
        background-color: #fff;
        padding: 32rpx 32rpx 32rpx 32rpx;
        margin-bottom: 16rpx;
        border-radius: 24rpx;
    }

    .content-user {
        margin-bottom: 14rpx;
    }

    .content-user img {
        width: 46rpx;
        height: 46rpx;
        border-radius: 50%;
        vertical-align: bottom;
    }

    .evaluate-content span,
    .evaluate-content .color {
        font-size: 24rpx;
        color: #9D9D9D;
    }

    .evaluate-content p,
    .evaluate-content .user-name {
        font-size: 32rpx;
        color: #333333;
        font-weight: 500;
        
        flex: 1;
    }

    .evaluate-content .img {
        width: 188rpx;
        height: 188rpx;
        border-radius: 16rpx;
        margin-bottom: 24rpx;
        margin-right: 10rpx;
    }

    .active {
        background: rgba(250, 81, 81, 0.1);
        border-radius: 24rpx;
        color: #FA5151;
        height: 48rpx;
        border: 0;
        width: 118rpx;
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

    .starall_box {
        margin-left: 80rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24rpx;
    }

    .starall img {
        width: 32rpx;
        height: 32rpx;
        margin-right: 8rpx;
    }

    .starall_box span {
        color: #666666;
        
        font-size: 24rpx;
    }

    .content-user {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .center {
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
        box-sizing: border-box;
            justify-content: flex-start;
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

    .review_title {
        display: flex;
        align-items: center;
        margin-left: 80rpx;
    }

    .review_title p {
        font-size: 24rpx;
        color: #999;
    }

    .review_title p:first-child {
        font-size: 26rpx;
        color: red;
    }

    .ev_img {
        width: 64rpx !important;
        height: 64rpx !important;
        border-radius: 50%;
        margin-right: 16rpx;
    }

    .amount {
        margin-top: 32rpx;
        display: flex;
        justify-content: flex-end;
        align-items: center;
    }

    .buttom_box {
        margin-top: 48rpx;
        font-size: 24rpx;
        
        display: flex;
        justify-content: center;
        align-items: center;
        color: #999999;
        padding-bottom: 152rpx;
    }

    .amount_icon {
        width: 33rpx;
        height: 34rpx;
        margin-right: 12rpx;
    }

    .amount_num {
        font-size: 28rpx;
        
        font-weight: 500;
        color: #333333;
    }
    .empty_img{
        width: 100%;
        height: 388rpx;
    }
    .empty{
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;
        margin-top: 80rpx;
    }
    .empty_txt{
        margin-top: 26rpx;
        font-size: 28rpx;
         
        font-weight: normal;
        color: #333333;
    }
    .huifu {
        margin-top: 24rpx;
        position: relative;
        background: #F8F8F8;
        padding: 24rpx;
        
        color: #333333;
        font-size: 28rpx;
        background: rgba(250, 81, 81, 0.1);
        border-radius: 16rpx;

        p {
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
            width: 90%;
            font-weight: 400;
            padding-top: 8upx;
            
            color: #333333;
            font-size: 28rpx;
        }

        .Open_p {
            width: 100%;
            display: inherit;
        }

        .Open {
            position: absolute;
            right: 20rpx;
            bottom: 20upx;
            color: #020202;
            font-weight: 500;
        }

        .disnone {
            display: none;
        }
    }
</style>
