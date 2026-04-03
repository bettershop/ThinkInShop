<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input v-model="inputInfo.name" size="medium" @keyup.enter.native="demand" class="Search-input"  :placeholder="$t('permissions.menulist.qsrcd')"></el-input>
          <el-select
            class="select-input"
            :placeholder="$t('qxzyz')"
            v-model="inputInfo.lang_code"
          >
            <el-option
              v-for="item in languages"
              :key="item.lang_code"
              :label="item.lang_name"
              :value="item.lang_code"
            >
            </el-option>
          </el-select>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
        </div>
      </div>
	  </div>
    <div class="jump-list">
      <el-button type="primary"  @click="superiors">
        <img src="@/assets/imgs/fhsj.png" alt="">
        {{$t('permissions.menulist.fhsj')}}
      </el-button>
    </div>
    <div class="menu-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"  v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
		  :height="tableHeight">
      <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
      <el-table-column prop="id_id" :label="$t('permissions.menulist.cdid')" width="88">
        </el-table-column>
        <el-table-column prop="title" :label="$t('permissions.menulist.cdmc')">
        </el-table-column>
        <el-table-column prop="lang_name" :label="$t('yz')"> </el-table-column>
        <el-table-column prop="guide_name" :label="$t('permissions.menulist.cdlx')">
          <template slot-scope="scope">
            <span>{{ scope.row.type === 0 ? $t('permissions.menulist.kzt') : $t('permissions.menulist.sc') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" :label="$t('permissions.menulist.cddj')">
        </el-table-column>
        <el-table-column prop="fatherName" :label="$t('permissions.menulist.sjcd')">
        </el-table-column>
        <el-table-column prop="add_time" :label="$t('permissions.menulist.tjsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('permissions.menulist.caozuo')" width="300">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-view" @click="viewlower(scope.row)">{{$t('permissions.menulist.ckxj')}}</el-button>
                <el-button icon="el-icon-edit-outline" @click="Edit(scope.row)">{{$t('permissions.menulist.bianji')}}</el-button>
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">{{$t('permissions.menulist.shanchu')}}</el-button>
              </div>
              <div class="OP-button-bottom">
                <el-button class="laiketui laiketui-add" @click="addmenu(scope.row)">{{$t('permissions.menulist.tjcd')}}</el-button>
                <el-button @click="moveUp(scope.$index)">
                  <i v-if="scope.$index !== 0 || dictionaryNum !== 1" class="el-icon-top"></i>
                  <i v-else class="el-icon-bottom"></i>
                  {{ scope.$index === 0 && dictionaryNum == 1 ? $t('permissions.menulist.xy') : $t('permissions.menulist.sy')}}
                </el-button>
                <el-button class="laiketui laiketui-zhiding" @click="placedTop(scope.row)" v-if="scope.$index !== 0 || dictionaryNum !==1">{{$t('permissions.menulist.zd')}}</el-button>
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
import { getMenuLeveInfo, moveTopMenuSort, delMenu, moveMenuSort } from '@/api/Platform/permissions'
import { mixinstest } from '@/mixins/index'

export default {
  name: 'viewmenu',
  mixins: [mixinstest],
  data() {
    return {
      languages:[],
      lang_code:'',
      inputInfo: {
        name: '',
        lang_code: '',
      },
      tableData: [],
      loading: true,
      level: null,
      // table高度
      tableHeight: null,
    }
  },

  created() {
    if(this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.inputInfo.lang_code = this.$route.query.p_lang_code? this.$route.query.p_lang_code : this.LaiKeCommon.getUserLangVal();
    this.getLanguage();
    this.getMenuLeveInfos(this.$route.query.id)
  },

  mounted() {
    this.$nextTick(function() {
      this.getHeight()
    })
    window.addEventListener('resize',this.getHeight(),false)

  },

  computed: {
    superior() {
        return this.$store.state.superior.superiorList
    }
  },

  watch: {
      $route:{
          handler:function() {
            // console.log(this.$route.query.id);
            this.getMenuLeveInfos(this.$route.query.id)
          },
          deep: true
      }
  },

  methods: {
    async getLanguage() {
      try {
        const result = await this.LaiKeCommon.getLanguages();
        this.languages = result.data.data;
      } catch (error) {
        console.error('获取语种列表失败:', error);
      }
    },
    getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
		},
    async getMenuLeveInfos(id) {
      const res = await getMenuLeveInfo({
        api: 'saas.role.getMenuLeveInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        name: this.inputInfo.name,
        lang_code: this.inputInfo.lang_code,
        sid: id
      })
      console.log(res);
      this.level = res.data.data.total === 0 ? this.level + 1 : res.data.data.list[0].level
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (res.data.data.total < 10) {
        this.current_num = this.total;
      }
    },

    reset() {
      this.inputInfo.name = ''
    },

    demand() {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getMenuLeveInfos(this.$route.query.id).then(() => {
        this.loading = false
        if(this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    superiors() {
      this.currpage = 1
      this.current_num = 10
      this.dictionaryNum = 1
      if(this.level === 2 || this.level === 1) {
        this.$router.push('/Platform/permissions/menulist')
      } else {
        // let ids = this.$store.state.superior.superiorList[0]
        let ids = this.$store.state.superior.superiorList.pop()
        // if(this.level === 3) {
        //   this.$store.commit('EMPTY_SUPERIOR')
        // } else {
        //   this.$store.commit('DEL_SUPERIOR')
        // }
        this.$router.push({path:'/Platform/permissions/viewmenu',query: { id: ids }})
      }
    },

    viewlower(value) {
      // console.log(value);
      this.dictionaryNum = 1
      this.$store.commit('ADD_SUPERIOR',this.$route.query.id)
      this.$router.push({path:'/Platform/permissions/viewmenu',query: {id: value.id}})
    },

    Edit(value) {
      if(this.level === 2) {
        this.$router.push({
          name: 'editormenulevel',
          params: value,
          query: {
            menulevel: this.level,
            dictionaryNum: this.dictionaryNum,
            pageSize: this.pageSize
          }
        })
      } else if (this.level === 3) {
        this.$router.push({
          name: 'editormenulevel',
          params: value,
          query: {
            menulevel: this.level,
            dictionaryNum: this.dictionaryNum,
            pageSize: this.pageSize
          }
        })
      } else {
        this.$router.push({
          name: 'editormenulevel',
          params: value,
          query: {
            menulevel: this.level,
            dictionaryNum: this.dictionaryNum,
            pageSize: this.pageSize
          }
        })
      }

    },

    Delete(value) {
      this.$confirm(this.$t('permissions.menulist.scqr'), this.$t('permissions.menulist.ts'), {
        confirmButtonText: this.$t('permissions.menulist.ok'),
        cancelButtonText: this.$t('permissions.menulist.cancel'),
        type: 'warning'
      }).then(() => {
        delMenu({
          api: 'saas.role.delMenu',
          menuId: value.id
        }).then(res => {
          if(res.data.code == '200') {
            console.log(res);
            this.$message({
              type: 'success',
              message: this.$t('commonLanguage.sccg'),
              offset: 102
            });
            this.getMenuLeveInfos(this.$route.query.id)
          }
        })
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // });
      });
    },

    addmenu(value) {
      console.log(value);
      if(this.level === 2) {
          this.$router.push({
            name: 'addmenulevel',
            params: value,
            query: {
              menulevel: this.level + 1
            }
          })
      } else if (this.level === 3) {
          this.$router.push({
            name: 'addmenulevel',
            params: value,
            query: {
              menulevel: this.level + 1
            }
          })
      }
    },

    moveUp(value) {
        if(value !== 0) {
            moveMenuSort({
              api: 'saas.role.moveMenuSort',
              downId: this.tableData[value - 1].id,
              upId: this.tableData[value].id
            }).then(res => {
              this.getMenuLeveInfos(this.$route.query.id)
              console.log(res);
              this.$message({
                type: 'success',
                message: this.$t('commonLanguage.sycg'),
                offset: 102
              });
            })
        } else {
            moveMenuSort({
              api: 'saas.role.moveMenuSort',
              downId: this.tableData[value + 1].id,
              upId: this.tableData[value].id
            }).then(res => {
              this.getMenuLeveInfos(this.$route.query.id)
              console.log(res);
              this.$message({
                type: 'success',
                message: this.$t('commonLanguage.xycg'),
                offset: 102
              });
            })
        }
    },

    placedTop(value) {
      console.log(value);
      moveTopMenuSort({
        api: 'saas.role.moveTopMenuSort',
        id: value.id,
        sid: value.s_id
      }).then(res => {
        if(res.data.code == '200') {
          console.log(res);
          this.getMenuLeveInfos(this.$route.query.id)
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
      this.getMenuLeveInfos(this.$route.query.id).then(() => {
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
      this.getMenuLeveInfos(this.$route.query.id).then(() => {
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
      .select-input {
        margin-right: 10px;
      }
      .query-input {
        .Search-input{
          margin-right: 10px;
        }
      }
    }

  }

  /deep/.jump-list {
    button {
      img {
        position: relative;
        top: 1px;
        right: 3px;
      }
    }
  }

  .menu-list {
    // height: 597px;
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

    /deep/.el-table{
      .OP-button{
        .OP-button-top{
          margin-bottom: 8px;
          display: flex;
          justify-content: start;
          button {
            &:not(:first-child) {
              margin-left: 8px !important;
            }
          }
        }
        .OP-button-bottom {
          display: flex;
          justify-content: start;
          .laiketui-add:before {
            margin-right: 5px;
            font-size: 10px;
          }
          .laiketui-zhiding:before {
            margin-right: 5px;
            // color: #888f9e;
            // font-weight: 600;
          }
        }
      }
    }

  }
}
</style>
