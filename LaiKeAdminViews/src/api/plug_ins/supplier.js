import request from "../https";
let { entries } = Object;

// 获取订单列表
export const orderList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取订单详情
export const orderDetailsInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取提现审核
export const withdraw = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 提现审核
export const examineWithdraw = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取提现配置
export const getConfig = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 更新提现配置
export const addUpdateConfig = (params) => {
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

// 获取经营收益
export const income = (params) => {
  return request({
    method: "post",
    params,
  });
};
// 获取订单结算列表
export const getSettlement = (params) => {
  return request({
    method: "post",
    params,
  });
};
