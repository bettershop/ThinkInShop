import request from "../https";
import qs from "qs";
let { entries } = Object;

// 获取会员配置信息
export const getUserConfigInfo = (params) => {
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

// 删除/修改配置
export const addUserRule = (params) => {
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

// 获取国家地区
export const getItuList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 自动生成账号
export const generateAccount = (params) => {
  return request({
    method: "post",
    params,
  });
};