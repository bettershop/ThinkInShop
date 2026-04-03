<template>
  <div class="container">
    <el-form
      ref="ruleForm"
      class="form-search"
      :rules="rules"
      :model="ruleForm"
      label-width="auto"
    >
      <div class="order-info">
        <div class="header">
          <span>{{ $t("plugInsSet.liveSetInfo.zbpz") }}</span>
        </div>
        <div class="order-block">
          <el-form-item :label="$t('plugInsSet.groupSetInfo.yhdrksz')">
            <el-switch
              v-model="ruleForm.is_open"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
            <div class="tip">
              <p>{{ $t("preSale.preSaleSet.preSaleInfo.one") }}</p>
              <p>{{ $t("plugInsSet.liveSetInfo.two") }}</p>
              <p>{{ $t("plugInsSet.liveSetInfo.three") }}</p>
              <p>{{ $t("plugInsSet.liveSetInfo.four") }}</p>
            </div>
          </el-form-item>
          <el-form-item :label="'店铺端入口设置'">
            <el-switch
              v-model="ruleForm.mch_is_open"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
          </el-form-item>
          <el-form-item
            :label="$t('plugInsSet.liveSetInfo.tlurl')"
            prop="push_url"
          >
            <el-input v-model="ruleForm.push_url"> </el-input>
          </el-form-item>
          <el-form-item
            :label="$t('plugInsSet.liveSetInfo.bfurl')"
            prop="play_url"
          >
            <el-input v-model="ruleForm.play_url"> </el-input>
          </el-form-item>
          <el-form-item :label="'播放器授权信息'">
            <div class="tip">
              <el-form-item
                label="License URL"
                prop="license_url"
                style='margin:16px 8px'

              >
                <el-input :placeholder="'请输入播放器授权的URL地址'" v-model="ruleForm.license_url"> </el-input>
              </el-form-item>
              <el-form-item
                label="License Key"
                prop="license_key"
                style='margin:16px 8px'

              >
            <el-input :placeholder="'请输入播放器授权的密钥'" v-model="ruleForm.license_key"> </el-input>
          </el-form-item>
            </div>
          </el-form-item>
        </div>
      </div>
      <!-- 敏感字 -->
      <div class="order-info">
        <div class="header">
          <span>{{ $t("plugInsSet.liveSetInfo.mgzsz") }}</span>
        </div>
        <!-- 操作按钮 -->
        <div class="jump-list">
          <div>
            <el-button
              class="bgColor laiketui laiketui-add"
              type="primary"
              @click="addQualifier"
              >{{ $t('plugInsSet.liveSetInfo.addmgz') }}</el-button
            >
            <el-button
              class="bgColor laiketui laiketui-add"
              type="primary"
              @click="batchImport"
              >{{ $t('plugInsSet.liveSetInfo.mgzpldr') }}</el-button
            >
            <el-button
              class="bgColor laiketui laiketui-add"
              type="primary"
              @click="deleteItem"
              >{{ $t('plugInsSet.liveSetInfo.mgzplsc') }}</el-button
            >
          </div>
          <div class="Search-condition">
            <div class="query-input">
              <el-input v-model="mgcForm.mgcText" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('plugInsSet.liveSetInfo.qsrmgzc1')"></el-input>
              <el-button class="fontColor" @click="mgcForm.mgcText = ''">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
              <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
           </div>
        </div>
      </div>
        <div class="order-block">
          <el-table
              :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
              v-loading="loading"
              :header-cell-style="{ 'text-align': 'center' }"
              :cell-style="{ 'text-align': 'center' }"
              :data="tableData"
              @selection-change="handleSelectionChange"
              ref="table"
              class="el-table"
              style="width: 100%"
            >
              <el-table-column type="selection"> </el-table-column>
              <el-table-column
                prop="word"
                :label="$t('plugInsSet.liveSetInfo.mgc')"/>
              <el-table-column  prop="add_time" :label="$t('plugInsSet.liveSetInfo.cjrq')"/>
              <el-table-column
                :label="$t('physicalgoods.cz')"
                fixed="right"
                width="200"
              >
                <template slot-scope="scope">
                  <div class="OP-button">
                    <div class="OP-button-bottom">
                      <el-button
                        icon="el-icon-edit-outline"
                        @click="updataItem(scope.row)"
                        class="bottom-btn"
                        >{{ $t('commentList.xg') }}</el-button
                      >

                      <el-button
                        icon="el-icon-delete"
                        @click="deleteItem(scope.row)"
                        >{{ $t('physicalgoods.shanchu') }}</el-button
                      >
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
        </div>
        <div class="pageBox" ref="pageBox">
        <div class="pageLeftText">
          {{ $t('DemoPage.tableExamplePage.show') }}
        </div>
        <el-pagination
          layout="sizes, slot, prev, pager, next"
          :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
          :next-text="$t('DemoPage.tableExamplePage.next_text')"
          @size-change="handleSizeChange"
          :page-sizes="pagesizes"
          :current-page="dictionaryNum"
          @current-change="handleCurrentChange"
          :total="total"
        >
          <div class="pageRightText">
            {{ $t('DemoPage.tableExamplePage.on_show') }}{{ currpage }}-{{
              current_num
            }}{{ $t('DemoPage.tableExamplePage.twig') }}{{ total
            }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
          </div>
        </el-pagination>
      </div>
      </div>

      <div class="basic-info">
        <div class="header">
          <span>{{ $t("plugInsSet.liveSetInfo.xysz") }}</span>
        </div>
        <div class="basic-block rules-blocks">
          <!-- <el-form-item :label="$t('preSale.preSaleSet.kqcj')">
              <el-switch
                v-model="ruleForm.isOpen"
                :active-value="1"
                :inactive-value="0"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              >
              </el-switch>
            </el-form-item> -->
          <el-form-item
            :label="$t('plugInsSet.liveSetInfo.xybt')"
            prop="agree_title"
          >
            <el-input
              :placeholder="$t('plugInsSet.liveSetInfo.qsrbt')"
              v-model="ruleForm.agree_title"
            >
            </el-input>
          </el-form-item>
          <el-form-item
            :label="$t('plugInsSet.liveSetInfo.xynr')"
            prop="agree_content"
          >
            <div class="rules-block">
              <vue-editor
                v-model="ruleForm.agree_content"
                useCustomImageHandler
                @image-added="handleImageAdded"
              ></vue-editor>
            </div>
          </el-form-item>
        </div>
      </div>
      <div class="footer-button">
        <el-button plain class="footer-cancel fontColor" @click="back">{{
          $t("DemoPage.tableFromPage.cancel")
        }}</el-button>
        <el-button
          type="primary"
          class="footer-save bgColor mgleft"
          @click="submitForm('ruleForm')"
          >{{ $t("DemoPage.tableFromPage.save") }}</el-button
        >
      </div>
    </el-form>
    <!-- 添加 -->
    <el-dialog
        :title="$t('plugInsSet.liveSetInfo.addmgz')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
        width="700px"
      >
      <el-form :rules="rules" ref="mgcForm" class="mgc-form" label-width="150px" :model="addFrom">
        <el-form-item :label="$t('plugInsSet.liveSetInfo.mgc') " prop="mgcText">
          <el-input :placeholder="$t('plugInsSet.liveSetInfo.qsrmgzc')" maxlength="50"  show-word-limit v-model="addFrom.mgcText"></el-input>
        </el-form-item>
        <el-form-item >
          <div>
            <el-button @click="addQualifier">取消</el-button>
            <el-button type="primary" @click="submit" :disabled="!booleSwitch" :loading="!booleSwitch" >确定</el-button>
          </div>
          </el-form-item>
      </el-form>
    </el-dialog>
    <!-- 导入 -->
    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('plugInsSet.liveSetInfo.mgzdr')"
        :visible.sync="dialogVisible1"
      >
        <div style="text-align: center;">
            <div class="item item-center" >
              <el-upload el-upload
              class="upload-demo"
              ref="upload"
              accept=".xlsx,.xls,.csv"
              :action="actionUrlEvent()"
              name="file"
              :data="uploadData"
              :show-file-list="false"
              :before-upload="handleBeforeUpload"
                :on-success="handleUploadSuccess"
                multiple
                >
                <i class="el-icon-document-copy"></i>
              {{ $t('plugInsSet.liveSetInfo.mgzdrwb')}}
            </el-upload>
          </div>
          <div class="uploads"><a href="./sensitive_words.xlsx">{{$t('batchImport.xzpld')}}</a></div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getSecConfig, setSecConfig } from "@/api/plug_ins/preSale";
