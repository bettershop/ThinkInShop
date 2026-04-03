import request from '../https'


// 获取页面数据
export const index = params => {
  return request({
    method: 'post',
    params
  })
}
// 保存/编辑
export const save = params => {
  return request({
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
// 删除
export const del = params => {
  return request({
    method: 'post',
    params
  })
}

// 获取Pc商城页面配置 

export const getConfig = params => {
  return request({
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 保存PC商城配置 admin.pc.
export const addConfig = params => {
  return request({
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}