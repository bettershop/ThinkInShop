<template>
  <div class="container">
    <!-- <div class="header">
      <span>店铺信息编辑</span>
    </div> -->

    <el-form :model="ruleForm" label-position="right" ref="ruleForm" label-width="100px" class="form-search" v-if="storeInfo">
      <div class="notice">
        <el-form-item :label="$t('stores.viewStore.dplg')" required>
          <l-upload
              :limit="1"
              v-model="ruleForm.logo"
              text=" "
            >
            </l-upload>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.dptx')" required>
          <l-upload
              :limit="1"
              v-model="ruleForm.headImg"
              text=" "
            >
            </l-upload>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.xct')" prop="">
          <l-upload
              :limit="1"
              v-model="ruleForm.posterImg"
              text=" "
            >
            </l-upload>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.dpmc')" required>
          <!-- <span>{{ storeInfo.name }}</span> -->
          <el-input style="width:580px !important" v-model="ruleForm.name"></el-input>
        </el-form-item>
        <el-form-item class="input-interval" :label="$t('stores.viewStore.zfjmh')" prop="">
          <el-input style="width:580px !important" v-model="ruleForm.roomid" @keyup.native="ruleForm.roomid = oninput2(ruleForm.roomid)"></el-input>
        </el-form-item>
        <el-form-item class="input-interval" :label="$t('stores.viewStore.dpxx') " prop="" >
          <el-input style="width:580px !important" v-model="ruleForm.shop_information"></el-input>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.dpfl') " prop="" required>
          <!-- 选择店铺分类 -->
          <el-select style="width:580px !important" class="select-input" v-model="ruleForm.mac_choose_fl" :placeholder="storeInfo.className">
            <el-option v-for="(item,index) in choose_fl" :key="index" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.jyfw') " prop="" required>
          <el-input style="width:580px !important" v-model="ruleForm.confines"></el-input>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.yyzt') " required>
          <!-- <span>{{ storeInfo.is_open == 0 ? $t('stores.viewStore.wyy') : storeInfo.is_open == 1 ? $t('stores.viewStore.yyz') : $t('stores.viewStore.ydy') }}</span> -->
          <el-select disabled style="width:580px !important" class="select-input" v-model="ruleForm.is_open" placeholder="请选择营业状态">
            <el-option v-for="(item,index) in statusList" :key="index" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <!-- <el-form-item :label="$t('stores.viewStore.yhmc') " prop=""> -->
          <!-- <span>{{ storeInfo.user_name }}</span> -->
          <!-- <el-input style="width:580px !important" v-model="ruleForm.user_name"></el-input>
        </el-form-item> -->
        <el-form-item :label="$t('stores.viewStore.zsxm') " required>
          <!-- <span>{{ storeInfo.realname }}</span> -->
          <el-input style="width:580px !important" v-model="ruleForm.realname"></el-input>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.sfzhm') " required>
          <!-- <span>{{ storeInfo.ID_number }}</span> -->
          <el-input style="width:580px !important" v-model="ruleForm.ID_number"></el-input>
        </el-form-item>
        <el-form-item class="input-interval" :label="$t('stores.viewStore.lxdh') " prop="" required>
          <div class="input-interval-block">
            <el-autocomplete
             class='auto'
              v-model="countryNum"
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
            <el-input v-model="ruleForm.tel"></el-input>
          </div>
        </el-form-item>
        <el-form-item v-if="isDomestic" class="cascadeAddress input-interval" :label="$t('stores.editorStore.szdq')" required>
            <div class="cascadeAddress-block" style="width:580px !important">
                <el-select class="select-input" v-model="ruleForm.sheng" :placeholder="$t('stores.editorStore.sheng')">
                    <el-option v-for="(item,index) in shengList" :key="index" :label="item.districtName" :value="item.districtName">
                        <div @click="getShi(item.id)">{{ item.districtName }}</div>
                    </el-option>
                </el-select>
                <el-select class="select-input" v-model="ruleForm.shi" :placeholder="$t('stores.editorStore.shi')">
                    <el-option v-for="(item,index) in shiList" :key="index" :label="item.districtName" :value="item.districtName">
                        <div @click="getXian(item.id)">{{ item.districtName }}</div>
                    </el-option>
                </el-select>
                <el-select class="select-input" v-model="ruleForm.xian" :placeholder="$t('stores.editorStore.xian')">
                    <el-option v-for="(item,index) in xianList" :key="index" :label="item.districtName" :value="item.districtName">
                        <div>{{ item.districtName }}</div>
                    </el-option>
                </el-select>
            </div>
        </el-form-item>
        <el-form-item :label="$t('stores.editorStore.xxdz')" prop="" required>
          <el-input style="width:580px !important" v-model="ruleForm.address"></el-input>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.ssxz') " prop="">
            <el-radio-group style="line-height: 52px;" v-model="ruleForm.shop_nature" @change="uploadChange($event)">
              <el-radio v-for="item in belongList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
            </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.sfzj')" v-if="ruleForm.shop_nature == 0" required>
          <l-upload
              v-if="ruleForm.shop_nature == 0"
              :limit="2"
              :key="1"
              text=" "
              v-model="ruleForm.business_licens"
            >
            </l-upload>
        </el-form-item>
        <el-form-item :label="$t('stores.viewStore.yyzz')" v-if="ruleForm.shop_nature == 1" required>
            <l-upload
              v-if="ruleForm.shop_nature == 1"
              :limit="1"
              :key="2"
              text=" "
              v-model="ruleForm.business_licens1"
            >
            </l-upload>
        </el-form-item>
        <el-form-item class="footer-button">
          <el-button type="primary" class="footer-save bgColor mgleft" @click="submitForm('ruleForm')">{{$t('DemoPage.tableFromPage.save')}}</el-button>
          <el-button plain class="footer-cancel fontColor shaco_left" @click="$router.back(-1)">{{$t('DemoPage.tableFromPage.cancel')}}</el-button>
        </el-form-item>
      </div>
	  </el-form>
    <div style="min-height: 20px;width: 100%;background: #edf1f5;"></div>
  </div>
</template>

<script>
import editorStore from '@/webManage/js/plug_ins/stores/editorStore'
export default editorStore
</script>

<style scoped lang="less">
@import  '../../../webManage/css/plug_ins/stores/editorStore.less';
</style>
