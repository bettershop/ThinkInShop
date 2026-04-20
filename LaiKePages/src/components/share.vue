<template>
    <div class="share">
        <!-- 登录验证 --> 
        <!-- H5分享弹框 -->
        <div class="mask" v-if="h5ShareMask" @tap="_closeAllMask">
            <!-- 邀请链接的弹框 -->
            <div class="shareMask" @tap.stop>
                <!-- 分享至 -->
                <div class="shareMaskTitle">{{ language.goods.goodsDet.Share_with }}</div>
                <!-- 可分享功能：二维码分享/链接分享 -->
                <div class="sharepyq">
                    <div class="shareIcon" @tap="showSaveEWM('h5')">
                        <img class="h5-img" :src="erm_img"/>
                        <!-- 二维码分享 -->
                        <p>{{ language.goods.goodsDet.QR }}</p>
                    </div>
                    <div class="shareIcon" @tap="copyLink">
                        <img class="h5-img" :src="copy_link"/>
                        <!-- 链接分享 -->
                        <p>{{ language.goods.goodsDet.Copy_link }}</p>
                    </div>
                    <div class="clearfix"></div>
                </div>
                <!-- 间隔线 -->
                <div class="line"></div>
                <!-- 取消按钮 -->
                <div class="shareEnd" @tap="_closeAllMask">{{language.goods.goodsDet.cancel}}</div>
            </div>
        </div>
        
        <!-- wx分享弹框 -->
        <div class="mask" v-if="wxShareMask" @tap="_closeAllMask">
            <!-- 邀请链接的弹框 -->
            <div class="shareMask" @tap.stop>
                <!-- 分享至 -->
                <div class="shareMaskTitle">{{ language.goods.goodsDet.Share_with }}</div>
                <!-- 可分享功能：微信好友/二维码分享 -->
                <div class="sharepyq">
                    <!-- 微信好友 -->
                    <div class="shareIcon">
                        <button class="share_btn" open-type="share">
                            <img :src="wx_img"/>
                            <p class="p">{{ language.goods.goodsDet.Wechat_friends }}</p>
                        </button>
                    </div>
                    <!-- 二维码分享 -->
                    <div class="shareIcon" @tap="showSaveEWM('wx')">
                        <img :src="erm_img"/>
                        <p>{{ language.goods.goodsDet.QR }}</p>
                    </div>
                    <div class="clearfix"></div>
                </div>
                <!-- 间隔线 -->
                <div class="line"></div>
                <!-- 取消按钮 -->
                <div class="shareEnd" @tap="_closeAllMask">{{language.goods.goodsDet.cancel}}</div>

            </div>
        </div>
        
        <!-- APP分享弹框 -->
        <div class="mask" v-if="appShareMask" @tap="_closeAllMask">
            <!-- 邀请链接的弹框 -->
            <div class="shareMask" @tap.stop>
                <!-- 分享至 -->
                <div class="shareMaskTitle">{{ language.goods.goodsDet.Share_with }}</div>
                <div class="sharepyq">
                    <!-- 微信分享 -->
                    <div class="shareIcon" style="width: 33.33% !important;" @tap="_invite('微信')">
                        <img class="h5-img" :src="wx_img"/>
                        <p>{{language.toasts.goodsDetailed.shareWay[0]}}</p>
                    </div>
                    <!-- 朋友圈分享 -->
                    <div class="shareIcon" style="width: 33.33% !important;" @tap="_invite('朋友圈')">
                        <img class="h5-img" :src="wxpyc_img"/>
                        <p>{{language.toasts.goodsDetailed.shareWay[1]}}</p>
                    </div>
                    <!-- 二维码分享 -->
                    <div class="shareIcon" style="width: 33.33% !important;" @tap="_invite('二维码分享')">
                        <img class="h5-img" :src="erm_img"/>
                        <p>{{language.toasts.goodsDetailed.shareWay[2]}}</p>
                    </div>
                    <div class="clearfix"></div>
                </div>
                <!-- 间隔线 -->
                <div class="line"></div>
                <!-- 取消按钮 -->
                <div class="shareEnd" @tap="_closeAllMask">{{language.goods.goodsDet.cancel}}</div>
            </div>
        </div>
        
        <!-- 海报生成器 -->
        <!-- #ifdef H5 -->
        <view
            v-if="posterInfo.status"
            class="my-canvas-box"
            @touchmove.stop.prevent
            @click.stop="posterInfo.status = false">
                <canvas 
                    class="my-canvas" 
                    canvas-id="myCanvas" 
                    :style="{height: share.mch.type?'942rpx':''}"
                    @click.stop="posterInfo.status = true">
                </canvas>
                <view class="canvas-tip" @click.stop="savaImage">
                    <img :src="download" alt="">{{ language.groupOrder.xiazai }}
                </view>
        </view>
        <!-- #endif -->
        <!-- #ifndef H5 -->
        <view 
            v-if="posterInfo.status"
            class="my-canvas-box" 
            @touchmove.stop.prevent 
            @click.stop="posterInfo.status = false">
                <canvas 
                    class="my-canvas" 
                    canvas-id="myCanvas"
                    :style="{height: share.mch.type?'942rpx':''}"
                    @longpress.stop="savaImage" 
                    @click.stop="posterInfo.status = true">
                </canvas>
                <view class="canvas-tip" @click.stop="savaImage">
                    {{ language.groupOrder.xiazai }}
                </view>
        </view>
        <!-- #endif -->
        
        <!-- 弹窗  复制成功 -->
        <showToast
            :is_showToast="is_showToast" 
            :is_showToast_obj="is_showToast_obj">
        </showToast>
    </div>
</template>

