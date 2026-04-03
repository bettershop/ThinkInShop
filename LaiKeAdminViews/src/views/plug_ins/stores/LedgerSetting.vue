<template>
  <div class="container">
    <el-form
      ref="ruleForm"
      class="form-search"
      :inline="true"
      :rules="rules"
      :model="ruleForm"
      label-width="auto"
    >
      <div class="activity-set">
        <div class="header">
          <span>{{ $t('stores.LedgerSetting.skxx') }}</span>
        </div>
        <div class="activity-block">
          <el-form-item
            class="exchange-price"
            :label="$t('stores.LedgerSetting.fzzh')"
          >
            <!-- <el-input
              :placeholder="$t('stores.LedgerSetting.qqrzshh')"
              v-model="ruleForm.sub_mch_id"
            >
            </el-input> -->
            {{ ruleForm.accounts_set }}
          </el-form-item>
          <el-form-item
            :label="$t('stores.LedgerSetting.scfzbl')"
            class="idClass"
          >
            <!-- <el-input
              v-model="ruleForm.sub_app_id"
              oninput="value=value.replace(/^(-1+)|[^\d]/g,'');value=value<0?'':value>=100?100:value"
              :placeholder="$t('stores.LedgerSetting.qsrzshyyid')"
            ></el-input> -->
            {{ proportion }}%
          </el-form-item>
        </div>
      </div>
      <div class="select-goods">
        <div class="header">
          <span>{{ $t('stores.LedgerSetting.fzxx') }}</span>
          <el-button @click="openDia" class="shaco_btn">{{
            $t('stores.LedgerSetting.tjfzzh')
          }}</el-button>
        </div>
        <div class="goods-block">
          <!-- <div class="Search">
            <div class="Search-condition">
              <div class="jump-list">
                <el-button
                  class="bgColor laiketui laiketui-add"
                  type="primary"
                  @click="openDia"
                  >{{ $t('stores.LedgerSetting.add') }}</el-button
                >
              </div>
            </div>
          </div> -->
          <div class="menu-list" ref="tableFather">
            <el-table
              :data="tableData"
              ref="table"
              :header-cell-style="tableHeaderColor"
              class="el-table"
              style="width: 100%"
              :height="410"
            >
              <template slot="empty">
                <div class="empty">
                  <img src="../../../assets/imgs/empty.png" alt="" />
                  <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
                </div>
              </template>
              <el-table-column prop="id" :label="$t('stores.LedgerSetting.xh')">
                <template slot-scope="scope">
                  <span>{{ scope.$index + 1 }}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="name"
                :label="$t('stores.LedgerSetting.fzjsfqc')"
              >
              </el-table-column>
              <el-table-column
                prop="type"
                :label="$t('stores.LedgerSetting.fzsjflx')"
              >
              </el-table-column>
              <el-table-column
                prop="account"
                :label="$t('stores.LedgerSetting.fzjsfzh')"
              >
              </el-table-column>
              <el-table-column
                prop="relationship"
                :label="$t('stores.LedgerSetting.yfzfdfxlx')"
              >
              </el-table-column>
              <el-table-column
                prop="proportion"
                :label="$t('stores.LedgerSetting.fzbl')"
              >
              </el-table-column>
              <el-table-column
                :label="$t('stores.LedgerSetting.cz')"
                fixed="right"
              >
                <template slot-scope="scope">
                  <div class="OP-button">
                    <div class="OP-button-top" style="justify-content: center">
                      <el-button
                        icon="el-icon-delete"
                        @click="del(scope.$index,scope.row)"
                        >{{ $t('stores.LedgerSetting.yc') }}</el-button
                      >
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <!-- <div class="pageBox" ref="pageBox" v-if="showPagebox">
              <div class="pageLeftText">
                {{ $t('DemoPage.tableExamplePage.show') }}
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
                  {{ $t('DemoPage.tableExamplePage.on_show') }}{{ currpage }}-{{
                    current_num
                  }}{{ $t('DemoPage.tableExamplePage.twig') }}{{ total
                  }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
                </div>
              </el-pagination>
            </div> -->
          </div>
        </div>
      </div>
      <div style="width: 100%; height: 1px"></div>
      <div class="footer-button">
        <el-button
          plain
          class="footer-cancel fontColor"
          @click="$router.go(-1)"
          >{{ $t('stores.LedgerSetting.fh') }}</el-button
        >
        <el-button
          type="primary"
          class="footer-save bgColor mgleft"
          @click="submitForm('ruleForm')"
          >{{ $t('stores.LedgerSetting.bc') }}</el-button
        >
      </div>
    </el-form>
    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('stores.LedgerSetting.add')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <el-form
          :model="ruleForm2"
          :rules="rules2"
          ref="ruleForm2"
          label-width="auto"
          class="demo-ruleForm"
        >
        <el-form-item
            :label="$t('stores.LedgerSetting.fzjsfqc')"
          >
            <el-input
              v-model="ruleForm2.name"
              :placeholder="$t('stores.LedgerSetting.qsrfzjsfqc')"
            ></el-input>
          </el-form-item>
          <el-form-item :label="$t('stores.LedgerSetting.fzsjflx')" prop="type">
            <el-select
              class="select-input"
              v-model="ruleForm2.type"
              :placeholder="$t('stores.LedgerSetting.qxzfzjsflx')"
            >
              <el-option
                v-for="item in tyleList"
                :key="item.value"
                :label="item.text"
                :value="item.value"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item
            :label="$t('stores.LedgerSetting.fzjsfzh')"
            prop="account"
          >
            <el-input
              v-model="ruleForm2.account"
              :placeholder="$t('stores.LedgerSetting.qsrfzjsfzh')"
            ></el-input>
          </el-form-item>
          <el-form-item
            :label="$t('stores.LedgerSetting.yfzfdfxlx')"
            prop="relationship"
          >
            <el-select
              class="select-input"
              v-model="ruleForm2.relationship"
              :placeholder="$t('stores.LedgerSetting.qxzyfzfdgxlx')"
            >
              <el-option
                v-for="item in relaList"
                :key="item.value"
                :label="item.text"
                :value="item.value"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item
            :label="$t('stores.LedgerSetting.fzbl')"
            prop="proportion"
            class="proportion"
          >
            <el-input
              v-model="ruleForm2.proportion"
              :placeholder="$t('stores.LedgerSetting.qtxfzbl')"
              oninput="value=value.replace(/^(-1+)|[^\d]/g,'');value=value<0?'':value>=100?100:value"
            >
              <template slot="append">%</template>
            </el-input>
          </el-form-item>
          <div class="form-footer">
            <el-form-item>
              <el-button class="footer-cancel fontColor" @click="handleClose">{{
                $t('stores.LedgerSetting.qx')
              }}</el-button>
              <el-button type="primary" @click="determine('ruleForm2')">{{
                $t('stores.LedgerSetting.qd')
              }}</el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getBill, saveBill, getFile } from '@/api/plug_ins/stores'
