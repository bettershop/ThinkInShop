/**
 * 获取是否开启免密码登录开关
 * @param {Object} store_type    店铺类型
 */
export function getLaiketuiNoRegisterLoginInfo(store_type) {
    uni.setStorageSync('needRegister', 2);
}
