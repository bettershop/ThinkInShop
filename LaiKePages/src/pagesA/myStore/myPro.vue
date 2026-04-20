<template>
    <div style="min-height: 100vh;width: 100vw;overflow: hidden;"
        :style="{backgroundColor: list.length == 0?'':'#F6F6F6'}">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.myStore.myPro" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <!-- 导航栏 -->
        <switchNavOne ref="switchNavOne" :is_switchNav="header" :is_switchNav_radius="'0 0 0 0'"
            :is_switchNav_padT="'0'" :is_switchNav_padB="'24rpx'" @choose="_header_index">
        </switchNavOne>
        <!-- 标签 -->
        <switchNavTwo :is_switchLable="header_label" @choose="_header_label_index"></switchNavTwo>
        <div class="relative">
            <template v-if="loadFlag">
                <ul class="goodsList" v-if="topTabBar" :style="{paddingTop:topDistanceMP}">
                    <template v-if="list.length > 0">
                        <li class="proList" v-for="(item, index) in list" :key="index">
                            <div class="proListUp">
                                <div class="proListUpLeft"><img :src="item.imgurl" @error="handleErrorImg(index)" />
                                </div>
                                <div class="proListUpRight">
                                    <div class="proTitle">{{ item.product_title }}</div>
                                    <div class="proSellData" @tap="_checkDetail(item.id)">
                                        <div class="sellMoney">{{store_default_currency_symbol}}{{ item.price}}</div>
                                        <div class="goodsInfo">
                                            <div class="stock" v-if="commodity_type == 0" :class="{ redColor:item.num < item.stockWarn }">
                                                {{language.myPro.kucun}}{{ item.num }}
                                            </div>
                                            <div v-if="status!=0 && commodity_type == 0">|</div>
                                            <div v-if="status!=0" class="volume">
                                                {{language.myPro.volume}}{{ item.volume }}
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>
                            <!-- 实物商品：item.status==1上架，2下架，3待审核 -->
                            <div class="proListDown">
                                <!-- 实物商品待审核状态 -->
                                <div class="proTip" v-if="item.mch_status==1||item.mch_status==3">
                                    {{item.mch_status==3?'审核失败':'审核中'}}
                                </div>
                                <div class="proTip" v-if="item.mch_status==4">{{'待提交'}}</div>

                                <!-- 删除商品 -->
                                <div class="proBtn" v-if="item.status != 2&&item.mch_status!=1&&item.mch_status!=4"
                                    @tap="_delPro(item.id)">{{language.myPro.delPro}}</div>
                                <!-- 虚拟商品不需要库存管理 -->
                                <!-- 库存管理 -->
                                <div class="proBtn" v-if="item.mch_status == 2 && commodity_type == 0" @tap="_addStock(item.id)">
                                    {{language.myPro.stock}}
                                </div>
                                <!-- 下架商品 -->
                                <div class="proBtn" v-if="item.status == 2" @tap="_upDownPro(item.status, item.id)">
                                    {{language.myPro.downPro}}
                                </div>
                                <!-- 上架商品 -->
                                <div class="proBtn" v-if="item.mch_status == 2 && item.status != 2"
                                    @tap="_upDownPro(item.status, item.id)">{{language.myPro.upPro}}</div>
                                <!-- 重新编辑商品 -->
                                <div class="proBtn" v-if="item.status != 2" @tap="_reEditor(item.id)">
                                    {{language.myPro.edit}}
                                </div>
                                <!-- 撤销审核 -->
                                <div class="proBtn" v-if="item.mch_status == 1" @tap="_checkStatus(item.id,0)">
                                    {{language.myPro.cancelAudit}}
                                </div>
                                <!-- 重新提交 -->
                                <div class="proBtn" v-if="item.mch_status == 4" @tap="_checkStatus(item.id,1)">
                                    {{language.myPro.submitAudit}}
                                </div>
                            </div>
                        </li>
                    </template>
                    <li class="proList proList_no" v-if="list.length == 0">
                        <image :src="noPro" mode="heightFix" alt="" class="noFindImg" />
                        <p>{{language.myPro.noGoods}}</p>
                    </li>
                    <uni-load-more v-if="list.length > 10" :loadingType="loadingType"></uni-load-more>
                </ul>
                <ul class="goodsList" v-else :style="{paddingTop:topDistanceMP}">
                    <template v-if="list.length < 0">
                        <li class="proList" v-for="(item, index) in list" :key="index">
                            <div class="proListUp">
                                <div class="proListUpLeft"><img :src="item.imgurl" @error="handleErrorImg(index)" />
                                </div>
                                <div class="proListUpRight" @tap="_checkDetail(item.id)">
                                    <div class="proTitle">{{ item.product_title }}</div>
                                    <div class="proSellData">
                                        <div class="sellMoney">{{store_default_currency_symbol}}{{ item.price }}</div>
                                        <div class="">{{language.myPro.kucun}}{{ item.num }}</div>
                                    </div>

                                </div>
                            </div>
                            <!-- mch_status 审核状态：1.待审核，2.审核通过，3.审核不通过，4.待提交 -->

                            <!-- status 状态 1:待上架 2:上架 3:下架 -->
                            <div class="proListDown">
                                <div class="proBtn" @tap="_delPro(item.id)"
                                    v-if="item.mch_status == 3 || item.mch_status == 4">{{language.myPro.delPro}}</div>
                                <div class="proBtn" @tap="_reEditor(item.id)"
                                    v-if="item.mch_status == 3 || item.mch_status == 4">{{language.myPro.edit}}</div>
                                <div class="proBtn" @tap="_checkStatus(item.id,0)" v-if="item.mch_status == 1">
                                    {{language.myPro.cancelAudit}}
                                </div>
                                <div class="proBtn" @tap="_checkStatus(item.id,1)" v-if="item.mch_status == 4">
                                    {{language.myPro.submitAudit}}
                                </div>
                            </div>
                        </li>
                    </template>
                    <li class="proList proList_no" v-else>
                        <img :src="noPro" alt="" class="noFindImg">
                        <p>{{language.myPro.noGoods}}</p>
                    </li>
                    <uni-load-more v-if="list.length > 10" :loadingType="loadingType"></uni-load-more>
                </ul>
            </template>
        </div>
        <div class="mask" v-if="mask_display1">
            <div class="mask_cont">
                <p>{{language.myPro.addStockDisc}}</p>
                <input type="number" v-model="addStockNum" :placeholder="language.myPro.addStockDisc"
                    :placeholder-style="placeStyle" />
                <div class="mask_button">
                    <div class="mask_button_left" @tap="_cancel()">{{language.myPro.cancel}}</div>
                    <div @tap="_confirm()">{{language.myPro.confirm}}</div>
                </div>
            </div>
        </div> 
        <view class="btn" v-if="this.$store.state.isFb && status == 2">
            <div class='bottom' @tap="toAddPro">{{language.myPro.toAdd}}</div>
        </view>

        <!-- 收藏操作提示 -->
        <view class="xieyi" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    {{is_sus_name}}
                </view>
            </view>
        </view>

        <!-- 物流模板弹窗 -->
        <view class="mask" v-if="is_freight">
            <view class="delMask">
                <p>{{language.storeCoupon.Tips}}</p>

                <text>{{language.storeCoupon.wlmubantishi}}</text>
                <view>
                    <view @tap="is_freight = false">{{language.storeCoupon.cancel}}</view>
                    <view style="color: #D73B48;" @tap="handleOk">{{language.storeCoupon.confirm}}</view>
                </view>
            </view>
        </view>
    </div>
