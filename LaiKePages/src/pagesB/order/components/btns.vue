<template>
    <view>
        <div v-if="isExtract" class="last_right">
            <!-- 自提订单 -->
            <template v-if="self_lifting == 1">
                <div v-if="qxdd" class="last_right_leftBtn" @click="onQxddClick">{{language.order.myorder.cancel_order}}</div>
                <div v-if="ljfk" @click="onLjfkClick">{{language.order.myorder.payment}}</div>
                <div v-if="cktqm"  @click="onCktqmClick" class="tiquma">{{language.order.myorder.Extraction[1]}}</div>
                <div v-if="tqm" @click="onCktqmClick">{{language.order.myorder.Extraction[0]}}</div>
                <div v-if="ljpj" @tap="_navigateTo(status, orde_id, otype, orderList)">{{language.order.order.button[2]}}</div>
            </template>
            <!-- 非自提订单 -->
            <template v-else>
                <!-- 虚拟订单 -->
                <template v-if="self_lifting == 3">
                    <div v-if="cktqm"  @click="onCktqmClick" class="tiquma">{{language.toasts.myOrder.tqm}}</div>
                </template>
                <div v-if="plsqsh" class="last_right_leftBtn" @click="onPlsqshClick">{{language.order.order.button2[0]}}</div>
                <!-- 删除订单 -->
                <div v-if="scdd" class="last_right_leftBtn" @click="onScddClick">{{language.order.order.leftText[1]}}</div>
                <!-- 取消订单 -->
                <div v-if="qxdd" class="last_right_leftBtn" @click="onQxddClick">{{language.order.order.leftText[0]}}</div>
                <div v-if="ckwl && otype!='VI'" class="last_right_leftBtn" @click="onCkwlClick">{{language.order.order.leftText[2]}}</div>
                <div v-if="jstk" class="last_right_leftBtn" @click="_jstk">{{ language.jstk.js }}</div>
                <div v-if="txfh" :class="{ a1: delivery_status == 1 }" @click="onTxfhClick">{{language.order.order.button2[3]}}</div>
                <div v-if="ljfk && otype!='YS'" @click="onLjfkClick">{{language.order.order.rightText[0]}}</div>
                <div v-if="zcgm && orderType != 'PS'" @click="onZcgmClick">{{language.order.order.rightText[4]}}</div>
                <div v-if="qrsh" @click="onQrshClick">{{language.order.order.rightText[2]}}</div>
                <div v-if="ljpj"  @tap="_navigateTo(status, orde_id, otype, orderList)">{{language.order.order.button[2]}}</div>
                <div v-if="zjpj&&!ljpj"  @tap="_navigateTo(status, orde_id, otype, orderList)">{{language.order.order.button[3]}}</div>
            </template>
        </div>
        <view v-else class="last_right">
            <div v-if="(otype=='pt' || otype=='PP') && status==7" @click="zcgm_s">{{language.order.order.rightText[4]}}</div>
            <div v-if="ptxq" class="last_right_leftBtn" @click="onPtxqClick">{{language.order.myorder.Group_details}}</div>
            <div v-if="scdd" class="last_right_leftBtn" @click="onScddClick">{{language.order.myorder.delete_order}}</div>
            <div v-if="ckwl && otype!='VI'" class="last_right_leftBtn" @click="onCkwlClick">{{language.order.myorder.View_Logistics}}</div>
            <div v-if="jstk" :class="{ a1: delivery_status == 1 }" @click="_jstk">{{language.order.myorder.jstk}}</div>
            <div v-if="txfh" :class="{ a1: delivery_status == 1 }" @click="onTxfhClick">{{language.order.order.button2[3]}}</div>
            <div v-if="qrsh" @click="onQrshClick">{{language.order.order.rightText[2]}}</div>
            <div v-if="yqhy" @click="onYqhyClick">{{language.order.myorder.Invite_friends}}</div>
            <div v-if="ljpj"  @tap="_navigateTo(status, orde_id, otype, orderList)">{{language.order.order.button[2]}}</div>
        </view>
    </view>
</template>

