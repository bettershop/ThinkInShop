import http from "../http";
let { entries } = Object;
// let formData = new FormData();
//   for (let [key, value] of entries(params)) {
//     formData.append(key, value);
//   }
//   return request({
//     method: "post",
//     data: formData,
//     headers: {
//       "Content-Type": "multipart/form-data",
//     },
//   });
// 后台登录
export const login = (data) => {
  console.log('登录的接口调用',data);
  let data1 = {
    api: "admin.saas.user.login",
    ...data,
  };
  let formData = new FormData();
  for (let [key, value] of entries(data1)) {
    formData.append(key, value);
  }
  return http({
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  // return http({
  //   method: "post",
  //   params: {
  //     api: "admin.saas.user.login",
  //     ...data,
  //   },
  // });
};

// 获取校验码
export const getCode = (data)=>{
  let data1 = {
    api: "admin.saas.user.getCode",
    ...data
  };
  let formData = new FormData();
  for (let [key, value] of entries(data1)) {
    formData.append(key, value);
  }
  return http({
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}
