<template>
    <div class="container">
        <heads :title="title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
       
        <div class="container_content" @tap="_changeEditor()" style="margin-top: 32rpx;">
            <div class="relative"><uni-richtext ref="richtext" :status="JSON.stringify(topTabBar)"></uni-richtext></div>
        </div>
        <div style="height: 100rpx;"></div>
        <div class="okBtn" @tap="ok()">
            <div class="okBtnBottom">
                {{language.storeGoodsDetail.complete}}
            </div>
        </div>
    </div>
</template>

<script> 
import uniRichtext from '@my-miniprogram/src/components/qiyue-richtext/uni-richtext.vue'; 
export default {
    data() {
        return {
            title: '',
            richList: '',
            uploadUrl: '',
            text: '',
            showText: true,
            content: [],
            imgimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/ffff2x.png',
            test: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/_bg2x.png',
            overflow: 'scroll',
            title: '',
            graphic: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/graphic.png',
            rubbish: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/rubbish.png',
            Text: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/Text.png',
            bback_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/bback.png',
            fx_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/fx.png',
            gw_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/gw.png',
            load_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/5-121204193R7.gif',
            editor: false,
            focus: false,
            bgColor:[{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
            topTabBar: 0,
        };
    },
    onLoad(option) {
        if (this.$store.state.goodsDetail) {
            this.richList = this.$store.state.goodsDetail;

        } else {
            this.richList = [];
        }
        if(option.title){
            this.title = option.title
        }
        //缓存中的url错误，没有store_type,重新获取.
        this.LaiKeTuiCommon.getApiUrl(uni.getStorageSync('url'))
    },
    methods: {
        changeTabBar(status){
            this.topTabBar = status
        },
        ok() {
            this.$store.state.goodsDetail = JSON.parse(JSON.stringify(this.richList));
            this.$store.state.detaiFlag = true;
            uni.navigateBack({
                delta: 1
            });
        },
        _changeEditor() {
            var me = this;
            // this.editor = true;
            setTimeout(function() {
                // me.focus = true;
            }, 200);
        },
        _creat() {
            this.focus = false;
            this.showText = false;
            var json = {
                name: 'div',
                attrs: {
                    class: 'demoCss',
                    style: 'color: #020202;text-align:left;height:25px;font-size:12px;padding: 0 15px;display:flex;align-items:center;'
                },
                children: [
                    {
                        type: 'text',
                        text: this.text
                    }
                ]
            };
            this.content.push(json);
        }
    },
    components: { 
        uniRichtext
    }
};
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped>
@import url("@/laike.less");
@import url('../../static/css/myStore/goodsDetail.less');
</style>
