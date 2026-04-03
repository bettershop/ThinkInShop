<template>
    <div class='box'>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.detailed.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <div style='border-radius: 0 0 24rpx 24rpx;' class='detailed-content' v-for="(item,index) in list"
             :key="index">
            <p class='title'>{{item.title}}</p>
            <p class='time'>{{item.time | dateFormat}}</p>
        </div>
        <div style='border-radius: 24rpx 24rpx 0 0;margin-top:24rpx;height: 100vh;' class='detailed-content' v-for="(item,index) in list"
             :key="index">
            <text decode="true" class="text">{{item.content}}</text>
        </div>
    </div>
</template>

<script>
    export default {
        data () {
            return {
                title: '系统信息',
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ],
                list: '',
                id: '',
            }
        },
        onLoad (option) {
            var me = this
            this.id = option.id
        },
        onShow () {
            var me = this
            this.isLogin(()=>{
            	me.getDetailedMesageData()
            })
        },
        methods: {
            changeLoginStatus () {
                this.getDetailedMesageData()
            },
            /**
             * 获取消息详情
             * */
            getDetailedMesageData () {
                var me = this
                var data = {
                    id: this.id,
                    type: 2,
                    api: 'app.message.oneindex',}
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                        me.list = res.data.message
                    } else {
                        uni.showToast({
                            title: res.data.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
            }
        }
    }
</script>

<style>
    page{
        background-color: #F4F5F6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/message/detailedMesage.less");
</style>
