<template>
    <div class="addStore-container" style="min-height: 100vh;">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        <heads
            v-if="store_id.length > 0"
            :title="language.addStore.title1"
            ishead_w="2"
            :bgColor="bgColor"
            titleColor="#333333"
        ></heads>
        <heads
            v-else=""
            :title="language.addStore.title"
            ishead_w="2"
            :bgColor="bgColor"
            titleColor="#333333"
        ></heads>
        <div class="addressBox">
            <div class="addStore-list">
                <div>
                    <p>{{ language.addStore.storeName }}</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            :placeholder="
                                language.addStore.storeNamePlaceholder
                            "
                            :placeholder-style="placeColor"
                            v-model="shop_name"
                            maxlength="20"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list">
                <div>
                    <p>{{ language.addStore.tel }}</p>
                    <div class="addStoreInput">
                        <div class="uni-input__area" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
                        {{ code2?'+' + code2 : '国家/地区' }}
                           <image :src="down" mode="aspectFill"></image>
                        </div>
                        <input
                            type="text"
                            :placeholder="language.addStore.telPlaceholder"
                            :placeholder-style="placeColor"
                            v-model="shop_mobile"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list" @tap="bindPickerState()">
                <div>
                    <p>{{ language.addStore.times }}</p>
                    <div class="addStoreInput">
                        <view v-if="time_all">
                            <span>{{ time_all }}</span></view
                        >
                        <view class="noValue" v-else>{{
                            language.addStore.timesPlaceholder
                        }}</view>
                        <img :src="jiantou" alt="" />
                    </div>
                </div>
            </div>
            <div class="addStore-list" @tap="showMulLinkageThreePicker()" v-if="international">
                <div>
                    <p>{{ language.addStore.city }}</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            @focus="hideKeyboard()"
                            disabled
                            :placeholder="language.addStore.cityPlaceholder"
                            v-model="city_all"
                            :placeholder-style="placeColor"
                        />
                        <img :src="jiantou" alt="" />
                    </div>
                </div>
            </div>
            <div class="addStore-list" v-if="international">
                <div>
                    <p>{{ language.addStore.address }}</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            :placeholder="language.addStore.addressPlaceholder"
                            :placeholder-style="placeColor"
                            v-model="shop_address"
                            maxlength="50"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list" v-if="!international">
                <div>
                    <p>街道地址</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            placeholder="请输入街道地址"
                            :placeholder-style="placeColor"
                            v-model="shop_address"
                            maxlength="50"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list" v-if="!international">
                <div>
                    <p>所在城市</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            placeholder="请输入所在城市"
                            :placeholder-style="placeColor"
                            v-model="city"
                            maxlength="50"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list" v-if="!international">
                <div>
                    <p>州/省/地区</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            placeholder="请输入州/省/地区"
                            :placeholder-style="placeColor"
                            v-model="province"
                            maxlength="50"
                        />
                    </div>
                </div>
            </div>            
            <div class="addStore-list">
                <div @tap="_default">
                    <p>{{ language.addStore.setDefault }}</p>
                        <switch
                            color="#fa5151"
                            style="transform: scale(0.8)"
                            :checked="is_default == 1"
                        />
                </div>
            </div>
        </div>
        <!-- 暂时隐藏账号管理  -->
<!--        <div class="addressBox1 addStore-list" v-if="edit"> 
            <div>
                <p>{{ language.addStore.zhgl }}</p>
                <div class="addStoreInput">
                    <div @tap="_account" style="flex: 1" class="youValue">
                        {{ language.addStore.ckzh }}
                    </div>
                    <img :src="jiantou" alt="" />
                </div>
            </div>
        </div> -->
        <!-- 核销时间 -->
