<template>
  <div class="dialog">
    <el-dialog
      :title="$t('uploadImage.xqwj')"
      width="70%"
      :visible.sync="dialogVisible"
      @close="$emit('close')"
      :modal-append-to-body='false'
      :modal="mask_layer"
    >
      <el-container class="container">
        <!--        左侧侧边栏-->
        <dialog-left-aside
          @changeType="containerType = $event"
          @changeGroup="changeGroupId"
          ref="dialogLeftAside"
        ></dialog-left-aside>
        <!--        从URL添加-->
        <el-container v-if="containerType === 0" class="url-add-container">
          <el-main>
            <el-row>
              <el-col :span="14" :offset="1">
                <el-form
                  :model="ruleForm"
                  :rules="rules"
                  ref="ruleForm"
                  label-width="80px"
                  class="url-form"
                >
                  <el-form-item label="URL" class="dialog-upimg imgUrl" prop="url">
                    <el-input
                      v-model="ruleForm.url"
                      placeholder="http://"
                    ></el-input>
                  </el-form-item>
                  <el-form-item :label="$t('uploadImage.ljwb')" class="dialog-upimg" prop="text">
                    <el-input v-model="ruleForm.text"></el-input>
                  </el-form-item>
                </el-form>
              </el-col>
            </el-row>
          </el-main>
          <el-footer height="72px">
            <el-button type="primary" @click="onHandleAddUrl"
              >{{ $t('uploadImage.qrtj') }}</el-button
            >
          </el-footer>
        </el-container>
        <!--        上传图片和媒体库-->
        <dialog-container
          v-else
          :groupId="groupId"
          v-on="$listeners"
        ></dialog-container>
      </el-container>
    </el-dialog>
    <!-- <div class="model" v-if="dialogVisible == true" @click="dialogVisible = false"></div> -->
  </div>
</template>

<script>
import dialogLeftAside from "./components/dialog-left-aside";
import dialogContainer from "./components/dialog-container";
import { getStorage, setStorage } from "@/utils/storage";

import Upload from "@/packages/apis/Upload";

let api = new Upload();

export default {
  name: "u-dialog",
  components: { dialogLeftAside, dialogContainer },
  props: {
    mask_layer: {
      type: Boolean,
      default: true
    },
  },
  data() {
    return {
      dialogVisible: true,//弹开显示
      containerType: 1, // 0 从URL添加，1上传图片和媒体库
      ruleForm: {//查询条件
        url: "",
        text: ""
      },
      rules: {
        url: [
          { required: true, message: this.$t('uploadImage.qsrurl'), trigger: "blur" }
        ]
      },
      groupId: -1 // 分组id
    };
  },
  created() {},
  watch: {
    dialogVisible() {}
  },
  methods: {
    //切换分组
    changeGroupId(groupId) {
      if (groupId) {
        this.groupId = Number.parseInt(groupId);
      } else {
        this.groupId = -1;
      }
    },
    //添加图片
    onHandleAddUrl() {
      if(!this.ruleForm.url) {
        this.errorMsg(this.$t('uploadImage.qsrurl'))
        return
      }
      this.$refs.ruleForm.validate(async valid => {
        if (!valid) return false;
        let res = await api.createByURL(this.ruleForm.url, this.ruleForm.text,this.$route.path.split('/')[1] == 'Platform' ? 0 : getStorage('laike_admin_userInfo').storeId);
        console.log(res)
        if (res.data.code == 200) {
          this.$message({
            message: this.$t('zdata.tjcg'),
            type: "success",
            offset: 102,
          });
          if (res.data.url) {
            this.$emit("getPic", {
              url: res.data.url
            });
          }
          // await this.$refs.dialogLeftAside.onHandleClassifySelect(-1)
          // this.dialogVisible = false;
        } else {
          this.errorMsg(res.message);
        }
      });
    }
  }
};
</script>

<style scoped lang="less">
.dialog {
  .model {
    position: fixed;
    width: 100vw;
    height: 100vh;
    left: 0;
    top: 0;
    transform: translate(-50%,-50%);
    opacity: .5;
    background: #000;
    z-index: 999999999;
  }

  /deep/ .el-dialog {
    /* 避免太贴顶 */
    top: 10vh;
    margin: auto !important;
    min-width: 1200px;
    /* 锁死弹窗总高度 */
    height: 65vh !important;
    /* 防极小屏幕 */
    max-height: 90vh;
    /* 让内部用 flex 布局撑满 */
    display: flex;
    flex-direction: column;
  }

  /deep/ .el-dialog__header {
    display: flex;
    align-items: center;
    border-bottom: 1px solid #d5dbe8;
    padding: 0 20px;
    height: 60px;
    .el-dialog__headerbtn {
      top: 0;
      height: 60px;
      line-height: 60px;
    }
  }

  /deep/ .el-dialog__body {
    padding: 0;
    /* 占满剩余空间 */
    flex: 1;
    /* 内容可滚 */
    overflow-y: auto !important;
    overflow-x: hidden;
    /* 防止 flex 子项撑破 */
    min-height: 0;
  }

  .container {
    background: #edf1f5;
    /* 继承弹窗高度 */
    height: 100%;
    display: flex;
    /* 左右布局 */
    flex-direction: row;
    //height: 600px;
    width: 100%;
    .url-add-container {
      background: #fff;
      /deep/ .el-footer {
        border-top: 1px solid #e9ecef;
        display: flex;
        align-items: center;
        flex-direction: row-reverse;
      }
    }
  }

  /deep/.url-form {
    .el-form-item {
      margin-bottom: 20px;
    }
  }
}
</style>
