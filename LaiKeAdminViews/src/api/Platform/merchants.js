import request from "../https";
let { entries } = Object;

// 主题颜色
export const updateColour = (params) => {
  return request({
    method: "post",
    params,
  });
};
// 获取商户信息-更改为formData形式会报错
export const getShopInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加/编辑商城
export const addStore = request.throttle((params) => {
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

// 删除商城
export const delStore = request.throttle((params) => {
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

// 重置密码
export const resetAdminPwd = request.throttle((params) => {
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

// 是否启用开关
export const setStoreOpenSwitch = (params) => {
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

// 设置默认商城
export const setStoreDefaultSwitch = (params) => {
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

// 获取权限列表
export const getUserRoleInfo = (params) => {
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

// 获取权限菜单列表
export const getUserRoleMenuInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 赋予系统管理员商城
export const setUserAdmin = (params) => {
  return request({
    method: "post",
    params,
  });
};
