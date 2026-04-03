<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button
          :label="$t('stores.dp')"
          @click.native.prevent="$router.push('/plug_ins/stores/store')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/store')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.dpfl')"
          @click.native.prevent="$router.push('/plug_ins/stores/storeFl')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/storeFl')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.dpsh')"
          @click.native.prevent="$router.push('/plug_ins/stores/auditList')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/auditList')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.bzjjl')"
          @click.native.prevent="$router.push('/plug_ins/stores/bondMoney')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/bondMoney.vue')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.bzjsh')"
          @click.native.prevent="$router.push('/plug_ins/stores/bondExamine')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/bondExamine.vue')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.spsh')"
          @click.native.prevent="$router.push('/plug_ins/stores/goodsAudit')"
          v-if="handleTabLimits(routerList, '/plug_ins/stores/goodsAudit.vue')"
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.txsh')"
          @click.native.prevent="
            $router.push('/plug_ins/stores/withdrawalAudit')
          "
          v-if="
            handleTabLimits(routerList, '/plug_ins/stores/withdrawalAudit.vue')
          "
        ></el-radio-button>
        <el-radio-button
          :label="$t('stores.txjl')"
          @click.native.prevent="
            $router.push('/plug_ins/stores/withdrawalRecord')
          "
          v-if="
            handleTabLimits(routerList, '/plug_ins/stores/withdrawalRecord.vue')
          "
        ></el-radio-button>
        <!-- <el-radio-button :label="$t('stores.dpsz')" @click.native.prevent="toset()"></el-radio-button> -->
      </el-radio-group>
    </div>

    <div class="Search">
      <div class="Search-condition">
        <div class="query-input">
          <el-input
            v-model="inputInfo.keyName"
            size="medium"
            @keyup.enter.native="demand"
            class="Search-input"
            :placeholder="$t('stores.bondExamine.qsrdp')"
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

    <div class="menu-list" ref="tableFather">
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
        <el-table-column
          fixed="left"
          prop="id"
          :label="$t('stores.auditList.dpid')"
        >
        </el-table-column>
        <el-table-column
          prop="store_info"
          :label="$t('stores.auditList.dpxx')"
          width="410"
        >
          <template slot-scope="scope">
            <div class="store-info">
              <div class="head-img">
                <img :src="scope.row.headimgurl" alt="" />
              </div>
              <div class="store_info">
                <div class="item name">
                  {{ $t('stores.auditList.dpmc') }}：{{ scope.row.name }}
                </div>
                <div class="item grey center">
                  {{ $t('stores.auditList.yhid') }}：{{ scope.row.user_id }}
                </div>
                <div class="item grey">
                  {{ $t('stores.auditList.ssyh') }}：{{ scope.row.userName }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="realname" :label="$t('stores.auditList.lxr')">
        </el-table-column>
        <el-table-column prop="tel" :label="$t('stores.auditList.lxdh')">
        </el-table-column>
        <el-table-column
          prop="promise_amt"
          :label="$t('stores.bondExamine.bzje')"
        >
        </el-table-column>
        <el-table-column prop="is_pass" :label="$t('stores.bondExamine.zt')">
          <template slot-scope="scope">
            <div class="store-info">
              <!-- {{scope.row.status  == 1 ? "交纳":"退还"}} -->
              <span v-if="scope.row.status == 1" style="color: #13c366">{{
                $t('stores.bondExamine.shtg')
              }}</span>
              <span v-if="scope.row.status == 2" style="color: #ff453d">{{
                $t('stores.bondExamine.shjj')
              }}</span>
              <span v-if="scope.row.status == 3">{{
                $t('stores.bondExamine.shz')
              }}</span>
            </div>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="mobile" :label="是否已退还">
            <template slot-scope="scope">
              <div class="store-info">
                {{scope.row.is_return_pay  == 1 ? "是":"否"}}
              </div>
            </template>
          </el-table-column> -->
        <el-table-column
          prop="add_date"
          :label="$t('stores.bondExamine.sqsj')"
          width="200"
        >
          <template slot-scope="scope">
            <span v-if="scope.row.is_return_pay != 1">{{
              scope.row.add_date | dateFormat
            }}</span>
            <span v-else>{{ scope.row.update_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('stores.cz')"
          width="200"
          class="hhh"
        >
          <template slot-scope="scope">
            <div class="OP-button">
              <span class="OP-button-top">
                <el-button
                  v-if="scope.row.status == 2"
                  icon="el-icon-view"
                  @click="View(scope.row)"
                  >{{ $t('stores.bondExamine.ckyy') }}</el-button
                >
                <el-button
                  v-if="scope.row.status == 3"
                  icon="el-icon-circle-check"
                  @click="pass(scope.row)"
                  >{{ $t('stores.bondExamine.tg') }}</el-button
                >
                <el-button
                  v-if="scope.row.status == 3"
                  icon="el-icon-circle-close"
                  @click="nopass(scope.row)"
                  >{{ $t('stores.bondExamine.jj') }}</el-button
                >
                <el-button
                  v-if="scope.row.status !== 3"
                  icon="el-icon-delete"
                  @click="Delete(scope.row)"
                  >{{ $t('stores.shanchu') }}</el-button
                >
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pageBox" ref="pageBox">
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
            {{ $t('DemoPage.tableExamplePage.on_show') }}
            {{ currpage }}-{{ current_num
          }}
            <!-- {{ currpage }}-{{
              current_num
            }} -->
            {{ $t('DemoPage.tableExamplePage.twig') }}{{ total
            }}{{ $t('DemoPage.tableExamplePage.twig_notes') }}
          </div>
        </el-pagination>
      </div>
    </div>
    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('stores.bondExamine.txyy')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose2"
      >
        <el-form
          :model="ruleForm"
          ref="ruleForm"
          :rules="rules"
          label-width="100px"
          class="demo-ruleForm"
        >
          <div class="task-container">
            <el-form-item
              class="integral"
              :label="$t('stores.bondExamine.jjly')"
              prop="reason"
            >
              <el-input
                type="textarea"
                v-model="ruleForm.reason"
                :placeholder="$t('stores.bondExamine.qsrjjly')"
              ></el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button class="bgColor" @click="handleClose2()">{{
                $t('stores.ccel')
              }}</el-button>
              <el-button
                class="bdColor"
                type="primary"
                @click="submitForm2('ruleForm')"
                >{{ $t('stores.okk') }}</el-button
              >
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getWithdrawalInfo } from '@/api/plug_ins/stores'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import request from '@/api/https'
export default {
  name: 'bondMoney',
  mixins: [mixinstest],
  data () {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('stores.bzjsh'),
      id: '',
      inputInfo: {
        keyName: null
      },

      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      dialogVisible2: false,
      ruleForm: {
        reason: ''
      },
      rules: {
        reason: [
          {
            required: true,
            message: this.$t('stores.bondExamine.qsrjjly'),
            trigger: 'blur'
          }
        ]
      }
    }
  },
  computed: {
    showItem () {
      let page = JSON.parse(JSON.stringify(this.dictionaryNum))
      let size = JSON.parse(JSON.stringify(this.pageSize))
      let showItem1 = (page - 1) * size + size
      if (showItem1 > this.total) {
        showItem1 = this.total
      }
      let showItem = (page - 1) * size + 1 + '-' + showItem1
      return showItem
    }
  },
  created () {
    if(this.$route.query.billId){
      this.inputInfo.keyName = this.$route.query.billId
    }
    this.getWithdrawalInfos()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    toset () {
      if (getStorage('laike_admin_userInfo').mchId !== 0) {
        this.$router.push('/plug_ins/stores/storeSet')
      } else {
        this.$message({
          type: 'error',
          message: '请添加店铺!',
          offset: 102
        })
        this.$router.push('/mall/fastBoot/index')
      }
    },
    View (e) {
      this.$confirm(e.refused_why, this.$t('stores.bondExamine.yy'), {
        confirmButtonText: this.$t('stores.okk'),
        cancelButtonText: this.$t('stores.ccel')
        // type: 'warning'
      })
        .then(() => {})
        .catch(() => {})
    },
    pass (value) {
      console.log(value)
      this.$confirm(this.$t('stores.bondExamine.qrtg'), this.$t('stores.ts'), {
        confirmButtonText: this.$t('stores.okk'),
        cancelButtonText: this.$t('stores.ccel'),
        type: 'warning'
      })
        .then(() => {
          request({
            method: 'post',
            params: {
              api: 'mch.Admin.Mch.PassOrRefused',
              id: value.aid,
              isPass: '1'
            }
          }).then(res => {
            console.log(res)
            this.getWithdrawalInfos()
          })
        })
        .catch(() => {})
    },
    nopass (value) {
      console.log(value)
      this.dialogVisible2 = true
      this.id = value.aid
      this.ruleForm.reason = ''
    },
    Delete (value) {
      console.log(value)
      this.$confirm(this.$t('stores.bondExamine.qrsc'), this.$t('stores.ts'), {
        confirmButtonText: this.$t('stores.okk'),
        cancelButtonText: this.$t('stores.ccel'),
        type: 'warning'
      })
        .then(() => {
          request({
            method: 'post',
            params: {
              api: 'mch.Admin.Mch.DeletePromisePrice',
              id: value.aid
            }
          }).then(res => {
            if (res.data.code == '200') {
              this.getWithdrawalInfos()
              this.$message({
                type: 'success',
                message: this.$t('stores.sccg'),
                offset: 102
              })
            }
          })
        })
        .catch(() => {})
    },
    handleClose2 (done) {
      this.dialogVisible2 = false
      this.id = null
      this.$refs['ruleForm'].clearValidate()
    },

    submitForm2 (formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm)
        if (valid) {
          try {
            if (this.ruleForm.reason.length > 100) {
              this.$message({
                type: 'error',
                message: this.$t('stores.bondExamine.jjlyw'),
                offset: 102
              })
              return
            }
            request({
              method: 'post',
              params: {
                api: 'mch.Admin.Mch.PassOrRefused',
                id: this.id,
                isPass: '2',
                refusedWhy: this.ruleForm.reason
              }
            }).then(res => {
              console.log(res)
              this.dialogVisible2 = false
              this.getWithdrawalInfos()
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
    },
    // 获取table高度
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    async getWithdrawalInfos () {
      const res = await getWithdrawalInfo({
        api: 'mch.Admin.Mch.SelectPromisePrice',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        title: this.inputInfo.keyName
      })
      // this.current_num = 10
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      this.sizeMeth()
      console.log(res)
    },
    sizeMeth(){
      this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
      this.current_num =
        this.tableData.length === this.pageSize
          ? this.dictionaryNum * this.pageSize
          : this.total
    },
    // 重置
    reset () {
      this.inputInfo.keyName = null
    },

    // 查询
    demand () {
      // this.currpage = 1
      // this.current_num = 10
      this.loading = true
      this.dictionaryNum = 1
      this.getWithdrawalInfos().then(() => {
        this.loading = false
      })
    },

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.getWithdrawalInfos().then(() => {
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
      this.getWithdrawalInfos().then(() => {
        this.currpage = (e - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    }
  }
}
</script>

<style scoped lang="less">
@import '../../../webManage/css/plug_ins/stores/bondExamine.less';
</style>
