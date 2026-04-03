import { del, save, index } from "@/api/order/comment";
import { isEmpty } from "element-ui/src/utils/util";

export default {
  name: 'edit',
  //初始化数据
  data() {
    return {
      mainData: {}, replyText: null, star: 0,
      starGroup: [this.$t('commentEdit.cp'), this.$t('commentEdit.sw'), this.$t('commentEdit.yb'), this.$t('commentEdit.my'), this.$t('commentEdit.hp')],
      isEdit: false,
      //评论图集
      commentImgList: null,
      //追评图集
      reviewImgList: null,
      falg: true
    }
  },
  //组装模板
  created() {
    if (this.$route.query.isView) {
      this.$router.currentRoute.matched[2].meta.title = '查看'
    }
    this.loadData();
  },
  mounted() {
  },
  methods: {
    retures() {
      this.$router.push({
        path: '/plug_ins/seckill/commentList'
      })
    },
    async loadData() {
      await index({
        api: 'plugin.sec.order.getCommentsInfo',
        cid: this.$route.query.id
      }).then(data => {
        if (!isEmpty(data)) {

          this.mainData = data.data.data.list[0];
          this.mainData.CommentType = Number(this.mainData.CommentType);

          this.star = this.mainData.CommentType
          //评价图数据处理
          this.commentImgList = this.mainData.images;
          if (!isEmpty(this.commentImgList)) {
            let imgList = [];
            this.commentImgList.forEach(function (item) {
              imgList.push(item.url)
            })
            this.commentImgList = imgList;
          }
          //追评图数据处理
          this.reviewImgList = this.mainData.review_images;
          if (!isEmpty(this.reviewImgList)) {
            let imgList = [];
            this.reviewImgList.forEach(function (item) {
              imgList.push(item.url)
            })
            this.reviewImgList = imgList;
          }
        } else {
          // this.$router.go(-1);
          this.$router.push({
            path: '/plug_ins/seckill/commentList'
          })
        }
      });
    },
    async Save() {
      this.dialogVisible = true;
      let title = '修改评论';
      let param = {
        api: 'plugin.sec.order.updateCommentsDetailInfoById',
        cid: this.mainData.id,
        commentText: decodeURIComponent(this.mainData.content),
        commentType: this.mainData.CommentType,
        commentImgUrls: this.commentImgList ? this.commentImgList.toString() : '',
        review: this.mainData.review ? decodeURIComponent(this.mainData.review) : '',
        reviewImgList: this.reviewImgList ? this.reviewImgList.toString() : '',
      }
      if (!this.falg) return
      this.falg = false
      await save(param).then(res => {
        if (!isEmpty(res.data.data)) {
          this.$message({
            type: 'success',
            offset: 100,
            message: this.$t('zdata.xgcg')
          })
        }
        this.dialogVisible = false;
        // this.$router.go(-1)
        this.$router.push({
          path: '/plug_ins/seckill/commentList'
        })
      }).finally(() => {
        setTimeout(() => {
          this.falg = true
        }, 1500)
      })
    },


  }

}
