import request from "../https";

// 获取面单设置列表
export const getTableList = (params) => {
    return request({
      method: "post",
      params,
    });
  };
  // 获取物流数据 logistics

  export const getLogisticsList = (params) => {
    return request({
      method: "post",
      params,
    });
  };

  // 保存编辑物流数据 
  export const saveLogistics = (params) => {
    return request({
      method: "post",
      params,
    });
  };
  // 数据回显 echo

  export const echoLogistics = (params) => {
    return request({
      method: "post",
      params,
    });
  };

  // 删除数据
  export const del = (params) => {
    return request({
      method: "post",
      params,
    });
  };