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
    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
		  :height="tableHeight">
      <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column :label="$t('functionnavigation.xh')" width="120">
          <template slot-scope="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="title" :label="$t('functionnavigation.dlmc')">
        </el-table-column>
        <el-table-column prop="id_id" :label="$t('functionnavigation.cdid')">
        </el-table-column>
        <el-table-column prop="guide_name" :label="$t('functionnavigation.cdmc')">
        </el-table-column>
        <el-table-column :label="$t('functionnavigation.sfxs')">
           <template slot-scope="scope">
            <el-switch v-model="scope.row.is_display" @change="switchs(scope.row)" :active-value="0" :inactive-value="1" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('functionnavigation.cz')" width="250">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-view" @click="viewlower(scope.row)">{{$t('functionnavigation.ckxj')}}</el-button>
                <!-- <el-button @click="moveUp(scope.$index)" v-if="scope.row.is_display === 1"> -->
                <el-button @click="moveUp(scope.$index)">
                  <i v-if="scope.$index !== 0 || dictionaryNum !== 1" class="el-icon-top"></i>
                  <i v-else class="el-icon-bottom"></i>
                  {{ scope.$index === 0&&dictionaryNum==1 ? $t('functionnavigation.xy') : $t('functionnavigation.sy')}}
                </el-button>
              </div>
              <div class="OP-button-bottom">
                <el-button class="laiketui laiketui-zhiding" @click="placedTop(scope.row)"  v-if="scope.$index !== 0||dictionaryNum!==1">{{$t('functionnavigation.zd')}}</el-button>
                <!-- <el-button class="laiketui laiketui-zhiding" @click="placedTop(scope.row)"  v-if="scope.$index !== 0 && scope.row.is_display === 1">置顶</el-button> -->
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
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import log from '@/libs/util.log'

export default {
  name: 'editornav',
  mixins: [mixinstest],
  data() {
    return {
      inputInfo: {
        name: '',
      },
      button_list:[],
      tableData: [],
      loading: true,

      // table高度
      tableHeight: null,

      level: null,
      ids: null

    }
  },

  created() {
    this.functionLists()
    this.getButtons()
  },

  mounted() {
    this.$nextTick(function() {
      this.getHeight()
    })
    window.addEventListener('resize',this.getHeight(),false)

  },

  methods: {
    getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
		},
    //获取按纽权限
    async getButtons() {
          let buttonList = await getButton ({
          api:'saas.role.getButton',
          token:getStorage('laike_admin_userInfo').token,
          storeId:getStorage('rolesInfo').storeId,
          menuId:getStorage('menuId'),
          })
          this.button_list=buttonList.data.data
          console.log(this.button_list,"获取按纽权限")
      },
    async functionLists() {
      const res = await functionList({
        api: 'admin.overview.functionList',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
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
      this.functionLists().then(() => {
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
        this.functionLists()
        console.log(res);
        if(res.data.code == '200') {
          if(value.is_display === 1) {
            this.$message({
              message: this.$t('zdata.czcg'),
              type: 'success',
              offset: 100
            })
          } else {
            this.$message({
              message: this.$t('zdata.czcg'),
              type: 'success',
              offset: 100
            })
          }
        }

      })
    },

    viewlower(value) {
      this.$router.push({path:'/mall/functionnavigation/viewnav',query: {id: value.id}})
    },

    Edit(value) {
      // console.log(value);
      if(this.level === 2) {
        this.$router.push({
            name: 'editormenulevel2',
            params: value
        })
      } else if (this.level === 3) {
        this.$router.push({
            name: 'editormenulevel3',
            params: value
        })
      }

    },

    Delete(value) {
      this.$confirm(this.$t('functionnavigation.scts'), this.$t('functionnavigation.ts'), {
        confirmButtonText: this.$t('functionnavigation.okk'),
        cancelButtonText: this.$t('functionnavigation.ccel'),
        type: 'warning'
      }).then(() => {
        delMenu({
          api: 'saas.role.delMenu',
          menuId: value.id
        }).then(res => {
          console.log(res);
          this.$message({
            type: 'success',
            message: this.$t('functionnavigation.cg'),
            offset: 100
          });
        })
        this.getMenuLeveInfos(this.$route.query.id)
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // });
      });
    },

    moveUp(value) {
      console.dir(value,this.ids)
        if(value !== 0|| this.dictionaryNum != 1) {
          console.log('this.ids237',this.ids);
            move({
              api: 'admin.overview.move',
              id2: this.ids ?this.ids :this.tableData[value - 1].id,
              id: this.tableData[value].id
            }).then(res => {
              console.log(res);
              if(res.data.code == 200){
                  this.$message({
                  type: 'success',
                  message: this.$t('functionnavigation.sycg'),
                  offset: 100
                });
                setTimeout(() => {
                  this.demand()
                }, 100);
              }
            })
        } else {
          console.log('this.ids256',this.ids);
            move({
              api: 'admin.overview.move',
              id2: this.tableData[value + 1].id,
              id: this.tableData[value].id
            }).then(res => {
              console.log(res);
              if(res.data.code == 200){
                  this.$message({
                  type: 'success',
                  message: this.$t('functionnavigation.xycg'),
                  offset: 100
                });
                setTimeout(() => {
                  this.demand()
                }, 100);
              }
            })
        }
    },

    placedTop(value) {
      console.log(value);
      sortTop({
        api: 'admin.overview.sortTop',
        id: value.id
      }).then(res => {
        console.log(res);
        if(res.data.code == '200') {
          this.demand()
          this.$message({
            type: 'success',
            message: this.$t('functionnavigation.zdcg'),
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
    this.functionLists().then(() => {
      this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
      this.loading = false
    })
	},
  //点击上一页，下一页
  handleCurrentChange(e){
    console.log(e)
    if(e > 1) {
        this.ids = this.tableData[this.tableData.length-1].id
      }else{
        this.ids = false
      }
      console.log('this.ids',this.ids);
    this.loading = true
    this.dictionaryNum = e
    this.currpage = ((e - 1) * this.pageSize) + 1
    this.functionLists().then(() => {
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
    }
  }
}
</style>