<script>
import showToast from "@/components/aComponents/showToast.vue"
import {copyText} from "@/common/util";
export default {
    data() {
        return {
            //弹窗
            is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
            is_showToast_obj: {}, //imgUrl提示图标  title提示文字
            //分享弹窗-是否显示
            wxShareMask: false, //是否显示-小程序分享弹窗
            appShareMask: false,//是否显示-app分享弹窗
            h5ShareMask: false, //是否显示-H5分享弹窗
            //用来控制canvas遮罩层的显示与隐藏
            posterInfo: { status: false }, 
            //当前组件-接口中获取
            mchName:'',         //店铺名称
            mchImgUrl:'',       //店铺头像
            mchImgPoster:'',    //店铺宣传图
            userName: '',       //用户名称
            userHeadUrl: '',    //用户头像
            codeUrl: '',        //二维码url
            watermarkName: '扫我查看详情',        //接口有返回数据则显示，否则显示默认
            watermarkUrl: 'www.laiketui.com',   //接口有返回数据则显示，否则显示默认
            //canvas上下文
            context: '',
            isCanvas:true,
            sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',//图标-成功提示
            wx_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wechat.png',//图标-微信
            wxpyc_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/fdc.png',//图标-微信朋友圈
            erm_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/ewmShare.png',//图标-二维码
            copy_link: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/copy_link.png',//图标-复制链接
            download: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/goodsDetailed_download.png',//图标-下载
            jtImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/canvsJT.png',//图标-箭头
        }
    },
    props: {
        share: {
            type: Object,
            default: () => {
                mch: {
                    type: false    //是否店铺分享
                    mchId: ''      //店铺id
                }
                goods: {
                    title: ''      //分享-商品标题
                    imgUrl: ''     //分享-商品图片
                    price: ''      //分享-商品金额
                }
                shareHref: ''      //分享-链接
                shareType: ''      //分享-类型：普通商品（shareType: 'gm'） 分销商品（shareType: 'fx'） 竞拍商品（shareType: 'jp'） 直播商品（shareType: 'zb'） 
            },
            required: true
        },
    },
    computed: {
        share_href(){
            //不用传网址，后台会从管理系统中 平台-》系统设置-》H5地址中获取 并重新拼接
            //let h5_url = uni.getStorageSync('h5_url')
            
            //不跳中间页，跳会存在空白页面
            //let path = '/pagesB/index/share?'
            //直接跳首页，然后再跳转分享页面
            let path = '/pages/shell/shell?'
            //父组件返回参数。 包含-》 share：用于首页重定向判断；pages：在首页重定向分享页；states：重定向分享页携带的参数；
            let shareHref = this.share.shareHref
            
            return path + shareHref
        }
    },
    components: {
        showToast,
    },
    mounted(){
       this.decideURl()
    },
    methods: {
        // 判断 本地地址 和 配置地址 是否一致, 并缓存 结果
        decideURl(){ 
            // #ifdef H5
                
                if(uni.getStorageSync('isCrossDomain')){
                    return
                }
            
                //跨域处理20240603
                let isCrossDomain = false
                
                //获取接口请求地址 及 H5服务器部署地址，如果不一样则要进行跨域处理
                let JKurl = this.LaiKeTuiCommon.LKT_ROOT_URL
                let H5url = 'https://' + window.location.host 
                if(JKurl != H5url){
                    isCrossDomain = true
                } else if(H5url.indexOf('localhost') != -1){
                    isCrossDomain = true
                }
                if(isCrossDomain){
                    uni.setStorageSync('isCrossDomain', {name:'接口请求地址与H5服务器部署地址是否不一致',value:isCrossDomain})
                }
            // #endif
        },
        //显示分享弹窗
        showShareMask() {
            let me = this
            //初始化分享数据
            this.getUserInfo()
            //区分终端类型： H5 || 小程序 || APP分享弹窗
            // #ifdef H5
                //终端：H5弹窗
                setTimeout(() => {
                    me.h5ShareMask = true;
                }, 300)
            // #endif
            // #ifdef MP-WEIXIN || MP-ALIPAY || MP-TOUTIAO || MP-BAIDU
                //终端：小程序弹窗
                setTimeout(function() {
                    me.wxShareMask = true;
                }, 500)
            // #endif
            // #ifdef APP
                //终端：APP弹窗
                setTimeout(function() {
                    me.appShareMask = true;
                }, 500)
            // #endif
        },
        //隐藏分享弹窗
        _closeAllMask() {
            this.h5ShareMask = false;//关闭h5的弹窗
            this.wxShareMask = false;//关闭微信的弹窗
            this.appShareMask = false;//关闭APP的弹窗
        },
        /**
         * 初始化分享 - 获取二维码/用户名称/用户头像/水印
         */
        getUserInfo() {
            console.log('分享二维码地址--》',this.share_href);
            //这里如果传入参数为空，请在父组件showShareMask外加一个this.$nextTick(()=>{})
            let data = {
               api: 'app.getCode.rqCodeInfo',
               path: this.share_href
            }
            this.$req.post({data}).then(res => {
                if(res.code != 200){
                    uni.showToast({
                        title: '获取用户头像/名称/二维码接口显示：' + res.message,
                        icon: 'none'
                    })
                }
                //JAVA和PHP返回的值不一样，区分一下；后期后台处理了再统一
                if(res.data?.qrCode.indexOf('http') >= 0){
                    this.codeUrl = res.data.qrCode
                } else {
                    this.codeUrl = this.LaiKeTuiCommon.LKT_ROOT_URL + '/images/' + res.data?.qrCode
                }
                this.userName = res.data?.userName
                this.userHeadUrl = res.data?.userHeadUrl
                //这里需要接口返回对应参数
                this.watermarkName = res.data.watermarkName
                this.watermarkUrl = res.data.watermarkUrl
                console.log('用户头像---》',this.userHeadUrl)
                console.log('二维码 ---》',this.codeUrl)
            })
        },
        /**
         * 点击二维码分享
         * 1. 如果是普通分享直接开始画海报；
         * 2. 如果是【店铺】需要获取额外参数：店铺名称、头像、宣传图
         * 3. 如果是【竞拍】需要获取额外参数：专场名称、logo
         * @param {Object} string 终端类型：h5 || wx || app
         */
        showSaveEWM(string) {
            this.isCanvas = false
            //是否店铺分享
            if (this.share.mch.type) {
                switch (this.share.shareType) {
                    case "jp":
                        console.log('竞拍专场分享--》专场id',this.share.mch.mchId);
                        //获取额外参数-专场信息
                        this.getJpInfo()
                        break;
                    default:
                        console.log('店铺分享--》店铺id',this.share.mch.mchId);
                        //获取额外参数-店铺信息
                        this.getMchInfo()
                }
            } else {
                console.log('商品分享--》商品名称',this.share.goods.title);
                //直接生成海报
                this.generatePoster()
            }
        },
        /**
         *  获取店铺信息
         */
        getMchInfo(){
            let data = {
                api:'mch.App.Mch.Store_homepage',
                type: '1',
                shop_id: this.share.mch.mchId,
                longitude: uni.getStorageSync('longitude')||'112.951227',
                latitude: uni.getStorageSync('latitude')||'28.227965',
            }
            this.$req.post({data}).then(res=>{
                if(res.code == 200){
                    //拿到 店铺名称、头像、宣传图
                    this.mchName = res.data.shop_name
                    this.mchImgUrl  = res.data.shop_logo
                    this.mchImgPoster = res.data.poster_img?res.data.poster_img:res.data.shop_logo
                    //生成海报
                    this.generatePoster(this.mchName, this.mchImgUrl, this.mchImgPoster)
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 2000,
                        icon: 'none'
                    });
                }
            })
        },
        /**
         *  获取专场信息
         */
        getJpInfo(){
            let data = {
                api: 'plugin.auction.AppAuction.specialDetail',
                specialId: this.share.mch.mchId,
            }
            this.$req.post({data}).then(res=>{
                if(res.code == 200){
                    //拿到 专场名称和logo
                    let name = res.data.specialName
                    let imgUrl = res.data.specialImg
                    //生成海报
                    this.generatePoster(name, imgUrl, imgUrl)
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 2000,
                        icon: 'none'
                    });
                }
            }) 
        },
        
        /**
         * Canvs 开始
         */
        /**
         * 画海报
         * @param {Object} name 分享名称    
         * @param {Object} imgUrl 分享头像
         * @param {Object} imgPoster 分享主图
         */
    // 图片压缩工具函数（组件内同级方法）
    compressImage(imgPath, maxWidth = 800, maxHeight = 800, quality = 0.8) {
      return new Promise((resolve, reject) => {
        const img = new Image();
        img.crossOrigin = 'anonymous'; // 处理跨域（需服务器支持）
        
        img.onload = () => {
          let width = img.width;
          let height = img.height;
          
          // 等比缩放（避免拉伸）
          if (width > maxWidth) {
            const ratio = maxWidth / width;
            width = maxWidth;
            height = height * ratio;
          }
          if (height > maxHeight) {
            const ratio = maxHeight / height;
            height = maxHeight;
            width = width * ratio;
          }
          
          // 临时Canvas压缩
          const canvas = document.createElement('canvas');
          const ctx = canvas.getContext('2d');
          canvas.width = width;
          canvas.height = height;
          ctx.drawImage(img, 0, 0, width, height);
          
          // 转换为dataURL（自动适配png/jpg）
          const mimeType = imgPath.includes('.png') ? 'image/png' : 'image/jpeg';
          const compressedDataUrl = canvas.toDataURL(mimeType, quality);
          resolve(compressedDataUrl);
        };
        
        img.onerror = (err) => {
          console.error('图片压缩加载失败', err);
          reject(err);
        };
        
        img.src = imgPath;
      });
    },
    
         // 完整的海报生成方法（已集成压缩逻辑）
         async generatePoster(name, imgUrl, imgPoster) {
           let _this = this;
           this.context = uni.createCanvasContext('myCanvas', this);
           
           // 填充白色背景
           _this.context.setFillStyle('#F4F5F6');
           _this.context.fillRect(0, 0, 600, 900);
           
           // 获取屏幕宽度并适配
           let device = await uni.getSystemInfo();
           let width = device[1].windowWidth;
           if (width > 600) width = 375; // H5端适配
           let wid = width / 375; // 缩放比例
           
           // 显示加载动画
           uni.showLoading({ title: '海报生成中...' });
           
           // 初始化图片地址
           let qrImg = this.codeUrl;
           let userHeadUrl = this.userHeadUrl;
           let proImg = this.share.goods.imgUrl;
           let jtImg = this.jtImg;
           
           // 店铺/竞拍专场分享（特殊逻辑）
           if (this.share.mch.type) {
             proImg = imgPoster; // 店铺宣传图替代商品图
             
             // 1. 绘制用户头像（压缩：小尺寸高画质）
             let b = new Date().getTime();
             userHeadUrl = userHeadUrl + '?b=' + b;
             uni.downloadFile({
               url: userHeadUrl,
               success: async function(res) {
                 if (res.statusCode !== 200) return;
                 try {
                   const compressedHead = await _this.compressImage(res.tempFilePath, 200, 200, 0.8);
                   _this.context.save();
                   _this.context.beginPath();
                   _this.context.arc(46, 46, 30, 0, 2 * Math.PI);
                   _this.context.clip();
                   _this.context.drawImage(compressedHead, 15*wid, 15*wid, 60*wid, 60*wid);
                   _this.context.restore();
                   _this.context.draw(true);
                 } catch (err) {
                   // 压缩失败降级用原图
                   _this.context.drawImage(res.tempFilePath, 15*wid, 15*wid, 60*wid, 60*wid);
                   _this.context.draw(true);
                 }
               }
             });
             
             // 绘制用户昵称+副标题
             _this.context.setFontSize(16);
             _this.context.setFillStyle('#000000');
             _this.context.fillText(`${_this.userName}｜${_this.language.goods.goodsDet.tj}`, 90*wid, 42*wid);
             _this.context.setFontSize(10);
             _this.context.setFillStyle('#999999');
             _this.context.fillText(_this.language.goods.goodsDet.zdgmhw, 90*wid, 62*wid);
             
             // 2. 绘制店铺宣传图（压缩：中等尺寸平衡画质）
             let a = new Date().getTime();
             proImg = proImg + '?a=' + a;
             uni.downloadFile({
               url: proImg,
               success: async function(res) {
                 if (res.statusCode !== 200) return;
                 try {
                   const compressedPro = await _this.compressImage(res.tempFilePath, 600, 600, 0.7);
                   _this.context.save();
                   _this.drawRoundedImage(_this.context, 8*wid, 16*wid, 92*wid, 268*wid, 268*wid, true, false, false, true);
                   _this.context.drawImage(compressedPro, 16*wid, 92*wid, 268*wid, 268*wid);
                   _this.context.restore();
                   _this.context.draw(true);
                 } catch (err) {
                   _this.context.drawImage(res.tempFilePath, 16*wid, 92*wid, 268*wid, 268*wid);
                   _this.context.draw(true);
                 }
               }
             });
             
             // 绘制详情背景
             this.drawRoundedRectangle(_this.context, 16*wid, 350*wid, 268*wid, 108*wid, 10, '#ffffff');
             
             // 3. 绘制店铺logo（压缩：小尺寸高画质）
             let c = new Date().getTime();
             imgUrl = imgUrl + '?c=' + c;
             uni.downloadFile({
               url: imgUrl,
               success: async function(res) {
                 if (res.statusCode !== 200) return;
                 try {
                   const compressedLogo = await _this.compressImage(res.tempFilePath, 150, 150, 0.8);
                   _this.context.save();
                   _this.drawRoundedImage(_this.context, 4*wid, 24*wid, 385*wid, 48*wid, 48*wid);
                   _this.context.drawImage(compressedLogo, 24*wid, 385*wid, 48*wid, 48*wid);
                   _this.context.restore();
                   _this.context.draw(true);
                 } catch (err) {
                   _this.context.drawImage(res.tempFilePath, 24*wid, 385*wid, 48*wid, 48*wid);
                   _this.context.draw(true);
                 }
               }
             });
             
             // 绘制店铺名称+扫码文字
             this.drawText(`${name}`, 80*wid, 382*wid, _this.context, 8, 0);
             if (this.share.shareType != 'jp') {
               _this.context.setFontSize(12);
               _this.context.setFillStyle('#888888');
               _this.context.fillText(_this.language.goods.goodsDet.smjdgg, 80*wid, 425*wid);
             }
             
             // 4. 绘制箭头图标（压缩：小图标低质量）
             if (uni.getStorageSync('isCrossDomain')) {
               jtImg = jtImg.split("images")[1];
               jtImg = '/api' + '/pic/images' + jtImg;
             }
             uni.downloadFile({
               url: jtImg,
               success: async function(res) {
                 if (res.statusCode !== 200) return;
                 try {
                   const compressedJt = await _this.compressImage(res.tempFilePath, 100, 100, 0.6);
                   _this.context.save();
                   _this.context.drawImage(compressedJt, 160*wid, 416*wid, 14*wid, 10*wid);
                   _this.context.restore();
                   _this.context.draw(true);
                 } catch (err) {
                   _this.context.drawImage(res.tempFilePath, 160*wid, 416*wid, 14*wid, 10*wid);
                   _this.context.draw(true);
                 }
               }
             });
             
             // 5. 绘制二维码（压缩：高画质保清晰）
             if (uni.getStorageSync('isCrossDomain')) {
               qrImg = qrImg.split("images")[1];
               qrImg = '/api' + '/images' + qrImg;
             }
             uni.downloadFile({
               url: qrImg,
               success: async function(res) {
                 if (res.statusCode !== 200) return;
                 try {
                   const compressedQr = res.tempFilePath;
                   _this.context.save();
                   // #ifdef MP
                   _this.context.drawImage(compressedQr, 193*wid, 365*wid, 80*wid, 80*wid);
                   // #endif
                   // #ifndef MP
                   _this.context.drawImage(compressedQr, 193*wid, 365*wid, 90*wid, 90*wid);
                   // #endif
                   _this.context.restore();
                   _this.context.draw(true);
                 } catch (err) {
                   // 二维码压缩失败强制用原图
                   _this.context.drawImage(res.tempFilePath, 193*wid, 365*wid, 90*wid, 90*wid);
                   _this.context.draw(true);
                 }
               }
             });
             
           } else {
             // 普通商品/直播分享（原有逻辑+压缩）
             
             // 1. 绘制用户头像
             let b = new Date().getTime();
             userHeadUrl = userHeadUrl + '?b=' + b;
             uni.downloadFile({
               url: userHeadUrl,
               success: async function(res) {
                 if (res.statusCode !== 200) return;
                 try {
                   const compressedHead = await _this.compressImage(res.tempFilePath, 200, 200, 0.8);
                   _this.context.save();
                   _this.context.beginPath();
                   _this.context.arc(46, 46, 30, 0, 2 * Math.PI);
                   _this.context.clip();
                   _this.context.drawImage(compressedHead, 15*wid, 15*wid, 60*wid, 60*wid);
                   _this.context.restore();
                   _this.context.draw(true);
                 } catch (err) {
                   _this.context.drawImage(res.tempFilePath, 15*wid, 15*wid, 60*wid, 60*wid);
                   _this.context.draw(true);
                 }
               }
             });
             
             // 绘制用户昵称+副标题
             _this.context.setFontSize(16);
             _this.context.setFillStyle('#000000');
             if (_this.share.shareType == 'zb') {
               _this.context.fillText(`${_this.userName}｜分享`, 90*wid, 50*wid);
             } else {
               _this.context.fillText(`${_this.userName}｜${_this.language.goods.goodsDet.tj}`, 90*wid, 42*wid);
             }
             _this.context.setFontSize(10);
             _this.context.setFillStyle('#999999');
             _this.share.shareType != 'zb' && _this.context.fillText(_this.language.goods.goodsDet.zdgmhw, 90*wid, 62*wid);
             
             // 2. 绘制商品图
             let a = new Date().getTime();
             proImg = proImg + '?a=' + a;
             uni.downloadFile({
               url: proImg,
               success: async function(res) {
                 if (res.statusCode !== 200) return;
                 try {
                   const compressedPro = await _this.compressImage(res.tempFilePath, 600, 600, 0.7);
                   _this.context.save();
                   _this.drawRoundedImage(_this.context, 8*wid, 16*wid, 92*wid, 268*wid, 268*wid);
                   _this.context.drawImage(compressedPro, 16*wid, 92*wid, 268*wid, 268*wid);
                   _this.context.restore();
                   _this.context.draw(true);
                 } catch (err) {
                   _this.context.drawImage(res.tempFilePath, 16*wid, 92*wid, 268*wid, 268*wid);
                   _this.context.draw(true);
                 }
               }
             });
             
             // 绘制详情背景+商品信息
             this.drawRoundedRectangle(_this.context, 16*wid, 350*wid, 268*wid, 108*wid, 10, '#ffffff');
             this.drawText(`${_this.share.goods.title}`, 24*wid, 390*wid, _this.context, 12, 1);
             
             // 直播专属绘制
             if (_this.share.shareType == 'zb') {
               _this.context.save();
               _this.context.setFillStyle('#BEBEBE');
               _this.context.setFontSize(12);
               _this.context.fillText(`${_this.share.goods.like}观看`, 24*wid, 427*wid);
               _this.context.restore();
               _this.context.setFontSize(20);
               _this.context.fillText('@', 24*wid, 390*wid);
               _this.context.fillText(_this.share.goods.price, 46*wid, 390*wid);
             } else if (_this.share.goods.price) {
               // 普通商品价格
               _this.context.setFontSize(14);
               _this.context.setFillStyle('#Fa5151');
               // 货币渲染
               const moneyUnit =this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL
               _this.context.fillText(`${moneyUnit}`, 24*wid, 390*wid);
               _this.context.setFontSize(24);
                // moneyUnit.length==1?2:moneyUnit.length 是因为 375px 环境下 金额单位一个长度的显示太小了 
                console.log((19*moneyUnit.length==1?2:moneyUnit.length))
               _this.context.fillText(`${this.LaiKeTuiCommon.formatPrice(Number(_this.share.goods.price)) }`, (19*(moneyUnit.length==1?2:moneyUnit.length)*wid), 390*wid);
             }
             
             // 3. 绘制二维码
             if (uni.getStorageSync('isCrossDomain')) {
               qrImg = qrImg.split("images")[1];
               qrImg = '/api' + '/images' + qrImg;
             }
             uni.downloadFile({
               url: qrImg,
               success: async function(res) {
                 if (res.statusCode !== 200) return;
                 try {
                   const compressedQr = res.tempFilePath;
                   _this.context.save();
                   // #ifdef MP
                   _this.context.drawImage(compressedQr, 210*wid, 363*wid, 68*wid, 68*wid);
                   // #endif
                   // #ifndef MP
                   _this.context.drawImage(compressedQr, 205*wid, 363*wid, 76*wid, 76*wid);
                   // #endif
                   _this.context.restore();
                   _this.context.draw(true);
                 } catch (err) {
                   _this.context.drawImage(res.tempFilePath, 205*wid, 363*wid, 76*wid, 76*wid);
                   _this.context.draw(true);
                 }
               }
             });
             
             // 绘制二维码文字
             _this.context.setFontSize(9);
             _this.context.setFillStyle('#999999');
             _this.context.fillText(
               _this.share.shareType == 'zb' ? '扫码查看直播' : _this.language.goods.goodsDet.swckxq,
               217*wid,
               _this.share.shareType == 'zb' ? 448*wid : 445*wid
             );
             
             // 绘制水印
             _this.context.save();
             _this.context.setFontSize(9);
             _this.context.setFillStyle('#BEBEBE');
             _this.context.setTextAlign('center');
             _this.context.fillText(_this.watermarkName, 150*wid, 474*wid);
             _this.context.setFontSize(8);
             _this.context.fillText(_this.watermarkUrl, 150*wid, 486*wid);
             _this.context.restore();
           }
           
           // 绘制完成后操作
           if (this.isCanvas == false) {
             this.posterInfo.status = true;
           }
           this._closeAllMask(); // 关闭弹窗
           uni.hideLoading(); // 关闭加载
         },
        //绘制圆角-矩形
        drawRoundedRectangle (ctx, x, y, width, height, r, color) {
           ctx.beginPath();
           ctx.moveTo(x + r, y);
           ctx.fillStyle = color;//矩形填充颜色
           ctx.lineTo(x + width - r, y);
           ctx.arc(x + width - r, y + r, r, Math.PI * 1.5, Math.PI * 2);
           ctx.lineTo(x + width, y + height - r);
           ctx.arc(x + width - r, y + height - r, r, 0, Math.PI * 0.5);
           ctx.lineTo(x + r, y + height);
           ctx.arc(x + r, y + height - r, r, Math.PI * 0.5, Math.PI);
           ctx.lineTo(x, y + r);
           ctx.arc(x + r, y + r, r, Math.PI, Math.PI * 1.5);
           ctx.fill();
        },
        /**
         * 绘制圆角-图片
         * @param {Object} ctx canvs上下文
         * @param {Object} r 图片四个角的（border-radius）
         * @param {Object} mrgL 图片左上角的（margin-left）
         * @param {Object} mrgT 图片左上角的（margin-top）
         * @param {Object} width 图片宽度（width）
         * @param {Object} height 图片高度（height）
         * @param {Boolean} right_top 右上角是否圆角（默认值：true）
         * @param {Boolean} right_bottom 右下角是否圆角（默认值：true）
         * @param {Boolean} left_bottom 左下角是否圆角（默认值：true）
         * @param {Boolean} left_top 左上角是否圆角（默认值：true）
         */
        drawRoundedImage(ctx, r, mrgL, mrgT, width, height, right_top=true, right_bottom=true, left_bottom=true, left_top=true){
            //重定向图片宽高
            width = width + mrgL
            height = height + mrgT
            //裁剪图片成圆角图-开始
            ctx.beginPath();//beginPath方法 告诉Context对象 开始绘制一个新的路径，否则接下来绘制的路径会与之前绘制的路径叠加
            ctx.moveTo(r, 0);//绘制起点
            if(right_top){
                ctx.lineTo(width-r, 0);//绘制点
                //arc后第一个圆角完成（右上）
                ctx.arc(width-r, mrgT+r, r, Math.PI*1.5, 0);//创建弧/曲线 (x坐标, y坐标, r半径, 0度起始角, 0度结束角, true逆时针/False顺时针绘图)
            } else {
                ctx.lineTo(width, 0);//绘制点
            }
            if(right_bottom){
                ctx.lineTo(width, height-r);
                //arc后第二个圆角完成（右下）
                ctx.arc(width-r, height-r, r, 0, Math.PI*0.5);
            } else {
                ctx.lineTo(width, height);
            }
            if(left_bottom){
                ctx.lineTo(mrgL+r, height);
                //arc后第三个圆角完成（左下）
                ctx.arc(mrgL+r, height-r, r, Math.PI*0.5, Math.PI);
            } else {
                ctx.lineTo(mrgL, height);
            }
            if(left_top){
                ctx.lineTo(mrgL, mrgT+r);
                //arc后第四个圆角完成（左上）
                ctx.arc(mrgL+r, mrgT+r, r, Math.PI, Math.PI*1.5);
            } else {
                ctx.lineTo(mrgL, mrgT);
            }
            ctx.closePath();//闭合图形成构建区
            ctx.clip();//构建区进行裁剪
            //裁剪图片成圆角图-结束
            
            //创建弧/曲线ctx.arc(x坐标, y坐标, r半径, 起始角, 结束角, true逆时针/False顺时针绘图)
            //注释：ctx.arc中 起始角-结束角 分为四个方向 右上/右下/左下/左上
            //右上 1.5*Math.PI - 0.0*Math.PI --》即为数学中第一象限位置
            //右下 0.0*Math.PI - 0.5*Math.PI --》即为数学中第二象限位置
            //左下 0.5*Math.PI - 1.0*Math.PI --》即为数学中第三象限位置
            //左上 1.0*Math.PI - 1.5*Math.PI --》即为数学中第四象限位置
            //其他 0.0*Math.PI - 2.0*Math.PI --》即为一个圆
        },
        //绘制圆形-图片
        circleImgTwo(ctx, img, x, y, w, h, r) {
            if (w < 2 * r) r = w / 2;
            if (h < 2 * r) r = h / 2;
            ctx.beginPath();
            ctx.moveTo(x + r, y);
            ctx.arcTo(x + w, y, x + w, y + h, r);
            ctx.arcTo(x + w, y + h, x, y + h, 0);
            ctx.arcTo(x, y + h, x, y, 0);
            ctx.arcTo(x, y, x + w, y, r);
            ctx.closePath();
            ctx.strokeStyle = '#FFFFFF'; // 设置绘制圆形边框的颜色
            ctx.stroke();
            ctx.clip();
            ctx.drawImage(img, x, y, w, h);
        },
        //文字超过n后自动换行显示
        drawText(context, x, y, canvas, xNum, yNum) {
            let strArr = [];
            let n = xNum;
            for (let i = 0, l = context.length; i < l / n; i++) {
                let a = context.slice(n * i, n * (i + 1));
                strArr.push(a);
            }
            strArr.forEach((item, index) => {
                //限制只能显示几行文字
                if (index > yNum) {
                    return;
                }
                y += 20;
                canvas.setFontSize(14);
                canvas.setFillStyle('#333333');
                canvas.fillText(item, x, y);
            });
        },
        //保存图片，小程序先获取相册权限
        savaImage() {
            let that = this
            // #ifdef H5
                that.saveSharePic()
            // #endif
            // #ifdef MP
                uni.getSetting({
                    success(res) {
                        if (!res.authSetting['scope.writePhotosAlbum']) {
                            uni.authorize({
                                scope: 'scope.writePhotosAlbum',
                                success(res) {
                                    that.saveSharePic()
                                },
                                fail() {
                                    uni.showModal({
                                        content: '请允许相册权限,拒绝将无法正常使用小程序',
                                        showCancel: false,
                                        success() {
                                            uni.openSetting({
                                                success(settingdata) {
                                                    if (settingdata.authSetting['scope.writePhotosAlbum']) {
                                                    } else {
                                                        console.log('获取权限失败')
                                                    }
                                                }
                                            })
                                        }
                                    })
                                }
                            })
                        } else {
                            that.saveSharePic()
                        }
                    }
                })
            // #endif
            // #ifdef APP
                that.saveSharePic()
            // #endif
        },
        // 长按保存生成的海报
        saveSharePic() {
            let me = this
            uni.showModal({
                title: this.language.goods.goodsDet.tishi,
                content: this.language.goods.goodsDet.qdybc,
                cancelText: this.language.maskM.qx,
                confirmText	: this.language.maskM.qr,
                success: function(res) {
                    if (res.confirm) {
                        // canvas生成图片
                        uni.canvasToTempFilePath({
                            // 这里修改保存的图片格式
                            fileType: 'jpg',
                            canvasId: 'myCanvas',
                            quality: 1,
                            success: function(res) {
                                // 保存到本地
                                console.log('生成canvas图片',res)
                                //#ifdef H5
                                me.saveImg(res.tempFilePath)
                                //#endif
                                //#ifndef H5
                                uni.saveImageToPhotosAlbum({
                                    filePath: res.tempFilePath,
                                    success: function() {
                                        uni.showToast({
                                            title: me.language.share.downloadSuccessful,
                                            icon: 'none',
                                            duration: 3000
                                        });
                                        me.posterInfo.status = false;
                                    },
                                    fail: function() {
                                        uni.showToast({
                                            title: me.language.share.downloadFailure,
                                            icon: 'none',
                                            duration: 3000
                                        });
                                    }
                                });
                                //#endif
                            }
                        },me);
                    }
                }
            });
        },
        /**
         * canvs生成图片后在H5中保存
         * @param {String} downloadUrl base64图片地址
         */
        saveImg(downloadUrl) {
            let getSystemType = uni.getStorageSync('getSystemType')
            //判断终端类型 PC-H5直接下载 其他H5都用长按保存方式
            if(getSystemType && getSystemType.value == 'PC-H5'){
                this.downloadImage(downloadUrl)
            } else {
                this.previewImage(downloadUrl)
            }
            //隐藏加载图标
            this.posterInfo.status = false;
        },
        /**
         * 预览图片
         * @param {String} downloadUrl base64图片地址
         */
        previewImage(downloadUrl){
            /**
             * 预览图片，用户手动保存图片到相册
             * 这种方式最兼容，都没有问题
             */
            let imgsArray = [];
            imgsArray[0] = downloadUrl;
            uni.previewImage({
                current: 0,
                urls: imgsArray
            });
            setTimeout(()=>{
                uni.showToast({
                    title: '长按保存到本地',
                    icon: 'none',
                })
            },100)
        },
        /**
         * 下载图片
         * 安卓机的微信、钉钉等内置浏览器无法下载 需要到外部浏览器下载（iOS可以下载）
         * @param {String} downloadUrl base64图片地址
         */
        downloadImage(downloadUrl){
            /**
             * uni.downloadFile() 转换
             * 先把base64转换成Blob数据，再用js动态创建标签下载图片
             * 【已测小米自带的浏览器可以下载，QQ浏览器、百度浏览器、夸克等可以识别文件类型，但是下载失败】
             */
            uni.downloadFile({
                url: downloadUrl,
                success: (res) => {
                    //动态创建标签下载图片
                    var link = document.createElement('a');
                    link.href = res.tempFilePath;
                    link.download = '分享海报.png';
                    link.click();
                }
            })
            
            /**
             * 原生转换
             * 先把base64转换成Blob数据，再用js动态创建标签下载图片
             *【已测小米自带的浏览器可以下载，QQ浏览器、百度浏览器、夸克等无法识别正确的文件类型】
             */
            // var arr = downloadUrl.split(',');
            // //解码使用 base-64 编码的字符串
            // var bytes = atob(arr[1]);   
            // //创建二进制数据缓冲区
            // let ab = new ArrayBuffer(bytes.length); 
            // //每个像素由红、绿、蓝三个分量组成,而每个分量又可以用 8 位无符号整数来表示
            // let ia = new Uint8Array(ab);
            // for (let i = 0; i < bytes.length; i++) {
            //     ia[i] = bytes.charCodeAt(i);
            // }
            // var blob = new Blob([ab], {
            //     type: 'application/octet-stream'
            // });
            // //URL.createObjectURL方法会根据传入的参数创建一个指向该参数对象的URL,
            // //这个URL的生命仅存在于它被创建的这个文档里.
            // var url = URL.createObjectURL(blob);
            // //动态创建标签下载图片
            // var a = document.createElement('a');
            // a.href = url;
            // a.download = new Date().valueOf() + ".png";
            // //模拟点击事件
            // var e = document.createEvent('MouseEvents');
            // e.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
            // a.dispatchEvent(e);
            // //通知浏览器URL已经不再需要指向对应的文件，将对象URL释放
            // URL.revokeObjectURL(url);
            
            /**
             * 直接使用js动态创建标签下载图片 
             *【在PC端浏览器中可以下载 iOS可以下载 Android不能下载】
             */
            // var oA = document.createElement("a");
            // oA.download = '分享海报.png';
            // oA.href = url;
            // document.body.appendChild(oA);
            // oA.click();
            // oA.remove(); //下载之后把创建的元素删除
        },
        /**
         * Canvs 结束
         */
        
        //复制链接
        copyLink () {
            this._closeAllMask()
            let url = uni.getStorageSync('h5_url') + this.share_href.slice(1)
            copyText('', url);
            console.log('复制链接--》',url);
            //提示复制成功
            this.is_showToast = 1
            this.is_showToast_obj.imgUrl = this.sus
            this.is_showToast_obj.title = this.language.goods.goodsDet.copy_success
            setTimeout(()=>{
                this.is_showToast = 0
            },1000)
        },
        
        //APP分享弹窗
        _invite(type) {
            this.LaiKeTuiInvite(type, this);
        },
        //APP二维码分享/朋友圈/微信
        LaiKeTuiInvite(type, me) {
            if (type == '二维码分享') {
                me._closeAllMask()
                me.showSaveEWM('app')
                return
            }
            if (type == '朋友圈') {
                let shareContent = `我在${me.share.goods.title}发现了一件不错的商品，快来看看吧。`;
                uni.share({
                    provider: 'weixin',
                    scene: 'WXSenceTimeline',
                    type: 0,
                    href: me.share_href,
                    title: me.share.goods.title,
                    summary: shareContent,
                    imageUrl: me.share.goods.imgUrl,
                    success: function(res) {
                        me.fastTap = true
                        uni.showToast({
                            title: me.language.toasts.goodsDetailed.shareOk,
                            icon: 'none'
                        })
                    },
                    fail: function(err) {
                        me.fastTap = true
                        uni.showToast({
                            title: me.language.toasts.goodsDetailed.shareFail,
                            icon: 'none'
                        })
                    }
                })
            } else if (type == '微信') {
                let shareType = 2;
                if(me.LaiKeTuiCommon.IS_SHARE_WECHAT_MINI_PROGRAM && !me.share.mch.type){ shareType = 5 }
                let shareContent = `我在${me.share.goods.title}发现了一件不错的商品，快来看看吧。`
                if (shareType === 2) {
                    uni.share({
                        provider: 'weixin',
                        scene: 'WXSceneSession',
                        type: 0,
                        href: me.share_href,
                        title: me.share.goods.title,
                        summary: shareContent,
                        imageUrl: me.share.goods.imgUrl,
                        success: function(res) {
                            me.fastTap = true
                            uni.showToast({
                                title: me.language.toasts.goodsDetailed.shareOk,
                                icon: 'none'
                            })
                        },
                        fail: function(err) {
                            me.fastTap = true
                            uni.showToast({
                                title: me.language.toasts.goodsDetailed.shareFail,
                                icon: 'none'
                            })
                        }
                    })
                } else {
                    uni.share({
                        provider: 'weixin',
                        scene: 'WXSceneSession',
                        type: 5,
                        imageUrl: me.share.goods.imgUrl,
                        title: me.share.goods.title,
                        miniProgram: {
                            type: 0,
                            id: me.LaiKeTuiCommon.WECHAT_MINI_PROGRAM_ID,
                            webUrl: me.share_href,
                            path: me.share_href
                        },
                        success: function(res) {
                            console.log(res);
                        },
                        fail: function(err) {
                            console.error(err);
                        }
                    });
                }
            }
        },
        
    }
}
</script>

