<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('template.lbtlb')" @click.native.prevent="$router.push('/plug_ins/template/playlist')"></el-radio-button>
        <el-radio-button :label="$t('template.uidhl')" @click.native.prevent="$router.push('/plug_ins/template/navigationBar')"></el-radio-button>
        <el-radio-button :label="$t('template.flgl')" @click.native.prevent="$router.push('/plug_ins/template/Classification')"></el-radio-button>
        <el-radio-button :label="$t('template.hdgl')" @click.native.prevent="$router.push('/plug_ins/template/activity')"></el-radio-button>
      </el-radio-group>
    </div>

    <div class="jump-list">
      <el-button class="bgColor laiketui laiketui-add" type="primary" @click="$router.push('/plug_ins/template/addActivity')">
        {{$t('template.activity.tjhd')}}</el-button>
    </div>

    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%" :height="tableHeight">
        <el-table-column prop="" :label="序号" width="70">
          <template slot-scope="scope">
            <span>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('template.activity.bt')" width="200">
        </el-table-column>
        <el-table-column prop="activity_type" :label="$t('template.activity.hdlx')" width="300">
          <template slot-scope="scope">
            <span>{{ scope.row.activity_type === 0 ? $t('template.activity.hdzt') : $t('template.activity.yxcj') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('template.activity.tjsj')" width="400">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="is_display" :label="$t('template.activity.sfxs')" width="300">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.is_display" @change="switchs(scope.row)" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
          </template>
        </el-table-column> -->
        <el-table-column fixed="right" :label="$t('template.cz')" width="300">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button v-if=" scope.$index !=0  " @click="moveUp(scope.$index)">
                  <i class="el-icon-top"></i>
                  {{$t('template.playlist.sy')}}
                </el-button>
                <el-button v-if="scope.$index ==0" @click="moveUp(scope.$index)">
                  <i class="el-icon-bottom"></i>
                  {{$t('template.playlist.xy')}}
                </el-button>
                <el-button icon="el-icon-edit-outline" @click="Edit(scope.row)">{{$t('template.bianji')}}</el-button>
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">{{$t('template.shanchu')}}</el-button>
              </div>
              <div class="OP-button-bottom">
                <el-button icon="el-icon-attract" @click="Activity(scope.row)" v-if="scope.row.activity_type === 0 ">
                  {{$t('template.activity.hdsp')}}</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="total>0">
        <div class="pageLeftText">{{$t('DemoPage.tableExamplePage.show')}}</div>
        <el-pagination layout="sizes, slot, prev, pager, next" :prev-text="$t('DemoPage.tableExamplePage.prev_text')" :next-text="$t('DemoPage.tableExamplePage.next_text')" @size-change="handleSizeChange" :page-sizes="pagesizes" :current-page="pagination.page" @current-change="handleCurrentChange" :total="total">
          <div class="pageRightText">{{$t('DemoPage.tableExamplePage.on_show')}}{{currpage}}-{{current_num}}{{$t('DemoPage.tableExamplePage.twig')}}{{total}}{{ $t('DemoPage.tableExamplePage.twig_notes') }}</div>
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import activity from '@/webManage/js/plug_ins/template/activity'
export default activity
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/template/activity.less';
</style>
