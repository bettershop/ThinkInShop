<template>
    <view class="btnsItem">  
        <view v-if="get_button_list['qxdd']" class="last_right_leftBtn" @click="onQxddClick">{{language.order.myorder.cancel_order}}</view>
        <!-- 删除订单 -->
        <view v-if="get_button_list['scdd']" class="last_right_leftBtn" @click="onScddClick">{{language.order.myorder.delete_order}}</view>
        <!-- 查看物流 -->
        <view v-if="get_button_list['ckwl']" @click="onCkwlClick">{{language.order.myorder.View_Logistics}}</view>
        <!-- 提醒发货 -->
        <view v-if="get_button_list['txfh'] && item.delivery_status == 0" @click="onTxfhClick">{{language.seckill.seckill_my.button[2]}}</view>
        <view v-if="get_button_list['txfh'] && item.delivery_status == 1" style="background: rgb(169, 169, 169) !important;color: #fff !important; border: none;">{{language.seckill.seckill_my.button[2]}}</view>
        <!-- 极速退款 -->
        <view v-if="get_button_list['jstk']" @click="_jstk">{{language.order.myorder.jstk}}</view>
        <!-- 确认收货 -->
        <view v-if="get_button_list['qrsh']" @click="onQrshClick">{{language.order.order.rightText[2]}}</view>
        <!-- 立即付款 -->
        <view v-if="get_button_list['ljfk']" @click="onLjfkClick">{{language.order.myorder.payment}}</view>
        <!-- 上传凭证 -->
         <!-- v-if="get_button_list['scpz']" -->
        <view v-if="get_button_list['scpz']" @click="up_pz">{{ language.afterDetail.upload_documents }}</view>
        <!-- 凭证审核中 -->
        <view v-if="get_button_list['pzshz']" @click="pz_review">{{language.pzsh}}</view>
        <!-- 核销码 -->
        <view v-if="get_button_list['hxm']"  @click="onCktqmClick" >{{language.toasts.myOrder.hxm}}</view>
        <!-- 立即评价 -->
        <view v-if="get_button_list['ljpj']" @click="togoReview">{{language.order.order.button[2]}}</view>
        <!-- 追加评价 -->
        <view v-if="get_button_list['zjpj']" @click="togoReviewJia">{{language.order.order.button[3]}}</view>
        <!-- 查看售后 -->
        <view v-if="get_button_list['cksh']" @click="seeAfterSale">{{language.order.order.button[4]}}</view>
        <!-- 申请售后 -->
        <view v-if="get_button_list['sqsh']" @click="onPlsqshClick">{{language.order.order.button[1]}}</view>
        <!-- 提取码 -->
        <view v-if="get_button_list['tqm']" @click="onCktqmClick">{{language.toasts.myOrder.tqm}}</view>
        <!-- 查看提取码 -->
        <view v-if="get_button_list['cktqm']" @click="onCktqmClick">{{language.order.myorder.Extraction[1]}}</view>
        <!-- 再次购买 -->
        <view v-if="get_button_list['zcgm']" @click="onZcgmClick">{{language.order.myorder.Buy_again}}</view>
        <!-- 申请开票 --> 
        <view v-if="get_button_list['sqkp']"  @click="_applyInvoice">{{language.order.myorder.sqkp}}</view>
         
    </view>
</template>

