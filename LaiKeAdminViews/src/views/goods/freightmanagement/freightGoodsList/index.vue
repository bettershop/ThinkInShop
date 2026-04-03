<template>
  <div class="container">

    <div class="jump-list">
        <el-button
          class="bgColor laiketui laiketui-add"
          type="primary"
          @click="addInventory"
          >更换模板</el-button
        >
      </div>
    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
      @selection-change="handleSelectionChange"
      :height="600"

		  >
      <el-table-column
        type="selection"
        >
      </el-table-column>
        <el-table-column fixed="left" prop="id" :label="$t('inventoryManagement.xh')"  >
        </el-table-column>

        <el-table-column prop="specifications" :label="$t('goodsDelivery.sptp')"  >
          <template slot-scope="{row}">
            <img :src="row.imgurl" style="width: 50px;height: 50px;"/>
          </template>
        </el-table-column>
        <el-table-column prop="product_title" :label="$t('goodsDelivery.spmc')" >
        </el-table-column>
        <el-table-column prop="price" :label="$t('releasephysical.sj')" >
          <template slot-scope="{row}">
           <span>{{ Number(row.price || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="shop_name" show-overflow-tooltip :label="$t('inventoryManagement.gg')">
        </el-table-column> -->
        <el-table-column prop="mch_name" :label="$t('orderSettlementList.dpmc')"  >

        </el-table-column>

	    </el-table>
      <div class="pageBox" ref="pageBox">
        <div class="pageLeftText">{{$t('DemoPage.tableExamplePage.show')}}</div>
        <el-pagination
          layout="sizes, slot, prev, pager, next"
          :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
          :next-text="$t('DemoPage.tableExamplePage.next_text')"
          @size-change="handleSizeChange"
          :page-sizes="pagesizes"
          :current-page="dictionaryNum"
          @current-change="handleCurrentChange"
          :total="total"
        >
          <div class="pageRightText">{{$t('DemoPage.tableExamplePage.on_show')}}{{currpage}}-{{current_num}}{{$t('DemoPage.tableExamplePage.twig')}}{{total}}{{ $t('DemoPage.tableExamplePage.twig_notes') }}</div>
        </el-pagination>
      </div>
    </div>
    <el-dialog
        :title="$t('physicalgoods.szyf')"
        :visible.sync="dialogVisible1"
        :before-close="handleClose1"
        width="600px"
      >
        <div class="body-box">
          <!-- 显示位置 -->
          <el-form ref="ruleForm" :model="form" label-width="100px">
            <!-- 设置运费 -->
            <el-form-item :label="$t('physicalgoods.szyf')" >
              <el-select
                class="select-input"
                v-model="form.freight"
                :placeholder="$t('releasephysical.qxzyf')"
              >
                <el-option
                  v-for="item in freightList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>
          <div class="floot-box">
            <el-button @click="dialogVisible1 = false">{{$t('inventoryManagement.ccel')}}</el-button>
            <el-button type="primary" @click="submitForm('ruleForm')">{{$t('addIntegralGoods.save')}}</el-button>
          </div>
        </div>
      </el-dialog>
  </div>
</template>

<script>
import { relatedProducts } from '@/api/goods/freightGoodsList'
import { getFreightInfo ,goodSteByGoodsid} from '@/api/goods/freightManagement'
import { getStorage } from '@/utils/storage'
import { mixinstest } from "@/mixins/index";


export default {
  mixins: [mixinstest],

  data(){
    return{
      tableData:[],
      freightList:[],
      idList:[],
      loading:false,
      dialogVisible1:false,
      dictionaryNum:1,
      pageSize:10,
      form:{
        freight:''
      }
    }
  },
  created(){
    this.id = this.$route.query.id
    this.getList()
    this.getFreightInfos()
  },
  methods: {
    handleSelectionChange(val){
      this.idList = val
    },
    addInventory(){
      if(this.idList.length == 0){
        this.warnMsg(this.$t('auction.releaseEvents.qxzsp'))
        return
      }
      this.dialogVisible1 = true
    },
    async getList() {
      const res = await relatedProducts({
        api:'admin.Freight.RelatedProducts',
        id:this.id, // 运费id
        pageNo:this.dictionaryNum,
        pageSize: this.pageSize,  // 每页多少条数据
      })
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
      console.log(res)
    },
     //选择一页多少条
     handleSizeChange(e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.getList().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      console.log(e,'eeeeee')
      this.loading = true
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.getList().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },
    // 获取运费列表
    async getFreightInfos() {
      const res = await getFreightInfo({
        api: "admin.goods.getFreightInfo",
        pageSize: 999,
        mchId: this.mchid
          ? this.mchid
          : getStorage("laike_admin_userInfo").mchId,
      });
      this.freightList = res.data.data.list;
    },
    handleClose1 (done) {
      this.dialogVisible1 = false
    },
    submitForm(){
      let data = {}
      // 设置运费
      if(this.form.freight == '' ){
        this.warnMsg(this.$t('preSale.releasephysical.qxzyfm'))
        return
      }
      data.api = 'admin.goods.BatchSetShippingFees'
      data.fid = this.form.freight
      data.goodsIds = this.idList.map(v=>v.id).join(',')

      goodSteByGoodsid({
        ...data
      }).then(({data,message})=>{
        if(data.code == 200){
          this.succesMsg(data.message)
          this.idList.splice(0,this.idList.length)
          this.getList()

          this.dialogVisible1 = false
        }else{
          this.errorMsg(data.message)
        }
      })

    },
  }
}
</script>

<style scoped lang="less">
.jump-list {
  button {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
  }
}
// @import '../../../webManage/css/goods/inventoryManagement/inventoryList.less';
</style>
