<template>
    <div class="receivingAddress" style="min-height: 100vh;" :style="address?'background-color: #F6f6f6;':''">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <!-- 标题 -->
        <div style='position: relative;'>
            <heads :title="language.receivingAddress.title" :ishead_w="2" :returnR="returnR" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        </div>
        
        
        <!-- 地址栏 -->
        <toload :load="load">
            <ul class='address' v-if='address.length'>
                <li v-for="(item,index) in address" :key='item.id'>
                    <div class='address_top' @tap="_state_manage(item)">
                        <div style="flex: 1;margin-right: 18rpx;">
                            <div class='address_user'>
                                <span class='address_ms' v-if='item.is_default==0?false:true'>{{language.receivingAddress.defaults}}</span>
                                <span style="margin-right: 32rpx;font-weight: bold;">{{item.name}}</span>
                                <span style="font-weight: bold;">+{{item.cpc}} {{item.tel}}</span>
                            </div>
                            <div class='address_xs'>
                                <p>{{item.address_xq}}</p>
                            </div>
                        </div>
                        <div @tap.stop="navTo('addAddress?state_addre=2&addr_id='+item.id)" style="border-left: 1px solid rgba(0, 0, 0, .1);">
                            <span style="margin-left: 16rpx;color: #D73B48;font-size: 28rpx;">{{language.receivingAddress.edit}}</span>
                        </div>
                    </div>
                    
                </li>
                <!-- #ifdef MP -->
                <p style="height: 100rpx;"></p>
                <!-- #endif -->
            </ul>
            <ul class='not-address' v-if='notDeliveryAddress.length'>
                <div  class="out-area-top">{{language.receivingAddress.cpsfw}}</div>
                <li class="out-area" v-for="(item,index) in notDeliveryAddress" :key='item.id' >
                    <div class='address_top'  @tap="showNotDelivery()">
                        <div style="flex: 1;margin-right: 18rpx;">
                            <div class='address_user'>
                                <span class='address_ms' v-if='item.is_default==0?false:true'>{{language.receivingAddress.defaults}}</span>
                                <span style="margin-right: 32rpx;font-weight: bold;">{{item.name}}</span>
                                <span style="font-weight: bold;">+{{item.cpc}} {{item.tel}}</span>
                            </div>
                            <div class='address_xs'>
                                <p>{{item.address_xq}}</p>
                            </div>
                        </div>
                        <div @tap.stop="navTo('addAddress?state_addre=2&addr_id='+item.id)" style="border-left: 1px solid rgba(0, 0, 0, .1);">
                            <span style="margin-left: 16rpx;color: #D73B48;font-size: 28rpx;">{{language.receivingAddress.edit}}</span>
                        </div>
                    </div>
                    
                </li>
                <!-- #ifdef MP -->
                <p style="height: 100rpx;"></p>
                <!-- #endif -->
            </ul>
            <!-- 当没有设置地址栏的时候，页面显示添加地图图片提示 -->
            <div class="address_wu" v-if="address.length == 0 && notDeliveryAddress.length == 0">
                <img :src="dizhiNo"/>
                <p>{{language.receivingAddress.zwsj}}～</p>
            </div>
        </toload>
        
        <view class="notDlveryModel" v-if="notDlveryShow">
            <view class="notDlveryModel-card">
                <image :src="address_img"></image>
                <div class="text-top">{{language.receivingAddress.gdzbz}}</div>
                <div class="text-middle">{{language.receivingAddress.qqzqr}}</div>
                <div class="text-bottom"  @tap="closeNotDlveryModel()">{{language.receivingAddress.zdl}}</div>
            </view>
        </view>
        
        <!-- 底部按钮 -->
        <template v-if="address.length == 0 && notDeliveryAddress.length == 0">
             <view class="btn">
                <div class='bottom' v-if="address" @tap="navTo('addAddress?state_addre=1')">{{language.receivingAddress.addnewaddress}}</div>
            </view>
        </template>
        <template v-else>
            <view class="btn">
                <div class='bottom' v-if="address" @tap="navTo('addAddress?state_addre=1')">{{language.receivingAddress.addnewaddress}}</div>
            </view>
        </template>
        <lkdelModel :content="text" :canceltext="language.receivingAddress.cancel" :confirmtext="language.receivingAddress.confirm" v-model="lkdelModel" @on-click="_delAddress"/>
    </div>
</template>

