export function LaiKeTui_axios(me) {
    let data = { 
        api: "mch.App.Mch.Order_details",
        shop_id: me.shop_id,
        sNo: me.order_id,
    };
    if(me.supplierInfo =='supplier'){
        // 供应商 非售后订单查看详情
        data.api = 'supplier.AppMch.Orders.orderDetailsInfo'
        delete data.shop_id 
        data.mchId = me.shop_id
    }
    me.$req.post({ data }).then((res) => {
        let { code, data, message } = res;
        me.load = false;
        if (code == 200) {
            data.list.coupon_price = Number(data.list.coupon_price).toFixed(2);
            data.list.preferential_amount = Number(
                data.list.preferential_amount
            ).toFixed(2);
            me.list = data.list;
            me.supplierInfo =data.supplierInfo
            me.show_write_store = data.show_write_store
            me.write_store_num = data.write_store_num
            me.city_all = data.list.sheng + "-" + data.list.shi + "-" + data.list.xian;
            //商家配送 查看
            me.virtualTimeEnd = data.list.storeSelfInfo?.delivery_time + ' ' + (data.list.storeSelfInfo?.delivery_period == 1 ? '上午':'下午')
            me.deliveryTime = data.list.storeSelfInfo?.delivery_time
            if( data.list.cpc){
                me.code2 = data.list.cpc
            }
            me.deliveryPeriod = data.list.storeSelfInfo?.delivery_period
            //商家配送 时间列表
            if(me.list.storeSelfInfo && me.list.storeSelfInfo.delivery_time && me.storeHxTime.length == 0){
                for(var a = 1; a<8; a++){
                    me.getTime(a)
                }
            }
        } else {
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
        }
    });
}
// 折扣商品详情
export function LaiKeTui_ZKaxios(me) {
    let data = { 
        api: "plugin.flashsale.AppMchOrderFlashSale.Order_details",
        shopId: me.shop_id,
        sNo: me.order_id,
    };
    me.$req.post({ data }).then((res) => {
        let { code, data, message } = res;
        me.load = false;
        if (code == 200) {
            data.list.coupon_price = Number(data.list.coupon_price).toFixed(2);
            data.list.preferential_amount = Number(
                data.list.preferential_amount
            ).toFixed(2);
            me.list = data.list;
            me.city_all =
                data.list.sheng + "-" + data.list.shi + "-" + data.list.xian;
        } else {
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
        }
    });
}
// 折扣商品编辑
export function LaiKeTui_ZKok(me) {
    setTimeout(function () {
        me.title = me.language.order.order.title;
        me.diplay = true;
        var orderDetail = {
            address: me.list.address,
            mobile: me.list.mobile,
            name: me.list.name,
            z_price: me.list.z_price,
            area: me.city_all,
            remarks: me.list.remarks,
        };
        var data = {
            // api: "plugin.Mch.flashsale.Order.saveEditOrder",
            api: "plugin.flashsale.AppMchOrderFlashSale.saveEditOrder",
            sNo: me.order_id,
            shopId: me.shop_id,
            orderDetail: JSON.stringify(orderDetail),
        };
        me.$req.post({ data }).then((res) => {
            me.editor = ''
            let { code, data, message } = res;
            if (code == 200) {
                me.is_sus = true;
                
                setTimeout(function () {
                    me.is_sus = false;
                    me._axios();
                }, 1500);
            }
        });
    }, 500);
}
//折扣商品发货
export function LaiKeTui_ZKsend(me) {
    if (!me.fastTap) {
        return;
    }
    me.fastTap = false;
    let data = {
        shop_id: me.shop_id,
        api:"plugin.Mch.flashsale.Order.Deliver",
        sNo: me.order_id,
        express_id: me.express_id,
        courier_num: me.courier_num,
        orderListId: "",
    };
    me.$req.post({ data }).then((res) => {
        let { code, data, message } = res;
        if (code == 200) {
            uni.showToast({
                title: me.language.toasts.order.send,
                duration: 1500,
                icon: "none",
            });
            setTimeout(function () {
                me._axios();
                me.fhDiv = false;
                me.fhRadios = false;
                me.fastTap = true;
            }, 1500);
        } else {
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
            me.fastTap = true;
        }
    });
}

