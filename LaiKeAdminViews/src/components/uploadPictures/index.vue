<template>
  <div class="Modal">
    <Row class="colLeft">
      <Col :xl="6" :lg="6" :md="6" :sm="6" :xs="24" class="colLeft">
      <div class="Nav">
        <div class="trees-coadd">
          <div class="scollhide">
            <div class="trees" v-if="show">
              <Tree :data="treeData" :render="renderContent" class="trformDataeeBox" ref="tree"></Tree>
            </div>
          </div>
        </div>
      </div>
      </Col>
      <Col :xl="18" :lg="18" :md="18" :sm="18" :xs="24" class="colLeft">
      <div class="conter">
        <div class="bnt acea-row row-middle">
          <Col span="24" class="mb10">
          <Button type="primary" :disabled="checkPicList.length===0" @click="checkPics" class="mr10">使用选中图片</Button>
          <Upload :show-upload-list="false" :action="fileUrl" class="mr10 mb10" :data="uploadData" name="file" :with-credentials="true" :multiple="true" accept="image/*" :on-success="handleSuccess" :on-error="handleError" style="margin-top: 1px;display: inline-block">
            <Button type="primary">上传图片</Button>
          </Upload>
          <Button type="success" @click.stop="add" class="mr10">添加分类</Button>
          <Button type="error" class="mr10" :disabled="checkPicList.length===0" @click.stop="editPicList('图片')">
            删除图片
          </Button>
          </Col>
        </div>
        <div class="pictrueList acea-row">
          <Row :gutter="24" class="conter">
            <div v-show="isShowPic" class="imagesNo">
              <Icon type="ios-images" :size="imageSize" color="#dbdbdb" />
              <span class="imagesNo_sp">图片库为空</span>
            </div>
            <div class="acea-row" style="display: grid;grid-template-columns: repeat(5, 1fr);">
              <div class="pictrueList_pic mr10 mb10" v-for="(item, index) in pictrueList" :key="index">
                <img :class="item.isSelect ? 'on': '' " :src="item.url" @click.stop="changImage(item, index, pictrueList)" />
              </div>
            </div>
          </Row>
        </div>
        <div class="footer acea-row row-between-wrapper">
          <Page :total="total" show-elevator show-total @on-change="pageChange" :page-size="fileData.limit" />
        </div>
      </div>
      </Col>
    </Row>
  </div>
</template>

