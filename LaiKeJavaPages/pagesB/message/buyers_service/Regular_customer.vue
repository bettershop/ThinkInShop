<template>
	<view @touchmove.stop.prevent="moveHandle">
		<lktauthorize ref="lktAuthorizeComp"></lktauthorize>
		<!-- <view> -->
		<heads ref="heads" :title="language.InputWidgets.myKeFu" :ishead_w="2" :border="true" :bgColor="bgColor"
			:titleColor="'#333333'"></heads>
		<view>
			<!-- 右上角粉色 -->
			<view class="yanse"></view>
			<!-- 聊天界面 -->
			<view class="pages">
				<headlist ref="mchlist" :style="{height:windowHeight}" :userlist="userlist" :height_max="windowHeight"
					@is_expressionboxa="iscloseclick" @change='changeuser'>
				</headlist>
				<scrollview style="width: 80%;" :style="{height:windowHeight}" :width_max="'100%'" :is_store='type'
					:height_max="windowHeight" :is_xs="false" :ChatArray="arrlist" :scroll="true"
					:scroll-top='scrollTop' @is_expressionboxa="iscloseclick" :temporarily='temporarily' @click="colse">
				</scrollview>
			</view>
			<!-- 底部输入 -->
			<InputWidgets @_fatherClick="_fatherClick" ref="inputWidgets" :is_store='type'
				:is_expressionbox="is_expression" :is_photobox="is_photo" :otherHeight='keyboardHeight'
				@_touchend="_touchend" @_focus='_focus' @_blur='_blur' @change="handleChange" @imgs="imgclick"
				@queryOrderList="selecOrderListBymchId" @img="jsimg" @is_expressionbox='xiangji' @is_photobox="emoji">
			</InputWidgets>
		</view>
		<!-- 订单列表 -->
		<view class="order-list mask" style="align-items: end;" v-if="queryListFlsg ">
			<view class="order-list-box">
				<view class="tit">
					<text>请选择需要咨询的订单 </text>
					<img class="cha" :src="xxxx" @tap="_mask_f()" />
				</view>
				<view class="noList" v-if="orderList && orderList.length==0">
					<div><img class="noFindImg" :src="noOrder" /></div>
					<span class="noFindText">{{
                       language.order.myorder.no_order
                    }}</span>
				</view>
				<view class="order-list-content">
					<view style="margin-bottom: 20rpx; padding: 10rpx 34rpx; " v-for="(item,index) in orderList"
						:key="index">
						<view class="order-id">{{language.yushou_settlement.bh}}:{{item.sNo}}</view>
						<template v-for="(goodItem,index) in item.list">
							<view class="goods-info" v-if='index == 0'>
								<view class="goods-img">
									<image :src="goodItem.imgUrl || ErrorImg" @error="handleErrorImg"></image>
								</view>
								<view class="" style="margin-left: 20rpx;width: 446rpx;">
									<view>
										<text class="product-name"></text>
										<view class='price'>合计：{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.old_total || 0)}}</view>
									</view>
									<view style="margin-top: 20rpx;">
										<text class="product-spec"></text>
										<text style='width: 200rpx;margin-left: 30rpx;'>共{{item.num}}件商品</text>
									</view>
									<!-- 按钮区域 -->
									<view class="controls-but">
										<view class="btn-group">
											<view class="" v-if="item.userCanAfter" @tap='shouhou(item)'>申请售后</view>
											<view @tap='sendOredId(item.id,goodItem,item)'>发送</view>
										</view>
									</view>
								</view>
							</view>
						</template>

					</view>
				</view>
			</view>

		</view>
	</view>
</template>

