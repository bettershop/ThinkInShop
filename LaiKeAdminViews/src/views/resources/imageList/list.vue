<template>
  <div class="container">
    <div class="mytop" @click="xuanzhong = ''">
      <div class="mytop_tab" :class="mytype==1?'active':''" @click="changeList(1)">{{ $t('material.tp') }}</div>
      <div class="mytop_tab" :class="mytype==2?'active':''" @click="changeList(2)">{{ $t('material.sp') }}</div>
    </div>

    <div class="merchants-list" ref="tableFather" @click="xuanzhong = ''">
      <div class="mybody">
        <!-- 分类 -->
        <div class="mybody_l">
          <div class="mybody_l_top">
            <el-button class="fontColor" @click="add"
              ><i class="el-icon-plus"></i>{{ $t('material.xzfl') }}</el-button
            >
          </div>
          <li
            class="mybody_l_li"
            :class="myclass == '-1' ? 'liActive' : ''"
            @click="goList()"
          >
            <p class='mybody_l_li_p'><span class="">{{ $t('material.qb') }}</span></p>
          </li>
          <div class="mybody_l_li_d" :style="{ height: tableHeight + 'px' }">
            <li
              class="mybody_l_li"
              :class="myclass == item.id ? 'liActive' : ''"
              v-for="(item, index) in classList"
              :key="item.id"
              @click="goList(item.id)"
              v-show="item.is_show == 2"
            >
              <el-input
                v-if="editclass == item.id"
                size="mini"
                v-model="item.name"
                @blur="myAddClass(item)"
              ></el-input>
              <p v-else class='mybody_l_li_p'>
                <span class="mybody_l_li_p_sp">{{ item.name }}</span
                ><i
                  class="el-icon-edit-outline"
                  @click.stop="editclass = item.id"
                ></i>
                <i v-if='item.haveContent==1' class="el-icon-delete" @click.stop="delclass(item.id)"></i>
                <i v-else class="el-icon-delete" @click.stop="delclass(item.id)"></i>

              </p>
            </li>
          </div>

        </div>
        <!-- 列表 -->
        <div class="mybody_r">
          <div class="mybody_r_Search" ref="mySearch">
            <!-- 查询 -->
            <div class="Search-condition">
              <div class="query-input">
                <el-input
                  size="medium"
                  v-model="inputInfo.imageName"
                  @keyup.enter.native="demand"
                  class="Search-input"
                  :placeholder="$t('material.qrcsmc')"
                ></el-input>
                <div class="select-date">
                  <el-date-picker
                    v-model="inputInfo.range"
                    type="datetimerange"
                    :range-separator="$t('reportManagement.businessReport.zhi')"
                    :start-placeholder="
                      $t('reportManagement.businessReport.ksrq')
                    "
                    :end-placeholder="
                      $t('reportManagement.businessReport.jsrq')
                    "
                    value-format="yyyy-MM-dd HH:mm:ss"
                    :editable="false"
                  ></el-date-picker>
                </div>
              </div>
              <div class="btn-list">
                <el-button class="fontColor" @click="reset">{{
                  $t("DemoPage.tableExamplePage.reset")
                }}</el-button>
                <el-button class="bgColor" type="primary" @click="demand"
                  >{{ $t("DemoPage.tableExamplePage.demand") }}
                </el-button>
                <el-button
                  class="bgColor export"
                  type="primary"
                  @click="upimg()"
                  ><i class="el-icon-upload2"></i>{{ $t('material.scsm') }}</el-button
                >
              </div>
            </div>

          </div>
          <!-- 主表 -->
          <div class="mybody_r_div" :style="{ height: tableHeight + 'px' }" v-if="page.tableData.length>0">
            <div
              class="mybody_r_div_d"
              v-for="(item, index) in page.tableData"
              :key="item.id"
              @click.stop="Chose(item)"
            >
              <el-checkbox
                class="mybody_r_div_d_i"
                v-model="item.select"
                v-show="item.select"
              ></el-checkbox>
              <img
                class="mybody_r_div_d_img" v-if='mytype==1'
                :class="xuanzhong == item.id ? 'div_active' : ''"
                :src="item.imgUrl"
                alt=""
              />
              <video class="mybody_r_div_d_img" v-if='mytype==2'
                :class="xuanzhong == item.id ? 'div_active' : ''"
                :src="item.imgUrl"></video>
              <p class="mybody_r_div_d_p">{{ item.name }}</p>
            </div>
            <i class="mybody_r_div_i" v-for="index in 10" :key="index"></i>
          </div>
          <div v-else class="empty">
                <div class="empty1">
                  <img src="../../../assets/imgs/empty.png" alt="" />
                 <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
              </div>
          </div>
          <!-- 底部 -->
          <div class="tabFoot" ref="tabFoot" style="width: calc(100% + 30px)">
            <div>
              <el-checkbox v-model="checkAll" @change='choseAll()'>{{ $t('material.qx') }}</el-checkbox>
              <span class="tabFoot_s">{{ $t('material.yx') }}{{ choseList.length }}{{ $t('material.g') }}</span>
            </div>
            <div>
              <el-button class="fontColor" @click='Download()' :disabled='choseList.length==0'
                ><i class="el-icon-download"></i>{{ $t('material.xz') }}</el-button
              >
              <el-button class="fontColor" @click="Del()" :disabled='choseList.length==0'
                ><i class="el-icon-delete"></i>{{ $t('material.sc') }}</el-button
              >
              <el-button class="fontColor" @click="dialogVisible3 = true" :disabled='choseList.length==0'
                >{{ $t('material.ydfl') }}</el-button
              >
            </div>
          </div>
          <!-- 详情 -->
          <div
            v-if="xuanzhong"
            class="list_right"
            :style="{ height: tableHeight + 'px' }"
            @click.stop
          >
            <p class="list_right_p">{{ $t('material.scxq') }} {{mytype}}</p>
            <div class="list_right_img">
              <img :src="imgEdit.imgUrl" alt="" v-if='mytype==1'/>
              <video class="mybody_r_div_d_img" v-if='mytype==2'
                :src="imgEdit.imgUrl"></video>
            </div>
            <el-form
              :model="imgEdit"
              :rules="rules"
              label-position="top"
              ref="ruleForm"
              label-width="auto"
              class="form-search"
            >
              <el-form-item :label="$t('material.mc')" prop="url">
                <el-input
                  size="medium"
                  v-model="imgEdit.name"
                  :placeholder="$t('material.qrmc')"
                ></el-input>
              </el-form-item>
              <el-form-item :label="$t('material.lj')" prop="url" >
                <p>
                  <i
                    class="el-icon-document-copy"
                    v-copy="imgEdit.imgUrl"
                  ></i>
                </p>
                <el-input
                  size="medium"
                  v-model="imgEdit.imgUrl"
                  :placeholder="$t('material.mrlj')" disabled
                ></el-input>
              </el-form-item>
              <el-form-item :label="$t('material.lb')">
                <!-- <i class='el-icon-document-copy mycopy'></i> -->
                <el-input
                  size="medium"
                  v-model="imgEdit.groupName"
                  :placeholder="$t('material.mrlb')" disabled
                ></el-input>
              </el-form-item>
              <el-form-item :label="$t('material.tjrq')">
                <el-input
                  size="medium"
                  v-model="imgEdit.add_time"
                  :placeholder="$t('material.qsrtjrq')" disabled
                ></el-input>
              </el-form-item>
              <el-form-item :label="$t('material.cc')" >
                <el-input
                  size="medium"
                  v-model="imgEdit.size"
                  :placeholder="$t('material.qsrcc')" disabled
                ></el-input>
              </el-form-item>
              <el-form-item :label="$t('material.dx')" >
                <el-input
                  size="medium"
                  v-model="imgEdit.dimension"
                  :placeholder="$t('material.qsrdx')" disabled
                ></el-input>
              </el-form-item>
              <el-form-item :label="$t('material.scr')">
                <el-input
                  size="medium"
                  v-model="imgEdit.add_user"
                  :placeholder="$t('material.qsrscr')" disabled
                ></el-input>
              </el-form-item>
              <el-button class="bgColor rulesave" @click='imgsave()' type="primary">{{ $t('material.bc') }}</el-button>
            </el-form>
          </div>
        </div>
      </div>
      <!-- 分页 -->
      <div class="pageBox" ref="pageBox">
        <div class="pageLeftText">
          {{ $t("DemoPage.tableExamplePage.show") }}
        </div>
        <el-pagination
          layout="sizes, slot, prev, pager, next"
          :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
          :next-text="$t('DemoPage.tableExamplePage.next_text')"
          @size-change="handleSizeChange"
          :page-sizes="pagesizes"
          :current-page="pagination.page"
          @current-change="handleCurrentChange"
          :total="total"
        >
          <div class="pageRightText">
            {{ $t("DemoPage.tableExamplePage.on_show") }}{{ currpage }}-{{
              current_num
            }}{{ $t("DemoPage.tableExamplePage.twig") }}{{ total
            }}{{ $t("DemoPage.tableExamplePage.twig_notes") }}
          </div>
        </el-pagination>
      </div>
    </div>
    <!-- 添加分类 -->
    <div class="dialog-block">
      <el-dialog
        :title="$t('material.tzc')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <div>
          <div class="dia_top">
            <div class="dia_top_h">
              <el-input
                class="dia_top_input"
                size="medium"
                v-model="className"
                :placeholder="$t('material.qsrcflm')"
              ></el-input>
              <el-button class="bgColor" type="primary" @click="myAddClass()"
                >{{ $t('material.tz') }}</el-button
              >
            </div>
            <p class="dia_top_p" v-show='showP'>{{ $t('material.czt') }}</p>
          </div>

          <p class="dia_p">
            {{ $t('material.flbm') }}<span>{{ $t('material.gxxz') }}</span>
          </p>
          <div class="dia_div">
            <div
              class="dia_div_li"
              v-for="(item, index) in classList2"
              :key="item.id"
            >

              <p class="dia_div_li_p">
                <el-checkbox
                  :true-label="2"
                  :false-label="1"
                  v-model="item.is_show"
                  ><span v-if="editclass2 != item.id" class="p_span">{{ item.name }}</span></el-checkbox
                >
                <el-input
                  v-if="editclass2 == item.id"
                  size="mini"
                  v-model="item.name"
                  @blur="myAddClass(item)"
                ></el-input>
                <i
                  class="el-icon-edit-outline"
                  @click="editclass2 = item.id"
                ></i>
                <i
                  v-if="item.haveContent != 1"
                  class="el-icon-delete"
                  @click="delclass(item.id)"
                ></i>
                <i
                  v-else
                  class="el-icon-delete"
                  style='opacity: 0.3;'
                ></i>
              </p>
            </div>
            <i class="dia_div_li_i" v-for="indexs in 10" :key="indexs"></i>
          </div>
        </div>
        <div slot="footer" class="form-footer">
          <el-button class="kipColor" @click="handleClose">{{
            $t("inventoryManagement.ccel")
          }}</el-button>
          <el-button
            class="bdColor"
            type="primary"
            @click="determine('ruleForm')"
            >{{ $t("inventoryManagement.okk") }}</el-button
          >
        </div>
      </el-dialog>
    </div>
    <!-- 上传素材 -->
    <div class="dialog-block2">
      <el-dialog
        :title="$t('material.scsm')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <el-upload
          drag
          :action="actionUrl"
          :auto-upload="false"
          :on-change="handleAvatarChange"
          :show-file-list="false"
          class='upimg_d_f'
        >
          <div class="upimg_d">
            <i class="el-icon-upload"></i>
            <p>{{ $t('material.ckjcsm') }}</p>
          </div>

        </el-upload>
        <div slot="footer" class="form-footer">
          <el-button class="kipColor" @click="handleClose2">{{
            $t("inventoryManagement.ccel")
          }}</el-button>
          <!-- <el-button class="bdColor" type="primary" @click="determine('ruleForm')">{{$t('inventoryManagement.okk')}}</el-button> -->
        </div>
      </el-dialog>
    </div>
    <!-- 移到分类 -->
    <div class="dialog-block3">
      <el-dialog
        :title="$t('material.ydfl')"
        :visible.sync="dialogVisible3"
        :before-close="handleClose3"
      >
        <div>
          <el-form label-position="right" label-width="90px">
            <el-form-item :label="$t('material.flmc')" required>
              <el-select class="select-input" v-model="cgclass">
                <el-option
                  v-for="item in classList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <div slot="footer" class="form-footer">
          <el-button class="kipColor" @click="handleClose3">{{
            $t("inventoryManagement.ccel")
          }}</el-button>
          <el-button class="bdColor" type="primary" @click="goClass()">{{
            $t("inventoryManagement.okk")
          }}</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>


