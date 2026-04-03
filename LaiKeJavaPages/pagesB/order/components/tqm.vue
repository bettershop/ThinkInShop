<template>
    <!-- 提取码弹出框 -->
    <div v-if="show" class="receiving_modal" @tap="receiving_stop">
        <div @tap.stop>
            <!-- 已完成 -->
            <img class="receiving_finish" v-if="receiving_check.status != 2" :src="finish3x" />
            <div class="receiving_content">
                <div class="receiving_content_title">{{language.order.order.order_number}}: {{ receiving_check.sNo }}</div>
                <div class="receiving_content_data" v-if="receiving_check.por_list.length == 1">
                    <div class="receiving_shop_img"><img :src="receiving_check.por_list[0].img" alt="" /></div>
                    <div class="receiving_content_item">
                        <p>{{ receiving_check.por_list[0].product_title }}</p>
                        <div class="receiving_size">
                            <span class="receiving_rmb">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(receiving_check.por_list[0].p_price) }}</span>
                           
                            <span class="receiving_count">×{{ receiving_check.por_list[0].num }}</span>
                        </div>
                        <div>
                            <p>
                                <span class="receiving_size_item">{{ receiving_check.por_list[0].size }}</span>
                                
                            </p>
                           
                        </div>
                    </div>
                </div>
                <scroll-view scroll-x v-else>
                    <div class="receiving_content_data1" :style="'width:' + imgWidth">
                        <div class="receiving_shop_img" v-for="(item, index) in receiving_check.por_list" :key="index">
                            <p class="receiving_shop_num" v-if="item.num > 1">{{ item.num }}</p>
                            <img :src="item.img" alt=""/>
                        </div>
                    </div>
                </scroll-view>
            </div>
            <div class="receiving_img"><img :src="receiving_check.extraction_code_img" alt="" :style="receiving_check.status != 2 ? 'opacity: 0.2' : ''" /></div>
            <div class="receiving_code" :style="receiving_check.status != 2 ? 'opacity: 1' : ''">
                <span class="receiving_code_data">{{ receiving_check.extraction_code }}</span>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "tqm",
        data () {
            return {
                finish3x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/finish3x.png',
                receiving_check: {},
                show: false,
                
            }
        },
        computed: {
            imgWidth() {
                if (this.receiving_check.por_list) {
                    if (this.receiving_check.por_list.length > 1) {
                        let width = this.receiving_check.por_list.length * 150;
                        return uni.upx2px(width) + 'px';
                    }
                }
            }
        },
        created() {
            uni.$on('receiving_check_emit', (receiving_check) => {
                this.receiving_check = receiving_check
            })
            uni.$on('receiving_check_show', () => {
                this.show = true
            })
            uni.$on('receiving_check_close', () => {
                this.show = false
            })
        },
        methods: {
            receiving_stop () {
                uni.$emit('receiving_check_close')
                this.show = false;
            }
        }
    }
</script>

<style scoped lang="less">
    .receiving_modal {
        position: fixed;
        width: 100%;
        height: 100%;
        top: 0;
        background: rgba(0, 0, 0, 0.6);
        z-index: 999;
         
    }

    .receiving_modal > div {
        position: absolute;
        width: 640rpx;
        height: 980rpx;
        left: 50%;
        transform: translateX(-50%);
        padding: 0 32rpx 0;
        box-sizing: border-box;
        background: rgb(255, 255, 255);
        border-radius: 20rpx;
        top: 50%;
        margin-top: -481rpx;
    }

    .receiving_content {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        height: 305rpx;
    }

    .receiving_content::after {
        content: '';
        position: absolute;
        top: 340rpx;
        left: 0;
        right: 0;
        border-bottom: 1rpx dashed rgba(0,0,0,.1);
    }

    .receiving_content_title {
        font-size: 28rpx;
        line-height: 30rpx;
        color:  #333333;
        padding: 32rpx 0; 
        border-bottom:1rpx solid rgba(0,0,0,.1);
    }

    .receiving_content_data {
        display: flex;
        align-items: center;
    }

    .receiving_content_data1 {
        display: flex;
        // height: 150rpx;
        align-items: center;
        // margin-top: 32rpx;
    }

    .receiving_content_bottom {
        display: flex;
        align-items: center;
        font-size: 26rpx;
        line-height: 26rpx;
        color: #020202;
        padding-bottom: 28rpx;
    }

    .receiving_shop_img {
        width: 176rpx;
        height: 176rpx;
        border-radius: 16rpx;
        // border: 1px solid #e6e6e6;
        img{
            width: 176rpx;
            height: 176rpx;
            border-radius: 16rpx;
        }
    }

    .receiving_content_data1 .receiving_shop_img {
        position: relative;
        margin-right: 30rpx;
    }

    .receiving_content_data1 .receiving_shop_num {
        position: absolute;
        right: -12rpx;
        top: 0;
        width: 30rpx;
        height: 30rpx;
        border-radius: 50%;
        border: 1px solid #ff3333;
        color: #ff3333;
        font-size: 22rpx;
        text-align: center;
        line-height: 30rpx;
    }

    .receiving_content_item {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        padding-left: 16rpx;
        // height: 120rpx;
        flex:1;
    }

    .receiving_content_item > div {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .receiving_content_item > p {
        // width: 492rpx;
        // height: 61rpx;
        font-size: 32rpx;
        font-weight: 500;
        color: #333333;
         word-break: break-all;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2; /* 超出几行省略 */
          overflow: hidden;
    }

    .receiving_content_item > div p {
        font-size: 26rpx;
        font-weight: bold;
        color: #333333;
    }

    .receiving_size {
        display: flex;
        align-items: center;
        margin:16rpx 0 16rpx;
    }

    .receiving_size_item {
        font-size: 20rpx;
        font-weight: 400;
        color: rgba(153, 153, 153, 1);
        margin-right: 22rpx;
        max-width: 352rpx;
        overflow: hidden;white-space: nowrap;text-overflow: ellipsis;
    }

    .receiving_count {
        font-size: 24rpx;
        
        font-weight: 400;
        color: #999999;
    }

    .receiving_img {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 500rpx;
        height: 500rpx;
        border-radius: 10rpx;
        margin: 48rpx auto 0rpx;
    }

    .receiving_img img {
        width: 100%;
        height: 100%;
    }

    .receiving_code {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 450rpx;
        height: 76rpx;
        border-radius: 5rpx;
        margin: 0 auto;
        font-size: 28rpx;
        
        font-weight: 400;
        color: #333333;
        
    }

    .receiving_name {
        font-size: 26rpx;
        font-weight: 500;
        margin-right: 18rpx;
        color: #242424;
        
    }

    .receiving_code_data {
        font-size: 35rpx;
        font-weight: bold;
        color: rgba(82, 82, 82, 1);
         ;
        
        font-size: 28rpx;
        color: #333333;
    }

    .receiving_finish {
        position: absolute;
        top: 650rpx;
        right: 160rpx;
        width: 108rpx;
        height: 108rpx;
    }

    .receiving_rmb {
        font-size: 32rpx;
        font-weight: 500;
        color: #FA5151;
    }
</style>
