<template>
    <div>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.applyStore.title"  ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <div class="relative">
            <div class="contentBox">
                <div class="formDiv">
                    <div class="formList">
                        <div class="leftText1">
                            <span class="must">*</span>
                            <span class="font_28">{{language.applyStore.shopName}}</span>
                        </div>
                        <div class="rightInput1">
                            <input
                                :placeholder-style="placeStyle"
                                type="text"
                                v-model="storeName"
                                maxlength="14"
                                :placeholder="storeNamePH"
                                @focus="_changePH1('storeName')"
                                @blur="_changePH('storeName')"
                            />
                        </div>
                    </div>
                    <div v-if="warnTextStatus" class="warningText">
                        <img :src="warnImg" class="warnImg" />
                        {{language.applyStore.nameExisting}}
                    </div>
                </div>
                <!-- 店铺信息 -->
                <div class="formDiv textarea">
                    <div class="formList">
                        <div class="leftText1">
                            <span class="must"></span>
                            <span class="font_28">{{language.applyStore.information}}</span>
                        </div>
                        <div class="rightInput1">
                            <textarea class="checkBox" v-model="storeTitle" :placeholder-style="placeStyle" maxlength="50" :placeholder="storeTitlePH"  @focus="_changePH1('storeTitle')" @blur="_changePH('storeTitle')" />
                        </div>
                    </div>
                </div>
                
                <!-- 店铺分类 -->
                <div class="content_row">
                    <div class="content_row_left"><span class="must" >*</span>{{language.myStore.dpfl}}</div>
                    <div class="content_row_right">
                        <picker @change="bindPickerChange" :value="mch_fl" :range="mch_arr" style="flex: 1;">
                            <view style="display: flex;align-items: center;justify-content: space-between;">
                                <view :class="mch_fl==-1?'uni-input':'uni-input-val'" >{{mch_fl==-1?language.myStore.chooseClass:mch_arr[mch_fl]}}</view>
                                <view class="jiantouDivs">
                                    <img :src="jiantou" alt="" />
                                </view>
                            </view>
                        </picker>
                    </div>
                </div>
                
                <div class="formDiv border_0">
                    <div class="formList">
                        <div class="leftText1">
                            <span class="must">*</span>
                            <span class="font_28">{{language.applyStore.business}}</span>
                        </div>
                        <div class="rightInput1">
                            <input
                                :placeholder-style="placeStyle"
                                type="text"
                                v-model="sellRange"
                                maxlength="50"
                                :placeholder="sellRangePH"
                                @focus="_changePH1('sellRange')"
                                @blur="_changePH('sellRange')"
                            />
                        </div>
                    </div>
                </div>
            </div>
            <div class="contentBox">
                <div class="formDiv">
                    <div class="formList">
                        <div class="leftText1">
                            <span class="must">*</span>
                            <span class="font_28">{{language.applyStore.username}}</span>
                        </div>
                        <div class="rightInput1">
                            <input
                                maxlength="20"
                                :placeholder-style="placeStyle"
                                type="text"
                                :placeholder="userNamePH"
                                @focus="_changePH1('userName')"
                                @blur="_changePH('userName')"
                                v-model="userName"
                            />
                        </div>
                    </div>
                </div>
                <!-- 身份证号码 -->
                <div class="formDiv">
                    <div class="formList">
                        <div class="leftText1">
                            <span class="must">*</span>
                            <span class="font_28">{{language.applyStore.IDnumber}}</span>
                        </div>
                        <div class="rightInput1">
                            <input
                                :placeholder-style="placeStyle"
                                type="idcard"
                                maxlength="18"
                                :placeholder="userIDPH"
                                @focus="_changePH1('userID')"
                                @blur="_changePH('userID')"
                                v-model="userID"
                            />
                        </div>
                    </div>
                </div>
                <div class="formDiv">
                    <div class="formList">
                        <div class="leftText1">
                            <span class="must">*</span>
                            <span class="font_28">{{language.applyStore.tel}}</span>
                        </div>
                        <div class="rightInput1 rightInput1_area">
                            <div class="uni-input__area" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
                               {{ code2?'+' + code2 : '国家/地区' }}
                               <image :src="down" mode="aspectFill"></image>
                            </div>
                            <input 
                                :placeholder-style="placeStyle"
                                type="number"
                                :placeholder="userPhonePH"
                                @focus="_changePH1('userPhone')"
                                @blur="_changePH('userPhone')"
                                v-model="userPhone"
                            />
                        </div>
                    </div>
                </div>
                
                
                <div class="content_row" v-if="code2 == '86' || code2 == '852' || code2 == '853'">
                    <div class="content_row_left"><span class="must">*</span>{{language.storeModifyAdress.area}}</div>
                    
                    <div class="content_row_right" @click="showMulLinkageThreePicker()">
                        <input type="text" @focus="hideKeyboard()" disabled :placeholder="language.storeModifyAdress.areaplacehold" v-model="city_all"
                               :placeholder-style="placeStyle"/>
                        <div class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </div>
                    </div>
                </div>
                
                <div class="content_row">
                    <div class="content_row_left"><span class="must">*</span>{{language.storeModifyAdress.address}}</div>
                    
                    <div class="content_row_right">
                        <input v-model="address" type="text" :placeholder="language.storeModifyAdress.addressPlacehold" :placeholder-style="placeStyle">
                    </div>
                </div>
            </div>
            <div class="contentBox">
                <div class="formDiv">
                    <div class="formList">
                        <div class="leftText1">
                            <span class="must"></span>
                            <span class="font_28">{{language.applyStore.nature}}</span></div>
                        <div class="rightInput1">
                            <view class="ri_outer" @tap="_changeType(0)">
                                <img class="quan_img" :src="storeType == 0 ? quan_hei : quan_hui" />
                                <span>{{language.applyStore.personal}}</span>
                            </view>
                            <view class="ri_outer" @tap="_changeType(1)">
                                <img class="quan_img" :src="storeType == 1 ? quan_hei : quan_hui" />
                                <span>{{language.applyStore.enterprise}}</span>
                            </view>
                        </div>
                    </div>
                </div>
                <div class="formDiv" style="border-bottom: 0;">
                    <div class="formList" style="padding-bottom: 20rpx;">
                        <div class="leftText1 permit">
                            <span class="must">*</span>
                            <span class="font_28">{{storeType==1?language.applyStore.license:language.applyStore.userCard}}</span>
                        </div>
                        <!-- 企业的凭证 -->
                        <div class="rightInput1 permitDiv" v-if="storeType == 1">
                            <div class="upLoadPic" :style="{border:upImg?'0':''}" @tap="upLoadImg()">
                                <!-- 删除图片图标 -->
                                <img v-if="upImg" :src="delImg" class="closeImg" @tap.stop="_delImg()" />
                                <!-- 没有上传图片 -->
                                <div v-if="!upImg" class="tc">
                                    <img class="img" :src="addImg" alt="" />
                                    <div class="text">{{language.applyStore.upload}}</div>
                                </div>
                                <!-- 当上传了图片 -->
                                <img class="img1" v-else :src="upImg" />
                            </div>
                        
                        </div>
                        <!-- 个人身份证 -->
                        <div class="rightInput1 permitDiv" v-if="storeType == 0">
                            <!-- 正面 -->
                            <div class="upLoadPic" :style="{border:addImgPositive?'0':''}" @tap="_chooseImg1()">
                                <!-- 删除图片图标 -->
                                <img v-if="addImgPositive" :src="delImg" class="closeImg" @tap.stop="_delImg1()" />
                                <!-- 没有上传图片 -->
                                <div v-if="!addImgPositive" class="tc">
                                    <img class="img" :src="addImg" alt="" />
                                    <div class="text">{{language.applyStore.uploadCard1}}</div>
                                </div>
                                <!-- 当上传了图片 -->
                                <img class="img1" v-else :src="addImgPositive" />
                            </div>
                            <!-- 反面 -->
                            <div class="upLoadPic" :style="{border:addImgBack?'0':''}" @tap="_chooseImg2()">                                
                                <!-- 删除图片图标 -->
                                <img v-if="addImgBack" :src="delImg" class="closeImg" @tap.stop="_delImg2()" />
                                <!-- 没有上传图片 -->
                                <div v-if="!addImgBack" class="tc">
                                    <img class="img" :src="addImg" alt="" />
                                    <div class="text">{{language.applyStore.uploadCard2}}</div>
                                </div>
                                <!-- 当上传了图片 -->
                                <img class="img1" v-else :src="addImgBack" />
                            </div>
                        </div>
                        
                    </div>
                </div>
               
            </div>
            
                <div class="protocol">
                    <div @tap="_agreement()">
                        <!-- #ifndef MP-ALIPAY -->
                        <img :src="agreement ? agreement_t : agreement_f" style="width:26rpx;height:26rpx;margin-right: 10rpx;" />
                        <!-- #endif -->
                        <!-- #ifdef MP-ALIPAY -->
                        <img v-if="agreement" :src="agreement_t" style="width:26rpx;height:26rpx;margin-right: 10rpx;" />
                        <img v-if="!agreement" :src="agreement_f" style="width:26rpx;height:26rpx;margin-right: 10rpx;" />
                        <!-- #endif -->
                        {{language.applyStore.read}}
                    </div>
                    <div @tap="_lookXY()">《{{name||''}}》</div>
                </div>
                
            <view class="btn">
                <div class="submitBtn" @tap="_submit()">{{language.applyStore.submit}}</div>
            </view>
            
            <!-- 重复提示弹窗（样式类和订单详情中的发票弹窗相似） -->
            <view class="mask_addInvoice" v-if="isMaskRepeat">
                <view class="mask_invoice_content ">
                    <view class="invoice_content_top">
                        <p class="remarksContent">{{repeatContent}}</p>
                    </view>
                    <view class="invoice_content_bottom">
                        <p @tap="isMaskRepeat = false">我知道了</p>
                    </view>
                </view>
            </view>
        </div>
        <mpvue-city-picker :themeColor="themeColor" ref="mpvueCityPicker" :pickerValueDefault="cityPickerValueDefault"
                           @onConfirm="onConfirm"></mpvue-city-picker>
    </div>
