<template>
    <view>
        <view class="pages">
            <heads :title="language.myPro.yssp" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
            <!-- 模式 -->
            <switchNavOne :is_switchNav="header" :is_switchNav_radius="'0 0 0 0'" @choose="_choose"></switchNavOne>
            <!-- 标签 -->
            <switchNavTwo :is_switchLable="header_label" @choose="_chooseLable"></switchNavTwo>
            <!-- 无数据显示 -->
            <div v-if="isData.length == 0" style="height: 100vh;width: 100%;display: flex;align-items: center;">
                <div class="noFindDiv" style="width: 100%;padding-top: 178rpx;height: 100%;">
                    <div><img class="noFindImg" :src="noOrder" /></div>
                    <span class="noFindText">{{language.yushou_details.others}}～</span>
                </div>
            </div>
            <!-- 有数据显示 -->
            <view v-else class="pages_box">
                <view class="pages_box_box" v-for="(item,index) in isData" :key="item.id">
                    <view class="pages_box_box_body" @click="_navigateto('/pagesA/myStore/uploadPro?type=yushou&pageStatus=see&id='+item.id)">
                        <view class="pages_box_box_left">
                            <image @error="_loadError(index)" :src="item.url" style="width: 164rpx;height: 164rpx;border-radius: 16rpx;"></image>
                        </view>
                        <view class="pages_box_box_body_right">
                            <view class="pages_box_box_body_right_title">{{item.title}}</view>
                            <template v-if="sellType==1">
                                <view class="pages_box_box_body_rigth_gg">{{language.yushou_details.yd}}：{{item.predetermineNum}}｜{{language.yushou_details.xl}}：{{item.volume}}</view>
                            </template>
                            <template v-else>
                                <view class="pages_box_box_body_rigth_gg">{{language.yushou_details.ykc}}：{{item.sellNum}}｜{{language.yushou_details.yd}}：{{item.predetermineNum}}｜{{language.yushou_details.xl}}：{{item.volume}}</view>
                            </template>
                            <view class="pages_box_box_body_right_price">
                                <view class="pages_box_box_body_right_price_jf">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{item.price.toFixed(2)}} <span class="djClass" v-if="sellType==1">{{language.waterfall.type[5]}}{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{item.deposit.toFixed(2)}}</span></view>
                            </view>
                        </view>
                    </view>
                    <view class="pages_box_box_jw">
                        <view class="pages_box_box_jw_btn">
                            <template v-if="status == 2">
                                <!-- 销量 -->
                                <view class="btn" @click="_navigateto('/pagesC/synthesize/yushou_details?type=xiaoliang&id='+item.id)">{{language.yushou_details.btn[1]}}</view>
                                <!-- 预定 -->
                                <view class="btn" @click="_navigateto('/pagesC/synthesize/yushou_details?type=yuding&id='+item.id)">{{language.yushou_details.btn[2]}}</view>
                                <!-- 下架 -->
                                <view class="btn" @click="_toAddPro('delist',item.id)">{{language.yushou_details.btn[5]}}</view>
                            </template>
                            <template v-if="status == 3">
                                <!-- 删除 -->
                                <view class="btn" @click="_toAddPro('del',item.id)">{{language.yushou_details.btn[0]}}</view>
                                <!-- 销量 -->
                                <view class="btn" @click="_navigateto('/pagesC/synthesize/yushou_details?type=xiaoliang&id='+item.id)">{{language.yushou_details.btn[1]}}</view>
                                <!-- 预定 -->
                                <view class="btn" @click="_navigateto('/pagesC/synthesize/yushou_details?type=yuding&id='+item.id)">{{language.yushou_details.btn[2]}}</view>
                                <!-- 编辑 -->
                                <view class="btn" @click="_navigateto('/pagesA/myStore/uploadPro?type=yushou&pageStatus=editor&id='+item.id)">{{language.yushou_details.btn[3]}}</view>
                                <!-- 上架 -->
                                <view class="btn" @click="_toAddPro('add',item.id)">{{language.yushou_details.btn[4]}}</view>
                            </template>
                            <template v-if="status == 1">
                                <!-- 删除 -->
                                <view class="btn" @click="_toAddPro('del',item.id)">{{language.yushou_details.btn[0]}}</view>
                                <!-- 编辑 -->
                                <view class="btn" @click="_navigateto('/pagesA/myStore/uploadPro?type=yushou&pageStatus=editor&id='+item.id)">{{language.yushou_details.btn[3]}}</view>
                                <!-- 上架 -->
                                <view class="btn" @click="_toAddPro('add',item.id)">{{language.yushou_details.btn[4]}}</view>
                            </template>
                            <template v-if="status == 4">
                            </template>
                        </view>
                    </view>
                </view>
            </view>
            <!-- 提示 -->
            <show-toast ref="tishi" :is_showToast="is_showToast1" :is_showToast_obj="is_showToast_obj"></show-toast>
            <!-- 删除/上架/提示框 -->
            <show-toast ref="xuanze" :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" @richText="_hide1" @confirm="_confirm"></show-toast>
            <!-- 占位 -->
            <view class="zhanwei"></view>
            <!-- 底部发布预售商品按钮 -->
            <view class="isFabu safe-area-inset-bottom">
                <view class="isFabu_btn" @tap="_navigateto('/pagesA/myStore/uploadPro?type=yushou')">{{language.yushou_details.fb}}</view>
            </view>
        </view>
    </view>
