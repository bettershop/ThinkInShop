<template>
    <div class="box">
        <!--头部模块-->
        <heads :title='language.search2.search.title' :border="true" :bgColor="bgColor" :titleColor="titleColor" :ishead_w="2"></heads>

        <div class='home_navigation'>
            <div class='home_input'>
                <img class='searchImg' :src="serchimg" alt="">
                <input type="text" v-model.trim="searchtxt" :placeholder="placeholder1" @blur="_placeholder()"
                       @focus="_placeholder1()"
                       id='input' :placeholder-style="placeStyle" name="sourch"/>
            </div>
            <div class='search_btn' @tap='_search'>{{language.search2.search.search}}</div>
        </div>

       
        <!--点击搜索框跳转的初始模块-->
        <div class='search'>
            <div v-if='cancel'>
                <div class='search-hot' v-if="storag">
                    <div class='hot'>
                        <div @tap="historyClick">
                            {{language.search2.search.History}}
                            <img v-if="!historyFlag" class="jiantou" :src="jiantouBottom" alt="">
                            <img v-else class="jiantou" :src="jiantouTop" alt="">
                        </div>
                        <!-- 清空历史记录 -->
                        <img class='hot_img1' :src="readimg" @tap='clearhis()'>
                    </div>
                    <template v-if="historyFlag">
                        <ul class='search-ul' ref='record'>
                            <li v-for='(item,index1) in storag' @tap.stop="_record(item)" :key="index1">{{item}}</li>
                        </ul>
                    </template>
                </div>
                <div class='search-hot' v-if='is_open' style="margin-top: 16rpx;">
                    <div class='hot'>
                        <span>{{language.search2.search.Hot}}</span>
                        <img class='hot_img' :src="show1" @tap='hotsearchStatu()' v-if='openHotWord'>
                        <img class='hot_img' :src="hide1" @tap='hotsearchStatu()' v-else>
                    </div>
                    <ul class='search-ul hotSearch1' v-if='openHotWord'>
                        <li v-for='(item,index) in host' @tap.stop="hotsearch(item)" :key="index">{{item}}</li>
                    </ul>
                    <div v-else class="hideHot">{{language.search2.search.Tips[0]}}</div>
                </div>
            </div>
            <!--  输入提示信息   -->
            <ul v-if='!cancel' class='keyword home_search' id="new_ul">
                <li v-for='(item,index2) in prompt' @tap.stop="_record(item)" :key="index2">
                    <img class='serchimg2' :src="serchimg" alt=""><p>{{item}}</p>
                </li>
            </ul>
        </div>
        <!-- 删除提示框 -->
        <view class="tishi_bg" v-if="is_tishi">
            <view class="tishi_nr">
                <view>{{language.search2.search.ts}}</view>
                <view>{{language.search2.search.sfsc}}</view>
                <view class="tishi_btn">
                    <view @tap="_quxiao">{{language.search2.search.qx}}</view>
                    <view @tap="_queren">{{language.search2.search.qd}}</view>
                </view>
            </view>
        </view>
    </div>
</template>

