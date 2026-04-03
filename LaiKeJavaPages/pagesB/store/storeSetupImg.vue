<template>
    <view class="">
        <heads
            :title="type == 2?language.storeSetup.xctsz:language.storeSetup.lgsz"
            :ishead_w="2"
            :bgColor="bgColor"
            :titleColor="'#333333'"
        ></heads>
        <view class="mybody">
            <view class="mybody_img">
                <img :src="img" alt="">
            </view>
            <view class="myspan">
                <span v-if='type==3'>{{language.storeSetup.sm120}}</span>
                <span v-if='type==2'>{{language.storeSetup.sm200}}</span>
            </view>
            <view class="btn">
                <div class="bottom" @tap="_changeImg(type)">
                    {{language.storeSetup.ghtp}}
                </div>
            </view>
        </view>
    </view>
</template>

<script>
    export default {
        data() {
            return {
                title: '',
                shop_id:'',
                type:'',
                img:'',
                bgColor: [
                    {
                        item: "#ffffff",
                    },
                    {
                        item: "#ffffff",
                    },
                ],
                type:'',
            };
        },
        onLoad(option) {
            this.LaiKeTuiCommon.getLKTApiUrl()
            this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
            this.type=option.type
            this._axios()
            this.type = option.type
        },
        methods: {
            _changeImg(type) {
                var me = this;
                let formData = {
                    access_id: me.access_id,
                    shop_id: me.shop_id,
                    api:"mch.App.Mch.Set_shop",
                };
            
                uni.chooseImage({
                    count: 1,
                    success: function (res) {
                        if (me.type == 2) {
                            // 宣传图
                            formData.posterImg = res.tempFilePaths[0];
                        } else if (me.type == 3) {
                            // logo
                            formData.logoImg = res.tempFilePaths[0];
                        }
                        uni.uploadFile({
                            url: uni.getStorageSync("url"),
                            filePath: res.tempFilePaths[0],
                            name: "file",
                            formData,
                            success: function (uploadFileRes) {
                                var data = uploadFileRes.data;
                                var json = {};
                                // #ifndef H5
                                if (me.platform1 == "android") {
                                    let str = data
                                        .replace("\r\n/g", "")
                                        .replace(/\n/g, "")
                                        .replace(/\r/g, "")
                                        .replace(/\\/g, "");
                                    var retData = str.split(",");
                                    json.code = retData[0].split(":")[1];
                                } else {
                                    json = JSON.parse(data);
                                }
                                // #endif
            
                                // #ifdef H5
                                json = JSON.parse(data);
                                // #endif
            
                                if (json.code == 200) {
                                    uni.showToast({
                                        title: me.language.storeSetup.Tips[8],
                                        icon: "none",
                                    });
                                    setTimeout(function () {
                                        me._axios();
                                    }, 1000);
                                } else {
                                    if (me.platform1 != "android") {
                                        uni.showToast({
                                            title: me.language.storeSetup.Tips[9],
                                            duration: 1500,
                                            icon: "none",
                                        });
                                    }
                                }
                            },
                        });
                    },
                });
            },
            _axios() {
                this.$req
                    .post({
                        data: {
                            api:"mch.App.Mch.Into_set_shop",
                            shop_id: this.shop_id,
                        },
                    })
                    .then((res) => {
                        if (res.code == 200) {
                            var data = res.data.list[0];
                            if(this.type==3){
                                this.img = data.logo;
                            }else if(this.type==2){
                                this.img = data.posterImg;
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
        }
    };
</script>

<style>
    .mybody{
        min-height: calc(100vh - 100rpx);
        background-color: #F4F5F6;
        
    }
    .mybody_img{
        padding: 32rpx 32rpx 16rpx 32rpx;
        
    }
    .mybody_img img{
        width: 100%;
        border-radius: 16rpx;
    }
    .myspan{
        margin: 0 32rpx;
        font-size: 28rpx;
        color: #999999;

    }
    .btn{
            position: fixed;
            bottom: 0;
            width: 100%;
            height: 124rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #ffffff;
            padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
            padding-bottom: env(safe-area-inset-bottom);
            border:1px solid #ffffff
        }
        .bottom {
            width: 686rpx;
            height: 92rpx;
            font-size: 32rpx;
            color: #fff;
            text-align: center;
            line-height: 92rpx;
            border-radius: 52rpx;
            margin: 0 auto;
            padding:0;
            background: #FA5151;
            font-size: 32rpx;
            color: #ffffff;
        }
</style>
