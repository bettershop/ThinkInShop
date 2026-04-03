<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input v-model="inputInfo.name" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('functionnavigation.qsrcd')"></el-input>
        </div>
        <div class="btn-list">
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
        </div>
      </div>
	  </div>
    <div class="jump-list">
      <el-button type="primary"  @click="$router.push('/mall/functionnavigation/editornav')">
        <img src="@/assets/imgs/fhsj.png" alt="">
        {{$t('functionnavigation.fhsj')}}
      </el-button>
    </div>
    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
		  :height="tableHeight">
      <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column prop="id_id" :label="$t('functionnavigation.xh')" width="86">
          <template slot-scope="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="guide_name" :label="$t('functionnavigation.dlmc')">
        </el-table-column>
        <el-table-column prop="id_id" :label="$t('functionnavigation.cdid')">
        </el-table-column>
        <el-table-column prop="title" :label="$t('functionnavigation.cdmc')">
        </el-table-column>
        <el-table-column prop="image1" class="image" :label="$t('functionnavigation.dltb')">
          <template slot-scope="scope">
              <img :src="scope.row.image1" alt="" @error="handleErrorImg">
          </template>
        </el-table-column>
        <el-table-column prop="briefintroduction" :label="$t('functionnavigation.dljj')">
        </el-table-column>
        <el-table-column :label="$t('functionnavigation.sfxs')">
           <template slot-scope="scope">
            <el-switch v-model="scope.row.is_display" @change="switchs(scope.row)" :active-value="0" :inactive-value="1" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('functionnavigation.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button @click="moveUp(scope.$index)">
                  <i v-if="scope.$index !== 0" class="el-icon-top"></i>
                  <i v-else class="el-icon-bottom"></i>
                  {{ scope.$index === 0 ? $t('functionnavigation.xy') : $t('functionnavigation.sy')}}
                </el-button>
                <el-button class="laiketui laiketui-zhiding" @click="placedTop(scope.row)"  v-if="scope.$index !== 0">{{$t('functionnavigation.zd')}}</el-button>
              </div>
            </div>
		  </template>
        </el-table-column>
	    </el-table>
      <div class="pageBox" ref="pageBox" v-if="showPagebox">
        <div class="pageLeftText">{{$t('DemoPage.tableExamplePage.show')}}</div>
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
          <div class="pageRightText">{{$t('DemoPage.tableExamplePage.on_show')}}{{currpage}}-{{current_num}}{{$t('DemoPage.tableExamplePage.twig')}}{{total}}{{ $t('DemoPage.tableExamplePage.twig_notes') }}</div>
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import { functionList, isDisplaySwitch, move, sortTop } from '@/api/mall/functionNavigation'
import { mixinstest } from '@/mixins/index'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: 'viewnav',
  mixins: [mixinstest],
  data() {
    return {
      inputInfo: {
        name: '',
      },

      tableData: [],
      loading: true,
      
      // table高度
      tableHeight: null,
      
    }
  },

  created() {
    this.functionLists(this.$route.query.id)
  },

  mounted() {
    this.$nextTick(function() {
      this.getHeight()
    })
    window.addEventListener('resize',this.getHeight(),false)

  },

  methods: {
    // 图片错误处理
    handleErrorImg(e){
      console.log('图片报错了',e.target.src);
      e.target.src=ErrorImg
    },
    getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
		},
    async functionLists(id) {
      const res = await functionList({
        api: 'admin.overview.functionList',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        sid: id,
        name: this.inputInfo.name,
      })
      console.log(res);
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
    },


    demand() {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.functionLists(this.$route.query.id).then(() => {
        this.loading = false
        if(this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    switchs(value) {
      isDisplaySwitch({
        api: 'admin.overview.isDisplaySwitch',
        id: value.id
      }).then(res => {
        if(res.data.code == '200') {
          this.functionLists(this.$route.query.id)
          console.log(res);
          if(res.data.code == '200') {
            if(value.is_display === 1) {
              this.$message({
                message: this.$t('zdata.czcg'),
                type: 'success',
                offset: 102
              })
            } else {
              this.$message({
                message: this.$t('zdata.czcg'),
                type: 'success',
                offset: 102
              })
            }
          }
          
        }
        
      })
    },

    moveUp(value) {
        console.log('value',value);
        
        if(value !== 0) {
            move({
              api: 'admin.overview.move',
              id2: this.tableData[value - 1].id,
              id: this.tableData[value].id
            }).then(res => {
              if(res.data.code == 200){
              this.functionLists(this.$route.query.id)
              console.log(res);
              this.$message({
                type: 'success',
                message: this.$t('zdata.sycg'),
                offset: 100
              });
            }
            })
        } else {
            move({
              api: 'admin.overview.move',
              id2: this.tableData[value + 1].id,
              id: this.tableData[value].id
            }).then(res => {
              if(res.data.code == 200){
              this.functionLists(this.$route.query.id)
              console.log(res);
              this.$message({
                type: 'success',
                message: this.$t('zdata.xycg'),
                offset: 100
              });
            }
            })
        }
    },

    placedTop(value) {
      sortTop({
        api: 'admin.overview.sortTop',
        id: value.id,
        sid: value.s_id
      }).then(res => {
        console.log(res)
        if(res.data.code == '200') {
          console.log(res);
          this.functionLists(this.$route.query.id)
          this.$message({
            type: 'success',
            message: this.$t('zdata.zdcg'),
            offset: 100
          });
        }
        
      })
    },

    //选择一页多少条
    handleSizeChange(e){
      this.loading = true
      console.log(e);
      // this.current_num = e
      this.pageSize = e
      this.functionLists(this.$route.query.id).then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
        this.loading = false
      })
    },
    
    //点击上一页，下一页
    handleCurrentChange(e){
      this.loading = true
      this.dictionaryNum = e
      this.currpage = ((e - 1) * this.pageSize) + 1
      this.functionLists(this.$route.query.id).then(() => {
        this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.loading = false
      })
    },

  }
}
</script>

<style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
  /deep/.Search{
    .Search-condition{
      .query-input {
        .Search-input{
          margin: 0 10px 0 0;
        }
      }
    }
    
  }

  .menu-list {
    flex: 1;
    background: #FFFFFF;
    border-radius: 4px;
    /deep/.el-table__header {
      thead {
        tr {
          th{
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
          td{
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

    /deep/.el-table{
      .OP-button{
        .OP-button-top{
          margin-bottom: 8px;
        }
      }
    }
  }
}
</style>