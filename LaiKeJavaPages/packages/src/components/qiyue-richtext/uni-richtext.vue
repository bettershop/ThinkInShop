<template>
    <view>
        <view class="mask" v-if="showMask" @click="hideInputPopup"></view>

        <view class="popup popup-insert-text" v-if="showInsertTextPopup">
            <view class="toolbar">
                <view class="iconfont icon-bold" @click="toolBarClick('bold')"></view>
                <view class="iconfont icon-italic" @click="toolBarClick('italic')"></view>
                <view class="iconfont icon-configure" @click="toolBarClick('fontsize')"></view>
                <view class="iconfont icon-underline" @click="toolBarClick('underline')"></view>
                <view class="iconfont icon-strike" @click="toolBarClick('strike')"></view>
                <view class="iconfont icon-alignleft" @click="toolBarClick('alignleft')"></view>
                <view class="iconfont icon-aligncenter" @click="toolBarClick('aligncenter')"></view>
                <view class="iconfont icon-alignright" @click="toolBarClick('alignright')"></view>
            </view>
            <view class="input-content">
                <textarea id="isTextarea" :adjust-position="false" :style="{height:textViewHight+'px'}" maxlength="-1" v-model="textareaData"></textarea>
            </view>
            <view style="position: fixed;bottom: 0;width: 100%;background-color: #ffffff;">
                <view style="display: flex;box-shadow: 0 0 10px rgba(0,0,0,.1);">
                    <view style="font-size: 14px;line-height: 40px;width: 50%;text-align: center;"
                        @click="textareaDataSave('input')">{{language.storeGoodsDetail.submit}}
                    </view>
                    <view style="font-size: 14px;line-height: 40px;width: 50%;text-align: center;"
                        @click="textareaDataCancel">{{language.storeGoodsDetail.cancel}}
                    </view>
                </view>
            </view>
        </view>
        <view class="popup popup-bottom" v-if="showPopup">
            <view class="float-popup-bottom">
                <view class="popup-bottom-button" @click="showInsertText">
                    <image :src="insertText" style="width: 48rpx;height: 48rpx;"></image>
                    <span>{{language.storeGoodsDetail.insertText}}</span>
                </view>
                <view class="popup-bottom-button" @click="showEditText">
                    <image :src="insertText" style="width: 48rpx;height: 48rpx;"></image>
                    <span>{{language.storeGoodsDetail.editText}}</span>
                </view>
                <view class="popup-bottom-button" @click="insertRichItem('image')">
                    <image :src="insertImg" style="width: 48rpx;height: 48rpx;"></image>                    
                    <span>{{language.storeGoodsDetail.insertImg}}</span>
                </view>
<!--                <view class="popup-bottom-button" @click="insertRichItem('video')">
                    <image :src="insertImg" style="width: 48rpx;height: 48rpx;"></image>                    
                    <span>插视频</span>
                </view> -->
                <view class="popup-bottom-button" @click="deleteRichItem">
                    <image :src="myStore_del" style="width: 48rpx;height: 48rpx;"></image>                    
                    <span>{{language.storeGoodsDetail.del}}</span>
                </view>
            </view>
        </view>
      
        <view class="content" id="isHeight">
            <!-- 详情商品标题 -->
 
            <!-- 商品内容 -->
            <view class="placeholder-tip" @click="showInputPopup(-1)" v-if="richList.length==0">
                <template v-if="status == 1">
                    {{isShow?language.storeGoodsDetail.textPlaceholdTitle[1]:''}}
                </template>
                <template v-else-if="status == 2">
                    {{isShow?language.storeGoodsDetail.textPlaceholdTitle[2]:''}}
                </template>
                <template v-else>
                    {{isShow?language.storeGoodsDetail.textPlaceholdTitle[3]:''}}
                </template>
            </view>
            <view ref="richtext" v-for="(item,index) in richList" :key="index"
                :style="index==richListIndex?'background:#cce0f2;':''" @click="showInputPopup(index,item)">
                <view v-if="item.tagType=='p'" :style="item.style">{{item.value}}</view>
                <image class="image" v-if="item.tagType=='image'" :src="item.value" mode="widthFix"></image>
                <video v-if="item.tagType=='video'" :src="item.value" controls></video>
            </view>
        </view>
    </view>
</template>

