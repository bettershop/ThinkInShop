<template>
    <view>
        <view class="mack" v-if="isMack">

        </view>
        <view class="select">
            <view class="content" :class="{hover: showItemList1, active: showItemList1, selected: product_class_id}" @click="handleItemClick1">
                <view class="item"  v-if="product_class == ''">{{language.productSelect.classify}}</view>
                <view class="item"  v-else>{{product_class}}</view>
            </view>
            <view class="content" :class="{hover: showItemList2, active: showItemList2, selected: product_class_id1}" @click="handleItemClick2">
                <view class="item"  v-if="product_class1 == ''">{{language.productSelect.subclass}}</view>
                <view class="item"  v-else>{{product_class1}}</view>
            </view>
            <view class="content" :class="{hover: showItemList3, active: showItemList3, selected: brand_id}" @click="handleItemClick3">
                <view class="item"  v-if="brand_class == ''">{{language.productSelect.brand}}</view>
                <view class="item"  v-else>{{brand_class}}</view>
            </view>
            <view class="content" :class="{hover: showGengduo, active: showGengduo, selected: proName}" @click="handleMoreClick">
                <view class="item" v-if="proName == ''">{{language.productSelect.more}}</view>
                    <view class="item"  v-else>{{proName}}</view>
            </view>
            <view class="item-list" v-if="showItemList1">
                <view :class="{ 'active': isActive&&product_class_id==item.cid }"
                      class="category"
                      v-for="(item,index) of product_class_list"
                      :key="item.cid"
                      @click="handleClassNameClick1(item.cid)" >
                    {{item.pname}}
                </view>
            </view>
            <view class="item-list" v-if="showItemList2">
                <view :class="{ 'active': product_class_id1==item.cid }" class="category" v-for="item of product_class_list1" :key="item.cid" @click="handleClassNameClick2(item.cid)" >
                    {{item.pname}}
                </view>
            </view>
            <view class="item-list" v-if="showItemList3">
                <view :class="{ 'active': brand_id==item.brand_id }" class="category" v-for="item of brand_class_list" :key="item.brand_id" @click="handleClassNameClick3(item.brand_id)" >
                    {{item.brand_name}}
                </view>
            </view>

            <!-- 点击更多的时候显示 start-->
            <view class="more" v-if="showGengduo">
                <view class="input-content">
                    <view class="product_name">{{language.productSelect.productName}}</view>
                    <input type="text" :placeholder="language.productSelect.productNamePlaceholder" class="moreinput" placeholder-style="padding-left: 20rpx;font-size:28rpx" v-model="proName">
                </view>
                <view class="more_btn">
                    <button type="warn" class="cancel" @tap='cancel()'>{{language.productSelect.cancel}}</button>
                    <button type="warn" class="end" @tap='check()'>{{language.productSelect.confirm}}</button>
                </view>
            </view>
        </view>
    </view>
    
</template>

<script>
    export default {
        name: "product-select",
        data () {
            return {
                showItemList1: false, //true: 显示商品一级分类  false: 不显示
                product_class:'',
                product_class1:'',
                showItemList2: false, //true: 显示商品二级分类  false: 不显示
                showItemList3: false, //true: 显示商品品牌  false: 不显示
                product_class_list: this.list1,
                product_class_list1:[],
                brand_class_list:[],
                showGengduo: false, //是否显示更多
                isMack: false,
                brand_class:'',
                isActive: false,
                product_class_id:'',
                product_class_id1:'',
                secondClass:[],
                proName:'',
                brand_id: ''
            }
        },
        props: {
            list1: {
                type: Array
            },
            list2: {
                type: Array
            }
        },
        watch: {
            list1 () {
                this.product_class_list = this.list1
            },
            list2 () {
                this.brand_class_list = this.list2
            }
        },
        created() {
            this.setLang();
        },
        methods: {
            // 取消
            cancel(){
                this.showGengduo = !this.showGengduo
                this.isMack = false;
                this.proName = '';
                this.goods_query()
            },
            // 确定
            check(){
                if(this.proName == ''){
                    uni.showToast({
                        title: this.language.productSelect.productNameTips,
                        duration:1500,
                        icon:'none'
                    })
                }
                this.showGengduo = !this.showGengduo
                this.goods_query()
                this.isMack = false
            },
            // 商品查询
            goods_query(){
                this.$emit('query', {
                    cid: this.product_class_id1 || this.product_class_id,
                    brand_id: this.brand_id,
                    product_title: this.proName
                })
            },
            // 点击显示一级分类列表
            handleItemClick1(){
                this.showItemList1 = !this.showItemList1
                this.showItemList2 = false
                this.showItemList3 = false
                this.showGengduo = false
                this.isMack = true;
                this.isMackClick();
            },
            // 点击显示二级分类列表
            handleItemClick2(){
                //判断如果你没有点击分类的话这里就不会显示（会有一个提示）
                if(this.product_class_list1.length === 0) {
                    uni.showToast({
                        title: this.language.productSelect.beforeClass,
                        duration: 2000,
                        icon: 'none'
                    });
                    return;
                }

                this.showItemList1 = false
                this.showItemList2 = !this.showItemList2
                this.showItemList3 = false
                this.showGengduo = false
                this.isMack = true;
                this.isMackClick();
            },
            //判断是否显示遮照层
            isMackClick(){
                if(this.showItemList1===false&&this.showItemList2===false&&this.showItemList3===false&&this.showGengduo===false){
                    this.isMack = false;
                }
            },
            // 点击显示品牌列表
            handleItemClick3(){
                this.showItemList1 = false
                this.showItemList2 = false
                this.showItemList3 = !this.showItemList3
                this.showGengduo = false
                this.isMack = true;
                this.isMackClick();
            },
            // 更多
            handleMoreClick(){
                this.showGengduo = !this.showGengduo
                this.showItemList3 = false
                this.showItemList2 = false
                this.showItemList1 = false
                this.isMack = true
                this.isMackClick()
            },
            // 选择一级分类
            handleClassNameClick1(e){
                this.isActive = true
                this.showItemList1 = !this.showItemList1
                this.product_class_id = e
                for(var i in this.product_class_list){
                    if(this.product_class_list[i].cid == e){
                        this.product_class = this.product_class_list[i].pname
                        this.secondClass = this.product_class_list[i].res
                    }
                }
                this.product_class_list1 = []
                this.product_class1 = ''
                this.product_class_id1 = ''
                for(var j in this.secondClass){
                    this.product_class_list1.push(this.secondClass[j])
                }
                if (this.product_class_list1.length) {
                    this.product_class_list1.unshift({
                        cid: '',
                        pname: '子类',
                    })
                }
                this.brand_id = '';
                this.brand_class = '';
                
                this.goods_query()
                this.isMack = false
            },
            // 选择二级分类
            handleClassNameClick2(e){
                this.showItemList2 = !this.showItemList2
                this.product_class_id1 = e
                for(var i in this.product_class_list1){
                    if(this.product_class_list1[i].cid == e){
                        this.product_class1 = this.product_class_list1[i].pname
                    }
                }
                this.brand_id = '';
                this.brand_class = '';
                this.goods_query()
                this.isMack = false
            },
            // 选择品牌
            handleClassNameClick3(e){
                this.showItemList3 = !this.showItemList3
                this.brand_id = e
                for(var i in this.brand_class_list){
                    if(this.brand_class_list[i].brand_id == e){
                        this.brand_class = this.brand_class_list[i].brand_name
                    }
                }
                this.goods_query()
                this.isMack = false
            },
        }
    }
