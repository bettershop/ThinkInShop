import request from "../https";
let { entries } = Object;

// 保存/编辑楼层信息
export const addFloor = (params) => {
  return request({
    method: "post",
    data: params,
    headers: {
      "Content-Type": "application/json",
    },
  });
};

// 获取楼层信息
export const getFloorInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 删除楼层
export const delFloor = (params) => {
  return request({
    method: "post",
    params,
  });
};


// 是否开启楼层
export const openFloor = (params) => {
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

// 获取商品列表
export const getGoodList = (params) => {
  return request({
    method: "post",
    params,
  });
};
// 添加商品
export const addGood = (params) => {
  return request({
    method: "post",
    data: params,
    headers: {
      "Content-Type": "application/json",
    },
  });
};
// 删除商品
export const delGood = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 排序商品
export const editSort = (params) => {
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

// 获取来源
export const getType = (params) => {
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