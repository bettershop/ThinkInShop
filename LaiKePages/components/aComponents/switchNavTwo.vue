<template>
    <view class="fixed" :style="{height:fixed_height}">
        <!-- 切换导航-无左右滑动效果 只适配了小于等于6个切换导航 大于6个请不要使用-->
        <view class="switchLable">
            <ul class="is_switchLable"
            :style="{
                    borderRadius: is_switchLable_radius,
                    backgroundColor: is_switchLable_obj.backgroundColor,
                    paddingTop: is_switchLable_padT,
                    paddingBottom: is_switchLable_padB
                }">
                <li class="is_switchLable_li" :class="{is_switchLable_choose:isSwitchLable==index}"
                    :style="{color:(isSwitchLable==index?is_switchLable_obj.choose_color:is_switchLable_obj.color)}"
                    v-for="(item, index) in is_switchLable" :key="index" 
                    @tap="_isSwitchLable(index,item)">
                    <view  :class="{is_switchLable_bg:isSwitchLable==index}" > 
                        {{ item.name || item }}
                    </view>
                </li> 
            </ul> 
        </view>
    </view>
</template>

<script>
    export default{
        props:{
            //必传  显示导航
            "is_switchLable":{
                type: Array,
                default: []
            },
            //非必传
            "is_switchLable_obj":{
                type: Object,
                default: ()=>(
                    {
                        color: '#666666',//未选中字体颜色
                        choose_color: '#FA5151',//选中字体颜色
                        background: 'rgba(250,81,81,0.1)',//选中的背景色
                        backgroundColor: '#fff',//背景色
                    }
                )
            },
            //非必传
            "is_switchLable_radius":{
                type: String,
                default: '0 0 24rpx 24rpx' //圆角
            },
            //非必传
            "is_switchLable_padT":{
                type: String,
                default: '0' //上 内边距
            },
            //非必传
            "is_switchLable_padB":{
                type: String,
                default: '16rpx' //下 内边距
            },
        },
        data(){
            return {
                fixed_height: '',//高度占位，防止遮挡。
                isSwitchLable: 0, //当前选中的标签
                is_choose: true, //防重复点击
            }
        },
        mounted(){
            const query = uni.createSelectorQuery().in(this);
            query.select('.is_switchLable').boundingClientRect(data => {
              this.fixed_height = data.height + 'px'
            }).exec();
        },
        methods:{
            //选择导航
            _isSwitchLable(index,item) {
                if(this.is_choose){
                    this.isSwitchLable = index
                    this.is_choose = false
                    setTimeout(()=>{
                        this.is_choose = true
                    },1000)
                    
                    if(item && item.hasOwnProperty('type')){
                        this.$emit("choose",{index,...item})
                    }else{
                        this.$emit("choose",index)
                    }
                } else {
                    console.log('防重复点击状态',!this.is_choose);
                }
            },
        }
    }
</script>

<style lang="less" scoped> 
    @import url("@/laike.less"); 
    .switchLable{
        position: fixed;
        width: 100%;
        height: auto;
    }
    .is_switchLable {
        width: 100%;
        //color: #999999;
        display: flex;
        justify-content: space-around;
        //padding-bottom: 16rpx;
        border-radius: 0 0 24rpx 24rpx;
        //background-color: #fff;
        .is_switchLable_li{
            flex: 1;
            width: 144rpx;
            height: 56rpx;
            //color: #666666;
            font-size: 28rpx;
            line-height: 56rpx;
            text-align: center;
            position: relative;
            z-index: 2;
            border-radius: 28rpx;
        }
        
        .is_switchLable_bg{
            position: absolute;
            left: 0;
            z-index: 1;
            width: 144rpx;
            height: 56rpx;
            background: rgba(250,81,81,0.1);
            border-radius: 28rpx;
            transition: all .5s;
        }
        .is_switchLable_choose{
            //color: #FA5151;
        }
    }
</style>
