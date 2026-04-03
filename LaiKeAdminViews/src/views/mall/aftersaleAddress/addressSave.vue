<template>
  <div class="container">
    <div class="add-menu">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="auto">
        <el-form-item :label="$t('addtemplate.ssgj')"  prop="country_num">
          <el-select   class="calculation" filterable v-model="ruleForm.country_num" :placeholder="$t('addtemplate.qxzssgj')">
            <el-option v-for="(item,index) in countriesList" :key="index" :label="item.zh_name" :value="item.num3">
              <div @click="getIds(item.num3)">{{ item.zh_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('aftersaleAddress.lxr')" prop="name">
          <el-input v-model="ruleForm.name" :placeholder="$t('aftersaleAddress.add.qsrlxr')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('aftersaleAddress.lxdh')" prop="tel">
          <!-- <el-input v-model="ruleForm.tel" :placeholder="$t('aftersaleAddress.add.qsrlxdh')"></el-input> -->
          <div style='display:flex;width:100%' class='phone'>
            <el-autocomplete
             class='custom-autocomplete'
              v-model="cpc"
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
              class="inputs"
              style="margin-left: 12px;"
              :placeholder="$t('increaseStore.qsrlxdh')"
              v-model="ruleForm.tel"
            >
            </el-input>
          </div>
        </el-form-item>
        <el-form-item v-if="isDomestic" :label="$t('aftersaleAddress.add.szdq')" prop="sheng">
          <el-select class="select-input" v-model="ruleForm.sheng" :placeholder="$t('aftersaleAddress.add.sheng')">
            <el-option v-for="(item,index) in shengList" :key="index" :label="item.districtName" :value="item.districtName">
              <div @click="getShi(item.id)">{{ item.districtName }}</div>
            </el-option>
          </el-select>
          <el-select class="select-input" v-model="ruleForm.shi" :placeholder="$t('aftersaleAddress.add.shi')">
            <el-option v-for="(item,index) in shiList" :key="index" :label="item.districtName" :value="item.districtName">
              <div @click="getXian(item.id)">{{ item.districtName }}</div>
            </el-option>
          </el-select>
          <el-select class="select-input" v-model="ruleForm.xian" :placeholder="$t('aftersaleAddress.add.xian')">
            <el-option v-for="(item,index) in xianList" :key="index" :label="item.districtName" :value="item.districtName">
              <div>{{ item.districtName }}</div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item  :label="$t('aftersaleAddress.xxdz')" prop="address">
          <el-input v-model="ruleForm.address" :placeholder="$t('aftersaleAddress.add.qsrxxdz')"></el-input>
        </el-form-item>
        <el-form-item label="州/省/地区" v-if="!isDomestic">
          <el-input v-model="ruleForm.province" placeholder="街道地址、邮政信箱、公司名称、转交方、公寓、套房、单元、大厦、楼层等" @keyup.native="ruleForm.code = oninput2(ruleForm.code)"></el-input>
        </el-form-item>
        <el-form-item label="城市" v-if="!isDomestic">
          <el-input v-model="ruleForm.city" placeholder="请输入所在城市" @keyup.native="ruleForm.code = oninput2(ruleForm.code)"></el-input>
        </el-form-item>
        <el-form-item :label="$t('aftersaleAddress.yzbm')" prop="code">
          <el-input v-model="ruleForm.code" :placeholder="$t('aftersaleAddress.add.qsryzbm')" @keyup.native="ruleForm.code = oninput2(ruleForm.code)"></el-input>
        </el-form-item>
        <el-form-item :label="$t('aftersaleAddress.sfmr')" prop="is_default">
          <el-radio-group v-model="ruleForm.is_default">
            <el-radio v-model="ruleForm.is_default" :label="1">{{$t('aftersaleAddress.Yes')}}</el-radio>
            <el-radio v-model="ruleForm.is_default" :label="0">{{$t('aftersaleAddress.No')}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>


<script>
import addressSave from "@/webManage/js/mall/aftersaleAddress/addressSave";
export default addressSave
</script>

<style scoped lang="less">
  @import "../../../webManage/css/mall/aftersaleAddress/addressSave.less";
</style>
