<template>
  <div id="app">
    <router-view v-if="isRouterAlive" />
  </div>
</template>

<script>
export default {
  name: 'App',
  provide() {
    return {
      reload: this.reload
    }
  },
  data() {
    return {
      isRouterAlive: true
    }
  },

  watch: {
    '$i18n.locale'(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.reload()
      }
    }
  },
  methods: {
    reload() {
      this.isRouterAlive = false
      this.$nextTick(function () {
        this.isRouterAlive = true
      })
    }
  }
}
</script>

<style lang="less"  >
.diy-titleName {
  margin-bottom: 11px;
  line-height: 1;
  margin-top: 6px;

  > span:first-child {
    font-family: Source Han Sans, Source Han Sans;
    font-weight: 500;
    font-size: 16px;
  }
}
.diy-newPrice {
  font-family: Source Han Sans, Source Han Sans;
  font-weight: 700;
  font-size: 17px;
  color: #fa5151;
  line-height: 17px;
  text-align: left;
  font-style: normal;
  text-transform: none;
}
.diy-lodPrice {
  font-family: Source Han Sans, Source Han Sans;
  font-weight: 500;
  font-size: 10px;
  color: #999999;
  line-height: 10px;
  text-align: left;
  font-style: normal;
  text-decoration-line: line-through;
  text-transform: none;
}
</style>