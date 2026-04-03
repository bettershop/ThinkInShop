<template>
  <div class="container">
    <el-radio-group
      fill="#2890ff"
      text-color="#fff"
      v-model="radio1"
    >
      <el-radio-button
        :label="$t('DemoPage.tableExamplePage.TAB1')"
      ></el-radio-button>
      <el-radio-button
        :label="$t('DemoPage.tableExamplePage.TAB2')"
      ></el-radio-button>
      <el-radio-button
        :label="$t('DemoPage.tableExamplePage.TAB3')"
      ></el-radio-button>
      <el-radio-button
        :label="$t('DemoPage.tableExamplePage.TAB4')"
      ></el-radio-button>
    </el-radio-group>

    <div class="Search">
      <div class="Search-condition">
        <el-select
          v-model="value"
          clearable
          :placeholder="
            $t('DemoPage.tableExamplePage.Select_commodity_category')
          "
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
        <el-select
          v-model="value"
          clearable
          :placeholder="$t('DemoPage.tableExamplePage.Choose_a_Product_Brand')"
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
        <el-select
          v-model="value"
          clearable
          :placeholder="$t('DemoPage.tableExamplePage.Select_display_location')"
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
        <el-select
          v-model="value"
          clearable
          :placeholder="$t('DemoPage.tableExamplePage.Select_Product_Type')"
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
        <el-select
          v-model="value"
          clearable
          :placeholder="$t('DemoPage.tableExamplePage.Select_commodity_status')"
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
        <el-input
          v-model="input"
          size="medium"
          class="Search-input"
          :placeholder="$t('DemoPage.tableExamplePage.The_input')"
        ></el-input>
        <el-button class="bgColor" type="primary">{{
          $t("DemoPage.tableExamplePage.demand")
        }}</el-button>
        <el-button class="fontColor">{{
          $t("DemoPage.tableExamplePage.reset")
        }}</el-button>
      </div>
      <el-button class="bgColor" type="primary">{{
        $t("DemoPage.tableExamplePage.export")
      }}</el-button>
    </div>
    <div class="jump-list">
      <el-button
        
        class="bgColor laiketui laiketui-add"
        type="primary"
        >{{ $t("physicalgoods.fbsp") }}</el-button
      >
      <el-button
        class="taobao"
        type="primary"
        icon="el-icon-thumb"
        >{{ $t("physicalgoods.tbzq") }}</el-button
      >
      
      <el-button
        class="batchImport"
        type="primary"
        icon="el-icon-upload2"
        >{{ $t("physicalgoods.pldr") }}</el-button
      >
      <el-button
        class="batchImport"
        type="primary"
        icon="el-icon-document"
        >{{ $t("physicalgoods.plscjl") }}</el-button
      >
      <el-button
        class="fontColor"
        :disabled="is_disabled"
        icon="el-icon-delete"
        >{{ $t("physicalgoods.plsc") }}</el-button
      >
      
    </div>
    <myTable ref="myTableData" :total="total" :tableHead="tableHead" :tableData="tableData" :loadData="loadData" :selection_change="handleSelectionChange">
        <template #status="scope">
          <span v-if="scope.row.state" style="color: green;">{{scope.row.menuID}}成功</span>
          <span v-else style="color: red;">{{scope.row.menuID}}失败</span>
        </template>
    </myTable>
    <!-- <div class="card-body">
      <el-table
        :data="tableData"
        element-loading-text="拼命加载中..."
        v-loading="loading"
        ref="table"
        @selection-change="handleSelectionChange"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
      >
        <el-table-column type="selection" width="55"> </el-table-column>
        <el-table-column
          fixed
          sortable
          prop="menuID"
          :label="$t('DemoPage.tableExamplePage.menu_id')"
          width="150"
        >
        </el-table-column>
        <el-table-column
          prop="img"
          :label="$t('DemoPage.tableExamplePage.picture')"
          width="120"
        >
          <template slot-scope="scope">
            <div class="table-img">
              <el-image
                :src="scope.row.img"
                :preview-src-list="srcList"
                fit="cover"
              ></el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="goods_name"
          :label="$t('DemoPage.tableExamplePage.Goods_details')"
          width="400"
        >
          <template slot-scope="scope">
            <div class="table-info">
              <el-image
                src="https://laikeds.oss-cn-shenzhen.aliyuncs.com/1/0/a6051502404600.jpg"
                fit="fill"
              ></el-image>
              <p>{{ scope.row.goods_name }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="menuName"
          :label="$t('DemoPage.tableExamplePage.menu_name')"
          width="120"
        >
        </el-table-column>
        <el-table-column
          prop="is_switch"
          :label="$t('DemoPage.tableExamplePage.switch')"
          width="120"
        >
          <template>
            <el-switch
              v-model="el_switch"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column
          prop="suID"
          :label="$t('DemoPage.tableExamplePage.Belongs_to')"
          width="100"
        >
        </el-table-column>
        <el-table-column
          :label="$t('DemoPage.tableExamplePage.Valid_time')"
          width="300"
        >
          <template slot-scope="scope">
            <div class="table-time">
              <span>开始时间：{{ scope.row.start_time }}</span>
              <span>结束时间：{{ scope.row.end_time }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="add_time" label="添加时间" width="200">
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <span v-if="scope.row.state == 0" class="table-state">待审核</span>
            <span
              v-else-if="scope.row.state == 1"
              class="table-state"
              style="color: #13ce66"
              >成功</span
            >
            <span v-else style="color: #ff453d">失败</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('DemoPage.tableExamplePage.operation')"
          fixed="right"
          width="300"
        >
          <template>
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-edit-outline" @click="Edit">{{
                  $t("DemoPage.tableExamplePage.edit")
                }}</el-button>
                <el-button icon="el-icon-delete" @click="Delete">{{
                  $t("DemoPage.tableExamplePage.delete")
                }}</el-button>
              </div>
              <div class="OP-button-bottom">
                <el-button icon="el-icon-document-copy">{{
                  $t("DemoPage.tableExamplePage.copy")
                }}</el-button>
                <el-button icon="el-icon-view">{{
                  $t("DemoPage.tableExamplePage.look_junior")
                }}</el-button>
                <el-dropdown trigger="click" class="table-dropdown">
                  <span class="el-dropdown-link">
                    {{ $t("DemoPage.tableExamplePage.more") }}
                    <i class="el-icon-arrow-down el-icon--right"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item>余额充值</el-dropdown-item>
                    <el-dropdown-item>积分修改</el-dropdown-item>
                    <el-dropdown-item>等级修改</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox">
        <div class="pageLeftText">显示</div>
        <el-pagination
          layout="sizes, slot, prev, pager, next"
          prev-text="上一页"
          next-text="下一页"
          @size-change="handleSizeChange"
          :page-sizes="pagesizes"
          :current-page="pagination.page"
          @current-change="handleCurrentChange"
          :total="total"
        >
          <div class="pageRightText">
            当前显示{{ currpage }}-{{ current_num }}条，共 {{ total }} 条记录
          </div>
        </el-pagination>
      </div>
    </div> -->
  </div>
