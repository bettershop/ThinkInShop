import request from "../https";
let { entries } = Object;

// 获取商品分类信息
export const getClassInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加/编辑类别
export const addClass = (params) => {
  const payload = params || {};
  const apiName = payload.api || "";
  const className =
    payload.className === undefined || payload.className === null
      ? ""
      : String(payload.className).trim();

  if (apiName === "admin.goods.addClass" && className.length > 15) {
    throw new Error(`分类名称最多15个字符，当前${className.length}个`);
  }

  if (Object.prototype.hasOwnProperty.call(payload, "className")) {
    payload.className = className;
  }

  let formData = new FormData();
  for (let [key, value] of entries(payload)) {
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

// 类别置顶
export const classSortTop = (params) => {
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

// 删除当前类别
export const delClass = (params) => {
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

// 获取当前分类的所有上级分类
export const getClassLevelTopAllInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取当前分类审核列表
export const auditList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 审核
export const audit = (params) => {
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
