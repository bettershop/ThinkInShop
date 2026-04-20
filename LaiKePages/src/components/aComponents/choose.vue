<template>
    <view class="choose" v-if="is_type" @tap="_isHide">
        <!-- 确认取消 -->
        <template v-if="is_type == 1">
            <view class="is_choose" :style="{borderRadius:is_choose_obj.borderRadius}" @tap.stop>
                <view :style="{backgroundColor: is_choose_obj.background}">
                    <view @tap="_leftClick" :style="{color:is_choose_obj.colorLeft}">{{is_choose_obj.left}}</view>
                    <view @tap="_rightClick" :style="{color:is_choose_obj.colorRight}">{{is_choose_obj.right}}</view>
                </view>
                <view>
                    <view 
                        :style="{color:is_choose_index == index?'#FA5151':''}"
                        v-for="(item,index) in is_choose" :key="index"
                        @tap="_choose(index)">
                        {{item}}
                    </view>
                </view>
                <view class="safe-area-inset-bottom"></view>
            </view>
        </template>
        <!-- 标题 -->
        <template v-if="is_type == 2">
            <view class="is_choose" :style="{borderRadius:is_choose_obj.borderRadius}" @tap.stop>
                <view :style="{backgroundColor: is_choose_obj.background}">
                    <view class="title">{{is_choose_obj.title}}</view>
                </view>
                <view>
                    <view 
                        :style="{color:is_choose_index == index?'#FA5151':''}"
                        v-for="(item,index) in is_choose" :key="index"
                        @tap="_choose(index,true)">
                        {{item}}
                    </view>
                </view>
                <view class="safe-area-inset-bottom"></view>
            </view> 
        </template>
        <!-- 计算方式 -->
        <template v-if="is_type == 3">
            <view class="is_choose" :style="{borderRadius:is_choose_obj.borderRadius}" @tap.stop>
                <view :style="{backgroundColor: is_choose_obj.background}">
                    <view class="title">{{is_choose_obj.title}}</view>
                </view>
                
                <view>
                    <view 
                        :style="{color:is_choose_index == index?'#FA5151':''}"
                        v-for="(item,index) in is_choose" :key="index"
                        @tap="_chooseMethod(item,index,true)">
                        {{item.name}}
                    </view>
                </view>
                <view class="safe-area-inset-bottom"></view>
            </view>
        </template>
        <!-- 语种 -->
        <template v-if="is_type == 4">
            <view class="is_choose" :style="{borderRadius:is_choose_obj.borderRadius}" @tap.stop>
                <view :style="{backgroundColor: is_choose_obj.background}">
                    <view class="title">{{is_choose_obj.title}}</view>
                </view>
                <view>
                    <view
                        :style="{color:is_choose_obj.selected_lang_value == item.lang_code ?'#FA5151':''}"
                        v-for="(item,index) in is_choose" :key="index"
                        @tap="_chooseLangMethod(item,index,true)">
                        {{item.lang_name}}
                    </view>
                </view>
                <view class="safe-area-inset-bottom"></view>
            </view>
        </template>
        <!-- 货币 -->
        <template v-if="is_type == 5">
            <view class="is_choose" :style="{borderRadius:is_choose_obj.borderRadius}" @tap.stop>
                <view :style="{backgroundColor: is_choose_obj.background}">
                    <view class="title">{{is_choose_obj.title_currency}}</view>
                </view>
                <view>
                    <view
                        :style="{color:is_choose_obj.selected_currency_value == item.currency_symbol ?'#FA5151':''}"
                        v-for="(item,index) in datas" :key="index"
                        @tap="_chooseCurrencyMethod(item,index,true)">
                        {{item.currency_code}}({{item.currency_symbol}})
                    </view>
                </view>
                <view class="safe-area-inset-bottom"></view>
            </view>
        </template>
    </view>
</template>

<script>
    export default{
        props:{
            "is_type": {
                type: Number,
                default: 0,
                //0隐藏 1单选
            },
            "is_choose": {
                type: Array,
                default: () => [],
            },
            "datas":{
                type: Array,
                default: () => []
            },
            "is_choose_obj": {
                type: Object,
                default: ()=>(
                    {
                        selected_lang_value:'zh_CN',
                        selected_currency_value:'￥',
                        title: '切换语言',
                        title_currency: '切换货币',
                        left: '取消',
                        colorLeft: '#999999',
                        right: '确认',
                        colorRight: '#FA5151',
                        background: '#F4F5F6',//显示图标
                        borderRadius: '24rpx 24rpx 0 0',//提示文字
                    }
                )
            },
            is_index: {
                type: Number,
                default: 0,
            },
            // --- 分割线 ---
        },
        watch: {
            is_index: {
                handler(newValue, oldValue) {
                    this.is_choose_index = newValue
                },
                deep: true
            },
        },

        data(){
            return {
                is_choose_index: 0,//选中的下标
            }
        },
        methods:{
            //取消
            _leftClick(){
               this.$emit('_leftClick')
            },
            //确认
            _rightClick(){
               this.$emit('_rightClick', this.is_choose_index)
            },
            //选中的弹窗下标
            _choose(index,emits){
                this.is_choose_index = index
                if(emits){
                    this.$emit('_choose', index)
                }
            },
            _chooseMethod(item,index,emits){
                this.is_choose_index = index
                if(emits){
                    this.$emit('_choose', index)
                }
            },
            
            //语种特殊处理
            _chooseLangMethod(item,index,emits){ 
                this.is_choose_obj.selected_lang_value = item.lang_code;
                if(emits){
                    this.$emit('_choose', item,'language')
                }
            },
            
            //货币特殊处理
            _chooseCurrencyMethod(item,index,emits){
                this.is_choose_obj.selected_currency_value = item.currency_symbol
                if(emits){
                    this.$emit('_choose', item,'currency')
                }
            },
            
            //点击黑色遮昭层，隐藏弹窗
            _isHide(){
                console.log('触发隐藏事件');
                this.$emit('_isHide')
            }
        }
    }
</script>

<style lang="less" scoped> 
     @import url("@/laike.less");
    .is_choose{
        animation: anShowToast3 0.5s both;
    }
    //上滑 显示动画
    @keyframes anShowToast3 {
       0% {
       margin-bottom: -100%;
       }
           
       100% {
       margin-bottom: 0;
       }
    }
    .choose{
        position: fixed;
        left: 0;
        right: 0;
        top: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, .5);
        z-index: 999;
        .is_choose{
            width: 100%;
            position: fixed;
            bottom: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background-color: #ffffff;
            overflow: hidden;
            >view:first-child{
                width: 100%;
                display: flex;
                >view{
                    flex: 1;
                    padding: 32rpx;
                    box-sizing: border-box;
                }
                >view:first-child{
                    text-align: left;
                }
                >view:last-child{
                    text-align: right;
                }
            }
            >view:nth-child(2){
                width: 100%;
                display: flex;
                flex-direction: column;
                padding: 0 32rpx;
                box-sizing: border-box;
                >view{
                    flex: 1;
                    padding: 32rpx 0;
                    box-sizing: border-box;
                    border-bottom: 2rpx solid rgba(0, 0, 0, .1);
                    text-align: center;
                    color: #333333;
                    font-size: 32rpx;;
                }
            }
            .title{
                text-align: center !important;
                font-size: 40rpx;
                color: #333333;
            }
            
        }
    }
</style>
