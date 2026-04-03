<template>
    <div class="container">
      <div class="Search">
        <div class="Search-condition">
          <div class="query-input">
            <el-input
              v-model="inputInfo.userName"
              size="medium"
              class="Search-input"
              placeholder="请输入用户名称"
            ></el-input>
            <el-select
              class="select-input"
              v-model="inputInfo.oType"
              placeholder="请选择类型"
            >
              <el-option
                v-for="item in TypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>
            <div class="select-date">
              <el-date-picker
                v-model="inputInfo.date"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd HH:mm:ss"
                :editable="false"
              >
              </el-date-picker>
            </div>
          </div>
          <div class="btn-list">
            <el-button class="fontColor" @click="reset">{{
              $t('DemoPage.tableExamplePage.reset')
            }}</el-button>
            <el-button class="bgColor" type="primary" @click="demand" v-enter="demand">{{
              $t('DemoPage.tableExamplePage.demand')
            }}</el-button>
            <span v-for="(item, index) in button_list" :key="index">
              <el-button
                v-if="item.title == '导出'"
                class="bgColor export"
                type="primary"
                @click="dialogShow"
                >导出</el-button
              >
            </span>
          </div>
        </div>
      </div>
  
      <div class="menu-list" ref="tableFather">
        <el-table
          element-loading-text="拼命加载中..."
          v-loading="loading"
          :data="tableData"
          ref="table"
          class="el-table"
          style="width: 100%"
          :height="tableHeight"
        >
          <el-table-column prop="user_id" label="用户ID"> </el-table-column>
          <el-table-column prop="user_name" label="用户名称"> </el-table-column>
          <el-table-column prop="money" label="充值总金额">
            <template slot-scope="scope">
              <span v-if="scope.row.money"
                >￥{{ scope.row.money.toFixed(2) }}</span
              >
            </template>
          </el-table-column>
          <el-table-column prop="source" label="来源">
            <template slot-scope="scope">
              <span v-if="scope.row.source == 1">小程序</span>
              <span v-if="scope.row.source == 2">app</span>
              <span v-if="scope.row.source == 3">支付宝小程序</span>
              <span v-if="scope.row.source == 4">头条小程序</span>
              <span v-if="scope.row.source == 5">百度小程序</span>
              <span v-if="scope.row.source == 6">PC端</span>
              <span v-if="scope.row.source == 7">H5</span>
            </template>
          </el-table-column>
          <el-table-column prop="mobile" label="手机号码"> </el-table-column>
          <el-table-column prop="type" label="类型">
            <template slot-scope="scope">
              <span v-if="scope.row.type == 1">用户充值</span>
              <span v-if="scope.row.type == 11">系统扣款</span>
              <span v-if="scope.row.type == 14">系统充值</span>
            </template>
          </el-table-column>
          <el-table-column prop="add_date" label="充值时间"> </el-table-column>
        </el-table>
        <div class="pageBox" ref="pageBox" v-if="showPagebox">
          <div class="pageLeftText">显示</div>
          <el-pagination
            layout="sizes, slot, prev, pager, next"
            prev-text="上一页"
            next-text="下一页"
            @size-change="handleSizeChange"
            :page-sizes="pagesizes"
            :current-page="pagination.page"
            @current-change="handleCurrentChange"
            :total="total"
          >
            <div class="pageRightText">
              当前显示{{ currpage }}-{{ current_num }}条，共 {{ total }} 条记录
            </div>
          </el-pagination>
        </div>
      </div>
      <div class="dialog-export">
        <!-- 弹框组件 -->
        <el-dialog
          title="导出数据"
          :visible.sync="dialogVisible"
          :before-close="handleClose"
        >
          <div class="item" @click="exportPage">
            <i class="el-icon-document"></i>
            <span>导出本页</span>
          </div>
          <div class="item item-center" @click="exportAll">
            <i class="el-icon-document-copy"></i>
            <span>导出全部</span>
          </div>
          <div class="item" @click="exportQuery">
            <i class="el-icon-document"></i>
            <span>导出查询</span>
          </div>
        </el-dialog>
      </div>
    </div>
  </template>
  
  <script>
 import { topList } from '@/api/finance/withdrawalManage'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'

