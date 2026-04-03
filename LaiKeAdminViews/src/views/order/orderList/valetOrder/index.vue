<template>
  <div class="container">
    <div class="header">
      <span>{{ $t("valetOrder.dkxd") }}</span>
    </div>
    <div class="text item">
      <div class="flex">
        <div class="BuyUser">
          <span>*</span>
          <label>{{ $t("valetOrder.gmyh") }}</label>
        </div>
        <div class="changeUser" style="max-height: 350px">
          <el-button plain class="cancel" @click="AddUser">{{
            $t("valetOrder.xzyh")
          }}</el-button>
          <el-table
            :class="tableData.length == 0 ? 'btt_flex' : ''"
            :data="tableData"
            style="max-height: 350px; width: 99% !important"
            :header-cell-style="header"
          >
            <template slot="empty">
              <div class="empty">
                <p style="color: #97a0b4">{{ $t("zdata.zwsj") }}</p>
              </div>
            </template>
            <el-table-column
              prop="user_id"
              align="center"
              :label="$t('valetOrder.yhid')"
            >
            </el-table-column>
            <el-table-column
              prop="zhanghao"
              align="center"
              :label="$t('valetOrder.yhzh')"
            >
            </el-table-column>
            <el-table-column
              prop="mobile"
              align="center"
              :label="$t('valetOrder.sjhm')"
            >
            </el-table-column>
            <el-table-column
              prop="money"
              align="center"
              :label="$t('valetOrder.ye',{currencySymbol:laikeCurrencySymbol})"
            >
              <template slot-scope="scope">
