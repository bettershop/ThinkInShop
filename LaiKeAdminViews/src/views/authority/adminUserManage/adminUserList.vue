<template>
  <div class="container">
    <div class="jump-list">
        <el-button v-has-permi="'addAdminUser'" class="bgColor laiketui laiketui-add" type="primary" @click="Save()">
          {{$t('adminUserList.tjgly')}}
        </el-button>
    </div>
    <div class="merchants-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="page.loading" :data="page.tableData" ref="table"
          class="el-table"
          style="width: 100%"
          :height="tableHeight">
          <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column prop="id" :label="$t('adminUserList.zhid')">
        </el-table-column>
        <el-table-column prop="name" :label="$t('adminUserList.zh')">
        </el-table-column>
        <el-table-column :label="$t('adminUserList.scbh')">
          <template>
            {{customerNumber}}
          </template>
        </el-table-column>
        <el-table-column prop="roleName" :label="$t('adminUserList.bdjs')">
        </el-table-column>
        <el-table-column prop="state" :label="$t('adminUserList.zhtai')">
          <template slot-scope="scope">
            <span v-if="scope.row.status==1">{{ $t('adminUserList.yjy') }}</span>
            <span v-if="scope.row.status==2">{{ $t('adminUserList.yqy') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="superName" :label="$t('adminUserList.tjr')">
        </el-table-column>
        <el-table-column :label="$t('adminUserList.tjsj')" width="200">
          <template slot-scope="scope" v-if="scope.row.add_date!=null">
            {{ scope.row.add_date | dateFormat}}
          </template>
        </el-table-column>

        <el-table-column :label="$t('adminUserList.cz')" width="300">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top" >
                <el-button v-has-permi="'disable'" icon="el-icon-circle-close" @click="Disable(scope.row.id,1)" v-if="scope.row.status===2">
                  {{$t('adminUserList.jy')}}</el-button>
                <el-button v-has-permi="'enable'" icon="el-icon-circle-check" @click="Disable(scope.row.id,2)" v-if="scope.row.status===1">
                  {{$t('adminUserList.qy')}}</el-button>
                <el-button v-has-permi="'editAdminUser'"  icon="el-icon-edit-outline" @click="Edit(scope.row.id)">{{$t('adminUserList.bianji')}}</el-button>
                <el-button v-has-permi="'delAdmin'"  icon="el-icon-delete" @click="Del(scope.row.id)">{{$t('adminUserList.shanchu')}}</el-button>
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
import main from "@/webManage/js/authority/adminUserManage/adminUserList";
export default main;
</script>

<style scoped lang="less">
@import "../../../common/commonStyle/form";
@import "../../../webManage/css/authority/adminUserManage/adminUserList.less";
</style>
