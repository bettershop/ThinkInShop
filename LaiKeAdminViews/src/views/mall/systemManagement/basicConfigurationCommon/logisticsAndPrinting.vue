<template>
  <div class="container merchants-list">
    <el-form
        :model="ruleForm"
        :rules="rules"
        label-position="right"
        ref="ruleForm"
        label-width="auto"
        class="form-search"
      >
    <!-- 物流配置 wlpz -->
    <div class="form-card">

        <div class="title">{{ $t(`systemManagement.kdpz`) }}</div>

        <el-form-item :label="$t(`systemManagement.wlgs`)" style="">
            <!-- <el-switch
            v-model="ruleForm.is_courier"
            :active-value="1"
            :inactive-value="0"
            active-color="#00ce6d"
            inactive-color="#d4dbe8"
            >
            </el-switch> -->
            <el-select
            v-model="ruleForm.is_courier"
            :placeholder="$t(`systemManagement.qxzwlgs`)"
            style="margin-bottom: 10px; width: 370px"
            >
            <el-option label="快递100" :value="1"> </el-option>
            <el-option label="17track" :value="2"> </el-option>
            </el-select>
            <div class="iframe-row-card">


            <div style="margin: 22px 0 22px 0" v-if="ruleForm.is_courier == 2">
                <!-- prop="express_secret" 去除校验 -->
                <el-form-item
                label="secret"
                :label-width="language === 'en' ? '170px' : '120px'"
                >
                <el-input v-model="ruleForm.track_secret" maxlength="32"></el-input>
                </el-form-item>
            </div>
            <div style="margin: 22px 0 22px 0" v-if="ruleForm.is_courier == 1">
                <el-form-item
                :label="$t(`systemManagement.cxjk`)"
                :label-width="language === 'en' ? '170px' : '120px'"
                >
                <el-input v-model="ruleForm.expressAddress"></el-input>
                </el-form-item>
            </div>

            <div style="margin: 0 0 22px 0" v-if="ruleForm.is_courier == 1">
                <el-form-item
                :label="$t(`systemManagement.bm`)"
                :label-width="language === 'en' ? '170px' : '120px'"
                >
                <el-input v-model="ruleForm.expressNumber"></el-input>
                </el-form-item>
            </div>

            <div style="margin: 0 0 22px 0" v-if="ruleForm.is_courier == 1">
                <el-form-item
                :label="$t(`systemManagement.sqkey`)"
                :label-width="language === 'en' ? '170px' : '120px'"
                >
                <el-input v-model="ruleForm.authorization"></el-input>
                </el-form-item>
            </div>

            <div style="margin: 0 0 22px 0" v-if="ruleForm.is_courier === 1">
                <!-- prop="express_secret" 去除校验 -->
                <el-form-item
                label="secret"
                :label-width="language === 'en' ? '170px' : '120px'"
                >
                <el-input v-model="ruleForm.express_secret"></el-input>
                </el-form-item>
            </div>
               <div style="margin: 0 0 22px 0" v-if="ruleForm.is_courier === 1">
                <el-form-item
                  :label="$t('systemManagement.kd100ydy')"
                  :label-width="language === 'en' ? '170px' : '120px'">
                  <el-switch
                    v-model="ruleForm.is_open_cloud"
                    :active-value="1"
                    :inactive-value="0"
                    active-color="#00ce6d"
                    inactive-color="#d4dbe8"
                  >
                  </el-switch>
                </el-form-item>
              </div>
               <div style="margin: 0 0 22px 0" v-if="ruleForm.is_open_cloud === 1 && ruleForm.is_courier === 1">
                <el-form-item
                  label="siid"
                  :label-width="language === 'en' ? '170px' : '120px'"
                >
                  <el-input v-model="ruleForm.siid" :placeholder="$t('systemManagement.qsrdjysbm')"></el-input>
                </el-form-item>
              </div>
              <div style="margin: 0 0 22px 0" v-if="ruleForm.is_open_cloud === 1 && ruleForm.is_courier === 1">
                <el-form-item
                  :label="$t('systemManagement.hddz')"

                  :label-width="language === 'en' ? '170px' : '120px'"
                >
                  <el-input v-model="ruleForm.cloud_notify"
                            :placeholder="$t('systemManagement.qsrhddz')"></el-input>
                </el-form-item>
              </div>


            </div>
        </el-form-item>
    </div>
    <!--打印配置 dypz -->
    <div class="form-card">
            <div class="title">{{ $t(`systemManagement.dypz`) }}</div>
            <el-form-item :label="$t('systemManagement.dypz')">
              <!-- <el-switch v-model="ruleForm.is_unipush" :active-value="1" :inactive-value="0" active-color="#00ce6d" inactive-color="#d4dbe8">
            </el-switch> -->

              <p style="color: #97a0b4">
                {{ $t("systemManagement.dypzts") }}
              </p>
              <div class="iframe-row-card">
                <div class="" style="margin: 22px 0 22px 0">
                  <el-form-item
                    :label="$t('systemManagement.dymc')"
                    prop="printName"
                    :label-width="language === 'en' ? '170px' : '120px'"
                  >
                    <el-input
                      v-model="ruleForm.printName"
                      :placeholder="$t('systemManagement.dymcts')"
                      maxlength="30"
                    ></el-input>
                  </el-form-item>
                </div>

                <div class="" style="margin: 0 0 22px 0">
                  <el-form-item
                    :label="$t('systemManagement.dywz')"
                    prop="printUrl"
                    :label-width="language === 'en' ? '170px' : '120px'"
                  >
                    <el-input
                      v-model="ruleForm.printUrl"
                      :placeholder="$t('systemManagement.dywzts')"
                      maxlength="30"
                    ></el-input>
                  </el-form-item>
                </div>

                <div class="" style="margin: 0 0 22px 0">
                  <el-form-item
                    :label="$t('systemManagement.dydz')"
                    prop="sheng"
                    :label-width="language === 'en' ? '170px' : '120px'"
                  >
                    <div class="select-input-box">
                      <div class="item">
                        <el-select
                          :class="
                            language === 'en' ? 'select-input2' : 'select-input'
                          "
                          v-model="ruleForm.sheng"
                          :placeholder="$t('aftersaleAddress.add.sheng')"
                        >
                          <el-option
                            v-for="(item, index) in shengList"
                            :key="index"
                            :label="item.districtName"
                            :value="item.districtName"
                          >
                            <div @click="getShi(item.id)">
                              {{ item.districtName }}
                            </div>
                          </el-option>
                        </el-select>
                      </div>
                      <div class="gap"> </div>
                      <div class="item">
                        <el-select
                          :class="
                            language === 'en' ? 'select-input2' : 'select-input'
                          "
                          v-model="ruleForm.shi"
                          :placeholder="$t('aftersaleAddress.add.shi')"
                        >
                          <el-option
                            v-for="(item, index) in shiList"
                            :key="index"
                            :label="item.districtName"
                            :value="item.districtName"
                          >
                            <div @click="getXian(item.id)">
                              {{ item.districtName }}
                            </div>
                          </el-option>
                        </el-select>
                      </div>
                      <div class="gap"> </div>
                      <div class="item">
                        <el-select
                          :class="
                            language === 'en' ? 'select-input2' : 'select-input'
                          "
                          v-model="ruleForm.xian"
                          :placeholder="$t('aftersaleAddress.add.xian')"
                        >
                          <el-option
                            v-for="(item, index) in xianList"
                            :key="index"
                            :label="item.districtName"
                            :value="item.districtName"
                          >
                            <div>{{ item.districtName }}</div>
                          </el-option>
                        </el-select>
                      </div>
                    </div>
                  </el-form-item>
                </div>
                <div class="" style="margin: 0 0 22px 0">
                  <el-form-item
                    :label="''"
                    prop="address"
                    :label-width="language === 'en' ? '170px' : '120px'"
                  >
                    <el-input
                      v-model="ruleForm.address"
                      :placeholder="$t('systemManagement.dydzts')"
                      maxlength="30"
                    ></el-input>
                  </el-form-item>
                </div>

                <div class="" style="margin: 0 0 22px 0">
                  <el-form-item
                    :label="$t('systemManagement.dydh')"
                    prop="phone"
                    :label-width="language === 'en' ? '170px' : '120px'"
                  >
                    <el-input
                      v-model="ruleForm.phone"
                      :placeholder="$t('systemManagement.dydhts')"
                      maxlength="30"
                    ></el-input>
                  </el-form-item>
                </div>
              </div>
            </el-form-item>
          </div>

    </el-form>

    <!-- 面单配置  -->
    <div class="form-card">
        <div class="title">{{ $t(`systemManagement.mdpz`) }}</div>

        <div class="Search">
            <div class="Search-condition">
                <div class="query-input">
                <el-input
                    size="medium"
                    @keyup.enter.native="demand"
                    class="Search-input"
                    v-model="gsName"
                    placeholder="请输入物流公司名称/编码"
                ></el-input>
                </div>
                <div class="btn-list">
                <el-button class="fontColor" @click="reset">{{
                    $t("DemoPage.tableExamplePage.reset")
                }}</el-button>
                <el-button class="bgColor" type="primary" @click="demand">{{
                    $t("DemoPage.tableExamplePage.demand")
                }}</el-button>
                </div>
            </div>
        </div>
    <div
      :class="language == 'en' ? 'jump-list jump-list2' : 'jump-list'"
      ref="tableFather"
    >
      <el-button
        class="bgColor laiketui laiketui-add"
        type="primary"
        @click="$router.push('/order/setExpressSheet/addExpress')"
        >{{ $t('sheetOfNoodles.tjsz') }}</el-button
      >
    </div>
    <!-- 表格数据 -->
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
                <p style="color: #414658">{{ $t("zdata.zwsj") }}</p>
            </div>
            </template>
            <el-table-column prop="id" label="ID"></el-table-column>
            <el-table-column prop="type" label="物流公司编码"></el-table-column>
            <el-table-column
            prop="kuaidi_name"
            label="物流公司名称"
            ></el-table-column>
            <el-table-column prop="partnerId" label="partnerid"></el-table-column>
            <el-table-column prop="add_time" label="添加时间"></el-table-column>

            <el-table-column
            fixed="right"
            :label="$t('Platform.numerical.cz')"
            width="200"
            >
            <template slot-scope="scope">
                <div class="OP-button">
                <div class="OP-button-top">
                    <el-button
                    icon="el-icon-edit-outline"
                    @click="handleGoEdit(scope.row.id,scope.row.express_id)"
                    >{{ $t("Platform.numerical.bianji") }}</el-button
                    >
                    <el-button
                    icon="el-icon-delete"
                    @click="Delete(scope.row.id)"
                    >{{ $t("Platform.numerical.shanchu") }}</el-button
                    >
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
</template>

