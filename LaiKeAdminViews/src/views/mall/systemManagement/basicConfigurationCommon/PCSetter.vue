<template>
  <div class="container">
  <!-- Pc商城 -->
  <div  >
      <div class="merchants-list">
        <el-form
          class="demo-ruleForm"
          :rules="browserRules"
          ref="browserForm"
          :model="browserForm"
          :label-width="language == 'en' ? 'auto' : '115px'"
        >
          <div class="card" style="padding-bottom: 22.4px">
            <div class="title">{{ $t(`terminalConfig.llqbq`) }}</div>
            <div class="shaco_flex">
              <div class="shaco_box_one">
                <div>
                  <el-form-item
                    prop="mallIcon"
                    :label="$t(`terminalConfig.tubiao`)"
                    style="margin-left: 30px"
                  >
                    <l-upload
                      :limit="1"
                      :text="`（${$t('terminalConfig.tbscgs')}）`"
                      v-model="browserForm.mallIcon"
                    >
                    </l-upload>
                  </el-form-item>
                </div>
              </div>
            </div>
            <div class="">
              <div class="shaco_box_one">
                <div>
                  <!-- <el-row :gutter="3" style="margin-left: 14px;padding-bottom: 18px;">
                   <el-col :lg="8">

                  </el-col>
                  </el-row> -->
                  <el-row :gutter="3" style="">
                    <el-col :lg="8">
                      <el-form-item
                        :label="$t(`terminalConfig.mingcheng`)"
                        style="margin-left: 30px"
                        prop="mallName"
                      >
                        <el-input
                          :placeholder="$t(`terminalConfig.mcts`)"
                          v-model="browserForm.mallName"
                          style="width: 100%"
                        ></el-input>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>
              </div>
            </div>
          </div>
        </el-form>
      </div>

      <div class="merchants-list">
        <el-form
          class="demo-ruleForm"
          ref="registerForm"
          :model="registerForm"
          :rules="registerRules"
          :label-width="language == 'en' ? 'auto' : '115px'"
        >
          <div class="card">
            <div class="title">{{ $t(`terminalConfig.dlypz`) }}</div>
            <div class="shaco_flex">
              <div class="shaco_box_one">
                <div>
                  <el-form-item
                    prop="mallLogo"
                    :label="$t(`terminalConfig.sclogo`)"
                    style="margin-left: 30px"
                  >
                    <l-upload
                      :limit="1"
                      :text="`（${$t(`terminalConfig.sclogojycc`)}）`"
                      v-model="registerForm.mallLogo"
                      :object_fit="`contain`"
                    >
                    </l-upload>
                  </el-form-item>
                </div>
              </div>
            </div>
            <div class="shaco_flex">
              <div class="shaco_box_one">
                <div>
                  <el-form-item
                    :label="$t(`terminalConfig.kjcd`)"
                    style="margin-left: 30px"
                  >
                    <!-- <el-input :placeholder="`请输入名称`"></el-input> -->
                    <el-checkbox-group v-model="registerForm.shortcutMenu2">
                      <el-checkbox
                        v-for="t in shortcutMenu2List"
                        :label="t.value"
                        :key="t.value"
                        >{{ t.name }}</el-checkbox
                      >
                    </el-checkbox-group>
                  </el-form-item>
                </div>
              </div>
            </div>
            <div class="">
              <div class="">
                <el-row
                  :gutter="3"
                  style="
                    margin-left: 48px;
                    padding-right: 20px;
                    padding-bottom: 0;
                  "
                >
                  <el-col :lg="8">
                    <el-form-item :label="$t(`terminalConfig.bqxxi`)">
                      <el-input
                        v-model="registerForm.archival"
                        :placeholder="$t(`terminalConfig.qsrbqxx`)"
                        style="width: 100%"
                      ></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :lg="8">
                    <el-form-item
                      style="margin-left: 10px"
                      :label="$t(`terminalConfig.baxxi`)"
                    >
                      <el-input
                        v-model="registerForm.copyright"
                        :placeholder="$t(`terminalConfig.qsrbaxxi`)"
                        style="width: 100%"
                      ></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :lg="8" style="padding-left: 10px">
                    <!-- <el-form-item
                      :label="$t(`terminalConfig.gwwz`)"
                      style="margin-left: 10px"
                    >
                      <el-input
                        v-model="registerForm.authority"
                        :placeholder="$t(`terminalConfig.qsrgwwz`)"
                        style="width: 100%"
                      >
                      <template slot="prepend">Http://</template>
                    </el-input>
                    </el-form-item> -->
                  </el-col>
                </el-row>
              </div>
            </div>
            <div>
              <div>
                <el-row
                  :gutter="3"
                  style="
                    margin-left: 48px;
                    padding-right: 20px;
                    padding-bottom: 0;
                  "
                >
                  <el-col :lg="8">
                    <el-form-item
                      :label="$t(`terminalConfig.gwwz`)"
                    >
                      <el-input
                        v-model="registerForm.authority"
                        :placeholder="$t(`terminalConfig.qsrgwwz`)"
                        style="width: 100%"
                      >
                        <!-- <template slot="prepend">Http://</template> -->
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :lg="8" style="display: flex;align-items: center;height: 28px;">
                    <span style="margin-left: 10px;color: #97a0b4;">{{$t('systemConfig.qsrktwx')}}</span>
                  </el-col>
                  <el-col :lg="8"> </el-col>
                </el-row>
              </div>
            </div>
            <!-- <div class="">
              <div class="shaco_box_one">
                <el-form-item
                  :label="$t(`terminalConfig.dblink`)"
                  style="margin-left: 30px"
                >
                  <div style="width: 37%">
                    <div
                      class="link-content"
                      style="margin-bottom: 22px"
                      v-for="(t, i) in registerForm.list"
                    >
                      <el-input style="width: 17%" v-model="t.name"></el-input>
                      <el-input
                        class="input-two"
                        v-model="t.link"
                        style="width: 58%"
                      ></el-input>
                      <div style="width: 18%; display: inline-block">
                        <i
                          class="el-icon-circle-plus-outline icon-two"
                          style="color: #2890ff; font-size: 20px"
                          @click="handleAddButtonLink(t, i)"
                        ></i>
                        <i
                          class="el-icon-remove-outline"
                          style="font-size: 20px"
                          v-if="i != 0"
                          @click="handleDelButtonLink(t, i)"
                        ></i>
                      </div>
                    </div>
                  </div>
                </el-form-item>
              </div>
            </div> -->

            <div style="padding-bottom: 18px">
              <div>
                <el-row
                  :gutter="3"
                  v-for="(t, i) in registerForm.list"
                  style="
                    margin-left: 48px;
                    padding-right: 20px;
                    padding-bottom: 0;
                  "
                  :key="i"
                >
                  <el-col :lg="8">
                    <el-form-item
                      :label="i == 0 ? $t(`terminalConfig.dblink`) : ''"
                    >
                      <el-input style="width: 28%" v-model="t.name"></el-input>
                      <el-input
                        v-model="t.link"
                        style="width: 69%; margin-left: 3%"
                      >
                        <!-- <template slot="prepend">Http://</template> -->
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :lg="8" style="padding-top: 10px; padding-left: 10px;display: flex;align-items: center;">
                    <i
                      class="el-icon-circle-plus-outline icon-two"
                      style="color: #2890ff; font-size: 20px"
                      @click="handleAddButtonLink(t, i)"
                    ></i>
                    <i
                      class="el-icon-remove-outline"
                      style="font-size: 20px; margin-left: 10px"
                      v-if="i != 0"
                      @click="handleDelButtonLink(t, i)"
                    ></i>
                    <span v-if="i == 0" style="margin-left: 10px;color: #97a0b4;">{{$t('systemConfig.qsrktwx')}}</span>
                  </el-col>
                  <el-col :lg="8"> </el-col>
                </el-row>
              </div>
            </div>
          </div>
        </el-form>
      </div>

      <div class="merchants-list">
        <el-form
          class="demo-ruleForm"
          ref="homePageForm"
          :model="homePageForm"
          :rules="homePageRules"
          :label-width="language == 'en' ? 'auto' : 'auto'"
        >
          <div class="card" style="padding-bottom: 40px">
            <div class="title">{{ $t(`terminalConfig.sypz`) }}</div>
            <div class="">
              <div class="shaco_box_one">
                <div>
                  <el-row :gutter="3">
                    <el-col :lg="8">
                      <el-form-item
                        :label="$t(`terminalConfig.hysy`)"
                        prop="welcomeTerm"
                        style="margin-left: 30px"
                      >
                        <el-input
                          :placeholder="$t(`terminalConfig.qsrmc`)"
                          v-model="homePageForm.welcomeTerm"
                          style="width: 100%"
                        ></el-input>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>
              </div>
            </div>
            <div class="shaco_flex">
              <div class="shaco_box_one">
                <div>
                  <el-form-item
                    :label="$t(`terminalConfig.sypzkjcd`)"
                    style="margin-left: 30px"
                  >
                    <!-- <el-input :placeholder="`请输入名称`"></el-input> -->
                    <el-checkbox-group v-model="homePageForm.shortcutMenu3">
                      <el-checkbox
                        v-for="t in shortcutMenu3List"
                        :label="t.value"
                        :key="t.value"
                        >{{ t.name }}</el-checkbox
                      >
                    </el-checkbox-group>
                  </el-form-item>
                </div>
              </div>
            </div>
            <div class="shaco_flex">
              <div class="shaco_box_one">
                <div>
                  <el-form-item
                    :label="$t(`terminalConfig.sypzfcsz`)"
                    style="margin-left: 30px"
                  >
                    <div class="set_box">
                      <el-form-item
                        :label="$t(`terminalConfig.syxzewm`)"
                        required
                      >
                        <l-upload
                          :limit="1"
                          :text="$t(`terminalConfig.ermjycc`)"
                          v-model="homePageForm.APPUrl"
                          :object_fit="`contain`"
                        >
                        </l-upload>
                      </el-form-item>
                      <el-form-item
                        :label="$t(`terminalConfig.xzappsm`)"
                        required
                      >
                        <el-input
                          v-model="homePageForm.APPExplain"
                          :placeholder="$t(`terminalConfig.apptshy`)"
                          style="width: 100%"
                        ></el-input>
                      </el-form-item>
                    </div>
                  </el-form-item>
                </div>
              </div>
            </div>
            <div class="">
              <div class="" style="width: ; padding: 0 20px">
                <div>
                  <el-form-item
                    :label="$t(`terminalConfig.sydblpz`)"
                    style="margin-left: 30px; margin-bottom: 0"
                  >
                    <div style="margin-bottom: 16px">
                      <el-button class="shaco_btn" @click="handleAddConfig">{{
                        $t(`terminalConfig.sytjpz`)
                      }}</el-button>
                      <span
                        style="
                          color: #97a0b4;
                          font-size: 14px;
                          margin-left: 10px;
                        "
                        >{{ $t(`terminalConfig.syzdtjsx`) }}</span
                      >
                    </div>
                  </el-form-item>
                  <div style="padding: 0 0px 0 146px">
                    <el-table
                      :data="tableData"
                      style="width: 100%"
                      :style="
                        tableData.length <= 0
                          ? `border-bottom: 1px solid #E9ECEF;`
                          : ''
                      "
                      :header-cell-style="{
                        background: '#F4F7F9',
                        height: '50px',
                      }"
                    >
                      <template slot="empty">
                        <div class="empty">
                          <img src="../../../../assets/imgs/empty.png" alt="" />
                          <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
                        </div>
                      </template>
                      <el-table-column
                        prop="store_id"
                        :label="$t(`terminalConfig.sypzxh`)"
                      >
                        <template slot-scope="scope">
                          {{ scope.$index + 1 }}
                        </template>
                      </el-table-column>
                      <el-table-column
                        prop="image"
                        :label="$t(`terminalConfig.sypztb`)"
                      >
                        <template slot-scope="scope">
                          <img
                            :src="scope.row.image"
                            style="width: 50px; height: 50px"
                            @error="handleErrorImg"
                          />
                        </template>
                      </el-table-column>
                      <el-table-column
                        prop="title"
                        :label="$t(`terminalConfig.sypzbt`)"
                      >
                      </el-table-column>
                      <el-table-column
                        prop="subheading"
                        :label="$t(`terminalConfig.sypzfbt`)"
                      >
                      </el-table-column>
                      <el-table-column
                        prop="sort"
                        :label="$t(`terminalConfig.sypzpxu`)"
                      >
                      </el-table-column>
                      <el-table-column
                        prop="add_date"
                        :label="$t(`terminalConfig.tjsj`)"
                        width="200"
                      >
                      </el-table-column>
                      <el-table-column
                        prop="address2"
                        :label="$t(`terminalConfig.cz`)"
                        fixed="right"
                        width="300"
                      >
                        <template slot-scope="scope">
                          <div class="OP-button">
                            <div class="OP-button-top">
                              <el-button
                                icon="el-icon-edit-outline"
                                @click="handleEditConfig(scope.row)"
                                >{{
                                  $t("pcBanner.bannerList.bianji")
                                }}</el-button
                              >
                              <el-button
                                icon="el-icon-delete"
                                @click="handleDelConfig(scope.row)"
                                >{{
                                  $t("pcBanner.bannerList.shanchu")
                                }}</el-button
                              >
                            </div>
                          </div>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-form>
      </div>

      <div class="merchants-list" style="padding-bottom: 4.8vw">
        <el-form
          class="demo-ruleForm"
          :label-width="language == 'en' ? 'auto' : '115px'"
        >
          <div class="card" style="padding: 0 0 40px 0">
            <div class="title">
              {{ $t("pcBanner.bannerList.lbt") }}
              <el-button class="shaco_btn" @click="handleAddBanner()">
                {{ $t("pcBanner.bannerList.tjlbt") }}
              </el-button>
            </div>
            <div style="padding: 0 20px 0px 20px">
              <!-- 轮播图列表 -->
              <BannerListPC ref="bannerList" />
            </div>
          </div>
        </el-form>
      </div>

      <!-- 楼层管理 -->
      <div class="merchants-list"  >

    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.name"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('floorList.qsrlcmc')"
          ></el-input>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t('DemoPage.tableExamplePage.reset')
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{
            $t('DemoPage.tableExamplePage.demand')
          }}</el-button>

        </div>
      </div>
    </div>
    <div class="jump-list">
      <el-button
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="dialogShow2()"
      >
        {{ $t("floorList.xzlc") }}</el-button
      >
    </div>
    <div class="merchants-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="loading"
        :data="blockTable"
        ref="table"
        class="el-table"
        style="width: 100%"
        :height="tableHeight"
      >
      <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column prop="id" :label="$t('floorList.lcid')"> </el-table-column>
        <el-table-column prop="block_name" :label="$t('floorList.lcmc')"> </el-table-column>
        <el-table-column :label="$t('floorList.sfkq')">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.enable_or_not"
              @change="switchs(scope.row)"
              :active-value="1"
              :inactive-value="2"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('floorList.tjsj')">
          <template slot-scope="scope">
            <span>{{ dateFormat('YYY-mm-dd HH:MM', scope.row.add_date) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" :label="$t('floorList.px')">
          <template slot-scope="scope">
            <el-input
              style="width: 100px;text-align: center;"
              :controls="false"
              v-model="scope.row.sort"
              @change="changesort(scope.row)"
              @input="scope.row.sort=scope.row.sort.replace(/^(0+)|[^\d]+/g,'')"
              :min="1"
            ></el-input>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('floorList.cz')" width="300">
          <template slot-scope="scope">
            <div class="OP-button">
              <!-- icon="el-icon-good" -->
              <el-button
                class="goodList"
                icon="el-icon-notebook-2"
                @click="godetail(scope.row)"
              >
                {{ $t('floorList.splb') }}</el-button
              >
              <el-button icon="el-icon-edit-outline" @click="edit(scope.row)">
                {{ $t('floorList.bj') }}</el-button
              >
              <el-button icon="el-icon-delete" @click="Delete(scope.row)">
                {{ $t('floorList.sc') }}</el-button
              >
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox">
        <div class="pageLeftText">{{ $t("DemoPage.tableExamplePage.show") }}</div>
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

    <div class="footerBox">
            <!-- <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">取消</el-button> -->
            <el-button
              type="primary"
              class="footer-save bgColor mgleft"
              @click="handleSaveConfiguration('ruleIndexForm')"
              >{{ $t("DemoPage.tableFromPage.save") }}</el-button
            >
      </div>

  </div>
 <div style="height: 50px;"></div>
    <!-- 添加配置弹窗 -->
    <div class="dialog-block-add-peizhi">
      <el-dialog
        :title="configTitle"
        :visible.sync="isConfig"
        :before-close="handleCloseConfig"
        key="dialog-block-add-peizhi"
      >
        <div class="add-config-container">
          <el-form
            :model="configurationForm"
            label-position="right"
            :rules="confiRules"
            ref="configurationForm"
            :label-width="`58px`"
            class="form-search"
          >
            <div class="notice">
              <el-form-item :label="$t('terminalConfig.sypztb')" prop="images">
                <l-upload
                  :limit="1"
                  ref="lupload"
                  v-model="configurationForm.images"
                  :text="$t('terminalConfig.sypztbts')"
                  :mask_layer="false"
                >
                </l-upload>
              </el-form-item>
              <el-form-item :label="$t('terminalConfig.sypzbt')" prop="title">
                <el-input
                  v-model="configurationForm.title"
                  :placeholder="$t('terminalConfig.sypzbtts')"
                ></el-input>
              </el-form-item>
              <el-form-item :label="$t('terminalConfig.sypzfbt')">
                <el-input
                  v-model="configurationForm.subheading"
                  :placeholder="$t('terminalConfig.sypzfbtts')"
                ></el-input>
              </el-form-item>

              <el-form-item :label="$t('terminalConfig.sypzpxu')">
                <el-input
                  v-model="configurationForm.sort"
                  :placeholder="$t('terminalConfig.sypzpxuts')"
                ></el-input>
              </el-form-item>
            </div>
          </el-form>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="handleCloseConfig">
            {{ $t("DemoPage.tableFromPage.cancel") }}
          </el-button>
          <el-button type="primary" @click="handleConfigSubmit">
            {{ $t("DemoPage.tableFromPage.save") }}
          </el-button>
        </span>
      </el-dialog>
    </div>
    <!-- 编辑配置弹窗 -->
    <!-- 添加楼层 -->

    <div class="dialog-block  ">
      <el-dialog
        :title="dialog_title"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <el-form class="demo-ruleForm" label-width="auto">
            <el-form-item :required="true" :label="$t('floorList.lcmc')" prop="">
              <el-input
                maxlength="20"
                v-model="ruleForm2.name"
                :placeholder="$t('floorList.qsrlcmc')"
              ></el-input>
            </el-form-item>
            <el-form-item :label="$t('floorList.pxh')" :required="true">
              <el-input
                v-model="ruleForm2.sort"
                v-on:input="ruleForm2.sort=ruleForm2.sort.replace(/^(0+)|[^\d]+/g,'')"
                :placeholder="$t('floorList.qsrpxh')"
              ></el-input>
            </el-form-item>
            <div class="form-footers">
              <el-form-item>
              <el-button class="bdColor" @click="handleClose2" plain>{{
                $t('DemoPage.tableFromPage.cancel')
              }}</el-button>
              <el-button type="primary" @click="submitForm()">
                {{
                $t('zdata.ok')
              }}
              </el-button>
            </el-form-item>
            </div>
        </el-form>
      </el-dialog>
    </div>

    </div>
  </div>
</template>

<script>
import main from "@/webManage/js/mall/terminalConfig/terminalList";
import { getFloorInfo, addFloor, delFloor ,openFloor} from '@/api/goods/floor'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import request from '@/api/https'
import { index, save, getConfig, addConfig } from "@/api/mall/terminalConfig";

export default {
  name: 'floorList',
  mixins: [mixinstest,main],
  data () {
    return {
      blockTable: [],
      loading: true,
      inputInfo: {
        name: ''
      },
      button_list: [],
      // 弹框数据
      dialogVisible: false,
      dialogVisible2: false,
      tableHeight: null,
      ruleForm2: {
        name: '',
        sort: '',
        id: ''
      },
      editid: '',
      dialog_title:this.$t('floorList.xzlc'),
      registerRules: {
        mallLogo: [
          {
            required: true,
            message: this.$t("terminalConfig.qsctp"),
            trigger: "change",
          },
        ],

      },
      browserRules: {
        mallIcon: [
          {
            required: true,
            message: this.$t("terminalConfig.qsctp"),
            trigger: "change",
          },
        ],
        mallName: [{ required: true, message: this.$t("terminalConfig.qingshurumc"), trigger: "blur" }],
      },
    }
  },

  created () {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.getFloorInfos()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
      // 保存PC商城配置
      async handleSaveConfiguration() {
      const isEmpty = await this.handleVerify();
      let registerForm1 = JSON.parse(JSON.stringify(this.registerForm));
      registerForm1.shortcutMenu2 = registerForm1.shortcutMenu2.toString();

      let homePageForm1 = JSON.parse(JSON.stringify(this.homePageForm));
      homePageForm1.shortcutMenu3 = homePageForm1.shortcutMenu3.toString();
      // ...homePageForm1  去除商品分享设置
      let obj = { ...this.browserForm, ...registerForm1, ...homePageForm1 };
      console.log("XXXXXXX234234", obj, homePageForm1.list);
      if (isEmpty) {
        let { entries } = Object;
        let data2 = {
          api: "admin.pc.addConfig",
          ...obj,
        };
        let formData = new FormData();
        for (let [key, value] of entries(data2)) {
          if (key == "list") {
            formData.append(key, JSON.stringify(value));
          } else {
            formData.append(key, value);
          }
        }
        const res = await addConfig(formData);
        if (res.data.code == 200) {
          this.$message({
            type: "success",
            message: this.$t('zdata.baccg'),
            offset: 102
          });
          this.handleGetConfig();
        }
        console.log("是否保存了苏剧层高", res);
      }

      console.log("xxxxxxxx228", isEmpty, obj);
    },
    switchs(row) {
      openFloor({
        api: "admin.block.EnableOrNot",
        id: row.id,
      }).then((res) => {
        if (res.data.code == "200") {
          this.$message({
            message: this.$t('zdata.czcg'),
            type: "success",
            offset: 102,
          });
        }
        this.getFloorInfos();
      });
    },
    changesort (value) {
      getFloorInfo({
        api: 'admin.block.add',
        id: value.id,
        sort: value.sort
      }).then(res => {
        console.log(res)
        if (res.data.code == 200) {
          this.getFloorInfos()
          this.$message({
            type: 'success',
            message: this.$t('zdata.czcg'),
            offset: 102
          })
        }
      })
    },
    getHeight () {
      // this.tableHeight =
      //   this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      // console.log(this.$refs.tableFather.clientHeight)
    },
    dateFormat (fmt, date) {
      let ret = ''
      date = new Date(date)
      const opt = {
        'Y+': date.getFullYear().toString(), // 年
        'm+': (date.getMonth() + 1).toString(), // 月
        'd+': date.getDate().toString(), // 日
        'H+': date.getHours().toString(), // 时
        'M+': date.getMinutes().toString(), // 分
        'S+': date.getSeconds().toString() // 秒
        // 有其他格式化字符需求可以继续添加，必须转化成字符串
      }
      for (let k in opt) {
        ret = new RegExp('(' + k + ')').exec(fmt)
        if (ret) {
          fmt = fmt.replace(
            ret[1],
            ret[1].length == 1 ? opt[k] : opt[k].padStart(ret[1].length, '0')
          )
        }
      }
      return fmt
    },
    // 获取楼层信息
    async getFloorInfos () {
      const res = await getFloorInfo({
        api: 'admin.block.list',
        storeType: 8,
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        name: this.inputInfo.name
      })
      console.log(res)
      this.total = res.data.data.total
      this.blockTable = res.data.data.list
      this.loading = false
      if (res.data.data.total < 10) {
        this.current_num = this.total
      }
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },
    submitForm () {
      if (this.ruleForm2.name == '') {
        this.warnMsg(this.$t('floorList.qsrlcmc'))
        return
      }
      if (this.ruleForm2.sort == '') {
        this.warnMsg(this.$t('floorList.qsrpxh'))
        return
      }
      let { entries } = Object

      let data = {
        api: 'admin.block.add',
        name: this.ruleForm2.name,
        sort: this.ruleForm2.sort,
        id: this.editid
      }

      let formData = new FormData()
      for (let [key, value] of entries(data)) {
        formData.append(key, value)
      }

      addFloor(formData).then(res => {
        if (res.data.code == '200') {
          console.log(res)
          if(this.editid == ''){
            this.$message({
                type: "success",
                message: this.$t("zdata.tjcg"),
                offset: 102,
              });
          }else{
            this.$message({
                type: "success",
                message: this.$t("zdata.bjcg"),
                offset: 102,
              });
          }
          this.dialogVisible2 = false
          this.ruleForm2.name = ''
          this.ruleForm2.sort = ''
          this.ruleForm2.id = ''
          this.getFloorInfos()
        }
      })
    },
    // 重置
    reset () {
      this.inputInfo.name = ''
    },

    // 查询
    demand () {
      // this.currpage = 1
      // this.current_num = 10

      this.loading = true
      this.dictionaryNum = 1
      this.getFloorInfos().then(() => {
        this.loading = false
      })
    },

    getClass (value) {
      return value.join(',')
    },

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.getFloorInfos().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.blockTable.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange (e) {
      this.loading = true
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.getFloorInfos().then(() => {
        this.current_num =
          this.blockTable.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },

    godetail (value) {
      this.$router.push({
        path: '/goods/floor/floorDetail',
        params: value,
        query: {
          id: value.id
        }
      })
    },

    edit (value) {
      request({
        method: 'post',
        params: {
          api: 'admin.block.selectOne',
          id: value.id
        }
      }).then(res => {
        this.editid = value.id
        this.ruleForm2.name = res.data.data.name
        this.ruleForm2.id = res.data.data.id
        this.ruleForm2.sort = res.data.data.sort
        this.dialog_title = this.$t("floorList.bjlc")
        this.dialogVisible2 = true
      })
    },

    // 删除
    Delete (value) {
      console.log(value)
      this.$confirm(this.$t("floorList.qdscm"), this.$t("floorList.ts"), {
        confirmButtonText: this.$t("floorList.qr"),
        cancelButtonText: this.$t("floorList.qx"),
        type: 'warning'
      })
        .then(() => {
          delFloor({
            api: 'admin.block.deleteBlock',
            id: value.id
          }).then(res => {
            if (res.data.code == '200') {
              console.log(res)
              let totalPage = Math.ceil((Number(this.total) - 1) / Number(this.pageSize))
              let dictionaryNum =this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum
              this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum
              this.getFloorInfos()
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
                offset: 102,
              });
            }
          })
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除'
          // })
        })
    },
    addOne () {
      this.labellist.push('')
    },

    minus (index) {
      if (this.labellist.length !== 0) {
        this.labellist.splice(index, 1)
      }
    },
    // 弹框方法
    dialogShow () {
      this.dialogVisible = true
    },
    dialogShow2 () {
      this.dialogVisible2 = true
      this.dialog_title = this.$t("floorList.xzlc")
      this.editid = ''
    },
    handleClose (done) {
      this.dialogVisible = false
    },
    handleClose2 (done) {
      this.dialogVisible2 = false
      this.ruleForm2.name = ''
      this.ruleForm2.sort = ''
      this.ruleForm2.id = ''
      this.editid = ''
    },
  }
}
</script>

<style scoped lang="less">
@import "../../../../webManage/css/mall/terminalConfig/terminalList.less";
/deep/.el-form-item {
        .prompt {
            margin-left: 14px;
            color: #97a0b4;
        }
    }
/deep/.el-icon-good {
  background: url('../../../../assets/imgs/good_list.png') center center
    no-repeat;
  background-size: 13px;
}

/deep/.el-icon-good:before {
  content: '替';
  font-size: 12px;
  visibility: hidden;
}
/deep/.goodList:hover {
  .el-icon-good {
    background: url('../../../../assets/imgs/good_listb.png') center center
      no-repeat;
    background-size: 13px;
  }
}


.footerBox {
    position: fixed;
    right: 0;
    bottom: 40px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 15px 20px;
    border-top: 1px solid #E9ECEF;
    background: #FFFFFF;
    width: 300%;
    z-index: 10;
    button {
      width: 70px;
      height: 40px;
    }
  }
.container {
  display: flex;
  flex-direction: column;
  /deep/.el-tabs__header {
    margin: 0 0 16px 0;
  }
  /deep/.el-tabs__nav-wrap::after {
    display: none;
  }
  /deep/.el-tabs__active-bar {
    display: none;
  }
  /deep/.el-tabs__nav {
    border: 1px solid #d5dbe8;
    border-radius: 4px;
    overflow: hidden;
  }
  /deep/.el-tabs__item {
    min-width: 112px;
    height: 42px;
    line-height: 42px;
    font-size: 16px;
    color: #6a7076;
    background: #ffffff;
    border-right: 1px solid #d5dbe8;
    padding: 0 20px;
  }
  /deep/.el-tabs__item.is-active {
    background: #2890ff;
    color: #ffffff;
  }
  /deep/.el-tabs__item:last-child {
    border-right: 0;
  }
  /deep/.Search {
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
        margin-right: 10px;
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

      .btn-list {
        .bgColor {
          background-color: #2890ff;
        }
        .bgColor:hover {
          background-color: #70aff3;
        }
        .fontColor {
          color: #6a7076;
          background-color: #fff;
          border: 1px solid #d5dbe8;
        }
        .fontColor:hover {
          background-color: #fff;
          color: #2890ff;
          border: 1px solid #2890ff;
        }
        .export {
          position: absolute;
          right: 30px;
        }
      }
    }
  }

  /deep/.jump-list {
    width: 100%;
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
    button {
      min-width: 120px;
      height: 40px;
      background: #28b6ff;
      border-radius: 4px;
      // padding: 0;
      border: none;
      span {
        font-size: 14px;
      }
    }
  }

  .merchants-list {
    flex: 1;
    // height: 605px;
    background: #ffffff !important;
    border-radius: 4px;
    /deep/.el-table__header {
      thead {
        tr {
          th {
            height: 61px;
            text-align: center;
            font-size: 14px;
            font-weight: bold;
            color: #414658;
          }
        }
      }
    }
    /deep/.el-table__body {
      tbody {
        tr {
          td {
            height: 92px;
            text-align: center;
            font-size: 14px;
            color: #414658;
            font-weight: 400;
          }
        }
      }
      .cell {
        img {
          width: 60px;
          height: 60px;
        }
        .el-input__inner{
          text-align: center;
        }
      }
    }

    /deep/.el-table {
      /deep/.OP-button {
        display: flex;
        /deep/button {
          min-width:82px !important;
          min-height: 82px !important;
          margin-left: 8px;
        }
        .OP-buttonBox {
          button {
            line-height: 1rem;
          }
        }
        .OP-button-top {
          margin-bottom: 8px;
          .laiketui-zhiding:before {
            margin-right: 5px;
          }
        }
      }
    }
  }
}

.dialog-block {
    // 弹框样式
    /deep/.el-dialog {
      width: auto;
      min-width: 580px;
      height: 314px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      .el-dialog__header {
        width: 100%;
        height: 58px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 1px solid #e9ecef;
        box-sizing: border-box;
        margin: 0;
        padding: 0 0 0 19px;
        .el-dialog__headerbtn {
          font-size: 18px;
          top: 0 !important;
        }
        .el-dialog__title {
          font-weight: normal;
        }
      }

      .el-dialog__body {
        padding: 41px 60px 21px 60px !important;
        .el-form-item__label {
          font-weight: normal;
        }
        .demo-ruleForm {
          .el-form-item {
            display: flex;
          }
          .el-form-item__content {
            margin-left: 0 !important;
          }
        }
        .form-footers {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          border-top: 1px solid #e9ecef;
          .el-form-item {
            text-align: right;
            margin-right: 10px;
            padding: 0 !important;
            height: 100%;
            display: block !important;
            .el-form-item__content {
              height: 100%;
              line-height: 72px;
              margin: 0 !important;
            }
          }
          .bgColor:hover {
            background-color: #fff;
            color: #2890ff;
            border: 1px solid #2890ff;
          }
        }
        .el-input {
          width: 380px;
        }
      }
    }
  }
.attribute-values {
  .el-form-item__content {
    display: flex;
    flex-direction: column;
    .add-info {
      display: flex;
      &:not(:last-child) {
        margin-bottom: 20px;
      }
      .el-input {
        width: 100%;
        height: 40px;
        input {
          width: 100%;
          height: 40px;
        }
      }

      .add-reduction {
        display: flex;
        margin: 8px 0;
        i {
          font-size: 20px;
          margin-left: 10px;
        }
        .el-icon-circle-plus-outline {
          color: #2890ff;
        }
      }
    }
  }
}
  .form-footers{
        width: 100% !important;
        display: flex;
        justify-content: flex-end;
        align-items: center;
        border-top: 0.0625rem solid #E9ECEF;
        padding: 20px;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        .el-form-item{
          padding-right: 20px;
        }
      }

/deep/.el-dialog__body{
  padding: 0px !important;
}
.pass-input{
  padding: 20px !important;
}
</style>
