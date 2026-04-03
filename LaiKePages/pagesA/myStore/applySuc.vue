<template>
    <div>
        <heads :title='language.applySuc.title' returnR='7'  :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <div class='relative'>
            <div class='noFindDiv'>
                <div class='noFindDiv_top'>
                    <img class='noFindImg' :src="sucImg"/>
                </div>
                <div class='noFindTitle'>{{language.applySuc.submittedSuccse}}</div>
                
                <span class="noFindText font_24">{{language.myStore.inReviewDisc1}}{{auto_examine}}{{language.myStore.inReviewDisc2}}</span>
                <br/>
                <span class='noFindText'> {{language.applySuc.waitReview}}</span>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        data () {
            return {
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ],
                back: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x.png',
                
                title: '申请结果',
                fastTap: true,
                auto_examine:3,
                sucImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/applySuc_apply.png',
            }
        },
        onLoad(){
            this.$req
                .post({
                    data: {
                        api:"mch.App.Mch.Index"
                    }
                })
                .then(res => {
                    if (res.code == 200) {
                        this.auto_examine=res.data.auto_examine//入驻申请审核时间
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
        }
    }
</script>

<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/myStore/applySuc.less");
</style>
