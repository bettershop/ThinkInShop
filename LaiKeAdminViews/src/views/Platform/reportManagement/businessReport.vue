<template>
  <div class="container">
    <div :class="language=='en'?'btn-nav-en':'btn-nav'">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('reportManagement.businessReport.shyybb')" @click.native.prevent="$router.push('/Platform/reportManagement/businessReport')"></el-radio-button>
        <el-radio-button :label="$t('reportManagement.businessReport.shxzyh')" @click.native.prevent="$router.push('/Platform/reportManagement/addUsersReport')"></el-radio-button>
        <el-radio-button :label="$t('reportManagement.businessReport.shddbb')" @click.native.prevent="$router.push('/Platform/reportManagement/orderReport')"></el-radio-button>
      </el-radio-group>
    </div>

    <div class="iframe-content" id="app" style=" background-color: #FFFFFF;border-radius: 4px;padding: 20px;">
      <div class="Ranking">
        <div class="Ranking-title">{{$t('reportManagement.businessReport.shyyeq')}}</div>
        <div class="Ranking-date">
          <div class="Ranking-tab">
            <span :class="tabIndex == 0 ? 'active' : ''" @click="Tab(0)">{{$t('reportManagement.businessReport.zr')}}</span>
            <span :class="tabIndex == 1 ? 'active' : ''" @click="Tab(1)">{{$t('reportManagement.businessReport.jr')}}</span>
            <span :class="tabIndex == 2 ? 'active' : ''" @click="Tab(2)">{{$t('reportManagement.businessReport.zj')}}</span>
            <span :class="tabIndex == 3 ? 'active' : ''" @click="Tab(3)">{{$t('reportManagement.businessReport.by')}}</span>
          </div>
          <el-date-picker
            v-model="value1"
            value-format="yyyy-MM-dd HH:mm:ss"
            type="datetimerange"
            @change="change"
            :range-separator="$t('reportManagement.businessReport.zhi')"
            :start-placeholder="$t('reportManagement.businessReport.ksrq')"
            :end-placeholder="$t('reportManagement.businessReport.jsrq')">
          </el-date-picker>
        </div>
      </div>
      <div id="chart">
      </div>
      <div class="allData">
        <h2>{{$t('reportManagement.businessReport.qbsj')}}</h2>
        <div class="allDataRight">
          <el-input v-model="SHinput" :placeholder="$t('reportManagement.businessReport.qsrshmc')" ></el-input>
          <el-button type="primary" size="small" @click="demand">{{$t('reportManagement.businessReport.query')}}</el-button>
        </div>
      </div>
      <div class="allTable">
        <el-table :data="tableData" @sort-change="sortChange" style="width: 100%;"  >
          <el-table-column  prop="mchId" :label="$t('reportManagement.businessReport.shid')" ></el-table-column>
          <el-table-column  prop="name" :label="$t('reportManagement.businessReport.shmc')" ></el-table-column>
          <el-table-column  prop="total" sortable='custom' :label="$t('reportManagement.businessReport.yye')"></el-table-column>
        </el-table>
        <div class="pageBox" v-if="showPagebox">
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
	  </div>
  </div>
</template>

<script>
import businessReport from '@/webManage/js/platform/reportManagement/businessReport'
export default businessReport
</script>

<style scoped lang="less">
@import '../../../webManage/css/platform/reportManagement/businessReport.less';
.iframe-content{
        display: flex;
        flex-direction: column;
        // justify-content: space-around;
        .allTable{
          flex: 1;
          display: flex;
          flex-direction: column;
          justify-content: space-between;
        }
      }
      #chart{
        min-height:315px
      }
</style>