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

export default {
  name: 'c_nav_bar',
  componentsName: 'nav_bar',
  cname: '导航',
  props: {
    activeIndex: {
      type: null
    },
    num: {
      type: null
    },
    index: {
      type: null
    }
  },
  components: {
    // formCreate: formCreate.$form()
    ...toolCom,
    rightBtn
  },
  watch: {
    num(nVal) {
      const defaultArray = this.$store.state.admin.mobildConfig.defaultArray
      console.log('c_nav_bar num::', nVal, defaultArray)
      const value = JSON.parse(JSON.stringify(defaultArray[nVal]))

      this.configObj = value[nVal] ? value[nVal] : value;
    },
    configObj: {
      handler(nVal, oVal) {
        console.log('c_nav_bar configObj::', nVal[this.num])
        this.$store.commit('admin/mobildConfig/UPDATEARR', { num: this.num, val: nVal[this.num] ? nVal[this.num] : nVal });
      },
      deep: true
    }
  },

  data() {
    return {
      configObj: {},
      rCom: [
        {
          components: toolCom.c_bg_color,
          configNme: 'txtColor'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'txtSelectColor'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'rightBorderColor'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'selectBorderColor'
        },
        {
          components: toolCom.c_bg_color,
          configNme: 'bgColor'
        },
        {
          components: toolCom.c_slider,
          configNme: 'mbConfig'
        }
        // toolCom.c_bg_color,
        // toolCom.c_slider
      ]
    }
  },
  mounted() {
    this.$nextTick(() => {
      const defaultArray = this.$store.state.admin.mobildConfig.defaultArray
      console.log('c_nav_bar num::', this.num, defaultArray)
      const value = JSON.parse(JSON.stringify(defaultArray[this.num]))
      console.log('valuevaluevaluevalue', value, this.num)
      this.configObj = value[this.num] ? value[this.num] : value
    })
  },
  methods: {
    // 获取组件参数
    getConfig(data) {

    }
  }
}
</script>

<style scoped>
</style>
