import { save, index } from "@/api/order/orderSet";
import { isEmpty } from "element-ui/src/utils/util";

export default {
  name: 'orderSet',
  //初始化数据
  data() {
    return {
      isop: false,
      isProportion: false,
      mainData: { 
        isMany: false,
        giveStatus: 0
      },

      issueTimeList: [
        {
          value: 0,
          name: '收货后'
        },
        {
          value: 1,
          name: '付款后'
        },
      ],
    }
  },
  //组装模板
  created() {
    this.loadData();
  },

  watch: {
    'mainData.same_piece': {
      handler: function (newVal, oldVal) {
        if (parseInt(newVal) <= 0) {
          this.mainData.same_piece = 1
        } else {
          this.mainData.same_piece = parseInt(newVal)
        }
        console.log(newVal);
      }
    },

    'mainData.same_order': {
      handler: function (newVal, oldVal) {
        if (parseInt(newVal) <= 0) {
          this.mainData.same_order = 1
        } else {
          this.mainData.same_order = parseInt(newVal)
        }
        console.log(newVal);
      }
    },
  },

  mounted() {
  },
  methods: {
    agreeChange() {
      this.$message({
        message: '切换发放时间可能导致部分订单无法获得积分',
        type: 'warning',
        offset: 100
      })
    },
    async loadData() {
      await index({
        api: 'admin.orderSet.index',
      }).then(data => {
        if (!isEmpty(data)) {
          data = data.data.data;
          let isMany = false;
          if (data.same_order > 0 || data.same_piece > 0) {
            isMany = true
          }
          data.isMany = isMany;
          this.mainData = data;
          if(this.mainData.proportion > 0) {
            this.isProportion = true
          }
          this.isop = this.mainData.auto_good_comment_day > 0 ? true : false
        }
      });
    },

    blurPiece() {
      if (!this.mainData.same_piece) {
        this.mainData.same_piece = 1
      }
    },

    blurOrder() {
      if (!this.mainData.same_order) {
        this.mainData.same_order = 1
      }
    },

    async Save() {

      this.mainData.auto_the_goods = Number(this.mainData.auto_the_goods)
      this.mainData.order_failure = Number(this.mainData.order_failure)
      this.mainData.order_after = Number(this.mainData.order_after)
      this.mainData.remind_day = Number(this.mainData.remind_day)
      this.mainData.remind_hour = Number(this.mainData.remind_hour)
      if (this.mainData.auto_the_goods < 1 || this.mainData.auto_the_goods != this.mainData.auto_the_goods.toFixed(0)) {
        this.$message({
          message: '自动收货时间必须为正整数',
          type: 'warning',
          offset: 100
        })
        return
      }
      if (Number(this.mainData.auto_the_goods) > 9999999) {
        this.$message({
          message: '自动收货时间过大，请重新输入',
          type: 'warning',
          offset: 100
        })
        return
      }
      if (this.mainData.order_failure < 1 || this.mainData.order_failure != this.mainData.order_failure.toFixed(0)) {
        this.$message({
          message: '订单失效时间必须为正整数',
          type: 'warning',
          offset: 100
        })
        return
      }
      if (this.mainData.order_after < 0 || this.mainData.order_after != this.mainData.order_after.toFixed(0)) {
        this.$message({
          message: '订单售后时间必须为正整数',
          type: 'warning',
          offset: 100
        })
        return
      }
      if (this.mainData.isMany&&(this.mainData.same_piece < 1 || this.mainData.same_piece != this.mainData.same_piece.toFixed(0))) {
        this.$message({
          message: '同件商品数量必须为正整数',
          type: 'warning',
          offset: 100
        })
        return
      }
      if (this.mainData.isMany&&(this.mainData.same_order < 1 || this.mainData.same_order != this.mainData.same_order.toFixed(0))) {
        this.$message({
          message: '同一订单中，商品数量必须为正整数',
          type: 'warning',
          offset: 100
        })
        return
      }
      console.dir(this.mainData.remind_day)
      console.dir(this.mainData.remind_hour)
      if (this.mainData.remind_day < 0 || this.mainData.remind_day != this.mainData.remind_day.toFixed(0)) {
        this.$message({
          message: '提醒限制的天必须为大于等于零的整数',
          type: 'warning',
          offset: 100
        })
        return
      }
      if (this.mainData.remind_hour < 0 || this.mainData.remind_hour != this.mainData.remind_hour.toFixed(0)) {
        this.$message({
          message: '提醒限制的小时必须为大于等于零的整数',
          type: 'warning',
          offset: 100
        })
        return
      }
      if (this.mainData.auto_good_comment_day < 0) {
        this.$message({
          message: '自动评价设置不能为负数',
          type: 'warning',
          offset: 100
        })
        return
      }

      this.dialogVisible = true;
      if (!this.mainData.isMany) {
        this.mainData.same_piece = 0;
        this.mainData.same_order = 0;
      }
      let param = {
        api: 'admin.orderSet.saveConfig',
        packageSettings: this.mainData.isMany ? 1 : 0,
        samePiece: this.mainData.same_piece,
        sameOrder: this.mainData.same_order,
        autoTheGoods: this.mainData.auto_the_goods,
        orderFailure: this.mainData.order_failure,
        orderAfter: this.mainData.order_after,
        remindHour: this.mainData.remind_hour,
        remindDay: this.mainData.remind_day,
        autoGoodCommentDay: this.mainData.auto_good_comment_day,
        autoCommentContent: this.mainData.autoCommentContent,

        giveStatus: this.mainData.giveStatus,
        amsTime: !this.mainData.giveStatus ? this.mainData.amsTime : 0,
        proportion: this.isProportion ? this.mainData.proportion : 0
      }

      await save(param).then(res => {
        if (!isEmpty(res) && res.data.code == '200') {
          this.$message({
            message: this.$t("zdata.baccg"),
            type: "success",
            offset: 100,
          });
          this.loadData();
        }
        this.dialogVisible = false;

      })
    },
    mychange(e) {
      if (e) {
        this.mainData.auto_good_comment_day = 1
      } else {
        this.mainData.auto_good_comment_day = 0
      }
    },

    oninput4(num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '');
      str = str.replace('.', '');
      if (Number(str) > 9999) {
        return 9999
      } else {
        return str
      }
    },

  }

}
