<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <template v-if="showList">
            <heads 
                ishead_w="2" 
                titleColor="#333333"
                :bgColor="bgColor" 
                :title="pageName">
            </heads>
        </template>
        <template v-else-if="!showList && !param">
            <heads 
                ishead_w="2" 
                titleColor="#333333"
                :bgColor="bgColor" 
                :callback="call_back" 
                :title="pageName">
            </heads>
        </template>

        <!-- 模板列表（查看） -->
        <template v-if="showList">
            <view class="list">
                <!-- 查看规则1 默认-->
                <view class="list-add" v-if="typeIndex == 1" v-for="(item, index) in selected" :key="index" @tap="type != 'see' && editMould(index)">
                    <view v-if="typeIndex == 1">
                        <!-- 首件运费 默认-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.yfy}}</view>
                            <view class="container_row_right">
                               <input disabled v-model="item.freight" type="digit" placeholder-class="cl"
                                   :placeholder="language.freight_rules.freightPlacehold" min="0">
                            </view>
                        </view>
                        <!-- 查看省份 默认-->
                        <view class="container_row" @tap="toSheng(item.city_value,item.indeterminateList)">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.choose}}</view>
                            <input style="visibility: hidden;" :value="item.province" type="text"
                                :placeholder="language.freight_rules.choosePlacehold" disabled>
                            <span v-if="item.city_value != ''">{{language.freight_rules.yxz}}</span>
                            <span v-else class="go">{{language.freight_rules.qxz}}</span>
                            <image :src="jiantou"></image>
                        </view>
                    </view>
                </view>
                <!-- 查看规则2 件数-->
                <view class="list-add" v-if="typeIndex == 2" v-for="(item, index) in selected" :key="index" @tap="type != 'see' && editMould(index)">
                    <view v-if="typeIndex == 2">
                         <!-- 查看省份 件数-->
                        <view class="container_row" @tap="toSheng(item.city_value,item.indeterminateList,item.name)">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.choose}}</view>
                            <view class="container_row_right">
                                <span v-if="item.city_value != ''">{{language.freight_rules.yxz }}</span>
                                <span v-else class="go">{{language.freight_rules.qxz }}</span>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- 首件数量 件数-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.sjsl}}</view>
                            <view class="container_row_right">
                                <span>{{item.one}}</span>
                            </view>
                        </view>
                        <!-- 首件运费 件数-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.sjyfy}}</view>
                            <view class="container_row_right">
                                <span class="left_price">{{item.freight}}</span>
                            </view>
                        </view>
                        <!-- 续件数量 件数-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.xjzl}}</view>
                            <view class="container_row_right">
                                <span>{{item.two}}</span>
                            </view>
                        </view>
                        <!-- 续件运费 件数-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.xjyfy}}</view>
                            <view class="container_row_right">
                                <span class="left_price">{{item.Tfreight}}</span>
                            </view>
                        </view>
                    </view>
                </view>
                <!-- 查看规则3 重量-->
                <view class="list-add" v-if="typeIndex == 3" v-for="(item, index) in selected" :key="index" @tap="type != 'see' && editMould(index)">
                    <view v-if="typeIndex == 3">
                         <!-- 查看省份 重量-->
                        <view class="container_row" @tap="toSheng(item.city_value,item.indeterminateList)">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{ language.freight_rules.choose }}</view>
                            <view class="container_row_right">
                                <span v-if="item.city_value != ''">{{language.freight_rules.yxz }}</span>
                                <span v-else class="go">{{language.freight_rules.qxz }}</span>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                        <!-- 首件数量 重量-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.sjzlkg}}</view>
                            <view class="container_row_right">
                                <span>{{item.one}}</span>
                            </view>
                        </view>
                        <!-- 首件运费 重量-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.sjyfy }}</view>
                            <view class="container_row_right">
                                <span class="left_price">{{item.freight}}</span>
                            </view>
                        </view>
                        <!-- 续件数量 重量-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.xjzlkg}}</view>
                            <view class="container_row_right">
                                <span>{{item.two}}</span>
                            </view>
                        </view>
                        <!-- 续件运费 重量-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.xjyfy }}</view>
                            <view class="container_row_right">
                                <span class="left_price">{{item.Tfreight}}</span>
                            </view>
                        </view>                       
                    </view>
                </view>
                <!-- 查看规则4 空状态-->
                <view v-if="selected == ''" class="noFreight">
                    <image :src="noFreight"></image>
                    <div>{{language.freight_rules.nhmyzd}}～</div>
                </view>
            </view>
        </template>
        
        <!-- 模板列表（编辑/添加） -->
        <template v-else-if="!showList && !param">
            <view class="list">
                <!-- 添加规则1 默认-->
                <view v-if="typeIndex == 1">
                    <!-- 首件运费 默认-->
                    <view class="list-add">
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.yfy }}</view>
                            <view class="container_row_right">
                                <input placeholder-style="color:#999999;font-size:32rpx" v-model="freight" type="digit" :placeholder="language.freight_rules.freightPlacehold"
                                    @input="notNegative" min="0">
                                <image class="clear" v-if="freight" @tap="clear('freight')" :src="clearImg"></image>
                            </view>      
                        </view>
                    </view> 
                    <!-- 选择省份 默认-->
                    <view class="list-add" @tap="toSheng(city_value,indeterminateList)">
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.choose}}</view>
                            <span v-if="city_value == ''" class="go">{{language.freight_rules.qxz}}</span>
                            <span v-else>{{language.freight_rules.yxz}}</span>
                            <image :src="jiantou"></image>
                        </view>
                    </view>   
                </view>
                <!-- 添加规则2 件数-->
                <view v-else-if="typeIndex == 2">
                    <!-- 选择省份 件数-->
                    <view class="list-add" @tap="toSheng(city_value,indeterminateList)">
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.choose}}</view>
                            <view class="container_row_right">
                                <span v-if="city_value == ''" class="go">{{language.freight_rules.qxz }}</span>
                                <span v-else>{{language.freight_rules.yxz }}</span>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                    </view>
                    <!-- 其他 件数-->
                    <view class="list-add">
                        <!-- 首件数量 件数-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.sjsl }}</view>
                            <view class="container_row_right">
                                <input placeholder-style="color:#999999;font-size:32rpx" v-model.trim="one" type="number" :placeholder="language.freight_rules.qsrsjsl" min="0">
                                <image class="clear" v-if="one" @tap="clear('one')" :src="clearImg"></image>
                            </view>  
                        </view>
                        <!-- 首件运费 件数-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.sjyfy }}</view>
                            <view class="container_row_right">
                                <input placeholder-style="color:#999999;font-size:32rpx" v-model="freight" type="digit" :placeholder="language.freight_rules.qsrsjyf" min="0"  class="left_price">
                                <image class="clear" v-if="freight" @tap="clear('freight')" :src="clearImg"></image>
                            </view>
                        </view>
                        <!-- 续件数量 件数-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.xjzl }}</view>
                            <view class="container_row_right">
                                <input placeholder-style="color:#999999;font-size:32rpx" v-model.trim="two" type="number" :placeholder="language.freight_rules.qsrxjsl" min="0">
                                <image class="clear" v-if="two" @tap="clear('two')" :src="clearImg"></image>
                            </view>
                        </view>
                        <!-- 续件运费 件数-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.xjyfy }}</view>
                            <view class="container_row_right">
                                <input placeholder-style="color:#999999;font-size:32rpx" v-model="Tfreight" type="digit" :placeholder="language.freight_rules.qsrxjyf" min="0"  class="left_price">
                                <image class="clear" v-if="Tfreight" @tap="clear('Tfreight')" :src="clearImg" ></image>
                            </view>
                        </view>
                    </view>
                </view>
                <!-- 添加规则3 重量-->
                <view v-else-if="typeIndex == 3">
                    <!-- 选择省份 重量-->
                    <view class="list-add" @tap="toSheng(city_value,indeterminateList)">
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.choose}}</view>
                            <view class="container_row_right">
                               <span v-if="city_value == ''" class="go">{{language.freight_rules.qxz }}</span>
                               <span v-else>{{language.freight_rules.yxz }}</span>
                               <image :src="jiantou"></image>
                            </view>
                        </view>
                    </view>
                    <!-- 其他 重量-->
                    <view class="list-add">
                        <!-- 首件数量 重量-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.sjzl }}</view>
                            <view class="container_row_right">
                                <input placeholder-style="color:#999999;font-size:32rpx" v-model.trim="one" type="number" :placeholder="language.freight_rules.qsrsjzl" min="0">
                                <image class="clear" v-if="one" @tap="clear('one')" :src="clearImg"></image>
                            </view>  
                        </view>
                        <!-- 首件运费 重量-->    
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.sjyfy }}</view>
                            <view class="container_row_right">
                                <input placeholder-style="color:#999999;font-size:32rpx" v-model="freight" type="digit" :placeholder="language.freight_rules.qsrsjyf" min="0"  class="left_price">
                                <image class="clear" v-if="freight" @tap="clear('freight')" :src="clearImg"></image>
                            </view>
                        </view>
                        <!-- 续件数量 重量-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.xfzl }}</view>
                            <view class="container_row_right">
                                <input placeholder-style="color:#999999;font-size:32rpx" v-model.trim="two" type="number" :placeholder="language.freight_rules.qsrxjzl" min="0">
                                <image class="clear" v-if="two" @tap="clear('two')" :src="clearImg"></image>
                            </view>
                        </view>
                        <!-- 续件运费 重量-->
                        <view class="container_row">
                            <span class="red-requre">*</span>
                            <view class="container_row_left">{{language.freight_rules.xjyfy }}</view>
                            <view class="container_row_right">
                                <input placeholder-style="color:#999999;font-size:32rpx" v-model="Tfreight" type="digit" :placeholder="language.freight_rules.qsrxjyf" min="0"  class="left_price">
                                <image class="clear" v-if="Tfreight" @tap="clear('Tfreight')" :src="clearImg" ></image>
                            </view>
                        </view>
                    </view>
                </view>
            </view>
        </template>
        
        <!-- 底部按钮 -->
        <template v-if="type != 'see'">
            <view class="container_bottomBtn">
                <!-- 添加规则 -->
                <view v-if="showList" class="one" @tap="goAddMould">{{language.freight_rules.tjgz}}</view>
                <!-- 保存规则 -->
                <view v-if="!showList && !param" class="one" :class="{disable:!ok_status}" @tap="saveOk">{{language.freight_rules.save}}</view>
                <!-- 删除规则 -->
                <view v-if="!showList && !param && firstAdd" class="two" @tap="delMould">{{language.freight_rules.sc}}</view>
            </view>
        </template>
        <!-- 添加编辑成功 -->
        <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    {{language.chooseGoods.sccg}}
                </view>
            </view>
        </view>
        <showToast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" @richText="richText" @confirm="confirm"></showToast>
    </view>
