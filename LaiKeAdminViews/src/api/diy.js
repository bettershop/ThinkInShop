// import request from '@/plugins/request';
// import request2 from '@/plugins/request/index2';
import qs from 'qs'

/**
 * @description DIY模板列表
 * @param {Object} param data {Object} 传值参数
 */
export function diyList (data) {
  return request({
    url: 'diy/get_list',
    method: 'get',
    params: data
  });
}
export async function axios(url,data) {
  try {
    let res = await request2.post(url, qs.stringify(data))
    console.log(res);
    return res
  } catch (e) {
    console.log(e);
  }
  //return request2.post(url,data)
}
/**
 * @description 保存DIY数据
 * @param {Object} param data {Object} 传值参数
 */
export function diySave (id, data) {
  return request2.post('index.php?module=template&action=Config&m=edit', {
    m: 'edit',
    id: id,
    value: data.value,
    cover: data.cover
  })
}

/**
 * @description 保存DIY数据
 * @param {Object} param data {Object} 传值参数
 */
export function diyCreate (data) {
  return request2.post('index.php?module=template&action=Config&m=create', {
    m: 'create',
    value: data.value,
    title: data.title,
    cover: data.cover
  })
}

/**
 * @description 获取DIY数据
 * @param {Object} param data {Object} 传值参数
 */
export async function diyGetInfo (id, data) {
  try {
    let res = await request2.post('index.php?module=template&action=Config&m=detail', qs.stringify({
      m: 'detail',
      id
    }))
    console.log(res);
    return res
  } catch (e) {
    console.log(e);
  }
}

export async function getPlugins (type = 2) {
  try {
    let res = await request2.post('index.php?module=template&action=Config&m=getPlugin', qs.stringify({
      m: 'getPlugin',
      type0: type
    }))
    return res;
  } catch (e) {
    console.error(e);
  }
}

/**
 * @description 删除DIY数据
 * @param {Object} param data {Object} 传值参数
 */
export function diyDel (id) {
  return request({
    url: 'diy/del/' + id,
    method: 'delete'
  });
}

/**
 * @description 使用diy模板
 * @param {Object} param data {Object} 传值参数
 */
export function setStatus (id) {
  return request({
    url: 'diy/set_status/' + id,
    method: 'put'
  });
}

/**
 * @description 获取分类
 */
export function categoryList () {
  return request({
    url: '/cms/category_list',
    method: 'get'
  });
}

/**
 * @description 获取产品分类
 */
export function getCategory () {
  return request({
    url: 'diy/get_category',
    method: 'get'
  });
}

/**
 * @description 获取链接列表
 */
export function getUrl () {
  return request({
    url: 'diy/get_url',
    method: 'get'
  });
}

/**
 * @description 获取商品列表
 */
export function getProduct (data) {
  return request({
    url: 'diy/get_product',
    method: 'get',
    params: data
  });
}