<script>
    import {mapMutations, mapState} from "vuex";

    export default {
        name: "btns",
        props: {
            status: {required: true}, // 订单状态, 0 未付款， 1，未发货， 2待收货 ，5 已完成/已收货 7订单关闭，售后结束 ,9 拼团中
            isExtract: {required: true}, // 拼单订单为false
            haveExpress: {require:false}, //判断发货
            self_lifting: {required: true}, // 是否自提订单，自提为1，否则为0
            sale_type: {required: true}, // 0 没有售后， 1部分商品售后， 2 全部进入了售后
            delivery_status: {required: true}, // 1 已经提醒发货了
            orde_id: {required: true}, // 订单id
            sNo: {required: true}, // 订单编号
            otype: {required: true}, // 订单类型  GM 正价 ,KJ 砍价， JP 竞拍， MS 秒杀, pt 拼团, integral 积分商城， FX 分销, PP 平台活动管理的拼团
            form: {required: false}, // order 从订单详情来的 
            goodsNum: {required: false}, // 商品数量，非必传，订单详情需要传
            isOrderDel: {required: false}, // 商品数量，非必传，订单详情需要传
            sellType: {required: false}, // 商品数量，非必传，订单详情需要传
            orderType: {required: false},
            orderList:{required:false},//订单列表，用于判断该订单是否有可评价商品
            type: {required: ''},
            jsJstk:{required: false},
            isItem:{required: {}},
        },
        data () {
            return {
                fastTap: true, // 防止重复点击
            }
        },
        components: {

        },
        computed: {
            ...mapState({
                status_id: state => state.status
            }),
            // 邀请好友
            yqhy () {
                if (this.self_lifting == 1) return false;
                if (this.status != 9) return false;
                if (this.form != 'order') return false;
                return true;
            },
            // 批量申请售后
            plsqsh () {
                
                if (this.self_lifting == 1) return false;
                if (this.status == 7) return false;
                //self_lifting == 4 虚拟商品 无需核销订单
                if (this.status == 5 && this.self_lifting != 4) return false;
                if (this.status == 0) return false;
                if (this.status == 1) return false;
                if (this.sale_type == 2) return false;
                if (this.form !== 'order') return false
                if (this.goodsNum <= 1) return false;
                
                if(this.otype == 'FS' || this.otype =='fs') return false
                return true;
            },
            // 拼团详情
            ptxq () {
                if (this.status != 1) return false;
                return  true;
            },
            // 提取码
            tqm () {
                if (this.self_lifting == 0) return false;
                if (this.status != 5) return false;
                if (this.sale_type != 0) return false;
                return true;
            },
            // 查看提取码
            cktqm () {
                if (this.self_lifting == 0) return false;
                if (this.self_lifting == 3 && this.status == 5) return true;
                if (this.self_lifting == 3 && this.status == 7){
                    if(this.isItem.isDfkGbi){
                        //不存在支付信息, 则是待付款-》关闭订单 不需要显示核销码
                        return false
                    } else {
                        return true;
                    }
                } 
                if (this.self_lifting == 3 && this.status == 8) return true;
                if (this.status != 2) return false;
                if (this.sale_type != 0) return false;
                return true;
            },
            // 确认收货
            qrsh () {
                if (this.self_lifting == 1) return false;
                if (this.status != 2) return false;
                if (this.sale_type != 0) return false;
                return true;
            },
            // 再次购买
            zcgm () {
                if (this.self_lifting == 1) return false;
                if (this.status != 7) return false;
                // 目前只有普通订单有该按钮了
                if(this.otype!='GM')  return false
                return true;
            },
            // 取消订单
            qxdd () {
                if (this.status != 0) return false;
                if (this.otype == 'JP') return false;
                if (this.type == 'YS') return false;
                return true;
            },
            // 删除订单
            scdd () {
                if(this.otype == 'JP') return false
                if(this.isOrderDel) {
                    return true
                } else {
                    return false
                }
            },
            // 查看物流
            ckwl () {
                if (this.haveExpress) return true
                if (this.self_lifting == 1 || this.self_lifting == 2) return false;
                if (this.status != 2 && this.status !=3 && this.status != 5) return false;
                return true;
            },
            // 极速退款
            jstk () {
                if (this.otype=='FX') return false;
                if (!this.jsJstk) return false;
                if (this.status != 1) return false;
                return true;
            },
            // 提醒发货
            txfh () {
                if (this.self_lifting == 1) return false;
                if (this.status != 1) return false;
                return true;
            },
            // 立即付款
            ljfk () {
                if (this.status != 0) return false;
                return true;
            },
            // 立即评价 comments_type 1待评价 2追评 3评论完成
            ljpj () {
                //integral订单没有追加评价
                if(this.otype=='integral'){
                    return false
                }
                //是否显示追加评价
                if(this.orderList){
                    let isTrue = this.orderList.some(function(item){
                        return item.comments_type==1
                    })
                    return isTrue
                }else{
                    return false
                }
            },
            // 追加评价 comments_type 1待评价 2追评 3评论完成
            zjpj () {
                //integral订单没有追加评价
                if(this.otype=='integral'){
                    return false
                }
                //是否显示追加评价
                if(this.orderList){
                    let isTrue = this.orderList.some(function(item){
                        return item.comments_type==2
                    })
                    return isTrue
                }else{
                    return false
                }
            },
        },
        created() {
        },
        methods: {
            ...mapMutations(['SET_ORDER_ID', 'SET_ADDRESS_ID', 'SET_CART_ID']),
            // 邀请好友
            onYqhyClick () {
                if (this.otype == 'pp') {
                    this.navTo('/pagesA/group/group_end?sNo=' + this.sNo + '&returnR=1&a_type=1')    
                } else {
                    this.navTo('/pagesA/group/group_end?sNo=' + this.sNo + '&returnR=1')
                }
                
                
            },
            /**
             * 极速退款 在父组件执行
             */
            _jstk(){
                this.$emit('_jstk', this.isItem)
            },
            /**
             *
             * 查看订单详情
             */
            _navigateTo(status, id, otype, item) {
                if(item.shop_id == 0){
                    return
                }
                
                let url = '/pagesB/order/order?order_id=' + id;
            
                if (status == 0) {
                    url += '&showPay=true';
                }
            
                // #ifdef H5
                url = '/pagesB/order/order?order_id=' + id;
                // #endif
            
                this.navTo(url)
            },
            // 拼团详情
            onPtxqClick () {
                if (this.otype == 'pp') {
                    this.navTo('/pagesA/group/group_end?sNo=' + this.sNo + '&returnR=1&a_type=1')
                } else {
                    this.navTo('/pagesA/group/group_end?sNo=' + this.sNo + '&returnR=1')
                }
            },
            // 取消订单
            onQxddClick  () {
                if (!this.fastTap) return false;
                this.fastTap = false;
                let data = {
                    api: 'app.order.removeOrder',
                    order_id: this.orde_id
                };

                uni.showModal({
                    title: this.language.order.myorder.prompt1,
                    content: this.language.order.myorder.sure1,
					confirmText: this.language.order.myorder.confirm,
					cancelText: this.language.order.myorder.cancel,
                    confirmColor:"#D73B48 !important",
                    cancelColor:'#333333 !important',
                    success: res => {
                        if (res.confirm) {
                            this.$req.post({data}).then(res => {
                                this.fastTap = true;
                                let { code, message } = res;

                                if (code == 200) {
                                    let parameter={
                                        imgUrl:0,//0代表成功图标
                                        title:this.language.order.orderSearch.Tips2[0]
                                    }
                                    this.$emit('showToastMask',parameter)                                   
                                }
                                this.$emit('refresh',this.orde_id)
                            });
                        } else if (res.cancel) {
                            this.fastTap = true;
                        }
                    }
                });
            },
            // 确认收货
            onQrshClick () {
                if (!this.fastTap) return false;
                this.fastTap = false;
                let _this = this
                uni.showModal({
                    title: this.language.order.myorder.prompt2,
                    content: this.language.order.myorder.Tips2,
					confirmText: this.language.order.myorder.confirm,
					cancelText: this.language.order.myorder.cancel,
                    success: res => {
                        if (res.confirm) {
                            //确认支付 在微信小程序端确认收货需要用到微信api
                            // #ifdef MP
                                //只有微信小程序支付才需要先调用wxapi 再调用确认收货接口
                                if(_this.isItem.pay_type == 'mini_wechat' && _this.isItem.wx_order_status == 2){
                                    wx.openBusinessView({
                                        businessType: 'weappOrderConfirm',
                                        extraData: {
                                          merchant_id: uni.getStorageSync('merchant_id').value,
                                          merchant_trade_no: _this.isItem.real_sno,
                                        },
                                        success(res) {
                                            if(res.extraData.status === 'success'){
                                                // 用户确认收货成功，再执行自己的代码
                                                _this._enterSH()
                                            }else if(res.extraData.status === 'fail'){
                                                // 用户确认收货失败
                                                uni.showToast({
                                                  title: "确认收货失败!",
                                                  icon: "none",
                                                });
                                            }else if(res.extraData.status === 'cancel'){
                                                // 用户取消
                                                uni.showToast({
                                                  title: "取消确认收货!",
                                                  icon: "none",
                                                });
                                            }
                                        },
                                        fail(res) {
                                        }
                                      });
                                } else {
                                    //其他支付直接调用确认收货接口
                                    _this._enterSH()
                                }
                            // #endif
                            // #ifndef MP
                                //非小程序直接调用确认收货接口
                                _this._enterSH()
                            // #endif
                        } else if (res.cancel) {
                            _this.fastTap = true;
                        }
                    }
                });
            },
            /**
             * 确认收货接口
             */
            _enterSH(){
                let data = {
                    api: 'app.order.ok_Order',
                    order_id: this.orde_id,
                };
                // 拼团订单确认收货
                if(this.otype=='PT'){
                    data={
                        api:'plugin.group.app.okOrder',
                        orderNo: this.sNo,
                    }
                }
                this.$req.post({data}).then(res => {
                    let { code, message } = res;
                    if (code == 200) {
                        let parameter={
                            imgUrl:0,//0代表成功图标
                            title:this.language.order.orderSearch.Tips2[4]
                        }
                        this.$emit('showToastMask',parameter)
                        setTimeout(() => {
                            this.$emit('refresh')
                        }, 1000);
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                    this.fastTap = true;
                });
            },
            /**
             * 验证订单是否关闭
             * @returns {Promise<void>}
             * @private
             */
            async _check_order_status () {
                let postData = {
                    api: 'app.order.order_details',
                    order_id: this.orde_id
                }
                let {
                    data: {
                        status
                    }
                } = await this.$req.post({data: postData})
                status = Number.parseInt(status)
                if((status == 7 || status == 6)) {
                    return Promise.reject(this.language.order.myorder.order_gb)
                }
                return  Promise.resolve()
            },
            // 再次购买
            onZcgmClick () {
                if (!this.fastTap) return false;
                this.fastTap = false;
                let commodity_type = this.otype == 'VI'?1:0;
                var data = {
                    api: 'app.order .buy_again',
                    id: this.orde_id,
                    commodity_type: commodity_type
                };

                this.$req.post({ data }).then(res => {
                    this.fastTap = true;
                    if (res.code == 200) {
                        let { data: {cart_id}, code, message } = res;
                        this.SET_CART_ID(cart_id)
                        this.SET_ORDER_ID('')
                        this.SET_ADDRESS_ID('')
                        let url = '/pagesE/pay/orderDetailsr?buy_again=true&cart_id=' + cart_id+'&commodity_type='+commodity_type+'&product_type='+this.otype
                        if (this.otype === 'FX') {
                            url = '/pagesE/pay/orderDetailsr?buy_again=true&cart_id=' + cart_id + '&isDistribution=true&commodity_type='+commodity_type
                        }
                        uni.navigateTo({
                            url: url
                        });
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                })
            },
            onPlsqshClick () {
                uni.navigateTo({
                    url: '/pagesB/order/batchOrder?orde_id=' + this.orde_id
                });
            },
            zcgm_s(){
                uni.navigateTo({
                    url: '/pagesA/group/group'
                });
            },
            // 删除订单
            onScddClick () {
                if (!this.fastTap) return false;
                this.fastTap = false;
                uni.showModal({
                    title: this.language.order.myorder.prompt,
                    content: this.language.order.myorder.sure,
					confirmText: this.language.order.myorder.confirm,
					cancelText: this.language.order.myorder.cancel,
                    success: res => {
                        if (res.confirm) {
                            /*发送请求*/
                            let data = {
                                api: 'app.order.del_order',
                                order_id: this.orde_id,
                            };

                            this.$req.post({ data }).then(res => {
                                this.fastTap = true;
                                let { code, message } = res;
                                if (code == 200) {
                                    let parameter={
                                        imgUrl:0,//0代表成功图标
                                        title:this.language.order.myorder.delete_success
                                    }
                                    let type = 'sc'
                                    this.$emit('showToastMask',parameter)
                                    if (this.form === 'order') {
                                        setTimeout(() => {
                                            uni.navigateBack();
                                        }, 1000)
                                    } else {
                                        this.$emit('refresh',this.orde_id)
                                    }


                                } else {
                                    uni.showToast({
                                        title: message,
                                        duration: 1000,
                                        icon: 'none'
                                    });
                                }
                            });
                        } else if (res.cancel) {
                            this.fastTap = true;
                        }
                    }
                });
            },
            // 查看物流
            onCkwlClick () {
                if (!this.fastTap) return false;
                this.fastTap = false;
                let data = {
                    api: 'app.order.logistics',
                    id: this.sNo,
                    o_source: 1,
                    type: ''
                };

                if (this.source == 1) {
                    data.type = 'pond';
                }

                this.$req.post({ data }).then(res => {
                    this.fastTap = true;
                    if (res.code == 200) {
                        let data = res.data;
                        let isPackage = res.isPackage
                        if (data.list.length > 1||!isPackage) {
                            uni.navigateTo({
                                url: '/pagesB/expressage/expressage?sNo=' + this.sNo
                            });
                        } else {
                            uni.navigateTo({
                                url: '/pagesC/expressage/expressage?list=' + JSON.stringify(data.list[0]) + '&sNo=' + this.sNo
                            });
                        }
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                })
            },
            // 查看提取码
            onCktqmClick () {
                if (!this.fastTap) return false;
                this.fastTap = false;
                let data = {
                    api: 'app.order.see_extraction_code',
                    order_id: this.orde_id,
                };
                if(this.self_lifting == 3){
                    data.order_type = 'VI'
                }
                this.$req.post({data}).then(res => {
                    this.fastTap = true;
                    uni.navigateTo({
                        url: '/pagesB/erCode/erCode?dataItem=' + encodeURIComponent(JSON.stringify(res.data))
                    })
                    return
                    uni.$emit('receiving_check_show')
                    let { code, data, message } = res;
                    if (code == 200) {
                        uni.$emit('receiving_check_emit', data)
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                }).catch(error=>{
                    this.fastTap = true;
                });
            },
            // 立即付款
            async onLjfkClick () {
                if (!this.fastTap) return false;
                this.fastTap = false;

                try {
                    this._check_order_status()
                } catch (e) {
                    uni.showToast({
                        title: e,
                        icon: 'none'
                    })
                    this.fastTap = true;

                    this.$emit('refresh')
                    return false

                }

                this.SET_ORDER_ID(this.orde_id)
                this.SET_ADDRESS_ID('')
                let path = '/pagesB/order/order_payment?order_id=' + this.orde_id + '&showPay=true&ordertype='+ this.otype + '&type=XQ' + '&sellType=' + this.sellType

                // #ifdef H5
                path += '&_store=h5';
                // #endif
                this.fastTap = true;
                this.navTo(path)
            },
            // 提醒发货
            onTxfhClick () {
                if (!this.fastTap) return false;
                if (this.delivery_status == 1) return false;
                this.fastTap = false;
                let data = {
                    api: 'app.order.delivery_delivery',
                    order_id: this.orde_id,
                };

                this.$req.post({ data }).then(res => {
                    if (res.code == 200) {                        
                        let parameter={
                            imgUrl:0,//0代表成功图标
                            title:this.language.order.myorder.deliver_goods2
                        }
                        let type = 'tx'
                        this.$emit('showToastMask',parameter)
                        this.$emit('refresh',type)
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });                  
                    }
                    this.fastTap = true;
                });
            }
        }
    }
</script>

<style scoped lang="less">
    @import url("@/laike.less");
    .last_right div{
        display: flex;
        align-items: center;
        justify-content: center;
        min-width: 144rpx;
        height: 72rpx;
        font-size: 24rpx;
        color: @btnBackground;
        border: 1rpx solid @btnBackground;
        border-radius: 56rpx;
        box-sizing: border-box;
        font-weight: 600;


        &:last-child {
            border: 1rpx solid @btnBackground ;
            background-color: transparent;
            color: @btnBackground;
            margin-left: 24rpx;
            border-radius: 56rpx;
            &.a1 {
                background-color: #CCCCCC !important;
                color: #FFFFFF !important;
                border: 0 !important;
            }
        }

        &.last_right_leftBtn {
            border: 1rpx solid #999999;
            background-color: transparent;
            color: #999999;
			margin-left: 24rpx;
            border-radius: 56rpx;
        }
       
    }
</style>
