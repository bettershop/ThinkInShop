<template>
    <view @touchmove.stop.prevent="moveHandle">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
    <!-- <view> -->
        <heads :title="language.InputWidgets.title" :ishead_w="2" :border="true" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <view>
            <!-- 右上角粉色 -->
            <view class="yanse"></view>
            <!-- 聊天界面 -->
            <view class="pages">
                <headlist
                    ref="userlist"
                    :style="{height:windowHeight}"
                    :userlist="userlist" 
                    :height_max="windowHeight"
                    @is_expressionboxa="iscloseclick" 
                    @change='changeuser'>
                </headlist>
                <scrollview 
                    style="width: 80%;"
                    :style="{height:windowHeight}"
                    :width_max="'100%'"
                    :is_store='type' 
                    :height_max="windowHeight" 
                    :is_xs="false" 
                    :ChatArray="arrlist" 
                    :scroll="true" 
                    :scroll-top='scrollTop'
                    @is_expressionboxa="iscloseclick" 
                    @click="colse" >
                </scrollview>
            </view>
            <!-- 底部输入 -->
            <InputWidgets
                @_fatherClick="_fatherClick"
                ref="inputWidgets"
                :is_expressionbox="is_expression"
                :is_photobox="is_photo"
                :otherHeight='keyboardHeight'
                :is_store='type'
                @_touchend="_touchend"
                @_focus='_focus' 
                @_blur='_blur'
                @change="handleChange" 
                @imgs="imgclick" 
                @img="jsimg" 
                @is_expressionbox='xiangji' 
                @is_photobox = "emoji">
            </InputWidgets>
        </view>
    </view>
</template>

