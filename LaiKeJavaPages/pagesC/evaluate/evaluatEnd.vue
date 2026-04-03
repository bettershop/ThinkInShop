<template>
    <div class='box'>
        <heads :title='language.evaluate.evaluatEnd.title' :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
        <div class='pic'>
            <img :src="gouhei_img"/>
        </div>
        <p>{{language.evaluate.evaluatEnd.title}}</p>
        <div class='button' @tap="navToHome()">{{language.evaluate.evaluatEnd.return_home}}</div>
    </div>
</template>

<script>
    export default {
        data () {
            return {
                gouhei_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/gouhei.png',
                title: '发布成功',
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
            }
        }
    }
</script>

<style scoped lang="less">  
    @import url("@/laike.less");
    @import url("../../static/css/evaluate/evaluatEnd.less");
</style>