<script>
	import scrollview from "@/components/A_chat/A_chat.vue"
	import headlist from "@/components/A_chat/Head_ist.vue"
	import InputWidgets from "@/components/A_chat/InputWidgets.vue"
	import {
		LaiKeTui_chooseImg
	} from "@/pagesA/myStore/myStore/uploadPro";
	import {
		onKeyboardHeightChange
	} from "@/common/getKeyboardHeight"
	export default {
		name: 'user_kefu',
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
				cover_map: '',
				socketTask: '',
				is_expression: false,
				is_photo: false,
				userlistindex: 0,
				scrollTop: 999999,
				type: 0, //0是用户，1是店铺
				user_ida: '', //进入时用户id
				mchid: '', //进入时店铺id
				currentUserId: '', //当前用户id 防串
				currentMchId: '', //当前店铺id 防串
				topusers: '', //当前店铺id
				arrlist: [], //聊天内容
				userlist: [], //店铺列表
				img_list: [], //发送图片
				input_content: '', //输入框
				//
				userImg: '', //用户头像
				//
				aTopHeight: 44, //头部组件高度
				aBottomHeight: 52, //底部输入框高度
				windowHeight: '', //聊天内容高度 '375px'
				//
				getSystemHeight: '', //终端初始可用屏幕高度
				SystemType: '', //获取操作系统类型（PC-H5/iOS-H5/Android-H5/iOS-MP/Android-MP/iOS-APP/Android-APP）
				virtualKeyBoardHeight: '', //手机虚拟键盘高度
				keyboardHeight: 0, //默认键盘高度
				keyboardShow: false, //键盘是否开启
				//需要增加一个参数，用于判断Socket是自动断开连接（返回上一页），还是页面关闭断开连接（不需要返回上一页）
				socketOffType: false, //true页面关闭
				lkt_type: '', //接口后台类型，解决java与php，websocket握手时传值方式不同的问题
				queryListFlsg: false,
				xxxx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xxxx.png',
				ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
				noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon/noOrder.png",
				orderList: [],
				page: 1,
				orederId: '',
				goodsInfo: {}, // 订单查询的对象
				temporarily: {}, 
				keyboardCallback: null,
				reconnectTimer: null,
				reconnectLock: false,
				reconnectCount: 0
			}
		},
		computed: {
			ifUser() {
				//监听有没有用户聊天，不存在用户则不能发送消息
				return this.userlist.length ? true : false
			}
		},
		beforeDestroy() {

		},
		created() { 
			/**
			 * 当前页面重新获取系统信息 解决部分浏览器可以改变窗口高度问题
			 */
			uni.getSystemInfo({
				success: function(res) {
					uni.setStorageSync('getSystemHeight', {
						name: '手机屏幕可用高度',
						value: res.windowHeight
					});
					this.getSystemHeight = res.windowHeight
					// #ifndef H5
					//小程序/APP: 通过系统信息计算input高度 (H5: 通过动态计算标签 获取input高度)
					var safeAreaBottom = res.screenHeight - res.safeArea.bottom;
					uni.setStorageSync('virtualKeyBoardHeight', {
						name: '手机虚拟键盘高度',
						value: safeAreaBottom
					})
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
			this.virtualKeyBoardHeight = uni.getStorageSync('virtualKeyBoardHeight').value ? uni.getStorageSync(
				'virtualKeyBoardHeight').value : 0
			//获取 键盘高度
			this.keyboardHeight = uni.getStorageSync('keyboardHeight').value ? uni.getStorageSync('keyboardHeight').value :
				0
			//区分终端类型 处理键盘高度
			// #ifdef H5
			if (this.SystemType == 'iOS-H5') {
				//iOS在键盘收起的时候input 会失去焦点（安卓不会）
				document.body.addEventListener('focusin', () => {
					//if(!this.keyboardHeight){
					setTimeout(() => {
						uni.getSystemInfo({
							success: (res) => {
								//iOS-H5另外处理(iOS-H5的键盘高度=挤的dom高度；当input在键盘弹窗上方时，不会挤压dom或者高度计算不准确)
								if (this.is_photo || this.is_expression) {
									return
								}
								this.keyboardHeight = this.getSystemHeight - res.windowHeight
								uni.setStorageSync('keyboardHeight', {
									name: '手机键盘高度',
									value: this.keyboardHeight
								})
							}
						})
					}, 500)
					//}
				})
			} else if (this.SystemType == 'Android-H5') {
				//Android键盘弹起会触发页面的resize 事件（ios不会）
				window.addEventListener('resize', () => {
					uni.getSystemInfo({
						success: (res) => {
							//适配Android-H5，解决手动收起键盘时，input还属于聚焦状态，占位弹窗未隐藏造成的显示问题
							if (res.windowHeight < this.getSystemHeight) {
								this.$refs.inputWidgets.isshow = true
							} else {
								this.$refs.inputWidgets.isshow = false
							}
							//如果input是聚焦状态，即显示键盘弹窗时,获取键盘高度
							if (this.keyboardShow) {
								this.keyboardHeight = this.getSystemHeight - res.windowHeight
								uni.setStorageSync('keyboardHeight', {
									name: '手机键盘高度',
									value: this.keyboardHeight
								})
							}
						}
					})
				})
			} else if (this.SystemType == 'PC-H5') {
				this.keyboardHeight = 350
			} else {}
			// #endif
			// #ifndef H5
			this.keyboardHeight = 350
			// #endif
		},
		onShow() {
			//初始化显示第一条客服消息
			// this.currentMchId = ''
			// this.userlistindex = 0
			// if(this.$refs.mchlist){this.$refs.mchlist.typeindex = 0}
			// //获取 店铺列表数据
			// this.getuserlist();
		},
		onUnload() {
			uni.removeStorageSync('temporarily')
			//清空 当前店铺id 与 店铺列表
			this.currentMchId = null;
			this.userlist = [];
			//标记为页面关闭，用于判断关闭socket是否需要返回上一页
			this.socketOffType = true
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
			//获取 进入时的店铺id
			if (option.mch_id) {
				this.mchid = option.mch_id
				this.currentMchId = this.mchid
			}
			//获取 当前用户id
			this.currentUserId = uni.getStorageSync('user_id')
			//获取 店铺列表数据
			this.getuserlist();
			// 获取 当前店铺的订单
			this.queryOrderList()
			//连接 websocket
			this.connectSocket();
		},
		mounted() {
			//设置 当前聊天内容高度
			this.getWindowHeight(0)
			if (uni.getStorageSync('temporarily')) {
				this.temporarily = JSON.parse(uni.getStorageSync('temporarily'))
			}

		},
		methods: {
			// 图片报错处理
			handleErrorImg(e) {
				setTimeout(() => {
					this.imgurl = this.ErrorImg
				}, 0)
			},
			//禁止页面滚动
			moveHandle() {
				return
			},
			//父组件接收到，需要计算聊天内容高度要求
			_fatherClick() {
				if (this.SystemType == 'PC-H5') {
					this.getWindowHeight(0)
				} else {
					this.getWindowHeight(this.keyboardHeight)
				}
				setTimeout(() => {
					this.$refs.inputWidgets.isshow = true
				}, 100)
			},
			//获取焦点
			_focus(keyboardHeight) {
				//标记键盘弹窗已开启
				this.keyboardShow = true
				//计算键盘高度
				// this.keyboardHeight = keyboardHeight
				//隐藏表情弹窗
				this.is_photo = false
				//隐藏相册
				this.is_expression = false
				onKeyboardHeightChange((res) => {
					this.keyboardHeight = res.height 
					//重新计算高度
					switch (this.SystemType) {
						case 'PC-H5':
							break
						default:
							this.getWindowHeight(res.height)
							break
					}
				})
			},
			//聊天内容高度（keyboardHeight 键盘高度）
			getWindowHeight(keyboardHeight) {
				this.windowHeight = this.getSystemHeight - this.aTopHeight - this.aBottomHeight - this
					.virtualKeyBoardHeight - keyboardHeight
				this.windowHeight = this.windowHeight + 'px'

				},
			//失去焦点
			_blur() {
				//标记键盘弹窗已关闭
				this.keyboardShow = false
				//失去焦点时，如果还有弹窗显示中，则不需要重新计算高度
				if (!this.is_expression && !this.is_photo) {
                    this.keyboardHeight = 0
					this.getWindowHeight(0)
				}
			},
			//点击笑脸显示表情
			emoji(keyboardHeight) {
				//不存在与店铺聊天的用户
				if (!this.ifUser) {
					uni.showToast({
						title: '当前不存在聊天用户',
						icon: 'none'
					})
					return
				}
				this.keyboardShow = false //标记键盘弹窗已关闭
				this.is_photo = false //相机开关
				this.is_expression = true //表情开关
				if (keyboardHeight <= 0) {
                    this.keyboardHeight = 0
					this.iscloseclick(false) 
				} else {
                    this.keyboardHeight = keyboardHeight
					this.getWindowHeight(this.keyboardHeight)
				}
			},
			//点击加号显示相册拍照
			xiangji(keyboardHeight) {
				//不存在与店铺聊天的用户
				if (!this.ifUser) {
					uni.showToast({
						title: '当前不存在聊天用户',
						icon: 'none'
					})
					return
				}
				this.keyboardShow = false //标记键盘弹窗已关闭
				this.is_photo = true //相机开关
				this.is_expression = false //表情开关
				if (keyboardHeight <= 0) {
                    this.keyboardHeight = 0
					this.iscloseclick(false)
				} else {
                    this.keyboardHeight = keyboardHeight
					this.getWindowHeight(this.keyboardHeight)
				}
			},
			//切换店铺
			changeuser(e, index) {
				uni.removeStorageSync('temporarily')
				this.temporarily = {}
				//获取当前店铺id
				this.topusers = e.mch_id
				this.currentMchId = e.mch_id
				//更新当前店铺数据，标记已读
				this.userlistindex = index
				this.userlist[this.userlistindex].no_read_num = 0
				//获取 当前店铺的聊天记录
				this._newlist();
				// 获取 当前店铺的订单
				this.queryOrderList()
			},
			/**
			 * 点击隐藏弹窗
			 * @param {Boolean} e false
			 */
			iscloseclick(e) {
				uni.hideKeyboard()
				this.is_expression = e
				this.is_photo = e
                this.keyboardHeight = 0
				this.getWindowHeight(0)
			},
			//拍照完成
			imgclick(e) {
				if (e) {
					this.img_list = e
					this.is_photo = false
					this.is_expression = false
					this.handleChange().then(() => {
						this._newlist()
					})

				}
			},
			//拍照未完成
			jsimg(e) {
			},
			selecOrderListBymchId() {
				this.queryListFlsg = true
			},

			_mask_f() {
				this.queryListFlsg = false
				this.keyboardShow = false
				//失去焦点时，如果还有弹窗显示中，则不需要重新计算高度
				if (!this.is_expression && !this.is_photo) {
                    this.keyboardHeight = 0
					this.getWindowHeight(0)
				}
			},
			// 订单列表查询
			queryOrderList() {
				const data = {
					api: "app.customer.orderIndex",
					mchId: this.currentMchId,
					page: this.page
				}
				this.$req.post({
					data
				}).then(res => {

					if (res.code == 200) {
						this.orderList = res.data.list
						this.totle = res.data.totle
					} else {
						uni.showToast({
							title: res.message
						})
					}
				})
			},
			onScroll(event) {

			},
			// 订单号查询发送
			sendOredId(id, goodsitem, otderItem) {
				this.orederId = id
				this.goodsInfo = goodsitem
				this.goodsInfo.r_sNo = otderItem.sNo
				this.goodsInfo.num = otderItem.num
				this.goodsInfo.old_total = otderItem.z_price

				this.goodsInfo.add_time = otderItem.add_time
				this.$nextTick(() => {
					// this.$refs['inputWidgets'].is_photobox = false
					this.$refs['inputWidgets'].isshow = true
				})
				this.handleChange()
				// this._newlist();
			},
			// 申请售后
			shouhou(item) {
				const id = item.list.map(v => v.id)
				this.navTo(`/pagesB/order/batchOrder?orde_id=${item.id}&order_details_id=${id.join(',')}`)

			},
			//
			colse() {
			},
			//发送消息
			async handleChange(e) {
				//不存在与店铺聊天的用户
				if (!this.ifUser) {
					uni.showToast({
						title: '当前不存在聊天用户',
						icon: 'none'
					})
					return
				}
				this.input_content = e
				const pid = uni.getStorageSync('pId')
				const text = typeof this.input_content === 'string' ? this.input_content.trim() : ''
				const isPureProductMessage = !!pid && !text && this.img_list?.length <= 0 && !this.orederId

				if ((!this.input_content || this.input_content.trim() === '') && this.img_list?.length <= 0 && !this
					.orederId && !isPureProductMessage) {
					return
				} else {
					var data = {
						api: 'app.msg.addMessage',
						send_id: this.currentUserId, //发送方id
						receive_id: this.currentMchId, //接收方id
						content: e ? e : '', //发送内容
						is_mch_send: this.type, //0用户 1店铺
						img_list: this.img_list, //发送图片传值
						orderId: this.orederId, //订单id
						pId: isPureProductMessage ? pid : ''
					}
						 
					await this.$req.post({
						data
					}).then(res => {
						this.queryListFlsg = false
						uni.removeStorageSync('pId')
						if (res.code != 200) {
							uni.showToast({
								title: res.message,
								icon: "none"
							})
							return
						}
						//如果是发多张图片情况下需要循环push
						if (this.img_list.length > 0) {
							this.img_list.forEach((item, index) => {
								let newData = {}
								newData.nike_name = 'noName'
								newData.img = this.userImg
								newData.is_mch_send = this.type
								newData.content = {
									text: item
								}
								this.arrlist.push(newData)
								//PHP需要这样再发送一次
								if (this.lkt_type == 'PHP') {
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
							newData.content = {
								text: this.input_content
							}
							if (data.orderId) {
								const orderInfo = {
									orderName: this.goodsInfo.p_name,
									price: this.goodsInfo.old_total,
									num: this.goodsInfo.num,
									orderNo: this.goodsInfo.r_sNo,
									addTime: this.goodsInfo.add_time,
									imgUrl: this.goodsInfo.imgUrl,
									orderId: this.orederId
								};
								newData.content = {
									order: orderInfo
								}
							} else {
								// 普通文本 
								newData.content = {
									text: this.input_content
								}
							}
							this.arrlist.push(newData)
							//PHP需要这样再发送一次
							if (this.lkt_type == 'PHP') {
								//上传服务器
								this.sendSocketMessage(res.data[0]).then(value => {
								}).catch(value => {
								})
							}
						}

						//当前聊天内容高度
						this.$nextTick(() => {
							this.arrlist = this.arrlist
                            this.keyboardHeight = 0
							this.getWindowHeight(0)
						})
						this.arrlist.splice(0, 0) //更新数组用的
						this.input_content = '' //清空输入框
						this.img_list = ''
						this.orederId = ''
					}).catch(() => {
						this.orederId = ''
						if (this.userlist.length == 0) {
							uni.showToast({
								title: '暂无会话',
								icon: 'none'
							})
						}
					})
				}
			},
			//获取店铺列表
			async getuserlist() {
				let data = {
					api: 'app.msg.user_mchList',
					userId: this.currentUserId, //当前用户id
					mchId: this.currentMchId, //当前店铺id 如有则会把此店铺排到第一位
				}
				this.$req.post({
					data
				}).then(async (res) => {
					if (res.data.list && res.data.list.length > 0) {

						//店铺列表
						this.userlist = res.data.list

						//如果当前店铺id存在
						if (!this.currentMchId) {
							//重新获取接口返回的店铺id -》这一步感觉没用
							this.topusers = res.data.list[0].mch_id
							this.currentMchId = this.topusers
						}
						//当前店铺id消息 全部已读
						if (this.topusers) {
							this.userlist[this.userlistindex].no_read_num = 0
						}
						//转换参数名
						this.userlist.forEach((item, index) => {
							this.$set(item, 'user_name', item.name)
						})
						if (uni.getStorageSync('pId')) {
							await this.handleChange()
						}
						//获取 当前店铺的聊天记录
						this._newlist()
					} else {
					}
				})
			},
			//返回当前店铺的聊天记录
			_newlist() {
				let data = {
					api: 'app.msg.getMessageList',
					userId: this.currentUserId,
					mchId: this.currentMchId,
					type: this.type,
				}
				this.$req.post({
					data
				}).then(res => {
					this.arrlist = res.data.list
					if (this.arrlist && this.arrlist.length > 0) {
						this.arrlist.forEach(v => {
							try {
								v.content = JSON.parse(v.content)
							} catch (error) {
								// 兼容旧数据 字符串
								v.content = {
									text: v.content
								}
							}
						})
					}
					this.userImg = res.data.userImg
				})
			},
			/**
			 * websocket 开始
			 */
			//连接websocket
			connectSocket() {
				//当前店铺id 不能为空
				if (this.currentMchId == '' || !this.currentMchId) {
					return;
				}

				//websocket请求地址
				let wsurl = ''
				if (this.lkt_type == 'JAVA') {
					let storType = this.LaiKeTuiCommon.getStoreType()
					wsurl = this.LaiKeTuiCommon.LKT_WEBSOCKET + this.currentUserId + '/' + this.currentUserId + '/' +
						storType;
				} else if (this.lkt_type == 'PHP') {
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
				}, );

				//监听WebSocket连接
				this.socketTask.onOpen(function(res) {
					that.reconnectCount = 0
					if (that.reconnectTimer) {
						clearTimeout(that.reconnectTimer)
						that.reconnectTimer = null
					}
					that.heart()
				})

				//监听WebSocket返回数据
				this.socketTask.onMessage(function(res) {
					//初始化data
					let data = {}
					try {
						data = typeof res.data === 'string' ? JSON.parse(res.data) : (res.data || {})
					} catch (err) {
						return
					}

					//PHP通过此方式传值 用户ID
					if (data.client_key) {
						let storType = that.LaiKeTuiCommon.getStoreType()
						let msg = {
							type: 'login', //消息类型
							is_mch_send: '0', //是否店铺发送，用户发送0 店铺发送1
							send_id: that.currentUserId, //发送方id
							client_key: data.client_key, //client_key
							source: storType, //stor_type
						}
						that.sendSocketMessage(msg).then(res => {
						}).catch(res => {
						})
					}

					//获取 当前数据的店铺id/发送者id
					if (!that.currentMchId || that.currentMchId == '') {
						if (data.mch_id) {
							that.currentMchId = data.mch_id
						} else if (data.send_id) {
							that.currentMchId = data.send_id
						}
					}

					//如果data.mch_id存在，即有新增用户与我方聊天
					if (data.mch_id) {
						let found = false;
						for (var i = 0; i < that.userlist.length; i++) {
							let tmpUserid = that.userlist[i].mch_id
							//如果店铺列表中【存在】当前店铺id，更新当前店铺数据
							if (data.mch_id == tmpUserid) {
								//更新用户数据
								that.userlist[i] = data
								found = true;
								break;
							}
							//如果当前店铺id == 发送方ID，则标记已读 
							if (that.currentMchId == tmpUserid) {
								that.userlist[i].no_read_num = 0
							}
						}
						//如果店铺列表中【不存在】当前店铺id，则新增店铺
						if (!found) {
							that.userlist.push(data)
						}
						//强制更新数组
						that.userlist.splice(0, 0)
					} else {
						//这里加一个防止串台的判断  
						setTimeout(() => {
							//如果当前店铺列表只存在一个，获取当前店铺id
							if (that.userlist.length == 1) {
								that.currentMchId = that.userlist[0].mch_id;
							}
							//如果当前店铺id == 发送方ID，获取聊天内容
							if (that.currentMchId && data.send_id && data.send_id == that.currentMchId) {
								try {
									data.content = JSON.parse(data.content)
								} catch (error) {
									// 兼容旧数据 字符串
									data.content = {
										text: data.content
									}
								}
								that.arrlist.push(data);
							}
							//如果当前店铺id == 发送方ID，则标记已读
							for (var i = 0; i < that.userlist.length; i++) {
								let tmpUserid = that.userlist[i].mch_id
								if (that.currentMchId == tmpUserid) {
									that.userlist[i].no_read_num = 0
								}
							}
							//强制更新数组
							that.userlist.splice(0, 0);
						}, 100);
					}
					//更新店铺列表
					that.userlist = that.userlist
					//转换参数名
					that.userlist.forEach((item, index) => {
						that.$set(item, 'user_name', item.name)
					})
					//迫使组件实例重新渲染
					that.$forceUpdate();
				});

				this.socketTask.onError(function(res) {
					//进入重新连接
					that.reconnect();
				})

				// 监听连接关闭 -
				this.socketTask.onClose((e) => {
					clearInterval(that.timer)
					that.timer = ''
					//用于判断Socket是自动断开连接，还是页面关闭断开连接
					if (!this.socketOffType) {
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
					if (this.currentMchId) {
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
				clearInterval(this.timer);
				let that = this;
				this.timer = '';
				let msg = {
					"type": "heartbeat",
				}
				this.timer = setInterval(() => {
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
	/deep/.scrollbox_left_box_conten {
		background-color: #FFFFFF;
	}

	/deep/.uni-system-preview-image {
		z-index: 999999 !important;
	}

	.noList {
		text-align: center;
		margin-top: 40rpx;
	}

	.noFindText {
		color: #888888;
	}

	.noFindImg {
		width: 750rpx;
		height: 394rpx;
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

	/deep/.scrollbox {
		width: 100%;
		// background-color: transparent;
		background-color: #f4f5f6 !important;
	}

	.pages {
		// width: 100%;
		display: flex;
		// background-color: #fff;
	}

	.order-list-box {
		position: absolute;
		width: 100%;
		height: 50%;
		padding-bottom: 36rpx;
		background-color: #FFFFFF;
		border-top-left-radius: 18rpx;
		border-top-right-radius: 18rpx;
		bottom: 0;

		.order-list-content {
			height: 96%;
			overflow-y: auto;
		}

		.tit {
			height: 80rpx;
			background-color: #f4f5f6;
			display: flex;
			align-items: center;
			justify-content: center;
			color: #000;
			font-size: 30rpx;
			border-top-left-radius: 18rpx;
			border-top-right-radius: 18rpx;
			position: relative;

			.cha {
				width: 48rpx;
				height: 48rpx;
				position: absolute;
				top: 10rpx;
				right: 10rpx;
				padding: 10rpx;
				background-size: 50% 50% !important;
			}
		}
	}

	.order-list {
		.order-id {
			white-space: nowrap;
			overflow: hidden;
			text-overflow: ellipsis;
			width: 560rpx;
			margin-bottom: 20rpx;
			color: #000
		}

		image {
			border-radius: 25rpx;
			width: 220rpx;
			height: 220rpx;
		}

		.goods-info {
			display: flex;
			border-bottom: 1rpx solid #eaeaea;

			view {
				view {
					display: flex;
					justify-content: space-between;

					.price {
						display: flex;
						justify-content: end;
						color: #000;
						font-size: 30rpx;
						margin-left: 20rpx;
						font-weight: 800;
						width: 400rpx;
					}

					.product-name,
					.product-spec {
						display: block;
						white-space: nowrap;
						overflow: hidden;
						text-overflow: ellipsis;
						width: 340rpx;

					}

					.product-name {
						font-family: system-ui;
						color: #323232;
					}

					.product-spec {
						color: #999999;
					}
				}

				.controls-but {
					display: flex;
					justify-content: flex-end;
					margin-top: 40rpx;

					.btn-group {
						view:nth-child(1) {
							color: #333333;
							background-color: #f4f4f7;
							border-right: none;
							padding: 12rpx 16rpx;
							font-size: 24rpx;
							border-radius: 14rpx;
							border-top-right-radius: 0px;
							border-bottom-right-radius: 0px;
						}

						view:nth-child(2) {
							color: #333333;
							background-color: #f4f4f7;
							border-right: none;
							padding: 12rpx 16rpx;
							font-size: 24rpx;
							border-radius: 14rpx;
							border-top-left-radius: 0px;
							border-bottom-left-radius: 0px;
						}
					}

				}
			}
		}
	}
</style>
