<template>
  <div class="container ">
    <!-- App配置 -->
    <div class="merchants-list">
    <el-form
        :model="ruleForm"
        :rules="rules"
        label-position="right"
        ref="ruleIndexForm"
        label-width="auto"
        class="form-search"
      >
    <!-- 物流配置 wlpz -->
     <div class="form-card">
        <div class="title">{{ $t(`systemManagement.gtpz`) }}</div>

        <el-form-item :label="$t(`systemManagement.unip`)" style="">

            <div class="iframe-row-card">
            <div style="margin: 22px 0 22px 0">
                <el-form-item
                :label="$t(`systemManagement.AppKey`)"
                :label-width="language === 'en' ? '170px' : '100px'"
                >
                <el-input v-model="ruleForm.pushAppkey"></el-input>
                </el-form-item>
            </div>

            <div style="margin: 0 0 22px 0">
                <el-form-item
                :label="$t(`systemManagement.AppId`)"
                :label-width="language === 'en' ? '170px' : '100px'"
                >
                <el-input v-model="ruleForm.pushAppid  "></el-input>
                </el-form-item>
            </div>

            <div style="margin: 0 0 22px 0">

                <el-form-item
                :label="$t(`systemManagement.MasterECRET`)"
                :label-width="language === 'en' ? '170px' : '100px'"
                >
                <el-input v-model="ruleForm.pushMasterEcret  "></el-input>
                </el-form-item>
            </div>

            </div>
        </el-form-item>
    </div>
    <div class="form-card">

   <div class="title">{{ $t(`systemManagement.xzdz`) }}</div>

   <el-form-item   style="">
       <div class="iframe-row-card">


       <div style="margin: 0 0 22px 0">
           <!-- prop="express_secret" 去除校验 -->
           <el-form-item
           label="android"
           :label-width="language === 'en' ? '170px' : '100px'"
           >
           <el-input v-model="ruleForm.android_download_link  "></el-input>
           </el-form-item>
       </div>

       <div style="margin: 0 0 22px 0">
           <el-form-item
           label="IOS"
           :label-width="language === 'en' ? '170px' : '100px'"
           >
           <el-input v-model="ruleForm.ios_download_link  "></el-input>
           </el-form-item>
       </div>
       </div>
   </el-form-item>
