<template>
    <div class="container">
  
        <template v-if="pageFlag">
            <heads returnR="no" @returnC="returnC" :title="title" :ishead_w="2" :bgColor="bgColor"
                :titleColor="'#333333'"></heads>
            <div class='home_navigation'>
                <div class='home_input'>
                    <img class='searchImg' :src="serchimg" alt="">
                    <input type="text" v-model="searchtxt" :placeholder="language.setAttr.qsrgjz" id='input'
                        placeholder-style="color:#999999;font-size: 26rpx" name="sourch" />
                    <image v-show="searchtxt.length > 0" @click="searchtxt = ''" class="cancel" :src="sc_icon" mode="">
                    </image>
                </div>
                <div class='search_btn' @tap='attrSearch'>{{language.search2.search.search}}</div>
            </div>
            <div style="height: 88rpx;"></div>

            <div>
                <ul class="choose_attr">
                    <template v-for="(item, index) in attrList">
                        <li v-if="item.show" @tap="_chooseAttr(item, index)" :key="item.id">
                            <img class="img" :src="item.status?rxz:hwxz" />
                            {{ item.text }}
                        </li>
                    </template>
                </ul>
                <view style="height: 100rpx;"></view>
                <div class='empty_play' v-if="iShow == true">
                    <img :src="no_search" />
                    <p> {{language.setAttr.zwssjg}}</p>
                </div>
                <div class="chooseBottom safe-area-inset-bottom">
                    <div class="chooseBottom_left" @tap="openChecked">
                        {{language.setAttr.yx}}
                        <span>{{chooseAttrList.length}}</span>
                        {{language.setAttr.gsxmc}}
                    </div>
                    <div class="btn1" @tap="_saveAttr">{{language.setAttr.storage}}</div>
                </div>
            </div>
        </template> 
        <template v-else >
            <heads :title="title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads> 
            <view class="attrContent" >
                    <!-- 属性名称搜索  增加 供应商商品不显示 规格  type != 'gys'-->
                <view class="attrContent_top" v-if="!(goodsType==1&&pageStatus==1) && type != 'gys'">
                    <view class="attrContent_top_left">{{language.setAttr.Attr[1]}}</view>
                    <view class="attrContent_top_center" @tap="_clickAttr">
                        <input class="input" type="text" :placeholder="language.setAttr.Attr[2]"
                            placeholder-style="font-size:28rpx;" :value="showAttrName" />
                        <view class="btn" @tap.stop="addName">{{language.setAttr.shop_Attr[1]}}</view>
                    </view> 
                </view> 
                <!--  增加 供应商商品不显示 规格  type != 'gys'-->
                <template v-if="showAttrName && !(goodsType==1&&pageStatus==1) && type != 'gys'">
                    <!-- 自选属性名的展示 -->
                    <view class="attrBox" v-for="(item, index) in attrDataList" :key="index" v-if="index">
                        <view class="attrBox_top">
                            <view class="attrName">
                                <view class="text">{{index}}</view>
                                <view class="attrTips">（{{language.setAttr.qxzsxz}}）</view>
                            </view>
                            <view class="attrValueBtn" @tap="addValue(index)">
                                <image :src="addattr_add" mode=""></image>
                                ＋{{language.setAttr.tjsxz}}
                            </view>
                            <image @tap="deleteAttr(index)" class="delete_img" :src="delete_img" mode="widthFix">
                            </image>
                        </view>
                        <view class="attrBox_ul">
                            <view :class="{ active: it.status }" @tap="_chooseAttrData(it, item, index)"
                                v-for="(it, id) in item" :key="id">
                                {{it.value}}
                                <image v-if="it.isAdd" @tap="removeValue(index, id)" :src="sc_icon" mode=""></image>
                            </view>
                        </view>
                    </view>
                </template>
                <scroll-view scroll-x="true" class="scroll-view_H" >
                    <div class="table">
                        <div class="tr">
                            <div class="th" v-for="(item, index) in attr_group" :key="index">{{ item.attr_group_name }}
                            </div>
                            <div class="th">{{language.uploadPro.price[type=='gys'?8:0]}} </div>
                            <div class="th">{{language.uploadPro.price[type=='gys'?10:1]}}</div>
                            <div class="th">{{language.setAttr.price[2]}}</div>
                            <div class="th" v-if="type != 'yushou' && xnHxType != 2">
                                <!-- 核销次数 -->
                                <template v-if="goodsType == 1">
                                    核销次数
                                </template>
                                <!-- 库存 -->
                                <template v-else>
                                    {{language.setAttr.price[3]}}
                                </template>
                            </div>
                            <div class="th">{{language.setAttr.price[4]}}</div>
                        </div>
                        <!-- 增加判断 如果 type == gys 则不可以修改 '售价' 以外的信息 -->
                        <div class="tr" v-for="(items, index) in attr_arr" :key="index">
                            <div class="td" v-for="(item, index1) in items.attr_list" :key="index1">{{ item.attr_name }}
                            </div>
                            <div class="td">
                                <div class="sh">
                                    <input @input="checkCbj($event,index)" v-if="index >= 0" class="input" :class="{'input-color':type == 'gys'}" type="number"
                                        v-model="items.cbj"  :disabled="type == 'gys'"/>
                                </div>
                            </div>
                            <div class="td">
                                <div class="sh">
                                    <input @input="checkYj($event,index)" v-if="index >= 0" class="input" :class="{'input-color':type == 'gys'}"  type="number"
                                        v-model="items.yj" :disabled="type == 'gys'"/>
                                </div>
                            </div>
                            <!-- 售价 -->
                            <div class="td">
                                <div class="sh">
                                    <input @input="checkSj($event,index)" v-if="index >= 0" class="input" type="number"
                                        v-model="items.sj" />
                                </div>
                            </div>
                            <div class="td" v-if="type != 'yushou' && xnHxType != 2">
                                <div class="sh">
                                    <input v-if="index >= 0" class="input"  :class="{'input-color':type == 'gys'}"  type="number"
                                        :style="{opacity: goodsType==1 && pageStatus==1?'0.5':'1'}"
                                        :disabled="(goodsType==1 && pageStatus==1) || type == 'gys'" v-model="items.kucun" />
                                </div>
                            </div>
                            <div class="td">
                                <div style="position: relative;">
                                    <span style="opacity: 0;">1</span> 
                                    <view v-if="items.img" class="upImg" style="border: 0;">
                                        <!-- 如果 type == gys 隐藏删除按钮 -->
                                        <image @tap="removeImg(index)" class="cha" :src="sc_icon" alt="" v-if="type != 'gys'"></image>
                                        <img class="see" :src="items.img" alt="">
                                    </view>
                                    <div v-else class="upImg" @tap="upImgClick(index)">
                                        <img class="shac" :src="shac" alt="">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="nodata" v-if="(chooseAttr.length < 1 || attr_group.length < 1) && attr_arr.length < 1">
                        <image :src="nodata_set" mode="widthFix"></image>
                        <div class="noDataTip">
                            <p>{{language.setAttr.zsmyxg}}</p>
                            <p>{{language.setAttr.qxxzs}}</p>
                        </div>
                    </div>
                </scroll-view>

                <view class="new_btn" @tap="_complete">
                    <div class='new_bottom'>{{language.setAttr.complete}}</div>
                </view>
            </view>

            <div v-if="false">
                <div class="chooseAttrStyle">
                    <div>{{language.setAttr.Add_how}}</div>
                    <ul>
                        <li @tap="attrStyle = 1">
                            <img :src="attrStyle == 1 ? quan_hei : quan_hui" />
                            {{language.setAttr.title2}}
                        </li>
                        <li @tap="attrStyle = 2">
                            <img :src="attrStyle == 2 ? quan_hei : quan_hui" />
                            {{language.setAttr.Attr[0]}}
                        </li>
                    </ul>
                </div>
                <template v-if="attrStyle == 1">
                    <div class="chooseAttr">
                        <div>{{language.setAttr.Attr[1]}}</div>
                        <input class="input" type="text" :placeholder="language.setAttr.Attr[2]"
                            placeholder-style="font-size:28upx" :value="chooseAttr" disabled @tap="_clickAttr" />
                        <img class="jiantou" :src="jiantou" />
                    </div>
                    <div class="attrBox" v-for="(item, index) in attrDataList" :key="index" v-if="index">
                        <div class="attr_hr"></div>
                        <div class="attrBoxTitle">
                            {{ index }}
                            <text class="text">({{language.setAttr.Attr[3]}})</text>
                            <img @tap="deleteAttr(index)" class="delete_img" :src="delete_img" />
                        </div>
                        <div class="attrList">
                            <div :class="{ active: it.status }" @tap="_chooseAttrData(it, item, index)"
                                v-for="(it, id) in item" :key="id">{{ it.value }}</div>
                        </div>
                    </div>
                    <scroll-view scroll-x="true" class="scroll-view_H" >
                        <div class="table">
                            <div class="tr">
                                <div class="th" v-for="(item, index) in attr_group" :key="index">
                                    {{ item.attr_group_name }}</div>
                                <div class="th">{{language.setAttr.price[0]}}</div>
                                <div class="th">{{language.setAttr.price[1]}}</div>
                                <div class="th">{{language.setAttr.price[2]}}</div>
                                <div class="th">{{language.setAttr.price[3]}}</div>
                            </div>
                            <div class="tr" v-for="(items, index) in attr_arr" :key="index">
                                <div class="td" v-for="(item, index1) in items.attr_list" :key="index1">
                                    {{ item.attr_name }}</div>
                                <div class="td">
                                    <div class="sh">
                                        {{ index == 0 ? items.cbj : '' }}
                                        <input v-if="index > 0" class="input" type="number" v-model="items.cbj" />
                                    </div>
                                </div>
                                <div class="td">
                                    <div class="sh">
                                        {{ index == 0 ? items.yj : '' }}
                                        <input v-if="index > 0" class="input" type="number" v-model="items.yj" />
                                    </div>
                                </div>
                                <div class="td">
                                    <div>
                                        {{ index == 0 ? items.sj : '' }}
                                        <input v-if="index > 0" class="input" type="number" v-model="items.sj" />
                                    </div>
                                </div>
                                <div class="td">
                                    <div class="sh">
                                        {{ index == 0 ? items.kucun : '' }}
                                        <input v-if="index > 0" class="input" type="number" v-model="items.kucun" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </scroll-view>
                </template>
                <template v-else>
                    <div class="addAttr" :class="{ 'border-bottom': addList.length > 0 }">
                        <div class="addLeft">{{language.setAttr.Attr[1]}}</div>
                        <input type="text" :placeholder="language.setAttr.shop_Attr[0]" v-model="addAttrName" />
                        <button @tap="addAttr">{{language.setAttr.shop_Attr[1]}}</button>
                    </div>
                    <div v-for="(item, index) in addList" :key="index">
                        <div class="addAttrName">
                            <div class="addLeft">{{ item.attr_group_name }}</div>
                            <input type="text" :placeholder="language.setAttr.shop_Attr[2]" v-model="item.childValue" />
                            <button class="addBtn" @tap="addAttrValue(item)">{{language.setAttr.add}}</button>
                            <button class="removeBtn" @tap="removeAttrName(index)">{{language.setAttr.deletes}}</button>
                        </div>
                        <div class="addAttrValue" v-if="item.attr_list.length > 0">
                            <div class="addLeft"></div>
                            <ul>
                                <li v-for="(items, indx) in item.attr_list" :key="indx"
                                    @tap="removeAttrValue(item, indx)">
                                    <div class="icon"></div>
                                    {{ items.attr_name }}
                                </li>
                            </ul>
                        </div>
                    </div>
                    <scroll-view scroll-x="true" class="scroll-view_H">
                        <div class="table">
                            <div class="tr">
                                <div class="th" v-for="(item, index) in addList" :key="index"
                                    v-if="item.attr_list.length > 0">{{ item.attr_group_name }}</div>
                                <div class="th">{{language.setAttr.price[0]}}</div>
                                <div class="th">{{language.setAttr.price[1]}}</div>
                                <div class="th">{{language.setAttr.price[2]}}</div>
                                <div class="th">{{language.setAttr.price[3]}}</div>
                            </div>
                            <div class="tr" v-for="(items, index) in attr_arr" :key="index">
                                <div class="td" v-for="(item, index1) in items.attr_list" :key="index1">
                                    {{ item.attr_name }}</div>
                                <div class="td">
                                    <div class="sh">
                                        {{ index == 0 ? items.cbj : '' }}
                                        <input v-if="index > 0" class="input" type="number" v-model="items.cbj" />
                                    </div>
                                </div>
                                <div class="td">
                                    <div class="sh">
                                        {{ index == 0 ? items.yj : '' }}
                                        <input v-if="index > 0" class="input" type="number" v-model="items.yj" />
                                    </div>
                                </div>
                                <div class="td">
                                    <div class="sh">
                                        {{ index == 0 ? items.sj : '' }}
                                        <input v-if="index > 0" class="input" type="number" v-model="items.sj" />
                                    </div>
                                </div>
                                <div class="td">
                                    <div class="sh">
                                        {{ index == 0 ? items.kucun : '' }}
                                        <input v-if="index > 0" class="input" type="number" v-model="items.kucun" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </scroll-view>
                </template>
                <view class="new_btn" @tap="_complete">
                    <div class='new_bottom'>{{language.setAttr.complete}}</div>
                </view>
            </div>
        </template>

        <div v-if="checkedFlag" class="mask" style="bottom: 98rpx" @tap="openChecked">
            <div class="checkedBox" @tap.stop>
                <div class="checkedBox_top">
                    {{language.setAttr.yxsxm}}

                    <div @tap="removeCheckAttr">
                        <img :src="delete_img" alt="">
                        {{language.setAttr.qk}}
                    </div>
                </div>

                <div class="checkedBox_ul">
                    <div v-for="item,index of chooseAttrList" :key="item.id">
                        {{item.text}}
                        <div class="icon" @tap="_chooseAttr(item, index)"></div>
                    </div>
                </div>
            </div>
        </div>

        <view class="mask" v-if="addAttrFlag" style="z-index: 999;">
            <view class="addMask">
                <view class="addMask_title">{{language.setAttr.tjsx}}</view>
                <input v-model="addAttrVal" type="text" :placeholder="language.setAttr.qsrsxz"
                    placeholder-style="color: color: #999999;">
                <view class="btn">
                    <view @tap="closeName">{{language.setAttr.qx}}</view>
                    <view @tap="inspect">{{language.setAttr.tj}}</view>
                </view>
            </view>
        </view>

        <view class="mask" v-if="addAttrValueFlag" style="z-index: 999;">
            <view class="addMask">
                <view class="addMask_title">{{language.setAttr.tjsxz}}</view>
                <input v-model="addAttrValueVal" type="text" :placeholder="language.setAttr.qsrsxz "
                    placeholder-style="color: color: #999999;">
                <view class="btn">
                    <view @tap="closeValue">{{language.setAttr.qx }}</view>
                    <view @tap="inspect2">{{language.setAttr.tj }}</view>
                </view>
            </view>
        </view>

    </div>
