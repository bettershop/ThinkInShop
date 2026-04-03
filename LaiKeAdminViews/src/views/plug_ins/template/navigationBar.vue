<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('template.lbtlb')"
          @click.native.prevent="$router.push('/plug_ins/template/playlist')"></el-radio-button>
        <el-radio-button :label="$t('template.uidhl')"
          @click.native.prevent="$router.push('/plug_ins/template/navigationBar')"></el-radio-button>
        <el-radio-button :label="$t('template.flgl')"
          @click.native.prevent="$router.push('/plug_ins/template/Classification')"></el-radio-button>
        <el-radio-button :label="$t('template.hdgl')"
          @click.native.prevent="$router.push('/plug_ins/template/activity')"></el-radio-button>
      </el-radio-group>
    </div>

    <div class="jump-list">
        <el-button class="bgColor laiketui laiketui-add" type="primary"
          @click="dialogShow('',true)">{{$t('template.navigationBar.tjui')}}</el-button>
    </div>

    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading"
        :data="tableData" ref="table" class="el-table" style="width: 100%" :height="tableHeight">
        <el-table-column prop="" :label="$t('template.navigationBar.xh')" width="70">
          <template slot-scope="scope">
            <span>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="image" :label="$t('template.navigationBar.tb')">
          <template slot-scope="scope">
            <img :src="scope.row.image" alt="">
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('template.navigationBar.mc')">
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('template.navigationBar.tjsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="isshow" :label="$t('template.navigationBar.sfxs')">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.isshow" @change="switchs(scope.row)" :active-value="1" :inactive-value="0"
              active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="isshow" :label="$t('template.navigationBar.sfdl')">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.is_login" @change="switchs1(scope.row)" :active-value="1" :inactive-value="0"
              active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('template.cz')">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                  <el-button v-if="scope.$index !=0  " @click="moveUp(scope.$index)">
                    <i class="el-icon-top"></i>
                    {{$t('template.playlist.sy')}}
                  </el-button>
                  <el-button style="margin-right: 95px;" v-if="scope.$index ==0"
                    @click="moveUp(scope.$index)">
                    <i class="el-icon-bottom"></i>
                    {{$t('template.playlist.xy')}}
                  </el-button>
                  <el-button class="laiketui laiketui-zhiding" @click="placedTop(scope.row)"
                    v-if="scope.$index !== 0 ">
                    {{$t('template.playlist.zd')}}</el-button>
              </div>
              <div class="OP-button-bottom">
                  <el-button icon="el-icon-edit-outline"
                    @click="dialogShow(scope.row,false)">{{$t('template.bianji')}}</el-button>
                  <el-button icon="el-icon-delete"
                    @click="Delete(scope.row)">{{$t('template.shanchu')}}</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="showPagebox">
        <div class="pageLeftText">{{$t('DemoPage.tableExamplePage.show')}}</div>
        <el-pagination layout="sizes, slot, prev, pager, next" :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
          :next-text="$t('DemoPage.tableExamplePage.next_text')" @size-change="handleSizeChange" :page-sizes="pagesizes"
          :current-page="pagination.page" @current-change="handleCurrentChange" :total="total">
          <div class="pageRightText">
            {{$t('DemoPage.tableExamplePage.on_show')}}{{currpage}}-{{current_num}}{{$t('DemoPage.tableExamplePage.twig')}}{{total}}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
          </div>
        </el-pagination>
      </div>
    </div>

    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog :title="title" :visible.sync="dialogVisible" :before-close="handleClose">
        <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
          <!-- 名称 -->
          <el-form-item :label="$t('template.navigationBar.mc')" required>
            <el-input v-model="ruleForm.name" :placeholder="$t('template.navigationBar.qsrdh')"></el-input>
            <span class="up_font">{{$t('template.navigationBar.zdbn')}}</span>
          </el-form-item>
          <!-- 图标 -->
          <el-form-item class="upload-img" :label="$t('template.navigationBar.tb')" required>
            <l-upload :limit="1" ref="upload" v-model="ruleForm.img" :text="$t('template.navigationBar.jy82')">
            </l-upload>
          </el-form-item>
          <!-- 跳转地址 -->
          <el-form-item :label="$t('template.navigationBar.tzdz')">
            <el-select class="select-input" v-model="ruleForm.class1" placeholder="">
              <el-option v-for="(item,index) in classList1" :key="index" :label="item.label" :value="item.value">
                <div @click="change(item)">{{ item.label }}</div>
              </el-option>
            </el-select>
            <el-select v-if="ruleForm.class1 !== 0" filterable class="select-input selects" v-model="ruleForm.url"
              :placeholder="$t('template.navigationBar.qsrss')">
              <el-option v-for="(item,index) in classList2" :key="index" :label="item.name" :value="item.parameter">
                <div @click="change(item)">{{ item.name }}</div>
              </el-option>
            </el-select>
            <el-input v-else v-model="ruleForm.url"></el-input>
          </el-form-item>
          <!-- 是否显示 -->
          <el-form-item :label="$t('template.navigationBar.sfxs')" required>
            <el-switch v-model="ruleForm.is_display" :active-value="1" :inactive-value="0" active-color="#00ce6d"
              inactive-color="#d4dbe8">
            </el-switch>
          </el-form-item>
          <!-- 是否登录 -->
          <el-form-item :label="$t('template.navigationBar.sfdl')" required>
            <el-switch v-model="ruleForm.is_login" :active-value="1" :inactive-value="0" active-color="#00ce6d"
              inactive-color="#d4dbe8">
            </el-switch>
          </el-form-item>
          <div class="form-footer">
            <el-form-item>
              <!-- 取消 -->
              <el-button class="fontColor" @click="handleClose(true)">{{$t('template.ccel')}}</el-button>
              <!-- 确认 -->
              <el-button class="bdColor" type="primary"
                @click="submitForm2('ruleForm')">{{$t('template.okk')}}</el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>

  </div>
</template>

<script>
  import navigationBar from '@/webManage/js/plug_ins/template/navigationBar'
  export default navigationBar
</script>

<style scoped lang="less">
  @import '../../../webManage/css/plug_ins/template/navigationBar.less';
</style>
