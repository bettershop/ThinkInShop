import { setStorage, getStorage } from '@/utils/storage'
import store from '@/store/index'
import { isEmpty } from 'element-ui/src/utils/util'
import { MessageBox, Message } from 'element-ui'
import { router } from '@/router'

const data = {
   
  install(Vue) {

    // 动态设置浏览器icon图标
    Vue.prototype.setDynamicFavicon = function (imgIcon) {
      
      if (!imgIcon) {
        return
      }
      // 1. 先移除已有的icon标签（避免重复）
      const oldLink = document.querySelector('link[rel="icon"]');
      if (oldLink) {
        document.head.removeChild(oldLink);
      }
      // 2. 创建新的icon标签（对应你提供的原生代码）
      const link = document.createElement('link');
      link.rel = 'icon';
      // 兼容ico/png格式，自动设置type
      link.type = imgIcon.includes('png')
        ? 'image/png'
        : 'image/x-icon';
      link.href = imgIcon;

      // 3. 插入到head中
      document.head.appendChild(link);
    }

    Vue.prototype.fn1 = function (value) {
      console.log(value)
    }

    Vue.prototype.fn2 = function () {
      console.log(456)
    }

    //获取来源
    Vue.prototype.getSource = function () {
      let obj = getStorage('laike_source')
      if (isEmpty(obj)) {
        store
          .dispatch('source/getSource')
          .then(r => (obj = getStorage('laike_source')))
      }
      let map = new Map()
      for (let i = 0, l = obj.length; i < l; i++) {
        let source = obj[i]
        map.set(source.value, source.label)
      }
      return map
    }

    // 设置只能输入整数
    Vue.prototype.oninput2 = function (num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '')
      str = str.replace('.', '')

      return str
    }

    // 设置只能输入整数和小数
    Vue.prototype.oninput = function (num, limit) {
      var str = num
      var len1 = str.substr(0, 1)
      var len2 = str.substr(1, 1)
      //如果第一位是0，第二位不是点，就用数字把点替换掉
      if (str.length > 1 && len1 == 0 && len2 != '.') {
        str = str.substr(1, 1)
      }
      //第一位不能是.
      if (len1 == '.') {
        str = ''
      }
      //限制只能输入一个小数点
      if (str.indexOf('.') != -1) {
        var str_ = str.substr(str.indexOf('.') + 1)
        if (str_.indexOf('.') != -1) {
          str = str.substr(0, str.indexOf('.') + str_.indexOf('.') + 1)
        }
      }
      //正则替换
      str = str.replace(/[^\d^\.]+/g, '') // 保留数字和小数点
      if (limit / 1 === 1) {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/, '$1') // 小数点后只能输 1 位
      } else {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/, '$1') // 小数点后只能输 2 位
      }

      return str
    }

    // 设置不能输入特殊字符
    Vue.prototype.oninput3 = function (num) {
      var str = num
      str = str.replace(/[^\w]/g, '')

      return str
    }

    // 设置不能输入汉字
    Vue.prototype.oninput4 = function (num) {
      var str = num
      str = str.replace(/[\u4E00-\u9FA5]/g, '')

      return str
    }

    Vue.prototype.stripscript = function (value) {
      var pattern = new RegExp(
        "[`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]"
      )
      var rs = ''
      for (var i = 0; i < value.length; i++) {
        rs = rs + value.substr(i, 1).replace(pattern, '')
      }
      return rs
    }

    //判断数组中是否包含某个值（检测按钮权限用）
    Vue.prototype.detectionBtn = function (arr, value) {
      return arr.some(item => item === value)
    }

    // 判断tab路由是否存在该权限设置
    let mySong = (route, value, name) => {
      if (name === 1) {
        return route.some(item => item.module === value)
      } else {
        return route.some(item => item.url === value)
      }
    }
    Vue.prototype.handleTabLimits = function (route, value, name) {
      // console.log('路由权限列表xxxxxx',route);
      return mySong(route, value, name)
      return true
    }

    /**
      * xuxiong
      * 如何调用，如下面调用案例,页面直接this调用
      * this.succesMsg(this.$t("zdata.sccg"))
      * this.warnMsg(this.$t("zdata.sccg"))
      * ....
      * this.confirmBox('确认删除该标签吗？',...其他参数).then(res => {
          确认后的逻辑执行
        }).catch(res => {
          取消回调
        })
    **/
    // 成功提示信息
    Vue.prototype.succesMsg = function (msg) {
      Message({
        type: 'success',
        // showClose: true,
        dangerouslyUseHTMLString: true,
        message: msg,
        offset: 102
      })
    }
    // 警告提示信息
    Vue.prototype.warnMsg = function (msg) {
      Message({
        type: 'warning',
        // showClose: true,
        dangerouslyUseHTMLString: true,
        message: msg,
        offset: 102
      })
    }
    // 错误提示信息
    Vue.prototype.errorMsg = function (msg) {
      Message({
        type: 'error',
        // showClose: true,
        dangerouslyUseHTMLString: true,
        message: msg,
        offset: 102
      })
    }
    // 一般信息提示信息
    Vue.prototype.infoMsg = function (msg) {
      Message({
        type: 'info',
        // showClose: true,
        dangerouslyUseHTMLString: true,
        message: msg,
        offset: 102
      })
    }
    // 确定一个确定按钮alertBox
    Vue.prototype.alertBox = function (msg, type, title, btnName) {
      // title是提示标题，没有传递就是默认为提示,是非必传参数
      // msg参数是提示的内容，必传参数
      // type是传递消息提示是什么类型，也是非必传参数，默认为warning
      // btnName是修改确认按钮文字 非必传
      let titleName = title ?? this.$t('zdata.ts')
      let confirmName = btnName ?? this.$t('zdata.zdl')
      let typeName = type ?? 'warning'
      return MessageBox.alert(msg, titleName, {
        type: typeName,
        confirmButtonText: confirmName,
        dangerouslyUseHTMLString: true
      })
    }
    // 确定取消;是否按钮弹出框
    Vue.prototype.confirmBox = function (msg, type, title, btnName) {
      // title是提示标题，没有传递就是默认为提示,是非必传参数
      // msg参数是提示的内容，必传参数
      // type是传递消息提示是什么类型，也是非必传参数，默认为warning
      // btnName是修改确认按钮文字 非必传
      let titleName = title ?? this.$t('zdata.ts')
      let typeName = type ?? 'warning'
      let confirmName = btnName ?? this.$t('zdata.ok')
      let cancelsName =
        btnName == this.$t('zdata.ok') ? this.$t('zdata.off') : ''
      return MessageBox.confirm(msg, titleName, {
        type: typeName,
        confirmButtonText: confirmName, //确认按钮文字
        cancelButtonText: cancelsName, //取消按钮文字
        closeOnClickModal: false, //是否可通过点击遮罩层关闭 MessageBox
        closeOnPressEscape: false, //是否可通过按下 ESC 键关闭 MessageBox
        dangerouslyUseHTMLString: true //是否将 message 作为 HTML 片段处理
      })
    }

    /**
          * 交换数组中任意两个位置的元素，返回新数组
          * @param {Array} originalArray - 原始数组
          * @param {number} fromIndex - 要交换的第一个元素的索引
          * @param {number} toIndex - 要交换的第二个元素的索引
          * @returns {Array} - 交换后的新数组
          */
    Vue.prototype.swapArrayElements = function (originalArray, fromIndex, toIndex) {
      // 确保索引在有效范围内
      if (fromIndex < 0 || fromIndex >= originalArray.length ||
        toIndex < 0 || toIndex >= originalArray.length) {
        throw new Error('索引超出数组范围');
      }

      // 创建原数组的深拷贝
      const newArray = JSON.parse(JSON.stringify(originalArray));

      // 使用临时变量保存要交换的值
      const temp = newArray[fromIndex];

      // 使用 Vue.set() 更新数组元素，确保触发响应式更新
      this.$set(newArray, fromIndex, newArray[toIndex]);
      this.$set(newArray, toIndex, temp);
      return newArray;
    }
    /**
     * 表单校验 -自动 滚动到第一个错误元素位置
     * @param {*} formRefName - 表单名称（ref）
     * @returns 
     */

    Vue.prototype.$validateAndScroll = function(formRefName = 'ruleForm',offset = -100) {
      return new Promise((resolve, reject) => {
        const form = this.$refs[formRefName]
        
        if (!form || !form.validate) {
          reject(new Error(`未找到表单引用或表单不是 ElForm 组件: ${formRefName}`))
          return
        }
        
        form.validate((valid) => {
          if (valid) {
            resolve(true)
            return
          }
          
          // 校验失败，查找第一个错误元素并滚动
          this.$nextTick(() => {
            const firstErrorEl = form.$el.querySelector('.el-form-item__error')
            
            if (firstErrorEl) {
              const formItemEl = firstErrorEl.closest('.el-form-item')
              
              if (formItemEl) {
                // const elementTop = formItemEl.getBoundingClientRect().top + window.pageYOffset
                // const targetPosition = elementTop + offset
                // console.log('targetPosition',targetPosition)
                // 平滑滚动到指定位置
                // window.scrollTo({
                //   top: 0,
                //   behavior: 'smooth'
                // })
               
                formItemEl.scrollIntoView({ behavior: 'smooth', block: 'start' })

              
                // 获取错误消息
                const errorMessage = firstErrorEl.textContent.trim()
                resolve({ valid: false, firstError: errorMessage })
              } else {
                resolve({ valid: false, firstError: '未找到错误元素' })
              }
            } else {
              resolve({ valid: false, firstError: '未找到错误提示' })
            }
          })
        })
      })
    }


  }
}

export default data
