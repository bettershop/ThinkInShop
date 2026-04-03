<template>
    <div class="container">
   
      <div class="jump-list"> 
        <el-button
           
          class="fontColor"
          @click="delAll"
          :disabled="is_disabled"
          icon="el-icon-delete"
          >{{ $t('physicalgoods.plsc') }}</el-button
        >
      </div>
      <div class="menu-list" ref="tableFather">
        <el-table
          :element-loading-text="$t('DemoPage.loading_text')"
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
              <img src="../../../assets/imgs/empty.png" alt="" />
              <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
            </div>
          </template>
          <el-table-column prop="commodity_type" :label="$t('orderLists.splx')">
            <template slot-scope="{row}">
              <span>{{ row.commodity_type == '0' ? $t('physicalgoods.swsp'):$t('physicalgoods.xnsp')}}</span>
            </template>
          </el-table-column>
          <el-table-column fixed="left" type="selection" width="100">
          </el-table-column>
           
          <el-table-column prop=" " :label="$t('slideshowSet.tp')">
            <template slot-scope="scope">
              <img 
                alt="" srcset=""
                :src="scope.row.imgurls ||'error'" 
                @error="handleErrorImg" 
              >
            </template>
          </el-table-column>
          <el-table-column
            fixed="center"
            prop="product_title"
            :label="$t('physicalgoods.spbt')" 
          >
            <template slot-scope="scope">
              <div class="goods-info"> 
                <div class="arr_box">
                  <p class="product-title">{{ scope.row.product_title }}</p> 
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="pname" :label="$t('physicalgoods.spfl')">
          </el-table-column>
          <el-table-column prop="brand_name" :label="$t('physicalgoods.pp')">
          </el-table-column>
         
         
          <el-table-column prop="price" :label="$t('orderLists.jg')">
            <template slot-scope="{row}">
              <span style="color: #ff453d">{{ row.price ? Number(row.price || 0).toFixed(2) : 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="upper_shelf_time"
            :label="$t('physicalgoods.sjsj')"
            width="200"
          >
            <template slot-scope="scope">
              <span></span>
              <span v-if="scope.row.add_time">{{
                scope.row.add_time | dateFormat
              }}</span>
            </template>
          </el-table-column>
         
          <el-table-column :label="$t('zdata.cz')" fixed="right" width="240">
            <template slot-scope="{row}">
              <div class="OP-button"> 
                <div class="OP-button-bottom"> 
                  <el-button
                  @click="Detail(row)"
                  >{{ $t('physicalgoods.bianji') }}</el-button>
                  <el-button
                    @click="delAll(row)" 
                    >{{ $t('physicalgoods.shanchu') }}</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="pageBox" ref="pageBox" v-if="showPagebox">
          <div class="pageLeftText">{{ $t('DemoPage.show') }}</div>
          <el-pagination
            layout="sizes, slot, prev, pager, next"
            :prev-text="$t('DemoPage.prev_text')"
            :next-text="$t('DemoPage.next_text')"
            @size-change="handleSizeChange"
            :page-sizes="pagesizes"
            :current-page="pagination.page"
            @current-change="handleCurrentChange"
            :total="total"
          >
            <div class="pageRightText">
              {{ $t('DemoPage.on_show') }}{{ currpage }}-{{ current_num
              }}{{ $t('DemoPage.twig') }} {{ total }}
              {{ $t('DemoPage.twig_notes') }}
            </div>
          </el-pagination>
        </div>
      </div>
 
   
    </div>
  </template>
  
  <script>
  import {
    index, 
  } from '@/api/goods/goodsList' 

  import {
    getList, 
    delListItemById
  } from '@/api/draftsList/draftsList' 

  import { mixinstest } from '@/mixins/index' 
  import { getStorage } from '@/utils/storage' 
  import ErrorImg from '@/assets/images/default_picture.png'
  
  export default {
    name: 'physicalgoods',
    mixins: [mixinstest],
    data () {
      return { 
        button_list: [], 
        IsItDescendingOrder:'',
        inputInfo: {
          goodsClass: '',
          brand: '',
          type: '',
          state: '',
          name: '',
          productId: ''
        },
  
        type: '',
        value: true,
        idList: [],
  
        tableData: [],
        loading: true,
   
        is_disabled: true,
  
        // 添加库存弹框数据
        dialogVisible3: false, 
        id: '',
   
  
        // table高度
        tableHeight: null,
  
        classIds: [], 
   
        is_Payment: true,
        isPromiseExamine: true
      }
    },
  
    created () { 
   
        this.indexs() 
      // 获取按钮权限
      this.getButtonList()
    },
    watch: {
      '$route.query.id': {
        handler: function () {
          index({
            api: 'mch.Mch.Goods.Index',
            pageNo: this.dictionaryNum,
            pageSize: this.pageSize,
            commodityType: 1,
            productId: this.$route.query.id,
            cid: this.inputInfo.goodsClass[this.inputInfo.goodsClass.length - 1],
            brandId: this.inputInfo.brand,
            status: this.inputInfo.state ? parseInt(this.inputInfo.state) : null,
            productTitle: this.inputInfo.name,
            IsItDescendingOrder: this.IsItDescendingOrder
  
          }).then(res => {
            console.log(res)
            this.total = res.data.data.total
            this.is_Payment = res.data.data.is_Payment
            this.isPromiseExamine = res.data.data.isPromiseExamine
            this.tableData = res.data.data.list
            this.loading = false
            if (this.total < this.current_num) {
              this.current_num = this.total
            }
          })
        }
      }
    },
    mounted () {
      this.$nextTick(function () {
        this.getHeight()
      })
      window.addEventListener('resize', this.getHeight(), false)
    },
  
    watch: {
      value () { 
      }
    },
  
    methods: { 
      // 获取按钮权限
      getButtonList () {
        let routeList = getStorage('route')
        let list = routeList.filter(item => item.path == 'goodslist')[0].children
        this.button_list = list.map(item => {
          return {
            text: item.name,
            path: item.path,
            url: item.url,
            checked: item.checked
          }
        })
        console.log('routeList', routeList, this.button_list)
      },
      // 图片错误处理
      handleErrorImg (e) {
        console.log('图片报错了', e.target.src)
        e.target.src = ErrorImg
      },  
   
      getHeight () {
        this.tableHeight =
          this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      },
  
      async indexs () {
        const res = await getList({
          api: 'admin.Drafts.get_list',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
        
        })
        console.log(res)
        this.total = res.data.data.total
        this.is_Payment = res.data.data.is_Payment
        this.isPromiseExamine = res.data.data.isPromiseExamine
  
        this.tableData = res.data.data.list
        this.loading = false
        if (this.total < this.current_num) {
          this.current_num = this.total
        }
      },    
      // 添加库存
      addInventory (value) {
        console.log(value)
        this.id = value.id
        this.dialogVisible3 = true
        
      },
   
  
      Detail (value) {
        // commodity_tyep 0 实物商品 1 虚拟商品
        console.log(value.commodity_type,'value.commodity_type')
        if(!value.commodity_type){
          this.$router.push({
            path: '/goods/goodslist/releasephysical',
            query: {
              draftsId: value.id,
              editId: value.id,
              type:'deafts',
              name: 'editor',
              classId: value.product_class
            }
          })
        }else{
          this.$router.push({
            path: '/goods/goodslist/releasevirtual',
            query: {
              draftsId: value.id,
              editId: value.id,
              type:'deafts',
              name: 'editor',
              classId: value.product_class
            }
          })
        }
      }, 
  
     
      //选择一页多少条
      handleSizeChange (e) {
        this.loading = true
        console.log(e)
        // this.dictionaryNum = 1
        // this.current_num = e
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
   
      // 批量删除
      delAll (item) {
        if (!item && this.idList.length === 0) {
          this.$message({
            type: 'error',
            message: this.$t('physicalgoods.qxzsp'),
            offset: 100
          })
        } else {
          const text  = item.id ? this.$t('physicalgoods.sfsccg1') : this.$t('physicalgoods.sfsccg')

          this.$confirm(text, this.$t('zdata.ts'), {
            confirmButtonText: this.$t('zdata.ok'),
            cancelButtonText: this.$t('zdata.off'),
            type: 'warning'
          })
            .then(() => {
              let ids = this.idList.map(item => {
                return item.id
              })
              delListItemById({
                api: 'admin.Drafts.del',
                id: item.id || ids.join(',')
              }).then(res => {
                if (res.data.code == '200') { 
                  // 判断如果是 删除了最后页最后的数据则查询上一页的信息
                  if(this.dictionaryNum != 1 && this.tableData && this.tableData.length == 1){
                    this.dictionaryNum --
                  }
                  this.indexs()
                  this.$message({
                    type: 'success',
                    message: this.$t('zdata.sccg'),
                    offset: 100
                  })
                }
              })
            })
            .catch(() => {
              // this.$message({
              //   type: 'info',
              //   message: '已取消删除'
              // });
            })
        }
      },
   
    }
  }
  </script>
  
  <style scoped lang="less">
  .container {
    display: flex;
    flex-direction: column;
  
    .sales-volume{
      display: flex;
      align-items: center;
      img {
        width: 15px;
        height: 15px;
        margin-left: 5px;
      }
    }
  
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
          .select-input {
            margin-right: 10px;
            &:not(:first-child):not(:last-child) {
              width: 180px;
            }
          }
          .input-name {
            width: 300px;
            height: 40px;
            margin-right: 10px;
            input {
              width: 300px;
              height: 40px;
            }
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
  
        .btn-list {
          .bgColor {
            background-color: #2890ff;
          }
          .bgColor:hover {
            background-color: #70aff3;
          }
          .fontColor {
            color: #6a7076;
            border: 1px solid #d5dbe8;
            background-color: #fff;
          }
          .fontColor:hover {
            color: #2890ff;
            border: 1px solid #2890ff;
            background-color: #fff;
          }
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
      button {
        min-width: 120px;
        height: 40px;
        background: #28b6ff;
        border-radius: 4px;
        // padding: 0;
        border: none;
        &:hover {
          opacity: 0.8;
        }
        span {
          font-size: 14px;
        }
      }
  
      .shelves {
        background-color: #13ce66;
      }
  
      .fontColor {
        min-width: 120px;
        height: 40px;
        color: #6a7076;
        background-color: #fff;
        border-radius: 4px;
        &:hover {
          color: #2890ff;
        }
      }
    }
  
    .menu-list {
      flex: 1;
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
          .goods-info {
            display: flex;
            align-items: center;
            img {
              margin-right: 10px;
            }
            .arr_box {
              display: flex;
              flex-direction: column;
              align-items: flex-start;
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
          .status_name {
            min-width: 58px;
            height: 20px;
            line-height: 20px;
            text-align: center;
            display: inline-block;
            border-radius: 10px;
            font-size: 14px;
            color: #fff;
            padding: 0 5px;
            &.active {
              background-color: #13ce66;
            }
            &.actives {
              background-color: #97a0b4;
            }
          }
          .product-title {
            text-align: left;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 3;
          }
        }
        .cell {
          img {
            width: 60px;
            height: 60px;
          }
        }
  
        .cell {
          .el-input {
            width: 82px;
            input {
              width: 82px;
              text-align: center;
            }
          }
        }
      }
  
      /deep/.el-table {
        .OP-button {
          .OP-button-top {
            margin-bottom: 8px;
            display: flex;
            justify-content: center;
          }
          .OP-button-bottom {
            display: flex;
            justify-content: center;
            .laiketui-jinru:before {
              margin-right: 6px;
            }
            .laiketui-add:before {
              margin-right: 5px;
              font-size: 10px;
            }
            .laiketui-zhiding:before {
              margin-right: 5px;
              color: #888f9e;
              font-weight: 600;
            }
            .laiketui-shangjia:before,
            .laiketui-xiajia:before {
              position: relative;
              top: 2px;
            }
          }
          .el-button {
            width: 88px !important;
            border: 1px solid #d5dbe8;
            border-radius: 2px;
          }
        }
      }
    }
  
    /deep/.dialog-size {
      .el-dialog__footer {
        border-top: 1px solid #e9ecef;
        padding: 16px 20px !important;
      }
  
      .el-dialog__header {
        border-bottom: 1px solid #e9ecef;
        height: 60px;
        display: flex;
        align-items: center;
        padding: 0 20px !important;
      }
  
      .el-dialog__body {
        width: 100%;
        padding: 0px 20px !important;
        .meau-list {
          width: 100%;
          padding: 40px 0;
          .Table {
            .el-table {
              border: 1px solid #e9ecef !important;
              .el-table__body {
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
              }
  
              .stock-input {
                text-align: center;
                // width: 150px;
                input {
                  text-align: center;
                }
              }
            }
          }
        }
      }
  
      .el-icon-close {
        color: #888f9e !important;
        font-size: 20px !important;
      }
    }
  }
  </style>
  