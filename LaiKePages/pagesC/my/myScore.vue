<template>
    <view class="yh-myscore">

        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>

        <heads :title='language.myScore.title' :ishead_w="ishead_w" :bgColor="bgColor" :titleColor="titleColor"
            :returnR="returnR"></heads>

        <toload v-if="!load" :load="load"></toload>
        <view v-else class="yh-halfWidth2">
            <view class="topBox" >
                <view class="score_head" >
                    <view class="toubu" :style="{backgroundImage: 'url('+ myScore_bj +')'}">
                        <view class='scoreData'>
                             <view class='scoreNum'>{{availableScore || 0}}</view>
                             <view class="yh-kyjf">
                                 <view>{{language.myScore.CurrentAvailablePoints}}</view>
                                 <view class="yh-kyjf_box" @tap='_toGradeUse2(true)'>
                                     <image class="yh-kyjf_img" :src="lang_text=='en'?myScore_sysm_en: myScore_sysm"></image>
                                 </view>
                             </view>
                         </view>
                         <view class="myhr"></view>
                         <view class="jf_box">
                             <view class="jf_box_left">
                                 <view class="jf_box_left_jf">{{scoreNum || 0}}</view>
                                 <view class="jf_box_left_txt">{{language.myScore.availableIntegral}}</view>
                             </view>
                             <view class="jf_box_center_line">
                                
                             </view>
                             <view class="jf_box_right">
                                 <view class="jf_box_right_jf">{{FrozenScore || 0}}</view>
                                 <view class="jf_box_right_txt">{{language.myScore.FrozenScore}}</view>
                             </view>
                         </view>
                    </view>
                </view>
            </view>
            <view class="mask_line"></view>
            <view class='scoreRecode' >
                <view class="recodeTopBox">
                    <view class='recodeTop'>
                        <view @tap='topChange(1)' ><span :class='{active: topStatus==1}'>{{language.myScore.gainDetail}}</span><span v-if='topStatus==1' class='border1'></span></view>
                        <view @tap='topChange(2)' ><span :class='{active: topStatus==2}'>{{language.myScore.useDetail}}</span><span v-if='topStatus==2' class='border1'></span></view>
                        <view @tap='topChange(3)' ><span :class='{active: topStatus==3}'>过期明细</span><span v-if='topStatus==3' class='border1'></span></view>
                    </view>
                    <view class="date-selector" >
                        <view class="date-selector-text" @click="openDatePicker">{{ selectedDate }}<image :src="xjt"></image></view>
                        <view class="screen-selector" @click="openScreenPicker">筛选 <image :src="screen"></image> </view>
                    </view>
                </view>
                <view class='recodeDiv'>
                    <view v-if='topStatus==1' style="border-radius: 0 0 24rpx 24rpx;background-color: #fff;" class="my">
                        <view v-for='(item,index) in list1' :key='index' class='recodeDetail'>
                            <view >
                                <view class='recodeTitle'>
                                    <span>{{item.name}}</span>
                                </view>
                                <view class='recodeTime'>{{item.sign_time}}</view>
                                <view class='recodeNo' v-if="item.sNo != ''">订单号：{{item.sNo}}</view>
                            </view>
                            <view>
                                <view class='recodeScore'>
                                    <span v-if='item.status'>+{{item.sign_score}}</span>
                                    <span v-else class="yh-sign_score">-{{item.sign_score}}</span>
                                </view>
                            </view>
           
                        </view>
                        <uni-load-more v-if='list1.length>5' :loadingType="loadingType1"></uni-load-more>
                        <view class="quesheng" v-if="list1.length<1">
                        <image :src="myScore_qs"></image>
                        <view>{{ language.zdata.zwhqmx }}</view>
                        </view>
                    </view>
        
                    <view v-else-if='topStatus==2' style="border-radius: 0 0 24rpx 24rpx;background-color: #fff;" class="my">
                        <view v-for='(item,index) in list2' :key='index' class='recodeDetail'>
                            <view>
                                <view class='recodeTitle'>
                                    <span>{{item.name}}</span>
                                </view>
                                <view class='recodeTime'>{{item.sign_time}}</view>
                                <view class='recodeNo' v-if="item.sNo != ''">订单号：{{item.sNo}}</view>
                            </view>
                            <view>
                                <view class='recodeScore'>
                                    <span>-{{item.sign_score}}</span>
                                </view>
                            </view>
                        </view>
                        <uni-load-more v-if='list2.length>5' :loadingType="loadingType2"></uni-load-more>
                        <view class="quesheng" v-if="list2.length<1">
                        <image :src="myScore_qs"></image>
                        <view>{{ language.zdata.zwsymx }}</view>
                        </view>
                    </view>
        
                    <view v-else-if='topStatus==3' style="border-radius: 0 0 24rpx 24rpx;background-color: #fff;" class="my">
                        <view v-for='(item,index) in list3' :key='index' class="new-item">
                            <view 
                                class='recodeDetail'
                                :style="{right: item.currentX + 'px'}"
                                @touchstart="_touchstart($event, item)" 
                                @touchmove="_touchmove($event, item)"
                                @touchend="_touchend($event, item)"
                            >
                                <view>
                                    <view class='recodeTitle'>
                                        <span>{{item.name}}</span>
                                    </view>
                                    <view class='recodeTime'>{{item.sign_time}}</view>
                                    <view class='recodeNo' v-if="item.sNo != ''">订单号：{{item.sNo}}</view>
                                </view>
                                <view>
                                    <view class='recodeScore'>
                                        <span class="yh-sign_score">-{{item.sign_score}}</span>
                                    </view>
                                </view>
                            </view>
                            <view class="new-btn" @tap="_del(item)">
                                <view>删除</view>
                            </view>
                        </view>
                        <uni-load-more v-if='list3.length>5' :loadingType="loadingType3"></uni-load-more>
                        <view class="quesheng" v-if="list3.length<1">
                        <image :src="myScore_qs"></image>
                        <view>{{ language.zdata.zwgqmx  }}</view>
                        </view>
                    </view>
        
                </view>
            </view>
        </view>
        <DatePicker 
          :showDatePicker="showDatePicker"
          @confirm="handleDateConfirm"
          @cancel="showDatePicker = false"
        />
        <screenPicker 
          :showScreenPicker="showScreenPicker"
          :dicType="dicType"
          @confirm="handleScreenConfirm"
          @reset="resetScreen"
          @cancel="showScreenPicker = false"
        />    
    </view>
    
