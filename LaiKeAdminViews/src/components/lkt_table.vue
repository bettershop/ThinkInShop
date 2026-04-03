<!-- <l-table ref="myTableData" 
  :total="total" // 总数
  :tableHead="tableHead" // 表头参数
  :tableData="tableData" // 列表参数
  :loadData="loadData" // 等待状态
  :selection_change="handleSelectionChange" // 多选方法
  ...>
  <template #status="scope">
    <span v-if="scope.row.state" style="color: green;">{{scope.row.menuID}}成功</span>
    <span v-else style="color: red;">{{scope.row.menuID}}失败</span>
  </template>
</l-table>
注意element自带的方法例如@sort-change要改写成下划线形式：sort_change
获取分页等参数方法this.$refs.myTableData

// tableHead: [
//   {
//     prop:'image_name',//渲染字段名
//     type:'selection',//type
//     fixed:true,//element自带的属性
//     sortable:true,
//     label:'商品详情',//表头名称
//     slot:'status',//自定义插槽
//     data:{ //自定义渲染
//       img:'img',//图片
//       switch:'state',//开关
//       switchFunc:this.seeDetail,//开关方法
//       serial:true,//序号
//       head:[//头部数据 横排显示
//         {
//           label:'订单编号：',
//           prop:'suID'
//         },
//         {
//           label:'商户订单编号：',
//           prop:'suID'
//         },
//       ],
//       detail:[//详细数据 纵向显示
//         {
//           label:'',
//           prop:'goods_name'
//         },
//         {
//           label:'规格：',
//           prop:'suID'
//         },
//       ],
//       btn:[//按钮
//         {
//           label:'查看详情',
//           func:this.seeDetail,//按钮方法
//         },
//         {
//           label:'编辑',
//           func:this.seeDetail,
//         }
//       ],
//     },
//     min_width:'',
//     width:'400',//宽度 element自带属性 
//   },
// ], 
//导出组件调用方法
    lExport(){
      let params = {
          api: "admin.order.index",
          search:{
            ...this.inputInfo
          }
      };  
      this.$refs.myTableData.dialogShow(params)
    },

// css需要父元素有
// .container {
//    display: flex;
//    flex-direction: column;
//    width: 100%;
//    height: 100%;
// }
-->