<script>
    import {mapMutations} from 'vuex'
    import lkdelModel from '@/components/delModel.vue'

    export default {
        data () {
            return {
                notDeliveryAddress:'',
                product:'',
                title: '收货地址',
                dizhiNo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/dizhiNo.png',
                add: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/tianjiadizhi2x.png',
                edtAdd: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/bianjidizhi2x.png',
                delAdd: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/shanchudizhi2x.png',
                address_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/seconds/address.png',
                fastTap: true,
                // manage: '', //管理状态
                del: '', //删除编辑状态
                del_index: -1, //删除地址的index
                state_manage: '', //判断从个人中心页面进(1)，还是订单页面进(2) , 还是从竞拍详情进，3
                address: '', //接受后台返回地址
                circle_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png',
                circle_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehei2x.png',
                order_id: '',
                re_img: '',
                addre_id: '', //地址id
                address_m: '管理',
                flag: true,
                lkdelModel: false,
                notDlveryShow: false,
                delobj: {},
                form: '',//来自哪个页面
                text: '',
                load: false,
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ],
                returnR: '',
            }
        },
        onShow () {
            this.isLogin()
            this._axios() 
            this.address_id = this.$store.state.address_id
            
            this.address_m = this.language.receivingAddress.management
        },
        onLoad (option) {
            this.order_id = this.$store.state.order_id
            if(option.product != undefined){
                this.product = option.product
            }
            if(option.form){
                this.form = option.form
            }
            this.state_manage = option.state_manage
            this.addre_id = option.addre_id
            // 下单流程进入地址列表时，返回固定回到上一页（避免 H5 栈异常回首页）
            if (String(this.state_manage) === '1') {
                this.returnR = '1'
            } else {
                this.returnR = ''
            }
            uni.setStorageSync('canshu', 'false')
            if (this.state_manage == 1) {
                this.del = false
                this.re_img = true
            } else if (this.state_manage == 2) {
                this.del = true
                this.re_img = false
            }
        },
        components: {
            lkdelModel
        },
        methods: {
            closeNotDlveryModel(){
                this.notDlveryShow = false;
            },
            showNotDelivery(){
                this.notDlveryShow = true;
            },
            changeLoginStatus () {
                this._axios()
            },
            //从订单页面进入点击调回前一页
            _state_manage (item) {
                this.addre_id = item.id
                var me = this
                if(item.id > 0 ){
                    me.$store.state.address_id = item.id
                    me.$store.state.address_id_new = item.id  // 缓存选择新地址id
                }
                uni.setStorageSync('chooseAddress', item)
                if(this.form != 'myInfo'){
                    this.navBack()
                }
            },
            //点击管理切换状态
            _manage () {
                this.del = !this.del
                if (this.del) {
                    this.address_m = this.language.receivingAddress.complete
                } else {
                    this.address_m = this.language.receivingAddress.management
                }
            },
            delAddress (addr_id, index) {

                this.delobj = {
                    addr_id,
                    index
                }
                if (this.address[index].is_default == 1) {
                    this.text = this.language.receivingAddress.confirm_delete
                } else {
                    this.text = ''
                }

                this.lkdelModel = true
            },
            // 删除地址
            _delAddress () {

                let {
                    addr_id,
                    index
                } = this.delobj

                if (!this.fastTap) {
                    return
                }

                this.fastTap = false
                var me = this

                var data = {
                    api: 'app.address.del_adds',
                    
                    addr_id: addr_id
                }


                this.$req.post({data}).then(res => {
                    me.fastTap = true

                    let {
                        code
                    } = res
                    if (code == 200) {
                        if (this.address.length === 1) {
                            me.$store.state.address_id_def = ''
                        } else if (this.address[index].is_default === 1) {
                            me.$store.state.address_id_def = ''
                            uni.setStorageSync('lkt_address_id_def', '')
                        }
                        uni.showToast({
                            title: me.language.receivingAddress.delete_success,
                            icon: 'none',
                            duration: 1000
                        })
                       
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                     me._axios()
                }).catch((e) => { me.fastTap = true })
                
                
                this.lkdelModel = false
            },
            //设置默认地址
            async _default (address_id, index) {
                if (!this.fastTap) {
                    return
                }
                this.fastTap = false
                var me = this
                var data = {
                    api: 'app.address.set_default',
                    addr_id: address_id
                }
                
                try {
                    let {code, message} = await this.$req.post({data})
                    if (code == 200) {
                        for (let i = 0; i < me.address.length; i++) {
                            me.address[i].is_default = 0
                        }
                        me.address[index].is_default = 1
                        me.$store.state.address_id_def = me.address[index].id
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                    me.fastTap = true
                } catch (e) {
                    me.fastTap = true
                }
            },
            async _axios () {
                var me = this
                var data = {
                    api: 'app.address.index',
                    product:this.product
                    
                }
                
                let res = await this.$req.post({data})
                let adds = []
                // res = res.data
                this.load = true;
                if (res.code == 200) {
                    
                    adds = res.data.adds || []
                    
                    let notDeliveryAddress = res.data.adds0 || []
                    
                    me.address = adds
                    me.notDeliveryAddress = notDeliveryAddress
                    // 如果创建的新地址选择了默认
                    if (uni.getStorageSync('address_default')) {
                        for (let i = 0; i < adds.length; i++) {
                            if (adds[i].is_default == 1) {
                                me.addre_id = adds[i].id
                                break
                            }
                        }
                        uni.removeStorageSync('address_default')
                    }
                    //排序 找到默认地址 并排序到第一个
                    me.address.unshift(...me.address.splice(me.address.findIndex(i => i.is_default==1), 1))
                } else {
                    
                    me.address = ''
                    me.notDeliveryAddress = ''
                }

                if (adds.length > 0 && me.$store.state.address_id_def == '') {
                    if (me.$store.state.address_id_def == '') {
                        for (var k in adds) {
                            if (adds[k].is_default == 1) {
                                me.$store.state.address_id_def = adds[k].id
                                uni.setStorageSync('lkt_address_id_def', adds[k].id)
                                me.$store.state.address_id = adds[k].id
                            }
                        }
                    } else {
                        me.$store.state.address_id_def = me.$store.state.address_id_def
                        uni.setStorageSync('lkt_address_id_def', me.$store.state.address_id_def)
                    }
                }
            },
            ...mapMutations({
                address_id: 'SET_ADDRESS_ID'
            })
        },
    }
</script>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/address/receivingAddress.less");
    .btn{
        position: fixed;
        bottom: 0;
        width: 100%;
        height: 124rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffffff;
        box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
    }
</style>
