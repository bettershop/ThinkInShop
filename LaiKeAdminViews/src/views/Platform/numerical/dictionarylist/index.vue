<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input v-model="inputInfo.code" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('Platform.numerical.qsrsjbm')"></el-input>
          <el-input v-model="inputInfo.name" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('Platform.numerical.qsrsjmc')"></el-input>
          <el-input v-model="inputInfo.attribute" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('Platform.numerical.qsrsjz')"></el-input>

          <el-select
            class="select-input"
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
    <div :class="language=='en'?'jump-list jump-list2':'jump-list'" ref="tableFather">
      <el-button class="bgColor laiketui laiketui-add" type="primary"  @click="$router.push('/Platform/numerical/adddictionary')">{{$t('Platform.numerical.tjsjzd')}}</el-button>
      <el-button class="bgColor" type="primary" icon="el-icon-remove-outline" @click="$router.push('/Platform/numerical/datanamemanagement')">{{$t('Platform.numerical.sjmcgl')}}</el-button>
    </div>

    <div class="dictionary-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
			:height="tableHeight">
      <template slot="empty">
          <div class="empty">
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column prop="code" :label="$t('Platform.numerical.sjbm')">
        </el-table-column>
        <el-table-column prop="name" :label="$t('Platform.numerical.sjmc')">
        </el-table-column>
        <el-table-column prop="lang_name" :label="$t('Platform.numerical.sjyz')"> </el-table-column>
        <el-table-column prop="text" :label="$t('Platform.numerical.sjz')">
        </el-table-column>
        <el-table-column :label="$t('Platform.numerical.sfsx')">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.status" @change="switchs(scope.row)" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="admin_name" :label="$t('Platform.numerical.tjr')">

        </el-table-column>
        <el-table-column prop="add_date" :label="$t('Platform.numerical.tjsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('Platform.numerical.cz')" width="200">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-edit-outline" @click="Edit(scope.row)">{{$t('Platform.numerical.bianji')}}</el-button>
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">{{$t('Platform.numerical.shanchu')}}</el-button>
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
import { dictionaryList, deleteDictionary, addDictionaryTable, switchDictionaryDetail } from '@/api/Platform/numerical'
import { mixinstest } from '@/mixins/index'
export default {
  name: 'dictionarylist',
  mixins: [mixinstest],
  data() {
    return {
      language:"",
      tableData: [],
      loading: true,
      inputInfo: {
        code: '',
        name: '',
        attribute: '',
        lang_code: ''
      },

      el_switch: true,
      countryList: [],
      languages: [],
      lang_code: '',
      country_num: '',
      // table高度
      tableHeight: null,
    }
  },

  created() {

    this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal();

    this.language = this.getCookit()

    if (this.$route.params.searchInfo) {
      this.inputInfo = {
        ...this.inputInfo,
        ...this.$route.params.searchInfo
      }
    }

    if(this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.getDictionaryList()
    this.getCountrys();
    this.getLanguage();
  },

  mounted() {
    this.$nextTick(function() {
      this.getHeight()
    })
    window.addEventListener('resize',this.getHeight(),false)

  },

  methods: {
    getIds(value) {
      this.country_num = value
    },

    getlangCode(value) {
      this.lang_code = value
    },

    async getCountrys() {
      try {
        const result = await this.LaiKeCommon.getCountries();
        this.countryList =  result.data.data;
      } catch (error) {
        console.error('获取国家列表失败:', error);
      }
    },

    async getLanguage() {
      try {
        const result = await this.LaiKeCommon.getLanguages();
        this.languages = result.data.data;
      } catch (error) {
        console.error('获取语种列表失败:', error);
      }
    },

    // 获取cookiet
    getCookit(){
      let myCookie = document.cookie.split(';').map(item=>{
         let arr = item.split('=')
         return {name:arr[0],value:arr[1]}
       })
       let strCookit = ''
       myCookie.forEach(item=>{
         if(item.name.indexOf('language')!==-1){
           strCookit = item.value
         }
       })
       return strCookit
    },
    getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
		},
    async getDictionaryList() {
      const res = await dictionaryList({
        api: 'saas.dic.getDictionaryInfo',
        storeType: 8,
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        lang_code: this.inputInfo.lang_code,
        dicNo: this.inputInfo.code,
        key: this.inputInfo.name,
        text: this.inputInfo.attribute,
      })
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },

    reset() {
      this.inputInfo.code = ''
      this.inputInfo.name = ''
      this.inputInfo.attribute = ''
      this.inputInfo.lang_code = ''
    },

    demand() {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getDictionaryList().then(() => {
        this.loading = false
        if(this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    //选择一页多少条
		handleSizeChange(e){
      this.loading = true
			console.log(e);
      // this.current_num = e
      this.pageSize = e
      this.getDictionaryList().then(() => {
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
      this.getDictionaryList().then(() => {
        this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.loading = false
      })

		},

    Edit(value) {

      this.$router.push({
        name: 'compile',
        params: {
          ...value,
          searchInfo: { ...this.inputInfo },
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize
        },
        query: {
          dictionaryNum: this.dictionaryNum,
          lang_code: this.lang_code,
          pageSize: this.pageSize
        }
      })
    },

    switchs(value) {
      // console.log(value);
      switchDictionaryDetail({
        api: 'saas.dic.switchDictionaryDetail',
        id: value.id,
        token: this.$store.getters.token
      }).then(res => {
        this.getDictionaryList()
        console.log(res);
        if(value.status === 1) {
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
      })
    },

    Delete(value) {
      console.log(value);
      this.$confirm(this.$t('Platform.numerical.qrsc'), this.$t('Platform.numerical.ts'), {
        confirmButtonText: this.$t('Platform.numerical.ok'),
        cancelButtonText: this.$t('Platform.numerical.cancel'),
        type: 'warning'
      }).then(() => {
        deleteDictionary({
          api: 'saas.dic.delDictionaryDetailInfo',
          idList: value.id
        }).then(res => {
          if(res.data.code == '200') {
            this.getDictionaryList()
            dictionaryList({
              api: 'saas.dic.getDictionaryInfo',
              key: '分页',
              status:1,
            }).then(res => {
              if(res.data.data.list.length !== 0) {
                let pagesize = res.data.data.list.map(item => {
                    return parseInt(item.text)
                })
                pagesize = pagesize.sort(function(a, b){return a - b})
                console.log(pagesize);
                this.pagesizes = pagesize
              }

            })
            this.$message({
              type: 'success',
              message: this.$t('zdata.sccg'),
              offset:102
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
  }
}
</script>

<style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
  /deep/.Search {
    .select-input {
      margin-right: 10px;
    }
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
        // margin-right: 10px;
        .Search-input {
          width: 280px;
          margin-right: 10px;
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
        .fontColor {
          color: #6a7076;
          background-color: #fff;
          border: 1px solid #d5dbe8;
        }
        .fontColor:hover {
          background-color: #fff;
          color: #2890ff;
          border: 1px solid #2890ff;
        }
        .export {
          position: absolute;
          right: 30px;
        }
      }
    }
  }

  /deep/.jump-list {
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
    button {
      min-width: 150px;
    }
  }

  /deep/.jump-list2 {
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
    button {
      min-width: 220px;
    }
  }

  .dictionary-list {
    // height: 605px;
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
        }
      }
    }
  }
}
</style>
