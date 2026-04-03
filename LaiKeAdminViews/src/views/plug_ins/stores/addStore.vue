<template>
  <div class="container">
    <div class="add-menu">
      <el-form
        :model="ruleForm"
        :rules="rules"
        ref="ruleForm"
        class="picture-ruleForm"
        label-width="auto"
      >
        <el-form-item class="upload-img" label="店铺logo" prop="logo">
          <l-upload
            :limit="1"
            v-model="ruleForm.logo"
            text="(最多上传一张，建议上传120px*40px的图片)"
          >
          </l-upload>
        </el-form-item>
        <el-form-item class="upload-img" label="店铺头像" prop="headImg">
          <l-upload
            :limit="1"
            v-model="ruleForm.headImg"
            text="(最多上传一张，建议上传120px*120px的图片)"
          >
          </l-upload>
        </el-form-item>
        <el-form-item class="upload-img" label="店铺宣传图" prop="posterImg">
          <l-upload
            :limit="1"
            v-model="ruleForm.posterImg"
            text="(图片尺寸200*200px，仅用于推荐商家功能及PC商城店铺主页呈现)"
          >
          </l-upload>
        </el-form-item>
        <el-form-item label="店铺名称" prop="name">
          <el-input
            maxlength="20"
            v-model="ruleForm.name"
            placeholder="请输入店铺名称"
          ></el-input>
        </el-form-item>
        <el-form-item label="店铺信息" prop="shop_information">
          <el-input
            maxlength="200"
            v-model="ruleForm.shop_information"
            placeholder="请输入店铺信息"
          ></el-input>
        </el-form-item>
        <!-- 选择店铺分类 -->
        <el-form-item label="店铺分类" prop="mac_choose_fl">
          <el-select
            class="select-input2"
            v-model="ruleForm.mac_choose_fl"
            placeholder="选择店铺分类"
          >
            <el-option
              v-for="(item, index) in choose_fl"
              :key="index"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
          <el-button class="add_bt" @click="dialogVisible = true">添加店铺分类</el-button>
        </el-form-item>
        <el-form-item label="经营范围" prop="shop_range">
          <el-input
            maxlength="200"
            v-model="ruleForm.shop_range"
            placeholder="请输入经营范围"
          ></el-input>
        </el-form-item>
        <!-- <el-form-item label="所在社区" prop="community_id">
          <el-input v-model="ruleForm.community_id" placeholder="请输入所在社区"></el-input>
        </el-form-item>
        <el-form-item label="物业管理员" prop="adminid">
          <el-input v-model="ruleForm.adminid" placeholder="请输入物业管理员"></el-input>
        </el-form-item> -->
        <el-form-item label="用户姓名" prop="realname">
          <el-input
            maxlength="20"
            placeholder="请输入用户姓名"
            v-model="ruleForm.realname"
          ></el-input>
        </el-form-item>
        <el-form-item label="身份证号" prop="ID_number">
          <el-input
            placeholder="请输入身份证号"
            @keyup.native="ruleForm.ID_number = oninput3(ruleForm.ID_number)"
            v-model="ruleForm.ID_number"
          ></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="tel">
          <div class="input-interval-block">
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
            <el-input
            placeholder="请输入联系电话"
            @keyup.native="ruleForm.tel = oninput2(ruleForm.tel)"
            v-model="ruleForm.tel"
          ></el-input>
          </div>

        </el-form-item>
        <el-form-item v-if="isDomestic" class="cascadeAddress" label="所在地区" prop="sheng">
          <div class="cascadeAddress-block">
            <el-select
              class="select-input"
              v-model="ruleForm.sheng"
              placeholder="省"
            >
              <el-option
                v-for="(item, index) in shengList"
                :key="index"
                :label="item.districtName"
                :value="item.districtName"
              >
                <div @click="getShi(item.id)">{{ item.districtName }}</div>
              </el-option>
            </el-select>
            <el-select
              class="select-input"
              v-model="ruleForm.shi"
              placeholder="市"
            >
              <el-option
                v-for="(item, index) in shiList"
                :key="index"
                :label="item.districtName"
                :value="item.districtName"
              >
                <div @click="getXian(item.id)">{{ item.districtName }}</div>
              </el-option>
            </el-select>
            <el-select
              class="select-input"
              v-model="ruleForm.xian"
              placeholder="县"
            >
              <el-option
                v-for="(item, index) in xianList"
                :key="index"
                :label="item.districtName"
                :value="item.districtName"
              >
                <div>{{ item.districtName }}</div>
              </el-option>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input
            placeholder="请输入详细地址"
            v-model="ruleForm.address"
          ></el-input>
        </el-form-item>
        <el-form-item label="所属性质">
          <!-- <el-radio v-model="ruleForm.shop_nature" label="1">个人</el-radio>
            <el-radio v-model="ruleForm.shop_nature" label="2">企业</el-radio> -->
          <el-radio-group
            v-model="ruleForm.shop_nature"
            @change="natureChange($event)"
          >
            <el-radio
              v-for="item in belongList"
              :label="item.value"
              :key="item.value"
              >{{ item.name }}</el-radio
            >
          </el-radio-group>
        </el-form-item>
        <!-- <el-form-item :label="$t('stores.viewStore.sfzj')" v-if="ruleForm.shop_nature == 0" required>
          <l-upload
              v-if="ruleForm.shop_nature == 0"
              :limit="2"
              :key="1"
              text=" "
              v-model="ruleForm.business_licens"
            >
            </l-upload>
        </el-form-item> -->
        <el-form-item
          v-show="ruleForm.shop_nature == 0"
          class="goods-img"
          required
          label="身份证正面"
        >
          <l-upload ref="upload1" :limit="1" text="" v-model="ruleForm.img1"> </l-upload>
        </el-form-item>
        <el-form-item
          v-show="ruleForm.shop_nature == 0"
          class="goods-img"
          required
          label="身份证反面"
        >
          <l-upload ref="upload2" :limit="1" text="" v-model="ruleForm.img2"> </l-upload>
        </el-form-item>
        <el-form-item
          :label="$t('stores.viewStore.yyzz')"
          v-if="ruleForm.shop_nature == 1"
          required
        >
          <l-upload
            v-show="ruleForm.shop_nature == 1"
            :limit="1"
            text=" "
            ref="upload3"
            v-model="ruleForm.img3"
          >
          </l-upload>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button
              class="bgColor"
              type="primary"
              @click="submitForm('ruleForm')"
              >{{ $t('DemoPage.tableFromPage.save') }}</el-button
            >
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{
              $t('DemoPage.tableFromPage.cancel')
            }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <div class="dialog-brand">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('releasephysical.tjfl')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <el-form
          :model="ruleForm2"
          :rules="rules2"
          ref="ruleForm2"
          class="picture-ruleForm"
          label-width="auto"
        >
        <el-form-item :label="$t('stores.addStoreFl.flmc')" prop="name">
          <el-input maxlength="20" class="select-input" v-model="ruleForm2.name" :placeholder="$t('stores.addStoreFl.qsrfl')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('stores.addStoreFl.fltb')">
          <l-upload
            ref="imgUpload"
            :limit="1"
            v-model="ruleForm2.img"
            :text="$t('stores.addStoreFl.zdscq')"
          >
          </l-upload>
        </el-form-item>
        <el-form-item class="input-interval" :label="$t('stores.addStoreFl.pxh')" prop="px">
          <el-input class="select-input" @keyup.native="ruleForm2.px = oninput2(ruleForm2.px)" v-model="ruleForm2.px" :placeholder="max + $t('stores.addStoreFl.mrpxh')"></el-input>
        </el-form-item>

       <el-form-item :label="$t('stores.addStoreFl.sfxs')" style="margin-bottom: 0;">
            <el-radio-group v-model="ruleForm2.ishow">
              <el-radio v-for="item in classList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
            </el-radio-group>
        </el-form-item>

          <div class="form-footer">
            <el-form-item>
              <el-button class="bdColor" @click="handleClose" plain>{{
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
    <div class="model" v-show="dialogVisible"></div>
  </div>
</template>

<script>
import addStore from '@/webManage/js/plug_ins/stores/addStore'
export default addStore
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/stores/addStore.less';
</style>
