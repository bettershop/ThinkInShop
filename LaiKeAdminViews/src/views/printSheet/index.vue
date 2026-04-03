<template>
  <div class="container">
    <div class="list">
      <!-- <h1>欢迎来到电子面单打印界面</h1> -->
      <div class="imgItem" v-for="i in list">
        <img :src="i.label" alt="" srcset="" />
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: "PrintSheet",
  props: {},
  components: {},
  data() {
    return {
      list:[]
    };
  },
  computed: {},
  watch: {},
  created() {
    this.list=JSON.parse(this.$route.query.list)
  },
  mounted() {
    this.$nextTick(() => {
      const images = this.$el.querySelectorAll('img');
      let loadedCount = 0;
      const total = images.length;
      let hasPrinted = false;
      const maxWaitTime = 8000; // 最大等待时间5秒

      const handlePrint = () => {
        if (hasPrinted) return;
        hasPrinted = true;
        window.print();
        setTimeout(() => window.close(), 300);
      };

      // 设置备用超时
      const timeoutId = setTimeout(handlePrint, maxWaitTime);

      if (total === 0) {
        handlePrint();
        return;
      }

      const imageLoaded = () => {
        loadedCount++;
        if (loadedCount === total) {
          clearTimeout(timeoutId);
          handlePrint();
        }
      };

      images.forEach(img => {
        if (img.complete) {
          imageLoaded();
        } else {
          img.addEventListener('load', imageLoaded);
          img.addEventListener('error', imageLoaded); // 处理加载失败
        }
      });
    });
  },
  methods: {},
};
</script>

<style scoped lang="less">
.container {
  width: 100%;
}
.list {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  padding: 1px;
  box-sizing: border-box;

  .imgItem {
    width: 100%;
    padding: 0;
    box-sizing: border-box;
    page-break-after: always; /* 每张面单单独一页 */

    img {
      width: 100%;
      height: auto;     /* ✅ 保持原始宽高比，防止拉伸变形 */
      display: block;   /* ✅ 消除行内元素底部空白 */
    }
  }
}
</style>
