import Base from "./Base";
import router from '@/router';
import { getStorage } from '@/utils/storage'
class Upload extends Base {
  getClassify() {
    let params = {
      api: 'resources.file.groupList',
    }
    return this.post(params);
  }

  async getFileList(data) {
    let params = {
      api: 'resources.file.index',
      pageNo: data.page,
      pageSize: data.pageSize,
      groupId: data.pid,
      startDate: data.start_time,
      endDate: data.end_time,
      title: data.title,
      storeId: data.storeId
    };
    if (data.img_type) {
      params.img_type = data.img_type;
    }
    return await this.post(params);
  }

  async deleteClassify(id) {
    let params = {
      api: 'resources.file.delCatalogue',
      id: id
    }

    return await this.post(params);
  }

  async createByURL(url, text, storeId) {
    let params = {
      api: 'resources.file.uploadUrlFiles',
      imgUrl: url,
      text: text,
      storeId: storeId
    }

    return await this.post(params);
  }

  async getFileInfo(id) {
    let params = {
      m: "getImgInfo",
      id: id
    };
    return await this.post("?module=system&action=Fupload", params);
    //
  }

  async editFileInfo(fileInfo) {
    let uri = "?module=system&action=Fupload";

    let data = {
      m: "updateImg",
      ...fileInfo
    };

    return await this.post(uri, data);
  }

  async deleteFile(id) {
    // let uri = "?module=software&action=group";

    // let data = {
    //   m: "deleteImg",
    //   data: JSON.stringify(fileList)
    // };

    let params = {
      api: 'resources.file.delFile',
      id: id
    }

    return await this.post(params);
  }

  async editClassify(id, name) {
    let params = {
      api: 'resources.file.createCatalogue',
      id: id,
      catalogueName: name
    }

    return await this.post(params);
  }

  async createClassify(name) {
    let params = {
      api: 'resources.file.createCatalogue',
      catalogueName: name
    }
    return await this.post(params);
  }

  async moveClassify(data) {
    let params = {
      api: 'resources.file.uploadFiles',
      id: data.id,
      groupId: data.groupId,
    }
    return await this.post(params);
  }

  async modifyInfo(data) {
    const currentPath = router && router.currentRoute && router.currentRoute.path
      ? router.currentRoute.path
      : ''
    const userInfo = getStorage('laike_admin_userInfo') || {}
    let params = {
      api: 'resources.file.uploadFiles',
      mchId: currentPath.split('/')[1] === 'Platform' ? '' : (userInfo.mchId || ''),
      id: data.id,
      groupId: data.groupId,
      title: data.title,
      explain: data.explain,
      alternativeText: data.alternative_text,
      describe: data.describe
    }
    return await this.post(params);
  }
}

export default Upload;
