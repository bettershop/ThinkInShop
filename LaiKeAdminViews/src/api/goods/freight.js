import request from "../https";
let { entries } = Object;

// 获取运费列表
export const index = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取运费列表
export const delStore = (params) => {
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

// 获取运费列表
export const setDefaultStore = (params) => {
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

// 获取运费列表
export const addFreight = (params) => {
  return request({
    method: "post",
    data: params,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 获取运费列表
export const getRegion = (params) => {
  return request({
    method: "post",
    params,
  });
};