</template>

<script>
    import switchNavTwo from '@/components/aComponents/switchNavTwo.vue'
    import switchNavOne from '@/components/aComponents/switchNavOne.vue'
    import showToast from '@/components/aComponents/showToast.vue'
    export default {
        data() {
            return {
                active:1,
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                is_showToast: 0, //删除/上架/提示弹窗
                is_showToast1: 0, //成功提示弹窗
                is_showToast_obj: {},
                is_choose: '',
                name: '',
                //header: [],
                //header_label: ['已上架', '已下架', '待上架'],
                page: 1,//页数
                sellType: 1,//模式
                status: 2,//上下架
                goodsIds: '',//上下架商品id
                isData: {}, //初始数据
                serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/noOrder.png',
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
            }
        },
        components:{
            showToast,
            switchNavOne,
            switchNavTwo
        },
        computed: {
            header(){
                let new_data = []
                new_data[0] = this.language.yushou_details.djms
                new_data[1] = this.language.yushou_details.dhms
                return new_data
            },
            header_label(){
                let new_data = []
                new_data[0] = this.language.yushou_details.ysj
                new_data[1] = this.language.yushou_details.yxj
                new_data[2] = this.language.yushou_details.dsj
                return new_data
            },
        },
        onLoad(option) {},
        onShow() {
            this._axios()
        },
        methods: {
            //图片加载失败
            _loadError(index){
                this.isData[index].url = this.ErrorImg
            },
            //跳转
            _navigateto(url){
                uni.navigateTo({
                    url: url
                })
            },
            towuliu(){
                uni.navigateTo({
                    url:'/pagesC/expressage/expressage'
                })
            },
            to_detal(){
                uni.navigateTo({
                    url:'/pagesC/pointsDetails/Points_details'
                })
            },
            //模式选择
            _choose(index){
                this.sellType = index + 1
                this._axios()
            },
            //标签选择
            _chooseLable(index){
                if(index == 0){
                    this.status = 2
                } else if(index == 1){
                    this.status = 3
                } else if(index == 2){
                    this.status = 1
                } else {
                    this.status = 4
                }
                this._axios()
            },
            //删除/上架商品
            _toAddPro(type, id){
                this.is_choose = type
                this.goodsIds = id
                if(type == 'del'){
                    this.is_showToast = 4
                    this.is_showToast_obj.button = this.language.yushou_details.ts[6]
                    this.is_showToast_obj.title = this.language.yushou_details.ts[0]
                    this.is_showToast_obj.content = this.language.yushou_details.ts[1]
                    this.is_showToast_obj.endButton = this.language.yushou_details.ts[7]
                } else if(type == 'add'){
                    this.throttle(this._add,1500,'' ) 
                   
                } else if(type == "delist"){
                    this.throttle(this._delist,1500,'' ) 
                }
            },
            //取消
            _hide1(e){
                this.is_showToast = e
            },
            //确认
            _confirm(e){
                this.is_showToast = e
                if(this.is_choose == 'del'){
                    this.throttle(this._del,500) 
                } else if(this.is_choose == 'add'){
                   this.throttle(this._add,1500,'' ) 
                } else if(this.is_choose == 'delist'){ 
                    this.throttle(this._delist,1500,'' )  
                }
            },
            /**
             * 防抖 异方法优化 减少 重复触发
             * 使用注意项，需要将执行的异步方法 修改为 同步方法
             * @param {Object} func 需要执行的方法 （必传）
             * @param {Object} _this this指向
             * @param {number || String} text 文案
             * @param {number} tiem 文案
             * 
             */
            throttle (func,tiem=1500,text ) {
                try{
                    if(!func || Object.prototype.toString.call(func) !== '[object Function]'){
                         throw new error(`TypeError: ${func} is not function!!`)
                    }
                    
                    const falg = this.$store.state.falg
                    return async (...args)=> {
                        const context = this; 
                        if(!falg){ 
                            uni.showLoading({
                                title: `${text|| this.language.util.qsh}`,
                                 mask:true //是否显示透明蒙层，防止触摸穿透
                            }) 
                            
                            setTimeout(()=>{
                               uni.hideLoading()
                            },Number(tiem))
                            return
                        } 
                         uni.showLoading({
                                title:this.language.util.qsh,
                                mask:true
                        }) 
                        this.$store.commit('upfalg',false); 
                        try{
                            await func.apply(context, args);  
                        }catch(e){
                            console.error(e);
                            this.$store.commit('upfalg',true);
                        }
                        uni.hideLoading()
                        setTimeout(()=>{
                            this.$store.commit('upfalg',true); 
                        },Number(tiem))
                    };
                }catch(e){
                    console.error(e);
                    this.$store.commit('upfalg',true);
                } 
            },
            /**
             * 预售商品 数据初始化
             * @param null
             */
            _axios(){
                let data = {
                    //api: 'app.mchPreSell.getGoodsList',
                    api: 'plugin.presell.AppPreSell.getGoodsList',
                    sellType: this.sellType,
                    status: this.status,
                    pageNo: this.page,
                    pageSize: 10,
                } 
                this.$req.post({data}).then(res=>{ 
                    if(res.code == 200){
                        this.isData = res.data.list
                        this.isData.forEach((item, index)=>{
                            item.url += '?a=' + Math.floor(Math.random() * 1000000);
                        })
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: "none"
                        })
                    }
                })
            },
            /**
             * 预售商品 删除商品
             * @param null
             */
            async _del(){ 
                let data = {
                    api: 'plugin.presell.AppPreSell.del',
                    goodsIds: this.goodsIds.toString(),
                } 
                await this.$req.post({data}).then(res=>{ 
                    if(res.code == 200){
                        this.is_showToast1 = 1
                        this.is_showToast_obj.imgUrl = this.sus
                        this.is_showToast_obj.title = this.language.yushou_details.sus[0]
                        setTimeout(()=>{
                            this.is_showToast1 = 0
                        },2000) 
                        this._axios()
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: "none"
                        })
                    }
                })
            },
            /**
             * 预售商品 上架商品
             * @param null
             */
            async _add(){
                let data = {
                    //api: 'app.mchPreSell.upperAndLowerGoods',
                    api: 'plugin.presell.AppPreSell.upperAndLowerGoods',
                    goodsIds: this.goodsIds,
                    status: 2, //0下架 1上架
                } 
                await this.$req.post({data}).then(res=>{ 
                    if(res.code == 200){
                        this.is_showToast1 = 1
                        this.is_showToast_obj.imgUrl = this.sus
                        this.is_showToast_obj.title = this.language.yushou_details.sus[1]
                        setTimeout(()=>{
                            this.is_showToast1 = 0
                        },2000)
                        this._axios()
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: "none"
                        })
                    }
                })
            },
            /**
             * 预售商品 下架商品
             * @param null
             */
            async _delist(){
                let data = {
                    //api: 'app.mchPreSell.upperAndLowerGoods',
                    api: 'plugin.presell.AppPreSell.upperAndLowerGoods',
                    goodsIds: this.goodsIds,
                    status: 0, //0下架 1上架
                } 
                await this.$req.post({data}).then(res=>{ 
                    if(res.code == 200){
                        this.is_showToast1 = 1
                        this.is_showToast_obj.imgUrl = this.sus
                        this.is_showToast_obj.title = this.language.yushou_details.sus[2]
                        setTimeout(()=>{
                            this.is_showToast1 = 0
                        },2000) 
                        this._axios()
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: "none"
                        })
                    }
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
    /deep/.switchLable{
        z-index: 999;
    }
    .pages_box{
        //animation: anShowToast5 1s both;
    }
    //右滑 显示动画
    @keyframes anShowToast5 {
       0% {
       opacity: 0;
       margin-left: -100%;
       }
           
       100% {
       opacity: 1;
       margin-left: 0;
       }
    }
    .btn{
        text-align: center;
        border-radius: 52rpx;
        font-size: 24rpx;
        color: #333333;
        background: #F4F5F6;
        margin-left: 24rpx;
        padding: 12rpx 24rpx;
        box-sizing: border-box;
    }
    .pages_box_box_jw_btn{
        >view:first-child{
            margin-left: 0;
        }
    }
    .pages_box_box_jw_btn{
        display: flex;
    }
    .pages_box_box_jw_sj{
        font-size: 28rpx;
         
        font-weight: normal;
        color: #999999;
    }
    .pages_box_box_jw{
        margin-top: 26rpx;
        display: flex;
        justify-content: flex-end;
        align-items: center;
    }
    .pages_box_box_body_right_price_num{
        font-size: 24rpx;
         
        font-weight: normal;
        color: #999999;
    }
    .pages_box_box_body_right_price_jf{
        font-size: 32rpx;
        
        font-weight: normal;
        color: #FA5151;
    }
    .pages_box_box_body_right_price{
        margin-top: 24rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        .djClass{
            margin-left: 20rpx;
            font-size: 28rpx;
            color: #333333;
        }
    }
    .pages_box_box_body_rigth_gg{
        font-size: 24rpx;
         
        font-weight: normal;
        color: #999999;
        margin-top: 20rpx;
    }
    .pages_box_box_body_right_title {
        font-size: 32rpx;
         
        font-weight: normal;
        color: #333333;
        width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
    }

    .pages_box_box_body_right {
        width: 498rpx;
        margin-left: 24rpx;
        display: flex;
        flex-direction: column;
    }

    .pages_box_box_left {
        width: 164rpx;
        height: 164rpx;
    }

    .pages_box_box_body {
        display: flex;
    }

    .pages_box_box_top_zt {
        font-size: 24rpx;
         
        font-weight: normal;
        color: #FA5151;
    }

    .pages_box_box_top_dd {
        font-size: 28rpx;
         
        font-weight: normal;
        color: #333333;
    }

    .pages_box_box_top {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .pages_box_box {
        // width: 100%;
        display: flex;
        padding: 32rpx;
        flex-direction: column;
        background-color: #fff;
        border-radius: 24rpx;
        margin-bottom: 16rpx;
    }

    .pages_box {
        margin-top: 24rpx;
        width: 100%;
        display: flex;
        flex-direction: column;
    }

    .noFindDiv {
        width: 100%;
        padding-top: 430rpx;
        height: 100%;
        background-color: #F4F5F6;

        .noFindText {
            color: #333333;
        }

        .noFindImg {
            width: 750rpx;
            height: 394rpx;
        }
    }
    
    .zhanwei{
        width: 100%;
        height: 100rpx;
    }
    .isFabu{
        width: 100%;
        background-color: #FFFFFF;
        display: flex;
        position: fixed;
        bottom: 0;
        .isFabu_btn{
            width: 686rpx;
            height: 92rpx;
            background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
            border-radius: 52rpx;
            font-size: 32rpx;
            color: #FFFFFF;
            text-align: center;
            line-height: 92rpx;
            margin: 16rpx auto;
        }
    }
    .pages {
        width: 100%;
    }
</style>
