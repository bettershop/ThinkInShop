<template>
    <div class='box'>
        <!-- #ifndef MP-ALIPAY -->
        <heads :title="language.collection.title" :bgColor="bgColor" :titleColor="'#333333'" :ishead_w="2"></heads>
        <!-- #endif -->
        

        <view :class="!navActive?'bj_box':''">
            <view class="twins">
                <view class="nav_twin" :class='{"nav_twin_hover": navActive}' @tap="changeNav(true,1)">
                    {{language.collection.commodity_collection}}
                    <view class="nav_line" v-if="navActive"></view>
                </view>
                <view  class="nav_twin" :class='{"nav_twin_hover": !navActive}' @tap="changeNav(false,2)">
                    {{language.collection.shop_collection}}
                    <view class="nav_line" v-if="!navActive"></view>
                </view>
            </view>
        </view>
        <view class="bj_box" v-if="navActive==true">
            <view style="background-color: #FFFFFF;border-radius: 0px 0px 24rpx 24rpx;">
                <view class="sc_box">
                <view class="sc_font_one">{{language.collection.g}}<span class="sc_font_two">{{collection1.length}}</span>{{language.collection.jsp}}</view>
                <view>
                    <div v-if="!navActive && collection1.length">
                        <p v-if='collection.length&&navType == 1' class='sc_font_three' @tap='_state' :class="{'active':!collection1.length}">{{state}}</p>
                        <p v-if='!loadFlag || (collection.length&&navType == 2)' class='sc_font_three' @tap='_state' :class="{'active':!collection.length}">{{state}}</p>
                    </div>
                    <div v-else>
                        <p v-if='collection1.length&&navType == 1' class='sc_font_three' @tap='_state' :class="{'active':!collection1.length}">{{state}}</p>
                        <p v-if='collection1.length&&navType == 2' class='sc_font_three' @tap='_state' :class="{'active':!collection.length}">{{state}}</p>
                    </div>
                </view>
            </view>
            </view>
        </view>

        <template v-if="loadFlag">
            <!--收藏商品展示模块-->
            <view class="" v-if="navActive==true" style="flex: 1;background-color: #F4F5F6;overflow: scroll;">
                <ul v-if='collection1.length'>
                    <li v-for='(item,index) in collection1' :key="item.p_id" :class="!statevalue?'list_li':'list_li_two'">
                        
                        <div class="list_imgBox" @tap.stop="_checkedOne(item, index)" v-if='statevalue'>
                            <img class='quan_img list_img' :src="display_img1[index] ? quan_hei:quan_hui"/> 
                        </div>
                        <div class="logoBox">
                            <img :src="item.imgurl" @tap="_goods(item.p_id,item.recycle,item.isPreGood)" @error="handleImgError(index)"/>
                            
                            <div v-if="item.recycle == 1" class="dowmPro" @tap="_goods(item.p_id,item.recycle,item.isPreGood)">{{ language.collection.ygq }}</div>
                            <div v-if="item.status == 3 && item.recycle != 1" class="dowmPro" >
                                {{language.store.shelf}}
                            </div>
                            <div v-if="item.num == 0 && item.recycle != 1 && item.status != 3" class="dowmPro">
                                <!-- 已售罄 -->
                                {{language.shoppingCart.soldOut}}
                            </div>
                        </div>
                        
                        <!-- 店铺 -->
                        <view class="dp_css">
                            {{item.mch_name}}
                         </view>
                        
                        <view :class="statevalue ? 'sc_right_X':'sc_right_Y' " @tap="_goods(item.p_id,item.recycle,item.isPreGood)">
                            <div class='describe' :class='{left:statevalue}'>
                                 <span class="preSale_tag" v-if="item.isPreGood" >{{language.goods.goods.presale}}</span>
                                 <span>{{item.product_title}}</span>
                            </div>

                          
                            <div class='collection_button' v-if="!is_grade">
                                <span class="span_b1"><span style="font-size: 24rpx;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>{{LaiKeTuiCommon.formatPrice(item.vip_yprice)}}</span>
                            </div>
                            <div class='collection_button' v-else>
                                <span class="span_b1">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.vip_price)}}</span>
                                <span class="span_b2">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.vip_yprice)}}</span>
                            </div>
                        </view>
                    </li>
                </ul>
                <div class='noFindDiv' v-else>
                    <div>
                        <img :src='collectioncollection_bj' class="find_img" />
                    </div>
                    <p class='find_text'>{{language.collection.commodity_Tips}}</p>
                </div>
            </view>

            <!--收藏店铺展示模块-->
            <view class="" v-else style="flex: 1 1 0%;background-color: #F4F5F6;">
                <ul v-if='collection.length' style="display: flex;flex-direction: column;justify-content: center;padding:24rpx 0;width: 100%;">
                    <li class="list_li_three" style="width: 100%;" v-for='(item,index) in collection' :key="index" @tap="navTo('/pagesB/store/store?shop_id=' + item.mch_id)">
                        <div class="list_imgBox" @tap.stop="_checkedOne(item, index)" v-if='statevalue'>
                            <img class='quan_img list_img1' :src="display_img[index] ? quan_hei:quan_hui"/>
                        </div>
                        <img class="dp_img" :src="item.img"></img>
                        <view class="dp_con">
                            <view class="dp_con1">
                                <view class="dp_con1_box">{{item.mch_name}}
                                <view class="new" v-if="item.isUploadNewDate">{{language.collection.bq}}</view>
                                </view>
                                <view v-if="item.is_attention==false" class="attention_btnl" @click.stop="attentionclick(item.mch_id,index)">+ 关注</view>
                                <view v-if="item.is_attention==true" class="attention_btn" @click.stop="_deletell(item.mch_id,index)">{{language.collection.unfollow}}</view>
                            </view>
                            <view class="dp_con2">
                                <view class="dp_con2_box">
                                    <view class="dp_con2_box_num" v-if="item.quantity_on_sale">{{item.quantity_on_sale}}</view>
                                    <view class="dp_con2_box_num" v-else>—</view>
                                    <view class="dp_con2_box_title">{{language.collection.zssp}}</view>
                                </view>
                                <view class="dp_con2_box">
                                    <view class="dp_con2_box_num" v-if="item.quantity_sold">{{item.quantity_sold}}</view>
                                    <view class="dp_con2_box_num" v-else>—</view>
                                    <view class="dp_con2_box_title">{{language.collection.ys}}</view>
                                </view>
                                <view class="dp_con2_box">
                                    <view class="dp_con2_box_num">{{item.collection_num}}</view>
                                    <view class="dp_con2_box_title">{{language.collection.rs}}</view>
                                </view>
                            </view>
                        </view>
                    </li>
                </ul>
                <div class='noFindDiv' v-else>
                    <div>
                        <img :src='pagescollectioncollection_bj' class='find_img'/>
                    </div>
                    <p class='noFindText'>{{language.collection.shop_Tips}}</p>
                </div>
            </view>

            <view class="mask_addInvoice" v-if="isMaskRepeat">
                <view class="mask_invoice_content ">
                    <view class="invoice_content_top">
                        <p class="remarksContent">{{ language.collection.gspygq }}</p>
                    </view>
                    <view class="invoice_content_bottom">
                        <p @tap="isMaskRepeat = false">{{ language.collection.wzdl }}</p>
                    </view>
                </view>
            </view>
            <!--点击管理后显示的全选和删除模块-->
            <div class='del' v-if='statevalue&&navType==1'>
                <div class='del_sele' @tap="_selectAll">
                    <img class='quan_img' :src="selectAll1 ? quan_hei : quan_hui"/>
                    <span>{{language.collection.Select_all}}</span>
                </div>
                <div class='del_div' @tap="_deletell">{{language.collection.Cancel_collection}}</div>
            </div>
            <div class='del' v-else-if='statevalue'>
                <div class='del_sele' @tap="_selectAll">
                    <img class='quan_img' :src="selectAll ? quan_hei : quan_hui"/>
                    <span>{{language.collection.Select_all}}</span>
                </div>
                <div class='del_div' @tap="_delete">{{language.collection.Cancel_collection}}</div>
            </div>
            <show-toast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" @richText="_hide1" @confirm="_confirm"></show-toast>
        </template>
        <!-- 取消成功 -->
        <view class="xieyi" style="background-color: initial;" v-if="is_susa">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">取消成功</view>
            </view>
        </view>
    </div>
