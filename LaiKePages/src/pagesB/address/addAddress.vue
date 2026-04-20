<template>
    <div style='height: 100vh;background-color: #F4F5F6;'>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <ul class='message'>
            <li>
                <span class="leftText"><span class="must">*</span>{{language.addAddress.consignee}}</span>
                <input type="text" placeholder-style="color:#999999;" :placeholder="language.addAddress.consignee_placeholder" v-model="user_name"/>
            </li>
            <li>
                <span class="leftText"><span class="must">*</span>{{language.addAddress.contactinfo}}</span>
                <div class="rightInput1_area">
                    <div class="uni-input__area" @tap.shop="navToAddress">
                        {{ code2?'+' + code2 : '国家/地区' }}
                        <image :src="down" mode="aspectFill"></image>
                    </div>
                    <input type="number" placeholder-style="color:#999999;" :placeholder="language.addAddress.contactinfo_placeholder" v-model="phone"/>
                </div>
            </li>
            <li v-if="international" class="flex" @click="showMulLinkageThreePicker()">
                <div style='display: flex;align-items: center;'>
                    <span class="leftText"><span class="must">*</span>{{language.addAddress.region}}</span>
                    <input disabled type="text" @focus="hideKeyboard()" :placeholder="language.addAddress.region_placeholder" v-model="city_all"
                           placeholder-style="color:#999999;"/>
                </div>
                <img class='arrow' :src="jiantou"/>
            </li>
            <li v-if="international">
                <span class="leftText"><span class="must">*</span>{{language.addAddress.addressinfo}}</span>
                <input type="text" placeholder-style="color:#999999;" :placeholder="language.addAddress.addressinfo_placeholder" v-model="address"/>
            </li>

            <!-- 国际化地址 -->
            <li v-if="!international">
                <span class="leftText"><span class="must">*</span>街道地址</span>
                <input type="text" placeholder-style="color:#999999;" placeholder="街道地址、邮政信箱、公司名称、转交方、公寓、套房、单元、大厦、楼层等" v-model="address"/>
            </li>
            <li v-if="!international">
                <span class="leftText"><span class="must">*</span>城市</span>
                <input type="text" placeholder-style="color:#999999;" placeholder="请输入所在城市" v-model="city"/>
            </li>
            <li v-if="!international">
                <span class="leftText"><span class="must">*</span>州/省/地区</span>
                <input type="text" placeholder-style="color:#999999;" placeholder="请输入州/省/地区" v-model="province"/>
            </li>
            <li >
                <span class="leftText"><span class="must">*</span>邮政编码</span>
                <input type="text" placeholder-style="color:#999999;" placeholder="请输入邮政编码" v-model="code"/>
            </li>
            
            <li class="flex adddis">
                <span class="leftText">{{language.addAddress.defaultaddress}}</span>
                <switch style="transform:scale(0.75);margin-right: -16rpx;" :checked="modify_default_flag" color="#FA5151" @change="_is_default" />
            </li>
        </ul>
        
        <view class="del" v-if="addr_id">
            <div class='setup-buttom storage' @tap="_preserve_address">{{language.addAddress.bzdz}}</div>
            <div class='kon-buttom k_storage' @tap="_delAddress">{{language.addAddress.scdz}}</div>
        </view>
        
        <view class="btn" v-if="!addr_id">
            <div class='setup-buttom storage' @tap="_preserve_address">{{language.addAddress.saveBtn}}</div>
        </view>

        <mpvue-city-picker :themeColor="themeColor" ref="mpvueCityPicker" :pickerValueDefault="cityPickerValueDefault"
                           @onConfirm="onConfirm"></mpvue-city-picker>
                           
        <!-- 提示 -->
        <view class="tishi" style="position: fixed;top: 50%;margin-top: -46rpx;left: 50%;margin-left: -128rpx;" v-if="is_tishi">
            <view style="width: 256rpx;height: 92rpx;background-color: rgba(0, 0, 0, .5);border-radius: 48rpx;color: #ffffff;display: flex;align-items: center;justify-content: center;">
                <span style="font-size: 16px;font-weight: 400;">{{is_tishi_content}}</span>
            </view>
        </view>
        <!-- 提示 -->
        <showToast
            :is_showToast="is_showToast" 
            :is_showToast_obj="is_showToast_obj">
        </showToast>
    </div>
</template>