</template>

<script>
    import showToast from '@/components/aComponents/showToast.vue'
    export default {
        data() {
            return {
                firstAdd: false, // 第一次点击添加不显示 修改才出现
                noFreight: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/dizhiNo.png',
                clearImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
                enter_type: '',
                param: false, // 添加和编辑参数
                pageName: "指定运费",
                selected: [],
                jselected: [],
                one: '', // 首件数量
                freight: '', // 首件运费
                two: '', // 续件重量
                Tfreight: '', //续件运费
                city_value: '', //省份ID
                indeterminateList: '', //半选状态省份ID
                name: '', //省份名称
                firstWight_temp: '', // 输入框首件数量
                firstFreith_temp: '',
                nextWight_temp: '',
                nextFreit_temp: '',
                freight_temp: '',
                is_sus:false,
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',

                showList: true,
                title: '添加模板',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
                typeIndex: 1, // 1是默认 2是件数 3是重量
                save_type: "添加",
                save_status: false,
                freight: '',
                ok_status: false, // 保存状态
                type: '', // see:查看  edit：编辑
                freight_sheng: {},
                
                bgColor: [
                    {
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
                is_showToast_obj: {
                    
                }, //imgUrl提示图标  title提示文字
            };
        },
        onShow() {
            this.isLogin()
            if (uni.getStorageSync('freight_sheng')) {
                this.freight_sheng = uni.getStorageSync('freight_sheng')
                this.city_value = this.freight_sheng.id//省份id
                this.name = this.freight_sheng.name//省份名称
                this.indeterminateList = this.freight_sheng.indeterminateList//半选状态省份
                uni.removeStorageSync('freight_sheng')
            }
            this.pageName = this.language.freight_add.zdyf
           
        },
        onLoad(option) {
            if (uni.getStorageSync('freight_rules')) {
                this.selected = uni.getStorageSync('freight_rules');
            }
            if (option.typeIndex != undefined) {
                this.typeIndex = option.typeIndex;
            }

            if (option.type != undefined) {
                this.type = option.type;
            }

        },
        components:{
            showToast
        },
        watch: {
            showList(newName, oldName) {
                if (newName) {
                    this.pageName = this.language.freight_add.zdyf
                }
            },

            freight_temp(newName, oldName) {
                if (this.freight_temp != '' && this.enter_type == 'freight') {

                    this.save_status = true;
                } else {
                    this.save_status = false;
                }

            },
            firstWight_temp(newName) {
                if (this.firstWight_temp != '' && this.enter_type == 'one') {
                    this.save_status = true;
                } else {
                    this.save_status = false;
                }

            },
            firstFreith_temp(newName) {
                if (this.firstFreith_temp != '' && this.enter_type == 'freight') {
                    this.save_status = true;
                } else {
                    this.save_status = false;
                }

            },
            nextWight_temp(newName) {
                if (this.nextWight_temp != '' && this.enter_type == 'two') {
                    this.save_status = true;
                } else {
                    this.save_status = false;
                }

            },
            nextFreit_temp(newName) {
                if (this.nextFreit_temp != '' && this.enter_type == 'Tfreight') {
                    this.save_status = true;
                } else {
                    this.save_status = false;
                }

            },
            city_value(newName) {
                this.okStatus();
            },
        },
        methods: {
            okStatus() {
                if (this.typeIndex == 1) { // 默认的判断
                    if (this.freight >= 0 && this.city_value != '') {
                        this.ok_status = true
                    } else {
                        this.ok_status = false
                    }
                } else {
                    if (this.one > 0 && this.two > 0 && this
                        .city_value != '') {
                        this.ok_status = true
                    } else {
                        this.ok_status = false
                    }
                }
            },
            clear(pre_name) {
                this.$set(this, pre_name, '')
            },
            notNegative(e) {
                //限制小数位
                if (this.decimal > 0) {
                    let regex = new RegExp(`^\\d+(?:\\.\\d{0,${this.decimal}})?`, "g");
                    e.target.value = (e.target.value.match(regex)[0]) || null;
                }
                //限制长度
                if (this.maxLength > 0 && e.target.value.length >= this.maxLength) {
                    e.target.value = e.target.value.slice(0, this.maxLength);
                }

            },

            clearValue(type) {
                this.freight = ''
            },
            goEnter(enter_type) {
                this.enter_type = enter_type
                if (this.enter_type == 'one') {
                    this.firstWight_temp = this.one
                } else if (this.enter_type == 'freight' && this.typeIndex != 1) {
                    this.firstFreith_temp = this.freight
                } else if (this.enter_type == 'two') {
                    this.nextWight_temp = this.two
                } else if (this.enter_type == 'Tfreight') {
                    this.nextFreit_temp = this.Tfreight
                } else if (this.enter_type == 'freight' && this.typeIndex == 1) {
                    this.freight_temp = this.freight
                }
                this.param = true;

            },
            // 保存
            saveEnter() {

                if (this.save_status) {
                    if (this.enter_type == 'one' && this.firstWight_temp > 0) {
                        this.one = this.firstWight_temp
                    } else if (this.enter_type == 'freight' && this.firstFreith_temp >= 0 && this.typeIndex != 1) {
                        this.freight = this.firstFreith_temp
                    } else if (this.enter_type == 'two' && this.nextWight_temp > 0) {
                        this.two = this.nextWight_temp
                    } else if (this.enter_type == 'Tfreight' && this.nextFreit_temp >= 0) {
                        this.Tfreight = this.nextFreit_temp
                    } else if (this.enter_type == 'freight' && this.freight_temp >= 0 && this.typeIndex == 1) {
                        this.freight = this.freight_temp
                    } else {
                        uni.showToast({
                            title: "输入值不能小于0",
                            icon: 'none',
                            duration: 1500,
                        })
                        return false;
                    }
                    this.okStatus();
                    this.param = false;
                }
            },
            call_back() { // 点击返回
                this.showList = true
            },
            showParam() { // 添加修改编辑参数的时候
                this.param = false;
            },
            // 取消弹窗
            richText(){
                this.is_showToast=0
            },
            // 确认弹窗
            confirm(){
               this.is_showToast=0
               this.selected.splice(this.save_type, 1)
               uni.setStorageSync('freight_rules', this.selected)
               this.is_sus = true
               
               setTimeout(() => {
                   this.is_sus = this.param = false;
                   this.showList = true
                   this.param = false;
               }, 1000)
            },
            delMould(index) {
                this.is_showToast = 4
                this.is_showToast_obj.title = this.language.freight_add.Tips
                this.is_showToast_obj.content = this.language.freight_rules.sfsc
                this.is_showToast_obj.button = this.language.showModal.cancel
                this.is_showToast_obj.endButton = this.language.showModal.confirm
                
            },
            editMould(index) {
                this.showList = false
                this.firstAdd = true
                this.pageName = this.language.freight_rules.bjgz
                if (this.typeIndex == 1) {
                    this.freight = this.selected[index].freight
                    this.city_value = this.selected[index].city_value
                    this.name = this.selected[index].name
                } else {
                    this.one = this.selected[index].one
                    this.freight = this.selected[index].freight
                    this.two = this.selected[index].two
                    this.Tfreight = this.selected[index].Tfreight
                    this.city_value = this.selected[index].city_value
                    this.name = this.selected[index].name
                }
                this.save_type = index;
            },
            goAddMould() {
                this.save_type = this.language.freight_rules.tj
                this.pageName = this.language.freight_rules.tjgz
                this.showList = false
                this.firstAdd = false
                this.freight = ''
                this.one = ''
                this.freight = ''
                this.two = ''
                this.Tfreight = ''
                this.freight_sheng = {}
                this.city_value = ''
                uni.removeStorageSync('freight_sheng')
            },
            
            changeLoginStatus() {},
            toSheng(freight_sheng_id,indeterminateList,name) {
                let url = '/pagesA/myStore/freight_sheng?show_type=' + this.type

                if (freight_sheng_id) {
                    url += '&freight_id=' + freight_sheng_id
                }
                if(this.type == 'see') {
                    url += '&viewCity=' +  name
                } else if(this.type == 'edit') {
                    if(name) {
                        url += '&viewCity=' + name + '&isOpenCity=true'
                    }
                } else {
                    if(indeterminateList) {
                        url += '&indeterminateList=' + indeterminateList
                    }
                }
                

                uni.navigateTo({
                    url
                })
            },
            saveOk() {
                if(this.city_value == ''){
                    uni.showToast({
                        title: this.language.freight_sheng.chooseTips,
                        icon: 'none'
                    })
                    return
                }
                //判断字段不能为空
                this.okStatus()
                //为空提示
                if (!this.ok_status) {
                    uni.showToast({
                        title: this.language.freight_rules.qwssr,
                        icon: 'none'
                    })
                    return false;
                }
                var that = this;
                let selectItem = {};
                if (this.typeIndex == 1) {
                    if (this.freight < 0) {
                        uni.showToast({
                            title: this.language.freight_rules.yfbnwf,
                            icon: 'none'
                        })
                        return
                    }
                    if (!this.freight_sheng) {
                        uni.showToast({
                            title: this.language.freight_rules.choosePlacehold,
                            icon: 'none'
                        })
                        return
                    }
                    selectItem.freight = this.freight
                    selectItem.name = this.name
                    selectItem.city_value = this.city_value
                    if (that.save_type == this.language.freight_rules.tj) {
                        that.selected.push(selectItem)
                    } else {
                        that.selected.forEach(function(item, i) {
                            if (i == that.save_type) {
                                that.selected[i] = selectItem;
                            }
                        })
                    }
                } else if (this.typeIndex == 2) {
                    if (this.one < 0 || this.freight < 0 || this.two < 0 || this.Tfreight < 0) {
                        uni.showToast({
                            title: this.language.freight_rules.bnwf,
                            icon: 'none'
                        })
                        return
                    }

                    selectItem.one = this.one
                    selectItem.freight = this.freight
                    selectItem.two = this.two
                    selectItem.Tfreight = this.Tfreight
                    selectItem.name = this.name
                    selectItem.city_value = this.city_value
                    selectItem.indeterminateList = this.indeterminateList
                    if (that.save_type == this.language.freight_rules.tj) {
                        that.selected.push(selectItem)
                    } else {
                        that.selected.forEach(function(item, i) {
                            if (i == that.save_type) {
                                that.selected[i] = selectItem;
                            }
                        })
                    }
                } else if (this.typeIndex == 3) {
                    selectItem.one = this.one
                    selectItem.freight = this.freight
                    selectItem.two = this.two
                    selectItem.Tfreight = this.Tfreight
                    selectItem.name = this.name
                    selectItem.city_value = this.city_value

                    if (that.save_type == this.language.freight_rules.tj) {
                        that.selected.push(selectItem)
                    } else {
                        that.selected.forEach(function(item, i) {
                            if (i == that.save_type) {
                                that.selected[i] = selectItem;
                            }
                        })
                    }
                }


                this.pageName = this.language.freight_rules.zdyf
                that.showList = true
                uni.setStorageSync('freight_rules', that.selected)
                
            }
        }
    };
</script>
<style>
	    page{
	        background-color: #f4f5f6;
	    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    
    .list-add{
        padding: 24rpx 0;
    }
    .list-add:not(:first-child) {
        margin-top: 16rpx;
        border-radius: 24rpx !important;
    }
    .list-add:first-child{
        border-radius: 0 0 24rpx 24rpx !important;
    }
    .uni-input-placeholder {
        display: block;
    }

    .container {
        min-height: 100vh;

        .list {
            background: #f4f5f6;
            border-radius: 0px 0px 24rpx 24rpx;
            margin-bottom: 54px;
            .noFreight {
                display: flex;
                justify-content: center;
                flex-direction: column;
                text-align: center;
                align-items: center;
                width: 750rpx;
                height: 668rpx;
                background: #FFFFFF;
                border-radius: 0px 0px 24rpx 24rpx;
                font-size: 28rpx;
                
                font-weight: 400;
                color: #333333;
                div{
                    margin-top:-52rpx;
                }
                image {
                    width: 750rpx;
                    height: 460rpx;
                }
            }


            &-add {
                border-radius: 6px;
                background-color: #FFFFFF;
            }
        }

        &_row:nth-last-child(1) {
            border-bottom: none;
        }

        &_row {
            display: flex;
            align-items: center;
            margin: 0 30rpx;
            height: 90rpx;
            border-bottom: 1px solid #EDEDED;

            .go {
                color: #999999;
                text-align: start;
                font-size: 32rpx;
            }

            &_left {

                margin-right: 200rpx;
                font-size: 28rpx;
                color: #333333;
                width: 170rpx;
            }

            .cl {
                color: red;
                text-align: end;
            }


            .red-requre {
                color: red;
                margin-right: 5px;
                flex: unset;
            }

            span {
                text-align: start;
            }



            input {
                text-align: end;
                flex: 1;
                font-size: 28rpx;
            }

            .clear {
                width: 14px;
                height: 14px;
                margin-left: 11px;
            }

            image {
                width: 30rpx;
                height: 54rpx;
                margin-left: 20rpx;
            }
        }

        &_bottomBtn {
            
            color: #FFFFFF;
            border: 0 !important;
            align-items: center;
            justify-content: center;
            
        
            .one {
                margin: 0 auto;
                display: block;
                background-color: #FA5151;
                color: #FFFFFF;
                border: 0 !important;
                display: flex;
                align-items: center;
                justify-content: center;
                height: 92rpx;
                font-size: 16px;
                width: 95%;
                border-radius: 52rpx;

            }

            .two {
                margin: 40rpx auto 16rpx auto;
                width: 95%;
                background-color: #F6F6F6;
                color: #FA5151;
                border: 1px solid #FA5151;
                display: flex;
                align-items: center;
                justify-content: center;
                height: 92rpx;
                font-size: 16px;
                border-radius: 52rpx;
            }
        }
    }
    .xieyi {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, 0.5);
        z-index: 99;
        display: flex;
        justify-content: center;
        align-items: center;
        > view {
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #ffffff;
            border-radius: 24rpx;
            .xieyi_btm {
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0, 0, 0, 0.1);
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_title {
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 32rpx;
                
                font-weight: 500;
                color: #333333;
                line-height: 44rpx;
                margin-top: 64rpx;
                margin-bottom: 32rpx;
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_text {
                width: 100%;
                max-height: 236rpx;
                overflow-y: scroll;
                padding: 0 32rpx;
                box-sizing: border-box;
            }
        }
    }
    
    .container_row {
        border-bottom: none;
        padding: 8rpx 0;
        input{
            text-align: start;
        }
    }
    .container_row_left {
        width: 180rpx;
        margin-right: 24rpx;
        font-size: 32rpx;
    }
    
    .container_row_right {
        flex: 1;
        height: 76rpx;
        border-radius: 16rpx;
        border: 2rpx solid rgba(0,0,0,0.1);
        padding: 0 16rpx;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        span{
            flex:1;
        }
        .left_price{
            color:#FA5151;
        }
        >image{
            width: 32rpx;
            height: 44rpx;
        }
    }
</style>
