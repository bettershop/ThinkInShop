import request from '../https'
let { entries } = Object

// 列表数据
export const index = params => {
  return request({
    method: 'post',
    params
  })
}
// 保存/编辑
export const save = request.throttle(params => {
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
})
// 删除
export const del = request.throttle(params => {
  return request({
    method: 'post',
    params
  })
})

// 充值列表数据
export const topList = params => {
  return request({
    method: 'post',
    params
  })
}

// 资金管理数据
export const fundList = params => {
  return request({
    method: 'post',
    params
  })
}

// 资金详情数据
export const fundsDetailsList = params => {
  return request({
    method: 'post',
    params
  })
}

// 积分列表数据
export const pointList = params => {
  return request({
    method: 'post',
    params
  })
}

// 积分详情数据
export const pointDetailsList = params => {
  return request({
    method: 'post',
    params
  })
}

// 分账记录
export const management = params => {
  return request({
    method: 'post',
    params
  })
}
