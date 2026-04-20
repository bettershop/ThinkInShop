<template>
    <view class="isWaterfall" :style="{padding:isPadding,minHeight:isHeight}">
        <!-- 
        //瀑布流逻辑：
        //默认切割第一个数据放入left数组在中（图片会在这带上left标签 data-name="0"）此时图片还未加载完成
        //图片加载完成后，会执行加载完毕事件（此时可以通过其他标签id，获取其他标签高度 + 图片高度）拿到当前单元div高度
        //因为图片带上left标签 所以当前单元div高度 属于left div。
        //left总高度 = left总高度 + 其他标签高度 + 图片高度
        //因为 left总高度 > right总高度 
        //所以从数据切割第一个数据放入right数组在中 （图片会在这带上right标签 data-name="1"）此时图片还未加载完成
        //图片加载完成后，会执行加载完毕事件...循环... 
        
        //使用：首页商品分类  tabBar/home.vue
        //插槽使用：预售商品  pagesC/preSale/goods/goods.vue   逻辑：禁用默认商品信息，父组件定义新的商品信息
        <waterfall
            ref="waterfall" 
            :isPadding="'0'" 
            :goodsLike="false"
            :addShopCar="false"
            :mchLogo="false"
            :goodsPriceText="false"
            @_seeGoods="_seeGoods"
            @_addShopCar="_addShopCar"
            @_goodsNumber="_goodsNumber"
            :goodList="list">
        </waterfall>
        -->
        <template>
            <!-- left -->
            <view class="isLeft">
                <view v-for="(item, index) in leftList" @tap="_seeGoods(item.id)" :key="item.id">
                    <!-- top -->
                    <view class="isWaterfall_img">
                        <!-- 商品主图 -->
                        <image-s 
                            :isSrc="item.imgurl"
                            :imageWidth="'100%'"
                            :imageBorderRadius="'16rpx'"
                            :dataName="1"
                            :imgIndex="item.id"
                            @_loadError="_leftError"
                            @imgError="handlerLeftError"
                            @_imageWaterfall="_imageWaterfall">
                        </image-s>
                        <!-- 已下架/已售罄 -->
                        <view 
                            v-if="item.status == 3 || item.contNum == 0"
                            class="dowmPro"> 
                            {{item.status == 3?language.waterfall.type[0]:item.contNum == 0?language.waterfall.type[1]:''}} 
                        </view>
                    </view>
                    <!-- content -->
                    <view class="isWaterfall_other" :id="'isWaterfall_other' + item.id">
                        <!-- 店铺 -->
                        <view v-if="mchShow" class="shop_name">
                            <!-- 店铺头像 -->
                            <image-s v-if="mchLogo" :isSrc="item.logo" :imageWidth="'30rpx'" :imageBorderRadius="'30rpx'"></image-s>
                            <span v-if="mchName"> {{item.mch_name}} </span>
                        </view>
                        <!-- 商品 名称 -->
                        <view v-if="goodsName" class="goods_name"> {{ item.product_title || item.name }} </view>
                        <!-- 商品 标签 -->
                        <view v-if="goodsLable && item.s_type_list.length" class="goods_lable">
                            
                            <view :style="{'backgroundColor':item_1.color}" v-for="(item_1, index_1) in item.s_type_list" :key="index_1">
                            {{item_1.name}}
                            </view>
                        </view>
                        <!-- 商品 价格 -->
                        <view v-if="goodsPrice" class="goods_price">                                         
                            <view>
                                <span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
                                <span>{{LaiKeTuiCommon.formatPrice(item.price)}}</span>
                                <!-- 划掉的价格 -->
                                <span v-if="goodsPriceText" class="text_decoration">{{LaiKeTuiCommon.formatPrice(item.yprice)}}</span>
                            </view>
                            <!-- 商品数量 -->
                            <view v-if="goodsShopNum">
                                <!-- 人付款 --> 
                                <span>{{ item.volum}}{{language.waterfall.xl}}</span>
                            </view>
                        </view>
                        <!-- #ifdef H5 -->
                            <!-- 插槽 ： 默认的商品信息隐藏，使用插槽方式显示新的商品信息-->
                            <slot name="slot_goodsInfo" :item="item"></slot>
                        <!-- #endif -->
                        <!-- 艹 小程序不支持动态插槽 -->
                        <!-- #ifdef MP -->
                            <!-- 预售使用 -->
                            <template v-if="type == 'yushou'">
                                <!-- 商品价格 -->
                                <view class="price2">
                                    <p>
                                        <span>
                                            {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}} <span>{{LaiKeTuiCommon.formatPrice(Number(item.price))}}</span>
                                        </span>
                                        <!-- 已定N件 -->
                                        <span>
                                            {{language.goods.goods.yd}}{{item.volum}}{{language.goods.goods.j}}
                                        </span>
                                    </p>
                                </view>
                                <!-- 定金 -->
                                <view class="price3" v-if="item.deposit">
                                    {{language.goods.goods.deposit}} <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(Number(item.deposit))}}</text>
                                </view>
                                <!-- 订货：划掉的价格 -->
                                <view class="price3 new_price" v-else>
                                    <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(Number(item.yprice))}}</text>
                                </view>
                            </template>
                        <!-- #endif -->
                    </view>
                    <!-- 找相似 -->
                    <view v-if="goodsLike" class="isWaterfall_btn" @tap.stop="_toUrl( '/pagesC/collection/discover?pro_id=' + item.id)">{{language.waterfall.type[6]}}</view>
                    <!-- 加入购物车 (实物商品) -->
                    <template v-if="item.commodity_type == 0">
                        <view v-if="addShopCar && item.isAddCar == 1" class="isWaterfall_btn1" @tap.stop="_addShopCar(item, item.id)">
                            <image-s :isSrc="addShopCarImgSrc" :imageWidth="'60rpx'" :imageBorderRadius="'60rpx'"></image-s>
                        </view>
                    </template>
                    <!-- 加入购物车 (虚拟商品) isAddCar=1 虚拟商品逻辑-无需核销/无需预约 才显示加入购物车 -->
                    <template v-else>
                        <view v-if="addShopCar && item.isAddCar == 1" class="isWaterfall_btn1" @tap.stop="_addShopCar(item, item.id)">
                            <image-s :isSrc="addShopCarImgSrc" :imageWidth="'60rpx'" :imageBorderRadius="'60rpx'"></image-s>
                        </view>
                    </template>
                </view>
            </view>
            <!-- right -->
            <view class="isRight">
                <view v-for="(item, index) in rightList" @tap="_seeGoods(item.id)" :key="item.id">
                    <!-- top -->
                    <view class="isWaterfall_img">
                        <!-- 商品主图 -->
                        <image-s 
                            :isSrc="item.imgurl"
                            :imageWidth="'100%'"
                            :imageBorderRadius="'16rpx'"
                            :dataName="2"
                            :imgIndex="item.id"
                            @_loadError="_rightError"
                            @imgError="handlerRightError"
                            @_imageWaterfall="_imageWaterfall">
                        </image-s>
                        <!-- 已下架/已售罄 -->
                        <view 
                            v-if="item.status == 3 || item.contNum == 0"
                            class="dowmPro"> 
                            {{item.status == 3?language.waterfall.type[0]:item.contNum == 0?language.waterfall.type[1]:''}} 
                        </view>
                    </view>
                    <!-- content -->
                    <view class="isWaterfall_other" :id="'isWaterfall_other' + item.id">
                        <!-- 店铺 -->
                        <view v-if="mchShow" class="shop_name">
                            <!-- 店铺头像 -->
                            <image-s v-if="mchLogo" :isSrc="item.logo" :imageWidth="'30rpx'" :imageBorderRadius="'30rpx'"></image-s>
                            <span v-if="mchName"> {{item.mch_name}} </span>
                        </view>
                        <!-- 商品 名称 -->
                        <view v-if="goodsName" class="goods_name"> {{ item.product_title || item.name }} </view>
                        <!-- 商品 标签 -->
                        <view v-if="goodsLable && item.s_type_list.length" class="goods_lable">
                            <view :style="{'backgroundColor':item_1.color}" v-for="(item_1, index_1) in item.s_type_list" :key="index_1">{{item_1.name}}</view>
                        </view>
                        <!-- 商品 价格 -->
                        <view v-if="goodsPrice" class="goods_price">                                         
                            <view>
                                <span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
                                <span>{{LaiKeTuiCommon.formatPrice(item.price)}}</span>
                                <span v-if="goodsPriceText" class="text_decoration">{{LaiKeTuiCommon.formatPrice(item.yprice)}}</span>
                            </view>
                            <view v-if="goodsShopNum">
                                <!-- 人付款 --> 
                                <span>{{ item.volum}}{{language.waterfall.xl}}</span>
                            </view>
                        </view>
                        <!-- #ifdef H5 -->
                            <!-- 插槽 ： 默认的商品信息隐藏，使用插槽方式显示新的商品信息-->
                            <slot name="slot_goodsInfo" :item="item"></slot>
                        <!-- #endif -->
                        <!-- 艹 小程序不支持动态插槽 -->
                        <!-- #ifdef MP -->
                            <!-- 预售使用 -->
                            <template v-if="type == 'yushou'">
                                <!-- 商品价格 -->
                                <view class="price2">
                                    <p>
                                        <span>
                                            {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}} <span>{{LaiKeTuiCommon.formatPrice(Number(item.price))}}</span>
                                        </span>
                                        <!-- 已定N件 -->
                                        <span>
                                            {{language.goods.goods.yd}}{{item.volum}}{{language.goods.goods.j}}
                                        </span>
                                    </p>
                                </view>
                                <!-- 定金 -->
                                <view class="price3" v-if="item.deposit">
                                    {{language.goods.goods.deposit}} <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(Number(item.deposit))}}</text>
                                </view>
                                <!-- 订货：划掉的价格 -->
                                <view class="price3 new_price" v-else>
                                    <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(Number(item.yprice))}}</text>
                                </view>
                            </template>
                        <!-- #endif -->
                    </view>
                    <!-- 找相似 -->
                    <view v-if="goodsLike" class="isWaterfall_btn" @tap.stop="_toUrl( '/pagesC/collection/discover?pro_id=' + item.id)">{{language.waterfall.type[6]}}</view>
                    <!-- 加入购物车 (实物商品) -->
                    <template v-if="item.commodity_type == 0">
                        <view v-if="addShopCar && item.isAddCar == 1" class="isWaterfall_btn1" @tap.stop="_addShopCar(item, item.id)">
                            <image-s :isSrc="addShopCarImgSrc" :imageWidth="'60rpx'" :imageBorderRadius="'60rpx'"></image-s>
                        </view>
                    </template>
                    <!-- 加入购物车 (虚拟商品) isAddCar=1 虚拟商品逻辑-无需核销/无需预约 才显示加入购物车 -->
                    <template v-else>
                        <view v-if="addShopCar && item.isAddCar == 1" class="isWaterfall_btn1" @tap.stop="_addShopCar(item, item.id)">
                            <image-s :isSrc="addShopCarImgSrc" :imageWidth="'60rpx'" :imageBorderRadius="'60rpx'"></image-s>
                        </view>
                    </template>
                </view>
            </view>
        </template>
    </view>      
