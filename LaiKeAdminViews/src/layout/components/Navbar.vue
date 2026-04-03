<template>
  <div class="navbar" ref="navHeader">
    <div class="left-control">
      <!-- <img :src="$store.getters.merchants_Logo" alt="" /> -->
      <img :src="merchants_Logo" alt="" />
      <hamburger :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    </div>

    <div class="right-block">
      <ul class="Hui-userbar">
        <li class="dropDown">
          <div class="top-dropDown">
            <img :src="$store.getters.head_img?$store.getters.head_img:'xxxxxx'" alt="" @error="handleErrorImg" />
            <span>{{ username }}</span>
            <i class="el-icon-arrow-down el-icon--right"></i>
          </div>
          <ul class="dropDown-menus">
            <li @click="dialogShow">
              <i class="el-icon-lock"></i>
              <span>{{ $t("topNav.changePassword") }}</span>
              <!-- <span>修改密码</span> -->
            </li>
            <li @click="dialogShow2">
              <i class="el-icon-user"></i>
              <!-- <span>基本信息</span> -->
              <span>{{ $t("topNav.basicInformation") }}</span>
            </li>
          </ul>
        </li>
        <li class="changeStore" v-if="type == 0">
          <div class="top-changeStore">
            <img class="shanghu" src="@/assets/imgs/shanghu.png" alt="" />
            <span>{{ defaultVersion }}</span>
            <img class="qiehuan" src="@/assets/imgs/qiehuan.png" alt="" />
          </div>
          <ul class="changeStore-menus">
            <li v-for="(item, index) in versionList" :key="index" @click="enterSystem(item)">
              <span>{{ item.name }}</span>
              <img v-show="item.id === showStoreId" src="../../assets/imgs/gouxuan.png" alt="" />
            </li>
          </ul>
        </li>
        <li class="headerLi">
          <div class="top-headerLi">
            <el-badge :value="allNum" :max="99" class="totle-item" v-if="allNum!==0"> </el-badge>
            <img src="@/assets/imgs/xiaoxi.png" alt="" />
          </div>
          <ul class="headerLi-menus">
            <li class="leveone">
              <div class="topmenus" @click="orderShow = !orderShow">
                <span class="order-remind">{{ $t("topNav.ddtx") }}</span>
                <div class="read">
                  <div class="operation">
                    <div v-if="orderInfo.total" class="operation-read" @click.stop="orderAllRead">
                      <i class="laiketui laiketui-biaojiweiyidu"></i>
                      <span @click="noticeReads('1,2,3,4,5,6')">{{
                        $t("topNav.yjyd")
                      }}</span>
                    </div>
                    <!-- <el-badge :value="orderInfo.total" :max="99" class="item" >
                    </el-badge> -->
                    <template v-if="orderInfo.total !== 0">
                      <span>（{{ orderInfo.total }}）</span>
                      <i :class="[
                          orderShow
                            ? 'el-icon-arrow-down'
                            : 'el-icon-arrow-right',
                        ]"></i>
                    </template>
                  </div>
                </div>
              </div>
              <ul class="detail-menus" v-show="orderShow">
                <li class="levetwo" v-if="orderInfo.list">
                  <div class="submenu" @click="notificationDelivery = !notificationDelivery">
                    <span class="order-remind">{{ $t("topNav.dfhtz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="orderInfo.list[0].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click="noticeReads(1)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="orderInfo.list[0].total != 0">
                          <span>（{{ orderInfo.list[0].total }}）</span>
                          <i :class="[
                              notificationDelivery
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="orderInfo.list[0]" v-show="notificationDelivery">
                    <li v-for="(item, index) in orderInfo.list[0].list" :key="index" @click="
                        goOrderList(
                          item.parameter,
                          item.id,
                          item.toUrl,
                          item.self_lifting
                        )
                      ">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.ddxx") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>
                <li class="levetwo" v-if="orderInfo.list">
                  <div class="submenu" @click="afterSales = !afterSales">
                    <span class="order-remind">{{ $t("topNav.shdcltz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="orderInfo.list[1].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click.stop="noticeReads(2)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="orderInfo.list[1].total != 0">
                          <span>（{{ orderInfo.list[1].total }}）</span>
                          <i :class="[
                              afterSales
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="orderInfo.list[1]" v-show="afterSales">
                    <li v-for="(item, index) in orderInfo.list[1].list" :key="index" @click="
                        salesReturnDetails(item.id, item.parameter, item.toUrl)
                      ">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.ddxx") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>
                <li class="levetwo" v-if="orderInfo.list">
                  <div class="submenu" @click="shipmentRemind = !shipmentRemind">
                    <span class="order-remind">{{ $t("topNav.fhtxtz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="orderInfo.list[2].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click.stop="noticeReads(3)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="orderInfo.list[2].total != 0">
                          <span>（{{ orderInfo.list[2].total }}）</span>
                          <i :class="[
                              shipmentRemind
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="orderInfo.list[2]" v-show="shipmentRemind">
                    <li v-for="(item, index) in orderInfo.list[2].list" :key="index" @click="
                        goOrderList(
                          item.parameter,
                          item.id,
                          item.toUrl,
                          item.self_lifting
                        )
                      ">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.ddxx") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>
                <li class="levetwo" v-if="orderInfo.list">
                  <div class="submenu" @click="orderDown = !orderDown">
                    <span class="order-remind">{{ $t("topNav.ddgbtz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="orderInfo.list[3].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click.stop="noticeReads(4)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="orderInfo.list[3].total != 0">
                          <span>（{{ orderInfo.list[3].total }}）</span>
                          <i :class="[
                              orderDown
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="orderInfo.list[3]" v-show="orderDown">
                    <li v-for="(item, index) in orderInfo.list[3].list" :key="index" @click="
                        goOrderList(
                          item.parameter,
                          item.id,
                          item.toUrl,
                          item.self_lifting
                        )
                      ">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.ddxx") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>
                <li class="levetwo" v-if="orderInfo.list">
                  <div class="submenu" @click="newOrder = !newOrder">
                    <span class="order-remind">{{ $t("topNav.xddtz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="orderInfo.list[4].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click.stop="noticeReads(5)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="orderInfo.list[4].total != 0">
                          <span>（{{ orderInfo.list[4].total }}）</span>
                          <i :class="[
                              newOrder
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="orderInfo.list[4]" v-show="newOrder">
                    <li v-for="(item, index) in orderInfo.list[4].list" :key="index" @click="
                        goOrderList(
                          item.parameter,
                          item.id,
                          item.toUrl,
                          item.self_lifting
                        )
                      ">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.ddxx") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>
                <li class="levetwo" v-if="orderInfo.list">
                  <div class="submenu" @click="orderGoods = !orderGoods">
                    <span class="order-remind">{{ $t("topNav.ddshtz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="orderInfo.list[5].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click.stop="noticeReads(6)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="orderInfo.list[5].total != 0">
                          <span>（{{ orderInfo.list[5].total }}）</span>
                          <i :class="[
                              orderGoods
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="orderInfo.list[5]" v-show="orderGoods">
                    <li v-for="(item, index) in orderInfo.list[5].list" :key="index" @click="
                        goOrderList(
                          item.parameter,
                          item.id,
                          item.toUrl,
                          item.self_lifting
                        )
                      ">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.ddxx") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>
              </ul>
            </li>
            <li class="leveone">
              <div class="topmenus" @click="goodsShow = !goodsShow">
                <span class="goods-remind">{{ $t("topNav.sptx") }}</span>
                <div class="read">
                  <div class="operation">
                    <div class="operation-read" v-if="goodsInfo.total">
                      <i class="laiketui laiketui-biaojiweiyidu"></i>
                      <span @click.stop="noticeReads('7,9')">{{
                        $t("topNav.yjyd")
                      }}</span>
                    </div>
                    <!-- <el-badge :value="goodsInfo.total" :max="99" class="item" v-if="goodsInfo.total!==0">
                    </el-badge> -->
                    <template v-if="goodsInfo.total!==0">
                      <span>（{{ goodsInfo.total }}）</span>
                      <i :class="[
                          goodsShow
                            ? 'el-icon-arrow-down'
                            : 'el-icon-arrow-right',
                        ]"></i>
                    </template>
                  </div>
                </div>
              </div>
              <ul class="detail-menus" v-show="goodsShow">
                <li class="levetwo" v-if="goodsInfo.list">
                  <div class="submenu" @click="goodsAudit = !goodsAudit">
                    <span class="order-remind">{{ $t("topNav.spshtz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="goodsInfo.list[0].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click.stop="noticeReads(7)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="goodsInfo.list[0].total != 0">
                          <span>（{{ goodsInfo.list[0].total }}）</span>
                          <i :class="[
                              goodsAudit
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="goodsInfo.list[0]" v-show="goodsAudit">
                    <li v-for="(item, index) in goodsInfo.list[0].list" :key="index" @click="
                        goGoodsAudit(item.content, item.id, item.parameter,item.toUrl)
                      ">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.spxx") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>
                <li class="levetwo" v-if="goodsInfo.list">
                  <div class="submenu" @click="goodsReplenishment = !goodsReplenishment">
                    <span class="order-remind">{{ $t("topNav.spbhtz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="goodsInfo.list[1].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click.stop="noticeReads(9)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="goodsInfo.list[1].total != 0">
                          <span>（{{ goodsInfo.list[1].total }}）</span>
                          <i :class="[
                              goodsReplenishment
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="goodsInfo.list[1]" v-show="goodsReplenishment">
                    <li v-for="(item, index) in goodsInfo.list[1].list" :key="index"
                      @click="goInventoryList(item.content, item.id)">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.spxx") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>
              </ul>
            </li>
            <!-- 新增账单提醒 -->
            <li class="leveone">
              <div class="topmenus" @click="billsShow = !billsShow">
                <span class="goods-remind">{{ $t("topNav.zdtx") }}</span>
                <div class="read">
                  <div class="operation">
                    <div class="operation-read" v-if="billsInfo.total">
                      <i class="laiketui laiketui-biaojiweiyidu"></i>
                      <span @click.stop="noticeReads('19,20,21')">{{
                        $t("topNav.yjyd")
                      }}</span>
                    </div>
                    <!-- <el-badge :value="billsInfo.total" :max="99" class="item" v-if="billsInfo.total!==0">
                    </el-badge> -->
                    <template v-if="billsInfo.total != 0">
                      <span>（{{ billsInfo.total }}）</span>
                      <i :class="[
                          billsShow
                            ? 'el-icon-arrow-down'
                            : 'el-icon-arrow-right',
                        ]"></i>
                    </template>
                  </div>
                </div>
              </div>
              <ul class="detail-menus" v-show="billsShow">
                <li class="levetwo" v-if="billsInfo.list">
                  <div class="submenu" @click="billsAudit = !billsAudit">
                    <span class="order-remind">{{ $t("topNav.yhshtz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="billsInfo.list[0].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click.stop="noticeReads(19)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="billsInfo.list[0].total != 0">
                          <span>（{{ billsInfo.list[0].total }}）</span>
                          <i :class="[
                              billsAudit
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="billsInfo.list[0]" v-show="billsAudit">
                    <li v-for="(item, index) in billsInfo.list[0].list" :key="index" @click="
                        goBillAduit(item.content, item.id, item.parameter,item.toUrl)
                      ">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.yhtxsh") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>

                <li class="levetwo" v-if="billsInfo.list">
                  <div class="submenu" @click="billsReplenishment = !billsReplenishment">
                    <span class="order-remind">{{ $t("topNav.dpshtz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="billsInfo.list[1].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click.stop="noticeReads(20)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="billsInfo.list[1].total != 0">
                          <span>（{{ billsInfo.list[1].total }}）</span>
                          <i :class="[
                              billsReplenishment
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="billsInfo.list[1]" v-show="billsReplenishment">
                    <li v-for="(item, index) in billsInfo.list[1].list" :key="index"
                      @click="goBillAduit(item.content, item.id, item.parameter,item.toUrl)">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.dptxsh") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>

                <li class="levetwo" v-if="billsInfo.list">
                  <div class="submenu" @click="billsThree = !billsThree">
                    <span class="order-remind">{{ $t("topNav.gysshtz") }}</span>
                    <div class="read">
                      <div class="operation">
                        <div class="operation-read" v-if="billsInfo.list[2].total">
                          <i class="laiketui laiketui-biaojiweiyidu"></i>
                          <span @click.stop="noticeReads(21)">{{
                            $t("topNav.yjyd")
                          }}</span>
                        </div>
                        <template v-if="billsInfo.list[2].total != 0">
                          <span>（{{ billsInfo.list[2].total }}）</span>
                          <i :class="[
                              billsThree
                                ? 'el-icon-arrow-down'
                                : 'el-icon-arrow-right',
                            ]"></i>
                        </template>
                      </div>
                    </div>
                  </div>
                  <ul class="order-details" v-if="billsInfo.list[2]" v-show="billsThree">
                    <li v-for="(item, index) in billsInfo.list[2].list" :key="index"
                      @click=" goBillAduit(item.content, item.id, item.parameter,item.toUrl)">
                      <div class="left-icon">
                        <img src="@/assets/imgs/logo2.png" alt="" />
                      </div>
                      <div class="right-info">
                        <div class="top-date">
                          <span class="orderInfo">{{ $t("topNav.gystxsh") }}</span>
                          <span class="date">{{ item.add_date }}</span>
                        </div>
                        <div class="bottom-detail">
                          <p>{{ item.content }}</p>
                          <a href="javascript:;">{{ $t("topNav.see") }}</a>
                        </div>
                      </div>
                    </li>
                  </ul>
                </li>
              </ul>
            </li>
          </ul>
        </li>
        <li class="Hui-skin">
          <div class="topHui-skin">
            <img class="pif" src="@/assets/imgs/huanfu.png" alt="" />
          </div>
          <ul class="Huiskin-menus">
            <li ref="lis" v-for="(item, index) in skinList" :key="index" @click="changeTag(index)"
              @mouseenter="mouseenter(item)" @mouseleave="mouseleave(item)" :style="{
                'border-radius': '2px',
                background: index == tag ? cNodeColor : '#fff',
                color: index == tag ? '#fff' : '#515a6e',
              }">
              <span>{{ $t(item.name) }}</span>
              <i v-show="item.color == cNodeColor" class="el-icon-check"></i>
            </li>
          </ul>
        </li>
        <!-- 暂时注释 -->
        <li class="Lang">
          <div class="top-lang">
            <img src="@/assets/imgs/lang.png" alt="" />
          </div>
          <ul class="lang-menus">
            <li v-for="(item,index) in languages" :key="index" :class="{ 'lang_active': trcLang === item.lang_code }" @click="toggleZh(item.lang_code)">
              <span>{{item.lang_name}}</span>
            </li>
          </ul>
        </li>
        <li class="sign-out" @click="signOut" v-bind:title="$t('topNav.tcxt')">
          <img src="@/assets/imgs/tc.png" alt="" />
        </li>
      </ul>


      <div class="box" v-if="!isShowFalg">
        <div style="position:fixed; top:20%; left:50%; transform:translateX(-50%); background:#fff; padding:30px 50px; border-radius:12px; box-shadow:0 4px 20px rgba(0,0,0,0.1); z-index:9999; width: 400px; text-align:center;">
        <!-- 提示框标题 -->
        <h3 style="font-size:20px; color:#f44336; margin-bottom:20px;">配置未完成 </h3>
        <!-- 提示内容 -->
        <p style="font-size:14px; color:#666; margin-bottom:20px;">为了尽快开始使用系统，请先进行快速配置。</p>
        <!-- 按钮区域 -->
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <!-- 取消按钮 -->
          <button @click="isShowFalg = true" style="background-color:#ccc; color:#fff; padding:10px 20px; border-radius:8px; border:none; cursor:pointer; font-size:14px;">暂不配置</button>
          <!-- 开始配置按钮 -->
          <a href="#" @click.stop="toUrl" style="background-color:#2d6dcc; color:#fff; padding:10px 30px; border-radius:8px; text-decoration:none; font-size:16px; cursor:pointer;">开始快捷配置</a>
        </div>
      </div>
    </div>

    </div>

    <div class="dialog-block">
      <!-- 修改密码 弹框组件 -->
      <el-dialog :title="$t('topNav.xgmm')" :visible.sync="dialogVisible" :before-close="handleClose"
        :modal-append-to-body="false">
        <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="auto" class="demo-ruleForm">
          <div class="pass-input">
            <el-form-item :label="$t('topNav.ymm')" prop="oldPassword">
              <el-input v-model="ruleForm.oldPassword" :type="flagType">
                <div slot="suffix" style="width: 31px;height: 100%;display: flex;justify-content: center;align-items: center;">
                  <img :src="!this.flag?hideIcon:showIcon" alt="" srcset="" style="width: 18px;height: 18px;" @click="getFlag()">
                </div>
              </el-input>
            </el-form-item>
            <el-form-item :label="$t('topNav.xmm')" prop="newPassword">
              <el-input v-model="ruleForm.newPassword" :type="flagType2">
                <div slot="suffix" style="width: 31px;height: 100%;display: flex;justify-content: center;align-items: center;">
                  <img :src="!this.flag2?hideIcon:showIcon" alt="" srcset="" style="width: 18px;height: 18px;" @click="getFlag2()">
                </div>
              </el-input>
            </el-form-item>
            <el-form-item :label="$t('topNav.qrmm')" prop="confirmPassword" style="margin-bottom: 0">
              <el-input v-model="ruleForm.confirmPassword" :type="flagType3">
                <div slot="suffix" style="width: 31px;height: 100%;display: flex;justify-content: center;align-items: center;">
                  <img :src="!this.flag3?hideIcon:showIcon" alt="" srcset="" style="width: 18px;height: 18px;" @click="getFlag3()">
                </div>
              </el-input>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <!-- 取消 -->
              <el-button @click="dialogVisible = false" class="qxcolor">{{
                $t("topNav.cancel")
              }}</el-button>
              <!-- 确认 -->
              <el-button type="primary" @click="determine('ruleForm')" class="qdcolor">{{ $t("topNav.ok") }}</el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>

    <div class="dialog-info">
      <!-- 基本信息 弹框组件 -->
      <el-dialog :title="$t('topNav.jbxx')" :visible.sync="dialogVisible2" :before-close="handleClose2"
        :modal-append-to-body="false">
        <el-form :model="ruleForm" ref="ruleForm2" label-width="auto" class="demo-ruleForm">
          <div class="pass-input">
            <el-form-item class="upload-headimg" :label="$t('topNav.tx')">
              <el-upload :class="
                  ruleForm2.headImg == ''
                    ? 'avatar-uploader'
                    : 'avatar-uploader-two'
                " :action="actionUrl" :data="uploadData" :show-file-list="false" :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload">
                <img v-if="ruleForm2.headImg" :src="ruleForm2.headImg" class="avatar" />
                <!-- <img :src="removeImg" @click.stop.prevent="removeImgs" v-if="ruleForm2.headImg" class="removeImg" alt=""> -->
                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                <div class="city-list" @click.stop.prevent="removeImgs">
                  <div class="avatar-flex">
                    <i class="el-icon-delete"></i>
                  </div>
                </div>
              </el-upload>
              <!-- <l-upload :limit="1" v-model="ruleForm2.headImg" text=" "></l-upload> -->
            </el-form-item>
            <el-form-item :label="$t('topNav.nc')">
              <el-input v-model="ruleForm2.nickname" :placeholder="$t('topNav.qsrnc')"></el-input>
            </el-form-item>
            <el-form-item :label="$t('topNav.sr')">
              <el-date-picker :append-to-body="false" type="date" :picker-options="pickerOptions" v-model="ruleForm2.birthday"
                value-format="yyyy-MM-dd" :placeholder="$t('topNav.xzrq')" style="width: 100%">
              </el-date-picker>
            </el-form-item>
            <el-form-item :label="$t('topNav.xb')">
              <el-radio-group v-model="ruleForm2.gender">
                <el-radio v-for="item in genderList" :label="item.value" :key="item.value">{{$t(item.name)}}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('topNav.sjhm')" style="margin-bottom: 0">
                <div style='display:flex;width:100%' class='phone'>
                  <el-autocomplete
                   class='custom-autocomplete'
                    v-model="cpc"
                    :popper-append-to-body="false"
                    :fetch-suggestions="querySearchAsync"
                    :placeholder="$t('membersLists.qh')"
                    @select="handleSelect"
                  >
                   <template slot-scope="{ item }">
                    <div>
                      <span>{{ item.name }}</span>
                      <span style="color: #999; margin-left: 10px;">+({{ item.code2 }})</span>
                    </div>
                  </template>
                  </el-autocomplete>
                  <el-input style="margin-left: 12px;" v-model="ruleForm2.phone"
                    v-on:input="ruleForm2.phone=ruleForm2.phone.replace(/^(-1+)|[^\d]+/g,'')"
                    :placeholder="$t('topNav.srsjhmts')"></el-input>
               </div>
            </el-form-item>
          </div>
          <div class="form-footer">
            <el-form-item>
              <el-button @click="dialogVisible2 = false" class="qxcolor">{{
                $t("topNav.cancel")
              }}</el-button>
              <el-button type="primary" @click="determine2('ruleForm2')"
                class="qdcolor">{{ $t("topNav.ok") }}</el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>

  </div>
</template>

<script>
  import {
    mapGetters
  } from "vuex";
  import Hamburger from "@/components/Hamburger";
  import {
    getStorage,
    setStorage,
    removeStorage
  } from "@/utils/storage";
  import {
    getShopInfo,
    updateColour
  } from "@/api/Platform/merchants";
  import {
    getRoleMenu
  } from "@/api/Platform/permissions";
  import ErrorImg from '@/assets/images/default_picture.png'
  import Cookies from 'js-cookie'
  import {
    updateAdminInfo,
    noticeList,
    noticeRead,
    loginOut,
    noticePopup,
  } from "@/api/layout/information";
  import Config from "@/packages/apis/Config";
  import {
    setUserAdmin
  } from "@/api/Platform/merchants";

  import { updateLanguage } from '@/lang/index'
  import { getItuList } from '@/api/members/membersSet'
  export default {
    components: {
      Hamburger,
    },
    data() {
      // 只能输入数字和字母
      var validatePass3 = (rule, value, callback) => {
        console.log('8148148148', /^[\da-z]+$/i.test(value));
        if (/^[\da-z]+$/i.test(value)) {
          callback();
        } else {
          callback(new Error("只能输入数字和字母"));
        }
      };
      var validatePass = (rule, value, callback) => {
        if (value === "") {
          callback(new Error("请输入密码"));
        } else {
          if (this.ruleForm.confirmPassword !== "") {
            this.$refs.ruleForm.validateField("confirmPassword");
          }
          callback();
        }
      };
      var validatePass2 = (rule, value, callback) => {

        if (value === "") {
          callback(new Error("请再次输入密码"));
        } else if (value !== this.ruleForm.newPassword) {
          callback(new Error("两次输入密码不一致!"));
        } else {
          callback();
        }
      };
      return {
        isShowFalg:true,
        cpc: '', //国家区号
        restaurants: [], //异步查询建议列表
        isDomestic: true, // 是否国内
        //商城支持的语种
        languages:[],
        //当前选中的语种
        trcLang: sessionStorage.getItem('trcLang' + getStorage("laike_admin_userInfo").storeId ) || null,
        merchants_Logo: getStorage("laike_head_img") ?
          getStorage("laike_head_img") : "",
        skinList: [{
            name: "topNav.Blue",
            icon: 0,
            color: "#2d6dcc",
            key: 0
          },
          {
            name: "topNav.Black",
            icon: 1,
            color: "#222222",
            key: 1
          },
          {
            name: "topNav.Green",
            icon: 1,
            color: "#4cbe8b",
            key: 2
          },
          {
            name: "topNav.Red",
            icon: 1,
            color: "#f45d5d",
            key: 3
          },
          {
            name: "topNav.Yellow",
            icon: 1,
            color: "#f2a61f",
            key: 4
          },
          {
            name: "topNav.Orange",
            icon: 1,
            color: "#f87235",
            key: 5
          },
        ],
        cNodeColor: "#2d6dcc",

        message: "退出系统",

        versionList: [],
        defaultVersion: "",
        showStoreId: "",

        // 弹框数据
        dialogVisible: false,
        ruleForm: {
          oldPassword: "",
          newPassword: "",
          confirmPassword: "",
        },
        rules: {
          oldPassword: [{
              required: true,
              message: "请填写原密码",
              trigger: "blur"
            },
            {
              min: 6,
              max: 16,
              message: "长度在 6 到 16 个字符",
              trigger: "blur",
            },
            {
              validator: validatePass3,
              trigger: "change"
            }
          ],
          newPassword: [{
              required: true,
              message: "请填写密码",
              trigger: "blur"
            },
            {
              min: 6,
              max: 16,
              message: "长度在 6 到 16 个字符",
              trigger: "blur",
            },
            {
              validator: validatePass,
              trigger: "blur"
            },
            {
              validator: validatePass3,
              trigger: "change"
            }
          ],
          confirmPassword: [{
              required: true,
              message: "请确认密码",
              trigger: "blur"
            },
            {
              min: 6,
              max: 16,
              message: "长度在 6 到 16 个字符",
              trigger: "blur",
            },
            {
              validator: validatePass2,
              trigger: "blur",
              required: true
            },
            {
              validator: validatePass3,
              trigger: "change"
            }
          ],
        },

        // 基本信息弹框数据
        dialogVisible2: false,
        ruleForm2: {
          headImg: "",
          nickname: "",
          birthday: "",
          gender: "",
          phone: "",
        },

        genderList: [{
            value: 1,
            name: "topNav.sexX",
          },
          {
            value: 0,
            name: "topNav.sexO",
          },
        ],

        actionUrl: Config.baseUrl,
        removeImg: require("../../assets/imgs/sha.png"),
        showIcon: require("../../assets/imgs/psd_show.png"),
        hideIcon: require("../../assets/imgs/psd_hide.png"),
        // 订单通知数据
        allNum: 0,
        orderInfo: {},
        goodsInfo: {},
        // 账单
        billsInfo:{},
        billsTota:"",
        orderShow: false,
        notificationDelivery: false,
        afterSales: false,
        shipmentRemind: false,
        orderDown: false,
        newOrder: false,
        orderGoods: false,

        goodsShow: false,
        goodsAudit: false,
        goodsReplenishment: false,

        billsShow:false,
        billsAudit:false,
        billsReplenishment:false,
        billsThree:false,

        pickerOptions: {
          disabledDate(time) {
            // 当天不可选 8.64e7一天的闰秒
            // return time.getTime() > Date.now() - 8.64e7;
            // 当天可选
            return time.getTime() > Date.now();
          },
        },

        flag: false,
        flagType: "password",

        flag2: false,
        flagType2: "password",

        flag3: false,
        flagType3: "password",
      };
    },
    computed: {
      ...mapGetters(["sidebar"]),

      tag() {
        return this.$store.state.skinPeeler.tag;
      },

      username() {
        return getStorage("laike_admin_userInfo").nickname;
      },

      type() {
        return getStorage("laike_admin_userInfo").type;
      },

      uploadData() {
        {
          return {
            api: "resources.file.uploadFiles",
            storeId: getStorage("laike_admin_userInfo").storeId,
            groupId: -1,
            uploadType: 2,
            accessId: this.$store.getters.token,
          };
        }
      },
    },
    watch: {
      tag() {
        if (this.tag == 0) {
          this.$refs.navHeader.style.backgroundColor = "#2d6dcc";
          this.cNodeColor = "#2d6dcc";
        } else if (this.tag == 1) {
          this.$refs.navHeader.style.backgroundColor = "#222222";
          this.cNodeColor = "#222222";
        } else if (this.tag == 2) {
          this.$refs.navHeader.style.backgroundColor = "#4cbe8b";
          this.cNodeColor = "#4cbe8b";
        } else if (this.tag == 3) {
          this.$refs.navHeader.style.backgroundColor = "#f45d5d";
          this.cNodeColor = "#f45d5d";
        } else if (this.tag == 4) {
          this.$refs.navHeader.style.backgroundColor = "#f2a61f";
          this.cNodeColor = "#f2a61f";
        } else {
          this.$refs.navHeader.style.backgroundColor = "#f87235";
          this.cNodeColor = "#f87235";
        }
      },
    },

    created() {
      this.getLanguage();
      this.queryAdd()
      this.getShopInfos().then(() => {
          this.showStoreId = getStorage("laike_admin_userInfo").storeId;
          for (let i = 0; i < this.versionList.length; i++) {
            if (this.showStoreId === this.versionList[i].id) {
              this.defaultVersion = this.versionList[i].name;
              break;
            }
          }
        }),
        this.noticeList();
    },

    mounted() {
      let me = this;
      let adminUser = getStorage('laike_admin_userInfo');
      if(adminUser && adminUser.color){
        let color = JSON.parse(adminUser.color)
        if (color) {
          this.$store.dispatch("toggleTag", color.key);
          this.$refs.navHeader.style.background = this.cNodeColor;
        }
      }

      setInterval(() => {
        if (!getStorage("tag")) {
          me.noticeList();
        }
      }, 6000);
      const haveStoreMchId = sessionStorage.getItem('haveStoreMchId')
      if(typeof haveStoreMchId == 'string'){

        this.isShowFalg = haveStoreMchId == 'true' ? true : false
        console.log('this.isShowFalg', this.isShowFalg)
        if(!this.isShowFalg){
          sessionStorage.removeItem('haveStoreMchId')
        }
      }
    },

    methods: {
      toUrl(){
        this.isShowFalg = true
        this.$router.push({ path: '/mall/fastBoot/index' })
      },
      // 异步查询建议列表的方法
      querySearchAsync(queryString, cb) {
        // 模拟异步请求
        setTimeout(() => {
          const results = queryString
            ? this.restaurants.filter(this.createFilter(queryString))
            : this.restaurants;
          // 调用回调函数，将查询结果传递给组件
          cb(results);
        }, 300);
      },
      createFilter(queryString) {
        return (country) => {
          const lowerCaseQuery = queryString.toLowerCase();
          return (
            country.name.toLowerCase().indexOf(lowerCaseQuery) > -1 ||
            country.code2.toLowerCase().indexOf(lowerCaseQuery) > -1 ||
            country.zh_name.toLowerCase().indexOf(lowerCaseQuery) > -1
          );
        };
      },
      // 选择建议项时触发的方法
      handleSelect(item) {
        console.log('选中的项:', item);
        this.state = item.code2; // 可以根据需求更新输入框显示的值
        this.cpc = item.code2;
        if (item.code2 == '86' || item.code2 == '852' || item.code2 == '853') {
          this.isDomestic = true;
        }else {
          this.isDomestic = false;
        }
      },
      queryAdd() {
        const data = {
          api: 'admin.user.getItuList',
          keyword: this.keyword
        }
        getItuList(data).then(res => {
          if (res.data.code == 200) {
            this.restaurants = res.data.data
            console.log('this.restaurants', this.restaurants)
            sessionStorage.setItem('restaurants', JSON.stringify(this.restaurants))
          }
        })
      },
      async getLanguage() {
        try {
          const result = await this.LaiKeCommon.getLanguages();
          this.languages = result.data.data;
        } catch (error) {
          console.error('获取语种列表失败:', error);
        }
      },

      // 图片错误处理
      handleErrorImg(e) {
        console.log('图片报错了', e.target.src);
        e.target.src = ErrorImg
      },
      //小眼睛切换状态
      getFlag() {
        this.flag = !this.flag;
        this.flagType = this.flag ? "text" : "password"; //text为显示密码；password为隐藏密码
      },
      //小眼睛切换状态
      getFlag2() {
        this.flag2 = !this.flag2;
        this.flagType2 = this.flag2 ? "text" : "password"; //text为显示密码；password为隐藏密码
      },
      //小眼睛切换状态
      getFlag3() {
        this.flag3 = !this.flag3;
        this.flagType3 = this.flag3 ? "text" : "password"; //text为显示密码；password为隐藏密码
      },
      async getShopInfos() {
        let laike_admin_userInfo = getStorage("laike_admin_userInfo");

        const res = await getShopInfo({
          api: "saas.shop.getShopInfo",
          storeType: 8,
          pageNo: 1,
          pageSize: 99,
          storeId: null,
        });
        this.versionList = res.data.data.dataList;

        if(laike_admin_userInfo)
        {
          let showStoreId = laike_admin_userInfo.storeId;
          let name = laike_admin_userInfo.store_name;
          if(!name){
            name = res.data.data.dataList[0].name;
          }
          this.showStoreId = showStoreId;
          this.defaultVersion = name;
        }
        else
        {
          this.showStoreId = res.data.data.dataList[0].id;
          this.defaultVersion = res.data.data.dataList[0].name;
        }
      },

      //切换商城 切换商城
      async enterSystem(value) {
        console.log(value);
        this.showStoreId = value.id;
        this.defaultVersion = value.name;


        let info = null;
        const res = await getShopInfo({ api: "saas.shop.getShopInfo", storeType: 8, storeId: value.id });
        info = res.data.data.dataList[0];

        let laike_admin_userInfo = getStorage("laike_admin_userInfo");
        let rolesInfo = getStorage("rolesInfo");

        // 更新本地存储
        laike_admin_userInfo.storeId = info.id;
        laike_admin_userInfo.storeDefaultCurrencyInfo = info.storeDefaultCurrencyInfo;
        laike_admin_userInfo.cpc = info.cpc;
        laike_admin_userInfo.lang = info.default_lang_code;
        laike_admin_userInfo.phone = info.mobile;
        laike_admin_userInfo.portrait = info.portrait;
        laike_admin_userInfo.role = info.roleId;
        laike_admin_userInfo.store_name = info.name;

        rolesInfo.storeId = info.id;
        rolesInfo.role = info.roleId;

        setStorage("laike_admin_userInfo", laike_admin_userInfo);
        setStorage("rolesInfo", rolesInfo);

        // 设置币种和语言
        const currency_symbol = info.storeDefaultCurrencyInfo?.currency_symbol || "￥";
        sessionStorage.setItem(`currency_symbol_${info.id}`, currency_symbol);
        updateLanguage(info.default_lang_code || "zh_CN", info.id);

        // 网站信息
        const website_information = {
          contact_address: info.contact_address,
          contact_number: info.contact_number,
          copyright_information: info.copyright_information,
          record_information: info.record_information,
          official_website: info.official_website
        };
        setStorage("website_information", website_information);

        setStorage("laike_head_img", info.merchant_logo);
        this.$store.commit("user/SET_MERCHANTSLOGO", info.merchant_logo);

        const userAdminRes = await setUserAdmin({ api: "admin.saas.user.setUserAdmin", storeId: info.id });
        laike_admin_userInfo.mchId = userAdminRes.data.data.mchId;
        setStorage("laike_admin_userInfo", laike_admin_userInfo);

        await getRoleMenu({ api: "saas.role.getRoleMenu", storeId: info.id, roleId: parseInt(info.roleId) });
        // 刷新页面
        this.$router.go(0);
      },

      // 订单通知
      async noticeList() {
        const res = await noticeList({
          api: "admin.notice.noticeList",
        });

        let orderInfo = res.data.data.list;
        this.allNum = orderInfo[0].total + orderInfo[1].total+orderInfo[2].total;
        this.ordertotal = orderInfo[0].total;
        this.goodstotal = orderInfo[1].total;
        this.orderInfo = orderInfo[0];
        this.goodsInfo = orderInfo[1];
        this.billsInfo = orderInfo[2]
        const idList = [];
        this.orderInfo.list.forEach((item) => {
          if (item.list.length) {
            item.list.forEach((items, index) => {
              if (items.is_popup === "0") {
                this.$notify({
                  title: "消息",
                  message: items.content,
                  type: "success",
                  offset: 100,
                });
                idList.push(items.id);
              }
            });
          }
        });
        if((!this.orderInfo && !orderInfo.list)||this.orderInfo.list.length == 0){
          this.orderInfo.list = null;
        }
        this.goodsInfo.list.forEach((item) => {
          if (item.list.length) {
            item.list.forEach((items, index) => {
              if (items.is_popup === "0") {
                this.$notify({
                  title: "消息",
                  message: items.content,
                  type: "success",
                  offset: (index + 1) * 100,
                });
                idList.push(items.id);
              }
            });
          }
        });
        if((!this.goodsInfo && !goodsInfo.list)||this.goodsInfo.list.length == 0){
          this.goodsInfo.list = null;
        }
        this.billsInfo.list.forEach((item) => {
          if (item.list.length) {
            item.list.forEach((items, index) => {
              if (items.is_popup === "0") {
                this.$notify({
                  title: "消息",
                  message: items.content,
                  type: "success",
                  offset: (index + 1) * 100,
                });
                idList.push(items.id);
              }
            });
          }
        });
        if((!this.billsInfo && !billsInfo.list)||this.billsInfo.list.length == 0){
          this.billsInfo.list = null;
        }
        if (idList.length) {
          noticePopup({
            api: "admin.notice.noticePopup",
            ids: idList.join(","),
          }).then((res) => {});
        }
      },

      async noticeReads(type) {
        this.$confirm("确定将该类型通知标记为已读?", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          })
          .then(() => {
            noticeRead({
              api: "admin.notice.noticeRead",
              types: type,
            }).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  type: "success",
                  message: this.$t('zdata.czcg'),
                  offset: 102,
                });
                this.noticeList();
              }
            });
          })
          .catch(() => {
            this.$message({
              type: "info",
              message: "已取消",
              offset: 100,

            });
          });
      },

      goOrderList(value, id, url, self_lifting) {
        noticeRead({
          api: "admin.notice.noticeRead",
          id: id,
        }).then((res) => {
          if (res.data.code == "200") {
            this.noticeList()
          }
        });
        this.$router.push({
          path: url,
          query: {
            no: value,
            self_lifting: self_lifting,
          },
        });
      },

      salesReturnDetails(id, value, toUrl) {
        noticeRead({
          api: "admin.notice.noticeRead",
          id: id,
        }).then((res) => {
          if (res.data.code == "200") {
            this.noticeList()
          }
        });
        if (toUrl) {
          this.$router.push({
            path: toUrl,
            query: {
              id: value,
            },
          });
        }
      },

      goGoodsAudit(value, id, goodsId, toUrl) {
        noticeRead({
          api: "admin.notice.noticeRead",
          id: id,
        }).then((res) => {
          if (res.data.code == "200") {
            this.noticeList()
          }
        });
        this.$router.push({
          // path: "/plug_ins/stores/goodsAudit",
          path: toUrl,
          query: {
            goodsId: goodsId,
          },
        });
      },

      goInventoryList(value, id) {
        noticeRead({
          api: "admin.notice.noticeRead",
          id: id,
        }).then((res) => {
          if (res.data.code == "200") {
            this.noticeList()
          }
        });
        this.$router.push({
          path: "/goods/inventoryManagement/inventoryList",
          query: {
            name: value,
          },
        });
      },

      // 账单提醒的跳转
      goBillAduit(value,id,billId,tourl){
        noticeRead({
          api: "admin.notice.noticeRead",
          id: id,
        }).then((res) => {
          if (res.data.code == "200") {
            this.noticeList()
          }
        });
        this.$router.push({
          // path: "/plug_ins/stores/goodsAudit",
          path: tourl,
          query: {
            billId: billId,
          },
        });
      },
      orderAllRead() {
        this.ordertotle = 0;
      },

      toggleSideBar() {
        this.$store.dispatch("app/toggleSideBar");
      },

      changeTag(index) {
        this.$store.dispatch("toggleTag", index);
        updateColour({
          api: "admin.saas.user.updateColor",
          color: this.skinList[index],
          name: getStorage('laike_admin_userInfo').name,
        }).then(() => {
          setStorage("laike_admin_userInfo", {
            ...getStorage('laike_admin_userInfo'),
            color: JSON.stringify(this.skinList[index]),
          });
        });

        this.$refs.navHeader.style.background = this.cNodeColor;
      },

      mouseenter(item) {
        this.$refs.lis[item.key].style.background = item.color;
        this.$refs.lis[item.key].style.color = "#fff";
      },

      mouseleave(item) {
        if (this.cNodeColor !== item.color) {
          this.$refs.lis[item.key].style.background = "#fff";
          this.$refs.lis[item.key].style.color = "#515a6e";
        }
      },

      toggleZh(lang) {
        this.trcLang = lang;
        this.$i18n.locale = lang;
        const rolesInfo = getStorage("rolesInfo");
        let storeId = rolesInfo.storeId;

        updateLanguage(lang,storeId);

        noticeRead({
          api: "admin.saas.user.select_language",
          language: lang,
        }).then((res) => {
          if (res.data.code == "200") {
            // this.noticeList()
            getRoleMenu({
              api: "saas.role.getRoleMenu",
              storeId: rolesInfo.storeId,
              roleId: parseInt(rolesInfo.role),
            })
              .then((res) => {
                this.$router.go(0);
              })
              .catch((error) => {
                reject(error);
              });
          }
        });
        removeStorage('background_color')
        // let color = JSON.parse(getStorage('background_color'))
        // console.log('131413141314',color);
        // if(color!==null){
        //    this.changeTag(color.key)
        //    this.$refs.navHeader.style.background = color.color
        //    console.log('131413141314',this.$refs.navHeader.style.background,color.color);
        // }
      },

      // toggleEn(lang) {
      //   this.$i18n.locale = lang
      //   this.$store.dispatch('lang/setLanguage', lang)
      // },

      // 退出登录
      signOut() {
        loginOut({
          api: "admin.saas.user.logout",
        }).then((res) => {
          let type = getStorage("rolesInfo").type;
          if (type == 0) {
            removeStorage("auctionTabel");
            window.sessionStorage.clear();
            this.$router.push("/login");
            location.reload();
          } else {
            removeStorage("auctionTabel");
            window.sessionStorage.clear();
            this.$router.push("/mallLogin");

            //  location.reload();
          }
        });
      },

      // 弹框方法
      dialogShow(value) {
        this.ruleForm.oldPassword = "";
        this.ruleForm.newPassword = "";
        this.ruleForm.confirmPassword = "";
        this.dialogVisible = true;
        this.$nextTick(() => {
          this.$refs.ruleForm.clearValidate();
        });
      },

      handleClose(done) {
        this.dialogVisible = false;
      },

      // 修改密码
      determine(formName) {
        this.$refs[formName].validate(async (valid) => {
          if (valid) {
            try {
              updateAdminInfo({
                api: "admin.saas.user.updateAdminInfo",
                passwordOld: this.ruleForm.oldPassword,
                password: this.ruleForm.newPassword,
                // storeId: null
              }).then((res) => {
                if (res.data.code == "200") {
                  this.$message({
                    message: "修改成功",
                    type: "success",
                    offset: 100,
                  });
                  //判断是总后台还是商户后台，并跳转对应登录页面
                  // 0: 系统管理员  1：客户 2:商城管理员 3:店主
                  let type = getStorage("rolesInfo").type;
                  if(type == 0){
                    removeStorage("auctionTabel");
                    window.sessionStorage.clear();
                    this.$router.push("/login");
                  } else {
                    removeStorage("auctionTabel");
                    window.sessionStorage.clear();
                    this.$router.push("/mallLogin");
                  }
                  location.reload();
                  this.dialogVisible = false;
                }
              });
            } catch (error) {
              this.$message({
                message: "密码不能为空",
                type: "error",
                offset: 100,
              });
            }
          } else {
            console.log("error submit!!");
            return false;
          }
        });
      },

      // 基本信息弹框方法
      dialogShow2(value) {
        const info = getStorage("laike_admin_userInfo");
        this.ruleForm2.headImg = info.portrait;
        this.ruleForm2.nickname = info.nickname;
        this.ruleForm2.birthday = info.birthday;
        this.ruleForm2.gender = info.sex;
        this.ruleForm2.phone = info.phone;
        this.cpc = info.cpc;
        this.dialogVisible2 = true;
      },

      handleClose2(done) {
        this.dialogVisible2 = false;
      },

      handleAvatarSuccess(res, file) {
        console.log(res,'resresres')
        if(res.code != 200){
          this.errorMsg(res.message)
          return
        }
        this.ruleForm2.headImg = res.data.imgUrls[0];
      },

      beforeAvatarUpload(file) {
        // const isJPG = file.type === 'image/jpeg';
        const isLt2M = file.size / 1024 / 1024 < 2;

        // if (!isJPG) {
        //   this.errorMsg('上传头像图片只能是 JPG 格式!');
        // }
        if (!isLt2M) {
          this.errorMsg("上传头像图片大小不能超过 2MB!");
        }
        return isLt2M;
      },

      removeImgs() {
        this.ruleForm2.headImg = "";
      },

      // 修改基本信息
      determine2(formName) {

        this.$refs[formName].validate(async (valid) => {
          if (valid) {
            try {
              if (this.ruleForm2.nickname&&this.ruleForm2.nickname.length > 20) {
                this.$message({
                  message: "昵称长度不能超过20个字符",
                  type: "error",
                  offset: 100,
                });
                return;
              }
              updateAdminInfo({
                api: "admin.saas.user.updateAdminInfo",
                portrait: this.ruleForm2.headImg,
                nickname: this.ruleForm2.nickname,
                birthday: this.ruleForm2.birthday,
                sex: this.ruleForm2.gender,
                phone: this.ruleForm2.phone,
                cpc: this.cpc,
                storeId: null,
              }).then((res) => {
                if (res.data.code == "200") {
                  const infos = getStorage("laike_admin_userInfo");
                  infos.portrait = this.ruleForm2.headImg;
                  infos.nickname = this.ruleForm2.nickname;
                  infos.birthday = this.ruleForm2.birthday;
                  infos.sex = this.ruleForm2.gender;
                  infos.phone = this.ruleForm2.phone;
                  infos.cpc = this.cpc;
                  setStorage("laike_admin_userInfo", infos);
                  // setStorage('laike_head_img',this.ruleForm2.headImg)
                  this.$message({
                    message: "修改成功",
                    type: "success",
                    offset: 100,
                  });
                  this.$router.go(0);
                  this.dialogVisible2 = false;
                }
              });
            } catch (error) {
              this.$message({
                message: error,
                type: "error",
                offset: 100,
              });
            }
          } else {
            console.log("error submit!!");
            return false;
          }
        });
      },
    },
  };
</script>

<style lang="less" scoped>
.box {
  position: fixed;
  background-color: rgba(102,102,102, 0.2);

  width: 100%;
  height: 100%;
  top: 0px;
  left: 0px;
}
  .actions {
    position: absolute;
    width: 100%;
    height: 100%;
    left: 0;
    top: 0;
    cursor: default;
    text-align: center;
    color: #fff;
    opacity: 0;
    font-size: 1.25rem;
    background-color: rgba(0, 0, 0, 0.5);
    transition: opacity 0.3s;
    display: flex;
    justify-content: space-around;
  }

  .avatar-uploader-two {
    height: 80px;
    width: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #c0ccda;
    position: relative;
  }

  .city-list {
    display: none;
    position: absolute;
    top: 0;
    left: 0;
    color: #fff;
    font-size: 19px;
    width: 100%;
    height: 100%;
    opacity: 1;
    font-size: 1.25rem;
    background-color: rgba(0, 0, 0, 0.5);
    transition: opacity 0.3s;
  }

  .avatar-uploader-two:hover {
    border: 1px solid #c0ccda;
    cursor: pointer;
  }

  .avatar-uploader-two:hover .city-list {
    display: block;
  }

  .avatar-flex {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 70px;
  }

  .navbar {
    height: 60px;
    position: relative;
    background-image: linear-gradient(to right, #0077ff, #00c0ff);
    // background-color: #2d6dcc;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    display: flex;
    justify-content: space-between;

    .left-control {
      width: 220px;
      height: 60px;
      display: flex;
      justify-content: space-between;
      align-items: center;

      img {
        margin-left: 26px;
        width: 121px;
        height: 40px;
      }

      .hamburger-container {
        height: 60px;
        line-height: 60px;
        cursor: pointer;
        transition: background 0.3s;
        -webkit-tap-highlight-color: transparent;
        padding: 0 !important;

        &:hover {
          background: rgba(0, 0, 0, 0.025);
        }
      }
    }

    .right-block {
      height: 60px;
      flex: 1;
      display: flex;
      justify-content: flex-end;
      align-items: center;
      padding-right: 40px;

      .Hui-userbar {
        list-style: none;
        padding: 0;
        margin: 0;
        display: flex;

        .dropDown {
          // width: 135px;
          height: 60px;
          line-height: 60px;
          position: relative;
          display: flex;
          justify-content: center;
          // margin-right: 21.5px;
          margin-left: 40px;

          &:hover .dropDown-menus {
            display: block;
          }

          .top-dropDown {
            cursor: pointer;
            color: #fff;
            display: flex;
            align-items: center;

            img {
              width: 32px;
              height: 32px;
              border-radius: 50%;
              margin-right: 5px;
            }
          }

          .dropDown-menus {
            list-style: none;
            padding: 0;
            margin: 0;
            position: absolute;
            left: 50%;
            transform: translateX(-50%);
            top: 60px;
            width: 184px;
            display: none;
            background-color: #fff;
            border: 1px solid #e9ecef;
            box-shadow: 0px 0px 14px 0px rgba(0, 0, 0, 0.1);
            border-radius: 4px;

            li {
              cursor: pointer;
              width: 100%;
              height: 40px;
              text-align: center;
              line-height: 40px;
              font-size: 14px;

              i {
                margin-right: 6px;
                font-size: 18px;
              }

              &:hover {
                color: #2d8cf0;
                background-color: #f6f7f8;
              }
            }
          }
        }

        .changeStore {
          width: auto;
          height: 60px;
          line-height: 60px;
          position: relative;
          display: flex;
          justify-content: center;
          // margin-right: 20px;
          margin-left: 40px;

          &:hover .changeStore-menus {
            display: block;
          }

          .top-changeStore {
            cursor: pointer;
            color: #fff;
            display: flex;
            align-items: center;

            span {
              margin: 0 6px;
            }

            .shanghu {
              width: 20px;
              height: 20px;
            }

            .qiehuan {
              width: 20px;
              height: 20px;
            }
          }

          .changeStore-menus {
            list-style: none;
            padding: 0;
            margin: 0;
            position: absolute;
            left: 50%;
            transform: translateX(-50%);
            top: 60px;
            width: 240px;
            max-height: 300px;
            overflow: hidden;
            overflow-y: auto;
            display: none;
            background-color: #fff;
            border: 1px solid #e9ecef;
            box-shadow: 0px 0px 14px 0px rgba(0, 0, 0, 0.1);
            border-radius: 4px;

            li {
              cursor: pointer;
              width: 100%;
              height: 40px;
              font-size: 14px;
              display: flex;
              justify-content: space-between;
              align-items: center;
              padding: 0 10px;

              &:hover {
                color: #2d8cf0;
                background-color: #f4f7f9;
              }
            }
          }
        }

        .headerLi {
          // width: 60px;
          height: 60px;
          line-height: 60px;
          position: relative;
          display: flex;
          justify-content: center;
          margin-left: 40px;

          &:hover .headerLi-menus {
            display: block;
          }

          .top-headerLi {
            cursor: pointer;
            color: #fff;
            display: flex;
            align-items: center;
            position: relative;

            img {
              width: 20px;
              height: 20px;
            }

            .totle-item {
              top: -3px;
              left: 6px;
              font-size: 12px;
              position: absolute;
            }
          }

          .headerLi-menus {
            list-style: none;
            padding: 0;
            margin: 0;
            position: absolute;
            right: -200px;
            top: 60px;
            width: 500px;
            display: none;
            background-color: transparent;

            .leveone {
              cursor: pointer;
              width: 100%;
              // height: 80px;
              font-size: 14px;
              display: flex;
              flex-direction: column;
              justify-content: center;
              align-items: center;
              padding: 10px 30px;
              background-color: #fff;
              margin: 10px 0;
              border-radius: 10px;
              border: 1px solid rgba(99, 61, 61, 0.1);
              box-shadow: 0px 3px 18px 0px rgba(173, 173, 173, 0.8);

              .topmenus {
                width: 100%;
                display: flex;
                justify-content: space-between;
                align-items: center;

                .order-remind,
                .goods-remind {
                  font-size: 16px;
                  font-weight: bold;
                  color: #333;
                }

                .operation {
                  display: flex;
                  justify-content: center;
                  align-items: center;

                  .operation-read {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    color: #0077ff;
                    margin-right: 10px;
                    font-size: 12px;
                  }

                  .item {
                    margin-top: 12px;
                  }
                }
              }

              .detail-menus {
                width: 100%;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;

                .levetwo {
                  width: 100%;
                  display: flex;
                  flex-direction: column;
                  justify-content: center;
                  align-items: center;

                  .submenu {
                    width: 100%;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    height: 50px;
                    border-bottom: 1px solid #f3f3f3;

                    .order-remind {
                      font-size: 14px;
                      font-weight: bold;
                      color: #666;
                    }

                    .operation {
                      display: flex;
                      justify-content: center;
                      align-items: center;

                      .operation-read {
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        color: #0077ff;
                        margin-right: 10px;
                        font-size: 12px;
                      }
                    }
                  }

                  .order-details {
                    width: 100%;
                    max-height: 260px;
                    overflow: hidden;
                    overflow-y: auto;

                    li {
                      width: 100%;
                      height: 78px;
                      display: flex;
                      margin: 10px 0;
                      font-size: 12px;

                      .left-icon {
                        width: 60px;
                        height: 100%;
                        background-color: #0880ff;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        border-top-left-radius: 8px;
                        border-bottom-left-radius: 8px;
                      }

                      .right-info {
                        flex: 1;
                        height: 78px;
                        border: 1px solid rgba(0, 0, 0, 0.1);
                        border-top-right-radius: 8px;
                        border-bottom-right-radius: 8px;
                        display: flex;
                        flex-direction: column;
                        // padding: 10px 20px;
                        padding: 10px 10px 10px 20px;

                        .top-date {
                          height: 40%;
                          display: flex;
                          justify-content: space-between;
                          align-items: center;

                          .orderInfo {
                            font-weight: bold;
                          }

                          .date {
                            color: #cecece;
                          }
                        }

                        .bottom-detail {
                          height: 60%;
                          display: flex;
                          align-items: center;
                          margin-top: 5px;

                          p {
                            width: 90%;
                            line-height: 16px;
                          }

                          a {
                            // flex: 1;
                            margin-left: 5px;
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        .Hui-skin {
          // width: 60px;
          height: 60px;
          line-height: 70px;
          position: relative;
          display: flex;
          justify-content: center;
          margin-left: 40px;

          &:hover .Huiskin-menus {
            display: block;
          }

          .top-Huiskin {
            cursor: pointer;
            color: #fff;
            display: flex;
            align-items: center;

            img {
              width: 20px;
              height: 20px;
            }
          }

          .Huiskin-menus {
            list-style: none;
            padding: 0;
            margin: 0;
            position: absolute;
            left: 50%;
            transform: translateX(-50%);
            top: 60px;
            width: 134px;
            display: none;
            background-color: #fff;
            border: 1px solid #e9ecef;
            box-shadow: 0px 0px 14px 0px rgba(0, 0, 0, 0.1);
            border-radius: 4px;

            li {
              cursor: pointer;
              width: 100%;
              height: 40px;
              font-size: 14px;
              display: flex;
              justify-content: space-between;
              align-items: center;
              padding: 0 10px;

              &:hover {
                color: #2d8cf0;
              }

              i {
                color: #ffffff !important;
                font-weight: bold;
              }
            }
          }
        }

        .sign-out {
          // width: 60px;
          height: 60px;
          position: relative;
          display: flex;
          justify-content: center;
          align-items: center;
          margin-left: 40px;

          img {
            width: 20px;
            height: 20px;
          }
        }

        .Lang {
          // width: 60px;
          height: 60px;
          line-height: 60px;
          position: relative;
          display: flex;
          justify-content: center;
          // margin-left: 10px;
          margin-left: 40px;

          &:hover .lang-menus {
            display: block;
          }

          .top-lang {
            cursor: pointer;
            color: #fff;
            display: flex;
            align-items: center;

            img {
              width: 24px;
              height: 24px;
            }
          }

          .lang-menus {
            list-style: none;
            padding: 0;
            margin: 0;
            position: absolute;
            left: 50%;
            transform: translateX(-50%);
            top: 60px;
            width: 134px;
            display: none;
            background-color: #fff;
            border: 1px solid #e9ecef;
            box-shadow: 0px 0px 14px 0px rgba(0, 0, 0, 0.1);
            border-radius: 4px;

            li {
              cursor: pointer;
              width: 100%;
              height: 40px;
              text-align: center;
              line-height: 40px;
              font-size: 14px;

              &:hover {
                color: #2d8cf0;
                background: #f4f7f9;
              }
            }
          }
        }
      }
    }

    .dialog-block {
      /deep/.el-dialog {
        width: 580px;
        height: 374px;
      }
    }

    .dialog-info {
      /deep/.el-dialog {
        width: 580px;
        height: 538px;

        .el-dialog__body {
          .upload-headimg {
            .el-form-item__content {
              display: flex;
              align-items: center;

              .avatar-uploader {
                height: 80px;
                width: 80px;
                display: flex;
                align-items: center;
                justify-content: center;
                border: 1px dashed #c0ccda;
              }

              .avatar-uploader:hover {
                border: 0.0625rem dashed #409eef;
                cursor: pointer;
              }

              .avatar-uploader .el-upload {
                cursor: pointer;
                position: relative;
                // border: 1px dashed #d9d9d9;
                // border-radius: 6px;
                // cursor: pointer;
                // position: relative;
                // overflow: hidden;
                // border-radius: 50px;
              }

              .avatar-uploader .el-upload:hover {
                border-color: #409eff;
              }

              .avatar-uploader-icon {
                font-size: 20px;
                color: #8c939d;
                width: 80px;
                // height: 80px;
                // line-height: 80px;
                text-align: center;
              }

              .avatar {
                width: 80px;
                height: 80px;
                display: block;
                // border-radius: 50px;
              }

              .removeImg {
                position: absolute;
                right: 0;
                top: 0;
              }
            }
          }

          .el-date-editor {
            width: 100%;

            input {
              width: 100%;
            }
          }
        }
      }
    }

    .dialog-block,
    .dialog-info {
      z-index: 2001;
      /deep/.el-date-picker{
        position: absolute !important;
        top: 36px !important;
        left: 0px !important;
      }

      /deep/.el-dialog {
        // width: 580px;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        margin: 0 !important;

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
            font-size: 16px;
            color: #414658;
          }
        }

        .el-dialog__body {
          // border-bottom: 1px solid #E9ECEF;
          padding: 40px 60px 40px 60px !important;

          .pass-input {
            .el-form {
              width: 340px;

              .el-form-item {
                width: 340px;
                height: 40px;

                .el-form-item__content {
                  .el-input {
                    width: 340px;
                    height: 40px;

                    input {
                      width: 340px;
                      height: 40px;
                    }
                  }
                }
              }
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

            .qxcolor {
              color: #6a7076;
              border: 1px solid #d5dbc6;
            }

            .qdcolor {
              background-color: #2890ff;
            }

            .qdcolor {
              background-color: #2890ff;
            }

            .qdcolor:hover {
              opacity: 0.8;
            }

            .qxcolor {
              color: #6a7076;
              border: 1px solid #d5dbe8;
            }

            .qxcolor:hover {
              color: #2890ff;
              border: 1px solid #2890ff;
              background-color: #fff;
            }
          }
        }
      }

      /deep/.el-form-item__label {
        font-weight: normal;
        color: #414658;
        padding: 0 14px 0 0 !important;
      }
    }

    /deep/.el-badge__content {
      border: none;
    }
  }
  .lang_active{
    color: #2890ff;
    font-weight: 600;
  }
  .pif{
	  width:20px;
	  height:20px;
  }
  /deep/.el-popper{
    position: absolute !important;
    top: 36px !important;
    left: 0px !important;
  }
</style>