<script>
import {
  categoryDelApi,
  categoryEditApi,
  createApi,
  fileListApi,
  getCategoryListApi,
  moveApi,

  classList,
  delClass,
  fileList
} from '@/api/uploadPictures';
import { getStorage } from '@/utils/storage'
import Setting from '@/setting';
import config from '@/packages/apis/Config'
import log from '@/libs/util.log';
// import { tableDelApi } from '@/api/common';
// import util from '@/libs/util';
let actionUrl = config.baseUrl
export default {
  name: 'uploadPictures',
  // components: { editFrom },
  props: {
    isChoice: {
      type: String,
      default: ''
    },
    gridBtn: {
      type: Object,
      default: null
    },
    gridPic: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      spinShow: false,
      fileUrl: config.baseUrl,
      modalPic: false,
      treeData: [],
      show: true, // 因为没有使用api没有前后端分离，分类无法使用 node 环境调试。 用 show 硬改
      treeData2: [],
      pictrueList: [],
      // uploadData: {}, // 上传参数
      checkPicList: [],
      uploadName: {
        name: ''
      },
      FromData: null,
      isShow: false,
      treeId: -1,
      isJudge: false,
      buttonProps: {
        type: 'default',
        size: 'small'
      },
      fileData: {
        pid: -1,
        page: 1,
        limit: 18
      },
      total: 0,
      pids: 0,
      list: [],
      modalTitleSs: '',
      isShowPic: false,
      header: {},
      ids: [], // 选中附件的id集合
    }
  },
  mounted() {
    this.getToken()
    this.getList()
    this.getFileList()
  },
  computed: {
    imageSize() {
      if (window.outerWidth <= 1440) {
        return "80";
      }
      return "120";
    },
    uploadData() {
      let data = {
        api: 'resources.file.uploadFiles',
        storeId: getStorage('laike_admin_userInfo').storeId,
        groupId: this.treeId,
        uploadType: 2,
        accessId: this.$store.getters.token
      }
      const { path, query } = this.$route
      console.log('page', path, this.$route)
      // 用于diy功能 图片上传
      if (path == '/plug_ins/template/addTemplate') {
        this.$set(data, 'img_type', 1)
        // 判断是否是 系统主题 0  还是 自定义主题  1
        data.diy_img_type = query.typeIndex - 1
        // 主题 id
        data.diyId = query.id
      }

      {
        return {
          ...data
        }
      }
    },
  },
  methods: {
    // 上传头部token
    getToken() {
      // this.header['Authori-zation'] = 'Bearer ' + util.cookies.get('token');
      this.header['Authori-zation'] = 'Bearer ' + this.$store.getters.token;
    },
    // 树状图
    renderContent(h, { root, node, data }) {
      let actionData = [];
      if (data.id !== '' && data.pid == 0) {
        // 添加子分类按钮

        // actionData.push(h('Button', {
        //     props: Object.assign({}, this.buttonProps, {
        //         icon: 'ios-add'
        //     }),
        //     style: {
        //         marginRight: '8px',
        //         display: data.flag ? 'inline' : 'none'
        //     },
        //     on: {
        //         click: () => {
        //             this.append(root, node, data)
        //         }
        //
        //     }
        // }));
      }
      if (data.id !== '') {
        actionData.push(h('Button', {
          props: Object.assign({}, this.buttonProps, {
            icon: 'md-create'
          }),
          style: {
            marginRight: '8px',
            display: data.flag ? 'inline' : 'none'
          },
          on: {
            click: () => {
              this.editPic(root, node, data)
            }
          }
        }));
        actionData.push(h('Button', {
          props: Object.assign({}, this.buttonProps, {
            icon: 'ios-remove'
          }),
          style: {
            display: data.flag ? 'inline' : 'none'
          },
          on: {
            click: () => {
              this.remove(root, node, data, '分类')
            }
          }
        }));
      }
      return h('div', {
        style: {
          display: 'inline-block',
          width: '90%'
        },
        class: ['tree-div'],
        on: {
          mouseenter: () => {
            this.onMouseOver(root, node, data)
          },
          mouseleave: () => {
            this.onMouseOver(root, node, data)
          }
        }
      }, [
        h('span', [
          h('span', {
            style: {
              cursor: 'pointer'
            },
            class: ['ivu-tree-title'],
            on: {
              click: (e) => {
                this.appendBtn(root, node, data, e)
              }
            }
          }, data.name)
        ]),
        h('span', {
          style: {
            display: 'inline-block',
            float: 'right'
          }
        }, actionData)
      ]);
    },
    renderContentSel(h, { root, node, data }) {
      return h('div', {
        style: {
          display: 'inline-block',
          width: '90%'
        },
      }, [
        h('span', [
          h('span', {
            style: {
              cursor: 'pointer'
            },
            class: ['ivu-tree-title'],
            on: {
              click: (e) => {
                this.handleCheckChange(root, node, data, e)
              }
            }
          }, data.title)
        ])
      ])
    },
    // 下拉树
    handleCheckChange(root, node, data, e) {
      this.list = []
      // this.pids = 0;
      let value = data.id;
      let title = data.title;
      this.list.push({
        value,
        title
      });
      if (this.ids.length) {
        this.pids = value;
        this.getMove();
      } else {
        this.warnMsg('请先选择图片');
      }
      let selected = this.$refs.reference.$el.querySelectorAll('.ivu-tree-title-selected');
      for (let i = 0; i < selected.length; i++) {
        selected[i].className = 'ivu-tree-title';
      }
      e.path[0].className = 'ivu-tree-title  ivu-tree-title-selected';// 当前点击的元素
    },
    // 移动分类
    getMove() {
      let data = {
        pid: this.pids,
        images: this.ids.toString()
      }
      moveApi(data).then(async res => {
        this.succesMsg(res.msg);
        this.getFileList();
        this.pids = 0;
        this.checkPicList = [];
        this.ids = [];
      }).catch(res => {
        this.errorMsg(res.msg);
      })
    },
    // 删除图片
    editPicList(tit) {
      console.log(this.ids);
      this.tits = tit;
      console.log(this.checkPicList);
      let ids = {
        ids: this.ids.toString()
      };
      let delfromData = {
        title: '删除选中图片',
        url: `file/file/delete`,
        method: 'POST',
        ids: ids,
        checkPicList: this.checkPicList
      };
      this.$modalSure(delfromData).then((res) => {
        if (res.code == 200) {
          this.succesMsg(this.$t("zdata.sccg"));
        }
        this.getFileList();
        this.checkPicList = [];
      }).catch(res => {
        this.errorMsg(res.msg);
      });
    },
    // 鼠标移入 移出
    onMouseOver(root, node, data) {
      event.preventDefault();
      data.flag = !data.flag
    },
    // 点击树
    appendBtn(root, node, data, e) {
      this.treeId = data.id;
      this.getFileList();
      let selected = this.$refs.tree.$el.querySelectorAll('.ivu-tree-title-selected');
      for (let i = 0; i < selected.length; i++) {
        selected[i].className = 'ivu-tree-title';
      }
      e.path[0].className = 'ivu-tree-title  ivu-tree-title-selected';// 当前点击的元素
    },
    // 点击添加
    append(root, node, data) {
      this.treeId = data.id;
      this.getFrom();
    },
    modalSure(delfromData, isDelete = false) {
      return new Promise((resolve, reject) => {
        let content = '';
        let title = delfromData.name;
        console.log(delfromData)
        if (delfromData.info !== undefined) {
          content = `<p>${delfromData.title}</p><p>${delfromData.info}</p>`
        } else {
          if (isDelete) {
            title = '删除' + title
          }
          content = `<p>确定要${title}吗？</p><p>${title}后将无法恢复，请谨慎操作！</p>`

        }
        this.$Modal.confirm({
          title: title,
          content: content,
          loading: true,
          onOk: () => {
            setTimeout(() => {
              this.$Modal.remove();
              if (delfromData.success) {
                delfromData.success.then(async res => {
                  resolve(res);
                }).catch(res => {
                  reject(res)
                })
              } else {
                console.log(delfromData)
                delClass({
                  api: 'resources.file.delCatalogue',
                  id: delfromData.id
                }).then(async res => {
                  resolve(res);
                }).catch(res => {
                  reject(res)
                })
                console.log(delfromData);
              }
            }, 300);
          },
          onCancel: () => {
            // this.$Message.info('取消成功');
          }
        });
      })
    },
    // 删除分类
    remove(root, node, data, tit) {
      this.modalSure(data, true).then(async (res) => {
        this.succesMsg('成功');
        this.treeId = 0;
        await this.getList();
        this.show = false;
        setTimeout(() => {
          this.show = true;
        }, 100)
        await this.getFileList();

        this.checkPicList = [];
      }).catch(res => {
        this.show = true;
        this.errorMsg(res.msg);
      });

      // let delfromData = {
      //     title: '删除 [ ' + data.title + ' ] ' + '分类',
      //     url: `file/category/${data.id}`,
      //     method: 'DELETE',
      //     ids: ''
      // };
      // this.$modalSure(delfromData).then((res) => {
      //     this.succesMsg(res.msg);
      //     this.getList();
      //     this.checkPicList = [];
      // }).catch(res => {
      //     this.errorMsg(res.msg);
      // });
    },
    // 确认删除树
    // submitModel () {
    //     if (this.tits === '图片') {
    //         this.getFileList();
    //         this.checkPicList = [];
    //     } else {
    //         this.getList();
    //         this.checkPicList = [];
    //     }
    // },
    // 编辑树表单
    editPic(root, node, data) {

      console.log(data);
      this.$modalForm(categoryEditApi(data)).then(() => this.getList());
    },
    // 搜索分类
    changePage() {
      this.getList();
    },
    // 分类列表树
    async getList() {
      try {
        // let res = await getCategoryListApi(this.uploadName);
        // this.treeData = res.data.list;
        // // this.treeData.unshift(data);
        // this.treeData2 = [...this.treeData];
        // this.addFlag(this.treeData);

        const res = await classList({
          api: 'resources.file.groupList'
        })
        this.treeData = res.data.data.list;
        this.treeData2 = [...this.treeData];
        this.addFlag(this.treeData);
        console.log(this.treeData)
        console.log(res)
        // this.treeId = this.treeData[0].id
      } catch (res) {
        this.errorMsg(res.msg);
      }

    },
    addFlag(treedata) {
      treedata.map(item => {
        this.$set(item, 'flag', false)
        item.children && this.addFlag(item.children)
      })
    },
    // 新建分类
    add() {
      console.log(this.treeData);
      this.treeId = -1;
      this.getFrom()
    },
    // 文件列表
    async getFileList() {
      this.fileData.pid = this.treeId;
      try {
        let data = {
          api: 'resources.file.index',
          pageSize: this.fileData.limit,
          pageNo: this.fileData.page,
          groupId: this.fileData.pid
        };
        const { path, query } = this.$route
        if (path == '/plug_ins/template/addTemplate') {
          this.$set(data, 'img_type', 1)
        }
        const res = await fileList({
          ...data
        })
        console.log(res)
        this.pictrueList = res.data.data.list;
        console.log(res)
        if (this.pictrueList.length) {
          this.isShowPic = false
        } else {
          this.isShowPic = true
        }
        this.total = res.data.data.total;


        // let res = await fileListApi(this.fileData);
        // this.pictrueList = res.data.list;
        // console.log(res)
        // if (this.pictrueList.length) {
        //   this.isShowPic = false
        // } else {
        //   this.isShowPic = true
        // }
        // this.total = res.data.count;
      } catch (res) {
        this.errorMsg(res.msg);
      }


    },
    pageChange(index) {
      this.fileData.page = index;
      this.getFileList();
      this.checkPicList = [];
    },
    // 新建分类表单
    getFrom() {
      console.log(this.treeData)
      this.$modalForm(
        createApi({ treeData: this.treeData })
      ).then(() => this.getList());
    },
    // 上传之前
    beforeUpload() {
      this.fileUrl = Setting.apiBaseURL + '/index.php?module=system&action=UploadImg' + '&group_id=' + this.treeId
      this.uploadData = {
        group_id: this.treeId
      }
      let promise = new Promise((resolve) => {
        this.$nextTick(function () {
          resolve(true);
        });
      });
      return promise;
    },
    // 上传成功
    handleSuccess(res, file) {
      console.log(res)
      console.log(file);
      if (res.code == '200') {
        this.$message({
          type: 'success',
          message: '上传成功!',
          offset: 100
        });
        this.getFileList();
      } else {
        this.errorMsg(res.message);
      }
    },
    handleError(error, file, fileList) {
      console.log(error, file, fileList)
      let that = this
      setTimeout(function () {
        that.getFileList()
      }, 300)

    },
    // 关闭
    cancel() {
      this.$emit('changeCancel')
    },
    // 选中图片
    changImage(item, index, row) {
      let selectItem = '';
      this.$set(this.pictrueList[index], 'isSelect', item.isSelect === undefined ? true : !item.isSelect);
      selectItem = this.pictrueList.filter((item) => {
        return item.isSelect === true
      });
      this.checkPicList = selectItem;
      this.ids = [];
      this.checkPicList.map((item, index) => {
        this.ids.push(item.id)
      });
    },
    // 点击使用选中图片
    checkPics() {
      if (this.isChoice === '单选') {
        if (this.checkPicList.length > 1) return this.errorMsg('最多只能选一张图片');
        this.$emit('getPic', this.checkPicList[0]);
      } else {
        this.$emit('getPicD', this.checkPicList);
        console.log(this.checkPicList)
      }
    }
  }
}
</script>