<!--       <div class="addressBox1 addStore-list" v-if="!edit">
            <div>
                 <span class="must">*</span>
                <p>{{ language.addStore.hxsj }}</p>
                <div class="addStoreInput">
                    <div @tap="writeo" style="flex: 1" class="youValue2">
                        {{ language.addStore.qsz }}
                    </div>
                    <img :src="jiantou" alt="" />
                </div>
            </div>
        </div> -->
        <!-- 营业状态弹窗 -->
        <div class="mask" v-if="pickerState">
            <div class="maskBox">
                <p class="maskBox_title">
                    <span></span>
                    <span>{{ language.addStore.xzyyzt }}</span>
                    <img class="closeImg" :src="closeImg" @tap="_closeState" />
                </p>
                <div class="maskBox_content">
                    <div class="maskBox_choose">
                        <div
                            class="state_box"
                            :class="{ stateActive: businessIndex == 1 }"
                            @tap="changeJobState(1)"
                        >
                            <img
                                :src="
                                    businessIndex == 0
                                        ? storeSetup_time
                                        : storeSetup_time2
                                "
                                class="stateImg"
                            />
                            <span class="state_text">{{
                                language.addStore.yyz
                            }}</span>
                        </div>
                        <div
                            class="state_box"
                            :class="{ stateActive: businessIndex == 0 }"
                            @tap="changeJobState(0)"
                        >
                            <img
                                :src="
                                    businessIndex == 1
                                        ? storeSetup_close
                                        : storeSetup_close2
                                "
                                class="stateImg"
                            />
                            <span class="state_text">{{
                                language.addStore.ydy
                            }}</span>
                        </div>
                    </div>
                    <div class="timeBtn">
                        <div class="timeBtn_box" @tap="openDatetimePicker">
                            <span v-if="!time_all">{{
                                language.addStore.szyysj
                            }}</span>
                            <span v-else>{{ time_all }}</span>
                            <img :src="jiantou" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- 添加编辑成功 -->
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
        <div class="btn shaco">
            <div class="bottomItem">
                <button v-if="edit" class="addStore-btn1" @tap="remove">
                    {{ language.addStore.del }}
                </button>
                <button class="addStore-btn" @tap="preservation">
                    {{ language.addStore.save }}
                </button>
            </div>
        </div>

        <mpvue-city-picker
            ref="mpvueCityPicker"
            :themeColor="themeColor"
            :pickerValueDefault="cityPickerValueDefault"
            @onConfirm="onConfirm"
        ></mpvue-city-picker>

        <!-- 时间选择 -->
        <time-picker
            ref="timePicker"
            :themeColor="themeColor"
            :urseTime="time_ys"
            @onConfirm="onConfirm1"
        ></time-picker>
    </div>
</template>