</template>

<script>
    export default {
        data() {
            return {
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                chooseImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/chooseMe.png',
                quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
                quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehei2x.png',
                delete_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/setAttr_delete.png',
                serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
                nodata_set: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/nodata_set.png',
                sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/sc_icon.png',
                addattr_add: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/addattr_add_new',
                title: '设置属性',
                pageFlag: false, //页面切换的开关，默认是选择属性名称，false是设置属性的主页面

                attrList: [], //所有的属性名称
                chooseAttr: '', //选中的属性名称
                attrDataList: {}, //所有的属性值
                attr_group: [], //最终返回给后台的属性数据[{attr_group_name:'尺寸',attr_list:[{attr_name:'L'}]}]
                attr_arr: [], //最终返回给后台的表格数据[{attr_list:[],bar_code:'',cbj:''}]
                costM: '', //默认成本价
                oldM: '', //默认原价
                sellM: '', //默认售价
                stock: '', //默认库存
                stockWarn: '', //库存预警
                arrimg: '', //默认图片

                attrStyle: 1, //属性添加方式 1、选择  2、自定义
                addList: [], //自定义的属性
                addAttrName: '', //自定义属性名

                searchtxt: '',
                checkedFlag: false,

                addAttrFlag: false,
                addAttrVal: '',
                addAttrValueFlag: false,
                addAttrValueVal: '',
                addAttrStr: '', // 添加属性名称的字符串，逗号隔开
                addAttrObj: {},
                addValue_pname: '', // 添加的属性值所属的属性名
                type: '',
                shac: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xiangji2x.png',
                hwxz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/hwxz.png',
                rxz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/rxz.png',
                no_search: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/no_search.png',
                pageStatus: 0,
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                inspectList: [],
                inspectList2: [],
                iShow: false,
                goodsType: '', //0实物商品 1虚拟商品
                xnHxType: '', //虚拟商品 - 1线下核销 2无需核销
                yushou_type:''// 预售商品 1-定金模式  2-订货模式
            };
        },
        computed: {
            chooseAttrList() {

                let arr = [];

                arr = this.attrList.filter(item => {
                    return item.status
                });

                return arr
            },
            showAttrName() {

                let str = ''
                if (this.addAttrStr) {
                    str += ',' + this.addAttrStr
                }

                if (!Array.isArray(this.chooseAttr) && this.chooseAttr.length > 0) {
                    str += ',' + this.chooseAttr
                }

                str = str.replace(',', '')
                return str
            }
        },
        watch: {
            attrStyle(newVal) {
                if (newVal == 1) {
                    this._table();
                } else {
                    this._abbTable();
                }
            }
        },
        onLoad(option) {
            this.LaiKeTuiCommon.getLKTApiUrl()
            if (option.type) {
                this.type = option.type.trim()
            }
            if (uni.getStorageSync('upload_attr_group')) {
                this.attr_group = uni.getStorageSync('upload_attr_group');
                this.attr_arr = uni.getStorageSync('upload_attr_arr');

                this.chooseAttr = uni.getStorageSync('upload_chooseAttr');

                this.addAttrStr = uni.getStorageSync('upload_addAttrStr');

                this.addAttrObj = uni.getStorageSync('upload_addAttrObj');

                this._axios(this.chooseAttr, 'show');
            }

            // costM成本价  oldM原价  sellM售价  stock库存  stockWarn库存预警  unit商品单位  pageStatus[0-上传，1-编辑，2-查看]
            this.costM = option.costM;
            this.oldM = option.oldM;
            this.sellM = option.sellM;
            this.stock = option.stock;
            this.arrimg = option.arrimg
            this.stockWarn = option.stockWarn;
            this.pageStatus = option.pageStatus
            this.goodsType = option.goodsType
            this.xnHxType = option.xnHxType
            this.yushou_type = Number(option.yushou_type || '') 
        },
        onShow() {
            this.title = this.language.setAttr.title
            if (uni.getStorageSync('upload_chooseAttr')) {
                this.title = this.language.setAttr.title2
            } else if (uni.getStorageSync('upload_attr_group')) {
                this.title = this.language.setAttr.title
            }
        },
        methods: {
            //成本价正则，不能输入负数
            checkCbj(row, index) {
                row.target.value = row.target.value.replace(/[^0-9.]/g, '')
                this.$nextTick(() => {
                    this.attr_arr[index].cbj = row.target.value
                })
            },
            //原价正则，不能输入负数
            checkYj(row, index) {
                row.target.value = row.target.value.replace(/[^0-9.]/g, '')
                this.$nextTick(() => {
                    this.attr_arr[index].yj = row.target.value
                })
            },
            //售价正则，不能输入负数
            checkSj(row, index) {
                row.target.value = row.target.value.replace(/[^0-9.]/g, '')
                this.$nextTick(() => {
                    this.attr_arr[index].sj = row.target.value
                })
            },
            //库存/核销次数正则，只能输入正整数
            checkKucun(row, index) {
                row.target.value = row.target.value.replace(/^0|[^\d]|[.]/g, '')
                this.$nextTick(() => {
                    this.attr_arr[index].kucun = row.target.value
                })
            },
            removeImg(index) {
                let item = this.attr_arr[index];
                item.img = '';
                this.attr_arr.splice(index, 1, item)
            },
            upImgClick(index) {
                uni.chooseImage({
                    count: 1,
                    // #ifndef MP-TOUTIAO
                    sizeType: ['original', 'compressed'], //可以指定是原图还是压缩图，默认二者都有
                    // #endif
                    sourceType: ['album', 'camera'], //从相册选择
                    success: async (res) => {

                        let res1 = await this.$req.upLoad(res.tempFilePaths[0])

                        let item = this.attr_arr[index];
                        item.img = res1.data;
                        this.attr_arr.splice(index, 1, item)

                    }
                })
            },
            /* 
                添加属性值
            */
            addValue(index) {
                this.addAttrValueVal = '';
                this.addValue_pname = index;
                this.addAttrValueFlag = true;
            },
            closeValue() {
                this.addAttrValueFlag = false;
            },
            inspect2() {
                var sid
                // this.addAttrValueVal = this.addAttrValueVal.replace(/(^\s*)|(\s*$)/g, '');
                if (/[\x21-\x2F\x3A-\x40\x5B-\x60\x7B-\x7E]+/g.test(this.addAttrValueVal)) {
                    uni.showToast({
                        title: this.language.setAttr.sxbnbhtsfh,
                        icon: 'none'
                    });
                    return;
                }
                if (!this.addAttrValueVal) {
                    uni.showToast({
                        title: this.language.setAttr.Tips[2],
                        icon: 'none'
                    });
                    return;
                }
                if (this.attrList.length > 0) {

                    var list = this.attrList.filter((item) => item.text === this.addValue_pname)
                    if (list.length == 0) {
                        this.addValueOk()
                        return
                    } else {
                        var sid = list[0].id
                    }
                    let data = {
                        api: 'saas.dic.getSkuInfo',
                        sid: sid,
                        pageSize: 100,
                    };
                    this.$req.post({
                        data
                    }).then(res => {
                        this.inspectList2 = res.data.list.filter((item) => item.name === this.addAttrValueVal)
                        this.addValueOk()
                    });
                } else {
                    this.addValueOk()
                }
            },
            addValueOk() {
                if (this.inspectList2.length > 0) {
                    uni.showToast({
                        title: this.language.setAttr.sxzycz,
                        icon: 'none'
                    });
                    return;
                } else {
                    let arr = this.attrDataList[this.addValue_pname];
                    var name = this.addAttrValueVal;
                    let flag = arr.some(item => item.value == name);
                    if (flag) {
                        uni.showToast({
                            title: this.language.setAttr.Tips[3],
                            icon: 'none'
                        });
                        return;
                    }

                    let it = {
                        id: name,
                        value: name,
                        status: false,
                        isAdd: true
                    }
                    this.attrDataList[this.addValue_pname].unshift(it);

                    this.addAttrValueFlag = false;
                    this._chooseAttrData(it, this.attrDataList[this.addValue_pname], this.addValue_pname)
                }
            },
            removeValue(indexs, index) {
                let items = this.attrDataList[indexs]
                let name = items[index].value
                items.splice(index, 1)
                this.$delete(this.attrDataList, indexs);
                this.attrDataList[indexs] = items;

                let groupIndex = -1;
                this.attr_group.filter((item, index) => {
                    if (item.attr_group_name == indexs) {
                        let i = item.attr_list.findIndex(it => {
                            return it.attr_name == name
                        })
                        item.attr_list.splice(i, 1)
                        if (item.attr_list.length == 0) {
                            groupIndex = index;
                        }
                    }
                });
                if (groupIndex >= 0) {
                    this.attr_group.splice(groupIndex, 1)
                }

                this._table();
            },
            /* 
                添加属性
            */
            addName() {
                this.addAttrVal = '';
                this.addAttrFlag = true;
            },
            /* 
                关闭属性弹窗
            */
            closeName() {
                this.addAttrFlag = false;
            },
            inspect() {
                // this.addAttrVal = this.addAttrVal.replace(/(^\s*)|(\s*$)/g, '');
                if (/[\x21-\x2F\x3A-\x40\x5B-\x60\x7B-\x7E]+/g.test(this.addAttrVal)) {
                    uni.showToast({
                        title: this.language.setAttr.sxbnbhtsfh,
                        icon: 'none'
                    });
                    return;
                }
                if (!this.addAttrVal) {
                    uni.showToast({
                        title: this.language.setAttr.Tips[0],
                        icon: 'none'
                    });
                    return;
                }
                let data = {
                    api: 'saas.dic.getSkuInfo',
                    key: this.addAttrVal,
                    pageSize: 100,
                };
                this.$req.post({
                    data
                }).then(res => {
                    this.inspectList = res.data.list.filter((item) => item.type === 1)
                    this.attrValOk()
                });
            },
            /* 
                确认属性名
            */
            attrValOk() {
                if (this.inspectList.length > 0) {
                    uni.showToast({
                        title: this.language.setAttr.sxmcycf,
                        icon: 'none'
                    });
                    return;
                } else {
                    let arr = Object.keys(this.attrDataList);
                    var name = this.addAttrVal;
                    let flag = arr.some(item => item == name);
                    if (flag) {
                        uni.showToast({
                            title: this.language.setAttr.Tips[1],
                            icon: 'none'
                        });
                        return;
                    }

                    let data = {

                        api: "mch.App.Mch.Get_attribute_value",
                        attribute_str: name
                    };
                    this.$req.post({
                        data
                    }).then(res => {
                        this.addAttrFlag = false;

                        let {
                            code,
                            data,
                            message
                        } = res
                        if (code == 200 && data.list[name].length > 0) {

                            if (this.chooseAttr) {
                                this.chooseAttr += ',' + this.addAttrVal
                            } else {
                                this.chooseAttr = this.addAttrVal
                            }

                            let attrName = data.list[name];
                            this.attrDataList[name] = data.list[name];

                            this.addAttrObj[name] = data.list[name];
                        } else {

                            if (this.addAttrStr) {
                                this.addAttrStr += ',' + this.addAttrVal
                            } else {
                                this.addAttrStr = this.addAttrVal
                            }

                            this.attrDataList[name] = [];
                            this.addAttrObj[name] = [];
                        }
                    });

                }
            },
            // 删除选择的属性名
            deleteAttr(index) {

                let arr1 = this.chooseAttr.split(',');
                let index1 = arr1.findIndex(item => item == index);
                if (index1 >= 0) {
                    arr1.splice(index1, 1);
                    this.chooseAttr = arr1.join();
                }

                let arr2 = this.addAttrStr.split(',');
                let index2 = arr2.findIndex(item => item == index);
                if (index2 >= 0) {
                    arr2.splice(index2, 1);
                    this.addAttrStr = arr2.join();
                }

                this.$delete(this.attrDataList, index);
                this.$delete(this.addAttrObj, index);

                let indx = this.attr_group.filter((item, indx) => {
                    if (item.attr_group_name == index) {
                        this.attr_group.splice(indx, 1);
                    }
                });

                this._table();
            },
            attrSearch() {
                this.attrList.filter(item => {
                    if (item.text.includes(this.searchtxt) || !this.searchtxt) {
                        item.show = true;
                    } else {
                        item.show = false;
                    }
                })
                this.iShow = this.attrList.every(item => {
                    return item.show == false
                })

            },
            openChecked() {
                if ((!this.checkedFlag) && !this.chooseAttr) {
                    uni.showToast({
                        title: this.language.setAttr.Tips[4],
                        icon: 'none'
                    });
                    return;
                }
                this.checkedFlag = !this.checkedFlag;
            },
            returnC() {
                this.pageFlag = false;
                this.title = this.language.setAttr.title;
                if (uni.getStorageSync('upload_attr_group')) {
                    this.attr_group = uni.getStorageSync('upload_attr_group');
                    this.attr_arr = uni.getStorageSync('upload_attr_arr');

                    this.chooseAttr = uni.getStorageSync('upload_chooseAttr');

                    this.addAttrStr = uni.getStorageSync('upload_addAttrStr');

                    this.addAttrObj = uni.getStorageSync('upload_addAttrObj');

                    this._axios(this.chooseAttr, 'show');
                }
            },

            // 自定义属性名
            addAttr() {
                this.addAttrName = this.addAttrName.replace(/(^\s*)|(\s*$)/g, '');
                if (!this.addAttrName) {
                    uni.showToast({
                        title: this.language.setAttr.Tips[0],
                        icon: 'none'
                    });
                    return;
                }

                var name = this.addAttrName;

                let flag = this.addList.find(item => item.attr_group_name == name);
                if (flag) {
                    uni.showToast({
                        title: this.language.setAttr.Tips[1],
                        icon: 'none'
                    });
                    return;
                }

                let data = {

                    api: "mch.App.Mch.Get_attribute_value",
                    attribute_str: name
                };
                this.$req.post({
                    data
                }).then(res => {
                    let {
                        code,
                        data,
                        message
                    } = res
                    if (code == 200 && data.list[name].length > 0) {
                        let attrName = data.list[name];
                        let attr_list = [];
                        attrName.filter(item => {
                            attr_list.push({
                                attr_name: item.value
                            });
                        });
                        this.addList.push({
                            attr_group_name: name,
                            childValue: '',
                            attr_list
                        });
                        this._abbTable();
                    } else {
                        this.addList.push({
                            attr_group_name: name,
                            childValue: '',
                            attr_list: []
                        });
                    }
                    this.addAttrName = '';
                });
            },
            // 删除属性名
            removeAttrName(index) {
                this.addList.splice(index, 1);
                this._abbTable();
            },
            // 自定义属性值
            addAttrValue(item) {
                item.childValue = item.childValue.replace(/(^\s*)|(\s*$)/g, '');
                if (!item.childValue) {
                    uni.showToast({
                        title: this.language.setAttr.Tips[2],
                        icon: 'none'
                    });
                    return;
                }
                let flag = item.attr_list.find(items => items.attr_name == item.childValue);
                if (flag) {
                    uni.showToast({
                        title: this.language.setAttr.Tips[3],
                        icon: 'none'
                    });
                    return;
                }
                item.attr_list.push({
                    attr_name: item.childValue
                });
                item.childValue = '';
                this._abbTable();
            },
            // 删除属性值
            removeAttrValue(item, index) {
                item.attr_list.splice(index, 1);
                this._abbTable();
            },
            // 点击选择属性
            _clickAttr() {
                this._axios(this.chooseAttr);
            },
            // 清空选择属性
            removeCheckAttr() {
                this.chooseAttr = '';
                this.attrList.filter(item => {
                    item.status = false;
                })
            },
            // 选择属性名称
            _chooseAttr(item, index) {

                item.status = !item.status;
                if (item.status) {
                    // 选中进入
                    let str = (this.chooseAttr ? ',' : '') + item.text;
                    this.chooseAttr += str;
                } else {
                    // 取消选中
                    let arr1 = this.chooseAttr.split(',');
                    let index1 = arr1.findIndex(items => items == item.text);
                    if (index1 >= 0) {
                        arr1.splice(index1, 1);
                        this.chooseAttr = arr1.join();
                    }
                    this.$delete(this.addAttrObj, item.text);
                }
            },
            // 保存属性名称
            _saveAttr() {

                if (!this.chooseAttr) {
                    uni.showToast({
                        title: this.language.setAttr.Tips[4],
                        icon: 'none'
                    });
                    return;
                }
                this.attr_arr = [];
                this.attr_group = [];
                this._axios(this.chooseAttr, 1);
            },
            // 选择子属性
            _chooseAttrData(item, items, index) {
                if (!item || item == undefined) {
                    return;
                }
                if (item && item.isAdd && item.status) {
                    return
                }
                item.status = !item.status;
                if (item.status) {
                    // 选中子属性
                    let flag = true;
                    for (let i = 0; i < this.attr_group.length; i++) {
                        if (this.attr_group[i].attr_group_name == index) {
                            flag = false;
                            this.attr_group[i].attr_list.push({
                                attr_name: item.value
                            });
                            break;
                        }
                    }
                    if (flag) {
                        this.attr_group.push({
                            attr_group_name: index,
                            attr_list: [{
                                attr_name: item.value
                            }]
                        });
                    }
                } else {
                    // 取消选中
                    for (let i = 0; i < this.attr_group.length; i++) {
                        if (this.attr_group[i].attr_group_name != index) {
                            continue;
                        }
                        let attr_list = this.attr_group[i].attr_list;
                        for (let j = 0; j < attr_list.length; j++) {
                            if (attr_list[j].attr_name != item.value) {
                                continue;
                            }
                            this.attr_group[i].attr_list.splice(j, 1);
                            if (this.attr_group[i].attr_list.length == 0) {
                                this.attr_group.splice(i, 1);
                            }
                            break;
                        }
                        break;
                    }
                }

                // 计算表格数据
                this._table();
            },
            _table() {
                // 表格总行数，计算方式为所有子属性的个数相乘
                let listX = 0;
                let attr_arr = [];
                for (let i = 0; i < this.attr_group.length; i++) {
                    let attr_list = this.attr_group[i].attr_list;
                    if (listX == 0) {
                        listX = attr_list.length;
                    } else {
                        listX = attr_list.length > 0 ? attr_list.length * listX : listX;
                    }
                }
                for (let i = 0; i < listX; i++) {
                    attr_arr.push({
                        cbj: this.costM,
                        yj: this.oldM,
                        sj: this.sellM,
                        kucun: this.stock,
                        img: this.arrimg,
                        attr_list: []
                    });
                }
                this.attr_arr.filter((item, index) => {
                    if (item.img && attr_arr[index]) {
                        attr_arr[index].img = item.img
                    }
                })
                this.attr_arr = attr_arr;

                var th_title = JSON.parse(JSON.stringify(this.attr_group));

                this._tableList(th_title, 0, listX, listX);
            },
            _abbTable() {
                // 表格总行数，计算方式为所有子属性的个数相乘
                let listX = 0;
                let attr_arr = [];
                for (let i = 0; i < this.addList.length; i++) {
                    let attr_list = this.addList[i].attr_list;
                    if (listX == 0) {
                        listX = attr_list.length;
                    } else {
                        listX = attr_list.length > 0 ? attr_list.length * listX : listX;
                    }
                }
                for (let i = 0; i < listX; i++) {
                    attr_arr.push({
                        cbj: this.costM,
                        yj: this.oldM,
                        sj: this.sellM,
                        kucun: this.stock,
                        attr_list: []
                    });
                }
                this.attr_arr = attr_arr;
                var th_title = JSON.parse(JSON.stringify(this.addList));
                this._tableList(th_title, 0, listX, listX);
            },
            _tableList(th_title, i, listX, _listX) {
                // 如果该循环的子项没有东西则停止递归
                if (!th_title[i]) {
                    if (i < th_title.length - 1) {
                        th_title.splice(i, 1);
                        this._tableList(th_title, i, listX, _listX);
                        return;
                    }
                    return;
                }

                // 如果该项属性的没有属性值，则删除该项重新递归
                if (th_title[i].attr_list.length == 0) {
                    th_title.splice(i, 1);
                    this._tableList(th_title, i, listX, _listX);
                    return;
                }

                // _listX代表当前属性有多少行,默认为总行数 listX总行数
                var strArr = JSON.parse(JSON.stringify(this.attr_arr));

                var xx = 0; // 代表当前行数
                var name = th_title[i].attr_group_name; //代表当前规格的标题,比如颜色/大小
                //listX为表格总行数
                if (i == 0) {
                    // 如果当前规格是第一个
                    // 当前规格有多少个,比如颜色,有蓝色白色两个
                    for (var j = 0; j < th_title[i].attr_list.length; j++) {
                        var value = th_title[i].attr_list[j].attr_name;
                        // 总行数除当前规格的个数,得出当前规格每个占多少行,比如白色白色白色,黑色黑色黑色
                        for (var x = 0; x < listX / th_title[i].attr_list.length; x++) {
                            strArr[xx].attr_list.push({
                                attr_id: '',
                                attr_name: value,
                                attr_group_name: name
                            });
                            xx++;
                        }
                    }
                } else if (i < th_title.length - 1) {
                    // 如果当前规格不是最后一个
                    // 当前规格的前一个每个属性有多少行,即当前规格和后面规格相乘的总行数
                    _listX = Math.round(_listX / th_title[i - 1].attr_list.length);
                    // 外面这层循环代表当前属性在内循环完成之后进入新的循环,比如白色白色黑色黑色红色红色,完成之后再次白色白色黑色黑色红色红色循环,总行数除以前一个属性每个属性有多少行,得出总循环数
                    for (var l = 0; l < listX / _listX; l++) {
                        for (var j = 0; j < th_title[i].attr_list.length; j++) {
                            var value = th_title[i].attr_list[j].attr_name;
                            // 当前规格的前一个每个属性行数,除当前
                            for (var x = 0; x < _listX / th_title[i].attr_list.length; x++) {
                                strArr[xx].attr_list.push({
                                    attr_id: '',
                                    attr_name: value,
                                    attr_group_name: name
                                });
                                xx++;
                            }
                        }
                    }
                } else {
                    // 如果当前规格属性是最后一个,格式是x,l,xl x,l,xl循环
                    for (var x = 0; x < listX / th_title[i].attr_list.length; x++) {
                        for (var j = 0; j < th_title[i].attr_list.length; j++) {
                            var value = th_title[i].attr_list[j].attr_name;
                            strArr[xx].attr_list.push({
                                attr_id: '',
                                attr_name: value,
                                attr_group_name: name
                            });
                            xx++;
                        }
                    }
                }

                this.attr_arr = strArr;
                i++;
                if (i < th_title.length) {
                    this._tableList(th_title, i, listX, _listX);
                }
            },
            // 请求方法
            _axios(attribute_str = '', flag) {
                let api = 'mch.App.Mch.Get_attribute_name'
                if (flag) {
                    api = "mch.App.Mch.Get_attribute_value"
                }
                let data = {

                    api,
                    attribute_str: attribute_str,
                    attr_arr: this.attr_group.length > 0 ? JSON.stringify(this.attr_group) : ''
                };

                this.$req.post({
                    data
                }).then(res => {

                    let {
                        code,
                        data,
                        message
                    } = res;
                    if (code == 200) {
                        if (!flag) {
                            this.pageFlag = true;
                            this.title = this.language.setAttr.Attr[2];
                            data.attribute.filter(item => {
                                item.show = true;
                            })
                            this.attrList = data.attribute;
                        } else {
                            this.pageFlag = false;
                            this.title = this.language.setAttr.title;

                            for (let i in this.addAttrObj) {
                                data.list[i] = this.addAttrObj[i];
                            }

                            this.attrDataList = data.list;


                            if (flag == 'show' && uni.getStorageSync('upload_attrDataList')) {

                                this.attrDataList = uni.getStorageSync('upload_attrDataList');
                                return
                            }

                            if (this.pageStatus != 1) {
                                this.attr_group = []
                                for (let index in this.attrDataList) {
                                    let items = this.attrDataList[index];
                                    items.filter((item) => {

                                        if (item.status) {
                                            let flag = true;
                                            for (let i = 0; i < this.attr_group.length; i++) {
                                                if (this.attr_group[i].attr_group_name == index) {
                                                    flag = false;
                                                    this.attr_group[i].attr_list.push({
                                                        attr_name: item.value
                                                    });
                                                    break;
                                                }
                                            }
                                            if (flag) {
                                                this.attr_group.push({
                                                    attr_group_name: index,
                                                    attr_list: [{
                                                        attr_name: item.value
                                                    }]
                                                });
                                            }
                                        }

                                    })
                                }


                                this._table();
                            }
                        }
                    }
                }).catch(err => {

                });
            },
            // 完成设置
            _complete() {
                // let rule = this.attr_arr.some(obj => Object.values(obj).some(value => value == ''))
                let rule = this.attr_arr.every(item => item.cbj && item.img && item.kucun && item.sj && item.yj);
                if (!rule && this.type != 'yushou') {
                    uni.showToast({
                        title: this.language.setAttr.qwssx,
                        icon: 'none'
                    })
                    return
                }
                if (this.attr_arr.length == 0) {
                    let str = this.attrStyle == 1 ? this.language.setAttr.Tips[5] : this.language.setAttr.Tips[6];
                    uni.showToast({
                        title: str,
                        icon: 'none'
                    });
                    return;
                }
                // 虚拟商品且不需要核销时走这个 库存 默认 9999999  this.goodsType == 1 && this.xnHxType == 2 
                // 预售商品 定价模式 库存 默认9999999 this.goodsType == 0 && this.yushou_type ==1 
                if(this.goodsType == 1 && this.xnHxType == 2 || (this.goodsType == 0 && this.yushou_type === 1 ) ){
                    this.attr_arr.forEach(v=>{
                        v.kucun = 9999999 
                    })
                }
                this.attr_arr.forEach(v=>{
                    v.unit = v.attr_list[0].attr_group_name
                })
                uni.setStorageSync('upload_attr_group', this.attr_group);
                uni.setStorageSync('upload_attr_arr', this.attr_arr);
                uni.setStorageSync('upload_chooseAttr', this.chooseAttr);

                uni.setStorageSync('upload_addAttrStr', this.addAttrStr);
                uni.setStorageSync('upload_addAttrObj', this.addAttrObj);

                uni.setStorageSync('upload_attrDataList', this.attrDataList);


                uni.navigateBack({
                    delta: 1
                });
            }
        }
    };
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped>
    @import url("@/laike.less");
    @import url('../../static/css/myStore/setAttr.less');
    .input-color{
        background-color: #f3f5f7;
    }
</style>