import { VueEditor } from "vue2-editor";
import OSS from "ali-oss";
import { getStorage } from "@/utils/storage";
import axios from "axios";
import Config from "@/packages/apis/Config";
let actionUrl = Config.baseUrl;

export default {
  name: "preSaleSet",
  components: {
    VueEditor,
  },
  data() {
    return {
      inOrderNum: "",
      actionUrl ,
      ruleForm: {
        // isOpen: 1,
        is_open: 0,
        mch_is_open:0,
        push_url: "",
        play_url: "",
        agree_title: "",
        agree_content: "",
        license_url:'',
        license_key:''
      },
      dialogVisible:false,
      booleSwitch: true,
      dialogVisible1:false,
      tableData:[],
      upItemId:'',
      ids:[],//批量删除
      currpage:0,
      current_num:0,
      total:0,
      dictionaryNum: 1,
      loading:false,
      addFrom:{
        mgcText:'',//添加限制词
      },
      mgcForm:{
        mgcText:'',//列表查询条件
      },
      fileName:'',
      file:'',
      tag:false,
      rules: {
        mgcText:[
          {
            required: true,
            message: this.$t('plugInsSet.liveSetInfo.qsrmgzc'),
            trigger: "blur",
          },
        ],
        push_url: [
          {
            required: true,
            message: this.$t("plugInsSet.liveSetInfo.qsrtl"),
            trigger: "blur",
          },
        ],
        play_url: [
          {
            required: true,
            message: this.$t("plugInsSet.liveSetInfo.qsrbf"),
            trigger: "blur",
          },
        ],
        license_url: [
          {
            required: true,
            message: '请输入License URL',
            trigger: "blur",
          },
        ],
        license_key: [
          {
            required: true,
            message: '请输入License Key',
            trigger: "blur",
          },
        ],
        agree_title: [
          {
            required: true,
            message: this.$t("plugInsSet.liveSetInfo.qsrbt"),
            trigger: "blur",
          },
        ],
        agree_content: [
          {
            required: true,
            message: this.$t("plugInsSet.liveSetInfo.qsrnr"),
            trigger: "blur",
          },
        ],
      },


    };
  },

  created() {
    this.getBase();
    this.getSecConfigs();
    this.inOrderNum = getStorage("inOrderNum");
    this.demand()
  },

  //保存对比是否有改动数据，有则需要提示弹窗
  beforeRouteLeave(to, from, next) {
    if (
      JSON.stringify(this.ruleForm) ==
      sessionStorage.getItem("ruleForm_preSaleSetInfo")
    ) {
      next();
    } else {
      console.log("表单变化，询问是否保存");
      next(false);
      this.$confirm(
        this.$t("coupons.couponsSet.sjygx"),
        this.$t("coupons.ts"),
        {
          distinguishCancelAndClose: true,
          confirmButtonText: this.$t("coupons.okk"),
          cancelButtonText: this.$t("coupons.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          //this.submitForm('ruleForm')
          next();
        })
        .catch(() => {
          //next()
        });
    }
  },
  computed: {
    // 导入文件 接口
    uploadData() {
      {
        const userInfo = getStorage('laike_admin_userInfo')
        return {
          //商城id
          // storeId: userInfo.storeId,
          //来源
          // storeType: 8,
          api: "admin.plugin.manage.addSensitives"
        };
      }
    }
  },
  methods: {
    actionUrlEvent(){
       const userInfo = getStorage('laike_admin_userInfo')
       const accessid = this.$store.getters.token
      return this.actionUrl+'?storeType=8&storeId='+userInfo.storeId+'&accessId='+accessid
    },
    // 添加\关闭 限制词弹窗
    addQualifier(){
       this.addFrom.mgcText = ''
      this.dialogVisible = !this.dialogVisible
    },
    handleClose (done) {
      this.upItemId = ''
      this.$nextTick(()=>{
        this.$refs.mgcForm.resetFields()
      })
      done()
    },
    // 批量导入
    batchImport(){
      this.dialogVisible1 = !this.dialogVisible1
    },
    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log(e)
      this.pageSize = e
      this.indexs().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange (e) {
      this.loading = true
      this.dictionaryNum = e
      console.log(this.pageSize);

      this.currpage = (e - 1) * (this.pageSize||0) + 1
      this.indexs().then(() => {
        this.current_num =
          this.tableData.length ===  (this.pageSize||0)
            ? e *  (this.pageSize||0)
            : this.total
        this.loading = false
      })
    },
    async submit(){
      if(!this.booleSwitch){
        return
      }
      await this.$refs['mgcForm'].validate((value)=>{
        if(value){
          this.booleSwitch = false
          getSecConfig({
            api: "admin.plugin.manage.addSensitive",
            word:this.addFrom.mgcText,
            // upItemId 有值就是修改没有就是 添加
            id:this.upItemId||''
          }).then((res) => {
              if(res.data.code == 200){
                if(!this.upItemId){
                  this.succesMsg(this.$t('commonLanguage.tjcg'))
                }else{
                  this.succesMsg(this.$t('commonLanguage.bjcg'))
                }
                this.dialogVisible = false
                this.demand()
              }
          }).finally(()=>{
            setTimeout(()=>{
              this.booleSwitch = true
            },1500)
          })
        }
      })
    },
    // 修改
    updataItem({id,word}){
       this.upItemId=id
       this.addFrom.mgcText=word
       this.dialogVisible = !this.dialogVisible
    },
    // 删除
    deleteItem({id}=''){
      if(!id && this.ids.length==0){
        this.warnMsg(this.$t('plugInsSet.liveSetInfo.qxzxyscdmgc'))
        return
      }
      this.confirmBox(this.$t('plugInsSet.liveSetInfo.qdscgmgc')).then(() => {
        getSecConfig({
          api: "admin.plugin.manage.deleteSensitive",
          ids: id||this.ids.join(',')
        }).then((res) => {
          if(res.data.code == 200){
            this.succesMsg(this.$t('commonLanguage.sccg'))
            this.indexs()
          }
        });
      })
    },
    // 列表查询
    async indexs () {
      await getSecConfig({
        api: "admin.plugin.manage.selectSensitive",
        word:this.mgcForm.mgcText,
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
      }).then((res) => {
          if (res.data.code == 200) {
            const {list,total} = res.data.data
            this.tableData = list
            this.total = total
            this.current_num = total
          }
        });
    },
    demand(){
      // this.dictionaryNum = 1
      // this.indexs()
      this.handleCurrentChange(1)
    },
     // 上传之前的处理
     handleBeforeUpload(file) {
      this.fileName = file.name;
      this.file = file;
      this.tag = true;
    },

    // 上传成功
    handleUploadSuccess(res) {
      // 清空 文件
      this.$refs.upload.clearFiles();
        if(res.code == 200){
          this.succesMsg(this.$t('zdata.czcg'))
          this.dialogVisible1 =false
          this.demand()
        }else{
          this.errorMsg(res.message)

        }
    },

    handleSelectionChange(e){
      this.ids = e.map(v=>v.id)
    },
    back() {
      this.$router.push({
        path: "/plug_ins/plugInsSet/plugInsList",
      });
    },
    getBase() {
      this.goodsEditorBase = Config.baseUrl;
    },
    async getSecConfigs() {
      const res = await getSecConfig({
        api: "admin.plugin.manage.getLivingConfig",
      });
      console.log(res.data, "赋值");
      const Config = res.data.data;
      if (!Config) {
        sessionStorage.setItem(
          "ruleForm_preSaleSetInfo",
          JSON.stringify(this.ruleForm)
        );
        return;
      }
      this.ruleForm.is_open = Config.is_open;
      this.ruleForm.mch_is_open=Config.mch_is_open;
      this.ruleForm.push_url = Config.push_url;
      this.ruleForm.play_url = Config.play_url;
      this.ruleForm.license_url = Config.license_url;
      this.ruleForm.license_key = Config.license_key;
      this.ruleForm.agree_title = Config.agree_title;
      this.ruleForm.agree_content = Config.agree_content;

      //保存对比是否有改动数据，有则需要提示弹窗
      sessionStorage.setItem(
        "ruleForm_preSaleSetInfo",
        JSON.stringify(this.ruleForm)
      );
    },
    handleImageAdded(file, Editor, cursorLocation, resetUploader) {
      var formData = new FormData();
      formData.append("file", file); //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl, //上传路径
        method: "POST",
        params: {
          api: "resources.file.uploadFiles",
          storeId: getStorage("laike_admin_userInfo").storeId,
          groupId: -1,
          uploadType: 2,
          coverage:1,
          accessId: this.$store.getters.token,
        },
        data: formData,
      })
        .then((result) => {
          let url = result.data.data.imgUrls[0]; // 返回给你的图片路径
          Editor.insertEmbed(cursorLocation, "image", url);
          // Editor.setSelection(length + 1)
          resetUploader();
        })
        .catch((err) => {
          console.log(err);
        });
    },
    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm, "保存");
        if (valid) {
          try {
            let { entries } = Object;
            let data = {
              api: "admin.plugin.manage.addLivingConfig",

              is_open: this.ruleForm.is_open,
              mch_is_open:this.ruleForm.mch_is_open,
              agreeContent: this.ruleForm.agree_content,
              agreeTitle: this.ruleForm.agree_title,
              pushUrl: this.ruleForm.push_url,
              playUrl: this.ruleForm.play_url,
              license_url: this.ruleForm.license_url,
              license_key: this.ruleForm.license_key,
            };

            let formData = new FormData();
            for (let [key, value] of entries(data)) {
              formData.append(key, value);
            }


            if(!this.booleSwitch){
              return
            }
            this.booleSwitch = false
            setSecConfig(formData).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("plugInsSet.czcg"),
                  type: "success",
                  offset: 102,
                });
                this.getSecConfigs();
                setTimeout(() => {
                  //this.$router.go(-1)
                  this.$router.push({
                    path: "/plug_ins/plugInsSet/plugInsList",
                  });
                }, 1000);
              }
            }).finally(()=>{
            setTimeout(()=>{
              this.booleSwitch = true
            },1500)
          })
          } catch (error) {
            this.$message({
              message: error.message,
              type: "error",
              showClose: true,
              offset: 102,
            });
          }
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },

    oninput3(num) {
      if (num == 0) {
        return;
      }
      var str = num;
      str = str.replace(/[^\.\d]/g, "");
      str = str.replace(".", "");
      return str;
    },
  },
};
</script>

