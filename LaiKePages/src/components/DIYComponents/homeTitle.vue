<template>
    <div class="home_title" :style="{marginTop:pxToRpxNum(mbConfig)+'rpx ',background:bgColor?`linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`:''}">
        <div class="left"> 
            <div class="title" :style="{fontSize:titleSize+'px',color:textColor[0].item}">
                {{title}}
            </div>
        </div>
        <div class="right" v-if="linkStyle" @click="$emit('toUrl', linkConfig)">
          {{language.home.more}}
        </div>
    </div>
</template>

<script>
export default {
    name: "home_title",
    props: {
        dataConfig: {
            type: Object,
            default: () => {
            }
        }
    },
    data() {
        return {
            title: this.dataConfig.titleConfig.value,//鏈接標題
            mbConfig: this.dataConfig.mbConfig.val,//頁面間距
            linkStyle: this.dataConfig.linkStyle.val,//是否显示更多
            linkConfig: this.dataConfig.linkConfig.value,//鏈接地址
            bgColor: this.dataConfig.bgColor.color,//背景顏色
            textColor: this.dataConfig.textColor.color,//文字顏色
            
            titleSize: this.dataConfig.titleSize.val,//字體大小
        }
    },
	created() {
		this.setLang();
	},
    methods: {
        
    }
}
</script>

<style lang="less" scoped>
.home_title {
    padding-left: 30rpx;
    display: flex; 
    align-items: center;
    justify-content: space-between;
    margin-left: 0rpx !important;
    margin-right: 0rpx !important;
    .left {
        display: flex;
        height: 100%;
        align-items: center;
        .line {
            width: 4rpx;
            height: 100%;
            background: #000000;
            margin-right: 15rpx;
        }
        
        .title {
            font-size: 38rpx;
            font-family: Source Han Sans CN;
            font-weight: 500;
            color: #000000; 
        }
    }
  
    .right {
      font-size: 24rpx;
      padding-right: 20rpx;
    }
}
</style>
