<template>
  <div class="container">
    <div class="commentAll iframe-table">
      <!-- <p class="page_title">修改评论</p> -->
      <div class="userbox"><img
        :src="mainData.headimgurl">
        <div class="userbox-body">
          <div class="singlebox-username">{{mainData.user_name}}</div>
          <div class="singlebox"  v-if="!isEdit">
            <div class="singlebox-starlist start">
              <el-rate
                v-model="star"
                show-text :texts="starGroup">
              </el-rate>
            </div>
            <div class="singlebox-time">{{mainData.add_time | dateFormat}}</div>
            <div class="singlebox-content">{{mainData.content}}
              <!-- <i class="laiketui laiketui-xiugai" @click="isEdit=!isEdit"/> -->
            </div>
            <!-- 评论图 -->
            <div class="singlebox-imglist">
              <div v-if="isEdit" class="singlebox_edit">
                <l-upload
                  :limit="-1"

                  v-model="commentImgList"
                  text=""
                >
                </l-upload>
              </div>
              <div v-if="!isEdit" class="singlebox_look">
                <l-upload
                  :limit="-1"

                  v-model="commentImgList"
                  text=""
                >
                </l-upload>
              </div>
            </div>
            <div class="singlebox-reply">
              <span>{{$t('commentEdit.zghf')}}：{{mainData.replyAdmin}}</span>
            </div>

            <!-- 追评 -->
            <div class="singlebox" v-if="mainData.review!=null">
              <div class="singlebox-username singlebox-red">{{$t('commentEdit.zjpl')}}</div>
              <div class="singlebox-time">{{mainData.review_time | dateFormat}}</div>
              <div class="singlebox-content">
                {{mainData.review == 'undefined' ? '' : mainData.review}}
              </div>
              <!-- 追评图片 -->
              <div class="singlebox-imglist">
                <span v-for="(item,i) in mainData.review_images" :key="i">
                  <img :src="item.url" alt=""/>
                </span>
              </div>
            </div>

          </div>
          <!-- <div class="xian" v-if="isEdit"> 修改</div> -->
          <!-- 修改区域 begin -->
          <div class="edit-box xian" v-if="isEdit">
            <div class="singlebox">
              <div class="singlebox-starlist-2 singlebox-starlist">
                <div class="start">
                  <el-rate
                    v-model="mainData.CommentType"
                    show-text :texts="starGroup">
                  </el-rate>
                </div>
              </div>
              <div class="singlebox-time">{{mainData.add_time | dateFormat}}</div>
              <textarea name="edit_content" id="content" v-model="mainData.content" cols="30" rows="2">{{mainData.content}}</textarea>
              <div class="l-upload" v-if="commentImgList!=null">
                <l-upload
                  :limit="5"
                  v-model="commentImgList"
                  text=""
                >
                </l-upload>
              </div>
              <div class="singlebox-reply">
                <span>{{$t('commentEdit.zghf')}}：{{mainData.replyAdmin}}</span>
              </div>
              <!-- 追评 -->
              <div class="singlebox" v-if="mainData.review!=null">
                <div class="singlebox-username singlebox-red">{{$t('commentEdit.zjpl')}}</div>
                <div class="singlebox-time">{{mainData.review_time | dateFormat}}</div>
                <textarea class="singlebox-textarea" name="edit_content" cols="30" v-model="mainData.review" rows="2">{{mainData.review}}</textarea>
                <!-- 追评图片 -->
                <div class="singlebox-imglist" v-if="reviewImgList!=null">
                  <l-upload
                    :limit="5"
                    v-model="reviewImgList"
                    text=""
                  >
                  </l-upload>
                </div>
              </div>
            </div>
            <div>
              <el-button v-if="isEdit" class="bgColor pull-right" type="primary" @click="Save()">
                {{$t('DemoPage.tableFromPage.save') }}
              </el-button>
              <el-button class="fontColor pull-right" type="" @click="$router.go(-1)">
                {{$t('commentEdit.fh')}}
              </el-button>
            </div>
          </div>
          <!-- 修改区域 end -->
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import main from "@/webManage/js/order/commentManage/commentEdit";
export default main;
</script>


<style scoped lang="less">
  /deep/.singlebox_look { 
    .el-icon-delete{
      display: none !important;
      }
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
  @import "../../../common/commonStyle/form";
  @import "../../../webManage/css/order/commentManage/commentEdit.less";
</style>