<style scoped lang="less">
	.share{
		z-index:120;
	}
.mask {
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    //height: ;
    width: 100%;
    background-color: rgba(000, 000, 000, 0.5);
    position: fixed;
    top: 0;
    left: 0;
    z-index: 999 !important;

    .shareMask {
        width: 100%;
        bottom: 0;
        max-height: 90%;
        background-color: #fff;
        position: absolute;
        border-radius: 24rpx 24rpx 0 0;
        /* #ifdef H5 */
        // width: 630rpx;
        // height: 192rpx;
        // bottom: 64rpx;
        // left: calc((100% - 630rpx) / 2);
        // border-radius: 20rpx;
        // box-sizing: border-box;
        /* #endif */

        .shareMaskTitle {
            height: 108rpx;
            line-height: 108rpx;
            font-size: 32rpx;
            text-align: center;
            color: #333333;
        }
        .line{
            width: 750rpx;
            height:16rpx;
            background: #F4F5F6;
        }
        .sharepyq {
            // width: 70%;// width: 70%;
            width: 100%;
            margin: 32rpx  auto ;

            .shareIcon {
                width: 50%;
                float: left;
                text-align: center;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    flex-direction: column;
                &.width_33 {
                    width: 33%;
                }
是
                button {
                    /* #ifndef MP */
                    background: transparent;
                    border-radius: 0;
                    padding: 0;
                    margin: 0;
                    /* #endif */
                }

                button::after {
                    border: none;
                }

                .share_btn {
                    background: transparent;
                    padding: 0;
                    margin: 0;
                    border-radius: 0;
                    // width: 250rpx;
                    height: 152rpx;
                }

                .share_btn::after {
                    border: none;
                }

                .share_btn .p {
                    margin-top: -26rpx;
                }

                img {
                    width: 80rpx;
                    height: 80rpx;
                    margin-bottom: 16rpx;
                    /* #ifdef H5 */
                    // margin-top: 34rpx;
                    // margin-bottom: 0;
                    /* #endif */
                }

                p {
                    height: 40rpx;
                    line-height: 40rpx;
                    font-size: 28rpx;
                    // margin-bottom: 25rpx;
                    color: #666666;
                    text-align: center;
                    /* #ifdef H5 */
                    // margin-bottom: 16rpx;
                    /* #endif */
                }
            }
        }

        .shareEnd {
            text-align: center;
            font-size: 32rpx;
            height: 108rpx;
            line-height: 108rpx;
            // border-top: 1px solid #eee;
            color: #333333;
        }
    }

    .allCenter {
        width: 630rpx;
        height: 187rpx;
        border-radius: 20rpx;
        background-color: #fff;
        margin: 0 auto;
        position: absolute;
        bottom: 60rpx;
        left: 60rpx;

        @supports (bottom: constant(safe-area-inset-bottom)) or (bottom: env(safe-area-inset-bottom)) {
            // 不知道为啥没有效果
            //bottom: calc(~"60rpx + constant(safe-area-inset-bottom)");
            //bottom: calc(~"60rpx + env(safe-area-inset-bottom)");
            bottom: 104rpx;
        }

        > div {
            width: 25%;
            text-align: center;
        }
        
        
    }

    .shareEwm {
        position: absolute;
        left: 10%;
        top: 7%;
        // height: 914rpx;
        width: 600rpx;
        background-color: #F4F5F6;
        border-radius: 8px;
        overflow: hidden;

        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
       
        
        .imgEwm {
            -webkit-touch-callout:default;
            width: 100%;
            /* #ifdef MP-TOUTIAO */
            height: 92%;
            /* #endif */
            /* #ifndef MP-TOUTIAO */
            height: 100%;
            /* #endif */            
            border-top-left-radius: 8px;
            border-top-right-radius: 8px;
        }

        .close {
            width: 48rpx;
            height: 48rpx;
            position: absolute;
            top: 32rpx;
            right: 32rpx;
        }

        
    }
    .saveEWMBtn {
        // border-top: 1px solid #E6E6E6;
        // width: 100%;
        // height: 100rpx;
        font-size: 32rpx;
        background-color: #fff;
        // border-bottom-left-radius: 8px;
        // border-bottom-right-radius: 8px;
        // line-height: 100rpx;
        // color: #000;
        color: #FFFFFF;
        position: absolute;
        // bottom: 0;
        bottom: 10%;
        text-align: center;
        
        width: 332rpx;
        height: 92rpx;
        line-height: 92rpx;
        background: #FA5151;
        border-radius: 46rpx;
        left: 50%;
        transform: translateX(-50%);
        
        display: flex;
        justify-content: center;
        align-items: center;
    
        .saves {
            width: 40rpx;
            height:40rpx;
            padding-right: 8rpx;
            margin-top: 8rpx;
        }
    }
}


 .my-canvas-box {
    width: 750rpx;
    height: 100vh;
    position: fixed;
    background-color: rgba(0, 0, 0, 0.6);
    z-index: 999;
    top: 0;
    left: 0;
    display: flex !important;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    
    &.hide {
        display: none;
    }

    &.show {
        display: block;
    }
    .canvas-tip {
        color: #ffffff;
        font-size: 24rpx;
        margin-top: 48rpx;
        text-align: center;            
        width: 332rpx;
        height: 92rpx;
        display: flex;
        justify-content: center;
        align-items: center;
        background: #FA5151;
        border-radius: 46rpx;
        img {
            width: 48rpx;
            height: 48rpx;
            margin-right: 10rpx;
        }
    }
    /* #ifdef MP-WEIXIN */
    .my-canvas {
        width: 600rpx;
        height: 1000rpx;
        background-color: #F4F5F6;
        // margin: 200rpx auto;
        z-index: 9999;
        border-radius: 10rpx;
        overflow: hidden;
    }
    /* #endif */

    /* #ifdef APP-PLUS || H5 */
    .my-canvas {
        width: 600rpx;
        height: 1000rpx;
        background-color: #F4F5F6;
        // margin-top: 220rpx auto;
        z-index: 999;
        border-radius: 10rpx;
        overflow: hidden;
    }
    /* #endif */
}

</style>
