<template>
  <div class="container">

    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.uname"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('membersLists.qsryhidmc')"
          ></el-input>
          <div class='diqu-box'>
            <!-- 区号 -->
             <el-autocomplete
             class='auto'
              v-model="state"
              :fetch-suggestions="querySearchAsync"
              :placeholder="$t('membersLists.qh')"
              @select="handleSelect"
            >
             <template slot-scope="{ item }">
              <div>
                <span>{{ item.name }}</span>
                <span style="color: #999; margin-left: 10px;">+({{ item.code2 }})</span>
              </div>
            </template>
            </el-autocomplete>

              <!-- 手机号 -->
            <el-input
              v-model="inputInfo.tel"
              size="medium"
              @keyup.enter.native="demand"
              class="Search-input"
              :placeholder="$t('membersLists.qsrsjh')"
            ></el-input>
          </div>

            <el-select
              class="select-input"
              v-model="inputInfo.source"
              :placeholder="$t('membersLists.zhly')"
            >
              <el-option
                v-for="(item, index) in sourceList"
                :key="index"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>

        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t("DemoPage.tableExamplePage.reset")
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand">{{
            $t("DemoPage.tableExamplePage.demand")
          }}</el-button>
          <el-button 
            class="bgColor export"
            type="primary"
            @click="dialogShow"
            >{{ $t("DemoPage.tableExamplePage.export") }}</el-button
          >
        </div>
      </div>
    </div>

    <div class="jump-list">
      <el-button 
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="addMembers"
      >
        {{ $t("membersLists.tjyh") }}</el-button
      >
      <el-button 
        class="batchImport"
        type="primary"
        icon="el-icon-upload2"
        @click="$router.push('/members/membersList/batch')"
        >{{ $t("membersLists.pldr") }}</el-button
      >
      <el-button 
        class="fontColor"
        icon="el-icon-tickets"
        @click="$router.push('/members/membersList/batchRecord')"
        >{{ $t("membersLists.drjl") }}</el-button
      >
    </div>

    <div class="menu-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="loading"
        :data="tableData"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
        :cell-class-name="addClass"
      >
        <!-- 空状态 -->
        <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #97a0b4">{{ $t("zdata.zwsj") }}</p>
          </div>
        </template>
        <!-- 用户ID -->
        <el-table-column
          fixed="left"
          prop="user_id"
          :label="$t('membersLists.yhid')"
          width="95"
        ></el-table-column>
        <!-- 用户头像 -->
        <el-table-column prop="imgurl" :label="$t('membersLists.yhtx')">
          <template slot-scope="scope">
            <img :src="scope.row.headimgurl" alt="" @error="handleErrorImg" />
          </template>
        </el-table-column>
        <!-- 用户名称 -->
        <el-table-column prop="user_name" :label="$t('membersLists.yhmc')">
        </el-table-column>
        <!-- 用户账号 -->
        <el-table-column prop="zhanghao" width="150" :label="$t('membersLists.yhzh')">
        </el-table-column>
        <!-- 手机号 -->
        <el-table-column prop="mobile" width="150" :label="$t('membersLists.sjhm')">
          <template slot-scope='{row}'>
            <span v-if='row.mobile'>+{{row.cpc}}<span>{{row.mobile}}</span></span>
          </template>
        </el-table-column>
        <!-- 邮箱 -->
         <el-table-column prop="mobile" width="150" :label="$t('merchants.addmerchants.yx')">
          <template slot-scope='{row}'>
            <span>{{row.e_mail}} </span>
          </template>
        </el-table-column>
        <!-- 账户余额 -->
        <el-table-column prop="money" :label="$t('membersLists.zhye')">
          <template slot-scope="scope">
            <span
              >{{laikeCurrencySymbol}} {{
                scope.row.money ? scope.row.money.toFixed(2) : "0.00"
              }}</span
            >
          </template>
        </el-table-column>
        <!-- 积分余额 -->
        <el-table-column prop="score" :label="$t('membersLists.syjf')">
        </el-table-column>
        <!-- 账号来源 -->
        <el-table-column prop="volume" :label="$t('membersLists.zhly')">
          <template slot-scope="scope">
            <span>{{$myGetSource(scope.row.source)}}</span>
          </template>
        </el-table-column>
        <!-- 有效订单数 -->
        <el-table-column prop="z_num" :label="$t('membersLists.yxdds')">
        </el-table-column>
        <!-- 交易金额 -->
        <el-table-column prop="z_price" :label="$t('membersLists.jyje')">
          <template slot-scope="scope">
            <span
              >{{laikeCurrencySymbol}} {{
                scope.row.z_price == null ? 0.0 : scope.row.z_price.toFixed(2)
              }}</span
            >
          </template>
        </el-table-column>
        <!-- 注册时间 -->
        <el-table-column
          prop="Register_data"
          :label="$t('membersLists.zcsj')"
          width="200"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.Register_data | dateFormat }}</span>
          </template>
        </el-table-column>
        <!-- 操作 -->
        <el-table-column
          fixed="right"
          :label="$t('membersLists.cz')"
          width="200"
          class="hhh"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button 
                  icon="el-icon-view"
                  @click="View(scope.row)"
                  >{{ $t("membersLists.ck") }}</el-button
                >
                <el-button 
                  icon="el-icon-edit-outline"
                  @click="Edit(scope.row)"
                  >{{ $t("membersLists.bianji") }}</el-button
                >
              </div>
              <div class="OP-button-bottom">
                <el-button 
                  :disabled="!scope.row.isDelBtn"
                  :class="!scope.row.isDelBtn ? 'cssDisabled' : ''"
                  icon="el-icon-delete"
                  @click="Delete(scope.row)"
                >
                  {{ $t("membersLists.shanchu") }}
                </el-button>
                <div class="more" @click="tags(scope.row)" >
                  {{ $t("membersLists.gd")
                  }}<i class="el-icon-caret-bottom"></i>
                  <ul
                    class="more-block"
                    :class="[tag == scope.row.user_id ? 'active' : '']"
                  >
                    <li class="more-box">
                      <div 
                        @click="dialogShow2(scope.row, 1)"
                      >
                        {{ $t("membersLists.yecz") }}
                      </div>
                    </li>
                    <li class="more-box">
                      <div 
                        @click="dialogShow2(scope.row, 2)"
                      >
                        {{ $t("membersLists.jfcz") }}
                      </div>
                    </li>
                    <li class="more-box">
                      <div 
                        @click="valetOrder(scope.row.user_id)"
                      >
                        {{ $t("membersLists.dkxd") }}
                      </div>
                    </li>
                    <li class="more-box">
                      <div 
                        @click="toOrder(scope.row)"
                      >
                        {{ $t("membersLists.ddck") }}
                      </div>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox">
        <div class="pageLeftText">
          {{ $t("DemoPage.tableExamplePage.show") }}
        </div>
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
          <div class="pageRightText">
            {{ $t("DemoPage.tableExamplePage.on_show") }}{{ currpage }}-{{
              current_num
            }}{{ $t("DemoPage.tableExamplePage.twig") }}{{ total
            }}{{ $t("DemoPage.tableExamplePage.twig_notes") }}
          </div>
        </el-pagination>
      </div>
    </div>

    <div :class="toggle === 1 ? 'dialog-block2' : 'dialog-block'">
      <!-- 弹框组件 -->
      <el-dialog
        :title="
          toggle == 1
            ? $t('membersLists.yecz')
            : toggle == 2
            ? $t('membersLists.jfcz')
            : $t('membersLists.djxg')
        "
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <el-form
          :model="ruleForm"
          :rules="rules"
          ref="ruleForm"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="task-container">
            <el-form-item
              v-if="toggle === 1"
              class="balance"
              :label="$t('membersLists.yeje',{currencySymbol:laikeCurrencySymbol})"
              prop="balance"
            >
              <el-input
                v-model="ruleForm.balance"
                :placeholder="$t('membersLists.qsczje')"
                :maxlength="6"
              ></el-input>
            </el-form-item>
            <el-form-item
              style="margin-top: 20px"
              v-if="toggle === 1"
              class="integral"
              :label="$t('membersLists.bz')"
              prop="remake"
            >
              <el-input
                v-model="ruleForm.remake"
                type="textarea"
                :placeholder="$t('membersLists.qsrbzxx')"
              ></el-input>
              <div class="explain">
                <img src="../../../../assets/imgs/ts3.png" alt="" />
                <span class="red">{{ $t("membersLists.kcqtfh") }}</span>
              </div>
            </el-form-item>
            <el-form-item
              v-if="toggle === 2"
              class="integral"
              :label="$t('membersLists.jfje')"
              prop="integral"
            >
              <el-input
                v-model="ruleForm.integral"
                :placeholder="$t('membersLists.qsczjf')"
                :maxlength="6"
              ></el-input>
              <div class="explain">
                <img src="../../../../assets/imgs/ts3.png" alt="" />
                <span class="red">{{ $t("membersLists.kcqtfh") }}</span>
              </div>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button class="qx_bt" @click="handleClose2()">{{
                $t("membersLists.ccel")
              }}</el-button>
              <el-button
                class="bdColor"
                type="primary"
                @click="submitForm2('ruleForm')"
                >{{ $t("membersLists.okk") }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>

    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('DemoPage.tableExamplePage.export_data')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <div class="item" @click="exportPage">
          <i class="el-icon-document"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_page") }}</span>
        </div>
        <div class="item item-center" @click="exportAll">
          <i class="el-icon-document-copy"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_all") }}</span>
        </div>
        <div class="item" @click="exportQuery">
          <i class="el-icon-document"></i>
          <span>{{ $t("DemoPage.tableExamplePage.export_query") }}</span>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import membersLists from "@/webManage/js/members/membersList/membersLists";
export default membersLists;
</script>

<style scoped lang="less">
@import "../../../../webManage/css/members/membersList/membersList.less";
.green_edge:before {
  content: "\e642";
  font-size: 14px;
  margin-right: 8px;
  position: relative;
  top: 1px;
}
.green_edge2:before {
  content: "\e644";
  font-size: 14px;
  margin-right: 8px;
  position: relative;
  top: 1px;
}
/deep/ .diqu-box{
    display: flex;
    align-items: center;
     .el-autocomplete{
        width:300px !important;
        margin-right: 10px;
        .el-input {
          width:300px !important;
          input{
            width:300px !important
          }
        }
      }
}
/deep/ .el-autocomplete-suggestion {
  width:auto !important
}
</style>
