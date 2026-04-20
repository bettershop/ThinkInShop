<template>
    <div class="container">
        <div class="position_head">
            <heads :title="language.storeMyCli.title" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>

            <div class="headInfo">
                <div class="topTabBar">
                    <div class="width_50" @tap="changeTabBar(true)">
                        <div :class="{ active1: topTabBar }">{{language.storeMyCli.visitorNum}}</div>
                    </div>
                    <div class="width_50" @tap="changeTabBar(false)">
                        <div :class="{ active2: !topTabBar }">{{language.storeMyCli.MyFans}}</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="load" v-if="load">
            <div>
                <img :src="loadGif" />
                <p>{{language.toload.loading}}</p>
            </div>
        </div>
        <!-- 判断中topTabBar为true时为访客数false为我的粉丝 -->
        <div v-else>
            <!-- 今天 -->
            <div v-if="list1.length > 0" class="content">
                <div class="date">
                    {{ time1 }}
                    <span>{{language.storeMyCli.today}}</span>
                </div>
                
                <div v-for="(item, index) in list1" :key="index" class="dateList">
                    <div class="dateListBox">
                        <div class="dateListLeft">
                            <img v-if="item[0]&&item[0].headimgurl" :src="item[0].headimgurl" alt="" />
                            <img v-if="item&&item.headImg" :src="item.headImg" alt="" />
                            <div class="userInfo">
                                <div class="userName" v-if="topTabBar">
                                    <span>{{ item[0].zhanghao }}</span>
                                    <span>{{ item[0].event }}</span>
                                </div>
                                <div class="userName" v-if="!topTabBar">
                                    <span v-if="item&&item.userName">{{ item.userName }}</span>
                                </div>
                                <div class="visitDate" v-if="topTabBar">
                                    <span class="mr_72">{{ item[0].add_time }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="dateListright " v-if="topTabBar">
                            <div class="expand" v-if="item.length > 1" @tap="showMore1(item, index)">
                                <img class="jiantou" :src="jiantou"
                                    :style="'transform: rotate(' + item[0].transform + 'deg)'" />
                            </div>
                        </div>
                        <div class="dateListright " v-else>
                            <div class="expand fansList" @tap="removeFans(item.cid)">
                                {{language.storeMyCli.yc}}
                            </div>
                        </div>
                    </div>
                    <template v-if="item[0]&&item[0].status&&topTabBar">
                        <div v-for="(items, indexs) in item" :key="indexs">
                            <div v-if="indexs != 0" class="dateListLeft">
                                <div class="dateListExpand">
                                    <div class="userInfo">
                                        <div class="userName">
                                            <span>{{ items.zhanghao }}</span>
                                            <span>{{ items.event }}</span>
                                        </div>
                                    </div>

                                    <div class="visitDate">
                                        <span class="mr_72">{{ items.add_time }}</span>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </template>
                </div>
            </div>
            <!-- 昨天 -->
            <div v-if="list2.length > 0" class="content">
                <div class="date">
                    {{ time2 }}
                    <span>{{language.storeMyCli.yesterday}}</span>
                </div>
                <div v-for="(item, index) in list2" :key="index" class="dateList">
                    <div class="dateListBox">
                        <div class="dateListLeft">
                            <img v-if="item[0]&&item[0].headimgurl" :src="item[0].headimgurl" alt="" />
                            <img v-if="item&&item.headImg" :src="item.headImg" alt="" />
                            <div class="userInfo">
                                <div class="userName" v-if="topTabBar">
                                    <span>{{ item[0].zhanghao }}</span>
                                    <span>{{ item[0].event }}</span>
                                </div>
                                <div class="userName" v-if="!topTabBar">
                                    <span v-if="item&&item.userName">{{ item.userName }}</span>
                                </div>
                                <div class="visitDate" v-if="topTabBar">
                                    <span class="mr_72">{{ item[0].add_time }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="dateListright " v-if="topTabBar">
                            <div class="expand" v-if="item.length > 1" @tap="showMore2(index)">
                                <img class="jiantou" :src="jiantou"
                                    :style="'transform: rotate(' + item[0].transform + 'deg)'" />
                            </div>
                        </div>
                        <div class="dateListright " v-else>
                            <div class="expand fansList" @tap="removeFans(item.cid)">
                                {{language.storeMyCli.yc}}
                            </div>
                        </div>
                    </div>
                    <template v-if="item[0]&&item[0].status&&topTabBar">
                        <div v-for="(items, indexs) in item" :key="indexs">
                            <div v-if="indexs != 0" class="dateListLeft">
                                <div class="dateListExpand">
                                    <div class="userInfo">
                                        <div class="userName">
                                            <span>{{ items.zhanghao }}</span>
                                            <span>{{ items.event }}</span>
                                        </div>
                                    </div>

                                    <div class="visitDate">
                                        <span class="mr_72">{{ items.add_time }}</span>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </template>
                </div>
            </div>
            <!-- 前天 -->
            <div v-if="list3.length > 0" class="content">
                <div class="date">
                    {{ time3 }}
                    <span>{{language.storeMyCli.beforeYesterday}}</span>
                </div>
                <div v-for="(item, index) in list3" :key="index" class="dateList">
                    <div class="dateListBox">
                        <div class="dateListLeft">
                            <img v-if="item[0]&&item[0].headimgurl" :src="item[0].headimgurl" alt="" />
                            <img v-if="item&&item.headImg" :src="item.headImg" alt="" />
                            <div class="userInfo">
                                <div class="userName" v-if="topTabBar">
                                    <span>{{ item[0].zhanghao }}</span>
                                    <span>{{ item[0].event }}</span>
                                </div>
                                <div class="userName" v-if="!topTabBar">
                                    <span v-if="item&&item.userName">{{ item.userName }}</span>
                                </div>
                                <div class="visitDate" v-if="topTabBar">
                                    <span class="mr_72">{{ item[0].add_time }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="dateListright " v-if="topTabBar">
                            <div class="expand" v-if="item.length > 1" @tap="showMore3(index)">
                                <img class="jiantou" :src="jiantou"
                                    :style="'transform: rotate(' + item[0].transform + 'deg)'" />
                            </div>
                        </div>
                        <div class="dateListright " v-else>
                            <div class="expand fansList" @tap="removeFans(item.cid)">
                                {{language.storeMyCli.yc}}
                            </div>
                        </div>
                    </div>
                    <template v-if="item[0]&&item[0].status&&topTabBar">
                        <div v-for="(items, indexs) in item" :key="indexs">
                            <div v-if="indexs != 0" class="dateListLeft">
                                <div class="dateListExpand">
                                    <div class="userInfo">
                                        <div class="userName">
                                            <span>{{ items.zhanghao }}</span>
                                            <span>{{ items.event }}</span>
                                        </div>
                                    </div>

                                    <div class="visitDate">
                                        <span class="mr_72">{{ items.add_time }}</span>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </template>
                </div>
            </div>

            <!-- 更早（注释自带更早二字） -->
            <div v-if="list4.length > 0" class="content">
                <div class="date">
                    {{ time4 }}
                </div>
                <div v-for="(item, index) in list4" :key="index" class="dateList">
                    <div class="dateListBox">
                        <div class="dateListLeft">
                            <img v-if="item[0]&&item[0].headimgurl" :src="item[0].headimgurl" alt="" />
                            <img v-if="item&&item.headImg" :src="item.headImg" alt="" />
                            <div class="userInfo">
                                <div class="userName" v-if="topTabBar">
                                    <span>{{ item[0].zhanghao }}</span>
                                    <span>{{ item[0].event }}</span>
                                </div>
                                <div class="userName" v-if="!topTabBar">
                                    <span v-if="item&&item.userName">{{ item.userName }}</span>
                                </div>

                                <div class="visitDate" v-if="topTabBar">
                                    <span class="mr_72">{{ item[0].add_time }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="dateListright " v-if="topTabBar">
                            <div class="expand" v-if="item.length > 1" @tap="showMore4(index)">
                                <img class="jiantou" :src="jiantou"
                                    :style="'transform: rotate(' + item[0].transform + 'deg)'" />
                            </div>
                        </div>
                        <div class="dateListright " v-else>
                            <div class="expand fansList" @tap="removeFans(item.cid)">
                                {{language.storeMyCli.yc}}
                            </div>
                        </div>
                    </div>
                    <template v-if="item[0]&&item[0].status&&topTabBar">
                        <div v-for="(items, indexs) in item" :key="indexs">
                            <div v-if="indexs != 0" class="dateListLeft">
                                <div class="dateListExpand">
                                    <div class="userInfo">
                                        <div class="userName">
                                            <span>{{ items.zhanghao }}</span>
                                            <span>{{ items.event }}</span>
                                        </div>
                                    </div>

                                    <div class="visitDate">
                                        <span class="mr_72">{{ items.add_time }}</span>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </template>
                </div>
            </div>
            <uni-load-more v-if="(list1.length>9&&list2.length>9&&list3.length>9&&list4.length>9)||listNum > 11" :loadingType="loadingType"></uni-load-more>
            <div class="wsj_box"
                v-if="list1.length == 0 && list2.length == 0 && list3.length == 0 && list4.length == 0">
                <img class="wsj_img" :src="topTabBar?myCli_noBrowsing:myCli_loadFailed" />
                <div>{{topTabBar?language.storeMyCli.noListFans:language.storeMyCli.noListVisitor}}</div>
            </div>
        </div>
        <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    {{language.auction.yccg}}
                </view>
            </view>
        </view>
          <showToast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" @richText="richText" @confirm="confirm"></showToast>
    </div>
</template>

<script>
    import showToast from '@/components/aComponents/showToast.vue'
    export default {
        data() {
            return {
                loadGif: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/loading.gif',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jianX.png',
                textImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yhqBg.png',
                title: '我的客户',

                load: true,
                shop_id: '',
                list1: [], //今天
                list2: [], //昨天
                list3: [], //前天
                list4: [], //更早
                time1: '',
                time2: '',
                time3: '',
                time4: '',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                wushuju: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wukehu.png',
                myCli_noBrowsing: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/myCli_noBrowsing.png', //暂无访客图
                myCli_loadFailed: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/myCli_loadFailed.png', //暂无粉丝图

                page: 1,
                listNum: 0,
                loadingType: 0,
                is_sus:false,
                topTabBar: true,
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
                is_showToast_obj: {
                    
                }, //imgUrl提示图标  title提示文字
                scid:'',
            };
        },
        onLoad(option) {

            this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
            this._axios(this.topTabBar);
        },
        components:{
            showToast
        },
        onReachBottom: function() {
            if (this.loadingType != 0) {
                return;
            }
            this.loadingType = 1;
            this.page++;
            this._axios(this.topTabBar);
        },
        methods: {
            changeTabBar(type) {
                this.list1 = [], //今天
                    this.list2 = [], //昨天
                    this.list3 = [], //前天
                    this.list4 = [], //更早
                    this.loadingType = 0;
                this.page = 1;
                this.listNum = [];
                this.load = true; //预加载参数
                if (type) {
                    this.topTabBar = true;
                } else {
                    this.topTabBar = false;
                }

                this._axios(this.topTabBar);

            },
            showMore1(item, index) {
                if (this.list1[index][0].status) {
                    this.list1[index][0].status = false;
                    this.list1[index][0].transform = 0;
                } else {
                    this.list1[index][0].status = true;
                    this.list1[index][0].transform = 180;
                }
            },
            showMore2(index) {
                if (this.list2[index][0].status) {
                    this.list2[index][0].status = false;
                    this.list2[index][0].transform = 0;
                } else {
                    this.list2[index][0].status = true;
                    this.list2[index][0].transform = 180;
                }
            },
            showMore3(index) {
                if (this.list3[index][0].status) {
                    this.list3[index][0].status = false;
                    this.list3[index][0].transform = 0;
                } else {
                    this.list3[index][0].status = true;
                    this.list3[index][0].transform = 180;
                }
            },
            showMore4(index) {
                if (this.list4[index][0].status) {
                    this.list4[index][0].status = false;
                    this.list4[index][0].transform = 0;
                } else {
                    this.list4[index][0].status = true;
                    this.list4[index][0].transform = 180;
                }
            },
            // 取消弹窗
            richText(){
                this.is_showToast=0
            },
            // 确认弹窗
            confirm(){
                this.is_showToast=0
                let data = {
                    api:"mch.App.Mch.RemoveFans",
                    cid: this.scid
                }
                let me=this
                me.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        me.is_sus = true
                        me.list1 = [], //今天
                        me.list2 = [], //昨天
                        me.list3 = [], //前天
                        me.list4 = [], //更早
                        me.listNum = [];
                        me.loadingType = 0;
                        me.page = 1;
                        me.load = true; //预加载参数
                        me._axios(false)
                        setTimeout(() => {
                            me.is_sus = false
                        }, 1000)
                    }else{
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                
                })
            },
            removeFans(cid) {
                this.scid =  cid
                this.is_showToast = 4
                this.is_showToast_obj.title = this.language.showModal.hint
                this.is_showToast_obj.content = this.language.showModal.qdyycgfsm
                this.is_showToast_obj.button = this.language.showModal.cancel
                this.is_showToast_obj.endButton = this.language.showModal.confirm
                
            },
            _axios(type) {
                let data = {}
                if (type) {
                    data = {
                       
                        api:"mch.App.Mch.Shop_customer",
                        shop_id: this.shop_id,
                        page: this.page
                    }
                } else {
                    data = {
                        api:"mch.App.Mch.ShopFans",
                        shop_id: this.shop_id,
                        page: this.page
                    }
                }
                this.$req
                    .post({
                        data
                    })
                    .then(res => {
                        let {
                            code,
                            data,
                            message
                        } = res;
                        this.load = false;
                        if (code == 200) {
                            this.listNum += Number(data.num);
                            if (Number(data.num) > 9) {
                                this.loadingType = 0;
                            } else {
                                this.loadingType = 2;
                            }
                            if (Array.isArray(data.list)) {
                                return
                            }

                            if (data.list.list1 && data.list.list1.res && data.list.list1.res.length > 0) {
                                let list1 = data.list.list1.res;
                                if(type){
                                   for (let i = 0; i < list1.length; i++) {
                                       list1[i][0].transform = 0;
                                   } 
                                }
                                this.list1 = list1;
                                this.time1 = data.list.list1.time;
                            }

                            if (Array.isArray(data.list.list2)) {
                                return
                            }

                            if (data.list.list2 && data.list.list2.res && data.list.list2.res.length > 0) {
                                let list2 = data.list.list2.res;
                                if(type){
                                   for (let i = 0; i < list2.length; i++) {
                                       list2[i][0].transform = 0;
                                   } 
                                }
                                this.list2 = list2;
                                this.time2 = data.list.list2.time;
                            }

                            if (Array.isArray(data.list.list3)) {
                                return
                            }

                            if (data.list.list3 && data.list.list3.res && data.list.list3.res.length > 0) {
                                let list3 = data.list.list3.res;
                                if(type){
                                   for (let i = 0; i < list3.length; i++) {
                                       list3[i][0].transform = 0;
                                   } 
                                }
                                this.list3 = list3;
                                this.time3 = data.list.list3.time;
                            }

                            if (Array.isArray(data.list.list3)) {
                                return
                            }

                            if (data.list.list4 && data.list.list4.res && data.list.list4.res.length > 0) {
                                let list4 = data.list.list4.res;
                                if(type){
                                   for (let i = 0; i < list4.length; i++) {
                                       list4[i][0].transform = 0;
                                   } 
                                }
                                this.list4 = data.list.list4.res;
                                this.time4 = data.list.list4.time;
                            }
                        } else {
                            if (message) {
                                uni.showToast({
                                    title: message,
                                    duration: 1500,
                                    icon: 'none'
                                });
                            }

                        }
                    });
            }
        },
        computed: {
            now: function() {
                var year = new Date().getFullYear();
                var month = new Date().getMonth() + 1;
                var date = new Date().getDate();
                var date1 = year + '-' + month + '-' + date;
                return date1;
            }
        }
    };
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url('../../static/css/myStore/myCli.less');
</style>
