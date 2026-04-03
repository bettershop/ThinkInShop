import request from "../https";
let { entries } = Object;
// 获取系统基本配置
export const getSystemIndex = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加修改系统基本配置
export const addSystemConfig = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 列表api
export const index = (params) => {
  let formData = new FormData();
  for (let [key, value] of entries(params)) {
    if(value!==null&&value!==""&&value!==undefined){
      formData.append(key, value);
  }
    // formData.append(key, value);
  }
  return request({
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};
//保存/编辑
export const save = (params) => {
  return request({
    method: "post",
    params,
  });
};
