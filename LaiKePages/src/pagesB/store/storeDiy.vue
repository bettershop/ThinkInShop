<template>
    <view  v-if="styleConfig.length > 0"  :style="grbj"> 
        <!-- DIY首页 -->
        <template>
            <view
            >
                <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
                <block v-for="(item, index) in styleConfig" :key="index"> 
                    <combination
                        v-if="item.name == 'combination'"
                        :dataConfig="item"
                        @toUrl="toUrl"
                    ></combination>
                    <guide
                        v-if="item.name == 'guide'"
                        :dataConfig="item"
                    ></guide>
                    <headerSerch
                        v-if="item.name == 'headerSerch'"
                        :dataConfig="item"
                        @toUrl="toUrl"
                        :region="region"
                    ></headerSerch>
                    <homeAdv
                        v-if="item.name == 'homeAdv'"
                        :dataConfig="item"
                        @toUrl="toUrl"
                    ></homeAdv>
                    <homeStore
                        v-if="item.name == 'homeStore'"
                        :dataConfig="item"
                        @toUrl="toUrl"
                    ></homeStore>
                    <homeVideo
                        v-if="item.name == 'homeVideo'"
                        :dataConfig="item"
                    ></homeVideo>
                    <menus
                        v-if="item.name == 'menus'"
                        :dataConfig="item"
                        @toUrl="toUrl"
                        :shop_id="shop_id"
                    ></menus>
                    <richText
                        v-if="item.name == 'richText'"
                        :dataConfig="item"
                        @toUrl="toUrl"
                    ></richText>
                    <seckill
                        v-if="item.name == 'seckill'"
                        :dataConfig="item"
                        @refresh="_axios"
                        @toUrl="toUrl"
                    ></seckill>
                    <reduction
                        v-if="item.name == 'reduction'"
                        :dataConfig="item"
                        @toUrl="toUrl"
                    ></reduction>
                    <swiperBg
                        v-if="item.name == 'swiperBg'"
                        :dataConfig="item"
                        @toUrl="toUrl"
                    ></swiperBg>
                    <swiperPicture
                        v-if="item.name == 'swiperPicture'"
                        :dataConfig="item"
                        @toUrl="toUrl"
                    ></swiperPicture>
                    <tabNav
                        ref="diyFl"
                        v-if="item.name == 'tabNav'"
                        :dataConfig="item"
                        :page="page"
                        :is_grade="is_grade"
                        :shop_id="shop_id"
                        :is_pen="is_open"
                        @loadingType="onHandleLoadingType"
                        @changePage="changePage"
                        @toUrl="toUrl"
                    ></tabNav>
                </block>
            </view>
        </template>

        <!-- #ifdef H5 -->
        <!-- 获取ios手机虚拟键盘高度 -->
        <view class="virtualKeyBoardHeight safe-area-inset-bottom" style="width: 100vw;height: 0rpx;opacity: 0;">1</view>
        <!-- #endif -->

        <!-- 弹窗提示区 -->
        <template>
            <!-- 提示：APP更新 -->
            <div class="mask" v-if="widgetinfoo">
                <div class="mask_all">
                    <img :src="upbg" class="mask_bg" />
                    <img :src="close" class="mask_close" @tap="close_mask()" />
                    <b class="mask_titel"
                        >{{ newapp.version }}{{ language.home.Tips[2] }}</b
                    >
                    <div class="mask_content">
                        <wxParse :content="newapp.note"></wxParse>
                    </div>
                    <div class="mask_text">
                        <div class="mask_btn" @tap="update()">
                            {{ language.home.Tips[1] }}
                        </div>
                    </div>
                </div>
            </div>

 

             <!-- 取消成功 -->
        <view class="xieyi mask" style="background-color: initial;" v-if="is_susa">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">{{ xieyiTitle }}</view>
            </view>
        </view>

            <!-- 提示：请授权位置获取更好的体验 -->
            <showToast
                :is_showToast="opensetting1"
                :is_showToast_obj="is_showToast_obj"
                @richText="opensetting1 = 0"
                @confirm="_openSetting"
            >
            </showToast>

            <!-- 提示：授权位置失败，请在系统设置中打开定位服务 -->
            <showToast
                :is_showToast="opensetting2"
                :is_showToast_obj="is_showToast_obj"
                @richText="opensetting2 = 0"
            >
            </showToast>

            <!-- 提示：vip续费  -->
            <view class="vippupu" v-if="vippupo && isxufei">
                <view class="vippupubox">
                    <view class="vippupubox_img" :style="bg_vip">
                        <view class="rights">
                            <scroll-view scroll-x="true" class="scrollx">
                                <view
                                    class="item"
                                    v-for="(item, index) in memberEquity"
                                    :key="index"
                                >
                                    <view class="itemimg">
                                        <img :src="item.icon" alt="" />
                                    </view>
                                    <view class="title">{{
                                        item.equityName
                                    }}</view>
                                    
                                </view>
                            </scroll-view>
                        </view>
                        <view class="vipbtn">
                            <view class="vipbtn_box" @click="vipClick">{{
                                language.my.xfvip
                            }}</view>
                        </view>
                        <view class="checkBox">
                            <image
                                @click="xzbtnClick()"
                                style="width: 32rpx; height: 32rpx"
                                :src="xzbtn ? xz : yuanquan"
                            >
                            </image>
                            <view class="checkBox_title">{{
                                language.my.gxbzxs
                            }}</view>
                        </view>
                    </view>
                    <view class="vippupubox_img_img" @click="shutClick">
                        <image
                            style="width: 100%; height: 100%"
                            :src="guanbi"
                        ></image>
                    </view>
                </view>
            </view>

            <!-- 提示：待付款订单弹窗 -->
            <template v-if="isShow_dialog_Pre">
                <showToast
                    :is_showToast="is_showToast"
                    :is_showToast_obj="is_showToast_obj"
                    :load="true"
                    @richText="is_showToast = 0"
                    @confirm="_order"
                >
                </showToast>
            </template>
        </template>
        <skus ref="attrModal" @confirm="_confirm"></skus>
        <!-- 回到顶部 -->
        <image
            class="zhiding"
            v-if="scrollTopNum > 1000"
            :src="zhiding"
            @tap="_zhiding"
        ></image>
        <!-- 公告提示弹窗 -->
        <notice ref="mynotice"></notice>
    </view>
</template>

<script>
     import notice from '@/components/notice.vue'
import showToast from "@/components/aComponents/showToast.vue";
import scrollViewNav from "@/components/aComponents/switchNavThree.vue";
import waterfall from "@/components/aComponents/waterfall.vue"; 

import headerSerch from "@/components/DIYComponents/headerSerch"; 
import combination from "@/components/DIYComponents/combination";
import guide from "@/components/DIYComponents/guide";
import menus from "@/components/DIYComponents/menus";
import richText from "@/components/DIYComponents/richText";
import seckill from "@/components/DIYComponents/seckill";
import reduction from "@/components/DIYComponents/reduction";
import swiperBg from "@/components/DIYComponents/swiperBg";
import tabNav from "@/components/DIYComponents/tabNav";
import homeAdv from "@/components/DIYComponents/homeAdv";
import homeStore from "@/components/DIYComponents/homeStore";
import HomeVideo from "@/components/DIYComponents/homeVideo";
import SwiperPicture from "@/components/DIYComponents/swiperPicture";
// #ifdef APP-PLUS 
import wxParse from '@my-miniprogram/src/components/mpvue-wxparse/src/wxParse.vue';
// #endif
import skus from "../../components/skus.vue";
import { getTimeDiff } from "@/common/util.js";
import { mapMutations, mapState } from "vuex";
export default {
    props: {
        shop_id: {
            type: String,
            default: "",
        },
        is_open:{
            type: String,
            default: "",
        },
    },
    
    data() {
        return {
            // 取消关注
            is_susa:false,
            xieyiTitle:'',
            sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
            //首页整体背景图
            grbj:
                "background-image: url(" +
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/grbj.png);background-size: 100vw 100vh;background-repeat: no-repeat; ",
            closeDiy: false, //diy：通过页面参数关闭diy来方便调试
            styleConfig: [], //diy：显示数据列表
            is_grade: false, //diy：tabNav组件控制是否显示商品价格
            //待支付弹窗
            isShow_dialog_Pre: false, //待支付弹窗：是否显示
            myPopup: 0, //待支付弹窗：显示待支付数量
            is_showToast: 0, //待支付弹窗: 是否显示
            is_showToast_obj: {}, //待支付弹窗: 弹窗内容
            grounds:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/home_remind.png", //待支付弹窗：背景图
            diongClose:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/home_remind3.png", //待支付弹窗图：删除按钮图
            //APP更新弹窗
            widgetinfoo: false, //APP更新弹窗：是否显示
            newapp: {}, //APP更新弹窗：标题内容
            upbg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/upbg1.png", //APP更新弹窗：背景图
            //提醒签到弹窗 
            home_sign:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "/images/icon1/home_sign.png", //提醒签到弹窗：背景图
            cha:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "/images/icon1/close_y.png", //提醒签到弹窗：删除按钮图
            //提醒开启定位弹窗
            opensetting1: 0, //提醒开启授权：绝授权之后再次提醒
            opensetting2: 0, //提醒开启定位：是否显示
            //VIP续费弹窗
            vippupo: false, //VIP续费弹窗：是否显示
            isxufei: false, //VIP续费弹窗：是否显示
            xzbtn: false, //VIP续费弹窗：用于判断是否勾选
            xz:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/member/xz.png", //VIP续费弹窗：勾选图标
            yuanquan:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/member/yuanquan.png", //VIP续费弹窗：未勾选图标
            guanbi:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/member/guanbi.png", //VIP续费弹窗：关闭弹窗
            bg_vip:
                "background-image: url(" +
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/member/vippupu.png)", //VIP续费弹窗：背景图
            //顶部区
            xxnum: "", //系统消息：未读数量
            grxx:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/grxx.png", //系统消息：图标
            region: { district: "岳麓区" }, //定位：默认地区
            isBg: false, //定位：是否设置白色背景
            longitude: "112.951227", //定位：精度
            latitude: "28.227965", //定位：纬度
            jianX_h:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/icon_xiahua.png", //定位： 向下尖括号图标
            syss:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/home/syss.png", //搜索：放大镜
            scrollTopNum_show: false, //header组件：是否显示
            titleColor: "#333333", //header组件：标题字体颜色
            bgColor: [{ tem: "#FFFFFF" }, { item: "#FFFFFF" }], //header组件：背景色
            headBg:
                "url(" +
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/grbj.png) 750rpx 100%;", //header组件：背景图
            //banner区
            banner: [], //banner：轮播列表图
            dotIndex: 0, //banner：轮播列表图下标
            //金刚区
            nav_list: [], //金刚区：插件列表
            appTitle: "", //首页标题
            //内容区
            livingStatus:false,//直播：是否显示
            Marketing_list: [], //内容区：显示插件
            Marketing_list_zdy: [], //自定义活动：活动列表
            Marketing_list_jf: {}, //积分商城：活动列表
            newImgList: [], //新品上市：商品列表
            recommendImgList: [], //好物优选：商品列表
            bottom_list: [], //商品分类：分类列表
            class_pro: [], //分类商品：商品列表
            handleNum: 0, //分类商品：当前分类商品数量
            class_cid: "", //分类商品：当前分类id用于下拉加载更多
            opensetting: false, //推荐店铺：是否显示
            r_mch: [], //推荐店铺：店铺列表
            memberPlugin: false, //VIP商品：是否显示
            goodsArr: [], //VIP商品：商品列表
            is_vip: 0, //VIP商品：跳转会员中心传入参数
            fxBg: "", //分销商品：广告图
            distribution_list: [], //分销商品：商品列表
            subtraction_list: [], //满减券： 列表
            secList: [], //限时秒杀：商品列表
            isDataKO: false, //商品瀑布：数据是否初始化（切换导航时清空左右div数据）
            minHeight: "", //瀑布流最小高度，用来固定切换导航时显示位置
            //回到顶部
            zhiding:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/zhiding.png", //回到顶部图标
            scrollTopNum: 0, //距离顶部距离
            //其他逻辑使用参数
            time: { hours: "00", minutes: "00", seconds: "00" }, //时：分：秒
            isClick: true, //用于防止重复点击
            Intervalid: "", //用于清除定时器
            page: 1, //请求第几页数据
            loadingType: 0, //下拉加载状态 0下拉加载更多/1加载中/2没有更多了
            clientHeight: 0, //当前屏幕高度
            coverBg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/product_share_img/cover.png", //小程序转发默认图--》不传则会默认选中当前页面顶部一部分截图
            onReachBottomBg: [{ item: "#014343" }, { item: "#014343" }], //背景色
            //其他新增参数
            memberEquity: "",
            // 加入购物车弹窗所需参数
            fastTap: true,
            proid: "",
            haveSkuBean: "",
            num: "",
            numb: "",
            secTitle: "",
            disTitle: "",
            isJF:false,//是否显示积分入口 
            homeOption: '',//onLoad中的option
            default_img:"../../static/img/default-img.png",
        };
    },
    components: {
        // #ifdef APP-PLUS
        wxParse,
        // #endif
        notice,
        SwiperPicture,
        HomeVideo,
        headerSerch, 
        combination,
        guide,
        menus,
        richText,
        seckill,
        swiperBg,
        tabNav,
        homeAdv,
        homeStore,
        reduction,
        waterfall,
        scrollViewNav, 
        showToast,
        skus,
    },
    computed: {
        //Mp时设置高度
        halfWidth: function () {
            var gru = uni.getStorageSync("data_height")
                ? uni.getStorageSync("data_height")
                : this.$store.state.data_height;
            var heigh = parseInt(gru);
            // #ifdef MP
            heigh = 0;
            // #endif
            var he = heigh * 2;
            return uni.upx2px(he) + "px";
        },
        ...mapState({
            _cart_num: "cart_num",
        }),
    },

    //点击 tab 时触发
    onTabItemTap(e) {
        //关闭小程序授权弹出窗
        this.LaiKeTuiCommon.closeMPAuthWin(this);
    },
    //小程序右上角转发事件
    onShareAppMessage: function (res) {
        return {
            title: this.language.home.title,
            path: "/pages/shell/shell?pageType=home",
            success: function (res) {},
            fail: function (res) {},
        };
    },
    mounted(option) {
        // #ifdef H5
        const query = uni.createSelectorQuery().in(this);
        query.select('.virtualKeyBoardHeight').boundingClientRect(data => {
            uni.setStorageSync('virtualKeyBoardHeight', {name:'手机虚拟键盘高度',value:data?.height-1})
        }).exec();
        // #endif 
        
        //存一下option，在onshow用
        // this.homeOption = option
  

        //移除位置缓存
        uni.removeStorageSync("region");
        uni.removeStorageSync("signFlag");

        //移除移动店铺端拼团缓存
        uni.removeStorageSync("shacoList");
        uni.removeStorageSync("proIds");

        //清除倒计时
        clearInterval(this.Intervalid);

        //调试diy
        // this.closeDiy = option.closeDiy;

        //当前屏幕高度
        // #ifdef H5
        this.clientHeight = document.body.clientHeight;
        // #endif
        // #ifndef H5
        this.clientHeight = uni.getSystemInfoSync().windowHeight;
        // #endif

        // #ifdef APP-PLUS
        this.widgetinfo();
        // #endif

        //好像没有用处
        uni.removeStorageSync("isHomeShow");
        this.getPage();
    },
    //页面卸载
    onUnload() {
        //清除定时器
        clearInterval(this.Intervalid);
    },
    onHide() {},
    //监听页面初次渲染完成
    onReady() {
        //动态设置当前页面的标题
        setTimeout(() => {
            uni.setNavigationBarTitle({ title: this.language.home.title  });
        }, 1000);
    },
    //上拉加载
    onReachBottom: function () {
        //diy上拉
        if (this.loadingType != 0) {
                return;
            }
            //diy上拉 页面++
            this.page++;
            //直接return 在diy组件中通过watch监听page变化 实现加载更多
            return;
    },
    //下拉刷新
    onPullDownRefresh() {
        uni.removeStorageSync("region");
        /*
         ** 获取页面数据
         */
        this.getShow(); //----------》数据渲染 开始 入口
        /*
         ** 获取页面数据
         */
    },
    //滚动条事件
    onPageScroll(e) {
        //节点离窗口顶部的距离
        let top1 = 0;
        const query = uni.createSelectorQuery().in(this);
        query
            .select(".goodsBox")
            .boundingClientRect((data) => {
                if (data) {
                    top1 = data.top;
                    //如果 节点离窗口顶部的距离 < 当前屏幕高度clientHeight > 0
                    if (top1 < this.clientHeight && top1 > 0) {
                        //并且 瀑布流数据大于0的时候 设置瀑布流最小高度
                        if (this.class_pro.length) {
                            //设置瀑布流最小高度 -- 用于切换导航时 固定导航在窗口的位置
                            this.minHeight =
                                (this.clientHeight - top1) * 2 + "rpx";
                        }
                    }
                }
            })
            .exec();
        //是否显示--回到顶部
        this.scrollTopNum = e.scrollTop;
        //是否隐藏 header组件中的标题模块
        if (e.scrollTop > 0) {
            this.headBg = "#ffffff";
            if (!this.isBg) {
                this.isBg = true;
            }
            // #ifdef MP-WEIXIN
            if (!this.scrollTopNum_show) {
                this.scrollTopNum_show = true;
            }
            // #endif
        } else {
            this.headBg =
                "url(" +
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/grbj.png) 750rpx 100%;";
            this.isBg = false;
            // #ifdef MP-WEIXIN
            this.scrollTopNum_show = false;
            // #endif
        }
    },
    //事件
    methods: {
        getPage() {
        uni.removeStorageSync('payTarget')
        this.loadingType = 0 
        this._axios();
        this.isxufei = false;
        uni.$emit("update");
        if (uni.getStorageSync("isxufei")) {
            this.isxufei = true;
        }
        // 待付款订单数量
        this.myPopup = uni.getStorageSync("myPopup");
        if (this.myPopup > 0 && uni.getStorageSync("login_key") == 0) {
            this.isShow_dialog_Pre = true;
            uni.setStorageSync("login_key", 1);
        }
        //预售订单待支付弹窗
        if (this.myPopup > 0) {
            this.is_showToast = 6;
            this.is_showToast_obj.title = "提示";
            this.is_showToast_obj.contentNodes =
                '<p>您有 <span style="color:#D73B48">' +
                this.myPopup +
                "</span> 笔待付款的预售订单，<br />请尽快处理！</p>";
            this.is_showToast_obj.button = "取消";
            this.is_showToast_obj.endButton = "去处理";
        }
        //购物车 右上角数量
        this.getCart();
        //新品上市：商品列表
        this.getNewImg();
        //好物优选：商品列表
        this.getRecommendImg();
        //限时秒杀：秒杀商品
        this.getSeckill(); 
        //会员商品
        this.getHasGrades();
        //防止重复点击跳转链接
        this.isClick = true;
        //好像没有用处
        if (uni.getStorageSync("isHomeShow")) {
            uni.pageScrollTo({
                scrollTop: 0,
                duration: 10,
            });
            uni.removeStorageSync("isHomeShow");
        }
        
        /*
         ** 获取页面数据
         */
        this.getShow(); //----------》数据渲染 开始 入口
        /*
         ** 获取页面数据
         */
        
        //分享进入
        // if(this.homeOption.scene || this.homeOption.share == 'true'){
        //     //小程序二维码分享 重定向
        //     if(this.homeOption.scene){
        //         this._wxEwmShare(this.homeOption.scene)
        //     } else {
        //         this._share(this.homeOption)
        //     }
        // } 

        // 调用弹窗
        // setTimeout(()=>{
        //     this.$refs.mynotice.getData()
        // },0)
    }, 
        //获取用户信息
        getUserInfor(){
            let data = {
                api: "app.user.index",
            };
            this.$req.post({ data }).then(res => {
                uni.setStorageSync('userinfo', res.data.data.user)
            })
        },
 
      
        // suk组件返回数据
        _confirm(sku) {
            Object.assign(this.$data, sku);

            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            if (Boolean(this.haveSkuBean)) {
                if (this.num == 0) {
                    uni.showToast({
                        title: this.language.proList.Tips[0],
                        duration: 1000,
                        icon: "none",
                    });
                    this.fastTap = true;
                } else if (this.num != 0) {
                    this._shopping();
                }
            } else {
                if (this.num == 0) {
                    uni.showToast({
                        title: this.language.proList.Tips[0],
                        duration: 1000,
                        icon: "none",
                    });
                    this.fastTap = true;
                } else {
                    uni.showToast({
                        title: this.language.proList.Tips[1],
                        duration: 1000,
                        icon: "none",
                    });
                    this.fastTap = true;
                }
            }
        },
        // 点击确定购买之后，如果库存不为零。则运行
        _shopping(id) {
            if (this.haveSkuBean) {
                var data = {
                    api: "app.product.add_cart",
                    pro_id: this.proid,
                    attribute_id: this.haveSkuBean.cid,
                    num: this.numb,
                    type: "addcart",
                };

                this.$req
                    .post({
                        data,
                    })
                    .then((res) => {
                        let { code, data, message } = res;
                        if (code == 200) {
                            uni.showToast({
                                title: this.language.proList.Tips[2],
                                icon: "none",
                            });

                            this.$store.state.access_id = data.access_id;

                            this.haveSkuBean = "";
                            this.$refs.attrModal._mask_f();
                            this.fastTap = true;
                            this.getCart()
                        } else {
                            uni.showToast({
                                title: message,
                                icon: "none",
                            });
                            this.fastTap = true;
                        }
                    })
                    .catch((error) => {
                        this.fastTap = true;
                    });
            } else {
                this.fastTap = true;
            }
        },
        //加入购物车end
        //查看商品详情
        _seeGoods(id) {
            this._goods(id);
        },
        //当前分类商品数量
        _goodsNumber(num) {
            this.handleNum = num;
        },
        //商品分类
        _changeNav(item, index) {
            //记录分类id 用于下拉加载更多
            try{ 
                if(!item)return
                this.class_cid = item.cid;
                //初始化 开启下拉加载，如果请求数据小于10则>0，停止禁用下拉并提示～我是有底线的～
                this.page = 1;
                this.loadingType = 0;
                //每次点击 都得初始化上一次商品瀑布流的数据
                this.isDataKO = true;
                //显示当前选中的下标数据
                this.class_pro = this.bottom_list[index].list.concat();
                //商品图片参数修改
                this.class_pro.forEach((item, index) => {
                    //可能有相同路径的图片，会无法加载@load事件；拼接随机字符串，可以解决这个问题
                    item.imgurl = item.cover_map + '?a=' + Math.floor(Math.random() * 1000000);
                    item.volumes = item.volume;
                    item.volume = item.payPeople;
                });
                //500毫秒，取消初始化
                setTimeout(() => {
                    this.isDataKO = false;
                }, 100);
            }catch(e){
                //TODO handle the exception
                this.class_pro = []
                this.class_cid = ''
                console.error('home.vue:',e)
            }
        },
        //回到顶部
        _zhiding() {
            uni.pageScrollTo({
                scrollTop: 0,
                duration: 300,
            });
        },
        //购物车 右上角数量
        getCart() {
            var data = {
                api: "app.cart.index",
                cart_id: "",
                page: 1,
            };
            this.$req.post({ data }).then((res) => {
                let {
                    code,
                    data: { data, list, mch_list, login_status, grade },
                    message,
                } = res;
                if (code == 200) {
                    this.cart_num(data.length);
                    //添加，移除tabbar购物车小点提醒
                    if (this._cart_num) {
                        var cart_num_str = String(this._cart_num);
                        // #ifdef MP-WEIXIN
                        uni.setTabBarBadge({
                            index: 2,
                            text: cart_num_str,
                        });
                        // #endif
                        // #ifndef MP-WEIXIN
                        uni.setTabBarBadge({
                            index: 2,
                            text: cart_num_str,
                        });
                        // #endif
                    } else {
                        // #ifdef MP-WEIXIN
                        uni.removeTabBarBadge({
                            index: 2,
                        });
                        // #endif
                        // #ifndef MP-WEIXIN
                        uni.removeTabBarBadge({
                            index: 2,
                        });
                        // #endif
                    }
                } else {
                    // #ifdef MP-WEIXIN
                    uni.removeTabBarBadge({
                        index: 2,
                    });
                    // #endif

                    // #ifndef MP-WEIXIN
                    uni.removeTabBarBadge({
                        index: 2,
                    });
                    // #endif
                }
            });
        },

        // - - - 分割线 - - -

        //VUEX 缓存库 @/store/index.js
        ...mapMutations({
            cart_num: "SET_CART_NUM",
        }),

        // - - - 分割线 - - -

        /*
         ** 跳转开始
         */

        //跳转：先验证登录
        _navgTo(url) {
            // this.isLogin(() => {
                uni.navigateTo({
                    url: url,
                });
            // });
        },
        //跳转：防止重复点击
        _navigateTo(url) {
            if (this.isClick) {
                this.isClick = false;
                this.isLogin(() => {
                    uni.navigateTo({
                        url: url,
                    });
                });
                setTimeout(function () {
                    this.isClick = true;
                }, 800);
            }
        },
        //跳转：多功能验证
        toUrl(url, is_login = false) {
            //如果是diy
            if (this.is_diy) {
                if (is_login) {
                    this.isLogin(() => {
                      
                        this.toUrl(url, false);
                    });
                    return false;
                }
                if (url.includes("tabBar")) {
                    uni.switchTab({
                        url,
                    });
                } else {
                    if (url.includes("http")) {
                        let query = { url };
                        uni.navigateTo({
                            url:
                                "/pagesC/webView/webView?query=" +
                                encodeURIComponent(JSON.stringify(query)),
                        });
                    } else {
                        uni.navigateTo({
                            url,
                        });
                    }
                }
                return false;
            }
            //如果是tabBar
            if (url.includes("tabBar")) {
                uni.switchTab({
                    url,
                });
            } else {
                uni.navigateTo({
                    url,
                });
            }
        },
        //跳转：金刚区跳转 根据参数判断是否开启登录验证跳转
        navClick(item) {
            // #ifndef MP-WEIXIN
            if (item.name == "来客电商直播" || item.name == "live") {
                uni.showToast({
                    title: this.language.home.openWeChat,
                    icon: "none",
                });
                return;
            }
            // #endif
            if (item.is_login) {
                this.isLogin(() => {
                    if (item.url.includes("tabBar")) {
                        uni.redirectTo({
                            url: item.url,
                        });
                    } else {
                        uni.navigateTo({
                            url: item.url,
                        });
                    }
                });
            } else {
                if (item.url.includes("tabBar")) {
                    uni.redirectTo({
                        url: item.url,
                    });
                } else {
                    // 54861 
                    // if (item.url == "/pagesB/userVip/memberCenter") {
                    //     this.isLogin(() => {
                    //         uni.navigateTo({
                    //             url: item.url,
                    //         });
                    //     });
                    // } else {
                        // this.isLogin(() => {
                        uni.navigateTo({
                            url: item.url,
                        });
                    // });
                    // }
                }
            }
        },
        //跳转：APP
        goApp() {
            location.href = "ZhongBingGouMang://pages/index/home/index";
        },
        //跳转：会员中心
        vipclick() {
            this.isLogin(() => {
                uni.navigateTo({
                    url: "/pagesB/userVip/memberCenter",
                });
            });
        },
        //跳转：续费VIP会员
        vipClick() {
            uni.navigateTo({
                url: "/pagesB/userVip/memberCenter?id=1",
            });
        },
        //跳转：会员VIP商品-查看详情
        navtovip(e) {
            if (this.is_vip == 1) {
                uni.navigateTo({
                    url:
                        "/pagesC/goods/goodsDetailed?pro_id=" +
                        e.id +
                        "&vipprice=" +
                        e.vipPrice +
                        "&price=" +
                        e.price +
                        "&is_hy=" +
                        this.is_vip,
                });
            } else {
                uni.showToast({
                    icon: "none",
                    title: "请先开通会员",
                });
            }
        },
        //跳转：限时秒杀-查看详情
        toSeckill(item) {
            let types = null;
            if (item.secStatus == 2) {
                types = 1;
            } else if (item.secStatus == 4) {
                types = 2;
            } else if (item.secStatus == 5) {
                types = 3;
            } else {
                types = 0;
            }
            uni.navigateTo({
                url:
                    "/pagesB/seckill/seckill_detail?pro_id=" +
                    item.goodsId +
                    "&navType=" +
                    types +
                    "&id=" +
                    item.secGoodsId +
                    "&type=MS",
            });
        },
        //跳转：待付款订单-去支付
        _order() {
            this.isLogin(() => {
                this.isShow_dialog_Pre = false;
                uni.navigateTo({
                    url: "/pagesC/preSale/order/myOrder?status=0",
                });
            });
        },
        //跳转：商品详情
        _goods(id) {
            this.$store.state.pro_id = id;
            uni.navigateTo({
                url: "/pagesC/goods/goodsDetailed?toback=true&pro_id=" + id,
            });
        },
        //跳转：分销商品详情
        _fx_goods(id, p_id) {
            this.$store.state.pro_id = id;
            uni.navigateTo({
                url:
                    "/pagesC/goods/goodsDetailed?isDistribution=true&toback=true&pro_id=" +
                    p_id +
                    "&fx_id=" +
                    id,
            });
        },
        //跳转：积分商品详情页
        _integralGoods(id, goods_id, integral, num) {
            this.isLogin(() => {
                uni.navigateTo({
                    url:
                        "/pagesB/integral/integral_detail?pro_id=" +
                        id +
                        "&goodsId=" +
                        goods_id +
                        "&integral=" +
                        integral +
                        "&num=" +
                        num,
                });
            });
        },
        /*
         ** 跳转结束
         */

        // - - - 分割线 - - -

        /*
         ** 定位开始
         */

        //定位：去选择地区
        toArea() {
            uni.setStorageSync("region", this.region);
            this.toUrl("/pagesB/home/chooseArea");
        },
        //定位：未开启定位时，点击去开启。
        _openSetting() {
            this.opensetting1 = 0;
            //调起客户端小程序设置界面，返回用户设置的操作结果。
            uni.openSetting({
                success(res) {
                },
            });
            //重新获取定位
            //this.getLocationAuthorize();
        },
        //定位：是否在微信浏览器中
        _isWx() {
            let en = window.navigator.userAgent.toLowerCase();
            return en.match(/MicroMessenger/i) == "micromessenger";
        },
        //定位：获取当前地址
        getLocationAuthorize() {
            // 小程序 获取定位
            // #ifdef MP
            uni.authorize({
                scope: "scope.userLocation",
                success: (res) => {
                    //已授权定位后 获取定位信息
                    this.getLocation();
                },
                fail: () => {
                    //未授权定位
                    //提示 请授权位置获取更好的体验
                    this.opensetting1 = 7;
                    this.is_showToast_obj.title = "授权提示";
                    this.is_showToast_obj.content =
                        "请授权位置信息，获取更好的体验";
                    this.is_showToast_obj.button = "取消";
                    this.is_showToast_obj.endButton = "确认";
                    this.is_showToast_obj.type = "primary";
                    this.is_showToast_obj.openType = "openSetting";
                    //请求页面数据 使用默认定位-岳麓区
                    this._isDiy();
                },
            });
            // #endif
            // H5 获取定位 （只有微信浏览器中可以获取定位，普通浏览器没有定位）
            // #ifdef H5
            if (this._isWx()) {
                //如果是微信浏览器
                //获取定位信息 暂时关闭微信浏览器中获取订单信息
                //this.getLocation();
                //请求页面数据 -- 这里和上面同步执行 就算点击拒绝也能使用默认定位请求
                this._isDiy();
            } else {
                //如果是普通浏览器
                //请求页面数据 使用默认定位-岳麓区
                this._isDiy();
            }
            // #endif
        },
        //定位：APP获取定位 微信浏览器获取定位
        getLocation() {
            let me = this;
            uni.getLocation({
                geocode: true,
                success: (res) => {
                    //定位成功 获取经纬度
                    this.longitude = res.longitude;
                    this.latitude = res.latitude;
                    //经纬度 存入缓存 （获取优选店铺接口需要用到）
                    uni.setStorageSync("longitude", this.longitude);
                    uni.setStorageSync("latitude", this.latitude);
                    //请求页面数据
                    this._isDiy();
                },
                fail: (e) => {
                    //未开启定位，获取定位失败
                    //提示 请在系统设置中打开定位服务
                    this.opensetting2 = 3;
                    this.is_showToast_obj.button = "我知道了";
                    this.is_showToast_obj.title =
                        "授权位置失败，请打开定位服务!";
                    //请求页面数据 使用默认定位-岳麓区
                    this._isDiy();
                },
                complete: () => {
                },
            });
        },

        //diy：获取diy首页数据
        async getDiy() {
            //如果获取定位成功 使用当前定位
            if (uni.getStorageSync("longitude")) {
                this.longitude = uni.getStorageSync("longitude");
                this.latitude = uni.getStorageSync("latitude");
            } else {
                //使用默认定位 岳麓区
                uni.setStorageSync("longitude", this.longitude);
                uni.setStorageSync("latitude", this.latitude);
            }
            //如果手动选择地区，则用之
            if (uni.getStorageSync("region")) {
                let region1 = uni.getStorageSync("region");
                if (region1 && region1.district.length > 3) {
                    region1.district = region1.district.substr(0, 2) + "...";
                }
                this.region = region1;
            }

            let data = {
                api: "mch.App.Mch.diy_home_page",
                shop_id:this.shop_id,
                longitude: this.longitude,
                latitude: this.latitude,
            };
            let res = await this.$req.post({ data });
            res = Object.assign(res, res.data);
            uni.stopPullDownRefresh();
            if (res.data.pageJson) {
                this.styleConfig = this.objToArr(JSON.parse(res.data.pageJson));
                let color = this.styleConfig[0].bgColor.color;
                let BgMod  = {}
                for(let item of this.styleConfig ){ 
                    if(item.name == 'homeBg' ){
                        BgMod  = item
                    }
                }
                if(BgMod && Object.keys(BgMod).length){ 
                    this.$emit("getHomeBg", BgMod.bgColor.color);
                }
            }
              
            this.objToArr(res.data).forEach((item) => {
                if (item.name === "homeBg") {
                    this.bgColor = item.bgColor.color;
                }
                if (item.name === "headerSerch") {
                    this.onReachBottomBg = item.bgColor.color;
                }
            });
            if (res.grade && res.grade != 0) {
                this.is_grade = true;
            }
            let region = res.region;
            if (region && region.district.length > 3) {
                region.district = region.district.substr(0, 2) + "...";
            }
            if (!uni.getStorageSync("region")) {
                this.region = region;
            }
        },

        //diy：对象转数组
        objToArr(data) {
            let obj = Object.keys(data);
            let m = obj.map((key) => data[key]);
            return m;
        },

        //diy组件：导航切换
        changePage(page) {
            this.page = page;
        },

        //diy组件：
        onHandleLoadingType(loadingType) {
            this.loadingType = loadingType;
        },
        /*
         ** DIY结束
         */

        // - - - 分割线 - - -

        /*
         ** 渲染数据开始
         */

        //获取页面数据
        getShow() {
            //先请求diy，获取是否使用diy首页
            let data = {
                api: "app.index.hasDiy",
            };
            this.$req.post({ data }).then((res) => {
                this.is_diy = res.data;
                this.loadingType = 0;
                //获取定位定位 -》 普通浏览器使用默认定位  -》 判断是否是diy首页，并初始化显示首页内容
                this.LaiKeTuiCommon.getUrlFirst(this.getLocationAuthorize);
            });
        },

        //获取数据判断：判断是否是diy首页，并初始化显示对应首页内容
        _isDiy() {
            //用户是否是会员
            this.getHasGrade();
            this.getDiy();
        },

        //初始化数据
        async _axios() {
            //如果获取定位成功 使用当前定位
            if (uni.getStorageSync("longitude")) {
                this.longitude = uni.getStorageSync("longitude");
                this.latitude = uni.getStorageSync("latitude");
            } else {
                //使用默认定位 岳麓区
                uni.setStorageSync("longitude", this.longitude);
                uni.setStorageSync("latitude", this.latitude);
            }
            //如果手动选择地区，则用之
            if (uni.getStorageSync("region")) {
                let region1 = uni.getStorageSync("region");
                if (
                    region1 &&
                    region1.district &&
                    region1.district.length > 3
                ) {
                    region1.district = region1.district.substr(0, 2) + "...";
                }
                this.region = region1;
            }
            // 首页数据
            var data = {
                api: "app.index.index",
                longitude: this.longitude,
                latitude: this.latitude,
            };
            this.$req
                .post({
                    data,
                    xhrFields: {
                        withCredentials: true,
                    },
                })
                .then((res) => {
                    uni.stopPullDownRefresh();
                    let {
                        data: {
                            banner,
                            Marketing_list,
                            list2,
                            xxnum,
                            r_mch,
                            is_sign_status,
                            sign_status,
                            grade,
                            region,
                            nav_list,
                        },
                    } = res;
                    this.vippupo = res.data.grade_remind;
                    this.appTitle = res.data.appTitle?res.data.appTitle:"";
                    this.memberPlugin = res.data.memberPlugin;
                    this.livingStatus=res.data.livingStatus
                    if (grade && grade != 0) {
                        this.is_grade = true;
                    } else {
                        this.is_grade = false;
                    }
                    if (res.data.distribution_list) {
                        this.distribution_list =
                            res.data.distribution_list.list;
                        this.disTitle = res.data.distribution_list.title;
                        this.fxBg = res.data.distribution_list.ad_image;
                        this.subtraction_list = res.data.subtraction_list;
                    }
                    this.Marketing_list = Marketing_list;
                    this.Marketing_list.forEach((item, index) => {
                        if (item.activity_type == 0) {
                            //去重
                            let is_true = this.Marketing_list_zdy.every(
                                (it, idx) => {
                                    return it.id != item.id;
                                }
                            );
                            if (is_true) {
                                // 自定义活动
                                this.Marketing_list_zdy.push(item);
                            }
                        } else if (
                            item.activity_type == 1 &&
                            item.plug_type == 7
                        ) {
                            //积分商品活动
                            this.Marketing_list_jf = item;
                        }
                    });
                    if (
                        region &&
                        region.district &&
                        region.district.length > 3
                    ) {
                        region.district = region.district.substr(0, 2) + "...";
                    }
                    if (!uni.getStorageSync("region")) {
                        this.region = region;
                    }
                    if (
                        is_sign_status == 1 &&
                        sign_status == 1 &&
                        !uni.getStorageSync("signFlag")
                    ) { 
                        uni.setStorageSync("signFlag", true);
                    }
                    this.nav_list = nav_list;
					
					// 小程序端需要隐藏直播功能入口，但h5要保留
					// #ifdef MP-WEIXIN
						 this.nav_list = nav_list.filter(v=>v.name !='直播')
					// #endif 
					
                    this.banner = banner;
                    list2.filter((item) => {
                        if (item.list) {
                            item.list.filter((it) => {
                                it.vip_price = Number(it.vip_price).toFixed(2);
                                it.vip_yprice = Number(it.vip_yprice).toFixed(
                                    2
                                );
                            });
                        }
                    });
                    this.bottom_list = list2;
                    //影响了从其他页面回到首页对loadingType赋值的操作 所以注释掉
                    //>=10条可以继续上拉
                    // if (list2.length < 10) {
                    //     this.loadingType = 2;
                    // } else {
                    //     this.loadingType = 0;
                    // }
                    //限制消息数超过99时显示99+
                    this.xxnum = xxnum > 99 ? "99+" : Number(xxnum);
                    //推荐商家
                    this.r_mch = r_mch;
                    //
                    if (Marketing_list) {
                        for (let item of Marketing_list.values()) {
                            if (
                                item.activity_type == 1 &&
                                item.plug_type == 8
                            ) {
                                if (
                                    item.list.current_time &&
                                    item.list.current_time[0]
                                ) {
                                    this.setCountDown(
                                        item.list.current_time[0]
                                    );
                                }
                                break;
                            }
                        }
                    }
                })
                .catch((error) => {
                    uni.stopPullDownRefresh();
                });
        },
        /*
         ** 渲染数据结束
         */

        // - - - 分割线 - - -

        /*
         ** 插件开始
         */

        //VIP：续费
        xzbtnClick() {
            this.xzbtn = !this.xzbtn;
        },
        //VIP：续费
        shutClick() {
            this.vippupo = false;
            if (!this.xzbtn) {
                uni.removeStorageSync("isxufei");
                return;
            } else {
                var data = {
                   
                    api:"plugin.member.AppMember.CloseFrame"
                };
                this.$req
                    .post({
                        data,
                    })
                    .then((res) => {
                    });
            }
        },
        //VIP：用户是否是会员
        async getHasGrade() {
            let data = {
                api: "app.index.get_membership_status",
            };
            let res = await this.$req.post({ data });
            this.getGrade = true;
            if (res.code == 200) {
                this.hasGrade = res.data.membership_status;
                uni.setStorageSync("hasGrade", this.hasGrade);
            }
        },
        //限时秒杀：秒杀商品
        getSeckill() {
            let data = {
                api:'plugin.sec.AppSec.goodsList',
                pageNo: 1,
                pageSize: 3,
            };
            this.$req.post({ data }).then((res) => {
                let { data } = res;
                if (res.code == 200) {
                    this.secList = res.data.list;
                    this.secTitle = res.data.title;
                }
            });
        },
        //会员中心：会员商品
        getHasGrades() {
            let data = {
               
                api:"plugin.member.AppMember.MemberCenter"
            };
            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    if (res.code == 200) {
                        this.goodsArr = res.data.memberPro;
                        this.is_vip = res.data.userInfo.grade;
                        this.memberEquity = res.data.memberEquity;
                    }
                });
        },
        //新品上市：商品列表
        getNewImg() {
            let data = {
                api: "app.index.new_arrival",
                page: 1,
                pageSize: 2,
            };
            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    if (res.data.list) {
                        this.newImgList = res.data.list.map((item) => {
                            return item.imgurl;
                        });
                    }
                });
        },
        //好物优选：商品列表
        getRecommendImg() {
            let data = {
                api: "app.index.recommend",
                page: 1,
                pageSize: 2,
            };
            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    if (res.data.list) {
                        this.recommendImgList = res.data.list.map((item) => {
                            return item.imgurl;
                        });
                    }
                });
        },
        /*
         ** 插件结束
         */

        // - - - 分割线 - - -

        /*
         ** APP开始
         */

        //APP：
        widgetinfo() {
            var me = this;
            plus.runtime.getProperty(plus.runtime.appid, function (widgetInfo) {
                uni.request({
                    url:
                        uni.getStorageSync("endurl") +
                        "widgetinfo/update.php?store_id=" +
                        me.LaiKeTuiCommon.LKT_STORE_ID,
                    data: {
                        version: widgetInfo.version,
                        name: widgetInfo.name,
                    },
                    success: (result) => {
                        me.newapp = result.data;
                        if (uni.getSystemInfoSync().platform === "android") {
                            me.newapp.url = me.newapp.android_url;
                        } else {
                            me.newapp.url = me.newapp.ios_url;
                        }
                        if (me.newapp.status == 1) {
                            me.widgetinfoo = true;
                            uni.hideTabBar();
                        }
                    },
                });
            });
        },
        //APP：更新
        close_mask() {
            uni.showTabBar();
            this.widgetinfoo = false;
        },
        //APP：更新
        update() {
            this.widgetinfoo = false;
            uni.showTabBar();
            plus.runtime.openURL(this.newapp.url, function (res) {
                uni.showToast({
                    title: this.language.home.updateFail,
                    duration: 1000,
                    icon: "none",
                });
            });
        },
        /*
         ** APP结束
         */

        // - - - 分割线 - - -

        /*
         ** 倒计时开始
         */

        //倒计时：年-月-人 时：分：秒
        setCountDown(item) {
            clearInterval(this.Intervalid);
            let year = new Date().getFullYear();
            let month = new Date().getMonth() + 1;
            let dates = new Date().getDate();
            let endtime = "";
            if (item.type == 2) {
                endtime = `${year}-${month}-${dates} ${item.starttime}`;
            } else {
                endtime = `${year}-${month}-${dates} ${item.endtime}`;
            }
            this.timeFun(endtime, item);
            this.Intervalid = setInterval(() => {
                this.timeFun(endtime, item);
            }, 1000);
        },
        //倒计时：定时事件
        timeFun(endtime, item) {
            let result = getTimeDiff(endtime);
            this.time.hours = result.hours;
            this.time.minutes = result.minutes;
            if (result.seconds < 0 || result.hours < 0 || result.minutes < 0) {
                clearInterval(this.Intervalid);
                this._axios();
                return;
            }
            this.time.seconds = result.seconds;
        },
        /*
         ** 倒计时结束
         */
        
        //分享重定向 - 小程序二维码分享
        _wxEwmShare(scene){
            uni.showLoading({title: '加载中...'})
            let data = {
                api: 'app.getcode.getCodeParameter',
                key: scene
            };
            this.$req.post({ data }).then(res => {
                uni.hideLoading()
                if(res.code == 200 && res.data.parameter) {
                    let parameter = res.data.parameter.split("&")
                    let share = parameter[0].split("=")[1]
                    //重定向页面 并处理pages中的 .- 以 / 替代，在分享时替换了的，现在切换回去.
                    let pages = parameter[1].split("=")[1].replace(/\.\-/g,'/')
                    //重定向页面携带的参数
                    let states = '?'
                    parameter.forEach((item, index)=>{
                        if(index>1){
                            states = states + item + '&'
                        }
                    })
                    states = states.slice(0, -1)
                    //分享重定向url
                    let sharUrl = '/' + pages + states
                    uni.redirectTo({ url: sharUrl });
                } else {
                    uni.showToast({
                        title: res.message,
                        icon: 'none'
                    });
                }
            });
        },
        
        //分享重定向
        _share(homeOption){
            let share = homeOption.share
            //重定向页面 并处理pages中的 .- 以 / 替代，在分享时替换了的，现在切换回去.
            let pages = homeOption.pages.replace(/\.\-/g,'/')
            //重定向页面携带的参数
            let states = '?'
            for (let i in homeOption) {
                if(i != 'share' && i != 'pages'){
                    states = states + i + "=" + homeOption[i] + "&";
                }
            }
            states = states.slice(0, -1)
            //分享重定向url
            let sharUrl = '/' + pages + states
            uni.redirectTo({ url: sharUrl });
        }
        
    },
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url("../../static/css/tabBar/home.less");
@media screen and (min-width: 600px) {
    .zhiding {
        left: calc(50% + 270rpx);
    }
}
</style>
