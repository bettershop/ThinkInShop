<template>
  <div class="container merchants-list" >
    <div class="order-info form-card">
     <!-- 搜索配置 -->
     <div class="form-card">
            <div class="title">{{ $t(`systemManagement.searchpz`) }}</div>
            <el-form
              :model="ruleForm"
              :rules="rules"
              label-position="right"
              ref="ruleForm"
              label-width="auto"
              class="form-search"
              >
            <el-form-item :label="$t(`systemManagement.searchpz`)" style="">

              <div class="iframe-row-card" >
                <div style="margin: 22px 0 22px 0">
                  <!-- <div class="card-row-left">关键词上限</div> -->
                  <!-- <div class="card-row-right"> -->
                  <el-form-item
                    :label="$t(`systemManagement.gjcsx`)"
                    prop="limitNum"
                    :label-width="language === 'en' ? '170px' : '100px'"
                  >
                    <el-input
                      v-model="ruleForm.limitNum"
                      :placeholder="$t('searchConfig.qsrgjcsx')"
                      @blur="ruleForm.limitNum = oninput2(ruleForm.limitNum)"
                      @keyup.native="
                        ruleForm.limitNum = oninput3(ruleForm.limitNum)
                      "
                    ></el-input>
                  </el-form-item>
                  <!-- </div> -->
                </div>
                <div style="margin: 0 0 22px 0">
                  <el-form-item
                    :label="$t(`systemManagement.gjc`)"
                    prop="keyword"
                    :label-width="language === 'en' ? '170px' : '100px'"
                  >
                    <el-input
                      v-model="ruleForm.keyword"
                      type="textarea"
                      :placeholder="$t('searchConfig.qsrgjc')"
                    ></el-input>
                    <span style="color: #97a0b4"
                      >({{ $t("searchConfig.gjcbz") }})</span
                    >
                  </el-form-item>
                </div>
              </div>
            </el-form-item>
          </el-form>
          </div>
      </div>

    <!-- 敏感词 -->
    <div class="order-info form-card">
      <div class="title">{{ $t("plugInsSet.liveSetInfo.mgzsz") }}</div>
        <!-- 操作按钮 -->
        <div class="jump-list">
          <div>
            <el-button
              class="bgColor laiketui laiketui-add"
              type="primary"
              @click="addQualifier"
              >{{ $t('plugInsSet.liveSetInfo.addmgz') }}</el-button
            >
            <el-button
              class="bgColor laiketui laiketui-add"
              type="primary"
              @click="batchImport"
              >{{ $t('plugInsSet.liveSetInfo.mgzpldr') }}</el-button
            >
            <el-button
              class="bgColor laiketui laiketui-add"
              type="primary"
              @click="deleteItem"
              >{{ $t('plugInsSet.liveSetInfo.mgzplsc') }}</el-button
            >
          </div>
          <div class="Search-condition">
            <div class="query-input">
              <el-input v-model="mgcForm.mgcText" size="medium" @keyup.enter.native="demand" class="Search-input" :placeholder="$t('plugInsSet.liveSetInfo.qsrmgzc1')"></el-input>
              <el-button class="fontColor" @click="mgcForm.mgcText = ''">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
              <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
           </div>
        </div>
        </div>
        <div class="order-block">
          <el-table
              :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
              v-loading="loading"
              :header-cell-style="{ 'text-align': 'center' }"
              :cell-style="{ 'text-align': 'center' }"
              :data="tableData"
              @selection-change="handleSelectionChange"
              ref="table"
              class="el-table"
              style="width: 100%"
            >
              <el-table-column type="selection"> </el-table-column>
              <el-table-column
                prop="word"
                :label="$t('plugInsSet.liveSetInfo.mgc')"/>
              <el-table-column  prop="add_time" :label="$t('plugInsSet.liveSetInfo.cjrq')"/>
              <el-table-column
                :label="$t('physicalgoods.cz')"
                fixed="right"
                width="200"
              >
                <template slot-scope="scope">
                  <div class="OP-button">
                    <div class="OP-button-bottom">
                      <el-button
                        icon="el-icon-document-copy"
                        @click="updataItem(scope.row)"
                        class="bottom-btn"
                        >{{ $t('commentList.xg') }}</el-button
                      >
                      <el-button
                        icon="el-icon-top"
                        @click="deleteItem(scope.row)"
                        >{{ $t('physicalgoods.shanchu') }}</el-button
                      >
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
        </div>
        <div class="pageBox" ref="pageBox">
        <div class="pageLeftText"  v-if="total > 0">
          {{ $t('DemoPage.tableExamplePage.show') }}
        </div>
        <el-pagination
          v-if="total > 0"
          layout="sizes, slot, prev, pager, next"
          :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
          :next-text="$t('DemoPage.tableExamplePage.next_text')"
          @size-change="handleSizeChange"
          :page-sizes="pagesizes"
          :current-page="dictionaryNum"
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

      <!-- 添加 -->
    <el-dialog
        :title="$t('plugInsSet.liveSetInfo.addmgz')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
        width="700px"
      >
      <el-form :rules="rules" ref="mgcForm" class="mgc-form" label-width="150px" :model="addFrom">
        <el-form-item :label="$t('plugInsSet.liveSetInfo.mgc') " prop="mgcText">
          <el-input :placeholder="$t('plugInsSet.liveSetInfo.qsrmgzc')" maxlength="50"  show-word-limit v-model="addFrom.mgcText"></el-input>
        </el-form-item>
        <el-form-item >
          <div>
            <el-button @click="addQualifier">取消</el-button>
            <el-button type="primary" @click="submit" :disabled="!booleSwitch" :loading="!booleSwitch" >确定</el-button>
          </div>
          </el-form-item>
      </el-form>
    </el-dialog>
      <!-- 导入 -->
    <div class="dialog-export">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('plugInsSet.liveSetInfo.mgzdr')"
        :visible.sync="dialogVisible1"
      >
        <div style="text-align: center;">
            <div class="item item-center" >
              <el-upload el-upload
              class="upload-demo"
              ref="upload"
              accept=".xlsx,.xls,.csv"
              :action="actionUrl"
              name="file"
              :data="uploadData"
              :show-file-list="false"
              :before-upload="handleBeforeUpload"
                :on-success="handleUploadSuccess"
                multiple
                >
                <i class="el-icon-document-copy"></i>
              {{ $t('plugInsSet.liveSetInfo.mgzdrwb')}}
            </el-upload>
          </div>
          <div class="uploads"><a href="./sensitive_words.xlsx">{{$t('batchImport.xzpld')}}</a></div>
        </div>
      </el-dialog>

      <div class="footerBox">
            <!-- <el-button plain class="footer-cancel fontColor" @click="$router.go(-1)">取消</el-button> -->
            <el-button
              type="primary"
              class="footer-save bgColor mgleft"
              @click="submitForm('ruleForm')"
              >{{ $t("DemoPage.tableFromPage.save") }}</el-button
            >
      </div>
    </div>
  </div>
