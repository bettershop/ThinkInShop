<template>
    <div ref='box' style="padding-bottom: 96rpx;">
        <heads :title="language.afterDetail.title" ishead_w="3" :bgColor="bgColor" titleColor="#ffffff"></heads>
        <!-- 不知道为什么要用 fixed 布局 -->
        <!-- 增加判断  如果是 限时折扣查看售后 则不选哟使用 fixed 布局 -->
        <view style=" background-color: #F4F5F6;"   
        >
            <div class='allgoods_s home_navigation flex yh-allgoods_s'>
                <div class="font-white status-box yh-font-white">{{getOrderType}}</div>
                <div class="font-white tips-box yh-tips-box" style="word-break: break-all">
                    {{
                    info.type == '0'?language.afterDetail.typeDisc_list[0]:
                    info.type == '1'?language.afterDetail.typeDisc_list[1]:
                    info.type == '2' || info.type == '5'?language.afterDetail.typeDisc_list[7]+info.r_content:
                    info.type == '3'|| info.type == '16'?language.afterDetail.typeDisc_list[2]:
                    info.type == '4' || info.type == '9'?language.afterDetail.typeDisc_list[3]:
                    info.type == '15'?'极速退款到账':
                    info.type == '11'?language.afterDetail.typeDisc_list[4]:
                    info.type == '12'?language.afterDetail.typeDisc_list[5]:
                    (info.type == '8' && info.r_content == language.afterDetail.receiving)  ?language.afterDetail.typeDisc_list[6]:
                    ((info.type == '8' || info.type == '10') && info.r_content != language.afterDetail.receiving )  ? language.afterDetail.typeDisc_list[7]+info.r_content:
                    ''
                }}
                </div>
            </div>
            <!--商品信息 --> 
            <ul class="order_goods" v-if="goodsInfo && (addGoodsList && addGoodsList.length == 0)">
                <li class="order_goods_lis lis_one">
                    <div class="order_one" @click="_goStore(goodsInfo.mchId)">
                        <img :src="goodsInfo.headimg" />
                        {{ goodsInfo.mchName }}
                        <img class="dd-boxa-img" :src="jiantou" />
                    </div>
                    <div class="order_two">
                        <img :src="goodsInfo.img?goodsInfo.img:ErrorImg" @error="handleErrorImg"  class="goodsImg" />
                        <div class="order_two_div1">
                            <p class="order_p_name">{{ goodsInfo.goodsName }}</p>

                            <div class="order_two_div2">
                                <!-- 价格 -->
                                <p class="flex_1">
                                    {{currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(goodsInfo.price,info.exchange_rate) }}
                                </p>
                                <!-- 数量 -->
                                <p class="color_two">x{{ goodsInfo.num }}</p>
                            </div>

                            <p class="color_one">{{ goodsInfo.size }}</p>
                        </div>

                    </div>
                </li>
            </ul>
             
            <!-- 限时折扣订单 售后 查看详情 -->
            <ul class="order_goods" v-if="goodsInfo && addGoodsList && addGoodsList.length>0  " >
                <li class="order_goods_lis lis_one" v-for="(item,index) in addGoodsList" :key="index">
                    <div class="order_one" @click="_goStore(item.mchId)">
                        <img :src="item.mchLogo" />
                        {{ item.mchName }}
                        <img class="dd-boxa-img" :src="jiantou" />
                    </div>
                    <div class="order_two">
                        <img :src="item.imgurl || item.img ?item.imgurl|| item.img :ErrorImg" @error="handleErrorImg"  class="goodsImg" />
                        <div class="order_two_div1">
                            <p class="order_p_name">{{ item.p_name || item.goodsName }}</p>
            
                            <div class="order_two_div2">
                                <!-- 价格 -->
                                <p class="flex_1">
                                    {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ item.z_price || item.price}}
                                </p>
                                <!-- 数量 -->
                                <p class="color_two">x{{ item.num }}</p>
                            </div>
            
                            <p class="color_one">{{ item.size }}</p>
                        </div>
            
                    </div>
                </li>
            </ul>
            
        </view>
        <!-- 商家退换物流信息 --> 
        <!-- 非限时折扣查看 信息  --> 
        <div v-if=" addGoodsList.length == 0 && (info.type == '11'||info.type == '12')" class="rt-address-box"   >
            <template  >
                <div class="flex address-row new-block-title">{{language.afterDetail.merchants_logistics}}</div>
                <div class="flex address-row">
                    <div>{{language.afterDetail.courier_name}}：</div>
                    <div>{{return_info.express}}</div>
                </div>
                <div class="flex address-row">
                    <div>{{language.afterDetail.courier_number}}：</div>
                    <div>{{return_info.express_num}}</div>
                </div>
                <div class="flex address-row">
                    <div>{{language.afterDetail.return_date}}：</div>
                    <div>{{return_info.add_data | dateFormat}}</div>
                </div>
            </template>
        </div>  
           <!-- 限时折扣查看 信息  --> 
        <div v-if=" addGoodsList.length > 0 && (info.type == '11'||info.type == '12')" class="rt-address-box" >
            <template >
                <div class="flex address-row new-block-title">{{language.afterDetail.merchants_logistics}}</div>
                <div class="flex address-row">
                    <div>{{language.afterDetail.courier_name}}：</div>
                    <div>{{return_info.express}}</div>
                </div>
                <div class="flex address-row">
                    <div>{{language.afterDetail.courier_number}}：</div>
                    <div>{{return_info.express_num}}</div>
                </div>
                <div class="flex address-row">
                    <div>{{language.afterDetail.return_date}}：</div>
                    <div>{{return_info.add_data | dateFormat}}</div>
                </div>
            </template>
        </div>
        <!-- 买家回寄物流信息 -->
        <div class="rt-address-box" v-if="send_info.name">
            <div class="flex address-row">{{language.afterDetail.buyer_logistics}}</div>
            <div class="flex address-row ">
                <div>{{language.afterDetail.courier_name}}：</div>
                <div>{{send_info.express}}</div>
            </div>
            <div class="flex address-row">
                <div>{{language.afterDetail.courier_number}}：</div>
                <div>{{send_info.express_num}}</div>
            </div>
            <div class="flex address-row">
                <div>{{language.afterDetail.contact}}：</div>
                <div>{{send_info.name}}</div>
            </div>
            <div class="flex address-row">
                <div>{{language.afterDetail.contact_phone}}：</div>
                <div>{{send_info.tel}}</div>
            </div>
            <div class="flex address-row">
                <div>{{language.afterDetail.return_date1}}：</div>
                <div>{{send_info.add_data | dateFormat}}</div>
            </div>
        </div>
        
        <div class="rt-address-box" v-if="info.type== '1' || info.type== '3' ||info.type== '5'||info.type== '11' ||info.type== '12'||info.type== '13'">
            <!-- 售后地址 -->
            <div class="flex address-row new-block-title">
                {{language.afterDetail.after_address}}
            </div>
            <!-- 退货退款地址 -->
            <div class="flex address-row">
                <div>{{language.afterDetail.return_address}}：</div>
                <div>{{address}}</div>
            </div>
            <!-- 联系人 -->
            <div class="flex address-row">
                <div>{{language.afterDetail.contact}}：</div>
                <div>{{name}}</div>
            </div>
            <!-- 联系电话 -->
            <div class="flex address-row">
                <div>{{language.afterDetail.contact_phone}}：</div>
                <div>{{phone}}</div>
            </div>
        </div>

        <div class="rt-product-box" style="margin-top: 24rpx;">
            <div class="flex address-row new-block-title">
                <span>{{language.afterDetail.after_information}}</span>
            </div>
            <!-- 商品名称 -->
            <div class="flex address-row">
                <div>{{language.afterDetail.product_name}}：</div>
                <div class="yh-p_name">{{info.p_name}}</div>
            </div>
            <!-- 订单号 -->
            <div class="flex address-row">
                <div>{{language.afterDetail.order_no}}：</div>
                <div class="order-id">{{info.r_sNo}}</div>
            </div>
            <!-- 申请时间 -->
            <div class="flex address-row">
                <div>{{language.afterDetail.apply_time}}：</div>
                <div>{{info.re_time}}</div>
            </div>
            <!-- 售后类型 -->
            <div class="flex address-row">
                <div>{{language.afterDetail.after_type}}：</div>
                <div>
                    {{info.re_type==1?language.afterDetail.after_typeList[0]:info.re_type==3?language.afterDetail.after_typeList[1]:language.afterDetail.after_typeList[2]}}
                </div>
            </div>
            <!-- 申请原因 -->
            <div class="flex address-row h_auto">
                <div>{{language.afterDetail.apply_reason}}：</div>
                <div class='yx_right'>{{info.content}}</div>
            </div>
            <!-- 退款金额 -->
            <div class="flex address-row" v-if="info.re_type != 3">
                <div>{{language.afterDetail.amount}}：</div>
                <div v-if="oType == 'IN'">积分{{ allow }} + </div>
                <div>{{currency_symbol}}{{LaiKeTuiCommon.getPriceWithExchangeRate(info.p_price ,info.exchange_rate)}}</div>
            </div>
            <!-- 虚拟商品-退款核销次数 -->
            <div class="flex address-row" v-if="info.r_write_off_num">
                <div>退款核销次数：</div>
                <div>{{info.r_write_off_num}}次</div>
            </div>
            <!-- 上传凭证 -->
            <div class="flex address-row h_auto" v-if="info.re_photo&&info.re_photo.length > 0">
                <div>{{language.afterDetail.upload_documents}}：</div>
                <div class="flex">
                    <div class="re_photo" v-for="(items,index) in info.re_photo" :key='index'>
                        <image :src="items" alt="" @click="_seeImg" mode="widthFix"></image>
                    </div>
                </div>
            </div>

        </div>

    </div>
