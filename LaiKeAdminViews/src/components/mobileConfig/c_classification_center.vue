<template>
  <div class="mobile-config">
    <div v-for="(item,key) in rCom" :key="key">
      <component :is="item.components.name" :configObj="configObj" ref="childData" :configNme="item.configNme" :key="key" @getConfig="getConfig" :index="activeIndex" :num="item.num"></component>
    </div>
    <rightBtn :activeIndex="activeIndex" :configObj="configObj"></rightBtn>
  </div>
</template>

  <script>
import toolCom from '@/components/mobileConfigRight/index.js'
import rightBtn from '@/components/rightBtn/index.vue';
import { mapMutations } from 'vuex'

export default {
  name: 'c_classification_center',
  componentsName: 'classification',
  components: {
    ...toolCom,
    rightBtn
  },
  props: {
    activeIndex: {
      type: null
    },
    num: {
      type: [String, Number]
    },
    index: {
      type: null
    }
  },
  data() {
    return {
      configObj: {},
      lockStatus: false,
      bgStatus: false,
      rCom: [

        {
          components: toolCom.c_classification_info,
          configNme: 'c_classification_info'
        },
      ]
    }
  },
  watch: {
    num: {
      handler(nVal, oVal) {
        console.log('分类页面的 num', nVal)
        if (this.$store.state.admin.mobildConfig.defaultArray) {
          const value = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray[nVal]))
          this.configObj = value;
          this.$set(this.configObj, 'link_key', nVal)
          this.$set(this.configObj, 'num', nVal)
        }
      },
      deep: true,
      immediate: true
    },
    configObj: {
      handler(nVal, oVal) {
        this.$store.commit('admin/mobildConfig/UPDATEARR', { num: this.num, val: nVal });
      },
      deep: true
    }
  },
  mounted() {
    this.$nextTick(() => {
      console.log('分类页面的', this.$store.state.admin.mobildConfig.defaultArray, this.num)
      if (this.$store.state.admin.mobildConfig.defaultArray) {
        const value = JSON.parse(JSON.stringify(this.$store.state.admin.mobildConfig.defaultArray[this.num]))
        this.configObj = value;
        this.$set(this.configObj, 'link_key', this.num)
        this.$set(this.configObj, 'num', this.num)
      }
    })
  },
  methods: {
    // 获取组件参数
    getConfig(data) {

    },
    ...mapMutations({
      add: 'admin/mobildConfig/UPDATEARR'
    })
  }
}
  </script>

  <style scoped lang="stylus">
  .title-tips {
    padding-bottom: 10px;
    font-size: 14px;
    color: #333;

    span {
      margin-right: 14px;
      color: #999;
    }
  }

  .quer-but {
    border: 1px solid #ccc;
    border-radius: 5px;
    padding: 5px 10px;
    color: #000;
    cursor: pointer;
    user-select: none;
    margin: 10px 5px;
  }

  .avitive {
    border: 1px solid #3190f0;
    color: #3190f0;
  }
</style>
