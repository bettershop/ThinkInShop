import request from "../https";

// 获取面单设置列表
export const getTableList = (params) => {
  return request({
    method: "post",
    params,
  });
};

export const getConfig = (params) => {
  return request({
    method: "post",
    params,
  });
};


export const updateStatus = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});

export const overridePrint = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});




export const del = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});
