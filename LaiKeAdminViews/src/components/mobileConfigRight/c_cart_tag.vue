<template>
  <div class="goods-box">
    <div>
      <div>
        <span class="title">标签</span>
      </div>
      <div class="hied">
        <p>
          <span>标签显示/隐藏</span>
          <ASwitch v-model="defaults.pageConfig.tabIsShow"></ASwitch>
        </p>
        <p>
          <span>标题名称</span>
          <span>拖拽可调整图标顺序</span>
        </p>
        <div>
          <draggable v-model="pageTypeList" :options="{animation:200}">
            <div v-for="(item,index) in pageTypeList" :key="index" class="draggable-item">
              <div style="margin: 10px;flex: none;">
                <img src="../../assets/imgs/sixdian.png" alt="" width="10px">
              </div>
              <div>
                标签名称
              </div>
              <div style="position: relative;flex: 1;margin-left: 10px;">
                <Input v-model="item.name" style="width: 100%;" />

              </div>
            </div>
          </draggable>
          <!-- <div class="add-but" @click="addList" v-if="pageTypeList.length<4">
                            + 添加
                        </div> -->
        </div>
      </div>
    </div>
  </div>
</template>
  
  <script>
import vuedraggable from 'vuedraggable'
import { Switch, Input } from 'iview'
export default {
  name: 'c_cart_tag',
  props: {
    configObj: {
      type: Object
    }
  },
  components: {
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
        if (this.defaults && this.defaults.pageConfig) {
          const { pageConfig } = this.defaults
          this.pageTypeList = pageConfig.pageTypeList
        }
      },
      immediate: true,
      deep: true
    },
    pageTypeList: {
      handler(nVal, oVal) {
        this.configObj.pageConfig.pageTypeList = nVal
      },
      deep: true
    }
  },
  data() {
    return {
      modals: false,
      pageTypeList: [],
      tempGoods: {},
      defaults: {}
    }
  },
  created() {
    this.defaults = this.configObj
  },
  methods: {
    change() {

    },
    addList() {
      if (this.pageTypeList.length >= 4) {
        this.$Message.warning('最多添加4个标签')
        return
      }
      this.pageTypeList.push({
        name: ''
      })
    },
    dlePageTypeList(index) {
      this.pageTypeList.splice(index, 1)
    }

  }
}
  </script>
  
  <style scoped lang="less">
.icondel_1 {
  position: absolute;
  right: -2px;
  top: 5px;
  transform: translateY(-50%);
  font-size: 20px;
  cursor: pointer;
}
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
.add-but {
  width: 100%;
  height: 38px;
  background: #fcfcfc;
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #d5dbe8;
  text-align: center;

  color: #7d869c;
  font-weight: normal;
  line-height: 14px;

  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.draggable-item {
  display: flex;
  align-items: center;
  background: #f4f7f9;
  padding: 0px 8px;
  margin: 10px 0;
  div:first-child {
    flex: 1;
  }
  div {
    margin: 5px 0;
  }
}
</style>
  