import request from "../https";
let { entries } = Object;
// 批量发货
export const batchDelivery = (data) => {
  return request({
    method: "post",
    headers: {
      "Content-Type": "multipart/form-data",
    },
    params: {
      api: "admin.order.batchDelivery",
    },
    data,
  });
};
// 列表数据
export const index = (params) => {
  return request({
    method: "post",
    params,
  });
};
// 保存/编辑
export const save = (params) => {
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
};
// 删除
export const del = (params) => {
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
};
