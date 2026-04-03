<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            v-model="searchForm.expressName"
            :placeholder="$t('sheetOfNoodles.qxrkddh')"
          ></el-input>

          <el-input
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            v-model="searchForm.sNo"
            :placeholder="$t('sheetOfNoodles.qsrddbh')"
          ></el-input>

          <el-input
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            v-model="searchForm.mch_name"
            :placeholder="$t('sheetOfNoodles.qsrdpmc')"
          ></el-input>

          <el-select
            class="select-input"
            v-model="searchForm.status"
            :placeholder="$t('sheetOfNoodles.qxzdyzt')"
          >
            <el-option
              v-for="item in stateList"
              :key="item.brand_id"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>

          <div class="select-date">
            <el-date-picker
              v-model="searchForm.date"
              type="datetimerange"
              :range-separator="$t('reportManagement.businessReport.zhi')"
              :start-placeholder="$t('reportManagement.businessReport.ksrq')"
              :end-placeholder="$t('reportManagement.businessReport.jsrq')"
              value-format="yyyy-MM-dd HH:mm:ss"
              :editable="false"
            >
            </el-date-picker>
          </div>
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
    <!-- /order/setExpressSheet/addExpress -->
    <!-- @click="$router.push('')" -->
    <div class="jump-list" ref="tableFather">
      <el-button
        class="bgColor el-icon-printer"
        type="primary"
        @click="handlePrint"
      >{{ $t('sheetOfNoodles.dymd') }}</el-button
      >
      <el-button v-if="is_open_cloud === 1 && is_search"
                 class="white-bg el-icon-edit"
                 type="primary"
                 @click="BatchupdateStatus"
      >{{ $t('sheetOfNoodles.ggzt') }}</el-button
      >
      <el-button v-if="is_open_cloud === 1"
        class="white-bg el-icon-printer"
        type="primary"
        @click="BatchoverridePrint"
      >{{ $t('sheetOfNoodles.plfd') }}</el-button
      >
      <div>
        <div style="margin-left: 8px; color: #909399; font-size: 10px;">
          注：复打仅支持在提交打印请求2天内的打印任务进行复打10次的操作
        </div>
        <!-- 新增的注释 -->
        <div style="margin-left: 8px; color: #909399; font-size: 10px; display: block;">
          取消电子面单只支持部分快递公司，支持快递公司列表请参照下方链接：<a href="https://api.kuaidi100.com/document/dianzimiandanquxiao" target="_blank" style="color: #409EFF;">https://api.kuaidi100.com/document/dianzimiandanquxiao</a>
        </div>
      </div>

    </div>

    <!-- 表格数据 -->
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
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
          </div>
        </template>
        <el-table-column
          fixed="left"
          type="selection"
          width="55"
        ></el-table-column>
        <el-table-column prop="id" :label="$t('sheetOfNoodles.xh')" width="150"></el-table-column>
        <el-table-column
          prop="courier_num"
          :label="$t('sheetOfNoodles.kddh')"
          width="150"
        ></el-table-column>
        <el-table-column
          prop="kuaidi_name"
          :label="$t('sheetOfNoodles.kdgs')"
          width="150"
        ></el-table-column>
        <el-table-column
          prop="kdComOrderNum"
          :label="$t('sheetOfNoodles.kdddd')"
          width="150"
        ></el-table-column>
        <el-table-column
          prop="sNo"
          :label="$t('sheetOfNoodles.ddbh')"
          width="200"
        ></el-table-column>
        <el-table-column
          prop="shop_name"
          :label="$t('sheetOfNoodles.ssdp')"
          width="150"
        ></el-table-column>
        <el-table-column prop="is_status" :label="$t('sheetOfNoodles.dyzt')" width="150">
          <template slot-scope="scope">
            {{ getStatusText(scope.row.is_status) }}
          </template>
        </el-table-column>
        <el-table-column prop="id" :label="$t('sheetOfNoodles.shdz')" width="270">
          <template slot-scope="scope">
            <p>{{$t('sheetOfNoodles.shr')}}:{{ scope.row.name }}</p>
            <p>{{$t('sheetOfNoodles.lxdh')}}:{{ scope.row.mobile }}</p>
            <p>
              {{$t('sheetOfNoodles.fhdz')}}:{{ scope.row.sheng }}{{ scope.row.shi
              }}{{ scope.row.xian }}{{ scope.row.address }}
            </p>
          </template>
        </el-table-column>
        <el-table-column prop="id" :label="$t('sheetOfNoodles.fhdz')" width="270">
          <template slot-scope="scope">
            <p>{{$t('sheetOfNoodles.shr')}}:{{ scope.row.send_name }}</p>
            <p>{{$t('sheetOfNoodles.lxdh')}}:{{ scope.row.send_tel }}</p>
            <p>
              {{$t('sheetOfNoodles.fhdz')}}:{{ scope.row.send_sheng }}{{ scope.row.send_shi
              }}{{ scope.row.send_xian }}{{ scope.row.send_address }}
            </p>
          </template>
        </el-table-column>
        <el-table-column
          prop="deliver_time"
          :label="$t('sheetOfNoodles.fhsj')"
          width="150"
        ></el-table-column>
        <el-table-column fixed="right" :label="$t('orderLists.cz')" width="220">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  icon="el-icon-circle-close"
                  @click="handleCloseOrder(scope.row.id)"
                >{{ $t('sheetOfNoodles.qxdd') }}</el-button
                >
                <el-button icon="el-icon-view" @click="PreviewImg(scope.row.label)">查看面单</el-button>
              </div>
              <div class="OP-button-button" style="margin-bottom:8px">
                <el-button
                  icon="el-icon-view"
                  @click="handleViewGoods(scope.row.id)"
                >{{ $t('sheetOfNoodles.cksp') }}</el-button
                >
                <el-button
                  icon="el-icon-printer" v-if="is_open_cloud === 1"
                  @click="overridePrint(scope.row.id)"
                >{{ $t('sheetOfNoodles.fd') }}</el-button
                >
              </div>


              <div class="OP-button-button" v-if="is_open_cloud === 1 && (scope.row.is_status === 0 || scope.row.is_status === 1)">
                <el-button icon="el-icon-edit"
                           @click="handleUpdateStatus(scope.row.id)"
                >{{ $t('sheetOfNoodles.ggzt') }}</el-button>

              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="showPage">
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
    <el-image
      ref="elImage"
      style="width: 0; height: 0;"
      :src="bigImageUrl"
      :preview-src-list="logicImageList">
    </el-image>
  </div>