<script>
import pageData from "@/api/constant/page";
import { del, save, index, download } from "@/api/resources/imageManage";
import { exportss } from "@/api/export/index";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";
import ErrorImg from "@/assets/images/default_picture.png";
import Config from "@/packages/apis/Config";
import axios from "axios";
import { addClass } from "element-ui/src/utils/dom";
export default {
  name: "list",
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      page: pageData.data(),
      dialogVisible: false,
      dialogVisible2: false,
      dialogVisible3: false,
      actionUrl: Config.baseUrl,
      mytype:1,//1图片2视频
      showP:false,//分类重复
      inputInfo: {
        imageName: "",
        range: "",
      },
      imageName: null,
      data: null,
      choseList: [],
      dataForm: {},
      button_list: [],
      //需要下载的图片
      needImg: null,
      // table高度
      tableHeight: null,
      //全选
      checkAll: false,
      //图片编辑
      imgEdit: {},
      rules: {},
      //分类列表
      classList: [],
      classList2: [],
      //选择分类
      myclass: '-1',
      //修改分类
      editclass: "",
      //修改分类
      editclass2: "",
      //选中
      xuanzhong: "",
      className: "",
      //移动分类
      cgclass: "",
    };
  },
  //组装模板
  created() {
    this.loadData();
    this.getClassList();
    this.getButtons();
  },
  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },
  methods: {
    copyImg(imgUrl){

    },
    // 图片错误处理
    handleErrorImg(e) {
      console.log("图片报错了", e.target.src);
      e.target.src = ErrorImg;
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight -
        this.$refs.pageBox.clientHeight -
        this.$refs.mySearch.clientHeight -
        this.$refs.tabFoot.clientHeight -
        25;
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "imageList") {
          return (this.menuId = item.id);
        }
      });
      let buttonList = await getButton({
        api: "saas.role.getButton",
        menuId: this.menuId,
      });
      this.button_list = buttonList.data.data;
    },
    async loadData() {
      await this.getList().then();
    },
    //切换标签
    goList(e) {
      if (e) {
        this.myclass = e;
      } else {
        this.myclass = -1;
      }
      this.demand();
    },
    // 获取列表
    async getList() {
      this.choseList = [];
      this.checkAll=false
      const res = await index({
        api: "admin.resources.index",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        imageName: this.inputInfo.imageName,
        startTime: this.inputInfo.range?.[0] ?? "",
        endTime: this.inputInfo.range?.[1] ?? "",
        groupId: this.myclass,
        type: this.mytype,
      });
      this.total = res.data.data.total;
      this.page.tableData = res.data.data.list;
      this.page.loading = false;
      if (res.data.data.total < 10) {
        this.current_num = this.total;
      }
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
    },
    // 获取分类列表
    getClassList() {
      index({
        api: "admin.resources.groupList",
        type: this.mytype,
      }).then((res) => {
        this.classList = res.data.data.list;
        this.classList2 = JSON.parse(JSON.stringify(res.data.data.list));
      });
    },

    // 重置
    reset() {
      this.inputInfo.imageName = null;
      this.inputInfo.range = null;
    },
    handleClose() {
      this.className=''
      this.dialogVisible = false;
    },
    handleClose2() {
      this.dialogVisible2 = false;
    },
    handleClose3() {
      this.dialogVisible3 = false;
    },
    upimg() {
      this.dialogVisible2 = true;
    },
    add() {
      this.dialogVisible = true;
    },
    // 查询
    demand() {
      this.showPagebox = false;
      this.page.loading = true;
      this.dictionaryNum = 1;
      this.loadData().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.page.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.page.loading = false;
      });
    },
    //切换视频/图片
    changeList(e){
      this.mytype=e
      this.myclass='-1'
      this.inputInfo.imageName = ''
      this.demand()
      this.getClassList();
    },
    //选择一页多少条
    handleSizeChange(e) {
      this.page.loading = true;
      this.pageSize = e;
      this.loadData().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.page.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.page.loading = false;
      });
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.page.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.loadData().then(() => {
        this.current_num =
          this.page.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.page.loading = false;
      });
    },
    //去重
    recates(e) {
      return e.filter((item, index) => {
        return e.indexOf(item) === index;
      });
    },
    // 上传改变
    async handleAvatarChange(file, fileList) {
      // this.uploadImgApi(file);
      try {
        if(this.mytype==2){
          await this.beforeAvatarUpload(file)
        }else{
          await this.beforeAvatarUpload2(file)
        }

        this.uploadImgApi(file)
      } catch (e) {
        this.warnMsg(JSON.stringify(e))
      }
    },
    beforeAvatarUpload (file) {
      return new Promise((resolve, reject) => {
        if (
          [
            'video/mp4',
            'video/mov',
            'video/flv',
            'video/avi',
            'video/wmv',
            'video/rmvb',
            'video/webm'
          ].indexOf(file.raw.type) == -1
        ) {
          return this.warnMsg(this.$t('releasephysical.qsczcdspgs'))
        }

        if (file.size / 1024 / 1024 > 100) {
          return this.warnMsg(this.$t('releasephysical.spdxbncg'))
        }
        resolve()
      })
    },
    beforeAvatarUpload2 (file) {
      console.log(file.raw.type,'file.raw.type')
      return new Promise((resolve, reject) => {

        if (
          [
            'image/png',
            'image/jpg',
            'image/jpeg',
          ].indexOf(file.raw.type) == -1
        ) {
          return this.warnMsg('请上传正确的图片格式')
        }

        resolve()
      })
    },
    uploadImgApi(file) {
      console.log("file", file);
      const videoSrc = URL.createObjectURL(file.raw);
      var formData = new FormData();
      formData.append("file", file.raw); //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl, //上传路径
        method: "POST",
        params: {
          api: "admin.resources.uploadFiles",
          storeId: getStorage("laike_admin_userInfo").storeId,
          accessId: this.$store.getters.token,
          groupId:this.myclass,
        },
        data: formData,
      })
        .then((res) => {
          if(res.data.code==200){
            this.$message({
              type: "success",
              message: "上传成功",
            });
            this.dialogVisible2 = false;
            this.getList();
          }else if(res.data.code==51073){
            let that=this
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
                  groupId:that.myclass,
                  coverage:1,
                },
                data: formData,
              })
                .then((res) => {
                  if(res.data.code==200){
                    that.$message({
                      type: "success",
                      message: "上传成功",
                    });
                    that.dialogVisible2 = false;
                    that.getList();
                  }

                })
                .catch((err) => {
                  console.log(err);
                });
              })
              .catch(() => {});
          }

        })
        .catch((err) => {
          console.log(err);
        });
    },
    //修改图片
    imgsave(){
      index({
        api: "admin.resources.uploadFiles",
        id: this.imgEdit.id,
        name: this.imgEdit.name,
        groupId:this.myclass,
      }).then((res) => {
        if (res.data.code == 200) {

          this.$message({
            type: "success",
            message: "修改成功",
          });

          this.getList();
        }
      });
    },
    // 分类修改
    determine() {
      let list = [];
      this.classList2.forEach((e) => {
        if (e.is_show == 2) {
          list.push(e.id);
        }
      });
      let id = list.join(",");
      index({
        api: "admin.resources.updateCatalogueShow",
        id: id,
        type:this.mytype
      }).then((res) => {
        if (res.data.code == 200) {
          this.succesMsg( "修改成功");
          this.className=''
          this.dialogVisible=false
          this.getClassList();
        }
      });
    },
    //添加分类
    myAddClass(item) {
      this.editclass = "";
      this.editclass2 = "";
      let name = "";
      let id = "";
      if (item) {
        name = item.name;
        id = item.id;
      } else {
        name = this.className;
      }
      index({
        api: "admin.resources.addGroup",
        id: id,
        catalogueName: name,
        type: this.mytype,
      }).then((res) => {
        if (res.data.code == 200) {
          if (id) {
            this.succesMsg("修改成功");
          } else {
            this.succesMsg( "添加成功" );
          }
          this.className=''
          this.getClassList();
        }else if(res.data.code==50755){
          if(!item){
            this.showP=true
            setTimeout(() => {
              this.showP=false
            }, 3000);
          }

          this.getClassList();
        }
      });
    },
    //移到分类
    goClass() {
      index({
        api: "admin.resources.updateCatalogueByImageIds",
        imageIds: this.choseList.toString(),
        catalogueId: this.cgclass,
      }).then((res) => {
        if (res.data.code == 200) {
          this.succesMsg("修改成功");

          this.getList();
          this.getClassList()
          this.dialogVisible3 = false;
        }
      });
    },
    //同步获取图片大小
    async getMeta(url) {
      let img = new Image();
      img.src = url;
      return new Promise(resolve => {
        img.onload = function() {
          return resolve({
            width: this.width,
            height: this.height
          });
        };
      });
    },
    // 获取图片尺寸
    async getSize(){
      if(this.mytype==1){//图片
        let { width, height } = await this.getMeta(
          this.imgEdit.imgUrl
        )
        this.imgEdit.size=width+'x'+height

      }else if(this.mytype==2){//视频
        // 获取上传的视频的宽高
        let that=this
        let videoObj = document.createElement("video");
        videoObj.src=that.imgEdit.imgUrl
        videoObj.onloadedmetadata = function (evt) {
            // URL.revokeObjectURL(that.imgEdit.imgUrl);
            console.log(videoObj.videoWidth , videoObj.videoHeight,'2222222222');
            that.imgEdit.size=videoObj.videoWidth+'x'+videoObj.videoHeight
        };

      }
      let res = await fetch(this.imgEdit.imgUrl)

      let data =await res.blob();
      let size = Math.ceil(data.size / 1024);
      this.imgEdit.dimension=size+'kb'

      // fetch(this.imgEdit.imgUrl).them((res)=>{
      //   let data =res.blob();
      //   let size = Math.ceil(data.size / 1024);
      //   this.imgEdit.dimension=size


      // })
      console.log(this.imgEdit,'this.imgEdit')

      this.$forceUpdate()

    },
    //选中项
    Chose(obj) {
      this.imgEdit = JSON.parse(JSON.stringify(obj)) ;
      this.getSize()
      this.xuanzhong = obj.id;
      let index = this.choseList.indexOf(obj.id);
      console.log(index, "this.xuanzhong", obj.select, this.page.tableData);
      if (index != -1) {
        obj.select = false;
        this.choseList.splice(index, 1);
      } else {
        this.choseList.push(obj.id);
        obj.select = true;
      }

      console.log(this.choseList);
      if (this.choseList.length == this.current_num) {
        this.checkAll = true;
      } else {
        this.checkAll = false;
      }
    },
    //全选
    choseAll() {
      if (!this.checkAll) {
        this.choseList=[]
        this.page.tableData.forEach(e=>{
          e.select=false
        })
      } else {
        this.choseList=[]
        this.page.tableData.forEach(e=>{
          e.select=true
          this.choseList.push(e.id)
        })
      }
    },
    //批量下载
    async Download() {
      await exportss(
        {
          api: "admin.resources.downForZip",
          imgIds: this.choseList.toString(),
          exportType: 1,
        },
        "pagegoods"
      );
    },
    isFillList() {
      const totalPage = Math.ceil(
        (this.total - this.page.tableData.length) / this.pageSize
      ); // 总页数
      this.dictionaryNum =
        this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum;
      this.dictionaryNum = this.dictionaryNum < 1 ? 1 : this.dictionaryNum;
      this.getList();
    },
    delclass(e) {
      this.$confirm(
        "该分类下有储存素材文件，确认删除分类及全部素材文件？",
        this.$t("zdata.ts"),
        {
          confirmButtonText: this.$t("zdata.ok"),
          cancelButtonText: this.$t("zdata.off"),
          type: "warning",
        }
      )
        .then(() => {
          this.delclass2(e);
        })
        .catch(() => {});
    },
    delclass2(e) {
      del({
        api: "admin.resources.delCatalogue",
        id: e,
      }).then((res) => {
        if (res.data.code == "200") {
          this.succesMsg(this.$t("zdata.sccg"));
          this.getClassList();
          // 重新查询列表
          this.dictionaryNum = 1;
          this.myclass =-1
          this.getList();
        }
      });
    },
    //批量删除
    Del() {
      this.$confirm(
        "删除该资源，可能会导致资源数据加载失败确认删除？",
        this.$t("zdata.ts"),
        {
          confirmButtonText: this.$t("zdata.ok"),
          cancelButtonText: this.$t("zdata.off"),
          type: "warning",
        }
      )
        .then(() => {
          del({
            api: "admin.resources.del",
            imgIds: this.choseList.toString(),
          }).then((res) => {
            if (res.data.code == "200") {
              this.succesMsg(this.$t("zdata.sccg"));
              this.isFillList();
            }
          });
        })
        .catch(() => {});
    },
  },
};
</script>


