/*
 * @Description: 表单数据预处理
 * @Author: 绍吉
 * @Date: 2019-08-23 15:37:31
 * @LastEditTime: 2019-08-30 18:14:16
 * @LastEditors: Please set LastEditors
 */
var that2 = '';
const pt = (code) => {
    const status = new Map([
        ['pt-0', that2.language.groupOrder.obligation],
        //['pt-1', '待发货'],
        ['pt-1', that2.language.order.myorder.to_delivered],
        ['pt-2', that2.language.order.myorder.to_Receiving],
        //['pt-2', '待收货'],
        
        //['pt-3', '交易成功'],
        ['pt-3', that2.language.groupOrder.complete],
        ['pt-9', that2.language.groupOrder.spelling],
        ['pt-10', that2.language.groupOrder.noRefund],
        ['pt-11', that2.language.groupOrder.refunded],
        ['pt-6', that2.language.groupOrder.closed],
        ['pt-12', that2.language.groupOrder.complete],
        ['pt-5', that2.language.groupOrder.complete],
        ['default', '']
    ])
    return status.get(code) || status.get('default')
}

const p = (code) => {
    const status = new Map([
        ['GM-0', that2.language.order.myorder.to_paid],
        ['GM-1', that2.language.order.myorder.to_delivered],
        ['GM-2', that2.language.order.myorder.to_Receiving],
        ['GM-3', that2.language.order.order.state[3]],
        ['GM-4', that2.language.order.myorder.return_goods],
        ['GM-5', that2.language.order.order.state[3]],
        ['GM-12', that2.language.order.myorder.transaction],
        ['GM-7', that2.language.order.order.state[4]],
        ['GM-6', that2.language.order.order.state[4]],
        ['default', '']
    ])
    return status.get(code) || status.get('default')
}

const integral = (code) => {
    const status = new Map([
        ['integral-0', that2.language.order.myorder.to_paid],
        ['integral-1', that2.language.order.myorder.to_delivered],
        ['integral-2', that2.language.order.myorder.to_Receiving],
        ['integral-3', that2.language.order.order.state[3]],
        ['integral-4', that2.language.order.myorder.return_goods],
        ['integral-5', that2.language.order.order.state[3]],
        ['integral-12', that2.language.order.myorder.transaction],
        ['integral-7', that2.language.order.order.state[4]],
        ['integral-6', that2.language.order.order.state[4]],
        ['default', '']
    ])
    return status.get(code) || status.get('default')
}

const fx = (code) => {
    const status = new Map([
        ['FX-0', that2.language.order.myorder.to_paid],
        ['FX-1', that2.language.order.myorder.to_delivered],
        ['FX-2', that2.language.order.myorder.to_Receiving],
        ['FX-3', that2.language.order.myorder.to_evaluated],
        ['FX-4', that2.language.order.myorder.return_goods],
        ['FX-5', that2.language.order.order.state[3]],
        ['FX-6', that2.language.order.order.state[4]],
        ['FX-7', that2.language.order.order.state[4]],
        ['FX-12', that2.language.order.order.state[3]],
        ['default', '']
    ])
    return status.get(code) || status.get('default')
}

const kj = (code) => {
    const status = new Map([
        ['KJ-0', that2.language.order.myorder.to_paid],
        ['KJ-1', that2.language.order.myorder.to_delivered],
        ['KJ-2', that2.language.order.myorder.to_Receiving],
        ['KJ-3', that2.language.order.myorder.to_evaluated],
        ['KJ-4', that2.language.order.myorder.return_goods],
        ['KJ-5', that2.language.order.order.state[3]],
        ['KJ-6', that2.language.order.order.state[4]],
        ['KJ-7', that2.language.order.order.state[4]],
        ['KJ-12', that2.language.order.order.state[3]],
        ['default', '']
    ])
    return status.get(code) || status.get('default')
}

const ms = (code) => {
    const status = new Map([
        ['MS-0', that2.language.order.myorder.to_paid],
        ['MS-1', that2.language.order.myorder.to_delivered],
        ['MS-2', that2.language.order.myorder.to_Receiving],
        ['MS-3', that2.language.order.myorder.to_evaluated],
        ['MS-4', that2.language.order.myorder.return_goods],
        ['MS-5', that2.language.order.order.state[3]],
        ['MS-6', that2.language.order.order.state[4]],
        ['MS-7', that2.language.order.order.state[4]],
        ['MS-12', that2.language.order.order.state[3]],
        ['default', '']
    ])
    return status.get(code) || status.get('default')
}

const CODE = new Map([
    [/^pt-[0-99]/, pt],
    [/^GM-[0-99]/, p],
    [/^integral-[0-99]/, integral],
    [/^FX-[0-99]/, fx],
    [/^KJ-[0-99]/, kj],
    [/^MS-[0-99]/, ms]
])



export default function(list, type = '',that) {
    if (Array.isArray(list)) {
        for (let item of list) {
            let {
                ismch,
                shop_name,
                shop_id,
                otype,
                status,
                list
            } = item

            if (otype !== 'pt') {
                item['isExtract'] = true
            }

            const code = `${otype}-${status}`
            that2 = that
            for (let or of CODE) {
                if (or[0].test(code)) {
                    item['codetext'] = or[1](code)
                    break
                }
            }

        }

        // 商品数据
        function setComList(list) {

            let arr = []

            // 修复禅道 11340 问题用in 代替
            for (let key in list) {
                let item = list[key] 
                if (item.shop_name !== undefined) {

                    if (!arr.length) {
                        arr.push(item.shop_name)

                        continue
                    }
                    arr.push(item.shop_name)
                }
            }

            let result = repetitionIndex(arr)

            // 删除店铺名
            for (let item of result) {
                list[item].shop_name = ''
            }
        }

        // 获取重复店铺下标
        function repetitionIndex(arr) {
            let result = []

            for (let i = 0; i < arr.length; i++) {
                for (let j = 1; j < arr.length; j++) {
                    if (arr[i] === arr[j] && j !== i && j > i) {
                        result.push(j)
                    }
                }
            }

            return result
        }

        list.forEach(e => {
            setComList(e.list)
        })
    }

}
