<template>
    <view class="isImage">
        <!-- 单图片 可瀑布流 -->
        <template v-if="isImageType == 1">
            <image
                :lazy-load="isLazyLoad"
                :mode="isMode"
                :src="isSrc"
                :class="'is_image'"
                :style="{
                    width: imageWidth,
                    height: imageheight,
                    borderRadius: imageBorderRadius,
                }"
                :data-name="dataName"
                @load="_loadSuccess($event)"
                @error="_loadError($event)"
            ></image>
            <!-- 
            <image-s
                :isSrc="item.imgurl"
                :imageWidth="'100%'"
                :imageBorderRadius="'16rpx'"
                :dataName="1"
                :imgIndex="index"
                @_imageWaterfall="_imageWaterfall">
            </image-s> 
            -->
        </template>
    </view>
</template>

<script>
    export default{
        props:{
            //类型
            "isImageType": {
                type: Number,
                default: 1, 
            },
            // --- 分割线 ---
            
            //非必传  懒加载
            "isLazyLoad": {
                type: Boolean,
                default: true, 
                //false关闭 true开启 （只针对page与scroll-view下的image有效）
            },
            //非必传  缩放模式
            "isMode": {
                type: String,
                default: 'widthFix', 
                // widthFix 宽度不变，高度自适应           --固定 宽度 的图片使用
                // heightFix 高度不变，宽度自适应          --固定 高度 的图片使用
                // scaleToFill 不保持比例，铺满设置的宽高   --固定 宽高 的图片使用
            },
            //必传  图片地址
            "isSrc": {
                type: String,
                default: '', 
            },
            // --- 分割线 ---
            
            //非必传  图片宽度
            "imageWidth": {
                type: String,
                default: '100%', 
            },
            //非必传  图片高度
            "imageheight": {
                type: String,
                default: 'initial', 
            },
            //非必传  图片圆角
            "imageBorderRadius": {
                type: String,
                default: 'initial', 
            },
            // --- 分割线 ---

            //（用瀑布流必传）  传入的e.target.dataset.name 用于 区分图片瀑布流 是左还是右div
            "dataName": {
                type: Number,
                default: 0, 
            },
            //（用瀑布流必传）  当前图片数据在原数组中的下标
            "imgIndex": {
                type: Number,
                default: 0, 
            },
        },
        data(){
            return{
                
            }
        },
        computed:{
            //可能有相同路径的图片，会无法加载@load事件；拼接随机字符串，可以解决这个问题
            // set_isSrc(){
            //     return this.isSrc + '?a=' + Math.floor(Math.random() * 1000000)
            // }
            //这里直接在接口返回数据的时候加上
        },
        methods:{
            //图片加载完毕执行
            _loadSuccess(e){
                //console.log('图片加载成功', e);
                
                //不用瀑布流就停止
                if(!this.dataName){return}
                
                //获取 img高度
                let divWidth = 375;                //实际宽度 图片想要显示的宽度（可以随便写，是一个x用来计算当前单元div的高度，后用总高度来区分放入左右list）
                let imgW = e.detail.width;         //图片原始 宽度
                let imgH = e.detail.height;        //图片原始 高度
                //重新计算图片高度 实际宽度 * 原始宽高比例  （ *100/100 用来保留两位小数）
                let newImgHeight = Math.round(divWidth * (imgH/imgW) * 100) / 100;
                
                //传给父组件，图片data-name归属，重新计算后的图片高度，数据的下标。
                this.$emit('_imageWaterfall', e.target.dataset.name, newImgHeight, this.imgIndex)
            },
            //图片加载失败
            _loadError(e){
                console.log('图片加载失败', e);
                //传给父组件，数据的下标。
                this.$emit('_loadError', this.imgIndex)
            },
        }
    }
</script>

<style lang="less" scoped> 
@import url("@/laike.less");
    .isImage{
        display: flex;
        .is_image{
            width: auto;
            height: auto;
        }
    }
</style>
