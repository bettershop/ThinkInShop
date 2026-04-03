<template>
  <div class="container">
    <el-form
      ref="ruleForm"
      class="form-search"
      :model="ruleForm"
      label-width="130px"
    >
      <div class="basic-info">
        <div class="header">
          <span>{{ $t('releasephysical.jbxx') }}</span>
        </div>
        <div class="basic-block">
          <el-form-item :label="$t('releasephysical.spbt')" required>
            <span> {{ goodsInfo.product_title }}</span>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.fbt')">
            <span> {{ goodsInfo.subtitle }}</span>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.gjc')" required>
            <span> {{ goodsInfo.keyword }}</span>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.txm')" required>
            <img :src="goodsInfo.scan" alt="" />
          </el-form-item>
          <el-form-item :label="$t('releasephysical.spfl')" required>
            <span>{{ goodsInfo.className }}</span>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.sppp')" required>
            <span>{{ goodsInfo.brandName }}</span>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.zl')" required v-if="goodsInfo.commodity_type != 1">
            <span>{{ goodsInfo.weight }}</span>
          </el-form-item>
          <el-form-item
            class="goosdImg"
            :label="$t('releasephysical.spfmt')"
            required
          >
            <div class="goods-cover">
              <img :src="goodsInfo.cover_map" alt="" />
            </div>
          </el-form-item>
          <el-form-item
            class="goosdImg"
            :label="$t('releasephysical.spzst')"
            required
          >
            <l-upload
              :limit="goodsImageUrls.length"
              :mainImg="true"
              v-model="goodsImageUrls"
              text=""
            >
            </l-upload>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.zssp')">
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
                  </div>
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

      <div class="goods-attribute" v-if="goodsDate.length > 0">
        <div class="header">
          <span>{{ $t('releasephysical.spsx') }}</span>
        </div>
        <div class="attribute-block">
          <el-table
            :data="goodsDate"
            style="width: 90%"
            :header-cell-style="header"
            height="350"
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
              prop="costprice"
              align="center"
              :label="$t('releasephysical.cbj')"
            >
              <template slot-scope="scope">
                <span>{{ scope.row.cbj }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="yprice"
              align="center"
              :label="$t('releasephysical.yj')"
            >
              <template slot-scope="scope">
                <span v-if="scope.row.yj">{{ scope.row.yj }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="price"
              align="center"
              :label="$t('releasephysical.sj')"
            >
              <template slot-scope="scope">
                <span>{{ scope.row.sj }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="kucun"
              align="center"
              :label="$t('releasephysical.kc')"
              v-if="goodsInfo.commodity_type != 1"
            >
            </el-table-column>
            <el-table-column
              prop="write_off_num"
              align="center"
              :label="$t('releasephysical.hxcs')"
              v-if="goodsInfo.commodity_type == 1"
            >
            </el-table-column>
            <el-table-column
              prop="unit"
              align="center"
              :label="$t('releasephysical.dw')"
            >
            </el-table-column>
            <el-table-column
              prop="imgurl"
              align="center"
              :label="$t('releasephysical.sctp')"
            >
              <template slot-scope="scope">
                <img :src="scope.row.img" alt="" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div class="goods-set">
        <div class="header">
          <span>{{ $t('releasephysical.spsz') }}</span>
        </div>
        <div class="set-block">
          <el-form-item
            class="inventory-warning"
            :label="$t('releasephysical.kcyj')"
            required
            v-if="goodsInfo.commodity_type != 1"
          >
            <div class="warning-info">
              {{ $t('releasephysical.dqckl')
              }}<span>{{ goodsInfo.min_inventory }}</span
              >{{ $t('releasephysical.sspkc') }}
            </div>
          </el-form-item>
          <el-form-item
            class="freight-set"
            :label="$t('releasephysical.yfsz')"
            required
            v-if="goodsInfo.commodity_type != 1"
          >
            <span> {{ goodsInfo.freightName }}</span>
          </el-form-item>
          <el-form-item :label="$t('releasephysical.xsbq')">
            <el-checkbox-group disabled v-model="ruleForm.checkedLabel">
              <el-checkbox
                v-for="label in labelList"
                :label="label.id"
                :key="label.id"
                >{{ label.name }}</el-checkbox
              >
            </el-checkbox-group>
          </el-form-item>
          <!-- <el-form-item
            class="activity-class"
            :label="$t('releasephysical.zchd')"
            required
          >
            <el-radio-group v-model="ruleForm.checkedActivity">
              <el-radio
                disabled
                v-for="activity in activityList"
                :label="activity.value"
                :key="activity.value"
                >{{ activity.name }}</el-radio
              >
            </el-radio-group>
          </el-form-item> -->
          <el-form-item class="show-local2" :label="$t('releasephysical.qdxs')">
            <!-- <el-checkbox-group disabled v-model="ruleForm.checked">
              <el-checkbox
                v-for="label in showAdrList"
                :label="label.value"
                :key="label.value"
                >{{ label.text }}</el-checkbox
              >
            </el-checkbox-group> -->
            <el-radio-group disabled v-model="ruleForm.checked">
              <el-radio v-for="label in showAdrList"
                :label="label.value"
                :key="label.value">{{ label.text }}</el-radio>
            </el-radio-group>
            <span class="show-font">{{ $t('releasephysical.rgbx') }}</span>
          </el-form-item>
        </div>
      </div>

      <div class="detailed-content">
        <div class="header">
          <span>{{ $t('releasephysical.xxnr') }}</span>
        </div>
        <div class="detailed-block">
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
                  </div>
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
          <!-- <el-form-item class="inventory-warning" :label="$t('releasephysical.spxq')">
            <div class="content" v-html="ruleForm.content">
            </div>
          </el-form-item> -->
          <el-form-item :label="$t('releasephysical.spjs')">
            <div
              class="add_box"
              v-for="(item, index) in IntroList"
              :key="index"
            >
              <div class="delet-box">
                <span>{{ item.name }}</span>
              </div>
              <el-form-item>
                <vue-editor
                  v-model="item.content"
                  useCustomImageHandler
                  @focus="onEditorFocus($event)"
                ></vue-editor>
              </el-form-item>
            </div>
          </el-form-item>
        </div>
      </div>

      <!-- <div class="footer-button">
        <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">返回</el-button>
      </div> -->
    </el-form>
  </div>
</template>

<script>
import viewGoods from '@/webManage/js/plug_ins/stores/viewGoods'
export default viewGoods
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/stores/viewGoods.less';
</style>
