<template>
    <view >  
        <template>
            <web-view :src="url" ></web-view>          
        </template>   
    </view>                                                                             
</template>

<script>
    import toload from '../../components/toload.vue'
    export default {
        components: {
            toload
        },
        data() {
            return {  
                title:'',
                load: false,
                url: '',
                access_id: '',
            }
        },
        onLoad(options) {          
            this.access_id = uni.getStorageSync('access_id') 
            this.user_phone= uni.getStorageSync('user_phone') ? uni.getStorageSync('user_phone') : ''
            let data = {
                api: 'app.plugins.getPluginInfo',
                pluginCode: 'presell',
            };
            this.$req.post({
                data
            }).then(res => {
                if (res.code == 200) {
                    this.url = res.data.pluginInfo.jump_address
                    if (options.status || options.id) {
                        if (options.status == 1 || options.status == 0) {
                            this.url = this.url + '#/pagesC/preSale/order/myOrder?status=' + options.status + '&access_id=' + this.access_id  + '&phone=' + this.user_phone +'&url=' + this.LaiKeTuiCommon.LKT_H5_DEFURL
                        } else {
                            this.url = this.url + '#/pagesC/preSale/goods/goodsDetailed?pro_id=' + options.id + '&access_id=' + this.access_id  + '&phone=' + this.user_phone + '&url=' + this.LaiKeTuiCommon.LKT_H5_DEFURL
                        }

                    } else {
                        this.url = this.url + '#/?access_id=' + this.access_id + '&phone=' + this.user_phone + '&url=' + this.LaiKeTuiCommon.LKT_H5_DEFURL                         
                    }

                }
            })
                
        },
        methods: {

        },


    }
</script>

<style>
</style>
