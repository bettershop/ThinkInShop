import request from "../https";
let { entries } = Object;

// 获取退货列表
export const getRefundList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 退货通过拒绝
export const examine = (params) => {
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
