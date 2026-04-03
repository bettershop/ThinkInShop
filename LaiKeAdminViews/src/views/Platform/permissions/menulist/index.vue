<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('permissions.menulist.cdlb')" @click.native.prevent="$router.push('/Platform/permissions/menulist')"></el-radio-button>
        <el-radio-button :label="$t('permissions.menulist.jslb')" @click.native.prevent="$router.push('/Platform/permissions/rolelist')"></el-radio-button>
      </el-radio-group>
    </div>
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-select class="select-input" v-model="inputInfo.code" :placeholder="$t('permissions.menulist.cdlx')">
            <el-option v-for="item in Dictionary" :key="item.value" :label="item.label" :value="item.label">
            </el-option>
          </el-select>

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

          <el-input v-model="inputInfo.name" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('permissions.menulist.qsrcd')"></el-input>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button class="bgColor" v-enter="demand" type="primary" @click="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
        </div>
      </div>
	  </div>
    <div class="jump-list">
      <el-button  class="bgColor laiketui laiketui-add" type="primary"  @click="$router.push('/Platform/permissions/addmenulevel')">{{$t('permissions.menulist.tjcd')}}</el-button>
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
        <el-table-column prop="add_time" :label="$t('permissions.menulist.tjsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_time | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('permissions.menulist.caozuo')" width="290">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-view" @click="viewlower(scope.row,inputInfo.lang_code)">{{$t('permissions.menulist.ckxj')}}</el-button>
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
  name: 'menulist',
  mixins: [mixinstest],
  data() {
    return {
      languages: [],
      radio1:this.$t('permissions.menulist.cdlb'),
      inputInfo: {
        code: '',
        name: '',
        lang_code: "",
      },
      type: '',
      Dictionary: [
        {
          value: '选项二',
          label: this.$t('permissions.menulist.kzt')
        },
        {
          value: '选项一',
          label: this.$t('permissions.menulist.sc')
        }
      ],

      tableData: [],
      loading: true,

      // table高度
      tableHeight: null,

      ids: null

    }
  },

  created() {
    if(this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal();
    this.getLanguage();
    this.getMenuLeveInfos()
    this.$store.commit('EMPTY_SUPERIOR')
  },

  mounted() {
    this.$nextTick(function() {
      this.getHeight()
    })
    window.addEventListener('resize',this.getHeight(),false)

  },

  watch: {
    'inputInfo.code':{
      handler:function() {
        if(this.inputInfo.code === this.$t('permissions.menulist.sc')) {
          this.type = 1
        } else if(this.inputInfo.code === this.$t('permissions.menulist.kzt')) {
          this.type = 0
        } else {
          this.type = ''
        }
      }
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
    async getMenuLeveInfos() {
      const res = await getMenuLeveInfo({
        api: 'saas.role.getMenuLeveInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        name: this.inputInfo.name,
        lang_code: this.inputInfo.lang_code,
        type: this.type
      })
      console.log(res.data);
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (res.data.data.total < 10) {
        this.current_num = this.total;
      }
    },

    reset() {
      this.inputInfo.code = ''
      this.inputInfo.name = ''
      this.inputInfo.lang_code = ''
    },

    demand() {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getMenuLeveInfos().then(() => {
        this.loading = false
        if(this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    viewlower(value,lang_code) {
      console.log(value.id);
      this.$router.push({path:'/Platform/permissions/viewmenu',query: {id: value.id,p_lang_code: lang_code}});
    },

    Edit(value) {
      console.log(value);
      this.$router.push({
        name: 'editormenulevel',
        params: value,
        query: {
          menulevel: 1,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize
        }
      })
    },

    addmenu(value) {
      this.$router.push({
        name: 'addmenulevel',
        params: value,
        query: {
          menulevel: 2
        }
      })
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
            this.getMenuLeveInfos()
            this.$message({
              type: 'success',
              message: this.$t('commonLanguage.sccg'),
              offset: 100
            });
          }
        })
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // });
      });
    },

    moveUp(value) {
      if(value == 0 && this.dictionaryNum == 1) {
          moveMenuSort({
            api: 'saas.role.moveMenuSort',
            downId: this.tableData[value + 1].id,
            upId: this.tableData[value].id
          }).then(res => {
            if(res.data.code == '200') {
              this.getMenuLeveInfos(this.$route.query.id)
              console.log(res);
              this.$message({
                type: 'success',
                message: this.$t('commonLanguage.xycg'),
                offset: 100
              });
            }
          })
      } else {
        moveMenuSort({
          api: 'saas.role.moveMenuSort',
          downId: this.tableData[value].id,
          upId: value == 0 ? this.ids : this.tableData[value - 1].id
        }).then(res => {
          if(res.data.code == '200') {
            this.getMenuLeveInfos(this.$route.query.id)
            console.log(res);
            this.$message({
              type: 'success',
              message: this.$t('commonLanguage.sycg'),
              offset: 100
            });
          }
        })
      }
    },

    placedTop(value) {
      console.log(value);
      moveTopMenuSort({
        api: 'saas.role.moveTopMenuSort',
        id: value.id,
        sid: 0
      }).then(res => {
        if(res.data.code == '200') {
          console.log(res);
          this.getMenuLeveInfos()
          this.$message({
            type: 'success',
            message: this.$t('commonLanguage.zdcg'),
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
      this.getMenuLeveInfos().then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
        this.loading = false
      })
		},
		//点击上一页，下一页
		handleCurrentChange(e){
      if(e > 1) {
        this.ids = this.tableData[this.tableData.length-1].id
      }
      this.loading = true
      this.dictionaryNum = e
      this.currpage = ((e - 1) * this.pageSize) + 1
      this.getMenuLeveInfos().then(() => {
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
  /deep/.btn-nav {
    span {
      height: 40px;
      font-size: 16px;
      border: none;
    }
  }

  /deep/.Search{
    .select-input {
      margin-right: 10px;
    }
    margin: 16px 0;
    .Search-condition{
      .query-input {
        .Search-input{
          width: 280px;
          margin: 0px 10px 0px 0px;
        }
      }
    }

  }

  /deep/.jump-list {
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
  }

  .menu-list {
    // height: 537px;
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
