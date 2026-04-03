<template>
  <div class="hot_imgs">
    <div class="title" v-if="configData.title">
      {{configData.title}}
    </div>
    <div class="list-box">
      <draggable
        class="dragArea list-group"
        :list="configData.list"
        group="peoples"
        handle=".move-icon"
      >
        <div class="item" v-for="(item,index) in configData.list" :key="index">
<!--          <div class="move-icon">-->
<!--            <span class="iconfont-diy icondrag"></span>-->
<!--          </div>-->
          <Upload :show-upload-list="false" :action="fileUrl" class="mr10 mb10 img-box"
                  :before-upload="beforeUpload"
                  :data="uploadData"
                  name="name"
                  :headers="header"
                  :multiple="false"
                  accept="video/*"
                  :on-success="handleSuccess"
                  style="margin-top: 1px;display: inline-block">
            <video :src="item.img" v-if="item.img"></video>
            <div class="upload-box" v-else>
              <Icon type="ios-camera-outline" size="22"/>
            </div>
          </Upload>
<!--          <div class="img-box" @click="modalPicTap('单选',index)">-->
<!--            -->
<!--&lt;!&ndash;            <div class="delect-btn" @click.stop="bindDelete(item,index)"><span class="iconfont-diy icondel_1"></span>&ndash;&gt;-->
<!--&lt;!&ndash;            </div>&ndash;&gt;-->
<!--          </div>-->
          <div class="info">
            <div class="info-item" v-for="(infos,key) in item.info" :key="key">
              <span>{{infos.title}}</span>
              <div class="input-box">
                <Input v-model="infos.value" :placeholder="infos.tips" :maxlength="infos.max"/>
              </div>
            </div>
          </div>
        </div>
      </draggable>
      <div>
        <Modal v-model="modalPic" width="60%" scrollable footer-hide closable title='上传商品图' :mask-closable="false"
               :z-index="1">
          <uploadPictures :isChoice="isChoice" @getPic="getPic" :gridBtn="gridBtn" :gridPic="gridPic"
                          v-if="modalPic"></uploadPictures>
        </Modal>
      </div>
    </div>
    <template v-if="configData.list">
      <div class="add-btn" v-if="configData.list.length < configData.maxList">
        <Button type="primary" ghost style="width: 100%; height: 40px; border-color:#1890FF; color: #1890FF;"
                @click="addBox">添加板块
        </Button>
      </div>
    </template>
  </div>
</template>

