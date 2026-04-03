/**
 * 订单类型枚举
 * @type {Object}
 */
export const ORDER_TYPES = {
    // 待付款
    ORDERS_R_STATUS_UNPAID: {
        value: 0,
        label: 'orderLists.dfk'
    },
    //  待发货
    ORDERS_R_STATUS_CONSIGNMENT: {
        value: 1,
        label: 'orderLists.dfh'
    },
    // 待收货
    ORDERS_R_STATUS_DISPATCHED: {
        value: 2,
        label: 'orderLists.dsh'
    },
    // 已完成
    ORDERS_R_STATUS_COMPLETE: {
        value: 5,
        label: 'orderLists.ywc'
    },
    // 订单关闭
    ORDERS_R_STATUS_CLOSE: {
        value: 7,
        label: 'orderLists.ygb'
    },
    //待核销
    ORDERS_R_STATUS_TOBEVERIFIED: {
        value: 8,
        label: 'orderLists.dhx'
    },
}

/**
 * 根据订单类型值获取对应的标签
 * @param {number} value - 订单类型值
 * @returns {string} - 订单类型标签
 */
export const getOrderTypeLabel = (value, _this) => {
    const status = Object.values(ORDER_TYPES).find(type => type.value === Number(value));
    return _this.$t(status.label) || '无效类型'
};