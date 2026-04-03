import request from '../https'
let { entries } = Object;

// 列表数据
export const index = params => {
  return request({
    method: 'post',
    params
  })
}
// 删除
export const del = request.throttle(params => {
  return request({
    method: 'post',
    params
  })
})
// 删除
export const disable = request.throttle(params => {
  return request({
    method: 'post',
    params
  })
})
// 保存/编辑
export const save = request.throttle(params => {
  let formData = new FormData();
  for (let [key, value] of entries(params)) {
    formData.append(key, value);
  }
  return request({
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
})
