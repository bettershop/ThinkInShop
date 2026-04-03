import request from "../../https";
let { entries } = Object;

// 列表数据
export const index = (params) => {
  return request({
    method: "post",
    data: params,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};
// 保存/编辑
export const save = (params) => {
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
// 保存编辑短信模板
export const saveMessage = (params) => {
  return request({
    method: "post",
    params,
    // headers: {
    //     'Content-Type': 'multipart/form-data'
    // }
  });
};

// 核心设置编辑保存
export const saveConfigInfo = (params) => {
  return request({
    method: "post",
    data: params,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};
// 删除
export const del = (params) => {
  return request({
    method: "post",
    params,
  });
};
