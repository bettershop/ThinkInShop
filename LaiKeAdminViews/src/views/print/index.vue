<template>
  <div class="container">
    <div class="list">
        <div class="orderBox" v-for="(item, index) of orderList" :key="index">
            <el-card :bordered="false">
                <div slot="header" class="clearfix">
                    <span>{{$t('print.xxdd')}}</span>
                </div>
                <div class="grids">
                    <div>
                        <span>{{$t('print.ddbh')}}:</span>
                        <span>{{ item.list.data.sNo }}</span>
                    </div>
                    <div>
                        <span>{{$t('print.zffs')}}:</span>
                        <span>{{ item.list.data.paytype }}</span>
                    </div>
                    <div>
                        <span>{{$t('print.ddzt')}}:</span>
                        <span>{{ item.list.detail[0].statusName }}</span>
                    </div>
                    <div>
                        <span>{{$t('print.kdgs')}}:</span>
                        <span> {{ getKuaidi(item.list.data) }} </span>
                    </div>

                    <div>
                        <span>{{$t('print.ddly')}}:</span>
                        <span>{{ $myGetSource(item.list.data.source) }}</span>
                    </div>
                    <div>
                        <span>{{$t('print.ddlx')}}:</span>
                        <span>{{ isOrderType(item.list.data) }}</span>
                        <!-- <span>{{ item.list.data.orderTypeName }}</span> -->

                        <!-- <span>{{ item.list.data.grText }}</span> -->
                    </div>

                    <div>
                        <span>{{$t('print.psfy')}}:</span>
                        <span v-if="Number(item.list.detail[0].freight) > 0"
                        >
                        <!-- {{ item.list.detail[0].freight }} -->
                        {{getAllFreight(item.list.detail)}}
                        </span