<!--                <span>{{laikeCurrencySymbol}} {{ LaiKeCommon.formatPrice(scope.row.money,user_exchange_rate)}}</span>-->
                <span>{{laikeCurrencySymbol}} {{ scope.row.money}}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop=""
              align="center"
              :label="$t('valetOrder.chdz')"
            >
              <template slot-scope="scope">
                <div class="addressInfo" v-if="scope.row.userAddress">
                  <div>
                    <span>{{ scope.row.userAddress.name }}</span>
                    <span style="padding-left: 5px">{{
                      scope.row.userAddress.tel
                    }}</span>
                  </div>
                  <p>
                    {{ scope.row.userAddress.sheng }}
                    {{ scope.row.userAddress.city }}
                    {{ scope.row.userAddress.quyu }}
                    {{ scope.row.userAddress.address }}
                  </p>
                </div>
                <a class="addA" @click="addAddress(scope.$index)" v-else>{{
                  $t("valetOrder.tjdz")
                }}</a>
              </template>
            </el-table-column>
          </el-table>
          <!-- <el-table
            :class="tableData.length == 0 ? 'btt_flex' : ''"
            :data="tableData"
            style="max-height: 350px"
            :header-cell-style="header"
          >

          </el-table> -->
        </div>
      </div>
      <div class="flex">
        <div class="BuyUser">
          <span>*</span>
          <label>{{ $t("valetOrder.gmsp") }}</label>
        </div>
        <div class="changeUser" style="height: 330px">
          <el-button plain @click="AddPro" class="cancel">{{
            $t("valetOrder.tjsp")
          }}</el-button>
          <el-table
            :row-key="rowKeys2"
            :data="ProData"
            height="100%"
            :key="itemKey"
            :header-cell-style="header"
            style="border-bottom: 1px solid #e9ecef; width: 99% !important"
          >
            <template slot="empty">
              <div class="empty">
                <p style="color: #97a0b4">{{ $t("zdata.zwsj") }}</p>
              </div>
            </template>
            <el-table-column
              prop="goodsId"
              align="center"
              :label="$t('valetOrder.spbh')"
            >
            </el-table-column>
            <el-table-column
              prop="proName"
              align="center"
              :label="$t('valetOrder.spmc')"
            >
              <template slot-scope="scope">
                <div class="Info">
                  <img
                    :src="scope.row.imgurl"
                    width="40"
                    height="40"
                    alt=""
                    @error="handleErrorImg"
                  />
                  <span>{{ scope.row.product_title }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              prop="attribute"
              align="center"
              :label="$t('valetOrder.gg')"
            >
            </el-table-column>
            <el-table-column
              prop="price"
              align="center"
              :label="$t('valetOrder.jg',{currencySymbol:user_currency_symbol})"
            >
              <template slot-scope="scope">
                <span>{{ LaiKeCommon.formatPrice(scope.row.price,user_exchange_rate)}}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="nums"
              align="center"
              :label="`${$t('valetOrder.gmsl')}(${conut})`"
            >
            </el-table-column>
            <el-table-column
              prop="freight"
              align="center"
              :label="$t('valetOrder.yf',{currencySymbol:user_currency_symbol})"
              class="change_of_freight"
            >
              <template slot-scope="scope">

                <el-input
                  v-model="scope.row.freight"
                  @input="handleChargeCarriage(scope.row, scope)"
                  @blur="handleChangeFreigh(scope.row, scope)"
                  :min="0"
                ></el-input>
              </template>
            </el-table-column>
            <el-table-column
              prop="name"
              align="center"
              :label="$t('valetOrder.ssdp')"
            >
            </el-table-column>
            <el-table-column
              prop="action"
              align="center"
              :label="$t('valetOrder.cz')"
            >
              <template slot-scope="scope">
                <el-button
                  class="delete"
                  @click="ChangeProdel(scope.$index, scope.row)"
                  plain
                  icon="el-icon-delete"
                  >{{ $t("valetOrder.shanchu") }}</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div class="flex">
        <div class="BuyUser">
          <span>*</span>
          <label>{{ $t("valetOrder.zffs") }}</label>
        </div>
        <div class="changeUser">
          <div></div>
          <div class="pay_d">
            <div class="pay_div" @click="isOfflinePayment=0" :style="{'borderColor': (isOfflinePayment==0 ?'#2890ff' : '')}">
              <img class="bgimg" src="@/assets/images/xxzf.png" alt="">
              <img v-if="isOfflinePayment==0" class="gouxuan" src="@/assets/images/gouxuan.png" alt="">
            </div>
            <div class="pay_div" @click="isOfflinePayment=1" :style="{'borderColor': (isOfflinePayment==1 ?'#2890ff' : '')}">
              <img class="bgimg" src="@/assets/images/yezf.png" alt="">
              <img v-if="isOfflinePayment==1" class="gouxuan" src="@/assets/images/gouxuan.png" alt="">

            </div>
          </div>
        </div>
      </div>
      <div class="BottomPrice">
        <div v-if="ProData && ProData.length>0" class="list-totle">
          {{ $t('home.homeReport.spzs') }}:{{ ProData.length }}
        </div>
        <div class="Price">
          <label for="">{{ $t("valetOrder.spzj") }}：</label>
          <span>{{user_currency_symbol}}{{ LaiKeCommon.formatPrice((total_price || 0),user_exchange_rate) }}</span>
        </div>
        <div class="Price">
          <label for="">{{ $t("valetOrder.yf1") }} {{user_currency_symbol}}：</label>
          <span>{{user_currency_symbol}}{{ LaiKeCommon.formatPrice((zfreight || 0),user_exchange_rate) }}</span>
          <!-- <el-input
            v-model="zfreight"
            placeholder=""
          ></el-input> -->
        </div>
        <div class="Price">
          <label for="">{{ $t("valetOrder.lj") }}：{{user_currency_symbol}}</label>
          <el-input
            v-model="knock"
            @blur="wipeBlur"
            @keyup.native="knock = oninput(knock, 2)"
            placeholder=""
          ></el-input>
        </div>
        <div class="Price Total">
          <label for="">{{ $t("valetOrder.hjzf") }}：</label>
          <span>{{user_currency_symbol}}{{ LaiKeCommon.formatPrice((combined || 0),user_exchange_rate)  }}</span>
        </div>
      </div>
    </div>
    <div class="dialog_one">
      <el-dialog :title="$t('valetOrder.xzyh')" :visible.sync="dialogVisible">
        <div class="">
          <div class="Search">
            <el-input
              class="Search-input"
              v-model="search"
              :placeholder="$t('valetOrder.qsryhid')"
            ></el-input>
            <el-button @click="Reset" plain>{{
              $t("DemoPage.tableExamplePage.reset")
            }}</el-button>
            <el-button @click="query2" type="primary" v-enter="query2">{{
              $t("DemoPage.tableExamplePage.demand")
            }}</el-button>
          </div>
          <div class="Table">
            <el-table
              height="350"
              :data="userdata"
              style="width: 100%"
              @current-change="handleSelectionChange"
            >
              <el-table-column
                :label="$t('valetOrder.xz')"
                align="center"
                width="55"
              >
                <template slot-scope="scope">
                  <el-radio :label="scope.row" v-model="tableRadio"
                    ><i></i
                  ></el-radio>
                </template>
              </el-table-column>
              <el-table-column
                prop="user_id"
                align="center"
                :label="$t('valetOrder.yhid')"
              >
              </el-table-column>
              <el-table-column
                prop="zhanghao"
                align="center"
                :label="$t('valetOrder.yhzh')"
              >
              </el-table-column>
              <el-table-column
                prop="mobile"
                align="center"
                :label="$t('valetOrder.sjhm')"
              >
              </el-table-column>
            </el-table>
            <div class="pageBox">
              <div class="pageLeftText">
                {{ $t("DemoPage.tableExamplePage.show") }}
              </div>
              <el-pagination
                layout="sizes, slot, prev, pager, next"
                :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
                :next-text="$t('DemoPage.tableExamplePage.next_text')"
                @size-change="handleSizeChange"
                :page-sizes="pagesizes"
                :pager-count="5"
                :current-page="pagination.page"
                @current-change="handleCurrentChange"
                :total="total"
              >
                <div class="pageRightText">
                  {{ $t("DemoPage.tableExamplePage.on_show") }} {{ showItem2 }}
                  {{ $t("DemoPage.tableExamplePage.twig") }}{{ total
                  }}{{ $t("DemoPage.tableExamplePage.twig_notes") }}
                </div>
              </el-pagination>
            </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button class="footer_one" @click="dialogVisible = false">{{
            $t("valetOrder.ccel")
          }}</el-button>
          <el-button type="primary" @click="confirm">{{
            $t("valetOrder.okk")
          }}</el-button>
        </span>
      </el-dialog>
    </div>
    <div class="dialog_one">
      <el-dialog
        :title="$t('valetOrder.tjdz')"
        :visible.sync="dialogVisible2"
        :before-close="handleClose"
        width="680px"
      >
        <el-form
          :model="ruleForm"
          :rules="rules"
          ref="ruleForm"
          label-width="100px"
          class="demo-ruleForm"
        >
          <el-form-item :label="$t('valetOrder.shr')" prop="name">
            <el-input v-model="ruleForm.name"></el-input>
          </el-form-item>
          <el-form-item :label="$t('valetOrder.lxdh')" prop="tel">
            <el-input v-model="ruleForm.tel"></el-input>
          </el-form-item>
          <el-form-item
            class="cascadeAddress"
            :label="$t('valetOrder.lxdz')"
            prop="quyu"
          >
            <div class="cascadeAddress-block">
              <el-form-item prop="sheng">
                <el-select
                  class="select-input"
                  v-model="ruleForm.sheng"
                  :placeholder="$t('valetOrder.sheng')"
                >
                  <el-option
                    v-for="(item, index) in shengList"
                    :key="index"
                    :label="item.districtName"
                    :value="item.districtName"
                  >
                    <div @click="getShi(item.id)">{{ item.districtName }}</div>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item prop="city">
                <el-select
                  class="select-input"
                  v-model="ruleForm.city"
                  :placeholder="$t('valetOrder.shi')"
                >
                  <el-option
                    v-for="(item, index) in shiList"
                    :key="index"
                    :label="item.districtName"
                    :value="item.districtName"
                  >
                    <div @click="getXian(item.id)">{{ item.districtName }}</div>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item prop="">
                <el-select
                  class="select-input"
                  v-model="ruleForm.quyu"
                  :placeholder="$t('valetOrder.xian')"
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
              </el-form-item>
            </div>
          </el-form-item>
          <el-form-item :label="$t('valetOrder.xxdz')" prop="address">
            <el-input
              :placeholder="$t('valetOrder.qsrxxdz')"
              v-model="ruleForm.address"
            ></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button class="footer_one" @click="handleClose()">{{
            $t("valetOrder.ccel")
          }}</el-button>
          <el-button type="primary" @click="Addconfirm">{{
            $t("valetOrder.okk")
          }}</el-button>
        </span>
      </el-dialog>
    </div>
    <div class="dialog_one">
      <el-dialog
        :title="$t('valetOrder.tjsp')"
        :visible.sync="dialogVisible3"
        width="920px"
      >
        <div class="">
          <el-radio-group v-model="tabKey">
            <el-radio-button label="0" :class="{ active1: tabKey == 1 }">{{
              $t("valetOrder.xzsp")
            }}</el-radio-button>
            <el-radio-button label="1" :class="{ active2: tabKey == 0 }">{{
              $t("valetOrder.yxsp")
            }}</el-radio-button>
          </el-radio-group>
          <div class="Search" style="margin-top: 20px" v-show="tabKey == '0'">
            <el-cascader
              style="margin-right: 10px"
              v-model="goodsClass"
              class="Search-select"
              ref="myCascader"
              :placeholder="$t('valetOrder.qxzspfl')"
              :options="classList"
              :props="{ checkStrictly: true }"
              @change="changeProvinceCity"
              clearable
            >
            </el-cascader>
            <el-select
              class="Search-select"
              v-model="brand"
              :placeholder="$t('valetOrder.qxzsppp')"
            >
              <el-option
                v-for="item in brandList"
                :key="item.brand_id"
                :label="item.brand_name"
                :value="item.brand_id"
              >
              </el-option>
            </el-select>
            <el-input
              class="Search-input"
              :placeholder="$t('valetOrder.qsrspmc')"
              v-model="Proname"
            ></el-input>
            <el-button @click="Reset" plain>{{
              $t("DemoPage.tableExamplePage.reset")
            }}</el-button>
            <el-button @click="query" type="primary" v-enter="query">{{
              $t("DemoPage.tableExamplePage.demand")
            }}</el-button>
          </div>
          <div class="Table" v-show="tabKey == '0'">
            <el-table
              ref="goodsListTable"
              :data="ProList"
              :row-key="rowKeys"
              style="width: 100%"
              @selection-change="handleSelectionChange2"
              height="350"
            >
              <el-table-column
                :reserve-selection="true"
                align="center"
                type="selection"
                width="55"
              ></el-table-column>
              <el-table-column
                prop="ProName"
                align="center"
                :label="$t('valetOrder.spmc')"
              >
                <template slot-scope="scope">
                  <div class="Info">
                    <img
                      :src="scope.row.imgurl"
                      width="40"
                      height="40"
                      alt=""
                      @error="handleErrorImg"
                    />
                    <span>{{ scope.row.product_title }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="attribute"
                align="center"
                :label="$t('valetOrder.gg')"
              >
              </el-table-column>
              <el-table-column
                prop="price"
                align="center"
                :label="$t('valetOrder.jg',{currencySymbol:user_currency_symbol})"
              >
                <template slot-scope="scope">
                  <span>{{ LaiKeCommon.formatPrice(scope.row.price,user_exchange_rate)}}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="num"
                align="center"
                :label="$t('valetOrder.kc')"
              >
              </el-table-column>
              <el-table-column
                prop="name"
                align="center"
                :label="$t('valetOrder.ssdp')"
              >
              </el-table-column>
            </el-table>
            <div class="pageBox">
              <div class="pageLeftText">
                {{ $t("DemoPage.tableExamplePage.show") }}
              </div>
              <el-pagination
                layout="sizes, slot, prev, pager, next"
                :prev-text="$t('DemoPage.tableExamplePage.prev_text')"
                :next-text="$t('DemoPage.tableExamplePage.next_text')"
                @size-change="handleSizeChange"
                :page-sizes="pagesizes"
                :pager-count="5"
                :current-page="pagination.page"
                @current-change="handleCurrentChange"
                :total="total2"
              >
                <div class="pageRightText">
                  {{ $t("DemoPage.tableExamplePage.on_show") }}{{ showItem
                  }}{{ $t("DemoPage.tableExamplePage.twig") }}{{ total2
                  }}{{ $t("DemoPage.tableExamplePage.twig_notes") }}
                </div>
              </el-pagination>
            </div>
          </div>
          <div class="Table" v-show="tabKey == '1'">
            <el-table height="350" :data="ChangeProList" :key="Math.random()">
              <el-table-column
                prop="ProName"
                align="center"
                :label="$t('valetOrder.spmc')"
              >
                <template slot-scope="scope">
                  <div class="Info">
                    <img
                      :src="scope.row.imgurl"
                      width="40"
                      height="40"
                      alt=""
                      @error="handleErrorImg"
                    />
                    <span>{{ scope.row.product_title }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="attribute"
                align="center"
                :label="$t('valetOrder.gg')"
              >
              </el-table-column>
              <el-table-column
                prop="price"
                align="center"
                :label="$t('valetOrder.jg',{currencySymbol:user_currency_symbol})"
              >
                <template slot-scope="scope">
                  <span>{{ LaiKeCommon.formatPrice(scope.row.price,user_exchange_rate)}}</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="nums"
                align="center"
                width="200"
                :label="$t('valetOrder.gmsl')"
              >
                <template slot-scope="scope">
                  <el-input-number
                    style="width: 170px"
                    v-model="scope.row.nums"
                    @change="handleChange($event, scope.$index)"
                    :min="1"
                    :max="Number(scope.row.num)"
                    :label="$t('valetOrder.mswz')"
                    :controls="false"
                  ></el-input-number>
                </template>
              </el-table-column>
              <el-table-column
                prop="name"
                align="center"
                :label="$t('valetOrder.ssdp')"
              >
              </el-table-column>
            </el-table>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button class="footer_one" @click="dialogVisible3 = false">{{
            $t("valetOrder.ccel")
          }}</el-button>
          <el-button type="primary" @click="AddProconfirm">{{
            $t("valetOrder.okk")
          }}</el-button>
        </span>
      </el-dialog>
    </div>
    <div class="footer-button">
      <el-button
        plain
        class="footer-cancel fontColor"
        @click="$router.go(-1)"
        >{{ $t("valetOrder.ccel") }}</el-button
      >
      <el-button
        type="primary"
        class="footer-save bgColor mgleft"
        @click="submitForm('ruleForm')"
        >{{ $t("valetOrder.qrxd") }}</el-button
      >
    </div>
  </div>
</template>

<script>
import valetOrder from "@/webManage/js/order/orderList/valetOrder";
export default valetOrder;
</script>

<style scoped lang="less">
@import "../../../../webManage/css/order/orderList/valetOrder.less";
.list-totle{
  margin: 10px 0px;
    position: absolute;
    left: 0px;
}
.pay_d {
    display: flex;
    margin-top: -20px;
    .pay_div {
      width: 200px;
      height: 88px;
      border-radius: 8px;
      margin-right: 20px;
      border: 1px solid #B2BCD1;
      position: relative;
      .bgimg{
        width: 200px;
      }
      .gouxuan{
        width: 32px;
        height: 32px;
        position: absolute;
        right: 0;
        bottom: 0;
      }
    }

  }
</style>