<script>
    import showToast from "@/components/aComponents/showToast.vue"
    import { mapMutations } from 'vuex'
    import mpvueCityPicker from '@my-miniprogram/src/components/mpvue-citypicker/mpvueCityPicker';
    import {
        telephone
    } from '../../common/landing.js'
    import {img} from "@/static/js/login/imgList.js";
    export default {
        data () {
            return {
                //提示
                is_tishi:false,
                is_tishi_content:'添加成功',
                //
                addNum: '',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                fastTap: true,
                title: '编辑地址',
                selected: '1', //组件的默认值
                cilck_index: -1, //选择省市的 index
                city_z: false, //显示隐藏状态
                city_addre: {}, //选择完省市区后存的信息
                province: '', //省
                city: '', //市
                area: '', //区
                is_showToast: 0,//
                is_showToast_obj: {},//
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                GroupID: '', //省id
                Group_name: '', //省名称
                city_id: '', //市id
                city_name: '', //城市名称
                code: '', //邮编
                area_name: '', //区名称
                area_id: '', //区id
                user_name: '', //用户名
                phone: '', //电话号码
                address: '', //详细地址
                state_addre: '', //判断从添加地址(1)还是编辑地址(2)
                addr_id: '', //地址id
                city_all: '', //最终选择省市区的值
                is_default: 0, //是否默认地址
                international: false, //是否国际化地址
                circle_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png', //图片地址
                circle_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehei2x.png', //图片地址
                modify_default: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/modify_default.png', //图片地址
                modify_default_flag: false,
                g_name: '省份',
                c_name: '城市',
                a_name: '地区',
                themeColor: '#D73B48',
                cityPickerValueDefault: [0, 0, 0],
                pickerText: '',
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ],
                code2: '',
                num3: '',
                down: img(this).down,
            }
        },
        onLoad (option) {
            this.addNum = option.addNum
            this.state_addre = option.state_addre
            this.addr_id = option.addr_id
 
           if (this.state_addre == 2) {
               this._axios()
           } 
        }, 
        
        onShow () {
            const pages = getCurrentPages();
            // return
            
            if(uni.getStorageSync('diqu')){
                let diqu = JSON.parse(uni.getStorageSync('diqu'))
                this.code2 = diqu?.code2
                this.num3 = diqu?.num3
                if (this.code2 === '86' || this.code2 === '852') {
                   this.international = true
                } else {
                   this.international = false
                }
                uni.removeStorageSync('diqu')
            } 

            if (this.state_addre == 2) {
                this.title = this.language.addAddress.title1
            } else {
                this.title = this.language.addAddress.title
            }

            this.isLogin()
            if(this.state_addre == 1){
               this.addressList()
            }
        },
        methods: {
            navToAddress(){
                uni.navigateTo({
                    url:'/pagesD/login/countryAndRegion',
                })  
            },
             // 删除地址
            _delAddress() {
                let addr_id = this.addr_id
                
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
                        me.is_showToast = 1
                        me.is_showToast_obj.imgUrl = me.sus
                        me.is_showToast_obj.title = me.language.receivingAddress.delete_success
                        setTimeout(()=>{
                            me.is_showToast = 0
                            uni.navigateBack({
                                delta: 1
                            })
                        }, 1000)
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                }).catch((e) => { me.fastTap = true })
                this.lkdelModel = false
            },
            changeLoginStatus () {
                this.login_status = 1
                this._axios()
            },
            hideKeyboard () {
                uni.hideKeyboard()
            },
            showMulLinkageThreePicker () {
                this.$refs.mpvueCityPicker.show()
            },
            onConfirm (e) {
                this.city_all = e.label
            },
            ...mapMutations({
                address_id: 'SET_ADDRESS_ID'
            }),
            // 新增地址成功后，根据来源页修正页面栈，避免返回到首页
            goAfterAddSuccess(addressId = '') {
                const pages = getCurrentPages()
                const prevPage = pages && pages.length > 1 ? pages[pages.length - 2] : null
                const prevRoute = prevPage && prevPage.route ? prevPage.route : ''
                const fromAddressList = prevRoute.indexOf('pagesB/address/receivingAddress') > -1

                if (fromAddressList) {
                    uni.navigateBack({
                        delta: 1
                    })
                    return
                }

                let url = '/pagesB/address/receivingAddress?state_manage=1'
                if (addressId) {
                    url += '&addre_id=' + addressId
                }
                uni.redirectTo({
                    url
                })
            },
            _axios () {
                var me = this
                var data = {
                    api: 'app.address.up_addsindex',
                    addr_id: this.addr_id
                }
                this.$req.post({data}).then(res => {
                    me.fastTap = true
                    if (res.code == 200) {
                        let { data: { address: { cpc, address,city, code, name, tel, is_default }, city_id, county, province }  } = res
                        me.user_name = name
                        me.phone = tel
                        me.code = code
                        me.cpc = cpc
                        me.code2 = cpc
                        me.address = address
                        me.province = res.data.address.province
                        me.city = city
                        me.city_all = province + '-' + city_id + '-' + county
                        me.is_default = is_default
                        if(me.state_addre == 2&&is_default == 1){
                            me.modify_default_flag = true
                        }
                        if (cpc == '86' ||cpc == '852') {
                          this.international = true
                        } else {
                          this.international = false
                        }
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }  
                })
            },
            async addressList(){
                var data = {
                    api: 'app.address.index',
                }
                
                let res = await this.$req.post({data})
                if (res.data.adds.length<=0) {
                    this.modify_default_flag = true
                }
            },
            //默认地址
            _is_default () {
                if (this.is_default === 1) {
                    this.is_default = 0
                } else {
                    this.is_default = 1
                }
            },
            //保存
            _preserve_address () {
                var me = this
                if (!this.fastTap) {
                    return
                }
             
                this.fastTap = false
                if (this.user_name && this.phone && this.address) {
                    let reg = /^[\u4e00-\u9fa5_a-zA-Z0-9\s*]+$/
                    if(!reg.test(this.user_name)){
                        this.fastTap = true
                        uni.showToast({
                            title: me.language.addAddress.Receiving_verification,
                            icon: 'none'
                        })
                        return
                    }
                    
                    
                    var data = {
                        api: 'app.address.',
                        user_name: this.user_name,
                        mobile: this.phone,
                        place: this.city_all,
                        is_default: this.is_default,
                        address: this.address,
                        cpc: this.code2,
                        code: this.code,
                        province: this.province,
                        city: this.city,
                    }
                    if (this.state_addre == 2) {
                        data.api = data.api+'up_adds'
                        data.addr_id = this.addr_id
                        this.$req.post({data}).then(res => {
                            let { code, message } = res
                            if (code == 200) {
                                me.is_showToast = 1
                                me.is_showToast_obj.imgUrl = me.sus
                                me.is_showToast_obj.title = me.language.addAddress.modify_success
                                setTimeout(()=>{
                                    me.is_showToast = 0
                                },2000) 
                                
                                if (me.is_default == 1) {
                                    me.$store.state.address_id_def = me.addr_id
                                }
                                uni.setStorageSync('lkt_address_id_def', me.addr_id)
                                setTimeout(function () {
                                    uni.navigateBack({
                                        delta: 1
                                    })
                                    me.fastTap = true
                                }, 1500)
                            } else {
                                me.fastTap = true
                                uni.showToast({
                                    title: message,
                                    duration: 1500,
                                    icon: 'none'
                                })
                            }    
                        })
                        
                    } else if (this.state_addre == 1) {

                        data.api = data.api + 'SaveAddress'
                        this.$req.post({data}).then(res => {
                            let { address_id, code, message } = res
                            if (code == 200) {
                                if (me.addNum == 0) {
                                    // 甘鹏说不获取，原因未知 禅道52843 --xuxiong
                                    // me.$store.state.address_id_def = address_id
                                } else if (data.is_default == 1) {
									//
                                    //me.$store.state.address_id_def = address_id
                                    // 告诉上一页，选择了默认地址
                                    uni.setStorageSync('address_default', 1)
                                }
                                me.is_showToast = 1
                                me.is_showToast_obj.imgUrl = me.sus
                                me.is_showToast_obj.title = me.language.addAddress.add_success
                                setTimeout(()=>{
                                    me.is_showToast = 0
                                },2000) 
                                
                                setTimeout(function () {
                                    me.goAfterAddSuccess(address_id)
                                    me.fastTap = true
                                }, 1500)
                            } else {
                                me.fastTap = true
                                uni.showToast({
                                    title: message,
                                    duration: 1500,
                                    icon: 'none'
                                })
                            }
                        })
                        
                    }
                } else {
                    this.fastTap = true
                    uni.showToast({
                        title: me.language.addAddress.complete_info,
                        duration: 1000,
                        icon: 'none'
                    })
                }
            },
        },
        components: {
            mpvueCityPicker,
            showToast
        }
    }
</script>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/address/addAddress.less");
    input{
        font-size: 32rpx;
        color: #333333;
    }
    .rightInput1_area{
        display: flex;
        align-items: center;
        flex: 1;
    }
    .uni-input__area {
        display: flex;
        align-items: center;
        width: 200rpx;
        font-size: 32rpx;
        color: #333333;
        image {
          width: 10rpx;
          height: 10rpx;
          margin-left: 8rpx;
        }
    }
</style>