>
                        <span v-else>{{$t('print.my')}}</span>
                    </div>
                    <div>
                        <span>{{$t('print.kddh')}}:</span>
                        <span>{{getOrderCode(item.list.data)}}</span>
                        <!-- <template v-if="getOrderCode(item.list.data) === '暂无'">
                          <span>{{ getOrderCode(item.list.data) }}</span>
                        </template>

                        <template>
                          <span v-for="(item2,i) in getOrderCode(item.data)" :key="i"
                            >{{ item2.num }} {{ item2.kuaidi_name }}</span
                          >
                        </template> -->
                    </div>

                    <div>
                        <span>{{$t('print.shr')}}:</span>
                        <span>{{ item.list.data.name }}</span>
                    </div>

                    <div>
                        <span>{{$t('print.lxdh')}}:</span>
                        <span>{{ item.list.data.mobile }}</span>
                    </div>

                    <div>
                        <span>{{$t('print.sfsj')}}:</span>
                        <span v-if="item.list.data.deliver_time"
                        >{{ item.list.data.deliver_time | dateFormat }}</span
                        >
                        <span v-else>{{$t('print.zw')}}</span>
                    </div>

                    <div>
                        <span>{{$t('print.shdz')}}:</span>
                        <span>{{ item.list.data.address }}</span>
                    </div>

                    <div>
                        <span>{{$t('print.xdsj')}}:</span>
                        <span v-if="item.list.data.add_time">{{ item.list.data.add_time | dateFormat }}</span>
                        <span v-else>{{$t('print.zw')}}</span>
                    </div>

                    <div>
                        <span>{{$t('print.fksj')}}:</span>
                        <span v-if="item.list.data.pay_time">{{ item.list.data.pay_time | dateFormat }}</span>
                        <span v-else>{{$t('print.zw')}}</span>
                    </div>

                    <div>
                        <span>{{$t('print.shsj')}}:</span>
                        <span v-if="item.list.data.arrive_time"
                        >{{ item.list.data.arrive_time | dateFormat }}</span
                        >
                        <span v-else>{{$t('print.zw')}}</span>
                    </div>
                    <div>
                        <span>{{$t('print.ddsm')}}:</span>
                        <span>{{ item.list.data.remarks }}</span>
                    </div>
                </div>

                <div class="dictionary-list" >
                    <el-table :data="item.list.detail" border ref="table" class="el-table" style="width: 100%">
                        <el-table-column prop="p_id" :label="$t('print.spid')" min-width="12.5%">
                        </el-table-column>
                        <el-table-column prop="p_name" :label="$t('print.spmc')" min-width="12.5%">
                        </el-table-column>
                        <el-table-column prop="mchName" :label="$t('print.dpmc')" min-width="12.5%">
                        </el-table-column>
                        <el-table-column prop="size" :label="$t('print.spgg')" min-width="12.5%">
                        </el-table-column>
                        <el-table-column prop="p_price" :label="$t('print.dj')" min-width="12.5%">
                            <template slot-scope="scope">
                                <span v-if="item.list.data.otype=='IN'">￥{{ scope.row.p_price }}+{{ scope.row.allow }}{{ $t('print.jf') }}</span>
                                <span v-else>￥{{ scope.row.p_price }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="num" :label="$t('print.sl')" min-width="12.5%">
                        </el-table-column>
                        <el-table-column prop="freight" :label="$t('print.yf')" min-width="12.5%">
                        </el-table-column>
                        <el-table-column prop="p_price" :label="$t('print.xj')" min-width="12.5%">
                            <template slot-scope="scope">
                                <span>
                                    <span  v-if="item.list.data.otype=='IN'">
                                        ￥{{ Number(scope.row.p_price).toFixed(2) *  Number(scope.row.num) }}+{{ Number(scope.row.allow).toFixed(2) * Number(scope.row.num) }}{{ $t('print.jf') }}
                                    </span>
                                    <span  v-else>
                                        ￥{{ Number(scope.row.p_price).toFixed(2) *  Number(scope.row.num) + Number(scope.row.freight)}}
                                    </span>
                                </span>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
 
                <div class="note">
                    <div class="top">
                        <div>
                            <p>
                                <span>{{$t('print.spzj')}}：</span>
                                <span v-if="item.list.data.otype=='IN'">￥{{getGoodsPrice(item.list.detail)}}+{{item.list.detail[0].allow}}{{ $t('print.jf') }}</span>
                                <span v-else>￥{{getGoodsPrice(item.list.detail)}}</span>

                            </p>
                            <p>
                                <span>{{$t('print.yf')}}：</span>
                                <span>￥{{ getAllFreight(item.list.detail) }}</span>
                            </p>
                            <p
                                    v-if="(item.list.data.otype == 'GM' || item.list.data.otype == '') && Number(item.list.coupon_price)>0"
                            >
                                <span>{{$t('print.dpyh')}}：</span>
                                <span>- ￥{{item.list.coupon_price}}</span>
                            </p>
                            <p
                                    v-if="(item.list.data.otype == 'GM' || item.list.data.otype == '') && item.list.discount_type"
                            >
                                <span>{{item.list.discount_type}}{{$t('print.yh')}}：</span>
                                <span>- ￥{{item.list.preferential_amount}}</span>
                            </p>
                            <p v-if="item.list.data.otype != 'GM' && item.list.data.otype != ''">
                                <span>{{$t('print.yhje')}}：</span>
                                <span>- ￥{{item.list.coupon_price}}</span>
                            </p>
                        </div>
                    </div>

                    <div class="bottom">
                        <p class="bold">
                            <span>{{$t('print.hjzf')}}：</span>
                            <span class="red" v-if="item.list.data.otype=='IN'">￥{{item.list.detail[0].z_price}}+{{item.list.detail[0].allow}}{{ $t('print.jf') }}</span>
                            <span class="red" v-else>￥{{item.list.detail[0].z_price}}</span>
                        </p>
                    </div>
                </div>

                <div class="footer">
                    <div class="print-info">
                        <span>{{$t('print.dysj')}}：{{ printTime | dateFormat }}</span>
                        <span>{{$t('print.czz')}}：{{ item.list.operator }}</span>
                        <span></span>
                    </div>

                    <div class="copyright">
                      <span>
                        <!-- 来客推（https://www.laiketui.com） -->
                        {{ item.list.printName }}({{ item.list.printUrl }})
                      </span>

                      <span>
                        <!-- {{$t('print.dz')}}：长沙市岳麓区绿地中央广场5栋3408 -->
                        {{$t('print.dz')}}：{{ item.list.printSheng }}{{ item.list.printShi }}{{ item.list.printxian }}{{ item.list.printAddress}}
                      </span>
                      <span>
                        <!-- {{$t('print.dh')}}：0731-86453408 -->
                        {{$t('print.dh')}}：{{ item.list.printPhone }}
                      </span>
                    </div>
                </div>
            </el-card>
        </div>
    </div>
  </div>
</template>

<script>
Date.prototype.Format = function(fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        S: this.getMilliseconds(), //毫秒
    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(
            RegExp.$1,
            (this.getFullYear() + "").substr(4 - RegExp.$1.length)
        );
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(
                RegExp.$1,
                RegExp.$1.length == 1
                    ? o[k]
                    : ("00" + o[k]).substr(("" + o[k]).length)
            );
    return fmt;
};
import { orderPrint } from '@/api/order/orderList'
export default {
    name: 'print',

    data() {
        return {
            orderList: [],
            loading: false,

            all_freight: 0,
            goodsPrice: 0,
            userpar: 0,
            grade_rate_amount: 0,
            coupon_price: 0,
            discount_type: 0,
            preferential_amount: 0,
            grade_rate: 0,
            printTime: new Date().Format("yyyy-MM-dd HH:mm:ss"),
        }
    },

    created() {
        this.orderPrints().then(() => {
            this.myprint()
            window.print()
            setTimeout(function() {
                window.close();
            },300)
        })
    },

    methods: {
        
        async orderPrints() {
            const res = await orderPrint({
                api: 'admin.order.orderPrint',
                sNo: this.$route.query.orderId
            })
            this.orderList = res.data.data
            console.log(this.orderList);
        },

        getQueryVariable(variable) {
            var query = window.location.search.substring(1);
            var vars = query.split("&");
            for (var i = 0; i < vars.length; i++) {
                var pair = vars[i].split("=");
                if (pair[0] == variable) {
                    return pair[1];
                }
            }
            return false;
        },

        /**
         * @name 总价计算
         */
        imputedPrice(record) {
            let { num, p_price, freight } = record;
            // return round(parseFloat(p_price) * Number(num),2)
            return (
                parseFloat(p_price) * Number(num) +
                parseFloat(freight)
            ).toFixed(2);
        },

        /**
         * @name 判断订单类型
         */
        isOrderType(data) {
            if (data.otype == "PT") {
                return "拼团订单";
            } else if (data.otype == "JP") {
                return "竞拍订单";
            } else if (data.otype == "IN") {
                return "积分订单";
            } else if (data.otype == "PS") {
                return "预售订单";
            } else if (data.otype == "FX") {
                return "分销订单";
            } else if (data.otype == "MS") {
                return "秒杀订单";
            } else if (data.otype == "ZB") {
                return "直播订单";
            } else if (data.otype == "FS") {
                return "限时折扣订单";
            } else if (data.otype == "VI") {
                return "虚拟订单";
            } else {
                if (data.drawid > 0) {
                    return "抽奖订单";
                } else {
                    return "普通订单";
                }
            }
        },

        /**
         * @name 获取订单编号
         */
        getOrderCode(data) {
            if (data.fh == 0) {
                return this.$t('zdata.zw');
            } else {
                return data.courierList[0];
            }
        },

        getPrintTime() {},

        /**
         * 取得快递单号
         */
        getKuaidi(data) {
            if (data.fh == 0) {
                return this.$t('zdata.zw');
            } else {
                return data.courier_num?data.courier_num[0].kuaidi_name:'';
            }
        },

        getGoodsPrice(detail) {
            let goodsPrice = 0;
            detail.map((item) => {
                goodsPrice += parseFloat(item.p_price) * parseInt(item.num);
                // this.all_freight += parseFloat(item.freight)
            });
            return goodsPrice.toFixed(2);
        },

        getAllFreight(detail) {
            let all_freight = 0;
            detail.map((item) => {
                all_freight += parseFloat(item.freight);
            });
            return all_freight;
        },
        myprint(){
            const tableNode = document.querySelectorAll('.el-table__header,.el-table__body');
            //el-table 打印不全的问题
            for (let k6 = 0; k6 < tableNode.length; k6++) {
                const tableItem = tableNode[k6];
                tableItem.style.width = '100%';
                const child = tableItem.childNodes;
                for (let i = 0; i < child.length; i++) {
                const element = child[i];
                if (element.localName === 'colgroup') {
                    element.innerHTML = '';
                }
                }
            }
            //el-table 格子里面打印超过格子的问题
            let cells = document.querySelectorAll('.cell');
            for (let k7 = 0; k7 < cells.length; k7++) {
                const cell = cells[k7];
                cell.style.width = '100%';
                cell.removeAttribute('style')
            }
        },
    }
}
</script>

<style scoped lang="less">

.container {
//   zoom: 0.64;
  width: 100%;
}
.container .list .orderBox {
  margin-bottom: 50px;
}
.container .list .el-card__body {
  padding: 0;
}
.container .list .el-card__header {
  padding: 0 0px;
}

.container .list .el-card__header .clearfix {
    display: flex;
    justify-content: center;
    align-items: center;
}

.container .list .el-card__header .clearfix span {
  font-weight: 900;
  text-align: center;
}
.container .list .grids {
  padding: 30px 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-gap: 10px;
}
.container .list .grids div span:nth-child(even) {
  color: #323544;
  font-size: 14px;
}
.container .list .grids div span:nth-child(odd) {
  color: #828b97;
  font-size: 14px;
}
.container .list .merchandise {
  padding: 0 0px;
  border: 1px solid #E9ECEF;
  border-top: none;
  position: relative;
  border-bottom: none;
}
.container .list .merchandise::before {
  content: "";
  height: 0px;
  width: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 999;
  border-top: 1px solid #E9ECEF;
}

.container .list .merchandise .nameimg {
  width: 60px;
  height: 60px;
}
.container .list .merchandise .sumups {
  position: absolute;
  top: 54px;
  background: #ffffff;
  width: 7.2%;
  display: flex;
  align-items: center;
}
.container .list .merchandise .small {
  font-size: 12px !important;
  color: #999 !important;
}
.container .list .merchandise .z_price {
  color: #323544;
  font-size: 14px;
  font-weight: bold;
}
.container .list .note {
  margin-top: 20px;
  padding: 0 0px;
}
.container .list .note p {
  display: inline-flex;
  justify-content: space-between;
  font-size: 16px;
  color: #323544;
  font-weight: bold;
  margin-left: 30px;
}
.container .list .note p.bold {
  font-weight: bold;
}
.container .list .note p.bold .red {
  color: black;
}
.container .list .note .bottom,
.container .list .note .top {
  display: inline-flex;
  flex-direction: row-reverse;
  width: 100%;
}
.container .list .footer {
  padding: 0 0px;
  display: flex;
  flex-direction: column;
  width: 100%;
  color: #333333;
  font-weight: 400;
  font-size: 14px;
}
.container .list .footer .copyright {
  display: flex;
  justify-content: space-between;
}
.container .list .footer .copyright span:first-child {
  width: 260px;
}
.container .list .footer .copyright span {
  width: 300px;
}
.container .list .footer .copyright span:last-child {
  width: 200px;
  text-align: right;
}
.container .list .footer .print-info {
  display: flex;
  margin-bottom: 10px;
  justify-content: space-between;
}
.container .list .footer .print-info span:first-child {
  width: 260px;
}
.container .list .footer .print-info span {
  width: 300px;
}
.container .list .footer .print-info span:last-child {
  width: 200px;
  text-align: right;
}

.dictionary-list {
    width: 98vw;
    border-radius: 4px;
    /deep/.el-table {
        width: 100% !important;
        .el-table__header-wrapper {
        width: 100%;
        .el-table__header {
            width: 100% !important;
        }
        thead {
            tr {
                th{
                    text-align: center;
                    font-size: 14px;
                    font-weight: bold;
                    color: #414658;
                }
            }
        }
        }
        .el-table__body-wrapper {
            width: 100%;
            .el-table__body {
                width: 100% !important;
            }
        tbody {
            tr {
            td{
                height: 92px;
                text-align: center;
                font-size: 14px;
                color: #414658;
                font-weight: 400;
                padding: 0;
            }
            }
        }
        }

        th {
            width: 100% !important;
            height: 61px;
        }
        .cell {
            width: 100% !important;
            .name-info {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                img {
                    width: 60px;
                    height: 60px;
                    margin-right: 10px;
                }
            }
        }
    }


}

</style>
