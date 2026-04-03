<template>
    <view class="webView">
        <heads title="外部链接"></heads>
        <web-view :src="url"></web-view>
    </view>
</template>

<script>
import toload from '../../components/toload.vue'
export default {
  components: { toload },
    data () {
        return {
            load: false,
            url: '',
        }
    },
    components: {toload},
    onLoad (options) {

        let query = JSON.parse(decodeURIComponent(options.query))
        
        this.url = query.url
    },
    methods: {

    },
}
</script>

<style lang="less" scoped>

</style>