<script>  
    import {mapMutations, mapState} from "vuex"; 
    export default {  
        name: "btnsItem",
        props: {
            // 订单对象
            item:{
                type:Object,
                default:()=>{}
            },
            // 订单类型 
           orderType:{
               type: String,
               default: ''
           },
           // 按钮显示对象
           get_button_list:{
               type:Object,
               default:()=>{}
           },
           orde_id: {required: true}, // 订单id
            // 订单商品
           orderList: {
              type:Array,
              default:[],
            },
          
           sNo:{
              type: [String,Number],
               default: ''
           },
           // 申请开票时 需要的金额字段
           invoicePrice:{
               type: [String,Number],
               default: ''
           },
           // 订单类型
           otype:{
               type: String,
               default: ''
           },
        },
        data () {
            return {
                fastTap: true, // 防止重复点击
                seeAllList: [], 
            }
        },
       mounted(){
       },
        created() { 
        },
        methods: {
            ...mapMutations([
             'SET_ORDER_ID'
            ,'SET_ADDRESS_ID'
            ,'SET_CART_ID'
            ,'SET_ISMASKINVOICE3'
            ,'SET_REFRESH'
            ,'SET_JSTK']),
            seeAll(){
                
            },
       
            // 评论
            togoReview(){
                if (this.orderList.length == 1) {
                    // 多商品评价
                    uni.navigateTo({
                        url: "/pagesC/evaluate/evaluating?order_details_id=" +
                            this.orderList[0].id +
                            "&all=true",
                    });
                } else {
                    uni.navigateTo({
                        url: "/pagesC/evaluate/evaluating?orderNo=" +
                            this.sNo +
                            "&all=true"
                    });
                } 
            },
            // 追加评论  - 只会以商品 为单位进行追评
            togoReviewJia(){ 
               if (this.orderList.length == 1) {
                   // 多商品评价
                   uni.navigateTo({
                       url: "/pagesC/evaluate/evaluating?order_details_id=" +
                           this.orderList[0].id +
                           "&num=all&type=1",
                   });
               } else {
                   uni.navigateTo({
                       url: "/pagesC/evaluate/evaluating?orderNo=" +
                           this.sNo +
                           "&num=all&type=1" +
                           '&order_details_id='+ this.item.id,
                   });
               }
                
            },
            // 申请开票
            _applyInvoice(item) {
               if (item.invoiceTimeout) {
                   this.isMaskInvoice = true;
               } else {
                   let data = {
                       api: "app.invoiceHeader.getDefault",
                   };
                   this.$req.post({ data }).then((res) => {
                       if (res.data && res.data.id) {
                           uni.navigateTo({
                               url:
                                   "/pagesB/invoice/openInvoice?sNo=" +
                                   this.item.sNo +
                                   "&price=" +
                                   this.item.invoicePrice,
                           });
                       } else {
                           this.SET_ISMASKINVOICE3(true)
                       }
                   });
                }
            },
         
            /**
             * 极速退款 在父组件执行
             */
            _jstk(){
                this.SET_JSTK(JSON.stringify(this.item))
            },
          
            // 查看售后
            seeAfterSale(){ 
                let order_details_id = ''
                if(this.item.list && this.item.list.length>1){
                    let ids = ''
                    if(this.item.otype != 'FS'){                        
                        ids = this.item.list.map(v=>{
                          return v.get_order_details_button.cksh == 1 ? v.id : ''
                      })
                    }else{
                        ids = this.item.list.filter(v=>{
                          return v.is_addp != 1 ? v.id : ''
                      }).map(v=>v.id)
                    }
                  
                  order_details_id = ids.length === 1 ? ids[0] :ids.join(',')
                }else if(this.item.list && this.item.list.length == 1){
                   order_details_id = this.item.list[0].id
                }else{
                  order_details_id = this.item.id
                }
                
                this.navTo(
                    '/pagesC/afterSale/afterDetail?order_id=' +
                        this.item.id+
                        '&id=' +
                       this.orderList[0].returnId +
                        '&pid=' +
                       this.orderList[0].p_id +
                        '&fromOrderDetail=true' +
                        '&order_details_id='+ order_details_id 
                    )
                
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
                                
                                    uni.showToast({
                                          title:this.language.order.orderSearch.Tips2[0],
                                          icon:'success'
                                    }) 
                                    this.SET_REFRESH()
                                }else{
                                    uni.showToast({
                                          title:message||'网络异常！',
                                          icon:'error'
                                    })
                                    return
                                } 
                                this.SET_REFRESH(this.orde_id)
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
                                if(_this.item.pay_type == 'mini_wechat' && _this.item.wx_order_status == 2){
                                    wx.openBusinessView({
                                        businessType: 'weappOrderConfirm',
                                        extraData: {
                                          merchant_id: uni.getStorageSync('merchant_id').value,
                                          merchant_trade_no: _this.item.real_sno,
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
                    this.fastTap = true;
                    if (code == 200) {
                   
                      uni.showToast({
                            title:this.language.order.orderSearch.Tips2[4],
                            icon:'success'
                      })
                        this.SET_REFRESH()
                        // setTimeout(() => {
                        //     this.SET_REFRESH()
                        // }, 1000);
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                    
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
                        let url = '/pagesE/pay/orderDetailsr?buy_again=true&cart_id=' + cart_id+'&commodity_type='+commodity_type
                        uni.setStorageSync('vipSource',0)
                        // 会员商品再次购买 
                        if(this.item.list[0]?.vipGoods || this.item.vipGoods){
                            
                              url = '/pagesE/pay/orderDetailsr?is_hy=true&buy_again=true&cart_id=' + cart_id+'&commodity_type='+commodity_type+''
                            uni.setStorageSync('vipSource',1)
                        }
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
                /**
                 *  订单详情点击申请售后 
                 * 如果有 list 则代表着 点击了'订单'底部申请售后的按钮
                 * 如果没有则代表着点击了 '商品'底部申请售后
                 */
                let order_details_id = ''
                if(this.item.list && this.item.list.length > 1){
                    // 点击订单按钮底部时 （多商品）
                     const ids = this.item.list.map(v=>{ 
                        if(this.item.otype != 'FS'){
                            // 普通商品申请售后流程
                            return (v.get_order_details_button.sqsh == 1 )? v.id : ''
                        }else{
                            // 限时折扣申请售后  
                            return v.id  
                        }
                    })
                    order_details_id = ids.join(',')
                    if(this.item.otype == 'FS'){
                        this.navTo(`/pagesA/returnGoods/returnGoods?order_details_id=${order_details_id}&order_anking=2&isbatch=true&r_status=5`)
                        return
                    }
                    if(order_details_id.length>0){
                        uni.navigateTo({
                            url: '/pagesB/order/batchOrder?orde_id=' + this.orde_id+'&order_details_id=' + order_details_id
                        });
                    }else{
                        console.error('数据错误!')
                    }
                }else if(this.item.list && this.item.list.length == 1){
                    // 点击订单按钮底部时 （单商品）
                    this.navTo(`/pagesA/returnGoods/returnGoods?order_details_id=${this.item.list[0].id}&order_anking=2&rType=undefined&isbatch=true&r_status=5`)
                }
                else{
                    // 点击 商品底部按钮时
                    this.navTo(`/pagesA/returnGoods/returnGoods?order_details_id=${this.item.id}&order_anking=2&rType=undefined&isbatch=true&r_status=5`)
                    
                }
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
                                order_id: this.orde_id
                            };

                            this.$req.post({ data }).then(res => {
                                this.fastTap = true;
                                let { code, message } = res;
                                if (code == 200) {
                                  
                                    uni.showToast({
                                           title:this.language.order.myorder.delete_success,
                                          icon:'success'
                                    })
                                     const pages  = getCurrentPages()
                                     const {route} = pages[pages.length -1]
                                     if(pages.length == 1){
                                         uni.redirectTo({
                                             url: '/pages/shell/shell?pageType=my'
                                         });
                                         return
                                     } 
                                      if(route == '/pagesB/order/order'){
                                          setTimeout(() => { 
                                              uni.navigateBack();
                                          }, 500)  
                                      }else{
                                           uni.pageScrollTo({
                                              scrollTop: 0,
                                              duration: 300
                                            });
                                      }
                                      this.SET_REFRESH(this.orde_id)
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
                if(this.item.self_lifting == 3){
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
            up_pz(){
                uni.setStorageSync('voucher', this.item)
                uni.navigateTo({
                    url: '/pagesE/pay/voucher'
                });
            },
            pz_review(){
                uni.showToast({
                    title: '付款凭证已提交，请等待管理员审核!',
                    duration: 1500,
                    icon: 'none'
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
                    this.SET_REFRESH()
                    return false

                }

                this.SET_ORDER_ID(this.orde_id)
                this.SET_ADDRESS_ID('')
                if(this.item.payTarget){ 
                 uni.setStorageSync('payTarget', this.item.payTarget)

                }
                let path = '/pagesB/order/order_payment?order_id=' + this.orde_id + '&showPay=true&ordertype='+ this.otype + '&type=XQ' + '&sellType=' + this.sellType

                // #ifdef H5
                path += '&_store=h5';
                // #endif
                this.fastTap = true;
                if (this.item.otype == 'IN' && this.item.z_price <= 0) {
                    this.$emit('pay_display',this.item)
                }else{
                    this.navTo(path)
                }
                
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
                        uni.showToast({
                             title:this.language.order.myorder.deliver_goods2,
                              icon:'success'
                        })
                        this.SET_REFRESH()
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
    .btnsItem{
        display: flex;
        view{
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
        margin-left: 24rpx;
        &.last_right_leftBtn {
            border: 1rpx solid #999999;
            background-color: transparent;
            color: #999999;
			margin-left: 24rpx;
            border-radius: 56rpx;
        } 
        }
        view:first-child{
            margin-left: 0rpx;
        }
    }
    .last_right {
        border:none !important
    }
</style>
