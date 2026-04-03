import request from "../https";
let { entries } = Object;
// 获取字典列表
export const dictionaryList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加/修改字典表明细
export const addDictionaryTable = (params) => {
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

// 获取字典目录下拉
export const dictionaryDirectories = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取字典目录编码
export const directoriesCode = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 删除字典明细
export const deleteDictionary = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 字典表明细开关
export const switchDictionaryDetail = (params) => {
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

// 数据名称管理
export const dataName = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加/编辑数据字典目录
export const addDictionaryInfo = (params) => {
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

// 删除字典目录
export const delDictionary = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加数据名称
export const addDataName = (params) => {
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

// 字典表开关
export const switchDictionary = (params) => {
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


// 判断是否跳转自营店
export const getGoStore = (params) => {
  return request({
    method: "post",
    params,
  });
};
