<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.freight.title" returnR="9" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <toload v-if="!loadFlag"></toload>
        <view v-else class="container" style="padding-bottom: 132rpx;">
          
            <view class="list">
                <view class="list-add" v-for="item,index of list" :key="index" 
                    @touchstart="touchStart($event,index)"
                    @touchmove="touchMove($event,item)" 
                    @touchend="touchend($event,item)" 
                     > 
                        <view class="content_left" @tap.shop="go_freight_add(item.is_use,item.id,item)" :style="{ transform: `translateX(${item.btnContainerLeft}px)` }">
                            <view class="freight_name">
                                <!-- <p class="default" v-if="item.is_default == 1">{{language.freight.default1}}</p> -->
                                <image v-if="item.is_default == 1" :src="mrmb"></image>
                                <p class="feright_name">{{ item.name }}</p>
                            </view>
                            <view class="freight_status">
                                <span v-if="item.is_use == 1">{{language.freight.beUsed}}</span>
                                <span v-else>{{language.freight.unused}}</span>
                            </view>
                        </view>
                        <view class="content_right">
                            <view class="set_default default" @tap="setDefault(item.id)" v-if="item.is_default != 1">{{language.freight.swmr}}</view> 
                            <view class="feright_edit edit" v-if="item.is_use != 1"@tap.shop="go_freight_add(item.is_use,item.id,item)">{{language.freight.edit}}</view>
                            <view class="feright_edit del" v-if="item.is_default != 1 && item.is_use != 1" @tap.stop="delAddressItem(item)" >{{language.freight.del}}</view>
                        </view>  
                </view>
            </view>
             
            <uni-load-more v-if='list.length>16' :loadingType="loadingType"></uni-load-more>
            
            <view class="noList" v-if='list.length == 0'>
                <image :src="noFreight"></image>
                <text>{{language.freight.noFreight}}</text>
            </view>
            
            <view v-if='list.length == 0' class="container_btn_noList" @tap="toUrl('/pagesA/myStore/freight_add?type=add')">{{language.freight.add}}</view>
            
        </view>
        
        
        <view class="container_btn" v-if='list.length != 0'>
            <view  class="container_bottomBtn" @tap="toUrl('/pagesA/myStore/freight_add?type=add')">{{language.freight.add}}</view>
        </view>
        <showToast
            :is_showToast="is_showToast" 
            :is_showToast_obj="is_showToast_obj" 
            @richText="is_showToast = 0" 
            @confirm="delFreight">
        </showToast>
    </view>
</template>

<script>
import showToast from '@/components/aComponents/showToast.vue'
export default {
    data() {
        return {
            loadFlag: false,
            title: '运费设置',
            shop_id: '',
            page: 1,
            list: [],
            loadingType: 0,
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
            noFreight: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/dizhiNo.png', 
            mrmb: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mrmb.png', 
            bgColor: [
                {
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            is_showToast:0,
            is_showToast_obj:{
                title:"是否删除当前地址",
                content:'是否删除当前地址?',
                button : '取消',
                endButton : '确认'
            },
            freightIds :0,
        };
    },
    components:{ 
        showToast
    },
    onShow() {
        this.isLogin()
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
        this.page = 1
        this.axios()
    },
    onReachBottom() {
        if(this.loadingType!=0){
            return
        }
        this.loadingType = 1
        this.page++
        this.axios()
    },
    methods: {
        touchStart(e,index) {
            this.startX = e.touches[0].clientX;
        },
        touchMove(e,item) { 
          const num =  item.is_use != 1 ? 3 : 1
          const moveX = e.touches[0].clientX;
          const deltaX =  this.startX  - moveX; 
          if(item.is_default){
              // 已经设置为默认就不显示操作按钮
              return
          }
          if (deltaX < 0) { // 向右滑动
            item.btnContainerLeft = Math.min(0,0)  
          } else {
           const translateX =  item.is_default == 0 ? (-60 * num)  : -60 / num
           // 再减去 左边设置的内边距
            item.btnContainerLeft = Math.min(0,translateX - 15);
          }
        },
        touchend(e,item){ 
            // const moveX =100
            // const deltaX =  this.startX - moveX;
            // const num =  item.is_use == 0 ? 3 :2 
            // if(item.is_default){
            //     // 已经设置为默认就不显示操作按钮
            //     return
            // }
            // if (deltaX < 0) { // 向右滑动
            //   item.btnContainerLeft = 0  
            // } else { 
            //  const translateX = item.is_default == 0 ? (-65 * num)  : -120 / num
            //   item.btnContainerLeft = Math.min(0,translateX); // 假设按钮最多显示150px 
            // }
        }, 
        go_freight_add(isUse,id,item){
            item.btnContainerLeft = 0
           if(isUse == 1) {
              this.toUrl('/pagesA/myStore/freight_add?type=see&id='+id)
           }else{
               this.toUrl('/pagesA/myStore/freight_add?type=edit&id='+id)
           }
        },
        delAddressItem(item){
          this.is_showToast = 4
          this.freightIds = item.id
        },
        delFreight(){
            this.is_showToast = 0
            uni.showLoading({
                title:this.language.showLoading.waiting
            })
            this.$req.post({
                data:{
                    api:'mch.Mch.Freight.DelFreight',
                    freightIds:this.freightIds
                }
            }).then(res=>{
                uni.hideLoading()
                if(res.code == 200){
                    this.message('删除成功')
                    uni.showToast({
                        icon:'success',
                        title:this.language.zdata.czcg
                    })
                }else{
                    uni.showToast({
                        icon:"error",
                        title:res.message
                    })
                }
            })
        },
        setDefault(id){
            let data = {
                api:"mch.App.Mch.Set_default",
                shop_id: this.shop_id, // 店铺ID
                id: id // 运费id
            }
            
            this.$req.post({data}).then(res=>{
                uni.showToast({
                    title: res.message,
                    icon: 'none'
                })
                if(res.code == 200){
                    uni.pageScrollTo({
                        scrollTop: 0,
                        duration: 10
                    })
                    
                    this.page = 1
                    this.axios()
                }
            })
        },
        changeLoginStatus(){
			this.page = 1
			this.axios()
		},
        toUrl(url){
            uni.navigateTo({
                url
            })
        },
        axios(){
            let data = {
                api:"mch.App.Mch.Freight_list",
                shop_id:this.shop_id, // 店铺ID
                page:this.page, // 加载次数
                pagesize: '20' // 每页显示多少条
            }
            
            this.$req.post({data}).then(res=>{
                let { code, message, data } = res
                if(code == 200){
                    if(this.page == 1){
                        this.list = []
                    }
                    this.list.push(...data.list)
                    this.list.forEach(v=>{
                        if(!Object.hasOwn(v,'btnContainerLeft')){
                            this.$set(v,'btnContainerLeft',0)
                        }
                    })
                    if(data.list.length>0){
                        this.loadingType = 0
                    }else{
                        this.loadingType = 2
                    }
                }else{
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    })
                }
                
                this.loadFlag = true
            })
        }
    }
};
</script>

<style>
   page {
       background-color: #f4f5f6;
   }
</style>
<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/freight.less');
</style>
