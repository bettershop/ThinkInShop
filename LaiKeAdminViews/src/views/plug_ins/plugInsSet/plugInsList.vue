<template>
  <div class="container">
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
          <img src="../../../assets/imgs/empty.png" alt="" />
          <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
        </div>
      </template>
<!--        <el-table-column-->
<!--          fixed="left"-->
<!--          prop="id"-->
<!--          :label="$t('plugInsSet.plugInsList.xh')"-->
<!--        >-->
<!--        </el-table-column>-->
        <el-table-column prop="name" :label="$t('plugInsSet.plugInsList.cjmc')">
          <template slot-scope="scope">
            <div class="group_box">
              <div class="group_span">
                <span>{{ scope.row.plugin_name }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="content"
          :label="$t('plugInsSet.plugInsList.cjjs')"
          width="350"
        >
          <template slot-scope="scope">
            <el-tooltip popper-class="tool-tip" placement="bottom" >
              <div slot="content">{{ scope.row.content }}</div>
              <div class="ma_box">
                <span class="ma_left">
                  {{ scope.row.content }}
                </span>
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column
          prop="isMchPlugin"
          :label="$t('plugInsSet.plugInsList.dplx')"
        >
          <template slot-scope="scope">
            {{
              scope.row.isMchPlugin == "1"
                ? $t("plugInsSet.plugInsList.dp")
                : $t("plugInsSet.plugInsList.sc")
            }}
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          :label="$t('plugInsSet.plugInsList.sfkq')"
        >
          <template slot="header" slot-scope="scope">
              <div style="display: flex;align-items: center;justify-content: center;">

            {{ $t("plugInsSet.plugInsList.sfkq") }}
            <el-tooltip placement="bottom" effect="light">
              <div slot="content">
                <p style="margin-bottom: 5px;color: #414658;">{{ $t('plugInsSet.pluginTip.kqs') }}</p>
                <p style="color: #97A0B4;">{{ $t('plugInsSet.pluginTip.kqsts') }}</p>
                <p style="margin-bottom: 5px;margin-top: 5px;color: #414658;">{{ $t('plugInsSet.pluginTip.gbs') }}</p>
                <p style="color: #97A0B4;">{{ $t('plugInsSet.pluginTip.gbsts') }}</p>
              </div>
              <img style="width: 13px;height: 13rpx;object-fit: contain;margin-left: 5px;" src="../../../assets//imgs//wenhao.png" alt="">
            </el-tooltip>
            </div>

          </template>
          <template slot-scope="scope">
            <el-switch
              @change="switchs(scope.row)"
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
              v-if="scope.row.isMchPlugin == '1'"
            >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column
          prop="optime"
          :label="$t('plugInsSet.plugInsList.xgsj')"
        >
          <template slot-scope="scope">
            {{ dateFormat("YYY-mm-dd HH:MM", scope.row.optime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="plugin_sort"
          :label="$t('plugInsSet.plugInsList.px')"
          width="180"
        >
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.plugin_sort"
              :min="0"
              :max="999"
              size="small"
              @change="saveSort(scope.row)"
              :placeholder="$t('plugInsSet.plugInsList.pxz')"
            ></el-input-number>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('plugInsSet.plugInsList.cz')"
          width="220"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  icon="el-icon-edit-outline"
                  @click="Edit(scope.row)"
                  >{{ $t("plugInsSet.plugInsList.bj") }}</el-button
                >
                <el-button
                  icon="el-icon-s-operation"
                  @click="Collocate(scope.row)"
                  >{{ $t("plugInsSet.plugInsList.pz") }}</el-button
                >
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import plugInsList from "@/webManage/js/plug_ins/plugInsSet/plugInsList";
export default plugInsList;
</script>

<style scoped lang="less">
@import "../../../webManage/css/plug_ins/plugInsSet/plugInsList.less";
</style>

<style>
.tool-tip {
  max-width: 350px;
}
</style>
