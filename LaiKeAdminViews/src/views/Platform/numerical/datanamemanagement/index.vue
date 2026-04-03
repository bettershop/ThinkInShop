<template>
  <div class="container">
    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="dataName"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('Platform.datanamemanagement.qsrsjmc')"
          ></el-input>
        </div>
        <div class="btn-list">
          <el-button class="fontColor" @click="reset">{{
            $t('DemoPage.tableExamplePage.reset')
          }}</el-button>
          <el-button
            class="bgColor"
            type="primary"
            @click="demand"
            v-enter="demand"
            >{{ $t('DemoPage.tableExamplePage.demand') }}</el-button
          >
        </div>
      </div>
    </div>
    <div class="jump-list">
      <el-button
        @click="dialogShow"
        class="bgColor laiketui laiketui-add"
        type="primary"
        >{{ $t('Platform.datanamemanagement.tjsjmc') }}</el-button
      >
    </div>
    <div class="dictionary-list" ref="tableFather">
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
            <img src="../../../../assets/imgs/empty.png" alt="" />
            <p style="color: #414658">{{ $t('zdata.zwsj') }}</p>
          </div>
        </template>
        <el-table-column
          prop="name"
          :label="$t('Platform.datanamemanagement.sjmc')"
        >
        </el-table-column>
        <el-table-column :label="$t('Platform.datanamemanagement.sfsx')">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.status"
              @change="switchs(scope.row)"
              :active-value="1"
              :inactive-value="0"
              active-color="#00ce6d"
              inactive-color="#d4dbe8"
            >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column
          prop="admin_name"
          :label="$t('Platform.datanamemanagement.tjr')"
        >
        </el-table-column>
        <el-table-column
          prop="add_date"
          :label="$t('Platform.datanamemanagement.tjsj')"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          prop="operation"
          :label="$t('Platform.datanamemanagement.cz')"
          width="200"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button
                  icon="el-icon-edit-outline"
                  @click="Edit(scope.row)"
                  >{{ $t('Platform.datanamemanagement.bianji') }}</el-button
                >
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">{{
                  $t('Platform.datanamemanagement.shanchu')
                }}</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox" v-if="showPagebox">
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
      </div>
    </div>

    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      :before-close="handleClose"
    >
      <el-form
        :model="ruleForm"
        :rules="rules"
        ref="ruleForm"
        label-width="100px"
        class="demo-ruleForm"
      >
        <el-form-item
          :label="$t('Platform.datanamemanagement.sjmc')"
          prop="dataName"
        >
          <el-input v-model="ruleForm.dataName"></el-input>
        </el-form-item>
        <el-form-item
          :label="$t('Platform.datanamemanagement.sfsx')"
          prop="resource"
        >
          <el-radio-group v-model="ruleForm.status">
            <el-radio v-model="radio" label="1">{{
              $t('Platform.datanamemanagement.yes')
            }}</el-radio>
            <el-radio v-model="radio" label="0">{{
              $t('Platform.datanamemanagement.no')
            }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="fontColor" @click="handleClose">{{
              $t('Platform.datanamemanagement.cancel')
            }}</el-button>
            <el-button
              class="bgColor"
              type="primary"
              @click="determine('ruleForm')"
              >{{ $t('Platform.datanamemanagement.ok') }}</el-button
            >
          </el-form-item>
        </div>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import {
  dataName,
  addDictionaryInfo,
  delDictionary,
  switchDictionary
} from '@/api/Platform/numerical'
import { mixinstest } from '@/mixins/index'

export default {
  name: 'datanamemanagement',
  mixins: [mixinstest],
  data () {
    return {
      loading: true,
      dataName: '',
      tableData: [],
      tableHeight: null, //表格高度

      // 弹框数据
      dialogVisible: false,
      ruleForm: {
        dataName: '',
        status: 1
      },
      radio: this.$t('Platform.datanamemanagement.yes'),
      rules: {
        dataName: [
          {
            required: true,
            message: this.$t('Platform.datanamemanagement.qtxsjmc'),
            trigger: 'blur'
          }
        ],
        statues: [
          {
            required: true,
            message: this.$t('Platform.datanamemanagement.yes'),
            trigger: 'change'
          }
        ]
      },

      // table高度
      tableHeight: null,

      title: this.$t('Platform.datanamemanagement.tjsjmc'),
      id: ''
    }
  },

  created () {
    this.getDataName()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    async getDataName () {
      const res = await dataName({
        api: 'saas.dic.getDictionaryCatalogInfo',
        storeType: 8,
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        name: this.dataName
      })
      console.log(res)
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
    },

    reset () {
      this.dataName = ''
    },

    demand () {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getDataName().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.getDataName().then(() => {
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
      this.currpage = e
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.getDataName().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },

    // 是否生效开关
    switchs (value) {
      console.log(value)
      switchDictionary({
        api: 'saas.dic.switchDictionary',
        id: value.id
      }).then(res => {
        this.getDataName()
        console.log(res)
        if (value.status === 1) {
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

    // 编辑字典目录
    Edit (value) {
      this.title = this.$t('Platform.datanamemanagement.bjsjmc')
      this.dialogVisible = true
      console.log(value)
      this.ruleForm.dataName = value.name
      this.ruleForm.status = `${value.status}`
      this.id = value.id
    },

    // 删除字典目录
    Delete (value) {
      console.log(value)
      this.$confirm(
        this.$t('Platform.datanamemanagement.qrsc'),
        this.$t('Platform.datanamemanagement.ts'),
        {
          confirmButtonText: this.$t('Platform.datanamemanagement.ok'),
          cancelButtonText: this.$t('Platform.datanamemanagement.cancel'),
          type: 'warning'
        }
      )
        .then(() => {
          delDictionary({
            api: 'saas.dic.delDictionary',
            idList: value.id
          }).then(res => {
            if (res.data.code == '200') {
              console.log(res)
              this.$message({
                type: 'success',
                message: this.$t('zdata.sccg'),
                offset: 102
              })
              this.getDataName()
            }
          })
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除'
          // });
        })
    },

    // 弹框方法
    dialogShow () {
      ;(this.ruleForm.dataName = ''),
        (this.ruleForm.status = '1'),
        (this.dialogVisible = true)
      this.title = this.$t('Platform.datanamemanagement.tjsjmc')
    },

    handleClose (done) {
      this.dialogVisible = false
      this.$refs['ruleForm'].clearValidate()
    },

    determine (formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm)
        if (valid) {
          try {
            addDictionaryInfo({
              api: 'saas.dic.addDictionaryInfo',
              name: this.ruleForm.dataName,
              isOpen: parseInt(this.ruleForm.status),
              id:
                this.title == this.$t('Platform.datanamemanagement.bjsjmc')
                  ? this.id
                  : ''
            }).then(res => {
              if (res.data.code == '200') {
                this.getDataName()
                console.log(res)
                this.$message({
                  message:
                    this.title == this.$t('Platform.datanamemanagement.bjsjmc')
                      ? this.$t('zdata.bjcg')
                      : this.$t('zdata.tjcg'),
                  type: 'success',
                  offset: 102
                })
                this.dialogVisible = false
              }
            })
          } catch (error) {
            this.$message({
              message: this.$t('Platform.datanamemanagement.sjmcwk'),
              type: 'error',
              offset: 102
            })
          }
        } else {
          // this.$message({
          //   message: '数据名称不能为空',
          //   type: 'error',
          //   offset: 100
          // })
          // return false;
        }
      })
    }
  }
}
</script>

<style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
  /deep/.Search {
    .Search-condition {
      .query-input {
        .Search-input {
          margin-right: 10px;
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

  .dictionary-list {
    // height: 605px;
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
    }

    /deep/.el-table {
      .OP-button {
        .OP-button-top {
          margin-bottom: 8px;
        }
      }
    }
  }

  // 弹框样式
  /deep/.el-dialog {
    width: 580px;
    height: 310px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    margin: 0 !important;
    .el-dialog__header {
      width: 100%;
      height: 60px;
      line-height: 60px;
      font-size: 16px;
      margin-left: 19px;
      font-weight: bold;
      border-bottom: 1px solid #e9ecef;
      box-sizing: border-box;
      margin: 0;
      padding: 0 0 0 19px;
      display: flex;
      align-items: center;
      .el-dialog__headerbtn {
        top: 0;
      }
      span {
        font-weight: normal;
      }
    }

    .el-dialog__body {
      padding: 41px 60px 0px 60px !important;

      .form-footer {
        width: 100%;
        height: 72px;
        position: absolute;
        bottom: 0;
        right: 0;
        display: flex;
        justify-content: flex-end;
        padding-right: 20px;
        border-top: 1px solid #e9ecef;
        .el-form-item {
          padding: 0 !important;
          height: 100%;
          .el-form-item__content {
            height: 100%;
            line-height: 72px;
            margin: 0 !important;
          }
        }
        .bgColor {
          margin-left: 14px;
        }
      }
    }
  }
}
</style>
