import {del, save, index} from "@/api/order/comment";
import {isEmpty} from "element-ui/src/utils/util";

export default {
  name: 'reply',
  //初始化数据
  data() {
    return {mainData: {}, replyText: null}
  },
  //组装模板
  created() {
    this.loadData();
  },
  methods: {
    async loadData() {
      const res = await index({
        api: 'admin.order.getCommentsDetailInfoById',
        cid: this.$route.params.id
      });
      this.mainData = res.data.data.list[0];
      console.log(res)
    },
    //回复
    async Reply() {
      this.dialogVisible = true;
      let title = '回复评论';
      if(!this.replyText) {
        this.$message({
          type: 'error',
          message: '回复评论不能为空'
        })
        return
      }
      await save({
        api: 'admin.order.replyComments',
        commentId: this.mainData.id,
        commentText: this.replyText,
      }).then(res => {
        if (res.data.code == '200') {
          this.$message({
            type: 'success',
            message: title + '成功!'
          })
          this.dialogVisible = false;
          this.$router.go(-1)
        }
      })
    },


  }

}
