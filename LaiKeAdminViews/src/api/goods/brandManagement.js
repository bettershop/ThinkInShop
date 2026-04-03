import request from "../https";
let { entries } = Object;

// 保存/编辑品牌信息
export const addBrand = request.throttle((params) => {
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

// 获取品牌信息
export const getBrandInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 品牌置顶
export const brandByTop = request.throttle((params) => {
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

// 删除品牌
export const delBrand = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});

// 获取国家列表
export const getCountry = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取语种列表
export const getLangs = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取分类信息
export const getClassInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取品牌审核列表
export const auditList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 品牌审核
export const examine = request.throttle((params) => {
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
