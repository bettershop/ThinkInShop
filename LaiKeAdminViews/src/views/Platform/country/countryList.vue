<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input size="medium" v-model="page.inputInfo.name" @keyup.enter.native="demand" class="Search-input"
                    :placeholder="$t('countryManage.qsrgjmc')"></el-input>
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
                 @click="Save()">{{$t('countryManage.tjgj')}}
      </el-button>
      <el-button class="fontColor" :disabled="is_disabled" icon="el-icon-delete" type="primary"
                 @click="Delete()">{{$t('countryManage.plsc')}}
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
        <el-table-column prop="id" :label="$t('countryManage.id')">
        </el-table-column>
        <el-table-column prop="national_flag" label="国旗">
          <template slot-scope="scope">
            <img :src="scope.row.national_flag" alt="" class="flagImg"/>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('countryManage.name')">
        </el-table-column>
        <el-table-column prop="zh_name" :label="$t('countryManage.zh_name')">
        </el-table-column>
        <el-table-column prop="code" :label="$t('countryManage.code')">
        </el-table-column>
        <el-table-column prop="code2" :label="$t('countryManage.code2')">
        </el-table-column>
        <el-table-column prop="num3" :label="$t('countryManage.num3')">
        </el-table-column>
        <el-table-column fixed="right" :label="$t('countryManage.cz')" width="250">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-edit-outline" @click="Save(scope.row.id)">{{$t('countryManage.bianji')}}</el-button>
                <el-button icon="el-icon-delete" @click="Delete(scope.row.id)">{{$t('countryManage.shanchu')}}</el-button>
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
import main from "@/webManage/js/platform/country/countryList";
export default main
</script>


<style scoped lang="less">
@import "../../../common/commonStyle/form";
@import "../../../webManage/css/platform/country/countryList";
</style>
