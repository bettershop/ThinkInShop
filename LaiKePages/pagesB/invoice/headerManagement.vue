<template>
    <view class="container">
        <view class="position_head">
            <heads :title="language.headerManagement.title" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        </view>
        <ul class="content" v-if="invoiceHeaderList.length">
            <li class="content_li" v-for="(item,index) in invoiceHeaderList" :key="index">
                <view class="content_li_left">
                    <p class="li_left_top" :class="{ 'active' : !item.company_tax_number }">
                        <span v-if="item.is_default == 1" class="default">{{language.headerManagement.mr}}</span>
                        <span class="headerTitle">{{item.company_name}}</span>
                    </p>
                    <p class="li_left_bottom" v-if="item.company_tax_number">
                        {{ item.company_tax_number }}
                    </p>
                </view>
                <view class="content_li_right">
                    <p @tap="delHeader(item)">{{language.headerManagement.del}}</p>
                    <p class="hr"></p>
                    <p @tap="editorHeader(item)">{{language.headerManagement.bj}}</p>
                </view>
            </li>
        </ul>
        <view class="no_header" v-else>
            <image :src="no_header_info" mode="heightFix"></image>
            <p>{{language.headerManagement.meiy}}</p>
        </view>
        <view class="btn3" v-if="!invoiceHeaderList.length">
            <p @tap="toAddHeadeer">{{language.headerManagement.add}}</p>
        </view>
        <view class="btn" v-if="invoiceHeaderList.length">
            <p @tap="toAddHeadeer">{{language.headerManagement.add}}</p>
        </view>
        <show-toast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj"></show-toast>

    </view>
</template>

<script>
import showToast from "@/components/aComponents/showToast.vue"
    export default {
        data() {
            return {
                 //提示
                 is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
                is_showToast_obj: {}, //imgUrl提示图标  title提示文字
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                title: '我的发票抬头',
                invoiceHeaderList: [],
                no_header_info: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/no_header_info.png',
                bgColor: [
                    {
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
            }
        },
        
        onShow() {
            this.getList()
        },
        
        onLoad(option) {
            
        },
        components: {
            showToast
        },
        
        methods: {
            getList() {
                let data = {
                    api: 'app.invoiceHeader.getList',
                }
                
                this.$req.post({data}).then(res => {
                    if(res.code == 200) {
                        this.invoiceHeaderList = res.data.list
                        if(this.invoiceHeaderList.length == 0) {
                            this.title = '我的发票抬头'
                        } else {
                            this.title = '抬头管理'
                        }
                    }
                    
                })
                .catch(err => {
                    uni.showToast({
                        title: err.message,
                        duration: 1500,
                        icon: 'none'
                    });
                })
            },
            toAddHeadeer() {
                uni.navigateTo({
                    url: '/pagesB/invoice/addInvoiceHeader'
                })
            },
            delHeader(item) {
                let data = {
                    api: 'app.invoiceHeader.del',
                    ids: item.id
                }
                
                this.$req.post({data}).then(res => {
                    if(res.code == 200) {
                        
                        this.is_showToast = 1
                            this.is_showToast_obj.imgUrl = this.sus
                            this.is_showToast_obj.title =  this.language.zdata.sccg
                            setTimeout(() => {
                                this.is_showToast = 0
                            }, 1500)
                        this.getList()
                    }
                })
                .catch(err => {
                    uni.showToast({
                        title: err.message,
                        duration: 1500,
                        icon: 'none'
                    });
                })
            },
            editorHeader(item) {
                uni.navigateTo({
                    url: '/pagesB/invoice/addInvoiceHeader?id=' + item.id
                })
            },
        },
    }
</script>

<style lang="less" scoped> 
    @import url("@/laike.less");
    @import url('../../static/css/invoice/headerManagement.less');
</style>
