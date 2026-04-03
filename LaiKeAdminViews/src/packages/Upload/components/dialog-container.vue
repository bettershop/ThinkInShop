<template>
  <el-container class="dialog-container">
    <el-tabs v-model="activeName" type="border-card" @tab-click="handleClick">
      <el-tab-pane
        :label="$t('uploadImage.scwj')"
        name="upload"
        class="upload-tab-pane"
      >
        <div class="upload-wrap">
          <el-upload
            class="upload-drag"
            drag
            :action="actionUrl"
            name="file"
            multiple
            :data="uploadData"
            :show-file-list="false"
            :before-upload="handleBeforeUpload"
            :on-success="handleUploadSuccess"
            :on-change="changes"
            accept="image/*,audio/*,video/*"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">
              {{ $t("uploadImage.jwjtdcc")
              }}<em>{{ $t("uploadImage.djsc") }}</em>
            </div>
            <div
              class="el-upload__tip"
              slot="tip"
              style="text-align: center"
              v-if="maxSize"
            >
              {{ $t("uploadImage.zdscwjdx") }}{{ maxSizeText }}
            </div>
          </el-upload>
        </div>
        <div class="gap"></div>
        <dialog-container-footer></dialog-container-footer>
      </el-tab-pane>
      <el-tab-pane
        :label="$t('uploadImage.mtk')"
        name="library"
        class="file-list-tab-pane"
      >
        <div class="file-list">
          <div class="file-list-left">
            <el-form
              class="file-info-form"
              :model="ruleForm2"
              :rules="rules2"
              ref="ruleForm2"
            >
              <el-row class="file-list-left-header" type="flex">
                <el-col :span="4" class="all-date dialog-upimg">
                  <el-date-picker
                    v-model="ruleForm2.time"
                    type="month"
                    @change="onTimeChange"
                    :placeholder="$t('uploadImage.qbrq')"
                  >
                  </el-date-picker>
                </el-col>

                <el-col :span="5" class="dialog-upimg">
                  <el-input
                    v-model="ruleForm2.title"
                    :placeholder="$t('uploadImage.sswjxm')"
                    @blur="queryFileList"
                  ></el-input>
                </el-col>
              </el-row>
            </el-form>

            <ul
              class="infinite-list"
              v-infinite-scroll="load"
              style="overflow: auto"
            >
              <li
                v-for="(item, index) of list"
                :key="index"
                @click="onHandleClickFileLi(index, item)"
                :class="{ active: checkedKey === index }"
              >
                <img :src="item.url" alt="" @error="handleErrorImg" />
                <el-checkbox
                  class="infinite-list-checked"
                  v-if="item.checked"
                  v-model="item.checked"
                ></el-checkbox>
              </li>
              <!--              <li v-for="i in count" class="infinite-list-item">{{ i }}</li>-->
            </ul>
          </div>

          <div class="file-list-right">
            <template v-if="imageInfo">
              <h4>
                {{ $t("uploadImage.fjxq") }}
              </h4>
              <p>{{ imageInfo.image_name }}</p>
              <div class="image-info">
                <img class="image" :src="imageInfo.url" />
                <div class="image-info-content">
                  <p>{{ imageInfo.add_time | dateFormat1 }}</p>
                  <p>{{ imageInfo.size }}</p>
                  <p>{{ imageInfo.widthAndHeight }}</p>
                  <div v-if="imageInfo.groupName">
                    <el-link
                      type="primary"
                      :underline="false"
                      @click="onMoveClassify(imageInfo.id)"
                      >{{ $t("uploadImage.yichu")
                      }}{{ imageInfo.groupName }}</el-link
                    >
                  </div>
                  <div>
                    <el-link
                      type="danger"
                      :underline="false"
                      @click="onDeleteFile(imageInfo.id)"
                      >{{ $t("uploadImage.yjsc") }}</el-link
                    >
                  </div>
                </div>
              </div>

              <el-form
                class="file-info-form"
                :model="ruleForm"
                :rules="rules"
                ref="ruleForm"
                label-position="top"
              >
                <el-form-item label="URL" prop="url">
                  <el-input v-model="ruleForm.url" :disabled="true"></el-input>
                </el-form-item>
                <el-form-item :label="$t('uploadImage.biaoti')" prop="title">
                  <el-input v-model="ruleForm.title"></el-input>
                </el-form-item>
                <el-form-item
                  :label="$t('uploadImage.shuoming')"
                  prop="explain"
                >
                  <el-input v-model="ruleForm.explain"></el-input>
                </el-form-item>

                <el-form-item
                  :label="$t('uploadImage.dtwb')"
                  prop="alternative_text"
                >
                  <el-input v-model="ruleForm.alternative_text"></el-input>
                </el-form-item>
                <el-form-item :label="$t('uploadImage.txms')" prop="describe">
                  <el-input
                    type="textarea"
                    :rows="2"
                    v-model="ruleForm.describe"
                  ></el-input>
                </el-form-item>
              </el-form>
            </template>
          </div>
        </div>

        <dialog-container-footer
          :list="list"
          :checkedKey="checkedKey"
          @move="queryFileList"
          @clear="onHandleClear"
          @add="onHandleAdd"
        ></dialog-container-footer>
      </el-tab-pane>
    </el-tabs>
  </el-container>