</template>

<script>
import {
  getSystemIndex
} from '@/api/mall/systemManagement'
import { mixinstest } from '@/mixins/index'
import { getSecConfig } from "@/api/plug_ins/preSale";
import Config from "@/packages/apis/Config";
let actionUrl = Config.baseUrl;
import { removeStorage, getStorage } from '@/utils/storage'

export default {
    mixins: [mixinstest],
    name:'sensitiveWords',
    data(){
        return {
            actionUrl,
            language:'',
            loading: false,
            dialogVisible1: false,
            dialogVisible: false,
            booleSwitch: true,
            ruleForm:{
              keyword:'',
              limitNum:'',

            },
            upItemId:"",
            addFrom:{
                mgcText:'',// 添加限制词
            },
            mgcForm:{
                mgcText:'',// 列表查询条件
            },
            tableData:[],// 敏感词列表 数组
            ids:[], // 被选中的敏感词 数组
            fileName:'',
            file:'',
            tag:false,
            rules: {
                limitNum: [
                  {
                    required: true,
                    message: this.$t('searchConfig.qsrgjcsx'),
                    trigger: 'blur'
                  }
                ],
                keyword: [
                  {
                    required: true,
                    message: this.$t('searchConfig.qsrgjc'),
                    trigger: 'blur'
                  }
                ],

                mgcText: [
                  {
                    required: true,
                    message: this.$t('searchConfig.qsrmgc'),
                    trigger: 'blur'
                  }
                ],


              },
        }
    },
    computed: {
    // 导入文件 接口
    uploadData() {
      {
        const userInfo = getStorage('laike_admin_userInfo')

          return {
          //商城id
          storeId: userInfo.storeId,
          //来源
          storeType: 8,

            api: "admin.system.importSensitives"
          };
        }
      }
    },
    created(){
      this.language = this.getCookit()
      this.indexs()
      this.getSystemIndexs()

    },
    methods:{
        // 收索配置保存
        submitForm(){
          this.$refs['ruleForm'].validate((value)=>{
            if(value){
              getSecConfig({
              api: "admin.system.SearchAndSensitiveWords",
              limitNum: this.ruleForm.limitNum, // 关键词上限
              keyword:  this.ruleForm.keyword // 关键词
              }).then((res) => {
                if(res.data.code == 200){
                    this.succesMsg(this.$t('commonLanguage.xgcg'))
                    this.indexs()
                }
            });
            }

          })

        },
        handleClose (done) {
            this.upItemId = ''
            this.$nextTick(()=>{
                this.$refs.mgcForm.resetFields()
            })
            done()
        },
        // 添加\关闭 限制词弹窗
        addQualifier(){
          this.upItemId = ''
            this.addFrom.mgcText = ''
            this.dialogVisible = !this.dialogVisible
        },
         // 修改
        updataItem({id,word}){
            this.upItemId=id
            this.addFrom.mgcText=word
            this.dialogVisible = !this.dialogVisible
        },
        // 删除
        deleteItem({id}=''){
            if(!id && this.ids.length==0){
                this.warnMsg(this.$t('plugInsSet.liveSetInfo.qxzxyscdmgc'))
                return
            }
            this.confirmBox(this.$t('plugInsSet.liveSetInfo.qdscgmgc')).then(() => {
                getSecConfig({
                  api: "admin.system.deleteSensitive",
                  ids: id||this.ids.join(',')
                  }).then((res) => {
                    if(res.data.code == 200){
                        this.succesMsg(this.$t('commonLanguage.xgcg'))
                        this.indexs()
                    }
                });
            })
        },
        // 批量导入
        batchImport(){
            this.dialogVisible1 = !this.dialogVisible1
        },
        async submit(){
            if(!this.booleSwitch){
                return
            }
            await this.$refs['mgcForm'].validate((value)=>{
                if(value){
                this.booleSwitch = false
                getSecConfig({
                    api: "admin.system.addSensitive",
                    word:this.addFrom.mgcText,
                    // upItemId 有值就是修改没有就是 添加
                    id:this.upItemId||''
                }).then((res) => {
                    if(res.data.code == 200){
                        if(!this.upItemId){
                        this.succesMsg(this.$t('commonLanguage.tjcg'))
                        }else{
                        this.succesMsg(this.$t('commonLanguage.bjcg'))
                        }
                        this.dialogVisible = false
                        this.demand()
                    }
                }).finally(()=>{
                    setTimeout(()=>{
                    this.booleSwitch = true
                    },1500)
                })
                }
            })
        },
        //点击上一页，下一页
        handleCurrentChange (e) {
            this.loading = true
            this.dictionaryNum = e
            console.log(this.pageSize);

            this.currpage = (e - 1) * (this.pageSize||0) + 1
            this.indexs().then(() => {
                this.current_num =
                this.tableData.length ===  (this.pageSize||0)
                    ? e *  (this.pageSize||0)
                    : this.total
                this.loading = false
            })
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
        demand(){
          this.handleCurrentChange(1)
        },
        handleSelectionChange(e){
            this.ids = e.map(v=>v.id)
        },
        // 上传之前的处理
        handleBeforeUpload(file) {
          this.fileName = file.name;
          this.file = file;
          this.tag = true;
        },
        // 上传成功
        handleUploadSuccess(res) {
          // 清空 文件
          this.$refs.upload.clearFiles();
            if(res.code == 200){
              this.succesMsg(this.$t('zdata.czcg'))
              this.dialogVisible1 =false
              this.demand()
            }else{
              this.errorMsg(res.message)
            }
        },
        // 列表查询
        async indexs () {
            await getSecConfig({
                api: "admin.system.selectSensitive",
                word:this.mgcForm.mgcText,
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
            }).then((res) => {
              if (res.data.code == 200) {
                   const {list,total} = res.data.data
                    this.tableData = list
                    this.total = total
                    this.current_num = total
                }
            });
        },
         // 获取cookiet
        getCookit() {
          let myCookie = document.cookie.split(';').map(item => {
            let arr = item.split('=')
            return { name: arr[0], value: arr[1] }
          })
          let strCookit = ''
          myCookie.forEach(item => {
            if (item.name.indexOf('language') !== -1) {
              strCookit = item.value
            }
          })
          return strCookit
        },
        async getSystemIndexs() {
          let { entries } = Object
          let data = {
            api: 'admin.system.GetSearchAndSensitiveWords'
          }
          let formData = new FormData()
          for (let [key, value] of entries(data)) {
            formData.append(key, value)
          }
          const res = await getSystemIndex(formData)
          // 搜索配置
          this.ruleForm.limitNum = res.data.data.list.num
          this.ruleForm.keyword = res.data.data.list.keyword
        },
    }
}
</script>

<style scoped lang="less">
.jump-list{
  justify-content: space-between;
  margin: 0px 10px;
  /deep/ span{
    margin-left: 5px;
  }
}
  .footerBox {
    position: fixed;
    right: 0;
    bottom: 40px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 15px 20px;
    border-top: 1px solid #E9ECEF;
    background: #FFFFFF;
    width: 300%;
    z-index: 10;
    button {
      width: 70px;
      height: 40px;
    }
  }

.query-input{
  display: flex;
  .Search-input{
    width: 200px;
  }
  .fontColor{
    border:1px solid #ccc
  }
}
.iframe-row-card {
    width: 710px;
    // height: 189px;
    background-color: #F4F7F9;
    // padding-bottom: 14px;
    // margin-top: 10px;
    margin-left: 3px;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    padding-right: 16px;
    padding-left: 10px;
    .select-input-box{
      display: flex;
      justify-content: space-between;
      .gap{
        width: 10px;
        height: 40px;

      }
    }
    .select-input {
        // width: 31.4%;
        width: 100%;
        height: 40px;
        input{
          width: 186px;
          height: 40px;
        }
        &:not(:last-child) {
          margin-right: 11px !important;
        }
      }
      .select-input2 {
        width: 162px;
        height: 40px;
        input{
          width: 162px;
          height: 40px;
        }
        &:not(:last-child) {
          margin-right: 11px !important;
        }
      }

}
</style>
