<template>
    <div>
        <toload :load="load"> 
        <!-- 禅道 2396 暂时先修改 returnR 为1  原来是 13 -->
            <heads :title="language.order.batchOrder.title" :returnR="1" :order_id="orde_id" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
            <ul class="order_goods" :style="{ top: halfWidth }">
                <li class="order_two" v-for="(item, index) in order" :key="index" >
                    <img class="quan_img" :src="display_img[index] ? quan_hei : quan_hui" @tap="_checkedOne(item, index)" />
                    
                    <img class="order_two_img" :src="item.imgurl?item.imgurl:ErrorImg" @error="handleErrorImg(index)" />
                    <div  class="oreder_info">
                        <div style="margin-right: 40rpx;width: 100%;" @tap="navTo( '/pagesC/goods/goodsDetailed?pro_id=' + item.p_id)">
                            <p class="order_space">{{ item.p_name }}</p>
                        </div>
                        <div style="display: flex;align-items: center;justify-content: space-between;">
                            <p class="order_price"><span class="price_symbol">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>{{ LaiKeTuiCommon.formatPrice(Number(item.p_price)) }}</p>
                            <p class="color_two">x{{ item.num }}</p>
                        </div>
                    </div>
                    
                </li>
            </ul>
            <div class="batch_bottom">
               <div class="batch_bottom_btn">
                   <div class="bottom_left">
                       <img class="quan_img" style="margin-right: 8rpx;" :src="selectAll ? quan_hei : quan_hui" @tap="_selectAll" />
                       <span>{{language.order.batchOrder.Select_all}}</span>
                   </div>
                   <div class="batch_bottom_q" @tap="_button">{{language.order.batchOrder.determine}}</div>
               </div>
            </div>
        </toload>
    </div>
</template>

<script>
export default {
    data() {
        return {
            title: '选择商品列表',
            orde_id: '',
            order: '',
            load: false,
            ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
            quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png',
            quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
            display_img: [], //圆圈的选中状态
            selectArray: [], //存储选中商品
            selectAll: false, //全选状态
            fastTap: true,
            rType: false,
            bgColor: [
                {
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            orderDetailsId:''
        };
    },
    onLoad(option) {
        this.orde_id = option.orde_id;
        this.rType = option.rType;
        this.orderDetailsId = option.order_details_id
        this._axios();
    },
    methods: {
        handleErrorImg(index){
            this.order[index].image = this.ErrorImg
        },
        //单选
        _checkedOne(item, indexli) {
            var cum = this.selectArray.findIndex(items=>items.id == item.id);
         
            // 如果是有的话 点击就是取消
            //判断点击的传入的值是否存在数组中，如果没有添加，如果有删除，同时设定选中状态（第一次点击添加进数组，第二次点击从数组中删除）
            if (cum < 0) {
                this.selectArray.push(item);
                this.$set(this.display_img, indexli, true);
            } else {
                this.selectArray.splice(cum, 1);
                this.$set(this.display_img, indexli, false);
            }
            //根据产品选状态，设定全选状态
            if (this.selectArray.length == this.order.length) {
                this.selectAll = true;
            } else {
                this.selectAll = false;
            }
        },
        //全选
        _selectAll() {
            //根据全选状态，设定商品选中状态
            this.selectAll = !this.selectAll;
            var img = this.$refs.img;
            //根据全选状态，设定商品选中状态
            if (this.selectAll) {
                for (var i = 0; i < this.order.length; i++) {
                    this.$set(this.selectArray, i, this.order[i]);
                    this.$set(this.display_img, i, true);
                }
            } else {
                this.selectArray = [];
                for (var i = 0; i < this.order.length; i++) {
                    this.$set(this.display_img, i, false);
                }
            }
        },
        _button() {
            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            let order_details_id = [];
            for (let i = 0; i < this.selectArray.length; i++) {
                let id = this.selectArray[i].id;
                order_details_id.push(id);
            }
            if (this.selectArray.length) {
                uni.navigateTo({
                    url: '/pagesA/returnGoods/returnGoods?order_details_id=' + order_details_id + '&order_anking=2&rType=' + this.rType + '&isbatch=true' + '&r_status=' + this.order[0].r_status,
                    success: () => {
                        this.fastTap = true;
                    }
                });
            } else {
                this.fastTap = true;
                uni.showToast({
                    title: this.language.order.batchOrder.Tips,
                    duration: 1000,
                    icon: 'none'
                });
            }
        },
        _axios() {
            var data = {
                order_id: this.orde_id,
                api: 'app.order.return_method',
                order_details_id:this.orderDetailsId,
            };

            this.$req.post({ data }).then(res => {
                if (res.code == 200) {
                    let {
                        data: { list }
                    } = res;
                    var order = [];
                    for (let i = 0; i < list.length; i++) {
                        // 之前的判断( list[i].r_status == 2 || list[i].r_status == 1) && list[i].s_type != 1 && list[i].refund
                        order.push(list[i]);
                    }
                    this.order = order;
                    this.load = true
                    
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                    this.load = true
                }
            });
        }
    }
};
</script>



<style>
    page{
        background: #F4F5F6
    }
</style>
<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/order/batchOrder.less');
</style>
