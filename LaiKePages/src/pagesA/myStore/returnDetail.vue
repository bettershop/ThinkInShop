<template>
    <div class="container">
        <heads :title="title" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <!-- 商品信息 -->
        <view class="container_top">
            <image :src="goods_list.img?goods_list.img:ErrorImg" @error="handleImgError" mode=""></image>
            <view class="container_top_center">
                <view class="container_top_name">{{goods_list.p_name}}</view>
                <view class="container_top_priceInfo">
                    <view class="goodsPrice"><view v-if="oType == 'IN'">{{language.returnDetail.jf}}{{ allow }} + </view > <view>{{mydata.currency_symbol || '￥'}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(goods_list.p_price),mydata.exchange_rate) }}</view> </view>
                    <text>x{{ goods_list.num }}</text>
                </view>
                <view class="container_top_left" v-if="goods_list.p_price">
                   <text>{{goods_list.size}}</text>
                </view>
            </view>
        </view>
        <!-- 处理记录 -->
        <view class="content" v-if="mydata.r_type == 15">
            <view class="content_title">                
                处理记录
            </view>
            <view class="content_item">
                <view class="content_item_left">
                    <!-- 申请结果： -->
                    申请结果
                </view>
                <view class="content_item_right">
                    {{return_info.auditType}}
                </view>
            </view>
            
            <view class="content_item">
                <view class="content_item_left">
                    <!-- 实退金额： -->
                    实退金额
                </view>
                <view class="content_item_right">
                    <view v-if="oType == 'IN'">{{language.returnDetail.jf}}{{ allow }} {{ return_info.real_money > 0 ? '+':'' }} </view>{{mydata.currency_symbol || '￥'}}{{LaiKeTuiCommon.getPriceWithExchangeRate(return_info.real_money,mydata.exchange_rate)}}
                </view>
            </view>
            
            
        </view>
        <!-- 审核记录 -->
        <view class="content" v-if="mydata.r_type != 15&&return_info.type != 0">
            <view class="content_title">                
                {{language.returnDetail.records}}
            </view>
            <view class="content_item">
                <view class="content_item_left">
                    <!-- 审核结果： -->
                    {{language.returnDetail.auditResult}}
                </view>
                <view class="content_item_right">
                    {{return_info.auditType}}
                </view>
            </view>
            <view class="content_item" v-if="return_info.audit_time">
                <view class="content_item_left">
                    <!-- 审核时间： -->
                    {{language.returnDetail.reviewTime}}
                </view>
                <view class="content_item_right">
                    {{return_info.audit_time}}
                </view>
            </view>
            <view class="content_item" v-if="return_info.re_type != 3 && (return_info.type == 4 || return_info.type == 9 || return_info.type == 13)">
                <view class="content_item_left">
                    <!-- 退款金额： -->
                    {{language.returnDetail.refundAmount}}
                </view>
                <view class="content_item_right">
                    <view v-if="oType == 'IN'">{{language.returnDetail.jf}}{{ allow }} {{ return_info.real_money > 0 ? '+':'' }} </view> {{mydata.currency_symbol || '￥'}}{{LaiKeTuiCommon.getPriceWithExchangeRate(return_info.real_money,mydata.exchange_rate)}}
                </view>
            </view>
            <view class="content_item" v-if="return_info.r_content">
                <view class="content_item_left">
                    <!-- 拒绝原因： -->
                    {{language.returnDetail.refusalReasons}}
                </view>
                <view class="content_item_right">
                    {{return_info.r_content}}
                </view>
            </view>
            
            <view class="content_item" v-if="send_info.express">
                <view class="content_item_left">
                    <!-- 回寄物流： -->
                    {{language.returnDetail.returnLogistics}}
                </view>
                <view class="content_item_right">
                    {{send_info.express_num}}（{{send_info.express}}）
                </view>
            </view>
            <view class="content_item" v-if="send_info.express">
                <view class="content_item_left">
                    <!-- 回寄时间： -->
                    {{language.returnDetail.returnTime}}
                </view>
                <view class="content_item_right">
                    {{send_info.add_data}}
                </view>
            </view>
            
            <view class="content_item" v-if="send_info1.express">
                <view class="content_item_left" v-if="send_info.express">
                    <!-- 退换物流： -->
                    {{language.returnDetail.returnLogistics1}}
                </view>
                <view class="content_item_left" v-else>
                    <!-- 回寄物流： -->
                    {{language.returnDetail.returnLogistics}}
                </view>
                <view class="content_item_right">
                    {{send_info1.express_num}}（{{send_info1.express}}）
                </view>
            </view>
            <view class="content_item" v-if="send_info1.express">
                <view class="content_item_left" v-if="send_info.express">
                    <!-- 回寄时间： -->
                    {{language.returnDetail.returnTime1}}
                </view>
                <view class="content_item_left" v-else>
                    <!-- 退换时间： -->
                    {{language.returnDetail.returnTime}}
                </view>
                <view class="content_item_right">
                    {{send_info1.add_data | dateFormat}}
                </view>
            </view>
            
        </view>
        <!-- 退货信息 -->
        <view class="content">
            <view class="content_title">{{language.returnDetail.returnInfo}}</view>
            <view class="content_item">
                <view class="content_item_left">
                    <!-- 订单号： -->
                    {{language.returnDetail.orderNumber}}
                </view>
                <view class="content_item_right">
                    {{return_info.r_sNo}}
                </view>
            </view>
            <view class="content_item">
                <view class="content_item_left">
                    <!-- 申请时间： -->
                    {{language.returnDetail.appliTime}}
                </view>
                <view class="content_item_right">
                    {{return_info.re_time}}
                </view>
            </view>
            <view class="content_item">
                <view class="content_item_left">
                    <!-- 退货类型： -->
                    {{language.returnDetail.returnType}}
                </view>
                <view class="content_item_right">
                    {{return_info.re_type == 1?language.returnDetail.returnList[0]:return_info.re_type == 2?language.returnDetail.returnList[1]:return_info.re_type == 3?language.returnDetail.returnList[2]:''}}
                </view>
            </view>
            <view class="content_item" v-if="return_info.re_type != 3">
                <view class="content_item_left">
                    <!-- 退款金额： -->
                    {{language.returnDetail.refund}}
                </view>
                <view class="content_item_right">
                    <view v-if="oType == 'IN'">{{language.returnDetail.jf}}{{ allow }} + </view> <view>{{mydata.currency_symbol || '￥'}}{{LaiKeTuiCommon.getPriceWithExchangeRate(return_info.re_apply_money,mydata.exchange_rate)}}</view>
                </view>
            </view>
            <view class="content_item">
                <view class="content_item_left">
                    <!-- 申请原因： -->
                    {{language.returnDetail.reasons}}
                </view>
                <view  v-if="mydata.r_type == 15" class="content_item_right">
                    {{ language.jstk.jstktx }}
                </view>
                <view v-else class="content_item_right">
                    {{return_info.content}}
                </view>
            </view>
            <view class="content_item moreVoucherImg" :class="{'voucherImg':return_info.re_photo&&return_info.re_photo.length==1}">
                
                <view class="content_item_left" v-if="return_info.re_photo&&return_info.re_photo.length==1">
                    <!-- 上传凭证： -->
                    {{language.returnDetail.credentials}}
                </view>
                <view class="content_item_right voucher"  v-if="return_info.re_photo&&return_info.re_photo.length==1"> 
                    <view class="oneImage">
                        <image v-for="item,index of return_info.re_photo" :key="index" :src="item" @tap="seeImg(item)"></image>
                    </view>
                </view>
                
                <view class="content_item_right moreVoucher"  v-else>
                    <view class="moreTip">
                        <view class="moreTipTItle">
                            <!-- 上传凭证： -->
                            {{language.returnDetail.credentials}}
                        </view >
                        <view class="moreTip_right" @tap="seeImg(return_info.re_photo)" v-if="return_info.re_photo&&return_info.re_photo.length>1">
                            <view>更多</view>
                            <image :src="jiantou2x" class="rightIcon"></image>
                        </view>
                    </view>
                    <view class="moreImage" v-if="return_info.re_photo&&return_info.re_photo.length>1">                        
                        <image mode="widthFix"  :src="return_info.re_photo[0]" @tap="seeImg(return_info.re_photo)"></image>
                    </view>
                </view>
            </view>
            
        </view> 
        <template>
            <!-- 是否允许退货/换货 -->
            <view class="auditBtn" v-if=" type == 'supplier' && (rType != 3 && rType!= 11) ">
                <!-- 供应商订单 时 按钮显示判断; rType 状态 参考 _getType方法注释 -->  
                <view class="auditBottomBtn1" >
                    <!-- 拒绝  退货退款 当 供应商 同意之后 不显示 拒绝按钮 -->
                    <view class='rejectBtn' @tap="auditCancel" v-if="refuseButton && rType != 5">{{language.returnDetail.reject}}</view>
                    <!-- 通过 -->
                    <view class='passBtn' @tap="auditOk" v-if="rType != 5">{{refuseButton? language.returnDetail.pass :'退款'}}</view>
                    <!-- 人工介入 退货退款 供应商 拒绝之后显示-->
                    <view class='rejectBtn' @tap="auditOk" v-if=" rType == 5">退款</view>
                </view> 
            </view>
            <!-- 原来的 按钮显示逻辑 -->
            <view class="auditBtn" v-if="isAudit && type != 'supplier' "> 
                <view class="auditBottomBtn1" >
                    <!-- 拒绝 -->
                    <view class='rejectBtn' @tap="auditCancel" >{{language.returnDetail.reject}}</view>
                    <!-- 通过 -->
                    <view class='passBtn' @tap="auditOk">{{language.returnDetail.pass}}</view>
                </view>
            </view>
            
            <!-- 人工审核 -->
            <view class="auditBtn" v-if="isRgcl">
                <view class="auditBottomBtn">
                    <view class='passBtn' style="flex: 1;" @tap="rgclOk">退款</view>
                </view>
            </view>
        </template>
        <!-- 退货回寄商品弹窗 （点击通过直接跳转了，暂未使用）-->
        <view class="sendMask" v-if="sendFlag" @touchmove.stop.prevent>
            <view>
                <view class="sendTips">
                    <!-- 确定用户寄回的商品没问题， -->
                    {{language.returnDetail.sendTips[0]}}
                    <br>
                    <!-- 并给用户寄回换货商品？ -->
                    {{language.returnDetail.sendTips[1]}}
                </view>
                <view class="sendBtn">
                    <button @tap="sendCancel">{{language.returnDetail.cancel}}</button>
                    <button @tap="sendOk">{{language.returnDetail.toSend}}</button>
                </view>
            </view>
        </view>
        <!-- 回寄物流同意弹窗 -->
        <view class="sendMask" v-if="auditFlag" @touchmove.stop.prevent>
            <view>
                <view class="sendTips">
                    <!-- 请填写拒绝理由 -->
                    <view class="sendTipsTitle">提示</view>
                    <view class="sendTipsIpt">
                        <!-- 确定要通过该用户的申请， -->
                        {{language.returnDetail.passTips[0]}}
                        <br>
                        <!-- 并让用户寄回？ -->
                        {{language.returnDetail.passTips[1]}}
                    </view>
                </view>
                <view class="sendBtn">
                    <button @tap="auditFail">{{language.returnDetail.cancel}}</button>
                    <!-- 确认通过 -->
                    <button @tap="auditConfirm">{{language.returnDetail.confirm}}</button>
                </view>
            </view>
        </view>
        <!-- 填写拒绝理由弹窗 -->
        <view class="sendMask" v-if="reasonFlag" @touchmove.stop.prevent>
            <view>
                <view class="reason">
                    <!-- 请填写拒绝理由 -->
                    <view class="reasonTitle">
                        {{language.returnDetail.jujueTips}}
                    </view>
                    <view class="reasonIpt">
                        <input class="reasonIpt_input" v-model="reasonText" placeholder-style="font-weight: 400;" type="text" :placeholder="language.returnDetail.jujuePlace">
                    </view>
                </view>
                <view class="sendBtn">
                    <button @tap="reasonFail">{{language.returnDetail.cancel}}</button>
                    <!-- 确认拒绝 -->
                    <button @tap="reasonConfirm">{{language.returnDetail.confirm}}</button>
                </view>
            </view>
        </view>
        <!-- 退款金额弹窗 -->
        <view class="sendMask" v-if="moneyFlag" @touchmove.stop.prevent>
            <view>
                <view class="returnMoney">
                    <view class="returnMoneyTitle">
                        <!-- 请填写退款金额 -->
                        {{language.returnDetail.refuldTitle}}
                    </view>
                    <view class="returnMoneyIpt">
                        {{mydata.currency_symbol}}<input @input="moneyChange" v-model="moneyText" type="digit" :placeholder="language.returnDetail.refuldPlace">
                    </view>
                    <view class="returnMoneyDisc">
                        <image :src="warnIng" mode=""></image>
                        {{language.returnDetail.moneyTips[0]}}{{mydata.currency_symbol || '￥'}}{{LaiKeTuiCommon.getPriceWithExchangeRate(return_info.re_apply_money,mydata.exchange_rate)}}{{language.returnDetail.moneyTips[1]}}
                    </view>
                </view>
                <view class="sendBtn">
                    <button @tap="moneyFail">{{language.returnDetail.cancel}}</button>
                    <!-- 确认退款 -->
                    <button @tap="moneyConfirm">{{language.returnDetail.confirm}}</button>
                </view>
            </view>
        </view>
        
    </div>
