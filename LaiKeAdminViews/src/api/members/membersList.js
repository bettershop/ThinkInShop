import request from "../https";
let { entries } = Object;

// 获取来源
export const getDictionaryInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取会员列表
export const getUserInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};
// 获取导入记录列表
export const deliverList = (params) => {
  return request({
    method: "post",
    params,
  });
};
// 删除导入记录列表
export const delDelivery = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});
//批量导入
export const batchDelivery = (params) => {
  return request({
    method: "post",
    data: params,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};
// 会员充值金额
export const userRechargeMoney = (params) => {
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

// 修改等级
export const updateUserGradeById = request.throttle((params) => {
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

// 删除用户
export const delUserById = request.throttle((params) => {
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

// 添加用户
export const saveUser = request.throttle((params) => {
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

// 修改会员信息
export const updateUserById = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});

// 获取会员等级下拉
export const goodsStatus = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 会员添加默认地址
export const saveAddress = (params) => {
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

// 获取开通方式
export const method = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取国家地区
export const getItuList = (params) => {
  return request({
    method: "post",
    params,
  });
};