</template>

<script>
    import switchNavOne from "@/components/aComponents/switchNavOne.vue";
    import switchNavTwo from '@/components/aComponents/switchNavTwo.vue'
    export default {
        data() {
            return {
                store_default_currency_code:'CNY',
                store_default_currency_symbol:'￥',
                store_default_exchange_rate:1,
                addStockNum: '',
                mask_display1: false,
                title: '我的商品',
                header: [],
                header_label: [],
                topTabBar: true,
                testImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yhqBg.png',
                noPro: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noPro.png',
                fastTap: true,
                shop_id: '',
                list: [],
                page: 1,
                pro_id: '',
                type: 2,
                placeStyle: 'color:#b8b8b8;font-size:28upx',
                loadFlag: false,

                up1: false,
                up2: false,
                status: 2,
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                //弹窗
                is_sus: false,
                is_sus_name: '下架成功',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                // 元素距上距离
                topDistanceMP: '0px',
                topDistanceH5: '0px',
                freightList: [],
                is_freight: false,
                loadingType: 0,
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                isPromiseExamine: true,
                is_Payment: true,
                commodity_type:0,//0实物，1虚拟
            };
        },
        components: {
            switchNavOne,
            switchNavTwo
        },
        onLoad(option) {
            this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
            this.up1 = uni.getStorageSync('_up1')
            this.up2 = uni.getStorageSync('_up2')
            this.header_label[0] = this.language.myPro.beenOn
            this.header_label[1] = this.language.yushou_details.dsj
            this.header_label[2] = this.language.myPro.shelves
            this.header_label[3] = this.language.myPro.dsh
            
            let storeCurrency = uni.getStorageSync('storeCurrency');
            this.store_default_exchange_rate = storeCurrency.exchange_rate;
            this.store_default_currency_symbol = storeCurrency.currency_symbol;
            this.store_default_currency_code = storeCurrency.currency_code;
        },
        mounted() {
            // 设置元素距离上边的距离
            uni.createSelectorQuery().in(this).select(".headInfo").boundingClientRect(data => {
                this.heightEle = data?.height
                this.topDistanceMP = this.heightEle + 'px'
            }).exec()
        },
        onShow() {
            const {goodsType} = this.language
			this.header= [goodsType.swsp,goodsType.xnsp]
			this.header_label = [goodsType.ysj,goodsType.dsj,goodsType.yxj,goodsType.dsh]
			
            this.isLogin(() => {})
            // this.list = []
            this.page = 1
            this._axios();
            // 获取物流模板
            this.handleGetFreight()
        },
        onReachBottom: function() {
            //是否还存在下一页商品
            if (this.loadingType != 0) {
                return;
            }
            this.page += 1;
            var data = {

                api: "mch.App.Mch.My_merchandise_load",
                shop_id: this.shop_id,
                page: this.page,
                                            commodity_type: this.commodity_type, // 商品类型 0.实物商品 1.虚拟商品
                status: this.status, // 状态 2:上架 3:下架 0:待审核
            };
            if (this.topTabBar) {
                //判断获取哪种商品
                data.type = 2;
                // 待审核商品（type值1,3为审核中以及审核失败）
                if (this.status == 0) {
                    data.type = '1,3'
                }
            } else {

            }

            if (this.list.length > 0) {
                this.$req.post({
                    data
                }).then(res => {
                    let {
                        code,
                        data,
                        message
                    } = res;

                    if (code == 200) {
                        if (this.page == 1) {
                            this.list = data.list;
                        } else {
                            this.list = this.list.concat(data.list);
                        }
                        if (data.list.length < 10) {
                            this.loadingType = 2;
                        } else {
                            this.loadingType = 0;
                        }
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
            }
        },
        methods: {
            handleErrorImg(index) {
                setTimeout(() => {
                    this.list[index].imgurl = this.ErrorImg
                }, 0)
            },
            // 获取物流模板信息
            handleGetFreight() {
                let data = {

                    api: "mch.App.Mch.Upload_merchandise_page",

                    shop_id: this.shop_id,
                }
                this.$req.post({
                    data
                }).then(res => {
                    let {
                        code,
                        data,
                        message
                    } = res
                    if (code == 200) {
                        this.freightList = data.freight_list
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
            },
            /**
             * 类型切换
             */
            _header_index(index) {
                this.commodity_type = index
                this.page = 1;
                this.loadFlag = false;
                this._axios();
            },
            /**
             * 导航栏切换
             */
            _header_label_index(status) {
                let type
                switch (status) {
                    case 0:
                        type = 2
                        break;
                    case 1:
                        type = 1
                        break;
                    case 2:
                        type = 3
                        break;
                    case 3:
                        type = 0
                        break;
                }
                this.status = type;
                if (type == 0) {
                    this.type = '1,3,4'
                } else {
                    this.type = 2
                }
                this.page = 1;
                this.loadFlag = false;
                this._axios();
            },
            // 当物流模板为空
            handleOk() {
                uni.navigateTo({
                    url: '/pagesA/myStore/freight_add?type=add'
                })
            },
            toAddPro() {
                // 增加判断-查看是否有物流模板，如果没有，则需先添加物流模板再做添加商品操作
                // if(this.freightList.length<=0){
                //     this.is_freight = true
                // }else{
                //     uni.navigateTo({
                //         url: '/pagesA/myStore/uploadPro'
                //     })
                // }
                if (this.is_Payment) {
                    // 保证金是否处于退还审核
                    if (this.isPromiseExamine) {
                        if (this.freightList.length <= 0) {
                            this.is_freight = true
                        } else {
                            uni.navigateTo({
                                url: '/pagesA/myStore/uploadPro?commodity_type=' + this.commodity_type
                            })
                        }
                    } else {
                        uni.showModal({
                            title: this.language.myStore.bzjth_title,
                            content: this.language.myStore.bzjth_content,
                            confirmColor: "#D73B48",
                            confirmText: this.language.showModal.confirm,
                            showCancel: false,
                            success: (e) => {
                                return;
                            },
                        });
                    }
                } else {
                    uni.showModal({
                        title: this.language.myStore.bzj_title,
                        content: this.language.myStore.bzj_content,
                        confirmColor: "#D73B48",
                        confirmText: this.language.showModal.confirm,
                        cancelText: this.language.showModal.cancel,
                        success: (e) => {
                            if (e.confirm) {
                                uni.navigateTo({
                                    url: "/pagesC/bond/payText",
                                });
                            } else {
                                return;
                            }
                        },
                    });
                }
            },
            changeLoginStatus() {
                this._axios();
            },
            _cancel() {
                this.mask_display1 = false;
                this.addStockNum = '';
            },
            _checkStatus(id, val) {
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;
                uni.showModal({
                    title: this.language.order.myorder.ts,
                    content: val === 0 ? this.language.order.myorder.sfcxf : this.language.order.myorder
                        .qdtjshm,
                    confirmText: this.language.order.myorder.confirm,
                    cancelText: this.language.order.myorder.cancel,
                    confirmColor: "#D73B48 !important",
                    cancelColor: '#333333 !important',
                    success: res => {
                        if (res.confirm) {
                            this.$req
                                .post({
                                    data: {

                                        api: "mch.App.Mch.Submit_audit",
                                        shop_id: this.shop_id,
                                        p_id: id
                                    }
                                })
                                .then(res => {
                                    let {
                                        code,
                                        data,
                                        message
                                    } = res
                                    if (code == 200) {

                                        if (val == 0) {
                                            this.is_sus_name = this.language.myPro.cxcg
                                        } else {
                                            this.is_sus_name = this.language.myPro.tjcg
                                        }
                                        this.is_sus = true
                                        setTimeout(() => {
                                            this.is_sus = false
                                            this.fastTap = true;
                                            this._axios();
                                        }, 1500);
                                    } else {
                                        uni.showToast({
                                            title: message,
                                            duration: 1500,
                                            icon: 'none'
                                        });
                                        this.fastTap = true;
                                    }
                                })
                                .catch(error => {
                                    this.fastTap = true;
                                });
                        } else if (res.cancel) {
                            this.fastTap = true;
                        }
                    }
                });

            },
            _reEditor(p_id) {
                uni.navigateTo({
                    url: '/pagesA/myStore/uploadPro?pageStatus=editor&p_id=' + p_id
                });
            },
            _addStock(id) {
                uni.navigateTo({
                    url: 'addStock?p_id=' + id
                });
            },
            _upDownPro(type, id) {
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;

                this.$req
                    .post({
                        data: {


                            api: "mch.App.Mch.My_merchandise_status",
                            shop_id: this.shop_id,
                            status: type,
                            p_id: id
                        }
                    })
                    .then(res => {
                        let {
                            code,
                            data,
                            message
                        } = res;
                        if (code == 200) {
                            if (type == 2) {
                                this.is_sus_name = this.language.myPro.xjcg
                            } else {
                                this.is_sus_name = this.language.myPro.sjcg
                            }
                            this.is_sus = true
                            setTimeout(() => {
                                this.is_sus = false
                                this.fastTap = true;
                                this._axios();
                            }, 1500);
                        } else {
                            this.fastTap = true;
                            uni.showToast({
                                title: message,
                                duration: 1500,
                                icon: 'none'
                            });
                        }
                    })
                    .catch(error => {
                        this.fastTap = true;
                    });
            },
            _delPro(id) {

                uni.showModal({
                    title: this.language.showModal.hint,
                    content: this.language.showModal.delPro,
                    confirmText: this.language.showModal.confirm,
                    cancelText: this.language.showModal.cancel,
                    cancelColor: '#333333 !important',
                    confirmColor: '#D73B48 !important',
                    success: (e) => {
                        if (e.confirm) {
                            if (!this.fastTap) {
                                return;
                            }
                            this.fastTap = false;

                            this.$req
                                .post({
                                    data: {


                                        api: "mch.App.Mch.Del_my_merchandise",
                                        shop_id: this.shop_id,
                                        p_id: id
                                    }
                                })
                                .then(res => {
                                    let {
                                        code,
                                        data,
                                        message
                                    } = res;
                                    if (code == 200) {

                                        this.is_sus_name = this.language.myPro.delSuccess
                                        this.is_sus = true
                                        setTimeout(() => {
                                            this.is_sus = false
                                            this.fastTap = true;
                                            this._axios();
                                        }, 1500);
                                    } else {
                                        uni.showToast({
                                            title: message,
                                            duration: 1500,
                                            icon: 'none'
                                        });
                                        this.fastTap = true;
                                    }
                                })
                                .catch(error => {
                                    this.fastTap = true;
                                });
                        }
                    }
                })

            },
            _checkDetail(id) {
                uni.navigateTo({
                    url: '/pagesA/myStore/uploadPro?pageStatus=see&p_id=' + id
                });
            },
            changeTabBar(type) {
                if (type) {
                    this.topTabBar = true;
                } else {
                    this.topTabBar = false;
                }
                this.list = [];
                this.loadingType = 0;
                this.page = 1;
                this.loadFlag = false;
                this._axios();
            },
            _axios() {
                uni.showLoading({
                    title: this.language.toload.loading
                });


                this.$req
                    .post({
                        data: { 
                            api: "mch.App.Mch.My_merchandise",
                            shop_id: this.shop_id,
                            type: this.type, //type为2时实物商品的上下架，为1，3实物商品的待审核
                            commodity_type: this.commodity_type, // 商品类型 0.实物商品 1.虚拟商品
                            status: this.type == 2 ? this.status : '', // 状态 0:上架 1:下架 2:待审核
                        }
                    })
                    .then(res => {
                        let {
                            code,
                            data,
                            message
                        } = res
                        this.loadFlag = true;
                        uni.hideLoading();
                        if (code == 200) {
                            this.isPromiseExamine = data.isPromiseExamine;
                            this.is_Payment = data.is_Payment;
                            if (this.page == 1) {
                                this.list = data.list;
                            } else {
                                this.list = this.list.concat(data.list);
                            }
                            if (data.list.length < 10) {
                                this.loadingType = 2;
                            } else {
                                this.loadingType = 0;
                            }
                        } else {
                            uni.showToast({
                                title: message,
                                duration: 1500,
                                icon: 'none'
                            });
                        }
                    });
            }
        }
    };
</script>

<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped>
    @import url("@/laike.less");
    @import url('../../static/css/myStore/myPro.less');
    .goodsList{
        margin-bottom: 80px;
    }
    .delMask {
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
        width: 640rpx;
        // height: 382rpx;
        background: rgba(255, 255, 255, 1);
        border-radius: 23rpx;
    }

    .delMask>p {
        text-align: center;
        padding: 64rpx 48rpx 0 48rpx;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .delMask>text {
        display: block;
        text-align: center;
        color: #999;
        font-size: 32rpx;
        line-height: 48rpx;
        padding: 24rpx 48rpx 64rpx 48rpx;
    }

    .delMask>view {
        display: flex;
    }

    .delMask>view view {
        flex: 1;
        height: 93rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #333;
        font-size: 34rpx;
        box-sizing: border-box;
        border-top: 1px solid #EEEEEE;
    }

    .delMask>view view:last-child {
        border-left: 1px solid #EEEEEE;
        color: #020202;
    }

    .xieyi {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, .5);
        z-index: 99;
        display: flex;
        justify-content: center;
        align-items: center;

        >view {
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #FFFFFF;
            border-radius: 24rpx;

            .xieyi_btm {
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0, 0, 0, .1);
                font-weight: bold;
                font-size: 32rpx;
            }

            .xieyi_title {
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 32rpx;

                font-weight: 500;
                color: #333333;
                line-height: 44rpx;
                margin-top: 64rpx;
                margin-bottom: 32rpx;
                font-weight: bold;
                font-size: 32rpx;
            }

            .xieyi_text {
                width: 100%;
                max-height: 236rpx;
                overflow-y: scroll;
                padding: 0 32rpx;
                box-sizing: border-box;
            }
        }
    }

</style>