</template>

<script>
import dialogContainerFooter from "./dialog-container-footer";
import Config from "../../apis/Config";
import Upload from "../../apis/Upload";
import moment from "moment";
import { conver } from "../../utils/utils";
import { getStorage } from "@/utils/storage";
import ErrorImg from "@/assets/images/default_picture.png";
import axios from "axios";
import log from "@/libs/util.log";
let api = new Upload();

let actionUrl = Config.baseUrl;

export default {
  name: "dialog-container",
  inject: ["indexCom"],
  props: {
    groupId: {
      type: Number,
    },
    groupList: {
      type: Array,
      default: () => {
        return [];
      },
    },
  },
  data() {
    return {
      activeName: "library", // upload library
      actionUrl,
      list: [],
      page: 0,
      pageSize: 30,
      checkedKey: null,
      hasMore: true,
      ruleForm: {
        url: "",
        title: "",
        explain: "",
        alternative_text: "",
        describe: "",
      },
      rules: {},
      ruleForm2: {
        time: "",
        title: "",
        start_time: "",
        end_time: "",
      },
      rules2: {},
      file:'',
    };
  },
  computed: {
    uploadData() {

      let data = {
          api: "resources.file.uploadFiles",
          storeId:
            this.$route.path.split("/")[1] == "Platform"
              ? 0
              : getStorage("laike_admin_userInfo").storeId,
          // storeId:  getStorage('laike_admin_userInfo').storeId,

          groupId: this.groupId,
          // mchId: getStorage('laike_admin_userInfo').mchId,
          uploadType: 2,
          store_type: 8,
          accessId: this.$store.getters.token,
          mchId:
            this.$route.path.split("/")[1] == "Platform"
              ? ""
              : getStorage("laike_admin_userInfo").mchId,
          }
        const {path,query} = this.$route
        console.log('page',path)
        // 用于diy功能 图片上传
        if(path == '/plug_ins/template/addTemplate'){
          this.$set(data,'img_type',1)
          // 判断是否是 系统主题 0  还是 自定义主题  1
          data.diy_img_type = query.typeIndex - 1
          // 主题 id
          data.diyId = query.id
        }

      {
        return {
          ...data
        };
      }
    },
    maxSelectNum() {
      return this.indexCom.maxSelectNum;
    },
    limit() {
      return this.indexCom.limit;
    },
    maxSize() {
      return this.indexCom.maxSize;
    },
    maxSizeText() {
      return conver(this.indexCom.maxSize);
    },
  },
  asyncComputed: {
    // 异步获取图片信息
    async imageInfo() {
      if (this.checkedKey !== null) {
        if (this.list[this.checkedKey].url) {
          let res = await fetch(this.list[this.checkedKey].url).catch((err) => {
            return {
              id: this.list[this.checkedKey].id,
              image_name: this.list[this.checkedKey].name,
              url: this.list[this.checkedKey].url,
              add_time: this.list[this.checkedKey].add_time,
            };
          });
          console.log(res);
          if (res && res.status == 200) {
            let data = await res.blob();
            let size = Math.ceil(data.size / 1024);
            let { width, height } = await this.getMeta(
              this.list[this.checkedKey].url
            );
            console.log(456);
            return {
              id: this.list[this.checkedKey].id,
              image_name: this.list[this.checkedKey].name,
              url: this.list[this.checkedKey].url,
              add_time: this.list[this.checkedKey].add_time,
              size: `${size}kb`,
              widthAndHeight: `${width} x ${height}`,
            };
          } else {
            console.log(123);
            return {
              id: this.list[this.checkedKey].id,
              image_name: this.list[this.checkedKey].name,
              url: this.list[this.checkedKey].url,
              add_time: this.list[this.checkedKey].add_time,
            };
          }
        } else {
          return {
            id: this.list[this.checkedKey].id,
            image_name: this.list[this.checkedKey].name,
            url: this.list[this.checkedKey].url,
            add_time: this.list[this.checkedKey].add_time,
          };
        }
      } else {
        console.log("哈哈哈哈哈哈哈哈");
        return false;
      }
    },
  },
  watch: {
    activeName(val) {
      if (val === "library") {
        this.empty();
        this.list = [];
        this.getFileList();
      } else {
      }
    },
    async groupId() {
      this.empty();
      await this.getFileList();
    },
    checkedKey(val) {
      if (val !== null) {
        this.$set(this.ruleForm, "url", this.list[this.checkedKey].url);
        this.$set(this.ruleForm, "id", this.list[this.checkedKey].id);
        this.$set(this.ruleForm, "title", this.list[this.checkedKey].title);
        this.$set(this.ruleForm, "explain", this.list[this.checkedKey].explain);
        this.$set(
          this.ruleForm,
          "alternative_text",
          this.list[this.checkedKey].alternative_text
        );
        this.$set(
          this.ruleForm,
          "describe",
          this.list[this.checkedKey].describe
        );
      } else {
      }
    },
  },
  components: { dialogContainerFooter },
  mounted() {
    // this.getFileList();
  },
  methods: {
    // 图片错误处理
    handleErrorImg(e) {
      console.log("图片报错了", e.target.src);
      e.target.src = ErrorImg;
    },
    async onHandleAdd() {
      let checkedList = this.list.filter((item) => item.checked);
      console.log("checkedList", checkedList);
      console.log("maxSelectNum", this.maxSelectNum);
      if (checkedList.length > this.maxSelectNum) {
        this.errorMsg(this.$t("uploadImage.cgldsxsl"));
        return false;
      }

      if (this.limit === 1) {
        this.$emit("getPic", {
          url: this.list.filter((item) => item.checked)[0].url,
          image_name: this.list.filter((item) => item.checked)[0].image_name,
        });
      } else {
        this.$emit(
          "getPicD",
          this.list.filter((item) => item.checked)
        );
      }
      await api
        .modifyInfo({
          id: this.list[this.checkedKey].id,
          groupId: this.groupId,
          title: this.ruleForm.title,
          explain: this.ruleForm.explain,
          alternative_text: this.ruleForm.alternative_text,
          describe: this.ruleForm.describe,
        })
        .then((res) => {});
    },
    onHandleClear() {
      this.checkedKey = null;
      this.list = this.list.map((item) => {
        item.checked = false;
        return item;
      });
    },
    queryFileList() {
      this.list = [];
      this.page = 1;
      this.checkedKey = null;
      this.getFileList();
    },
    onTimeChange(time) {
      let startTime = moment(time);
      let endTime = moment(time).add(1, "M").subtract(1, "d");

      this.$set(this.ruleForm2, "start_time", startTime.format("YYYY-MM-DD"));
      this.$set(this.ruleForm2, "end_time", endTime.format("YYYY-MM-DD"));
      this.queryFileList();
    },
    async editFileInfo() {
      this.$set(this.list[this.checkedKey], "title", this.ruleForm.title);
      this.$set(this.list[this.checkedKey], "explain", this.ruleForm.explain);
      this.$set(
        this.list[this.checkedKey],
        "alternative_text",
        this.ruleForm.alternative_text
      );
      this.$set(this.list[this.checkedKey], "describe", this.ruleForm.describe);

      let res = await api.editFileInfo(this.ruleForm);
    },
    // 移出分类
    async onMoveClassify(id) {
      await api.moveClassify(-1, [
        {
          id: id,
        },
      ]);
      this.succesMsg(this.$t("uploadImage.ydcg"));
      this.empty();
      await this.getFileList();
    },
    // 初始化清空数据
    empty() {
      this.list = [];
      this.page = 1;
      this.pageSize = 30;
      this.$refs.ruleForm2.resetFields();
      this.checkedKey = null;
    },
    // 永久删除图片
    async onDeleteFile(id) {
      await api.deleteFile(id).then((res) => {
        console.log(res);
        if (res.data.code == "200") {
          this.succesMsg(this.$t("zdata.sccg"));
          this.empty();
          this.getFileList();
        } else {
        }
      });
    },
    /**
     * 获取分类名称
     * @returns {Promise<null|*>}
     */
    async getClassifyName() {
      api.getClassify().then((res) => {
        let key = res.data.list.findIndex((item) => item.id == this.groupId);
        if (res.data.list[key].id === -1) {
          // 全部商品不显示移除分类按钮
          return null;
        }
        return res.data.list[key].name;
      });
    },
    async getMeta(url) {
      let img = new Image();
      img.src = url;
      return new Promise((resolve) => {
        img.onload = function () {
          return resolve({
            width: this.width,
            height: this.height,
          });
        };
      });
    },
    onHandleClickFileLi(index, item) {
      console.log(item);
      let checkedList = this.list.filter((item) => item.checked);
      if (
        checkedList.length >= this.maxSelectNum &&
        !this.list[index].checked &&
        this.limit !== 1
      ) {
        this.errorMsg(this.$t("uploadImage.cgldsxsl"));
        return false;
      }
      if (this.limit === 1) {
        this.list = this.list.map((item) => {
          item.checked = false;
          return item;
        });
        this.list[index].checked = true;
      } else {
        this.list[index].checked = !this.list[index].checked;
      }

      this.checkedKey = index;
    },
    load() {
      if (this.hasMore) {
        this.page++;
        this.getFileList();
      }
    },
    handleClick() {},
    changes() {},
    // 上传成功
    handleUploadSuccess(res) {
      console.log(res);
      if (res.code == 51073) {
        let that = this;
        console.log(this.file)
        let formData = new FormData();
        formData.append("file", this.file); //第一个file 后台接收的参数名
        this.$confirm(
          "上传的文件名称及路径重复，确认覆盖？",
          this.$t("zdata.ts"),
          {
            confirmButtonText: this.$t("zdata.ok"),
            cancelButtonText: this.$t("zdata.off"),
            type: "warning",
          }
        )
          .then(() => {
            axios({
              url: that.actionUrl, //上传路径
              method: "POST",
              params: {
                api: "admin.resources.uploadFiles",
                storeId: getStorage("laike_admin_userInfo").storeId,
                accessId: that.$store.getters.token,
                groupId: '-1',
                coverage: 1,
              },
              data:formData
            })
              .then((res) => {
                if (res.data.code == 200) {
                  that.$message({
                    type: "success",
                    message: "上传成功",
                    offset:120
                  });

                }
              })
              .catch((err) => {
                console.log(err);
              });
          })
          .catch(() => {});
      } else if (res.code != 200) {
        that.$message({
          type: "error",
          message: res.message,
          offset:120
        });
      }
      this.activeName = "library";
    },
    // 上传之前的处理
    handleBeforeUpload(file) {
      if (file.size > 5242880) {
        this.errorMsg(this.$t("uploadImage.ccscdx"));

        return false;
      }
      this.file=file
      const fileType = file.name.slice(file.name.lastIndexOf('.')+1 ,file.name.length)
      if(fileType == 'icon'){
        this.$message({
          message:"不能用ico图片",
          type:'error',
          offset:'100'
          });
        return false;
      } 
    },
    // 获取文件列表
    async getFileList() {
      let that = this;
      if (this.ruleForm2.start_time == "Invalid date") {
        this.ruleForm2.start_time = "";
      }
      if (this.ruleForm2.end_time == "Invalid date") {
        this.ruleForm2.end_time = "";
      }
      let  data= {
          page: this.page,
          pageSize: this.pageSize,
          pid: this.groupId,
          start_time: this.ruleForm2.start_time,
          end_time: this.ruleForm2.end_time,
          title: this.ruleForm2.title,
          storeId:
            that.$route.path.split("/")[1] == "Platform"
              ? 0
              : getStorage("laike_admin_userInfo").storeId,
        }

      console.log("504504504504", that.$route.path.split("/")[1]);

      const {path,query} = this.$route
        console.log('page',path)
        // 用于diy功能 图片上传
        if(path == '/plug_ins/template/addTemplate'){
          this.$set(data,'img_type',1) 
        }

      await api
        .getFileList(data)
        .then((res) => {
          if (res.data.code === "200") {
            res.data.data.list = res.data.data.list.map((item) => {
              item["checked"] = false;
              return item;
            });
            this.list = this.list.concat(res.data.data.list);
            if (res.data.data.list.length) {
              this.hasMore = true;
            } else {
              this.hasMore = false;
            }
          } else {
            this.hasMore = false;
          }
        });
    },
  },
};
</script>

<style scoped lang="less">
@import "../style/dialog-container.less";
</style>
