/**
 * 时间：2024/04/29 xuxiong
 * 说明：统一消息回复
 */
import { MessageBox, Message } from 'element-ui'
/**
 * 如何调用，如下面调用案例
 * import {succesMsg,warnMsg,infoMsg,
     errorMsg,alertBox,confirmBox} from '@/utils/msgBox.ts'
 
  confirmBox('确认删除该标签吗？','确定',null).then(res => {
    alert("确定："+res)
  }).catch(res => {
    alert("取消关闭："+res)
  })
 * */
// 成功提示信息
export function succesMsg (msg) {
  Message({
    type: 'success',
    // showClose: true,
    dangerouslyUseHTMLString: true,
    message: msg,
    offset: 102
  })
}

// 警告提示信息
export function warnMsg (msg) {
  Message({
    type: 'warning',
    // showClose: true,
    dangerouslyUseHTMLString: true,
    message: msg,
    offset: 102
  })
}

// 错误提示信息
export function errorMsg (msg) {
  Message({
    type: 'error',
    // showClose: true,
    dangerouslyUseHTMLString: true,
    message: msg,
    offset: 102
  })
}

// 一般信息提示信息
export function infoMsg (msg) {
  Message({
    type: 'info',
    // showClose: true,
    dangerouslyUseHTMLString: true,
    message: msg,
    offset: 102
  })
}
// 确定一个确定按钮alertBox
export function alertBox (msg, type, title, btnName) {
  // title是提示标题，没有传递就是默认为提示,是非必传参数
  // msg参数是提示的内容，必传参数
  // type是传递消息提示是什么类型，也是非必传参数，默认为warning
  // btnName是修改确认按钮文字 非必传
  let titleName = title ?? '提示'
  let confirmName = btnName ?? '知道了'
  let typeName = type ?? 'warning'
  return MessageBox.alert(msg, titleName, {
    type: typeName,
    confirmButtonText: confirmName,
    dangerouslyUseHTMLString: true
  })
}

// 确定取消;是否按钮弹出框
export function confirmBox (msg, type, title, btnName) {
  // title是提示标题，没有传递就是默认为提示,是非必传参数
  // msg参数是提示的内容，必传参数
  // type是传递消息提示是什么类型，也是非必传参数，默认为warning
  // btnName是修改确认按钮文字 非必传
  let titleName = title ?? '提示'
  let typeName = type ?? 'warning'
  let confirmName = btnName ?? '确定'
  let cancelsName = btnName == '确定' ? '取消' : ''
  return MessageBox.confirm(msg, titleName, {
    type: typeName,
    confirmButtonText: confirmName, //确认按钮文字
    cancelButtonText: cancelsName, //取消按钮文字
    closeOnClickModal: false, //是否可通过点击遮罩层关闭 MessageBox
    closeOnPressEscape: false, //是否可通过按下 ESC 键关闭 MessageBox
    dangerouslyUseHTMLString: true //是否将 message 作为 HTML 片段处理
  })
}
