import request from "../https";
let { entries } = Object;

// 获取分销列表
export const getDistributionInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 修改推荐人
export const editeReferences = request.throttle((params) => {
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

// 修改分销商等级
export const editeGrade = request.throttle((params) => {
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

// 获取分销等级下拉
export const getDistributionGradeList = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取未开通分销商的列表
export const getGrabbleListInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 删除分销商
export const delDistribution = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});

// 开通分销商
export const builderLevel = (params) => {
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

// 批量删除分销商品
export const delDistributionGoods = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});

// 添加/编辑分销商品
export const addDistributionGoods = request.throttle((params) => {
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

// 编辑分佣
export const editeDistribution = request.throttle((params) => {
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

// 获取佣金记录列表
export const getRecordInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取分销商品列表
export const getGoodsDistributionInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取非分销商品列表
export const getGoodsNotDistributionInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取分销等级信息
export const getGradeInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 删除等级
export const delGrade = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});

// 添加/编辑分销等级
export const addGradeInfo = request.throttle((params) => {
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

// 获取佣金排行榜
export const getDistributionTopInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取非当前排行榜的用户列表
export const getDistributionUserInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加/编辑用户排行榜
export const addDistributionRankInfo = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});

// 批量删除排行榜数据
export const delBatchRank = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});

// 获取推荐关系
export const getRelationshipInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 获取提现记录
export const getWithdrawalRecordInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 提现审核
export const withdrawalExamine = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 删除提现记录
export const delWithdrawalRecord = request.throttle((params) => {
  return request({
    method: "post",
    params,
  });
});

// 获取分销配置信息
export const getDistributionConfigInfo = (params) => {
  return request({
    method: "post",
    params,
  });
};

// 添加/编辑分销配置信息
export const addDistributionConfigInfo = request.throttle((params) => {
  return request({
    method: "post",
    data: params,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
});
