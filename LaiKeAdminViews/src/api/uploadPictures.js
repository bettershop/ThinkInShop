import request from './https'
// import request from '@/plugins/request';
// import request2 from '@/plugins/request/index2';
import qs from 'qs'

/**
 * @description 附件分类--列表
 * @param {Object} param data {Object} 传值参数
 */
export async function getCategoryListApi (data) {
  let res = await request2({
    url: 'index.php?module=software&action=group',
    method: 'post',
    data: qs.stringify({
      m: 'list_group'
    })
  });
  res.data.list = [];
  res.data.map(item => {
    let newItem = {
      children: [],
      enname: null,
      id: item.id,
      name: item.name,
      pid: 0,
      title: item.name
    }
    res.data.list.push(newItem)
  })
  console.log(res);
  return res;
}

/**
 * @description 添加分类
 */
export async function createApi (treeData) {
  console.log(treeData)
  return {
    'status': 200,
    'msg': 'ok',
    'data': {
      'rules': [
        {
          'type': 'input',
          'field': 'name',
          'title': '分类名称',
          'value': '',
          'props': {
            'type': 'text',
            'placeholder': '请输入分类名称',
            'maxlength': 30
          },
          'validate': [],
          'col': []
        }
      ],
      'title': '添加分类',
      'action': 'index.php?module=software&action=group&m=save_group',
      'method': 'POST',
      'info': '',
      'status': true,
      treeData
    }
  }
  // return request({
  //   url: 'file/category/create',
  //   method: 'get',
  //   params: id
  // });
}

/**
 * @description 编辑分类
 */
export async function categoryEditApi (data) {
  console.log(data.name);
  return {
    'status': 200,
    'msg': 'ok',
    'data': {
      'rules': [
        {
          'type': 'input',
          'field': 'name',
          'title': '分类名称',
          'value': data.name,
          'props': {
            'type': 'text',
            'placeholder': '请输入分类名称',
            'maxlength': 30
          },
          'validate': [],
          'col': []
        }
      ],
      'title': '编辑分类',
      'action': 'index.php?module=software&action=group&type=edit',
      'id': data.id,
      'method': 'POST',
      'info': '',
      'status': true
    }
  }
  // return request({
  //   url: `file/category/${id}/edit`,
  //   method: 'get'
  // });
}

/**
 * @description 删除分类
 * @param data
 */
export async function categoryDelApi (data) {
  let postData = {}
  postData = qs.stringify({
    m: 'save_group',
    data: JSON.stringify({
      id: data.id,
      name: '',
      is_default: 0,
      is_delete: 1
    })
  })

  await request2.post('index.php?module=software&action=group', postData, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
  return {
    'status': 200,
    'msg': 'ok',
    'data': null
  }
}

/**
 * @description 附件列表
 * @param {Object} param data {Object} 传值
 */
export async function fileListApi (data) {
  let params = {
    api: 'resources.file.index',
    pageNo: data.page,
    groupId: data.pid
  }
  const {path,query} = this.$route   
  if(path == '/plug_ins/template/addTemplate'){ 
    this.$set(data,'img_type',1)
    // 判断是否是 系统主题 0  还是 自定义主题  1
    params.diy_img_type = query.typeIndex - 1
    // 主题 id
    params.diyId = query.id
  }
  let res = await request({
      method: 'post',
      params
  })
  let list = []
  res.data.list.map(item => {
    list.push({
      att_dir: item.url,
      att_id: item.id,
      satt_dir: item.url,
      isSelect: 0
    })
  })
  return {
    code: 200,
    msg: 'ok',
    data: {
      list: list,
      count: res.data.total
    }
  }
  // let params = {
  //   module: 'system',
  //   action: 'Fupload',
  //   type: 'image',
  //   page: data.page,
  //   dataType: 'json',
  //   group_id: data.pid
  // }
  // let res = await request2.get('index.php', {
  //   params: params
  // })

  // let list = []
  // res.data.list.map(item => {
  //   list.push({
  //     att_dir: item.file_url,
  //     att_id: item.id,
  //     satt_dir: item.file_url,
  //     isSelect: item.selected
  //   })
  // })
  // return {
  //   code: 200,
  //   msg: 'ok',
  //   data: {
  //     list: list,
  //     count: res.data.count
  //   }
  // }
}

/**
 * @description 移动分类，修改附件分类表单
 * @param {Object} param data {Object} 传值
 */
export function moveApi (data) {
  return request({
    url: 'file/file/do_move',
    method: 'put',
    data
  });
}

/**
 * @description 删除附件
 * @param {String} param ids {String} 图片id拼接成的字符串
 */
export async function fileDelApi (checkPicList) {
  console.log(checkPicList);
  let postData = {}
  let delData = [];
  checkPicList.map(item => {
    let item2 = {
      file_url: item.att_dir,
      id: item.att_id,
      selected: 1
    }
    delData.push(item2)
  })

  postData = {
    m: 'diy_delete',
    data: delData
  }

  let res = await request2.post('index.php?module=software&action=group', postData, {
    headers: {
      // 'Content-Type': 'application/x-www-form-urlencoded'
      'Content-Type': 'application/json'
    }
  })
  console.log(res);
  return {
    'status': 200,
    'msg': 'ok',
    'data': null
  }
}


//  获取分类列表
export const classList = params => {
  return request({
      method: 'post',
      params
  })
}

//  获取文件列表
export const fileList = params => {
  return request({
      method: 'post',
      params
  })
}

//  删除分类
export const delClass = params => {
  return request({
      method: 'post',
      params
  })
}

//  删除图片
export const delFile = params => {
  return request({
      method: 'post',
      params
  })
}

//  添加分类
export const addClass = params => {
  return request({
      method: 'post',
      params
  })
}
