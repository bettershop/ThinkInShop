<template>
    <div class="container">
      <div class="btn-nav">
        <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
          <el-radio-button label="2" @click.native.prevent="$router.push('/plug_ins/myWebConfig/configList')">{{$t('webConfig.pcConfig')}}</el-radio-button> 
          <el-radio-button label="1" @click.native.prevent="$router.push('/plug_ins/myWebConfig/floorList')" >{{$t('webConfig.lcpz')}}</el-radio-button>
        </el-radio-group>
      </div>
      <div class="Search" style=" margin-top: 16px;">
        <div class="Search-condition">
          <div class="query-input">
            <el-input
              v-model="inputInfo.name"
              size="medium"
              @keyup.enter.native="demand"
              class="Search-input"
              :placeholder="$t('floorList.qsrlcmc')"
            ></el-input>
          </div>
          <div class="btn-list">
            <el-button class="fontColor" @click="reset">{{
              $t('DemoPage.tableExamplePage.reset')
            }}</el-button>
            <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{
              $t('DemoPage.tableExamplePage.demand')
            }}</el-button>
  
          </div>
        </div>
      </div>
      <div class="jump-list">
        <el-button
          class="bgColor laiketui laiketui-add"
          type="primary"
          @click="dialogShow2()"
        >
          {{ $t("floorList.xzlc") }}</el-button
        >
      </div>
      <div class="merchants-list" ref="tableFather">
        <el-table
          :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')"
          v-loading="loading"
          :data="tableData"
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
          <el-table-column prop="id" :label="$t('floorList.lcid')"> </el-table-column>
          <el-table-column prop="block_name" :label="$t('floorList.lcmc')"> </el-table-column>
          <el-table-column :label="$t('floorList.sfkq')">
            <template slot-scope="scope">
              <el-switch
                v-model="scope.row.enable_or_not"
                @change="switchs(scope.row)"
                :active-value="1"
                :inactive-value="2"
                active-color="#00ce6d"
                inactive-color="#d4dbe8"
              ></el-switch>
            </template>
          </el-table-column>
          <el-table-column prop="add_date" :label="$t('floorList.tjsj')">
            <template slot-scope="scope">
              <span>{{ dateFormat('YYY-mm-dd HH:MM', scope.row.add_date) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sort" :label="$t('floorList.px')">
            <template slot-scope="scope">
              <el-input
                style="width: 100px;text-align: center;"
                :controls="false"
                v-model="scope.row.sort"
                @change="changesort(scope.row)"
                @input="scope.row.sort=scope.row.sort.replace(/^(0+)|[^\d]+/g,'')"
                :min="1"
              ></el-input>
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="$t('floorList.cz')" width="300">
            <template slot-scope="scope">
              <div class="OP-button">
                <!-- icon="el-icon-good" -->
                <el-button
                  class="goodList"
                  icon="el-icon-notebook-2"
                  @click="godetail(scope.row)"
                >
                  {{ $t('floorList.splb') }}</el-button
                >
                <el-button icon="el-icon-edit-outline" @click="edit(scope.row)">
                  {{ $t('floorList.bj') }}</el-button
                >
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">
                  {{ $t('floorList.sc') }}</el-button
                >
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="pageBox" ref="pageBox">
          <div class="pageLeftText">{{ $t("DemoPage.tableExamplePage.show") }}</div>
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
      <!-- 添加楼层 -->
      <div class="dialog-block">
        <el-dialog
          :title="dialog_title"
          :visible.sync="dialogVisible2"
          :before-close="handleClose2"
        >
          <el-form class="demo-ruleForm" label-width="auto">
              <el-form-item :required="true" :label="$t('floorList.lcmc')" prop="">
                <el-input
                  maxlength="20"
                  v-model="ruleForm2.name"
                  :placeholder="$t('floorList.qsrlcmc')"
                ></el-input>
              </el-form-item>
              <el-form-item :label="$t('floorList.pxh')" :required="true">
                <el-input
                  v-model="ruleForm2.sort"
                  v-on:input="ruleForm2.sort=ruleForm2.sort.replace(/^(0+)|[^\d]+/g,'')"
                  :placeholder="$t('floorList.qsrpxh')"
                ></el-input>
              </el-form-item>
              <el-form-item :label="$t('floorList.lctb')">
                <l-upload
                  v-if="dialogVisible2"
                  :limit="1"
                  v-model="ruleForm2.img"
                  ref="lupload"
                  :mask_layer="false"
                  :text="$t('brandmanagement.addbrand.jy120')"
                >
                </l-upload>
  
              </el-form-item>
              <div class="form-footer">
                <el-form-item>
                <el-button class="bdColor" @click="handleClose2" plain>{{
                  $t('DemoPage.tableFromPage.cancel')
                }}</el-button>
                <el-button type="primary" @click="submitForm()">
                  {{
                    $t('zdata.ok')
                  }}
                </el-button>
              </el-form-item>
              </div>
          </el-form>
        </el-dialog>
      </div>
    </div>
  </template>
  
  <script>
  import { getFloorInfo, addFloor, delFloor ,openFloor} from '@/api/goods/floor'
  import { exports } from '@/api/export/index'
  import { mixinstest } from '@/mixins/index'
  import request from '@/api/https'
  
  export default {
    name: 'floorList',
    mixins: [mixinstest],
    data () {
      return {
        tableData: [],
        loading: true,
        inputInfo: {
          name: ''
        },
        button_list: [],
        // 弹框数据
        dialogVisible: false,
        dialogVisible2: false,
        tableHeight: null,
        ruleForm2: {
          name: '',
          sort: '',
          img: '',
          id: ''
        },
        editid: '',
        dialog_title:this.$t('floorList.xzlc'),
        radio1:1
      }
    },
  
    created () {
      if (this.$route.params.pageSize) {
        this.pagination.page = this.$route.params.dictionaryNum
        this.dictionaryNum = this.$route.params.dictionaryNum
        this.pageSize = this.$route.params.pageSize
      }
      this.getFloorInfos()
    },
  
    mounted () {
      this.$nextTick(function () {
        this.getHeight()
      })
      window.addEventListener('resize', this.getHeight(), false)
    },
  
    methods: {
      switchs(row) {
        openFloor({
          api: "admin.block.EnableOrNot",
          id: row.id,
        }).then((res) => {
          if (res.data.code == "200") {
            this.$message({
              message: this.$t('zdata.czcg'),
              type: "success",
              offset: 102,
            });
          }
          this.getFloorInfos();
        });
      },
      changesort (value) {
        getFloorInfo({
          api: 'admin.block.add',
          id: value.id,
          sort: value.sort
        }).then(res => {
          console.log(res)
          if (res.data.code == 200) {
            this.getFloorInfos()
            this.$message({
              type: 'success',
              message: this.$t('zdata.czcg'),
              offset: 102
            })
          }
        })
      },
      getHeight () {
        this.tableHeight =
          this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
        console.log(this.$refs.tableFather.clientHeight)
      },
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
      // 获取楼层信息
      async getFloorInfos () {
        const res = await getFloorInfo({
          api: 'admin.block.list',
          storeType: 8,
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          name: this.inputInfo.name
        })
        console.log(res)
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
      submitForm () {
        if (this.ruleForm2.name == '') {
          this.warnMsg(this.$t('floorList.qsrlcmc'))
          return
        }
        if (this.ruleForm2.sort == '') {
          this.warnMsg(this.$t('floorList.qsrpxh'))
          return
        }
        let { entries } = Object
  
        let data = {
          api: 'admin.block.add',
          name: this.ruleForm2.name,
          sort: this.ruleForm2.sort,
          img: this.ruleForm2.img,
          id: this.editid
        }
  
        let formData = new FormData()
        for (let [key, value] of entries(data)) {
          formData.append(key, value)
        }
  
        addFloor(formData).then(res => {
          if (res.data.code == '200') {
            console.log(res)
            if(this.editid == ''){
              this.$message({
                  type: "success",
                  message: this.$t("zdata.tjcg"),
                  offset: 102,
                });
            }else{
              this.$message({
                  type: "success",
                  message: this.$t("zdata.bjcg"),
                  offset: 102,
                });
            }
            this.dialogVisible2 = false
            this.ruleForm2.name = ''
            this.ruleForm2.img = ''
            this.ruleForm2.sort = ''
            this.ruleForm2.id = ''
            this.getFloorInfos()
          }
        })
      },
      // 重置
      reset () {
        this.inputInfo.name = ''
      },
  
      // 查询
      demand () {
        // this.currpage = 1
        // this.current_num = 10
  
        this.loading = true
        this.dictionaryNum = 1
        this.getFloorInfos().then(() => {
          this.loading = false
          if (this.tableData.length > 5) {
          }
        })
      },
  
      getClass (value) {
        return value.join(',')
      },
  
      //选择一页多少条
      handleSizeChange (e) {
        this.loading = true
        console.log(e)
        // this.current_num = e
        this.pageSize = e
        this.getFloorInfos().then(() => {
          this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
          this.current_num =
            this.tableData.length === this.pageSize
              ? this.dictionaryNum * this.pageSize
              : this.total
          this.loading = false
        })
      },
  
      //点击上一页，下一页
      handleCurrentChange (e) {
        this.loading = true
        this.dictionaryNum = e
        this.currpage = (e - 1) * this.pageSize + 1
        this.getFloorInfos().then(() => {
          this.current_num =
            this.tableData.length === this.pageSize
              ? e * this.pageSize
              : this.total
          this.loading = false
        })
      },
  
      godetail (value) {
        this.$router.push({
          name: 'floorDetail',
          params: value,
          query: {
            id: value.id
          }
        })
      },
  
      edit (value) {
        request({
          method: 'post',
          params: {
            api: 'admin.block.selectOne',
            id: value.id
          }
        }).then(res => {
          this.editid = value.id
          this.ruleForm2.name = res.data.data.name
          this.ruleForm2.img = value.img
          this.ruleForm2.id = res.data.data.id
          this.ruleForm2.sort = res.data.data.sort
          this.dialog_title = this.$t("floorList.bjlc")
          this.dialogVisible2 = true
          console.log(this.ruleForm2,value,'this.ruleForm2')
        })
      },
  
      // 删除
      Delete (value) {
        console.log(value)
        this.$confirm(this.$t("floorList.qdscm"), this.$t("floorList.ts"), {
          confirmButtonText: this.$t("floorList.qr"),
          cancelButtonText: this.$t("floorList.qx"),
          type: 'warning'
        })
          .then(() => {
            delFloor({
              api: 'admin.block.deleteBlock',
              id: value.id
            }).then(res => {
              if (res.data.code == '200') {
                console.log(res)
                let totalPage = Math.ceil((Number(this.total) - 1) / Number(this.pageSize))
                let dictionaryNum =this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum
                this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum
                this.getFloorInfos()
                this.$message({
                  type: "success",
                  message: this.$t("zdata.sccg"),
                  offset: 102,
                });
              }
            })
          })
          .catch(() => {
            // this.$message({
            //   type: 'info',
            //   message: '已取消删除'
            // })
          })
      },
      addOne () {
        this.labellist.push('')
      },
  
      minus (index) {
        if (this.labellist.length !== 0) {
          this.labellist.splice(index, 1)
        }
      },
      // 弹框方法
      dialogShow () {
        this.dialogVisible = true
      },
      dialogShow2 () {
        this.dialogVisible2 = true
        this.dialog_title = this.$t("floorList.xzlc")
        this.editid = ''
      },
      handleClose (done) {
        this.dialogVisible = false
      },
      handleClose2 (done) {
        this.dialogVisible2 = false
        this.ruleForm2.name = ''
        this.ruleForm2.img = ''
        this.ruleForm2.sort = ''
        this.ruleForm2.id = ''
        this.editid = ''
        console.log(this.ruleForm2.img,'this.ruleForm2.img')
      },
    }
  }
  </script>
  
  <style scoped lang="less">
  /deep/.el-icon-good {
    background: url('../../../assets/imgs/good_list.png') center center
      no-repeat;
    background-size: 13px;
  }
  
  /deep/.el-icon-good:before {
    content: '替';
    font-size: 12px;
    visibility: hidden;
  }
  /deep/.goodList:hover {
    .el-icon-good {
      background: url('../../../assets/imgs/good_listb.png') center center
        no-repeat;
      background-size: 13px;
    }
  }
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
      width: 100%;
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      .laiketui-add:before {
        font-size: 14px;
        margin-right: 8px;
      }
      button {
        min-width: 120px;
        height: 40px;
        background: #28b6ff;
        border-radius: 4px;
        // padding: 0;
        border: none;
        span {
          font-size: 14px;
        }
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
          .el-input__inner{
            text-align: center;
          }
        }
      }
  
      /deep/.el-table {
        /deep/.OP-button {
          display: flex;
          /deep/button {
            min-width:82px !important;
            min-height: 82px !important;
            margin-left: 8px;
          }
          .OP-buttonBox {
            button {
              line-height: 1rem;
            }
          }
          .OP-button-top {
            margin-bottom: 8px;
            .laiketui-zhiding:before {
              margin-right: 5px;
            }
          }
        }
      }
    }
  }
  
  .dialog-block {
      // 弹框样式
      /deep/.el-dialog {
        width: 580px;
        height: 400px;
        .el-dialog__header {
          width: 100%;
          height: 58px;
          line-height: 58px;
          font-size: 16px;
          margin-left: 19px;
          font-weight: bold;
          border-bottom: 1px solid #e9ecef;
          box-sizing: border-box;
          margin: 0;
          padding: 0 0 0 19px;
          .el-dialog__headerbtn {
            font-size: 18px;
            top: 0 !important;
          }
          .el-dialog__title {
            font-weight: normal;
          }
        }
        .l-upload{
          .el-dialog__header{
            .el-dialog__headerbtn{
              top:0
            }
          }
          .el-dialog__body {
            padding-bottom: 0;
            padding-left: 0;
            padding-right: 0;
            padding-top: 0;
            .pass-input{
              padding-bottom: 18px;
            }
          }
        }
  
        .el-dialog__body {
          // padding: 41px 60px 21px 60px !important;
          .el-form-item__label {
            font-weight: normal;
          }
          .demo-ruleForm {
            .el-form-item {
              display: flex;
            }
            .el-form-item__content {
              margin-left: 0 !important;
            }
  
          }
          .form-footer {
            width: 100%;
            height: 72px;
            position: absolute;
            bottom: 0;
            right: 0;
            border-top: 1px solid #e9ecef;
            .el-form-item {
              text-align: right;
              margin-right: 10px;
              padding: 0 !important;
              height: 100%;
              display: block !important;
              .el-form-item__content {
                height: 100%;
                line-height: 72px;
                margin: 0 !important;
              }
            }
            .bgColor:hover {
              background-color: #fff;
              color: #2890ff;
              border: 1px solid #2890ff;
            }
          }
          .el-input {
            width: 380px;
          }
        }
      }
    }
  .attribute-values {
    .el-form-item__content {
      display: flex;
      flex-direction: column;
      .add-info {
        display: flex;
        &:not(:last-child) {
          margin-bottom: 20px;
        }
        .el-input {
          width: 100%;
          height: 40px;
          input {
            width: 100%;
            height: 40px;
          }
        }
  
        .add-reduction {
          display: flex;
          margin: 8px 0;
          i {
            font-size: 20px;
            margin-left: 10px;
          }
          .el-icon-circle-plus-outline {
            color: #2890ff;
          }
        }
      }
    }
  }
  </style>
  