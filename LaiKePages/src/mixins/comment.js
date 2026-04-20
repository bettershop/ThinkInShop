export default {
  data() {
    return {
      // 评论数据
      comments: [ ],
      placeholder: '说点什么吧...',
      commentIdx: '',//一级评论索引
      page: 1, // 当前页码
      pagesize: 12, // 每页数据条数
      parentId: '',//父级评论id
      showComment:false,//显示评论弹窗
      commentCount:0,//评论总数
      commentId:0,//整个文章评论id
      childPage:0,//二级评论页码
      top_id:0,//一级评论id
      scrollTop:0,//滚动距离
      top_total:0,//一级评论总数
    }
  },
  methods: {
    // 打开评论弹窗
    openComment(id) {
      this.commentId = id
      this.getComments()
      this.showComment = true;
    },
    
    // 关闭评论弹窗（接收组件事件）
    closeComment() {
      this.showComment = false;
      this.onClear()
    },
    //一级评论
    getComments() {
      let data = {
        api:"plugin.bbs.Appbbs.getcomment",
        id: this.commentId,
        page: this.page,
        pagesize: 10,
      }
      this.$req.post({data}).then(res=>{
          if (res.code == 200) {
              res.data.list.forEach((item,index) => {
                item.showReplies = false;
                item.replies = item.replies || [];
              })
              if (this.page == 1) {
                this.comments = res.data.list;
              }else{
                this.comments = this.comments.concat(res.data.list);
              }
              this.commentCount = res.data.total
              this.top_total = res.data.top_total
          }
      })
    },
    // 获取更多一级评论
    addMore(){
        if (this.top_total <= this.comments.length) {
          return;
        }
        this.page++
        this.getComments()
    },
    // 获取更多二级评论
    addChildMore(id){
     this.childPage++
     this.getChildComment(id)
    },
    //二级评论
    getChildComment(id) {
      let data = {
        api:"plugin.bbs.Appbbs.getChildComment",
        id: id,
        page: this.childPage,
        pagesize: 10,
      }
      this.$req.post({data}).then(res=>{
          if (res.code == 200) {
                let index = this.comments.findIndex(item => item.id == id)
                console.log(index,this.childPage,'this.childPage')
                if (this.childPage == 1) {
                    this.comments[index].replies = res.data.list;
                    this.comments[index].showReplies = true;
                }else{
                    this.comments[index].replies = this.comments[index].replies.concat(res.data.list);
                }
                
          }
      })
    },    
    // 添加新评论
    addComment(newComment) {
      console.log(newComment,'newComment')
      this.isLogin(() => {
        let data = {
         api:"plugin.bbs.Appbbs.commentPost",
         id: this.id,
         top_id: this.top_id,
         content:newComment,
        }  
        if (this.parentId) {
          data.parentId = this.parentId
          this.$req.post({data}).then(res=>{ 
               if (res.code == 200) {
                if (this.parentId  ==  this.top_id) {
                    var newReplies = [res.data.data, ...this.comments[this.commentIdx].replies];
                }else{
                    var newReplies = [...this.comments[this.commentIdx].replies,res.data.data];
                }
                this.$set(this.comments[this.commentIdx], 'replies', newReplies);
                this.comments[this.commentIdx].reply_num = this.comments[this.commentIdx].reply_num + 1;
                this.commentCount = this.commentCount + 1
                if (newReplies.length > 0) {
                   this.$set(this.comments[this.commentIdx], 'showReplies', true);
                   this.$set(this.comments[this.commentIdx], 'has_sub_comments', true);
                }
                 this.placeholder = '说点什么吧...'
                 this.parentId = ''
                 this.top_id =''
               }
          })

        }else{
            this.$req.post({data}).then(res=>{ 
               if (res.code == 200) {
                 this.comments.unshift(res.data.data)
                 this.commentCount = this.commentCount + 1
                 this.scrollTop = 0;
                 this.placeholder = '说点什么吧...'
               }
            })
        }          
      })
    },

    onClear(){
        this.placeholder = '说点什么吧...'
        this.parentId = ''
        this.top_id =''
    },
    onScroll(e){
      this.scrollTop = e.detail.scrollTop;
    },
    // 点赞评论（接收组件事件）
    likeComment(id) {
        this.isLogin(() => {
          let data = {
            api:"plugin.bbs.Appbbs.likeComment",
            id: id,
          }
          this.$req.post({data}).then(res=>{
            if (res.code == 200) {
                let index = this.comments.findIndex(item => item.id == id)
                this.comments[index].is_like = !this.comments[index].is_like;
                this.comments[index].like_num = this.comments[index].like_num + (!this.comments[index].is_like ? -1 : 1)
            }
          })
        })
    },
    // 列表显示
    toggleReplies(id,commentIdx) {
      this.childPage = 1
      if (!this.comments[commentIdx].showReplies) {
        this.commentIdx = commentIdx;
        this.getChildComment(id);
      }else{
        this.comments[commentIdx].showReplies = false
      }
    },
    //二级回复
    toggleReplyToInput(id) {
     this.childPage = 1;
     let parentIndex = -1; // 父级评论在comments中的索引
     let selfIndex = -1; // 当前回复在父级replies中的索引
     let targetParentComment = null; // 找到的父级评论对象
        // 遍历顶级评论数组，查找包含目标id的父级评论
        this.comments.some((comment, pIdx) => {
            // 检查当前评论是否有回复，且回复是数组
            if (comment.replies && Array.isArray(comment.replies)) {
              // 在当前评论的回复中查找目标id
              const sIdx = comment.replies.findIndex(reply => reply.id === id);
              if (sIdx !== -1) {
                parentIndex = pIdx;
                selfIndex = sIdx;
                targetParentComment = comment;
                return true;
            }
        }
            return false;
        });
       if (targetParentComment && parentIndex !== -1 && selfIndex !== -1) {
         this.parentId = id;
         this.top_id = targetParentComment.id;
         this.commentIdx = parentIndex;
         this.placeholder = `回复: ${targetParentComment.replies[selfIndex].user_name}`;
       } else {
         this.parentId = null;
         this.top_id = null;
         this.placeholder = '';
         this.commentIdx = '';
       }
    },
    //一级回复
    toggleReplyInput(id){
      this.top_id = id
      this.parentId = id
      this.page = 1
      let index = this.comments.findIndex(item => item.id == id)
      this.commentIdx = index;
      this.placeholder = '回复: ' + this.comments[index].user_name
    },

    // 回复点赞
    likeReply(id, index) {
      this.isLogin(() => {
          let data = {
            api:"plugin.bbs.Appbbs.likeComment",
            id: id,
          }
          this.$req.post({data}).then(res=>{
            if (res.code == 200) {
                let item = this.comments[this.commentIdx].replies[index]
                item.like_num = item.like_num + (item.is_like ? -1 : 1)
                item.is_like =  !item.is_like;
            }
          })
        })
    },    
  }
}
