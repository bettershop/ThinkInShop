<template>
    <div class="body">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads v-if="load" :title="language.shop.sign.title"></heads>
        <div class="load" v-if="load">
            <div>
                <img :src="loadGif" />
                <p>{{language.shop.sign.Loading}}</p>
            </div>
        </div>
        <div class="sign_top" v-if="!load" :style="{backgroundImage: 'url('+ sign_bg +')'}">
            <heads :title="language.shop.sign.title" navWhite='true' ishead_w="1"></heads>
            <div class="sign_head">
                <div class="sign_one">
                    <div class="sign_left" @tap="toScore">
                        <img class="img" :src="imgurl" />
                        <span>{{language.shop.sign.Current}}：</span>
                        <span class="span">{{ score ? score : 0 }}</span>
                    </div>
                    <div class="sign_right" @tap="_complete_report">
                        <img class="img" :src="sign_rule" />
                        {{language.shop.sign.ruel}}
                    </div>
                </div>
                <div class="relative">
                    <div :class="sign_status == 0 ? 'lose' : 'active'" id="signBtn" @tap="_sign">{{ sign_status == 0 ? language.shop.sign.todaySigning : language.shop.sign.signIn }}</div>
                    <div v-if="sign_status != 0" class="signTextDown"></div>
                </div>
                <p>{{language.shop.sign.Tips[0]}}{{ num ? num : 0 }}{{language.shop.sign.day}}，{{language.shop.sign.Tips[1]}}{{ sign_num.length }}{{language.shop.sign.day}}</p>
            </div>
        </div>
        <div v-if="!load" class="signDayDiv">
            <div class="signDay">
                <div class="sign7Div" v-for="(index, item) in sign7" :key="item">
                    <div :class="num + 1 > index ? 'signDayBC1' : 'signDayBC2'">
                        <div :class="num + 1 > index ? 'signDayText1' : 'signDayText2'" :style="{ marginBottom: signTextTop }">{{ index }}</div>
                        <img v-if="index != 7" :src="num + 1 > index ? sign_right : sign_core" :class="num + 1 > index ? 'signDayImg1' : 'signDayImg2'" />
                        <img v-else :src="sign_gift" :class="num + 1 > index ? 'signDayImg1' : 'signDayImg2'" />
                    </div>
                    <div class="signDownText">{{ index }}{{language.shop.sign.day}}</div>
                </div>
            </div>
        </div>
        <div v-if="!load" class="calendar">
            <!--  日历头部     -->
            <div class="date">
                <span style="margin: 0 40rpx;">{{ time.year }}-{{ time.month > 9 ? time.month : '0' + time.month }}</span>
            </div>

            <div class="month">
                <!--  周几     -->
                <div class="week">
                    <div>{{language.shop.sign.week[0]}}</div>
                    <div>{{language.shop.sign.week[1]}}</div>
                    <div>{{language.shop.sign.week[2]}}</div>
                    <div>{{language.shop.sign.week[3]}}</div>
                    <div>{{language.shop.sign.week[4]}}</div>
                    <div>{{language.shop.sign.week[5]}}</div>
                    <div>{{language.shop.sign.week[6]}}</div>
                </div>
                <!--   每月对应的日期    -->
                <ul>
                    <li v-for="is in dat1" :key="is.id"></li>
                    <li v-for="(i, index) in dat2" :key="index">
                        <div :class="time.day == index + 1 ? 'today' : ''">
                            <span>
                                <i v-if="time.day == index + 1" class="w_100" :class="signData[index] ? 'sign_in' : ''" :style="{fontSize: languageType=='en_US'?'22rpx':''}">{{language.shop.sign.today}}</i>
                                <i v-else class="w_100" :class="signData[index] ? 'sign_in' : ''">{{ index + 1 }}</i>
                            </span>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
        <div class="complete" v-if="showFliter">
            <div class="complete_tishi">
                <img :src="guanbi" class="closeBtn" @tap="_close" />
                <img class="sign_suc" :src="sign_suc" />
                <p class="complete_text">{{language.shop.sign.Tips[2]}}!</p>
                
                <p v-if="is_many_time!=1">
                    {{language.shop.sign.get}}
                    <span class="color_ff3">{{ sign_score }}</span>
                    {{language.shop.sign.integral}},{{language.shop.sign.Tips[0]}}
                    <span class="color_ff3">{{ num }}</span>
                    {{language.shop.sign.day}}
                </p>
                
                <template v-else>
                    <p>
                        {{language.shop.sign.Section}}<span class="color_ff3">{{score_num - surplus_sign_num}}</span>{{language.shop.sign.Tips[3]}}
                        <span class="color_ff3">{{ sign_score }}</span>
                        {{language.shop.sign.integral}}
                    </p>
                    
                    <p v-if="surplus_sign_num>0">
                        {{language.shop.sign.Tips[4]}}
                        <span class="color_ff3">{{ surplus_sign_num }}</span>
                        {{language.shop.sign.second}}，{{language.shop.sign.Tips[6]}}
                        <span class="color_ff3">{{distance_next_h}}：{{distance_next_m}}：{{distance_next_s}}</span>
                    </p>
                    <p v-else>
                        {{language.shop.sign.Tips[5]}}
                    </p>
                </template>
            </div>
        </div>
        <div class="mask" v-if="complete_report">
            <div class="maskContent">
                <div class="maskContentTitle">{{language.shop.sign.ruel}}</div>
                <view class="richBox"><rich-text class="richtext font_14" :nodes="details"></rich-text></view>
                <div class="closeBtnDiv"><button class="closeDiv" type="button" @tap="_oncomplete_report">{{language.shop.sign.got}}</button></div>
            </div>
        </div>
    </div>
