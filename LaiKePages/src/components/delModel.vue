<template>
    <view class="delmodel" v-if="show">
        <view class="delmodel-card">
            <view class="delmodel-card-body">
                {{ content || language.delModel.text }}
            </view>
            <view class="delmodel-card-btn" :class="{'cardbtn1':!iscancel }">
                <view class="cancel" @click="cancel" v-if="iscancel">
                    {{ canceltext || language.delModel.cancel }}
                </view>
                <view class="confirm" @click="handleClick" v-if="isnoconfirm">
                    {{ confirmtext || language.delModel.confirm}}
                </view>
            </view>
        </view>
    </view>
</template>

<script>
    export default {
        props: ['value', 'content', 'noconfirm', 'nocancel', 'canceltext', 'confirmtext'],
        data () {
            return {
                text: '确认将这个地址删除？',
                show: false,
                isnoconfirm: true,
                iscancel: true
            }
        },
        watch: {
            value: function (val) {
                this.show = val
            },
            show: function (val) {
                this.$emit('input', val)
            }
        },
        created () {
			this.setLang();
			
			this.text = this.language.delModel.text
			
            if (!this.noconfirm) {
                this.isnoconfirm = true
            } else {
                this.isnoconfirm = false
            }

            if (!this.nocancel) {
                this.iscancel = true
            } else {
                this.iscancel = false
            }
        },
        methods: {
            cancel: function () {
                this.show = !this.show
            },
            handleClick: function () {
                this.$emit('on-click')
            }
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
    .delmodel {
        width: 100%;
        min-height: 100vh;
        position: fixed;
        background: rgba(0, 0, 0, 0.6);
        top: 0;
        z-index: 9999;
        display: flex;
        align-items: center;
        justify-content: center;

        &-card {
            width: 550rpx;
            height: 250rpx;
            background: #FFFFFF;
            border-radius: 23rpx;
            display: flex;
            flex-direction: column;

            &-body {
                flex: 1;
                display: flex;
                justify-content: center;
                align-items: center;

                color: #020202;
                font-size: 30rpx;
            }

            &-btn {
                border-top: 2rpx solid #EEEEEE;
                column-count: 2;
                column-rule: 2rpx solid #EEEEEE;
                text-align: center;

                .cancel {
                    color: #999999;
                    font-size: 34rpx;
                    line-height: 100rpx;
                }

                .confirm {
                    color: #020202;
                    font-size: 34rpx;
                    line-height: 100rpx;
                }
            }

            .cardbtn1 {
                column-count: 1;
            }
        }
    }
</style>