</template>

<script>
    import mpvueCityPicker from '@my-miniprogram/src/components/mpvue-citypicker/mpvueCityPicker';  
    import provinceData from '@my-miniprogram/src/components/mpvue-citypicker/city-data/province.js';
    import cityData from '@my-miniprogram/src/components/mpvue-citypicker/city-data/city.js';
    import areaData from '@my-miniprogram/src/components/mpvue-citypicker/city-data/area.js'; 
    
import {img} from "@/static/js/login/imgList.js";
export default {
    data() {
        return {
            bgColor: [
                {
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            
            upImg1: '',
            goOn: false,
            licence: '',
            storeType: 0,
            warnTextStatus: false,
            // 企业凭证
            upImg: '',
            // 个人身份证
            addImgPositive:"",
            addImgBack:'',
            
            name:'',
            title: '申请开店',
            fastTap: true,
            storeName: '',
            storeNamePH: '请填写店铺名称',
            storeNamePH1: '请填写店铺名称',
            storeTitle: '',
            storeTitlePH: '请填写店铺简介信息（50字以内）',
            storeTitlePH1: '请填写店铺简介信息（50字以内）',
            userName: '',
            userNamePH: '请填写真实姓名',
            userNamePH1: '请填写真实姓名',
            userID: '',
            userIDPH: '请填写身份证号',
            userIDPH1: '请填写身份证号',
            userPhone: '',
            userPhonePH: '请填写联系电话',
            userPhonePH1: '请填写联系电话',
            userAdd: '',
            address: '',
            userAddPH: '请填写联系地址',
            userAddPH1: '请填写联系地址',
            sellRange: '',
            sellRangePH: '请填写店铺经营范围',
            sellRangePH1: '请填写店铺经营范围',
            agreement: false,
            warnImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/warnIng.png',
            addImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/tianjiadizhi2x.png',
            agreement_t: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
            agreement_f: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png',
            delImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
            storeNameStatus: false,
            storeTitleStatus: true,//店铺信息是否是必填
            userNameStatus: false,
            userIDStatus: false,
            userPhoneStatus: false,
            compStatus: false,
            sellRangeStatus: false,
            quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png',
            quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
            placeStyle: 'color:#999999;font-size:32rpx;font-weight:400;',
            
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            
            city_all: '',
            
            themeColor: '#D73B48',
            cityPickerValueDefault: [0, 0, 0],
            shop_id: '',
            
            pageType: '', //是否是从申请店铺进入
            
            mch_fl: -1, //店铺分类 (设置-1用于默认文字)
            mch_arr:[], //店铺分类列表
            mch_data:[], //店铺分类总数据
            
            isMaskRepeat:false,
            repeatContent:'',
            code2: '86',
            num3: '156',
            down: img(this).down,
            
        };
    },
    components:{ mpvueCityPicker },
    onLoad(option) {
        this.goOn = option.goOn;
        this.axios()
        this.axios_fl()
        this.city_all = localStorage.getItem('citys')
    },
    onShow() {
        if(uni.getStorageSync('applyCity')){
            this.userAdd = uni.getStorageSync('applyCity')
            this.address = uni.getStorageSync('applyAddress')
            uni.removeStorageSync('applyCity')
            uni.removeStorageSync('applyAddress')
        }
        if(uni.getStorageSync('diqu')){
          let diqu = JSON.parse(uni.getStorageSync('diqu'))
          this.code2 = diqu?.code2
          this.num3 = diqu?.num3 
        } 
        this.storeNamePH = this.language.applyStore.shopNamePlaceholder
        this.storeNamePH1 = this.language.applyStore.shopNamePlaceholder
        
        this.storeTitlePH = this.language.applyStore.informationPlaceholder
        this.storeTitlePH1 = this.language.applyStore.informationPlaceholder
        
        this.sellRangePH = this.language.applyStore.businessPlaceholder
        this.sellRangePH1 = this.language.applyStore.businessPlaceholder
        
        this.userNamePH = this.language.applyStore.usernamePlaceholder
        this.userNamePH1 = this.language.applyStore.usernamePlaceholder
        
        this.userIDPH = this.language.applyStore.IDnumberPlaceholder
        this.userIDPH1 = this.language.applyStore.IDnumberPlaceholder
        
        this.userPhonePH = this.language.applyStore.telPlaceholder
        this.userPhonePH1 = this.language.applyStore.telPlaceholder
        
        this.userAddPH = this.language.applyStore.addressPlaceholder
        this.userAddPH1 = this.language.applyStore.addressPlaceholder
        this.axios()
    },
    watch:{
      userID(){
          this.$nextTick(function() {
          	 this.userID = this.userID.replace(/[\W]/g, "")//只允许输入数字和字母
          })
          // this.checkIdcard(this.userID)//校验身份证号码是否正确（该函数有问题）
      }
    },
    mounted() {
        this.isLogin(()=>{
			
			this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
			
			if (this.goOn) {
			    this.$req
			        .post({
			            data: {
			                api:"mch.App.Mch.Continue_apply",
			                shop_id: this.shop_id
			            }
			        })
			        .then(res => {
			            var data = res.data.list;
			            this.storeName = data.name;
			            this.storeTitle = data.shop_information;
			            this.sellRange = data.shop_range;
			            this.userName = data.realname;
			            this.userID = data.id_number;
			            this.userPhone = data.tel;
                        this.mch_data.forEach((item,index)=>{
                            if(item.id==data.cid){
                               this.mch_fl=index
                               return 
                            }
                        })
			            this.userAdd = data.sheng +'-'+ data.shi +'-'+ data.xian;
                        this.city_all = data.sheng +'-'+ data.shi +'-'+ data.xian;
			            this.address = data.address;
			            this.storeType = data.shop_nature;
			            this.agreement = true;
                        
                        if(this.storeType==0){
                            this.addImgPositive=data.business_license.split(',')[0];
                            this.addImgBack=data.business_license.split(',')[1];
                        }else{
                            this.upImg = data.business_license;//企业营业执照
                        }
			            this.storeNameStatus = true;
			            this.storeTitleStatus = true;
			            this.userNameStatus = true;
			            this.userIDStatus = true;
			            this.userPhoneStatus = true;
			        });
			}
		})
    },
    methods: {
        //店铺分类
        bindPickerChange(e) {
            this.mch_fl = e.detail.value
        },
        hideKeyboard (event) {
            event.preventDefault();
            uni.hideKeyboard()
        },
        showMulLinkageThreePicker () {
            this.$refs.mpvueCityPicker.show()
        },
        onConfirm (e) {
            this.city_all = e.label
        },
        modifyAddress(){
            uni.navigateTo({
                url: '/pagesA/myStore/modifyAdress?pageType=apply&shop_id='+this.shop_id + '&userAdd='+this.userAdd+'&address='+this.address
            })
        },
        changeLoginStatus() {
            
        },
        // 删除企业凭证
        _delImg() {
            this.upImg = '';
        },
        // 删除个人身份证正面图
		_delImg1() {
			this.addImgPositive = '';
		},
		// 删除个人身份证反面图
		_delImg2() {
			this.addImgBack = '';
		},
        // 上传图片
        upLoadImg() {
            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            if (this.upImg) {
                this.fastTap = true;
                uni.previewImage({
                    urls: this.upImg,
                    success: function() {}
                });
            } else {
                this.fastTap = true;
                uni.chooseImage({
                    count: 1,
                    success: async  res => {
                        let imageInfo = await this.getImageList(res.tempFilePaths, this)
                        this.upImg = imageInfo[0]
                    },
                    error: function() {}
                });
            }
        },
        
        // 传正面图
        _chooseImg1(){
            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            if (this.addImgPositive) {
                this.fastTap = true;
                uni.previewImage({
                    urls: this.addImgPositive,
                    success: function() {}
                });
            } else {
                this.fastTap = true;
                uni.chooseImage({
                    count: 1,
                    success: async  res => {
                        let imageInfo = await this.getImageList(res.tempFilePaths, this)
                        this.addImgPositive = imageInfo[0]
                    },
                    error: function() {}
                });
            }
        },
        // 传反面图
        _chooseImg2() {
            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            if (this.addImgBack) {
                this.fastTap = true;
                uni.previewImage({
                    urls: this.addImgBack,
                    success: function() {}
                });
            } else {
                this.fastTap = true;
                uni.chooseImage({
                    count: 1,
                    success: async  res => {
                        let imageInfo = await this.getImageList(res.tempFilePaths, this)
                        this.addImgBack = imageInfo[0]
                    },
                    error: function() {}
                });
            }
        },
        // 当需要一次性上传多张图片时（需要数组的形式传参时）
        async getImageList(tempFilePaths, me) {
        	let list = tempFilePaths
        	uni.showLoading({
        		title: me.language.showLoading.upLoading,
        		mask: true
        	})
        	for (let key of list.keys()) {
        		let res = await me.$req.upLoad(list[key])
        		list[key] = res.data
        	}
        	uni.hideLoading()
        	return Promise.resolve(list);
        },
        
        _submit() {
            if(this.mch_fl == -1){
                return uni.showToast({
                    title: this.language.chooseClass.saveFail,
                    icon: 'none'
                })
            }
            
            var me = this;
            if (!this.fastTap) {
                return;
            }
            
            // #ifdef APP-PLUS || HS
            uni.setStorage('citys',this.city_all)
            // #endif
            
            // #ifdef MP-WEIXIN
            wx.setStorageSync('citys',this.city_all)
            // #endif
            
            if(this.code2 == '86' || this.code2 == '852' || this.code2 == '853'){
                this.userAdd = this.city_all
            }else{
                this.userAdd = ''
            }
            this.fastTap = false;
            // 个人凭证
            if(me.storeType==0&&(this.addImgBack&&this.addImgPositive)){
                this.upImg=[]
                this.upImg.push(this.addImgPositive,this.addImgBack)
            }
            let flag =
                this.storeNameStatus &&
                this.storeTitleStatus &&
                this.userNameStatus &&
                this.userIDStatus &&
                this.userPhoneStatus &&
                this.agreement &&
                this.sellRange&&(this.upImg||(this.addImgPositive&&this.addImgBack));
            if (flag) {
                // && this.upImg != this.upImg1
                if (this.upImg ) {
                    let data = {
                        api:"mch.App.Mch.Apply",
                        name: me.storeName,
                        shop_information: me.storeTitle,
                        shop_range: me.sellRange,
                        realname: me.userName,
                        ID_number: me.userID,
                        tel: me.userPhone,
                        city_all: me.userAdd,
                        address: me.address,
                        shop_nature: me.storeType,
                        cid:me.mch_data[me.mch_fl].id,
                        imgUrls:me.upImg.toString(),
                        cpc:me.code2,
                    }
                    me.$req.post({data}).then((res)=>{
                        me.fastTap = true;
                        if (res.code == 200) {
                            uni.redirectTo({
                                url: '/pagesA/myStore/applySuc'
                            });
                        }  else if(res.code == 50059){//该状态表示重新提交的申请信息未修改
                            this.isMaskRepeat=true
                            this.repeatContent=res.message
                        }else {
                            me.fastTap = true;
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            });
                        }
                    })
                    .catch((err) => {
                        me.fastTap = true;
                        uni.showToast({
                            title: err.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    })
                } else {
                    let data = {
                        api: 'app.mch.apply',
                        name: me.storeName,
                        shop_information: me.storeTitle,
                        shop_range: me.sellRange,
                        realname: me.userName,
                        ID_number: me.userID,
                        tel: me.userPhone,
                        city_all: me.userAdd,
                        address: me.address,
                        shop_nature: me.storeType,
                        cid:me.mch_data[me.mch_fl].id,
                        cpc:me.code2,
                    }
                    me.$req.post({data}).then(res => {
                        me.fastTap = true;
                        if (res.code == 200) {
                            uni.redirectTo({
                                url: '../myStore/applySuc'
                            });
                        }else{
                            me.fastTap = true;
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            });
                        }
                    }).catch(()=>{
                        me.fastTap = true;
                    })
                }
            } else if (!this.agreement) {
                this.fastTap = true;
                uni.showToast({
                    title: this.language.applyStore.agreementFail,
                    icon: 'none'
                });
            } else {
                this.fastTap = true;
                var res = '';
                if (!this.storeNameStatus) {
                    res = this.language.applyStore.shopNameFail;
                } else if (!this.storeTitleStatus) {
                    res = this.language.applyStore.informationFail;
                } else if (!this.userNameStatus) {
                    res = this.language.applyStore.usernameFail;
                } else if (!this.userIDStatus) {
                    if(this.userID.length==0){
                        res = this.language.applyStore.IDFail;
                    }else{
                        res = this.language.applyStore.IDFail1;
                    }
                } else if (!this.userPhoneStatus) {
                    res = this.language.applyStore.telFail1;
                } else if (!this.agreement) {
                    res = this.language.applyStore.agreementFail;
                } else if (!this.sellRange) {
                    res = this.language.applyStore.businessFail;
                }else if (!this.sellRange) {
                    res = this.language.applyStore.businessFail;
                }else if (!(this.upImg||(this.addImgPositive&&this.addImgBack))) {
                    res = '请上传照片信息';
                }
                
                
                uni.showToast({
                    title: res,
                    icon: 'none',
                    duration: 1500
                });
            }
        },
        _agreement() {
            this.agreement = !this.agreement;
        },
        _lookXY() {
            uni.navigateTo({
                url: './applyAgreement'
            });
        },
        _changeType: function(num) {
            this.storeType = num;
            // 切换所属性质时清另外一种属性图片
            //goOn为true时店铺未通过审核重新编辑（则在切换时不需要清空图片）
            if(!this.goOn){
                if(this.storeType==0){
                    // 企业
                    this.upImg=''
                }else{
                    // 个人
                    this.addImgPositive=""
                    this.addImgBack=''
                }
            }
            
        },
        // 离开事件
        _changePH(type) {
            var me = this;
            if (type == 'storeName') {
                this.storeNamePH = this.storeNamePH1;
                if (this.storeName.length == 0) {
                    this.storeNameStatus = false;
                } else if (this.storeName.length > 0) {
                    let data = {
                        api:"mch.App.Mch.Verify_store_name",
                        name: me.storeName
                    }
                    me.$req.post({data}).then(res => {
                        if (res.code == 200) {
                            me.storeNameStatus = true;
                            me.warnTextStatus = false;
                        } else {
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            });
                            me.storeNameStatus = false;
                            me.warnTextStatus = true;
                        }
                    })
                }
            } else if (type == 'storeTitle') {
                this.storeTitlePH = this.storeTitlePH1;
                if (this.storeTitle.length == 0) {
                    // 店铺信息非必填项
                } else if (this.storeTitle.length > 0) {
                    this.storeTitleStatus = true;
                }
            } else if (type == 'userName') {
                this.userNamePH = this.userNamePH1;
                if (this.userName.length == 0) {
                    this.userNameStatus = false;
                } else if (this.userName.length > 0) {
                    this.userNameStatus = true;
                }
            } else if (type == 'userID') {
                this.userIDPH = this.userIDPH1;
                if (this.userID.length == 0) {
                    this.userIDStatus = false;
                } else if (this.userID.length < 18) {
                    uni.showToast({
                        title: this.language.applyStore.IDFail1,
                        duration: 1500,
                        icon: 'none'
                    });
                } else if (this.userID.length == 18) {
                    this.userIDStatus = true;
                }
            } else if (type == 'userPhone') {
                this.userPhonePH = this.userPhonePH1;
                if (this.userPhone.length == 0) {
                    this.userPhoneStatus = false;
                } else if (this.userPhone.length < 11) {
                    uni.showToast({
                        title: this.language.applyStore.telFail1,
                        duration: 1500,
                        icon: 'none'
                    });
                    this.userPhoneStatus = false;
                } else if (this.userPhone.length == 11) {
                    this.userPhoneStatus = true;
                }
            } else if (type == 'userAdd') {
                this.userAddPH = this.userAddPH1;

            } else if (type == 'sellRange') {
                this.sellRangePH = this.sellRangePH1;
                if (this.sellRange.length == 0) {
                    this.sellRangeStatus = false;
                } else if (this.sellRange.length > 0) {
                    this.sellRangeStatus = true;
                }
            }
        },
        // 获取焦点
        _changePH1(type) {
            if (type == 'storeName') {
                // 店铺名称
                this.storeNamePH = '';
            } else if (type == 'storeTitle') {
                // 店铺信息
                this.storeTitlePH = '';
            } else if (type == 'userName') {
                // 用户姓名
                this.userNamePH = '';
            } else if (type == 'userID') {
                // 身份证号
                this.userIDPH = '';
            } else if (type == 'userPhone') {
                // 联系电话
                this.userPhonePH = '';
            } else if (type == 'userAdd') {
                // 联系地址
                this.userAddPH = '';
            } else if (type == 'sellRange') {
                // 经营范围
                this.sellRangePH = '';
            }
        },
        axios(){
            let data = {
                api:"mch.App.Mch.Agreement",
            }
            
            this.$req.post({data}).then(res=>{
                let { code, message, data } = res
                
                if(code == 200){
                    this.name = data.name
                }else{
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    })
                }
                
                this.loadFlag = true
            })
        },
        axios_fl(){
            let data = {
                api: 'app.index.mchClass'
            }
            this.$req.post({data}).then(res=>{
                let { code, message, data } = res
                if(code == 200){
                    this.mch_data = data.list
                    this.mch_arr = []
                    data.list.forEach((item,index)=>{
                        this.mch_arr.push(item.name)
                    })
                }else{
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    })
                }
                
                this.loadFlag = true
            })
        },
        checkIdcard(idCard) { 
            idCard = this.trim(idCard);//去掉字符串头尾空格          
            console.dir(idCard)           
            if (idCard.length == 15) {   
                return this.isValidityBrithBy15IdCard(idCard);       //进行15位身份证的验证    
            } else if (idCard.length == 18) {  
                var a_idCard = idCard.split("");                // 得到身份证数组   
                if(this.isValidityBrithBy18IdCard(idCard) && this.isTrueValidateCodeBy18IdCard(a_idCard)){   //进行18位身份证的基本验证和第18位的验证
                    uni.showToast({
                        title: this.language.applyStore.IDFail1,
                        duration: 1500,
                        icon: 'none'
                    });
                    this.userIDStatus = false;
                    return true;   
                }else {   
                    return false;   
                }   
            } else {   
                return false;   
            }   
        } ,
        /**  
         * 判断身份证号码为18位时最后的验证位是否正确  (该函数处理是参照https://www.cnblogs.com/candlia/p/11919891.html)
         * @param a_idCard 身份证号码数组  
         * @return  
         */  
        isTrueValidateCodeBy18IdCard(a_idCard) {  
            var Wi = [ 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1 ]; // 加权因子 
            var ValideCode = [ 1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2 ];   // 身份证验证位值.10代表X
            var sum = 0;                             // 声明加权求和变量   
            if (a_idCard[17].toLowerCase() == 'x') {   
                a_idCard[17] = 10;                    // 将最后位为x的验证码替换为10方便后续操作   
            }   
            for ( var i = 0; i < 17; i++) {   
                sum += Wi[i] * a_idCard[i];            // 加权求和   
            }   
            valCodePosition = sum % 11;                // 得到验证码所位置   
            if (a_idCard[17] == ValideCode[valCodePosition]) {   
                return true;   
            } else {   
                return false;   
            }   
        },
        /**  
          * 验证18位数身份证号码中的生日是否是有效生日  
          * @param idCard 18位书身份证字符串  
          * @return  
          */  
        isValidityBrithBy18IdCard(idCard18){   
            var year =  idCard18.substring(6,10);   
            var month = idCard18.substring(10,12);   
            var day = idCard18.substring(12,14);   
            var temp_date = new Date(year,parseFloat(month)-1,parseFloat(day));   
            // 这里用getFullYear()获取年份，避免千年虫问题   
            if(temp_date.getFullYear()!=parseFloat(year)   
                  ||temp_date.getMonth()!=parseFloat(month)-1   
                  ||temp_date.getDate()!=parseFloat(day)){  
                      uni.showToast({
                          title: this.language.applyStore.IDFail1,
                          duration: 1500,
                          icon: 'none'
                      });
                    return false;   
            }else{   
                return true;   
            }   
          }, 
          /**  
           * 验证15位数身份证号码中的生日是否是有效生日  
           * @param idCard15 15位书身份证字符串  
           * @return  
           */  
          isValidityBrithBy15IdCard(idCard15){   
              var year =  idCard15.substring(7,9);   
              var month = idCard15.substring(10,12);   
              var day = idCard15.substring(12,14);   
              var temp_date = new Date(year,parseFloat(month)-1,parseFloat(day));   
              // 对于老身份证中的你年龄则不需考虑千年虫问题而使用getYear()方法   
              if(temp_date.getYear()!=parseFloat(year)   
                      ||temp_date.getMonth()!=parseFloat(month)-1   
                      ||temp_date.getDate()!=parseFloat(day)){   
                         uni.showToast({
                             title: this.language.applyStore.IDFail1,
                             duration: 1500,
                             icon: 'none'
                         });
                        return false;   
                }else{   
                    return true;   
                }   
          }   ,
        //去掉字符串头尾空格   
        trim(str) {   
            return str.replace(/(^\s*)|(\s*$)/g, "");   
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
@import url('../../static/css/myStore/applyStore.less');


.rightInput1_area{
      display: flex;
      align-items: center;
      width: 100%;
      padding: 0 20rpx;
      box-sizing: border-box;
      border: 0.5px solid rgba(0, 0, 0, 0.1);
      border-radius: 8px;
      .input{
        height: 100%;
        flex: 1;
        border: none;
        outline: none;
      }
      uni-input{
        border: none !important;
      }
    .uni-input__area {
        display: flex;
        align-items: center;
        font-size: 32rpx;
        color: #333333;
        image {
          width: 10rpx;
          height: 10rpx;
          margin-left: 8rpx;
        }
    }
}
/deep/.uni-input-input{
    font-size: 32rpx;
    font-weight: 400 !important;
}
input{
    font-weight: 400 !important;
}
.content_row{
    display: flex;
    align-items: center;
    justify-content: flex-start;
    margin-bottom: 24rpx;
}

.content_row_left{
    font-size: 32rpx;
    color: #020202;
    margin-left: 12rpx;
    margin-right: 32rpx;
    line-height: 36rpx;
}

.content_row_right{
    flex: 1;
    // width:70%;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16rpx;
    height: 76rpx;
    font-size: 32rpx;
    border:1rpx solid rgba(0,0,0,.1);
    border-radius: 16rpx;
    input{
        font-size: 32rpx;
        flex: 1;
    }
    .uni-input{
        color: #999999;
        font-weight: 400;
        height: 48rpx;
    }
    .uni-input-val{
        color: #333333;
        font-weight: 400;
    }
}

.content_row_right>p{
    text-align: right;
    font-size: 32rpx;
    
    color: #999999;
    line-height: 32rpx;
    word-break: break-all;
}

.head_img{
    width: 90rpx;
    height: 90rpx;
    border-radius: 50%;
}

.jiantouDiv{
    display: flex;
    width: 32rpx;
    height: 44rpx;
    margin-left: 20rpx;
}
.jiantouDiv img{
    width: 100%;
    height: 100%;
}
.jiantouDivs{
    width: 32rpx;
    height: 44rpx;
    margin-left: 20rpx;
}
.jiantouDivs img{
    width: 100%;
    height: 100%;
}
.bottomBtn{
    height:98rpx;
}

.bottomBtn>div{
    .solidBtn();
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height:98rpx;
    line-height: 98rpx;
    text-align: center;
    font-size: 30rpx;
}


.mask {
    height: 100vh;
    width: 100%;
    background-color: rgba(000, 000, 000, 0.5);
    position: fixed;
    top: 0;
    left: 0;
    z-index: 34;
    display: flex;
    justify-content: center;
    align-items: center;
}

.mask_cont {
    text-align: center;
    font-size: 30rpx;
    color: #020202;
    background-color: #fff;
    border-radius: 23rpx;
}

.mask_cont>p {
    margin: 38rpx 0;
    font-size: 34rpx;
}

.mask_cont>input {
    width: 500rpx;
    padding-left: 20rpx;
    height: 66rpx;
    border-radius: 10rpx;
    margin: 0 40rpx 30rpx 40rpx;
    border: 1px solid #999999;
    font-size: 32rpx;
}

.mask_button {
    border-top: 1px solid #eee;
}

.mask_button>div {
    float: left;
    width: 50%;
    height: 84rpx;
    color: #020202;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30rpx;
}

.mask_button:after {
    display: block;
    content: "";
    clear: both;
}

.mask_button_left {
    border-right: 1px solid #eee;
    color: #9D9D9D !important;
}

.mask_cont textarea{
    width:480rpx;
    height:150rpx;
    background:rgba(255,255,255,1);
    border:1px solid rgba(221,221,221,1);
    border-radius:6rpx;
    margin: 0 35rpx 40rpx;
    text-align: left;
    padding: 20rpx;
    box-sizing: border-box;
    font-size: 28rpx;
}


.textLeng{
    position: absolute;right: 40rpx;bottom: 2rpx;
    font-size: 28rpx;
    line-height: 28rpx;
    color: #999999;
}

.cancellationContent{
    width:550rpx;
    min-height:280rpx;
    background:rgba(255,255,255,1);
    border-radius:23rpx;
}

.cancellationContent_text{
    margin: 62rpx auto 56rpx;
    text-align: center;
    line-height: 36.5rpx;
    font-size: 28rpx;
    color: #020202;
}

.cancellationContent_btn{
    display: flex;
    align-items: center;
    height: 94rpx;
    border-top: 1px solid #EEEEEE;
    box-sizing: border-box;
}

.cancellationContent_btn>div{
    flex: 1;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 34rpx;
    color: #999999;
}

.cancellationContent_btn>div:last-child{
    color: #020202;
    border-left: 1px solid #EEEEEE;
}

.contentBox .textarea{
    height: 208rpx;
    margin-bottom: 20rpx;
}

</style>
