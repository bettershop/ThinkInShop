<template>
  <div class="goods-box">
    <div style="margin-top: 10px;">
      <div>
        <span class="title">标题</span>
      </div>
      <div class="hied">
        <p>
          <span>标题显示/隐藏</span>
          <ASwitch v-model="defaults.pageConfig.pageNameShow"></ASwitch>
        </p>
        <p>
          <span>标题名称</span>
          <Input class="my-input" v-model="defaults.pageConfig.pageName" />
        </p>
      </div>
    </div>
  </div>
</template>
  
  <script>
import vuedraggable from 'vuedraggable'
import goodsList from '@/components/goodsList'
import { Switch, Input } from 'iview'
export default {
  name: 'c_cart_title',
  props: {
    configObj: {
      type: Object
    }
  },
  components: {
    goodsList,
    'ASwitch': Switch,
    'Input': Input,
    draggable: vuedraggable,
  },
  watch: {
    configObj: {
      handler(nVal, oVal) {
        if (nVal && Object.keys(nVal).length) {
          this.defaults = nVal
        }
      },
      immediate: true,
      deep: true
    }
  },
  data() {
    return {
      modals: false,
      goodsList: [],
      tempGoods: {},
      defaults: {}
    }
  },
  created() {
    this.defaults = this.configObj
  },
  methods: {


  }
}
  </script>
  
  <style scoped lang="less">
.title {
  font-family: MicrosoftYaHei;
  font-size: 14px;
  font-weight: normal;
  line-height: 14px;
  letter-spacing: 0em;

  color: #414658;
}
.hied {
  p {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 10px 0;
  }
  span {
    user-select: none;
    font-family: MicrosoftYaHei;
    font-size: 14px;
    font-weight: normal;
    line-height: 14px;
    letter-spacing: 0em;

    color: #7d869c;
  }
  .my-input {
    width: 176px;
    height: 32px;
    border-radius: 4px;
    opacity: 1;
  }
}
</style>
  