<template>
    <div class="container">
        <heads :title="title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
       
        <div class="container_content" @tap="_changeEditor()" style="margin-top: 50rpx;height: 100vh;">
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

export default {
    data() {
        return {
            richList: [],
            richIndex:0,
            title:"",
            uploadUrl: '',
            text: '',
            showText: true,
            content: [],
            imgimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/ffff2x.png',
            test: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/_bg2x.png',
            overflow: 'scroll',
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
        this.richIndex = option.index
        let obj = JSON.parse(option.itemIntroList)
        this.title = obj.name
        
        if (this.$store.state.goodsDetail) {
            this.richList = this.$store.state.goodsDetail;
        } else {
            this.richList = [];
        }
    },
    methods: {
        
        ok() {
            this.$store.state.goodsDetail = JSON.parse(JSON.stringify(this.richList));
            this.$store.state.detaiFlag = true;
            // 传递值
            uni.setStorageSync('richList',JSON.stringify(this.richList))
            uni.setStorageSync('richIndex',JSON.stringify(this.richIndex) )
            uni.navigateBack({
                delta: 1
            });
        },
        _changeEditor() {
            var me = this;
            setTimeout(function() {
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
