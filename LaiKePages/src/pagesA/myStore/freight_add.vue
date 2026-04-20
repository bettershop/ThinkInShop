<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <template v-if="enterParam">
            <heads  
                ishead_w="2" 
                :bgColor="bgColor" 
                titleColor="#333333" 
                :callback="showParam"
                :title="language.freight_add.title ">
            </heads>
        </template>
        <template v-if="!enterParam">
            <heads 
                ishead_w="2" 
                :bgColor="bgColor" 
                titleColor="#333333" 
                :title="title">
            </heads>
        </template>
        <template v-if="enterParam">
            <view class="head-top">
                <view class="container_row  enter" style="border-bottom:none;">
                    <span class="red-requre">*</span>
                    <view class="container_row_left">{{language.freight_add.name}}</view>
                    <view class="container_row_right">
                        <input :disabled="type=='see'" v-model="pre_name" type="text"
                            :placeholder="language.freight_add.name_placeholder" maxlength="20">
                        <image class="clear" v-if="pre_name" @tap="clear('pre_name')" :src="clearImg"></image>
                    </view>
                </view>
                <view class="container_bottomBtn" :class="{disable:!pre_name}" v-if="show_type != 'see'">
                    <view class="one" @tap="changeName()">{{language.freight_add.save}}</view>
                </view>
            </view>
        </template>
        <template v-if="!enterParam">
            <view class="head-top">
                <view class="top">

                    <!-- 选择计算方式弹窗(注释的为之前弹窗样式) -->
                    <view class="container_row" @tap="_openCountryMethod">
                        <span class="red-requre">*</span>
                        <view class="container_row_left">{{language.lang.ssgj}}</view>
                        <view class="container_row_right" :style="{backgroundColor:show_type=='see'|| type=='see'?'#F4F5F6':''}">
                            <input type="text" disabled="true" v-if=" countryList.length > 0 " v-model="countrySet" :placeholder="language.lang.qxzgj"  />
                            <input type="text" disabled="true" v-if="proCountryPickerArray.length==0" :placeholder="language.lang.qxzgj" :placeholder-style="placeStyle" style="padding-right: 70rpx;" />
                            <!-- 箭头图标 -->
                            <image v-if="type != 'see'" class="jiantou" :src="jiantou"></image>
                        </view>
                    </view>

                    <!-- 选择商品所属国家弹窗 -->
                    <mpvue-picker v-if="proCountryShow" :themeColor="themeColor" ref="proCountryShowPicker" :mode="mode"
                                  :deepLength="deepLength" :pickerValueDefault="proCountryPickerDefault" @onConfirm="onConfirmProCountry"
                                  :pickerValueArray="proCountryPickerArray"></mpvue-picker>

                    <!-- 选择计算方式弹窗(注释的为之前弹窗样式) -->
                    <view class="container_row" @tap="_openLanguageMethod">
                        <span class="red-requre">*</span>
                        <view class="container_row_left">{{language.lang.yz}}</view>
                        <view class="container_row_right" :style="{backgroundColor:show_type=='see'|| type=='see'?'#F4F5F6':''}">
                            <view :style="lang_css"> {{lang_name}}</view>
                            <!-- 箭头图标 -->
                            <image v-if="type != 'see'" class="jiantou" :src="jiantou"></image>
                        </view>
                    </view>

                    <!-- 选择语种弹窗 -->
                    <chooseS
                        ref="languagePop"
                        :is_choose_obj='is_choose_obj_language'
                        :is_type="chooseLanguageType"
                        :is_choose="languages"
                        @_choose="_chooseLanguage"
                        @_isHide="_isHideLanguage">
                    </chooseS>


                    <!-- 模版名称 -->
                    <view class="container_row first_con">
                        <span class="red-requre">*</span>
                        <view class="container_row_left">{{language.freight_add.name}}</view>
                        <view class="container_row_right" :style="{backgroundColor:show_type=='see' || type=='see'?'#F4F5F6':''}">
                            <input 
                                v-model="formData.name" type="text" maxlength="20"
                                :disabled="type=='see'"
                                placeholder-style="color:#999999;font-size:32rpx"
                                :placeholder="language.freight_add.name_placeholder">
                        </view>
                    </view> 
                    
                    <!-- 选择计算方式弹窗(注释的为之前弹窗样式) -->
                    <view class="container_row" @tap="_openMethod">
                        <span class="red-requre">*</span>
                        <view class="container_row_left">{{language.freight_add.jsfs}}</view>
                        <view class="container_row_right" :style="{backgroundColor:show_type=='see'|| type=='see'?'#F4F5F6':''}">
                            <view style="font-size:32rpx"> {{typeArr[typeIndex].name}}</view>
                            <!-- 箭头图标 -->
                            <image v-if="type != 'see'" class="jiantou" :src="jiantou"></image>
                        </view>
                    </view>
                    
                    <!-- 选择弹窗 -->
                    <chooseS
                        ref="isLanguage" 
                        :is_choose_obj='is_choose_obj' 
                        :is_type="chooseType" 
                        :is_choose="typeArr"
                         @_choose="_choose" 
                         @_isHide="_isHide">
                    </chooseS>
                   
                    <view class="container_row" @tap="goSetDefaultFreigt()">
                        <span class="red-requre">*</span>
                        <view class="container_row_left">{{language.freight_add.mryf}}</view>
                        <view class="container_row_right" :style="{backgroundColor:show_type=='see'|| type=='see'?'#F4F5F6':''}">
                            <template v-if="defaultFreight.num1 == ''">
                                <span class="set" style="color:#999999;font-size: 32rpx;">{{language.freight_add.qsz}}</span>
                            </template>
                            <template v-else>
                                <span style="font-size: 32rpx;">{{language.freight_add.qck}}</span>
                            </template>
                            <!-- 箭头图标 -->
                            <image class="jiantou" :src="jiantou"></image>
                        </view>
                    </view>
                </view>
                
                <view class="dispensable" v-if="country_num == 156">
                    <view class="container_row" @tap="goAddMould()">
                        <!-- *号 -->
                        <view class="container_row_left" style="margin-right: 12rpx;">{{language.freight_add.zdyf}}</view>
                        <view class="container_row_right" :style="{backgroundColor:show_type=='see'|| type=='see'?'#F4F5F6':''}">
                            <template v-if="formData.rules == ''">
                                <span style="color:#999999;font-size: 32rpx;">{{language.freight_add.qsz }}</span>
                            </template>
                            <template v-else>
                                <span style="font-size: 32rpx;">{{language.freight_add.qck }}</span>
                            </template>
                            <!-- 箭头图标 -->
                            <image class="jiantou" :src="jiantou"></image>
                        </view>
                    </view>
                    
                    <view class="container_row" v-if="country_num == 156" @tap="not_delivery">
                        <view class="container_row_left" style="margin-right: 12rpx;">{{language.freight_add.bfdq}}</view>
                        <view class="container_row_right" :style="{backgroundColor:show_type=='see'|| type=='see'?'#F4F5F6':''}">
                            <template v-if="no_delivey_province == '' && type =='add' ">
                                <span class="set" style="color:#999999;font-size: 32rpx;">{{language.freight_add.qsz }}</span>
                            </template>
                            <template v-else-if="no_delivey_province == ''">
                                <span style="font-size:32rpx">{{language.freight_add.wsz}}</span>
                            </template>
                            <template v-else>
                                <span style="font-size: 32rpx;">{{language.freight_add.qck }}</span>
                            </template>
                            <!-- 箭头图标 -->
                            <image v-if="!(no_delivey_province == '' && type == 'see')" class="jiantou" :src="jiantou"></image>
                        </view>
                    </view>
                </view>
                <template v-if="type == 'add'">
                    <view class="dispensable set_default">
                        <view class="container_row">
                            <!-- *号 -->
                            <view class="container_row_left">{{language.freight_add.setDefault}}</view>
                            <view class="container_row_right row_switch">
                                <switch :checked="formData.is_default" color="#FA5151" @change="_default"
                                    style="transform:scale(0.8)" />
                            </view>
                        </view>
                    </view>
                </template>
                <template v-if="(type != 'see' && show_type == 'edit') || show_type == 'add'">
                    <view class="container_bottomBtn">
                        <view class="one" :class="{disable:!saveOkStatus}" @tap="saveOk">{{language.freight_add.save}}</view>
                    </view>
                </template>
                <template v-else-if="show_type != 'see'">
                    <view class="container_bottomBtn">
                        <view class="one" @tap="edit">{{language.freight_add.bj}}</view>
                        <view class="two" @tap="delC">{{language.freight_add.del}}</view>
                    </view>
                </template>
                <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
                    <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                        <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                            <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                        </view>
                        <view class="xieyi_title"
                            style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                            {{ is_text }}
                        </view>
                    </view>
                </view>
                 <showToast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" @richText="richText" @confirm="confirm"></showToast>
                </view>
        </template>
    </view>
