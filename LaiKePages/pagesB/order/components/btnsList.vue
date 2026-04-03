<template>
    <view class="but-body">
        
        <!-- 实物订单 - 列表按钮 显示 --> 
            <view class="more">
                <!-- 更多 -->
                <text  @click='seeAll' v-if="falg">{{language.memberCenter.more}}</text> 
             
                <view class="more-pop" v-if="butAllFalg"  >  
                            <btnsItem
                              :get_button_list='seeAllList'
                              :orde_id="orde_id"
                              :sNo="sNo"
                              :orderList="orderList"
                              :invoicePrice='invoicePrice'  
                              :item='item'
                              :otype='otype'
                              :applyInvoice='applyInvoice'
                              @pay_display='pay_display'
                          />
                </view>
            </view> 
             
           <view class="last_right">  
             <btnsItem
                   :get_button_list='get_button_list'
                   :orde_id="orde_id"
                   :sNo="sNo"
                   :orderList="orderList"
                   :invoicePrice='invoicePrice'  
                   :item='item'
                   :otype='otype'
                   @applyInvoice='applyInvoice'
                   @pay_display='pay_display'
               />
            </view>
    </view>
</template>

<script> 
    import btnsItem from "@/pagesB/order/components/btnsItem";
    import {mapMutations, mapState} from "vuex";

    export default {
        name: "btns",
        props: {
            // 订单对象
            item:{
                type:Object,
                default:()=>{}
            }, 
            // 订单类型 
           orderType:{
               type: String,
               default: ''
           },
           // 按钮显示对象
           buttonList:{
               type:Object,
               default:()=>{}
           },
           // 申请开票
           applyInvoice:{
               type:Function,
               default:()=>{}
            },
           orde_id: {required: true}, // 订单id
            // 订单商品
           orderList: {
              type:Array,
              default:[],
            },
           sNo:{
               type: [String , Number],
               default: ''
           },
           // 申请开票时 需要的金额字段
           invoicePrice:{
               type: [String , Number],
               default: ''
           },
           // 订单类型
           otype:{
               type: String,
               default: ''
           },
        },
        data () {
            return {
                fastTap: true, // 防止重复点击
                seeAllList: {},
                 get_button_list : {},
                 falg:false,
                 butAllFalg:false,
            }
        },
       components: { 
           btnsItem
        },
        watch: {
            seeAllList:{
                handler(newValue, oldValue) {
                    if(Object.keys(newValue).length == 0){
                        this.falg = false
                        this.butAllFalg =false
                    }else{
                        this.falg = true
                        this.butAllFalg =true
                    }
                },
                deep: true,
                immediate:true
            }
        },
      created() {
          this.setButtonList()
      },
      onShow(){
          this.butAllFalg = false
      },
      watch: {
          buttonList: {
              handler(newValue, oldValue) { 
                 this.setButtonList()
              }, 
              deep: true 
          }
      },
        methods: {
            setButtonList(){
                const obj = {};
                this.seeAllList = {}; // 清空 seeAllList
                this.falg = false
                // 过滤状态为0的字段
                const arr=  Object.entries(this.buttonList).filter(([key, value], index) => {
                  return value == 1
                }).sort()
                
                arr.forEach((item,index)=>{
                    if(index <3){ 
                        this.$set(obj , item[0], item[1]);
                    }else{
                        // 大于三个显示更多
                        this.falg = true 
                        this.$set(this.seeAllList , item[0], item[1]);
                    }
                }) 
                this.get_button_list = obj; // 更新 get_button_list  
            },
            seeAll(){
                this.butAllFalg = !this.butAllFalg 
            },
            pay_display(e) {
                this.$emit('pay_display', e);
            }
        }
    }
</script>

<style scoped lang="less">
    @import url("@/laike.less");
    .but-body {
      width: 100%;
      display: flex;
      justify-content: space-between;
      .more{
          height: 72rpx;
          line-height: 72rpx;
          display: flex;
          align-items: center;
          justify-content: space-between;
          position: relative;
          text-align: center;
          margin-right: 24rpx;
          .more-pop {
              position: absolute; 
              bottom: 70rpx;
              background-color: #fff;
              text-align: center;
              border:2rpx solid #f3f3f3;
               /deep/view{
                    border:none !important;
                }
          }
          .last_right {
              display: block !important;
              
          }
          .last_right *{
              color: #000 !important;
          }
      }
  }
 
  .aas{
         view {
            border: nono !important;
        }
    }
</style>
