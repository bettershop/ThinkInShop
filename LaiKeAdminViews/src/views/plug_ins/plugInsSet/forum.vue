<template>
  <div class="container">
    <el-form
      ref="ruleForm"
      class="form-search"
      :rules="rules"
      :model="ruleForm"

    >
      <div class="basic-info">
        <div class="header">
          <span>{{ $t("distribution.distributionSet.jcsz") }}</span>
        </div>
        <div class="basic-block">
          <el-form-item :label="$t('plugInsSet.groupSetInfo.yhdrksz')">
            <el-switch
              v-model="ruleForm.is_status"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
            <div class="tip">
              <p>{{ $t('plugInsSet.discountInfo.one') }}</p>
              <p class="sb_font">1.首页-金刚区 [种草]</p>
              <p class="sb_font">2.我的-功能中心 [我的种草]</p>
            </div>
          </el-form-item>
        </div>
      </div>
      <div class="basic-info">
        <div class="header">
          <span>腾讯云视频设置</span>
        </div>
        <div class="basic-block">
          <el-form-item label="总控制台-访问管理-访问密钥-APPID" prop="appid" >
            <el-input
              style="width: 100%"
              placeholder="请输入appid"
              v-model="ruleForm.appid"
            >
            </el-input>
          </el-form-item>
          <el-form-item label="总控制台-访问管理-访问密钥-Secretld" prop="secret_id" >
            <el-input
              style="width: 100%"
              placeholder="请输入密钥id"
              v-model="ruleForm.secret_id"
            >
            </el-input>
          </el-form-item>
          <el-form-item label="总控制台-访问管理-访问密钥-SecretKey" prop="secret_key" >
            <el-input
              style="width: 100%"
              placeholder="请输入密钥"
              v-model="ruleForm.secret_key"
            >
            </el-input>
          </el-form-item>
        </div>
        <div class="basic-block">
         <el-form-item label="云点播控制台应用-模板设置-视频转码模板" prop="definition_template_id" >
            <el-select
              v-model="ruleForm.definition_template_id"
              placeholder="请输入转码模板id"
              class="select-input"
              style="width: 100%"
              >
                <el-option
                  v-for="item in definitions"
                  :key="item.value"
                  :label="item.text"
                  :value="item.value">
                </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="云点播控制台应用-模板设置-视频采样截图模板id" prop="sample_template_id" >
            <el-input
              style="width: 100%"
              placeholder="请输入采样截图模板id"
              v-model="ruleForm.sample_template_id"
            >
            </el-input>
          </el-form-item>
          <el-form-item label="云点播控制台应用-分发播放设置-域名管理-Key 防盗链" prop="security_key" >
            <el-input
              style="width: 100%"
              placeholder="请输入防盗链 key"
              v-model="ruleForm.security_key"
            >
            </el-input>
          </el-form-item>
        </div>
      <div class="basic-block">

          <el-form-item label="云点播控制台应用-上传存储设置-存储地域" prop="region" >
            <el-select
              v-model="ruleForm.region"
              placeholder="请输入区域存储"
              multiple
              class="select-input"
              style="width: 100%"
              @change="setRegion"
              >
                <el-option
                  v-for="item in regions"
                  :key="item.value"
                  :label="item.text"
                  :value="item.value">
                </el-option>
              </el-select>

          </el-form-item>
          <el-form-item label="云点播控制台应用-回调设置-回调URL" prop="notify_url" >
            <el-input
              style="width: 100%"
              placeholder="请输入回调地址"
              v-model="ruleForm.notify_url"
            >
            </el-input>
          </el-form-item>
          <el-form-item label="云点播控制台-License管理-SDK License" prop="license_url" >
            <el-input
              style="width: 100%"
              placeholder="请输入许可证url"
              v-model="ruleForm.license_url"
            >
            </el-input>
          </el-form-item>
        </div>
      <div class="basic-block" >
          <el-form-item label="过期时间(h)" prop="expire_time" >
            <el-input
              style="width: 100%"
              placeholder="请输入过期时间"
              v-model="ruleForm.expire_time"
              @input="handleNumberInput"
            >
            </el-input>
          </el-form-item>
          <el-form-item label="是否开启cdn" prop="is_cdn" >
            <el-select
              v-model="ruleForm.is_cdn"
              placeholder="请选择"
              class="select-input"
              style="width: 100%"
              >
              <el-option
                label="是"
                :value="1"
              ></el-option>
              <el-option
                label="否"
                :value="0"
              ></el-option>
              </el-select>

          </el-form-item>
          <el-form-item label="" prop="cdn_url" v-if="ruleForm.is_cdn">
            <div>cdn域名播放地址</div>
            <el-input
              style="width: 100%"
              placeholder="请输入cdn域名播放地址"
              v-model="ruleForm.cdn_url"
            >
            </el-input>
          </el-form-item>
        </div>
      </div>
      <div class="basic-info" >
        <div class="header">
          <span>长文章主图模板</span>
          <el-button style="height: 40px;" @click="dialogVisible = true ">添加模板</el-button>
        </div>
        <div class="content">
            <el-table   :header-cell-style="{ 'text-align': 'center' }"
              :cell-style="{ 'text-align': 'center' }" :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%">
              <el-table-column prop="" :label="$t('template.playlist.xh')">
                <template slot-scope="scope">
                  <span>{{ scope.$index + 1 }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="模板名称">
              </el-table-column>
              <el-table-column prop="icon" label="模板图标">
                <template slot-scope="scope">
                  <img style="width: 80px;height: 80px;" :src="scope.row.icon" alt="">
                </template>
              </el-table-column>
              <el-table-column prop="img" label="模板主图">
                <template slot-scope="scope">
                  <img style="width: 80px;height: 80px;" :src="scope.row.img" alt="">
                </template>
              </el-table-column>
              <el-table-column prop="status" label="是否启用">
                <template slot-scope="scope">
                  <el-switch
                    v-model="scope.row.status"
                    active-color="#13ce66"
                    @change="edit(scope.row)"
                    :active-value="1"
                    :inactive-value="0"
                  >
                  </el-switch>
                </template>
              </el-table-column>
              <el-table-column prop="create_time" label="添加时间">
              </el-table-column>
              <el-table-column  :label="$t('zdata.cz')" width="200">
                <template slot-scope="scope">
                  <div class="OP-button">
                    <div class="OP-button-top">
                      <div class="flex_div">
                        <el-button @click="editInfo(scope.row)">{{ $t('group.groupGoods.bj') }}</el-button>
                        <el-button @click="Delete(scope.row)">{{ $t('group.groupGoods.sc') }}</el-button>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
        </div>
      </div>
      <div style="min-height: 80px"></div>
      <div class="footer-button">
        <el-button
          plain
          class="footer-cancel fontColor"
          @click="back"
          >{{ $t("DemoPage.tableFromPage.cancel") }}</el-button
        >
        <el-button
          type="primary"
          class="footer-save bgColor mgleft"
          @click="submitForm('ruleForm')"
          >{{ $t("DemoPage.tableFromPage.save") }}</el-button
        >
      </div>

    </el-form>
    <div class="dialog-refuse">
      <el-dialog :title="id?'编辑':'新增'" :visible.sync="dialogVisible" @close="cancel()">
        <el-form label-width="80px" class="demo-ruleForm">
          <div class="pass-input">
            <el-form-item label="模板名称" prop="name">
              <div class="input-box">
                <el-input v-model="templateInfo.name" size="medium" :maxlength="4"  class="Search-input" placeholder="请输入话题名称"></el-input>
                <div class="input-text">最多4个字</div>
              </div>
            </el-form-item>
            <el-form-item label="模板图标" prop="icon">
              <div class="upload-box">
                  <el-upload accept="image/*" :class="
                      templateInfo.icon == ''
                        ? 'avatar-uploader'
                        : 'avatar-uploader-two'
                    " :action="actionUrl" :data="uploadData" :show-file-list="false" :on-success="handleAvatarIconSuccess"
                    :before-upload="beforeAvatarUpload">
                    <img v-if="templateInfo.icon" :src="templateInfo.icon" class="avatar" />
                    <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                    <div class="city-list" @click.stop.prevent="removeIcons">
                      <div class="avatar-flex">
                        <i class="el-icon-delete"></i>
                      </div>
                    </div>
                  </el-upload>
                  <div class="upload-text">最多上传一张，建议上传50*50尺寸的图片</div>
              </div>
            </el-form-item>
            <el-form-item label="模板主图" prop="img">
                  <div class="upload-box">
                   <el-upload accept="image/*" :class="
                      templateInfo.img == ''
                        ? 'avatar-uploader'
                        : 'avatar-uploader-two'
                    " :action="actionUrl" :data="uploadData" :show-file-list="false" :on-success="handleAvatarImgSuccess"
                    :before-upload="beforeAvatarUpload">
                    <img v-if="templateInfo.img" :src="templateInfo.img" class="avatar" />
                    <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                    <div class="city-list" @click.stop.prevent="removeImgs">
                      <div class="avatar-flex">
                        <i class="el-icon-delete"></i>
                      </div>
                    </div>
                  </el-upload>
                  <div class="upload-text">最多上传一张，建议上传200*200px尺寸的图片</div>
              </div>
            </el-form-item>
          </div>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bdColor left_kep" @click="cancel()" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
            <el-button class="bgColor" type="primary" @click="submitTemplate()">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
          </el-form-item>
        </div>
        </el-form>
      </el-dialog>
    </div>

  </div>
</template>
<script>
import { getDiscount, setDiscount } from "@/api/plug_ins/coupons";

import {
  getList,
  submit,
  operation
} from '@/api/plug_ins/tieba'
import Config from "@/packages/apis/Config";
  import {
    getStorage,
  } from "@/utils/storage";
import { dictionaryList } from '@/api/Platform/numerical'
export default {
  name: "",
  props: {},
  components: {},
  data() {
    return {
      loading:false,
      ruleForm: {
        is_status: 1,
        secret_key: "",
        secret_id: "",
        appid: "",
        expire_time: "",
        notify_url: "",
        is_cdn: "",
        definition_template_id: "",
        sample_template_id: 10,
        region: [],
        security_key: "",
        cdn_url: "",
      },
      dialogVisible:false,
      tableData:[],
      templateInfo:{
        name:'',
        icon:'',
        img:'',
      },
      rules: {
        name: [
          {
            required: true,
            message: "请输入模板名称",
            trigger: "blur",
          },
        ],
        icon: [
          {
            required: true,
            message: "请上传模板图标",
            trigger: "blur",
          },
        ],
        img: [
          {
            required: true,
            message: "请上传模板主图",
            trigger: "blur",
          },
        ],
      },
      rules: {
        secret_key: [
          {
            required: true,
            message: "请输入密钥",
            trigger: "blur",
          },
        ],
        secret_id: [
          {
            required: true,
            message: "请输入密钥id",
            trigger: "blur",
          },
        ],
        appid: [
          {
            required: true,
            message: "请输入appid",
            trigger: "blur",
          },
        ],
        expire_time: [
          {
            required: true,
            message: "请输入过期时间",
            trigger: "blur",
          },
        ],
        notify_url: [
          {
            required: true,
            message: "请输入回调地址",
            trigger: "blur",
          },
        ],
        is_cdn: [
          {
            required: true,
            message: "请选择是否开启cdn",
            trigger: "blur",
          },
        ],
        definition_template_id: [
          {
            required: true,
            message: "请输入转码模板id",
            trigger: "blur",
          },
        ],
        sample_template_id: [
          {
            required: true,
            message: "请输入采样截图模板id",
            trigger: "blur",
          },
        ],
        region: [
          {
            required: true,
            message: "请输入区域存储",
            trigger: "blur",
          },
        ],
        security_key: [
          {
            required: true,
            message: "请输入防盗链 key",
            trigger: "blur",
          },
        ],
      },
      flag: true,
      actionUrl: Config.baseUrl,
      id:'',
      definitions:[],
      regions:[]
    };
  },
  computed: {
    uploadData() {
      {
        return {
          api: "resources.file.uploadFiles",
          storeId: getStorage("laike_admin_userInfo").storeId,
          groupId: -1,
          uploadType: 2,
          accessId: this.$store.getters.token,
          coverage :1
        };
      }
    },
  },
  watch: {},
  created() {
    this.getDictionaryList()
    this.handleGetDiscount();
    this.getTableData()

  },
  mounted() {},
  beforeRouteLeave (to, from, next) {
    if (JSON.stringify(this.ruleForm) == sessionStorage.getItem('ruleForm_discount')) {
      next()
    } else {
      console.log('表单变化，询问是否保存')
      next(false)
      this.$confirm(
        this.$t('coupons.couponsSet.sjygx'),
        this.$t('coupons.ts'),
        {
          distinguishCancelAndClose: true,
          confirmButtonText: this.$t('coupons.okk'),
          cancelButtonText: this.$t('coupons.ccel'),
          type: 'warning'
        }
      )
        .then(() => {
          // this.submitForm()
          next()
        })
        .catch(() => {
          // next()
          // next('/plug_ins/plugInsSet/plugInsList')
        })
    }
  },
  methods: {
      handleNumberInput(val) {
        // 1. 过滤非数字字符
        let numVal = val.replace(/\D/g, ''); 
        if (numVal.length > 1 && numVal.startsWith('0')) {
          numVal = numVal.replace(/^0+/, '');
        }
        this.ruleForm.expire_time = numVal;
      },
      cancel(){
        this.dialogVisible = false
        this.templateInfo = {
            name:'',
            icon:'',
            img:'',
            status:1,
            id:''
        }
      },
      async getDictionaryList() {
        const res = await dictionaryList({
          api: 'plugin.bbs.Adminbbs.getDictData',
          storeType: 8,
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
        })
        this.definitions = res.data.data.definitions
        this.regions = res.data.data.regions
      },
      beforeAvatarUpload(file) {
        // const isJPG = file.type === 'image/jpeg';
        const isLt2M = file.size / 1024 / 1024 < 2;

        // if (!isJPG) {
        //   this.errorMsg('上传头像图片只能是 JPG 格式!');
        // }
        if (!isLt2M) {
          this.errorMsg("上传头像图片大小不能超过 2MB!");
        }
        return isLt2M;
      },
      handleAvatarImgSuccess(res, file) {
        console.log(res,'resresres')
        if(res.code != 200){
          this.errorMsg(res.message)
          return
        }
        this.templateInfo.img = res.data.imgUrls[0];
      },
      handleAvatarIconSuccess(res, file) {
        console.log(res,'resresres')
        if(res.code != 200){
          this.errorMsg(res.message)
          return
        }
        this.templateInfo.icon = res.data.imgUrls[0];
      },
      removeImgs() {
        this.templateInfo.img = ''
      },
      removeIcons(){
        this.templateInfo.icon = ''
      },

   async submitTemplate(){
      operation({
        api: "plugin.bbs.AdminTemplate.addOrUpdate",
        name:this.templateInfo.name,
        icon:this.templateInfo.icon,
        img:this.templateInfo.img,
        status:this.templateInfo.status,
        id:this.templateInfo.id || '',
      }).then(res => {
        if (res.data.code == '200') {
          this.getTableData()
          this.dialogVisible = false
          this.$message({
            type: 'success',
            message: this.$t('tieaba.article.czcg'),
            offset: 100
          })
          this.templateInfo = {
            name:'',
            icon:'',
            img:'',
            status:1,
            id:''
          }
        }
      })
    },
    async add(){
      let res = await submit({
        api: "plugin.bbs.AdminTemplate.addOrUpdate",
        name:this.templateInfo.name,
        icon:this.templateInfo.icon,
        img:this.templateInfo.img
      });
      if (res.data.code == 200) {
        this.dialogVisible = false
        this.templateInfo = {
            name:'',
            icon:'',
            img:'',
            status:1,
            id:''
        }
        this.getTableData()
      }
    },
    back () {
      this.$router.push({
        path: '/plug_ins/plugInsSet/plugInsList'
      })
    },

    edit(row) {
      operation({
        api: "plugin.bbs.AdminTemplate.addOrUpdate",
        name:row.name,
        icon:row.icon,
        img:row.img,
        status:row.status,
        id:row.id
      }).then(res => {
        if (res.data.code == '200') {
          this.getTableData()
          this.templateInfo = {
            name:'',
            icon:'',
            img:'',
            status:1,
            id:''
          }
          this.$message({
            type: 'success',
            message: this.$t('tieaba.article.czcg'),
            offset: 100
          })
        }
      })
    },
    editInfo(row){
      this.dialogVisible = true
      this.templateInfo = row
    },
    Delete(row) {
      this.$confirm('确认要删除吗?', this.$t('zdata.ts'), {
        confirmButtonText: this.$t('zdata.ok'),
        cancelButtonText: this.$t('zdata.off'),
        type: 'warning'
      })
        .then(() => {
          operation({
            api: 'plugin.bbs.AdminTemplate.del',
            ids: row.id
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.getTableData()
              this.$message({
                type: 'success',
                message: this.$t('tieaba.article.czcg'),
                offset: 100
              })
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('group.yqxsc'),
            offset: 100
          })
        })
    },

    async handleGetDiscount() {
      const res = await getDiscount({
        api: "plugin.bbs.Adminbbs.getconfig",
      });

      if (res.data.code == 200) {
        res.data.data.list.definition_template_id = res.data.data.list.definition_template_id.toString()
        this.ruleForm = { ...this.ruleForm, ...res.data.data.list}
        this.ruleForm.is_status = res.data.data.list.is_status
      }
      
    },
    setRegion(e){
      console.log(this.ruleForm.region,'regionregion')
    },
    async getTableData() {
      const res = await getList({
        api: "plugin.bbs.AdminTemplate.index",
      });
       if (res.data.code == 200) {
        this.tableData = res.data.data.list
      }
    },
    async submitForm() {
      this.$refs.ruleForm.validate(async (valid) => {
        if (valid) {
          if (this.ruleForm.goodSwitch == 1) {
            if (
              this.ruleForm.auto_good_comment_day == "" ||
              this.ruleForm.auto_good_comment_content == ""
            ) {
              this.$message({
              type: "warning",
              message:this.$t("plugInsSet.groupSetInfo.kqhpszbnwk"),
              offset: 102,
            });
              return;
            }
          }
          if (!this.flag) {
            return
          }
          this.flag = false
          this.ruleForm.region = this.ruleForm.region.join(',')
          const res = await setDiscount({
            api: "plugin.bbs.Adminbbs.setconfig",
            ...this.ruleForm,
          }).finally(()=>{
            setTimeout(()=>{
              this.flag = true
            },1500)
          })
          if (res.data.code == 200) {
            this.$message({
              type: "success",
              message: this.$t("zdata.baccg"),
              offset: 102,
            });
            sessionStorage.setItem('ruleForm_discount', JSON.stringify(this.ruleForm))
            setTimeout(() => {
              this.$router.push({
                    path: '/plug_ins/plugInsSet/plugInsList'
                  })
            }, 1000);
          }
        }
      });
    },
  },
};
</script>
<style scoped lang="less">
@import "../../../webManage/css/plug_ins/plugInsSet/forum.less";
.basic-block{
  width: 100%;
  display: flex;
  /deep/.el-form-item{
    width: 32%;
    margin-right: 2%;
  }
  /deep/.el-form-item:nth-child(3){
    margin-right: 0;
  }

}
.dialog-refuse {
  .upload-box{
    display: flex;
    align-items: center;
    .upload-text{
      margin-left: 8px;
      color: rgb(182, 180, 180);
    }
  }
  .input-box{
    display: flex;
    align-items: center;
    .Search-input{
      width: 80% !important;
    }
    .input-text{
      margin-left: 8px;
      color: rgb(182, 180, 180);
    }
  }
  /* 弹框样式 */
  /deep/.el-dialog {
    width: 580px;
    min-height: 450px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    margin: 0 !important;
    .el-dialog__header {
      width: 100%;
      height: 58px;
      line-height: 58px;
      font-size: 16px;
      margin-left: 19px;
      font-weight: bold;
      border-bottom: 1px solid #e9ecef;
      box-sizing: border-box;
      margin: 0;
      padding: 0 0 0 19px;
      .el-dialog__headerbtn {
        font-size: 18px;
        top: 0 !important;
      }
      .el-dialog__title {
        font-weight: normal;
        font-size: 16px;
        color: #414658;
      }
    }

    .el-dialog__body {
      padding: 41px 60px 16px 60px !important;
      .pass-input {
        /deep/.demo-ruleForm {
          width: 340px;
        }
        .el-textarea {
          width: 340px;
          max-height: 193px;
          border-radius: 4px;
          textarea {
            width: 340px;
            max-height: 193px !important;
            border-radius: 4px;
            padding-top: 9px;
          }
        }
      }
      .form-footer {
        width: 100%;
        height: 72px;
        position: absolute;
        bottom: 0;
        right: 0;
        display: flex;
        justify-content: flex-end;
        padding-right: 20px;
        border-top: 1px solid #e9ecef;
        .el-form-item {
          padding: 0 !important;
          height: 100%;
          .el-form-item__content {
            height: 100%;
            line-height: 72px;
            margin: 0 !important;
          }
        }

        .qxcolor {
          color: #6a7076;
          border: 1px solid #d5dbc6;
        }
        .qdcolor {
          background-color: #2890ff;
        }
        .qdcolor {
          background-color: #2890ff;
        }
        .qdcolor:hover {
          opacity: 0.8;
        }
        .qxcolor {
          color: #6a7076;
          border: 1px solid #d5dbe8;
          // margin-left: 14px;
        }
        .qxcolor:hover {
          color: #2890ff;
          border: 1px solid #2890ff;
          background-color: #fff;
        }
      }
    }
  }
  /deep/.el-form-item__label {
    font-weight: normal;
    color: #414658;
  }
}
  .avatar-uploader-two {
    height: 80px;
    width: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #c0ccda;
    position: relative;
  }

  .city-list {
    display: none;
    position: absolute;
    top: 0;
    left: 0;
    color: #fff;
    font-size: 19px;
    width: 100%;
    height: 100%;
    opacity: 1;
    font-size: 1.25rem;
    background-color: rgba(0, 0, 0, 0.5);
    transition: opacity 0.3s;
  }

  .avatar-uploader-two:hover {
    border: 1px solid #c0ccda;
    cursor: pointer;
  }

  .avatar-uploader-two:hover .city-list {
    display: block;
  }

  .avatar-flex {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 70px;
  }
 .avatar-uploader {
    height: 80px;
    width: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px dashed #c0ccda;
  }

  .avatar-uploader:hover {
    border: 0.0625rem dashed #409eef;
    cursor: pointer;
  }
  .avatar-uploader .el-upload {
    cursor: pointer;
    position: relative;
  }
  .avatar-uploader .el-upload:hover {
    border-color: #409eff;
  }
  .avatar-uploader-icon {
    font-size: 20px;
    color: #8c939d;
    width: 80px;
    text-align: center;
  }
  .avatar {
    width: 80px;
    height: 80px;
    display: block;
  }
  .removeImg {
    position: absolute;
    right: 0;
    top: 0;
  }
</style>
