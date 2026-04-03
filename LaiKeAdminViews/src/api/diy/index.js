import qs from 'qs'
import request from '../https'
let { entries } = Object;

// 创建DIY
export const diyCreate = params => {
    return request({
        method: 'post',
        params
    })
}

//  获取diy信息
export const diyGetInfo = params => {
    return request({
        method: 'post',
        params
    })
}
// 添加、编辑diy信息
export const addDiyInfo = params => {
    let formData = new FormData()
    for (let [key, value] of entries(params)) {
      formData.append(key, value)
    }
    return request({
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }