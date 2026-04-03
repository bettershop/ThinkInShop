import request from "../https";
let { entries } = Object;

// 获取店铺列表
export const getMchInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取店铺分类
export const getMchFl = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 删除店铺
export const delMchInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 编辑店铺
export const modifyMchInfo = (params) => {
  let formData = new FormData();
  for (let [key, value] of entries(params)) {
    if(value!==null&&value!==undefined){
      formData.append(key, value);
    }
  }
  return request({
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 获取店铺审核列表
export const getMchExamineInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 店铺审核通过/拒绝
export const examineMch = (params) => {
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

// 获取商品审核列表
export const getGoodsExamineInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取商品详情信息
export const getGoodsDetailInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 商品审核
export const goodsExamine = (params) => {
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

// 获取店铺提现审核列表
export const getWithdrawalExamineInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 店铺提现审核
export const withdrawalExamine = (params) => {
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

// 获取提现记录列表
export const getWithdrawalInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取商城配置信息
export const getStoreConfigInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加/修改商城配置
export const setStoreConfigInfo = (params) => {
  return request({
    method: "post",
    data: params,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 新增店铺
export const addStore = (params) => {
  return request({
    method: "post",
    data: params,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 跳转
export const goStoreInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};
// 获取店铺分账信息
export const getBill = params => {
  return request({
    method: 'post',
    params
  })
}

// 保存店铺分账信息
export const saveBill = params => {
  return request({
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取商品单位
export const getFile = params => {
  return request({
    method: 'post',
    params
  })
}

// 获取url
export const getUrl = params => {
  return request({
    method: 'post',
    params
  })
}
// 获取url
export const getDownload = params => {
  return request({
    method: 'post',
    responseType: 'blob',
    params
  })
}
