<template>
    <div style='min-height: 100vh;'>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.addStock.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <div class='load' v-if='load'>
            <div>
                <img :src="loadGif"/>
                <p>{{language.addStock.load}}…</p>
            </div>
        </div>
        <div v-else class='relative' :style="{marginTop:halfWidth}">
            <div class='attrList table' :style='{width:tableList}'>
                <div class='attrListHead tr'>
                    <div class='list2 th' v-for='(item,index) in attr' :key='index'>
                        {{item.attrName}}
                    </div>
                    <div class='list1 th'>{{language.addStock.inventory}}</div>
                    <div class='list1 th'>{{language.addStock.warning}}</div>
                    <div class='list1 th'>{{language.addStock.addInventory}}</div>
                </div>
                <div class='attrListBody tr' v-for='(item,index) in attrList' :key="index">
                    <div v-if='item.length>0' class='tr attrListTr1'>
                        <div class='list2  aa td' v-for='(items,indexs) in item' :key='indexs'>
                            <span>{{item[indexs].value}}</span>
                        </div>
                        <div class='list1 td bcd' :class='{"redColor":item[0].stock<item[0].stockWarn}'>
                            {{item[0].stock}}
                        </div>
                        <div class='list1 td'>{{item[0].stockWarn}}</div>
                        <div class='list1 td'><input @input='checkNum($event,item[0],index)' type="text"
                                                     v-model="item[0].addStockNum" :style="{color:item[0].addStockNum==0?'#666666':''}"></div>
                    </div>
                    <div v-else class='tr'>
                        <div class='list2  td bb'>
                            <span>{{item.value}}</span>
                        </div>
                        <div class='list1 td asd' :class='{"redColor":item.stock<item.stockWarn}'>{{item.stock}}</div>
                        <div class='list1 td'>{{item.stockWarn}}</div>
                        <div class='list1 td'><input @input='checkNum($event,item,index)' type="text"
                                                     v-model="item.addStockNum" :style="{color:item.addStockNum==0?'#666666':''}"></div>
                    </div>
                </div>
            </div>
            <view class="btn">
                <div class='bottom'  @tap='_sub()'>{{language.addStock.save}}</div>
            </view>
        </div>
        <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    {{language.addStock.tips[1]}}
                </view>
            </view>
        </view>
    </div>
</template>

<script>
    export default {
        data () {
            return {
                back: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x_w.png',
                addStockNum: '',
                title: '库存管理',
                
                loadGif: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/loading.gif',
                load: false,
                shop_id: '',
                p_id: '',
                attr: '',
                attrList: '',
                fastTap: true,
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ],
                is_sus:false,
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
            }
        },
        onLoad (option) {
            this.p_id = option.p_id
            this.isLogin(()=>{
				
				this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id
				this._axios()
			})
        },
        onShow() {
            uni.setNavigationBarTitle({
                title: this.language.addStock.title
            })
        },
        methods: {
            changeLoginStatus () {
                this._axios()
            },
            _back1 () {
                var showModal = false
                if (this.attrList[0].length > 1) {
                    for (let i = 0; i < this.attrList.length; i++) {
                        if (this.attrList[i][0].addStockNum != 0) {
                            showModal = true
                        }
                    }
                } else {
                    for (let i = 0; i < this.attrList.length; i++) {
                        if (this.attrList[i].addStockNum != 0) {
                            showModal = true
                        }
                    }
                }
                
                if (!showModal) {
                    uni.navigateBack({
                        delta: 1
                    })
                } else {
                    uni.showModal({
                        title: this.language.showModal.hint,
                        content: this.language.showModal.submit,
						confirmText: this.language.showModal.confirm,
						cancelText: this.language.showModal.cancel,
                        success: (res) => {
                            if (res.confirm) {
                                this._sub()
                            } else {
                                uni.navigateBack({
                                    delta: 1
                                })
                            }
                        }
                    })
                }
            },
            checkNum (e, attr, index) {
                if (Number(e.target.value)) {
                    if (Math.abs(e.target.value) >= attr.stock) {
                        uni.showToast({
                            title: this.language.addStock.tips[0],
                            duration: 1500,
                            icon: 'none'
                        })
                        attr.addStockNum = 0
                    }
                }
            },
            _sub () {
                if (!this.fastTap) {
                    return
                }
                this.fastTap = false
                var changeNum = []
                if (this.attrList[0].length > 1) {
                    for (var i = 0; i < this.attrList.length; i++) {
                        changeNum.push(this.attrList[i][0].addStockNum)
                    }
                } else {
                    for (var i = 0; i < this.attrList.length; i++) {
                        changeNum.push(this.attrList[i].addStockNum)
                    }
                }

                changeNum = changeNum.join(',')
                
                let atrList = this.attrList.map((item,index) => {
                    return {
                        id: item[0].cid,
                        pid: this.p_id,
                        num: item[0].addStockNum
                    }
                })
                this.$req.post({
                    data: {
                        
                        shop_id: this.shop_id,
                        
                        api:"mch.App.Mch.Up_stock",
                        number: changeNum,
                        
                        attributeInfo: JSON.stringify(atrList)
                    }
                }).then(res=>{
                    let { code, data, message } = res;
                    this.fastTap = true
                    if (code == 200) {
                        this.is_sus = true
                        setTimeout(function () {
                            this.is_sus = false
                            uni.navigateBack({
                                delta: 1
                            })
                        }, 1500)
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                }).catch(error=>{
                    this.fastTap = true
                })
            },
            _axios () {
                this.$req.post({
                    data: {
                        shop_id: this.shop_id,
                        api:"mch.App.Mch.Up_stock_page",
                        p_id: this.p_id,
                    }
                }).then(res=>{
                    let { code, data, message } = res;
                    if (code == 200) {
                        this.attr = data.attr
                        this.attrList = data.attrList
                    } else {
                        uni.showToast({
                            title: message,
                            icon: 'none',
                            duration: 1500
                        })
                    }
                })
            }
        },
        computed: {
            tableList: function () {
                if (this.attr.length == 0) {
                    var width = 750
                    return uni.upx2px(width) + 'px'
                } else {
                    var width = this.tableWidth * 2 + 130 * 5
                    return uni.upx2px(width) + 'px'
                }
            },
            halfWidth: function () {
            }
        },
    }
</script>

<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/myStore/addStock.less");
</style>