</template>
<script> 
import htmlParser from '../../common/html-parser.js';

export default {
    data() {
        return {
            details: '',
            title: '每日签到',
            sign7: [1, 2, 3, 4, 5, 6, 7],
            signData: [],
            sign_right: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sign_right.png',
            sign_rule: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sign_rule.png',
            sign_suc: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sign_suc.png',
            sign_core: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sign_core.png',
            sign_gift: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sign_gift.png',
            back2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x.png',
            bback: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/bback.png',
            flag: true,
            guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
            chengg: uni.getStorageSync('endurl') + 'images/icon/qiandaochengg2x.png',
            report_t: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/qiandao2x.png',
            report_f: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yiqiandao.png',
            loadGif: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/loading.gif',
            sign_bg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sign_bg.png',
            
            dat: new Date(),
            dat1: '',
            dat2: '',
            time: {
                year: '',
                month: '',
                day: ''
            },
            load: true,
            data_day: '',
            daySign: '', //时候签到
            num: '', //连续签到天数
            showFliter: false, //签到显示界面
            sign_score: '', //签到积分
            score: '', //当前积分
            sign_status: '', //判断当天是否签到
            
            showSign: [],
            sign_num: '',
            complete_report: false,
            active: '点击签到',
            lose: '今日已签',
            ss: 1,
            sign_status1: true,
            signFinish: true,
            imgurl: '',
            returnR: '',
            
            surplus_sign_num: '',
            distance_next_h: '',
            distance_next_m: '',
            distance_next_s: '',
            distance_next_time: '',
            
            is_many_time: '', //是否开启当天多次签到
            score_num: '', // 每天可签到次数
        };
    },
    onLoad(option) {
        this.time.year = this.dat.getFullYear();
        this.time.month = this.dat.getMonth() + 1;
        this.time.day = this.dat.getDate();
        
        let year = new Date().getFullYear();
        this.dat1 = new Date(year, this.time.month - 1, 1).getDay();
        this.dat2 = new Date(year, this.time.month, 0).getDate();
    },
    onShow() {
        this.signFinish = true;

        
        this._axios();
    },
    computed: {
        today: function(index) {
            return this.timeDay == index + 1 && this.timeMonth == this.datMonth + 1;
        },
        timeDay: function(index) {
            return this.time.day == index + 1;
        },
        timeMonth: function() {
            return this.time.month;
        },
        datMonth: function() {
            return this.dat.getMonth();
        },
        signTextTop: function() {
            // #ifndef MP-WEIXIN
            return uni.upx2px(0) + 'px';
            // #endif
        }
    },
    methods: {
        /* 
             跳转至我的积分
             */
        toScore() {
            const pages = getCurrentPages();
            if (
                (pages.length == 2 && pages[0].route == 'pages/shell/shell?pageType=home') ||
                (pages.length == 3 && pages[0].route == 'pages/shell/shell?pageType=home' && pages[1].route == 'pages/shell/shell?pageType=home')
            ) {
                uni.navigateTo({
                    url: '/pagesC/my/myScore'
                });
            } else {
                uni.navigateBack({
                    delta: 1
                });
            }
        },
        changeLoginStatus() {
            this._axios();
        },
        in_array(stringToSearch, arrayToSearch) {
            for (let s = 0; s < arrayToSearch.length; s++) {
                let thisEntry = arrayToSearch[s].toString();
                if (thisEntry == stringToSearch) {
                    return true;
                }
            }
            return false;
        },
        _back() {
            this.flag = false;
            uni.navigateBack({
                delta: 1
            });
        },
        _complete_report() {
            this.complete_report = true;
        },
        _oncomplete_report() {
            this.complete_report = false;
        },
        getremainTime(){
            var distance_next_time = this.distance_next_time
            
            if(((!distance_next_time) && distance_next_time!==0) || distance_next_time<0){
                return
            }
            
            this.distance_next_h = parseInt(distance_next_time / 3600)
            
            let remainT = distance_next_time % 3600
            
            this.distance_next_m = parseInt(remainT / 60)
            
            this.distance_next_s = remainT % 60
            
            if(this.distance_next_h<10){
                this.distance_next_h = '0' + this.distance_next_h
            }
            
            if(this.distance_next_m<10){
                this.distance_next_m = '0' + this.distance_next_m
            }
            
            if(this.distance_next_s<10){
                this.distance_next_s = '0' + this.distance_next_s
            }
            
            setTimeout(()=>{
                this.distance_next_time--
                this.getremainTime()
            },1000)
        },
        _sign() {
            if (this.sign_status == 0) {
                return;
            }

            if (!this.signFinish) {
                return;
            }

            this.signFinish = false;
            this.isLogin(()=>{
				if (this.sign_status == 1) {
				    var data = {
				        api: 'app.sign.index',
				    };
				
				    this.$req
				        .post({ data })
				        .then(res => {
				            let { score, sign_score, num, distance_next_time, surplus_sign_num } = res.data;
				            if (res.code == 200) {
				                this.showFliter = true;
				                this.sign_score = sign_score;
				                this.num = num;
                                
                                this.surplus_sign_num = surplus_sign_num
                                
                                this.distance_next_time = Number(distance_next_time)
                                
                                this.getremainTime()
				            } else if (res.code == 404) {
				                uni.showToast({
				                    title: res.message,
				                    duration: 1500,
				                    icon: 'none'
				                });
				                setTimeout(function() {
				                    uni.navigateTo({
				                        url: '/pagesD/login/newLogin?landing_code=1'
				                    });
				                }, 1500);
				            } else {
				                this.signFinish = true;
				                uni.showToast({
				                    title: res.message,
				                    duration: 1500,
				                    icon: 'none'
				                });
				            }
				        })
				        .catch(error => {
				            this.signFinish = true;
				        });
				}
			})
        },
        _close() {
            this.showFliter = false;
            this._axios();
        },
        _axios() {
            this.signData = [];
            var data = {
                api: 'app.sign.sign',
                year: this.time.year,
                month: this.time.month,
                
            };

            this.$req.post({ data }).then(res => {
                if (res.code == 200) {
                    setTimeout(() => {
                        this.load = false;
                    }, 200);

                    let { score, sign_status, num, sign_time, imgurl, details, is_many_time, score_num } = res.data;
                    
                    this.is_many_time = is_many_time
                    this.score_num = score_num

                    this.sign_status = sign_status;
                    if (sign_status == 0) {
                        this.sign_status1 = false;
                    }
                    this.score = score;
                    this.num = num;
                    this.sign_num = sign_time;
                    this.imgurl = imgurl;
                    this.details = htmlParser(details);
                    let data_day = [];
                    
                    let year = new Date().getFullYear();
                    var data_t = new Date(year, this.time.month, 0).getDate();
                    if (sign_time.length > 0) {
                        for (var i = 0; i < data_t; i++) {
                            var data_f = `${year}-${this.dat.getMonth() + 1}-${i + 1}`;
                            data_day.push(data_f);
                            for (var f = 0; f < sign_time.length; f++) {
                                if (data_f == sign_time[f]) {
                                    this.showSign.push(sign_time[f]);
                                }
                            }
                        }
                        this.data_day = data_day;
                    } else {
                        for (var i = 0; i < data_t; i++) {
                            var data_f = `${year}-${this.dat.getMonth() + 1}-${i + 1}`;
                            data_day.push(data_f);
                        }
                        this.showSign = [];
                        this.data_day = data_day;
                    }
                    for (var i = 0; i < data_t; i++) {
                        if (this.in_array(this.data_day[i], this.showSign)) {
                            this.signData.push(true);
                        } else {
                            this.signData.push(false);
                        }
                    }
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            });
        }
    },
    components: { 
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/shop/sign.less');
</style>
