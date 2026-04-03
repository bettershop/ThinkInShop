<template>
    <div class="addStore-container" style="min-height: 100vh;">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        <heads
            v-if="store_id.length > 0"
            :title="language.myStore.expressAdd.bjsz"
            ishead_w="2"
            :bgColor="bgColor"
            titleColor="#333333"
        ></heads>
        <heads
            v-else
            :title="language.myStore.expressAdd.tjsz"
            ishead_w="2"
            :bgColor="bgColor"
            titleColor="#333333"
        ></heads>
        <div class="addressBox">
            
            <div class="addStore-list">
                <div>
                    <p><span style="color: #FA5151;margin-right: 8rpx;">*</span>{{ language.myStore.expressAdd.wlgs }}</p>
                    <div class="addStoreInput">
                        <picker style="width: 100%;" @change="bindPickerChange"  :range-key="'kuaidi_name'" :value="index" :range="array">
                            <view class="uni-input">{{array[index]?array[index].kuaidi_name:language.shipments.express[1]}}</view>
                        </picker>
                        <img :src="jiantou" alt="" />
                    </div>
                </div>
            </div>
            
            
        </div>

        <div class="addressBox1"> 
            <div class="addStore-list">
                <div>
                    <p><span style="color: #FA5151;margin-right: 8rpx;">*</span>partnerId</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            :placeholder="
                                language.myStore.expressAdd.qsrzhxx
                            "
                            :placeholder-style="placeColor"
                            v-model="partnerId"
                            maxlength="20"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list">
                <div>
                    <p>partnerKey</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            :placeholder="language.myStore.expressAdd.qsrzhmm"
                            :placeholder-style="placeColor"
                            v-model="partnerKey"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list">
                <div>
                    <p>partnerSecret</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            :placeholder="language.myStore.expressAdd.qsrmy"
                            :placeholder-style="placeColor"
                            v-model="partnerSecret"
                            maxlength="50"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list">
                <div>
                    <p>partnerName</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            :placeholder="language.myStore.expressAdd.qsrkhmc"
                            :placeholder-style="placeColor"
                            v-model="partnerName"
                            maxlength="50"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list">
                <div>
                    <p>net</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            :placeholder="language.myStore.expressAdd.qsrwd"
                            :placeholder-style="placeColor"
                            v-model="net"
                            maxlength="50"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list">
                <div>
                    <p>code</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            :placeholder="language.myStore.expressAdd.qsrczbh"
                            :placeholder-style="placeColor"
                            v-model="code"
                            maxlength="50"
                        />
                    </div>
                </div>
            </div>
            <div class="addStore-list">
                <div>
                    <p>checkMan</p>
                    <div class="addStoreInput">
                        <input
                            type="text"
                            :placeholder="language.myStore.expressAdd.qsrkdym"
                            :placeholder-style="placeColor"
                            v-model="checkMan"
                            maxlength="50"
                        />
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
            <div class="bottomItem" v-if="edit">
                <button class="addStore-btn1" @tap="remove">
                    {{ language.addStore.del }}
                </button>
                <button class="addStore-btn" @tap="add">
                    {{ language.addStore.save }}
                </button>
            </div>
            <div class="bottomItem" v-else>
                <button class="addStore-btn" @tap="add">
                    {{ language.addStore.save }}
                </button>
            </div>
            
        </div>


    </div>
</template>

<script>

