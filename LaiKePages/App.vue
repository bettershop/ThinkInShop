<script>
import {mapMutations} from 'vuex';
import { getSystemConfig, setPageTitle } from './common/systemConfig.js';

export default {
    data() {
        return {
            access_id: false,
            getGrade: true,
            hasGrade: '', 
        };
    }, 
 
    onLoanch(){  
    }, 
   onUnload(){
        uni.removeStorageSync('setLangFlag')
   },
	    async onShow(){
	        // 在页面显示时，检查本地存储中是否存在语言类型。 如果不存在，则默认设置为中文（zh）。
	        if (!uni.getStorageSync('language')) {
	            uni.setStorageSync('language', 'zh_CN')
	        }
	        if (!uni.getStorageSync('lang_code')) {
	            uni.setStorageSync('lang_code', uni.getStorageSync('language') || 'zh_CN')
	        }
        //实时获取商城默认币种
        let  result = await this.LaiKeTuiCommon.getDefaultCurrencys();
        let storeCurrency = result.data;
        uni.setStorageSync('storeCurrency',storeCurrency);
        
        let currency = uni.getStorageSync('currency')
        if (currency) {
            //没有登录的情况下currency是为空的
            //用户自己选择的货币符号
            this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = currency.currency_symbol
            //汇率
            this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE = currency.exchange_rate
        } else if(storeCurrency) {
            //第一次进入移动端商城storeCurrency为空
            uni.setStorageSync('currency',storeCurrency);
            //商城默认货币符号
            this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = storeCurrency.currency_symbol;
            //汇率
            this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE = storeCurrency.exchange_rate
        }

        this.refreshSystemTitle();
    }, 
    onLaunch(options) {  
        // #ifdef H5 
            let storeID =  uni.getStorageSync('store_id') || options.query.store_id;
            this.LaiKeTuiCommon.initStoreID(storeID); 
            
            this.getSystemIcon()
        // #endif
        
        uni.removeStorageSync('hasGrade'); 
        
	        if (!uni.getStorageSync('language')) {
	        	uni.setStorageSync('language', 'zh_CN')
	        }
	        if (!uni.getStorageSync('lang_code')) {
	            uni.setStorageSync('lang_code', uni.getStorageSync('language') || 'zh_CN')
	        }
        
        // 导航信息条高度
        uni.getSystemInfo({
            success:  (res)=> { 
                var nate = res.statusBarHeight;
                uni.setStorageSync('data_height', nate);
                uni.setStorageSync('getSystemHeight', {name:'手机屏幕可用高度',value:res.windowHeight});
                //获取底部安全区域的高度
                // #ifndef H5
                var safeAreaBottom = res.screenHeight - res.safeArea.bottom;
                uni.setStorageSync('virtualKeyBoardHeight', {name:'手机虚拟键盘高度',value:safeAreaBottom})
                // #endif 
                this.data_height(nate);  
            }
        });
        
        //获取终端类型
        this.systemType() 

        // 先用缓存标题兜底，避免 H5 标签显示 URL
        setPageTitle();
        
        this.geturl().finally(() => {
            this.refreshSystemTitle();
        });
     
        // type: 1.文章海报 2.红包海报 3.商品海报 4.分销海报 5.邀请海报 6.竞拍海报
        // store_type=1小程序,=2app
        // store_id商城id
        //proType商品的类型：

        // #ifdef APP-PLUS
        var cid = this.LaiKeTuiCommon.getClientid();
        let timer = setInterval(() => {
            if (!cid || cid == null || cid == 'null') {
                cid = this.LaiKeTuiCommon.getClientid();
            } else {
                clearInterval(timer);
                timer = null;
                uni.setStorageSync('cid', cid);
            }
        }, 1000);

        const _handlePush = function(message) {
            uni.redirectTo({
                url: '/pages/shell/shell?pageType=my'
            });
        };
        plus.push.addEventListener(
            'click',
            function(message) {
                _handlePush();
            },
            false
        );
        plus.push.addEventListener(
            'receive',
            function(message) {
                logoutPushMsg(message);
            },
            false
        );

        //创建本地推送
        function createLocalPushMsg(content) {
            var options = { cover: false };
            plus.push.createMessage(content, 'LocalMSG', options);
        }

        //获取穿透参数
        function logoutPushMsg(msg) {
            if (msg.payload && msg.payload != 'LocalMSG') {
                if (typeof msg.payload == 'string') {
                    createLocalPushMsg(msg.content);
                } else {
                    var data = JSON.parse(msg.payload);
                    createLocalPushMsg(data.content);
                }
            }
        }

        // #endif

        let data = {
            api: 'app.index.get_membership_status',} 
        this.$req.post({data}).then(res=>{ 
            this.getGrade = true
            if(res.code == 200){
                this.hasGrade = res.data.membership_status
                uni.setStorageSync('hasGrade', this.hasGrade)
            }
        })
    },
    methods: {
        ...mapMutations({
            data_height: 'SET_DATA_HEIGHT'
        }),
          
        // 获取浏览器 icon配置
        async getSystemIcon() { 
          let systemInfo = {};
          try {
              systemInfo = await getSystemConfig();
          } catch (error) {
              return;
          }
          const mallName = systemInfo && (
              systemInfo.name ||
              systemInfo.mall_name ||
              systemInfo.store_name ||
              systemInfo.appTitle ||
              systemInfo.title
          )
          if (mallName) {
            uni.setStorageSync("mall_name", mallName)
            uni.setStorageSync("appTitle", mallName)
            setPageTitle()
          }
          if (systemInfo.html_icon) {
            const { html_icon } = systemInfo
      
            const oldLink = document.querySelector('link[rel="icon"]');
            if (oldLink) {
              document.head.removeChild(oldLink);
            }
      
            // 创建新icon标签（对应你提供的原生代码）
            const link = document.createElement('link');
            link.rel = 'icon';
            // 自动识别icon格式，兼容ico/png
            link.type = html_icon.includes('png') ? 'image/png' : 'image/x-icon';
            link.href = html_icon;
      
            // 插入到head
            document.head.appendChild(link);
          }
        },
        refreshSystemTitle(retry = true) {
            setPageTitle();
            getSystemConfig().then(() => {
                setPageTitle();
            }).catch(err => {
                console.log('获取系统配置失败，未更新标题', err);
                if (retry) {
                    setTimeout(() => {
                        this.refreshSystemTitle(false);
                    }, 800);
                }
            });
        },
        
        // 获取设备 基地址
        async geturl () {
            let me = this;
            let data = {
                api: 'app.url.geturl',
                get: 'mini_url,H5,endurl'
            };
            let res = await this.$req.post({data}) 

			me.$store.state.h5_url = me.LaiKeTuiCommon.LKT_H5_DEFURL;
			me.$store.state.endurl = me.LaiKeTuiCommon.LKT_ENDURL;
          
            uni.setStorageSync('h5_url', me.$store.state.h5_url);
            uni.setStorageSync('endurl', me.$store.state.endurl);
        },
        /**
         * 操作系统判断
         */
        systemType(){
            let type = ''
            uni.getSystemInfo({
                success: (res)=> {
                    type =  res.osName
                }
            })
            // #ifdef H5
                //平台、设备和操作系统
                let system = {
                	win: false,
                	mac: false,
                	xll: false,
                	ipad: false
                };
                //检测平台
                let p = navigator.platform;
                system.win = p.indexOf("Win") == 0;
                system.mac = p.indexOf("Mac") == 0;
                system.x11 = (p == "X11") || (p.indexOf("Linux") == 0);
                system.ipad = (navigator.userAgent.match(/iPad/i) != null) ? true : false;
                //判断是PC端打开还是手机端打开
                if (system.win || system.mac || system.xll || system.ipad) {
                    uni.setStorageSync('getSystemType',  {name:'操作系统类型',value:'PC-H5'});
                } else {
                    if(type == 'ios'){
                        uni.setStorageSync('getSystemType', {name:'操作系统类型',value:'iOS-H5'});
                    }else{
                        uni.setStorageSync('getSystemType', {name:'操作系统类型',value:'Android-H5'});
                    }
                }	
            // #endif
            // #ifdef MP
                if(type == 'ios'){
                    uni.setStorageSync('getSystemType', {name:'操作系统类型',value:'iOS-MP'});
                }else{
                    uni.setStorageSync('getSystemType', {name:'操作系统类型',value:'Android-MP'});
                }
            // #endif
            // #ifdef APP
                if(type == 'ios'){
                    uni.setStorageSync('getSystemType', {name:'操作系统类型',value:'iOS-APP'});
                }else{
                    uni.setStorageSync('getSystemType', {name:'操作系统类型',value:'Android-APP'});
                }
            // #endif		
        } 
    }
};
</script>
 
