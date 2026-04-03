<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.code"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('merchants.merchantslist.enter_mall_name')"
          ></el-input>
          <div class="select-date">
            <el-date-picker
              v-model="inputInfo.range"
              type="datetimerange"
              :range-separator="$t('reportManagement.businessReport.zhi')"
              :start-placeholder="$t('reportManagement.businessReport.ksrq')"
              :end-placeholder="$t('reportManagement.businessReport.jsrq')"
              value-format="yyyy-MM-dd HH:mm:ss"
              :editable="false"
            ></el-date-picker>
          </div>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button
            class="bgColor"
            type="primary"
            v-enter="demand"
            @click="demand"
          >{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
          <el-button class="bgColor export" type="primary" @click="dialogShow2">{{$t('DemoPage.tableExamplePage.export')}}</el-button>
        </div>
      </div>
    </div>
    <div class="jump-list">
      <el-button
      class="bgColor laiketui laiketui-add"
      type="primary"
      @click="addMall"
    >{{$t('merchants.merchantslist.tjsc')}}</el-button>
    </div>
    <div class="merchants-list" ref="tableFather">
      <!-- tableHeight 515-->
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="loading"
        :data="tableData"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
      >
      <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column prop="id" :label="$t('merchants.merchantslist.scID')">
          <template slot-scope="scope">
            {{ scope.row.id }}
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('merchants.merchantslist.scmc')" width="140px" show-overflow-tooltip></el-table-column>
        <el-table-column prop="mobile" :label="$t('merchants.merchantslist.sj')" width="140px"></el-table-column>
        <el-table-column prop="price" :label="$t('merchants.merchantslist.jg')"></el-table-column>
        <el-table-column prop="company" :label="$t('merchants.merchantslist.gsmc')" width="140px" show-overflow-tooltip></el-table-column>
        <el-table-column prop="add_date" :label="$t('merchants.merchantslist.gmsj')" width="210px">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="end_date" :label="$t('merchants.merchantslist.dqsj')" width="210px">
          <template slot-scope="scope">
            <span>{{ scope.row.end_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="roleName" :label="$t('merchants.merchantslist.js')" show-overflow-tooltip></el-table-column>
        <el-table-column prop="state" :label="$t('merchants.merchantslist.zt')">
          <template slot-scope="scope">
            <span
              class="state"
              :class="scope.row.status === 0 ? 'active1' : 'active2' "
            >{{ scope.row.status === 0 ? $t('merchants.merchantslist.sxz') : scope.row.status === 2 ? $t('merchants.merchantslist.ysd') : $t('merchants.merchantslist.ydq') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchants.merchantslist.swmr')">
          <template slot-scope="scope">
            <!-- v-if="scope.row.status === 0 || scope.row.status === 2" 本来是限制只有生效中的商城才可以设置为默认 改需求都需要设置 -->
            <el-switch
              :disabled="scope.row.is_default == 1"
              v-model="scope.row.is_default"
              @change="switchs(scope.row)"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            ></el-switch>
          </template>
        </el-table-column>

        <el-table-column fixed="right" :label="$t('DemoPage.tableExamplePage.operation')" width="310" >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                  <el-button
                  style="min-width:82px;height:24px"
                  icon="el-icon-lock"
                  @click="Lock(scope.row)"
                >{{ scope.row.status === 0  ? $t('merchants.merchantslist.sd') : $t('merchants.merchantslist.qy') }}</el-button>
                <el-button style="min-width:82px;height:24px" icon="el-icon-edit-outline" @click="Edit(scope.row)">{{ $t('merchants.merchantslist.edit') }}</el-button>
                <el-button style="min-width:82px;height:24px" :disabled="scope.row.status == 0 || scope.row.status == 2" v-if="scope.row.is_default != 1" icon="el-icon-delete" @click="Delete(scope.row)">{{ $t('merchants.merchantslist.delete') }}</el-button>
                <el-button style="min-width:82px;height:24px" v-if="scope.row.is_default == 1" icon="el-icon-edit-outline" @click="dialogShow(scope.row)">{{ $t('merchants.merchantslist.mmcz') }}</el-button>
              </div>
              <div class="OP-button-bottom">
                <el-button style="min-width:82px;height:24px" class="laiketui laiketui-jinru" @click="enterSystem(scope.row)">{{ $t('merchants.merchantslist.jrxt') }}</el-button>
                <!-- 禅道bug https://cd.houjiemeishi.com/bug-view-2134.html 有描述隐藏原因 -->
                <!-- <el-button style="min-width:82px;height:24px" class="laiketui laiketui-jinru" @click="enterH5Home(scope.row)">{{ $t('merchants.merchantslist.jrH5') }}</el-button> -->
                <el-button style="min-width:82px;height:24px" v-if="scope.row.is_default != 1" icon="el-icon-edit-outline" @click="dialogShow(scope.row)">{{ $t('merchants.merchantslist.mmcz') }}</el-button>
                <div v-if="scope.row.is_default == 1" style="min-width: 82px;margin-right: 8px;"></div>
              </div>
            </div>
		      </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="showPagebox">
        <div class="pageLeftText">{{$t('DemoPage.tableExamplePage.show')}}</div>
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
          <div class="pageRightText">{{$t('DemoPage.tableExamplePage.on_show')}}{{currpage}}-{{current_num}}{{$t('DemoPage.tableExamplePage.twig')}}{{total}}{{ $t('DemoPage.tableExamplePage.twig_notes') }}</div>
        </el-pagination>
      </div>
    </div>

    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog :title="title" :visible.sync="dialogVisible" :before-close="handleClose">
        <el-form
          :model="ruleForm2"
          :rules="rules2"
          ref="ruleForm2"
          label-width="auto"
          class="demo-ruleForm"
        >
          <div class="pass-input">
            <el-form-item
              :label="$t('merchants.merchantslist.sczh')"
              prop="title"
            >
              <el-input v-model="ruleForm2.title" :disabled="true"></el-input>
            </el-form-item>
            <el-form-item :label="$t('merchants.merchantslist.xmm')" prop="newPassword">
              <el-input v-model="ruleForm2.newPassword" show-password></el-input>
            </el-form-item>
            <el-form-item :label="$t('merchants.merchantslist.qrmm')" prop="confirmPassword">
              <el-input v-model="ruleForm2.confirmPassword" show-password></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button @click="handleDetecancel" class="qxcolor">{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
              <el-button type="primary" @click="determine('ruleForm2')" class="qdcolor">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>

    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog :title="$t('DemoPage.tableExamplePage.export_data')" :visible.sync="dialogVisible2" :before-close="handleClose2">
        <div class="item" @click="exportPage">
          <i class="el-icon-document"></i>
          <span>{{$t('DemoPage.tableExamplePage.export_page')}}</span>
        </div>
        <div class="item item-center" @click="exportAll">
          <i class="el-icon-document-copy"></i>
          <span>{{$t('DemoPage.tableExamplePage.export_all')}}</span>
        </div>
        <div class="item" @click="exportQuery">
          <i class="el-icon-document"></i>
          <span>{{$t('DemoPage.tableExamplePage.export_query')}}</span>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {
  getShopInfo,
  resetAdminPwd,
  delStore,
  setStoreOpenSwitch,
  setStoreDefaultSwitch,
} from "@/api/Platform/merchants";
import { getRoleMenu } from "@/api/Platform/permissions";
import { setStorage, getStorage } from "@/utils/storage";
import { exports } from "@/api/export/index";
import { mixinstest } from "@/mixins/index";
import { setUserAdmin } from '@/api/Platform/merchants'
import { getThridParmate} from '@/api/Platform/parameter'

export default {
  name: "shoppinglist",

  mixins: [mixinstest],
  data() {
    var validatePass3 = (rule, value, callback) => {
        console.log('8148148148', /^[\da-z]+$/i.test(value));
        if (/^[\da-z]+$/i.test(value)) {
          callback();
        } else {
          callback(new Error("只能输入数字和字母"));
        }
      };
    var validatePass = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请输入密码"));
      } else {
        if (this.ruleForm2.confirmPassword !== "") {
          this.$refs.ruleForm2.validateField("confirmPassword");
        }
        callback();
      }
    };
    var validatePass2 = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请再次输入密码"));
      } else if (value !== this.ruleForm2.newPassword) {
        callback(new Error("两次输入密码不一致!"));
      } else {
        callback();
      }
    };
    return {
      tableData: [],
      loading: true,
      inputInfo: {
        code: "",
        range: "",
      },
      // 弹框数据
      dialogVisible: false,
      title: "",
      adminId: "",
      ruleForm2: {
        title: "",
        newPassword: "",
        confirmPassword: "",
      },
      rules2: {
        title: [
          {
            required: true,
            message: this.$t("merchants.merchantslist.qqrmm"),
            trigger: "blur",
          },
        ],
        newPassword: [
          { required: true, message: this.$t('merchants.merchantslist.qtxmm'), trigger: "blur" },
          {
            min: 6,
            max: 16,
            message: "长度在 6 到 16 个字符",
            trigger: "blur",
          },
          { validator: validatePass, trigger: "blur" },
          {
              validator: validatePass3,
              trigger: "change"
            }
        ],
        confirmPassword: [
          { required: true, message: this.$t('merchants.merchantslist.qqrmm'), trigger: "blur" },
          {
            min: 6,
            max: 16,
            message: "长度在 6 到 16 个字符",
            trigger: "blur",
          },
          { validator: validatePass2, trigger: "blur", required: true },
          {
              validator: validatePass3,
              trigger: "change"
            }
        ],
      },

      // table高度
      tableHeight: null,

      // 导出弹框数据
      dialogVisible2: false,
      storeIdPrefix:false,
    };
  },

  created() {
    if(this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
      this.inputInfo = this.$route.params.inputInfo
    }
    this.getShopInfos();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    addMall(){
      if(this.storeIdPrefix == false){
      this.$confirm('您还没有设置商城ID前缀，请前往系统设置页设置！', '提示', {
        confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
      })
        .then(() => {
          this.$router.push('/mall/basicConfiguration')
        })
        .catch(() => {})
      }else{
        this.$router.push('/Platform/merchants/addmerchants')
      }
    },
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      },
    // 获取商城列表
    async getShopInfos() {
      const res = await getShopInfo({
        api: "saas.shop.getShopInfo",
        storeType: 8,
        storeId: null,
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        storeName: this.inputInfo.code,
        startDate: this.inputInfo.range[0],
        endDate: this.inputInfo.range[1],
      });
      this.total = res.data.data.total;
      this.storeIdPrefix = res.data.data.storeIdPrefix
      this.tableData = res.data.data.dataList;
      this.loading = false;
      if (res.data.data.total < 10) {
        this.current_num = this.total;
      }
    },

    // 重置
    reset() {
      this.inputInfo.code = "";
      this.inputInfo.range = "";
    },

    // 查询
    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.getShopInfos().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    // 导出
    derive() {},

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e;
      this.pageSize = e;
      this.getShopInfos().then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
        this.loading = false;
      });
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.getShopInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    // 锁定
    Lock(value) {
      if (value.status === 0) {
        this.$confirm(this.$t('merchants.merchantslist.sdts'), this.$t('merchants.merchantslist.ts'), {
          confirmButtonText: this.$t('merchants.merchantslist.save'),
          cancelButtonText: this.$t('merchants.merchantslist.cancel'),
          type: "warning",
        })
          .then(() => {
            setStoreOpenSwitch({
              api: "saas.shop.setStoreOpenSwitch",
              id: value.id,
            }).then((res) => {
              if (res.data.code == 200) {
                this.getShopInfos();
                this.$message({
                  type: "success",
                  message: this.$t('zdata.sdcg'),
                  offset: 102,
                });
              }
            });
          })
          .catch(() => {
            // this.$message({
            //   type: "info",
            //   message: "已取消",
            // });
          });
      } else if (value.status === 1) {
        this.$confirm(this.$t('merchants.merchantslist.qyts'), this.$t('merchants.merchantslist.ts'), {
          confirmButtonText: this.$t('merchants.merchantslist.save'),
          cancelButtonText: this.$t('merchants.merchantslist.cancel'),
          type: "warning",
        })
          .then(() => {
            setStoreOpenSwitch({
              api: "saas.shop.setStoreOpenSwitch",
              id: value.id,
            }).then((res) => {
              this.getShopInfos();
              // this.$message({
              //   message: res.data.message,
              //   type: "error",
              //   offset: 100,
              // })
            });
          })
          .catch(() => {
            // this.$message({
            //   type: "info",
            //   message: "已取消",
            // });
          });
      } else {
        this.$confirm(this.$t('merchants.merchantslist.jcsdts'), this.$t('merchants.merchantslist.ts'), {
          confirmButtonText: this.$t('merchants.merchantslist.save'),
          cancelButtonText: this.$t('merchants.merchantslist.cancel'),
          type: "warning",
        })
          .then(() => {
            setStoreOpenSwitch({
              api: "saas.shop.setStoreOpenSwitch",
              id: value.id,
            }).then((res) => {
              if (res.data.code == "200") {
                this.getShopInfos();
                this.$message({
                  type: "success",
                  message: this.$t('zdata.qycg'),
                  offset: 102,
                });
              }
            });
          })
          .catch(() => {
            // this.$message({
            //   type: "info",
            //   message: "已取消",
            // });
          });
      }
    },

    // 编辑
    Edit(value) {
      this.$router.push({
        name: "editormerchants",
        params: value,
        query: {
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
          inputInfo: this.inputInfo
        }
      });
    },

    // 删除
    Delete(value) {
      console.log(value);
      this.$confirm(this.$t('merchants.merchantslist.scts'), this.$t('merchants.merchantslist.ts'), {
        confirmButtonText: this.$t('merchants.merchantslist.save'),
        cancelButtonText: this.$t('merchants.merchantslist.cancel'),
        type: "warning",
      })
        .then(() => {
          delStore({
            api: "saas.shop.delStore",
            id: value.id,
          }).then((res) => {
            if (res.data.code == "200") {
              this.getShopInfos();
              this.$message({
                type: "success",
                message: this.$t('zdata.sccg'),
                offset:102,
              });
            }
          });
        })
        .catch(() => {
          // this.$message({
          //   type: "info",
          //   message: "已取消删除",
          // });
        });
    },

    async enterH5Home(value){
      let params = Object.assign({
          api: 'saas.authorize.getThridParmate'
      },this.ruleForm);
      const res = await getThridParmate(params);
      // debugger
      let data = res.data.data.list;
      if(!data){
        this.errorMsg("未配置平台参数");
          return;
      }
      let domain = data.work_domain;
      if(!domain){
        this.errorMsg( "平台参数未配置H5访问地址");
          return;
      }
      let h5Url = domain +  "?store_id=" + value.id +'&eliminate='+true;

      var a = document.createElement("a");
      a.setAttribute("href", h5Url);
      a.setAttribute("target", "_blank");
      a.setAttribute("id", "camnpr");
      document.body.appendChild(a);
      a.click();
    },

    // 进入系统
    enterSystem(value) {
      const laike_admin_userInfo = getStorage("laike_admin_userInfo");
      const rolesInfo = getStorage("rolesInfo");
      getShopInfo({
        api: "saas.shop.getShopInfo",
        storeType: 8,
        storeId: value.id,
      }).then((res) => {
        const info = res.data.data.dataList[0];
        console.log(info);
        laike_admin_userInfo.storeId = info.id;
        rolesInfo.storeId = info.id;
        setStorage("laike_admin_userInfo", laike_admin_userInfo);
        console.log('574574574574',rolesInfo);
        setStorage("rolesInfo", rolesInfo);
        this.$store.commit("user/SET_MERCHANTSLOGO", info.merchant_logo);
        setStorage("laike_head_img", info.merchant_logo);

        setUserAdmin({
          api: 'admin.saas.user.setUserAdmin',
        })
        .then(response => {
          const laike_admin_userInfo = getStorage('laike_admin_userInfo')
          laike_admin_userInfo.mchId = response.data.data.mchId
          setStorage('laike_admin_userInfo',laike_admin_userInfo)
          resolve(response)
        })
        
        this.$router.go(0);
      });
    },

    // 开关
    switchs(value) {
      console.log('593593593',value);
      setStoreDefaultSwitch({
        api: "saas.shop.setStoreDefaultSwitch",
        store_id: value.id,
      }).then((res) => {
        if (res.data.code == "200") {
          this.$message({
            message: this.$t('zdata.czcg')+this.$t('zdata.ysz')+value.name+this.$t('zdata.wgly'),
            type: "success",
            offset: 102,
          });
        }
        this.getShopInfos();
      });
    },

    // 弹框方法
    dialogShow(value) {
      (this.ruleForm2.dataName = ""),
      (this.ruleForm2.status = "1"),
      (this.ruleForm2.title = value.adminName),
      (this.dialogVisible = true);
      this.title = value.name + this.$t('merchants.merchantslist.mmcz');
      this.adminId = value.admin_id;
    },

    handleClose(done) {
      this.dialogVisible = false;
      this.ruleForm2.newPassword =""
      this.ruleForm2.confirmPassword =""
    },
    handleDetecancel(){
      this.dialogVisible = false
      this.ruleForm2.newPassword =""
      this.ruleForm2.confirmPassword =""
    },

    // 修改密码
    determine(formName2) {
      this.$refs[formName2].validate(async (valid) => {
        if (valid) {
          try {
            resetAdminPwd({
              api: "saas.shop.resetAdminPwd",
              adminId: this.adminId,
              pwd: this.ruleForm2.newPassword,
            }).then((res) => {
              console.log(res);
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t('merchants.merchantslist.cg'),
                  type: "success",
                  offset: 102,
                });
                this.dialogVisible = false;
                // this.$refs[formName2]

              }
            });
          } catch (error) {
            // this.$message({
            //   message: "密码不能为空",
            //   type: "error",
            //   offset: 100,
            // });
          } finally{
            this.ruleForm2.newPassword =""
            this.ruleForm2.confirmPassword =""
          }
        } else {
          // this.$message({
          //   message: "确认密码不能为空",
          //   type: "error",
          //   offset: 100,
          // });
          // return false;
        }
      });
    },

    // 弹框方法
    dialogShow2() {
      this.dialogVisible2 = true;
    },

    handleClose2(done) {
      this.dialogVisible2 = false;
    },

    async exportPage() {
      exports(
        {
          api: "saas.shop.getShopInfo",
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          storeId: 0,
          storeName: this.inputInfo.code,
          startDate: this.inputInfo.range[0],
          endDate: this.inputInfo.range[1],
        },
        "userlist"
      );
    },

    async exportAll() {
      exports(
        {
          api: "saas.shop.getShopInfo",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          storeId: 0,
          pageSize:99999
          // storeName: this.inputInfo.code,
          // startDate: this.inputInfo.range[0],
          // endDate: this.inputInfo.range[1],
        },
        "userlist"
      );
    },

    async exportQuery() {
      exports(
        {
          api: "saas.shop.getShopInfo",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          storeId: 0,
          storeName: this.inputInfo.code,
          startDate: this.inputInfo.range[0],
          endDate: this.inputInfo.range[1],
        },
        "userlist"
      );
    },
  },
};
</script>

