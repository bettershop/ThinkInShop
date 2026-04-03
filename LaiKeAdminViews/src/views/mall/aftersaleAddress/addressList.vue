<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            size="medium"
            v-model="page.inputInfo.name"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('aftersaleAddress.qsrlxr')"
          ></el-input>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand"
            >{{ $t("DemoPage.tableExamplePage.demand") }}
          </el-button>
        </div>
      </div>
    </div>
    <div class="jump-list">
      <el-button
        v-has-permi="'save'"
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="Save()"
        >{{$t('aftersaleAddress.tjdz')}}
      </el-button>
    </div>
    <div class="merchants-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="page.loading"
        :data="page.tableData"
        ref="table"
        class="el-table"
        style="width: 100%;"
        :height="tableHeight"
      >
      <!-- tableHeight -->
      <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column :label="$t('aftersaleAddress.xh')" width="120">
          <template slot-scope="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('aftersaleAddress.lxr')">
        </el-table-column>
        <el-table-column prop="tel" :label="$t('aftersaleAddress.lxdh')">
          <template slot-scope="scope">
           +{{ scope.row.cpc }} {{ scope.row.tel }}
          </template>
        </el-table-column>
        <el-table-column prop="address_xq" :label="$t('aftersaleAddress.xxdz')">
        </el-table-column>
        <el-table-column prop="code" :label="$t('aftersaleAddress.yzbm')">
        </el-table-column>
        <el-table-column :label="$t('aftersaleAddress.sfmr')">
          <template slot-scope="scope">
            <!-- {{ scope.row.is_default === 1 ? $t('aftersaleAddress.mrdz') : $t('aftersaleAddress.No') }} -->
            <el-switch
                v-model="scope.row.is_default"
                :active-value="1"
                :inactive-value="0"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
                @change="Default(scope.row.id)"
              >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('aftersaleAddress.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <!-- 按钮权限 -->
                <el-button
                  v-has-permi="'edit'"
                  icon="el-icon-edit-outline"
                  @click="Save(scope.row.id)"
                  >{{$t('aftersaleAddress.bianji')}}</el-button
                >
                <el-button
                    :disabled="scope.row.is_default === 1"
                    v-has-permi="'deleteAddress'"
                    icon="el-icon-delete"
                    @click="Delete(scope.row.id)"
                    >{{$t('aftersaleAddress.shanchu')}}</el-button
                  >
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
import addressList from "@/webManage/js/mall/aftersaleAddress/addressList";
export default addressList;
</script>

<style scoped lang="less">
@import "../../../webManage/css/mall/aftersaleAddress/addressList.less";
.merchants-list{
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

</style>
