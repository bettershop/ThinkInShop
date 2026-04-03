<template>
    <view class="content">
        <!-- 头部 -->
        <heads
            :title="title"
            :titleColor="titleColor"
            :bgImg="headBg"
            :ishead_w="fanhui"
        ></heads>
        <!-- 列表 -->
        <view class="list" v-for="(item, index) in shopList" :key="index" :style="{borderRadius: index == 0 ? '0 0 24rpx 24rpx':'24rpx'}">
            <view>
                <image :src="item.choose ? quanHei : quanHui" @tap="_choose(index)"></image>
                <span>{{item.name}}</span>
            </view>
        </view>
        <!-- 底部按钮 -->
        <view class="submit" @tap="_back">
            <view>
                <view>
                    已选择
                    <span>{{chooseNum}}</span>
                    门店
                </view>
                <view>
                    保存
                </view>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return{
            title: "选择门店",
            titleColor: "#333333", //标题字体颜色
            fanhui: 2,
            headBg: "#ffffff",
            quanType: false,
            quanHui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
            quanHei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
            shop_id: '',//
            shopList: [],
            listName: '',//选择的门店name 空为全部
            listId: '',//选择的门店id 空为全部
            chooseNum: 0,//选择了几个门店
        }
    },
    onLoad(option) {
        this.shop_id = option.shop_id || ''
        this._axios()
    },
    onShow() {},
    methods: {
        _back(){
            if(this.chooseNum == this.shopList.length){
                this.listName = ''
                this.listId = ''
                uni.removeStorageSync('uploadProXnShopListName')
                uni.removeStorageSync('uploadProXnShopListId')
            } else {
                uni.setStorageSync('uploadProXnShopListName', this.listName)
                uni.setStorageSync('uploadProXnShopListId', this.listId)
            }
            uni.navigateBack()
        },
        _axios(){
            let data = {
                api: 'mch.App.Mch.getWriteStore',
                mchId: this.shop_id
            } 
            this.$req.post({data}).then(res=>{ 
                if(res.code == 200){
                    this.shopList = res.data
                    //数据处理
                    if(uni.getStorageSync('uploadProXnShopListId')){
                        let uploadProXnShopListId = uni.getStorageSync('uploadProXnShopListId')
                        let list = uploadProXnShopListId.split(',')
                        let listName = ''
                        let listId = ''
                        let chooseNum = 0
                        this.shopList.forEach((item, index)=>{
                            this.$set(item, 'choose', false)
                            list.forEach((itm, idx)=>{
                                if(item.id == itm){
                                    this.shopList[index].choose = true
                                    chooseNum++
                                    if(listName == ''){
                                        listName = item.name
                                    } else {
                                        listName = listName + ' ' + item.name
                                    }
                                    if(listId == ''){
                                        listId = item.id + ''
                                    } else {
                                        listId = listId + ',' + item.id
                                    }
                                }
                            })
                        })
                        this.listName = listName
                        this.listId = listId
                        this.chooseNum = chooseNum
                    } else {
                        this.shopList.forEach((item, index)=>{
                            this.chooseNum++
                            this.$set(item, 'choose', true)
                        })
                    }
                } else {
                    uni.showToast({
                        title: res.message,
                        icon: 'none'
                    })
                }
            })
        },
        _choose(index){
            this.shopList[index].choose = !this.shopList[index].choose
            this.$nextTick(()=>{
                let listName = ''
                let listId = ''
                let chooseNum = 0
                this.shopList.forEach((item, index)=>{
                    if(item.choose){
                        chooseNum++
                        if(listName == ''){
                            listName = item.name
                        } else {
                            listName = listName + ' ' + item.name
                        }
                        if(listId == ''){
                            listId = item.id + ''
                        } else {
                            listId = listId + ',' + item.id
                        }
                    }
                })
                this.listName = listName
                this.listId = listId
                this.chooseNum = chooseNum
                if(!this.chooseNum){
                    uni.showToast({
                        title: '至少选择一个核销门店',
                        icon: 'none'
                    })
                    this._choose(index)
                }
            })
        },
    },
};
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped> 
    .content{
        .list{
            display: flex;
            flex-direction: column;
            justify-content: center;
            background-color: #ffffff;
            padding: 32rpx;
            box-sizing: border-box;
            margin-bottom: 16rpx;
            >view{
                display: flex;
                align-items: center;
                >image{
                    width: 32rpx;
                    height: 32rpx;
                    margin-right: 24rpx;
                }
                >span{
                    font-size: 32rpx;
                    color: rgba(0,0,0,0.75);
                }
            }
        }
        .submit {
            position: fixed;
            bottom: 0;
            width: 100%;
            z-index: 999;
            height: 124rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #ffffff;
            box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
            >view{
                flex: 1;
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 32rpx;
                box-sizing: border-box;
                >view:first-child{
                    font-size: 32rpx;
                    color: #666666;
                    >span{
                       color: #FA5151;
                       margin: 0 12rpx;
                    }
                }
                >view:last-child{
                    font-size: 28rpx;
                    color: #FFFFFF;
                    padding: 16rpx 68rpx;
                    box-sizing: border-box;
                    background: linear-gradient( 270deg, #FF6F6F 0%, #FA5151 100%);
                    border-radius: 36rpx;
                }
            }
        }
    }
</style>