</template>

<script>
import { label } from "@/api/goods/goodsList";
import myTable from "@/components/lkt_table";


export default {
  name: "TableExample",
  components:{
    myTable
  },
  data: function () {
    return {
      visible: false,
      radio1: "标签页一",
      options: [
        { value: "选项1", label: "黄金糕" },
        { value: "选项2", label: "双皮奶" },
        { value: "选项3", label: "蚵仔煎" },
        { value: "选项4", label: "龙须面" },
        { value: "选项5", label: "北京烤鸭" },
      ],
      value: "",
      input: "",
      tableData: [],
      tableHead:[],
      el_switch: "",
      total: 20,
      // pagesizes: [10, 25, 50, 100],
      // pagination: {
      //   page: 1,
      //   pagesize: 10,
      // },
      // currpage: 1,
      // current_num: "",
      // loading: true,
      // tableHeight: null, //表格高度
      is_disabled: true,
      form: {
        datetime: "",
        time: "",
        state: "",
        name: "",
        pp: "",
      },
      is_open: false,
      Open_text: "展开",

      srcList: [require("@/assets/imgs/goods.jpeg")], //大图预览
      parentVM: null,
    };
  },
  computed: {
    name() {
      return this.$store.getters.name;
    },
  },
  mounted() {
    this.current_num = this.tableData.length;

  },
  created() {
    //  this.parentVM = $(parent.window)[0].VM;
    this.tableHead=[
      {
        type:'selection',
        width:'55',
      },
      {
        label:'菜单ID',
        prop:'menuID',
        fixed:true,
        sortable:true,
        width:'150',
      },
      {
        prop:'',
        label:'图片',
        width:'120',
        min_width:'',
        data:{
          img:'img',
        }
      },
      {
        prop:'image_name',
        label:'商品详情',
        data:{
          img:'img',
          head:[
            {
              label:'订单编号：',
              prop:'suID'
            },
            {
              label:'商户订单编号：',
              prop:'suID'
            },
          ],
          detail:[
            {
              label:'',
              prop:'goods_name'
            },
            {
              label:'规格：',
              prop:'suID'
            },
          ],
        },
        width:'400',
      },
      {
        prop:'menuName',
        label:'菜单名称',
        width:'120',
      },
      {
        prop:'',
        label:'开关',
        data:{
          switch:'state',
          switchFunc:this.seeDetail
        },
        width:'120',
      },
      {
        prop:'suID',
        label:'所属ID',
        width:'120',
      },
      {
        prop:'',
        label:'有效时间',
        data:{
          detail:[
            {
              label:'开始时间：',
              prop:'start_time'
            },
            {
              label:'结束时间：',
              prop:'end_time'
            }
          ],
        },
        width:'240',
      },
      {
        prop:'add_time',
        label:'添加时间',
        width:'200',
      },
      {
        label:'状态',
        width:'120',
        slot:'status'
      },
      {
        prop:'',
        label:'操作',
        fixed:'right',
        data:{
          btn:[
            {
              label:'查看详情',
              func:this.seeDetail,
            },
            {
              label:'编辑',
              func:this.seeDetail,
            }
          ],
        },
        width:'200',
      },
    ]
    this.loadData()
  },
  methods: {
    async loadData() {
      this.tableData=[]
      for (var i = 0; i < 10; i++) {
        this.tableData.push({
          menuID: "SH_" + i,
          img: require("@/assets/imgs/goods.jpeg"),
          goods_name:
            "adidas 阿迪达斯 NEO板鞋2018夏季新款运动帆布鞋休闲鞋DA9530 DB009" + i,
          menuName: "商户管理" + i,
          is_switch: true,
          suID: "SH_" + i,
          start_time: "2020-11-08 15:22:00",
          end_time: "2020-11-09 15:22:00",
          add_time: "2021-11-08 19:32:00",
          state: i,
        });
      }
    },
    seeDetail(e){
      console.log(e)
      this.$message({
                type: "success",
                message: '查看第'+e.menuID+'条数据',
                offset: 100,
              });
    },

    handleSelectionChange(val) {
      console.log(val)
      this.$message({
                type: "success",
                message: '选中了'+val.length+'条数据',
                offset: 100,
              });
      if (val.length == 0) {
        this.is_disabled = true;
      } else {
        this.is_disabled = false;
      }
    },
    
    Open() {
      if (this.is_open) {
        this.is_open = false;
        this.Open_text = "展开";
      } else {
        this.is_open = true;
        this.Open_text = "收起";
      }
    },
    Delete(id) {
      if (id) {
        // this.parentVM.dialog('删除提示', '是否确认删除', true);
        // console.log(VM);
        // VM.$data.dialogVisible = true
        // console.log(VM);
      } else {
        // 批量删除
      }
    },
    handleClose() {

    },
    handleConfirm() {

    },
    Edit() {
      window.location.href =
        "index.php?module=demo&action=ComplexFormDemo&id=1";
    },
  },
};
</script>

