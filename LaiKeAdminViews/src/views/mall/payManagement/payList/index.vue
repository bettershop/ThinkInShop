<template>
  <div class="container">
    <div class="dictionary-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="loading"
        :data="tableData"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
      >
        <!-- 增加序号-logo -->
        <el-table-column
          prop="Nosort"
          :label="$t('payManagement.xh')"
          width="120"
        >
          <template slot-scope="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="logo" :label="$t('payManagement.tb')">
          <template slot-scope="scope">
            <img
              :src="`${scope.row.logo}`"
              alt=""
              style="width: 60px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('payManagement.mc')">
        </el-table-column>
        <el-table-column prop="mask" :label="$t('payManagement.beizhuL')">
          <template slot-scope="scope">
            {{ scope.row.description }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('payManagement.kqzt')">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.status"
              @change="switchs(scope.row)"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="isdefaultpay" :label="$t('payManagement.mrfs')">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.isdefaultpay"
              @change="settingDefaultPaytype(scope.row)"
              :active-value="1"
              :inactive-value="2"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('payManagement.cz')"
          :width="language=='en'?300:200"
        >
          <template slot-scope="scope">
            <div class="OP-button">
                <el-button
                  v-has-permi="'editIcon'"
                  icon="el-icon-edit-outline"
                  @click="handleEditIcon(scope.row)"
                  >{{ $t("payManagement.bj") }}</el-button
                >
                <el-button
                  v-has-permi="'parameterModify'"
                  v-if="
                    scope.row.class_name !== 'wallet_pay' && scope.row.class_name !== 'offline_payment'
                  "
                  icon="el-icon-edit-outline"
                  @click="parameter(scope.row)"
                  >{{ $t("payManagement.csxg") }}</el-button
                >
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="showPagebox">
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
  </div>
</template>

<script>
import payList from "@/webManage/js/mall/payManagement/payList";
export default payList;
</script>

<style scoped lang="less">
@import "../../../../webManage/css/mall/payManagement/payList.less";
</style>
