<template>
    <div class="discover" style="background-color: #fff;">
        <heads :title="language.discover.title"></heads>

        <template v-if="loadFlag">
            <div class="discover_one" v-if="product">
                <img :src="product.imgurl" />
                <div>
                    <p>{{ product.product_title }}</p>
                    <div>
                        {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}<span>{{ Number(product.price).toFixed(2) }}</span>
					</div>
                </div>
            </div>
            <div class="hr"></div>
            <div class="discover_tj">
                <div class="discover_left"><i></i></div>
                <p>{{language.discover.recommend}}</p>
                <div class="discover_right"><i></i></div>
            </div>
            <ul class="allgoods" v-if="collection">
                <li class="allgoods_li" v-for="(items, index) in collection" @tap="_goods(items.id)" :key="index">
                    <view class="relative" style="float: left;display: flex;">
                        <image class="li_image" lazy-load="true" :src="items.imgurl" />
                    </view>
                    <view class="li_right">
                        <p>{{ items.product_title }}</p>
                        <view class="right_dpn">{{language.discover.shop}}：{{ items.mch_name }}</view>
                        <div class="li_right_bottom">
                            <div class="allgoods_li_priceBox" v-if="!is_grade">
                                <div>
                                    <span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
                                    {{ Number(items.vip_yprice).toFixed(2) }}
                                </div>
                            </div>
                            <div class="allgoods_li_priceBox" v-else>
                                <div>
                                    <span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
                                    {{Number(items.vip_price).toFixed(2)}}
                                </div>
                                <p style="color: #999999;text-decoration: line-through;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ Number(items.vip_yprice).toFixed(2) }}</p>
                            </div>
                            <img :src="shopImg" class="allgoods_shopImg" @tap.stop="shopping_j(items.id, items)"/>
                        </div>
                    </view>
                </li>
            </ul>
            <div v-else-if="hasFlag == -1" class="relative">
                <div class="noFindDiv">
                    <div><img class="noFindImg" alt="" :src="noFind" /></div>
                    <span class="noFindText">{{language.discover.nofind}}</span>
                </div>
            </div>
        </template>
        
        <skus ref="attrModal" :stock="language.discover.stock2" :nums="language.discover.num" :confirm="language.discover.confirm" @confirm="_confirm"></skus>
    </div>
</template>

<script>
import skus from '../../components/skus.vue';
    
export default {
    data() {
        return {
            is_grade: false,
            shopImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/allgoods_shopImg.png',
            noImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/loading.gif',
            noFind: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noFind.png',
            title: '发现',
            
            collection: '', //相似商品
            product: '', //本商品
            hasFlag: 0,
            loadFlag: false,
            
            fastTap: true,
            pro_id: '',
            proid: '',
            haveSkuBean: '',
            num: '',
            numb: ''
        };
    },
    components: { skus },
    onLoad(option) {
        
        this.pro_id = this.$store.state.pro_id;
        if (option.pro_id) {
            this.pro_id = option.pro_id;
        }
        this._axios();
    },
    methods: {
        _confirm(sku){
            Object.assign(this.$data,sku)
            
            if (!this.fastTap) {
                return
            }
            this.fastTap = false
            if (Boolean(this.haveSkuBean)) {
                if (this.num == 0) {
                    uni.showToast({
                        title: this.language.discover.stock,
                        duration: 1000,
                        icon: 'none'
                    })
                    this.fastTap = true
                } else if (this.num != 0) {
                    this._shopping()
                }
            } else {
                if (this.num == 0) {
                    uni.showToast({
                        title: this.language.discover.stock,
                        duration: 1000,
                        icon: 'none'
                    })
                    this.fastTap = true
                } else {
                    uni.showToast({
                        title: this.language.discover.shop_verification,
                        duration: 1000,
                        icon: 'none'
                    })
                    this.fastTap = true
                }
            }
        },
        // 点击确定购买之后，如果库存不为零。则运行
        _shopping (id) {
            if (this.haveSkuBean) {
                var data = {
                    api: 'app.product.add_cart',
                    pro_id: this.proid,
                    attribute_id: this.haveSkuBean.cid,
                    num: this.numb,
                    type: 'addcart'
                }
        
                this.$req
                    .post({ data })
                    .then(res => {
                        let { code, data, message } = res
                        if (code == 200) {
                            uni.showToast({
                                title: this.language.discover.add_success,
                                icon: 'none'
                            })
        
                            this.$store.state.access_id = data.access_id
                            
                            this.haveSkuBean = ''
                            this.$refs.attrModal._mask_f()
                            this.fastTap = true
                        } else {
                            uni.showToast({
                                title: message,
                                icon: 'none'
                            })
                            this.fastTap = true
                        }
                    })
                    .catch(error => {
                        this.fastTap = true
                    })
            } else {
                
                this.fastTap = true
            }
        },
        // 为你推荐商品右下角的小购物车图标
        shopping_j (id) {
            if (!this.fastTap) {
                return
            }
            this.fastTap = false
            this.proid = id
            var data = {
                api: 'app.product.index',
                pro_id: id
            }
        
            this.$req
                .post({ data })
                .then(res => {
                    this.fastTap = true
                    let {
                        data: { collection_id, pro, comments, attrList, skuBeanList, attribute_list, qj_price, type }
                    } = res
                    
                    this.$refs.attrModal.imgurl = pro.img_arr[0]
                    this.$refs.attrModal.num = pro.num
                    this.$refs.attrModal.price = qj_price
                    this.$refs.attrModal.skuBeanList = attribute_list
                    this.$refs.attrModal.initData()
                    this.$refs.attrModal._mask_display()
                })
                .catch(error => {
                    this.fastTap = true
                })
        },
        _axios() {
            var data = {
                api: 'app.addFavorites.similar',
                
                pro_id: this.pro_id
            };
            
            this.$req.post({data}).then(res=>{
                this.loadFlag = true;
                this.is_grade = res.data.isGrade
                if (res.code == 200) {
                    this.collection = res.data.list;
                    this.product = res.data.product[0];
                    if (this.collection.length == 0) {
                        this.hasFlag = -1;
                    }
                } else {
                    this.hasFlag = -1;
                    this.collection.length = '';
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            })
        },
        _goods(id) {
            this.$store.commit('SET_PRO_ID', id);
            uni.navigateTo({
                url: '/pagesC/goods/goodsDetailed'
            });
        },
    }
};
</script>

<style lang="less" scoped> 
    @import url("@/laike.less");
    @import url('../../static/css/collection/discover.less');
</style>