<script>
    import Vue from 'vue'
    export default {
        name: 'uni-richtext',
        data(){
            return {
                showMask: false,
                showPopup: false,
                showInsertTextPopup: false,
                fontSizeList: ['14px', '16px', '20px', '28px', '35px'],
                richListIndex: 0,
                textareaDataType: '',
                textareaData: '',
                textareaDataStyle: 'padding:10px;font-size:14px;',
                textareaDataColor: '',
                tmpTag: '',
                platform: 'android',
                richList: [],
                isShow: true,
                appHight: 0, //当前屏幕高度
                textViewHight: 0, //当前输入框最大高度
                insertImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/insertImg.png',
                insertText: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/insertText.png',
                myStore_del: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/myStore_del.png',
            }
        },
        props: {
            richList1: {
                type: Array,
                default: () => {
                    return []
                }
            },
            myTitle:{
                type: String,
                default:'',
            },
            //富文本提示文字 0:请输入商品介绍 1:请输入商品参数 2:请输入商品品牌
            status:{
                type: String,
                default: '0'
            },
            //富文本上面是否还有其他view，计算高度 例如：商品详情。。。
            is_top_viewHeight:{
                type: Number,
                default: 51
            },
        },
        created() {
            //获取当前屏幕高度
            let app = uni.getSystemInfoSync()
            this.appHight = app.windowHeight
            console.log('当前屏幕高度',this.appHight);
        },
        mounted() {
            //获取元素距离顶部距离
            const query = uni.createSelectorQuery().in(this);
            query.select('#isHeight').boundingClientRect(data => {
                console.log('获取元素距离顶部距离',data.top);
                this.textViewHight = this.appHight - (data.top + 120)
            }).exec();
            
            if (this.$store.state.goodsDetail) {
                this.richList = this.$store.state.goodsDetail;
            } else {
                this.richList = [];
            }
            console.log('richList',this.richList);
            this.setLang();
            console.log('组件挂载完毕');
        },
        onLoad(option) {
            this.platform = uni.getSystemInfoSync().platform
            console.log('onLoad');
        },
        onShow() {
            this.platform = uni.getSystemInfoSync().platform
            console.log('onShow');
        },
        watch: {
            richList: {
                handler(newValue, oldValue) {
                    this.$parent.richList = newValue
                },
                immediate: true,
                deep: true
            },
            textareaDataColor(newValue, oldValue) {
                this.textareaDataStyle = this.textareaDataStyle.replace(/^color:.*;$/, '')
                this.textareaDataStyle += 'color:' + newValue + ';'
            }
        },
        methods: {
            insertRichItem(type) {
                let me = this
                if (type == 'image') {
                    let data = {
                        // module: 'app',
                        // action: 'mch',
                        // m: 'uploadImgs'
                        api:"mch.App.Mch.UploadImgs",
                    }
                    let access_id = Vue.prototype.$store.state.access_id || uni.getStorageSync('access_id')
                    if (access_id) {
                        data.access_id = access_id;
                    }
                    uni.chooseImage({
                        count: 1, //默认9
                        // #ifndef MP-TOUTIAO
                        sizeType: ['original', 'compressed'], //可以指定是原图还是压缩图，默认二者都有
                        // #endif
                        sourceType: ['album', 'camera'], //从相册选择
                        success: res => {
                            uni.showLoading({
                                title: me.language.showLoading.upLoading,
                                mask: true,
                            })
                            uni.uploadFile({
                                url: uni.getStorageSync('url'), //仅为示例，非真实的接口地址
                                filePath: res.tempFilePaths[0],
                                name: 'image',
                                formData: data,
                                success: (uploadFileRes) => {
                                    let resData = {}
                                    // #ifndef H5
                                    if (me.platform == 'android') {
                                        let data1 = uploadFileRes.data.replace('\r\n/g', '')
                                            .replace(/\n/g, '').replace(/\r/g, '').replace(
                                                /\\/g, '')

                                        resData.data = ''
                                        let str = data1.split(':')[3] + ':' + data1.split(':')[
                                            4]
                                        resData.data = str.substr(1, str.length - 3)
                                    } else {
                                        resData = JSON.parse(uploadFileRes.data)
                                    }
                                    // #endif

                                    // #ifdef H5
                                    console.log(uploadFileRes.data)
                                    resData = JSON.parse(uploadFileRes.data)
                                    // #endif
                                    resData.data = resData.data.split(',')[0].replace('"', '')
                                    if (this.richListIndex == -1) {
                                        this.richList.push({
                                            'tagType': 'image',
                                            'value': resData.data,
                                            'style': ''
                                        })
                                    } else {
                                        this.richList.splice(this.richListIndex + 1, 0, {
                                            'tagType': 'image',
                                            'value': resData.data,
                                            'style': ''
                                        })
                                    }

                                },
                                complete: () => {
                                    console.log(me.platform == 'android')
                                    uni.hideLoading()
                                    this.hideInputPopup()
                                }
                            })
                        }
                    })
                } else if (type == 'video') {
                    var urlV = uni.getStorageSync('url')+'&store_type=2'
                    let data = {
                        // module: 'app',
                        // action: 'mch',
                        // m: 'uploadImgs'
                        api:"mch.App.Mch.UploadImgs",
                    }
                    let access_id = Vue.prototype.$store.state.access_id || uni.getStorageSync('access_id')
                    if (access_id) {
                        data.access_id = access_id;
                    }
                    uni.chooseVideo({
                        count: 1,
                        sourceType: ['camera', 'album'],
                        success: res => {
                            uni.showLoading({
                                mask: true,
                            })
                            uni.uploadFile({
                                url: uni.getStorageSync('url'),
                                filePath: res.tempFilePath,
                                name: 'image',
                                formData: data,
                                success: (uploadFileRes) => {
                                    var videoList = JSON.parse(uploadFileRes.data)
                                    if(videoList.code == 200){
                                        if (this.richListIndex == -1) {
                                            this.richList.push({
                                                'tagType': 'video',
                                                'value': videoList.data,
                                                'style': ''
                                            })
                                        } else {
                                            this.richList.splice(this.richListIndex + 1, 0, {
                                                'tagType': 'video',
                                                'value': videoList.data,
                                                'style': ''
                                            })
                                        }
                                    }
                                    console.log('richList222222222222',this.richList);
                                },
                                fail: function(error) {
                                    console.log(JSON.stringify(error))
                                },
                                complete: () => {
                                    uni.hideLoading()
                                    this.hideInputPopup()
                                }
                            })
                        },
                    })
                }
            },
            deleteRichItem() {
                let index = this.richListIndex
                if (index != -1) {
                    this.richList.splice(index, 1)
                    this.hideInputPopup()
                }
            },
            textareaDataCancel() {
                this.textareaData = ''
                this.textareaDataColor = ''
                this.textareaDataStyle = 'padding:10px;font-size:14px'
                this.showInsertTextPopup = false
            },
            textareaDataSave() { //保存文字输入
                if (this.textareaDataType == 'input') //插入文字
                {
                    if (this.richListIndex == -1) {
                        this.richList.push({
                            'tagType': 'p',
                            'value': this.textareaData,
                            'style': this.textareaDataStyle
                        })
                    } else {
                        this.richList.splice(this.richListIndex + 1, 0, {
                            'tagType': 'p',
                            'value': this.textareaData,
                            'style': this.textareaDataStyle
                        })
                    }
                } else if (this.textareaDataType == 'edit') {
                    this.richList[this.richListIndex] = {
                        'tagType': 'p',
                        'value': this.textareaData,
                        'style': this.textareaDataStyle
                    }
                }
                this.textareaData = ''
                this.textareaDataColor = ''
                this.textareaDataStyle = 'padding:10px;font-size:14px'
                this.showInsertTextPopup = false
            },
            toolBarClick(type) { //文字编辑工具栏点击
                switch (type) {
                    case 'bold':
                        if (this.textareaDataStyle.indexOf('font-weight:bold;') != -1) {
                            this.textareaDataStyle = this.textareaDataStyle.replace(/font-weight:bold;/, '')
                        } else {
                            this.textareaDataStyle += 'font-weight:bold;'
                        }
                        break
                    case 'italic':
                        if (this.textareaDataStyle.indexOf('font-style:italic;') != -1) {
                            this.textareaDataStyle = this.textareaDataStyle.replace(/font-style:italic;/, '')
                        } else {
                            this.textareaDataStyle += 'font-style:italic;'
                        }
                        break
                    case 'fontsize':
                        uni.showActionSheet({
                            itemList: this.fontSizeList,
                            success: (res) => {
                                let fontsize = this.fontSizeList[res.tapIndex]
                                this.textareaDataStyle = this.textareaDataStyle.replace(/^font-size:.*px;$/,
                                    '')
                                this.textareaDataStyle += 'font-size:' + fontsize + ';'
                            }
                        })
                        break
                    case 'alignleft':
                        this.textareaDataStyle = this.textareaDataStyle.replace(/^text-align:.*;$/, '')
                        this.textareaDataStyle += 'text-align:left;'
                        break
                    case 'aligncenter':
                        this.textareaDataStyle = this.textareaDataStyle.replace(/^text-align:.*;$/, '')
                        this.textareaDataStyle += 'text-align:center;'
                        break
                    case 'alignright':
                        this.textareaDataStyle = this.textareaDataStyle.replace(/^text-align:.*;$/, '')
                        this.textareaDataStyle += 'text-align:right;'
                        break
                    case 'underline':
                        if (this.textareaDataStyle.indexOf('text-decoration: underline;') != -1) {
                            this.textareaDataStyle = this.textareaDataStyle.replace(/text-decoration: underline;/, '')
                        } else {
                            this.textareaDataStyle = this.textareaDataStyle.replace(/^text-decoration:.*;$/, '')
                            this.textareaDataStyle += 'text-decoration: underline;'
                        }
                        break
                    case 'strike':
                        if (this.textareaDataStyle.indexOf('text-decoration: line-through;') != -1) {
                            this.textareaDataStyle = this.textareaDataStyle.replace(/text-decoration: line-through;/,
                                '')
                        } else {
                            this.textareaDataStyle = this.textareaDataStyle.replace(/^text-decoration:.*;$/, '')
                            this.textareaDataStyle += 'text-decoration: line-through;'
                        }
                        break
                }
            },
            showInsertText() { //显示插入文字编辑框
                this.textareaDataType = 'input'
                this.hideInputPopup()
                this.showInsertTextPopup = true
            },
            showEditText() {



                if (this.richList.length == 0) {
                    this.showInsertText()
                    return
                }
                this.textareaDataType = 'edit'
                this.textareaData = this.tmpTag.value
                this.textareaDataStyle = this.tmpTag.style
                this.hideInputPopup()
                this.showInsertTextPopup = true
            },
            hideInputPopup() {
                
                this.showMask = false
                this.showPopup = false
            },
            showInputPopup(index, tmpTag) {
                console.log('xxxxxxxxxxx',this.myTitle);
                if(this.myTitle=='查看活动'){
                    return
                }
                this.tmpTag = tmpTag
                this.richListIndex = index
                this.showMask = true
                this.showPopup = true
            }
        }
    }