<template>
  <div class="merchants-list" ref="tableFather">
    <el-table
      :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
      v-loading="loading"
      :data="myData"
      ref="table"
      class="el-table"
      style="width: 100%"
      :height="tableHeight"
      :row-key="row_key"
      @selection-change="selection_change"
      @select="select"
      @cell-mouse-enter="cell_mouse_enter"
      @cell-mouse-leave="cell_mouse_leave"
      @cell-click="cell_click"
      @row-click="row_click"
      @header-click="header_click"
      @sort-change="sort_change"
      @filter-change="filter_change"
    >
      <!-- 没有数据显示 -->
      <template v-slot:empty v-if="myData.length == 0 && !loading">
        <div class="empty">
          <!-- <img src="@/assets/imgs/empty.png" alt="" /> -->
          <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
        </div>
      </template>
      <!-- 表头循环渲染 -->
      <template v-for="(item, index) in tableHead">
        <!-- type状态需要单独判断 -->
        <el-table-column
          v-if="item.type"
          :key="index"
          :type="item.type"
          :width="item.width"
          :reserve-selection="true"
        >
        </el-table-column>
        <el-table-column
          v-if="!item.type"
          :key="index"
          :label="item.label"
          :prop="item.prop"
          :type="item.type"
          :width="item.width"
          :min-width="item.min_width"
          :fixed="item.fixed"
          :sortable="item.sortable"
          :stripe="item.stripe"
          :border="item.border"
          :size="item.size"
          :index="item.index"
          :render-header="item.render_header"
          :sort-method="item.sort_method"
          :sort-by="item.sort_by"
          :sort-orders="item.sort_orders"
          :resizable="item.resizable"
          :formatter="item.formatter"
          :show-overflow-tooltip="item.show_overflow_tooltip"
          :align="item.align"
          :header-align="item.header_align"
          :class-name="item.class_name"
          :selectable="item.selectable"
          :filters="item.filters"
          :filter-placement="item.filter_placement"
          :filter-multiple="item.filter_multiple"
          :filter-method="item.filter_method"
          :filtered-value="item.filtered_value"
        >
          <template v-if="!item.type" #default="scope">
            <!-- 自定义插槽 -->
            <div v-if="item.slot">
              <slot :name="item.slot" v-bind:row="scope.row" :index="scope.$index"></slot>
              <!-- #status对应的item.slot 参数依然按scope.row获取 -->
              <!-- <template #status="scope"> 
                  <span v-if="scope.row.state" style="color: green;">{{scope.row.menuID}}成功</span>
                  <span v-else style="color: red;">{{scope.row.menuID}}失败</span>
                </template> -->
            </div>
            <!-- 一般prop渲染 -->
            <span v-else-if="!item.data">
              {{ scope.row[item.prop] }}
            </span>
            <!-- 自定义渲染 -->
            <div v-else>
              <!-- 头部 -->
              <div class="head-info" v-if="item.data.head">
                <span v-for="(items, indexs) in item.data.head" :key="indexs"
                  >{{ items.label }}{{ scope.row[items.prop] }}</span
                >
              </div>
              <div class="head-info_d" v-if="item.data.head"></div>
              <div :class="item.data.detail ? 'table-info' : ''">
                <!-- 图片 -->
                <div v-if="item.data.img">
                  <img :src="scope.row[item.data.img]" alt="" @error="handleErrorImg" />
                </div>
                <!-- 纵向排列数据 -->
                <div class="table-info_r">
                  <div v-for="(items, indexs) in item.data.detail" :key="indexs">
                    <span>{{ items.label }}</span>
                    <span>{{ scope.row[items.prop] }}</span>
                  </div>
                </div>
              </div>
              <!-- 序号 -->
              <div v-if="item.data.serial">
                {{ scope.$index + 1 }}
              </div>
              <!-- 开关 -->
              <div v-if="item.data.switch">
                <el-switch
                  v-model="scope.row[item.data.switch]"
                  active-color="#00ce6d"
                  inactive-color="#d4dbe8"
                  @change="item.data.switchFunc(scope.row)"
                >
                </el-switch>
              </div>
              <!-- 链接 -->
              <div v-if="item.data.link">
                <a :href="scope.row[item.data.link]" target="blank">{{
                  scope.row[item.data.link]
                }}</a>
              </div>
              <!-- 按钮 -->
              <div v-if="item.data.btn">
                <div class="OP-button">
                  <div class="OP-button-top">
                    <el-button
                      @click="items.func(scope.row)"
                      v-for="(items, indexs) in item.data.btn"
                      :key="indexs"
                      :icon="items.icon"
                      >{{ items.label }}</el-button
                    >
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
      </template>
    </el-table>
    <!-- 分页器 -->
    <div ref="pageBox" class="pageBox">
      <div class="pageLeftText">
        {{ $t('DemoPage.tableExamplePage.show') }}
      </div>
      <el-pagination
        layout="sizes, slot, prev, pager, next"
        :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
        :next-text="$t('DemoPage.tableExamplePage.next_text')"
        :background="true"
        @size-change="handleSizeChange"
        :page-sizes="pagesizes"
        :current-page="pagination.page"
        @current-change="handleCurrentChange"
        :total="total"
      >
        <div class="pageRightText">
          {{ $t('DemoPage.tableExamplePage.on_show') }}{{ currpage }}-{{ current_num
          }}{{ $t('DemoPage.tableExamplePage.twig') }}{{ total
          }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
        </div>
      </el-pagination>
    </div>
    <!-- 弹框组件 -->

    <div class="dialog-export">
      <el-dialog
        :title="$t('DemoPage.tableExamplePage.export_data')"
        v-model="dialogVisible"
        :before-close="handleClose"
      >
        <div class="item" @click="exportPage">
          <Icon icon="iconoir:page"/>
          <span>{{ $t('DemoPage.tableExamplePage.export_page') }}</span>
        </div>
        <div class="item item-center" @click="exportAll">
          <Icon icon="grommet-icons:copy"/>
          <span>{{ $t('DemoPage.tableExamplePage.export_all') }}</span>
        </div>
        <div class="item" @click="exportQuery">
          <Icon icon="iconoir:page"/>
          <span>{{ $t('DemoPage.tableExamplePage.export_query') }}</span>
        </div>
      </el-dialog>
    </div>
  </div>
</template>
    
    
<script>
import { ref, nextTick } from 'vue'
import { exports } from "@/api/export/index";
export default {
  name: 'list',
  props: {
    // 总数
    total: {
      type: Number,
      default: 0
    },
    // 表头参数
    tableHead: {
      type: Array,
      default: []
    },
    // 列表参数
    tableData: {
      type: Array,
      default: []
    },
    // 列表请求方法
    loadData: {
      type: Function,
      default: () => {}
    },
    row_key: {
      type: [String, Function],
      default: 'id'
    },
    // 以下是elemnet自带方法
    // 当选择项发生变化时会触发该事件
    select: {
      type: Function,
      default: () => {}
    },
    // 当选择项发生变化时会触发该事件
    selection_change: {
      type: Function,
      default: () => {}
    },
    // 当单元格 hover 进入时会触发该事件
    cell_mouse_enter: {
      type: Function,
      default: () => {}
    },
    // 当单元格 hover 退出时会触发该事件
    cell_mouse_leave: {
      type: Function,
      default: () => {}
    },
    // 当某个单元格被点击时会触发该事件
    cell_click: {
      type: Function,
      default: () => {}
    },
    // 当某一行被点击时会触发该事件
    row_click: {
      type: Function,
      default: () => {}
    },
    // 当某一列的表头被点击时会触发该事件
    header_click: {
      type: Function,
      default: () => {}
    },
    // 当表格的排序条件发生变化的时候会触发该事件
    sort_change: {
      type: Function,
      default: () => {}
    },
    // 当表格的筛选条件发生变化的时候会触发该事件，参数的值是一个对象，对象的 key 是 column 的 columnKey，对应的 value 为用户选择的筛选条件的数组。
    filter_change: {
      type: Function,
      default: () => {}
    }
  },
  //初始化数据
  data() {
    return {
      myData: [],
      pagesizes: [10, 25, 50, 100],
      pagination: {
        page: 1,
        pagesize: 10
      },
      currpage: 1,
      current_num: 10,
      // table高度
      tableHeight: null,
      loading: false,
      //导出参数
      dialogVisible: false,
      api:'',
      search:{}
    }
  },
  watch: {
    tableData(newVal, oldVal) {
      // console.log(newVal, oldVal)
      this.myData = newVal
    }
  },
  //组装模板
  created() {},
  mounted() {
    this.$nextTick(() => {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },
  methods: {
    //查询
    demand(e) {
      this.loading = true
      if (e) {
        this.pagination.page = e
      }
      this.isFillList()
      this.loadData().then(() => {
        this.loading = false
        this.currpage = (this.pagination.page - 1) * this.pagination.pagesize + 1
        this.current_num =
          this.myData.length === this.pagination.pagesize
            ? this.pagination.page * this.pagination.pagesize
            : this.total
      })
    },
    clear() {
      this.$refs.table.clearSelection()
    },
    //删除最后一条数据做的判断
    isFillList() {
      let totalPage = Math.ceil((this.total - this.myData.length) / this.pagination.pagesize)
      // let totalPage = this.myData.length
      let dictionaryNum = this.pagination.page > totalPage ? totalPage : this.pagination.page
      this.pagination.page = dictionaryNum < 1 ? 1 : dictionaryNum
      // this.loadData(); //数据初始化方法
    },
    // 图片错误处理
    handleErrorImg(e) {
      console.log('图片报错了', e.target.src)
      // e.target.src = ErrorImg
    },

    // 获取table高度
    getHeight() {
      
      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true
      this.pagination.pagesize = e
      this.loadData().then(() => {
        this.loading = false
        this.currpage = (this.pagination.page - 1) * this.pagination.pagesize + 1
        this.current_num =
          this.myData.length === this.pagination.pagesize
            ? this.pagination.page * this.pagination.pagesize
            : this.total
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      // 做数据上下移动操作使用
      this.$emit('currentChange', e)
      this.loading = true
      this.pagination.page = e
      this.currpage = (e - 1) * this.pagination.pagesize + 1
      this.loadData().then(() => {
        this.loading = false
        this.current_num =
          this.myData.length === this.pagination.pagesize
            ? e * this.pagination.pagesize
            : this.total
      })
    },
    // 导出弹框方法
    dialogShow(parmas) {
      console.log(parmas)
      this.api=parmas.api
      this.search=parmas.search
      this.dialogVisible = true
    },
    async exportPage() {
      exports(
        {
          api: this.api,
          pageNo: this.pagination.page,
          pageSize: this.pagination.pagesize,
          exportType: 1,
          ...this.search
        },
        '导出本页'
      )
    },

    async exportAll() {
      exports(
        {
          api: this.api,
          pageNo: 1,
          pageSize: 9999,
          exportType: 1
        },
        '导出全部'
      )
    },

    async exportQuery() {
      exports(
        {
          api: this.api,
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          ...this.search
        },
        '导出查询'
      )
    },

    handleClose(done) {
      this.dialogVisible = false
    }
  }
}
</script>
    
    
<style scoped lang="less">
.merchants-list {
  flex: 1;
  background: #ffffff;
  border-radius: 4px;

  /deep/ .el-table__header {
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

  /deep/ .el-table__body {
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
  .head-info_d {
    margin-top: 2.5rem;
  }
  .head-info {
    position: absolute;
    top: 18px;
    left: 10px;
    min-width: 1200px;
    height: auto;
    z-index: 2;
    text-align: left;
    display: flex;
    color: #969dab;

    .red-dot {
      display: block;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background-color: #ff453d;
      margin: 8px 5px 0 0px;
    }

    span {
      // &:not(:first-child) {
      //   margin-right: 30px;
      // }
    }
  }
  .table-info {
    display: flex;
    align-items: center;
    .el-image {
      width: 60px;
      height: 60px;
    }
    .table-info_r {
      // width: calc(100% - 10px - 60px);
      text-align: left;
      padding-left: 10px;
      font-size: 14px;
      font-weight: 400;
      color: #414658;
    }
  }

  // /deep/ .el-table {
  //   .state.active1 {
  //     color: #13CE66;
  //   }

  //   .state.active2 {
  //     color: #FF453D;
  //   }

  //   .OP-button {
  //     .OP-button-top {
  //       margin-bottom: 8px;
  //       display: flex;
  //       .laiketui-zhiding:before {
  //         margin-right: 5px;
  //       }
  //     }
  //     .OP-button-bottom {
  //       display: flex;
  //       justify-content: start;
  //     }
  //   }
  // }
}
// 分页器
.pageBox {
  padding: 0 20px;
  height: 76px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-top: 1px solid #edf1f5;
  .pageLeftText {
    font-size: 14px;
    font-weight: 400;
    color: #6a7076;
  }
  .el-pagination {
    flex: 1;
    display: flex;
    align-items: center;
    padding: 0;
  }
  .el-pagination__sizes {
    height: 36px !important;
    line-height: 36px !important;
    .el-input--mini .el-input__inner {
      height: 36px !important;
      line-height: 36px !important;
    }
  }
  .pageRightText {
    margin-right: auto;
    font-size: 14px;
    font-weight: 400;
    color: #6a7076;
  }
  .btn-next,
  .btn-prev {
    padding: 0;
    width: 92px;
    height: 36px;
    border: 1px solid #d5dbe8;
    border-radius: 2px;
  }
  .btn-prev {
    margin-right: 8px;
  }

  .el-pager li {
    width: 36px;
    height: 36px;
    line-height: 36px;
    border: 1px solid #d5dbe8 !important;
    border-radius: 2px;
    color: #6a7076;
    margin-right: 8px;
    font-weight: normal;
  }
  .el-pager li:hover {
    border: 1px solid #2890ff !important;
    color: #2890ff;
  }
  .el-pager li.active {
    // border: 1px solid #D5DBE8!important;
    border-radius: 2px;
    color: #ffffff !important;
    background: #2890ff;
    border: none !important;
  }
}
//导出弹框
.dialog-export {
  ::v-deep(.el-dialog) {
    padding: 0;
    width: 540px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    margin: 0 !important;
    .el-dialog__header {
      width: 100%;
      height: 48px;
      line-height: 48px;
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
      }
    }

    .el-dialog__body {
      width: 100%;
      padding: 40px 20px !important;
      display: flex;
      justify-content: center;
      align-items: center;
      box-sizing: border-box;
      .item {
        width: 100px;
        height: 100px;
        border: 1px solid #7ebcff;
        border-radius: 4px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        color: #2890ff;
        font-size: 14px;
        .el-icon svg {
          font-size: 30px !important;
        }
        i {
          font-size: 30px !important;
          margin-bottom: 10px !important;
        }
        &:hover {
          background-color: #2890ff;
          color: #fff;
        }
      }

      .item-center {
        margin: 0 60px;
      }
    }
  }
}
</style>
    
    