<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-select
            class="select-input"
            v-model="inputInfo.lang_code"
            :placeholder="$t('qxzyz')"
          >
            <el-option
              v-for="item in languages"
              :key="item.lang_code"
              :label="item.lang_name"
              :value="item.lang_code"
            >
            </el-option>
          </el-select>
          <el-input
            size="medium"
            v-model="inputInfo.name"
            @keyup.enter.native="demand"
            :placeholder="$t('tagList.qsrspbq')"
          ></el-input>
        </div>
        <div class="btn-list">
          <el-button @click="reset" plain>{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button class="bgColor" type="primary" @click="demand"
            >{{ $t("DemoPage.tableExamplePage.demand") }}
          </el-button>
        </div>
      </div>
    </div>
    <div class="jump-list">
      <el-button
        v-if="detectionBtn(button_list, '添加商品标签')"
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="dialogShow"
        >{{$t('tagList.xzbq')}}
      </el-button>
    </div>

    <!-- 弹框组件 -->
    <div class="dialog-block">
      <el-dialog
        :title="title"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <el-form
          :model="dataForm"
          :rules="rules"
          ref="ruleForm2"
          label-width="auto"
          class="demo-ruleForm"
        >
          <div class="">
            <el-form-item :label="$t('tagList.bqmc')" prop="name">
              <el-input type="text" maxlength="4" v-model="dataForm.name"></el-input>
            </el-form-item>
            <el-form-item :label="$t('tagList.ssgj')" prop="country_num">
              <el-select class="select-input" filterable v-model="dataForm.country_num" :placeholder="$t('tagList.qxzssgj')">
                <el-option v-for="(item,index) in countryList" :key="index" :label="item.zh_name" :value="item.num3">
                  <div @click="getIds(item.num3)">{{ item.zh_name }}</div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('yz')" prop="lang_code">
              <el-select :disabled="lang_disabled" class="select-input" filterable v-model="dataForm.lang_code" :placeholder="$t('qxzyz')">
                <el-option v-for="(item,index) in languages" :key="index" :label="item.lang_name" :value="item.lang_code">
                  <div @click="getlangCode(item.lang_code)">{{ item.lang_name }}</div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('tagList.qxzbjs')" required style="margin-bottom:0">
              <el-color-picker v-model="dataForm.color" popper-class="color-popper"></el-color-picker>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button class="fontColor" @click="handleClose()"
                >{{$t('tagList.ccel')}}</el-button
              >
              <el-button
                type="primary"
                @click="Save('ruleForm2')"
                class="qdcolor"
                >{{$t('tagList.okk')}}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>

    <div class="merchants-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="loading"
        :data="tableData"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
      >
      <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column prop="id" :label="$t('tagList.bqid')" width="100"> </el-table-column>
        <el-table-column prop="" :label="$t('tagList.bq')">
          <template slot-scope="scope">
            <div style="display:flex;justify-content: center;">
              <div class="mytag" :style="{ background: scope.row.color }">
                {{ scope.row.name }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('tagList.bqmc')"> </el-table-column>
        <el-table-column prop="lang_name" :label="$t('yz')"> </el-table-column>
        <el-table-column :label="$t('tagList.tjsj')">
          <template slot-scope="scope">
            {{ scope.row.add_time | dateFormat }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('tagList.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  v-if="detectionBtn(button_list, '编辑商品标签') && scope.row.name != '推荐' && scope.row.name != '热销' && scope.row.name != '新品'"
                  icon="el-icon-edit-outline"
                  @click="Show(scope.row.id)"
                  >{{$t('tagList.bianji')}}</el-button
                >
                <el-button
                  v-if="detectionBtn(button_list, '删除商品标签') && scope.row.name != '推荐' && scope.row.name != '热销' && scope.row.name != '新品'"
                  icon="el-icon-delete"
                  @click="Delete(scope.row.id)"
                  >{{$t('tagList.shanchu')}}</el-button
                >
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
  </div>
</template>


<script>
import tagList from "@/webManage/js/goods/tag/tagList";
// import { backgroundClip } from "html2canvas/dist/types/css/property-descriptors/background-clip";
export default tagList;
</script>
<style>
.color-popper{
  left: 832px !important;
}
</style>
<style scoped lang="less">
.mytag{
  padding: 0 5px;
  height: 22px;
  border-radius: 4px;
  color: #fff;
}
.mytag1{
  width: 32px;
  margin: 8px 0;
}
/deep/.myselect .el-input{
  width: 360px!important;
}

/deep/.el-dialog__header{
  border-bottom:1px solid #E9ECEF!important;
}
/deep/.el-button--default{
  background: #fff
}

/deep/.Search {
  .select-input {
    margin-right: 10px;
  }
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 16px;
  .Search-condition {
    display: flex;
    align-items: center;
    .query-input {
      display: flex;
      // margin-right: 10px;
      .Search-input {
        width: 280px;
        margin-right: 10px;
        .el-input__inner {
          height: 40px;
          line-height: 40px;
          border: 1px solid #d5dbe8;
        }
        .el-input__inner:hover {
          border: 1px solid #b2bcd4;
        }
        .el-input__inner:focus {
          border-color: #409eff;
        }
        .el-input__inner::-webkit-input-placeholder {
          color: #97a0b4;
        }
      }
    }

  }
}

@import "../../../common/commonStyle/form";
@import "../../../webManage/css/goods/tag/tagList.less";
</style>
