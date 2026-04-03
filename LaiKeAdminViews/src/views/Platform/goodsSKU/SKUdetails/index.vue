<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">

        <el-select
          :placeholder="$t('qxzyz')"
          class="select-input"
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

        <div class="query-input">
          <el-input v-model="inputInfo.code" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('goodsSKU.SKUdetails.qsrsxbm')"></el-input>
          <el-input v-model="inputInfo.name" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('goodsSKU.SKUdetails.qsrsxz')"></el-input>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
          <el-button class="bgColor export" type="primary" @click="dialogShow">{{$t('DemoPage.tableExamplePage.export')}}</el-button>
        </div>
      </div>
	  </div>
    <div class="jump-list">
      <el-button class="fontColor" @click="delAll" :disabled="is_disabled" icon="el-icon-delete" >{{$t('goodsSKU.SKUdetails.plsc')}}</el-button>
    </div>
    <div class="dictionary-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" ref="table" class="el-table" style="width: 100%"
			:height="tableHeight">
      <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
          </div>
        </template>
        <el-table-column type="selection">
        </el-table-column>
        <el-table-column prop="code" :label="$t('goodsSKU.SKUdetails.sxbm')" >
        </el-table-column>
        <el-table-column prop="lang_name" :label="$t('yz')"> </el-table-column>
        <el-table-column prop="name" :label="$t('goodsSKU.SKUdetails.sxz')">
        </el-table-column>
        <el-table-column :label="$t('goodsSKU.SKUdetails.sfsx')">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.status" @change="switchs(scope.row)" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="admin_name" :label="$t('goodsSKU.SKUdetails.tjr')">
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('goodsSKU.SKUdetails.tjsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('goodsSKU.SKUdetails.cz')">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-delete" :disabled="scope.row.status == 1 ? true : false" @click="Delete(scope.row)">{{$t('goodsSKU.SKUdetails.shanchu')}}</el-button>
              </div>
            </div>
				</template>
        </el-table-column>
		  </el-table>
      <div class="pageBox" ref="pageBox" v-if="showPagebox">
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
import { getSkuInfo, setSkuSwitch, delSku } from '@/api/Platform/goodsSku'
import { exports } from '@/api/export/index'
export default {
  name: 'SKUdetails',

  data() {
    return {
      countryList: [],
      languages: [],
      lang_code: '',
      country_num: '',
      tableData: [],
      loading: true,
      is_disabled: true,
      idList: [],
      inputInfo: {
        code: '',
        name: '',
        lang_code: '',
      },

      total:20,
			pagesizes: [10, 25, 50, 100],
			pagination: {
				page: 1,
				pagesize: 10,
			},
			currpage: 1,
      current_num: 10,
      dictionaryNum: 1,
      pageSize: 10,
      showPagebox: true,

      // table高度
      tableHeight: null,

      // 弹框数据
      dialogVisible: false,
    }
  },

  created() {
    this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal();
    this.getSkuInfos()
    this.getCountrys();
    this.getLanguage();
  },

  mounted() {
    this.$nextTick(function() {
      this.getHeight()
    })
    window.addEventListener('resize',this.getHeight(),false)

  },

  methods: {
    getIds(value) {
      this.country_num = value
    },

    getlangCode(value) {
      this.lang_code = value
    },

    async getCountrys() {
      try {
        const result = await this.LaiKeCommon.getCountries();
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
    getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
		},
    async getSkuInfos() {

      if(this.$route.query.lang_code)
      {
        this.inputInfo.lang_code = this.$route.query.lang_code;
      }
      else
      {
        this.inputInfo.lang_code = ''
      }

      const res = await getSkuInfo({
        api: 'saas.dic.getSkuInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        dataCode: this.inputInfo.code,
        lang_code: this.inputInfo.lang_code,
        dataName: this.inputInfo.name,
        id: parseInt(this.$route.query.id),
      })
      console.log(res);
      if(res.data.data.list && res.data.data.list.length) {
        this.total = res.data.data.list[0].sunSkusTotal
        this.tableData = res.data.data.list[0].sunSkus
      } else {
        this.tableData = []
        this.total = 0
      }
      this.sizeMeth()
      this.loading = false
    },
    sizeMeth () {
      this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
      this.current_num =
        this.tableData.length === this.pageSize
          ? this.dictionaryNum * this.pageSize
          : this.total
    },
    reset() {
      this.inputInfo.code = ''
      this.inputInfo.lang_code = ''
      this.inputInfo.name = ''
    },

    demand() {
      this.currpage = 1
      this.current_num = 10
      if(this.inputInfo.code === '' && this.inputInfo.name === '') {
        this.getSkuInfos().then(() => {
          if(this.tableData.length > 5) {
            this.showPagebox = true
          }
        })
      } else {
        this.showPagebox = false
        this.loading = true
        this.dictionaryNum = 1
        getSkuInfo({
          api: 'saas.dic.getSkuInfo',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          dataCode: this.inputInfo.code,
          lang_code: this.inputInfo.lang_code,
          dataName: this.inputInfo.name,
          sid: parseInt(this.$route.query.id),
        }).then(res => {
          console.log(res);
          this.total = res.data.data.total
          this.tableData = res.data.data.list
          this.loading = false
          if(this.tableData.length > 5) {
            this.showPagebox = true
          }
        })
      }

      // this.getSkuInfos().then(() => {
      //   this.loading = false
      //   if(this.tableData.length > 5) {
      //     this.showPagebox = true
      //   }
      // })
    },

    // 是否生效
    switchs(value) {
      console.log(value);
      setSkuSwitch({
        api: 'saas.dic.setSkuSwitch',
        id: value.id,
      }).then(res => {
        this.getSkuInfos()
        console.log(res);
        if(value.status === 1) {
          this.$message({
            message: this.$t('zdata.czcg'),
            type: 'success',
            offset: 102
          })
        } else {
          this.$message({
            message: this.$t('zdata.czcg'),
            type: 'success',
            offset: 102
          })
        }
      })
    },

    //选择一页多少条
		handleSizeChange(e){
      this.loading = true
			console.log(e);
      // this.current_num = e
      this.pageSize = e
      this.getSkuInfos().then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
        this.loading = false
      })
		},
		//点击上一页，下一页
		handleCurrentChange(e){
      this.loading = true
      this.dictionaryNum = e
      this.currpage = ((e - 1) * this.pageSize) + 1
      this.getSkuInfos().then(() => {
        this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.loading = false
      })

		},

    Delete(value) {
      console.log(value);
      this.$confirm(this.$t('goodsSKU.SKUdetails.scqr'), this.$t('goodsSKU.SKUdetails.ts'), {
        confirmButtonText: this.$t('goodsSKU.SKUdetails.okk'),
        cancelButtonText: this.$t('goodsSKU.SKUdetails.ccel'),
        type: 'warning'
      }).then(() => {
        delSku({
          api: 'saas.dic.delSku',
          idList: value.id
        }).then(res => {
          if(res.data.code == '200') {
            console.log(res);
            this.getSkuInfos()
            this.$message({
              type: 'success',
              message: this.$t('zdata.sccg'),
              offset: 102
            });
          }
        })
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // });
      });
    },

    // 选框改变
    handleSelectionChange(val) {
			if(val.length==0){
				this.is_disabled = true
			}else{
				this.is_disabled = false
			}
      console.log(val);
      this.idList = val.map(item => {
        return item.id
      })
      this.idList = this.idList.join(',')
    },

    // 批量删除
    delAll() {
      this.$confirm(this.$t('goodsSKU.SKUdetails.scqr'), this.$t('goodsSKU.SKUdetails.ts'), {
        confirmButtonText: this.$t('goodsSKU.SKUdetails.okk'),
        cancelButtonText: this.$t('goodsSKU.SKUdetails.ccel'),
        type: 'warning'
      }).then(() => {
        delSku({
          api: 'saas.dic.delSku',
          idList: this.idList
        }).then(res => {
          if(res.data.code == '200') {
            console.log(res);
            this.$message({
              type: 'success',
              message: this.$t('zdata.sccg'),
              offset: 102
            });
            this.getSkuInfos()
          }

        })
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // });
      });
    },

    // 弹框方法
    dialogShow() {
      this.dialogVisible = true
    },

    handleClose(done) {
      this.dialogVisible = false
    },

    async exportPage() {
      await exports({
        api: 'saas.dic.getSkuInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        exportType: 1,
        dataCode: this.inputInfo.code,
        dataName: this.inputInfo.name,
        sid: parseInt(this.$route.query.id),
      },'pagegoods')
    },

    async exportAll() {
      console.log(this.total);
      await exports({
        api: 'saas.dic.getSkuInfo',
        pageNo: 1,
        pageSize: this.total,
        exportType: 1,
        dataCode: this.inputInfo.code,
        dataName: this.inputInfo.name,
        sid: parseInt(this.$route.query.id),
      },'allgoods')
    },

    async exportQuery() {
      await exports({
        api: 'saas.dic.getSkuInfo',
        pageNo: 1,
        pageSize: this.total,
        exportType: 1,
        dataCode: this.inputInfo.code,
        dataName: this.inputInfo.name,
        sid: parseInt(this.$route.query.id),
      },'querygoods')
    }
  }
}
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

  .dictionary-list {
    flex: 1;
    background: #FFFFFF;
    border-radius: 4px;
    /deep/.el-table__header {
      thead {
        tr {
          th{
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
          td{
            height: 92px;
            text-align: center;
            font-size: 14px;
            color: #414658;
            font-weight: 400;
          }
        }
      }
    }

    /deep/.el-table{
      .OP-button{
        button{
          padding: 5px;
          min-height: 24px;
          background: #FFFFFF;
          border: 1px solid #D5DBE8;
          border-radius: 2px;
          font-size: 12px;
          font-weight: 400;
          color: #888F9E;
        }
        button:hover{
          border:1px solid rgb(64, 158, 255);
          color: rgb(64, 158, 255);
        }
        button:hover i{
          color: rgb(64, 158, 255);
        }
        .OP-button-top{
          margin-bottom: 8px;
          justify-content: center;
        }
        .table-dropdown{
          .el-dropdown-link{
            cursor: pointer;
            padding: 0 5px;
            height: 22px;
            border: 1px solid #D5DBE8;
            border-radius: 2px;
            margin-left: 10px;
            font-size: 12px;
            font-weight: 400;
            color: #888F9E;
          }
        }
      }
    }

    /deep/.pageBox{
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: #FFFFFF;
      padding: 0 20px;
      height: 76px;
      .pageLeftText{
        font-size: 14px;
        font-weight: 400;
        color: #6A7076;
      }
      .el-pagination {
          flex: 1;
          display: flex;
          align-items: center;
          padding: 0;
      }
      .el-pagination__sizes{
        height: 36px!important;
        line-height: 36px!important;
        .el-input--mini .el-input__inner{
          height: 36px!important;
          line-height: 36px!important;
        }
      }
      .pageRightText {
        margin-right: auto;
        font-size: 14px;
        font-weight: 400;
        color: #6A7076;
      }
      .btn-next,.btn-prev{
        padding: 0;
        width: 82px;
        height: 36px;
        border: 1px solid #D5DBE8;
        border-radius: 2px;
      }
      .btn-prev{
        margin-right: 8px;
      }

      .el-pager li{
        width: 36px;
        height: 36px;
        line-height: 36px;
        border: 1px solid #D5DBE8!important;
        border-radius: 2px;
        color: #6A7076;
        margin-right: 8px;
      }
      .el-pager li:hover{
        border: 1px solid #2890FF!important;
        color: #2890FF;
      }
      .el-pager li.active{
        border: 1px solid #D5DBE8!important;
        color: #FFFFFF!important;
        background: #2890FF;
      }
    }
  }
}
</style>