</div>

    </el-form>
    </div>
      <div
      class="merchants-list"
      :class="tabPosition === '2' ? `` : 'intro'"
      :style="tabPosition === '2' ? `flex=1` : ''"
    >
      <div
        class="card"
        :style="tabPosition === '2' ? `height:100%;margin-bottom:0;` : ''"
      >
        <div class="title">
          <div>
            {{ $t("terminalConfig.ydt") }}
            <span style="color: rgb(151, 160, 180); font-size: 14px"
              >（ {{ $t("terminalConfig.zdktjsx") }}）</span
            >
          </div>
          <el-button
            class="shaco_btn"
            @click="showisAddGuidBox()"
            :disabled="page.tableData.length >= 3"
            >{{ $t("DemoPage.tableExamplePage.add_Guid") }}</el-button
          >
        </div>
        <!-- 弹框组件 end -->
        <div
          :style="
            page.tableData.length <= 0
              ? `padding: 0 20px 40px 20px`
              : 'padding: 0 20px 0px 20px'
          "
        >
          <el-table
            :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
            v-loading="page.loading"
            :data="page.tableData"
            ref="table"
            class="el-table"
            style="width: 100%"
            :height="tabPosition === '2' ? `520` : `252`"
            :style="
              page.tableData.length <= 0
                ? `border-bottom: 1px solid #E9ECEF;`
                : ''
            "
            :header-cell-style="{
              background: '#F4F7F9',
              height: '50px',
            }"
          >
            <template slot="empty">
              <div class="empty">
                <img src="../../../../assets/imgs/empty.png" alt="" />
                <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
              </div>
            </template>
            <el-table-column :label="$t('terminalConfig.xh')" align="center">
              <template slot-scope="scope">
                {{ scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('terminalConfig.tp')" align="center">
              <template slot-scope="scope">
                <img :src="scope.row.image" alt="" @error="handleErrorImg" />
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('terminalConfig.pxh')"
              prop="sort"
              align="center"
            >
            </el-table-column>
            <el-table-column
              :label="$t('terminalConfig.tjsj')"
              width="250"
              align="center"
            >
              <template slot-scope="scope">
                {{ scope.row.add_date | dateFormat }}
              </template>
            </el-table-column>

            <el-table-column
              :label="$t('terminalConfig.cz')"
              width="250"
              align="center"
            >
              <template slot-scope="scope">
                <div class="OP-button">
                  <div class="OP-button-top">
                    <el-button
                      icon="el-icon-edit-outline"
                      @click="loadGuid(scope.row.id)"
                      >{{ $t("DemoPage.tableExamplePage.edit") }}
                    </el-button>
                    <el-button
                      icon="el-icon-delete"
                      @click="Delete(scope.row.id)"
                      >{{ $t("DemoPage.tableExamplePage.delete") }}
                    </el-button>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <div class="footerBox">
            <!-- <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">取消</el-button> -->
            <el-button
              type="primary"
              class="footer-save bgColor mgleft"
              @click="submitForm('ruleIndexForm')"
              >{{ $t("DemoPage.tableFromPage.save") }}</el-button
            >
      </div>

    <div class="dialog-block">
      <el-dialog
        :title="$t('terminalConfig.bjydt')"
        :visible.sync="dialogVisible"
        :before-close="close"
      >
        <el-form
          :model="guidForm"
          :rules="rules"
          ref="ruleForm1"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="pass-input">
            <el-form-item :label="$t('terminalConfig.ydt')" prop="image">
              <l-upload
                :limit="1"
                v-model="guidForm.image"
                ref="upload"
                :text="$t('terminalConfig.jy750')"
                :mask_layer="false"
              >
              </l-upload>
            </el-form-item>
            <el-form-item :label="$t('terminalConfig.xh')">
              <el-input
                v-model="guidForm.sort"
                :placeholder="$t('terminalConfig.qsrxh')"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footers">
            <el-form-item>
              <el-button @click="cancel('ruleForm1')" class="shaco_color">{{
                $t("terminalConfig.ccel")
              }}</el-button>
              <el-button
                type="primary"
                @click="saveGuid(guidForm.id, 'ruleForm1')"
                class="qdcolor"
                >{{ $t("terminalConfig.okk") }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
    <!-- 添加引导图弹框组件 -->
    <div class="dialog-block">
      <el-dialog
        :title="$t('terminalConfig.tjydt')"
        :visible.sync="isAddGuidBox"
        :before-close="close2"
      >
        <el-form
          :model="guidFormAdd"
          :rules="rules"
          ref="ruleForm2"
          label-width="20%"
          class="demo-ruleForm"
        >
          <div class="pass-input">
            <el-form-item :label="$t('terminalConfig.ydt')" prop="image">
              <l-upload
                :limit="1"
                v-model="guidFormAdd.image"
                ref="upload"
                :text="$t('terminalConfig.jy750')"
                :mask_layer="false"
              >
              </l-upload>
            </el-form-item>
            <el-form-item :label="$t('terminalConfig.xh')">
              <el-input
                v-model="guidFormAdd.sort"
                type="number"
                min="0"
                @input="exgNumber()"
                :placeholder="$t('terminalConfig.qsrxh')"

              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footers">
            <el-button @click="cancel('ruleForm2')" class="shaco_color">{{
                $t("terminalConfig.ccel")
              }}</el-button>
              <el-button
                type="primary"
                @click="saveGuid(null, 'ruleForm2')"
                class="qdcolor"
                >{{ $t("terminalConfig.okk") }}</el-button
              >
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { VueEditor } from "vue2-editor";
import pageData from "@/api/constant/page";
import { index, save, getConfig, addConfig } from "@/api/mall/terminalConfig";
import { isEmpty } from "element-ui/src/utils/util";
import fa from "element-ui/src/locale/lang/fa";
import { del } from "@/api/mall/aftersaleAddress/aftersaleAddress";
import BannerListPC from "@/views/mall/terminalConfig/bannerListPC.vue";
import ErrorImg from '@/assets/images/default_picture.png'

import {
  getBottomInfo,
  addBottomInfo,
  delBottomById,
} from "@/api/mall/bannerPc";
export default {
  name:'appSetter',
  props: {
    tabPosition: {
      type: String,
      default: '1'
    }
  },
  data() {
    return {
      ruleForm:{
        android_download_link:'',
        ios_download_link:'',
        push_Appid:'',
        push_Appkey:'',
        push_MasterECRET:'',
      },
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      language: "",
      page: pageData.data(),
      // tabPosition: "1",
      type: 0,
      dialogVisible: false,
      isAddGuidBox: false,
      appForm: { editions: "123", type: 1 },
      weiXinForm: {},
      //引导图
      guidList: [],
      guidTotal: 0,
      //编辑引导图参数
      guidForm: {},
      //添加引导图参数
      guidFormAdd: { type: "1", image: null, sort: null },
      rules: {

        editions: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrbbh"),
            trigger: "blur",
          },
        ],
        android_url: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrazxz"),
            trigger: "blur",
          },
        ],
        ios_url: [
          {
            required: true,
            message: this.$t("terminalConfig.qsriosxz"),
            trigger: "blur",
          },
        ],
        edition: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrappym"),
            trigger: "blur",
          },
        ],
        image: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrydt"),
            trigger: "change",
          },
        ],
        appId: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrxcxid"),
            trigger: "blur",
          },
        ],
        appSecret: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrxcxmy"),
            trigger: "blur",
          },
        ],
      },
      wxRules: {
        appId: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrxcxid"),
            trigger: "blur",
          },
        ],
        appSecret: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrxcxmy"),
            trigger: "blur",
          },
        ],
        appTitle: [
          { required: true, message: this.$t("terminalConfig.qsrsybt"), trigger: "blur" },
        ],
        appLogo: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrydt"),
            trigger: "change",
          },
        ],
      },
      indexForm: {
        indexRules: "",
        indexLogo: "",
      },
      indexRules: {},
      updataType: 0,
      // PC轮播图页面
      shortcutMenu: [this.$t("terminalConfig.citieslist")[0], this.$t("terminalConfig.citieslist")[1]],
      cities: [this.$t("terminalConfig.citieslist")[0], this.$t("terminalConfig.citieslist")[1]],
      tableData: [],
      // 新增配置
      isConfig: false,
      configEditId: "",
      configTitle: "",
      // 浏览器配置
      browserForm: {
        mallIcon: "",
        mallName: "",
      },
      browserRules: {
        mallIcon: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrydt"),
            trigger: "change",
          },
        ],
        mallName: [{ required: true, message: this.$t("terminalConfig.qingshurumc"), trigger: "blur" }],
      },
      // 登录页配置
      registerForm: {
        mallLogo: "",
        shortcutMenu2: [],
        archival: "",
        copyright: "",
        authority: "",
        list: [{ name: "", link: "" }],
      },
      shortcutMenu2List: [
        { name: this.$t("terminalConfig.citieslist")[0], value: "1" },
        { name: this.$t("terminalConfig.citieslist")[1], value: "2" },
      ],
      registerRules: {
        mallLogo: [
          {
            required: true,
            message: this.$t("terminalConfig.qsrydt"),
            trigger: "change",
          },
        ],
      },
      // 首页配置
      homePageForm: {
        welcomeTerm: "",
        shortcutMenu3: [],
        APPUrl: "",
        APPExplain: "",
        H5Url: "",
        textExplain: ""
      },
      homePageRules: {
        welcomeTerm: [
          { required: true, message: this.$t("terminalConfig.qsrhysy"), trigger: "blur" },
        ],
      },
      shortcutMenu3List: [
        { name: this.$t("terminalConfig.shortcutMenu3List")[0], value: "1" },
        { name: this.$t("terminalConfig.shortcutMenu3List")[1], value: "2" },
        { name: this.$t("terminalConfig.shortcutMenu3List")[2], value: "3" },
        { name: this.$t("terminalConfig.shortcutMenu3List")[3], value: "4" },
        { name: this.$t("terminalConfig.shortcutMenu3List")[4], value: "5" },
        { name: this.$t("terminalConfig.shortcutMenu3List")[5], value: "6" },
        { name: this.$t("terminalConfig.shortcutMenu3List")[7], value: "8" },
      ],
      // { name:  this.$t("terminalConfig.shortcutMenu3List")[6], value: "7" },
      configurationForm: {
        images: "",
        title: "",
        subheading: "",
        sort: "",
        id: "",
      },
      confiRules: {
        images: [
          {
            required: true,
            message: this.$t("terminalConfig.qtjzp"),
            trigger: "blur",
          },
        ],
        title: [
          { required: true, message: this.$t("terminalConfig.sypzbtts"), trigger: "blur" },
        ]
      },
    };
  },
  components: {
    VueEditor,
    BannerListPC,
  },
  watch: {
    "guidFormAdd.image": {
      handler: function () {
        if (this.guidFormAdd.image != null) {
          this.$refs["ruleForm2"].clearValidate();
        }
      },
    },
  },
  created() {
    this.language = this.getCookit()
    console.log('232232232232', this.language);
    console.log('cookit', this.getCookit());
    this.loadData();
    this.handletBottomInfo();
    this.handleGetConfig();
    // this.$router.currentRoute.matched[2].meta.title = "微信小程序";
  },
  mounted() {
  },

  methods: {
    getTabIndex() {
      console.log('this.$refs.tab_bun', this.$refs.tab_bun.$children[0].value);
      // this.tabPosition = this.$refs.tab_bun.$children[0].value
    },
    // 获取cookiet
    getCookit() {
      let myCookie = document.cookie.split(';').map(item => {
        let arr = item.split('=')
        return { name: arr[0], value: arr[1] }
      })
      let strCookit = ''
      myCookie.forEach(item => {
        if (item.name.indexOf('language') !== -1) {
          strCookit = item.value
        }
      })
      return strCookit
    },
    // 图片错误处理
    handleErrorImg(e) {
      console.log('图片报错了', e.target.src);
      e.target.src = ErrorImg
    },
    changeValue(e) {
      this.$forceUpdate();
    },
    // 保存PC商城配置
    async handleSaveConfiguration() {
      const isEmpty = await this.handleVerify();
      let registerForm1 = JSON.parse(JSON.stringify(this.registerForm));
      registerForm1.shortcutMenu2 = registerForm1.shortcutMenu2.toString();

      let homePageForm1 = JSON.parse(JSON.stringify(this.homePageForm));
      homePageForm1.shortcutMenu3 = homePageForm1.shortcutMenu3.toString();
      // ...homePageForm1  去除商品分享设置
      let obj = { ...this.browserForm, ...registerForm1, ...homePageForm1 };
      console.log("XXXXXXX234234", obj, homePageForm1.list);
      if (isEmpty) {
        let { entries } = Object;
        let data2 = {
          api: "admin.pc.addConfig",
          ...obj,
        };
        let formData = new FormData();
        for (let [key, value] of entries(data2)) {
          if (key == "list") {
            formData.append(key, JSON.stringify(value));
          } else {
            formData.append(key, value);
          }
        }
        const res = await addConfig(formData);
        if (res.data.code == 200) {
          this.$message({
            type: "success",
            message: this.$t('zdata.baccg'),
            offset: 102
          });
          this.handleGetConfig();
        }
        console.log("是否保存了苏剧层高", res);
      }

      console.log("xxxxxxxx228", isEmpty, obj);
    },
    // 表单校验
    async handleVerify() {
      let IsV = false;
      this.$refs[`browserForm`].validate(async (valid) => {
        if (valid) {
          IsV = true;
        } else {
          IsV = false;
        }
      });
      this.$refs[`registerForm`].validate(async (valid) => {
        if (valid) {
          IsV = true;
        } else {
          IsV = false;
        }
      });
      // this.$refs[`homePageForm`].validate(async (valid) => {
      //   if (valid) {
      //     IsV = true;
      //   } else {
      //     IsV = false;
      //   }
      // });
      console.log("表单校验是否通过", IsV);
      return IsV;
    },
    // 添加底部链接
    handleAddButtonLink(v, i) {
      let obj = { name: "", link: "" };
      this.registerForm.list.push(obj);
    },
    handleDelButtonLink(v, i) {
      this.registerForm.list.splice(i, 1);
    },
    // 获取pc配置的数据
    async handleGetConfig() {
      let { entries } = Object;
      let data = {
        api: "admin.pc.getConfig",
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        if (value !== null && value !== "" && value !== undefined) {
          formData.append(key, value);
        } else {
          formData.append(key, '');
        }
      }
      const res = await getConfig(formData);
      console.log("pc配置", JSON.parse(res.data.data[1]));
      this.browserForm = JSON.parse(res.data.data[1]);

      console.log(
        "pc配置2",
        JSON.parse(res.data.data[2]),
        JSON.parse(res.data.data[2]).shortcutMenu2.split(",")
      );
      this.registerForm = JSON.parse(res.data.data[2]);
      this.registerForm.shortcutMenu2 = JSON.parse(
        res.data.data[2]
      ).shortcutMenu2.split(",");
      this.registerForm.list = JSON.parse(JSON.parse(res.data.data[2]).list);
      console.log(
        "pc配置",
        JSON.parse(res.data.data[3]),
        JSON.parse(res.data.data[3]).shortcutMenu3.split(",")
      );
      this.homePageForm = JSON.parse(res.data.data[3]);
      this.homePageForm.shortcutMenu3 = JSON.parse(
        res.data.data[3]
      ).shortcutMenu3.split(",");
    },
    // 添加配置
    async handleConfigSubmit() {
      console.log("xxx", this.configurationForm);
      this.$refs[`configurationForm`].validate(async (valid) => {
        if (valid) {
          let { entries } = Object;
          let data = {
            api: "admin.pc.addBottomInfo",
            ...this.configurationForm,
          };
          let formData = new FormData();
          for (let [key, value] of entries(data)) {
            formData.append(key, value);
          }
          const res = await addBottomInfo(formData);
          console.log("是否添加配置成功", res);
          if (res.data.code == 200) {
            this.$message({
              type: "success",
              message: this.configurationForm.id ? this.$t('zdata.bjcg') : this.$t('zdata.tjcg'),
              offset: 102
            });
            this.isConfig = false;
            this.handletBottomInfo();
          }
        } else {
          return false;
        }
      });
    },
    // 编辑配置
    async handleEditConfig(value) {
      console.log("编辑配置", value);
      this.configurationForm.images = value.image;
      this.configurationForm.title = value.title;
      this.configurationForm.subheading = value.subheading;
      this.configurationForm.sort = value.sort;
      this.configurationForm.id = value.id;
      this.configTitle = this.$t('terminalConfig.bjpz');
      this.isConfig = true;
    },
    // 删除配置
    async handleDelConfig(value) {
      this.$confirm(this.$t('terminalConfig.qdyscgpzm'), this.$t("pcBanner.bannerList.ts"), {
        confirmButtonText: this.$t("pcBanner.bannerList.okk"),
        cancelButtonText: this.$t("pcBanner.bannerList.ccel"),
        type: "warning",
      })
        .then(async () => {
          //
          // id
          let { entries } = Object;
          let data = {
            api: "admin.pc.delBottomById",
            id: value.id,
          };
          let formData = new FormData();
          for (let [key, value] of entries(data)) {
            formData.append(key, value);
          }
          const res = await delBottomById(formData);
          console.log("删除", res);
          if (res.data.code == 200) {
            this.$message({
              type: "success",
              message: this.$t('zdata.sccg'),
              offset: 102

            });
            this.handletBottomInfo();
          }
        })
        .catch(() => { });
    },
    async handletBottomInfo() {
      let { entries } = Object;
      let data = {
        api: "admin.pc.getBottomInfo",
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await getBottomInfo(formData);
      if (res.data.code == 200) {
        this.tableData = res.data.data.list;
      }
      console.log("2025", res);
    },
    // 添加配置
    handleAddConfig() {
      if (this.tableData.length >= 4) {
        this.$message({
          message: this.$t('terminalConfig.syzdtjsx'),
          type: "warning",
          offset: 102

        });
        return;
      }
      this.isConfig = true;
      this.configTitle = this.$t('terminalConfig.sytjpz');
      this.configurationForm.images = undefined;
      this.configurationForm.title = "";
      this.configurationForm.subheading = "";
      this.configurationForm.sort = "";
      this.configurationForm.id = "";
      this.$nextTick(() => {
        this.$refs.lupload.fileList = [];
      });
    },
    // 配置操作
    handleCloseConfig() {
      this.isConfig = false;
      this.configurationForm.images = undefined;
      this.configurationForm.title = "";
      this.configurationForm.subheading = "";
      this.configurationForm.sort = "";
      this.configurationForm.id = "";
      this.$refs["configurationForm"].clearValidate();

    },
    // 轮播图界面操作
    handleAddBanner() {
      this.$refs.bannerList.addBanner = true;
      this.$refs.bannerList.handleCloseData()
    },
    async loadData() {
      const res = await index({
        api: "admin.terminal.index",
        type: 2,
        storeType: this.tabPosition,
      });
      let guidList = null;
      if (!isEmpty(res)) {
        let data = res.data.data;
        guidList = data.guide_list;

          this.ruleForm.pushAppkey   = data.push_Appkey;
          this.ruleForm.pushAppid = data.push_Appid;
          this.ruleForm.pushMasterEcret = data.push_MasterECRET;

          this.ruleForm.android_download_link = data.android_download_link || data.android_url;
          this.ruleForm.ios_download_link = data.ios_download_link || data.ios_url;

      }
      this.page.total = this.guidTotal;
      this.page.tableData = guidList;
      this.page.loading = false;
      if (this.page.tableData < 10) {
        this.page.current_num = this.page.total;
      }
      if (this.page.total < this.page.current_num) {
        this.page.current_num = this.page.total;
      }
      this.page.showPagebox = true;
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.page.loading = true;
      // this.page.current_num = e
      this.page.pageSize = e;
      this.getBrandInfos().then(() => {
        this.page.currpage =
          (this.page.dictionaryNum - 1) * this.page.pageSize + 1;
        this.page.current_num =
          this.page.tableData.length === this.page.pageSize
            ? this.page.dictionaryNum * this.page.pageSize
            : this.page.total;
        this.page.loading = false;
      });
    },
    // 查询
    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.page.showPagebox = false;
      this.page.loading = true;
      this.page.dictionaryNum = 1;
      this.loadData().then(() => {
        this.page.loading = false;
        if (this.page.tableData.length > 5) {
          this.page.showPagebox = true;
        }
      });
    },
    //点击上一页，下一页
    handleCurrentChange(e) {
      this.page.loading = true;
      this.page.dictionaryNum = e;
      this.page.currpage = (e - 1) * this.page.pageSize + 1;
      this.getBrandInfos().then(() => {
        this.page.current_num =
          this.page.tableData.length === this.page.pageSize
            ? e * this.page.pageSize
            : this.page.total;
        this.page.loading = false;
      });
    },
    showisAddGuidBox() {
      this.isAddGuidBox = true;
      console.log('606606606606', this.$refs.upload.fileList);
      // this.$nextTick(()=>{
      //   this.$refs.upload.fileList = []
      // })
      setTimeout(() => {
        this.$refs.upload.fileList = []
      })
    },

    exgNumber() {
      if (this.guidFormAdd.sort < 0) {
        this.guidFormAdd.sort = 0;
      }

      this.guidFormAdd.sort = Number(this.guidFormAdd.sort);
      if (this.guidFormAdd.sort != this.guidFormAdd.sort.toFixed(0)) {
        this.$message({
          message: this.$t(`terminalConfig.pxhbnwxs`),
          type: "warning",
          offset: 102

        });
        this.guidFormAdd.sort = this.guidFormAdd.sort.toFixed(0);
      }
    },

    //图片上传
    handleImageAdded() { },
    Delete(id) {
      this.$confirm(
        this.$t("terminalConfig.scts"),
        this.$t("terminalConfig.ts"),
        {
          confirmButtonText: this.$t("terminalConfig.okk"),
          cancelButtonText: this.$t("terminalConfig.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          let { entries } = Object;
          let data = {
            api: "admin.weixinApp.delWeiXinGuideImage",
            type: this.tabPosition,
            id: id,
          };
          let formData = new FormData();
          for (let [key, value] of entries(data)) {
            formData.append(key, value);
          }
          del(formData).then((res) => {
            this.demand();
            this.$message({
              type: "success",
              message: this.$t("terminalConfig.shanchuchengg"),
              offset: 102

            });
          });
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除'
          // })
        });
    },
    //加载引导图信息
    async loadGuid(id) {
      this.dialogVisible = true;
      const res = await index({
        api: "admin.weixinApp.getWeiXinGuideImageInfo",
        id: id,
        storeType: this.tabPosition,
      });

      console.log(res);
      let data = res.data.data;
      this.guidForm = data.list[0];
      this.guidForm.type = this.guidForm.type;
    },
    //保存引导图
    async saveGuid(id, ruleForm) {
      this.guidFormAdd.sort = Number(this.guidFormAdd.sort);
      if (this.guidFormAdd.sort != this.guidFormAdd.sort.toFixed(0)) {
        this.$message({
          message: this.$t('zdata.pxbnwxs'),
          type: "warning",
          offset: 102

        });
        this.guidFormAdd.sort = this.guidFormAdd.sort.toFixed(0);
      }
      this.$refs[ruleForm].validate((valid) => {
        if (valid) {
          try {
            if (!isEmpty(id)) {
              let { entries } = Object;
              let data = {
                api: "admin.weixinApp.addWeiXinGuideImage",
                id: id ? id : "",
                type: parseInt(this.guidForm.type),
                sort: this.guidForm.sort,
                imgUrl: this.guidForm.image,
                storeType: this.tabPosition,
                source:2
              };

              let formData = new FormData();
              for (let [key, value] of entries(data)) {
                if (value !== null && value !== "" && value !== undefined) {
                  formData.append(key, value);
                } else {
                  formData.append(key, '');
                }
              }
              const res = save(formData);

              res.then((ress) => {
                console.log(ress);
                if (ress.data.code == 200) {
                  this.$message({
                    message: this.$t("terminalConfig.bjcg"),
                    type: "success",
                    offset: 102,
                  });
                  this.loadData();
                  this.dialogVisible = false;
                  this.guidFormAdd = { type: "1", image: null, sort: null };
                  this.$refs.upload.fileList = [];
                }
              });
            } else {
              let { entries } = Object;
              let data = {
                api: "admin.weixinApp.addWeiXinGuideImage",
                type: parseInt(this.guidFormAdd.type),
                sort: parseInt(this.guidFormAdd.sort),
                imgUrl: this.guidFormAdd.image,
                storeType: this.tabPosition,
                source:2

              };

              let formData = new FormData();
              for (let [key, value] of entries(data)) {
                formData.append(key, value);
              }
              const res = save(formData);
              res.then((ress) => {
                console.log(ress);
                if (ress.data.code == 200) {
                  this.$message({
                    message: this.$t("terminalConfig.tianjiachengg"),
                    type: "success",
                    offset: 102,
                  });
                  this.loadData();
                  this.isAddGuidBox = false;
                  this.guidFormAdd = { type: "1", image: null, sort: null };
                  this.$refs.upload.fileList = [];
                  this.handletBottomInfo();
                }
              });
            }
          } catch (err) {
            this.$message({
              message: "",
              type: "error",
              offset: 102,
            });
          }
        }
      });
    },
    close2() {
      this.isAddGuidBox = false;
      this.guidFormAdd = { type: "1", image: null, sort: null };
      this.$refs.upload.fileList = [];
      this.$refs["ruleForm2"].clearValidate();
    },
    close() {
      this.dialogVisible = false;
      this.guidForm = { image: null };
      this.$refs["ruleForm2"].clearValidate();
    },
    cancel(val) {
      this.$refs[val].clearValidate();
      this.isAddGuidBox = false;
      this.dialogVisible = false;
      this.$refs.upload.fileList = [];
    },
    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        let text = this.$t("terminalConfig.cg");
        let param = {
            api: "admin.terminal.saveApp",
            ...this.ruleForm,

          };

        if (valid) {
          if (true) {
            this.$refs[formName].validate(async (valid) => {
              console.log("图片没有保存", this.weiXinForm);
              return;
            });
          }
          try {
            let { entries } = Object;
            let formData = new FormData();
            for (let [key, value] of entries(param)) {
              if (value !== null && value !== "" && value !== undefined) {
                formData.append(key, value);
              } else {
                formData.append(key, "");
              }
            }
            save(formData).then((res) => {
              console.log(res);
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t(`zdata.baccg`),
                  type: "success",
                  offset: 102,
                });
                this.demand();
              }
            });
          } catch (e) {
            this.$message({
              message: e.message,
              type: "error",
              showClose: true,
              offset: 102
            });
          }
        } else {
          return false;
        }
      });
    },
  },
};

</script>

<style scoped lang="less">
@import "../../../../webManage/css/mall/terminalConfig/terminalList.less";
/deep/.el-form-item {
        .prompt {
            margin-left: 14px;
            color: #97a0b4;
        }
    }
  .footerBox {
    position: fixed;
    right: 0;
    bottom: 40px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 15px 20px;
    border-top: 1px solid #E9ECEF;
    background: #FFFFFF;
    width: 300%;
    z-index: 10;
    button {
      width: 70px;
      height: 40px;
    }
  }
  .form-footers{
        width: 100% !important;
        display: flex;
        justify-content: flex-end;
        align-items: center;
        border-top: 0.0625rem solid #E9ECEF;
        padding: 20px;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        .el-form-item{
          padding-right: 20px;
        }
      }

/deep/.el-dialog__body{
  padding: 0px !important;
}
.pass-input{
  padding: 20px !important;
}
</style>