<style scoped lang="stylus">
>>>.ivu-tree-title {
  width: 90%;
}

>>>.tree-div {
  display: flex !important;
  line-height: 40px;
  align-items: center;
  justify-content: space-between;
}

.selectTreeClass {
  background: #d5e8fc;
}

@media only screen and (max-width: 1441px) {
  >>> .ivu-modal {
    width: 70% !important;
  }
}

.treeBox {
  width: 100%;
  height: 100%;

  >>> .ivu-tree-title-selected, .ivu-tree-title-selected:hover {
    color: #2D8cF0 !important;
    background-color: #fff !important;
  }

  >>> .ivu-btn-icon-only {
    width: 20px !important;
    height: 20px !important;
  }

  >>> .ivu-tree-title:hover {
    color: #2D8cF0 !important;
    background-color: #fff !important;
  }
}

.pictrueList_pic {
  width: 120px;
  height: 120px;
  cursor: pointer;

  img {
    width: 100%;
    height: 100%;
  }
}

@media only screen and (max-width: 1441px) {
  .pictrueList_pic {
    width: 80px;
    height: 80px;
    cursor: pointer;

    img {
      width: 100%;
      height: 100%;
    }
  }
}

.trees-coadd {
  width: 100%;
  border-radius: 4px;
  overflow: hidden;
  position: relative;

  .scollhide {
    overflow-x: hidden;
    overflow-y: scroll;
    padding: 10px 0 10px 0;
    box-sizing: border-box;

    .trees {
      width: 100%;
      max-height: 374px;
    }
  }

  .scollhide::-webkit-scrollbar {
    display: none;
  }
}

.treeSel >>> .ivu-select-dropdown-list {
  padding: 0 5px !important;
  box-sizing: border-box;
}

.imagesNo {
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  margin: 65px 0;

  .imagesNo_sp {
    font-size: 13px;
    color: #dbdbdb;
    line-height: 3;
  }
}

.Modal {
  width: 100%;
  height: 100%;
  background: #fff !important;
}

.Nav {
  width: 100%;
  border-right: 1px solid #eee;
}

.colLeft {
  padding-right: 0 !important;
  height: 100%;
}

.conter {
  width: 99%;
  height: 100%;
}

.conter .bnt {
  width: 100%;
  padding: 0 13px 10px 15px;
  box-sizing: border-box;
}

.conter .pictrueList {
  padding-left: 25px;
  width: 100%;
}

.conter .pictrueList img {
  width: 100%;
  border: 2px solid #fff;
}

.conter .pictrueList img.on {
  border: 2px solid #5FB878;
}

.conter .footer {
  padding: 20px;
}
</style>
<style>
.ivu-modal {
  top: 50% !important;
  transform: translateY(-50%) !important;
}
</style>
<style scoped llang="less">
.tree-div > span {
  width: 50% !important;
}
</style>
