import request from "../https";
let { entries } = Object;

// 获取库存列表
export const getStockInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取商品库存详细信息
export const getStockDetailInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加库存
export const addStock = request.throttle((params) => {
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

// 批量修改库存
export const batchAddStock = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});