import { mixinstest } from '@/mixins/index'

export default {
  name: 'LedgerSetting',
  mixins: [mixinstest],

  data () {
    return {
      ruleForm: {
        // sub_mch_id: '', //子商户号
        // sub_app_id: '', //子商户应用ID
        accounts_set:'',//商城分账账号
      },
      proportion: 100,
      rules: {
        // sub_mch_id: [
        //   {
        //     required: true,
        //     message: this.$t('stores.LedgerSetting.qqrzshh'),
        //     trigger: 'blur'
        //   }
        // ],
        // sub_app_id: [
        //   {
        //     required: true,
        //     message: this.$t('stores.LedgerSetting.qsrzshyyid'),
        //     trigger: 'blur'
        //   }
        // ]
      },
      rules2: {
        type: [
          {
            required: true,
            message: this.$t('stores.LedgerSetting.qxzfzjsflx'),
            trigger: 'change'
          }
        ],
        account: [
          {
            required: true,
            message: this.$t('stores.LedgerSetting.qsrfzjsfzh'),
            trigger: 'blur'
          }
        ],
        relationship: [
          {
            required: true,
            message: this.$t('stores.LedgerSetting.qxzyfzfdgxlx'),
            trigger: 'change'
          }
        ],
        proportion: [
          {
            required: true,
            message: this.$t('stores.LedgerSetting.qtxfzbl'),
            trigger: 'blur'
          }
        ]
      },
      courierList: [],
      ruleForm2: {
        name:'', //分账接收方全称
        type: '', //分账接收方类型
        account: '', //分账接收方账号
        relationship: '', //分账接收方关系
        proportion: '' //分账比例
      },
      dialogVisible: false,
      tableData: [],
      tyleList: [],
      relaList: [],
      divideAccountInfo: [] //最后传给后端的json
    }
  },

  created () {
    this.getList() //获取分账方信息
    this.getType() //获取分账接收方类型
    this.getRela() //获取分账接收方类型
  },

  watch: {
    'ruleForm.shelves_num': {
      handler: function () {
        if (parseInt(this.ruleForm.shelves_num) == 0) {
          this.ruleForm.shelves_num = ''
        }

        if (this.stock && parseInt(this.ruleForm.shelves_num) > this.stock) {
          this.ruleForm.shelves_num = this.stock
        }
      }
    },

    'inputInfo.goodsClass': {
      handler: function () {
        this.inputInfo.brand = ''
      }
    }
  },

  methods: {
    // 获取分账接收方类型
    async getType () {
      const res = await getFile({
        api: 'saas.dic.getDictionaryInfo',
        pageSize: 9999,
        key: '分账接收方类型',
        status: 1
      })
      this.tyleList = res.data.data.list
    },
    // 获取分账接收方类型
    async getRela () {
      const res = await getFile({
        api: 'saas.dic.getDictionaryInfo',
        pageSize: 9999,
        key: '分账接收方关系',
        status: 1
      })
      this.relaList = res.data.data.list
    },
    openDia () {
      this.dialogVisible = true
    },
    handleClose () {
      this.ruleForm2.name = ''
      this.ruleForm2.type = ''
      this.ruleForm2.account = ''
      this.ruleForm2.relationship = ''
      this.ruleForm2.proportion = ''
      //清除校验
      this.$nextTick(() => {
        this.$refs['ruleForm2'].clearValidate()
      })
      this.dialogVisible = false
    },
    async getList () {
      const res = await getBill({
        api: 'admin.divideAccount.divideAccountInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        mchId: this.$route.query.id
      })
      var arr = res.data.data
      this.current_num = 10
      var sum = arr.list.reduce((accumulator, current) => accumulator + current.proportion, 0);
      this.ruleForm.accounts_set = arr.accounts_set
      this.proportion = 100 - Number(sum)
      // this.ruleForm.sub_mch_id = arr.sub_mch_id
      // this.ruleForm.sub_app_id = arr.sub_app_id
      //   if(arr.list != [] ){
      this.tableData = arr.list.map(item => {
        return {
          name: item.name,
          type: item.typeDesc,
          account: item.account,
          relationship: item.relationshipDesc,
          proportion: item.proportion
        }
      })
      this.divideAccountInfo = arr.list.map(item => {
        return {
          type: item.type,
          account: item.account,
          relationship: item.relationship,
          proportion: item.proportion
        }
      })
      //   }
      //   取消分页
      //   this.total = res.data.data.total
      //   this.tableData = res.data.data.res
      //   if (this.total < this.current_num) {
      //     this.current_num = this.total
      //   }
      //   if (this.total == 0) {
      //     this.showPagebox = false
      //   } else {
      //     this.showPagebox = true
      //   }
    },

    tableHeaderColor ({ row, column, rowIndex, columnIndex }) {
      if (rowIndex === 0) {
        return 'background-color: #f4f7f9;'
      }
    },
    del (index,row) {
      this.tableData.splice(index, 1)
      this.divideAccountInfo.splice(index, 1)
      this.proportion += row.proportion
      console.log('tableData', this.tableData)
      console.log('divideAccountInfo', this.divideAccountInfo)
    },
    // 是否分账
    switchs (row) {
      // switchShow({
      //   api: 'plugin.auction.AdminAuction.switchSession',
      //   accessId: this.$store.getters.token,
      //   id: row.id
      // }).then(res => {
      //   if (res.data.code == '200') {
      //     this.geteventsLists()
      //     this.$message({
      //       type: 'success',
      //       message: this.$t('auction.cg'),
      //       offset: 100
      //     })
      //   }
      // })
    },
    //添加分账信息
    determine (formName) {
      this.$refs[formName].validate(async valid => {
        if (valid) {
          try {
            var type = this.tyleList.find(
              item => item.value === this.ruleForm2.type
            ).text
            var relationship = this.relaList.find(
              item => item.value === this.ruleForm2.relationship
            ).text
            var list = {
              name:this.ruleForm2.name,
              type: this.ruleForm2.type,
              account: this.ruleForm2.account,
              relationship: this.ruleForm2.relationship,
              proportion: this.ruleForm2.proportion,
              typeDesc: type,
              relationshipDesc: relationship
            }
            var arr = {
              name:this.ruleForm2.name,
              type: type,
              account: this.ruleForm2.account,
              relationship: relationship,
              proportion: this.ruleForm2.proportion
            }
            var num = Number(this.proportion) - Number(this.ruleForm2.proportion)
            if (num < 70) {
              this.errorMsg(this.$t('stores.LedgerSetting.fzblbdxybfz'))
              return
            } else {
              this.proportion = num
            }
            this.divideAccountInfo.push(list)
            this.tableData.push(arr)
            this.$message({
              message: this.$t('zdata.tjcg'),
              type: 'success',
              offset: 102
            })
            this.handleClose()
          } catch (error) {
            this.$message({
              message: this.$t('stores.LedgerSetting.mmbnwk'),
              type: 'error',
              offset: 102
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    submitForm (formName) {
      this.$refs[formName].validate(async valid => {
        if (valid) {
          try {
            // if (this.tableData.length == 0) {
            //   this.$message({
            //     message: '请添加分账信息',
            //     type: 'error',
            //     offset: 120
            //   })
            //   return
            // }
            let { entries } = Object
            var data = {
              api: 'admin.divideAccount.saveDivideAccount',
              mchId: this.$route.query.id,
              // accounts_set:this.ruleForm.accounts_set,
              // subMchId: this.ruleForm.sub_mch_id, //子商户号
              // subAppId: this.ruleForm.sub_app_id, //子商户应用号
              divideAccountInfo: JSON.stringify(this.divideAccountInfo) //分账信息（json数组）
            }
            let formData = new FormData()
            for (let [key, value] of entries(data)) {
              formData.append(key, value)
            }
            saveBill(formData).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('stores.LedgerSetting.bccg'),
                  type: 'success',
                  offset: 102
                })
                this.$router.go(-1)
              }
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }

    // //选择一页多少条
    // handleSizeChange (e) {
    //   this.pageSize = e
    //   this.getProLists().then(() => {
    //     this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
    //     this.current_num =
    //       this.tableData.length === this.pageSize
    //         ? this.dictionaryNum * this.pageSize
    //         : this.total
    //   })
    // },

    // //点击上一页，下一页
    // handleCurrentChange (e) {
    //   this.dictionaryNum = e
    //   this.currpage = (e - 1) * this.pageSize + 1
    //   this.getProLists().then(() => {
    //     this.current_num =
    //       this.tableData.length === this.pageSize
    //         ? e * this.pageSize
    //         : this.total
    //   })
    // },
  }
}
</script>

<style scoped lang="less">
.container {
  width: 100%;
  .dialog-block {
    // 弹框样式
    /deep/.el-dialog {
      width: auto;
      min-width: 580px;
      height: 478px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      .proportion{
        margin-bottom: 0 !important;
      }
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

      .el-dialog__body {
        // padding: 41px 60px 20px 60px !important;
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
          width: 360px;
        }
      }
    }
  }
  /deep/.shaco_btn {
    color: #2890ff !important;
    font-size: 14px !important;
    border: 1px solid #2890ff;
    span {
      color: #2890ff !important;
      font-size: 14px !important;
    }
  }
  /deep/.el-form {
    .header {
      width: 100%;
      height: 60px;
      line-height: 60px;
      border-bottom: 1px solid #e9ecef;
      padding-left: 20px;
      padding-right: 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      span {
        font-weight: 400;
        font-size: 16px;
        color: #414658;
      }
    }

    .select-goods {
      border-radius: 4px;
      width: 100%;
      background-color: #fff;
      margin-bottom: 90px;
      .goods-block {
        margin-top: 20px;
        display: flex;
        flex-direction: column;
        padding: 0 20px 20px 20px;
        .Search {
          display: flex;
          padding: 0;
          align-items: center;
          background: #ffffff;
          border-radius: 4px;
          margin-bottom: 0;
          .Search-condition {
            display: flex;
            align-items: center;
            .query-input {
              display: flex;
              .select-input {
                width: 180px;
                height: 40px;
              }
              .select-brand {
                margin: 0 10px;
              }
              .input-name {
                width: 280px;
                height: 40px;
                margin-right: 10px;
                input {
                  width: 280px;
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

            .jump-list {
              button {
                min-width: 92px !important;
                height: 40px;
              }
              .laiketui-add:before {
                font-size: 14px;
                margin-right: 8px;
              }
              .bgColor:hover {
                opacity: 0.8;
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
              .export {
                position: absolute;
                right: 30px;
              }
            }
          }
        }

        .menu-list {
          height: 410px;
          width: 100%;
          flex: 1;
          background: #ffffff;
          border-radius: 4px;
          .el-table__header {
            thead {
              tr {
                th {
                  height: 50px;
                  text-align: center;
                  font-size: 14px;
                  font-weight: bold;
                  color: #414658;
                }
              }
            }
          }
          .el-table__body {
            tbody {
              tr {
                td {
                  height: 90px;
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
                justify-content: flex-start;
                align-items: center;
                img {
                  width: 60px;
                  height: 60px;
                }
                span {
                  margin-left: 10px;
                  text-align: left;
                }
              }
            }
          }
          .el-table {
            &::before {
              left: 0;
              bottom: 0;
              width: 100%;
              height: 0px;
            }
            .el-table__fixed-right::before {
              height: 0 !important;
            }
          }
        }
      }
    }

    .activity-set {
      border-radius: 4px;
      width: 100%;
      background-color: #fff;
      margin-bottom: 16px;
      .activity-block {
        display: flex;
        // justify-content: center;
        padding: 40px;
        // flex-direction: column;
        .el-form-item {
          margin-bottom: 0 !important;
        }
        .idClass {
          margin-left: 40px;
        }
        .exchange-price {
          .el-form-item__content {
            display: flex;
            .add {
              margin: 0 !important;
            }
          }
        }
        .el-input {
          width: 420px;
          height: 40px;
          input {
            width: 420px;
          }
        }
      }
    }

    .footer-button {
      position: fixed;
      right: 0;
      bottom: 40px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding: 15px 20px;
      border-top: 1px solid #e9ecef;
      background: #ffffff;
      width: 300%;
      z-index: 10;
      button {
        width: 70px;
        height: 40px;
      }
      .bgColor {
        margin-left: 14px;
      }
      .bgColor:hover {
        opacity: 0.8;
      }

      .fontColor {
        color: #6a7076;
        border: 1px solid #d5dbe8;
        margin-left: 14px;
      }
      .fontColor:hover {
        color: #2890ff;
        border: 1px solid #2890ff;
      }
    }
  }
}
</style>