<script>
    import {
        mapMutations
    } from 'vuex'

    export default {
        data () {
            return {
                title: '搜索商品',
                openHotText: '关闭热门搜索',
                openHotWord: false,
                back_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x.png',
                serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
                show: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/show.png',
                show1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/search/search_kai.png',
                hide: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/hide.png',
                hide1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/search/search_guan.png',
                readimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/search/search_del.png',
                jiantouBottom: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/search/search_down.png',
                jiantouTop: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/search/search_top.png',
                sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/home/syss.png',
                cancel: false,
                placeholder: '请输入商品名称',
                placeholder1: '',
                host: '',
                searchtxt: '',
                storag: '', //	接收历史记录的值
                prompt: '', //	接收输入提示的值
                searchgoods: '', //接收搜索后商品返回的数据
                success: '', //搜索商品是否有数据返回
                topTabBar: 0, // 0|1，商品|店铺
                is_open: false,
                placeStyle: 'color:#999999;font-size: 26upx',
                historyFlag: true,
                bgColor:[{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                titleColor:'#333333',
                is_tishi:false,
            }
        },
        onShow: function () {
            this.storag = uni.getStorageSync('history')
            this.openHotWord = uni.getStorageSync('hotStatu')
            this.placeholder1 = this.placeholder
            if (this.openHotWord) {
                this.openHotText = this.language.search2.search.close
            } else {
                this.openHotText = this.language.search2.search.open
            }
            this._axios()
            this.cancel = true
            this.placeholder1 = this.language.search2.search.Tips[3]
        },
        methods: {
            cleardata(){
                this.searchtxt = ''
            },
            historyClick(){
                this.historyFlag = !this.historyFlag
            },
            //输入框失去焦点
            _placeholder () {
                this.placeholder1 = this.language.search2.search.Tips[3]
            },
            //输入框获得焦点
            _placeholder1 () {
                this.placeholder1 = ''
            },
            // 查看热门搜索
            hotsearchStatu () {
                if (this.openHotWord) {
                    this.openHotWord = false
                } else {
                    this.openHotWord = true
                }

                let hotStatus = this.openHotWord
                uni.setStorage({
                    key: 'hotStatu',
                    data: hotStatus
                })
            },
            //从主页点击搜索框，进入搜索界面，热门数据请求
            _axios () {
                var me = this
                var data = {
                    api: 'app.search.hot_search',
                    type: this.topTabBar
                }
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                        me.host = res.data.list
                        if (res.data.is_open == 1) {
                            me.is_open = true
                        } else {
                            me.is_open = false
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
            //点击搜索时，实行的数据请求及处理
            _search () {
                let data = this.storag
                if (this.searchtxt != '') { //搜索框不为空  
                    let me = this
                    // #ifndef MP-WEIXIN
                    if (data.length > 0) {
                        
                        if (!me.in_array(me.searchtxt, data)) {
                            data.unshift(me.searchtxt)
                        } else {
                            var b = data.indexOf(me.searchtxt)
                            data.splice(b, 1)
                            data.unshift(me.searchtxt)
                        }
                        
                    } else {
                        data = []
                        data.unshift(me.searchtxt)
                    }
                    uni.setStorageSync('history', data)
                    me.type(1)
                    me.cancel = false
                    uni.navigateTo({
                        url: 'searchResult?searchtxt=' + me.searchtxt + '&topTabBar=' + me.topTabBar
                    })
                    me.searchtxt = ''

                    // #endif
                    //#ifdef MP-WEIXIN
                    if (data.length > 0) {
                        
                        if (!me.in_array(me.searchtxt, data)) {
                            data.unshift(me.searchtxt)
                        } else {
                            var b = data.indexOf(me.searchtxt)
                            data.splice(b, 1)
                            data.unshift(me.searchtxt)
                        }
                    } else {
                        data = []
                        data.unshift(me.searchtxt)
                    }
                    uni.setStorage({
                        key: 'history',
                        data: data,
                        success: function (res) {
                            uni.navigateTo({
                                url: 'searchResult?searchtxt=' + me.searchtxt + '&topTabBar=' + me.topTabBar,
                                success: function () {
                                    me.type(1)
                                    me.cancel = false
                                    me.searchtxt = ''
                                }
                            })
                        }
                    })
                    //#endif
                } else {
                    uni.showToast({
                        title: this.language.search2.search.Tips[1],
                        duration: 1500,
                        icon: 'none'
                    })
                }
            },
            //判断数组中是否已有当前字符串
            in_array (stringToSearch, arrayToSearch) {
                for (let s = 0; s < arrayToSearch.length; s++) {
                    let thisEntry = arrayToSearch[s].toString()
                    if (thisEntry == stringToSearch) {
                        return true
                    }
                }
                return false
            },
            //清空历史记录
            clearhis(){
                this.is_tishi = !this.is_tishi
            },
            //取消删除历史记录
            _quxiao(){
                this.is_tishi = !this.is_tishi
            },
            //确认删除历史记录
            _queren(){
                this.clearhiss()
            },
            clearhiss() {
                let me = this
                uni.removeStorage({
                    key: 'history',
                    success: function () {
                        me.storag = ''
                        uni.showToast({
                            title: this.language.search2.search.Tips[2],
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
                this.is_tishi = !this.is_tishi
            },
            //点击热门搜索把搜索的记录添加到good-storage中并跳转搜索列表页   
            hotsearch (item) {
                this.searchtxt = item
                this._search()
            },
            //提示信息，历史记录的商品搜素
            _record (item) {
                this.searchtxt = item
                this._search()
            },
            ...mapMutations({
                type: 'SET_TYPE',
            }),
        },
        watch: {
            topTabBar: function (val) {
                // 监听 商品店铺切换动态设置标题
                // 0 商品 1 店铺
                if (val) {
                    this.title = this.language.search2.search.title2
                    return
                }
                this.title = this.language.search2.search.title
            },
            // 监听搜索输入框中值的变化
            searchtxt: function (val) {
                if (val == '') {
                    this.cancel = true
                } else {
                    this.cancel = false
                }
                //监听输入内容向后台请求数据  
                var me = this
                var data = {
                    api: 'app.search.input_search',
                    type: me.topTabBar
                }
                data.keyword = this.searchtxt
                if (this.searchtxt !== '') {
                    this.$req.post({data}).then(res => {
                        if (res.data.length != 0) {
                            me.prompt = res.data
                        } else {
                            me.prompt = false
                        }
                    })
                }
            }
        },
    }
</script>

<style>
    page{
        height: 100vh;
        background-color: #F4F5F6;
    }
</style>

<style lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/search/search.less");
    
</style>