<style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
  /deep/.Search {
    .Search-condition {
      display: flex;
      align-items: center;
      .query-input {
        display: flex;
        margin-right: 10px;
        .Search-input {
          margin-right: 10px;
        }
      }
    }
  }

  /deep/.jump-list {
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
  }

  .merchants-list {
    flex: 1;
    background: #ffffff !important;
    border-radius: 4px;
    /deep/.el-table__header {
      thead {
        tr {
          th {
            height: 61px;
            text-align: center;
            font-size: 14px;
            font-weight: bold;
            color: #414658;
          }
        }
      }
    }
    /deep/.el-table__body {
      tbody {
        tr {
          td {
            height: 92px;
            text-align: center;
            font-size: 14px;
            color: #414658;
            font-weight: 400;
          }
        }
      }
    }

    /deep/.el-table {
      .OP-button {
        display: flex;
        flex-direction: column;
        // align-items: center;
        .OP-button-top {
          // margin-bottom: 8px;
          display: flex;
          justify-content: start;
        }

        .OP-button-bottom {
          display: flex;
          justify-content: start;
          .laiketui-jinru:before {
            margin-right: 6px;
          }
        }
      }
    }
    .active1 {
      color: #00ce6d;
    }
    .active2 {
      color: #ff453d;
    }
  }

  .dialog-block {
   /* 弹框样式 */
    /deep/.el-dialog {
      width: 580px;
      height: 370px;
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
        border-bottom: 1px solid #e9ecef;
        padding: 41px 60px 16px 60px !important;
        .pass-input {
          .el-form {
            width: 340px;
            .el-form-item {
              width: 340px;
              height: 40px;
              .el-form-item__content {
                .el-input {
                  width: 340px;
                  height: 40px;
                  input {
                    width: 340px;
                    height: 40px;
                  }
                }
              }
            }
          }
        }
        .form-footer {
          width: 174px;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
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
}
</style>
