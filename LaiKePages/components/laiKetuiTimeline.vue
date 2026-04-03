<template>
   <!-- 
   组件介绍 ：时间线 
   使用方式 <lktTimeline :events='list.list'></lktTimeline> --> 
  <div class="timeline"> 
  <template v-if="events.length >0">
    <div v-for="(item, index) in events" :key="index" class="timeline-item">
      <div class="circle"></div>
      <div class="content">
        <div class="time"> {{ item.time }}</div>
        <div class="item_context">{{ item.context }}</div>
      </div>
      <div v-if="index < events.length - 1" class="line"></div>
    </div>
    </template>
    <div class='zwwl' v-else>
        <img :src="zwwl" style='width: 750rpx;height: 460rpx;'>
        {{language.expressage.logistics_tips}}~
    </div>
  </div>
</template>

<script>
export default {
  name: 'Timeline',
  props: {
    events: {
      type: Array,
      default:()=>[]
    }
  },
  data(){
      return{
        zwwl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zwwl.png',
      }
  }
}
</script>

<style scoped lang="less">
.timeline {
  position: relative;
  padding-left: 40rpx; /* 留出空间放置圆点 */
   .timeline-item:nth-child(1) {
       .circle{
            background-color: #f55451; /* 圆点的颜色 */
       }
  }
}
.zwwl {
    text-align: center;
    display: flex;
    flex-direction: column;
    padding-top: 224rpx;
    font-size: 28rpx;
    color: #333333;
    justify-content: center;
    align-items: center;
}
.time{
    font-size: 24rpx;
    color: #666;
    margin-bottom: 10rpx;
 
}
.item_context {
    margin-left: 10rpx;
    margin-bottom: 10rpx;
}
.timeline-item {
  position: relative;
  margin: 24rpx 0; /* 每个时间节点的间距 */
}

.circle {
  width: 24rpx;
  height: 24rpx;
  background-color: #cccccc; /* 圆点的颜色 */
  border-radius: 50%;
  position: absolute;
  left: 0; /* 圆点在左边 */
  top: 0;
}


.content {
  padding-left: 40rpx; /* 内容与圆点间距 */
}

.line {
  width: 4rpx; /* 线的宽度 */
  background-color: #cccccc; /* 线的颜色 */
  position: absolute;
  left: 12rpx; /* 线与圆点对齐 */
  top: 24rpx; /* 线的起始位置 */
  height: calc(100% - 0rpx); /* 线的高度 */
}
</style>