<script>
import mpvueCityPicker from '@my-miniprogram/src/components/mpvue-citypicker/mpvueCityPicker'; 
import timePicker from "../../components/time-picker.vue";
import {img} from "@/static/js/login/imgList.js";
export default {
    data() {
        return {
            title: "",
            themeColor: "#D73B48",
            cityPickerValueDefault: [0, 0, 0],
            pickerText: "",
            shop_id: "",
            shop_name: "", // 门店名称
            shop_mobile: "", // 门店电话
            time_all: "", //时间段
            time_ys: "", // 原始时间
            city_all: "", //最终选择省市区的值
            shop_address: "", // 详细地址
            edit: "",
            btn_status: true,
            placeColor: "color:#b8b8b8",
            is_sus:false,
            is_text:'',
            sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
            jiantou:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/jiantou2x.png",
            xuanzehei:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/xuanzehei2x.png",
            xuanzehui:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/xuanzehui2x.png",
            is_default: 0,

            store_id: "",
            closeImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/close2x.png",
            storeSetup_close:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/storeSetup_close.png",
            storeSetup_close2:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/storeSetup_close2.png",
            storeSetup_time:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/storeSetup_time.png",
            storeSetup_time2:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/storeSetup_time2.png",
            pickerState: false,
            businessIndex: 1,
            storeType: 1, //1添加门店，2为编辑门店
            userAccount: "", //用户账号
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            code2: '86', 
            down: img(this).down,
            international: false, //是否国际化地址
            city: '',
            province: '',
        };
    },
    onLoad(option) {
        this.shop_id = uni.getStorageSync('mch_id');
        this.edit = option.edit;

        if (option.type && option.type == 1) {
            this.storeType = option.type;
            // 添加门店的时候获取该账号信息
            this.getUserInfo();
        } else {
            this.storeType = 2;
        }
           this.store_id = option.id || '';
    },
    onUnload() {
        uni.removeStorageSync('diqu')
    },
    async onShow() { 
        let diqu =  JSON.parse(uni.getStorageSync('diqu') || `{}`)
         
        if(this.edit ){
            await this._edit_page();
        }else{
            this.code2 = diqu?.code2
            uni.removeStorageSync('diqu')
            
            if (this.code2 === '86' || this.code2 === '852') {
                  this.international = true
            } else {
                  this.international = false
            }
        }
        
       
        this.title = this.language.addStore.tjxxmd;
    },
    methods: {
        getUserInfo() {
            var data = {
                api: "app.user.index",
            };
            this.$req.post({ data }).then((res) => {
                if (res.code == 200) {
                    this.userAccount = res.data.data.user.user_name;
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: "none",
                    });
                }
            });
        },
        _account() {
            this.navTo("/pagesC/myStore/accountManagement?mch_store_id=" + this.store_id +'&shop_id=' +this.shop_id);
        },
        writeo(){
            this.navTo("/pagesB/myStore/verificationManagement");
        },
        _default() {
            if (this.is_default == 0) {
                this.is_default = 1;
            } else {
                this.is_default = 0;
            }
        },
        bindPickerState() {
            this.$refs.timePicker.show(this.time_ys);
        },
        _closeState() {
            this.pickerState = false;
        },
        changeJobState(e) {
            this.businessIndex = e;
            if (this.businessIndex == 0) {
                var is_open = 2;
            } else {
                var is_open = 1;
            }
        },
        remove() {
            let me = this;
            var data = {
                api:"mch.App.Mch.Del_store",
                id: this.store_id,
                shop_id: this.shop_id,
            };

            uni.showModal({
                title: this.language.addStore.scts,
                confirmText: this.language.showModal.confirm,
                cancelText: this.language.showModal.cancel,
                content: this.language.addStore.qdsc,
                confirmColor: "#D73B48",
                success: function (res) {
                    if (res.confirm) {
                        // 隐藏做门店编辑，做完解除
                        me.$req.post({ data }).then((row) => {
                            if(row.code == 200){
                                me.is_text = me.language.chooseGoods.sccg
                                me.is_sus = true
                                setTimeout(() => {
                                    me.is_sus = false
                                    uni.navigateBack({
                                        delta: 1,
                                    });
                                }, 1500);
                            }else{
                                uni.showToast({
                                    title:row.message,
                                    icon:'none',
                                })
                            }
                        });
                    } else if (res.cancel) {
                    }
                },
            });
        },
        // 点击城市
        showMulLinkageThreePicker() {
            this.$refs.mpvueCityPicker.show();
        },
        hideKeyboard() {
            uni.hideKeyboard();
        },
        // 城市-确定
        onConfirm(e) {
            this.city_all = e.label;
        },
        // 营业时间-确定
        onConfirm1(e) {
            var start = e[0].replace(/undefined/g, "00");
            var end = e[1].replace(/undefined/g, "00");
            if (start === end) {
                uni.showToast({
                    title: "开始时间不能等于结束时间",
                    duration: 1500,
                    icon: "none",
                });
                return;
            }
            this.time_all = start + "~" + end;
            this.time_ys = this.time_all;
        },
        // 点击营业时间
        openDatetimePicker() {
            this.$refs.timePicker.show(this.time_ys);
        },
        // 编辑页面
        async  _edit_page() {
            uni.showLoading({
                title: this.language.showLoading.loading,
                mask: true,
            });
            var data = {
                api:"mch.App.Mch.Edit_store_page",
                id: this.store_id,
                shop_id: this.shop_id,
            };

            const res = await this.$req.post({ data })
        
            uni.hideLoading();
            try{ 
                if (res && res.code == 200) {
                    this.shop_name = res.data.list.name;
                    this.shop_mobile = res.data.list.mobile;
                    this.time_all = res.data.list.business_hours[0];
                    this.time_ys = res.data.list.business_hours[0];
                    this.city_all =
                        res.data.list.sheng +
                        "-" +
                        res.data.list.shi +
                        "-" +
                        res.data.list.xian;
                    this.shop_address = res.data.list.address;
                    this.is_default = res.data.list.is_default;
                    this.province = res.data.list.province;
                    this.city = res.data.list.city;
                    this.code2 = res.data.list.cpc;
                    
                    let diqu =  JSON.parse(uni.getStorageSync('diqu') || `{}`)
                    if(Object.keys(diqu || {}).length > 0){
                        this.code2 = diqu?.code2 
                        uni.removeStorageSync('diqu')
                    } 
                    
                    if (this.code2 === '86' || this.code2 === '852'){
                        this.international = true;
                    }else{
                        this.international = false;
                    }
                
                    
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: "none",
                    });
                } 
            }catch(e){
                console.error(e)
            }
        },
        // 保存
        preservation() {
            if (this.btn_status) {
                this.btn_status = false;
                if (this.storeInfoCheck()) {
                    if (this.edit) {
                        this._edit();
                    } else {
                        this.add();
                    }
                }
            }
        },
        /**
         * 前端检测门店信息
         */
        storeInfoCheck() {
            if (!this.shop_name) {
                uni.showToast({
                    title: this.language.addStore.storeNamePlaceholder,
                    duration: 1000,
                    icon: "none",
                });
                this.btn_status = true;
                return false;
            }

            if (!this.shop_mobile) {
                uni.showToast({
                    title: this.language.addStore.telPlaceholder,
                    duration: 1000,
                    icon: "none",
                });
                this.btn_status = true;
                return false;
            }

            if (!this.time_ys) {
                uni.showToast({
                    title: this.language.addStore.timesPlaceholder,
                    duration: 1000,
                    icon: "none",
                });
                this.btn_status = true;
                return false;
            }

            if (!this.city_all) {
                uni.showToast({
                    title: this.language.addStore.addressPlaceholder,
                    duration: 1000,
                    icon: "none",
                });
                this.btn_status = true;
                return false;
            }
            return true;
        },
        // 添加
        add() {
            uni.showLoading({
                title: this.language.showLoading.loading,
                mask: true,
            });
            var data = {
                api:"mch.App.Mch.Add_store",
                shop_id: this.shop_id,
                name: this.shop_name,
                mobile: this.shop_mobile,
                business_hours: this.time_all,
                city_all: this.city_all,
                address: this.shop_address,
                is_default: this.is_default,
                cpc: this.code2,
            };

            this.$req
                .post({ data })
                .then((res) => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        this.is_text = this.language.putForward.tjcg
                        this.is_sus = true
                        setTimeout(function () {
                           this.is_sus = false
                            uni.navigateBack({
                                delta: 1,
                            });
                        }, 1500);
                    } else {
                        this.btn_status = true;
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                })
                .catch((error) => {
                    this.btn_status = true;
                });
        },
        // 编辑
        _edit() {
            uni.showLoading({
                title: this.language.showLoading.loading,
                mask: true,
            });
            var data = {
                api:"mch.App.Mch.Edit_store",
                id: this.store_id,
                shop_id: this.shop_id,
                name: this.shop_name,
                mobile: this.shop_mobile,
                business_hours: this.time_all,
                city_all: this.city_all,
                address: this.shop_address,
                is_default: this.is_default,
                cpc: this.code2,
                province: this.province || '',
                ciyt: this.city || ''
            };
            this.$req.post({ data }).then((res) => {
                uni.hideLoading();
                if (res.code == 200) {
                    this.is_text = this.language.putForward.bjcg
                    this.is_sus = true
                    setTimeout(function () {
                        this.is_sus = false
                        uni.navigateBack({
                            delta: 1,
                        });
                    }, 1500);
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: "none",
                    });
                }
            });
        },
    },
    components: {
        mpvueCityPicker,
        timePicker,
    },
};
</script>
<style>
page {
    background-color: #f4f5f6;
}
</style>
<style scoped lang="less">
@import url("@/laike.less");
@import url("../../static/css/myStore/addStore.less");
    .uni-input__area {
        display: flex;
        align-items: center;
        font-size: 32rpx;
        color: #333333;
        margin-right: 12rpx;
        image {
          width: 10rpx;
          height: 10rpx;
          margin-left: 8rpx;
        }
    }
</style>
