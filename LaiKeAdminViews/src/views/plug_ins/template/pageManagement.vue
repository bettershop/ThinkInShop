<template>
    <div class="container">
        <div class="menu-list" ref="tableFather">
          <div class="Search">
            <div class="Search-condition">
              <div class="query-input">
                <el-input
                  clearable
                  class="input-name"
                  @keyup.enter.native="demand"
                  v-model="inputInfo.name"
                  :placeholder="$t('template.pageList.qsrymcm')"
                ></el-input>

                <el-select
                  class="select-input"
                  v-model="inputInfo.status"
                  :placeholder="$t('salesReturnList.qxzzt')"
                >
                  <el-option
                    v-for="item in labelList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  >
                  </el-option>
                </el-select>
            </div>
            <div class="btn-list">
              <el-button class="fontColor" @click="reset">{{
                $t('DemoPage.tableExamplePage.reset')
              }}</el-button>
              <el-button class="bgColor" type="primary" @click="demand">{{
                $t('DemoPage.tableExamplePage.demand')
              }}</el-button>
          </div>
      </div>
          </div>
          <template v-if="tableData && tableData.length == 0">
            <div class="empty"  style="height: 490px;text-align: center;display: flex;flex-direction: column;align-items: center;justify-content: center;">
              <img src="../../../assets/imgs/wu.png" alt="" style="width: 200px;">
              <p style="color: ##97A0B4">{{ $t('zdata.zwsj') }}</p>
            </div>
          </template>
          <template v-else>
              <el-table
                :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
                v-loading="loading"
                :data="tableData"
                ref="table"
                class="el-table"
                height="490"
                :header-cell-style="handleHeaderCellStyle"
                :cell-style="handleHeaderCellStyle"
              >
                <el-table-column prop="" :label="$t('template.pageManafenebt.xh')"  >
                  <template slot-scope="{row}">
                    <span>{{ row.id }}</span>
                  </template>
                </el-table-column>
                <!-- <el-table-column :label="$t('template.pageManafenebt.slt')"  >
                  <template slot-scope="{row}">
                    <div class="img-box">
                        <img v-if="row.image" :src="row.image" class="wt-icon">
                    </div>
                  </template>
                </el-table-column> -->
                <el-table-column
                  prop="page_name"
                  :label="$t('template.pageManafenebt.ymmc')"
                  
                >
                </el-table-column>
                <el-table-column
                  prop="mchName"
                  :label="$t('template.pageManafenebt.zt')"
                 
                >
                <template slot-scope="{row}">
                  <span>{{ row.status == 0?'待使用':'已使用' }}</span>
                </template>
                </el-table-column>
                <el-table-column
                  prop="link"
                  :label="$t('template.pageManafenebt.ljdz')"
                   

                >
                <template slot-scope="{row}">
                  <span>{{ row.link }}</span>
                </template>
                </el-table-column>
                <el-table-column prop="create_by" :label="$t('template.pageManafenebt.cjr')"  >
                </el-table-column>
                <el-table-column
                  prop="update_time"
                  :label="$t('template.pageManafenebt.zjczsj')"
                  
                >
                </el-table-column>

                <el-table-column
                  fixed="right"
                  :label="$t('template.pageManafenebt.cz')"
                  
                >
                  <template slot-scope="{row}">
                    <div class="OP-button">
                      <div class="OP-button-top">
                        <template  >
                          <el-button
                          icon="el-icon-link"
                            @click="copyThelink(row)"
                            >{{ $t('template.pageManafenebt.fzlj') }}</el-button
                          >
                        </template>
                        <template >
                          <el-button
                            icon="el-icon-edit-outline"
                            @click="upPageItem(row, 2)"
                            >{{ $t('template.pageManafenebt.bjym') }}</el-button
                          >
                        </template>
                        <template >
                          <el-button
                             icon="el-icon-view"
                             :disabled="row.status == 0"
                              :class="{ 'disabled-btn': row.status == 0 }"

                            @click="lockInfo(row)"
                            >{{ $t('template.pageManafenebt.glzt') }}</el-button
                          >
                        </template>
                        <template >
                          <el-button
                            icon="el-icon-delete"
                            @click="delPageById(row)"
                            >{{ $t('template.pageManafenebt.scym') }}</el-button
                          >
                        </template>
                      </div>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </template>
      <div class="pageBox" ref="pageBox1" v-if="tableData && tableData.length >0" >
        <div class="pageLeftText">
          {{ $t('DemoPage.tableExamplePage.show') }}
        </div>
            <el-pagination
             layout="sizes, slot, prev, pager, next"
            :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
            :next-text="$t('DemoPage.tableExamplePage.next_text')"
            @size-change="handleSizeChange"
            :page-sizes="pagesizes"
            :current-page="currpage"
            @current-change="handleCurrentChange"
            :total="total"
            >
            <div class="pageRightText">
                {{ $t('DemoPage.tableExamplePage.on_show') }}{{ currpage }}-{{
                current_num
                }}{{ $t('DemoPage.tableExamplePage.twig') }}{{ total
                }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
            </div>
            </el-pagination>
        </div>
    </div>
      <!-- 查看关联主题 -->
      <el-dialog :title="$t('template.pageManafenebt.clglzt')" :visible.sync="dialogFormVisible" class="dialog dit"
      :before-close="handleClose"   >

            <template >
                <el-form :model="createdFrom" :rules="rules" ref="createdFromRef" inline  >
                    <el-form-item>
                        <el-input v-model="createdFrom.ztName" autocomplete="off" :placeholder="$t('template.pageManafenebt.qsrymmc')"></el-input>
                    </el-form-item>
                    <el-form-item label='' >
                        <el-button type="primary" @click="quyerListByText">{{ $t('template.pageManafenebt.cx') }}</el-button>
                    </el-form-item>
                </el-form>
                <template  v-if="gridData.length <=0">
                      <div style="height: 476px;" class="empty">
                          <img src="../../../assets/imgs/wu.png" alt="" />
                          <p style="color: #97A0B4">{{ $t('template.pageManafenebt.zwzt') }}</p>
                      </div>
                  </template>
                  <template v-else>

                <el-table :data="gridData"
                height="400"
                :header-cell-style="handleHeaderCellStyle"
                :cell-style="handleHeaderCellStyle">

                    <el-table-column property="bind_id" :label="$t('template.pageManafenebt.xh')" ></el-table-column>
                    <el-table-column property="name" :label="$t('template.divTemplate.ztmc')" ></el-table-column>
                    <el-table-column property="bind_time" :label="$t('template.pageManafenebt.bdsj')"></el-table-column>
                    <el-table-column :label="$t('template.pageManafenebt.cz')">
                        <template slot-scope="{row}">
                            <el-button type="text" @click="toThemeById(row)">{{ $t('template.pageManafenebt.dw') }}</el-button>
                            <el-button type="text" @click="unbindById(row)">{{ $t('template.pageManafenebt.jb') }}</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="pageBox" ref="pageBox"  >
                    <el-pagination
                    layout="slot, pager, next"
                    :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
                    :next-text="$t('DemoPage.tableExamplePage.next_text')"
                    @size-change="handleSizeChange1"
                    :page-sizes="pagesizes"
                    :current-page="pagination.page"
                    @current-change="handleCurrentChange1"
                    :total="total1"
                    >
                    <div class="pageRightText">
                        {{ $t('DemoPage.tableExamplePage.on_show') }}{{ currpage1 }}-{{
                        current_num1
                        }}{{ $t('DemoPage.tableExamplePage.twig') }}{{ total1
                        }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
                    </div>
                    </el-pagination>
                </div>
              </template>

            </template>

      </el-dialog>
    </div>
  </template>

  <script>
  import pageManagement from '@/webManage/js/plug_ins/template/pageManagement'
  export default pageManagement
  </script>

  <style scoped lang='less'>
  @import '../../../webManage/css/plug_ins/template/pageManagement.less';
  </style>
