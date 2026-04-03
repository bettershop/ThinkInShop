import request from "../https";
let { entries } = Object;
// 获取属性名称下拉
export const getSkuAttributeList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取商品属性信息
export const getSkuInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加/修改商品属性
export const addSku = (params) => {
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

// 添加/修改商品名称
export const addSkuName = (params) => {
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

// 批量删除商品属性
export const delSku = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 商品属性生效开关
export const setSkuSwitch = (params) => {
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

// 获取商品属性属性值列表
export const getSkuList = (params) => {
  return request({
    method: "post",
    params,
  });
};
