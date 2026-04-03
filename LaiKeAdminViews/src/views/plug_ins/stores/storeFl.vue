<template>
  <div class="container">

    <!-- tabbar -->
    <div class="btn-nav">
      <!-- 二级导航 -->
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('stores.dp')" @click.native.prevent="$router.push('/plug_ins/stores/store')" v-if="handleTabLimits(routerList,'/plug_ins/stores/store')"></el-radio-button>
        <el-radio-button :label="$t('stores.dpfl')" @click.native.prevent="$router.push('/plug_ins/stores/storeFl')" v-if="handleTabLimits(routerList,'/plug_ins/stores/storeFl')"></el-radio-button>
        <el-radio-button :label="$t('stores.dpsh')" @click.native.prevent="$router.push('/plug_ins/stores/auditList')" v-if="handleTabLimits(routerList,'/plug_ins/stores/auditList')"></el-radio-button>
        <el-radio-button :label="$t('stores.bzjjl')" @click.native.prevent="$router.push('/plug_ins/stores/bondMoney')" v-if="handleTabLimits(routerList,'/plug_ins/stores/bondMoney.vue')"></el-radio-button>
        <el-radio-button :label="$t('stores.bzjsh')" @click.native.prevent="$router.push('/plug_ins/stores/bondExamine')" v-if="handleTabLimits(routerList,'/plug_ins/stores/bondExamine.vue')"></el-radio-button>
        <el-radio-button :label="$t('stores.spsh')" @click.native.prevent="$router.push('/plug_ins/stores/goodsAudit')" v-if="handleTabLimits(routerList,'/plug_ins/stores/goodsAudit.vue')"></el-radio-button>
        <el-radio-button :label="$t('stores.txsh')" @click.native.prevent="$router.push('/plug_ins/stores/withdrawalAudit')" v-if="handleTabLimits(routerList,'/plug_ins/stores/withdrawalAudit.vue')"></el-radio-button>
        <el-radio-button :label="$t('stores.txjl')" @click.native.prevent="$router.push('/plug_ins/stores/withdrawalRecord')" v-if="handleTabLimits(routerList,'/plug_ins/stores/withdrawalRecord.vue')"></el-radio-button>
        <!-- <el-radio-button :label="$t('stores.dpsz')" @click.native.prevent="toset()"></el-radio-button> -->
      </el-radio-group>
    </div>



    <!-- 搜索 -->
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <!-- 请输入分类名称 -->
          <el-input v-model="inputInfo.storeName" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('stores.storeFl.qsrflmc')"></el-input>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <!-- 查询按钮 -->
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
        </div>
      </div>
	  </div>

    <!-- 添加分类 -->
    <div class="jump-list" style="margin-bottom: 1rem;">
      <span>
        <el-button class="bgColor laiketui laiketui-add shaco_bt  " type="primary"  @click="View('','add')">{{$t('stores.storeFl.tjfl')}}</el-button>
      </span>
    </div>

    <!-- 内容表格 -->
    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
		  :height="tableHeight">
      <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{$t('zdata.zwsj') }}</p>
          </div>
        </template>
        <!-- 分类ID -->
        <el-table-column  prop="id" fixed="left" label="ID">
        </el-table-column>
        <!-- 分类名称 -->
        <el-table-column  prop="name" :label="$t('stores.storeFl.flmc')">
        </el-table-column>
        <!-- 分类图片 -->
        <el-table-column  prop="store_info" :label="$t('stores.storeFl.tp')" width="410">
          <template slot-scope="scope">
            <div class="stores-info" style="margin: 0;justify-content: center;">
              <div class="head-img">
                <img :src="scope.row.img" alt="" @error="handleErrorImg" v-if="scope.row.img">
              </div>
            </div>
          </template>
        </el-table-column>
        <!-- 分类排序 -->
        <el-table-column prop="sort" :label="$t('stores.storeFl.px')">
        </el-table-column>
        <!-- 是否显示 -->
        <el-table-column :label="$t('stores.storeFl.sfxs')">
          <template slot-scope="scope">
            <el-switch @change="switchs(scope.row)" v-model="scope.row.is_display" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8"></el-switch>
          </template>
        </el-table-column>
        <!-- 创建时间 -->
        <el-table-column :label="$t('stores.storeFl.cjsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date }}</span>
          </template>
        </el-table-column>
        <!-- 操作 -->
        <el-table-column fixed="right" :label="$t('stores.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <span class="OP-button-top">
                <el-button icon="el-icon-edit-outline" @click="View(scope.row,'set')">{{$t('stores.bianji')}}</el-button>
                <el-button icon="el-icon-delete" @click="Through(scope.row)">{{$t('stores.shanchu')}}</el-button>
              </span>
            </div>
		      </template>
        </el-table-column>
	    </el-table>
      <!-- 页面跳转 上一页/下一页 -->
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
  </div>
</template>

<script>
import storeFl from '@/webManage/js/plug_ins/stores/storeFl'
export default storeFl
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/stores/auditList.less';
</style>
