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
      <el-button class="bgColor laiketui laiketui-add" type="primary" @click="$router.push('/plug_ins/template/addSlideShow')">
        {{$t('template.playlist.tjlbt')}}</el-button>
    </div>
    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%" :height="tableHeight">
        <el-table-column prop="" :label="$t('template.playlist.xh')" width="70">
          <template slot-scope="scope">
            <span>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="image" :label="$t('template.playlist.tp')" width="670">
          <template slot-scope="scope">
            <img :src="scope.row.image" alt="">
          </template>
        </el-table-column>
        <el-table-column prop="url" :label="$t('template.playlist.lj')" width="450">
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('template.playlist.tjsj')" width="280">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('template.cz')" width="180">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button v-if="  scope.$index !=0  " @click="moveUp(scope.$index)">
                  <i class="el-icon-top"></i>
                  {{$t('template.playlist.sy')}}
                </el-button>
                <el-button style="margin-right: 95px;" v-if="scope.$index ==0" @click="moveUp(scope.$index)">
                  <i class="el-icon-bottom"></i>
                  {{$t('template.playlist.xy')}}
                </el-button>
                <el-button class="laiketui laiketui-zhiding" @click="placedTop(scope.row)" v-if=" scope.$index !== 0">
                  {{$t('template.playlist.zd')}}</el-button>
              </div>
              <div class="OP-button-bottom">
                <el-button icon="el-icon-edit-outline" @click="Edit(scope.row)">{{$t('template.bianji')}}</el-button>
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">{{$t('template.shanchu')}}</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="showPagebox">
        <div class="pageLeftText">{{$t('DemoPage.tableExamplePage.show')}}</div>
        <el-pagination layout="sizes, slot, prev, pager, next" :prev-text="$t('DemoPage.tableExamplePage.prev_text')" :next-text="$t('DemoPage.tableExamplePage.next_text')" @size-change="handleSizeChange" :page-sizes="pagesizes" :current-page="pagination.page" @current-change="handleCurrentChange" :total="total">
          <div class="pageRightText">{{$t('DemoPage.tableExamplePage.on_show')}}{{currpage}}-{{current_num}}{{$t('DemoPage.tableExamplePage.twig')}}{{total}}{{ $t('DemoPage.tableExamplePage.twig_notes') }}</div>
        </el-pagination>
      </div>
    </div>
  </div>
</template>


<script>
import playlist from '@/webManage/js/plug_ins/template/playlist'
export default playlist
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/template/playlist.less';
</style>
