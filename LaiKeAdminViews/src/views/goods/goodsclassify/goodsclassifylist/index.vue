<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.name"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('goodsclassifylist.qsrfl')"
          ></el-input>
          <el-select
            class="select-input"
            :placeholder="$t('qxzyz')"
            v-model="inputInfo.lang_code"
          >
            <el-option
              v-for="item in languages"
              :key="item.lang_code"
              :label="item.lang_name"
              :value="item.lang_code"
            >
            </el-option>
          </el-select>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand">{{
            $t("DemoPage.tableExamplePage.demand")
          }}</el-button>
            <el-button
              v-has-permi="'export'"
              class="bgColor export"
              type="primary"
              @click="dialogShow"
              >{{ $t("DemoPage.tableExamplePage.export") }}</el-button
            >
        </div>
      </div>
    </div>
    <div>
      <div class="jump-list">
        <el-button
          v-has-permi="'addClass'"
          class="bgColor laiketui laiketui-add"
          type="primary"
          @click="addClasss"
          >{{ $t("goodsclassifylist.xzfl") }}
        </el-button >
      </div>
    </div>
    <div class="menu-list" ref="tableFather">
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
            <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
          </div>
        </template>
        <el-table-column prop="cid" :label="$t('goodsclassifylist.flid')">
        </el-table-column>
        <el-table-column prop="img" :label="$t('goodsclassifylist.fltp')">
          <template slot-scope="scope">
            <img :src="scope.row.img" alt="" @error="handleErrorImg"/>
          </template>
        </el-table-column>
        <el-table-column prop="lang_name" :label="$t('yz')"> </el-table-column>
        <el-table-column prop="pname" :label="$t('goodsclassifylist.flmc')">
        </el-table-column>
        <el-table-column prop="level" :label="$t('goodsclassifylist.fljb')">
          <template>
            <span>{{ $t("goodsclassifylist.yij") }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('goodsclassifylist.tjsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('goodsclassifylist.cz')"
          width="300"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  class="laiketui laiketui-zhiding"
                  @click="placedTop(scope.row)"
                  v-has-permi="'topping'"
                  v-if="(scope.$index > 0 || dictionaryNum !== 1)"
                >
                  {{ $t("goodsclassifylist.zd") }} </el-button
                >
                <el-button
                  v-has-permi="'editClass'"
                  icon="el-icon-edit-outline"
                  @click="Edit(scope.row)"
                  >{{ $t("goodsclassifylist.bianji")}}</el-button
                >
                <el-button
                  v-has-permi="'deleteClass'"
                  icon="el-icon-delete"
                  @click="Delete(scope.row)"
                  >{{ $t("goodsclassifylist.shanchu")}}</el-button
                >
              </div>
              <div class="OP-button-bottom">
                <el-button
                  icon="el-icon-view"
                  v-has-permi="'viewClass'"
                  @click="viewlower(scope.row)"
                >
                  {{ $t("goodsclassifylist.ckxj") }}</el-button
                >
                <el-button
                  class="laiketui laiketui-add"
                  @click="addclass(scope.row)"
                  v-has-permi="'addSonClass'"
                >
                  {{ $t("goodsclassifylist.tjzl") }}</el-button
                >
              </div>
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

    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('DemoPage.tableExamplePage.export_data')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <div class="item" @click="exportPage">
          <i class="el-icon-document"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_page") }}</span>
        </div>
        <div class="item item-center" @click="exportAll">
          <i class="el-icon-document-copy"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_all") }}</span>
        </div>
        <div class="item" @click="exportQuery">
          <i class="el-icon-document"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_query") }}</span>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getClassInfo, classSortTop, delClass } from "@/api/goods/goodsClass";
import { exports } from "@/api/export/index";
import { mixinstest } from "@/mixins/index";
import { getStorage, setStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: "goodsclassifylist",
  mixins: [mixinstest],
  data() {
    return {
      inputInfo: {
        name: "",
        lang_code: "",
      },
      type: "",
      button_list: [],
      tableData: [],
      loading: true,

      // 弹框数据
      dialogVisible: false,
      // table高度
      tableHeight: null,
      languages: null,
    };
  },

  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
    }

    if(this.$route.query && this.$route.query.length >0){
        this.inputInfo.name = this.$route.query.name
        this.inputInfo.lang_code = this.$route.query.lang_code
    } else {
        this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal();
    }

    this.getClassInfos();
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
     // 图片错误处理
     handleErrorImg(e){
      console.log('图片报错了',e.target.src);
      e.target.src=ErrorImg
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "goodsclassify") {
          return (this.menuId = item.id);
        }
      });
      let buttonList = await getButton({
        api: "saas.role.getButton",
        menuId: this.menuId,
      });
      this.button_list = buttonList.data.data;
      this.button_list = buttonList.data.data.map((item) => {
        return item.title;
      });
      console.log(this.button_list, "获取按纽权限");
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      // debugger
      console.log(this.$refs.tableFather.clientHeight);
    },

    async getClassInfos() {
      const res = await getClassInfo({
        api: "admin.goods.getClassInfo",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        className: this.inputInfo.name,
        lang_code: this.inputInfo.lang_code,
        level: 0,
      });
      console.log(res);
      this.total = res.data.data.total;
      this.tableData = res.data.data.classInfo;
      this.loading = false;
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
      //this.current_num = 计算出当前是第几条-第几条   如21 -30条
      this.current_num =
        this.tableData.length === this.pageSize
          ? this.dictionaryNum * this.pageSize
          : this.total;
      this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
    },

    reset() {
      this.inputInfo.name = "";
      this.inputInfo.lang_code = "";
    },

    demand() {
      //this.currpage = 1;
      //this.current_num = 10;
      //this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.getClassInfos().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    derive() {},

    viewlower(value) {
      console.log(value);
      this.$router.push({
        path: "/goods/goodsclassify/viewClass",
        query: { id: value.cid,lang_code:this.inputInfo.lang_code ,name:this.inputInfo.name},
      });
      setStorage("button_list", this.button_list);
    },

    Edit(value) {
      console.log(value);
      this.$router.push({
        name: "editClass",
        params: value,
        query: {
          classlevel: 1,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
        },
      });
    },

    addClasss() { 
      this.$router.push({
        name: "addClass",
        query: {
          classlevel: 1,
        },
      });
    },

    addclass(value) { 
      this.$router.push({
        name: "addClass",
        params: value,
        query: {
          id: value.cid,
          lang_code: value.lang_code,
          country_num: value.country_num,
          classlevel: 2,
          name: "add",
        },
      });
    },

    Delete(value) {
      this.$confirm(
        this.$t("goodsclassifylist.scts"),
        this.$t("goodsclassifylist.ts"),
        {
          confirmButtonText: this.$t("goodsclassifylist.okk"),
          cancelButtonText: this.$t("goodsclassifylist.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          delClass({
            api: "admin.goods.delClass",
            classId: value.cid,
          }).then((res) => {
            if (res.data.code == "200") {
              console.log(res);
              if (this.dictionaryNum != 1 && this.tableData.length <= 1) {
                this.dictionaryNum--;
              }
              this.getClassInfos();
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

    // moveUp(value) {
    //     if(value !== 0) {
    //         moveMenuSort({
    //           api: 'saas.role.moveMenuSort',
    //           downId: this.tableData[value - 1].id,
    //           upId: this.tableData[value].id
    //         }).then(res => {
    //           this.getMenuLeveInfos(this.$route.query.id)
    //           console.log(res);
    //           this.$message({
    //             type: 'success',
    //             message: '操作成功!',
    //             offset: 102
    //           });
    //         })
    //     } else {
    //         moveMenuSort({
    //           api: 'saas.role.moveMenuSort',
    //           downId: this.tableData[value + 1].id,
    //           upId: this.tableData[value].id
    //         }).then(res => {
    //           this.getMenuLeveInfos(this.$route.query.id)
    //           console.log(res);
    //           this.$message({
    //             type: 'success',
    //             message: '操作成功!',
    //             offset: 102
    //           });
    //         })
    //     }
    // },

    placedTop(value) {
      classSortTop({
        api: "admin.goods.classSortTop",
        classId: value.cid,
      }).then((res) => {
        console.log(res);
        this.getClassInfos();
        this.$message({
          type: "success",
          message: this.$t("zdata.zdcg"),
          offset: 102,
        });
      });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.getClassInfos().then(() => {
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
      this.getClassInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    // 弹框方法
    dialogShow() {
      this.dialogVisible = true;
    },

    handleClose(done) {
      this.dialogVisible = false;
    },

    async exportPage() {
      await exports(
        {
          api: "admin.goods.getClassInfo",
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          className: this.inputInfo.name,
          level: 0,
        },
        "ClassInfo"
      );
    },

    async exportAll() {
      console.log(this.total);
      await exports(
        {
          api: "admin.goods.getClassInfo",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          className: this.inputInfo.name,
          level: 0,
        },
        "allClassInfo"
      );
    },

    async exportQuery() {
      await exports(
        {
          api: "admin.goods.getClassInfo",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          className: this.inputInfo.name,
          level: 0,
        },
        "queryClassInfo"
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

  /deep/.jump-list {
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
  }

  .menu-list {
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
      .cell {
        img {
          width: 60px;
          height: 60px;
        }
      }
    }

    /deep/.el-table {
      .OP-button {
        .OP-button-bottom {
          .laiketui-add:before {
            margin-right: 5px;
            font-size: 10px;
          }
          .laiketui-zhiding:before {
            margin-right: 5px;
            color: #888f9e;
            font-weight: 600;
          }
        }
      }
    }
  }
}
</style>
