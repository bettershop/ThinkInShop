<template>
	<view class="lines" :style="'padding:0 '+ lrEdge +'px;margin-top:'+ mbConfig +'px;'" v-if="heightConfig">
		<view class="item" :style="'border-bottom-width:'+ heightConfig +'rpx;border-bottom-color:'+ lineColor +';border-bottom-style:'+ lineStyle +';'"></view>
	</view>
</template>

<script>
	export default {
		name: 'guide',
		props: {
			dataConfig: {
				type: Object,
				default: () => {}
			}
		},
        watch: {
            dataConfig () {
                this.heightConfig = this.dataConfig.heightConfig.val
                this.lineColor = this.dataConfig.lineColor.color[0].item
                this.lineStyle = this.dataConfig.lineStyle.list[this.dataConfig.lineStyle.type].style
                this.lrEdge = this.dataConfig.lrEdge.val
                this.mbConfig = this.dataConfig.mbConfig.val
            }
        },
		data() {
			return {
				heightConfig:this.dataConfig.heightConfig.val,
				lineColor:this.dataConfig.lineColor.color[0].item,
				lineStyle:this.dataConfig.lineStyle.list[this.dataConfig.lineStyle.type].style,
				lrEdge:this.dataConfig.lrEdge.val,
				mbConfig:this.dataConfig.mbConfig.val
			};
		}
	}
</script>

<style lang="less">
	.lines{
		padding: 0 20rpx;
		margin-top: 20rpx;
		.item{
			width: 100%;
			box-sizing: border-box;
			border-bottom-color: red;
			border-bottom-width: 1px;
			// dashed
			border-bottom-style: dotted;
		}
	}
</style>