<style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
}

/deep/.el-radio-group {
  .el-radio-button {
    width: 112px;
    height: 42px;
    &:not(:last-child) .el-radio-button__inner {
      border-right: 1px solid #dcdfe6;
    }
  }
  .el-radio-button__inner {
    color: #6a7076;
    display: block;
    font-size: 16px;
    width: 112px;
    height: 42px;
    border: none;
    &:hover {
      color: #2890ff;
    }
  }
}

/deep/.Search {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-radius: 4px;
  padding: 10px;
  margin: 16px 0 17px 0;
  .Search-condition {
    display: flex;
    align-items: center;
  }
  .Search-input {
    width: 280px;
    margin-right: 10px;
    input {
      height: 40px;
      line-height: 40px;
    }
  }
  .el-select {
    width: 180px;
    margin-right: 8px;
  }
  .bgColor {
    background-color: #2890ff;
  }
  .bgColor:hover {
    opacity: 0.8;
  }
  .fontColor {
    color: #6a7076;
    border: 1px solid #d5dbe8;
    margin-left: 14px;
  }
  .fontColor:hover {
    color: #2890ff;
    border: 1px solid #2890ff;
    background-color: #fff;
  }
  .el-input__inner {
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

/deep/.function-button {
  margin-bottom: 15px;
  .bgColor {
    background-color: #2890ff;
  }
  .bgColor:hover {
    background-color: #70aff3;
  }
  .fontColor {
    color: #6a7076;
    border: none;
    height: 40px;
  }
}

/deep/.form-search {
  background: #ffffff;
  padding: 0 24px;
  margin: 10px 0;
  .Search-Button {
    display: flex;
    justify-content: space-between;
  }
  .el-select {
    width: 100%;
  }
  .el-date-editor.el-input {
    width: 100%;
  }
  .el-col {
    .el-form-item {
      margin: 12px 0;
    }
    .Search-Button {
      margin: 12px 0;
    }
  }
  .el-form-item__label {
    color: #414658;
  }
  .bgColor {
    background-color: #2890ff;
  }
  .bgColor:hover {
    opacity: 0.8;
  }
  .fontColor {
    color: #6a7076;
    border: 1px solid #d5dbe8;
    margin-left: 14px;
    background-color: #fff;
  }
  .fontColor:hover {
    color: #2890ff;
    border: 1px solid #2890ff;
  }
  .el-input__inner {
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

  .el-form-item__label {
    font-weight: normal;
  }
}

/deep/.el-table th > .cell {
  text-align: center;
  font-size: 14px;
  font-weight: bold;
  color: #414658;
}

/deep/.el-table .cell {
  text-align: center;
  font-size: 14px;
  color: #414658;
  font-weight: 400;
}

/deep/.el-table {
  .table-time {
    display: flex;
    flex-direction: column;
    align-items: center;
    span {
      font-size: 14px;
      font-weight: 400;
      color: #414658;
    }
  }
  .table-img {
    .el-image {
      width: 60px;
      height: 60px;
    }
  }
  
  .table-state {
    font-size: 14px;
    font-weight: 400;
    color: #414658;
  }
  .OP-button {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    button {
      padding: 5px;
      height: 22px;
      background: #ffffff;
      border: 1px solid #d5dbe8;
      border-radius: 2px;
      font-size: 12px;
      font-weight: 400;
      color: #888f9e;
    }
    button:hover {
      border: 1px solid rgb(64, 158, 255);
      color: rgb(64, 158, 255);
    }
    button:hover i {
      color: rgb(64, 158, 255);
    }
    .OP-button-top {
      margin-bottom: 8px;
    }
    .table-dropdown {
      .el-dropdown-link {
        cursor: pointer;
        padding: 0 5px;
        height: 22px;
        border: 1px solid #d5dbe8;
        border-radius: 2px;
        margin-left: 10px;
        font-size: 12px;
        font-weight: 400;
        color: #888f9e;
      }
    }
  }
}


/deep/ .el-table .OP-button button {
  line-height: 9px;
}
/deep/ .el-table .OP-button .table-dropdown .el-dropdown-link {
  padding: 2px 5px;
}

.form-search,
.el-table {
  border-radius: 4px;
}

// /deep/tbody .el-checkbox::after {
// 	content: '';
// 	display: block;
// 	width: 10px;
// 	height: 10px;
// 	background-color: red;
// 	border-radius: 50px;
// }
</style>