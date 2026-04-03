import request from '../https'
let { entries } = Object;

// 获取竞拍商城列表
export const getAuctionList = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取保证金列表
export const getCashList = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除保证金
export const delcash = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取专场列表
export const getspecialLlist = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 是否显示
export const switchShow = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取竞拍配置
export const getConfig = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 竞拍配置
export const addConfig = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 发布专场
export const addspecial = params => {
  let formData = new FormData();
    for (let [key, value] of entries(params)) {
      if(key=="id"&&!value){

      }else{

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
}
// 专场明细
export const specialdetails = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除专场
export const delespecial = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 专场上线
export const specialline = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取场次列表
export const geteventsList = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

//获取专场下拉
export const geteventselect = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

//获取还未参与的商品
export const getstore = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

//发布/修改场次
export const setEvents = params => {
  return request({
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

//删除场次
export const delEvents = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

//获取场次明细
export const eventsDetails = params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

//查看拍品列表
export const seebidList= params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

//删除竞拍商品
export const delectBid= params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

//出价详情列表
export const checkDetails= params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
//------------------竞拍订单接口--------------------------
export const orderList= params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const delOrder= params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
export const closeOrder= params => {
  return request({
    method: 'post',
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}