<template>
    <div class="container merchants-list">
      <!-- <div class="btn-nav">
        <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
          <el-radio-button :label="$t('systemManagement.jcpz')" @click.native.prevent="$router.push('/mall/basicConfiguration')"></el-radio-button>
          <el-radio-button :label="$t('systemManagement.xypz')" @click.native.prevent="tabJump()" ></el-radio-button>
          <el-radio-button :label="$t('systemManagement.cjwt')" @click.native.prevent="$router.push('/mall/commonProblem')"></el-radio-button>
          <el-radio-button :label="$t('systemManagement.xszn')" @click.native.prevent="$router.push('/mall/newbieGuide')"></el-radio-button>
          <el-radio-button :label="$t('systemManagement.shfw')" @click.native.prevent="$router.push('/mall/afterSales')"></el-radio-button>
          <el-radio-button :label="$t('systemManagement.gywm')" @click.native.prevent="$router.push('/mall/aboutUs')"></el-radio-button>
        </el-radio-group>
      </div> -->
      <div class="jump-list" >
        <el-button
          class="bgColor laiketui laiketui-add"
          type="primary"
          @click="$router.push('/mall/agreement/addAgreement')"
        >
          {{$t('systemManagement.protocolConfiguration.tjxy')}}
        </el-button>
      </div>
      <div class="menu-list" ref="tableFather">
        <el-table
          :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
          v-loading="loading"
          :data="tableData"
          ref="table"
          class="el-table"
          style="width: 100%"
          height="600"
        >
        <template slot="empty">
                <div class="empty">
                  <img src="../../../../assets/imgs/empty.png" alt="" />
                  <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
                </div>
              </template>
          <el-table-column fixed="left" prop="id" :label="$t('systemManagement.protocolConfiguration.xh')"> </el-table-column>
          <el-table-column prop="name" :label="$t('systemManagement.protocolConfiguration.bt')"> </el-table-column>
          <el-table-column prop="" :label="$t('systemManagement.protocolConfiguration.lx')">
            <template slot-scope="scope">
              <span>{{
                scope.row.type === 0
                  ? $t('systemManagement.protocolConfiguration.zc')
                  : scope.row.type === 1
                  ? $t('systemManagement.protocolConfiguration.dpsq') 
                  : scope.row.type === 2
                  ? $t('systemManagement.protocolConfiguration.ysxy') 
                  : $t('systemManagement.protocolConfiguration.hyxy') 
              }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="modify_date" :label="$t('systemManagement.protocolConfiguration.fbsj')">
            <template slot-scope="scope">
              <span>{{ scope.row.modify_date | dateFormat }}</span>
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="$t('systemManagement.protocolConfiguration.cz')" width="200">
            <template slot-scope="scope">
              <div class="OP-button">
                <div
                  class="OP-button-top"
                >
                  <el-button
                    
                    icon="el-icon-edit-outline"
                    @click="Edit(scope.row)"
                    >{{$t('systemManagement.protocolConfiguration.bianji')}}</el-button
                  >
                  <el-button
                    
                    class="el-icon-delete"
                    @click="Delete(scope.row)"
                    >{{$t('systemManagement.protocolConfiguration.shanchu')}}</el-button
                  >
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="pageBox" ref="pageBox" v-if="showPagebox">
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
  import protocolConfiguration from "@/webManage/js/mall/systemManagement/protocolConfiguration";
  export default protocolConfiguration;
  </script>
  
  <style scoped lang="less">
  @import "../../../../webManage/css/mall/systemManagement/protocolConfiguration.less";
  </style>