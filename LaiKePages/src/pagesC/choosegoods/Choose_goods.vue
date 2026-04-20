<template>
    <view>
        <heads :title="language.chooseGoods.title" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
        <view class="pages">
            <view class="search">
                <view>
                    <view class='search_input'>
                        <image class="searchImg" :src="serchimg" alt="">
                            <input v-model="name" type="text" :placeholder="language.choosePro.searchName" />
                            <image v-show="name.length > 0" @tap="cleardata" class="cancel" :src="sc_icon" mode=""></image>
                        </image>
                    </view>
                    <view class='search_btn' @tap='_search'>{{language.choosePro.searchBtn}}</view>
                </view>
            </view>
            <view class="page_list">
                <view class="pages_box" v-for="(item,index) in list" :key="index">
                    <view class="page_box_select">
                        <image v-if="updata" style="width: 32rpx;height: 32rpx;" @tap="choose_id(index)"
                            :src="item.is_check==true?mycoupon_yxz:mycoupon_wxz"></image>
                    </view>
                    <view class="pages_box_img">
                        <image :src="item.imgUrl" style="width: 152rpx;height: 152rpx;border-radius: 16rpx;"></image>
                    </view>
                    <view class="pages_box_detal">
                        <view class="pages_box_detal_txt">{{item.goodsName}}</view>
                        <view class="pages_box_detal_price_kca">{{item.attrName}}</view>
                        <view class="pages_box_detal_price">
                            <view class="pages_box_detal_price_jg" @tap="cuowuaclick">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{item.price}}</view>
                            <view class="pages_box_detal_price_kc">{{language.chooseGoods.kc}}:{{item.stockNum}}件</view>
                        </view>
                    </view>
                </view>
            </view>
            <view @touchmove.stop.prevent class="bounced" v-if="mask">
                <view class="bounced-box">
                    <view class="bounced-box-box">
                        <view class="bounced-box-box-title">{{language.chooseGoods.yxsp}}</view>
                        <image :src="xxxx" class="bounced-box-box-img" @tap="maskclick"></image>
                    </view>
                    <view class="bounced-box-list">
                        <scroll-view class="scroll-box" scroll-y="true" @touchmove.stop.prevent >
                            <view class="pages_box_box" v-if="is_masklist" v-for="(item,index) in masklist" :key="index">
                                <view class="pages_box_img" >
                                    <image  :src="item.imgUrl" style="width: 152rpx;height: 152rpx;border-radius: 16rpx;"></image>
                                </view>
                                <view class="pages_box_detal">
                                    <view class="pages_box_detal_txt">{{item.goodsName}}</view>
                                    <view class="pages_box_detal_price_kca">{{item.attrName}}</view>
                                    <view class="pages_box_detal_price">
                                        <view class="pages_box_detal_price_left">
                                            <view class="pages_box_detal_price_jg">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{item.price}}</view>
                                            <view class="pages_box_detal_price_kc">{{language.chooseGoods.kc}}:{{item.stockNum}}件</view>
                                        </view>
                                        <view>
                                            <image @tap="deleteclick(index)" :src="Activities_set" style="width: 32rpx;height: 32rpx;"></image>
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </scroll-view>
                    </view>
                </view>
            </view>
            <view class="bottomBox">
                <view class="qxBox" @tap="clickAll">
                    <image class="circleImg"  :src="is_all?mycoupon_yxz:mycoupon_wxz" mode=""></image>
                    <text class="text">{{language.choosePro.checkAll}}</text>
                </view>
                <view class="textBox" @tap="maskclick">
                    {{language.choosePro.selected}}<view>{{masklist.length}}</view>{{language.chooseGoods.jsp}}
                </view>
                <view class="btn" @tap="addOk">{{language.chooseGoods.hdsz}}</view>
            </view>
            <!-- 提示 -->
            <view class="xieyi" style="background-color: initial;" v-if="is_cuowua">
                <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                    <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                        <image style="width: 68rpx;height: 68rpx;" :src="cuowua"></image>
                    </view>
                    <view class="xieyi_title" style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">{{language.chooseGoods.qxzsp}}</view>
                </view>
            </view>
            <!-- 提示 -->
            <view class="xieyi" style="background-color: initial;z-index: 9999;" v-if="is_sus">
                <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                    <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                        <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                    </view>
                    <view class="xieyi_title" style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">{{language.chooseGoods.sccg}}</view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
    export default {
        data() {
            return {
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                mask:false,
                masklist:[],
                list:[],
                loadingType: 0,
                pageNo:1,
                checkNum:'',
                updata: true,
                is_masklist:true,
                is_sus:false,
                is_all:false,
                xxxx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xxxx.png',
                is_cuowua:false,
                Activities_set: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/Activities_set.png',
                name: '',
                guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                cuowua: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/cuowua.png',
                serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
                sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
                mycoupon_wxz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_wxz.png',
                mycoupon_yxz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_yxz.png',
            }
        },
        onShow() {
            this._axios(1)
           this.masklist = uni.getStorageSync('masklist')
        },
        watch:{
            list:{
                 handler(newVal,logVal){
                 },
                 depp:true   
            },
            masklist:{
                 handler(newVal,logVal){
                 },
                 depp:true   
            }
        },
        onReachBottom() {
            if (this.loadingType == 1 || this.loadingType == 2) {
                return false
            }
            this.loadingType = 1;
            this.pageNo++;
            this._axios(1)
            
        },
        methods: {
            cleardata(){
                this.name= ''
            },
            choose_id(e){
                this.list[e].is_check = !this.list[e].is_check
                this.is_is_all = false
                this.is_is_all = true
                this.masklist = []
                var newlist = []
                var truelist = []//用于接受 列表已经勾选
                var falselist = []//以上取反
                this.list.forEach(item => {     //判断是否全选
                   
                    if(item.is_check){
                        this.is_all = true
                        truelist.push(item.is_check)
                        this.masklist.push(item)
                    }else{
                        this.is_all = false
                        falselist.push(item.is_check)
                    }
                })  
                if(this.list.length == truelist.length){ //是全选
                    this.is_all = true
                    this.is_is_all = false
                    this.is_is_all = true
                }else{                                  //不是全选
                    this.is_all = false
                    this.is_is_all = false
                    this.is_is_all = true
                }
                for(var i = 0; i<this.list.length;i++){     //选中更换状态
                    if(this.list[i].is_check){
                        this.list[i].is_check = true
                    }else{
                        this.list[i].is_check = false
                    }
                }
                this.updata = false
                this.updata = true
            },
            _search(){
                this._axios(1)
            },
            _axios (e) {
                var data = {
                    api:'plugin.integral.AppIntegral.getProList',
                    pro_name: this.name,
                    page:this.pageNo
                }
                this.$req.post({data}).then(res=>{
                    if (res.code == 200) {
                        if (this.pageNo == 1) {
                            this.list = res.data.res
                        } else {
                            this.list.push(...res.data.res)
                        }
                        if (res.data.res.length > 0) {
                            this.loadingType = 0
                        } else {
                            this.loadingType = 2
                        }
                        this.is_all = false 
                        if(e==1){
                            for(var i = 0;i<this.masklist.length;i++){
                                if(this.masklist[i].is_check == true){
                                    for(var a = 0;a<this.list.length;a++){
                                        if(this.list[a].attrId == this.masklist[i].attrId){
                                            this.list[a].is_check = true
                                        }
                                    }
                                } 
                            }
                        }else{
                            for (var a in this.list) {
                                this.list[a].is_check = false
                            }
                        }
                        
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
                
            },
            deleteclick(index){
                this.masklist[index].is_check = false
                this.is_masklist = false
                this.masklist.splice(index, 1);
                setTimeout(()=>{
                    this.is_masklist = true
                },100)
                this.is_sus = true
                setTimeout(()=>{
                    this.is_sus = false
                },1000)
            },
            cuowuaclick(){
                this.is_cuowua = true
                setTimeout(()=>{
                    this.is_cuowua = false
                },3000)
            },
            clickAll(){
                for (var i = 0; i < this.list.length; i++) {
                    if (this.is_all == true) {
                        this.list[i].is_check = false
                    } else {
                        this.list[i].is_check = true
                    }
                } 
                this.is_all = !this.is_all
                if(this.is_all == true){
                    this.masklist = JSON.parse(JSON.stringify(this.list))
                }else{
                    this.masklist.splice(0,this.masklist.length)
                }
                this.is_is_all = false
                this.is_is_all = true
                this.updata = false
                this.updata = true
            },
            maskclick() {
                this.mask = !this.mask
                
            },
            addOk(){
                if(this.masklist.length==0){
                    uni.showToast({
                        icon:'none',
                        title:'请添加商品'
                    })
                    return
                }
                    for(var q in this.masklist){
                        this.masklist[q].num = this.masklist[q].stockNum
                    }
                uni.setStorageSync('masklist',this.masklist)
                uni.navigateTo({
                    url:'Activities_set',
                })
            }
        }
    }
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped> 
    @import url("@/laike.less");
    .pages_box_box {
        background-color: #fff;
        width: auto;
        height: 152rpx;
        padding: 24rpx 32rpx;
        border-bottom: 1rpx solid #eee;
        align-items: center;
        display: flex;
    }
    
    .pages_box_detal_price_left{
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .bounced {
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        position: fixed;
        left: 0;
        top: 0;
        z-index: 1000;
    }
    .scroll-box {
        width: 100%;
        height: 1060rpx;
    }
    .goodslista {
        width: 100%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        margin-top: 32rpx;
    }
    .bounced-box {
        bottom: 0;
        width: 100%;
        height: 75%;
        background: #fff;
        position: fixed;
        z-index: 999;
        border-radius: 24rpx;
    }
    
    .bounced-box-box {
        width: 100%;
        height: 108rpx;
        display: flex;
        align-items: center;
    }
    
    .bounced-box-box-title {
        margin-left: 312rpx;
    }
    
    .bounced-box-box-img {
        width: 48rpx;
        height: 48rpx;
        margin-left: 230rpx;
    }
    .goodlistBox {
        background-color: #fff;
        border-radius: 25rpx;
        width: 100%;
        height: 260rpx;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-bottom: 24rpx;
    }
    .bottomBox{
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        z-index: 9999;
        height:104rpx;
        background:rgba(255,255,255,1);
        display: flex;
        align-items: center;
        box-sizing: border-box;
        padding-left: 30rpx;
        border-top: 1px solid #E5E5E5;
        box-sizing: unset;
        box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
        .circleImg{
            margin-right: 32rpx;
            width: 34rpx;
            height: 34rpx;
        }
        .qxBox{
            display: flex;
            align-items: center;
        }
        
        .text{
            font-size: 32rpx;
            
            font-weight: 400;
            color: #666666;
        }
        
        .textBox{
            display: flex;
            margin-left: 32rpx;
            font-size: 32rpx;
            
            font-weight: 400;
            color: #666666;
            
            view{
                color: @priceColor;
                font-weight: bold;
                margin: 0 4rpx;
            }
        }
        
        
        .btn{
            display: flex;
            align-items: center;
            justify-content: center;
            width:192rpx !important;
            background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
            height:72rpx;
            font-size: 28rpx;
            border-radius: 36rpx;
            
            font-weight: 500;
            color: #FFFFFF;
            margin-left: auto;
            margin-right: 32rpx;
        }
    }
    
    .pages_box_detal_price_kc {
        font-size: 24rpx;
         
        font-weight: normal;
        color: #999999;
        margin-left: 32rpx;
    }
    .pages_box_detal_price_kca {
        font-size: 24rpx;
         
        font-weight: normal;
        color: #999999;
        margin-top: 8rpx;
    }
    .pages_box_detal_price_jg{
        font-size: 32rpx;
        
        font-weight: normal;
        color: #FA5151;

    }
    .pages_box_detal_price{
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 16rpx;
    }
    .pages_box_detal_txt{
        text-overflow: -o-ellipsis-lastline;
          overflow: hidden;        //溢出内容隐藏
          text-overflow: ellipsis;    //文本溢出部分用省略号表示
          display: -webkit-box;      //特别显示模式
          -webkit-line-clamp: 2;      //行数
          line-clamp: 2;          
          -webkit-box-orient: vertical;  //盒子中内容竖直排列
          font-size: 32rpx;
           
          font-weight: normal;
          color: #333333;
    }
    .pages_box_detal{
        flex: 1;
        margin-left: 24rpx;
        display: flex;
        flex-direction: column;
    }
    .pages_box_img{
        display: flex;
        justify-content: center;
        align-items: center;
        margin-left: 32rpx;
    }
    .page_list{
        margin-top: 20rpx;
        display: flex;
        flex-direction: column;
        padding-bottom: 100rpx;
    }
    .pages_box {
        background-color: #fff;
        width: auto;
        height: 152rpx;
        border-radius: 24rpx;
        align-items: center;
        display: flex;
        padding: 32rpx;
        margin-bottom: 16rpx;
    }



    .pages {
        width: 100%;
    }

    .search {
        height: 108rpx;
        background-color: #fff;
        border-radius: 0 0 24rpx 24rpx;
        display: flex;
        justify-content: center;
        align-items: center;

        >view {
            display: flex;
            align-items: center;
            width: 100vw;
            padding: 4rpx 8rpx 18rpx;
            overflow: hidden;
            z-index: 99;

            .search_input {
                position: relative;
                display: flex;
                align-items: center;
                padding: 0 57rpx;
                width: 542rpx;
                margin-left: 24rpx;
                height: 68rpx;
                background: rgba(243, 243, 243, 1);
                border-radius: 35rpx;
                box-sizing: border-box;

                input {
                    font-size: 24rpx;
                    margin-left: 8rpx;
                }

                .cancel {
                    position: absolute;
                    top: 50%;
                    transform: translateY(-50%);
                    width: 30rpx;
                    height: 30rpx;
                    right: 20rpx;
                }
            }

            .searchImg {
                width: 30rpx;
                height: 30rpx;
                left: 20rpx;
                top: 50%;
                transform: translateY(-50%);
            }

            .search_btn {
                width: 120rpx;
                height: 64rpx;
                text-align: center;
                line-height: 64rpx;
                border-radius: 32px;
                font-size: 28rpx;
                
                font-weight: 500;
                border: 1rpx solid #eee;
                color: #333333;
                white-space: nowrap;
                margin-left: 24rpx;
            }
        }
    }
    .xieyi{
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, .5);
        z-index: 99;
        display: flex;
        justify-content: center;
        align-items: center;
        >view{
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #FFFFFF;
            border-radius: 24rpx;
            .xieyi_btm{
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0,0,0,.1);
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_title{
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 32rpx;
                
                font-weight: 500;
                color: #333333;
                line-height: 44rpx;
                margin-top: 64rpx;
                margin-bottom: 32rpx;
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_text{
                width: 100%;
                max-height: 236rpx;
                overflow-y: scroll;
                padding: 0 32rpx;
                box-sizing: border-box;
            }
        }
    }

</style>