</template>

<script>
    import {
        mapMutations
    } from 'vuex'
    import showToast from '@/components/aComponents/showToast.vue'
    export default {
        data () {
            return {
                is_susa:false,
                bgColor:[{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                collectioncollection_bj:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/collectioncollection_bj.png',
                pagescollectioncollection_bj: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pagescollectioncollection_bj.png', 
                attention:'取消关注', 
                collection: [], //收藏商品数据
                collection1: [], //收藏商品数据
                state: '编辑', //管理与完成的切换
                statevalue: false, //管理与完成的状态值
                selectvalue: '', //单选的状态值
                selectvalue1: '', //单选的状态值
                selectAll: '', //全选的状态值
                selectAll1: '', //全选的状态值
                selectArray: [], //单选数据存储
                selectArray1: [], //单选数据存储
                deletecolle: {}, //删除商品
                grade: 0,
                quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png',
                quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                display_img: [], //圆圈的选中状态
                display_img1: [], //圆圈的选中状态
                flag: true,
                fastTap: true,
                navActive: true,
                navType: 1,
                is_grade:false,
                is_attention:false,//是否关注
                loadFlag: false,
                mch_status: false,
                is_showToast:0,
                is_showToast_obj:{},
                attentionclickid:'',
                attentionclickindex:'',
                isMaskRepeat:false, 
            }
        },
        onLoad () {
           
            this.navType = 1

        },
        onShow () {
            this.state = this.language.collection.edit
            this._axios()
        },
       
        computed: {
           
        },
        methods: {
            // 图片报错
            handleImgError(index){
                const defaultPicture = this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png'
                this.collection1[index].imgurl =defaultPicture
            },
            //取消
            _hide1(e){
                this.is_showToast = 0
            },
            _deletell(id,index){
                if(this.navType==1){
                    if (this.selectArray1.length || this.selectArray.length) {
                        this.is_showToast = 4
                        this.is_showToast_obj.title = ''
                        if(this.navType==2){
                            this.is_showToast_obj.content = this.language.collection.qrqxgz
                            this.attentionclickid = id
                            this.attentionclickindex = index
                        }else{
                            this.is_showToast_obj.content = this.language.collection.qrqxsc
                        }
                        this.is_showToast_obj.button = this.language.collection.zxx
                        this.is_showToast_obj.endButton = this.language.collection.qr
                    }else{
                        uni.showToast({
                            icon:'none',
                            title:this.language.collection.nhwxz
                        })
                    }
                }else{
                    this.is_showToast = 4
                    this.is_showToast_obj.title = ''
                    if(this.navType==2){
                        this.is_showToast_obj.content = this.language.collection.qrqxgz
                        this.attentionclickid = id
                        this.attentionclickindex = index
                    }else{
                        this.is_showToast_obj.content = this.language.collection.qrqxsc
                    }
                    this.is_showToast_obj.button = this.language.collection.zxx
                    this.is_showToast_obj.endButton = this.language.collection.qr 
                }
                

            },
            //确认
            _confirm(e){
                if(this.navType==2){
                    this.attentionclick(this.attentionclickid,this.attentionclickindex)
                }else{
                    this._delete()
                }
                this.is_showToast = 0
            },

            attentionclick(e,index){
                    var data = {
                        
                        api:"mch.App.Mch.Collection_shop",
                        shop_id:e
                    };
                    this.$req
                        .post({
                            data
                        })
                        .then(res => {
                            
                            this.is_susa = true
                            setTimeout(()=>{
                                this.is_susa=false
                            }, 1500)
                            if (res.code == 200) {
                                var  i = ''
                                i =  !this.collection[index].is_attention
                                this.collection[index].is_attention = i
                                this.$forceUpdate(this.collection)
                                this._axios()
                                setTimeout(() => {
                                    this.fastTap = true;
                                }, 1500);
                            }
                        })
                        .catch(error => {
                        }); 
            }, 
            _state () {
			    if (!this.loadFlag) return false;
                if (this.collection1.length || this.collection.length) {
                    this.selectAll = ''
                    this.selectAll1 = ''
                    this.statevalue = !this.statevalue
                    if (this.statevalue) {
                        this.state = this.language.collection.complete
                    } else {
                        this.state = this.language.collection.edit
                    }
                } else {
                    this.statevalue = !this.statevalue
                    if (this.statevalue) {
                        this.state = this.language.collection.complete
                    } else {
                        this.state = this.language.collection.edit
                    }
                }
            },
            _axios () {
                var data = {
                    api: 'app.addFavorites.collection',
                    
                    type: this.navType
                }
                
                this.$req.post({data}).then(res=>{
                    this.loadFlag = true
                    
                    if (res.code == 200) {
                        if (this.navType == 1) {
                            this.collection1 = res.data.list
  
                            this.mch_status = res.data.mch_status
                            
                            
                            this.is_grade = res.data.isGrade
                            
                        } else {
                            try {
                                if (Array.isArray(res.data)) {
                                    this.collection = res.data;    

                                } else {
                                    this.collection = res.data.list    
                                }
                                for(var i in this.collection){
                                    this.collection[i].is_attention = true
                                }
                            } catch (e) {
                                this.collection = [];
                            }
                        }
                    } else {
                        this.collection = ''
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
                
            },

            // 点击切换导航条
            changeNav (flag, type) {
                this.statevalue = false
                this.state = this.language.collection.edit
                this.navActive = flag
                this.navType = type
                this.loadFlag = false
                this._axios()
            },

            //单选
            _checkedOne (item, indexli) {
                if (this.navType == 1) {
                    var cum = this.selectArray1.findIndex(items=>item.id==items.id)
                    //判断点击的传入的值是否存在数组中，如果没有添加，如果有删除，同时设定选中状态（第一次点击添加进数组，第二次点击从数组中删除）
                    if (this.statevalue) {
                        var img = this.$refs.img
                        if (cum < 0) {
                            this.selectArray1.push(item)
                            this.$set(this.display_img1, indexli, true)
                        } else {
                            this.selectArray1.splice(cum, 1)
                            this.$set(this.display_img1, indexli, false)
                        }
                        //根据产品选状态，设定全选状态
                        if (this.selectArray1.length == this.collection1.length) {
                            this.selectAll1 = true
                        } else {
                            this.selectAll1 = false
                        }
                    }
                } else {
                    var cum = this.selectArray.findIndex(items=>item.id==items.id)
                    //判断点击的传入的值是否存在数组中，如果没有添加，如果有删除，同时设定选中状态（第一次点击添加进数组，第二次点击从数组中删除）
                    if (this.statevalue) {
                        var img = this.$refs.img
                        if (cum < 0) {
                            this.selectArray.push(item)
                            this.$set(this.display_img, indexli, true)
                        } else {
                            this.selectArray.splice(cum, 1)
                            this.$set(this.display_img, indexli, false)
                        }
                        //根据产品选状态，设定全选状态
                        if (this.selectArray.length == this.collection.length) {
                            this.selectAll = true
                        } else {
                            this.selectAll = false
                        }
                    }
                }
            },
            _selectHandle (obj) {
                obj.selectAll1 = !obj.selectAll1
                var img = this.$refs.img
                //根据全选状态，设定商品选中状态
                if (obj.selectAll1) {
                    for (var i = 0; i < obj.collection1.length; i++) {
                        this.$set(obj.selectArray1, i, obj.collection1[i])
                        this.$set(obj.display_img1, i, true)
                    }
                } else {
                    obj.selectArray1 = []
                    this.selectArray1 = []
                    for (var i = 0; i < obj.collection1.length; i++) {
                        this.$set(obj.display_img1, i, false)
                    }
                }
                return obj
            },
            //全选
            _selectAll () {
                if (this.navType == 1) {
                    var obj = {
                        selectAll1: this.selectAll1,
                        display_img1: this.display_img1,
                        selectArray1: this.selectArray1,
                        collection1: this.collection1
                    }
                    obj = this._selectHandle(obj)
                    this.selectAll1 = obj.selectAll1
                } else {
                    var obj = {
                        selectAll1: this.selectAll,
                        display_img1: this.display_img,
                        selectArray1: this.selectArray,
                        collection1: this.collection
                    }
                    obj = this._selectHandle(obj)
                    this.selectAll = obj.selectAll1
                }

            },
            //删除
            _delete () {
                if (!this.fastTap) {
                    return
                }
                
                this.fastTap = false
                
                if (this.selectArray1.length || this.selectArray.length) {
                    this.deletecolle.collection = ''
                    this.deletecolle.access_id = this.access_id
                    if (this.navType == 1) {
                        this.selectArray1.forEach((item, index)=>{
                            this.deletecolle.collection += item.id + ','
                        })
                    } else {
                        this.selectArray.forEach((item, index)=>{
                            this.deletecolle.collection += item.id + ','
                        })
                    }
                    
                    this.deletecolle.module = 'app'
                    this.deletecolle.action = 'addFavorites'
                    this.deletecolle.app = 'removeFavorites'

                    var data = this.deletecolle
                    
                    this.$req.post({data}).then(res=>{
                        this.fastTap = true
                        var s1 = []
                        var s = []
                        if (res.code == 200) {
                        
                            var itemIds = this.deletecolle.collection
                            for (var x in itemIds) {
                        
                                if (this.navType == 1) {
                                    this.selectArray1.forEach(function (item, index) {
                                        if (itemIds[x] != item.id) {
                                            s1.push(item)
                                        }
                                    })
                                    this.selectArray1 = s1
                                } else {
                                    this.selectArray.forEach(function (item, index) {
                                        if (itemIds[x] != item.id) {
                                            s.push(item)
                                        }
                                    })
                                    this.selectArray = s
                                }
                            }
                            this.display_img1 = []
                            for (var i = 0; i < this.collection1.length; i++) {
                                this.display_img1.push(false)
                            }
                            this.display_img = []
                            for (var i = 0; i < this.collection.length; i++) {
                                this.display_img.push(false)
                            }
                            var tips = '';
                            if(this.language.collection.edit=="编辑"){
                                 tips = res.message
                            }else{
                                 tips= this.language.collection.Cancelled_successfully
                            }
                            
                                    this.is_susa=true
                                    setTimeout(()=>{
                                        this.loadFlag = false
                                        this.collection = []
                                        this.collection1 = []
                                        this._axios()
                                        this.selectArray1 = []
                                        this.statevalue = !this.statevalue
                                        this.state = this.language.collection.edit
                                        this.is_susa=false

                                    }, 1500)
                            
                        
                        } else {
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            })
                        }
                        for (var i = 0; i < this.display_img.length; i++) {
                            this.$set(this.display_img1, i, false)
                        }
                    }).catch(error=>{
                        this.fastTap = true
                    })
                    
                } else {
                    this.fastTap = true
                    uni.showToast({
                        title: this.language.collection.commodity_delete,
                        duration: 1000,
                        icon: 'none'
                    })
                }
            },
            _goods (id, recycle,isPreGood) {
                if(recycle == 1){
                    this.isMaskRepeat = true
                    return
                }
                this.pro_id(id)
                if(isPreGood){
                    uni.navigateTo({
                        url: '/pagesC/preSale/goods/goodsDetailed?toback=true&pro_id=' + id
                    })
                }else{
                    uni.navigateTo({
                        url: '/pagesC/goods/goodsDetailed'
                    })
                }
               
            },
            ...mapMutations({
                pro_id: 'SET_PRO_ID'
            }),
        },
        components: {
                showToast
        }

    }
</script>

<style lang="less" scoped> 
    @import url("@/laike.less");
    @import url("../../static/css/collection/collection.less");
</style>