</template>

<script>
export default {
    data() {
        return {
            rType:0,
            ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
            title: '退货详情',
            shop_id: '',
            sNo: '',
            order_details_id: '',
            isAudit: '',
            reasonText: '',
            moneyText: '',
            type: '',
            goods_list: {},
            return_info: {},
            send_info: {},
            send_info1: {},
            sendFlag: false,//退货回寄商品弹窗
            isRgcl: false, //是否人工处理
            auditFlag: false,//回寄物流同意弹窗
            reasonFlag: false,//填写拒绝理由弹窗
            moneyFlag: false,//退款金额弹窗
            bgColor: [{item: '#ffffff'},{item: '#ffffff'}],//头部背景色
            warnIng: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/shh.png',
            jiantou2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            mydata:'',
            oType: '',
            allow:'',
        };
    },
    onLoad(option) {
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
        this.order_details_id = option.order_details_id;
        this.sNo = option.sNo;
        this.isAudit = option.type
        //是否人工处理进入
        if(option.isRgcl){
            //显示退款大按钮
            this.isRgcl = JSON.parse(option.isRgcl)
        }
        //根据判断显示对应售后详情
        if(option.yushou){
            this.type = 'yushou'
            //预售售后详情
            this.axiosYs()
        } else if(option.pintuan){
            this.type = 'pintuan'
            //拼团售后详情
            this.axiosPt()
        }else if(option.supplier){
            this.type = 'supplier'
            //供应商售后详情
            this.axiosSupplier()
        }else {
            //普通售后详情
            this.axios()
        }
    },
    onShow(){
        //设置标题
        this.title = this.language.returnDetail.title
        uni.setNavigationBarTitle({title: this.language.returnDetail.title})
    },
    methods: {
        handleImgError(){
            this.goods_list.img = this.ErrorImg
        },
        /**
         * 
         * 单独写一个方法，用于处理传给后台的type参数
         * @param {Number} value 1通过审核 2拒绝审核
         * 
         * r_type: 订单状态
         *  0:审核中 1:同意并让用户寄回, 2:拒绝 退货退款(没有收到回寄商品) 3:用户已快递 4:收到寄回商品 同意并退款
         *  5:拒绝并退回商品(拒绝回寄的商品 这个时候需要人工介入) 8:拒绝 退款 9:同意并退款 10:拒绝 售后(用户还未寄回商品)
         *  11:同意并且寄回商品 12:售后结束 13:最终状态-人工审核成功 14 人工审核失败(暂时没有这种状态)
         *  15 订单未发货-系统自动急速退款 16 收到回寄商品,确定不影响二次销售
         * 
         * re_type: 退款类型
         *  1:退货退款  2:退款 3:换货
         */
        _getType(value){
            let r_type = this.return_info.type
            let re_type = this.return_info.re_type
            switch (r_type){
                //第一次审核（审核中）
                case 0:
                    //通过审核
                    if(value == 1){
                        //re_type：1.退货退款 3.换货
                        if(re_type == 1 || re_type == 3){
                            //审核通过让用户寄回商品
                            return 1
                        }
                        //re_type：2.仅退款
                        if(re_type == 2){
                            //退款成功
                            return 9
                        }
                    }
                    //拒绝审核
                    if(value == 2){
                        //re_type：1.退货退款
                        if(re_type == 1){
                            // 如果是 供应商订单 退货退款 应该是 2
                            if(this.type = 'supplier'){
                                return 2
                            }
                            //退货退款失败，需要重新申请
                            return 10
                        }
                        //re_type：2.仅退款
                        if(re_type == 2){
                            //退款失败，需要重新申请
                            return 8
                        }
                        //re_type：3.换货
                        if(re_type == 3){
                            //换货失败，需要重新申请
                            return 10
                        }
                    }
                    break
                //第二次审核（用户已快递，即用户已寄回商品）
                case 3:
                    //通过审核
                    if(value == 1){
                        //re_type：1.退货退款
                        if(re_type == 1){
                            //收到用户回寄商品，确认退款
                            return 4
                        }
                        //re_type：3.换货
                        if(re_type == 3){
                            //换货成功，寄替换的商品给用户
                            return 11
                        }
                    }
                    //拒绝审核
                    if(value == 2){
                        //re_type：1.退货退款 3.换货
                        if(re_type == 1 || re_type == 3){
                            //未收到用户回寄商品，进入人工售后
                            return 5
                        }
                    }
                    break
                //人工售后（进行退款）
                case 5:
                    //确认退款
                    if(value == 1){
                        return 13
                    }
                    break
                // 收到回寄商品,确定不影响二次销售
                case 16:
                    // 同意
                    if(value == 1){
                        if(re_type == 1){
                            // 同意 退货退款
                            return 4
                        }
                        if(re_type == 2){
                            //  同意退款
                            return 9
                        }
                        if(re_type == 3){
                            // 1 同意换货
                            return 1
                        }
                       
                    }
                    if(value == 2){
                        if(re_type == 2){
                            //  拒绝退款
                            return 8
                        }
                        if(re_type == 3){
                            // 10 拒绝换货
                            return 10
                        }
                        
                    }
                    break
                default:
                    //如需添加逻辑可在上面增加case x:break
            }
        },
        /**
         * 预览凭证图片
         * @param {Object} img
         */
        seeImg(img){
            if(img && typeof img == 'string'){
                uni.previewImage({
                    urls: [img]
                })
            }else{
                uni.previewImage({
                    urls:img,
                    indicator:'default'
                })
            }
        },
        /**
         * 拒绝审核：填写拒绝理由弹窗
         */
        auditCancel(){
            this.reasonFlag = true
        },
        /**
         * 填写拒绝理由弹窗：取消拒绝
         */
        reasonFail(){
            this.reasonFlag = false
        },
        /**
         * 填写拒绝理由弹窗：确认拒绝
         */
        reasonConfirm(){
            //提示：拒绝理由不能为空
            if(!this.reasonText){
                uni.showToast({
                    title: this.language.returnDetail.refusalReason,
                    icon: 'none'
                })
                return
            }
            //请求接口
            let data = {
                api:'mch.App.Mch.Examine',
                shop_id: this.shop_id,
                sNo: this.sNo,
                id: this.order_details_id,
                text: this.reasonText
            }
            //预售接口
            if(this.type == 'yushou'){
                data.api = 'plugin.presell.AppPreSell.Examine'
            }
            //拼团接口
            if(this.type == 'pintuan'){
                data.api = 'plugin.group.appMch.Examine'
            }
           
            //处理传给后台的type参数
            data.r_type = this._getType(2)
            
            //供应商接口
            if(this.type == 'supplier'){
               data.api = 'supplier.AppMch.Orders.examine'
               data.type = data.r_type
               delete data.r_type 
            }
            
            //加载提示：请稍后...
            uni.showLoading({
                title: this.language.showLoading.waiting,
                mask: true
            })
            this.$req.post({data}).then(res=>{
                uni.hideLoading()
                uni.showToast({
                    title: res.message,
                    icon: 'none'
                })
                this.reasonFlag = false
                if(res.code == 200){
                    setTimeout(()=>{
                        if (getCurrentPages().length > 1) {
                            uni.navigateBack({
                                delta: 1
                            })
                        } else {
                            uni.switchTab({
                                url: '/pagesA/myStore/myOrder'
                            })
                        }
                    },1000)
                }
            })
        },
        /**
         * 通过审核：
         */
        auditOk(){
            //换货并且收到用户回寄的快递（换货成功）；跳转填写物流页面，回寄替换的商品。
            if(this.return_info.type == 3 && this.return_info.re_type == 3){
                uni.redirectTo({
                    url: '/pagesA/myStore/shipments?type=1&id=' + this.sNo + '&order_details_id=' + this.order_details_id +'&group=' + this.type
                })
                return
            }
            //审核用户退货退款或者换货；审核通过显示确认弹窗 （如果是拒绝，直接进入退货退款失败）
            if(this.return_info.type == 0 && (this.return_info.re_type == 3|| this.return_info.re_type == 1)){
                this.auditFlag = true
                return
            }
            //仅退款并且是审核中||退货退款并且是用户已快递（已寄回商品）；审核通过显示输入退款金额弹窗
            if((this.return_info.type == 0 && this.return_info.re_type == 2) || (this.return_info.type == 3 && this.return_info.re_type == 1)){
                this.moneyFlag = true
                this.moneyText =  this.LaiKeTuiCommon.getPriceWithExchangeRate(this.return_info.re_apply_money,this.mydata.exchange_rate)
                return
            }
            // 供应商售后订单  
            if(this.type == 'supplier' ){
                this.moneyFlag = true
                return
            }
            
            //其他逻辑走以下请求：
            //请求接口
            let data = {
                api:'mch.App.Mch.Examine',
                shop_id: this.shop_id,
                sNo: this.sNo,
                id: this.order_details_id,
            }
            //预售接口
            if(this.type == 'yushou'){
                data.api = 'plugin.presell.AppPreSell.Examine'
            }
            //拼团接口
            if(this.type == 'pintuan'){
                data.api = 'plugin.group.appMch.Examine'
            }
       
            //处理传给后台的type参数
            data.r_type = this._getType(1)
            
            //供应商接口
            if(this.type == 'supplier'){
               data.api = 'supplier.AppMch.Orders.examine'
               data.type = data.r_type
               delete data.r_type 
            }
            //加载提示：请稍后...
            uni.showLoading({
                title: this.language.showLoading.waiting,
                mask: true
            })
            this.$req.post({data}).then(res=>{
                uni.hideLoading()
                uni.showToast({
                    title: res.message,
                    icon: 'none'
                })
                if(res.code == 200){
                    setTimeout(()=>{
                        if (getCurrentPages().length > 1) {
                            uni.navigateBack({
                                delta: 1
                            })
                        } else {
                            uni.switchTab({
                                url: '/pagesA/myStore/myOrder'
                            })
                        }
                    },1000)
                }
            })
        },
        /**
         * 人工处理：显示输入退款金额弹窗
         */
        rgclOk(){
            this.moneyFlag = true
            this.moneyText = this.LaiKeTuiCommon.getPriceWithExchangeRate(this.return_info.re_apply_money,this.mydata.exchange_rate)
        },
        /**
         * 退款金额弹窗：输入的金额处理
         * @param {Object} e
         */
        moneyChange(e){
            if(Number(this.moneyText)>Number(this.return_info.re_apply_money)){
                setTimeout(()=>{
                    this.LaiKeTuiCommon.getPriceWithExchangeRate(this.return_info.re_apply_money,this.mydata.exchange_rate)
                },0)
            }
        },
        /**
         * 退款金额弹窗：取消
         */
        moneyFail(){
            this.moneyFlag = false
        },
        /**
         * 退款金额弹窗：确认
         */
        moneyConfirm(){
            //提示：退款金额不能为空
            if(!this.moneyText){
                uni.showToast({
                    title: this.language.storeMyOrder.refundSum1,
                    icon: 'none'
                })
                return
            }
            
            //金额转回去
            this.moneyText = this.LaiKeTuiCommon.getBasePriceFromExchange(this.moneyText,this.mydata.exchange_rate)
            //请求接口
            let data = {
                api:'mch.App.Mch.Examine',
                shop_id: this.shop_id,
                sNo: this.sNo,
                id: this.order_details_id,
                price: this.moneyText   
            }
            //预售接口
            if(this.type == 'yushou'){
                data.api = 'plugin.presell.AppPreSell.Examine'
            }
            //拼团接口
            if(this.type == 'pintuan'){
                data.api = 'plugin.group.appMch.Examine'
            } 
            //处理传给后台的type参数
            data.r_type = this._getType(1)
            
            //供应商接口
            if(this.type == 'supplier'){
                data.api = 'supplier.AppMch.Orders.examine'
                data.type = data.r_type
                delete data.r_type 
            }
        
            //加载提示：请稍后...
            uni.showLoading({
                title: this.language.showLoading.waiting,
                mask: true
            })
            this.$req.post({data}).then(res=>{
                uni.hideLoading()
                uni.showToast({
                    title: res.message,
                    icon: 'none'
                })
                this.moneyFlag = false
                if(res.code == 200){
                    setTimeout(()=>{
                        if (getCurrentPages().length > 1) {
                            uni.navigateBack({
                                delta: 1
                            })
                        } else {
                            uni.switchTab({
                                url: '/pagesA/myStore/myOrder'
                            })
                        }
                    },1000)
                }
            })
        },
        /**
         * 确认同意回寄物流弹窗：取消
         */
        auditFail(){
            this.auditFlag = false
        },
        /**
         * 确认同意回寄物流弹窗：确认
         */
        auditConfirm(){
            //请求接口
            let data = {
                api:'mch.App.Mch.Examine',
                shop_id: this.shop_id,
                sNo: this.sNo,
                id: this.order_details_id,
            }
            //预售接口
            if(this.type == 'yushou'){
                data.api = 'plugin.presell.AppPreSell.Examine'
            }
            //拼团接口
            if(this.type == 'pintuan'){
                data.api = 'plugin.group.appMch.Examine'
            }
            //处理传给后台的type参数
            data.r_type = this._getType(1)
            
            //供应商接口
            if(this.type == 'supplier'){
               data.api = 'supplier.AppMch.Orders.examine'
               data.type = data.r_type
               delete data.r_type 
            }
            
            //加载提示：请稍后...
            uni.showLoading({
                title: this.language.showLoading.waiting,
                mask: true
            })
            this.$req.post({data}).then(res=>{
                uni.hideLoading()
                uni.showToast({
                    title: res.message,
                    icon: 'none'
                })
                this.auditFlag = false
                if(res.code == 200){
                    setTimeout(()=>{
                        if (getCurrentPages().length > 1) {
                            uni.navigateBack({
                                delta: 1
                            })
                        } else {
                            uni.switchTab({
                                url: '/pagesA/myStore/myOrder'
                            })
                        }
                    },1000)
                }
            })
        },
        /**
         * 退货回寄商品弹窗：取消
         */
        sendCancel(){
            this.sendFlag = false
        },
        /**
         * 退货回寄商品弹窗：确认-去回寄
         */
        sendOk(){
            this.sendFlag = false
            uni.redirectTo({
                url: '/pagesA/myStore/shipments?type=1&id=' + this.sNo + '&order_details_id=' + this.order_details_id
            }) 
        },
        /**
         * 预售售后详情
         */
        axiosYs(){
            let data = {
                //api:'app.mchPreSell.Returndetail',
                api:'plugin.presell.AppPreSell.returnDetail',
                shop_id: this.shop_id,
                sNo: this.sNo,
                id: this.order_details_id,
            };
            this.$req.post({data}).then(res => {
                let { code, data, message } = res;
                if (code == 200) {
                    this.load = true;
                    if(data.info.type == 0){
                        data.info.auditType = this.language.returnDetail.type_list[0]
                    }else if((data.info.type == 1 || data.info.type == 3) && data.info.re_type == 1){
                        data.info.auditType = this.language.returnDetail.type_list[1]
                    }else if(data.info.type == 4 || data.info.type == 9|| data.info.type == 13 ||data.info.type == 15){
                        data.info.auditType = this.language.returnDetail.type_list[2]
                    }else if((data.info.type == 2 || data.info.type == 5 || data.info.type == 8)&&data.info.re_type != 3){
                        data.info.auditType = this.language.returnDetail.type_list[3]
                    }else if((data.info.type == 1 || data.info.type == 3 || data.info.type == 11)&&data.info.re_type == 3){
                        data.info.auditType = this.language.returnDetail.type_list[4]
                    }else if(data.info.type == 12){
                        data.info.auditType = this.language.returnDetail.type_list[5]
                    }else if((data.info.type == 5 || data.info.type == 10) && data.info.re_type != 2){
                        if(data.info.re_type == 1){
                            data.info.auditType = this.language.returnDetail.type_list[7]
                        }else if(data.info.re_type == 3){
                            data.info.auditType = this.language.returnDetail.type_list[6]
                        }
                    }
                    data.info.audit_time = data.audit_time
                    this.goods_list = data.goods_list
                    this.return_info = data.info
                    this.send_info = data.send_info
                    this.send_info1 = data.return_info
                    this.mydata=data
                    this.oType = res.data.oType
                    this.allow = res.data.allow
                } else {
                    uni.showToast({
                        title: message,
                        duration: 1500,
                        icon: 'none'
                    });
                    this.load = true;
                }
            });
        },
        /**
         * 拼团售后详情
         */
        axiosPt() {
            let data = {
                shop_id: this.shop_id,
                sNo: this.sNo,
                id: this.order_details_id,
                api:'plugin.group.appMch.GetRefundById',
                store_type:7
            };
            this.$req.post({data}).then(res => {
                let { code, data, message } = res;
                if (code == 200) {
                    this.goods_list = data.goods_list
                    this.load = true;
                    if(data.info.type == 0){
                        data.info.auditType = this.language.returnDetail.type_list[0]
                    }else if((data.info.type == 1 || data.info.type == 3) && data.info.re_type == 1){
                        data.info.auditType = this.language.returnDetail.type_list[1]
                    }else if(data.info.type == 4 || data.info.type == 9|| data.info.type == 13|| data.info.type == 15){
                        data.info.auditType = this.language.returnDetail.type_list[2]
                    }else if((data.info.type == 2 || data.info.type == 5 || data.info.type == 8)&&data.info.re_type != 3){
                        data.info.auditType = this.language.returnDetail.type_list[3]
                    }else if((data.info.type == 1 || data.info.type == 3 || data.info.type == 11)&&data.info.re_type == 3){
                        data.info.auditType = this.language.returnDetail.type_list[4]
                    }else if(data.info.type == 12){
                        data.info.auditType = this.language.returnDetail.type_list[5]
                    }else if((data.info.type == 5 || data.info.type == 10) && data.info.re_type == 3){
                        data.info.auditType = this.language.returnDetail.type_list[6]
                    }
                    data.info.audit_time = data.audit_time
                    this.return_info = data.info
                    this.send_info = data.send_info
                    this.send_info1 = data.return_info
                    this.mydata=data
                    this.oType = res.data.oType
                    this.allow = res.data.allow
                } else {
                    uni.showToast({
                        title: message,
                        duration: 1500,
                        icon: 'none'
                    });
                    this.load = true;
                }
            });
        },
        /**
         * 供应商售后详情
         */
         axiosSupplier() {
             let data = { 
                 id: this.order_details_id,
                 api:'supplier.AppMch.Orders.refundPageById',
                 // store_type:7,
                 shop_id: this.shop_id,
                 sNo: this.sNo,
             };
             this.$req.post({data}).then(res => {
                 let { code, data, message } = res;
                 if (code == 200) {
                     this.goods_list = data.goods_list
                     this.load = true;  
                     if(data.info.type == 0){
                         data.info.auditType = this.language.returnDetail.type_list[0]
                     }else if((data.info.type == 1 || data.info.type == 3) && data.info.re_type == 1){
                         data.info.auditType = this.language.returnDetail.type_list[1]
                     }else if(data.info.type == 4 || data.info.type == 9|| data.info.type == 13|| data.info.type == 15){
                         data.info.auditType = this.language.returnDetail.type_list[2]
                     }else if((data.info.type == 2 || data.info.type == 5 || data.info.type == 8)&&data.info.re_type != 3){
                         data.info.auditType = this.language.returnDetail.type_list[3]
                     }else if((data.info.type == 1 || data.info.type == 3 || data.info.type == 11)&&data.info.re_type == 3){
                         data.info.auditType = this.language.returnDetail.type_list[4]
                     }else if(data.info.type == 12){
                         data.info.auditType = this.language.returnDetail.type_list[5]
                     }else if((data.info.type == 5 || data.info.type == 10) && data.info.re_type == 3){
                         data.info.auditType = this.language.returnDetail.type_list[6]
                     }
                     data.info.audit_time = data.audit_time
                     this.return_info = data.info
                     this.send_info = data.send_info
                     this.send_info1 = data.return_info
                     this.rType= data.r_type
                     this.refuseButton = data.refuseButton
                     this.mydata=data
                     this.oType = res.data.oType
                     this.allow = res.data.allow
                 } else {
                     uni.showToast({
                         title: message,
                         duration: 1500,
                         icon: 'none'
                     });
                     this.load = true;
                 }
             });
         },
        /**
         * 售后详情
         */
        axios() {
            let data = {
                shop_id: this.shop_id,
                sNo: this.sNo,
                id: this.order_details_id,
                api:'mch.App.Mch.Returndetail'
            };
            this.$req.post({data}).then(res => {
                let { code, data, message } = res;
                if (code == 200) {
                    this.goods_list = data.goods_list
                    this.load = true;
                    if(data.info.type == 0){
                        data.info.auditType = this.language.returnDetail.type_list[0]
                    }else if((data.info.type == 1 || data.info.type == 3) && data.info.re_type == 1){
                        data.info.auditType = this.language.returnDetail.type_list[1]
                    }else if(data.info.type == 4 || data.info.type == 9|| data.info.type == 13|| data.info.type == 15){
                        data.info.auditType = this.language.returnDetail.type_list[2]
                    }else if((data.info.type == 2 || data.info.type == 5 || data.info.type == 8)&&data.info.re_type != 3){
                        data.info.auditType = this.language.returnDetail.type_list[3]
                    }else if((data.info.type == 1 || data.info.type == 3 || data.info.type == 11)&&data.info.re_type == 3){
                        data.info.auditType = this.language.returnDetail.type_list[4]
                    }else if(data.info.type == 12){
                        data.info.auditType = this.language.returnDetail.type_list[5]
                    }else if((data.info.type == 5 || data.info.type == 10) && data.info.re_type != 2){
                        if(data.info.re_type == 1){
                            data.info.auditType = this.language.returnDetail.type_list[7]
                        }else if(data.info.re_type == 3){
                            data.info.auditType = this.language.returnDetail.type_list[6]
                        }
                    }
                    data.info.audit_time = data.audit_time
                    this.return_info = data.info
                    this.send_info = data.send_info
                    this.send_info1 = data.return_info
                    this.mydata=data
                    this.oType = res.data.oType
                    this.allow = res.data.allow
                } else {
                    uni.showToast({
                        title: message,
                        duration: 1500,
                        icon: 'none'
                    });
                    this.load = true;
                }
            });
        },
    }
};
</script>
<style>
    page{
        background: #F4F5F6
    }
</style>
<style lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/returnDetail.less');
</style>
