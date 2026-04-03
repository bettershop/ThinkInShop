<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            class="one-input"
            @keyup.enter.native="demand"
            v-model="inputInfo.key"
            :placeholder="$t('floorDetail.text1')"
          >
          </el-input>
          <el-cascader
            style="margin-right: 10px"
            v-model="inputInfo.cid"
            class="select-input"
            ref="myCascader"
            :placeholder="$t('floorDetail.text2')"
            :options="classList"
            :props="{ checkStrictly: true }"
            @change="changeProvinceCity"
            clearable
          >
          </el-cascader>
          <el-select
            class="select-input"
            v-model="inputInfo.bid"
            :placeholder="$t('floorDetail.text3')"
            style="margin-right: 10px"
          >
            <el-option
              v-for="item in brandList"
              :key="item.brand_id"
              :label="item.brand_name"
              :value="item.brand_id"
            >
            </el-option>
          </el-select>
          <el-cascader
            style="margin-right: 10px"
            v-model="inputInfo.type"
            class="select-input"
            ref="myCascader"
            :placeholder="$t('floorDetail.qsrlygjz')"
            :options="allMchAdnSupplierList"
            :props="{ checkStrictly: true }"
            @change="changSource"
            clearable
          >
          </el-cascader> 
          <el-date-picker
            v-model="inputInfo.date"
            type="datetimerange"
            :range-separator="$t('reportManagement.businessReport.zhi')"
            :start-placeholder="$t('reportManagement.businessReport.ksrq')"
            :end-placeholder="$t('reportManagement.businessReport.jsrq')"
            value-format="yyyy-MM-dd HH:mm:ss"
            :editable="false"
          >
          </el-date-picker>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t('DemoPage.tableExamplePage.reset')
          }}</el-button>
          <el-button class="bgColor" type="primary" @click="demand">{{
            $t('DemoPage.tableExamplePage.demand')
          }}</el-button>
        </div>
      </div>
    </div>
    <div class="jump-list">
      <el-button
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="AddPro()"
      >
        {{ $t('floorDetail.tjsp') }}</el-button
      >
      <el-button
        class="fontColor"
        @click="Delete('', 'all')"
        icon="el-icon-delete"
      >
        {{ $t('floorDetail.plsc') }}</el-button
      >
    </div>

    <div class="merchants-list" ref="tableFather">
      <el-table
        :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
        @selection-change="handleSelectionChange2"
        v-loading="loading"
        :data="tableData"
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
        <el-table-column prop="id" :label="$t('floorDetail.spid')">
        </el-table-column>
        <el-table-column prop="imgUrl" :label="$t('floorDetail.sptp')">
          <template slot-scope="scope">
            <img
              :src="scope.row.imgUrl"
              alt=""
              width="60"
              height="60"
              @error="handleErrorImg"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="product_title"
          :label="$t('floorDetail.spbt')"
          width="240"
        >
          <template slot-scope="scope">
            <div class="goodsInfo">
              <p class="product_title">{{ scope.row.product_title }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="className" :label="$t('floorDetail.fl')">
        </el-table-column>
        <el-table-column prop="brandName" :label="$t('floorDetail.pp')">
        </el-table-column>
        <el-table-column prop="num" :label="$t('floorDetail.kc')">
        </el-table-column>
        <el-table-column prop="pro_source" :label="$t('floorDetail.sply')">
        </el-table-column>
        <el-table-column prop="mch_name" :label="$t('physicalgoods.ssdp')">
        </el-table-column>
        <el-table-column
          prop="upper_shelf_time"
          :label="$t('floorDetail.czsj')"
          width="150px"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.proAddDate }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="sort" :label="$t('floorDetail.px')" width="150">
          <template slot-scope="scope">
            <el-input-number
              style="width: 100px"
              :controls="false"
              v-model="scope.row.sort"
              @change="changesort(scope.row)"
              :min="1"
              :label="$t('floorDetail.px')"
            ></el-input-number>
          </template>
        </el-table-column>
 
        <el-table-column
          fixed="right"
          :label="$t('floorDetail.cz')"
          width="180"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <!-- <el-button   icon="el-icon-edit-outline" @click="addimg(scope.row)">
                    设置主图</el-button> -->
              <el-button
                style="margin-bottom: 8px"
                icon="el-icon-delete"
                @click="Delete(scope.row, 'one')"
              >
                {{ $t('floorDetail.yc') }}</el-button
              >
              <!-- <el-button icon="el-icon-view" @click="godetail(scope.row)">
                详情</el-button
              > -->
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
    <el-dialog
      class="add_dia"
      :title="$t('floorDetail.tjsp')"
      :visible.sync="dialogVisible2"
      :before-close="handleClose"
      width="920px"
    >
      <div class="">
        <div class="Search">
          <el-cascader
            style="margin-right: 10px"
            v-model="inputInfo2.cid"
            class="four-input"
            ref="myCascader2"
            :placeholder="$t('floorDetail.text2')"
            :options="classList"
            :props="{ checkStrictly: true }"
            @change="changeProvinceCity2"
            clearable
          >
          </el-cascader>
          <el-select
            class="four-input"
            v-model="inputInfo2.brandId"
            :placeholder="$t('floorDetail.text3')"
            style="margin-right: 10px"
          >
            <el-option
              v-for="item in brandList2"
              :key="item.brand_id"
              :label="item.brand_name"
              :value="item.brand_id"
            >
            </el-option>
          </el-select>
          <el-cascader
            style="margin-right: 10px"
            v-model="inputInfo2.type"
            class="select-input"
            ref="myCascader"
            :placeholder="$t('floorDetail.qsrlygjz')"
            :options="allMchAdnSupplierList"
            :props="{ checkStrictly: true }"
            @change="changSource2"
            clearable
          ></el-cascader> 
          <el-input
            class="three-input"
            v-model="inputInfo2.productTitle"
            :placeholder="$t('floorDetail.text4')"
          ></el-input>

          <el-button @click="reset2" plain>{{
            $t('floorDetail.cz2')
          }}</el-button>
          <el-button @click="query2" type="primary">{{
            $t('floorDetail.cx2')
          }}</el-button>
        </div>
        <div class="Table">
          <el-table
            height="350"
            :data="prodata"
            :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
            v-loading="loading2"
            style="width: 100%"
            @selection-change="handleSelectionChange"
            :row-key="getRowKeys"
            ref="multipleTable"
          >
            <el-table-column
              :reserve-selection="true"
              align="center"
              type="selection"
              width="55"
            ></el-table-column>
            <el-table-column
              prop="ProName"
              align="center"
              :label="$t('floorDetail.spxx')"
              width="280"
            >
              <template slot-scope="scope">
                <div class="Info">
                  <div>
                    <img
                      :src="scope.row.imgurl"
                      width="60"
                      height="60"
                      alt=""
                      @error="handleErrorImg"
                    />
                  </div>
                  <div style="display: flex; margin-left: 10px">
                    <span style="text-align: left">{{
                      scope.row.product_title
                    }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              prop="p_name"
              align="center"
              :label="$t('floorDetail.fl')"
            >
            </el-table-column>
            <el-table-column
              prop="brand_name"
              align="center"
              :label="$t('floorDetail.pp')"
            >
            </el-table-column>
            <el-table-column
              prop="num"
              align="center"
              :label="$t('floorDetail.kc')"
            >
            </el-table-column>
            <el-table-column
              prop="pro_source"
              align="center"
              :label="$t('floorDetail.sply')"
            >
            </el-table-column>
            <el-table-column prop="mch_name" :label="$t('physicalgoods.ssdp')">
            </el-table-column>
          </el-table>
          <div class="pageBox">
            <div class="pageLeftText">
              {{ $t('DemoPage.tableExamplePage.show') }}
            </div>
            <el-pagination
              layout="sizes, slot, prev, pager, next"
              :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
              :next-text="$t('DemoPage.tableExamplePage.next_text')"
              @size-change="handleSizeChange2"
              :page-sizes="pagesizes"
              :pager-count="5"
              :current-page="pagination2.page"
              @current-change="handleCurrentChange2"
              :total="total2"
            >
              <div class="pageRightText">
                {{ $t('DemoPage.tableExamplePage.on_show') }}{{ showItem
                }}{{ $t('DemoPage.tableExamplePage.twig') }}{{ total2
                }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
              </div>
            </el-pagination>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <div>
          <span class="font_one">{{ $t('floorDetail.yx2') }}</span
          ><span class="font_two"
            >{{ num_lenght }} {{ $t('floorDetail.jian') }}</span
          >
        </div>
        <div>
          <el-button class="bdColor" @click="handleClose" plain>{{
            $t('DemoPage.tableFromPage.cancel')
          }}</el-button>
          <el-button type="primary" @click="confirm">{{
            $t('floorDetail.qd')
          }}</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import ErrorImg from '@/assets/images/default_picture.png'
import { choiceClass } from '@/api/goods/goodsList'
import {
  getGoodList,
  addGood,
  delGood,
  editSort,
  getType
} from '@/api/goods/floor'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import request from '@/api/https'
export default {
  name: 'floorDetail',
  mixins: [mixinstest],
  data () {
    return {
      tableData: [],
      brandList: [],
      brandList2: [],
      loading: true,
      loading2: true,
      inputInfo: {
        key: '',
        cid: '',
        bid: '',
        date: '',
        type: ''
      },
      inputInfo2: {
        cid: '',
        brandId: '',
        type:'',
        productTitle: '',
      },
      inputInfo3: {
        goodsShow: ''
      },
      button_list: [],
      dialogVisible3: false,
      // 弹框数据
      dialogVisible2: false,
      prodata: [],
      classList: [],
      allMchAdnSupplierList:[
        {
          value:0,
          label: '供应商',
          index: 0,
          children: []
        }
        ,{
          value:1,
          label: '店铺', 
          index: 1,
          children: []
        }
      ],   
      tableHeight: null,
      currpage: 1,
      currpage2: 1,
      current_num: '',
      current_num2: '',
      total: 0,
      total2: 0,
      pagination: {
        page: 1,
        pagesize: 10
      },
      pagination2: {
        page: 1,
        pagesize: 10
      },
      pagesizes: [10, 25, 50, 100],
      prochangedata: '',
      prochangedata2: '',
      pid: '',
      num_lenght: 0,
      sourceId: '',
      sourceType: '',
      typeList: [],
      sourceId2: '',
      sourceType2: '',
      typeList2: []
    }
  },

  created () {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    // this.getprolist()
    this.getTypeList()
    this.getGoodLists()
    // this.getButtons()
    this.choiceClasss()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },
  computed: {
    showItem () {
      let showItem1 =
        (this.currpage2 - 1) * this.pagination2.pagesize +
        this.pagination2.pagesize
      if (showItem1 > this.total2) {
        showItem1 = this.total2
      }
      let showItem =
        (this.currpage2 - 1) * this.pagination2.pagesize + 1 + '-' + showItem1
      return showItem
    }
  },
  methods: {
    handleErrorImg (e) {
      console.log('图片报错了', e.target.src)
      e.target.src = ErrorImg
    },  
    getTypeList () {
      getType({
        api: 'admin.block.getALlMchOrSupplier'
      }).then(res => {
        console.log('res', res)
        if (res.data.code == 200) {
          const { data:{data} } = res
          // 供应商列表
          this.allMchAdnSupplierList[0].children = data.allSupplier.map((obj,index)=>({
            value: obj.id,
            label: obj.name, 
            type: obj.type, 
          }))
          // 店铺列表
          this.allMchAdnSupplierList[1].children = data.allMch.map((obj,index)=>({
            value: obj.id,
            label: obj.name, 
            type: obj.type, 
          })) 
          // this.typeList = res.data.data.list
          // this.typeList2 = res.data.data.list
        }
      })
    },
    changesort (value) {
      editSort({
        api: 'admin.block.editSort',
        mappingId: value.mappingId,
        sort: value.sort
      }).then(res => {
        console.log(res)
        if (res.data.code == 200) {
          this.getGoodLists()
          this.$message({
            type: 'success',
            message: this.$t('zdata.czcg'),
            offset: 102
          })
        }
      })
    },
    // godetail (value) {
    //   console.log('value',value);

    //   this.$router.push({
    //     path: '/goods/goodslist/pooldetail',
    //     query: {
    //       id: value.id,
    //       name: "see",
    //       store_id: value.store_id,
    //       is_platform: value.is_platform,
    //       status: value.status_name,
    //       dictionaryNum: this.dictionaryNum,
    //       pageSize: this.pageSize,
    //       classId: value.classId,
    //       inputInfo: this.inputInfo,
    //     }
    //   })
    // },
    addimg (value) {
      this.pid = value.id
      this.inputInfo3.goodsShow = ''
      this.dialogVisible3 = true
    },
    async choiceClasss () {
      const res = await choiceClass({
        api: 'admin.goods.choiceClass'
      })
      res.data.data.list.class_list[0].forEach((item, index) => {
        let obj = item
        this.classList.push({
          value: obj.cid,
          label: obj.pname,
          index: index,
          level: obj.level,
          children: []
        })
      })
    },
    // 根据商品类别id获取商品品牌
    changeProvinceCity (value) {
      // this.inputInfo.bid = "";
      this.inputInfo.bid = []
      this.brandList = []
      choiceClass({
        api: 'admin.goods.choiceClass',
        classId: value.length > 1 ? value[value.length - 1] : value[0]
      }).then(res => {
        console.log('res', res)

        let num = this.$refs.myCascader.getCheckedNodes()[0].data.index
        this.brandList = res.data.data.list.brand_list
        if (res.data.data.list.class_list[0].length !== 0) {
          this.$refs.myCascader.getCheckedNodes()[0].data.children = []
          res.data.data.list.class_list[0].forEach((item, index) => {
            let obj = item
            this.$refs.myCascader.getCheckedNodes()[0].data.children.push({
              value: obj.cid,
              label: obj.pname,
              index: index,
              level: obj.level,
              children: []
            })
          })
        }
      })
      console.log('brandList', this.brandList)
    },
    // 列表使用
    changSource(value){ 
      console.log(value,value.length)
      if(value.length == 1){
        // typeNumber 0 供应商, 1 店铺
        const typeNumber = value[0]
        this.sourceType = typeNumber == 0 ? 'isSupplier' : 'isMch'
        // 默认选中子集第一个
        this.inputInfo.type = [typeNumber,this.allMchAdnSupplierList[typeNumber].children[0].value]

        this.sourceId = this.inputInfo.type[1]
      }else{
        this.sourceId = value[1] 
      }

    },
     // 添加视频弹窗 使用
     changSource2(value){ 
      if(value.length == 1){
        // typeNumber 0 供应商, 1 店铺
        const typeNumber = value[0]
        this.sourceType2 = typeNumber == 0 ? 'isSupplier' : 'isMch'

        // 默认选中子集第一个
        this.inputInfo2.type = [typeNumber,this.allMchAdnSupplierList[typeNumber].children[0].value]
        
        this.sourceId2 = this.allMchAdnSupplierList[typeNumber].children[0].value
      }else{
        this.sourceId2 = value[1] 
      }
    },
    // 根据商品类别id获取商品品牌
    changeProvinceCity2 (value) {
      this.inputInfo2.brandId = []
      this.brandList2 = []
      choiceClass({
        api: 'admin.goods.choiceClass',
        classId: value.length > 1 ? value[value.length - 1] : value[0]
      }).then(res => {
        this.brandList2 = res.data.data.list.brand_list
        if (res.data.data.list.class_list[0].length !== 0) {
          this.$refs.myCascader2.getCheckedNodes()[0].data.children = []
          res.data.data.list.class_list[0].forEach((item, index) => {
            let obj = item
            this.$refs.myCascader2.getCheckedNodes()[0].data.children.push({
              value: obj.cid,
              label: obj.pname,
              index: index,
              level: obj.level,
              children: []
            })
          })
        }
      })
    },
    isFillList () {
      let totalPage = Math.ceil(
        (Number(this.total) - 1) / Number(this.pageSize)
      )
      let dictionaryNum =
        this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum
      this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum
      this.getGoodLists() //数据初始化方法
    },
    // 删除
    Delete (value, row) {
      if (row == 'all') {
        if (this.prochangedata2.length == 0) {
          this.warnMsg(this.$t('floorDetail.text5'))
          return
        }
      }
      console.log(value)
      let idList = ''
      if (value) {
        idList = value.id
      } else {
        idList = this.prochangedata2.map(item => {
          return item.id
        })
        idList = idList.toString()
      }

      this.$confirm(this.$t('floorDetail.qdscm'), this.$t('floorDetail.ts'), {
        confirmButtonText: this.$t('floorDetail.qr'),
        cancelButtonText: this.$t('floorDetail.qx'),
        type: 'warning'
      })
        .then(() => {
          delGood({
            api: 'admin.block.delGoods',
            id: this.$route.query.id,
            goodsIds: idList
          }).then(res => {
            if (res.data.code == '200') {
              console.log(res)
              if (row == 'all') {
                var num = this.prochangedata2.length
              } else {
                var num = 1
              }
              let totalPage = Math.ceil(
                (Number(this.total) - num) / Number(this.pageSize)
              )
              let dictionaryNum =
                this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum
              this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum
              this.getGoodLists() //数据初始化方法
              this.getprolist()
              this.$message({
                type: 'success',
                message: this.$t('zdata.ycgc'),
                offset: 102
              })
            }
          })
        })
        .catch(() => {
          // this.$message({
          //   type: "info",
          //   message: "已取消删除",
          // });
        })
    },
    confirm2 () {
      let mainImg = this.inputInfo3.goodsShow
      if (mainImg) {
        let pos = mainImg.lastIndexOf('/') + 1
        mainImg = mainImg.substr(pos, mainImg.length)
      }
      request({
        method: 'post',
        params: {
          api: 'admin.block.setMainProImage',
          pid: this.pid,
          id: this.$route.query.id,
          imgurl: mainImg
        }
      }).then(res => {
        if (res.data.code == '200') {
          console.log(res)
          this.$message({
            type: 'success',
            message: this.$t('floorDetail.szcg'),
            offset: 102
          })
          this.getGoodLists()
          this.inputInfo3.goodsShow = ''
          this.dialogVisible3 = false
        }
      })
    },
    getRowKeys (row) {
      return row.id
    },
    confirm () {
      if (this.prochangedata.length == 0) {
        this.warnMsg(this.$t('floorDetail.text6'))
        return
      }
      let { entries } = Object
      let mylist = this.prochangedata.map(item => {
        return item.id
      })

      let data = {
        api: 'admin.block.addOrDeleteGoodsWithBlock',
        goodsId: mylist.toString(),
        id: this.$route.query.id
      }

      let formData = new FormData()
      for (let [key, value] of entries(data)) {
        formData.append(key, value)
      }

      addGood(formData).then(res => {
        if (res.data.code == '200') {
          console.log(res)
          this.$message({
            message: this.$t('zdata.tjcg'),
            type: 'success',
            offset: 102
          })
          this.getGoodLists()
          this.getprolist()
          this.handleClose()
        }
      })
      // this.$refs.multipleTable.clearSelection();
      // this.getprolist()
    },
    handleSelectionChange (e) {
      this.prochangedata = e
      this.num_lenght = e.length
      // this.handleCurrentChange2(1)
    },
    handleSelectionChange2 (e) {
      this.prochangedata2 = e
      console.log(this.prochangedata2)
      // this.handleCurrentChange2(1)
    },
    query () {
      this.getGoodLists()
    },
    query2 () {
      this.currpage2 = 1
      this.getprolist()
    },
    AddPro () {
      this.dialogVisible2 = true
      this.current_num2 = this.prodata.length
      this.currpage2 = 1
      this.pagination2.pageSize = 10
      this.getprolist()
    },
    async getprolist () {
      await request({
        method: 'post',
        params: {
          api: 'admin.block.GetSpecifiedGoodsInfo',
          pageNo: this.currpage2,
          pagesize: this.pagination2.pagesize,
          productTitle: this.inputInfo2.productTitle,
          sourceType:this.sourceType2,
          sourceId:this.sourceId2,
          brandId: this.inputInfo2.brandId,
          cid: this.inputInfo2.cid[this.inputInfo2.cid.length - 1],
          blockId: this.$route.query.id
        }
      }).then(res => {
        console.log(res)
        this.total2 = Number(res.data.data.total)
        this.prodata = res.data.data.list
        this.loading2 = false
        // this.current_num2 = this.prodata.length;
        // if (this.total2 < this.current_num2) {
        //   this.current_num2 = this.total2;
        // }
        if (res.data.data.total < 10) {
          this.current_num2 = this.total2
        }
        if (this.total2 < this.current_num2) {
          this.current_num2 = this.total2
        }
      })
    },

    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight)
    },
    //获取按纽权限
    // async getButtons () {
    //   let route = getStorage('route')
    //   route.forEach(item => {
    //     if (item.path == 'floorDetail') {
    //       return (this.menuId = item.id)
    //     }
    //   })
    //   let buttonList = await getButton({
    //     api: 'saas.role.getButton',
    //     menuId: this.menuId
    //   })
    //   this.button_list = buttonList.data.data
    // },
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
    // 获取商品信息
    async getGoodLists () {
      const res = await getGoodList({
        api: 'admin.block.getGoodsByBlock',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        id: this.$route.query.id,
        key: this.inputInfo.key,
        bid: this.inputInfo.bid, 

        sourceType:this.sourceType ,

        sourceId:this.sourceId,
        startTime: this.inputInfo.date[0],
        endTime: this.inputInfo.date[1],
        cid: this.inputInfo.cid[this.inputInfo.cid.length - 1]
      })
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (res.data.data.total < 10) {
        this.current_num = this.total
      }
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },
    handleClose () {
      this.inputInfo2.cid = ''
      this.inputInfo2.brandId = ''
      this.inputInfo2.productTitle = ''
      this.brandList2 = []
      this.$refs.multipleTable.clearSelection()
      this.dialogVisible2 = false
    },

    // 重置
    reset () {
      this.inputInfo.key = ''
      this.inputInfo.bid = ''
      this.inputInfo.cid = ''
      this.inputInfo.date = ''
      this.inputInfo.type = ''
      this.sourceId = ''
      this.sourceType = ''
      this.brandList = []
    },
    // 重置
    reset2 () {
      this.inputInfo2.cid = ''
      this.inputInfo2.brandId = ''
      this.inputInfo2.productTitle = ''
      this.inputInfo2.type = ''
      this.sourceId2 = ''
      this.sourceType2 = ''
      this.brandList2 = []
    },
    // 查询
    demand () {
      // this.currpage = 1;
      // this.current_num = 10;

      this.loading = true
      this.dictionaryNum = 1
      this.getGoodLists().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
        }
      })
    },

    //

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.getGoodLists().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },
    //选择一页多少条
    handleSizeChange2 (e) {
      this.loading2 = true
      this.pagination2.pagesize = e
      this.getprolist().then(() => {
        this.loading2 = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange (e) {
      this.loading = true
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.getGoodLists().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },
    handleCurrentChange2 (e) {
      this.currpage2 = e
      // if (this.dialogVisible3) {
      this.getprolist()
      // } else {
      //     this.getUserList()
      // }
    }
  }
}
</script>

<style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
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
        .one-input {
          width: 234px;
          height: 40px;
          margin-right: 10px;
          input {
            width: 234px;
            height: 40px;
          }
        }
        .two-input {
          width: 166px;
          height: 40px;
          margin-right: 10px;
          input {
            width: 166px;
            height: 40px;
          }
        }
        // .select-input {
        //   width: 197px;
        //   height: 40px;
        //   margin-right: 10px;
        //   input{
        //     width: 197px;
        //     height: 40px;
        //   }
        // }
        .Search-input {
          width: 280px;
          margin-right: 10px;
          input {
            width: 280px;
          }
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
        .export {
          position: absolute;
          right: 30px;
        }
      }
    }
    .three-input {
      width: 200px !important;
      height: 40px;
      margin-right: 10px;
      input {
        width: 200px !important;
        height: 40px;
      }
    }
    .four-input {
      width: 160px !important;
      height: 40px;
      margin-right: 10px;
      .el-input {
        width: 160px !important;
        height: 40px;
      }
      input {
        width: 160px !important;
        height: 40px;
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
    .bgColor {
      width: 120px;
      height: 40px;
      background: #28b6ff;
      border-radius: 4px;
      border: none;
      font-size: 14px;
    }
    button {
      width: 120px;
      height: 40px;
      border-radius: 4px;
      padding: 0;
      border: none;
      span {
        font-size: 14px;
      }
    }
    button:hover {
      opacity: 0.8;
    }
  }

  .merchants-list {
    flex: 1;
    // height: 605px;
    background: #ffffff;
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
      }
    }

    /deep/.el-table {
      .OP-button {
        display: flex;
        flex-direction: column;
        align-items: center;
        button {
          margin-left: 0;
        }
      }
    }
  }
}
.cell {
  .status_name {
    margin: auto;
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
/deep/.add_dia {
  .el-dialog {
    width: 950px;
    // height: 751px;
  }
  .el-dialog__header {
    border-bottom: 1px solid #e9ecef;
    padding: 20px;
  }
  .el-dialog__body {
    padding: 20px;
  }
  .el-dialog__footer {
    border-top: 1px solid #e9ecef;
    padding: 17px 20px 17px 20px;
  }
  .dialog-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .Search {
    display: flex;
    align-items: center;
    background: #ffffff;
    border-radius: 0.25rem;
    padding: 0;
    margin-bottom: 20px;
  }
  .select-input {
    width: 180px;
    input {
      width: 180px;
    }
  }
  .Table {
    border: 1px solid #e9ecef;
  }
  .pageBox {
    height: 68px;
  }
  .Info {
    display: flex;
    align-items: center;
  }
  .font_one {
    font-size: 14px;
    font-family: MicrosoftYaHei-, MicrosoftYaHei;
    font-weight: normal;
    color: #414658;
  }
  .font_two {
    font-size: 14px;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    color: #414658;
  }
}
</style>
