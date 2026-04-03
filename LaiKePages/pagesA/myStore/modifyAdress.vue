<template>
    <div class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.storeModifyAdress.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        
        <div class="content">
            
            <div class="content_row newBg">
                <div class="content_row_left">{{language.storeModifyAdress.area}}</div>
                
                <div class="content_row_right" @click="showMulLinkageThreePicker()">
                    <input type="text" disabled='true' @focus="hideKeyboard()" :placeholder="language.storeModifyAdress.areaplacehold" v-model="city_all"
                           placeholder-style="color:#999999"/>
                    <div class="jiantouDiv">
                        <img :src="jiantou" alt="" />
                    </div>
                </div>
            </div>
            
            <div class="content_row newBg">
                <div class="content_row_left">{{language.storeModifyAdress.address}}</div>
                
                <div class="content_row_right">
                    <input v-model="address" type="text" :placeholder="language.storeModifyAdress.addressPlacehold"  placeholder-style="color:#999999">
                </div>
            </div>
            
        </div>
        
        
        <view class="btn">
            <div class='bottom' @tap="saveOk">{{language.storeModifyAdress.save}}</div>
        </view>
        
        <mpvue-city-picker :themeColor="themeColor" ref="mpvueCityPicker" :pickerValueDefault="cityPickerValueDefault"
                           @onConfirm="onConfirm"></mpvue-city-picker>
        
    </div>
</template>

<script>
    import mpvueCityPicker from '@my-miniprogram/src/components/mpvue-citypicker/mpvueCityPicker'; 
    
    import provinceData from '@my-miniprogram/src/components/mpvue-citypicker/city-data/province.js';
    import cityData from '@my-miniprogram/src/components/mpvue-citypicker/city-data/city.js';
    import areaData from '@my-miniprogram/src/components/mpvue-citypicker/city-data/area.js'; 
    
export default {
    data() {
        return {
            title: '修改联系地址',
            
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            city_all: '',
            address: '',
            
            themeColor: '#D73B48',
            cityPickerValueDefault: [0, 0, 0],
            shop_id: '',
            
            pageType: '', //是否是从申请店铺进入
            bgColor:[{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
        };
    },
    onLoad(option) {
        this.shop_id = option.shop_id
        this.city_all = option.userAdd
        this.address = option.address
        this.pageType = option.pageType
        
        if(this.city_all){
            this.$nextTick(()=>{
                var arr = this.city_all.split('-')
                
                provinceData.some((item,index)=>{
                    if(item.label == arr[0]){
                        arr[0] = index
                    }
                })
                
                cityData[arr[0]].some((item,index)=>{
                    if(item.label == arr[1]){
                        arr[1] = index
                    }
                })
                
                areaData[arr[0]][arr[1]].some((item,index)=>{
                    if(item.label == arr[2]){
                        arr[2] = index
                    }
                })
                
                this.cityPickerValueDefault = arr
            })
        }
    },
    methods: {
		changeLoginStatus(){
			
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
        
        saveOk(){
            if(this.pageType == 'apply'){
                if(!this.city_all){
                    uni.showToast({
                        title: this.language.storeModifyAdress.areaplacehold
                    })
                    return
                }
                if(!this.address){
                    uni.showToast({
                        title: this.language.storeModifyAdress.addressPlacehold
                    })
                    return
                }
                uni.setStorageSync('applyCity',this.city_all)
                uni.setStorageSync('applyAddress',this.address)
                uni.navigateBack({
                    delta: 1
                })
                return
            }
            if(this.address == ''){
                uni.showToast({
                    title: '请输入完整的地址！',
                    duration: 1500,
                    icon: 'none'
                });
                return
            }
            // this.city_all   所在地区
            // this.address   详细地址
            let data = {
                
                api:"mch.App.Mch.Set_shop",
                shop_id: this.shop_id,
                city_all: this.city_all,
                address: this.address,
            }
            
            this.$req.post({data}).then(res=>{
                if (res.code == 200) {
                    uni.showToast({
                        title: this.language.storeModifyAdress.succse,
                        duration: 1500,
                        icon: 'none'
                    });
                    setTimeout(() => {
                        uni.navigateBack({
                            delta: 1
                        })
                    }, 1500);
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            })
        }
    },
    components:{ mpvueCityPicker }
};
</script>

<style lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/storeSetup.less');
.bottom{
    .solidBtn()
}
.newBg{
    margin: 0;
    border: 0;
    padding:24rpx 32rpx;
    border-radius: 0px 0px 24rpx 24rpx;
    &:nth-child(1){
        border-radius: 0;
        padding-bottom: 0;
    }
    .content_row_right{
        height: 38px;
        display: flex;
        align-items: center;
        position: relative;
        flex: 1;
        overflow-x: auto;
        border: 0.5px solid rgba(0, 0, 0, 0.1);
        border-radius: 8px;
        padding: 8px;
        box-sizing: border-box;
        font-size: 32rpx;
                
        font-weight: 500;   
        color: #333333;
        input{
            flex: 1;
                  
            color: #333333;
        }
        /deep/.uni-input-input{
            
            font-weight: 500; 
            font-size: 32rpx;
        }
    }
}
</style>
