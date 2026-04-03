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

    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%" :height="tableHeight">
        <!-- 序号 -->
        <el-table-column prop="" :label="$t('template.Classification.xh')" width="70">
          <template slot-scope="scope">
            <span>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <!-- 姓名分类 -->
        <el-table-column prop="pname" :label="$t('template.Classification.flmc')">
        </el-table-column>
        <!-- 是否显示 -->
        <el-table-column prop="is_display" :label="$t('template.Classification.sfxs')">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.is_display" @change="switchs(scope.row)" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
          </template>
        </el-table-column>
        <!-- 手术 -->
        <el-table-column fixed="right" :label="$t('template.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button v-if="scope.$index !=0  " @click="moveUp(scope.$index)">
                  <i class="el-icon-top"></i>
                  {{$t('template.playlist.sy')}}
                </el-button>
                <el-button v-if="scope.$index ==0" @click="moveUp(scope.$index)">
                  <i class="el-icon-bottom"></i>
                  {{$t('template.playlist.xy')}}
                </el-button>
                <el-button class="laiketui laiketui-zhiding" @click="placedTop(scope.row)" v-if="scope.$index !== 0">
                  {{$t('template.playlist.zd')}}</el-button>
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
import classification from '@/webManage/js/plug_ins/template/classification'
export default classification
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/template/classification.less';
</style>
