<template>
    <div class='us-all'>
        <heads :title="language.aboutMe.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <toload :load="load">
            <rich-text :nodes="contentNodes" class="yh-contentNodes"></rich-text>
        </toload>
    </div>
</template>

<script>
    import htmlParser from '../../common/html-parser.js'

    export default {
        data () {
            return {
                title: '关于我们',
                log: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/15306155488.png',
                content: '',
                contentNodes: [],
                load: false,
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ]
            }
        },
        onLoad () {
            let me = this;
            let data = {
                api: 'app.user.about_us',
            }
            this.$req.post({data}).then(res => {
                if (res.code == 200) {
                    me.load = true
                    me.content = '<div style=\'padding-left:8px;padding-right:8px;\'>' + res.data.aboutus.replace(new RegExp('<view',
                        'gm'), '<p').replace(new RegExp('</view>', 'gm'), '</p>') + '</div>'

                    var htmlString = me.content.replace(/\\/g, '').replace(/<img/g, '<img  style=\'width:318px;height:230px;margin-left:-5px;\' ')
                    me.contentNodes = htmlParser(htmlString)

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
    @import url("../../static/css/my/aboutMe.less");
    /deep/.toload{
        background-color: #ffffff !important;
    }
</style>
