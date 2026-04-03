<template>
  <div class="container">

    <div class="basic-configuration" style="height: 182px;">
      <div class="header">
        <span>{{$t('internationalsetting.base_setting')}}</span>
      </div>

      <el-form :model="ruleForm" label-position="right" ref="ruleForm" label-width="92px" class="form-search" :rules="rules">
        <div class="notice">
          <el-form-item :label="$t('internationalsetting.default_lang')" prop="default_lang_code">
            <el-select :disabled="isNone !=''" class="select-input" filterable v-model="ruleForm.default_lang_code" :placeholder="$t('qxzyz')">
              <el-option v-for="(item,index) in languages" :key="index" :label="item.lang_name" :value="item.lang_code">
                <div >{{ item.lang_name }}</div>
              </el-option>
            </el-select>
          </el-form-item>
        </div>

      </el-form>
    </div>
    <div class="basic-configuration">
      <div class="header">
        <span>{{$t('internationalsetting.hbsz')}}</span>
      </div>

      <el-form :model="ruleForm" label-position="right" ref="ruleForm" label-width="92px" class="form-search" :rules="rules">
      <div class="notice" >
        <el-table
            :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
            v-loading="page.loading"
            :data="page.tableData"
            ref="table"
            class="el-table"
            style="width: 100%"
            :height="tableHeight"
            @selection-change="handleSelectionChange"
            @sort-change="sortChange"
            :header-cell-style="{ backgroundColor: '#F4F7F9' }"
            >
            <template slot="empty">
              <div class="empty">
                <img src="../../../assets/imgs/empty.png" alt="" />
                <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
              </div>
            </template>
            <el-table-column align="center" prop="currency_name" :label="$t('internationalsetting.hbmc')">
            </el-table-column>
            <el-table-column align="center" prop="currency_symbol" :label="$t('internationalsetting.hbfh')">
            </el-table-column>
            <el-table-column align="center" :label="$t('internationalsetting.hl')">
              <template slot-scope="scope">
                <el-input class="logisticsSort" v-model="scope.row.exchange_rate" @change="settingRates(scope.row)" @input="handleInput(scope.row)"/>
              </template>
            </el-table-column>
            <el-table-column align="center" :label="$t('internationalsetting.sfmrhb')">
              <template slot-scope="scope">
                <el-switch @change="Switch(scope.row)" :disabled="isSwitchFlag" v-model="switchFlag = scope.row.default_currency == 1 " :active-value="true" :inactive-value="false" />
              </template>
            </el-table-column>
            <el-table-column align="center" fixed="right" :label="$t('internationalsetting.cz')" width="251">
              <template slot-scope="scope">
                <div class="OP-button">
                  <div class="OP-button-top">
                    <el-button icon="el-icon-delete" :disabled=" scope.row.default_currency == 1 " @click="Delete(scope.row)" >{{$t('logisticsCompanyManage.logisticsCompanyList.shanchu')}}</el-button>
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
        <div class="footer-button">

       <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')" v-if="!isNone && isSwitchFlag>=0">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
        </div>

      </el-form>

    </div>
  </div>
</template>

<script>
import international from '@/webManage/js/mall/internationalSetting/setting'
export default international
</script>

<style scoped lang="less">
@import '../../../webManage/css/mall/internationalSetting/setting.less';
</style>