<style scoped lang="less">

.container {
  display: flex;
  flex-direction: column;
  .merchants-list {
    flex: 1;
  }
}
.mytop {
  display: flex;
  margin-bottom: 20px;
  .mytop_tab {
    width: 99px;
    height: 40px;
    border-radius: 4px 4px 4px 4px;
    background: #fff;
    margin-right: 10px;
    text-align: center;
    line-height: 40px;
    cursor: pointer;
  }
  .active {
    background: #28b6ff;
    color: #fff;
  }
}
.mybody {
  display: flex;
  padding: 20px 20px 0 20px;
  background: #fff;

  .mybody_l {
    width: 140px;
    background: #f4f7f9;
    border-radius: 4px 4px 0 0;
    margin-right: 20px;
    .mybody_l_top {
      text-align: center;
      padding: 10px 0;
      border-bottom: 1px solid #e9ecef;
    }
    .mybody_l_li_d{
      overflow-x: hidden;
    }
    .mybody_l_li {
      i{
        cursor:pointer;
        font-size: 18px;
        margin-left: 5px;
      }
      padding: 20px;
      border-bottom: 1px solid #e9ecef;
      .mybody_l_li_p{
        display:flex;
      }
      .mybody_l_li_p_sp {
        display: inline-block;
        width: 55px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    .liActive {
      background: #fff;
      color: #28b6ff;
    }
  }
  .mybody_r {
    position: relative;
    width: 100%;
    .mybody_r_Search {
      background: #f4f7f9;
      display: flex;
      padding: 10px;
      margin-bottom: 10px;
      .Search-condition {
        display: flex;
        .query-input {
          display: flex;
          .Search-input {
            margin-right: 10px;
            /deep/.el-input__inner {
              height: 40px;
              line-height: 40px;
            }
          }
        }
        .select-date {
          margin-right: 10px;
        }
      }
    }
    .empty{
      display: flex;
      height: 603px;
      justify-content: center;
      align-items: center;
      .empty1{
        text-align: center;
        p{
          margin-top: 10px;
        }
      }
    }
    .mybody_r_div {
      overflow-x: hidden;
      display: flex;
      justify-content: space-between;
      flex-wrap: wrap;
      .mybody_r_div_d {
        position: relative;
        width: 165px;
        height: 195px;
        margin-bottom: 10px;
        .mybody_r_div_d_img {
          width: 165px;
          height: 165px;
          border-radius: 4px 4px 4px 4px;
        }
        .mybody_r_div_d_p {
          text-align: center;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .mybody_r_div_d_i {
          position: absolute;
          top: 10px;
          right: 10px;
        }
      }
      .mybody_r_div_i {
        width: 165px;
      }
      .div_active {
        width: 161px;
        height: 161px;
        border: 2px solid #70aff3;
      }
    }
    // .mybody_r_div:after{
    //   content:'';
    //   flex:auto;
    // }
    .tabFoot {
      padding: 0 20px;
      display: flex;
      height: 76px;
      line-height: 76px;
      justify-content: space-between;
      border-top: 1px solid #e9ecef;
      position: relative;
      left: -20px;
      .tabFoot_s {
        margin-left: 10px;
      }
    }
    .list_right {
      z-index: 20;
      overflow-x: hidden;
      position: absolute;
      top: 70px;
      right: 0;
      width: 350px;
      background: #f4f7f9;
      border-radius: 4px 4px 4px 4px;
      padding: 0 20px;
      .list_right_p {
        height: 60px;
        line-height: 60px;
        border-bottom: 1px solid #d5dbe8;
        margin-bottom: 20px;
      }
      .list_right_img {
        text-align: center;
        margin-bottom: 20px;
        border-bottom: 1px solid #d5dbe8;
        img {
          width: 175px;
          height: 175px;
          background: #ffffff;
          border-radius: 4px 4px 4px 4px;
          margin-bottom: 20px;
        }
        .mybody_r_div_d_img {
          width: 175px;
          height: 175px;
          background: #ffffff;
          border-radius: 4px 4px 4px 4px;
          margin-bottom: 20px;

        }
      }
      .list_right_div {
      }
      .rulesave{
        float:right;
      }
    }
  }
}
/deep/.el-dialog__header {
  height: 60px;
  border-bottom: 1px solid #e9ecef;
}
.dia_top {
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 20px;
  .dia_top_h {
    display: flex;
    /deep/.el-input {
      width: 400px;
      margin-right: 10px;
      .el-input__inner {
        height: 40px;
        line-height: 40px;
        width: 400px;
      }
    }
  }
  .dia_top_p {
    color: #ff0000;
    margin-top: 10px;
  }
}
.dia_p {
  height: 60px;
  line-height: 60px;
  font-size: 16px;
  border-bottom: 1px solid #e9ecef;
  span {
    font-size: 14px;
    color: #888f9e;
  }
}
.dia_div {
  overflow-x: hidden;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  max-height: 310px;
  .dia_div_li_i{
    width:25%;
  }
  .dia_div_li {
    width: 25%;
    box-sizing: border-box;

    height: 60px;
    padding: 20px;
    .dia_div_li_p{
      display: flex;
      .p_span{
        display: inline-block;
        width: 55px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin: -6px 0;
      }
      i {
        cursor:pointer;
        font-size: 18px;
        margin-left: 10px;
      }
      /deep/.el-input{
        position: relative;
        top: -4px;
        left:5px;
      }
    }

  }
}
.btn-list {
  flex: 1;
  .bgColor {
    background-color: #2890ff;
  }
  .bgColor:hover {
    background-color: #70aff3;
  }
  .fontColor {
    color: #6a7076;
    border: 1px solid #d5dbe8;
    background-color: #fff;
  }
  .fontColor:hover {
    color: #2890ff;
    border: 1px solid #2890ff;
    background-color: #fff;
  }
  .export {
    position: absolute;
    right: 20px;
  }
}
.upimg_d_f{
  /deep/ .el-upload-dragger {
            width: 460px;
            margin: 0 auto;
            background: #f4f7f9;
          }
}

/deep/.el-dialog__footer {
  border-top: 1px solid #e9ecef;
  height: 60px;
}
.dialog-block2 {
  /deep/.el-dialog {
    width: 500px;
  }
}
.dialog-block3 {
  /deep/.el-dialog {
    width: 400px;
  }
}
// .el-form-item{
//   position: relative;
// }
// .mycopy{
//   position: absolute;
//   left: 80px;
//   top: 10px;
// }
</style>

