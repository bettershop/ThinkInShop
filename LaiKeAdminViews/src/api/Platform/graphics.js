import request from "../https";
let { entries } = Object;
// 添加/编辑图片上传配置
export const addImageConfigInfo = (params) => {
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

// 获取商户信息
export const getImageConfigInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};
