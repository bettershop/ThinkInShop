<template>
  <div class="container">
    <div class="menu-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="loading"
        :data="tableData"
        ref="table"
        class="el-table"
        style="width: 100%"
        :style="
          tableData.length <= 0 ? `border-bottom: 1px solid #E9ECEF;` : ''
        "
        :height="tableHeight"
        :header-cell-style="{
          background: '#F4F7F9',
          height: '50px',
        }"
      >
        <template slot="empty">
          <div class="empty">
            <img src="../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
          </div>
        </template>
        <el-table-column
          prop=""
          :label="$t('pcBanner.bannerList.xh')"
          width="70"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.sort }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="image"
          :label="$t('pcBanner.bannerList.tp')"
          width="600"
        >
          <template slot-scope="scope">
            <img
              :src="scope.row.image"
              alt=""
              style="width: 212px; height: 50px"
              @error="handleErrorImg"
            />
          </template>
        </el-table-column>
        <el-table-column prop="url" :label="$t('pcBanner.bannerList.lj')">
          <template slot-scope="scope">
            <span>{{ scope.row.url }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="add_date"
          :label="$t('pcBanner.bannerList.tjsj')"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('pcBanner.bannerList.cz')"
          width="300"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <!-- <el-button class="laiketui laiketui-zhiding" @click="placedTop(scope.row)" v-if=" scope.$index !== 0">{{$t('pcBanner.bannerList.zd')}}</el-button> -->
                <!-- @click="Edit(scope.row)" -->
                <el-button
                  icon="el-icon-edit-outline"
                  @click="handleEditBanner(scope.row)"
                  >{{ $t("pcBanner.bannerList.bianji") }}</el-button
                >
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">{{
                  $t("pcBanner.bannerList.shanchu")
                }}</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <!-- <div class="pageBox" ref="pageBox" v-if="showPagebox">
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
        </div> -->
    </div>
    <!-- 新增弹窗 -->
    <div class="banner-add-ande-edit">
      <el-dialog
        :title="$t('pcBanner.bannerList.tjlbt')"
        :visible.sync="addBanner"
        :before-close="addbannerClose"
      >
        <div class="add-banner-container">
          <el-form
            :model="ruleForm"
            label-position="right"
            :rules="rules"
            ref="addruleForm"
            label-width="55px"
          >
            <div class="notice">
              <el-form-item
                :label="$t('pcBanner.bannerList.tp')"
                prop="img"
                class="upload-img"
              >
                <l-upload
                  :limit="1"
                  v-model="ruleForm.img"
                  :text="$t('pcBanner.bannerList.jy1200')"
                  ref="addupload"
                  :mask_layer="false"
                >
                </l-upload>
              </el-form-item>
              <el-form-item :label="$t('pcBanner.bannerList.lj')">
                <el-input
                  v-model="ruleForm.url"
                  :placeholder="$t('pcBanner.bannerList.lujints')"
                ></el-input>
              </el-form-item>

              <el-form-item :label="$t('pcBanner.bannerList.pxh')">
                <el-input
                  v-model="ruleForm.sort"
                  :placeholder="$t('pcBanner.bannerList.qsrpxh')"
                ></el-input>
              </el-form-item>

              <!-- <div class="footer-button"> -->
              <!-- <el-form-item>
              <el-button
                type="primary"
                class="footer-save bgColor"
                @click="submitForm('ruleForm')"
                ></el-button
              >
              <el-button
                plain
                class="footer-cancel fontColor mgleft"
                @click="$router.go(-1)"
                ></el-button
              >
            </el-form-item> -->
            </div>
          </el-form>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="handleClose" class="shaco_color">
            {{ $t("DemoPage.tableFromPage.cancel") }}
          </el-button>
          <el-button type="primary" @click="submitForm('addupload')">
            {{ $t("DemoPage.tableFromPage.save") }}
          </el-button>
        </span>
      </el-dialog>
    </div>
    <div class="banner-add-ande-edit">
      <!-- 编辑弹窗 -->
      <el-dialog
      :title="$t('pcBanner.bannerList.tjlbt')"
        :visible.sync="editBanner"
        :before-close="editBannerClose"
      >
        <div class="add-banner-container">
          <el-form
            :model="ruleForm"
            label-position="right"
            :rules="rules"
            ref="editRuleForm"
            label-width="55px"
          >
            <div class="notice">
              <el-form-item :label="$t('pcBanner.bannerList.tp')" prop="img">
                <l-upload
                  :limit="1"
                  v-model="ruleForm.img"
                  ref="uplaodBanner"
                  :text="$t('pcBanner.bannerList.jy1200')"
                  :mask_layer="false"
                >
                </l-upload>
              </el-form-item>
              <el-form-item :label="$t('pcBanner.bannerList.lj')">
                <el-input
                  v-model="ruleForm.url"
                  :placeholder="$t('pcBanner.bannerList.lujints')"
                ></el-input>
              </el-form-item>

              <el-form-item :label="$t('pcBanner.bannerList.pxh')">
                <el-input
                  v-model="ruleForm.sort"
                  :placeholder="$t('pcBanner.bannerList.qsrpxh')"
                ></el-input>
              </el-form-item>
            </div>
          </el-form>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="editBanner = false" class="shaco_color">
            {{ $t("DemoPage.tableFromPage.cancel") }}
          </el-button>
          <el-button
            type="primary"
            @click="handleEditSubmitForm('editRuleForm')"
          >
            {{ $t("DemoPage.tableFromPage.save") }}
          </el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import bannerList from "@/webManage/js/mall/terminalConfig/bannerListPC";
export default bannerList;
</script>

<style scoped lang="less">
@import "../../../webManage/css/mall/terminalConfig/bannerListPC.less";
</style>
