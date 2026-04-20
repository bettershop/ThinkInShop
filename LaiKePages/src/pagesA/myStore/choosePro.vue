<template>
    <view class="container">
        <heads :title="title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        
        <template v-if="!is_selected">
            <view class="search">
                <view>
                    <view class='search_input'>
                        <image class="searchImg" :src="serchimg" alt="">
                        <input v-model="name" type="text" :placeholder="language.choosePro.searchName"/>
                        <image v-show="name.length > 0" @click="cleardata" class="cancel" :src="sc_icon" mode=""></image>
                    </view>
                    <view class='search_btn' @tap='_search'>{{language.choosePro.searchBtn}}</view>
                </view>
            </view>
            
            <view class="hr"></view>
            
            <view class="proBox">
                <view class="prolist" v-for="item,index of proList" :key="item.id" @click="checkPro(item)">
                    <image class="circleImg" :src="item.checked?circle_hei:circle_hui" mode=""></image>
                    
                    <image class="prolist_img" :src="item.imgurl" mode=""></image>
                    
                    <view class="prolist_right">
                        <view class="prolist_right_title">{{item.product_title}}</view>
                        <view class="prolist_right_price">
                            <view>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.price)}}</view>
                            <text>{{language.choosePro.inventory}} {{item.num}}</text>
                        </view>
                    </view>
                </view>
            </view>
            
            <view class="bottomBox" v-if="proList.length >= 1">
                <view class="qxBox" @tap="clickAll">
                    <image class="circleImg" :src="checkAll&&checkNum!=0?circle_hei:circle_hui" mode=""></image>
                    <text class="text">{{language.choosePro.checkAll}}</text>
                </view>
                <view class="textBox">
                    {{language.choosePro.selected}}<view>{{checkNum}}</view>{{language.choosePro.items}}
                </view>
                <view class="btn" @tap="addOk">{{language.choosePro.add}}</view>
            </view>
            
            <view class="nodata" v-if="proList.length < 1">
                <image :src="noPro" mode=""></image>
                <p>{{language.choosePro.zwsp}}~</p>
            </view>
        </template>
        
        <template v-else>
            
            <view class="proBoxa">
                <view class="prolist" v-for="item,index of checkList" :key="item.id" @click="checkPro1(item)">
                    <image v-if="modifyType" class="circleImg" :src="item.checked?circle_hei:circle_hui" mode=""></image>
                    
                    <image class="prolist_img" :src="item.imgurl" mode=""></image>
                    
                    <view class="prolist_right">
                        <view class="prolist_right_title">{{item.product_title}}</view>
                        <view class="prolist_right_price">
                            <view>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.price)}}</view>
                            <text>{{language.choosePro.inventory}} {{item.num}}</text>
                        </view>
                        <view class="prolist_right_btn" @click="delpro(item,index)">
                            <view>{{language.choosePro.ycsp}}</view>
                        </view>
                    </view>
                </view>
            </view>
            
            <view class="nodata" v-if="checkAddList.length < 1">
                <image :src="noPro" mode=""></image>
                <p>{{language.choosePro.zwkxsp}}~</p>
            </view>
            
            <view v-if="modifyType" class="bottomBox">
                <view class="qxBox" @click="qxClick">
                    <image class="circleImg" :src="checkNum1 == checkList.length&&checkNum1!=0?circle_hei:circle_hui" mode=""></image>
                    <text class="text">{{language.choosePro.checkAll}}</text>
                </view>
                <view class="textBox">
                    {{language.choosePro.selected}}<view>{{checkNum}}</view>{{language.choosePro.items}}
                </view>
                <view class="btn" @tap="delCheck">{{language.choosePro.del}}</view>
            </view>
            <view v-else class="btn_box">
                <view class="w_btn" @tap="changeTab">{{language.choosePro.continueAdd}}</view>
            </view>
        </template>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: '选择商品',
            serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
            circle_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png',
            circle_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_yxz.png',
            noPro: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noPro.png',
            sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+ 'images/icon1/delete2x.png',
            
            proList: [],
            name: '',
            loadingType: 0,
            bgColor: [{
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            modifyType: false,
            checkList: [],
            shop_id:'',
            page: 1,
            
            checkProListId: '',
            checkAddList: [],
            is_selected: false,
        };
    },
    created(option) {
        let checkProList = uni.getStorageSync('checkProList')?uni.getStorageSync('checkProList'):[]
        if(checkProList.length>0){
            this.is_selected = true
            this.checkList = checkProList
        }
        
        if((!uni.getStorageSync('checkProList')) && uni.getStorageSync('checkProListId')){
            this.is_selected = true
            this.checkProListId = uni.getStorageSync('checkProListId')
        }
        
        this.axios()
    },
    onShow() {
        this.title = this.language.choosePro.title
        
        let checkProList = uni.getStorageSync('checkProList')?uni.getStorageSync('checkProList'):[]
        if(checkProList.length>0){
            this.title = this.language.choosePro.title1
        }
        
        if((!uni.getStorageSync('checkProList')) && uni.getStorageSync('checkProListId')){
            this.title = this.language.choosePro.title1
        }
        
    },
    onReachBottom(){
        if (this.loadingType != 0) {
            return
        }
        this.loadingType = 1
        this.page++
        this.axios()
    },
    computed:{
        checkAll(){
            let flag = this.proList.some(item=>{
                return item.checked == false
            })
            return !flag
        },
        checkNum(){
            let num = 0
            this.checkAddList.filter(item=>{
                if(item.checked){
                    num += 1
                }
            })
            return num
        },
        checkNum1(){
            let num = 0
            this.checkList.filter(item=>{
                if(item.checked){
                    num += 1
                }
            })
            return num
        }
    },
    onBackPress() {
        uni.setStorageSync('checkProList',this.checkList)
    },
    methods: {
        delpro(e,l){
            var index = l
                    uni.showModal({
                        title: this.language.showModal.hint,
                        content: this.language.choosePro.qryc,
                        confirmText: this.language.showModal.confirm,
                        cancelText: this.language.showModal.cancel,
                        cancelColor: '#333333 !important',
                        confirmColor: '#D73B48 !important',
                        success: (e) => {
                            if (e.confirm) {
                                this.checkList.splice(index, 1)　
                                
                            }
                        }
                    })
                },
        cleardata(){
            this.name = ''  
        },
        clickAll(){
            if(!this.checkAll){
                this.proList.filter(item=>{
                    item.checked = true
                })
                this.checkAddList = this.proList
            }else{
                this.proList.filter(item=>{
                    item.checked = false
                })
                this.checkAddList = []
            }
        },
        delCheck(){
            if(this.checkNum1 == 0){
                uni.showToast({
                    title: this.language.choosePro.chooseProTips,
                    icon: 'none'
                })
                return
            }
            
            let checkList = []
            
            this.checkList.filter(item=>{
                if(!item.checked){
                    checkList.push(item)
                }
            })
            
            this.checkList = checkList
            
            this.checkAddList = checkList
            console.dir(this.checkAddList)
            
            let checkProListId = ''
            this.checkList.filter(items=>{
                checkProListId += ',' + items.id
            })
            this.checkProListId = checkProListId.replace(',','')
            
        },
        changeTab(){
            this.is_selected = false
            this.title = this.language.choosePro.title
            this.proList = []
            this.axios()
        },
        addOk(){
            let checkList = []
            this.proList.filter(item=>{
                if(item.checked){
                    item.checked = false
                    checkList.push(item)
                }
            })
            
            if(checkList.length == 0){
                uni.showToast({
                    title: this.language.choosePro.chooseProTips,
                    icon: 'none'
                })
                return
            }
            
            this.checkList = checkList
            this.proList = []
            
            this.is_selected = true
            this.title = this.language.choosePro.title1
            uni.setStorageSync('checkProList',this.checkList)
            console.dir(this.is_selected)
            uni.navigateBack({})
        },
        qxClick(){
            if(!this.is_selected){
                let proList = this.proList
                let flag = this.checkNum == proList.length
                
                proList.filter(item=>{
                    item.checked = !flag
                })
                this.proList = proList
            }else{
                let checkList = this.checkList
                let flag = this.checkNum1 == checkList.length
                
                checkList.filter(item=>{
                    item.checked = !flag
                })
                this.checkList = checkList
            }
            
        },
        clickModify(){
            this.modifyType = !this.modifyType
            
            if(this.checkList){
                this.checkList.filter(item=>{
                    item.checked = false
                })
            }
            
        },
        checkPro(item){
            item.checked = !item.checked
            
            if(item.checked){
                this.checkAddList.push(item)
            }else{
                let i = this.checkAddList.findIndex(items=>{
                    return items.id == item.id
                })
                if(i>=0){
                    this.checkAddList.splice(i,1)
                }
            }
        },
        checkPro1(item){
            if(this.modifyType){
                item.checked = !item.checked
            }
        },
        _search(){
            this.page = 1;
            this.proList = []
            this.axios();
        },
        axios(){
            let data = {
                
                api:"plugin.coupon.AppMchcoupon.Product",
                mch_id: uni.getStorageSync('shop_id'), // 店铺id
                name:this.name, // 商品名称
                page:this.page, // 页码
            }
            
            this.$req.post({data}).then(res=>{
                if(res.code == 200){
                    let checkAddList = JSON.parse(JSON.stringify(this.checkAddList))
                    checkAddList.filter(item=>{
                        item.checked = true
                    })
                    if(this.checkList.length>0){
                        let checkProListId = ''
                        this.checkList.filter(items=>{
                            if(!checkAddList.some(item=>item.id==items.id)){
                                items.checked = true
                                checkAddList.push(items)
                            }
                            
                            checkProListId += ',' + items.id
                        })
                        this.checkAddList = checkAddList
                        
                        this.checkProListId = checkProListId.replace(',','')
                    }
                    
                    
                    res.data.filter(item=>{
                        item.checked = false
                        
                        checkAddList.filter(it=>{
                            if(it.id == item.id){
                                item.checked = true
                            }
                        })
                    })
                    
                    // 编辑商品时
                    if(this.checkProListId){
                        let checkProListId = this.checkProListId.split(',')
                        let checkList = []
                        res.data.filter(items=>{
                            checkProListId.filter(item=>{
                                if(items.id == item){
                                    items.checked = true
                                    
                                    checkList.push(items)
                                }
                            })
                        })
                        this.checkList = checkList
                        uni.setStorageSync('checkProList',checkList)
                    }
                    
                    this.proList.push(...res.data)
                    
                    if (res.data.length > 0) {
                        this.loadingType = 0
                    } else {
                        this.loadingType = 2
                    }
                }else{
                    uni.showToast({
                        title: res.message,
                        icon: 'none'
                    })
                }
            })
        }
    },
};
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/choosePro.less');
</style>
