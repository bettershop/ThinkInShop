<template>
  <div class="container">
    <div class="btn-nav">
      <el-radio-group fill="#2890ff" text-color="#fff" v-model="radio1">
        <el-radio-button :label="$t('permissions.menulist.cdlb')" @click.native.prevent="$router.push('/Platform/permissions/menulist')"></el-radio-button>
        <el-radio-button :label="$t('permissions.menulist.jslb')" @click.native.prevent="$router.push('/Platform/permissions/rolelist')"></el-radio-button>
      </el-radio-group>
    </div>
    <div class="jump-list">
      <el-button class="bgColor laiketui laiketui-add" type="primary"  @click="$router.push('/Platform/permissions/addrole')">{{$t('permissions.rolelist.tjjs')}}</el-button>
    </div>
    <div class="role-list" ref="tableFather">
      <el-table :element-loading-text="$t('DemoPage.tableExamplePage.loading_text')" v-loading="loading" :data="tableData" ref="table" class="el-table" style="width: 100%"
			:height="tableHeight">
        <el-table-column prop="id" :label="$t('permissions.rolelist.xh')" width="120">
        </el-table-column>
        <el-table-column prop="name" :label="$t('permissions.rolelist.js')">
        </el-table-column>
        <el-table-column prop="role_describe" :label="$t('permissions.rolelist.ms')">
        </el-table-column>
        <el-table-column prop="level" :label="$t('permissions.rolelist.bdsh')">
          <template slot-scope="scope">
            <p v-for="(item,index) in scope.row.bindAdminList.slice(0,3)" :key="index">{{ item.name }}</p>
            <p v-if="scope.row.bindAdminList.length >3">......</p>
          </template>
        </el-table-column>
        <el-table-column prop="add_date" :label="$t('permissions.rolelist.tjsj')">
          <template slot-scope="scope">
            <span>{{ scope.row.add_date | dateFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('permissions.rolelist.caozuo')" width="300">
          <template slot-scope="scope">
            <div class="OP-button">
              <div class="OP-button-top">
                <el-button icon="el-icon-view" @click="view(scope.row)">{{$t('permissions.rolelist.see')}}</el-button>
                <el-button icon="el-icon-edit-outline" @click="permissionmodify(scope.row)">{{$t('permissions.rolelist.qxxg')}}</el-button>   
              </div>
              <div class="OP-button-bottom">
                <el-button class="laiketui laiketui-bangding" @click="dialogShow(scope.row)">{{$t('permissions.rolelist.bdsh')}}</el-button>
                <el-button icon="el-icon-delete" @click="Delete(scope.row)">{{$t('permissions.rolelist.shanchu')}}</el-button>
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
    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('permissions.rolelist.bdsh')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <div class="Search">
          <el-input v-model="merchants" size="medium" @keyup.enter.native="demand" :placeholder="$t('permissions.rolelist.qsrsh')"></el-input>
          <el-button class="fontColor" @click="reset">{{$t('DemoPage.tableExamplePage.reset')}}</el-button>
          <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{$t('DemoPage.tableExamplePage.demand')}}</el-button>
	      </div>
        <div class="merchant-list" ref="tableBindFather">
          <el-table v-loading="loading" :data="merchantLists" ref="table2" class="merchant-table" style="width: 100%"
          :height="tableBindHeight">
            <el-table-column prop="name" :label="$t('permissions.rolelist.shmc')">
              <template slot-scope="scope">
                <el-checkbox :style="{'--fill-color':'#2890ff'}" @change="change3(scope.row.id)"></el-checkbox>
                <span>{{ scope.row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="tel" :label="$t('permissions.rolelist.sjh')">
            </el-table-column>
		      </el-table>
        </div>
        <div class="bind-merchant">
          <h3>{{$t('permissions.rolelist.ybdsh')}}</h3>
          <div class="merchant-block">
            <div class="item" v-for="(item,index) in merchantList" :key="index">
              <el-checkbox @change="change4(item.id)" checked :style="{'--fill-color':'#2890ff'}"></el-checkbox>
              <span>{{ item.name }}</span>
            </div>
          </div>
        </div>
        <div slot="footer" class="form-footer">
            <el-button class="fontColor" @click="cancel">{{$t('permissions.rolelist.cancel')}}</el-button>
            <el-button class="bgColor" type="primary" @click="determine">{{$t('permissions.rolelist.ok')}}</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getRoleListInfo, delUserRoleMenu, getBindListInfo, bindRole, verificationBind } from '@/api/Platform/permissions'
import { mixinstest } from '@/mixins/index'
export default {
    name: 'rolelist',
    mixins: [mixinstest],
    data() {
      return {
        radio1:this.$t('permissions.menulist.jslb'),

        tableData: [],
        loading: true,

        // 弹框数据
        dialogVisible: false,
        merchants: '',
        merchantLists: [],
        merchantList: [],
        id: null,
        idList: [],
        // table高度
        tableHeight: null,
        tableBindHeight: null,

        bindId: [],
      }
    },

    created() {
      this.getRoleListInfos()
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
      async getRoleListInfos() {
        const res = await getRoleListInfo({
          api: 'saas.role.getRoleListInfo',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
        })
        this.total = res.data.data.total
        this.tableData = res.data.data.list
        this.loading = false
        if (res.data.data.total < 10) {
          this.current_num = this.total;
        }
        let bindList = []
        for(var i = 0; i<this.tableData.length; i++) {
          bindList.push(...this.tableData[i].bindAdminList)
        }
      },

      // 获取已绑定商户
      async getBindListInfoss() {
        const res = await getBindListInfo({
          api: 'saas.role.getBindListInfo',
          roleId: this.id,
          isBind: 0
        })
        console.log(res);
        this.merchantLists = res.data.data.bindAdminList
      },

      // 获取未绑定商户
      async getBindListInfos() {
        const res = await getBindListInfo({
          api: 'saas.role.getBindListInfo',
          roleId: this.id,
          isBind: 1
        })
        console.log(res);
        this.merchantList = res.data.data.bindAdminList
      },

      view(value) {
        this.$router.push({ name: 'viewrole', params: value })
      },

      permissionmodify(value) {
        this.$router.push({ name: 'rolepermission', params: value })
      },

      // binding(value) {
      //   console.log(value);
      // },

      Delete(value) {
        this.$confirm(this.$t('permissions.rolelist.scqr'), this.$t('permissions.rolelist.ts'), {
          confirmButtonText: this.$t('permissions.rolelist.ok'),
          cancelButtonText: this.$t('permissions.rolelist.cancel'),
          type: 'warning'
        }).then(() => {
          delUserRoleMenu({
            api: 'saas.role.delUserRoleMenu',
            id: value.id
          }).then(res => {
            if(res.data.code == '200') {
              console.log(res);
              this.getRoleListInfos()
              this.$message({
                type: 'success',
                message: this.$t('zdata.sccg'),
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

      //选择一页多少条
      handleSizeChange(e){
        this.loading = true
        console.log(e);
        // this.current_num = e
        this.pageSize = e
        this.getRoleListInfos().then(() => {
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
        this.getRoleListInfos().then(() => {
          this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
          this.loading = false
        })
        
      },

      // 弹框方法
      dialogShow(value) {
        this.dialogVisible = true
        console.log(value);
        this.id = value.id
        this.getBindListInfoss()
        this.getBindListInfos()
        this.$nextTick(function() {
          this.tableBindHeight = this.$refs.tableBindFather.clientHeight
          this.$refs.table2.doLayout();
        })
        
      },

      change(value) {
        this.idList.push(value)
      },

      change3(value) {
        console.log(value);
        if(this.idList.some(item => {
          return item == value
        })) {
          for(var i = 0; i < this.idList.length; i++) {
            if(this.idList[i] == value) {
              this.idList.splice(i, 1);
              this.bindId.splice(i, 1);
              break;
            }
          }
        } else {
          this.idList.push(value)
          this.bindId.push(value)
        }
      },

      change4(value) {
        if(this.idList.some(item => {
          return item == value
        })) {
          for(var i = 0; i < this.idList.length; i++) {
            if(this.idList[i] == value) {
              this.idList.splice(i, 1);
              break;
            }
          }
        } else {
          this.idList.push(value)
        }
      },

      cancel() {
        this.merchantLists = [],
        this.merchantList = [],
        this.idList = []
        this.dialogVisible = false
      },

      determine() {
        if(this.bindId.length !== 0) {
          verificationBind({
            api: 'saas.role.verificationBind',
            adminIds: this.bindId.join(',')
          }).then(res => {
            console.log(res);
            if(res.data.data) {
              this.$confirm(this.$t('permissions.rolelist.bdqr'), this.$t('permissions.rolelist.ts'), {
                confirmButtonText: this.$t('permissions.rolelist.ok'),
                cancelButtonText: this.$t('permissions.rolelist.cancel'),
                type: 'warning'
              })
              .then(() => {
                bindRole({
                  api: 'saas.role.bindRole',
                  roleId: this.id,
                  adminIds: this.idList.join()
                }).then(res => {
                  if(res.data.code == '200') {
                    console.log(res);
                    this.merchantLists = [],
                    this.merchantList = [],
                    this.idList = []
                    this.$message({
                      type: 'success',
                      message: this.$t('zdata.bdcg'),
                      offset: 120
                    })
                    this.dialogVisible = false
                    this.getRoleListInfos()
                  }
                })
              })
              .catch(() => {
                  // this.$message({
                  //   type: 'info',
                  //   message: '已取消'
                  // });          
              })
            } else {
              bindRole({
                api: 'saas.role.bindRole',
                roleId: this.id,
                adminIds: this.idList.join()
              }).then(res => {
                if(res.data.code == '200') {
                  console.log(res);
                  this.merchantLists = [],
                  this.merchantList = [],
                  this.idList = []
                  this.$message({
                    type: 'success',
                    message: this.$t('zdata.bdcg'),
                    offset: 120
                  })
                  this.dialogVisible = false
                  this.getRoleListInfos()
                }
              })
            }
          })
        } else {
          bindRole({
            api: 'saas.role.bindRole',
            roleId: this.id,
            adminIds: this.idList.join()
          }).then(res => {
            if(res.data.code == '200') {
              console.log(res);
              this.merchantLists = [],
              this.merchantList = [],
              this.idList = []
              this.$message({
                type: 'success',
                message: '成功'
              })
              this.dialogVisible = false
              this.getRoleListInfos()
            }
          })
        }
      },

      reset() {
        this.merchants = ''
      },

      demand() {
        getBindListInfo({
          api: 'saas.role.getBindListInfo',
          name: this.merchants,
          roleId: this.id,
          isBind: 0
        }).then(res => {
          console.log(res);
          this.merchantLists = res.data.data.bindAdminList
        })
      },
      
      handleClose(done) {
        this.dialogVisible = false
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
      height: 42px;
      font-size: 16px;
      border: none;
    }
  }
  /deep/.jump-list {
    margin: 16px 0;
    .laiketui-add:before {
      font-size: 14px;
      margin-right: 8px;
    }
  }

  .role-list {
    // height: 623px;
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
        // display: flex;
        // flex-direction: column;
        // align-items: center;
        .OP-button-top{
          margin-bottom: 8px;
          display: flex;
          justify-content: center;
          button {
            &:not(:first-child) {
              margin-left: 8px !important;
            }
          }
        }
        .OP-button-bottom {
          display: flex;
          justify-content: center;
          .laiketui-add:before {
            margin-right: 5px;
            font-size: 10px;
          }
          .laiketui-bangding:before {
            margin-right: 5px;
          }
        }
      }
    }
  }

  .dialog-block {
    // 弹框样式
    /deep/.el-dialog {
      width: 920px;
      // height: 720px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%,-50%);
      margin: 0 !important;
      .el-dialog__header {
        width: 100%;
        height: 62px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 1px solid #E9ECEF;
        box-sizing: border-box;
        margin: 0;
        padding: 0 0 0 19px;
        span {
          color: #414658;
          font-size: 16px;
        }
        .el-dialog__headerbtn {
          font-size: 18px;
          top: 0 !important;
        }
        .el-dialog__title {
          font-weight: normal;
        }
        
      }

      .el-dialog__body {
        width: 100%;
        // height: 587px;
        padding: 40px 20px;
        border-bottom: 1px solid #E9ECEF;
        .Search{
          display: flex;
          align-items: center;
          justify-content: start;
          margin-bottom: 20px;
          padding-left: 0;
          padding: 0;
          .el-input {
            width: 280px;
            height: 40px;
            margin-right: 10px;
            .el-input__inner {
              width: 280px;
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
          .bgColor {
            background-color: #2890ff;
          }
          .bgColor:hover {
            background-color: #70aff3;
          }
          .fontColor {
            color: #6a7076;
            border: 1px solid #d5dbe8;
            background-color: #fff;
          }
          .fontColor:hover {
            color: #2890FF;
            border: 1px solid #2890FF;
            background-color: #fff;
          }
          
        }

        .merchant-list {
          width: 880px;
          height: 292px;
          // border: 1px solid #E9ECEF;
          border-bottom: none;
          .el-table {
            border: 1px solid #E9ECEF;
          }
          .el-table__header {
            thead {
              height: 50px;
              border-bottom: 1px solid #E9ECEF;
              tr {
                th {
                  height: 50px;
                  padding-left: 40px;
                }
              }
            }
          }
          .el-table__body {
            td {
              height: 60px !important;
            }
            .cell {
              padding-left: 50px;
              label {
                margin-left: 0;
              }
            }
          }

          .el-checkbox {
            margin: 0 8px 0 21px;
          }

          .cell {
            color: #414658;
          }
          
        }

        .bind-merchant {
          margin-top: 30px;
          color: #414658;
          h3 {
            font-size: 14px;
          }
          .merchant-block {
            margin-top: 15px;
            width: 880px;
            height: 98px;
            background: #F4F7F9;
            border-radius: 4px;
            padding: 0 22px;
            display: flex;
            flex-wrap: wrap;
            overflow: hidden;
            overflow-y: auto;
            .item {
              margin: 20px 128px 0 0;
              .el-checkbox {
                margin-right: 10px;
              }
            }
          }
        }

        .el-checkbox__inner {
          border: 1px solid #b2bcd1;
        }
        .el-checkbox__label {
          color: var(--fill-color);
        }

        .el-checkbox__input.is-checked .el-checkbox__inner,
        .el-checkbox__input.is-indeterminate .el-checkbox__inner {
          background-color: var(--fill-color);
          border-color: var(--fill-color)
        }

        .el-checkbox__input.is-focus .el-checkbox__inner,
          .el-checkbox__inner:hover {
            border-color: var(--fill-color);
          }
      }

      .el-dialog__footer {
        padding: 0 20px 0 0;
        height: 72px;
        line-height: 72px;
        .form-footer {
          .bgColor {
            background-color: #2890ff;
          }
          .bgColor:hover {
            background-color: #70aff3;
          }
          .fontColor {
            color: #6a7076;
            border: 1px solid #d5dbe8;
          }
          .fontColor:hover {
            background-color: #fff;
            color: #2890FF;
            border: 1px solid #2890FF;
          }

          .el-button+.el-button {
            margin-left: 14px;
          }
        }
      }
    }
  }
}
</style>