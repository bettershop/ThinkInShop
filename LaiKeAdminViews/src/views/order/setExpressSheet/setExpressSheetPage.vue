<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            v-model="gsName"
            :placeholder="$t('sheetOfNoodles.qsrwlgsmc')"
          ></el-input>
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
    <div
      :class="language == 'en' ? 'jump-list jump-list2' : 'jump-list'"
      ref="tableFather"
    >
      <el-button
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="$router.push('/order/setExpressSheet/addExpress')"
        >{{ $t('sheetOfNoodles.tjsz') }}</el-button
      >
    </div>
    <!-- 表格数据 -->
    <div class="dictionary-list" ref="tableFather">
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
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
          </div>
        </template>
        <el-table-column prop="id" label="ID"></el-table-column>
        <el-table-column prop="type" :label="$t('sheetOfNoodles.wlgsbm') "></el-table-column>
        <el-table-column
          prop="kuaidi_name"
          :label="$t('sheetOfNoodles.wlgsmc')"
        ></el-table-column>
        <el-table-column prop="partnerId" :label="$t('sheetOfNoodles.expressSheet.zhhmm')"></el-table-column>
        <el-table-column prop="add_time" :label="$t('sheetOfNoodles.tjsj')"></el-table-column>

        <el-table-column
          fixed="right"
          :label="$t('Platform.numerical.cz')"
          width="200"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  icon="el-icon-edit-outline"
                  @click="handleGoEdit(scope.row.id,scope.row.express_id)"
                  >{{ $t("Platform.numerical.bianji") }}</el-button
                >
                <el-button
                  icon="el-icon-delete"
                  @click="Delete(scope.row.id)"
                  >{{ $t("Platform.numerical.shanchu") }}</el-button
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
import { getTableList, del } from "@/api/order/setExpressSheet";
import { mixinstest } from "@/mixins/index";
import pageData from "@/api/constant/page";

export default {
  name: "SetExpressSheetPage",
  mixins: [mixinstest],
  props: {},
  components: {},
  data() {
    return {
      language: "ZH",
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      page: pageData.data(),
      gsName:""
    };
  },
  computed: {},
  watch: {},
  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
    }
    this.getTabList();
  },
  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },
  methods: {
    // 跳转编辑
    handleGoEdit(id,express_id) {
      this.$router.push({
        name: "editExpress",
        query: {
          id: id,
          express_id:express_id
        },
      });
    },
    // 删除数据
    Delete(id) {
      let data = {
        api: "admin.express.del_logistics",
        id: id,
      };

      this.$confirm("确定要删除该面单设置吗？", this.$t("smsList.ts"), {
        confirmButtonText: this.$t("smsList.okk"),
        cancelButtonText: this.$t("smsList.ccel"),
        type: "warning",
      })
        .then(() => {
          del(data).then((res) => {
            console.log("resres", res);
            if (res.data.code == "200") {
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
                offset: 102,
              });
              this.getTabList()
            }
          });
        })
        .catch(() => {});
    },
    reset() {
      this.gsName = ""
    },
    demand(e) {
      this.currpage = 1;
      this.current_num = 10;
      // this.showPagebox = false;
      this.page.loading = true;
      this.dictionaryNum = 1;
      this.getTabList().then(() => {
        this.page.loading = false;
        if (this.page.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },
    // 获取表格数据
    async getTabList() {
      const res = await getTableList({
        api: "admin.express.logistics_list",
        name: this.gsName,
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
      });
      // if(res.code==200){
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.loading = false;
      if (res.data.data.total < 10) {
        this.current_num = this.total;
      }
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
      // }
    },
    // 页码切换
    handleSizeChange(e) {
      this.page.loading = true;
      this.page.pageSize = e;
      this.getTabList().then(()=>{
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.page.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.page.loading = false;
      })
    },
    handleCurrentChange(e) {
      this.page.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.getTabList().then(()=>{
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.page.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.page.loading = false;
      })
    },
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
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
      .query-input {
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
    button {
      min-width: 120px;
    }
  }

  /deep/.jump-list2 {
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
    button {
      min-width: 220px;
    }
  }

  .dictionary-list {
    // height: 605px;
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

    /deep/.el-table {
      .OP-button {
        .OP-button-top {
          margin-bottom: 8px;
        }
      }
    }
  }
}
</style>
