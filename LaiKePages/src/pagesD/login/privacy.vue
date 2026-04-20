<template>
    <div>
        <heads :title='title' :titleColor="'#333333'" :bgColor="bgColor"></heads>
        <toload :load="load">
            <rich-text class="richtext" :nodes="contentNodes" style="font-size: 14px;"></rich-text>
        </toload>

    </div>
</template>

<script>
    import htmlParser from '@/common/html-parser.js'

    export default {
        data () {
            return {
                title: '隐私协议',
                bgColor: [
                  {
                      item: '#FFFFFF'
                  },
                  {
                      item: '#FFFFFF'
                  }
                ],
                content: '',
                contentNodes: [],
                load: false,
                loadImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/loading.gif'
            }
        },
        onLoad () {
            var me = this
            let data = {
                api: 'app.login.privacy_agreement',
            }
            this.$req.post({data}).then(res => {
                if (res.data.content) {
                    //吧view标签替换为html的P标签
                    me.content = '<div style=\'padding-left:8px;padding-right:8px;\'>' + res.data.content.replace(new RegExp('<view', 'gm'), '<p').replace(new RegExp('</view>', 'gm'), '</p>') + '</div>'
                    var htmlString = me.content.replace(/\\/g, '').replace(/<img/g, '<img style="display:none;"')
                    htmlString = htmlString.replace('<div style=\'padding-left:8px;padding-right:8px;\'>', '<div style="padding: 25px 15px;">')
                    me.contentNodes = htmlParser(htmlString)
                    // #ifndef MP-ALIPAY
                    setTimeout(function () {
                        me.load = true
                    }, 50)
                    // #endif
                    // #ifdef MP-ALIPAY
                    me.load = true
                    // #endif
                }else{
                    me.load = true
                }
            })
        }
    }
</script>

<style scoped lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/login/agreement.less");
</style>
