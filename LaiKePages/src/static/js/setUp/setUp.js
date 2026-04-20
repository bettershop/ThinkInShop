
/*
*1分钟倒计时
* 页面渲染用 count
*/  
const _timeCommon = (me) => {
    const TIME_COUNT = 60;
    if (!me.timer) {
        me.display = false;
        me.count = TIME_COUNT;
        me.timer = setInterval(() => {
            if (me.count > 0 && me.count <= TIME_COUNT) {
                me.count--;
            } else {
                me.display = true;
                clearInterval(me.timer);
                me.timer = null;
            }
        }, 1000);
    }
}

module.exports = {
   _timeCommon
}
