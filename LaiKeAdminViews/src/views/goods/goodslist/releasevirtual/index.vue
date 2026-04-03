<template>
  <div class="container" id="hhhh">
    <el-form
      ref="ruleForm"
      class="form-search"
      :rules="rules"
      :model="ruleForm"
      label-width="auto"
    >
      <div class="basic-info">
        <div class="header">
          <span>{{ $t('releasephysical.jcxx') }}</span>
        </div>
        <div class="basic-block">
          <div class="basic-items">
            <el-form-item
              :label="$t('ssgj')"
              prop="country_num">
              <el-select
                :disabled="edit_disabled"
                class="select-input"
                filterable
                v-model="ruleForm.country_num"
                :placeholder="$t('qxzssgj')"
              >
                <el-option
                  v-for="item in countriesList"
                  :key="item.id"
                  :label="item.zh_name"
                  :value="item.num3"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('yz')" prop="lang_code">
              <el-select
                :disabled="edit_disabled"
                class="select-input"
                v-model="ruleForm.lang_code"
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
            </el-form-item>
          </div>
          <div class="basic-items">
            <el-form-item :label="$t('releasephysical.spbt')" prop="goodsTitle">
              <el-input
                v-model="ruleForm.goodsTitle"
                :placeholder="$t('releasephysical.qsrspbt')"
              ></el-input>
            </el-form-item>
            <el-form-item :label="$t('releasephysical.fbt')" prop="subtitle">
              <el-input
                v-model="ruleForm.subtitle"
                :placeholder="$t('releasephysical.qsrfbt')"
              ></el-input>
            </el-form-item>
            <el-form-item :label="$t('releasephysical.gjc')" prop="keywords">
              <el-input
                v-model="ruleForm.keywords"
                :placeholder="$t('releasephysical.qsrspgjc')"
              ></el-input>
            </el-form-item>
          </div>
          <div class="basic-items">
            <el-form-item
              :label="$t('releasephysical.spfl')"
              class="goods-class"
              prop="goodsClass"
            >
              <el-cascader
                v-model="ruleForm.goodsClass"
                class="select-input"
                ref="myCascader"
                :placeholder="$t('releasephysical.qsrspfl')"
                :options="classList"
                :props="{ checkStrictly: true }"
                @change="changeProvinceCity"
                clearable
              >
              </el-cascader>
              <el-button type="primary" @click="dialogShow1">{{
                $t('releasephysical.tjfl')
              }}</el-button>
            </el-form-item>
            <el-form-item
              class="goods-brand"
              :label="$t('releasephysical.sppp')"
              prop="goodsBrand"
            >
              <el-select
                class="select-input"
                v-model="ruleForm.goodsBrand"
                :placeholder="$t('releasephysical.qxzsppp')"
              >
                <el-option
                  v-for="item in brandList"
                  :key="item.brand_id"
                  :label="item.brand_name"
                  :value="item.brand_id"
                >
                </el-option>
              </el-select>
              <el-button type="primary" @click="dialogShow2">{{
                $t('releasephysical.tjpp')
              }}</el-button>
            </el-form-item>
            <!-- <el-form-item :label="$t('releasephysical.zl')" prop="weight">
              <el-input
                @keyup.native="ruleForm.weight = oninput(ruleForm.weight, 2)"
                v-model="ruleForm.weight"
                :placeholder="$t('releasephysical.qsrzl')"
                ><template slot="append">KG</template></el-input
              >
            </el-form-item> -->
          </div>
          <el-form-item
            class="goods-img"
            required
            :label="$t('releasephysical.spfmt')"
            prop="goodsCover"
          >
            <l-upload
              :limit="1"
              v-model="ruleForm.goodsCover"
              :text="$t('releasephysical.spfmtbz')"
            >
            </l-upload>
          </el-form-item>
          <el-form-item
            class="goods-img"
            required
            :label="$t('releasephysical.spzst')"
            prop="goodsShow"
          >
            <l-upload
              :limit="5"
              :mainImg="true"
              v-model="ruleForm.goodsShow"
              :text="$t('releasephysical.spzxtbz')"
            >
            </l-upload>
          </el-form-item>

          <el-form-item class="goods-img" :label="$t('releasephysical.zssp')">
            <div class="upload-box">
              <div class="avatar-uploader-box">
                <!-- 图片预览 -->
                <div
                  :key="index"
                  class="video-preview"
                  v-for="(item, index) in videoList"
                >
                  <video
                    :src="item.url"
                    @mouseover.stop="item.isShowPopup = true"
                    class="avatar"
                  >
                    {{ $t('releasephysical.bzcspbf') }}
                  </video>
                  <!-- 显示查看和删除的按钮弹窗 -->
                  <div
                    @mouseleave="item.isShowPopup = false"
                    class="avatar-uploader-popup"
                    v-show="item.url && item.isShowPopup"
                  >
                    <i @click="previewVideo(item)" class="el-icon-view"></i>
                    <i @click="deleteVideo(index)" class="el-icon-delete"></i>
                  </div>
                </div>

                <!-- 方框样式 -->
                <el-upload
                  :action="actionUrl"
                  :auto-upload="false"
                  :on-change="handleAvatarChange"
                  :show-file-list="false"
                  class="avatar-uploader"
                  ref="avatarUploader"
                  v-show="this.videoList.length == 0"
                >
                  <span style="display: block" v-loading="videoLoading">
                    <i class="el-icon-plus avatar-uploader-icon"></i>
                  </span>
                </el-upload>

                <!-- 上传提示文字样式 -->
                <div class="upload-tip">
                  <slot>{{ $t('releasephysical.sptext') }}</slot>
                </div>
              </div>

              <!-- 查看大图 -->
              <el-dialog
                :visible.sync="dialogVisible_video"
                append-to-body
                center
                :title="$t('releasephysical.spck')"
                :before-close="handleClose"
              >
                <video
                  :src="videoSrc"
                  ref="video"
                  controls
                  alt
                  width="100%"
                  style="height: 500px"
                >
                  {{ $t('releasephysical.bzcspbf') }}
                </video>
              </el-dialog>
            </div>
          </el-form-item>
        </div>
      </div>
      <div class="basic-info">
        <div class="header">
          <span>{{ $t('releasephysical.hxsz') }}</span>
        </div>
        <div class="basic-block">
          <div class="basic-items">
            <el-form-item
              :label="$t('releasephysical.sfxyhx')"
              style="width: 100%"
            >
              <el-radio-group
                :disabled="$route.query.name == 'editor'&&$route.query.type != 'deafts'"
                v-model="writeOffSettings"
                @change="writeChange"
              >
                <el-radio :disabled="storeList.lenght == 0" :label="1">{{
                  $t('releasephysical.xxhx')
                }}</el-radio>
                <el-radio :label="2">{{ $t('releasephysical.wxhx') }}</el-radio>
              </el-radio-group>
              <span class="show-font">{{ $t('releasephysical.text14') }}</span>
              <div class="box" v-if="writeOffSettings == 1">
                <el-form-item :label="$t('releasephysical.yysjsz')">
                  <el-switch
                    v-model="isAppointment"
                    :active-value="2"
                    :inactive-value="1"
                    active-color="#13ce66"
                    inactive-color="#d5dbe8"
                  >
                  </el-switch>
                  <span class="font1">
                    {{ $t('releasephysical.mrwgbzt') }}</span
                  >
                </el-form-item>
                <el-form-item el-form-item :label="$t('releasephysical.hxmd')">
                  <el-select
                    class="select-input"
                    v-model="writeOffMchIds"
                    filterable
                    multiple
                    default-first-option
                    :placeholder="$t('releasephysical.qba')"
                  >
                    <el-option
                      v-for="item in storeList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    >
                    </el-option>
                  </el-select>
                </el-form-item>
              </div>
            </el-form-item>
          </div>
        </div>
      </div>
      <div class="goods-attribute">
        <div class="header">
          <span>{{ $t('releasephysical.ggsz') }}</span>
        </div>
        <div class="attribute-block">
          <div class="attribute-items">
            <el-form-item :label="$t('releasephysical.cbj')" prop="cbj">
              <el-input
                v-model="ruleForm.cbj"
                @keyup.native="ruleForm.cbj = oninput(ruleForm.cbj, 2)"
                :placeholder="$t('releasephysical.qsrmrcbj')"
                ><template slot="append">{{
                  laikeCurrencySymbol
                }}</template></el-input
              >
            </el-form-item>
            <el-form-item :label="$t('releasephysical.yj')" prop="yj">
              <el-input
                v-model="ruleForm.yj"
                @keyup.native="ruleForm.yj = oninput(ruleForm.yj, 2)"
                :placeholder="$t('releasephysical.qsrmryj')"
                ><template slot="append">{{
                  laikeCurrencySymbol
                }}</template></el-input
              >
            </el-form-item>
            <el-form-item :label="$t('releasephysical.sj')" prop="sj">
              <el-input
                v-model="ruleForm.sj"
                @keyup.native="ruleForm.sj = oninput(ruleForm.sj, 2)"
                :placeholder="$t('releasephysical.qsrmrsj')"
                ><template slot="append">{{
                  laikeCurrencySymbol
                }}</template></el-input
              >
            </el-form-item>
          </div>
          <div class="attribute-items">
            <el-form-item :label="$t('releasephysical.dw')" prop="unit">
              <el-select
                class="select-input"
                v-model="ruleForm.unit"
                :placeholder="$t('releasephysical.qxzdw')"
              >
                <el-option
                  v-for="item in unitList"
                  :key="item.id"
                  :label="item.text"
                  :value="item.text"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div class="attribute-items">
            <el-form-item
              class="goods-img"
              prop="attrImg"
              :label="$t('releasephysical.spsxtp')"
            >
              <l-upload
                :limit="1"
                v-model="ruleForm.attrImg"
                :text="$t('releasephysical.jycc160px')"
              >
              </l-upload>
            </el-form-item>
          </div>
          <el-form-item
            :label="$t('releasephysical.szsx')"
            class="add-attribute"
            prop="attr"
          >
            <el-button
              type="primary"
              @click="addAttr"
              :class="$route.query.name == 'editor'&&$route.query.type != 'deafts' ? 'opacity' : ''"
              :disabled="$route.query.name == 'editor'&&$route.query.type != 'deafts'"
              >{{ $t('releasephysical.tjsx') }}</el-button
            >
          </el-form-item>
          <el-form-item class="attribute-table">
            <el-table
              v-if="strArr.length > 0"
              :data="strArr"
              ref="table"
              style="width: 100%"
              max-height="340"
            >
              <el-table-column
                v-for="(item, index) of attrTitle"
                :key="index"
                prop="attr_name"
                :label="item.attr_group_name"
                align="center"
                show-overflow-tooltip
                column-key="attr_name"
                :filters="filterTable[index]"
                :filter-method="filterHandler"
              >
                <template slot-scope="scope">
                  <div v-text="scope.row.attr_list[index].attr_name"></div>
                </template>
              </el-table-column>
              <el-table-column
                :label="$t('releasephysical.cbj')"
                align="center"
                min-width="150"
              >
                <template slot-scope="scope">
                  <div>
                    <el-input
                      v-model="scope.row.cbj"
                      style="width: 140px"
                    ></el-input>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                :label="$t('releasephysical.yj')"
                align="center"
                min-width="150"
              >
                <template slot-scope="scope">
                  <div>
                    <el-input
                      v-model="scope.row.yj"
                      style="width: 140px"
                    ></el-input>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                :label="$t('releasephysical.sj')"
                align="center"
                min-width="150"
              >
                <template slot-scope="scope">
                  <div>
                    <el-input
                      v-model="scope.row.sj"
                      style="width: 140px"
                    ></el-input>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                :label="$t('releasephysical.hxcs')"
                align="center"
                min-width="150"
                v-if="writeOffSettings == 1"
              >
                <template slot-scope="scope">
                  <div>
                    <el-input
                      :disabled="$route.query.name == 'editor'&&$route.query.type != 'deafts'"
                      v-model="scope.row.kucun"
                      oninput="value=value.replace(/^(0+)|[^\d]/g,'')"
                      style="width: 140px"
                    ></el-input>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="unit"
                :label="$t('releasephysical.dw')"
                align="center"
              >
              </el-table-column>
              <el-table-column
                :label="$t('releasephysical.txm')"
                align="center"
                width="200"
              >
                <template slot-scope="scope">
                  <div style="display: flex; align-items: center">
                    <el-input v-model="scope.row.bar_code"></el-input>
                    <el-link
                      style="
                        white-space: nowrap;
                        margin-left: 10px;
                        color: #2890ff;
                      "
                      >{{ $t('releasephysical.sm') }}</el-link
                    >
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                fixed="right"
                :label="$t('releasephysical.sctp')"
                width="140"
              >
                <template slot-scope="scope">
                  <div @click="clickImage">
                    <l-upload
                      :limit="1"
                      :is_small_img="true"
                      v-model="scope.row.img"
                      text=""
                      :size="40"
                      :heightSize="40"
                    ></l-upload>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-form-item>
        </div>
      </div>

      <div class="goods-set">
        <div :class="isShow ? 'header' : 'header2'">
          <span>{{ $t('releasephysical.xssz') }}</span>
          <div class="box" @click="launchShow">
            <span class="font-color">{{
              isShow ? $t('releasephysical.sq') : $t('releasephysical.zk')
            }}</span>
            <img
              v-if="isShow"
              src="../../../../assets/imgs/jts.png"
              width="16"
              height="8"
            />
            <img
              v-if="!isShow"
              src="../../../../assets/imgs/jtx.png"
              width="16"
              height="8"
            />
          </div>
        </div>
        <div class="set-block" v-if="isShow">

          <el-form-item
            class="show-local2"
            :label="$t('releasephysical.qdxs')"
            prop="checked"
          >
            <el-radio-group v-model="ruleForm.checked">
              <el-radio
                v-for="label in showAdrList"
                :label="label.value"
                :key="label.value"
                >{{ label.text }}</el-radio
              >
            </el-radio-group>
            <span class="show-font">{{ $t('releasephysical.rgbx') }}</span>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.xnxl')" prop="virtualSales">
            <el-input
              v-model="ruleForm.virtualSales"
              @keyup.native="
                ruleForm.virtualSales = oninput2(ruleForm.virtualSales)
              "
            ></el-input>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.pxh')">
            <el-input
              v-model="ruleForm.sequence"
              @keyup.native="ruleForm.sequence = oninput2(ruleForm.sequence)"
            ></el-input>
          </el-form-item>
        </div>
      </div>
      <div class="detail-container">
        <div :class="isContent ? 'header' : 'header2'">
          <span>{{ $t('releasephysical.spxx') }}</span>
          <div class="box" @click="launchContent">
            <span class="font-color">{{
              isContent ? $t('releasephysical.sq') : $t('releasephysical.zk')
            }}</span>
            <img
              v-if="isContent"
              src="../../../../assets/imgs/jts.png"
              width="16"
              height="8"
            />
            <img
              v-if="!isContent"
              src="../../../../assets/imgs/jtx.png"
              width="16"
              height="8"
            />
          </div>
        </div>
        <div class="detail-block" v-if="isContent">
          <el-form-item
            :label="$t('releasephysical.spsp')"
            style="margin-bottom: 0"
          >
            <div class="upload-box">
              <div class="avatar-uploader-box">
                <div
                  :key="index"
                  class="video-preview"
                  v-for="(item, index) in videoList2"
                >
                  <video
                    :src="item.url"
                    @mouseover.stop="item.isShowPopup = true"
                    class="avatar"
                  >
                    {{ $t('releasephysical.bzcspbf') }}
                  </video>
                  <div
                    @mouseleave="item.isShowPopup = false"
                    class="avatar-uploader-popup"
                    v-show="item.url && item.isShowPopup"
                  >
                    <i @click="previewVideo2(item)" class="el-icon-view"></i>
                    <i @click="deleteVideo2(index)" class="el-icon-delete"></i>
                  </div>
                </div>
                <el-upload
                  :action="actionUrl"
                  :auto-upload="false"
                  :on-change="handleAvatarChange2"
                  :show-file-list="false"
                  class="avatar-uploader"
                  ref="avatarUploader2"
                  v-show="this.videoList2.length == 0"
                >
                  <span style="display: block" v-loading="videoLoading2">
                    <i class="el-icon-plus avatar-uploader-icon"></i>
                  </span>
                </el-upload>

                <div class="upload-tip">
                  <slot>{{ $t('releasephysical.sptext2') }}</slot>
                </div>
              </div>

              <el-dialog
                :visible.sync="dialogVisible_video2"
                append-to-body
                center
                :title="$t('releasephysical.spck')"
                :before-close="handleClose5"
              >
                <video
                  :src="videoSrc2"
                  ref="video"
                  controls
                  alt
                  width="100%"
                  style="height: 500px"
                >
                  {{ $t('releasephysical.bzcspbf') }}
                </video>
              </el-dialog>
            </div>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.spjs')">
            <el-button class="add" type="primary" @click="addIntro">{{
              $t('releasephysical.tjjsa')
            }}</el-button>
            <div
              class="add_box"
              v-for="(item, index) in IntroList"
              :key="index"
            >
              <div class="delet-box">
                <span>{{ item.name }}</span>
                <el-button
                  icon="el-icon-delete"
                  type="text"
                  @click="delIntro(index)"
                  >{{ $t('releasephysical.sc') }}</el-button
                >
              </div>
              <el-form-item>
                <vue-editor
                  v-model="item.content"
                  useCustomImageHandler
                  @image-added="handleImageAdded"
                ></vue-editor>
              </el-form-item>
            </div>
          </el-form-item>
          <!-- <el-form-item :label="$t('releasephysical.spxq')" prop="content">
            <vue-editor
              v-model="ruleForm.content"
              useCustomImageHandler
              @image-added="handleImageAdded"
            ></vue-editor>
          </el-form-item> -->
        </div>
      </div>

      <div class="footer-button">
        <el-button
          plain
          class="footer-cancel fontColor"
          @click="leave"
          >{{ $t('merchants.addmerchants.cancel') }}</el-button
        >
        <el-button
          type="primary"
          class="footer-save bgColor mgleft"
          @click="submitForm('ruleForm')"
          >{{ $t('merchants.addmerchants.save') }}</el-button
        >
      </div>
    </el-form>
    <!-- 添加分类 -->
    <div class="dialog-class">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('releasephysical.tjfl')"
        :visible.sync="dialogVisible1"
        :before-close="handleClose1"
        :append-to-body="false"
      >
        <el-form
          :model="ruleForm2"
          :rules="rules2"
          ref="ruleForm2"
          class="picture-ruleForm"
          label-width="100px"
        >
          <el-form-item :label="$t('releasephysical.flmc')" prop="classname">
            <el-input
              v-model="ruleForm2.classname"
              maxlength="15"
              show-word-limit
              :placeholder="$t('releasephysical.qsrflmc')"
            ></el-input>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.ywfbt')">
            <el-input
              v-model="ruleForm2.subtitle"
              :placeholder="$t('releasephysical.qsrywfbq')"
            ></el-input>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.fldj')" prop="classlevel">
            <el-select
              class="select-input"
              v-model="ruleForm2.classlevel"
              :placeholder="$t('releasephysical.qsrfldj')"
            >
              <el-option
                v-for="(item, index) in menuLevelList"
                :key="index"
                :label="item.label"
                :value="item.label"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item
            :label="$t('releasephysical.sjfl')"
            prop=""
            v-if="inputShow !== 1"
          >
            <div class="superior">
              <el-select
                v-show="inputShow >= 2"
                class="select-input"
                v-model="ruleForm2.select_2"
                :placeholder="$t('releasephysical.qxzyj')"
              >
                <el-option
                  v-for="(item, index) in options1"
                  :key="index"
                  :label="item.pname"
                  :value="item.cid"
                >
                </el-option>
              </el-select>
              <el-select
                v-show="inputShow >= 3"
                class="select-input"
                v-model="ruleForm2.select_3"
                :placeholder="$t('releasephysical.qxzej')"
              >
                <el-option
                  v-for="(item, index) in options2"
                  :key="index"
                  :label="item.pname"
                  :value="item.cid"
                >
                </el-option>
              </el-select>
              <el-select
                v-show="inputShow >= 4"
                class="select-input"
                v-model="ruleForm2.select_4"
                :placeholder="$t('releasephysical.qxzsj')"
              >
                <el-option
                  v-for="(item, index) in options3"
                  :key="index"
                  :label="item.pname"
                  :value="item.cid"
                >
                </el-option>
              </el-select>
              <el-select
                v-show="inputShow >= 5"
                class="select-input"
                v-model="ruleForm2.select_5"
                :placeholder="$t('releasephysical.qxzsij')"
              >
                <el-option
                  v-for="(item, index) in options4"
                  :key="index"
                  :label="item.pname"
                  :value="item.cid"
                >
                </el-option>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item
            class="upload-img"
            :label="$t('releasephysical.fltb')"
            prop="classLogo"
          >
            <l-upload
              :limit="1"
              ref="classUpload"
              v-model="ruleForm2.classLogo"
              :text="$t('releasephysical.fltbbz')"
            >
            </l-upload>
          </el-form-item>
          <div class="form-footer">
            <el-form-item>
              <el-button class="bdColor" @click="handleClose1()" plain>{{
                $t('DemoPage.tableFromPage.cancel')
              }}</el-button>
              <el-button
                class="bgColor"
                type="primary"
                @click="submitForm2('ruleForm2')"
                >{{ $t('DemoPage.tableFromPage.save') }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
    <!-- 添加品牌 -->
    <div class="dialog-brand">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('releasephysical.tjpp')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <el-form
          :model="ruleForm3"
          :rules="rules3"
          ref="ruleForm3"
          class="picture-ruleForm"
          label-width="90px"
        >
          <el-form-item
            class="brandname"
            :label="$t('releasephysical.ppmc')"
            prop="brandname"
          >
            <el-input
              v-model="ruleForm3.brandname"
              :placeholder="$t('releasephysical.qsrppmc')"
            ></el-input>
          </el-form-item>
          <el-form-item
            class="upload-img"
            :label="$t('releasephysical.pplo')"
            prop="brandLogo"
            required
          >
            <l-upload
              :limit="1"
              ref="upload3"
              v-model="ruleForm3.brandLogo"
              :text="$t('releasephysical.pplobz')"
            >
            </l-upload>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.ssfl')" prop="brandtype">
            <el-select
              class="select-input belongClass"
              multiple
              filterable
              v-model="ruleForm3.brandtype"
              :placeholder="$t('releasephysical.qxzssfl')"
            >
              <el-option
                v-for="(item, index) in brandTypeList"
                :key="index"
                :label="item.pname"
                :value="item.cid"
              >
                <div @click="getId(item.cid)">{{ item.pname }}</div>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.ssgj')" prop="countries">
            <el-select
              class="select-input"
              filterable
              v-model="ruleForm3.countries"
              :placeholder="$t('releasephysical.qxzssgj')"
            >
              <el-option
                v-for="(item, index) in countriesList"
                :key="index"
                :label="item.zh_name"
                :value="item.zh_name"
              >
                <div @click="getIds(item.id)">{{ item.zh_name }}</div>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item
            class="textarea"
            :label="$t('releasephysical.bz')"
            prop="note"
          >
            <el-input
              type="textarea"
              v-model="ruleForm3.note"
              :placeholder="$t('releasephysical.qtxbz')"
            ></el-input>
          </el-form-item>
          <div class="form-footer">
            <el-form-item>
              <el-button class="bdColor" @click="handleClose2" plain>{{
                $t('DemoPage.tableFromPage.cancel')
              }}</el-button>
              <el-button
                class="bgColor"
                type="primary"
                @click="submitForm3('ruleForm3')"
                >{{ $t('DemoPage.tableFromPage.save') }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
    <!-- 添加运费 -->
    <div class="dialog-freight">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('releasephysical.tjyf')"
        :visible.sync="dialogVisible3"
        :before-close="handleClose3"
      >
        <el-form
          :model="ruleForm5"
          :rules="rules5"
          ref="ruleForm5"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="notice">
            <el-form-item
              class="title"
              :label="$t('releasephysical.yfmc')"
              prop="templateName"
            >
              <el-input
                v-model="ruleForm5.templateName"
                :placeholder="$t('releasephysical.qsryfmc')"
              ></el-input>
            </el-form-item>
            <el-form-item
              class="rule-block"
              required
              :label="$t('releasephysical.yfgz')"
            >
              <div class="add-rule">
                <el-button
                  class="bgColor"
                  @click="dialogShow5"
                  type="primary"
                  >{{ $t('releasephysical.tjgz') }}</el-button
                >
              </div>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button
                class="bgColor"
                type="primary"
                @click="submitForm4('ruleForm5')"
                >{{ $t('DemoPage.tableFromPage.save') }}</el-button
              >
              <el-button
                class="bdColor"
                @click="dialogVisible3 = false"
                plain
                >{{ $t('DemoPage.tableFromPage.cancel') }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
        <div class="dictionary-list" v-if="tableData.length !== 0">
          <el-table
            :data="tableData"
            ref="table"
            class="el-table"
            style="width: 100%"
            :height="250"
          >
            <el-table-column
              prop="freight"
              :label="$t('releasephysical.yf')"
              width="80"
            >
            </el-table-column>
            <el-table-column prop="name" :label="$t('releasephysical.sf')">
            </el-table-column>
            <el-table-column :label="$t('releasephysical.cz')" width="80">
              <template slot-scope="scope">
                <div class="OP-button">
                  <div class="OP-button-top">
                    <el-button
                      icon="el-icon-delete"
                      @click="Delete(scope.row, scope.$index)"
                      >{{ $t('releasephysical.shanchu') }}</el-button
                    >
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-dialog>
    </div>
    <!-- 添加运费模板 -->
    <div class="dialog-freightTemplate">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('releasephysical.tjyfgz')"
        :visible.sync="dialogVisible4"
        :before-close="handleClose4"
      >
        <el-form
          :model="ruleForm6"
          :rules="rules6"
          ref="ruleForm6"
          label-width="100px"
          class="demo-ruleForm"
        >
          <el-form-item :label="$t('releasephysical.yf')" prop="freight">
            <el-input
              v-model="ruleForm6.freight"
              @keyup.native="ruleForm6.freight = oninput(ruleForm6.freight, 2)"
            ></el-input>
          </el-form-item>
          <el-form-item
            class="check-provinces"
            :label="$t('releasephysical.xzsf')"
            prop="status"
          >
            <el-checkbox
              :indeterminate="isIndeterminate"
              v-model="checkAll"
              @change="handleCheckAllChange"
              >{{ $t('releasephysical.quanxuan') }}</el-checkbox
            >
            <el-checkbox-group
              v-model="checkedCities"
              @change="handleCheckedCitiesChange"
            >
              <el-checkbox v-for="city in cities" :label="city" :key="city">{{
                city
              }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <div class="form-footer">
            <el-form-item>
              <el-button class="bgColor" @click="dialogVisible4 = false">{{
                $t('releasephysical.ccel')
              }}</el-button>
              <el-button
                class="bdColor"
                type="primary"
                @click="determine('ruleForm6')"
                >{{ $t('releasephysical.okk') }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('releasephysical.tjjsa')"
        :visible.sync="dialogVisible"
        :before-close="handleClose7"
      >
        <el-form
          :model="ruleForm7"
          :rules="rules7"
          ref="ruleForm7"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="task-container">
            <el-form-item :label="$t('releasephysical.btmc')" prop="name">
              <el-input
                v-model="ruleForm7.name"
                :placeholder="$t('releasephysical.qsrbqmc')"
                maxlength="10"
              >
              </el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button class="qx_bt" @click="handleClose7()">{{
                $t('DemoPage.tableFromPage.cancel')
              }}</el-button>
              <el-button
                class="bdColor"
                type="primary"
                @click="addIntroduction('ruleForm7')"
                >{{ $t('DemoPage.tableFromPage.save') }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
    <!-- 设置属性组件 -->
    <l-Transfer
      v-if="transferDialog"
      :visible.sync="transferDialog"
      :data-source="dataSource"
      :attr-key="attrKey"
      @submit="handleSubmit"
    />
    <div class="model" v-show="dialogVisible1 || dialogVisible2"></div>
  </div>
</template>

<script>
import $ from 'jquery'
import { VueEditor } from 'vue2-editor'
import {
  getGoodsActiveList,
  label,
  addProduct,
  choiceClass,
  goodsUnit,
  addGoods,
  getGoodsInfoById,
  getStoreList
} from '@/api/goods/goodsList'
import {
  getFreightInfo,
  addFreight,
  getRegion
} from '@/api/goods/freightManagement'
import {
  addDrafts,
  queryDraftsInfo
} from "@/api/draftsList/draftsList";
import { addClass } from '@/api/goods/goodsClass'
import { addBrand, getClassInfo, getCountry } from '@/api/goods/brandManagement'
import { dictionaryList } from '@/api/Platform/numerical'
import Config from '@/packages/apis/Config'
import { getStorage, setStorage } from '@/utils/storage'
import axios from 'axios'
import { isArray } from 'ali-oss/lib/common/utils/isArray'
import getPageTitle from '@/utils/get-page-title'
var attrFlag = true
export default {
  name: 'releasevirtual',
  components: {
    VueEditor
  },
  data () {
    // var validatePass = (rule, value, callback) => {
    //   if (!value.length) {
    //     callback(new Error("请选择配送方式"));
    //   } else {
    //     this.$refs.ruleForm.clearValidate('defaultLogo')
    //     callback();
    //   }
    // };
    return {
      laikeCurrencySymbol:'￥',
      // 国家
      countryList: [],

      // 语种
      languages: [],

      // 添加介绍弹窗
      dialogVisible: false,
      ruleForm7: {
        name: ''
      },
      rules7: {
        name: [{ required: true, message: '请输入标题名称', trigger: 'blur' }]
      },
      IntroList: [],
      ruleForm: {
        // 基本信息
        country_num: "",
        lang_code: "",
        goodsTitle: '',
        subtitle: '',
        keywords: '',
        goodsClass: '',
        goodsBrand: '',
        goodsCover: '',
        goodsShow: '',
        // weight: '',
        // 商品属性
        cbj: '',
        yj: '',
        sj: '',
        unit: '',
        // kucun: '',
        attr: '',
        attrImg: '',

        // 商品设置
        // inventoryWarning: '',
        // freight: '',
        checkedLabel: [],
        // distributionMode: [],
        checkedActivity: 1,
        checked: '',
        virtualSales: '',
        sequence: '',
        edit_disabled:false,
        laikeCurrencySymbol:'￥',
        // 详细内容
        content: ''
      },
      rules: {
        // 基本信息
        country_num: [
          {
            required: true,
            message: this.$t("releasephysical.qxzssgj"),
            trigger: "blur",
          },
        ],
        lang_code: [
          {
            required: true,
            message: this.$t("releasephysical.qxzyz"),
            trigger: "blur",
          },
        ],
        goodsTitle: [
          {
            required: true,
            message: this.$t('releasephysical.qsrspbt'),
            trigger: 'blur'
          }
        ],
        keywords: [
          {
            required: true,
            message: this.$t('releasephysical.qsrspgjc'),
            trigger: 'blur'
          }
        ],
        goodsClass: [
          {
            required: true,
            message: this.$t('releasephysical.qsrspfl'),
            trigger: 'blur'
          }
        ],
        goodsBrand: [
          {
            required: true,
            message: this.$t('releasephysical.qxzsppp'),
            trigger: 'blur'
          }
        ],
        goodsCover: [
          {
            required: true,
            message: this.$t('releasephysical.qscfmt'),
            trigger: 'change'
          }
        ],
        goodsShow: [
          {
            required: true,
            message: this.$t('releasephysical.qsczst'),
            trigger: 'change'
          }
        ],
        // 商品属性
        cbj: [
          {
            required: true,
            message: this.$t('releasephysical.qsrmrcbj'),
            trigger: 'blur'
          }
        ],
        yj: [
          {
            required: true,
            message: this.$t('releasephysical.qsrmryj'),
            trigger: 'blur'
          }
        ],
        sj: [
          {
            required: true,
            message: this.$t('releasephysical.qsrmrsj'),
            trigger: 'blur'
          }
        ],
        unit: [
          {
            required: true,
            message: this.$t('releasephysical.qxzdw'),
            trigger: 'change'
          }
        ],
        attrImg: [
          {
            required: true,
            message: this.$t('releasephysical.text11'),
            trigger: 'change'
          }
        ],
        checkedActivity: [
          {
            required: true,
            message: this.$t('releasephysical.qxzzchd'),
            trigger: 'change'
          }
        ],
        content: [
          {
            required: true,
            message: this.$t('releasephysical.qsrspxq'),
            trigger: 'blur'
          }
        ]
      },

      check_attr: [], // 编辑复制选中回显属性数据
      check_attr1: [], // 移除的属性数据

      showAdrList: [], // 商品展示位置

      // 添加分类弹框数据
      dialogVisible1: false,
      ruleForm2: {
        classname: '',
        subtitle: '',
        classlevel: '',
        classLogo: '',
        select_2: '',
        select_3: '',
        select_4: '',
        select_5: ''
      },
      inputShow: 1,
      rules2: {
        lang_code: [
          {
            required: true,
            message: this.$t("releasephysical.qxzyz"),
            trigger: "change",
          },
        ],
        country_num: [
          {
            required: true,
            message: this.$t("releasephysical.qxzssgj"),
            trigger: "change",
          },
        ],
        classname: [
          {
            required: true,
            message: this.$t('releasephysical.qsrflmc'),
            trigger: 'blur'
          }
        ],
        // subtitle: [
        //   { required: true, message: '请输入英文副标题', trigger: 'blur' }
        // ],
        classlevel: [
          {
            required: true,
            message: this.$t('releasephysical.qsrfldj'),
            trigger: 'change'
          }
        ],
        superiorclass: [
          {
            required: true,
            message: this.$t('releasephysical.qsrsjfl'),
            trigger: 'change'
          }
        ]
      },

      menuLevelList: [
        {
          value: 1,
          label: this.$t('releasephysical.yij')
        },
        {
          value: 2,
          label: this.$t('releasephysical.erj')
        },
        {
          value: 3,
          label: this.$t('releasephysical.sanj')
        },
        {
          value: 4,
          label: this.$t('releasephysical.sij')
        },
        {
          value: 5,
          label: this.$t('releasephysical.wuj')
        }
      ],

      options1: [],

      options2: [],

      options3: [],

      options4: [],

      // 添加品牌弹框数据
      dialogVisible2: false,
      // 添加商品介绍弹窗
      dialogVisible: false,
      ruleForm7: {},
      rules7: {
        name: [{ required: true, message: '请输入标题名称', trigger: 'blur' }]
      },
      ruleForm3: {
        brandname: '',
        brandLogo: '',
        brandtype: '',
        lang_code: "",
        country_num: "",
        countries: '',
        note: ''
      },
      rules3: {
        brandtype: [
          {
            required: true,
            message: this.$t('releasephysical.qxzssfl'),
            trigger: 'change'
          }
        ],
        brandLogo: [
          {
            required: true,
            message: this.$t('releasephysical.qscpplg'),
            trigger: 'change'
          }
        ],
        lang_code: [
          {
            required: true,
            message: this.$t("releasephysical.qxzyz"),
            trigger: "change",
          },
        ],
        country_num: [
          {
            required: true,
            message: this.$t("releasephysical.qxzssgj"),
            trigger: "change",
          },
        ],
        brandname: [
          {
            required: true,
            message: this.$t('releasephysical.qsrppmc'),
            trigger: 'blur'
          }
        ]
      },

      brandTypeList: [],

      countriesList: [],

      id: null,
      countriesId: null,

      // 添加属性弹框数据
      imgArr: [],
      attrTitle: JSON.parse('[]', true), //可选规格数据
      strArr: JSON.parse('[]', true), //已选规格数据
      // strArr: [{"cbj":"11","yj":"1","sj":"1","unit":"根","kucun":"1","image":"https://laikeds.oss-cn-shenzhen.aliyuncs.com/1/0/1624863705554.jpg","bar_code":"","attr_list":[{"attr_id":"","attr_name":"银灰色","attr_group_name":"颜色"}],"cid":""}],
      unitList: [],

      // 添加运费弹框数据
      dialogVisible3: false,
      ruleForm5: {
        templateName: ''
      },

      rules5: {
        templateName: [
          {
            required: true,
            message: this.$t('releasephysical.qsryfmc'),
            trigger: 'blur'
          }
        ]
      },

      // 添加运费规则弹框数据
      dialogVisible4: false,
      tableData: [],
      checkAll: false,
      checkedCities: [],
      cities: [],
      isIndeterminate: true,
      ruleForm6: {
        freight: null
      },
      rules6: {
        freight: [
          {
            required: true,
            message: this.$t('releasephysical.qtxyf'),
            trigger: 'blur'
          }
        ]
      },

      // 商品分类列表
      classList: [],

      // 商品分类列表
      brandList: [],

      // 支持活动类型
      activityList: [],

      // 标签
      labelList: [],

      // 配送方式
      // distributionModeList: [
      //   {
      //     id: 1,
      //     name: '邮寄'
      //   },
      //   {
      //     id: 2,
      //     name: '自提'
      //   }
      // ],

      // 运费
      freightList: [],

      togdisable: false,

      actionUrl: Config.baseUrl,

      goodsEditorBase: '',
      storeIds: '',
      mchid: '',

      emptyAttr: false,

      classIds: [],

      freightType: true,
      brandType: true,
      videoList: [],
      videoList2: [],
      videoSrc: '',
      videoSrc2: '',
      dialogVisible_video: false,
      dialogVisible_video2: false,
      videoUp: '',
      videoUp2: '',
      videoLoading: false,
      videoLoading2: false,
      attrNum: 0,
      pdkc: '',
      transferDialog: false,
      dataSource: [], //给子组件原数据需要push的值，要配合key一起用
      attrKey: [], //给子组件回显的数据的key
      listX: 0,
      isContent: false,
      isShow: false,
      writeOffSettings: 2, //核销设置 1.线下核销 2.无需核销
      isAppointment: 1, //预约时间设置 1.无需预约下单 2.需要预约下单
      writeOffMchIds: [], //核销门店id  0全部门店,  1,2,3使用逗号分割
      storeList: [], //门店List
      // hxNum:[],
      mchSid: ''
    }
  },

  created () {

    let langCode = null;
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();

    if (this.$route.query.name && this.$route.query.name == 'editor') {

      this.edit_disabled = true;
      this.ruleForm.lang_code = this.$route.query.lang_code
      langCode = this.ruleForm.lang_code

      this.$router.currentRoute.matched[2].meta.title = '编辑虚拟商品'
      document.title = getPageTitle('编辑虚拟商品')
      this.mchSid = this.$route.query.mch_id
      this.classIds = this.$route.query.classId
        ?.split('-')
        .filter(item => {
          if (item !== '') {
            return item
          }
        })
        .map(item => {
          return parseInt(item)
        })
    } else if (this.$route.query.name && this.$route.query.name == 'copy') {
      this.edit_disabled = true;
      let title =  " L-" + this.$route.query.lang_name ;
      this.$router.currentRoute.matched[2].meta.title = '复制虚拟商品'
      document.title = getPageTitle('复制虚拟商品')
      this.mchSid = this.$route.query.mch_id
      this.classIds = this.$route.query.classId
        .split('-')
        .filter(item => {
          if (item !== '') {
            return item
          }
        })
        .map(item => {
          return parseInt(item)
        })
      this.ruleForm.lang_code = this.$route.query.lang_code
      langCode = this.ruleForm.lang_code
    } else {
      this.ruleForm.lang_code = this.LaiKeCommon.getUserLangVal();
      langCode = this.ruleForm.lang_code
      this.edit_disabled = false;
      // this.strArr = [{"cbj":"","yj":"","sj":"","unit":"张","kucun":"","img":"","bar_code":"","attr_list":[{"attr_id":"","attr_name":"默认","attr_group_name":"尺码"}],"cid":""}],
      // this.attrTitle = [{"attr_group_name":"尺码","attr_list":[{"attr_name":"默认"}]}]
      this.$router.currentRoute.matched[2].meta.title = '发布虚拟商品'
      document.title = getPageTitle('发布虚拟商品')

      this.emptyAttr = true
    }
    this.getDictionaryList(langCode)
    this.getClassInfoss(langCode)
    // this.getGoodsActiveLists()
    this.labels(langCode)
    this.choiceClasss(langCode).then(() => {
      if (this.$route.query.id) {
        this.allClass(this.classList)
      }
    })
    this.getRegions()
    this.getClassInfos(langCode)
    this.getCountrys()
    this.getLanguage()
    this.goodsUnits(langCode)
    this.getStore()
    if (
      this.$route.query.name == 'editor' &&
      this.$route.query.status !== '待上架'
    ) {
      this.togdisable = true
    }
    if (
      this.$route.query.name == 'editor' ||
      this.$route.query.name == 'copy'
    ) {
      // 草稿箱编辑回显
      if(this.$route.query.editId && this.$route.query.type =='deafts'){
        this.queryDraftsGoodsInfo()
        return
      }
      getGoodsInfoById({
        api: 'admin.goods.getGoodsInfoById',
        goodsId: this.$route.query.id
      })
        .then(res => {
          if (this.$route.query.name == 'editor') {
            this.mchid = res.data.data.list.mch_id
          }
          let goodsAttribute = res.data.data.list
          if (res.data.data.video) {
            this.videoList.push({
              videoFile: res.data.data.video,
              url: res.data.data.video,
              isShowPopup: false
            })
            this.videoUp = res.data.data.video
          }
          if (res.data.data.proVideo) {
            this.videoList2.push({
              videoFile: res.data.data.proVideo,
              url: res.data.data.proVideo,
              isShowPopup: false
            })
            this.videoUp2 = res.data.data.proVideo
          }
          if (goodsAttribute.content) {
            // let list = JSON.parse(goodsAttribute.content)
            try {
              this.IntroList = JSON.parse(goodsAttribute.content)
            } catch (error) {
              this.IntroList = []
            }
          }
          this.ruleForm.goodsTitle = goodsAttribute.product_title
          this.ruleForm.subtitle = goodsAttribute.subtitle
          this.ruleForm.keywords = goodsAttribute.keyword
          this.ruleForm.country_num = res.data.data.country_num;
          if(goodsAttribute.product_class){
            this.ruleForm.goodsClass = goodsAttribute.product_class
              .split('-')
              .filter(item => {
                if (item !== '') {
                  return item
                }
              })
              .map(item => {
                return parseInt(item)
              })
          }


          if(this.$route.query.name == "copy")
          {
            if(langCode == this.LaiKeCommon.getUserLangVal())
            {
              this.ruleForm.goodsBrand = goodsAttribute.brand_id;
              this.ruleForm.unit = res.data.data.initial.unit;
            } else {
              this.ruleForm.goodsBrand = "" ;
              this.ruleForm.unit = "";
            }
          }
          else
          {
            this.ruleForm.goodsBrand = goodsAttribute.brand_id;
            this.ruleForm.unit = res.data.data.initial.unit;
          }

          this.ruleForm.goodsCover = res.data.data.cover_map
          this.ruleForm.goodsShow = res.data.data.imgurls

          //禅道46076
          res.data.data.show_adr.forEach(item => {
            if (item.status == true) {
              // this.ruleForm.checked.push(item.value)
              this.ruleForm.checked = item.value.toString()
            }
          })
          this.ruleForm.virtualSales = goodsAttribute.volume
          // this.ruleForm.content = goodsAttribute.content
          // this.IntroList = JSON.parse(goodsAttribute.content)
          if (goodsAttribute.content) {
            // let list = JSON.parse(goodsAttribute.content)
            try {
              this.IntroList = JSON.parse(goodsAttribute.content)
            } catch (error) {
              this.IntroList = []
            }
          }
          this.ruleForm.cbj = res.data.data.initial.cbj
          this.ruleForm.yj = res.data.data.initial.yj
          this.ruleForm.sj = res.data.data.initial.sj
          // this.ruleForm.kucun = res.data.data.initial.kucun
          this.ruleForm.attrImg = res.data.data.initial.attrImg
          this.attrTitle = res.data.data.attr_group_list
          this.strArr = res.data.data.checked_attr_list
          //暂时弃用以下处理，编辑直接去掉添加属性按钮

          //核销设置
          this.writeOffSettings = goodsAttribute.write_off_settings
          if (this.writeOffSettings == 2) {
            //无需核销
            this.isAppointment = 1
            this.writeOffMchIds = []
          } else {
            //线下核销
            this.isAppointment = goodsAttribute.is_appointment
            //将获取的字符串转为number类型，不然下拉框不回显
            //判断为0是因为如果选择的是全部，不需要回显数据
            if (goodsAttribute.write_off_mch_ids == 0) {
              this.writeOffMchIds = []
            } else {
              this.writeOffMchIds = goodsAttribute.write_off_mch_ids
                .split(',')
                .map(Number)
            }
          }
          //给穿梭框组件传参所用
          this.dataSource = res.data.data.strArr
          this.dataSource.forEach(row => {
            this.attrKey.push(Number(row.sid))
          })

          this.ruleForm.sequence = res.data.data.list.sort
          choiceClass({
            api: 'admin.goods.choiceClass',
            classId: res.data.data.class_id
          }).then(res => {
            this.brandList = res.data.data.list.brand_list.filter(item => Number(item.notset) !== 1);
          })

          // this.check_attr = res.data.data.checked_attr_list.map((item) => {
          //   return item.attr_list[0];
          // });

          res.data.data.checked_attr_list.map(item => {
            this.check_attr = this.check_attr.concat(item.attr_list)
          })
          this.check_attr = this.unique(this.check_attr)
          this.check_attr = this.check_attr.map(item => {
            return {
              label: item.attr_group_name + ':' + item.attr_name,
              name1: item.attr_group_name + ':' + item.attr_name,
              name: item.attr_group_name,
              sname: item.attr_name,
              attr_id: item.attr_id ? item.attr_id : ''
            }
          })
        })
        .then(() => {
          this.getFreightInfos(langCode)
        })
    } else {
      this.togdisable = false
      this.getFreightInfos(langCode)
    }
  },

  mounted () {
    this.getBase()
    $('#maskClass').remove()
    this.storeIds = getStorage('rolesInfo').storeId
  },

  beforeRouteLeave (to, from, next) {
    if (
      to.name == 'physicalgoods' &&
      (this.$route.query.name == 'editor' || this.$route.query.name == 'copy')
    ) {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
      to.params.inputInfo = this.$route.query.inputInfo
    }
    if (to.name == 'memberGoods') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
      to.params.inputInfo = this.$route.query.inputInfo
    }
         /**
     * 草稿箱拦截
     * 判断是否有目录信息，
     * 如果没有直接放行
     * 如果有则弹出操作提示
     */

     /**
      * 无需判断的 属性
      * checkedActivity : 不知道是什么 页面没有交互 写死 的  1
      * freight :运费设置
      */
      let falg = false
      const statsNone = ['checkedActivity','freight']
      if( !['上架','下架','待上架'].includes(this.$route.query.status)){
        for(let key in this.ruleForm){
          // 根据 input 输入框 判断是否需要保存草稿箱
          if(typeof this.ruleForm[key] != 'object' && this.ruleForm[key] && !statsNone.includes(key)){
            falg = true
          }
        }
      }
      //
      if(!falg){
        next();
      }else{
        // 点击保存按钮时
        if(this.flag){
          next();
          return
        }
        // 对象没有任何改变时
        this.$confirm(
          this.$t('releasephysical.ckg'),
          this.$t('coupons.ts'),
          {
            confirmButtonText: this.$t('coupons.okk'),
            cancelButtonText: this.$t('coupons.ccel'),
            type: 'warning',
            closeOnClickModal:false
          }
        )
          .then( async () => {

            const res = await this.addDraftsBox()
            if(res.data.code == 200){
              this.$message({
	                message: this.$t("zdata.czcg"),
	                type: "success",
	                offset: 102,
	              });
                setTimeout(()=>{
                  next()
                },1500)
            } else{
              this.$message({
	                message:res.data.message,
	                type: "error",
	                offset: 102,
	              });
            }
          })
          .catch((e) => {
            next()
          })
      }
  },

  watch: {
    strArr: {
      handler (newName, oldName) {
        // && this.attrNum == 0
        if (newName && newName.length > 0) {
          this.ruleForm.attr = 1
          if (this.ruleForm.attrImg) {
            this.strArr?.filter(item => {
              if (!item.img) {
                item.img = this.ruleForm.attrImg
              }
            })
          }
        } else {
          this.ruleForm.attr = ''
        }
        console.log(123)
      },
      deep: true,
      immediate: true
    },

    //语种联动数据
    "ruleForm.lang_code"(val, oldVal) {

      //分类联动
      this.getClassInfoss(val);
      if(oldVal || this.$route.query.name == "copy" ){
        this.ruleForm.goodsClass = "";
        this.ruleForm.goodsBrand  = ""
        this.classList = [];
        this.classList.length = 0;

      }
      // 标签
      this.labels(val);
      this.choiceClasss(val).then(() => {
        if (this.$route.query.id) {
          this.allClass(this.classList);
        }
      });

      this.goodsUnits(val);
      this.getDictionaryList(val);


    },

    'ruleForm.cbj' (val, oldVal) {
      if (this.strArr && this.strArr.length > 0 && oldVal) {
        this.strArr?.filter(item => {
          item.cbj = val
        })
      }
    },

    'ruleForm.yj' (val, oldVal) {
      if (this.strArr && this.strArr.length > 0 && oldVal) {
        this.strArr?.filter(item => {
          item.yj = val
        })
      }
    },

    'ruleForm.sj' (val, oldVal) {
      if (this.strArr && this.strArr.length > 0 && oldVal) {
        this.strArr?.filter(item => {
          item.sj = val
        })
      }
    },

    'ruleForm.unit' (val, oldVal) {
      if (this.strArr && this.strArr.length > 0 && oldVal) {
        this.strArr?.filter(item => {
          item.unit = val
        })
      }
    },

    'ruleForm.attrImg' (val, oldVal) {
      // if(this.attrNum == 0){
      if (this.strArr && this.strArr.length > 0) {
        this.strArr?.filter(item => {
          if (!item.img) {
            item.img = val
          }
        })
        // }
      }
    },

    checkAll () {
      if (this.checkAll === true) {
        this.checkedCities = this.cities
      }
    },

    'ruleForm2.classlevel' () {
      if (this.ruleForm2.classlevel === this.$t('releasephysical.erj')) {
        this.inputShow = 2
      } else if (
        this.ruleForm2.classlevel === this.$t('releasephysical.sanj')
      ) {
        this.inputShow = 3
      } else if (this.ruleForm2.classlevel === this.$t('releasephysical.sij')) {
        this.inputShow = 4
      } else if (this.ruleForm2.classlevel === this.$t('releasephysical.wuj')) {
        this.inputShow = 5
      } else {
        this.inputShow = 1
      }
    },

    'ruleForm2.select_2' (newVal) {
      console.log(newVal)
      this.ruleForm2.select_3 = ''
      this.ruleForm2.select_4 = ''
      this.ruleForm2.select_5 = ''
      getClassInfo({
        api: 'admin.goods.getClassInfo',
        pageSize: 100,
        classId: newVal,
        type: 1
      }).then(res => {
        this.options2 = res.data.data.classInfo
      })
    },

    'ruleForm2.select_3' (newVal) {
      console.log(newVal)
      this.ruleForm2.select_4 = ''
      this.ruleForm2.select_5 = ''
      getClassInfo({
        api: 'admin.goods.getClassInfo',
        pageSize: 100,
        classId: newVal,
        type: 1
      }).then(res => {
        this.options3 = res.data.data.classInfo
      })
    },

    'ruleForm2.select_4' (newVal) {
      console.log(newVal)
      this.ruleForm2.select_5 = ''
      getClassInfo({
        api: 'admin.goods.getClassInfo',
        pageSize: 100,
        classId: newVal,
        type: 1
      }).then(res => {
        this.options4 = res.data.data.classInfo
      })
    }
  },

  computed: {
    filterTable () {
      let arr = []
      if (this.attrTitle.length > 0) {
        this.attrTitle?.filter((items, indexs) => {
          arr.push([])
          items.attr_list.filter(item => {
            arr[indexs].push({
              text: item.attr_name,
              value: item.attr_name
            })
          })
        })
      }
      return arr
    },

    uploadData () {
      {
        return {
          api: 'resources.file.uploadFiles',
          storeType: 8,
          storeId: getStorage('laike_admin_userInfo').storeId,
          groupId: -1,
          uploadType: 2,
          accessId: this.$store.getters.token
        }
      }
    }
  },

  methods: {
    // 添加至草稿箱
    async addDraftsBox(){
      let attrList =[]
      let strList =[]
      if (this.strArr.length <= 0) {
          strList = [];
        } else {
          strList = this.strArr;
        }
        // 如果没有 规格信息就给一个空数组
        if (this.attrTitle.length <= 0) {
          attrList = [ ];
        } else {
          attrList = this.attrTitle;
        }
        // 商品介绍
        for (let i = 0; i < this.IntroList.length; i++) {
          if (this.IntroList[i].content === "") {
            this.$message({
              message:
                this.$t("releasephysical.bta") +
                this.IntroList[i].name +
                this.$t("releasephysical.btb"),
              type: "error",
              offset: 102
            });
            this.fangdou = true;
            return;
          }
        }
      this.ruleForm.content = JSON.stringify(this.IntroList);
      let mchIds
            if (this.writeOffMchIds.length > 0) {
              mchIds = this.writeOffMchIds.join(',')
            } else {
              mchIds = 0
            }
      const data = {
        api:'admin.Drafts.add',
        id: this.$route.query.editId,// 草稿箱ID
        text:JSON.stringify({
              commodityType: 1, //商品类型 0.实物商品 1.虚拟商品
              writeOffSettings: this.writeOffSettings, //核销设置 1.线下核销 2.无需核销
              writeOffMchIds: mchIds, //核销门店id  0全部门店,  1,2,3使用逗号分割
              isAppointment: this.isAppointment, //预约时间设置 1.无需预约下单 2.需要预约下单
              pId:
                this.$route.query.name && this.$route.query.name !== 'copy'
                  ? this.$route.query.id
                  : null,
              mch_id: getStorage('laike_admin_userInfo').mchId,

              // 基本信息
              ...this.ruleForm,
              productClassId:
                this.ruleForm.goodsClass[this.ruleForm.goodsClass.length - 1],
              // 商品属性
              initial: {
                cbj:this.ruleForm.cbj,
                yj:this.ruleForm.yj,
                sj:this.ruleForm.sj,
                unit:this.ruleForm.unit,
                attrImg:this.ruleForm.attrImg
              },
              brandId: this.ruleForm.goodsBrand,
              showImg:this.ruleForm.goodsShow[0],

              active: parseInt(this.ruleForm.checkedActivity),
              displayPosition: this.ruleForm.checked
                ? this.ruleForm.checked
                : '3',
              videoList: this.videoList,
              videoList2: this.videoList2,
              IntroList: this.IntroList,
              strArr: this.strArr,
              attrTitle: this.attrTitle,
              })

      }
      const res =  await addDrafts(data)
      return res

    },
    leave(){
      let falg = false
      const statsNone = ['checkedActivity','freight']
      if( !['上架','下架','待上架'].includes(this.$route.query.status)){
        for(let key in this.ruleForm){
          // 根据 input 输入框 判断是否需要保存草稿箱
          if(typeof this.ruleForm[key] != 'object' && this.ruleForm[key] && !statsNone.includes(key)){
            falg = true
          }
        }
      }
      this.flag = true
      if(!falg){
        this.$router.go(-1)
      }else{

        // 对象没有任何改变时
        this.$confirm(
          this.$t('releasephysical.ckg'),
          this.$t('coupons.ts'),
          {
            confirmButtonText: this.$t('coupons.okk'),
            cancelButtonText: this.$t('coupons.ccel'),
            type: 'warning',
            closeOnClickModal:false
          }
        )
          .then( async () => {
            const res = await this.addDraftsBox()
            console.log(11111,res)
            if(res.data.code == 200){
              this.$message({
	                message: this.$t("zdata.czcg"),
	                type: "success",
	                offset: 102,
	              });
                setTimeout(()=>{
                  this.$router.go(-1)
                },1500)
            } else{
              this.$message({
	                message:res.data.message,
	                type: "error",
	                offset: 102,
	              });
            }
          })
          .catch((e) => {
            this.$router.go(-1)
          })
      }
    },
    queryDraftsGoodsInfo(){
      const data = {
        api: 'admin.Drafts.edit_page',
        id: this.$route.query.editId// 草稿箱ID
      }
      queryDraftsInfo(data).then(({data:res})=>{
      if(res.code == 200){
        const data = JSON.parse( res.data.text)
        this.ruleFormCope = JSON.stringify(this.ruleForm)
        this.ruleForm = data
        this.strArr = data.strArr
        this.attrTitle = data.attrTitle
        this.videoList2 = data.videoList2
        this.videoList = data.videoList
        this.IntroList = data.IntroList
        console.log('草稿箱', data.IntroList,this.ruleForm)
        // 品牌分类
        if(this.ruleForm.goodsClass){
            this.classIds = this.ruleForm.goodsClass
            this.classList.splice(0,this.classList.length)
            this.choiceClasss().then(() => {
              if (this.classIds.length>0) {
                this.allClass(this.classList);
              }
            });
            choiceClass({
              api: "admin.goods.choiceClass",
              classId: this.ruleForm.goodsClass[this.ruleForm.goodsClass.length-1],
            }).then((res) => {
              this.brandList = res.data.data.list.brand_list.filter(item => Number(item.notset) !== 1);;
            });
          }
      }
  })

},
    //切换核销设置
    writeChange () {
      this.isAppointment = 1
      this.writeOffMchIds = []
    },
    handleSubmit (arrList, zcList) {
      this.strArr = []
      this.attrKey = []
      this.dataSource = []
      console.log('子组件传过来的处理好的数据直接用', arrList)
      console.log('子组件传过来的数组对象(处理key)', zcList)
      this.attrTitle = arrList
      this.dataSource = zcList
      zcList.forEach(row => {
        this.attrKey.push(Number(row.sid))
      })
      console.log('attrKey', this.attrKey)
      var listX = 0
      for (var i = 0; i < this.attrTitle.length; i++) {
        var attr_list = this.attrTitle[i].attr_list
        if (listX == 0) {
          listX = attr_list.length
        } else {
          listX = attr_list.length > 0 ? attr_list.length * listX : listX
        }
      }
      for (var i = 0; i < listX; i++) {
        //没有数据的时候新增商品属性值
        this.strArr.push({
          cbj: this.ruleForm.cbj,
          yj: this.ruleForm.yj,
          sj: this.ruleForm.sj,
          unit: this.ruleForm.unit,
          // kucun: this.ruleForm.kucun,
          img: '', //图片
          bar_code: '', // 条形码
          attr_list: [],
          cid: ''
        })
        //如果strarr原本有数据
      }
      this.listX = listX
      this.recursion(this.attrTitle, 0, listX)
      console.log('this.strArr', this.strArr)
    },
    recursion (th_title, i, _listX) {
      // 如果该循环的子项没有东西则停止递归
      if (!th_title[i]) {
        if (i < th_title.length - 1) {
          th_title.splice(i, 1)
          this.recursion(th_title, i, _listX)
          return
        }
        return
      }

      // 如果该项属性的没有属性值，则删除该项重新递归
      if (th_title[i].attr_list.length == 0) {
        th_title.splice(i, 1)
        this.recursion(th_title, i, _listX)
        return
      }
      var xx = 0
      if (i == 0) {
        // 第一个规格属性的格式是白色白色白色,黑色黑色黑色
        for (var j = 0; j < th_title[i].attr_list.length; j++) {
          var value = th_title[i].attr_list[j].attr_name
          for (var x = 0; x < this.listX / th_title[i].attr_list.length; x++) {
            var name = th_title[i].attr_group_name
            this.strArr[xx].attr_list.push({
              attr_id: '',
              attr_name: value,
              attr_group_name: name
            })
            xx++
          }
        }
      } else if (i < th_title.length - 1) {
        _listX = Math.round(_listX / th_title[i - 1].attr_list.length)
        // 外面这层循环代表当前属性在内循环完成之后进入新的循环,比如白色白色黑色黑色红色红色,完成之后再次白色白色黑色黑色红色红色循环,总行数除以前一个属性每个属性有多少行,得出总循环数
        for (var l = 0; l < this.listX / _listX; l++) {
          for (var j = 0; j < th_title[i].attr_list.length; j++) {
            var value = th_title[i].attr_list[j].attr_name
            // 当前规格的前一个每个属性行数,除当前
            for (var x = 0; x < _listX / th_title[i].attr_list.length; x++) {
              var name = th_title[i].attr_group_name
              this.strArr[xx].attr_list.push({
                attr_id: '',
                attr_name: value,
                attr_group_name: name
              })
              xx++
            }
          }
        }
      } else {
        // 后面的规格属性格式是x,l,xl x,l,xl循环
        for (var x = 0; x < this.listX / th_title[i].attr_list.length; x++) {
          for (var j = 0; j < th_title[i].attr_list.length; j++) {
            var value = th_title[i].attr_list[j].attr_name
            var name = th_title[i].attr_group_name
            this.strArr[xx].attr_list.push({
              attr_id: '',
              attr_name: value,
              attr_group_name: name
            })
            xx++
          }
        }
      }
      i++
      if (i < th_title.length) {
        this.recursion(th_title, i, _listX)
      }
    },
    addIntro () {
      this.$nextTick(() => {
        this.$refs['ruleForm7'].clearValidate()
      })
      this.dialogVisible = true
    },
    handleClose7 () {
      this.$nextTick(() => {
        this.$refs['ruleForm7'].clearValidate()
      })
      this.ruleForm7.name = ''
      this.dialogVisible = false
    },
    delIntro (index) {
      this.IntroList.splice(index, 1)
    },
    //添加介绍
    addIntroduction (formName7) {
      this.$refs[formName7].validate(valid => {
        if (valid) {
          let data = {
            name: this.ruleForm7.name,
            content: ''
          }
          this.IntroList.push(data)
          this.handleClose7()
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    beforeAvatarUpload (file) {
      return new Promise((resolve, reject) => {
        if (
          [
            'video/mp4',
            'video/mov',
            'video/flv',
            'video/avi',
            'video/wmv',
            'video/rmvb',
            'video/webm'
          ].indexOf(file.raw.type) == -1
        ) {
          return this.warnMsg(this.$t('releasephysical.qsczcdspgs'))
        }

        if (file.size / 1024 / 1024 > 100) {
          return this.warnMsg(this.$t('releasephysical.spdxbncg'))
        }
        resolve()
      })
    },
    // 上传改变
    async handleAvatarChange (file, fileList) {
      try {
        await this.beforeAvatarUpload(file)
        this.uploadImgApi(file)
      } catch (e) {
        this.warnMsg(JSON.stringify(e))
      }
    },
    // 上传改变
    async handleAvatarChange2 (file, fileList) {
      try {
        await this.beforeAvatarUpload(file)
        this.uploadImgApi2(file)
      } catch (e) {
        this.warnMsg(JSON.stringify(e))
      }
    },
    // 上传视频准备
    uploadImgApi (file) {
      console.log('file', file)
      this.videoLoading = true
      const videoSrc = URL.createObjectURL(file.raw)
      var formData = new FormData()
      formData.append('file', file.raw) //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl, //上传路径
        method: 'POST',
        params: {
          api: 'resources.file.uploadFiles',
          storeType: 8,
          storeId: getStorage('laike_admin_userInfo').storeId,
          groupId: -1,
          uploadType: 2,
          accessId: this.$store.getters.token
        },
        data: formData
      })
        .then(res => {
          this.videoList.push({
            videoFile: file,
            url: videoSrc,
            isShowPopup: false
          })
          this.videoUp = res.data.data.imgUrls[0]
          console.log('videoUp', this.videoUp)
          setTimeout(() => {
            this.videoLoading = false
          }, 1000)
        })
        .catch(err => {
          console.log(err)
        })
    },
    // 上传视频准备
    uploadImgApi2 (file) {
      console.log('file', file)
      this.videoLoading2 = true
      const videoSrc = URL.createObjectURL(file.raw)
      var formData = new FormData()
      formData.append('file', file.raw) //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl, //上传路径
        method: 'POST',
        params: {
          api: 'resources.file.uploadFiles',
          storeType: 8,
          storeId: getStorage('laike_admin_userInfo').storeId,
          groupId: -1,
          uploadType: 2,
          accessId: this.$store.getters.token
        },
        data: formData
      })
        .then(res => {
          this.videoList2.push({
            videoFile: file,
            url: videoSrc,
            isShowPopup: false
          })
          this.videoUp2 = res.data.data.imgUrls[0]
          setTimeout(() => {
            this.videoLoading2 = false
          }, 1000)
        })
        .catch(err => {
          console.log(err)
        })
    },
    // 预览视频
    previewVideo (data) {
      this.videoSrc = data.url
      this.dialogVisible_video = true
    },
    // 预览视频
    previewVideo2 (data) {
      this.videoSrc2 = data.url
      this.dialogVisible_video2 = true
    },
    // 删除视频
    deleteVideo (index) {
      this.videoList.splice(index, 1)
      this.videoUp = ''
    },
    // 删除视频
    deleteVideo2 (index) {
      this.videoList2.splice(index, 1)
      this.videoUp2 = ''
    },
    handleClose () {
      const video = document.getElementsByTagName('video')[1]
      if (!video.paused) {
        video.currentTime = 0
        video.pause()
      }
      this.dialogVisible_video = false
    },
    handleClose5 () {
      const video = document.getElementsByTagName('video')[1]
      if (!video.paused) {
        video.currentTime = 0
        video.pause()
      }
      this.dialogVisible_video2 = false
    },
    unique (arr) {
      return [...new Set(arr.map(item => JSON.stringify(item)))].map(val =>
        JSON.parse(val)
      )
    },

    oninput2 (num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '')
      str = str.replace('.', '')

      return str
    },
    getBase () {
      this.goodsEditorBase = process.env.VUE_APP_BASE_API
    },

    async getDictionaryList(language) {

      if(!language){
        language = this.LaiKeCommon.getUserLangVal();
      }

      const res = await dictionaryList({
        api: "saas.dic.getDictionaryInfo",
        key: "商品展示位置",
        lang_code:language,
        status: 1,
      });

      this.showAdrList = res.data.data.list;
      console.log("显示位置", this.showAdrList);
    },

    // 加载所有分类
    async allClass (value) {
      for (let i = 0; i < value.length; i++) {
        if (this.classIds.includes(value[i].value)) {
          choiceClass({
            api: 'admin.goods.choiceClass',
            classId: value[i].value
          }).then(res => {
            if (res.data.data.list?.class_list.length !== 0) {
              res.data.data.list?.class_list[0].forEach((item, index) => {
                let obj = item
                value[i].children.push({
                  value: obj.cid,
                  label: obj.pname,
                  index: index,
                  children: []
                })
              })
              this.allClass(value[i].children)
            }
          })
        } else {
          continue
        }
      }
    },

    filterHandler (value, row) {
      let flag = row.attr_list.some(item => {
        return item.attr_name == value
      })
      return flag
    },

    clickImage () {
      this.attrNum = 1
      $('.el-table__body-wrapper').css('height', '1000000')
      setTimeout(() => {
        $(document)
          .off()
          .on('click', function () {
            $('.el-table__body-wrapper').css('height', '265px')
          })
      }, 1000)
    },

    // 获取商品类别
    async choiceClasss(language) {

      const res = await choiceClass({
        api: "admin.goods.choiceClass",
        lang_code: language
      });

      const rawList = res.data.data.list.class_list[0] || [];

      const seen = new Set();
      this.classList = rawList
        .filter(item => {
          if (item.notset === 1) return false;
          if (seen.has(item.cid)) return false;
          seen.add(item.cid);
          return true;
        })
        .map((item, index) => ({
          value: item.cid,
          label: item.pname,
          index: index,
          level: item.level,
          notset: item.notset,
          children: []
        }));

    },

    // 根据商品类别id获取商品品牌
    changeProvinceCity (value) {
      if (!value) {
        return
      }
      this.ruleForm.goodsBrand = ''
      this.brandList = []
      choiceClass({
        api: 'admin.goods.choiceClass',
        classId: value?.length > 1 ? value[value?.length - 1] : value[0]
      }).then(res => {
        this.brandList = res.data.data.list.brand_list.filter(item => Number(item.notset) !== 1);
        if (res.data.data.list?.class_list[0].length !== 0) {
          this.$refs.myCascader.getCheckedNodes()[0].data.children = []

          res.data.data.list?.class_list[0].forEach((item, index) => {
            let obj = item
            if( obj.notset == 0) {
              this.$refs.myCascader.getCheckedNodes()[0].data.children.push({
                value: obj.cid,
                label: obj.pname,
                index: index,
                children: []
              })
            }
          })
        }
      })
    },

    // 获取支持活动类型
    async getGoodsActiveLists () {
      const res = await getGoodsActiveList({
        api: 'admin.goods.getGoodsActive'
      })
      this.activityList = res.data.data.filter(item => {
        if (item.status) {
          return item
        }
      })
    },

    // 获取商品标签
    async labels(language ) {
      if(!language){
        language = this.LaiKeCommon.getUserLangVal();
      }

      const res = await label({
        api: "admin.label.index",
        lang_code:language,
        pageSize: 9999,
      });

      this.labelList = res.data.data.list;
    },

    launchContent () {
      this.isContent = !this.isContent
    },
    launchShow () {
      this.isShow = !this.isShow
    },
    // 获取商品单位
    async goodsUnits(language) {

      if(!language){
        language = this.LaiKeCommon.getUserLangVal();
      }

      const res = await goodsUnit({
        api: "saas.dic.getDictionaryInfo",
        pageSize: 9999,
        key: "单位",
        status: 1,
        lang_code:language,
      });
      this.unitList = res.data.data.list;
    },
    // 获取店铺门店列表
    async getStore () {
      const res = await getStoreList({
        api: 'admin.addGoods.getMchStore',
        mchId: this.mchSid ?? ''
      })
      this.storeList = res.data.data.list
    },

    // 获取运费列表
    async getFreightInfos(language) {

      if(!language){
        language = this.LaiKeCommon.getUserLangVal();
      }

      const res = await getFreightInfo({
        api: "admin.goods.getFreightInfo",
        pageSize: 999,
        lang_code:language,
        mchId: this.mchid
          ? this.mchid
          : getStorage("laike_admin_userInfo").mchId,
      });
      this.freightList = res.data.data.list;
      if (!this.ruleForm.freight) {
        this.ruleForm.freight = this.freightList[0].id;
      }
    },

    // 商品详情
    handleImageAdded (file, Editor, cursorLocation, resetUploader) {
      console.log('file', file)

      var formData = new FormData()
      formData.append('file', file) //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl, //上传路径
        method: 'POST',
        params: {
          api: 'resources.file.uploadFiles',
          storeType: 8,
          storeId: getStorage('laike_admin_userInfo').storeId,
          groupId: -1,
          uploadType: 2,
          accessId: this.$store.getters.token
        },
        data: formData
      })
        .then(result => {
          let url = result.data.data.imgUrls[0] // 返回给你的图片路径
          Editor.insertEmbed(cursorLocation+1, 'image', url)
          // Editor.setSelection(length + 1)
          resetUploader()
        })
        .catch(err => {
          console.log(err)
        })
    },

    // 添加分类弹框方法 //
    async getClassInfoss (language) {
      if(!language){
        language = this.LaiKeCommon.getUserLangVal();
      }

      const res = await getClassInfo({
        api: "admin.goods.getClassInfo",
        lang_code:language,
        pageSize: 100,
      });
      console.log(res);
      this.options1 = res.data.data.classInfo;
    },

    dialogShow1 () {
      this.dialogVisible1 = true
      this.$refs.classUpload.fileList = []
      this.ruleForm2.classname = ''
      this.ruleForm2.subtitle = ''
      this.ruleForm2.classlevel = ''
      this.ruleForm2.classLogo = ''
      this.$refs['ruleForm2'].clearValidate()
    },
    addIntro () {
      this.$nextTick(() => {
        this.$refs['ruleForm7'].clearValidate()
      })
      this.dialogVisible = true
    },
    handleClose7 () {
      this.$nextTick(() => {
        this.$refs['ruleForm7'].clearValidate()
      })
      this.ruleForm7.name = ''
      this.dialogVisible = false
    },
    handleClose1 (done) {
      this.dialogVisible1 = false
      this.ruleForm2.classname = ''
      this.ruleForm2.subtitle = ''
      this.ruleForm2.classlevel = ''
      this.ruleForm2.classLogo = ''
      this.$refs['ruleForm2'].clearValidate()
    },

    handleAvatarSuccess (res, file) {
      console.log(res)
      this.ruleForm2.classLogo = res.data.imgUrls[0]
    },

    submitForm2 (formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm2)
        if (valid) {
          try {
            if (this.inputShow === 1) {
              addClass({
                api: 'admin.goods.addClass',
                className: this.ruleForm2.classname,
                ename: this.ruleForm2.subtitle,
                level: 0,
                fatherId: 0,
                img: this.ruleForm2.classLogo
              }).then(res => {
                if (res.data.code == '200') {
                  console.log(res)
                  this.$message({
                    message: this.$t('releasephysical.fbcg'),
                    type: 'success',
                    offset: 102
                  })
                  this.dialogVisible1 = false
                  this.classList = []
                  this.getClassInfoss()
                  this.choiceClasss().then(() => {
                    if (this.$route.query.id) {
                      this.allClass(this.classList)
                    }
                  })
                  this.getClassInfos()
                  // this.choiceClasss()
                }
              })
            } else {
              addClass({
                api: 'admin.goods.addClass',
                className: this.ruleForm2.classname,
                ename: this.ruleForm2.subtitle,
                level: this.inputShow - 1,
                fatherId: this.getfatherMenuId(this.inputShow),
                img: this.ruleForm2.classLogo
              }).then(res => {
                if (res.data.code == '200') {
                  console.log(res)
                  this.$message({
                    message: this.$t('releasephysical.fbcg'),
                    type: 'success',
                    offset: 102
                  })
                  this.ruleForm2.classname = ''
                  this.ruleForm2.subtitle = ''
                  this.ruleForm2.classlevel = ''
                  this.ruleForm2.classLogo = ''
                  this.classList = []
                  this.handleClose1()
                  this.choiceClasss()
                  this.getClassInfos()
                }
              })
            }
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },

    getType (value) {
      if (value === '商城') {
        return 0
      } else {
        return 1
      }
    },

    getfatherMenuId (value) {
      if (this.inputShow === 2) {
        return this.ruleForm2.select_2
      } else if (this.inputShow === 3) {
        if (this.ruleForm2.select_3 === '') {
          return this.ruleForm2.select_2
        } else {
          return this.ruleForm2.select_3
        }
      } else if (this.inputShow === 4) {
        if (this.ruleForm2.select_4 === '') {
          return this.ruleForm2.select_3
        } else if (this.ruleForm2.select_3 === '') {
          return this.ruleForm2.select_2
        } else {
          return this.ruleForm2.select_4
        }
      } else {
        if (this.ruleForm2.select_5 === '') {
          return this.ruleForm2.select_4
        } else if (this.ruleForm2.select_4 === '') {
          return this.ruleForm2.select_3
        } else if (this.ruleForm2.select_3 === '') {
          return this.ruleForm2.select_2
        } else {
          return this.ruleForm2.select_5
        }
      }
    },

    // 添加品牌弹框方法 //
    dialogShow2 () {
      this.dialogVisible2 = true
      this.ruleForm3.brandname = ''
      this.ruleForm3.brandLogo = ''
      this.ruleForm3.brandtype = ''
      this.ruleForm3.countries = ''
      this.ruleForm3.note = ''
      this.$nextTick(() => {
        this.$refs['ruleForm3'].clearValidate()
      })
    },

    handleClose2 () {
      this.dialogVisible2 = false
      this.ruleForm3.brandname = ''
      this.ruleForm3.brandLogo = ''
      this.ruleForm3.brandtype = ''
      this.ruleForm3.countries = ''
      this.ruleForm3.note = ''
      this.$refs.upload3.fileList = []
      this.$nextTick(() => {
        this.$refs['ruleForm3'].clearValidate()
      })
    },

    handleAvatarSuccess1 (res, file) {
      console.log(res)
      this.ruleForm3.brandLogo = res.data.imgUrls[0]
    },

    // 获取所属分类
    async getClassInfos () {
      const res = await getClassInfo({
        api: 'admin.goods.getClassInfo'
      })
      console.log(res)
      this.brandTypeList = res.data.data.classInfo
    },

    // 获取国家列表
    async getCountrys () {
      const res = await getCountry({
        api: 'admin.goods.getCountry'
      })

      console.log(res)
      this.countriesList = res.data.data
    },

    // 获取语种列表
    async getLanguage() {
      const result = await this.LaiKeCommon.getLanguages();
      this.languages = result.data.data;
    },

    getId (value) {
      this.id = value
    },

    getIds (value) {
      this.countriesId = value
      this.ruleForm3.country_num = value;
    },

    submitForm3 (formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm3)
        if (valid) {
          try {
            if (!this.brandType) {
              return
            }
            let that = this
            this.brandType = false
            setTimeout(function () {
              that.brandType = true
            }, 3000)
            addBrand({
              api: 'admin.goods.addBrand',
              brandName: this.ruleForm3.brandname,
              brandLogo: this.ruleForm3.brandLogo,
              brandClass: this.ruleForm3.brandtype.join(','),
              producer: this.countriesId,
              country_num: this.ruleForm3.country_num,
              lang_code: this.ruleForm3.lang_code,
              remarks: this.ruleForm3.note
            }).then(res => {
              console.log(res)
              if (res.data.code === '413') {
                this.$message({
                  message: res.data.message,
                  type: 'error',
                  offset: 102
                })
              } else if (res.data.code == '200') {
                this.$message({
                  message: this.$t('releasephysical.fbcg'),
                  type: 'success',
                  offset: 102
                })
                this.handleClose2()
                if (
                  this.ruleForm.goodsClass &&
                  this.ruleForm.goodsClass.length
                ) {
                  choiceClass({
                    api: 'admin.goods.choiceClass',
                    classId:
                      this.ruleForm.goodsClass[
                        this.ruleForm.goodsClass.length - 1
                      ]
                  }).then(res => {
                    this.brandList = res.data.data.list.brand_list.filter(item => Number(item.notset) !== 1);
                  })
                }
              }
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },

    // 添加属性弹框方法 //

    /* 添加属性 */
    addAttr () {
      if (
        !(
          (
            this.ruleForm.cbj &&
            this.ruleForm.yj &&
            this.ruleForm.sj &&
            this.ruleForm.unit != 0
          )
          // this.ruleForm.kucun
        )
      ) {
        this.errorMsg(this.$t('releasephysical.text13'))
        return
      }
      this.transferDialog = true
    },

    arrConcat (arr1, arr2) {
      return arr1.concat(arr2)
    },

    // 添加运费弹框方法 //
    dialogShow4 () {
      this.ruleForm5.templateName = ''
      this.tableData = []
      this.dialogVisible3 = true
    },

    handleClose3 (done) {
      this.dialogVisible3 = false
      this.$refs['ruleForm5'].clearValidate()
    },

    Delete (value, index) {
      this.cities = this.cities.concat(value.name.split(','))
      this.tableData.splice(index, 1)
    },

    // 添加运费
    submitForm4 (formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm4)
        if (valid) {
          try {
            if (!this.freightType) {
              return
            }
            let that = this
            this.freightType = false
            setTimeout(function () {
              that.freightType = true
            }, 500)
            addFreight({
              api: 'admin.goods.addFreight',
              name: this.ruleForm5.templateName,
              hiddenFreight: encodeURIComponent(JSON.stringify(this.tableData))
            }).then(res => {
              if (res.data.code == '200') {
                console.log(res)
                this.$message({
                  message: this.$t('physicalgoods.cg'),
                  type: 'success',
                  offset: 102
                })
                this.getFreightInfos()
                this.dialogVisible3 = false
              }
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },

    // 添加运费规则弹框方法
    dialogShow5 () {
      this.isIndeterminate = false
      this.checkedCities = []
      this.ruleForm6.freight = null
      if (this.cities.length == 0 && this.tableData.length !== 0) {
        this.$message({
          message: '添加失败，地区运费规则已全部设置!',
          type: 'error',
          offset: 102
        })
      } else {
        this.dialogVisible4 = true
      }

      this.checkAll = false
      if (this.tableData.length === 0) {
        this.getRegions()
      }
    },

    handleClose4 (done) {
      this.dialogVisible4 = false
      this.$refs['ruleForm6'].clearValidate()
    },

    // 获取城市列表
    async getRegions () {
      const res = await getRegion({
        api: 'admin.goods.getRegion',
        level: 2
      })
      console.log(res)
      this.cities = res.data.data.map(item => {
        return item.districtName
      })
    },

    handleCheckAllChange (val) {
      this.checkedCities = val ? cityOptions : []
      this.isIndeterminate = false
    },
    handleCheckedCitiesChange (value) {
      let checkedCount = value.length
      this.checkAll = checkedCount === this.cities.length
      this.isIndeterminate =
        checkedCount > 0 && checkedCount < this.cities.length
    },

    determine (formName) {
      this.$refs[formName].validate(async valid => {
        if (this.checkedCities.length == 0) {
          this.$message({
            message: '请选择省份',
            type: 'error',
            offset: 102
          })
        } else {
          if (valid) {
            try {
              var obj = {
                one: this.ruleForm6.freight,
                name: this.checkedCities.join(),
                freight: this.ruleForm6.freight
              }
              this.tableData.push(obj)
              console.log(this.tableData)
              this.cities = this.cities.filter(item => {
                if (!this.checkedCities.includes(item)) {
                  return item
                }
              })
              this.dialogVisible4 = false
            } catch (error) {
              this.$message({
                message: '请输入运费',
                type: 'error',
                offset: 102
              })
            }
          } else {
            this.$message({
              message: '请输入运费',
              type: 'error',
              offset: 102
            })
            // return false;
          }
        }
      })
    },

    // 发布商品
    submitForm (formName) {
      this.$validateAndScroll(formName).then( async (valid) => {

        if (valid) {
          try {
            if (this.ruleForm.goodsTitle.length > 20) {
              this.$message({
                message: this.$t('releasephysical.text1'),
                type: 'error',
                offset: 102
              })
              return
            }
            if (this.ruleForm.keywords.length > 20) {
              this.$message({
                message: this.$t('releasephysical.text2'),
                type: 'error',
                offset: 102
              })
              return
            }
            if (this.ruleForm.subtitle.length > 20) {
              this.$message({
                message: this.$t('releasephysical.text3'),
                type: 'error',
                offset: 102
              })
              return
            }
            if (Number(this.ruleForm.sj) == 0) {
              this.$message({
                message: this.$t('releasephysical.text4'),
                type: 'error',
                offset: 102
              })
              return
            }
            let isImg = false
            this.strArr?.filter(item => {
              if (!item.img) {
                isImg = true
              }
            })
            if (isImg) {
              this.$message({
                message: this.$t('releasephysical.text5'),
                type: 'error',
                offset: 102
              })
              return
            }
            for (let i = 0; i < this.IntroList.length; i++) {
              if (this.IntroList[i].content === '') {
                this.$message({
                  message:
                    this.$t('releasephysical.bt') +
                    this.IntroList[i].name +
                    this.$t('releasephysical.text6'),
                  type: 'error',
                  offset: 102
                })
                return
              }
            }
            if (this.videoLoading == true) {
              this.$message({
                message: this.$t('releasephysical.text7'),
                type: 'error',
                offset: 102
              })
              return
            }
            if (this.videoLoading2 == true) {
              this.$message({
                message: this.$t('releasephysical.text8'),
                type: 'error',
                offset: 102
              })
              return
            }
            this.ruleForm.content = JSON.stringify(this.IntroList)
            // this.strArr = [{"cbj":"","yj":"","sj":"","unit":"张","kucun":"","img":"","bar_code":"","attr_list":[{"attr_id":"","attr_name":"默认","attr_group_name":"尺码"}],"cid":""}],
            // this.attrTitle = [{"attr_group_name":"尺码","attr_list":[{"attr_name":"默认"}]}]
            var strList = []
            var attrList = []
            if (this.strArr.length <= 0) {
              strList = [
                {
                  cbj: this.ruleForm.cbj,
                  yj: this.ruleForm.yj,
                  sj: this.ruleForm.sj,
                  unit: this.ruleForm.unit,
                  kucun: 1,
                  img: this.ruleForm.attrImg,
                  bar_code: '',
                  attr_list: [
                    { attr_id: '', attr_name: '默认', attr_group_name: '默认' }
                  ],
                  cid: ''
                }
              ]
            } else {
              strList = this.strArr
            }
            if (this.attrTitle.length <= 0) {
              attrList = [
                { attr_group_name: '默认', attr_list: [{ attr_name: '默认' }] }
              ]
            } else {
              attrList = this.attrTitle
            }
            let mchIds
            if (this.writeOffMchIds.length > 0) {
              mchIds = this.writeOffMchIds.join(',')
            } else {
              mchIds = 0
            }
            addProduct({
              api: 'admin.goods.addGoods',
              commodityType: 1, //商品类型 0.实物商品 1.虚拟商品
              writeOffSettings: this.writeOffSettings, //核销设置 1.线下核销 2.无需核销
              writeOffMchIds: mchIds, //核销门店id  0全部门店,  1,2,3使用逗号分割
              isAppointment: this.isAppointment, //预约时间设置 1.无需预约下单 2.需要预约下单
              pId:
                this.$route.query.name && this.$route.query.name !== 'copy'
                  ? this.$route.query.id
                  : null,
              draftsId:this.$route.query.editId || null,//草稿箱id
              mch_id: getStorage('laike_admin_userInfo').mchId,

              // 基本信息
              productTitle: this.ruleForm.goodsTitle,
              country_num: this.ruleForm.country_num,
              lang_code: this.ruleForm.lang_code,
              subtitle: this.ruleForm.subtitle,
              keyword: this.ruleForm.keywords,
              productClassId:
                this.ruleForm.goodsClass[this.ruleForm.goodsClass.length - 1],
              brandId: this.ruleForm.goodsBrand,
              // weight: this.ruleForm.weight,
              coverMap: this.ruleForm.goodsCover,
              showImg: this.ruleForm.goodsShow.join(','),
              // 商品属性
              initial: `cbj=${this.ruleForm.cbj},yj=${this.ruleForm.yj},sj=${this.ruleForm.sj},unit=${this.ruleForm.unit},attrImg=${this.ruleForm.attrImg}`,
              attrGroup: JSON.stringify(attrList),
              attrArr: JSON.stringify(strList),
              // 商品设置
              // stockWarn: this.ruleForm.inventoryWarning,
              // freightId: this.ruleForm.freight,
              sType: null,
              active: parseInt(this.ruleForm.checkedActivity),
              displayPosition: this.ruleForm.checked
                ? this.ruleForm.checked
                : '3',
              volume: this.ruleForm.virtualSales,
              sort: this.ruleForm.sequence,
              // 产品内容
              content: this.ruleForm.content,
              unit: this.ruleForm.unit,
              video: this.videoUp,
              proVideo: this.videoUp2,
              strArr: JSON.stringify(this.dataSource)
            }).then(res => {
              if (res.data.code == '200') {
                this.flag = true
                if (
                  this.$route.query.id &&
                  this.$route.query.name == 'editor'
                ) {
                  this.$message({
                    message: this.$t('zdata.bjcg'),
                    type: 'success',
                    offset: 102
                  })
                } else if (
                  this.$route.query.id &&
                  this.$route.query.name == 'copy'
                ) {
                  this.$message({
                    message: this.$t('zdata.fzcg'),
                    type: 'success',
                    offset: 102
                  })
                } else {
                  this.$message({
                    message: this.$t('releasephysical.fbcg'),
                    type: 'success',
                    offset: 102
                  })
                }
                this.$router.push('/goods/goodslist/virtualgoods')
              }
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              offset: 102
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    delIntro (index) {
      this.IntroList.splice(index, 1)
    },
    //添加介绍
    addIntroduction (formName7) {
      this.$refs[formName7].validate(valid => {
        if (valid) {
          let data = {
            name: this.ruleForm7.name,
            content: ''
          }
          this.IntroList.push(data)
          this.handleClose7()
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
</script>

<style scoped lang="less">
.upload-box {
  .avatar-uploader-box {
    position: relative;
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    padding-bottom: 20px;
    min-width: 350px;
    .avatar-uploader {
      margin-right: 10px;
      .el-upload {
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        &:hover {
          border-color: #409eff;
        }
      }
      .avatar-uploader-icon {
        font-size: 1.25rem;
        color: #8c939d;
        width: 80px;
        height: 80px;
        text-align: center;
        border: 0.0625rem dashed #c0ccda;
        border-radius: 0.25rem;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      .avatar-uploader-icon:hover {
        border: 0.0625rem dashed #409eef;
        cursor: pointer;
      }
    }
    .video-preview {
      position: relative;
      padding-right: 20px;
      .avatar {
        width: 80px;
        height: 80px;
        display: block;
        border-radius: 6px;
      }
      .avatar-uploader-popup {
        position: absolute;
        top: 0;
        left: 0;
        z-index: 2;
        width: 80px;
        height: 80px;
        background-color: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: space-around;
        align-items: center;
        color: #fff;
        font-size: 1.25rem;
        border-radius: 6px;
        transition: 3s;
        i {
          font-size: 1.25rem;
        }
      }
    }
    .upload-tip {
      display: flex;
      align-items: center;
      font-size: 0.875rem;
      font-weight: 400;
      color: #97a0b4;
    }
  }
}
.container {
  width: 100%;
  .opacity {
    opacity: 0.5;
  }
  /deep/.el-form {
    padding-bottom: 38px;
    .header {
      width: 100%;
      height: 60px;
      line-height: 60px;
      border-bottom: 1px solid #e9ecef;
      padding-left: 20px;
      span {
        font-weight: 400;
        font-size: 16px;
        color: #414658;
      }
    }
    .el-input {
      width: 400px;
      height: 40px;
      // input {
      //   width: 400px;
      //   height: 40px;
      // }
    }
    .composite {
      input {
        width: 345px !important;
      }
    }

    .basic-info {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;
      .show-font {
        color: #97a0b4;
        margin-left: 10px;
      }
      .basic-block {
        margin-top: 40px;
        padding: 0 20px 20px 20px;
        .goods-img {
          width: 100%;
        }

        .goods-class {
          .el-form-item__content {
            display: flex;
            .select-input {
              width: 294px !important;
              margin-right: 10px;
              .el-input {
                width: 294px !important;
                input {
                  width: 294px !important;
                }
              }
            }
            button {
              width: 96px;
              height: 38px;
              border: 1px solid #2890ff;
              border-radius: 4px;
              background-color: #fff;
              color: #2890ff;
              margin-left: 10px;
              margin: 0;
              padding: 0;
            }
          }
        }

        .goods-brand {
          .el-form-item__content {
            display: flex;
            .select-input {
              width: 294px !important;
              margin-right: 10px;
              .el-input {
                width: 294px !important;
                input {
                  width: 294px !important;
                }
              }
            }
            button {
              width: 96px;
              height: 38px;
              border: 1px solid #2890ff;
              border-radius: 4px;
              background-color: #fff;
              color: #2890ff;
              margin-left: 10px;
              margin: 0;
              padding: 0;
            }
          }
        }

        .basic-items {
          width: 100%;
          display: flex;
          justify-content: flex-start;
          .box {
            width: 580px;
            min-height: 140px;
            background: #f4f7f9;
            border-radius: 4px;
            padding: 20px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            .el-form-item {
              width: auto !important;
            }
            .font1 {
              font-size: 14px;
              color: #97a0b4;
              margin-left: 10px;
            }
            .el-select .el-tag__close.el-icon-close::before {
              color: #ffffff;
            }
          }
          .el-form-item {
            width: 33.3%;
          }
        }
      }
    }

    .goods-attribute {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;
      .attribute-block {
        margin-top: 40px;
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        padding: 0 20px 0 20px;

        .attribute-items {
          width: 100%;
          display: flex;
          justify-content: flex-start;
          .el-form-item {
            width: 33.3%;
          }
        }

        .add-attribute {
          width: 100%;
          button {
            width: 96px;
            height: 38px;
            border: 1px solid #2890ff;
            border-radius: 4px;
            background-color: #fff;
            color: #2890ff;
            margin-left: 10px;
            margin: 0;
            padding: 0;
          }
        }
      }
    }

    .goods-set {
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      border-radius: 4px;
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-right: 20px;
        .box {
          cursor: pointer;
          .font-color {
            font-weight: normal;
            font-size: 16px;
            color: #2890ff;
            margin-right: 8px;
          }
        }
      }
      .header2 {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-right: 20px;
        border-radius: 4px;
        width: 100%;
        height: 60px;
        line-height: 60px;
        padding-left: 20px;
        span {
          font-weight: 400;
          font-size: 16px;
          color: #414658;
        }
        .box {
          cursor: pointer;
          .font-color {
            font-weight: normal;
            font-size: 16px;
            color: #2890ff;
            margin-right: 8px;
          }
        }
      }
      .set-block {
        margin-top: 40px;
        padding: 0 20px 25px 20px;
        color: #414658;

        .inventory-warning {
          .el-input {
            width: 140px;
            height: 40px;
            margin: 0 8px;
            input {
              width: 140px;
              height: 40px;
            }
          }
          .grey {
            color: rgb(151, 160, 180);
            margin-left: 10px;
          }
        }

        .freight-set {
          button {
            width: 96px;
            height: 38px;
            border: 1px solid #2890ff;
            border-radius: 4px;
            background-color: #fff;
            color: #2890ff;
            margin-left: 8px !important;
            margin: 0;
            padding: 0;
          }
        }

        .activity-class {
          .el-checkbox__inner {
            border-radius: 50px;
          }
        }

        .show-local {
          .el-form-item__content {
            display: flex;
          }
          .show-font {
            color: #97a0b4;
            margin-left: 10px;
          }
        }
        .show-local2 {
          .el-form-item__content {
            display: flex;
            align-items: center;
          }
          .show-font {
            color: #97a0b4;
            margin-left: 10px;
          }
        }
      }
    }

    .detail-container {
      width: 100%;
      background-color: #fff;
      margin-bottom: 53px;
      border-radius: 4px;
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-right: 20px;
        .box {
          cursor: pointer;
          .font-color {
            font-weight: normal;
            font-size: 16px;
            color: #2890ff;
            margin-right: 8px;
          }
        }
      }
      .header2 {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-right: 20px;
        border-radius: 4px;
        width: 100%;
        height: 60px;
        line-height: 60px;
        padding-left: 20px;
        span {
          font-weight: 400;
          font-size: 16px;
          color: #414658;
        }
        .box {
          cursor: pointer;
          .font-color {
            font-weight: normal;
            font-size: 16px;
            color: #2890ff;
            margin-right: 8px;
          }
        }
      }
      .delet-box {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      .detail-block {
        margin-top: 40px;
        // padding: 0 20px 70px 20px;
        padding: 0 20px 20px 20px;
        // margin-bottom: 54px;
        .add_box {
          width: 100%;
          // height: 460px;
          padding: 10px 20px 20px 20px;
          background: #f4f7f9;
          border-radius: 4px;
          margin-bottom: 16px;
        }
        .add {
          width: 96px;
          height: 38px;
          border: 1px solid #2890ff;
          border-radius: 4px;
          background-color: #fff;
          color: #2890ff;
          margin-left: 10px;
          margin: 0;
          padding: 0;
          margin-bottom: 16px;
        }
        .el-form-item {
          // .el-form-item__content {
          //   // height: 383px;
          // }
          .quillWrapper {
            width: 100%;
            height: 385px;
            background: #ffffff;
            .quillWrapper {
              .ql-container {
                height: 343px !important;
              }
            }
            .ql-container {
              height: 343px !important;
            }
          }
        }
      }
    }

    .footer-button {
      position: fixed;
      right: 0;
      bottom: 40px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding: 15px 20px;
      border-top: 1px solid #e9ecef;
      background: #ffffff;
      width: 300%;
      z-index: 10;
      button {
        width: 70px;
        height: 40px;
        padding: 0;
      }
      .bgColor {
        margin-left: 14px;
      }
      .bgColor:hover {
        opacity: 0.8;
      }

      .fontColor {
        color: #6a7076;
        border: 1px solid #d5dbe8;
        margin-left: 14px;
      }
      .fontColor:hover {
        color: #2890ff;
        border: 1px solid #2890ff;
      }
    }
    .el-form-item__label {
      font-weight: normal;
    }
  }
  .dialog-block {
    // }
    /deep/.el-tag.el-tag--info .el-tag__close {
      color: #ffffff;
    }
    /deep/.el-input {
      width: 376px;
      height: auto;
      input {
        width: 376px;
        height: auto;
      }
    }
    // 弹框样式
    /deep/.el-dialog {
      width: 580px;
      height: 290px;
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
        padding: 60px 60px 60px 45px !important;

        .el-form {
          width: 100%;
          padding-bottom: 0 !important;

          .task-container {
            // padding: 41px 0px 0px 0px !important;
            .el-form-item {
              margin-bottom: 0 !important;
            }

            width: 100%;

            .balance,
            .integral,
            .level {
              display: flex;
              justify-content: center;

              .el-form-item__content {
                margin-left: 0px !important;
                display: flex;
                flex-direction: column;
                .el-input {
                  width: 360px;
                  height: auto;
                }

                .explain {
                  display: flex;
                  align-items: center;

                  img {
                    width: 12px;
                    height: 12px;
                    margin-right: 5px;
                  }

                  .red {
                    color: #6a7076;
                    font-size: 12px;
                  }
                }
              }
            }
          }
        }

        .el-form-item__label {
          font-weight: normal;
        }

        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          border-top: 1px solid #e9ecef;

          .el-form-item {
            padding: 0 !important;
            height: 100%;
            display: flex;
            justify-content: flex-end;
            margin-right: 17px;

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

          .qx_bt:hover {
            background-color: #fff;
            color: #2890ff;
            border: 1px solid #2890ff;
          }
        }
      }
    }
  }
  .dialog-class {
    .el-dialog__wrapper {
      display: flex;
      justify-content: center;
      align-items: center;
    }
    /deep/.el-dialog {
      width: 640px;
      margin-top: 0 !important;
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
        display: flex;
        justify-content: center;
        // padding: 40px 20px;
        .el-form-item {
          display: flex;
          .el-form-item__content {
            width: 400px;
            margin-left: 0px !important;
          }
          .select-input {
            width: 400px;
            height: 40px;
          }
        }

        .upload-img {
          margin-bottom: 38px;
        }

        .superior {
          width: 400px;
          display: flex;
          .el-select {
            flex: 1;
            &:not(:first-child) {
              margin-left: 8px;
            }
            .el-input {
              width: 100%;
              input {
                width: 100%;
              }
            }
          }
        }

        .upload-img {
          .el-form-item__content {
            display: flex;
            align-items: center;
            .avatar-uploader .el-upload {
              border: 1px dashed #d9d9d9;
              border-radius: 6px;
              cursor: pointer;
              position: relative;
              overflow: hidden;
            }
            .avatar-uploader .el-upload:hover {
              border-color: #409eff;
            }
            .avatar-uploader-icon {
              font-size: 28px;
              color: #8c939d;
              width: 80px;
              height: 80px;
              line-height: 80px;
              text-align: center;
            }
            .avatar {
              width: 80px;
              height: 80px;
              display: block;
            }
            .removeImg {
              position: absolute;
              right: 0;
              top: 0;
            }

            .text {
              margin-bottom: 10px;
              margin-left: 5px;
            }
          }
        }
        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          border-top: 1px solid #e9ecef;
          .el-form-item {
            width: 100%;
            padding: 0 !important;
            height: 100%;
            .el-form-item__content {
              height: 100%;
              width: 100%;
              line-height: 72px;
              margin: 0 !important;
              text-align: right;
              padding-right: 20px;
            }
          }
        }
      }
    }
  }

  .dialog-brand {
    .el-dialog__wrapper {
      display: flex;
      justify-content: center;
      align-items: center;
    }
    /deep/.el-dialog {
      width: 640px;
      height: 550px;
      margin-top: 0 !important;
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
        display: flex;
        justify-content: center;
        // padding: 40px 20px;
        .el-form {
          padding-bottom: 0;
        }
        .el-form-item {
          display: flex;
          .el-form-item__content {
            width: 400px;
            margin-left: 0px !important;
          }
          .select-input {
            width: 400px;
            height: 40px;
          }

          .belongClass {
            height: auto;
            .el-input {
              height: auto;
            }
            .el-tag.el-tag--info .el-tag__close {
              color: #ffffff;
              margin-top: 1px;
            }
          }
        }

        .upload-img {
          .el-form-item__content {
            display: flex;
            align-items: center;
            .avatar-uploader .el-upload {
              border: 1px dashed #d9d9d9;
              border-radius: 6px;
              cursor: pointer;
              position: relative;
              overflow: hidden;
            }
            .avatar-uploader .el-upload:hover {
              border-color: #409eff;
            }
            .avatar-uploader-icon {
              font-size: 28px;
              color: #8c939d;
              width: 80px;
              height: 80px;
              line-height: 80px;
              text-align: center;
            }
            .avatar {
              width: 80px;
              height: 80px;
              display: block;
            }
            .removeImg {
              position: absolute;
              right: 0;
              top: 0;
            }

            .text {
              margin-bottom: 10px;
              margin-left: 5px;
            }
          }
        }

        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          border-top: 1px solid #e9ecef;
          .el-form-item {
            width: 100%;
            padding: 0 !important;
            height: 100%;
            .el-form-item__content {
              height: 100%;
              width: 100%;
              line-height: 72px;
              margin: 0 !important;
              text-align: right;
              padding-right: 20px;
            }
          }
        }
      }
    }
  }

  .dialog-freight {
    // 弹框样式
    /deep/.el-dialog {
      width: 724px;
      height: 680px;
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
        border-bottom: 2px solid #e9ecef;
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
        .el-form {
          padding-bottom: 0px !important;
        }
        .notice {
          padding: 40px 0 0 60px;
          display: flex;
          flex-direction: column;
          .title {
            .el-form-item__label {
              font-weight: normal;
            }
            .el-form-item__label {
              color: #414658;
            }
            .el-form-item__content {
              display: flex;
              input {
                width: 420px;
                height: 40px;
              }
            }
          }
        }
        .form-footer {
          width: 174px;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          .el-form-item {
            padding: 0 !important;
            height: 100%;
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

        .dictionary-list {
          width: 558px !important;
          border-radius: 4px;
          margin-left: 140px;
          .el-table {
            .el-table__header-wrapper {
              thead {
                tr {
                  background-color: #f4f7f9 !important;
                  th {
                    height: 61px;
                    text-align: center;
                    font-size: 14px;
                    font-weight: bold;
                    color: #414658;
                    background-color: #f4f7f9 !important;
                  }
                }
              }
            }
            .el-table__body-wrapper {
              background-color: #f4f7f9;
              tbody {
                tr {
                  td {
                    height: 92px;
                    text-align: center;
                    font-size: 14px;
                    color: #414658;
                    font-weight: 400;
                    padding: 0;
                  }
                }
              }
            }
            button {
              padding: 5px;
              height: 22px;
              background: #ffffff;
              border: 1px solid #d5dbe8;
              border-radius: 2px;
              font-size: 12px;
              font-weight: 400;
              color: #888f9e;
            }
            button:hover {
              border: 1px solid rgb(64, 158, 255);
              color: rgb(64, 158, 255);
            }
            button:hover i {
              color: rgb(64, 158, 255);
            }
          }
        }
      }
    }
  }

  .dialog-freightTemplate {
    // 弹框样式
    /deep/.el-dialog {
      width: 680px;
      height: 680px;
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
        border-bottom: 2px solid #e9ecef;
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
        padding: 41px 0px 0px 0px !important;
        // border-bottom: 1px solid #E9ECEF;
        .check-provinces {
          .el-checkbox {
            width: 120px;
            height: 30px;
            margin-right: 14px;
          }
          .el-form-item__content {
            padding-top: 5px;
          }
        }
        .el-form-item__content {
          line-height: 30px;
        }
        .el-form-item {
          margin-bottom: 12px;
        }
        .el-form-item__label {
          font-weight: normal;
        }
        .el-input__inner {
          width: 304px;
          height: 40px;
        }
        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          border-top: 1px solid #e9ecef;
          .el-form-item {
            padding: 0 !important;
            height: 100%;
            display: flex;
            justify-content: flex-end;
            margin-right: 17px;
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
      }
    }
  }

  /deep/.el-form-item__label {
    color: #414658;
  }
  /deep/.el-input__inner {
    border: 1px solid #d5dbe8;
  }
  /deep/.el-input__inner:hover {
    border: 1px solid #b2bcd4;
  }
  /deep/.el-input__inner:focus {
    border-color: #409eff;
  }
  /deep/.el-input__inner::-webkit-input-placeholder {
    color: #97a0b4;
  }

  .model {
    position: fixed;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    opacity: 0.5;
    background: #000;
    z-index: 2000;
  }
}
</style>

<style>
/* .main-nav {
  z-index: 3 !important;
}
.big-header {
  z-index: 3 !important;
} */
/* .zZindex {
  z-index: 3 !important;
} */
/* .footer-button {
  z-index: 3 !important;
} */
.zZindex {
  z-index: 99999999 !important;
}
.maskNew {
  position: fixed;
  z-index: 10000;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  display: block;
}

/* 添加属性弹窗 */
.maskNew .mask-content {
  border-radius: 4px;
  position: relative;
  background-color: #fff;
  display: flex !important;
  flex-direction: column;
  padding-top: 0;
  width: 680px;
  height: 510px !important;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  margin: 0 !important;
}
.maskNew .mask-title {
  line-height: 58px;
  border-bottom: 1px solid #e9ecef;
  font-size: 16px;
  color: #414658;
  padding-left: 19px;
  margin-bottom: 0;
}
.maskNew .mask-content-data {
  padding: 40px 40px 0;
  overflow: hidden;
  flex: 1;
}
.maskNew .mask-content-data > div {
  display: flex;
}
.maskNew .shooser_attrDiv {
  width: 520px;
  height: 272px;
  background: #f4f7f9;
  margin-top: 10px;
  overflow-x: hidden;
  overflow-y: auto;
  padding: 15px;
  box-sizing: border-box;
  font-size: 14px;
}
.maskNew .shooser_attrDiv select {
  width: 240px;
  height: 36px;
  font-size: 14px;
  color: #414658 !important;
}
.maskNew .shooser_attrDiv > div,
#choose_attrDiv .custom_attr {
  display: flex;
  align-items: center;
}
.maskNew .shooser_attrDiv ul,
.shooser_attrDiv label {
  margin-bottom: 0;
}
.maskNew .shooser_attrDiv ul {
  width: 100%;
  max-height: 171px;
  position: absolute;
  background: #ffffff;
  border: 1px solid #d3dae3;
  border-top: 0;
  overflow-y: auto;
  background: #ffffff;
  z-index: 99;
}
.maskNew .shooser_attrDiv li {
  height: 34px;
}
.maskNew .shooser_attrDiv li:hover {
  background: #f4f7f9;
}
.maskNew .shooser_attrDiv li > label {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 10px;
  box-sizing: border-box;
}
.maskNew .attr_title {
  width: 90px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #2890ff;
  border-radius: 2px;
  color: #2890ff;
  position: relative;
  margin: 20px 0;
  background: #ffffff;
}
.maskNew .attr_title img {
  position: absolute;
  right: 0;
  top: 0;
  width: 12px;
  height: 12px;
}
.maskNew .attr_content {
  display: flex;
  flex-wrap: wrap;
}
.maskNew .attr_content label {
  margin-right: 20px;
  margin-bottom: 10px;
}
.maskNew .custom_attrDiv .left_text {
  display: inline-block;
  width: 70px;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.maskNew .custom_attrDiv input[type='text'] {
  padding-left: 10px;
}
.maskNew .custom_attrDiv a.addBtn,
.custom_attrDiv a.removeBtn {
  display: inline-block;
  width: 88px;
  height: 38px;
  box-sizing: border-box;
  line-height: 38px;
  text-align: center;
  margin-left: 10px;
}
.maskNew .custom_attrDiv a.addBtn {
  color: #2890ff;
  border: 1px solid #2890ff;
}
.maskNew .custom_attrDiv a.removeBtn {
  color: #828b97;
  border: 1px solid #828b97;
}
.maskNew .custom_attrVlue {
  flex-wrap: wrap;
}
.maskNew .custom_attr {
  display: flex;
}
.maskNew .custom_attrVlue a {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 10px;
  background: #ffffff;
  color: #414658;
  border: 1px dashed #b2bcd1;
  margin-bottom: 10px;
  margin-right: 10px;
}
.maskNew .custom_attrVlue a img {
  width: 12px;
  height: 12px;
  margin-left: 10px;
}

.maskNew .mask-bottom {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e9ecef;
  margin-top: auto;
}
.maskNew .mask-bottom input[type='button'] {
  width: 70px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  margin: 16px 0;
  border: 0;
  outline: 0;
  cursor: pointer;
}
.maskNew .mask-bottom input[type='button']:first-child {
  border: 1px solid #d5dbe8;
  color: #6a7076;
  margin-right: 10px;
  background: #ffffff;
  border-radius: 4px;
}
.maskNew .mask-bottom input[type='button']:last-child {
  background: #2890ff;
  color: #ffffff;
  margin-right: 20px;
  border-radius: 4px;
}

.maskNew .maskContent1 input[type='submit'] {
  width: 100px;
  height: 40px;
  border: 1px solid #eee;
  border-radius: 5px;
  background: #008def;
  color: #fff;
  font-size: 16px;
  line-height: 40px;
  display: inline-block;
}

.maskNew .closeMaskBtn {
  width: 100px;
  height: 40px;
  border-radius: 5px;
  background: #fff;
  color: #008def;
  border: 1px solid #008def;
  font-size: 16px;
  line-height: 40px;
  display: inline-block;
  text-align: center;
  box-sizing: border-box;
  cursor: pointer;
  margin-right: 10px;
}

.maskNew .closeA {
  position: absolute;
  right: 10px;
  top: 15px;
  width: 30px;
  height: 30px;
  color: #eee;
}

.maskNew .checkbox {
  position: relative;
  display: inline-block;
  color: #414658;
  font-size: 14px;
  line-height: 14px;
  padding-left: 22px;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

.maskNew .checkbox i {
  position: absolute;
  display: block;
  left: 0;
  top: 0;
  width: 14px;
  height: 14px;
  border: 1px solid #b2bcd1;
  border-radius: 2px;
  box-sizing: border-box;
}

.attr_close_btn:hover {
  color: #2890ff !important;
  border: 1px solid #2890ff !important;
}

.attr_sava_btn:hover {
  background-color: #70aff3 !important;
}

.attr-input {
  height: 40px;
  border: 1px solid #d5dbe8;
  border-radius: 4px;
}

.attr-input::-webkit-input-placeholder {
  color: #97a0b4;
}

.attr-input:focus {
  outline: none;
  border: 1px solid #2890ff !important;
}

#searchAttrIpt {
  height: 40px;
  border: 1px solid #d5dbe8;
  border-radius: 4px;
}

#searchAttrIpt::-webkit-input-placeholder {
  color: #97a0b4;
}

#searchAttrIpt:focus {
  outline: none;
  border: 1px solid #2890ff !important;
}

#chooseAttr {
  border: 1px solid #d5dbe8;
  border-radius: 4px;
}

.addBtn,
.removeBtn {
  border-radius: 4px;
}
</style>

<style scoped lang="less">
/deep/.attribute-table {
  width: 90% !important;
  margin-bottom: 40px;
  .el-table__body-wrapper {
    border-bottom: 1px solid #edf1f5;
  }
  .el-form-item__content {
    width: 100%;
  }
  .el-table--scrollable-x .el-table__body-wrapper {
    overflow-x: auto;
  }
  .el-table .hidden-columns {
    visibility: hidden;
    position: absolute;
    z-index: -1;
  }
  .el-table--fit td.gutter,
  .el-table--fit th.gutter {
    border-right-width: 1px;
  }
  .el-table--scrollable-x .el-table__body-wrapper {
    overflow-x: auto;
  }
  .el-table--scrollable-y .el-table__body-wrapper {
    overflow-y: auto;
  }
  .el-table thead {
    color: #414658;
    font-weight: 500;
  }
  .el-table thead.is-group th {
    background: #f4f7f9;
  }
  .el-table td,
  .el-table th {
    padding: 12px 0;
    min-width: 0;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    text-overflow: ellipsis;
    vertical-align: middle;
    position: relative;
    text-align: center;
  }
  thead th {
    padding: 0 !important;
    height: 50px;
  }

  .el-table {
    z-index: 9 !important;
    .el-input {
      text-align: center;
      width: 140px;
      input {
        width: 140px;
        text-align: center;
      }
    }
  }

  .el-table .l-upload {
    display: flex;
    justify-content: center;
  }

  .el-table th {
    background-color: #f4f7f9;
  }
}
</style>