<style scoped lang="less">
//隐藏滚动条
::-webkit-scrollbar {
  display: none !important;
}
.container {
  display: flex;
  flex-direction: column;
  overflow: auto;
  padding-bottom:60px;
  /deep/.el-form {
    .header {
      width: 100%;
      height: 60px;
      line-height: 60px;
      border-bottom: 1px solid #e9ecef;
      padding-left: 20px;
      span {
        font-weight: 400;
        font-size: 16px;
        color: #414658;
      }
    }

    .basic-info {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;
      .basic-block {
        margin-top: 40px;
        padding: 0 96px 40px 20px;

        .el-input {
          width: 400px;
        }
      }
      .rules-blocks {
        padding: 0 96px 40px 20px;
      }
    }

    .order-info {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;
      .order-block {
        margin-top: 40px;
        padding: 0 20px 20px 20px;

        .el-input {
          width: 400px;
        }
      }
    }

    .rules-set {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      .rules-block {
        margin-top: 40px;
        padding: 0 20px 120px 20px;
        .quillWrapper {
          height: 341px;
          .quillWrapper {
            .ql-container {
              height: 300px;
            }
          }
        }
      }
    }

    .footer-button {
      position: fixed;
      right: 0;
      bottom: 40px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding: 15px 20px;
      border-top: 1px solid #e9ecef;
      background: #ffffff;
      width: 300%;
      z-index: 10;
      button {
        width: 70px;
        height: 40px;
      }
      .bgColor {
        margin-left: 14px;
      }
      .bgColor:hover {
        opacity: 0.8;
      }

      .fontColor {
        color: #6a7076;
        border: 1px solid #333;
        margin-left: 14px;
      }
      .fontColor:hover {
        color: #2890ff;
        border: 1px solid #2890ff;
      }
    }
    .el-form-item__label {
      font-weight: normal;
    }
  }
}
/deep/.el-input-group__append {
  padding: 0 !important;
  width: 45px !important;
  text-align: center;
}
.container {
  width: 100%;
  height: 737px;
  .djby {
    width: 36.25rem;
    background: rgba(244, 247, 249, 1);
    border-radius: 4px;
    padding: 20px 20px 6px 20px;
    box-sizing: border-box;
    div {
      width: 100%;
    }
    .myrow {
      div {
        width: 7rem !important;
      }
    }
    .row {
      display: flex;
      // align-items: center;
      margin-bottom: 14px;
      .el-input {
        width: 150px;
        margin: 0 5px;
        .el-input__inner {
          border-color: #d5dbe8;
          color: #414658;
          font-size: 14px;
          padding: 0 10px;
        }
      }
    }
  }
  .tip {
    width: 580px;
    // height: 83px;
    background-color: #f4f7f9;
    font-size: 14px;
    color: #97a0b4;
    padding: 8px;
    border-radius: 4px;
    p {
      display: flex;
      align-items: center;
      height: 22px;
      line-height: 1;
    }
  }
  .width {
    width: 570px;
  }
}
/deep/.el-table {
      .OP-button {
        .OP-button-bottom {
          .shangjia {
            display: flex;
            align-items: center;
            justify-content: center;
            .font-shangjia {
              margin-left: 3px;
              margin-bottom: 3px;
            }
          }
          .shangjia > span {
            height: 24px;
            line-height: 24px;
            display: inline-block;
          }
          .laiketui-shangjia:before,
          .laiketui-xiajia:before {
            font-size: 13px;
            position: relative;
            // top: 2px;
          }
        }
      }
    }
    .jump-list {
      margin: 10px;
      justify-content: space-between;
    > button:last-child{
      background-color: #fff;
      color: #838383;
      border: 1px solid #ccc;
    }
    .fontColor {
        color: #6a7076;
        border: 1px solid #333;
        margin-left: 14px;
      }
    .Search-input {
      width: 200px;
    }
  }
  .mgc-form{
    .uploads{
      margin-top:10px;
    }
    /deep/.el-input{
      width: 350px;
    }
    > div:last-child {
      text-align: end;
    }
  }
  .dialog-export {
    /deep/.item-center{
      width: 70px;
      height: 70px;
    }
    /deep/.el-upload-dragger{
        width: 100%;
        height: 100%;
        border: none;
      }
      /deep/ .el-upload:hover {
        cursor: default;
      }
  }

</style>