<script>
    import scrollview from "@/components/A_chat/A_chat.vue"
    import headlist from "@/components/A_chat/Head_ist.vue"
    import InputWidgets from"@/components/A_chat/InputWidgets.vue"
    import {LaiKeTui_chooseImg} from "@/pagesA/myStore/myStore/uploadPro";
    export default {
        name: 'mch_kefu',
        components: { //注册组件
            scrollview,
            headlist,
            InputWidgets
        },
        data() {
            return {
                bgColor: [{
                        item: 'rgba(0,0,0,0)'
                    },
                    {
                        item: 'rgba(0,0,0,0)'
                    }
                ],
                cover_map:'',
                socketTask:'',
                is_expression:false,
                is_photo:false,
                userlistindex:0,
                scrollTop:999999,
                type:1,             //0是用户，1是店铺
                user_ida:'',       //进入时用户id
                mchid:'',          //进入时店铺id
                currentUserId: '', //当前用户id 防串
                topusers:'',
                arrlist:[],        //聊天内容
                userlist:[],       //店铺列表
                img_list:[],       //发送图片
                input_content:'',//输入框
                //
                userImg: '',//用户头像
                //
                aTopHeight: 44,    //头部组件高度
                aBottomHeight: 52, //底部输入框高度
                windowHeight:'',   //聊天内容高度 '375px'
                //
                getSystemHeight:'',//终端初始可用屏幕高度
                SystemType: '',//获取操作系统类型（PC-H5/iOS-H5/Android-H5/iOS-MP/Android-MP/iOS-APP/Android-APP）
                virtualKeyBoardHeight: '',//手机虚拟键盘高度
                keyboardHeight: 0, //默认键盘高度
                keyboardShow: false,//键盘是否开启
                //需要增加一个参数，用于判断Socket是自动断开连接（返回上一页），还是页面关闭断开连接（不需要返回上一页）
                socketOffType: false, //true页面关闭
                lkt_type: '',//接口后台类型，解决java与php，websocket握手时传值方式不同的问题
                wsConnected: false,
                wsClientKey: '',
                reconnectTimer: null,
                reconnectLock: false,
                reconnectCount: 0,
            }
        },
        computed:{
            ifUser(){
                //监听有没有用户聊天，不存在用户则不能发送消息
                return this.userlist.length?true:false
            }
        },
        created() {
            /**
             * 当前页面重新获取系统信息 解决部分浏览器可以改变窗口高度问题
             */
            uni.getSystemInfo({
                success: function(res) {
                    uni.setStorageSync('getSystemHeight', {name:'手机屏幕可用高度',value:res.windowHeight});
                    this.getSystemHeight = res.windowHeight
                    // #ifndef H5
                    //小程序/APP: 通过系统信息计算input高度 (H5: 通过动态计算标签 获取input高度)
                    var safeAreaBottom = res.screenHeight - res.safeArea.bottom;
                    uni.setStorageSync('virtualKeyBoardHeight', {name:'手机虚拟键盘高度',value:safeAreaBottom})
                    // #endif
                }
            });
            //获取 终端初始可用屏幕高度
            this.getSystemHeight = uni.getStorageSync('getSystemHeight').value
            //获取 操作系统类型
            this.SystemType = uni.getStorageSync('getSystemType').value
            //获取 信息栏+头部高度
            this.aTopHeight = uni.getStorageSync('headerHeight')
            //获取 手机虚拟键盘高度
            this.virtualKeyBoardHeight = uni.getStorageSync('virtualKeyBoardHeight').value?uni.getStorageSync('virtualKeyBoardHeight').value:0
            //获取 键盘高度
            this.keyboardHeight = uni.getStorageSync('keyboardHeight').value?uni.getStorageSync('keyboardHeight').value:0
            //区分终端类型 处理键盘高度
            // #ifdef H5
                if(this.SystemType == 'iOS-H5'){
                    //iOS在键盘收起的时候input 会失去焦点（安卓不会）
                    document.body.addEventListener('focusin', () => {
                        //if(!this.keyboardHeight){
                            setTimeout(()=>{
                                uni.getSystemInfo({
                                    success: (res) => {
                                        //iOS-H5另外处理(iOS-H5的键盘高度=挤的dom高度；当input在键盘弹窗上方时，不会挤压dom或者高度计算不准确)
                                        if(this.is_photo||this.is_expression){return}
                                        this.keyboardHeight = this.getSystemHeight - res.windowHeight
                                        uni.setStorageSync('keyboardHeight', {name:'手机键盘高度',value:this.keyboardHeight})
                                    }
                                })
                            }, 500)
                        //}
                    })
                } else if(this.SystemType == 'Android-H5'){
                    //Android键盘弹起会触发页面的resize 事件（ios不会）
                    window.addEventListener('resize', () => {
                        uni.getSystemInfo({
                            success: (res) => {
                                //适配Android-H5，解决手动收起键盘时，input还属于聚焦状态，占位弹窗未隐藏造成的显示问题
                                if(res.windowHeight<this.getSystemHeight){
                                    this.$refs.inputWidgets.isshow = true
                                } else {this.$refs.inputWidgets.isshow = false}
                                //如果input是聚焦状态，即显示键盘弹窗时,获取键盘高度
                                if(this.keyboardShow){
                                    this.keyboardHeight = this.getSystemHeight - res.windowHeight
                                    uni.setStorageSync('keyboardHeight', {name:'手机键盘高度',value:this.keyboardHeight})
                                }
                            }
                        })
                    })
                } else if(this.SystemType == 'PC-H5'){
                    this.keyboardHeight = 350
                } else {}
            // #endif
            // #ifndef H5
                this.keyboardHeight = 350
            // #endif
        },
        onShow() {
            // //初始化显示第一条客服消息
            // this.currentUserId = ''
            // this.userlistindex = 0
            // if(this.$refs.userlist){this.$refs.userlist.typeindex = 0}
            // this.getuserlist();
        },
        onUnload() { 
            this.currentUserId = null;
            this.userlist = [];  
            //标记为页面关闭，用于判断关闭socket是否需要返回上一页
            this.socketOffType = true
            this.wsConnected = false
            this.wsClientKey = ''
            //关闭socket
            uni.closeSocket()
            if (this.reconnectTimer) {
                clearTimeout(this.reconnectTimer)
                this.reconnectTimer = null
            }
        },
        onLoad(option) {
            this.isLogin(() => {})
            this.lkt_type = String(this.LaiKeTuiCommon.LKT_TYPE ? this.LaiKeTuiCommon.LKT_TYPE : 'JAVA').trim().toUpperCase()
            //
            this.currentUserId = null;
            this.mchid = uni.getStorageSync('mch_id') 
            //获取 用户列表数据
            this.getuserlist();
            //连接 websocket
            this.connectSocket();
        },
        mounted() {
            //设置 当前聊天内容高度
            this.getWindowHeight(0)
        },
        methods: {
            //禁止页面滚动
            moveHandle(){
                return
            },
            //父组件接收到，需要计算聊天内容高度要求
            _fatherClick(){
                if(this.SystemType=='PC-H5'){
                    this.getWindowHeight(0)
                } else {
                    this.getWindowHeight(this.keyboardHeight)
                }
                setTimeout(()=>{
                    this.$refs.inputWidgets.isshow = true
                }, 100)
            },
            //获取焦点
            _focus(keyboardHeight){
                //标记键盘弹窗已开启
                this.keyboardShow = true
                //计算键盘高度
                this.keyboardHeight = keyboardHeight
                //隐藏表情弹窗
                this.is_photo = false
                //隐藏相册
                this.is_expression = false
                //重新计算高度
                switch(this.SystemType){
                    case 'PC-H5':
                        break
                    default:
                        this.getWindowHeight(keyboardHeight)
                        break
                }
            },
            //聊天内容高度（keyboardHeight 键盘高度）
            getWindowHeight(keyboardHeight){
                this.windowHeight = this.getSystemHeight - this.aTopHeight - this.aBottomHeight - this.virtualKeyBoardHeight - keyboardHeight
                this.windowHeight = this.windowHeight + 'px'
            },
            //失去焦点
            _blur(){
                //标记键盘弹窗已关闭
                this.keyboardShow = false
                //失去焦点时，如果还有弹窗显示中，则不需要重新计算高度
                if(!this.is_expression&&!this.is_photo){this.getWindowHeight(0)}
            },
            //点击笑脸显示表情
            emoji(keyboardHeight){
                //不存在与店铺聊天的用户
                if(!this.ifUser){
                    uni.showToast({
                        title: '当前不存在聊天用户',
                        icon: 'none'
                    })
                    return
                }
                this.keyboardShow = false  //标记键盘弹窗已关闭
                this.is_photo = false   //相机开关
                this.is_expression = true   //表情开关
                this.getWindowHeight(this.keyboardHeight)
            },
            //点击加号显示相册拍照
            xiangji(keyboardHeight){
                //不存在与店铺聊天的用户
                if(!this.ifUser){
                    uni.showToast({
                        title: '当前不存在聊天用户',
                        icon: 'none'
                    })
                    return
                }
                this.keyboardShow = false  //标记键盘弹窗已关闭
                this.is_photo = true    //相机开关
                this.is_expression = false  //表情开关
                this.getWindowHeight(this.keyboardHeight)
            },
            //切换店铺
            changeuser(e,index){
                //获取当前用户id
                this.topusers = e.user_id 
                this.currentUserId = e.user_id  
                //更新当前用户数据，标记已读
                this.userlistindex = index
                this.userlist[this.userlistindex].no_read_num = 0 
                //获取 当前用户的聊天记录
                this._newlist();
            },
            /**
             * 点击隐藏弹窗
             * @param {Boolean} e false
             */
            iscloseclick(e){
                uni.hideKeyboard()
                this.is_expression = e
                this.is_photo = e
                this.getWindowHeight(0)
            },
            //拍照完成
            imgclick(e){
                if(e){
                    this.img_list = e
                    this.is_photo = false
                    this.is_expression = false
                    this.handleChange()
                }
            },
            //拍照未完成
            jsimg(e){
            },
            //
            colse(){
            },
            //发送消息
            handleChange(e){
                //不存在与店铺聊天的用户
                if(!this.ifUser){
                    uni.showToast({
                        title: '当前不存在聊天用户',
                        icon: 'none'
                    })
                    return
                }
                this.input_content = e
                if(!this.input_content && !this.img_list){
                }else{ 
                    var data = {
                        api: 'app.msg.addMessage',
                        send_id:this.mchid,
                        receive_id:this.currentUserId,
                        content:e?e:'',
                        is_mch_send:this.type,
                        img_list:this.img_list
                    }
                    this.$req.post({data}).then(res => {
                        //如果是发多张图片情况下需要循环push
                        if(this.img_list.length>0){
                            this.img_list.forEach((item, index)=>{
                                let newData = {}
                                newData.nike_name = this.arrlist[0].nike_name
                                newData.img = this.arrlist[0].img
                                newData.is_mch_send = this.type
                                newData.content = item
                                // this.arrlist.push(newData)
                                this._newlist()
                                //PHP需要这样再发送一次
                                if(this.lkt_type == 'PHP'){
                                    //上传服务器
                                    this.sendSocketMessage(res.data[index]).then(value => {
                                    }).catch(value => {
                                    })
                                }
                            })
                        } else {
                            let newData = {}
                            newData.nike_name = 'noName'
                            newData.img = this.userImg
                            newData.is_mch_send = this.type
                            newData.content = this.input_content
                            // this.arrlist.push(newData)
                            this._newlist()
                            //PHP需要这样再发送一次
                            if(this.lkt_type == 'PHP'){
                                //上传服务器
                                this.sendSocketMessage(res.data[0]).then(value => {
                                }).catch(value => {
                                })
                            }
                        }
                        //当前聊天内容高度
                        this.$nextTick(() => {
                            this.arrlist = this.arrlist
                            this.getWindowHeight(0)
                        })
                        this.arrlist.splice(0,0)//更新数组用的
                        this.input_content = ''//清空输入框
                        this.img_list = ''
                    }).catch(()=>{
                        if(this.userlist.length==0){
                            uni.showToast({
                                title:'暂无会话',
                                icon:'none'
                            })
                        }
                    })
                }
            },
            //获取用户列表
           async getuserlist(){
                var data = {
                    api: 'app.msg.mch_userList',
                    mchId:this.mchid,
                }
                this.$req.post({
                    data
                }).then(res => {  
                    if(res.data.list && res.data.list.length > 0){
                        //用户列表
                        this.userlist = res.data.list  
                        //如果当前用户id存在
                        if(!this.currentUserId){ 
                            this.topusers = res.data.list[0].user_id  
                            this.currentUserId = this.topusers  
                        } 
                        //当前用户id消息 全部已读
                        if(this.topusers){
                            this.userlist[this.userlistindex].no_read_num = 0 
                        }
                        //获取 当前店铺的聊天记录
                        this._newlist()
                    }
                })
            },
            //返回当前用户的聊天记录
            _newlist(){
                var data = {
                    api: 'app.msg.getMessageList',
                    userId:this.currentUserId,
                    mchId:this.mchid,
                    type:this.type,
                }
                this.$req.post({data}).then(res => {
                    this.arrlist = res.data.list
                    this.arrlist.forEach(v=>{
                        try {
                            v.content = JSON.parse(v.content) 
                        } catch (error) {
                            // 兼容旧数据 字符串
                            v.content = {text: v.content}
                        }
                    }) 
                    
                })
            },
            /**
             * websocket 开始
             */
            //连接websocket
            connectSocket() {
                if (this.socketTask && this.wsConnected) {
                    return;
                }
                //当前店铺id 不能为空
                if(this.mchid == '' || !this.mchid  ){
                    return;
                } 
                
                //websocket请求地址
                let wsurl = ''
                if(this.lkt_type == 'JAVA'){
                    let storType = this.LaiKeTuiCommon.getStoreType()
                    wsurl = this.LaiKeTuiCommon.LKT_WEBSOCKET + this.mchid + '/' + this.mchid + '/' + storType;
                } else if(this.lkt_type == 'PHP') {
                    wsurl = this.LaiKeTuiCommon.LKT_WEBSOCKET;
                } else {
                }
                
                let that = this;
                //获取websocket 上下文
                this.socketTask = uni.connectSocket({
                    url: wsurl,
                        success(res) {
                        },
                        fail(err) {
                        }
                    }, 
                ); 
                
                //监听WebSocket连接
                this.socketTask.onOpen(function(res) {
                    that.wsConnected = true
                    that.reconnectCount = 0
                    if (that.reconnectTimer) {
                        clearTimeout(that.reconnectTimer)
                        that.reconnectTimer = null
                    }
                    that.heart()
                })
                
                //监听WebSocket返回数据
                this.socketTask.onMessage(function(res) { 
                    var data =  {}
                    try {
                        data = typeof res.data === 'string' ? JSON.parse(res.data) : (res.data || {})
                    } catch (err) {
                        return
                    }
                    
                    //PHP通过此方式传值 店铺ID
                    if(data.client_key){
                        that.wsClientKey = data.client_key
                        let storType = that.LaiKeTuiCommon.getStoreType()
                        let msg = {
                            type: 'login',//消息类型
                            is_mch_send: '1',//是否店铺发送，用户发送0 店铺发送1
                            send_id: that.mchid,//发送方id
                            client_key: data.client_key,//client_key
                            source: storType,//stor_type
                        }
                        that.sendSocketMessage(msg).then(res => {
                        }).catch(res => {
                        })
                    }
                    
                    //获取 当前数据的用户id/发送者id
                    if(!that.currentUserId || that.currentUserId == ''){
                        if(data.user_id){
                            that.currentUserId = data.user_id
                        }else if(data.send_id){
                            that.currentUserId = data.send_id 
                        }
                    }
                    
                    //如果user_id存在，即有新增用户与我方聊天
                    if(data.user_id){
                        let found = false;
                        for(var i=0;i<that.userlist.length;i++){
                            let tmpUserid = that.userlist[i].user_id
                            //如果用户列表中【存在】当前用户id，更新当前用户数据
                            if(data.user_id == tmpUserid){
                                //更新用户数据
                                that.userlist[i] = data
                                found = true; 
                                break;
                            }
                            //如果当前用户id == 发送方ID，则标记已读 
                            if(that.currentUserId == tmpUserid){
                                that.userlist[i].no_read_num = 0
                            }
                        }
                        //如果用户列表中【不存在】当前用户id，则新增用户
                        if(!found){
                            that.userlist.push(data)
                        } 
                        //强制更新数组
                        that.userlist.splice(0,0)
                    }else{
                        //这里加一个防止串台的判断  
                        setTimeout(()=>{
                            //如果当前用户列表只存在一个，获取当前用户id
                            if( that.userlist.length == 1 ){ 
                                that.currentUserId = that.userlist[0].user_id;
                            }
                            //如果当前用户id == 发送方ID，获取聊天内容
                            if( that.currentUserId && data.send_id && data.send_id == that.currentUserId ){
                                try { 
                                    data.content = JSON.parse(data.content) 
                                } catch (error) {
                                    // 兼容旧数据 字符串
                                    data.content = {text: data.content}
                                }
                                that.arrlist.push(data);
                            }
                            //如果当前用户id == 发送方ID，则标记已读
                            for( var i = 0 ; i < that.userlist.length ; i++){
                                let tmpUserid = that.userlist[i].user_id
                                //如果当前店主正在聊的客户和数组中的客户是同一个则设置未读为0
                                if( that.currentUserId == tmpUserid ) {
                                    that.userlist[i].no_read_num = 0
                                }
                            }
                            //强制更新数组
                            that.userlist.splice( 0,0 )
                        }, 100 );  
                    } 
                    //更新用户列表
                    that.userlist = that.userlist 
                    //迫使组件实例重新渲染
                    that.$forceUpdate();
                });
        
                this.socketTask.onError(function(res) {
                    that.wsConnected = false
                    that.wsClientKey = ''
                    //进入重新连接
                    that.reconnect(); 
                })
                
                // 监听连接关闭 -
                this.socketTask.onClose((e) => {
                    that.wsConnected = false
                    that.wsClientKey = ''
                    clearInterval(that.timer)
                    that.timer = ''
                    //用于判断Socket是自动断开连接，还是页面关闭断开连接
                    if(!this.socketOffType){
                        uni.showToast({
                            title: '连接中断，正在重连',
                            icon: 'none'
                        })
                        that.reconnect()
                    }
                })
            },
            //进入重新连接
            reconnect() {
                if (this.socketOffType || this.reconnectLock) return
                this.reconnectLock = true
                this.wsConnected = false
                this.wsClientKey = ''
                this.socketTask = null;
                if (this.reconnectTimer) {
                    clearTimeout(this.reconnectTimer)
                    this.reconnectTimer = null
                }
                const delay = Math.min(1000 * Math.max(1, this.reconnectCount + 1), 5000)
                this.reconnectTimer = setTimeout(() => {
                    if (this.socketOffType) {
                        this.reconnectLock = false
                        return
                    }
                    if (this.mchid) {
                        this.connectSocket()
                        this.reconnectCount += 1
                    }
                    this.reconnectLock = false
                }, delay)
            },
            //发送消息
            sendSocketMessage(msg) {
                let that = this
                const data = typeof msg === 'string' ? msg : JSON.stringify(msg)
                return new Promise((reslove, reject) => {
                    if (!that.socketTask || typeof that.socketTask.send !== 'function') {
                        reject(new Error('socketTask is null'))
                        return
                    }
                    that.socketTask.send({
                        data,
                        success(res) {
                            reslove(res)
                        },
                        fail(res) {
                            reject(res)
                        }
                    });
                })
            },
            //心跳
            heart() {
                let that = this;
                clearInterval(this.timer);
                this.timer = '';
                let msg = {
                    "type": "heartbeat",
                }
                this.timer = setInterval(() => {
                    if (that.wsClientKey) {
                        that.sendSocketMessage({
                            type: 'login',
                            is_mch_send: '1',
                            send_id: that.mchid,
                            client_key: that.wsClientKey,
                            source: that.LaiKeTuiCommon.getStoreType(),
                        }).catch(() => {})
                    }
                    that.sendSocketMessage(msg).then(res => {
                    }).catch(res => {
                    })
                }, 15000)
            },
            /**
             * websocket 结束
             */
        }
    }
</script>

<style scoped lang="less">
    /deep/.scrollbox_left_box_conten{
        background-color: #FFFFFF;
    }
    /deep/.uni-system-preview-image{
        z-index: 999999 !important;
    }
    .yanse {
        position: fixed;
        top: -278rpx;
        right: -170rpx;
        width: 492rpx;
        z-index: 10;
        height: 492rpx;
        background: #FA5151;
        opacity: 0.12;
        filter: blur(50px);
    }
    /deep/.scrollbox{
        width: 100%;
        // background-color: transparent;
        background-color: #f4f5f6 !important;
    }
    .pages{
        // width: 100%;
        display: flex;
        // background-color: #fff;
    }
</style>
