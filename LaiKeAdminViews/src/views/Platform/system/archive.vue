<template>
  <div class="f_container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">

        <el-radio-button
          label="2"
          @click.native.prevent="$router.push('/Platform/system/graphics')"
          >{{ $t("archive.tpsz") }}</el-radio-button
        >
        <el-radio-button
          label="3"
          @click.native.prevent="$router.push('/Platform/system/archive')"
          >{{ $t("archive.sjkbf") }}</el-radio-button
        >
        <el-radio-button v-if="isJava" label="4" @click.native.prevent="openScheduledTask">{{ $t('archive.dsrw') }}</el-radio-button>

      </el-radio-group>
    </div>
    <div class="form-card">
      <div class="title">{{ $t("archive.bfsz") }}</div>
      <el-form
        :model="ruleForm"
        :rules="rules"
        label-position="right"
        ref="ruleForm"
        label-width="auto"
        class="form-search"
      >
        <el-form-item :label="$t('archive.zdbf')" style="" label-width="120px">
          <el-switch
            v-model="ruleForm.isOpen"
            :active-value="1"
            :inactive-value="0"
            active-color="#00ce6d"
            inactive-color="#d4dbe8"
          >
          </el-switch>
          <div class="iframe-row-card" >
            <div class="iframe-row-card_d">
              <el-form-item :label="$t('archive.zxzq')" style="margin-bottom:20px">
                <el-select v-model="ruleForm.selectDate" @change='change()'>
                  <el-option :label="$t('archive.selectDate.mt')" value="1"></el-option>
                  <el-option :label="$t('archive.selectDate.nt')" value="2"></el-option>
                  <el-option :label="$t('archive.selectDate.mxs')" value="3"></el-option>
                  <el-option :label="$t('archive.selectDate.nxs')" value="4"></el-option>
                  <el-option :label="$t('archive.selectDate.nfz')" value="5"></el-option>
                  <el-option :label="$t('archive.selectDate.mz')" value="6"></el-option>
                  <el-option :label="$t('archive.selectDate.my')" value="7"></el-option>
                </el-select>
                <el-select v-model="ruleForm.execute_cycle[5]" v-if='ruleForm.selectDate==6'>
                  <el-option :label="$t('archive.execute_cycle.zy')" value="1"></el-option>
                  <el-option :label="$t('archive.execute_cycle.zr')" value="2"></el-option>
                  <el-option :label="$t('archive.execute_cycle.zs')" value="3"></el-option>
                  <el-option :label="$t('archive.execute_cycle.zt')" value="4"></el-option>
                  <el-option :label="$t('archive.execute_cycle.zw')" value="5"></el-option>
                  <el-option :label="$t('archive.execute_cycle.zl')" value="6"></el-option>
                  <el-option :label="$t('archive.execute_cycle.zr')" value="7"></el-option>
                </el-select>
                <el-input v-model="ruleForm.execute_cycle[3]" v-if='ruleForm.selectDate==2||ruleForm.selectDate==7'>
                  <template slot="append">{{
                    $t("systemManagement.day")
                  }}</template>
                </el-input>
                <el-input v-model="ruleForm.execute_cycle[2]" v-if='ruleForm.selectDate==1||ruleForm.selectDate==2||ruleForm.selectDate==4||ruleForm.selectDate==6||ruleForm.selectDate==7'>
                  <template slot="append">{{
                    $t("systemManagement.hour")
                  }}</template>
                </el-input>
                <el-input v-model="ruleForm.execute_cycle[1]">
                  <template slot="append">{{
                    $t("systemManagement.fenzhong")
                  }}</template>
                </el-input>
              </el-form-item>
              <el-form-item :label="$t('archive.wjlj')" class='card_d_i' prop="url">
                <el-input v-model="ruleForm.url">
                </el-input>

              </el-form-item>

            </div>
          </div>

        </el-form-item>
        <el-form-item :label="''" style="" label-width="110px">
          <el-button
                class="mybtn"
                type="primary"
                @click="save()"
                >{{ $t('archive.bc') }}</el-button
              >
        </el-form-item>
      </el-form>
    </div>
    <div class="form-card">
      <div class="title">{{ $t('archive.bfwjlb') }}</div>
      <el-button
        class="mybtn"
        type="primary"
        @click='saveNow()'
        >{{ $t('archive.ljbf') }}</el-button
      >
      <div class="dictionary-list" ref="tableFather">
        <el-table
          :data="tableData"
          ref="table"
          class="el-table"
          style="width: 100%"
          :header-cell-style="{backgroundColor: '#f4f7f9',textAlign: 'center'}"
          :cell-style="{textAlign: 'center'}"
        >
          <template slot="empty">
            <div class="empty">
              <img src="@/assets/imgs/empty.png" alt="" />
              <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
            </div>
          </template>
          <el-table-column
            prop="id"
            :label="$t('bulletin.announcementlist.xh')"
            min-width="120"
          >
          </el-table-column>
          <el-table-column
            prop="file_name"
            :label="$t('archive.wjmc')"
          >
          </el-table-column>
          <el-table-column
            prop="file_size"
            :label="$t('archive.wjdx')"
          >
            <template slot-scope="scope">
              <span>{{ scope.row.file_size }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="file_type"
            :label="$t('archive.bflx')"
            min-width="100"
          >
          </el-table-column>
          <el-table-column
            prop="file_url"
            :label="$t('archive.wjlj')"
            min-width="120"
          >

          </el-table-column>
          <el-table-column
            prop="status"
            :label="$t('archive.zt')"
          >
            <template slot-scope="scope">
              <span v-if='scope.row.status==0'>{{ $t('archive.status.bfz') }}</span>
              <span v-if='scope.row.status==1'>{{ $t('archive.status.ybf') }}</span>
              <span v-if='scope.row.status==2'>{{ $t('archive.status.bfb') }}</span>

            </template>
          </el-table-column>
          <el-table-column
            prop="add_time"
            :label="$t('archive.bfsj')"
            min-width="200"
          >
            <template slot-scope="scope">
              <span>{{ scope.row.add_time }}</span>
            </template>
          </el-table-column>
          <el-table-column
            fixed="right"
            prop="operation"
            :label="$t('bulletin.announcementlist.cz')"
            width="200"
          >
            <template slot-scope="scope">
              <div class="OP-button">
                <div class="OP-button-top">
                  <el-button
                    icon="el-icon-circle-close"
                    v-if="scope.row.status == 0"
                    @click="quxiao(scope.row)"
                    >{{ $t('archive.qx') }}</el-button
                  >
                  <el-button
                    icon="el-icon-download"
                    v-if="scope.row.status == 1"
                    @click="Edit(scope.row)"
                    ><a :style='{color:"#888fe9"}' :href="scope.row.xz_url">{{ $t('archive.xz') }}</a></el-button
                  >
                  <el-button
                    icon="el-icon-refresh-left"
                    v-if="scope.row.status == 1"
                    @click="myreturn(scope.row)"
                    >{{ $t('archive.hy') }}</el-button
                  >
                </div>
                <div class="OP-button-bottom">
                  <el-button v-if="scope.row.status != 0" icon="el-icon-delete" @click="Delete(scope.row)">{{
                    $t("bulletin.announcementlist.shanchu")
                  }}</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="pageBox" ref="pageBox">
          <div class="pageLeftText">
            {{ $t("DemoPage.tableExamplePage.show") }}
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
              {{ $t("DemoPage.tableExamplePage.on_show") }}{{ currpage }}-{{
                current_num
              }}{{ $t("DemoPage.tableExamplePage.twig") }}{{ total
              }}{{ $t("DemoPage.tableExamplePage.twig_notes") }}
            </div>
          </el-pagination>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from "@/api/https";
import { mixinstest } from "@/mixins/index";
export default {
  name: "archive",
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      radio1: 3,
      ruleForm: {
        selectDate:'1',
        isOpen: 0,
        url:'',
        execute_cycle:[0,0,0,'*','*','*'],
      },
      rules:{
          // 文件地址
          url: [
            { required: true, message: '请输入文件地址', trigger: 'blur' }
          ],
      },
      tableData:[],

    };
  },
  computed: {
    isJava() {
      return process.env.VUE_APP_LANG_TYPE == 1
    }
  },
  //组装模板
  created() {
    this.loadData();
    this.loadList();
  },

  methods: {
    // 打开定时任务的外部链接
    openScheduledTask() {
      window.open(process.env.VUE_APP_XXL_JOB_URL, '_blank');
    },
    loadData() {
      request({
        method: "post",
        params: {
          api: "admin.BackUp.queryConfig",

        },
      }).then((res)=>{
        if(res.data.code==200){
          let backUpConfigModel=res.data.data.backUpConfigModel
          this.ruleForm.isOpen=backUpConfigModel.is_open
          this.ruleForm.url=backUpConfigModel.url
          this.ruleForm.execute_cycle=backUpConfigModel.execute_cycle.split(' ')
          this.ruleForm.selectDate=backUpConfigModel.query_data.toString()
        }

      });

    },
    async loadList() {
      const res = await request({
        method: "post",
        params: {
          api: "admin.BackUp.backUpRecord",
          page: this.dictionaryNum,
          pageSize: this.pageSize,
        },
      });
      this.tableData=res.data.data.list

      this.total=res.data.data.total

    },
    change(){
      if(this.ruleForm.selectDate==1){
        this.ruleForm.execute_cycle=[0,0,0,'*','*','*']
      }else if(this.ruleForm.selectDate==2){
        this.ruleForm.execute_cycle=[0,0,0,0,'*','*']
      }else if(this.ruleForm.selectDate==3){
        this.ruleForm.execute_cycle=[0,0,'*','*','*','*']
      }else if(this.ruleForm.selectDate==4){
        this.ruleForm.execute_cycle=[0,0,0,'*','*','*']
      }else if(this.ruleForm.selectDate==5){
        this.ruleForm.execute_cycle=[0,0,'*','*','*','*']
      }else if(this.ruleForm.selectDate==6){
        this.ruleForm.execute_cycle=[0,0,0,'*','*','1']
      }else if(this.ruleForm.selectDate==7){
        this.ruleForm.execute_cycle=[0,0,0,0,'*','*']
      }

    },
    saveNow(){
      this.$confirm('是否立即备份？', this.$t("commentList.ts"), {
        confirmButtonText: this.$t("commentList.okk"),
        cancelButtonText: this.$t("commentList.ccel"),
        type: "warning",
      })
        .then(() => {
          request({
            method: "post",
            params: {
              api: "admin.BackUp.immediately",

            },
          }).then((res)=>{
            if(res.data.code==200){
                this.$message({
                message: res.data.message,
                type: 'success',
                offset:102,
              })
              this.loadList()
            }

          });
        })

    },
    //保存配置
    save(){
      let execute_cycle=this.ruleForm.execute_cycle.join(' ')
      console.log(execute_cycle,'this.ruleForm.execute_cycle')

      request({
        method: "post",
        params: {
          api: "admin.BackUp.addConfig",
          is_open:this.ruleForm.isOpen,
          url:this.ruleForm.url,
          execute_cycle:execute_cycle,
          query_data:this.ruleForm.selectDate
        },
      }).then((res)=>{
        if(res.data.code==200){
            this.$message({
            message: res.data.message,
            type: 'success',
            offset:102,
          })
        }

      });
    },
    //下载
    Edit(e){

    },
    myreturn(e){
      this.$confirm('确认还原该备份文件？', this.$t("commentList.ts"), {
        confirmButtonText: this.$t("commentList.okk"),
        cancelButtonText: this.$t("commentList.ccel"),
        type: "warning",
      })
        .then(() => {
          request({
            method: "post",
            params: {
              api: "admin.BackUp.reduction",
              id:e.id
            },
          }).then((res)=>{
            if(res.data.code==200){
                this.$message({
                message: res.data.message,
                type: 'success',
                offset:102,
              })
              this.loadList()
            }

          });
        })

    },
    quxiao(e){
      this.$confirm('确认取消正在备份中的文件？', this.$t("commentList.ts"), {
        confirmButtonText: this.$t("commentList.okk"),
        cancelButtonText: this.$t("commentList.ccel"),
        type: "warning",
      })
        .then(() => {
          request({
            method: "post",
            params: {
              api: "admin.BackUp.cancelTask",
              id:e.id
            },
          }).then((res)=>{
            if(res.data.code==200){
                this.$message({
                message: res.data.message,
                type: 'success',
                offset:102,
              })
              this.loadList()
            }
          });
        })

    },
    Delete(e){
      this.$confirm('确认删除该备份文件？', this.$t("commentList.ts"), {
        confirmButtonText: this.$t("commentList.okk"),
        cancelButtonText: this.$t("commentList.ccel"),
        type: "warning",
      })
        .then(() => {
          request({
            method: "post",
            params: {
              api: "admin.BackUp.delBackUpRecord",
              id:e.id
            },
          }).then((res)=>{
            if(res.data.code==200){
                this.$message({
                message: res.data.message,
                type: 'success',
                offset:102,
              })
              this.loadList()
            }
          });
        })

    },
    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      this.pageSize = e;
      this.loadList().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.loadList().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },
  },
};
</script>

