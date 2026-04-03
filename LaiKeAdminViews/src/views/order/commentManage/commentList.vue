<template>
  <div class="container">
    <div class="Search">
      <el-select
        class="select-input"
        v-model="dataForm.CommentType"
        :placeholder="$t('commentList.pjlx')"
      >
        <el-option
          v-for="(item, index) in commentTypeList"
          :key="index"
          :label="item.name"
          :value="item.val"
        >
          <div>{{ item.name }}</div>
        </el-option>
      </el-select>

      <div class="Search-condition">
        <div class="query-input">
          <el-input
            size="medium"
            v-model="dataForm.orderNo"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('commentList.qsrddbh')"
          ></el-input>
        </div>
        <div class="query-input">
          <el-input
            size="medium"
            v-model="dataForm.mchName"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('commentList.qsrdpmc')"
          ></el-input>
        </div>
        <div class="select-date">
          <!-- <span>评论日期：</span> -->
          <el-date-picker
            v-model="dataForm.choseDate"
            type="datetimerange"
            :range-separator="$t('reportManagement.businessReport.zhi')"
            :start-placeholder="$t('reportManagement.businessReport.ksrq')"
            :end-placeholder="$t('reportManagement.businessReport.jsrq')"
            value-format="yyyy-MM-dd HH:mm:ss"
            :editable="false"
          >
          </el-date-picker>
        </div>

        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand"
            >{{ $t("DemoPage.tableExamplePage.demand") }}
          </el-button>
        </div>
      </div>
    </div>
    <div class="merchants-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="page.loading"
        :data="page.tableData"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
      >
      <template slot="empty">
          <div class="empty">
              <img src="../../../assets/imgs/empty.png" alt="">
              <p style="color: #414658 ;">{{$t('zdata.zwsj') }}</p>
          </div>
      </template>
        <el-table-column :label="$t('commentList.xh')" width="120">
          <template slot-scope="scope">
            {{ scope.$index + page.currpage }}
          </template>
        </el-table-column>
        <el-table-column prop="uid" :label="$t('commentList.yhid')"> </el-table-column>
        <el-table-column prop="user_name" :label="$t('integralOrder.yhmc')" width="120"> </el-table-column>
        <el-table-column prop="p_name" :label="$t('commentList.spmc')" width="180"> </el-table-column>
        <el-table-column prop="shop_name" :label="$t('commentList.dpmc')" width="200"> </el-table-column>

        <el-table-column prop="r_sNo" :label="$t('commentList.ddbh')" width="200"> </el-table-column>

        <el-table-column :label="$t('commentList.pf')">
          <template slot-scope="scope">
            <i
              class="laiketui laiketui-haoping"
              v-if="scope.row.CommentType > 3"
            ></i>
            <i
              class="laiketui laiketui-chaping"
              v-else-if="scope.row.CommentType < 3"
            ></i>
            <i class="laiketui laiketui-zhongping" v-else></i>
          </template>
        </el-table-column>
        <el-table-column prop="content" :label="$t('commentList.pjnr')" width="328">
        </el-table-column>

        <!-- <el-table-column prop="p_price" :label="$t('commentList.ddje')"> </el-table-column> -->
        <el-table-column prop="img" :label="$t('commentList.pjtp')"  width="200">
          <template slot-scope="scope">
            <div class="comment-img" style="margin: auto;">
              <viewer :images="sales_imgs">
                <img
                  v-for="src in scope.row.commentImgList"
                  :src="src"
                  :key="src"
                  @error="handleErrorImg"
                />
              </viewer>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commentList.pjsj')" width="200">
          <template slot-scope="scope">
            {{ scope.row.add_time | dateFormat }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('commentList.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  v-has-permi="'viewComment'"
                  v-if="scope.row.isMain"
                  icon="el-icon-edit-outline"
                  @click="Edit(scope.row.id,1)"
                  >{{$t('commentList.xg')}}</el-button
                >
                <el-button
                  v-has-permi="'viewComment'"
                  v-if="!scope.row.isMain"
                  icon="el-icon-edit-outline"
                  @click="Edit(scope.row.id,2)"
                  >{{$t('commentList.ck')}}</el-button
                >
                <el-button
                  v-has-permi="'delComments'"
                  icon="el-icon-delete"
                  @click="Delete(scope.row.id)"
                  >{{$t('commentList.shanchu')}}</el-button
                >
              </div>
              <div class="OP-button-bottom">
                <el-button
                  icon="el-icon-chat-dot-square"
                  @click="myReply(scope.row.id)"
                  v-has-permi="'viewComment'"
                  v-if="
                    scope.row.isMain&&scope.row.replyText == null||scope.row.replyText == ''
                  "
                >
                  {{$t('commentList.hf')}}
                </el-button>
                <el-button
                  v-has-permi="'commentReply'"
                  icon="el-icon-view"
                  @click="see(scope.row.id)"
                  >{{$t('commentList.mx')}}</el-button
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
      <div class="pageBox" style="" v-else>

      </div>
    </div>
    <!-- 弹框组件 -->
    <div class="dialog-block">
      <el-dialog
        :title="$t('commentList.hf')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
        width="580px"
      >
        <el-form
          :model="dataForm2"
          :rules="rules2"
          ref="dataForm2"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="" style="margin-bottom: 85px;padding-right: 40px;">
            <el-form-item :label="$t('commentList.hfnr')" prop="replyText">
              <el-input type="textarea" v-model="dataForm2.replyText" :placeholder="$t('commentList.qsrhf')"></el-input>
            </el-form-item>

          </div>
          <div class="form-footer">

              <el-button class="fontColor" @click="handleClose()"
                >{{$t('commentList.ccel')}}</el-button
              >
              <el-button
                type="primary"
                @click="Reply('ruleForm2')"
                class="qdcolor"
                >{{$t('commentList.okk')}}</el-button
              >

          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>


</template>

<script>
import list from "@/webManage/js/order/commentManage/commentList";
export default list;
</script>


<style scoped lang="less">
/deep/.el-dialog__header{
  border-bottom: 1px solid #E9ECEF ;
  padding-bottom: 1.25rem
}
/deep/.el-dialog{
  margin-top: 35vh!important;
}
/deep/.form-footer{
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  // height: 72px;
  padding: 17px;
  border-top: 1px solid #E9ECEF ;
  display: flex;
  justify-content: end
}
.query-input{
  margin-right: 0!important;
}

@import "../../../common/commonStyle/form";
@import "../../../webManage/css/order/commentManage/commentList";
</style>