export default {
    data() {
        return {
            title: "",
            
            partnerId:'',
            partnerKey:'',
            partnerSecret:'',
            partnerName:'',
            net:'',
            code:'',
            checkMan:'',
            
            array: [],
            index: 0,
 
            shop_id: "",

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

            businessIndex: 1,
            
    
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            express_id: '',
        };
    },
    onLoad(option) {
        this.shop_id = this.$store.state.shop_id;
        if(option.express_id){
            this.express_id = option.express_id
        }
        this.getlog()
        if (option.id) {
            this.edit = option.id;
            this._edit_page();
        }
    },
    onShow() {
        this.title = this.language.myStore.express.mdsz;
    },
    methods: {
        
        bindPickerChange: function(e) {
                    this.index = e.detail.value
        },

        remove() {
            let me = this;
            var data = {
                api:"mch.App.Mch.del_logistics",
                id: this.edit,
            };

            uni.showModal({
                title: this.language.addStore.scts,
                confirmText: this.language.showModal.confirm,
                cancelText: this.language.showModal.cancel,
                content: this.language.myStore.expressAdd.qdsc,
                confirmColor: "#D73B48",
                success: function (res) {
                    if (res.confirm) {
                        // 隐藏做门店编辑，做完解除
                        me.$req.post({ data }).then((res) => {
                            
                            me.is_text = me.language.chooseGoods.sccg
                            me.is_sus = true
                            setTimeout(() => {
                                me.is_sus = false
                                uni.navigateBack({
                                    delta: 1,
                                });
                            }, 1500);
                        });
                    } else if (res.cancel) {
                    }
                },
            });
        },
        // 物流列表
        getlog() {
            var data = {
                api:"mch.App.Mch.get_logistics",
            };
            if(this.express_id != ''){
                data.id = this.express_id
            }
            this.$req.post({ data }).then((res) => {
                uni.hideLoading();
                if (res.code == 200) {
                    this.array = res.data.list
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: "none",
                    });
                }
            });
        },
        // 编辑页面
        _edit_page() {
            uni.showLoading({
                title: this.language.showLoading.loading,
                mask: true,
            });
            var data = {
                api:"mch.App.Mch.edit_logistics_page",
                id: this.edit,
            };
            this.$req.post({ data }).then((res) => {
                uni.hideLoading();
                if (res.code == 200) {
                    this.partnerId=res.data.list.partnerId
                    this.partnerKey=res.data.list.partnerKey
                    this.partnerSecret=res.data.list.partnerSecret
                    this.partnerName=res.data.list.partnerName
                    this.net=res.data.list.net
                    this.code=res.data.list.code
                    this.checkMan=res.data.list.checkMan
                    for(let i=0;i<=this.array.length;i++){
                        if(this.array[i].id==res.data.list.express_id){
                            this.index=i
                        }
                    }
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: "none",
                    });
                }
            });
        },
        // 保存
        preservation() {
            if (this.btn_status) {
                this.btn_status = false;

            }
        },
        // 添加
        add() {
            uni.showLoading({
                title: this.language.showLoading.loading,
                mask: true,
            });
            var data = {
                api:"mch.App.Mch.add_logistics",
                express_id:this.array[this.index].id,
                partnerId: this.partnerId,
                partnerKey: this.partnerKey,
                partnerSecret: this.partnerSecret,
                partnerName: this.partnerName,
                net: this.net,
                code: this.code,
                checkMan: this.checkMan,
            };
            if(this.edit){
                data.id = this.edit
            }
            this.$req
                .post({ data })
                .then((res) => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        
                        if(this.edit){
                            this.is_text = this.language.putForward.bjcg
                        }else{
                            this.is_text = this.language.putForward.tjcg
                        }
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
        
    },
    components: {
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

.addressBox{
    background-color: #ffffff;
    border-radius: 0px 0px 24rpx 24rpx;
    padding:32rpx 32rpx 8rpx;
}
.addressBox1{
    background-color: #ffffff;
    border-radius: 24rpx;
    padding:32rpx 32rpx 8rpx;
    margin-top: 24rpx;
}
.addStore-list {
    position: relative;
}

.addStore-list>div {
    font-size: 32rpx;
    color: #333333;
    font-size: 32rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-bottom: 24rpx
}

.addStore-list p {
    padding: 16rpx 0;
    min-width: 210rpx;
    font-weight: 400;
    margin-right: 60rpx;
    font-weight: 400 !important;
}
.xieyi {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 99;
    display: flex;
    justify-content: center;
    align-items: center;
    > view {
        width: 640rpx;
        min-height: 100rpx;
        max-height: 486rpx;
        background: #ffffff;
        border-radius: 24rpx;
        .xieyi_btm {
            height: 108rpx;
            color: @D73B48;
            display: flex;
            justify-content: center;
            align-items: center;
            border-top: 0.5px solid rgba(0, 0, 0, 0.1);
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

.addStore-list .addStoreInput{
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex: 1;
    height: 76rpx;
    font-weight: 400 !important;
    color: #333333;
    border-radius:16rpx;
    padding: 16rpx;
    border: 1rpx solid rgba(0,0,0,.1);
    >uni-view >span{
        font-weight: 400 !important;
    }
    input {
        flex: 1;
        margin-right: 32rpx;
        font-size: 32rpx;
    }
    input::placeholder{
        font-weight: 400;
    }
    /deep/.uni-input-input{
        font-size: 32rpx;
        
    }
    .noValue{
        font-size: 32rpx;
        
        font-weight: 400;
        color: rgb(184, 184, 184);
    }
    .youValue{
        font-size: 32rpx;
        font-family: PingFangSC-Regular, PingFangSC-Regular;
        font-weight: normal;
        color: #333333;
    }
}
.addStore-list img {
    width: 32rpx;
    height: 44rpx;
}

.addStore-list .xuanze{
    width: 32rpx;
    height: 32rpx;
    margin-left: auto;
}

.addStore-btn, .addStore-btn1{
    width: 690rpx;
    height: 90rpx;
    line-height: 90rpx;
    border: 0;
    outline: 0;
    margin: 0 auto;
    font-size: 32rpx;
}
.addStore-btn:after{
    border: 0;
}

.mask {
    height: 100vh;
    width: 100%;
    background-color: rgba(000, 000, 000, 0.5);
    position: fixed;
    top: 0;
    left: 0;
    z-index: 999;
    display: flex;
    justify-content: center;
    align-items: center;
}


.btn{
        position: fixed;
        bottom: 0;
        z-index: 99;
        width: 100%;
        min-height: 124rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffffff;
        box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
    }
.bottomItem {
    button{
        width: 100%;
        height: 100%;
        text-align: center;
        border-radius: 52rpx;
    }
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 686rpx;
    height: 92rpx;
    font-size: 32rpx;
    color: #fff;
    text-align: center;
    line-height: 92rpx;
    margin: 16rpx auto;
    padding:0;
    
}
.addStore-btn1{
    .outBtn();
    margin-right: 22rpx;
}
.addStore-btn{
    .solidBtn();
}

</style>
