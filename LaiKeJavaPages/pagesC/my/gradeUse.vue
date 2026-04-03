<template>
    <div class='us-all'>
        <heads :title='language.gradeUse.title'></heads>

        <div v-if='content' class="logo">
            <rich-text class="richtext yh-content" :nodes="content"></rich-text>
        </div>

    </div>
</template>

<script>
    import htmlParser from '@/common/html-parser.js'

    export default {
        data () {
            return {
                title: '使用说明',
                log: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/15306155488.png',
                content: '',
                
            }
        },
        onLoad () {
            

            let me = this
            uni.showLoading({
                title: this.language.showLoading.loading
            })
            
            this.$req.post({data}).then(res => {
                uni.hideLoading()
                if (res.code == 200) {
                    me.content = htmlParser(res.Instructions)
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    })
                }
            })
            

        }
    }
</script>

<style scoped lang="less">
    @import url("../../static/css/my/gradeUse.less");
</style>