</script>

<style>
    @import 'markdown.css';
    .content {
        padding: 16rpx;
        box-sizing: border-box;
    }

    .popup-bottom-button_box {
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .content_title {
        font-size: 36rpx;
        
        font-weight: 500;
        color: #333333;
        /* margin:0 32rpx; */
        padding: 32rpx 0;
        border-bottom: 1rpx solid rgba(0, 0, 0, .1);
    }

    .placeholder-tip {
        width: 100%;
        font-size: 36rpx;
        color: #999999;
        font-weight: 400;
    }

    .mask {
        position: fixed;
        z-index: 998;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        background-color: rgba(0, 0, 0, .3);
    }

    .popup {
        position: fixed;
        z-index: 999;
        background-color: #ffffff;
        -webkit-box-shadow: 0 0 30rpx rgba(0, 0, 0, .1);
        box-shadow: 0 0 30rpx rgba(0, 0, 0, .1);
    }

    .popup-insert-text {
        width: 100%;
        height: 100vh;
    }

    .popup-bottom {
        bottom: 0;
        width: 100%;
    }
    .float-popup-bottom{
        display: flex;
        flex-wrap:wrap;
        justify-content: space-around;
        align-items: center;
    }
    .popup-bottom-box {
        display: flex;
        justify-content: space-around;
        flex-wrap: wrap;
        position: fixed;
        bottom: 0;
        right: 0;
        width: 100%;
        height: 140rpx;
        z-index: 99;
        background: linear-gradient(180deg, rgba(244,245,246,0) 0%, #F4F5F6 100%);
    }

    .popup-bottom-button {
        width: 20%;
        height: 168rpx;
        font-size: 14px;
        font-weight: 400;
        color: #333333;
        text-align: center;
        line-height: 40px;
        display: flex;
        justify-content: center;
        flex-direction: column;
        align-items: center;
    }

    .popup-bottom-button:last-child {
        /* color: red; */
    }


    .input-content {
        width: 100%;
    }

    .input-content textarea {
        width: 100%;
        padding: 16rpx 24rpx;
        box-sizing: border-box;
        font-size: 14px;
        line-height: 1.5;
    }

    .preview {
        border-top: 1px solid #e0e0e0;
        width: 100%;
    }

    .toolbar {
        width: 100%;
        border: none;
        box-shadow: 0 0px 2px rgba(0, 0, 0, 0.157), 0 0px 2px rgba(0, 0, 0, 0.227);
        display: flex;
        justify-content: space-around;
    }

    .toolbar .iconfont {
        display: inline-block;
        cursor: pointer;
        height: 30px;
        width: 30px;
        margin: 6px 0 5px 0px;
        font-size: 16px;
        padding: 5px 6px 5px 4px;
        color: #757575;
        text-align: center;
        background: none;
        border: none;
        outline: none;
        line-height: 2.2;
        vertical-align: middle;
    }
    
    .newClass{
        border-bottom: initial;
        font-size: 36rpx;
        color: #999999;
        line-height: 48rpx;
    }
</style>