<style scoped lang="less">
.f_container {
  padding: 0 0 1px 0;
  .btn-nav {
    margin-bottom: 20px;
  }
  .form-search{
    padding: 40px 0 22.4px 0;
  }
  /deep/.form-card {
    background-color: #fff;
    border-radius: 4px;
    // padding-bottom: 40px;
    // padding-bottom: 22.4px;
    margin-bottom: 16px;
    .title {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 60px;
      padding: 0 20px;
      border-bottom: 1px solid #e9ecef;
      font-size: 16px;
      font-weight: 400;
      color: #414658;
      box-sizing: border-box;
      // margin-bottom: 40px;
      // position: sticky;
      top: 0;
      background-color: #ffffff;
      border-radius: 4px 4px 0 0;
      // z-index: 1000;
    }
    .el-input-group__append {
      // color: #000;
      color: #414658;
    }
  }
  .mybtn{
    margin: 20px;
    background:rgba(40, 182, 255, 1);
    border-color:rgba(40, 182, 255, 1);
  }
  .iframe-row-card {
    width: 720px;
    // height: 189px;
    background-color: #f4f7f9;
    margin-left: 3px;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    padding: 20px 0 20px 20px;
    .iframe-row-card_d{
      margin: 22px 0 22px 0
    }
    .el-input-group {
      width: 130px;
      margin-right: 10px;
    }
    .el-select {
      width: 90px;
      margin-right: 10px;
    }
    // /deep/.el-form-item__content{
    //   margin-bottom: 20px;
    // }
    .card_d_i{
      /deep/.el-input__inner{
        width:400px;
      }
    }

  }
  .dictionary-list{
    margin:0 20px;

  }
}
</style>
