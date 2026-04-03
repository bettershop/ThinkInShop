<template>
  <div class="container">
    <!-- <div class="header">
      <span>编辑活动</span>
    </div> -->
    <el-form :model="ruleForm" label-position="right" ref="ruleForm" label-width="100px" class="form-search">
      <div class="notice">
        <el-form-item :label="$t('template.addActivity.hdlx')">
          <el-radio-group v-model="ruleForm.activeType">
            <el-radio v-for="item in activeTypeList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <div class="activity-project" v-if="ruleForm.activeType === 0">
          <el-form-item :label="$t('template.addActivity.bt')" required>
            <el-input v-model="ruleForm.activeTitle" size="medium" class="Search-input" :placeholder="$t('template.addActivity.qsrbt')"></el-input>
            <span class="up_font">{{$t('template.addActivity.btrh')}}</span>
          </el-form-item>
          <el-form-item class="goos-block" :label="$t('template.addActivity.zssp')" required>
            <el-button plain @click="AddPro" class="cancel">{{$t('template.addActivity.tjsp')}}</el-button>
            <div class="changeUser" style="height: 330px;">
              <el-table :data="ChangeProList" height="100%" :header-cell-style="header">
                <el-table-column prop="proName" align="center" :label="$t('template.addActivity.spxx')">
                  <template slot-scope="scope">
                    <div class="Info">
                      <img :src="scope.row.imgurl" width="40" height="40" alt="">
                      <span>{{ scope.row.product_title  }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="price" align="center" :label="$t('template.addActivity.jg')">
                </el-table-column>
                <el-table-column prop="num" align="center" :label="$t('template.addActivity.kc')">
                </el-table-column>
                <el-table-column prop="action" align="center" :label="$t('template.cz')">
                    <template slot-scope="scope">
                        <el-button class="delete" @click="ChangeProdel(scope.$index,scope.row)" plain icon="el-icon-delete">{{$t('template.shanchu')}}</el-button>
                    </template>
                </el-table-column>
              </el-table>
            </div>
          </el-form-item>
        </div>
        <div class="plug-in" v-else>
          <el-form-item :label="$t('template.addActivity.cjlx')" required>
            <el-select class="Search-select" v-model="ruleForm.plugType" :placeholder="$t('template.addActivity.qxzcj')">
              <el-option v-for="item in plugTypeList" :key="item.value" :label="item.name" :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('template.addActivity.bt')" required v-if="ruleForm.plugType !== 10">
            <el-input v-model="ruleForm.plugTitle" size="medium" class="Search-input" :placeholder="$t('template.addActivity.qsrbt')"></el-input>
          </el-form-item>
          <el-form-item :label="$t('template.addActivity.szhd')" required v-else>
            <el-select class="Search-select" multiple v-model="ruleForm.setActivity" :placeholder="$t('template.addActivity.qxzcj')">
              <el-option v-for="item in setActivityList" :key="item.id" :label="item.name" :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </div>
        <div class="footer-button">
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{$t('template.addActivity.save')}}</el-button>
          <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">{{$t('template.ccel')}}</el-button>
        </div>
      </div>
        
      
    </el-form>

    <el-dialog :title="$t('template.addActivity.tjsp')" :visible.sync="dialogVisible3" width="920px">
      <div class="">
        <div class="Search" style="margin-top: 20px;">
          <el-cascader
            v-model="goodsClass"
            class="select-input"
            ref="myCascader"
            :placeholder="$t('template.addActivity.qszspfl')"
            :options="classList"
            :props="{ checkStrictly: true }"
            @change="changeProvinceCity"
            clearable>
          </el-cascader>
          <el-select class="Search-select" v-model="brand" :placeholder="$t('template.addActivity.qxzsppp')">
              <el-option v-for="item in brandList" :key="item.brand_id" :label="item.brand_name" :value="item.brand_id">
              </el-option>
          </el-select>
          <el-input class="Search-input" :placeholder="$t('template.addActivity.qsrspmc')" v-model="Proname"></el-input>
          <el-button @click="Reset" plain>{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button @click="query" type="primary" v-enter="query">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
        </div>
        <div class="Table">
          <el-table :data="ProList" :row-key="rowKeys" ref="multipleTable" style="width: 100%" @selection-change="handleSelectionChange2" height="350" :header-cell-style="header">
            <el-table-column :reserve-selection="true" align="center" type="selection" width="55"></el-table-column>
            <el-table-column prop="ProName" align="center" :label="$t('template.addActivity.spmc')">
              <template slot-scope="scope">
                <div class="Info">
                  <img :src="scope.row.imgurl" width="40" height="40" alt="">
                  <span>{{scope.row.product_title}}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="price" align="center" :label="$t('template.addActivity.jg')">
            </el-table-column>
            <el-table-column prop="num" align="center" :label="$t('template.addActivity.kc')">
            </el-table-column>
          </el-table>
          <div class="pageBox">
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
      <span slot="footer" class="dialog-footer">
          <el-button class="mb_bt" @click="cancel">{{$t('template.ccel')}}</el-button>
          <el-button type="primary" @click="AddProconfirm">{{$t('template.okk')}}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import editorActivity from '@/webManage/js/plug_ins/template/editorActivity'
export default editorActivity
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/template/editorActivity.less';
</style>