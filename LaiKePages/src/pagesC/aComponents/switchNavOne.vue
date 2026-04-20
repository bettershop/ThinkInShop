<template>
    <!-- 切换导航-无左右滑动效果 只适配了小于等于6个切换导航 大于6个请不要使用-->
    <view class="switchNav"  :style="{top:is_switchNav_top}">
        <ul class="is_switchNav"
            :style="{
                borderRadius: is_switchNav_radius,
                backgroundColor: is_switchNav_obj.backgroundColor,
                paddingTop: is_switchNav_padT,
                paddingBottom: is_switchNav_padB
            }">
            <li class="is_switchNav_li" :class="{is_switchNav_choose:isSwitchNav==index}"
                :style="{color:(isSwitchNav==index?is_switchNav_obj.choose_color:is_switchNav_obj.color)}"
                v-for="(item, index) in is_switchNav" :key="index"
                @tap="_isSwitchNav(index)">
                {{item}}
            </li>
            <li v-if="is_switchNav.length>1" class="is_switchNav_line" 
                :style="{
                    left:(
                        (is_switchNav.length==2?138:is_switchNav.length==3?76:is_switchNav.length==4?44:is_switchNav.length==5?24:12)
                        +
                        (is_switchNav.length==2?374:is_switchNav.length==3?250:is_switchNav.length==4?186:is_switchNav.length==5?150:126)
                        *
                        isSwitchNav
                    )+'rpx',
                    borderBottom:is_switchNav_obj.borderBottom
                }">
            </li>
        </ul>
    </view>
</template>

<script>
    export default{
        props:{
            //必传  负责有遮挡   父组件计算top：缓存中的data_height + heads组件高度 + 其他组件高度
            "is_switchNav_top":{
                type: String,
                default: '0' //定位 上边距
            },
            //必传  显示导航
            "is_switchNav":{
                type: Array,
                default: []
            },
            //非必传
            "is_switchNav_obj":{
                type: Object,
                default: ()=>(
                    {
                        color: '#999999',//未选中字体颜色
                        choose_color: '#333333',//选中字体颜色
                        borderBottom: '4rpx solid #FA5151',//选中的下划线
                        backgroundColor: '#fff',//背景色
                    }
                )
            },
            //非必传
            "is_switchNav_radius":{
                type: String,
                default: '0 0 24rpx 24rpx' //圆角
            },
            //非必传
            "is_switchNav_padT":{
                type: String,
                default: '32rpx' //上 内边距
            },
            //非必传
            "is_switchNav_padB":{
                type: String,
                default: '48rpx' //下 内边距
            },
        },
        data(){
            return {
                isSwitchNav: 0, //当前选中的标签
                is_choose: true, //防重复点击
            }
        },
        methods:{
            //选择导航
            _isSwitchNav(index) {
                if(this.is_choose){
                    this.isSwitchNav = index
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
    .is_switchNav{
        position: relative;
        animation: anShowToast3 0.5s both;
    }
    @keyframes anShowToast3{
       0% {
       opacity: 0;
       margin-top: -100%;
       }
           
       100% {
       opacity: 1;
       margin-top: 0;
       }
    }
    .switchNav{
        position: fixed;
        width: 100%;
        height: auto;
    }
    .is_switchNav{
        width: 100%;
        height: auto;
        display: flex;
        align-items: center;
        justify-content: space-around;
        .is_switchNav_li{
            position: relative;
            z-index: 2;
            flex: 1;
            height: 70rpx;
            line-height: 70rpx;
            font-size: 28rpx;
            text-align: center;
        }
        .is_switchNav_choose{
            font-weight: bold;
            font-size: 32rpx;
            transition: all .1s;
        }
        .is_switchNav_line{
            position: absolute;
            left: 0;
            z-index: 1;
            width: 100rpx;
            height: 70rpx;
            transition: all .5s;
        }
    }
</style>
