<template>
  <div class="container">
    <div class="notice">
      <header>{{$t('systemNotice.scgg')}}</header>
      <el-empty :description=" $t('zdata.zwsj')" :image="require('../../../assets/imgs/empty.png')"  v-if="noticeList.length<=0" style="height: 60vh;">
      </el-empty>
      <div class="list">
        <div class="item" @click="detail(item.id)" v-for="(item,index) in noticeList" :key="index">
          <p class="title">
            <span>{{item.title}}</span>
            <img v-if="!item.is_read" :src="language=='en'?require(`../../../assets/imgs/weidunew.png`):require(`../../../assets/imgs/weidu.png`)" alt="">
          </p>
          <div class="date">{{item.startdate | dateFormat}}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import lang from "@/store/modules/lang";
import main from "@/webManage/js/mall/systemNotice/noticeList";

export default main
</script>

<style scoped lang="less">
  @import "../../../webManage/css/mall/systemNotice/noticeList";
  /deep/.notice{
    .el-empty__image img{
      width: 100px;
    }
  }
</style>
