<template>
  <el-breadcrumb class="app-breadcrumb">
    <transition-group name="breadcrumb" id="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in newList" :key="item.path">
        <span
          v-if="item && item.redirect === 'noRedirect' || index == newList.length - 1"
          class="no-redirect"
          >{{ generateTitle(item.meta.title) }}</span
        >
        <a v-else @click.prevent="handleLink(item)">{{
          generateTitle(item.meta.title)
        }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script>
import pathToRegexp from 'path-to-regexp'
export default {
  data () {
    return {
      levelList: null,
      newList: []
    }
  },
  watch: {
    $route () {
      this.getBreadcrumb()
    }
  },
  created () {
    this.getBreadcrumb()
  },
  methods: {
    getBreadcrumb () {
      console.log('3737373737', this.$route.matched)
      let matched = this.$route.matched.filter(
        item => item.meta && item.meta.title
      )
      const first = matched[0]

      this.levelList = matched.filter(
        item => item.meta && item.meta.title && item.meta.breadcrumb !== false
      )
      this.newList = []
      let redirect = this.levelList[1].redirect
      this.levelList.filter(item => {
        this.newList.push(item)
      })
      console.log('levelList',this.levelList)
      if (
        this.newList.length == 3 &&
        this.newList[1].meta.title == this.newList[2].meta.title
        // &&this.newList[2].meta.title !== '订单列表'
      ) {
        this.newList.pop()
      }
      // &&this.newList[2].meta.title!='提现管理'&&(this.newList[3].meta.title!='提现记录'||this.newList[3].meta.title!='提现审核'||this.newList[3].meta.title!='提现设置')
      if (
        this.newList[0].meta.title == '用户' &&
        this.newList[1].meta.title !== '用户列表' &&
        this.newList[1].meta.title !== '提现管理'
      ) {
        console.log('74747474747474', this.newList)

        let arr = {
          meta: {
            title: '用户列表'
          },
          redirect: '/members/membersList'
        }
        this.newList.splice(1, 0, arr)

        if (
          this.newList[2].meta.title == '添加用户等级' ||
          this.newList[2].meta.title == '编辑用户等级'
        ) {
          let arrs = {
            meta: {
              title: '用户等级'
            },
            redirect: '/members/membersLevel'
          }
          this.newList.splice(2, 0, arrs)
        }
      }
      if (this.newList[0].meta.title == '商品') {
        if (this.newList[1].meta.title == '商品列表') {
          if (this.newList[2].meta.title == '发布商品' || this.newList[2].meta.title == '编辑商品' || this.newList[2].meta.title == '复制商品') {
            let arrs1 = {
              meta: {
                title: '实物商品'
              },
              redirect: '/goods/goodslist/physicalgoods'
            }
            this.newList.splice(2, 0, arrs1)
          }
          if (this.newList[2].meta.title == '发布虚拟商品' || this.newList[2].meta.title == '编辑虚拟商品' || this.newList[2].meta.title == '复制虚拟商品') {
            let arrs1 = {
              meta: {
                title: '虚拟商品'
              },
              redirect: '/goods/goodslist/virtualgoods'
            }
            this.newList.splice(2, 0, arrs1)
          }
        }

      }
      if (this.newList[1].meta.title == '卡券') {
        if (
          this.newList[2].meta.title == '编辑优惠券'
          ||  this.newList[2].meta.title == '领取记录'
          ||  this.newList[2].meta.title == '赠送记录'
          ||  this.newList[2].meta.title == '添加优惠券'
        ) {
          let arrs = {
            meta: {
              title: '优惠券列表'
            },
            redirect: '/plug_ins/coupons/couponsList'
          }
          this.newList.splice(2, 0, arrs)
        }
      }
      if (this.newList[1].meta.title == '秒杀') {
        if (
          this.newList[2].meta.title == '商品列表'
          || this.newList[2].meta.title == '添加商品'
        ) {
          let arrs = {
            meta: {
              title: '秒杀标签'
            },
            redirect: '/plug_ins/seckill/seckillLabel'
          }
          this.newList.splice(2, 0, arrs)
        }

        if (
          this.newList[2].meta.title == '编辑' ||
          this.newList[2].meta.title == '查看商品'
        ) {
          let arrs1 = {
            meta: {
              title: '秒杀标签'
            },
            redirect: '/plug_ins/seckill/seckillLabel'
          }
          let arrs2 = {
            meta: {
              title: '商品列表'
            },
            redirect: '/plug_ins/seckill/goodsList'
          }
          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (this.newList[2].meta.title == '售后管理') {
          let arrs = {
            meta: {
              title: '秒杀订单'
            },
            redirect: '/plug_ins/seckill/seckillOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '售后详情') {
          let arrs1 = {
            meta: {
              title: '秒杀订单'
            },
            redirect: '/plug_ins/seckill/seckillOrder'
          }
          let arrs2 = {
            meta: {
              title: '售后管理'
            },
            redirect: '/plug_ins/seckill/afterSaleList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (this.newList[2].meta.title == '订单结算') {
          let arrs = {
            meta: {
              title: '秒杀订单'
            },
            redirect: '/plug_ins/seckill/seckillOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '查看') {
          let arrs1 = {
            meta: {
              title: '秒杀订单'
            },
            redirect: '/plug_ins/seckill/seckillOrder'
          }
          let arrs2 = {
            meta: {
              title: '订单结算'
            },
            redirect: '/plug_ins/seckill/orderSettlementList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (
          this.newList[2].meta.title == '评论明细' ||
          this.newList[2].meta.title == '修改'
        ) {
          let arrs1 = {
            meta: {
              title: '秒杀订单'
            },
            redirect: '/plug_ins/seckill/seckillOrder'
          }
          let arrs2 = {
            meta: {
              title: '评价管理'
            },
            redirect: '/plug_ins/seckill/commentList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (this.newList[2].meta.title == '评价管理') {
          let arrs = {
            meta: {
              title: '秒杀订单'
            },
            redirect: '/plug_ins/seckill/seckillOrder'
          }

          this.newList.splice(2, 0, arrs)
        }

        if (
          this.newList[2].meta.title == '订单详情' ||
          this.newList[2].meta.title == '商品发货' ||
          this.newList[2].meta.title == '编辑订单'
        ) {
          let arrs = {
            meta: {
              title: '秒杀订单'
            },
            redirect: '/plug_ins/seckill/seckillOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
      }
      console.log('我走了吗', this.$route)
      console.log('我走了吗', this.newList)
      if (this.newList[1].meta.title == '主题管理') {
        if(this.$route.name == 'addTemplate' && this.$route.query.createdFrom ){
          let arrs2 = {
            meta: {
              title: '创建主题'
            },
            redirect: ''
          }
          this.newList.splice(2, 1, arrs2)
        } else if(this.$route.name == 'addTemplate' && !this.$route.query.createdFrom ){
          let arrs2 = {
            meta: {
              title: '编辑主题'
            },
            redirect: ''
           }
          this.newList.splice(2, 1, arrs2)
        }
        // 隐藏 diy 模板管理
        if (
          this.newList[1].meta.title == '主题管理'  && Object.keys(this.$route.query).length ==0
        ) {
          this.$set(this.newList[2].meta,'title','')
        }

        if (
          this.newList[2] &&
          this.newList[2].meta.title == '轮播图列表' ||
          this.newList[2].meta.title == 'UI导航栏' ||
          this.newList[2].meta.title == '分类管理' ||
          this.newList[2].meta.title == '活动管理'
        ) {
          let arrs = {
            meta: {
              title: '编辑默认模板'
            },
            redirect: '/plug_ins/template/divTemplate'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (
          this.newList[2] &&
          this.newList[2].meta.title == '添加轮播图' ||
          this.newList[2].meta.title == '编辑轮播图'
        ) {
          let arrs = {
            meta: {
              title: '编辑默认模板'
            },
            redirect: '/plug_ins/template/divTemplate'
          }
          let arrs2 = {
            meta: {
              title: '轮播图列表'
            },
            redirect: '/plug_ins/template/playlist'
          }
          this.newList.splice(2, 0, arrs)
          this.newList.splice(3, 0, arrs2)
        }
        if (
          this.newList[2] &&
          this.newList[2].meta.title == '添加活动' ||
          this.newList[2].meta.title == '编辑活动'
        ) {
          let arrs = {
            meta: {
              title: '编辑默认模板'
            },
            redirect: '/plug_ins/template/divTemplate'
          }
          let arrs2 = {
            meta: {
              title: '活动管理'
            },
            redirect: '/plug_ins/template/activity'
          }
          this.newList.splice(2, 0, arrs)
          this.newList.splice(3, 0, arrs2)
        }


         const arr =  this.newList.filter(
        item =>  item.meta.title
      )
      this.newList = arr
        console.log('走了111111',this.newList)
      }
      if (this.newList[0].meta.title == '商城') {
        if (this.newList[1].meta.title == '插件配置') {
          if (
            this.newList[2].meta.title == '编辑' ||
            this.newList[2].meta.title == '拼团配置' ||
            this.newList[2].meta.title == '竞拍配置'
          ) {
            let arrs = {
              meta: {
                title: '插件列表'
              },
              redirect: '/plug_ins/plugInsSet/plugInsList'
            }
            this.newList.splice(2, 0, arrs)
          }
        }
      }
      if (this.newList[1].meta.title == '积分商城') {
        if (
          this.newList[2].meta.title == '添加商品' ||
          this.newList[2].meta.title == '编辑' ||
          this.newList[2].meta.title == '兑换记录'
        ) {
          let arrs = {
            meta: {
              title: '积分商品'
            },
            redirect: '/plug_ins/integralMall/integralGoodsList'
          }
          this.newList.splice(2, 0, arrs)
        }

        if (this.newList[2].meta.title == '售后管理') {
          let arrs = {
            meta: {
              title: '兑换订单'
            },
            redirect: '/plug_ins/integralMall/integralOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '售后详情') {
          let arrs1 = {
            meta: {
              title: '兑换订单'
            },
            redirect: '/plug_ins/integralMall/integralOrder'
          }
          let arrs2 = {
            meta: {
              title: '售后管理'
            },
            redirect: '/plug_ins/integralMall/afterSaleList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (this.newList[2].meta.title == '订单结算') {
          let arrs = {
            meta: {
              title: '兑换订单'
            },
            redirect: '/plug_ins/integralMall/integralOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '查看') {
          let arrs1 = {
            meta: {
              title: '兑换订单'
            },
            redirect: '/plug_ins/integralMall/integralOrder'
          }
          let arrs2 = {
            meta: {
              title: '订单结算'
            },
            redirect: '/plug_ins/integralMall/orderSettlementList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (this.newList[2].meta.title == '评价管理') {
          let arrs = {
            meta: {
              title: '兑换订单'
            },
            redirect: '/plug_ins/integralMall/integralOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '修改'
          ||this.newList[2].meta.title == '评价明细'
        ) {
          let arrs1 = {
            meta: {
              title: '兑换订单'
            },
            redirect: '/plug_ins/integralMall/integralOrder'
          }
          let arrs2 = {
            meta: {
              title: '评价管理'
            },
            redirect: '/plug_ins/integralMall/commentList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (
          this.newList[2].meta.title == '订单详情' ||
          this.newList[2].meta.title == '商品发货' ||
          this.newList[2].meta.title == '编辑订单'
        ) {
          let arrs = {
            meta: {
              title: '兑换订单'
            },
            redirect: '/plug_ins/integralMall/integralOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
      }
      if (this.newList[1].meta.title == '会员') {
        if(
          this.newList[2].meta.title == '购买记录'
          || this.newList[2].meta.title == '编辑会员'
          || this.newList[2].meta.title == '添加会员'
        ){
          let arrs1 = {
            meta: {
              title: '会员列表'
            },
            redirect: '/plug_ins/member/memberList'
          }
          this.newList.splice(2, 0, arrs1)
        }

        if(
          this.newList[2].meta.title == '添加商品'
        ){
          let arrs1 = {
            meta: {
              title: '会员商品'
            },
            redirect: '/plug_ins/member/memberGoods'
          }
          this.newList.splice(2, 0, arrs1)
        }
      }
      if (this.newList[1].meta.title == '拼团') {
        if(
          this.newList[2].meta.title == '添加拼团活动'
          || this.newList[2].meta.title == '查看详情'
        ){
          let arrs1 = {
            meta: {
              title: '拼团活动'
            },
            redirect: '/plug_ins/group/groupGoods'
          }
          this.newList.splice(2, 0, arrs1)
        }

        if(
          this.newList[2].meta.title == '开团详情'
        ){
          let arrs1 = {
            meta: {
              title: '开团记录'
            },
            redirect: '/plug_ins/group/groupRecord'
          }
          this.newList.splice(2, 0, arrs1)
        }

        if (
          this.newList[2].meta.title == '售后管理' ||
          this.newList[2].meta.title == '订单结算' ||
          this.newList[2].meta.title == '订单详情' ||
          this.newList[2].meta.title == '编辑订单' ||
          this.newList[2].meta.title == '商品发货'
        ) {
          let arrs1 = {
            meta: {
              title: '拼团订单'
            },
            redirect: '/plug_ins/group/groupOrderList'
          }
          this.newList.splice(2, 0, arrs1)
        }
        if (this.newList[2].meta.title == '售后详情') {
          let arrs1 = {
            meta: {
              title: '拼团订单'
            },
            redirect: '/plug_ins/group/groupOrderList'
          }
          let arrs2 = {
            meta: {
              title: '售后管理'
            },
            redirect: '/plug_ins/group/salesReturnList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }
        if (this.newList[2].meta.title == '查看') {
          let arrs1 = {
            meta: {
              title: '拼团订单'
            },
            redirect: '/plug_ins/group/groupOrderList'
          }
          let arrs2 = {
            meta: {
              title: '订单结算'
            },
            redirect: '/plug_ins/group/orderSettlementList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }
        if (
          this.newList[2].meta.title == '评价明细' ||
          this.newList[2].meta.title == '修改' ||
          this.newList[2].meta.title == '查看评论' ||
          this.newList[2].meta.title == '详情'
        ) {
          let arrs1 = {
            meta: {
              title: '拼团订单'
            },
            redirect: '/plug_ins/group/groupOrderList'
          }
          let arrs2 = {
            meta: {
              title: '评价管理'
            },
            redirect: '/plug_ins/group/commentList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (this.newList[2].meta.title == '评价管理') {
          let arrs = {
            meta: {
              title: '拼团订单'
            },
            redirect: '/plug_ins/group/groupOrderList'
          }

          this.newList.splice(2, 0, arrs)
        }
      }
      if (this.newList[1].meta.title == '竞拍') {
        if (
          this.newList[2].meta.title == '评价明细' ||
          this.newList[2].meta.title == '修改' ||
          this.newList[2].meta.title == '查看评论'
        ) {
          let arrs1 = {
            meta: {
              title: '竞拍订单'
            },
            redirect: '/plug_ins/auction/auctionOrder'
          }
          let arrs2 = {
            meta: {
              title: '评价管理'
            },
            redirect: '/plug_ins/auction/evaluateList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }
        if (this.newList[2].meta.title == '结算详情') {
          let arrs1 = {
            meta: {
              title: '竞拍订单'
            },
            redirect: '/plug_ins/auction/auctionOrder'
          }
          let arrs2 = {
            meta: {
              title: '订单结算'
            },
            redirect: '/plug_ins/auction/orderSettlementList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }
        if (this.newList[2].meta.title == '评价管理') {
          let arrs = {
            meta: {
              title: '竞拍订单'
            },
            redirect: '/plug_ins/auction/auctionOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (
          this.newList[2].meta.title == '发布专场' ||
          this.newList[2].meta.title == '编辑专场' ||
          this.newList[2].meta.title == '缴纳记录' ||
          this.newList[2].meta.title == '查看场次' ||
          this.newList[2].meta.title == '查看拍品'
        ) {
          let arrs = {
            meta: {
              title: '专场列表'
            },
            redirect: '/plug_ins/auction/specialLlist'
          }
          this.newList.splice(2, 0, arrs)
        }
      }
      if (this.newList[1].meta.title == '店铺') {

        if (
          this.newList[2].meta.title == '添加店铺' ||
          this.newList[2].meta.title == '分账设置'  ||
          this.newList[2].meta.title == '编辑' ||
          (this.newList[2].meta.title == '查看' && this.$route.query.mchLook)
        ) {
          let arrs = {
            meta: {
              title: '店铺列表'
            },
            redirect: '/plug_ins/stores/store'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (
          (this.newList[2].meta.title == '查看' && this.$route.query.storeAudit)
        ) {
          let arrs = {
            meta: {
              title: '店铺审核'
            },
            redirect: '/plug_ins/stores/auditList'
          }
          this.newList.splice(2, 0, arrs)
        }

        if (
          this.newList[2].meta.title == '添加分类' ||
          this.newList[2].meta.title == '编辑分类'
        ) {
          let arrs = {
            meta: {
              title: '店铺分类'
            },
            redirect: '/plug_ins/stores/storeFl'
          }
          this.newList.splice(2, 0, arrs)
        }
      }

      if (this.newList[1].meta.title == '店铺') {
        if (this.newList[2].meta.title == '查看详情') {
          let arrs = {
            meta: {
              title: '商品审核'
            },
            redirect: '/plug_ins/stores/goodsAudit'
          }
          this.newList.splice(2, 0, arrs)
        }
      }
      if (this.newList[1].meta.title == '竞拍') {
        if (this.newList[2].meta.title == '出价详情') {
          let arrs = {
            meta: {
              title: '专场列表'
            },
            redirect: '/plug_ins/auction/specialLlist'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '查看') {
          let arrs1 = {
            meta: {
              title: '竞拍订单'
            },
            redirect: '/plug_ins/auction/auctionOrder'
          }
          let arrs2 = {
            meta: {
              title: '订单结算'
            },
            redirect: '/plug_ins/auction/orderSettlementList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }
      }
      if (this.newList[1].meta.title == '竞拍') {
        if (
          this.newList[2].meta.title == '订单结算' ||
          this.newList[2].meta.title == '商品发货' ||
          this.newList[2].meta.title == '编辑订单' ||
          this.newList[2].meta.title == '订单详情'
        ) {
          let arrs = {
            meta: {
              title: '竞拍订单'
            },
            redirect: '/plug_ins/auction/auctionOrder'
          }
          this.newList.splice(2, 0, arrs)
        }
      }
      if (this.newList[1].meta.title == '分销') {
        if (
          this.newList[2].meta.title == '查看下级' ||
          this.newList[2].meta.title == '编辑' ||
          this.newList[2].meta.title == '添加分销商'
        ) {
          let arrs = {
            meta: {
              title: '分销商管理'
            },
            redirect: '/plug_ins/distribution/distributorsList'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (
          this.newList[2].meta.title == '添加分销等级' ||
          this.newList[2].meta.title == '编辑分销等级'
        ) {
          let arrs = {
            meta: {
              title: '分销等级'
            },
            redirect: '/plug_ins/distribution/distributionLevel'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (
          this.newList[2].meta.title == '添加商品' ||
          this.newList[2].meta.title == '编辑商品'
        ) {
          let arrs = {
            meta: {
              title: '分销商品'
            },
            redirect: '/plug_ins/distribution/distributionGoods'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (
          this.newList[2].meta.title == '订单详情' ||
          this.newList[2].meta.title == '编辑订单' ||
          this.newList[2].meta.title == '评价列表' ||
          this.newList[2].meta.title == '售后管理' ||
          this.newList[2].meta.title == '订单结算'
        ) {
          let arrs = {
            meta: {
              title: '分销订单'
            },
            redirect: '/plug_ins/distribution/distributorsOrderList'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (
          this.newList[2].meta.title == '修改评价' ||
          this.newList[2].meta.title == '评价明细'
        ) {
          let arrs1 = {
            meta: {
              title: '分销订单'
            },
            redirect: '/plug_ins/distribution/distributorsOrderList'
          }
          let arrs2 = {
            meta: {
              title: '评价列表'
            },
            redirect: '/plug_ins/distribution/distributorsCommentList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }
      }

      //预售
      if (this.newList[1].meta.title == '商品预售') {
        if (
          this.newList[2].meta.title == '发布商品' ||
          this.newList[2].meta.title == '编辑商品' ||
          this.newList[2].meta.title == '查看详情'
        ) {
          let arrs = {
            meta: {
              title: '商品列表'
            },
            redirect: '/plug_ins/preSale/goodsList'
          }
          this.newList.splice(2, 0, arrs)
        }

        if (this.newList[2].meta.title == '售后管理') {
          let arrs = {
            meta: {
              title: '预售订单'
            },
            redirect: '/plug_ins/preSale/preSaleOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '售后详情') {
          let arrs1 = {
            meta: {
              title: '预售订单'
            },
            redirect: '/plug_ins/preSale/preSaleOrder'
          }
          let arrs2 = {
            meta: {
              title: '售后管理'
            },
            redirect: '/plug_ins/preSale/afterSaleList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (this.newList[2].meta.title == '订单结算') {
          let arrs = {
            meta: {
              title: '预售订单'
            },
            redirect: '/plug_ins/preSale/preSaleOrder'
          }

          this.newList.splice(2, 0, arrs)
        }

        if (this.newList[2].meta.title == '查看') {
          let arrs1 = {
            meta: {
              title: '预售订单'
            },
            redirect: '/plug_ins/preSale/preSaleOrder'
          }
          let arrs2 = {
            meta: {
              title: '订单结算'
            },
            redirect: '/plug_ins/preSale/orderSettlementList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (this.newList[2].meta.title == '评价管理') {
          let arrs = {
            meta: {
              title: '预售订单'
            },
            redirect: '/plug_ins/preSale/preSaleOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '评价明细') {
          let arrs1 = {
            meta: {
              title: '预售订单'
            },
            redirect: '/plug_ins/preSale/preSaleOrder'
          }
          let arrs2 = {
            meta: {
              title: '评价管理'
            },
            redirect: '/plug_ins/preSale/evaluateList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }
        if (
          this.newList[2].meta.title == '修改' ||
          this.newList[2].meta.title == '查看评论'
        ) {
          let arrs1 = {
            meta: {
              title: '预售订单'
            },
            redirect: '/plug_ins/preSale/preSaleOrder'
          }
          let arrs2 = {
            meta: {
              title: '评价管理'
            },
            redirect: '/plug_ins/preSale/evaluateList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (
          this.newList[2].meta.title == '订单详情' ||
          this.newList[2].meta.title == '商品发货' ||
          this.newList[2].meta.title == '编辑订单' ||
          this.newList[2].meta.title == '查看物流'
        ) {
          let arrs = {
            meta: {
              title: '预售订单'
            },
            redirect: '/plug_ins/preSale/preSaleOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
      }
      //直播
      if (this.newList[1].meta.title == '直播') {
        if (
          this.newList[2].meta.title == '查看粉丝'
        ) {
          let arrs = {
            meta: {
              title: '直播间管理'
            },
            redirect: '/plug_ins/live/liveList'
          }
          this.newList.splice(2, 0, arrs)
        }

        if (this.newList[2].meta.title == '商品详情'&&this.$route.query.type==1) {
          let arrs1 = {
            meta: {
              title: '直播场次'
            },
            redirect: '/plug_ins/live/liveSession'
          }
          let arrs2 = {
            meta: {
              title: '查看商品'
            },
            redirect: '/plug_ins/live/liveSessionGoods'
          }
          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)

        }else if (this.newList[2].meta.title == '商品详情') {
          let arrs1 = {
            meta: {
              title: '直播商品'
            },
            redirect: '/plug_ins/live/liveGoods'
          }

          this.newList.splice(2, 0, arrs1)


        }
        if (this.newList[2].meta.title == '查看商品') {
          let arrs = {
            meta: {
              title: '直播场次'
            },
            redirect: '/plug_ins/live/liveSession'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '查看订单'||
        this.newList[2].meta.title == '查看观众') {
          let arrs1 = {
            meta: {
              title: '直播场次'
            },
            redirect: '/plug_ins/live/liveSession'
          }
          // let arrs2 = {
          //   meta: {
          //     title: '售后管理'
          //   },
          //   redirect: '/plug_ins/preSale/afterSaleList'
          // }

          this.newList.splice(2, 0, arrs1)
          // this.newList.splice(3, 0, arrs2)
        }
        if (this.newList[2].meta.title == '退款售后') {
          let arrs = {
            meta: {
              title: '直播订单'
            },
            redirect: '/plug_ins/live/liveOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '售后详情') {
          let arrs1 = {
            meta: {
              title: '直播订单'
            },
            redirect: '/plug_ins/live/liveOrder'
          }
          let arrs2 = {
            meta: {
              title: '退款售后'
            },
            redirect: '/plug_ins/live/liveSalesReturnList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (this.newList[2].meta.title == '订单结算') {
          let arrs = {
            meta: {
              title: '直播订单'
            },
            redirect: '/plug_ins/live/liveOrder'
          }

          this.newList.splice(2, 0, arrs)
        }

        if (this.newList[2].meta.title == '查看') {
          let arrs1 = {
            meta: {
              title: '直播订单'
            },
            redirect: '/plug_ins/live/liveOrder'
          }
          let arrs2 = {
            meta: {
              title: '评价管理'
            },
            redirect: '/plug_ins/live/liveCommentList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (this.newList[2].meta.title == '评价管理') {
          let arrs = {
            meta: {
              title: '直播订单'
            },
            redirect: '/plug_ins/live/liveOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
        if (this.newList[2].meta.title == '评论明细') {
          let arrs1 = {
            meta: {
              title: '直播订单'
            },
            redirect: '/plug_ins/live/liveOrder'
          }
          let arrs2 = {
            meta: {
              title: '评价管理'
            },
            redirect: '/plug_ins/live/liveCommentList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
          console.log(' this.newList', this.newList)
        }
        if (
          this.newList[2].meta.title == '修改' ||
          this.newList[2].meta.title == '查看评论'
        ) {
          let arrs1 = {
            meta: {
              title: '直播订单'
            },
            redirect: '/plug_ins/live/liveOrder'
          }
          let arrs2 = {
            meta: {
              title: '评价管理'
            },
            redirect: '/plug_ins/live/liveCommentList'
          }

          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }

        if (
          this.newList[2].meta.title == '订单详情' ||
          this.newList[2].meta.title == '商品发货' ||
          this.newList[2].meta.title == '编辑订单' ||
          this.newList[2].meta.title == '查看物流'
        ) {
          let arrs = {
            meta: {
              title: '直播订单'
            },
            redirect: '/plug_ins/live/liveOrder'
          }

          this.newList.splice(2, 0, arrs)
        }
      }
      //权限管理
      if (this.newList[1].meta.title == '权限管理') {
        if (
          this.newList[2].meta.title == '添加菜单' ||
          this.newList[2].meta.title == '编辑菜单' ||
          this.newList[2].meta.title == '查看下级'
        ) {
          let arrs = {
            meta: {
              title: '菜单列表'
            },
            redirect: '/Platform/permissions/menulist'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (
          this.newList[2].meta.title == '添加角色' ||
          this.newList[2].meta.title == '查看角色' ||
          this.newList[2].meta.title == '权限修改'
        ) {
          let arrs = {
            meta: {
              title: '角色列表'
            },
            redirect: '/Platform/permissions/rolelist'
          }
          this.newList.splice(2, 0, arrs)
        }
      }

      if (this.newList[1].meta.title == '短信配置') {
        if (
          this.newList[2].meta.title == '添加短信' ||
          this.newList[2].meta.title == '编辑短信'
        ) {
          let arrs = {
            meta: {
              title: '短信列表'
            },
            redirect: '/mall/sms/smsList'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (
          this.newList[2].meta.title == '添加模板' ||
          this.newList[2].meta.title == '编辑模板'
        ) {
          let arrs = {
            meta: {
              title: '短信模板'
            },
            redirect: '/mall/sms/smsList'
          }
          this.newList.splice(2, 0, arrs)
        }
      }

      // 供应商
      if (this.newList[1].meta.title == '供应商') {
        if(
          this.newList[2].meta.title == '待审核商品'
          || this.newList[2].meta.title == '违规下架商品'
          || this.newList[2].meta.title == '断供商品列表'
        ){
          let arrs = {
            meta: {
              title: '商品列表'
            },
            redirect: '/plug_ins/supplier/poolList'
          }
          this.newList.splice(2, 0, arrs)
        }

        if (
          this.newList[2].meta.title == '添加供应商' ||
          this.newList[2].meta.title == '编辑供应商'
        ) {
          let arrs = {
            meta: {
              title: '供应商管理'
            },
            redirect: '/plug_ins/supplier/supplierList'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (
          this.newList[2].meta.title == '查看详情' &&
          this.$route.query.poolList
        ) {
          let arrs = {
            meta: {
              title: '商品列表'
            },
            redirect: '/plug_ins/supplier/poolList'
          }
          this.newList.splice(2, 0, arrs)
        }
        if (
          this.newList[2].meta.title == '查看详情' &&
          this.$route.query.poolReviewed
        ) {
          let arrs1 = {
            meta: {
              title: '商品列表'
            },
            redirect: '/plug_ins/supplier/poolList'
          }
          let arrs2 = {
            meta: {
              title: '待审核商品'
            },
            redirect: '/plug_ins/supplier/poolReviewed'
          }
          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }
        if (
          this.newList[2].meta.title == '查看详情' &&
          this.$route.query.poolLower
        ) {
          let arrs1 = {
            meta: {
              title: '商品列表'
            },
            redirect: '/plug_ins/supplier/poolList'
          }
          let arrs2 = {
            meta: {
              title: '违规下架商品'
            },
            redirect: '/plug_ins/supplier/poolLower'
          }
          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }
        if (
          this.newList[2].meta.title == '查看详情' &&
          this.$route.query.poolUnable
        ) {
          let arrs1 = {
            meta: {
              title: '商品列表'
            },
            redirect: '/plug_ins/supplier/poolList'
          }
          let arrs2 = {
            meta: {
              title: '断供商品列表'
            },
            redirect: '/plug_ins/supplier/poolUnable'
          }
          this.newList.splice(2, 0, arrs1)
          this.newList.splice(3, 0, arrs2)
        }
        if(
          this.newList[2].meta.title == '订单详情'
        ){
          let arrs = {
            meta: {
              title: '供应商订单'
            },
            redirect: '/plug_ins/supplier/supplierOrder'
          }
          this.newList.splice(2, 0, arrs)
        }
        if(
          this.newList[2].meta.title == '提现记录'
        ){
          let arrs = {
            meta: {
              title: '提现审核'
            },
            redirect: '/plug_ins/supplier/withdrawalAudit'
          }
          this.newList.splice(2, 0, arrs)
        }
      }
    },
    isDashboard (route) {
      const name = route && route.name
      if (!name) {
        return false
      }
      return name.trim().toLocaleLowerCase() === 'Dashboard'.toLocaleLowerCase()
    },
    pathCompile (path) {
      const { params } = this.$route
      var toPath = pathToRegexp.compile(path)
      return toPath(params)
    },
    handleLink (item) {
      const { redirect, path } = item
      if (item.meta.returns || item.returns) {
        this.$router.go(-1)
      } else {
        if (redirect) {
          this.$router.push(redirect)
          return
        }
        this.$router.push(this.pathCompile(path))
      }
      console.log(redirect)
    },

    generateTitle (title) {
      const hasKey = this.$te('route.' + title)
      if (hasKey) {
        const translatedTitle = this.$t('route.' + title)
        return translatedTitle
      }
      return title
    }
  }
}
</script>

<style lang="scss" scoped>
.app-breadcrumb.el-breadcrumb {
  display: inline-block;
  font-size: 18px;
  line-height: 60px;
  margin-left: 20px;

  .no-redirect {
    color: #414658;
    cursor: text;
  }

  a {
    color: #414658;
  }

  a:hover {
    color: #2890ff;
  }

  span {
    color: #6a7076 !important;
  }
}
</style>
