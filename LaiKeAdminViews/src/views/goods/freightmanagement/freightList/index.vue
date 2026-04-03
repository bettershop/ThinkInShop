<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">

          <el-select
            class="select-input"
            v-model="inputInfo.lang_code"
            :placeholder="$t('qxzyz')"
          >
            <el-option
              v-for="item in languages"
              :key="item.lang_code"
              :label="item.lang_name"
              :value="item.lang_code"
            >
            </el-option>
          </el-select>

          <el-select
            class="select-input"
            v-model="inputInfo.status"
            :placeholder="$t('freightlist.qxzyf')"
          >
            <el-option
              v-for="item in Dictionary"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-select
            class="select-input"
            v-model="inputInfo.type"
            :placeholder="$t('freightlist.qxzjs')"
          >
            <!-- <el-option :label="$t('freightlist.mr')" value="2"></el-option> -->
            <el-option :label="$t('freightlist.jian')" value="0"></el-option>
            <el-option :label="$t('freightlist.zl')" value="1"></el-option>
          </el-select>
          <el-input
            v-model="inputInfo.name"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('freightlist.qsrmb')"
          ></el-input>
          <!-- <el-input v-model="inputInfo.name" size="medium" @keyup.enter.native="demand" class="Search-input" placeholder="请输入运费名称"></el-input> -->
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand">{{
            $t("DemoPage.tableExamplePage.demand")
          }}</el-button>
        </div>
      </div>
    </div>
    <div class="jump-list">
      <el-button
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="addTemplate"
      >
        {{ $t("freightlist.tjmc") }}</el-button
      >
      <el-button
        class="fontColor"
        @click="delAll"
        :disabled="is_disabled"
        icon="el-icon-delete"
      >
        {{ $t("freightlist.plsc") }}</el-button
      >
    </div>
    <div class="dictionary-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
      >
        <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
          </div>
        </template>
        <el-table-column type="selection" width="48"> </el-table-column>
        <!-- <el-table-column prop="id" :label="id">
        </el-table-column> -->
        <el-table-column prop="name" :label="$t('freightlist.yfmc')">
        </el-table-column>
        <el-table-column prop="lang_name" :label="$t('yz')"> </el-table-column>
        <el-table-column prop="type" :label="$t('freightlist.jsfs')">
          <template slot-scope="scope">
            <span v-if="scope.row.type == 1">{{ $t("freightlist.zl") }}</span>
            <span v-if="scope.row.type == 0">{{ $t("freightlist.jian") }}</span>
            <span v-if="scope.row.type == 2">{{ $t("freightlist.mr") }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('freightlist.swmr')">
          <template slot-scope="scope">
            <el-switch

              v-model="scope.row.is_default"
              @change="switchs(scope.row)"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="is_use" :label="$t('freightlist.zt')">
          <template slot-scope="scope">
            <span>{{
              scope.row.is_use == 1
                ? $t("freightlist.ysy")
                : $t("freightlist.wsy")
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="" :label="$t('freightlist.mbgz')">
          <template slot-scope="{row}">
            <span>{{ row.rule }} - {{ row.rule1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="add_time" :label="$t('freightlist.tjsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('freightlist.cz')"
          width="300"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  icon="el-icon-view"
                  v-if="scope.row.is_use === 1"
                  @click="details(scope.row)"
                  >{{ $t("freightlist.ck") }}</el-button
                >
                <el-button
                  icon="el-icon-edit-outline"
                  v-if="scope.row.is_use !== 1"
                  @click="Edit(scope.row)"
                  >{{ $t("freightlist.bianji") }}</el-button
                >
                <!-- :disabled="scope.row.is_default" -->
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">{{
                  $t("freightlist.shanchu")
                }}</el-button>
                <!-- 关联商品 -->
                <el-button   @click="relatedGoods(scope.row)">{{
                  $t("freightlist.glsp")
                }}</el-button>
              </div>
              <!-- <div class="OP-button-bottom">
                <el-button
                  icon="el-icon-edit-outline"
                  v-if="scope.row.is_use !== 1"
                  @click="Edit(scope.row)"
                  >编辑</el-button
                >
              </div> -->
            </div>
          </template>
        </el-table-column>
      </el-table>
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
  </div>
</template>

<script>
import {
  getFreightInfo,
  delFreight,
  freightSetDefault,
} from "@/api/goods/freightManagement";
import { getStorage } from "@/utils/storage";
import { mixinstest } from "@/mixins/index";
import { getButton } from "@/api/layout/information";
export default {
  name: "freightList",
  mixins: [mixinstest],
  data() {
    return {
      tableData: [],
      languages:[],
      loading: true,
      is_disabled: true,
      idList: [],
      inputInfo: {
        status: "",
        name: "",
        lang_code: "",
        type: "",
      },
      Dictionary: [
        {
          value: "0",
          label: this.$t("freightlist.wsy"),
        },
        {
          value: "1",
          label: this.$t("freightlist.ysy"),
        },
      ],
      button_list: [],
      freight: null,
      // table高度
      tableHeight: null,
    };
  },

  created() {
  //   if (this.$route.params.pageSize) {
  //     this.pagination.page = this.$route.params.dictionaryNum;
  //     this.dictionaryNum = this.$route.params.dictionaryNum;
  //     this.pageSize = this.$route.params.pageSize;
  //   }
    this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal();
    this.getFreightInfos();
    this.getButtons();
    this.getLanguage();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    async getLanguage() {
      try {
        const result = await this.LaiKeCommon.getLanguages();
        this.languages = result.data.data;
      } catch (error) {
        console.error('获取语种列表失败:', error);
      }
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "freightmanagement") {
          return (this.menuId = item.id);
        }
      });
      let buttonList = await getButton({
        api: "saas.role.getButton",
        menuId: this.menuId,
      });
      this.button_list = buttonList.data.data;
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      // debugger
      console.log(this.$refs.tableFather.clientHeight);
    },

    async getFreightInfos() {
      const res = await getFreightInfo({
        api: "admin.AdminFreight.AdminGetFreightInfo",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        status: this.inputInfo.status,
        name: this.inputInfo.name,
        lang_code: this.inputInfo.lang_code,
        type: this.inputInfo.type,
        mchId: getStorage("laike_admin_userInfo").mchId,
      });
      console.log(res);
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.loading = false;
      this.freight = res.data.data.list?.[0]?.freight;
      this.sizeMeth()
      console.log(this.freight);
    },
    sizeMeth(){
      this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
      this.current_num =
        this.tableData.length === this.pageSize
          ? this.dictionaryNum * this.pageSize
          : this.total
    },
    addTemplate(){
      if(getStorage("laike_admin_userInfo").mchId == 0){
        this.$message({
          type: "error",
          message: this.$t("plugInsSet.plugInsList.qtjdp"),
          offset: 102,
        });
        this.$router.push("/mall/fastBoot/index");
      } else {
        this.$router.push('/goods/freightmanagement/addFreight')
      }
    },
    reset() {
      this.inputInfo.status = "";
      this.inputInfo.name = "";
      this.inputInfo.type = "";
      this.inputInfo.lang_code = "";
    },

    demand() {
      // this.currpage = 1;
      // this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.getFreightInfos().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    // 设置默认
    switchs(value) {
      console.log(value);
      freightSetDefault({
        api: "admin.goods.freightSetDefault",
        id: value.id,
      }).then((res) => {
        if (res.data.code == "200") {
          if (value.is_default) {
            this.$message({
              message: this.$t("zdata.czcg"),
              type: "success",
              offset: 102,
            });
          } else {
            this.$message({
              message: this.$t("freightlist.zsblygmryf"),
              type: "error",
              offset: 102,
            });
          }

          this.getFreightInfos();
          console.log(res);
        }
      });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.getFreightInfos().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.getFreightInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    Edit(value) {
      this.$router.push({
        name: "editFreight",
        query: {
          id: value.id,
          lang_code: value.lang_code
        },
        params: value,
      });
    },
    relatedGoods(row){
      this.$router.push({
        path: "/goods/freightmanagement/freightGoodsList",
        query: {
          id: row.id,
        }
      })
    },
    Delete(value) {
      console.log(value);
      this.$confirm(this.$t("freightlist.qrsc"), this.$t("freightlist.ts"), {
        confirmButtonText: this.$t("freightlist.okk"),
        cancelButtonText: this.$t("freightlist.ccel"),
        type: "warning",
      })
        .then(() => {
          delFreight({
            api: "admin.goods.delFreight",
            idList: value.id,
          }).then((res) => {
            if (res.data.code == "200") {
              console.log(res);
              if (this.dictionaryNum != 1 && this.tableData.length <= 1) {
                this.dictionaryNum--;
              }
              this.getFreightInfos();
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
                offset: 102,
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

    details(value) {
      this.$router.push({
        name: "addFreight",
        query: {
          name: "view",
          id: value.id,
        },
        params: value,
      });
    },

    // 选框改变
    handleSelectionChange(val) {
      if (val.length == 0) {
        this.is_disabled = true;
      } else {
        this.is_disabled = false;
      }
      console.log(val);
      this.idList = val.map((item) => {
        return item.id;
      });
      this.idList = this.idList.join(",");
    },

    // 批量删除
    delAll() {
      this.$confirm(this.$t("freightlist.qrsc"), this.$t("freightlist.ts"), {
        confirmButtonText: this.$t("freightlist.okk"),
        cancelButtonText: this.$t("freightlist.ccel"),
        type: "warning",
      })
        .then(() => {
          delFreight({
            api: "admin.goods.delFreight",
            freightIds: this.idList,
          }).then((res) => {
            if (res.data.code == "200") {
              console.log(res);
              if (
                (this.dictionaryNum != 1 && this.tableData.length <= 1) ||
                this.idList >= this.tableData.length
              ) {
                this.dictionaryNum--;
              }
              this.getFreightInfos();
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
                offset: 102,
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
  },
};
</script>

<style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
  /deep/.Search {
    .select-input {
      margin-right: 10px;
    }
    display: flex;
    align-items: center;
    background: #ffffff;
    border-radius: 4px;
    padding: 10px;
    margin-bottom: 16px;
    .Search-condition {
      display: flex;
      align-items: center;
      .query-input {
        display: flex;
        // margin-right: 10px;
        .Search-input {
          width: 280px;
          margin-right: 10px;
          .el-input__inner {
            height: 40px;
            line-height: 40px;
            border: 1px solid #d5dbe8;
          }
          .el-input__inner:hover {
            border: 1px solid #b2bcd4;
          }
          .el-input__inner:focus {
            border-color: #409eff;
          }
          .el-input__inner::-webkit-input-placeholder {
            color: #97a0b4;
          }
        }
      }

      .btn-list {
        .bgColor {
          background-color: #2890ff;
        }
        .bgColor:hover {
          background-color: #70aff3;
        }
        .fontColor {
          color: #6a7076;
          background-color: #fff;
          border: 1px solid #d5dbe8;
        }
        .fontColor:hover {
          background-color: #fff;
          color: #2890ff;
          border: 1px solid #2890ff;
        }
        .export {
          position: absolute;
          right: 30px;
        }
      }
    }
  }
  .jump-list {
    width: 100%;
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
    .bgColor {
      min-width: 120px;
      height: 40px;
      background: #28b6ff;
      border-radius: 4px;
      border: none;
      font-size: 14px;
    }
  }

  .dictionary-list {
    flex: 1;
    background: #ffffff;
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
  }
}
</style>
