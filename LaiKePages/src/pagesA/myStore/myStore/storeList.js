// 进入上传商品页面
export function LaiKeTui_axios (me) {
    let data = {
        // module: 'app',
        // action: 'mch',
        // m: 'order_details',
        api:"mch.App.Mch.Order_details",
        shop_id: me.shop_id,
        sNo: me.order_id,
    }
    me.$req.post({data}).then(res => {
        if (res.code == 200) {
            me.list = res.list
        } else {
            uni.showToast({
                title: res.message,
                duration: 1500,
                icon: 'none'
            })
        }
    })
}
