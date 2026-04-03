import request from "../https";
let { entries } = Object;

// 获取库存列表
export const relatedProducts = (params) => {
  return request({
    method: "post",
    params,
  });
};