</template>

<script>
    import imageS from '@/components/aComponents/images.vue';
    export default{
        props:{
            // --- 父组件属性 ---
            "type": {
                type: String,
                default: '', 
            },
            //总数据 它有一个镜像数据set_list用于组件的数据操作，它切换了存储的指针，防止父组件数据被切割。
            "goodList": {
                type: Array,
                default: [], 
            },
            //是否显示 店铺
            "mchShow": {
                type: Boolean,
                default: true, 
            },
            //是否显示 店铺logo
            "mchLogo": {
                type: Boolean,
                default: true, 
            },
            //是否显示 店铺名称
            "mchName": {
                type: Boolean,
                default: true, 
            },
            //是否显示 商品名称
            "goodsName": {
                type: Boolean,
                default: true, 
            },
            //是否显示 商品标签
            "goodsLable": {
                type: Boolean,
                default: true, 
            },
            //是否显示 商品价格
            "goodsPrice": {
                type: Boolean,
                default: true, 
            },
            //是否显示 商品原价
            "goodsPriceText": {
                type: Boolean,
                default: true, 
            },
            //是否显示 商品已购买人数
            "goodsShopNum": {
                type: Boolean,
                default: true, 
            },
            //是否显示 商品找相似
            "goodsLike": {
                type: Boolean,
                default: true, 
            },
            //是否显示 商品加入购物车
            "addShopCar": {
                type: Boolean,
                default: true, 
            },
            //isWaterfall 内边距
            "isPadding": {
                type: String,
                default: '30rpx', 
            },
            //isWaterfall 最小高度 （设置最小高度，优化切换分类导航时，加载时间不一致造成的页面跳动问题，首页有用到）
            "isHeight": {
                type: String,
                default: '', 
            },
            //是否 清空leftList/rightList  
            //商品瀑布：数据是否初始化（切换导航时清空左右div数据）
            "isDataKO": {
                type: Boolean,
                default: false, 
            },
        },
        components:{
            imageS
        },
        data(){
            return {
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                set_list: [],   //总数据goodList镜像，此镜像数据会被切割。
                leftList: [],   //左 显示的数据
                rightList: [],  //右 显示的数据
                leftHeight: 0, //左单元 高度
                rightHeight:0, //右单元 高度
                goodsNumber:0, //当前商品数量
                ll:'',
                //加入购物车图标
                addShopCarImgSrc: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/redgwc.png',
                //商品默认图片
                defaultPicture: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
            }
        },
        watch:{
            goodList: {
                deep: true,       // 开启深度监听  专门对付复杂数据类型
                immediate: true,  // 首次监听  一打开页面就会监听
                //监听事件
                handler(e) {
                    console.log('监听到数据变化：',e);
                    //切换时清空，下拉加载时不清空
                    if(this.isDataKO){
                        console.log('清空数据');
                        this.rightList = []
                        this.leftList = []
                        this.leftHeight = 0
                        this.rightHeight = 0
                        this.goodsNumber = 0
                    }
                    //返回满足条件的数据 进行数据去重处，
                    let newList = e.filter((item,index)=>{
                        //过滤 右边已存在的数据
                        let right = this.rightList.every((its,inx)=>{
                            return its.id != item.id
                        })
                        //过滤 左边已存在的数据
                        let left = this.leftList.every((its,inx)=>{
                            return its.id != item.id
                        })
                        return right == true && left == true
                    })
                    //深拷贝，解决拷贝指针问题
                    this.set_list = JSON.parse(JSON.stringify(newList))
                    //切割数据，分配至左边或者右边
                    this._waterfall()
                },
            },
        },
        mounted() {
            //切割分配第一条数据至左侧
            //this._waterfall()
            //console.log('切割分配第一条数据至左侧，剩余数据：', this.set_list);
            this.ll = this.language.waterfall.type[3]
        },
        methods:{
            handlerRightError(index){
                this.rightList[index].imgurl = this.ErrorImg

            },
            handlerLeftError(index){
                this.leftList[index].imgurl = this.ErrorImg

            },
            //跳转
            _toUrl(url){
                uni.navigateTo({
                    url
                });
            },
            //加入购物车事件 返回给父组件
            _addShopCar(item,id){
                this.$emit('_addShopCar', item, id)
            },
            //查看商品详情事件  返回给父组件
            _seeGoods(id){
                this.$emit('_seeGoods', id)
            },
            //当前商品数量  返回给父组件
            _goodsNumber(isNumber){
                this.$emit('_goodsNumber', isNumber)
            },
            //当前图片 加载失败返回事件
            _leftError(itemId){
                this.leftList.forEach((item, index)=>{
                    //图片加载失败，则使用默认图片
                    if(item.id == itemId){item.imgurl = this.defaultPicture}
                })
            },
            //当前图片 加载失败返回事件
            _rightError(itemId){
                this.rightList.forEach((item, index)=>{
                    //图片加载失败，则使用默认图片
                    if(item.id == itemId){item.imgurl = this.defaultPicture}
                })
            },
            //当前图片 加载完毕返回事件
            _imageWaterfall(type, imgHeight, index){
                //获取 img高度
                let newImgHeight = imgHeight
                //获取 其他标签高度 (需要计算其他标签高度时)
                let height = 0;
                const query = uni.createSelectorQuery().in(this);
                //根据type的值 获取当前div的id
                if(type == 1){
                    query.select('#isWaterfall_other'+index).boundingClientRect(data => {
                        height = data.height;
                        //当前单元高度  (*2用来放大差距)
                        let wholeHeight = newImgHeight + (height * 2);
                        //单元高度 是属于left div
                        this.leftHeight += wholeHeight
                        //console.log('当前高度：左' + this.leftHeight + '，右' + this.rightHeight);
                        //切割并分配数据
                        this._waterfall()
                    }).exec();
                } else {
                    query.select('#isWaterfall_other'+index).boundingClientRect(data => {
                        height = data.height;
                        //当前单元高度  (*2用来放大差距)
                        let wholeHeight = newImgHeight + (height * 2);
                        //单元高度 是属于right div
                        this.rightHeight += wholeHeight
                        //console.log('当前高度：左' + this.leftHeight + '，右' + this.rightHeight);
                        //切割并分配数据
                        this._waterfall()
                    }).exec();
                }
            },
            //切割总数据，分配至 左/右 单元数据
            _waterfall(){
                //统计当前商品数量，并把数量传给父组件，用于下拉加载判断
                this.goodsNumber++
                this._goodsNumber(this.goodsNumber)
                //数据分配完了 提示
                if(this.set_list.length == 0){
                    return
                }
                //数据切割分配
                if(this.leftHeight>this.rightHeight){
                    //切割分配至rightList
                    let cardList = this.set_list.splice(0,1);
                    this.rightList.push(cardList[0]);
                }else if(this.leftHeight<=this.rightHeight){
                    //切割分配至leftList
                    let cardList = this.set_list.splice(0,1);
                    this.leftList.push(cardList[0]);
                }
            },
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
    .isWaterfall{
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: space-between;
        box-sizing: border-box;
        .isLeft{
            width: 336rpx;
            height: auto;
            display: flex;
            flex-direction: column;
            >view{
                position: relative;
                background-color: #ffffff;
                border-radius: 16rpx;
                margin-bottom: 20rpx;
            }
        }
        .isRight{
            width: 336rpx;
            height: auto;
            display: flex;
            flex-direction: column;
            >view{
                position: relative;
                background-color: #ffffff;
                border-radius: 16rpx;
                margin-bottom: 20rpx;
            }
        }
        .isWaterfall_img{
            width: 100%;
            height: auto;
            min-height: 140rpx;
            position: relative;
            .dowmPro {
                width: 120rpx;
                height: 120rpx;
                border-radius: 50%;
                background-color: rgba(0, 0, 0, 0.6);
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                color: #FFFFFF;
                font-size: 26rpx;
                line-height: 120rpx;
                text-align: center;
            }
        }
        .isWaterfall_other{
            display: flex;
            flex-direction: column;
            .shop_name{
                display: flex;
                align-items: center;
                margin: 16rpx;
                margin-top: 10rpx;
                margin-bottom: 8rpx;
                /deep/ .isImage{
                    margin-right: 12rpx;
                }
                >span{
                    color: #999;
                    font-size: 24rpx;
                    line-height: 32rpx;
                }
            }
            .goods_name{
                font-size: 28rpx;
                color: #333333;
                line-height: 40rpx;
                margin: 16rpx;
                margin-top: 4rpx;
                // 超出隐藏并省略
                width: 300rpx;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 1;
            }
            .goods_lable{
                display: flex;
                flex-wrap: wrap;
                margin-right: 24rpx;
                margin-bottom: 8rpx;
                padding-left: 20rpx;
                >view{
                    //min-width: 64rpx;
                    //height: 40rpx;
                    padding: 8rpx;
                    box-sizing: border-box;
                    font-size: 20rpx;
                    color: #ffffff;
                    height: 40rpx;
                    border-radius: 8rpx;
                    margin-right: 8rpx;
                    margin-bottom: 8rpx;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    box-sizing: border-box;
                }
            }
            .goods_lable1{
                width: -webkit-fit-content;
                width: fit-content;
                height: 42rpx;
                background: linear-gradient(113deg, #FA5151 0%, #FF6F6F 100%);
                border-radius: 30rpx;
                padding: 0 16rpx;
                margin: 12rpx 0;
                margin-left: 16rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                
                font-size: 24rpx;
                font-weight: 500;
                color: #FFFFFF;
            }
            /deep/.goods_price{
                font-size: 24rpx;
                padding: 0 16rpx 16rpx 16rpx;
                box-sizing: border-box;
                display: flex;
                align-items: flex-end;
                >view:nth-child(1){
                    color: #FA5151;
                    font-weight: 600;
                    line-height: 32rpx;
                    display: flex;
                    align-items: flex-end;
                    >span:nth-child(2){
                        font-size: 34rpx;
                        font-family: DIN;
                        font-weight: bold;
                        line-height: 38rpx !important;
                        margin-right: 8rpx;
                    }
                    .text_decoration{
                        color: #C2C4C8;
                        text-decoration: line-through;
                    }
                }
                >view:nth-child(2){
                    color: #666666;
                    line-height: 32rpx;
                }
            }
            .goods_price_dj{
                color: #FA5151;
                font-size: 24rpx;
                padding: 0 16rpx 16rpx 16rpx;
                box-sizing: border-box;
                display: flex;
                align-items: flex-end;
                margin-top: -12rpx;
            }
        }
        .isWaterfall_btn{
            position: absolute;
            display: flex;
            right: 0;
            bottom: 0;
            align-items: center;
            justify-content: center;
            width:90rpx;
            height:36rpx;
            background:#FA9D3B;
            border-radius:10rpx 0rpx 0rpx 0rpx;
            margin-left: auto;
            font-size: 22rpx;
            color: #ffffff;
        }
        .isWaterfall_btn1{
            position: absolute;
            right: 16rpx;
            bottom: 16rpx;
        }
    }
</style>