</template>

<script>
    import chooseS from "@/components/aComponents/choose.vue"
    import showToast from '@/components/aComponents/showToast.vue'
    import mpvuePicker from "../../components/mpvuePicker.vue";
    import {
        LaiKeTui_showProCountry,
        LaiKeTui_onConfirmProCountry
    } from '@/pagesA/myStore/myStore/uploadPro.js';
    export default {
        data() {
            return {
                is_choose_obj_language:{},
                chooseLanguageType:0,
                languages:[],
                lang_code: '', //默认中文
                lang_name: '', //默认中文
                lang_css: 'color:#999999;font-size:32rpx', //默认中文
                
                countrySet: '',
                countryList:[],
                country_num: 156, //默认中国
                proCountryShow: false,
                proCountryPickerArray: [],
                proCountryPickerDefault: [],
                themeColor: '#FA5151',
                mode: '',
                deepLength:0,
                placeStyle: 'color:#999999;font-size:28upx',
                
                chooseClassFlag: false,
                closeImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/close2x.png',
                is_choose_obj:{},
                clearImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
                show_type: '',
                province_id: '',
                pre_name: '',
                no_delivey_province: '',
                no_delivey_province_id: '',
                reachNumber: '', //满多少件包邮
                typeIndex: 0,
                title: '添加模板',
                type: '',
                is_sus:false,
                is_text:'',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
                xuanzehei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehei2x.png',
                xuanzehui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
                shanchudizhi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/shanchudizhi2x.png',
                freight_top: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/freight_top.png',
                freight_bottom: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/freight_bottom.png',
                freight_del: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/freight_del.png',
                typeArr: [
                    {
                        id: 0,
                        name: '件数'
                    },
                    {
                        id: 1,
                        name: '重量'
                    }
                ],

                enterParam: false, // 输入参数框
                number: [0],
                saveOkStatus: false,
                formData: {
                    name: '',
                    rules: [],
                    is_default: false
                },
                shop_id: '',
                freight_id: '',
                clickFlag: true,

                defaultFreight: {
                    num1: '',
                    num2: '',
                    num3: '',
                    num4: ''
                },

                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                chooseType:0,
                is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
                is_showToast_obj: {
                    
                }, //imgUrl提示图标  title提示文字
            };
        },
        components:{
            mpvuePicker,
            chooseS,
            showToast
        },
        watch: {
            formData: {
                handler: function() {
                    // 优化前是this.formData.name != '' && this.formData.rules != ''(模板名称和指定运费都不能为空)
                    if (this.formData.name != '') {
                        this.saveOkStatus = true;
                    } else {
                        this.saveOkStatus = false;
                    }
                },
                deep: true
            }
        },
        async onLoad(option) {
            
            this.lang_name = this.language.lang.qxzyz;

            const result =  await this.LaiKeTuiCommon.getCountryList();
            this.countryList = result.data;

            this.proCountryPickerArray = [] ;
            for(let i = 0 ; i < this.countryList.length ; i++ ){
                this.proCountryPickerArray.push(this.countryList[i].name)
            }


            const result1 = await this.LaiKeTuiCommon.getLanguages();
            this.languages = result1.data;
            
            for (let i = 1; i < 2000; i++) {
                this.number.push(i)
            }
            uni.removeStorageSync('not_delivery_province')
            uni.removeStorageSync('freight_rules')

            
            
            this.type = option.type
            if (option.type == 'see') {
                this.show_type = "see"
            } else if (option.type == 'edit') {
                this.show_type = ""
                this.type = 'edit'
            } else if (option.type == 'add') {
                this.show_type = "add"
                this.type = 'add'
            }
            if (option.id) {
                this.freight_id = option.id
            }



            // 之前下面这块是放在onShow里面
            if (this.type == 'add') {
                this.title = this.language.freight_add.title
            } else if (this.type == 'edit') {
                this.title = this.language.freight_add.title1
            } else if (this.type == 'see') {
                this.title = this.language.freight_add.title2
            }

            uni.setNavigationBarTitle({
                title: this.title
            })

            this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
            let no_delivey_province = [];
            if (uni.getStorageSync('not_delivery_province') != '') {
                no_delivey_province = uni.getStorageSync('not_delivery_province');
                if (no_delivey_province) {
                    this.no_delivey_province = no_delivey_province.name
                    this.no_delivey_province_id = no_delivey_province.id
                }
            }

            if (uni.getStorageSync('freight_rules')) {
                let rule = uni.getStorageSync('freight_rules')
                
                this.formData.rules = rule
            } else if (this.freight_id) {
                this.axios()
            }
        },
        onShow() {
            typeArr: [
                {
                    id: 0,
                    name: this.language.freight_add.js
                },
                {
                    id: 1,
                    name: this.language.freight_add.zl
                }
            ]
            if (this.type == 'add') {
                this.title = this.language.freight_add.title
            } else if (this.type == 'edit') {
                this.title = this.language.freight_add.title1
            } else if (this.type == 'see') {
                this.title = this.language.freight_add.title2
            }
            let no_delivey_province = [];
            if (uni.getStorageSync('not_delivery_province') != '') {
                no_delivey_province = uni.getStorageSync('not_delivery_province');
                if (no_delivey_province) {
                    this.no_delivey_province = no_delivey_province.name
                    this.no_delivey_province_id = no_delivey_province.id
                }
            }
            if (uni.getStorageSync('freight_rules')) {
                let rule = uni.getStorageSync('freight_rules')
                
                this.formData.rules = rule
            } else if (this.freight_id) {
                this.axios()
            }
            this.is_choose_obj_language = {
                title: this.language.lang.qxzyz,
                colorLeft: '#999999',
                colorRight: '#FA5151',
                background: '#F4F5F6',//显示图标
                borderRadius: '24rpx 24rpx 0 0',//提示文字
            }
            this.is_choose_obj = {
                title: this.language.freight_add.jsfs,
                colorLeft: '#999999',
                colorRight: '#FA5151',
                background: '#F4F5F6',//显示图标
                borderRadius: '24rpx 24rpx 0 0',//提示文字
            }
            if (uni.getStorageSync('defaultFreight')) {
                this.defaultFreight = uni.getStorageSync('defaultFreight')
                uni.removeStorageSync('defaultFreight')
            }
            this.isLogin()
        },
        methods: {

            _isHide(){
                this.chooseType = 0
            },
            
            // 打开计算方式
            _openMethod() {
                // 查看模式不能弹窗
                if (this.type != 'see') {
                    this.chooseType = 3
                }
            },
            
            // 打开语种方式
            _openLanguageMethod() {
                // 查看模式不能弹窗
                if (this.type != 'see') {
                    this.chooseLanguageType = 4
                }
            }, 
            
            // 打开国家列表
            async _openCountryMethod(){
                const result =  await this.LaiKeTuiCommon.getCountryList();
                this.countryList = result.data;
                LaiKeTui_showProCountry(this);
            },

            // 点击国家
            onConfirmProCountry(e) {
                LaiKeTui_onConfirmProCountry(this, e);
            },
            
            //隐藏
            _isHideLanguage(){
                this.chooseLanguageType = 0
            },
            
            // 关闭计算方式
            _closeMethod() {
                this.chooseClassFlag = false;
            },
            
            // 选择语种
            _chooseLanguage(langObj) {
                this.chooseLanguageType = 0;
                this.lang_code = langObj.lang_code;
                this.lang_name = langObj.lang_name;
                this.lang_css = "color:#333333;font-size:32rpx";
                
            },
            
            // 选择计算方式
            _choose(index) {
                this.chooseClassFlag = false;
                if (this.typeIndex != this.type) {
                    uni.removeStorageSync('freight_rules')
                    this.formData.rules = ''
                }
                this.chooseType = 0
                this.typeIndex = index
            },
            // 去设置默认运费
            goSetDefaultFreigt() {
                let url = '/pagesA/myStore/freight_default?defaultFreight=' + JSON.stringify(this.defaultFreight) +
                    '&type=' + this.type + '&typeIndex=' + this.typeIndex
                uni.navigateTo({
                    url
                })
            },
            clear(pre_name) {
                this.pre_name = ''
            },
            showPickerClumn(e) {
            },
            edit() {
                this.type = 'edit';
                this.show_type = 'edit';
                this.title = this.language.freight_add.title1
            },
            showParam() { // 添加修改编辑参数的时候
                this.enterParam = false;
            },
            changeName() {
                if (this.pre_name) {
                    this.formData.name = this.pre_name
                    this.enterParam = false
                }
            },
            enterName() {
                this.enterParam = true
                this.pre_name = this.formData.name
            },
            not_delivery() {
                // 查看模式和没有设置不能进入
                if (this.no_delivey_province == ''  && this.type == 'see') {
                    return false;
                }
                // 排除规则添加地址
                let hidden_freight = '';
                let cityValue = '';
                if (this.formData.rules != '') {
                    hidden_freight = this.formData.rules

                    hidden_freight.forEach(item => {
                        cityValue = cityValue + item.city_value + ','
                    })
                }
                let url = '/pagesA/myStore/freight_sheng?type=not_delivery&show_type=' + this.type
                if (this.no_delivey_province) {
                    if (uni.getStorageSync('not_delivery_province').indeterminateList) {
                        url += '&freight_id=' + this.no_delivey_province_id + '&indeterminateList=' + uni
                            .getStorageSync('not_delivery_province').indeterminateList
                    } else {
                        url += '&freight_id=' + this.no_delivey_province_id + '&viewCity=' + uni.getStorageSync(
                            'not_delivery_province').name
                    }

                }
                //排除规则添加地址
                if (this.formData.rules) {
                    url += '&remove_id=' + cityValue
                }

                uni.navigateTo({
                    url
                })
            },
            
            // 设置指定运费
            goAddMould() {
                let typeIndex = Number(this.typeIndex) ? 3 : 2
                let url = '/pagesA/myStore/freight_rules?typeIndex=' + typeIndex + "&type=" + this.type;
                uni.navigateTo({
                    url
                })
            },
            changeLoginStatus() {
                if (this.freight_id) {
                    this.axios()
                }
            },
            axios() {
                let data = {
                    
                    api:"mch.App.Mch.Freight_modify_show",
                    shop_id: this.shop_id, // 店铺ID
                    id: this.freight_id, // 运费id
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

                        //国家回显
                        this.country_num = data.country_num
                        this.countrySet = data.country_name

                        //语种回显
                        this.lang_code = data.lang_code
                        this.lang_name = data.lang_name
                        
                        this.formData.name = data.name
                        
                        this.typeIndex = data.type
                        this.reachNumber = data.package_settings
                        let no_delivery = '';
                        let no_delivery_ojb = {}
                        this.defaultFreight = data.default_freight
                        if (data.no_delivery.length > 0) {

                            data.no_delivery.forEach(item => {
                                no_delivery = no_delivery + ',' + item[0]
                            })
                            no_delivery_ojb.id = '';
                            no_delivery_ojb.name = data.no_delivery.join(',')
                        }
                        this.province_id = no_delivery
                        this.no_delivey_province = data.no_delivery.join(',')

                        // 运费设置
                        // debugger
                        uni.setStorageSync('freight_rules', data.freight);
                        // 不发货城市
                        uni.setStorageSync('not_delivery_province', no_delivery_ojb);

                       


                        this.formData.rules = data.freight

                        this.formData.is_default = data.is_default == 1 ? true : false
                    } else {
                        uni.showToast({
                            title: message,
                            icon: 'none'
                        })
                    }
                })
            },
            // 取消弹窗
            richText(){
                this.is_showToast=0
            },
            // 确认弹窗
            confirm(){
                // debugger
                this.is_showToast=0
                  if (!this.clickFlag) {
                      return
                  }
                  this.clickFlag = false
                  let data = {
                      
                      api:"mch.App.Mch.Freight_del",
                      shop_id: this.shop_id, // 店铺ID
                      id: this.freight_id
                  }
              
              this.$req.post({
                  data
              }).then(res => {
                  let {
                      code,
                      message
                  } = res
              
                
                  if (code == 200) {
                      this.is_text = this.language.putForward.sccg
                      this.is_sus = true
                      setTimeout(() => {
                          this.is_sus = false
                          uni.navigateBack({
                              delta: 1
                          })
                      }, 1000)
                  } else {
                      uni.showToast({
                          title: message,
                          icon: 'none'
                      })
                      this.clickFlag = true
                  }
              }).catch(error => {
                  this.clickFlag = true
              })
            },
            delC() {
                this.is_showToast = 4
                this.is_showToast_obj.title = this.language.freight_add.Tips
                this.is_showToast_obj.content = this.language.freight_add.TipsText
                this.is_showToast_obj.button = this.language.showModal.cancel
                this.is_showToast_obj.endButton = this.language.showModal.confirm
                


            },
            checkval(str) {
                var reg = /^[\u4e00-\u9fa5_a-zA-Z0-9]+$/
                if (reg.test(str)) {
                    return true
                } else {
                    return false
                }
            },
            saveOk() {
                if (!this.saveOkStatus) {
                    return false;
                }
                if (!this.formData.name) {
                    uni.showToast({
                        title: this.language.freight_add.nameTips,
                        icon: "none"
                    })
                    return
                }
                if (!this.defaultFreight.num1) {
                    uni.showToast({
                        title: '请添加默认运费',
                        icon: "none"
                    })
                    return
                }


                let hidden_freight = ''
                if (uni.getStorageSync('freight_rules') != '') {
                    hidden_freight = uni.getStorageSync('freight_rules');
                }

                // 当它没有填指定运费时，参数类型改变
                if (hidden_freight == '') {
                    hidden_freight = []
                }
                let no_delivey_province = [];
                let no_delivey_province_arr = [];
                if (this.no_delivey_province) {
                    no_delivey_province_arr = this.no_delivey_province.split(',')
                    no_delivey_province_arr.forEach(item => {
                        let item_arr = [];
                        if (item == '') {
                            return
                        }
                        item_arr.push(item)

                        no_delivey_province.push(item_arr)
                    })
                }

                let data = {
                    api:"mch.App.Mch.Freight_add",
                    type: this.typeArr[this.typeIndex].id,
                    is_package_settings: this.reachNumber > 0 ? 1 : 0,
                    package_settings: this.reachNumber > 0 ? this.reachNumber : 0,
                    is_no_delivery: no_delivey_province.length > 0 ? 1 : 0,
                    no_delivery: JSON.stringify(no_delivey_province_arr),
                    defaultFreight: JSON.stringify(this.defaultFreight),
                    shop_id: this.shop_id, // 店铺ID
                    name: this.formData.name, // 规则名称
                    hidden_freight: JSON.stringify(hidden_freight), // 运费信息
                    lang_code: this.lang_code, // 语种
                    country_num: this.country_num, // 国家
                    is_default: this.formData.is_default ? 1 : 0
                }

                if (this.type == 'edit') {
                    data.api = 'mch.App.Mch.Freight_modify'
                    data.id = this.freight_id
                }

                if (!this.clickFlag) {
                    return
                }
                this.clickFlag = false
                this.$req.post({
                    data
                }).then(res => {
                    let {
                        code,
                        message
                    } = res

                    
                    if (code == 200) {
                        if(this.type == 'edit'){
                            this.is_text = this.language.putForward.bjcg
                        }else{
                            this.is_text = this.language.putForward.tjcg
                        }
                            this.is_sus = true
                        setTimeout(() => {
                            this.is_sus = false
                            let url = '/pagesA/myStore/freight';
                            uni.navigateTo({
                                url
                            })
                        }, 1000)
                    } else {
                        uni.showToast({
                            title: message,
                            icon: 'none'
                        })
                        this.clickFlag = true
                    }
                }).catch(error => {
                    this.clickFlag = true
                })
            },
            click_del(index) {
                this.formData.rules.splice(index, 1)
            },
            click_bottom(item) {
                item.status = !item.status
            },
            _default() {
                if (this.type != 'see') {
                    this.formData.is_default = !this.formData.is_default
                }

            },
            toUrl(url) {
                let freights = this.formData.rules
                let freight_data = {}
                let sel_city = []

                freights.filter((item, index) => {
                    freight_data[index] = {
                        "one": item.freight,
                        "name": item.freight_sheng.name
                    }

                    sel_city.push(item.freight_sheng.id)
                })

                let data = {
                    
                    api:"mch.App.Mch.Get_sheng",
                    shop_id: this.shop_id, // 店铺ID
                    data: JSON.stringify(freight_data),
                    sel_city: JSON.stringify(sel_city)
                }

                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        uni.setStorageSync('freight_data', this.formData.rules)
                        uni.navigateTo({
                            url
                        })
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }
                })
            }
        }
    };
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
@import url("@/laike.less");
    @import url('../../static/css/myStore/freight_add.less');
</style>
