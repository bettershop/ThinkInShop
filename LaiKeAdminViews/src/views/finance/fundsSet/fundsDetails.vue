  <template>
    <div class="container">
      <!-- <div class="font_one">
        查看详情
      </div> -->
      <div class="Search">
        <div class="Search-condition">
          <div class="query-input">
            <!-- <el-input
              v-model="inputInfo.userName"
              size="medium"
              class="Search-input"
              placeholder="请输入用户名称"
            ></el-input> -->
            <el-select
              class="select-input"
              v-model="inputInfo.ostatus"
              :placeholder="$t('fundsDetails.qxzrz')"
            >
              <el-option
                v-for="item in kepList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>
            <el-select
              class="select-input"
              v-model="inputInfo.oType"
              :placeholder="$t('fundsDetails.qxzlx')"
            >
              <el-option
                v-for="item in TypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>
            <div class="select-date">
              <el-date-picker
                v-model="inputInfo.date"
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
              $t('DemoPage.tableExamplePage.reset')
            }}</el-button>
            <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{
              $t('DemoPage.tableExamplePage.demand')
            }}</el-button>
            <span v-for="(item, index) in button_list" :key="index">
              <el-button
                v-if="item.title == '导出'"
                class="bgColor export"
                type="primary"
                @click="dialogShow"
                >{{$t('DemoPage.tableExamplePage.export')}}</el-button
              >
            </span>
          </div>
        </div>
      </div>
	  <div class="allPrice">
	  	<div><span>入账总金额：{{laikeCurrencySymbol}} {{income}}</span><span style="margin-left:150px;">出账总金额：{{laikeCurrencySymbol}}  {{outcome}}</span></div>
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
                    <img src="../../../assets/imgs/empty.png" alt="">
                    <p style="color: #414658 ;">{{$t('zdata.zwsj')}}</p>
                </div>
            </template>
          <el-table-column prop="id" :label="$t('fundsDetails.xh')"> </el-table-column>
          <el-table-column prop="user_name" :label="$t('fundsDetails.yhmc')"> </el-table-column>
          <el-table-column prop="statusName" :label="$t('fundsDetails.rzzc')">
            <template slot-scope="scope">
              <span v-if="scope.row.statusName == '入账'">{{$t('fundsDetails.rz')}}</span>
              <span v-if="scope.row.statusName == '支出'">{{$t('fundsDetails.zc')}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="money" :label="$t('fundsDetails.je')">
            <template slot-scope="scope">
                <span v-if="scope.row.money">{{laikeCurrencySymbol}} {{ scope.row.money.toFixed(2)}}</span>
              </template>
          </el-table-column>
          <el-table-column prop="typeName" :label="$t('fundsDetails.lx')">
            <!-- <template slot-scope="scope">
              <span v-if="scope.row.type == 1">{{$t('fundsDetails.yhcz')}}</span>
              <span v-if="scope.row.type == 2">{{$t('fundsDetails.sqtx')}}</span>
              <span v-if="scope.row.type == 4">{{$t('fundsDetails.yexf')}}</span>

              <span v-if="scope.row.type == 5">{{$t('fundsDetails.tk')}}</span>
              <span v-if="scope.row.type == 14">{{$t('fundsDetails.xtcz')}}</span>
              <span v-if="scope.row.type == 22">{{$t('fundsDetails.txsb')}}</span>
              <span v-if="scope.row.type == 27">{{$t('fundsDetails.jpyj')}}</span>


              <span v-if="scope.row.type == 26">{{$t('fundsDetails.jnjp')}}</span>
              <span v-if="scope.row.type == 39">{{$t('fundsDetails.dpyj')}}</span>
              <span v-if="scope.row.type == 38">{{$t('fundsDetails.jndp')}}</span>
              <span v-if="scope.row.type == 11">{{$t('fundsDetails.xtkc')}}</span>
              <span v-if="scope.row.type == 13">{{$t('fundsDetails.djjf')}}</span>

            </template> -->
          </el-table-column>
          <el-table-column prop="addtime" :label="$t('fundsDetails.rzsj')"> </el-table-column>
        </el-table>
        <div class="pageBox" ref="pageBox" v-show="showPagebox">
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
  import fundsDetails from '@/webManage/js/finance/withdrawalManage/fundsDetails'
  export default fundsDetails
  </script>

  <style scoped lang="less">
  @import '../../../webManage/css/finance/withdrawalManage/fundsDetails.less';
  .allPrice{
  	 	  display: flex;
  	 	  align-items: center;
  	 	  border-radius: 4px;
  	 	  padding: 10px;
  	 	  margin-bottom: 16px;
  	 	  background: rgba(40,144,255,0.1);
  	 	  border-radius: 4px;
  	 	  border: 1px solid #2890FF;
  	 	  >div{
			  flex: 1;
  	 		  font-size: 16px;
  	 		  color: #2890FF;
  	 	  }
  	   }
  </style>
