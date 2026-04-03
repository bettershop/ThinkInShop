<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input size="medium" v-model="page.inputInfo.name" @keyup.enter.native="demand" class="Search-input"
          :placeholder="$t('logisticsCompanyManage.logisticsCompanyList.qsrwlgs')"></el-input>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}
          </el-button>
        </div>
      </div>
    </div>
    <div class="jump-list">
      <el-button class="bgColor laiketui laiketui-add" type="primary" :style="language=='en'?'width: 220px;':''"
        @click="Save()">{{$t('logisticsCompanyManage.logisticsCompanyList.tjwl')}}
      </el-button>
      <el-button class="fontColor" :disabled="is_disabled" icon="el-icon-delete" type="primary"
        @click="Delete()">{{$t('logisticsCompanyManage.logisticsCompanyList.plsc')}}
      </el-button>
    </div>

    <div class="merchants-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="page.loading" :data="page.tableData" ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight" @selection-change="handleSelectionChange" @sort-change="sortChange">
        <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column type="selection">
        </el-table-column>
        <el-table-column prop="id" label="ID">
        </el-table-column>
        <el-table-column prop="kuaidi_name" :label="$t('logisticsCompanyManage.logisticsCompanyList.wlgsmc')">
        </el-table-column>
        <el-table-column prop="type" :label="$t('logisticsCompanyManage.logisticsCompanyList.bm')">
        </el-table-column>
        <el-table-column :label="$t('logisticsCompanyManage.logisticsCompanyList.kg')">
          <template slot-scope="scope">
            <el-switch v-model="switchFlag=scope.row.is_open===1" @change="Switch(scope.row.id)"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('logisticsCompanyManage.logisticsCompanyList.px')" sortable='sequence'>
          <template slot-scope="scope">
            <el-input class="logisticsSort" v-model="scope.row.sort" @change="Sort(scope.row)"  @input="scope.row.sort=scope.row.sort.replace(/^(0+)|[^\d]+/g,'')"/>
          </template>
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('logisticsCompanyManage.logisticsCompanyList.tjsj')">
          <template slot-scope="scope" v-if="scope.row.add_date!=null">
            {{scope.row.add_date|dateFormat}}
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('logisticsCompanyManage.logisticsCompanyList.cz')" width="250">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-edit-outline" @click="Save(scope.row.id)">{{$t('logisticsCompanyManage.logisticsCompanyList.bianji')}}</el-button>
                <el-button icon="el-icon-delete" @click="Delete(scope.row.id)">{{$t('logisticsCompanyManage.logisticsCompanyList.shanchu')}}</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="page.showPagebox">
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
</template>

<script>
import main from "@/webManage/js/platform/logisticsCompanyManage/logisticsCompanyList";
export default main
</script>


<style scoped lang="less">
  @import "../../../common/commonStyle/form";
  @import "../../../webManage/css/platform/logisticsCompanyManage/logisticsCompanyList";
</style>
