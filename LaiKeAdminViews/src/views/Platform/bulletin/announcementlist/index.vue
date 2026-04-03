<template>
  <div class="container">
    <div class="jump-list">
      <el-button
        @click="$router.push('/Platform/bulletin/announcementrelease')"
        class="bgColor"
        type="primary"
        icon="el-icon-circle-plus-outline"
        >{{ $t("bulletin.announcementlist.fbgg") }}</el-button
      >
    </div>
    <div class="dictionary-list" ref="tableFather">
      <el-table
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
        <el-table-column
          prop="id"
          :label="$t('bulletin.announcementlist.xh')"
          width="120"
        >
        </el-table-column>
        <el-table-column
          prop="title"
          :label="$t('bulletin.announcementlist.ggbt')"
        >
        </el-table-column>
        <el-table-column
          prop="type"
          :label="$t('bulletin.announcementlist.gglx')"
        >
          <template slot-scope="scope">
            <span>{{ type(scope.row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="storeTypesName"
          :label="$t('bulletin.announcementlist.ggjsf')"
          width="130"
        >
        </el-table-column>
        <!-- <el-table-column prop="name" :label="$t('bulletin.announcementlist.ggnr')" width="385">
                <template slot-scope="scope">
                    <div class="content" v-html="delP(scope.row.content)"></div>
                </template>
            </el-table-column> -->
        <el-table-column
          prop="name"
          :label="$t('bulletin.announcementlist.yxsj')"
          width="250"
        >
          <template slot-scope="scope">
            <p>
              {{ $t("bulletin.announcementlist.kssj")
              }}{{ scope.row.startdate }}
            </p>
            <p>
              {{ $t("bulletin.announcementlist.jssj") }}{{ scope.row.enddate }}
            </p>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          :label="$t('bulletin.announcementlist.ggzt')"
        >
        <template slot-scope="scope">
            <span>{{ type2(scope.row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="add_date"
          :label="$t('bulletin.announcementlist.cjjs')"
          width="200"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.add_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          prop="operation"
          :label="$t('bulletin.announcementlist.cz')"
          width="200"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  icon="el-icon-edit-outline"
                  v-if="scope.row.status == '未生效'"
                  @click="Edit(scope.row)"
                  >{{ $t("bulletin.announcementlist.bianji") }}</el-button
                >
                <el-button
                  icon="el-icon-view"
                  v-if="
                    scope.row.status == '已失效' || scope.row.status == '生效中'
                  "
                  @click="View(scope.row)"
                  >{{ $t("bulletin.announcementlist.chakan") }}</el-button
                >
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">{{
                  $t("bulletin.announcementlist.shanchu")
                }}</el-button>
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
import { getSysNoticeInfo, delSysNoticeInfo } from "@/api/Platform/bulletin.js";
import { mixinstest } from "@/mixins/index";
export default {
  name: "announcementlist",
  mixins: [mixinstest],
  data() {
    return {
      tableData: [],

      // table高度
      tableHeight: null,
    };
  },

  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
    }
    this.getSysNoticeInfos();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
    },
    async getSysNoticeInfos() {
      const res = await getSysNoticeInfo({
        api: "saas.sysNotice.getSysNoticeInfo",
        token:
          "eyJUeXBlIjoiSnd0IiwiYWxnIjoiSFMyNTYifQ.eyJpYXQiOjE2MjIxMDE3NzUsImV4cCI6MTYyMjE4ODE3NX0.mDs4OK-7eI5MViGAAIqMEzLLTmLiuWsGfA7croIE_7k",
        storeType: 8,
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
      });
      console.log(res);
      this.total = res.data.data.total;
      if (res.data.data.list.length < 10) {
        this.current_num = this.total;
      }
      this.tableData = res.data.data.list;
    },

    //选择一页多少条
    handleSizeChange(e) {
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.getSysNoticeInfos().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
      });
    },
    //点击上一页，下一页
    handleCurrentChange(e) {
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.getSysNoticeInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
      });
    },

    Edit(value) {
      this.$router.push({
        name: "editorannouncement",
        params: value,
        query: {
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
        },
      });
      console.log(value);
    },
    View(value) {
      this.$router.push({
        name: "vieworannouncement",
        params: value,
        query: {
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
        },
      });
      console.log(value);
    },
    Delete(val) {
      console.log(val);
      this.$confirm(
        this.$t("bulletin.announcementlist.scqr"),
        this.$t("bulletin.announcementlist.ts"),
        {
          confirmButtonText: this.$t("bulletin.announcementlist.okk"),
          cancelButtonText: this.$t("bulletin.announcementlist.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          delSysNoticeInfo({
            api: "saas.sysNotice.delSysNoticeInfo",
            // storeId: val.store_id,
            id: val.id,
          }).then((res) => {
            if (this.dictionaryNum != 1 && this.tableData.length <= 1) {
                this.dictionaryNum--;
              }
            this.getSysNoticeInfos();
            this.$message({
              type: "success",
              message: this.$t("zdata.sccg"),
              offset: 102,
            });
            console.log(res);
          });
        })
        .catch(() => {
          // this.$message({
          // type: 'info',
          // message: '已取消删除'
          // });
        });
    },

    type(val) {
      if (val === 1) {
        return this.$t('bulletin.announcementrelease.xtwh');
      } else if (val == 2) {
        return this.$t('bulletin.announcementrelease.bbgx');
      } else {
        return this.$t('bulletin.announcementrelease.ptgg');
      }
    },
    type2(val){
      if (val === '生效中') {
        return this.$t('bulletin.announcementrelease.sxz')
      }else if(val === '未生效'){
        return this.$t('bulletin.announcementrelease.wsx')
      }else if(val === '已失效'){
        return this.$t('bulletin.announcementrelease.ysx')
      }
    },

    delP(val) {
      // return val.replace(/<\/?p[^>]*>/gi,'')
      return val.substring(0, val.length);
    },
  },
};
</script>

<style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
  /deep/.jump-list {
    button {
      min-width: 200px;
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
            .cell {
              img {
                width: 162px;
                height: 162px;
              }
            }
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
