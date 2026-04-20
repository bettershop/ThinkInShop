<template>
    <div class='box systemMesage'>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.message.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>

        <toload :load="load">

            <div class="m_top">
                <div class="top_text">{{language.message.share}}
                    <text class="red">{{total}}</text>
                    {{language.message.Messages}}，{{language.message.Yes}}
                    <text class="red">{{noread}}</text>
                    {{language.message.Unread}}
                </div>
                <div v-if="noread>0" class="top_read flex1" @tap="readAll"><img class="top_readimg" :src="read">
                </div>
                <div v-else class="top_read flex1" style="color: #c2c2c2;"><img class="top_readimg" :src="readed">
                </div>
                <div class="top_read top_ed" v-if="opeshow&&total>0" v-show="list.length" @click="onEdit">
                    {{language.message.edit}}</div>
                <div class="top_read top_ed" v-if="!opeshow&&total>0" @click="onYes">{{language.message.complete}}</div>
            </div>

            <ul class="systemMesage-ul" v-if="total>0" style="background-color: #f6f6f6;padding-top: 120rpx">
                <li v-for="(item,index) in list" @tap.prevent='_detailedMasege(item,index)' :key="index">
                    <!-- 时间 -->
                    <span class="time">{{item.time}}</span>

                    <div class="lk-crad">

                        <div class="lk-label" v-if="!opeshow">
                            <image v-if="item.show" @tap.stop="setSelected(index)" class="lk-label-image"
                                :src="selectimg"></image>
                            <div v-if="!item.show" @tap.stop="setSelected(index)" class="lk-label-divs"></div>
                        </div>

                        <!-- 主体 -->
                        <div class="content">
                            <view style="display: flex;align-items: center;">
                                <span class="title">{{language.message.System_message}}</span>
                                <span class="new_s" v-if='item.type==1'>NEW</span>
                            </view>
                            <div class="con">{{item.content}}</div>
                            <div class="border"></div>
                            <div class='syatem-bottom'>
                                <span>{{language.message.View_details}} </span>
                                <img :src="jiantou">
                            </div>
                        </div>

                    </div>
                </li>
                <div v-if="!opeshow" style="height: 98rpx;"></div>
            </ul>
            <div class='noFindDiv' v-else>
                <div>
                    <img :src='noCol' class='noFindImg' />
                </div>
                <span class='noFindText'>{{language.message.no_message}}！</span>
            </div>

            <div class="messagetail" v-if="!opeshow&&list&&list.length>0">
                <div class="lk-label chec" @click="AllSetSelected">

                    <div v-if="!selectall" class="lk-label-div"></div>
                    <image v-else class="lk-label-img" :src="selectimg"></image>
                    <span class="lk-label-span">{{language.message.Select_all}}</span>

                </div>
                <div class="btn" @tap="del">
                    {{language.message.deletes}}
                </div>
            </div>
        </toload>
        <!-- 提示 -->
        <show-toast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj"></show-toast>
    </div>
</template>

