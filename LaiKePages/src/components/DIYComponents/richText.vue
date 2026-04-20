<template>
	<view class='richText' :style="'background-color:'+bgColor+';margin:'+ udConfig +'px '+ lrConfig +'px;'" v-if="description">
		<rich-text class="richtext" :nodes="description"></rich-text>
	</view>
</template>


<script>
	import htmlParser from '@/common/html-parser.js'
	export default {
		name: 'richText',
		props: {
			dataConfig: {
				type: Object,
				default: () => {}
			}
		},
        watch: {
            dataConfig () {
                this.bgColor = this.dataConfig.bgColor.color[0].item
                this.lrConfig = this.dataConfig.lrConfig.val
                this.udConfig = this.dataConfig.udConfig.val
                this.description = this.dataConfig.richText.val.replace(/src/g,
                    'style=\'width:100%!important;height: auto!important;\' src').replace(
                    /<table/g, `<table style="width:100%!important;"`)

                this.description = htmlParser(this.description)
            }
        },
		data() {
			return {
				bgColor:this.dataConfig.bgColor.color[0].item,
				lrConfig:this.dataConfig.lrConfig.val,
				description: '',
				udConfig:this.dataConfig.udConfig.val
			};
		},
		created() {
			this.description = this.dataConfig.richText.val.replace(/src/g,
                    'style=\'width:100%!important;height: auto!important;\' src').replace(
					/<table/g, `<table style="width:100%!important;"`)
					
			this.description = htmlParser(this.description)
		},
		methods: {
			
		}
	}
</script>

<style lang="less">
	.richText{
		padding: 20rpx;
		background-color: #fff;
		margin: 0 20rpx;
		// border-radius: 24rpx;
	}
</style>