<script>
    import vuedraggable from 'vuedraggable'
    import uploadPictures from '@/components/uploadPictures';
    import Setting from '@/setting';

    export default {
        name: 'c_menu_list_video',
        props: {
            configObj: {
                type: Object
            },
            configNme: {
                type: String
            },
            index: {
                type: null
            }
        },
        components: {
            draggable: vuedraggable,
            uploadPictures
        },
        data () {
            return {
                fileUrl: Setting.apiBaseURL + '/index.php?module=system&action=UploadMedia&group_id=0&m=Videoupload',
                defaults: {},
                configData: {},
                menus: [],
                uploadData: {}, // 上传参数
                header: {},
                list: [
                    {
                        title: 'aa',
                        val: ''
                    }
                ],
                modalPic: false,
                isChoice: '单选',
                gridBtn: {
                    xl: 4,
                    lg: 8,
                    md: 8,
                    sm: 8,
                    xs: 8
                },
                gridPic: {
                    xl: 6,
                    lg: 8,
                    md: 12,
                    sm: 12,
                    xs: 12
                },
                activeIndex: 0,
                lastObj: {}
            }
        },
        mounted () {
            this.$nextTick(() => {
                this.defaults = this.configObj
                // this.configData = this.configObj[this.configNme]
                if( this.configObj[this.configNme]){
                  this.configData = this.configObj[this.configNme]
                }else{
                  this.configData = this.defaults[this.defaults.link_key][this.configNme]
                }
            })
        },
        watch: {
            configObj: {
                handler (nVal, oVal) {
                    this.defaults = nVal
                    // this.configData = nVal[this.configNme]
                    console.log('configObjconfigObjconfigObj',this.defaults)
                    if(!this.configObj){
                      this.configData ={}
                      return
                    }
                    if( this.configObj[this.configNme]){
                      this.configData = nVal[this.configNme]
                    }else{
                      this.configData = nVal[nVal.link_key][this.configNme]
                    }
                },
                deep: true
            }
        },
        methods: {
            // 上传成功
            handleSuccess (res, file) {
                console.log(res);
                console.log(file);
                if (res.error === 0) {
                    this.configData.list[0].img = Setting.apiBaseURL + '/' + res.url
                    this.succesMsg('上传成功');
                    // this.getFileList();
                } else {
                    this.errorMsg(res.message);
                }
            },
            // 上传之前
            beforeUpload () {
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
            addBox () {
                if (this.configData.list.length == 0) {
                    this.configData.list.push(this.lastObj)
                } else {
                    const obj = JSON.parse(JSON.stringify(this.configData.list[this.configData.list.length - 1]))
                    this.configData.list.push(obj)
                }
            },
            // 点击图文封面
            modalPicTap (title, index) {
                this.activeIndex = index
                this.modalPic = true;
            },
            // 添加自定义弹窗
            addCustomDialog (editorId) {
                window.UE.registerUI('test-dialog', function (editor, uiName) {
                    const dialog = new window.UE.ui.Dialog({
                        iframeUrl: '/admin/widget.images/index.html?fodder=dialog',
                        editor: editor,
                        name: uiName,
                        title: '上传图片',
                        cssRules: 'width:1200px;height:500px;padding:20px;'
                    });
                    this.dialog = dialog;
                    // 参考上面的自定义按钮
                    var btn = new window.UE.ui.Button({
                        name: 'dialog-button',
                        title: '上传图片',
                        cssRules: 'background-image: url(../../../assets/images/icons.png);background-position: -726px -77px;',
                        onclick: function () {
                            // 渲染dialog
                            dialog.render();
                            dialog.open();
                        }
                    });

                    return btn;
                }, 37);
            },
            // 获取图片信息
            getPic (pc) {
                this.$nextTick(() => {
                    this.configData.list[this.activeIndex].img = pc.att_dir;
                    this.modalPic = false;
                })
            },
            // 删除
            bindDelete (item, index) {
                if (this.configData.list.length == 1) {
                    this.lastObj = this.configData.list[0]
                }
                this.configData.list.splice(index, 1)
            }
        }
    }
</script>

<style scoped lang="stylus">
  .img-box >>> .ivu-upload-select {
    width: 100%
    height: 100%
  }
  .hot_imgs
    margin-bottom 20px
    border-top 1px solid rgba(0, 0, 0, 0.05)

    .title
      padding 13px 0
      color #999
      font-size 12px
      border-bottom 1px solid rgba(0, 0, 0, 0.05)

    .list-box
      .item
        position relative
        display flex
        margin-top 20px

        .move-icon
          display flex
          align-items center
          justify-content center
          width 30px
          height 80px
          cursor move

        .img-box
          position relative
          width 80px
          height 80px

          video
            width 100%
            height 100%

        .info
          flex 1
          margin-left 22px

          .info-item
            display flex
            align-items center
            margin-bottom 10px

            span
              width 40px
              font-size 13px

            .input-box
              flex 1

        .delect-btn
          position absolute
          right: -10px;
          top: -15px;

          .iconfont-diy
            font-size 22px
            color #999

    .add-btn
      margin-top 10px

  .upload-box
    display flex
    align-items center
    justify-content center
    width 100%
    height 100%
    border-radius: 4px
    background: #f2f2f2

  .iconfont-diy
    color #DDDDDD
    font-size 28px
</style>
