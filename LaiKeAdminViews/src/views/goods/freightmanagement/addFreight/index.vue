<template>
  <div class="container">
    <!-- <div class="header">
			<span>{{this.$router.currentRoute.matched[2].meta.title}}</span>
		</div> -->

    <el-form
      :model="ruleForm"
      :rules="rule"
      ref="ruleForm"
      class="form-search"
      label-width="auto"
    >
      <div class="notice">
        <el-form-item :label="$t('addtemplate.ssgj')" class="title calculation" prop="country_num">
          <el-select  :disabled="disabled" filterable v-model="ruleForm.country_num" :placeholder="$t('addtemplate.qxzssgj')">
            <el-option v-for="(item,index) in countriesList" :key="index" :label="item.zh_name" :value="item.num3">
              <div @click="getIds(item.num3)">{{ item.zh_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('addtemplate.yfyz')" class="title calculation"  prop="lang_code">
          <el-select :disabled="disabled" filterable v-model="ruleForm.lang_code" :placeholder="$t('addtemplate.qxzyz')">
            <el-option v-for="(item,index) in languages" :key="index" :label="item.lang_name" :value="item.lang_code">
              <div @click="getlangCode(item.lang_code)">{{ item.lang_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item class="title" :label="$t('addtemplate.yfmc')" prop="name">
          <el-input
            v-model="ruleForm.name"
            :placeholder="$t('addtemplate.qsryfmb')"
            :disabled="disabled"
          ></el-input>
        </el-form-item>
        <el-form-item
          class="title calculation"
          :label="$t('addtemplate.jsfs')"
          prop="type"
        >
          <el-select
            v-model="ruleForm.type"
            :placeholder="$t('addtemplate.qxz')"
            :disabled="disabled"
          >
            <el-option :label="$t('addtemplate.jj')" value="0"></el-option>
            <el-option :label="$t('addtemplate.jz')" value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('addtemplate.mryf')" prop="defaultFreight">
          <div class="model_price">
            <div style="display: flex; margin-bottom: 10px; margin-top: 10px">
              <div>
                <el-input
                  :disabled="disabled"
                  v-model="ruleForm.num1"
                  class="num_input"
                  :placeholder="
                    ruleForm.type == 0
                      ? $t('addtemplate.qsrjs')
                      : $t('addtemplate.qsrqk')
                  "
                  @keyup.native="
                    ruleForm.num1 =
                      ruleForm.type == 0
                        ? oninput2(ruleForm.num1)
                        : oninput(ruleForm.num1, 2)
                  "
                >
                  <template slot="append">{{
                    ruleForm.type == 0
                      ? $t('addtemplate.jn')
                      : $t('addtemplate.qkn')
                  }}</template>
                </el-input>
              </div>
              <div style="margin-left: 10px">
                <el-input
                  :disabled="disabled"
                  v-model="ruleForm.num2"
                  class="num_input"
                  :placeholder="$t('addtemplate.qsrjg')"
                  @keyup.native="ruleForm.num2 = oninput(ruleForm.num2, 2)"
                >
                  <template slot="append">{{
                      laikeCurrencySymbol
                  }}</template>
                </el-input>
              </div>
            </div>
            <div style="margin-left: 20px; margin-bottom: 10px">
              <span style="color: #414658; margin-right: 4px">{{
                $t('addtemplate.mzj')
              }}</span>
              <el-input
                :disabled="disabled"
                v-model="ruleForm.num3"
                class="price_input"
                :placeholder="
                  ruleForm.type == 0
                    ? $t('addtemplate.qsrjs')
                    : $t('addtemplate.qsrqks')
                "
                @keyup.native="
                  ruleForm.num3 =
                    ruleForm.type == 0
                      ? oninput2(ruleForm.num3)
                      : oninput(ruleForm.num3, 2)
                "
              >
                <template slot="append">{{
                  ruleForm.type == 0
                    ? $t('addtemplate.jian')
                    : $t('addtemplate.qk')
                }}</template>
              </el-input>
            </div>
            <div style="margin-left: 6px; margin-bottom: 10px">
              <span style="color: #414658; margin-right: 4px">{{
                $t('addtemplate.zjyf')
              }}</span>
              <el-input
                :disabled="disabled"
                v-model="ruleForm.num4"
                class="price_input"
                :placeholder="$t('addtemplate.qsryf')"
                @keyup.native="ruleForm.num4 = oninput(ruleForm.num4, 2)"
              >
                <template slot="append">{{ laikeCurrencySymbol }}</template>
              </el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('addtemplate.zddq')" v-if="isCN">
          <div>
            <el-button
              v-if="disabled == false"
              @click="setPrice()"
              class="bt_price"
              >{{ $t('addtemplate.szyf') }}</el-button
            >
          </div>
          <el-form-item
            style="width: 830px"
            v-if="ruleForm.tableData && ruleForm.tableData.length != 0"
          >
            <el-table
              :data="ruleForm.tableData"
              ref="table"
              :class="{ active: $route.query.name == 'view' }"
              class="el-table"
              style="width: 100%; margin-top: 20px"
              :rules="rules2"
              :header-cell-style="tableHeaderColor"
              :cell-style="{ textAlign: 'center' }"
            >
              <el-table-column :label="$t('addtemplate.sdd')" width="200">
                <template slot-scope="scope">
                  <div>
                    <div>{{ scope.row.picList }}</div>
                    <div
                      v-if="disabled == false"
                      class="edit_one"
                      @click="edit(scope.row, scope.$index)"
                    >
                      |&nbsp;{{ $t('addtemplate.bianji') }}
                    </div>
                    <div
                      v-if="disabled"
                      class="edit_one"
                      @click="dialogShow2(scope.row, scope.$index)"
                    >
                      |&nbsp;{{ $t('addtemplate.ck') }}
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                :label="$t('addtemplate.sjj')"
                v-if="ruleForm.type == 0"
                :key="Math.random()"
              >
                <template slot-scope="scope">
                  <el-input
                    class="input_freight"
                    :disabled="disabled"
                    style="width: 100px"
                    v-model="scope.row.one"
                    v-on:input="
                      scope.row.one = scope.row.one.replace(/^(0+)|[^\d]+/g, '')
                    "
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column
                :label="$t('addtemplate.szqk')"
                v-if="ruleForm.type == 1"
                :key="Math.random()"
              >
                <template slot-scope="scope">
                  <el-input
                    class="input_freight"
                    :disabled="disabled"
                    style="width: 100px"
                    v-on:input="
                      scope.row.one = scope.row.one.replace(/^(0+)|[^\d]+/g, '')
                    "
                    v-model="scope.row.one"
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column :label="$t('addtemplate.sfy')">
                <template slot-scope="scope">
                  <el-input
                    class="input_freight"
                    :disabled="disabled"
                    style="width: 100px"
                    v-on:input="
                      scope.row.freight = oninput(scope.row.freight, 2)
                    "
                    v-model="scope.row.freight"
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column
                :label="$t('addtemplate.xjj')"
                v-if="ruleForm.type == 0"
                :key="Math.random()"
              >
                <template slot-scope="scope">
                  <el-input
                    class="input_freight"
                    :disabled="disabled"
                    style="width: 100px"
                    v-on:input="
                      scope.row.two = scope.row.two.replace(/^(0+)|[^\d]+/g, '')
                    "
                    v-model="scope.row.two"
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column
                :label="$t('addtemplate.xzqk')"
                v-if="ruleForm.type == 1"
                :key="Math.random()"
              >
                <template slot-scope="scope">
                  <el-input
                    class="input_freight"
                    :disabled="disabled"
                    style="width: 100px"
                    v-on:input="
                      scope.row.two = scope.row.two.replace(/^(0+)|[^\d]+/g, '')
                    "
                    v-model="scope.row.two"
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column :label="$t('addtemplate.xfy')">
                <template slot-scope="scope">
                  <el-input
                    class="input_freight"
                    :disabled="disabled"
                    style="width: 100px"
                    v-on:input="
                      scope.row.Tfreight = oninput(scope.row.Tfreight, 2)
                    "
                    v-model="scope.row.Tfreight"
                  ></el-input>
                </template>
              </el-table-column>
              <el-table-column :label="$t('addtemplate.cz')">
                <template slot-scope="scope">
                  <el-button
                    class="del_select"
                    :disabled="disabled"
                    icon="el-icon-delete"
                    size="mini"
                    @click="Delete(scope.$index, scope.row)"
                    >{{ $t('addtemplate.shanchu') }}</el-button
                  >
                </template>
              </el-table-column>
            </el-table>
          </el-form-item>
        </el-form-item>

        <el-form-item v-if="isCN" class="rule-block" :label="$t('addtemplate.bpsdq')">
          <div>
            <el-button
              v-if="disabled == false"
              @click="setRegion()"
              class="bt_price"
              >{{ $t('addtemplate.xzdq') }}</el-button
            >
          </div>
          <div class="send-areas" v-if="addressList && addressList.length>0">
            <el-form-item>
              <div class="send_pic">
                <el-tree
                  :data="addressList"
                  :props="defaultProps"
                  :default-expanded-keys="[nodeKey]"
                  :auto-expand-parent="true"
                  node-key="id"
                >
                  <p slot-scope="{ node }">
                    {{ node.label }}
                   <img :src="removeImg" @click.stop="deleteNode(node)" v-if="!disabled" class="removeImg" alt="">

                  </p>
                </el-tree>
              </div>
            </el-form-item>
          </div>
        </el-form-item>

        <el-form-item v-if="$route.query.name != 'view'">
          <div class="bt_top">
            <el-button
              type="primary"
              class="footer-save bgColor mgleft"
              @click="submitForm('ruleForm')"
              >{{ $t('DemoPage.tableFromPage.save') }}
            </el-button>
            <el-button
              plain
              class="footer-cancel fontColor"
              style="margin-left: 14px"
              @click="$router.go(-1)"
              >{{ $t('DemoPage.tableFromPage.cancel') }}</el-button
            >
          </div>
        </el-form-item>
      </div>
    </el-form>
    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('addtemplate.ckdq')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
        width="600px"
      >
        <el-form
          :model="ruleForm"
          ref="ruleForm"
          label-width="100px"
          class="demo-ruleForm"

        >
        <div style=" ">
            <div
            class="task-container"
            v-for="(item, index) in citiesList"
            :key="index"
            >
            <div class="courier-company">{{ item }}</div>
          </div>
        </div>
        </el-form>
      </el-dialog>
    </div>
    <!-- 添加指定运费的地区 -->
    <div class="dialog-pic">
      <el-dialog
        :title="$t('addtemplate.xzdq')"
        :destroy-on-close="false"
        :visible.sync="dialogpic"
      >
        <el-cascader-panel
          @change="panelChange($event)"
          v-model="cascader"
          :props="props1"
          :options="options"
          ref="cascader"
          :clearable="true"
        ></el-cascader-panel>
        <span slot="footer" class="dialog-footer">
          <el-button class="bgColor" @click="dialogpic = false">{{
            $t('DemoPage.tableFromPage.cancel')
          }}</el-button>
          <el-button class="bdColor" type="primary" @click="setmit()">{{
            $t('DemoPage.tableFromPage.save')
          }}</el-button>
        </span>
      </el-dialog>
    </div>
    <!-- 不发地区 -->
    <div class="dialog-area">
      <el-dialog
        :title="$t('addtemplate.xzdq')"
        :destroy-on-close="false"
        :visible.sync="dialogarea"
      >
        <el-cascader-panel
          v-model="cascader2"
          :props="props1"
          :options="options"
          @change="handleChange"
          :collapse-tags="true"

          ref="cascader2"
        >
        </el-cascader-panel>
        <span slot="footer" class="dialog-footer">
          <el-button class="bgColor" @click="dialogarea = false">{{
            $t('DemoPage.tableFromPage.cancel')
          }}</el-button>
          <el-button class="bdColor" type="primary" @click="submit()">{{
            $t('DemoPage.tableFromPage.save')
          }}</el-button>
        </span>
      </el-dialog>
    </div>
    <!-- 编辑指定运费的地区 -->
    <div class="dialog-edit">
      <el-dialog
        :title="$t('addtemplate.xzdq')"
        :destroy-on-close="false"
        :visible.sync="dialogedit"
      >
        <el-cascader-panel
          @change="panelChange($event)"
          v-model="cascader3"
          :props="props1"
          ref="cascader3"
          :options="options"
        ></el-cascader-panel>
        <span slot="footer" class="dialog-footer">
          <el-button class="bgColor" @click="dialogedit = false">{{
            $t('DemoPage.tableFromPage.cancel')
          }}</el-button>
          <el-button class="bdColor" type="primary" @click="editmit()">{{
            $t('DemoPage.tableFromPage.save')
          }}</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { addFreight, getRegion } from '@/api/goods/freight'
import request from '@/api/https'
import cascade from '@/api/publics/cascade'
export default {
  name: 'addFreight',

  data () {
    // 默认运费模板不为空校验
    var checkFreight = (rule, value, callback) => {
      if (
        this.ruleForm.num1 == '' ||
        this.ruleForm.num2 == '' ||
        this.ruleForm.num3 == '' ||
        this.ruleForm.num4 == ''
      ) {
        return callback(new Error(this.$t('addtemplate.mryfmbbnwk')))
      } else if (this.ruleForm.num1 == '0' || this.ruleForm.num3 == '0') {
        return callback(new Error(this.$t('addtemplate.mryfsjbnw')))
      } else {
        return callback() //自定义校验规则通过需要返回一个回调函数，不然会导致表单校验不生效
      }
    }
    // 仓库地址不为空校验
    var checkAddress = (rule, value, callback) => {
      if (
        this.ruleForm.province == '' ||
        this.ruleForm.city == '' ||
        this.ruleForm.area == '' ||
        this.ruleForm.address == '' ||
        this.ruleForm.province == undefined ||
        this.ruleForm.city == undefined ||
        this.ruleForm.area == undefined
      ) {
        return callback(new Error(this.$t('addtemplate.qtxwzckdz')))
      } else {
        return callback()
      }
    }
    let self = this
    return {
      props1: {
        value: 'id', //设置默认值value
        label: 'district_name', //设置显示的是什么
        children: 'children', //设置子元素数组是啥
        multiple: true //开启多选模式
      },
      props: {
        multiple: true, //开启多选模式
        emitPath: false,
        lazy: true,
        async lazyLoad (node, resolve) {
          const { level, data } = node
          // console.log(node,'------',data,'------',level);
          let levelID = level ? level + 2 : 2
          let parentId = data ? data.value : ''
          if (levelID >= 5) {
            // 第三级 resolve(null)和return一起用 可以解决存在加载loading效果或者直接不return造成的多余请求
            resolve(null)
            return
          }
          let nodes = await self.getGClist(levelID, parentId)

          let _nodes = nodes.map(item => ({
            value: item.id,
            label: item.district_name,
            disabled: false,
            leaf: level >= 2
          }))

          for (let i in _nodes) {
            // if(self.ruleForm.tableData&&self.ruleForm.tableData.length==0){
            // 	resolve(_nodes);
            // 	return
            // }
            // 设置运费地址为后tableData有值，从而来设置哪些省不能选
            for (let k in self.ruleForm.tableData) {
              // 禁选已设置地区

              for (let m in self.ruleForm.tableData[k].cid) {
                if (
                  _nodes[i].label == self.ruleForm.tableData[k].cid[m].label
                ) {
                  if (self.editName != '') {
                    self.$set(_nodes[i], 'disabled', false)
                  } else {
                    self.$set(_nodes[i], 'disabled', true)
                  }
                }
              }
            }
          }
          resolve(_nodes)
        }
      },
      sentList: '',
      sub: '',
      newRow: [],
      cascader: [['1', '2', '3']],
      cascader2: [['1', '2', '3']],
      cascader3: [['1', '35', '402']],
      dialogVisible2: false, //查看详细地区弹窗
      citiesList: [], //查看对应地区
      selectArrs: [],
      options: [],

      id: '',
      countriesList: [],
      languages: [],
      lang_code: null,
      country_num: null,
      laikeCurrencySymbol:'￥',
      ruleForm: {
        //省市区
        province: '',
        city: '',
        area: '',
        address: '', //默认地址
        name: '', //模板名称
        type: '0', //计算方式0件1重量
        is_package_settings: '0',
        package_settings: '',
        is_no_delivery: '0',
        list: [],
        // 默认运费
        num1: '',
        num2: '',
        num3: '',
        num4: '',
        lang_code: '',
        country_num: '',
        tableData: []
      },
      optionsCope:[],
      //省市级联集
      shengList: {},
      shiList: {},
      xianList: {},

      yixuanT: [],
      yixuanD: [],
      mycities: [],
      no_delivery: [],
      editName: '', //编辑标识
      rule: {
        country_num: [
          { required: true, message: this.$t('qxzssgj') , trigger: 'blur' }
        ],
        lang_code: [
          { required: true, message: this.$t('qxzyz') , trigger: 'blur' }
        ],
        name: [
          {
            required: true,
            message: this.$t('addtemplate.qsryfmb'),
            trigger: 'blur'
          }
        ],
        defaultFreight: [
          {
            validator: checkFreight,
            required: true,
            trigger: 'blur'
          }
        ],
        warehouseAddress: [
          {
            validator: checkAddress,
            required: true,
            trigger: 'blur'
          }
        ]
      },
      dialogedit: false,
      dialogpic: false,
      dialogarea: false,
      checkAll: false,
      checkedCities: [],
      cities: [],
      isIndeterminate: false,
      // 弹框数据
      dialogVisible: false,
      ruleForm2: {
        freight: null
      },
      rules2: {
        // freight: [
        //   { required: true, message: '请填设置运费', trigger: 'blur' }
        // ]
      },
      disabled: false,

      isCN:true,//是否为中国地区 false的时候不显示 不配送地区和指定地区运费

      removeImg: require("../../../../assets/imgs/sha.png"),
      queryAddressIdList: [] ,// 不配送地区 的所有地区
      addressList: [], // 不配送地区 的 最终结果
      defaultProps: {
        children: 'children',
        label: 'district_name',
        aid: 'id',
      },
      nodeKey:''
    }
  },

  async created () {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    this.ruleForm.lang_code = this.LaiKeCommon.getUserLangVal()
    this.updateIsCN(this.ruleForm.lang_code);
    await this.getCountrys()
    if (this.$route.query.id) {
      this.id = this.$route.query.id
      this.getdata(this.id) //查看模板调用的函数
    }
    if (this.$route.query.name == 'view') {
      this.$router.currentRoute.matched[2].meta.title = '查看模板'
      this.disabled = true
    } else {
      // this.getSheng();
      this.$router.currentRoute.matched[2].meta.title = '添加模板'
    }
    this.getSelectCountrys();
    this.getLanguages();
  },

  watch: {
    // 添加模板时候切换计算方式需要输入框的重置值
    'ruleForm.type': {
      handler: function () {
        if (this.$route.query.name != 'view') {
          this.ruleForm.num1 = ''
          this.ruleForm.num2 = ''
          this.ruleForm.num3 = ''
          this.ruleForm.num4 = ''
          if (this.ruleForm.tableData.length > 0) {
            let data = this.ruleForm.tableData
            for (let key in data) {
              data[key].one = ''
              data[key].freight = ''
              data[key].two = ''
              data[key].Tfreight = ''
            }
          }
        }
      }
    },
    //取出选中地区的 三级id，改变 不发货地区数组
    "ruleForm.tableData":{
      handler:function(newVal,lodVal){
        let idArr=[]
        newVal.forEach(val=>{
          if(val.canshuID && val.canshuID.length>0){
            val.canshuID.filter(v=>{
                // 如果没有 三级  则 添加 二级
                idArr.push(v[2]||v[1])
            })
          }
        })
        // 根据 三级 二级 id 过滤选中的地区
        const notAddressList = this.filterData(this.optionsCope,idArr)
        const optionsList  = notAddressList.filter(v=>v.children && v.children.length>0)

        this.$nextTick(() => {
          ++this.cascader_count1;
          ++this.cascader_count2;
          this.$set(this, 'options', [...optionsList]);
        });


        console.log('过滤后的结果',this.options)
      },
      deep:true
    },
    checkAll () {
      if (this.checkAll === true) {
        this.cities.forEach(item => {
          this.checkedCities.push(item.district_name)
        })
      }
    },

    checkedCities: {
      handler: function () {
        if (this.checkedCities.length === this.cities.length) {
          this.checkAll = true
          this.isIndeterminate = false
        }
      }
    },
    // 被 改变时 重新组合不配送地区名称
    addressList:{
      handler:function(newVal,lodVal){
        this.no_delivery = this.nonDeliveryListName(newVal)
      },
      deep:true
    },
    lang_code:{
      handler:function(newVal,lodVal){
        if(newVal !=  'zh_CN' && newVal !=  'zh_TW' && newVal !=  'zh_HK' )
        {
          this.isCN  = false
        }
        else if(newVal ==  'zh_CN' || newVal ==  'zh_TW'|| newVal ==  'zh_HK')
        {
          this.isCN  = true
        }
      },
      deep:true
    }
  },

  mounted () {

  },
  methods: {
    updateIsCN(langCode) {
      if (langCode !== 'zh_CN' && langCode !== 'zh_TW' && langCode !== 'zh_HK') {
        this.isCN = false;
      } else {
        this.isCN = true;
      }
    },
  // 过滤 选中的子地区
  filterData(arr, idsToFilter) {
		 const numberArray = idsToFilter.map(Number)
		 return arr.map(item => {
		       // 递归过滤子数组
		       const filteredList = item.children ? this.filterData(item.children,numberArray) : [];
		       // 返回一个新的对象，过滤掉需要删除的元素
		       const arr = {
		         ...item,
		         children: filteredList.filter(subItem => subItem && !numberArray.includes(subItem.id))
		       };
			   return arr
		     })
		},
    // 获取国家列表
    async getSelectCountrys() {
      const res = await this.LaiKeCommon.getCountries()
      this.countriesList = res.data.data
    },

    getIds(value) {
      this.country_num = value
    },

    // 获取语言列表
    async getLanguages() {
      const res = await this.LaiKeCommon.getLanguages();
      this.languages = res.data.data
    },

    getlangCode(value) {
      this.lang_code = value
    },

    // 获取选中地址的 一级 id
    handleChange(value) {
      const numberAddressIdList = value.map(v=>v[0])
      const oneAddressIdList = [...new Set(numberAddressIdList) ]
      this.queryAddressIdList = this.options.filter(v=> oneAddressIdList.includes(v.id) )
    },

    // 删除
    deleteNode(node){
      this.nodeKey = node.data.district_pid
      let address = JSON.parse(JSON.stringify(this.addressList))
      this.delItmeByCName(address,node.label)
      this.addressList = address.filter(v=> v.children && v.children.length>0)

      this.cascader2 = this.nonDeliveryListID(this.addressList)

    },
    // 不选地区 - 查找删除操作
    delItmeByCName(delArr,delName){
      for (let i = delArr.length - 1; i >= 0; i--) {
        const item = delArr[i];

        // 如果有 children，则递归调用
        if (item.children) {
            this.delItmeByCName(item.children, delName);
        }

        // 如果 district_name 等于 delName，则删除该项
        if (item.district_name === delName) {
            delArr.splice(i, 1);
        }
    }
    },
    tableHeaderColor ({ row, column, rowIndex, columnIndex }) {
      if (rowIndex === 0) {
        return 'background-color: #f4f7f9;height: 50px;text-align:center;padding:0;'
      }
    },
    // 获取城市信息
    async getCountrys () {
      const res = await getRegion({
        api: 'admin.AdminFreight.cityInfo'
      })

      if (res && res.data.code == 200) {
        this.options = res.data.data.list
        this.optionsCope = JSON.parse(JSON.stringify(this.options))

        console.log(this.options)
      }
    },
    async getGClist (level, parentId) {
      // 获取省份地址

      const res = await cascade.getSheng(level, parentId)
      if (res.data.code == 200 && res.status == 200) {
        // const records = res.data.records;
        const records = res.data.data
        return Promise.resolve(records)
      }
    },

    //设置运费
    setPrice () {
      this.cascader = []
      this.dialogpic = true
      // this.getRegions()
    },

    // 获取省级
    async getSheng () {
      const res = await cascade.getSheng()
      this.shengList = res.data.data
    },

    //打开选择地区面板
    setRegion () {
      this.dialogarea = true
    },
    //选择
    panelChange (row, editName) {
      console.log(row, editName)
    },
    // 查看已选省市县
    dialogShow2 (row, index) {
      console.log('show', row, index)
      this.dialogVisible2 = true

      this.citiesList= row.name.split(',')

      console.log(this.filterSelected(row.name.split(',')))
    },
    handleClose2 (done) {
      this.dialogVisible2 = false
    },
    //编辑省份
    edit (row, index) {
      console.log(row)
      console.log('edit:', row, index)
      this.dialogedit = true
      this.cascader = row.canshuID
      this.cascader3 = row.canshuID
      this.sub = index
    },
    //移除table行
    Delete (index, row) {
      this.$refs['cascader'].clearCheckedNodes()
      this.$refs['cascader'].activePath = []
      this.ruleForm.tableData.splice(index, 1)
    },
    //不发地区确认
    submit () {
      let arrList = this.$refs['cascader2'].getCheckedNodes()

      if (arrList.length == 0) {
        this.$message({
          message: this.$t('addtemplate.text2'),
          type: 'error',
          offset: 102
        })
        return
      }
      console.log(arrList, '-------')
      this.nonDeliveryArea(arrList)

      var arr = []
      var canshu = []
      for (let i = 0; i < arrList.length; i++) {
        var obj = []
        obj = arrList[i].pathLabels[0]
        if (arrList[i].pathLabels.length == 3) {
          canshu.push(arrList[i].pathLabels.join('-'))
        }
        arr.push(obj)
      }
      var newArr = [...new Set(arr)].join(',')
      this.sentList = newArr
      console.log('sentList', this.sentList)
      // console.log('sentList', this.sentList, 'canshu:', canshu)
      // this.no_delivery = canshu
      // 当有不发地区时，是否不配送参数为1
      if (this.sentList != '') {
        this.ruleForm.is_no_delivery = 1
      } else {
        this.ruleForm.is_no_delivery = 0
      }

      this.dialogarea = false
      // return newArr
      return canshu
    },
    /**
     * 整理 不配送地区 名称
     * @param list 地区
     * @param nameText 地区名称
     */
    nonDeliveryListName(list,nameText){
      let  nameList = []
      list.forEach(v=>{
          if(v.children){
            nameList = nameList.concat(this.nonDeliveryListName(v.children, nameText ? nameText + '-' + v.district_name: v.district_name))
          }else{
            nameList.push(`${nameText}-${v.district_name}`)
          }
      })
      return nameList
    },

    // 整理 不发货地区
    nonDeliveryArea(value){
      let  addressList =JSON.parse(JSON.stringify(this.queryAddressIdList))
      // 只找 三级地址id
      const threeIdsList = value.filter(v=>v.level == 3).map(v=>v.value)

      this.addressList = this.getSelectedData(addressList,threeIdsList)

      console.log('最终 不选择地区的 结果：',this.addressList  )
    },
    /**
     * 根据三级id 显示不发货地区
     * @param addressList  地区数组
     * @param threeIdsList 存放三级id 的数组
     * @param normalTetx 根据什么 关键字 查找; 默认 GroupID 地区id
     */
    getSelectedData(addressList, threeIdsList,normalTetx = 'id') {
      let selectedData = [];
      addressList.forEach(v => {
          // 如果当前节点有子节点
          if (v.children && v.children.length > 0) {
              // 递归处理子节点
              const childSelectedData = this.getSelectedData(v.children, threeIdsList,normalTetx);
              // 如果子节点中有被选中的数据，保留当前节点并将子节点的数据合并
              if (childSelectedData.length > 0) {
                  selectedData.push({ ...v, children: childSelectedData });
              }
          } else {
              // 当前节点是叶子节点，检查是否在 threeIdsList 中
              if (threeIdsList.includes(v[normalTetx])) {
                  selectedData.push(v);
              }
          }
      });
      return selectedData;
    },

    //设置运费确认
    setmit () {
      let arrList = this.$refs['cascader'].getCheckedNodes()
      if (arrList.length == 0) {
        this.$message({
          message: this.$t('addtemplate.text2'),
          type: 'error',
          offset: 102
        })
        return
      }
      console.log(arrList)
      console.log(arrList, '    666666666666   ', arrList.length)
      var arr = [] //省份
      var arrID = [] //省份id
      var canshu = [] //县级
      var canshuID = [] //县级id
      for (let i = 0; i < arrList.length; i++) {
        var obj = []
        var objID = []
        obj = arrList[i].pathLabels[0] //只要省级
        // obj = arrList[i].pathLabels[arrList[i].pathLabels.length - 1] //回显到县级
        // objID= arrList[i].path[0]	//省id
        objID = arrList[i].path[0] //区id

        // console.log(obj, '     ', arrList[i].pathLabels)

        canshuID.push(arrList[i].path)
        if (arrList[i].pathLabels.length == 3) {
          canshu.push(arrList[i].pathLabels.join('-'))
        }
        arrID.push(objID)
        arr.push(obj)
      }
      console.log('canshu:', canshu, canshu.toString(), 'canshuID:', canshuID)
      var newArr = [...new Set(arr)].join(',')
      var newArrID = [...new Set(arrID)].join(',')
      this.dialogpic = false
      let list = {
        // cid: this.newRow, //
        cid: newArrID.toString(),
        // cid:"402,403,404",
        picList: newArr, //[北京市，上西市]
        name: canshu.toString(), //【'北京市-市辖区-东城区'，'北京市-市辖区-东城区'】
        canshuID: canshuID, //[[1,35,402],[1,25,405]]
        one: '',
        freight: '',
        two: '',
        Tfreight: ''
      }
      this.ruleForm.tableData.push(list)
      console.log('tableData', this.ruleForm.tableData, list)
    },
    //编辑确认
    editmit () {
      let arrList = this.$refs['cascader3'].getCheckedNodes()
      if (arrList.length == 0) {
        this.$message({
          message: this.$t('addtemplate.text2'),
          type: 'error',
          offset: 102
        })
        return
      }
      console.log(arrList)
      var arr = []
      var arrID = []

      var canshu = []
      var canshuID = []
      for (let i = 0; i < arrList.length; i++) {
        var obj = []
        var objID = []
        obj = arrList[i].pathLabels[0] //省
        objID = arrList[i].path[0] //省id
        // obj = arrList[i].pathLabels[arrList[i].pathLabels.length - 1] //回显到县级

        canshuID.push(arrList[i].path)
        if (arrList[i].pathLabels.length == 3) {
          canshu.push(arrList[i].pathLabels.join('-'))
        }
        arrID.push(objID)
        arr.push(obj)
      }
      var newArr = [...new Set(arr)].join(',')
      var newArrID = [...new Set(arrID)].join(',')
      // console.log("arrList:",arrList)
      this.dialogedit = false
      this.ruleForm.tableData[this.sub].picList = newArr
      this.ruleForm.tableData[this.sub].cid = newArrID
      this.ruleForm.tableData[this.sub].name = canshu
      this.ruleForm.tableData[this.sub].canshuID = canshuID
      // console.log("this.sub:", this.sub, "this.ruleForm.tableData:", this.ruleForm.tableData);
    },
    // 获取城市列表
    getRegions (e) {
      request({
        method: 'post',
        params: {
          api: 'admin.goods.getRegion',
          level: 2
        }
      }).then(res => {
        this.cities = res.data.data
        this.mycities = res.data.data
      })
      // this.cities = res.data.data.map(item => {
      //   return item.g_CName
      // })
    },
    // Regfreight(){
    //   if(Number(this.ruleForm2.freight) < 0){
    //     this.$message({
    //       message:'运费不能为负数',
    //       type:'warning',
    //       offset:102
    //     })
    //     return this.ruleForm2.freight = 0
    //   }else return
    // },

    // 弹框方法
    dialogShow () {
      this.isIndeterminate = false
      if (this.cities.length == 0 && this.ruleForm.list.length !== 0) {
        this.$message({
          message: this.$t('addtemplate.text1'),
          type: 'error',
          offset: 102
        })
      } else {
        this.dialogVisible = true
      }
      this.checkedCities = []

      if (this.ruleForm.list.length === 0) {
        this.getRegions().then(() => {
          this.checkAll = false
        })
      }
    },
    // 关闭选择城市弹窗，清楚表格选择城市的状态
    handleClose (done) {
      this.dialogVisible = false
      this.$refs['ruleForm2'].clearValidate()
    },
    yixuan () {
      console.log(this.yixuanT, this.yixuanD)
      // 筛选出还未选择的省
      this.cities = this.mycities.filter(item => {
        if (!this.yixuanT.includes(item.district_name)) {
          return item.district_name
        }
      })

      this.cities = this.cities.filter(item => {
        if (!this.yixuanD.includes(item.id)) {
          return item.id
        }
      })
      // this.cities
      console.log(this.cities)
    },
    // 保存运费规则
    determine (formName) {
      this.$refs[formName].validate(async valid => {
        if (this.checkedCities.length == 0) {
          this.$message({
            message: this.$t('addtemplate.text2'),
            type: 'error',
            offset: 102
          })
        } else {
          if (valid) {
            try {
              var obj = {
                // one: this.ruleForm2.freight?this.ruleForm2.freight:'2',
                // freight: this.ruleForm2.freight,
                // two: this.ruleForm2.freight,
                // Tfreight:'',
                one: '0',
                // Tone:'0',
                freight: '0',
                two: '0',
                Tfreight: '0',
                name: this.checkedCities.join()
              }
              this.ruleForm.list.push(obj)
              console.log(this.checkedCities)
              // this.cities = this.cities.filter(item => {//过滤选出未选中的省份（移至yixuan（）函数中处理了）
              //   if (!this.checkedCities.includes(item.district_name)) {
              //     return item.district_name
              //   }
              // })
              this.yixuanT = [...this.yixuanT, ...this.checkedCities] //获取已选的省份
              this.yixuan()
              ;(this.ruleForm2.freight = null), //保存后清空
                this.handleClose()
            } catch (error) {
              // this.$message({
              //   message: '数据名称不能为空',
              //   type: 'error',
              //   offset: 102
              // })
            }
          } else {
            // this.$message({
            //   message: '数据名称不能为空',
            //   type: 'error',
            //   offset: 102
            // })
            // return false;
          }
        }
      })
    },

    // 添加运费
    submitForm (formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm, '=======')
        let arr = []
        this.no_delivery.forEach(element => {
          // arr.push([element + ''])
          arr.push(element + '')
        })
        console.log(this.ruleForm.list)
        if (valid) {
          try {
            let myerr = false
            this.ruleForm.tableData.map(item => {
              if (Array.isArray(item.name)) {
                item.name = item.name.join(',')
              }
            })
            let defaultFreights = {
              num1: this.ruleForm.num1,
              num2: this.ruleForm.num2,
              num3: this.ruleForm.num3,
              num4: this.ruleForm.num4
            }
            // 剔除掉cnashuID（该值只用作编辑回显运费，添加运费时不需要）
            // for (const key in this.ruleForm.tableData) {
            //   delete this.ruleForm.tableData[key].canshuID;
            // }
            let isTag = true
            this.ruleForm.tableData.map(item => {
              if (!item.one || !item.two || !item.freight || !item.Tfreight) {
                isTag = false
              }
            })
            if (!isTag) {
              this.$message({
                message: this.$t('addtemplate.text3'),
                type: 'error',
                offset: 102
              })
              return
            }

            const threeIdsList = this.getThreeIdsList(this.addressList)

            let data = {
              api: 'admin.AdminFreight.addAdminFreight',
              fid: this.id,
              name: this.ruleForm.name,
              country_num: this.ruleForm.country_num,
              lang_code: this.ruleForm.lang_code,
              type: this.ruleForm.type,
              isPackageSettings: this.ruleForm.is_package_settings,
              packageSettings: this.ruleForm.package_settings,
              isNoDelivery: this.ruleForm.is_no_delivery,
              noDelivery: JSON.stringify(arr),
              hiddenFreight: JSON.stringify(this.ruleForm.tableData),
              defaultFreight: JSON.stringify(defaultFreights),
              threeIdsList:threeIdsList
            }

            let { entries } = Object

            let formData = new FormData()
            for (let [key, value] of entries(data)) {
              formData.append(key, value)
            }

            console.log('formData:', formData)
            addFreight(formData).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('zdata.tjcg'),
                  type: 'success',
                  offset: 102
                })
                this.$router.go(-1)
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
    // 只返回三级id
    getThreeIdsList(list){
      let threeisList = []
      list.forEach(v=>{
          if(v.children){
            threeisList = threeisList.concat(this.getThreeIdsList(v.children))
          }else{
            threeisList.push(v.id)
          }
      })
      return threeisList
    },
    getdata (id) {
      let that = this
      request({
        method: 'post',
        params: {
          api: 'admin.AdminFreight.AdminGetFreightInfo',
          fid: id
        }
      }).then(res => {
        console.log(res)
        let result = res.data.data
        this.ruleForm.name = result.name

        this.ruleForm.address = result.address
        this.ruleForm.tableData = result.freight
        this.ruleForm.type = result.type.toString()

        this.ruleForm.num1 = result.default_freight.num1
        this.ruleForm.num2 = result.default_freight.num2
        this.ruleForm.num3 = result.default_freight.num3
        this.ruleForm.num4 = result.default_freight.num4

        this.getSelectCountrys().then(() => {
          if(result.country_num) {
            this.ruleForm.country_num = parseInt(result.country_num)
          }
        })

        this.getLanguages().then(() => {
          if(result.lang_code) {
            this.ruleForm.lang_code = result.lang_code
            this.updateIsCN(result.lang_code);
          }
        })

        if (result.no_delivery) {
          this.sentList = result.no_delivery.join(',')
          let options = JSON.parse(JSON.stringify(this.options))

          let oneNameList = []
          // 获取选中的地区的
          result.no_delivery.forEach(v=>{
            oneNameList.push(v.split('-')[0])
          })
          oneNameList =  Array.from( new Set(oneNameList))

          // 过滤 一级地区，只获取到 当前选中的 一级地区
          options =  options.filter(v=> oneNameList.includes(v.district_name))

          let addRess = []
          oneNameList.forEach((val,ind)=>{
            addRess.push([])
            result.no_delivery.forEach(i=>{
                if(i.includes(val)){
                  addRess[ind].push(i)
                }
            })
          })
          // 提取路径并查找对象
          let data = [];
          for (let pathArray of addRess) {
            let path = pathArray[0];
            let foundObject = this.findObject([path], options);
            if (foundObject) {
              data.push(foundObject);
            }
          }
          this.addressList =data
          //this.cascader2 = this.nonDeliveryListID(this.addressList)
        }
        setTimeout(() => {
          this.yixuanD = [...this.yixuanD, ...this.no_delivery]
          this.yixuan()
        }, 1000)

      })
    },
      /**
     *  整理 不配送地区 id
     * @param list 地区
     * @param id 地区id
     * 返回一个 地区id的 二维数组
     */
     nonDeliveryListID(list,id){
      let  nameList = []
      list.forEach(v=>{
          if(v.children){
            nameList = nameList.concat(this.nonDeliveryListID(v.children, id ? `${id},${v.id}`: v.id))
          }else{
            // 转成 number 类型数组
            nameList.push(`${id},${v.id}`.split(',').map(Number))
          }
      })
      return  nameList
    },
     /**
     * 根据名称字符串显示 回显不发货地区
     * @param pathArray 地区名称 数组
     * @param data 地区
     */
     findObject(pathArray, data) {
      for (let item of data) {
          // 创建完整路径
          let fullPath = item.name;
          if (item.children) {
              fullPath += '-' + item.children.map(child => child.district_name).join('-');
          }

          // 检查是否有匹配的路径
          for (let path of pathArray) {
              let paths = path.split('-');
              if (paths[0] === item.district_name) {
                  let current = item;
                  let found = true;

                  for (let i = 1; i < paths.length; i++) {
                      let childFound = false;
                      if (current.children) {
                          for (let child of current.children) {
                              if (child.district_name === paths[i]) {
                                  current = child;
                                  childFound = true;
                                  break;
                              }
                          }
                      }
                      if (!childFound) {
                          found = false;
                          break;
                      }
                  }

                  if (found) {
                      return item; // 找到匹配的对象
                  }
              }
          }

        // 递归查找子项
        if (item.children) {
            let result = this.findObject(pathArray, item.children);
            if (result) {
                return result;
            }
        }
     }
        return null; // 未找到
    },
    filterSelected (list) {
      // let list=['北京市-市辖区-西城区', '北京市-市辖区-朝阳区', '天津市', '天津市-市辖区', '天津市-市辖区-和平区', '天津市-市辖区-河东区','山西省-太原市', '山西省-太原市-小店区', '山西省-太原市-迎泽区']
      // list.forEach(item=>{if(item.indexOf('-')==-1){list.filter(items=>{if(items.indexOf(item)!=-1){return items}})}})

      // list.forEach(item=>{if(item.split('-').length<3){console.log(item)}})
      // let list=['北京市-市辖区-西城区', '北京市-市辖区-朝阳区', '天津市', '天津市-市辖区', '天津市-市辖区-和平区', '天津市-市辖区-河东区', '天津市-市辖区-河西区', '天津市-市辖区-南开区', '天津市-市辖区-河北区', '天津市-市辖区-红桥区', '天津市-市辖区-东丽区', '天津市-市辖区-西青区', '天津市-市辖区-津南区', '天津市-市辖区-北辰区', '天津市-市辖区-武清区', '天津市-市辖区-宝坻区', '天津市-市辖区-滨海新区', '天津市-市辖区-宁河区', '天津市-市辖区-静海区', '天津市-市辖区-蓟州区', '山西省-太原市', '山西省-太原市-小店区', '山西省-太原市-迎泽区', '山西省-太原市-杏花岭区', '山西省-太原市-尖草坪区', '山西省-太原市-万柏林区', '山西省-太原市-晋源区']
      // let list=['河北省-石家庄市', '河北省-石家庄市-长安区', '河北省-石家庄市-桥西区']
      let arr = []
      let newArr = []
      let newArrT = []
      let newArrF = []
      let newArrB = []
      if (list && list.length > 0) {
        list.forEach((item, index) => {
          if (item.indexOf('-') == -1) {
            list.filter(items => {
              if (items.indexOf(item) != -1) {
                if (items.indexOf('-') == -1) {
                  arr.push(items)
                }
                // return items
              } else {
                newArr.push(items)
              }
            })
          } else {
            newArrT.push(item)
          }
        })

        newArr.forEach(item => {
          if (item.split('-').length < 3) {
            newArrF.push(item)
          }
        })
        newArrB.push(...newArrF, ...arr)
        // newArrB.push()
        newArrF.forEach(item => {
          newArr.filter(items => {
            if (items.indexOf(item) == -1) {
              newArrB.push(items)
            }
          })
        })
        // console.log("arr:",arr,'  newArr',newArr,'   newArrT',newArrT,"   newArrF：",newArrF," newArrB:",newArrB)
      }
      // console.log(list)
      return newArrB
    }
  }
}
</script>

<style scoped lang="less">
/deep/.el-dialog__header{
  position: sticky !important;
  top: 0!important;
  background-color: white!important;
}
.edit_one {
  color: #2890ff;
  cursor: pointer;
}

.send-areas {
  width: 519px;
  background: #f4f7f9;
  border-radius: 4px;
  box-sizing: border-box;
  padding: 10px 20px 10px 20px;
  margin-top: 20px;
}

.send_pic {
  display: flex;
  justify-content: space-between;
  align-items: center;
  /deep/ .el-tree {
    background-color: #f4f7f9;
    width: 100%;
  }

  /deep/ .el-tree-node__content{
      p{
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .removeImg {
          float: right;
          height: 15px;
        }
      }
      display: flex;
      width: 100%;

      // justify-content: space-between;

  }
}

.bt_price {
  border: 1px solid #2890ff;
  color: #2890ff;
}

// .bt_top {
//   margin-top: 20px;
// }

.num_input {
  width: 265px;
}

.price_input {
  width: 474px;
}
.model_price {
  width: 580px;
  background: rgba(244, 247, 249, 1);
  border-radius: 2px;
  padding: 14px 20px;
  box-sizing: border-box;
}

.container {
  width: 100%;
  height: 737px;
  background-color: #fff;
  overflow: auto;
  border-radius: 4px;

  .header {
    width: 100%;
    height: 60px;
    line-height: 60px;
    padding-left: 20px;
    border-bottom: 1px solid #e9ecef;

    span {
      font-size: 16px;
      font-weight: 400;
      color: #414658;
    }
  }

  /deep/.el-form {
    width: 100%;
    display: flex;
    align-items: center;
    flex-flow: wrap;
    .notice {
      padding: 20px 0 18px 0px;
      display: flex;
      flex-direction: column;
      // margin-left: 50%;
      // transform: translateX(-50%);
      margin: 0 auto;

      .cascadeAddress {
        .el-form-item__content {
          .cascadeAddress-block {
            .el-select {
              width: 132px;

              &:not(:last-child) {
                margin-right: 10px !important;
              }

              .el-input {
                width: 132px;
              }

              .el-input {
                width: 132px;

                input {
                  width: 132px;
                }
              }
            }
          }
        }
      }

      .title {
        //margin-top: 20px;

        input {
          width: 580px;
          height: 40px;
        }

        .el-form-item__label {
          font-weight: normal;
        }

        .el-form-item__label {
          color: #414658;
        }

        // .el-form-item__content {
        // 	display: flex;

        // 	input {
        // 		width: 420px;
        // 		height: 40px;
        // 	}
        // }
      }

      .dictionary-list {
        width: 800px;
        border-radius: 4px;
        margin-left: 90px;

        /deep/.el-table {
          width: 800px !important;

          .el-table__header-wrapper {
            width: 800px;
            background-color: #f4f7f9 !important;

            .el-table__header {
              width: 800px !important;
              background-color: #f4f7f9 !important;
            }

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
            width: 800px;
            background-color: #f4f7f9;

            .el-table__body {
              width: 800px !important;
            }

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

          .OP-button {
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

            .OP-button-top {
              button {
                &:not(:first-child) {
                  margin-left: 8px !important;
                }
              }
            }
          }
        }
      }
    }

    .footer-button {
      margin-left: 90px;

      button {
        width: 70px;
        height: 40px;
      }
    }
  }

  .dialog-area {
    /deep/.el-dialog {
      width: 617px;
      // height: 400px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      overflow: auto;
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
        padding: 40px;
        border-bottom: 1px solid #e9ecef;
      }
      .el-dialog__footer {
        padding-top: 15px !important;
        padding-bottom: 15px !important;
      }
    }
  }

  .dialog-pic {
    /deep/.el-dialog {
      width: 617px;
      // height: 400px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      overflow: auto;
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
        padding: 40px;
        border-bottom: 1px solid #e9ecef;
      }
      .el-dialog__footer {
        padding-top: 15px !important;
        padding-bottom: 15px !important;
      }
    }
  }

  .dialog-edit {
    /deep/.el-dialog {
      width: 617px;
      // height: 400px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      overflow: auto;
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
        padding: 40px;
        border-bottom: 1px solid #e9ecef;
      }
      .el-dialog__footer {
        padding-top: 15px !important;
        padding-bottom: 15px !important;
      }
    }
  }

  .dialog-block {
    // 弹框样式
    /deep/.el-dialog {
      width: 580px;
      height: 445px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      overflow: auto;
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
        padding: 40px !important;
        font-size: 14px;
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
}
/deep/.el-input__inner {
  padding-right: 0;
  border-color: #d5dbe8 !important;
}

.calculation {
  margin-top: 0 !important;
}

.bgColor:hover {
  border: none;
  color: #2890ff;
  border: 1px solid #2890ff;
  background-color: #ffffff;
}

/deep/.input_freight {
  text-align: center !important;
  .el-input__inner {
    text-align: center !important;
    padding: 0 !important;
  }
}

/deep/.del_select:hover {
  background-color: #ffffff;
  color: #2890ff;
  border: 1px solid #2890ff;
}

/deep/ .el-table {
  &.active {
    margin-top: 0 !important;
  }
}

.mgleft:hover {
  background-color: #2890ff;
  color: #ffffff;
  opacity: 0.8;
}
</style>