// 直播的商品详情
export function LaiKeTui_ZBaxios(me) {
    let data = {
        api: "plugin.living.AppMchOrder.Order_details",
        shop_id: me.shop_id,
        sNo: me.order_id,
    };
    me.$req.post({ data }).then((res) => {
        let { code, data, message } = res;
        me.load = false;
        if (code == 200) {
            data.list.coupon_price = Number(data.list.coupon_price).toFixed(2);
            data.list.preferential_amount = Number(
                data.list.preferential_amount
            ).toFixed(2);
            me.list = data.list;
            me.city_all =
                data.list.sheng + "-" + data.list.shi + "-" + data.list.xian;
        } else {
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
        }
    });
}
// 直播编辑
export function LaiKeTui_ZBok(me) {
    setTimeout(function () {
        me.title = me.language.order.order.title;

        me.diplay = true;
        var orderDetail = {
            address: me.list.address,
            mobile: me.list.mobile,
            name: me.list.name,
            z_price: me.list.z_price,
            area: me.city_all,
            remarks: me.list.remarks,
        };
        var data = {
            api: "plugin.living.AppMchOrder.Up_order",
            sNo: me.order_id,
            shop_id: me.shop_id,

            orderDetail: JSON.stringify(orderDetail),
        };
        me.$req.post({ data }).then((res) => {
            let { code, data, message } = res;
            me.is_sus = true;
            setTimeout(function () {
                me.is_sus = false;
            }, 1500);
            if (code == 200) {
                // 禅道 2372: 保存后需返回到直播订单页面
                uni.navigateBack({
                    delta:1
                })
                // me._axios(); 
            }
        });
    }, 500);
}
// 直播发货
export function LaiKeTui_ZBsend(me) {
    if (!me.fastTap) {
        return;
    }
    me.fastTap = false;
    let data = {
        shop_id: me.shop_id,
        api:"plugin.living.AppMchOrder.Send",
        sNo: me.order_id,
        express_id: me.express_id,
        courier_num: me.courier_num,
        orderList_id: "",
    };
    me.$req.post({ data }).then((res) => {
        let { code, data, message } = res;
        if (code == 200) {
            uni.showToast({
                title: me.language.toasts.order.send,
                duration: 1500,
                icon: "none",
            });
            setTimeout(function () {
                me._axios();
                me.fhDiv = false;
                me.fhRadios = false;
                me.fastTap = true;
            }, 1500);
        } else {
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
            me.fastTap = true;
        }
    });
}

export function LaiKeTui_showFhDiv(me) {
    me.fhDiv = true;
    let data = {
        shop_id: me.shop_id,
        // module: 'app',
        // action: 'mch',
        // m: 'into_send',
        api: "mch.App.Mch.Into_send",
    };
    me.$req.post({ data }).then((res) => {
        let { code, data, message } = res;
        if (code == 200) {
            var list = [];
            me.kuaidiList = data.list;
            for (var key in data.list) {
                list.push(data.list[key].kuaidi_name);
            }
            me.pickerValueArray = list;
        } else {
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
        }
    });
}
// 编辑订单
export function LaiKeTui_ok(me) {
    setTimeout(function () {
        me.title = me.language.order.order.title;
        me.diplay = true;
        var orderDetail = {
            address: me.list.address,
            mobile: me.list.mobile,
            name: me.list.name,
            z_price: me.list.z_price,
            area: me.city_all,
            remarks: me.list.remarks,
            delivery_time: me.deliveryTime || '',
            delivery_period: me.deliveryPeriod || '',
        };
        orderDetail.cpc = me.code2
        if(!['86','852','853'].includes(me.code2)){
            orderDetail.area = ''
        }
        var data = {
            api: "mch.App.Mch.Up_order",
            sNo: me.order_id,
            shop_id: me.shop_id,
            orderDetail: JSON.stringify(orderDetail),
        };
        me.$req.post({ data }).then((res) => {
            let { code, data, message } = res;
          
            if (code == 200) {
                me.is_sus = true;
                setTimeout(function () {
                    me.is_sus = false;
                }, 1500);
                
                uni.removeStorageSync('diqu')
                me._axios();
            }else{
                uni.showToast({
                    title:me.language.wlyc,
                    icon:'error'
                })
            }
        });
    }, 500);
}

export function LaiKeTui_send(me) {
    if (!me.fastTap) {
        return;
    }
    me.fastTap = false;

    let data = {
        shop_id: me.shop_id,
        // module: 'app',
        // action: 'mch',
        // m: 'send',
        api: "mch.App.Mch.Send",
        sNo: me.order_id,
        express_id: me.express_id,
        courier_num: me.courier_num,
        orderList_id: "",
    };
    me.$req.post({ data }).then((res) => {
        let { code, data, message } = res;
        if (code == 200) {
            uni.showToast({
                title: me.language.toasts.order.send,
                duration: 1500,
                icon: "none",
            });
            setTimeout(function () {
                me._axios();
                me.fhDiv = false;
                me.fhRadios = false;
                me.fastTap = true;
            }, 1500);
        } else {
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
            me.fastTap = true;
        }
    });
}

export function QRs(me, rew) {
    var data = {
        // module: 'app',
        // action: 'mch',
        // m: 'sweep_extraction_code',
        api: "mch.App.Mch.Sweep_extraction_code",
        shop_id: me.shop_id,
        extraction_code: rew.result,
    };
    me.$req.post({ data }).then((res) => {
        let { code, data, message } = res;
        uni.hideLoading();
        if (code == 200) {
            me.order_id = data.order_id;
            me.p_price = data.p_price;
            me.sNo = data.sNo;
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
            // 成功后跳转 QRsuccess页面
            uni.redirectTo({
                url:
                    "/pagesC/myStore/QRsuccess?p_price=" +
                    me.p_price +
                    "&sNo=" +
                    me.sNo +
                    "&order_id=" +
                    me.order_id,
            });
        } else {
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
        }
    });
    me.$req.post({ data }).then((res) => {
        let { code, data, message } = res;
        uni.hideLoading();
        if (code == 200) {
            me.order_id = data.order_id;
            me.p_price = data.p_price;
            me.sNo = data.sNo;
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
            // 成功后跳转 QRsuccess页面
            uni.redirectTo({
                url:
                    "/pagesC/myStore/QRsuccess?p_price=" +
                    me.p_price +
                    "&sNo=" +
                    me.sNo +
                    "&order_id=" +
                    me.order_id,
            });
        } else {
            uni.showToast({
                title: message,
                duration: 1500,
                icon: "none",
            });
        }
    });
}