</script>

<style scoped lang="less">

    @import url("@/laike.less");
    .select{
        position: fixed;
        display: flex;
        justify-content: space-around;
        align-items: center;
        width: 100vw;
        height: 100rpx;
        border-top:1rpx solid #EEEEEE;
        border-bottom:1rpx solid #EEEEEE;
        background-color: #FFFFFF;
        z-index: 10000;
    }
    .select .content{
        position: relative;
        &.selected {
            color: @tabColor;
        }
        &::after{
            content: '';
            position: absolute;
            top: -8rpx;
            bottom: 0;
            right: -24rpx;
            margin: auto;
            display: inline-block;
            width: 10rpx;
            height: 10rpx;
            border-left: 2rpx solid #999999;
            border-bottom: 2rpx solid #999999;
            border-right: 2rpx solid transparent;
            border-top: 2rpx solid transparent;
            transform: rotate(-45deg);
        }
        &.active::after {
            right: -24rpx;
            transform: rotate(130deg);
            top: 10rpx;
        }
        &.selected::after {
            border-left: 2rpx solid @tabColor;
            border-bottom: 2rpx solid @tabColor;
        }
        
    }
    
    .select .content.hover{
        color: @tabColor;
    }
    .select .content.hover::after{
        border-left: 2rpx solid @tabColor;
        border-bottom: 2rpx solid @tabColor;
    }
    .mack{
        position: fixed;
        width: 750rpx;
        height: 100%;
        background-color: rgba(0,0,0,0.5);
        z-index: 999;
    }
    .select .item{
        max-width: 110rpx;
        white-space: nowrap;
        overflow: hidden;
        font-size: 28rpx;
        text-overflow: ellipsis;
    }
    .item-icon{
        width: 10rpx;
        height: 10rpx;
    }
    .item-list{
        position: absolute;
        top: 100rpx;
        width: 750rpx;
        height: 320rpx;
        padding-left: 100rpx;
        background: #FFFFFF;
        z-index: 10000;
        overflow-y: scroll;
        font-size: 28rpx;
    }
    .item-list .category{
        float: left;
        width: 300rpx;
        height: 80rpx;
        line-height: 80rpx;

        color: #666666;
        
        &.active {
            color: @tabColor;
        }
    }
    .item-list .category:hover{
        background-color: #EEEEEE;
    }
    .isActive{
        background-color: #EEEEEE;
    }
    .more{
        position: absolute;
        top: 102rpx;
        width: 100vw;
        background-color: #FFFFFF;
        z-index: 1000;
        box-sizing: border-box;
    }
    .input-content{
        display: flex;
        flex-direction: column;
        padding: 0 30rpx;
        box-sizing: border-box;
    }
    .more .product_name{
        padding: 27rpx 0 20rpx 0;
        color: #666666;
        font-size: 26rpx;
    }
    .more .more_btn{
        display: flex;
        justify-content: space-around;
        align-items: center;
        width: 750rpx;
        height: 120rpx;
        border-top: 1px solid #E6E6E6;
    }
    .more_btn button{
        display: flex;
        align-items: center;
        justify-content: center;
        width: 330rpx;
        height: 80rpx;
        line-height: 0;
        border-radius: 8rpx;
        font-size: 28rpx;
    }
    .more_btn .cancel{
        .outBtn();
    }
    .more_btn .end{
        .solidBtn();
    }
    .more_btn .end:after{
        border: 0;
    }
    .moreinput{
        height: 70rpx;
        border: 1px solid #E6E6E6;
        width: 100%;
        border-radius: 6rpx;
        font-size: 28rpx;
        padding-left: 20rpx;
        box-sizing: border-box;
        color: #333;
        margin-bottom: 40rpx;
    }
</style>