</template>

<script>
    import ruleModal from '@/components/ruleModal.vue'
    import DatePicker from './components/datePicker.vue';
    import ScreenPicker from './components/screenPicker.vue';
    export default {
        components: { DatePicker,ruleModal,ScreenPicker},
        data () {
            return {
                topStatus: 1,
                title: '我的积分',
                bgColor: [{
                        item: '#FA5151 '
                    },
                    {
                        item: '#D73B48'
                    }
                ],
                ishead_w: 3,
                titleColor: '#ffffff',
                load: false,
                scoreNum: '',
                FrozenScore:'',//冻结积分
                availableScore: '',//可用积分
                selectedDate: new Date().getFullYear() +'年'+ parseInt(new Date().getMonth() + 1) +'月',
                showDatePicker: false,
                showScreenPicker: false,
                list1: [],
                list2: [],
                list3: [],
                page1: 1,
                page2: 1,
                page3: 1,
                loadingType1: 0,
                loadingType2: 0,
                loadingType3: 0,
                contentNodes:[],
                load: false,
                
                xjt: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xjt.png',
                screen: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/screen.png',
                myScoreimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/integralback.png',// 积分背景
                myScore_bj: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/myScore_bj.png',// 积分背景
                myScore_sysm: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/myScore_sysm.png',// 积分背景
                myScore_sysm_en: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/myScore_sysm_en.png',// 积分背景
                useExp: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sign_rule.png',
                myScore_qs: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/myScore_qs.png',
                loadGif: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/loading.gif',
                sign_bg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sign_bg.png',
                is_tc: false,
                returnR: '',
                content: '',
                signPlugin: 0,//签到插件是否开启
                lang_text:"",
                specify_type: '', // 筛选类型
                specify_sNo: '', // 筛选编号
                sign_time: '', // 筛选值
                dicType:[]
            }
        },
        onLoad(option) {
            this.isLogin(()=>{})
            this.signPlugin = parseInt(option.signPlugin)
            this._axios1()
        },
        onShow () {
            this.lang_text = uni.getStorageSync('language')
            
        },
        watch:{
            topStatus() {
                this.specify_type = '';
                this.sign_time = '';
                this.specify_sNo = '';
                this.refresh()
            }
        },
        methods: {
            handleDateConfirm({ year, month }) {
               this.sign_time = `${year}-${month < 10 ? '0' + month : month}`; // 格式化日期
               this.selectedDate = `${year}年${month}月`;
               this.showDatePicker = false; // 关闭日期选择器
               this.refresh()
            },
            refresh(){
                this.page1 = 1
                this.page2 = 1
                this.page3 = 1
                this.loadingType1 = 0
                this.loadingType2 = 0
                this.loadingType3 = 0
           
                if(this.topStatus == 1) {
                    this._axios1()
                } else if(this.topStatus == 2) {
                    this._axios2()
                } else if(this.topStatus == 3) {
                    this._axios3()
                } else {
                }
            },
            handleScreenConfirm(e){
                this.specify_type = e.specify_type;
                this.specify_sNo = e.specify_sNo;
                this.showScreenPicker = false; 
                this.refresh()
            },
            resetScreen() {
                this.specify_type = '';
                this.specify_sNo = '';
            },
            openDatePicker() {
              this.showDatePicker = true;
            },
            openScreenPicker() {
              this.showScreenPicker = true;
            },
            _touchstart(event, item){
                //记录触摸开始的x坐标
                item.startX = event.touches[0].pageX
            },
            _touchmove(event, item){
                let moveJl = item.startX - event.touches[0].pageX
                //向左移动
                if(moveJl>0){
                    //如果是展开状态，在按钮宽度基础上增加移动距离
                    if(item.moveZK){
                        item.currentX = 70 + moveJl
                    } else {
                        //未展开状态，就等于移动的距离
                        item.currentX = moveJl
                    }
                } else {
                    //如果移动的距离大于0了，才可以向右移动（向右是负数）
                    if(item.currentX > 0){
                        item.currentX = 70 + moveJl
                    }
                }
            },
            _touchend(event, item){
                //移动距离始终等于按钮宽度
                if(item.currentX >= 70){
                    item.currentX = 70
                    item.moveZK = true
                } else {
                    //小于按钮宽度的，不展示出来
                    item.currentX = 0
                    item.moveZK = false
                }
            },
            _del(item){
                uni.showLoading({
                    title: '正在删除...'
                })
                setTimeout(()=>{
                    let data = {
                        api:'app.integral.deleteSign',
                        ids: item.id,
                    }
                    this.$req.post({data}).then(res => {
                        uni.hideLoading()
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                        if(res.code == 200) {
                            this._axios3()
                        }
                    })
                }, 1000)
            },
            _toGradeUse2 (is) {
                uni.navigateTo({
                    url:'/pagesB/regulation/regulation?api='+'plugin.integral.App.Instructions'+'&title='+'积分规则'
                })
            },
            moveHandle: function() {
            	return false;
            },
            //关闭协议弹窗
            _xieyiShow(e){
                    this.is_tc = !this.is_tc
            },
             
            /**
             *
             * */
            changeLoginStatus () {
                this._axios1()
            },
            /**
             *
             * */
            _toGradeUse (is) {
                uni.navigateTo({
                    url: '/pagesC/my/scoreRule'
                })
            },
            /**
             *
             * */
            topChange (num) {
                this.topStatus = num
                uni.pageScrollTo({ scrollTop: 0, duration: 10 })
            },
            /**
             *
             * */
            _axios1() {
                var me = this
                let data = {
                    api:'plugin.integral.App.integral',
                    type: 1,
                    pageNo: this.page1,
                    pageSize: 10,
                    specify_type: this.specify_type, // 筛选类型
                    sNo: this.specify_sNo, // 筛选编号
                    sign_time: this.sign_time, // 筛选值
                }
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                        me.list1 = res.data.list || []
                        me.scoreNum = res.data.score || 0
                        me.FrozenScore = res.data.FrozenScore || 0
                        me.availableScore = Number(res.data.score) - Number(res.data.FrozenScore)
                        me.dicType = res.data.dicType || []
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                    me.load = true
                })
            },
            _axios2() {
                var me = this
                let data = {
                    api:"plugin.integral.App.integral",
                    type: 2,
                    pageNo: this.page2,
                    pageSize: 10,
                    specify_type: this.specify_type, // 筛选类型
                    sNo: this.specify_sNo, // 筛选编号
                    sign_time: this.sign_time, // 筛选值
                }
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                        me.list2 = res.data.list || []
                        me.scoreNum = res.data.score || 0
                        me.FrozenScore = res.data.FrozenScore || 0
                        me.availableScore = Number(res.data.score) - Number(res.data.FrozenScore)
                        me.dicType = res.data.dicType || []
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                    me.load = true
                })
            },
            _axios3() {
                var me = this
                let data = {
                    api:"plugin.integral.App.integral",
                    type: 3,
                    pageNo: this.page3,
                    pageSize: 10,
                    specify_type: this.specify_type, // 筛选类型
                    sNo: this.specify_sNo, // 筛选编号
                    sign_time: this.sign_time, // 筛选值
                }
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                        me.list3 = res.data.list || []
                        me.scoreNum = res.data.score || 0         
                        me.FrozenScore = res.data.FrozenScore || 0
                        me.availableScore = Number(res.data.score) - Number(res.data.FrozenScore)
                        me.list3.forEach((item, index)=>{
                            this.$set(item, 'startX', 0) //触摸开始坐标
                            this.$set(item, 'currentX', 0) //触摸移动距离
                            this.$set(item, 'moveZK', false) //是否显示了按钮
                        })
                        me.dicType = res.data.dicType || []
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                    me.load = true
                })
            }
        },
        computed: {
            halfWidth1: function () {
                var gru = uni.getStorageSync('data_height') ? uni.getStorageSync('data_height') : this.$store.state.data_height
                var heigh = parseInt(gru)
                var he = 0
                // #ifdef MP
                he = 350
                // #endif
                // #ifdef APP-PLUS
                he = 350
                // #endif
                // #ifdef H5
                he = 350
                // #endif
                return uni.upx2px(he) + 'px'
            },
            halfWidth2: function () {
                var gru = uni.getStorageSync('data_height') ? uni.getStorageSync('data_height') : this.$store.state.data_height
                var heigh = parseInt(gru)
                var he = heigh * 2
                // #ifdef MP
                he = 0
                // #endif
                // #ifdef APP-PLUS
                he = heigh * 2
                // #endif
                // #ifdef H5
                // he=heigh*2+44
                he = 0
                // #endif
                return uni.upx2px(he) + 'px'
            }
        },
        onReachBottom: function () {
            var me = this
            if (this.topStatus == 1) {
                if (this.loadingType1 != 0) {
                    return
                }
                this.loadingType1 = 1
                me.page1 += 1
                var data = {
                    api:'plugin.integral.App.loadIntegral',
                    type: 1,
                    pageNo: this.page1,
                    pageSize: 10,
                    specify_type: this.specify_type, // 筛选类型
                    sNo: this.specify_sNo, // 筛选编号
                    sign_time: this.sign_time, // 筛选值
                }
                this.$req.post({data}).then(res => {
                    if(this.page1 == 1){
                        me.list1 = res.data.list
                    } else {
                        me.list1 = me.list1.concat(res.data.list)
                    }
                    if (res.data.list.length > 0) {
                        me.loadingType1 = 0
                    } else {
                        me.loadingType1 = 2
                    }
                })
            } else if (this.topStatus == 2) {
                if (this.loadingType2 != 0) {
                    return
                }
                this.loadingType2 = 1
                this.page2 ++
                var data = {
                    api:"plugin.integral.App.loadIntegral",
                    type: 2,
                    pageNo: this.page2,
                    pageSize: 10,
                    specify_type: this.specify_type, // 筛选类型
                    sNo: this.specify_sNo, // 筛选编号
                    sign_time: this.sign_time, // 筛选值
                }
                this.$req.post({data}).then(res => {
                    if(this.page2 == 1){
                        me.list2 = res.data.list
                    } else {
                        me.list2 = me.list2.concat(res.data.list)
                    }
                    if (res.data.list.length > 0) {
                        me.loadingType2 = 0
                    } else {
                        me.loadingType2 = 2
                    }
                })
            } else if (this.topStatus == 3) {
                if (this.loadingType3 != 0) {
                    return
                }
                this.loadingType3 = 1
                this.page3 ++
                var data = {
                    api:"plugin.integral.App.loadIntegral",
                    type: 3,
                    pageNo: this.page3,
                    pageSize: 10,
                    specify_type: this.specify_type, // 筛选类型
                    sNo: this.specify_sNo, // 筛选编号
                    sign_time: this.sign_time, // 筛选值
                }
                
                this.$req.post({data}).then(res => {
                    if(this.page3 == 1){
                        me.list3 = res.data.list
                    } else {
                        me.list3 = me.list3.concat(res.data.list)
                    }
                    me.list3.forEach((item, index)=>{
                        this.$set(item, 'startX', 0) //触摸开始坐标
                        this.$set(item, 'currentX', 0) //触摸移动距离
                        this.$set(item, 'moveZK', false) //是否显示了按钮
                    })
                    if (res.data.list.length > 0) {
                        me.loadingType3 = 0
                    } else {
                        me.loadingType3 = 2
                    }
                })
                
            }

        },
    }
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped> @import url("@/laike.less");
    @import url("../../static/css/my/myScore.less");
    /deep/.ruleModal{
        .ruleModal-content{
            width: 636rpx !important;
            height: 486rpx !important;
            border-radius: 24rpx !important;
            .ruleModal-content-footer{
                border-top: 0.5px solid #f1f1f1;
            }
        }
    }
</style>