<script>
    import showToast from "@/components/aComponents/showToast.vue"
    import {
        mapMutations
    } from 'vuex'

    export default {
        data() {
            return {
                //提示
                is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
                is_showToast_obj: {}, //imgUrl提示图标  title提示文字
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                //
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                readed: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/readed.png',
                noCol: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/nomeaasge.png',
                newimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/newgreen.png',
                read: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/readgreen.png',
                selectimg:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +  'images/icon/new_red_select.png',
                title: '消息',
                list: [],
                total: '',
                noread: '',
                page: 2,
                load: false,
                loadingType: 0,
                opeshow: true, // 12-13 编辑true 完成!false 隐藏显示控制
                isChick: true,
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ]
            }
        },
        components: {
            showToast
        },
        computed: {
            selectall() {
                let flag = false

                if (this.list && this.list.length > 0) {
                    let flag1 = this.list.some(item => {
                        return item.show == false
                    })

                    flag = !flag1
                }

                return flag
            }
        },
        onReachBottom() {
            var me = this
            if (me.loadingType != 0) {
                return
            }
            me.loadingType = 1
            var data = {
                api: 'app.message.more',
                page: me.page
            }
            if (me.list.length > 0) {
                this.$req.post({
                    data
                }).then(res => {
                    let data = res.data.message
                    me.page ++
                    if (res.code == 200) {
                        if (data.length > 0) {

                            for (let t of data) {
                                t.show = false
                            }

                            me.list.push(...data)
                            me.loadingType = 0
                        } else {
                            me.loadingType = 3
                        }
                    } else {
                        me.loadingType = 2
                    }
                })
            }
        },
        onShow() {
            if (this.load) {
                this.page = 2
                this.getNoReadAndTotal()
            }

        },
        mounted() {
            this.loadingType = 0
            this.wallets()
        },
        methods: {
            ...mapMutations({
                status: 'data_height'
            }),
            changeLoginStatus() {
                this.wallets()
            },
            /*
             * @func 全部选中
             * @docs
             * 2019-7-31 17:53:28
             * */
            AllSetSelected() {
                let flag = !this.selectall
                for (let t of this.list) {
                    t.show = flag
                }
            },
            /*
             * @func 选中
             * @param { Number } i 当前选中下标 
             * @docs 
             * 2019-7-31 16:53:33
             * */
            setSelected(i) {
                this.list[i].show = !this.list[i].show
                // this.list[i].type = 0
            },
            /*
             * @func 完成事件
             * @return { undefined }
             * @docs 删除完成作用返回
             * 2019-7-31 14:35:15
             * */
            onYes() {
                this.isChick = true
                this.opeshow = !this.opeshow
            },
            /*
             *	@func 编辑事件
             * @return { undefined }
             * @docs 用于切换编辑状态
             * 2019-7-30 17:47:36 
             * */
            onEdit() {
                this.isChick = false
                this.opeshow = !this.opeshow
            },
            /**
             * 删除选中订单
             * @return { undefined }
             * */
            del() {
                let {
                    list
                } = this
                let ids = ''
                let i = 0
                for (let item of list) {
                    if (item.show) {
                        ids += item.id + ','
                        i++
                    }
                }

                if (i <= 0) {
                    uni.showToast({
                        title: this.language.message.wantDel,
                        icon: 'none',
                        duration: 1500
                    })
                    return
                }

                //传空 删除所有消息 不是 删除当前页面
                if (this.selectall) {
                    ids = ''
                }

                let data = {
                    api: 'app.message.del',
                    id: ids
                }
                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        this.is_showToast = 1
                        this.is_showToast_obj.imgUrl = this.sus
                        this.is_showToast_obj.title = this.language.zdata.sccg
                        setTimeout(()=>{
                            this.is_showToast = 0
                        },1000)
                        this.wallets()
                    }
                })
            },
            getNoReadAndTotal() {
                let data = {
                    api: 'app.message.index',
                }


                this.$req.post({
                    data
                }).then(res => {
                    if (res.code === 200) {
                        res = res.data
                        for (let t of res.message) {
                            t.show = false
                        }

                        this.list = res.message
                        this.noread = res.noread ? res.noread : 0
                        this.total = res.total ? res.total : 0
                        if (res.message.length > 0) {
                            this.loadingType = 0
                        } else {
                            this.loadingType = 2
                        }
                    }
                })
            },
            /**
             * 获取订单数据
             *
             * */
            wallets() {
                let data = {
                    api: 'app.message.index',
                }

                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        res = res.data
                        this.load = true
                        // 添加判断
                        this.list = []
                        for (let t of res.message) {
                            t.show = false
                        }

                        this.list.push(...res.message)
                        this.noread = res.noread ? res.noread : 0
                        this.total = res.total ? res.total : 0
                    } else if (res.code == 404) {
                        uni.showToast({
                            title: this.language.message.noLogin,
                            icon: 'none',
                            duration: 1500
                        })
                        setTimeout(function() {
                            uni.navigateTo({
                                url: '../../pagesD/login/newLogin?landing_code=1',
                            })
                        }, 1500)
                    } else if (res.code == 115) {
                        uni.showToast({
                            title: this.language.message.timeout,
                            icon: 'none',
                            duration: 1500
                        })
                        setTimeout(function() {
                            uni.navigateTo({
                                url: '../../pagesD/login/newLogin?landing_code=1',
                            })
                        }, 1500)
                    } else {
                        uni.showToast({
                            title: this.language.message.failGet,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
            },
            _detailedMasege: function(item, i) {
                // 每次跳转消息详情，清除该条new标签
                if (this.isChick) {
                    if (this.noread !== 0 && item.type == 1) {
                        this.noread--
                    }
                    this.list[i].type = 2
                    uni.navigateTo({
                        url: 'detailedMesage?id=' + item.id
                    })
                    return
                }


            },
            /**
             * 消息全部已读
             * 2019-8-2 11:07:13
             **/
            readAll: function() {
                if (this.list) {
                    // 判断是否有未选中
                    let ult = this.list.filter(res => res.type == '1')
                    if (!ult.length) {
                        uni.showToast({
                            title: this.language.message.nounread,
                            duration: 3000,
                            icon: 'none'
                        })
                        return
                    }

                    let data = {
                        api: 'app.message.all',}
                    this.$req.post({
                        data
                    }).then(res => {
                        if (res.code == 200) {
                            this.wallets()
                        }
                    })
                }
            },
        }
    }
</script>

<style lang="less" scoped> 
    @import url("@/laike.less");
    @import url("../../static/css/message/systemMesage.less");
</style>
