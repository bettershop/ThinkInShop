/**
 * 时间处理方法
 * @param {string}  starTime    传入开始时间  （时间格式：2020-07-25 15:00:00）
 * @param {string}  endTime     传入结束时间  （时间格式：2020-07-25 15:00:00）
 * @param {Boolean} time_max    判断开始/结束倒计时  （true/false）
 * @return {Object} { times:{all:剩余总时间戳，h:剩余时，m:剩余分，s:剩余秒}, time_a:当前时间戳, time_b:传入开始时间戳, time_c:传入结束时间戳 }
 */
export function  my_setTime (starTime, endTime, time_max){
    // console.log('进入时间处理函数--》my_setTime')
    //获取当前时间戳 time_a
    let time_a = Date.now();
    let time_b = 0
    let time_c = 0
    //开始时间
    if(starTime){
        console.log('传入开始时间--》starTime')
        //获取开始的时间 time_b
        let date1 = new Date(starTime);
        //判断是不是- iOS
        if(date1 == 'Invalid Date'){
            //是iOS
            //console.log('iOS，时间格式读取错误，执行格式转换！')
            let crsh1 = starTime.replace(/-/g,'/');
            date1 = new Date(crsh1);
        } else {
            //不是iOS
            //console.log('非iOS，时间格式读取正常！')
        }
        //转化时间戳
        time_b = Date.parse(date1)
    }
    //结束时间
    if(endTime){
        console.log('传入结束时间--》endTime')
        //获取结束的时间戳 time_c
        let date2 = new Date(endTime);
        //判断是不是- iOS
        if(date2 == 'Invalid Date'){
            //是iOS
            //console.log('iOS，时间格式读取错误，执行格式转换！')
            let crsh2 = endTime.replace(/-/g,'/');
            date2 = new Date(crsh2);
        } else {
            //不是iOS
            //console.log('非iOS，时间格式读取正常！')
        }
        //转化时间戳
        time_c = Date.parse(date2)
    }
    //判断是 开始的倒计时/结束的倒计时
    let times = {}
    let y_time = null
    if(time_max){
        y_time = time_b - time_a    //剩余 开始时间戳 = 开始时间 - 当前时间
    } else {
        y_time = time_c - time_a    //剩余 结束时间戳 = 结束时间 - 当前时间
    }
    
    //剩余总时间戳 放入time对象中
    times.all = y_time
    
    //剩余总时间戳-转化-小时
    times.h = Math.floor(y_time/1000/3600)
    //转化-时最小为0
    if(times.h < 0){times.h = 0}
    
    //剩余总时间戳-先除小时取余-转化-分钟
    let h_qy = y_time/1000%3600 
    times.m = Math.floor(h_qy/60)
    //转化-分最小为0
    if(times.m < 0){times.m = 0}
    
    //剩余总时间戳-先除分钟取余-转化-秒钟
    let m_qy = h_qy%60 
    times.s = Math.floor(m_qy)
    //转化-秒最小为0
    if(times.s < 0){times.s = 0}
    
    //{ times:{all:剩余总时间戳，h:剩余时，m:剩余分，s:剩余秒}, time_a:当前时间戳, time_b:传入开始时间戳, time_c:传入结束时间戳 }
    return {
        times: times,
        time_a: time_a,
        time_b: time_b,
        time_c: time_c,
    }
}

/**
 * 开启倒计时方法
 * @param {Object} time 单个倒计时 {h:24, m:59, s:59}
 * @param {Object} time 列表循环倒计时 [{h:24, m:59, s:59}, {h:24, m:59, s:59}, {h:24, m:59, s:59}]
 */
export function  time_s (time){
    //console.log('进入倒计时函数--》time_s')
    //列表循环倒计时 数组对象 -- time[{h:24, m:59, s:59}, {h:24, m:59, s:59}, {h:24, m:59, s:59}]
    if(time[0]){
        console.log('循环倒计时方法--》time[{}]')
        //初始化倒计时
        let autoTime = []
        //这里i，不能用var，let会保存i。
        for(let i=0;i<time.length;i++){
            autoTime[i] = setInterval(()=>{
                //如果时分秒有一个大余0，则秒钟开始倒计时
                if(time[i].h > 0 | time[i].m > 0 |  time[i].s > 0 ){
                    //先判断秒钟减到0后，分钟要减减
                    if(time[i].s == 0){
                        //如果时分有一个大于0，则秒钟=60 且分钟开始倒计时
                        if(time[i].h > 0 | time[i].m > 0){
                            time[i].s = 60
                            //先判断分钟减到0后，小时减减
                            if(time[i].m == 0){
                                //如果时大于0，则分钟=60 且小时开始倒计时
                                if(time[i].h > 0){
                                    time[i].m = 60
                                    //先判断小时减到0后，小时就不变了
                                    if(time[i].h == 0){}
                                    //再写小时未减到0，小时要减减
                                    time[i].h--
                                }
                            }
                            //再写分钟未减到0，分钟要减减
                            time[i].m--
                        }
                    }
                    //再写秒钟未减到0，秒钟要减减
                    time[i].s--
                } else if(time[i].h == 0 & time[i].m == 0 &  time[i].s == 0 ) {
                    let nubell = i + 1
                    console.log('time_s--》第'+nubell+'条数据：倒计时时间到了！已经清除计时器！')
                    clearInterval(autoTime[i])
                }
            },1000)
        }
    }
    //单个倒计时 对象 -- time：{h:24, m:59, s:59}
    if(time.s){
        console.log('普通倒计时方法--》time.h')
        //初始化倒计时
        let autoTime = []
        autoTime = setInterval(()=>{
            //如果时分秒有一个大余0，则秒钟开始倒计时
            if(time.h > 0 | time.m > 0 |  time.s > 0 ){
                //先判断秒钟减到0后，分钟要减减
                if(time.s == 0){
                    //如果时分有一个大于0，则秒钟=60 且分钟开始倒计时
                    if(time.h > 0 | time.m > 0){
                        time.s = 60
                        //先判断分钟减到0后，小时减减
                        if(time.m == 0){
                            //如果时大于0，则分钟=60 且小时开始倒计时
                            if(time.h > 0){
                                time.m = 60
                                //先判断小时减到0后，小时就不变了
                                if(time.h == 0){}
                                //再写小时未减到0，小时要减减
                                time.h--
                            }
                        }
                        //再写分钟未减到0，分钟要减减
                        time.m--
                    }
                }
                //再写秒钟未减到0，秒钟要减减
                time.s--
            } else if(time.h == 0 & time.m == 0 &  time.s == 0 ) {
                console.log('time_s--》倒计时时间到了！已经清除计时器！')
                clearInterval(autoTime)
            }
        },1000)
    }
}

/**
 * 精度丢失问题
 * @param {Number} figure   数字
 * @param {Number} digit    精度到小数点几位
 * @return {Number} 处理精度丢失后的数字
 */
export function  solve_Float (figure, digit){
    //Math.pow(底数x,指数y) 10的 digit 次方
    var m = Math.pow(10, digit);
    //先转化整数，再转化为小数 Math.round向上取整
    return Math.round(figure * m, 10) / m;
}
