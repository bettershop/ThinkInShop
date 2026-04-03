<template>
  <div class="container">
    <div class="btn-nav">
      <l-tab
        ref="navLabel"
        :nav_label_type="nav_label_type"
        :nav_label_list="nav_label_list"
        @_choose="nav_choose"
      >
      </l-tab>
    </div>
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">

          <el-select
            class="select-input"
            v-model="inputInfo.lang_code"
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

          <el-cascader
            v-model="inputInfo.goodsClass"
            class="select-input"
            ref="myCascader"
            :placeholder="$t('physicalgoods.qxzspfl')"
            :options="classList"
            :props="{ checkStrictly: true }"
            @change="changeProvinceCity"
            clearable
          >
          </el-cascader>
          <el-select
            class="select-input"
            v-model="inputInfo.brand"
            @change="changeBrand"
            :placeholder="$t('physicalgoods.qxzsppp')"
          >
            <el-option
              v-for="item in brandList"
              :key="item.brand_id"
              :label="item.brand_name"
              :value="item.brand_id"
            >
            </el-option>
          </el-select>
          <el-select
            class="select-input"
            v-model="inputInfo.state"
            :placeholder="$t('physicalgoods.qxzspzt')"
          >
            <el-option
              v-for="item in statusList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>

          <el-select
            class="select-input"
            v-model="inputInfo.location"
            :placeholder="$t('physicalgoods.qxzxswz')"
          >
            <el-option
              v-for="item in localList"
              :key="item.value"
              :label="item.text"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-select
            class="select-input"
            v-model="inputInfo.label"
            :placeholder="$t('physicalgoods.qxzspbq')"
          >
            <el-option
              v-for="item in labelList"
              :key="item.value"
              :label="item.name"
              :value="item.id"
            >
            </el-option>
          </el-select>
          <l-input
            clearable
            class="input-name"
            v-enter="demand"
            v-model="inputInfo.name"
            :placeholder="$t('physicalgoods.qsrspmc')"
          ></l-input>
        </div>
        <div class="btn-list">
          <l-button class="fontColor" @click="reset">{{
            $t('DemoPage.tableExamplePage.reset')
          }}</l-button>
          <l-button
            class="bgColor"
            type="primary"
            v-enter="demand"
            @click="demand"
            >{{ $t('DemoPage.tableExamplePage.demand') }}</l-button
          >
          <el-button
            v-has-permi="'export/goods'"
            class="bgColor export"
            type="primary"
            @click="dialogShow"
            >{{ $t('DemoPage.tableExamplePage.export') }}</el-button
          >
        </div>
      </div>
    </div>
    <div class="jump-list">
      <l-button
        v-has-permi="'releasevirtual'"
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="releaseGoods"
        >{{ $t('physicalgoods.fbsp') }}</l-button
      >

      <l-button
        v-has-permi="'batchUnshelveGoods'"
        class=" laiketui laiketui-xiajia"
        type="primary"
        @click="shelves(3)"
        >{{ $t('physicalgoods.xj') }}</l-button
      >
      <l-button
        v-has-permi="'batchShelvesGoods'"
        class=" laiketui laiketui-shangjia"
        type="primary"
        @click="shelves(2)"
        >{{ $t('physicalgoods.sj') }}</l-button
      >
      <el-button
        v-has-permi="'butDisplayPosition'"
        class="laiketui laiketui-shangjia"
        type="primary"
        @click="butEventer($t('physicalgoods.xzwz'))"
        >{{ $t('physicalgoods.xzwz') }}</el-button
      >
      <l-button
        v-has-permi="'batchDeleteGoods'"
        class="fontColor"
        @click="delAll"
        :disabled="is_disabled"
        :icon="'el-icon-delete'"
        >{{ $t('physicalgoods.plsc') }}</l-button
      >
      <div class="show-goods">
        <div class="center_b">
          <span>{{ $t('physicalgoods.sfxsxj') }}</span>
          <el-switch
            v-model="value"
            :active-value="1"
            :inactive-value="0"
            @change="switchs()"
            active-color="#13ce66"
            inactive-color="#d5dbe8"
          >
          </el-switch>
        </div>
      </div>
    </div>
    <div class="menu-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
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
        <el-table-column type="selection"> </el-table-column>
        <el-table-column prop="id" :label="$t('physicalgoods.spbh')">
        </el-table-column>
        <!-- <el-table-column prop="imgurl" :label="$t('physicalgoods.sptp')">
          <template slot-scope="scope">
            <img :src="scope.row.imgurl" alt="" />
          </template>
        </el-table-column> -->
        <el-table-column
          prop="product_title"
          :label="$t('physicalgoods.spxx')"
          width="200"
        >
          <template slot-scope="scope">
            <div class="goodsDetail">
              <div class="goodsImg">
                <img :src="scope.row.imgurl" alt="" @error="handleErrorImg" />
              </div>
              <div class="goodsInfo">
                <p class="product_title">{{ scope.row.product_title }}</p>
                <div
                  class="label_list"
                  v-if="scope.row.labelList && scope.row.labelList.length"
                >
                  <p
                    v-for="(item, index) in scope.row.s_type_list"
                    :style="{ background: item.color }"
                    :key="index"
                  >
                    {{ item.name }}
                  </p>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="lang_name"
          :label="$t('physicalgoods.spyz')"
          width="100"
        >
        </el-table-column>
        <el-table-column
          prop="pname"
          :label="$t('physicalgoods.spfl')"
          width="200"
        >
        </el-table-column>
        <el-table-column prop="brand_name" :label="$t('physicalgoods.pp')">
        </el-table-column>
        <el-table-column prop="price" :label="$t('physicalgoods.jg')">
        </el-table-column>
        <el-table-column :label="$t('physicalgoods.spzt')">
          <template slot-scope="scope">
            <div class="div_centent">
              <span
              class="status_name"
              :class="{
                actives: scope.row.status === 1 || scope.row.status === 3,
                active: scope.row.status === 2
              }"
            >
              {{ getStatusName(scope.row.status) }}
            </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('physicalgoods.ssdp')">
        </el-table-column>
        <el-table-column prop="volume" :label="$t('physicalgoods.xl')">
        </el-table-column>
        <el-table-column prop="showName" :label="$t('physicalgoods.xswz')">
          <!-- <template slot-scope="scope">
            <span>{{
              scope.row.showAdrNameList[0] == "0"
                ? $t("physicalgoods.qbsp")
                : $t("physicalgoods.gwc")
            }}</span>
          </template> -->
        </el-table-column>
        <el-table-column
          prop="upper_shelf_time"
          :label="$t('physicalgoods.sjsj')"
          width="200"
        >
          <template slot-scope="scope">
            <span></span>
            <span v-if="scope.row.upper_shelf_time">{{
              scope.row.upper_shelf_time | dateFormat
            }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('physicalgoods.px')">
          <template slot-scope="scope">
            <el-input
              v-model="scope.row.sort"
              @keyup.native="scope.row.sort = oninput2(scope.row.sort)"
              @input="changeInput(scope.$index)"
              @change="Sort(scope.row, scope.$index)"
            />
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('physicalgoods.cz')"
          fixed="right"
          width="200"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  v-has-permi="'/goods/goodslist/editorphysical'"
                  icon="el-icon-edit-outline"
                  @click="Edit(scope.row)"
                  >{{ $t('physicalgoods.bianji') }}</el-button
                >
                <el-button
                  v-has-permi="'deleteGoods'"
                  icon="el-icon-delete"
                  @click="Delete(scope.row)"
                >{{ $t('physicalgoods.shanchu') }}</el-button
                >
              </div>
              <div class="OP-button-bottom">
                <div class="more" @click="tags(scope.row)" >
                  <i class="el-icon-caret-bottom"></i> {{ $t('yz') }}
                  <ul class="more-block" :class="[tag == scope.row.id ? 'active' : '']" >
                    <li class="more-box" v-for="(item,index) in languages" :key="index">
                      <div @click="Copy(scope.row,item)" >{{item.lang_name}}</div>
                    </li>
                  </ul>
                </div>
                <el-button
                  v-has-permi="'downGoods'"
                  v-if="scope.row.status == 2"
                  icon="el-icon-bottom"
                  @click="standdown(scope.row)"
                >{{ $t('physicalgoods.xj') }}</el-button
                >
                <el-button
                  v-has-permi="'upGoods'"
                  v-if="scope.row.status == 1 || scope.row.status == 3"
                  icon="el-icon-top"
                  @click="standdown(scope.row)"
                  >{{ $t('physicalgoods.sj') }}</el-button
                >

              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox">
        <div class="pageLeftText">
          {{ $t('DemoPage.tableExamplePage.show') }}
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
            {{ $t('DemoPage.tableExamplePage.on_show') }}{{ currpage }}-{{
              current_num
            }}{{ $t('DemoPage.tableExamplePage.twig') }}{{ total
            }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
          </div>
        </el-pagination>
      </div>
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
          <span>{{ $t('DemoPage.tableExamplePage.export_page') }}</span>
        </div>
        <div class="item item-center" @click="exportAll">
          <i class="el-icon-document-copy"></i>
          <span>{{ $t('DemoPage.tableExamplePage.export_all') }}</span>
        </div>
        <div class="item" @click="exportQuery">
          <i class="el-icon-document"></i>
          <span>{{ $t('DemoPage.tableExamplePage.export_query') }}</span>
        </div>
      </el-dialog>
    </div>
    <!-- 弹框组件 -->
    <el-dialog
        :title="title"
        :visible.sync="dialogVisible1"
        :before-close="handleClose1"
        width="600px"
      >
        <div class="body-box">
          <!-- 显示位置 -->
          <el-form ref="ruleForm" :model="form" label-width="100px">
            <!-- 设置显示位置 -->
            <el-form-item :label="title" >
                <el-radio-group v-model="form.primary">
                  <el-radio
                  v-for="item in localList"
                  :key="item.id"
                  :label="item.value"
                  :value="item.value">{{item.text}}</el-radio>
                </el-radio-group>
            </el-form-item>
          </el-form>

          <div class="floot-box">
            <el-button @click="resetForm('ruleForm')">{{$t('inventoryManagement.ccel')}}</el-button>
            <el-button type="primary" @click="submitForm('ruleForm')">{{$t('addIntegralGoods.save')}}</el-button>
          </div>
        </div>
      </el-dialog>
  </div>
</template>

<script>
import {
  index,
  delGoodsById,
  goodsMovePosition,
  upperAndLowerShelves,
  isOpen,
  goodsByTop,
  label,
  choiceClass,
  getGoodsActiveList,
  editSort
} from '@/api/goods/goodsList'
import { exports } from '@/api/export/index'
import { getStorage } from '@/utils/storage'
import { mixinstest } from '@/mixins/index'
import { dictionaryList } from '@/api/Platform/numerical'
import { getFreightInfo ,goodSteByGoodsid} from '@/api/goods/freightManagement'
import ErrorImg from '@/assets/images/default_picture.png'
import { getCountries, getLanguages } from '@/api/common'

export default {
  name: 'virtualgoods',
  mixins: [mixinstest],
  data () {
    return {
      // 国家
      countriesList: [],
      // 语种
      languages: [],
      title: '', //解决js报错
      radio1: this.$t('physicalgoods.xnsp'),
      classList: [],
      brandList: [],
      brandnotset:null,
      classnotset:null,
      activityList: [],
      statusList: [
        {
          value: '1',
          label: this.$t('physicalgoods.dsj')
        },
        {
          value: '2',
          label: this.$t('physicalgoods.ysj')
        },
        {
          value: '3',
          label: this.$t('physicalgoods.yxj')
        }
      ],
      localList: [],
      tag: "",
      standUpDown: this.$t('physicalgoods.spsj'),

      labelList: [],
      inputInfo: {
        lang_code:'',
        goodsClass: '',
        brand: '',
        type: '',
        state: '',
        location: '',
        label: '',
        name: ''
      },

      type: '',
      value: 1,
      value2: 1,
      is_disabled: true,
      idList: [],
      tableData: [],
      loading: true,
      dialogVisible1: false,

      // 弹框数据
      dialogVisible: false,

      isShow_sort: false,
      isShow_sorts: false,

      // table高度
      tableHeight: null,

      classIds: [],
      menuId: '',
      mchId: '',
      freightInfoList: [],
      //导航类型 1不需要权限 2需要权限认证
      nav_label_type: 1,
      //导航数据 name:导航文字 label:绑定值 value:数量 disabled:是否禁用 routerTypes是否启用权限判断
      nav_label_list: [
        {
          id: '实物商品',
          name: this.$t('physicalgoods.swsp'),
          value: 0,
          ischoose: false
        },
        {
          id: '虚拟商品',
          name: this.$t('physicalgoods.xnsp'),
          value: 0,
          ischoose: true
        }
      ],
      form: {
        primary:''
      }
    }
  },

  created () {

    this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal();

    this.choiceClasss().then(() => {
      if (
        this.$route.params.inputInfo.goodsClass &&
        this.$route.params.inputInfo.goodsClass.length > 1
      ) {
        this.classIds = this.$route.params.inputInfo.goodsClass
        this.allClass(this.classList)
      }
    })
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
      this.inputInfo = this.$route.params.inputInfo
    }
    this.indexs()
    this.getDictionaryList()
    this.getGoodsActiveLists()
    this.labels()
    this.getLangs()
    this.getCountrys()
    // 获取物流模板数据
    this.handleGetFreightInfo()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  watch: {
    "inputInfo.lang_code"() {
      this.classList.lenth = 0;
      this.classList = [];
      this.choiceClasss();
    }
  },

  methods: {

    // 获取语种列表
    async getLangs() {
      const result = await this.LaiKeCommon.getLanguages();
      this.languages = result.data.data;
    },

    // 获取国家列表
    async getCountrys() {
      const res = await this.LaiKeCommon.getCountries();
      this.countriesList = res.data.data;
    },


    butEventer(type){
      if(this.idList && this.idList.length ==0){
        this.warnMsg(this.$t('distribution.addGoods.qxzsp'))
        return
      }
        this.title = type
        this.dialogVisible1 = true

    },
    utEventer(type){
      if(this.idList && this.idList.length ==0){
        this.warnMsg(this.$t('distribution.addGoods.qxzsp'))
        return
      }
        this.title = type
        this.dialogVisible1 = true

    },
    handleClose1 (done) {
      this.dialogVisible1 = false
    },
    //tab跳转方法
    nav_choose (id) {
      switch (id) {
        case '实物商品':
          this.$router.push('/goods/goodslist/physicalgoods')
          break
        case '虚拟商品':
          this.$router.push('/goods/goodslist/virtualgoods')
          break
      }
    },
    submitForm(){
      let data = {}
        // 设置 显示位置
        if(this.form.primary == '' ){
          this.warnMsg(this.$t('physicalgoods.qxzxswz'))
          return
        }
        data.api = 'admin.goods.BatchSelectionOfLocations'
        data.status = this.form.primary

      data.goodsIds = this.idList.map(v=>v.id).join(',')

      goodSteByGoodsid({
        ...data
      }).then(({data,message})=>{
        if(data.code == 200){
          this.succesMsg(this.title + this.$t('supplier.cg'))
          this.indexs()
        }else{
          this.errorMsg(message)
        }
      }).finally(()=>{
          this.resetForm()
      })
    },
    resetForm(){
      this.dialogVisible1 = false
       for(let key in this.form){
        this.form[key] = ''
       }
    },
    // 图片错误处理
    handleErrorImg (e) {
      console.log('图片报错了', e.target.src)
      e.target.src = ErrorImg
    },
    // 查看是否有运费模板
    // 获取运费列表
    async handleGetFreightInfo () {
      const res = await getFreightInfo({
        api: 'admin.goods.getFreightInfo',
        pageSize: 999,
        mchId: getStorage('laike_admin_userInfo').mchId
      })
      this.freightInfoList = res.data.data.list
    },
    switchs () {
      this.isOpens()
    },
    async getDictionaryList () {
      const res = await dictionaryList({
        api: 'saas.dic.getDictionaryInfo',
        key: '商品展示位置',
        status: 1
      })

      this.localList = res.data.data.list
      console.log('localList', this.localList)
    },
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },

    // 获取商品类别
    async choiceClasss () {
      const res = await choiceClass({
        api: 'admin.goods.choiceClass',
        lang_code: this.inputInfo.lang_code
      })

      const rawList = res.data.data.list.class_list[0] || [];

      const seen = new Set();
      this.classList = rawList
        .filter(item => {
          // if (item.notset === 1) return false;
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

    changeBrand(value)
    {
      const selected = this.brandList.find(item => item.brand_id === value);
      if (selected) {
        console.log('notset:', selected.notset);
        // 可选：赋值到别的字段
        this.brandnotset = selected.notset;
      }
    },

    // 根据商品类别id获取商品品牌
    changeProvinceCity (value) {
      console.log('选中的值数组:', value);
      // 获取当前选中的节点（只取第一个）
      const selectedNode = this.$refs.myCascader?.getCheckedNodes?.()[0];
      console.log('选中的节点对象数组:', selectedNode);
      if (!selectedNode || !selectedNode.data) {
        console.warn('未选中任何节点，或节点数据不存在');
        return;
      }
      const dataNode = selectedNode.data;
      this.classnotset = dataNode.notset;
      console.log("数据")
      console.log(dataNode)

      this.inputInfo.brand = ''
      this.brandList = []
      choiceClass({
        api: 'admin.goods.choiceClass',
        lang_code:this.inputInfo.lang_code,
        classId: value.length > 1 ? value[value.length - 1] : value[0]
      }).then(res => {
        const result = res?.data?.data?.list || {};
        this.brandList = result.brand_list || [];
        // 更新当前节点的 children
        const newClassList = result.class_list?.[0] || [];
        dataNode.children = newClassList.map((item, index) => ({
          value: item.cid,
          label: item.pname,
          index,
          notset: item.notset,
          level: item.level,
          children: []
        }));
      })
    },

    // 加载所有分类
    async allClass (value) {
      for (let i = 0; i < value.length - 1; i++) {
        if (this.classIds.includes(value[i].value)) {
          choiceClass({
            api: 'admin.goods.choiceClass',
            classId: value[i].value
          }).then(res => {
            if (res.data.data.list.class_list.length !== 0) {
              this.brandList = res.data.data.list.brand_list
              res.data.data.list.class_list[0].forEach((item, index) => {
                let obj = item
                value[i].children.push({
                  value: obj.cid,
                  label: obj.pname,
                  notset:obj.notset,
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
    async labels () {
      const res = await label({
        api: 'admin.label.index'
      })
      console.log(res)
      this.labelList = res.data.data.list
    },

    async indexs () {
      const res = await index({
        api: 'admin.goods.index',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        lang_code: this.inputInfo.lang_code,
        commodityType: 1,//商品类型 0.实物商品 1.虚拟商品
        active: this.inputInfo.type,
        cid: this.inputInfo.goodsClass[this.inputInfo.goodsClass.length - 1],
        brandId: this.inputInfo.brand,
        status: this.inputInfo.state ? parseInt(this.inputInfo.state) : null,
        showAdr: this.inputInfo.location,
        classnotset:this.classnotset,
        brandnotset:this.brandnotset,
        productTitle: this.inputInfo.name,
        goodsTga: this.inputInfo.label ? parseInt(this.inputInfo.label) : null
      })
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      this.value = res.data.data.is_open
      this.value2 = res.data.data.isDisplaySellOut
      this.sizeMeth()
      this.$nextTick(function () {
        this.$refs.table.doLayout()
        this.getHeight()
      })
      if (
        this.inputInfo.goodsClass ||
        this.inputInfo.location ||
        this.isShow_sorts
      ) {
        this.isShow_sort = true
      } else {
        this.isShow_sort = false
        this.isShow_sorts = false
      }
    },
    sizeMeth () {
      this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
      this.current_num =
        this.tableData.length === this.pageSize
          ? this.dictionaryNum * this.pageSize
          : this.total
    },
    //修改序号
    async Sort (value, index) {
      console.log(value)
      if (value.sort)
        editSort({
          api: 'admin.goods.editSort',
          id: value.id,
          sort: value.sort
        }).then(res => {
          if (res.data.code == '200') {
            this.indexs()
            this.succesMsg(this.$t('physicalgoods.cg'))
          }
        })
    },

    changeInput (value, index) {
      if (parseInt(value) < 0) {
        this.tableData[index].sort = 1
      }
      // console.log(value);
    },

    releaseGoods () {
      if (getStorage('laike_admin_userInfo').mchId == 0) {
        this.errorMsg(this.$t('plugInsSet.plugInsList.qtjdp'))
        this.$router.push('/mall/fastBoot/index')
      } else if (this.freightInfoList.length <= 0) {
        this.confirmBox(this.$t('physicalgoods.wltishi')).then(() => {
          this.$router.push('/goods/freightmanagement/addFreight')
        })
      } else {
        this.$router.push('/goods/goodslist/releasevirtual')
      }
    },

    // 是否线上下架商品
    async isOpens () {
      const res = await isOpen({
        api: 'admin.goods.isOpen'
      })
      console.log(res)
      if (res.data.code == '200') {
        this.indexs()
        this.succesMsg(this.$t('zdata.czcg'))
      }
    },
    // 是否显示已售罄商品
    async isOpens2 () {
      const res = await isOpen({
        api: 'admin.goods.displaySellOut'
      })
      if (res.data.code == '200') {
        this.indexs()
        this.succesMsg(this.$t('zdata.czcg'))
      }
    },
    reset () {
      this.inputInfo.goodsClass = ''
      this.inputInfo.brand = ''
      this.inputInfo.type = ''
      this.inputInfo.state = ''
      this.inputInfo.location = ''
      this.inputInfo.label = ''
      this.inputInfo.name = ''
      this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal()
      this.brandList = []
      this.isShow_sorts = true
      this.classnotset = null
      this.brandnotset = null
    },

    demand () {
      this.loading = true
      this.dictionaryNum = 1
      this.indexs().then(() => {
        this.loading = false
      })
    },

    Edit (value) {
      this.$router.push({
        path: '/goods/goodslist/releasevirtual',
        query: {
          id: value.id,
          name: 'editor',
          status: value.status_name,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
          lang_name: value.lang_name,
          lang_code: value.lang_code,
          classId: value.product_class,
          inputInfo: this.inputInfo,
          mch_id:value.mch_id,
        }
      })
    },

    Delete (value) {
      this.confirmBox(this.$t('physicalgoods.scsp')).then(() => {
        delGoodsById({
          api: 'admin.goods.delGoodsById',
          goodsId: value.id
        }).then(res => {
          if (res.data.code == '200') {
            console.log(res, this.dictionaryNum)
            if (this.dictionaryNum != 1 && this.tableData.length <= 1) {
              this.dictionaryNum--
            }
            this.indexs()
            this.succesMsg(this.$t('zdata.sccg'))
          }
        })
      })
    },

    tags(value) {
      if (this.tag == value.id) {
        this.tag = "";
      } else {
        this.tag = value.id;
      }
    },

    Copy (value,item) {
      value.lang_code = item.lang_code;
      this.$router.push({
        path: '/goods/goodslist/releasevirtual',
        query: {
          id: value.id,
          name: 'copy',
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
          lang_name: item.lang_name,
          lang_code: item.lang_code,
          classId: value.product_class,
          inputInfo: this.inputInfo,
          mch_id:value.mch_id,
        }
      })
    },

    moveUp (value) {
      if (value !== 0) {
        goodsMovePosition({
          api: 'admin.goods.goodsMovePosition',
          moveGoodsId: this.tableData[value - 1].id,
          goodsId: this.tableData[value].id
        }).then(res => {
          if (res.data.code == '200') {
            this.indexs()
            this.succesMsg(this.$t('physicalgoods.cg'))
          }
        })
      } else {
        goodsMovePosition({
          api: 'admin.goods.goodsMovePosition',
          moveGoodsId: this.tableData[value + 1].id,
          goodsId: this.tableData[value].id
        }).then(res => {
          if (res.data.code == '200') {
            this.indexs()
            console.log(res)
            this.succesMsg(this.$t('physicalgoods.cg'))
          }
        })
      }
    },

    placedTop (value) {
      goodsByTop({
        api: 'admin.goods.goodsByTop',
        goodsId: value.id
      }).then(res => {
        console.log(res)
        this.indexs()
        this.succesMsg(this.$t('physicalgoods.cg'))
      })
      console.log(value)
    },

    // 商品上下架
    standdown (value) {
      console.log(value)
      if (value.status === 2) {
        upperAndLowerShelves({
          api: 'admin.goods.upperAndLowerShelves',
          goodsIds: value.id,
          status: 0
        }).then(res => {
          if (res.data.code == '200') {
            console.log(res)
            this.indexs()
            this.succesMsg(this.$t('physicalgoods.xjcg'))
          }
        })
      } else {
        upperAndLowerShelves({
          api: 'admin.goods.upperAndLowerShelves',
          goodsIds: value.id,
          status: 2
        }).then(res => {
          if (res.data.code == '200') {
            console.log(res)
            this.indexs()
            this.succesMsg(this.$t('physicalgoods.sjcg'))
          }
        })
      }
    },

    // 批量上架
    shelves (number) {
      if (this.idList.length === 0) {
        this.warnMsg(this.$t('physicalgoods.qxzsp'))
      } else {
        if (number === 2) {
          this.idList = this.idList.map(item => {
            return item.id
          })
          this.idList = this.idList.join(',')
          upperAndLowerShelves({
            api: 'admin.goods.BatchLoadingAndUnloading',
            goodsIds: this.idList,
            status: 2
          }).then(res => {
            if (res.data.code == '200') {
              console.log(res)
              this.indexs()
              this.succesMsg(this.$t('physicalgoods.sjcg'))
            }
          })
        } else {
          this.idList = this.idList.map(item => {
            return item.id
          })
          this.idList = this.idList.join(',')
          upperAndLowerShelves({
            api: 'admin.goods.BatchLoadingAndUnloading',
            goodsIds: this.idList,
            status: 3
          }).then(res => {
            if (res.data.code == '200') {
              console.log(res)
              this.indexs()
              this.succesMsg(this.$t('physicalgoods.xjcg'))
            }
          })
        }
      }
    },

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log(e)
      this.pageSize = e
      this.indexs().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
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
      this.indexs().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },

    // 选框改变
    handleSelectionChange (val) {
      if (val.length == 0) {
        this.is_disabled = true
        this.standUpDown = this.$t('physicalgoods.spsj')
      } else {
        this.is_disabled = false
        let is_up = []
        val.map(item => {
          if (item.status_name == '待上架' || item.status_name == '下架') {
            this.standUpDown = this.$t('physicalgoods.spsj')
          } else {
            is_up.push(item)
          }
        })
        if (is_up.length == val.length) {
          this.standUpDown = this.$t('physicalgoods.spxj')
        }
      }
      this.idList = val
      console.log(val)
    },

    // 批量删除
    delAll () {
      this.confirmBox(this.$t('physicalgoods.sfsc')).then(() => {
        this.idList = this.idList.map(item => {
          return item.id
        })
        this.idList = this.idList.join(',')
        delGoodsById({
          api: 'admin.goods.delGoodsById',
          goodsId: this.idList
        }).then(res => {
          if (res.data.code == '200') {
            console.log(res)
            if (
              (this.dictionaryNum != 1 && this.tableData.length <= 1) ||
              this.idList >= this.tableData.length
            ) {
              this.dictionaryNum--
            }
            this.indexs()
            this.succesMsg(this.$t('zdata.sccg'))
          }
        })
      })
    },

    getStatusName (value) {
      if (value === 1) {
        return '待上架'
      } else if (value === 2) {
        return '已上架'
      } else {
        return '已下架'
      }
    },

    // 弹框方法
    dialogShow () {
      this.dialogVisible = true
    },

    handleClose (done) {
      this.dialogVisible = false
    },

    async exportPage () {
      await exports(
        {
          api: 'admin.goods.index',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          commodityType: 1,//商品类型 0.实物商品 1.虚拟商品
          active: this.inputInfo.type,
          cid: this.inputInfo.goodsClass[this.inputInfo.goodsClass.length - 1],
          brandId: this.inputInfo.brand,
          status: this.inputInfo.state ? parseInt(this.inputInfo.state) : null,
          showAdr: this.inputInfo.location,
          productTitle: this.inputInfo.name,
          goodsTga: this.inputInfo.label ? parseInt(this.inputInfo.label) : null
        },
        'pagegoods'
      )
    },

    async exportAll () {
      console.log(this.total)
      await exports(
        {
          api: 'admin.goods.index',
          pageNo: 1,
          pageSize: 999999,
          exportType: 1,
          commodityType: 1 //商品类型 0.实物商品 1.虚拟商品
        },
        'allgoods'
      )
    },

    async exportQuery () {
      await exports(
        {
          api: 'admin.goods.index',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          commodityType: 1,//商品类型 0.实物商品 1.虚拟商品
          active: this.inputInfo.type,
          cid: this.inputInfo.goodsClass[this.inputInfo.goodsClass.length - 1],
          brandId: this.inputInfo.brand,
          status: this.inputInfo.state ? parseInt(this.inputInfo.state) : null,
          showAdr: this.inputInfo.location,
          productTitle: this.inputInfo.name,
          goodsTga: this.inputInfo.label ? parseInt(this.inputInfo.label) : null
        },
        'querygoods'
      )
    }
  }
}
</script>

<style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
  /deep/.btn-nav {
    margin-bottom: 16px;
    .el-radio-button {
      width: 104px;
      .el-radio-button__inner {
        width: 104px;
      }
    }
    span {
      height: 42px;
      font-size: 16px;
      border: none;
    }
  }
  /deep/.Search {
    .Search-condition {
      .query-input {
        display: flex;
        .select-input {
          margin-right: 8px;
        }
        .input-name {
          width: 200px;
          height: 40px;
          margin-right: 8px;
          input {
            width: 200px;
            height: 40px;
          }
        }
      }
    }
  }

  /deep/.el-table {
    .OP-button {
      .OP-button-bottom {
        .more {
          width: 82Px;
          padding: 5Px;
          min-height: 22Px;
          background: #ffffff;
          border: 1px solid #d5dbe8;
          border-radius: 2px;
          font-size: 12px;
          font-weight: 400;
          color: #888f9e;
          line-height: 9px;
          cursor: pointer;
          //position: relative;
          &:hover {
            border: 1px solid rgb(64, 158, 255);
            color: rgb(64, 158, 255);
          }

          &:hover i {
            color: rgb(64, 158, 255);
          }

          .more-block {
            min-width: 82Px;
            overflow-x: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            position: absolute;
            border: 1px solid #d5dbe8;
            border-top: 0px;
            left: 25.3%;
            transform: translateX(-50%);
            top: 80%;
            z-index: 9999;
            background-color: #fff;
            display: none;

            &.active {
              display: block !important;
            }

            li {
              div {
                padding: 0 0.5px;
                min-height: 22px;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                color: #888f9e;
                //border-bottom: 1px solid #d5dbe8;
                &:hover {
                  background-color: #2890ff;
                  color: #fff;
                }
              }
            }
          }
        }
      }
    }
  }

  /deep/.jump-list {
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
    .laiketui-shangjia:before {
      font-size: 14px;
      margin-right: 8px;
      position: relative;
      top: 1px;
    }
    .laiketui-xiajia:before {
      font-size: 14px;
      margin-right: 8px;
      position: relative;
      top: 1px;
    }



    .taobao {
      background-color: #5387dd;
    }

    .batchImport {
      background-color: #2bc2c1;
    }

    .shelves {
      background-color: #13ce66;
    }
    .show-goods {
      position: absolute;
      right: 30px;
      display: flex;
      .center_a {
        display: flex;
        justify-content: center;
        align-items: center;
      }
      .center_b {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-left: 20px;
      }
    }
  }

  .menu-list {
    flex: 1;
    background: #ffffff;
    border-radius: 4px;
    .div_centent{
      display: flex;
      justify-content: center;
      align-items: center;
    }
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
        .status_name {
          width: 58px;
          height: 20px;
          line-height: 20px;
          text-align: center;
          display: block;
          border-radius: 10px;
          font-size: 14px;
          color: #fff;
          &.active {
            background-color: #18c364;
          }
          &.actives {
            background-color: #97a0b4;
          }
        }
      }
      .cell {
        .goodsDetail {
          display: flex;
          align-items: center;
          .goodsImg {
            img {
              width: 60px;
              height: 60px;
            }
          }
          .goodsInfo {
            display: flex;
            flex-direction: column;
            margin-left: 10px;
            .product_title {
              text-align: left !important;
            }

            .label_list {
              display: flex;
              p {
                height: 20px;
                line-height: 20px;
                color: #ffffff;
                padding: 0 3px;
                font-size: 12px;
                background: #42b4b3;
                &.active1 {
                  background: #42b4b3;
                }
                &.active2 {
                  background: #ff5041;
                }
                &.active3 {
                  background: #fe9331;
                }
                &:not(:first-child) {
                  margin-left: 10px;
                }
              }
            }
          }
        }
      }

      .cell {
        .el-input {
          max-width: 140px;
          input {
            text-align: center;
          }
        }
      }
    }

    /deep/.el-table {
      .OP-button {
        .OP-button-bottom {
          .shangjia {
            display: flex;
            align-items: center;
            justify-content: center;
            .font-shangjia {
              margin-left: 3px;
              margin-bottom: 3px;
            }
          }
          .shangjia > span {
            height: 24px;
            line-height: 24px;
            display: inline-block;
          }
          .laiketui-shangjia:before,
          .laiketui-xiajia:before {
            font-size: 13px;
            position: relative;
            // top: 2px;
          }
        }
      }
    }
  }
}
</style>