</template>
<script>
import { mixinstest } from "@/mixins/index";

import {getTableList, del, getConfig, updateStatus,overridePrint} from "@/api/order/electronicSheetList";
import pageData from "@/api/constant/page";
import {delOrder} from "@/api/order/orderList";

export default {
  name: "ElectronicSheetList",
  mixins: [mixinstest],
  props: {},
  components: {},
  loading: true,
  data() {
    return {
      is_open_cloud:0,
      is_search:false,
      language: "ZH",
      bigImageUrl:'',
      logicImageList:[],
      // 搜索数据
      searchForm: {
        expressName: "",
        sNo: "",
        mch_name: "",
        date: null,
        status: "",
      },
      stateList: [
        { label: "未打印", value: 0 },
        { label: "已打印", value: 1 },
        { label: "打印失败", value: 2 }
      ],
      loading: true,
      page: pageData.data(),
      tableData: [],
      // table高度
      tableHeight: null,
      checkTabList: [],
      ids:"",
      showPage:true,
      // 分页相关变量初始化
      total: 0, // 总数据量
      currpage: 1, // 当前页起始数
      current_num: 0, // 当前页结束数
      pageSize: 10, // 每页条数
      dictionaryNum: 1, // 当前页码
      pagination: { page: 1 } // 分页对象
    };
  },
  computed: {},
  watch: {},
  created() {
    if (this.$route.query.no) {
      this.searchForm.sNo = this.$route.query.no;
    }
    this.handleTableList();
    this.getConfig();
  },
  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },
  methods: {

    getStatusText(status) {
      const statusMap = {
        0: this.$t('sheetOfNoodles.wdy'),
        1: this.$t('sheetOfNoodles.yyd'),
        2: this.$t('sheetOfNoodles.dysb')
      };
      // 无匹配时返回空/默认值
      return statusMap[status] || '';
    },

    PreviewImg(url) {
      // 调用接口之后获取图片数据
      this.logicImageList = [url]
      this.$nextTick(() => {
        this.$refs.elImage.clickHandler()
      })
    },
    handleCloseOrder(id) {
      let data = {
        api: "admin.order.CancelElectronicWaybill",
        id: id,
      };

      this.$confirm("确定要取消该电子面单发货吗？", this.$t("smsList.ts"), {
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
                message: this.$t("orderLists.czcg"),
                offset: 102,
              });
              this.handleTableList();
            }
          });
        })
        .catch(() => {});
    },

    handleUpdateStatus(id){
      this.$confirm(this.$t('orderLists.cztx'), this.$t('orderLists.ts'), {
        confirmButtonText: this.$t('orderLists.okk'),
        cancelButtonText: this.$t('orderLists.ccel'),
        type: 'warning'
      })
        .then(() => {
          updateStatus({
            api: 'admin.order.updateExpressDeliveryStatus',
            ids: id
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.$message({
                message: this.$t('orderLists.czcg'),
                type: 'success',
                offset: 100
              })
              this.handleTableList();
            }
          })
        })
    },


    overridePrint(id){
      let data = {
        api: "admin.order.overridePrint",
        ids: id,
      };

      this.$confirm("确定要进行复打操作吗？", this.$t("smsList.ts"), {
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
                message: this.$t("orderLists.czcg"),
                offset: 102,
              });
              this.handleTableList();
            }
          });
        })
        .catch(() => {});
    },

    handleViewGoods(id) {
      console.log("index", id);
      this.$router.push({
        name: "goodList",
        query: {
          id: id,
        },
      });
    },
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      //   this.tableHeight =400
    },
    demand() {
      this.dictionaryNum = 1
      this.showPage=false
      this.is_search = this.checkValue(this.searchForm.status)

      this.handleTableList();
    },
    checkValue(value) {
      if (value === '') return false;
      const strValue = String(value);
      return (strValue === '0' || strValue === '1') && strValue !== '2';
    },
    reset() {
      this.searchForm.expressName = "";
      this.searchForm.sNo = "";
      this.searchForm.mch_name = "";
      this.searchForm.status = "";
      this.searchForm.date = "";
      this.pageSize = 10;
      this.dictionaryNum = 1;
      this.is_search = false;
    },


    //获取快递100云打印开关配置
    async getConfig(){
      let data = {
        api: "admin.system.GetLogisticsAndPrinting",
      };
      const res = await getConfig(data);
      this.is_open_cloud = res.data.data.list.is_open_cloud || 0;
      console.log("this.is_open_cloud",this.is_open_cloud);
    },

    async handleTableList() {
      let data = {
        api: "admin.order.ShippingRecords",
        expressName: this.searchForm.expressName,
        sNo: this.searchForm.sNo,
        mch_name: this.searchForm.mch_name,
        status: this.searchForm.status,
        startDate: this.searchForm.date ? this.searchForm.date[0] : "",
        endDate: this.searchForm.date ? this.searchForm.date[1] : "",
        pageSize: this.pageSize,
        pageNo: this.dictionaryNum,
      };
      const res = await getTableList(data);
      console.log("xxxxx113113113", res);
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.pagination.page = this.dictionaryNum;
      this.loading = false;

      // 【关键改动1】修复current_num计算逻辑
      // 逻辑：当前页结束数 = 最小(当前页起始数+每页条数-1, 总数据量)
      const currentStart = (this.dictionaryNum - 1) * this.pageSize + 1; // 当前页起始数
      this.currpage = currentStart; // 同步更新起始数
      this.current_num = Math.min(currentStart + this.pageSize - 1, this.total);

      this.showPage=true
    },
    // 页码切换
    handleSizeChange(e) {
      this.page.loading = true;
      this.pageSize = e; // 【关键改动2】修正为直接赋值给pageSize（原page.pageSize未定义）
      this.handleTableList().then(() => {
        this.page.loading = false;
      });
    },
    handleCurrentChange(e) {
      this.page.loading = true;
      this.dictionaryNum = e;
      this.handleTableList().then(() => {
        this.page.loading = false;
      });
    },
    handleSelectionChange(val) {
      console.log("val",val);
      this.checkTabList = val;

      this.ids = val.map(item =>
        item.id
      ).join(",")
    },
    // 打印面单
    handlePrint() {
      if (this.checkTabList.length <= 0) {
        this.$message({
          message: "请选择电子面单",
          type: "error",
          offset: 100,
        });
      } else {
        let routeData = this.$router.resolve({
          path: "/printSheet",
          query: {
            list:JSON.stringify(this.checkTabList)
          },
        });
        window.open(routeData.href, "_blank");
      }
    },

    BatchupdateStatus() {
      if (this.checkTabList.length <= 0) {
        this.$message({
          message: "请选择电子面单",
          type: "error",
          offset: 100,
        });
      }else {
        this.$confirm(this.$t('orderLists.cztx'), this.$t('orderLists.ts'), {
          confirmButtonText: this.$t('orderLists.okk'),
          cancelButtonText: this.$t('orderLists.ccel'),
          type: 'warning'
        })
          .then(() => {
            updateStatus({
              api: 'admin.order.updateExpressDeliveryStatus',
              ids: this.ids
            }).then(res => {
              console.log(res)
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('orderLists.czcg'),
                  type: 'success',
                  offset: 100
                })
                this.handleTableList();
              }
            })
          })
      }
    },

    BatchoverridePrint(){
      if (this.checkTabList.length <= 0) {
        this.$message({
          message: "请选择电子面单",
          type: "error",
          offset: 100,
        });
      }else {
        this.$confirm(this.$t('orderLists.cztx'), this.$t('orderLists.ts'), {
          confirmButtonText: this.$t('orderLists.okk'),
          cancelButtonText: this.$t('orderLists.ccel'),
          type: 'warning'
        })
          .then(() => {
            overridePrint({
              api: 'admin.order.overridePrint',
              ids: this.ids
            }).then(res => {
              console.log(res)
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('orderLists.czcg'),
                  type: 'success',
                  offset: 100
                })
                this.handleTableList();
              }
            })
          })
      }
    },
  },
};
</script>
<style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
  /deep/.Search {
    margin: 16px 0;
    .Search-condition {
      .query-input {
        display: flex;
        margin-right: 10px;

        .Search-input {
          margin-right: 10px;
        }

        .select-input {
          margin-right: 10px;
        }
      }
    }
  }

  /deep/.jump-list {
    .laiketui:before {
      font-size: 14px;
      margin-right: 8px;
    }

    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }

    .laiketui-shangjia:before {
      font-size: 14px;
      margin-right: 8px;
      position: relative;
      top: 1px;
    }

    button {
      margin-right: 10px;
      padding: 0 10px;
    }

    .el-icon-printer:before {
      margin-right: 8px;
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
        .OP-button-button {
          // justify-content:
          display: flex;
        }
      }
    }
  }
}
.white-bg {
  background-color: #ffffff !important;  // 白色背景
  color: #909399 !important;            // Element UI 标准灰色（可按需调整色值）
  border-color: #e6e6e6 !important;     // 浅灰色边框，适配整体风格
}
</style>
