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
              placeholder="请输入商品名称"
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
          <el-table-column prop="p_id" label="商品编号"></el-table-column>
          <el-table-column prop="code" label="商品名称">
            <template slot-scope="scope" >
                <div style="display: flex;justify-content: center;align-items: center;">
                    <img :src="scope.row.img" alt="" style="width: 50px;height: 50px;">
                    <span>{{ scope.row.p_name }}</span>
                </div>
            </template>
          </el-table-column>
          <el-table-column
            prop="attribute"
            label="商品规格"
          ></el-table-column>
          <el-table-column prop="num" label="数量"></el-table-column>
          <el-table-column prop="p_price" label="商品价格"></el-table-column>
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
          api: "admin.order.getPro",
          name: this.gsName,
          id:this.$route.query.id,
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
        min-width: 150px;
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
  