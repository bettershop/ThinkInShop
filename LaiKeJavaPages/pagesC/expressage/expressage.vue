<template>
    <div>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title='language.expressage.title' :returnR="returnR"  ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>

        <!-- 头 -->
        <div class='expressage_name'>
            <div>
                <p class="logistics_company">{{language.expressage.logistics_company}}<span>{{name}}</span></p>
                <p class="Logistics_orderNo">{{language.expressage.order}}
                    <span>
                        <!-- 复制 -->
                        <div class='copy' type="button" @tap="onCopy()">{{language.expressage.copy}}</div>
                        {{courier_num}} 
                    </span>
                </p>
            </div>
            
        </div>

        <!-- 物流信息 -->
        <ul v-if='expressage&&expressage.length>0' class="LogisticsInfo">
            <template v-if='expressage.length'>
                <div class='expressage_yuanl'></div>
                <li v-for="(item,index) in expressage" :key="index" class="xiantiao">
                        <div class='expressage_right'>
                            <p class='expressage_time' >
                                <span :style="index==0?'color:#FA5151;':'color:#999999;'">{{item.ftime}}</span>
                            </p>
                            <p :style="index==0?'color:#FA5151;':'color:#999999;'">{{item.context||item.description}}</p>
                            
                        </div>
                        <div class='expressage_left'>
                            <div class='expressage_yuan'></div>
                        </div>
                    
                </li>
            </template>
            <li v-if='!expressage.length'>
                <div class='expressage_right'>
                </div>
                <div class='expressage_left'>
                    <div class='expressage_yuan'></div>
                    <div class='expressage__xian' ref='expressage__xian'></div>
                </div>
            </li>
        </ul>
        <div class='zwwl' v-if="!expressage.length">
            <img :src="zwwl" style='width: 750rpx;height: 460rpx;'>
            {{language.expressage.logistics_tips}}~
        </div>
    </div>
</template>

<script>

    import { copyText } from '@/common/util.js'

    export default {
        data () {
            return {
                title: '物流信息',
                sNo: '',
                arr: new Array(5),
                
                courier_num: '', //快递单号
                name: '', //快递公司
                expressage: '', //物流信息
                source: '', //跳转来源
                msg: '', //提示文字
                zwwl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zwwl.png',
                is_integral:false,
                returnR: 12,
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
            }
        },
        onLoad (option) {
            this.sNo = option.sNo            
            if(option.is_integral){
                this.is_integral = option.is_integral
            }
            
            if (option.list) {
                try {
                    // 尝试解析 JSON
                    const data = JSON.parse(option.list)
                    this.courier_num = data.courier_num
                    this.name = data.kuaidi_name
                    this.expressage = data.list || []
                    if (this.expressage.length == 0) {
                        this.msg = '暂无物流信息'
                    }
                } catch (error) {
                    // 使用正则表达式提取单号和快递公司
                    const match = option.list.match(/(\d+)\(([^)]+)\)/);
                    if (match) {
                      this.courier_num = match[1];
                      this.name = match[2];
                      this.expressage = option.list?.list || []
                      if (this.expressage.length == 0) {
                          this.msg = '暂无物流信息'
                      }
                    } else {
                    }
                }


            }else{
                this.isLogin(()=>{
                	this._axios()
                })
            }
        },
        onShow () {
            //移到了onlond里面
        },
        methods: {
            changeLoginStatus () {
                this._axios()
            },
            /**
             * 复制
             * @return { undefined }
             * */
            onCopy () {
                
                // #ifndef MP-WEIXIN
                uni.setClipboardData({
                    data: this.courier_num,
                    success: function (res) {
                        
                    }
                })
                // #endif
               
               
                uni.showToast({
                    title: this.language.expressage.copy_success,
                    icon: 'none',
                    duration: 1500
                })

                // #ifdef MP-WEIXIN
                copyText('', this.courier_num)
                // #endif
            },
            onError (e) {
                uni.showToast({
                    title: this.language.expressage.no_copy,
                    duration: 1000,
                    icon: 'none'
                })
            },
            /**
             * 加载数据
             * @return { undefined }
             * */
            async _axios () {
                uni.showLoading({
                    title: this.language.showLoading.loading
                })
                if(this.is_integral){
                    var data = {
                        api:'plugin.integral.AppIntegral.kuaidishow',
                        orderno: this.sNo,
                    }
                }else{
                    var data = {
                        api: 'app.order.logistics',
                        id: this.sNo,
                        o_source: 1,
                        type: '',
                    }
                }
                

                if (this.source == 1) {
                    data.type = 'pond'
                }

                let res = await this.$req.post({data})
                try {
                    let { code, message } = res;
                    uni.hideLoading()
                    if (code == 200) {
                        if(!this.courier_num) {
                            this.courier_num = res.data.list[0].courier_num
                        }
                        if(!this.name) {
                            this.name = res.data.list[0].kuaidi_name
                        }
                        this.expressage = res.data.list[0].list || []
                        if (this.expressage.length == 0) {
                            this.msg = this.language.expressage.Not_yet
                        }
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                } catch (e) {
                    uni.showToast({
                        title: this.language.expressage.loading_fail,
                        duration: 2000,
                        icon: 'none'
                    })
                }
            }
        }
    }
</script>

<style>
    page{
        background: #F4F5F6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/expressage/expressage.less");
</style>
