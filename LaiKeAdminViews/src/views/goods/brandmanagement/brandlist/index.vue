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
            :placeholder="$t('brandmanagement.qsrpp')"
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
            >{{$t("DemoPage.tableExamplePage.export")}}</el-button
          >
        </div>
      </div>
    </div>
    <div class="jump-list">
      <el-button
        v-has-permi="'addbrand'"
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="$router.push('/goods/brandmanagement/addbrand')"
      >
        {{$t('brandmanagement.xzpp')}}</el-button
      >
    </div>
    <div class="merchants-list" ref="tableFather">
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
        <el-table-column prop="brand_id" label="ID"> </el-table-column>
        <el-table-column prop="brand_pic" class="brand_pic" :label="$t('brandmanagement.pptp')">
          <template slot-scope="scope">
            <img :src="scope.row.brand_pic" alt="" width="80" height="80" @error="handleErrorImg"/>
          </template>
        </el-table-column>
        <el-table-column prop="brand_name" :label="$t('brandmanagement.ppmc')"> </el-table-column>
<!--        <el-table-column prop="country_num" hidden="true" :label="$t('brandmanagement.ssgj')"> </el-table-column>-->
        <el-table-column prop="lang_name" :label="$t('brandmanagement.ppyz')"> </el-table-column>
        <el-table-column prop="categories" :label="$t('brandmanagement.ssfl')">
          <template slot-scope="scope">
            <span>{{ getClass(scope.row.categories) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="brand_time" :label="$t('brandmanagement.tjsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.brand_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('brandmanagement.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  v-has-permi="'editorbrand'"
                  icon="el-icon-edit-outline"
                  @click="Edit(scope.row)"
                >
                  {{$t('brandmanagement.bianji')}}</el-button
                >
                <el-button
                  v-has-permi="'delBrand'"
                  icon="el-icon-delete"
                  @click="Delete(scope.row)"
                >
                  {{$t('brandmanagement.shanchu')}}</el-button
                >
              </div>
              <div class="OP-button-bottom">
                <el-button
                  v-has-permi="'topping'"
                  v-if="
                    (scope.$index > 0 || currpage !== 1)
                  "
                  class="laiketui laiketui-zhiding"
                  @click="placedTop(scope.row)"
                >
                  {{$t('brandmanagement.zd')}}</el-button
                >
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox">
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
    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('DemoPage.tableExamplePage.export_data')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
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
  getBrandInfo,
  addBrand,
  brandByTop,
  delBrand,
} from "@/api/goods/brandManagement";
import { exports } from "@/api/export/index";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: "brandlist",
  mixins: [mixinstest],
  data() {
    return {
      tableData: [],
      loading: true,
      inputInfo: {
        name: "",
        lang_code: "",
      },
      button_list: [],
      // 弹框数据
      dialogVisible: false,
      tableHeight: null,
      // 国家
      countryList: [],
      // 语种
      languages: [],
    };
  },

  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
    }

    this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal();

    this.getBrandInfos();
    this.getButtons();
    // this.getCountrys();
    this.getLanguage();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    async getCountrys() {
      try {
        const result = await getCountries();
        this.countryList =  result.data.data;
      } catch (error) {
        console.error('获取国家列表失败:', error);
      }
    },

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
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      console.log(this.$refs.tableFather.clientHeight);
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "brandmanagement") {
          return (this.menuId = item.id);
        }
      });
      let buttonList = await getButton({
        api: "saas.role.getButton",
        menuId: this.menuId,
      });
      this.button_list = buttonList.data.data;
      this.button_list = buttonList.data.data.map(item => {
        return item.title
      })
    },
    // 获取品牌信息
    async getBrandInfos() {
      const res = await getBrandInfo({
        api: "admin.goods.getBrandInfo",
        storeType: 8,
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        brandName: this.inputInfo.name,
        lang_code: this.inputInfo.lang_code,
      });
      console.log(res);
      this.total = res.data.data.total;
      this.tableData = res.data.data.brandInfoList;
      this.loading = false;
      if (res.data.data.total < 10) {
        this.current_num = this.total;
      }
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
    },

    // 重置
    reset() {
      this.inputInfo.name = "";
      this.inputInfo.lang_code = "";
    },

    // 查询
    demand() {
      // this.currpage = 1;
      // this.current_num = 10;
      // this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.getBrandInfos().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    getClass(value) {
      return value.join(",");
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.getBrandInfos().then(() => {
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
      this.getBrandInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    // 置顶
    placedTop(value) {
      brandByTop({
        api: "admin.goods.brandByTop",
        brandId: value.brand_id,
      }).then((res) => {
        console.log(res);
        this.getBrandInfos();
        this.$message({
          type: "success",
          message: this.$t('zdata.zdcg'),
          offset: 102,
        });
      });
    },

    // 编辑
    Edit(value) {
      this.$router.push({
        name: "editorbrand",
        params: value,
        query: {
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
        },
      });
    },

    // 删除
    Delete(value) {
      console.log(value);
      this.$confirm(this.$t('brandmanagement.qrsc'), this.$t('brandmanagement.ts'), {
        confirmButtonText: this.$t('brandmanagement.okk'),
        cancelButtonText: this.$t('brandmanagement.ccel'),
        type: "warning",
      })
        .then(() => {
          delBrand({
            api: "admin.goods.delBrand",
            brandId: value.brand_id,
          }).then((res) => {
            if (res.data.code == "200") {
              console.log(res);
              if (this.dictionaryNum != 1 && this.tableData.length <= 1) {
                this.dictionaryNum--;
              }
              this.getBrandInfos();
              this.$message({
                type: "success",
                offset: 102,
                message: this.$t('zdata.sccg'),
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
          api: "admin.goods.getBrandInfo",
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          brandName: this.inputInfo.name,
        },
        "pagegoods"
      );
    },

    async exportAll() {
      console.log(this.total);
      await exports(
        {
          api: "admin.goods.getBrandInfo",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          brandName: this.inputInfo.name,
        },
        "allgoods"
      );
    },

    async exportQuery() {
      await exports(
        {
          api: "admin.goods.getBrandInfo",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          brandName: this.inputInfo.name,
        },
        "querygoods"
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
    width: 100%;
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
    button {
      min-width: 120px;
      height: 40px;
      background: #28b6ff;
      border-radius: 4px;
      padding: 0;
      border: none;
      span {
        font-size: 14px;
      }
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
      .cell {
        img {
          width: 60px;
          height: 60px;
        }
      }
    }
  }
}
</style>