<script>
import {
  getSystemIndex
} from '@/api/mall/systemManagement'
import { getTableList, del } from "@/api/order/setExpressSheet";
import pageData from "@/api/constant/page";
import cascade from '@/api/publics/cascade'
import { getSecConfig } from "@/api/plug_ins/preSale";

import { mixinstest } from "@/mixins/index";


export default {
    name:'logisticsAndPrinting',
    mixins:[mixinstest],
    props:{
        language:{
            type:String,
            default:''
        }
    },
    data(){
        return{
            ruleForm:{
                // 快递配置
                is_courier: 1, // 是否开启快递100
                expressNumber: '',  // 查询接口地址
                expressAddress: '',   // 查询接口地址
                authorization: '',
                track_secret:'',  // 17track  使用
                express_secret: '',  // secret
                express_tempId: '',   // tempId
                is_open_cloud: 0,  //是否开启云打印
                siid:'',           //打印机设备码
                cloud_notify:'',   //云打印回调地址
                // 打印配置
                printName:'',  // 打印名称
                printUrl: '', // 打印网址
                sheng: '',   // 省
                shi: '',  // 市
                xian: '', // 县
                address: '', // 地址
                phone: '', // 联系电话
            },

            loading: true,
            tableData: [],
            tableHeight: '',
            shiList: [],
            page: pageData.data(),
            gsName: '',
            shengList: [],
            xianList: [],

            rules: {
                // printName: [
                // {
                //     required: true,
                //     message: this.$t('systemManagement.dymcts'),
                //     trigger: 'blur'
                // }
                // ],
                // printUrl: [
                // {
                //     required: true,
                //     message: this.$t('systemManagement.dywzts'),
                //     trigger: 'blur'
                // }
                // ],
                // sheng: [
                // {
                //     required: true,
                //     message: this.$t('aftersaleAddress.add.qxzs'),
                //     trigger: 'change'
                // }
                // ],
                // shi: [
                // {
                //     required: true,
                //     message: this.$t('aftersaleAddress.add.qxzss'),
                //     trigger: 'change'
                // }
                // ],
                // xian: [
                // {
                //     required: true,
                //     message: this.$t('aftersaleAddress.add.qxzx'),
                //     trigger: 'change'
                // }
                // ],
                track_secret: [
                {
                    required: true,
                    message: this.$t('systemManagement.qsrsecret'),
                    trigger: 'blur'
                }
                ],
                address: [
                {
                    required: true,
                    message: this.$t('systemManagement.dydzts'),
                    trigger: 'blur'
                }
                ],
                // phone: [
                // {
                //     required: true,
                //     message: this.$t('systemManagement.dydhts'),
                //     trigger: 'blur'
                // }
                // ],
            },
        }
    },
    created(){
        this.getSystemIndexs();
        this.getHeight();
        // 获取 列表信息
        this.getTabList();
        this.getSheng()
    },
    methods:{
      // 收索配置保存
      submitForm(){

        if (this.ruleForm.is_open_cloud === 1){
          if (!this.ruleForm.siid || this.ruleForm.siid.trim() === '') {
            this.$message.error({
              message: this.$t('systemManagement.qsrdjysbm'),
              offset: 100
            });
            return;
          }
          if (!this.ruleForm.cloud_notify || this.ruleForm.cloud_notify.trim() === '') {
            this.$message.error({
              message: this.$t('systemManagement.qsrhddz'),
              offset: 100 // 新增：提示框偏移100px
            });
            return;
          }
        }
            getSecConfig({
              api: "admin.system.LogisticsAndPrinting",
              ...this.ruleForm
              }).then((res) => {
                if(res.data.code == 200){
                    this.succesMsg(this.$t('commonLanguage.xgcg'))
                    this.indexs()
                }
            });
        },
        getHeight() {
            this.$nextTick(()=>{
                this.tableHeight =
                this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight + 200;
            })
         },
          // 跳转编辑
        handleGoEdit(id,express_id) {
            this.$router.push({
                name: "editExpress",
                query: {
                id: id,
                express_id:express_id
                },
            });
        },
        // 删除数据
        Delete(id) {
            let data = {
                api: "admin.express.del_logistics",
                id: id,
            };

            this.$confirm("确定要删除该面单设置吗？", this.$t("smsList.ts"), {
                confirmButtonText: this.$t("smsList.okk"),
                cancelButtonText: this.$t("smsList.ccel"),
                type: "warning",
            })
                .then(() => {
                del(data).then((res) => {
                    console.log("resres", res);
                    if (res.data.code == "200") {
                    this.$message({
                        type: "success",
                        message: this.$t("zdata.sccg"),
                        offset: 102,
                    });
                    this.getTabList()
                    }
                });
                })
                .catch(() => {});
        },
        reset() {
            this.gsName = ""
        },
        demand(e) {
            this.currpage = 1;
            this.current_num = 10;
            // this.showPagebox = false;
            this.page.loading = true;
            this.dictionaryNum = 1;
            this.getTabList().then(() => {
                this.page.loading = false;
                if (this.page.tableData.length > 5) {
                this.showPagebox = true;
                }
            });
        },
        // 获取表格数据
        async getTabList() {
            const res = await getTableList({
                api: "admin.express.logistics_list",
                name: this.gsName,
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
            });
            // if(res.code==200){
            this.total = res.data.data.total;
            this.tableData = res.data.data.list;
            this.loading = false;
            if (res.data.data.total < 10) {
                this.current_num = this.total;
            }
            if (this.total < this.current_num) {
                this.current_num = this.total;
            }
            // }
        },
        // 页码切换
        handleSizeChange(e) {
            this.page.loading = true;
            this.page.pageSize = e;
            this.getTabList().then(()=>{
                this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
                this.current_num =
                this.page.tableData.length === this.pageSize
                    ? this.dictionaryNum * this.pageSize
                    : this.total;
                this.page.loading = false;
            })
        },
        handleCurrentChange(e) {
            this.page.loading = true;
            this.dictionaryNum = e;
            this.currpage = (e - 1) * this.pageSize + 1;
            this.getTabList().then(()=>{
                this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
                this.current_num =
                this.page.tableData.length === this.pageSize
                    ? this.dictionaryNum * this.pageSize
                    : this.total;
                this.page.loading = false;
            })
        },
         // 获取省级
         async getSheng() {
            const res = await cascade.getSheng()
            this.shengList = res.data.data
            },
            // 获取市级
            async getShi(sid, flag) {
            const res = await cascade.getShi(sid)
            this.shiList = res.data.data
            if (!flag) {
                this.ruleForm.shi = ''
                this.ruleForm.xian = ''
            }
            },
            // 获取县级
            async getXian(sid, flag) {
            const res = await cascade.getXian(sid)
            this.xianList = res.data.data
            if (!flag) {
                this.ruleForm.xian = ''
            }
        },
        async getSystemIndexs() {
          let { entries } = Object
          let data = {
            api: 'admin.system.GetLogisticsAndPrinting'
          }
          let formData = new FormData()
          for (let [key, value] of entries(data)) {
            formData.append(key, value)
          }
          const res = await getSystemIndex(formData)
          console.log(res)
          let systemInfo = res.data.data.express_list || res.data.data.list
          let printSetupConfig = res.data.data.print_list || res.data.data.list

            this.ruleForm.expressNumber = systemInfo.express_number
            this.ruleForm.expressAddress = systemInfo.express_address
            this.ruleForm.authorization = systemInfo.express_key
            this.ruleForm.express_secret = systemInfo.express_secret || ""
            this.ruleForm.track_secret = systemInfo.track_secret || ""
            this.ruleForm.express_tempId = systemInfo.express_tempId || ""
            // this.ruleForm.is_courier = systemInfo.is_express, // 是否开启快递100
            this.ruleForm.is_open_cloud = systemInfo.is_open_cloud || 0
            this.ruleForm.siid = systemInfo.siid || ""
            this.ruleForm.cloud_notify = systemInfo.cloud_notify || ""

            //打印设置
            this.ruleForm.printName = printSetupConfig.print_name
            this.ruleForm.printUrl = printSetupConfig.print_url
            this.ruleForm.phone = printSetupConfig.phone
            this.ruleForm.address = printSetupConfig.address
            this.ruleForm.sheng = printSetupConfig.sheng ? printSetupConfig.sheng : ''
            this.ruleForm.shi = printSetupConfig.shi ? printSetupConfig.shi : ''
            this.ruleForm.xian = printSetupConfig.xian ? printSetupConfig.xian : ''

        },
    }
}
</script>

<style lang="less" scoped>

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

.jump-list2,.jump-list{
    margin: 0px 10px;
}
.dictionary-list{
  padding:0 24px;
  box-sizing: border-box;
}
</style>
