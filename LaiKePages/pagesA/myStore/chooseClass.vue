<template>
    <view class="container">
        <heads :title="language.chooseClass.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        
        <view class="content">
            <view class="list" v-for="items,indexs of data" :key="indexs">
                <view class="list_top list_item">
                    <label class="label" @tap.stop="clickCheck(indexs)">
                        <image class="circleImg" :src="items.checked?circle_hei:circle_hui" mode=""></image>
                        <text>{{items.name}}</text>
                    </label>
                    <image v-if="items.children&&items.children.length>0" @tap.stop="checkOpen(items)" class="jiantou" :src="jiantou" :style="items.open?'transform: rotate(180deg);':''"></image>
                </view>
                
                <view class="list_bottom" v-if="items.open">
                    <view class="list_bottom_item" v-for="item,index of items.children" :key="index">
                        <view class="list_item">
                            <label class="label" @tap="clickCheck(indexs,index)">
                                <image class="circleImg" :src="item.checked?circle_hei:circle_hui" mode=""></image>
                                <text>{{item.label}}</text>
                            </label>
                            <image v-if="item.children&&item.children.length>0" @tap="clickBottom(indexs,index)" class="jiantou" :src="jiantou" :style="item.open?'transform: rotate(180deg);':''"></image>
                        </view>
                        
                        <template v-if="item.open">
                            <view class="list_item pl_60" v-for="it,i of item.children" :key="i">
                                <label class="label" @tap="clickCheck(indexs,index,i)">
                                    <image class="circleImg" :src="it.checked?circle_hei:circle_hui" mode=""></image>
                                    <text>{{it.label}}</text>
                                </label>
                            </view>
                        </template>
                        
                    </view>
                </view>
            </view>
            <view class="nodata" v-if="data.length<1">
                <image :src="noPro" mode=""></image>
                <p>{{language.chooseClass.nhzwfl}}~</p>
            </view>
        </view>
        

        <view  class="btn_box" v-if="data.length>0">
            <view class="w_btn" @tap="saveOk">{{language.chooseClass.save}}</view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: '选择分类',
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jianX.png',
            circle_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png',
            circle_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_yxz.png',
            chooseMe: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/chooseMe.png',
            noPro: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noPro.png',
            bgColor: [{
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            classList: [],
            data: [],
        };
    },
    created(option) {
        this.axios()
    },
    computed:{
        checkId(){
            let id = []
            
            this.data.filter(items=>{
                if(items.checked){
                    id.push(items.id)
                }
                
                if(items.children&&items.children.length>0){
                    items.children.filter(item=>{
                        if(item.checked){
                            id.push(item.id)
                        }
                        
                        if(item.children&&item.children.length>0){
                            item.children.filter(it=>{
                                if(it.checked){
                                    id.push(it.id)
                                }
                            })
                        }
                    })
                }
            })
            
            return id.join()
        },
        checkText(){
            let text = []
            
            this.data.filter(items=>{
                if(items.checked){
                    text.push(items.label)
                }
                
                if(items.children&&items.children.length>0){
                    items.children.filter(item=>{
                        if(item.checked){
                            text.push(item.label)
                        }
                        
                        if(item.children&&item.children.length>0){
                            item.children.filter(it=>{
                                if(it.checked){
                                    text.push(it.label)
                                }
                            })
                        }
                    })
                }
            })
            
            return text.join()
        }
    },
    methods: {
        checkOpen(items){
            items.open = !items.open
        },
        saveOk(){
            if(!this.checkId){
                uni.showToast({
                    title: this.language.chooseClass.saveFail,
                    icon: 'none'
                })
                return
            }
            
            uni.setStorageSync('chooseClass',this.checkId)
            uni.setStorageSync('chooseClassText',this.checkText)
            
            uni.navigateBack({
                delta: 1
            })
        },
        clickCheck(indexs,index,i){
            let items = this.data[indexs]

            if(index>=0){
                let item = items.children[index]
                
                if(i>=0){
                    let it = item.children[i]
                    
                    it.checked = !it.checked
                }else{
                    item.checked = !item.checked
                    
                    if(item.children&&item.children.length>0){
                        item.children.filter(it=>{
                            it.checked = item.checked
                        })
                    }
                }
                
            }else{
                items.checked = !items.checked
                
                if(items.children&&items.children.length>0){
                    items.children.filter(item=>{
                        item.checked = items.checked
                        
                        if(item.children&&item.children.length>0){
                            item.children.filter(it=>{
                                it.checked = items.checked
                            })
                        }
                    })
                }
            }
            
            // 筛选下级分类全选或没有全选
            if(items.children&&items.children.length>0){
                let item_flag = true
                let it_flag = true
                
                items.children.filter(item=>{
                    
                    if(item.children&&item.children.length>0){
                        item.children.filter(it=>{
                            if(!it.checked){
                                it_flag = false
                            }
                        })
                        item.checked = it_flag
                    }
                    
                    if(!item.checked){
                        item_flag = false
                    }
                    
                })
                
                if(item_flag&&it_flag){
                    items.checked = true
                }else{
                    items.checked = false
                }
            }
            
            this.data.splice(indexs,1,items)
            
        },
        /* 
            打开查看三级分类
        */
        clickBottom(indexs,index){
            let items = this.data[indexs]
            
            items.children[index].open = !items.children[index].open
            this.data.splice(indexs,1,items)
        },
        /* 
            请求方法
        */
       axios(){
           let data = {
            
            api:"plugin.coupon.AppMchcoupon.Fenlei",
               mch_id: uni.getStorageSync('shop_id'), // 店铺id
           }
           
           this.$req.post({data}).then(res=>{
               this.classList = res.data
               
               // 请求完成后筛选二三级分类
               this.getFenlei(this.classList)
           })
           
       },
        /*
            获取分类
         */
        getFenlei(list) {
            let checkId = [];
            if(uni.getStorageSync('chooseClass')){
                checkId = uni.getStorageSync('chooseClass').split(',')
            }
            
        	var classList = []
        
        	list.filter(item => {
        		if (!classList[item.level]) {
        			classList[item.level] = []
        		}
        		item.label = item.name
                
                item.checked = false
                item.open = false
                
                checkId.filter(it=>{
                    if(it == item.id){
                        item.checked = true
                    }
                })
        
        		classList[item.level].push(item)
        	})
        	this.classList = classList
        
        	this.filterName(0)
        
        	this.filterFenlei(4)
        },
        /* 
            筛选分类  i代表筛选的下标为i的等级分类
        */
        filterFenlei(i) {
        	if (i == 0) {
        		this.data = this.classList[0]
        		return
        	}
        
        	if ((!this.classList[i]) || this.classList[i].length == 0) {
        		this.filterFenlei(i - 1)
        		return
        	}
        
        	// 把当前等级的分类一一插入对应的上一级分类中
        	this.classList[i - 1].filter(items => {
        		this.classList[i].filter(item => {
        			if (item.pId == items.id) {
        				if (!items.children) {
        					items.children = []
        				}
        
        				items.children.push(item)
        			}
        		})
        	})
        
        	this.filterFenlei(i - 1)
        
        },
        // 为子类名添加上上级分类的标识
        filterName(i) {
        	// 循环到5级分类时停止
        	if (i == 4) {
        		return
        	}
        	// 当前分类的下一级为空时停止
        	if ((!this.classList[i + 1]) || this.classList[i + 1].length == 0) {
        		return
        	}
        
        	this.classList[i].filter(items => {
        		if (i == 0) {
        			items.sname = items.name
        		}
        
        		this.classList[i + 1].filter(item => {
        			if (item.pId == items.id) {
        				var name = items.sname
        
        				name += '/' + item.name
        
        				item.sname = name
        			}
        		})
        	})
        
        	this.filterName(i + 1)
        },
        
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
@import url('../../static/css/myStore/chooseClass.less');
</style>
