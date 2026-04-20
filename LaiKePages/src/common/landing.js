// ✅【修改】删除全局 let toast; —— 不再需要
// let toast;

import langModules from '@/common/lang/config.js'

// ✅【新增】辅助函数：获取当前语言的 landing toast 数组
function getCurrentLandingToasts() {
    const currentLang = uni.getStorageSync('language') || 'zh_CN';
    // 安全访问：如果当前语言无配置，回退到 zh_CN
    return langModules[currentLang]?.toasts?.landing || langModules.zh_CN.toasts.landing;
}

//用户名验证
export function onblur(value, display) {
    // ✅【修改】替换整个 if-else 块
    const toast = getCurrentLandingToasts();

    var re = /^\w{6,20}$/g
    var rez = re.test(value)
    if (rez == true) {
        uni.showToast({
            title: toast[0],
            duration: 1000,
            icon: 'none'
        })
        return
    }
    if (value == '') {
        display.style.display = 'block'
        display.innerHTML = toast[1]
        uni.showToast({
            title: toast[1],
            duration: 1000,
            icon: 'none'
        })
    } else {
        display.style.display = 'block'
        // ⚠️ 注意：value = '' 只改局部变量，无法清空外部输入框
        // 如需清空，应操作传入的引用或通过事件通知
    }
}

//密码验证
export function pass(value, display, a) {
    // ✅【修改】替换整个 if-else 块
    const toast = getCurrentLandingToasts();

    var re = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$/
    var rez = re.test(value)
    if (rez == true) {
        return
    }
    if (value == '') {
        uni.showToast({
            title: toast[2],
            duration: 1000,
            icon: 'none'
        })
    } else {
        uni.showToast({
            title: toast[3],
            duration: 1000,
            icon: 'none'
        })
    }
}

//确认密码验证
export function confirmpass(value1, value2) {
    // ✅【修改】替换整个 if-else 块
    const toast = getCurrentLandingToasts();

    if (value1 == '') {
        uni.showToast({
            title: toast[4],
            duration: 1000,
            icon: 'none'
        })
        return false
    }

    if (value2 == '') {
        uni.showToast({
            title: toast[5],
            duration: 1000,
            icon: 'none'
        })
        return false
    }

    if (value1 != value2) {
        uni.showToast({
            title: toast[6],
            duration: 1000,
            icon: 'none'
        })
        return false
    }

    return 1
}

//验证手机号码
export function telephone(value) {
    // ✅【修改】替换整个 if-else 块
    const toast = getCurrentLandingToasts();

    var re = /^1[3|4|5|6|7|8|9][0-9]\d{4,8}$/
    var wrResult = re.test(value)
    if (wrResult && value.length == 11) {
        return 1
    } else if (value == '') {
        return 2
    } else {
        uni.showToast({
            title: toast[7],
            duration: 1000,
            icon: 'none'
        })
        return 3
    }
}

//一分钟倒计时（无需语言，保留原样）
export function time(timer, count) {
    const TIME_COUNT = 60
    if (!timer) {
        count = TIME_COUNT
        timer = setInterval(() => {
            if (count > 0 && count <= TIME_COUNT) {
                count--
            } else {
                clearInterval(timer)
                timer = null
            }
        }, 1000)
    }
}

//清空内容（⚠️ 此函数实际无效，因 JS 基本类型按值传递）
export function empty(value) {
    // ✅【建议】此函数可删除，或改为操作对象/数组
    value = ''
}