<style lang="less">
    /*每个页面公共css */
    @import '@/laike.less';
    @import '@/common/public.less';
    
    /* #ifndef MP */
    @import '@/static/iconfont1/iconfont.css';
    /* #endif */
    
    @import '@/static/css/base.css';
    .translateSelectLanguage{
        display: none !important;
    }
   .uni-tabbar {
     display: none !important;
   }
   /* 微信小程序原生tabbar类名 */
   .tabbar {
     display: none !important;
   }
   /* H5端可能的类名 */
   .weui-tabbar {
     display: none !important;
   } 
    //解决IOS H5页面上下滑动tabbar与浏览器底部之间出现空挡
    uni-tabbar{
        bottom: 0rpx !important;
        z-index: 990;
    }
    //提示语要显示在图片预览的上一层
    /deep/uni-toast{
        z-index: 1000 !important;
    }
    //图片预览按钮
    /deep/uni-swiper .uni-swiper-navigation-hide {
        opacity: 1 !important;
    }
    /deep/.delmask{
        z-index: 99999 !important;
        height: 100vh !important;
    }
    /deep/.uni-modal {
        font-size: 15px !important;
        border-radius: 11px !important;
        max-width: 640rpx !important;
        
        .uni-modal__hd{
             padding: 64rpx 48rpx 0;
        }
        .uni-modal__bd{
                padding: 22rpx 48rpx 66rpx;
        }
        .uni-modal__ft {
            .uni-modal__btn_default {
                font-size: 17px !important;
                color:#333333 !important;
            }
            .uni-modal__btn_primary {
                font-size: 17px !important;
                color: #D73B48 !important;
            }
        }
    }
    .uni-switch-input:before {
         background-color: #999999 !important;
    }
    .new_input{
        .uni-input-input{
            font-size: 32rpx;
        }
    }
    .input{
        .uni-input-input{
            font-size: 32rpx;
            color: #333333;
        }
    }
    .delmask{
        z-index: 99999 !important;
        height: 100vh !important;
    }
    ::-webkit-scrollbar{
        display: none;
        width: 0 !important;
        height: 0 !important;
        -webkit-appearance: none;
        background: transparent;
        color: transparent;
    }
    // 佣金提现页面输入框
    #put_mon{
        .uni-input-input{
            font-size: 48rpx;
            font-weight: normal;
            color: #333333;
            font-family: DIN;
        }
    }
</style>
