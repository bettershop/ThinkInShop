<template>
    <view class="isScrollView">
        <!-- 切换导航-有滑动效果 切换数量没限制-->
        <!--
        X方向 scroll-x   
        过度动画 scroll-with-animation
        滚动到对应子标签id位置 scroll-into-view
        -->
        <scroll-view 
            class="isScrollView_nav" 
            scroll-x="true" 
            scroll-with-animation="true" 
            :scroll-into-view="scrollIndex"
        >
            <view class="isScrollView_nav_view">
                <view 
                    class="isScrollView_nav_item"
                    :id="'scroll'+index"
                    :class="{ active: index == navIndex }"
                    v-for="(item, index) of is_ScrollView" :key="index"
                    @tap="_changeNav(item, index)"
                >
                    <view>{{ item.pname }}</view>
                    <text v-if="item.english_name">{{ item.english_name }}</text>
                </view>
            </view>
        </scroll-view>
    </view>
</template>

<script>
    export default{
        props:{
            // --- 数据渲染 ---
            
            "is_ScrollView":{
                type: Array,
                default: []
            },
            
            // --- 样式 ---
            //用这种方式太麻烦了 直接在主页面用 /deep/  --》案例：mchList.vue
            "is_ScrollViewStyle":{
                type: Object,
                default: ()=>(
                    {
                        color: '#020202',//未选中字体颜色
                        color_other: '#666666', //副标题未选中字体颜色
                        choose_color: '#333333',//选中字体颜色
                        choose_color_other: '#FA5151', //副标题选中字体颜色
                        borderBottom: '8rpx solid #FA5151',//选中的下划线
                    }
                )
            },
        },
        data(){
            return {
                navIndex: null,     //当前选中的下标
                scrollIndex: '',    //对应子标签id位置
                isAnimation: false, //默认选中第一条时不需要动画
            }
        },
        mounted(){
            console.log('组件挂载完毕，默认选中第一导航',this.is_ScrollView);
            if(this.is_ScrollView.length>0){
                this._changeNav(this.is_ScrollView[0],0)
            }
            //开启动画
            this.isAnimation = true
        },
        methods:{
            //切换导航
            _changeNav(item, index){
                console.log('item',item);
                console.log('11111111',index);
                //防重复点击
                if(this.navIndex == index){
                    console.log('防重复点击状态', this.navIndex == index);
                    return
                }
                
                //选中的当前下标
                this.navIndex = index
                
                //自动滚动过动画效果
                let scroll_class = '.isScrollView_nav'
                this._animation(index, scroll_class)
                
                console.log('1111');
                //选中的下标事件 返回给父组件
                    this.$emit('_changeNav', item, index)
            },
            //动画效果
            _animation(index, scroll_class){
                //默认选中第一条时不需要动画
                if(!this.isAnimation){return}
                
                //节点离窗口顶部的距离
                let top1 = 0 
                const query = uni.createSelectorQuery().in(this);
                query.select(scroll_class).boundingClientRect(data => {
                  top1 = data.top
                }).exec();
                
                //滚动条离顶部的距离
                let top2 = 0 
                uni.createSelectorQuery().selectViewport().scrollOffset(res => {
                    top2 = res.scrollTop
                }).exec();
                
                //分类导航自动滚动
                this.scrollIndex = 'scroll' + (index-1) 
            },
            
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
    /deep/.uni-scroll-view::-webkit-scrollbar{
        display: none;
    }
    .isScrollView{
        .isScrollView_nav{
            .isScrollView_nav_view{
                display: flex;
                padding-bottom: 16rpx;
                margin-top: 12rpx;
                .isScrollView_nav_item{
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    flex-direction: column;
                    height: 60rpx;
                    padding: 0 30rpx;
                    position: relative;
                    >view{
                        text-align: center;
                        font-size: 28rpx;
                        color: #020202;
                        line-height: 34rpx;
                        white-space: nowrap;
                    }
                    >text{
                        color: #666666;
                        font-size: 24rpx;
                        line-height: 24rpx;
                        white-space: nowrap;
                    }
                }
                .isScrollView_nav_item.active>view{
                    color: #333333;
                    font-weight: bold;
                }
                .isScrollView_nav_item.active>text{
                    color:#FA5151;
                }
                .isScrollView_nav_item.active:after{
                    content: '';
                    position: absolute;
                    bottom: -16rpx;
                    left: 50%;
                    transform: translateX(-50%);
                    width:48rpx;
                    height:8rpx;
                    background:rgba(250, 81, 81, 1.0);
                    border-radius:24rpx;
                }
            }
        }
    }
</style>
