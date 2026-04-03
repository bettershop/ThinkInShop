import request from "../https";
let { entries } = Object;

// 获取快递公司
export const searchExpress = params => {
  return request({
    method: "post",
    params
  });
};

// 获取订单列表
export const orderList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 删除订单
export const delOrder = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});

// 订单详情
export const orderDetailsInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 编辑订单页面详情
export const editOrderView = (params) => {
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

// 编辑订单
export const saveEditOrder = request.throttle((params) => {
  let formData = new FormData();
  for (let [key, value] of entries(params)) {
    if ((key == 'orderStatus' || key == 'orderAmt') && value == null) {
    } else {
      formData.append(key, value);
    }
  }
  return request({
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
});

// 发货界面信息
export const deliveryView = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 发货提交
export const deliverySave = request.throttle((params) => {
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
});

// 当前所有的发货接口 适用 电子面单 普通订单 店铺代发
export const deliverySubmission = (params) => {
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

// 打印订单
export const orderPrint = (params) => {
  return request({
    method: "post",
    params,
  });
};

//代客下单
export const helpOrder = (params) => {
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
  // return request({
  //   method: "post",
  //   params,
  // });
};

// 获取商品列表
export const getGoodsConfigureList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取订单统计
export const orderCount = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取代客下单结算数据
export const Settlement = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取物流信息
export const kuaidishow = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取适用核销门店
export const getStoreList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取适用核销门店
export const getRecordList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取核销信息
export const verificationExtractionCode = (params) => {
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