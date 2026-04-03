import request from "../https";
let { entries } = Object;

// 获取会员列表
export const getMenberList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 删除会员
export const delMember = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取会员购买记录
export const getBuyRecord = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取会员信息
export const getMemberInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 编辑会员信息
export const updateMember = (params) => {
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

// 获取非会员列表
export const getUser = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加会员
export const addMember = (params) => {
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

// 获取会员商品列表
export const memberProList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 删除会员商品
export const delMemberPro = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 非会员商品列表
export const proList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加会员商品
export const addMemberPro = (params) => {
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

// 设置会员制配置信息
export const addOrUpdate = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取会员制配置
export const getMemberConfig = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 保存
export const saveMember = (params) => {
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
}

// 判断是否跳转自营店
export const getGoStore = (params) => {
  return request({
    method: "post",
    params,
  });
};