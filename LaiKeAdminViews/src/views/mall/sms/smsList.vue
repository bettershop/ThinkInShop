<template>
  <div class="container">
    <!-- <el-main> -->
      <!-- tbl页 -->
      <div class="btn-nav">
        <el-radio-group v-model="tabPosition" @change="tbl" ref="tab_bun">
          <el-radio-button label="2" v-if="handleTabLimits(routerList,'smsTemplate',1)" >{{$t('smsList.gndxmb')}}</el-radio-button>
          <el-radio-button label="4" v-if="handleTabLimits(routerList,'smsTemplate',1)" >{{$t('smsList.gwdxmb')}}</el-radio-button>
          <el-radio-button label="3" v-if="handleTabLimits(routerList,'CoreSetup',1)">{{$t('smsList.hxsz')}}</el-radio-button>
        </el-radio-group>
      </div>
      <div class="hr"></div>
      <div class="jump-list" v-show="tabPosition!=='3'">
        <el-button v-if="tabPosition == '1'" class="bgColor laiketui laiketui-add" type="primary" @click="Save()">{{$t('smsList.tj')}}</el-button>
        <el-button v-if="tabPosition == '2'||tabPosition == '4'" class="bgColor laiketui laiketui-add" type="primary" @click="Add()">{{$t('smsList.tjmb')}}</el-button>

      </div>
      <!-- 短信列表 -->
      <!-- <div class="m-table" v-if="tabPosition!=='3'"> -->
        <div class="merchants-list" v-show="tabPosition!=='3'" ref="tableFather">
          <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="page.loading" :data="page.tableData" ref="table"
            class="el-table"
            style="width: 100%"
            :key="flag"
            :height="tableHeight">
            <template slot="empty">
              <div class="empty">
                <img src="../../../assets/imgs/empty.png" alt="" />
                <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
              </div>
            </template>
            <div v-if="tabPosition==='1'">
              <el-table-column :label="$t('smsList.xgsj')" align="center">
                <template slot-scope="scope">
                  {{scope.row.add_time | dateFormat}}
                </template>
              </el-table-column>
              <el-table-column prop="id" label="ID" width="120" align="center">
              </el-table-column>
              <el-table-column prop="NAME" :label="$t('smsList.dxmc')" align="center">
              </el-table-column>
              <el-table-column :label="$t('smsList.lx')" width="228" align="center">
                <template slot-scope="scope">
                  <div v-if="scope.row.type===0">{{$t('smsList.dxyzm')}}</div>
                  <div v-else>{{$t('smsList.tz')}}</div>
                </template>
              </el-table-column>
              <el-table-column prop="content1" :label="$t('smsList.nr')"   align="left">
              </el-table-column>
            </div>
            <!-- 短信模板 -->
            <div v-else-if="tabPosition==='2' || tabPosition==='4'">
              <el-table-column prop="add_time" :label="$t('smsList.xgsj')" align="center">
                <template slot-scope="scope">
                  {{scope.row.add_time | dateFormat}}
                </template>
              </el-table-column>
              <el-table-column width="88" prop="id" label="ID" align="center">
              </el-table-column>
              <el-table-column :label="$t('smsList.dxmbmc')" prop="name" align="center">
              </el-table-column>
              <el-table-column :label="$t('smsList.lx')" width="228" align="center">
                <template slot-scope="scope">
                  <div v-if="scope.row.type===0">{{$t('smsList.dxyzm')}}</div>
                  <div v-else>{{$t('smsList.tz')}}</div>
                </template>
              </el-table-column>
              <el-table-column :label="$t('smsList.dxcode')" prop="TemplateCode" align="center">
              </el-table-column>
              <el-table-column :label="$t('smsList.nr')" prop="content"   align="left">
              </el-table-column>
            </div>
              <!-- 短信列表按钮 -->
            <el-table-column fixed="right" :label="$t('smsList.cz')" width="200" v-if="tabPosition=='1'">
              <template slot-scope="scope">
                <div class="OP-button">
                  <div class="OP-button-top">
                    <el-button   icon="el-icon-edit-outline" @click="Save(scope.row.id)">{{$t('smsList.bianji')}}</el-button>
                    <el-button   icon="el-icon-delete" @click="Delete(scope.row.id)">{{$t('smsList.shanchu')}}</el-button>
                  </div>
                </div>
              </template>
            </el-table-column>
              <!-- 短信模板按钮 -->
            <el-table-column fixed="right" :label="$t('smsList.cz')" width="200" v-if="tabPosition=='2' || tabPosition =='4'">
              <template slot-scope="scope">
                <div class="OP-button">
                  <div class="OP-button-top">
                    <el-button   icon="el-icon-edit-outline" @click="Save(scope.row.id)">{{$t('smsList.bianji')}}</el-button>
                    <el-button   icon="el-icon-delete" @click="Delete(scope.row.id)">{{$t('smsList.shanchu')}}</el-button>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div class="pageBox" ref="pageBox">
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
      <!-- </div> -->
      <!-- 短信列表 end -->
       <!-- 核心设置 -->
      <div class="core-set" v-show="tabPosition=='3'">
        <el-form :model="mainForm" :rules="mainForm.rules" ref="ruleForm" class="picture-ruleForm" label-width="150px">
          <el-form-item label="accessKeyId" prop="accessKeyId">
            <el-input v-model="mainForm.accessKeyId"></el-input>
            <span class="left">{{$t('smsList.alyid')}}</span>
          </el-form-item>
          <el-form-item label="accessKeySecret" prop="accessKeySecret">
            <el-input v-model="mainForm.accessKeySecret" show-password></el-input>
            <span class="left">{{$t('smsList.alymy')}}</span>
          </el-form-item>

          <div class="form-footer">
            <el-form-item>
              <el-button class="bgColor" type="primary" @click="saveConfig('ruleForm')">{{ $t('DemoPage.tableFromPage.save')
                }}
              </el-button>
              <!-- <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}
              </el-button> -->
            </el-form-item>
          </div>
        </el-form>
      </div>
    <!-- </el-main> -->
  </div>
</template>

<script>
import main from "@/webManage/js/mall/sms/smsList";
export default main
</script>

<style scoped lang="less">
@import "../../../webManage/css/mall/sms/smsList.less";
</style>
