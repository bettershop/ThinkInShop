<template>
    <!-- 切换导航-无左右滑动效果 只适配了小于等于6个切换导航 大于6个请不要使用-->
    <view class="switchLable" :style="{top:is_switchLable_top}">
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
                @tap="_isSwitchLable(index)">
                {{ item }}
            </li>
            <li class="is_switchLable_bg"
                :style="{
                    left:(
                        (is_switchLable.length==2?116:is_switchLable.length==3?52:is_switchLable.length==4?24:2)
                        +
                        (is_switchLable.length==2?374:is_switchLable.length==3?250:is_switchLable.length==4?186:150)
                        *
                        isSwitchLable
                    )+'rpx',
                    backgroundColor:is_switchLable_obj.background
                }">
            </li>
        </ul>
    </view>
</template>

<script>
    export default{
        props:{
            //必传  负责有遮挡   父组件计算top：缓存中的data_height + heads组件高度 + 其他组件高度
            "is_switchLable_top":{
                type: String,
                default: '0' //定位 上边距
            },
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
                isSwitchLable: 0, //当前选中的标签
                is_choose: true, //防重复点击
            }
        },
        methods:{
            //选择导航
            _isSwitchLable(index) {
                if(this.is_choose){
                    this.isSwitchLable = index
                    this.is_choose = false
                    setTimeout(()=>{
                        this.is_choose = true
                    },1000)
                    this.$emit("choose",index)
                } else {
                }
            },
        }
    }
</script>

<style lang="less" scoped> 
    @import url("@/laike.less");
    //使用动画
    .switchLable{
        position: relative;
        animation: anShowToast3 1s both;
    }
    @keyframes anShowToast3{
       0% {
       opacity: 0;
       margin-left: -100%;
       }
           
       100% {
       opacity: 1;
       margin-left: 0;
       }
    }
    .switchLable{
        position: fixed;
        width: 100%;
        height: auto;
    }
    .is_switchLable {
        width: 100%;
        display: flex;
        justify-content: space-around;
        border-radius: 0 0 24rpx 24rpx;
        .is_switchLable_li{
            flex: 1;
            width: 144rpx;
            height: 56rpx;
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
            border-radius: 28rpx;
            transition: all .5s;
        }
        .is_switchLable_choose{
        }
    }
</style>
