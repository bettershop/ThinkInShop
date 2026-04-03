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
            :placeholder="$t('viewgoodsclasslower.qxzfldj')"
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
        </div>
      </div>
    </div>
    <div class="jump-list">
      <el-button type="primary" @click="superiors">
        <img src="@/assets/imgs/fhsj.png" alt="" />
        {{ $t("viewgoodsclasslower.fhsj") }}
      </el-button>
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
            <p style="color: #414658">{{$t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column
          prop="cid"
          :label="$t('viewgoodsclasslower.flid')"
          width="88"
        >
        </el-table-column>
        <el-table-column prop="title" :label="$t('viewgoodsclasslower.fltp')">
          <template slot-scope="scope">
            <img :src="scope.row.img" alt=""  @error="handleErrorImg"/>
          </template>
        </el-table-column>
        <el-table-column prop="pname" :label="$t('viewgoodsclasslower.flmc')">
        </el-table-column>
        <el-table-column prop="lang_name" :label="$t('viewgoodsclasslower.flyz')"> </el-table-column>
        <el-table-column prop="level" :label="$t('viewgoodsclasslower.fljb')">
          <template slot-scope="scope">
            <span>{{ getLevel(scope.row.level) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="add_date"
          :label="$t('viewgoodsclasslower.tjsj')"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('viewgoodsclasslower.cz')"
          width="320"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  class="laiketui laiketui-zhiding"
                  @click="placedTop(scope.row)"
                  v-if="scope.$index !== 0"
                >
                  {{ $t("viewgoodsclasslower.zd") }}</el-button
                >
                <el-button
                  icon="el-icon-edit-outline"
                  @click="Edit(scope.row)"
                  >{{ $t("viewgoodsclasslower.bianji") }}</el-button
                >
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">{{
                  $t("viewgoodsclasslower.shanchu")
                }}</el-button>
              </div>
              <div class="OP-button-bottom" v-if="scope.row.level !== 4">
                <el-button icon="el-icon-view" @click="viewlower(scope.row)">{{
                  $t("viewgoodsclasslower.xcxj")
                }}</el-button>
                <el-button
                  class="laiketui laiketui-add"
                  @click="addmenu(scope.row)"
                  >{{ $t("viewgoodsclasslower.tjzl") }}</el-button
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
  </div>
</template>

<script>
import { getClassInfo, classSortTop, delClass } from "@/api/goods/goodsClass";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import ErrorImg from '@/assets/images/default_picture.png'
export default {
  name: "viewClass",
  mixins: [mixinstest],
  data() {
    return {
      sjquery_name:"",
      sjquery_lang_code:"",
      inputInfo: {
        name: "",
        lang_code: "",
      },
      button_list: [],
      tableData: [],
      loading: true,

      level: null,
      // table高度
      tableHeight: null,

      tag: true,
      languages: null,
    };
  },

  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
    }
    this.inputInfo.lang_code = this.$route.query.lang_code;
    this.sjquery_name = this.$route.query.name
    this.sjquery_lang_code = this.$route.query.lang_code
    this.getClassInfos(this.$route.query.id);
    console.log(this.$store.state.superior.superiorList);
    this.button_list = getStorage("button_list");
    this.getLanguage();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  computed: {
    // superior() {
    //   return this.$store.state.superior.superiorList
    // },
    goodsList() {
      return this.$store.state.superior.goodsSuperiorList;
    },
  },

  watch: {
    $route: {
      handler: function () {
        this.getClassInfos(this.$route.query.id);
      },
      deep: true,
    },
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
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      // debugger
      console.log(this.$refs.tableFather.clientHeight);
    },

    async getClassInfos(id) {
      console.log('dictionaryNum',this.dictionaryNum);

      const res = await getClassInfo({
        api: "admin.goods.getClassInfo",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        classId: id,
        className: this.inputInfo.name,
        lang_code: this.inputInfo.lang_code,
        type: 1,
        level: this.level - 1,
      });
      console.log(res);
      if (res.data.data.classInfo.length == 0) {
        // if(this.tag) {
        //   this.level += 1
        // }
        this.level += 1;
      } else {
        this.level = res.data.data.classInfo[0].level + 1;
      }
      this.tag = false;
      // this.level = res.data.data.classInfo.length == 0 ? this.level + 1 : res.data.data.classInfo[0].level + 1
      this.total = res.data.data.total;
      console.log(this.level);
      // debugger
      if (this.total < 10) {
        this.current_num = this.total;
      }
      this.tableData = res.data.data.classInfo;
      this.loading = false;
    },

    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.getClassInfos(this.$route.query.id).then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    reset(){
      this.inputInfo.name = '';
      this.inputInfo.lang_code = '';
    },

    superiors() {

      let  param = {};

      if(this.sjquery_lang_code != null && this.sjquery_lang_code != '') {
        param.lang_code = this.sjquery_lang_code;
      }

      if(this.sjquery_name != null && this.sjquery_name != '') {
        param.name = this.sjquery_name;
      }

      if (this.level === 2 || this.level === 1) {
        this.$router.push({
          path: "/goods/goodsclassify/goodsclassifylist",
          query: param
        });
        this.$store.commit("EMPTY_GOODS");
      } else {
        let ids = this.$store.state.superior.goodsSuperiorList.pop();
        if(ids == undefined){
          this.$router.push({
            path: "/goods/goodsclassify/goodsclassifylist",
            query: param
          });
          this.$store.commit("EMPTY_GOODS");
        }else{
          this.$router.push({
          path: "/goods/goodsclassify/viewClass",
          query: { id: ids },
        });
        }
      }
    },

    viewlower(value) {
      this.$store.commit("ADD_GOODS", this.$route.query.id);
      this.dictionaryNum = 1
      this.$router.push({
        path: "/goods/goodsclassify/viewClass",
        query: { id: value.cid },
      });
    },

    Edit(value) {
      if (this.level === 2) {
        this.$router.push({
          name: "editClass",
          params: value,
          query: {
            id: value.cid,
            classlevel: this.level,
            dictionaryNum: this.dictionaryNum,
            pageSize: this.pageSize,
          },
        });
      } else if (this.level === 3) {
        this.$router.push({
          name: "editClass",
          params: value,
          query: {
            id: value.cid,
            classlevel: this.level,
          },
        });
      } else if (this.level === 4) {
        this.$router.push({
          name: "editClass",
          params: value,
          query: {
            id: value.cid,
            classlevel: this.level,
          },
        });
      } else if (this.level === 5) {
        this.$router.push({
          name: "editClass",
          params: value,
          query: {
            id: value.cid,
            classlevel: this.level,
          },
        });
      }
    },

    Delete(value) {
      this.$confirm(
        this.$t("viewgoodsclasslower.qrsc"),
        this.$t("viewgoodsclasslower.ts"),
        {
          confirmButtonText: this.$t("viewgoodsclasslower.okk"),
          cancelButtonText: this.$t("viewgoodsclasslower.ccel"),
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
              this.getClassInfos(this.$route.query.id);
              this.$message({
                type: "success",
                message: this.$t("viewgoodsclasslower.cg"),
                offset: 102,
              });
            }
          });
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除'
          // });
        });
    },

    addmenu(value) {
      console.log(value);
      if (this.level === 2) {
        this.$router.push({
          name: "addClass",
          query: {
            id: value.cid,
            classlevel: this.level + 1,
            lang_code: value.lang_code,
            country_num: value.country_num,
            name: "add",
          },
        });
      } else if (this.level === 3) {
        this.$router.push({
          name: "addClass",
          query: {
            id: value.cid,
            classlevel: this.level + 1,
            lang_code: value.lang_code,
            country_num: value.country_num,
            name: "add",
          },
        });
      } else if (this.level === 4) {
        this.$router.push({
          name: "addClass",
          query: {
            id: value.cid,
            classlevel: this.level + 1,
            lang_code: value.lang_code,
            country_num: value.country_num,
            name: "add",
          },
        });
      }
    },

    placedTop(value) {
      classSortTop({
        api: "admin.goods.classSortTop",
        classId: value.cid,
      }).then((res) => {
        console.log(res);
        this.getClassInfos(this.$route.query.id);
        this.$message({
          type: "success",
          message: this.$t("viewgoodsclasslower.cg"),
          offset: 102,
        });
      });
    },

    getLevel(value) {
      if (value === 1) {
        return this.$t("viewgoodsclasslower.erj");
      } else if (value === 2) {
        return this.$t("viewgoodsclasslower.sanj");
      } else if (value === 3) {
        return this.$t("viewgoodsclasslower.sij");
      } else {
        return this.$t("viewgoodsclasslower.wuj");
      }
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.getClassInfos(this.$route.query.id).then(() => {
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
      this.getClassInfos(this.$route.query.id).then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
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

  /deep/.jump-list {
    button {
      img {
        position: relative;
        top: 1px;
      }
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
        button {
          line-height: 1;
          margin-right: 8px;
        }
        .OP-button-top {
          // margin-bottom: 8px;
          display: flex;
          justify-content: start;
        }
        .OP-button-bottom {
          display: flex;
          justify-content: start;
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