</template>

<script>
    import Heads from '../../components/header.vue'

    export default {
        data() {
            return {
                title: '售后详情',
                id: '', //订单id
                pid: '', //商品id
                after_id: '',
                currency_symbol:'￥',
                currency_code:'CNY',
                exchange_rate:1,
                address: '',
                name: '',
                phone: '',
                info: {},
                goodsInfo: {},
                record: [], //售后记录
                send_info: [], //买家回寄信息
                return_info: [], //买家回寄信息
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                gdInnerImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/gd_inner.png',
                fxBottomImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/fx_bottom.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                fromOrderDetail: '',
                order_details_id:'',
                bgColor: [{
                        item: '#FA5151 '
                    },
                    {
                        item: '#D73B48'
                    }
                ],
                addGoodsList:[],
                oType: '',
                allow:'',
            }
        },
        onLoad(option) {
            this.id = option.order_id
            this.pid = option.pid
            if (option.id && option.id != 'undefined') {
                this.after_id = option.id
            }
            if (option.fromOrderDetail) {
                this.fromOrderDetail = option.fromOrderDetail
            }
            if(option.order_details_id){
                this.order_details_id = option.order_details_id
            }
        },
        onShow() {
            this.$nextTick(() => {
                this._axios()
            })
        },
        computed:{
            /**
            * 单独写一个方法，处理订单状态
            * 
            * r_type: 订单状态
            *  0:审核中 1:同意并让用户寄回, 2:拒绝 退货退款(没有收到回寄商品) 3:用户已快递 4:收到寄回商品 同意并退款
            *  5:拒绝并退回商品(拒绝回寄的商品 这个时候需要人工介入) 8:拒绝 退款 9:同意并退款 10:拒绝 售后(用户还未寄回商品)
            *  11:同意并且寄回商品 12:售后结束 13:最终状态-人工审核成功
            * 
            * re_type: 退款类型
            *  1:退货退款  2:退款 3:换货
            */
            getOrderType(){
                let name = ''
                //订单信息
                let item = this.info
                //订单状态
                let r_type = this.info.type
                //订单类型
                let re_type = this.info.re_type
                
                switch (Number(r_type)){
                    case 0:
                        name = this.language.afterDetail.type_list[0]
                        break
                    case 1:
                        if(re_type == 3){
                            name = this.language.afterDetail.type_list[6]
                        } else {
                            name = this.language.afterDetail.type_list[1]
                        }
                        break
                    case 2:
                        name = this.language.afterDetail.type_list[2]
                        break
                    case 3:
                        if(re_type == 1){
                            name = this.language.afterDetail.type_list[1]
                        } else {
                            name = this.language.afterDetail.type_list[6]
                        }
                        break
                    case 4:
                        if(re_type != 3){
                            name = this.language.afterDetail.type_list[4]
                        }
                        break
                    case 5:
                        // name = this.language.afterDetail.type_list[2] //  5是退换失败，10是退款失败
                        name = this.language.afterDetail.type_list[3]
                        break
                    case 8:
                        if(this.info.r_content == this.language.afterDetail.receiving){
                            name = this.language.afterDetail.type_list[5]
                        } else{
                            name = this.language.afterDetail.type_list[2]
                        }
                        break
                    case 9:
                        if(re_type != 3){
                            name = this.language.afterDetail.type_list[4]
                        }
                        break
                    case 10:
                        // name = this.language.afterDetail.type_list[3]  //   5是退换失败，10是退款失败
                        name = this.language.afterDetail.type_list[2]
                        break
                    case 11:
                        if(re_type == 3){
                            name = this.language.afterDetail.type_list[6]
                        }
                        break
                    case 12:
                        if(re_type == 3){
                            name = this.language.afterDetail.type_list[5]
                        } else {
                            name = this.language.afterDetail.type_list[4]
                        }
                        break
                    case 13:
                        name = this.language.afterDetail.type_list[4]
                        break
                    case 15:
                        if(re_type != 3){
                            name = this.language.afterDetail.type_list[4]
                        }
                        break
                    case 16:
                        if(re_type == 1){
                            name = this.language.afterDetail.type_list[1]
                        } else {
                            name = this.language.afterDetail.type_list[6]
                        }
                        break
                    default:
                        name = ''
                }
                
                return name
            },  
        },
        methods: {
            handleErrorImg(){
                this.goodsInfo.img = this.ErrorImg
            },
            //放大图片
            _seeImg() {
                let imgsArray = this.info.re_photo;
                uni.previewImage({
                    current: 0,
                    urls: imgsArray
                });
            },
            //
            _axios() {
                var data = {
                    api: 'app.order.Returndetail',
                    pid: this.pid,
                }

                if (this.after_id) {
                    data.id = this.after_id
                }
                if (this.fromOrderDetail) {
                    data.orderDetailId = this.order_details_id
                }

                this.$req.post({
                    data
                }).then(res => {
                    if(res.code != 200){
                        uni.showToast({
                            title:res.message,
                            icon:'none'
                        })
                        return
                    }
                    let {
                        data: {
                            info,
                            goodsInfo,
                            return_info,
                            send_info,
                            store_info,
                            record,
                            currency_symbol,
                            currency_code,
                            exchange_rate,
                            addGoodsList
                        }
                    } = res;
                    if (res.code == 200) {
                        this.addGoodsList = addGoodsList || []
                        if(this.addGoodsList && this.addGoodsList.length>0){
                            this.addGoodsList.unshift(goodsInfo)
                        }
                        this.currency_code = currency_code
                        this.currency_symbol = currency_symbol
                        this.exchange_rate = exchange_rate
                        this.info = info
                        this.return_info = return_info
                        this.goodsInfo = goodsInfo
                        this.send_info = send_info
                        this.store_info = store_info
                        this.record = record
                        this.address = this.store_info.address
                        this.name = this.store_info.name
                        this.phone = this.store_info.phone
                        this.oType = res.data.oType
                        this.allow = res.data.allow
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                    setTimeout(()=>{
                        const query = uni.createSelectorQuery().in(this);
                        query.select('#getHeight').boundingClientRect(data => {
                            this.getHeight = data.height + 'px'
                        }).exec();
                    })
                })

            },
            _goStore(shop_id) {
                uni.navigateTo({
                    url: '/pagesB/store/store?shop_id=' + shop_id
                });
            },
            /* 
                重新编辑
            */
            _edit_after() {
                uni.navigateTo({
                    url: '../../pagesA/returnGoods/refund?refund_type=3&order_details_id=' + this.id +
                        'order_anking=1&r_status=3&rType=3',
                })
            },
        },
        components: {
            Heads
        }
    }
</script>
<style>
    page {
        background: #F4F5F6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/afterSale/afterDetail.less");
    .re_photo{
        >image{
            width: 160rpx !important;
            height: 160rpx !important;
            margin-right: 24rpx;
            border-radius: 16rpx;
        }
    }
    .order-id {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        width: 540rpx;
        // margin-bottom: 20rpx;
        color:#000
    }
</style>
