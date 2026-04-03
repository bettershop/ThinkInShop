// import request from '@/plugins/request';
/**
 * @description 商品管理-- 分类
 */
export function treeListApi () {
  return request({
    url: 'product/category/tree/1',
    method: 'get'
  });
}

/**
 * @description 选择商品 -- 列表
 */
export function changeListApi (params) {
  return request({
    url: `product/product/list`,
    method: 'GET',
    params
  });
}
