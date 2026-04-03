<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            size="medium"
            v-model="inputInfo.lang_name"
            @keyup.enter.native="demand"
            :placeholder="$t('langmanager.qsrsyzmc')"
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
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="dialogShow"
      >{{$t('langmanager.xzyz')}}
      </el-button>
    </div>

    <!-- 弹框组件 -->
    <div class="dialog-block">
      <el-dialog
        :title="$t('langmanager.xzyz')"
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
            <el-form-item :label="$t('langmanager.yzmc')" prop="lang_name">
              <el-input type="text" maxlength="10" v-model="dataForm.lang_name"></el-input>
            </el-form-item>
            <el-form-item :label="$t('langmanager.yzbm')" prop="lang_code">
              <el-input type="text" maxlength="10" v-model="dataForm.lang_code"></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button class="fontColor" @click="handleClose()"
              >{{$t('langmanager.ccel')}}</el-button
              >
              <el-button
                type="primary"
                @click="Save('ruleForm2')"
                class="qdcolor"
              >{{$t('langmanager.okk')}}</el-button
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
        <el-table-column prop="lang_name" :label="$t('langmanager.yzmc')"> </el-table-column>
        <el-table-column prop="lang_code" :label="$t('yz')"> </el-table-column>
        <el-table-column :label="$t('langmanager.tjsj')">
          <template slot-scope="scope">
            {{ scope.row.op_time | dateFormat }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('langmanager.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  icon="el-icon-edit-outline"
                  @click="Show(scope.row.id)"
                >{{$t('langmanager.bianji')}}</el-button
                >
                <el-button
                  icon="el-icon-delete"
                  @click="Delete(scope.row.id)"
                >{{$t('langmanager.shanchu')}}</el-button
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
import langmanager from "@/webManage/js/platform/langmanager/langmanager";
export default langmanager;
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
@import "../../../webManage/css/platform/langmanager/langmanager";
</style>