export default {
    name: 'rechargeList',
    mixins: [mixinstest],
    data() {
        return {
            exportType:'',//判断是否为导出
            TypeList: [
                {
                    value: '1',
                    label: '用户充值'
                },
                {
                    value: '11',
                    label: '系统扣款'
                },
                {
                    value: '14',
                    label: '系统充值'
                },
            ],// 订单状态
            inputInfo: {
                userName: '',
                oType: '',
                date: ''
            },

            tableData: [],
            loading: true,
            type: '',
            // table高度
            tableHeight: null,
            button_list:[],
            // 导出弹框数据
            dialogVisible: false,
        }
    },

    created() {
        this.getButtons()
        this.axios()
    },

    mounted() {
        this.$nextTick(function() {
            this.getHeight()
        })
        window.addEventListener('resize',this.getHeight(),false)
    },

    methods: {
        //初始化充值列表数据
        async axios(){
            const res = await topList({
                api: 'admin.user.getupInfo',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                userName:this.inputInfo.userName,
                oType:this.inputInfo.oType,
                startDate: this.inputInfo.date?.[0]??'',
                endDate: this.inputInfo.date?.[1]??'',
                // exportType:this.exportType??'',
            })
            console.log('res:',res);
            
            if(res.data.code == 200){
                this.loading = false
                this.tableData = res.data.data.list
                this.total = res.data.data.total
                console.log('tableData:',this.tableData);
            }
            
        },
        // 获取table高度
        getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
            console.log(this.$refs.tableFather.clientHeight);
		},
        //获取按纽权限
        async getButtons() {
            let route=getStorage('route')
            route.forEach(item => {
                if(item.path=='salesReturn'){                
                    return this.menuId=item.id                                        
                }
            });
            let buttonList = await getButton ({
            api:'saas.role.getButton',
            menuId: this.menuId,
            })
            this.button_list=buttonList.data.data
           
        },

        // 重置
        reset() {
            this.inputInfo.userName = '',
            this.inputInfo.oType = '',
            this.inputInfo.date = ''
        },
  
        // 查询
        demand() {
            this.currpage = 1
            this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.axios().then(() => {
                this.loading = false
                if(this.tableData.length > 5) {
                    this.showPagebox = true
                }
            })
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            // this.current_num = e
            this.pageSize = e
            this.axios().then(() => {
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
            this.axios().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

        // 导出弹框方法
        dialogShow() {
            this.dialogVisible = true
        },
      
        handleClose(done) {
            this.dialogVisible = false
        },

        async exportPage() {
            exports({
                api: 'admin.user.getupInfo',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                userName:this.inputInfo.userName,
                oType:this.inputInfo.oType,
                startDate: this.inputInfo.date?.[0]??'',
                endDate: this.inputInfo.date?.[1]??'',
                exportType:1,
            },'充值列表_导出本页')
        },
    
        async exportAll() {
            exports({
                api: 'admin.user.getupInfo',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                userName:this.inputInfo.userName,
                oType:this.inputInfo.oType,
                startDate: this.inputInfo.date?.[0]??'',
                endDate: this.inputInfo.date?.[1]??'',
            },'充值列表_导出全部')
        },
    
        async exportQuery() {
            exports({
                api: 'admin.user.getupInfo',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                userName:this.inputInfo.userName,
                oType:this.inputInfo.oType,
                startDate: this.inputInfo.date?.[0]??'',
                endDate: this.inputInfo.date?.[1]??'',
            },'充值列表_导出查询')
        }
    }
}
  </script>
  
  <style scoped lang="less">
.container {
  display: flex;
  flex-direction: column;
  /deep/.Search{
    display: flex;
    align-items: center;
    background: #ffffff;
    border-radius: 4px;
    padding: 10px;
    margin-bottom: 16px;
    .Search-condition{
      display: flex;
      align-items: center;
      .query-input {
        display: flex;
        margin-right: 10px;
        .Search-input{
          width: 280px;
          margin-right: 10px;
        }
        .select-input {
          margin-right: 8px;
          width: 180px;
        }
      }
    }
  }

  .menu-list {
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
      .cell {
        .status_name {
          width: 58px;
          height: 20px;
          line-height: 20px;
          text-align: center;
          display: block;
          border-radius: 10px;
          font-size: 14px;
          color: #fff;
          &.active {
              background-color: #18C364;
          }
          &.actives {
              background-color: #97A0B4;
          }
        }
      }
      .cell {
          img {
            width: 60px;
            height: 60px;
          }
      }
    }

    /deep/.el-table{
        .add {
          .cell {
            overflow: visible !important;
          }
        }
        .OP-button{     
          button{
            margin: 0 4px ;
          }  
          .OP-button-top{
            margin-bottom: 8px;
            button{
              margin-right: 0px !important;
            }
          }
          .OP-button-bottom {
            display: flex;
            justify-content: center;
            .item2{
              display: flex;
              justify-content: space-between;
              
              // button{
              //   margin-right: 8px;
              // }
            }
            .view {
              // button {
              //   margin-right: 0px !important;
              // }
            }
          }
        }
    }
  }

  .dialog-block {
    // 弹框样式
    /deep/.el-dialog {
      width: 460px;
      height: 17.5rem;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%,-50%);
      margin: 0 !important;
      .el-dialog__header {
        width: 100%;
        height: 58px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 2px solid #E9ECEF;
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
        padding: 0 !important;
        .el-form {
          width: 100%;
          .task-container {
            padding: 41px 0px 0px 0px !important;
            width: 100%;
            .through {
              width: 100%;
              display: flex;
              flex-direction: column;
              justify-content: center;
              align-items: center;
              h3 {
                margin-top: -20px;
              }
              .select-input {
                .el-form-item__label {
                  width: 100px !important;
                }
                .el-form-item__content {
                  margin-left: 100px !important;
                }
              }
              .refund {
                display: flex;
                justify-content: center;
                align-items: center;
                margin-top: 20px;
                .y-refund {
                  margin-right: 20px;
                }
                .s-refund {
                  .el-input {
                    width: 100px;
                    input {
                      padding-right: 0px !important;
                    }
                  }
                }
              }
            }

            .balance,.integral,.level {
              display: flex;
              justify-content: center;
              .el-form-item__content {
                margin-left: 0px !important;
                .el-input {
                  width: 200px;
                }
              }
            }
          }
        }
        .el-form-item__label {
          font-weight: normal;
        }
        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          border-top: 1px solid #E9ECEF;
          .el-form-item {
            padding: 0 !important;
            height: 100%;
            display: flex;
            justify-content: flex-end;
            margin-right: 17px;
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
      }
    }
  }
}
  </style>
  