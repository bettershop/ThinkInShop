<template>
  <div class="container">
    <!-- <el-main> -->
    <!-- tbl页 -->
    <div class="btn-nav">
      <el-radio-group
        v-model="tabPosition"
        aria-checked="true"
        @change="tbl"
        ref="tab_bun"
      >
        <el-radio-button
          label="1"
          v-if="handleTabLimits(routerList, 'WeChat', 1)"
          >{{ $t("terminalConfig.wxxcx") }}</el-radio-button
        >
        <el-radio-button label="2" v-if="handleTabLimits(routerList, 'App', 1)"
          >APP</el-radio-button
        >
        <el-radio-button
          label="3"
          v-if="handleTabLimits(routerList, 'PCMall', 1)"
          >{{ $t(`terminalConfig.pcsc`) }}</el-radio-button
        >
      </el-radio-group>
    </div>
    <!-- <div class="hr"></div> -->
    <!-- App配置 -->
    <div v-if="tabPosition === '2'">
      <el-form
        :model="appForm"
        :rules="rules"
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        ref="ruleForm"
        label-width="115px"
        class="demo-ruleForm"
      >
        <div class="card" v-if="false">
          <div class="title">{{ $t("terminalConfig.bbpz") }}</div>
          <div class="shaco_flex">
            <div class="shaco_box_one">
              <div>
                <el-form-item
                  :label="$t('terminalConfig.appmc')"
                  prop="appname"
                >
                  <el-input
                    v-model="appForm.appname"
                    :placeholder="$t('terminalConfig.qsrappmc')"
                  ></el-input>
                </el-form-item>
              </div>
              <div>
                <el-form-item
                  :label="$t('terminalConfig.iosxz')"
                  prop="ios_url"
                >
                  <el-input
                    v-model="appForm.ios_url"
                    :placeholder="$t('terminalConfig.qsriosxz')"
                  ></el-input>
                </el-form-item>
              </div>
            </div>
            <div class="shaco_box_two">
              <div>
                <el-form-item :label="$t('terminalConfig.bbh')" prop="editions">
                  <el-input
                    v-model="appForm.editions"
                    :placeholder="$t('terminalConfig.qsrbbh')"
                  >
                  </el-input>
                </el-form-item>
              </div>
              <div style="position: relative">
                <el-form-item
                  :label="$t('terminalConfig.appym')"
                  prop="edition"
                >
                  <el-input
                    v-model="appForm.edition"
                    :placeholder="$t('terminalConfig.qsrappym')"
                  >
                  </el-input>
                  <span class="H5_font">{{ $t("terminalConfig.H5pz") }}</span>
                </el-form-item>
              </div>
            </div>
            <div class="shaco_box_two">
              <div>
                <el-form-item
                  :label="$t('terminalConfig.azxz')"
                  prop="android_url"
                  style="margin-left: 30px"
                >
                  <el-input
                    v-model="appForm.android_url"
                    :placeholder="$t('terminalConfig.qsrazxz')"
                  ></el-input>
                </el-form-item>
              </div>
            </div>
          </div>
          <div class="shaco_box_three">
            <el-form-item :label="$t('terminalConfig.zdgxts')" prop="type">
              <!-- <el-switch
                      v-model="appForm.type"
                      active-color="#00ce6d"
                      inactive-color="#d5dbe8"
                      active-value="1"
                      inactive-value="0"
                      @change="change_type">
                  </el-switch> -->
              <el-switch
                v-model="updataType"
                @change="change_type"
                :active-value="0"
                :inactive-value="1"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              >
              </el-switch>
            </el-form-item>
          </div>
          <div class="shaco_box_three">
            <el-form-item
              :label="$t('terminalConfig.xxnr')"
              prop="content"
              style="margin-bottom: 40px"
            >
              <div class="context-box">
                <vue-editor
                  v-model="appForm.content"
                  @image-added="handleImageAdded"
                ></vue-editor>
              </div>
            </el-form-item>
          </div>
          <!-- <el-row :gutter="3" style="margin-left:30px;">
              <el-col :lg="7">
                <el-form-item :label="APP名称" prop="appname">
                  <el-input v-model="appForm.appname" :placeholder="请输入App名称"></el-input>
                </el-form-item>
              </el-col>
              <el-col :lg="7">
                <el-form-item :label="版本号" prop="editions">
                  <el-input v-model="appForm.editions" :placeholder="请输入版本号"></el-input>
                </el-form-item>
              </el-col>
              <el-col :lg="7">
                <el-form-item :label="安卓下载地址" prop="android_url" style="margin-left:30px;">
                  <el-input v-model="appForm.android_url" :placeholder="请输入安卓下载地址"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="3" style="margin-left:30px;">
              <el-col :lg="7">
                <el-form-item :label="IOS下载地址" prop="ios_url">
                  <el-input v-model="appForm.ios_url" :placeholder="请输入IOS下载地址"></el-input>
                </el-form-item>
              </el-col>
              <el-col :lg="7">
                <el-form-item :label="APP域名" prop="edition">
                  <el-input v-model="appForm.edition" :placeholder="请输入APP域名"></el-input>
                </el-form-item>
              </el-col>
              <span style="margin-left: 13px;line-height:27px;color: #97A0B4;font-size:14px">H5的配置</span>
            </el-row> -->
          <!-- <el-row :gutter="1" style="margin-left:30px;">
              <el-col :lg="7">
                <el-form-item :label="自动更新提示" prop="type">
                  <el-switch
                      v-model="appForm.type"
                      active-color="#00ce6d"
                      inactive-color="#d5dbe8"
                      active-value="1"
                      inactive-value="0"
                      @change="change_type">
                  </el-switch>
                  <el-switch v-model="updataType" @change="change_type" :active-value="0" :inactive-value="1" active-color="#00ce6d" inactive-color="#d4dbe8">
                  </el-switch>
                </el-form-item>
              </el-col>
            </el-row> -->
          <!-- <el-row :gutter="1" style="margin-left:30px;">
              <el-col :lg="21">
                <el-form-item :label="详细内容" prop="content" style="margin-bottom:30px;">
                  <div class="context-box">
                    <vue-editor v-model="appForm.content" @image-added="handleImageAdded"></vue-editor>
                  </div>
                </el-form-item>
              </el-col>
            </el-row> -->
        </div>
      </el-form>
    </div>
    <!-- 微信小程序配置 -->
    <div v-if="tabPosition === '1'">
      <div class="merchants-list">
        <el-form
          :model="weiXinForm"
          :rules="wxRules"
          ref="ruleIndexForm"
          class="demo-ruleForm"
          :label-width="language == 'en' ? 'auto' : '115px'"
        >
          <div class="card" style="padding-bottom: 20px; margin-bottom: 16px">
            <div class="title">{{ $t("terminalConfig.ympz") }}</div>
            <div class="shaco_flex">
              <div class="shaco_box_one">
                <div>
                  <el-form-item
                    prop="appTitle"
                    :label="$t('terminalConfig.sybt')"
                    style="margin-left: 30px"
                  >
                    <el-input
                      v-model="weiXinForm.appTitle"
                      :placeholder="$t('terminalConfig.qsrsybt')"
                      @input="changeValue"
                      style="width: 195%"
                    ></el-input>
                  </el-form-item>
                </div>
              </div>
            </div>
            <div class="shaco_flex">
              <div class="shaco_box_one">
                <div>
                  <el-form-item
                    prop="appLogo"
                    :label="$t('terminalConfig.sqdlicon')"
                    style="margin-left: 30px"
                  >
                    <l-upload
                      :limit="1"
                      :text="`（${$t('terminalConfig.sqdljycc')}）`"
                      v-model="weiXinForm.appLogo"
                    >
                    </l-upload>
                  </el-form-item>
                </div>
              </div>
            </div>
          </div>
        </el-form>
      </div>

      <div class="merchants-list api-config">
        <el-form
          :model="weiXinForm"
          :rules="wxRules"
          :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
          ref="ruleForm"
          :label-width="language == 'en' ? 'auto' : '115px'"
          class="demo-ruleForm"
        >
          <div class="card">
            <div class="title">{{ $t("terminalConfig.jkpz") }}</div>
            <el-row :gutter="3" style="margin-left: 14px; padding-bottom: 18px">
              <el-col :lg="8">
                <el-form-item
                  :label="$t('terminalConfig.xcxid')"
                  prop="appId"
                  style="margin-left: 30px"
                >
                  <el-input
                    v-model="weiXinForm.appId"
                    :placeholder="$t('terminalConfig.qsrxcxid')"
                    style="width: 100%"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :lg="8">
                <el-form-item
                  :label="$t('terminalConfig.xcxmy')"
                  prop="appSecret"
                  style="margin-left: 30px"
                >
                  <el-input
                    v-model="weiXinForm.appSecret"
                    :placeholder="$t('terminalConfig.qsrxcxmy')"
                    style="width: 100%"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-form>
      </div>
      <div class="merchants-list">
        <el-form
          :model="weiXinForm"
          ref="wxRules"
          :label-width="language == 'en' ? 'auto' : '115px'"
          class="demo-ruleForm"
        >
          <div class="card">
            <div class="title">{{ $t("terminalConfig.dyxx") }}</div>
            <el-row :gutter="3" style="margin-left: 14px; padding-bottom: 18px">
              <el-col :lg="8">
                <el-form-item
                  :label="$t('terminalConfig.gmcg')"
                  style="margin-left: 30px"
                >
                  <el-input
                    v-model="weiXinForm.pay_success"
                    :placeholder="$t('terminalConfig.qsrgmcg')"
                    style="width: 100%"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :lg="8">
                <el-form-item
                  :label="$t('terminalConfig.ddfh')"
                  style="margin-left: 30px"
                >
                  <el-input
                    v-model="weiXinForm.delivery"
                    :placeholder="$t('terminalConfig.qsrddfh')"
                    style="width: 100%"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :lg="8">
                <el-form-item
                  :label="$t('terminalConfig.tktz')"
                  style="margin-left: "
                >
                  <el-input
                    v-model="weiXinForm.refund_res"
                    :placeholder="$t('terminalConfig.qsrtktz')"
                    style="width: 100%; padding-right: 20px"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-form>
      </div>
      <div class="merchants-list" style="margin-bottom: 0px">
        <el-form
          :model="weiXinForm"
          ref="wxRules"
          :label-width="language == 'en' ? 'auto' : '115px'"
          class="demo-ruleForm"
        >
          <div class="card">
            <div class="title">{{ $t("terminalConfig.shpz") }}</div>
            <el-row :gutter="3" style="margin-left: 30px; padding-bottom: 18px">
              <el-col :lg="7">
                <el-form-item :label="$t('terminalConfig.ycqb')">
                  <el-switch v-model="weiXinForm.Hide_your_wallet" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-form>
      </div>
    </div>

    <!-- Pc商城 -->
    <div v-if="tabPosition === '3'">
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
                  :key="i"
                  style="
                    margin-left: 48px;
                    padding-right: 20px;
                    padding-bottom: 0;
                  "
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
            <!-- <div class="shaco_flex">
              <div class="shaco_box_one">
                <div>
                  <el-form-item :label="$t(`terminalConfig.syspfxsz`) " style="margin-left: 30px">
                    <div class="set_box">
                      <el-form-item :label="$t(`terminalConfig.syhwewm`) " required>
                        <l-upload
                          :limit="1"
                          :text="$t(`terminalConfig.syhets`) "
                          v-model="homePageForm.H5Url"
                          :object_fit="`contain`"
                        >
                        </l-upload>
                      </el-form-item>
                      <el-form-item :label="$t(`terminalConfig.sywzsm`) " required>
                        <el-input
                          v-model="homePageForm.textExplain"
                          :placeholder="$t(`terminalConfig.sywzsmts`) "
                         style="width: 100%"
                      ></el-input>
                      </el-form-item>
                    </div>
                  </el-form-item>
                </div>
              </div>
            </div> -->
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
                          <img src="../../../assets/imgs/empty.png" alt="" />
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
    </div>

    <!-- 引导图 begin -->
    <div
      class="merchants-list"
      v-if="tabPosition != '3'"
      :class="tabPosition === '2' ? `` : 'intro'"
      :style="tabPosition === '2' ? `flex=1` : ''"
    >
      <div
        class="card"
        :style="tabPosition === '2' ? `height:100%;margin-bottom:0;` : ''"
      >
        <div class="title">
          <div>
            {{ $t("terminalConfig.ydt") }}
            <span style="color: rgb(151, 160, 180); font-size: 14px"
              >（ {{ $t("terminalConfig.zdktjsx") }}）</span
            >
          </div>
          <el-button
            class="shaco_btn"
            @click="showisAddGuidBox()"
            :disabled="page.tableData.length >= 3"
            >{{ $t("DemoPage.tableExamplePage.add_Guid") }}</el-button
          >
        </div>
        <!-- 弹框组件 end -->
        <div
          :style="
            page.tableData.length <= 0
              ? `padding: 0 20px 40px 20px`
              : 'padding: 0 20px 0px 20px'
          "
        >
          <el-table
            :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
            v-loading="page.loading"
            :data="page.tableData"
            ref="table"
            class="el-table"
            style="width: 100%"
            :height="tabPosition === '2' ? `520` : `252`"
            :style="
              page.tableData.length <= 0
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
                <img src="../../../assets/imgs/empty.png" alt="" />
                <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
              </div>
            </template>
            <el-table-column :label="$t('terminalConfig.xh')" align="center">
              <template slot-scope="scope">
                {{ scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('terminalConfig.tp')" align="center">
              <template slot-scope="scope">
                <img :src="scope.row.image" alt="" @error="handleErrorImg" />
              </template>
            </el-table-column>
            <!-- <el-table-column :label="$t('terminalConfig.lx')" align="center">
                <template slot-scope="scope">{{
                  scope.row.type == 1
                    ? $t("terminalConfig.qd")
                    : $t("terminalConfig.az")
                }}</template>
              </el-table-column> -->
            <el-table-column
              :label="$t('terminalConfig.pxh')"
              prop="sort"
              align="center"
            >
            </el-table-column>
            <el-table-column
              :label="$t('terminalConfig.tjsj')"
              width="250"
              align="center"
            >
              <template slot-scope="scope">
                {{ scope.row.add_date | dateFormat }}
              </template>
            </el-table-column>

            <el-table-column
              :label="$t('terminalConfig.cz')"
              width="250"
              align="center"
            >
              <template slot-scope="scope">
                <div class="OP-button">
                  <div class="OP-button-top">
                    <el-button
                      icon="el-icon-edit-outline"
                      @click="loadGuid(scope.row.id)"
                      >{{ $t("DemoPage.tableExamplePage.edit") }}
                    </el-button>
                    <el-button
                      icon="el-icon-delete"
                      @click="Delete(scope.row.id)"
                      >{{ $t("DemoPage.tableExamplePage.delete") }}
                    </el-button>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
    <!-- 引导图 end -->

    <div class="footerBox" v-if="tabPosition == '1' || tabPosition == '3'">
      <el-button
        class="bgColor"
        v-if="tabPosition != '2' && tabPosition != '3'"
        type="primary"
        @click="submitForm('ruleForm')"
        >{{ $t("DemoPage.tableFromPage.save") }}
      </el-button>
      <el-button
        class="bgColor"
        v-if="tabPosition == '3'"
        type="primary"
        @click="handleSaveConfiguration()"
        >{{ $t("DemoPage.tableFromPage.save") }}
      </el-button>
    </div>
    <!-- </el-main> -->

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

    <!-- 编辑加引导图弹框组件 -->
    <div class="dialog-block">
      <el-dialog
        :title="$t('terminalConfig.bjydt')"
        :visible.sync="dialogVisible"
        :before-close="close"
      >
        <el-form
          :model="guidForm"
          :rules="rules"
          ref="ruleForm1"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="pass-input">
            <el-form-item :label="$t('terminalConfig.ydt')" prop="image">
              <l-upload
                :limit="1"
                v-model="guidForm.image"
                ref="upload"
                :text="$t('terminalConfig.jy750')"
                :mask_layer="false"
              >
              </l-upload>
            </el-form-item>
            <el-form-item :label="$t('terminalConfig.xh')">
              <el-input
                v-model="guidForm.sort"
                :placeholder="$t('terminalConfig.qsrxh')"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footers">
            <el-form-item>
              <el-button @click="cancel('ruleForm1')" class="shaco_color">{{
                $t("terminalConfig.ccel")
              }}</el-button>
              <el-button
                type="primary"
                @click="saveGuid(guidForm.id, 'ruleForm1')"
                class="qdcolor"
                >{{ $t("terminalConfig.okk") }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
    <!-- 添加引导图弹框组件 -->
    <div class="dialog-block">
      <el-dialog
        :title="$t('terminalConfig.tjydt')"
        :visible.sync="isAddGuidBox"
        :before-close="close2"
      >
        <el-form
          :model="guidFormAdd"
          :rules="rules"
          ref="ruleForm2"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="pass-input">
            <el-form-item :label="$t('terminalConfig.ydt')" prop="image">
              <l-upload
                :limit="1"
                v-model="guidFormAdd.image"
                ref="upload"
                :text="$t('terminalConfig.jy750')"
                :mask_layer="false"
              >
              </l-upload>
            </el-form-item>
            <el-form-item :label="$t('terminalConfig.xh')">
              <el-input
                v-model="guidFormAdd.sort"
                type="number"
                @input="exgNumber()"
                :placeholder="$t('terminalConfig.qsrxh')"
                style="width: 360px"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footers">
            <el-button @click="cancel('ruleForm2')" class="shaco_color">{{
                $t("terminalConfig.ccel")
              }}</el-button>
              <el-button
                type="primary"
                @click="saveGuid(null, 'ruleForm2')"
                class="qdcolor"
                >{{ $t("terminalConfig.okk") }}</el-button
              >
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import main from "@/webManage/js/mall/terminalConfig/terminalList";
export default main;
</script>

<style scoped lang="less">
@import "../../../webManage/css/mall/terminalConfig/terminalList.less";
/deep/.el-form-item {
        .prompt {
            margin-left: 14px;
            color: #97a0b4;
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
