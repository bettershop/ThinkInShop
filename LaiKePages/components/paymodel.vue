<template>
    <view class="payModel" v-if="show">
        <view class="payModel-msg" @touchmove.stop.prevent @click="cancel"></view>
        <view class="payModel-card" @touchmove.stop.prevent>
            <view class="payModel-card-head">
                <text>{{language.paymodel.iptPassword}}</text>
                <image :src="qiandaoguanbiimg" @click="cancel"></image>
            </view>
            <view class="payModel-card-body">
                <view class=""></view>
                <view class="code">
                    <template v-for="(item,index) in password">
                        <view class="code-item" :key="index">
                            <template v-if="index<num">●</template>
                        </view>
                    </template>
                </view>
                <view class="forget">
                    <text @click="forgetPW">{{language.paymodel.forgot}}</text>
                </view>
            </view>
            <view class="payModel-card-keyboard">
                <view class="keyboard">
                    <template v-for="(item,index) of numlist">
                        <view :key="index" v-if="!item.isDelete" :class="{grey: !item.isNumber}"  @click="enterPassword(item.text)">
                            {{ item.text }}
                        </view>
                        <view  v-else @click="deletePassword" style="background: #EAE9E8;">
                            {{language.paymodel.del}}
                        </view>
                    </template>
                </view>
            </view>
            <!-- 适配苹果占位 此处不直接写在父级中的原因是 黑色背景不是在父级中，如果写在父级会造成底部空缺一块 bugid 55337 -->
            <view class="safe-area-inset-bottom" style="background-color: #ffffff;"></view>
        </view>
    </view>
</template>

<script>

    let passlist = []

    for (let i = 0; i < 6; i++) {
        passlist.push({ text: '', pass: '' })
    }

    export default {
        props: ['value'],
        data () {
            return {
                numlist: [
					{
						text: 1,
						isNumber: true,
					},
					{
						text: 2,
						isNumber: true,
					},
					{
						text: 3,
						isNumber: true,
					},
					{
						text: 4,
						isNumber: true,
					},
					{
						text: 5,
						isNumber: true,
					},
					{
						text: 6,
						isNumber: true,
					},
					{
						text: 7,
						isNumber: true,
					},
					{
						text: 8,
						isNumber: true,
					},
					{
						text: 9,
						isNumber: true,
					},
					{
						text: '',
						isNumber: false,
					},
					{
						text: 0,
						isNumber: true,
					},
					{
						text: '删除',
						isNumber: false,
						isDelete: true
					},
				],  // 密码数组
				
                password: passlist,							// 密码对象
                conu: 6,													// 最大密码
                num: 0,													// 密码下标
                qiandaoguanbiimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
                show: false											// 控制密码框
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
        created(){
			this.numlist[11].text = this.language.paymodel.del
            this.setLang();
        },
        methods: {
            /**
             * add密码
             * */
            enterPassword: function (item) {
                if (item === '') {
                    return
                }

                if (this.num >= this.conu) {
                    return
                }
                this.password[this.num].text = '●'
                this.password[this.num].pass = item

                this.num++

                // 密码输入等于6 自动完成
                if (this.num === this.conu) {

                    let pass = ''
                    for (let item of this.password) {
                        pass += item.pass.toString()
                    }

                    this.success(pass)
                }
            },
            /**
             * 删除已输入密码
             * */
            deletePassword: function () {
                if (this.num == 0) {
                    return
                }
                this.password[this.num - 1].text = ''
                this.password[this.num - 1].pass = ''
                this.num--
            },
            /**
             * 关闭密码输入框
             * */
            cancel: function () {
                this.restorePassword()
                this.$emit('cancel')
            },
            /**
             * 初始化组件内数据
             * */
            restorePassword: function () {
                this.show = !this.show
                for (let item of this.password) {
                    item.text = ''
                    item.pass = ''
                }
                this.num = 0
            },
            /**
             * 自动完成回调函数
             * */
            success: function (pass) {
                this.$emit('success', pass)
                this.restorePassword()
            },
            /**
             * 找回密码
             * */
            forgetPW: function () {
                uni.navigateTo({
                    url: '/pagesB/setUp/paymentPassword'
                })
            }
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
    .payModel {
        position: fixed;
        top: 0;
        width: 100%;
        display: flex;
        align-items: flex-end;
        bottom: 0;
        z-index: 9999;
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);

        &-msg {
            width: 100%;
            min-height: 100vh;
            z-index: 100001;
            background-color: rgba(000, 000, 000, 0.5);
            position: absolute;
        }
        &-card {
            width: 100%;
            height: 676rpx;
            display: flex;
            flex-direction: column;
            z-index: 100002;
            &-head {
                height: 112rpx;
                border-bottom: 2rpx solid #DDDDDD;
                display: flex;
                background: #FFFFFF;
                align-items: center;
                padding: 0rpx 40rpx;
                justify-content: flex-end;
				position: relative;
                border-top-left-radius: 24rpx;
                border-top-right-radius: 24rpx;
                image {
                    width: 30rpx;
                    height: 30rpx;
                    margin-right: 20rpx;
					position: absolute;
                }
                text {
                    flex: 1;
                    text-align: center;
                }
                color: #333333;
                font-size: 32rpx;
                font-weight: bold;
            }
            &-body {
                flex: 1;
                background: #FFFFFF;
                .code {
                    display: flex;
                    justify-content: center;
                    margin-top: 50rpx;
                    &-item {
                        width: 80rpx;
                        height: 80rpx;
                        border: 2rpx solid #DDDDDD;
						border-left: none;
                        display: flex;
                        justify-content: center;
                        align-items: center;
						box-sizing: border-box;
						&:first-child {
							border-left: 2rpx solid #DDDDDD;
						}
                    }
                }
                .forget {
                    text-align: end;
                    margin: 24rpx 30rpx;
                    color: #FA5151;
                }
            }
            &-keyboard {
                height: 360rpx;
                .keyboard {
					width: 100.5vw;
                    display: grid;
                    height: 362rpx;
                    background: #EAE9E8;
					grid-template-columns: 1fr 1fr 1fr;
					grid-column-gap: -5rpx;
                    view {
                        background: #FFFFFF;
                        font-size: 40rpx;
                        font-weight: bold;
                        color: #333333;
                        border: 2rpx solid #DDDDDD;
                        margin-left: -1px;
                        margin-top: -1px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
						box-sizing: border-box;
                        &.grey {
							background-color: #EAE9E8;
						}
                    }
                }
                .box0 {
                    grid-column-start: 2;
                }
            }
        }
    }
    .box1 {
        background: #EAE9E8 !important;
    }
</style>
